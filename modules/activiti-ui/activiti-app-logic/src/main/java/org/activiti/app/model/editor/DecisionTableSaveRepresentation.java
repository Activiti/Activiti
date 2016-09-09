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
import org.activiti.app.model.editor.decisiontable.DecisionTableRepresentation;


public class DecisionTableSaveRepresentation extends AbstractRepresentation {

    protected boolean reusable;
    protected boolean newVersion;
    protected String comment;
    protected String decisionTableImageBase64;
    protected DecisionTableRepresentation decisionTableRepresentation;

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
    public String getDecisionTableImageBase64() {
		return decisionTableImageBase64;
	}
	public void setDecisionTableImageBase64(String decisionTableImageBase64) {
		this.decisionTableImageBase64 = decisionTableImageBase64;
	}
	public DecisionTableRepresentation getDecisionTableRepresentation() {
        return decisionTableRepresentation;
    }
    public void setDecisionTableRepresentation(DecisionTableRepresentation decisionTableRepresentation) {
        this.decisionTableRepresentation = decisionTableRepresentation;
    }
}
