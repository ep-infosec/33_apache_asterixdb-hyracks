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

package org.apache.hyracks.storage.am.common.ophelpers;

public class LongArrayList {
	private long[] data;
	private int size;
	private int first;
	private final int growth;

	public LongArrayList(int initialCapacity, int growth) {
		data = new long[initialCapacity];
		size = 0;
		first = 0;
		this.growth = growth;
	}

	public int size() {
		return size;
	}

	public int first() {
		return first;
	}

	public void addFirst(long i) {
	    long[] newData = new long[data.length + 1];
        System.arraycopy(data, 0, newData, 0, first);
        System.arraycopy(data, first, newData, first + 1, size - first);
        data = newData;
        data[first] = i;
        size++;
    }


	public void add(long i) {
		if (size == data.length) {
			long[] newData = new long[data.length + growth];
			System.arraycopy(data, 0, newData, 0, data.length);
			data = newData;
		}

		data[size++] = i;
	}

	public void removeLast() {
		if (size > 0)
			size--;
	}

	// WARNING: caller is responsible for checking size > 0
	public long getLast() {
		return data[size - 1];
	}

	public long get(int i) {
		return data[i];
	}

	// WARNING: caller is responsible for checking i < size
	public void set(int i, long value) {
		data[i] = value;

	}

	public long getFirst() {
		return data[first];
	}

	public void moveFirst() {
		first++;
	}

	public void clear() {
		size = 0;
		first = 0;
	}

	public boolean isLast() {
		return size == first;
	}

	public boolean isEmpty() {
		return size == 0;
	}
}
