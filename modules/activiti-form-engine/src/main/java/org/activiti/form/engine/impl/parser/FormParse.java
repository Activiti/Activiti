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
package org.activiti.form.engine.impl.parser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.activiti.editor.form.converter.FormJsonConverter;
import org.activiti.form.engine.ActivitiFormException;
import org.activiti.form.engine.FormEngineConfiguration;
import org.activiti.form.engine.impl.context.Context;
import org.activiti.form.engine.impl.io.InputStreamSource;
import org.activiti.form.engine.impl.io.ResourceStreamSource;
import org.activiti.form.engine.impl.io.StreamSource;
import org.activiti.form.engine.impl.io.StringStreamSource;
import org.activiti.form.engine.impl.io.UrlStreamSource;
import org.activiti.form.engine.impl.persistence.entity.FormDeploymentEntity;
import org.activiti.form.engine.impl.persistence.entity.FormEntity;
import org.activiti.form.model.FormDefinition;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Specific parsing of one form json file.
 * 
 * @author Tijs Rademakers
 * @author Joram Barrez
 */
public class FormParse {

  protected static final Logger LOGGER = LoggerFactory.getLogger(FormParse.class);

  protected String name;

  protected boolean validateSchema = true;

  protected StreamSource streamSource;
  protected String sourceSystemId;

  protected FormDefinition formDefinition;

  protected String targetNamespace;

  /** The deployment to which the parsed decision tables will be added. */
  protected FormDeploymentEntity deployment;

  /** The end result of the parsing: a list of decision tables. */
  protected List<FormEntity> forms = new ArrayList<FormEntity>();

  public FormParse deployment(FormDeploymentEntity deployment) {
    this.deployment = deployment;
    return this;
  }

  public FormParse execute(FormEngineConfiguration formEngineConfig) {
    String encoding = formEngineConfig.getXmlEncoding();
    FormJsonConverter converter = new FormJsonConverter();
    
    try {
      InputStreamReader in = null;
      if (encoding != null) {
        in = new InputStreamReader(streamSource.getInputStream(), encoding);
      } else {
        in = new InputStreamReader(streamSource.getInputStream());
      }
    
      String formJson = IOUtils.toString(in);
      formDefinition = converter.convertToForm(formJson, null, 1);
  
      if (formDefinition != null && formDefinition.getFields() != null) {
        FormEntity formEntity = Context.getFormEngineConfiguration().getFormEntityManager().create();
        formEntity.setKey(formDefinition.getKey());
        formEntity.setName(formDefinition.getName());
        formEntity.setResourceName(name);
        formEntity.setDeploymentId(deployment.getId());
        formEntity.setParentDeploymentId(deployment.getParentDeploymentId());
        formEntity.setDescription(formDefinition.getDescription());
        forms.add(formEntity);
      }
    } catch(Exception e) {
      throw new ActivitiFormException("Error parsing form JSON", e);
    }
    return this;
  }

  public FormParse name(String name) {
    this.name = name;
    return this;
  }

  public FormParse sourceInputStream(InputStream inputStream) {
    if (name == null) {
        name("inputStream");
    }
    setStreamSource(new InputStreamSource(inputStream));
    return this;
  }

  public FormParse sourceUrl(URL url) {
    if (name == null) {
      name(url.toString());
    }
    setStreamSource(new UrlStreamSource(url));
    return this;
  }

  public FormParse sourceUrl(String url) {
    try {
      return sourceUrl(new URL(url));
    } catch (MalformedURLException e) {
      throw new ActivitiFormException("malformed url: " + url, e);
    }
  }

  public FormParse sourceResource(String resource) {
    if (name == null) {
      name(resource);
    }
    setStreamSource(new ResourceStreamSource(resource));
    return this;
  }

  public FormParse sourceString(String string) {
    if (name == null) {
      name("string");
    }
    setStreamSource(new StringStreamSource(string));
    return this;
  }

  protected void setStreamSource(StreamSource streamSource) {
    if (this.streamSource != null) {
      throw new ActivitiFormException("invalid: multiple sources " + this.streamSource + " and " + streamSource);
    }
    this.streamSource = streamSource;
  }

  public String getSourceSystemId() {
    return sourceSystemId;
  }

  public FormParse setSourceSystemId(String sourceSystemId) {
    this.sourceSystemId = sourceSystemId;
    return this;
  }

  /*
   * ------------------- GETTERS AND SETTERS -------------------
   */

  public List<FormEntity> getForms() {
    return forms;
  }

  public String getTargetNamespace() {
    return targetNamespace;
  }

  public FormDeploymentEntity getDeployment() {
    return deployment;
  }

  public void setDeployment(FormDeploymentEntity deployment) {
    this.deployment = deployment;
  }

  public FormDefinition getFormDefinition() {
    return formDefinition;
  }

  public void setFormDefinition(FormDefinition formDefinition) {
    this.formDefinition = formDefinition;
  }
}
