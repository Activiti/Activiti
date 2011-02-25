/**
 * Copyright (c) 2009
 * Philipp Giese, Sven Wagner-Boysen
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.activiti.kickstart.bpmn20.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.activiti.kickstart.bpmn20.annotations.ChildElements;
import org.activiti.kickstart.bpmn20.model.activity.AdHocSubProcess;
import org.activiti.kickstart.bpmn20.model.activity.CallActivity;
import org.activiti.kickstart.bpmn20.model.activity.SubProcess;
import org.activiti.kickstart.bpmn20.model.activity.Task;
import org.activiti.kickstart.bpmn20.model.activity.Transaction;
import org.activiti.kickstart.bpmn20.model.activity.type.BusinessRuleTask;
import org.activiti.kickstart.bpmn20.model.activity.type.ManualTask;
import org.activiti.kickstart.bpmn20.model.activity.type.ReceiveTask;
import org.activiti.kickstart.bpmn20.model.activity.type.ScriptTask;
import org.activiti.kickstart.bpmn20.model.activity.type.SendTask;
import org.activiti.kickstart.bpmn20.model.activity.type.ServiceTask;
import org.activiti.kickstart.bpmn20.model.activity.type.UserTask;
import org.activiti.kickstart.bpmn20.model.artifacts.Artifact;
import org.activiti.kickstart.bpmn20.model.artifacts.Group;
import org.activiti.kickstart.bpmn20.model.artifacts.TextAnnotation;
import org.activiti.kickstart.bpmn20.model.choreography.ChoreographyActivity;
import org.activiti.kickstart.bpmn20.model.choreography.ChoreographyTask;
import org.activiti.kickstart.bpmn20.model.choreography.SubChoreography;
import org.activiti.kickstart.bpmn20.model.connector.Association;
import org.activiti.kickstart.bpmn20.model.connector.Edge;
import org.activiti.kickstart.bpmn20.model.connector.SequenceFlow;
import org.activiti.kickstart.bpmn20.model.data_object.DataObject;
import org.activiti.kickstart.bpmn20.model.data_object.DataStore;
import org.activiti.kickstart.bpmn20.model.data_object.Message;
import org.activiti.kickstart.bpmn20.model.event.BoundaryEvent;
import org.activiti.kickstart.bpmn20.model.event.EndEvent;
import org.activiti.kickstart.bpmn20.model.event.Event;
import org.activiti.kickstart.bpmn20.model.event.IntermediateCatchEvent;
import org.activiti.kickstart.bpmn20.model.event.IntermediateThrowEvent;
import org.activiti.kickstart.bpmn20.model.event.StartEvent;
import org.activiti.kickstart.bpmn20.model.gateway.ComplexGateway;
import org.activiti.kickstart.bpmn20.model.gateway.EventBasedGateway;
import org.activiti.kickstart.bpmn20.model.gateway.ExclusiveGateway;
import org.activiti.kickstart.bpmn20.model.gateway.InclusiveGateway;
import org.activiti.kickstart.bpmn20.model.gateway.ParallelGateway;
import org.activiti.kickstart.bpmn20.model.misc.ProcessType;
import org.activiti.kickstart.bpmn20.model.participant.Lane;
import org.activiti.kickstart.bpmn20.model.participant.LaneSet;


/**
 * <p>Java class for tProcess complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tProcess">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.omg.org/bpmn20}tCallableElement">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.omg.org/bpmn20}auditing" minOccurs="0"/>
 *         &lt;element ref="{http://www.omg.org/bpmn20}monitoring" minOccurs="0"/>
 *         &lt;element ref="{http://www.omg.org/bpmn20}property" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.omg.org/bpmn20}laneSet" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.omg.org/bpmn20}flowElement" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.omg.org/bpmn20}artifact" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="supports" type="{http://www.w3.org/2001/XMLSchema}QName" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="processType" type="{http://www.omg.org/bpmn20}tProcessType" default="none" />
 *       &lt;attribute name="isClosed" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="definitionalCollaborationRef" type="{http://www.w3.org/2001/XMLSchema}QName" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlRootElement(name = "process")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tProcess", propOrder = {
//    "auditing",
//    "monitoring",
//    "property",
    "laneSet",
    "flowElement",
    "artifact",
    "supports",
    "isClosed",
    "isExecutable"
})
public class Process
    extends CallableElement
{

//    protected TAuditing auditing;
//    protected TMonitoring monitoring;
//    protected List<TProperty> property;
//	@XmlElementRefs({
//		/* Events */
//		@XmlElementRef(type = StartEvent.class),
//		@XmlElementRef(type = EndEvent.class),
//		@XmlElementRef(type = IntermediateThrowEvent.class),
//		@XmlElementRef(type = IntermediateCatchEvent.class),
//		
//		/* Activities */
//		@XmlElementRef(type = Task.class),
//		@XmlElementRef(type = ReceiveTask.class),
//		@XmlElementRef(type = ManualTask.class),
//		@XmlElementRef(type = ScriptTask.class),
//		@XmlElementRef(type = SendTask.class),
//		@XmlElementRef(type = ServiceTask.class),
//		@XmlElementRef(type = UserTask.class),
//		@XmlElementRef(type = BusinessRuleTask.class),
//		@XmlElementRef(type = SubProcess.class),
//		
//		/* Gateways */
//		@XmlElementRef(type = ExclusiveGateway.class),
//		@XmlElementRef(type = ParallelGateway.class),
//		@XmlElementRef(type = ComplexGateway.class),
//		@XmlElementRef(type = EventBasedGateway.class),
//		@XmlElementRef(type = InclusiveGateway.class),
//		
//		/* Edges */
//		@XmlElementRef(type = SequenceFlow.class),
//		
//		/* Artifacts / Data elements */
//		@XmlElementRef(type = DataObject.class),
//		@XmlElementRef(type = TextAnnotation.class),
//		@XmlElementRef(type = ITSystem.class),
//		
//		/* Partner */
//		@XmlElementRef(type = Participant.class)
//	})
	@XmlElementRef
    protected List<FlowElement> flowElement;
    @XmlElementRef
    protected List<Artifact> artifact;
    protected List<QName> supports;
    @XmlAttribute
    protected ProcessType processType;
    @XmlAttribute
    protected Boolean isClosed;
    @XmlAttribute
    protected boolean isExecutable;
    @XmlAttribute
    protected QName definitionalCollaborationRef;
    
    @XmlElement(type = LaneSet.class)
    protected List<LaneSet> laneSet;
    
    /**
     * Adds the child to the process's flow elements if possible.
     */
    public void addChild(BaseElement child) {
    	if(child instanceof Artifact) {
    		this.getArtifact().add((Artifact) child);
    	}
    	
    	else if(child instanceof FlowElement) {
    		this.getFlowElement().add((FlowElement) child);
    	}
    	
    	if(child instanceof FlowElement) {
    		((FlowElement) child).setProcess(this);
    	}
    }
    
    /**
     * Remove the child element from the process.
     * 
     * @param child
     * 		Child element to remove.
     */
    public void removeChild(BaseElement child) {
    	this.getArtifact().remove(child);
    	
    	this.getFlowElement().remove(child);
    	
    	removeFromLaneSet(child);
    }
    
    /**
     * Remove the element recursively from the lane set.
     */
    private void removeFromLaneSet(BaseElement child) {
    	if(this.laneSet != null) {
    		for(LaneSet laneSet : this.getLaneSet()) {
    			laneSet.removeChild(child);
    		}
    	}
    }
    
    
    /**
     * Determines whether the process contains choreograhy elements.
     * @return 
     * 		{@code true} if a {@link ChoreographyActivity} is contained 
     * 		<br />
     * 		{@code false} otherwise.
     */
    public boolean isChoreographyProcess() {
    	for(FlowElement flowEle : this.getFlowElement()) {
    		if(flowEle instanceof ChoreographyActivity) 
    			return true;
    	}
    	
    	return false;
    }
    
    public List<FlowElement> getFlowElementsForChoreography() {
    	ArrayList<FlowElement> elements = new ArrayList<FlowElement>();
    	for(FlowElement flowEle : this.getFlowElement()) {
    		elements.add(flowEle);
    		
    		/* Retrieve by associations connected messages */
    		for(Edge e : flowEle.getOutgoing()) {
    			if(e.getTargetRef() instanceof Message) {
    				elements.add(e);
    				elements.add(e.getTargetRef());
    			}
    		}
    		
    		for(Edge e : flowEle.getIncoming()) {
    			if(e.getSourceRef() instanceof Message) {
    				elements.add(e);
    				elements.add(e.getSourceRef());
    			}
    		}
    	}
    	
    	return elements;
    }
    
    /**
     * Retrieve all subprocesses and child subprocesses recursively.
     * 
     * @return
     * 		A flat list of the contained subprocesses.
     */
    public List<SubProcess> getSubprocessList() {
    	List<SubProcess> subprocesses = new ArrayList<SubProcess>();
    	
    	for(FlowElement flowEle : getFlowElement()) {
    		/* Process subprocess */
    		if(flowEle instanceof SubProcess) {
    			subprocesses.add((SubProcess) flowEle);
    			subprocesses.addAll(((SubProcess) flowEle).getSubprocessList());
    		}
    	}
    	
    	return subprocesses;
    }
    
    /**
     * Retrieves a list of subchoreographies contained in the process including
     * children.
     * 
     * @return
     */
    public List<SubChoreography> getSubChoreographyList() {
    	List<SubChoreography> subchoreographies = new ArrayList<SubChoreography>();
    	
    	for(FlowElement flowEle : getFlowElement()) {
    		/* Subchoreography */
    		if(flowEle instanceof SubChoreography) {
    			subchoreographies.add((SubChoreography) flowEle);
    			
    		}
    	}
    	
    	return subchoreographies;
    }
    
    /**
     * Returns a list of {@link Lane} participating in this process.
     * @return
     */
    public List<Lane> getAllLanes() {
    	List<Lane> laneList = new ArrayList<Lane>();
    	
    	if(this.getLaneSet() == null) {
    		return laneList;
    	}
    	
    	for(LaneSet laneSet : getLaneSet()) {
    		laneList.addAll(laneSet.getAllLanes());
    	}
    	
    	return laneList;
    }
    
    /* Getter & Setter */
    
    
    public List<LaneSet> getLaneSet() {
    	if(this.laneSet == null) {
    		this.laneSet = new ArrayList<LaneSet>();
    	}
    	return this.laneSet;
    }
    
    /**
     * Gets the value of the auditing property.
     * 
     * @return
     *     possible object is
     *     {@link TAuditing }
     *     
     */
//    public TAuditing getAuditing() {
//        return auditing;
//    }

    /**
     * Sets the value of the auditing property.
     * 
     * @param value
     *     allowed object is
     *     {@link TAuditing }
     *     
     */
//    public void setAuditing(TAuditing value) {
//        this.auditing = value;
//    }

    /**
     * Gets the value of the monitoring property.
     * 
     * @return
     *     possible object is
     *     {@link TMonitoring }
     *     
     */
//    public TMonitoring getMonitoring() {
//        return monitoring;
//    }

    /**
     * Sets the value of the monitoring property.
     * 
     * @param value
     *     allowed object is
     *     {@link TMonitoring }
     *     
     */
//    public void setMonitoring(TMonitoring value) {
//        this.monitoring = value;
//    }

    /**
     * Gets the value of the property property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the property property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProperty().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TProperty }
     * 
     * 
     */
//    public List<TProperty> getProperty() {
//        if (property == null) {
//            property = new ArrayList<TProperty>();
//        }
//        return this.property;
//    }

    /**
     * Gets the value of the laneSet property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the laneSet property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLaneSet().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LaneSet }
     * 
     * 
     */
//    public List<LaneSet> getLaneSet() {
//        if (laneSet == null) {
//            laneSet = new ArrayList<LaneSet>();
//        }
//        return this.laneSet;
//    }

    /**
     * Gets the value of the flowElement property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the flowElement property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFlowElement().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link ManualTask }{@code >}
     * {@link JAXBElement }{@code <}{@link TCallChoreographyActivity }{@code >}
     * {@link JAXBElement }{@code <}{@link Transaction }{@code >}
     * {@link JAXBElement }{@code <}{@link EndEvent }{@code >}
     * {@link JAXBElement }{@code <}{@link IntermediateCatchEvent }{@code >}
     * {@link JAXBElement }{@code <}{@link FlowElement }{@code >}
     * {@link JAXBElement }{@code <}{@link CallActivity }{@code >}
     * {@link JAXBElement }{@code <}{@link ComplexGateway }{@code >}
     * {@link JAXBElement }{@code <}{@link BoundaryEvent }{@code >}
     * {@link JAXBElement }{@code <}{@link StartEvent }{@code >}
     * {@link JAXBElement }{@code <}{@link ExclusiveGateway }{@code >}
     * {@link JAXBElement }{@code <}{@link BusinessRuleTask }{@code >}
     * {@link JAXBElement }{@code <}{@link ScriptTask }{@code >}
     * {@link JAXBElement }{@code <}{@link InclusiveGateway }{@code >}
     * {@link JAXBElement }{@code <}{@link DataObject }{@code >}
     * {@link JAXBElement }{@code <}{@link Event }{@code >}
     * {@link JAXBElement }{@code <}{@link ServiceTask }{@code >}
     * {@link JAXBElement }{@code <}{@link ChoreographyTask }{@code >}
     * {@link JAXBElement }{@code <}{@link DataStore }{@code >}
     * {@link JAXBElement }{@code <}{@link SubProcess }{@code >}
     * {@link JAXBElement }{@code <}{@link IntermediateThrowEvent }{@code >}
     * {@link JAXBElement }{@code <}{@link UserTask }{@code >}
     * {@link JAXBElement }{@code <}{@link SequenceFlow }{@code >}
     * {@link JAXBElement }{@code <}{@link EventBasedGateway }{@code >}
     * {@link JAXBElement }{@code <}{@link AdHocSubProcess }{@code >}
     * {@link JAXBElement }{@code <}{@link SendTask }{@code >}
     * {@link JAXBElement }{@code <}{@link ChoreographySubProcess }{@code >}
     * {@link JAXBElement }{@code <}{@link ReceiveTask }{@code >}
     * {@link JAXBElement }{@code <}{@link TImplicitThrowEvent }{@code >}
     * {@link JAXBElement }{@code <}{@link ParallelGateway }{@code >}
     * {@link JAXBElement }{@code <}{@link Task }{@code >}
     * 
     * 
     */
    @ChildElements
    public List<FlowElement> getFlowElement() {
        if (flowElement == null) {
            flowElement = new ArrayList<FlowElement>();
        }
        return this.flowElement;
    }

    /**
     * Gets the value of the artifact property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the artifact property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getArtifact().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link Artifact }{@code >}
     * {@link JAXBElement }{@code <}{@link Association }{@code >}
     * {@link JAXBElement }{@code <}{@link Group }{@code >}
     * {@link JAXBElement }{@code <}{@link TextAnnotation }{@code >}
     * 
     * 
     */
    public List<Artifact> getArtifact() {
        if (artifact == null) {
            artifact = new ArrayList<Artifact>();
        }
        return this.artifact;
    }

    /**
     * Gets the value of the supports property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the supports property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSupports().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link QName }
     * 
     * 
     */
    public List<QName> getSupports() {
        if (supports == null) {
            supports = new ArrayList<QName>();
        }
        return this.supports;
    }

    /**
     * Gets the value of the processType property.
     * 
     * @return
     *     possible object is
     *     {@link TProcessType }
     *     
     */
//    public TProcessType getProcessType() {
//        if (processType == null) {
//            return TProcessType.NONE;
//        } else {
//            return processType;
//        }
//    }

    /**
     * Sets the value of the processType property.
     * 
     * @param value
     *     allowed object is
     *     {@link TProcessType }
     *     
     */
//    public void setProcessType(TProcessType value) {
//        this.processType = value;
//    }

    /**
     * Gets the value of the isClosed property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isIsClosed() {
        if (isClosed == null) {
            return false;
        } else {
            return isClosed;
        }
    }

    /**
     * Sets the value of the isClosed property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsClosed(Boolean value) {
        this.isClosed = value;
    }

    public boolean isExecutable() {
		return isExecutable;
	}

	public void setExecutable(boolean isExecutable) {
		this.isExecutable = isExecutable;
	}

	/**
     * Gets the value of the definitionalCollaborationRef property.
     * 
     * @return
     *     possible object is
     *     {@link QName }
     *     
     */
    public QName getDefinitionalCollaborationRef() {
        return definitionalCollaborationRef;
    }

    /**
     * Sets the value of the definitionalCollaborationRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link QName }
     *     
     */
    public void setDefinitionalCollaborationRef(QName value) {
        this.definitionalCollaborationRef = value;
    }

	/**
	 * @return the processType
	 */
	public ProcessType getProcessType() {
		/* None as default value */
		if(this.processType == null)
			this.processType = ProcessType.NONE;
		
		return processType;
	}

	/**
	 * @param processType the processType to set
	 */
	public void setProcessType(ProcessType processType) {
		this.processType = processType;
	}

}
