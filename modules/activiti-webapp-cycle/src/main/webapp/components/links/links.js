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
   * Links constructor.
   *
   * @param {String} htmlId The HTML id of the parent element
   * @return {Activiti.component.Links} The new component.Links instance
   * @constructor
   */
  Activiti.component.Links = function Links_constructor(htmlId, connectorId, artifactId)
  {
    Activiti.component.Links.superclass.constructor.call(this, "Activiti.component.Links", htmlId);
    this.service = new Activiti.service.RepositoryService(this);

    this._connectorId = connectorId;
    this._artifactId = artifactId;

    this._linksDataSource = new YAHOO.util.XHRDataSource(Activiti.service.REST_PROXY_URI_RELATIVE + "artifact-links?connectorId=" + encodeURIComponent(this._connectorId) + "&artifactId=" + encodeURIComponent(this._artifactId));
    this._linksDataSource.responseType = YAHOO.util.XHRDataSource.TYPE_JSARRAY;

    // this._linksDataSource.doBeforeParseData = function(oRequest, oFullResponse, oCallback) {
    //   // check the response to deal with potential authentication errors
    // };

    this._incomingLinksDataSource = new YAHOO.util.XHRDataSource(Activiti.service.REST_PROXY_URI_RELATIVE + "incoming-artifact-links?connectorId=" + encodeURIComponent(this._connectorId) + "&artifactId=" + encodeURIComponent(this._artifactId));
    this._incomingLinksDataSource.responseType = YAHOO.util.XHRDataSource.TYPE_JSARRAY;

    this._linksDataTable = {};
    this._incomingLinksDataTable = {};

    return this;
  };

  YAHOO.extend(Activiti.component.Links, Activiti.component.Base,
  {
    /**
    * Fired by YUI when parent element is available for scripting.
    * Template initialisation, including instantiation of YUI widgets and event listener binding.
    *
    * @method onReady
    */
    onReady: function Links_onReady()
    {
      var me = this;

      var linksElId = this.id + '-links-div';
      var addLinkElId = this.id + 'add-link';
      var incomingLinksElId = this.id + 'incoming-links-div';

      var el = document.getElementById(this.id);

      var linksWrapperEl = document.createElement("div");
      linksWrapperEl.setAttribute('class', 'links-wrapper');
      linksWrapperEl.innerHTML = '<h3>Outgoing Links</h3><div id="' + linksElId + '"></div><span id="' + addLinkElId + '" class="yui-button"><span class="first-child"><button type="button">Add link</button></span></span>';

      el.appendChild(linksWrapperEl);

      var incomingLinksWrapperEl = document.createElement('div');
      incomingLinksWrapperEl.setAttribute('class', 'incoming-links-wrapper');
      incomingLinksWrapperEl.innerHTML = '<h3>Incoming Links</h3><div id="' + incomingLinksElId + '"></div>';

      el.appendChild(incomingLinksWrapperEl);

      // TODO: i18n
      var linksColumnDefs = [
        {key: "Name", sortable:true, resizeable:true},
        // {key: "Revision", sortable:true},
        {key: "Element", resizeable:true},
        {key: "Type", resizeable:true}
      ];

      // instantiate the links data table
      this._linksDataTable = new YAHOO.widget.DataTable(linksElId, linksColumnDefs, this._linksDataSource, {scrollable:true});

      // parse the response of the request that loaded the links from the server and 
      // transform it into a format that is easily consumed by the YUI dataTable component
      this._linksDataTable.doBeforeLoadData = function (sRequest, oResponse, oPayload) {
          var jsonArray = oResponse.results;
          var rows = [];
          for(var i=0; i<jsonArray.length; i++) {
            var row = {
              Name: '<a class="openArtifactLink" ' + ((me.id.indexOf('links-widget')  != -1) ? 'target="blank"' : '') + ' href="' + Activiti.constants.URL_CONTEXT + "start#event=" + Activiti.util.eventDescriptorToState('updateArtifactView', {"connectorId": encodeURIComponent(jsonArray[i].artifact.targetConnectorId), "repositoryNodeId": encodeURIComponent(jsonArray[i].artifact.targetArtifactId), "isRepositoryArtifact": true, "name": encodeURIComponent(jsonArray[i].artifact.label), "activeTabIndex": 0}) + '">' + jsonArray[i].artifact.label + '</a>',
              Revision: jsonArray[i].artifact.targetArtifactRevision,
              Element: jsonArray[i].artifact.targetElementName,
              Type: '<div class="artifact-type ' + me.getClassForContentType(jsonArray[i].artifact.targetContentType) + '">' + jsonArray[i].artifact.targetContentType + '</div>'
            };
            rows.push(row);
          }          
          oResponse.results = rows;
          return true;
      };   

      var addLinkButton = new YAHOO.widget.Button(addLinkElId, { label:"Add link", id:"addLinkButton" });
      addLinkButton.addListener("click", this.onClickAddLinkButton, null, this);

      // instantiate the links data table
      this._incomingLinksDataTable = new YAHOO.widget.DataTable(incomingLinksElId, linksColumnDefs, this._incomingLinksDataSource, {scrollable:true});
      // parse the response of the request that loaded the backlinks from the server and 
      // transform it into a format that is easily consumed by the YUI dataTable component
      this._incomingLinksDataTable.doBeforeLoadData = function (sRequest, oResponse, oPayload) {
          var jsonArray = oResponse.results;
          var rows = [];
          for(var i=0; i<jsonArray.length; i++) {
            var row = {
              Name: '<a class="openArtifactLink" href="' + Activiti.constants.URL_CONTEXT + "start#event=" + Activiti.util.eventDescriptorToState('updateArtifactView', {"connectorId": encodeURIComponent(jsonArray[i].artifact.sourceConnectorId), "repositoryNodeId": encodeURIComponent(jsonArray[i].artifact.sourceArtifactId), "isRepositoryArtifact": true, "name": encodeURIComponent(jsonArray[i].artifact.label), "activeTabIndex": 0}) + '">' + jsonArray[i].artifact.label + '</a>',
              Revision: jsonArray[i].artifact.sourceArtifactRevision,
              Element: jsonArray[i].artifact.sourceElementName,
              Type: '<div class="artifact-type ' + me.getClassForContentType(jsonArray[i].artifact.sourceContentType) + '">' + jsonArray[i].artifact.sourceContentType + '</div>'
            };
            rows.push(row);
          }          
          oResponse.results = rows;
          return true;
      };
    },

    onClickAddLinkButton: function Links_onClickAddLinkButton(event, args)
    {
      return new Activiti.component.FileChooserDialog(this.id, "onAddLinkSubmit", true, this, false, true);
    },

    onAddLinkSubmit: function Links_onAddLinkSubmit(obj) {
      this.service.createArtifactLink({"connectorId": this._connectorId, "artifactId": this._artifactId, "targetConnectorId": obj.connectorId,"targetArtifactId": obj.nodeId});
    },

    /**
     * This method is called when the service method createArtifactLink returns and reloads 
     * the links data table so the newly added link becomes visible.
     *
     * @param args object that contains three attributes: config, json and serverResponse
     */
    onCreateArtifactLinkSuccess: function Links_onCreateArtifactLinkSuccess(args)
    {
      var oCallback = {
          success : this._linksDataTable.onDataReturnInitializeTable,
          failure : this._linksDataTable.onDataReturnInitializeTable,
          scope : this._linksDataTable,
          argument: this._linksDataTable.getState() // data payload that will be returned to the callback function
      };
      this._linksDataSource.sendRequest("", oCallback);
    },

    /**
     * Convenience method to retrieve css class attributes for content types.
     *
     * @param contentType the content type to retrieve the css class for
     * @return {string} the content type class name
     */
    getClassForContentType: function Links_getClassForContentType(contentType) {
      if(contentType === "image/png" || contentType === "image/gif" || contentType === "image/jpeg") {
        return "icon-img";
      } else if(contentType === "application/xml") {
        return "icon-code-red";
      } else if(contentType === "text/html") {
        return "icon-www";
      } else if(contentType === "text/plain") {
        return "icon-txt";
      } else if(contentType === "application/pdf") {
        return "icon-pdf";
      } else if(contentType === "application/json;charset=UTF-8") {
        return "icon-code-blue";
      } else if(contentType === "application/msword") {
        return "icon-doc";
      } else if(contentType === "application/powerpoint") {
        return "icon-ppt";
      } else if(contentType === "application/excel") {
        return "icon-xls";
      } else if(contentType === "application/javascript") {
        return "icon-code-blue";
      }
      // Use white page as default icon for all other content types
      return "icon-blank";
    }

  });
})();
