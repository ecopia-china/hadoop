package com.ecopiatech.platform.hadoop.fs;

public class FileData {
  public static String DATATYPE_OBJECT = "object";
  public static String DATATYPE_PROJECT = "project";
  public static String DATATYPE_LAYER = "layer";
  public static String DATATYPE_RASTER = "raster";
  public static String DATATYPE_DRASTER = "draster";
  public static String DATATYPE_DDLINK = "ddlink";

  public String type;
  public String data_path;
  public String etag;
  public long length;
  public boolean shared;

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