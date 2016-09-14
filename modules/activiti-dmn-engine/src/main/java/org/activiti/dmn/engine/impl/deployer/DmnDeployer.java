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
package org.activiti.dmn.engine.impl.deployer;

import java.util.LinkedHashMap;
import java.util.Map;

import org.activiti.dmn.engine.impl.cfg.IdGenerator;
import org.activiti.dmn.engine.impl.context.Context;
import org.activiti.dmn.engine.impl.interceptor.CommandContext;
import org.activiti.dmn.engine.impl.persistence.deploy.Deployer;
import org.activiti.dmn.engine.impl.persistence.entity.DecisionTableEntity;
import org.activiti.dmn.engine.impl.persistence.entity.DecisionTableEntityManager;
import org.activiti.dmn.engine.impl.persistence.entity.DmnDeploymentEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
public class DmnDeployer implements Deployer {

  private static final Logger log = LoggerFactory.getLogger(DmnDeployer.class);

  protected IdGenerator idGenerator;
  protected ParsedDeploymentBuilderFactory parsedDeploymentBuilderFactory;
  protected DmnDeploymentHelper dmnDeploymentHelper;
  protected CachingAndArtifactsManager cachingAndArtifactsManager;

  public void deploy(DmnDeploymentEntity deployment, Map<String, Object> deploymentSettings) {
    log.debug("Processing deployment {}", deployment.getName());

    // The ParsedDeployment represents the deployment, the decision tables, and the DMN
    // resource, parse, and model associated with each decision table.
    ParsedDeployment parsedDeployment = parsedDeploymentBuilderFactory.getBuilderForDeploymentAndSettings(deployment, deploymentSettings).build();

    dmnDeploymentHelper.verifyDecisionTablesDoNotShareKeys(parsedDeployment.getAllDecisionTables());

    dmnDeploymentHelper.copyDeploymentValuesToDecisionTables(parsedDeployment.getDeployment(), parsedDeployment.getAllDecisionTables());
    dmnDeploymentHelper.setResourceNamesOnDecisionTables(parsedDeployment);

    if (deployment.isNew()) {
      Map<DecisionTableEntity, DecisionTableEntity> mapOfNewDecisionTableToPreviousVersion = getPreviousVersionsOfDecisionTables(parsedDeployment);
      setDecisionTableVersionsAndIds(parsedDeployment, mapOfNewDecisionTableToPreviousVersion);
      persistDecisionTables(parsedDeployment);
    } else {
      makeDecisionTablesConsistentWithPersistedVersions(parsedDeployment);
    }

    cachingAndArtifactsManager.updateCachingAndArtifacts(parsedDeployment);
  }

  /**
   * Constructs a map from new DecisionTableEntities to the previous version by key and tenant. If no previous version exists, no map entry is created.
   */
  protected Map<DecisionTableEntity, DecisionTableEntity> getPreviousVersionsOfDecisionTables(ParsedDeployment parsedDeployment) {

    Map<DecisionTableEntity, DecisionTableEntity> result = new LinkedHashMap<DecisionTableEntity, DecisionTableEntity>();

    for (DecisionTableEntity newDefinition : parsedDeployment.getAllDecisionTables()) {
      DecisionTableEntity existingDefinition = dmnDeploymentHelper.getMostRecentVersionOfDecisionTable(newDefinition);

      if (existingDefinition != null) {
        result.put(newDefinition, existingDefinition);
      }
    }

    return result;
  }

  /**
   * Sets the version on each decision table entity, and the identifier. If the map contains an older version for a decision table, then the version is set to that older entity's version plus one;
   * otherwise it is set to 1.
   */
  protected void setDecisionTableVersionsAndIds(ParsedDeployment parsedDeployment, Map<DecisionTableEntity, DecisionTableEntity> mapNewToOldDecisionTables) {

    for (DecisionTableEntity decisionTable : parsedDeployment.getAllDecisionTables()) {
      int version = 1;

      DecisionTableEntity latest = mapNewToOldDecisionTables.get(decisionTable);
      if (latest != null) {
        version = latest.getVersion() + 1;
      }

      decisionTable.setVersion(version);
      decisionTable.setId(idGenerator.getNextId());
    }
  }

  /**
   * Saves each decision table. It is assumed that the deployment is new, the definitions have never been saved before, and that they have all their values properly set up.
   */
  protected void persistDecisionTables(ParsedDeployment parsedDeployment) {
    CommandContext commandContext = Context.getCommandContext();
    DecisionTableEntityManager decisionTableEntityManager = commandContext.getDecisionTableEntityManager();

    for (DecisionTableEntity decisionTable : parsedDeployment.getAllDecisionTables()) {
      decisionTableEntityManager.insert(decisionTable);
    }
  }

  /**
   * Loads the persisted version of each decision table and set values on the in-memory version to be consistent.
   */
  protected void makeDecisionTablesConsistentWithPersistedVersions(ParsedDeployment parsedDeployment) {
    for (DecisionTableEntity decisionTable : parsedDeployment.getAllDecisionTables()) {
      DecisionTableEntity persistedDecisionTable = dmnDeploymentHelper.getPersistedInstanceOfDecisionTable(decisionTable);

      if (persistedDecisionTable != null) {
        decisionTable.setId(persistedDecisionTable.getId());
        decisionTable.setVersion(persistedDecisionTable.getVersion());
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

  public DmnDeploymentHelper getDmnDeploymentHelper() {
    return dmnDeploymentHelper;
  }

  public void setDmnDeploymentHelper(DmnDeploymentHelper dmnDeploymentHelper) {
    this.dmnDeploymentHelper = dmnDeploymentHelper;
  }

  public CachingAndArtifactsManager getCachingAndArtifcatsManager() {
    return cachingAndArtifactsManager;
  }

  public void setCachingAndArtifactsManager(CachingAndArtifactsManager manager) {
    this.cachingAndArtifactsManager = manager;
  }
}
