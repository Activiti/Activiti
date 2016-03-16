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

/**
   @namespace Namespace for the Oryx core elements.
   @name ORYX.Core
*/
if(!ORYX.Core) {ORYX.Core = {};}

/**
 * @class Oryx canvas.
 * @extends ORYX.Core.AbstractShape
 *
 */
ORYX.Core.Canvas = ORYX.Core.AbstractShape.extend({
    /** @lends ORYX.Core.Canvas.prototype */

	/**
	 * Defines the current zoom level
	 */
	zoomLevel:1,

	/**
	 * Constructor
	 */
	construct: function(options, stencil, facade) {
		arguments.callee.$.construct.apply(this, arguments);

		if(!(options && options.width && options.height)) {
		
			ORYX.Log.fatal("Canvas is missing mandatory parameters options.width and options.height.");
			return;
		}
		this.facade = facade;	
		//TODO: set document resource id
		this.resourceId = options.id;

		this.nodes = [];
		
		this.edges = [];
		
		// Row highlighting states
		this.colHighlightState = 0;
		
		this.colHighlightEnabled = false; 
		
		//init svg document
		this.rootNode = ORYX.Editor.graft("http://www.w3.org/2000/svg", options.parentNode,
			['svg', {id: this.id, width: options.width, height: options.height},
				['defs', {}]
			]);
			
		this.rootNode.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
		this.rootNode.setAttribute("xmlns:svg", "http://www.w3.org/2000/svg");

		this._htmlContainer = ORYX.Editor.graft("http://www.w3.org/1999/xhtml", options.parentNode,
			['div', {id: "oryx_canvas_htmlContainer", style:"position:absolute; top:5px"}]);

		// Additional SVG-node BELOW the stencils to allow underlays (if that is even a word) by plugins
		this.underlayNode = ORYX.Editor.graft("http://www.w3.org/2000/svg", this.rootNode,
				['svg', {id: "underlay-container"}]);
		
		// Create 2 svg-elements in the svg-container
		this.columnHightlight1 = ORYX.Editor.graft("http://www.w3.org/2000/svg", this.underlayNode,
				['rect', {x: 0, width: ORYX.CONFIG.FORM_ROW_WIDTH + 35, height: "100%", style: "fill: #fff6d5", visibility: "hidden"}]);
		
		this.columnHightlight2 = ORYX.Editor.graft("http://www.w3.org/2000/svg", this.underlayNode,
				['rect', {x: ORYX.CONFIG.FORM_ROW_WIDTH + 35, width: ORYX.CONFIG.FORM_ROW_WIDTH + 25, height: "100%", style: "fill: #fff6d5", visibility: "hidden"}]);
		
		this.node = ORYX.Editor.graft("http://www.w3.org/2000/svg", this.rootNode,
			['g', {},
				['g', {"class": "stencils"},
					['g', {"class": "me"}],
					['g', {"class": "children"}],
					['g', {"class": "edge"}]
				],
				['g', {"class":"svgcontainer"}]
			]);
		
		/*
		var off = 2 * ORYX.CONFIG.GRID_DISTANCE;
		var size = 3;
		var d = "";
		for(var i = 0; i <= options.width; i += off)
			for(var j = 0; j <= options.height; j += off)
				d = d + "M" + (i - size) + " " + j + " l" + (2*size) + " 0 m" + (-size) + " " + (-size) + " l0 " + (2*size) + " m0" + (-size) + " ";
							
		ORYX.Editor.graft("http://www.w3.org/2000/svg", this.node.firstChild.firstChild,
			['path', {d:d , stroke:'#000000', 'stroke-width':'0.15px'},]);
		*/
		
		//Global definition of default font for shapes
		//Definitions in the SVG definition of a stencil will overwrite these settings for
		// that stencil.
		/*if(navigator.platform.indexOf("Mac") > -1) {
			this.node.setAttributeNS(null, 'stroke', 'black');
			this.node.setAttributeNS(null, 'stroke-width', '0.5px');
			this.node.setAttributeNS(null, 'font-family', 'Skia');
			//this.node.setAttributeNS(null, 'letter-spacing', '2px');
			this.node.setAttributeNS(null, 'font-size', ORYX.CONFIG.LABEL_DEFAULT_LINE_HEIGHT);
		} else {
			this.node.setAttributeNS(null, 'stroke', 'none');
			this.node.setAttributeNS(null, 'font-family', 'Verdana');
			this.node.setAttributeNS(null, 'font-size', ORYX.CONFIG.LABEL_DEFAULT_LINE_HEIGHT);
		}*/
		
		this.node.setAttributeNS(null, 'stroke', 'none');
		this.node.setAttributeNS(null, 'font-family', 'Verdana, sans-serif');
		this.node.setAttributeNS(null, 'font-size-adjust', 'none');
		this.node.setAttributeNS(null, 'font-style', 'normal');
		this.node.setAttributeNS(null, 'font-variant', 'normal');
		this.node.setAttributeNS(null, 'font-weight', 'normal');
		this.node.setAttributeNS(null, 'line-heigth', 'normal');
		
		this.node.setAttributeNS(null, 'font-size', ORYX.CONFIG.LABEL_DEFAULT_LINE_HEIGHT);
			
		this.bounds.set(0,0,options.width, options.height);
		
		this.addEventHandlers(this.rootNode.parentNode);
		
		//disable context menu
		this.rootNode.oncontextmenu = function() {return false;};
	},
	
	focus: function(){
		
		try {
			// Get a href
			if (!this.focusEl) 
			{
				this.focusEl = jQuery('body').append(jQuery('<a href="#" class="x-grid3-focus x-grid3-focus-canvas"/>'));
				this.focusEl.swallowEvent("click", true);
			}
			
			// Focus it
			this.focusEl.focus.defer(1, this.focusEl);
			this.focusEl.blur.defer(3, this.focusEl);
			
		} catch(e){}
	},
	
	setHightlightState: function(state) {
		if(this.colHighlightEnabled && this.colHighlightState != state) {
			if(state == 0) {
				this.columnHightlight1.setAttribute("visibility", "hidden");
				this.columnHightlight2.setAttribute("visibility", "hidden");
			} else if(state == 1) {
				this.columnHightlight1.setAttribute("visibility", "visible");
				this.columnHightlight2.setAttribute("visibility", "hidden");
			} else if(state == 2) {
				this.columnHightlight1.setAttribute("visibility", "hidden");
				this.columnHightlight2.setAttribute("visibility", "visible");
			} else if(state == 3) {
				this.columnHightlight1.setAttribute("visibility", "visible");
				this.columnHightlight2.setAttribute("visibility", "visible");
			}
			this.colHighlightState = state;
		}
	},
	
	setHightlightStateBasedOnX : function(x) {
		if(x > ORYX.CONFIG.FORM_ROW_WIDTH + 30) {
			this.setHightlightState(2);
		} else {
			this.setHightlightState(1);
		}
	},
	
	update: function() {
		
		this.nodes.each(function(node) {
			this._traverseForUpdate(node);
		}.bind(this));
		
		// call stencil's layout callback
		// (needed for row layouting of xforms)
		//this.getStencil().layout(this);
		
		var layoutEvents = this.getStencil().layout();
		
		if(layoutEvents) {
			layoutEvents.each(function(event) {
		
				// setup additional attributes
				event.shape = this;
				event.forceExecution = true;
				event.target = this.rootNode;
				
				// do layouting
				
				this._delegateEvent(event);
			}.bind(this))
		}
		
		this.nodes.invoke("_update");
		
		this.edges.invoke("_update", true);
		
		/*this.children.each(function(child) {
			child._update();
		});*/
	},
	
	_traverseForUpdate: function(shape) {
		var childRet = shape.isChanged;
		shape.getChildNodes(false, function(child) {
			if(this._traverseForUpdate(child)) {
				childRet = true;
			}
		}.bind(this));
		
		if(childRet) {
			shape.layout();
			return true;
		} else {
			return false;
		}
	},
	
	layout: function() {
		
		
		
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
	 * buggy crap! use base class impl instead! 
	 * @param {Object} iterator
	 */
/*	getChildEdges: function(iterator) {
		if(iterator) {
			this.edges.each(function(edge) {
				iterator(edge);
			});
		}
		
		return this.edges.clone();
	},
*/	
	/**
	 * Overrides the UIObject.add method. Adds uiObject to the correct sub node.
	 * @param {UIObject} uiObject
	 */
	add: function(uiObject, index, silent) {
		//if uiObject is child of another UIObject, remove it.
		if(uiObject instanceof ORYX.Core.UIObject) {
			if (!(this.children.member(uiObject))) {
				//if uiObject is child of another parent, remove it from that parent.
				if(uiObject.parent) {
					uiObject.parent.remove(uiObject, true);
				}

				//add uiObject to the Canvas
				//add uiObject to this Shape
				if(index != undefined)
					this.children.splice(index, 0, uiObject);
				else
					this.children.push(uiObject);

				//set parent reference
				uiObject.parent = this;

				//add uiObject.node to this.node depending on the type of uiObject
				if(uiObject instanceof ORYX.Core.Shape) {
					if(uiObject instanceof ORYX.Core.Edge) {
						uiObject.addMarkers(this.rootNode.getElementsByTagNameNS(NAMESPACE_SVG, "defs")[0]);
						uiObject.node = this.node.childNodes[0].childNodes[2].appendChild(uiObject.node);
						this.edges.push(uiObject);
					} else {
						uiObject.node = this.node.childNodes[0].childNodes[1].appendChild(uiObject.node);
						this.nodes.push(uiObject);
					}
				} else {	//UIObject
					uiObject.node = this.node.appendChild(uiObject.node);
				}

				uiObject.bounds.registerCallback(this._changedCallback);
					
				if(this.eventHandlerCallback && silent !== true)
					this.eventHandlerCallback({type:ORYX.CONFIG.EVENT_SHAPEADDED,shape:uiObject})
			} else {
				
				ORYX.Log.warn("add: ORYX.Core.UIObject is already a child of this object.");
			}
		} else {

			ORYX.Log.fatal("add: Parameter is not of type ORYX.Core.UIObject.");
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
					this.edges = this.edges.without(uiObject);
				} else {
					uiObject.node = this.node.childNodes[0].childNodes[1].removeChild(uiObject.node);
					this.nodes = this.nodes.without(uiObject);
				}
			} else {	//UIObject
					uiObject.node = this.node.removeChild(uiObject.node);
			}

			if(this.eventHandlerCallback && silent !== true)
				this.eventHandlerCallback({type:ORYX.CONFIG.EVENT_SHAPEREMOVED,shape:uiObject, parent:parent});
				
			uiObject.bounds.unregisterCallback(this._changedCallback);
		} else {

			ORYX.Log.warn("remove: ORYX.Core.UIObject is not a child of this object.");
		}
	},
	
	removeAll: function() {
	    var childShapes = this.getChildShapes();
	    for (var i = 0; i < childShapes.length; i++) {
	        var childObject = childShapes[i];
	        this.remove(childObject);
	    }
	},
    
    /**
     * Creates shapes out of the given collection of shape objects and adds them to the canvas.
     * @example 
     * canvas.addShapeObjects({
         bounds:{ lowerRight:{ y:510, x:633 }, upperLeft:{ y:146, x:210 } },
         resourceId:"oryx_F0715955-50F2-403D-9851-C08CFE70F8BD",
         childShapes:[],
         properties:{},
         stencil:{
           id:"Subprocess"
         },
         outgoing:[{resourceId: 'aShape'}],
         target: {resourceId: 'aShape'}
       });
     * @param {Object} shapeObjects 
     * @param {Function} [eventHandler] An event handler passed to each newly created shape (as eventHandlerCallback)
     * @return {Array} A collection of ORYX.Core.Shape
     * @methodOf ORYX.Core.Canvas.prototype
     */
    addShapeObjects: function(shapeObjects, eventHandler){
        if(!shapeObjects) return;
		
		this.initializingShapes = true;
        
        /*FIXME This implementation is very evil! At first, all shapes are created on
          canvas. In a second step, the attributes are applied. There must be a distinction
          between the configuration phase (where the outgoings, for example, are just named),
          and the creation phase (where the outgoings are evaluated). This must be reflected
          in code to provide a nicer API/ implementation!!! */
        
        var addShape = function(shape, parent){
            // Create a new Stencil
            var stencil = ORYX.Core.StencilSet.stencil(this.getStencil().namespace() + shape.stencil.id );

            // Create a new Shape
            var ShapeClass = (stencil.type() == "node") ? ORYX.Core.Node : ORYX.Core.Edge;
            var newShape = new ShapeClass(
              {'eventHandlerCallback': eventHandler},
              stencil, this.facade);
            
            // Set the resource id
            newShape.resourceId = shape.resourceId;
            newShape.node.id = "svg-" + shape.resourceId;
			
            // Set parent to json object to be used later
            // Due to the nested json structure, normally shape.parent is not set/ must not be set. 
            // In special cases, it can be easier to set this directly instead of a nested structure.
            shape.parent = "#" + ((shape.parent && shape.parent.resourceId) || parent.resourceId);
            
            // Add the shape to the canvas
            this.add( newShape );

            return {
              json: shape,
              object: newShape
            };
        }.bind(this);
        
        /** Builds up recursively a flatted array of shapes, including a javascript object and json representation
         * @param {Object} shape Any object that has Object#childShapes
         */
        var addChildShapesRecursively = function(shape){
            var addedShapes = [];
        
            if (shape.childShapes && shape.childShapes.constructor == String)
            {
            	shape.childShapes = JSON.parse(shape.childShapes);
            }
 
            shape.childShapes.each(function(childShape){
              addedShapes.push(addShape(childShape, shape));
              addedShapes = addedShapes.concat(addChildShapesRecursively(childShape));
            });
            
            return addedShapes;
        }.bind(this);

        var shapes = addChildShapesRecursively({
            childShapes: shapeObjects, 
            resourceId: this.resourceId
        });
                    

        // prepare deserialisation parameter
        shapes.each(
            function(shape){
            	var properties = [];
                for(field in shape.json.properties){
                    properties.push({
                      prefix: 'oryx',
                      name: field,
                      value: shape.json.properties[field]
                    });
                  }
                  
                  // Outgoings
                  shape.json.outgoing.each(function(out){
                    properties.push({
                      prefix: 'raziel',
                      name: 'outgoing',
                      value: "#"+out.resourceId
                    });
                  });
                  
                  // Target 
                  // (because of a bug, the first outgoing is taken when there is no target,
                  // can be removed after some time)
                  if(shape.object instanceof ORYX.Core.Edge) {
	                  var target = shape.json.target || shape.json.outgoing[0];
	                  if(target){
	                    properties.push({
	                      prefix: 'raziel',
	                      name: 'target',
	                      value: "#"+target.resourceId
	                    });
	                  }
                  }
                  
                  // Bounds
                  if (shape.json.bounds) {
                      properties.push({
                          prefix: 'oryx',
                          name: 'bounds',
                          value: shape.json.bounds.upperLeft.x + "," + shape.json.bounds.upperLeft.y + "," + shape.json.bounds.lowerRight.x + "," + shape.json.bounds.lowerRight.y
                      });
                  }
                  
                  //Dockers [{x:40, y:50}, {x:30, y:60}] => "40 50 30 60  #"
                  if(shape.json.dockers){
                    properties.push({
                      prefix: 'oryx',
                      name: 'dockers',
                      value: shape.json.dockers.inject("", function(dockersStr, docker){
                        return dockersStr + docker.x + " " + docker.y + " ";
                      }) + " #"
                    });
                  }
                  
                  //Parent
                  properties.push({
                    prefix: 'raziel',
                    name: 'parent',
                    value: shape.json.parent
                  });
            
                  shape.__properties = properties;
	         }.bind(this)
        );
  
        // Deserialize the properties from the shapes
        // This can't be done earlier because Shape#deserialize expects that all referenced nodes are already there
        
        // first, deserialize all nodes
        shapes.each(function(shape) {
        	if(shape.object instanceof ORYX.Core.Node) {
        		shape.object.deserialize(shape.__properties, shape.json);
        	}
        });
        
        // second, deserialize all edges
        shapes.each(function(shape) {
        	if(shape.object instanceof ORYX.Core.Edge) {
        		shape.object.deserialize(shape.__properties, shape.json);
				shape.object._oldBounds = shape.object.bounds.clone();
				shape.object._update();
        	}
        });
       
		delete this.initializingShapes;
        return shapes.pluck("object");
    },
    
    /**
     * Updates the size of the canvas, regarding to the containg shapes.
     */
    updateSize: function(){
        // Check the size for the canvas
        var maxWidth    = 0;
        var maxHeight   = 0;
        var offset      = 100;
        this.getChildShapes(true, function(shape){
            var b = shape.bounds;
            maxWidth    = Math.max( maxWidth, b.lowerRight().x + offset)
            maxHeight   = Math.max( maxHeight, b.lowerRight().y + offset)
        }); 
        
        if( this.bounds.width() < maxWidth || this.bounds.height() < maxHeight ){
            this.setSize({width: Math.max(this.bounds.width(), maxWidth), height: Math.max(this.bounds.height(), maxHeight)})
        }
    },

	getRootNode: function() {
		return this.rootNode;
	},
	
	getUnderlayNode: function() {
		return this.underlayNode;
	},
	
	getSvgContainer: function() {
		return this.node.childNodes[1];
	},
	
	getHTMLContainer: function() {
		return this._htmlContainer;
	},	

	/**
	 * Return all elements of the same highest level
	 * @param {Object} elements
	 */
	getShapesWithSharedParent: function(elements) {

		// If there is no elements, return []
		if(!elements || elements.length < 1) { return []; }
		// If there is one element, return this element
		if(elements.length == 1) { return elements;}

		return elements.findAll(function(value){
			var parentShape = value.parent;
			while(parentShape){
				if(elements.member(parentShape)) return false;
				parentShape = parentShape.parent;
			}
			return true;
		});		

	},

	setSize: function(size, dontSetBounds) {
		if(!size || !size.width || !size.height){return;};
		
		if(this.rootNode.parentNode){
			this.rootNode.parentNode.style.width = size.width + 'px';
			this.rootNode.parentNode.style.height = size.height + 'px';
		}
		
		this.rootNode.setAttributeNS(null, 'width', size.width);
		this.rootNode.setAttributeNS(null, 'height', size.height);

		//this._htmlContainer.style.top = "-" + (size.height + 4) + 'px';		
		if( !dontSetBounds ){
			this.bounds.set({a:{x:0,y:0},b:{x:size.width/this.zoomLevel,y:size.height/this.zoomLevel}});
		}
	},
	
	/**
	 * Returns an SVG document of the current process.
	 * @param {Boolean} escapeText Use true, if you want to parse it with an XmlParser,
	 * 					false, if you want to use the SVG document in browser on client side.
	 */
	getSVGRepresentation: function(escapeText) {
		// Get the serialized svg image source
        var svgClone = this.getRootNode().cloneNode(true);
		
		this._removeInvisibleElements(svgClone);
		
		var x1, y1, x2, y2;
		this.getChildShapes(true).each(function(shape) {
			var absBounds = shape.absoluteBounds();
			var ul = absBounds.upperLeft();
			var lr = absBounds.lowerRight();
			if(x1 == undefined) {
				x1 = ul.x;
				y1 = ul.y;
				x2 = lr.x;
				y2 = lr.y;
			} else {
				x1 = Math.min(x1, ul.x);
				y1 = Math.min(y1, ul.y);
				x2 = Math.max(x2, lr.x);
				y2 = Math.max(y2, lr.y);
			}
		});
		
		var margin = 50;
		var width, height, tx, ty;
		if(x1 == undefined) {
			width = 0;
			height = 0;
			tx = 0;
			ty = 0;
		} else {
			width = x2;
			height = y2;
			tx = -x1+margin/2;
			ty = -y1+margin/2;
		}
		
        // Set the width and height
        svgClone.setAttributeNS(null, 'width', width + margin);
        svgClone.setAttributeNS(null, 'height', height + margin);
		
		//remove scale factor
		svgClone.childNodes[1].removeAttributeNS(null, 'transform');
		
		try{
			var svgCont = svgClone.childNodes[1].childNodes[1];
			svgCont.parentNode.removeChild(svgCont);
		} catch(e) {}

		if(escapeText) {
			$A(svgClone.getElementsByTagNameNS(ORYX.CONFIG.NAMESPACE_SVG, 'tspan')).each(function(elem) {
				elem.textContent = elem.textContent.escapeHTML();
			});
			
			$A(svgClone.getElementsByTagNameNS(ORYX.CONFIG.NAMESPACE_SVG, 'text')).each(function(elem) {
				if(elem.childNodes.length == 0)
					elem.textContent = elem.textContent.escapeHTML();
			});
		}
		
		// generating absolute urls for the pdf-exporter
		$A(svgClone.getElementsByTagNameNS(ORYX.CONFIG.NAMESPACE_SVG, 'image')).each(function(elem) {
			var href = elem.getAttributeNS("http://www.w3.org/1999/xlink","href");
			
			if(!href.match("^(http|https)://")) {
				href = window.location.protocol + "//" + window.location.host + href;
				elem.setAttributeNS("http://www.w3.org/1999/xlink", "href", href);
			}
		});
		
		
		// escape all links
		$A(svgClone.getElementsByTagNameNS(ORYX.CONFIG.NAMESPACE_SVG, 'a')).each(function(elem) {
			elem.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", (elem.getAttributeNS("http://www.w3.org/1999/xlink","href")||"").escapeHTML());
		});
		
        return svgClone;
	},
	
	/**   
	* Removes all nodes (and its children) that has the
	* attribute visibility set to "hidden"
	*/
	_removeInvisibleElements: function(element) {
		var index = 0;
		while(index < element.childNodes.length) {
			var child = element.childNodes[index];
			if(child.getAttributeNS &&
				child.getAttributeNS(null, "visibility") === "hidden") {
				element.removeChild(child);
			} else {
				this._removeInvisibleElements(child);
				index++; 
			}
		}
		
	},
	
	/**
	 * This method checks all shapes on the canvas and removes all shapes that
	 * contain invalid bounds values or dockers values(NaN)
	 */
	/*cleanUp: function(parent) {
		if (!parent) {
			parent = this;
		}
		parent.getChildShapes().each(function(shape){
			var a = shape.bounds.a;
			var b = shape.bounds.b;
			if (isNaN(a.x) || isNaN(a.y) || isNaN(b.x) || isNaN(b.y)) {
				parent.remove(shape);
			}
			else {
				shape.getDockers().any(function(docker) {
					a = docker.bounds.a;
					b = docker.bounds.b;
					if (isNaN(a.x) || isNaN(a.y) || isNaN(b.x) || isNaN(b.y)) {
						parent.remove(shape);
						return true;
					}
					return false;
				});
				shape.getMagnets().any(function(magnet) {
					a = magnet.bounds.a;
					b = magnet.bounds.b;
					if (isNaN(a.x) || isNaN(a.y) || isNaN(b.x) || isNaN(b.y)) {
						parent.remove(shape);
						return true;
					}
					return false;
				});
				this.cleanUp(shape);
			}
		}.bind(this));
	},*/

	_delegateEvent: function(event) {
		if(this.eventHandlerCallback && ( event.target == this.rootNode || event.target == this.rootNode.parentNode )) {
			this.eventHandlerCallback(event, this);
		}
	},
	
	toString: function() { return "Canvas " + this.id },
    
    /**
     * Calls {@link ORYX.Core.AbstractShape#toJSON} and adds some stencil set information.
     */
    toJSON: function() {
        var json = arguments.callee.$.toJSON.apply(this, arguments);
        
//		if(ORYX.CONFIG.STENCILSET_HANDLER.length > 0) {
//			json.stencilset = {
//				url: this.getStencil().stencilSet().namespace()
//	        };
//		} else {
			json.stencilset = {
				url: this.getStencil().stencilSet().source(),
				namespace: this.getStencil().stencilSet().namespace()
	        };	
//		}
        
        
        return json;
    }
 });