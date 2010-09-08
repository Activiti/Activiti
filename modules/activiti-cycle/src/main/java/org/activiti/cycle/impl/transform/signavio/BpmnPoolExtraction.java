package org.activiti.cycle.impl.transform.signavio;

import java.util.ArrayList;

import org.oryxeditor.server.diagram.Diagram;
import org.oryxeditor.server.diagram.Shape;

public class BpmnPoolExtraction extends OryxTransformation {

	protected final String poolName;

	public BpmnPoolExtraction(String poolName) {
		this.poolName = poolName;
	}

	@Override
	public Diagram transform(Diagram diagram) {
		ArrayList<Shape> childShapes = diagram.getChildShapes();
		ArrayList<Shape> extractedChildShapes = new ArrayList<Shape>();
		Shape pool = null;
		for (Shape childShape : childShapes) {
			if ("Pool".equals(childShape.getStencil().getId())
					&& (childShape.getProperty("name").contains(poolName))) {
				extractedChildShapes.add(childShape);
				pool = childShape;
				break;
			}
		}
		if (pool != null) {
			for (Shape childShape : childShapes) {
				if ("SequenceFlow".equals(childShape.getStencil().getId())
						&& edgeIsContainedIn(childShape, pool)) {
					extractedChildShapes.add(childShape);
				}
			}
			doCleanupOnShapes(pool, null);
			diagram.setChildShapes(extractedChildShapes);
			
		} else {
			// throw new BpmnPoolNotFoundException(poolName);
		}
		return diagram;
	}

	private boolean edgeIsContainedIn(Shape edge, Shape parent) {
		for (Shape incomingShape : edge.getIncomings()) {
			if (shapeIsContainedIn(incomingShape, parent)) {
				for (Shape outgoingShape : edge.getOutgoings()) {
					if (shapeIsContainedIn(outgoingShape, parent)) {
						return true;
					}
				}
				return false;
			}
		}
		return false;
	}

	private boolean shapeIsContainedIn(Shape child, Shape parent) {
		Shape parentOfChild = child.getParent();
		if (parentOfChild == null) {
			return false;
		} else if (parentOfChild == parent) {
			return true;
		} else {
			return shapeIsContainedIn(parentOfChild, parent);
		}
	}
	
	private void deleteAllIncomingReferencesNotContainedInShape(Shape sourceShape, Shape shapeToClean) {  
    ArrayList<Shape> incomings = shapeToClean.getIncomings();
    if (incomings != null && incomings.size() > 0) {
      ArrayList<Shape> removeIncomings = new ArrayList<Shape>();
      for (Shape incoming : incomings) {
        // TODO: Christian: Find a better way to determine the difference between the incoming sources 
        // can be flowElements or activities in case of attached elements
        Shape incomingSource = incoming.getIncomings().get(0);
        if (incomingSource.getStencilId().equals("MessageFlow") || incomingSource.getStencilId().equals("SequenceFlow")) {
          incomingSource = incoming;
        }
        if (!shapeIsContainedIn(incomingSource, sourceShape)) {
          removeIncomings.add(incoming);
        }
      }
//      if (removeIncomings.size() > 0) {
//        System.out.println("Deleting Incomings for shape: " + shapeToClean.getProperty("name") + ", " + shapeToClean.getStencilId());
//        for (Shape shape2 : removeIncomings) {
//          System.out.println("Delete Shape: " + shape2.getStencilId() + ", " + shape2.getResourceId() + " coming from " + ((Shape) shape2.getIncomings().get(0)).getProperty("name") + "(" + ((Shape) shape2.getIncomings().get(0)).getStencilId() + ")");
//        }
//      }
      incomings.removeAll(removeIncomings);
      shapeToClean.setIncomings(incomings);
    }
	}
	
	private void deleteAllOutgoingReferencesNotContainedInShape(Shape sourceShape, Shape shapeToClean) {
    ArrayList<Shape> outgoings = shapeToClean.getOutgoings();
    if (outgoings != null && outgoings.size() > 0) {
      ArrayList<Shape> removeOutgoings = new ArrayList<Shape>();
      for (Shape outgoing : outgoings) {
        Shape target = outgoing.getTarget();
        if (target != null) {
          if (!(shapeIsContainedIn(target, sourceShape))) {
            removeOutgoings.add(outgoing);
          }
        }
      }
//      if (removeOutgoings.size() > 0) {
//        System.out.println("Deleting outgoings for shape: " + shapeToClean.getProperty("name") + ", " + shapeToClean.getStencilId());
//        for (Shape shape2 : removeOutgoings) {
//          System.out.println("Delete Shape: " + shape2.getStencilId() + ", " + shape2.getResourceId() + " targeting " + shape2.getTarget().getProperty("name") + "(" + shape2.getStencilId() + ")");
//        }
//      }
      outgoings.removeAll(removeOutgoings);
      shapeToClean.setOutgoings(outgoings);
    }
	}
	
	private void doCleanupOnShapes(Shape sourceShape, Shape shape) {
	  // for first invoke
	  if (shape == null) {
	    shape = sourceShape;
	  }
	  deleteAllIncomingReferencesNotContainedInShape(sourceShape, shape);
	  deleteAllOutgoingReferencesNotContainedInShape(sourceShape, shape);
//	  System.out.println("");
	  if (shape.getChildShapes().size() == 0) {
	    return;
	  }
	  for (Shape child : shape.getChildShapes()) {
        // recursion to iterate through all children of sourceShape
	    doCleanupOnShapes(sourceShape, child);
    }
	}
}
