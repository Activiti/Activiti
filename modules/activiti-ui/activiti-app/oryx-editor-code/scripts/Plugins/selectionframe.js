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

 ORYX.Plugins.SelectionFrame = Clazz.extend({

	construct: function(facade) {
		this.facade = facade;

		// Register on MouseEvents
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_MOUSEDOWN, this.handleMouseDown.bind(this));
		document.documentElement.addEventListener(ORYX.CONFIG.EVENT_MOUSEUP, this.handleMouseUp.bind(this), true);

		// Some initiale variables
		this.position 		= {x:0, y:0};
		this.size 			= {width:0, height:0};
		this.offsetPosition = {x: 0, y: 0};

		// (Un)Register Mouse-Move Event
		this.moveCallback 	= undefined;
		this.offsetScroll	= {x:0,y:0};
		// HTML-Node of Selection-Frame
		this.node = ORYX.Editor.graft("http://www.w3.org/1999/xhtml", $('canvasSection'),
			['div', {'class':'Oryx_SelectionFrame'}]);

		this.hide();
	},

	handleMouseDown: function(event, uiObj) {
		// If there is the Canvas
		if( uiObj instanceof ORYX.Core.Canvas ) {
			// Calculate the Offset
			var scrollNode = uiObj.rootNode.parentNode.parentNode;
						
			var a = this.facade.getCanvas().node.getScreenCTM();
			this.offsetPosition = {
				x: a.e,
				y: a.f
			};

			// Set the new Position
			this.setPos({
			    x: Event.pointerX(event) - jQuery("#canvasSection").offset().left, 
				y: Event.pointerY(event) - jQuery("#canvasSection").offset().top + 5
			});
			
			// Reset the size
			this.resize({width:0, height:0});
			this.moveCallback = this.handleMouseMove.bind(this);
		
			// Register Mouse-Move Event
			document.documentElement.addEventListener(ORYX.CONFIG.EVENT_MOUSEMOVE, this.moveCallback, false);

			this.offsetScroll		= {x:scrollNode.scrollLeft,y:scrollNode.scrollTop};
			
			// Show the Frame
			this.show();
		}

		Event.stop(event);
	},

	handleMouseUp: function(event) {
		// If there was an MouseMoving
		if(this.moveCallback) {
			// Hide the Frame
			this.hide();

			// Unregister Mouse-Move
			document.documentElement.removeEventListener(ORYX.CONFIG.EVENT_MOUSEMOVE, this.moveCallback, false);			
		
			this.moveCallback = undefined;

			var corrSVG = this.facade.getCanvas().node.getScreenCTM();

			// Calculate the positions of the Frame
			var a = {
				x: this.size.width > 0 ? this.position.x : this.position.x + this.size.width,
				y: this.size.height > 0 ? this.position.y : this.position.y + this.size.height
			};

			var b = {
				x: a.x + Math.abs(this.size.width),
				y: a.y + Math.abs(this.size.height)
			};
			
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
            
            if (additionalIEZoom === 1) {
                a.x = a.x - (corrSVG.e - jQuery("#canvasSection").offset().left);
                a.y = a.y - (corrSVG.f - jQuery("#canvasSection").offset().top);
                b.x = b.x - (corrSVG.e - jQuery("#canvasSection").offset().left);
                b.y = b.y - (corrSVG.f - jQuery("#canvasSection").offset().top);
            
            } else {
                 var canvasOffsetLeft = jQuery("#canvasSection").offset().left;
                 var canvasScrollLeft = jQuery("#canvasSection").scrollLeft();
                 var canvasScrollTop = jQuery("#canvasSection").scrollTop();
                 
                 var offset = a.e - (canvasOffsetLeft * additionalIEZoom);
                 var additionaloffset = 0;
                 if (offset > 10) {
                     additionaloffset = (offset / additionalIEZoom) - offset;
                 }
                 
                 a.x = a.x - (corrSVG.e - (canvasOffsetLeft * additionalIEZoom) + additionaloffset + ((canvasScrollLeft * additionalIEZoom) - canvasScrollLeft));
                 a.y = a.y - (corrSVG.f - (jQuery("#canvasSection").offset().top * additionalIEZoom) + ((canvasScrollTop * additionalIEZoom) - canvasScrollTop));
                 b.x = b.x - (corrSVG.e - (canvasOffsetLeft * additionalIEZoom) + additionaloffset + ((canvasScrollLeft * additionalIEZoom) - canvasScrollLeft));
                 b.y = b.y - (corrSVG.f - (jQuery("#canvasSection").offset().top * additionalIEZoom) + ((canvasScrollTop * additionalIEZoom) - canvasScrollTop));
            }
			
			

			// Fit to SVG-Coordinates
			a.x /= corrSVG.a; a.y /= corrSVG.d;
			b.x /= corrSVG.a; b.y /= corrSVG.d;
			
			// Calculate the elements from the childs of the canvas
			var elements = this.facade.getCanvas().getChildShapes(true).findAll(function(value) {
				var absBounds = value.absoluteBounds();
				
				var bA = absBounds.upperLeft();
				var bB = absBounds.lowerRight();
				
				if(bA.x > a.x && bA.y > a.y && bB.x < b.x && bB.y < b.y)
					return true;
				return false;
			});

			// Set the selection
			this.facade.setSelection(elements);
		}
	},

	handleMouseMove: function(event) {
		// Calculate the size
		var size = {
			width	: Event.pointerX(event) - this.position.x - jQuery("#canvasSection").offset().left,
			height	: Event.pointerY(event) - this.position.y - jQuery("#canvasSection").offset().top + 5
		};

		var scrollNode 	= this.facade.getCanvas().rootNode.parentNode.parentNode;
		size.width 		-= this.offsetScroll.x - scrollNode.scrollLeft; 
		size.height 	-= this.offsetScroll.y - scrollNode.scrollTop;
						
		// Set the size
		this.resize(size);

		Event.stop(event);
	},

	hide: function() {
		this.node.style.display = "none";
	},

	show: function() {
		this.node.style.display = "";
	},

	setPos: function(pos) {
		// Set the Position
		this.node.style.top = pos.y + "px";
		this.node.style.left = pos.x + "px";
		this.position = pos;
	},

	resize: function(size) {

		// Calculate the negative offset
		this.setPos(this.position);
		this.size = Object.clone(size);
		
		if(size.width < 0) {
			this.node.style.left = (this.position.x + size.width) + "px";
			size.width = - size.width;
		}
		if(size.height < 0) {
			this.node.style.top = (this.position.y + size.height) + "px";
			size.height = - size.height;
		}

		// Set the size
		this.node.style.width = size.width + "px";
		this.node.style.height = size.height + "px";
	}

});


