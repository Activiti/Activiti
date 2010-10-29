package org.activiti.cycle.impl.connector.signavio.util;

import java.util.Map;
import java.util.Map.Entry;

import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.impl.connector.signavio.SignavioConnector;


public class SignavioSvgApiBuilder {

  private static final String HEADER = "<html><head></head><body>";
  private static final String FOOTER = "</body></html>";
  private static final String SVGAPI_URL = "http://signavio-core-components.googlecode.com/svn/trunk/api/src/signavio-svg.js";

  private SignavioConnector connector;
  private RepositoryArtifact artifact;
  
  private String authToken;
  private String clickFunction;
  
  private Map<Map,String> nodes;
//  private Map nodes;
//  private String color;
  
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
   * Key: Signavio ID, Value: a string
   */
  public SignavioSvgApiBuilder highlightNodes(Map nodes, String color) {
    this.nodes.put(nodes, color);
    
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
  
  public String build() {
    String svgApiCall = "";
    
    svgApiCall += HEADER;
    svgApiCall += "<script type=\"text/javascript\" src=\"" + SVGAPI_URL + "\"></script>";
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
    
    svgApiCall += FOOTER;
    
    return svgApiCall;
  }
  
  private String buildMessages() {
    if (nodes == null || nodes.isEmpty()) {
      return "";
    }
    
    String mappingValidationErrorHtml = "<h2>Mapping validation issues</h2>";
    for (Object nodeMap : nodes.entrySet()) {
      Entry entryNodeMap = (Entry) nodeMap;
      // set color for list
      mappingValidationErrorHtml += "<ul style=\"" + (String) entryNodeMap.getValue() + "\">";
      for (Object errorObj : ((Map) entryNodeMap.getKey()).entrySet()) {
        Entry mappingValidationError = (Entry) errorObj;
        mappingValidationErrorHtml += "<li>SID: " + mappingValidationError.getKey() + " - Message: " + mappingValidationError.getValue() + "</li>";
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
  
  private String highlightNodesMap(Map nodes, String color) {
    if (nodes == null || nodes.isEmpty()) {
      return "";
    }
    // set default color if not provided
    if (color == null || color.length() == 0) {
      color = "red";
    }
    
    String highlightning = "{ nodes:[";
    for (Object obj : nodes.entrySet()) {
      Entry nodeEntry = (Entry) obj;
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
    if (nodes == null || nodes.isEmpty()) {
      return "";
    }
    
    String highlightning = ",focus: [";
    for (Object nodeMap : nodes.entrySet()) {
      Entry nodeEntry = (Entry) nodeMap;
      highlightning += highlightNodesMap((Map) nodeEntry.getKey(), (String) nodeEntry.getValue());
      highlightning += ", ";
    }
    // empty node for comma issues
    highlightning += "\"\"";
    highlightning += "]";
    
    return highlightning;  
  }
}
