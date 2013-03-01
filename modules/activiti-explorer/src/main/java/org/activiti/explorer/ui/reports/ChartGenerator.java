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

import java.util.Iterator;

import org.activiti.engine.ActivitiException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.dussan.vaadin.dcharts.DCharts;
import org.dussan.vaadin.dcharts.base.elements.XYaxis;
import org.dussan.vaadin.dcharts.data.DataSeries;
import org.dussan.vaadin.dcharts.data.Ticks;
import org.dussan.vaadin.dcharts.metadata.renderers.AxisRenderers;
import org.dussan.vaadin.dcharts.metadata.renderers.SeriesRenderers;
import org.dussan.vaadin.dcharts.options.Axes;
import org.dussan.vaadin.dcharts.options.Highlighter;
import org.dussan.vaadin.dcharts.options.Options;
import org.dussan.vaadin.dcharts.options.SeriesDefaults;

/**
 * @author Joram Barrez
 */
public class ChartGenerator {
  
  public static final String CHART_TYPE_BAR_CHART = "barChart";
  public static final String CHART_TYPE_PIE_CHART = "pieChart";

  public static ChartComponent generateChart(byte[] reportData) {
    
    // Convert json to pojo
    JsonNode jsonNode = convert(reportData);
    JsonNode dataNode = jsonNode.get("data");
    
    // Retrieve data
    String description = jsonNode.get("description").getTextValue();
    String[] names = new String[dataNode.size()];
    Number[] values = new Number[dataNode.size()];
    
    int index = 0;
    Iterator<String> fieldIterator = dataNode.getFieldNames();
    while(fieldIterator.hasNext()) {
      String field = fieldIterator.next();
      names[index] = field;
      values[index] = dataNode.get(field).getNumberValue();
      index++;
    }
    
    // Create chart
    String type = jsonNode.get("type").getTextValue();
    DCharts chart = null;
    if (CHART_TYPE_BAR_CHART.equals(type)) {
      
      DataSeries dataSeries = new DataSeries().add((Object[]) values);
      SeriesDefaults seriesDefaults = new SeriesDefaults().setRenderer(SeriesRenderers.BAR);
      Axes axes = new Axes().addAxis(new XYaxis().setRenderer(AxisRenderers.CATEGORY).setTicks(new Ticks().add((Object[]) names)));
      Highlighter highlighter = new Highlighter().setShow(false);
      Options options = new Options().setSeriesDefaults(seriesDefaults).setAxes(axes).setHighlighter(highlighter);
      options.setAnimate(true);
      options.setAnimateReplot(true);
      chart = new DCharts().setDataSeries(dataSeries).setOptions(options);
      
    } else if(CHART_TYPE_PIE_CHART.equals(type)) {
      
      DataSeries dataSeries = new DataSeries().newSeries();
      for (int i=0; i<names.length; i++) {
        dataSeries.add(names[i], values[i]);
      }
      SeriesDefaults seriesDefaults = new SeriesDefaults().setRenderer(SeriesRenderers.PIE);
      Options options = new Options().setSeriesDefaults(seriesDefaults);
      options.setAnimate(true);
      options.setAnimateReplot(true);
      chart = new DCharts().setDataSeries(dataSeries).setOptions(options);
    }
    
    
    return new ChartComponent(description, chart);
  }
  
  protected static JsonNode convert(byte[] jsonBytes) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      return objectMapper.readTree(jsonBytes);
    } catch (Exception e) {
      throw new ActivitiException("Report dataset contains invalid json", e);
    }
  }
  
  
  
}
