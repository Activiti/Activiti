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

ORYX.Plugins.Save = Clazz.extend({
	
    facade: undefined,
	
	processURI: undefined,
	
	changeSymbol : "*",
	
    construct: function(facade){
		this.facade = facade;
		
		this.facade.offer({
			'name': ORYX.I18N.Save.save,
			'functionality': this.save.bind(this,false),
			'group': ORYX.I18N.Save.group,
			'icon': ORYX.PATH + "images/disk.png",
			'description': ORYX.I18N.Save.saveDesc,
			'index': 1,
			'minShape': 0,
			'maxShape': 0,
			keyCodes: [{
			 		metaKeys: [ORYX.CONFIG.META_KEY_META_CTRL],
					keyCode: 83, // s-Keycode
					keyAction: ORYX.CONFIG.KEY_ACTION_UP 
				}
			 ]
		});
		
		document.addEventListener("keydown", function(e){
			if (e.ctrlKey&&e.keyCode === 83){
				Event.stop(e);
			}
		}, false)
		
		
		/*this.facade.offer({
			'name': ORYX.I18N.Save.saveAs,
			'functionality': this.save.bind(this,true),
			'group': ORYX.I18N.Save.group,
			'icon': ORYX.PATH + "images/disk_multi.png",
			'description': ORYX.I18N.Save.saveAsDesc,
			'index': 2,
			'minShape': 0,
			'maxShape': 0
		});	*/
		
		window.onbeforeunload = this.onUnLoad.bind(this)
		
		this.changeDifference = 0;
		
		// Register on event for executing commands --> store all commands in a stack		 
		// --> Execute
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_UNDO_EXECUTE, function(){ this.changeDifference++; this.updateTitle(); }.bind(this) );
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_EXECUTE_COMMANDS, function(){ this.changeDifference++; this.updateTitle(); }.bind(this) );
		// --> Rollback
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_UNDO_ROLLBACK, function(){ this.changeDifference--; this.updateTitle(); }.bind(this) );
		
		//TODO very critical for load time performance!!!
		//this.serializedDOM = DataManager.__persistDOM(this.facade);
	},
	
	updateTitle: function(){
		
		var value = window.document.title || document.getElementsByTagName("title")[0].childNodes[0].nodeValue;
		
		if (this.changeDifference === 0 && value.startsWith(this.changeSymbol)){
			window.document.title = value.slice(1);
		} else if (this.changeDifference !== 0 && !value.startsWith(this.changeSymbol)){
			window.document.title = this.changeSymbol + "" + value;
		}
	},
	
	onUnLoad: function(){
		if(this.changeDifference !== 0 || (this.facade.getModelMetaData()['new'] && this.facade.getCanvas().getChildShapes().size() > 0)) {
			return ORYX.I18N.Save.unsavedData;
		}
		
	},
		
	
    saveSynchronously: function(forceNew, modelInfo){
		
		if (!modelInfo) {
			return;
		}
		
		var modelMeta = this.facade.getModelMetaData();
		var reqURI = modelMeta.modelHandler;


		// Get the stencilset
		var ss = this.facade.getStencilSets().values()[0]
		
		var typeTitle = ss.title();
		
		// Define Default values
		var name = (modelMeta["new"] && modelMeta.name === "") ? ORYX.I18N.Save.newProcess : modelInfo.name;
		var defaultData = {title:Signavio.Utils.escapeHTML(name||""), summary:Signavio.Utils.escapeHTML(modelInfo.description||""), type:typeTitle, url: reqURI, namespace: modelInfo.model.stencilset.namespace, comment: '' }
		
		// Create a Template
		var dialog = new Ext.XTemplate(		
					// TODO find some nice words here -- copy from above ;)
					'<form class="oryx_repository_edit_model" action="#" id="edit_model" onsubmit="return false;">',
									
						'<fieldset>',
							'<p class="description">' + ORYX.I18N.Save.dialogDesciption + '</p>',
							'<input type="hidden" name="namespace" value="{namespace}" />',
							'<p><label for="edit_model_title">' + ORYX.I18N.Save.dialogLabelTitle + '</label><input type="text" class="text" name="title" value="{title}" id="edit_model_title" onfocus="this.className = \'text activated\'" onblur="this.className = \'text\'"/></p>',
							'<p><label for="edit_model_summary">' + ORYX.I18N.Save.dialogLabelDesc + '</label><textarea rows="5" name="summary" id="edit_model_summary" onfocus="this.className = \'activated\'" onblur="this.className = \'\'">{summary}</textarea></p>',
							(modelMeta.versioning) ? '<p><label for="edit_model_comment">' + ORYX.I18N.Save.dialogLabelComment + '</label><textarea rows="5" name="comment" id="edit_model_comment" onfocus="this.className = \'activated\'" onblur="this.className = \'\'">{comment}</textarea></p>' : '',
							'<p><label for="edit_model_type">' + ORYX.I18N.Save.dialogLabelType + '</label><input type="text" name="type" class="text disabled" value="{type}" disabled="disabled" id="edit_model_type" /></p>',
							
						'</fieldset>',
					
					'</form>')
		
		// Create the callback for the template
		callback = function(form){

			    // raise loading enable event
		        /*this.facade.raiseEvent({
		            type: ORYX.CONFIG.EVENT_LOADING_ENABLE,
					text: ORYX.I18N.Save.saving
		        });*/

				var title 		= form.elements["title"].value.strip();
				title 			= title.length == 0 ? defaultData.title : title;
				
				var summary 	= form.elements["summary"].value.strip();	
				summary 		= summary.length == 0 ? defaultData.summary : summary;
				
				var namespace	= form.elements["namespace"].value.strip();
				namespace		= namespace.length == 0 ? defaultData.namespace : namespace;
				
				modelMeta.name = title;
				modelMeta.description = summary;
				modelMeta.parent = modelInfo.parent;
				modelMeta.namespace = namespace;
	        		
				//added changing title of page after first save, but with the changed flag
				if(!forceNew) window.document.title = this.changeSymbol + title + " | " + ORYX.CONFIG.APPNAME;
					
					
		        // Get json
				var json = this.facade.getJSON();
				
				var glossary = [];
				
				//Support for glossary
				if (this.facade.hasGlossaryExtension) {
					
					Ext.apply(json, ORYX.Core.AbstractShape.JSONHelper);
					var allNodes = json.getChildShapes(true);
					
					var orders = {};
					
					this.facade.getGlossary().each(function(entry){
						if ("undefined" == typeof orders[entry.shape.resourceId+"-"+entry.property.prefix()+"-"+entry.property.id()]){
							orders[entry.shape.resourceId+"-"+entry.property.prefix()+"-"+entry.property.id()] = 0;
						}
						// Add entry
						glossary.push({
							itemId		: entry.glossary,
			            	elementId	: entry.shape.resourceId,
			            	propertyId	: entry.property.prefix()+"-"+entry.property.id(),
				            order		: orders[entry.shape.resourceId+"-"+entry.property.prefix()+"-"+entry.property.id()]++
						});
						
						// Replace the property with the generated glossary url
						/*var rId = entry.shape.resourceId;
						var pKe = entry.property.id();
						for (var i=0, size=allNodes.length; i<size; ++i) {
							var sh = allNodes[i];
							if (sh.resourceId == rId) {
								for (var prop in sh.properties) {
									if (prop === pKe) {
										sh.properties[prop] = this.facade.generateGlossaryURL(entry.glossary, sh.properties[prop]);
										break;
									}
								}
								break;
							}
						}*/
						
						
						// Replace SVG
						if (entry.property.refToView() && entry.property.refToView().length > 0) {
							entry.property.refToView().each(function(ref){
								var node = $(entry.shape.id+""+ref);
								if (node)
									node.setAttribute("oryx:glossaryIds", entry.glossary + ";")
							})
						}
					}.bind(this))
					

					// Set the json as string
					json = json.serialize();

				} else {
					json = Ext.encode(json);
				}
				
				// Set the glossaries as string
				glossary = Ext.encode(glossary);
				
				var selection = this.facade.getSelection();
				this.facade.setSelection([]);

				// Get the serialized svg image source
		        var svgClone 	= this.facade.getCanvas().getSVGRepresentation(true);
				this.facade.setSelection(selection);
		        if (this.facade.getCanvas().properties["oryx-showstripableelements"] === false) {
		        	var stripOutArray = svgClone.getElementsByClassName("stripable-element");
		        	for (var i=stripOutArray.length-1; i>=0; i--) {
		        		stripOutArray[i].parentNode.removeChild(stripOutArray[i]);
		        	}
		        }
				  
				// Remove all forced stripable elements 
	        	var stripOutArray = svgClone.getElementsByClassName("stripable-element-force");
	        	for (var i=stripOutArray.length-1; i>=0; i--) {
	        		stripOutArray[i].parentNode.removeChild(stripOutArray[i]);
	        	}
				          
				// Parse dom to string
		        var svgDOM 	= DataManager.serialize(svgClone);
				
		        var params = {
		        		json_xml: json,
		        		svg_xml: svgDOM,
		        		name: title,
		        		type: defaultData.type,
		        		parent: modelMeta.parent,
		        		description: summary,
		        		glossary_xml: glossary,
		        		namespace: modelMeta.namespace,
		        		views: Ext.util.JSON.encode(modelMeta.views || [])
		        };
		        
				var success = false;
				
				var successFn = function(transport) {
					var loc = transport.getResponseHeader.location;
					if (!this.processURI && loc) {
						this.processURI = loc;
					}
	
					if( forceNew ){
						var resJSON = transport.responseText.evalJSON();
						
						var modelURL = location.href.substring(0, location.href.indexOf(location.search)) + '?id=' + resJSON.href.substring(7);
						var newURLWin = new Ext.Window({
							title:		ORYX.I18N.Save.savedAs, 
							bodyStyle:	"background:white;padding:10px", 
							width:		'auto', 
							height:		'auto',
							html:"<div style='font-weight:bold;margin-bottom:10px'>"+ORYX.I18N.Save.savedDescription+":</div><span><a href='" + modelURL +"' target='_blank'>" + modelURL + "</a></span>",
							buttons:[{text:'Ok',handler:function(){newURLWin.destroy()}}]
						});
						newURLWin.show();
						
						window.open(modelURL);
					}
	
					//show saved status
					/*this.facade.raiseEvent({
							type:ORYX.CONFIG.EVENT_LOADING_STATUS,
							text:ORYX.I18N.Save.saved
						});*/
						
					success = true;
					
					win.close();
				
					if (success) {
						// Reset changes
						this.changeDifference = 0;
						this.updateTitle();
						
						var resJSON = transport.responseText.evalJSON();
						if(resJSON.modelId) {
							modelMeta.modelId = resJSON.modelId;
						}
						
						if(modelMeta["new"]) {
							modelMeta["new"] = false;
						}
					}
					
					
					delete this.saving;
						
				}.bind(this);
				
				var failure = function(transport) {
						// raise loading disable event.
		                this.facade.raiseEvent({
		                    type: ORYX.CONFIG.EVENT_LOADING_DISABLE
		                });
						
						win.close();
						
						if(transport.status && transport.status === 401) {
							Ext.Msg.alert(ORYX.I18N.Oryx.title, ORYX.I18N.Save.notAuthorized).setIcon(Ext.Msg.WARNING).getDialog().setWidth(260).center().syncSize();
						} else if(transport.status && transport.status === 403) {
							Ext.Msg.alert(ORYX.I18N.Oryx.title, ORYX.I18N.Save.noRights).setIcon(Ext.Msg.WARNING).getDialog().setWidth(260).center().syncSize();
						} else if(transport.statusText === "transaction aborted") {
							Ext.Msg.alert(ORYX.I18N.Oryx.title, ORYX.I18N.Save.transAborted).setIcon(Ext.Msg.WARNING).getDialog().setWidth(260).center().syncSize();
						} else if(transport.statusText === "communication failure") {
							Ext.Msg.alert(ORYX.I18N.Oryx.title, ORYX.I18N.Save.comFailed).setIcon(Ext.Msg.WARNING).getDialog().setWidth(260).center().syncSize();
						} else {
							Ext.Msg.alert(ORYX.I18N.Oryx.title, ORYX.I18N.Save.failed).setIcon(Ext.Msg.WARNING).getDialog().setWidth(260).center().syncSize();
						}
						
						delete this.saving;
						
					}.bind(this);
				
				if(modelMeta["new"]) {	
					// Send the request out
					params.id = modelMeta.modelId;					
					this.sendSaveRequest('POST', reqURI, params, true, successFn, failure);
				} else if(forceNew) {
					this.sendSaveRequest('POST', reqURI, params, true, successFn, failure);
				} else {						
					params.id = modelMeta.modelId;
					// Send the request out
					this.sendSaveRequest('PUT', reqURI, params, false, successFn, failure);
				}
		}.bind(this);
			
		// Create a new window				
		win = new Ext.Window({
			id		: 'Propertie_Window',
	        width	: 'auto',
	        height	: 'auto',
		    title	: forceNew ? ORYX.I18N.Save.saveAsTitle : ORYX.I18N.Save.save,
	        modal	: true,
	        resize	: false,
			bodyStyle: 'background:#FFFFFF',
	        html	: dialog.apply( defaultData ),
	        defaultButton: 0,
			buttons:[{
				text: ORYX.I18N.Save.saveBtn,
				handler: function(){
				
					win.body.mask(ORYX.I18N.Save.pleaseWait, "x-waiting-box");
					
					window.setTimeout(function(){
						
						callback($('edit_model'));
						
					}.bind(this), 10);			
				},
				listeners:{
					render:function(){
						this.focus();
					}
				}
			},{
            	text: ORYX.I18N.Save.close,
            	handler: function(){
	               win.close();
            	}.bind(this)
			}],
			listeners: {
				close: function(){					
                	win.destroy();
					delete this.saving;
				}.bind(this)
			}
	    });
				      
		win.show();
    },
	
	/**
	 * Get the model data and call the success callback
	 * 
	 * @param {Function} success Success callback
	 */
	retrieveModelData: function(success){
		
		var onComplete = function(){
			Ext.getBody().unmask();
		}
		
		var modelMeta = this.facade.getModelMetaData();
		
		new Ajax.Request("../service/model/" + modelMeta.modelId + "/json", {
            method: 'get',
            asynchronous: true,
			requestHeaders: {
				"Accept":"application/json"
			},
			encoding: 'UTF-8',
			onSuccess: (function(transport) {
				modelInfo = (transport.responseText||"{}").evalJSON();
				onComplete();
				success(modelInfo);
			}).bind(this),
			onException: function(){
				// raise loading disable event.
                this.facade.raiseEvent({
                    type: ORYX.CONFIG.EVENT_LOADING_DISABLE
                });

				delete this.saving;
				onComplete();
				Ext.Msg.alert(ORYX.I18N.Oryx.title, ORYX.I18N.Save.exception).setIcon(Ext.Msg.ERROR).getDialog().setWidth(260).syncSize();
			}.bind(this),
			onFailure: (function(transport) {
				// raise loading disable event.
                this.facade.raiseEvent({
                    type: ORYX.CONFIG.EVENT_LOADING_DISABLE
                });
				
				delete this.saving;
				onComplete();
				Ext.Msg.alert(ORYX.I18N.Oryx.title, ORYX.I18N.Save.failed).setIcon(Ext.Msg.ERROR).getDialog().setWidth(260).syncSize();
			}).bind(this),
			on401: (function(transport) {
				// raise loading disable event.
                this.facade.raiseEvent({
                    type: ORYX.CONFIG.EVENT_LOADING_DISABLE
                });
				
				delete this.saving;
				onComplete();
				Ext.Msg.alert(ORYX.I18N.Oryx.title, ORYX.I18N.Save.notAuthorized).setIcon(Ext.Msg.WARNING).getDialog().setWidth(260).syncSize();
			}).bind(this),
			on403: (function(transport) {
				// raise loading disable event.
                this.facade.raiseEvent({
                    type: ORYX.CONFIG.EVENT_LOADING_DISABLE
                });
				
				delete this.saving;
				onComplete();
				Ext.Msg.alert(ORYX.I18N.Oryx.title, ORYX.I18N.Save.noRights).setIcon(Ext.Msg.ERROR).getDialog().setWidth(260).syncSize();
			}).bind(this)
		});
	},
	
	sendSaveRequest: function(method, url, params, forceNew, success, failure){
		
		var saveUri;
		if(forceNew == false) {
			saveUri = "../service/model/" + params.id + "/save";
		} else {
			saveUri = "../service/model/new";
		}
		
		// Send the request to the server.
		Ext.Ajax.request({
			url				: saveUri,
			method			: method,
			timeout			: 1800000,
			disableCaching	: true,
			headers			: {'Accept':"application/json"},
			params			: params,
			success			: success,
			failure			: failure
		});
	},
    
    /**
     * Saves the current process to the server.
     */
    save: function(forceNew, event){
        
		// Check if currently is saving
		if (this.saving){
			return;
		}
		
		this.saving = true;
		
		this.facade.raiseEvent({
			type: ORYX.CONFIG.EVENT_ABOUT_TO_SAVE
		});
		
        // ... save synchronously
        window.setTimeout((function(){
            
			var meta = this.facade.getModelMetaData();
			// Check if new...
			if (meta["new"]){
         		this.saveSynchronously(forceNew, meta);
			} else {
				Ext.getBody().mask(ORYX.I18N.Save.retrieveData, "x-waiting-box");
				// ...otherwise, get the current model data first.
				this.retrieveModelData(this.saveSynchronously.bind(this, forceNew))
			}
        }).bind(this), 10);

        
        return true;
    }	
});
