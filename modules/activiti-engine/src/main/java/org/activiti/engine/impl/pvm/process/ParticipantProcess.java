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


/**
 * Object indicating that a {@link ProcessDefinitionImpl} is a participant in a collaboration (pool). 
 * Currently only used to store graphical information and the pool name.
 * 
 * @author Frederik Heremans
 */
public class ParticipantProcess implements HasDIBounds , Serializable {

  private static final long serialVersionUID = 1L;
  
  protected String id;
  protected String name;
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
  
  
}
