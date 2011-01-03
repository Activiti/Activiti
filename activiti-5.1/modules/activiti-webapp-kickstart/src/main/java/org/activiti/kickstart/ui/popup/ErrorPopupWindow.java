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
package org.activiti.kickstart.ui.popup;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * @author Joram Barrez
 */
public class ErrorPopupWindow extends Window {

  protected static final long serialVersionUID = 5499992460391485767L;

  protected static final String TITLE = "Oops!";
  protected static final String SOMETHING_WENT_WRONG = "Something went wrong ...";

  public ErrorPopupWindow(Exception e) {
    setModal(true);
    setWidth("50%");
    center();
    setCaption(TITLE);

    VerticalLayout layout = new VerticalLayout();
    layout.setSpacing(true);
    layout.addComponent(new Label(SOMETHING_WENT_WRONG));
    layout.addComponent(new Label(e.toString()));
    addComponent(layout);
  }

}
