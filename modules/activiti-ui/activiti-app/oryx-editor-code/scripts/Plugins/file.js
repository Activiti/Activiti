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


if (!ORYX.Plugins) 
    ORYX.Plugins = new Object();

ORYX.Plugins.Save = Clazz.extend({
	
    facade: undefined,
	
	processURI: undefined,
	
	changeSymbol : "*",
	
    construct: function(facade){
		this.facade = facade;
		
		document.addEventListener("keydown", function(e){
			if (e.ctrlKey&&e.keyCode === 83){
				Event.stop(e);
			}
		}, false);
		
		window.onbeforeunload = this.onUnLoad.bind(this);
		
		this.changeDifference = 0;
		
		// Register on event for executing commands --> store all commands in a stack		 
		// --> Execute
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_UNDO_EXECUTE, function(){ this.changeDifference++; this.updateTitle(); }.bind(this) );
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_EXECUTE_COMMANDS, function(){ this.changeDifference++; this.updateTitle(); }.bind(this) );
		// --> Saved from other places in the editor
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_SAVED, function(){ this.changeDifference = 0; this.updateTitle(); }.bind(this) );
		
		// --> Rollback
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_UNDO_ROLLBACK, function(){ this.changeDifference--; this.updateTitle(); }.bind(this) );
		
		//TODO very critical for load time performance!!!
		//this.serializedDOM = DataManager.__persistDOM(this.facade);
		
		this.hasChanges = this._hasChanges.bind(this);
	},
	
	updateTitle: function(){
		
		var value = window.document.title || document.getElementsByTagName("title")[0].childNodes[0].nodeValue;
		
		if (this.changeDifference === 0 && value.startsWith(this.changeSymbol)){
			window.document.title = value.slice(1);
		} else if (this.changeDifference !== 0 && !value.startsWith(this.changeSymbol)){
			window.document.title = this.changeSymbol + "" + value;
		}
	},
	
	_hasChanges: function() {
	  return this.changeDifference !== 0 || (this.facade.getModelMetaData()['new'] && this.facade.getCanvas().getChildShapes().size() > 0);
	},
	
	onUnLoad: function(){
		if(this._hasChanges()) {
			return ORYX.I18N.Save.unsavedData;
		}	
	}
});
