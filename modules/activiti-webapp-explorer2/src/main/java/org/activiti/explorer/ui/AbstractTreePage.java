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

import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Tree;


/**
 * Superclass for all pages that have a tree on the left side of the page.
 * 
 * @author Joram Barrez
 */
public abstract class AbstractTreePage extends AbstractPage {

  private static final long serialVersionUID = 1L;

  @Override
  protected AbstractSelect createSelectComponent() {
    Tree tree = createTree();
    tree.setSizeFull();
    return tree;
  }
  
  protected abstract Tree createTree();

  @Override
  public void refreshSelectNext() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void selectElement(int index) {
    throw new UnsupportedOperationException();
  }

}
