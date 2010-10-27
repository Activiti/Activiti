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
       loadTree: "loadTree"
     }
   });

  YAHOO.extend(Activiti.service.RepositoryService, Activiti.service.RestService,
  {

    /**
     * Loads the repository tree
     *
     * @method loadTree
     */
    loadTree: function RepositoryService_loadTree()
    {
      this.jsonGet(this.loadTreeURL(), null, "loadTree");
    },

    /**
     * Creates the GET url used to load the tree
     *
     * @method loadTreeURL
     * @return {string} The url
     */
    loadTreeURL: function RepositoryService_loadTreeURL()
    {
      return Activiti.service.REST_PROXY_URI_RELATIVE + "child-nodes?connectorId=/&artifactId=''";
    },

    /**
     * TODO: document it.. Also see dynamicLoad in repo-tree.js
     *
     */
    loadNodeData: function RepositoryService_loadNodeData(node, fnLoadComplete)
    {
      var obj = [node, fnLoadComplete];
      this.jsonGet(this.loadNodeURL(node.data.connectorId, node.data.artifactId), obj, "loadNodeData");
    },

    /**
     * TODO: doc
     * Creates the GET url used to load the tree
     *
     * @method loadTreeURL
     * @return {string} The url
     */
    loadNodeURL: function RepositoryService_loadNodeURL(connectorId, nodeid)
    {
      return Activiti.service.REST_PROXY_URI_RELATIVE + "child-nodes?connectorId=" + encodeURIComponent(connectorId) + "&artifactId=" + encodeURIComponent(nodeid);
    },

    /**
     * Loads an artifact (id and url)
     *
     * @method loadArtifact
     * @param artifactid {string} The id of the artifact to be loaded
     * @param obj {Object} Helper object to be sent to the callback
     */
    loadArtifact: function RepositoryService_loadArtifact(connectorId, artifactid, obj)
    {
      this.jsonGet(this.loadArtifactURL(connectorId, artifactid), obj, "loadArtifact");
    },

    /**
     * Creates the GET url used to load the artifact
     *
     * @method loadArtifactURL
     * @param artifactid {string} The id of the artifact
     * @return {string} The url
     */
    loadArtifactURL: function RepositoryService_loadArtifactURL(connectorId, artifactid)
    {
      return Activiti.service.REST_PROXY_URI_RELATIVE + "artifact?connectorId=" + encodeURIComponent(connectorId) + "&artifactId=" + encodeURIComponent(artifactid) + "&restProxyUri=" + encodeURIComponent(Activiti.service.REST_PROXY_URI_RELATIVE);
    },

    // TODO: doc
    loadArtifactActionForm: function RepositoryService_loadArtifactActionForm(connectorId, artifactId, artifactActionName, obj)
    {
      this.jsonGet(this.loadArtifactActionFormURL(connectorId, artifactId, artifactActionName), obj, "loadArtifactActionForm");
    },

    // TODO: doc
    loadArtifactActionFormURL: function RepositoryService_loadArtifactActionFormURL(connectorId, artifactId, artifactActionName)
    {
      return Activiti.service.REST_PROXY_URI_RELATIVE + "artifact-action-form?connectorId=" + encodeURIComponent(connectorId) + "&artifactId=" + encodeURIComponent(artifactId) + "&actionName=" + encodeURIComponent(artifactActionName);
    },

    executeArtifactAction: function RepositoryService_executeArtifactAction(connectorId, artifactId, artifactActionName, variables, obj)
    {
      this.jsonPut(this.executeArtifactFormURL(connectorId, artifactId, artifactActionName), variables, obj, "executeArtifactAction");
    },

    executeArtifactFormURL: function RepositoryService_executeArtifactFormURL(connectorId, artifactId, artifactActionName)
    {
      return Activiti.service.REST_PROXY_URI_RELATIVE + "artifact-action?connectorId=" + encodeURIComponent(connectorId) + "&artifactId=" + encodeURIComponent(artifactId) + "&actionName=" + encodeURIComponent(artifactActionName);
    },

    /**
     * Creates a new artifactLink by posting the provided artifactLinkLiteral parameter to the
     * REST API. The 'artifactLinkLiteral' is an object literal that should contain the following
     * values: "connectorId", "artifactId", "targetConnectorId", "targetArtifactId"
     *
     * @param artifactLinkLiteral object literal with the values of the link to be created
     */
    createArtifactLink: function RepositoryService_createArtifactLink(artifactLinkLiteral) {
      this.jsonPost(this.createArtifactLinkURL(), artifactLinkLiteral, null, "createArtifactLink");
    },

    /**
     * Creates the POST url to use when creating an artifact-link
     *
     * @method createArtifactLinkURL
     * @return {string} The url
     */
    createArtifactLinkURL: function RepositoryService_createArtifactLinkURL() {
      return Activiti.service.REST_PROXY_URI_RELATIVE + "artifact-link";
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
  Activiti.widget.ExecuteArtifactActionForm = function ExecuteArtifactActionForm_constructor(id, connectorId, artifactId, artifactActionName)
  {
    Activiti.widget.ExecuteArtifactActionForm.superclass.constructor.call(this, id);
    this.connectorId = connectorId;
    this.artifactId = artifactId;
    this.artifactActionName = artifactActionName;
    this.service = new Activiti.service.RepositoryService(this);
    this.service.setCallback("loadArtifactActionForm", { fn: this.onLoadFormSuccess, scope: this }, {fn: this.onLoadFormFailure, scope: this });
    this.service.loadArtifactActionForm(this.connectorId, this.artifactId, this.artifactActionName);
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
      this.service.executeArtifactAction(this.connectorId, this.artifactId, this.artifactActionName, variables);
    }

  });

})();
