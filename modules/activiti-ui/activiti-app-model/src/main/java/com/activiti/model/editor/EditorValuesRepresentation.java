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
package com.activiti.model.editor;

import java.util.ArrayList;
import java.util.List;

import com.activiti.model.common.AbstractRepresentation;


public class EditorValuesRepresentation extends AbstractRepresentation {

    protected List<FormFieldValuesRepresentation> formFieldValues = new ArrayList<FormFieldValuesRepresentation>();
    protected List<OutcomeValuesRepresentation> outcomeValues = new ArrayList<OutcomeValuesRepresentation>();
    
    public List<FormFieldValuesRepresentation> getFormFieldValues() {
        return formFieldValues;
    }
    public void setFormFieldValues(
            List<FormFieldValuesRepresentation> formFieldValues) {
        this.formFieldValues = formFieldValues;
    }
    public List<OutcomeValuesRepresentation> getOutcomeValues() {
        return outcomeValues;
    }
    public void setOutcomeValues(List<OutcomeValuesRepresentation> outcomeValues) {
        this.outcomeValues = outcomeValues;
    }
}
