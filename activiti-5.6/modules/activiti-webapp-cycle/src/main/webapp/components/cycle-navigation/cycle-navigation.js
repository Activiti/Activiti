(function()
{
  /**
   * Shortcuts
   */
  var Dom = YAHOO.util.Dom,
      Selector = YAHOO.util.Selector,
      Event = YAHOO.util.Event,
      Pagination = Activiti.util.Pagination,
      $html = Activiti.util.decodeHTML;
      
  /**
   * CycleNavigation constructor.
   *
   * @param {String} htmlId The HTML id of the parent element
   * @return {Activiti.component.CycleNavigation} The new component.CycleNavigation instance
   * @constructor
   */
  Activiti.component.CycleNavigation = function CycleNavigation_constructor(htmlId)
  {
    Activiti.component.CycleNavigation.superclass.constructor.call(this, "Activiti.component.CycleNavigation", htmlId);

    // Create new service instances and set this component to receive the callbacks
    this.services.repositoryService = new Activiti.service.RepositoryService(this);

    // Listen for updateArtifactView event in order to be able to switch to the selected tab if needed
    this.onEvent(Activiti.event.updateArtifactView, this.onUpdateArtifactView);

    this.messages = {};
    this._tabView;
    
    this._connectorId = "";
    this._nodeId = "";
    this._vFolderId = "";
    this._file = false;
    this._label = "";
    this._activeNavigationTabIndex = 0;
    this._activeArtifactViewTabIndex = 0;
    
    this._processSolutionsTree = {};
    this._repoTree = {};
    
    this._contextMenu = {};
    
    this.waitDialog = 
    		new YAHOO.widget.Panel(this.id + "-wait",
    			{ width:"200px", 
    			  fixedcenter:true, 
    			  close:false, 
    			  draggable:false, 
    			  zindex:4,
    			  modal:true,
    			  visible:false
    			} 
    		);
    this.waitDialog.setBody('<div id="cycle-navigation-waiting-dialog"/>');
    this.waitDialog.render(document.body);
    
    return this;
  };

  YAHOO.extend(Activiti.component.CycleNavigation, Activiti.component.Base,
  {
  
    /**
    * Fired by YUI when parent element is available for scripting.
    * Template initialisation, including instantiation of YUI widgets and event listener binding.
    *
    * @method onReady
    */
    onReady: function CycleNavigation_onReady()
    {
      if (!Activiti.event.isInitEvent(Activiti.event.updateArtifactView)) {
        this.fireEvent(Activiti.event.updateArtifactView, {activeNavigationTabIndex: 0, activeArtifactViewTabIndex: 0, connectorId: "PS", nodeId: '', vFolderId: '', label: '', file: ''}, null, true);
      }

      var reloadLink = document.createElement('a');
      reloadLink.setAttribute('class', 'tree-refresh-link')
      reloadLink.setAttribute('href', "javascript:location.reload();");
      reloadLink.setAttribute('title', "Refresh Tree");
      reloadLink.innerHTML = '<img src="' + Activiti.constants.URL_CONTEXT + '/res/images/arrow_refresh.png" alt="Refresh Tree">';
      document.getElementById(this.id).appendChild(reloadLink);
    },

    /**
     * Event listener for "Activiti.event.updateArtifactView" event, sets the focus to the currently active tab and 
     * checks whether the contents of the tab are initialized. If not, it loads the contents via the repositoryService.
     *
     * @method onUpdateArtifactView
     * @param event {Object} the event that triggered the invokation of this method
     * @param args {array} an array of arguments containing the object literal of the event at index 1
     */
    onUpdateArtifactView: function RepoTree_onUpdateArtifactView(event, args)
    {
      var me = this;      
      var eventValue = args[1].value;
      
      this._activeNavigationTabIndex = eventValue.activeNavigationTabIndex;
      this._activeArtifactViewTabIndex = eventValue.activeArtifactViewTabIndex;
      this._connectorId = eventValue.connectorId;
      this._nodeId = eventValue.nodeId;
      this._vFolderId = eventValue.vFolderId; 
      this._label = eventValue.label;
      this._file = eventValue.file;

      // Check whether the tab view is already initialized
      if(!this._tabView) {
        // Initialize the tab view
        this._tabView = new YAHOO.widget.TabView();
        
        // Add tab for the "process solutions" tree
        var processSolutionsTreeUrl = Activiti.service.REST_PROXY_URI_RELATIVE + "tree?" + Activiti.service.Ajax.jsonToParamString({connectorId: this._connectorId||'', nodeId: this._nodeId||'', vFolderId: this._vFolderId||'', treeId: "ps"});
        var processSolutionsTab = new YAHOO.widget.Tab({
          label: this.msg("label.process-solutions"), 
          dataSrc: processSolutionsTreeUrl, 
          cacheData: true
        });

        processSolutionsTab.loadHandler.success = function(response) {
          me.onLoadProcessSolutionsTabSuccess(this /* the tab */, response);
        };
        processSolutionsTab.loadHandler.failure = function(response) {
          me.onLoadProcessSolutionsTabFailure(this /* the tab */, response);
        };
        this._tabView.addTab(processSolutionsTab);

        // Add tab for the "repositories" tree
        var repositoriesTreeUrl = Activiti.service.REST_PROXY_URI_RELATIVE + "tree?" + Activiti.service.Ajax.jsonToParamString({connectorId: this._connectorId||'', nodeId: this._nodeId||'', vFolderId: this._vFolderId||'', treeId: "repo"});
        var repositoriesTab = new YAHOO.widget.Tab({
          label: this.msg("label.repositories"), 
          dataSrc: repositoriesTreeUrl,
          cacheData: true
        });
        
        repositoriesTab.loadHandler.success = function(response) {
          me.onLoadRepositoriesTabSuccess(this /* the tab */, response);
        };
        repositoriesTab.loadHandler.failure = function(response) {
          me.onLoadRepositoriesTabFailure(this /* the tab */, response);
        };
        this._tabView.addTab(repositoriesTab);
        
        // Append the tab view to a HTML element...
        this._tabView.appendTo(this.id);
        
        // Select the active tab without firing an event (last parameter is 'silent=true')
        this._tabView.set("activeTab", this._tabView.getTab(args[1].value.activeNavigationTabIndex), true);
        
        // replace the tabViews onActiveTabChange evnet handler with our own one        
        this._tabView.unsubscribe("activeTabChange", this._tabView._onActiveTabChange);
        this._tabView.subscribe("activeTabChange", this.onActiveTabChange, null, this);

        // Initialize the context menu

        // TODO: (Nils Preusker, 17.2.2011), This is a hard coded implementation of "dynamic" context menu entries, based on the type of the tree node.
        // There are several ways of doing this right in the future:
        // 1) Dynamically load contextr menu when it is invoked. THis includes adding a "context-menu.get" webscript and javascript logic to render it.
        //    The disadvantage is that the context menues would take some time to load, which might be counter intuitive for the user.
        // 2) Add context menu information to the data array that every tree node contains and render a context menu based on that.
        // 
        // Another issue is that the related dialogs should be dynamic as well. Maybe we could use a similar approach like we did for the actions menu.
        
        this._contextMenu = new YAHOO.widget.ContextMenu(this.id + "-cycle-tree-context-menu-div", {
          trigger: this.id
        });

        this._contextMenu.render(document.body);
        this._contextMenu.subscribe("triggerContextMenu", function (event, menu) {
          // retrieve the node the context menu was triggered on
          var target = this.contextEventTarget;
          var node = me._processSolutionsTree._treeView ? me._processSolutionsTree._treeView.getNodeByElement(target) : null;
          if(me._activeNavigationTabIndex === 0 && node) {
            this.clearContent();
            if(node.data.file) {
              // this.addItems([]);
            } else if(node.data.folder) {
              if(node.data.vFolderType && node.data.vFolderType == "Management") {
                this.addItem({ text: "Add New Business Document...", onclick: { fn: me.onCreateArtifactContextMenuClick, obj: {title: "New business document", node: node}, scope: me } });
              } else if(node.data.vFolderType && node.data.vFolderType == "Requirements") {
                this.addItem({ text: "Add New Requirement...", onclick: { fn: me.onCreateArtifactContextMenuClick, obj: {title: "New requirement", node: node} , scope: me } });
              } else if(node.data.vFolderType && node.data.vFolderType == "Processes") {
                this.addItem({ text: "Add New Process Diagram...", onclick: { fn: me.onAddNewProcessDiagramContextMenuClick, obj: node, scope: me } });
              }
              this.addItem({ text: "Create Process Solution...", onclick: { fn: me.onCreateProcessSolutionContextMenuClick, obj: node, scope: me } });
            }
            this.render();
          } else if (me._activeNavigationTabIndex === 1) {
            node = me._repoTree._treeView ? me._repoTree._treeView.getNodeByElement(target) : null;
            if(node) {
              this.clearContent();
              if(node.data.file) {
                // this.addItems([]);
              } else if(node.data.folder) {
                this.addItem({ text: "New artifact...", onclick: { fn: me.onCreateArtifactContextMenuClick, obj: {title: "New artifact", node: node}, scope: me } });
                this.addItem({ text: "New folder...", onclick: { fn: me.onCreateFolderContextMenuClick, obj: node, scope: me } });
              }          
              this.render();
            }
          }
        });
        
      } else {
        // Update active tab selection silently, without firing an event (last parameter 'true')
        this._tabView.set("activeTab", this._tabView.getTab(this._activeNavigationTabIndex), true);
      }
      // TODO:
      // ** Known limitation ** 
      // If a user clicks a link in the links list of an artifact and the repository tree is not yet initialized (tab has not been clicked since last page load), 
      // the tree doesn't open because the tabs datasource still uses the initial URL. The first shot at fixing this by updating the tabs data source didn't work.
      // Here is the code anyway...
      
      // if(this._tabView.getTab(1) && this._tabView.getTab(1)._configs.dataLoaded.value) {
      //   this._tabView.getTab(1)._configs.dataSrc = Activiti.service.REST_PROXY_URI_RELATIVE + "tree?" + Activiti.service.Ajax.jsonToParamString({connectorId: this._connectorId||'', nodeId: this._nodeId||'', vFolderId: this._vFolderId||'', treeId: "repo"});
      // }
    },

    /**
     * This method is invoked when the "Create artifact here..." context menu item is clicked. It returns a new dialog component to
     * provide details for the new artifact.
     *
     * @method onCreateArtifactContextMenuClick
     * @param eventName {string} the name of the event that lead to the invokation of this method
     * @param params {Array} array of parameters that contains the event that lead to the invokation of this method
     * @param node {Object} the tree node that the context menu was invoked on
     * @return {Activiti.component.CreateArtifactDialog} dialog to provide details for the new artifact
     */
    onCreateArtifactContextMenuClick: function CycleNavigation_onCreateArtifactContextMenuClick(eventName, params, obj)
    {
      var me = this;
      var fnOnUpload = function(response) {
        var json = YAHOO.lang.JSON.parse(response.responseText);
        me.fireEvent(Activiti.event.updateArtifactView, {activeNavigationTabIndex: me._activeNavigationTabIndex, activeArtifactViewTabIndex: 0, connectorId: json.connectorId, nodeId: json.nodeId, vFolderId: json.vFolderId, label: json.label, file: "true"}, null, true);
      };
      return new Activiti.component.CreateArtifactDialog(this.id, obj.node.data.connectorId, obj.node.data.nodeId, obj.title, fnOnUpload);
    },

    onAddNewProcessDiagramContextMenuClick: function CycleNavigation_onAddNewProcessDiagramContextMenuClick(eventName, params, node)
    {
      // Remember the attributes of the current node (the folder that will contain the new artifact) in order to re-load the tree later
      this._connectorId = node.data.connectorId;
      this._nodeId = node.data.nodeId;
      this._vFolderId = node.data.vFolderId; 
      this._label = node.label;
      this._file = node.data.file;
      // Call the repository service to create the new artifact
      this.services.repositoryService.createArtifact({connectorId: node.data.connectorId, parentFolderId: node.data.nodeId, artifactName: 'New Model', file: ''});
    },
    
    onCreateArtifactSuccess: function CycleNavigation_onCreateArtifactSuccess(response, obj)
    {
      // Open the new model in the Activiti Modeler using the URL from the response
      var responseJson = YAHOO.lang.JSON.parse(response.serverResponse.responseText);
      window.open(responseJson.openLinks[0]["Open modeler"],'test');

      // Show dialog to refresh tree
      var me = this;
		  var content = document.createElement("div");
      content.innerHTML = '<div class="bd"><form id="' + this.id + '-refresh-page" accept-charset="utf-8"><h1>Content might have changed</h1><p>The content of the current folder might have changed.<br/>Would you like to refresh the view?</p></form></div>';

      var dialog = new YAHOO.widget.Dialog(content, 
      {
        fixedcenter: "contained",
        visible: false,
        constraintoviewport: true,
        modal: true,
        hideaftersubmit: false,
        buttons: [
          { text: this.msg("button.yes") , handler: { fn: function(event, dialog) {
              // Fire an event to select the parent folder of the new artifact
              me.fireEvent(Activiti.event.updateArtifactView, {activeNavigationTabIndex: me._activeNavigationTabIndex, activeArtifactViewTabIndex: 0, connectorId: me._connectorId, nodeId: me._nodeId, vFolderId: me._vFolderId, label: me._label, file: me._file}, null, true);
              // Reload the page in order to re-fetch the files that might have changed
              location.reload();
            }, isDefault:true }
          },
          { text: this.msg("button.no"), handler: { fn: function(event, dialog) {
              dialog.cancel();
            }}
          }
        ]
      });
		  dialog.render(document.body);
		  dialog.show();
    },
    
    onCreateArtifactFailure: function CycleNavigation_onCreateArtifactFailure(response, obj)
    {
      // TODO: Show error message
    },

    /**
     * This method is invoked when the "Create folder here..." context menu item is clicked. It returns a new dialog component to
     * provide details for the new folder.
     *
     * @method onCreateArtifactContextMenuClick
     * @param eventName {string} the name of the event that lead to the invokation of this method
     * @param params {Array} array of parameters that contains the event that lead to the invokation of this method
     * @param node the tree node that the context menu was invoked on
     * @return {Activiti.component.CreateFolderDialog} dialog to provide details for the new folder
     */
    onCreateFolderContextMenuClick: function CycleNavigation_onCreateFolderContextMenuClick(eventName, params, node)
    {
      return new Activiti.component.CreateFolderDialog(this.id, node.data.connectorId, node.data.nodeId);
    },

    onCreateProcessSolutionContextMenuClick: function CycleNavigation_onCreateProcessSolutionContextMenuClick(eventName, params, node) {
      var me = this;
		  var content = document.createElement("div");
      content.innerHTML = '<div class="bd"><form id="' + this.id + '-create-process-solution-form" accept-charset="utf-8"><h1>Create new Process Solution</h1><table><tr><td><label>Name:<br/><input type="text" name="processSolutionName" value="" /></label><br/></td></tr></table></form></div>';      
    
      var dialog = new YAHOO.widget.Dialog(content, 
      {
        fixedcenter: "contained",
        visible: false,
        constraintoviewport: true,
        modal: true,
        hideaftersubmit: false,
        buttons: [
          { text: Activiti.i18n.getMessage("button.ok") , handler: { fn: function CreateFolderDialog_onSubmit(event, dialog) {
              me.waitDialog.show();
              me.services.repositoryService.createProcessSolution(dialog.getData());
              if (dialog) {
                dialog.destroy();
              }
            }, isDefault:true }
          },
          { text: Activiti.i18n.getMessage("button.cancel"), handler: { fn: function CreateFolderDialog_onCancel(event, dialog) {
              dialog.cancel();
            }}
          }
        ]
      });
		  dialog.render(document.body);
		  dialog.show();
    },
    
    onCreateProcessSolutionSuccess: function CycleNavigation_onCreateProcessSolutionSuccess(response, obj)
    {
      this.waitDialog.hide();
      if(response.json) {
        this.fireEvent(Activiti.event.updateArtifactView, {activeNavigationTabIndex: this._activeNavigationTabIndex, activeArtifactViewTabIndex: 0, connectorId: response.json.connectorId, nodeId: response.json.nodeId, vFolderId: response.json.vFolderId||'', label: response.json.label, folder: response.json.folder}, null, true);
      }
    },
    
    onCreateProcessSolutionFailure: function CycleNavigation_onCreateProcessSolutionFailure(response, obj)
    {
      this.waitDialog.hide();
    },

    /**
     * We need to override the Base_setMessage method here so that we can store the messages and pass 
     * them on to the components that are instanciated here.
     *
     * @method setMessages
     */
    setMessages: function CycleNavigation_setMessages(messages) 
    {
      this.messages = messages;
      Activiti.i18n.addMessages(this.messages, this.name);
      return this;
    },

    onLoadProcessSolutionsTabSuccess: function Artifact_onLoadProcessSolutionsTabSuccess(tab, response) 
    {
      var responseJson = YAHOO.lang.JSON.parse(response.responseText);
      tab.set('content', "<div id='process-solutions-tree-" + this.id + "'></div>");
      this._processSolutionsTree = new Activiti.component.Tree("process-solutions-tree-" + this.id, responseJson, 0, this._connectorId, this._nodeId, this._vFolderId, "ps", this).setMessages(this.messages);
    },

    onLoadProcessSolutionsTabFailure: function Artifact_onLoadProcessSolutionsTabFailure(tab, response) 
    {
      var responseJson = YAHOO.lang.JSON.parse(response.responseText);
      var tabContent = "<h3>Java Stack Trace:</h3>";
      for(var line in responseJson.callstack) {
        if( line == 1 || (responseJson.callstack[line].indexOf("org.activiti.cycle") != -1) || responseJson.callstack[line].indexOf("org.activiti.rest.api.cycle") != -1) {
          tabContent += "<span class=\"cycle-stack-trace-highlight\">" + responseJson.callstack[line] + "</span>";
        } else {
          tabContent += "<span class=\"cycle-stack-trace\">" + responseJson.callstack[line] + "</span>";
        }
      }
      tab.set('content', tabContent);
      Activiti.widget.PopupManager.displayError(responseJson.message);
    },
    
    onLoadRepositoriesTabSuccess: function Artifact_onLoadRepositoriesTabSuccess(tab, response) 
    {
      var responseJson = YAHOO.lang.JSON.parse(response.responseText);
      tab.set('content', "<div id='repositories-tree-" + this.id + "'></div>");
      this._repoTree = new Activiti.component.Tree("repositories-tree-" + this.id, responseJson, 1, this._connectorId, this._nodeId, this._vFolderId, "repo", this).setMessages(this.messages);
    },

    onLoadRepositoriesTabFailure: function Artifact_onLoadRepositoriesTabFailure(tab, response) 
    {
      var responseJson = YAHOO.lang.JSON.parse(response.responseText);
      var tabContent = "<h3>Java Stack Trace:</h3>";
      for(var line in responseJson.callstack) {
        if( line == 1 || (responseJson.callstack[line].indexOf("org.activiti.cycle") != -1) || responseJson.callstack[line].indexOf("org.activiti.rest.api.cycle") != -1) {
          tabContent += "<span class=\"cycle-stack-trace-highlight\">" + responseJson.callstack[line] + "</span>";
        } else {
          tabContent += "<span class=\"cycle-stack-trace\">" + responseJson.callstack[line] + "</span>";
        }
      }
      tab.set('content', tabContent);
      Activiti.widget.PopupManager.displayError(responseJson.message);
    },

    onActiveTabChange: function CycleNavigation_onActiveTabChange(event)
    {
      var newActiveTabIndex = this._tabView.getTabIndex(event.newValue);
      this.fireEvent(Activiti.event.updateArtifactView, {activeNavigationTabIndex: newActiveTabIndex, activeArtifactViewTabIndex: '', connectorId: '', nodeId: '', vFolderId: '', label: '', file: ''}, null, true);
      YAHOO.util.Event.preventDefault(event);
    }
  });

})();
