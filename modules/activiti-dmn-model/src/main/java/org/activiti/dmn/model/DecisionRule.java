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
package org.activiti.dmn.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yvo Swillens
 */
public class DecisionRule extends DmnElement {

    protected List<RuleInputClauseContainer> inputEntries = new ArrayList<RuleInputClauseContainer>();
    protected List<RuleOutputClauseContainer> outputEntries = new ArrayList<RuleOutputClauseContainer>();

    public List<RuleInputClauseContainer> getInputEntries() {
        return inputEntries;
    }

    public void addInputEntry(RuleInputClauseContainer inputEntry) {
        this.inputEntries.add(inputEntry);
    }
    
    public void setInputEntries(List<RuleInputClauseContainer> inputEntries) {
        this.inputEntries = inputEntries;
    }

    public List<RuleOutputClauseContainer> getOutputEntries() {
        return outputEntries;
    }

    public void addOutputEntry(RuleOutputClauseContainer outputEntry) {
        this.outputEntries.add(outputEntry);
    }
    
    public void setOutputEntries(List<RuleOutputClauseContainer> outputEntries) {
        this.outputEntries = outputEntries;
    }
}
