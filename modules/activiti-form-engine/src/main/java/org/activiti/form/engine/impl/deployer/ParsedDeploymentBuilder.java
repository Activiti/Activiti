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

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.activiti.form.engine.impl.context.Context;
import org.activiti.form.engine.impl.parser.FormParse;
import org.activiti.form.engine.impl.parser.FormParseFactory;
import org.activiti.form.engine.impl.persistence.entity.FormDeploymentEntity;
import org.activiti.form.engine.impl.persistence.entity.FormEntity;
import org.activiti.form.engine.impl.persistence.entity.ResourceEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParsedDeploymentBuilder {

  private static final Logger log = LoggerFactory.getLogger(ParsedDeploymentBuilder.class);
  
  public static final String[] FORM_RESOURCE_SUFFIXES = new String[] { "form" };

  protected FormDeploymentEntity deployment;
  protected FormParseFactory formParseFactory;

  public ParsedDeploymentBuilder(FormDeploymentEntity deployment, FormParseFactory formParseFactory) {
    this.deployment = deployment;
    this.formParseFactory = formParseFactory;
  }

  public ParsedDeployment build() {
    List<FormEntity> forms = new ArrayList<FormEntity>();
    Map<FormEntity, FormParse> formToParseMap = new LinkedHashMap<FormEntity, FormParse>();
    Map<FormEntity, ResourceEntity> formToResourceMap = new LinkedHashMap<FormEntity, ResourceEntity>();

    for (ResourceEntity resource : deployment.getResources().values()) {
      if (isFormResource(resource.getName())) {
        log.debug("Processing Form resource {}", resource.getName());
        FormParse parse = createFormParseFromResource(resource);
        for (FormEntity form : parse.getForms()) {
          forms.add(form);
          formToParseMap.put(form, parse);
          formToResourceMap.put(form, resource);
        }
      }
    }

    return new ParsedDeployment(deployment, forms, formToParseMap, formToResourceMap);
  }

  protected FormParse createFormParseFromResource(ResourceEntity resource) {
    String resourceName = resource.getName();
    ByteArrayInputStream inputStream = new ByteArrayInputStream(resource.getBytes());

    FormParse formParse = formParseFactory.createParse()
        .sourceInputStream(inputStream)
        .setSourceSystemId(resourceName)
        .deployment(deployment)
        .name(resourceName);
    
    formParse.execute(Context.getFormEngineConfiguration());
    return formParse;
  }

  protected boolean isFormResource(String resourceName) {
    for (String suffix : FORM_RESOURCE_SUFFIXES) {
      if (resourceName.endsWith(suffix)) {
        return true;
      }
    }

    return false;
  }

}