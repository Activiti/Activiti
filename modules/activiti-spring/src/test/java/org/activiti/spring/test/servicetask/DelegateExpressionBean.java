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
package org.activiti.spring.test.servicetask;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegation;


/**
 * @author Joram Barrez
 */
public class DelegateExpressionBean implements JavaDelegation {
  
  private SentenceGenerator sentenceGenerator;
  
  public void execute(DelegateExecution execution) throws Exception {
    execution.setVariable("myVar", sentenceGenerator.getSentence());
  }
  
  public void setSentenceGenerator(SentenceGenerator sentenceGenerator) {
    this.sentenceGenerator = sentenceGenerator;
  }

}
