if (!ORYX.Plugins) 
    ORYX.Plugins = new Object();

/**
   This plugin deploys the diagram to the Activiti Engine.
   Needs server communication.
   @class ORYX.Plugins.Deployer
   @constructor Creates a new instance
   @extends ORYX.Plugins.AbstractPlugin
*/
ORYX.Plugins.Deployer = ORYX.Plugins.AbstractPlugin.extend({
    /**@private*/
    construct: function(){
        arguments.callee.$.construct.apply(this, arguments);
                
        this.active = false;
        this.raisedEventIds = [];
        
        this.facade.offer({
            'name': ORYX.I18N.Deployer.name,
            'functionality': this.perform.bind(this),
            'group': ORYX.I18N.Deployer.group,
            'icon': ORYX.PATH + "images/checker_syntax.png",
            'description': ORYX.I18N.Deployer.desc,
            'index': 0,
            'toggle': true,
            'minShape': 0,
            'maxShape': 0
        });
    },
    
    perform: function(button, pressed){
        if (pressed) {
            this.deploy({
                onSuccess: function(){
                    this.setActivated(button, false);
                    Ext.Msg.alert("deployed process", "deployed");
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
    
    deploy: function(options){
    	
    	Ext.applyIf(options || {}, {
            onSuccess: Ext.emptyFn,
            onFailure: Ext.emptyFn
        });
            
        Ext.Msg.wait(ORYX.I18N.SyntaxChecker.checkingMessage);

		var data = this.facade.getSerializedJSON();
        
        // Send the request to the server.
        new Ajax.Request(ORYX.CONFIG.DEPLOY_URL, {
            method: 'POST',
            asynchronous: false,
            parameters: {
            	resource: location.href,
                data_json: data
            },
            onSuccess: function(request){
                Ext.Msg.hide();
                options.onSuccess();
                
            }.bind(this),
            onFailure: function(){
                Ext.Msg.hide();
                options.onFailure();
            }
        });
    }
});