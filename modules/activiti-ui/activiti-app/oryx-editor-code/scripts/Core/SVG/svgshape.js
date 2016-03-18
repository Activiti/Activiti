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
 *
 * Config variables
 */
NAMESPACE_ORYX = "http://www.b3mn.org/oryx";
NAMESPACE_SVG = "http://www.w3.org/2000/svg/";

/**
 * @classDescription This class wraps the manipulation of a SVG basic shape or a path.
 * @namespace ORYX.Core.SVG
 * uses Inheritance (Clazz)
 * uses Prototype 1.5.0
 * uses PathParser by Kevin Lindsey (http://kevlindev.com/)
 * uses MinMaxPathHandler
 * uses EditPathHandler
 *
 */

//init package
if(!ORYX) {var ORYX = {};}
if(!ORYX.Core) {ORYX.Core = {};}
if(!ORYX.Core.SVG) {ORYX.Core.SVG = {};}

ORYX.Core.SVG.SVGShape = Clazz.extend({

	/**
	 * Constructor
	 * @param svgElem {SVGElement} An SVGElement that is a basic shape or a path.
	 */
	construct: function(svgElem) {
		arguments.callee.$.construct.apply(this, arguments);

		this.type;
		this.element = svgElem;
		this.x = undefined;
		this.y = undefined;
		this.width = undefined;
		this.height = undefined;
		this.oldX = undefined;
		this.oldY = undefined;
		this.oldWidth = undefined;
		this.oldHeight = undefined;
		this.radiusX = undefined;
		this.radiusY = undefined;
		this.isHorizontallyResizable = false;
		this.isVerticallyResizable = false;
		//this.anchors = [];
		this.anchorLeft = false;
		this.anchorRight = false;
		this.anchorTop = false;
		this.anchorBottom = false;
		
		//attributes of path elements of edge objects
		this.allowDockers = true;
		this.resizeMarkerMid = false;

		this.editPathParser;
		this.editPathHandler;

		this.init(); //initialisation of all the properties declared above.
	},

	/**
	 * Initializes the values that are defined in the constructor.
	 */
	init: function() {

		/**initialize position and size*/
		if(ORYX.Editor.checkClassType(this.element, SVGRectElement) || ORYX.Editor.checkClassType(this.element, SVGImageElement)) {
			this.type = "Rect";
			
			var xAttr = this.element.getAttributeNS(null, "x");
			if(xAttr) {
				this.oldX = parseFloat(xAttr);
			} else {
				throw "Missing attribute in element " + this.element;
			}
			var yAttr = this.element.getAttributeNS(null, "y");
			if(yAttr) {
				this.oldY = parseFloat(yAttr);
			} else {
				throw "Missing attribute in element " + this.element;
			}
			var widthAttr = this.element.getAttributeNS(null, "width");
			if(widthAttr) {
				this.oldWidth = parseFloat(widthAttr);
			} else {
				throw "Missing attribute in element " + this.element;
			}
			var heightAttr = this.element.getAttributeNS(null, "height");
			if(heightAttr) {
				this.oldHeight = parseFloat(heightAttr);
			} else {
				throw "Missing attribute in element " + this.element;
			}

		} else if(ORYX.Editor.checkClassType(this.element, SVGCircleElement)) {
			this.type = "Circle";
			
			var cx = undefined;
			var cy = undefined;
			//var r = undefined;

			var cxAttr = this.element.getAttributeNS(null, "cx");
			if(cxAttr) {
				cx = parseFloat(cxAttr);
			} else {
				throw "Missing attribute in element " + this.element;
			}
			var cyAttr = this.element.getAttributeNS(null, "cy");
			if(cyAttr) {
				cy = parseFloat(cyAttr);
			} else {
				throw "Missing attribute in element " + this.element;
			}
			var rAttr = this.element.getAttributeNS(null, "r");
			if(rAttr) {
				//r = parseFloat(rAttr);
				this.radiusX = parseFloat(rAttr);
			} else {
				throw "Missing attribute in element " + this.element;
			}
			this.oldX = cx - this.radiusX;
			this.oldY = cy - this.radiusX;
			this.oldWidth = 2*this.radiusX;
			this.oldHeight = 2*this.radiusX;

		} else if(ORYX.Editor.checkClassType(this.element, SVGEllipseElement)) {
			this.type = "Ellipse";
			
			var cx = undefined;
			var cy = undefined;
			//var rx = undefined;
			//var ry = undefined;
			var cxAttr = this.element.getAttributeNS(null, "cx");
			if(cxAttr) {
				cx = parseFloat(cxAttr);
			} else {
				throw "Missing attribute in element " + this.element;
			}
			var cyAttr = this.element.getAttributeNS(null, "cy");
			if(cyAttr) {
				cy = parseFloat(cyAttr);
			} else {
				throw "Missing attribute in element " + this.element;
			}
			var rxAttr = this.element.getAttributeNS(null, "rx");
			if(rxAttr) {
				this.radiusX = parseFloat(rxAttr);
			} else {
				throw "Missing attribute in element " + this.element;
			}
			var ryAttr = this.element.getAttributeNS(null, "ry");
			if(ryAttr) {
				this.radiusY = parseFloat(ryAttr);
			} else {
				throw "Missing attribute in element " + this.element;
			}
			this.oldX = cx - this.radiusX;
			this.oldY = cy - this.radiusY;
			this.oldWidth = 2*this.radiusX;
			this.oldHeight = 2*this.radiusY;

		} else if(ORYX.Editor.checkClassType(this.element, SVGLineElement)) {
			this.type = "Line";
			
			var x1 = undefined;
			var y1 = undefined;
			var x2 = undefined;
			var y2 = undefined;
			var x1Attr = this.element.getAttributeNS(null, "x1");
			if(x1Attr) {
				x1 = parseFloat(x1Attr);
			} else {
				throw "Missing attribute in element " + this.element;
			}
			var y1Attr = this.element.getAttributeNS(null, "y1");
			if(y1Attr) {
				y1 = parseFloat(y1Attr);
			} else {
				throw "Missing attribute in element " + this.element;
			}
			var x2Attr = this.element.getAttributeNS(null, "x2");
			if(x2Attr) {
				x2 = parseFloat(x2Attr);
			} else {
				throw "Missing attribute in element " + this.element;
			}
			var y2Attr = this.element.getAttributeNS(null, "y2");
			if(y2Attr) {
				y2 = parseFloat(y2Attr);
			} else {
				throw "Missing attribute in element " + this.element;
			}
			this.oldX = Math.min(x1,x2);
			this.oldY = Math.min(y1,y2);
			this.oldWidth = Math.abs(x1-x2);
			this.oldHeight = Math.abs(y1-y2);

		} else if(ORYX.Editor.checkClassType(this.element, SVGPolylineElement) || ORYX.Editor.checkClassType(this.element, SVGPolygonElement)) {
			this.type = "Polyline";
			
			var pointsArray = [];
			if (this.element.points&&this.element.points.numberOfItems){
			    for(var i=0, size=this.element.points.numberOfItems; i<size; i++){
			        pointsArray.push(this.element.points.getItem(i).x)
			        pointsArray.push(this.element.points.getItem(i).y)
			    }
			} else {
				var points = this.element.getAttributeNS(null, "points");
				if(points) {
					points = points.replace(/,/g , " ");
					pointsArray = points.split(" ");
					pointsArray = pointsArray.without("");
				} else {
					throw "Missing attribute in element " + this.element;
				}
			}
			

			if(pointsArray && pointsArray.length && pointsArray.length > 1) {
				var minX = parseFloat(pointsArray[0]);
				var minY = parseFloat(pointsArray[1]);
				var maxX = parseFloat(pointsArray[0]);
				var maxY = parseFloat(pointsArray[1]);

				for(var i = 0; i < pointsArray.length; i++) {
					minX = Math.min(minX, parseFloat(pointsArray[i]));
					maxX = Math.max(maxX, parseFloat(pointsArray[i]));
					i++;
					minY = Math.min(minY, parseFloat(pointsArray[i]));
					maxY = Math.max(maxY, parseFloat(pointsArray[i]));
				}

				this.oldX = minX;
				this.oldY = minY;
				this.oldWidth = maxX-minX;
				this.oldHeight = maxY-minY;
			} else {
				throw "Missing attribute in element " + this.element;
			}

		} else if(ORYX.Editor.checkClassType(this.element, SVGPathElement)) {
			this.type = "Path";
			
			this.editPathParser = new PathParser();
			this.editPathHandler = new ORYX.Core.SVG.EditPathHandler();
			this.editPathParser.setHandler(this.editPathHandler);
		
			var parser = new PathParser();
			var handler = new ORYX.Core.SVG.MinMaxPathHandler();
			parser.setHandler(handler);
			parser.parsePath(this.element);

			this.oldX = handler.minX;
			this.oldY = handler.minY;
			this.oldWidth = handler.maxX - handler.minX;
			this.oldHeight = handler.maxY - handler.minY;

			delete parser;
			delete handler;
		} else {
			throw "Element is not a shape.";
		}

		/** initialize attributes of oryx namespace */
		//resize
		var resizeAttr = this.element.getAttributeNS(NAMESPACE_ORYX, "resize");
		if(resizeAttr) {
			resizeAttr = resizeAttr.toLowerCase();
			if(resizeAttr.match(/horizontal/)) {
				this.isHorizontallyResizable = true;
			} else {
				this.isHorizontallyResizable = false;
			}
			if(resizeAttr.match(/vertical/)) {
				this.isVerticallyResizable = true;
			} else {
				this.isVerticallyResizable = false;
			}
		} else {
			this.isHorizontallyResizable = false;
			this.isVerticallyResizable = false;
		}

		//anchors
		var anchorAttr = this.element.getAttributeNS(NAMESPACE_ORYX, "anchors");
		if(anchorAttr) {
			anchorAttr = anchorAttr.replace("/,/g", " ");
			var anchors = anchorAttr.split(" ").without("");
			
			for(var i = 0; i < anchors.length; i++) {
				switch(anchors[i].toLowerCase()) {
					case "left":
						this.anchorLeft = true;
						break;
					case "right":
						this.anchorRight = true;
						break;
					case "top":
						this.anchorTop = true;
						break;
					case "bottom":
						this.anchorBottom = true;
						break;
				}
			}
		}
		
		//allowDockers and resizeMarkerMid
		if(ORYX.Editor.checkClassType(this.element, SVGPathElement)) {
			var allowDockersAttr = this.element.getAttributeNS(NAMESPACE_ORYX, "allowDockers"); 
			if(allowDockersAttr) {
				if(allowDockersAttr.toLowerCase() === "no") {
					this.allowDockers = false; 
				} else {
					this.allowDockers = true;
				}
			}
			
			var resizeMarkerMidAttr = this.element.getAttributeNS(NAMESPACE_ORYX, "resizeMarker-mid"); 
			if(resizeMarkerMidAttr) {
				if(resizeMarkerMidAttr.toLowerCase() === "yes") {
					this.resizeMarkerMid = true; 
				} else {
					this.resizeMarkerMid = false;
				}
			}
		}	
			
		this.x = this.oldX;
		this.y = this.oldY;
		this.width = this.oldWidth;
		this.height = this.oldHeight;
	},

	/**
	 * Writes the changed values into the SVG element.
	 */
	update: function() {
		
		if(this.x !== this.oldX || this.y !== this.oldY || this.width !== this.oldWidth || this.height !== this.oldHeight) {
			switch(this.type) {
				case "Rect":
					if(this.x !== this.oldX) this.element.setAttributeNS(null, "x", this.x);
					if(this.y !== this.oldY) this.element.setAttributeNS(null, "y", this.y);
				 	if(this.width !== this.oldWidth) this.element.setAttributeNS(null, "width", this.width);
					if(this.height !== this.oldHeight) this.element.setAttributeNS(null, "height", this.height);
					break;
				case "Circle":
					//calculate the radius
					//var r;
//					if(this.width/this.oldWidth <= this.height/this.oldHeight) {
//						this.radiusX = ((this.width > this.height) ? this.width : this.height)/2.0;
//					} else {
					 	this.radiusX = ((this.width < this.height) ? this.width : this.height)/2.0;
					//}
	
					this.element.setAttributeNS(null, "cx", this.x + this.width/2.0);
					this.element.setAttributeNS(null, "cy", this.y + this.height/2.0);
					this.element.setAttributeNS(null, "r", this.radiusX);
					break;
				case "Ellipse":
					this.radiusX = this.width/2;
					this.radiusY = this.height/2;
	
					this.element.setAttributeNS(null, "cx", this.x + this.radiusX);
					this.element.setAttributeNS(null, "cy", this.y + this.radiusY);
					this.element.setAttributeNS(null, "rx", this.radiusX);
					this.element.setAttributeNS(null, "ry", this.radiusY);
					break;
				case "Line":
					if(this.x !== this.oldX)
						this.element.setAttributeNS(null, "x1", this.x);
						
					if(this.y !== this.oldY)
						this.element.setAttributeNS(null, "y1", this.y);
						
					if(this.x !== this.oldX || this.width !== this.oldWidth)
						this.element.setAttributeNS(null, "x2", this.x + this.width);
					
					if(this.y !== this.oldY || this.height !== this.oldHeight)
						this.element.setAttributeNS(null, "y2", this.y + this.height);
					break;
				case "Polyline":
					var points = this.element.getAttributeNS(null, "points");
					if(points) {
						points = points.replace(/,/g, " ").split(" ").without("");
	
						if(points && points.length && points.length > 1) {
	
							//TODO what if oldWidth == 0?
							var widthDelta = (this.oldWidth === 0) ? 0 : this.width / this.oldWidth;
						    var heightDelta = (this.oldHeight === 0) ? 0 : this.height / this.oldHeight;
	
							var updatedPoints = "";
						    for(var i = 0; i < points.length; i++) {
								var x = (parseFloat(points[i])-this.oldX)*widthDelta + this.x;
								i++;
								var y = (parseFloat(points[i])-this.oldY)*heightDelta + this.y;
		    					updatedPoints += x + " " + y + " ";
						    }
							this.element.setAttributeNS(null, "points", updatedPoints);
						} else {
							//TODO error
						}
					} else {
						//TODO error
					}
					break;
				case "Path":
					//calculate scaling delta
					//TODO what if oldWidth == 0?
					var widthDelta = (this.oldWidth === 0) ? 0 : this.width / this.oldWidth;
					var heightDelta = (this.oldHeight === 0) ? 0 : this.height / this.oldHeight;
	
					//use path parser to edit each point of the path
					this.editPathHandler.init(this.x, this.y, this.oldX, this.oldY, widthDelta, heightDelta);
					this.editPathParser.parsePath(this.element);
	
					//change d attribute of path
					this.element.setAttributeNS(null, "d", this.editPathHandler.d);
					break;
			}

			this.oldX = this.x;
			this.oldY = this.y;
			this.oldWidth = this.width;
			this.oldHeight = this.height;
		}
		
		// Remove cached variables
		delete this.visible;
		delete this.handler;
	},
	
	isPointIncluded: function(pointX, pointY) {

		// Check if there are the right arguments and if the node is visible
		if(!pointX || !pointY || !this.isVisible()) {
			return false;
		}

		switch(this.type) {
			case "Rect":
				return (pointX >= this.x && pointX <= this.x + this.width &&
						pointY >= this.y && pointY <= this.y+this.height);
				break;
			case "Circle":
				//calculate the radius
//				var r;
//				if(this.width/this.oldWidth <= this.height/this.oldHeight) {
//					r = ((this.width > this.height) ? this.width : this.height)/2.0;
//				} else {
//				 	r = ((this.width < this.height) ? this.width : this.height)/2.0;
//				}
				return ORYX.Core.Math.isPointInEllipse(pointX, pointY, this.x + this.width/2.0, this.y + this.height/2.0, this.radiusX, this.radiusX);
				break;
			case "Ellipse":
				return ORYX.Core.Math.isPointInEllipse(pointX, pointY, this.x + this.radiusX, this.y + this.radiusY, this.radiusX, this.radiusY);			
				break;
			case "Line":
				return ORYX.Core.Math.isPointInLine(pointX, pointY, this.x, this.y, this.x + this.width, this.y + this.height);
				break;
			case "Polyline":
				var points = this.element.getAttributeNS(null, "points");
	
				if(points) {
					points = points.replace(/,/g , " ").split(" ").without("");
	
					points = points.collect(function(n) {
						return parseFloat(n);
					});
					
					return ORYX.Core.Math.isPointInPolygone(pointX, pointY, points);
				} else {
					return false;
				}
				break;
			case "Path":
				
				// Cache Path handler
				if (!this.handler) {
					var parser = new PathParser();
					this.handler = new ORYX.Core.SVG.PointsPathHandler();
					parser.setHandler(this.handler);
					parser.parsePath(this.element);
				}
				
				return ORYX.Core.Math.isPointInPolygone(pointX, pointY, this.handler.points);

				break;
			default:
				return false;
		}
	},

	/**
	 * Returns true if the element is visible
	 * @param {SVGElement} elem
	 * @return boolean
	 */
	isVisible: function(elem) {
		
		if (this.visible !== undefined){
			return this.visible;
		}
			
		if (!elem) {
			elem = this.element;
		}

		var hasOwnerSVG = false;
		try { 
			hasOwnerSVG = !!elem.ownerSVGElement;
		} catch(e){}
		
		// Is SVG context
		if ( hasOwnerSVG ) {
			// IF G-Element
			if (ORYX.Editor.checkClassType(elem, SVGGElement)) {
				if (elem.className && elem.className.baseVal == "me") {
					this.visible = true;
					return this.visible;
				}
			}

			// Check if fill or stroke is set
			var fill = elem.getAttributeNS(null, "fill");
			var stroke = elem.getAttributeNS(null, "stroke");
			if (fill && fill == "none" && stroke && stroke == "none") {
				this.visible = false;
			} else {
				// Check if displayed
				var attr = elem.getAttributeNS(null, "display");
				if(!attr)
					this.visible = this.isVisible(elem.parentNode);
				else if (attr == "none") 
					this.visible = false;
				else
					this.visible = true;
			}
		} else {
			this.visible = true;
		}
		
		return this.visible;
	},

	toString: function() { return (this.element) ? "SVGShape " + this.element.id : "SVGShape " + this.element;}
 });