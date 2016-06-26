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
package com.activiti.image;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.imageio.ImageIO;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.Artifact;
import org.activiti.bpmn.model.Association;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.GraphicInfo;
import org.activiti.bpmn.model.Lane;
import org.activiti.bpmn.model.Pool;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class BpmnImageTest {
    
    @Test
    public void testSimpleImage() throws Exception {
        generateImage("simple.bpmn");
    }

    @Test
    public void testSubProcessWithTimerImage() throws Exception {
        generateImage("SubProcessWithTimer.bpmn");
    }
    
    @Test
    public void testBigImage() throws Exception {
        generateImage("big.bpmn");
    }
    
    @Test
    public void testBigScaledImage() throws Exception {
    	double scaleFactor = 1.0;
    	BpmnModel bpmnModel = getBpmnModel("big.bpmn");
        GraphicInfo diagramInfo = calculateDiagramSize(bpmnModel);
        if (diagramInfo.getWidth() > 300f) {
        	scaleFactor = diagramInfo.getWidth() / 300f;
        	scaleDiagram(bpmnModel, scaleFactor);
        }
        generateImage(bpmnModel, scaleFactor);
    }
    
    protected void generateImage(String file) throws Exception {
    	generateImage(file, 1.0);
    }
    
    protected void generateImage(String file, double scaleFactor) throws Exception {
        BpmnModel bpmnModel = getBpmnModel(file);
        generateImage(bpmnModel, scaleFactor);
    }
    
    protected void generateImage(BpmnModel bpmnModel, double scaleFactor) throws Exception {
        ProcessDiagramGenerator diagramGenerator = new DefaultProcessDiagramGenerator(scaleFactor);
        InputStream diagramStream = diagramGenerator.generatePngDiagram(bpmnModel, scaleFactor);
        FileOutputStream fileStream = new FileOutputStream("test.png");
        IOUtils.copy(diagramStream, fileStream);
        fileStream.flush();
        fileStream.close();
        assertNotNull(ImageIO.read(new File("test.png")));
    }
    
    protected BpmnModel getBpmnModel(String file) throws Exception {
    	BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
        InputStream xmlStream = this.getClass().getClassLoader().getResourceAsStream(file);
        XMLInputFactory xif = XMLInputFactory.newInstance();
        InputStreamReader in = new InputStreamReader(xmlStream);
        XMLStreamReader xtr = xif.createXMLStreamReader(in);
        BpmnModel bpmnModel = xmlConverter.convertToBpmnModel(xtr);
        return bpmnModel;
    }
    
    protected GraphicInfo calculateDiagramSize(BpmnModel bpmnModel) {
		GraphicInfo diagramInfo = new GraphicInfo();

	    for (Pool pool : bpmnModel.getPools()) {
	      GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(pool.getId());
	      double elementMaxX = graphicInfo.getX() + graphicInfo.getWidth();
	      double elementMaxY = graphicInfo.getY() + graphicInfo.getHeight();
	      
	      if (elementMaxX > diagramInfo.getWidth()) {
	    	  diagramInfo.setWidth(elementMaxX);
	      }
	      if (elementMaxY > diagramInfo.getHeight()) {
	    	  diagramInfo.setHeight(elementMaxY);
	      }
	    }
	    
	    for (Process process : bpmnModel.getProcesses()) {
	    	calculateWidthForFlowElements(process.getFlowElements(), bpmnModel, diagramInfo);
	    	calculateWidthForArtifacts(process.getArtifacts(), bpmnModel, diagramInfo);
	    }
	    return diagramInfo;
	}
	
	protected void scaleDiagram(BpmnModel bpmnModel, double scaleFactor) {
	    for (Pool pool : bpmnModel.getPools()) {
	      GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(pool.getId());
	      scaleGraphicInfo(graphicInfo, scaleFactor);
	    }
	    
	    for (Process process : bpmnModel.getProcesses()) {
	    	scaleFlowElements(process.getFlowElements(), bpmnModel, scaleFactor);
	    	scaleArtifacts(process.getArtifacts(), bpmnModel, scaleFactor);
	    	for (Lane lane : process.getLanes()) {
	    		scaleGraphicInfo(bpmnModel.getGraphicInfo(lane.getId()), scaleFactor);
			}
	    }
	}
	
	protected void calculateWidthForFlowElements(Collection<FlowElement> elementList, BpmnModel bpmnModel, GraphicInfo diagramInfo) {
	    for (FlowElement flowElement : elementList) {
	    	List<GraphicInfo> graphicInfoList = new ArrayList<GraphicInfo>();
	    	if (flowElement instanceof SequenceFlow) {
	    		graphicInfoList.addAll(bpmnModel.getFlowLocationGraphicInfo(flowElement.getId()));
	    	} else {
	    		graphicInfoList.add(bpmnModel.getGraphicInfo(flowElement.getId()));
	    	}
	    	
	    	processGraphicInfoList(graphicInfoList, diagramInfo);
	    }
	}
	
	protected void calculateWidthForArtifacts(Collection<Artifact> artifactList, BpmnModel bpmnModel, GraphicInfo diagramInfo) {
	    for (Artifact artifact : artifactList) {
	    	List<GraphicInfo> graphicInfoList = new ArrayList<GraphicInfo>();
	    	if (artifact instanceof Association) {
	    		graphicInfoList.addAll(bpmnModel.getFlowLocationGraphicInfo(artifact.getId()));
	    	} else {
	    		graphicInfoList.add(bpmnModel.getGraphicInfo(artifact.getId()));
	    	}
	    	
	    	processGraphicInfoList(graphicInfoList, diagramInfo);
	    }
	}
	
	protected void processGraphicInfoList(List<GraphicInfo> graphicInfoList, GraphicInfo diagramInfo) {
		for (GraphicInfo graphicInfo : graphicInfoList) {
    		double elementMaxX = graphicInfo.getX() + graphicInfo.getWidth();
	    	double elementMaxY = graphicInfo.getY() + graphicInfo.getHeight();
		      
	    	if (elementMaxX > diagramInfo.getWidth()) {
	    		diagramInfo.setWidth(elementMaxX);
	    	}
	    	if (elementMaxY > diagramInfo.getHeight()) {
	    		diagramInfo.setHeight(elementMaxY);
	    	}
    	}
	}
    
    protected void scaleFlowElements(Collection<FlowElement> elementList, BpmnModel bpmnModel, double scaleFactor) {
	    for (FlowElement flowElement : elementList) {
	    	List<GraphicInfo> graphicInfoList = new ArrayList<GraphicInfo>();
	    	if (flowElement instanceof SequenceFlow) {
	    		graphicInfoList.addAll(bpmnModel.getFlowLocationGraphicInfo(flowElement.getId()));
	    	} else {
	    		graphicInfoList.add(bpmnModel.getGraphicInfo(flowElement.getId()));
	    	}
	    	
	    	scaleGraphicInfoList(graphicInfoList, scaleFactor);
	    	
	    	if (flowElement instanceof SubProcess) {
	    		SubProcess subProcess = (SubProcess) flowElement;
	    		scaleFlowElements(subProcess.getFlowElements(), bpmnModel, scaleFactor);
	    	}
	    }
	}
	
	protected void scaleArtifacts(Collection<Artifact> artifactList, BpmnModel bpmnModel, double scaleFactor) {
	    for (Artifact artifact : artifactList) {
	    	List<GraphicInfo> graphicInfoList = new ArrayList<GraphicInfo>();
	    	if (artifact instanceof Association) {
	    		graphicInfoList.addAll(bpmnModel.getFlowLocationGraphicInfo(artifact.getId()));
	    	} else {
	    		graphicInfoList.add(bpmnModel.getGraphicInfo(artifact.getId()));
	    	}
	    	
	    	scaleGraphicInfoList(graphicInfoList, scaleFactor);
	    }
	}
	
	protected void scaleGraphicInfoList(List<GraphicInfo> graphicInfoList, double scaleFactor) {
		for (GraphicInfo graphicInfo : graphicInfoList) {
			scaleGraphicInfo(graphicInfo, scaleFactor);
		}
	}
	
	protected void scaleGraphicInfo(GraphicInfo graphicInfo, double scaleFactor) {
		graphicInfo.setX(graphicInfo.getX() / scaleFactor);
		graphicInfo.setY(graphicInfo.getY() / scaleFactor);
		graphicInfo.setWidth(graphicInfo.getWidth() / scaleFactor);
		graphicInfo.setHeight(graphicInfo.getHeight() / scaleFactor);
	}
}
