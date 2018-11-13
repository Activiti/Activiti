package org.activiti.spring.boot.process;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
        final String type = jsonNode.get(JsonTypeInfo.Id.CLASS.getDefaultPropertyName()).asText();
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

        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        assertThat(objectMapper.canSerialize(CustomType.class)).isTrue();

        final String json = objectMapper.writeValueAsString(customType);

        ObjectNode jsonNode = (ObjectNode) objectMapper.readTree(json);


        final Object o = objectMapper.convertValue(jsonNode, Object.class);

        assertThat(o.getClass()).isEqualTo(CustomType.class);
    }

    @Test
    public void dontBlowUpWhenTypeNotOnClassPath() throws IOException {

        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE,false);

        final String json = "{ \"testvar2element\":\"testvar2element\",\"@class\":\"ClassNotOnClassPath\"}";

        ObjectNode jsonNode = (ObjectNode) objectMapper.readTree(json);


        final Object o = objectMapper.convertValue(jsonNode, Object.class);

        assertThat(o).isNull();
    }


}
