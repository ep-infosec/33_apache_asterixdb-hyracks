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
package org.apache.hyracks.storage.am.lsm.btree.impls;

import java.util.List;

import org.apache.hyracks.api.exceptions.HyracksDataException;
import org.apache.hyracks.dataflow.common.data.accessors.ITupleReference;
import org.apache.hyracks.storage.am.btree.api.IBTreeLeafFrame;
import org.apache.hyracks.storage.am.btree.impls.BTree;
import org.apache.hyracks.storage.am.btree.impls.BTree.BTreeAccessor;
import org.apache.hyracks.storage.am.btree.impls.BTreeRangeSearchCursor;
import org.apache.hyracks.storage.am.btree.impls.RangePredicate;
import org.apache.hyracks.storage.am.common.api.ICursorInitialState;
import org.apache.hyracks.storage.am.common.api.ISearchPredicate;
import org.apache.hyracks.storage.am.common.api.ITreeIndexCursor;
import org.apache.hyracks.storage.am.common.api.IndexException;
import org.apache.hyracks.storage.am.common.impls.NoOpOperationCallback;
import org.apache.hyracks.storage.am.common.ophelpers.MultiComparator;
import org.apache.hyracks.storage.am.lsm.common.api.ILSMComponent;
import org.apache.hyracks.storage.am.lsm.common.api.ILSMComponent.LSMComponentType;
import org.apache.hyracks.storage.am.lsm.common.api.ILSMHarness;
import org.apache.hyracks.storage.am.lsm.common.api.ILSMIndexOperationContext;
import org.apache.hyracks.storage.am.lsm.common.impls.BloomFilterAwareBTreePointSearchCursor;
import org.apache.hyracks.storage.common.buffercache.IBufferCache;
import org.apache.hyracks.storage.common.buffercache.ICachedPage;

public abstract class LSMBTreeWithBuddyAbstractCursor implements ITreeIndexCursor {

    protected boolean open;
    protected BTreeRangeSearchCursor[] btreeCursors;
    protected BTreeRangeSearchCursor[] buddyBtreeCursors;
    protected BTreeAccessor[] btreeAccessors;
    protected BTreeAccessor[] buddyBtreeAccessors;
    protected MultiComparator btreeCmp;
    protected MultiComparator buddyBtreeCmp;
    protected int numberOfTrees;
    protected RangePredicate btreeRangePredicate;
    protected RangePredicate buddyBtreeRangePredicate;
    protected ITupleReference frameTuple;
    protected boolean includeMutableComponent;
    protected ILSMHarness lsmHarness;
    protected boolean foundNext;
    protected final ILSMIndexOperationContext opCtx;

    protected List<ILSMComponent> operationalComponents;

    public LSMBTreeWithBuddyAbstractCursor(ILSMIndexOperationContext opCtx) {
        super();
        this.opCtx = opCtx;
        buddyBtreeRangePredicate = new RangePredicate(null, null, true, true, null, null);
    }

    public ITreeIndexCursor getCursor(int cursorIndex) {
        return btreeCursors[cursorIndex];
    }

    @Override
    public void open(ICursorInitialState initialState, ISearchPredicate searchPred)
            throws IndexException, HyracksDataException {

        LSMBTreeWithBuddyCursorInitialState lsmInitialState = (LSMBTreeWithBuddyCursorInitialState) initialState;
        btreeCmp = lsmInitialState.getBTreeCmp();
        buddyBtreeCmp = lsmInitialState.getBuddyBTreeCmp();

        operationalComponents = lsmInitialState.getOperationalComponents();
        lsmHarness = lsmInitialState.getLSMHarness();
        numberOfTrees = operationalComponents.size();

        if (btreeCursors == null || btreeCursors.length != numberOfTrees) {
            // need to re-use the following four instead of re-creating
            btreeCursors = new BTreeRangeSearchCursor[numberOfTrees];
            buddyBtreeCursors = new BTreeRangeSearchCursor[numberOfTrees];
            btreeAccessors = new BTreeAccessor[numberOfTrees];
            buddyBtreeAccessors = new BTreeAccessor[numberOfTrees];
        }

        includeMutableComponent = false;

        for (int i = 0; i < numberOfTrees; i++) {
            ILSMComponent component = operationalComponents.get(i);
            BTree btree;
            BTree buddyBtree;
            if (component.getType() == LSMComponentType.MEMORY) {
                // This is not needed at the moment but is implemented anyway
                includeMutableComponent = true;
                // No need for a bloom filter for the in-memory BTree.
                if (buddyBtreeCursors[i] == null || buddyBtreeCursors[i].isBloomFilterAware()) {
                    buddyBtreeCursors[i] = new BTreeRangeSearchCursor(
                            (IBTreeLeafFrame) lsmInitialState.getBuddyBTreeLeafFrameFactory().createFrame(), false);
                } else {
                    buddyBtreeCursors[i].reset();
                }
                btree = ((LSMBTreeWithBuddyMemoryComponent) component).getBTree();
                buddyBtree = ((LSMBTreeWithBuddyMemoryComponent) component).getBuddyBTree();
            } else {
                if (buddyBtreeCursors[i] == null || !buddyBtreeCursors[i].isBloomFilterAware()) {
                    buddyBtreeCursors[i] = new BloomFilterAwareBTreePointSearchCursor(
                            (IBTreeLeafFrame) lsmInitialState.getBuddyBTreeLeafFrameFactory().createFrame(), false,
                            ((LSMBTreeWithBuddyDiskComponent) operationalComponents.get(i)).getBloomFilter());
                } else {
                    buddyBtreeCursors[i].reset();
                }
                btree = ((LSMBTreeWithBuddyDiskComponent) component).getBTree();
                buddyBtree = ((LSMBTreeWithBuddyDiskComponent) component).getBuddyBTree();
            }
            IBTreeLeafFrame leafFrame = (IBTreeLeafFrame) lsmInitialState.getBTreeLeafFrameFactory().createFrame();
            if (btreeAccessors[i] == null) {
                btreeCursors[i] = new BTreeRangeSearchCursor(leafFrame, false);
                btreeAccessors[i] = (BTreeAccessor) btree.createAccessor(NoOpOperationCallback.INSTANCE,
                        NoOpOperationCallback.INSTANCE);
                buddyBtreeAccessors[i] = (BTreeAccessor) buddyBtree.createAccessor(NoOpOperationCallback.INSTANCE,
                        NoOpOperationCallback.INSTANCE);
            } else {
                btreeCursors[i].reset();
                btreeAccessors[i].reset(btree, NoOpOperationCallback.INSTANCE, NoOpOperationCallback.INSTANCE);
                buddyBtreeAccessors[i].reset(buddyBtree, NoOpOperationCallback.INSTANCE,
                        NoOpOperationCallback.INSTANCE);
            }
        }
        btreeRangePredicate = (RangePredicate) searchPred;
        buddyBtreeRangePredicate.reset(null, null, true, true, buddyBtreeCmp, buddyBtreeCmp);
        open = true;
    }

    @Override
    public void close() throws HyracksDataException {
        if (!open) {
            return;
        }
        try {
            if (btreeCursors != null && buddyBtreeCursors != null) {
                for (int i = 0; i < numberOfTrees; i++) {
                    btreeCursors[i].close();
                    buddyBtreeCursors[i].close();
                }
            }
            btreeCursors = null;
            buddyBtreeCursors = null;
        } finally {
            lsmHarness.endSearch(opCtx);
        }
        foundNext = false;
        open = false;
    }

    @Override
    public ITupleReference getTuple() {
        return frameTuple;
    }

    @Override
    public ICachedPage getPage() {
        // Do nothing
        return null;
    }

    @Override
    public void setBufferCache(IBufferCache bufferCache) {
        // Do nothing
    }

    @Override
    public void setFileId(int fileId) {
        // Do nothing
    }

    @Override
    public boolean exclusiveLatchNodes() {
        return false;
    }

    @Override
    public void markCurrentTupleAsUpdated() throws HyracksDataException {
        throw new HyracksDataException("Updating tuples is not supported with this cursor.");
    }

}
