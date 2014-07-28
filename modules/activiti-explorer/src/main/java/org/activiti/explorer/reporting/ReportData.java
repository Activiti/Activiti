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
package org.activiti.explorer.reporting;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.ActivitiException;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


/**
 * @author Joram Barrez
 */
public class ReportData {
  
  protected String title;
  
  protected List<Dataset> datasets = new ArrayList<Dataset>();

  public String getTitle() {
    return title;
  }
  
  public void setTitle(String title) {
    this.title = title;
  }

  public List<Dataset> getDatasets() {
    return datasets;
  }
  
  public void setDatasets(List<Dataset> datasets) {
    this.datasets = datasets;
  }
  
  public void addDataset(Dataset dataset) {
    datasets.add(dataset);
  }
  
  public Dataset newDataset() {
    Dataset dataset = new Dataset();
    addDataset(dataset);
    return dataset;
  }
  
  public String toString() {
    try {
      return new String(toBytes(), "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new ActivitiException("Could not convert report data to json", e);
    }
  }
  
  public byte[] toBytes() {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.configure(SerializationFeature.FLUSH_AFTER_WRITE_VALUE, false);
      objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
      objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
      return objectMapper.writeValueAsBytes(this);
    } catch (Exception e) {
      throw new ActivitiException("Could not convert report data to json", e);
    }
  }
  
}
