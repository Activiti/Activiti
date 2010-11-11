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
package org.activiti.rest.api.cycle;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.activiti.cycle.ContentRepresentation;
import org.activiti.cycle.CycleDefaultMimeType;
import org.activiti.cycle.CycleService;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryAuthenticationException;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.impl.CycleServiceImpl;
import org.activiti.cycle.impl.conf.PasswordEnabledRepositoryConnectorConfiguration;
import org.activiti.cycle.impl.conf.RepositoryConnectorConfiguration;
import org.activiti.cycle.impl.transform.TransformationException;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.rest.util.ActivitiRequest;
import org.activiti.rest.util.ActivitiStreamingWebScript;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * 
 * @author Nils Preusker (nils.preusker@camunda.com)
 */
public class ContentGet extends ActivitiStreamingWebScript {

  private CycleService cycleService;

  private void init(ActivitiRequest req) {
    String cuid = req.getCurrentUserId();

    HttpSession session = req.getHttpServletRequest().getSession(true);

    // Retrieve the list of configured connectors for the current user (either
    // from the session or, if not present, from the database
    List<RepositoryConnector> connectors = CycleServiceImpl.getConfiguredRepositoryConnectors(cuid, session);

    // Make sure we know username and password for all connectors that require
    // login. If it is not stored in the users configuration it should be
    // provided as a parameter in the request.
    for (RepositoryConnector connector : getPasswordEnabledConnectors(connectors)) {
      Map<String, String> connectorsMap = new HashMap<String, String>();
      
      PasswordEnabledRepositoryConnectorConfiguration conf = (PasswordEnabledRepositoryConnectorConfiguration) connector.getConfiguration();
      String username = req.getString(conf.getId() + "_username");
      String password = req.getString(conf.getId() + "_password");
      if (username != null && password != null) {
        conf.setUser(username);
        conf.setPassword(password);
      } else if (conf.getUser() == null || conf.getPassword() == null) {
        connectorsMap.put(conf.getId(), conf.getName());
      }
      // If one or more logins are missing (not provided in either the
      // configuration or as HTTP parameter) we'll throw an authentication
      // exception with the list of connectors that are missing login
      // information
      if (connectorsMap.size() > 0) {
        throw new RepositoryAuthenticationException("Repository authentication error: missing login", connectorsMap);
      }
    }

    // Initialize the cycleService
    this.cycleService = CycleServiceImpl.getCycleService(cuid, session, connectors);
  }

  private List<RepositoryConnector> getPasswordEnabledConnectors(List<RepositoryConnector> connectors) {
    List<RepositoryConnector> LoginEnabledconnectors = new ArrayList<RepositoryConnector>();
    for (RepositoryConnector connector : connectors) {
      RepositoryConnectorConfiguration conf = connector.getConfiguration();
      if (PasswordEnabledRepositoryConnectorConfiguration.class.isInstance(conf)) {
        LoginEnabledconnectors.add(connector);
      }
    }
    return LoginEnabledconnectors;
  }

  @Override
  protected void executeStreamingWebScript(ActivitiRequest req, WebScriptResponse res) throws IOException {
    init(req);

    // Retrieve the artifactId from the request
    String cnonectorId = req.getMandatoryString("connectorId");
    String artifactId = req.getMandatoryString("artifactId");
    String contentRepresentationId = req.getMandatoryString("contentRepresentationId");

    // Retrieve the artifact from the repository
    RepositoryArtifact artifact = cycleService.getRepositoryArtifact(cnonectorId, artifactId);

    ContentRepresentation contentRepresentation = artifact.getArtifactType().getContentRepresentation(contentRepresentationId);

    String contentType = contentRepresentation.getMimeType().getContentType();
    // assuming we want to create an attachment for binary data...
    boolean attach = contentType.startsWith("application/") ? true : false;

    // TODO: This code should become obsolete when the connectors store the file
    // names properly with suffix.
    String attachmentFileName = null;
    if (attach) {
      attachmentFileName = artifact.getMetadata().getName();

      if (contentType.equals(CycleDefaultMimeType.XML) && !attachmentFileName.endsWith(".xml")) {
        attachmentFileName += ".xml";
      } else if (contentType.equals(CycleDefaultMimeType.JSON) && !attachmentFileName.endsWith(".json")) {
        attachmentFileName += ".json";
      } else if (contentType.equals(CycleDefaultMimeType.TEXT) && !attachmentFileName.endsWith(".txt")) {
        attachmentFileName += ".txt";
      } else if (contentType.equals(CycleDefaultMimeType.PDF) && !attachmentFileName.endsWith(".pdf")) {
        attachmentFileName += ".pdf";
      } else if (contentType.equals(CycleDefaultMimeType.MS_EXCEL) && !attachmentFileName.endsWith(".xls")) {
        attachmentFileName += ".xls";
      } else if (contentType.equals(CycleDefaultMimeType.MS_POWERPOINT) && !attachmentFileName.endsWith(".ppt")) {
        attachmentFileName += ".ppt";
      } else if (contentType.equals(CycleDefaultMimeType.MS_WORD) && !attachmentFileName.endsWith(".doc")) {
        attachmentFileName += ".doc";
      }
    }

    InputStream contentInputStream = null;
    try {
      contentInputStream = this.cycleService.getContent(artifact.getConnectorId(), artifact.getNodeId(), contentRepresentation.getId()).asInputStream();
      streamResponse(res, contentInputStream, new Date(0),
      // TODO: what is a good way to determine the etag? Using a fake one...
              "W/\"647-1281077702000\"", attach, attachmentFileName, contentType);
    } catch (TransformationException e) {
      streamResponse(res, new ByteArrayInputStream(e.getRenderContent().getBytes()), new Date(0),
      // TODO: what is a good way to determine the etag? Using a fake one...
              "W/\"647-1281077702000\"", false, null, CycleDefaultMimeType.HTML.getContentType());
    } finally {
      IoUtil.closeSilently(contentInputStream);
    }

  }

}
