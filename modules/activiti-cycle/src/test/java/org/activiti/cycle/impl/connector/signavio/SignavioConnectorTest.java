package org.activiti.cycle.impl.connector.signavio;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.activiti.cycle.RepositoryFolder;
import org.activiti.cycle.RepositoryNode;
import org.activiti.cycle.impl.connector.signavio.transform.JsonTransformation;
import org.activiti.cycle.impl.connector.signavio.transform.pattern.AdjustShapeNamesForXmlNCName;
import org.activiti.cycle.impl.connector.signavio.transform.pattern.AdjustShapeNamesTransformation;
import org.activiti.cycle.impl.connector.signavio.transform.pattern.BpmnPoolExtraction;
import org.activiti.cycle.impl.connector.signavio.transform.pattern.ExchangeSignavioUuidWithName;
import org.activiti.cycle.impl.connector.signavio.transform.pattern.MakeNamesUnique;
import org.activiti.cycle.impl.connector.signavio.transform.pattern.ReplaceEmptyShapeNamesWithTypes;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SignavioConnectorTest {

  @Test
  public void test() {
    // TODO: Write :-)
  }
  
  @Ignore
  @Test
  public void testTransformation() throws IOException, JSONException {
    // NArf, we need a running signavio for this :-( So skipped for the moment
    
     InputStream is =
     this.getClass().getResourceAsStream("/org.activiti.cycle.impl.connector.signavio.transform.pattern/engine-pool.json");
     BufferedReader reader = new BufferedReader(new InputStreamReader(is));
     StringBuilder sb = new StringBuilder();
     String line = null;
     while ((line = reader.readLine()) != null) {
     sb.append(line + "\n");
     }
     is.close();
    
     String json = new SignavioConnector(null).transformJsonToBpmn20Xml(sb.toString());
     String transformedJson = new ExchangeSignavioUuidWithName().transform(new JSONObject(json)).toString();
        
     System.out.println(json);
     System.out.println(transformedJson);
  }
  
  @Ignore
  @Test
  public void testSignavioTransformations() throws IOException, JSONException, ParserConfigurationException, SAXException {
    // define transformations
    List<JsonTransformation> transformations = new ArrayList<JsonTransformation>();
    transformations.add(new BpmnPoolExtraction("Process Engine"));
    transformations.add(new ReplaceEmptyShapeNamesWithTypes());
    transformations.add(new ExchangeSignavioUuidWithName());
    
    // read file to xml document
    String file = "c:/Process Engine Pool.oryx.xml";
    FileInputStream fis = new FileInputStream(file);
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();
    Document document = db.parse(fis);
	  fis.close();
	  
	  // extract json-representation from xml document as string
	  NodeList list = document.getElementsByTagName("json-representation");
	  Node jsonNode = list.item(0);
	  String sourceJson = jsonNode.getTextContent();
	  System.out.println("SourceJSON:\n" + sourceJson);
    
	  // create signavio conf + connector
	  SignavioConnectorConfiguration conf = new SignavioConnectorConfiguration("http://localhost:8080/activiti-modeler/");
    SignavioConnector connector = new SignavioConnector(conf);
	  
    // test pre-transformation json to bpmn20 xml
	  String jsonXmlBeforeTransformation = connector.transformJsonToBpmn20Xml(sourceJson);
	  System.out.println("JSONXmlBeforeTransformation:\n" + jsonXmlBeforeTransformation);

	  // execute working transformation loop on json
    JSONObject jsonObj = new JSONObject(sourceJson);
    for (JsonTransformation trafo : transformations) {
      jsonObj = trafo.transform(jsonObj);
      System.out.println("Transformation(" + trafo.getClass().getSimpleName() + "):\n" + jsonObj);
    }
    
    System.out.println("After working transformations:\n" + jsonObj.toString());
    
	  // transform transformed json to bpmn20 xml
	  String processEngineBpmnXml = connector.transformJsonToBpmn20Xml(jsonObj.toString());
	  System.out.println("ProcessEngineBpmnXml:\n" + processEngineBpmnXml);
  }
  
  @Ignore
  @Test
  public void testActivitiCompliantBpmn20() throws Exception {
    // define transformations
    List<JsonTransformation> transformations = new ArrayList<JsonTransformation>();
    transformations.add(new BpmnPoolExtraction("Process Engine"));
    transformations.add(new AdjustShapeNamesTransformation());
//    transformations.add(new ExchangeSignavioUuidWithName());
    
    // create signavio conf + connector
    SignavioConnectorConfiguration conf = new SignavioConnectorConfiguration("editor-url with ending slash");
    conf.setLoginRequired(true);
    conf.setUser("user");
    conf.setPassword("xxx");
    SignavioConnector connector = (SignavioConnector) conf.createConnector();
    connector.login(conf.getUser(), conf.getPassword());
    String sourceJson = connector.getJsonResponse(conf.getModelUrl("modelId/json")).getEntity().getText();
    
    System.out.println(sourceJson);
    
    // test pre-transformation json to bpmn20 xml
    String jsonXmlBeforeTransformation = connector.transformJsonToBpmn20Xml(sourceJson);
    System.out.println("JSONXmlBeforeTransformation:\n" + jsonXmlBeforeTransformation);

    // execute working transformation loop on json
    JSONObject jsonObj = new JSONObject(sourceJson);
    for (JsonTransformation trafo : transformations) {
      jsonObj = trafo.transform(jsonObj);
      System.out.println("Transformation(" + trafo.getClass().getSimpleName() + "):\n" + jsonObj);
    }
    
    System.out.println("After working transformations:\n" + jsonObj.toString());
    
    // transform transformed json to bpmn20 xml
    String processEngineBpmnXml = connector.transformJsonToBpmn20Xml(jsonObj.toString());
    System.out.println("ProcessEngineBpmnXml:\n" + processEngineBpmnXml);
  }
  
  @Ignore
  @Test
  public void testOryx() {
    SignavioConnectorConfiguration conf = new OryxConnectorConfiguration("http://oryx-project.org/");
    SignavioConnector connector = new SignavioConnector(conf);
    List<RepositoryNode> childNodes = connector.getChildren("/").asList();
    assertTrue(childNodes.size() > 0);
  }
  
  @Ignore
  @Test
  public void testSignavioLogin() {
    SignavioConnectorConfiguration conf = new SignavioConnectorConfiguration("https://editor.signavio.com/");
    conf.setLoginRequired(true);
    SignavioConnector connector = new SignavioConnector(conf);
    connector.login("falko.menge@camunda.com", "not.my.password");
    List<RepositoryNode> childNodes = connector.getChildren("/").asList();
    assertTrue(childNodes.size() > 0);
  }

  @Ignore
  @Test
  public void testGetFolderInfo() throws IOException, JSONException {
    SignavioConnectorConfiguration conf = new SignavioConnectorConfiguration("https://editor.signavio.com/");
    conf.setLoginRequired(true);
    conf.setUser("user");
    conf.setPassword("pw");
    SignavioConnector connector = (SignavioConnector) conf.createConnector();
    connector.login(conf.getUser(), conf.getPassword());
    
    RepositoryFolder folder = connector.getRepositoryFolder("/407c45d65cad48548f7b2dac5cfde5fe");
    Assert.assertNotNull(folder);
    System.out.println(folder);
  }
}
