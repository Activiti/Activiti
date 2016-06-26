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
import com.activiti.model.editor.form.FormOutcomeRepresentation;


public class OutcomeValuesRepresentation extends AbstractRepresentation {

    protected Long formId;
    protected String formName;
    protected List<FormOutcomeRepresentation> outcomes = new ArrayList<FormOutcomeRepresentation>();
    
    public Long getFormId() {
        return formId;
    }
    public void setFormId(Long formId) {
        this.formId = formId;
    }
    public String getFormName() {
        return formName;
    }
    public void setFormName(String formName) {
        this.formName = formName;
    }
    public List<FormOutcomeRepresentation> getOutcomes() {
        return outcomes;
    }
    public void setOutcomes(List<FormOutcomeRepresentation> outcomes) {
        this.outcomes = outcomes;
    }
}
