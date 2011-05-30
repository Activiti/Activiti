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

package org.activiti.cycle.impl.connector.signavio.transform.pattern;

import org.oryxeditor.server.diagram.Shape;

/**
 * Adjusts names of Oryx shapes for use as names for jPDL3 Nodes
 * 
 * '\' is forbidden by Node.setName() in jBPM 3.2.6.SP1
 * '"' is converted into '&quot;', which is hard to read in the XML source code
 * '\n' is problematic for referencing and also hard to read
 * 
 * @author Falko Menge <falko.menge@camunda.com>
 */
public class AdjustShapeNamesForJpdl3 extends OryxShapeNameTransformation {

  @Override
  public String transformName(String name, Shape shape) {
    return name.replaceAll("\n", " ").replaceAll("/", "|").replaceAll("\"", "");
  }

}
