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
package org.apache.hyracks.storage.common.buffercache;

import java.nio.ByteBuffer;

public interface ICachedPage {
    public ByteBuffer getBuffer();

    public void acquireReadLatch();

    public void releaseReadLatch();

    public void acquireWriteLatch();

    public void releaseWriteLatch(boolean markDirty);

    public boolean confiscated();

    public IQueueInfo getQueueInfo();

    public void setQueueInfo(IQueueInfo queueInfo);
}
