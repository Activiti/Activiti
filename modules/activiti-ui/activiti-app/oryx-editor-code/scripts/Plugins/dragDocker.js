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

if(!ORYX.Plugins)
	ORYX.Plugins = new Object();

ORYX.Plugins.DragDocker = Clazz.extend({

	/**
	 *	Constructor
	 *	@param {Object} Facade: The Facade of the Editor
	 */
	construct: function(facade) {
		this.facade = facade;
		
		// Set the valid and invalid color
		this.VALIDCOLOR 	= ORYX.CONFIG.SELECTION_VALID_COLOR;
		this.INVALIDCOLOR 	= ORYX.CONFIG.SELECTION_INVALID_COLOR;
		
		// Define Variables 
		this.shapeSelection = undefined;
		this.docker 		= undefined;
		this.dockerParent   = undefined;
		this.dockerSource 	= undefined;
		this.dockerTarget 	= undefined;
		this.lastUIObj 		= undefined;
		this.isStartDocker 	= undefined;
		this.isEndDocker 	= undefined;
		this.undockTreshold	= 10;
		this.initialDockerPosition = undefined;
		this.outerDockerNotMoved = undefined;
		this.isValid 		= false;
		
		// For the Drag and Drop
		// Register on MouseDown-Event on a Docker
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_MOUSEDOWN, this.handleMouseDown.bind(this));
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_DOCKERDRAG, this.handleDockerDrag.bind(this));

		
		// Register on over/out to show / hide a docker
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_MOUSEOVER, this.handleMouseOver.bind(this));
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_MOUSEOUT, this.handleMouseOut.bind(this));		
		
		
	},
	
	/**
	 * MouseOut Handler
	 *
	 */
	handleMouseOut: function(event, uiObj) {
		// If there is a Docker, hide this
		if(!this.docker && uiObj instanceof ORYX.Core.Controls.Docker) {
			uiObj.hide();
		} else if(!this.docker && uiObj instanceof ORYX.Core.Edge) {
			uiObj.dockers.each(function(docker){
				docker.hide();
			});
		}
	},

	/**
	 * MouseOver Handler
	 *
	 */
	handleMouseOver: function(event, uiObj) {
		// If there is a Docker, show this		
		if(!this.docker && uiObj instanceof ORYX.Core.Controls.Docker) {
			uiObj.show();
		} else if(!this.docker && uiObj instanceof ORYX.Core.Edge) {
			uiObj.dockers.each(function(docker){
				docker.show();
			});
		}
	},
	/**
	 * DockerDrag Handler
	 * delegates the uiEvent of the drag event to the mouseDown function
	 */
	handleDockerDrag: function(event, uiObj) {
		this.handleMouseDown(event.uiEvent, uiObj);
	},
	
	/**
	 * MouseDown Handler
	 *
	 */	
	handleMouseDown: function(event, uiObj) {
		// If there is a Docker
		if(uiObj instanceof ORYX.Core.Controls.Docker && uiObj.isMovable) {
			
			/* Buffering shape selection and clear selection*/
			this.shapeSelection = this.facade.getSelection();
			this.facade.setSelection();
			
			this.docker = uiObj;
			this.initialDockerPosition = this.docker.bounds.center();
			this.outerDockerNotMoved = false;			
			this.dockerParent = uiObj.parent;
			
			// Define command arguments
			this._commandArg = {docker:uiObj, dockedShape:uiObj.getDockedShape(), refPoint:uiObj.referencePoint || uiObj.bounds.center()};

			// Show the Docker
			this.docker.show();
			
			// If the Dockers Parent is an Edge, 
			//  and the Docker is either the first or last Docker of the Edge
			if(uiObj.parent instanceof ORYX.Core.Edge && 
			   	(uiObj.parent.dockers.first() == uiObj || uiObj.parent.dockers.last() == uiObj)) {
				
				// Get the Edge Source or Target
				if(uiObj.parent.dockers.first() == uiObj && uiObj.parent.dockers.last().getDockedShape()) {
					this.dockerTarget = uiObj.parent.dockers.last().getDockedShape();
				} else if(uiObj.parent.dockers.last() == uiObj && uiObj.parent.dockers.first().getDockedShape()) {
					this.dockerSource = uiObj.parent.dockers.first().getDockedShape();
				}
				
			} else {
				// If there parent is not an Edge, undefined the Source and Target
				this.dockerSource = undefined;
				this.dockerTarget = undefined;				
			}
		
			this.isStartDocker = this.docker.parent.dockers.first() === this.docker;
			this.isEndDocker = this.docker.parent.dockers.last() === this.docker;
					
			// add to canvas while dragging
			this.facade.getCanvas().add(this.docker.parent);
			
			// Hide all Labels from Docker
			this.docker.parent.getLabels().each(function(label) {
				label.hide();
			});
			
			var eventCoordinates = this.facade.eventCoordinates(event);
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
                eventCoordinates.x = eventCoordinates.x / additionalIEZoom;
                eventCoordinates.y = eventCoordinates.y / additionalIEZoom;
            }
			
			// Undocked the Docker from current Shape
			if ((!this.isStartDocker && !this.isEndDocker) || !this.docker.isDocked()) {
				
				this.docker.setDockedShape(undefined);
				// Set the Docker to the center of the mouse pointer
				this.docker.bounds.centerMoveTo(eventCoordinates);
				this.dockerParent._update();
			} else {
				this.outerDockerNotMoved = true;
			}
			
			var option = {movedCallback: this.dockerMoved.bind(this), upCallback: this.dockerMovedFinished.bind(this)};
			
			this.startEventPos = eventCoordinates;
			
			// Enable the Docker for Drag'n'Drop, give the mouseMove and mouseUp-Callback with
			ORYX.Core.UIEnableDrag(event, uiObj, option);
		}
	},
	
	/**
	 * Docker MouseMove Handler
	 *
	 */
	dockerMoved: function(event) {
		this.outerDockerNotMoved = false;
		var snapToMagnet = undefined;
		
		if (this.docker.parent) {
			if (this.isStartDocker || this.isEndDocker) {
			
				// Get the EventPosition and all Shapes on these point
				var evPos = this.facade.eventCoordinates(event);
				
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
	            	evPos.x = evPos.x / additionalIEZoom;
	            	evPos.y = evPos.y / additionalIEZoom;
	            }
				
				if(this.docker.isDocked()) {
					/* Only consider start/end dockers if they are moved over a treshold */
					var distanceDockerPointer = 
						ORYX.Core.Math.getDistancePointToPoint(evPos, this.initialDockerPosition);
					if(distanceDockerPointer < this.undockTreshold) {
						this.outerDockerNotMoved = true;
						return;
					}
					
					/* Undock the docker */
					this.docker.setDockedShape(undefined);
					// Set the Docker to the center of the mouse pointer
					//this.docker.bounds.centerMoveTo(evPos);
					this.dockerParent._update();
				}
				
				var shapes = this.facade.getCanvas().getAbstractShapesAtPosition(evPos);
				
				// Get the top level Shape on these, but not the same as Dockers parent
				var uiObj = shapes.pop();
				if (this.docker.parent === uiObj) {
					uiObj = shapes.pop();
				}
				
				// If the top level Shape the same as the last Shape, then return
				if (this.lastUIObj == uiObj) {
				//return;
				
				// If the top level uiObj instance of Shape and this isn't the parent of the docker 
				}
				else 
					if (uiObj instanceof ORYX.Core.Shape) {
						
						// Ask by the StencilSet if the source, the edge and the target valid connections.
						if (this.docker.parent instanceof ORYX.Core.Edge) {
							
							var highestParent = this.getHighestParentBeforeCanvas(uiObj);
							/* Ensure that the shape to dock is not a child shape 
							 * of the same edge.
							 */
							if (highestParent instanceof ORYX.Core.Edge && this.docker.parent === highestParent) 
							{
								this.isValid = false;
								this.dockerParent._update();
								return;
							}
							this.isValid = false;
							var curObj = uiObj, orgObj = uiObj;
							while (!this.isValid && curObj && !(curObj instanceof ORYX.Core.Canvas))
							{
								uiObj = curObj;
								this.isValid = this.facade.getRules().canConnect({
											sourceShape: this.dockerSource ? // Is there a docked source 
															this.dockerSource : // than set this
															(this.isStartDocker ? // if not and if the Docker is the start docker
																uiObj : // take the last uiObj
																undefined), // if not set it to undefined;
											edgeShape: this.docker.parent,
											targetShape: this.dockerTarget ? // Is there a docked target 
											this.dockerTarget : // than set this
														(this.isEndDocker ? // if not and if the Docker is not the start docker
															uiObj : // take the last uiObj
															undefined) // if not set it to undefined;
										});
								curObj = curObj.parent;
							}
							
							// Reset uiObj if no 
							// valid parent is found
							if (!this.isValid){
								uiObj = orgObj;
							}

						}
						else {
							this.isValid = this.facade.getRules().canConnect({
								sourceShape: uiObj,
								edgeShape: this.docker.parent,
								targetShape: this.docker.parent
							});
						}
						
						// If there is a lastUIObj, hide the magnets
						if (this.lastUIObj) {
							this.hideMagnets(this.lastUIObj);
						}
						
						// If there is a valid connection, show the magnets
						if (this.isValid) {
							this.showMagnets(uiObj);
						}
						
						// Set the Highlight Rectangle by these value
						this.showHighlight(uiObj, this.isValid ? this.VALIDCOLOR : this.INVALIDCOLOR);
						
						// Buffer the current Shape
						this.lastUIObj = uiObj;
					}
					else {
						// If there is no top level Shape, then hide the highligting of the last Shape
						this.hideHighlight();
						this.lastUIObj ? this.hideMagnets(this.lastUIObj) : null;
						this.lastUIObj = undefined;
						this.isValid = false;
					}
				
				// Snap to the nearest Magnet
				if (this.lastUIObj && this.isValid && !(event.shiftKey || event.ctrlKey)) {
					snapToMagnet = this.lastUIObj.magnets.find(function(magnet){
						return magnet.absoluteBounds().isIncluded(evPos);
					});
					
					if (snapToMagnet) {
						this.docker.bounds.centerMoveTo(snapToMagnet.absoluteCenterXY());
					//this.docker.update()
					}
				}
			}
		}
		// Snap to on the nearest Docker of the same parent
		if(!(event.shiftKey || event.ctrlKey) && !snapToMagnet) {
			var minOffset = ORYX.CONFIG.DOCKER_SNAP_OFFSET;
			var nearestX = minOffset + 1;
			var nearestY = minOffset + 1;
			
			var dockerCenter = this.docker.bounds.center();
			
			if (this.docker.parent) {
				
				this.docker.parent.dockers.each((function(docker){
					if (this.docker == docker) {
						return
					};
					
					var center = docker.referencePoint ? docker.getAbsoluteReferencePoint() : docker.bounds.center();
					
					nearestX = Math.abs(nearestX) > Math.abs(center.x - dockerCenter.x) ? center.x - dockerCenter.x : nearestX;
					nearestY = Math.abs(nearestY) > Math.abs(center.y - dockerCenter.y) ? center.y - dockerCenter.y : nearestY;
					
					
				}).bind(this));
				
				if (Math.abs(nearestX) < minOffset || Math.abs(nearestY) < minOffset) {
					nearestX = Math.abs(nearestX) < minOffset ? nearestX : 0;
					nearestY = Math.abs(nearestY) < minOffset ? nearestY : 0;
					
					this.docker.bounds.centerMoveTo(dockerCenter.x + nearestX, dockerCenter.y + nearestY);
					//this.docker.update()
				} else {
					
					
					
					var previous = this.docker.parent.dockers[Math.max(this.docker.parent.dockers.indexOf(this.docker)-1, 0)];
					var next = this.docker.parent.dockers[Math.min(this.docker.parent.dockers.indexOf(this.docker)+1, this.docker.parent.dockers.length-1)];
					
					if (previous && next && previous !== this.docker && next !== this.docker){
						var cp = previous.bounds.center();
						var cn = next.bounds.center();
						var cd = this.docker.bounds.center();
						
						// Checks if the point is on the line between previous and next
						if (ORYX.Core.Math.isPointInLine(cd.x, cd.y, cp.x, cp.y, cn.x, cn.y, 10)) {
							// Get the rise
							var raise = (Number(cn.y)-Number(cp.y))/(Number(cn.x)-Number(cp.x));
							// Calculate the intersection point
							var intersecX = ((cp.y-(cp.x*raise))-(cd.y-(cd.x*(-Math.pow(raise,-1)))))/((-Math.pow(raise,-1))-raise);
							var intersecY = (cp.y-(cp.x*raise))+(raise*intersecX);
							
							if(isNaN(intersecX) || isNaN(intersecY)) {return;}
							
							this.docker.bounds.centerMoveTo(intersecX, intersecY);
						}
					}
					
				}
			}
		}
		//this.facade.getCanvas().update();
		this.dockerParent._update();
	},

	/**
	 * Docker MouseUp Handler
	 *
	 */
	dockerMovedFinished: function(event) {
		
		/* Reset to buffered shape selection */
		this.facade.setSelection(this.shapeSelection);
		
		// Hide the border
		this.hideHighlight();
		
		// Show all Labels from Docker
		this.dockerParent.getLabels().each(function(label){
			label.show();
			//label.update();
		});
	
		// If there is a last top level Shape
		if(this.lastUIObj && (this.isStartDocker || this.isEndDocker)){				
			// If there is a valid connection, the set as a docked Shape to them
			if(this.isValid) {
				
				this.docker.setDockedShape(this.lastUIObj);	
				
				this.facade.raiseEvent({
					type 	:ORYX.CONFIG.EVENT_DRAGDOCKER_DOCKED, 
					docker	: this.docker,
					parent	: this.docker.parent,
					target	: this.lastUIObj
				});
			}
			
			this.hideMagnets(this.lastUIObj);
		}
		
		// Hide the Docker
		this.docker.hide();
		
		if(this.outerDockerNotMoved) {
			// Get the EventPosition and all Shapes on these point
			var evPos = this.facade.eventCoordinates(event);
			var shapes = this.facade.getCanvas().getAbstractShapesAtPosition(evPos);
			
			/* Remove edges from selection */
			var shapeWithoutEdges = shapes.findAll(function(node) {
				return node instanceof ORYX.Core.Node;
			});
			shapes = shapeWithoutEdges.length ? shapeWithoutEdges : shapes;
			this.facade.setSelection(shapes);
		} else {
			//Command-Pattern for dragging one docker
			var dragDockerCommand = ORYX.Core.Command.extend({
				construct: function(docker, newPos, oldPos, newDockedShape, oldDockedShape, facade){
					this.docker 		= docker;
					this.index			= docker.parent.dockers.indexOf(docker);
					this.newPosition	= newPos;
					this.newDockedShape = newDockedShape;
					this.oldPosition	= oldPos;
					this.oldDockedShape	= oldDockedShape;
					this.facade			= facade;
					this.index			= docker.parent.dockers.indexOf(docker);
					this.shape			= docker.parent;
					
				},			
				execute: function(){
					if (!this.docker.parent){
						this.docker = this.shape.dockers[this.index];
					}
					this.dock( this.newDockedShape, this.newPosition );
					this.removedDockers = this.shape.removeUnusedDockers();
					this.facade.updateSelection();
				},
				rollback: function(){
					this.dock( this.oldDockedShape, this.oldPosition );
					(this.removedDockers||$H({})).each(function(d){
						this.shape.add(d.value, Number(d.key));
						this.shape._update(true);
					}.bind(this));
					this.facade.updateSelection();
				},
				dock:function( toDockShape, pos ){			
					// Set the Docker to the new Shape
					this.docker.setDockedShape( undefined );
					if( toDockShape ){			
						this.docker.setDockedShape( toDockShape );	
						this.docker.setReferencePoint( pos );
						//this.docker.update();	
						//this.docker.parent._update();				
					} else {
						this.docker.bounds.centerMoveTo( pos );
					}
	
					this.facade.getCanvas().update();
				}
			});
			
			
			if (this.docker.parent){
				// Instanziate the dockCommand
				var command = new dragDockerCommand(this.docker, this.docker.getDockedShape() ? this.docker.referencePoint : this.docker.bounds.center(), this._commandArg.refPoint, this.docker.getDockedShape(), this._commandArg.dockedShape, this.facade);
				this.facade.executeCommands( [command] );	
			}
		}
	
		// Update all Shapes
		//this.facade.updateSelection();
			
		// Undefined all variables
		this.docker 		= undefined;
		this.dockerParent   = undefined;
		this.dockerSource 	= undefined;
		this.dockerTarget 	= undefined;	
		this.lastUIObj 		= undefined;		
	},
	
	/**
	 * Hide the highlighting
	 */
	hideHighlight: function() {
		this.facade.raiseEvent({type:ORYX.CONFIG.EVENT_HIGHLIGHT_HIDE, highlightId:'validDockedShape'});
	},

	/**
	 * Show the highlighting
	 *
	 */
	showHighlight: function(uiObj, color) {
		
		this.facade.raiseEvent({
										type:		ORYX.CONFIG.EVENT_HIGHLIGHT_SHOW, 
										highlightId:'validDockedShape',
										elements:	[uiObj],
										color:		color
									});
	},
	
	showMagnets: function(uiObj){
		uiObj.magnets.each(function(magnet) {
			magnet.show();
		});
	},
	
	hideMagnets: function(uiObj){
		uiObj.magnets.each(function(magnet) {
			magnet.hide();
		});
	},
	
	getHighestParentBeforeCanvas: function(shape) {
		if(!(shape instanceof ORYX.Core.Shape)) {return undefined;}
		
		var parent = shape.parent;
		while(parent && !(parent.parent instanceof ORYX.Core.Canvas)) {
			parent = parent.parent;
		}	
		
		return parent;		
	}	

});

