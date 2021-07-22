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

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;

import org.apache.hadoop.conf.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import java.net.URI;

/**
 * Abstract base class for S3A unit tests using a mock S3 client and a null
 * metadata store.
 */
public abstract class AbstractExfsMockTest {

  protected static final String BUCKET = "mock-bucket";
  protected static final AmazonServiceException NOT_FOUND;
  static {
    NOT_FOUND = new AmazonServiceException("Not Found");
    NOT_FOUND.setStatusCode(404);
  }

  @Rule
  public ExpectedException exception = ExpectedException.none();

  protected ExfsFileSystem fs;
  protected AmazonS3 s3;

  @Before
  public void setup() throws Exception {
    fs = new ExfsFileSystemImpl();
    Configuration conf = new Configuration();
    conf.set("fs.exfs.redis.url", "redis://192.168.33.1");
    URI uri = URI.create("exfs://655541751814");
    fs.initialize(uri, conf);
  }

  @After
  public void teardown() throws Exception {
    if (fs != null) {
      fs.close();
    }
  }
}
