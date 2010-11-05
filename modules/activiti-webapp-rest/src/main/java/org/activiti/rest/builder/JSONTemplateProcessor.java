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

package org.activiti.rest.builder;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.processor.AbstractTemplateProcessor;

/**
 * A Template Processor that passes the model on to a registered delegate class
 * of type {@link JSONObjectBuilder}, which will create a {@link JSONObject}.
 * The resulting JSONObject is written to the response.
 * 
 * When no builder is defined for the required template,
 * <code>hasTemplate(template)</code> returns false.
 * 
 * @author Frederik Heremans
 */
public class JSONTemplateProcessor extends AbstractTemplateProcessor {

  private static final String PROCESSOR_EXTENTION = "object";
  private static final String PROCESSOR_EXTENTION_SUFFIX = "." + PROCESSOR_EXTENTION;
  private static final String PROCESSOR_NAME = "JSONObject";
  private static final Integer DEFAULT_INDENT_FACTOR = 2;

  protected String defaultEncoding;
  protected String templateNamePrefix;
  protected ApplicationContext applicationContext;
  protected Integer indentFactor = DEFAULT_INDENT_FACTOR;

  private Map<String, JSONObjectBuilder> objectBuilders = new HashMap<String, JSONObjectBuilder>();

  public boolean hasTemplate(String template) {
    return objectBuilders.containsKey(getTemplateKey(template));
  }

  public void process(String template, Object model, Writer out) {
    JSONObjectBuilder builder = objectBuilders.get(getTemplateKey(template));
    if (builder == null) {
      throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "Template " + template + " is registered on this this processor.");
    }

    try {
      JSONObject resultObject = builder.createJsonObject(model);
      writeJSON(resultObject, out);
    } catch (JSONException jsone) {
      throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "Error while creating JSONObject from model", jsone);
    }
  }

  public void processString(String template, Object model, Writer out) {
    throw new UnsupportedOperationException(this.getClass().getSimpleName() + " does not support processing of template string.");
  }

  @Override
  public void init() {
    initializeObjectBuilders();
  }

  public void reset() {
    initializeObjectBuilders();
  }

  protected void initializeObjectBuilders() {
    if (applicationContext != null) {
      // Search in context for all JSONObjectBuilders
      Map<String, JSONObjectBuilder> buildersFromContext = applicationContext.getBeansOfType(JSONObjectBuilder.class);

      Map<String, JSONObjectBuilder> newBuilders = new HashMap<String, JSONObjectBuilder>();
      for (JSONObjectBuilder builder : buildersFromContext.values()) {
        newBuilders.put(templateNamePrefix + builder.getTemplateName(), builder);
      }
      objectBuilders = newBuilders;
    }
  }

  protected String getTemplateKey(String template) {
    if (template != null) {
      return template.replace(PROCESSOR_EXTENTION_SUFFIX, "");
    }
    return null;
  }

  protected void writeJSON(JSONObject object, Writer out) throws WebScriptException {
    try {
      if (indentFactor == 0) {
        // When no indentation is required, we use JSONObject.write, which is
        // cheaper than toString()
        object.write(out);
      } else {
        out.write(object.toString(indentFactor));
      }
    } catch (JSONException jsone) {
      throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "Error while writing JSONObject", jsone);
    } catch (IOException ioe) {
      throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "Error while writing JSONObject", ioe);
    }
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) {
    super.setApplicationContext(applicationContext);
    this.applicationContext = applicationContext;
  }

  public String getDefaultEncoding() {
    return defaultEncoding;
  }

  public String getName() {
    return PROCESSOR_NAME;
  }

  public String getExtension() {
    return PROCESSOR_EXTENTION;
  }

  public void setDefaultEncoding(String defaultEncoding) {
    this.defaultEncoding = defaultEncoding;
  }

  public void setTemplateNamePrefix(String templateNamePrefix) {
    this.templateNamePrefix = templateNamePrefix;
  }

  public void setIndentFactor(Integer indentFactor) {
    if (indentFactor != null && indentFactor > 0) {
      this.indentFactor = indentFactor;
    } else {
      this.indentFactor = DEFAULT_INDENT_FACTOR;
    }
  }
}
