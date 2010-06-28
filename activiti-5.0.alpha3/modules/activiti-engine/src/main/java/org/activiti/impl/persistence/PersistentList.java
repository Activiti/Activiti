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
package org.activiti.impl.persistence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * @author Tom Baeyens
 */
public class PersistentList<T> extends ArrayList<T> {

  private static final long serialVersionUID = 1L;
  
  protected List<T> added = new ArrayList<T>();
  protected List<T> removed = new ArrayList<T>();

  @Override
  public void add(int index, T element) {
    added.add(element);
    super.add(index, element);
  }

  @Override
  public boolean add(T element) {
    added.add(element);
    return super.add(element);
  }

  @Override
  public boolean addAll(Collection< ? extends T> c) {
    added.addAll(c);
    return super.addAll(c);
  }

  @Override
  public boolean addAll(int index, Collection< ? extends T> c) {
    added.addAll(c);
    return super.addAll(index, c);
  }

  @Override
  public T remove(int index) {
    removed.add(get(index));
    return super.remove(index);
  }

  @Override
  public boolean remove(Object o) {
    boolean isRemoved = super.remove(o);
    if (isRemoved) {
      removed.add((T) o);
    }
    return isRemoved;
  }

  @Override
  protected void removeRange(int fromIndex, int toIndex) {
    throw new UnsupportedOperationException("this method is not available on "+getClass().getName());
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException("this method is not available on "+getClass().getName());
  }

  @Override
  public T set(int index, T element) {
    throw new UnsupportedOperationException("this method is not available on "+getClass().getName());
  }
}
