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

import java.awt.Image;

import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;

import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;


/**
 * Label that allows to call direcly with skype when clicked.
 * 
 * @author Joram Barrez
 */
public class SkypeLabel extends CustomComponent {

  private static final long serialVersionUID = 1L;
  
  /**
   * Constructs a {@link SkypeLabel} based on the given Skype id
   */
  public SkypeLabel(String skypeId) {
    CssLayout layout = new CssLayout();
    setCompositionRoot(layout);
    
    Label label = new Label("<script type='text/javascript' src='http://download.skype.com/share/skypebuttons/js/skypeCheck.js'></script>", Label.CONTENT_XHTML);
    layout.addComponent(label);
    
    Link link = new Link(null, new ExternalResource("skype:" + skypeId + "?call"));
    link.setIcon(Images.SKYPE);
    layout.addComponent(link);
    
    setWidth(16, UNITS_PIXELS);
    setHeight(16, UNITS_PIXELS);
  }

}
