package org.activiti.cycle.impl.action.form;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.activiti.cycle.CycleComponentFactory;
import org.activiti.cycle.impl.ActivitiCycleTest;
import org.activiti.cycle.impl.action.fom.FormHandler;
import org.junit.Before;
import org.junit.Test;

public class FormHandlerTest extends ActivitiCycleTest {

  private FormHandler formHandler;
  private String form;
  private NameBean nameBean;

  @Before
  public void init() {
    form = loadForm("FormParserTestForm.html");
    formHandler = CycleComponentFactory.getCycleComponentInstance(FormHandler.class, FormHandler.class);
    nameBean = CycleComponentFactory.getCycleComponentInstance("nameBean", NameBean.class);
  }

  @Test
  public void testParseForm() {
    String processedForm = formHandler.parseForm(form);

    Assert.assertTrue(processedForm.contains(nameBean.getFirstname()));
    Assert.assertTrue(processedForm.contains(nameBean.getLastname()));
  }

  @Test
  public void testSetValues() {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("name", "Gonzo");
    parameters.put("lastname", "The Bear");
    formHandler.setValues(form, parameters);

    Assert.assertEquals("Gonzo", nameBean.getFirstname());
    Assert.assertEquals("The Bear", nameBean.getLastname());
  }

  private String loadForm(String string) {
    BufferedReader reader = null;
    try {
      InputStream is = this.getClass().getResourceAsStream(string);
      reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
      StringWriter resultWriter = new StringWriter();
      String line;
      while ((line = reader.readLine()) != null) {
        resultWriter.append(line + "\n");
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

}