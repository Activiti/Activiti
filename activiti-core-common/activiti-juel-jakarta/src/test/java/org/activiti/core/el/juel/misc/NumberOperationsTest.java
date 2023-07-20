/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.core.el.juel.misc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.activiti.core.el.juel.test.TestCase;
import org.junit.jupiter.api.Test;

public class NumberOperationsTest extends TestCase {

    private TypeConverter converter = TypeConverter.DEFAULT;

    /*
     * Test method for 'org.activiti.core.el.juel.NumberOperations.add(Object, Object)'
     */
    @Test
    public void testAdd() {
        assertEquals(Long.valueOf(0), NumberOperations.add(converter, null, null));

        BigDecimal bd1 = new BigDecimal(1);
        Integer i1 = Integer.valueOf(1);
        Long l1 = Long.valueOf(1);
        Float f1 = Float.valueOf(1);
        Double d1 = Double.valueOf(1);
        String e1 = "1e0";
        String s1 = "1";
        BigInteger bi1 = new BigInteger("1");

        Long l2 = Long.valueOf(2);
        BigDecimal bd2 = new BigDecimal(2);
        Double d2 = Double.valueOf(2);
        BigInteger bi2 = new BigInteger("2");

        assertEquals(bd2, NumberOperations.add(converter, l1, bd1));
        assertEquals(bd2, NumberOperations.add(converter, bd1, l1));

        assertEquals(bd2, NumberOperations.add(converter, f1, bi1));
        assertEquals(bd2, NumberOperations.add(converter, bi1, f1));

        assertEquals(d2, NumberOperations.add(converter, f1, l1));
        assertEquals(d2, NumberOperations.add(converter, l1, f1));

        assertEquals(bd2, NumberOperations.add(converter, d1, bi1));
        assertEquals(bd2, NumberOperations.add(converter, bi1, d1));

        assertEquals(d2, NumberOperations.add(converter, d1, l1));
        assertEquals(d2, NumberOperations.add(converter, l1, d1));

        assertEquals(bd2, NumberOperations.add(converter, e1, bi1));
        assertEquals(bd2, NumberOperations.add(converter, bi1, e1));

        assertEquals(d2, NumberOperations.add(converter, e1, l1));
        assertEquals(d2, NumberOperations.add(converter, l1, e1));

        assertEquals(bi2, NumberOperations.add(converter, l1, bi1));
        assertEquals(bi2, NumberOperations.add(converter, bi1, l1));

        assertEquals(l2, NumberOperations.add(converter, i1, l1));
        assertEquals(l2, NumberOperations.add(converter, l1, i1));

        assertEquals(l2, NumberOperations.add(converter, i1, s1));
        assertEquals(l2, NumberOperations.add(converter, s1, i1));
    }

    /*
     * Test method for 'org.activiti.core.el.juel.NumberOperations.sub(Object, Object)'
     */
    @Test
    public void testSub() {
        assertEquals(Long.valueOf(0), NumberOperations.sub(converter, null, null));

        BigDecimal bd1 = new BigDecimal(1);
        Integer i1 = Integer.valueOf(1);
        Long l1 = Long.valueOf(1);
        Float f1 = Float.valueOf(1);
        Double d1 = Double.valueOf(1);
        String e1 = "1e0";
        String s1 = "1";
        BigInteger bi1 = new BigInteger("1");

        Long l2 = Long.valueOf(0);
        BigDecimal bd2 = new BigDecimal(0);
        Double d2 = Double.valueOf(0);
        BigInteger bi2 = new BigInteger("0");

        assertEquals(bd2, NumberOperations.sub(converter, l1, bd1));
        assertEquals(bd2, NumberOperations.sub(converter, bd1, l1));

        assertEquals(bd2, NumberOperations.sub(converter, f1, bi1));
        assertEquals(bd2, NumberOperations.sub(converter, bi1, f1));

        assertEquals(d2, NumberOperations.sub(converter, f1, l1));
        assertEquals(d2, NumberOperations.sub(converter, l1, f1));

        assertEquals(bd2, NumberOperations.sub(converter, d1, bi1));
        assertEquals(bd2, NumberOperations.sub(converter, bi1, d1));

        assertEquals(d2, NumberOperations.sub(converter, d1, l1));
        assertEquals(d2, NumberOperations.sub(converter, l1, d1));

        assertEquals(bd2, NumberOperations.sub(converter, e1, bi1));
        assertEquals(bd2, NumberOperations.sub(converter, bi1, e1));

        assertEquals(d2, NumberOperations.sub(converter, e1, l1));
        assertEquals(d2, NumberOperations.sub(converter, l1, e1));

        assertEquals(bi2, NumberOperations.sub(converter, l1, bi1));
        assertEquals(bi2, NumberOperations.sub(converter, bi1, l1));

        assertEquals(l2, NumberOperations.sub(converter, i1, l1));
        assertEquals(l2, NumberOperations.sub(converter, l1, i1));

        assertEquals(l2, NumberOperations.sub(converter, i1, s1));
        assertEquals(l2, NumberOperations.sub(converter, s1, i1));
    }

    /*
     * Test method for 'org.activiti.core.el.juel.NumberOperations.mul(Object, Object)'
     */
    @Test
    public void testMul() {
        assertEquals(Long.valueOf(0), NumberOperations.mul(converter, null, null));

        BigDecimal bd1 = new BigDecimal(1);
        Integer i1 = Integer.valueOf(1);
        Long l1 = Long.valueOf(1);
        Float f1 = Float.valueOf(1);
        Double d1 =  Double.valueOf(1);
        String e1 = "1e0";
        String s1 = "1";
        BigInteger bi1 = new BigInteger("1");

        Long l2 = Long.valueOf(1);
        BigDecimal bd2 = new BigDecimal(1);
        Double d2 = Double.valueOf(1);
        BigInteger bi2 = new BigInteger("1");

        assertEquals(bd2, NumberOperations.mul(converter, l1, bd1));
        assertEquals(bd2, NumberOperations.mul(converter, bd1, l1));

        assertEquals(bd2, NumberOperations.mul(converter, f1, bi1));
        assertEquals(bd2, NumberOperations.mul(converter, bi1, f1));

        assertEquals(d2, NumberOperations.mul(converter, f1, l1));
        assertEquals(d2, NumberOperations.mul(converter, l1, f1));

        assertEquals(bd2, NumberOperations.mul(converter, d1, bi1));
        assertEquals(bd2, NumberOperations.mul(converter, bi1, d1));

        assertEquals(d2, NumberOperations.mul(converter, d1, l1));
        assertEquals(d2, NumberOperations.mul(converter, l1, d1));

        assertEquals(bd2, NumberOperations.mul(converter, e1, bi1));
        assertEquals(bd2, NumberOperations.mul(converter, bi1, e1));

        assertEquals(d2, NumberOperations.mul(converter, e1, l1));
        assertEquals(d2, NumberOperations.mul(converter, l1, e1));

        assertEquals(bi2, NumberOperations.mul(converter, l1, bi1));
        assertEquals(bi2, NumberOperations.mul(converter, bi1, l1));

        assertEquals(l2, NumberOperations.mul(converter, i1, l1));
        assertEquals(l2, NumberOperations.mul(converter, l1, i1));

        assertEquals(l2, NumberOperations.mul(converter, i1, s1));
        assertEquals(l2, NumberOperations.mul(converter, s1, i1));
    }

    /*
     * Test method for 'org.activiti.core.el.juel.NumberOperations.div(Object, Object)'
     */
    @Test
    public void testDiv() {
        assertEquals(Long.valueOf(0), NumberOperations.div(converter, null, null));

        BigDecimal bd1 = new BigDecimal(1);
        Integer i1 = Integer.valueOf(1);
        Long l1 = Long.valueOf(1);
        Float f1 = Float.valueOf(1);
        Double d1 = Double.valueOf(1);
        String e1 = "1e0";
        String s1 = "1";
        BigInteger bi1 = new BigInteger("1");

        BigDecimal bd2 = new BigDecimal(1);
        Double d2 = Double.valueOf(1);

        assertEquals(bd2, NumberOperations.div(converter, l1, bd1));
        assertEquals(bd2, NumberOperations.div(converter, bd1, l1));

        assertEquals(bd2, NumberOperations.div(converter, f1, bi1));
        assertEquals(bd2, NumberOperations.div(converter, bi1, f1));

        assertEquals(d2, NumberOperations.div(converter, f1, l1));
        assertEquals(d2, NumberOperations.div(converter, l1, f1));

        assertEquals(d2, NumberOperations.div(converter, d1, l1));
        assertEquals(d2, NumberOperations.div(converter, l1, d1));

        assertEquals(d2, NumberOperations.div(converter, e1, l1));
        assertEquals(d2, NumberOperations.div(converter, l1, e1));

        assertEquals(d2, NumberOperations.div(converter, i1, l1));
        assertEquals(d2, NumberOperations.div(converter, l1, i1));

        assertEquals(d2, NumberOperations.div(converter, i1, s1));
        assertEquals(d2, NumberOperations.div(converter, s1, i1));
    }

    /*
     * Test method for 'org.activiti.core.el.juel.NumberOperations.mod(Object, Object)'
     */
    @Test
    public void testMod() {
        assertEquals(Long.valueOf(0), NumberOperations.mod(converter, null, null));

        BigDecimal bd1 = new BigDecimal(1);
        Integer i1 = Integer.valueOf(1);
        Long l1 = Long.valueOf(1);
        Float f1 = Float.valueOf(1);
        Double d1 = Double.valueOf(1);
        String e1 = "1e0";
        String s1 = "1";
        BigInteger bi1 = new BigInteger("1");

        Long l2 = Long.valueOf(0);
        Double d2 = Double.valueOf(0);
        BigInteger bi2 = new BigInteger("0");

        assertEquals(d2, NumberOperations.mod(converter, l1, bd1));
        assertEquals(d2, NumberOperations.mod(converter, bd1, l1));

        assertEquals(d2, NumberOperations.mod(converter, f1, bi1));
        assertEquals(d2, NumberOperations.mod(converter, bi1, f1));

        assertEquals(d2, NumberOperations.mod(converter, f1, l1));
        assertEquals(d2, NumberOperations.mod(converter, l1, f1));

        assertEquals(d2, NumberOperations.mod(converter, d1, l1));
        assertEquals(d2, NumberOperations.mod(converter, l1, d1));

        assertEquals(d2, NumberOperations.mod(converter, d1, bi1));
        assertEquals(d2, NumberOperations.mod(converter, bi1, d1));

        assertEquals(d2, NumberOperations.mod(converter, e1, bi1));
        assertEquals(d2, NumberOperations.mod(converter, bi1, e1));

        assertEquals(d2, NumberOperations.mod(converter, e1, l1));
        assertEquals(d2, NumberOperations.mod(converter, l1, e1));

        assertEquals(bi2, NumberOperations.mod(converter, l1, bi1));
        assertEquals(bi2, NumberOperations.mod(converter, bi1, l1));

        assertEquals(l2, NumberOperations.mod(converter, i1, l1));
        assertEquals(l2, NumberOperations.mod(converter, l1, i1));

        assertEquals(l2, NumberOperations.mod(converter, i1, s1));
        assertEquals(l2, NumberOperations.mod(converter, s1, i1));
    }

    /*
     * Test method for 'org.activiti.core.el.juel.NumberOperations.neg(Object)'
     */
    @Test
    public void testNeg() {
        assertEquals(Long.valueOf(0), NumberOperations.neg(converter, null));

        BigDecimal bd1 = new BigDecimal(1);
        Integer i1 = Integer.valueOf(1);
        Long l1 = Long.valueOf(1);
        Float f1 = Float.valueOf(1);
        Double d1 = Double.valueOf(1);
        String e1 = "1e0";
        String s1 = "1";
        BigInteger bi1 = new BigInteger("1");

        BigDecimal bd2 = new BigDecimal(-1);
        Integer i2 = Integer.valueOf(-1);
        Long l2 = Long.valueOf(-1);
        Float f2 = Float.valueOf(-1);
        Double d2 = Double.valueOf(-1);
        BigInteger bi2 = new BigInteger("-1");

        assertEquals(bd2, NumberOperations.neg(converter, bd1));
        assertEquals(bi2, NumberOperations.neg(converter, bi1));
        assertEquals(d2, NumberOperations.neg(converter, e1));
        assertEquals(l2, NumberOperations.neg(converter, s1));
        assertEquals(i2, NumberOperations.neg(converter, i1));
        assertEquals(l2, NumberOperations.neg(converter, l1));
        assertEquals(d2, NumberOperations.neg(converter, d1));
        assertEquals(f2, NumberOperations.neg(converter, f1));
    }
}
