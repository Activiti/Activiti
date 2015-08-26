/*
 * Activiti app component part of the Activiti project
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
angular.module('activitiModeler')
  .controller('FormCtrl', ['$rootScope', '$scope', '$translate', '$http', '$location', '$routeParams','$modal', '$timeout', '$popover', 
                              function ($rootScope, $scope, $translate, $http, $location, $routeParams, $modal, $timeout, $popover) {

    // Main page (needed for visual indicator of current page)
    $rootScope.setMainPageById('forms');
    
    $scope.formMode = 'read';
    
    // Initialize model
    $scope.model = {
        // Store the main model id, this points to the current version of a model,
        // even when we're showing history
        latestModelId: $routeParams.modelId
    };
    
    $scope.loadForm = function() {
      var url;
      if ($routeParams.modelHistoryId) {
        url = ACTIVITI.CONFIG.contextRoot + '/app/rest/models/' + $routeParams.modelId
          + '/history/' + $routeParams.modelHistoryId;
      } else {
        url = ACTIVITI.CONFIG.contextRoot + '/app/rest/models/' + $routeParams.modelId;
      }
      
      $http({method: 'GET', url: url}).
        success(function(data, status, headers, config) {
          $scope.model.form = data;
          $scope.loadVersions();

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
        includeLatestVersion: !$scope.model.form.latestVersion  
      };
      
      $http({method: 'GET', url: ACTIVITI.CONFIG.contextRoot + '/app/rest/models/' + $scope.model.latestModelId + '/history', params: params}).
	      success(function(data, status, headers, config) {
	        if ($scope.model.form.latestVersion) {
	          if (!data.data) {
	            data.data = [];
	          }
	          data.data.unshift($scope.model.form);
	        }
	        
	        $scope.model.versions = data;
	      });
    };
    
    $scope.showVersion = function(version) {
      if (version) {
        if (version.latestVersion) {
            $location.path("/forms/" +  $scope.model.latestModelId);
        } else {
          // Show latest version, no history-suffix needed in URL
          $location.path("/forms/" +  $scope.model.latestModelId + "/history/" + version.id);
        }
      }
    };
    
    $scope.returnToList = function() {
        $location.path("/forms/");
    };
    
    $scope.editForm = function() {
        _internalCreateModal({
    		template: 'views/popup/model-edit.html',
	        scope: $scope
    	}, $modal, $scope);
    };

    $scope.duplicateForm = function() {

      var modalInstance = _internalCreateModal({
        template: 'views/popup/form-duplicate.html?version=' + Date.now()
      }, $modal, $scope);

      modalInstance.$scope.originalModel = $scope.model;

      modalInstance.$scope.duplicateFormCallback = function(result) {
        $rootScope.editorHistory = [];
        $location.path("/form-editor/" + result.id);
      };
    };

    $scope.deleteForm = function() {
        _internalCreateModal({
    		template: 'views/popup/model-delete.html',
    		scope: $scope
    	}, $modal, $scope);
    };
    
    $scope.openEditor = function() {
      if ($scope.model.form) {
    	  $location.path("/form-editor/" + $scope.model.form.id);
      }
    };

    $scope.toggleHistory = function($event) {
      if (!$scope.historyState) {
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
        };
        
        // When popup is hidden or scope is destroyed, hide popup
        state.popover.$scope.$on('tooltip.hide', destroy);
        $scope.$on('$destroy', destroy);
      }
    };
    
    $scope.loadForm();
    
}]);
