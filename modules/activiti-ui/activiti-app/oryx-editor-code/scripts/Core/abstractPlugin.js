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

if(!ORYX){ var ORYX = {} }
if(!ORYX.Plugins){ ORYX.Plugins = {} }

/**
   This abstract plugin class can be used to build plugins on.
   It provides some more basic functionality like registering events (on*-handlers)...
   @example
    ORYX.Plugins.MyPlugin = ORYX.Plugins.AbstractPlugin.extend({
        construct: function() {
            // Call super class constructor
            arguments.callee.$.construct.apply(this, arguments);
            
            [...]
        },
        [...]
    });
   
   @class ORYX.Plugins.AbstractPlugin
   @constructor Creates a new instance
   @author Willi Tscheschner
*/
ORYX.Plugins.AbstractPlugin = Clazz.extend({
    /** 
     * The facade which offer editor-specific functionality
     * @type Facade
     * @memberOf ORYX.Plugins.AbstractPlugin.prototype
     */
	facade: null,
	
	construct: function( facade ){
		this.facade = facade;
		
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_LOADED, this.onLoaded.bind(this));
	},
        
    /**
       Overwrite to handle load event. TODO: Document params!!!
       @methodOf ORYX.Plugins.AbstractPlugin.prototype
    */
	onLoaded: function(){},
	
    /**
       Overwrite to handle selection changed event. TODO: Document params!!!
       @methodOf ORYX.Plugins.AbstractPlugin.prototype
    */
	onSelectionChanged: function(){},
	
    /**
       Show overlay on given shape.
       @methodOf ORYX.Plugins.AbstractPlugin.prototype
       @example
       showOverlay(
           myShape,
           { stroke: "green" },
           ORYX.Editor.graft("http://www.w3.org/2000/svg", null, ['path', {
               "title": "Click the element to execute it!",
               "stroke-width": 2.0,
               "stroke": "black",
               "d": "M0,-5 L5,0 L0,5 Z",
               "line-captions": "round"
           }])
       )
       @param {Oryx.XXX.Shape[]} shapes One shape or array of shapes the overlay should be put on
       @param {Oryx.XXX.Attributes} attributes some attributes...
       @param {Oryx.svg.node} svgNode The svg node which should be used as overlay
       @param {String} [svgNode="NW"] The svg node position where the overlay should be placed
    */
	showOverlay: function(shapes, attributes, svgNode, svgNodePosition ){
		
		if( !(shapes instanceof Array) ){
			shapes = [shapes]
		}
		
		// Define Shapes
		shapes = shapes.map(function(shape){
			var el = shape;
			if( typeof shape == "string" ){
				el = this.facade.getCanvas().getChildShapeByResourceId( shape );
				el = el || this.facade.getCanvas().getChildById( shape, true );
			}
			return el;
		}.bind(this)).compact();
		
		// Define unified id
		if( !this.overlayID ){
			this.overlayID = this.type + ORYX.Editor.provideId();
		}
		
		this.facade.raiseEvent({
			type		: ORYX.CONFIG.EVENT_OVERLAY_SHOW,
			id			: this.overlayID,
			shapes		: shapes,
			attributes 	: attributes,
			node		: svgNode,
			nodePosition: svgNodePosition || "NW"
		});
		
	},
	
    /**
       Hide current overlay.
       @methodOf ORYX.Plugins.AbstractPlugin.prototype
    */
	hideOverlay: function(){
		this.facade.raiseEvent({
			type	: ORYX.CONFIG.EVENT_OVERLAY_HIDE,
			id		: this.overlayID
		});		
	},
	
    /**
       Does a transformation with the given xslt stylesheet.
       @methodOf ORYX.Plugins.AbstractPlugin.prototype
       @param {String} data The data (e.g. eRDF) which should be transformed
       @param {String} stylesheet URL of a stylesheet which should be used for transforming data.
    */
	doTransform: function( data, stylesheet ) {		
		
		if( !stylesheet || !data ){
			return ""
		}

        var parser 		= new DOMParser();
        var parsedData 	= parser.parseFromString(data, "text/xml");
		source=stylesheet;
		new Ajax.Request(source, {
			asynchronous: false,
			method: 'get',
			onSuccess: function(transport){
				xsl = transport.responseText
			}.bind(this),
			onFailure: (function(transport){
				ORYX.Log.error("XSL load failed" + transport);
			}).bind(this)
		});
        var xsltProcessor = new XSLTProcessor();
		var domParser = new DOMParser();
		var xslObject = domParser.parseFromString(xsl, "text/xml");
        xsltProcessor.importStylesheet(xslObject);
        
        try {
        	
            var newData 		= xsltProcessor.transformToFragment(parsedData, document);
            var serializedData 	= (new XMLSerializer()).serializeToString(newData);
            
           	/* Firefox 2 to 3 problem?! */
            serializedData = !serializedData.startsWith("<?xml") ? "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + serializedData : serializedData;
            
            return serializedData;
            
        }catch (error) {
            return -1;
        }
        
	},
	
	/**
	 * Opens a new window that shows the given XML content.
	 * @methodOf ORYX.Plugins.AbstractPlugin.prototype
	 * @param {Object} content The XML content to be shown.
	 * @example
	 * openDownloadWindow( "my.xml", "<exampleXML />" );
	 */
	openXMLWindow: function(content) {
		var win = window.open(
		   'data:application/xml,' + encodeURIComponent(
		     content
		   ),
		   '_blank', "resizable=yes,width=600,height=600,toolbar=0,scrollbars=yes"
		);
	},
	
    /**
     * Opens a download window for downloading the given content.
     * @methodOf ORYX.Plugins.AbstractPlugin.prototype
     * @param {String} filename The content's file name
     * @param {String} content The content to download
     */
	openDownloadWindow: function(filename, content) {
		var win = window.open("");
		if (win != null) {
			win.document.open();
			win.document.write("<html><body>");
			var submitForm = win.document.createElement("form");
			win.document.body.appendChild(submitForm);
			
			var createHiddenElement = function(name, value) {
				var newElement = document.createElement("input");
				newElement.name=name;
				newElement.type="hidden";
				newElement.value = value;
				return newElement
			}
			
			submitForm.appendChild( createHiddenElement("download", content) );
			submitForm.appendChild( createHiddenElement("file", filename) );
			
			
			submitForm.method = "POST";
			win.document.write("</body></html>");
			win.document.close();
			submitForm.action= ORYX.PATH + "/download";
			submitForm.submit();
		}		
	},
    
    /**
     * Serializes DOM.
     * @methodOf ORYX.Plugins.AbstractPlugin.prototype
     * @type {String} Serialized DOM
     */
    getSerializedDOM: function(){
        // Force to set all resource IDs
        var serializedDOM = DataManager.serializeDOM( this.facade );

        //add namespaces
        serializedDOM = '<?xml version="1.0" encoding="utf-8"?>' +
        '<html xmlns="http://www.w3.org/1999/xhtml" ' +
        'xmlns:b3mn="http://b3mn.org/2007/b3mn" ' +
        'xmlns:ext="http://b3mn.org/2007/ext" ' +
        'xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" ' +
        'xmlns:atom="http://b3mn.org/2007/atom+xhtml">' +
        '<head profile="http://purl.org/NET/erdf/profile">' +
        '<link rel="schema.dc" href="http://purl.org/dc/elements/1.1/" />' +
        '<link rel="schema.dcTerms" href="http://purl.org/dc/terms/ " />' +
        '<link rel="schema.b3mn" href="http://b3mn.org" />' +
        '<link rel="schema.oryx" href="http://oryx-editor.org/" />' +
        '<link rel="schema.raziel" href="http://raziel.org/" />' +
        '<base href="' +
        location.href.split("?")[0] +
        '" />' +
        '</head><body>' +
        serializedDOM +
        '</body></html>';
        
        return serializedDOM;
    },
    
    /**
     * Sets the editor in read only mode: Edges/ dockers cannot be moved anymore,
     * shapes cannot be selected anymore.
     * @methodOf ORYX.Plugins.AbstractPlugin.prototype
     */
    enableReadOnlyMode: function(){
        //Edges cannot be moved anymore
        this.facade.disableEvent(ORYX.CONFIG.EVENT_MOUSEDOWN);
        
        // Stop the user from editing the diagram while the plugin is active
        this._stopSelectionChange = function(){
            if(this.facade.getSelection().length > 0) {
                this.facade.setSelection([]);
            }
        };
        this.facade.registerOnEvent(ORYX.CONFIG.EVENT_SELECTION_CHANGED, this._stopSelectionChange.bind(this));
    },
    /**
     * Disables read only mode, see @see
     * @methodOf ORYX.Plugins.AbstractPlugin.prototype
     * @see ORYX.Plugins.AbstractPlugin.prototype.enableReadOnlyMode
     */
    disableReadOnlyMode: function(){
        // Edges can be moved now again
        this.facade.enableEvent(ORYX.CONFIG.EVENT_MOUSEDOWN);
        
        if (this._stopSelectionChange) {
            this.facade.unregisterOnEvent(ORYX.CONFIG.EVENT_SELECTION_CHANGED, this._stopSelectionChange.bind(this));
            this._stopSelectionChange = undefined;
        }
    },
    
    /**
     * Extracts RDF from DOM.
     * @methodOf ORYX.Plugins.AbstractPlugin.prototype
     * @type {String} Extracted RFD. Null if there are transformation errors.
     */
    getRDFFromDOM: function(){
        //convert to RDF
		try {
			var xsl = "";
			source=ORYX.PATH + "lib/extract-rdf.xsl";
			new Ajax.Request(source, {
				asynchronous: false,
				method: 'get',
				onSuccess: function(transport){
					xsl = transport.responseText
				}.bind(this),
				onFailure: (function(transport){
					ORYX.Log.error("XSL load failed" + transport);
				}).bind(this)
			});
			
			var domParser = new DOMParser();
			var xmlObject = domParser.parseFromString(this.getSerializedDOM(), "text/xml");
			var xslObject = domParser.parseFromString(xsl, "text/xml");
			var xsltProcessor = new XSLTProcessor();
			xsltProcessor.importStylesheet(xslObject);
			var result = xsltProcessor.transformToFragment(xmlObject, document);
			
			var serializer = new XMLSerializer();
			
			return serializer.serializeToString(result);
		} catch(e){
			console.log("error serializing " + e);
			return "";
		}

		
    },
    
    /**
	 * Checks if a certain stencil set is loaded right now.
	 * 
	 */
	isStencilSetExtensionLoaded: function(stencilSetExtensionNamespace) {
		return this.facade.getStencilSets().values().any(
			function(ss){ 
				return ss.extensions().keys().any(
					function(extensionKey) {
						return extensionKey == stencilSetExtensionNamespace;
					}.bind(this)
				);
			}.bind(this)
		);
	},
	
	/**
	 * Raises an event so that registered layouters does
	 * have the posiblility to layout the given shapes 
	 * For further reading, have a look into the AbstractLayouter
	 * class
	 * @param {Object} shapes
	 */
	doLayout: function(shapes){
		// Raises a do layout event
		if (this.facade.raiseEvent)
		{
			this.facade.raiseEvent({
				type		: ORYX.CONFIG.EVENT_LAYOUT,
				shapes		: shapes
			});
		}
		else
		{
			this.facade.handleEvents({
				type		: ORYX.CONFIG.EVENT_LAYOUT,
				shapes		: shapes
			});
		}
	},
	
	
	/**
	 * Does a primitive layouting with the incoming/outgoing 
	 * edges (set the dockers to the right position) and if 
	 * necessary, it will be called the real layouting 
	 * @param {ORYX.Core.Node} node
	 * @param {Array} edges
	 */
	layoutEdges : function(node, allEdges, offset){		

		if (!this.facade.isExecutingCommands()){ return }		

		var Command = ORYX.Core.Command.extend({
			construct: function(edges, node, offset, plugin){
				this.edges = edges;
				this.node = node;
				this.plugin = plugin;
				this.offset = offset;
				
				// Get the new absolute center
				var center = node.absoluteXY();
				this.ulo = {x: center.x - offset.x, y:center.y - offset.y};
				
				
			},
			execute: function(){
				
				if (this.changes){
					this.executeAgain();
					return;
				} else {
					this.changes = [];
					this.edges.each(function(edge){
						this.changes.push({
							edge: edge,
							oldDockerPositions: edge.dockers.map(function(r){ return r.bounds.center() })
						})
					}.bind(this));
				}
				
				// Find all edges, which are related to the node and
				// have more than two dockers
				this.edges
					// Find all edges with more than two dockers
					.findAll(function(r){ return r.dockers.length > 2 }.bind(this))
					// For every edge, check second and one before last docker
					// if there are horizontal/vertical on the same level
					// and if so, align the the bounds 
					.each(function(edge){
						if (edge.dockers.first().getDockedShape() === this.node){
							var second = edge.dockers[1];
							if (this.align(second.bounds, edge.dockers.first())){ second.update(); }
						} else if (edge.dockers.last().getDockedShape() === this.node) {
							var beforeLast = edge.dockers[edge.dockers.length-2];
							if (this.align(beforeLast.bounds, edge.dockers.last())){ beforeLast.update(); }									
						}
						edge._update(true);
						edge.removeUnusedDockers();
						if (this.isBendPointIncluded(edge)){
							this.plugin.doLayout(edge);
							return;
						}
					}.bind(this));
				
				
				// Find all edges, which have only to dockers 
				// and is located horizontal/vertical.
				// Do layout with those edges
				this.edges
					// Find all edges with exactly two dockers
					.each(function(edge){
						if (edge.dockers.length == 2){
							var p1 = edge.dockers.first().getAbsoluteReferencePoint() || edge.dockers.first().bounds.center();
							var p2 = edge.dockers.last().getAbsoluteReferencePoint() || edge.dockers.first().bounds.center();
							// Find all horizontal/vertical edges
							if (Math.abs(-Math.abs(p1.x - p2.x) + Math.abs(this.offset.x)) < 2 || Math.abs(-Math.abs(p1.y - p2.y) + Math.abs(this.offset.y)) < 2){
								this.plugin.doLayout(edge);
							}
						}
					}.bind(this));
		
				this.edges.each(function(edge, i){
					this.changes[i].dockerPositions = edge.dockers.map(function(r){ return r.bounds.center() });
				}.bind(this));
				
			},
			/**
			 * Align the bounds if the center is 
			 * the same than the old center
			 * @params {Object} bounds
			 * @params {Object} bounds2
			 */
			align: function(bounds, refDocker){
				
				var abRef = refDocker.getAbsoluteReferencePoint() || refDocker.bounds.center();
				
				var xdif = bounds.center().x-abRef.x;
				var ydif = bounds.center().y-abRef.y;
				if (Math.abs(-Math.abs(xdif) + Math.abs(this.offset.x)) < 3 && this.offset.xs === undefined){
					bounds.moveBy({x:-xdif, y:0})
				}
				if (Math.abs(-Math.abs(ydif) + Math.abs(this.offset.y)) < 3 && this.offset.ys === undefined){
					bounds.moveBy({y:-ydif, x:0})
				}
				
				if (this.offset.xs !== undefined || this.offset.ys !== undefined){
					var absPXY = refDocker.getDockedShape().absoluteXY();
					xdif = bounds.center().x-(absPXY.x+((abRef.x-absPXY.x)/this.offset.xs));
					ydif = bounds.center().y-(absPXY.y+((abRef.y-absPXY.y)/this.offset.ys));
					
					if (Math.abs(-Math.abs(xdif) + Math.abs(this.offset.x)) < 3){
						bounds.moveBy({x:-(bounds.center().x-abRef.x), y:0})
					}
					
					if (Math.abs(-Math.abs(ydif) + Math.abs(this.offset.y)) < 3){
						bounds.moveBy({y:-(bounds.center().y-abRef.y), x:0})
					}
				}
			},
			
			/**						
			 * Returns a TRUE if there are bend point which overlay the shape
			 */
			isBendPointIncluded: function(edge){
				// Get absolute bounds
				var ab = edge.dockers.first().getDockedShape();
				var bb = edge.dockers.last().getDockedShape();
				
				if (ab) {
					ab = ab.absoluteBounds();
					ab.widen(5);
				}
				
				if (bb) {
					bb = bb.absoluteBounds();
					bb.widen(20); // Wide with 20 because of the arrow from the edge
				}
				
				return edge.dockers
						.any(function(docker, i){ 
							var c = docker.bounds.center();
									// Dont count first and last
							return 	i != 0 && i != edge.dockers.length-1 && 
									// Check if the point is included to the absolute bounds
									((ab && ab.isIncluded(c)) || (bb && bb.isIncluded(c)))
						})
			},
			
			removeAllDocker: function(edge){
				edge.dockers.slice(1, edge.dockers.length-1).each(function(docker){
					edge.removeDocker(docker);
				})
			},
			executeAgain: function(){
				this.changes.each(function(change){
					// Reset the dockers
					this.removeAllDocker(change.edge);
					change.dockerPositions.each(function(pos, i){	
						if (i==0||i==change.dockerPositions.length-1){ return }					
						var docker = change.edge.createDocker(undefined, pos);
						docker.bounds.centerMoveTo(pos);
						docker.update();
					}.bind(this));
					change.edge._update(true);
				}.bind(this));
			},
			rollback: function(){					
				this.changes.each(function(change){
					// Reset the dockers
					this.removeAllDocker(change.edge);
					change.oldDockerPositions.each(function(pos, i){	
						if (i==0||i==change.oldDockerPositions.length-1){ return }					
						var docker = change.edge.createDocker(undefined, pos);
						docker.bounds.centerMoveTo(pos);
						docker.update();
					}.bind(this));
					change.edge._update(true);
				}.bind(this));
			}
		});
		
		this.facade.executeCommands([new Command(allEdges, node, offset, this)]);

	}
});