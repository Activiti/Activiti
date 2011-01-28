package org.activiti.cycle.impl.connector.signavio.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.impl.connector.signavio.SignavioConnectorConfiguration;
import org.activiti.cycle.impl.connector.signavio.SignavioConnectorInterface;

public class SignavioSvgApiBuilder {

  public static String SIGNAVIO_SVG_API_SCRIPT_TEMPLATE = "SignavioSvgApiScriptTemplate.xml";
  public static String SIGNAVIO_SVG_API_HTML_TEMPLATE = "SignavioSvgApiHtmlTemplate.xml";
  // TODO: fix absolute paths
  public static final String SVGAPI_URL_LOCAL = "http://localhost:8080/activiti-modeler/api/signavio-svg.js";
  // TODO: fix absolute paths
  public static final String SERVER_SCRIPT_URL = "http://localhost:8080/activiti-modeler";

  private SignavioConnectorInterface connector;
  
  private SignavioConnectorConfiguration connectorConfiguration;

  private RepositoryArtifact artifact;

  private SignavioModelHighlighter modelHighlighter = new SignavioModelHighlighter();

  private String htmlContent = "";

  private String zoomLevel = "100";

  private String script;

  public SignavioSvgApiBuilder(SignavioConnectorInterface connector, RepositoryArtifact artifact) {
    this.connector = connector;
    this.connectorConfiguration = (SignavioConnectorConfiguration) connector.getConfiguration();
    this.artifact = artifact;
  }

  public String buildHtml(String script, String htmlContent) {
    this.script = script;
    this.htmlContent = htmlContent;
    return buildHtml();
  }

  public String buildHtml() {
    // load template
    String template = loadTemplate(SIGNAVIO_SVG_API_HTML_TEMPLATE);
    if (template == null)
      return "";

    // add script
    template = template.replace("SCRIPT", buildScript());

    // add additional HTML content:
    template = template.replaceAll("HTML_CONTENT", htmlContent);

    return template;
  }

  public String buildScript(int zoomLevel) {
    this.zoomLevel = String.valueOf(zoomLevel);
    return buildScript();
  }

  public String buildScript() {
    if (script != null)
      return script;

    String template = loadTemplate(SIGNAVIO_SVG_API_SCRIPT_TEMPLATE);
    if (template == null)
      return "";
    // set properties
    template = template.replaceAll("SIGNAVIO_EDITOR_SRC", SVGAPI_URL_LOCAL);
    template = template.replaceAll("SIGNAVIO_MODEL_URL", connectorConfiguration.getModelUrl(artifact.getNodeId()));
    template = template.replaceAll("SIGNAVIO_SERVER_URL", connectorConfiguration.getSignavioUrl());
    template = template.replaceAll("SIGNAVIO_ZOOM", zoomLevel);

    // build highlights:
    StringWriter extensionsWriter = new StringWriter();
    modelHighlighter.participateInBuilder(extensionsWriter);
    template = template.replaceAll("HIGHLIGHTS", extensionsWriter.toString());

    return template;
  }

  private String loadTemplate(String template) {
    BufferedReader reader = null;
    try {
      InputStream is = this.getClass().getResourceAsStream(template);
      reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
      StringWriter resultWriter = new StringWriter();
      String line;
      while ((line = reader.readLine()) != null) {
        resultWriter.append(line);
      }
      reader.close();
      return resultWriter.toString();
    } catch (IOException e) {
      if (reader == null)
        return null;
      try {
        reader.close();
      } catch (IOException ex) {

      }
      return null;
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

  public SignavioSvgApiBuilder addHighlight(SignavioSvgHighlight highlight) {
    modelHighlighter.addHighlight(highlight);
    return this;
  }

  public SignavioSvgApiBuilder addHtmlContent(String additionalContent) {
    htmlContent = additionalContent;
    return this;
  }

  public SignavioSvgApiBuilder highlightNodes(List<SignavioSvgHighlight> nodes) {
    for (SignavioSvgHighlight signavioSvgHighlight : nodes) {
      addHighlight(signavioSvgHighlight);
    }
    return this;
  }

}