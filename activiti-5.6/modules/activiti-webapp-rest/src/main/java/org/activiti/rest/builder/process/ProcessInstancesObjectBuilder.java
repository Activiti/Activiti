package org.activiti.rest.builder.process;

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.rest.builder.BaseJSONObjectBuilder;
import org.activiti.rest.util.JSONUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class ProcessInstancesObjectBuilder extends BaseJSONObjectBuilder {

  private HistoricProcessInstanceJSONConverter converter = new HistoricProcessInstanceJSONConverter();

  @SuppressWarnings("unchecked")
  public JSONObject createJsonObject(Object modelObject) throws JSONException {
    Map<String, Object> model = getModelAsMap(modelObject);

    JSONObject result = new JSONObject();
    JSONUtil.putPagingInfo(result, model);

    List<HistoricProcessInstance> definitions = (List<HistoricProcessInstance>) model.get("processInstances");
    JSONArray dataArray = JSONUtil.putNewArray(result, "data");
    for (HistoricProcessInstance processDefinition : definitions) {
      JSONObject jsonTask = converter.getJSONObject(processDefinition);
      dataArray.put(jsonTask);
    }
    return result;
  }
}
