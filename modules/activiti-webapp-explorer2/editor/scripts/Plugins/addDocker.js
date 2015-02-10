if(!ORYX.Plugins)
	ORYX.Plugins = new Object();

ORYX.Plugins.AddDocker = Clazz.extend({

	/**
	 *	Constructor
	 *	@param {Object} Facade: The Facade of the Editor
	 */
	construct: function(facade) {
		this.facade = facade;
		this.enableAdd = false;
		this.enableRemove = false;
		
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_MOUSEDOWN, this.handleMouseDown.bind(this));
	},
	
	setEnableAdd: function(enable){
		this.enableAdd = enable;
		
		if(this.enableAdd) {
    		jQuery("#add-bendpoint-button").addClass('pressed');
    	} else {
    		jQuery("#add-bendpoint-button").removeClass('pressed');
    		jQuery("#add-bendpoint-button").blur();
    	}
	},
	setEnableRemove: function(enable){
		this.enableRemove = enable;
		
		if(this.enableRemove) {
    		jQuery("#remove-bendpoint-button").addClass('pressed');
    	} else {
    		jQuery("#remove-bendpoint-button").removeClass('pressed');
    		jQuery("#remove-bendpoint-button").blur();
    	}
	},
	
    enabledAdd: function(enable){
        return this.enableAdd;
    },
    enabledRemove: function(){
        return this.enableRemove;
    },
	
	/**
	 * MouseDown Handler
	 *
	 */	
	handleMouseDown: function(event, uiObj) {
		if (this.enabledAdd() && uiObj instanceof ORYX.Core.Edge) {
            this.newDockerCommand({
                edge: uiObj,
                position: this.facade.eventCoordinates(event)
            });
            this.setEnableAdd(false);
            
		} else if (this.enabledRemove() &&
				   uiObj instanceof ORYX.Core.Controls.Docker &&
				   uiObj.parent instanceof ORYX.Core.Edge) {
            this.newDockerCommand({
                edge: uiObj.parent,
                docker: uiObj
            });
            this.setEnableRemove(false);
        }
		document.body.style.cursor = 'default';
	},
    
    // Options: edge (required), position (required if add), docker (required if delete)
    newDockerCommand: function(options){
        if(!options.edge)
            return;

        var commandClass = ORYX.Core.Command.extend({
            construct: function(addEnabled, deleteEnabled, edge, docker, pos, facade){
                this.addEnabled = addEnabled;
                this.deleteEnabled = deleteEnabled;
                this.edge = edge;
                this.docker = docker;
                this.pos = pos;
                this.facade = facade;
            },
            execute: function(){
                if (this.addEnabled) {
					if (!this.docker){
                    	this.docker = this.edge.addDocker(this.pos);
						this.index = this.edge.dockers.indexOf(this.docker);
					} else {
                    	this.edge.add(this.docker, this.index);
					}
                }
                else if (this.deleteEnabled) {
					this.index = this.edge.dockers.indexOf(this.docker);
                    this.pos = this.docker.bounds.center();
                    this.edge.removeDocker(this.docker);
                }
                this.edge.getLabels().invoke("show");
                this.facade.getCanvas().update();
                this.facade.updateSelection();
            },
            rollback: function(){
                if (this.addEnabled) {
                    if (this.docker instanceof ORYX.Core.Controls.Docker) {
                        this.edge.removeDocker(this.docker);
                    }
                }
                else if (this.deleteEnabled) {
                    this.edge.add(this.docker, this.index);
                }
                this.edge.getLabels().invoke("show");
                this.facade.getCanvas().update();
                this.facade.updateSelection();
            }
        })
        
        var command = new commandClass(this.enabledAdd(), this.enabledRemove(), options.edge, options.docker, options.position, this.facade);
        
        this.facade.executeCommands([command]);
    }
});

