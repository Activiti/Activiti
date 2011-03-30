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

package org.activiti.explorer.ui.util;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;


/**
 * Column generator that generates an image based on the value of a boolean
 * item property for which this column generator is added. Renders one image 
 * (or no image) when value is 'true' and another (or none) when value is false;
 * 
 * @author Frederik Heremans
 */
public class ThemedImageBooleanColumnGenerator implements ColumnGenerator {

  private static final long serialVersionUID = -738405586040486434L;
  
  private Resource trueImage;
  private Resource falseImage;
  
  /**
   * @param imageWhenTrue image show when boolean value is true, can be null 
   *  to have no image rendered.
   * @param imageWhenFalse image show when boolean value is false, can be null 
   *  to have no image rendered.
   */
  public ThemedImageBooleanColumnGenerator(String imageWhenTrue, String imageWhenFalse) {
    if(imageWhenTrue != null) {
      trueImage = new ThemeResource(imageWhenTrue);
    }
    if(imageWhenFalse != null) {
      falseImage = new ThemeResource(imageWhenFalse);
    }
  }
  
  /**
   * @param imageWhenTrue image show when boolean value is true, can be null 
   *  to have no image rendered.
   * @param imageWhenFalse image show when boolean value is false, can be null 
   *  to have no image rendered.
   */
  public ThemedImageBooleanColumnGenerator(Resource imageWhenTrue, Resource imageWhenFalse) {
    trueImage = imageWhenTrue;
    falseImage = imageWhenFalse;
  }

  public Component generateCell(Table source, Object itemId, Object columnId) {
    boolean booleanValue = false;
    Item item = source.getItem(itemId);
    if(item != null) {
      Property booleanProp = item.getItemProperty(columnId);
      if(booleanProp != null) {
        booleanValue = (Boolean) booleanProp.getValue();
      }
    }
    
    if(booleanValue) {
      return new Embedded(null, trueImage);
    } else {
      return new Embedded(null, falseImage);
    }
  }

}
