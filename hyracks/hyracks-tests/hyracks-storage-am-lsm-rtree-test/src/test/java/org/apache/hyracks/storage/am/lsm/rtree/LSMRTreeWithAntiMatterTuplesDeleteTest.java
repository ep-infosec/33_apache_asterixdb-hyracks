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

package org.apache.hyracks.storage.am.lsm.rtree;

import java.util.Random;

import org.junit.After;
import org.junit.Before;

import org.apache.hyracks.api.dataflow.value.ISerializerDeserializer;
import org.apache.hyracks.api.exceptions.HyracksDataException;
import org.apache.hyracks.api.exceptions.HyracksException;
import org.apache.hyracks.storage.am.common.api.IPrimitiveValueProviderFactory;
import org.apache.hyracks.storage.am.config.AccessMethodTestsConfig;
import org.apache.hyracks.storage.am.lsm.rtree.util.LSMRTreeTestHarness;
import org.apache.hyracks.storage.am.lsm.rtree.util.LSMRTreeWithAntiMatterTuplesTestContext;
import org.apache.hyracks.storage.am.rtree.AbstractRTreeDeleteTest;
import org.apache.hyracks.storage.am.rtree.AbstractRTreeTestContext;
import org.apache.hyracks.storage.am.rtree.frames.RTreePolicyType;

@SuppressWarnings("rawtypes")
public class LSMRTreeWithAntiMatterTuplesDeleteTest extends AbstractRTreeDeleteTest {

    private final LSMRTreeTestHarness harness = new LSMRTreeTestHarness();

    public LSMRTreeWithAntiMatterTuplesDeleteTest() {
        super(AccessMethodTestsConfig.LSM_RTREE_TEST_RSTAR_POLICY);
    }

    @Before
    public void setUp() throws HyracksException {
        harness.setUp();
    }

    @After
    public void tearDown() throws HyracksDataException {
        harness.tearDown();
    }

    @Override
    protected AbstractRTreeTestContext createTestContext(ISerializerDeserializer[] fieldSerdes,
            IPrimitiveValueProviderFactory[] valueProviderFactories, int numKeys, RTreePolicyType rtreePolicyType)
            throws Exception {
        return LSMRTreeWithAntiMatterTuplesTestContext.create(harness.getVirtualBufferCaches(),
                harness.getFileReference(), harness.getDiskBufferCache(), harness.getDiskFileMapProvider(),
                fieldSerdes, valueProviderFactories, numKeys, rtreePolicyType, harness.getMergePolicy(),
                harness.getOperationTracker(), harness.getIOScheduler(), harness.getIOOperationCallback());
    }

    @Override
    protected Random getRandom() {
        return harness.getRandom();
    }
}
