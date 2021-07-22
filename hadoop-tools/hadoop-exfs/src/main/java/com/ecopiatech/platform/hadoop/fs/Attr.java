package com.ecopiatech.platform.hadoop.fs;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;

public class Attr {
  public static String TYPE_DIRECTORY = "directory";
  public static String TYPE_FILE = "regular";
  public static int DEFAULT_REPLICAS = 1;

  public int inode;
  public int flags;
  public String type;
  public int mode;
  public int uid;
  public int gid;
  public long atime;
  public long mtime;
  public long ctime;
  public int atimensec;
  public int mtimensec;
  public int ctimensec;
  public int nlink;
  public long length;
  public int rdev;
  public long parent;
  public boolean full;
  public long ecopiauid;

  public FileStatus toFileStatus(Path p, String username) {
    return new FileStatus(length, type.equals(TYPE_DIRECTORY),
        DEFAULT_REPLICAS, 0,
        mtime * 1000 + mtimensec/1000000,
        atime * 1000 + atimensec / 1000000,
        new FsPermission(mode),
        username, username, p);
  }

  @Override
  public String toString() {
    return "Attr{" +
        "inode=" + inode +
        ", flags=" + flags +
        ", type='" + type + '\'' +
        ", mode=" + mode +
        ", uid=" + uid +
        ", gid=" + gid +
        ", atime=" + atime +
        ", mtime=" + mtime +
        ", ctime=" + ctime +
        ", atimensec=" + atimensec +
        ", mtimensec=" + mtimensec +
        ", ctimensec=" + ctimensec +
        ", nlink=" + nlink +
        ", length=" + length +
        ", rdev=" + rdev +
        ", parent=" + parent +
        ", full=" + full +
        ", ecopiauid=" + ecopiauid +
        '}';
  }
}
