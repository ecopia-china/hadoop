package com.ecopiatech.platform.hadoop.fs;

public class FileData {
  private String type;
  private String data_path;
  private String etag;
  private long length;
  private boolean shared;

  @Override
  public String toString() {
    return "FileData{" +
        "type='" + type + '\'' +
        ", data_path='" + data_path + '\'' +
        ", etag='" + etag + '\'' +
        ", length=" + length +
        ", shared=" + shared +
        '}';
  }
}