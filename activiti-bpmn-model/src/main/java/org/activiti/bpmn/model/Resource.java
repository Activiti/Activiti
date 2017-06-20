package org.activiti.bpmn.model;

/**
 * The Resource class is used to specify resources that can be referenced by
 * Activities. These Resources can be Human Resources as well as any other
 * resource assigned to Activities during Process execution time.
 *
 */
public class Resource extends BaseElement {

  protected String name;

  public Resource(String resourceId, String resourceName) {
    super();
    setId(resourceId);
    setName(resourceName);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public BaseElement clone() {
    return new Resource(getId(), getName());
  }
}
