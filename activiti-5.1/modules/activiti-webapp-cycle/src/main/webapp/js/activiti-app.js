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
      var url = Activiti.service.REST_PROXY_URI_RELATIVE + "child-nodes?connectorId=/&artifactId=''";
      if(data) {
        url += "&" + Activiti.service.Ajax.jsonToParamString(data);
      }
      return url;
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
    loadArtifact: function RepositoryService_loadArtifact(connectorId, artifactid)
    {
      this.jsonGet(this.loadArtifactURL(connectorId, artifactid), null, "loadArtifact");
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
    
    createTagUrl: function RepositoryService_createTagUrl(connectorId, artifactId, tag) {
      return Activiti.service.REST_PROXY_URI_RELATIVE + "tag";
    },
    
    loadTagsByRepositoryNode: function RepositoryService_loadTagsByRepositoryNode(connectorId, repositoryNodeId) {
      this.jsonGet(this.loadTagsByRepositoryNodeUrl(connectorId, repositoryNodeId), null, "loadTagsByRepositoryNode");
    },
    
    loadTagsByRepositoryNodeUrl: function RepositoryService_loadTagsByRepositoryNodeUrl(connectorId, repositoryNodeId) {
      return Activiti.service.REST_PROXY_URI_RELATIVE + "tags?connectorId=" + encodeURIComponent(connectorId) + "&repositoryNodeId=" + encodeURIComponent(repositoryNodeId);
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
      return Activiti.service.REST_PROXY_URI_RELATIVE + "tag?connectorId=" + encodeURIComponent(tagLiteral.connectorId) + "&repositoryNodeId=" + encodeURIComponent(tagLiteral.repositoryNodeId) + "&tagName=" + encodeURIComponent(tagLiteral.tagName);
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
    }

  });
})();


/**
 * CYCLE WIDGETS
 */


/**
 * Activiti Cycle ExecuteArtifactActionForm.
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
   * @param connectorId {string} the connector id of the artifact
   * @param artifactId {string} the id of the artifact
   * @param artifactActionName {string} the name of the action
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

    this.waitDialog = 
    		new YAHOO.widget.Panel("wait",  
    			{ width:"200px", 
    			  fixedcenter:true, 
    			  close:false, 
    			  draggable:false, 
    			  zindex:4,
    			  modal:true,
    			  visible:false
    			} 
    		);

    this.waitDialog.setBody('<div id="action-waiting-dialog"/>');
    this.waitDialog.render(document.body);
    
    return this;
  };

  YAHOO.extend(Activiti.widget.ExecuteArtifactActionForm, Activiti.widget.Form,
  {

    /**
     * Submit the form
     *
     * @method doSubmit
     */
    doSubmit: function ExecuteArtifactActionForm__doSubmit(variables)
    {
      this.waitDialog.show();
      this.service.executeArtifactAction(this.connectorId, this.artifactId, this.artifactActionName, variables);
      if (this.dialog) {
        this.dialog.destroy();
      }
    },
    
    onExecuteArtifactActionSuccess: function ExecuteArtifactActionForm_onExecuteArtifactActionSuccess(response, object) {
      this.waitDialog.hide();
      // TODO: i18n
      Activiti.widget.PopupManager.displayMessage({
        text: 'Successfully executed "' + this.artifactActionName + '"'
      });
    },
    
    onExecuteArtifactActionFailure: function ExecuteArtifactActionForm_onExecuteArtifactActionFailure(response, object) {
      this.waitDialog.hide();
    }

  });

})();

/**
 * Activiti Cycle FileChooserDialog constructor.
 *
 * @namespace Activiti.widget
 * @class Activiti.component.FileChooserDialog
 */
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
	 * @return {Activiti.component.FileChooserDialog} The new Activiti.component.FileChooserDialog instance
	 * @constructor
	 */
	Activiti.component.FileChooserDialog = function FileChooserDialog_constructor(htmlId, callbackFn, showFiles, scope, highlightFolders, highlightFiles)
  {
    Activiti.component.FileChooserDialog.superclass.constructor.call(this, "Activiti.component.FileChooserDialog", htmlId);

    // Create new service instances and set this component to receive the callbacks
    this.services.repositoryService = new Activiti.service.RepositoryService(this);

    this._dialog = {};
		this._treeView = {};
		this._currentNode = {};
		this._callbackFn = callbackFn;
		this._showFiles = showFiles;
    this._scope = scope;
    this._highlightFolders = highlightFolders;
    this._highlightFiles = highlightFiles;

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
	    // TODO: make heading depend on configuration: "folder/ file" etc. ...
      content.innerHTML = '<div class="bd"><h1>Select ' + (this._highlightFolders ? 'folder' : 'file') + '</h1><div id="fileChooserTree" class="ygtv-checkbox"/></div>';

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
      
      for(var nodeIndex in this._treeView._nodes) {
        var node = this._treeView.getNodeByIndex(nodeIndex);
        if(node.data.folder) {
          node.enableHighlight = this._highlightFolders;
        } else if (node.data.file) {
          node.enableHighlight = this._highlightFiles;
        }
      }

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
			  if( treeNodesJson[i].folder ) {
			    var node = new YAHOO.widget.TextNode(treeNodesJson[i], obj[0], false);
			    node.enableHighlight = this._highlightFolders;
			  } else if( this._showFiles && treeNodesJson[i].file ) {
			    var node = new YAHOO.widget.TextNode(treeNodesJson[i], obj[0], true);
			    if(treeNodesJson[i].contentType) {
            if(treeNodesJson[i].contentType === "image/png" || treeNodesJson[i].contentType === "image/gif" || treeNodesJson[i].contentType === "image/jpeg") {
              node.labelStyle = "icon-img";
            } else if(treeNodesJson[i].contentType === "application/xml") {
              node.labelStyle = "icon-code-red";
            } else if(treeNodesJson[i].contentType === "text/html") {
              node.labelStyle = "icon-www";
            } else if(treeNodesJson[i].contentType === "text/plain") {
              node.labelStyle = "icon-txt";
            } else if(treeNodesJson[i].contentType === "application/pdf") {
              node.labelStyle = "icon-pdf";
            } else if(treeNodesJson[i].contentType === "application/json;charset=UTF-8") {
              node.labelStyle = "icon-code-blue";
            } else if(treeNodesJson[i].contentType === "application/msword") {
              node.labelStyle = "icon-doc";
            } else if(treeNodesJson[i].contentType === "application/powerpoint") {
              node.labelStyle = "icon-ppt";
            } else if(treeNodesJson[i].contentType === "application/excel") {
              node.labelStyle = "icon-xls";
            } else if(treeNodesJson[i].contentType === "application/javascript") {
              node.labelStyle = "icon-code-blue";
            } else {
              // Use white page as default icon for all other content types
              node.labelStyle = "icon-blank";
            }
          }
          node.enableHighlight = this._highlightFiles;
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
      } else if( (args.node.data.file && this._highlightFiles) || (args.node.data.folder && this._highlightFolders) ) {
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
      if(this._currentNode.data && this._currentNode.data.connectorId && this._currentNode.data.artifactId && this._currentNode.label) {        
        
        if(this._scope) {
          this._scope[this._callbackFn]({"connectorId": this._currentNode.data.connectorId, "nodeId": this._currentNode.data.artifactId, "nodeName": this._currentNode.label});
        } else {
          this._callbackFn({"connectorId": this._currentNode.data.connectorId, "nodeId": this._currentNode.data.artifactId, "nodeName": this._currentNode.label});
        }
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
	 * TaggingComponent constructor.
	 *
	 * @param htmlId {String} The HTML id of the parent element
	 * @param repositoryNodeLiteral An object literal that contains the connectorId, the repositoryNodeId and the repositoryNodeLabel of the repository node to be tagged
	 * @param elId The id of the Dom element that the tagging-component should be appended to
	 * @return {Activiti.component.TaggingComponent} The new component.TaggingComponent instance
	 * @constructor
	 */
	Activiti.component.TaggingComponent = function TaggingComponent_constructor(htmlId, repositoryNodeLiteral, elId)
  {
    Activiti.component.TaggingComponent.superclass.constructor.call(this, "Activiti.component.TaggingComponent", htmlId);

    this.service = new Activiti.service.RepositoryService(this);

    this._repositoryNode = repositoryNodeLiteral;
    this._el = document.getElementById(elId);
    this._tags = [];

    return this;
  };

  YAHOO.extend(Activiti.component.TaggingComponent, Activiti.component.Base,
  {
	
		/**
		* Fired by YUI when parent element is available for scripting.
		* Template initialisation, including instantiation of YUI widgets and event listener binding.
		*
		* @method onReady
		*/
		onReady: function TagThisDialog_onReady()
		{
		  this.service.loadTagsByRepositoryNode(this._repositoryNode.connectorId, this._repositoryNode.repositoryNodeId);
		},

    onLoadTagsByRepositoryNodeSuccess: function TaggingComponent_RepositoryService_onloadTagsByRepositoryNodeSuccess(response, obj) {
      
      if(response.json.authenticationError) {
        return;
      }
      
      this._el.innerHTML = "Tags: ";
      this._tags = response.json;
      // add tag spans if there are any tags
      for(tag in this._tags) {
        var tagSpan = document.createElement("span");
        tagSpan.innerHTML = this._tags[tag];
        var removeTagLink = document.createElement("a");
        removeTagLink.setAttribute("href", "");
        removeTagLink.setAttribute("title", "Remove Tag");
        removeTagLink.setAttribute("id", this._tags[tag]);
        removeTagLink.innerHTML = "x";
        // add click listener to the link
        YAHOO.util.Event.addListener(removeTagLink, "click", this.onRemoveTagLinkClick, this._tags[tag], this);
        tagSpan.appendChild(removeTagLink);
        this._el.appendChild(tagSpan);
      }
      
      // create the edit link
      var editLink = document.createElement('a');
      editLink.setAttribute("href", "");
      editLink.setAttribute("title", "Edit Tags");
      editLink.setAttribute("id", this.id + "-add-tags-link");
      // TODO: i18n
      editLink.innerHTML = "edit";
      // add click listener to the link
      YAHOO.util.Event.addListener(editLink, "click", this.onEditLinkClick, this, true);
      this._el.appendChild(editLink);
    },

    onEditLinkClick: function TaggingComponent_onEditLinkClick(event) {

      this._el.innerHTML = "Tags: ";
      
      var taggingForm = document.createElement("form");
      taggingForm.setAttribute("onsubmit", "javascript: return false;");
      this._el.appendChild(taggingForm);
      
      // Create the text input for the tags and append it to the el
      var tagInput = document.createElement("input");
      tagInput.setAttribute("type", "text");
      tagInput.setAttribute("id", this.id + "-tag-input");
      var tagsString = "";
      for(tag in this._tags) {
        tagsString += this._tags[tag] + ",";
      }
      tagInput.setAttribute("value", tagsString);
      taggingForm.appendChild(tagInput);
      
      // create the save link and append it to the el
      var saveLink = document.createElement("a");
      saveLink.setAttribute("href", "");
      saveLink.setAttribute("title", "Save Tags");
      saveLink.innerHTML = "save";
      YAHOO.util.Event.addListener(saveLink, "click", this.onSaveLinkClick, this, true);
      this._el.appendChild(saveLink);

      // create the cancel link and append it to the el
      var cancelLink = document.createElement("a");
      cancelLink.setAttribute("href", "");
      cancelLink.setAttribute("title", "Cancel");
      cancelLink.innerHTML = "cancel";
      YAHOO.util.Event.addListener(cancelLink, "click", this.onCancelLinkClick, this, true);
      this._el.appendChild(cancelLink);
      
      // create a div for tag suggestions and append it to the el
      var suggestionsDiv = document.createElement("div");
      suggestionsDiv.setAttribute("id", this.id + "suggestions-div");
      this._el.appendChild(suggestionsDiv);
      
      // create the datasource to load tag suggestions when the user starts typing 
      var tagsDataSource = new YAHOO.util.XHRDataSource(Activiti.service.REST_PROXY_URI_RELATIVE + "tags");
      tagsDataSource.responseSchema = {
          fields : [ "name" ]
        };
      
      tagsDataSource.responseType = YAHOO.util.XHRDataSource.TYPE_JSARRAY;
      
      // Instantiate the AutoComplete
      var autoComplete = new YAHOO.widget.AutoComplete(this.id + "-tag-input", this.id + "suggestions-div", tagsDataSource);      
      autoComplete.generateRequest = function(sQuery) {
            return '?tag=' + sQuery;
      };
      
      autoComplete.allowBrowserAutocomplete = false; // Disable the browser's built-in autocomplete caching mechanism
      autoComplete.typeAhead = true; // Enable type ahead
      autoComplete.alwaysShowContainer = false;
      autoComplete.minQueryLength = 0; // Can be 0, which will return all results
      autoComplete.maxResultsDisplayed = 10; // Show more results, scrolling is enabled via CSS
      autoComplete.delimChar = [",",";"]; // Enable comma and semi-colon delimiters
      autoComplete.autoHighlight = false; // Auto-highlighting interferes with adding new tags

      // Populate list to start a new interaction
      autoComplete.itemSelectEvent.subscribe(function(sType, aArgs) {
          autoComplete.sendQuery("");
      });
      tagInput.focus();
      
      // prevent a full page reload by stopping the default event
      YAHOO.util.Event.preventDefault(event);
    },
    
    onSaveLinkClick: function TaggingComponent_onSaveLinkClick(event, obj) {
      var rawTags = YAHOO.util.Dom.get(this.id + "-tag-input").value.split(",");
      var tags = [];
      for(var tag in rawTags) {
        // Make sure the tag is not blank
        if(!Activiti.util.blank(rawTags[tag])) {
          // trim the string before adding the tag
          tags.push(Activiti.util.trim(rawTags[tag]));
        }
      }
      var tagsLiteral = YAHOO.lang.merge(this._repositoryNode, {tags: tags})
      this.service.saveTags(tagsLiteral);
      YAHOO.util.Event.preventDefault(event);
    },

    onCancelLinkClick: function TaggingComponent_onCancelLinkClick(event, obj) {
      this.service.loadTagsByRepositoryNode(this._repositoryNode.connectorId, this._repositoryNode.repositoryNodeId);
      YAHOO.util.Event.preventDefault(event);
    },

    onRemoveTagLinkClick: function TaggingComponent_onRemoveTagLinkClick(event, tag) {
      var tagLiteral = YAHOO.lang.merge(this._repositoryNode, {tagName: tag})
      this.service.deleteTag(tagLiteral);
      YAHOO.util.Event.preventDefault(event);
    },

    onSaveTagsSuccess: function TaggingComponent_RepositoryService_onSaveTagsSuccess(response, obj) {
      this.service.loadTagsByRepositoryNode(this._repositoryNode.connectorId, this._repositoryNode.repositoryNodeId);
    },
    
    onDeleteTagSuccess: function TaggingComponent_RepositoryService_onDeleteTagSuccess(response, obj) {
      // Make sure the view is updated after removing a tag.
      this.service.loadTagsByRepositoryNode(this._repositoryNode.connectorId, this._repositoryNode.repositoryNodeId);
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

      content.innerHTML = '<div class="bd"><form id="' + this.id + '-create-folder-form" accept-charset="utf-8"><h1>Create new folder</h1><table><tr><td><label>Name:<br/><input type="text" name="name" value="" /></label><br/></td></tr></table><input type="hidden" name="connectorId" value="' + this._connectorId + '" /><input type="hidden" name="parentFolderId" value="' + this._parentFolderId + '" /></form></div>';

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
          text: "Successfully created folder"
        });
      } else {
        Activiti.widget.PopupManager.displayError("Unable to create folder");
      }
    },

    onFailure: function CreateFolderDialog_onFailure(o) {
      // TODO: i18n
      // Activiti.widget.PopupManager.displayError("Unable to create folder. Check your internet connection and make sure the Activiti server can be reached.");
    }

	});

})();


/**
 * CreateArtifactDialog 
 *
 * @namespace Activiti.component
 * @class Activiti.component.CreateArtifactDialog
 */
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
	 * CreateArtifactDialog constructor.
	 *
	 * @param htmlId {String} The HTML id of the parent element
	 * @param connectorId {String} The id of the connector the artifact should be created in
	 * @param parentFolderId The id of the folder the artifact should be created in
	 * @return {Activiti.component.CreateArtifactDialog} The new component.CreateArtifactDialog instance
	 * @constructor
	 */
	Activiti.component.CreateArtifactDialog = function CreateArtifactDialog_constructor(htmlId, connectorId, parentFolderId)
  {
    Activiti.component.CreateArtifactDialog.superclass.constructor.call(this, "Activiti.component.CreateArtifactDialog", htmlId);

    this._dialog = {};
		this._connectorId = connectorId;
		this._parentFolderId = parentFolderId;

    return this;
  };

  YAHOO.extend(Activiti.component.CreateArtifactDialog, Activiti.component.Base,
  {
	
		/**
		* Fired by YUI when parent element is available for scripting.
		* Template initialisation, including instantiation of YUI widgets and event listener binding.
		*
		* @method onReady
		*/
		onReady: function CreateArtifactDialog_onReady()
		{
		  var content = document.createElement("div");

	    // TODO: i18n

      content.innerHTML = '<div class="bd"><form id="' + this.id + '-artifact-upload-form" action="' + Activiti.service.REST_PROXY_URI_RELATIVE + 'artifact" method="POST" enctype="multipart/form-data" accept-charset="utf-8"><h1>Create new artifact</h1><table><tr><td><label>Name:<br/><input type="text" name="artifactName" value="" /></label><br/></td></tr><tr><td><label>Upload a file:<br/><input type="file" name="file" value="" /></label><br/></td></tr></table><input type="hidden" name="connectorId" value="' + this._connectorId + '" /><input type="hidden" name="parentFolderId" value="' + this._parentFolderId + '" /></form></div>';

      this._dialog = new YAHOO.widget.Dialog(content, {
        fixedcenter: true,
        visible: false,
        constraintoviewport: true,
        modal: true,
        buttons: [
          // TODO: i18n
          { text: "Create" , handler: { fn: this.onSubmit }, isDefault:true },
          { text: "Cancel", handler: { fn: this.onCancel } }
        ]
      });

      this._dialog.callback.upload = this.onUpload;
		  this._dialog.render(document.body);

      // TODO: validation

      // this._dialog.getButtons()[0].set("disabled", true);
		  this._dialog.show();
		},

    onSubmit: function CreateArtifactDialog_onSubmit(event, dialog) {
      if (dialog.form.enctype && dialog.form.enctype == "multipart/form-data") {
        var d = dialog.form.ownerDocument;
        var iframe = d.createElement("iframe");
        iframe.style.display = "none";
        Dom.generateId(iframe, "formAjaxSubmit");
        iframe.name = iframe.id;
        document.body.appendChild(iframe);

        // makes it possible to target the frame properly in IE.
        window.frames[iframe.name].name = iframe.name;
        dialog.form.target = iframe.name;
        this.submit();
      }
    },

    onCancel: function CreateArtifactDialog_onCancel() {
      this.cancel();
    },

    onUpload: function CreateArtifactDialog_onUplaod(o) {
      // TODO: fire an event for e.g. the tree to reload it's nodes etc.
      // TODO: i18n
      if(o.responseText.indexOf("success: true") != -1) {
        Activiti.widget.PopupManager.displayMessage({
          text: "Successfully created artifact"
        });
      } else {
        Activiti.widget.PopupManager.displayError("Unable to create artifact");
      }
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
	 * CreateArtifactDialog constructor.
	 *
	 * @param htmlId {String} The HTML id of the parent element
	 * @param repoInError {String} connectorId of the connector 
	 * @param authenticationError The authentication error message for the first repository
	 * @return {Activiti.component.AuthenticationDialog} The new component.AuthenticationDialog instance
	 * @constructor
	 */
	Activiti.component.AuthenticationDialog = function AuthenticationDialog_constructor(htmlId, repoInError, authenticationError)
  {
    Activiti.component.AuthenticationDialog.superclass.constructor.call(this, "Activiti.component.AuthenticationDialog", htmlId);

    this.service = new Activiti.service.RepositoryService(this);

    this._dialog = {};
		this._repoId = repoInError;
		this._authenticationError = authenticationError;

    return this;
  };

  YAHOO.extend(Activiti.component.AuthenticationDialog, Activiti.component.Base,
  {
	
		/**
		* Fired by YUI when parent element is available for scripting.
		* Template initialisation, including instantiation of YUI widgets and event listener binding.
		*
		* @method onReady
		*/
		onReady: function AuthenticationDialog_onReady()
		{
		  var content = document.createElement("div");
      // TODO: i18n
      var formHtml = '<div class="bd"><form id="' + this.id + '-repo-authentication-dialog" ><h1>Authentication required</h1><p style="color: red; font-weight: bold; max-width:400px">' + this._authenticationError + '</p>';
      formHtml += '<input type="hidden" name="connector-login-request" value="'+ this._repoId +'" />'
      formHtml += '<h2>' + name + '</h2><table><tr><td><label>Username:<br/><input type="text" name="' + this._repoId + '_username" value="" /></label><br/></td></tr><tr><td><label>Password:<br/><input type="password" name="' + this._repoId + '_password" value="" /></label><br/></td></tr></table>';
      
      formHtml += "</form></div>";
      content.innerHTML = formHtml;        
      this._dialog = new YAHOO.widget.Dialog(content, {
        fixedcenter: true,
        visible: false,
        constraintoviewport: true,
        modal: true,
        buttons: [
          // TODO: i18n
          { text: "Login" , handler: { fn: this.onSubmit, scope: this }, isDefault:true },
          { text: "Cancel", handler: { fn: this.onCancel, scope: this } }
        ]
      });
      this._dialog.render(document.body);
      this._dialog.show();
		},

    /**
     * This method is invoked when the authentication dialog for the repositories is submitted.
     * We pass the data from the dialog (usernames and passwords for the repositories that are
     * missing login information) to the loadTree method so it can be added as parameters to the URL.
     *
     * @param event the click event that caused the invokation of this method
     * @param dialog the authentication dialog that is being submitted
     */
    onSubmit: function AuthenticationDialog_onSubmit(event, dialog) {
      this.service.loadTree(dialog.getData());
      if (this._dialog) {
        this._dialog.destroy();
      }
      location.reload();
    },

    /**
     * This method is invoked when the authentication dialog for the repositories is canceled. 
     * Usernames and passwords from the dialog are replaced with empty strings in order to send these to the server.
     */
    onCancel: function AuthenticationDialog_onCancel() {
      var data = {};      
      for (attr in this._dialog.getData()) {
        if(this._dialog.getData().hasOwnProperty(attr)) {
          data[attr] = '""';
        }
      }
      this.service.loadTree(data);
      this._dialog.cancel();
      location.reload();
    }

	});

})();