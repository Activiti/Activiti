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
package org.activiti.kickstart.dto;

import java.util.ArrayList;
import java.util.List;

import org.activiti.kickstart.util.ExpressionUtil;

/**
 * @author Joram Barrez
 */
public class FormDto {

  protected String title;
  protected String description;
  protected boolean required;
  protected List<FormPropertyDto> formProperties = new ArrayList<FormPropertyDto>();

  public String getTitle() {
    return title;
  }
  public void setTitle(String title) {
    this.title = title;
  }
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }
  public List<FormPropertyDto> getFormProperties() {
    return formProperties;
  }
  public void setFormProperties(List<FormPropertyDto> formProperties) {
    this.formProperties = formProperties;
  }
  public boolean isRequired() {
    return required;
  }
  public void setRequired(boolean required) {
    this.required = required;
  }
  public void addFormProperty(FormPropertyDto formProperty) {
    formProperties.add(formProperty);
  }

  public String toString() {
    StringBuilder strb = new StringBuilder();
    strb.append(title + "___");
    strb.append(description + "___");
    for (FormPropertyDto property : formProperties) {
      strb.append(property.getProperty() + ";");
      strb.append(property.getType() + ";");
      strb.append(property.isRequired());
      strb.append("@");
    }
    strb.deleteCharAt(strb.length() - 1);
    return strb.toString();
  }
  
  public String convertToHtml() {
    StringBuilder strb = new StringBuilder();
    strb.append("<h1>" + ExpressionUtil.replaceWhiteSpaces(getTitle()) + "</h1>");
    strb.append("<p>" + ExpressionUtil.replaceWhiteSpaces(getDescription()) + "</p>");
    strb.append("<table>");
    for (FormPropertyDto property : getFormProperties()) {
      strb.append("<tr><label><td>");
      strb.append(property.getProperty() + ":</td><td>");
      if (property.getType().equals("text")) {
        strb.append("<input type='text' name='" + property.getProperty().replace(" ", "") + "' value='' />");
      } else if (property.getType().equals("number")) {
        strb.append("<input type='number' name='" + property.getProperty().replace(" ", "") + "' value='' ");
        strb.append("<input type='hidden' name='" + property.getProperty().replace(" ", "") + "_type' value='Integer' ");
      } else if (property.getType().equals("date")) {
        strb.append("<input type='date' name='" + property.getProperty().replace(" ", "") + "' value='' ");
        strb.append("<input type='hidden' name='" + property.getProperty().replace(" ", "") + "_type' value='Date' ");
      }

      if (property.isRequired()) {
        strb.append("<input type='hidden' name='" + property.getProperty().replace(" ", "") + "_required' value='true' ");
      }
      strb.append("</td></label></tr>");
    }

    strb.append("</table>");
    System.out.println(strb);
    return strb.toString();
  }

  public static FormDto createFromSerialized(String serialized) {
    String[] content = serialized.split("___");

    FormDto form = new FormDto();
    form.setTitle(content[0].trim());
    form.setDescription(content[1].trim());

    String[] propertiesContent = content[2].split("@");
    for (String propertyContent : propertiesContent) {
      content = propertyContent.split(";");
      FormPropertyDto property = new FormPropertyDto();
      property.setProperty(content[0]);
      property.setType(content[1]);
      property.setRequired(new Boolean(content[2]));
      form.addFormProperty(property);
    }

    return form;
  }

}
