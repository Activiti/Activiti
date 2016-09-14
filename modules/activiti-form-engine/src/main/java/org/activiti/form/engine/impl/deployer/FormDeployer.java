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

import java.util.LinkedHashMap;
import java.util.Map;

import org.activiti.form.engine.impl.cfg.IdGenerator;
import org.activiti.form.engine.impl.context.Context;
import org.activiti.form.engine.impl.interceptor.CommandContext;
import org.activiti.form.engine.impl.persistence.deploy.Deployer;
import org.activiti.form.engine.impl.persistence.entity.FormDeploymentEntity;
import org.activiti.form.engine.impl.persistence.entity.FormEntity;
import org.activiti.form.engine.impl.persistence.entity.FormEntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
public class FormDeployer implements Deployer {

  private static final Logger log = LoggerFactory.getLogger(FormDeployer.class);

  protected IdGenerator idGenerator;
  protected ParsedDeploymentBuilderFactory parsedDeploymentBuilderFactory;
  protected FormDeploymentHelper formDeploymentHelper;
  protected CachingAndArtifactsManager cachingAndArtifactsManager;

  public void deploy(FormDeploymentEntity deployment) {
    log.debug("Processing deployment {}", deployment.getName());

    // The ParsedDeployment represents the deployment, the forms, and the form
    // resource, parse, and model associated with each form.
    ParsedDeployment parsedDeployment = parsedDeploymentBuilderFactory.getBuilderForDeployment(deployment).build();

    formDeploymentHelper.verifyFormsDoNotShareKeys(parsedDeployment.getAllForms());

    formDeploymentHelper.copyDeploymentValuesToForms(parsedDeployment.getDeployment(), parsedDeployment.getAllForms());
    formDeploymentHelper.setResourceNamesOnForms(parsedDeployment);

    if (deployment.isNew()) {
      Map<FormEntity, FormEntity> mapOfNewFormToPreviousVersion = getPreviousVersionsOfForms(parsedDeployment);
      setFormVersionsAndIds(parsedDeployment, mapOfNewFormToPreviousVersion);
      persistForms(parsedDeployment);
    } else {
      makeFormsConsistentWithPersistedVersions(parsedDeployment);
    }

    cachingAndArtifactsManager.updateCachingAndArtifacts(parsedDeployment);
  }

  /**
   * Constructs a map from new FormEntities to the previous version by key and tenant. If no previous version exists, no map entry is created.
   */
  protected Map<FormEntity, FormEntity> getPreviousVersionsOfForms(ParsedDeployment parsedDeployment) {

    Map<FormEntity, FormEntity> result = new LinkedHashMap<FormEntity, FormEntity>();

    for (FormEntity newDefinition : parsedDeployment.getAllForms()) {
      FormEntity existingForm = formDeploymentHelper.getMostRecentVersionOfForm(newDefinition);

      if (existingForm != null) {
        result.put(newDefinition, existingForm);
      }
    }

    return result;
  }

  /**
   * Sets the version on each form entity, and the identifier. If the map contains an older version for a form, then the version is set to that older entity's version plus one; otherwise it is set to
   * 1.
   */
  protected void setFormVersionsAndIds(ParsedDeployment parsedDeployment, Map<FormEntity, FormEntity> mapNewToOldForms) {

    for (FormEntity form : parsedDeployment.getAllForms()) {
      int version = 1;

      FormEntity latest = mapNewToOldForms.get(form);
      if (latest != null) {
        version = latest.getVersion() + 1;
      }

      form.setVersion(version);
      form.setId(idGenerator.getNextId());
    }
  }

  /**
   * Saves each decision table. It is assumed that the deployment is new, the definitions have never been saved before, and that they have all their values properly set up.
   */
  protected void persistForms(ParsedDeployment parsedDeployment) {
    CommandContext commandContext = Context.getCommandContext();
    FormEntityManager formEntityManager = commandContext.getFormEntityManager();

    for (FormEntity form : parsedDeployment.getAllForms()) {
      formEntityManager.insert(form);
    }
  }

  /**
   * Loads the persisted version of each form and set values on the in-memory version to be consistent.
   */
  protected void makeFormsConsistentWithPersistedVersions(ParsedDeployment parsedDeployment) {
    for (FormEntity form : parsedDeployment.getAllForms()) {
      FormEntity persistedForm = formDeploymentHelper.getPersistedInstanceOfForm(form);

      if (persistedForm != null) {
        form.setId(persistedForm.getId());
        form.setVersion(persistedForm.getVersion());
      }
    }
  }

  public IdGenerator getIdGenerator() {
    return idGenerator;
  }

  public void setIdGenerator(IdGenerator idGenerator) {
    this.idGenerator = idGenerator;
  }

  public ParsedDeploymentBuilderFactory getExParsedDeploymentBuilderFactory() {
    return parsedDeploymentBuilderFactory;
  }

  public void setParsedDeploymentBuilderFactory(ParsedDeploymentBuilderFactory parsedDeploymentBuilderFactory) {
    this.parsedDeploymentBuilderFactory = parsedDeploymentBuilderFactory;
  }

  public FormDeploymentHelper getFormDeploymentHelper() {
    return formDeploymentHelper;
  }

  public void setFormDeploymentHelper(FormDeploymentHelper formDeploymentHelper) {
    this.formDeploymentHelper = formDeploymentHelper;
  }

  public CachingAndArtifactsManager getCachingAndArtifcatsManager() {
    return cachingAndArtifactsManager;
  }

  public void setCachingAndArtifactsManager(CachingAndArtifactsManager manager) {
    this.cachingAndArtifactsManager = manager;
  }
}
