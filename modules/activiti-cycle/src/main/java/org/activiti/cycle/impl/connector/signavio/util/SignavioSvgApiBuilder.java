package org.activiti.cycle.impl.connector.signavio.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.impl.connector.signavio.SignavioConnector;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SignavioSvgApiBuilder {

  public static final String HEADER = "<html><head><link href=\"../../res/css/activiti-core.css\" type=\"text/css\" rel=\"stylesheet\"></head><body>";
  public static final String FOOTER = "</body></html>";
  public static final String SVGAPI_URL_REMOTE = "http://signavio-core-components.googlecode.com/svn/trunk/api/api/signavio-svg.js";
  // path to signavio-svg.js in activiti-modeler or so...
  public static final String SVGAPI_URL_LOCAL = "http://localhost:8080/activiti-modeler/api/signavio-svg.js";
  // maybe make this changable?
  public static final String SERVER_SCRIPT_URL = "http://localhost:8080/activiti-modeler";

  private SignavioConnector connector;
  private RepositoryArtifact artifact;

  private String authToken;
  private String clickFunction;

  private boolean useLocalScripts = false;
  
  /**
   * Map mapping a color for highlighting with a {@link Map} of node (ids) with
   * messages to show
   */
  private Map<String, Map<String, List<String>>> nodesToHighlight = new HashMap<String, Map<String, List<String>>>();

  /**
   * Constructor to create a SignavioSvgApiBuilder object.
   * 
   * @param connector
   *          required for constructing correct model url
   * @param artifact
   *          required for constructing correct model url
   */
  public SignavioSvgApiBuilder(SignavioConnector connector, RepositoryArtifact artifact) {
    this.connector = connector;
    this.artifact = artifact;
  }

  /**
   * Submit a map containing the nodes to be highlighted and in which color.
   * Key: Signavio ID, Value: Message to show
   */
  public SignavioSvgApiBuilder highlightNodes(Map<String, List<String>> nodes, String color) {
    nodesToHighlight.put(color, nodes);
    return this;
  }

  /**
   * Maybe required to get access to models in saas/enterprise signavio.
   * 
   * @param authToken
   *          authtoken for saas / enterprise signavio
   */
  public SignavioSvgApiBuilder authToken(String authToken) {
    this.authToken = authToken;
    return this;
  }

  /**
   * A javascript function submitted as string. You have the javascript
   * variables 'node' and 'editor' to your disposal.
   * 
   * @param clickFunction
   *          a string representation of a javascript function, e.g.
   *          'if(node.properties["oryx-name"]||node.properties["oryx-title"]) {
   *          alert("Name: " + node.properties["oryx-name"] + "\n(Sid: " +
   *          node.resourceId + ")"); };'
   * @return
   */
  public SignavioSvgApiBuilder clickFunction(String clickFunction) {
    this.clickFunction = clickFunction;
    return this;
  }

  public SignavioSvgApiBuilder useLocalScripts(boolean useLocalScripts) {
    this.useLocalScripts = useLocalScripts;
    return this;
  }
  
  /**
   * use buildHtml instead
   * 
   * @throws JSONException
   */
  @Deprecated
  public String build() {
    return buildHtml();
  }

  public String buildHtml() {
    return buildHtml(buildScript(), null);
  }
  
  public static String buildHtml(String content) {
    return buildHtml(content, null);
  }

  public static String buildHtml(String content, String additionalContent) {
    if (additionalContent == null) {
      additionalContent = "";
    }
    return HEADER + "<div id=\"model\">" + content + "</div>" + additionalContent + FOOTER;
  }

  public static String buildHtml(String content, String additionalContent, int height) {
    if (additionalContent == null) {
      additionalContent = "";
    }
    return HEADER + "<div id=\"model\" style=\"height: " + height + "px;\">" + content + "</div>" + additionalContent + FOOTER;
  }
  
  public String buildScript() {
    return buildScript(100);
  }

  public String buildScript(Integer zoom) {
    try {
      StringBuilder svgApiCall = new StringBuilder();
      svgApiCall.append("<script type=\"text/javascript\" src=\"");
      if (useLocalScripts) {
        svgApiCall.append(SVGAPI_URL_LOCAL);
      } else {        
        svgApiCall.append(SVGAPI_URL_REMOTE);
      }
      svgApiCall.append("\"></script>");
      svgApiCall.append("<script type=\"text/plain\">");
      svgApiCall.append("{");
      svgApiCall.append("url: \"" + connector.getConfiguration().getModelUrl(artifact.getNodeId()) + "\"");
  
      if (useLocalScripts) {
        // url to svgapi script
        svgApiCall.append(", server: \"" + SERVER_SCRIPT_URL + "\"");
      }
  
      svgApiCall.append(", element: \"model\"");
      
      // if authToken is available
      if (authToken != null && authToken.length() > 0) {
        svgApiCall.append(", authToken: \"" + authToken + "\",");
      }
  
      // register mouseover event on callback function
//      svgApiCall.append(", callback: " + registerMouseOverEvent());
  
      // executed when click on a shape
      svgApiCall.append(", click: " + createClickFunction());
  
      // highlight nodes
      svgApiCall.append(", focus: " + buildHighlightning());
  
      // initial zoom
      svgApiCall.append(", zoom: " + zoom);
  
      svgApiCall.append("}");
      svgApiCall.append("</script>");
    
      return svgApiCall.toString();
    }
    catch (JSONException ex) {
      throw new RepositoryException("Unexpected exception with JSON handling for " + artifact, ex);
    }
  }
  
  public static String buildTable(String headline, Map<String, List<String>> nodesMap) {
    StringBuffer html = new StringBuffer();
    if (nodesMap != null && !nodesMap.isEmpty()) {
      html.append("<h2>" + headline + "</h2>");

      html.append("<table border=\"1\">");
      html.append("<thead><tr>");
      html.append("<th>Signavio Id</th><th>Message</th>");
      html.append("</tr></thead>");
      html.append("<tbody>");
      for (Entry<String, List<String>> mapEntry : nodesMap.entrySet()) {
        html.append("<tr>");
        html.append("<td style=\"vertical-align: top\">" + mapEntry.getKey() + "</td>");
        html.append("<td>");
        for (String message : mapEntry.getValue()) {
          html.append(message + "<br/>");
        }
        html.append("</td>");
        html.append("</tr>");
      }
      html.append("</tbody>");
      html.append("</table>");
      html.append("<br/>");
    }
    return html.toString();
  }  

  private String registerMouseOverEvent() throws JSONException {
    if (nodesToHighlight == null || nodesToHighlight.isEmpty()) {
      return "function(editor) {}";
    }

    StringBuilder callbackFunc = new StringBuilder();
    callbackFunc.append("function(editor) {");
    callbackFunc.append("editor.registerOnEvent(\"mouseover\", function(evt, node) {");
    callbackFunc.append("var errorMessages = " + createJsonMessagesObject() + ";");
    callbackFunc.append("var myNodeMessages = errorMessages[node.resourceId];");
    callbackFunc.append("if (myNodeMessages != '' && myNodeMessages != 'undefined' && myNodeMessages != undefined) {");
    callbackFunc.append("var myNodeMessagesStr = \"\";");
    callbackFunc.append("for (msg in myNodeMessages) {");
    callbackFunc.append("myNodeMessagesStr += myNodeMessages[msg] + \"\\n\";");
    callbackFunc.append("}");
    callbackFunc.append("alert(\"Sid: \" + node.resourceId + \"\\nMessages: \" + myNodeMessagesStr);");
    callbackFunc.append("}");
    // @TODO: doesn't work atm, unable to get variable 'me'
//    callbackFunc.append("if (node instanceof me.ORYX.Core.Shape) {");
//    callbackFunc.append("}");
    callbackFunc.append("});");

    callbackFunc.append("}");
    return callbackFunc.toString();
  }

  private String createClickFunction() throws JSONException {
    StringBuilder clickFunc = new StringBuilder();
    clickFunc.append("function(node, editor) {");
    if (clickFunction != null && clickFunction.length() > 0) {
      clickFunc.append(clickFunction);
    } else {
      // create default click function
      clickFunc.append("var errorMessages = " + createJsonMessagesObject() + ";");
      clickFunc.append("var myNodeMessages = errorMessages[node.resourceId];");
      clickFunc.append("if (myNodeMessages != '' && myNodeMessages != 'undefined' && myNodeMessages != undefined) {");
      clickFunc.append("var myNodeMessagesStr = \"\";");
      clickFunc.append("for (msg in myNodeMessages) {");
      clickFunc.append("myNodeMessagesStr += myNodeMessages[msg] + \"\\n\";");
      clickFunc.append("}");
      clickFunc.append("alert(\"Sid: \" + node.resourceId + \"\\nErrorMessages: \" + myNodeMessagesStr);");
      clickFunc.append("}");

      
      //clickFunc.append("var errorMessages = " + createJsonMessagesObject() + ";");
//      clickFunc.append("if(node.properties[\"oryx-name\"] || node.properties[\"oryx-title\"] || node.resourceId) {");
//      clickFunc.append("alert(\"Name: \" + node.properties[\"oryx-name\"] + \" (Sid: \" + node.resourceId + \")\");");
//      clickFunc.append("}");
    }
    clickFunc.append("}");

    return clickFunc.toString();
  }

  private JSONObject highlightNodesMap(String color, Map<String, List<String>> map) throws JSONException {
    if (map == null || map.isEmpty()) {
      return null;
    }
    // set default color if not provided
    if (color == null || color.length() == 0) {
      color = "red";
    }
    JSONObject highlightNodesObj = new JSONObject();

    highlightNodesObj.put("nodes", map.keySet());
    JSONObject attributes = new JSONObject();
    attributes.put("fill", color);
    highlightNodesObj.put("attributes", attributes);

    return highlightNodesObj;
  }

  private String buildHighlightning() throws JSONException {
    if (nodesToHighlight == null || nodesToHighlight.isEmpty()) {
      return "[]";
    }
    
    JSONArray focusArr = new JSONArray();
    for (Entry<String, Map<String, List<String>>> entry : nodesToHighlight.entrySet()) {
      focusArr.put(highlightNodesMap((String) entry.getKey(), (Map<String, List<String>>) entry.getValue()));
    }
    
    return focusArr.toString();
  }

  private String createJsonMessagesObject() throws JSONException {
    if (nodesToHighlight == null || nodesToHighlight.isEmpty()) {
      return "\"\"";
    }
    
    JSONObject jsonMessageObj = new JSONObject();
    for (Entry<String, Map<String, List<String>>> entry : nodesToHighlight.entrySet()) {
      for (Entry<String, List<String>> mapEntry : entry.getValue().entrySet()) {
        jsonMessageObj.put(mapEntry.getKey(), mapEntry.getValue());
      }
    }

    return jsonMessageObj.toString();
  }
  
}