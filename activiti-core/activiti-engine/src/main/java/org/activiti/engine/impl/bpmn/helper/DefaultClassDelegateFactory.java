/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
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

package org.activiti.engine.impl.bpmn.helper;

import java.util.List;

import org.activiti.bpmn.model.MapExceptionEntry;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.bpmn.parser.FieldDeclaration;

public class DefaultClassDelegateFactory implements ClassDelegateFactory {
  public ClassDelegate create(String id, String className, List<FieldDeclaration> fieldDeclarations,
      Expression skipExpression, List<MapExceptionEntry> mapExceptions) {
    return new ClassDelegate(id, className, fieldDeclarations, skipExpression, mapExceptions);
  }

  public ClassDelegate create(String className, List<FieldDeclaration> fieldDeclarations) {
    return new ClassDelegate(className, fieldDeclarations);
  }
}
