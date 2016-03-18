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
public class DecisionTable extends Expression {

    protected List<InputClause> inputs = new ArrayList<InputClause>();
    protected List<OutputClause> outputs = new ArrayList<OutputClause>();
    protected List<DecisionRule> rules = new ArrayList<DecisionRule>();
    protected HitPolicy hitPolicy;
    protected BuiltinAggregator aggregation;
    protected DecisionTableOrientation preferredOrientation;
    protected String outputLabel;

    public List<InputClause> getInputs() {
        return inputs;
    }

    public void addInput(InputClause input) {
        this.inputs.add(input);
    }

    public List<OutputClause> getOutputs() {
        return outputs;
    }

    public void addOutput(OutputClause output) {
        this.outputs.add(output);
    }

    public List<DecisionRule> getRules() {
        return rules;
    }

    public void addRule(DecisionRule rule) {
        this.rules.add(rule);
    }

    public HitPolicy getHitPolicy() {
        return hitPolicy;
    }

    public void setHitPolicy(HitPolicy hitPolicy) {
        this.hitPolicy = hitPolicy;
    }

    public BuiltinAggregator getAggregation() {
        return aggregation;
    }

    public void setAggregation(BuiltinAggregator aggregation) {
        this.aggregation = aggregation;
    }

    public DecisionTableOrientation getPreferredOrientation() {
        return preferredOrientation;
    }

    public void setPreferredOrientation(DecisionTableOrientation preferredOrientation) {
        this.preferredOrientation = preferredOrientation;
    }

    public String getOutputLabel() {
        return outputLabel;
    }

    public void setOutputLabel(String outputLabel) {
        this.outputLabel = outputLabel;
    }
}
