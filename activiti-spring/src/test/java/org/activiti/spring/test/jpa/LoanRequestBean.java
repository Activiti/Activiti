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

package org.activiti.spring.test.jpa;

import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Service bean that handles loan requests.
 * 

 */
public class LoanRequestBean {

  @PersistenceContext
  private EntityManager entityManager;

  @Transactional
  public LoanRequest newLoanRequest(String customerName, Long amount) {
    LoanRequest lr = new LoanRequest();
    lr.setCustomerName(customerName);
    lr.setAmount(amount);
    lr.setApproved(false);
    entityManager.persist(lr);
    return lr;
  }

  public LoanRequest getLoanRequest(Long id) {
    return entityManager.find(LoanRequest.class, id);
  }
}
