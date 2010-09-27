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
package org.activiti.rest.util;

import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil;
import org.activiti.engine.*;
import org.activiti.rest.Config;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.Collection;
import java.util.Date;

/**
 * Helper class that wrapps the webscript request to perform methods upon it.
 *
 * @author Erik Winlof and Nils Preusker
 */
public class ActivitiStreamingWebScript extends AbstractWebScript {

  /**
   * The activiti config bean
   */
  protected Config config;

  /**
   * Setter for the activiti config bean
   *
   * @param config The activiti config bean
   */
  public void setConfig(Config config) {
    this.config = config;
  }

  public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
    try {
      // Create activiti request to add heler methods
      ActivitiRequest ar = new ActivitiRequest(req);

      // Set logged in web user as current user in engine api
      getIdentityService().setAuthenticatedUserId(ar.getCurrentUserId());

      // Let implementing webscript do something useful
      executeStreamingWebScript(new ActivitiRequest(req), res);
    }
    finally {
      // Reset the current engine api user
      getIdentityService().setAuthenticatedUserId(null);
    }
  }

  /**
   * Override this method to implement the unique functionality for this webscript
   *
   * @param req The activiti request
   * @param res The webscript response
   */
  protected void executeStreamingWebScript(ActivitiRequest req, WebScriptResponse res) throws IOException {
    // Override to make something useful
  }

  public String getMimeType(InputStream inputStream) {
    return config.getMimeType(inputStream);    
  }

  /**
   * Streams an input stream to the webscript response
   *
   * @param res The response to stream the response to
   * @param inputStream The input stream conatining the data
   * @param modified The date and time when the streamed data was modified
   * @param eTag The cache eTag to use for the data
   * @param attachFileName Will if provided be used as a attach file name header to improve the chances of
   *    being opened by an external program
   * @param mimetype The mimetype of the contents
   * @throws IOException
   */
  protected void streamResponse(WebScriptResponse res, InputStream inputStream,
                                Date modified, String eTag,
                                boolean attach, String attachFileName,
                                String mimetype) throws IOException {
    if (attach) {
      String headerValue = "attachment";
      if (attachFileName != null && attachFileName.length() > 0) {

        headerValue += "; filename=" + attachFileName;
      }

      /**
       * set header based on filename - will force a Save As from the browse if
       * it doesn't recognize it
       * this is better than the default response of the browser trying to
       * display the contents
       */
      res.setHeader("Content-Disposition", headerValue);
    }

    if (mimetype != null) {
      res.setContentType(mimetype);
    }

    // res.setContentEncoding(...);
    // res.setHeader("Content-Length", ...);

    // Set caching
    Cache cache = new Cache();
    cache.setNeverCache(false);
    cache.setMustRevalidate(true);
    cache.setMaxAge(0L);
    cache.setLastModified(modified);
    if (eTag != null) {
      cache.setETag(eTag);
    }
    res.setCache(cache);

    /**
     * get the content and stream directly to the response output stream assuming
     * the repository is capable of streaming in chunks, this should allow large files
     * to be streamed directly to the browser response stream.
     */
    try {
      byte[] buffer = new byte[0xFFFF];
      for (int len; (len = inputStream.read(buffer)) != -1;)
        res.getOutputStream().write(buffer, 0, len);
    } catch (SocketException e) {
      // TODO: client cut the connection, log the message?
    }
  }

  /**
   * Returns the process engine info.
   *
   * @return The process engine info
   */
  protected ProcessEngineInfo getProcessEngineInfo() {
    return ProcessEngines.getProcessEngineInfo(config.getEngine());
  }

  /**
   * Returns the process engine.
   *
   * @return The process engine
   */
  protected ProcessEngine getProcessEngine() {
    return ProcessEngines.getProcessEngine(config.getEngine());
  }

  /**
   * Returns the identity service.
   *
   * @return The identity service
   */
  protected IdentityService getIdentityService() {
    return getProcessEngine().getIdentityService();
  }

  /**
   * Returns the management service.
   *
   * @return The management service.
   */
  protected ManagementService getManagementService() {
    return getProcessEngine().getManagementService();
  }

  /**
   * Returns The process service.
   *
   * @return The process service
   */
  protected RuntimeService getRuntimeService() {
    return getProcessEngine().getRuntimeService();
  }

  /**
   * Returns The repository service.
   *
   * @return The repository service
   */
  protected RepositoryService getRepositoryService() {
    return getProcessEngine().getRepositoryService();
  }

  /**
   * Returns the task service.
   *
   * @return The task service
   */
  protected TaskService getTaskService() {
    return getProcessEngine().getTaskService();
  }


}
