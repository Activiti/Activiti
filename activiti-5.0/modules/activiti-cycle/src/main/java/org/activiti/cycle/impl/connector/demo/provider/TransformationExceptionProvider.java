package org.activiti.cycle.impl.connector.demo.provider;

import org.activiti.cycle.Content;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.impl.ContentProviderImpl;
import org.activiti.cycle.impl.connector.signavio.util.SignavioSvgApiBuilder;
import org.activiti.cycle.impl.connector.signavio.transform.TransformationException;

public class TransformationExceptionProvider extends ContentProviderImpl {

  @Override
  public void addValueToContent(Content content, RepositoryConnector connector, RepositoryArtifact artifact) {
    String renderContent = "";
    renderContent += "<html><head></head><body>"
          + "<script type=\"text/javascript\" src=\"http://signavio-core-components.googlecode.com/svn/trunk/api/src/signavio-svg.js\"></script>"
          + "<script type=\"text/plain\">" + "{"
          + "url: \"http://localhost:8080/activiti-modeler/p/model/root-directory;examples;VacationRequest;VacationRequest.signavio.xml\","
          + "click: function(node, editor){" + "if(node.properties[\"oryx-name\"]||node.properties[\"oryx-title\"]) {"
          + "alert(\"Name: \" + node.properties[\"oryx-name\"] + \" (SID: \" + node.resourceId + \")\");" + "}" + "}" + "}" + "</script>"
          + "</body></html>";
    
    throw new TransformationException("You wanted an exception, you get an exception :-)", renderContent);
  }

}
