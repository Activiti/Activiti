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
package org.activiti.editor.language.bpmn.parser;

import org.activiti.editor.language.bpmn.model.BaseElement;
import org.activiti.editor.language.bpmn.model.BoundaryEvent;
import org.activiti.editor.language.bpmn.model.Process;

/**
 * @author Tijs Rademakers
 */
public class BoundaryEventModel extends BaseElement {
  
  public static final String TIMEEVENT = "timeevent";
  public static final String ERROREVENT = "errorevent";
  public static final String SIGNALEVENT = "signalevent";
  
  public BoundaryEvent boundaryEvent;
  public String attachedRef;
  public String type;
  public Process parentProcess;
}
