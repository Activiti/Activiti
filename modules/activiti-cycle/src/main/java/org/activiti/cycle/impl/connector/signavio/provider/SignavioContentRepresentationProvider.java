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
package org.activiti.cycle.impl.connector.signavio.provider;

import java.io.IOException;
import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;

import org.activiti.cycle.ContentRepresentationProvider;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.impl.connector.signavio.SignavioConnector;
import org.restlet.data.Response;
import org.restlet.resource.DomRepresentation;

public abstract class SignavioContentRepresentationProvider extends ContentRepresentationProvider {

  public SignavioContentRepresentationProvider(String name, String type, boolean downloadable) {
    super(name, type, downloadable);
  }

  public SignavioConnector getConnector(RepositoryArtifact artifact) {
    return (SignavioConnector) artifact.getOriginalConnector();
  }

  public Response getJsonResponse(RepositoryArtifact artifact, String urlSuffix) throws IOException {
    SignavioConnector connector = getConnector(artifact);
    String url = connector.getModelUrl(artifact) + urlSuffix;
    return connector.getJsonResponse(url);
  }

  public String getXmlAsString(DomRepresentation xmlData) throws TransformerFactoryConfigurationError, TransformerConfigurationException, TransformerException,
          IOException {
    StringWriter stringWriter = new StringWriter();
    StreamResult streamResult = new StreamResult(stringWriter);
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
    transformer.transform(xmlData.getDomSource(), streamResult);

    return stringWriter.toString();
  }
}
