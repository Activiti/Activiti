package org.activiti.spring.boot.process;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;


public class DynamicCustomTypeSerializeDeserializeTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    private CustomTypeAnnotated customObj = new CustomTypeAnnotated();

    @Test
    public void serializeAndDeserializeCustomTypeWithAnnotation() throws IOException {

        assertThat(objectMapper.canSerialize(CustomTypeAnnotated.class)).isTrue();

        final String json = objectMapper.writeValueAsString(customObj);

        final JsonNode jsonNode = objectMapper.readTree(json);

        //need to find the type of the class in order to deserialize it
        //can do this so long as JsonTypeInfo annotation on the class - see https://stackoverflow.com/a/28384407/9705485
        final String type = jsonNode.get("type").asText();
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
    public void verifyClassHasAnnotation(){
        //JsonTypeInfo annotation is needed for us to deserialize

        assertThat(CustomTypeAnnotated.class.isAnnotationPresent(JsonTypeInfo.class));
        assertThat(CustomTypeAnnotated.class.getAnnotation(JsonTypeInfo.class).property()).isEqualTo("type");
        assertThat(CustomTypeAnnotated.class.getAnnotation(JsonTypeInfo.class).include()).isEqualTo(JsonTypeInfo.As.PROPERTY);
        assertThat(CustomTypeAnnotated.class.getAnnotation(JsonTypeInfo.class).use()).isEqualTo(JsonTypeInfo.Id.CLASS);
    }

}
