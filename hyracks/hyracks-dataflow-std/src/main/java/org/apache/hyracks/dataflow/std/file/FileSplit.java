/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.hyracks.dataflow.std.file;

import java.io.File;
import java.io.Serializable;

import org.apache.hyracks.api.io.FileReference;

public class FileSplit implements Serializable {
    private static final long serialVersionUID = 1L;
    private final FileReference file;
    private final int ioDeviceId;
    private final int partition;
    private final String nodeName;

    public FileSplit(String nodeName, FileReference file) {
        this.nodeName = nodeName;
        this.file = file;
        this.ioDeviceId = 0;
        this.partition = -1;
    }

    public FileSplit(String nodeName, FileReference file, int ioDeviceId, int partition) {
        this.nodeName = nodeName;
        this.file = file;
        this.ioDeviceId = ioDeviceId;
        this.partition = partition;
    }

    public FileSplit(String nodeName, String path, int ioDeviceId) {
        this.nodeName = nodeName;
        this.file = new FileReference(new File(path));
        this.ioDeviceId = ioDeviceId;
        this.partition = -1;
    }

    public FileSplit(String nodeName, String path) {
        this.nodeName = nodeName;
        this.file = new FileReference(new File(path));
        this.ioDeviceId = 0;
        this.partition = -1;
    }

    public String getNodeName() {
        return nodeName;
    }

    public FileReference getLocalFile() {
        return file;
    }

    public int getIODeviceId() {
        return ioDeviceId;
    }

    public int getPartition() {
        return partition;
    }

    @Override
    public String toString() {
        return file.toString();
    }
}
