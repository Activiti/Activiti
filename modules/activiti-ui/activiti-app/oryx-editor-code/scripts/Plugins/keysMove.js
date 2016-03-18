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

ORYX.Plugins.KeysMove = ORYX.Plugins.AbstractPlugin.extend({

    facade: undefined,
    
    construct: function(facade){
    
        this.facade = facade;
        this.copyElements = [];
        
        //this.facade.registerOnEvent(ORYX.CONFIG.EVENT_KEYDOWN, this.keyHandler.bind(this));

		// SELECT ALL
		this.facade.offer({
		keyCodes: [{
		 		metaKeys: [ORYX.CONFIG.META_KEY_META_CTRL],
				keyCode: 65,
				keyAction: ORYX.CONFIG.KEY_ACTION_DOWN 
			}
		 ],
         functionality: this.selectAll.bind(this)
         });
		 
		// MOVE LEFT SMALL		
		this.facade.offer({
		keyCodes: [{
		 		metaKeys: [ORYX.CONFIG.META_KEY_META_CTRL],
				keyCode: ORYX.CONFIG.KEY_CODE_LEFT,
				keyAction: ORYX.CONFIG.KEY_ACTION_DOWN 
			}
		 ],
         functionality: this.move.bind(this, ORYX.CONFIG.KEY_CODE_LEFT, false)
         });
		 
		 // MOVE LEFT
		 this.facade.offer({
		 keyCodes: [{
				keyCode: ORYX.CONFIG.KEY_CODE_LEFT,
				keyAction: ORYX.CONFIG.KEY_ACTION_DOWN 
			}
		 ],
         functionality: this.move.bind(this, ORYX.CONFIG.KEY_CODE_LEFT, true)
         });
		 
		// MOVE RIGHT SMALL	
		 this.facade.offer({
		 keyCodes: [{
		 		metaKeys: [ORYX.CONFIG.META_KEY_META_CTRL],
				keyCode: ORYX.CONFIG.KEY_CODE_RIGHT,
				keyAction: ORYX.CONFIG.KEY_ACTION_DOWN 
			}
		 ],
         functionality: this.move.bind(this, ORYX.CONFIG.KEY_CODE_RIGHT, false)
         });
		 
		// MOVE RIGHT	
		 this.facade.offer({
		 keyCodes: [{
				keyCode: ORYX.CONFIG.KEY_CODE_RIGHT,
				keyAction: ORYX.CONFIG.KEY_ACTION_DOWN 
			}
		 ],
         functionality: this.move.bind(this, ORYX.CONFIG.KEY_CODE_RIGHT, true)
         });
		 
		// MOVE UP SMALL	
		 this.facade.offer({
		 keyCodes: [{
		 		metaKeys: [ORYX.CONFIG.META_KEY_META_CTRL],
				keyCode: ORYX.CONFIG.KEY_CODE_UP,
				keyAction: ORYX.CONFIG.KEY_ACTION_DOWN 
			}
		 ],
         functionality: this.move.bind(this, ORYX.CONFIG.KEY_CODE_UP, false)
         });
		 
		// MOVE UP	
		 this.facade.offer({
		 keyCodes: [{
				keyCode: ORYX.CONFIG.KEY_CODE_UP,
				keyAction: ORYX.CONFIG.KEY_ACTION_DOWN 
			}
		 ],
         functionality: this.move.bind(this, ORYX.CONFIG.KEY_CODE_UP, true)
         });
		 
		// MOVE DOWN SMALL	
		 this.facade.offer({
		 keyCodes: [{
		 		metaKeys: [ORYX.CONFIG.META_KEY_META_CTRL],
				keyCode: ORYX.CONFIG.KEY_CODE_DOWN,
				keyAction: ORYX.CONFIG.KEY_ACTION_DOWN 
			}
		 ],
         functionality: this.move.bind(this, ORYX.CONFIG.KEY_CODE_DOWN, false)
         });
		 
		// MOVE DOWN	
		 this.facade.offer({
		 keyCodes: [{
				keyCode: ORYX.CONFIG.KEY_CODE_DOWN,
				keyAction: ORYX.CONFIG.KEY_ACTION_DOWN 
			}
		 ],
         functionality: this.move.bind(this, ORYX.CONFIG.KEY_CODE_DOWN, true)
         });
		 
         
    },
    
	/**
	 * Select all shapes in the editor
	 *
	 */
	selectAll: function(e){
    	Event.stop(e.event);
		this.facade.setSelection(this.facade.getCanvas().getChildShapes(true))
	},
	
	move: function(key, far, e) {
		
    	Event.stop(e.event);

		// calculate the distance to move the objects and get the selection.
		var distance = far? 20 : 5;
		var selection = this.facade.getSelection();
		var currentSelection = this.facade.getSelection();
		var p = {x: 0, y: 0};
		
		// switch on the key pressed and populate the point to move by.
		switch(key) {

			case ORYX.CONFIG.KEY_CODE_LEFT:
				p.x = -1*distance;
				break;
			case ORYX.CONFIG.KEY_CODE_RIGHT:
				p.x = distance;
				break;
			case ORYX.CONFIG.KEY_CODE_UP:
				p.y = -1*distance;
				break;
			case ORYX.CONFIG.KEY_CODE_DOWN:
				p.y = distance;
				break;
		}
		
		// move each shape in the selection by the point calculated and update it.
		selection = selection.findAll(function(shape){ 
			// Check if this shape is docked to an shape in the selection			
			if(shape instanceof ORYX.Core.Node && shape.dockers.length == 1 && selection.include( shape.dockers.first().getDockedShape() )){ 
				return false 
			} 
			
			// Check if any of the parent shape is included in the selection
			var s = shape.parent; 
			do{ 
				if(selection.include(s)){ 
					return false
				}
			}while(s = s.parent); 
			
			// Otherwise, return true
			return true;
			
		});
		
		/* Edges must not be movable, if only edges are selected and at least 
		 * one of them is docked.
		 */
		var edgesMovable = true;
		var onlyEdgesSelected = selection.all(function(shape) {
			if(shape instanceof ORYX.Core.Edge) {
				if(shape.isDocked()) {
					edgesMovable = false;
				}
				return true;	
			}
			return false;
		});
		
		if(onlyEdgesSelected && !edgesMovable) {
			/* Abort moving shapes */
			return;
		}
		
		selection = selection.map(function(shape){ 
			if( shape instanceof ORYX.Core.Node ){
				/*if( shape.dockers.length == 1 ){
					return shape.dockers.first()
				} else {*/
					return shape
				//}
			} else if( shape instanceof ORYX.Core.Edge ) {
				
				var dockers = shape.dockers;
				
				if( selection.include( shape.dockers.first().getDockedShape() ) ){
					dockers = dockers.without( shape.dockers.first() )
				}

				if( selection.include( shape.dockers.last().getDockedShape() ) ){
					dockers = dockers.without( shape.dockers.last() )
				}
				
				return dockers	
							
			} else {
				return null
			}
		
		}).flatten().compact();
		
		if (selection.size() > 0) {
			
			//Stop moving at canvas borders
			var selectionBounds = [ this.facade.getCanvas().bounds.lowerRight().x,
			                        this.facade.getCanvas().bounds.lowerRight().y,
			                        0,
			                        0 ];
			selection.each(function(s) {
				selectionBounds[0] = Math.min(selectionBounds[0], s.bounds.upperLeft().x);
				selectionBounds[1] = Math.min(selectionBounds[1], s.bounds.upperLeft().y);
				selectionBounds[2] = Math.max(selectionBounds[2], s.bounds.lowerRight().x);
				selectionBounds[3] = Math.max(selectionBounds[3], s.bounds.lowerRight().y);
			});
			if(selectionBounds[0]+p.x < 0)
				p.x = -selectionBounds[0];
			if(selectionBounds[1]+p.y < 0)
				p.y = -selectionBounds[1];
			if(selectionBounds[2]+p.x > this.facade.getCanvas().bounds.lowerRight().x)
				p.x = this.facade.getCanvas().bounds.lowerRight().x - selectionBounds[2];
			if(selectionBounds[3]+p.y > this.facade.getCanvas().bounds.lowerRight().y)
				p.y = this.facade.getCanvas().bounds.lowerRight().y - selectionBounds[3];
			
			if(p.x!=0 || p.y!=0) {
				// Instantiate the moveCommand
				var commands = [new ORYX.Core.Command.Move(selection, p, null, currentSelection, this)];
				// Execute the commands			
				this.facade.executeCommands(commands);
			}
			
		}
	},
	
	getUndockedCommant: function(shapes){

		var undockEdgeCommand = ORYX.Core.Command.extend({
			construct: function(moveShapes){
				this.dockers = moveShapes.collect(function(shape){ return shape instanceof ORYX.Core.Controls.Docker ? {docker:shape, dockedShape:shape.getDockedShape(), refPoint:shape.referencePoint} : undefined }).compact();
			},			
			execute: function(){
				this.dockers.each(function(el){
					el.docker.setDockedShape(undefined);
				})
			},
			rollback: function(){
				this.dockers.each(function(el){
					el.docker.setDockedShape(el.dockedShape);
					el.docker.setReferencePoint(el.refPoint);
					//el.docker.update();
				})
			}
		});
		
		command = new undockEdgeCommand( shapes );
		command.execute();	
		return command;
	},
	
//    /**
//     * The key handler for this plugin. Every action from the set of cut, copy,
//     * paste and delete should be accessible trough simple keyboard shortcuts.
//     * This method checks whether any event triggers one of those actions.
//     *
//     * @param {Object} event The keyboard event that should be analysed for
//     *     triggering of this plugin.
//     */
//    keyHandler: function(event){
//        //TODO document what event.which is.
//        
//        ORYX.Log.debug("keysMove.js handles a keyEvent.");
//        
//        // assure we have the current event.
//        if (!event) 
//            event = window.event;
//        
//        // get the currently pressed key and state of control key.
//        var pressedKey = event.which || event.keyCode;
//        var ctrlPressed = event.ctrlKey;
//
//		// if the key is one of the arrow keys, forward to move and return.
//		if ([ORYX.CONFIG.KEY_CODE_LEFT, ORYX.CONFIG.KEY_CODE_RIGHT,
//			ORYX.CONFIG.KEY_CODE_UP, ORYX.CONFIG.KEY_CODE_DOWN].include(pressedKey)) {
//			
//			this.move(pressedKey, !ctrlPressed);
//			return;
//		}
//		
//    }
	
});
