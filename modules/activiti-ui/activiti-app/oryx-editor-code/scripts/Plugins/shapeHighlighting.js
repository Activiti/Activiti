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

ORYX.Plugins.ShapeHighlighting = Clazz.extend({

	construct: function(facade) {
		
		this.parentNode = facade.getCanvas().getSvgContainer();
		
		// The parent Node
		this.node = ORYX.Editor.graft("http://www.w3.org/2000/svg", this.parentNode,
					['g']);

		this.highlightNodes = {};
		
		facade.registerOnEvent(ORYX.CONFIG.EVENT_HIGHLIGHT_SHOW, this.setHighlight.bind(this));
		facade.registerOnEvent(ORYX.CONFIG.EVENT_HIGHLIGHT_HIDE, this.hideHighlight.bind(this));		

	},

	setHighlight: function(options) {
		if(options && options.highlightId){
			var node = this.highlightNodes[options.highlightId];
			
			if(!node){
				node= ORYX.Editor.graft("http://www.w3.org/2000/svg", this.node,
					['path', {
						"stroke-width": 2.0, "fill":"none"
						}]);	
			
				this.highlightNodes[options.highlightId] = node;
			}

			if(options.elements && options.elements.length > 0) {
				
				this.setAttributesByStyle( node, options );
				this.show(node);
			
			} else {
			
				this.hide(node);			
			
			}
			
		}
	},
	
	hideHighlight: function(options) {
		if(options && options.highlightId && this.highlightNodes[options.highlightId]){
			this.hide(this.highlightNodes[options.highlightId]);
		}		
	},
	
	hide: function(node) {
		node.setAttributeNS(null, 'display', 'none');
	},

	show: function(node) {
		node.setAttributeNS(null, 'display', '');
	},
	
	setAttributesByStyle: function( node, options ){
		
		// If the style say, that it should look like a rectangle
		if( options.style && options.style == ORYX.CONFIG.SELECTION_HIGHLIGHT_STYLE_RECTANGLE ){
			
			// Set like this
			var bo = options.elements[0].absoluteBounds();
			
			var strWidth = options.strokewidth ? options.strokewidth 	: ORYX.CONFIG.BORDER_OFFSET
			
			node.setAttributeNS(null, "d", this.getPathRectangle( bo.a, bo.b , strWidth ) );
			node.setAttributeNS(null, "stroke", 		options.color 		? options.color 		: ORYX.CONFIG.SELECTION_HIGHLIGHT_COLOR);
			node.setAttributeNS(null, "stroke-opacity", options.opacity 	? options.opacity 		: 0.2);
			node.setAttributeNS(null, "stroke-width", 	strWidth);
						
		} else if(options.elements.length == 1 
					&& options.elements[0] instanceof ORYX.Core.Edge &&
					options.highlightId != "selection") {
			
			/* Highlight containment of edge's childs */
			var path = this.getPathEdge(options.elements[0].dockers);
			if (path && path.length > 0)
			{
				node.setAttributeNS(null, "d", path);
			}
			node.setAttributeNS(null, "stroke", options.color ? options.color : ORYX.CONFIG.SELECTION_HIGHLIGHT_COLOR);
			node.setAttributeNS(null, "stroke-opacity", options.opacity ? options.opacity : 0.2);
			node.setAttributeNS(null, "stroke-width", 	ORYX.CONFIG.OFFSET_EDGE_BOUNDS);
			
		}else {
			// If not, set just the corners
			var path = this.getPathByElements(options.elements);
			if (path && path.length > 0)
			{
				node.setAttributeNS(null, "d", path);
			}
			node.setAttributeNS(null, "stroke", options.color ? options.color : ORYX.CONFIG.SELECTION_HIGHLIGHT_COLOR);
			node.setAttributeNS(null, "stroke-opacity", options.opacity ? options.opacity : 1.0);
			node.setAttributeNS(null, "stroke-width", 	options.strokewidth ? options.strokewidth 	: 2.0);
						
		}
	},
	
	getPathByElements: function(elements){
		if(!elements || elements.length <= 0) {return undefined}
		
		// Get the padding and the size
		var padding = ORYX.CONFIG.SELECTED_AREA_PADDING;
		
		var path = ""
		
		// Get thru all Elements
		elements.each((function(element) {
			if(!element) {return}
			// Get the absolute Bounds and the two Points
			var bounds = element.absoluteBounds();
			bounds.widen(padding)
			var a = bounds.upperLeft();
			var b = bounds.lowerRight();
			
			path = path + this.getPath(a ,b);
												
		}).bind(this));

		return path;
		
	},

	getPath: function(a, b){
				
		return this.getPathCorners(a, b);
	
	},
			
	getPathCorners: function(a, b){

		var size = ORYX.CONFIG.SELECTION_HIGHLIGHT_SIZE;
				
		var path = ""

		// Set: Upper left 
		path = path + "M" + a.x + " " + (a.y + size) + " l0 -" + size + " l" + size + " 0 ";
		// Set: Lower left
		path = path + "M" + a.x + " " + (b.y - size) + " l0 " + size + " l" + size + " 0 ";
		// Set: Lower right
		path = path + "M" + b.x + " " + (b.y - size) + " l0 " + size + " l-" + size + " 0 ";
		// Set: Upper right
		path = path + "M" + b.x + " " + (a.y + size) + " l0 -" + size + " l-" + size + " 0 ";
		
		return path;
	},
	
	getPathRectangle: function(a, b, strokeWidth){

		var size = ORYX.CONFIG.SELECTION_HIGHLIGHT_SIZE;

		var path 	= ""
		var offset 	= strokeWidth / 2.0;
		 
		// Set: Upper left 
		path = path + "M" + (a.x + offset) + " " + (a.y);
		path = path + " L" + (a.x + offset) + " " + (b.y - offset);
		path = path + " L" + (b.x - offset) + " " + (b.y - offset);
		path = path + " L" + (b.x - offset) + " " + (a.y + offset);
		path = path + " L" + (a.x + offset) + " " + (a.y + offset);

		return path;
	},
	
	getPathEdge: function(edgeDockers) {
		var length = edgeDockers.length;
		var path = "M" + edgeDockers[0].bounds.center().x + " " 
					+  edgeDockers[0].bounds.center().y;
		
		for(i=1; i<length; i++) {
			var dockerPoint = edgeDockers[i].bounds.center();
			path = path + " L" + dockerPoint.x + " " +  dockerPoint.y;
		}
		
		return path;
	}
	
});

 
ORYX.Plugins.HighlightingSelectedShapes = Clazz.extend({

	construct: function(facade) {
		this.facade = facade;
		this.opacityFull = 0.9;
		this.opacityLow = 0.4;

		// Register on Dragging-Events for show/hide of ShapeMenu
		//this.facade.registerOnEvent(ORYX.CONFIG.EVENT_DRAGDROP_START, this.hide.bind(this));
		//this.facade.registerOnEvent(ORYX.CONFIG.EVENT_DRAGDROP_END,  this.show.bind(this));		
	},

	/**
	 * On the Selection-Changed
	 *
	 */
	onSelectionChanged: function(event) {
		if(event.elements && event.elements.length > 1) {
			this.facade.raiseEvent({
										type:		ORYX.CONFIG.EVENT_HIGHLIGHT_SHOW, 
										highlightId:'selection',
										elements:	event.elements.without(event.subSelection),
										color:		ORYX.CONFIG.SELECTION_HIGHLIGHT_COLOR,
										opacity: 	!event.subSelection ? this.opacityFull : this.opacityLow
									});

			if(event.subSelection){
				this.facade.raiseEvent({
											type:		ORYX.CONFIG.EVENT_HIGHLIGHT_SHOW, 
											highlightId:'subselection',
											elements:	[event.subSelection],
											color:		ORYX.CONFIG.SELECTION_HIGHLIGHT_COLOR,
											opacity: 	this.opacityFull
										});	
			} else {
				this.facade.raiseEvent({type:ORYX.CONFIG.EVENT_HIGHLIGHT_HIDE, highlightId:'subselection'});				
			}						
			
		} else {
			this.facade.raiseEvent({type:ORYX.CONFIG.EVENT_HIGHLIGHT_HIDE, highlightId:'selection'});
			this.facade.raiseEvent({type:ORYX.CONFIG.EVENT_HIGHLIGHT_HIDE, highlightId:'subselection'});
		}		
	}
});