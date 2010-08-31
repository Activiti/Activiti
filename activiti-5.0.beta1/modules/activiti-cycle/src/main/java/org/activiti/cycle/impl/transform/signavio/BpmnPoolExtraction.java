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
					&& poolName.equals(childShape.getProperty("name"))) {
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
			deleteAllOutgoingReferencesNotContainedInShape(pool, null);
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
	
	private void deleteAllOutgoingReferencesNotContainedInShape(Shape sourceShape, Shape childShape) {
	  // for first invoke
	  if (childShape == null) {
	    childShape = sourceShape;
	  }
	  if (childShape.getChildShapes().isEmpty()) {
	    return;
    }
	  for (Shape child : childShape.getChildShapes()) {
      ArrayList<Shape> outgoings = child.getOutgoings();
      ArrayList<Shape> removeOutgoings = new ArrayList<Shape>();
      if (!outgoings.isEmpty()) {
        for (Shape outgoing : outgoings) {
          Shape target = outgoing.getTarget();
          if (!(shapeIsContainedIn(target, sourceShape))) {
            removeOutgoings.add(outgoing);
          }
        }
        outgoings.removeAll(removeOutgoings);
        child.setOutgoings(outgoings);
      } else {
        // recursion to iterate through all children of sourceShape
        deleteAllOutgoingReferencesNotContainedInShape(sourceShape, child);
      }
    }
	}
}
