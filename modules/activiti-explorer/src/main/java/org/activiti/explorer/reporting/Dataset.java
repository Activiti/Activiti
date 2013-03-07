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
package org.activiti.explorer.reporting;

import java.util.HashMap;
import java.util.Map;


/**
 * @author Joram Barrez
 */
public class Dataset {
  
  protected String type;
  
  protected String description;
  
  protected String xaxis;
  
  protected String yaxis;
  
  protected Map<String, Number> data = new HashMap<String, Number>();

  public String getType() {
    return type;
  }
  
  public void setType(String type) {
    this.type = type;
  }
  
  public String getDescription() {
    return description;
  }
  
  public void setDescription(String description) {
    this.description = description;
  }
  
  public Map<String, Number> getData() {
    return data;
  }
  
  public String getXaxis() {
    return xaxis;
  }
  
  public void setXaxis(String xaxis) {
    this.xaxis = xaxis;
  }
  
  public String getYaxis() {
    return yaxis;
  }
  
  public void setYaxis(String yaxis) {
    this.yaxis = yaxis;
  }

  public void setData(Map<String, Number> data) {
    this.data = data;
  }
  
  public void add(String key, Number value) {
    data.put(key, value);
  }

}
