/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.engine.test.bpmn.webservice;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.bpmn.ItemDefinition;
import org.activiti.engine.impl.bpmn.Message;
import org.activiti.engine.impl.bpmn.Operation;
import org.activiti.engine.impl.bpmn.SimpleStructure;
import org.activiti.engine.impl.bpmn.WebServiceActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.transformer.Transformer;
import org.activiti.engine.impl.webservice.SyncWebServiceClient;
import org.activiti.engine.impl.webservice.WSOperation;
import org.activiti.engine.impl.webservice.WSService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * A test for the WebServiceActivity class
 * 
 * @author Esteban Robles Luna
 */
public class WebServiceActivityBehaviorTest {

  private SyncWebServiceClient client;
  private ActivityExecution execution;
  private Transformer inTransformer;
  private Transformer outTransformer;
  private Operation operation;
  private WebServiceActivityBehavior activity;
  private SimpleStructure inStructure;
  private Message inMessage;
  private SimpleStructure outStructure;
  private Message outMessage;

  @Before
  public void setUp() {
    client = mock(SyncWebServiceClient.class);
    execution = mock(ActivityExecution.class);
    inTransformer = mock(Transformer.class);
    outTransformer = mock(Transformer.class);
    
    inStructure = new SimpleStructure("id");
    ItemDefinition inItemDefinition = new ItemDefinition("id", inStructure);
    inMessage = new Message("id", inItemDefinition);
    
    outStructure = new SimpleStructure("id");
    ItemDefinition outItemDefinition = new ItemDefinition("id", outStructure);
    outMessage = new Message("id", outItemDefinition);
    
    operation = new Operation("idSetTo", "setTo", null, inMessage);
    WSService service = new WSService("counter", "http://counter.example", client);
    operation.setImplementation(new WSOperation("setTo", service));
    activity = new WebServiceActivityBehavior(operation);
  }
  
  @After
  public void tearDown() {
    verifyNoMoreInteractions(client);
    verifyNoMoreInteractions(execution);
    verifyNoMoreInteractions(inTransformer);
    verifyNoMoreInteractions(outTransformer);
  }
  
  @Test
  public void testWebServiceCallWithNeitherInNorOutTransformers() throws Exception {
    activity.addInVariable("valueToSet");
    activity.addOutVariable("resultOfCall");
    
    inStructure.setFieldName(0, "f1", String.class);
    outStructure.setFieldName(0, "fo1", String.class);
    operation.setOutMessage(outMessage);
    
    when(execution.getVariable("valueToSet")).thenReturn("11");
    when(client.send("setTo", new Object[] { "11" })).thenReturn(new Object[] { 33 });
    
    activity.execute(execution);
    
    verify(client).send("setTo", new Object[] { "11" });
    verify(execution).getVariable("valueToSet");
    verify(execution).setVariable("resultOfCall", 33);
  }
  
  @Test
  public void testWebServiceCallWithNoOutTransformers() throws Exception {
    activity.addInVariable("valueToSet");
    activity.addOutVariable("resultOfCall");

    inStructure.setFieldName(0, "f1", String.class);
    outStructure.setFieldName(0, "fo1", String.class);
    operation.setOutMessage(outMessage);

    activity.addInTransformer(inTransformer);
    
    when(execution.getVariable("valueToSet")).thenReturn("11");
    when(inTransformer.transform("11")).thenReturn(11);
    when(client.send("setTo", new Object[] { 11 })).thenReturn(new Object[] { 33 });
    
    activity.execute(execution);
    
    verify(client).send("setTo", new Object[] { 11 });
    verify(execution).getVariable("valueToSet");
    verify(execution).setVariable("resultOfCall", 33);
    verify(inTransformer).transform("11");
  }
  
  @Test
  public void testWebServiceCallWithNoInTransformers() throws Exception {
    activity.addInVariable("valueToSet");
    activity.addOutVariable("resultOfCall");

    inStructure.setFieldName(0, "f1", String.class);
    outStructure.setFieldName(0, "fo1", String.class);
    operation.setOutMessage(outMessage);

    activity.addOutTransformer(outTransformer);
    
    when(execution.getVariable("valueToSet")).thenReturn("11");
    when(outTransformer.transform(33)).thenReturn("The result was: 33");
    when(client.send("setTo", new Object[] { "11" })).thenReturn(new Object[] { 33 });
    
    activity.execute(execution);
    
    verify(client).send("setTo", new Object[] { "11" });
    verify(execution).getVariable("valueToSet");
    verify(execution).setVariable("resultOfCall", "The result was: 33");
    verify(outTransformer).transform(33);
  }
  
  @Test
  public void testWebServiceCallWithInAndOutTransformers() throws Exception {
    activity.addInVariable("valueToSet");
    activity.addOutVariable("resultOfCall");

    inStructure.setFieldName(0, "f1", String.class);
    outStructure.setFieldName(0, "fo1", String.class);
    operation.setOutMessage(outMessage);

    activity.addInTransformer(inTransformer);
    activity.addOutTransformer(outTransformer);
    
    when(execution.getVariable("valueToSet")).thenReturn("11");
    when(inTransformer.transform("11")).thenReturn(11);
    when(outTransformer.transform(33)).thenReturn("The result was: 33");
    when(client.send("setTo", new Object[] { 11 })).thenReturn(new Object[] { 33 });
    
    activity.execute(execution);
    
    verify(client).send("setTo", new Object[] { 11 });
    verify(execution).getVariable("valueToSet");
    verify(execution).setVariable("resultOfCall", "The result was: 33");
    verify(inTransformer).transform("11");
    verify(outTransformer).transform(33);
    verify(client).send("setTo", new Object[] { 11 });
  }
  
  @Test
  public void testInArgumentsSizeDoesNotMatch() {
    activity.addInVariable("valueToSet");
    activity.addInVariable("valueToSet2");
    activity.addOutVariable("resultOfCall");

    inStructure.setFieldName(0, "f1", String.class);
    inStructure.setFieldName(1, "f2", String.class);
    outStructure.setFieldName(0, "fo1", String.class);
    operation.setOutMessage(outMessage);

    activity.addInTransformer(inTransformer);

    try {
      activity.execute(execution);
      Assert.fail("Should have thrown an exception");
    } catch (ActivitiException e) {
      Assert.assertEquals("The size of IN arguments and transformers does not match", e.getMessage());
    } catch (Exception e) {
      Assert.fail("Should have thrown an ActivitiException");
    }
  }

  @Test
  public void testOutArgumentsSizeDoesNotMatch() {
    activity.addInVariable("valueToSet");
    activity.addOutVariable("resultOfCall");
    activity.addOutVariable("resultOfCall2");

    inStructure.setFieldName(0, "f1", String.class);
    outStructure.setFieldName(0, "fo1", String.class);
    outStructure.setFieldName(1, "fo2", String.class);
    operation.setOutMessage(outMessage);

    activity.addOutTransformer(outTransformer);
    
    try {
      activity.execute(execution);
      Assert.fail("Should have thrown an exception");
    } catch (ActivitiException e) {
      Assert.assertEquals("The size of OUT arguments and transformers does not match", e.getMessage());
    } catch (Exception e) {
      Assert.fail("Should have thrown an ActivitiException");
    }
  }
}