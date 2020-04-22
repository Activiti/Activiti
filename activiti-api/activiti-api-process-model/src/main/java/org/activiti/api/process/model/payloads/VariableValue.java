package org.activiti.api.process.model.payloads;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

public class VariableValue implements Serializable {

    private static final long serialVersionUID = 1L;
    private String type;
    private String value;

    VariableValue() { }

    public VariableValue(String type, String value) {
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
        VariableValue other = (VariableValue) obj;
        return Objects.equals(type, other.type) && Objects.equals(value, other.value);
    }

    @Override
    public String toString() {
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

    public String escape( String value )
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

}
