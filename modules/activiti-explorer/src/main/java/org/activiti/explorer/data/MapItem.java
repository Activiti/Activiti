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
package org.activiti.explorer.data;

import java.util.Collection;
import java.util.Map;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;


/**
 * @author Joram Barrez
 */
public class MapItem implements Item {
  
  private static final long serialVersionUID = 5079849025046231994L;
  
  protected Map<? extends Object, Object> map;
  
  public MapItem(Map<? extends Object, Object> map) {
    this.map = map;
  }
  
  public Property getItemProperty(Object id) {
    Object obj = map.get(id);
    if (obj == null) {
      return null;
    }
    return new ObjectProperty<Object>(obj);
  }

  public Collection< ? > getItemPropertyIds() {
    return map.keySet();
  }

  public boolean addItemProperty(Object id, Property property) throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  public boolean removeItemProperty(Object id) throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

}
