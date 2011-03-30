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

import java.text.DateFormat;
import java.util.Date;

import org.activiti.explorer.Constant;

import com.ocpsoft.pretty.time.PrettyTime;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;


/**
 * Column generator that renders a simple label with a pretty time
 * for a date-property. The item property that is used, is determined by
 * the property ID you pass when adding this column generator to a table.
 * 
 * @author Frederik Heremans
 */
public class PrettyTimeColumnGenerator implements ColumnGenerator {
  
  private static final long serialVersionUID = 5839316455889287370L;
  
  private String noDateCaption;
  private DateFormat dateFormat;
  
  public PrettyTimeColumnGenerator() {
    this("");
  }
  
  public PrettyTimeColumnGenerator(String noDateCaption) {
    this.noDateCaption = noDateCaption;
    this.dateFormat = (DateFormat) Constant.DEFAULT_DATE_FORMATTER.clone();
  }

  public Component generateCell(Table source, Object itemId, Object columnId) {
    Item item = source.getItem(itemId);
    Date value = null;
    if (item != null) {
      // Get item property
      Property dateProperty = item.getItemProperty(columnId);
      if (dateProperty != null && Date.class.equals(dateProperty.getType())) {
        value = (Date) dateProperty.getValue();
      }
    }
    Label prettyTimeLabel = new Label();
    prettyTimeLabel.setSizeUndefined();
    
    if (value != null) {
      prettyTimeLabel.setValue(new PrettyTime().format(value));
      prettyTimeLabel.setDescription(dateFormat.format(value));
    } else {
      prettyTimeLabel.setValue(noDateCaption);
      prettyTimeLabel.setDescription(noDateCaption);
    }
    return prettyTimeLabel;
  }
}
