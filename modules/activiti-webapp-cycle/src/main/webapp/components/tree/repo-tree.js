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
	 * RepoTree constructor.
	 *
	 * @param {String} htmlId The HTML id of the parent element
	 * @return {Activiti.component.RepoTree} The new component.RepoTree instance
	 * @constructor
	 */
	Activiti.component.RepoTree = function RepoTree_constructor(htmlId)
  {
    Activiti.component.RepoTree.superclass.constructor.call(this, "Activiti.component.RepoTree", htmlId);

    // Create new service instances and set this component to receive the callbacks
    this.services.repositoryService = new Activiti.service.RepositoryService(this);

		// Listen for updateArtifactView event in order to be able to expand the tree up to the selected artifact
    this.onEvent(Activiti.event.updateArtifactView, this.onUpdateArtifactView);

		this._treeView = {};

    return this;
  };

  YAHOO.extend(Activiti.component.RepoTree, Activiti.component.Base,
  {
	
		/**
		* Fired by YUI when parent element is available for scripting.
		* Template initialisation, including instantiation of YUI widgets and event listener binding.
		*
		* @method onReady
		*/
		onReady: function RepoTree_onReady()
		{
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
    onLoadTreeSuccess: function RepoTree_RepositoryService_onLoadTreeSuccess(response)
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
	   	me._treeView = new YAHOO.widget.TreeView("treeDiv1", treeNodesJson);

			// set the callback function to dynamically load child nodes
			// set iconMode to 1 to use the leaf node icon when a node has no children. 
		  me._treeView.setDynamicLoad(loadTreeNodes, 1);
		  me._treeView.render();

			me._treeView.subscribe("clickEvent", this.onLabelClick, null, this);
			
			// me._treeView.subscribe("expand", this.onNodeExpand, null, this);
			// me._treeView.subscribe("collapse", this.onNodeCollapse, null, this);
    },

		/**
     * 
		 * 
     */
		onLoadNodeDataSuccess: function RepoTree_RepositoryService_onLoadNodeDataSuccess(response, obj)
    {
      // Retrieve rest api response
      var treeNodesJson = response.json;

      if(treeNodesJson) {
			  for(var i = 0; i<treeNodesJson.length; i++) {
				  var node = new YAHOO.widget.TextNode(treeNodesJson[i], obj[0], treeNodesJson[i].expanded);
				  if(treeNodesJson[i].contentType) {
					  if(treeNodesJson[i].contentType === "image/png" || treeNodesJson[i].contentType === "image/gif" || treeNodesJson[i].contentType === "image/jpeg") {
						  node.labelStyle = "icon-img";
					  } else if(treeNodesJson[i].contentType === "application/xml") {
						  node.labelStyle = "icon-code-red";
					  }	else if(treeNodesJson[i].contentType === "text/html") {
  					  node.labelStyle = "icon-www";
  				  } else if(treeNodesJson[i].contentType === "text/plain") {
						  node.labelStyle = "icon-txt";
					  }	else if(treeNodesJson[i].contentType === "application/pdf") {
						  node.labelStyle = "icon-pdf";
					  }	else if(treeNodesJson[i].contentType === "application/json;charset=UTF-8") {
						  node.labelStyle = "icon-code-blue";
					  }	else if(treeNodesJson[i].contentType === "application/msword") {
						  node.labelStyle = "icon-doc";
					  }	else if(treeNodesJson[i].contentType === "application/powerpoint") {
						  node.labelStyle = "icon-ppt";
					  }	else if(treeNodesJson[i].contentType === "application/excel") {
						  node.labelStyle = "icon-xls";
					  }	else if(treeNodesJson[i].contentType === "application/javascript") {
						  node.labelStyle = "icon-code-blue";
					  } else {
					    // Use white page as default icon for all other content types
					    node.labelStyle = "icon-blank";
					  }
				  }
			  }
		  }

			// call the fnLoadComplete function that the treeView component provides to 
			// indicate that the loading of the sub nodes was successfull.
			obj[1]();
    },

    // TODO: See how we can handle failures
    // onLoadNodeDataFailure: function RepoTree_RepositoryService_onLoadNodeDataFailure(response, obj)
    //     {
    //       
    //       
    //     },

		/**
     * Will fire a Activiti.event.selectTreeLabel event so other components may display the node
     *
     * @method onLabelClick
     * @param e {object} The click event
     */
    onLabelClick: function RepoTree_onLabelClick (event)
    {
	
			// Map the node properties to the event value object (value object property -> node property):
			// - repositoryNodeId -> node.data.id
			// - isRepositoryArtifact -> node.data.file
			// - name -> node.label

			this.fireEvent(Activiti.event.updateArtifactView, {"connectorId": event.node.data.connectorId, "repositoryNodeId": event.node.data.artifactId, "isRepositoryArtifact": event.node.data.file, "name": event.node.label, "activeTabIndex": 0}, null, true);
    },

		// onNodeExpand: function RepoTree_onNodeExpand (node)
		//    {
		// 
		//      // TODO
		//      // do the cookie processing to store the expand/collapse state of the tree
		// 
		//    },
		//    
		//    onNodeCollapse: function RepoTree_onNodeCollapse (node)
		//    {
		// 
		//      // TODO
		//      // do the cookie processing to store the expand/collapse state of the tree
		// 
		//    },

		onUpdateArtifactView: function Artifact_onUpdateArtifactView(event, args)
		{
			if(!this._treeView._nodes) {
				// tree is not yet initialized, we are coming from an external URL
        // TODO: load the tree up to the currently selected node
			} else {
				// tree is initialized, this is either a regular click on the tree or an event from the browser history manager
				var connectorId = args[1].value.connectorId;
				var repositoryNodeId = args[1].value.repositoryNodeId;
				var nodes = this._treeView.getNodesBy( function(node) {
				  if(node.data.connectorId && node.data.artifactId && node.data.connectorId === connectorId && node.data.artifactId === repositoryNodeId) {
				    return true;
				  }
				  return false;
				});
        var node = nodes[0];

				if(node && (node != this._treeView.currentFocus) ) {
				  // if the node isn't already focused this is a browser history event and we manually set focus to the current node
          node.focus();
				}
			}
		}

	});

})();
