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




package org.activiti.standalone.cfg;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.query.QueryProperty;


public class AttachmentQueryProperty implements QueryProperty {

  private static final long serialVersionUID = 1L;

  private static final Map<String, AttachmentQueryProperty> properties = new HashMap<String, AttachmentQueryProperty>();

  public static final AttachmentQueryProperty ATTACHMENT_ID = new AttachmentQueryProperty("RES.ID_");
  public static final AttachmentQueryProperty NAME = new AttachmentQueryProperty("RES.NAME_");
  public static final AttachmentQueryProperty DESCRIPTION = new AttachmentQueryProperty("RES.DESCRIPTION_");
  public static final AttachmentQueryProperty USER = new AttachmentQueryProperty("RES.USER_ID_");
  public static final AttachmentQueryProperty CREATE_TIME = new AttachmentQueryProperty("RES.TIME_");
  public static final AttachmentQueryProperty PROCESS_INSTANCE_ID = new AttachmentQueryProperty("RES.PROC_INST_ID_");
  public static final AttachmentQueryProperty TASK_ID = new AttachmentQueryProperty("RES.TASK_ID_");
  public static final AttachmentQueryProperty TYPE = new AttachmentQueryProperty("RES.TYPE_");
  public static final AttachmentQueryProperty URL = new AttachmentQueryProperty("RES.URL_");

  private String name;

  public AttachmentQueryProperty(String name) {
    this.name = name;
    properties.put(name, this);
  }

  public String getName() {
    return name;
  }

  public static AttachmentQueryProperty findByName(String propertyName) {
    return properties.get(propertyName);
  }

}
