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

package org.activiti.engine.impl.pvm.process;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** 
* A single lane in a BPMN 2.0 LaneSet, currently only used internally for rendering the
* diagram. The PVM doesn't actually use the laneSets/lanes.
* 
* @author Frederik Heremans
*/
public class Lane implements HasDIBounds, Serializable {

  private static final long serialVersionUID = 1L;
  protected String id;
  protected String name;
  protected List<String> flowNodeIds;
 
  protected int x = -1;
  protected int y = -1;
  protected int width = -1;
  protected int height = -1;
  
  public void setId(String id) {
    this.id = id;
  }
  
  public String getId() {
    return id;
  }
  
  
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public int getX() {
    return x;
  }
  
  public void setX(int x) {
    this.x = x;
  }

  public int getY() {
    return y;
  }

  public void setY(int y) {
    this.y = y;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }
  
  
  public List<String> getFlowNodeIds() {
    if(flowNodeIds == null) {
      flowNodeIds = new ArrayList<String>();
    }
    return flowNodeIds;
  }

}
