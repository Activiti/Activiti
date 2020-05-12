package org.activiti.api.runtime.test.model.impl;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY,property = "@class")
public class CustomPojoAnnotated extends CustomPojo {

    CustomPojoAnnotated() {
    }

    public CustomPojoAnnotated(String string, String string2) {
        super(string, string2);
    }
}
