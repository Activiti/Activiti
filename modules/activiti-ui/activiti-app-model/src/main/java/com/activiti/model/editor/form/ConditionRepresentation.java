/**
 * Activiti app component part of the Activiti project
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.activiti.model.editor.form;

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
