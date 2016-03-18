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

/**
 * This plugin offer the functionality of undo/redo
 * Therewith the command pattern is used.
 * 
 * A Plugin which want that the changes could get undo/redo has 
 * to implement a command-class (which implements the method .execute(), .rollback()).
 * Those instance of class must be execute thru the facade.executeCommands(). If so,
 * those command get stored here in the undo/redo stack and can get reset/restore.
 *
 **/

if (!ORYX.Plugins) 
    ORYX.Plugins = new Object();

ORYX.Plugins.Undo = Clazz.extend({
	
	// Defines the facade
    facade		: undefined,
    
	// Defines the undo/redo Stack
	undoStack	: [],
	redoStack	: [],
	
	// Constructor 
    construct: function(facade){
    
        this.facade = facade;     
		
		// Offers the functionality of undo                
        this.facade.offer({
			name			: ORYX.I18N.Undo.undo,
			description		: ORYX.I18N.Undo.undoDesc,
			icon			: ORYX.PATH + "images/arrow_undo.png",
			keyCodes: [{
					metaKeys: [ORYX.CONFIG.META_KEY_META_CTRL],
					keyCode: 90,
					keyAction: ORYX.CONFIG.KEY_ACTION_DOWN
				}
		 	],
			functionality	: this.doUndo.bind(this),
			group			: ORYX.I18N.Undo.group,
			isEnabled		: function(){ return this.undoStack.length > 0 }.bind(this),
			index			: 0
		}); 

		// Offers the functionality of redo
        this.facade.offer({
			name			: ORYX.I18N.Undo.redo,
			description		: ORYX.I18N.Undo.redoDesc,
			icon			: ORYX.PATH + "images/arrow_redo.png",
			keyCodes: [{
					metaKeys: [ORYX.CONFIG.META_KEY_META_CTRL],
					keyCode: 89,
					keyAction: ORYX.CONFIG.KEY_ACTION_DOWN
				}
		 	],
			functionality	: this.doRedo.bind(this),
			group			: ORYX.I18N.Undo.group,
			isEnabled		: function(){ return this.redoStack.length > 0 }.bind(this),
			index			: 1
		}); 
		
		// Register on event for executing commands --> store all commands in a stack		 
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_EXECUTE_COMMANDS, this.handleExecuteCommands.bind(this) );
    	
	},
	
	/**
	 * Stores all executed commands in a stack
	 * 
	 * @param {Object} evt
	 */
	handleExecuteCommands: function( evt ){
		
		// If the event has commands
		if( !evt.commands ){ return }
		
		// Add the commands to a undo stack ...
		this.undoStack.push( evt.commands );
		// ...and delete the redo stack
		this.redoStack = [];
		
		// Update
		this.facade.getCanvas().update();
		this.facade.updateSelection();
		
	},
	
	/**
	 * Does the undo
	 * 
	 */
	doUndo: function(){
		
		// Get the last commands
		var lastCommands = this.undoStack.pop();
		
		if( lastCommands ){
			// Add the commands to the redo stack
			this.redoStack.push( lastCommands );
			
			// Rollback every command
			for(var i=lastCommands.length-1; i>=0; --i){
				lastCommands[i].rollback();
			}
					
			// Update and refresh the canvas
			//this.facade.getCanvas().update();
			//this.facade.updateSelection();
			this.facade.raiseEvent({
				type 	: ORYX.CONFIG.EVENT_UNDO_ROLLBACK, 
				commands: lastCommands
			});
			
			// Update
			this.facade.getCanvas().update();
			this.facade.updateSelection();
		}
	},
	
	/**
	 * Does the redo
	 * 
	 */
	doRedo: function(){
		
		// Get the last commands from the redo stack
		var lastCommands = this.redoStack.pop();
		
		if( lastCommands ){
			// Add this commands to the undo stack
			this.undoStack.push( lastCommands );
			
			// Execute those commands
			lastCommands.each(function(command){
				command.execute();
			});
				
			// Update and refresh the canvas		
			//this.facade.getCanvas().update();
			//this.facade.updateSelection();
			this.facade.raiseEvent({
				type 	: ORYX.CONFIG.EVENT_UNDO_EXECUTE, 
				commands: lastCommands
			});
			
			// Update
			this.facade.getCanvas().update();
			this.facade.updateSelection();
		}
	}
	
});
