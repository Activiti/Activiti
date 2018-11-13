package org.activiti.spring.boot.process;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.engine.impl.variable.JsonType;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;


public class DynamicCustomTypeSerializeDeserializeTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    private CustomTypeAnnotated customTypeAnnotated = new CustomTypeAnnotated();

    private CustomType customType = new CustomType();

    @Test
    public void serializeAndDeserializeCustomPOJOWithAnnotation() throws IOException {

        assertThat(objectMapper.canSerialize(CustomTypeAnnotated.class)).isTrue();

        final String json = objectMapper.writeValueAsString(customTypeAnnotated);

        final JsonNode jsonNode = objectMapper.readTree(json);

        //need to find the type of the class in order to deserialize it
        //can do this so long as JsonTypeInfo annotation on the class - see https://stackoverflow.com/a/28384407/9705485
        final String type = jsonNode.get(JsonType.JAVA_TYPE_FIELD).asText();
        Class<?> cls = null;
        try {
            cls = Class.forName(type,false,this.getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        final Object o = objectMapper.convertValue(jsonNode, cls);

        assertThat(o.getClass()).isEqualTo(CustomTypeAnnotated.class);
    }


    @Test
    public void serializeAndDeserializeCustomPOJOWithoutAnnotation() throws IOException {

        assertThat(objectMapper.canSerialize(CustomType.class)).isTrue();

        final String json = objectMapper.writeValueAsString(customTypeAnnotated);

        ObjectNode jsonNode = (ObjectNode) objectMapper.readTree(json);

        jsonNode.put(JsonType.JAVA_TYPE_FIELD,customType.getClass().getTypeName());

        final String type = jsonNode.get(JsonType.JAVA_TYPE_FIELD).asText();
        Class<?> cls = null;
        try {
            cls = Class.forName(type,false,this.getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        //now deserialize the object but it doesn't have a `type` field
        // or an annotation telling jackson to ignore a type field so we need to tell jackson that's ok
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        final Object o = objectMapper.convertValue(jsonNode, cls);

        assertThat(o.getClass()).isEqualTo(CustomType.class);
    }

    @Test
    public void verifyClassHasAnnotation(){
        //JsonTypeInfo annotation is needed for us to deserialize

        assertThat(CustomTypeAnnotated.class.isAnnotationPresent(JsonTypeInfo.class));
        assertThat(CustomTypeAnnotated.class.getAnnotation(JsonTypeInfo.class).property()).isEqualTo(JsonType.JAVA_TYPE_FIELD);
        assertThat(CustomTypeAnnotated.class.getAnnotation(JsonTypeInfo.class).include()).isEqualTo(JsonTypeInfo.As.PROPERTY);
        assertThat(CustomTypeAnnotated.class.getAnnotation(JsonTypeInfo.class).use()).isEqualTo(JsonTypeInfo.Id.CLASS);
    }

}
