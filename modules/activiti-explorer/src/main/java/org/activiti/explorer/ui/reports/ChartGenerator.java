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
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;
import org.dussan.vaadin.dcharts.DCharts;
import org.dussan.vaadin.dcharts.base.elements.XYaxis;
import org.dussan.vaadin.dcharts.base.elements.XYseries;
import org.dussan.vaadin.dcharts.base.renderers.MarkerRenderer;
import org.dussan.vaadin.dcharts.data.DataSeries;
import org.dussan.vaadin.dcharts.data.Ticks;
import org.dussan.vaadin.dcharts.metadata.LegendPlacements;
import org.dussan.vaadin.dcharts.metadata.XYaxes;
import org.dussan.vaadin.dcharts.metadata.renderers.AxisRenderers;
import org.dussan.vaadin.dcharts.metadata.renderers.LabelRenderers;
import org.dussan.vaadin.dcharts.metadata.renderers.SeriesRenderers;
import org.dussan.vaadin.dcharts.metadata.styles.MarkerStyles;
import org.dussan.vaadin.dcharts.options.Axes;
import org.dussan.vaadin.dcharts.options.AxesDefaults;
import org.dussan.vaadin.dcharts.options.Highlighter;
import org.dussan.vaadin.dcharts.options.Legend;
import org.dussan.vaadin.dcharts.options.Options;
import org.dussan.vaadin.dcharts.options.Series;
import org.dussan.vaadin.dcharts.options.SeriesDefaults;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;

/**
 * @author Joram Barrez
 */
public class ChartGenerator {
  
  public static final String CHART_TYPE_BAR_CHART = "barChart";
  public static final String CHART_TYPE_PIE_CHART = "pieChart";
  public static final String CHART_TYPE_LINE_CHART = "lineChart";
  public static final String CHART_TYPE_LIST = "list";

  public static ChartComponent generateChart(byte[] reportData) {
    
    // Convert json to pojo
    JsonNode jsonNode = convert(reportData);
    
    // Title
    JsonNode titleNode = jsonNode.get("title");
    String title = null;
    if (titleNode != null) {
      title = titleNode.textValue();
    }
    
    ChartComponent chartComponent = new ChartComponent(title);
    
    
    // Retrieve data sets
    JsonNode datasetsNode = jsonNode.get("datasets");
    
    // If no data was returned
    if (datasetsNode.size() == 0) {
      chartComponent.addChart(null, null,  ExplorerApp.get().getI18nManager().getMessage(Messages.REPORTING_ERROR_NOT_ENOUGH_DATA));
      return chartComponent;
    }
    
    if (datasetsNode != null && datasetsNode.isArray()) {
      
      Iterator<JsonNode> dataIterator = datasetsNode.iterator();
      while (dataIterator.hasNext()) {
        
        JsonNode datasetNode = dataIterator.next();
        
        JsonNode descriptionNode = datasetNode.get("description");
        String description = null;
        if (descriptionNode != null) {
          description = descriptionNode.textValue();
        }
        JsonNode dataNode = datasetNode.get("data");
        
        if (dataNode == null || dataNode.size() == 0) {
          chartComponent.addChart(description, null, ExplorerApp.get().getI18nManager().getMessage(Messages.REPORTING_ERROR_NOT_ENOUGH_DATA));
        } else {
        
          String[] names = new String[dataNode.size()];
          Number[] values = new Number[dataNode.size()];
          
          int index = 0;
          Iterator<String> fieldIterator = dataNode.fieldNames();
          while(fieldIterator.hasNext()) {
            String field = fieldIterator.next();
            names[index] = field;
            values[index] = dataNode.get(field).numberValue();
            index++;
          }
          
          // Generate chart (or 'no data' message)
          if (names.length > 0) {
            Component chart = createChart(datasetNode, names, values);
            chartComponent.addChart(description, chart, null);
          } else {
            chartComponent.addChart(description, null, ExplorerApp.get().getI18nManager().getMessage(Messages.REPORTING_ERROR_NOT_ENOUGH_DATA));
          }
          
        }
        
      }
      
    }
    
    
    return chartComponent;
  }

  protected static Component createChart(JsonNode dataNode, String[] names, Number[] values) {
    String type = dataNode.get("type").textValue();
    
    JsonNode xAxisNode = dataNode.get("xaxis");
    String xAxis = null;
    if (xAxisNode != null) {
      xAxis = xAxisNode.textValue();
    }
    
    JsonNode yAxisNode = dataNode.get("yaxis");
    String yAxis = null;
    if (yAxisNode != null) {
      yAxis = yAxisNode.textValue();
    }
    
    Component chart = null;
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
      
      Legend legend = new Legend().setShow(true).setPlacement(LegendPlacements.INSIDE);
      options.setLegend(legend);
      
      Highlighter highlighter = new Highlighter().setShow(true);
      options.setHighlighter(highlighter);
      
      chart = new DCharts().setDataSeries(dataSeries).setOptions(options);
      
    } else if (CHART_TYPE_LINE_CHART.equals(type)) {

      AxesDefaults axesDefaults = new AxesDefaults().setLabelRenderer(LabelRenderers.CANVAS);
      Axes axes = new Axes()
        .addAxis(new XYaxis().setLabel(xAxis != null ? xAxis : "").setMin(names[0]).setMax(names[values.length - 1]).setDrawMajorTickMarks(true))
        .addAxis(new XYaxis(XYaxes.Y).setLabel(yAxis != null ? yAxis : "").setDrawMajorTickMarks(true));
      Options options = new Options().setAxesDefaults(axesDefaults).setAxes(axes);
      DataSeries dataSeries = new DataSeries().newSeries();
      for (int i=0; i<names.length; i++) {
       
//        if (parseLong(names[i]) != null) {
//          dataSeries.add(parseLong(names[i]), values[i]);
//        } else if (parseDouble(names[i]) != null) {
//          dataSeries.add(parseDouble(names[i]), values[i]);
//        } else {
//          dataSeries.add(names[i], values[i]);
//        }
        
        dataSeries.add(names[i], values[i]);
        
      } 
      
      Series series = new Series().addSeries(
              new XYseries().setShowLine(true).setMarkerOptions(new MarkerRenderer().setShadow(true).setSize(7).setStyle(MarkerStyles.CIRCLE)));
      options.setSeries(series);
      
      options.setAnimate(true);
      options.setAnimateReplot(true);
      
      Highlighter highlighter = new Highlighter().setShow(true);
      options.setHighlighter(highlighter);
      
      chart = new DCharts().setDataSeries(dataSeries).setOptions(options);
      
    } else if (CHART_TYPE_LIST.equals(type)) {
      
      GridLayout grid = new GridLayout(2, names.length);
      grid.setSpacing(true);
      
      for (int i=0; i<names.length; i++) {
        String name = names[i];
        Label nameLabel = new Label(name);
        nameLabel.addStyleName(ExplorerLayout.STYLE_LABEL_BOLD);
        grid.addComponent(nameLabel, 0, i);
        
        Number value = values[i];
        Label valueLabel = new Label(value + "");
        grid.addComponent(valueLabel, 1, i);
      }
      
      chart = grid;
      
    }
    
    if (chart instanceof DCharts) {
      // Needed, otherwise the chart will not be shown
      ((DCharts) chart).show();
    }
    
    return chart;
  }
  
  protected static JsonNode convert(byte[] jsonBytes) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      return objectMapper.readTree(jsonBytes);
    } catch (Exception e) {
      throw new ActivitiException("Report dataset contains invalid json", e);
    }
  }
  
  protected static Long parseLong(String s) {
    try {
      Long value = Long.parseLong(s);
      return value;
    } catch (NumberFormatException e) {
      return null;
    }
  }
  
  protected static Double parseDouble(String s) {
    try {
      Double value = Double.parseDouble(s);
      return value;
    } catch (NumberFormatException e) {
      return null;
    }
  }
  
}
