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
package com.activiti.service.activiti;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.activiti.domain.ServerConfig;
import com.activiti.service.activiti.exception.ActivitiServiceException;
import com.activiti.web.rest.exception.BadRequestException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Service for invoking Activiti REST services.
 */
@Service
public class ProcessInstanceService {

    private final Logger log = LoggerFactory.getLogger(ProcessInstanceService.class);

    public static final String HISTORIC_PROCESS_INSTANCE_URL = "history/historic-process-instances/{0}";
    public static final String HISTORIC_TASK_LIST_URL = "history/historic-task-instances";
    public static final String HISTORIC_VARIABLE_INSTANCE_LIST_URL = "history/historic-variable-instances";
    public static final String HISTORIC_ACTIVITY_INSTANCE_LIST_URL = "history/historic-activity-instances";
    public static final String CURRENT_ACTIVITY_INSTANCE_LIST_URL = "runtime/executions/{0}/activities";
    public static final String RUNTIME_PROCESS_INSTANCE_URL = "runtime/process-instances/{0}";
    public static final String RUNTIME_PROCESS_INSTANCE_VARIABLES = "runtime/process-instances/{0}/variables";
    public static final String RUNTIME_PROCESS_INSTANCE_VARIABLE_URL = "runtime/process-instances/{0}/variables/{1}";

    private static final String DEFAULT_SUBTASK_RESULT_SIZE = "1024";
    private static final String DEFAULT_ACTIVITY_SIZE = "1024";
    private static final String DEFAULT_PROCESSINSTANCE_SIZE = "100";
    private static final String DEFAULT_VARIABLE_RESULT_SIZE = "1024";

    @Autowired
    protected ActivitiClientService clientUtil;

	@Autowired
	protected JobService jobService;

	@Autowired
	protected ObjectMapper objectMapper;

	public JsonNode listProcesInstances(ObjectNode bodyNode, ServerConfig serverConfig) {
		JsonNode resultNode = null;
		try {
			URIBuilder builder = new URIBuilder("query/historic-process-instances");

			String uri = clientUtil.getUriWithPagingAndOrderParameters(builder, bodyNode);
			HttpPost post = clientUtil.createPost(uri, serverConfig);

			post.setEntity(clientUtil.createStringEntity(bodyNode.toString()));
			resultNode = clientUtil.executeRequest(post, serverConfig);
		} catch (Exception e) {
			throw new ActivitiServiceException(e.getMessage(), e);
		}
		return resultNode;
	}

    public JsonNode listProcesInstancesForProcessDefinition(ObjectNode bodyNode, ServerConfig serverConfig) {
        JsonNode resultNode = null;
        try {
            URIBuilder builder = new URIBuilder("query/historic-process-instances");

            builder.addParameter("size", DEFAULT_PROCESSINSTANCE_SIZE);
            builder.addParameter("sort", "startTime");
            builder.addParameter("order", "desc");

            String uri = clientUtil.getUriWithPagingAndOrderParameters(builder, bodyNode);
            HttpPost post = clientUtil.createPost(uri, serverConfig);

            post.setEntity(clientUtil.createStringEntity(bodyNode.toString()));
            resultNode = clientUtil.executeRequest(post, serverConfig);
        } catch (Exception e) {
            throw new ActivitiServiceException(e.getMessage(), e);
        }
        return resultNode;
    }

	public JsonNode getProcessInstance(ServerConfig serverConfig, String processInstanceId) {
		if (processInstanceId == null) {
			throw new IllegalArgumentException("Process instance id is required");
		}

		URIBuilder builder = clientUtil.createUriBuilder(MessageFormat.format(HISTORIC_PROCESS_INSTANCE_URL, processInstanceId));
		HttpGet get = new HttpGet(clientUtil.getServerUrl(serverConfig, builder));
		return clientUtil.executeRequest(get, serverConfig);
	}

	public JsonNode getTasks(ServerConfig serverConfig, String processInstanceId) {
		URIBuilder builder = clientUtil.createUriBuilder(HISTORIC_TASK_LIST_URL);
		builder.addParameter("processInstanceId", processInstanceId);
		builder.addParameter("size", DEFAULT_SUBTASK_RESULT_SIZE);

		HttpGet get = new HttpGet(clientUtil.getServerUrl(serverConfig, builder));
		return clientUtil.executeRequest(get, serverConfig);
	}

	public JsonNode getVariables(ServerConfig serverConfig, String processInstanceId) {
        URIBuilder builder = clientUtil.createUriBuilder(HISTORIC_VARIABLE_INSTANCE_LIST_URL);
        builder.addParameter("processInstanceId", processInstanceId);
        builder.addParameter("size", DEFAULT_VARIABLE_RESULT_SIZE);
        builder.addParameter("sort", "variableName");
		HttpGet get = new HttpGet(clientUtil.getServerUrl(serverConfig, builder));
		return clientUtil.executeRequest(get, serverConfig);
	}

    public void updateVariable(ServerConfig serverConfig, String processInstanceId, String variableName, ObjectNode objectNode) {
        URIBuilder builder = clientUtil.createUriBuilder(MessageFormat.format(RUNTIME_PROCESS_INSTANCE_VARIABLE_URL, processInstanceId, variableName));
        HttpPut put = clientUtil.createPut(builder, serverConfig);
        put.setEntity(clientUtil.createStringEntity(objectNode.toString()));
        clientUtil.executeRequest(put, serverConfig);
    }

    public void createVariable(ServerConfig serverConfig, String processInstanceId, ObjectNode objectNode) {
        URIBuilder builder = clientUtil.createUriBuilder(MessageFormat.format(RUNTIME_PROCESS_INSTANCE_VARIABLES, processInstanceId));
        HttpPost post = clientUtil.createPost(builder, serverConfig);
        ArrayNode variablesNode = objectMapper.createArrayNode();
        variablesNode.add(objectNode);

        post.setEntity(clientUtil.createStringEntity(variablesNode.toString()));
        clientUtil.executeRequest(post, serverConfig, 201);
    }

    public void deleteVariable(ServerConfig serverConfig, String processInstanceId, String variableName) {
        URIBuilder builder = clientUtil.createUriBuilder(MessageFormat.format(RUNTIME_PROCESS_INSTANCE_VARIABLE_URL, processInstanceId, variableName));
        HttpDelete delete = clientUtil.createDelete(builder, serverConfig);
        clientUtil.executeRequestNoResponseBody(delete, serverConfig, 204);
    }

	public void executeAction(ServerConfig serverConfig, String processInstanceId, JsonNode actionBody) throws ActivitiServiceException {
		boolean validAction = false;

		if (actionBody.has("action")) {
			String action = actionBody.get("action").asText();

			if ("delete".equals(action)) {
				validAction = true;

				// Delete historic instance
				URIBuilder builder = clientUtil.createUriBuilder(MessageFormat.format(HISTORIC_PROCESS_INSTANCE_URL, processInstanceId));
				HttpDelete delete = new HttpDelete(clientUtil.getServerUrl(serverConfig, builder));
				clientUtil.executeRequestNoResponseBody(delete, serverConfig, HttpStatus.SC_OK);

			} else if ("terminate".equals(action)) {
				validAction = true;

				// Delete runtime instance
				URIBuilder builder = clientUtil.createUriBuilder(MessageFormat.format(RUNTIME_PROCESS_INSTANCE_URL, processInstanceId));
				if (actionBody.has("deleteReason")) {
					builder.addParameter("deleteReason", actionBody.get("deleteReason").asText());
				}

				HttpDelete delete = new HttpDelete(clientUtil.getServerUrl(serverConfig, builder));
				clientUtil.executeRequestNoResponseBody(delete, serverConfig, HttpStatus.SC_NO_CONTENT);
			}
		}

		if(!validAction) {
			throw new BadRequestException("Action is missing in the request body or the given action is not supported.");
		}
	}

	public JsonNode getSubProcesses(ServerConfig serverConfig, String processInstanceId) {
		ObjectNode requestNode = objectMapper.createObjectNode();
		requestNode.put("superProcessInstanceId", processInstanceId);
		return listProcesInstances(requestNode, serverConfig);
	}

	public JsonNode getJobs(ServerConfig serverConfig, String processInstanceId) {
		return jobService.listJobs(serverConfig, Collections.singletonMap("processInstanceId", new String[] {processInstanceId}));
	}

	public List<String> getCompletedActivityInstancesAndProcessDefinitionId(ServerConfig serverConfig, String processInstanceId) {
		URIBuilder builder = clientUtil.createUriBuilder(HISTORIC_ACTIVITY_INSTANCE_LIST_URL);
		builder.addParameter("processInstanceId", processInstanceId);
		//builder.addParameter("finished", "true");
		builder.addParameter("sort", "startTime");
		builder.addParameter("order", "asc");
		builder.addParameter("size", DEFAULT_ACTIVITY_SIZE);

		HttpGet get = new HttpGet(clientUtil.getServerUrl(serverConfig, builder));
		JsonNode node = clientUtil.executeRequest(get, serverConfig);

		List<String> result = new ArrayList<String>();
		if (node.has("data") && node.get("data").isArray()) {
			ArrayNode data = (ArrayNode) node.get("data");
			ObjectNode activity = null;
			for (int i=0; i < data.size(); i++) {
				activity = (ObjectNode) data.get(i);
				if (activity.has("activityType")) {
					result.add(activity.get("activityId").asText());
				}
			}
		}

		return result;
	}

	public List<String> getCurrentActivityInstances(ServerConfig serverConfig, String processInstanceId) {
		URIBuilder builder = clientUtil.createUriBuilder(MessageFormat.format(CURRENT_ACTIVITY_INSTANCE_LIST_URL, processInstanceId));
		HttpGet get = new HttpGet(clientUtil.getServerUrl(serverConfig, builder));
		JsonNode node = clientUtil.executeRequest(get, serverConfig);

		List<String> result = new ArrayList<String>();
		if (node.isArray()) {
			ArrayNode data = (ArrayNode) node;
			for (int i=0; i < data.size(); i++) {
				if (data.get(i) != null) {
					result.add(data.get(i).asText());
				}
			}
		}
		return result;
	}
}
