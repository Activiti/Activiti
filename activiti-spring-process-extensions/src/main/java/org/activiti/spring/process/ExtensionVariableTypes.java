package org.activiti.spring.process;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public enum ExtensionVariableTypes {

    BOOLEAN("boolean",Boolean.class),
    STRING("string",String.class),
    INTEGER("integer",Integer.class),
    JSON("json", ObjectNode.class),
    DATE("date",new SimpleDateFormat("yyyy-MM-dd"),Date.class);

    String name;
    Set<Class> classes;
    Format format;

    private ExtensionVariableTypes(String name, Class... classes){
        this.name = name;
        this.classes = new HashSet(Arrays.asList(classes));
    }

    private ExtensionVariableTypes(String name, Format format, Class... classes){
        this(name,classes);
        this.format=format;
    }
    public static ExtensionVariableTypes getEnumByString(String name){
        for(ExtensionVariableTypes e : ExtensionVariableTypes.values()){
            if(name.equals( e.name )) {
                return e;
            }
        }
        return null;
    }
    public SimpleDateFormat getDateFormat(){
        return (SimpleDateFormat)format;
    }
}
