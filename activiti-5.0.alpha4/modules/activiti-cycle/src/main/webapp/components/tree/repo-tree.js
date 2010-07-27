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
      // Retrieve rest api response
      var treeNodesJson = response.json;

			// instantiate the TreeView control
	   	var tree = new YAHOO.widget.TreeView("treeDiv1", treeNodesJson);
		  tree.render();

			tree.subscribe("clickEvent", this.onLabelClick, null, this);
			
			//Event.addListener(document.getElementById("left"), "click", this.onDivClick, null, this);
			
			//var oElement = document.getElementById("elementid");
			//function fnCallback(e) { alert("click"); }
			//YAHOO.util.Event.addListener(oElement, "click", fnCallback);
			

    },

		//onDivClick: function RepoTree_onDivClick (e, el)
    //{
    //  this.fireEvent("Activiti.event.selectDiv", el, e, true);
    //},

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





















































































