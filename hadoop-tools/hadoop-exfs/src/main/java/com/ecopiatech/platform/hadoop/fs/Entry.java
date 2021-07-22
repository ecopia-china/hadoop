package com.ecopiatech.platform.hadoop.fs;

public class Entry {
  public String name;
  public Attr attr;

  @Override
  public String toString() {
    return "Entry{" +
        "name='" + name + '\'' +
        ", attr=" + attr +
        '}';
  }
}
