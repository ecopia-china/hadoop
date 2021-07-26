package com.ecopiatech.platform.hadoop.fs;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.fs.s3a.*;
import org.apache.hadoop.fs.s3a.commit.CommitConstants;
import org.apache.hadoop.fs.s3a.commit.MagicCommitIntegration;
import org.apache.hadoop.fs.s3a.commit.PutTracker;
import org.apache.hadoop.fs.store.EtagChecksum;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.util.BlockingThreadPoolExecutorService;
import org.apache.hadoop.util.Progressable;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.*;
import org.apache.hadoop.util.SemaphoredDelegatingExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.hadoop.fs.s3a.Constants.*;
import static com.ecopiatech.platform.hadoop.fs.ExfsUtils.*;

public class ExfsFileSystem extends FileSystem {
  public ExfsFileSystem() {
    s3fs = new S3AFileSystem();
  }
  public static class ListDir_return extends Structure {
    public static class ByValue extends ListDir_return implements Structure.ByValue {
    }
    public Pointer r0;
    public int r1;
    protected List<String> getFieldOrder() {
      return Arrays.asList("r0", "r1");
    }
  }

  public static class FileStat_return extends Structure {
    public static class ByValue extends FileStat_return implements Structure.ByValue {
    }
    public Pointer r0;
    public Pointer r1;
    public int r2;
    protected List<String> getFieldOrder() {
      return Arrays.asList("r0", "r1", "r2");
    }
  }

  public interface LibExfs extends Library {
    int InitExfs(String redisURL);
    FileStat_return.ByValue FileStat(String path);
    ListDir_return.ByValue ListDir(String path, String contain);
    int CreateDirAll(String path, int permission);
    int MoveEntry(String src, String dst);
    int DeleteDir(String path);
    int DeleteFile(String path);
    int CreateObjFile(String path, String objectPath, String objectETag,
                      long length, boolean isShared, int mode);
  }

  private LibExfs libexfs;
  final private S3AFileSystem s3fs;

  private URI uri;
  private Path workingDir;
  private String username;

  public static final Logger LOG = LoggerFactory.getLogger(ExfsFileSystem.class);

  private long partSize;
  private String blockOutputBuffer;
  private ExfsDataBlocks.BlockFactory blockFactory;
  private int blockOutputActiveBlocks;
  private ListeningExecutorService boundedThreadPool;
  private ExfsInstrumentation instrumentation;
  private MagicCommitIntegration committerIntegration;
  private LocalDirAllocator directoryAllocator;

  /** Called after a new FileSystem instance is constructed.
   * @param name a uri whose authority section names the host, port, etc.
   *   for this FileSystem
   * @param conf the configuration to use for the FS. The
   * bucket-specific options are patched over the base ones before any use is
   * made of the config.
   */
  public void initialize(URI name, Configuration conf)
      throws IOException {
    LOG.debug("initalize exfs, uri: " + name);
    InputStream is = ClassLoader.class.getResourceAsStream("/libexfs.so");
    File file = File.createTempFile("lib", ".so");
    OutputStream os = new FileOutputStream(file);
    byte[] buffer = new byte[1024];
    int length;
    while ((length = is.read(buffer)) != -1) {
      os.write(buffer, 0, length);
    }
    is.close();
    os.close();
    file.deleteOnExit();

    try {
      libexfs = Native.load(file.getAbsolutePath(), LibExfs.class);
      if (libexfs == null) {
        throw new IOException("Exfs initialized failed for exfs://" + name);
      }

      String redis_url = conf.get("fs.exfs.redis.url", "");
      if (redis_url.equals("")) {
        throw new IOException("fs.exfs.redis.url is required");
      }
      int ret = libexfs.InitExfs(redis_url);
      if (ret != 0) {
        throw new IOException("Exfs initialized failed for exfs://" + name);
      }
      s3fs.initialize(URI.create("s3a://ecopia-platform"), conf);
      uri = name;
      workingDir = new Path("/");
      username = UserGroupInformation.getCurrentUser().getShortUserName();
      blockOutputActiveBlocks = intOption(conf,
          FAST_UPLOAD_ACTIVE_BLOCKS, DEFAULT_FAST_UPLOAD_ACTIVE_BLOCKS, 1);
      instrumentation = new ExfsInstrumentation(name);
      int maxThreads = conf.getInt(MAX_THREADS, DEFAULT_MAX_THREADS);
      if (maxThreads < 2) {
        LOG.warn(MAX_THREADS + " must be at least 2: forcing to 2.");
        maxThreads = 2;
      }
      int totalTasks = intOption(conf,
          MAX_TOTAL_TASKS, DEFAULT_MAX_TOTAL_TASKS, 1);
      long keepAliveTime = longOption(conf, KEEPALIVE_TIME,
          DEFAULT_KEEPALIVE_TIME, 0);
      boundedThreadPool = BlockingThreadPoolExecutorService.newInstance(
          maxThreads,
          maxThreads + totalTasks,
          keepAliveTime, TimeUnit.SECONDS,
          "s3a-transfer-shared");
      boolean magicCommitterEnabled = conf.getBoolean(
          CommitConstants.MAGIC_COMMITTER_ENABLED,
          CommitConstants.DEFAULT_MAGIC_COMMITTER_ENABLED);
      LOG.debug("Filesystem support for magic committers {} enabled",
          magicCommitterEnabled ? "is" : "is not");
      committerIntegration = new MagicCommitIntegration(
          s3fs, magicCommitterEnabled);

      partSize = getMultipartSizeProperty(conf,
          MULTIPART_SIZE, DEFAULT_MULTIPART_SIZE);
      blockOutputBuffer = conf.getTrimmed(FAST_UPLOAD_BUFFER,
          DEFAULT_FAST_UPLOAD_BUFFER);
      partSize = ensureOutputParameterInRange(MULTIPART_SIZE, partSize);
      blockFactory = ExfsDataBlocks.createFactory(this, blockOutputBuffer);
      blockOutputActiveBlocks = intOption(conf,
          FAST_UPLOAD_ACTIVE_BLOCKS, DEFAULT_FAST_UPLOAD_ACTIVE_BLOCKS, 1);
      LOG.debug("Using S3ABlockOutputStream with buffer = {}; block={};" +
              " queue limit={}",
          blockOutputBuffer, partSize, blockOutputActiveBlocks);

    } catch (Exception e) {
      throw new IOException("initializing " + new Path(name) + ", " + e);
    }
  }
  /**
   * Return the protocol scheme for the FileSystem.
   *
   * @return "exfs"
   */
  @Override
  public String getScheme() {
    LOG.debug("get scheme");
    return "exfs";
  }

  /**
   * Get the storage statistics of this filesystem.
   * @return the storage statistics
   */
  @Override
  public ExfsStorageStatistics getStorageStatistics() {
    LOG.debug("get storage statistics");
    return null;
  }

  /**
   * Check that a Path belongs to this FileSystem.
   * Unlike the superclass, this version does not look at authority,
   * only hostnames.
   * @param path to check
   * @throws IllegalArgumentException if there is an FS mismatch
   */
  @Override
  public void checkPath(Path path) {
    LOG.debug("check path: " + path);
  }

  /**
   * {@inheritDoc}
   * @throws FileNotFoundException if the parent directory is not present -or
   * is not a directory.
   */
  @Override
  public FSDataOutputStream createNonRecursive(Path path,
                                               FsPermission permission,
                                               EnumSet<CreateFlag> flags,
                                               int bufferSize,
                                               short replication,
                                               long blockSize,
                                               Progressable progress) throws IOException {
    LOG.debug("createNonRecursive: " + path);
    return null;
  }

  /**
   * @param f The file path
   * @param length The length of the file range for checksum calculation
   * @return The EtagChecksum or null if checksums are not enabled or supported.
   * @throws IOException IO failure
   * @see <a href="http://docs.aws.amazon.com/AmazonS3/latest/API/RESTCommonResponseHeaders.html">Common Response Headers</a>
   */
  @Override
  @Retries.RetryTranslated
  public EtagChecksum getFileChecksum(Path f, final long length)
      throws IOException {
    LOG.debug("get file checksum: " + f);
    return null;
  }

  /**
   * @param f a path
   * @param recursive if the subdirectories need to be traversed recursively
   *
       * @return an iterator that traverses statuses of the files/directories
   *         in the given path
   * @throws FileNotFoundException if {@code path} does not exist
   * @throws IOException if any I/O error occurred
   */
  @Override
  @Retries.OnceTranslated
  public RemoteIterator<LocatedFileStatus> listFiles(Path f, boolean recursive)
      throws FileNotFoundException, IOException {
    LOG.debug("list files: " + f);
    return null;
  }

  /**
   * Override superclass so as to add statistic collection.
   * {@inheritDoc}
   */
  @Override
  public RemoteIterator<LocatedFileStatus> listLocatedStatus(Path f)
      throws IOException {
    return listLocatedStatus(f, ACCEPT_ALL);
  }

  /**
   * @param f a path
   * @param filter a path filter
   * @return an iterator that traverses statuses of the files/directories
   *         in the given path
   * @throws FileNotFoundException if {@code path} does not exist
   * @throws IOException if any I/O error occurred
   */
  @Override
  @Retries.OnceTranslated("s3guard not retrying")
  public RemoteIterator<LocatedFileStatus> listLocatedStatus(
      final Path f, final PathFilter filter)
      throws FileNotFoundException, IOException {
    LOG.debug("list located status: " + f);
    return null;
  }

  /**
   * Returns a URI whose scheme and authority identify this FileSystem.
   */
  @Override
  public URI getUri() {
    LOG.debug("get uri: " + uri);
    return uri;
  }

  /**
   * Opens an FSDataInputStream at the indicated Path.
   * @param f the file name to open
   * @param bufferSize the size of the buffer to be used.
   */
  public FSDataInputStream open(Path f, int bufferSize)
      throws IOException {
    LOG.debug("open file: " + f);
    ExfsFileStatus s = getFileStatusInternal(f);
    return s3fs.open(toS3APath(s.getDataPath()), bufferSize);
  }

  /**
   * Create an FSDataOutputStream at the indicated Path with write-progress
   * reporting.
   * Retry policy: retrying, translated on the getFileStatus() probe.
   * No data is uploaded to S3 in this call, so retry issues related to that.
   * @param f the file name to open
   * @param permission the permission to set.
   * @param overwrite if a file with this name already exists, then if true,
   *   the file will be overwritten, and if false an error will be thrown.
   * @param bufferSize the size of the buffer to be used.
   * @param replication required block replication for the file.
   * @param blockSize the requested block size.
   * @param progress the progress reporter.
   * @throws IOException in the event of IO related errors.
   * @see #setPermission(Path, FsPermission)
   */
  @Override
  @SuppressWarnings("IOResourceOpenedButNotSafelyClosed")
  public FSDataOutputStream create(Path f, FsPermission permission,
                                   boolean overwrite, int bufferSize, short replication, long blockSize,
                                   Progressable progress) throws IOException {
    LOG.debug("create file: " + f);
    boolean exists;
    ExfsFileStatus s = null;
    try {
      s = getFileStatusInternal(f);
      exists = s != null;
    } catch (FileNotFoundException e) {
      exists = false;
    }

    if (exists && !overwrite) {
      throw new FileAlreadyExistsException(f + " already exist");
    } else if (exists && s.isDirectory()) {
      throw new FileAlreadyExistsException(f + " is a direcotry");
    } else if (exists && overwrite) {
      // Delete origin file
      if (!delete(f, false)) {
        LOG.error("delete previous file error: " + f);
      }
    }

    // Create in s3 fs
    // TODO check s3 repeat name
    URI s3uri = toS3URI(f);
    Path path = toS3APath(s3uri);
    String key = s3fs.pathToKey(path);
    instrumentation.fileCreated();
    PutTracker putTracker =
        committerIntegration.createTracker(path, key);
    String destKey = putTracker.getDestKey();
    return new FSDataOutputStream(
        new ExfsBlockOutputStream(this,
            destKey,
            new SemaphoredDelegatingExecutor(boundedThreadPool,
                blockOutputActiveBlocks, true),
            progress,
            partSize,
            blockFactory,
            instrumentation.newOutputStreamStatistics(statistics),
            s3fs.getWriteOperationHelper(),
            putTracker),
        null);
  }

  /**
   * Increment a statistic by 1.
   * This increments both the instrumentation and storage statistics.
   * @param statistic The operation to increment
   */
  protected void incrementStatistic(Statistic statistic) {
  }

  /**
   * Increment the write operation counter.
   * This is somewhat inaccurate, as it appears to be invoked more
   * often than needed in progress callbacks.
   */
  public void incrementWriteOperations() {
    statistics.incrementWriteOps(1);
  }

  /**
   * Demand create the directory allocator, then create a temporary file.
   * {@link LocalDirAllocator#createTmpFileForWrite(String, long, Configuration)}.
   *  @param pathStr prefix for the temporary file
   *  @param size the size of the file that is going to be written
   *  @param conf the Configuration object
   *  @return a unique temporary file
   *  @throws IOException IO problems
   */
  synchronized File createTmpFileForWrite(String pathStr, long size,
                                          Configuration conf) throws IOException {
    if (directoryAllocator == null) {
      String bufferDir = conf.get(BUFFER_DIR) != null
          ? BUFFER_DIR : HADOOP_TMP_DIR;
      directoryAllocator = new LocalDirAllocator(bufferDir);
    }
    return directoryAllocator.createTmpFileForWrite(pathStr, size, conf);
  }

  /**
   * Append to an existing file (optional operation).
   * @param f the existing file to be appended.
   * @param bufferSize the size of the buffer to be used.
   * @param progress for reporting progress if it is not null.
   * @throws IOException indicating that append is not supported.
   */
  public FSDataOutputStream append(Path f, int bufferSize, Progressable progress)
      throws IOException {
    LOG.debug("append file: " + f);
    throw new UnsupportedOperationException("Append is not supported by ExfsFileSystem");
  }

  /**
   * Renames Path src to Path dst.  Can take place on local fs
   * or remote DFS.
   *
   * @param src path to be renamed
   * @param dst new path after rename
   * @throws IOException on IO failure
   * @return true if rename is successful
   */
  public boolean rename(Path src, Path dst) throws IOException {
    LOG.debug("rename file, from "+ src +" to " + dst);
    int ret = libexfs.MoveEntry(toExfsPath(src), toExfsPath(dst));
    if (ret != 0) {
      throw new IOException(Errno.toString(ret));
    }
    return true;
  }

  /**
   * Delete a Path. This operation is at least {@code O(files)}, with
   * added overheads to enumerate the path. It is also not atomic.
   *
   * @param f the path to delete.
   * @param recursive if path is a directory and set to
   * true, the directory is deleted else throws an exception. In
   * case of a file the recursive can be set to either true or false.
   * @return true if the path existed and then was deleted; false if there
   * was no path in the first place, or the corner cases of root path deletion
   * have surfaced.
   * @throws IOException due to inability to delete a directory or file.
   */
  @Retries.RetryTranslated
  public boolean delete(Path f, boolean recursive) throws IOException {
    LOG.debug("delete file: " + f);
    ExfsFileStatus s;
    try {
      s = getFileStatusInternal(f);
    } catch (FileNotFoundException e) {
      return false;
    }
    if (s.isDirectory()) {
      if (recursive) {
        deleteRecursive(f);
      } else {
        int ret = libexfs.DeleteDir(toExfsPath(f));
        if (ret != 0) {
          throw new IOException(Errno.toString(ret));
        }
      }
    } else if (s.isFile()) {
      int ret = libexfs.DeleteFile(toExfsPath(f));
      if (ret != 0) {
        throw new IOException(Errno.toString(ret));
      }
      if (!deleteS3Object(s.getDataPath())) {
        LOG.error("delete object error: " + s);
      }
    }
    return true;
  }

  private boolean deleteRecursive(Path f) throws IOException {
    ExfsFileStatus[] ss = listStatusInternal(f);
    LOG.debug("dir: " + f + " entry size: " + ss.length);
    for (ExfsFileStatus s : ss) {
      if (s.isDirectory()) {
        LOG.debug("delete dir recursive: " + s.getPath());
        deleteRecursive(s.getPath());
      } else {
        LOG.debug("delete file: " + s.getPath());
        int ret = libexfs.DeleteFile(toExfsPath(s.getPath()));
        if (ret != 0) {
          throw new IOException(Errno.toString(ret));
        }
        if (!deleteS3Object(s.getDataPath())) {
          LOG.error("delete object error: " + s);
        }
      }
    }
    LOG.debug("delete dir: " + f);
    int ret = libexfs.DeleteDir(toExfsPath(f));
    if (ret != 0) {
      throw new IOException(Errno.toString(ret));
    }
    return true;
  }

  private boolean deleteS3Object(URI s3uri) {
    try {
      s3fs.delete(toS3APath(s3uri), false);
    } catch (Exception e) {
      LOG.error("delete s3 object error: " + e);
      return false;
    }
    return true;
  }

  private Path toS3APath(URI s3uri) {
    URI s3auri;
    try {
      s3auri = new URI(
        "s3a", s3uri.getHost(), s3uri.getPath(), s3uri.getFragment()
      );
    } catch (URISyntaxException e) {
      LOG.error("parse uri error: " + e);
      return null;
    }
    return new Path(s3auri);
  }

  private URI toS3URI(Path exfs) {
    URI u = exfs.toUri();
    URI s3uri = null;
    try {
      s3uri = new URI(
          "s3", "ecopia-platform/"+u.getHost(), u.getPath(), u.getFragment()
      );
    } catch (URISyntaxException e) {
      LOG.error("parse uri error: " + e);
    }
    return s3uri;
  }

  /**
   * List the statuses of the files/directories in the given path if the path is
   * a directory.
   *
   * @param f given path
   * @return the statuses of the files/directories in the given patch
   * @throws FileNotFoundException when the path does not exist;
   *         IOException see specific implementation
   */
  public FileStatus[] listStatus(Path f) throws IOException {
    return listStatusInternal(f);
  }

  public ExfsFileStatus[] listStatusInternal(Path f) throws IOException {
    LOG.debug("list status: " + f);
    Pointer ptr = null;
    try {
      ListDir_return.ByValue ret = libexfs.ListDir(toExfsPath(f), "");
      if (ret.r1 != 0) {
        throw new IOException(Errno.toString(ret.r1));
      }
      ptr = ret.r0;
      String j = ptr.getString(0, "utf8");
      List<Entry> entries = JSON.parseArray(j, Entry.class);
      List<ExfsFileStatus> status = new ArrayList<>();
      for (Entry e : entries) {
        if (e.name.equals(".") || e.name.equals("..")) {
          continue;
        }
        status.add(new ExfsFileStatus(e.attr, new Path(f, e.name), username));
      }
      ExfsFileStatus[] output = new ExfsFileStatus[status.size()];
      return status.toArray(output);
    } finally {
      if (ptr != null) {
        Native.free(Pointer.nativeValue(ptr));
      }
    }
  }

  /**
   * Set the current working directory for the given file system. All relative
   * paths will be resolved relative to it.
   *
   * @param newDir the current working directory.
   */
  public void setWorkingDirectory(Path newDir) {
    LOG.debug("set working dir: " + newDir);
    workingDir = newDir;
  }

  /**
   * Get the current working directory for the given file system.
   * @return the directory pathname
   */
  public Path getWorkingDirectory() {
    LOG.debug("get working dir: " + workingDir);
    return workingDir;
  }

  /**
   * Make the given path and all non-existent parents into
   * directories. Has the semantics of Unix {@code 'mkdir -p'}.
   * Existence of the directory hierarchy is not an error.
   * @param path path to create
   * @param permission to apply to f
   * @return true if a directory was created or already existed
   * @throws FileAlreadyExistsException there is a file at the path specified
   * @throws IOException other IO problems
   */
  public boolean mkdirs(Path path, FsPermission permission)
      throws IOException, FileAlreadyExistsException {
    LOG.debug("mkdirs: " + path);
    int ret = libexfs.CreateDirAll(toExfsPath(path), permission.toShort());
    if (ret == 17 /* EEXIST 17 File exists */) {
      throw new FileAlreadyExistsException(path + " already exist");
    } else if (ret != 0) {
      throw new IOException(Errno.toString(ret));
    }
    return true;
  }

  /**
   * Return a file status object that represents the path.
   * @param f The path we want information from
   * @return a FileStatus object
   * @throws FileNotFoundException when the path does not exist
   * @throws IOException on other problems.
   */
  @Retries.RetryTranslated
  public FileStatus getFileStatus(final Path f) throws IOException {
    return getFileStatusInternal(f);
  }

  private ExfsFileStatus getFileStatusInternal(final Path f) throws IOException {
    LOG.debug("get file status: " + f);
    Pointer ptr0 = null;
    Pointer ptr1 = null;
    try {
      FileStat_return ret = libexfs.FileStat(toExfsPath(f));
      if (ret.r2 == 2 /* ENOENT 2 No such file or directory */) {
        throw new FileNotFoundException(f + " not exist");
      } else if (ret.r2 != 0) {
        throw new IOException(Errno.toString(ret.r2));
      }
      ptr0 = ret.r0;
      ptr1 = ret.r1;
      String j = ptr0.getString(0);
      String j1 = ptr1.getString(0);
      Attr attr = JSON.parseObject(j, Attr.class);
      FileData data = JSON.parseObject(j1, FileData.class);
      ExfsFileStatus s = new ExfsFileStatus(attr, data, f, username);
      LOG.debug("get file status res: " + s + " attr: " + attr + " data: " + data);
      return s;
    } finally {
      if (ptr0 != null) {
        Native.free(Pointer.nativeValue(ptr0));
      }
      if (ptr1 != null) {
        Native.free(Pointer.nativeValue(ptr1));
      }
    }
  }

  private String toExfsPath(Path p) {
    URI uri = p.toUri();
    return uri.getHost() + uri.getPath();
  }
}
