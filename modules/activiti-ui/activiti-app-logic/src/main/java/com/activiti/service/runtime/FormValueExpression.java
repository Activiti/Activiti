/**
 * Activiti app component part of the Activiti project
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.activiti.service.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.activiti.model.component.SimpleContentTypeMapper;
import com.activiti.model.editor.form.FormFieldRepresentation;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Container for all values in a form that are based on previously filled in
 * fields, which eventually resolves to a concatenated String.
 * 
 * @author Frederik Heremans
 */
public class FormValueExpression {

    private static final Logger logger = LoggerFactory.getLogger(FormValueExpression.class);

    protected List<FormValueSegment> segments;
    protected static Pattern pattern = Pattern.compile("\\$\\{[^\\}]*\\}");
    protected FormFieldRepresentation field;

    public FormValueExpression(FormFieldRepresentation field) {
        segments = new ArrayList<FormValueSegment>();
        this.field = field;
    }

    public static FormValueExpression parse(String expression, FormFieldRepresentation field) {
        FormValueExpression result = new FormValueExpression(field);

        // Look for any expressions
        Matcher matcher = pattern.matcher(expression);
        int start = 0;
        while (matcher.find(start)) {
            // Process raw string from previous start until new start
            if (start != matcher.start()) {
                result.segments.add(new LiteralFormValueSegment(expression.substring(start, matcher.start())));
            }

            if (matcher.start() < matcher.end() - 3) {
                // Ignore empty expressions
                result.segments.add(new FieldValueFormValueSegment(expression.substring(matcher.start() + 2, matcher.end() - 1)));
            }

            // Reset the start for the next find cycle
            start = matcher.end();
        }

        // Any text remaining after the expression, can just be added as literal
        if (start < expression.length()) {
            result.segments.add(new LiteralFormValueSegment(expression.substring(start)));
        }
        return result;
    }

    public void apply(Map<String, Object> fieldValues, Map<String, String> variableTypes, String processInstanceId, 
            RelatedContentService relatedContentService, SimpleContentTypeMapper contentTypeMapper, ObjectMapper objectMapper) {

        if (segments.size() == 1 && segments.get(0) instanceof FieldValueFormValueSegment) {
            // In case the expression is a single value, use the raw object value
            FieldValueFormValueSegment valueSegment = (FieldValueFormValueSegment) segments.get(0);
            field.setValue(fieldValues.get(valueSegment.getFieldId()));

        } else {
            // Create a string-representation of all segments concatenated
            StringBuilder builder = new StringBuilder();

            String value = null;
            for (FormValueSegment segment : segments) {
                value = segment.getStringValue(fieldValues, variableTypes, field);
                if (value != null) {
                    builder.append(value);
                }
            }

            field.setValue(builder.toString());
        }
    }

    /**
     * @return a list of field values needed in order to get the value for this
     *         expression.
     */
    public List<String> getRequiredFieldIds() {
        ArrayList<String> result = new ArrayList<String>();

        for (FormValueSegment segment : segments) {
            if (segment instanceof FieldValueFormValueSegment) {
                result.add((((FieldValueFormValueSegment) segment)).getFieldId());
            }
        }
        return result;
    }
}
