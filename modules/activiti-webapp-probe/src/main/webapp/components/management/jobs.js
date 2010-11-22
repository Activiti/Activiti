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
    
    // Listen for events that interest us
    this.onEvent(Activiti.event.jobAction, this.onJobAction);
    
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
        [
          { event: Activiti.event.displayJobs, value: {} },
          { event: "linkClickEvent", subscribe: true, trigger: Activiti.event.jobAction}
        ],
        this.id + "-datatable",
        [ this.id + "-paginator" ],
        [
          {key:"exceptionMessage", label: this.msg("jobs.label.status")},
          {key:"id", label: this.msg("jobs.label.id"), sortable:true},
          {key:"executionId", label: this.msg("jobs.label.executionId"), sortable:true},
          {key:"retries", label: this.msg("jobs.label.retries"), sortable:true},
          {key:"processInstanceId", label: this.msg("jobs.label.processInstanceId"), sortable:true},
          {key:"dueDate", label: this.msg("jobs.label.dueDate"), sortable:true},
          {key:"action", label: this.msg("jobs.label.actions")},
          {key:"select", label: this.msg("jobs.label.select")}
        ]
      );
      
      // Needed to load data and set up other events
      if (!Activiti.event.isInitEvent(Activiti.event.displayJobs)) 
      {
        this.fireEvent(Activiti.event.displayJobs, {}, null);
      }   
      
      // Set up button events
      this.executeButton = new YAHOO.widget.Button(this.id + "-execute", 
      {
        onclick: 
        {
          fn: this.onMultiExecuteClick,
          obj:{},
          scope: this
        }
      });

    },
    
    onJobAction: function Jobs_onJobAction(event, args, me)
    {
     var el = args[1].e.target, 
        commonClasses = "jobAction ",
        jobId = el.rel;
      switch (el.className.replace(commonClasses, ""))
      {
        case "executeJob":
          me.executeJob(jobId)
          break;
        case "viewException":
          var exception = Dom.getElementsByClassName("exception", "span", el)[0].innerHTML
          me.onViewException(exception);
          break;
        default:
      }
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
       actions.push('<a href="#exception?jobId='+data.id+'" rel="' + data.id + '" class="jobAction viewException">' + this.msg("jobs.link.view-exception") + '<span class="exception">' + data.exceptionMessage + '</span></a>'); 
      };
      actions.push('<a href="#execute?jobId='+data.id+'" rel="' + data.id + '" class="jobAction executeJob">' + this.msg("jobs.link.execute") + '</a>');
      el.innerHTML = actions.join("")
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
        this.executeJobs(selectIds)
      } else 
      {
        Activiti.widget.PopupManager.displayMessage({
          text: this.msg("jobs.message.none-selected")
        })
      } 
    },

    /**
     * 
     * Send a single job to the execution queue
     * 
     * @method executeJob
     * @param {String} jobId
     */
    executeJob: function Jobs_executeJob(jobId) 
    {
      this.services.managementService.executeJob(jobId);
    },
    
    /**
     * 
     * Send Multiple Jobs to the execution queue
     * 
     * @method executeJobs
     * @param {Array} selectedIds - an array of JobIds as strings.
     */
    executeJobs: function Jobs_executeJobs(selectedIds) 
    {
      this.services.managementService.executeJobs(selectedIds);
    },
    
    onExecuteJobSuccess: function Jobs_onExecuteJobSuccess(result)
    {      
      Activiti.widget.PopupManager.displayMessage({
        text: this.msg("jobs.message.execute.success")
      });
      this.widgets.dataTable.reload();
    },
    
    onExecuteJobFailure: function Jobs_onExecuteJobFailure(result)
    {      
      //Failure message automatically displayed.
      this.widgets.dataTable.reload();
    },
    
    onViewException: function Jobs_onViewException(exception)
    {
      Activiti.widget.PopupManager.displayError(exception);
    }
    
  });

})();