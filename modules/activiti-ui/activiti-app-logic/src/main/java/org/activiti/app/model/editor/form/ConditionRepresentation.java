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
package org.activiti.app.model.editor.form;

/**
 * @author Joram Barrez
 * @author Yvo Swillens
 */
public class ConditionRepresentation {
	
	// Condition
	private String leftFormFieldId;
	private String leftRestResponseId;
	private String operator;
	private Object rightValue;
	private String rightType;
	private String rightFormFieldId;
	private String rightRestResponseId;

	// Next condition
	private String nextConditionOperator;
	private ConditionRepresentation nextCondition;
	

	public String getLeftFormFieldId() {
		return leftFormFieldId;
	}
	public ConditionRepresentation setLeftFormFieldId(String leftFormFieldId) {
		this.leftFormFieldId = leftFormFieldId;
		return this;
	}
	public String getLeftRestResponseId() {
		return leftRestResponseId;
	}
	public void setLeftRestResponseId(String leftRestResponseId) {
		this.leftRestResponseId = leftRestResponseId;
	}
	public String getOperator() {
		return operator;
	}
	public ConditionRepresentation setOperator(String operator) {
		this.operator = operator;
		return  this;
	}
	public Object getRightValue() {
		return rightValue;
	}
	public ConditionRepresentation setRightValue(Object value) {
		this.rightValue = value;
		return this;
	}
	public String getRightType() {
        return rightType;
    }
    public ConditionRepresentation setRightType(String rightType) {
        this.rightType = rightType;
        return this;
    }
	public String getRightFormFieldId() {
		return rightFormFieldId;
	}
	public ConditionRepresentation setRightFormFieldId(String rightFormFieldId) {
		this.rightFormFieldId = rightFormFieldId;
		return this;
	}
    public String getRightRestResponseId() {
        return rightRestResponseId;
    }
    public void setRightRestResponseId(String rightRestResponseId) {
        this.rightRestResponseId = rightRestResponseId;
    }
	public String getNextConditionOperator() {
		return nextConditionOperator;
	}
	public ConditionRepresentation getNextCondition() {
		return nextCondition;
	}
	public ConditionRepresentation setNextCondition(String operator, ConditionRepresentation nextCondition) {
		this.nextConditionOperator = operator;
		this.nextCondition = nextCondition;
		return this;
	}
}
