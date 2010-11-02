package org.activiti.cycle.impl.connector.signavio.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.impl.connector.signavio.SignavioConnector;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SignavioSvgApiBuilder {

  public static final String HEADER = "<html><head></head><body>";
  public static final String FOOTER = "</body></html>";
  public static final String SVGAPI_URL = "http://signavio-core-components.googlecode.com/svn/trunk/api/src/signavio-svg.js";
  // maybe make this changable?
  public static final String SERVER_SCRIPT_URL = "http://localhost:8080/activiti-modeler";

  private SignavioConnector connector;
  private RepositoryArtifact artifact;

  private String authToken;
  private String clickFunction;

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

  /**
   * use buildHtml instead
   * 
   * @throws JSONException
   */
  @Deprecated
  public String build() throws JSONException {
    return buildHtml();
  }

  public String buildHtml() throws JSONException {
    return buildHtml(buildScript());
  }

  public static String buildHtml(String content) {
    return HEADER + content + FOOTER;
  }

  public static String buildHtml(String content, int height) {
    return HEADER + "<div id=\"model\" style=\"height: " + height + "px; width: 600px;\">" + content + "</div>" + FOOTER;
  }

  public String buildScript() throws JSONException {
    StringBuilder svgApiCall = new StringBuilder();
    svgApiCall.append("<script type=\"text/javascript\" src=\"" + SVGAPI_URL + "\"></script>");
    svgApiCall.append("<script type=\"text/plain\">");
    svgApiCall.append("{");
    // url to svgapi script
    svgApiCall.append("url: \"http://localhost:8080/activiti-modeler\"");
//    svgApiCall.append("url: \"" + connector.getConfiguration().getModelUrl(artifact.getNodeId()) + "\"");

    // if authToken is available
    if (authToken != null && authToken.length() > 0) {
      svgApiCall.append(", authToken: \"" + authToken + "\",");
    }

    // register mouseover event on callback function
    svgApiCall.append(", callback: " + registerMouseOverEvent());

    // executed when click on a shape
    svgApiCall.append(", click: " + createClickFunction());

    // highlight nodes
    svgApiCall.append(", focus: " + buildHighlightning());

    svgApiCall.append("}");
    svgApiCall.append("</script>");

    // include messages as text
    svgApiCall.append("<div id=\"messages\">" +buildMessages() + "</div>");

    return svgApiCall.toString();
  }

  private String registerMouseOverEvent() throws JSONException {
    StringBuilder callbackFunc = new StringBuilder();
    callbackFunc.append("function(editor) {");
    callbackFunc.append("editor.registerOnEvent(\"mouseover\", function(evt, node) {");
    callbackFunc.append("var errorMessages = " + createJsonMessagesObject() + ";");
    callbackFunc.append("var myNodeMessages = errorMessages[node.resourceId];");
    callbackFunc.append("if (myNodeMessages.length > 0) {");
    callbackFunc.append("var myNodeMessagesStr;");
    callbackFunc.append("for (msg in myNodeMessages) {");
    callbackFunc.append("myNodeMessagesStr += myNodeMessages[msg] + \"\n\";");
    callbackFunc.append("}");
    callbackFunc.append("}");
    callbackFunc.append("if (node instanceof me.ORYX.Core.Shape) {");
    callbackFunc.append("alert(\"Sid: \" + node.resourceId + \"\nMessages: \" + myNodeMessagesStr);");
    callbackFunc.append("}");
    callbackFunc.append("});");

    callbackFunc.append("}");

    return callbackFunc.toString();
  }

  private String buildMessages() {
    if (nodesToHighlight == null || nodesToHighlight.isEmpty()) {
      return "";
    }

    StringBuilder mappingValidationErrorHtml = new StringBuilder();
    mappingValidationErrorHtml.append("<h2>Mapping validation issues</h2>");
    for (Entry<String, Map<String, List<String>>> entry : nodesToHighlight.entrySet()) {
      if (entry.getValue() != null && !entry.getValue().isEmpty()) {
        mappingValidationErrorHtml.append("<table border=\"1\">");
        mappingValidationErrorHtml.append("<thead><tr>");
        mappingValidationErrorHtml.append("<th>SignavioId</th><th>Mapping validation messages</th>");
        mappingValidationErrorHtml.append("</tr></thead>");
        mappingValidationErrorHtml.append("<tbody>");
        for (Entry<String, List<String>> mapEntry : entry.getValue().entrySet()) {
          mappingValidationErrorHtml.append("<tr>");
          mappingValidationErrorHtml.append("<td>" + mapEntry.getKey() + "</td>");
          mappingValidationErrorHtml.append("<td>");
          for (String message : mapEntry.getValue()) {
            mappingValidationErrorHtml.append(message + "<br/>");
          }
          mappingValidationErrorHtml.append("</td>");
          mappingValidationErrorHtml.append("</tr>");
        }
        mappingValidationErrorHtml.append("</tbody>");
        mappingValidationErrorHtml.append("</table>");
        mappingValidationErrorHtml.append("<br/>");
      }
    }

    return mappingValidationErrorHtml.toString();
  }

  private String createClickFunction() throws JSONException {
    StringBuilder clickFunc = new StringBuilder();
    clickFunc.append("function(node, editor) {");
    clickFunc.append("var errorMessages = " + createJsonMessagesObject() + ";");
    if (clickFunction != null && clickFunction.length() > 0) {
      clickFunc.append(clickFunction);
    } else {
      // create default click function
      clickFunc.append("if(node.properties[\"oryx-name\"]||node.properties[\"oryx-title\"]) {");
      clickFunc.append("alert(\"Name: \" + node.properties[\"oryx-name\"] + \" (Sid: \" + node.resourceId + \")\");");
      clickFunc.append("}");
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
      return "";
    }
    JSONArray focusArr = new JSONArray();

    for (Entry<String, Map<String, List<String>>> entry : nodesToHighlight.entrySet()) {
      focusArr.put(highlightNodesMap((String) entry.getKey(), (Map<String, List<String>>) entry.getValue()));
    }

    return focusArr.toString();
  }

  private String createJsonMessagesObject() throws JSONException {
    JSONObject jsonMessageObj = new JSONObject();

    for (Entry<String, Map<String, List<String>>> entry : nodesToHighlight.entrySet()) {
      for (Entry<String, List<String>> mapEntry : entry.getValue().entrySet()) {
        jsonMessageObj.put(mapEntry.getKey(), mapEntry.getValue());
      }
    }

    return jsonMessageObj.toString();
  }

   public static void main(String[] args) throws JSONException {
   Map<String, List<String>> properties = new HashMap<String, List<String>>();
   ArrayList<String> list1 = new ArrayList<String>();
   list1.add("adasdsadsgffdgfd");
   list1.add("fdgdlkzjtlkhjk");
   ArrayList<String> list2 = new ArrayList<String>();
   list2.add("545354353453543adasdsadsgffdgfd");
   list2.add("3256879867468fdgdlkzjtlkhjk");
   properties.put("sid-43245-345-435-345", list1);
   properties.put("sid-87686-6456-4645-456", list2);
      
   SignavioSvgApiBuilder test = new SignavioSvgApiBuilder(null,
   null).highlightNodes(properties, "red").highlightNodes(properties, "blue");
   // System.out.println(test.createJsonMessagesObject());
   // System.out.println(test.buildHighlightning());
   System.out.println(test.buildHtml());
   }
}