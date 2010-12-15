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
* Activiti.component.Jobs
*
* Displays a list of Jobs
*
*
* @namespace Activiti
* @class Activiti.component.Jobs
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
  * Jobs constructor.
  *
  * @param {String} htmlId The HTML id of the parent element
  * @return {Activiti.component.Deployments} The new component.Deployments instance
  * @constructor
  */
  Activiti.component.Jobs = function Jobs_constructor(htmlId)
  {
    Activiti.component.Jobs.superclass.constructor.call(this, "Activiti.component.Jobs", htmlId);
    this.services.managementService = new Activiti.service.ManagementService(this);
    return this;
  };
  
  YAHOO.extend(Activiti.component.Jobs, Activiti.component.Base,
  {
  
    /**
    * Fired by YUI when parent element is available for scripting.
    * Template initialisation, including instantiation of YUI widgets and event listener binding.
    *
    * @method onReady
    */
    onReady: function Jobs_onReady()
    {
      this.widgets.dataTable = new Activiti.widget.DataTable(
        this.id + "-jobs",
        this,
        [ { event: Activiti.event.displayJobs, value: {} } ],
        this.id + "-datatable",
        [ this.id + "-paginator" ],
        [
          { key:"select", label: this.msg("jobs.label.select") },
          { key:"exceptionMessage", label: this.msg("jobs.label.status") },
          { key:"id", label: this.msg("jobs.label.id"), sortable:true },
          { key:"executionId", label: this.msg("jobs.label.executionId"), sortable:true },
          { key:"retries", label: this.msg("jobs.label.retries"), sortable:true },
          { key:"processInstanceId", label: this.msg("jobs.label.processInstanceId"), sortable:true },
          { key:"dueDate", label: this.msg("jobs.label.dueDate"), sortable:true },
          { key:"action", label: this.msg("jobs.label.actions") }
        ]
      );
      
      // Needed to load data and set up other events
      if (!Activiti.event.isInitEvent(Activiti.event.displayJobs)) 
      {
        this.fireEvent(Activiti.event.displayJobs, {}, null);
      }   
      
      // Set up button events
      this.widgets.executeButton = Activiti.widget.createButton(this, "execute", this.onMultiExecuteClick);
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
    onDataTableCreateURL: function Jobs_onDataTableCreateURL(dataTable, eventName, eventValue)
    {
      return this.services.managementService.loadJobsURL(eventValue);
    },

    /**
     * 
     * Activiti.widget.DataTable-callback that is called to render the content of each cell in the Status row
     * 
     * @method onDataTableRenderCellStatus
     * @param {Object} dataTable
     * @param {Object} el
     * @param {Object} oRecord
     * @param {Object} oColumn
     * @param {Object} oData
     */
    onDataTableRenderCellExceptionMessage: function Jobs_onDataTableRenderCellStatus(dataTable, el, oRecord, oColumn, oData) 
    {
      var data = oRecord.getData(),
        status = (data.exceptionMessage === null)? "pending": "failed";
      el.innerHTML = "<span class='statusMessage " + status + "'>"+this.msg("jobs.status."+status)+"</span>";
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
    onDataTableRenderCellAction: function Jobs_onDataTableRenderCellAction(dataTable, el, oRecord, oColumn, oData) 
    {
      var data = oRecord.getData(), 
        actions = [];
      if (data.exceptionMessage !== null)
      {
        actions.push('<a href="#" class="onActionViewException" title="' + this.msg("jobs.link.view-exception") + '" tabindex="0">&nbsp;</a>');
      }
      actions.push('<a href="#" class="onActionExecuteJob" title="' + this.msg("jobs.link.execute") + '" tabindex="0">&nbsp;</a>');
      el.innerHTML = actions.join("<br/>")
    },

    /**
     * 
     * Activiti.widget.DataTable-callback that is called to render the content of each cell in the Select row
     * 
     * @method onDataTableRenderCellAction
     * @param {Object} dataTable
     * @param {Object} el
     * @param {Object} oRecord
     * @param {Object} oColumn
     * @param {Object} oData
     */
    onDataTableRenderCellSelect: function Jobs_onDataTableRenderCellSelect(dataTable, el, oRecord, oColumn, oData) 
    {
      el.innerHTML = "<input type='checkbox' value='"+ oRecord.getData().id +"' class='jobSelect' />";
    },
 
     /**
     * 
     * Activiti.widget.DataTable-callback that is called to render the content of each cell in the Due Date row:
     *  - Formats the date w/ i18n string
     *  - Ensures something is displayed, even in the case of a 'null' value
     * 
     * @method onDataTableRenderCellAction
     * @param {Object} dataTable
     * @param {Object} el
     * @param {Object} oRecord
     * @param {Object} oColumn
     * @param {Object} oData
     */   
    onDataTableRenderCellDueDate: function Jobs_onDataTableRenderCellDueDate(dataTable, el, oRecord, oColumn, oData) 
    {
      el.innerHTML = (oRecord.getData().dueDate === null)? this.msg("jobs.label.null") : Activiti.thirdparty.dateFormat(Activiti.thirdparty.fromISO8601(oRecord.getData().dueDate), this.msg("Activiti.date-format.default"))
    },
    
    /**
     * 
     * Activiti.widget.DataTable-callback that is called to render the content of each cell in the Assignee row:
     *  - Ensures something is displayed, even in the case of a 'null' value
     * 
     * @method onDataTableRenderCellAction
     * @param {Object} dataTable
     * @param {Object} el
     * @param {Object} oRecord
     * @param {Object} oColumn
     * @param {Object} oData
     */    
    onDataTableRenderCellAssignee: function Jobs_onDataTableRenderCellAssignee(dataTable, el, oRecord, oColumn, oData) 
    {
      el.innerHTML = (oRecord.getData().assignee === null)? this.msg("jobs.label.null") : oRecord.getData().assignee;
    },
    
    /**
     * 
     * onMultiExecuteClick
     * 
     * @method onMultiExecuteClick
     * @param {Object} e - the event object of the action that triggered the click
     * @param {Object} obj - helper object passed from the initialisation
     */
    onMultiExecuteClick: function Jobs_onMultiExecuteClick(e, obj) 
    {
      var selectEls = Dom.getElementsByClassName("jobSelect"),
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
        this.services.managementService.executeJobs(selectIds);
      }
      else
      {
        Activiti.widget.PopupManager.displayMessage({
          text: this.msg("jobs.message.none-selected")
        })
      } 
    },

    /**
     * Called when a execute job link has been clicked in the datatable, will execute the job.
     * 
     * @method executeJob
     * @param {Object} data
     */
    onActionExecuteJob: function Jobs_onActionExecuteJob(data)
    {
      this.services.managementService.executeJob(data.id);
    },

    onExecuteJobSuccess: function Jobs_onExecuteJobSuccess(result)
    {      
      // Success message will be automatically displayed by service but reload to update status
      this.widgets.dataTable.reload();
    },
    
    onExecuteJobFailure: function Jobs_onExecuteJobFailure(result)
    {      
      // Failure message will be automatically displayed by service  but reload to update status
      this.widgets.dataTable.reload();
    },
    
    onViewException: function Jobs_onViewException(data)
    {
      Activiti.widget.PopupManager.displayError(data.exceptionMessage);
    }
    
  });

})();