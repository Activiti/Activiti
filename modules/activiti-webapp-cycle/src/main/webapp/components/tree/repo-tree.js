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
    onLoadTreeSuccess: function RepoTree_RepositoryService_onLoadTreeSuccess(response, obj)
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
			
			me._treeView.subscribe("expand", this.onNodeExpand, null, this);
			me._treeView.subscribe("collapse", this.onNodeCollapse, null, this);
    },

		/**
     * 
		 * 
     */
		onLoadNodeDataSuccess: function RepoTree_RepositoryService_onLoadNodeDataSuccess(response, obj)
    {
      // Retrieve rest api response
      var treeNodesJson = response.json;

			for(var i = 0; i<treeNodesJson.length; i++) {
				var node = new YAHOO.widget.TextNode(treeNodesJson[i], obj[0], treeNodesJson[i].expanded);
				if(treeNodesJson[i].contentType) {
					if(treeNodesJson[i].contentType === "image/png" || treeNodesJson[i].contentType === "image/gif" || treeNodesJson[i].contentType === "image/jpeg") {
						node.labelStyle = "icon-img";
					} else if(treeNodesJson[i].contentType === "application/xml") {
						node.labelStyle = "icon-code-red";
					}	else if(treeNodesJson[i].contentType === "text/plain") {
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
					}		else if(treeNodesJson[i].contentType === "application/javascript") {
						node.labelStyle = "icon-code-blue";
					}
				}
			}

			// call the fnLoadComplete function that the treeView component provides to 
			// indicate that the loading of the sub nodes was successfull.
			obj[1]();
    },

		/**
     * Will fire a Activiti.event.selectTreeLabel event so other components may display the node
     *
     * @method onLabelClick
     * @param e {object} The click event
     */
    onLabelClick: function RepoTree_onLabelClick (node)
    {
	
			// Map the node properties to the event value object (value object property -> node property):
			// - repositoryNodeId -> node.data.id
			// - isRepositoryArtifact -> node.data.file
			// - name -> node.label

			this.fireEvent(Activiti.event.updateArtifactView, {"repositoryNodeId": node.node.data.id, "isRepositoryArtifact": node.node.data.file, "name": node.node.label, "activeTabIndex": 0}, null, true);
    },

		onNodeExpand: function RepoTree_onNodeExpand (node)
		{

			// TODO
			// do the cookie processing to store the expand/collapse state of the tree

		},
		
		onNodeCollapse: function RepoTree_onNodeCollapse (node)
		{

			// TODO
			// do the cookie processing to store the expand/collapse state of the tree

		},

		onUpdateArtifactView: function Artifact_onUpdateArtifactView(event, args)
		{
			if(!this._treeView._nodes) {
				// tree is not initialized yet, we are coming from an external URL
				
			} else {
				// Check, whether the tree contains the node that was selected, otherwise we will have to request it from the REST API
				var nodeExists = false;
				for(var i=0; i<this._treeView._nodes.length; i++) {
					if(this._treeView._nodes && this._treeView._nodes[i] && (this._treeView._nodes[i].data.id === args[1].value.repositoryNodeId) ) {
						nodeExists = true;
					}
				}
				//alert("Node is there:" + nodeExists);
			}
		}

	});

})();
