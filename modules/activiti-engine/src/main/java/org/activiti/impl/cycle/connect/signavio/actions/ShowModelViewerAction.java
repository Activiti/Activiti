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
package org.activiti.impl.cycle.connect.signavio.actions;

import java.util.Map;
import java.util.logging.Level;

import org.activiti.impl.cycle.connect.api.RepositoryArtifact;
import org.activiti.impl.cycle.connect.api.RepositoryNode;
import org.activiti.impl.cycle.connect.api.actions.FileAction;
import org.activiti.impl.cycle.connect.api.actions.FileActionGuiRepresentation;

/**
 * 
 * @author christian.lipphardt@camunda.com
 */
public class ShowModelViewerAction extends FileAction {

  @Override
  public void execute() {
  }

  @Override
  public void execute(RepositoryNode itemInfo) {
  }

  @Override
  public void execute(RepositoryNode itemInfo, Map<String, Object> param) {
  }
  
  @Override
  public FileActionGuiRepresentation getGuiRepresentation() {
    return FileActionGuiRepresentation.DEFAULT_PANE;
  }

  @Override
  public String getGuiRepresentationHtmlSnippet() {
    String htmlCode = getHTMLSnippetForPublishingProcessModell(getFile());

    if(log.isLoggable(Level.FINEST)) {
      log.finest(htmlCode);
    }

    return htmlCode;
  }

  @Override
  public String getGuiRepresentationAsString() {
    return FileActionGuiRepresentation.DEFAULT_PANE.toString();
  }

  @Override
  public String getName() {
    return "Show Zoomer for Signavio Model";
  }

  public String getHTMLSnippetForPublishingProcessModell(RepositoryArtifact fileInfo) {
    return null;
    // TODO: Implement

    // try {
    // SignavioConnector connector = (SignavioConnector)
    // getFile().getConnector();
    //
    // // String htmlSnippet =
    // // getHTMLSnippetForPublishingProcessModell(getFile());
    //
    // // Creating the JSON Object for the Snippet
    // JSONObject resultJsonObject = new JSONObject();
    //
    // resultJsonObject.put("url", connector.getModelUrl(fileInfo));
    // resultJsonObject.put("overflowX", "fit");
    // resultJsonObject.put("overflowY", "fit");
    // resultJsonObject.put("zoomSlider", true);
    // resultJsonObject.put("linkSubProcesses", false);
    //
    // // Generating the authToken
    // JSONArray embeddedModelJSONArray = connector.getEmbeddedModel(fileInfo);
    //
    // String authToken, authKey_png, authKey_json, authKey_stencilset;
    // authToken = authKey_png = authKey_json = authKey_stencilset = "";
    //
    // for (int i = 0; i < embeddedModelJSONArray.length(); i++) {
    // JSONObject tempJsonObject =
    // embeddedModelJSONArray.getJSONObject(i).getJSONObject("rep");
    //
    // String tempRepType = tempJsonObject.getString("type");
    //
    // // Getting the authKey we need
    // if (tempRepType.equals("png"))
    // authKey_png = tempJsonObject.getString("authkey");
    //
    // else if (tempRepType.equals("json"))
    // authKey_json = tempJsonObject.getString("authkey");
    //
    // else if (tempRepType.equals("stencilset"))
    // authKey_stencilset = tempJsonObject.getString("authkey");
    // }
    //
    // authToken = authKey_png + "_" + authKey_json + "_" + authKey_stencilset;
    //
    // // An alternative algorithm for creating the authToken, if the order of
    // // the JsonArray is static and doesn't change.
    // //
    // // JSONObject tempJsonObject =
    // // embeddedBOJSONArray.getJSONObject(0).getJSONObject("rep");
    // // authToken += tempJsonObject.getString("authkey") + "_";
    // //
    // // tempJsonObject =
    // // embeddedBOJSONArray.getJSONObject(1).getJSONObject("rep");
    // // authToken += tempJsonObject.getString("authkey") + "_";
    // //
    // // tempJsonObject =
    // // embeddedBOJSONArray.getJSONObject(1).getJSONObject("rep");
    // // authToken += tempJsonObject.getString("authkey");
    //
    // resultJsonObject.put("authToken", authToken);
    //
    // // Suggestion: method returns var SERVER_URL for signavio to achieve the
    // following
    // // kann man folgendes machen.
    // // mashupURL = connector.getSERVER_URL().substring(0,SERVER_URL.length()
    // -
    // // 2) + "mashup/signavio.js";
    // // Letzteres um das 'p' in SERVER_URL wegzukriegen
    //
    // String firstScriptTag = "<script type=\"text/javascript\" src=\"" +
    // connector.getSignavioConfiguration().getMashupUrl() + "signavio.js" +
    // "\"></script>";
    // String secondScriptTag = "<script type=\"text/plain\">" +
    // resultJsonObject.toString() + "</script>";
    //
    // String resultHtmlSnippet = firstScriptTag.concat(secondScriptTag);
    //
    // return resultHtmlSnippet;
    // } catch (Exception exception) {
    // throw new
    // RepositoryException("Exception during converting embeddedModel into Snippet",
    // exception);
    // }
  }

}
