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
package org.activiti.explorer.ui;

import org.activiti.engine.ActivitiException;
import org.activiti.explorer.Environments;



/**
 * @author Joram Barrez
 */
public abstract class NoParamComponentFactory<T> implements ComponentFactory<T>{
  
  protected Class<? extends T> clazz;
  
  public void initialise(String environment) {
    if (environment.equals(Environments.ALFRESCO)) {
      clazz = getAlfrescoComponentClass();
    } else {
      clazz = getDefaultComponentClass();
    }
  }
  
  public T create() {
    try {
      return clazz.newInstance();
    } catch (Exception e) {
      throw new ActivitiException("Couldn't instantiate class " + clazz, e); 
    }
  }
  
  protected abstract Class<? extends T> getAlfrescoComponentClass();
  protected abstract Class<? extends T> getDefaultComponentClass();

}
