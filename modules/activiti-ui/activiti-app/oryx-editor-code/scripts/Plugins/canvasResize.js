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
if (!ORYX.Plugins) {
    ORYX.Plugins = new Object();
}



/**
 * This plugin is responsible for resizing the canvas.
 * @param {Object} facade The editor plugin facade to register enhancements with.
 */
ORYX.Plugins.CanvasResize = Clazz.extend({

    construct: function(facade){
		
        this.facade = facade;
		new ORYX.Plugins.CanvasResizeButton( this.facade.getCanvas(), "N", this.resize.bind(this));
		new ORYX.Plugins.CanvasResizeButton( this.facade.getCanvas(), "W", this.resize.bind(this));
		new ORYX.Plugins.CanvasResizeButton( this.facade.getCanvas(), "E", this.resize.bind(this));
		new ORYX.Plugins.CanvasResizeButton( this.facade.getCanvas(), "S", this.resize.bind(this));
		
		window.setTimeout(function(){jQuery(window).trigger('resize');});

    },
    
    resize: function( position, shrink ){
    	
    	resizeCanvas = function(position, extentionSize, facade) {
        	var canvas 		= facade.getCanvas();
    		var b 			= canvas.bounds;
    		var scrollNode 	= facade.getCanvas().getHTMLContainer().parentNode.parentNode;
    		
    		if( position == "E" || position == "W"){
    			canvas.setSize({width: (b.width() + extentionSize)*canvas.zoomLevel, height: (b.height())*canvas.zoomLevel})

    		} else if( position == "S" || position == "N"){
    			canvas.setSize({width: (b.width())*canvas.zoomLevel, height: (b.height() + extentionSize)*canvas.zoomLevel})
    		}

    		if( position == "N" || position == "W"){
    			
    			var move = position == "N" ? {x: 0, y: extentionSize}: {x: extentionSize, y: 0 };

    			// Move all children
    			canvas.getChildNodes(false, function(shape){ shape.bounds.moveBy(move) })
    			// Move all dockers, when the edge has at least one docked shape
    			var edges = canvas.getChildEdges().findAll(function(edge){ return edge.getAllDockedShapes().length > 0})
    			var dockers = edges.collect(function(edge){ return edge.dockers.findAll(function(docker){ return !docker.getDockedShape() })}).flatten();
    			dockers.each(function(docker){ docker.bounds.moveBy(move)})
    		} else if( position == "S" ){
    			scrollNode.scrollTop += extentionSize;
    		} else if( position == "E" ){
    			scrollNode.scrollLeft += extentionSize;
    		}
    		
    		jQuery(window).trigger('resize');
    		
    		canvas.update();
    		facade.updateSelection();
        }
		
		var commandClass = ORYX.Core.Command.extend({
			construct: function(position, extentionSize, facade){
				this.position = position;
				this.extentionSize = extentionSize;
				this.facade = facade;
			},			
			execute: function(){
				resizeCanvas(this.position, this.extentionSize, this.facade);
			},
			rollback: function(){
				resizeCanvas(this.position, -this.extentionSize, this.facade);
			},
			update:function(){
			}
		});
		
		var extentionSize = ORYX.CONFIG.CANVAS_RESIZE_INTERVAL;
		if(shrink) extentionSize = -extentionSize;
		var command = new commandClass(position, extentionSize, this.facade);
		
		this.facade.executeCommands([command]);
			
    }
    
});


ORYX.Plugins.CanvasResizeButton = Clazz.extend({
	
	construct: function(canvas, position, callback){
		this.canvas = canvas;
		var parentNode = canvas.getHTMLContainer().parentNode;
		
		window.myParent=parentNode;
			
		var actualScrollNode = jQuery('#canvasSection')[0];
		var scrollNode 	= actualScrollNode;
		var canvasNode = jQuery('#canvasSection').find(".ORYX_Editor")[0];
		var svgRootNode = canvasNode.children[0];
		
		var iconClass = 'glyphicon glyphicon-chevron-';
		var iconClassShrink = 'glyphicon glyphicon-chevron-';
		if(position == 'N') {
			iconClass += 'up';
			iconClassShrink += 'down';
		} else if(position == 'S') {
			iconClass += 'down';
			iconClassShrink += 'up';
		} else if(position == 'E') {
			iconClass += 'right';
			iconClassShrink += 'left';
		} else if(position == 'W') {
			iconClass += 'left';
			iconClassShrink += 'right';
		}
		
		// The buttons
		var idGrow = 'canvas-shrink-' + position;
		var idShrink = 'canvas-grow-' + position;
		
		var buttonGrow 	= ORYX.Editor.graft("http://www.w3.org/1999/xhtml", parentNode, ['div', {'class': 'canvas_resize_indicator canvas_resize_indicator_grow' + ' ' + position, 'id': idGrow ,'title':ORYX.I18N.RESIZE.tipGrow+ORYX.I18N.RESIZE[position]},
             ['i', {'class' : iconClass}]
		]);
		var buttonShrink 	= ORYX.Editor.graft("http://www.w3.org/1999/xhtml", parentNode, ['div', {'class': 'canvas_resize_indicator canvas_resize_indicator_shrink' + ' ' + position, 'id': idShrink ,'title':ORYX.I18N.RESIZE.tipGrow+ORYX.I18N.RESIZE[position]},
             ['i', {'class' : iconClassShrink}]
		]);
		// Defines a callback which gives back
		// a boolean if the current mouse event 
		// is over the particular button area
		var offSetWidth = 60;
		var isOverOffset = function(event) {
			
			var isOverButton = event.target.id.indexOf("canvas-shrink") != -1
				|| event.target.id.indexOf("canvas-grow") != -1
				|| event.target.parentNode.id.indexOf("canvas-shrink") != -1
				|| event.target.parentNode.id.indexOf("canvas-grow") != -1;
			if(isOverButton) {
				if(event.target.id == idGrow || event.target.id == idShrink || 
						event.target.parentNode.id == idGrow || event.target.parentNode.id == idShrink ) {
					return true;
				} else {
					return false;
				}
			}
			
			if(event.target!=parentNode && event.target!=scrollNode&& event.target!=scrollNode.firstChild&& event.target!=svgRootNode&& event.target!=scrollNode){ return false; }
			
			//if(inCanvas){offSetWidth=30}else{offSetWidth=30*2}
			//Safari work around
			var X=event.offsetX !== undefined ? event.offsetX : event.layerX;
			var Y=event.offsetY !== undefined ? event.offsetY : event.layerY;
			
			var canvasOffset = 0;
			if(canvasNode.clientWidth < actualScrollNode.clientWidth) {
              	var widthDiff = actualScrollNode.clientWidth -  canvasNode.clientWidth;
              	canvasOffset = widthDiff / 2;
            }
			
				// Adjust to relative location to the actual viewport
				Y = Y - actualScrollNode.scrollTop;
				X = X - actualScrollNode.scrollLeft;
			
			
			if(position == "N"){
				return  Y < offSetWidth;
			} else if(position == "W"){
				return X < offSetWidth + canvasOffset;
			} else if(position == "E"){
				return actualScrollNode.clientWidth - X < offSetWidth + canvasOffset;
			} else if(position == "S"){
				return actualScrollNode.clientHeight - Y < offSetWidth;
			}
			
			return false;
		};
		
		var showButtons = (function() {
			buttonGrow.show(); 
        
			var w = canvas.bounds.width();
			var h = canvas.bounds.height();
        
			if(position=="N" && (h - ORYX.CONFIG.CANVAS_RESIZE_INTERVAL > ORYX.CONFIG.CANVAS_MIN_HEIGHT)) buttonShrink.show();
			else if(position=="E" && (w - ORYX.CONFIG.CANVAS_RESIZE_INTERVAL > ORYX.CONFIG.CANVAS_MIN_WIDTH)) buttonShrink.show();
			else if(position=="S" && (h - ORYX.CONFIG.CANVAS_RESIZE_INTERVAL > ORYX.CONFIG.CANVAS_MIN_HEIGHT)) buttonShrink.show();
			else if(position=="W" && (w - ORYX.CONFIG.CANVAS_RESIZE_INTERVAL > ORYX.CONFIG.CANVAS_MIN_WIDTH)) buttonShrink.show();
			else buttonShrink.hide();
			

		}).bind(this);
        
		var hideButtons = function() {
			buttonGrow.hide(); 
			buttonShrink.hide();
		};
        
		// If the mouse move is over the button area, show the button
		parentNode.parentNode.addEventListener(	ORYX.CONFIG.EVENT_MOUSEMOVE, 	function(event){ if( isOverOffset(event) ){showButtons();} else {hideButtons()}} , false );
		// If the mouse is over the button, show them
		buttonGrow.addEventListener(		ORYX.CONFIG.EVENT_MOUSEOVER, 	function(event){showButtons();}, true );
		buttonShrink.addEventListener(		ORYX.CONFIG.EVENT_MOUSEOVER, 	function(event){showButtons();}, true );
		// If the mouse is out, hide the button
		//scrollNode.addEventListener(		ORYX.CONFIG.EVENT_MOUSEOUT, 	function(event){button.hide()}, true )
		parentNode.parentNode.addEventListener(	ORYX.CONFIG.EVENT_MOUSEOUT, 	function(event){hideButtons()} , true );
		//svgRootNode.addEventListener(	ORYX.CONFIG.EVENT_MOUSEOUT, 	function(event){ inCanvas = false } , true );
        
		// Hide the button initialy
		hideButtons();

		// Add the callbacks
	    buttonGrow.addEventListener('click', function(){callback( position ); showButtons();}, true);
	    buttonShrink.addEventListener('click', function(){callback( position, true ); showButtons();}, true);

	}
	

});

