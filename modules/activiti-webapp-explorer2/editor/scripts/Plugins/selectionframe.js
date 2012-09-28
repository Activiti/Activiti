/**
 * Copyright (c) 2006
 * Martin Czuchra, Nicolas Peters, Daniel Polak, Willi Tscheschner
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 **/

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
		this.offsetPosition = {x: 0, y: 0}

		// (Un)Register Mouse-Move Event
		this.moveCallback 	= undefined;
		this.offsetScroll	= {x:0,y:0}
		// HTML-Node of Selection-Frame
		this.node = ORYX.Editor.graft("http://www.w3.org/1999/xhtml", this.facade.getCanvas().getHTMLContainer(),
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
			}

			// Set the new Position
			this.setPos({x: Event.pointerX(event)-this.offsetPosition.x, y:Event.pointerY(event)-this.offsetPosition.y});
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
			}

			var b = {
				x: a.x + Math.abs(this.size.width),
				y: a.y + Math.abs(this.size.height)
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
				return false
			});

			// Set the selection
			this.facade.setSelection(elements);
		}
	},

	handleMouseMove: function(event) {
		// Calculate the size
		var size = {
			width	: Event.pointerX(event) - this.position.x - this.offsetPosition.x,
			height	: Event.pointerY(event) - this.position.y - this.offsetPosition.y,
		}

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


