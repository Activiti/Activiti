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
package com.activiti.model.editor.form;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Erik Winlof
 *
 */
public class ContainerRepresentation extends FormFieldRepresentation {

    protected Map<String, List<FormFieldRepresentation>> fields = new HashMap<String, List<FormFieldRepresentation>>();

    public Map<String, List<FormFieldRepresentation>> getFields() {
        return fields;
    }

    public void setFields(Map<String, List<FormFieldRepresentation>> fields) {
        this.fields = fields;
    }
}