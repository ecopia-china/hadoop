package com.ecopiatech.platform.hadoop.fs;

import org.apache.hadoop.fs.permission.FsPermission;

import java.io.IOException;

public class ExfsFileCreator {
  private ExfsFileSystem.LibExfs libexfs;
  private String path;
  private String s3path;

  public ExfsFileCreator(ExfsFileSystem.LibExfs libexfs,
                         String path, String s3path) {
    this.libexfs = libexfs;
    this.path = path;
    this.s3path = s3path;
  }

  public void create(long length) throws IOException {
    int ret = libexfs.CreateObjFile(path, s3path,
       "", length, true,
        FsPermission.getFileDefault().toShort());
    if (ret != 0) {
      throw new IOException(Errno.toString(ret));
    }
  }
}
