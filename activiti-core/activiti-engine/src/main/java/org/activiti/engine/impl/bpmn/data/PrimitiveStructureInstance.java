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
 * An instance of {@link PrimitiveStructureDefinition}
 * 

 */
public class PrimitiveStructureInstance implements StructureInstance {

  protected Object primitive;

  protected PrimitiveStructureDefinition definition;

  public PrimitiveStructureInstance(PrimitiveStructureDefinition definition) {
    this(definition, null);
  }

  public PrimitiveStructureInstance(PrimitiveStructureDefinition definition, Object primitive) {
    this.definition = definition;
    this.primitive = primitive;
  }

  public Object getPrimitive() {
    return this.primitive;
  }

  public Object[] toArray() {
    return new Object[] { this.primitive };
  }

  public void loadFrom(Object[] array) {
    for (int i = 0; i < array.length; i++) {
      Object object = array[i];
      if (this.definition.getPrimitiveClass().isInstance(object)) {
        this.primitive = object;
        return;
      }
    }
  }
}
