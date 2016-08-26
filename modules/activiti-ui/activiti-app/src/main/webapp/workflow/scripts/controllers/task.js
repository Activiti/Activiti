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
    .controller('TaskController', ['$rootScope', '$scope', '$translate', '$timeout','$location', '$modal', '$popover', 'appResourceRoot', 'CommentService', 'TaskService', '$routeParams', 'AppDefinitionService',
        function ($rootScope, $scope, $translate, $timeout, $location, $modal, $popover, appResourceRoot, CommentService, TaskService, $routeParams, AppDefinitionService) {

            // Ensure correct main page is set
            $rootScope.setMainPageById('tasks');

            $scope.selectedTask = { id: $routeParams.taskId };

            $scope.deploymentKey = $routeParams.deploymentKey;

            $scope.$on('task-completed', function (event, data) {
                $rootScope.addAlertPromise($translate('TASK.ALERT.COMPLETED', data));
                $scope.openTasks();
            });

            $scope.openTasks = function(task) {
                var path='';
                if($rootScope.activeAppDefinition && !ACTIVITI.CONFIG.integrationProfile) {
                    path = "/apps/" + $rootScope.activeAppDefinition.id;
                }
                $location.path(path + "/tasks");
            };
        }]
);

angular.module('activitiApp')
  .controller('TaskDetailController', ['$rootScope', '$scope', '$translate', '$http','$location', '$routeParams', 'appResourceRoot', 'CommentService', 'TaskService', 'FormService', 'RelatedContentService', '$timeout', '$modal', '$popover',
        function ($rootScope, $scope, $translate, $http, $location, $routeParams, appResourceRoot, CommentService, TaskService, FormService, RelatedContentService, $timeout, $modal, $popover) {

    $scope.model = {
        // Indirect binding between selected task in parent scope to have control over display
        // before actual selected task is switched
	    task: $scope.selectedTask,
	    completeButtonDisabled: false,
	    claimButtonDisabled: false,
        uploadInProgress: false
	};

    $scope.activeTab = 'form';

    $scope.model.involvementSummary = {
        loading: false
    };

    $scope.model.contentSummary = {
        loading: false
    };

    $scope.model.commentSummary = {
        loading: false
    };

    $scope.resetModel = function() {
        // Reset tabs
        $scope.taskTabs = [];
        if ($scope.model.task.formKey != null) {
            $scope.taskTabs.push(
                {
                    'id': 'form',
                    'title': 'TASK.TITLE.FORM'
                }
            );
            $scope.activeTab = 'form';
        } else {
            $scope.activeTab = 'details';
        }

        $scope.taskTabs.push( {
            'id': 'details',
            'title': 'TASK.TITLE.DETAILS'
        });

        // Reset summary model
        $scope.model.involvementSummary = {
            loading: true
        };

        $scope.model.contentSummary = {
            loading: true
        };

        $scope.model.commentSummary = {
            loading: true
        };

        $scope.model.content = undefined;
        $scope.model.comments = undefined;

        $timeout(function() {
            // Force refresh of all auto-height components as the tabs can be hidden or shown
            $rootScope.window.forceRefresh = true;
        }, 100);

        var today = new Date();
        $scope.today = new Date(today.getFullYear(), today.getMonth(), today.getDate() , 0, 0, 0, 0);
    };

    $scope.showPeople = function() {
        $scope.activeTab = 'details';
    };
    $scope.showContent = function() {
        $scope.activeTab = 'details';
    };
    $scope.showComments= function() {
        $scope.activeTab = 'details';
    };
    $scope.toggleForm= function() {
        if($scope.activeTab == 'form') {
            $scope.activeTab = 'details';
        } else {
            $scope.activeTab = 'form';
        }
    };

    // The selected task is set by the parent, eg in tasks.js
    $scope.$watch('selectedTask', function(newValue) {
        if(newValue && newValue.id) {
            $scope.model.taskUpdating = true;
            $scope.model.task = newValue;
            if ($scope.model.task.formKey) {
                $scope.resetModel();
            }
            $scope.getTask(newValue.id);
        } else {
            // Reset whole model to make sure nothing is left behind in case a new task will
            // be selected in the future
            $scope.model = {};
        }
    });

	// Ensure correct main page is set
    $rootScope.setMainPageById('tasks');


    $scope.setTaskAssignee = function(user) {
        var alertData = {
            firstName: user.firstName,
            lastName: user.lastName,
            taskName: $scope.model.task.name
        };

        TaskService.assignTask($scope.model.task.id, user.id).then(function(data) {
            $rootScope.addAlertPromise($translate('TASK.ALERT.ASSIGNED', alertData));
            $scope.model.task = data;
        });
    };

    $scope.setTaskAssigneeByEmail = function(email) {
        TaskService.assignTaskByEmail($scope.model.task.id, email).then(function() {
            $scope.model.task.assignee = {email: email}; // Faking a user (since it will only be an email address)
        });
    };

    $scope.involvePerson = function (user) {
        var alertData = {
            firstName: user.firstName,
            lastName: user.lastName,
            taskName: $scope.model.task.name
        };

        TaskService.involveUserInTask(user.id, $scope.model.task.id).then(function() {
            $rootScope.addAlertPromise($translate('TASK.ALERT.PERSON-INVOLVED',
                alertData));

            if(!$scope.model.task.involvedPeople) {
                $scope.model.task.involvedPeople = [user];
            } else {
                $scope.model.task.involvedPeople.push(user);
            }
        });
    };

    $scope.involvePersonByEmail = function(email) {
        TaskService.involveUserInTaskByEmail(email, $scope.model.task.id).then(function() {
            if(!$scope.model.task.involvedPeople) {
                $scope.model.task.involvedPeople = {email: email};
            } else {
                $scope.model.task.involvedPeople.push({email: email});
            }
        });
    };

    $scope.removeInvolvedUser = function (user) {
        var alertData = {
            firstName: user.firstName,
            lastName: user.lastName,
            taskName: $scope.model.task.name
        };

        TaskService.removeInvolvedUserInTask(user, $scope.model.task.id).then(function() {
            $rootScope.addAlertPromise($translate('TASK.ALERT.PERSON-NO-LONGER-INVOLVED',
                alertData));

            $scope.model.task.involvedPeople.splice($.inArray(user, $scope.model.task.involvedPeople),1);
        });
    };

    $scope.getTask = function(taskId) {
        $scope.model.loading = true;
        $scope.model.formData = undefined;
        $scope.model.hasFormKey = false;
        if ($scope.model.task.formKey) {
            $scope.model.hasFormKey = true;
        }

        $http({method: 'GET', url: ACTIVITI.CONFIG.contextRoot + '/app/rest/tasks/' + taskId}).
            success(function(response, status, headers, config) {

                // Do not replace the model, as it's still used in the task-list
                angular.extend($scope.model.task, response);

                $scope.model.loading = false;
                $scope.noSuchTask = false;

                if (!$scope.model.hasFormKey) {
                    $scope.resetModel();
                }

                $scope.loadComments();
                $scope.loadRelatedContent();

                if($scope.model.task.processInstanceId) {
                    $scope.loadProcessInstance();
                } else {
                    $scope.model.processInstance = null;
                }

                $scope.refreshInvolvmentSummary();

                // Loading form already
                if ($scope.model.task.formKey !== null && $scope.model.task.formKey !== undefined) {
                    FormService.getTaskForm($scope.model.task.id).then(function(formData) {
                        $scope.model.formData = formData;
                    });
                } else {
                    $scope.model.formData = undefined;
                }

                $scope.model.taskUpdating = false;
            }).
            error(function(response, status, headers, config) {
                $scope.noSuchTask = true;
            });
    };

    $scope.$watch('model.task.involvedPeople', function(newValue) {
        $scope.refreshInvolvmentSummary();
    }, true);

    $scope.refreshInvolvmentSummary = function() {
        if($scope.model.task) {
            var newValue = $scope.model.task.involvedPeople;
            $scope.model.involvementSummary.loading = false;
            if(newValue && newValue.length > 0) {
                $scope.model.involvementSummary.count = newValue.length;

                if(newValue.length > 8) {
                    $scope.model.involvementSummary.overflow = true;
                    $scope.model.involvementSummary.items = [];

                    for(var i=0; i< 8; i++) {
                        $scope.model.involvementSummary.items.push(newValue[i]);
                    }
                } else {
                    $scope.model.involvementSummary.overflow = false;
                    $scope.model.involvementSummary.items = newValue;
                }

            } else {
                $scope.model.involvementSummary.count = 0;
            }
        }
    };

    $scope.$watch('model.content.data', function(newValue) {
        if($scope.model.task) {
            $scope.model.contentSummary.loading = false;
            if(newValue && newValue.length > 0) {
                $scope.model.contentSummary.count = newValue.length;

                if(newValue.length > 8) {
                    $scope.model.contentSummary.overflow = true;
                    $scope.model.contentSummary.items = [];

                    for(var i=0; i< 8; i++) {
                        $scope.model.contentSummary.items.push(newValue[i]);
                    }
                } else {
                    $scope.model.contentSummary.overflow = false;
                    $scope.model.contentSummary.items = newValue;
                }

            } else {
                $scope.model.contentSummary.count = 0;
            }
        }
    }, true);

    $scope.$watch('model.comments.data', function(newValue) {
       $scope.refreshCommentSummary();
    }, true);

    $scope.refreshCommentSummary = function() {
        if ($scope.model.task) {
            var newValue = $scope.model.comments ? $scope.model.comments.data : undefined;
            $scope.model.commentSummary.loading = false;

            if(newValue) {
                $scope.model.commentSummary.count = newValue.length;
            } else {
                $scope.model.commentSummary.loading = true;
                $scope.model.commentSummary.count = undefined;
            }
        }
    };

    $scope.dragOverContent = function(over) {
        if(over && ! $scope.model.contentSummary.addContent) {
            $scope.model.contentSummary.addContent = true;
        }
    };

    $scope.$watch('model.content.data', function(newValue) {
        if($scope.model.task) {
        }
    }, true);

    $scope.loadComments = function() {
        CommentService.getTaskComments($scope.model.task.id).then(function (data) {
            $scope.model.comments = data;

            $scope.refreshCommentSummary();
        });
    };

    $scope.toggleCreateComment = function() {
        if($scope.model.commentSummary.addComment) {
            $scope.model.commentSummary.newComment = undefined;
        }

        $scope.model.commentSummary.addComment = ! $scope.model.commentSummary.addComment;

        if($scope.model.commentSummary.addComment) {
            $timeout(function() {
                angular.element('.focusable').focus();
            }, 100);

        }
    };

    $scope.toggleCreateContent = function() {
        $scope.model.contentSummary.addContent = ! $scope.model.contentSummary.addContent;
    };

    $scope.onContentUploaded = function(content) {
        if ($scope.model.content && $scope.model.content.data) {
            $scope.model.content.data.push(content);
            RelatedContentService.addUrlToContent(content);
            $scope.model.selectedContent = content;
        }
        $rootScope.addAlertPromise($translate('TASK.ALERT.RELATED-CONTENT-ADDED', content), 'info');
        $scope.toggleCreateContent();
    };

    $scope.onContentDeleted = function(content) {
        if ($scope.model.content && $scope.model.content.data) {
            $scope.model.content.data.forEach(function(value, i, arr){
                if (content === value) {
                    arr.splice(i, 1);
                }
            })
        }
    };

    $scope.selectContent = function (content) {
        if ($scope.model.selectedContent == content) {
            $scope.model.selectedContent = undefined;
        } else {
            $scope.model.selectedContent = content;
        }
    };

    $scope.confirmNewComment = function() {
        $scope.model.commentSummary.loading = true;
        CommentService.createTaskComment($scope.model.task.id, $scope.model.commentSummary.newComment.trim())
            .then(function(comment) {
                $scope.model.commentSummary.newComment = undefined;
                $scope.model.commentSummary.addComment = false;
                $scope.model.commentSummary.loading = false;
                $rootScope.addAlertPromise($translate('TASK.ALERT.COMMENT-ADDED', $scope.model.task));
                $scope.loadComments();
            });
    };

    $scope.$watch('model.task.dueDate', function(newValue, oldValue) {
        if (!$scope.model.taskUpdating && $scope.model.task) {
            // Update task due-date

            if (oldValue === null && newValue === null
                || oldValue === null && newValue === undefined
                || oldValue === undefined && newValue === undefined
                || oldValue === undefined && newValue === null) {
                return;
            }

            // Normalize the date to midnight
            if(newValue && newValue !== undefined && newValue.getHours && newValue.getHours() != 23) {
                newValue.setHours(23);
                newValue.setMinutes(59);
                newValue.setSeconds(59);
                $scope.model.task.dueDate = newValue;
            }

            if (new Date(oldValue).getTime() != new Date(newValue).getTime() || oldValue != null && newValue != null) {
                $scope.model.taskUpdating = true;
                // Explicitly force NULL value when undefined to make sure the null
                // is sent to the service
                var data = {
                    dueDate: newValue ? newValue : null
                };
                TaskService.updateTask($scope.model.task.id, data).then(function(response) {
                    $scope.model.taskUpdating = false;
                });
            }
        }
    });

    $scope.createTaskInline = function() {
        if(!$scope.newTask) {
            $scope.newTask = {
                name: 'New task',
                inline: true
            };
        }
    };

    $scope.createProcess = function() {
        $rootScope.createProcessInstance = true;
        $scope.openProcessInstance();
    };

    $scope.selectProcessDefinition = function (definition) {
        $scope.newProcessInstance.processDefinitionId = definition.id;
        $scope.newProcessInstance.name = definition.name + ' - ' + new moment().format('MMMM Do YYYY');

        $timeout(function () {
            angular.element('#start-process-name').focus();
        }, 20);
    };

    $scope.closeInlineTaskCreation = function($event) {
        $scope.newTask = undefined;
        $event.stopPropagation();
    };

    $scope.completeTask = function() {
        $scope.model.completeButtonDisabled = true;
        TaskService.completeTask($scope.model.task.id);
    };

    $scope.claimTask = function() {
        $scope.model.loading = true;
        $scope.model.claimButtonDisabled = true;
        TaskService.claimTask($scope.model.task.id).then(function(data) {
            // Refetch data on claim success
            $scope.getTask($scope.model.task.id);
        });
    };

    // TODO: move process instance loading to separate service and merge with process.js
    $scope.loadProcessInstance = function() {
        $http({method: 'GET', url: ACTIVITI.CONFIG.contextRoot + '/app/rest/process-instances/' + $scope.model.task.processInstanceId}).
            success(function(response, status, headers, config) {
                $scope.model.processInstance = response;
            }).
            error(function(response, status, headers, config) {
                // Do nothing. User is not allowed to see the process instance
            });
    };

    $scope.openProcessInstance = function(id) {
        $rootScope.root.selectedProcessId = id;
        var path='';
        if($rootScope.activeAppDefinition && !ACTIVITI.CONFIG.integrationProfile) {
            path = "/apps/" + $rootScope.activeAppDefinition.id;
        }
        $location.path(path + "/processes");
    };

    $scope.returnToTaskList = function() {
        var path='';
        if($rootScope.activeAppDefinition && !ACTIVITI.CONFIG.integrationProfile) {
            path = "/apps/" + $rootScope.activeAppDefinition.id;
        }
        $location.path(path + "/tasks");
    };

    // OLD STUFF


    $scope.returnToList = function() {
        $location.path("/tasks");
    };

    $scope.loadRelatedContent = function() {
        $scope.model.content = undefined;
        TaskService.getRelatedContent($scope.model.task.id).then(function (data) {
            $scope.model.content = data;
        });
    };

    $scope.$watch("model.content", function(newValue) {
       if(newValue && newValue.data && newValue.data.length > 0) {
           var needsRefresh = false;
           for(var i=0; i<newValue.data.length; i++) {
               var entry =  newValue.data[i];
               if(!entry.contentAvailable) {
                   needsRefresh = true;
                   break;
               }
           }
       }
    }, true);

    $scope.editComment = function() {
        $scope.model.editingComment = true;
    };

    $scope.stopEditComment = function() {
        $scope.model.editingComment = false;
    };


    $scope.userInvolved = function (user) {
        var alertData = {
            firstName: user.firstName,
            lastName: user.lastName,
            taskName: $scope.model.task.name
        };

        TaskService.involveUserInTask(user.id, $scope.model.task.id).then(function() {
            $rootScope.addAlertPromise($translate('TASK.ALERT.PERSON-INVOLVED',
                alertData));

            if(!$scope.model.task.involvedPeople) {
                $scope.model.task.involvedPeople = [user];
            } else {
                $scope.model.task.involvedPeople.push(user);
            }

        });
    };

    $scope.assigneeSelected = function(user) {
        var alertData = {
            firstName: user.firstName,
            lastName: user.lastName,
            taskName: $scope.model.task.name
        };

        TaskService.assignTask($scope.model.task.id, user.id).then(function() {
            $rootScope.addAlertPromise($translate('TASK.ALERT.ASSIGNED',
                alertData));

        $scope.model.task.assignee = user;
        });
    };


    $scope.revealContent = function(content) {
        $scope.model.activeTab = "content";
        $scope.model.selectedContent = content;
    };

    $scope.hasDetails = function() {

        if ($scope.model.loading == true
            || ($scope.model.involvementSummary === null || $scope.model.involvementSummary === undefined || $scope.model.involvementSummary.loading === true)
            || ($scope.model.contentSummary === null || $scope.model.contentSummary === undefined || $scope.model.contentSummary.loading === true)
            || ($scope.model.commentSummary === null || $scope.model.commentSummary === undefined || $scope.model.commentSummary.loading === true) ) {
            return false;
        }

        if ($scope.model.task !== null && $scope.model.task !== undefined) {

            // Returning true by default, or the screen will flicker until all the data (people/comments/content) have been fetched
            var hasPeople = false;
            var hasContent = false;
            var hasComments = false;

            // Involved people
            if ($scope.model.task.involvedPeople !== null
                && $scope.model.task.involvedPeople !== undefined
                && $scope.model.task.involvedPeople.length > 0) {
                hasPeople = true;
            }

            // Content
            if ($scope.model.content !== null
                && $scope.model.content !== undefined
                && $scope.model.content.data.length > 0) {
                hasContent = true;
            }

            // Comments
            if ($scope.model.comments !== null
                && $scope.model.comments !==undefined
                && $scope.model.comments.data.length > 0) {
                hasComments = true;
            }

            return hasPeople || hasContent || hasComments;

        }
        return false;
    };

        $scope.uploadInProgress = function(state) {
            if (state !== 'undefined') {
                $scope.model.uploadInProgress = state;
            }
        };
 }]);


angular.module('activitiApp')
    .controller('CreateTaskController', ['$rootScope', '$scope', '$translate', '$http', '$location', 'TaskService',
        function ($rootScope, $scope, $translate, $http, $location, TaskService) {

            $scope.createTask = function() {
                TaskService.createTask($scope.newTask).then(function(createdTask) {
                    $scope.resetModel();
                    $rootScope.addAlertPromise($translate('TASK.ALERT.CREATED', createdTask));
                });
            };

            $scope.resetModel = function() {
                $scope.newTask = {
                    name: '',
                    description: ''
                };
            };

            $scope.resetModel();

        }
]);

angular.module('activitiApp')
    .controller('ContentDetailsController', ['$rootScope', '$scope', '$translate', '$modal', 'appResourceRoot', 'RelatedContentService',
        function ($rootScope, $scope, $translate, $modal, appResourceRoot, RelatedContentService) {

            $scope.model = {
                selectedContent: $scope.content,
                selectedTask : $scope.task
            };

            // Map simple-type to readable content type name
            var translateKey;
            if($scope.content) {
                translateKey = "CONTENT.SIMPLE-TYPE." + $scope.content.simpleType.toUpperCase();
            } else {
                translateKey = "CONTENT.SIMPLE-TYPE.CONTENT";
            }

            $translate(translateKey).then(function(message) {
                $scope.model.contentType = message;
            });

            $scope.getPdfViewerUrl = function(content) {
                var urlEncoded = encodeURIComponent(content.pdfUrl);

                return appResourceRoot + 'views/templates/viewer.html?file=' + urlEncoded;
            };

            $scope.deleteContent = function(content, task) {
                var modalInstance = _internalCreateModal({
                    template: appResourceRoot + 'views/modal/delete-content.html',
                    show: true
                }, $modal, $scope);

                modalInstance.$scope.popup = {
                    content: content,
                    loading: false
                };

                modalInstance.$scope.ok = function() {
                    RelatedContentService.deleteContent(content.id, task && task.id).then(function() {
                        $scope.$emit('content-deleted', {content: content});
                        $scope.model.selectedContent = null;
                        $scope.model.selectedTask = null;
                    });
                };
            };
        }
]);
