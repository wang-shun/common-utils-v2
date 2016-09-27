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

package com.youzan.sz.jutil.bloomfilter;

import com.youzan.platform.util.lang.StringUtil;
import com.youzan.sz.jutil.bytes.BitBuffer;
import com.youzan.sz.jutil.bytes.ByteUtil;
import org.apache.commons.codec.binary.Base64;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

//import com.qq.jutil.bytes.BitBuffer;
//import com.qq.jutil.bytes.ByteUtil;

@SuppressWarnings("serial")
public class BloomFilter implements Serializable {

    private static final int  EXCESS = 20;
    private static MurmurHash hasher = new MurmurHash();

    private BitBuffer         filter;
    private int               hashCount;

    public BloomFilter(int hashes, BitBuffer filter) {
        hashCount = hashes;
        this.filter = filter;
    }

    public int getHashCount() {
        return hashCount;
    }

    private int[] getHashBuckets(int key) {
        byte[] bsKey = ByteUtil.enbyteInt(key);
        return getHashBuckets(bsKey, hashCount, buckets());
    }

    private int[] getHashBuckets(String key) {
        byte[] b;
        try {
            b = key.getBytes("UTF-16");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return getHashBuckets(b, hashCount, buckets());
    }

    private int[] getHashBuckets(byte[] key) {
        return getHashBuckets(key, hashCount, buckets());
    }

    private static BitBuffer bucketsFor(long numElements, int bucketsPer) {
        long numBits = numElements * bucketsPer + EXCESS;
        return new BitBuffer((int) Math.min(Integer.MAX_VALUE, numBits));
    }

    public int buckets() {
        return filter.getCapacity();
    }

    public boolean isPresent(int key) {
        for (int bucketIndex : getHashBuckets(key)) {
            if (!filter.getBit(bucketIndex)) {
                return false;
            }
        }
        return true;
    }

    public boolean isPresent(String key) {
        for (int bucketIndex : getHashBuckets(key)) {
            if (!filter.getBit(bucketIndex)) {
                return false;
            }
        }
        return true;
    }

    public boolean isPresent(byte[] key) {
        for (int bucketIndex : getHashBuckets(key)) {
            if (!filter.getBit(bucketIndex)) {
                return false;
            }
        }
        return true;
    }

    public void add(int key) {
        for (int bucketIndex : getHashBuckets(key)) {
            filter.putBit(bucketIndex, true);
        }
    }

    public void add(String key) {
        for (int bucketIndex : getHashBuckets(key)) {
            filter.putBit(bucketIndex, true);
        }
    }

    public void add(byte[] key) {
        for (int bucketIndex : getHashBuckets(key)) {
            filter.putBit(bucketIndex, true);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[BloomFilter:");
        sb.append(hashCount + ",");
        sb.append(filter.getCapacity());
        sb.append("]");
        return sb.toString();
    }

    public BitBuffer getBitBuffer() {
        return filter;
    }

    public int emptyBuckets() {
        int n = 0;
        for (int i = 0; i < buckets(); i++) {
            if (!filter.getBit(i)) {
                n++;
            }
        }
        return n;
    }

    /**
     * Calculates the maximum number of buckets per element that this implementation
     * can support.  Crucially, it will lower the bucket count if necessary to meet
     * BitSet's size restrictions.
     */
    private static int maxBucketsPerElement(long numElements) {
        numElements = Math.max(1, numElements);
        double v = (Integer.MAX_VALUE - EXCESS) / (double) numElements;
        if (v < 1.0) {
            throw new UnsupportedOperationException("Cannot compute probabilities for " + numElements + " elements.");
        }
        return Math.min(BloomCalculations.probs.length - 1, (int) v);
    }

    /**
     * @return A BloomFilter with the lowest practical false positive probability
     * for the given number of elements.
     */
    public static BloomFilter getFilter(long numElements, int targetBucketsPerElem) {
        int maxBucketsPerElement = Math.max(1, maxBucketsPerElement(numElements));
        int bucketsPerElement = Math.min(targetBucketsPerElem, maxBucketsPerElement);
        if (bucketsPerElement < targetBucketsPerElem) {
            System.err.println(
                String.format("Cannot provide an optimal BloomFilter for %d elements (%d/%d buckets per element).",
                    numElements, bucketsPerElement, targetBucketsPerElem));
        }
        BloomCalculations.BloomSpecification spec = BloomCalculations.computeBloomSpec(bucketsPerElement);
        return new BloomFilter(spec.K, bucketsFor(numElements, spec.bucketsPerElement));
    }

    /**
     * @return The smallest BloomFilter that can provide the given false positive
     * probability rate for the given number of elements.
     *
     * Asserts that the given probability can be satisfied using this filter.
     */
    public static BloomFilter getFilter(long numElements, double maxFalsePosProbability) {
        assert maxFalsePosProbability <= 1.0 : "Invalid probability";
        int bucketsPerElement = maxBucketsPerElement(numElements);
        BloomCalculations.BloomSpecification spec = BloomCalculations.computeBloomSpec(bucketsPerElement,
            maxFalsePosProbability);
        return new BloomFilter(spec.K, bucketsFor(numElements, spec.bucketsPerElement));
    }
    //
    //    public static BloomFilter getFilter(byte[] b, long numElements, double maxFalsePosProbability) {
    //        final BitBuffer bitBuffer = new BitBuffer(b);
    //        final BloomFilter filter = getFilter(numElements, maxFalsePosProbability);
    //        filter.add(b);
    //
    //    }

    private static int[] getHashBuckets(byte[] b, int hashCount, int max) {
        int[] result = new int[hashCount];
        int hash1 = hasher.hash(b, b.length, 0);
        int hash2 = hasher.hash(b, b.length, hash1);
        for (int i = 0; i < hashCount; i++) {
            result[i] = Math.abs((hash1 + i * hash2) % max);
        }
        return result;
    }

    private static void testMarshal() throws UnsupportedEncodingException {
        int numElements = 1_000_000;
        BloomFilter filter = getFilter(numElements, 0.01);
        filter.add(12345);
        filter.add(123456);
        filter.add(123457);
        filter.add(123458);
        System.out.println(filter.getBitBuffer().toString());
        System.out.println("newFilter0:" + filter.isPresent(123456));
        System.out.println("newFilter0:" + filter.isPresent(12456));

        BloomFilter newFilter1 = new BloomFilter(filter.getHashCount(), filter.getBitBuffer());
        System.out.println("newFilter1:" + newFilter1.isPresent(123456));
        System.out.println("newFilter1:" + newFilter1.isPresent(12456));

        final byte[] bytes = filter.getBitBuffer().toArray();
        final BitBuffer bitBuffer = new BitBuffer(bytes, filter.getBitBuffer().getCapacity());
        BloomFilter newFilter2 = new BloomFilter(filter.getHashCount(), bitBuffer);
        System.out.println("newFilter2:" + newFilter2.isPresent(123456));
        System.out.println("newFilter2:" + newFilter2.isPresent(12456));

        //        final byte[] bytes = filter.getBitBuffer().toArray();

        System.out.println(new String(bytes).contentEquals(new String(new String(bytes).getBytes())));
        System.out.println(Arrays.equals(bytes, new String(bytes).getBytes()));
        System.out.println(Arrays.equals(bytes, ByteUtil.enbyteString(ByteUtil.debyteString(bytes))));

        System.out
            .println("equal:" + Arrays.equals(bytes, Base64.decodeBase64(new String(Base64.encodeBase64(bytes)))));

        for (byte aByte : bytes) {
            if (aByte != 0)
                System.out.print(aByte);
        }

        System.out.println();
        for (byte aByte : new String(bytes).getBytes()) {
            if (aByte != 0)
                System.out.print(aByte);
        }
        System.out.println();
        for (byte aByte : ByteUtil.enbyteString(ByteUtil.debyteString(bytes))) {
            if (aByte != 0)
                System.out.print(aByte);
        }
        System.out.println();

        final byte[] byteStr = new String(bytes).getBytes();
        final BitBuffer bitBufferStr = new BitBuffer(byteStr, filter.getBitBuffer().getCapacity());
        BloomFilter newFilter3 = new BloomFilter(filter.getHashCount(), bitBufferStr);
        System.out.println("newFilter3:" + newFilter3.isPresent(123456));
        System.out.println("newFilter3:" + newFilter3.isPresent(12456));

    }

    public static void main(String[] avg) throws UnsupportedEncodingException {
        //numElements:黑名单数量
        testMarshal();
        int numElements = 1_000_000;
        BloomFilter filter = getFilter(numElements, 0.01);
        //        final int maxBucketsPerElement = filter.maxBucketsPerElement(1_000_000_000);
        //        final BloomCalculations.BloomSpecification bloomSpecification = BloomCalculations
        //            .computeBloomSpec(maxBucketsPerElement, 0.1);
        //        System.out.println(bloomSpecification);
        //        System.out.println("maxBucketsPerElement:" + maxBucketsPerElement);
        //        for (int i = 0; i < 100; i++) {
        //            filter.add(i);
        //        }
        //        int n = 0;
        //        for (int i = 100; i < 200; i++) {
        //            if (filter.isPresent(i)) {
        //                n++;
        //            }
        //        }
        //        System.out.println(n);
        //        System.out.println("hashCount:" + filter.getHashCount());
        //        System.out.println("buckets:" + filter.buckets() + "|bytes:" + filter.buckets() / 8 / 1024 / 1024);
        //        Set<Integer> black = new HashSet<Integer>();
        //        Random random = new Random();
        //        for (int i = 0; i < numElements; i++) {
        //            int uin = Math.abs(random.nextInt(numElements * 10));
        //            black.add(uin);
        //            filter.add(uin);
        //        }
        //        AtomicInteger req = new AtomicInteger();
        //        AtomicInteger miss = new AtomicInteger();
        //        AtomicInteger error = new AtomicInteger();
        //        AtomicInteger succ = new AtomicInteger();
        //        for (int i = 0; i < numElements * 10; i++) {
        //            req.getAndIncrement();
        //            int uin = Math.abs(random.nextInt(numElements * 10));
        //            boolean bFilter = filter.isPresent(uin);
        //            boolean bBlack = black.contains(uin);
        //            //误杀
        //            if (bFilter && !bBlack)
        //                miss.getAndIncrement();
        //            if (!bFilter && bBlack)
        //                error.getAndIncrement();
        //            if (bFilter && bBlack)
        //                succ.getAndIncrement();
        //            if (!bFilter && !bBlack)
        //                succ.getAndIncrement();
        //        }
        //        double dMiss = miss.get();
        //        double dReq = req.get();
        //        System.out.println("miss:" + miss.get());
        //        System.out.println("succ:" + succ.get());
        //        System.out.println("error:" + error.get());
        //        System.out.println("req:" + req.get());
        //        System.out.println("blackSize:" + black.size());
        //        System.out.println(dMiss / dReq);
    }
}
