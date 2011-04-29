package org.activiti.cycle.impl.connector.signavio.transform.pattern;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.oryxeditor.server.diagram.Diagram;
import org.oryxeditor.server.diagram.Shape;

public class BpmnPoolExtraction extends OryxTransformation {

  public static final String DEFAULT_ENGINE_POOL_NAME = "Process Engine";
  
  protected final String poolName;

  public BpmnPoolExtraction(String poolName) {
    this.poolName = poolName;
  }

  @Override
  public Diagram transform(Diagram diagram) {
    ArrayList<Shape> childShapes = diagram.getChildShapes();
    ArrayList<Shape> extractedChildShapes = new ArrayList<Shape>();
    Shape pool = null;
    if ((pool = checkForProcessEnginePoolExecutable(childShapes)) == null) {
      if (poolName != null && poolName.length() > 0) {
        pool = checkForProcessEnginePoolName(childShapes, poolName);
      }
    }
    if (pool != null) {
      extractedChildShapes.add(pool);
      for (Shape childShape : childShapes) {
        if ("SequenceFlow".equals(childShape.getStencilId()) && edgeIsContainedIn(childShape, pool)) {
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

  private Shape checkForProcessEnginePoolExecutable(List<Shape> shapes) {
    Shape processEnginePool = null;
    for (Shape childShape : shapes) {
      if ("Pool".equals(childShape.getStencilId())) {
        // TODO: property is named isExecutable in BPMN2_0 XML Spec, isexecutable in Signavio
        String isExecutable = childShape.getProperty("isexecutable");
        if (isExecutable != null && isExecutable.equals("true")) {
          processEnginePool = childShape;
          break;
        }
      }
    }
    return processEnginePool;
  }

  private Shape checkForProcessEnginePoolName(List<Shape> shapes, String poolName) {
    Shape processEnginePool = null;
    for (Shape childShape : shapes) {
      if ("Pool".equals(childShape.getStencilId()) && (childShape.getProperty("name").contains(poolName))) {
        processEnginePool = childShape;
        break;
      }
    }
    return processEnginePool;
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
        // TODO: Christian: Find a better way to determine the difference
        // between the incoming sources
        // can be flowElements or activities in case of attached elements
        Shape incomingSource = incoming.getIncomings().get(0);
        if (incomingSource.getStencilId().equals("MessageFlow") || incomingSource.getStencilId().equals("SequenceFlow")) {
          incomingSource = incoming;
        }
        if (!shapeIsContainedIn(incomingSource, sourceShape)) {
          removeIncomings.add(incoming);
        }
      }
      if (log.isLoggable(Level.FINE)) {
        if (removeIncomings.size() > 0) {
          log.fine("Deleting Incomings for shape: " + shapeToClean.getProperty("name") + ", " + shapeToClean.getStencilId());
          for (Shape shape2 : removeIncomings) {
            log.fine("Delete Shape: " + shape2.getStencilId() + ", " + shape2.getResourceId() + " coming from "
                    + ((Shape) shape2.getIncomings().get(0)).getProperty("name") + "(" + ((Shape) shape2.getIncomings().get(0)).getStencilId() + ")");
          }
        }
      }
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
      if (log.isLoggable(Level.FINE)) {
        if (removeOutgoings.size() > 0) {
          log.fine("Deleting outgoings for shape: " + shapeToClean.getProperty("name") + ", " + shapeToClean.getStencilId());
          for (Shape shape2 : removeOutgoings) {
            log.fine("Delete Shape: " + shape2.getStencilId() + ", " + shape2.getResourceId() + " targeting " + shape2.getTarget().getProperty("name") + "("
                    + shape2.getStencilId() + ")");
          }
        }
      }
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
    if (shape.getChildShapes().size() == 0) {
      return;
    }
    for (Shape child : shape.getChildShapes()) {
      // recursion to iterate through all children of sourceShape
      doCleanupOnShapes(sourceShape, child);
    }
  }
}
