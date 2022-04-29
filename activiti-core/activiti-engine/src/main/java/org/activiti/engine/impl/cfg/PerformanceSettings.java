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

package org.activiti.engine.impl.cfg;


public class PerformanceSettings {

  /**
   * Experimental setting: if true, whenever an execution is fetched from the data store,
   * the whole execution tree is fetched in the same roundtrip.
   *
   * Less roundtrips to the database outweighs doing many, smaller fetches and often
   * multiple executions from the same tree are needed anyway when executing process instances.
   */
  protected boolean enableEagerExecutionTreeFetching;

  /**
   * Experimental setting: keeps a count on each execution that holds
   * how many variables, jobs, tasks, event subscriptions, etc. the execution has.
   *
   * This makes the delete more performant as a query is not needed anymore
   * to check if there is related data. However, maintaining the count
   * does mean more updates to the execution and potentially more optimistic locking opportunities.
   * Typically keeping the counts lead to better performance as deletes are a large part of the
   * execution tree maintenance.
   */
  protected boolean enableExecutionRelationshipCounts;

  /**
   * If false, no check will be done on boot.
   */
  protected boolean validateExecutionRelationshipCountConfigOnBoot = true;

  /**
   * Experimental setting: in certain places in the engine (execution/process instance/historic process instance/
   * tasks/data objects) localization is supported. When this setting is false,
   * localization is completely disabled, which gives a small performance gain.
   */
  protected boolean enableLocalization = true;

  public boolean isEnableEagerExecutionTreeFetching() {
    return enableEagerExecutionTreeFetching;
  }

  public void setEnableEagerExecutionTreeFetching(boolean enableEagerExecutionTreeFetching) {
    this.enableEagerExecutionTreeFetching = enableEagerExecutionTreeFetching;
  }

  public boolean isEnableExecutionRelationshipCounts() {
    return enableExecutionRelationshipCounts;
  }

  public void setEnableExecutionRelationshipCounts(boolean enableExecutionRelationshipCounts) {
    this.enableExecutionRelationshipCounts = enableExecutionRelationshipCounts;
  }

  public boolean isValidateExecutionRelationshipCountConfigOnBoot() {
    return validateExecutionRelationshipCountConfigOnBoot;
  }

  public void setValidateExecutionRelationshipCountConfigOnBoot(boolean validateExecutionRelationshipCountConfigOnBoot) {
    this.validateExecutionRelationshipCountConfigOnBoot = validateExecutionRelationshipCountConfigOnBoot;
  }

  public boolean isEnableLocalization() {
    return enableLocalization;
  }

  public void setEnableLocalization(boolean enableLocalization) {
    this.enableLocalization = enableLocalization;
  }

}
