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

import org.activiti.cycle.Content;
import org.activiti.cycle.MimeType;
import org.activiti.cycle.RenderInfo;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryArtifactType;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.components.RuntimeConnectorList;
import org.activiti.cycle.context.CycleApplicationContext;
import org.activiti.cycle.context.CycleSessionContext;
import org.activiti.cycle.impl.connector.signavio.SignavioConnector;
import org.activiti.cycle.impl.connector.signavio.SignavioConnectorConfiguration;
import org.activiti.cycle.impl.connector.signavio.SignavioConnectorInterface;
import org.activiti.cycle.impl.connector.signavio.repositoryartifacttype.SignavioBpmn20ArtifactType;
import org.activiti.cycle.impl.mimetype.HtmlMimeType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Reference;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;

/**
 * TODO: Clarify what this does exactly and improve!
 * 
 * Embedebale Model still has problems in the OSS version (usable there at all?)
 * 
 * 
 */
public class EmbeddableModelProvider extends SignavioContentRepresentationProvider {
  
  // !!! WARNING: this might be broken. - meyerd !!!
  
  public String getId() {
    return "EmbeddableModelProvider";
  }


  public RenderInfo getRenderInfo() {
    // TODO: ?
    return RenderInfo.HTML;
  }
  
  public boolean isForDownload() {
    return false;
  }

  public MimeType getRepresentationMimeType() {
    return CycleApplicationContext.get(HtmlMimeType.class);
  }

  public RepositoryArtifactType getRepositoryArtifactType() {
    return CycleApplicationContext.get(SignavioBpmn20ArtifactType.class);
  }
  
  public Content getContent(RepositoryArtifact artifact) {
    try {
      Content content = new Content();
      SignavioConnectorInterface connector = (SignavioConnectorInterface) CycleSessionContext.get(RuntimeConnectorList.class).getConnectorById(artifact.getConnectorId());      
      JSONArray embeddedModel = getEmbeddedModel(connector, artifact);
      String hmtlSnippet = getHmtlSnippet(connector, artifact, embeddedModel);
      content.setValue(hmtlSnippet);
      return content;
    } catch (Exception ex) {
      throw new RepositoryException("Exception while retrieving embeddedModel from Signavio", ex);
    }
  }


  public JSONArray getEmbeddedModel(SignavioConnectorInterface connector, RepositoryArtifact artifact) throws IOException, JSONException {
    Client client = connector.initClient();
    
    Reference embeddedModelRef = new Reference(((SignavioConnectorConfiguration)connector.getConfiguration()).getPurl());

    // Create POST parameters
    Form embeddedModelForm = new Form();
    embeddedModelForm.add("label", "");
    embeddedModelForm.add("mails", "");
    embeddedModelForm.add("message", "");
    embeddedModelForm.add("sbo", artifact.getNodeId());
    embeddedModelForm.add("type", "png");
    Representation embeddedModelRep = embeddedModelForm.getWebRepresentation();

    // Create Request
    Request embeddedBORequest = new Request(Method.POST, embeddedModelRef, embeddedModelRep);
    // Set MediaType to retrieve JSON data
    embeddedBORequest.getClientInfo().getAcceptedMediaTypes().add(new Preference<MediaType>(MediaType.APPLICATION_JSON));

    // Add the securityToken to the Request Headers
    Form requestHeaders = new Form();
    requestHeaders.add("token", connector.getSecurityToken());
    embeddedBORequest.getAttributes().put("org.restlet.http.headers", requestHeaders);

    // Send the request and retrieve the response
    Response embeddedBOResponse = client.handle(embeddedBORequest);

    // Get the ResponseBody as JSON
    JsonRepresentation jsonData = new JsonRepresentation(embeddedBOResponse.getEntity());

    // Transform to JSONArray
    JSONArray jsonArray = jsonData.getJsonArray();

    // Content of jsonArray above with modelID
    // /model/6fd6be02c610475c9daab28a046282e2
    // [
    // {
    // "rel":"purl",
    // "href":"/purl/6fd6be02c610475c9daab28a046282e2/info/f5f981a520574e9e9404e2cd09473d93",
    // "rep":
    // {
    // "id":"f5f981a520574e9e9404e2cd09473d93",
    // "authkey":"f32447ff8d073af5373542f6b6fb8b265b68ed71d6a1b9c5415fdf522d430",
    // "sbo":"6fd6be02c610475c9daab28a046282e2",
    // "label":"",
    // "type":"png",
    // "uri":"/p/model/6fd6be02c610475c9daab28a046282e2/png?inline"
    // }
    // },
    // {
    // "rel":"purl",
    // "href":"/purl/6fd6be02c610475c9daab28a046282e2/info/6fe6c5f6360d4d5983c538d7001b02c2",
    // "rep":
    // {
    // "id":"6fe6c5f6360d4d5983c538d7001b02c2",
    // "authkey":"c42e3bc5e57d7d2121156d16e1e1896d0c153fb5ac29f1e78f177628243f97",
    // "sbo":"6fd6be02c610475c9daab28a046282e2",
    // "label":"",
    // "type":"json",
    // "uri":"/p/model/6fd6be02c610475c9daab28a046282e2/json?inline"
    // }
    // },
    // {
    // "rel":"purl",
    // "href":"/purl/6fd6be02c610475c9daab28a046282e2/info/64a2f892812942ad98a6e70d790b862d",
    // "rep":
    // {
    // "id":"64a2f892812942ad98a6e70d790b862d",
    // "authkey":"bb43cdb6d6c8cb568c3fbcc567e7d521ca5f9e74d0cfe39813e62cce7d88c61",
    // "sbo":"6fd6be02c610475c9daab28a046282e2",
    // "label":"",
    // "type":"stencilset",
    // "uri":"/p/editor_stencilset"
    // }
    // },
    // {
    // "rel":"purl",
    // "href":"/purl/6fd6be02c610475c9daab28a046282e2/info/d59c157675c0476f9b6279b8ee97105b",
    // "rep":
    // {
    // "id":"d59c157675c0476f9b6279b8ee97105b",
    // "authkey":"eb19dcfedba71d9f7daefeb4188ad72daeec46539926bc6fd3e43a5ddd31d7",
    // "sbo":"6fd6be02c610475c9daab28a046282e2",
    // "label":"",
    // "type":"gadget",
    // "uri":"/p/model/6fd6be02c610475c9daab28a046282e2/gadget"
    // }
    // }
    // ]

    // this should be the return value of the method

    // <script type="text/javascript"
    // src="http://127.0.0.1:8080/mashup/signavio.js"></script>
    // <script type="text/plain">
    // {
    // url: "http://127.0.0.1:8080/p/model/6fd6be02c610475c9daab28a046282e2",
    // authToken:
    // "f32447ff8d073af5373542f6b6fb8b265b68ed71d6a1b9c5415fdf522d430_c42e3bc5e57d7d2121156d16e1e1896d0c153fb5ac29f1e78f177628243f97_bb43cdb6d6c8cb568c3fbcc567e7d521ca5f9e74d0cfe39813e62cce7d88c61",
    // overflowX: "fit",
    // overflowY: "fit",
    // <!-- begin optional ------
    // width: 200,
    // height: 200,
    // ---- end optional ------->
    // zoomSlider: true,
    // linkSubProcesses: false
    // }
    // </script>

    return jsonArray;
  }

  public String getHmtlSnippet(SignavioConnectorInterface connector, RepositoryArtifact artifact, JSONArray embeddedModelJSONArray) throws JSONException {

    // String htmlSnippet =
    // getHTMLSnippetForPublishingProcessModell(getFile());

    // Creating the JSON Object for the Snippet
    JSONObject resultJsonObject = new JSONObject();
    
    SignavioConnectorConfiguration connectorConfiguration = (SignavioConnectorConfiguration)connector.getConfiguration();

    resultJsonObject.put("url", connectorConfiguration.getModelUrl(artifact.getNodeId()));
    resultJsonObject.put("overflowX", "fit");
    resultJsonObject.put("overflowY", "fit");
    resultJsonObject.put("zoomSlider", true);
    resultJsonObject.put("linkSubProcesses", false);

    // Generating the authToken
    String authToken, authKey_png, authKey_json, authKey_stencilset;
    authToken = authKey_png = authKey_json = authKey_stencilset = "";

    for (int i = 0; i < embeddedModelJSONArray.length(); i++) {
      JSONObject tempJsonObject = embeddedModelJSONArray.getJSONObject(i).getJSONObject("rep");

      String tempRepType = tempJsonObject.getString("type");

      // Getting the authKey we need
      if (tempRepType.equals("png")) {
        authKey_png = tempJsonObject.getString("authkey");
      } else if (tempRepType.equals("json")) {
        authKey_json = tempJsonObject.getString("authkey");
      } else if (tempRepType.equals("stencilset")) {
        authKey_stencilset = tempJsonObject.getString("authkey");
      }
    }

    authToken = authKey_png + "_" + authKey_json + "_" + authKey_stencilset;

    // An alternative algorithm for creating the authToken, if the order of
    // the JsonArray is static and doesn't change.
    //
    // JSONObject tempJsonObject =
    // embeddedBOJSONArray.getJSONObject(0).getJSONObject("rep");
    // authToken += tempJsonObject.getString("authkey") + "_";
    //
    // tempJsonObject =
    // embeddedBOJSONArray.getJSONObject(1).getJSONObject("rep");
    // authToken += tempJsonObject.getString("authkey") + "_";
    //
    // tempJsonObject =
    // embeddedBOJSONArray.getJSONObject(1).getJSONObject("rep");
    // authToken += tempJsonObject.getString("authkey");

    resultJsonObject.put("authToken", authToken);

    // Suggestion: method returns var SERVER_URL for signavio to achieve the
    // following
    // kann man folgendes machen.
    // mashupURL = connector.getSERVER_URL().substring(0,SERVER_URL.length() -
    // 2) + "mashup/signavio.js";
    // Letzteres um das 'p' in SERVER_URL wegzukriegen

    String firstScriptTag = "<script type=\"text/javascript\" src=\"" + connectorConfiguration.getMashupUrl() + "signavio.js"
            + "\"></script>";
    String secondScriptTag = "<script type=\"text/plain\">" + resultJsonObject.toString() + "</script>";

    String resultHtmlSnippet = firstScriptTag.concat(secondScriptTag);

    return resultHtmlSnippet;
  }
  /**
   * Use this method to cancel the sharing of the embedded model
   */
  public void deleteEmbeddedModel(SignavioConnector connector, RepositoryArtifact artifact) {
    try {
      Client client = connector.initClient();

      Reference embeddedModelRef = new Reference(connector.getConfiguration().getSignavioUrl() + "purl/" + artifact.getNodeId() + "/info/");

      Request embeddedModelRequest = new Request(Method.DELETE, embeddedModelRef);

      // not needed!
      // Form embeddedModelForm = new Form();
      // embeddedModelForm.add("label", "");
      // embeddedModelForm.add("mails", "");
      // embeddedModelForm.add("message", "");
      // embeddedModelForm.add("sbo", fileInfo.getPath().substring(7));
      // embeddedModelForm.add("type", "png");
      // Representation embeddedBORep =
      // embeddedModelForm.getWebRepresentation();

      Form requestHeaders = new Form();
      requestHeaders.add("token", connector.getSecurityToken());
      embeddedModelRequest.getAttributes().put("org.restlet.http.headers", requestHeaders);

      client.handle(embeddedModelRequest);

    } catch (Exception ex) {
      throw new RepositoryException("Exception while accessing Signavio repository", ex);
    }
  }



}
