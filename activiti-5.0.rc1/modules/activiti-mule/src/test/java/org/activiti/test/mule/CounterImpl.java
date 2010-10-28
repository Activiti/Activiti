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
package org.activiti.test.mule;

import javax.jws.WebService;

/**
 * An implementation of a Counter WS
 * 
 * @author Esteban Robles Luna
 */
@WebService(endpointInterface = "org.activiti.test.mule.Counter",
        serviceName = "Counter")
public class CounterImpl implements Counter {

  protected int count;
  
  public CounterImpl() {
    this.count = -1;
  }
  
  /**
   * {@inheritDoc}
   */
  public int getCount() {
    return this.count;
  }

  /**
   * {@inheritDoc}
   */
  public void inc() {
    this.count++;
  }

  /**
   * {@inheritDoc}
   */
  public void reset() {
    this.setTo(0);
  }

  /**
   * {@inheritDoc}
   */
  public void setTo(int value) {
    this.count = value;
  }
  
  /**
   * {@inheritDoc}
   */
  public String prettyPrintCount(String prefix, String suffix) {
    return prefix + this.getCount() + suffix;
  }
}