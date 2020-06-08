/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.api.runtime.model.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;

public class ProcessVariablesMapTypeRegistry {

    private static Map<String, Class<?>> typeRegistry = new HashMap<>();
    private static Map<Class<?>, String> classRegistry = new HashMap<>();
    private static List<Class<?>> scalarTypes = Arrays.asList(int.class,
                                                              byte.class,
                                                              short.class,
                                                              boolean.class,
                                                              long.class,
                                                              double.class,
                                                              float.class,
                                                              char.class,
                                                              Character.class,
                                                              Integer.class,
                                                              Byte.class,
                                                              Short.class,
                                                              Boolean.class,
                                                              Long.class,
                                                              Double.class,
                                                              Float.class,
                                                              BigDecimal.class,
                                                              Date.class,
                                                              String.class,
                                                              LocalDateTime.class,
                                                              LocalDate.class);

    private static Class<?>[] containerTypes = {Map.class,
                                                JsonNode.class,
                                                List.class,
                                                Set.class};

    static {
        typeRegistry.put("byte", Byte.class);
        typeRegistry.put("character", Character.class);
        typeRegistry.put("short", Short.class);
        typeRegistry.put("string", String.class);
        typeRegistry.put("long", Long.class);
        typeRegistry.put("integer", Integer.class);
        typeRegistry.put("boolean", Boolean.class);
        typeRegistry.put("double", Double.class);
        typeRegistry.put("float", Float.class);
        typeRegistry.put("date", Date.class);
        typeRegistry.put("localdate", LocalDate.class);
        typeRegistry.put("localdatetime", LocalDateTime.class);
        typeRegistry.put("bigdecimal", BigDecimal.class);
        typeRegistry.put("json", JsonNode.class);
        typeRegistry.put("map", Map.class);
        typeRegistry.put("set", Set.class);
        typeRegistry.put("list", List.class);
        typeRegistry.put("object", ObjectValue.class);

        classRegistry.put(Byte.class, "byte");
        classRegistry.put(Character.class, "character");
        classRegistry.put(Short.class, "short");
        classRegistry.put(String.class, "string");
        classRegistry.put(Long.class, "long");
        classRegistry.put(Integer.class, "integer");
        classRegistry.put(Boolean.class, "boolean");
        classRegistry.put(Double.class, "double");
        classRegistry.put(Float.class, "float");
        classRegistry.put(Date.class, "date");
        classRegistry.put(LocalDate.class, "localdate");
        classRegistry.put(BigDecimal.class, "bigdecimal");
        classRegistry.put(JsonNode.class, "json");
        classRegistry.put(Map.class, "map");
        classRegistry.put(List.class, "list");
        classRegistry.put(Set.class, "set");
        classRegistry.put(LocalDateTime.class, "localdatetime");
        classRegistry.put(ObjectValue.class, "object");
    }

    public static Class<?> forType(String type) {
        return forType(type, ObjectValue.class);
    }

    public static Class<?> forType(String type, Class<?> defaultType) {
        return typeRegistry.getOrDefault(type, defaultType);
    }

    public static String forClass(Class<?> clazz) {
        return classRegistry.getOrDefault(clazz, "object");
    }

    public static boolean isScalarType(Class<?> clazz) {
        return scalarTypes.contains(clazz);
    }

    public static Optional<Class<?>> getContainerType(Class<?> clazz,
                                                      Object value) {
        return Stream.of(containerTypes)
                     .filter(type -> type.isInstance(value))
                     .findFirst();
    }

    public static boolean canConvert(Object value) {
        Class<?> clazz = value.getClass();

        return scalarTypes.contains(clazz) || getContainerType(clazz, value).isPresent();
    }

    public static boolean containsType(String type) {
        return typeRegistry.containsKey(type);
    }

}
