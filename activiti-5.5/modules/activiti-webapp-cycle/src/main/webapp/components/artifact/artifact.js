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
   * Artifact constructor.
   *
   * @param {String} htmlId The HTML id of the parent element
   * @return {Activiti.component.Artifact} The new component.Artifact instance
   * @constructor
   */
  Activiti.component.Artifact = function Artifact_constructor(htmlId)
  {
    Activiti.component.Artifact.superclass.constructor.call(this, "Activiti.component.Artifact", htmlId);
    // Create new service instances and set this component to receive the callbacks
    this.services.repositoryService = new Activiti.service.RepositoryService(this);
    // Listen for events that interest this component
    this.onEvent(Activiti.event.updateArtifactView, this.onUpdateArtifactView);
    this.onEvent(Activiti.event.clickFormEventButton, this.onClickFormEventButton);

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

    this._tabView = {};
    this._connectorId = "";
    this._nodeId = "";
    this._file = false;
    this._label = "";
    this._activeTabIndex = 0;
    this._activeNavigationTabIndex = 0;

    this._fileChooserDialog = {};
    this._linksDataTable = {};
    this._linksDataSource = {};
    this._backlinksDataTable = {};

    return this;
  };

  YAHOO.extend(Activiti.component.Artifact, Activiti.component.Base,
  {
    /**
    * Fired by YUI when parent element is available for scripting.
    * Template initialisation, including instantiation of YUI widgets and event listener binding.
    *
    * @method onReady
    */
    onReady: function Artifact_onReady()
    {
      var size = parseInt(Dom.getStyle('content', 'width'), 10); 
      var left, main;
      var resize = new YAHOO.util.Resize('left', {
          handles: ['r'],
          minWidth: 320,
          maxWidth: 485
        });
      left = Dom.get('left');
      main = Dom.get('main');
      resize.on('resize', function(event) {
          var newLeftWidth = event.width;
          var viewport = [
             Dom.getViewportWidth(),
             Dom.getViewportHeight()
          ];
          var leftWidthPercentage = 100 / (viewport[0] / newLeftWidth);
          var mainWidthPercentage = 100 - leftWidthPercentage - 2;
          var mainWidthPx = viewport[0] / (100 / mainWidthPercentage);
          
          if(mainWidthPx < (viewport[0] - 355)) {
            Dom.setStyle(main, 'width', mainWidthPercentage + '%');
          }
        });
    },
    
    /**
     * This method is invoked when a node in the tree is selected or the active tab chages. 
     * It first checks whether the node is still the same and updates the active tab if 
     * needed. If a new node was selected in the tree, the current artifact view is removed
     * and the loadArtifact method is invoked with the new artifacts id.
     * 
     * @method onUpdateArtifactView
     * @param event {String} the name of the event that triggered this method invokation
     * @param args {array} an array of object literals
     */
    onUpdateArtifactView: function Artifact_onUpdateArtifactView(event, args) {
      
      var eventValue = args[1].value;
      
      this._connectorId = eventValue.connectorId;
      this._nodeId = eventValue.nodeId;
      this._file = eventValue.file;
      this._label = eventValue.label;
      this._activeTabIndex = eventValue.activeArtifactViewTabIndex;
      this._activeNavigationTabIndex = eventValue.activeNavigationTabIndex;

      // get the header el of the content area
      var headerEl = Selector.query("h1", this.id, true);
      // determine whether the node is still the same
      if(eventValue.connectorId && eventValue.nodeId && (eventValue.connectorId + "-" + eventValue.nodeId === headerEl.id)) {
        // still the same node, if the tab view is instanciated, the tab selection should be updated
        if(this._tabView.set) {
          // Update active tab selection silently, without firing an event (last parameter 'true')
          this._tabView.set("activeTab", this._tabView.getTab(this._activeTabIndex), true);
        }
      } else {
        // a new node has been selected in the tree
        var tabViewHtml = YAHOO.util.Selector.query('div', 'artifact-div', true);
        // check whether an artifact was selected before. If yes, remove tabViewHtml and actions
        if(tabViewHtml) {
          var artifactDiv = document.getElementById('artifact-div');
          artifactDiv.removeChild(tabViewHtml);
          var optionsDiv = document.getElementById('options-div');
          optionsDiv.innerHTML = "";
          optionsDiv.removeAttribute("class");
        }
        
        var homeDiv = YAHOO.util.Selector.query('div', this.id, true);
        if(homeDiv) {
          homeDiv.innerHTML = "";
        }
        
        if(eventValue.nodeId && !(eventValue.connectorId.indexOf("ps-") === 0 && eventValue.nodeId === "/")) {
          // instantiate the tagging component
          new Activiti.component.TaggingComponent(this.id, {connectorId: eventValue.connectorId, nodeId: eventValue.nodeId, repositoryNodeLabel: eventValue.name}, "tags-div");
        } else {
          var tagsDiv = document.getElementById('tags-div');
          tagsDiv.innerHTML = "";
        }
        // Check whether the selected node is a file node. If so, load its data
        if(this._file) {
          this.services.repositoryService.loadArtifact({connectorId: eventValue.connectorId, nodeId: eventValue.nodeId, vFolderId: eventValue.vFolderId});
        }
        
        headerEl.setAttribute('id', eventValue.connectorId + "-" + eventValue.nodeId);
        if(eventValue.nodeId && eventValue.nodeId === "/" && eventValue.connectorId && eventValue.connectorId.indexOf("ps-") != -1) {
          // Update the heading that displays the name of the selected node
          headerEl.innerHTML = "Process Solution: " + eventValue.label;
          headerEl.setAttribute('class', 'home');
          
          // Load process solution
          var processSolutionId = eventValue.connectorId.substring(eventValue.connectorId.indexOf("-") + 1, eventValue.connectorId.length);
          this.services.repositoryService.loadProcessSolution({processSolutionId: processSolutionId});
        } else {
          // Update the heading that displays the name of the selected node
          headerEl.removeAttribute('class');
          headerEl.innerHTML = eventValue.label||'';
        }
        
        // Remove the comments
        var commentsDiv = YAHOO.util.Dom.get(this.id + '-comments');
        if(commentsDiv) {
          commentsDiv.innerHTML = '';
        }
      }
    },
    
    /**
     * This method is invoked when an artifact is loaded successfully. It will draw a
     * yui tab view component to display the different content representations of the
     * artifact and create an options panel for links, downloads and actions.
     *
     * @method onLoadArtifactSuccess
     * @param response {object} The callback response
     * @param obj {object} Helper object
     */
    onLoadArtifactSuccess: function Artifact_RepositoryService_onLoadArtifactSuccess(response, obj)
    {
      var me = this;
      
      if(response.json.authenticationError) {
        return new Activiti.component.AuthenticationDialog(this.id, response.json.repoInError, response.json.authenticationError);
      }

      this._tabView = new YAHOO.widget.TabView(); 
      
      // Retrieve rest api response
      var artifactJson = response.json;
      // create a tab for each content representation
      for(var i = 0; i<artifactJson.contentRepresentations.length; i++) {
        var tab = new YAHOO.widget.Tab({ 
          label: artifactJson.contentRepresentations[i], 
          dataSrc: this.loadTabDataURL(artifactJson.connectorId, artifactJson.nodeId, artifactJson.contentRepresentations[i]), 
          cacheData: true
        });
        tab.addListener("contentChange", this.onTabDataLoaded);
        tab.loadHandler.success = function(response) {
          me.onLoadTabSuccess(this /* the tab */, response);
        };
        tab.loadHandler.failure = function(response) {
          me.onLoadTabFailure(this /* the tab */, response);
        };
        this._tabView.addTab(tab);
      }

      var linksTab = new YAHOO.widget.Tab({ 
        label: "Links", 
        dataSrc: Activiti.constants.URL_CONTEXT + 'component/links?' + Activiti.service.Ajax.jsonToParamString({htmlid: this.id + '_links_tab', connectorId: artifactJson.connectorId, nodeId: artifactJson.nodeId, vFolderId: artifactJson.vFolderId||'', activeNavigationTabIndex: this._activeNavigationTabIndex, activeArtifactViewTabIndex: this._activeTabIndex}),
        cacheData: true
      });
      linksTab.addListener("contentChange", this.onTabDataLoaded);
      linksTab.loadHandler.success = function(response) {
        
        this.set('content', response.responseText);
        
        var scripts = [];
        var script = null;
        var regexp = /<script[^>]*>([\s\S]*?)<\/script>/gi;
        while ((script = regexp.exec(response.responseText)))
        {
          scripts.push(script[1]);
        }
        scripts = scripts.join("\n");

        // Remove the script from the responseText so it doesn't get executed twice
        response.responseText = response.responseText.replace(regexp, "");

        // Use setTimeout to execute the script. Note scope will always be "window"
        window.setTimeout(scripts, 0);

      };
      this._tabView.addTab(linksTab);

      this._tabView.appendTo('artifact-div');

      // replace the tabViews onActiveTabChange evnet handler with our own one
      this._tabView.unsubscribe("activeTabChange", this._tabView._onActiveTabChange);
      this._tabView.subscribe("activeTabChange", this.onActiveTabChange, null, this);

      // Select the active tab without firing an event (last parameter is 'silent=true')
      this._tabView.set("activeTab", this._tabView.getTab(this._activeTabIndex), true);

      // Get the options panel
      var optionsDiv = document.getElementById("options-div");

      // Add a dropdown for actions, links and downloads
      if(artifactJson.actions.length > 0 || artifactJson.links.length > 0 || artifactJson.downloads.length > 0) {
        var actionsMenuItems = [];
        var actions = [];
        for(i = 0; i<artifactJson.actions.length; i++) {
          actions.push({ text: artifactJson.actions[i].label, value: {connectorId: artifactJson.connectorId, nodeId: artifactJson.nodeId, vFolderId: artifactJson.vFolderId, actionName: artifactJson.actions[i].name}, onclick: { fn: this.onExecuteActionClick } });
        }
        // Add a custom action to upload a requirements document and link the selected process to it
        if(artifactJson.addRequirementAction) {
          actions.push({ text: "Add New Requirement", onclick: {
            fn: this.onAddNewRequirementActionClick,
            obj: {
              connectorId: artifactJson.addRequirementAction.connectorId,
              parentFolderId: artifactJson.addRequirementAction.parentFolderId,
              linkToConnectorId: artifactJson.connectorId,
              linkToNodeId: artifactJson.nodeId
            },
            scope: this
          }});
        }
        if(actions.length > 0) {
          actionsMenuItems.push(actions);
        }
        var links = [];
        for(i=0; i<artifactJson.links.length; i++) {
          if(artifactJson.links[i].warning) {
            links.push({ text: artifactJson.links[i].label, onclick: { fn: this.onExecuteLinkActionWithWarningClick, obj: {url: artifactJson.links[i].url, warning: artifactJson.links[i].warning}, scope: this } });            
          } else {
            links.push({ text: artifactJson.links[i].label, url: artifactJson.links[i].url, target: "_blank"});            
          }
        }
        if(links.length > 0) {
          actionsMenuItems.push(links);
        }
        var downloads = [];
        for(i=0; i<artifactJson.downloads.length; i++) {
          downloads.push({ text: artifactJson.downloads[i].label, url: Activiti.service.REST_PROXY_URI_RELATIVE + artifactJson.downloads[i].url, target: "_blank"});
        }
        if(downloads.length > 0) {
          actionsMenuItems.push(downloads);
        }
        // TODO: i18n
        var optionsMenu = new YAHOO.widget.Button({ type: "menu", label: "Actions", name: "options", menu: actionsMenuItems, container: optionsDiv });
      }
      optionsDiv.setAttribute('class', 'active');
      
      this.services.repositoryService.loadComments({connectorId: this._connectorId, nodeId: this._nodeId});
      
    },

    onLoadProcessSolutionSuccess: function Artifact_onLoadProcessSolutionSuccess(response, obj)
    {
      var headerEl = Selector.query("h1", this.id, true);
      
      var homeDiv = document.createElement('div');
      homeDiv.setAttribute('class', 'home-div');

      var stateDoneButtonId = "state-done-button";

      homeDiv.innerHTML = "<fieldset><legend>Solution State:</legend><div class='state-div active'><div><div><div class='specification'></div><span>Specification</span></div></div></div>" +
       "<div class='state-div " + ((response.json.state === "IN_IMPLEMENTATION" || response.json.state === "IN_TESTING" || response.json.state === "IN_OPERATIONS") ? "active" : "inactive") + "'><div><div><div class='implementation'></div><span>Implementation</span></div></div></div>" +
       "<div class='state-div " + ((response.json.state === "IN_TESTING" || response.json.state === "IN_OPERATIONS") ? "active" : "inactive") + "'><div><div><div class='testing'></div><span>Testing</span></div></div></div>" +
       "<div class='state-div " + ((response.json.state === "IN_OPERATIONS") ? "active" : "inactive") + "'><div><div><div class='operations'></div><span>Operations</span></div></div></div>" +
       (response.json.state != "IN_OPERATIONS" ? '<span id="' + stateDoneButtonId + '" class="yui-button"><span class="first-child"><button type="button"></button></span></span>' : '') + 
       '</fieldset>';

      // temporary workaround until the backend returns descriptions...
      var descriptions = {
        Management: "For your business documents related to the solution",
        Processes: "Your BPMN diagrams, created with the Activiti Modeler",
        Requirements: "Requirements (e.g. Word or text documents) describing your solution",
        Implementation: 'All technical artifacts, e.g. Java files and Eclipse projects'
      };

      var solutionArtifacts = "<fieldset id='solution-artifacts-fieldset'><legend>Solution Artifacts</legend></fieldset>";
      homeDiv.innerHTML += solutionArtifacts;
      Dom.insertAfter(homeDiv, headerEl);
      
      var solutionArtifactsfieldsetEl = document.getElementById("solution-artifacts-fieldset");
      
      for(var index in response.json.folders) {
        var folder = response.json.folders[index];
        var solutionArtifactEl = document.createElement('div');
        solutionArtifactEl.setAttribute('class', 'solution-artifact ' + folder.type.toLowerCase())
        
        var folderLink = document.createElement('a');
        folderLink.innerHTML = folder.label;
        solutionArtifactEl.appendChild(folderLink);

        var descriptionSpan = document.createElement('span');
        descriptionSpan.innerHTML = (folder.description||descriptions[folder.type]);
        solutionArtifactEl.appendChild(descriptionSpan);

        solutionArtifactsfieldsetEl.appendChild(solutionArtifactEl);
        
        Event.addListener(folderLink, "click", this.onFolderLinkClick, {connectorId: folder.connectorId, nodeId: folder.nodeId, label: folder.label}, this);
      }

      // Create a button to move on to the next state, unless the current state is the last one "Operations"
      if(response.json.state != "IN_OPERATIONS") {
        // A map of the states and their names. It is debatable whether this is the right place for this...
        var states = {
          IN_SPECIFICATION: "Specification",
          IN_IMPLEMENTATION: "Implementation",
          IN_TESTING: "Testing",
          IN_OPERATIONS: "Operations"
        };

        // Some ugly code to determine the next state for the state button
        var nextState = false; 
        for(var key in states) {
          if(nextState) {
            nextState = key;
            break;
          }
          if(key === response.json.state) {
            nextState = true;
          }
        }

        var stateDoneButton = new YAHOO.widget.Button(stateDoneButtonId, { label: states[response.json.state] + " done", id: "stateDoneButton" });
        stateDoneButton.addListener("click", this.onClickStateDoneButton, {processSolutionId: response.json.id, state: nextState}, this);
      }
    },

    onLoadProcessSolutionFailure: function Artifact_onLoadProcessSolutionFailure(response, obj)
    {
      // TODO: Create a proper error dialog that we can reuse in all failure handlers
      // alert("Oooops, something went wrong... But don't worry, we're working on it!");
    },

    onClickStateDoneButton: function Artifact_onClickStateDoneButton(event, obj)
    {
      this.waitDialog.show();
      this.services.repositoryService.updateProcessSolution(obj);
    },

    onUpdateProcessSolutionSuccess: function RepositoryService_Artifact_onUpdateProcessSolutionSuccess(response, obj)
    {
      location.reload();
    },

    onUpdateProcessSolutionFailure: function RepositoryService_Artifact_onUpdateProcessSolutionFailure(response, obj)
    {
      // TODO: Create a proper error dialog that we can reuse in all failure handlers
      this.waitDialog.hide();
      // alert("Oooops, something went wrong... But don't worry, we're working on it!");
    },

    onFolderLinkClick: function Artifact_onFolderLinkClick(event, obj)
    {
      this.fireEvent(Activiti.event.updateArtifactView, {activeNavigationTabIndex: this._activeNavigationTabIndex, activeArtifactViewTabIndex: this._activeArtifactViewTabIndex, connectorId: obj.connectorId, nodeId: obj.nodeId, vFolderId: "", label: obj.label, file: false}, null, true);
    },

    onLoadTabSuccess: function Artifact_onLoadTabSuccess(tab, response) {
      
      try {
        var responseJson = YAHOO.lang.JSON.parse(response.responseText);
        // parse response, create tab content and set it to the tab
        
        var tabContent;
        if(responseJson.renderInfo == "IMAGE") {
          tabContent = '<div class="artifact-image"><img id="' + responseJson.contentRepresentationId + '" src="' + Activiti.service.REST_PROXY_URI_RELATIVE + "content?connectorId=" + encodeURIComponent(responseJson.connectorId) + "&nodeId=" + encodeURIComponent(responseJson.nodeId) + "&contentRepresentationId=" + encodeURIComponent(responseJson.contentRepresentationId) + '" border=0></img></div>';
        } else if (responseJson.renderInfo == "HTML") {
          tabContent = '<div class="artifact-html"><iframe src ="' + Activiti.service.REST_PROXY_URI_RELATIVE + "content?connectorId=" + encodeURIComponent(responseJson.connectorId) + "&nodeId=" + encodeURIComponent(responseJson.nodeId) + "&contentRepresentationId=" + encodeURIComponent(responseJson.contentRepresentationId) + '"><p>Your browser does not support iframes.</p></iframe></div>';
        } else if (responseJson.renderInfo == "HTML_REFERENCE") {
          tabContent = '<div class="artifact-html-reference"><iframe src ="' + responseJson.url + '"><p>Your browser does not support iframes.</p></iframe></div>';
        } else if (responseJson.renderInfo == "BINARY") {
          // TODO: show some information but no content for binary
          tabContent = '<div class="artifact-binary"><p>No preview available...</p></div>';
        } else if (responseJson.renderInfo == "CODE") {
          tabContent = '<div class="artifact-code"><pre id="' + responseJson.contentRepresentationId + '" class="prettyprint" >' + responseJson.content + '</pre></div>';
        } else if (responseJson.renderInfo == "TEXT_PLAIN") {
          tabContent = '<div class="artifact-text-plain"><pre id="' + responseJson.contentRepresentationId + '">' + responseJson.content + '</pre></div>';
        }
        tab.set('content', tabContent);
      }
      catch (e) {
          alert("Invalid response for tab data");
      }
    },

    onLoadTabFailure: function Artifact_onLoadTabFailure(tab, response) {
      var responseJson = YAHOO.lang.JSON.parse(response.responseText);
      var tabContent = "<h3>Java Stack Trace:</h3>";
      for(var line in responseJson.callstack) {
        if( line == 1 || (responseJson.callstack[line].indexOf("org.activiti.cycle") != -1) || responseJson.callstack[line].indexOf("org.activiti.rest.api.cycle") != -1) {
          tabContent += "<span class=\"cycle-stack-trace-highlight\">" + responseJson.callstack[line] + "</span>";
        } else {
          tabContent += "<span class=\"cycle-stack-trace\">" + responseJson.callstack[line] + "</span>";
        }
      }
      tab.set('content', tabContent);
      Activiti.widget.PopupManager.displayError(responseJson.message);
    },

    onLoadCommentsSuccess: function Artifact_onLoadCommentSuccess(response, obj) {      
      var commentsDiv = YAHOO.util.Dom.get(this.id + '-comments');
      
      if(commentsDiv) {
        commentsDiv.innerHTML = '';
      } else {
        commentsDiv = document.createElement('div');  
      }
      
      if(response.json.authenticationError) {
        return new Activiti.component.AuthenticationDialog(this.id, response.json.repoInError, response.json.authenticationError);
      }
      // Retrieve rest api response
      var commentsJson = response.json;

      var artifactEl = document.getElementById("artifact-div");

      commentsDiv.setAttribute('class', 'comments');
      commentsDiv.setAttribute('id', this.id + '-comments');
      commentsDiv.innerHTML += "<h2>Comments</h2>"
      
      for(var item in commentsJson) {
        if(!commentsJson[item].RepositoryNodeComment.answeredCommentId) {
          this.composeCommentHtml(commentsDiv, commentsJson[item].RepositoryNodeComment, commentsJson);
        }
      }
      
      commentsDiv.innerHTML += '<form><textarea id="comment-input" name="comment" value=""></textarea></form><span id="addCommentButton" class="yui-button"><span class="first-child"><button type="button">Add Comment</button></span></span>';
      
      artifactEl.appendChild(commentsDiv);
      
      var replyLinks = Dom.getElementsByClassName("comment-reply-link", 'a', commentsDiv);
      YAHOO.util.Event.addListener(replyLinks, "click", this.onReplyLinkClick, null, this);
      
      var addCommentButton = new YAHOO.widget.Button("addCommentButton", { label:"Add comment", id:"addCommentButton" });
      addCommentButton.addListener("click", this.onClickAddCommentButton, null, this);
      
      this.waitDialog.hide();
    },

    onReplyLinkClick: function Artifact_onReplyLinkClick(event, obj)
    {
      var replyDiv = document.createElement('div');
      replyDiv.innerHTML = '<form id="comment-reply-form-' + event.target.id + '" class="comment-reply"><input type="hidden" name="comment-id" value="' + event.target.id + '" /><textarea id="comment-reply-input" name="comment-reply" value=""></textarea></form><span id="reply-button' + event.target.id + '" class="yui-button"><span class="first-child"><button type="button">Reply</button></span></span>';
      Dom.insertAfter(replyDiv, event.target);
      
      var replyButton = new YAHOO.widget.Button("reply-button" + event.target.id, { label:"Reply" });
      replyButton.addListener("click", this.onReplyButtonClick, 'comment-reply-form-' + event.target.id, this);
      
      // remove the 'reply' link
      event.target.parentNode.removeChild(event.target);
      
      YAHOO.util.Event.preventDefault(event);
    },

    onReplyButtonClick: function Artifact_onReplyButtonClick(event, id)
    {
      var replyForm = YAHOO.util.Dom.get(id);
      var data = {connectorId: this._connectorId, nodeId: this._nodeId};
      for(var prop in replyForm.childNodes) {
        if(replyForm.childNodes[prop] && replyForm.childNodes[prop].name == "comment-id") {
          data['answeredCommentId'] = replyForm.childNodes[prop].value;
        }
        if(replyForm.childNodes[prop] && replyForm.childNodes[prop].name == "comment-reply") {
          data['content'] = replyForm.childNodes[prop].value;
        }
      }
      if(data.content) {
        this.waitDialog.show();
        this.services.repositoryService.saveComment(data); 
      }
    },

    /**
     * Click event listener for the "Add Comment" button.
     * 
     * @param event {object} The event that was triggered
     * @param args {Array} The event values     
     */
    onClickAddCommentButton: function Artifact_onClickAddLinkButton(event, args)
    {
      var comment = YAHOO.util.Dom.get("comment-input");
      if(comment.value) {
        this.waitDialog.show();
        this.services.repositoryService.saveComment({connectorId: this._connectorId, nodeId: this._nodeId, content: comment.value}); 
      }
    },
    
    onSaveCommentSuccess: function Artifact_onSaveCommentSuccess(response, obj)
    {
      this.services.repositoryService.loadComments({connectorId: this._connectorId, nodeId: this._nodeId});
      // TODO: i18n
      Activiti.widget.PopupManager.displayMessage({
        text: 'Successfully added comment'
      });
    },

    onSaveCommentFailure: function Artifact_onSaveCommentFailure(response, obj)
    {
      this.waitDialog.hide();
    },

    onExecuteActionClick: function Artifact_onExecuteActionClick(e)
    {
      return new Activiti.widget.ExecuteArtifactActionForm(this.id + "-executeArtifactActionForm", this.value.connectorId, this.value.nodeId, this.value.vFolderId, this.value.actionName);
    },
    
    onExecuteLinkActionWithWarningClick: function Artifact_onExecuteLinkActionWithWarningClick(eventName, event, obj)
    {
      var url = obj.url;
      
      var content = document.createElement("div");
      content.innerHTML = '<div class="bd"><form id="' + this.id + '-confirm-edit" accept-charset="utf-8"><h1>Warning</h1><p>' + obj.warning + '</p></form></div>';
      
      var dialog = new YAHOO.widget.Dialog(content, 
      {
        fixedcenter: "contained",
        visible: false,
        constraintoviewport: true,
        modal: true,
        hideaftersubmit: false,
        buttons: [
          { text: this.msg("button.yes") , handler: { fn: function(event, dialog) {
              window.open(url);
              if (dialog) {
                dialog.destroy();
              }
            }, isDefault:true }
          },
          { text: Activiti.i18n.getMessage("button.cancel"), handler: { fn: function CreateFolderDialog_onCancel(event, dialog) {
              dialog.cancel();
            }}
          }
        ]
      });
		  dialog.render(document.body);
		  dialog.show();
    },
    
    onAddNewRequirementActionClick: function Artifact_onAddNewRequirementActionClick(eventName, event, obj)
    {
      var me = this;
      var fnOnUpload = function(response) {
        var json = YAHOO.lang.JSON.parse(response.responseText);
        me.fireEvent(Activiti.event.updateArtifactView, {activeNavigationTabIndex: me._activeNavigationTabIndex, activeArtifactViewTabIndex: 0, connectorId: json.connectorId, nodeId: json.nodeId, vFolderId: json.vFolderId, label: json.label, file: "true"}, null, true);
      };
      return new Activiti.component.CreateArtifactDialog(this.id, obj.connectorId, obj.parentFolderId, "Add New Requirement", fnOnUpload, obj.linkToConnectorId, obj.linkToNodeId);
    },
    
    onTabDataLoaded: function Artifact_onTabDataLoaded()
    {
      prettyPrint();
    },

    loadTabDataURL: function Artifact_loadTabDataURL(connectorId, nodeId, representationId)
    {
      return Activiti.service.REST_PROXY_URI_RELATIVE + "content-representation?connectorId=" + encodeURIComponent(connectorId) + "&nodeId=" + encodeURIComponent(nodeId) + "&representationId=" + encodeURIComponent(representationId);
    },

    onActiveTabChange: function Artifact_onActiveTabChange(event)
    {
      var newActiveTabIndex = this._tabView.getTabIndex(event.newValue);
      this.fireEvent(Activiti.event.updateArtifactView, {activeNavigationTabIndex: this._activeNavigationTabIndex, activeArtifactViewTabIndex: newActiveTabIndex, connectorId: this._connectorId, nodeId: this._nodeId, vFolderId: this._vFolderId, label: this._label, file: this._file}, null, true);
      YAHOO.util.Event.preventDefault(event);
    },

    onClickFormEventButton: function Artifact_onClickFormEventButton(event, args)
    {
      var obj = args[1].value;

      if(obj.inputName === "targetFolderId") {
        return new Activiti.component.FileChooserDialog(this.id, function (args) {

          var connectorIdInput = Selector.query("input[name=targetConnectorId]", obj.activitiFormWidget.dialog.form, true);
          connectorIdInput.value = args.connectorId;

          var targetFolderId = Selector.query("input[name=targetFolderId]", obj.activitiFormWidget.dialog.form, true);
          targetFolderId.value = args.nodeId;
          targetFolderId.type = "hidden";

          var formEventValueSpan = Selector.query("span[class=form-Event-value]", obj.activitiFormWidget.dialog.form, true);
          formEventValueSpan.innerHTML = args.nodeName;

          obj.activitiFormWidget.doValidate();
        }, false, null, true, false);  
      } else if (obj.inputName === "targetArtifactId") {
        var treeRootConnectorId = Selector.query("input[name=treeRootConnectorId]", obj.activitiFormWidget.dialog.form, true).value,
        treeRootNodeId = Selector.query("input[name=treeRootNodeId]", obj.activitiFormWidget.dialog.form, true).value;
        return new Activiti.component.FileChooserDialog(this.id, function (args) {

          var connectorIdInput = Selector.query("input[name=targetConnectorId]", obj.activitiFormWidget.dialog.form, true);
          connectorIdInput.value = args.connectorId;

          var targetArtifactId = Selector.query("input[name=targetArtifactId]", obj.activitiFormWidget.dialog.form, true);
          targetArtifactId.value = args.nodeId;
          targetArtifactId.type = "hidden";

          var formEventValueSpan = Selector.query("span[class=form-Event-value]", obj.activitiFormWidget.dialog.form, true);
          formEventValueSpan.innerHTML = args.nodeName;

          obj.activitiFormWidget.doValidate();
        }, true, null, false, true, treeRootConnectorId, treeRootNodeId);
      }
    },

    composeCommentHtml: function Artifact_composeCommentHtml(commentEl, comment, comments) {
      var replyEl = document.createElement('div');
      replyEl.setAttribute('class', 'comment');
      replyEl.innerHTML = '<span class="comment-author">' + Activiti.util.encodeHTML(comment.author) + '</span><span class="comment-date">' + Activiti.util.encodeHTML(comment.creationDate) + '</span><span class="comment-content">' + Activiti.util.encodeHTML(comment.content) + '</span><a href="#" id="' + comment.id + '" class="comment-reply-link">reply</a>';
      var currentId = comment.id;
      for(var reply in comments) {
        if(comments[reply].RepositoryNodeComment.answeredCommentId == currentId) {
          this.composeCommentHtml(replyEl, comments[reply].RepositoryNodeComment, comments);
        }  
      }
      commentEl.appendChild(replyEl);
    }
    
  });

})();
