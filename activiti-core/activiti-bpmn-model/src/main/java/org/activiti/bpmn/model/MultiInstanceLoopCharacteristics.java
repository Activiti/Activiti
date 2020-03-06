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
package org.activiti.bpmn.model;

public class MultiInstanceLoopCharacteristics extends BaseElement {

  private String inputDataItem;
  private String loopCardinality;
  private String completionCondition;
  private String elementVariable;
  private String elementIndexVariable;
  private boolean sequential;
  private String loopDataOutputRef;
  private String outputDataItem;

  public String getInputDataItem() {
    return inputDataItem;
  }

  public void setInputDataItem(String inputDataItem) {
    this.inputDataItem = inputDataItem;
  }

  public String getLoopCardinality() {
    return loopCardinality;
  }

  public void setLoopCardinality(String loopCardinality) {
    this.loopCardinality = loopCardinality;
  }

  public String getCompletionCondition() {
    return completionCondition;
  }

  public void setCompletionCondition(String completionCondition) {
    this.completionCondition = completionCondition;
  }

  public String getElementVariable() {
    return elementVariable;
  }

  public void setElementVariable(String elementVariable) {
    this.elementVariable = elementVariable;
  }

  public String getElementIndexVariable() {
    return elementIndexVariable;
  }

  public void setElementIndexVariable(String elementIndexVariable) {
    this.elementIndexVariable = elementIndexVariable;
  }

  public boolean isSequential() {
    return sequential;
  }

  public void setSequential(boolean sequential) {
    this.sequential = sequential;
  }

    public String getLoopDataOutputRef() {
        return loopDataOutputRef;
    }

    public void setLoopDataOutputRef(String loopDataOutputRef) {
        this.loopDataOutputRef = loopDataOutputRef;
    }

    public String getOutputDataItem() {
        return outputDataItem;
    }

    public void setOutputDataItem(String outputDataItem) {
        this.outputDataItem = outputDataItem;
    }

    public MultiInstanceLoopCharacteristics clone() {
    MultiInstanceLoopCharacteristics clone = new MultiInstanceLoopCharacteristics();
    clone.setValues(this);
    return clone;
  }

  public void setValues(MultiInstanceLoopCharacteristics otherLoopCharacteristics) {
    setInputDataItem(otherLoopCharacteristics.getInputDataItem());
    setLoopCardinality(otherLoopCharacteristics.getLoopCardinality());
    setCompletionCondition(otherLoopCharacteristics.getCompletionCondition());
    setElementVariable(otherLoopCharacteristics.getElementVariable());
    setElementIndexVariable(otherLoopCharacteristics.getElementIndexVariable());
    setSequential(otherLoopCharacteristics.isSequential());
    setLoopDataOutputRef(otherLoopCharacteristics.getLoopDataOutputRef());
    setOutputDataItem(otherLoopCharacteristics.getOutputDataItem());
  }
}
