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
package org.activiti.dmn.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Yvo Swillens
 */
public class RuleExecutionAuditContainer {

  protected Date startTime;
  protected Date endTime;

  protected List<ExpressionExecution> conditionResults = new ArrayList<ExpressionExecution>();
  protected List<ExpressionExecution> conclusionResults = new ArrayList<ExpressionExecution>();

  public RuleExecutionAuditContainer() {
    this.startTime = new Date();
  }

  public void addConditionResult(ExpressionExecution expressionExecution) {
    conditionResults.add(expressionExecution);
  }

  public void addConclusionResult(ExpressionExecution executionResult) {
    conclusionResults.add(executionResult);
  }

  public void markRuleEnd() {
    endTime = new Date();
  }

  public Date getStartTime() {
    return startTime;
  }

  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  public Date getEndTime() {
    return endTime;
  }

  public void setEndTime(Date endTime) {
    this.endTime = endTime;
  }

  public List<ExpressionExecution> getConditionResults() {
    return conditionResults;
  }

  public void setConditionResults(List<ExpressionExecution> conditionResults) {
    this.conditionResults = conditionResults;
  }

  public List<ExpressionExecution> getConclusionResults() {
    return conclusionResults;
  }

  public void setConclusionResults(List<ExpressionExecution> conclusionResults) {
    this.conclusionResults = conclusionResults;
  }
}
