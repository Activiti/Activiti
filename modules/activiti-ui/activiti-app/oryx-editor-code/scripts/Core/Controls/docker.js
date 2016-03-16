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

/**
 * Init namespaces
 */
if(!ORYX) {var ORYX = {};}
if(!ORYX.Core) {ORYX.Core = {};}
if(!ORYX.Core.Controls) {ORYX.Core.Controls = {};}


/**
 * @classDescription Represents a movable docker that can be bound to a shape. Dockers are used
 * for positioning shape objects.
 * @extends {Control}
 * 
 * TODO absoluteXY und absoluteCenterXY von einem Docker liefern falsche Werte!!!
 */
ORYX.Core.Controls.Docker = ORYX.Core.Controls.Control.extend({
	/**
	 * Constructor
	 */
	construct: function() {
		arguments.callee.$.construct.apply(this, arguments);
		
		this.isMovable = true;				// Enables movability
		this.bounds.set(0, 0, 16, 16);		// Set the bounds
		this.referencePoint = undefined;		// Refrenzpoint 
		this._dockedShapeBounds = undefined;		
		this._dockedShape = undefined;
		this._oldRefPoint1 = undefined;
		this._oldRefPoint2 = undefined;
		
		//this.anchors = [];
		this.anchorLeft;
		this.anchorRight;
		this.anchorTop;
		this.anchorBottom;

		this.node = ORYX.Editor.graft("http://www.w3.org/2000/svg",
			null,
			['g']);

		// The DockerNode reprasentation
		this._dockerNode = ORYX.Editor.graft("http://www.w3.org/2000/svg",
			this.node,
			['g', {"pointer-events":"all"},
					['circle', {cx:"8", cy:"8", r:"8", stroke:"none", fill:"none"}],
					['circle', {cx:"8", cy:"8", r:"3", stroke:"black", fill:"red", "stroke-width":"1"}]
				]);
			
		// The ReferenzNode reprasentation	
		this._referencePointNode = ORYX.Editor.graft("http://www.w3.org/2000/svg",
			this.node,	
			['g', {"pointer-events":"none"},
				['circle', {cx: this.bounds.upperLeft().x, cy: this.bounds.upperLeft().y, r: 3, fill:"red", "fill-opacity":0.4}]]);

		// Hide the Docker
		this.hide();
		
		//Add to the EventHandler
		this.addEventHandlers(this._dockerNode);

		// Buffer the Update Callback for un-/register on Event-Handler 
		this._updateCallback = this._changed.bind(this);
	},
	
	update: function() {
		// If there have an DockedShape	
		if(this._dockedShape) {
			if(this._dockedShapeBounds && this._dockedShape instanceof ORYX.Core.Node) {
				// Calc the delta of width and height of the lastBounds and the current Bounds
				var dswidth = this._dockedShapeBounds.width();
				var dsheight = this._dockedShapeBounds.height();
				if(!dswidth)
					dswidth = 1;
				if(!dsheight)
					dsheight = 1;	
				var widthDelta = 	this._dockedShape.bounds.width() 	/ dswidth;
				var heightDelta = 	this._dockedShape.bounds.height() 	/ dsheight;
				
				// If there is an different
				if(widthDelta !== 1.0 || heightDelta !== 1.0) {
					// Set the delta
					this.referencePoint.x *= widthDelta;
					this.referencePoint.y *= heightDelta;
				}
	
				// Clone these bounds
				this._dockedShapeBounds = this._dockedShape.bounds.clone();				
			}
			
			// Get the first and the last Docker of the parent Shape
			var dockerIndex = this.parent.dockers.indexOf(this)
			var dock1 = this;
			var dock2 = this.parent.dockers.length > 1 ? 
							(dockerIndex === 0?							// If there is the first element
							 	this.parent.dockers[dockerIndex + 1]:	// then take the next docker
								this.parent.dockers[dockerIndex - 1]):  // if not, then take the docker before
							undefined;
			
			// Calculate the first absolute Refenzpoint 
			var absoluteReferenzPoint1 = dock1.getDockedShape() ? 
				dock1.getAbsoluteReferencePoint() : 
				dock1.bounds.center();

			// Calculate the last absolute Refenzpoint 
			var absoluteReferenzPoint2 = dock2 && dock2.getDockedShape() ? 
				dock2.getAbsoluteReferencePoint() : 
				dock2 ? 
					dock2.bounds.center() :
					undefined;

			// If there is no last absolute Referenzpoint		
			if(!absoluteReferenzPoint2) {
				// Calculate from the middle of the DockedShape
				var center = this._dockedShape.absoluteCenterXY();
				var minDimension = this._dockedShape.bounds.width() * this._dockedShape.bounds.height(); 
				absoluteReferenzPoint2 = {
					x: absoluteReferenzPoint1.x + (center.x - absoluteReferenzPoint1.x) * -minDimension,
					y: absoluteReferenzPoint1.y + (center.y - absoluteReferenzPoint1.y) * -minDimension
				}
			}
			
			var newPoint = undefined;
			
			/*if (!this._oldRefPoint1 || !this._oldRefPoint2 ||
				absoluteReferenzPoint1.x !== this._oldRefPoint1.x ||
				absoluteReferenzPoint1.y !== this._oldRefPoint1.y ||
				absoluteReferenzPoint2.x !== this._oldRefPoint2.x ||
				absoluteReferenzPoint2.y !== this._oldRefPoint2.y) {*/
				
				// Get the new point for the Docker, calucalted by the intersection point of the Shape and the two points
				newPoint = this._dockedShape.getIntersectionPoint(absoluteReferenzPoint1, absoluteReferenzPoint2);
				
				// If there is new point, take the referencepoint as the new point
				if(!newPoint) {
					newPoint = this.getAbsoluteReferencePoint();
				}
				
				if(this.parent && this.parent.parent) {
					var grandParentPos = this.parent.parent.absoluteXY();
					newPoint.x -= grandParentPos.x;
					newPoint.y -= grandParentPos.y;
				}
				
				// Set the bounds to the new point
				this.bounds.centerMoveTo(newPoint)
			
				this._oldRefPoint1 = absoluteReferenzPoint1;
				this._oldRefPoint2 = absoluteReferenzPoint2;
			} 
			/*else {
				newPoint = this.bounds.center();
			}*/
			
			
	//	}
		
		// Call the super class
		arguments.callee.$.update.apply(this, arguments);
	},

	/**
	 * Calls the super class refresh method and updates the view of the docker.
	 */
	refresh: function() {
		arguments.callee.$.refresh.apply(this, arguments);
		
		// Refresh the dockers node
		var p = this.bounds.upperLeft();
		this._dockerNode.setAttributeNS(null, 'transform','translate(' + p.x + ', ' + p.y + ')');
		
		// Refresh the referencepoints node
		p = Object.clone(this.referencePoint);
		
		if(p && this._dockedShape){
			var upL 
			if(this.parent instanceof ORYX.Core.Edge) {
				upL = this._dockedShape.absoluteXY();
			} else {
				upL = this._dockedShape.bounds.upperLeft();
			}
			p.x += upL.x;
			p.y += upL.y;
		} else {
			p = this.bounds.center();
		}			

		this._referencePointNode.setAttributeNS(null, 'transform','translate(' + p.x + ', ' + p.y + ')');
	},

	/**
	 * Set the reference point
	 * @param {Object} point
	 */	
	setReferencePoint: function(point) {
		// Set the referencepoint
		if(this.referencePoint !== point &&
			(!this.referencePoint || 
			!point ||
			this.referencePoint.x !== point.x || 
			this.referencePoint.y !== point.y)) {
				
			this.referencePoint = point;
			this._changed();			
		}

		
		// Update directly, because the referencepoint has no influence of the bounds
		//this.refresh();
	},
	
	/**
	 * Get the absolute referencepoint
	 */
	getAbsoluteReferencePoint: function() {
		if(!this.referencePoint || !this._dockedShape) {
			return undefined;
		} else {
			var absUL = this._dockedShape.absoluteXY();
			return {	
						x: this.referencePoint.x + absUL.x,
						y: this.referencePoint.y + absUL.y
					}
		}
	},	
	
	/**
	 * Set the docked Shape from the docker
	 * @param {Object} shape
	 */
	setDockedShape: function(shape) {

		// If there is an old docked Shape
		if(this._dockedShape) {
			this._dockedShape.bounds.unregisterCallback(this._updateCallback)
			
			// Delete the Shapes from the incoming and outgoing array
			// If this Docker the incoming of the Shape
			if(this === this.parent.dockers.first()) {
				
				this.parent.incoming = this.parent.incoming.without(this._dockedShape);
				this._dockedShape.outgoing = this._dockedShape.outgoing.without(this.parent);
			
			// If this Docker the outgoing of the Shape	
			} else if (this === this.parent.dockers.last()){
	
				this.parent.outgoing = this.parent.outgoing.without(this._dockedShape);
				this._dockedShape.incoming = this._dockedShape.incoming.without(this.parent);
							
			}
			
		}

		
		// Set the new Shape
		this._dockedShape = shape;
		this._dockedShapeBounds = undefined;
		var referencePoint = undefined;
		
		// If there is an Shape, register the updateCallback if there are changes in the shape bounds
		if(this._dockedShape) {
			
			// Add the Shapes to the incoming and outgoing array
			// If this Docker the incoming of the Shape
			if(this === this.parent.dockers.first()) {
				
				this.parent.incoming.push(shape);
				shape.outgoing.push(this.parent);
			
			// If this Docker the outgoing of the Shape	
			} else if (this === this.parent.dockers.last()){
	
				this.parent.outgoing.push(shape);
				shape.incoming.push(this.parent);
							
			}
			
			// Get the bounds and set the new referencepoint
			var bounds = this.bounds;
			var absUL = shape.absoluteXY();
			
			/*if(shape.parent){
				var b = shape.parent.bounds.upperLeft();
				absUL.x -= b.x;
				absUL.y -= b.y;
			}*/
			
			referencePoint = {
				x: bounds.center().x - absUL.x,
				y: bounds.center().y - absUL.y
			}	
						
			this._dockedShapeBounds = this._dockedShape.bounds.clone();
			
			this._dockedShape.bounds.registerCallback(this._updateCallback);
			
			// Set the color of the docker as docked
			this.setDockerColor(ORYX.CONFIG.DOCKER_DOCKED_COLOR);				
		} else {
			// Set the color of the docker as undocked
			this.setDockerColor(ORYX.CONFIG.DOCKER_UNDOCKED_COLOR);
		}

		// Set the referencepoint
		this.setReferencePoint(referencePoint);
		this._changed();
		//this.update();
	},
	
	/**
	 * Get the docked Shape
	 */
	getDockedShape: function() {
		return this._dockedShape;
	},

	/**
	 * Returns TRUE if the docker has a docked shape
	 */
	isDocked: function() {
		return !!this._dockedShape;
	},
		
	/**
	 * Set the Color of the Docker
	 * @param {Object} color
	 */
	setDockerColor: function(color) {
		this._dockerNode.lastChild.setAttributeNS(null, "fill", color);
	},
	
	preventHiding: function(prevent){
		this._preventHiding = Math.max(0, (this._preventHiding||0) + (prevent ? 1 : -1));
	},
	
	/**
	 * Hides this UIObject and all its children.
	 */
	hide: function() {
		if (this._preventHiding){
			return false;
		}
		
		// Hide docker and reference point
		this.node.setAttributeNS(null, 'visibility', 'hidden');
		this._referencePointNode.setAttributeNS(null, 'visibility', 'hidden');
		
		this.children.each(function(uiObj) {
			uiObj.hide();	
		});				
	},
	
	/**
	 * Enables visibility of this UIObject and all its children.
	 */
	show: function() {
		// Show docker
		this.node.setAttributeNS(null, 'visibility', 'visible');
		
		// Hide reference point if the connected shape is an edge
		if (this.getDockedShape() instanceof ORYX.Core.Edge){
			this._referencePointNode.setAttributeNS(null, 'visibility', 'hidden');
		} else {
			this._referencePointNode.setAttributeNS(null, 'visibility', 'visible');
		}
		
		this.children.each(function(uiObj) {
			uiObj.show();	
		});		
	},
	
	toString: function() { return "Docker " + this.id }
});