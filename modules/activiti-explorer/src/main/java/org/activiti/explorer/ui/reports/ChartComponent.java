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

import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

/**
 * @author Joram Barrez
 */
public class ChartComponent extends VerticalLayout {

  private static final long serialVersionUID = 1L;

  public ChartComponent(String title) {

    if (title != null) {
      Label label = new Label(title);
      label.addStyleName(Reindeer.LABEL_H2);
      addComponent(label);
    }

  }

  public void addChart(String description, Component chart, String errorMessage) {

    addComponent(new Label("&nbsp;", Label.CONTENT_XHTML));
    addComponent(new Label("&nbsp;", Label.CONTENT_XHTML));

    // Description
    if (description != null) {
      Label label = new Label(description);
      label.addStyleName(Reindeer.LABEL_H2);
      addComponent(label);

      addComponent(new Label("&nbsp;", Label.CONTENT_XHTML));
    }

    // Chart
    if (chart != null) {
      if (chart instanceof DCharts) {
        // DCharts doesn't know how to size itself
        chart.setWidth(600, UNITS_PIXELS);
        chart.setHeight(450, UNITS_PIXELS);
        ((DCharts) chart).show();
      }
      addComponent(chart);
    }

    // Error message
    if (errorMessage != null) {
      Label errorLabel = new Label(errorMessage);
      addComponent(errorLabel);
    }
  }

}
