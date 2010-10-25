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
  private Map nodes;
  private String color;
  
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
  
  public SignavioSvgApiBuilder highlightNodes(Map nodes, String color) {
    this.nodes = nodes;
    this.color = color;
    
    return this;
  }
  
//  public SignavioSvgApiBuilder highlightNode(String nodeId, String message, String color) {
//    
//    return this;
//  }
  
  public SignavioSvgApiBuilder authToken(String authToken) {
    this.authToken = authToken;
    
    return this;
  }
  
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
    svgApiCall += FOOTER;
    
    return svgApiCall;
  }
  
  private String createUrl() {
    return "url: \"" + connector.getConfiguration().getModelUrl(artifact.getNodeId()) + "\"";
  }
  
  private String createClickFunction() {
    if (clickFunction != null && clickFunction.length() > 0) {
      return ", " + clickFunction;
    } else {
      // create default click function
      String function = ", click: function(node, editor) {";
      function += "if(node.properties[\"oryx-name\"]||node.properties[\"oryx-title\"]) {";
      function += "alert(\"Name: \" + node.properties[\"oryx-name\"] + \" (Sid: \" + node.resourceId + \")\");";
      function += "} }";

      return function;
    }
  }
  
  private String highlightNode(String nodeId, String message, String color) {
    return "\"" + nodeId + "\", ";
  }
  
  private String buildHighlightning() {
    if (nodes == null || nodes.isEmpty()) {
      return "";
    }
    
    String highlightning = ",focus: [{";

    highlightning += "nodes:[";
    for (Object node : nodes.entrySet()) {
      Entry nodeEntry = (Entry) node;
      highlightning += highlightNode((String) nodeEntry.getKey(), (String) nodeEntry.getValue(), color);
    }
    // empty node for comma issues
    highlightning += "\"\"";
    highlightning += "],";
    if (color != null && color.length() > 0) {
      highlightning += "attributes:{ fill:\"" + color + "\" }";
    } else {
      highlightning += "attributes:{ fill:\"red\" }";
    }
    highlightning += "}]";
    
    return highlightning;  
  }
}
