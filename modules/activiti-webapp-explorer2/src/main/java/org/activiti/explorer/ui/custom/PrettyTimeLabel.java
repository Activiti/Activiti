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

import java.text.DateFormat;
import java.util.Date;

import org.activiti.explorer.Constants;

import com.ocpsoft.pretty.time.PrettyTime;
import com.vaadin.ui.Label;


/**
 * Label that renders the given date in a human readable format. The tooltip
 * contains the date, using the default date formatting.
 * 
 * @author Frederik Heremans
 */
public class PrettyTimeLabel extends Label {
  
  private static final long serialVersionUID = 1L;
  
  public PrettyTimeLabel(Date date) {
    this(date, "");
  }
  
  public PrettyTimeLabel(Date date, String noDateCaption) {
    
    if (date != null) {
      DateFormat dateFormat = (DateFormat) Constants.DEFAULT_DATE_FORMATTER.clone();
      setValue(new PrettyTime().format(date));
      setDescription(dateFormat.format(date));
    } else {
      setValue(noDateCaption);
      setDescription(noDateCaption);
    }
  }
}
