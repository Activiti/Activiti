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

ORYX.Plugins.Edit = Clazz.extend({
    
    construct: function(facade){
    
        this.facade = facade;
        this.clipboard = new ORYX.Plugins.Edit.ClipBoard();
        
        //this.facade.registerOnEvent(ORYX.CONFIG.EVENT_KEYDOWN, this.keyHandler.bind(this));
        
        this.facade.offer({
         name: ORYX.I18N.Edit.cut,
         description: ORYX.I18N.Edit.cutDesc,
         icon: ORYX.PATH + "images/cut.png",
		 keyCodes: [{
				metaKeys: [ORYX.CONFIG.META_KEY_META_CTRL],
				keyCode: 88,
				keyAction: ORYX.CONFIG.KEY_ACTION_DOWN
			}
		 ],
         functionality: this.callEdit.bind(this, this.editCut),
         group: ORYX.I18N.Edit.group,
         index: 1,
         minShape: 1
         });
         
        this.facade.offer({
         name: ORYX.I18N.Edit.copy,
         description: ORYX.I18N.Edit.copyDesc,
         icon: ORYX.PATH + "images/page_copy.png",
		 keyCodes: [{
				metaKeys: [ORYX.CONFIG.META_KEY_META_CTRL],
				keyCode: 67,
				keyAction: ORYX.CONFIG.KEY_ACTION_DOWN
			}
		 ],
         functionality: this.callEdit.bind(this, this.editCopy, [true, false]),
         group: ORYX.I18N.Edit.group,
         index: 2,
         minShape: 1
         });
         
        this.facade.offer({
         name: ORYX.I18N.Edit.paste,
         description: ORYX.I18N.Edit.pasteDesc,
         icon: ORYX.PATH + "images/page_paste.png",
		 keyCodes: [{
				metaKeys: [ORYX.CONFIG.META_KEY_META_CTRL],
				keyCode: 86,
				keyAction: ORYX.CONFIG.KEY_ACTION_DOWN
			}
		 ],
         functionality: this.callEdit.bind(this, this.editPaste),
         isEnabled: this.clipboard.isOccupied.bind(this.clipboard),
         group: ORYX.I18N.Edit.group,
         index: 3,
         minShape: 0,
         maxShape: 0
         });
         
        this.facade.offer({
            name: ORYX.I18N.Edit.del,
            description: ORYX.I18N.Edit.delDesc,
            icon: ORYX.PATH + "images/cross.png",
			keyCodes: [{
					metaKeys: [ORYX.CONFIG.META_KEY_META_CTRL],
					keyCode: 8,
					keyAction: ORYX.CONFIG.KEY_ACTION_DOWN
				},
				{	
					keyCode: 46,
					keyAction: ORYX.CONFIG.KEY_ACTION_DOWN
				}
			],
            functionality: this.callEdit.bind(this, this.editDelete),
            group: ORYX.I18N.Edit.group,
            index: 4,
            minShape: 1
        });
    },
	
	callEdit: function(fn, args){
		window.setTimeout(function(){
			fn.apply(this, (args instanceof Array ? args : []));
		}.bind(this), 1);
	},
	
	/**
	 * Handles the mouse down event and starts the copy-move-paste action, if
	 * control or meta key is pressed.
	 */
	handleMouseDown: function(event) {
		if(this._controlPressed) {
			this._controlPressed = false;
			this.editCopy();
//			console.log("copiedEle: %0",this.clipboard.shapesAsJson)
//			console.log("mousevent: %o",event)
			this.editPaste();
			event.forceExecution = true;
			this.facade.raiseEvent(event, this.clipboard.shapesAsJson);
			
		}
	},
    
    /**
     * The key handler for this plugin. Every action from the set of cut, copy,
     * paste and delete should be accessible trough simple keyboard shortcuts.
     * This method checks whether any event triggers one of those actions.
     *
     * @param {Object} event The keyboard event that should be analysed for
     *     triggering of this plugin.
     */
//    keyHandler: function(event){
//        //TODO document what event.which is.
//        
//        ORYX.Log.debug("edit.js handles a keyEvent.");
//        
//        // assure we have the current event.
//        if (!event) 
//            event = window.event;
//        
//        
//        // get the currently pressed key and state of control key.
//        var pressedKey = event.which || event.keyCode;
//        var ctrlPressed = event.ctrlKey;
//        
//        // if the object is to be deleted, do so, and return immediately.
//        if ((pressedKey == ORYX.CONFIG.KEY_CODE_DELETE) ||
//        ((pressedKey == ORYX.CONFIG.KEY_CODE_BACKSPACE) &&
//        (event.metaKey || event.appleMetaKey))) {
//        
//            ORYX.Log.debug("edit.js deletes the shape.");
//            this.editDelete();
//            return;
//        }
//        
//         // if control key is not pressed, we're not interested anymore.
//         if (!ctrlPressed)
//         return;
//         
//         // when ctrl is pressed, switch trough the possibilities.
//         switch (pressedKey) {
//         
//	         // cut.
//	         case ORYX.CONFIG.KEY_CODE_X:
//	         this.editCut();
//	         break;
//	         
//	         // copy.
//	         case ORYX.CONFIG.KEY_CODE_C:
//	         this.editCopy();
//	         break;
//	         
//	         // paste.
//	         case ORYX.CONFIG.KEY_CODE_V:
//	         this.editPaste();
//	         break;
//         }
//    },
	
    /**
     * Returns a list of shapes which should be considered while copying.
     * Besides the shapes of given ones, edges and attached nodes are added to the result set.
     * If one of the given shape is a child of another given shape, it is not put into the result. 
     */
    getAllShapesToConsider: function(shapes){
        var shapesToConsider = []; // only top-level shapes
        var childShapesToConsider = []; // all child shapes of top-level shapes
        
        shapes.each(function(shape){
            //Throw away these shapes which have a parent in given shapes
            isChildShapeOfAnother = shapes.any(function(s2){
                return s2.hasChildShape(shape);
            });
            if(isChildShapeOfAnother) return;
            
            // This shape should be considered
            shapesToConsider.push(shape);
            // Consider attached nodes (e.g. intermediate events)
            if (shape instanceof ORYX.Core.Node) {
				var attached = shape.getOutgoingNodes();
				attached = attached.findAll(function(a){ return !shapes.include(a) });
                shapesToConsider = shapesToConsider.concat(attached);
            }
            
            childShapesToConsider = childShapesToConsider.concat(shape.getChildShapes(true));
        }.bind(this));
        
        // All edges between considered child shapes should be considered
        // Look for these edges having incoming and outgoing in childShapesToConsider
        var edgesToConsider = this.facade.getCanvas().getChildEdges().select(function(edge){
            // Ignore if already added
            if(shapesToConsider.include(edge)) return false;
            // Ignore if there are no docked shapes
            if(edge.getAllDockedShapes().size() === 0) return false; 
            // True if all docked shapes are in considered child shapes
            return edge.getAllDockedShapes().all(function(shape){
                // Remember: Edges can have other edges on outgoing, that is why edges must not be included in childShapesToConsider
                return shape instanceof ORYX.Core.Edge || childShapesToConsider.include(shape);
            });
        });
        shapesToConsider = shapesToConsider.concat(edgesToConsider);
        
        return shapesToConsider;
    },
    
    /**
     * Performs the cut operation by first copy-ing and then deleting the
     * current selection.
     */
    editCut: function(){
        //TODO document why this returns false.
        //TODO document what the magic boolean parameters are supposed to do.
        
        this.editCopy(false, true);
        this.editDelete(true);
        return false;
    },
    
    /**
     * Performs the copy operation.
     * @param {Object} will_not_update ??
     */
    editCopy: function( will_update, useNoOffset ){
        var selection = this.facade.getSelection();
        
        //if the selection is empty, do not remove the previously copied elements
        if(selection.length == 0) return;
        
        this.clipboard.refresh(selection, this.getAllShapesToConsider(selection), this.facade.getCanvas().getStencil().stencilSet().namespace(), useNoOffset);

        if( will_update ) this.facade.updateSelection();
    },
    
    /**
     * Performs the paste operation.
     */
    editPaste: function(){
        // Create a new canvas with childShapes 
		//and stencilset namespace to be JSON Import conform
		var canvas = {
            childShapes: this.clipboard.shapesAsJson,
			stencilset:{
				namespace:this.clipboard.SSnamespace
			}
        };
        // Apply json helper to iterate over json object
		jQuery.extend(canvas, ORYX.Core.AbstractShape.JSONHelper);
        
        var childShapeResourceIds = canvas.getChildShapes(true).pluck("resourceId");
        var outgoings = {};
        // Iterate over all shapes
        canvas.eachChild(function(shape, parent){
            // Throw away these references where referenced shape isn't copied
            shape.outgoing = shape.outgoing.select(function(out){
                return childShapeResourceIds.include(out.resourceId);
            });
			shape.outgoing.each(function(out){
				if (!outgoings[out.resourceId]){ outgoings[out.resourceId] = []; }
				outgoings[out.resourceId].push(shape);
			});
			
            return shape;
        }.bind(this), true, true);
        

        // Iterate over all shapes
        canvas.eachChild(function(shape, parent){
            
        	// Check if there has a valid target
            if(shape.target && !(childShapeResourceIds.include(shape.target.resourceId))){
                shape.target = undefined;
                shape.targetRemoved = true;
            }
    		
    		// Check if the first docker is removed
    		if(	shape.dockers && 
    			shape.dockers.length >= 1 && 
    			shape.dockers[0].getDocker &&
    			((shape.dockers[0].getDocker().getDockedShape() &&
    			!childShapeResourceIds.include(shape.dockers[0].getDocker().getDockedShape().resourceId)) || 
    			!shape.getShape().dockers[0].getDockedShape()&&!outgoings[shape.resourceId])) {
    				
    			shape.sourceRemoved = true;
    		}
			
            return shape;
        }.bind(this), true, true);

		
        // Iterate over top-level shapes
        canvas.eachChild(function(shape, parent){
            // All top-level shapes should get an offset in their bounds
            // Move the shape occording to COPY_MOVE_OFFSET
        	if (this.clipboard.useOffset) {
	            shape.bounds = {
	                lowerRight: {
	                    x: shape.bounds.lowerRight.x + ORYX.CONFIG.COPY_MOVE_OFFSET,
	                    y: shape.bounds.lowerRight.y + ORYX.CONFIG.COPY_MOVE_OFFSET
	                },
	                upperLeft: {
	                    x: shape.bounds.upperLeft.x + ORYX.CONFIG.COPY_MOVE_OFFSET,
	                    y: shape.bounds.upperLeft.y + ORYX.CONFIG.COPY_MOVE_OFFSET
	                }
	            };
        	}
            // Only apply offset to shapes with a target
            if (shape.dockers){
                shape.dockers = shape.dockers.map(function(docker, i){
                    // If shape had a target but the copied does not have anyone anymore,
                    // migrate the relative dockers to absolute ones.
                    if( (shape.targetRemoved === true && i == shape.dockers.length - 1&&docker.getDocker) ||
						(shape.sourceRemoved === true && i == 0&&docker.getDocker)){

                        docker = docker.getDocker().bounds.center();
                    }

					// If it is the first docker and it has a docked shape, 
					// just return the coordinates
				   	if ((i == 0 && docker.getDocker instanceof Function && 
				   		shape.sourceRemoved !== true && (docker.getDocker().getDockedShape() || ((outgoings[shape.resourceId]||[]).length > 0 && (!(shape.getShape() instanceof ORYX.Core.Node) || outgoings[shape.resourceId][0].getShape() instanceof ORYX.Core.Node)))) || 
						(i == shape.dockers.length - 1 && docker.getDocker instanceof Function && 
						shape.targetRemoved !== true && (docker.getDocker().getDockedShape() || shape.target))){
							
						return {
                        	x: docker.x, 
                        	y: docker.y,
                        	getDocker: docker.getDocker
						}
					} else if (this.clipboard.useOffset) {
	                    return {
		                        x: docker.x + ORYX.CONFIG.COPY_MOVE_OFFSET, 
		                        y: docker.y + ORYX.CONFIG.COPY_MOVE_OFFSET,
	                        	getDocker: docker.getDocker
		                    };
				   	} else {
				   		return {
                        	x: docker.x, 
                        	y: docker.y,
                        	getDocker: docker.getDocker
						};
				   	}
                }.bind(this));

            } else if (shape.getShape() instanceof ORYX.Core.Node && shape.dockers && shape.dockers.length > 0 && (!shape.dockers.first().getDocker || shape.sourceRemoved === true || !(shape.dockers.first().getDocker().getDockedShape() || outgoings[shape.resourceId]))){
            	
            	shape.dockers = shape.dockers.map(function(docker, i){
            		
                    if((shape.sourceRemoved === true && i == 0&&docker.getDocker)){
                    	docker = docker.getDocker().bounds.center();
                    }
                    
                    if (this.clipboard.useOffset) {
	            		return {
	                        x: docker.x + ORYX.CONFIG.COPY_MOVE_OFFSET, 
	                        y: docker.y + ORYX.CONFIG.COPY_MOVE_OFFSET,
	                    	getDocker: docker.getDocker
	                    };
                    } else {
	            		return {
	                        x: docker.x, 
	                        y: docker.y,
	                    	getDocker: docker.getDocker
	                    };
                    }
            	}.bind(this));
            }
            
            return shape;
        }.bind(this), false, true);

        this.clipboard.useOffset = true;
        this.facade.importJSON(canvas);
    },
    
    /**
     * Performs the delete operation. No more asking.
     */
    editDelete: function(){
        var selection = this.facade.getSelection();
        
        var clipboard = new ORYX.Plugins.Edit.ClipBoard();
        clipboard.refresh(selection, this.getAllShapesToConsider(selection));
        
		var command = new ORYX.Plugins.Edit.DeleteCommand(clipboard , this.facade);
                                       
		this.facade.executeCommands([command]);
    }
}); 

ORYX.Plugins.Edit.ClipBoard = Clazz.extend({
    construct: function(){
        this.shapesAsJson = [];
        this.selection = [];
		this.SSnamespace="";
		this.useOffset=true;
    },
    isOccupied: function(){
        return this.shapesAsJson.length > 0;
    },
    refresh: function(selection, shapes, namespace, useNoOffset){
        this.selection = selection;
        this.SSnamespace=namespace;
        // Store outgoings, targets and parents to restore them later on
        this.outgoings = {};
        this.parents = {};
        this.targets = {};
        this.useOffset = useNoOffset !== true;
        
        this.shapesAsJson = shapes.map(function(shape){
            var s = shape.toJSON();
            s.parent = {resourceId : shape.getParentShape().resourceId};
            s.parentIndex = shape.getParentShape().getChildShapes().indexOf(shape)
            return s;
        });
    }
});

ORYX.Plugins.Edit.DeleteCommand = ORYX.Core.Command.extend({
    construct: function(clipboard, facade){
        this.clipboard          = clipboard;
        this.shapesAsJson       = clipboard.shapesAsJson;
        this.facade             = facade;
        
        // Store dockers of deleted shapes to restore connections
        this.dockers            = this.shapesAsJson.map(function(shapeAsJson) {
            var shape = shapeAsJson.getShape();
            var incomingDockers = shape.getIncomingShapes().map(function(s){return s.getDockers().last();});
            var outgoingDockers = shape.getOutgoingShapes().map(function(s){return s.getDockers().first();});
            var dockers = shape.getDockers().concat(incomingDockers, outgoingDockers).compact().map(function(docker){
                return {
                    object: docker,
                    referencePoint: docker.referencePoint,
                    dockedShape: docker.getDockedShape()
                };
            });
            return dockers;
        }).flatten();
    },          
    execute: function(){
        this.shapesAsJson.each(function(shapeAsJson){
            // Delete shape
            this.facade.deleteShape(shapeAsJson.getShape());
        }.bind(this));
        
        this.facade.setSelection([]);
        this.facade.getCanvas().update();		
		this.facade.updateSelection();
        
    },
    rollback: function(){
        this.shapesAsJson.each(function(shapeAsJson) {
            var shape = shapeAsJson.getShape();
            var parent = this.facade.getCanvas().getChildShapeByResourceId(shapeAsJson.parent.resourceId) || this.facade.getCanvas();
            parent.add(shape, shape.parentIndex);
        }.bind(this));
        
        //reconnect shapes
        this.dockers.each(function(d) {
            d.object.setDockedShape(d.dockedShape);
            d.object.setReferencePoint(d.referencePoint);
        }.bind(this));
        
        this.facade.setSelection(this.selectedShapes);
        this.facade.getCanvas().update();	
		this.facade.updateSelection();
        
    }
});