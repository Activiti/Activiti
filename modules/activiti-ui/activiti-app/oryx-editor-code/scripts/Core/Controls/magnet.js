/*
 * Activiti app component part of the Activiti project
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */


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
