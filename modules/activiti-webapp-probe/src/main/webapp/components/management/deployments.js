/* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

/**
* Activiti.component.Deployments
*
* Displays a list of Deployments
*
*
* @namespace Activiti
* @class Activiti.component.Deployments
*/
(function()
{
  /**
  * Shortcuts
  */
  var Dom = YAHOO.util.Dom,
  Selector = YAHOO.util.Selector,
  Event = YAHOO.util.Event,
  $html = Activiti.util.decodeHTML;
  
  /**
  * Deployments constructor.
  *
  * @param {String} htmlId The HTML id of the parent element
  * @return {Activiti.component.Deployments} The new component.Deployments instance
  * @constructor
  */
  Activiti.component.Deployments = function Deployments_constructor(htmlId)
  {
    Activiti.component.Deployments.superclass.constructor.call(this, "Activiti.component.Deployments", htmlId);
           
    this.services.managementService = new Activiti.service.ManagementService(this);
    
    // Listen for events that interest us
    this.onEvent(Activiti.event.deleteDeployment, this.onDeleteClick);
    
    return this;
  };
  
  YAHOO.extend(Activiti.component.Deployments, Activiti.component.Base,
  {
  
    /**
    * Fired by YUI when parent element is available for scripting.
    * Template initialisation, including instantiation of YUI widgets and event listener binding.
    *
    * @method onReady
    */
    onReady: function Deployments_onReady()
    {
      //Event.addListener(deleteEls, "click", this.onDeleteClick, false, this);
      //Event.addListener(deleteCascadeEls, "click", this.onDeleteClick, true, this);
      var actionHeading = this.msg("deployments.label.action")
      this.widgets.dataTable = new Activiti.widget.DataTable(
        this.id + "-deployments",
        this,
        [
          { event: Activiti.event.displayDeployments, value: {} },
          { event: "linkClickEvent", subscribe: true, trigger: Activiti.event.deleteDeployment}
        ],
        this.id + "-datatable",
        [ this.id + "-paginator" ],
        [
          {key:"id", label: this.msg("deployments.label.id"), sortable:true},
          {key:"name", label: this.msg("deployments.label.name"), sortable:true},
          {key:"deploymentTime", label: this.msg("deployments.label.deploymentTime"), sortable:true},
          {key:"action", label: actionHeading}
        ]
      );
      
      // Needed to load data and set up other events
      if (!Activiti.event.isInitEvent(Activiti.event.displayDeployments)) 
      {
        this.fireEvent(Activiti.event.displayDeployments, {}, null, true);
      }   
      
      // Set up button events
      var buttonId = this.id+"-delete";
      
      this.deleteButton = new YAHOO.widget.Button(buttonId, 
      {
        onclick: 
        {
          fn: this.onDeleteClick,
          obj: 
          {
            cascade: false
          },
          scope: this
        }
      });

      this.deleteCascadeButton = new YAHOO.widget.Button(buttonId+"Cascade", 
      {
        onclick: 
        {
          fn: this.onDeleteClick,
          obj: 
          {
            cascade: true
          },
          scope: this
        }
      });
      
      // Display form if url
      /* TODO: Decide on URL schema and update this code:
      var processId = Activiti.util.getQueryStringParameter("id");
      if (processId != null)
      {
        this.startProcess(processId);
      }
      */
    },
    
    
    /**
    * Handler for click events on delete buttons
    *
    * @method onDeleteClick
    * @param e {object} The click event
    */
    onDeleteClick: function Deployments_onDeleteClick(e, obj)
    {
      el = Event.getTarget(e);
      cascade = obj.cascade ? obj.cascade : false;
      var selectEls = Dom.getElementsByClassName("deleteSelect"),
        selectIds = [];
      for (var i = 0; i < selectEls.length ;i++) 
      {
        if (selectEls[i].checked === true) 
        {
          selectIds.push(selectEls[i].value);
        }
      }
       if (selectIds.length >= 1) 
      {
        this.deleteDeployment(selectIds, cascade);
      } else 
      {
        Activiti.widget.PopupManager.displayMessage({
          text: this.msg("deployments.message.none-selected")
        })
      } 
    }, 
   
   onDeleteConfirm: function Deployments_onDeleteConfirm(e, obj) 
   {
     this.confirmDialogue.hide();
     this.services.managementService.deleteDeployments(obj.deploymentIds, obj.cascade);
   },
   
   onDeleteCancel: function Deployments_onDeleteCancel(obj) 
   {
     this.confirmDialogue.hide();
   },
    
    /**
    * Prompts user for confirmation and then makes the REST api call to delete service.
    * 
    * @method deleteDeployment
    * @param {Array} deploymentIds - the id of the deployment to be deleted
    * @param {Boolean} cascade - Should the Delete cascade to related resources?
    */
    deleteDeployment: function Deployments_deleteDeployment(deploymentIds, cascade) 
    {
      // Set up message string & helper object
      var count = (deploymentIds.length > 1)? "plural": "singular",
        cascadeString = (cascade)? "Cascade" : "",
        obj = 
        {
          deploymentIds: deploymentIds,
          cascade: cascade
        }
      // Instantiate the Dialog
      this.confirmDialogue = new YAHOO.widget.SimpleDialog("simpledialog1", 
      { 
        width: "300px",
        fixedcenter: true,
        visible: false,
        draggable: false,
        close: false,
        text: this.msg("deployments.prompt.delete"+cascadeString+"."+count),
        icon: YAHOO.widget.SimpleDialog.ICON_WARN,
        constraintoviewport: true,
        buttons: 
        [ 
          { text:this.msg("deployments.prompt.confirm"), handler:
            {
              fn:this.onDeleteConfirm,
              obj:obj,
              scope: this
            }
          },
          { text:this.msg("deployments.prompt.cancel"),  handler:
            { 
              fn: this.onDeleteCancel,
              obj:obj,
              scope: this
             }, 
             isDefault:true 
          } 
        ]
      });      
      // Confirm with user
      this.confirmDialogue.setHeader(this.msg("deployments.prompt.header"))
      this.confirmDialogue.render(this.id);
      this.confirmDialogue.show();
    
    },
    
    /**
     * Fires when a deployment has been deleted - refreshes data.
     * 
     * @method onDeploymentDeletedEvent
     * @param {Object} result
     */
    onDeleteDeploymentSuccess: function Deployments_onDeleteDeploymentSuccess(result)
    {
      Activiti.widget.PopupManager.displayMessage({
        text: this.msg("deployments.message.delete.success")
      })
      this.widgets.dataTable.reload();
    },
    
    /**
     * Fires when a deployment has NOT been deleted - refreshes data.
     * 
     * @method onDeploymentDeletedEvent
     * @param {Object} result
     */
    onDeleteDeploymentFailure: function Deployments_onDeleteDeploymentFailure(result)
    {
      Activiti.widget.PopupManager.displayMessage({
        text: this.msg("deployments.message.delete.failure")
      })
    },
    
    
    /**
     * Activiti.widget.DataTable-callback to construct the url to use to load data into the data table.
     *
     * @method onDataTableCreateURL
     * @param dataTable {Activiti.widget.DataTable} The data table that is invoking the callback
     * @param eventName The name of the event to create a url from
     * @param eventValue The event values to create a url from
     * @return A url, based on the event, to use when loading data into the data table
     */
    onDataTableCreateURL: function Deployments_onDataTableCreateURL(dataTable, eventName, eventValue)
    {
      return this.services.managementService.loadDeploymentsURL(eventValue);
    },
    
    /**
     * 
     * Activiti.widget.DataTable-callback that is called to render the content of each cell in the Actions row
     * 
     * @method onDataTableRenderCellAction
     * @param {Object} dataTable
     * @param {Object} el
     * @param {Object} oRecord
     * @param {Object} oColumn
     * @param {Object} oData
     */
    onDataTableRenderCellAction: function Processes_onDataTableRenderCellAssignee(dataTable, el, oRecord, oColumn, oData) 
    {
      el.innerHTML = "<input type='checkbox' value='"+ oRecord.getData().id +"' class='deleteSelect' />";
    },
    
    /**
     * 
     * Activiti.widget.DataTable-callback that is called to render the content of each cell in the DeploymentTime row
     * 
     * This funtion formats the time according to a property string prior to display.
     * 
     * @method onDataTableRenderCellDeploymentTime
     * @param {Object} dataTable
     * @param {Object} el
     * @param {Object} oRecord
     * @param {Object} oColumn
     * @param {Object} oData
     */
    onDataTableRenderCellDeploymentTime: function Processes_onDataTableRenderCellAssignee(dataTable, el, oRecord, oColumn, oData) 
    {
      el.innerHTML = Activiti.thirdparty.dateFormat(Activiti.thirdparty.fromISO8601(oRecord.getData().deploymentTime), this.msg("Activiti.date-format.default"));
    }
    
  });

})();