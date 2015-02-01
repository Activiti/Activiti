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

package org.activiti.engine.test.bpmn.gateway;

import java.io.Serializable;

/**
 * Inclusive Gateway test order was originally stolen from the
 * ExclusiveGatewayTestOrder.
 * 
 * @author Joram Barrez
 * @author Tom Van Buskirk
 */
public class InclusiveGatewayTestOrder implements Serializable {

  private static final long serialVersionUID = 1L;

  private int price;

  public InclusiveGatewayTestOrder(int price) {
    this.price = price;
  }

  public void setPrice(int price) {
    this.price = price;
  }

  public int getPrice() {
    return price;
  }

  public boolean isBasic() {
    return price <= 100;
  }

  public boolean isStandard() {
    return price <= 150;
  }

  public boolean isGold() {
    return price <= 200;
  }

}
