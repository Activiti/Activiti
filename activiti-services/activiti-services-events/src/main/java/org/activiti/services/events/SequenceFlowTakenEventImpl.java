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

public class SequenceFlowTakenEventImpl extends AbstractProcessEngineEvent implements SequenceFlowTakenEvent {
    private String sequenceFlowId;
    private String sourceActivityId;
    private String sourceActivityName;
    private String sourceActivityType;
    private String targetActivityId;
    private String targetActivityName;
    private String targetActivityType;

    public SequenceFlowTakenEventImpl(String applicatioName,
                                      String executionId,
                                      String processDefinitionId,
                                      String processInstanceId,
                                      String sequenceFlowId,
                                      String sourceActivityId,
                                      String sourceActivityName,
                                      String sourceActivityType,
                                      String targetActivityId,
                                      String targetActivityName,
                                      String targetActivityType) {
        super(applicatioName,
              executionId,
              processDefinitionId,
              processInstanceId);
        this.sequenceFlowId = sequenceFlowId;
        this.sourceActivityId = sourceActivityId;
        this.sourceActivityName = sourceActivityName;
        this.sourceActivityType = sourceActivityType;
        this.targetActivityId = targetActivityId;
        this.targetActivityName = targetActivityName;
        this.targetActivityType = targetActivityType;
    }

    @Override
    public String getEventType() {
        return "SequenceFlowTakenEvent";
    }

    @Override
    public String getSequenceFlowId() {
        return sequenceFlowId;
    }

    @Override
    public String getSourceActivityId() {
        return sourceActivityId;
    }

    @Override
    public String getSourceActivityName() {
        return sourceActivityName;
    }

    @Override
    public String getSourceActivityType() {
        return sourceActivityType;
    }

    @Override
    public String getTargetActivityId() {
        return targetActivityId;
    }

    @Override
    public String getTargetActivityName() {
        return targetActivityName;
    }

    @Override
    public String getTargetActivityType() {
        return targetActivityType;
    }
}
