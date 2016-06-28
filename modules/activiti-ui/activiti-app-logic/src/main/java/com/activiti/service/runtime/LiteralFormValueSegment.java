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

import java.util.Map;

import com.activiti.model.editor.form.FormFieldRepresentation;


/**
 * {@link FormValueSegment} representing a literal string.
 * 
 * @author Frederik Heremans
 */
public class LiteralFormValueSegment extends FormValueSegment {

    private String value;
    
    public LiteralFormValueSegment(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public String getStringValue(Map<String, Object> fieldValues,  Map<String, String> fieldTypes, FormFieldRepresentation field) {
        return value;
    }
}
