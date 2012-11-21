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

package org.activiti.explorer.ui.custom;

import com.vaadin.terminal.Resource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;


/**
 * A {@link ColumnGenerator} that always returns the same component to render,
 * which is an {@link Embeddable} containing the theme image passed in the constructor.
 * 
 * @author Frederik Heremans
 */
public class ThemeImageColumnGenerator implements ColumnGenerator {

  private static final long serialVersionUID = -7742412844347541389L;
  
  private Resource image;
  
  public ThemeImageColumnGenerator(String imageName) {
    image = new ThemeResource(imageName);
  }
  
  public ThemeImageColumnGenerator(Resource image) {
    this.image = image;
  }
  
  public Component generateCell(Table source, Object itemId, Object columnId) {
    return new Embedded(null, image);
  }

}
