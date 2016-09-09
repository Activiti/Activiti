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
package org.activiti.app.model.editor;

import java.util.ArrayList;
import java.util.List;

import org.activiti.app.model.common.AbstractRepresentation;


public class FormFieldValuesRepresentation extends AbstractRepresentation {

    protected Long formId;
    protected String formName;
    protected List<FormFieldSummaryRepresentation> fields = new ArrayList<FormFieldSummaryRepresentation>();
    
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
    public List<FormFieldSummaryRepresentation> getFields() {
        return fields;
    }
    public void setFields(List<FormFieldSummaryRepresentation> fields) {
        this.fields = fields;
    }
}
