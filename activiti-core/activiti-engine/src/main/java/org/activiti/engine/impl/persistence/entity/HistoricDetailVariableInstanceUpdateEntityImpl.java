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


package org.activiti.engine.impl.persistence.entity;

import org.activiti.engine.impl.variable.VariableType;
import org.apache.commons.lang3.StringUtils;

/**


 */
public class HistoricDetailVariableInstanceUpdateEntityImpl extends HistoricDetailEntityImpl implements HistoricDetailVariableInstanceUpdateEntity {

  private static final long serialVersionUID = 1L;

  protected int revision;

  protected String name;
  protected VariableType variableType;

  protected Long longValue;
  protected Double doubleValue;
  protected String textValue;
  protected String textValue2;
  protected ByteArrayRef byteArrayRef;

  protected Object cachedValue;

  public HistoricDetailVariableInstanceUpdateEntityImpl() {
    this.detailType = "VariableUpdate";
  }

  public Object getPersistentState() {
    // HistoricDetailVariableInstanceUpdateEntity is immutable, so always
    // the same object is returned
    return HistoricDetailVariableInstanceUpdateEntityImpl.class;
  }

  public Object getValue() {
    if (!variableType.isCachable() || cachedValue == null) {
      cachedValue = variableType.getValue(this);
    }
    return cachedValue;
  }

  public String getVariableTypeName() {
    return (variableType != null ? variableType.getTypeName() : null);
  }

  public int getRevisionNext() {
    return revision + 1;
  }

  // byte array value /////////////////////////////////////////////////////////

  @Override
  public byte[] getBytes() {
    if (byteArrayRef != null) {
      return byteArrayRef.getBytes();
    }
    return null;
  }

  public ByteArrayRef getByteArrayRef() {
    return byteArrayRef;
  }

  public void setBytes(byte[] bytes) {
    String byteArrayName = "hist.detail.var-" + name;
    if (byteArrayRef == null) {
      byteArrayRef = new ByteArrayRef();
    }
    byteArrayRef.setValue(byteArrayName, bytes);
  }

  // getters and setters ////////////////////////////////////////////////////////

  public int getRevision() {
    return revision;
  }

  public void setRevision(int revision) {
    this.revision = revision;
  }

  public String getVariableName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public VariableType getVariableType() {
    return variableType;
  }

  public void setVariableType(VariableType variableType) {
    this.variableType = variableType;
  }

  public Long getLongValue() {
    return longValue;
  }

  public void setLongValue(Long longValue) {
    this.longValue = longValue;
  }

  public Double getDoubleValue() {
    return doubleValue;
  }

  public void setDoubleValue(Double doubleValue) {
    this.doubleValue = doubleValue;
  }

  public String getTextValue() {
    return textValue;
  }

  public void setTextValue(String textValue) {
    this.textValue = textValue;
  }

  public String getTextValue2() {
    return textValue2;
  }

  public void setTextValue2(String textValue2) {
    this.textValue2 = textValue2;
  }

  public Object getCachedValue() {
    return cachedValue;
  }

  public void setCachedValue(Object cachedValue) {
    this.cachedValue = cachedValue;
  }

  // common methods ///////////////////////////////////////////////////////////////

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("HistoricDetailVariableInstanceUpdateEntity[");
    sb.append("id=").append(id);
    sb.append(", name=").append(name);
    sb.append(", type=").append(variableType != null ? variableType.getTypeName() : "null");
    if (longValue != null) {
      sb.append(", longValue=").append(longValue);
    }
    if (doubleValue != null) {
      sb.append(", doubleValue=").append(doubleValue);
    }
    if (textValue != null) {
      sb.append(", textValue=").append(StringUtils.abbreviate(textValue, 40));
    }
    if (textValue2 != null) {
      sb.append(", textValue2=").append(StringUtils.abbreviate(textValue2, 40));
    }
    if (byteArrayRef != null && byteArrayRef.getId() != null) {
      sb.append(", byteArrayValueId=").append(byteArrayRef.getId());
    }
    sb.append("]");
    return sb.toString();
  }

}
