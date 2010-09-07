package org.activiti.cycle.impl.connector.signavio;

import java.io.BufferedInputStream;
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

import org.activiti.cycle.impl.connector.signavio.action.CreateTechnicalBpmnXmlAction;
import org.activiti.cycle.impl.transform.JsonTransformation;
import org.activiti.cycle.impl.transform.signavio.AdjustShapeNamesTransformation;
import org.activiti.cycle.impl.transform.signavio.BpmnPoolExtraction;
import org.activiti.cycle.impl.transform.signavio.ExchangeSignavioUuidWithNameTransformation;
import org.json.JSONException;
import org.json.JSONObject;
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
     this.getClass().getResourceAsStream("/org/activiti/cycle/impl/transform/signavio/engine-pool.json");
     BufferedReader reader = new BufferedReader(new InputStreamReader(is));
     StringBuilder sb = new StringBuilder();
     String line = null;
     while ((line = reader.readLine()) != null) {
     sb.append(line + "\n");
     }
     is.close();
    
     String json = new SignavioConnector(null).transformJsonToBpmn20Xml(sb.toString());
     String transformedJson = new ExchangeSignavioUuidWithNameTransformation().transform(new JSONObject(json)).toString();
        
     System.out.println(json);
     System.out.println(transformedJson);
  }
  
  @Ignore
  @Test
  public void testSignavioTransformations() throws IOException, JSONException, ParserConfigurationException, SAXException {
    // define transformations
    List<JsonTransformation> transformations = new ArrayList<JsonTransformation>();
    transformations.add(new BpmnPoolExtraction("Process Engine"));
    transformations.add(new ExchangeSignavioUuidWithNameTransformation());
    transformations.add(new AdjustShapeNamesTransformation());
    
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
	  SignavioConnectorConfiguration conf = new SignavioConnectorConfiguration("http://localhost:8080/activiti-modeler/p/");
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
}
