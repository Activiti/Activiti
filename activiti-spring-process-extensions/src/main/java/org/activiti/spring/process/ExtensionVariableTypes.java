package org.activiti.spring.process;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

/***
 * Only used when reading extension json containing defaults, not runtime variables.
 */
public enum ExtensionVariableTypes {

    BOOLEAN("boolean",Boolean.class),
    STRING("string",String.class),
    INTEGER("integer",Integer.class),
    JSON("json", ObjectNode.class),
    DATE("date",Date.class,new SimpleDateFormat("yyyy-MM-dd"));

    String name;
    Class clazz;
    Format format;

    private ExtensionVariableTypes(String name, Class clazz){
        this.name = name;
        this.clazz = clazz;
    }

    private ExtensionVariableTypes(String name, Class clazz, Format format){
        this(name,clazz);
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
