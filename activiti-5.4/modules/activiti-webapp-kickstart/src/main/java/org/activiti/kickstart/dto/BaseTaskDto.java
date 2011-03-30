package org.activiti.kickstart.dto;

import org.activiti.kickstart.bpmn20.model.FlowElement;

public abstract class BaseTaskDto {

  protected String id;

  protected String name;

  protected String description;

  protected boolean startWithPrevious;

  public BaseTaskDto() {
    super();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean getStartsWithPrevious() {
    return startWithPrevious;
  }

  public void setStartWithPrevious(boolean startWithPrevious) {
    this.startWithPrevious = startWithPrevious;
  }

  /**
   * Creates the Flow element for this BaseTaskDto. Every Dto knows about its
   * corresponding FlowElement;
   * 
   * @return A instance of {@link FlowElement} which represents this Dto in BPMN
   *         2.0
   */
  public abstract FlowElement createFlowElement();

}
