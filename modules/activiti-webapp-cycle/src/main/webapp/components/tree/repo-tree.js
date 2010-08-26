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
				me.services.repositoryService.loadNodeData(node, fnLoadComplete);
				// TODO: see if there is a way to define a timeout even if the server returns a HTTP 500 status
				//timeout: 7000
			};

			// instantiate the TreeView control
	   	var tree = new YAHOO.widget.TreeView("treeDiv1", treeNodesJson);

			// set the callback function to dynamically load child nodes
			// set iconMode to 1 to use the leaf node icon when a node has no children. 
		  tree.setDynamicLoad(loadTreeNodes, 1);
		  tree.render();

			tree.subscribe("clickEvent", this.onLabelClick, null, this);
    },

		/**
     * TODO: doc
     */
		onLoadNodeDataSuccess: function RepoTree_RepositoryService_onLoadNodeDataSuccess(response, obj)
    {
      // Retrieve rest api response
      var treeNodesJson = response.json;

			for(var i = 0; i<treeNodesJson.length; i++) {
				new YAHOO.widget.TextNode(treeNodesJson[i], obj[0], treeNodesJson[i].expanded);
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
      this.fireEvent(Activiti.event.selectTreeLabel, node, null, false);
    }

	});

})();
