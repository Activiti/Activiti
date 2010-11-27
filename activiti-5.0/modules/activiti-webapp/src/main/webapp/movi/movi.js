/**
 * Copyright (c) 2009
 * Jan-Felix Schwarz
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

// Declare the MOVI global namespace
if (typeof MOVI == "undefined" || !MOVI) { var MOVI = {}; }

/**
 * Declare the movi module (needed for doc generation)
 * @module movi
 */


/**
 * Returns the namespace specified and creates it if it doesn't exist
 * <pre>
 * MOVI.namespace("property.package");
 * MOVI.namespace("MOVI.property.package");
 * </pre>
 * Either of the above would create MOVI.property, then
 * MOVI.property.package
 *
 * Be careful when naming packages. Reserved words may work in some browsers
 * and not others. For instance, the following will fail in Safari:
 * <pre>
 * MOVI.namespace("really.long.nested.namespace");
 * </pre>
 * This fails because "long" is a future reserved word in ECMAScript
 *
 * This method is taken from YUI's YAHOO object.
 *
 * @method namespace
 * @static
 * @param  {String*} arguments 1-n namespaces to create 
 * @return {Object}  A reference to the last namespace object created
 */
MOVI.namespace = function() {
    var a=arguments, o=null, i, j, d;
    for (i=0; i<a.length; i=i+1) {
        d=a[i].split(".");
        o=MOVI;

        // MOVI is implied, so it is ignored if it is included
        for (j=(d[0] == "MOVI") ? 1 : 0; j<d.length; j=j+1) {
            o[d[j]]=o[d[j]] || {};
            o=o[d[j]];
        }
    }

    return o;
};

// Adopt Yahoo's inheritance mechanism
MOVI.extend = YAHOO.extend;

// Adopt Yahoo's logger
MOVI.log = YAHOO.log;

/**
 * Loads the required YUI resources and all MOVI scripts. When loading
 * is finished the specified callback is executed.
 *
 * Use this method to initialize MOVI and wrap your MOVI specific code
 * in the callback function passed to this method.
 * <pre>
 * MOVI.init(function() {
 *     var modelViewer = new MOVI.widget.ModelViewer(YAHOO.util.Dom.get("myMoviDivId"));
 * }, "script/movi", setUpMyUI, ["editor"]);
 * </pre>
 *
 * @method init
 * @static
 * @param {Function} modelReadyCallback The function to call after loading of the model is finished
 * @param {String} moviBase The path to the base directory of MOVI.
 * @param {Function} yuiReadyCallback (optional) The function to call after loading of the YUI modules is finished
 * @param {String*} yuiModules (optional) Additional YUI modules to load. By default MOVI loads 'yahoo', 'dom', 'event', 
 * 'get', 'event', 'logger', 'resize'. Use this parameter if your script depends on other YUI modules.
 */
MOVI.init = function(callback, moviBase, yuiReadyCallback, yuiModules) {
	
	var _YUI_BASE_DIR = "http://yui.yahooapis.com/2.7.0/build/";
	
	if(!YAHOO.lang.isFunction(callback)) callback = function(){};
	
	if(!YAHOO.lang.isFunction(yuiReadyCallback)) yuiReadyCallback = undefined;
	
	if(!YAHOO.lang.isArray(yuiModules)) yuiModules = [];
	
	// load required YUI modules
	var yuiLoader = new YAHOO.util.YUILoader({
		require: yuiModules.concat(["yahoo", "dom", "element", "get", "event", "logger", "slider", "container"]), 
		base: _YUI_BASE_DIR,
		loadOptional: true,
		filter: "RAW",
		onSuccess: function() {
			
			if(yuiReadyCallback)
				yuiReadyCallback();
			
			YAHOO.widget.Logger.enableBrowserConsole();  // remove this line for production use 
			
			if(!YAHOO.lang.isString(moviBase)) {
				throw new Error("MOVI base directory is not specified.", "movi.js");
				return false;
			}
			
			YAHOO.util.Get.css(moviBase + "/style/movi.css", {});
			if (YAHOO.env.ua.ie > 0) { 
				// load custom stylesheets for IE
				YAHOO.util.Get.css(moviBase + "/style/movi_ie.css", {});
			}
			
			// load MOVI modules in correct order to satisfy dependencies
			// path references are relative to YUI root directory
			YAHOO.util.Get.script([ moviBase + "/config.js", 
									moviBase + "/modelviewer.js", 
									moviBase + "/stencil.js", 
									moviBase + "/stencilset.js", 
									moviBase + "/shape.js", 
									moviBase + "/node.js", 
									moviBase + "/edge.js", 
									moviBase + "/canvas.js",
									moviBase + "/marker.js", 
									moviBase + "/annotation.js",
									moviBase + "/modelnavigator.js", 
									moviBase + "/shapeselect.js", 
									moviBase + "/toolbar.js",
									moviBase + "/zoom.js" ], {
				onSuccess: callback,  // execute user specified callback
				onFailure: function() {
					throw new Error("Unable to load MOVI modules from base dir '" + moviBase + "'", "movi.js");
				}
			});
		},
		onFailure: function(msg, xhrobj) { 
			var m = "Unable to load YUI modules from base dir '" + _YUI_BASE_DIR + "': " + msg; 
			if (xhrobj) { 
				m += ", " + YAHOO.lang.dump(xhrobj); 
			} 
			throw new Error(m, "movi.js"); 
		}
	});
	
	yuiLoader.insert();
};/**
 * Copyright (c) 2009
 * Jan-Felix Schwarz
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


// Declare the MOVI.config namespace
MOVI.namespace("config");

MOVI.config = {
	
	/**
	 * The margin of the model's PNG image
	 * @property modelBorder
	 */
	MODEL_MARGIN: 50 // equals variable margin in Oryx' Canvas#getSVGRepresentation()
	
}/**
 * Copyright (c) 2009
 * Jan-Felix Schwarz
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

// Declare the MOVI.widget namespace
MOVI.namespace("widget");

(function() {
	
	var _SCROLLBOX_CLASS_NAME = "movi-scrollbox",
		_MODELIMG_CLASS_NAME = "movi-modelimg";
	
	/**
     * Array to keep track of all instances
	 * @property _instances
     * @private
     */
	var _instances = new Array();

    /**
     * A widget to display an Oryx model
     * @namespace MOVI.widget
     * @class MOVI.widget.ModelViewer
     * @extends YAHOO.util.Element
     * @constructor
     * @param {HTMLElement | String} el The id of the container DIV element that will 
 	 * wrap the ModelViewer, or a reference to a DIV element. The DIV element must
	 * exist in the document.
     */
    MOVI.widget.ModelViewer = function(el) {

		// register the new instance
		this._index = _instances.length;
		_instances[this._index] = this;
		
    	MOVI.widget.ModelViewer.superclass.constructor.call(this, el); 

		this.onZoomLevelChange = new YAHOO.util.CustomEvent("movi-zoomLevelChange", this); 
		this.onZoomLevelChangeStart = new YAHOO.util.CustomEvent("movi-zoomLevelChangeStart", this); 
		this.onZoomLevelChangeEnd = new YAHOO.util.CustomEvent("movi-zoomLevelChangeEnd", this); 
		
		this.onModelLoadStart = new YAHOO.util.CustomEvent("movi-modelLoadStart", this); 
		this.onModelLoadEnd = new YAHOO.util.CustomEvent("movi-modelLoadEnd", this); 

		var existingScrollboxArr = this.getElementsByClassName(_SCROLLBOX_CLASS_NAME);
		if(existingScrollboxArr.length==1) {
			// use existing scrollbox element if available
			this._scrollbox = new YAHOO.util.Element(existingScrollboxArr[0]);
		} else {
			// otherwise create the scrollbox element
			this._scrollbox = new YAHOO.util.Element(document.createElement("div"));
			this._scrollbox.addClass(_SCROLLBOX_CLASS_NAME);
			this.appendChild(this._scrollbox);
		}
		
		this._scrollbox.set("id", _SCROLLBOX_CLASS_NAME + this._index);
		var imgEl = new YAHOO.util.Element(document.createElement("img"));
		imgEl.set("id", _MODELIMG_CLASS_NAME + this._index);
		imgEl.addClass(_MODELIMG_CLASS_NAME);
		this._scrollbox.appendChild(imgEl);
							
		// prevent marking
		this.setStyle("-moz-user-select", "-moz-none");    // Gecko-based, Mozilla
		this.setStyle("-webkit-user-select", "none"); // Safari 3.0
		this.setStyle("-khtml-user-select", "none");  // Safari 2.0
		if (YAHOO.env.ua.ie > 0)
			this.set("unselectable", "on");           // IE
		
		this._image = new YAHOO.util.Element(_MODELIMG_CLASS_NAME + this._index);
		
		// callbacks for model dragging
		this.addListener("mousedown", this._onMouseDown, this, this, true);
		YAHOO.util.Event.addListener(document, "mouseup", this._onMouseUp, this, this, true);
			
		this._loadOptions = {};
    };

	/**
     * Returns the ModelViewer instance with the specified id.
     * @method getInstance
     * @param {Integer | String} id The id of the ModelViewer instance, 
	 * or the id of the instance's host DIV element.
     * @return {MOVI.widget.ModelViewer} A ModelViewer instance
     */
	MOVI.widget.ModelViewer.getInstance = function(id) {
		if(YAHOO.lang.isNumber(id)) {
			return _instances[id];
		} else {
			for(var i = 0; i < _instances.length; i++) {
				if(_instances[i].get("id")==id)
					return _instances[i];
			}
		}
	};
	
	/**
     * Remove the ModelViewer instance with the specified id.
     * @method removeInstance
     * @param {Integer | String} id The id of the ModelViewer instance
     */
	MOVI.widget.ModelViewer.removeInstance = function(id) {
		if(YAHOO.lang.isNumber(id)) {
			YAHOO.util.Event.purgeElement(_instances[id].get("element"), true);
			_instances[id].get("element").innerHTML = "";
			delete _instances[id];
			_instances.splice(id, 1);
		}
	};
	
 	MOVI.extend(MOVI.widget.ModelViewer, YAHOO.util.Element, {
	
		/**
	     * URI of the model to display
	     * @property _modelUri
		 * @type String
		 * @private
	     */
		_modelUri: "",
		
		/**
	     * Options passed to loadModel method
	     * @property _loadOptions
		 * @type Object
		 * @private
	     */
		_loadOptions: null,
		
		/**
	     * The canvas container Element realizing scrolling
	     * @property _scrollbox
		 * @type Element
		 * @private
	     */
		_scrollbox: null,
		
		/**
	     * The image element
	     * @property _image
		 * @type Element
		 * @private
	     */
		_image: null,
		
		/**
	     * The original width of the image
	     * @property _imageWidth
		 * @type Integer
		 * @private
	     */
		_imageWidth: 0,
		
		/**
	     * The original height of the image
	     * @property _imageHeight
		 * @type Integer
		 * @private
	     */
		_imageHeight: 0,
		
		/**
	     * The model zoom level in percent
	     * @property _zoomLevel
		 * @type Number
		 * @private
	     */
		_zoomLevel: 100,
		
		/**
	     * An array that stores tokens that identify resources to load. Loading of these resources is 
		 * synchronized before the model load success callback is executed
	     * @property _syncResources
		 * @type Array
		 * @private
	     */
		_syncResources: null,
		
		/**
		 * The model's canvas
		 * @property canvas
		 * @type Canvas
		 */
		canvas: null,
		
		/**
		 * The event that is triggered when the model zoom level changes
		 * @property onZoomLevelChange
		 * @type YAHOO.util.CustomEvent
		 */
		onZoomLevelChange: null,
		
		/**
		 * The event that is triggered when the model zoom level change starts
		 * (on zoom slider slideStart event)
		 * @property onZoomLevelChangeStart
		 * @type YAHOO.util.CustomEvent
		 */
		onZoomLevelChangeStart: null,
		
		/**
		 * The event that is triggered when the model zoom level change is finished
		 * (on zoom slider slideEnd event)
		 * @property onZoomLevelChangeEnd
		 * @type YAHOO.util.CustomEvent
		 */
		onZoomLevelChangeEnd: null,
		
		/**
		 * The event that is triggered when the model loading starts
		 * @property onModelLoadStart
		 * @type YAHOO.util.CustomEvent
		 */
		onModelLoadStart: null,
		
		/**
		 * The event that is triggered when the model loading is finished
		 * @property onModelLoadEnd
		 * @type YAHOO.util.CustomEvent
		 */
		onModelLoadEnd: null,
		
		/**
	     * Callback that is executed when the model is finished
		 * loading.
	     * @method _onSuccess
		 * @private
	     */
		_onSuccess: function() {
			MOVI.log("Model loaded successfully.", "info", "modelviewer.js");
			var scope = this._loadOptions.scope || window;
			if(this._loadOptions.onSuccess)
				this._loadOptions.onSuccess.call(scope, this);
			this.onModelLoadEnd.fire(this._modelUri);
			this.onZoomLevelChangeEnd.fire(this.getZoomLevel());
		},
		
		/**
	     * Callback for handling model load failures.
	     * @method _onLoadFailure
		 * @private
	     */
		_onLoadFailure: function() {
			MOVI.log("Could not load model.", "error", "modelviewer.js");
			var scope = this._loadOptions.scope || window;
			if(this._loadOptions.onFailure)
				this._loadOptions.onFailure.call(scope, this);
		},
		
		/**
	     * Callback for handling model load timeouts.
	     * @method _onLoadTimeout
		 * @private
	     */
		_onLoadTimeout: function() {
			MOVI.log("A timeout occured while trying to load model.", "error", "modelviewer.js");
			var scope = this._loadOptions.scope || window;
			if(this._loadOptions.onTimeout)
				this._loadOptions.onTimeout.call(scope, this);
			else if(this._loadOptions.onFailure)
				this._loadOptions.onFailure.call(scope, this);
		},
		
		/**
	     * Callback for handling stencil set load failures.
	     * @method _onStencilSetLoadFailure
		 * @private
	     */
		_onStencilSetLoadFailure: function() {
			MOVI.log("Could not load stencil set for model.", "error", "modelviewer.js");
			var scope = this._loadOptions.scope || window;
			if(this._loadOptions.onFailure)
				this._loadOptions.onFailure.call(scope, this);
		},
		
		/**
	     * Callback for handling stencil set load timeouts.
	     * @method _onStencilSetLoadTimeout
		 * @private
	     */
		_onStencilSetLoadTimeout: function() {
			MOVI.log("A timeout occured while trying to load stencil set for model.", "error", "modelviewer.js");
			var scope = this._loadOptions.scope || window;
			if(this._loadOptions.onTimeout)
				this._loadOptions.onTimeout.call(scope, this);
			else if(this._loadOptions.onFailure)
				this._loadOptions.onFailure.call(scope, this);
		},
		
		/**
	     * Synchronization of asynchronous resource loading. When all resources (model image and 
		 * model data) are loaded successfully this method will trigger the execution of the 
		 * model load success callback.
		 * @method _syncLoadingReady
	     * @param resource {String} A string that identifies the successfully loaded resource ("image" or "data")
		 * @private
	     */
		_syncLoadingReady: function(resource) {
			// get index of resource in array
			for(var i=0; i<this._syncResources.length; i++) {
				if( this._syncResources[i]==resource ||
				    (this._syncResources[i].indexOf("_")>0 && 
				    this._syncResources[i].substring(0, this._syncResources[i].indexOf("_"))==resource) ) 
				        break;
			}
			if(i>=0) {
				this._syncResources.splice(i, 1);
			}
			
			if(this._syncResources.length==0) {
				if(this._loadOptions.onlyImage) this._onSuccess();
			    else if(this._initCanvas()) this._onSuccess();
			}
				
		},
		
		/**
		 * Initialize the Canvas object when all model, stencil set, and stencil
		 * set extension data are available.
		 * @method _initCanvas
		 * @private
		 */
		_initCanvas: function() {
		    
		    // first process all stencil set extensions
		    for(var i=0; i<this._ssextensions.length; i++) {
		        this.canvas.stencilset.addExtension(this._ssextensions[i]);
		    }
		    // then initialize the Canvas object
		    var prefix = "movi_" + this._index + "-";
		    try {
				this.canvas = new MOVI.model.Canvas(this, this.canvas, prefix);
			} catch(e) {
				MOVI.log("A " + e.name + " occured while trying to initialize the model: " + 
							e.message, "error", "modelviewer.js");
				this._onLoadFailure();
				return false;
			}
			this._scrollbox.appendChild(this.canvas);
			return true;
		},
		
		/**
	     * Returns the model viewer index (unique per page)
	     * @method getIndex
	     */
		getIndex: function() {
			return this._index;
		},

		/**
	     * Loads the specified Oryx model in the viewer.
	     * @method loadModel
	     * @param {String} uri The URI of the model (without /self). 
		 * For example: 'http://oryx-editor.org/backend/poem/model/1234'
		 * @param {Object} opt Options: 
         * <dl>
         * <dt>onSuccess</dt>
         * <dd>
         * Callback to execute when the model is finished loading
         * The callback receives the ModelViewer instance back.
         * </dd>
         * <dt>onFailure</dt>
         * <dd>
         * Callback to execute when the model load operation fails
         * The callback receives the ModelViewer instance back.
         * </dd>
		 * <dt>onTimeout</dt>
         * <dd>
         * Callback to execute when a timeout occurs. If not set
		 * the onFailure callback will be executed on timeout.
         * The callback receives the ModelViewer instance back.
         * </dd>
 		 * <dt>scope</dt>
		 * <dd>The execution scope for the callbacks.</dd>
 	 	 * <dt>timeout</dt>
         * <dd>
         * Number of milliseconds to wait before aborting the loading
		 * of the model and executing the onFailure callback.
		 * The default value is 15000 (15 seconds).
         * </dd>
         * <dt>urlModificationFunction</dt>
         * <dd>
         * Function that takes the URL of the model PNG and JSON representation 
         * and of the model's stencil set as parameter and returns a modificated 
         * URL string for the request.
         * The function is called in the specified scope with an additional
         * parameter that indicates the type of the requested resource 
         * ("png", "json", "stencilset")
         * </dd>
		 * <dt>onlyImage</dt>
         * <dd>
         * If set to true, the model JSON data and the stencil set definition
		 * data is not loaded. Thus the model loading is accelerated, but it
		 * won't be possible to register events on shapes, use markers or annotations.
		 * Defaults to false.
         * </dd>
		 * </dl>
		 * @throws Exception, if passed URI is not valid
	     */
		loadModel: function(uri, opt) {
			
			if(!YAHOO.lang.isString(uri)) {
				throw new URIError("No valid URI passed to loadModel." , "modelviewer.js");
				this._onLoadFailure();
			}
			
			if(uri.charAt(uri.length-1)=="/") uri = uri.substr(0, uri.length-1);
			this._modelUri = uri;
			
			this._loadOptions = opt || {};
			if(!this._loadOptions.timeout)
				this._loadOptions.timeout = 15000; // default timeout
				
			this.onModelLoadStart.fire(this._modelUri);
			this.onZoomLevelChangeStart.fire(this.getZoomLevel());
			
			this._syncResources = new Array();
			
			if(!this._loadOptions.onlyImage) {
				
				this._syncResources.push("data"); // include model data in synchronization

				var jsonp = encodeURIComponent(
					"MOVI.widget.ModelViewer.getInstance(" + 
					this._index + ").loadModelCallback");
				var url = uri + "/json?jsonp=" + jsonp;

				if(YAHOO.lang.isFunction(this._loadOptions.urlModificationFunction))
				    url = this._loadOptions.urlModificationFunction.call(this._loadOptions.scope || this, url, this, "json");

				var transactionObj = YAHOO.util.Get.script(url, {
					onFailure: this._onLoadFailure, 
					onTimeout: this._onLoadTimeout,
					timeout  : this._loadOptions.timeout,
					scope    : this
				});
				
			}
			
			this._loadImage(uri);
		},
		
		/**
	     * Load the PNG image representation of the model.
	     * @method _loadImage
		 * @param {String} uri The URI of the model (without /self).
		 * @private
	     */
		_loadImage: function(uri) {
			
			this._syncResources.push("image"); // include image loading in synchronization
			
			// append timestamp to allow reloads of the image
			var imgUrl = uri + "/png?" + (new Date()).getTime();
			
			if(YAHOO.lang.isFunction(this._loadOptions.urlModificationFunction))
			    imgUrl = this._loadOptions.urlModificationFunction.call(this._loadOptions.scope || this, imgUrl, this, "png");
			
			this._image.setStyle("width", "");
			this._image.setStyle("height", "");
			
			var self = this;
			this._image.get("element").onload = function() {
				self._imageWidth = parseInt(self._image.getStyle("width"), 10) || self._image.get("element").offsetWidth;
				self._imageHeight = parseInt(self._image.getStyle("height"), 10) || self._image.get("element").offsetHeight;
				self._syncLoadingReady("image"); // notify successful loading of image
			};
			
			this._image.set("src", imgUrl);
		},
		
		/**
	     * JSONP callback to load model data from the JSON web service
	     * @method loadModelCallback
	     * @param jsonObj The delivered JSON Object
	     */
		loadModelCallback: function(jsonObj) {
			
			if(this.canvas!=null)
				this._scrollbox.removeChild(this.canvas);
			
			this.canvas = jsonObj;

			if(!this.canvas.stencilset) {
				MOVI.log("Could not find stencil set definition for model.", "error", "canvas.js");
				this._onFailure();
			}
			
			// check if some stencil set extensions need to be loaded
			
			if(this.canvas.ssextensions) {
			    for(var i=0; i<this.canvas.ssextensions.length; i++) {
			        this._syncResources.push("ssextension_" + i); // add to resource loading sync
			    }
			}
			
			// load stencil set
			
			if(this.canvas.stencilset.url.substring(0, 7)!="http://") {
				// relative stencilset url, make absolute
				// guess server base url (model url substring until first slash)
				var index = this._modelUri.substring(7).indexOf("/");
				this.canvas.stencilset.url = this._modelUri.substring(0, 7+index) + this.canvas.stencilset.url;
			}
			
			var jsonp = encodeURIComponent(
				"MOVI.widget.ModelViewer.getInstance(" + 
				this._index + ").loadStencilSetCallback");
			var pathStringIndex = this.canvas.stencilset.url.indexOf("/stencilsets/");
			
			var url = this.canvas.stencilset.url.substring(0, pathStringIndex) +
				"/jsonp?resource=" + encodeURIComponent(this.canvas.stencilset.url.substring(pathStringIndex+13)) + 
				"&type=stencilset&jsonp=" + jsonp;
				
			if(YAHOO.lang.isFunction(this._loadOptions.urlModificationFunction))
    		    url = this._loadOptions.urlModificationFunction.call(this._loadOptions.scope || this, url, this, "stencilset");

			var transactionObj = YAHOO.util.Get.script(url, {
				onFailure: this._onStencilSetLoadFailure, 
				onTimeout: this._onStencilSetLoadTimeout,
				data	 : this._loadOptions, 
				timeout  : this._loadOptions.timeout,
				scope    : this 
			});
			
			// load all stencil set extensions
			
			this._ssextensions = new Array();
            if(this.canvas.ssextensions) {
			    for(var i=0; i<this.canvas.ssextensions.length; i++) {
			        var ssext_jsonp = encodeURIComponent(
        				"MOVI.widget.ModelViewer.getInstance(" + 
        				this._index + ").loadStencilSetExtensionCallback");
			        var ssext_url = this.canvas.stencilset.url.substring(0, pathStringIndex) +
        				"/jsonp?resource=" + encodeURIComponent(this.canvas.ssextensions[i]) + 
        				"&type=ssextension&jsonp=" + ssext_jsonp;
        				
        			if(YAHOO.lang.isFunction(this._loadOptions.urlModificationFunction))
                	    ssext_url = this._loadOptions.urlModificationFunction.call(this._loadOptions.scope || this, ssext_url, this, "ssextension");
			        
			        var ssext_transactionObj = YAHOO.util.Get.script(ssext_url, {
            			onFailure: this._onStencilSetLoadFailure, 
            			onTimeout: this._onStencilSetLoadTimeout,
            			data	 : this._loadOptions, 
            			timeout  : this._loadOptions.timeout,
            			scope    : this 
            		});
			    }
			}
		},
		
		/**
	     * JSONP callback to load stencilset data from the JSON web service
	     * @method loadStencilSetCallback
	     * @param jsonObj The delivered JSON Object
	     */
		loadStencilSetCallback: function(jsonObj) {
			this.canvas.stencilset = new MOVI.stencilset.Stencilset(jsonObj);
			this._syncLoadingReady("data"); // notify successful loading of data
		},
		
		/**
	     * JSONP callback to load stencilset extension data from the JSON web service
	     * @method loadStencilSetExtensionCallback
	     * @param jsonObj The delivered JSON Object
	     */
		loadStencilSetExtensionCallback: function(jsonObj) {
			this._ssextensions.push(jsonObj); 
			this._syncLoadingReady("ssextension"); // notify successful loading
		},
		
		/**
		 * Returns the original width (100% zoom level) of the model image element in pixels
		 * @method getImgWidth
		 * @return {Integer} The image width
		 */
		getImgWidth: function() {
			return this._imageWidth || this.getScrollboxEl().get("element").offsetWidth;
		},
		
		/**
		 * Returns the original height (100% zoom level) of the model image element in pixels
		 * @method getImgHeight
		 * @return {Integer} The image height
		 */
		getImgHeight: function() {
			return this._imageHeight || this.getScrollboxEl().get("element").offsetHeight;
		},
		
		/**
		 * Returns the absolute URI of the displayed model
		 * @method getImgHeight
		 * @return {String} The models absolute URI
		 */
		getModelUri: function() {
			return this._modelUri;
		},
		
		/**
		 * Returns the canvas container Element realizing scrolling
		 * @method getScrollboxEx
		 * @return {Element} The scrollbox Element
		 */
		getScrollboxEl: function() {
			return this._scrollbox;
		},
		
		/**
		 * Scrolls to the shape, i.e. centers the (canvas around the) shape 
		 * within the scrollbox.
		 * @method scrollToShape
		 * @param {Shape} shape the shape or its resource id
		 * @return {Element} The shape element / null if it does not exist
		 * @throws Error if scrollbox dimensions cannot be calculated
		 */
		scrollToShape: function(shape) {
			if ("string" == typeof(shape)) {
				shape = this.canvas.getShape(shape);
				if (null == shape) {
					return null;
				}
			}
			
			if (null != (h = this.getScrollboxEl().getStyle("height").match(/(\d+)px/) || this.getScrollboxEl().get("element").offsetHeight) &&
			    null != (w = this.getScrollboxEl().getStyle("width").match(/(\d+)px/) || this.getScrollboxEl().get("element").offsetWidth))
			{
				var bounds = shape.getAbsBounds();			

				var origin = {
					x: bounds.upperLeft.x + (bounds.lowerRight.x - bounds.upperLeft.x)/2,
					y: bounds.upperLeft.y + (bounds.lowerRight.y - bounds.upperLeft.y)/2
				}
				
				var zoomFactor = this.getZoomLevel()/100;

				var target = {
					x: parseInt(origin.x*zoomFactor - parseInt(w[1])/2 ),
					y: parseInt(origin.y*zoomFactor - parseInt(h[1])/2 )
				}

				this.getScrollboxEl().set("scrollTop", target.y);
				this.getScrollboxEl().set("scrollLeft", target.x);
			}
			else {
				throw new Error("Unable to calculate scrollbox dimensions", "modelviewer.js");
			}
			
			return shape;
		},
		
		/**
		 * Scrolls to the specified position, centers the (canvas around the) position 
		 * within the scrollbox.
		 * @method centerScrollTo
		 * @param {Integer} x The center's x position
		 * @param {Integer} y The center's y position
		 * @throws Error if scrollbox dimensions cannot be calculated
		 */
		centerScrollTo: function(x, y) {
			if(!YAHOO.lang.isNumber(x) || !YAHOO.lang.isNumber(y)) {
				throw new TypeError("The coordinates passed to centerScrollTo(x, y) have to be of type Integer.", 
									"modelviewer.js");
			}
			
			var left = Math.round(x - parseInt(this.getScrollboxEl().getStyle("width"), 10)/2),
				top  = Math.round(y - parseInt(this.getScrollboxEl().getStyle("height"), 10)/2);
			this.getScrollboxEl().set("scrollLeft", left);
			this.getScrollboxEl().set("scrollTop", top);
		},
		
		/**
		 * Set the model zoom level in percent (minimum: 0, maximum: 100)
		 * @method setZoomLevel
		 * @param {Number} percent The zoom level in percent
		 * @param {Boolean} notifyStartEnd (optional) If set to false markers, annotations and shape rects will 
		 * not be updated. Default is true.
		 */
		setZoomLevel: function(percent, notifyStartEnd) {
			if(!YAHOO.lang.isNumber(percent)) {
				throw new TypeError("The parameter passed to have to setZoomLevel has to be of type Number.", 
									"modelviewer.js");
			}
			if(percent<=0) percent=0;
			else if(percent>100) percent=100;
			
			this._zoomLevel = percent;
			
			this._image.setStyle("width", Math.round(this.getImgWidth()*this._zoomLevel/100) + "px");
			this._image.setStyle("height", Math.round(this.getImgHeight()*this._zoomLevel/100) + "px");
			
			// fire events
			if(notifyStartEnd!==false) this.onZoomLevelChangeStart.fire(this._zoomLevel);
			this.onZoomLevelChange.fire(this._zoomLevel);
			if(notifyStartEnd!==false) this.onZoomLevelChangeEnd.fire(this._zoomLevel);
		},
		
		/**
		 * Returns the model zoom level in percent (minimum: 0, maximum: 100)
		 * @method getZoomLevel
		 * @return {Number} The current model zoom level in percent
		 */
		getZoomLevel: function() {
			return this._zoomLevel;
		},
		
		/**
		 * Set the model zoom level so that the model fits in height and width to the model viewer
		 * @method fitModelToViewer
		 */
		fitModelToViewer: function() {
			var scaleHorizontal = (this.getScrollboxEl().get("offsetWidth")-5) / this.getImgWidth();
			var scaleVertical = (this.getScrollboxEl().get("offsetHeight")-5) / this.getImgHeight();
			var scale = (scaleHorizontal < scaleVertical) ? scaleHorizontal : scaleVertical;
			if(scale>1)	scale = 1;
			this.setZoomLevel(scale*100);
		},
		
		/**
		 * @method _onMouseDown
		 * @private
		 */
		_onMouseDown: function(ev) {
			// don't start drag scrolling when the mouse down event occurs on a scrollbar
			var SCROLLBAR_WIDTH = 20;
			var mouseAbsXY = YAHOO.util.Event.getXY(ev);
			var ul = YAHOO.util.Dom.getXY(this._scrollbox);
			var lrX = ul[0]+this.getScrollboxEl().get("offsetWidth"),
				lrY = ul[1]+this.getScrollboxEl().get("offsetHeight");
			if(this.getScrollboxEl().get("element").clientWidth < this.getScrollboxEl().get("element").scrollWidth) {
				// horizontal scrollbar shown
				if( (mouseAbsXY[1]>=lrY-20) && (mouseAbsXY[1]<=lrY) ) {
					// mouse down on scrollbar
					return;
				}
			}
			if(this.getScrollboxEl().get("element").clientHeight < this.getScrollboxEl().get("element").scrollHeight) {
				// vertical scrollbar shown
				if( (mouseAbsXY[0]>=lrX-20) && (mouseAbsXY[0]<=lrX) ) {
					// mouse down on scrollbar
					return;
				}
			}
			
		
			if(ev.target==this._scrollbox.get("element") || ev.target==this._image.get("element")
					|| ev.target==this.canvas.get("element")) {
				YAHOO.util.Event.preventDefault(ev);
				YAHOO.util.Event.stopPropagation(ev);
			}
			this._scrollbox.removeListener("mousemove", this._onModelDrag);

			this._mouseCoords = { x: mouseAbsXY[0], y: mouseAbsXY[1] };
			this._scrollbox.addListener("mousemove", this._onModelDrag, this, this, true);
			this._isDragging = true;
		},
		
		/**
		 * @method _onMouseUp
		 * @private
		 */
		_onMouseUp: function(ev) {
			YAHOO.util.Event.preventDefault(ev);
			this._scrollbox.removeListener("mousemove", this._onModelDrag);
			this._isDragging = false;
		},
		
		/**
		 * Callback method that is executed when the model is dragged
		 * @method _onModelDrag
		 * @private
		 */
		_onModelDrag: function(ev) {
			YAHOO.util.Event.preventDefault(ev);
			var mouseAbsXY = YAHOO.util.Event.getXY(ev);
			var sl = this._scrollbox.get("scrollLeft"); 
			var st = this._scrollbox.get("scrollTop");
			this._scrollbox.set("scrollLeft", sl - (mouseAbsXY[0] - this._mouseCoords.x));
			this._scrollbox.set("scrollTop", st - (mouseAbsXY[1] - this._mouseCoords.y));
			this._mouseCoords.x = mouseAbsXY[0]; this._mouseCoords.y = mouseAbsXY[1];
		}
		
		
	});
	
})();/**
 * Copyright (c) 2009
 * Jan-Felix Schwarz
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


MOVI.namespace("stencilset");

(function() {
	
	/**
     * Stencil represents an entity of a stencilset.
     * @namespace MOVI.stencilset
     * @class MOVI.stencilset.Stencil
     * @constructor
     * @param {Object} jsonObj The JSON object from which the new stencil
     * is created.
     * @param {String} namespace The namespace of the stencil set that defines this stencil
     */
	MOVI.stencilset.Stencil = function(jsonObject, propertyPackages, namespace) {
		
		// TODO: Doc for stencil attributes from JSON
		YAHOO.lang.augmentObject(this, jsonObject, true);
		
		if(this.propertyPackages) {
			var props = {};
			for(var propIndex in this.properties) {
				var prop = this.properties[propIndex];
				props[prop.id] = prop;
			}
			
			for(var i = 0; i < this.propertyPackages.length; i++) {
				var propPackName = this.propertyPackages[i];
				var propPack = propertyPackages[propPackName];
				if(propPack) {
					for(var j = 0; j < propPack.length; j++) {
						var prop = propPack[j];
						props[prop.id] = prop;
					}
				}
			}
			
			this.properties = [];
			
			for(var propKey in props) {
				if(!(props[propKey] instanceof Function)) {
					this.properties.push(props[propKey]);
				}
			}
		}
		
		this.stencilSetNamespace = namespace;
		
	}
	
	// TODO: Add convenience methods
	
})();

/**
 * Copyright (c) 2009
 * Jan-Felix Schwarz
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


MOVI.namespace("stencilset");

(function() {
	
	/**
     * Stencilset is a collection of stencils.
     * @namespace MOVI.stencilset
     * @class MOVI.stencilset.Stencilset
     * @constructor
     * @param {Object} jsonObj The JSON definition of a stencilset
     */
	MOVI.stencilset.Stencilset = function(jsonObj) {
		
		this.title = jsonObj.title;
		this.namespace = jsonObj.namespace;
		this.description = jsonObj.description;
		
		this.stencils = {};
		
		if(!jsonObj.stencils) {
			MOVI.log("Stencilset contains no stencil definitions", "warning", "stencilset.js" );
		}
		
		this.propertyPackages = {};
		
		// init property packages
		if(jsonObj.propertyPackages) {
			for(var i = 0; i < jsonObj.propertyPackages.length; i++) {
				var pp = jsonObj.propertyPackages[i];
				this.propertyPackages[pp.name] = pp.properties;
			}
		}
		
		for(var key in jsonObj.stencils) {
			if(!YAHOO.lang.hasOwnProperty(jsonObj.stencils, key)) continue;
			
			var stencil = jsonObj.stencils[key];
			this.stencils[stencil.id] = new MOVI.stencilset.Stencil(stencil, this.propertyPackages, jsonObj.namespace);
		}

	};

	MOVI.stencilset.Stencilset.prototype = {
		
		/**
	     * A key map containing all stencils of the stencilset as values.
		 * Retrieve an entry using the stencil's id as the key.
	     * @property stencils
	     */
		stencils: null,
		
		/**
	     * Returns the stencil object with the specified id. If
		 * the stencilset does not contain a matching stencil
		 * null is returned.
	     * @method getStencil
	     * @param jsonObj The delivered JSON Object
	     */
		getStencil: function(id) {
			return this.stencils[id] || null;
		},
		
		/**
	     * Adds a stencil set extension to this stencil set.
	     * @method addExtension
	     * @param jsonObj The JSON representation of the stencil set extension
	     */
		addExtension: function(jsonObj) {
		    
		    // init additional property packages
    		if(jsonObj.propertyPackages) {
    			for(var i = 0; i < jsonObj.propertyPackages.length; i++) {
    				var pp = jsonObj.propertyPackages[i];
    				this.propertyPackages[pp.name] = pp.properties;
    			}
    		}
    		
		    // init additional stencils
		    if(jsonObj.stencils) {
		        for(var i=0; i<jsonObj.stencils.length; i++) {
		            var stencil = jsonObj.stencils[i];
		            this.stencils[stencil.id] = new MOVI.stencilset.Stencil(stencil, this.propertyPackages, jsonObj.namespace);
		        }
		    }
		    
		    // init additional properties
		    if(jsonObj.properties) {
		        for(var i=0; i<jsonObj.properties.length; i++) {
		            var property = jsonObj.properties[i];
		            for(var key in this.stencils) {
		                if(!YAHOO.lang.hasOwnProperty(this.stencils, key)) continue;
		                var stencil = this.stencils[key];
		                for(var j=0; j<property.roles.length; j++) {
		                    for(var k=0; k<stencil.roles.length; k++) {
		                        if(property.roles[j]==stencil.roles[k]) {
		                            for(var l=0; l<property.properties.length; l++) {
		                                stencil.properties.push(property.properties[l]);
		                            }
		                        }
		                    }
		                }
		                
		            }
		        }
		    }
		    
		    // note: removing of stencils and properties is not regarded here
		}
		
	}
	
})();

/**
 * Copyright (c) 2009
 * Jan-Felix Schwarz
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


MOVI.namespace("model");

(function() {
	
	/**
     * Create the host element.
     * @method _createHostElement
     * @private
     */
    var _createHostElement = function() {
        var el = document.createElement('div');
        return el;
    };

	/**
     * Returns the appropriate subclass to choose when creating the shape 
	 * object from the specified JSON object. If no subclass can be 
	 * determined the Shape base class is returned.
     * @method _getSubclassForJSONObj
 	 * @param {Object} jsonObj The JSON object to create the shape from
	 * @param {Stencilset} stencilset The stencilset object
     * @private
     */
	var _getSubclassForJSONObj = function(jsonObj, stencilset) {
		var stencil = stencilset.getStencil(jsonObj.stencil.id);
		if(stencil) {
			if(stencil.type=="node")
				return MOVI.model.Node;
			else if(stencil.type=="edge")
				return MOVI.model.Edge;
		}
		return MOVI.model.Shape;
	}
	
	/**
     * An abstract base class providing a wrapper object for all 
	 * model shapes (including the canvas, nodes, and edges).
     * @namespace MOVI.model
     * @class MOVI.model.Shape
     * @extends YAHOO.util.Element
     * @constructor
     * @param {Object} jsonObj The JSON object from which the new shape
     * is created
	 * @param {Object} stencilset The stencilset for lookup of the shape's 
	 * stencil
	 * @param {Shape} parent The shapes's parent shape
	 * @param {String} prefix The element's ID prefix (unique per modelviewer)
	 * @param {Object} attr (optional) A key map of the shape element's 
     * initial attributes
     */
    MOVI.model.Shape = function(jsonObj, stencilset, parent, prefix, attr) {
		
		YAHOO.lang.augmentObject(this, jsonObj, true);
		
		if(!stencilset) {
			throw new Error("No stencilset associated for shape with resource id" + 
					 this.resourceId + ".", "shape.js");
			return false;
		}
		
		if(!this.resourceId) {
			throw new Error("The shape has no resource id.", "shape.js");
			return false;
		}
	
		if(!this.stencil || !this.stencil.id) {
			throw new Error("No stencil definition found for shape with resource id " + 
				this.resourceId, "shape.js");
			return false;
		}
	
		// retrieve stencil
		var sId = this.stencil.id;
		this.stencil = stencilset.getStencil(sId);
		
		if(!this.stencil) {
			throw new Error("Could not find definition for stencil " + sId + 
					 " in stencilset " + stencilset.namespace + ".", "shape.js" );
			return false;
		}
		
		// register parent shape
		this.parentShape = parent;

		// create host element
		attr = attr || {};
        el = _createHostElement.call(this, attr);
		
		MOVI.model.Shape.superclass.constructor.call(this, el, attr); 		
		
		this.set("id", prefix + this.resourceId);
		
		/* create child shape objects and append them as child elements */
		
		var childSh = this.childShapes; // store children's JSON objects in temp var
		this.childShapes = {};			// reset childShapes map
	
		// create shape object for each child and append to childShapes map
		for(var i = 0; i<childSh.length; i++) { 
			var child = childSh[i];
			var subclass = _getSubclassForJSONObj(child, stencilset);
			child = new subclass(child, stencilset, this, prefix);
			this.childShapes[child.resourceId] = child;
			this.appendChild(child);
		}
		
	}
	
	MOVI.extend(MOVI.model.Shape, YAHOO.util.Element, {
		
		/**
		 * The shapes parent shape
		 * @property parentShape
		 */
		parentShape: null,
		
		/**
	     * Returns the canvas that owns this shape
	     * @method getCanvas
		 * @return {Canvas} The Canvas the shape belongs to
	     */
		getCanvas: function() {
			var e = this;
			while(e.parentShape!=null)
				e = e.parentShape;
			return e;
		},

		/**
		 * Returns a set on all incoming shapes
		 * @method getIncomingShapes
		 * @return {Array}
		 */
		getIncomingShapes: function(){
			
			if (!this._incomingShapes){
							
				var incoming = [];
				var canvas = this.getCanvas();
	
				if (canvas) {
					var nodes = canvas.shapes;
					for (var i in nodes){
						if(!YAHOO.lang.hasOwnProperty(nodes, i)) continue;
				
						if ((nodes[i].outgoing||[]).length > 0) {
							var isOutgoing = false;
								for (var j=0, oLen = nodes[i].outgoing.length; j < oLen; ++j){
									if (nodes[i].outgoing[j].resourceId == this.resourceId){
										incoming.push(nodes[i]);
										break;
									}
								}
						}
					}
				}
				this._incomingShapes = incoming;
			}

			return this._incomingShapes;				
		},
				
		/**
		 * Returns a set on all outgoing shapes
		 * @method getOutgoingShapes
		 * @return {Array}
		 */
		getOutgoingShapes: function(){
			
			if (!this._outgoingShapes){
				
				var outgoings = [];
				var canvas = this.getCanvas();
		
				if (canvas) {
					var og = this.outgoing || [];
					for (var i=0, len=og.length; i<len; ++i){
						var shape = canvas.getShape(og[i].resourceId);
						if (shape) {
							outgoings.push(shape);	
						}
					}
				}
				
				this._outgoingShapes = outgoings;
			}

			return this._outgoingShapes;				
		},
		
		/**
		 * Returns the bounds of a shape
		 * 
		 */
		getBounds: function(){
			
			if (!this.bounds) {
				
				// ERROR HANDLING if bounds are not set
				if (this.dockers) {
					var min, max;
					
					for (var i = 0, len = this.dockers.length; i < len; ++i) {
						
						var pos = {x:this.dockers[i].x, y:this.dockers[i].y}
						
						// Add the bounds of the docked incoming shape
						if (i==0){
							var nodes = this.getIncomingShapes();
							if (nodes.length > 0){
								var bounds = nodes[0].getAbsBounds();
								pos.x += bounds.upperLeft.x;
								pos.y += bounds.upperLeft.y;
							}
						}
						
						// Add the bounds of the docked outgoing shape
						if (i == len-1&&(this.outgoing||[]).length > 0) {
							var nodes = this.getOutgoingShapes();
							if (nodes.length > 0){
								var bounds = nodes[0].getAbsBounds();
								pos.x += bounds.upperLeft.x;
								pos.y += bounds.upperLeft.y;
							}
						}
						
						if (!min) min = {x: pos.x, y: pos.y}
						if (!max) max = {x: pos.x, y: pos.y }
						
						
						min.x = Math.min(min.x, pos.x);
						min.y = Math.min(min.y, pos.y);
						max.x = Math.max(max.x, pos.x);
						max.y = Math.max(max.y, pos.y);
					}
					
					if (min && max) {
						this.bounds = {
							upperLeft: min,
							lowerRight: max
						}
					}
				}
				
				if (!this.bounds){
					this.bounds =  { 
						upperLeft: { x: 0, y: 0 }, 
						lowerRight: { x: 0, y: 0 } 
					}
				}
			}
			
			return { upperLeft: { 
				x: this.bounds.upperLeft.x, 
				y: this.bounds.upperLeft.y
			  }, 
			  lowerRight: { 
				x: this.bounds.lowerRight.x,
				y: this.bounds.lowerRight.y
			  } 
			}

		},
		
		/**
	     * Returns the shape's absolute bounds coordinates. 'Absolute' means
		 * relative to the canvas element rather than relative to the document
	     * @method getAbsBounds
		 * @return {Object} The absolute shape bounds accessible as 
		 * { upperLeft: {x:Number,y:Number}, lowerRight: {x:Number,y:Number} }
	     */
		getAbsBounds: function() {
			var absBounds =	this.getBounds();	
			var e = this;
			while(e.parentShape!=null && e.parentShape.parentShape!=null) {
				e = e.parentShape;
				var b = e.getBounds();
				absBounds.upperLeft.x +=b.upperLeft.x;
				absBounds.upperLeft.y += b.upperLeft.y;
				absBounds.lowerRight.x += b.upperLeft.x;
				absBounds.lowerRight.y += b.upperLeft.y;
			}
			return absBounds;
		},
		
		/**
		 * Tests if the shape has children
		 * @method hasChildShapes
		 * @return {Boolean} true if the shape has children, false otherwise
		 */
		hasChildShapes: function() {
			for(var key in this.childShapes) {
				if(!YAHOO.lang.hasOwnProperty(this.childShapes, key)) continue;
				return true;
			}
			return false;
		},
		
		/**
		 * Returns the stencil of this shape
		 * @method getStencil
		 * @return {Stencil} The stencil object
		 */
		getStencil: function() {
			return this.stencil;
		},
		
		/**
		 * Returns true if the shape is a node
		 * @method isNode
		 * @return {Boolean} true if shape is a node
		 */
		isNode: function() {
			return false;
		},
		
		/**
		 * Returns true if the shape is an edge
		 * @method isEdge
		 * @return {Boolean} true if shape is an edge
		 */
		isEdge: function() {
			return false;
		}
		
		// TODO: Doc for shape attributes from JSON
		
	});
	
})();/**
 * Copyright (c) 2009
 * Jan-Felix Schwarz
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


MOVI.namespace("model");

(function() {
	
	var _CLASS_NAME = "movi-node";
	
	/**
     * A wrapper class for model nodes.
     * @namespace MOVI.model
     * @class MOVI.model.Node
     * @extends MOVI.model.Shape
     * @constructor
     * @param {Object} jsonObj The JSON object from which the new node
     * is created.
	 * @param {Object} stencilset The stencilset for lookup of the node's 
	 * stencil.
	 * @param {Shape} parent The node's parent shape
	 * @param {String} prefix The element's ID prefix (unique per modelviewer)
     */
    MOVI.model.Node = function(jsonObj, stencilset, parent, prefix) {
	
		// element's attributes
		var attr = {};
		
		MOVI.model.Node.superclass.constructor.call(this, jsonObj, stencilset, parent, prefix, attr); 
		this.set("className", _CLASS_NAME);
		
		this.getCanvas().getModelViewer().onZoomLevelChangeEnd.subscribe(this.update, this, true);
		
		this.update();
	}
	
	MOVI.extend(MOVI.model.Node, MOVI.model.Shape, {
		
		/**
	     * Update style properties of the element
	     * @method update
	     */
		update: function() {
			var zoomFactor = this.getCanvas().getModelViewer().getZoomLevel() / 100;
			
			var left = Math.round(this.bounds.upperLeft.x * zoomFactor);
			var top = Math.round(this.bounds.upperLeft.y * zoomFactor);
			var width = Math.round((this.bounds.lowerRight.x - this.bounds.upperLeft.x) * zoomFactor);
			var height = Math.round((this.bounds.lowerRight.y - this.bounds.upperLeft.y) * zoomFactor);
			
			this.setStyle("left", left + "px");
			this.setStyle("top", top + "px");
			this.setStyle("width", width + "px");
			this.setStyle("height", height + "px");
		},
		
		/**
	     * Overrides the method of the abstract super class
	     */
		isNode: function() {
			return true;
		}
		
	});
	
})();/**
 * Copyright (c) 2009
 * Jan-Felix Schwarz
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


MOVI.namespace("model");

(function() {
	
	var _CLASS_NAME = "movi-edge";
	
	/**
     * A wrapper class for model edges.
     * @namespace MOVI.model
     * @class MOVI.model.Edge
     * @extends MOVI.model.Shape
     * @constructor
     * @param {Object} jsonObj The JSON object from which the new edge
     * is created.
	 * @param {Object} stencilset The stencilset for lookup of the edge's 
	 * stencil.
	 * @param {Shape} parent The edges's parent shape
	 * @param {String} prefix The element's ID prefix (unique per modelviewer)
     */
    MOVI.model.Edge = function(jsonObj, stencilset, parent, prefix) {
	
		// element's attributes
		var attr = {};
		
		MOVI.model.Node.superclass.constructor.call(this, jsonObj, stencilset, parent, prefix, attr); 
		
		this.set("className", _CLASS_NAME);
	}
	
	MOVI.extend(MOVI.model.Edge, MOVI.model.Shape, {
		
		/**
	     * Update style properties of the element
	     * @method update
	     */
		update: function() {
			
		},
		
		/**
		 * Returns the intersection point of the bounds rectangle and the segment p1->p2 where p1
		 * is located inside the bounds rectangle.
		 * If there is no intersection, p1 is returned
		 * @method _getIntersectionPoint
		 * @private
		 */
		_getIntersectionPoint: function(bounds, p1, p2) {
			
			var linearEquation = function(x) {
				return p1.y + (p2.y-p1.y)/(p2.x-p1.x) * (x-p1.x);
			};
			
			var inverseLinearEquation = function(x) {
				return p1.x + (p2.x-p1.x)/(p2.y-p1.y) * (x-p1.y);
			};
			
			// intersection points of the straight line through p1,p2 and the bounds rectangle
			var s = []; 
				s[0] = {x: inverseLinearEquation(bounds.upperLeft.y), y: bounds.upperLeft.y};
				s[1] = {x: bounds.lowerRight.x, y: linearEquation(bounds.lowerRight.x)};
				s[2] = {x: inverseLinearEquation(bounds.lowerRight.y), y: bounds.lowerRight.y};
				s[3] = {x: bounds.upperLeft.x, y: linearEquation(bounds.upperLeft.x)};
			
			for(var i=0; i<=3; i++) {
	
				if(s[i].x==Infinity || s[i].y==Infinity) continue; // there is no intersection
				
				if(isNaN(s[i].x)) {
					// the segment p1->p2 lies over a segment of the bounding box,
					// there is no definite intersection point
					// -> use the point that is the nearest to the p2
					s[i].x = (p2.x>p1.x) ? bounds.lowerRight.x : bounds.upperLeft.x; 
				}
				if(isNaN(s[i].y)) {
					// the segment p1->p2 lies over a segment of the bounding box,
					// there is no definite intersection point
					// -> use the point that is the nearest to the p2
					s[i].y = (p2.y>p1.y) ? bounds.lowerRight.y : bounds.upperLeft.y; 
				}
				
				// test if intersection point is located on the segment p1->p2
				var tx = (s[i].x-p1.x)/(p2.x-p1.x),
					ty = (s[i].y-p1.y)/(p2.y-p1.y);
				
				if(Math.abs(tx)==Infinity||Math.abs(ty)==Infinity) continue;
				
				if(isNaN(tx) && ty>=0 && ty<=1) {
					// p1.x==p2.x==s.x
					return {x: p1.x, 
							y: p1.y + ty*(p2.y-p1.y)};
				} else if(isNaN(ty) && tx>=0 && tx<=1) {
					// p1.y==p2.y==s.y
					return {x: p1.x + tx*(p2.x-p1.x), 
							y: p1.y};
				} else if((Math.round(tx*10)/10)==(Math.round(ty*10)/10) && tx>=0 && tx<=1) {
					return {x: p1.x + tx*(p2.x-p1.x), 
							y: p1.y + tx*(p2.y-p1.y)};
				}
			}
			
			return {x:p1.x, y:p1.y};
		},
		
		/**
		 * Returns an array of the edges absolute coordinates {x: INTEGER, y: INTEGER} 
		 * @method getCoordinates
		 * @return {Object[]} An array of absolute coordinates {x: INTEGER, y: INTEGER}
		 */
		getCoordinates: function() {
			var coords = [];
			
			var incoming = this.getIncomingShapes(),
				outgoing = this.getOutgoingShapes();
				
			var len = this.dockers.length;
			for (var i = 0; i < len; ++i) {
				
				var pos = {x:this.dockers[i].x, y:this.dockers[i].y}
				
				// Add the bounds of the docked incoming shape
				if(i==0 && incoming.length>0){
					var bounds = incoming[0].getAbsBounds();
					pos.x += bounds.upperLeft.x;
					pos.y += bounds.upperLeft.y;
				}
				
				// Add the bounds of the docked outgoing shape
				if(i==(len-1) && outgoing.length>0) {
					var bounds = outgoing[0].getAbsBounds();
					pos.x += bounds.upperLeft.x;
					pos.y += bounds.upperLeft.y;
				}
				
				coords.push(pos);
			}
				
			if(incoming.length>0)
				coords[0] = this._getIntersectionPoint(incoming[0].getAbsBounds(), coords[0], coords[1]);
			if(outgoing.length>0)
				coords[len-1] = this._getIntersectionPoint(outgoing[0].getAbsBounds(), coords[len-1], coords[len-2]);
		
			return coords;
		},
		
		/**
	     * Overrides the method of the abstract super class
	     */
		isEdge: function() {
			return true;
		}
		
	});
	
})();/**
 * Copyright (c) 2009
 * Jan-Felix Schwarz
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


MOVI.namespace("model");

(function() {
	
	var _CLASS_NAME = "movi-canvas";
	
	/**
     * Canvas provides a wrapper object for the model's root shape.
     * @namespace MOVI.model
     * @class MOVI.model.Canvas
     * @extends MOVI.model.Shape
     * @constructor
	 * @param {Modelviewer} modelviewer The Modelviewer object that owns
	 * the canvas
     * @param {Object} jsonObj The JSON object from which the new canvas
     * is created.
	 * @param {String} prefix The element's ID prefix (unique per modelviewer)
     */
    MOVI.model.Canvas = function(modelviewer, jsonObj, prefix) {
	
		this._modelviewer = modelviewer;

		// element's attributes
		var attr = {};
		
		MOVI.model.Canvas.superclass.constructor.call(this, jsonObj, jsonObj.stencilset, null, prefix, attr); 
		
		this.set("className", _CLASS_NAME);
		
		this.shapes = {};
		this._resourceIds = [];
		this._indexShapes();
		
		this._modelviewer.onZoomLevelChangeStart.subscribe(function() {
			this.setStyle("display", "none");
		}, this, true);
		this._modelviewer.onZoomLevelChangeEnd.subscribe(function() {
			this._update();
			this.setStyle("display", "block");
		}, this, true);
		
		this._update();
		
	}
	
	MOVI.extend(MOVI.model.Canvas, MOVI.model.Shape, {
		
		/**
		 * The owning modelviewer
		 * @property modelviewer
		 * @type Modelviewer
		 * @private
		 */
		_modelviewer: null,
		
		/**
		 * A key map containing all shapes of the model with their
		 * resource ids as keys.
		 * @property shapes
		 * @type Object
		 */
		shapes: null,
		
		/**
	     * Index all shapes owned by the model. Deep traverse canvas'
	     * child shapes and add all objects to the 'shapes' property.
	     * @method _indexShapes
		 * @param {Shape} recShape (optional) Temporarily considered shape
		 * for recursion
	     * @private
	     */
		_indexShapes: function(recShape) {
			recShape = recShape || this;
			for(var key in recShape.childShapes) {
				if(!YAHOO.lang.hasOwnProperty(recShape.childShapes, key)) continue;
				
				var child = recShape.childShapes[key];
				this.shapes[child.resourceId] = child;
				this._resourceIds.push(child.resourceId);
				this._indexShapes(child);
			}
		},
		
		/**
	     * Update style properties of the element
	     * @method _update
	     * @private
	     */
		_update: function() {
			var zoomFactor = this._modelviewer.getZoomLevel() / 100;
			var minX, minY, maxX, maxY, bounds;
			for(var i in this.childShapes) {
				if(!YAHOO.lang.hasOwnProperty(this.childShapes, i)) continue;
				
				bounds = this.childShapes[i].getAbsBounds();
				if(minX==undefined) minX = bounds.upperLeft.x;
				else minX = Math.min(minX, bounds.upperLeft.x);
				if(minY==undefined) minY = bounds.upperLeft.y;
				else minY = Math.min(minY, bounds.upperLeft.y);
				
				if(maxX==undefined) maxX = bounds.upperLeft.x;
				else maxX = Math.max(maxX, bounds.upperLeft.x);
				if(maxY==undefined) maxY = bounds.upperLeft.y;
				else maxY = Math.max(maxY, bounds.upperLeft.y);
			}
			for(var i in this.childShapes) {
				if(!YAHOO.lang.hasOwnProperty(this.childShapes, i)) continue;
				
				bounds = this.childShapes[i].bounds;
				bounds.upperLeft.x -= minX;
				bounds.upperLeft.y -= minY;
				bounds.lowerRight.x -= minX;
				bounds.lowerRight.y -= minY;
				this.childShapes[i].update();
				
				// for edges also adjust the absolute coordinates of the dockers
				if(this.childShapes[i].isEdge() && this.childShapes[i].dockers && this.childShapes[i].dockers.length>2) {
					for(var j=1; j<this.childShapes[i].dockers.length-1; j++) {
						this.childShapes[i].dockers[j].x -= minX;
						this.childShapes[i].dockers[j].y -= minY;
					}
				}
			}
			if(!this.bounds || !this.bounds.upperLeft || !this.bounds.lowerRight)
			    this.bounds = { upperLeft: {x: 0, y: 0}, lowerRight: {x: maxX-minX, y: maxY-minY}};
			    
			var left = (MOVI.config.MODEL_MARGIN / 2)*zoomFactor + "px";
			var top = (MOVI.config.MODEL_MARGIN / 2)*zoomFactor + "px";
			var width = (this._modelviewer.getImgWidth() - MOVI.config.MODEL_MARGIN)*zoomFactor + "px";
			var height = (this._modelviewer.getImgHeight() - MOVI.config.MODEL_MARGIN)*zoomFactor + "px";
			this.setStyle("left", left);
			this.setStyle("top", top);
			this.setStyle("width", width);
			this.setStyle("height", height); 
		},
		
		
		/**
	     * Returns the shape with the specified resource id. 
		 * If no matching stencil is found null is returned.
	     * @method getShape
	     * @param resourceId The shape's resource id
	     */
		getShape: function(resourceId) {
			return this.shapes[resourceId] || null;
		},
		
		/**
		 * Returns the owning modelviewer
		 * @method getModelViewer
		 * @return {ModelViewer} The owning model viewer
		 */
		getModelViewer: function() {
			return this._modelviewer;
		},
		
		/**
		 * Returns all nodes of the model
		 * @method getNodes
		 * @return {Object} A key map (String -> Node) of all nodes with their resource ids as keys
		 */
		getNodes: function() {
			var nodes = {};
			for(var key in this.shapes) {
				if(!YAHOO.lang.hasOwnProperty(this.shapes, key)) continue;
				
				if(this.shapes[key].stencil.type=="node")
					nodes[key] = this.shapes[key];
			}
			return nodes;
		},
		
		/**
		 * Returns all edges of the model
		 * @method getEdges
		 * @return {Object} A key map (String -> Node) of all edges with their resource ids as keys
		 */
		getEdges: function() {
			var edges = {};
			for(var key in this.shapes) {
				if(!YAHOO.lang.hasOwnProperty(this.shapes, key)) continue;
				
				if(this.shapes[key].stencil.type=="edge")
					edges[key] = this.shapes[key];
			}
			return edges;
		},
		
		/**
		 * Returns an Array containing all resource IDs of all shapes
		 * @method getResourceIds
		 * @return {Array} An Array of resource IDs as Strings of all shapes of the model
		 */
		getResourceIds: function() {
			return this._resourceIds;
		}
		
	});
	
})();/**
 * Copyright (c) 2009
 * Jan-Felix Schwarz
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


MOVI.namespace("util");

(function() {
	
	var _MARKER_RECT_CLASS_NAME = "movi-marker",
		_ICON_CLASS_NAME = "movi-marker-icon",
		_ICON_MARGIN = -10; 
		
	/**
	 * A model overlay polygone.
	 * @namespace MOVI.util
	 * @class MOVI.util.Overlay
	 * @constructor
	 * @param {ModelViewer} modelviewer The parent model viewer
	 * @param {Object[]} coords The coordinates of the polygone
	 * @param {Object} style (optional) A key map of style options. Available keys:
	 * <dl>
     * <dt>color</dt>
     * <dd># HEX color code</dd>
     * <dt>width</dt>
     * <dd>line width unit</dd>
	 * <dt>opacity</dt>
	 * <dd>the opacity value (between 0.0 - not visible and 1.0 - completely opaque)</dd>
	 * </dl>
	 */	
	MOVI.util.Overlay = function(modelviewer, coords, style) {
	
		if(coords.length==0) throw new Error("No coordinates specified for the Overlay!");
		if(!style) style = {};
		
		this.style = {};
		this.style.color = style.color || "#FF0000";
		this.style.width = style.width || 10.0;
		this.style.opacity = style.opacity || 0.4;
		
		this.modelviewer = modelviewer;
		this.coords = coords;
				
		var canvasEl = document.createElement("canvas");
		if(canvasEl.getContext){
			// client's browser supports canvas
			
			var ctx = canvasEl.getContext("2d");
			MOVI.util.Overlay.superclass.constructor.call(this, canvasEl, {});
			this.set("width", parseInt(this.modelviewer.canvas.getStyle("width")));
			this.set("height", parseInt(this.modelviewer.canvas.getStyle("height")));
			
			ctx.beginPath();
			ctx.lineJoin = "round";
			ctx.lineCap = "round";
			ctx.moveTo(coords[0].x, coords[0].y);
			for(var i=1; i<coords.length; i++) {
				ctx.lineTo(coords[i].x, coords[i].y);
			}
			ctx.moveTo(500,800);
			ctx.lineTo(500,1000);
			ctx.strokeStyle = 
				"rgba("+
				parseInt(this.style.color.substring(1,3), 16)+ ","+
				parseInt(this.style.color.substring(3,5), 16)+ ","+
				parseInt(this.style.color.substring(5,7), 16)+ ","+
				this.style.opacity+
				")";
			ctx.lineWidth = this.style.width;
			ctx.stroke();
			
		} else {
			// IE fallback: use VML
			
			// create xmlns
			if(!document.namespaces["vml"]) { 
				var stl = document.createStyleSheet(); 
				stl.addRule("vml\\:*", "behavior: url(#default#VML);"); 
				document.namespaces.add("vml", "urn:schemas-microsoft-com:vml"); 
			 }
			
			var shapeEl = document.createElement("vml:shape");
			shapeEl.setAttribute("coordsize", parseInt(this.modelviewer.canvas.getStyle("width")) + ", " +  parseInt(this.modelviewer.canvas.getStyle("height")));
			MOVI.util.Overlay.superclass.constructor.call(this, shapeEl, {});
			
			var strokeEl = new YAHOO.util.Element(document.createElement("vml:stroke"));
			strokeEl.set("opacity", this.style.opacity);
			strokeEl.set("color", this.style.color);
			strokeEl.set("weight", this.style.width);
			strokeEl.set("joinstyle", "round");
			strokeEl.set("endcap", "round");
			
			var fillEl = new YAHOO.util.Element(document.createElement("vml:fill"));
			fillEl.set("opacity", "0");
			
			var pathEl = new YAHOO.util.Element(document.createElement("vml:path"));
			var v = "M "+ Math.round(coords[0].x)+","+Math.round(coords[0].y) +" L";
			for(var i=1; i<coords.length; i++) {
				v += " " + Math.round(coords[i].x)+","+Math.round(coords[i].y);
				if(i!=coords.length-1) v += ",";
			}
			pathEl.set("v", v);
			
			this.appendChild(strokeEl);
			this.appendChild(fillEl);
			this.appendChild(pathEl);

		/*	var innerHTML = 
				"<vml:stroke opacity='" + this.style.opacity + "' color='" + this.style.color + "' weight='" + this.style.width +"' joinstyle='round' endcap='round'></vml:stroke>"+
				"<vml:fill opacity='0'></vml:fill>"+
				"<vml:path v='M "+ Math.round(coords[0].x)+","+Math.round(coords[0].y) +" L";
			for(var i=1; i<coords.length; i++) {
				innerHTML += " " + Math.round(coords[i].x)+","+Math.round(coords[i].y);
				if(i!=coords.length-1) innerHTML += ",";
			}
			innerHTML += 
				"'/>";
			this.set("innerHTML", innerHTML);*/
			
		}
		
		this.setStyle("position", "absolute");
		this.setStyle("left", "0px");
		this.setStyle("top", "0px");
		this.modelviewer.canvas.appendChild(this);
		
		this.update();
	};
	
	MOVI.extend(MOVI.util.Overlay, YAHOO.util.Element, {
		
		/**
		 * Update the overlay according to the current zoom level
		 * @method update
		 */
		update: function() {
			this.setStyle("width", Math.round(parseInt(this.modelviewer.canvas.getStyle("width"))*this.modelviewer.getZoomLevel()/100)+"px");
			this.setStyle("height", Math.round(parseInt(this.modelviewer.canvas.getStyle("height"))*this.modelviewer.getZoomLevel()/100)+"px");
		},
		
		/**
		 * Remove the overlay from the DOM
		 * @method remove
		 */
		remove: function() {
			this.modelviewer.canvas.removeChild(this);
		},
		
		/**
		 * Show the overlay
		 * @method show
		 */
		show: function() {
			this.setStyle("display", "block");
		},
		
		/**
		 * Hide the overlay
		 * @method hide
		 */
		hide: function() {
			this.setStyle("display", "none");
		}
		
	});
	
	/**
     * Attach Marker objects to the model to highlight a shape or a set of shapes
	 * by overlaying rectangles
     * @namespace MOVI.util
     * @class MOVI.util.Marker
     * @constructor
	 * @param {Shape|Shapes[]} shapes The shapes to mark
	 * @param {Object} nodeStyle (optional) A key map of CSS style properties to be attached
	 * to the node marking rectangles.
	 * @param {Object} edgeStyle (optional) A key map of style properties for all edge markings.
	 * Available properties:
	 * <dl>
     * <dt>color</dt>
     * <dd># HEX color code</dd>
     * <dt>width</dt>
     * <dd>line width unit</dd>
	 * <dt>opacity</dt>
	 * <dd>the opacity value (between 0.0 - not visible and 1.0 - completely opaque)</dd>
	 * </dl>
     */
    MOVI.util.Marker = function(shapes, nodeStyle, edgeStyle) {
		
		if(!shapes) shapes = [];
		if(!YAHOO.lang.isArray(shapes)) shapes = [shapes];
		
		this._icons = {
			north: null,
			west: null,
			south: null,
			east: null,
			northwest: null,
			southwest: null,
			northeast: null,
			southeast: null
		};
		
		this._nodeStyle = nodeStyle || {};
		this._edgeStyle = edgeStyle || {};
		this.overlays = {};

		this._shapes = {};
		// create shape rect elements
		for(var i = 0; i < shapes.length; i++) {
			var s = shapes[i];
			this._shapes[s.resourceId] = s;
			if(s.isNode()) {
				this.overlays[s.resourceId] = new YAHOO.util.Element(document.createElement('div'));
				this.overlays[s.resourceId].setStyle("position", "absolute");
				s.appendChild(this.overlays[s.resourceId]);
			} else {
				this.overlays[s.resourceId] = new MOVI.util.Overlay(s.getCanvas().getModelViewer(), s.getCoordinates(), this._edgeStyle);
			}
		}
		
		this.markerRect = new YAHOO.util.Element(document.createElement('div'));
		this.markerRect.set("className", _MARKER_RECT_CLASS_NAME);
	
		this._update();
	};
	
	/**
	 * The padding beween the marker's border and the shape's border.
	 * @property MOVI.util.Marker.PADDING
	 * @static
	 * @type integer
	 * @default 2
	 */
	MOVI.util.Marker.PADDING = 4;
	
	MOVI.util.Marker.prototype = {
		
		/**
		 * A key map containing the CSS style property definitions that are applied 
		 * to each node marking rectangle.
		 * @property _nodeStyle
		 * @type Object
		 * @private
		 */
		_nodeStyle: null,
		
		/**
		 * A key map containing the style property definitions that are applied 
		 * to each edge marking.
		 * @property _edgeStyle
		 * @type Object
		 * @private
		 */
		_edgeStyle: null,
		
		/**
		 * The class name that is specified for each shape marking rectangle.
		 * @property _className
		 * @type String
		 * @private
		 */
		_className: "",
		
		/**
		 * A key map containing all marked shapes with their resource IDs as keys
		 * @property _shapes
		 * @type Object
		 * @private
		 */
		_shapes: null,
		
		/**
		 * The callback object to be executed when the marker has changed
		 * @property _changedCallback
		 * @type Object
		 * @private
		 */
		_changedCallback: null,
		
		/**
		 * The callback object to be executed when the canvas object the marker belongs to is available
		 * @property _canvasAvailableCallback
		 * @type Object
		 * @private
		 */
		_canvasAvailableCallback: null,
		
		/**
		 * A hash map containing the icon elements with their orientation as key
		 * @property _icons
		 * @type Object
		 * @private
		 */
		_icons: null,
		
		/**
		 * A key map containing all shape overlay elements of the marker with the
		 * associated shape resource IDs as keys
		 * @property overlays
		 * @type { Integer : Element }
		 */
		overlays: null,
		
		/**
		 * The marker's outer rectangle element
		 * @property markerRect
		 * @type Element
		 */
		markerRect: null,
		
		/**
		 * The parent canvas element of this marker
		 * @property canvas
		 * @type Canvas
		 */
		canvas: null,
		
		/**
	     * Update style properties of the shape rectangle elements and update the bounds of the
		 * outer marking rectangle element
	     * @method _update
	     * @private
	     */
		_update: function() {
			
			var canvas = null;
			
			/* update shape marking rectangle elements */
			
			for(var i in this._shapes) {
				if(!YAHOO.lang.hasOwnProperty(this._shapes, i)) continue;
			
				var shape = this._shapes[i];
				
				var overlay = this.overlays[i];
				
				if(canvas==null) canvas = shape.getCanvas();
				var zoomFactor = canvas.getModelViewer().getZoomLevel() / 100;
				
				if(shape.isEdge()) {
					overlay.update();
					continue;
				}
				
				/* following code is only executed for node overlays */
				
				// apply styles
				for(var prop in this._nodeStyle) {
					if(!YAHOO.lang.hasOwnProperty(this._nodeStyle, prop)) continue;
					overlay.setStyle(prop, this._nodeStyle[prop]);
				}
				
				/* adjust position */
				
				// get border widths
				var bTWidth = parseInt(overlay.getStyle("border-top-width") || 0),
					bRWidth = parseInt(overlay.getStyle("border-right-width") || 0),
					bBWidth = parseInt(overlay.getStyle("border-bottom-width") || 0),
					bLWidth = parseInt(overlay.getStyle("border-left-width") || 0);
							
				bTWidth	= isNaN(bTWidth) ? 0 : bTWidth;
				bRWidth	= isNaN(bRWidth) ? 0 : bRWidth;
				bBWidth	= isNaN(bBWidth) ? 0 : bBWidth;
				bLWidth	= isNaN(bLWidth) ? 0 : bLWidth;
				
				var left = - MOVI.util.Marker.PADDING;
				var top = - MOVI.util.Marker.PADDING;
				if(YAHOO.env.ua.ie) {
					left = Math.round(left/2);
					top = Math.round(top/2);
				}
				
				var width = Math.round((shape.bounds.lowerRight.x
							- shape.bounds.upperLeft.x)*zoomFactor) + 2*MOVI.util.Marker.PADDING
							- bLWidth - bRWidth;
				var height = Math.round((shape.bounds.lowerRight.y
							 - shape.bounds.upperLeft.y)*zoomFactor) + 2*MOVI.util.Marker.PADDING
						 	 - bTWidth - bBWidth;				
			
				overlay.setStyle("left", left + "px");
				overlay.setStyle("top", top + "px");
				overlay.setStyle("width", width + "px");
				overlay.setStyle("height", height + "px");
				
				overlay.set("className", this._className);
			}
			
			/* update outer marking rectangle element */
			
			if(canvas!=null) {
				if(this.canvas==null) {
					this.canvas = canvas;
					
					// canvas is now set for the marker because the first shape has been added
					// now we can append the elements to the dom 
					// (all marked shapes have to belong to the same canvas)
					this.canvas.appendChild(this.markerRect); 
					for(var orientation in this._icons) {
						if(!YAHOO.lang.hasOwnProperty(this._icons, orientation)) continue;
						
						if(this._icons[orientation])
							this.canvas.appendChild(this._icons[orientation]); 
					}
					
					// now that we have our canvas and therefore the model viewer we can subscribe
					// to the zoom event
					this.canvas.getModelViewer().onZoomLevelChangeEnd.subscribe(function() {
						this._update();
					}, this, true);
					
					this._onCanvasAvailable();
				}
				
				// get border widths
				var bTWidth = parseInt(this.markerRect.getStyle("border-top-width")||0),
					bRWidth = parseInt(this.markerRect.getStyle("border-right-width")||0),
					bBWidth = parseInt(this.markerRect.getStyle("border-bottom-width")||0),
					bLWidth = parseInt(this.markerRect.getStyle("border-left-width")||0);
				
				bTWidth	= isNaN(bTWidth) ? 0 : bTWidth;
				bRWidth	= isNaN(bRWidth) ? 0 : bRWidth;
				bBWidth	= isNaN(bBWidth) ? 0 : bBWidth;
				bLWidth	= isNaN(bLWidth) ? 0 : bLWidth;
				
				var left = Math.round(this.getAbsBounds().upperLeft.x)*zoomFactor - MOVI.util.Marker.PADDING;
				var top = Math.round(this.getAbsBounds().upperLeft.y)*zoomFactor - MOVI.util.Marker.PADDING;
				var width = Math.round((this.getAbsBounds().lowerRight.x
							- this.getAbsBounds().upperLeft.x)*zoomFactor) + 2*MOVI.util.Marker.PADDING
							- bLWidth - bRWidth;
				var height = Math.round((this.getAbsBounds().lowerRight.y
							 - this.getAbsBounds().upperLeft.y)*zoomFactor) + 2*MOVI.util.Marker.PADDING
						 	 - bTWidth - bBWidth;

				this.markerRect.setStyle("left", left + "px");
				this.markerRect.setStyle("top", top + "px");
				this.markerRect.setStyle("width", width + "px");
				this.markerRect.setStyle("height", height + "px");
			}
			
			/* update icons */
			this._updateIcons()
		},
		
		/**
	     * Update positions of the icons appended to the marker
	     * @method _updateIcons
	     * @private
	     */
		_updateIcons: function() {
			
			if(this.canvas)
				var zoomFactor = this.canvas.getModelViewer().getZoomLevel() / 100;
			else
				var zoomFactor = 1.0;
			
			var bounds = this.getAbsBounds();
			for(var orientation in this._icons) {
				if(!YAHOO.lang.hasOwnProperty(this._icons, orientation)) continue;
				
				var left, top, margin;
				var icon = this._icons[orientation];
				if(icon) {
					
					var width = parseInt(icon.getStyle("width"), 10); 
					var height = parseInt(icon.getStyle("height"), 10);

					if(orientation=="north") {
						left = Math.round(bounds.upperLeft.x*zoomFactor + ((bounds.lowerRight.x-bounds.upperLeft.x)*zoomFactor - width)/2);
						top = Math.round(bounds.upperLeft.y*zoomFactor - height);
						margin = -_ICON_MARGIN + "px 0 0 0";
					} else if(orientation=="east") {
						left = bounds.lowerRight.x*zoomFactor;
						top = Math.round(bounds.upperLeft.y*zoomFactor + ((bounds.lowerRight.y-bounds.upperLeft.y)*zoomFactor - height)/2);
						margin = "0 0 0 " + _ICON_MARGIN + "px";
					} else if(orientation=="south") {
						left = Math.round(bounds.upperLeft.x*zoomFactor + ((bounds.lowerRight.x-bounds.upperLeft.x)*zoomFactor - width)/2);
						top = bounds.lowerRight.y*zoomFactor;
						margin = _ICON_MARGIN + "px 0 0 0";
					} else if(orientation=="west") {
						left = bounds.upperLeft.x*zoomFactor - width;
						top = Math.round(bounds.upperLeft.y*zoomFactor + ((bounds.lowerRight.y-bounds.upperLeft.y)*zoomFactor - height)/2);
						margin = "0 0 0 " + -_ICON_MARGIN + "px";
					} else if(orientation=="northeast") {
						left = bounds.lowerRight.x*zoomFactor;
						top = bounds.upperLeft.y*zoomFactor - height;
						margin = -_ICON_MARGIN + "px 0 0 " + _ICON_MARGIN + "px";
					} else if(orientation=="southeast") {
						left = bounds.lowerRight.x*zoomFactor;
						top = bounds.lowerRight.y*zoomFactor;
						margin = _ICON_MARGIN + "px 0 0 " + _ICON_MARGIN + "px";
					} else if(orientation=="northwest") {
						left = bounds.upperLeft.x*zoomFactor - width;
						top = bounds.upperLeft.y*zoomFactor - height;
						margin = -_ICON_MARGIN + "px 0 0 " + -_ICON_MARGIN + "px";
					} else if(orientation=="southwest") {
						left = bounds.upperLeft.x*zoomFactor - width;
						top = bounds.lowerRight.y*zoomFactor;
						margin = _ICON_MARGIN + "px 0 0 " + -_ICON_MARGIN + "px";
					}
					icon.setStyle("left", left + "px");
					icon.setStyle("top", top + "px");
					icon.setStyle("margin", margin);
				}
			}
		},
		
		/**
	     * Executes the user-specified callback when the marker changed
	     * @method _onChanged
		 * @private
	     */
		_onChanged: function() {
			if(!this._changedCallback) return;
			this._changedCallback.callback.call(
				this._changedCallback.scope,
				this,
				this._changedCallback.data
			);
		},
		
		/**
	     * Executes the user-specified callback when the canvas object the marker belongs to is available
	     * @method _onCanvasAvailable
		 * @private
	     */
		_onCanvasAvailable: function() {
			if(!this._canvasAvailableCallback) return;
			this._canvasAvailableCallback.callback.call(
				this._canvasAvailableCallback.scope,
				this,
				this._canvasAvailableCallback.data
			);
		},
		
		/**
	     * Wrapper for setting style properties of all node marking rectangle elements
	     * @method setNodeStyle
		 * @param {String} property The property
		 * @param {String} value The value
	     */
		setNodeStyle: function(property, value) {
			this._nodeStyle[property] = value;
			this._update();
		},
		
		/**
	     * Returns the value for the specified style property that is applied to each 
		 * node marking rectangle element
	     * @method getNodeStyle
		 * @param {String} property The property
		 * @returns {String} The value of the property
	     */
		getNodeStyle: function(property) {
			return this._nodeStyle[property];
		},
		
		/**
	     * Wrapper for setting the class name for all node marking rectangle elements
	     * @method setNodeClassName
		 * @param {String} className The class name value
	     */
		setNodeClassName: function(className) {
			this._className = className;
			this._update();
		},
		
		/**
	     * Return the class name applied for all node marking rectangle elements
	     * @method getNodeClassName
		 * @returns {String} The applied class name
	     */
		getNodeClassName: function() {
			return this._className;
		},
		
		/**
	     * Deprecated alias for setNodeClassName
	     * @method setRectStyle
	     */
		setRectStyle: function(property, value) {
			this.setNodeStyle(property, value);
		},
		
		/**
	     * Deprecated alias for setNodeClassName
	     * @method getRectStyle
	     */
		getRectStyle: function(property) {
			return this.getNodeStyle(property);
		},
		
		/**
	     * Deprecated alias for setNodeClassName
	     * @method setRectClassName
	     */
		setRectClassName: function(className) {
			this.setNodeClassName(className);
		},
		
		/**
	     * Deprecated alias for getNodeClassName
	     * @method getRectClassName
	     */
		getRectClassName: function() {
			return this.getNodeClassName();
		},
		
		/**
	     * Wrapper for setting style properties of all edge markings
	     * @method setEdgeStyle
		 * @param {String} property The property
		 * Available properties:
		 * <dl>
	     * <dt>color</dt>
	     * <dd># HEX color code</dd>
	     * <dt>width</dt>
	     * <dd>line width unit</dd>
		 * <dt>opacity</dt>
		 * <dd>the opacity value (between 0.0 - not visible and 1.0 - completely opaque)</dd>
		 * </dl>
		 * @param {String} value The value
	     */		
		setEdgeStyle: function(property) {
			this._edgeStyle[property] = value;
			for(var key in this._shapes) {
				if(!YAHOO.lang.hasOwnProperty(this._shapes, key)) continue;
				
				this.overlays[key].remove();
				var coords = this.overlays[key].coords;
				var modelviewer = this.overlays[key].modelviewer;
				delete this.overlays[key];
				this.overlays[key] = new MOVI.util.Overlay(modelviewer, coords, this._edgeStyle);
			}
		},
		
		/**
	     * Returns the value for the specified style property that is applied to each 
		 * edge marking 
	     * @method getEdgeStyle
		 * @param {String} property The property
		 * Available properties:
		 * <dl>
	     * <dt>color</dt>
	     * <dd># HEX color code</dd>
	     * <dt>width</dt>
	     * <dd>line width unit</dd>
		 * <dt>opacity</dt>
		 * <dd>the opacity value (between 0.0 - not visible and 1.0 - completely opaque)</dd>
		 * </dl>
		 * @returns {String} The value of the property
	     */
		getEdgeStyle: function(property) {
			return this._edgeStyle[property];
		},
		
		/**
	     * Add a shape to the marker
	     * @method addShape
		 * @param {Shape} shape The shape object to add
	     */
		addShape: function(shape) {
			if(this._shapes[shape.resourceId])
				return;
			
			this._shapes[shape.resourceId] = shape;
			
			if(shape.isNode()) {
				var rect = new YAHOO.util.Element(document.createElement('div'));
				rect.set("className", this.getRectClassName());
				rect.setStyle("position", "absolute");
				shape.appendChild(rect);
				this.overlays[shape.resourceId] = rect;
			} else {
				this.overlays[s.resourceId] = new MOVI.util.Overlay(shape.getCanvas().getModelViewer(), shape.getCoordinates());
			}
			
			this._update();
			this._onChanged();
		},
		
		/**
	     * Remove a shape from the marker
	     * @method removeShape
		 * @param {Shape} shape The shape object to be removed
	     */
		removeShape: function(shape) {
			if(shape.isNode())
				shape.removeChild(this.overlays[shape.resourceId]);
			else
				this.overlays[shape.resourceId].remove();
			delete this.overlays[shape.resourceId];
			delete this._shapes[shape.resourceId];
			this._update();
			this._onChanged()
		},
		
		/**
	     * Remove all shapes from the marker
	     * @method removeAllShapes
	     */
		removeAllShapes: function() {
			for(var key in this._shapes) {
				if(!YAHOO.lang.hasOwnProperty(this._shapes, key)) continue;
				
				if(this._shapes[key].isNode())
					this._shapes[key].removeChild(this.overlays[key]);
				else
					this.overlays[key].remove();
				
				delete this.overlays[key];
				delete this._shapes[key];
			}
			this._update();
			this._onChanged()
		},
		
		/**
	     * Return all marked shapes
	     * @method getShapes
		 * @param {[Shape]} An array of the marked Shape objects
	     */
		getShapes: function() {
			var marked = new Array();
			for(var key in this._shapes) {
				if(!YAHOO.lang.hasOwnProperty(this._shapes, key)) continue;
				marked.push(this._shapes[key]);
			}
				
			return marked;
		},
		
		/**
	     * Show the marker
	     * @method show
	     */
		show: function() {
			this.markerRect.setStyle("display", "block");
			this.setRectStyle("display", "block");
			for(var key in this._shapes) {
				if(!YAHOO.lang.hasOwnProperty(this._shapes, key)) continue;
				if(this._shapes[key].isEdge()) this.overlays[key].show();
			}
			for(var orientation in this._icons) {
				if(!YAHOO.lang.hasOwnProperty(this._icons, orientation)) continue;
				if(this._icons[orientation]) 
					this._icons[orientation].setStyle("display", "block");
			}
		},
		
		/**
	     * Hide the marker
	     * @method hide
	     */
		hide: function() {
			this.markerRect.setStyle("display", "none");
			this.setRectStyle("display", "none");
			for(var key in this._shapes) {
				if(!YAHOO.lang.hasOwnProperty(this._shapes, key)) continue;
				if(this._shapes[key].isEdge()) this.overlays[key].hide();
			}
			for(var orientation in this._icons) {
				if(!YAHOO.lang.hasOwnProperty(this._icons, orientation)) continue;
				if(this._icons[orientation]) 
					this._icons[orientation].setStyle("display", "none");
			}
		},
		
		/**
		 * Fade in the rec
		 */		
		fadeIn: function() {
			if(YAHOO.env.ua.ie){ return }
			for(var key in this.overlays ){
				if (typeof key != "string") {return}
				this.overlays[key].setStyle("opacity", 0);	
				var anim = new YAHOO.util.ColorAnim(this.overlays[key], { opacity: { to: 1 } }, 0.4, YAHOO.util.Easing.easeOut);
				anim.animate();	
			}
		},
		
		
		/**
		 * Fade out the rectangle, call fn when complete
		 * @param {Object} fn
		 */
		fadeOut: function(fn){
			if(YAHOO.env.ua.ie){ 
				if (fn instanceof Function) { fn() }
				return 
			}
			var anim;
			for(var key in this.overlays ){
				if (typeof key != "string") {return}
				anim = new YAHOO.util.ColorAnim(this.overlays[key], { opacity: { to: 0 } }, 0.4, YAHOO.util.Easing.easeOut);
				anim.animate();	
			}
			if (anim && fn instanceof Function){
				anim.onComplete.subscribe(fn) 
			}
		},
		
		/**
	     * Toggle show/hide the marker
	     * @method toggle
	     */
		toggle: function() {
			if(this.markerRect.getStyle("display")=="none")
				this.show();
			else 
				this.hide();
		},
		
		/**
	     * Remove all marker elements from the DOM
	     * @method remove
	     */
		remove: function() {
			this.removeAllShapes();
			this.markerRect.get("element").parentNode.removeChild(this.markerRect.get("element"));
			delete this.markerRect;
		},
		
		/**
	     * Returns the marker's absolute bounds coordinates. 'Absolute' means
		 * relative to the canvas element rather than relative to the document
	     * @method getAbsBounds
		 * @return {Object} The absolute marker bounds accessible as 
		 * { upperLeft: {x:Number,y:Number}, lowerRight: {x:Number,y:Number} }
	     */
		getAbsBounds: function() {
			if(this._cachedAbsBounds) return this._cachedAbsBounds;
			var upperLeft  = {x: undefined, y: undefined};
				lowerRight = {x: undefined, y: undefined};
			for(var i in this._shapes) {
				if(!YAHOO.lang.hasOwnProperty(this._shapes, i)) continue;
				
				var bounds = this._shapes[i].getAbsBounds();
				if(upperLeft.x == undefined || bounds.upperLeft.x < upperLeft.x)
					upperLeft.x = bounds.upperLeft.x;
				if(upperLeft.y == undefined || bounds.upperLeft.y < upperLeft.y)
					upperLeft.y = bounds.upperLeft.y;
				if(lowerRight.x == undefined || bounds.lowerRight.x > lowerRight.x)
					lowerRight.x = bounds.lowerRight.x;
				if(lowerRight.y == undefined || bounds.upperLeft.y > lowerRight.y)
					lowerRight.y = bounds.lowerRight.y;
			}
			this._cachedAbsBounds = { upperLeft: upperLeft, lowerRight: lowerRight };
			return this._cachedAbsBounds;
		},
		
		/**
		 * Specify callback to be executed when the marker changes
		 * (shapes are added to or removed from the marker)
		 * @param {Function} callback The callback method
		 * @param {Object} scope (optional) The execution scope of the callback 
		 * (in none is specified the context of the Marker object is used)
		 * @param {Object} data (optional) An optional data object to pass to the callback method
		 * @method onChanged
		 */
		onChanged: function(callback, scope, data) {
			if(!YAHOO.lang.isFunction(callback)) {
				throw new TypeError("Specified callback is not a function.", "error", "marker.js");
				return;
			}
			if(!scope) scope = this;
			this._changedCallback = {
				callback: callback,
				scope: scope,
				data: data
			};
		},
		
		/**
		 * Specify callback to be executed when the canvas object the marker belongs to is available
		 * @param {Function} callback The callback method
		 * @param {Object} scope (optional) The execution scope of the callback 
		 * (in none is specified the context of the Marker object is used)
		 * @param {Object} data (optional) An optional data object to pass to the callback method
		 * @method onCanvasAvailable
		 */
		onCanvasAvailable: function(callback, scope, data) {
			if(!YAHOO.lang.isFunction(callback)) {
				throw new TypeError("Specified callback is not a function.", "error", "marker.js");
				return;
			}
			if(!scope) scope = this;
			this._canvasAvailableCallback = {
				callback: callback,
				scope: scope,
				data: data
			};
		},
		
		/**
		 * Append an icon element to the bounds of the marker
		 * @param {String} orientation The orientation where to position the icon.
		 * Possible values: 'north', 'west', 'south', 'east', 'northwest', 'southwest', 'northeast', 'southeast'
		 * @param {HTMLElement|String} icon An HTMLElement object representing the icon, or a String specifying
		 * the URL of an image for the icon.
		 * @return {Element} A reference to the new icon element
		 * @method addIcon
		 */
		addIcon: function(orientation, icon) {
			if(!YAHOO.lang.isString(orientation)) {
				throw new TypeError("The orientation paramter is expected to be of type String", "error", "marker.js");
				return;
			}
			
			if(YAHOO.lang.isString(icon)) {
				var img = new Image();
				var self = this;
				img.onload = function() {
					self._updateIcons.call(self);
				};
				img.src = icon;
				
				icon = new YAHOO.util.Element(img);
			} else {
				icon = new YAHOO.util.Element(icon);
			}
			
			icon.set("className", _ICON_CLASS_NAME);

			for(var key in this._icons) {
				if(!YAHOO.lang.hasOwnProperty(this._icons, key)) continue;
				
				if(key === orientation) {
					if(this._icons[orientation]) this.removeIcon(orientation);
					if(this.canvas) this.canvas.appendChild(icon);
					this._icons[key] = icon;
					this._updateIcons();
					return icon;
				}
			}
			
			throw new Error("The specified orientation is not valid.", "error", "marker.js");
		},
		
		/**
		 * Remove the icon that currently resides in the specified orientation from the marker 
		 * @param {String} orientation The orientation of the icon to remove.
		 * @method removeIcon
		 */
		removeIcon: function(orientation) {
			if(this._icons[orientation] && this.canvas) 
				this.canvas.removeChild(this._icons[orientation]);
			this._icons[orientation] = null;
		},
		
		/**
		 * Returns the icon element at the specified orientation.
		 * @param {String} orientation The orientation of the icon
		 * @return {Element} The icon at the specified orientation (null if no icon resides at that
		 * orientation)
		 * @method getIcon
		 */
		getIcon: function(orientation) {
			return this._icons[orientation];
		}
		
	}
	
})();/**
 * Copyright (c) 2009
 * Jan-Felix Schwarz
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

MOVI.namespace("util");

(function() {
	
	var _BUBBLE_VISIBLE_CLASS_NAME 		= "movi-bubble-visible",
		_BUBBLE_HIDDEN_CLASS_NAME 		= "movi-bubble-hidden",
		_BUBBLE_UL_CLASS_NAME 			= "movi-bubble-ul",
		_BUBBLE_UR_CLASS_NAME 			= "movi-bubble-ur",
		_BUBBLE_LL_CLASS_NAME 			= "movi-bubble-ll",
		_BUBBLE_LR_CLASS_NAME 			= "movi-bubble-lr",
		_BUBBLE_BORDERTOP_CLASS_NAME 	= "movi-bubble-bt",
		_BUBBLE_BORDERBOTTOM_CLASS_NAME	= "movi-bubble-bb",
		_BUBBLE_BORDERLEFT_CLASS_NAME   = "movi-bubble-bl",
		_BUBBLE_BORDERRIGHT_CLASS_NAME  = "movi-bubble-br",
		_BUBBLE_ARROW_LEFT_CLASS_NAME 	= "movi-bubble-al",
		_BUBBLE_ARROW_RIGHT_CLASS_NAME 	= "movi-bubble-ar",
		_BUBBLE_ARROW_TOP_CLASS_NAME 	= "movi-bubble-at",
		_BUBBLE_ARROW_BOTTOM_CLASS_NAME	= "movi-bubble-ab",
		_BUBBLE_CONTENT_CLASS_NAME	 	= "movi-bubble-bc",
		_BUBBLE_CLOSEBUTTON_CLASS_NAME 	= "movi-bubble-closebutton"
		
	var Event	= YAHOO.util.Event,
		Element	= YAHOO.util.Element;
	
	/**
	 * Create an Annotation for a Marker to enrich the model with additional information 
	 * and functionality. The Annotation renders a speech bubble containing arbitrary
	 * XHTML content and attachs it to the Marker.
	 * @namespace MOVI.util
	 * @class MOVI.util.Annotation
	 * @extends YAHOO.util.Element
	 * @constructor
	 * @param {Marker} marker The Marker to attach the Annotation to
	 * @param {String} content The Annotation's inner HTML content 
	 */
    MOVI.util.Annotation = function(marker, content) {
	
		if(!marker) {
			throw new Error("No marker specified for annotation.", "annotation.js");
			return false;
		}
		
		if(!YAHOO.lang.isString(content)) {
			throw new TypeError("A String is expected for the annotation's content.", "annotation.js");
		}
		
		// create host element
        el = _createHostElement.call(this);
		
		MOVI.util.Annotation.superclass.constructor.call(this, el, {}); 
		
		_createBubble.call(this, content);
		
		this._content = new Element(
			this.getElementsByClassName(_BUBBLE_CONTENT_CLASS_NAME)[0]);
		
		this._marker = marker;
		this._marker.onChanged(this._update, this);
		
		(new Element(this.getElementsByClassName(_BUBBLE_CLOSEBUTTON_CLASS_NAME)[0]))
			.addListener("click", this._close, this, this);
		
		// don't bubble mouse events on annotations
		this.addListener("mouseover", function(ev) { Event.stopPropagation(ev) });
		this.addListener("mouseout", function(ev) { Event.stopPropagation(ev) });
		this.addListener("click", function(ev) { Event.stopPropagation(ev) });
		this.addListener("mousedown", function(ev) { Event.stopPropagation(ev) });
		
		// allow user to select text 
		this.setStyle("-moz-user-select", "text");    // Gecko-based, Mozilla
		this.setStyle("-webkit-user-select", "text"); // Safari 3.0
		this.setStyle("-khtml-user-select", "text");  // Safari 2.0
		if (YAHOO.env.ua.ie > 0)
			this.set("unselectable", "off");           // IE
			
		this._update();
	}

	MOVI.extend(MOVI.util.Annotation, Element, {
		
		/**
		 * The Element containing the specified content as inner HTML.
		 * @property _contentElement
		 * @type Element
		 * @private
		 */
		_contentElement: null,
		
		/**
		 * The marker that the annotation is attached to
		 * @property _marker
		 * @type Marker
		 * @private
		 */
		_marker: null,
		
		/**
		 * The canvas that the annotation is attached to
		 * @property _canvas
		 * @type Canvas
		 * @private
		 */
		_canvas: null,
		
		/**
		 * The callback that is executed when the annotation bubble is closed
		 * @property _closeCallback
		 * @type Object
		 * @private
		 */
		_closeCallback: null,
		
		/**
	     * Callback that is executed when the close button is clicked
	     * @method _onClose
		 * @private
	     */
		_close: function() {
			this.hide();
			if(this._closeCallback) {
				this._closeCallback.callback.call(
					this._closeCallback.callback.scope,
					this,
					this._closeCallback.callback.data
				);
			}
		},
		
		/**
	     * Update the position of the annotation bubble
	     * @method _update
		 * @private
	     */
		_update: function() {
			var zoomFactor = 1;
			if(!this._canvas) {
				this._canvas = this._marker.canvas;
				if(this._canvas) {
					this._canvas.appendChild(this);
					// now that we have our canvas and therefore the model viewer we can subscribe
					// to the zoom event
					this._canvas.getModelViewer().onZoomLevelChangeEnd.subscribe(function() {
						this._update();
					}, this, true);
					
					zoomFactor = this._canvas.getModelViewer().getZoomLevel() / 100;
					
				} else {
					// canvas not available, update again when it becomes available
					this._marker.onCanvasAvailable(this._update, this);
				}
			} else {
				zoomFactor = this._canvas.getModelViewer().getZoomLevel() / 100;
			}
			
			var bounds = this._marker.getAbsBounds();
			var left = Math.round(bounds.upperLeft.x * zoomFactor);
			var right = Math.round(bounds.lowerRight.x * zoomFactor);
			var top = Math.round((bounds.upperLeft.y + (bounds.lowerRight.y-bounds.upperLeft.y)*0.7) * zoomFactor);
			var w = parseInt(this._canvas.getStyle("width"), 10);

			if(left>(w/2))
				this.setPosition(left, top);
			else 
				this.setPosition(right, top);
			
		},
		
		/**
	     * Set the absolute position of the annotation bubble 
	     * @method setPosition
		 * @param {Integer} x The x coordinate of the position the bubble points to
		 * @param {Integer} y The y coordinate of the position the bubble points to
	     */
		setPosition: function(x, y) {
			var w = parseInt(this._canvas.getStyle("width"), 10);
			if( w && w>300 && (x>(w/2)) ) {
				// align the annotation to the left (only if the canvas is wider than 300px)
				this.setStyle("right", (w-x) + "px");
				this.setStyle("left", "");
				this.addClass("movi-bubble-right");
			} else {
				// align the annotation to the right
				this.setStyle("left", x + "px");
				this.setStyle("right", "");
				this.removeClass("movi-bubble-right");
			}
			this.setStyle("top", y + "px");
		},
		
		/**
	     * Show the annotation bubble
	     * @method show
	     */
		show: function() {
			//this._marker.show();
			this.replaceClass(_BUBBLE_HIDDEN_CLASS_NAME, _BUBBLE_VISIBLE_CLASS_NAME);
			this.bringToFront();
			this.visible = true;
		},
		
		/**
	     * Hide the annotation bubble
	     * @method hide
	     */
		hide: function() {
			this.replaceClass(_BUBBLE_VISIBLE_CLASS_NAME, _BUBBLE_HIDDEN_CLASS_NAME);
			this.visible = false;
		},
		
		/**
	     * Toggle show/hide the annotation bubble
	     * @method toggle
	     */
		toggle: function() {
			if(this.isVisible())
				this.hide();
			else 
				this.show();
		},
		
		/**
		 * Returns true if the annotation is visible
		 * @method isVisible
		 * @returns {Boolean} 
		 */
		isVisible: function() {
		    return "undefined" == typeof(this.visible) ? this.hasClass(_BUBBLE_VISIBLE_CLASS_NAME) : this.visible;
		},
		
		/**
	     * Remove the element from the DOM
	     * @method remove
	     */
		remove: function() {
			this.get("element").parentNode.removeChild(this.get("element"));
		},
		
		/**
	     * Specfiy a callback that is executed when the annotation bubble is
		 * closed using the close button
	     * @method onClose
		 * @param {Function} callback The callback function
		 * @param {Object} scope The object to use as the scope for the callback
		 * @param {Any} data The variable to pass to the callback function
	     */
		onClose: function(callback, scope, data) {
			this._closeCallback = {
				callback: callback,
				scope: scope,
				data: data
			};
		},
		
		/**
	     * Brings the annotation to the front of all the other annotations on the canvas
	     * @method bringToFront
	     */
		bringToFront: function() {
			// TODO: method is outdated, as the markers z-index is updated and the annotation
			// node is no longer a child of it. also, this method has very poor performance in IE6
			return; // skip all calls of this method for the first
			
			// if element has not been added to the dom do nothing
			if(!this.get("element").parentNode) return;
			
			MOVI.util.Annotation
			
			// detemine the maximal z-index of markers of visible annotation elements
			var maxZIndex = 0;
			var canvasEl = new Element(this.get("element").parentNode.parentNode);
			var elements = canvasEl.getElementsByClassName(_BUBBLE_VISIBLE_CLASS_NAME);
			for(var key in elements) {
				if(!YAHOO.lang.hasOwnProperty(elements, key)) continue;
				var zIndex = parseInt((new Element(elements[key].parentNode)).getStyle("z-index"), 10);
				if(zIndex>maxZIndex) maxZIndex = zIndex;
			}
			// set the z-index of parent marker to maximum+1*/
			this._marker.markerRect.setStyle("z-index", maxZIndex+1);
		},
		
		/**
	     * Specfiy a new marker to which the annotation is attached
	     * @method setMarker
		 * @param {MOVI.util.Marker} marker The marker element the annotation shall be attached to
	     */
		setMarker: function(marker) {
			this._canvas = undefined;
			this._marker = marker;
			this._marker.onChanged(this._update, this);
			this._update();
		},
		
		/**
	     * Returns the Marker instance this annotation is attached to
	     * @method getMarker
	     */
		getMarker: function(marker) {
			return this._marker;
		}
		
	});
	
	/**
     * Create the host element.
	 * @method
	 * @private
     */
    var _createHostElement = function() {
        var el = document.createElement('div');
        return el;
    };

	/**
     * Create the bubble elements inside the host element.
	 * @method
	 * @private
     */
	var _createBubble = function(content) {
		
		var isIE6 = YAHOO.env.ua.ie === 6;
		
		this.set("className", _BUBBLE_HIDDEN_CLASS_NAME);
		
		this.set("innerHTML", 	
						["<div class=\"" + _BUBBLE_UL_CLASS_NAME + "\">",
							(isIE6 ? "<img class=\""+ _BUBBLE_UL_CLASS_NAME +"\">":""),
							"<div class=\"" + _BUBBLE_UR_CLASS_NAME + "\">",
								(isIE6 ? "<img class=\""+ _BUBBLE_UR_CLASS_NAME +"\">":""),
					   			"<div class=\"" + _BUBBLE_LL_CLASS_NAME + "\">",
									(isIE6 ? "<img class=\""+ _BUBBLE_LL_CLASS_NAME +"\">":""),
									"<div class=\"" + _BUBBLE_LR_CLASS_NAME + "\">",
										(isIE6 ? "<img class=\""+ _BUBBLE_LR_CLASS_NAME +"\">":""),
								    	"<div class=\"" + _BUBBLE_BORDERTOP_CLASS_NAME + "\"></div>",
								    	"<div class=\"" + _BUBBLE_BORDERLEFT_CLASS_NAME + "\">",
											(isIE6 ? "<img class=\""+ _BUBBLE_BORDERLEFT_CLASS_NAME +"\">":""),
								    		"<div class=\"" + _BUBBLE_BORDERRIGHT_CLASS_NAME + "\">",
												(isIE6 ? "<img class=\""+ _BUBBLE_BORDERRIGHT_CLASS_NAME +"\">":""),
								   		   		"<div class=\"" + _BUBBLE_CONTENT_CLASS_NAME + "\">",
										    		"<div class=\"" + _BUBBLE_CLOSEBUTTON_CLASS_NAME + "\"></div>",
								            			content,
								         			"</div>",
								        		"</div>",
											"</div>",
								        "<div class=\"" + _BUBBLE_BORDERBOTTOM_CLASS_NAME + "\"></div>",
					   				"</div>",
								"</div>",
					   		"</div>",
						"</div>",
					    "<div class=\"" + _BUBBLE_ARROW_LEFT_CLASS_NAME + "\"/>"].join(""));
	}
	
})();/**
 * Copyright (c) 2009
 * Jan-Felix Schwarz
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


// Declare the MOVI.widget namespace
MOVI.namespace("widget");

(function() {
	
	var _NAVIGATOR_CLASS_NAME = "movi-navigator",
		_CLIPPING_RECT_CLASS_NAME = "movi-navigator-clippingrect",
		_NAVIGATOR_IMG_CLASS_NAME = "movi-navigator-img";
	
	/**
     * Add image to the host element and create the clipping rectangle element.
	 * Ensure that host element's position is not static to allow absolute
	 * positioning of the clipping rect relative to the host element.
     * @method _setUpHostElement
     * @private
     */
	var _setUpHostElement = function() {
		if(this.getStyle("position")=="static")
			this.setStyle("position", "relative"); 
		this.set("innerHTML", 
				"<img class=\"" + _NAVIGATOR_IMG_CLASS_NAME + "\" />" + 
				"<div class=\"" + _CLIPPING_RECT_CLASS_NAME + "\" />");
	};
	
	/**
     * The ModelNavigator widget is a UI control that enables users to navigate
	 * in a model displayed in a ModelViewer component. A rectangle indicates the
	 * model clipping.
     * @namespace MOVI.widget
     * @class MOVI.widget.ModelNavigator
     * @extends YAHOO.util.Element
     * @constructor
     * @param {HTMLElement | String } el The id of the container DIV element that will 
	 * wrap the ModelNavigator, or a reference to a DIV element. The DIV element must
	 * exist in the document.
	 * @modelviewer {ModelViewer} modelviewer The ModelViewer that is navigated 
     */
    MOVI.widget.ModelNavigator = function(el, modelviewer) {
	
		if(!modelviewer) {
			throw new Error("No model viewer specified for model navigator", "modelnavigator.js");
			return false;
		}
	
		this.modelviewer = modelviewer;
		
    	MOVI.widget.ModelNavigator.superclass.constructor.call(this, el); 

		_setUpHostElement.call(this);
		
		this._clippingRect = new YAHOO.util.Element(
			this.getElementsByClassName(_CLIPPING_RECT_CLASS_NAME)[0]);
			
		this._image = new YAHOO.util.Element(
			this.getElementsByTagName("img")[0]);
			
		this.addClass(_NAVIGATOR_CLASS_NAME);
		
		// add event listeners to modelviewer's scrollbox
		var scrollboxId = this.modelviewer.getScrollboxEl().get("id");
		if(!scrollboxId || !YAHOO.util.Event.addListener(
								scrollboxId, "scroll", this.update, this, true)) {
			throw new Error("Could not add event listener to scrollbox", "modelnavigator.js");
		}
		
		// add event listener for zooming
		this.modelviewer.onZoomLevelChange.subscribe(this.update, this, true);
		
		// add event listeners to modelnavigator
		this._clippingRect.addListener("mousedown", this._onClippingRectDrag, this, this, true);
		this._clippingRect.addListener("click", function(ev) { YAHOO.util.Event.stopEvent(ev); }, this, this, true);
		this.addListener("click", this._onClick, this, this, true);
		YAHOO.util.Event.addListener(document, "mouseup", this._onMouseUp, this, this, true);
		
		this._absXY = [];
		this._mouseOffset = {x: 0, y: 0};
		
		this.update();
		
    };

	MOVI.extend(MOVI.widget.ModelNavigator, YAHOO.util.Element, {
		
		/**
		 * The clipping rectangle DIV element.
		 * @property _clippingRect
		 * @type Element
		 * @private
		 */
		_clippingRect: null,
		
		/**
	     * The image element
	     * @property _image
		 * @type Element
		 * @private
	     */
		_image: null,
		
		/**
	     * The absolute position [x, y] of the navigator element 
	     * @property _absXY
		 * @type Array
		 * @private
	     */
		_absXY: null,
		
		/**
	     * The offset of the mouse pointer when dragging the clipping rect
	     * @property _mouseOffset
		 * @type Object
		 * @private
	     */
		_mouseOffset: null,
		
		/**
		 * The ModelViewer that is navigated
		 * @property modelviewer
		 * @type ModelViewer
		 */
		modelviewer: null,
		
		/**
		 * @method _onClippingRectDrag
		 * @private
		 */
		_onClippingRectDrag: function(ev) {
			YAHOO.util.Event.preventDefault(ev);
			this._absXY = YAHOO.util.Dom.getXY(this);
			var mouseAbsXY = YAHOO.util.Event.getXY(ev);
			var clippingRectAbsXY = YAHOO.util.Dom.getXY(this._clippingRect);
			this._mouseOffset.x = mouseAbsXY[0] - clippingRectAbsXY[0];
			this._mouseOffset.y = mouseAbsXY[1] - clippingRectAbsXY[1];
			this.addListener("mousemove", this._onClippingRectMove, this, this, true);
		},
		
		/**
		 * @method _onMouseUp
		 * @private
		 */
		_onMouseUp: function(ev) {
			YAHOO.util.Event.preventDefault(ev);
			this.removeListener("mousemove", this._onClippingRectMove);
		},
		
		/**
		 * @method _onClick
		 * @private
		 */
		_onClick: function(ev) {
			YAHOO.util.Event.preventDefault(ev);
			this._absXY = YAHOO.util.Dom.getXY(this);
			var mouseAbsXY = YAHOO.util.Event.getXY(ev);
			var navigatorWidth = parseInt(this.getStyle("width"), 10);
			var scale =  this.modelviewer.getImgWidth() / navigatorWidth;
			var zoomFactor = this.modelviewer.getZoomLevel() / 100;
			var centerX = Math.round(scale * (mouseAbsXY[0] - this._absXY[0]) * zoomFactor);
			var centerY = Math.round(scale * (mouseAbsXY[1] - this._absXY[1]) * zoomFactor);
			this.modelviewer.centerScrollTo(centerX, centerY);
		},
		
		/**
		 * Callback method that is executed when the clipping rectangle is moved
		 * @method _scrollMove
		 * @private
		 */
		_onClippingRectMove: function(ev) {
			YAHOO.util.Event.preventDefault(ev);
			var mouseAbsXY = YAHOO.util.Event.getXY(ev);
			var x = mouseAbsXY[0] - this._absXY[0] - this._mouseOffset.x,
				y = mouseAbsXY[1] - this._absXY[1] - this._mouseOffset.y;
			var navigatorWidth = parseInt(this.getStyle("width"), 10);
			var scale =  this.modelviewer.getImgWidth() / navigatorWidth;
			var zoomFactor = this.modelviewer.getZoomLevel() / 100;
			var scrollboxEl = this.modelviewer.getScrollboxEl();
			scrollboxEl.set("scrollLeft", Math.round(scale * x * zoomFactor));
			scrollboxEl.set("scrollTop", Math.round(scale * y * zoomFactor));
		},
		
		/**
	     * Callback to update the clipping rectangle's position and size.
		 * Also updates the image if another model has been loaded in the 
		 * model viewer.
	     * @method update
	     */
		update: function() {
			var zoomFactor = this.modelviewer.getZoomLevel() / 100;
			
			var pngUrl = this.modelviewer.getModelUri() + "/png";
			var scrollboxEl = this.modelviewer.getScrollboxEl();
				
			// update image source if necessary
			if(this._image.get("src")!=pngUrl)
				this._image.set("src", pngUrl);
				
			var navigatorWidth = parseInt(this.getStyle("width"), 10);
			var scale = navigatorWidth / this.modelviewer.getImgWidth();
			
			if(scale>1) {
				navigatorWidth = this.modelviewer.getImgWidth();
				this.setStyle("width", navigatorWidth + "px");
				scale = 1;
			}
			
			var navigatorHeight = Math.round(scale * this.modelviewer.getImgHeight());
			
			// adjust navigator's height
			this.setStyle("height", navigatorHeight + "px");
			
			// calculate position of clipping rect
			var left = Math.round(scale * scrollboxEl.get("scrollLeft") / zoomFactor);
			var top = Math.round(scale * scrollboxEl.get("scrollTop") / zoomFactor);

			// calculate dimensions of clipping rect
			var scrollboxWidth = parseInt(scrollboxEl.getStyle("width"), 10);
			var scrollboxHeight = parseInt(scrollboxEl.getStyle("height"), 10);
			var clippingRectWidth = Math.round(scale * scrollboxWidth / zoomFactor);
			var clippingRectHeight = Math.round(scale * scrollboxHeight / zoomFactor);
			if(left+clippingRectWidth>navigatorWidth) clippingRectWidth = navigatorWidth-left;
			if(top+clippingRectHeight>navigatorHeight) clippingRectHeight = navigatorHeight-top;
			
			// update clipping rect
			this._clippingRect.setStyle("width", clippingRectWidth + "px");
			this._clippingRect.setStyle("height", clippingRectHeight + "px");
			this._clippingRect.setStyle("left", left + "px");
			this._clippingRect.setStyle("top", top + "px");
			
		}
		
	});
	
})();/**
 * Copyright (c) 2009
 * Jan-Felix Schwarz
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


MOVI.namespace("util");

(function() {
	
	var _SELECT_RECT_CLASS_NAME = "movi-select-rect",
		_HIGHLIGHT_RECT_CLASS_NAME = "movi-highlight-rect";
		
	var Marker 	= MOVI.util.Marker,
		Event 	= YAHOO.util.Event; 
	
	/**
     * Enbable shape selection for the specified model viewer
     * @namespace MOVI.util
     * @class MOVI.util.ShapeSelect
     * @constructor
	 * @param {ModelViewer} modelviewer The ModelViewer for that shape selection is enabled
	 * @param {Shape*} shapes (optional) The subset of shapes that are selectable. If not specified 
	 * all shapes are selectable.
	 * @param {Boolean} multiselect (optional) If set to true, multiple shapes can be selected (default is false).
     */
    MOVI.util.ShapeSelect = function(modelviewer, shapes, multiselect) {
	
		if(!modelviewer) {
			throw new Error("No model viewer specified for shape select.", "error", "shapeselect.js");
			return false;
		}
		
		if(!YAHOO.lang.isArray(shapes)) {
			if(shapes==true) multiselect = true;
			shapes = modelviewer.canvas.getNodes(); // only nodes supported atm
		}
		
		this._allowMultiselect = multiselect;
		
		this._selectableShapes = {};
		this._selectedShapes = {};
		this._highlightMarkers = {};
		
		for(var key in shapes) {
			if(!YAHOO.lang.hasOwnProperty(shapes, key)) continue;
			
			var shape = modelviewer.canvas.shapes[key];
			
			if(shape.parentShape!=null) {
				shape.addListener("mouseover", this._onMouseOver, shape, this);
				shape.addListener("mouseout", this._onMouseOut, shape, this);
				shape.addListener("click", this._onClick, shape, this);
				this._selectableShapes[shape.resourceId] = shape;
			}
			
		}
		
		modelviewer.canvas.addListener("click", this.reset, null, this)
		
		this._selectionMarker = new Marker();
		this._selectionMarker.setRectClassName(_SELECT_RECT_CLASS_NAME);
		this._selectionMarker.show();
		this._init();
	};
	
	MOVI.util.ShapeSelect.prototype = {
		
		_selectableShapes: null,
		
		_selectedShapes: null,
		
		_highlightMarkers: null,
		
		_selectionMarker: null,
		
		_selectionChangedCallback: null,
		
		_allowMultiselect: false,
		
		_init: function() {
			// create highlighting markers
			for(var key in this._selectableShapes) {
				if(!YAHOO.lang.hasOwnProperty(this._selectableShapes, key)) continue;
				
				var s = this._selectableShapes[key];
				var marker = new Marker(s);
				marker.setRectClassName(_HIGHLIGHT_RECT_CLASS_NAME);
				marker.hide();
				this._highlightMarkers[s.resourceId] = marker;
			}
		},
		
		_onMouseOver: function(ev, shape) {
			Event.stopPropagation(ev);
		
			if(!this._selectedShapes[shape.resourceId])
				this.highlight(shape);
		},
		
		_onMouseOut: function(ev, shape) {
			Event.stopPropagation(ev);
		
			if(!this._selectedShapes[shape.resourceId])
				this.unhighlight(shape);
		},
		
		_onClick: function(ev, shape) {
			Event.stopPropagation(ev);
			
			if(this._selectedShapes[shape.resourceId]) {
				this.deselect(shape);
			} else {
				if(!this._allowMultiselect)
					this._reset();
				this.select(shape);	
			}
				
		},
		
		_onSelectionChanged: function() {
			if(!this._selectionChangedCallback) return;
			this._selectionChangedCallback.callback.call(
				this._selectionChangedCallback.scope,
				this,
				this._selectionChangedCallback.data
			);
		},
		
		_reset: function() {
			for(var key in this._selectedShapes) {
				if(!YAHOO.lang.hasOwnProperty(this._selectableShapes, key)) continue;
				
				var s = this._selectedShapes[key];
				this._selectionMarker.removeShape(s);
				delete this._selectedShapes[key];
			}
		},
		
		/**
		 * Add the specified shapes to the current selection
		 * @method select
		 * @param {[Shape] | Shape} shapes The shapes to add to the selection
		 */
		select: function(shapes) {
			if(!YAHOO.lang.isArray(shapes)) shapes = [shapes];
			
			for(var key in shapes) {
				if(!YAHOO.lang.hasOwnProperty(shapes, key)) continue;
				
				var s = shapes[key];
				
				this.unhighlight(s);
			
				if(!this._selectableShapes[s.resourceId]) {
					MOVI.log("Specified shape with resource id " + s.resourceId + 
							 " is not selectable.", "warn", "shapeselect.js");
					continue;
				}
		
				this._selectedShapes[s.resourceId] = s;
				this._selectionMarker.addShape(s);
				
			}
			
			this._onSelectionChanged();
		}, 
		
		/**
		 * Remove the specified shapes from the current selection
		 * @method deselect
		 * @param {[Shape] | Shape} shapes The shapes to remove from the selection
		 */
		deselect: function(shapes) {
			if(!YAHOO.lang.isArray(shapes)) shapes = [shapes];
			
			for(var key in shapes) {
				if(!YAHOO.lang.hasOwnProperty(shapes, key)) continue;
				
				var s = shapes[key];
				delete this._selectedShapes[s.resourceId];
				this._selectionMarker.removeShape(s);
			}
			
			this._onSelectionChanged();
		},
		
		/**
		 * Highlight the specified shape by showing the highlighting marker
		 * @method highlight
		 * @param {Shape} shape The shape to be highlighted
		 */
		highlight: function(shape) {
			this._highlightMarkers[shape.resourceId].show();
		},
		
		/**
		 * Unhighlight the specified shape by hiding the highlighting marker
		 * @method unhighlight
		 * @param {Shape} shape The shape to be unhighlighted
		 */
		unhighlight: function(shape) {
			this._highlightMarkers[shape.resourceId].hide();
		},
		
		/**
		 * Reset the current selection
		 * @method reset
		 */
		reset: function() {
			if(this.getSelectedShapes().length==0)
				return;
			this._reset();
			this._onSelectionChanged();
		},
		
		/**
		 * Returns the currently selected shapes
		 * @method getSelectShapes
		 * @returns {[Shape]} An array of selected Shape objects
		 */
		getSelectedShapes: function() {
			var selected = new Array();
			for(var key in this._selectedShapes) {
				if(!YAHOO.lang.hasOwnProperty(this._selectedShapes, key)) continue;
				
				selected.push(this._selectedShapes[key]);
			}
				
			return selected;
		},
		
		/**
		 * Returns the marker of the current selection
		 * @method getSelectionMarker
		 * @returns {Marker} The selection marker
		 */
		getSelectionMarker: function() {
			return this._selectionMarker;
		},
		
		/**
		 * Specify callback to be executed when the selection changes
		 * (shapes are added to or removed from the current selection)
		 * @param {Function} callback The callback method
		 * @param {Object} scope (optional) The execution scope of the callback 
		 * (in none is specified the context of the ShapeSelect object is used)
		 * @param {Any} data (optional) An optional data object to pass to the callback method
		 * @method onSelectionChanged
		 */
		onSelectionChanged: function(callback, scope, data) {
			if(!YAHOO.lang.isFunction(callback)) {
				throw new TypeError("Specified callback is not a function.", "shapeselect.js");
				return;
			}
			if(!scope) scope = this;
			this._selectionChangedCallback = {
				callback: callback,
				scope: scope,
				data: data
			};
		}
		
	}
	
	
})();/**
 * Copyright (c) 2009
 * Jan-Felix Schwarz
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


// Declare the MOVI.widget namespace
MOVI.namespace("widget");

(function() {
	
	var _TOOLBAR_CLASS_NAME 			= "movi-toolbar",
		_TOOLBAR_BUTTON_CLASS_NAME		= "movi-toolbar-button",
		_TOOLBAR_GROUP_CLASS_NAME		= "movi-toolbar-group";
		_TOOLBAR_GROUP_LABEL_CLASS_NAME	= "movi-toolbar-group-label"
	
	var _DEFAULT_GROUP_NAME 			= "movi-toolbar-default-group";
	
	/**
     * The Toolbar widget is a UI control that hosts buttons to trigger certain
	 * functionality. It allows generic registration of new toolbar buttons.
     * @namespace MOVI.widget
     * @class MOVI.widget.Toolbar
     * @extends YAHOO.util.Element
     * @constructor
     * @param {HTMLElement | String } el The id of the container DIV element that will 
	 * wrap the Toolbar, or a reference to a DIV element. The DIV element must
	 * exist in the document.
	 * @modelviewer {ModelViewer} modelviewer The ModelViewer that is controlled 
     */
    MOVI.widget.Toolbar = function(el, modelviewer) {
	
		this.modelviewer = modelviewer;
		
		this._groups = {};
		
		MOVI.widget.Toolbar.superclass.constructor.call(this, el); 
		
		this.addClass(_TOOLBAR_CLASS_NAME);
		
	};
	
	MOVI.extend(MOVI.widget.Toolbar, YAHOO.util.Element, {
		
		/**
		 * A key map containing all group objects with their name as keys
		 * @property groups
		 * @type {Object}
		 * @private
		 */
		_groups: null,
		
		/**
		 * The ModelViewer that is controlled
		 * @property modelviewer
		 * @type ModelViewer
		 */
		modelviewer: null,
		
		/**
		 * Update the toolbar DOM node 
		 * @method _update
		 * @private
		 */
		_update: function() {
			
			this.set("innerHTML", "");
			
			for(var key in this._groups) {
				if(!YAHOO.lang.hasOwnProperty(this._groups, key)) continue;
				
				var group = this._groups[key];
				
				var groupEl = new YAHOO.util.Element(document.createElement("div"));
				groupEl.set("className", _TOOLBAR_GROUP_CLASS_NAME);
				this.appendChild(groupEl);
				
				if(this._showGroupCaptions) {
					var groupLabelEl = new YAHOO.util.Element(document.createElement("div"));
					groupLabelEl.set("className", _TOOLBAR_GROUP_LABEL_CLASS_NAME);
					groupEl.appendChild(groupLabelEl);
					if(key!=_DEFAULT_GROUP_NAME) groupLabelEl.set("innerHTML", key);
				}
				
				for(var i=0; i<group.length; i++) {
					if(group[i]) groupEl.appendChild(group[i]);
				}
			}
			
			
			var clearDiv = new YAHOO.util.Element(document.createElement("div"));
			clearDiv.setStyle("clear", "left");
			this.appendChild(clearDiv);
			
		},
		
		/**
		 * Add a new button to the toolbar
		 * @param {Object} config Button configuration: 
		 * <dl>
		 * <dt>{String} icon (optional)</dt>
		 * <dd>
		 * The URL of an icon image
		 * </dd>
		 * <dt>{String} caption (optional)</dt>
		 * <dd>
		 * The caption of the button
		 * </dd>
		 * <dt>{String} tooltip (optional)</dt>
		 * <dd>
		 * A tooltip message for the button
		 * </dd>
		 * <dt>{String} group (optional)</dt>
		 * <dd>
		 * The button group to append the new button to. If no such group exists
		 * it will be created. Groups are positioned in the order they were created.
		 * </dd>
		 * <dt>{Function} callback</dt>
		 * <dd>The callback method when the button is triggered.</dd>
		 * <dt>{Object} scope (optional)</dt>
		 * <dd>The execution scope for the callback.</dd>
		 * <dt>{Any} data (optional)</dt>
		 * <dd>
		 * An optional data object to pass to the callback method
		 * </dd>
		 * </dl>
		 * @method addButton
		 * @return {String} An index to reference the button in the toolbar
		 */
		addButton: function(config) {
			
			if(!YAHOO.lang.isFunction(config.callback)) {
				throw new Error("No valid callback method specified", "toolbar.js");
				return null;
			}
			
			// create the button
			var button = new YAHOO.util.Element(document.createElement("div"));
			button.set("className", _TOOLBAR_BUTTON_CLASS_NAME);
			
			var scope = config.scope ? config.scope : this;
			button.on("click", config.callback, config.data, scope);
			
			if(config.icon) {
				if(!YAHOO.lang.isString(config.icon)) {
					throw new TypeError("The icon has to be a String value", "toolbar.js");
					return null;
				}
				var iconEl = new YAHOO.util.Element(document.createElement("img"));
				iconEl.set("src", config.icon);
				button.appendChild(iconEl);
			}
			if(config.caption) {
				if(!YAHOO.lang.isString(config.caption)) {
					throw new TypeError("The caption has to be a String value", "toolbar.js");
					return null;
				}
				var captionEl = new YAHOO.util.Element(document.createElement("span"));
				captionEl.set("html", config.caption);
			}
			if(!config.icon && !config.caption) {
				throw new Error("At least one - icon or caption - must be specified", "toolbar.js");
				return false;
			}
			
			if(config.tooltip) {
				if(!YAHOO.lang.isString(config.tooltip)) {
					throw new TypeError("The tooltip has to be a String value", "toolbar.js");
					return null;
				}
				button.set("title", config.tooltip);
				if(iconEl) iconEl.set("alt", config.tooltip);
			}
			
			if(config.group) {
				if(!YAHOO.lang.isString(config.group)) {
					throw new TypeError("The group has to be a String value", "toolbar.js");
					return null;
				}
				
				var groupName = config.group;
				var buttonId = groupName;
			} else {
				var groupName = _DEFAULT_GROUP_NAME;
				var buttonId = "";
			}
			
			// if group doesn't exist create it
			if(!this._groups[groupName])
				this._groups[groupName] = new Array();
			
			this._groups[groupName].push(button);
			buttonId += "_" + this._groups[groupName].length-1;
			
			this._update();
			
			return buttonId;
		},
		
		/**
		 * Remove the button with the specified id from the toolbar
		 * @param {String} orientation The orientation of the icon to remove.
		 * @method addButton
		 * @return {Integer} A unique id to reference the button in the toolbar
		 */
		removeButton: function(id) {
			
			var i = id.lastIndexOf("_");
			var groupName = id.substring(0, i-1);
			var index = id.substring(i+1);
			
			this._groups[groupName][index] = undefined;
			this._update();
		},
		
		/**
		 * Show captions for the button groups. (Per default captions are not shown)
		 * @method showGroupCaptions
		 */
		showGroupCaptions: function() {
			this._showGroupCaptions = true;
			this._update();
		},
		
		/**
		 * Hide captions for the button groups. (Per default captions are not shown)
		 * @method hideGroupCaptions
		 */
		hideGroupCaptions: function() {
			this._showGroupCaptions = false;
			this._update();
		}
	
	});
	
	
})();/**
 * Copyright (c) 2009
 * Jan-Felix Schwarz
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


MOVI.namespace("widget");

(function() {
	
	var _SLIDER_CLASS_NAME_PREFIX			= "movi-zoomslider",
		_SLIDER_THUMB_CLASS_NAME			= "movi-zoomslider-thumb",
		_SLIDER_DEFAULT_LENGTH				= 100,
		_SLIDER_DEFAULT_STEP				= 1,
		_FULLSCREEN_LIGHTBOX_CLASS_NAME		= "movi-fullscreen-lightbox",
		_FULLSCREEN_CONTAINER_CLASS_NAME	= "movi-fullscreen-modelcontainer";
	
	/**
     * Add image to the host element and create the clipping rectangle element.
	 * Ensure that host element's position is not static to allow absolute
	 * positioning of the clipping rect relative to the host element
     * @method _setUpHostElementZoomSlider
     * @private
     */
	var _setUpHostElementZoomSlider = function() {
		
		var div1 = new YAHOO.util.Element(document.createElement("div"));
		div1.set("id", _SLIDER_CLASS_NAME_PREFIX + this.modelviewer.getIndex());
		div1.addClass(_SLIDER_CLASS_NAME_PREFIX  + "-" + this.orientation);
		div1.set("title", "Zoom Slider");
		
		var div2 = new YAHOO.util.Element(document.createElement("div"));
		div2.set("id", _SLIDER_THUMB_CLASS_NAME + this.modelviewer.getIndex());
		div2.addClass(_SLIDER_THUMB_CLASS_NAME);
		
		div1.appendChild(div2);
		this.appendChild(div1);
		
	};
	
	/**
	 * Calculate the minimal zoom level in percent (stop zooming out when model fits the model viewer in height and width)
	 * @method _getMinZoomLevel
	 * @private
	 */

	var _getMinZoomLevel = function(modelviewer) {		
		var scaleHorizontal = (modelviewer.getScrollboxEl().get("offsetWidth")-5) / modelviewer.getImgWidth();
		var scaleVertical = (modelviewer.getScrollboxEl().get("offsetHeight")-5) / modelviewer.getImgHeight();
		var scale = (scaleHorizontal < scaleVertical) ? scaleHorizontal : scaleVertical;
		if(scale>1)	scale = 1;
		return scale*100;
	};
	
	/**
     * The ZoomSlider widget is a slider UI control that enables users to adjust the
 	 * zoom level of a model viewer.
     * @namespace MOVI.widget
     * @class MOVI.widget.ZoomSlider
     * @constructor
	 * @param {HTMLElement | String } el The id of the container DIV element that will 
	 * wrap the ZoomSlider, or a reference to a DIV element. The DIV element must
	 * exist in the document.
	 * @param {ModelViewer} modelviewer The ModelViewer for that zooming is enabled
	 * @param {Object} opt (optional) Options: 
     * <dl>
     * <dt>orientation</dt>
     * <dd>
     * expected values: "horizontal" (default), "vertical"
     * </dd>
     * <dt>trackLength</dt>
     * <dd>
     * The length of the slider track in pixels (default: 100)
     * </dd>
     * <dt>step</dt>
     * <dd>
     * The slider thumb should move this many pixels at a time (default: 1)
     * </dd>
     * <dt>reverse</dt>
     * <dd>
     * Set to true to invert to default direction of the slider which is left-to right, or
     * alternatively top-to-bottom (default: false)
     * </dd>
	 * </dl>
     */
    MOVI.widget.ZoomSlider = function(el, modelviewer, opt) {
	
		if(!modelviewer) {
			throw new Error("No model viewer specified for zoom slider", "zoom.js");
			return false;
		}
	
		this.modelviewer = modelviewer;
		
    	MOVI.widget.ZoomSlider.superclass.constructor.call(this, el);
    	
    	if(!opt) opt = {};
		
		this.orientation = opt.orientation || "horizontal";
		this.trackLength = opt.trackLength || _SLIDER_DEFAULT_LENGTH;
		this.step = opt.step || _SLIDER_DEFAULT_STEP;
		this.reverse = opt.reverse || false;
	
		_setUpHostElementZoomSlider.call(this);
		
		// instantiate the slider
		if(this.orientation=="vertical") {
		    this.slider = YAHOO.widget.Slider.getVertSlider(
    			_SLIDER_CLASS_NAME_PREFIX + this.modelviewer.getIndex(), 
    			_SLIDER_THUMB_CLASS_NAME + this.modelviewer.getIndex(), 
    			0, this.trackLength, this.step
    		);
		} else {
		    this.slider = YAHOO.widget.Slider.getHorizSlider(
    			_SLIDER_CLASS_NAME_PREFIX + this.modelviewer.getIndex(), 
    			_SLIDER_THUMB_CLASS_NAME + this.modelviewer.getIndex(), 
    			0, this.trackLength, this.step
    		);
		}
		
		this.update();
		
		this.slider.subscribe('change', this.onChange, this, true);
		this.slider.subscribe('slideStart', this._onSlideStart, this, true);
		this.slider.subscribe('slideEnd', this._onSlideEnd, this, true);
		
		this.modelviewer.onModelLoadEnd.subscribe(this.update, this, true);
	};
	
	MOVI.extend(MOVI.widget.ZoomSlider, YAHOO.util.Element, {
		
		/**
		 * The slider UI element
		 * @property slider
		 * @type YAHOO.widget.Slider
		 */
		slider: null,
		
		/**
		 * Callback that is executed when sliding starts
		 * @method _onSlideStart
		 * @private
		 */
		_onSlideStart: function() {
			this.modelviewer.onZoomLevelChangeStart.fire(this.modelviewer.getZoomLevel());
		},
		
		/**
		 * Callback that is executed when sliding ends
		 * @method _onSlideEnd
		 * @private
		 */
		_onSlideEnd: function() {
			this.modelviewer.onZoomLevelChangeEnd.fire(this.modelviewer.getZoomLevel());
		},
		
		/**
		 * Callback method that is executed when the slider is changed. Updates the zoom level of the model viewer.
		 * This method should also be called when the model viewer is resized.
		 * @method onChange
		 */
		onChange: function() {
			var minZoomLevel = _getMinZoomLevel(this.modelviewer);
			var maxZoomLevel = 100;
			var zoomStep = (maxZoomLevel-minZoomLevel) / this.trackLength;
			if(this.reverse)
			    this.modelviewer.setZoomLevel(minZoomLevel + (this.trackLength - this.slider.getValue()) * zoomStep, false);
			else
			    this.modelviewer.setZoomLevel(minZoomLevel + this.slider.getValue() * zoomStep, false);
		},
		
		/**
		 * Update the state of the slider control to current model zoom level.
		 * This method should be called when the model viewer's zoom level has been changed.
		 * @method update
		 */
		update: function() {
			var minZoomLevel = _getMinZoomLevel(this.modelviewer);
			var maxZoomLevel = 100;
			var zoomStep = (maxZoomLevel-minZoomLevel) / this.trackLength;
			if(this.reverse) 
			    this.slider.setValue(this.trackLength - (this.modelviewer.getZoomLevel() - minZoomLevel) / zoomStep, false, true, true);
			else
			    this.slider.setValue((this.modelviewer.getZoomLevel() - minZoomLevel) / zoomStep, false, true, true);
		}
		
	});
	
	
	
	/**
	 * Swap two nodes. In IE, we use the native method, for others we
	 * emulate the IE behavior
	 * @method _swapNode
	 * @param n1 the first node to swap
	 * @param n2 the other node to swap
	 * @private
	 */
	var _swapNode = function(n1, n2) {
	    if (n1.swapNode) {
	        n1.swapNode(n2);
	    } else {
	        var p = n2.parentNode;
	        var s = n2.nextSibling;

	        if (s == n1) {
	            p.insertBefore(n1, n2);
	        } else if (n2 == n1.nextSibling) {
	            p.insertBefore(n2, n1);
	        } else {
	            n1.parentNode.replaceChild(n2, n1);
	            p.insertBefore(n1, s);
	        }
	    }
	};
	
	/**
     * 
     * @method _setUpHostElementFullscreenViewer
     * @private
     */
	var _setUpHostElementFullscreenViewer = function() {
		var el = new YAHOO.util.Element(document.createElement('div'));
		el.set("innerHTML", 
			"<div class=\"hd\"></div>" +
		    "<div class=\"bd\"><div class=\"" + _FULLSCREEN_CONTAINER_CLASS_NAME + "\"></div></div>" +
		    "<div class=\"ft\"></div>");
		(new YAHOO.util.Element(document.body)).appendChild(el);
		return el;
	};
	
	/**
     * The FullscreenViewer widget is a UI widget that displays the complete model in a
	 * lightbox fullscreen view.
     * @namespace MOVI.widget
     * @class MOVI.widget.FullscreenViewer
     * @constructor
	 * @param {ModelViewer} modelviewer The ModelViewer for that is viewed in fullscreen mode
     */
    MOVI.widget.FullscreenViewer = function(modelviewer) {
	
		this.modelviewer = modelviewer;
		
		var el = _setUpHostElementFullscreenViewer();
		el.addClass(_FULLSCREEN_LIGHTBOX_CLASS_NAME);
		var elId = _FULLSCREEN_LIGHTBOX_CLASS_NAME+this.modelviewer.getIndex();
		el.set("id", elId);
		
		MOVI.widget.FullscreenViewer.superclass.constructor.call(this, el);
		
		this.dialog = new YAHOO.widget.Dialog(elId, {
			fixedcenter: true,
			visible: false,
			draggable: true,
			modal: true,
			close: true,
			constraintoviewport: true
		});
		
		this.dialog.render();
		
		// add listener to close button
		YAHOO.util.Event.on(
			YAHOO.util.Dom.getElementsByClassName("container-close", "a", elId), 
			"click",
			this.close, this, true
		);
		
		this._fullscreenContainer = new YAHOO.util.Element(el.getElementsByClassName(_FULLSCREEN_CONTAINER_CLASS_NAME)[0]);
		this._modelViewerPlaceholder = new YAHOO.util.Element(document.createElement("div"));
		this._fullscreenContainer.appendChild(this._modelViewerPlaceholder);
	};
	
	MOVI.extend(MOVI.widget.FullscreenViewer, YAHOO.util.Element, {
		
		/**
		 * The fullscreen model viewer conatainer element
		 * @property _fullscreenContainer
		 * @type YAHOO.util.Element
		 * @private
		 */
		_fullscreenContainer: null,
		
		/**
		 * A placeholder element that is swapped with the original model viewer element
		 * @property _modelViewerPlaceholder
		 * @type YAHOO.util.Element
		 * @private
		 */
		_modelViewerPlaceholder: null,
		
		/**
		 * The Dialog widget that represents the lightbox
		 * @property dialog
		 * @type YAHOO.widget.Dialog
		 */
		dialog: null,
		
		open: function() {
			this.dialog.cfg.setProperty("width", (YAHOO.util.Dom.getViewportWidth()-20)+"px");
			this.dialog.cfg.setProperty("height", (YAHOO.util.Dom.getViewportHeight()-20)+"px");
			_swapNode(this.modelviewer.getScrollboxEl().get("element"), this._modelViewerPlaceholder.get("element"));
			
			// zoom to fit to fullscreen
			var minZoomLevel = _getMinZoomLevel(this.modelviewer);
		
			// store original zoom factor
			this._originalZoomLevel = this.modelviewer.getZoomLevel();
			this.modelviewer.setZoomLevel(minZoomLevel);
			
			this.dialog.show();
		},
		
		close: function() {
			_swapNode(this.modelviewer.getScrollboxEl().get("element"), this._modelViewerPlaceholder.get("element"));
			this.modelviewer.setZoomLevel(this._originalZoomLevel);
			this.dialog.hide();
		}
		
	});
	
	
})();