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

package org.activiti.kickstart.bpmn20.model.participant;

import java.util.ArrayList;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.activiti.kickstart.bpmn20.annotations.ChildElements;
import org.activiti.kickstart.bpmn20.model.BaseElement;
import org.activiti.kickstart.bpmn20.model.FlowElement;
import org.activiti.kickstart.bpmn20.model.Process;
import org.activiti.kickstart.bpmn20.util.EscapingStringAdapter;


/**
 * <p>Java class for tLaneSet complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tLaneSet">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.omg.org/bpmn20}tBaseElement">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.omg.org/bpmn20}lane" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tLaneSet", propOrder = {
    "name",
	"lanes"//,
    //"parentLane"
})
public class LaneSet
    extends BaseElement
{
	
	@XmlElementRef(type = Lane.class)
    protected List<Lane> lanes;
	
//	@XmlIDREF
//	@XmlAttribute
	@XmlTransient
	protected Lane parentLane;
	
//	@XmlIDREF
//	@XmlAttribute
	@XmlTransient
	protected Process process;
	
	@XmlAttribute
	@XmlJavaTypeAdapter(EscapingStringAdapter.class)
	protected String name;
	
	@XmlTransient
	public String _processType;
	@XmlTransient
	public String _isClosed;
	
	public void addChild(BaseElement child) {
		if(child instanceof Lane) {
			Lane lane = (Lane) child;
			this.getLanes().add(lane);
			lane.setLaneSet(this);
		}
	}
	
//	/**
//	 * Creates the lane compartment including all sub lane compartment for this
//	 * lane set.
//	 */
//	public LaneCompartment createLaneCompartment() {
//		LaneCompartment laneComp = new LaneCompartment();
//		laneComp.setId(Lane)
//	}
//	
	/**
	 * 
	 * @return All {@link FlowElement} that are contained in the {@link LaneSet}
	 */
	public List<FlowElement> getChildFlowElements() {
		ArrayList<FlowElement> deepestFlowElements = new ArrayList<FlowElement>();
		List<Lane> lanes = this.getDeepestLanes(this.getLanes()); 
		
		for(Lane lane : lanes) {
			deepestFlowElements.addAll(lane.getFlowNodeRef());
		}
		
		return deepestFlowElements;
	}
	
	/**
	 * Retrieve the deepest child lanes in a lane set
	 * @param lanes
	 * @return
	 */
	private List<Lane> getDeepestLanes(List<Lane> lanes) {
		ArrayList<Lane> laneList = new ArrayList<Lane>();
		if(lanes == null)
			return laneList;
		for(Lane lane : lanes) {
			if(lane.childLaneSet == null) 
				/* Deepest lane in lane tree */
				laneList.add(lane);
			else if(lane.getChildLaneSet(false).lanes != null && lane.getChildLaneSet(false).getLanes().size() > 0) {
				laneList.addAll(this.getDeepestLanes(lane.getChildLaneSet(false).getLanes()));
			} else {
//				laneList.add(lane);
			}
		}
		return laneList;
	}
	
	/**
	 * Returns all contained child lane and their children.
	 * 
	 * @return
	 */
	public List<Lane> getAllLanes() {
		List<Lane> laneList = new ArrayList<Lane>();
		for(Lane lane : this.getLanes()) {
			laneList.add(lane);
			laneList.addAll(lane.getLaneList());
		}
		
		return laneList;
	}
	
	/**
	 * Removes the child element from the underling lanes and child lane sets.
	 * @param child
	 */
	public void removeChild(BaseElement child) {
		for(Lane lane : this.getLanes()) {
			lane.getFlowNodeRef().remove(child);
			if(lane.childLaneSet != null) {
				lane.getChildLaneSet(false).removeChild(child);
			}
		}
	}
	
//	/**
//	 * Basic method for the conversion of BPMN2.0 to the editor's internal format. 
//	 * {@see BaseElement#toShape(BPMN2DiagramConverter)}
//	 * @param converterForShapeCoordinateLookup an instance of {@link BPMN2DiagramConverter}, offering several lookup methods needed for the conversion.
//	 */
//	  public Shape toShape(BPMN2DiagramConverter converterForShapeCoordinateLookup) {
//	    	Shape shape = super.toShape(converterForShapeCoordinateLookup);
//	    	
//	    	// This should not work...? according to the standard, a laneset contains lanes, it is just a container and no graphical element.
//	    	// > Well, thus it never shows up as a BPMNShape anyway... :D
//	    	shape.setStencil(new StencilType("Pool"));	   
//	    	
//	    	List<FlowElement> x = this.getChildFlowElements();
//	    	ArrayList<Shape> children = new ArrayList<Shape>();
//	    	for(FlowElement f : x){
//	    		children.add(new Shape(f.getId()));
//	    	}
//	    	shape.setChildShapes(children);
//	    	
//	    	this.getParentLane().addChild(this);
//	    	//this.getPool();
//	    	
//	    	return shape;
//	  }
	
	/* Getter & Setter */
	
    /**
     * Gets the value of the lane property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the lane property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLane().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Lane }
     * 
     * 
     */
	@ChildElements
    public List<Lane> getLanes() {
        if (this.lanes == null) {
            this.lanes = new ArrayList<Lane>();
        }
        return this.lanes;
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the parentLane
	 */
	public Lane getParentLane() {
		return parentLane;
	}

	/**
	 * @param parentLane the parentLane to set
	 */
	public void setParentLane(Lane parentLane) {
		this.parentLane = parentLane;
	}

	/**
	 * @return the process
	 */
	public Process getProcess() {
		return process;
	}

	/**
	 * @param process the process to set
	 */
	public void setProcess(Process process) {
		this.process = process;
	}

}
