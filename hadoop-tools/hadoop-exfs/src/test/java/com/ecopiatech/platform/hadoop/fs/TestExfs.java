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

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * S3A tests for getFileStatus using mock S3 client.
 */
public class TestExfs extends AbstractExfsMockTest {

  // @Test
  // public void testFileStat() throws Exception {
  //   Path p = new Path("exfs://655541751814/data/file1");
  //   FileStatus fileStatus = fs.getFileStatus(p);
  //   assertTrue(fileStatus.isFile());
  // }

  // @Test
  // public void testListStatus() throws Exception {
  //   Path dir = new Path("exfs://655541751814/data/general_address.csv");
  //   FileStatus[] status = fs.listStatus(dir);
  //   assertEquals(1, status.length);
  // }
}
