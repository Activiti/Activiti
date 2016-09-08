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

import org.activiti.app.model.common.AbstractRepresentation;
import org.activiti.app.model.editor.form.FormRepresentation;


public class FormSaveRepresentation extends AbstractRepresentation {

    protected boolean reusable;
    protected boolean newVersion;
    protected String comment;
    protected String formImageBase64;
    protected FormRepresentation formRepresentation;
    
    public boolean isReusable() {
        return reusable;
    }
    public void setReusable(boolean reusable) {
        this.reusable = reusable;
    }
    public boolean isNewVersion() {
        return newVersion;
    }
    public void setNewVersion(boolean newVersion) {
        this.newVersion = newVersion;
    }
    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
    public String getFormImageBase64() {
		return formImageBase64;
	}
	public void setFormImageBase64(String formImageBase64) {
		this.formImageBase64 = formImageBase64;
	}
	public FormRepresentation getFormRepresentation() {
        return formRepresentation;
    }
    public void setFormRepresentation(FormRepresentation formRepresentation) {
        this.formRepresentation = formRepresentation;
    }
}
