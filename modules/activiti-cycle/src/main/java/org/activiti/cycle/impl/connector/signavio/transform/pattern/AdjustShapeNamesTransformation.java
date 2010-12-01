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
 * Adjusts names from Signavio (remove new lines, ' and maybe add more in future)
 * See https://app.camunda.com/jira/browse/HEMERA-164.
 * 
 * @deprecated because it doesn't state its target. Use {@link AdjustShapeNamesForJpdl3Transformation} instead.
 * 
 * @author bernd.ruecker@camunda.com
 */
public class AdjustShapeNamesTransformation extends OryxShapeNameTransformation {

  @Override
  public String transformName(String name, Shape shape) {
    return name.replaceAll("\n", " ").replaceAll("'", "").replaceAll("\"", "");
  }

}
