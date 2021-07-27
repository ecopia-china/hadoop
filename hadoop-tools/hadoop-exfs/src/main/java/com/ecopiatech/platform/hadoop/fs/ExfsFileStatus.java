package com.ecopiatech.platform.hadoop.fs;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;

import java.net.URI;

public class ExfsFileStatus extends FileStatus {
  private FileData data;

  public ExfsFileStatus(Attr attr, Path p, String username, long blocksize) {
    super(attr.length, attr.type.equals(attr.TYPE_DIRECTORY),
        attr.DEFAULT_REPLICAS, blocksize,
        attr.modification_time(), attr.access_time(),
        new FsPermission(attr.mode),
        username, username, null, p, false, true, false);
  }

  public ExfsFileStatus(Attr attr, FileData d, Path p, String username, long blocksize) {
    super(attr.length, attr.type.equals(attr.TYPE_DIRECTORY),
        attr.DEFAULT_REPLICAS, blocksize,
        attr.modification_time(), attr.access_time(),
        new FsPermission(attr.mode),
        username, username, null, p, false, true, false);
    data = d;
  }

  public String getDataType() {
    return data.type;
  }

  public URI getDataPath() {
    return URI.create(data.data_path);
  }

  public String getDataETag() {
    return data.etag;
  }

  public long getDataLength() {
    return data.length;
  }

  public boolean isShared() {
    return data.shared;
  }
}
