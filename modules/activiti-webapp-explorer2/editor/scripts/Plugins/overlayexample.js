
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
if (!ORYX.Plugins) 
    ORYX.Plugins = new Object();

ORYX.Plugins.OverlayExample = Clazz.extend({

    facade: undefined,
    
    construct: function(facade){
		
        this.facade = facade;
        
		this.active = false;
		this.el 	= undefined;
		this.callback = undefined;
		
        this.facade.offer({
            'name': "Overlay Test",
            'functionality': this.testing.bind(this),
            'group': "Overlay",
            'icon': ORYX.PATH + "images/disk.png",
            'description': "Overlay Test",
            'index': 1,
            'minShape': 0,
            'maxShape': 0
        });
		
    },
    
	testing: function(){

		if (this.active) {
			
			this.facade.raiseEvent({
				type: ORYX.CONFIG.EVENT_OVERLAY_HIDE,
				id: "overlaytest.test"
			});
			
		} else {
			
			var els = this.facade.getCanvas().getChildShapes(true);
			
			this.el = els[0]
			this.showOverlay( this.el )
			
		}
		
		this.active = !this.active;	
		
		if( this.active ){
			this.callback = this.doMouseUp.bind(this)
			this.facade.registerOnEvent(ORYX.CONFIG.EVENT_MOUSEUP, this.callback)
		} else {
			this.facade.unregisterOnEvent(ORYX.CONFIG.EVENT_MOUSEUP, this.callback)
			this.callback = undefined;
		}				
		
	},
	
	doMouseUp: function(event, arg){
		if( arg instanceof ORYX.Core.Shape && arg != this.el){
			this.el = arg;
			this.showOverlay( this.el )
		}
	},
	
	showOverlay: function(shape){

		var cross = ORYX.Editor.graft("http://www.w3.org/2000/svg", null ,
					['path', {
						"stroke-width": 5.0, "stroke":"red", "d":  "M0,0 L-15,-15 M-15,0 L0,-15", "line-captions": "round"
						}]);
							
		this.facade.raiseEvent({
				type: 			ORYX.CONFIG.EVENT_OVERLAY_SHOW,
				id: 			"overlaytest.test",
				shapes: 		[shape],
				attributes: 	{fill: "red", stroke:"green", "stroke-width":4},
				node:			cross,
				nodePosition:	shape instanceof ORYX.Core.Edge ? "START" : "NE"
			});
					
	}
	
    
});
