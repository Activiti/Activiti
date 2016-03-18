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

/**
 * @classDescription Base class for Shapes.
 * @extends ORYX.Core.AbstractShape
 */
ORYX.Core.Shape = {

	/**
	 * Constructor
	 */
	construct: function(options, stencil, facade) {
		// call base class constructor
		arguments.callee.$.construct.apply(this, arguments);
		
		this.facade = facade;
		this.dockers = [];
		this.magnets = [];
		
		this._defaultMagnet;
		
		this.incoming = [];
		this.outgoing = [];
		
		this.nodes = [];
		
		this._dockerChangedCallback = this._dockerChanged.bind(this);
		
		//Hash map for all labels. Labels are not treated as children of shapes.
		this._labels = new Hash();
		
		// create SVG node
		this.node = ORYX.Editor.graft("http://www.w3.org/2000/svg",
			null,
			['g', {id:"svg-" + this.resourceId},
				['g', {"class": "stencils"},
					['g', {"class": "me"}],
					['g', {"class": "children", style:"overflow:hidden"}],
					['g', {"class": "edge"}]
				],
				['g', {"class": "controls"},
					['g', {"class": "dockers"}],
					['g', {"class": "magnets"}]				
				]
			]);
	},

	/**
	 * If changed flag is set, refresh method is called.
	 */
	update: function() {
		//if(this.isChanged) {
			//this.layout();
		//}
	},
	
	/**
	 * !!!Not called from any sub class!!!
	 */
	_update: function() {

	},
	
	/**
	 * Calls the super class refresh method
	 *  and updates the svg elements that are referenced by a property.
	 */
	refresh: function() {
		//call base class refresh method
		arguments.callee.$.refresh.apply(this, arguments);
		
		if(this.node.ownerDocument) {
			//adjust SVG to properties' values
			var me = this;
			this.propertiesChanged.each((function(propChanged) {
				if(propChanged.value) {
					var prop = this.properties[propChanged.key];
					var property = this.getStencil().property(propChanged.key);
					if (property != undefined) {
						this.propertiesChanged[propChanged.key] = false;
	
						//handle choice properties
						if(property.type() == ORYX.CONFIG.TYPE_CHOICE) {
							//iterate all references to SVG elements
							property.refToView().each((function(ref) {
								//if property is referencing a label, update the label
								if(ref !== "") {
									var label = this._labels[this.id + ref];
									if (label && property.item(prop)) {
										label.text(property.item(prop).title());
									}
								}
							}).bind(this));
								
							//if the choice's items are referencing SVG elements
							// show the selected and hide all other referenced SVG
							// elements
							var refreshedSvgElements = new Hash();
							property.items().each((function(item) {
								item.refToView().each((function(itemRef) {
									if(itemRef == "") { return; }
									
									var svgElem = this.node.ownerDocument.getElementById(this.id + itemRef);
		
									if(!svgElem) { return; }
									
									
									/* Do not refresh the same svg element multiple times */
									if(!refreshedSvgElements[svgElem.id] || prop == item.value()) {
										svgElem.setAttributeNS(null, 'display', ((prop == item.value()) ? 'inherit' : 'none'));
										refreshedSvgElements[svgElem.id] = svgElem;
									}
									
									// Reload the href if there is an image-tag
									if(ORYX.Editor.checkClassType(svgElem, SVGImageElement)) {
										svgElem.setAttributeNS('http://www.w3.org/1999/xlink', 'href', svgElem.getAttributeNS('http://www.w3.org/1999/xlink', 'href'));
									}
								}).bind(this));
							}).bind(this));
							
						} else { //handle properties that are not of type choice
							//iterate all references to SVG elements
							property.refToView().each((function(ref) {
								//if the property does not reference an SVG element,
								// do nothing
							    if(ref === "") { return; }

								var refId = this.id + ref;
								
								if (property.type() === ORYX.CONFIG.TYPE_KISBPM_MULTIINSTANCE)
								{
									if (ref === "multiinstance") {
										
										var svgElemParallel = this.node.ownerDocument.getElementById(this.id + 'parallel');
										if(svgElemParallel) 
										{
											if (prop === 'Parallel')
											{
												svgElemParallel.setAttributeNS(null, 'display', 'inherit');
											}
											else
											{
												svgElemParallel.setAttributeNS(null, 'display', 'none');
											}
										} 
										
										var svgElemSequential = this.node.ownerDocument.getElementById(this.id + 'sequential');
										
										if(svgElemSequential) 
										{
											if (prop === 'Sequential')
											{
												svgElemSequential.setAttributeNS(null, 'display', 'inherit');
											}
											else
											{
												svgElemSequential.setAttributeNS(null, 'display', 'none');
											}
										} 
									}
									return;
									
								} 
								else if (property.type() === "cancelactivity")
								{
									var svgElemFrame = this.node.ownerDocument.getElementById(this.id + 'frame');
									var svgElemFrame2 = this.node.ownerDocument.getElementById(this.id + 'frame2');
									
									if (prop === 'true')
									{
										svgElemFrame.setAttributeNS(null, 'display', 'inherit');
										svgElemFrame2.setAttributeNS(null, 'display', 'inherit');
									}
									else
									{
										svgElemFrame.setAttributeNS(null, 'display', 'none');
										svgElemFrame2.setAttributeNS(null, 'display', 'none');
									}
								}
								
								//get the SVG element
								var svgElem = this.node.ownerDocument.getElementById(refId);
								
								//if the SVG element can not be found
								if(!svgElem || !(svgElem.ownerSVGElement)) { 
									//if the referenced SVG element is a SVGAElement, it cannot
									// be found with getElementById (Firefox bug).
									// this is a work around
									if(property.type() === ORYX.CONFIG.TYPE_URL || property.type() === ORYX.CONFIG.TYPE_DIAGRAM_LINK) {
										var svgElems = this.node.ownerDocument.getElementsByTagNameNS('http://www.w3.org/2000/svg', 'a');
										
										svgElem = $A(svgElems).find(function(elem) {
											return elem.getAttributeNS(null, 'id') === refId;
										});
										
										if(!svgElem) { return; } 
									} else {
										//this.propertiesChanged[propChanged.key] = true;
										return;
									}					
								}
								
								if (property.complexAttributeToView()) {
									var label = this._labels[refId];
									if (label) {
										try {
									    	propJson = prop.evalJSON();
									    	var value = propJson[property.complexAttributeToView()]
									    	label.text(value ? value : prop);
									    } catch (e) {
									    	label.text(prop);
									    }
									}
									
								} else {
									switch (property.type()) {
										case ORYX.CONFIG.TYPE_BOOLEAN:	
										    if (typeof prop == "string")
												prop = prop === "true"
		
											svgElem.setAttributeNS(null, 'display', (!(prop === property.inverseBoolean())) ? 'inherit' : 'none');
											
											break;
										case ORYX.CONFIG.TYPE_COLOR:
											if(property.fill()) {
												if (svgElem.tagName.toLowerCase() === "stop"){
													if (prop){
														
														if (property.lightness() &&  property.lightness() !== 1){
															prop = ORYX.Utils.adjustLightness(prop, property.lightness());
														}
														
														svgElem.setAttributeNS(null, "stop-color", prop);
													
														// Adjust stop color of the others
														if (svgElem.parentNode.tagName.toLowerCase() === "radialgradient"){
															ORYX.Utils.adjustGradient(svgElem.parentNode, svgElem);
														}
													}
													
													// If there is no value, set opaque
													if (svgElem.parentNode.tagName.toLowerCase() === "radialgradient"){
														$A(svgElem.parentNode.getElementsByTagName('stop')).each(function(stop){
															stop.setAttributeNS(null, "stop-opacity", prop ? stop.getAttributeNS(ORYX.CONFIG.NAMESPACE_ORYX, 'default-stop-opacity') || 1 : 0);
														}.bind(this))
													}
												} else {
													svgElem.setAttributeNS(null, 'fill', prop);
												}
											}
											if(property.stroke()) {
												svgElem.setAttributeNS(null, 'stroke', prop);
											}
											break;
										case ORYX.CONFIG.TYPE_STRING:
											var label = this._labels[refId];
											if (label) {
												label.text(prop);
											}
											break;
										case ORYX.CONFIG.TYPE_EXPRESSION:
											var label = this._labels[refId];
											if (label) {
												label.text(prop);
											}
											break;
										case ORYX.CONFIG.TYPE_DATASOURCE:
											var label = this._labels[refId];
											if (label) {
												label.text(prop);
											}
											break;	
										case ORYX.CONFIG.TYPE_INTEGER:
											var label = this._labels[refId];
											if (label) {
												label.text(prop);
											}
											break;
										case ORYX.CONFIG.TYPE_FLOAT:
											if(property.fillOpacity()) {
												svgElem.setAttributeNS(null, 'fill-opacity', prop);
											} 
											if(property.strokeOpacity()) {
												svgElem.setAttributeNS(null, 'stroke-opacity', prop);
											}
											if(!property.fillOpacity() && !property.strokeOpacity()) {
												var label = this._labels[refId];
												if (label) {
													label.text(prop);
												}
											}
											break;
										
	  								 	case ORYX.CONFIG.TYPE_SUB_PROCESS_LINK:
	  								 	    if (ref == "subprocesslink") {
	  											var onclickAttr = svgElem.getAttributeNodeNS('', 'onclick');
	  											var styleAttr = svgElem.getAttributeNodeNS('', 'style');
	  											
	  											if (onclickAttr) {
	  												if (prop && prop.id) {
	  													if (styleAttr) {
                                                        	styleAttr.textContent = "cursor:pointer;"
                                                    	}
	  													onclickAttr.textContent = "KISBPM.TOOLBAR.ACTIONS.navigateToProcess(" + prop.id + ");return false;";
	  							    	   			} else {
	  							    	   				if (styleAttr) {
                                                        	styleAttr.textContent = "cursor:default;"
                                                    	}
	  							    	   				onclickAttr.textContent = "return false;";
	  							    		        }
	  											}
	  										}
	  										break;
	  										
										case ORYX.CONFIG.TYPE_URL:
											break;
										
									}
								}
							}).bind(this));
							
							
						}
					}
					
				}
			}).bind(this));
			
			//update labels
			this._labels.values().each(function(label) {
				label.update();
			});
		}
	},
	
	layout: function() {
		//this.getStencil().layout(this)
		var layoutEvents = this.getStencil().layout()
		if (layoutEvents) {
			layoutEvents.each(function(event) {
				
				// setup additional attributes
				event.shape = this;
				event.forceExecution = true;
				
				// do layouting
				this._delegateEvent(event);
			}.bind(this))
			
		}
	},
	
	/**
	 * Returns an array of Label objects.
	 */
	getLabels: function() {
		return this._labels.values();
	},
	
	/**
	 * Returns the label for a given ref
	 * @return {ORYX.Core.Label} Returns null if there is no label
	 */
	getLabel: function(ref){
		if (!ref){
			return null;
		}
		return (this._labels.find(function(o){
				return o.key.endsWith(ref);
			})||{}).value || null;
	},
	
	/**
	 * Hides all related labels
	 * 
	 */
	hideLabels: function(){
		this.getLabels().invoke("hide");
	},

	/**
	 * Shows all related labels
	 * 
	 */
	showLabels: function(){
		var labels = this.getLabels();
		labels.invoke("show");
		labels.each(function(label) {
				label.update();
		});
	},
	
	setOpacity: function(value, animate){
		
		value = Math.max(Math.min((typeof value == "number" ? value : 1.0), 1.0), 0.0);
				
		if (value !== 1.0){
			value = String(value);
			this.node.setAttributeNS(null, "fill-opacity", value)
			this.node.setAttributeNS(null, "stroke-opacity", value)
		} else {
			this.node.removeAttributeNS(null, "fill-opacity");
			this.node.removeAttributeNS(null, "stroke-opacity");
		}
	},
	
	/**
	 * Returns an array of dockers of this object.
	 */
	getDockers: function() {
		return this.dockers;
	},
	
	getMagnets: function() {
		return this.magnets;
	},
	
	getDefaultMagnet: function() {
		if(this._defaultMagnet) {
			return this._defaultMagnet;
		} else if (this.magnets.length > 0) {
			return this.magnets[0];
		} else {
			return undefined;
		}
	},

	getParentShape: function() {
		return this.parent;
	},
	
	getIncomingShapes: function(iterator) {
		if(iterator) {
			this.incoming.each(iterator);
		}
		return this.incoming;
	},
	
	getIncomingNodes: function(iterator) {
        return this.incoming.select(function(incoming){
            var isNode = (incoming instanceof ORYX.Core.Node);
            if(isNode && iterator) iterator(incoming);
            return isNode;
        });
    },
	
	
	getOutgoingShapes: function(iterator) {
		if(iterator) {
			this.outgoing.each(iterator);
		}
		return this.outgoing;
	},
    
    getOutgoingNodes: function(iterator) {
        return this.outgoing.select(function(out){
            var isNode = (out instanceof ORYX.Core.Node);
            if(isNode && iterator) iterator(out);
            return isNode;
        });
    },
	
	getAllDockedShapes: function(iterator) {
		var result = this.incoming.concat(this.outgoing);
		if(iterator) {
			result.each(iterator);
		}
		return result
	},

	getCanvas: function() {
		if(this.parent instanceof ORYX.Core.Canvas) {
			return this.parent;
		} else if(this.parent instanceof ORYX.Core.Shape) {
			return this.parent.getCanvas();
		} else {
			return undefined;
		}
	},
	
	/**
	 * 
	 * @param {Object} deep
	 * @param {Object} iterator
	 */
	getChildNodes: function(deep, iterator) {
		if(!deep && !iterator) {
			return this.nodes.clone();
		} else {
			var result = [];
			this.nodes.each(function(uiObject) {
				if(!uiObject.isVisible){return}
				if(iterator) {
					iterator(uiObject);
				}
				result.push(uiObject);
				
				if(deep && uiObject instanceof ORYX.Core.Shape) {
					result = result.concat(uiObject.getChildNodes(deep, iterator));
				}
			});
	
			return result;
		}
	},
	
	/**
	 * Overrides the UIObject.add method. Adds uiObject to the correct sub node.
	 * @param {UIObject} uiObject
	 * @param {Number} index
	 */
	add: function(uiObject, index, silent) {
		//parameter has to be an UIObject, but
		// must not be an Edge.
		if(uiObject instanceof ORYX.Core.UIObject 
			&& !(uiObject instanceof ORYX.Core.Edge)) {
			
			if (!(this.children.member(uiObject))) {
				//if uiObject is child of another parent, remove it from that parent.
				if(uiObject.parent) {
					uiObject.parent.remove(uiObject, true);
				}

				//add uiObject to this Shape
				if(index != undefined)
					this.children.splice(index, 0, uiObject);
				else
					this.children.push(uiObject);

				//set parent reference
				uiObject.parent = this;

				//add uiObject.node to this.node depending on the type of uiObject
				var parent;
				if(uiObject instanceof ORYX.Core.Node) {
					parent = this.node.childNodes[0].childNodes[1];
					this.nodes.push(uiObject);
				} else if(uiObject instanceof ORYX.Core.Controls.Control) {
					var ctrls = this.node.childNodes[1];
					if(uiObject instanceof ORYX.Core.Controls.Docker) {
						parent = ctrls.childNodes[0];
						if (this.dockers.length >= 2){
							this.dockers.splice(index!==undefined?Math.min(index, this.dockers.length-1):this.dockers.length-1, 0, uiObject);
						} else {
							this.dockers.push(uiObject);
						}
					} else if(uiObject instanceof ORYX.Core.Controls.Magnet) {
						parent = ctrls.childNodes[1];
						this.magnets.push(uiObject);
					} else {
						parent = ctrls;
					}
				} else {	//UIObject
					parent = this.node;
				}

				if(index != undefined && index < parent.childNodes.length)
					uiObject.node = parent.insertBefore(uiObject.node, parent.childNodes[index]);
				else
					uiObject.node = parent.appendChild(uiObject.node);
					
				this._changed();
				//uiObject.bounds.registerCallback(this._changedCallback);
				
				
				if(this.eventHandlerCallback && silent !== true)
					this.eventHandlerCallback({type:ORYX.CONFIG.EVENT_SHAPEADDED,shape:uiObject})
					
			} else {

				ORYX.Log.warn("add: ORYX.Core.UIObject is already a child of this object.");
			}
		} else {

			ORYX.Log.warn("add: Parameter is not of type ORYX.Core.UIObject.");
		}
	},

	/**
	 * Overrides the UIObject.remove method. Removes uiObject.
	 * @param {UIObject} uiObject
	 */
	remove: function(uiObject, silent) {
		//if uiObject is a child of this object, remove it.
		if (this.children.member(uiObject)) {
			//remove uiObject from children
			var parent = uiObject.parent;

			this.children = this.children.without(uiObject);

			//delete parent reference of uiObject
			uiObject.parent = undefined;

			//delete uiObject.node from this.node
			if(uiObject instanceof ORYX.Core.Shape) {
				if(uiObject instanceof ORYX.Core.Edge) {
					uiObject.removeMarkers();
					uiObject.node = this.node.childNodes[0].childNodes[2].removeChild(uiObject.node);
				} else {
					uiObject.node = this.node.childNodes[0].childNodes[1].removeChild(uiObject.node);
					this.nodes = this.nodes.without(uiObject);
				}
			} else if(uiObject instanceof ORYX.Core.Controls.Control) {
				if (uiObject instanceof ORYX.Core.Controls.Docker) {
					uiObject.node = this.node.childNodes[1].childNodes[0].removeChild(uiObject.node);
					this.dockers = this.dockers.without(uiObject);
				} else if (uiObject instanceof ORYX.Core.Controls.Magnet) {
					uiObject.node = this.node.childNodes[1].childNodes[1].removeChild(uiObject.node);
					this.magnets = this.magnets.without(uiObject);
				} else {
					uiObject.node = this.node.childNodes[1].removeChild(uiObject.node);
				}
			}

			if(this.eventHandlerCallback && silent !== true)
				this.eventHandlerCallback({type: ORYX.CONFIG.EVENT_SHAPEREMOVED, shape: uiObject, parent: parent});
			
			this._changed();
			//uiObject.bounds.unregisterCallback(this._changedCallback);
		} else {

			ORYX.Log.warn("remove: ORYX.Core.UIObject is not a child of this object.");
		}
	},
	
	/**
	 * Calculate the Border Intersection Point between two points
	 * @param {PointA}
	 * @param {PointB}
	 */
	getIntersectionPoint: function() {
			
		var pointAX, pointAY, pointBX, pointBY;
		
		// Get the the two Points	
		switch(arguments.length) {
			case 2:
				pointAX = arguments[0].x;
				pointAY = arguments[0].y;
				pointBX = arguments[1].x;
				pointBY = arguments[1].y;
				break;
			case 4:
				pointAX = arguments[0];
				pointAY = arguments[1];
				pointBX = arguments[2];
				pointBY = arguments[3];
				break;
			default:
				throw "getIntersectionPoints needs two or four arguments";
		}
		
		
		
		// Defined an include and exclude point
		var includePointX, includePointY, excludePointX, excludePointY;

		var bounds = this.absoluteBounds();
		
		if(this.isPointIncluded(pointAX, pointAY, bounds)){
			includePointX = pointAX;
			includePointY = pointAY;
		} else {
			excludePointX = pointAX;
			excludePointY = pointAY;
		}

		if(this.isPointIncluded(pointBX, pointBY, bounds)){
			includePointX = pointBX;
			includePointY = pointBY;
		} else {
			excludePointX = pointBX;
			excludePointY = pointBY;
		}
				
		// If there is no inclue or exclude Shape, than return
		if(!includePointX || !includePointY || !excludePointX || !excludePointY) {
			return undefined;
		}

		var midPointX = 0;
		var midPointY = 0;		
		
		var refPointX, refPointY;
		
		var minDifferent = 1;
		// Get the UpperLeft and LowerRight
		//var ul = bounds.upperLeft();
		//var lr = bounds.lowerRight();
		
		var i = 0;
		
		while(true) {
			// Calculate the midpoint of the current to points	
			var midPointX = Math.min(includePointX, excludePointX) + ((Math.max(includePointX, excludePointX) - Math.min(includePointX, excludePointX)) / 2.0);
			var midPointY = Math.min(includePointY, excludePointY) + ((Math.max(includePointY, excludePointY) - Math.min(includePointY, excludePointY)) / 2.0);
			
			
			// Set the new midpoint by the means of the include of the bounds
			if(this.isPointIncluded(midPointX, midPointY, bounds)){
				includePointX = midPointX;
				includePointY = midPointY;
			} else {
				excludePointX = midPointX;
				excludePointY = midPointY;
			}			
			
			// Calc the length of the line
			var length = Math.sqrt(Math.pow(includePointX - excludePointX, 2) + Math.pow(includePointY - excludePointY, 2))
			// Calc a point one step from the include point
			refPointX = includePointX + ((excludePointX - includePointX) / length),
			refPointY = includePointY + ((excludePointY - includePointY) / length)
					
			
			// If the reference point not in the bounds, break
			if(!this.isPointIncluded(refPointX, refPointY, bounds)) {
				break
			}
							
			
		}

		// Return the last includepoint
		return {x:refPointX , y:refPointY};
	},

   
    
    /**
     * Calculate if the point is inside the Shape
     * @param {PointX}
     * @param {PointY} 
     */
    isPointIncluded: function(){
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
		var me = this.node.firstChild.firstChild;
		while(node){
			if (node == me){
				return true;
			}
			node = node.parentNode;
		}
		return false
	},
    
    /**
     * Calculate if the point is over an special offset area
     * @param {Point}
     */
    isPointOverOffset: function(){
		return  this.isPointIncluded.apply( this , arguments )
	},
		
	_dockerChanged: function() {

	},
		
	/**
	 * Create a Docker for this Edge
	 *
	 */
	createDocker: function(index, position) {
		var docker = new ORYX.Core.Controls.Docker({eventHandlerCallback: this.eventHandlerCallback});
		docker.bounds.registerCallback(this._dockerChangedCallback);
		if (position) {
			docker.bounds.centerMoveTo(position);
		}
		this.add(docker, index);
		
		return docker
	},

	/**
	 * Get the serialized object
	 * return Array with hash-entrees (prefix, name, value)
	 * Following values will given:
	 * 		Bounds
	 * 		Outgoing Shapes
	 * 		Parent
	 */
	serialize: function() {
		var serializedObject = arguments.callee.$.serialize.apply(this);

		// Add the bounds
		serializedObject.push({name: 'bounds', prefix:'oryx', value: this.bounds.serializeForERDF(), type: 'literal'});

		// Add the outgoing shapes
		this.getOutgoingShapes().each((function(followingShape){
			serializedObject.push({name: 'outgoing', prefix:'raziel', value: '#'+ERDF.__stripHashes(followingShape.resourceId), type: 'resource'});			
		}).bind(this));

		// Add the parent shape, if the parent not the canvas
		//if(this.parent instanceof ORYX.Core.Shape){
			serializedObject.push({name: 'parent', prefix:'raziel', value: '#'+ERDF.__stripHashes(this.parent.resourceId), type: 'resource'});	
		//}			
		
		return serializedObject;
	},
		
		
	deserialize: function(serialize, json){
		arguments.callee.$.deserialize.apply(this, arguments);
		
		// Set the Bounds
		var bounds = serialize.find(function(ser){ return 'oryx-bounds' === (ser.prefix+"-"+ser.name) });
		if (bounds) {
			var b = bounds.value.replace(/,/g, " ").split(" ").without("");
			if (this instanceof ORYX.Core.Edge) {
				if (!this.dockers.first().isChanged)
					this.dockers.first().bounds.centerMoveTo(parseFloat(b[0]), parseFloat(b[1]));
				if (!this.dockers.last().isChanged)
					this.dockers.last().bounds.centerMoveTo(parseFloat(b[2]), parseFloat(b[3]));
			} else {
				this.bounds.set(parseFloat(b[0]), parseFloat(b[1]), parseFloat(b[2]), parseFloat(b[3]));
			}
		}
		
		if (json && json.labels instanceof Array){
			json.labels.each(function(slabel){
				var label = this.getLabel(slabel.ref);
				if (label){
					label.deserialize(slabel, this);
				}
			}.bind(this))
		}
	},
	
	toJSON: function(){
		var json = arguments.callee.$.toJSON.apply(this, arguments);
		
		var labels = [], id = this.id;
		this._labels.each(function(obj){
			var slabel = obj.value.serialize();
			if (slabel){
				slabel.ref = obj.key.replace(id, '');
				labels.push(slabel);
			}
		});
		
		if (labels.length > 0){
			json.labels = labels;
		}
		return json;
	},

		
	/**
	 * Private methods.
	 */

	/**
	 * Child classes have to overwrite this method for initializing a loaded
	 * SVG representation.
	 * @param {SVGDocument} svgDocument
	 */
	_init: function(svgDocument) {
		//adjust ids
		this._adjustIds(svgDocument, 0);
	},

	_adjustIds: function(element, idIndex) {
		if(element instanceof Element) {
			var eid = element.getAttributeNS(null, 'id');
			if(eid && eid !== "") {
				element.setAttributeNS(null, 'id', this.id + eid);
			} else {
				element.setAttributeNS(null, 'id', this.id + "_" + this.id + "_" + idIndex);
				idIndex++;
			}
			
			// Replace URL in fill attribute
			var fill = element.getAttributeNS(null, 'fill');
			if (fill&&fill.include("url(#")){
				fill = fill.replace(/url\(#/g, 'url(#'+this.id);
				element.setAttributeNS(null, 'fill', fill);
			}
			
			if(element.hasChildNodes()) {
				for(var i = 0; i < element.childNodes.length; i++) {
					idIndex = this._adjustIds(element.childNodes[i], idIndex);
				}
			}
		}
		return idIndex;
	},

	toString: function() { return "ORYX.Core.Shape " + this.getId() }
};
ORYX.Core.Shape = ORYX.Core.AbstractShape.extend(ORYX.Core.Shape);