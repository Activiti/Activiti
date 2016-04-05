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

NAMESPACE_SVG = "http://www.w3.org/2000/svg";
NAMESPACE_ORYX = "http://www.b3mn.org/oryx";


/**
 * Init namespaces
 */
if (!ORYX) {
    var ORYX = {};
}
if (!ORYX.Core) {
    ORYX.Core = {};
}


/**
 * @classDescription Abstract base class for all connections.
 * @extends {ORYX.Core.Shape}
 * @param options {Object}
 *
 * TODO da die verschiebung der Edge nicht ueber eine
 *  translation gemacht wird, die sich auch auf alle kind UIObjects auswirkt,
 *  muessen die kinder hier beim verschieben speziell betrachtet werden.
 *  Das sollte ueberarbeitet werden.
 *
 */
ORYX.Core.Edge = {
    /**
     * Constructor
     * @param {Object} options
     * @param {Stencil} stencil
     */
    construct: function(options, stencil, facade){
        arguments.callee.$.construct.apply(this, arguments);
        
        this.isMovable = true;
        this.isSelectable = true;
		
		this._dockerUpdated = false;
        
        this._markers = new Hash(); //a hash map of SVGMarker objects where keys are the marker ids
        this._paths = [];
        this._interactionPaths = [];
        this._dockersByPath = new Hash();
        this._markersByPath = new Hash();
		
		/* Data structures to store positioning information of attached child nodes */ 
		this.attachedNodePositionData = new Hash();
        
        //TODO was muss hier initial erzeugt werden?
        var stencilNode = this.node.childNodes[0].childNodes[0];
        stencilNode = ORYX.Editor.graft("http://www.w3.org/2000/svg", stencilNode, ['g', {
            "pointer-events": "painted"
        }]);
        
        //Add to the EventHandler
        this.addEventHandlers(stencilNode.parentNode);
        
        
        this._oldBounds = this.bounds.clone();
        
        //load stencil
        this._init(this._stencil.view());
        
        if (stencil instanceof Array) {
            this.deserialize(stencil);
        }
        
    },
    
    _update: function(force){
        if(this._dockerUpdated || this.isChanged || force) {
		  
			this.dockers.invoke("update");
			
	        if (false && (this.bounds.width() === 0 || this.bounds.height() === 0)) {
				var width = this.bounds.width();
				var height = this.bounds.height();
	            this.bounds.extend({
	                x: width === 0 ? 2 : 0,
	                y: height === 0 ? 2 : 0
	            });
	            this.bounds.moveBy({
	                x: width === 0 ? -1 : 0,
	                y: height === 0 ? -1 : 0
	            });
            
	        }
	        
	        // TODO: Bounds muss abhaengig des Eltern-Shapes gesetzt werden
	        var upL = this.bounds.upperLeft();
	        var oldUpL = this._oldBounds.upperLeft();
	        var oldWidth = this._oldBounds.width() === 0 ? this.bounds.width() : this._oldBounds.width();
	        var oldHeight = this._oldBounds.height() === 0 ? this.bounds.height() : this._oldBounds.height();
	        var diffX = upL.x - oldUpL.x;
	        var diffY = upL.y - oldUpL.y;
	        var diffWidth = (this.bounds.width() / oldWidth) || 1;
	        var diffHeight = (this.bounds.height() / oldHeight) || 1;
	        
	        this.dockers.each((function(docker){
	            // Unregister on BoundsChangedCallback
	            docker.bounds.unregisterCallback(this._dockerChangedCallback);
	            
	            // If there is any changes at the edge and is there is not an DockersUpdate
	            // set the new bounds to the docker
	            if (!this._dockerUpdated) {
	                docker.bounds.moveBy(diffX, diffY);
	                
	                if (diffWidth !== 1 || diffHeight !== 1) {
	                    var relX = docker.bounds.upperLeft().x - upL.x;
	                    var relY = docker.bounds.upperLeft().y - upL.y;
	                    
	                    docker.bounds.moveTo(upL.x + relX * diffWidth, upL.y + relY * diffHeight);
	                }
	            }
	            // Do Docker update and register on DockersBoundChange
	            docker.update();
	            docker.bounds.registerCallback(this._dockerChangedCallback);
	            
	        }).bind(this));
	        
	        if (this._dockerUpdated) {
	            var a = this.dockers.first().bounds.center();
	            var b = this.dockers.first().bounds.center();
	            
	            this.dockers.each((function(docker){
	                var center = docker.bounds.center();
	                a.x = Math.min(a.x, center.x);
	                a.y = Math.min(a.y, center.y);
	                b.x = Math.max(b.x, center.x);
	                b.y = Math.max(b.y, center.y);
	            }).bind(this));
	            
	            //set the bounds of the the association
	            this.bounds.set(Object.clone(a), Object.clone(b));
	        }
			
			upL = this.bounds.upperLeft(); oldUpL = this._oldBounds.upperLeft();
			diffWidth = (this.bounds.width() / (oldWidth||this.bounds.width())); diffHeight = (this.bounds.height() / (oldHeight||this.bounds.height())); 
	        diffX = upL.x - oldUpL.x; diffY = upL.y - oldUpL.y;
					
			//reposition labels
			this.getLabels().each(function(label) {
				
				if (label.getReferencePoint()){
					var ref = label.getReferencePoint();
					var from = ref.segment.from, to = ref.segment.to;
					if (!from || !from.parent || !to || !to.parent) {
						return;
					}
					
					var fromPosition = from.bounds.center(), toPosition = to.bounds.center();

					if (fromPosition.x === ref.segment.fromPosition.x && fromPosition.y === ref.segment.fromPosition.y &&
						toPosition.x === ref.segment.toPosition.x && toPosition.y === ref.segment.toPosition.y && !ref.dirty){
						return;
					}
					
					if (!this.parent.initializingShapes) {
						var oldDistance = ORYX.Core.Math.getDistanceBetweenTwoPoints(ref.segment.fromPosition, ref.segment.toPosition, ref.intersection);
						var newIntersection = ORYX.Core.Math.getPointBetweenTwoPoints(fromPosition, toPosition, isNaN(oldDistance) ? 0.5 : oldDistance);
						
						/**
						 * Set position
						 */
						// Get the orthogonal identity vector of the current segment
						var oiv = ORYX.Core.Math.getOrthogonalIdentityVector(fromPosition, toPosition);
						var isHor = Math.abs(oiv.y)===1, isVer = Math.abs(oiv.x)===1;
						oiv.x *= ref.distance; oiv.y *= ref.distance; 				// vector * distance
						oiv.x += newIntersection.x; oiv.y += newIntersection.y; 	// vector + the intersection point				
						var mx = isHor && ref.orientation && (ref.iorientation||ref.orientation).endsWith("r") ? -label.getWidth() : 0;		
						var my = isVer && ref.orientation && (ref.iorientation||ref.orientation).startsWith("l") ? -label.getHeight()+2 : 0;
						label.setX(oiv.x+mx); label.setY(oiv.y+my);
						
						// Update the reference point
						this.updateReferencePointOfLabel(label, newIntersection, from, to);
					} else {
						var oiv = ORYX.Core.Math.getOrthogonalIdentityVector(fromPosition, toPosition);
						oiv.x *= ref.distance; oiv.y *= ref.distance; // vector * distance
						oiv.x += ref.intersection.x; oiv.y += ref.intersection.y; // vector + the intersection point		
						label.setX(oiv.x); label.setY(oiv.y);
						ref.segment.fromPosition = fromPosition; ref.segment.toPosition = toPosition;		
					}
					
					return;	
				}
				
				// Update label position if no reference point is set
				if (label.position && !this.parent.initializingShapes){
					var x = label.position.x + (diffX * (diffWidth||1));
					if (x > this.bounds.lowerRight().x){
						x += this.bounds.width()-(this.bounds.width()/(diffWidth||1));
					}
					
					var y = label.position.y + (diffY * (diffHeight||1));
					if (y > this.bounds.lowerRight().y){
						y += this.bounds.height()-(this.bounds.height()/(diffHeight||1));
					}
					label.setX(x);label.setY(y);
					return;
				}
				
				switch (label.getEdgePosition()) {
					case "starttop":
						var angle = this._getAngle(this.dockers[0], this.dockers[1]);
						var pos = this.dockers.first().bounds.center();
						
						if (angle <= 90 || angle > 270) {
							label.horizontalAlign("left");
							label.verticalAlign("bottom");
							label.x = pos.x + label.getOffsetTop();
							label.y = pos.y - label.getOffsetTop();
							label.rotate(360 - angle, pos);
						} else {
							label.horizontalAlign("right");
							label.verticalAlign("bottom");
							label.x = pos.x - label.getOffsetTop();
							label.y = pos.y - label.getOffsetTop();
							label.rotate(180 - angle, pos);
						}
						
						break;

					case "startmiddle":
						var angle = this._getAngle(this.dockers[0], this.dockers[1]);
						var pos = this.dockers.first().bounds.center();
						
						if (angle <= 90 || angle > 270) {
							label.horizontalAlign("left");
							label.verticalAlign("bottom");
							label.x = pos.x + 2;
							label.y = pos.y + 4;
							label.rotate(360 - angle, pos);
						} else {
							label.horizontalAlign("right");
							label.verticalAlign("bottom");
							label.x = pos.x + 1;
							label.y = pos.y + 4;
							label.rotate(180 - angle, pos);
						}
						
						break;
												
					case "startbottom":
						var angle = this._getAngle(this.dockers[0], this.dockers[1]);
						var pos = this.dockers.first().bounds.center();
						
						if (angle <= 90 || angle > 270) {
							label.horizontalAlign("left");
							label.verticalAlign("top");
							label.x = pos.x + label.getOffsetBottom();
							label.y = pos.y + label.getOffsetBottom();
							label.rotate(360 - angle, pos);
						} else {
							label.horizontalAlign("right");
							label.verticalAlign("top");
							label.x = pos.x - label.getOffsetBottom();
							label.y = pos.y + label.getOffsetBottom();
							label.rotate(180 - angle, pos);
						}
						
						break;
					case "midtop":
						var numOfDockers = this.dockers.length;
						if(numOfDockers%2 == 0) {
							var angle = this._getAngle(this.dockers[numOfDockers/2-1], this.dockers[numOfDockers/2])
							var pos1 = this.dockers[numOfDockers/2-1].bounds.center();
							var pos2 = this.dockers[numOfDockers/2].bounds.center();
							var pos = {x:(pos1.x + pos2.x)/2.0, y:(pos1.y+pos2.y)/2.0};
							
							label.horizontalAlign("center");
							label.verticalAlign("bottom");
							label.x = pos.x;
							label.y = pos.y - label.getOffsetTop();
								
							if (angle <= 90 || angle > 270) {
								label.rotate(360 - angle, pos);
							} else {
								label.rotate(180 - angle, pos);
							}
						} else {
							var index = parseInt(numOfDockers/2);
							var angle = this._getAngle(this.dockers[index], this.dockers[index+1])
							var pos = this.dockers[index].bounds.center();
							
							if (angle <= 90 || angle > 270) {
								label.horizontalAlign("left");
								label.verticalAlign("bottom");
								label.x = pos.x + label.getOffsetTop();
								label.y = pos.y - label.getOffsetTop();
								label.rotate(360 - angle, pos);
							} else {
								label.horizontalAlign("right");
								label.verticalAlign("bottom");
								label.x = pos.x - label.getOffsetTop();
								label.y = pos.y - label.getOffsetTop();
								label.rotate(180 - angle, pos);
							}
						}
						
						break;
					case "midbottom":
						var numOfDockers = this.dockers.length;
						if(numOfDockers%2 == 0) {
							var angle = this._getAngle(this.dockers[numOfDockers/2-1], this.dockers[numOfDockers/2])
							var pos1 = this.dockers[numOfDockers/2-1].bounds.center();
							var pos2 = this.dockers[numOfDockers/2].bounds.center();
							var pos = {x:(pos1.x + pos2.x)/2.0, y:(pos1.y+pos2.y)/2.0};
							
							label.horizontalAlign("center");
							label.verticalAlign("top");
							label.x = pos.x;
							label.y = pos.y + label.getOffsetTop();
							
							if (angle <= 90 || angle > 270) {
								label.rotate(360 - angle, pos);
							} else {
								label.rotate(180 - angle, pos);
							}
						} else {
							var index = parseInt(numOfDockers/2);
							var angle = this._getAngle(this.dockers[index], this.dockers[index+1])
							var pos = this.dockers[index].bounds.center();
							
							if (angle <= 90 || angle > 270) {
								label.horizontalAlign("left");
								label.verticalAlign("top");
								label.x = pos.x + label.getOffsetBottom();
								label.y = pos.y + label.getOffsetBottom();
								label.rotate(360 - angle, pos);
							} else {
								label.horizontalAlign("right");
								label.verticalAlign("top");
								label.x = pos.x - label.getOffsetBottom();
								label.y = pos.y + label.getOffsetBottom();
								label.rotate(180 - angle, pos);
							}
						}
						
						break;
					case "endtop":
						var length = this.dockers.length;
						var angle = this._getAngle(this.dockers[length-2], this.dockers[length-1]);
						var pos = this.dockers.last().bounds.center();
						
						if (angle <= 90 || angle > 270) {
							label.horizontalAlign("right");
							label.verticalAlign("bottom");
							label.x = pos.x - label.getOffsetTop();
							label.y = pos.y - label.getOffsetTop();
							label.rotate(360 - angle, pos);
						} else {
							label.horizontalAlign("left");
							label.verticalAlign("bottom");
							label.x = pos.x + label.getOffsetTop();
							label.y = pos.y - label.getOffsetTop();
							label.rotate(180 - angle, pos);
						}
						
						break;
					case "endbottom":
						var length = this.dockers.length;
						var angle = this._getAngle(this.dockers[length-2], this.dockers[length-1]);
						var pos = this.dockers.last().bounds.center();
						
						if (angle <= 90 || angle > 270) {
							label.horizontalAlign("right");
							label.verticalAlign("top");
							label.x = pos.x - label.getOffsetBottom();
							label.y = pos.y + label.getOffsetBottom();
							label.rotate(360 - angle, pos);
						} else {
							label.horizontalAlign("left");
							label.verticalAlign("top");
							label.x = pos.x + label.getOffsetBottom();
							label.y = pos.y + label.getOffsetBottom();
							label.rotate(180 - angle, pos);
						}
						
						break;
				}
			}.bind(this));
			
			this.children.each(function(value) {
				if(value instanceof ORYX.Core.Node) {
					this.calculatePositionOfAttachedChildNode.call(this, value);
				}
			}.bind(this));
			
			this.refreshAttachedNodes();
			this.refresh();
			
			this.isChanged = false;
			this._dockerUpdated = false;
			
			this._oldBounds = this.bounds.clone();
        }
		
		
  	    // IE10 specific fix, start and end-markes get left behind when moving path
		var userAgent = navigator.userAgent;
		if (navigator.appVersion.indexOf("MSIE 10") !== -1 || (userAgent.indexOf('Trident') !== -1 && userAgent.indexOf('rv:11') !== -1)) 
		{
			this.node.parentNode.insertBefore(this.node, this.node);
		}
    },
	
	/**
	 *  Moves a point to the upperLeft of a node's bounds.
	 *  
	 *  @param {point} point
	 *  	The point to move
	 *  @param {ORYX.Core.Bounds} bounds
	 *  	The Bounds of the related noe
	 */
	movePointToUpperLeftOfNode: function(point, bounds) {
		point.x -= bounds.width()/2;
		point.y -= bounds.height()/2;
	},
	
	/**
	 * Refreshes the visual representation of edge's attached nodes.
	 */	
	refreshAttachedNodes: function() {
		this.attachedNodePositionData.values().each(function(nodeData) {
			var startPoint = nodeData.segment.docker1.bounds.center();
			var endPoint = nodeData.segment.docker2.bounds.center();
			this.relativizePoint(startPoint);
			this.relativizePoint(endPoint);
			
			var newNodePosition = new Object();
			
			/* Calculate new x-coordinate */
			newNodePosition.x = startPoint.x 
								+ nodeData.relativDistanceFromDocker1
									* (endPoint.x - startPoint.x);
			
			/* Calculate new y-coordinate */
			newNodePosition.y = startPoint.y 
								+ nodeData.relativDistanceFromDocker1
									* (endPoint.y - startPoint.y);
			
			/* Convert new position to the upper left of the node */
			this.movePointToUpperLeftOfNode(newNodePosition, nodeData.node.bounds);
			
			/* Move node to its new position */
			nodeData.node.bounds.moveTo(newNodePosition);
			nodeData.node._update();					
			
		}.bind(this));
	},
	
	/**
	 * Calculates the position of an edge's child node. The node is placed on 
	 * the path of the edge.
	 * 
	 * @param {node}
	 * 		The node to calculate the new position
	 * @return {Point}
	 * 		The calculated upper left point of the node's shape.
	 */
	calculatePositionOfAttachedChildNode: function(node) {
		/* Initialize position */
		var position = new Object();
		position.x = 0;
		position.y = 0;
		
		/* Case: Node was just added */
		if(!this.attachedNodePositionData[node.getId()]) {
			this.attachedNodePositionData[node.getId()] = new Object();
			this.attachedNodePositionData[node.getId()]
					.relativDistanceFromDocker1 = 0;
			this.attachedNodePositionData[node.getId()].node = node;
			this.attachedNodePositionData[node.getId()].segment = new Object();
			this.findEdgeSegmentForNode(node);
		}else if(node.isChanged) {
			this.findEdgeSegmentForNode(node);
		}
		
		
		
	},
	
	/**
	 * Finds the appropriate edge segement for a node.
	 * The segment is choosen, which has the smallest distance to the node.
	 * 
	 * @param {ORYX.Core.Node} node
	 * 		The concerning node
	 */
	findEdgeSegmentForNode: function(node) {
		var length = this.dockers.length;
		var smallestDistance = undefined;
		
		for(i=1;i<length;i++) {
			var lineP1 = this.dockers[i-1].bounds.center();
			var lineP2 = this.dockers[i].bounds.center();
			this.relativizePoint(lineP1);
			this.relativizePoint(lineP2);
			
			var nodeCenterPoint = node.bounds.center();
			var distance = ORYX.Core.Math.distancePointLinie(
															lineP1,
															lineP2, 
															nodeCenterPoint, 
															true);
			
			if((distance || distance == 0) && ((!smallestDistance && smallestDistance != 0) 
						|| distance < smallestDistance)) {
				
				smallestDistance = distance;
				
				this.attachedNodePositionData[node.getId()].segment.docker1 = 
													this.dockers[i-1];
				this.attachedNodePositionData[node.getId()].segment.docker2 = 
													this.dockers[i];
	
			}
			
			/* Either the distance does not match the segment or the distance
			 * between docker1 and docker2 is 0
			 * 
			 * In this case choose the nearest docker as attaching point.
			 * 
			 */
			if(!distance && !smallestDistance && smallestDistance != 0) {
				(ORYX.Core.Math.getDistancePointToPoint(nodeCenterPoint, lineP1)
					< ORYX.Core.Math.getDistancePointToPoint(nodeCenterPoint, lineP2)) ?
					this.attachedNodePositionData[node.getId()].relativDistanceFromDocker1 = 0 :
					this.attachedNodePositionData[node.getId()].relativDistanceFromDocker1 = 1;
				this.attachedNodePositionData[node.getId()].segment.docker1 = 
													this.dockers[i-1];
				this.attachedNodePositionData[node.getId()].segment.docker2 = 
													this.dockers[i];
			}
		}
		
		/* Calculate position on edge segment for the node */
		if(smallestDistance || smallestDistance == 0) {
			this.attachedNodePositionData[node.getId()].relativDistanceFromDocker1 =
			this.getLineParameterForPosition(
					this.attachedNodePositionData[node.getId()].segment.docker1,
					this.attachedNodePositionData[node.getId()].segment.docker2,
					node);
		}
	},
	
	
	/**
	 *
	 * @param {ORYX.Core.Node|Object} node or position
	 * @return {Object} An object with the following attribute: {ORYX.Core.Docker} fromDocker, {ORYX.Core.Docker} toDocker, {X/Y} position, {int} distance
	 */
 	findSegment: function(node){
		
		var length = this.dockers.length;
		var result;
		
		var nodeCenterPoint = node instanceof ORYX.Core.UIObject ? node.bounds.center() : node;
			
		for (i = 1; i < length; i++) {
			var lineP1 = this.dockers[i - 1].bounds.center();
			var lineP2 = this.dockers[i].bounds.center();
			
			var distance = ORYX.Core.Math.distancePointLinie(lineP1, lineP2, nodeCenterPoint, true);
			
			if (typeof distance == "number" && (result === undefined || distance < result.distance)) {
				result = {
					distance: distance,
					fromDocker: this.dockers[i - 1],
					toDocker: this.dockers[i]
				}
				
			}
		}
		return result;
	},
	
	/**
	 * Returns the value of the scalar to determine the position of the node on 
	 * line defined by docker1 and docker2.
	 * 
	 * @param {point} docker1
	 * 		The docker that defines the start of the line segment
	 * @param {point} docker2
	 * 		The docker that defines the end of the line segment
	 * @param {ORYX.Core.Node} node
	 * 		The concerning node
	 * 
	 * @return {float} positionParameter
	 * 		The scalar value to determine the position on the line
	 */
	getLineParameterForPosition: function(docker1, docker2, node) {
		var dockerPoint1 = docker1.bounds.center();
		var dockerPoint2 = docker2.bounds.center();
		this.relativizePoint(dockerPoint1);
		this.relativizePoint(dockerPoint2);
		
		var intersectionPoint = ORYX.Core.Math.getPointOfIntersectionPointLine(
									dockerPoint1,
									dockerPoint2,
									node.bounds.center(), true);
		if(!intersectionPoint) {
			return 0;
		}
		
		var relativeDistance = 
			ORYX.Core.Math.getDistancePointToPoint(intersectionPoint, dockerPoint1) /
			ORYX.Core.Math.getDistancePointToPoint(dockerPoint1, dockerPoint2);
		
		return relativeDistance;
	},
	/**
	 * Makes point relative to the upper left of the edge's bound.
	 * 
	 * @param {point} point
	 * 		The point to relativize
	 */
	relativizePoint: function(point) {
		point.x -= this.bounds.upperLeft().x;
		point.y -= this.bounds.upperLeft().y;		
	},
	
	/**
	 * Move the first and last docker and calls the refresh method.
	 * Attention: This does not calculates intersection point between the
	 * edge and the bounded nodes. This only works if only the nodes are
	 * moves.
	 *
	 */
	optimizedUpdate: function(){
		
		var updateDocker = function(docker){
			if (!docker._dockedShape || !docker._dockedShapeBounds)
				return;
			var off = {
			    x: docker._dockedShape.bounds.a.x - docker._dockedShapeBounds.a.x,
			    y: docker._dockedShape.bounds.a.y - docker._dockedShapeBounds.a.y
			};
			docker.bounds.moveBy(off);
			docker._dockedShapeBounds.moveBy(off);
		}
		
		updateDocker(this.dockers.first());
		updateDocker(this.dockers.last());
		
		this.refresh();
	},
    
    refresh: function(){
        //call base class refresh method
        arguments.callee.$.refresh.apply(this, arguments);
        
        //TODO consider points for marker mids
        var lastPoint;
        this._paths.each((function(path, index){
            var dockers = this._dockersByPath[path.id];
            var c = undefined;
			var d = undefined;
            if (lastPoint) {
                d = "M" + lastPoint.x + " " + lastPoint.y;
            }
            else {
                c = dockers[0].bounds.center();
                lastPoint = c;
                
                d = "M" + c.x + " " + c.y;
            }
            
            for (var i = 1; i < dockers.length; i++) {
                // for each docker, draw a line to the center
                c = dockers[i].bounds.center();
                d = d + "L" + c.x + " " + c.y + " ";
                lastPoint = c;
            }
            
            path.setAttributeNS(null, "d", d);
            this._interactionPaths[index].setAttributeNS(null, "d", d);
            
        }).bind(this));
		
		
		/* move child shapes of an edge */
		if(this.getChildNodes().length > 0) {
	        var x = this.bounds.upperLeft().x;
	        var y = this.bounds.upperLeft().y;
	        
			this.node.firstChild.childNodes[1].setAttributeNS(null, "transform", "translate(" + x + ", " + y + ")");
		}
		
    },
    
    /**
     * Calculate the Border Intersection Point between two points
     * @param {PointA}
     * @param {PointB}
     */
    getIntersectionPoint: function(){
    
        var length = Math.floor(this.dockers.length / 2)
        
        return ORYX.Core.Math.midPoint(this.dockers[length - 1].bounds.center(), this.dockers[length].bounds.center())
    },
    
	/**
     * Returns TRUE if the bounds is over the edge
     * @param {Bounds}
     *
     */
    isBoundsIncluded: function(bounds){
		var dockers = this.dockers, size = dockers.length;
		return dockers.any(function(docker, i){
			if (i == size-1){ return false; }
			var a = docker.bounds.center();
			var b = dockers[i+1].bounds.center();
			
			return ORYX.Core.Math.isRectOverLine(a.x, a.y, b.x, b.y, bounds.a.x, bounds.a.y, bounds.b.x, bounds.b.y);
		});
	},
    
    /**
     * Calculate if the point is inside the Shape
     * @param {PointX}
     * @param {PointY} 
     */
    isPointIncluded: function(pointX, pointY){
    
        var isbetweenAB = this.absoluteBounds().isIncluded(pointX, pointY, 
												ORYX.CONFIG.OFFSET_EDGE_BOUNDS);
        
		var isPointIncluded = undefined;
		
        if (isbetweenAB && this.dockers.length > 0) {
		
			var i = 0;
			var point1, point2;
			
			
			do {
			
				point1 = this.dockers[i].bounds.center();
				point2 = this.dockers[++i].bounds.center();
				
				isPointIncluded = ORYX.Core.Math.isPointInLine(pointX, pointY, 
											point1.x, point1.y, 
											point2.x, point2.y, 
											ORYX.CONFIG.OFFSET_EDGE_BOUNDS);
				
			} while (!isPointIncluded && i < this.dockers.length - 1)
			
		}
		
		return isPointIncluded;

    },
  
    
    /**
     * Calculate if the point is over an special offset area
     * @param {Point}
     */
    isPointOverOffset: function(){
		return  false
	},
	
	/**
	 * Returns TRUE if the given node
	 * is a child node of the shapes node
	 * @param {Element} node
	 * @return {Boolean}
	 *
	 */
	containsNode: function(node){
		if (this._paths.include(node) || 
       		this._interactionPaths.include(node)){
			return true;		
		}	
		return false;
	},
	
	/**
	* Returns the angle of the line between two dockers
	* (0 - 359.99999999)
	*/
	_getAngle: function(docker1, docker2) {
		var p1 = docker1 instanceof ORYX.Core.Controls.Docker ? docker1.absoluteCenterXY() : docker1;
		var p2 = docker2 instanceof ORYX.Core.Controls.Docker ? docker2.absoluteCenterXY() : docker2;
		
		return ORYX.Core.Math.getAngle(p1, p2);
	},
	    
    alignDockers: function(){
        this._update(true);
        
        var firstPoint = this.dockers.first().bounds.center();
        var lastPoint = this.dockers.last().bounds.center();
        
        var deltaX = lastPoint.x - firstPoint.x;
        var deltaY = lastPoint.y - firstPoint.y;
        
        var numOfDockers = this.dockers.length - 1;
        
        this.dockers.each((function(docker, index){
            var part = index / numOfDockers;
            docker.bounds.unregisterCallback(this._dockerChangedCallback);
            docker.bounds.moveTo(firstPoint.x + part * deltaX, firstPoint.y + part * deltaY);
            docker.bounds.registerCallback(this._dockerChangedCallback);
        }).bind(this));
        
        this._dockerChanged();
    },
    
	add: function(shape){
        arguments.callee.$.add.apply(this, arguments);
		
		// If the new shape is a Docker which is not contained
		if (shape instanceof ORYX.Core.Controls.Docker && this.dockers.include(shape)){
			// Add it to the dockers list ordered by paths		
			var pathArray = this._dockersByPath.values()[0];
			if (pathArray) {
				pathArray.splice(this.dockers.indexOf(shape), 0, shape);
			}
			
			/* Perform nessary adjustments on the edge's child shapes */
			this.handleChildShapesAfterAddDocker(shape);
		}
	},
	
	/**
	 * Performs nessary adjustments on the edge's child shapes.
	 * 
	 * @param {ORYX.Core.Controls.Docker} docker
	 * 		The added docker
	 */
	handleChildShapesAfterAddDocker: function(docker) {
		/* Ensure type of Docker */
		if(!docker instanceof ORYX.Core.Controls.Docker) {return undefined;}
		
		var index = this.dockers.indexOf(docker);
		if(!(0 < index && index < this.dockers.length - 1)) {
		/* Exception: Expect added docker between first and last node of the edge */
			return undefined;
		} 
			
		/* Get child nodes concerning the segment of the new docker */
		var startDocker = this.dockers[index-1];
		var endDocker = this.dockers[index+1];
		
		/* Adjust the position of edge's child nodes */
		var segmentElements = 
			this.getAttachedNodePositionDataForSegment(startDocker, endDocker);
		
		var lengthSegmentPart1 = ORYX.Core.Math.getDistancePointToPoint(
										startDocker.bounds.center(),
										docker.bounds.center());
		var lengthSegmentPart2 = ORYX.Core.Math.getDistancePointToPoint(
										endDocker.bounds.center(),
										docker.bounds.center());
										
		if(!(lengthSegmentPart1 + lengthSegmentPart2)) {return;}
		
		var relativDockerPosition = lengthSegmentPart1 / (lengthSegmentPart1 + lengthSegmentPart2);
			
		segmentElements.each(function(nodePositionData) {
			/* Assign child node to the new segment */
			if(nodePositionData.value.relativDistanceFromDocker1 < relativDockerPosition) {
				/* Case: before added Docker */
				nodePositionData.value.segment.docker2 = docker;
				nodePositionData.value.relativDistanceFromDocker1 = 
					nodePositionData.value.relativDistanceFromDocker1 / relativDockerPosition;
			} else {
				/* Case: after added Docker */
				nodePositionData.value.segment.docker1 = docker;
				var newFullDistance = 1 - relativDockerPosition;
				var relativPartOfSegment = 
							nodePositionData.value.relativDistanceFromDocker1
							- relativDockerPosition;
				
				nodePositionData.value.relativDistanceFromDocker1 = 
										relativPartOfSegment / newFullDistance;
				
			}
		})
		
		
		// Update all labels reference points
		this.getLabels().each(function(label){

			var ref = label.getReferencePoint();
			if (!ref) {
				return;
			}
			var index = this.dockers.indexOf(docker);
			if (index >= ref.segment.fromIndex && index <= ref.segment.toIndex){
				
				var segment = this.findSegment(ref.intersection);
				if (!segment){ 
					// Choose whether the first of the last segment
					segment.fromDocker = ref.segment.fromIndex >= (this.dockers.length/2) ? this.dockers[0] : this.dockers[this.dockers.length-2]; 
					segment.toDocker = this.dockers[this.dockers.indexOf(from)+1]; // The next one if the to docker
				}
				
				var fromPosition = segment.fromDocker.bounds.center(), toPosition = segment.toDocker.bounds.center();
			
				var intersection = ORYX.Core.Math.getPointOfIntersectionPointLine(
										fromPosition, 		// P1 - Center of the first docker
										toPosition, 		// P2 - Center of the second docker
										ref.intersection, 	// P3 - Center of the label
										true);
				//var oldDistance = ORYX.Core.Math.getDistanceBetweenTwoPoints(ref.segment.fromPosition, ref.segment.toPosition, ref.intersection);
				//intersection = ORYX.Core.Math.getPointBetweenTwoPoints(fromPosition, toPosition, isNaN(oldDistance) ? 0.5 : (lengthOld*oldDistance)/lengthNew);
					
				// Update the reference point
				this.updateReferencePointOfLabel(label, intersection, segment.fromDocker, segment.toDocker, true);
			}
		}.bind(this));
		
		/* Update attached nodes visual representation */
		this.refreshAttachedNodes();
	},
	
	/**
	 *	Returns elements from {@link attachedNodePositiondata} that match the
	 *  segement defined by startDocker and endDocker.
	 *  
	 *  @param {ORYX.Core.Controls.Docker} startDocker
	 *  	The docker defining the begin of the segment.
	 *  @param {ORYX.Core.Controls.Docker} endDocker
	 *  	The docker defining the begin of the segment.
	 *  
	 *  @return {Hash} attachedNodePositionData
	 *  	Child elements matching the segment
	 */
	getAttachedNodePositionDataForSegment: function(startDocker, endDocker) {
		/* Ensure that the segment is defined correctly */
		if(!((startDocker instanceof ORYX.Core.Controls.Docker) 
			&& (endDocker instanceof ORYX.Core.Controls.Docker))) {
				return [];
			}
			
		/* Get elements of the segment */
		var elementsOfSegment = 
			this.attachedNodePositionData.findAll(function(nodePositionData) {
				return nodePositionData.value.segment.docker1 === startDocker &&
						nodePositionData.value.segment.docker2 === endDocker;
			});
		
		/* Return a Hash in each case */
		if(!elementsOfSegment) {return [];}
		
		return elementsOfSegment;
	},
	
	/**
	 * Removes an edge's child shape
	 */
	remove: function(shape) {
		arguments.callee.$.remove.apply(this, arguments);
		
		if(this.attachedNodePositionData[shape.getId()]) {
			delete this.attachedNodePositionData[shape.getId()];
		}
		
		/* Adjust child shapes if neccessary */
		if(shape instanceof ORYX.Core.Controls.Docker) {
			this.handleChildShapesAfterRemoveDocker(shape);
		}
	},
	
	updateReferencePointOfLabel: function(label, intersection, from, to, dirty){
		if (!label.getReferencePoint() || !label.isVisible) {
			return;
		}
		
		var ref = label.getReferencePoint();
		
		//
		if (ref.orientation && ref.orientation !== "ce"){
			var angle = this._getAngle(from, to);
			if (ref.distance >= 0){
				if(angle == 0){
					label.horizontalAlign("left");//ref.orientation == "lr" ? "right" : "left");
					label.verticalAlign("bottom");
				} else if (angle > 0 && angle < 90){
					label.horizontalAlign("right");
					label.verticalAlign("bottom");
				} else if (angle == 90){
					label.horizontalAlign("right");
					label.verticalAlign("top");//ref.orientation == "lr" ? "bottom" : "top");
				} else if (angle > 90 && angle < 180){
					label.horizontalAlign("right");
					label.verticalAlign("top");
				} else if (angle == 180){
					label.horizontalAlign("left");//ref.orientation == "ur" ? "right" : "left");
					label.verticalAlign("top");
				} else if (angle > 180 && angle < 270){
					label.horizontalAlign("left");
					label.verticalAlign("top");				
				} else if (angle == 270){
					label.horizontalAlign("left");
					label.verticalAlign("top");//ref.orientation == "ll" ? "bottom" : "top");
				} else if (angle > 270 && angle <= 360){
					label.horizontalAlign("left");
					label.verticalAlign("bottom");
				}
			} else {
				if(angle == 0){
					label.horizontalAlign("left");//ref.orientation == "ur" ? "right" : "left");
					label.verticalAlign("top");
				} else if (angle > 0 && angle < 90){
					label.horizontalAlign("left");
					label.verticalAlign("top");
				} else if (angle == 90){
					label.horizontalAlign("left");
					label.verticalAlign("top");//ref.orientation == "ll" ? "bottom" : "top");
				} else if (angle > 90 && angle < 180){
					label.horizontalAlign("left");
					label.verticalAlign("bottom");
				} else if (angle == 180){
					label.horizontalAlign("left");//ref.orientation == "lr" ? "right" : "left");
					label.verticalAlign("bottom");
				} else if (angle > 180 && angle < 270){
					label.horizontalAlign("right");
					label.verticalAlign("bottom");
				} else if (angle == 270){
					label.horizontalAlign("right");
					label.verticalAlign("top");//ref.orientation == "lr" ? "bottom" : "top");
				} else if (angle > 270 && angle <= 360){
					label.horizontalAlign("right");
					label.verticalAlign("top");
				}			
			}
			ref.iorientation = ref.iorientation || ref.orientation;
			ref.orientation = (label.verticalAlign()=="top"?"u":"l") + (label.horizontalAlign()=="left"?"l":"r");
		}
	
		label.setReferencePoint(jQuery.extend({},{
				intersection: intersection,
				segment: {
					from: from,
					fromIndex: this.dockers.indexOf(from),
					fromPosition: from.bounds.center(),
					to: to,
					toIndex: this.dockers.indexOf(to),
					toPosition: to.bounds.center()
				},
				dirty: dirty || false
			},ref))
	},
	/**
	 * 	Adjusts the child shapes of an edges after a docker was removed.
	 * 	
	 *  @param{ORYX.Core.Controls.Docker} docker
	 *  	The removed docker.
	 */
	handleChildShapesAfterRemoveDocker: function(docker) {
		/* Ensure docker type */
		if(!(docker instanceof ORYX.Core.Controls.Docker)) {return;}
		
		this.attachedNodePositionData.each(function(nodePositionData) {
			if(nodePositionData.value.segment.docker1 === docker) {
				/* The new start of the segment is the predecessor of docker2. */
				var index = this.dockers.indexOf(nodePositionData.value.segment.docker2);
				if(index == -1) {return;}
				nodePositionData.value.segment.docker1 = this.dockers[index - 1];
			} 
			else if(nodePositionData.value.segment.docker2 === docker) {
				/* The new end of the segment is the successor of docker1. */
				var index = this.dockers.indexOf(nodePositionData.value.segment.docker1);
				if(index == -1) {return;}
				nodePositionData.value.segment.docker2 = this.dockers[index + 1];
			}
		}.bind(this));
		
		// Update all labels reference points
		this.getLabels().each(function(label){

			var ref = label.getReferencePoint();
			if (!ref) {
				return;
			}
			var from = ref.segment.from;
			var to = ref.segment.to;
			
			if (from !== docker && to !== docker){ 
				return; 
			}
			
			var segment = this.findSegment(ref.intersection);
			if (!segment){ 
				from = segment.fromDocker;
				to = segment.toDocker;
			} else {
				from = from === docker ? this.dockers[this.dockers.indexOf(to)-1] : from;
				to = this.dockers[this.dockers.indexOf(from)+1];
			}

			var intersection = ORYX.Core.Math.getPointOfIntersectionPointLine(from.bounds.center(), to.bounds.center(), ref.intersection, true);			
			// Update the reference point
			this.updateReferencePointOfLabel(label, intersection, from, to, true);
		}.bind(this));
		
		/* Update attached nodes visual representation */
		this.refreshAttachedNodes();
	},
	
	/**
     *@deprecated Use the .createDocker() Method and set the point via the bounds
     */
    addDocker: function(position, exDocker){
        var lastDocker;
		var result;
        this._dockersByPath.any((function(pair){
            return pair.value.any((function(docker, index){
                if (!lastDocker) {
                    lastDocker = docker;
                    return false;
                }
                else {
                    var point1 = lastDocker.bounds.center();
                    var point2 = docker.bounds.center();
                    
                    var additionalIEZoom = 1;
                    if (!isNaN(screen.logicalXDPI) && !isNaN(screen.systemXDPI)) {
                        var ua = navigator.userAgent;
                        if (ua.indexOf('MSIE') >= 0) {
                            //IE 10 and below
                            var zoom = Math.round((screen.deviceXDPI / screen.logicalXDPI) * 100);
                            if (zoom !== 100) {
                                additionalIEZoom = zoom / 100
                            }
                        }
                    }
                    
                    if (additionalIEZoom !== 1) {
                        position.x = position.x / additionalIEZoom;
                        position.y = position.y / additionalIEZoom;
                    }
                    
                    if (ORYX.Core.Math.isPointInLine(position.x, position.y, point1.x, point1.y, point2.x, point2.y, 10)) {
                        var path = this._paths.find(function(path){
                            return path.id === pair.key;
                        });
                        if (path) {
                            var allowAttr = path.getAttributeNS(NAMESPACE_ORYX, 'allowDockers');
                            if (allowAttr && allowAttr.toLowerCase() === "no") {
                                return true;
                            }
                        }
                        
                        var newDocker = (exDocker) ? exDocker : this.createDocker(this.dockers.indexOf(lastDocker) + 1, position);
						newDocker.bounds.centerMoveTo(position);
                        if(exDocker)
							this.add(newDocker, this.dockers.indexOf(lastDocker) + 1);
						result = newDocker;
                        return true;
                    }
                    else {
                        lastDocker = docker;
                        return false;
                    }
                }
            }).bind(this));
        }).bind(this));
		return result;
    },
    
    removeDocker: function(docker){
        if (this.dockers.length > 2 && !(this.dockers.first() === docker)) {
            this._dockersByPath.any((function(pair){
                if (pair.value.member(docker)) {
                    if (docker === pair.value.last()) {
                        return true;
                    }
                    else {
                        this.remove(docker);
                        this._dockersByPath[pair.key] = pair.value.without(docker);
                        this.isChanged = true;
                        this._dockerChanged();
                        return true;
                    }
                }
                return false;
            }).bind(this));
        }
    },
	
	/**
	 * Removes all dockers from the edge which are on 
	 * the line between two dockers
	 * @return {Object} Removed dockers in an indicied array 
	 * (key is the removed position of the docker, value is docker themselve)
	 */
	removeUnusedDockers:function(){
		var marked = $H({});
		
		this.dockers.each(function(docker, i){
			if (i==0||i==this.dockers.length-1){ return }
			var previous = this.dockers[i-1];
			
			/* Do not consider already removed dockers */
			if(marked.values().indexOf(previous) != -1 && this.dockers[i-2]) {
				previous = this.dockers[i-2];
			}
			var next = this.dockers[i+1];
			
			var cp = previous.getDockedShape() && previous.referencePoint ? previous.getAbsoluteReferencePoint() : previous.bounds.center();
			var cn = next.getDockedShape() && next.referencePoint ? next.getAbsoluteReferencePoint() : next.bounds.center();
			var cd = docker.bounds.center();
			
			if (ORYX.Core.Math.isPointInLine(cd.x, cd.y, cp.x, cp.y, cn.x, cn.y, 1)){
				marked[i] = docker;
			}
		}.bind(this))
		
		marked.each(function(docker){
			this.removeDocker(docker.value);
		}.bind(this))
		
		if (marked.values().length > 0){
			this._update(true);
		}
		
		return marked;
	},
    
    /**
     * Initializes the Edge after loading the SVG representation of the edge.
     * @param {SVGDocument} svgDocument
     */
    _init: function(svgDocument){
        arguments.callee.$._init.apply(this, arguments);
        
        var minPointX, minPointY, maxPointX, maxPointY;
        
        //init markers
        var defs = svgDocument.getElementsByTagNameNS(NAMESPACE_SVG, "defs");
        if (defs.length > 0) {
            defs = defs[0];
            var markerElements = $A(defs.getElementsByTagNameNS(NAMESPACE_SVG, "marker"));
            var marker;
            var me = this;
            markerElements.each(function(markerElement){
                try {
                    marker = new ORYX.Core.SVG.SVGMarker(markerElement.cloneNode(true));
                    me._markers[marker.id] = marker;
                    var textElements = $A(marker.element.getElementsByTagNameNS(NAMESPACE_SVG, "text"));
                    var label;
                    textElements.each(function(textElement){
                        label = new ORYX.Core.SVG.Label({
                            textElement: textElement,
							shapeId: this.id
                        });
                        me._labels[label.id] = label;
                    });
                } 
                catch (e) {
                }
            });
        }
        
        
        var gs = svgDocument.getElementsByTagNameNS(NAMESPACE_SVG, "g");
        if (gs.length <= 0) {
            throw "Edge: No g element found.";
        }
        var g = gs[0];
        
        
        g.setAttributeNS(null, "id", null);
        
        var isFirst = true;
        
        $A(g.childNodes).each((function(path, index){
            if (ORYX.Editor.checkClassType(path, SVGPathElement)) {
                path = path.cloneNode(false);
                
                var pathId = this.id + "_" + index;
                path.setAttributeNS(null, "id", pathId);
                this._paths.push(path);
                
                //check, if markers are set and update the id
                var markersByThisPath = [];
                var markerUrl = path.getAttributeNS(null, "marker-start");
                
                if (markerUrl && markerUrl !== "") {
                    markerUrl = markerUrl.strip();
                    markerUrl = markerUrl.replace(/^url\(#/, '');
                    
                    var markerStartId = this.getValidMarkerId(markerUrl);
                    path.setAttributeNS(null, "marker-start", "url(#" + markerStartId + ")");
                    
                    markersByThisPath.push(this._markers[markerStartId]);
                }
                
                markerUrl = path.getAttributeNS(null, "marker-mid");
                
                if (markerUrl && markerUrl !== "") {
                    markerUrl = markerUrl.strip();
                    markerUrl = markerUrl.replace(/^url\(#/, '');
                    var markerMidId = this.getValidMarkerId(markerUrl);
                    path.setAttributeNS(null, "marker-mid", "url(#" + markerMidId + ")");
                    
                    markersByThisPath.push(this._markers[markerMidId]);
                }
                
                markerUrl = path.getAttributeNS(null, "marker-end");
                
                if (markerUrl && markerUrl !== "") {
                    markerUrl = markerUrl.strip();
                    
                    var markerEndId = this.getValidMarkerId(markerUrl);
                    path.setAttributeNS(null, "marker-end", "url(#" + markerEndId + ")");
                    
                    markersByThisPath.push(this._markers[markerEndId]);
                }
                
                this._markersByPath[pathId] = markersByThisPath;
                
                //init dockers
                var parser = new PathParser();
                var handler = new ORYX.Core.SVG.PointsPathHandler();
                parser.setHandler(handler);
                parser.parsePath(path);
                
                if (handler.points.length < 4) {
                    throw "Edge: Path has to have two or more points specified.";
                }
                
                this._dockersByPath[pathId] = [];
                
				for (var i = 0; i < handler.points.length; i += 2) {
					//handler.points.each((function(point, pIndex){
					var x = handler.points[i];
					var y = handler.points[i+1];
					if (isFirst || i > 0) {
						var docker = new ORYX.Core.Controls.Docker({
							eventHandlerCallback: this.eventHandlerCallback
						});
						docker.bounds.centerMoveTo(x,y);
						docker.bounds.registerCallback(this._dockerChangedCallback);
						this.add(docker, this.dockers.length);
						
						//this._dockersByPath[pathId].push(docker);
						
						//calculate minPoint and maxPoint
						if (minPointX) {
							minPointX = Math.min(x, minPointX);
							minPointY = Math.min(y, minPointY);
						}
						else {
							minPointX = x;
							minPointY = y;
						}
						
						if (maxPointX) {
							maxPointX = Math.max(x, maxPointX);
							maxPointY = Math.max(y, maxPointY);
						}
						else {
							maxPointX = x;
							maxPointY = y;
						}
					}
					//}).bind(this));
				}
                isFirst = false;
            }
        }).bind(this));
        
        this.bounds.set(minPointX, minPointY, maxPointX, maxPointY);
        
        if (false&&(this.bounds.width() === 0 || this.bounds.height() === 0)) {
			var width = this.bounds.width();
			var height = this.bounds.height();
			
            this.bounds.extend({
                x: width === 0 ? 2 : 0,
                y: height === 0 ? 2 : 0
            });
            
            this.bounds.moveBy({
                x: width === 0 ? -1 : 0,
                y: height === 0 ? -1 : 0
            });
            
        }
        
        this._oldBounds = this.bounds.clone();
        
        //add paths to this.node
        this._paths.reverse();
        var paths = [];
        this._paths.each((function(path){
            paths.push(this.node.childNodes[0].childNodes[0].childNodes[0].appendChild(path));
        }).bind(this));
        
        this._paths = paths;
        
        //init interaction path
        this._paths.each((function(path){
            var iPath = path.cloneNode(false);
			iPath.setAttributeNS(null, "id", undefined);
            iPath.setAttributeNS(null, "stroke-width", 10);
            iPath.setAttributeNS(null, "visibility", "hidden");
            iPath.setAttributeNS(null, "stroke-dasharray", null);
            iPath.setAttributeNS(null, "stroke", "black");
            iPath.setAttributeNS(null, "fill", "none");
			iPath.setAttributeNS(null, "title", this.getStencil().title());
            this._interactionPaths.push(this.node.childNodes[0].childNodes[0].childNodes[0].appendChild(iPath));
        }).bind(this));
        
        this._paths.reverse();
        this._interactionPaths.reverse();
		
		/**initialize labels*/
        var textElems = svgDocument.getElementsByTagNameNS(ORYX.CONFIG.NAMESPACE_SVG, 'text');
        
		$A(textElems).each((function(textElem){
            var label = new ORYX.Core.SVG.Label({
                textElement: textElem,
				shapeId: this.id
            });
            this.node.childNodes[0].childNodes[0].appendChild(label.node);
            this._labels[label.id] = label;
			
			label.registerOnChange(this.layout.bind(this));
        }).bind(this)); 
		
        
        this.propertiesChanged.each(function(pair){
            pair.value = true;
        });
        
        
        //if(this.dockers.length == 2) {
        	
        	
        	
      //  }
		
        //this._update(true);
    },
    
    getValidMarkerId: function(markerUrl) {
    	if(markerUrl.indexOf("url(\"#") >= 0) {
            // Fix for IE9, additional quotes are added to the <id
            var rawId = markerUrl.replace(/^url\(\"#/, "").replace(/\"\)$/, '');
            return this.id + rawId;
          } else {
            markerUrl = markerUrl.replace(/^url\(#/, '');
            return this.id.concat(markerUrl.replace(/\)$/, ''));
          }
    },
    
    /**
     * Adds all necessary markers of this Edge to the SVG document.
     * Has to be called, while this.node is part of DOM.
     */
    addMarkers: function(defs){
        this._markers.each(function(marker){
            if (!defs.ownerDocument.getElementById(marker.value.id)) {
                marker.value.element = defs.appendChild(marker.value.element);
            }
        });
    },
    
    /**
     * Removes all necessary markers of this Edge from the SVG document.
     * Has to be called, while this.node is part of DOM.
     */
    removeMarkers: function(){
        var svgElement = this.node.ownerSVGElement;
        if (svgElement) {
            var defs = svgElement.getElementsByTagNameNS(NAMESPACE_SVG, "defs");
            if (defs.length > 0) {
                defs = defs[0];
                this._markers.each(function(marker){
                    var foundMarker = defs.ownerDocument.getElementById(marker.value.id);
                    if (foundMarker) {
                        marker.value.element = defs.removeChild(marker.value.element);
                    }
                });
            }
        }
    },
    
    /**
     * Calls when a docker has changed
     */
    _dockerChanged: function(){
    
        //this._update(true);
		this._dockerUpdated = true;
        
    },
    
    serialize: function(){
        var result = arguments.callee.$.serialize.apply(this);
        
        //add dockers triple
        var value = "";
        this._dockersByPath.each((function(pair){
            pair.value.each(function(docker){
                var position = docker.getDockedShape() && docker.referencePoint ? docker.referencePoint : docker.bounds.center();
                value = value.concat(position.x + " " + position.y + " ");
            });
            
            value += " # ";
        }).bind(this));
        result.push({
            name: 'dockers',
            prefix: 'oryx',
            value: value,
            type: 'literal'
        });
        
        //add parent triple dependant on the dockedShapes
        //TODO change this when canvas becomes a resource
/*        var source = this.dockers.first().getDockedShape();
        var target = this.dockers.last().getDockedShape();
        var sharedParent;
        if (source && target) {
            //get shared parent
            while (source.parent) {
                source = source.parent;
                if (source instanceof ORYX.Core.Canvas) {
                    sharedParent = source;
                    break;
                }
                else {
                    var targetParent = target.parent;
                    var found;
                    while (targetParent) {
                        if (source === targetParent) {
                            sharedParent = source;
                            found = true;
                            break;
                        }
                        else {
                            targetParent = targetParent.parent;
                        }
                    }
                    if (found) {
                        break;
                    }
                }
            }
        }
        else 
            if (source) {
                sharedParent = source.parent;
            }
            else 
                if (target) {
                    sharedParent = target.parent;
                }
*/        
        //if (sharedParent) {
/*            result.push({
                name: 'parent',
                prefix: 'raziel',
                //value: '#' + ERDF.__stripHashes(sharedParent.resourceId),
                value: '#' + ERDF.__stripHashes(this.getCanvas().resourceId),
                type: 'resource'
            });*/
        //}
		
		//serialize target and source
		var lastDocker = this.dockers.last();
		
		var target = lastDocker.getDockedShape();
		
		if(target) {
			result.push({
				name: 'target',
				prefix: 'raziel',
				value: '#' + ERDF.__stripHashes(target.resourceId),
				type: 'resource'
			});
		}
        
        try {
            //result = this.getStencil().serialize(this, result);
			var serializeEvent = this.getStencil().serialize();
			
			/*
			 * call serialize callback by reference, result should be found
			 * in serializeEvent.result
			 */
			if(serializeEvent.type) {
				serializeEvent.shape = this;
				serializeEvent.data = result;
				serializeEvent.result = undefined;
				serializeEvent.forceExecution = true;
				
				this._delegateEvent(serializeEvent);
				
				if(serializeEvent.result) {
					result = serializeEvent.result;
				}
			}
        } 
        catch (e) {
        }
        return result;
    },
    
    deserialize: function(data){
        try {
            //data = this.getStencil().deserialize(this, data);
			
			var deserializeEvent = this.getStencil().deserialize();
			
			/*
			 * call serialize callback by reference, result should be found
			 * in serializeEventInfo.result
			 */
			if(deserializeEvent.type) {
				deserializeEvent.shape = this;
				deserializeEvent.data = data;
				deserializeEvent.result = undefined;
				deserializeEvent.forceExecution = true;
				
				this._delegateEvent(deserializeEvent);
				if(deserializeEvent.result) {
					data = deserializeEvent.result;
				}
			}
        } 
        catch (e) {
        }
        
		// Set the outgoing shapes
		var target = data.find(function(ser) {return (ser.prefix+"-"+ser.name) == 'raziel-target'});
		var targetShape;
		if(target) {
			targetShape = this.getCanvas().getChildShapeByResourceId(target.value);
		}
		
		var outgoing = data.findAll(function(ser){ return (ser.prefix+"-"+ser.name) == 'raziel-outgoing'});
		outgoing.each((function(obj){
			// TODO: Look at Canvas
			if(!this.parent) {return};
								
			// Set outgoing Shape
			var next = this.getCanvas().getChildShapeByResourceId(obj.value);
															
			if(next){
				if(next == targetShape) {
					// If this is an edge, set the last docker to the next shape
					this.dockers.last().setDockedShape(next);
					this.dockers.last().setReferencePoint({x: next.bounds.width() / 2.0, y: next.bounds.height() / 2.0});
				} else if(next instanceof ORYX.Core.Edge) {
					//Set the first docker of the next shape
					next.dockers.first().setDockedShape(this);
					//next.dockers.first().setReferencePoint({x: this.bounds.width() / 2.0, y: this.bounds.height() / 2.0});
				} /*else if(next.dockers.length > 0) { //next is a node and next has a docker
					next.dockers.first().setDockedShape(this);
					next.dockers.first().setReferencePoint({x: this.bounds.width() / 2.0, y: this.bounds.height() / 2.0});
				}*/
			}	
			
		}).bind(this));
		
        
        var oryxDockers = data.find(function(obj){
            return (obj.prefix === "oryx" &&
            obj.name === "dockers");
        });
		
        if (oryxDockers) {
            var dataByPath = oryxDockers.value.split("#").without("").without(" ");
            
            dataByPath.each((function(data, index){
                var values = data.replace(/,/g, " ").split(" ").without("");
                
                //for each docker two values must be defined
                if (values.length % 2 === 0) {
                    var path = this._paths[index];
                    
                    if (path) {
                        if (index === 0) {
                            while (this._dockersByPath[path.id].length > 2) {
                                this.removeDocker(this._dockersByPath[path.id][1]);
                            }
                        }
                        else {
                            while (this._dockersByPath[path.id].length > 1) {
                                this.removeDocker(this._dockersByPath[path.id][0]);
                            }
                        }
                        
                        var dockersByPath = this._dockersByPath[path.id];
                        
                        if (index === 0) {
                            //set position of first docker
                            var x = parseFloat(values.shift());
                            var y = parseFloat(values.shift());
                            
                            if (dockersByPath.first().getDockedShape()) {
                                dockersByPath.first().setReferencePoint({
                                    x: x,
                                    y: y
                                });
                            }
                            else {
                                dockersByPath.first().bounds.centerMoveTo(x, y);
                            }
                        }
                        
                        //set position of last docker
                        y = parseFloat(values.pop());
                        x = parseFloat(values.pop());
                        
                        if (dockersByPath.last().getDockedShape()) {
                            dockersByPath.last().setReferencePoint({
                                x: x,
                                y: y
                            });
                        } else {
                            dockersByPath.last().bounds.centerMoveTo(x, y);
                        }
                        
                        //add additional dockers
                        for (var i = 0; i < values.length; i++) {
                            x = parseFloat(values[i]);
                            y = parseFloat(values[++i]);
                            
                            var newDocker = this.createDocker();
                            newDocker.bounds.centerMoveTo(x, y);
                            
                            //this.dockers = this.dockers.without(newDocker);
                            //this.dockers.splice(this.dockers.indexOf(dockersByPath.last()), 0, newDocker);
                            //dockersByPath.splice(this.dockers.indexOf(dockersByPath.last()), 0, newDocker);
						}
                    }
                }
            }).bind(this));
        } else {
            this.alignDockers();
        }
		
        arguments.callee.$.deserialize.apply(this, arguments);
		
		this._changed();
    },
    
    toString: function(){
        return this.getStencil().title() + " " + this.id;
    },
    
    /**
     * @return {ORYX.Core.Shape} Returns last docked shape or null.
     */
    getTarget: function(){
        return this.dockers.last() ? this.dockers.last().getDockedShape() : null;
    },
	
	/**
	 * @return {ORYX.Core.Shape} Returns the first docked shape or null
	 */
	getSource: function() {
		return this.dockers.first() ? this.dockers.first().getDockedShape() : null;
	},
	
	/**
	 * Checks whether the edge is at least docked to one shape.
	 * 
	 * @return {boolean} True if edge is docked
	 */
	isDocked: function() {
		var isDocked = false;
		this.dockers.each(function(docker) {
			if(docker.isDocked()) {
				isDocked = true;
				throw $break;
			}
		});
		return isDocked;
	},
    
    /**
     * Calls {@link ORYX.Core.AbstractShape#toJSON} and add a some stencil set information.
     */
    toJSON: function() {
        var json = arguments.callee.$.toJSON.apply(this, arguments);
        
        if(this.getTarget()) {
            json.target = {
                resourceId: this.getTarget().resourceId
            };
        }
        
        return json;
    }
};
ORYX.Core.Edge = ORYX.Core.Shape.extend(ORYX.Core.Edge);
