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

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.activiti.dmn.engine.impl.DeploymentSettings;
import org.activiti.dmn.engine.impl.context.Context;
import org.activiti.dmn.engine.impl.parser.DmnParse;
import org.activiti.dmn.engine.impl.parser.DmnParseFactory;
import org.activiti.dmn.engine.impl.persistence.entity.DecisionTableEntity;
import org.activiti.dmn.engine.impl.persistence.entity.DmnDeploymentEntity;
import org.activiti.dmn.engine.impl.persistence.entity.ResourceEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParsedDeploymentBuilder {

  private static final Logger log = LoggerFactory.getLogger(ParsedDeploymentBuilder.class);
  
  public static final String[] DMN_RESOURCE_SUFFIXES = new String[] { "dmn" };

  protected DmnDeploymentEntity deployment;
  protected DmnParseFactory dmnParseFactory;
  protected Map<String, Object> deploymentSettings;

  public ParsedDeploymentBuilder(DmnDeploymentEntity deployment, DmnParseFactory dmnParseFactory, Map<String, Object> deploymentSettings) {
    this.deployment = deployment;
    this.dmnParseFactory = dmnParseFactory;
    this.deploymentSettings = deploymentSettings;
  }

  public ParsedDeployment build() {
    List<DecisionTableEntity> decisionTables = new ArrayList<DecisionTableEntity>();
    Map<DecisionTableEntity, DmnParse> decisionTablesToDmnParseMap = new LinkedHashMap<DecisionTableEntity, DmnParse>();
    Map<DecisionTableEntity, ResourceEntity> decisionTablesToResourceMap = new LinkedHashMap<DecisionTableEntity, ResourceEntity>();

    for (ResourceEntity resource : deployment.getResources().values()) {
      if (isDmnResource(resource.getName())) {
        log.debug("Processing DMN resource {}", resource.getName());
        DmnParse parse = createDmnParseFromResource(resource);
        for (DecisionTableEntity decisionTable : parse.getDecisionTables()) {
          decisionTables.add(decisionTable);
          decisionTablesToDmnParseMap.put(decisionTable, parse);
          decisionTablesToResourceMap.put(decisionTable, resource);
        }
      }
    }

    return new ParsedDeployment(deployment, decisionTables, decisionTablesToDmnParseMap, decisionTablesToResourceMap);
  }

  protected DmnParse createDmnParseFromResource(ResourceEntity resource) {
    String resourceName = resource.getName();
    ByteArrayInputStream inputStream = new ByteArrayInputStream(resource.getBytes());

    DmnParse dmnParse = dmnParseFactory.createParse()
        .sourceInputStream(inputStream)
        .setSourceSystemId(resourceName)
        .deployment(deployment)
        .name(resourceName);

    if (deploymentSettings != null) {

      // Schema validation if needed
      if (deploymentSettings.containsKey(DeploymentSettings.IS_DMN_XSD_VALIDATION_ENABLED)) {
        dmnParse.setValidateSchema((Boolean) deploymentSettings.get(DeploymentSettings.IS_DMN_XSD_VALIDATION_ENABLED));
      }

    } else {
      // On redeploy, we assume it is validated at the first deploy
      dmnParse.setValidateSchema(false);
    }
    
    dmnParse.execute(Context.getDmnEngineConfiguration());
    return dmnParse;
  }

  protected boolean isDmnResource(String resourceName) {
    for (String suffix : DMN_RESOURCE_SUFFIXES) {
      if (resourceName.endsWith(suffix)) {
        return true;
      }
    }

    return false;
  }

}