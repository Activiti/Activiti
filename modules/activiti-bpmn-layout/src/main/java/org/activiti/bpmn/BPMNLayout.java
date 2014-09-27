package org.activiti.bpmn;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mxgraph.layout.mxGraphLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;

/**
 * BPMNLayout
 * 
 * @author Shi Yaoqiang(shi_yaoqiang@yahoo.com)
 */
public class BPMNLayout extends mxGraphLayout {
  
  // NEW
  
  protected BpmnAutoLayout bpmnAutoLayout;
	
  public void setBpmnAutoLayout(BpmnAutoLayout bpmnAutoLayout) {
    this.bpmnAutoLayout = bpmnAutoLayout;
  }

  // NEW
  
  
  /**
	 * Specifies the orientation of the layout. Default is true.
	 */
	protected boolean horizontal;

	/**
	 * Specifies if edge directions should be inverted. Default is false.
	 */
	protected boolean invert;

	/**
	 * If the parent should be resized to match the width/height of the tree. Default is true.
	 */
	protected boolean resizeParent = true;

	/**
	 * Specifies if the tree should be moved to the top, left corner if it is inside a top-level layer. Default is true.
	 */
	protected boolean moveTree = true;

	/**
	 * Specifies if all edge points of traversed edges should be removed. Default is true.
	 */
	protected boolean resetEdges = true;

	/**
	 * Holds the levelDistance. Default is 40.
	 */
	protected int levelDistance = 40;

	/**
	 * Holds the nodeDistance. Default is 20.
	 */
	protected int nodeDistance = 20;

	/**
	 * 
	 * @param graph
	 */
	public BPMNLayout(mxGraph graph) {
		this(graph, true);
	}

	/**
	 * 
	 * @param graph
	 * @param horizontal
	 */
	public BPMNLayout(mxGraph graph, boolean horizontal) {
		this(graph, horizontal, false);
	}

	/**
	 * 
	 * @param graph
	 * @param horizontal
	 * @param invert
	 */
	public BPMNLayout(mxGraph graph, boolean horizontal, boolean invert) {
		super(graph);
		setUseBoundingBox(false);
		this.horizontal = horizontal;
		this.invert = invert;
	}

	public mxGraph getGraph() {
		return (mxGraph) graph;
	}
	
	/**
	 * Returns a boolean indicating if the given <mxCell> should be ignored as a vertex. This returns true if the cell
	 * has no connections.
	 * 
	 * @param vertex
	 *            Object that represents the vertex to be tested.
	 * @return Returns true if the vertex should be ignored.
	 */
	public boolean isVertexIgnored(Object vertex) {
		return super.isVertexIgnored(vertex) || graph.isSwimlane(vertex) || graph.getModel().getGeometry(vertex).isRelative()
				|| graph.getConnections(vertex).length == 0;
	}

	/**
	 * @return the horizontal
	 */
	public boolean isHorizontal() {
		return horizontal;
	}

	/**
	 * @param horizontal
	 *            the horizontal to set
	 */
	public void setHorizontal(boolean horizontal) {
		this.horizontal = horizontal;
	}

	/**
	 * @return the invert
	 */
	public boolean isInvert() {
		return invert;
	}

	/**
	 * @param invert
	 *            the invert to set
	 */
	public void setInvert(boolean invert) {
		this.invert = invert;
	}

	/**
	 * @return the resizeParent
	 */
	public boolean isResizeParent() {
		return resizeParent;
	}

	/**
	 * @param resizeParent
	 *            the resizeParent to set
	 */
	public void setResizeParent(boolean resizeParent) {
		this.resizeParent = resizeParent;
	}

	/**
	 * @return the moveTree
	 */
	public boolean isMoveTree() {
		return moveTree;
	}

	/**
	 * @param moveTree
	 *            the moveTree to set
	 */
	public void setMoveTree(boolean moveTree) {
		this.moveTree = moveTree;
	}

	/**
	 * @return the resetEdges
	 */
	public boolean isResetEdges() {
		return resetEdges;
	}

	/**
	 * @param resetEdges
	 *            the resetEdges to set
	 */
	public void setResetEdges(boolean resetEdges) {
		this.resetEdges = resetEdges;
	}

	/**
	 * @return the levelDistance
	 */
	public int getLevelDistance() {
		return levelDistance;
	}

	/**
	 * @param levelDistance
	 *            the levelDistance to set
	 */
	public void setLevelDistance(int levelDistance) {
		this.levelDistance = levelDistance;
	}

	/**
	 * @return the nodeDistance
	 */
	public int getNodeDistance() {
		return nodeDistance;
	}

	/**
	 * @param nodeDistance
	 *            the nodeDistance to set
	 */
	public void setNodeDistance(int nodeDistance) {
		this.nodeDistance = nodeDistance;
	}

	public void execute(Object parent) {
		mxIGraphModel model = graph.getModel();
		List<Object> roots = graph.findTreeRoots(parent, true, invert);
//		if (getGraph().isOrganizationElement(parent)) {
//			roots = Arrays.asList(graph.getSelectionCells());
//		}
		for (Object root : roots) {
			parent = model.getParent(root);
			
			if (isBoundaryEvent(root)) {
				parent = model.getParent(parent);
			}
			model.beginUpdate();

			try {
				TreeNode node = dfs(root, parent, null);

				if (node != null) {
					layout(node);

					double x0 = graph.getGridSize();
					double y0 = x0;

					if (!moveTree || parent == graph.getDefaultParent() || parent == graph.getCurrentRoot()) {
						mxGeometry g = model.getGeometry(root);
						if (g.isRelative()) {
							g = model.getGeometry(model.getParent(root));
						}
						if (g != null) {
							x0 = g.getX();
							y0 = g.getY();
						}
					}

					mxRectangle bounds = null;

					if (horizontal) {
						bounds = horizontalLayout(node, x0, y0, null);
					} else {
						bounds = verticalLayout(node, null, x0, y0, null);
					}

					if (bounds != null) {
						double dx = 0;
						double dy = 0;

						if (bounds.getX() < 0) {
							dx = Math.abs(x0 - bounds.getX());
						}

						if (bounds.getY() < 0) {
							dy = Math.abs(y0 - bounds.getY());
						}

						if (parent != null) {
							mxRectangle size = graph.getStartSize(parent);
							dx += size.getWidth();
							dy += size.getHeight();

							// Resize parent swimlane
							if (resizeParent && !graph.isCellCollapsed(parent)) {
								mxGeometry g = model.getGeometry(parent);

								if (g != null) {
									double width = bounds.getWidth() + size.getWidth() - bounds.getX() + 2 * x0;
									double height = bounds.getHeight() + size.getHeight() - bounds.getY() + 2 * y0;

									g = (mxGeometry) g.clone();

									if (g.getWidth() > width) {
										dx += (g.getWidth() - width) / 2;
									} else {
										g.setWidth(width);
									}

									if (g.getHeight() > height) {
										if (horizontal) {
											dy += (g.getHeight() - height) / 2;
										}
									} else {
										g.setHeight(height);
									}

									model.setGeometry(parent, g);
								}
							}
						}
						if (model.getParent(node.cell) != graph.getCurrentRoot() && model.getParent(node.cell) != graph.getDefaultParent()) {
							moveNode(node, dx, dy);
						}
					}
				}
			} finally {
				model.endUpdate();
			}
		}
	}
	
	protected boolean isBoundaryEvent(Object obj) {
	  if (obj instanceof mxCell) {
	    mxCell cell = (mxCell) obj;
	    return cell.getId().startsWith("boundary-event-");
	  }
	  return false;
	}

	/**
	 * Moves the specified node and all of its children by the given amount.
	 */
	protected void moveNode(TreeNode node, double dx, double dy) {
		node.x += dx;
		node.y += dy;
		apply(node, null);

		TreeNode child = node.child;

		while (child != null) {
			moveNode(child, dx, dy);
			child = child.next;
		}
	}

	/**
	 * Does a depth first search starting at the specified cell. Makes sure the specified swimlane is never left by the
	 * algorithm.
	 */
	protected TreeNode dfs(Object cell, Object parent, Set<Object> visited) {
		if (visited == null) {
			visited = new HashSet<Object>();
		}

		TreeNode node = null;

		mxIGraphModel model = graph.getModel();
		if (cell != null && !visited.contains(cell) && (!isVertexIgnored(cell) || isBoundaryEvent(cell))) {
			visited.add(cell);
			node = createNode(cell);

			TreeNode prev = null;
			Object[] out = graph.getEdges(cell, parent, invert, !invert, false);

			for (int i = 0; i < out.length; i++) {
				Object edge = out[i];

				if (!isEdgeIgnored(edge)) {
					// Resets the points on the traversed edge
					if (resetEdges) {
						setEdgePoints(edge, null);
					}

					// Checks if terminal in same swimlane
					Object target = graph.getView().getVisibleTerminal(edge, invert);
					TreeNode tmp = dfs(target, parent, visited);

					if (tmp != null && model.getGeometry(target) != null) {
						if (prev == null) {
							node.child = tmp;
						} else {
							prev.next = tmp;
						}

						prev = tmp;
					}
				}
			}
		}

		return node;
	}

	/**
	 * Starts the actual compact tree layout algorithm at the given node.
	 */
	protected void layout(TreeNode node) {
		if (node != null) {
			TreeNode child = node.child;

			while (child != null) {
				layout(child);
				child = child.next;
			}

			if (node.child != null) {
				attachParent(node, join(node));
			} else {
				layoutLeaf(node);
			}
		}
	}

	protected mxRectangle horizontalLayout(TreeNode node, double x0, double y0, mxRectangle bounds) {
		node.x += x0 + node.offsetX;
		node.y += y0 + node.offsetY;
		bounds = apply(node, bounds);
		TreeNode child = node.child;

		if (child != null) {
			bounds = horizontalLayout(child, node.x, node.y, bounds);
			double siblingOffset = node.y + child.offsetY;
			TreeNode s = child.next;

			while (s != null) {
				bounds = horizontalLayout(s, node.x + child.offsetX, siblingOffset, bounds);
				siblingOffset += s.offsetY;
				s = s.next;
			}
		}

		return bounds;
	}

	protected mxRectangle verticalLayout(TreeNode node, Object parent, double x0, double y0, mxRectangle bounds) {
		node.x += x0 + node.offsetY;
		node.y += y0 + node.offsetX;
		bounds = apply(node, bounds);
		TreeNode child = node.child;

		if (child != null) {
			bounds = verticalLayout(child, node, node.x, node.y, bounds);
			double siblingOffset = node.x + child.offsetY;
			TreeNode s = child.next;

			while (s != null) {
				bounds = verticalLayout(s, node, siblingOffset, node.y + child.offsetX, bounds);
				siblingOffset += s.offsetY;
				s = s.next;
			}
		}

		return bounds;
	}

	/**
	 * 
	 */
	protected void attachParent(TreeNode node, double height) {
		double x = nodeDistance + levelDistance;
		double y2 = (height - node.width) / 2 - nodeDistance;
		double y1 = y2 + node.width + 2 * nodeDistance - height;

		node.child.offsetX = x + node.height;
		if (isBoundaryEvent(node.cell)) {
			node.child.offsetY = y1 + node.child.width;
		} else {
			node.child.offsetY = y1;
		}

		node.contour.upperHead = createLine(node.height, 0, createLine(x, y1, node.contour.upperHead));
		node.contour.lowerHead = createLine(node.height, 0, createLine(x, y2, node.contour.lowerHead));
	}

	/**
	 * 
	 */
	protected void layoutLeaf(TreeNode node) {
		double dist = 2 * nodeDistance;

		node.contour.upperTail = createLine(node.height + dist, 0, null);
		node.contour.upperHead = node.contour.upperTail;
		node.contour.lowerTail = createLine(0, -node.width - dist, null);
		node.contour.lowerHead = createLine(node.height + dist, 0, node.contour.lowerTail);
	}

	/**
	 * 
	 */
	protected double join(TreeNode node) {
		double dist = 2 * nodeDistance;

		TreeNode child = node.child;
		node.contour = child.contour;
		double h = child.width + dist;
		double sum = h;
		child = child.next;

		while (child != null) {
			double d = merge(node.contour, child.contour);
			child.offsetY = d + h;
			child.offsetX = 0;
			h = child.width + dist;
			sum += d + h;
			child = child.next;
		}

		return sum;
	}

	/**
	 * 
	 */
	protected double merge(Polygon p1, Polygon p2) {
		double x = 0;
		double y = 0;
		double total = 0;

		Polyline upper = p1.lowerHead;
		Polyline lower = p2.upperHead;

		while (lower != null && upper != null) {
			double d = offset(x, y, lower.dx, lower.dy, upper.dx, upper.dy);
			y += d;
			total += d;

			if (x + lower.dx <= upper.dx) {
				x += lower.dx;
				y += lower.dy;
				lower = lower.next;
			} else {
				x -= upper.dx;
				y -= upper.dy;
				upper = upper.next;
			}
		}

		if (lower != null) {
			Polyline b = bridge(p1.upperTail, 0, 0, lower, x, y);
			p1.upperTail = (b.next != null) ? p2.upperTail : b;
			p1.lowerTail = p2.lowerTail;
		} else {
			Polyline b = bridge(p2.lowerTail, x, y, upper, 0, 0);

			if (b.next == null) {
				p1.lowerTail = b;
			}
		}

		p1.lowerHead = p2.lowerHead;

		return total;
	}

	/**
	 * 
	 */
	protected double offset(double p1, double p2, double a1, double a2, double b1, double b2) {
		double d = 0;

		if (b1 <= p1 || p1 + a1 <= 0) {
			return 0;
		}

		double t = b1 * a2 - a1 * b2;

		if (t > 0) {
			if (p1 < 0) {
				double s = p1 * a2;
				d = s / a1 - p2;
			} else if (p1 > 0) {
				double s = p1 * b2;
				d = s / b1 - p2;
			} else {
				d = -p2;
			}
		} else if (b1 < p1 + a1) {
			double s = (b1 - p1) * a2;
			d = b2 - (p2 + s / a1);
		} else if (b1 > p1 + a1) {
			double s = (a1 + p1) * b2;
			d = s / b1 - (p2 + a2);
		} else {
			d = b2 - (p2 + a2);
		}

		if (d > 0) {
			return d;
		}

		return 0;
	}

	/**
	 * 
	 */
	protected Polyline bridge(Polyline line1, double x1, double y1, Polyline line2, double x2, double y2) {
		double dx = x2 + line2.dx - x1;
		double dy = 0;
		double s = 0;

		if (line2.dx == 0) {
			dy = line2.dy;
		} else {
			s = dx * line2.dy;
			dy = s / line2.dx;
		}

		Polyline r = createLine(dx, dy, line2.next);
		line1.next = createLine(0, y2 + line2.dy - dy - y1, r);

		return r;
	}

	/**
	 * 
	 */
	protected TreeNode createNode(Object cell) {
		TreeNode node = new TreeNode(cell);

		mxRectangle geo = getVertexBounds(cell);

		if (geo != null) {
			if (horizontal) {
				node.width = geo.getHeight();
				node.height = geo.getWidth();
			} else {
				node.width = geo.getWidth();
				node.height = geo.getHeight();
			}
		}

		return node;
	}

	/**
	 * 
	 */
	protected mxRectangle apply(TreeNode node, mxRectangle bounds) {
		mxRectangle g = graph.getModel().getGeometry(node.cell);

		if (node.cell != null && g != null) {
			if (isVertexMovable(node.cell)) {
				g = setVertexLocation(node.cell, node.x, node.y);
			}

			if (bounds == null) {
				bounds = new mxRectangle(g.getX(), g.getY(), g.getWidth(), g.getHeight());
			} else {
				bounds = new mxRectangle(Math.min(bounds.getX(), g.getX()), Math.min(bounds.getY(), g.getY()), Math.max(bounds.getX() + bounds.getWidth(),
						g.getX() + g.getWidth()), Math.max(bounds.getY() + bounds.getHeight(), g.getY() + g.getHeight()));
			}
		}

		return bounds;
	}

	/**
	 * 
	 */
	protected Polyline createLine(double dx, double dy, Polyline next) {
		return new Polyline(dx, dy, next);
	}

	/**
	 * 
	 */
	protected static class TreeNode {
		/**
		 * 
		 */
		protected Object cell;

		/**
		 * 
		 */
		protected double x, y, width, height, offsetX, offsetY;

		/**
		 * 
		 */
		protected TreeNode child, next; // parent, sibling

		/**
		 * 
		 */
		protected Polygon contour = new Polygon();

		/**
		 * 
		 */
		public TreeNode(Object cell) {
			this.cell = cell;
		}

	}

	/**
	 * 
	 */
	protected static class Polygon {

		/**
		 * 
		 */
		protected Polyline lowerHead, lowerTail, upperHead, upperTail;

	}

	/**
	 * 
	 */
	protected static class Polyline {

		/**
		 * 
		 */
		protected double dx, dy;

		/**
		 * 
		 */
		protected Polyline next;

		/**
		 * 
		 */
		protected Polyline(double dx, double dy, Polyline next) {
			this.dx = dx;
			this.dy = dy;
			this.next = next;
		}

	}

}
