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

/**
 * @author Falko Menge <falko.menge@camunda.com>
 */

// define namespace if it doesn't already exist
if (!ORYX) { 
    var ORYX = new Object();
}
if (!ORYX.Plugins) { 
    ORYX.Plugins = new Object();
}

ORYX.Plugins.ActivitiCycle = ORYX.Plugins.AbstractPlugin.extend({

	button: undefined,

	panel: undefined,
	
	numberOfLinks: 1,

	construct: function() {

		// call super class constructor
		arguments.callee.$.construct.apply(this, arguments);

		// add button to toolbar
//		this.facade.offer({
//            'name': 'Activiti Cycle: 0 Links',
//            'cls': 'x-btn-text-icon', // button with icon and text
//            'functionality': this.perform.bind(this),
//            'group': 'Activiti',
//            'icon': ORYX.PATH + "images/activiti_logo_16x16.png",
//            'description': 'Activiti Cycle',
//            'index': 1,
//            'minShape': 0,
//            'maxShape': 0
//        });

		this.panel = new Ext.Panel({
			title: this.getPanelTitle(),
			cls: 'activiticyclepanel',
			border: false,
			autoWidth: true,
			autoScroll: true,
			tbar: [{
				text: 'Show links',
				handler: this.showLinks.bind(this),
	            'cls': 'x-btn-text-icon', // button with icon and text
	            'icon': ORYX.PATH + "images/activiti_logo_16x16.png"
			},{
				text: 'Add link',
				handler: this.openAddLinkDialog.bind(this),
	            'cls': 'x-btn-text-icon', // button with icon and text
	            'icon': ORYX.PATH + "images/activiti_logo_16x16.png"
			}]//,
//			html: '3 Artifacts linked with this model'
		});

		var region = this.facade.addToRegion("west", this.panel);
		
//		this.button = new Ext.Button({
//			text: 'Add Link',
//            'cls': 'x-btn-text-icon', // button with icon and text
//            'icon': ORYX.PATH + "images/activiti_logo_16x16.png",
//			autoWidth: true,
//			handler: this.perform.bind(this)
//		});
//		this.panel.add(this.button);	
//		this.panel.doLayout();

	},
	
	showLinks: function () {
        // fake data
		validExtensions = [ {
					title : 'ABPM-2',
					definition : 'User Story',
					'extends' : 'JIRA'
				} ];
        loadedExtensions = [];
        successCallback = this.openAddLinkDialog.bind(this);
        
        // Extract the data
        var data = [];
        validExtensions.each(function(value){
            data.push([value.title, value.definition, value["extends"]]);
        });
        
        // Create a new Selection Model
        var sm = new Ext.grid.CheckboxSelectionModel();
        
        // Create a new Grid with a selection box
        var grid = new Ext.grid.GridPanel({
        	deferRowRender: false,
            id: 'oryx_new_stencilset_extention_grid',
            store: new Ext.data.SimpleStore({
                fields: ['title', 'definition', 'extends']
            }),
            cm: new Ext.grid.ColumnModel([
                sm,
                {
                    header: 'Target Artifact',
                    width: 200,
                    sortable: true,
                    dataIndex: 'title'
                },
                {
                    header: 'Type',
                    width: 90,
                    sortable: true,
                    dataIndex: 'definition'
                },
                {
                    header: 'Repository',
                    width: 90,
                    sortable: true,
                    dataIndex: 'extends'
                }
            ]),
            sm: sm,
            frame: true,
            width: 400,
            height: 400,
            iconCls: 'icon-grid',
            listeners: {
                "render": function(){
                    this.getStore().loadData(data);
                    selectItems.defer(1);
                }
            }
        });
        
        function selectItems() {
        	// Select loaded extensions
    		var selectedRecords = new Array();
    		grid.store.each(function(rec) {
    			if(loadedExtensions.any(function(ext) { return ext.definition == rec.get('definition'); }))
    				selectedRecords.push(rec);
    		});
    		sm.selectRecords(selectedRecords);
        }
        
       /* grid.store.on("load", function() { 
        	console.log("okay"); 
        	grid.getSelectionModel().selectRecords(selectedRecords);
        }, this, {delay:500});*/
        
        
        
        // Create a new Panel
        var panel = new Ext.Panel({
            items: [{
                xtype: 'label',
                text: 'The following list show artifacts from Activiti Cycle that are linked with the current model.',
                style: 'margin:10px;display:block'
            }, grid],
            frame: true,
            buttons: [{
                text: 'Remove selected links',
                handler: function(){
                    var selectionModel = Ext.getCmp('oryx_new_stencilset_extention_grid').getSelectionModel();
                    var result = selectionModel.selections.items.collect(function(item){
                        return item.data;
                    });
//                    Ext.getCmp('activiti_cycle_show_links_window').close();
                    successCallback(result);
                }.bind(this)
            }, {
                text: 'Add link',
                handler: function(){
                    var selectionModel = Ext.getCmp('oryx_new_stencilset_extention_grid').getSelectionModel();
                    var result = selectionModel.selections.items.collect(function(item){
                        return item.data;
                    });
//                    Ext.getCmp('activiti_cycle_show_links_window').close();
                    successCallback(result);
                }.bind(this)
            }, {
                text: 'Close',
                handler: function(){
                    Ext.getCmp('activiti_cycle_show_links_window').close();
                }.bind(this)
            }]
        });
        
        // Create a new Window
        var window = new Ext.Window({
            id: 'activiti_cycle_show_links_window',
            width: 427,
            title: 'Activiti Cycle: Linked Artifacts',
            floating: true,
            shim: true,
            modal: true,
            resizable: false,
            autoHeight: true,
            items: [panel]
        });
        
        // Show the window
        window.show();
        
	},

	openAddLinkDialog: function () {
		var options = [
			[
				undefined,
				'User Story',
				'user_story'
			]
		];
		
		var store = new Ext.data.SimpleStore({
	        fields: [{name: 'icon'},
				{name: 'title'},
				{name: 'value'}	],
	        data : options
	    });
	
	    var typeCombo = new Ext.form.ComboBox({
			tpl: '<tpl for="."><div class="x-combo-list-item">{[(values.icon) ? "<img src=\'" + values.icon + "\' />" : ""]} {title}</div></tpl>',
	        store: store,
	        displayField:'title',
			valueField: 'value',
	        typeAhead: true,
	        mode: 'local',
	        triggerAction: 'all',
	        selectOnFocus:true
	    });
		
        // Create a new Panel
        var panel = new Ext.Panel({
            items: [
                {
	                xtype: 'label',
	                text: 'Please select the target artifact from Activiti Cycle that should be linked with the current model.',
	                style: 'margin-top:5px;margin-bottom:5px;display:block'
                },
                {
	                xtype: 'panel',
	                frame: true,
	                html: '<iframe src="http://localhost:8080/activiti-cycle/" width="389" height="300" frameborder="no" />'
                },
                {
	                xtype: 'label',
	                text: 'Type:',
	                style: 'margin-top:10px;display:block'
                },
                typeCombo
            ],
            frame: true,
            buttons: [{
                text: 'Add link',
                handler: function(){
            		this.addLink();
            		Ext.getCmp('activiti_cycle_add_link_window').close();
            	}.bind(this)
            }, {
                text: 'Cancel',
                handler: function(){
                    Ext.getCmp('activiti_cycle_add_link_window').close();
                }.bind(this)
            }]
        });

		// Create a new Window
        var window = new Ext.Window({
            id: 'activiti_cycle_add_link_window',
            width: 427,
            title: 'Activiti Cycle: Add Link',
            floating: true,
            shim: true,
            modal: true,
            resizable: false,
            autoHeight: true,
            items: [panel]
        });
        
        // Show the window
        window.show();
	},
	
	addLink: function () {
		++this.numberOfLinks;
		this.panel.setTitle(this.getPanelTitle());
	},
	
	getPanelTitle: function () {
		return 'Activiti Cycle: ' + this.numberOfLinks + ' artifact' + (this.numberOfLinks == 1 ? '' : 's') + ' linked';
	}

});
