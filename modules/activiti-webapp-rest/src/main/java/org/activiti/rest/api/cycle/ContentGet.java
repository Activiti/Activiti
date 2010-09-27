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

import java.io.IOException;
import java.util.Collection;
import java.util.Date;

import javax.servlet.http.HttpSession;

import org.activiti.cycle.ContentRepresentation;
import org.activiti.cycle.ContentType;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.rest.util.ActivitiRequest;
import org.activiti.rest.util.ActivitiStreamingWebScript;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * @author Nils Preusker
 */
public class ContentGet extends ActivitiStreamingWebScript {

  @Override
  protected void executeStreamingWebScript(ActivitiRequest req, WebScriptResponse res) throws IOException {
    // Retrieve the artifactId from the request
    String artifactId = req.getMandatoryString("artifactId");
    String contentType = req.getMandatoryString("content-type");
    // TODO: add check for supported content types

    // Retrieve session and repo connector
    String cuid = req.getCurrentUserId();
    HttpSession session = req.getHttpSession();
    RepositoryConnector conn = SessionUtil.getRepositoryConnector(cuid, session);

    // Retrieve the artifact from the repository
    RepositoryArtifact artifact = conn.getRepositoryArtifact(artifactId);

    Collection<ContentRepresentation> representations = artifact.getArtifactType().getContentRepresentations();
    for (ContentRepresentation representation : representations) {
      if (representation.getMimeType().equals(contentType)) {

        // assuming we want to create an attachment for binary data...
        boolean attach = contentType.startsWith("application/") ? true : false;

        // TODO: This code should become obsolete when the connectors store the file names properly with suffix.
        String attachmentFileName = null;
        if(attach) {
          attachmentFileName = artifact.getMetadata().getName();

          if(contentType.equals(ContentType.XML) && !attachmentFileName.endsWith(".xml")) {
            attachmentFileName += ".xml";
          } else if(contentType.equals(ContentType.JSON) && !attachmentFileName.endsWith(".json")) {
            attachmentFileName += ".json";
          } else if(contentType.equals(ContentType.TEXT) && !attachmentFileName.endsWith(".txt")) {
            attachmentFileName += ".txt";
          } else if(contentType.equals(ContentType.PDF) && !attachmentFileName.endsWith(".pdf")) {
            attachmentFileName += ".pdf";
          } else if(contentType.equals(ContentType.MS_EXCEL) && !attachmentFileName.endsWith(".xls")) {
            attachmentFileName += ".xls";
          } else if(contentType.equals(ContentType.MS_POWERPOINT) && !attachmentFileName.endsWith(".ppt")) {
            attachmentFileName += ".ppt";
          } else if(contentType.equals(ContentType.MS_WORD) && !attachmentFileName.endsWith(".doc")) {
            attachmentFileName += ".doc";
          }

        }

        // TODO: what is a good way to determine the etag? Using a fake one...
        streamResponse(res, conn.getContent(artifact.getId(), representation.getId()).asInputStream(), new Date(0),
            "W/\"647-1281077702000\"", attach, attachmentFileName, contentType);
      }
    }

  }

}
