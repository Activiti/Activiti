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
package org.activiti.spring.test.taskassignment;

import static java.util.Arrays.asList;

import org.activiti.engine.delegate.DelegateExecution;

import java.util.List;

/**
 */
public class FakeLdapService {

  public String findManagerForEmployee(String employee) {
    // Pretty useless LDAP service ...
    return "Kermit The Frog";
  }

  public List<String> findAllSales() {
    return asList("kermit", "gonzo", "fozzie");
  }

  public List<String> findManagers(DelegateExecution execution, String emp) {
    if (execution == null) {
      throw new RuntimeException("Execution parameter is null");
    }

    if (emp == null || "".equals(emp)) {
      throw new RuntimeException("emp parameter is null or empty");
    }

    return asList("management", "directors");
  }

}
