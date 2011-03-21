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
import java.util.List;
import java.util.Map;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;


/**
 * @author Joram Barrez
 */
public abstract class AbstractLazyLoadingQuery<T> implements LazyLoadingQuery {
  
  protected LazyLoadingContainer lazyLoadingContainer;
  
  public List<Item> loadItems(int start, int count) {
    List<Item> items = new ArrayList<Item>();
    for (T bean : loadBeans(start, count)) {
      items.add(toItem(bean));
    }
    return items;
  }
  
  protected abstract List<T> loadBeans(int start, int count);
  
  public <T> Item toItem(final T bean) {
    if (bean instanceof Map) {
      return new MapItem((Map<Object, Object>) bean);
    } 
    return new BeanItem<T>(bean);
  }
  
  public void setLazyLoadingContainer(LazyLoadingContainer lazyLoadingContainer) {
    this.lazyLoadingContainer = lazyLoadingContainer;
  }

}
