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
   * @param nodeId {string} the id of the artifact
   * @param artifactActionName {string} the name of the action
   * @return {Activiti.widget.ExecuteArtifactActionForm} The new Activiti.widget.ExecuteArtifactActionForm instance
   * @constructor
   */
  Activiti.widget.ExecuteArtifactActionForm = function ExecuteArtifactActionForm_constructor(id, connectorId, nodeId, vFolderId, artifactActionName)
  {
    Activiti.widget.ExecuteArtifactActionForm.superclass.constructor.call(this, id);
    this.connectorId = connectorId;
    this.nodeId = nodeId;
    this.vFolderId = vFolderId;
    this.artifactActionName = artifactActionName;
    this.service = new Activiti.service.RepositoryService(this);
    this.service.setCallback("loadArtifactActionForm", { fn: this.onLoadFormSuccess, scope: this }, {fn: this.onLoadFormFailure, scope: this });
    this.service.loadArtifactActionForm({connectorId: this.connectorId, nodeId: this.nodeId, vFolderId: this.vFolderId, actionName: this.artifactActionName});

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
      this.service.executeArtifactAction({connectorId: this.connectorId, nodeId: this.nodeId, vFolderId: this.vFolderId, actionName: this.artifactActionName}, variables);
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
 * @namespace Activiti.component
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
	 * @param htmlId {String} The HTML id of the parent element
	 * @param callbackFn {function} The function that shall be invoked when submitting the dialog
	 * @param scope {Object} The scope the callback shall be called in or null if scope can be the FileChooserDialog component
	 * @param highlightFolders {boolean} Flag to indicate whether folders should be selectable in the tree
	 * @param highlightFiles {boolean} Flag to indicate whether files should be selectable in the tree
	 * @param treeRootConnectorId {String} Optional parameter to specify a root folder for the tree
	 * @param treeRootNodeId {String} Optional parameter to specify a root folder for the tree
	 * @return {Activiti.component.FileChooserDialog} The new Activiti.component.FileChooserDialog instance
	 * @constructor
	 */
	Activiti.component.FileChooserDialog = function FileChooserDialog_constructor(htmlId, callbackFn, showFiles, scope, highlightFolders, highlightFiles, treeRootConnectorId, treeRootNodeId)
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

    // Optional parameters to provide a root node when loading the tree 
    this._treeRootConnectorId = treeRootConnectorId||null;
    this._treeRootNodeId = treeRootNodeId||null;

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

			// load the json representation of the tree, onLoadTreeSuccess will be called when the load function returns
      var treeConfig;
      if(this._treeRootConnectorId && this._treeRootNodeId) {
        treeConfig = {treeId: "ps", treeRootConnectorId: this._treeRootConnectorId, treeRootNodeId: this._treeRootNodeId};
      } else {
        treeConfig = {treeId: "repo"};
      }
			this.services.repositoryService.loadTree(treeConfig);
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
					me.services.repositoryService.loadChildNodes({connectorId: node.data.connectorId, nodeId: node.data.nodeId, vFolderId: node.data.vFolderId, treeId: "repo"}, node, fnLoadComplete);
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
		onLoadChildNodesSuccess: function FileChooserDialog_RepositoryService_onLoadChildNodesSuccess(response, obj)
    {
      // Retrieve rest api response
      var treeNodesJson = response.json;

			for(var i = 0; i<treeNodesJson.length; i++) {
			  if( treeNodesJson[i].folder ) {
			    var node = new YAHOO.widget.TextNode(treeNodesJson[i], obj.parentNode, false);
			    node.enableHighlight = this._highlightFolders;
			  } else if( this._showFiles && treeNodesJson[i].file ) {
			    var node = new YAHOO.widget.TextNode(treeNodesJson[i], obj.parentNode, true);
          node.enableHighlight = this._highlightFiles;
			  }
			}

			// call the fnLoadComplete function that the treeView component provides to 
			// indicate that the loading of the sub nodes was successfull.
			obj.fnLoadComplete();
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
      if(this._currentNode.data && this._currentNode.data.connectorId && this._currentNode.data.nodeId && this._currentNode.label) {        
        
        if(this._scope) {
          this._scope[this._callbackFn]({"connectorId": this._currentNode.data.connectorId, "nodeId": this._currentNode.data.nodeId, "nodeName": this._currentNode.label});
        } else {
          this._callbackFn({"connectorId": this._currentNode.data.connectorId, "nodeId": this._currentNode.data.nodeId, "nodeName": this._currentNode.label});
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
	 * @param nodeLiteral An object literal that contains the connectorId, the nodeId and the nodeLabel of the repository node to be tagged
	 * @param elId The id of the Dom element that the tagging-component should be appended to
	 * @return {Activiti.component.TaggingComponent} The new component.TaggingComponent instance
	 * @constructor
	 */
	Activiti.component.TaggingComponent = function TaggingComponent_constructor(htmlId, nodeLiteral, elId)
  {
    Activiti.component.TaggingComponent.superclass.constructor.call(this, "Activiti.component.TaggingComponent", htmlId);

    this.service = new Activiti.service.RepositoryService(this);

    this._node = nodeLiteral;
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
		  this.service.loadTagsByNode(this._node.connectorId, this._node.nodeId);
		},

    onLoadTagsByNodeSuccess: function TaggingComponent_RepositoryService_onloadTagsByNodeSuccess(response, obj) {
      
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
      var tagsLiteral = YAHOO.lang.merge(this._node, {tags: tags})
      this.service.saveTags(tagsLiteral);
      YAHOO.util.Event.preventDefault(event);
    },

    onCancelLinkClick: function TaggingComponent_onCancelLinkClick(event, obj) {
      this.service.loadTagsByNode(this._node.connectorId, this._node.nodeId);
      YAHOO.util.Event.preventDefault(event);
    },

    onRemoveTagLinkClick: function TaggingComponent_onRemoveTagLinkClick(event, tag) {
      var tagLiteral = YAHOO.lang.merge(this._node, {tagName: tag})
      this.service.deleteTag(tagLiteral);
      YAHOO.util.Event.preventDefault(event);
    },

    onSaveTagsSuccess: function TaggingComponent_RepositoryService_onSaveTagsSuccess(response, obj) {
      this.service.loadTagsByNode(this._node.connectorId, this._node.nodeId);
    },
    
    onDeleteTagSuccess: function TaggingComponent_RepositoryService_onDeleteTagSuccess(response, obj) {
      // Make sure the view is updated after removing a tag.
      this.service.loadTagsByNode(this._node.connectorId, this._node.nodeId);
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
	 * @param title {String} The title that will be displayed in the dialog
	 * @param fnOnUpload {function} Callback handler for the upload, will be called when the upload succeeded
	 * @param linkToConnectorId {String} Optional parameter to refer to another artifact the new one should be linked to
	 * @param linkToNodeId {String} Optional parameter to refer to another artifact the new one should be linked to
	 * @return {Activiti.component.CreateArtifactDialog} The new component.CreateArtifactDialog instance
	 * @constructor
	 */
	Activiti.component.CreateArtifactDialog = function CreateArtifactDialog_constructor(htmlId, connectorId, parentFolderId, title, fnOnUpload, linkToConnectorId, linkToNodeId)
  {
    Activiti.component.CreateArtifactDialog.superclass.constructor.call(this, "Activiti.component.CreateArtifactDialog", htmlId);

    this._dialog = {};
		this._connectorId = connectorId;
		this._parentFolderId = parentFolderId;
		this._title = title;
		this._onUpload = fnOnUpload;

    this._linkToConnectorId = linkToConnectorId;
    this._linkToNodeId = linkToNodeId;

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

      content.innerHTML = '<div class="bd"><form id="' + this.id + '-artifact-upload-form" action="' + Activiti.service.REST_PROXY_URI_RELATIVE + 'artifact" method="POST" enctype="multipart/form-data" accept-charset="utf-8">' +
        '<h1>' + this._title + '</h1><table><tr><td>' +
        '<label>Name:<br/><input type="text" name="artifactName" value="" /></label><br/></td></tr><tr><td>' +
        '<label>Upload a file:<br/><input type="file" name="file" value="" /></label><br/></td></tr></table>' +
        '<input type="hidden" name="connectorId" value="' + this._connectorId + '" />' +
        '<input type="hidden" name="parentFolderId" value="' + this._parentFolderId + '" />' +
        ((this._linkToConnectorId && this._linkToNodeId) ? '<input type="hidden" name="linkToConnectorId" value="' + this._linkToConnectorId + '" /><input type="hidden" name="linkToNodeId" value="' + this._linkToNodeId + '" />' : '') +
        '</form></div>';

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

      this._dialog.callback.upload = this._onUpload;
		  this._dialog.render(document.body);

      // TODO: validation

      // this._dialog.getButtons()[0].set("disabled", true);
		  this._dialog.show();
		},

    onSubmit: function CreateArtifactDialog_onSubmit(event, dialog) {
      var data = dialog.getData();

      // Make sure we submit a file name and a proper file extension
      if(!data.artifactName) {
        // If the name field is empty, replace it with the name of the upload file
        dialog.form.artifactName.value = data.file;
      } else if(data.artifactName.indexOf(".") === -1) {
        // If the name doesn't have an extension, use the one from the upload file
        dialog.form.artifactName.value += data.file.substr(data.file.indexOf("."), data.file.length);
      }

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