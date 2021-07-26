package com.ecopiatech.platform.hadoop.fs;

import java.io.IOException;

public class ExfsFileCreator {
  private ExfsFileSystem.LibExfs libexfs;
  private String path;
  private String s3path;
  private int mode;

  public ExfsFileCreator(ExfsFileSystem.LibExfs libexfs,
                         String path, String s3path, int mode) {
    this.libexfs = libexfs;
    this.path = path;
    this.s3path = s3path;
    this.mode = mode;
  }

  public void create(long length) throws IOException {
    int ret = libexfs.CreateObjFile(path, s3path, "", length, true, mode);
    if (ret != 0) {
      throw new IOException(Errno.toString(ret));
    }
  }
}
