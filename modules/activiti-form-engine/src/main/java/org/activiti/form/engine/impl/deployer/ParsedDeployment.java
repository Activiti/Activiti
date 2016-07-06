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
package org.activiti.form.engine.impl.deployer;

import java.util.List;
import java.util.Map;

import org.activiti.form.engine.impl.parser.FormParse;
import org.activiti.form.engine.impl.persistence.entity.FormDeploymentEntity;
import org.activiti.form.engine.impl.persistence.entity.FormEntity;
import org.activiti.form.engine.impl.persistence.entity.ResourceEntity;
import org.activiti.form.model.FormDefinition;

/**
 * An intermediate representation of a DeploymentEntity which keeps track of all of the entity's
 * DecisionTableEntities and resources, and BPMN parses, models, and processes associated
 * with each DecisionTableEntity - all produced by parsing the deployment.
 * 
 * The DecisionTableEntities are expected to be "not fully set-up" - they may be inconsistent with the 
 * DeploymentEntity and/or the persisted versions, and if the deployment is new, they will not yet be persisted.
 */
public class ParsedDeployment {
  
  protected FormDeploymentEntity deploymentEntity;

  protected List<FormEntity> forms;
  protected Map<FormEntity, FormParse> mapFormsToParses;
  protected Map<FormEntity, ResourceEntity> mapFormsToResources;
  
  public ParsedDeployment(
      FormDeploymentEntity entity, List<FormEntity> forms,
      Map<FormEntity, FormParse> mapFormsToParses,
      Map<FormEntity, ResourceEntity> mapFormsToResources) {
    
    this.deploymentEntity = entity;
    this.forms = forms;
    this.mapFormsToParses = mapFormsToParses;
    this.mapFormsToResources = mapFormsToResources;
  }

  
  public FormDeploymentEntity getDeployment() {
    return deploymentEntity;
  }

  public List<FormEntity> getAllForms() {
    return forms;
  }

  public ResourceEntity getResourceForForm(FormEntity form) {
    return mapFormsToResources.get(form);
  }

  public FormParse getFormParseForForm(FormEntity form) {
    return mapFormsToParses.get(form);
  }

  public FormDefinition getFormDefinitionForForm(FormEntity form) {
    FormParse parse = getFormParseForForm(form);
    
    return (parse == null ? null : parse.getFormDefinition());
  } 
}

