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
import java.io.InputStream;
import java.net.SocketException;
import java.util.Collection;
import java.util.Date;

import javax.servlet.http.HttpSession;

import org.activiti.cycle.ContentRepresentationDefinition;
import org.activiti.cycle.ContentType;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.springframework.extensions.surf.util.Base64;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;

/**
 * @author Nils Preusker
 */
public class ContentGet extends AbstractWebScript {

  public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

    // Retrieve the artifactId from the request
    String artifactId = req.getParameter("artifactId");
    if (artifactId == null || artifactId.length() == 0) {
      throw new RuntimeException("Missing required parameter: artifactId");
    }
    String contentType = req.getParameter("content-type");
    if (artifactId == null || artifactId.length() == 0) {
      throw new RuntimeException("Missing required parameter: content-type");
    }
    // TODO: add check for supported content types

    // Retrieve session and repo connector
    String cuid = getCurrentUserId(req);
    HttpSession session = ((WebScriptServletRequest) req).getHttpServletRequest().getSession(true);
    RepositoryConnector conn = SessionUtil.getRepositoryConnector(cuid, session);

    // Retrieve the artifact from the repository
    RepositoryArtifact artifact = conn.getRepositoryArtifact(artifactId);

    Collection<ContentRepresentationDefinition> representations = artifact.getContentRepresentationDefinitions();
    for (ContentRepresentationDefinition representation : representations) {
      if (representation.getType().equals(contentType)) {

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
        streamContentImpl(req, res, conn.getContent(artifact.getId(), representation.getName()).asInputStream(), attach, new Date(0),
                "W/\"647-1281077702000\"", attachmentFileName, contentType);
      }
    }

  }
  protected void streamContentImpl(WebScriptRequest req, WebScriptResponse res, InputStream in, boolean attach, Date modified, String eTag,
          String attachFileName, String mimetype) throws IOException {
    setAttachment(res, attach, attachFileName);

    res.setContentType(mimetype);
    // TODO: determine encoding and set it on the response
    // res.setContentEncoding(...);

    // TODO: determine the content length...
    // res.setHeader("Content-Length", ...);

    // set caching
    Cache cache = new Cache();
    cache.setNeverCache(false);
    cache.setMustRevalidate(true);
    cache.setMaxAge(0L);
    cache.setLastModified(modified);
    cache.setETag(eTag);
    res.setCache(cache);

    // get the content and stream directly to the response output stream
    // assuming the repository is capable of streaming in chunks, this should
    // allow large files
    // to be streamed directly to the browser response stream.
    try {
      byte[] buffer = new byte[0xFFFF];
      for (int len; (len = in.read(buffer)) != -1;)
        res.getOutputStream().write(buffer, 0, len);
    } catch (SocketException e) {
      // TODO: client cut the connection, log the message?
    }
  }

  /**
   * Set attachment header
   * 
   * @param res
   * @param attach
   * @param attachFileName
   */
  protected void setAttachment(WebScriptResponse res, boolean attach, String attachFileName) {
    if (attach == true) {
      String headerValue = "attachment";
      if (attachFileName != null && attachFileName.length() > 0) {

        headerValue += "; filename=" + attachFileName;
      }

      // set header based on filename - will force a Save As from the browse if
      // it doesn't recognize it
      // this is better than the default response of the browser trying to
      // display the contents
      res.setHeader("Content-Disposition", headerValue);
    }
  }

  /**
   * Returns the username for the current user.
   * 
   * @param req The webscript request
   * @return The username of the current user
   */
  protected String getCurrentUserId(WebScriptRequest req) {
    String authorization = req.getHeader("Authorization");
    if (authorization != null) {
      String[] parts = authorization.split(" ");
      if (parts.length == 2) {
        return new String(Base64.decode(parts[1])).split(":")[0];
      }
    }
    return null;
  }

}
