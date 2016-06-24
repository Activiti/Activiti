/**
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package org.activiti.editor.dmn.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yvoswillens on 27/08/15.
 */
public class DmnJsonConverterUtil {

    protected static final Logger logger = LoggerFactory.getLogger(DmnJsonConverterUtil.class);

    private static ObjectMapper objectMapper = new ObjectMapper();

    public static String getValueAsString(String name, JsonNode objectNode) {
        String propertyValue = null;
        JsonNode jsonNode = objectNode.get(name);
        if (jsonNode != null && jsonNode.isNull() == false) {
            propertyValue = jsonNode.asText();
        }
        return propertyValue;
    }
}
