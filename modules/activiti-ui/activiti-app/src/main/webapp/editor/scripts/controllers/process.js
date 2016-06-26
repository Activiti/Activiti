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
angular.module('activitiModeler')
  .controller('ProcessCtrl', ['$rootScope', '$scope', '$translate', '$http', '$location', '$routeParams','$modal', '$popover', '$timeout', 'appResourceRoot', 'ResourceService',
                              function ($rootScope, $scope, $translate, $http, $location, $routeParams, $modal, $popover, $timeout, appResourceRoot, ResourceService) {

    // Main page (needed for visual indicator of current page)
    $rootScope.setMainPageById('processes');

    // Initialize model
    $scope.model = {
        // Store the main model id, this points to the current version of a model,
        // even when we're showing history
        latestModelId: $routeParams.modelId
    };
    
    $scope.loadProcess = function() {
      var url;
      if ($routeParams.modelHistoryId) {
        url = ACTIVITI.CONFIG.contextRoot + '/app/rest/models/' + $routeParams.modelId
          + '/history/' + $routeParams.modelHistoryId;
      } else {
        url = ACTIVITI.CONFIG.contextRoot + '/app/rest/models/' + $routeParams.modelId;
      }
      
      $http({method: 'GET', url: url}).
        success(function(data, status, headers, config) {
          $scope.model.process = data;
          
          $scope.loadVersions();

          $scope.model.bpmn20DownloadUrl = ACTIVITI.CONFIG.contextRoot + '/app/rest/models/' + $routeParams.modelId +
    			($routeParams.modelHistoryId == undefined ? '' : '/history/' + $routeParams.modelHistoryId) + '/bpmn20?version=' + Date.now();


        	  $rootScope.$on('$routeChangeStart', function(event, next, current) {
        		  jQuery('.qtip').qtip('destroy', true);
        	  });
        	  
	          $timeout(function() {
	            jQuery("#bpmnModel").attr('data-model-id', $routeParams.modelId);
	            jQuery("#bpmnModel").attr('data-model-type', 'design');
	            
	            // in case we want to show a historic model, include additional attribute on the div
	            if(!$scope.model.process.latestVersion) {
	              jQuery("#bpmnModel").attr('data-history-id', $routeParams.modelHistoryId);
	            }

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
              });

        }).error(function(data, status, headers, config) {
          $scope.returnToList();
        });
    };
    
    $scope.useAsNewVersion = function() {
        _internalCreateModal({
    		template: 'views/popup/model-use-as-new-version.html',
    		scope: $scope
    	}, $modal, $scope);
    };
    
    $scope.loadVersions = function() {
      
      var params = {
        includeLatestVersion: !$scope.model.process.latestVersion  
      };
      
      $http({method: 'GET', url: ACTIVITI.CONFIG.contextRoot + '/app/rest/models/' + $scope.model.latestModelId +'/history', params: params}).
      success(function(data, status, headers, config) {
        if ($scope.model.process.latestVersion) {
          if (!data.data) {
            data.data = [];
          }
          data.data.unshift($scope.model.process);
        }
        
        $scope.model.versions = data;
      });
    };
    
    $scope.showVersion = function(version) {
      if(version) {
        if(version.latestVersion) {
            $location.path("/processes/" +  $scope.model.latestModelId);
        } else{
          // Show latest version, no history-suffix needed in URL
          $location.path("/processes/" +  $scope.model.latestModelId + "/history/" + version.id);
        }
      }
    };
    
    $scope.returnToList = function() {
        $location.path("/processes/");
    };
    
    $scope.editProcess = function() {
        _internalCreateModal({
    		template: 'views/popup/model-edit.html',
	        scope: $scope
    	}, $modal, $scope);
    };

    $scope.duplicateProcess = function() {
      var modalInstance = _internalCreateModal({
        template: 'views/popup/process-duplicate.html?version=' + Date.now()
      }, $modal, $scope);

      modalInstance.$scope.originalModel = $scope.model;
    };

    $scope.deleteProcess = function() {
        _internalCreateModal({
    		template: 'views/popup/model-delete.html',
    		scope: $scope
    	}, $modal, $scope);
    };
    
    $scope.openEditor = function() {
      if ($scope.model.process) {
        $location.path("/editor/" + $scope.model.process.id);
      }
    };
      
    $scope.toggleHistory = function($event) {
        if(!$scope.historyState) {
          var state = {};
          $scope.historyState = state;
          
          // Create popover
          state.popover = $popover(angular.element($event.target), {
            template: 'views/popover/history.html',
            placement: 'bottom-right',
            show: true,
            scope: $scope,
            container: 'body'
          });
          
          var destroy = function() {
            state.popover.destroy();
            delete $scope.historyState;
          }
          
          // When popup is hidden or scope is destroyed, hide popup
          state.popover.$scope.$on('tooltip.hide', destroy);
          $scope.$on('$destroy', destroy);
        }
    };
    
    $scope.loadProcess();
}]);
