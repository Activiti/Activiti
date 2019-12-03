package org.activiti.spring.boot.process;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY,property = "@class")
public class CustomTypeAnnotated extends CustomType{
}
