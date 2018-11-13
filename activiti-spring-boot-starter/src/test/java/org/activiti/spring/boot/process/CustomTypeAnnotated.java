package org.activiti.spring.boot.process;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.activiti.engine.impl.variable.JsonType;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = JsonType.JAVA_TYPE_FIELD)
public class CustomTypeAnnotated {

    String customTypeField1;
    String customTypeField2;

    public String getCustomTypeField1() {
        return customTypeField1;
    }

    public void setCustomTypeField1(String customTypeField1) {
        this.customTypeField1 = customTypeField1;
    }

    public String getCustomTypeField2() {
        return customTypeField2;
    }

    public void setCustomTypeField2(String customTypeField2) {
        this.customTypeField2 = customTypeField2;
    }
}
