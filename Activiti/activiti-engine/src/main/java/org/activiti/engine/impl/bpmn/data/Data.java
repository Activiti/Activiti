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
package org.activiti.engine.impl.bpmn.data;

/**
 * Implementation of the BPMN 2.0 'dataInput' and 'dataOutput'
 * 

 */
public class Data {

  protected String id;

  protected String name;

  protected ItemDefinition definition;

  public Data(String id, String name, ItemDefinition definition) {
    this.id = id;
    this.name = name;
    this.definition = definition;
  }

  public String getId() {
    return this.id;
  }

  public String getName() {
    return this.name;
  }

  public ItemDefinition getDefinition() {
    return this.definition;
  }
}
