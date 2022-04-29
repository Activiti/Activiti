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

public class AdhocSubProcess extends SubProcess {

  public static final String ORDERING_PARALLEL = "Parallel";
  public static final String ORDERING_SEQUENTIALL = "Sequential";

  protected String completionCondition;
  protected String ordering = ORDERING_PARALLEL;
  protected boolean cancelRemainingInstances = true;

  public String getCompletionCondition() {
    return completionCondition;
  }

  public void setCompletionCondition(String completionCondition) {
    this.completionCondition = completionCondition;
  }

  public String getOrdering() {
    return ordering;
  }

  public void setOrdering(String ordering) {
    this.ordering = ordering;
  }

  public boolean hasParallelOrdering() {
    return !ORDERING_SEQUENTIALL.equals(ordering);
  }

  public boolean hasSequentialOrdering() {
    return ORDERING_SEQUENTIALL.equals(ordering);
  }

  public boolean isCancelRemainingInstances() {
    return cancelRemainingInstances;
  }

  public void setCancelRemainingInstances(boolean cancelRemainingInstances) {
    this.cancelRemainingInstances = cancelRemainingInstances;
  }
}
