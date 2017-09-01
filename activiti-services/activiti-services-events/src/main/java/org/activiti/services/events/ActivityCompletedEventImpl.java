/*
 * Copyright 2017 Alfresco and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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
 *
 */

package org.activiti.services.events;

public class ActivityCompletedEventImpl extends AbstractProcessEngineEvent implements ActivityCompletedEvent {

    protected String activityId;
    protected String activityName;
    protected String activityType;

    public ActivityCompletedEventImpl() {
    }

    public ActivityCompletedEventImpl(String applicationName,
                                      String executionId,
                                      String processDefinitionId,
                                      String processInstanceId,
                                      String activityId,
                                      String activityName,
                                      String activityType) {
        super(applicationName,
              executionId,
              processDefinitionId,
              processInstanceId);
        this.activityId = activityId;
        this.activityName = activityName;
        this.activityType = activityType;
    }

    @Override
    public String getActivityId() {
        return activityId;
    }

    @Override
    public String getActivityName() {
        return activityName;
    }

    @Override
    public String getActivityType() {
        return activityType;
    }

    @Override
    public String getEventType() {
        return "ActivityCompletedEvent";
    }
}
