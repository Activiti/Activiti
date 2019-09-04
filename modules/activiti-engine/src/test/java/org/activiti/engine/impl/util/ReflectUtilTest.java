package org.activiti.engine.impl.util;

import junit.framework.TestCase;

public class ReflectUtilTest extends TestCase {

    public void testLoadNonPrimitiveClass() {
        Class<?> aClass = ReflectUtil.loadClass("java.lang.Integer");
        assertNotNull(aClass);
    }

    public void testLoadPrimitiveClass() {
        Class<?> aClass = ReflectUtil.loadClass("int");
        assertNotNull(aClass);
    }
}