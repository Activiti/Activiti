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

package org.activiti.cycle.impl.transform.signavio;

import java.util.Map;

import org.activiti.cycle.impl.connector.signavio.util.CustomProperty;
import org.oryxeditor.server.diagram.Diagram;
import org.oryxeditor.server.diagram.Shape;

/**
 * @author Falko Menge <falko.menge@camunda.com>
 */
public abstract class OryxShapeNameTransformation extends OryxTransformation {

  public OryxShapeNameTransformation() {
    super();
  }

  @Override
  public Diagram transform(Diagram diagram) {
    for (Shape shape : diagram.getShapes()) {
      Map<String, String> properties = shape.getProperties();
      if (properties.containsKey("name")) {
        String oldName = properties.get("name");
  
        String newName = transformName(oldName);
  
        if (!newName.equals(oldName)) {
          setOriginalName(shape, oldName);
          properties.put("name", newName);
        }
  
      }
    }
  
    return diagram;
  }

  public abstract String transformName(String name);

  public static void setOriginalName(Shape shape, String oldName) {
    String oldDocumentation = shape.getProperty("documentation");
    String newDocumentation = CustomProperty.ORIGINAL_NAME.setValueUnlessPropertyExists(oldDocumentation, oldName);
    if (newDocumentation != null && !newDocumentation.equals(oldDocumentation)) {
      shape.putProperty("documentation", newDocumentation);
    }
  }

}