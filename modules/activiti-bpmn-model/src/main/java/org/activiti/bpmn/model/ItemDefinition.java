package org.activiti.bpmn.model;

public class ItemDefinition extends BaseElement {

  protected String structureRef;
  protected String itemKind;

  public String getStructureRef() {
    return structureRef;
  }

  public void setStructureRef(String structureRef) {
    this.structureRef = structureRef;
  }

  public String getItemKind() {
    return itemKind;
  }

  public void setItemKind(String itemKind) {
    this.itemKind = itemKind;
  }
}
