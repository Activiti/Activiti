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
 * Activiti.component.TaskList
 *
 * Will display a list of tasks based on the filter from the Activiti.event.TASKS_FILTER_CLICKED event.
 * Will also let user clain and complete taks in the list.
 *
 * @namespace Activiti
 * @class Activiti.component.TaskList
 */
(function()
{
  /**
   * YUI Library aliases
   */
  var Dom = YAHOO.util.Dom,
      Selector = YAHOO.util.Selector,
      Event = YAHOO.util.Event,
      Pagination = Activiti.util.Pagination,
      $html = Activiti.util.encodeHTML;

  /**
   * TaskList constructor.
   *
   * @param {String} htmlId The HTML id of the parent element
   * @return {Activiti.component.TaskList} The new component.TaskList instance
   * @constructor
   */
  Activiti.component.TaskList = function TaskList_constructor(htmlId)
  {
    Activiti.component.TaskList.superclass.constructor.call(this, "Activiti.component.TaskList", htmlId);

    // Create new service instances and set this component to receive the callbacks
    this.services.taskService = new Activiti.service.TaskService(this);

    // Listen for events that interest this component
    this.onEvent(Activiti.event.selectTaskFilter, this.onSelectTaskFilterEvent);
    this.onEvent(Activiti.service.ProcessService.event.startProcessSuccess, this.refreshTaskList);
    this.onEvent(Activiti.service.TaskService.event.claimTaskSuccess, this.refreshTaskList);
    this.onEvent(Activiti.service.TaskService.event.completeTaskSuccess, this.refreshTaskList);

    return this;
  };

  YAHOO.extend(Activiti.component.TaskList, Activiti.component.Base,
  {

    /**
     * Fired by YUI when parent element is available for scripting.
     * Template initialisation, including instantiation of YUI widgets and event listener binding.
     *
     * @method onReady
     */
    onReady: function TaskList_onReady()
    {
      /**
       * Create a data table that supports pagination and browser back buttons that will call:
       * onDataTableCreateURL
       * onDataTableRenderCell<FieldKey>
       */
      this.widgets.dataTable = new Activiti.widget.DataTable(this.id + "-task-list",
        this,
        [ { event: Activiti.event.selectTaskFilter, value: {} }],
        this.id + "-datatable",
        [ this.id + "-paginator" ],
        [ "id", "name", "description", "priority", "assignee", "executionId", "formResourceKey" ],
        [
          { key:"id", label: "Select", sortable: false, width: 30 },
          { key:"assignee", label: "Select", sortable: false, width: 30 },
          { key:"name", label: "Select", sortable: false },
          { key:"priority", label: "Select", sortable: false, width: 200 }
        ]
      );
		var taskId = Activiti.util.getQueryStringParameter("taskId");
		if (taskId != null) 
		{
			new Activiti.widget.CompleteTaskForm(this.id + "-completeTaskForm", taskId, null);
		}
    },

    /**
     * Will refresh the current list of tasks since a new tasks could have been created after the process was started.
     *
     * @method refreshTaskList
     * @param event {string} The event
     * @param eventArgs {object} The event args
     */
    refreshTaskList: function TaskList_refreshTaskList(event, eventArgs)
    {
      this.widgets.dataTable.reload();
    },

    /**
     * Changes the title
     * The data table will handle it self.
     *
     * @method onSelectTaskFilterEvent
     * @param event
     * @param args
     */
    onSelectTaskFilterEvent: function TaskFilters_onSelectTaskFilterEvent(event, args) {
      // Display the filter as a header
      var filter = this.getEventValue(args),
        headerEl = Selector.query("h1", this.id, true);
      if (filter["assignee"]) {
        headerEl.innerHTML = this.msg("label.filter.assignee");
      }
      else if (filter["candidate"]) {
        headerEl.innerHTML = this.msg("label.filter.candidate");
      }
      else if (filter["candidate-group"]) {
        var groupName =  Activiti.constants.GROUPS.assignment[filter["candidate-group"]];
        headerEl.innerHTML = this.msg("label.filter.candidate-group", $html(groupName));
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
    onDataTableCreateURL: function TaskList_onDataTableCreateURL(dataTable, eventName, eventValue) {
      if (eventValue) {
        if (!eventValue[Pagination.SORT]) {
          // Set default sort order if not provided
          eventValue[Pagination.SORT] = "id";
        }
        return this.services.taskService.loadTasksURL(eventValue);
      }
      else {
        return null;
      }

    },

    /**
     * Activiti.widget.DataTable-callback to render the task icon.
     *
     * @method onDataTableRenderCellId
     * @param dataTable {Activiti.widget.DataTable} The data table that is invoking the callback
     * @param el The cell element
     * @param oRecord The data record
     * @param oColumn the data table column
     * @param oData the cell data
     */
    onDataTableRenderCellId: function TaskList_onDataTableRenderCellId(dataTable, el, oRecord, oColumn, oData) {
      el.innerHTML = '<span class="task" title="' + this.msg('tooltip.task', $html(oRecord.getData().id)) + '">&nbsp;</span>';
    },

    /**
     * Activiti.widget.DataTable-callback to render the assignee icon.
     *
     * @method onDataTableRenderCellAssignee
     * @param dataTable {Activiti.widget.DataTable} The data table that is invoking the callback
     * @param el The cell element
     * @param oRecord The data record
     * @param oColumn the data table column
     * @param oData the cell data
     */
    onDataTableRenderCellAssignee: function TaskList_onDataTableRenderCellAssignee(dataTable, el, oRecord, oColumn, oData) {
      if (oRecord.getData().assignee) {
        el.innerHTML = '<span class="user-task-assigned" title="' + this.msg("tooltip.assigned", $html(oRecord.getData().assignee)) + '">&nbsp;</span>';
      }
      else {
        el.innerHTML = '<span class="user-task-unassigned" title="' + this.msg("tooltip.unassigned") + '">&nbsp;</span>';
      }
    },

    /**
     * Activiti.widget.DataTable-callback to render the name & description.
     *
     * @method onDataTableRenderCellName
     * @param dataTable {Activiti.widget.DataTable} The data table that is invoking the callback
     * @param el The cell element
     * @param oRecord The data record
     * @param oColumn the data table column
     * @param oData the cell data
     */
    onDataTableRenderCellName: function TaskList_onDataTableRenderCellName(dataTable, el, oRecord, oColumn, oData) {
      var task = oRecord.getData(),
        header = '<h3>' + $html(task.id) + ' | ' + $html(task.name) + '</h3>',
        description = '<div class="">' + $html(task.description) + '</div>';
      el.innerHTML = header + description;
    },

    /**
     * Activiti.widget.DataTable-callback to render the assignee actions.
     *
     * @method onDataTableRenderCellPriority
     * @param dataTable {Activiti.widget.DataTable} The data table that is invoking the callback
     * @param el The cell element
     * @param oRecord The data record
     * @param oColumn the data table column
     * @param oData the cell data
     */
    onDataTableRenderCellPriority: function TaskList_onDataTableRenderCellPriority(dataTable, el, oRecord, oColumn, oData) {
      var task = oRecord.getData(),
          action = (task.assignee == Activiti.constants.USERNAME ? "complete" : "claim"),
			 labelString = (action === "complete" && task.formResourceKey !== null ? "completeForm" : action);
      var actionButton = new YAHOO.widget.Button({
        label: this.msg("task.action." + labelString),
        id: Activiti.util.generateDomId(),
        container: el
      });
      actionButton.on("click", this.onTaskActionClick, {
        action: action,
        taskId: task.id,
        button: actionButton
      }, this);
    },

    /**
     * Called when an task action button has been clicked
     *
     * @method onTaskActionClick
     * @param e The click event
     * @param obj The callback object with task information
     */
    onTaskActionClick: function TaskList_onTaskActionClick(e, obj) {
		if (obj.action == "complete") {
        new Activiti.widget.CompleteTaskForm(this.id + "-completeTaskForm", obj.taskId, obj.button);
      }
      else if (obj.action == "claim") {
        obj.button.set("disabled", true);
        this.services.taskService.claimTask(obj.taskId);
      }
    }
  });

})();
