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
var idCounter = 0;
var ID_PREFIX = "resource";

/**
 * Main initialization method. To be called when loading
 * of the document, including all scripts, is completed.
 */
function init() {

	ORYX.Log.debug("Querying editor instances");

	// Hack for WebKit to set the SVGElement-Classes
	ORYX.Editor.setMissingClasses();
    
    // If someone wants to create the editor instance himself
    if (window.onOryxResourcesLoaded) {
        window.onOryxResourcesLoaded();
    } 
    // Else fetch the model from server and display editor
    else {
		var modelId = window.location.search.substring(4);
		var modelUrl = "./service/model/" + modelId + "/json";

        ORYX.Editor.createByUrl(modelUrl);
    }
}

/**
   @namespace Global Oryx name space
   @name ORYX
*/
if(!ORYX) {var ORYX = {};}

/**
 * The Editor class.
 * @class ORYX.Editor
 * @extends Clazz
 * @param {Object} config An editor object, passed to {@link ORYX.Editor#loadSerialized}
 * @param {String} config.id Any ID that can be used inside the editor. If fullscreen=false, any HTML node with this id must be present to render the editor to this node.
 * @param {boolean} [config.fullscreen=true] Render editor in fullscreen mode or not.
 * @param {String} config.stencilset.url Stencil set URL.
 * @param {String} [config.stencil.id] Stencil type used for creating the canvas.  
 * @param {Object} config.properties Any properties applied to the canvas.
*/
ORYX.Editor = {
    /** @lends ORYX.Editor.prototype */
	// Defines the global dom event listener 
	DOMEventListeners: new Hash(),

	// Defines the selection
	selection: [],
	
	// Defines the current zoom level
	zoomLevel:1.0,

	construct: function(config) {
		
		// initialization.
		this._eventsQueue 	= [];
		this.loadedPlugins 	= [];
		this.pluginsData 	= [];
		
		
		//meta data about the model for the signavio warehouse
		//directory, new, name, description, revision, model (the model data)
		
		this.modelMetaData = config;
		
		var model = config;
		
		this.id = model.modelId;
		
		if(config.model) {
			model = config.model;
		}
		
        if(!this.id) {
        	this.id = model.id;
        	if(!this.id) {
        		this.id = ORYX.Editor.provideId();
        	}
        }
		
        // Defines if the editor should be fullscreen or not
		this.fullscreen = config.fullscreen !== false;
		
		// Initialize the eventlistener
		this._initEventListener();

		// Load particular stencilset
		if(ORYX.CONFIG.BACKEND_SWITCH) {
			var ssUrl = (model.stencilset.namespace||model.stencilset.url).replace("#", "%23");
        	ORYX.Core.StencilSet.loadStencilSet(ssUrl, this.modelMetaData, this.id);
		} else {
			var ssUrl = model.stencilset.url;
        	ORYX.Core.StencilSet.loadStencilSet(ssUrl, this.modelMetaData, this.id);
		}

		// CREATES the canvas
		this._createCanvas(model.stencil ? model.stencil.id : null, model.properties);

		// GENERATES the whole EXT.VIEWPORT
		this._generateGUI();

		// Initializing of a callback to check loading ends
		var loadPluginFinished 	= false;
		var loadContentFinished = false;
		var initFinished = function(){	
			if( !loadPluginFinished || !loadContentFinished ){ return }
			this._finishedLoading();
		}.bind(this)
		
		// LOAD the plugins
		window.setTimeout(function(){
			this.loadPlugins();
			loadPluginFinished = true;
			initFinished();
		}.bind(this), 100);

		// LOAD the content of the current editor instance
		window.setTimeout(function(){
            this.loadSerialized(model, true); // Request the meta data as well
            this.getCanvas().update();
			loadContentFinished = true;
			initFinished();
		}.bind(this), 200);
	},
	
	_finishedLoading: function() {
		// Raise Loaded Event
		this.handleEvents( {type:ORYX.CONFIG.EVENT_LOADED} )
	},
	
	_initEventListener: function(){

		// Register on Events
		
		document.documentElement.addEventListener(ORYX.CONFIG.EVENT_KEYDOWN, this.catchKeyDownEvents.bind(this), false);
		document.documentElement.addEventListener(ORYX.CONFIG.EVENT_KEYUP, this.catchKeyUpEvents.bind(this), false);

		// Enable Key up and down Event
		this._keydownEnabled = 	true;
		this._keyupEnabled =  	true;

		this.DOMEventListeners[ORYX.CONFIG.EVENT_MOUSEDOWN] = [];
		this.DOMEventListeners[ORYX.CONFIG.EVENT_MOUSEUP] 	= [];
		this.DOMEventListeners[ORYX.CONFIG.EVENT_MOUSEOVER] = [];
		this.DOMEventListeners[ORYX.CONFIG.EVENT_MOUSEOUT] 	= [];
		this.DOMEventListeners[ORYX.CONFIG.EVENT_SELECTION_CHANGED] = [];
		this.DOMEventListeners[ORYX.CONFIG.EVENT_MOUSEMOVE] = [];
				
	},
	
	/**
	 * Generate the whole viewport of the
	 * Editor and initialized the Ext-Framework
	 * 
	 */
	_generateGUI: function() {

		// Defines the layout height if it's NOT fullscreen
		var layoutHeight 	= ORYX.CONFIG.WINDOW_HEIGHT;
		var canvasParent	= this.getCanvas().rootNode.parentNode;
		
		jQuery("#canvasSection").append(canvasParent);
		if (canvasParent.parentNode) {
    		// Set the editor to the center, and refresh the size
    	 	canvasParent.parentNode.setAttributeNS(null, 'align', 'center');
    	 	canvasParent.setAttributeNS(null, 'align', 'left');
    		this.getCanvas().setSize({
    			width	: ORYX.CONFIG.CANVAS_WIDTH,
    			height	: ORYX.CONFIG.CANVAS_HEIGHT
    		});	
		}
						
	},
	
	getAvailablePlugins: function(){
		var curAvailablePlugins=ORYX.availablePlugins.clone();
		curAvailablePlugins.each(function(plugin){
			if(this.loadedPlugins.find(function(loadedPlugin){
				return loadedPlugin.type==this.name;
			}.bind(plugin))){
				plugin.engaged=true;
			}else{
				plugin.engaged=false;
			}
			}.bind(this));
		return curAvailablePlugins;
	},

	loadScript: function (url, callback){
	    var script = document.createElement("script")
	    script.type = "text/javascript";
	    if (script.readyState){  //IE
	        script.onreadystatechange = function(){
	            if (script.readyState == "loaded" || script.readyState == "complete"){
	                script.onreadystatechange = null;
	                callback();
	            }
        	};
    	} else {  //Others
	        script.onload = function(){
	            callback();
	        };
		}
	    script.src = url;
		document.getElementsByTagName("head")[0].appendChild(script);
	},
	/**
	 * activate Plugin
	 * 
	 * @param {String} name
	 * @param {Function} callback
	 * 		callback(sucess, [errorCode])
	 * 			errorCodes: NOTUSEINSTENCILSET, REQUIRESTENCILSET, NOTFOUND, YETACTIVATED
	 */
	activatePluginByName: function(name, callback, loadTry){

		var match=this.getAvailablePlugins().find(function(value){return value.name==name});
		if(match && (!match.engaged || (match.engaged==='false'))){		
				var loadedStencilSetsNamespaces = this.getStencilSets().keys();
				var facade = this._getPluginFacade();
				var newPlugin;
				var me=this;
				ORYX.Log.debug("Initializing plugin '%0'", match.name);
				
					if (!match.requires 	|| !match.requires.namespaces 	|| match.requires.namespaces.any(function(req){ return loadedStencilSetsNamespaces.indexOf(req) >= 0 }) ){
						if(!match.notUsesIn 	|| !match.notUsesIn.namespaces 	|| !match.notUsesIn.namespaces.any(function(req){ return loadedStencilSetsNamespaces.indexOf(req) >= 0 })){
	
					try {
						
						var className 	= eval(match.name);
							var newPlugin = new className(facade, match);
							newPlugin.type = match.name;
							
							// If there is an GUI-Plugin, they get all Plugins-Offer-Meta-Data
							if (newPlugin.registryChanged) 
								newPlugin.registryChanged(me.pluginsData);
							
							// If there have an onSelection-Method it will pushed to the Editor Event-Handler
							if (newPlugin.onSelectionChanged) 
								me.registerOnEvent(ORYX.CONFIG.EVENT_SELECTION_CHANGED, newPlugin.onSelectionChanged.bind(newPlugin));
							this.loadedPlugins.push(newPlugin);
							this.loadedPlugins.each(function(loaded){
								if(loaded.registryChanged)
									loaded.registryChanged(this.pluginsData);
							}.bind(me));
							callback(true);
						
					} catch(e) {
						ORYX.Log.warn("Plugin %0 is not available", match.name);
						if(!!loadTry){
							callback(false,"INITFAILED");
							return;
						}
						this.loadScript("plugins/scripts/"+match.source, this.activatePluginByName.bind(this,match.name,callback,true));
					}
					}else{
						callback(false,"NOTUSEINSTENCILSET");
						ORYX.Log.info("Plugin need a stencilset which is not loaded'", match.name);
					}
								
				} else {
					callback(false,"REQUIRESTENCILSET");
					ORYX.Log.info("Plugin need a stencilset which is not loaded'", match.name);
				}

			
			}else{
				callback(false, match?"NOTFOUND":"YETACTIVATED");
				//TODO error handling
			}
	},

	/**
	 *  Laden der Plugins
	 */
	loadPlugins: function() {
		
		// if there should be plugins but still are none, try again.
		// TODO this should wait for every plugin respectively.
		/*if (!ORYX.Plugins && ORYX.availablePlugins.length > 0) {
			window.setTimeout(this.loadPlugins.bind(this), 100);
			return;
		}*/
		
		var me = this;
		var newPlugins = [];


		var loadedStencilSetsNamespaces = this.getStencilSets().keys();

		// Available Plugins will be initalize
		var facade = this._getPluginFacade();
		
		// If there is an Array where all plugins are described, than only take those
		// (that comes from the usage of oryx with a mashup api)
		if( ORYX.MashupAPI && ORYX.MashupAPI.loadablePlugins && ORYX.MashupAPI.loadablePlugins instanceof Array ){
		
			// Get the plugins from the available plugins (those who are in the plugins.xml)
			ORYX.availablePlugins = $A(ORYX.availablePlugins).findAll(function(value){
										return ORYX.MashupAPI.loadablePlugins.include( value.name )
									})
			
			// Add those plugins to the list, which are only in the loadablePlugins list
			ORYX.MashupAPI.loadablePlugins.each(function( className ){
				if( !(ORYX.availablePlugins.find(function(val){ return val.name == className }))){
					ORYX.availablePlugins.push( {name: className } );
				}
			})
		}
		
		
		ORYX.availablePlugins.each(function(value) {
			ORYX.Log.debug("Initializing plugin '%0'", value.name);
				if( (!value.requires 	|| !value.requires.namespaces 	|| value.requires.namespaces.any(function(req){ return loadedStencilSetsNamespaces.indexOf(req) >= 0 }) ) &&
					(!value.notUsesIn 	|| !value.notUsesIn.namespaces 	|| !value.notUsesIn.namespaces.any(function(req){ return loadedStencilSetsNamespaces.indexOf(req) >= 0 }) )&&
					/*only load activated plugins or undefined */
					(value.engaged || (value.engaged===undefined)) ){

				try {
					var className 	= eval(value.name);
					if( className ){
						var plugin		= new className(facade, value);
						plugin.type		= value.name;
						newPlugins.push( plugin );
						plugin.engaged=true;
					}
				} catch(e) {
					ORYX.Log.warn("Plugin %0 is not available %1", value.name, e);
				}
							
			} else {
				ORYX.Log.info("Plugin need a stencilset which is not loaded'", value.name);
			}
			
		});

		newPlugins.each(function(value) {
			// If there is an GUI-Plugin, they get all Plugins-Offer-Meta-Data
			if(value.registryChanged)
				value.registryChanged(me.pluginsData);

			// If there have an onSelection-Method it will pushed to the Editor Event-Handler
			if(value.onSelectionChanged)
				me.registerOnEvent(ORYX.CONFIG.EVENT_SELECTION_CHANGED, value.onSelectionChanged.bind(value));
		});

		this.loadedPlugins = newPlugins;
		
		this.registerPluginsOnKeyEvents();
		
		this.setSelection();
		
	},

	/**
	 * Creates the Canvas
	 * @param {String} [stencilType] The stencil type used for creating the canvas. If not given, a stencil with myBeRoot = true from current stencil set is taken.
	 * @param {Object} [canvasConfig] Any canvas properties (like language).
	 */
	_createCanvas: function(stencilType, canvasConfig) {
        if (stencilType) {
            // Add namespace to stencilType
            if (stencilType.search(/^http/) === -1) {
                stencilType = this.getStencilSets().values()[0].namespace() + stencilType;
            }
        }
        else {
            // Get any root stencil type
            stencilType = this.getStencilSets().values()[0].findRootStencilName();
        }
        
		// get the stencil associated with the type
		var canvasStencil = ORYX.Core.StencilSet.stencil(stencilType);
			
		if (!canvasStencil) 
			ORYX.Log.fatal("Initialisation failed, because the stencil with the type %0 is not part of one of the loaded stencil sets.", stencilType);
		
		// create all dom
		// TODO fix border, so the visible canvas has a double border and some spacing to the scrollbars
		var div = ORYX.Editor.graft("http://www.w3.org/1999/xhtml", null, ['div']);
		// set class for custom styling
		div.addClassName("ORYX_Editor");
						
		// create the canvas
		this._canvas = new ORYX.Core.Canvas({
			width					: ORYX.CONFIG.CANVAS_WIDTH,
			height					: ORYX.CONFIG.CANVAS_HEIGHT,
			'eventHandlerCallback'	: this.handleEvents.bind(this),
			id						: this.id,
			parentNode				: div
		}, canvasStencil, this._getPluginFacade());
        
        if (canvasConfig) {
          // Migrate canvasConfig to an RDF-like structure
          //FIXME this isn't nice at all because we don't want rdf any longer
          var properties = [];
          for(field in canvasConfig){
            properties.push({
              prefix: 'oryx',
              name: field,
              value: canvasConfig[field]
            });
          }
            
          this._canvas.deserialize(properties);
        }
				
	},

	/**
	 * Returns a per-editor singleton plugin facade.
	 * To be used in plugin initialization.
	 */
	_getPluginFacade: function() {

		// if there is no pluginfacade already created:
		if(!(this._pluginFacade))

			// create it.
			this._pluginFacade = {

				activatePluginByName:		this.activatePluginByName.bind(this),
				//deactivatePluginByName:		this.deactivatePluginByName.bind(this),
				getAvailablePlugins:	this.getAvailablePlugins.bind(this),
				offer:					this.offer.bind(this),
				getStencilSets:			this.getStencilSets.bind(this),
				getStencilSetExtensionDefinition:function(){ return Object.clone(this.ss_extensions_def||{})}.bind(this),
				getRules:				this.getRules.bind(this),
				loadStencilSet:			this.loadStencilSet.bind(this),
				createShape:			this.createShape.bind(this),
				deleteShape:			this.deleteShape.bind(this),
				getSelection:			this.getSelection.bind(this),
				setSelection:			this.setSelection.bind(this),
				updateSelection:		this.updateSelection.bind(this),
				getCanvas:				this.getCanvas.bind(this),
				
				importJSON:				this.importJSON.bind(this),
                getJSON:                this.getJSON.bind(this),
                getSerializedJSON:      this.getSerializedJSON.bind(this),
				
				executeCommands:		this.executeCommands.bind(this),
				isExecutingCommands:	this.isExecutingCommands.bind(this),
				
				registerOnEvent:		this.registerOnEvent.bind(this),
				unregisterOnEvent:		this.unregisterOnEvent.bind(this),
				raiseEvent:				this.handleEvents.bind(this),
				enableEvent:			this.enableEvent.bind(this),
				disableEvent:			this.disableEvent.bind(this),
				
				eventCoordinates:		this.eventCoordinates.bind(this),
				eventCoordinatesXY:		this.eventCoordinatesXY.bind(this),
								
				getModelMetaData:		this.getModelMetaData.bind(this)
			};

		// return it.
		return this._pluginFacade;
	},

	isExecutingCommands: function(){
		return !!this.commandExecuting;
	},

	/**
	 * Implementes the command pattern
	 * (The real usage of the command pattern
	 * is implemented and shown in the Plugins/undo.js)
	 *
	 * @param <Oryx.Core.Command>[] Array of commands
	 */
	executeCommands: function(commands){
		
		if (!this.commandStack){
			this.commandStack = [];
		}
		if (!this.commandStackExecuted){
			this.commandStackExecuted = [];
		}
		
		
		this.commandStack = [].concat(this.commandStack)
							  .concat(commands);
		
		// Check if already executes
		if (this.commandExecuting){ return; }
		
		// Start execution
		this.commandExecuting = true;
		
		// Iterate over all commands
		while(this.commandStack.length > 0){
			var command = this.commandStack.shift();
			// and execute it
			command.execute();
			this.commandStackExecuted.push(command);
		}
		
		// Raise event for executing commands
		this.handleEvents({
			type		: ORYX.CONFIG.EVENT_EXECUTE_COMMANDS,
			commands	: this.commandStackExecuted
		});
		
		// Remove temporary vars
		delete this.commandStack;
		delete this.commandStackExecuted;
		delete this.commandExecuting;
		
		
		this.updateSelection();

	},
	
    /**
     * Returns JSON of underlying canvas (calls ORYX.Canvas#toJSON()).
     * @return {Object} Returns JSON representation as JSON object.
     */
    getJSON: function(){
    	delete Array.prototype.toJSON;
        var canvasJSON = this.getCanvas().toJSON();
        canvasJSON.ssextensions = this.getStencilSets().values()[0].extensions().keys().findAll(function(sse){ return !sse.endsWith('/meta#') });
        return canvasJSON;
    },
    
    /**
     * Serializes a call to toJSON().
     * @return {String} Returns JSON representation as string.
     */
    getSerializedJSON: function(){
        return JSON.stringify(this.getJSON());
    },
    
	/**
	* Imports shapes in JSON as expected by {@link ORYX.Editor#loadSerialized}
	* @param {Object|String} jsonObject The (serialized) json object to be imported
	* @param {boolean } [noSelectionAfterImport=false] Set to true if no shapes should be selected after import
	* @throws {SyntaxError} If the serialized json object contains syntax errors
	*/
	importJSON: function(jsonObject, noSelectionAfterImport) {
		
        try {
            jsonObject = this.renewResourceIds(jsonObject);
        } catch(error){
            throw error;
        }     
		//check, if the imported json model can be loaded in this editor
		// (stencil set has to fit)
		if(jsonObject.stencilset.namespace && jsonObject.stencilset.namespace !== this.getCanvas().getStencil().stencilSet().namespace()) {
			alert(String.format(ORYX.I18N.JSONImport.wrongSS, jsonObject.stencilset.namespace, this.getCanvas().getStencil().stencilSet().namespace()));
			return null;
		} else {
			var commandClass = ORYX.Core.Command.extend({
			construct: function(jsonObject, loadSerializedCB, noSelectionAfterImport, facade){
				this.jsonObject = jsonObject;
				this.noSelection = noSelectionAfterImport;
				this.facade = facade;
				this.shapes;
				this.connections = [];
				this.parents = new Hash();
				this.selection = this.facade.getSelection();
				this.loadSerialized = loadSerializedCB;
			},			
			execute: function(){
				
				if (!this.shapes) {
					// Import the shapes out of the serialization		
					this.shapes	= this.loadSerialized( this.jsonObject );		
					
					//store all connections
					this.shapes.each(function(shape) {
						
						if (shape.getDockers) {
							var dockers = shape.getDockers();
							if (dockers) {
								if (dockers.length > 0) {
									this.connections.push([dockers.first(), dockers.first().getDockedShape(), dockers.first().referencePoint]);
								}
								if (dockers.length > 1) {
									this.connections.push([dockers.last(), dockers.last().getDockedShape(), dockers.last().referencePoint]);
								}
							}
						}
						
						//store parents
						this.parents[shape.id] = shape.parent;
					}.bind(this));
				} else {
					this.shapes.each(function(shape) {
						this.parents[shape.id].add(shape);
					}.bind(this));
					
					this.connections.each(function(con) {
						con[0].setDockedShape(con[1]);
						con[0].setReferencePoint(con[2]);
						con[0].update();
					});
				}
				
				//this.parents.values().uniq().invoke("update");
				this.facade.getCanvas().update();			
					
				if(!this.noSelection)
					this.facade.setSelection(this.shapes);
				else
					this.facade.updateSelection();
					
				// call updateSize again, because during loadSerialized the edges' bounds  
				// are not yet initialized properly
				this.facade.getCanvas().updateSize();	
					
				},
				rollback: function(){
					var selection = this.facade.getSelection();
					
					this.shapes.each(function(shape) {
						selection = selection.without(shape);
						this.facade.deleteShape(shape);
					}.bind(this));
					
					/*this.parents.values().uniq().each(function(parent) {
						if(!this.shapes.member(parent))
							parent.update();
					}.bind(this));*/
					
					this.facade.getCanvas().update();
					
					this.facade.setSelection(selection);
				}
			})
			
			var command = new commandClass(jsonObject, 
											this.loadSerialized.bind(this),
											noSelectionAfterImport,
											this._getPluginFacade());
			
			this.executeCommands([command]);	
			
			return command.shapes.clone();
		}
	},
    
    /**
     * This method renew all resource Ids and according references.
     * Warning: The implementation performs a substitution on the serialized object for
     * easier implementation. This results in a low performance which is acceptable if this
     * is only used when importing models.
     * @param {Object|String} jsonObject
     * @throws {SyntaxError} If the serialized json object contains syntax errors.
     * @return {Object} The jsonObject with renewed ids.
     * @private
     */
    renewResourceIds: function(jsonObject){
        // For renewing resource ids, a serialized and object version is needed
        if(Object.prototype.toString.call(jsonObject) === "String"){
            try {
                var serJsonObject = jsonObject;
                jsonObject = JSON.parse(jsonObject);
            } catch(error){
                throw new SyntaxError(error.message);
            }
        } else {
            var serJsonObject = JSON.stringify(jsonObject);
        }        
        
        // collect all resourceIds recursively
        var collectResourceIds = function(shapes){
            if(!shapes) return [];
            
            return shapes.map(function(shape){
                return collectResourceIds(shape.childShapes).concat(shape.resourceId);
            }).flatten();
        }
        var resourceIds = collectResourceIds(jsonObject.childShapes);
        
        // Replace each resource id by a new one
        resourceIds.each(function(oldResourceId){
            var newResourceId = ORYX.Editor.provideId();
            serJsonObject = serJsonObject.replace(new RegExp(oldResourceId, 'g'), newResourceId);
        });
        
        return JSON.parse(serJsonObject);
    },

    /**
     * Loads serialized model to the oryx.
     * @example
     * editor.loadSerialized({
     *    resourceId: "mymodel1",
     *    childShapes: [
     *       {
     *          stencil:{ id:"Subprocess" },
     *          outgoing:[{resourceId: 'aShape'}],
     *          target: {resourceId: 'aShape'},
     *          bounds:{ lowerRight:{ y:510, x:633 }, upperLeft:{ y:146, x:210 } },
     *          resourceId: "myshape1",
     *          childShapes:[],
     *          properties:{},
     *       }
     *    ],
     *    properties:{
     *       language: "English"
     *    },
     *    stencilset:{
     *       url:"http://localhost:8080/oryx/stencilsets/bpmn1.1/bpmn1.1.json"
     *    },
     *    stencil:{
     *       id:"BPMNDiagram"
     *    }
     * });
     * @param {Object} model Description of the model to load.
     * @param {Array} [model.ssextensions] List of stenctil set extensions.
     * @param {String} model.stencilset.url
     * @param {String} model.stencil.id 
     * @param {Array} model.childShapes
     * @param {Array} [model.properties]
     * @param {String} model.resourceId
     * @return {ORYX.Core.Shape[]} List of created shapes
     * @methodOf ORYX.Editor.prototype
     */
    loadSerialized: function(model, requestMeta){
        var canvas  = this.getCanvas();
      
        // Bugfix (cf. http://code.google.com/p/oryx-editor/issues/detail?id=240)
        // Deserialize the canvas' stencil set extensions properties first!
        this.loadSSExtensions(model.ssextensions);
		
		// Load Meta Data Extension if available
		// #Signavio
		if (requestMeta === true) {
			var metaDataExtension = this.getExtensionForMetaData();
			if (metaDataExtension) {
				this.loadSSExtension(metaDataExtension);
			}
		}
		
        var shapes = this.getCanvas().addShapeObjects(model.childShapes, this.handleEvents.bind(this));
        
        if(model.properties) {
        	for(key in model.properties) {
        		var value = model.properties[key];
				var prop = this.getCanvas().getStencil().property("oryx-"+key);
        		if (!(typeof value === "string") && (!prop || !prop.isList())) {
        			value = JSON.stringify(value);
        		}
            	this.getCanvas().setProperty("oryx-" + key, value);
            }
        }
        
        
        this.getCanvas().updateSize();
		
		// Force to update the selection
		this.selection = [null];
		this.setSelection([]);
		
        return shapes;
    },
	
	/**
	 * Return the namespace of the extension which
	 * provided all the self defined meta data
	 * @return {String} Returns null if no extension is defined, otherwise the namespace
	 *
	 */
	getExtensionForMetaData: function(){
		if (!this.ss_extensions_def||!(this.ss_extensions_def.extensions instanceof Array)){
			return null;
		}
		
		var stencilsets = this.getStencilSets();
		var extension = this.ss_extensions_def.extensions.find(function(ex){
				return !!stencilsets[ex["extends"]] && ex.namespace.endsWith("/meta#");
			});
			
		return extension ? extension.namespace || null : null;		
	},
    
    /**
     * Calls ORYX.Editor.prototype.ss_extension_namespace for each element
     * @param {Array} ss_extension_namespaces An array of stencil set extension namespaces.
     */
    loadSSExtensions: function(ss_extension_namespaces){
        if(!ss_extension_namespaces) return;

        ss_extension_namespaces.each(function(ss_extension_namespace){
            this.loadSSExtension(ss_extension_namespace);
        }.bind(this));
    },
	
	/**
	* Loads a stencil set extension.
	* The stencil set extensions definiton file must already
	* be loaded when the editor is initialized.
	*/
	loadSSExtension: function(ss_extension_namespace) {				
		
		if (this.ss_extensions_def) {
			var extension = this.ss_extensions_def.extensions.find(function(ex){
				return (ex.namespace == ss_extension_namespace);
			});
			
			if (!extension) {
				return;
			}
			
			var stencilset = this.getStencilSets()[extension["extends"]];
			
			if (!stencilset) {
				return;
			}
			
			// Check if absolute or relative url
			if ((extension["definition"]||"").startsWith("/")){
				stencilset.addExtension(extension["definition"])
			} else {
				stencilset.addExtension(ORYX.CONFIG.SS_EXTENSIONS_FOLDER + extension["definition"])
			}
			
			//stencilset.addExtension("/oryx/build/stencilsets/extensions/" + extension["definition"])
			this.getRules().initializeRules(stencilset);
			
			this._getPluginFacade().raiseEvent({
				type: ORYX.CONFIG.EVENT_STENCIL_SET_LOADED
			});
		}
		
	},

	disableEvent: function(eventType){
		if(eventType == ORYX.CONFIG.EVENT_KEYDOWN) {
			this._keydownEnabled = false;
		}
		if(eventType == ORYX.CONFIG.EVENT_KEYUP) {
			this._keyupEnabled = false;
		}
		if(this.DOMEventListeners.keys().member(eventType)) {
			var value = this.DOMEventListeners.remove(eventType);
			this.DOMEventListeners['disable_' + eventType] = value;
		}
	},

	enableEvent: function(eventType){
		if(eventType == ORYX.CONFIG.EVENT_KEYDOWN) {
			this._keydownEnabled = true;
		}
		
		if(eventType == ORYX.CONFIG.EVENT_KEYUP) {
			this._keyupEnabled = true;
		}
		
		if(this.DOMEventListeners.keys().member("disable_" + eventType)) {
			var value = this.DOMEventListeners.remove("disable_" + eventType);
			this.DOMEventListeners[eventType] = value;
		}
	},

	/**
	 *  Methods for the PluginFacade
	 */
	registerOnEvent: function(eventType, callback) {
		if(!(this.DOMEventListeners.keys().member(eventType))) {
			this.DOMEventListeners[eventType] = [];
		}

		this.DOMEventListeners[eventType].push(callback);
	},

	unregisterOnEvent: function(eventType, callback) {
		if(this.DOMEventListeners.keys().member(eventType)) {
			this.DOMEventListeners[eventType] = this.DOMEventListeners[eventType].without(callback);
		} else {
			// Event is not supported
			// TODO: Error Handling
		}
	},

	getSelection: function() {
		return this.selection || [];
	},

	getStencilSets: function() { 
		return ORYX.Core.StencilSet.stencilSets(this.id); 
	},
	
	getRules: function() {
		return ORYX.Core.StencilSet.rules(this.id);
	},
	
	loadStencilSet: function(source) {
		try {
			ORYX.Core.StencilSet.loadStencilSet(source, this.modelMetaData, this.id);
			this.handleEvents({type:ORYX.CONFIG.EVENT_STENCIL_SET_LOADED});
		} catch (e) {
			ORYX.Log.warn("Requesting stencil set file failed. (" + e + ")");
		}
	},

	offer: function(pluginData) {
		if(!this.pluginsData.member(pluginData)){
			this.pluginsData.push(pluginData);
		}
	},
	
	/**
	 * It creates an new event or adds the callback, if already existing,
	 * for the key combination that the plugin passes in keyCodes attribute
	 * of the offer method.
	 * 
	 * The new key down event fits the schema:
	 * 		key.event[.metactrl][.alt][.shift].'thekeyCode'
	 */
	registerPluginsOnKeyEvents: function() {
		this.pluginsData.each(function(pluginData) {
			
			if(pluginData.keyCodes) {
				
				pluginData.keyCodes.each(function(keyComb) {
					var eventName = "key.event";
					
					/* Include key action */
					eventName += '.' + keyComb.keyAction;
					
					if(keyComb.metaKeys) {
						/* Register on ctrl or apple meta key as meta key */
						if(keyComb.metaKeys.
							indexOf(ORYX.CONFIG.META_KEY_META_CTRL) > -1) {
								eventName += "." + ORYX.CONFIG.META_KEY_META_CTRL;
						}
							
						/* Register on alt key as meta key */
						if(keyComb.metaKeys.
							indexOf(ORYX.CONFIG.META_KEY_ALT) > -1) {
								eventName += '.' + ORYX.CONFIG.META_KEY_ALT;
						}
						
						/* Register on shift key as meta key */
						if(keyComb.metaKeys.
							indexOf(ORYX.CONFIG.META_KEY_SHIFT) > -1) {
								eventName += '.' + ORYX.CONFIG.META_KEY_SHIFT;
						}		
					}
					
					/* Register on the actual key */
					if(keyComb.keyCode)	{
						eventName += '.' + keyComb.keyCode;
					}
					
					/* Register the event */
					ORYX.Log.debug("Register Plugin on Key Event: %0", eventName);
					if (pluginData.toggle === true && pluginData.buttonInstance) {
						this.registerOnEvent(eventName, function(){
							pluginData.buttonInstance.toggle(!pluginData.buttonInstance.pressed); // Toggle 
							pluginData.functionality.call(pluginData, pluginData.buttonInstance, pluginData.buttonInstance.pressed); // Call function
						});
					} else {
						this.registerOnEvent(eventName, pluginData.functionality)
					}
				
				}.bind(this));
			}
		}.bind(this));
	},
	
	isEqual: function(a,b){
		return a === b || (a.length === b.length && a.all(function(r){ return b.include(r) }))
	},
	
	isDirty: function(a){
		return a.any(function(shape){ return shape.isPropertyChanged() })
	},

	setSelection: function(elements, subSelectionElement, force) {
		
		if (!elements) { elements = []; }
		if (!(elements instanceof Array)) { elements = [elements]; }
		
		elements = elements.findAll(function(n){ return n && n instanceof ORYX.Core.Shape });
		
		if (elements[0] instanceof ORYX.Core.Canvas) {
			elements = [];
		}
		
		if (!force && this.isEqual(this.selection, elements) && !this.isDirty(elements)){
			return;
		}
		
		this.selection = elements;
		this._subSelection = subSelectionElement;
		
		this.handleEvents({type:ORYX.CONFIG.EVENT_SELECTION_CHANGED, elements:elements, subSelection: subSelectionElement, force: !!force})
	},
	
	updateSelection: function() {
		this.setSelection(this.selection, this._subSelection, true);
		/*var s = this.selection;
		this.setSelection();
		this.setSelection(s);*/
	},

	getCanvas: function() {
		return this._canvas;
	},
	

	/**
	*	option = {
	*		type: string,
	*		position: {x:int, y:int},
	*		connectingType:	uiObj-Class
	*		connectedShape: uiObj
	*		draggin: bool
	*		namespace: url
	*       parent: ORYX.Core.AbstractShape
	*		template: a template shape that the newly created inherits properties from.
	*		}
	*/
	createShape: function(option) {

		if(option && option.serialize && option.serialize instanceof Array){
		
			var type = option.serialize.find(function(obj){return (obj.prefix+"-"+obj.name) == "oryx-type"});
			var stencil = ORYX.Core.StencilSet.stencil(type.value);
			
			if(stencil.type() == 'node'){
				var newShapeObject = new ORYX.Core.Node({'eventHandlerCallback':this.handleEvents.bind(this)}, stencil, this._getPluginFacade());	
			} else {
				var newShapeObject = new ORYX.Core.Edge({'eventHandlerCallback':this.handleEvents.bind(this)}, stencil, this._getPluginFacade());	
			}
		
			this.getCanvas().add(newShapeObject);
			newShapeObject.deserialize(option.serialize);
		
			return newShapeObject;
		}

		// If there is no argument, throw an exception
		if(!option || !option.type || !option.namespace) { throw "To create a new shape you have to give an argument with type and namespace";}
		
		var canvas = this.getCanvas();
		var newShapeObject;

		// Get the shape type
		var shapetype = option.type;

		// Get the stencil set
		var sset = ORYX.Core.StencilSet.stencilSet(option.namespace);
		// Create an New Shape, dependents on an Edge or a Node
		if(sset.stencil(shapetype).type() == "node") {
			newShapeObject = new ORYX.Core.Node({'eventHandlerCallback':this.handleEvents.bind(this)}, sset.stencil(shapetype), this._getPluginFacade())
		} else {
			newShapeObject = new ORYX.Core.Edge({'eventHandlerCallback':this.handleEvents.bind(this)}, sset.stencil(shapetype), this._getPluginFacade())
		}
		
		// when there is a template, inherit the properties.
		if(option.template) {

			newShapeObject._jsonStencil.properties = option.template._jsonStencil.properties;
			newShapeObject.postProcessProperties();
		}

		// Add to the canvas
		if(option.parent && newShapeObject instanceof ORYX.Core.Node) {
			option.parent.add(newShapeObject);
		} else {
			canvas.add(newShapeObject);
		}
		
		
		// Set the position
		var point = option.position ? option.position : {x:100, y:200};
	
		
		var con;
		// If there is create a shape and in the argument there is given an ConnectingType and is instance of an edge
		if(option.connectingType && option.connectedShape && !(newShapeObject instanceof ORYX.Core.Edge)) {

			// there will be create a new Edge
			con = new ORYX.Core.Edge({'eventHandlerCallback':this.handleEvents.bind(this)}, sset.stencil(option.connectingType));
			
			// And both endings dockers will be referenced to the both shapes
			con.dockers.first().setDockedShape(option.connectedShape);
			
			var magnet = option.connectedShape.getDefaultMagnet()
			var cPoint = magnet ? magnet.bounds.center() : option.connectedShape.bounds.midPoint();
			con.dockers.first().setReferencePoint( cPoint );
			con.dockers.last().setDockedShape(newShapeObject);
			con.dockers.last().setReferencePoint(newShapeObject.getDefaultMagnet().bounds.center());		
			
			// The Edge will be added to the canvas and be updated
			canvas.add(con);	
			//con.update();
			
		} 
		
		// Move the new Shape to the position
		if(newShapeObject instanceof ORYX.Core.Edge && option.connectedShape) {

			newShapeObject.dockers.first().setDockedShape(option.connectedShape);
			
			if( option.connectedShape instanceof ORYX.Core.Node ){
				newShapeObject.dockers.first().setReferencePoint(option.connectedShape.getDefaultMagnet().bounds.center());					
				newShapeObject.dockers.last().bounds.centerMoveTo(point);			
			} else {
				newShapeObject.dockers.first().setReferencePoint(option.connectedShape.bounds.midPoint());								
			}
			
			var start = newShapeObject.dockers.first();
        	var end = newShapeObject.dockers.last();
        	
        	if(start.getDockedShape() && end.getDockedShape()) {
        		var startPoint = start.getAbsoluteReferencePoint();
        		var endPoint = end.getAbsoluteReferencePoint();
        		
        		var docker = newShapeObject.createDocker();
        		docker.bounds.centerMoveTo({
        			x: startPoint.x + (endPont.x - startPoint.x) / 2,
        			y: startPoint.y + (endPont.y - startPoint.y) / 2
        		});
        	}

		} else {
			
			var b = newShapeObject.bounds
			if( newShapeObject instanceof ORYX.Core.Node && newShapeObject.dockers.length == 1){
				b = newShapeObject.dockers.first().bounds
			}
			
			b.centerMoveTo(point);
			
			var upL = b.upperLeft();
			b.moveBy( -Math.min(upL.x, 0) , -Math.min(upL.y, 0) )
			
			var lwR = b.lowerRight();
			b.moveBy( -Math.max(lwR.x-canvas.bounds.width(), 0) , -Math.max(lwR.y-canvas.bounds.height(), 0) )
			
		}
		
		// Update the shape
		if (newShapeObject instanceof ORYX.Core.Edge) {
			newShapeObject._update(false);
		}
		
		// And refresh the selection
		if(!(newShapeObject instanceof ORYX.Core.Edge)&&!(option.dontUpdateSelection)) {
			this.setSelection([newShapeObject]);
		}
		
		if(con && con.alignDockers) {
			//con.alignDockers();
		} 
		if(newShapeObject.alignDockers) {
			newShapeObject.alignDockers();
		}

		return newShapeObject;
	},
	
	deleteShape: function(shape) {
		
		if (!shape || !shape.parent){ return }
		
		//remove shape from parent
		// this also removes it from DOM
		shape.parent.remove(shape);
		
		//delete references to outgoing edges
		shape.getOutgoingShapes().each(function(os) {
			var docker = os.getDockers().first();
			if(docker && docker.getDockedShape() == shape) {
				docker.setDockedShape(undefined);
			}
		});
		
		//delete references to incoming edges
		shape.getIncomingShapes().each(function(is) {
			var docker = is.getDockers().last();
			if(docker && docker.getDockedShape() == shape) {
				docker.setDockedShape(undefined);
			}
		});
		
		//delete references of the shape's dockers
		shape.getDockers().each(function(docker) {
			docker.setDockedShape(undefined);
		});
	},
	
	/**
	 * Returns an object with meta data about the model.
	 * Like name, description, ...
	 * 
	 * Empty object with the current backend.
	 * 
	 * @return {Object} Meta data about the model
	 */
	getModelMetaData: function() {
		return this.modelMetaData;
	},

	/* Event-Handler Methods */
	
	/**
	* Helper method to execute an event immediately. The event is not
	* scheduled in the _eventsQueue. Needed to handle Layout-Callbacks.
	*/
	_executeEventImmediately: function(eventObj) {
		if(this.DOMEventListeners.keys().member(eventObj.event.type)) {
			this.DOMEventListeners[eventObj.event.type].each((function(value) {
				value(eventObj.event, eventObj.arg);		
			}).bind(this));
		}
	},

	_executeEvents: function() {
		this._queueRunning = true;
		while(this._eventsQueue.length > 0) {
			var val = this._eventsQueue.shift();
			this._executeEventImmediately(val);
		}
		this._queueRunning = false;
	},
	
	/**
	 * Leitet die Events an die Editor-Spezifischen Event-Methoden weiter
	 * @param {Object} event Event , welches gefeuert wurde
	 * @param {Object} uiObj Target-UiObj
	 */
	handleEvents: function(event, uiObj) {
		
		ORYX.Log.trace("Dispatching event type %0 on %1", event.type, uiObj);

		switch(event.type) {
			case ORYX.CONFIG.EVENT_MOUSEDOWN:
				this._handleMouseDown(event, uiObj);
				break;
			case ORYX.CONFIG.EVENT_MOUSEMOVE:
				this._handleMouseMove(event, uiObj);
				break;
			case ORYX.CONFIG.EVENT_MOUSEUP:
				this._handleMouseUp(event, uiObj);
				break;
			case ORYX.CONFIG.EVENT_MOUSEOVER:
				this._handleMouseHover(event, uiObj);
				break;
			case ORYX.CONFIG.EVENT_MOUSEOUT:
				this._handleMouseOut(event, uiObj);
				break;
		}
		/* Force execution if necessary. Used while handle Layout-Callbacks. */
		if(event.forceExecution) {
			this._executeEventImmediately({event: event, arg: uiObj});
		} else {
			this._eventsQueue.push({event: event, arg: uiObj});
		}
		
		if(!this._queueRunning) {
			this._executeEvents();
		}
		
		// TODO: Make this return whether no listener returned false.
		// So that, when one considers bubbling undesireable, it won't happen.
		return false;
	},

	isValidEvent: function(e){
		try {
			var isInput = ["INPUT", "TEXTAREA"].include(e.target.tagName.toUpperCase());
			var gridHasFocus = e.target.className.include("x-grid3-focus") && !e.target.className.include("x-grid3-focus-canvas");
			return !isInput && !gridHasFocus;
		} catch(e){
			return false;
		}
	},

	catchKeyUpEvents: function(event) {
		if(!this._keyupEnabled) {
			return;
		}
		/* assure we have the current event. */
        if (!event) 
            event = window.event;
        
		// Checks if the event comes from some input field
		if (!this.isValidEvent(event)){
			return;
		}
		
		/* Create key up event type */
		var keyUpEvent = this.createKeyCombEvent(event,	ORYX.CONFIG.KEY_ACTION_UP);
		
		ORYX.Log.debug("Key Event to handle: %0", keyUpEvent);

		/* forward to dispatching. */
		this.handleEvents({type: keyUpEvent, event:event});
	},
	
	/**
	 * Catches all key down events and forward the appropriated event to 
	 * dispatching concerning to the pressed keys.
	 * 
	 * @param {Event} 
	 * 		The key down event to handle
	 */
	catchKeyDownEvents: function(event) {
		if(!this._keydownEnabled) {
			return;
		}
		/* Assure we have the current event. */
        if (!event) 
            event = window.event;
        
		/* Fixed in FF3 */
		// This is a mac-specific fix. The mozilla event object has no knowledge
		// of meta key modifier on osx, however, it is needed for certain
		// shortcuts. This fix adds the metaKey field to the event object, so
		// that all listeners that registered per Oryx plugin facade profit from
		// this. The original bug is filed in
		// https://bugzilla.mozilla.org/show_bug.cgi?id=418334
		//if (this.__currentKey == ORYX.CONFIG.KEY_CODE_META) {
		//	event.appleMetaKey = true;
		//}
		//this.__currentKey = pressedKey;
		
		// Checks if the event comes from some input field
		if (!this.isValidEvent(event)){
			return;
		}
		
		/* Create key up event type */
		var keyDownEvent = this.createKeyCombEvent(event, ORYX.CONFIG.KEY_ACTION_DOWN);
		
		ORYX.Log.debug("Key Event to handle: %0", keyDownEvent);
		
		/* Forward to dispatching. */
		this.handleEvents({type: keyDownEvent,event: event});
	},
	
	/**
	 * Creates the event type name concerning to the pressed keys.
	 * 
	 * @param {Event} keyDownEvent
	 * 		The source keyDownEvent to build up the event name
	 */
	createKeyCombEvent: function(keyEvent, keyAction) {

		/* Get the currently pressed key code. */
        var pressedKey = keyEvent.which || keyEvent.keyCode;
		//this.__currentKey = pressedKey;
		
		/* Event name */
		var eventName = "key.event";
		
		/* Key action */
		if(keyAction) {
			eventName += "." + keyAction;
		}
		
		/* Ctrl or apple meta key is pressed */
		if(keyEvent.ctrlKey || keyEvent.metaKey) {
			eventName += "." + ORYX.CONFIG.META_KEY_META_CTRL;
		}
		
		/* Alt key is pressed */
		if(keyEvent.altKey) {
			eventName += "." + ORYX.CONFIG.META_KEY_ALT;
		}
		
		/* Alt key is pressed */
		if(keyEvent.shiftKey) {
			eventName += "." + ORYX.CONFIG.META_KEY_SHIFT;
		}
		
		/* Return the composed event name */
		return  eventName + "." + pressedKey;
	},

	_handleMouseDown: function(event, uiObj) {
		
		// get canvas.
		var canvas = this.getCanvas();
		// Try to get the focus
		canvas.focus()
	
		// find the shape that is responsible for this element's id.
		var element = event.currentTarget;
		var elementController = uiObj;

		// gather information on selection.
		var currentIsSelectable = (elementController !== null) &&
			(elementController !== undefined) && (elementController.isSelectable);
		var currentIsMovable = (elementController !== null) &&
			(elementController !== undefined) && (elementController.isMovable);
		var modifierKeyPressed = event.shiftKey || event.ctrlKey;
		var noObjectsSelected = this.selection.length === 0;
		var currentIsSelected = this.selection.member(elementController);


		// Rule #1: When there is nothing selected, select the clicked object.
		if(currentIsSelectable && noObjectsSelected) {

			this.setSelection([elementController]);

			ORYX.Log.trace("Rule #1 applied for mouse down on %0", element.id);

		// Rule #3: When at least one element is selected, and there is no
		// control key pressed, and the clicked object is not selected, select
		// the clicked object.
		} else if(currentIsSelectable && !noObjectsSelected &&
			!modifierKeyPressed && !currentIsSelected) {

			this.setSelection([elementController]);

			//var objectType = elementController.readAttributes();
			//alert(objectType[0] + ": " + objectType[1]);

			ORYX.Log.trace("Rule #3 applied for mouse down on %0", element.id);

		// Rule #4: When the control key is pressed, and the current object is
		// not selected, add it to the selection.
		} else if(currentIsSelectable && modifierKeyPressed
			&& !currentIsSelected) {
				
			var newSelection = this.selection.clone();
			newSelection.push(elementController)
			this.setSelection(newSelection)

			ORYX.Log.trace("Rule #4 applied for mouse down on %0", element.id);

		// Rule #6
		} else if(currentIsSelectable && currentIsSelected &&
			modifierKeyPressed) {

			var newSelection = this.selection.clone();
			this.setSelection(newSelection.without(elementController))

			ORYX.Log.trace("Rule #6 applied for mouse down on %0", elementController.id);

		// Rule #5: When there is at least one object selected and no control
		// key pressed, we're dragging.
		/*} else if(currentIsSelectable && !noObjectsSelected
			&& !modifierKeyPressed) {

			if(this.log.isTraceEnabled())
				this.log.trace("Rule #5 applied for mouse down on "+element.id);
*/
		// Rule #2: When clicked on something that is neither
		// selectable nor movable, clear the selection, and return.
		} else if (!currentIsSelectable && !currentIsMovable) {
			
			this.setSelection([]);
			
			ORYX.Log.trace("Rule #2 applied for mouse down on %0", element.id);

			return;

		// Rule #7: When the current object is not selectable but movable,
		// it is probably a control. Leave the selection unchanged but set
		// the movedObject to the current one and enable Drag. Dockers will
		// be processed in the dragDocker plugin.
		} else if(!currentIsSelectable && currentIsMovable && !(elementController instanceof ORYX.Core.Controls.Docker)) {
			
			// TODO: If there is any moveable elements, do this in a plugin
			//ORYX.Core.UIEnableDrag(event, elementController);

			ORYX.Log.trace("Rule #7 applied for mouse down on %0", element.id);
		
		// Rule #8: When the element is selectable and is currently selected and no 
		// modifier key is pressed
		} else if(currentIsSelectable && currentIsSelected &&
			!modifierKeyPressed) {
			
			this._subSelection = this._subSelection != elementController ? elementController : undefined;
						
			this.setSelection(this.selection, this._subSelection);
			
			ORYX.Log.trace("Rule #8 applied for mouse down on %0", element.id);
		}
		
		
		// prevent event from bubbling, return.
		//Event.stop(event);
		return;
	},

	_handleMouseMove: function(event, uiObj) {
		return;
	},

	_handleMouseUp: function(event, uiObj) {
		// get canvas.
		var canvas = this.getCanvas();

		// find the shape that is responsible for this elemement's id.
		var elementController = uiObj;

		//get event position
		var evPos = this.eventCoordinates(event);

		//Event.stop(event);
	},

	_handleMouseHover: function(event, uiObj) {
		return;
	},

	_handleMouseOut: function(event, uiObj) {
		return;
	},

	/**
	 * Calculates the event coordinates to SVG document coordinates.
	 * @param {Event} event
	 * @return {SVGPoint} The event coordinates in the SVG document
	 */
	eventCoordinates: function(event) {

		var canvas = this.getCanvas();

		var svgPoint = canvas.node.ownerSVGElement.createSVGPoint();
		svgPoint.x = event.clientX;
		svgPoint.y = event.clientY;
		
		var additionalIEZoom = 1;
        if (!isNaN(screen.logicalXDPI) && !isNaN(screen.systemXDPI)) {
            var ua = navigator.userAgent;
            if (ua.indexOf('MSIE') >= 0) {
                //IE 10 and below
                var zoom = Math.round((screen.deviceXDPI / screen.logicalXDPI) * 100);
                if (zoom !== 100) {
                    additionalIEZoom = zoom / 100
                }
            }
        }
        
        if (additionalIEZoom !== 1) {
            svgPoint.x = svgPoint.x * additionalIEZoom;
            svgPoint.y = svgPoint.y * additionalIEZoom;
        }
		
		var matrix = canvas.node.getScreenCTM();
		return svgPoint.matrixTransform(matrix.inverse());
	},
	
	eventCoordinatesXY: function(x, y) {

		var canvas = this.getCanvas();

		var svgPoint = canvas.node.ownerSVGElement.createSVGPoint();
		svgPoint.x = x;
		svgPoint.y = y;
		
		var additionalIEZoom = 1;
        if (!isNaN(screen.logicalXDPI) && !isNaN(screen.systemXDPI)) {
            var ua = navigator.userAgent;
            if (ua.indexOf('MSIE') >= 0) {
                //IE 10 and below
                var zoom = Math.round((screen.deviceXDPI / screen.logicalXDPI) * 100);
                if (zoom !== 100) {
                    additionalIEZoom = zoom / 100
                }
            }
        }
        
        if (additionalIEZoom !== 1) {
            svgPoint.x = svgPoint.x * additionalIEZoom;
            svgPoint.y = svgPoint.y * additionalIEZoom;
        }
		
		var matrix = canvas.node.getScreenCTM();
		return svgPoint.matrixTransform(matrix.inverse());
	}
};
ORYX.Editor = Clazz.extend(ORYX.Editor);

/**
 * Creates a new ORYX.Editor instance by fetching a model from given url and passing it to the constructur
 * @param {String} modelUrl The JSON URL of a model.
 * @param {Object} config Editor config passed to the constructur, merged with the response of the request to modelUrl
 */
ORYX.Editor.createByUrl = function(modelUrl){
    new Ajax.Request(modelUrl, {
      method: 'GET',
      onSuccess: function(transport) {
    	var editorConfig = JSON.parse(transport.responseText);
        new ORYX.Editor(editorConfig);
      }.bind(this)
    });
}

// TODO Implement namespace awareness on attribute level.
/**
 * graft() function
 * Originally by Sean M. Burke from interglacial.com, altered for usage with
 * SVG and namespace (xmlns) support. Be sure you understand xmlns before
 * using this funtion, as it creates all grafted elements in the xmlns
 * provided by you and all element's attribures in default xmlns. If you
 * need to graft elements in a certain xmlns and wish to assign attributes
 * in both that and another xmlns, you will need to do stepwise grafting,
 * adding non-default attributes yourself or you'll have to enhance this
 * function. Latter, I would appreciate: martin�apfelfabrik.de
 * @param {Object} namespace The namespace in which
 * 					elements should be grafted.
 * @param {Object} parent The element that should contain the grafted
 * 					structure after the function returned.
 * @param {Object} t the crafting structure.
 * @param {Object} doc the document in which grafting is performed.
 */
ORYX.Editor.graft = function(namespace, parent, t, doc) {

    doc = (doc || (parent && parent.ownerDocument) || document);
    var e;
    if(t === undefined) {
        throw "Can't graft an undefined value";
    } else if(t.constructor == String) {
        e = doc.createTextNode( t );
    } else {
        for(var i = 0; i < t.length; i++) {
            if( i === 0 && t[i].constructor == String ) {
                var snared;
                snared = t[i].match( /^([a-z][a-z0-9]*)\.([^\s\.]+)$/i );
                if( snared ) {
                    e = doc.createElementNS(namespace, snared[1] );
                    e.setAttributeNS(null, 'class', snared[2] );
                    continue;
                }
                snared = t[i].match( /^([a-z][a-z0-9]*)$/i );
                if( snared ) {
                    e = doc.createElementNS(namespace, snared[1] );  // but no class
                    continue;
                }

                // Otherwise:
                e = doc.createElementNS(namespace, "span" );
                e.setAttribute(null, "class", "namelessFromLOL" );
            }

            if( t[i] === undefined ) {
                throw "Can't graft an undefined value in a list!";
            } else if( t[i].constructor == String || t[i].constructor == Array ) {
                this.graft(namespace, e, t[i], doc );
            } else if(  t[i].constructor == Number ) {
                this.graft(namespace, e, t[i].toString(), doc );
            } else if(  t[i].constructor == Object ) {
                // hash's properties => element's attributes
                for(var k in t[i]) { e.setAttributeNS(null, k, t[i][k] ); }
            } else {

			}
        }
    }
	if(parent && parent.appendChild) {
	    parent.appendChild( e );
	} else {

	}
    return e; // return the topmost created node
};

ORYX.Editor.provideId = function() {
	var res = [], hex = '0123456789ABCDEF';

	for (var i = 0; i < 36; i++) res[i] = Math.floor(Math.random()*0x10);

	res[14] = 4;
	res[19] = (res[19] & 0x3) | 0x8;

	for (var i = 0; i < 36; i++) res[i] = hex[res[i]];

	res[8] = res[13] = res[18] = res[23] = '-';

	return "oryx_" + res.join('');
};

/**
 * When working with Ext, conditionally the window needs to be resized. To do
 * so, use this class method. Resize is deferred until 100ms, and all subsequent
 * resizeBugFix calls are ignored until the initially requested resize is
 * performed.
 */
ORYX.Editor.resizeFix = function() {
	if (!ORYX.Editor._resizeFixTimeout) {
		ORYX.Editor._resizeFixTimeout = window.setTimeout(function() {
			window.resizeBy(1,1);
			window.resizeBy(-1,-1);
			ORYX.Editor._resizefixTimeout = null;
		}, 100); 
	}
};

ORYX.Editor.Cookie = {
	
	callbacks:[],
		
	onChange: function( callback, interval ){
	
		this.callbacks.push(callback);
		this.start( interval )
	
	},
	
	start: function( interval ){
		
		if( this.pe ){
			return;
		}
		
		var currentString = document.cookie;
		
		this.pe = new PeriodicalExecuter( function(){
			
			if( currentString != document.cookie ){
				currentString = document.cookie;
				this.callbacks.each(function(callback){ callback(this.getParams()) }.bind(this));
			}
			
		}.bind(this), ( interval || 10000 ) / 1000);	
	},
	
	stop: function(){

		if( this.pe ){
			this.pe.stop();
			this.pe = null;
		}
	},
		
	getParams: function(){
		var res = {};
		
		var p = document.cookie;
		p.split("; ").each(function(param){ res[param.split("=")[0]] = param.split("=")[1];});
		
		return res;
	},	
	
	toString: function(){
		return document.cookie;
	}
};

/**
 * Workaround for SAFARI/Webkit, because
 * when trying to check SVGSVGElement of instanceof there is 
 * raising an error
 * 
 */
ORYX.Editor.SVGClassElementsAreAvailable = true;
ORYX.Editor.setMissingClasses = function() {
	
	try {
		SVGElement;
	} catch(e) {
		ORYX.Editor.SVGClassElementsAreAvailable = false;
		SVGSVGElement 		= document.createElementNS('http://www.w3.org/2000/svg', 'svg').toString();
		SVGGElement 		= document.createElementNS('http://www.w3.org/2000/svg', 'g').toString();
		SVGPathElement 		= document.createElementNS('http://www.w3.org/2000/svg', 'path').toString();
		SVGTextElement 		= document.createElementNS('http://www.w3.org/2000/svg', 'text').toString();
		//SVGMarkerElement 	= document.createElementNS('http://www.w3.org/2000/svg', 'marker').toString();
		SVGRectElement 		= document.createElementNS('http://www.w3.org/2000/svg', 'rect').toString();
		SVGImageElement 	= document.createElementNS('http://www.w3.org/2000/svg', 'image').toString();
		SVGCircleElement 	= document.createElementNS('http://www.w3.org/2000/svg', 'circle').toString();
		SVGEllipseElement 	= document.createElementNS('http://www.w3.org/2000/svg', 'ellipse').toString();
		SVGLineElement	 	= document.createElementNS('http://www.w3.org/2000/svg', 'line').toString();
		SVGPolylineElement 	= document.createElementNS('http://www.w3.org/2000/svg', 'polyline').toString();
		SVGPolygonElement 	= document.createElementNS('http://www.w3.org/2000/svg', 'polygon').toString();
		
	}
	
}
ORYX.Editor.checkClassType = function( classInst, classType ) {
	
	if( ORYX.Editor.SVGClassElementsAreAvailable ){
		return classInst instanceof classType
	} else {
		return classInst == classType
	}
};
