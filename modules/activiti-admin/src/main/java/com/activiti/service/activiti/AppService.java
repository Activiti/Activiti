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

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.activiti.domain.ServerConfig;
import com.activiti.service.AttachmentResponseInfo;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Service for invoking Activiti REST services.
 */
@Service
public class AppService {

    public static final String APP_LIST_URL = "enterprise/runtime-app-deployments";
    public static final String APP_URL = "enterprise/runtime-app-deployments/{0}";
    public static final String PROCESS_DEFINITIONS_URL = "enterprise/process-definitions";
    public static final String DECISION_TABLES_URL = "enterprise/decisions/decision-tables";
    public static final String FORMS_URL = "enterprise/forms";
    public static final String APP_IMPORT_AND_PUBLISH_URL = "enterprise/app-definitions/publish-app";
    public static final String APP_IMPORT_AND_PUBLISH_AS_NEW_VERSION_URL = "/enterprise/app-definitions/{0}/publish-app";
    public static final String EXPORT_DEPLOYED_APP_URL = "enterprise/export-app-deployment/{0}";
    public static final String EXPORT_APP_URL = "enterprise/app-definitions/{0}/export";
    public static final String APP_BY_DEPLOYMENT_URL = "enterprise/runtime-app-deployment";
    
    @Autowired
    protected ActivitiClientService clientUtil;

    public JsonNode listAppDefinitions(ServerConfig serverConfig, Map<String, String[]> parameterMap) {
        URIBuilder builder =  clientUtil.createUriBuilder(APP_LIST_URL);
        addParametersToBuilder(builder, parameterMap);
        
        HttpGet get = new HttpGet(clientUtil.getServerUrl(serverConfig, builder));
        return clientUtil.executeRequest(get, serverConfig);
    }

    public JsonNode getAppDefinition(ServerConfig serverConfig, String appDeploymentId) {
        HttpGet get = new HttpGet(clientUtil.getServerUrl(serverConfig, clientUtil.createUriBuilder(MessageFormat.format(APP_URL, appDeploymentId))));
        return clientUtil.executeRequest(get, serverConfig);
    }
    
    public void getAppDefinitionByDeployment(ServerConfig serverConfig, HttpServletResponse httpResponse, Map<String, String[]> parameterMap) {
        URIBuilder builder =  clientUtil.createUriBuilder(APP_BY_DEPLOYMENT_URL);
        addParametersToBuilder(builder, parameterMap);
        
        HttpGet get = new HttpGet(clientUtil.getServerUrl(serverConfig, builder));
        clientUtil.execute(get, httpResponse, serverConfig);
    }
    
    public void deleteAppDeployment(ServerConfig serverConfig, HttpServletResponse httpResponse, String appDeploymentId) {
        HttpDelete delete = new HttpDelete(clientUtil.getServerUrl(serverConfig, clientUtil.createUriBuilder(MessageFormat.format(APP_URL, appDeploymentId))));
        clientUtil.execute(delete, httpResponse, serverConfig);
    }

    public JsonNode getProcessDefinitionsForDeploymentId(ServerConfig serverConfig, String deploymentId) {
        URIBuilder builder =  clientUtil.createUriBuilder(PROCESS_DEFINITIONS_URL);
        builder.addParameter("deploymentId", deploymentId);
        HttpGet get = new HttpGet(clientUtil.getServerUrl(serverConfig, builder));
        return clientUtil.executeRequest(get, serverConfig);
    }

    public JsonNode getDecisionDefinitionsForDeploymentId(ServerConfig serverConfig, String dmnDeploymentId) {
        URIBuilder builder =  clientUtil.createUriBuilder(DECISION_TABLES_URL);
        builder.addParameter("deploymentId", dmnDeploymentId);
        HttpGet get = new HttpGet(clientUtil.getServerUrl(serverConfig, builder));
        return clientUtil.executeRequest(get, serverConfig);
    }

    public JsonNode getFormsForAppDeploymentId(ServerConfig serverConfig, String appDeploymentId) {
        URIBuilder builder =  clientUtil.createUriBuilder(FORMS_URL);
        builder.addParameter("appDeploymentId", appDeploymentId);
        HttpGet get = new HttpGet(clientUtil.getServerUrl(serverConfig, builder));
        return clientUtil.executeRequest(get, serverConfig);
    }

    public void uploadAppDefinition(HttpServletResponse httpResponse, ServerConfig serverConfig, String name, InputStream inputStream) throws IOException {
        uploadAppDefinition(httpResponse, serverConfig, name, IOUtils.toByteArray(inputStream));
    }

    
    public JsonNode exportApp(ServerConfig serverConfig, String deploymentId, HttpServletResponse httpResponse) throws IOException {
        URIBuilder builder =  clientUtil.createUriBuilder(MessageFormat.format(EXPORT_DEPLOYED_APP_URL, deploymentId));
        HttpGet get = new HttpGet(clientUtil.getServerUrl(serverConfig, builder));
        return clientUtil.executeDownloadRequest(get, httpResponse, serverConfig);
    }
    
    public JsonNode redeployApp(HttpServletResponse httpResponse, ServerConfig serverConfig, ServerConfig targetServerConfig, String deploymentId) throws IOException {
        URIBuilder builder =  clientUtil.createUriBuilder(MessageFormat.format(EXPORT_DEPLOYED_APP_URL, deploymentId));
        HttpGet get = new HttpGet(clientUtil.getServerUrl(serverConfig, builder));
        AttachmentResponseInfo attachmentResponseInfo = clientUtil.executeDownloadRequest(get, serverConfig, 200, 404);
        if (attachmentResponseInfo.isSuccess()) {
            uploadAppDefinition(httpResponse, targetServerConfig, attachmentResponseInfo.getFileName(), attachmentResponseInfo.getBytes());
            return null;
        } else {
            httpResponse.setStatus(attachmentResponseInfo.getStatusCode());
            return attachmentResponseInfo.getContent();
        }
    }
    
    public JsonNode redeployReplaceApp(HttpServletResponse httpResponse, ServerConfig serverConfig, ServerConfig targetServerConfig, String deploymentId, String appId) throws IOException {
        URIBuilder builder =  clientUtil.createUriBuilder(MessageFormat.format(EXPORT_DEPLOYED_APP_URL, deploymentId));
        HttpGet get = new HttpGet(clientUtil.getServerUrl(serverConfig, builder));
        AttachmentResponseInfo attachmentResponseInfo = clientUtil.executeDownloadRequest(get, serverConfig, 200, 404);
        if (attachmentResponseInfo.isSuccess()) {
            uploadNewAppDefinitionVersion(httpResponse, targetServerConfig, attachmentResponseInfo.getFileName(), attachmentResponseInfo.getBytes(), appId);
            return null;
        } else {
            httpResponse.setStatus(attachmentResponseInfo.getStatusCode());
            return attachmentResponseInfo.getContent();
        }
    }
    
    protected void uploadAppDefinition(HttpServletResponse httpResponse, ServerConfig serverConfig, String name, byte[] bytes) throws IOException {
        HttpPost post = new HttpPost(clientUtil.getServerUrl(serverConfig, APP_IMPORT_AND_PUBLISH_URL));
        HttpEntity reqEntity = MultipartEntityBuilder.create()
                .addBinaryBody("file", bytes, ContentType.APPLICATION_OCTET_STREAM, name).build();
        post.setEntity(reqEntity);
        clientUtil.execute(post, httpResponse, serverConfig);
    }
    
    protected void uploadNewAppDefinitionVersion(HttpServletResponse httpResponse, ServerConfig serverConfig, String name, byte[] bytes, String appId) throws IOException {
        URIBuilder builder =  clientUtil.createUriBuilder(MessageFormat.format(APP_IMPORT_AND_PUBLISH_AS_NEW_VERSION_URL, appId));
        HttpPost post = new HttpPost(clientUtil.getServerUrl(serverConfig, builder));
        HttpEntity reqEntity = MultipartEntityBuilder.create()
                .addBinaryBody("file", bytes, ContentType.APPLICATION_OCTET_STREAM, name).build();
        post.setEntity(reqEntity);
        clientUtil.execute(post, httpResponse, serverConfig);
    }
    
    protected void addParametersToBuilder(URIBuilder builder, Map<String, String[]> parameterMap) {
        for (String name : parameterMap.keySet()) {
            builder.addParameter(name, parameterMap.get(name)[0]);
        }
    }
}
