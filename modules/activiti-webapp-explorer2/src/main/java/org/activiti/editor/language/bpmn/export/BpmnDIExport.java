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
package org.activiti.editor.language.bpmn.export;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamWriter;

import math.geom2d.Point2D;
import math.geom2d.conic.Circle2D;
import math.geom2d.line.Line2D;
import math.geom2d.polygon.Polyline2D;

import org.activiti.editor.json.constants.EditorJsonConstants;
import org.activiti.editor.stencilset.StencilConstants;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;


/**
 * @author Tijs Rademakers
 */
public class BpmnDIExport implements ActivitiNamespaceConstants, EditorJsonConstants, StencilConstants {
  
	private static final List<String> DI_CIRCLES = new ArrayList<String>();
	private static final List<String> DI_RECTANGLES = new ArrayList<String>();
	private static final List<String> DI_GATEWAY = new ArrayList<String>();
	
	static {
		DI_CIRCLES.add(STENCIL_EVENT_START_ERROR);
		DI_CIRCLES.add(STENCIL_EVENT_START_MESSAGE);
		DI_CIRCLES.add(STENCIL_EVENT_START_NONE);
		DI_CIRCLES.add(STENCIL_EVENT_START_SIGNAL);
		DI_CIRCLES.add(STENCIL_EVENT_START_TIMER);
		
		DI_CIRCLES.add(STENCIL_EVENT_BOUNDARY_ERROR);
		DI_CIRCLES.add(STENCIL_EVENT_BOUNDARY_SIGNAL);
		DI_CIRCLES.add(STENCIL_EVENT_BOUNDARY_TIMER);
		
		DI_CIRCLES.add(STENCIL_EVENT_CATCH_MESSAGE);
		DI_CIRCLES.add(STENCIL_EVENT_CATCH_SIGNAL);
		DI_CIRCLES.add(STENCIL_EVENT_CATCH_TIMER);
		
		DI_CIRCLES.add(STENCIL_EVENT_THROW_NONE);
		DI_CIRCLES.add(STENCIL_EVENT_THROW_SIGNAL);
		
		DI_CIRCLES.add(STENCIL_EVENT_END_NONE);
		
		DI_RECTANGLES.add(STENCIL_CALL_ACTIVITY);
		DI_RECTANGLES.add(STENCIL_SUB_PROCESS);
		DI_RECTANGLES.add(STENCIL_TASK_BUSINESS_RULE);
		DI_RECTANGLES.add(STENCIL_TASK_MANUAL);
		DI_RECTANGLES.add(STENCIL_TASK_RECEIVE);
		DI_RECTANGLES.add(STENCIL_TASK_SCRIPT);
		DI_RECTANGLES.add(STENCIL_TASK_SEND);
		DI_RECTANGLES.add(STENCIL_TASK_SERVICE);
		DI_RECTANGLES.add(STENCIL_TASK_USER);
		
		DI_GATEWAY.add(STENCIL_GATEWAY_EVENT);
		DI_GATEWAY.add(STENCIL_GATEWAY_EXCLUSIVE);
		DI_GATEWAY.add(STENCIL_GATEWAY_INCLUSIVE);
		DI_GATEWAY.add(STENCIL_GATEWAY_PARALLEL);
	}
	
  private XMLStreamWriter xtw;
  private ObjectNode modelNode;
  private Map<String, ObjectNode> shapeMap = new HashMap<String, ObjectNode>();
  private Map<String, ObjectNode> sourceRefMap = new HashMap<String, ObjectNode>();
  private Map<String, ObjectNode> edgeMap = new HashMap<String, ObjectNode>();
  private Map<String, List<ObjectNode>> sourceAndTargetMap = new HashMap<String, List<ObjectNode>>();

  public void createDIXML(ObjectNode modelNode, XMLStreamWriter inputXtw) throws Exception {
    xtw = inputXtw;
    this.modelNode = modelNode;
    xtw.writeStartElement(BPMNDI_PREFIX, "BPMNDiagram", BPMNDI_NAMESPACE);
    xtw.writeAttribute("id", "BPMNDiagram_" + BaseShapeHelper.getPropertyValueAsString(PROPERTY_PROCESS_ID, modelNode));

    xtw.writeStartElement(BPMNDI_PREFIX, "BPMNPlane", BPMNDI_NAMESPACE);
    xtw.writeAttribute("bpmnElement", BaseShapeHelper.getPropertyValueAsString(PROPERTY_PROCESS_ID, modelNode));
    xtw.writeAttribute("id", "BPMNPlane_" + BaseShapeHelper.getPropertyValueAsString(PROPERTY_PROCESS_ID, modelNode));
    
    filterAllShapes(modelNode, 0, 0);
    filterAllEdges(modelNode);
    
    writeShapes();
    writeEdges();
    
    xtw.writeEndElement();
    xtw.writeEndElement();
  }
  
  private void filterAllShapes(ObjectNode objectNode, double parentX, double parentY) throws Exception {
  	if (objectNode.get(EDITOR_CHILD_SHAPES) != null) {
	  	for (JsonNode jsonChildNode : objectNode.get(EDITOR_CHILD_SHAPES)) {
	  		
	  		ObjectNode childNode = (ObjectNode) jsonChildNode;
	  		String stencilId = getStencilId(childNode);
	      if (STENCIL_SEQUENCE_FLOW.equals(stencilId) == false) {
	      	
	      	JsonNode boundsNode = childNode.get(EDITOR_BOUNDS);
	      	ObjectNode upperLeftNode = (ObjectNode) boundsNode.get(EDITOR_BOUNDS_UPPER_LEFT);
	        double upperLeftX = upperLeftNode.get(EDITOR_BOUNDS_X).asDouble();
	        upperLeftNode.put(EDITOR_BOUNDS_X, upperLeftX + parentX);
	        double upperLeftY = upperLeftNode.get(EDITOR_BOUNDS_Y).asDouble();
	        upperLeftNode.put(EDITOR_BOUNDS_Y, upperLeftY + parentY);
	        
	        ObjectNode lowerRightNode = (ObjectNode) boundsNode.get(EDITOR_BOUNDS_LOWER_RIGHT);
	        double lowerRightX = lowerRightNode.get(EDITOR_BOUNDS_X).asDouble();
	        lowerRightNode.put(EDITOR_BOUNDS_X, lowerRightX + parentX);
	        double lowerRightY = lowerRightNode.get(EDITOR_BOUNDS_Y).asDouble();
	        lowerRightNode.put(EDITOR_BOUNDS_Y, lowerRightY + parentY);
	        
	        String childShapeId = childNode.get(EDITOR_SHAPE_ID).asText();
	      	shapeMap.put(childShapeId, childNode);
	      	
	      	ArrayNode outgoingNode = (ArrayNode) childNode.get("outgoing");
	        if (outgoingNode != null && outgoingNode.size() > 0) {
	          for (JsonNode outgoingChildNode : outgoingNode) {
	            JsonNode resourceNode = outgoingChildNode.get(EDITOR_SHAPE_ID);
	            if (resourceNode != null) {
	              sourceRefMap.put(resourceNode.asText(), childNode);
	            }
	          }
	        }
	      	
	      	filterAllShapes(childNode, upperLeftX, upperLeftY);
	      }
	    }
  	}
  }
  
  private void filterAllEdges(ObjectNode objectNode) throws Exception {
  	if (objectNode.get(EDITOR_CHILD_SHAPES) != null) {
	  	for (JsonNode jsonChildNode : objectNode.get(EDITOR_CHILD_SHAPES)) {
	  		
	  		ObjectNode childNode = (ObjectNode) jsonChildNode;
	  		String stencilId = getStencilId(childNode);
	  		if (STENCIL_SUB_PROCESS.equals(stencilId)) {
	  			filterAllEdges(childNode);
	  			
	  		} else if (STENCIL_SEQUENCE_FLOW.equals(stencilId)) {
	      	
	      	String childEdgeId = childNode.get(EDITOR_SHAPE_ID).asText();
	      	
	      	String targetRefId = childNode.get("target").get(EDITOR_SHAPE_ID).asText();
	        List<ObjectNode> sourceAndTargetList = new ArrayList<ObjectNode>();
	        sourceAndTargetList.add(sourceRefMap.get(childEdgeId));
	        sourceAndTargetList.add(shapeMap.get(targetRefId));
	      	
	      	edgeMap.put(childEdgeId, childNode);
	      	sourceAndTargetMap.put(childEdgeId, sourceAndTargetList);
	      }
	    }
  	}
  }
  
  private void writeShapes() throws Exception {
    for (ObjectNode childNode : shapeMap.values()) {
    	
    	xtw.writeStartElement(BPMNDI_PREFIX, "BPMNShape", BPMNDI_NAMESPACE);
      xtw.writeAttribute("bpmnElement", childNode.get(EDITOR_SHAPE_ID).asText());
      xtw.writeAttribute("id", "BPMNShape_" + childNode.get(EDITOR_SHAPE_ID).asText());
      
      JsonNode boundsNode = childNode.get(EDITOR_BOUNDS);
      BoundsLocation upperLeftLocation = getLocation(EDITOR_BOUNDS_UPPER_LEFT, boundsNode);
      BoundsLocation lowerRightLocation = getLocation(EDITOR_BOUNDS_LOWER_RIGHT, boundsNode);
      
      createBounds(upperLeftLocation.x, upperLeftLocation.y, 
          lowerRightLocation.x - upperLeftLocation.x, lowerRightLocation.y - upperLeftLocation.y);
      
      xtw.writeEndElement();
    }
  }
  
  private void writeEdges() throws Exception {
  	for (String edgeId : edgeMap.keySet()) {
  		
  		ObjectNode edgeNode = edgeMap.get(edgeId);
  		List<ObjectNode> sourceAndTargetList = sourceAndTargetMap.get(edgeId);
  		
	    xtw.writeStartElement(BPMNDI_PREFIX, "BPMNEdge", BPMNDI_NAMESPACE);
	    xtw.writeAttribute("bpmnElement", edgeId);
	    xtw.writeAttribute("id", "BPMNEdge_" + edgeId);
	    
	    ObjectNode sourceRefNode = sourceAndTargetList.get(0);
	    ObjectNode targetRefNode = sourceAndTargetList.get(1);
	    
	    JsonNode dockersNode = edgeNode.get(EDITOR_DOCKERS);
	    double sourceDockersX = dockersNode.get(0).get(EDITOR_BOUNDS_X).getDoubleValue();
	    double sourceDockersY = dockersNode.get(0).get(EDITOR_BOUNDS_Y).getDoubleValue();
	    
	    JsonNode sourceRefBoundsNode = sourceRefNode.get(EDITOR_BOUNDS);
	    BoundsLocation sourceRefUpperLeftLocation = getLocation(EDITOR_BOUNDS_UPPER_LEFT, sourceRefBoundsNode);
	    BoundsLocation sourceRefLowerRightLocation = getLocation(EDITOR_BOUNDS_LOWER_RIGHT, sourceRefBoundsNode);
	    
	    JsonNode targetRefBoundsNode = targetRefNode.get(EDITOR_BOUNDS);
	    BoundsLocation targetRefUpperLeftLocation = getLocation(EDITOR_BOUNDS_UPPER_LEFT, targetRefBoundsNode);
	    BoundsLocation targetRefLowerRightLocation = getLocation(EDITOR_BOUNDS_LOWER_RIGHT, targetRefBoundsNode);
	    
	    double sourceRefLineX = sourceRefUpperLeftLocation.x + sourceDockersX;
	    double sourceRefLineY = sourceRefUpperLeftLocation.y + sourceDockersY;
	    
	    double nextPointInLineX;
	    double nextPointInLineY;
	    
	    nextPointInLineX = dockersNode.get(1).get(EDITOR_BOUNDS_X).getDoubleValue();
	    nextPointInLineY = dockersNode.get(1).get(EDITOR_BOUNDS_Y).getDoubleValue();
	    if (dockersNode.size() == 2) {
	    	nextPointInLineX += targetRefUpperLeftLocation.x;
	    	nextPointInLineY += targetRefUpperLeftLocation.y;
	    }
	    
	    Line2D firstLine = new Line2D(sourceRefLineX, sourceRefLineY, nextPointInLineX, nextPointInLineY);
	    
	    String sourceRefStencilId = getStencilId(sourceRefNode);
	    String targetRefStencilId = getStencilId(targetRefNode);
	    
	    if (DI_CIRCLES.contains(sourceRefStencilId)) {
	    	Circle2D eventCircle = new Circle2D(sourceRefUpperLeftLocation.x + sourceDockersX, 
	    			sourceRefUpperLeftLocation.y + sourceDockersY, sourceDockersX);
	    	
	    	Collection<Point2D> intersections = eventCircle.intersections(firstLine);
	    	Point2D intersection = intersections.iterator().next();
	    	createWayPoint(intersection.getX(), intersection.getY(), xtw);
	    
	    } else if (DI_RECTANGLES.contains(sourceRefStencilId)) {
	    	Polyline2D rectangle = createRectangle(sourceRefUpperLeftLocation, sourceRefLowerRightLocation);
	    	
	    	Collection<Point2D> intersections = rectangle.intersections(firstLine);
	    	Point2D intersection = intersections.iterator().next();
	    	createWayPoint(intersection.getX(), intersection.getY(), xtw);
	    
	    } else if (DI_GATEWAY.contains(sourceRefStencilId)) {
	    	Polyline2D gatewayRectangle = createGateway(sourceRefUpperLeftLocation, sourceRefLowerRightLocation);
	    	
	    	Collection<Point2D> intersections = gatewayRectangle.intersections(firstLine);
	    	Point2D intersection = intersections.iterator().next();
	    	createWayPoint(intersection.getX(), intersection.getY(), xtw);
	    }
	    
	    Line2D lastLine = null;
	    
	    if (dockersNode.size() > 2) {
	    	for(int i = 1; i < dockersNode.size() - 1; i++) {
	    		double x = dockersNode.get(i).get(EDITOR_BOUNDS_X).getDoubleValue();
	        double y = dockersNode.get(i).get(EDITOR_BOUNDS_Y).getDoubleValue();
	        createWayPoint(x, y, xtw);
	    	}
	    	
	    	double startLastLineX = dockersNode.get(dockersNode.size() - 2).get(EDITOR_BOUNDS_X).getDoubleValue();
		    double startLastLineY = dockersNode.get(dockersNode.size() - 2).get(EDITOR_BOUNDS_Y).getDoubleValue();
		    
		    double endLastLineX = dockersNode.get(dockersNode.size() - 1).get(EDITOR_BOUNDS_X).getDoubleValue();
		    double endLastLineY = dockersNode.get(dockersNode.size() - 1).get(EDITOR_BOUNDS_Y).getDoubleValue();
		    
		    endLastLineX += targetRefUpperLeftLocation.x;
		    endLastLineY += targetRefUpperLeftLocation.y;
		    
		    lastLine = new Line2D(startLastLineX, startLastLineY, endLastLineX, endLastLineY);
	    	
	    } else {
	    	lastLine = firstLine;
	    }
	    
	    if (DI_RECTANGLES.contains(targetRefStencilId)) {
	    	Polyline2D rectangle = createRectangle(targetRefUpperLeftLocation, targetRefLowerRightLocation);
    		
    		Collection<Point2D> intersections = rectangle.intersections(lastLine);
      	Point2D intersection = intersections.iterator().next();
      	createWayPoint(intersection.getX(), intersection.getY(), xtw);
      	
    	} else if (DI_CIRCLES.contains(targetRefStencilId)) {
    		
    		double targetDockersX = dockersNode.get(dockersNode.size() - 1).get(EDITOR_BOUNDS_X).getDoubleValue();
  	    double targetDockersY = dockersNode.get(dockersNode.size() - 1).get(EDITOR_BOUNDS_Y).getDoubleValue();
    		
    		Circle2D eventCircle = new Circle2D(targetRefUpperLeftLocation.x + targetDockersX, 
    				targetRefUpperLeftLocation.y + targetDockersY, targetDockersX);
    		
    		Collection<Point2D> intersections = eventCircle.intersections(lastLine);
      	Point2D intersection = intersections.iterator().next();
      	createWayPoint(intersection.getX(), intersection.getY(), xtw);
      	
    	} else if (DI_GATEWAY.contains(targetRefStencilId)) {
	    	Polyline2D gatewayRectangle = createGateway(targetRefUpperLeftLocation, targetRefLowerRightLocation);
	    	
	    	Collection<Point2D> intersections = gatewayRectangle.intersections(lastLine);
	    	Point2D intersection = intersections.iterator().next();
	    	createWayPoint(intersection.getX(), intersection.getY(), xtw);
	    }
	    
	    xtw.writeEndElement();
  	}
  }
  
  private BoundsLocation getLocation(String name, JsonNode boundsNode) {
    ObjectNode boundsLocationNode = (ObjectNode) boundsNode.get(name);
    double x = boundsLocationNode.get(EDITOR_BOUNDS_X).getDoubleValue();
    double y = boundsLocationNode.get(EDITOR_BOUNDS_Y).getDoubleValue();
    BoundsLocation location = new BoundsLocation();
    location.x = x;
    location.y = y;
    return location;
  }
  
  private void createBounds(double x, double y, double width, double height) throws Exception {
    xtw.writeStartElement(OMGDC_PREFIX, "Bounds", OMGDC_NAMESPACE);
    xtw.writeAttribute("height", "" + height);
    xtw.writeAttribute("width", "" + width);
    xtw.writeAttribute("x", "" + x);
    xtw.writeAttribute("y", "" + y);
    xtw.writeEndElement();
  }
  
  private void createWayPoint(double x, double y, XMLStreamWriter xtw) throws Exception {
    xtw.writeStartElement(OMGDI_PREFIX, "waypoint", OMGDI_NAMESPACE);
    xtw.writeAttribute("x", "" + x);
    xtw.writeAttribute("y", "" + y);
    xtw.writeEndElement();
  }
  
  private class BoundsLocation {
    public double x;
    public double y;
  }
  
  private Polyline2D createRectangle(BoundsLocation upperLeft, BoundsLocation lowerRight) {
  	Polyline2D rectangle = new Polyline2D(new Point2D(upperLeft.x, upperLeft.y),
				new Point2D(lowerRight.x, upperLeft.y),
				new Point2D(lowerRight.x, lowerRight.y),
				new Point2D(upperLeft.x, lowerRight.y),
				new Point2D(upperLeft.x, upperLeft.y));
  	return rectangle;
  }
  
  private Polyline2D createGateway(BoundsLocation upperLeft, BoundsLocation lowerRight) {
  	
  	double middleX = upperLeft.x + ((lowerRight.x - upperLeft.x) / 2);
  	double middleY = upperLeft.y + ((lowerRight.y - upperLeft.y) / 2);
  	
  	Polyline2D gatewayRectangle = new Polyline2D(new Point2D(upperLeft.x, middleY),
				new Point2D(middleX, upperLeft.y),
				new Point2D(lowerRight.x, middleY),
				new Point2D(middleX, lowerRight.y),
				new Point2D(upperLeft.x, middleY));
  	
  	return gatewayRectangle;
  }
  
  protected String getStencilId(ObjectNode objectNode) {
  	String stencilId = null;
    ObjectNode stencilNode = (ObjectNode) objectNode.get(EDITOR_STENCIL);
    if (stencilNode != null && stencilNode.get(EDITOR_STENCIL_ID) != null) {
      stencilId = stencilNode.get(EDITOR_STENCIL_ID).asText();
    }
    return stencilId;
  }
}
