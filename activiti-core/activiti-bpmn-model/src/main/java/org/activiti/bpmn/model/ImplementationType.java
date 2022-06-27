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
package org.activiti.bpmn.model;

public class ImplementationType {

  public static String IMPLEMENTATION_TYPE_CLASS = "class";
  public static String IMPLEMENTATION_TYPE_EXPRESSION = "expression";
  public static String IMPLEMENTATION_TYPE_DELEGATEEXPRESSION = "delegateExpression";
  public static String IMPLEMENTATION_TYPE_INSTANCE = "instance";
  public static String IMPLEMENTATION_TYPE_THROW_SIGNAL_EVENT = "throwSignalEvent";
  public static String IMPLEMENTATION_TYPE_THROW_GLOBAL_SIGNAL_EVENT = "throwGlobalSignalEvent";
  public static String IMPLEMENTATION_TYPE_THROW_MESSAGE_EVENT = "throwMessageEvent";
  public static String IMPLEMENTATION_TYPE_THROW_ERROR_EVENT = "throwErrorEvent";
  public static String IMPLEMENTATION_TYPE_WEBSERVICE = "##WebService";

  public static String IMPLEMENTATION_TYPE_INVALID_THROW_EVENT = "invalidThrowEvent";

}
