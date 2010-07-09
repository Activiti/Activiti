package org.activiti.test.pvm.embedded;

import java.util.logging.Logger;

import org.activiti.impl.definition.ProcessDefinitionImpl;
import org.activiti.impl.embeddedstate.SimpleEmbeddedState;
import org.activiti.pvm.ActivityExecution;
import org.activiti.pvm.EventActivityBehavior;
import org.activiti.pvm.ProcessDefinitionBuilder;

public class EmployeeCareerProcess extends SimpleEmbeddedState {
  
  private static final Logger LOG = Logger.getLogger(EmployeeCareerProcess.class.getName());
  
  protected Employee employee;
  
  public EmployeeCareerProcess(Employee employee) {
    this.employee = employee;
  }
  
  @Override
  public void start() {
    super.start("employee", employee);
  }
  
  
  // TODO: I don't like this ...
  @Override
  public void deserializeExecutionTree() {
    super.deserializeExecutionTree();
    // The deserialized process instance doesn't have any variables set, so we need to add it
    embeddedProcessInstance.setVariable("employee", employee);
  }

  protected ProcessDefinitionImpl getProcessDefinition() {
    return (ProcessDefinitionImpl) ProcessDefinitionBuilder
    .createProcessDefinitionBuilder()
      .createActivity("junior developer")
        .behavior(new JuniorDeveloperState())
        .initial()
        .transition("senior developer", "enoughYears")
        .transition("young potential", "birdWithGoldenEggs")
      .endActivity()
      .createActivity("young potential")
        .behavior(new YoungPotentialState())
      .endActivity()
      .createActivity("senior developer")
        .behavior(new SeniorDeveloperState())
        .transition("project lead", "enoughExperience")
      .endActivity()
      .createActivity("project lead")
        .behavior(new ProjectLeadState())
        .transition("CTO", "marryCeoDaughter")
      .endActivity()
      .createActivity("manager")
        .behavior(new ManagerState())
      .endActivity()
      .createActivity("CTO")
        .behavior(new CtoState())
      .endActivity()
    .build();
  }
    
  static abstract class CareerState implements EventActivityBehavior {

    public void execute(ActivityExecution execution) throws Exception {
      LOG.info("Employee " + getEmployee(execution).getName() + " is now a " + execution.getActivity().getId());
      changeWage(getEmployee(execution), getWage());
    }
    
    protected Employee getEmployee(ActivityExecution execution) {
      return (Employee) execution.getVariable("employee");
    }
    
    protected abstract double getWage();
    
    protected void changeWage(Employee employee, double wage) {
      employee.setWage(wage);
      LOG.info("Wage of employee " + employee.getName() + " changed to " + wage);
    }

  }

  static class JuniorDeveloperState extends CareerState {

    protected double getWage() {
      return 1000.0;
    }

    public void event(ActivityExecution execution, Object event) throws Exception {
      String eventString = (String) event;
      if (eventString.equals("3 years of experience")) {
        
        execution.take("enoughYears");
        
      } else if (eventString.equals("evaluated")) {
        
        Employee emp = getEmployee(execution);
        int nrOfPerfectEvaluations = 0;
        for (Integer evaluation : emp.getEvaluations()) {
          if (evaluation >= 9) {
            nrOfPerfectEvaluations++;
          }
        }
        
        if (nrOfPerfectEvaluations >= 2) {
          execution.take("birdWithGoldenEggs");
        }
        
      }
    }
    
    

  }

  static class YoungPotentialState extends CareerState {

    protected double getWage() {
      return 1500.0;
    }

    public void event(ActivityExecution execution, Object event) throws Exception {
    }

  }

  static class SeniorDeveloperState extends CareerState {

    protected double getWage() {
      return 2000.0;
    }

    public void event(ActivityExecution execution, Object event) throws Exception {
      String eventString = (String) event;
      
      // Quickfix and not very pretty to look at: better to use real event objects
      
      if (eventString.contains("years of experience")
              && getEmployee(execution).getNrOfSucessFulProjects() == 2) {
        
        if (Integer.valueOf(eventString.charAt(0)) >= 5) {
          execution.take("enoughExperience");          
        }
        
      }
    }

  }

  static class ProjectLeadState extends CareerState {

    protected double getWage() {
      return 3000.0;
    }

    public void event(ActivityExecution execution, Object event) throws Exception {
      String eventString = (String) event;
      if (eventString.equals("marryWithCeoDaughter")) {
        execution.take("marryCeoDaughter");
      }
    }

  }

  static class ManagerState extends CareerState {

    protected double getWage() {
      return 5000.0;
    }

    public void event(ActivityExecution execution, Object event) throws Exception {
    }

  }

  static class CtoState extends CareerState {

    protected double getWage() {
      return 9999.0;
    }

    public void event(ActivityExecution execution, Object event) throws Exception {
    }

  }

}