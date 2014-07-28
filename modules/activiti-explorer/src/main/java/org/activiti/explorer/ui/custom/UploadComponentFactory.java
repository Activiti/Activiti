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

package org.activiti.explorer.ui.custom;

import org.activiti.explorer.Environments;
import org.activiti.explorer.ui.ComponentFactory;


/**
 * @author Frederik Heremans
 */
public class UploadComponentFactory implements ComponentFactory<UploadComponent> {

  private static final long serialVersionUID = 1L;
  protected boolean enableDrop = true; 
          
  @Override
  public void initialise(String environment) {
    if (environment.equals(Environments.ALFRESCO)) {
      enableDrop = false;
    }
  }

  @Override
  public UploadComponent create() {
    return new UploadComponent(enableDrop);
  }

}
