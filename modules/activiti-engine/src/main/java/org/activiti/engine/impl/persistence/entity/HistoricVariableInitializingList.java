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

package org.activiti.engine.impl.persistence.entity;

import java.util.ArrayList;
import java.util.Collection;

import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.variable.CacheableVariable;
import org.activiti.engine.impl.variable.JPAEntityListVariableType;
import org.activiti.engine.impl.variable.JPAEntityVariableType;


/**
 * List that initialises binary variable values if command-context is active.
 * 
 * @author Frederik Heremans
 */
public class HistoricVariableInitializingList extends ArrayList<HistoricVariableInstanceEntity> {

  private static final long serialVersionUID = 1L;
  
  @Override
  public void add(int index, HistoricVariableInstanceEntity e) {
    super.add(index, e);
    initializeVariable(e);
  }
  
  @Override
  public boolean add(HistoricVariableInstanceEntity e) {
    initializeVariable(e);
    return super.add(e);
  }
  @Override
  public boolean addAll(Collection< ? extends HistoricVariableInstanceEntity> c) {
    for(HistoricVariableInstanceEntity e : c) {
      initializeVariable(e);
    }
    return super.addAll(c);
  }
  @Override
  public boolean addAll(int index, Collection< ? extends HistoricVariableInstanceEntity> c) {
    for(HistoricVariableInstanceEntity e : c) {
      initializeVariable(e);
    }
    return super.addAll(index, c);
  }

  /**
   * If the passed {@link HistoricVariableInstanceEntity} is a binary variable and the command-context is active,
   * the variable value is fetched to ensure the byte-array is populated.
   */
  protected void initializeVariable(HistoricVariableInstanceEntity e) {
    if(Context.getCommandContext() != null && e != null && e.getVariableType() != null) {
      e.getValue();
      
      // make sure JPA entities are cached for later retrieval
      if (JPAEntityVariableType.TYPE_NAME.equals(e.getVariableType().getTypeName()) || JPAEntityListVariableType.TYPE_NAME.equals(e.getVariableType().getTypeName())) {
        ((CacheableVariable) e.getVariableType()).setForceCacheable(true);
      }
    }
  }
}