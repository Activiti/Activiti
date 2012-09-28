/**
 * Copyright (c) 2009
 * Matthias Kunze
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
*/

if (!ORYX.Plugins) {
	ORYX.Plugins = {};
}  

if (!ORYX.Config) {
	ORYX.Config = {};
}

ORYX.Config.SignavioFileRepositoryModelHandler = "/signavio/p/model/"; 

ORYX.Plugins.FileRepositorySave = Clazz.extend({
	
    facade: undefined,
	modelUri: window.location.hash.substring(1).strip() || undefined,
	
    construct: function(facade){
		this.facade = facade;

		this.facade.offer({
			'name': ORYX.I18N.Save.save,
			'functionality': this.save.bind(this, false),
			'group': ORYX.I18N.Save.group,
			'icon': ORYX.PATH + "images/disk.png",
			'description': ORYX.I18N.Save.saveDesc,
			'index': 1,
			'minShape': 0,
			'maxShape': 0
		});
		
		/*this.facade.offer({
			'name': ORYX.I18N.Save.saveAs,
			'functionality': this.save.bind(this, true),
			'group': ORYX.I18N.Save.group,
			'icon': ORYX.PATH + "images/disk_multi.png",
			'description': ORYX.I18N.Save.saveAsDesc,
			'index': 2,
			'minShape': 0,
			'maxShape': 0
		});*/
		

		// ask before closing the window
		this.changeDifference = 0;		
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_UNDO_EXECUTE, function(){ this.changeDifference++; });
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_EXECUTE_COMMANDS, function(){this.changeDifference++; });
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_UNDO_ROLLBACK, function(){this.changeDifference--; });
		
		window.onbeforeunload = function(){
			if (this.changeDifference > 0){
				return ORYX.I18N.Save.unsavedData;
			}
		}.bind(this);
	},

	saveAs: function(){
		return this.save(true);
	},
	
	save: function(_saveAs) {
		// saveAs requires to save it as a new file
		// save first time requires to save it as a new file
		_saveAs = _saveAs || !this.modelUri;

		var svgDOM = DataManager.serialize(this.facade.getCanvas().getSVGRepresentation(true));
		var serializedDOM = Ext.encode(this.facade.getJSON());
		
		var modelData = {
			name: "New Process",
			description: "",
			comment: "",
			namespace: this.facade.getStencilSets().values()[0].namespace(),
			json_xml: serializedDOM,
			parent: undefined,
			svg_xml: svgDOM,
			type: this.facade.getStencilSets().values()[0].namespace()
		};
		
		// try to load available information
		if (this.modelUri) {
			var request = new Ajax.Request(ORYX.Config.SignavioFileRepositoryModelHandler + this.modelUri.replace(/^\/?/,"") + "/info", {
				method: "GET",
				asynchronous: false
			});
			
			if (!request.transport || !request.transport.status == 200) {
				Ext.Msg.show({
				   title: "Unable to load model data",
				   msg: "The model does not seem to exist anymore or the model storage is unavailable. Remove the model-id (everything behind #) and try again.",
				   buttons: Ext.MessageBox.OK
				});
				
				return false;
			}
			
			var data = request.transport.responseText.evalJSON();
			["name", "description", "comment", "parent", "type"].each(function(key) {
				modelData[key] = data[key] || modelData[key];
			});
		}
		else {
			// model has no URI, so it should at least have parent assigned in the editor URL
			var parent = Ext.urlDecode(location.search.substring(1))["directory"];
			if (!parent) {
				Ext.Msg.show({
				   title: "Parent Directory Missing",
				   msg: "The model does not semm to be saved yet, however I can't find a parent diractory. Thus the model cannot be saved at all. My bad. :( \n PS: Your best shot is to try to export the model, create a new one, import andh hope for the best.",
				   buttons: Ext.MessageBox.OK
				});
				
				return false;
			}
			modelData.parent = parent;
		}
		
			
		if (!_saveAs) {
			this.submit(modelData, false);
			return true;
		}
			
		var form = new Ext.XTemplate(		
			'<form class="oryx_repository_edit_model" action="#" id="edit_model" onsubmit="return false;">',
				'<fieldset>',
					'<p class="description">' + ORYX.I18N.Save.dialogDesciption + '</p>',
					'<input type="hidden" name="namespace" value="{namespace}" />',
					'<p><label for="edit_model_title">' + ORYX.I18N.Save.dialogLabelTitle + '</label><input type="text" class="text" name="title" value="{name}" id="edit_model_title" onfocus="this.className = \'text activated\'" onblur="this.className = \'text\'"/></p>',
					'<p><label for="edit_model_summary">' + ORYX.I18N.Save.dialogLabelDesc + '</label><textarea rows="5" name="summary" id="edit_model_summary" onfocus="this.className = \'activated\'" onblur="this.className = \'\'">{description}</textarea></p>',
					'<p><label for="edit_model_type">' + ORYX.I18N.Save.dialogLabelType + '</label><input type="text" name="type" class="text disabled" value="{type}" disabled="disabled" id="edit_model_type" /></p>',
				'</fieldset>',
			'</form>'
		);

		var win = new Ext.Window({
			// id:		'Propertie_Window',
	        width:	'auto',
	        height:	'auto',
		    title:	(_saveAs ? ORYX.I18N.Save.saveAsTitle : ORYX.I18N.Save.save),
	        modal:	true,
			bodyStyle: 'background:#FFFFFF',
	        html: 	form.apply(modelData),
	        
			buttons:[
			    {
			    	text: ORYX.I18N.Save.saveBtn,
			    	handler: function(){
			    	
			    		modelData.name = $("edit_model_title").value.strip(); 
			    		modelData.description = $("edit_model_summary").value.strip()

			    		this.submit(modelData, true);
						win.close();
						
					}.bind(this)
			    },{
			    	text: ORYX.I18N.Save.close,
			    	handler: function(){
			    		win.close();
            		}
			    }
			],
			listeners: {
				close: function(){					
                	win.close();
				}
			}
	    });
	
		win.show();
		
		return true;
	
	},
	
	submit: function(params, _saveAs) {
		
		this.facade.raiseEvent({
            type: ORYX.CONFIG.EVENT_LOADING_ENABLE,
			text: ORYX.I18N.Save.saving
        });
		
		if (_saveAs) {
			new Ajax.Request(ORYX.Config.SignavioFileRepositoryModelHandler, {
				method: "POST",
				parameters: params,
				asynchronous: true,
				onSuccess: function success(transport){
					this.modelUri = transport.responseText.evalJSON()["href"].replace(/^\/?(model)?\/?/,"");
					location.hash =  this.modelUri;
					this.changeDifference = 0;
					
					this.facade.raiseEvent({
						type:ORYX.CONFIG.EVENT_LOADING_STATUS,
						text:ORYX.I18N.Save.saved
					});
					
					
					
				}.bind(this),
				onFailure: function failure(){
					alert("fail");
				}.bind(this),
				on403: function f403(){
					alert("403");
				}.bind(this)
			});
		}
		else {			
			new Ajax.Request(ORYX.Config.SignavioFileRepositoryModelHandler + this.modelUri, {
				method: "PUT",
				parameters: params,
				asynchronous: true,
				onSuccess: function success(transport){
					this.changeDifference = 0;					
					this.facade.raiseEvent({
						type:ORYX.CONFIG.EVENT_LOADING_STATUS,
						text:ORYX.I18N.Save.saved
					});
					
				}.bind(this),
				onFailure: function failure(){
					alert("fail");
				}.bind(this),
				on403: function f403(){
					alert("403");
				}.bind(this)
			});
		}
	}
});

onOryxResourcesLoaded = function () {
	if (location.hash.slice(1).length == 0 || location.hash.slice(1).indexOf('new')!=-1) {
		var stencilset = ORYX.Utils.getParamFromUrl('stencilset') 
			? ORYX.Utils.getParamFromUrl('stencilset')
			: 'stencilsets/bpmn1.1/bpmn1.1.json';

		new ORYX.Editor({
			id: 'oryx-canvas123',
			stencilset: {
				url: '/oryx/'+stencilset
			}
		});
	}
	else {
		ORYX.Editor.createByUrl(ORYX.Config.SignavioFileRepositoryModelHandler + location.hash.slice(1).replace(/^\/?/,"") + "/json", {
			id: 'oryx-canvas123'
		});
	};
};