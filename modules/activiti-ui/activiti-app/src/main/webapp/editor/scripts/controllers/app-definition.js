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
  .controller('AppDefinitionCtrl', ['$rootScope', '$scope', '$translate', '$http', '$location', '$routeParams', '$modal', '$popover', '$timeout',
                              function ($rootScope, $scope, $translate, $http, $location, $routeParams, $modal, $popover, $timeout) {

    // Main page (needed for visual indicator of current page)
    $rootScope.setMainPageById('apps');

    // Initialize model
    $scope.model = {
        // Store the main model id, this points to the current version of a model,
        // even when we're showing history
        latestModelId: $routeParams.modelId
    };

    $scope.loadApp = function() {
    	var url;
    	var definitionUrl;

    	if ($routeParams.modelHistoryId) {
    		url = ACTIVITI.CONFIG.contextRoot + '/app/rest/models/' + $routeParams.modelId
    			+ '/history/' + $routeParams.modelHistoryId;
    		definitionUrl = ACTIVITI.CONFIG.contextRoot + '/app/rest/app-definitions/' + $routeParams.modelId
                + '/history/' + $routeParams.modelHistoryId;
    	} else {
    		url = ACTIVITI.CONFIG.contextRoot + '/app/rest/models/' + $routeParams.modelId;
    		definitionUrl = ACTIVITI.CONFIG.contextRoot + '/app/rest/app-definitions/' + $routeParams.modelId;

    		$scope.model.appExportUrl = ACTIVITI.CONFIG.contextRoot + '/app/rest/app-definitions/' + $routeParams.modelId +
                '/export?version=' + Date.now();
    	}

    	$http({method: 'GET', url: url}).
        	success(function(data, status, headers, config) {
        		$scope.model.app = data;
        		$scope.loadVersions();

        	}).error(function(data, status, headers, config) {
        		$scope.returnToList();
        	});

    	$http({method: 'GET', url: definitionUrl}).
            success(function(data, status, headers, config) {
                $scope.model.appDefinition = data;
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
        includeLatestVersion: !$scope.model.app.latestVersion
      };

      $http({method: 'GET', url: ACTIVITI.CONFIG.contextRoot + '/app/rest/models/' + $scope.model.latestModelId + '/history', params: params}).
	      success(function(data, status, headers, config) {
	        if ($scope.model.app.latestVersion) {
	          if (!data.data) {
	            data.data = [];
	          }
	          data.data.unshift($scope.model.app);
	        }

	        $scope.model.versions = data;
	      });
    };

    $scope.showVersion = function(version) {
      if (version) {
        if (version.latestVersion) {
            $location.path("/apps/" +  $scope.model.latestModelId);
        } else {
          // Show latest version, no history-suffix needed in URL
          $location.path("/apps/" +  $scope.model.latestModelId + "/history/" + version.id);
        }
      }
    };

    $scope.returnToList = function() {
        $location.path("/apps/");
    };

    $scope.openEditor = function() {
        $location.path("/app-editor/" + $scope.model.latestModelId);
    };

    $scope.editApp = function() {
        _internalCreateModal({
    		template: 'views/popup/model-edit.html',
	        scope: $scope
    	}, $modal, $scope);
    };

    $scope.duplicateApp = function () {
      var modalInstance = _internalCreateModal({
          template: 'views/popup/app-definition-duplicate.html?version=' + Date.now()
      }, $modal, $scope);

      modalInstance.$scope.originalModel = $scope.model;
    };


    $scope.deleteApp = function() {
        $http({method: 'GET', url: ACTIVITI.CONFIG.contextRoot + '/app/rest/runtime/app-definitions/model/' + $scope.model.latestModelId}).
            success(function(data, status, headers, config) {
                if (data && data.id) {
                    // User is owner of the app definition and the app definition is deployed
                    _internalCreateModal({
                        template: 'views/popup/app-definition-delete.html?version=' + Date.now(),
                        scope: $scope
                    }, $modal, $scope);

                } else {
                    _internalCreateModal({
                        template: 'views/popup/model-delete.html?version=' + Date.now(),
                        scope: $scope
                    }, $modal, $scope);
                }
            });
    };

    $scope.publish = function() {
        _internalCreateModal({
            template: 'views/popup/app-definition-publish.html?version=' + Date.now(),
            scope: $scope
        }, $modal, $scope);
    };

    $scope.shareApp = function() {
        _internalCreateModal({
    		template: 'views/popup/model-share.html?version=' + Date.now(),
    		scope: $scope
    	}, $modal, $scope);
    };

    $scope.importAppDefinition = function () {
        _internalCreateModal({
            template: 'views/popup/app-definition-import.html?version=' + Date.now(),
            scope: $scope
        }, $modal, $scope);
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
          };

          // When popup is hidden or scope is destroyed, hide popup
          state.popover.$scope.$on('tooltip.hide', destroy);
          $scope.$on('$destroy', destroy);
        }
      };

    $scope.loadApp();
}]);

angular.module('activitiModeler')
.controller('PublishAppDefinitionPopupCtrl', ['$rootScope', '$scope', '$http', '$route', '$translate', function ($rootScope, $scope, $http, $route, $translate) {

    $scope.popup = {
        loading: false,
        comment: ''
    };

    $scope.ok = function (force) {
        $scope.popup.loading = true;
        var data = {
            comment: $scope.popup.comment
        };

        if (force !== undefined && force !== null && force === true) {
            data.force = true;
        }

        delete $scope.popup.error;

        $http({method: 'POST', url: ACTIVITI.CONFIG.contextRoot + '/app/rest/app-definitions/' + $scope.model.app.id + '/publish', data: data}).
            success(function(data, status, headers, config) {
                $scope.$hide();

                if (data.errorType == 1) {
                    var error = {modelName: data.errorDescription};
                    $scope.addAlertPromise($translate('APP.POPUP.HAS-CUSTOM-STENCILITEM', error), 'error');

                } else if (data.errorType == 2) {
                    var error = { modelName: data.errorDescription};
                    $scope.addAlertPromise($translate('APP.POPUP.HAS-VALIDATIONERROR', error), 'error');

                } else {
                    $scope.popup.loading = false;
                    $route.reload();
                    $scope.addAlertPromise($translate('APP.ALERT.PUBLISH-CONFIRM'), 'info');
                }
            }).
            error(function(data, status, headers, config) {

                $scope.popup.loading = false;

                if (status === 409 && data && data.messageKey === 'app.publish.procdef.key.conflict') {
                    $scope.popup.error = {
                        type: 'conflictingProcDefKey',
                        data: data.customData
                    };
                } else if(status === 409 && data && data.messageKey === 'app.publish.procdef.duplicate.keys') {
                    $scope.popup.error = {
                        type: 'duplicateProcDefKeys',
                        data: data.customData
                    };
                } else if (status === 409 && data && data.messageKey === 'app.publish.process.model.already.used') {
                    $scope.popup.error = {
                        type: 'processModelAlreadyUsed',
                        data: data.customData
                    };
                } else {
                    $scope.$hide();
                    $scope.addAlertPromise($translate('APP.ALERT.PUBLISH-ERROR'), 'error');
                }
            });
    };

    $scope.cancel = function () {
        if (!$scope.popup.loading) {
            $scope.$hide();
        }
    };
}]);

angular.module('activitiModeler')
.controller('DeleteAppDefinitionPopupCrtl', ['$rootScope', '$scope', '$http', '$translate', function ($rootScope, $scope, $http, $translate) {

    $scope.popup = {
        loading: false,
        cascade: 'false'
    };

    $scope.ok = function () {
        $scope.popup.loading = true;
        var params = {
            // Explicit string-check because radio-values cannot be js-booleans
            cascade : $scope.popup.cascade === 'true',
            deleteRuntimeApp: true
        };

        $http({method: 'DELETE', url: ACTIVITI.CONFIG.contextRoot + '/app/rest/models/' + $scope.model.app.id, params: params}).
            success(function(data, status, headers, config) {
                $scope.$hide();
                $scope.popup.loading = false;
                $scope.addAlertPromise($translate('APP.ALERT.DELETE-CONFIRM'), 'info');
                $scope.returnToList();
            }).
            error(function(data, status, headers, config) {
                $scope.$hide();
                $scope.popup.loading = false;
            });
    };

    $scope.cancel = function () {
        if (!$scope.popup.loading) {
            $scope.$hide();
        }
    };
}]);

angular.module('activitiModeler')
.controller('ImportNewVersionAppDefinitionCtrl', ['$rootScope', '$scope', '$http', 'Upload', '$route', function ($rootScope, $scope, $http, Upload, $route) {

  $scope.popup = {
       loading: false,
       renewIdmIds: false
  };

  $scope.onFileSelect = function($files, isIE) {

      $scope.popup.loading = true;

      for (var i = 0; i < $files.length; i++) {
          var file = $files[i];

          var url;
          if (isIE) {
              url = ACTIVITI.CONFIG.contextRoot + '/app/rest/app-definitions/' + $scope.model.app.id + '/text/import?renewIdmEntries=' + $scope.popup.renewIdmIds;
          } else {
              url = ACTIVITI.CONFIG.contextRoot + '/app/rest/app-definitions/' + $scope.model.app.id + '/import?renewIdmEntries=' + $scope.popup.renewIdmIds;
          }

          Upload.upload({
              url: url,
              method: 'POST',
              file: file
          }).progress(function(evt) {
              $scope.popup.uploadProgress = parseInt(100.0 * evt.loaded / evt.total);

          }).success(function(data, status, headers, config) {
              $scope.popup.loading = false;

              $route.reload();
              $scope.$hide();

          }).error(function(data, status, headers, config) {

              if (data && data.message) {
                  $scope.popup.errorMessage = data.message;
              }

              $scope.popup.error = true;
              $scope.popup.loading = false;
          });
      }
  };

  $scope.cancel = function () {
      if (!$scope.popup.loading) {
          $scope.$hide();
      }
  };
}]);
