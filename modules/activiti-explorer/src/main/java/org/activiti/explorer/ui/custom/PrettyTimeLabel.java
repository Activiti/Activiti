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
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.activiti.explorer.Constants;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.util.time.HumanTime;

import com.vaadin.ui.Label;


/**
 * Label that renders the given date in a human readable format. The tooltip
 * contains the date, using the default date formatting.
 * 
 * @author Frederik Heremans
 * @author Joram Barrez
 */
public class PrettyTimeLabel extends Label {
  
  private static final long serialVersionUID = 1L;
  
  protected String labelTemplate;
  protected Date date;
  protected String noDateCaption;
  protected boolean showTime;
  
  public PrettyTimeLabel(Date date, boolean showTime) {
    this(date, "",  showTime);
  }
  
  public PrettyTimeLabel(Date date, String noDateCaption,  boolean showTime) {
   this(null, date, noDateCaption, showTime);
  }
  

  /**
   * Constructor for pretty time label.
   * 
   * @param labelTemplate
   *          template to use for date, eg. "Date: {0}". Null, if date/time
   *          should just be shown.
   * @param date
   *          to show
   * @param noDateCaption
   *          caption of label to show when dat is null. Empty label is shown
   *          when null.
   */
  public PrettyTimeLabel(String labelTemplate, Date date, String noDateCaption, boolean showTime) {
    this.labelTemplate = labelTemplate;
    this.date = date;
    this.noDateCaption = noDateCaption;
    this.showTime = showTime;
    
    init();
  }
  
  protected void init() {
    
    final I18nManager i18nManager = ExplorerApp.get().getI18nManager();
    if (date != null) {
      DateFormat dateFormat = null;
      if(showTime) {
        dateFormat = new SimpleDateFormat(Constants.DEFAULT_TIME_FORMAT);
      } else {
        dateFormat = new SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT);
      }
      
      if(labelTemplate != null) {
        super.setValue(MessageFormat.format(labelTemplate, new HumanTime(i18nManager).format(date)));
      } else {
        super.setValue(new HumanTime(i18nManager).format(date));
      }
      setDescription(dateFormat.format(date));
    } else {
      super.setValue(noDateCaption);
      setDescription(noDateCaption);
    }
  }
  
  @Override
  public void setValue(Object newValue) {
    if (newValue instanceof Date) {
      date = (Date) newValue;
      init();
    } else if (newValue instanceof String) {
      date = null;
      init();
    } else {
      throw new IllegalArgumentException("Can only set " + Date.class + " as new value for prettyTimeLabel");
    }
  }
  
}
