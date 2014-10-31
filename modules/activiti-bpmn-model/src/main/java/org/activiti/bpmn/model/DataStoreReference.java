package org.activiti.bpmn.model;

public class DataStoreReference extends FlowElement {
  
  protected String dataState;
  protected String itemSubjectRef;
  protected String dataStoreRef;
  
  public String getDataState() {
    return dataState;
  }

  public void setDataState(String dataState) {
    this.dataState = dataState;
  }

  public String getItemSubjectRef() {
    return itemSubjectRef;
  }

  public void setItemSubjectRef(String itemSubjectRef) {
    this.itemSubjectRef = itemSubjectRef;
  }

  public String getDataStoreRef() {
    return dataStoreRef;
  }

  public void setDataStoreRef(String dataStoreRef) {
    this.dataStoreRef = dataStoreRef;
  }

  public DataStoreReference clone() {
    DataStoreReference clone = new DataStoreReference();
    clone.setValues(this);
    return clone;
  }
  
  public void setValues(DataStoreReference otherElement) {
    super.setValues(otherElement);
    setDataState(otherElement.getDataState());
    setItemSubjectRef(otherElement.getItemSubjectRef());
    setDataStoreRef(otherElement.getDataStoreRef());
  }

}
