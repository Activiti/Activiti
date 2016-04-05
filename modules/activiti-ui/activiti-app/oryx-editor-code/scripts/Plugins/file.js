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
