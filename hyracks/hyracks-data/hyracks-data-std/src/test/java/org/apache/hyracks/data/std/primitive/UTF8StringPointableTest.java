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

package org.apache.hyracks.data.std.primitive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.hyracks.data.std.util.GrowableArray;
import org.apache.hyracks.data.std.util.UTF8StringBuilder;
import org.apache.hyracks.util.string.UTF8StringSample;
import org.apache.hyracks.util.string.UTF8StringUtil;
import org.junit.Test;

public class UTF8StringPointableTest {
    public static UTF8StringPointable STRING_EMPTY = UTF8StringPointable
            .generateUTF8Pointable(UTF8StringSample.EMPTY_STRING);
    public static UTF8StringPointable STRING_UTF8_MIX = UTF8StringPointable
            .generateUTF8Pointable(UTF8StringSample.STRING_UTF8_MIX);
    public static UTF8StringPointable STRING_UTF8_MIX_LOWERCASE = UTF8StringPointable.generateUTF8Pointable(
            UTF8StringSample.STRING_UTF8_MIX_LOWERCASE);

    public static UTF8StringPointable STRING_LEN_127 = UTF8StringPointable
            .generateUTF8Pointable(UTF8StringSample.STRING_LEN_127);
    public static UTF8StringPointable STRING_LEN_128 = UTF8StringPointable
            .generateUTF8Pointable(UTF8StringSample.STRING_LEN_128);

    @Test
    public void testGetStringLength() throws Exception {
        UTF8StringPointable utf8Ptr = UTF8StringPointable.generateUTF8Pointable(UTF8StringSample.STRING_LEN_127);
        assertEquals(127, utf8Ptr.getUTF8Length());
        assertEquals(1, utf8Ptr.getMetaDataLength());
        assertEquals(127, utf8Ptr.getStringLength());

        byte[] bytes = UTF8StringUtil.writeStringToBytes(UTF8StringSample.STRING_LEN_128);
        utf8Ptr.set(bytes, 0, bytes.length);
        assertEquals(128, utf8Ptr.getUTF8Length());
        assertEquals(2, utf8Ptr.getMetaDataLength());
        assertEquals(128, utf8Ptr.getStringLength());
    }

    @Test
    public void testContains() throws Exception {
        assertTrue(STRING_UTF8_MIX.contains(STRING_UTF8_MIX, false));
        assertTrue(STRING_UTF8_MIX.contains(STRING_UTF8_MIX, true));
        assertTrue(STRING_UTF8_MIX.contains(STRING_EMPTY, true));

        assertTrue(STRING_UTF8_MIX.contains(STRING_UTF8_MIX_LOWERCASE, true));
        assertTrue(STRING_UTF8_MIX_LOWERCASE.contains(STRING_UTF8_MIX, true));
    }

    @Test
    public void testStartsWith() throws Exception {
        assertTrue(STRING_LEN_128.startsWith(STRING_LEN_127, true));
        assertFalse(STRING_LEN_127.startsWith(STRING_LEN_128, true));

        assertTrue(STRING_LEN_127.startsWith(STRING_EMPTY, true));
    }

    @Test
    public void testEndsWith() throws Exception {
        assertTrue(STRING_LEN_128.endsWith(STRING_LEN_127, true));
        assertFalse(STRING_LEN_127.endsWith(STRING_LEN_128, true));

        assertTrue(STRING_LEN_127.startsWith(STRING_EMPTY, true));
    }

    @Test
    public void testConcat() throws Exception {
        UTF8StringPointable expected = UTF8StringPointable.generateUTF8Pointable(
                UTF8StringSample.generateStringRepeatBy(UTF8StringSample.ONE_ASCII_CHAR, 127 + 128));

        GrowableArray storage = new GrowableArray();
        UTF8StringBuilder builder = new UTF8StringBuilder();
        STRING_LEN_127.concat(STRING_LEN_128, builder, storage);

        UTF8StringPointable actual = new UTF8StringPointable();
        actual.set(storage.getByteArray(), 0, storage.getLength());

        assertEquals(0, expected.compareTo(actual));

        storage.reset();
        STRING_LEN_127.concat(STRING_EMPTY, builder, storage);
        actual.set(storage.getByteArray(), 0, storage.getLength());

        assertEquals(0, STRING_LEN_127.compareTo(actual));
    }

    @Test
    public void testSubstr() throws Exception {
        GrowableArray storage = new GrowableArray();
        UTF8StringBuilder builder = new UTF8StringBuilder();

        STRING_LEN_128.substr(1, 127, builder, storage);
        UTF8StringPointable result = new UTF8StringPointable();
        result.set(storage.getByteArray(), 0, storage.getLength());

        assertEquals(0, STRING_LEN_127.compareTo(result));

        storage.reset();
        STRING_UTF8_MIX.substr(0, UTF8StringSample.STRING_UTF8_MIX.length(), builder, storage);
        result.set(storage.getByteArray(), 0, storage.getLength());
        assertEquals(0, STRING_UTF8_MIX.compareTo(result));
    }

    @Test
    public void testSubstrBefore() throws Exception {
        UTF8StringBuilder builder = new UTF8StringBuilder();
        GrowableArray storage = new GrowableArray();

        STRING_LEN_128.substrBefore(STRING_LEN_127, builder, storage);
        UTF8StringPointable result = new UTF8StringPointable();
        result.set(storage.getByteArray(), 0, storage.getLength());

        assertEquals(0, STRING_EMPTY.compareTo(result));

        storage.reset();
        UTF8StringPointable testPtr = UTF8StringPointable.generateUTF8Pointable("Mix??????123");
        UTF8StringPointable pattern = UTF8StringPointable.generateUTF8Pointable("???");
        UTF8StringPointable expect = UTF8StringPointable.generateUTF8Pointable("Mix???");
        testPtr.substrBefore(pattern, builder, storage);
        result.set(storage.getByteArray(), 0, storage.getLength());
        assertEquals(0, expect.compareTo(result));
    }

    @Test
    public void testSubstrAfter() throws Exception {
        UTF8StringBuilder builder = new UTF8StringBuilder();
        GrowableArray storage = new GrowableArray();

        STRING_LEN_128.substrAfter(STRING_LEN_127, builder, storage);
        UTF8StringPointable result = new UTF8StringPointable();
        result.set(storage.getByteArray(), 0, storage.getLength());

        UTF8StringPointable expect = UTF8StringPointable
                .generateUTF8Pointable(Character.toString(UTF8StringSample.ONE_ASCII_CHAR));
        assertEquals(0, expect.compareTo(result));

        storage.reset();
        UTF8StringPointable testPtr = UTF8StringPointable.generateUTF8Pointable("Mix??????123");
        UTF8StringPointable pattern = UTF8StringPointable.generateUTF8Pointable("???");
        expect = UTF8StringPointable.generateUTF8Pointable("123");
        testPtr.substrAfter(pattern, builder, storage);
        result.set(storage.getByteArray(), 0, storage.getLength());
        assertEquals(0, expect.compareTo(result));
    }

    @Test
    public void testLowercase() throws Exception {
        UTF8StringBuilder builder = new UTF8StringBuilder();
        GrowableArray storage = new GrowableArray();

        UTF8StringPointable result = new UTF8StringPointable();
        STRING_UTF8_MIX.lowercase(builder, storage);

        result.set(storage.getByteArray(), 0, storage.getLength());

        assertEquals(0, STRING_UTF8_MIX_LOWERCASE.compareTo(result));
    }

    @Test
    public void testUppercase() throws Exception {
        UTF8StringBuilder builder = new UTF8StringBuilder();
        GrowableArray storage = new GrowableArray();

        UTF8StringPointable result = new UTF8StringPointable();
        STRING_UTF8_MIX_LOWERCASE.uppercase(builder, storage);

        result.set(storage.getByteArray(), 0, storage.getLength());

        UTF8StringPointable expected = UTF8StringPointable
                .generateUTF8Pointable(UTF8StringSample.STRING_UTF8_MIX_LOWERCASE.toUpperCase());
        assertEquals(0, expected.compareTo(result));

    }
}
