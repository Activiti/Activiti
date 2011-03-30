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

package org.activiti.explorer.util;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;


/**
 * Container that is just a simple list of {@link Item}s, which are identified by their index
 * in the list (order of adding).
 * 
 * @author Frederik Heremans
 */
public class ItemListContainer implements Container {

  private static final long serialVersionUID = 1L;
  
  protected List<Item> items = new ArrayList<Item>();
  protected List<Object> containerPropertyIds = new ArrayList<Object>();
  protected Map<Object, Class<?>> containerPropertyTypes = new HashMap<Object, Class<?>>();
  protected Map<Object, Object> containerPropertyDefaultValues = new HashMap<Object, Object>();
  
  public void addItem(Item item) {
    items.add(item);
  }
  
  public Item getItem(Object itemId) {
    Integer index = (Integer) itemId;
    
    if(index >= 0 && index < items.size()) {
      return items.get(index);
    }
    return null;
  }

  public Collection< ? > getContainerPropertyIds() {
    return containerPropertyIds;
  }

  public Collection< ? > getItemIds() {
    // We don't need an actual list of elements,
    // so we can just override the get() and save some memory
    return new AbstractList<Integer>() {
      public int size() {
        return ItemListContainer.this.size();
      }
      public Integer get(int index) {
        return index;
      }
    };
  }

  public Class< ? > getType(Object propertyId) {
    return containerPropertyTypes.get(propertyId);
  }
  
  public Object getDefaultValue(Object propertyId) {
    return containerPropertyDefaultValues.get(propertyId);
  }

  public int size() {
    return items.size();
  }

  public boolean containsId(Object itemId) {
    return getItem(itemId) != null;
  }

  public Item addItem(Object itemId) throws UnsupportedOperationException {
    throw new UnsupportedOperationException("Cannot add items based on ID");
  }

  public Object addItem() throws UnsupportedOperationException {
    throw new UnsupportedOperationException("Cannot add items based on ID");
  }

  public boolean removeItem(Object itemId) throws UnsupportedOperationException {
    throw new UnsupportedOperationException("Cannot remove items based on ID");
  }

  public boolean addContainerProperty(Object propertyId, Class< ? > type, Object defaultValue) throws UnsupportedOperationException {
    containerPropertyIds.add(propertyId);
    containerPropertyTypes.put(propertyId, type);
    containerPropertyDefaultValues.put(propertyId, defaultValue);
    return true;
  }

  public boolean removeContainerProperty(Object propertyId) throws UnsupportedOperationException {
    containerPropertyIds.remove(propertyId);
    containerPropertyTypes.remove(propertyId);
    containerPropertyDefaultValues.remove(propertyId);
    return true;
  }

  public boolean removeAllItems() throws UnsupportedOperationException {
    items.clear();
    return true;
  }

  public Property getContainerProperty(Object itemId, Object propertyId) {
    Item item = getItem(itemId);
    return item.getItemProperty(propertyId);
  }

}
