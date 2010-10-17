/**
 * CYCLE SERVICES
 */

/**
 * Activiti RepositoryService.
 *
 * @namespace Activiti.service
 * @class Activiti.service.RepositoryService
 */
(function()
{
	var that = this;

  /**
   * RepositoryService constructor.
   *
   * @parameter handler {object} The response handler object
   * @return {Activiti.service.RepositoryService} The new Activiti.service.RepositoryService instance
   * @constructor
   */
  Activiti.service.RepositoryService = function RepositoryService_constructor(callbackHandler)
  {
		Activiti.service.RepositoryService.superclass.constructor.call(this, "Activiti.service.RepositoryService", callbackHandler);
    that = this;
		return this;
  };

  /**
   * Event constants
   */
   YAHOO.lang.augmentObject(Activiti.service.ManagementService,
   {
     event: {
       loadTree: "loadTree",
     }
   });

  YAHOO.extend(Activiti.service.RepositoryService, Activiti.service.RestService,
  {

    /**
     * Loads the repository tree
     *
     * @method loadTree
     * @param obj {Object} Helper object to be sent to the callback
     */
    loadTree: function RepositoryService_loadTree(obj)
    {
      this.jsonGet(this.loadTreeURL(), obj, "loadTree");
    },

    /**
     * Creates the GET url used to load the tree
     *
     * @method loadTreeURL
     * @return {string} The url
     */
    loadTreeURL: function RepositoryService_loadTreeURL()
    {
      return Activiti.service.REST_PROXY_URI_RELATIVE + "repo-tree?id=/&folder=true";
    },

		/**
		 * TODO: document it.. Also see dynamicLoad in repo-tree.js
		 *
		 */
		loadNodeData: function RepositoryService_loadNodeData(node, fnLoadComplete)
		{
			var obj = [node, fnLoadComplete];
			this.jsonGet(this.loadNodeURL(node.data.id, node.data.folder), obj, "loadNodeData");
	  },

		/**
		 * TODO: doc
     * Creates the GET url used to load the tree
     *
     * @method loadTreeURL
     * @return {string} The url
     */
    loadNodeURL: function RepositoryService_loadNodeURL(nodeid, folder)
    {
      return Activiti.service.REST_PROXY_URI_RELATIVE + "repo-tree?id=" + encodeURIComponent(nodeid) + "&folder=" + folder;
    },

		/**
     * Loads an artifact (id and url)
     *
     * @method loadArtifact
     * @param artifactid {string} The id of the artifact to be loaded
     * @param obj {Object} Helper object to be sent to the callback
     */
    loadArtifact: function RepositoryService_loadArtifact(artifactid, obj)
    {
      this.jsonGet(this.loadArtifactURL(artifactid), obj, "loadArtifact");
    },

    /**
     * Creates the GET url used to load the artifact
     *
     * @method loadArtifactURL
		 * @param artifactid {string} The id of the artifact
     * @return {string} The url
     */
    loadArtifactURL: function RepositoryService_loadArtifactURL(artifactid)
    {
      return Activiti.service.REST_PROXY_URI_RELATIVE + "artifact?artifactId=" + encodeURIComponent(artifactid) + "&restProxyUri=" + encodeURIComponent(Activiti.service.REST_PROXY_URI_RELATIVE);
    },

		// TODO: doc
    loadArtifactActionForm: function RepositoryService_loadArtifactActionForm(artifactId, artifactActionName, obj)
    {
		  this.jsonGet(this.loadArtifactActionFormURL(artifactId, artifactActionName), obj, "loadArtifactActionForm");
    },

		// TODO: doc
    loadArtifactActionFormURL: function RepositoryService_loadArtifactActionFormURL(artifactId, artifactActionName)
    {
			return Activiti.service.REST_PROXY_URI_RELATIVE + "artifact-action-form?artifactId=" + encodeURIComponent(artifactId) + "&actionName=" + encodeURIComponent(artifactActionName);
    },

		executeArtifactAction: function RepositoryService_executeArtifactAction(artifactId, artifactActionName, variables, obj)
		{
			this.jsonPut(this.executeArtifactFormURL(artifactId, artifactActionName), variables, obj, "executeArtifactAction");
		},

		executeArtifactFormURL: function RepositoryService_executeArtifactFormURL(artifactId, artifactActionName)
		{
			return Activiti.service.REST_PROXY_URI_RELATIVE + "artifact-action?artifactId=" + encodeURIComponent(artifactId) + "&actionName=" + encodeURIComponent(artifactActionName);
		}

  });
})();


/**
 * CYCLE WIDGETS
 */


/**
 * Activiti ExecuteArtifactActionForm.
 *
 * @namespace Activiti.widget
 * @class Activiti.widget.ExecuteArtifactActionForm
 */
(function()
{

  /**
   * ExecuteArtifactActionForm constructor.
   *
	 * @param id {string} The components id
   * @parameter handler {object} The response handler object
   * @return {Activiti.widget.ExecuteArtifactActionForm} The new Activiti.widget.ExecuteArtifactActionForm instance
   * @constructor
   */
  Activiti.widget.ExecuteArtifactActionForm = function ExecuteArtifactActionForm_constructor(id, artifactId, artifactActionName)
  {
    Activiti.widget.ExecuteArtifactActionForm.superclass.constructor.call(this, id);
    this.artifactId = artifactId;
		this.artifactActionName = artifactActionName;
    this.service = new Activiti.service.RepositoryService(this);
    this.service.setCallback("loadArtifactActionForm", { fn: this.onLoadFormSuccess, scope: this }, {fn: this.onLoadFormFailure, scope: this });
    this.service.loadArtifactActionForm(this.artifactId, this.artifactActionName);
    return this;
  };

  YAHOO.extend(Activiti.widget.ExecuteArtifactActionForm, Activiti.widget.Form,
  {

     /**
     * Start a process instance
     *
     * @method doSubmit
     */
    doSubmit: function ExecuteArtifactActionForm__doSubmit(variables)
    {
      this.service.executeArtifactAction(this.artifactId, this.artifactActionName, variables);
    }

  });

})();
