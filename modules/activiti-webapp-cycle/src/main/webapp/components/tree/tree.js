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
   * Tree constructor.
   *
   * @param {String} htmlId The HTML id of the parent element
   * @return {Activiti.component.Tree} The new component.Tree instance
   * @constructor
   */
  Activiti.component.Tree = function Tree_constructor(htmlId, nodesJson, containingNavigationTabIndex, connectorId, nodeId, vFolderId, treeId, tab)
  {
    Activiti.component.Tree.superclass.constructor.call(this, "Activiti.component.Tree", htmlId);

    // Create new service instances and set this component to receive the callbacks
    this.services.repositoryService = new Activiti.service.RepositoryService(this);

    // Listen for updateArtifactView event in order to be able to expand the tree up to the selected artifact
    this.onEvent(Activiti.event.updateArtifactView, this.onUpdateArtifactView);

    this._treeId = treeId;

    this._nodesJson = nodesJson;
    this._containingNavigationTabIndex = containingNavigationTabIndex;

    this._treeView = {};
    this._dialog = {};
    
    this._tab = tab;

    this._activeNavigationTabIndex = 0;

    this._connectorId = connectorId;
    this._nodeId = nodeId;
    this._vFolderId = vFolderId;

    return this;
  };

  YAHOO.extend(Activiti.component.Tree, Activiti.component.Base,
  {
  
    /**
    * Fired by YUI when parent element is available for scripting.
    * Template initialisation, including instantiation of YUI widgets and event listener binding.
    *
    * @method onReady
    */
    onReady: function Tree_onReady()
    {
      // Initialize the tree
      this.initTree();
    },
    
    /**
     * Event listener for "Activiti.event.updateArtifactView" event, checks whether the tree is 
     * initialized, initializes the tree if it isn't and sets focus to the currently active node
     * if the tree is initialized.
     *
     * @method onTriggerEvent
     * @param event {Object} the event that triggered the invokation of this method
     * @param args {array} an array of arguments containing the object literal of the event at index 1
     */
    onUpdateArtifactView: function Tree_onUpdateArtifactView(event, args)
    {
      var eventValue = args[1].value;
      this._connectorId = eventValue.connectorId;
      this._nodeId = eventValue.nodeId;
      this._vFolderId = eventValue.vFolderId; 
      
      this._activeNavigationTabIndex = eventValue.activeNavigationTabIndex;

      if(this._containingNavigationTabIndex == args[1].value.activeNavigationTabIndex) {
        if(this.getNodeByConnectorAndId(this._connectorId, this._nodeId)) {
          this.highlightCurrentNode();          
        } else if(this._connectorId && this._nodeId) {
          this.services.repositoryService.loadTree({connectorId: this._connectorId||'', nodeId: this._nodeId||'', vFolderId: this._vFolderId||'', treeId: this._treeId||''});
        }
      } 
    },

    onLoadTreeSuccess: function Tree_onLoadTreeSuccess(response, obj)
    {
      this._nodesJson = response.json;
      this.initTree();
    },

    initTree: function Tree_initTree()
    {
      var me = this;
      
      // Add a 'home' icon to each root node of the process solutions tree
      if(this._treeId === "ps") {
        for(var index in this._nodesJson) {
          this._nodesJson[index].labelStyle = "icon-home";
        }
      }
      
      // Define a method to dynamically load tree nodes tp pass it to the tree instance later
      var loadTreeNodes = function (node, fnLoadComplete) {
        if(node.data.connectorId && node.data.nodeId && node.data.connectorId == me._connectorId && node.data.nodeId == me._nodeId) {
          me.highlightCurrentNode();
        }
        if(node.data.file || node.children.length > 0) {
          // TODO: (Nils Preusker, 16.2.2011) check the "node.children.length > 0" part...
          // Don't attempt to load child nodes for artifacts or nodes that are already loaded
          fnLoadComplete();
        } else {
          me.services.repositoryService.loadChildNodes({connectorId: node.data.connectorId||'', nodeId: node.data.nodeId||'', vFolderId: node.data.vFolderId||'', treeId: me._treeId||''}, node, fnLoadComplete);
        }
      };

      // instantiate the TreeView control
      this._treeView = new YAHOO.widget.TreeView(this.id, this._nodesJson);

      // set the callback function to dynamically load child nodes
      // set iconMode to 1 to use the leaf node icon when a node has no children. 
      this._treeView.setDynamicLoad(loadTreeNodes, 1);
      this._treeView.render();
      
      if(this._treeView.getNodeCount() == 0) {
    	  // select the repository tree if the process solution tree has no nodes:
    	  this._tab._tabView.selectTab(1);   	  
      }

      // Subscribe to the click event of the tree
      this._treeView.subscribe("clickEvent", this.onClickEvent, null, this);
    },

    /**
     * Will fire an Activiti.event.updateArtifactView event so other components may react.
     *
     * @method onClickEvent
     * @param e {object} The click event
     */
    onClickEvent: function Tree_onClickEvent(event)
    {
      this.fireEvent(Activiti.event.updateArtifactView, {activeNavigationTabIndex: this._containingNavigationTabIndex, activeArtifactViewTabIndex: 0, connectorId: event.node.data.connectorId, nodeId: event.node.data.nodeId, vFolderId: event.node.data.vFolderId, label: event.node.label, file: event.node.data.file}, null, true);
    },

    /**
     * Success callback of the RepositoryService method loadChildNodes. This method gets invoked when the asynchronous request returns. It creates a
     * new TextNode instacne based on the JSON in the response and inserts it into the tree. It also determines the file type and sets the style 
     * attribute of the node accordingly.
     * 
     * @method onLoadChildNodesSuccess
     * @param response the response object that contains the JSON response string
     * @param obj an array of objects that contains the containing node at index 0 and the loadComplete callback of the treeView component at index 1
     */
    onLoadChildNodesSuccess: function Tree_RepositoryService_onLoadChildNodesSuccess(response, obj)
    {
      var me = this;
      if(response.json.authenticationError) {
        return new Activiti.component.AuthenticationDialog(this.id, response.json.repoInError, response.json.authenticationError);
      }
      // Retrieve rest api response
      var treeNodesJson = response.json;

      if(treeNodesJson) {
        for(var i = 0; i<treeNodesJson.length; i++) {
          var node = new YAHOO.widget.TextNode(treeNodesJson[i], obj.parentNode, treeNodesJson[i].expanded);
        }
      }

      // call the fnLoadComplete function that the treeView component provides to 
      // indicate that the loading of the sub nodes was successfull.
      obj.fnLoadComplete();
    },

    /**
     * Failure callback of the RepositoryService method loadChildNodes. This method gets invoked if anything goes wrong while loading a tree node. 
     *
     * @method onLoadChildNodesFailure
     * @param response the response object that contains the JSON response string
     * @param obj an array of objects that contains the containing node at index 0 and the loadComplete callback of the treeView component at index 1
     */
    onLoadChildNodesFailure: function Tree_RepositoryService_onLoadChildNodesFailure(response, obj)
    {
      // TODO: see how we can show a custom error message here.

      // call the trees load complete function anyway to keep the rest of the tree usable.
      obj.fnLoadComplete();
    },

    highlightCurrentNode: function Tree_highlightCurrentNode() {
      var me = this;
      var node = this.getNodeByConnectorAndId(this._connectorId, this._nodeId);
      if(node && (node != this._treeView.currentFocus) ) {
        // if the node isn't already focused this is a browser history event and we manually set focus to the current node
        if(node) {
          node.focus();
        }
      }
    },

    getNodeByConnectorAndId: function Tree_getNodeByConnectorAndId(connectorId, nodeId) {
      var nodes = this._treeView.getNodesBy( function(node) {
        if(node.data.connectorId && node.data.connectorId === connectorId && node.data.nodeId && node.data.nodeId === nodeId) {
          return true;
        }
        return false;
      });
      return nodes ? nodes[0] : null;
    }

  });

})();