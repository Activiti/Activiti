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
    this.taskFilter = null;

    // Listen for events that interest this component
    this.onEvent(Activiti.event.TASKS_FILTER_CLICKED, this.onTasksFilterClickedEvent);
    this.onEvent(Activiti.service.ProcessService.event.START_PROCESS_SUCCESS, this.refreshTaskList);
    this.onEvent(Activiti.service.TaskService.event.CLAIM_TASK_SUCCESS, this.refreshTaskList);
    this.onEvent(Activiti.service.TaskService.event.COMPLETE_TASK_SUCCESS, this.refreshTaskList);

    return this;
  };

  YAHOO.extend(Activiti.component.TaskList, Activiti.component.Base,
  {

    /**
     * The current task filter in use
     */
    taskFilter: null,

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
      this.widgets.dataTable = new Activiti.widget.DataTable(
        this,
        this.id + "-datatable",
        [ this.id + "-paginator" ],
        [ "id", "name", "description", "priority", "assignee", "executionId" ],
        [
          { key:"id", label: "Select", sortable: false, width: 30 },
          { key:"assignee", label: "Select", sortable: false, width: 30 },
          { key:"name", label: "Select", sortable: false },
          { key:"priority", label: "Select", sortable: false, width: 200 }
        ]
      );
    },

    /**
     * Called when the global event for a task filter click has been fired in another component.
     * Will set that filter on this component and call refresh so the listed tasks are refreshed.
     *
     * @method onTasksFilterClickedEvent
     * @param event {string} The event
     * @param eventArgs {object} The event args
     */
    onTasksFilterClickedEvent: function TaskList_onTasksFilterClickedEvent(event, eventArgs)
    {
      this.taskFilter = eventArgs[1].value;
      Selector.query("h1", this.id, true).innerHTML = $html(this.taskFilter.title);
      if (this.widgets.dataTable)
      {
        // Fire
        this.widgets.dataTable.load(this._getURL(), true);
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
      this.widgets.dataTable.load(this._getURL(), true);
    },

    /**
     * Activiti.widget.DataTable-callback to construct the url to use to load data into the data table.
     *
     * @method onDataTableCreateURL
     * @param startIndex THe pagination start position
     * @param sortKey THe column to sort the data by
     * @param dir The direction to sort the data in
     * @param pageSize the size of the response
     * @return A url, based on the current filter, to use when loading data into the data table
     */
    onDataTableCreateURL: function TaskList_onDataTableCreateURL(startIndex, sortKey, dir, pageSize) {
      if (this.taskFilter) {
        // Persist values from paginator
        this.taskFilter.filter.startIndex = startIndex;
        this.taskFilter.filter.pageSize = pageSize || 10;
        this.taskFilter.filter.sort = sortKey || "id";
        this.taskFilter.filter.dir = (dir) ? dir.substring(7) : "asc";

        // Get the url to request new tasks
        return this._getURL();
      }
      else {
        return null;
      }
    },

    /**
     * Activiti.widget.DataTable-callback to render the task icon.
     *
     * @method onDataTableRenderCellId
     * @param el The cell element
     * @param oRecord The data record
     * @param oColumn the data table column
     * @param oData the cell data
     */
    onDataTableRenderCellId: function TaskList_onDataTableRenderCellId(el, oRecord, oColumn, oData) {
      el.innerHTML = '<span class="task" title="' + this.msg('tooltip.task', $html(oRecord.getData().id)) + '">&nbsp;</span>';
    },

    /**
     * Activiti.widget.DataTable-callback to render the assignee icon.
     *
     * @method onDataTableRenderCellAssignee
     * @param el The cell element
     * @param oRecord The data record
     * @param oColumn the data table column
     * @param oData the cell data
     */
    onDataTableRenderCellAssignee: function TaskList_onDataTableRenderCellAssignee(el, oRecord, oColumn, oData) {
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
     * @param el The cell element
     * @param oRecord The data record
     * @param oColumn the data table column
     * @param oData the cell data
     */
    onDataTableRenderCellName: function TaskList_onDataTableRenderCellName(el, oRecord, oColumn, oData) {
      var task = oRecord.getData(),
        header = '<h3>' + $html(task.id) + ' | ' + $html(task.name) + '</h3>',
        description = '<div class="">' + $html(task.description) + '</div>';
      el.innerHTML = header + description;
    },

    /**
     * Activiti.widget.DataTable-callback to render the assignee actions.
     *
     * @method onDataTableRenderCellPriority
     * @param el The cell element
     * @param oRecord The data record
     * @param oColumn the data table column
     * @param oData the cell data
     */
    onDataTableRenderCellPriority: function TaskList_onDataTableRenderCellPriority(el, oRecord, oColumn, oData) {
      var task = oRecord.getData(),
          action = (task.assignee == Activiti.constants.USERNAME ? "complete" : "claim");
      var actionButton = new YAHOO.widget.Button({
        label: this.msg("task.action." + action),
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
      obj.button.set("disabled", true);
      if (obj.action == "complete") {
        this.services.taskService.completeTask(obj.taskId);
      }
      else if (obj.action == "claim") {
        this.services.taskService.claimTask(obj.taskId);
      }
    },

    /**
     * Creates the url, based on the current filters, to use to load the data into the data table.
     *
     * @method _getURL
     * @return the url to load tasks into the data table.
     * @Œprivate
     */
    _getURL: function TaskList__getURL() {
      return this.services.taskService.loadTasksURL(this.taskFilter.filter);
    }

  });

})();
