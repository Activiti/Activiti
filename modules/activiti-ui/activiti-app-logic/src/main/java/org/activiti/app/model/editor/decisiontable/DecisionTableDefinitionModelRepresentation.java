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
package org.activiti.app.model.editor.decisiontable;

/**
 * @author Bassam Al-Sarori
 */
public class DecisionTableDefinitionModelRepresentation {

    protected DecisionTableDefinitionRepresentation decisionTableDefinition;
    protected String description;
    protected String editorJson;

    public DecisionTableDefinitionRepresentation getDecisionTableDefinition() {
        return decisionTableDefinition;
    }
    
    public void setDecisionTableDefinition(DecisionTableDefinitionRepresentation decisionTableDefinition) {
        this.decisionTableDefinition = decisionTableDefinition;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEditorJson() {
        return editorJson;
    }

    public void setEditorJson(String editorJson) {
        this.editorJson = editorJson;
    }
}
