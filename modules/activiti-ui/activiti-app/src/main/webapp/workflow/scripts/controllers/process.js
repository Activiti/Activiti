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
  .controller('ProcessController', ['$rootScope', '$scope', '$translate', '$http', '$timeout', '$location', '$modal', '$routeParams', 'AppDefinitionService',
    function ($rootScope, $scope, $translate, $http, $timeout, $location, $modal, $routeParams, AppDefinitionService) {

      // Ensure correct main page is set
      $rootScope.setMainPageById('processes');

      $scope.selectedProcessInstance = { id: $routeParams.processId };

        $scope.deploymentKey = $routeParams.deploymentKey;

        $scope.$on('processinstance-deleted', function (event, data) {
            $scope.openProcesses();
        });

        $scope.openProcesses = function(task) {
            var path='';
            if($rootScope.activeAppDefinition && !ACTIVITI.CONFIG.integrationProfile) {
                path = "/apps/" + $rootScope.activeAppDefinition.id;
            }
            $location.path(path + "/processes");
        };
}]);

angular.module('activitiApp')
    .controller('ProcessDetailController', ['$rootScope', '$scope', '$translate', '$http', '$timeout','$location', '$route', '$modal', '$routeParams', '$popover', 'appResourceRoot', 'TaskService', 'CommentService', 'RelatedContentService',
        function ($rootScope, $scope, $translate, $http, $timeout, $location, $route, $modal, $routeParams, $popover, appResourceRoot, TaskService, CommentService, RelatedContentService) {

    $rootScope.root.showStartForm = false;

    $scope.model = {
        // Indirect binding between selected task in parent scope to have control over display
        // before actual selected task is switched
        processInstance: $scope.selectedProcessInstance
    };

    $scope.$watch('selectedProcessInstance', function(newValue) {
        if (newValue && newValue.id) {
            $scope.model.processUpdating = true;
            $scope.model.processInstance = newValue;

            $scope.getProcessInstance(newValue.id);
        }
    });

    $scope.getProcessInstance = function(processInstanceId) {
        $http({method: 'GET', url: ACTIVITI.CONFIG.contextRoot + '/app/rest/process-instances/' + processInstanceId}).
            success(function(response, status, headers, config) {
                $scope.model.processInstance = response;
                $scope.loadProcessTasks();
                $scope.loadComments();
            }).
            error(function(response, status, headers, config) {
                console.log('Something went wrong: ' + response);
            });
    };

    $rootScope.loadProcessTasks = function() {

        // Runtime tasks
        TaskService.getProcessInstanceTasks($scope.model.processInstance.id, false).then(function(response) {
            $scope.model.processTasks = response.data;
        });

        TaskService.getProcessInstanceTasks($scope.model.processInstance.id, true).then(function(response) {
            if(response.data && response.data.length > 0) {
                $scope.model.completedProcessTasks = response.data;
            } else {
                $scope.model.completedProcessTasks = [];
            }

            // Calculate duration
            for(var i=0; i<response.data.length; i++) {
                var task = response.data[i];
                if(task.duration) {
                    task.duration = moment.duration(task.duration).humanize();
                }
            }
        });
    };

    $scope.toggleCreateComment = function() {

        $scope.model.addComment = !$scope.model.addComment;

        if($scope.model.addComment) {
            $timeout(function() {
                angular.element('.focusable').focus();
            }, 100);
        }
    };

    $scope.cancelProcess = function(final) {
        if ($scope.model.processInstance) {
            var modalInstance = _internalCreateModal({
                template: appResourceRoot + 'views/modal/process-cancel.html',
                scope: $scope,
                show: true
            }, $modal, $scope);

            if(final) {
                modalInstance.$scope.finalDelete = true;
            }
        }
    };

    $scope.deleteProcess = function() {
        $scope.cancelProcess(true);
    };

    $scope.$on('processinstance-deleted', function (event, data) {
        $route.reload();
    });

    $scope.openTask = function(task) {
        // TODO: use URL instead
        $rootScope.root.selectedTaskId = task.id;
        var path='';
        if($rootScope.activeAppDefinition && !ACTIVITI.CONFIG.integrationProfile) {
            path = "/apps/" + $rootScope.activeAppDefinition.id;
        }
        $location.path(path + "/tasks");
    };

    $scope.openStartForm = function() {
        $rootScope.root.showStartForm = true;
        $rootScope.root.selectedProcessId = $scope.model.processInstance.id;
    };

    $scope.popupShown = function() {

    };

    $scope.closeDiagramPopup = function() {
        jQuery('.qtip').qtip('destroy', true);
    };

    $scope.loadComments = function() {
        CommentService.getProcessInstanceComments($scope.model.processInstance.id).then(function (data) {
            $scope.model.comments = data;
        });
    };

    $scope.confirmNewComment = function() {
        $scope.model.commentLoading = true;

        CommentService.createProcessInstanceComment($scope.model.processInstance.id, $scope.model.newComment.trim())
            .then(function(comment) {
                $scope.model.newComment = undefined;
                $scope.model.commentLoading = false;
                $rootScope.addAlertPromise($translate('PROCESS.ALERT.COMMENT-ADDED'));

                $scope.toggleCreateComment();


                $scope.loadComments();
            });
    };

    $scope.showDiagram = function() {
        var modalInstance = _internalCreateModal({
            template: appResourceRoot + 'views/modal/process-instance-graphical.html',
            scope: $scope,
            show: true
        }, $modal, $scope);

    };
}]);

angular.module('activitiApp')
    .controller('ShowProcessDiagramCtrl', ['$scope', '$timeout', 'ResourceService', 'appResourceRoot',
        function ($scope, $timeout, ResourceService, appResourceRoot) {

            $timeout(function() {
                jQuery("#bpmnModel").attr('data-model-id', $scope.model.processInstance.id);
                jQuery("#bpmnModel").attr('data-model-type', 'runtime');

                // in case we want to show a historic model, include additional attribute on the div
                /*
                  if(!$scope.model.process.latestVersion) {
                    jQuery("#bpmnModel").attr('data-history-id', $routeParams.processModelHistoryId);
                  }
                */
                var viewerUrl = appResourceRoot + "../display/displaymodel.html?version=" + Date.now();

                // If Activiti has been deployed inside an AMD environment Raphael will fail to register
                // itself globally until displaymodel.js (which depends ona global Raphale variable) is runned,
                // therefor remove AMD's define method until we have loaded in Raphael and displaymodel.js
                // and assume/hope its not used during.
                var amdDefine = window.define;
                window.define = undefined;
                ResourceService.loadFromHtml(viewerUrl, function(){
                    // Restore AMD's define method again
                    window.define = amdDefine;
                });
            }, 100);
        }
    ]
);

angular.module('activitiApp')
.controller('CancelProcessCtrl', ['$scope', '$http', '$route', 'ProcessService', function ($scope, $http, $route, ProcessService) {

        $scope.popup = {loading: false};

        $scope.ok = function() {
            $scope.popup.loading = true;

            ProcessService.deleteProcess($scope.model.processInstance.id).
                then(function(response, status, headers, config) {
                    $scope.$hide();
                }).
                finally(function(response, status, headers, config) {
                    $scope.popup.loading = false;
                })
        };

        $scope.cancel = function() {
            $scope.$hide();
        }
    }
]);
