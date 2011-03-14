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

import java.util.Collection;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;


/**
 * @author Joram Barrez
 */
public class TaskListContainer implements Container {

  private static final long serialVersionUID = -8165289515891774850L;

  public Item getItem(Object itemId) {
    return null;
  }

  public Collection< ? > getContainerPropertyIds() {
    return null;
  }

  public Collection< ? > getItemIds() {
    return null;
  }

  public Property getContainerProperty(Object itemId, Object propertyId) {
    return null;
  }

  public Class< ? > getType(Object propertyId) {
    return null;
  }

  public int size() {
    return 0;
  }

  public boolean containsId(Object itemId) {
    return false;
  }

  public Item addItem(Object itemId) throws UnsupportedOperationException {
    return null;
  }

  public Object addItem() throws UnsupportedOperationException {
    return null;
  }

  public boolean removeItem(Object itemId) throws UnsupportedOperationException {
    return false;
  }

  public boolean addContainerProperty(Object propertyId, Class< ? > type, Object defaultValue) throws UnsupportedOperationException {
    return false;
  }

  public boolean removeContainerProperty(Object propertyId) throws UnsupportedOperationException {
    return false;
  }

  public boolean removeAllItems() throws UnsupportedOperationException {
    return false;
  }

}
