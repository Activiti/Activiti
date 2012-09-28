/**
 * Copyright (c) 2008, Gero Decker, refactored by Kai Schlichting
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

/**
   This plugin is a generic syntax checker for different diagram types.
   Needs server communication.
   @class ORYX.Plugins.SyntaxChecker
   @constructor Creates a new instance
   @extends ORYX.Plugins.AbstractPlugin
*/
ORYX.Plugins.SyntaxChecker = ORYX.Plugins.AbstractPlugin.extend({
    /**@private*/
    construct: function(){
        arguments.callee.$.construct.apply(this, arguments);
                
        this.active = false;
        this.raisedEventIds = [];
        
        this.facade.offer({
            'name': ORYX.I18N.SyntaxChecker.name,
            'functionality': this.perform.bind(this),
            'group': ORYX.I18N.SyntaxChecker.group,
            'icon': ORYX.PATH + "images/checker_syntax.png",
            'description': ORYX.I18N.SyntaxChecker.desc,
            'index': 0,
            'toggle': true,
            'minShape': 0,
            'maxShape': 0
        });
        
        this.facade.registerOnEvent(ORYX.Plugins.SyntaxChecker.CHECK_FOR_ERRORS_EVENT, this.checkForErrors.bind(this));
        this.facade.registerOnEvent(ORYX.Plugins.SyntaxChecker.RESET_ERRORS_EVENT, this.resetErrors.bind(this));
        this.facade.registerOnEvent(ORYX.Plugins.SyntaxChecker.SHOW_ERRORS_EVENT, this.doShowErrors.bind(this));
    },
    
    perform: function(button, pressed){
        if (!pressed) {
            this.resetErrors();
        } else {
            this.checkForErrors({
                onNoErrors: function(){
                    this.setActivated(button, false);
                    this.facade.raiseEvent({
            			type:ORYX.CONFIG.EVENT_LOADING_STATUS,
            			text:ORYX.I18N.SyntaxChecker.noErrors,
            			timeout:10000
            		});
                    //Ext.Msg.alert(ORYX.I18N.Oryx.title, ORYX.I18N.SyntaxChecker.noErrors);
                }.bind(this),
                onErrors: function(){
                    this.enableDeactivationHandler(button);
                }.bind(this),
                onFailure: function(){
                    this.setActivated(button, false);
                    Ext.Msg.alert(ORYX.I18N.Oryx.title, ORYX.I18N.SyntaxChecker.invalid);
                }.bind(this)
            });      
        }
    },
    
    /**
     * Registers handler for deactivating syntax checker as soon as somewhere is clicked...
     * @param {Ext.Button} Toolbar button
     */
    enableDeactivationHandler: function(button){
        var deactivate = function(){
            this.setActivated(button, false);
            this.resetErrors();
            this.facade.unregisterOnEvent(ORYX.CONFIG.EVENT_MOUSEDOWN, deactivate);
        };
        
        this.facade.registerOnEvent(ORYX.CONFIG.EVENT_MOUSEDOWN, deactivate.bind(this));
    },
    
    /**
     * Sets the activated state of the plugin
     * @param {Ext.Button} Toolbar button
     * @param {Object} activated
     */
    setActivated: function(button, activated){
        button.toggle(activated);
        if(activated === undefined){
            this.active = !this.active;
        } else {
            this.active = activated;
        }
    },
    
    /**
     * Performs request to server to check for errors on current model.
     * @methodOf ORYX.Plugins.SyntaxChecker.prototype
     * @param {Object} options Configuration hash
     * @param {String} context A context send to the syntax checker servlet
     * @param {Function} [options.onNoErrors] Raised when model has no errors.
     * @param {Function} [options.onErrors] Raised when model has errors.
     * @param {Function} [options.onFailure] Raised when server communcation failed.
     * @param {boolean} [options.showErrors=true] Display errors on nodes on canvas (by calling ORYX.Plugins.SyntaxChecker.prototype.showErrors)
     */
    checkForErrors: function(options){
        Ext.applyIf(options || {}, {
          showErrors: true,
          onErrors: Ext.emptyFn,
          onNoErrors: Ext.emptyFn,
          onFailure: Ext.emptyFn
        });
            
        Ext.Msg.wait(ORYX.I18N.SyntaxChecker.checkingMessage);

		var ss = this.facade.getStencilSets();
		var data = null;
		var includesJson = false;
		
		if(ss.keys().include("http://b3mn.org/stencilset/bpmn2.0#") ||
			ss.keys().include("http://b3mn.org/stencilset/bpmn2.0choreography#") ||
				ss.keys().include("http://b3mn.org/stencilset/bpmn2.0conversation#")) {
			data = this.facade.getSerializedJSON();
			includesJson = true;
		} else {
			data = this.getRDFFromDOM();
		}
        
        // Send the request to the server.
        new Ajax.Request(ORYX.CONFIG.SYNTAXCHECKER_URL, {
            method: 'POST',
            asynchronous: false,
            parameters: {
                resource: location.href,
                data_json: data,
                context: options.context,
				isJson: includesJson
            },
            onSuccess: function(request){
                var resp = (request&&request.responseText?request.responseText:"{}").evalJSON();
                
                Ext.Msg.hide();
                
                if (resp instanceof Object) {
                    resp = $H(resp)
                    if (resp.size() > 0) {
                        if(options.showErrors) this.showErrors(resp);
                 
                        options.onErrors();
                    }
                    else {
                        options.onNoErrors();
                    }
                }
                else {
                    options.onFailure();
                }
            }.bind(this),
            onFailure: function(){
                Ext.Msg.hide();
                options.onFailure();
            }
        });
    },
    
    /** Called on SHOW_ERRORS_EVENT.
     * 
     * @param {Object} event
     * @param {Object} args
     */
    doShowErrors: function(event, args){
        this.showErrors(event.errors);
    },
    
    /**
     * Shows overlays for each given error
     * @methodOf ORYX.Plugins.SyntaxChecker.prototype
     * @param {Hash|Object} errors
     * @example
     * showErrors({
     *     myShape1: "This has an error!",
     *     myShape2: "Another error!"
     * })
     */
    showErrors: function(errors){
        // If normal object is given, convert to hash
        if(!(errors instanceof Hash)){
            errors = new Hash(errors);
        }
        
        // Get all Valid ResourceIDs and collect all shapes
        errors.keys().each(function(value){
            var sh = this.facade.getCanvas().getChildShapeByResourceId(value);
            if (sh) {
                this.raiseOverlay(sh, this.parseCodeToMsg(errors[value]));
            }
        }.bind(this));
        this.active = !this.active;
        
        //show a status message with a hint to the error messages in the tooltip
        this.facade.raiseEvent({
			type:ORYX.CONFIG.EVENT_LOADING_STATUS,
			text:ORYX.I18N.SyntaxChecker.notice,
			timeout:10000
		});
    },
    parseCodeToMsg: function(code){
    	var msg = code.replace(/: /g, "<br />").replace(/, /g, "<br />");
    	var codes = msg.split("<br />");
    	for (var i=0; i < codes.length; i++) {
    		var singleCode = codes[i];
    		var replacement = this.parseSingleCodeToMsg(singleCode);
    		if (singleCode != replacement) {
    			msg = msg.replace(singleCode, replacement);
    		}
    	}
		
		return msg;
	},
	
	parseSingleCodeToMsg: function(code){
		return ORYX.I18N.SyntaxChecker[code]||code;
	},
    /**
     * Resets all (displayed) errors
     * @methodOf ORYX.Plugins.SyntaxChecker.prototype
     */
    resetErrors: function(){
        this.raisedEventIds.each(function(id){
            this.facade.raiseEvent({
                type: ORYX.CONFIG.EVENT_OVERLAY_HIDE,
                id: id
            });
        }.bind(this))
        
        this.raisedEventIds = [];
        this.active = false;
    },
    
    raiseOverlay: function(shape, errorMsg){
        var id = "syntaxchecker." + this.raisedEventIds.length;
        var crossId = ORYX.Editor.provideId();
        var cross = ORYX.Editor.graft("http://www.w3.org/2000/svg", null, ['path', {
            "id":crossId,
            "title":"",
            "stroke-width": 5.0,
            "stroke": "red",
            "d": "M20,-5 L5,-20 M5,-5 L20,-20",
            "line-captions": "round"
        }]);
        
        this.facade.raiseEvent({
            type: ORYX.CONFIG.EVENT_OVERLAY_SHOW,
            id: id,
            shapes: [shape],
            node: cross,
            nodePosition: shape instanceof ORYX.Core.Edge ? "START" : "NW"
        });
        
        var tooltip = new Ext.ToolTip({
        	showDelay:50,
        	html:errorMsg,
        	target:crossId
        });
        
        this.raisedEventIds.push(id);
        
        return cross;
    }
});

ORYX.Plugins.SyntaxChecker.CHECK_FOR_ERRORS_EVENT = "checkForErrors";
ORYX.Plugins.SyntaxChecker.RESET_ERRORS_EVENT = "resetErrors";
ORYX.Plugins.SyntaxChecker.SHOW_ERRORS_EVENT = "showErrors";

ORYX.Plugins.PetrinetSyntaxChecker = ORYX.Plugins.SyntaxChecker.extend({
    /*FIXME:: BPMN + EPC syntax checker needs rdf, but petri nets needs erdf.
     * So we override getRDFFromDOM from AbstractPlugin to return erdf.
     */
    getRDFFromDOM: function(){
        return this.facade.getERDF();
    }
});