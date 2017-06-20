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
 * Implementation of the BPMN 2.0 'itemDefinition'
 * 

 */
public class ItemDefinition {

  protected String id;

  protected StructureDefinition structure;

  protected boolean isCollection;

  protected ItemKind itemKind;

  private ItemDefinition() {
    this.isCollection = false;
    this.itemKind = ItemKind.Information;
  }

  public ItemDefinition(String id, StructureDefinition structure) {
    this();
    this.id = id;
    this.structure = structure;
  }

  public ItemInstance createInstance() {
    return new ItemInstance(this, this.structure.createInstance());
  }

  public StructureDefinition getStructureDefinition() {
    return this.structure;
  }

  public boolean isCollection() {
    return isCollection;
  }

  public void setCollection(boolean isCollection) {
    this.isCollection = isCollection;
  }

  public ItemKind getItemKind() {
    return itemKind;
  }

  public void setItemKind(ItemKind itemKind) {
    this.itemKind = itemKind;
  }

  public String getId() {
    return this.id;
  }
}
