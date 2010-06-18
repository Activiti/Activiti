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
package org.activiti.test.pvm.embedded;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Joram Barrez
 */
public class Employee implements Serializable {
  
  private static final long serialVersionUID = 1L;

  private String name;
  
  private double wage = 0.0;
  
  private int yearsOfExperience = 0;
  
  private int nrOfSucessFulProjects = 0;
  
  private List<Integer> evaluations = new ArrayList<Integer>();
  
  private boolean active = false;
  
  transient private EmployeeCareerProcess careerState;
  
  public Employee(String name) {
    this.name = name;
    this.careerState = new EmployeeCareerProcess(this);
  }
  
  public String getCareerState() {
    return careerState.getCurrentState().get(0);
  }
  
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
  
  public double getWage() {
    return wage;
  }
  
  public void setWage(double wage) {
    this.wage = wage;
  }
  
  public int getYearsOfExperience() {
    return yearsOfExperience;
  }
  
  public void setYearsOfExperience(int yearsOfExperience) {
    this.yearsOfExperience = yearsOfExperience;
    careerState.fireEvent(yearsOfExperience + " years of experience");
  }
  
  public int getNrOfSucessFulProjects() {
    return nrOfSucessFulProjects;
  }
  
  public void setNrOfSucessFulProjects(int nrOfSucessFulProjects) {
    this.nrOfSucessFulProjects = nrOfSucessFulProjects;
  }
  
  public List<Integer> getEvaluations() {
    return evaluations;
  }
  
  public void setEvaluations(List<Integer> evaluations) {
    this.evaluations = evaluations;
  }
  
  public void addEvaluation(int evaluation) {
    evaluations.add(evaluation);
    careerState.fireEvent("evaluated");
  }
  
  public boolean isActive() {
    return active;
  }
  
  public void setActive(boolean active) {
    this.active = active;
    careerState.start();
  }
  
  public void marryWithCeoDaughter() {
    careerState.fireEvent("marryWithCeoDaughter");
  }

}
