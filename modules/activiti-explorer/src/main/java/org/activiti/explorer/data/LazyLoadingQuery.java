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

import java.io.Serializable;
import java.util.List;

import com.vaadin.data.Item;


/**
 * Inspired on the LazyQueryContainer add-on for Vaadin
 * (https://github.com/tlaukkan/vaadin-lazyquerycontainer) 
 * 
 * @author Joram Barrez
 */
public interface LazyLoadingQuery extends Serializable {
  
  int size();
  
  List<Item> loadItems(int start, int count);
  
  Item loadSingleResult(String id);
  
  void setLazyLoadingContainer(LazyLoadingContainer lazyLoadingContainer);
  
  void setSorting(Object[] propertyIds, boolean[] ascending);
  
}
