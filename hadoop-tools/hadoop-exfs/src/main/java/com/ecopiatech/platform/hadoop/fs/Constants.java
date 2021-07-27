/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ecopiatech.platform.hadoop.fs;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;

/**
 * All the constants used with the {@link ExfsFileSystem}.
 *
 * Some of the strings are marked as {@code Unstable}. This means
 * that they may be unsupported in future; at which point they will be marked
 * as deprecated and simply ignored.
 */
@InterfaceAudience.Public
@InterfaceStability.Evolving
public final class Constants {

  private Constants() {
  }

  public static final String LIBEXFS_SO_FILE = "/libexfs.so";
  public static final String REDIS_URL_CONF = "fs.exfs.redis.url";
  public static final String S3A_DEFAULT_URI = "s3a://ecopia-platform";
  public static final String S3_BUCKET_CONF = "fs.exfs.s3.bucket";
  public static final String S3_DEFAULT_BUCKET = "ecopia-platform";
}
