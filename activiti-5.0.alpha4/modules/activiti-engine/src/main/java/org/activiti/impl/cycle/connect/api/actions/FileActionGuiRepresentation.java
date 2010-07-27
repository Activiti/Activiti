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
package org.activiti.impl.cycle.connect.api.actions;

/**
 * Enum to express how the gui representation of a {@link FileAction} should be implemented.
 * 
 * The list of options (still under development): * 
 * <tr>
 *   <td>DEFAULT_PANE: Something is shown in the default panel in the GUI (e.g. a PNG)</td>
 *   <td>OWN_WINDOW: Something is opened in a own Browser window (e.g. Signavio Modeler)</td>
 *   <td>NONE: The action doesn't need any GUI (e.g. writing something to SVN)</td>
 *   <td>MODAL_PANEL: The action does need additional parameters from the user</td>
 * </tr>
 * 
 * @author bernd.ruecker@camunda.com
 */
public enum FileActionGuiRepresentation {

  DEFAULT_PANE, OWN_WINDOW, NONE, MODAL_PANEL

}
