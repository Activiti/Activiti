package org.activiti.starter.tests.util;

import java.io.IOException;
import java.io.InputStream;

import org.activiti.engine.impl.util.IoUtil;

public class TestResourceUtil {

    public static String getProcessXml(final String processDefinitionKey) throws IOException {
        try (InputStream is = ClassLoader.getSystemResourceAsStream("processes/" + processDefinitionKey + ".bpmn20.xml")) {
            return new String(IoUtil.readInputStream(is, null), "UTF-8");
        }
    }
}
