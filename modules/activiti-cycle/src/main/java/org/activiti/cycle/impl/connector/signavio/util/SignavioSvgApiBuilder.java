package org.activiti.cycle.impl.connector.signavio.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.impl.connector.signavio.SignavioConnector;


public class SignavioSvgApiBuilder {

  public static final String HEADER = "<html><head></head><body>";
  public static final String FOOTER = "</body></html>";
  public static final String SVGAPI_URL = "http://signavio-core-components.googlecode.com/svn/trunk/api/src/signavio-svg.js";

  private SignavioConnector connector;
  private RepositoryArtifact artifact;
  
  private String authToken;
  private String clickFunction;
  
  /**
   * Map mapping a color for highlighting with a {@link Map} of node (ids) with
   * messages to show
   */
  private Map<String, Map<String, String>> nodesToHighlight = new HashMap<String, Map<String, String>>();
  
  /**
   * Constructor to create a SignavioSvgApiBuilder object.
   * @param connector required for constructing correct model url
   * @param artifact required for constructing correct model url
   */
  public SignavioSvgApiBuilder(SignavioConnector connector, RepositoryArtifact artifact) {
    this.connector = connector;
    this.artifact = artifact;
  }
  
//  public void test() {
//  String text = "";
//  text += "<html><head></head><body>"
//          // + "<div id=\"model\" style=\"height: 600px; width: 600px;\">"
//          + "<script type=\"text/javascript\" src=\"http://signavio-core-components.googlecode.com/svn/trunk/api/src/signavio-svg.js\"></script>"
//          + "<script type=\"text/plain\">" + "{"
//          + "url: \""
//          + connector.getConfiguration().getModelUrl(artifact.getOriginalNodeId())
//          + "\","
//          // + "overflowX: \"fit\","
//          // + "overflowY: \"fit\","
//          + "click: function(node, editor){" + "if(node.properties[\"oryx-name\"]||node.properties[\"oryx-title\"]) {"
//          + "alert(\"Name: \" + node.properties[\"oryx-name\"] + \" (SID: \" + node.resourceId + \")\");" + "}" + "}" + "}" + "</script>"
//          // + "</div>"
//          + "</body></html>";
  // focus:[ "sid-60AB9173-E9AA-4E2E-A7AE-1E270EF4E900",
  // {
  // properties: ["oryx-name", "oryx-title"],
  // position: "NW",
  // templateFn: function(values, node){
  // var name = values[0] || values[1];
  // name = name.slice(0, 5) + (name.length > 5?"...":"");
  // return "<t"+"ext stroke='none' y='-5'>"+(name)+"</text>";
  // }
  // },{
  // nodes:["sid-06EEE957-812F-4AB9-8C91-F64D052F6AB7"],
  // attributes:{ fill:"red" }
  // }
  // ]
//  }

  /**
   * Submit a map containing the nodes to be highlighted and in which color.
   * Key: Signavio ID, Value: Message to show
   */
  public SignavioSvgApiBuilder highlightNodes(Map<String, String> nodes, String color) {
    nodesToHighlight.put(color, nodes);    
    return this;
  }
  
  /**
   * Maybe required to get access to models in saas/enterprise signavio.
   * @param authToken authtoken for saas / enterprise signavio
   */
  public SignavioSvgApiBuilder authToken(String authToken) {
    this.authToken = authToken;    
    return this;
  }
  
  /**
   * A javascript function submitted as string. You have the javascript variables 'node' and 'editor' to your disposal.
   * @param clickFunction a string representation of a javascript function, e.g.
   * 'if(node.properties["oryx-name"]||node.properties["oryx-title"]) {
   *    alert("Name: " + node.properties["oryx-name"] + "\n(Sid: " + node.resourceId + ")");
   *  };'
   * @return
   */
  public SignavioSvgApiBuilder clickFunction(String clickFunction) {
    this.clickFunction = clickFunction;    
    return this;
  }

  /**
   * use buildHtml instead
   */
  @Deprecated
  public String build() {
    return buildHtml();
  }
  
  public String buildHtml() {
    return buildHtml(buildScript());
  }

  public static String buildHtml(String content) {
    return HEADER + content + FOOTER;
  }
  
  public static String buildHtml(String content, int height) {
    return HEADER + "<div id=\"model\" style=\"height: " + height + "px; width: 600px;\">" + content + FOOTER;
  }

  public String buildScript() {
    String svgApiCall = "<script type=\"text/javascript\" src=\"" + SVGAPI_URL + "\"></script>";
    svgApiCall += "<script type=\"text/plain\">";
    svgApiCall += "{";
    // url to svgapi script
    svgApiCall += createUrl();
    
    // if authToken is available
    if (authToken != null && authToken.length() > 0) {
      svgApiCall += ", authToken: \"" + authToken + "\",";
    }
    
    // executed when click on a shape
    svgApiCall += createClickFunction();

    // highlight nodes
    svgApiCall += buildHighlightning();
    
    svgApiCall += "}";
    svgApiCall += "</script>";
    
    // include messages as text
    svgApiCall += buildMessages();
    
    return svgApiCall;
  }
  
  private String buildMessages() {
    if (nodesToHighlight == null || nodesToHighlight.isEmpty()) {
      return "";
    }
    
    String mappingValidationErrorHtml = "<h2>Mapping validation issues</h2>";
    for (Entry<String, Map<String, String>> entry : nodesToHighlight.entrySet()) {
      // set color for list
      mappingValidationErrorHtml += "<ul style=\"" + (String) entry.getKey() + "\">";
      for (Entry<String, String> messageObject : entry.getValue().entrySet()) {
        mappingValidationErrorHtml += "<li>SID: " + messageObject.getKey() + " - Message: " + messageObject.getValue() + "</li>";
      }
      mappingValidationErrorHtml += "</ul>";
    }
    
    return mappingValidationErrorHtml;
  }

  private String createUrl() {
    return "url: \"" + connector.getConfiguration().getModelUrl(artifact.getNodeId()) + "\"";
  }
  
  private String createClickFunction() {
    String function = ", click : function(node, editor) {";
    
    if (clickFunction != null && clickFunction.length() > 0) {
      function += clickFunction;
    } else {
      // create default click function
      function += "if(node.properties[\"oryx-name\"]||node.properties[\"oryx-title\"]) {";
      function += "alert(\"Name: \" + node.properties[\"oryx-name\"] + \" (Sid: \" + node.resourceId + \")\");";
      function += "}";
    }
    function += "}";
    
    return function;
  }
  
  private String highlightNode(String nodeId, String message) {
    return "\"" + nodeId + "\", ";
  }
  
  private String highlightNodesMap(String color, Map<String, String> nodes) {
    if (nodes == null || nodes.isEmpty()) {
      return "";
    }
    // set default color if not provided
    if (color == null || color.length() == 0) {
      color = "red";
    }
    
    String highlightning = "{ nodes:[";
    for (Entry<String, String> nodeEntry : nodes.entrySet()) {
      highlightning += highlightNode((String) nodeEntry.getKey(), (String) nodeEntry.getValue());        
    }
    // empty node for comma issues
    highlightning += "\"\"";
    highlightning += "],";
    highlightning += "attributes:{ fill:\"" + color + "\" }";
    highlightning += "}";
    
    return highlightning;
  }
  
  private String buildHighlightning() {
    if (nodesToHighlight == null || nodesToHighlight.isEmpty()) {
      return "";
    }
    
    String highlightning = ",focus: [";
    for (Entry<String, Map<String, String>> entry : nodesToHighlight.entrySet()) {
      highlightning += highlightNodesMap((String) entry.getKey(), (Map<String, String>) entry.getValue());
      highlightning += ", ";
    }
    // empty node for comma issues
    highlightning += "\"\"";
    highlightning += "]";
    
    return highlightning;  
  }
}
