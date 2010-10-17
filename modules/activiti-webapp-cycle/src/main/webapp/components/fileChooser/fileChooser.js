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
	 * FileChooserDialog constructor.
	 *
	 * @param {String} htmlId The HTML id of the parent element
	 * @return {Activiti.component.RepoTree} The new component.FileChooserDialog instance
	 * @constructor
	 */
	Activiti.component.FileChooserDialog = function FileChooserDialog_constructor(htmlId, callbackFn)
  {
    Activiti.component.FileChooserDialog.superclass.constructor.call(this, "Activiti.component.FileChooserDialog", htmlId);

    // Create new service instances and set this component to receive the callbacks
    this.services.repositoryService = new Activiti.service.RepositoryService(this);

    this._dialog = {};
		this._treeView = {};
		this._currentNode = {};
		this._callbackFn = callbackFn;

    return this;
  };

  YAHOO.extend(Activiti.component.FileChooserDialog, Activiti.component.Base,
  {
	
		/**
		* Fired by YUI when parent element is available for scripting.
		* Template initialisation, including instantiation of YUI widgets and event listener binding.
		*
		* @method onReady
		*/
		onReady: function FileChooserDialog_onReady()
		{
		  
		  var content = document.createElement("div");
	    // TODO: i18n
      content.innerHTML = '<div class="bd"><h1>Select folder</h1><div id="fileChooserTree" class="ygtv-checkbox"/></div>';

      this._dialog = new YAHOO.widget.Dialog(content, {
        fixedcenter: true,
        visible: false,
        constraintoviewport: true,
        modal: true,
        buttons: [
          // TODO: i18n
          { text: "Select" , handler: { fn: this.onSubmit, scope: this }, isDefault:true },
          { text: "Cancel", handler: { fn: this.onCancel, scope: this } }
        ]
      });
		  
		  this._dialog.render(document.body);
		  
			// load the json representation of the tree, onLoadTreeSuccess will be called due to naming convention
			this.services.repositoryService.loadTree();
		
		},
		
		/**
     * Will display the tree
     *
     * @method onLoadTreeSuccess
     * @param response {object} The callback response
     * @param obj {object} Helper object
     */
    onLoadTreeSuccess: function FileChooserDialog_RepositoryService_onLoadTreeSuccess(response, obj)
    {
			var	me = this;
	
	    // Retrieve rest api response
      var treeNodesJson = response.json;
	    
	    var loadTreeNodes = function (node, fnLoadComplete) {
				if(node.data.file) {
					// Don't attempt to load child nodes for artifacts
					fnLoadComplete();
				} else {
					me.services.repositoryService.loadNodeData(node, fnLoadComplete);
					// TODO: see if there is a way to define a timeout even if the server returns a HTTP 500 status
					//timeout: 7000
				}
			};

			// instantiate the TreeView control
	   	this._treeView = new YAHOO.widget.TreeView("fileChooserTree", treeNodesJson);
      this._treeView.singleNodeHighlight = true;
      this._treeView.subscribe('clickEvent', this.onTreeNodeLabelClicked, this, true); 

			// set the callback function to dynamically load child nodes
			// set iconMode to 1 to use the leaf node icon when a node has no children. 
		  this._treeView.setDynamicLoad(loadTreeNodes, 1);
		  this._treeView.render();

      this._dialog.getButtons()[0].set("disabled", true);

		  this._dialog.show();
    },

		/**
     * TODO: documentation
		 * 
     */
		onLoadNodeDataSuccess: function FileChooserDialog_RepositoryService_onLoadNodeDataSuccess(response, obj)
    {
      // Retrieve rest api response
      var treeNodesJson = response.json;

			for(var i = 0; i<treeNodesJson.length; i++) {
			  if(treeNodesJson[i].folder) {
			    var node = new YAHOO.widget.TextNode(treeNodesJson[i], obj[0], treeNodesJson[i].expanded);
			  }
			}

			// call the fnLoadComplete function that the treeView component provides to 
			// indicate that the loading of the sub nodes was successfull.
			obj[1]();
    },
    
    /**
     * This method will be invoked when the label of a node in the tree is clicked. It first toggels the
     * disabled property of the select button and then propagates the click event to the 
     * "onEventToggleHighlight" method of the treeView component in order to toggle the node selection. 
     *
     * @method onTreeNodeLabelClicked
     * @param args {object} an object that contains the node that was clicked and the click event
     */
    onTreeNodeLabelClicked: function FileChooserDialog_onTreeNodeLabelClicked (args)
    {
      if(args.node.highlightState == 1) {
        // clear the current node and disable 'select' button
        this._currentNode = {};
        this._dialog.getButtons()[0].set("disabled", true);
      } else {
        // set current node and enable 'select' button
        this._currentNode = args.node;
        this._dialog.getButtons()[0].set("disabled", false);
      }
      this._treeView.onEventToggleHighlight(args);
      // Return false to prevent default for event. This causes the check-box to be checked 
      // without expanding the node when the label is clicked.
      return false;
    },

    onSubmit: function FileChooserDialog_onSubmit() {
      if(this._currentNode.data && this._currentNode.data.id && this._currentNode.label) {        
        this._callbackFn({"nodeId": this._currentNode.data.id, "nodeName": this._currentNode.label});
      } else {
        // TODO: handle error... should never happen due to validation, though...
      }
      if (this._dialog) {
        this._dialog.destroy();
      }
    },
    
    onCancel: function FileChooserDialog_onCancel() {
      if (this._dialog) {
        this._dialog.destroy();
      }
    }

	});

})();