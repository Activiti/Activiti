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
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.activiti.cycle.ContentRepresentation;
import org.activiti.cycle.MimeType;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryAuthenticationException;
import org.activiti.cycle.context.CycleApplicationContext;
import org.activiti.cycle.context.CycleRequestContext;
import org.activiti.cycle.impl.connector.signavio.transform.TransformationException;
import org.activiti.cycle.impl.mimetype.HtmlMimeType;
import org.activiti.cycle.impl.mimetype.JsonMimeType;
import org.activiti.cycle.impl.mimetype.MsExcelMimeType;
import org.activiti.cycle.impl.mimetype.MsPowerpointMimeType;
import org.activiti.cycle.impl.mimetype.MsWordMimeType;
import org.activiti.cycle.impl.mimetype.PdfMimeType;
import org.activiti.cycle.impl.mimetype.TextMimeType;
import org.activiti.cycle.impl.mimetype.XmlMimeType;
import org.activiti.cycle.service.CycleConfigurationService;
import org.activiti.cycle.service.CycleContentService;
import org.activiti.cycle.service.CyclePluginService;
import org.activiti.cycle.service.CycleProcessSolutionService;
import org.activiti.cycle.service.CycleRepositoryService;
import org.activiti.cycle.service.CycleServiceFactory;
import org.activiti.cycle.service.CycleTagService;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.rest.api.cycle.session.CycleHttpSession;
import org.activiti.rest.util.ActivitiRequest;
import org.activiti.rest.util.ActivitiStreamingWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * 
 * @author Nils Preusker (nils.preusker@camunda.com)
 */
public class ContentGet extends ActivitiStreamingWebScript {

  protected CycleRepositoryService repositoryService;
  protected CycleTagService tagService;
  protected CycleConfigurationService configurationService;
  protected CycleContentService contentService;
  protected CyclePluginService pluginService;
  protected CycleProcessSolutionService processSolutionService;

  public ContentGet() {
    configurationService = CycleServiceFactory.getConfigurationService();
    repositoryService = CycleServiceFactory.getRepositoryService();
    tagService = CycleServiceFactory.getTagService();
    contentService = CycleServiceFactory.getContentService();
    pluginService = CycleServiceFactory.getCyclePluginService();
    processSolutionService = CycleServiceFactory.getProcessSolutionService();
  }

  @Override
  protected void executeStreamingWebScript(ActivitiRequest req, WebScriptResponse res) throws IOException {
    try {
      // open cycle ui-session
      CycleHttpSession.openSession(req);
      // execute the request in the context of a CycleHttpSession
      getContent(req, res);
    } catch (RepositoryAuthenticationException e) {
      // try to login
      CycleHttpSession.tryConnectorLogin(req, e.getConnectorId());
      // retry to execute the request
      getContent(req, res);
    } finally {
      // close the CycleHttpSession
      CycleHttpSession.closeSession(req);
    }
  }

  private void getContent(ActivitiRequest req, WebScriptResponse res) throws IOException {
    CycleContentService contentService = CycleServiceFactory.getContentService();

    // Retrieve the nodeId from the request
    String connectorId = req.getMandatoryString("connectorId");
    String nodeId = req.getMandatoryString("nodeId");
    String contentRepresentationId = req.getMandatoryString("contentRepresentationId");

    String vFolderId = req.getString("vFolderId");

    if (vFolderId != null && vFolderId.length() > 0 && !vFolderId.equals("undefined")) {
      connectorId = "ps-" + processSolutionService.getVirtualRepositoryFolderById(vFolderId).getProcessSolutionId();
      CycleRequestContext.set("vFolderId", vFolderId);
    }
    
    // Retrieve the artifact from the repository
    RepositoryArtifact artifact = repositoryService.getRepositoryArtifact(connectorId, nodeId);

    ContentRepresentation contentRepresentation = contentService.getContentRepresentation(artifact, contentRepresentationId);

    MimeType contentType = contentRepresentation.getRepresentationMimeType();
    // assuming we want to create an attachment for binary data...
    boolean attach = contentType.getName().startsWith("application/") ? true : false;

    // TODO: This code should become obsolete when the connectors store the file
    // names properly with suffix.
    String attachmentFileName = null;
    if (attach) {
      attachmentFileName = artifact.getMetadata().getName();

      if (contentType.equals(CycleApplicationContext.get(XmlMimeType.class)) && !attachmentFileName.endsWith(".xml")) {
        attachmentFileName += ".xml";
      } else if (contentType.equals(CycleApplicationContext.get(JsonMimeType.class)) && !attachmentFileName.endsWith(".json")) {
        attachmentFileName += ".json";
      } else if (contentType.equals(CycleApplicationContext.get(TextMimeType.class)) && !attachmentFileName.endsWith(".txt")) {
        attachmentFileName += ".txt";
      } else if (contentType.equals(CycleApplicationContext.get(PdfMimeType.class)) && !attachmentFileName.endsWith(".pdf")) {
        attachmentFileName += ".pdf";
      } else if (contentType.equals(CycleApplicationContext.get(MsExcelMimeType.class)) && !attachmentFileName.endsWith(".xls")) {
        attachmentFileName += ".xls";
      } else if (contentType.equals(CycleApplicationContext.get(MsPowerpointMimeType.class)) && !attachmentFileName.endsWith(".ppt")) {
        attachmentFileName += ".ppt";
      } else if (contentType.equals(CycleApplicationContext.get(MsWordMimeType.class)) && !attachmentFileName.endsWith(".doc")) {
        attachmentFileName += ".doc";
      }
    }

    InputStream contentInputStream = null;
    try {
      contentInputStream = contentRepresentation.getContent(artifact).asInputStream();

      // TODO: this is broken for SignavioPNG

      // Calculate an etag for the content using the MD5 algorithm
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] messageDigest = md.digest(contentRepresentation.getContent(artifact).asByteArray());

      BigInteger number = new BigInteger(1, messageDigest);
      String etag = number.toString(16);
      while (etag.length() < 32) {
        etag = "0" + etag;
      }
      // String etag ="";
      String requestEtag = req.getHttpServletRequest().getHeader("If-None-Match");
      if (requestEtag != null) {
        // For some reason the etag (If-None-Match) parameter is always returned
        // as a quoted string, remove the quotes before comparing it with the
        // newly calculated etag
        requestEtag = requestEtag.replace("\"", "");
      }

      // Check whether the file has been modified since it was last fetched by
      // the client
      if (etag.equals(requestEtag)) {
        throw new WebScriptException(HttpServletResponse.SC_NOT_MODIFIED, "");
      } else {
        streamResponse(res, contentInputStream, new Date(0), etag, attach, attachmentFileName, contentType.getContentType());
      }

    } catch (TransformationException e) {
      // Stream the contents of the exception as HTML, this is a workaround to
      // display exceptions that occur during content transformations
      streamResponse(res, new ByteArrayInputStream(e.getRenderContent().getBytes()), new Date(0), "", false, null,
              CycleApplicationContext.get(HtmlMimeType.class).getContentType());
    } catch (NoSuchAlgorithmException e) {
      // This should never be reached... MessageDigest throws an exception if it
      // is being instantiated with a wrong algorithm, but we know that MD5
      // exists.
    } finally {
      IoUtil.closeSilently(contentInputStream);
    }
  }

}
