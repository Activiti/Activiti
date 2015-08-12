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

package org.activiti.examples.bpmn.expression;

import java.io.Serializable;


/**
 * Simple POJO, just for test purposes
 * 
 * @author Joram Barrez
 */
public class UelExpressionTestOrder implements Serializable {
  
  private static final long serialVersionUID = 1L;
  
  protected int price;
  
  public UelExpressionTestOrder(int price) {
    this.price = price;
  }
  
  public void setPrice(int price) {
    this.price = price;
  }
  
  public int getPrice() {
    return price;
  }
  
  public boolean isPremiumOrder() {
    return price >= 250;
  }

}
