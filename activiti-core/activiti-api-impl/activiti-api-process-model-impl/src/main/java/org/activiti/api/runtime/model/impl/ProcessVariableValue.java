/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
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

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ProcessVariableValue implements Serializable {

    private static final long serialVersionUID = 1L;
    private String type;
    private String value;

    ProcessVariableValue() { }

    private ProcessVariableValue(Builder builder) {
        this.type = builder.type;
        this.value = builder.value;
    }

    public ProcessVariableValue(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ProcessVariableValue other = (ProcessVariableValue) obj;
        return Objects.equals(type, other.type) && Objects.equals(value, other.value);
    }

    public Map<String, String> toMap() {
        Map<String, String> result = new LinkedHashMap<>(2);

        result.put("type", type);
        result.put("value", value);

        return result;
    }

    public String toJson() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"type\":\"")
               .append(type)
               .append("\",\"value\":")
               .append(Optional.ofNullable(value)
                               .map(this::escape)
                               .orElse("null"))
               .append("}");
        return builder.toString();
    }

    @Override
    public String toString() {
        return toJson();
    }

    private String escape( String value )
    {
        StringBuilder builder = new StringBuilder();
        builder.append( "\"" );
        for( char c : value.toCharArray() )
        {
            if( c == '\'' )
                builder.append( "\\'" );
            else if ( c == '\"' )
                builder.append( "\\\"" );
            else if( c == '\r' )
                builder.append( "\\r" );
            else if( c == '\n' )
                builder.append( "\\n" );
            else if( c == '\t' )
                builder.append( "\\t" );
            else if( c < 32 || c >= 127 )
                builder.append( String.format( "\\u%04x", (int)c ) );
            else
                builder.append( c );
        }
        builder.append( "\"" );
        return builder.toString();
    }

    /**
     * Creates builder to build {@link ProcessVariableValue}.
     * @return created builder
     */
    public static ITypeStage builder() {
        return new Builder();
    }

    /**
     * Definition of a stage for staged builder.
     */
    public interface ITypeStage {

        /**
        * Builder method for type parameter.
        * @param type field to set
        * @return builder
        */
        public IValueStage type(String type);
    }

    /**
     * Definition of a stage for staged builder.
     */
    public interface IValueStage {

        /**
        * Builder method for value parameter.
        * @param value field to set
        * @return builder
        */
        public IBuildStage value(String value);
    }

    /**
     * Definition of a stage for staged builder.
     */
    public interface IBuildStage {

        /**
        * Builder method of the builder.
        * @return built class
        */
        public ProcessVariableValue build();
    }

    /**
     * Builder to build {@link ProcessVariableValue}.
     */
    public static final class Builder implements ITypeStage, IValueStage, IBuildStage {

        private String type;
        private String value;

        private Builder() {
        }

        @Override
        public IValueStage type(String type) {
            this.type = type;
            return this;
        }

        @Override
        public IBuildStage value(String value) {
            this.value = value;
            return this;
        }

        @Override
        public ProcessVariableValue build() {
            return new ProcessVariableValue(this);
        }
    }
}
