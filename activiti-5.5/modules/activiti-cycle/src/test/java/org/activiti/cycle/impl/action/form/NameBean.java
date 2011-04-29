package org.activiti.cycle.impl.action.form;

import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleContextType;

/**
 * A request-scoped cycle component for testing the FormParser
 * 
 * @author daniel.meyer@camunda.com
 */
@CycleComponent(name = "nameBean", context = CycleContextType.REQUEST)
public class NameBean {

  private String firstname = "Kermit";

  private String lastname = "The Frog";

  public String getFirstname() {
    return firstname;
  }

  public void setFirstname(String firstname) {
    this.firstname = firstname;
  }

  public String getLastname() {
    return lastname;
  }

  public void setLastname(String lastname) {
    this.lastname = lastname;
  }

}
