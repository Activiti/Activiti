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
package org.activiti.engine.impl.persistence.repository;



/**
 * @author Tom Baeyens
 */
public class PropertyEntity {

  String name;
  int dbversion;
  String value;

  public PropertyEntity() {
  }

  public PropertyEntity(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }
  
  public int getDbversion() {
    return dbversion;
  }

  public void setDbversion(int dbversion) {
    this.dbversion = dbversion;
  }
  public String getValue() {
    return value;
  }
  public void setValue(String value) {
    this.value = value;
  }
}
