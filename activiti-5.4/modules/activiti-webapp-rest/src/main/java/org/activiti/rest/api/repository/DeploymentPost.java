package org.activiti.rest.api.repository;

import java.util.Map;
import java.util.zip.ZipInputStream;

import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.rest.util.ActivitiRequest;
import org.activiti.rest.util.ActivitiWebScript;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.servlet.FormData;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;

public class DeploymentPost extends ActivitiWebScript {

  /**
   * Deletes the deployment.
   * 
   * @param req
   *          The webscripts request
   * @param status
   *          The webscripts status
   * @param cache
   *          The webscript cache
   * @param model
   *          The webscripts template model
   */
  @Override
  protected void executeWebScript(ActivitiRequest req, Status status, Cache cache, Map<String, Object> model) {
    try {
      // Make sure to set response header: "X-XSS-Protection" 0
      FormData.FormField file = ((WebScriptServletRequest) req.getWebScriptRequest()).getFileField("deployment");
      DeploymentBuilder deploymentBuilder = getRepositoryService().createDeployment();
      String fileName = file.getFilename();
      if (fileName.endsWith(".bpmn20.xml")) {
        deploymentBuilder.addInputStream(fileName, file.getInputStream());
      } else if (fileName.endsWith(".bar") || fileName.endsWith(".zip")) {
        deploymentBuilder.addZipInputStream(new ZipInputStream(file.getInputStream()));
      } else {
        throw new WebScriptException(Status.STATUS_BAD_REQUEST, "File must be of type .bpmn20.xml, .bar or .zip");
      }
      deploymentBuilder.name(fileName);
      deploymentBuilder.deploy();
      String success = req.getBody().getString("success");
      if (success != null) {
        model.put("success", success);
      }
    } catch (Exception e) {
      String failure = req.getString("failure", null);
      if (failure == null) {
        failure = req.getBody().getString("failure");
      }
      if (failure != null) {
        model.put("failure", failure);
        model.put("error", e.getMessage());
      } else if (e instanceof WebScriptException) {
        throw (WebScriptException) e;
      } else {
        throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, e.getMessage(), e);
      }
    }
  }
}
