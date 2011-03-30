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
     * Loads the tree
     *
     * @method loadTree
     */
    loadTree: function RepositoryService_loadTree(data)
    {
      this.jsonGet(this.loadTreeURL(data), null, "loadTree");
    },

    /**
     * Creates the GET url used to load the tree
     *
     * @method loadTreeURL
     * @return {string} The url
     */
    loadTreeURL: function RepositoryService_loadTreeURL(data)
    {
      var url = Activiti.service.REST_PROXY_URI_RELATIVE + "tree";
      if(data) {
        url += "?" + Activiti.service.Ajax.jsonToParamString(data);
      }
      return url;
    },

    /**
     * Loads the child nodes of a given node in the tree.
     *
     * @method loadChildNodes
     */
    loadChildNodes: function RepositoryService_loadChildNodes(data, node, fnLoadComplete)
    {
      var obj = {parentNode: node, fnLoadComplete: fnLoadComplete};
      this.jsonGet(this.loadChildNodesURL(data), obj, "loadChildNodes");
    },

    /**
     * Creates the GET url to load child nodes of a given tree node.
     *
     * @method loadTreeURL
     * @return {string} The url
     */
    loadChildNodesURL: function RepositoryService_loadChildNodesURL(data)
    {
      var url = Activiti.service.REST_PROXY_URI_RELATIVE + "child-nodes";
      if(data) {
        url += "?" + Activiti.service.Ajax.jsonToParamString(data);
      }
      return url;
    },

    /**
     * Loads an artifact (id and url)
     *
     * @method loadArtifact
     * @param artifactLiteral {Object} Object literal containing connectorId, nodeId and vFolderId
     */
    loadArtifact: function RepositoryService_loadArtifact(artifactLiteral)
    {
      this.jsonGet(this.loadArtifactURL(artifactLiteral), null, "loadArtifact");
    },

    /**
     * Creates the GET url to load an artifact
     *
     * @method loadArtifactURL
     * @param artifactLiteral {Object} Object literal containing connectorId, nodeId and vFolderId
     * @return {string} The url
     */
    loadArtifactURL: function RepositoryService_loadArtifactURL(artifactLiteral)
    {
      var url = Activiti.service.REST_PROXY_URI_RELATIVE + "artifact";
      if(artifactLiteral) {
        url += "?" + Activiti.service.Ajax.jsonToParamString(artifactLiteral);
      }
      return url;
    },

    /**
     * Loads artifact action forms.
     *
     * @method loadArtifactActionForm
     * @param artifactLiteral {Object} Object literal containing connectorId, nodeId, vFolderId and actionName
     */
    loadArtifactActionForm: function RepositoryService_loadArtifactActionForm(artifactLiteral)
    {
      this.jsonGet(this.loadArtifactActionFormURL(artifactLiteral), null, "loadArtifactActionForm");
    },

    /**
     * Composes the URL to load artifact action forms.
     *
     * @method loadArtifactActionFormURL
     * @param artifactLiteral {Object} Object literal containing connectorId, nodeId, vFolderId and actionName
     * @return {string} The URL
     */
    loadArtifactActionFormURL: function RepositoryService_loadArtifactActionFormURL(artifactLiteral)
    {
      var url = Activiti.service.REST_PROXY_URI_RELATIVE + "artifact-action-form";
      if(artifactLiteral) {
        url += "?" + Activiti.service.Ajax.jsonToParamString(artifactLiteral);
      }
      return url;
    },

    /**
     * Sends the form fields of an artifact action form to the server using HTTP PUT.
     *
     * @method executeArtifactAction
     * @param actionLiteral {Object} Object literal containing the connectorId, nodeId, vFolderId of the node 
     *        the action is to be executed on and actionName (the name of the action)
     * @param variables {Object} Object literal containing the generic form variables
     */
    executeArtifactAction: function RepositoryService_executeArtifactAction(actionLiteral, variables, obj)
    {
      this.jsonPut(this.executeArtifactFormURL(actionLiteral), variables, obj, "executeArtifactAction");
    },

    /**
     * Creates the HTTP PUT URL to execute artifact actions.
     *
     * @method executeArtifactFormURL
     * @param actionLiteral {Object} Object literal containing the connectorId, nodeId, vFolderId of the node 
     *        the action is to be executed on and actionName (the name of the action)
     * @return The HTTP PUT URL to execute artifact actions
     */
    executeArtifactFormURL: function RepositoryService_executeArtifactFormURL(actionLiteral)
    {
      var url = Activiti.service.REST_PROXY_URI_RELATIVE + "artifact-action";
      if(actionLiteral) {
        url += "?" + Activiti.service.Ajax.jsonToParamString(actionLiteral);
      }
      return url;
    },

    createArtifact: function RepositoryService_createArtifact(artifactLiteral)
    {
      this.jsonPost(Activiti.service.REST_PROXY_URI_RELATIVE + 'artifact', artifactLiteral, null, "createArtifact");
    },

    /**
     * Creates a new artifactLink by posting the provided artifactLinkLiteral parameter to the
     * REST API. The 'artifactLinkLiteral' is an object literal that should contain the following
     * values: "connectorId", "nodeId", "targetConnectorId", "targetNodeId"
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
    },
    
    deleteArtifactLink: function RepositoryService_deleteArtifactLink(linkId) {
      this.jsonDelete(this.deleteArtifactLinkURL(linkId), null, "deleteArtifactLink");
    },
    
    deleteArtifactLinkURL: function RepositoryService_deleteArtifactLinkURL(linkId) {
      return Activiti.service.REST_PROXY_URI_RELATIVE + "artifact-link?linkId=" + linkId;
    },

    /**
     * Returns the url to load the links for a given artifact from the server
     * 
     * @method loadArtifactLinksURL
     * @param {Object} eventValue an object of URL parameters
     * @return {string} the URL
     */
    loadArtifactLinksURL: function RepositoryService_loadArtifactLinksURL(eventValue) {
      var url = Activiti.service.REST_PROXY_URI_RELATIVE + "artifact-links", 
      params = Activiti.util.objectToArgumentString(eventValue);
      return (params) ? url + "?" + params : url;
    },
    
    /**
     * Returns the url to load the links for a given artifact from the server
     * 
     * @method loadIncomingArtifactLinksURL
     * @param {Object} eventValue an object of URL parameters
     * @return {string} the URL
     */
    loadIncomingArtifactLinksURL: function RepositoryService_loadIncomingArtifactLinksURL(eventValue) {
      var url = Activiti.service.REST_PROXY_URI_RELATIVE + "incoming-artifact-links",
      params = Activiti.util.objectToArgumentString(eventValue);
      return (params) ? url + "?" + params : url;
    },

    /**
     * Creates a new folder by posting the provided object literal parameter to the
     * REST API. The 'folderLiteral' is an object literal that should contain the following
     * values: "connectorId", "parentFolderId", "name"
     *
     * @method createFolder
     * @param folderLiteral object literal with the values of the folder to be created
     */
    createFolder: function RepositoryService_createFolder(folderLiteral) {
      this.jsonPost(this.createFolderURL(), folderLiteral, null, "createFolder");
    },

    /**
     * Creates the POST url to use when creating a folder
     *
     * @method createFolderURL
     * @return {string} The url
     */
    createFolderURL: function RepositoryService_createFolderURL() {
      return Activiti.service.REST_PROXY_URI_RELATIVE + "folder";
    },
    
    createTag: function RepositoryService_createTag(tagLiteral) {
      this.jsonPost(this.createTagUrl(), tagLiteral, null, "createTag");
    },
    
    createTagUrl: function RepositoryService_createTagUrl(connectorId, nodeId, tag) {
      return Activiti.service.REST_PROXY_URI_RELATIVE + "tag";
    },
    
    loadTagsByNode: function RepositoryService_loadTagsByNode(connectorId, nodeId) {
      this.jsonGet(this.loadTagsByNodeUrl(connectorId, nodeId), null, "loadTagsByNode");
    },
    
    loadTagsByNodeUrl: function RepositoryService_loadTagsByNodeUrl(connectorId, nodeId) {
      return Activiti.service.REST_PROXY_URI_RELATIVE + "tags?connectorId=" + encodeURIComponent(connectorId) + "&nodeId=" + encodeURIComponent(nodeId);
    },
    
    saveTags: function RepositoryService_saveTags(tagsLiteral) {
      this.jsonPost(this.saveTagsUrl(), tagsLiteral, null, "saveTags");
    },
    
    saveTagsUrl: function RepositoryService_saveTags() {
      return Activiti.service.REST_PROXY_URI_RELATIVE + "tags";
    },

    deleteTag: function RepositoryService_deleteTag(tagLiteral) {
      this.jsonDelete(this.deleteTagUrl(tagLiteral), null, "deleteTag");
    },
    
    deleteTagUrl: function RepositoryService_deleteTagUrl(tagLiteral) {
      return Activiti.service.REST_PROXY_URI_RELATIVE + "tag?connectorId=" + encodeURIComponent(tagLiteral.connectorId) + "&nodeId=" + encodeURIComponent(tagLiteral.nodeId) + "&tagName=" + encodeURIComponent(tagLiteral.tagName);
    },
    
    loadUserConfig: function RepsositoryService_loadUserConfig() {
      this.jsonGet(Activiti.service.REST_PROXY_URI_RELATIVE + "user-config", null, "loadUserConfig");
    },
    
    loadAvailableConnectorConfigs: function RepositoryService_loadAvailableConnectorConfigs() {
      this.jsonGet(Activiti.service.REST_PROXY_URI_RELATIVE + "available-connector-configs", null, "loadAvailableConnectorConfigs");
    },
    
    saveRepositoryConnectorConfiguration: function RepositoryService_saveRepositoryConnectorConfiguration(configuration) {
      this.jsonPost(Activiti.service.REST_PROXY_URI_RELATIVE + "user-config", configuration, null, "saveRepositoryConnectorConfiguration");
    },
    
    // {connectorId: "...", nodeId: "..."}
    loadComments: function RepositoryService_loadComments(obj) {
      this.jsonGet(this.loadCommentsURL(obj), null, "loadComments");
    },
    
    loadCommentsURL: function RepositoryService_loadCommentsURL(obj) {
      var url = Activiti.service.REST_PROXY_URI_RELATIVE + "comment",
      params = Activiti.util.objectToArgumentString(obj);
      return (params) ? url + "?" + params : url;
    },

    // {connectorId: "...", nodeId: "...", content: "..."}  
    saveComment: function RepositoryService_saveComment(obj) {
      this.jsonPost(Activiti.service.REST_PROXY_URI_RELATIVE + "comment", obj, null, "saveComment");
    },
    
    createProcessSolution: function RepositoryService_saveComment(obj) {
      this.jsonPost(Activiti.service.REST_PROXY_URI_RELATIVE + "process-solution", obj, null, "createProcessSolution");
    },

    loadProcessSolution: function RepositoryService_loadProcessSolution(processSolutionLiteral)
    {
      var url = Activiti.service.REST_PROXY_URI_RELATIVE + "process-solution",
      params = Activiti.util.objectToArgumentString(processSolutionLiteral);
      url = (params) ? url + "?" + params : url;
      this.jsonGet(url, null, "loadProcessSolution");
    },
    
    updateProcessSolution: function RepositoryService_updateProcessSolution(obj)
    {
      this.jsonPut(Activiti.service.REST_PROXY_URI_RELATIVE + "process-solution", obj, null, "updateProcessSolution");
    }

  });
})();