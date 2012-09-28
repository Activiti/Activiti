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

/**
 * Init namespaces
 */
if(!ORYX) {var ORYX = {};}
if(!ORYX.Core) {ORYX.Core = {};}
if(!ORYX.Core.Controls) {ORYX.Core.Controls = {};}


/**
 * @classDescription Represents a magnet that is part of another shape and can
 * be attached to dockers. Magnets are used for linking edge objects
 * to other Shape objects.
 * @extends {Control}
 */
ORYX.Core.Controls.Magnet = ORYX.Core.Controls.Control.extend({
		
	/**
	 * Constructor
	 */
	construct: function() {
		arguments.callee.$.construct.apply(this, arguments);
		
		//this.anchors = [];
		this.anchorLeft;
		this.anchorRight;
		this.anchorTop;
		this.anchorBottom;
		
		this.bounds.set(0, 0, 16, 16);
		
		//graft magnet's root node into owner's control group.
		this.node = ORYX.Editor.graft("http://www.w3.org/2000/svg",
			null,
			['g', {"pointer-events":"all"},
					['circle', {cx:"8", cy:"8", r:"4", stroke:"none", fill:"red", "fill-opacity":"0.3"}],
				]);
			
		this.hide();
	},
	
	update: function() {
		arguments.callee.$.update.apply(this, arguments);
		
		//this.isChanged = true;
	},
	
	_update: function() {		
		arguments.callee.$.update.apply(this, arguments);
		
		//this.isChanged = true;
	},
	
	refresh: function() {
		arguments.callee.$.refresh.apply(this, arguments);

		var p = this.bounds.upperLeft();
		/*if(this.parent) {
			var parentPos = this.parent.bounds.upperLeft();
			p.x += parentPos.x;
			p.y += parentPos.y;
		}*/
		
		this.node.setAttributeNS(null, 'transform','translate(' + p.x + ', ' + p.y + ')');
	},
	
	show: function() {
		//this.refresh();
		arguments.callee.$.show.apply(this, arguments);
	},
	
	toString: function() {
		return "Magnet " + this.id;
	}
});
