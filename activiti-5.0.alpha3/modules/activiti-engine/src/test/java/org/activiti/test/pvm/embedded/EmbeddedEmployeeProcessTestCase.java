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

import junit.framework.TestCase;

import org.activiti.impl.util.LogUtil;
import org.junit.Test;

/**
 * 
 * @author Joram Barrez
 */
public class EmbeddedEmployeeProcessTestCase extends TestCase {

  static {
    LogUtil.readJavaUtilLoggingConfigFromClasspath();
  }

  @Test
  public void testRegularEmployeeToCto() {

    // Create a freshly-out-of-college employee

    Employee employee = new Employee("Joram Barrez");
    employee.setActive(true);
    assertEquals("junior developer", employee.getCareerState());
    assertEquals(1000.0, employee.getWage());

    // Give the employee some years of experience

    employee.setYearsOfExperience(1);
    assertEquals("junior developer", employee.getCareerState());

    employee.setYearsOfExperience(2);
    assertEquals("junior developer", employee.getCareerState());

    employee.setYearsOfExperience(3);
    assertEquals("senior developer", employee.getCareerState());
    assertEquals(2000.0, employee.getWage());

    // Employees can become a project lead every year IF they have at least two
    // successful projects done

    employee.setNrOfSucessFulProjects(1);
    assertEquals("senior developer", employee.getCareerState());

    employee.setYearsOfExperience(4);
    assertEquals("senior developer", employee.getCareerState());

    employee.setYearsOfExperience(5);
    assertEquals("senior developer", employee.getCareerState());

    employee.setNrOfSucessFulProjects(2);
    assertEquals("senior developer", employee.getCareerState());

    employee.setYearsOfExperience(6);
    assertEquals("project lead", employee.getCareerState());
    assertEquals(3000.0, employee.getWage());

    // marry the CEO daughter

    employee.marryWithCeoDaughter();
    assertEquals("CTO", employee.getCareerState());
    assertEquals(9999.0, employee.getWage());
  }

  @Test
  public void testYoungPotential() {

    Employee employee = new Employee("Joram Barrez");
    employee.setActive(true);
    assertEquals("junior developer", employee.getCareerState());
    assertEquals(1000.0, employee.getWage());

    employee.addEvaluation(9);
    assertEquals("junior developer", employee.getCareerState());

    employee.addEvaluation(10);
    assertEquals("young potential", employee.getCareerState());
    assertEquals(1500.0, employee.getWage());

  }

}
