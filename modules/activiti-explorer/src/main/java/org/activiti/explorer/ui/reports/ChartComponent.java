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
package org.activiti.explorer.ui.reports;

import org.dussan.vaadin.dcharts.DCharts;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


/**
 * @author Joram Barrez
 */
public class ChartComponent extends VerticalLayout {
  
  private static final long serialVersionUID = 1L;

  public ChartComponent(String description, DCharts dCharts) {
    
    addComponent(new Label("&nbsp;", Label.CONTENT_XHTML));
    addComponent(new Label("&nbsp;", Label.CONTENT_XHTML));
    
    // Description
    Label label = new Label(description);
    label.addStyleName(Reindeer.LABEL_H2);
    addComponent(label);
    
    addComponent(new Label("&nbsp;", Label.CONTENT_XHTML));
    
    // Chart
    dCharts.setWidth(600, UNITS_PIXELS);
    dCharts.setHeight(500, UNITS_PIXELS);
    addComponent(dCharts);
    dCharts.show();
  }

}
