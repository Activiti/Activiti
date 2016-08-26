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
'use strict';

angular.module('activitiApp')
  .controller('TasksController', ['$rootScope', '$scope', '$translate', '$timeout','$location', '$modal', '$popover', 'appResourceRoot', 'CommentService', 'TaskService', '$routeParams', 'AppDefinitionService',
        function ($rootScope, $scope, $translate, $timeout, $location, $modal, $popover, appResourceRoot, CommentService, TaskService, $routeParams, AppDefinitionService) {

            // Ensure correct main page is set
            $rootScope.setMainPageById('tasks');

            $scope.model = {
                page: 0,
                initialLoad: false
            };

            // Init sort options
            $scope.model.sorts = [
                { id: 'created-desc', title: 'TASK.FILTER.CREATED-DESC' },
                { id: 'created-asc', title: 'TASK.FILTER.CREATED-ASC' },
                { id: 'due-desc', title: 'TASK.FILTER.DUE-DESC' },
                { id: 'due-asc', title: 'TASK.FILTER.DUE-ASC' }
            ];

            // Init state options
            $scope.model.stateFilterOptions  = [
                { id: 'open', title: 'TASK.FILTER.STATE-OPEN' },
                { id: 'completed', title: 'TASK.FILTER.STATE-COMPLETED' }
            ];

            // Init assignment options
            $scope.model.assignmentOptions = [
                { id: 'involved', title: $translate.instant('TASK.FILTER.ASSIGNMENT-INVOLVED') },
                { id: 'assignee', title: $translate.instant('TASK.FILTER.ASSIGNMENT-ASSIGNEE') },
                { id: 'candidate', title: $translate.instant('TASK.FILTER.ASSIGNMENT-CANDIDATE') }
            ];

            if ($scope.account && $scope.account.groups && $scope.account.groups.length > 0) {
                for (var i=0; i < $scope.account.groups.length; i++) {
                    if ($scope.account.groups[i].type == 1) {
                        $scope.model.assignmentOptions.push({ id: 'group_' + $scope.account.groups[i].id, title: $translate.instant('TASK.FILTER.ASSIGNMENT-GROUP', $scope.account.groups[i]) });
                    }
                }
            }

            // Sets defaults for all filters
            $scope.resetFilters = function(forcePropagateToRootScope) {

                $scope.model.page = 0;

                // Init empty filter model
                $scope.model.filter = {
                    loading: false,
                    expanded: false,
                    param: {
                        state: $scope.model.stateFilterOptions[0],
                        nonDefaultFilter: false
                    }
                };

                // Defaults
                $scope.model.filter.param.sort = $scope.model.sorts[0].id;
                $scope.model.filter.param.assignment = $scope.model.assignmentOptions[0].id;
                $scope.model.filter.param.processDefinitionId = 'default';

                $scope.deploymentKey = $routeParams.deploymentKey;
                $scope.missingAppdefinition = $scope.deploymentKey === false;

                // In case of viewing tasks in an app-context, need to make filter aware of this
                $scope.model.filter.param.deploymentKey = $scope.deploymentKey;

                // Propagate to root filter
                if (forcePropagateToRootScope === true || ($rootScope.taskFilter === null && $rootScope.taskFilter === undefined)) {
                    $rootScope.taskFilter = { param: $scope.model.filter.param };
                }
            };

            $scope.resetFilters(); // First time: init defaults


            // The filter is stored on the rootScope, which allows the user to switch back and forth without losing the filter.
            if ($rootScope.taskFilter !== null && $rootScope.taskFilter !== undefined) {
                $scope.model.filter.param = $rootScope.taskFilter.param;
            } else {
                $rootScope.taskFilter = { param: $scope.model.filter.param };
            }

            // Update app on rootScope. If app id present, it will fetch definition if not already fetched to update view and navigation accordingly
            AppDefinitionService.setActiveDeploymentKey($scope.deploymentKey);

            // Callback for state selection widget
            $scope.selectStateFilter = function (state) {
                if(state !== $scope.model.filter.param.state) {
                    $scope.model.filter.param.state = state;
                    $scope.collapseFilter();
                    $scope.resetPaging();
                    $scope.refreshFilter();
                }
            };

            $scope.sortChanged = function() {
                $scope.resetPaging();
                $scope.refreshFilter();
            };

            $scope.nextPage = function() {
                $scope.model.page = $scope.model.page +1;
                $scope.refreshFilter();
            };

            $scope.resetPaging = function() {
                $scope.model.page = 0;
            };

            $scope.refreshFilter = function () {
                $scope.model.filter.loading = true;

                var params = $scope.model.filter.param;
                params.nonDefaultFilter = false;

                // Assignee
                var data = {
                    assignee: $rootScope.account.id,
                    page: $scope.model.page
                };

                // Text filter
                if (params.text) {
                    data.text = params.text;
                    params.nonDefaultFilter = true;
                }

                // State folder
                if (params.state) {
                    data.state = params.state.id;

                    if (params.state.id !== 'open') {
                        params.nonDefaultFilter = true;
                    }
                }

                // Assignment
                if (params.assignment) {
                    data.assignment = params.assignment;

                    if (params.assignment !== 'involved') {
                        params.nonDefaultFilter = true;
                    }
                }

                // Process definition
                if (params.processDefinitionId && params.processDefinitionId !== 'default') { // default = empty choice
                    data.processDefinitionId = params.processDefinitionId;
                    params.nonDefaultFilter = true;
                }

                // App definition
                if (params.deploymentKey) {
                    data.deploymentKey = params.deploymentKey;
                }

                // Sort order
                data.sort = params.sort;


                TaskService.queryTasks(data).then(function (response) {
                    $scope.model.filter.loading = false;

                    if (response.start === 0) {
                        $scope.model.tasks = response.data;

                        if((!response.data || response.data.length == 0) && !params.nonDefaultFilter) {
                            $scope.state= {noOwnTasks: true};
                        } else {
                            $scope.state= {noOwnTasks: false};
                        }

                        $scope.selectedTask = $scope.model.tasks[0];

                    } else {
                        for (var taskIndex=0; taskIndex<response.data.length; taskIndex++) {
                            $scope.model.tasks.push(response.data[taskIndex]);
                        }
                        $scope.state= {noOwnTasks: false};

                        if (!$scope.selectedTask) {
                            $scope.selectedTask = response.data[0];
                        }
                    }
                    $scope.model.hasNextPage = (response.start + response.size < response.total);


                    // If coming from the process page the root.selectedTaskId will be set
                    var taskIsPartOfCurrentList = false;
                    if ($rootScope.root.selectedTaskId) {
                        for (var i = 0; i < response.data.length; i++) {
                            if (response.data[i].id == $rootScope.root.selectedTaskId) {
                                taskIsPartOfCurrentList = true;
                                $scope.selectedTask = response.data[i];
                                break;
                            }
                        }

                        // if the task isn't part of the currently showed list,
                        // the details are still shown
                        if (!taskIsPartOfCurrentList) {
                            // This is kinda cheating, but this way no additional fetch is needed
                            $scope.selectedTask = { id:  $rootScope.root.selectedTaskId};
                        }

                        // Always remove the root task id when it was used (otherwise will screw up next calls)
                        $rootScope.root.selectedTaskId = undefined;

                    } else {
                        if (!$scope.selectedTask) {
                            $scope.selectedTask = $scope.model.tasks[0];
                        }
                    }

                });
            };

            $scope.selectTask = function (task) {
                $scope.selectedTask = task;
            };

            $scope.createTask = function () {
                // Create popover
                if (!$scope.createTaskPopover) {
                    $scope.newTask = {
                        name: 'New task'
                    };

                    $scope.createTaskPopover = $popover(angular.element('#toggle-create-task'), {
                        template: appResourceRoot+ 'views/popover/create-task-popover.html',
                        placement: 'bottom-left',
                        show: true,
                        scope: $scope
                    });

                    $scope.createTaskPopover.$scope.$on('tooltip.hide', function () {
                        $scope.createTaskPopover.$scope.$destroy();
                        $scope.createTaskPopover.destroy();
                        $scope.createTaskPopover = undefined;

                        $scope.newTask = undefined;
                    });
                }
            };

            $scope.confirmTaskCreation = function (newTask) {

                if(!newTask) {
                    newTask = $scope.newTask;
                }
                if (newTask && newTask.name) {
                    var taskData = {
                        name: newTask.name,
                        description: newTask.description,
                    };

                    if ($rootScope.activeAppDefinition) {
                        taskData.category = '' + $rootScope.activeAppDefinition.id;
                    }

                    newTask.loading = true;
                    TaskService.createTask(taskData).then(function (task) {
                        newTask.loading = false;

                        if($scope.createTaskPopover) {
                            $scope.createTaskPopover.$scope.$destroy();
                            $scope.createTaskPopover.destroy();
                            $scope.createTaskPopover = undefined;
                        }

                        $rootScope.addAlertPromise($translate('TASK.ALERT.CREATED', task));
                    });
                }
            };

            $scope.expandFilter = function () {
                $scope.model.filter.expanded = true;
            };

            $scope.collapseFilter = function () {
                $scope.model.filter.expanded = false;
            };


            $scope.$on('task-completed', function (event, data) {
                $rootScope.addAlertPromise($translate('TASK.ALERT.COMPLETED', data));
                if (data && data.taskId == $scope.selectedTask.id) {
                    // Current task has been completed. Refresh the filter
                    $scope.selectedTask = undefined;
                    $scope.resetPaging();
                    $scope.refreshFilter();
                }
            });
            
            $scope.$on('task-saved', function (event, data) {
                $rootScope.addAlertPromise($translate('TASK.ALERT.SAVED', data));
            });

            $scope.$on('task-completed-error', function (event, data) {
                $rootScope.addAlert(data.error.message, 'error');
            });
            
            $scope.$on('task-save-error', function (event, data) {
                $rootScope.addAlert(data.error.message, 'error');
            });

            // Refreshing list when new task is created
            $scope.$on("new-task-created", function (event, task) {
                // New tasks should always be on top, hence the sorting is set to last created
                $scope.resetFilters(true);
                $scope.resetPaging();
                $scope.refreshFilter();
            });

            // Load process definitions for the filter dropdown
            $rootScope.loadProcessDefinitions($scope.deploymentKey);

            // Add a watch, so we can
            var unregisterWatch = $scope.$watch('root.processDefinitions', function(data) {
                if (data !== null && data != undefined && data.length > 0) {
                    var processDefinitions = data;
                    processDefinitions.splice(0, 0, {id:'default', name: ''});
                    $scope.model.processDefinitions = processDefinitions;
                    unregisterWatch();
                }
            });

            // Initial load
            $scope.refreshFilter();

  }]);
