/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.validation.validator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.activiti.validation.ValidationError;
import org.apache.commons.text.StringSubstitutor;

public class ValidationErrorDecorator {

  public static final String PARAM_PREFIX = "{{";
  public static final String PARAM_SUFFIX = "}}";

  private Map<String, ErrorMessageDefinition> errorMessages;

  public ValidationErrorDecorator() {
    this.init();
  }

  public void init() {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      TypeReference<Map<String, ErrorMessageDefinition>> typeReference = new TypeReference<>() {
      };
      InputStream inputStream = getClass().getResourceAsStream("/process-validation-messages.json");
      this.errorMessages = objectMapper.readValue(inputStream, typeReference);
    } catch (IOException e) {
      throw new RuntimeException("Failed to load error messages", e);
    }
  }

  public void decorate(
      ValidationError error
  ) {
    error.setProblem(
        resolveMessage(errorMessages.get(error.getKey()).getProblem(), error.getParams()));
    error.setDefaultDescription(
        resolveMessage(errorMessages.get(error.getKey()).getDescription(), error.getParams()));
  }

  public String resolveMessage(String message, Map<String, String> params) {
    StringSubstitutor sub = new StringSubstitutor(params, PARAM_PREFIX, PARAM_SUFFIX);
    return sub.replace(message);
  }
}
