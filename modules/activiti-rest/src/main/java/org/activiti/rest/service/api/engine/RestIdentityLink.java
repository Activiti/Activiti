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

package org.activiti.rest.service.api.engine;


/**
 * @author Frederik Heremans
 */
public class RestIdentityLink {

  private String url;
  private String user;
  private String group;
  private String type;
  
  public String getUrl() {
    return url;
  }
  public void setUrl(String url) {
    this.url = url;
  }
  public String getUser() {
    return user;
  }
  public void setUser(String user) {
    this.user = user;
  }
  public String getGroup() {
    return group;
  }
  public void setGroup(String group) {
    this.group = group;
  }
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }
}
