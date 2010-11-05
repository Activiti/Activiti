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
	 * CreateFolderDialog constructor.
	 *
	 * @param htmlId {String} The HTML id of the parent element
	 * @param connectorId {String} The id of the connector the artifact should be created in
	 * @param parentFolderId The id of the folder the artifact should be created in
	 * @return {Activiti.component.CreateArtifactDialog} The new component.CreateFolderDialog instance
	 * @constructor
	 */
	Activiti.component.CreateFolderDialog = function CreateFolderDialog_constructor(htmlId, connectorId, parentFolderId)
  {
    Activiti.component.CreateFolderDialog.superclass.constructor.call(this, "Activiti.component.CreateFolderDialog", htmlId);

    this.service = new Activiti.service.RepositoryService(this);

    this._dialog = {};
		this._connectorId = connectorId;
		this._parentFolderId = parentFolderId;

    return this;
  };

  YAHOO.extend(Activiti.component.CreateFolderDialog, Activiti.component.Base,
  {
	
		/**
		* Fired by YUI when parent element is available for scripting.
		* Template initialisation, including instantiation of YUI widgets and event listener binding.
		*
		* @method onReady
		*/
		onReady: function CreateFolderDialog_onReady()
		{
		  var content = document.createElement("div");

	    // TODO: i18n

	    // TODO: switch to rest proxy URL (Activiti.service.REST_PROXY_URI_RELATIVE), when using this URL at the moment, it seems to be redirecting to the GET URL... Find out what goes wrong here.

      content.innerHTML = '<div class="bd"><form id="' + this.id + '-artifact-upload-form" action="http://localhost:8080/activiti-rest/service/folder" method="POST" enctype="multipart/form-data" accept-charset="utf-8"><h1>Create new folder</h1><table><tr><td><label>Name:<br/><input type="text" name="name" value="" /></label><br/></td></tr></table><input type="hidden" name="connectorId" value="' + this._connectorId + '" /><input type="hidden" name="parentFolderId" value="' + this._parentFolderId + '" /></form></div>';

      this._dialog = new YAHOO.widget.Dialog(content, {
        fixedcenter: true,
        visible: false,
        constraintoviewport: true,
        modal: true,
        buttons: [
          // TODO: i18n
          { text: "Create" , handler: { fn: this.onSubmit, scope: this }, isDefault:true },
          { text: "Cancel", handler: { fn: this.onCancel, scope: this } }
        ]
      });

      this._dialog.callback.success = this.onSuccess;
      this._dialog.callback.failure = this.onFailure;

		  this._dialog.render(document.body);

      // TODO: validation

      // this._dialog.getButtons()[0].set("disabled", true);
		  this._dialog.show();
		},

    onSubmit: function CreateFolderDialog_onSubmit(event, dialog) {
      this.service.createFolder(dialog.getData());
      if (this._dialog) {
        this._dialog.destroy();
      }
    },

    onCancel: function CreateFolderDialog_onCancel() {
      this._dialog.cancel();
    },

    onSuccess: function CreateFolderDialog_onSuccess(o) {
      // TODO: fire an event for e.g. the tree to reload it's nodes etc.
      // TODO: i18n
      if(o.json.success) {
        Activiti.widget.PopupManager.displayMessage({
          text: "Successfully created artifact"
        });
      } else {
        Activiti.widget.PopupManager.displayError("Error creating artifact", "Unable to create artifact");
      }
    },

    onFailure: function CreateFolderDialog_onFailure(o) {
      // TODO: i18n
      Activiti.widget.PopupManager.displayError("Connection Error", "Unable to create folder. Check your internet connection and make sure the Activiti server can be reached.");
    }

	});

})();