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
	 * @param {String} htmlId The HTML id of the parent element
	 * @return {Activiti.component.CreateArtifactDialog} The new component.CreateArtifactDialog instance
	 * @constructor
	 */
	Activiti.component.CreateArtifactDialog = function CreateArtifactDialog_constructor(htmlId, connectorId, containingFolderId, callbackFn)
  {
    Activiti.component.CreateArtifactDialog.superclass.constructor.call(this, "Activiti.component.CreateArtifactDialog", htmlId);

    this._dialog = {};
		this._callbackFn = callbackFn;
		
		this._connectorId = connectorId;
		this._containingFolderId = containingFolderId;

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

	    // TODO: switch to rest proxy URL (Activiti.service.REST_PROXY_URI_RELATIVE), when using this URL at the moment, it seems to be redirecting to the GET URL... Find out what goes wrong here.

      content.innerHTML = '<div class="bd"><form id="' + this.id + '-artifact-upload-form" action="http://localhost:8080/activiti-rest/service/artifact" method="POST" enctype="multipart/form-data" accept-charset="utf-8"><h1>Create new artifact</h1><table><tr><td><label>Name:<br/><input type="text" name="artifactName" value="" /></label><br/></td></tr><tr><td><label>Upload a file:<br/><input type="file" name="file" value="" /></label><br/></td></tr></table><input type="hidden" name="connectorId" value="' + this._connectorId + '" /><input type="hidden" name="containingFolderId" value="' + this._containingFolderId + '" /></form></div>';

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
      var message;
      if(o.responseText.indexOf("success: true") != -1) {
        message = "Successfully created artifact";
      } else {
        message = "Unable to create artifact";
      }

      Activiti.widget.PopupManager.displayMessage({
        text: message
      });
    }

	});

})();