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
 * Activiti.component.TaskFilters
 *
 * Displays filters to use for filtering task searches.
 *
 * Will:
 * - Display a task summary of how many tasks a filter has
 * - Fire an Activiti.event.TASKS_FILTER_CLICKED event when a filter is clicked so other components may use the filter.
 *
 * @namespace Activiti
 * @class Activiti.component.TaskFilters
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
   * TaskFilters constructor.
   *
   * @param {String} htmlId The HTML id of the parent element
   * @return {Activiti.component.TaskFilters} The new component.TaskFilters instance
   * @constructor
   */
  Activiti.component.TaskFilters = function TaskFilters_constructor(htmlId)
  {
    Activiti.component.TaskFilters.superclass.constructor.call(this, "Activiti.component.TaskFilters", htmlId);

    // Create new service instances and set this component to receive the callbacks
    this.services.taskService = new Activiti.service.TaskService(this);    
    this.onEvent(Activiti.service.ProcessService.event.START_PROCESS_SUCCESS, this.refreshTaskSummary);
    this.onEvent(Activiti.service.TaskService.event.CLAIM_TASK_SUCCESS, this.refreshTaskSummary);
    this.onEvent(Activiti.service.TaskService.event.COMPLETE_TASK_SUCCESS, this.refreshTaskSummary);

    return this;
  };

  YAHOO.extend(Activiti.component.TaskFilters, Activiti.component.Base,
  {

    /**
     * Fired by YUI when parent element is available for scripting.
     * Template initialisation, including instantiation of YUI widgets and event listener binding.
     *
     * @method onReady
     */
    onReady: function TaskFilters_onReady()
    {
      // Get task summary, onLoadTaskSummarySuccess will be called because of naming convention
      this.services.taskService.loadTaskSummary(Activiti.constants.USERNAME);

      // Attach event listeners on all filter links
      var filterEls = Selector.query("li.filter a", this.id);
      Event.addListener(filterEls, "click", this.onFilterClick, null, this);

      // Simulate the first filter being clicked
      if (filterEls.length > 0) {
        this.onFilterClick(null, filterEls[0]);
      }
    },

    /**
     * Will display the task summary
     *
     * @method onGetTaskSummary
     * @param response {object} The callback response
     * @param obj {object} Helper object
     */
    onLoadTaskSummarySuccess: function TaskFilters_TaskService_onGetTaskSummarySuccess(response, obj)
    {
      // Take rest api response and display the task count summary next to the filter labels
      var summary = response.json;
      this._displayTaskCount("assigned", summary.assigned.total);
      this._displayTaskCount("unassigned", summary.unassigned.total);
      for (var group in summary.unassigned.groups) {
        this._displayTaskCount("unassigned-group-" + group, summary.unassigned.groups[group]);
      }
    },

    /**
     * Displays the number of tasks inside paranthesis
     *
     * @method _displayTaskCount
     * @param name {string} The name of the task count
     * @param taskCount {int} The number of tasks
     * @private
     */
    _displayTaskCount: function TaskFilters__displayTaskCount(name, taskCount) {
      // Find the span element and display the count inside it
      var spanEl = Selector.query("li.filter em." + name, this.id, true);
      if (spanEl) {
        var newValue = "(" + taskCount + ")",
          oldValue = spanEl.innerHTML;
        spanEl.innerHTML = newValue;
        if (oldValue != null && oldValue.length > 0 && oldValue[0] == '(' && oldValue != newValue) {
          Activiti.util.Anim.pulse(Selector.query("a", spanEl.parentNode, true));
        }
      }      
    },

    /**
     * Will fire a Activiti.event.TASKS_FILTER_CLICKED event so other components may filter tasks
     *
     * @method onFilterClick
     * @param e {object} The click event
     * @param el {object} The clicked filter link
     */
    onFilterClick: function TaskFilters_onFilterClick (e, el)
    {
      // Get target
      el = el ? el : Event.getTarget(e);

      // Highlight 
      Activiti.util.toggleClass(Selector.query("li.filter a", this.id), el, "current");
      
      // Parse the filter string form the rel attribute and turn it into an object
      var filter = {},
        filterPairs = el.getAttribute("rel").split("&"),
        filterPair;
      for (var i = 0, il = filterPairs.length; i < il; i++) {
        filterPair = filterPairs[i].split("=");
        if (filterPair.length == 2)
        {
          filter[filterPair[0]] =  filterPair[1];
        }
      }

      // Fire the event
      this.fireEvent(Activiti.event.TASKS_FILTER_CLICKED, {
        title: el.getAttribute("title"),
        filter: filter
      });

      if (e) {
        YAHOO.util.Event.preventDefault(e); 
      }
    },


    /**
     * Called when events that affect the list of tasks has been fired.
     * Will refresh the current task summary.
     *
     * @method refreshTaskSummary
     * @param event {string} The event
     * @param eventArgs {object} The event args
     */
    refreshTaskSummary: function TaskList_refreshTaskSummary(event, eventArgs)
    {
      this.services.taskService.loadTaskSummary(Activiti.constants.USERNAME);
    }

  });

})();
