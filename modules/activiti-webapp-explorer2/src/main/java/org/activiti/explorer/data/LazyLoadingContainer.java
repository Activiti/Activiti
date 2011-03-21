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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;



/**
 * Inspired on the LazyQueryContainer add-on for Vaadin
 * (https://github.com/tlaukkan/vaadin-lazyquerycontainer) 
 * 
 * @author Joram Barrez
 */
public class LazyLoadingContainer implements Container.Indexed, Container.Sortable{

  private static final long serialVersionUID = -3173261287161090288L;
  
  protected LazyLoadingQuery lazyLoadingQuery;
  protected int batchSize;
  protected int size = -1;
  
  protected List<Object> containerPropertyIds = new ArrayList<Object>();
  protected Map<Object, Class<?>> containerPropertyTypes = new HashMap<Object, Class<?>>();
  protected Map<Object, Object> containerPropertyDefaultValues = new HashMap<Object, Object>();
  
  protected Map<Integer, Item> itemCache = new HashMap<Integer, Item>();
  
  public LazyLoadingContainer(LazyLoadingQuery lazyLoadingQuery, int batchSize) {
    this.lazyLoadingQuery = lazyLoadingQuery;
    this.batchSize = batchSize;
    lazyLoadingQuery.setLazyLoadingContainer(this);
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
  
  public Collection< ? > getContainerPropertyIds() {
    return containerPropertyIds;
  }

  public Class< ? > getType(Object propertyId) {
    return containerPropertyTypes.get(propertyId);
  }
  
  public Object getDefaultValue(Object propertyId) {
    return containerPropertyDefaultValues.get(propertyId);
  }
  
  public Property getContainerProperty(Object itemId, Object propertyId) {
    return getItem(itemId).getItemProperty(propertyId);
  }
  
  public boolean containsId(Object itemId) {
    Integer index = (Integer) itemId;
    return index >= 0 && index < size();
  }

  public Item getItem(Object itemId) {
    if (!itemCache.containsKey(itemId)) {
      Integer index = (Integer) itemId;
      int start = index - (index%batchSize);
      
      List<Item> batch = lazyLoadingQuery.loadItems(start, batchSize);
      for (Item batchItem : batch) {
        itemCache.put(start, batchItem);
        start++;
      }
    }
    return itemCache.get(itemId);
  }
  
  public int size() {
    if (size == -1) {
      size = lazyLoadingQuery.size();
    }
    return size;
  }

  public Collection< ? > getItemIds() {
    return new ArrayList<Integer>() {
      public int size() {
        return size();
      }
      public Integer get(int index) {
        return index;
      }
    };
  }
  
  public Object firstItemId() {
    return 0;
  }

  public Object lastItemId() {
    return size()-1;
  }

  public boolean isFirstId(Object itemId) {
    return ((Integer) itemId).equals(0);
  }

  public boolean isLastId(Object itemId) {
    return ((Integer) itemId).equals(size()-1);
  }
  
  public Object nextItemId(Object itemId) {
    Integer index = (Integer) itemId;
    return index++;
  }

  public Object prevItemId(Object itemId) {
   Integer index = (Integer) itemId;
   return index--;
  }
  
  public int indexOfId(Object itemId) {
    return (Integer) itemId;
  }

  public Object getIdByIndex(int index) {
    return new Integer(index);
  }  
  
  // Special operation to support accessing a page straight from an URL
//  public int getIndexForObjectId(Object id) {
//    
//  }
  
  // Unsupported Operations ----------------------------------------------------------------------
  
  public Object addItem() throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }
  
  public Object addItemAfter(Object previousItemId) throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  public Item addItemAfter(Object previousItemId, Object newItemId) throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }
  
  public Object addItemAt(int index) throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  public Item addItemAt(int index, Object newItemId) throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  public boolean removeItem(Object itemId) throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  public boolean removeAllItems() throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  public void sort(Object[] propertyId, boolean[] ascending) {
    throw new UnsupportedOperationException();
  }
  
  public Collection< ? > getSortableContainerPropertyIds() {
    throw new UnsupportedOperationException();
  }
  
  public Item addItem(Object itemId) throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

}
