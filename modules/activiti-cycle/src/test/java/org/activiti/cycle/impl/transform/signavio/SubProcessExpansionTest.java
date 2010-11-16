package org.activiti.cycle.impl.transform.signavio;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.activiti.cycle.impl.connector.fs.FileSystemConnector;
import org.activiti.cycle.impl.connector.fs.FileSystemConnectorConfiguration;
import org.activiti.cycle.impl.connector.fs.SignavioFileSystemConnector;
import org.activiti.cycle.impl.plugin.PluginFinder;
import org.activiti.cycle.impl.transform.JsonTransformer;
import org.activiti.cycle.impl.util.IoUtils;
import org.json.JSONException;
import org.junit.Test;


public class SubProcessExpansionTest {

  @Test
  public void testGetModelIdFromSignavioUrl() throws UnsupportedEncodingException {
    assertEquals("root-directory;sub-process-child.signavio.xml", SubProcessExpansion.getModelIdFromSignavioUrl("http://localhost:8080/activiti-modeler/p/editor?id=root-directory%3Bsub-process-child.signavio.xml"));
    assertEquals("aa4414840103420193a18de25ae4b446", SubProcessExpansion.getModelIdFromSignavioUrl("https://editor.signavio.com/p/model/aa4414840103420193a18de25ae4b446"));
  }

  @Test
  public void testTransform() throws FileNotFoundException, IOException, JSONException {
    String packageName = getClass().getPackage().getName();
    String path = "src/test/resources/" + packageName.replace(".", "/");
    String inputFileName = path + "/sub-process-parent.json";
    String expectedFileName = path + "/" + getClass().getSimpleName() + ".testTransform.expected.json";

    String input = IoUtils.readText(new FileInputStream(inputFileName));
    String expected = IoUtils.readText(new FileInputStream(expectedFileName));

    PluginFinder.checkPluginInitialization(); // needed for registering artifact types
    FileSystemConnector repositoryConnector = new SignavioFileSystemConnector(new FileSystemConnectorConfiguration("filesystem", new File(path)));

    JsonTransformer jsonTransformer = new JsonTransformer();
    jsonTransformer.addJsonTransformation(new SubProcessExpansion(repositoryConnector));
    
    String actual = jsonTransformer.transform(input).toString(4);
    assertEquals(expected, actual);
  }

}
