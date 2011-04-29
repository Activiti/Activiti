package org.activiti.cycle.impl.connector.signavio.util;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Performs highlighting of nodes depending on {@link SignavioSvgHighlightType}
 * and {@link SignavioSvgNodeType}. Configure the highlighter using
 * {@link #setHighlightColor(SignavioSvgHighlightType, String)}.
 * 
 * @author daniel.meyer@camunda.com
 */
public class SignavioModelHighlighter {

  Logger log = Logger.getLogger(SignavioModelHighlighter.class.getName());

  public SortedMap<SignavioSvgHighlightType, Set<SignavioSvgHighlight>> highlights = new TreeMap<SignavioSvgHighlightType, Set<SignavioSvgHighlight>>(
          new SignavioSvgHighlightTypeComparator());

  public Map<SignavioSvgHighlightType, String> highlightColorMap = new HashMap<SignavioSvgHighlightType, String>();

  public void setHighlightColor(SignavioSvgHighlightType type, String color) {
    highlightColorMap.put(type, color);
  }

  public String getHighlightColor(SignavioSvgHighlightType type) {
    return highlightColorMap.get(type);
  }

  public SignavioModelHighlighter() {
    init();
  }

  private void init() {
    // set default colors:
    highlightColorMap.put(SignavioSvgHighlightType.ERROR, "red");
    highlightColorMap.put(SignavioSvgHighlightType.INFO, "grey");
    highlightColorMap.put(SignavioSvgHighlightType.WARNING, "#FFE736");
  }

  public void addHighlight(SignavioSvgHighlight highlight) {
    Set<SignavioSvgHighlight> highlightset = highlights.get(highlight.getHighlightType());
    if (highlightset == null) {
      highlightset = new HashSet<SignavioSvgHighlight>();
      highlights.put(highlight.getHighlightType(), highlightset);
    }
    highlightset.add(highlight);
  }

  public void participateInBuilder(StringWriter writer) {
    if (highlights.size() == 0) {
      writer.write("");
    }
    JSONArray focusArr = new JSONArray();
    for (Entry<SignavioSvgHighlightType, Set<SignavioSvgHighlight>> highlightset : highlights.entrySet()) {
      for (Object obj : doHighlighting(highlightset.getValue(), highlightset.getKey())) {
        focusArr.put(obj);
      }
    }
    if (focusArr.length() > 0) {
      String clickFunction = createClickFunction();
      if (clickFunction != null) {
        writer.write(" click: " + clickFunction + ",");
      }
      writer.write(" focus: " + focusArr.toString());
    }
  }

  private Object[] doHighlighting(Set<SignavioSvgHighlight> highlightset, SignavioSvgHighlightType highlightType) {
    Set<String> containers = new HashSet<String>();
    Set<String> nodes = new HashSet<String>();
    Set<String> connectors = new HashSet<String>();

    for (SignavioSvgHighlight signavioSvgHighlight : highlightset) {
      if (SignavioSvgNodeType.CONTAINER.equals(signavioSvgHighlight.getNodeType())) {
        containers.add(signavioSvgHighlight.getNodeId());
        continue;
      }
      if (SignavioSvgNodeType.CONNECTOR.equals(signavioSvgHighlight.getNodeType())) {
        connectors.add(signavioSvgHighlight.getNodeId());
        continue;
      }
      if (SignavioSvgNodeType.NODE.equals(signavioSvgHighlight.getNodeType())) {
        nodes.add(signavioSvgHighlight.getNodeId());
        continue;
      }
    }

    List<Object> result = new ArrayList<Object>();

    if (containers.size() > 0) {
      for (String containerId : containers) {
        result.add(containerId);
      }
    }

    try {
      if (nodes.size() > 0) {
        JSONObject obj = new JSONObject();
        obj.put("nodes", nodes);
        JSONObject attributes = new JSONObject();
        attributes.put("fill", highlightColorMap.get(highlightType));
        obj.put("attributes", attributes);
        result.add(obj);
      }
    } catch (JSONException e) {
      log.log(Level.WARNING, e.getMessage(), e);
    }
    try {
      if (connectors.size() > 0) {
        JSONObject obj = new JSONObject();
        obj.put("nodes", connectors);
        JSONObject attributes = new JSONObject();
        attributes.put("stroke", highlightColorMap.get(highlightType));
        obj.put("attributes", attributes);
        result.add(obj);
      }
    } catch (JSONException e) {
      log.log(Level.WARNING, e.getMessage(), e);
    }
    return result.toArray(new Object[0]);
  }

  private String createClickFunction() {
    try {
      StringBuilder clickFunc = new StringBuilder();
      clickFunc.append("function(node, editor) {");

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

      // clickFunc.append("var errorMessages = " + createJsonMessagesObject() +
      // ";");
      // clickFunc.append("if(node.properties[\"oryx-name\"] || node.properties[\"oryx-title\"] || node.resourceId) {");
      // clickFunc.append("alert(\"Name: \" + node.properties[\"oryx-name\"] + \" (Sid: \" + node.resourceId + \")\");");
      // clickFunc.append("}");

      clickFunc.append("}");

      return clickFunc.toString();
    } catch (JSONException e) {
      log.log(Level.WARNING, e.getMessage(), e);
      return null;
    }
  }

  private String createJsonMessagesObject() throws JSONException {
    if (highlights.isEmpty()) {
      return "\"\"";
    }

    Map<String, List<String>> messageMap = new HashMap<String, List<String>>();
    JSONObject jsonMessageObj = new JSONObject();
    // collect all messages per node.
    for (Entry<SignavioSvgHighlightType, Set<SignavioSvgHighlight>> entry : highlights.entrySet()) {
      for (SignavioSvgHighlight highlight : entry.getValue()) {
        List<String> messagesForThisNode = messageMap.get(highlight.getNodeId());
        if (messagesForThisNode == null) {
          messagesForThisNode = new ArrayList<String>();
          messageMap.put(highlight.getNodeId(), messagesForThisNode);
        }
        messagesForThisNode.add(highlight.getMessage());
      }
    }

    // build message element
    for (Entry<String, List<String>> messageEnty : messageMap.entrySet()) {
      jsonMessageObj.put(messageEnty.getKey(), messageEnty.getValue());
    }

    return jsonMessageObj.toString();
  }

}
