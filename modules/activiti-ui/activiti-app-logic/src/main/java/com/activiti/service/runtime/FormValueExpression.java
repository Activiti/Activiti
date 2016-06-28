/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
