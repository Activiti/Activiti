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
