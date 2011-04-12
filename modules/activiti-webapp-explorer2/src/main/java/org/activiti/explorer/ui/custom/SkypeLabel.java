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

import com.vaadin.ui.Label;


/**
 * Label that allows to call direcly with skype when clicked.
 * 
 * @author Joram Barrez
 */
public class SkypeLabel extends Label {

  private static final long serialVersionUID = 1L;
  
  public SkypeLabel(String skypeId) {
    super("<script type='text/javascript' " +
            "src='http://download.skype.com/share/skypebuttons/js/skypeCheck.js'></script>" +
            "<a href='skype:" + skypeId + "?call'>" +
            "<img src='VAADIN/themes/activiti/img/skype.png' style='border: none;' /></a>",
            Label.CONTENT_XHTML);
  }

}
