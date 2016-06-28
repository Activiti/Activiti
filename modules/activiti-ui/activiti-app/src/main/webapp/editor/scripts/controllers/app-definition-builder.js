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
  .controller('AppDefinitionBuilderController', ['$rootScope', '$scope', '$translate', '$http', '$location', '$routeParams', '$modal', '$popover', '$timeout', 
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
    	$http({method: 'GET', url: ACTIVITI.CONFIG.contextRoot + '/app/rest/app-definitions/' + $routeParams.modelId}).
        	success(function(data, status, headers, config) {
        	    $rootScope.currentAppDefinition = data;
        	    if (!$rootScope.currentAppDefinition.definition.theme) {
        	        $rootScope.currentAppDefinition.definition.theme = 'theme-1';
        	    }
                if (!$rootScope.currentAppDefinition.definition.icon) {
                    $rootScope.currentAppDefinition.definition.icon = 'glyphicon-asterisk';
                }

        	}).error(function(data, status, headers, config) {
        		console.log("Error loading model");
        	});
    };

    // Cleanup app definition on rootscope when scope is destroyed
    $scope.$on('$destroy', function() {
        $rootScope.currentAppDefinition = undefined;
    });
    
    $scope.editIncludedModels = function() {
        _internalCreateModal({
            template: 'views/popup/app-definition-models-included.html?version=' + Date.now(),
            scope: $scope
        }, $modal, $scope);
    };

    $scope.availableIcons = [
        'glyphicon-asterisk', 'glyphicon-plus',
        'glyphicon-euro', 'glyphicon-cloud', 'glyphicon-envelope',
        'glyphicon-pencil', 'glyphicon-glass', 'glyphicon-music',
        'glyphicon-search', 'glyphicon-heart', 'glyphicon-star',
        'glyphicon-star-empty', 'glyphicon-user', 'glyphicon-film',
        'glyphicon-th-large', 'glyphicon-th', 'glyphicon-th-list',
        'glyphicon-ok', 'glyphicon-remove', 'glyphicon-zoom-in',
        'glyphicon-zoom-out', 'glyphicon-off', 'glyphicon-signal',
        'glyphicon-cog', 'glyphicon-trash', 'glyphicon-home',
        'glyphicon-file', 'glyphicon-time', 'glyphicon-road',
        'glyphicon-download-alt', 'glyphicon-download',
        'glyphicon-upload', 'glyphicon-inbox', 'glyphicon-play-circle',
        'glyphicon-repeat', 'glyphicon-refresh', 'glyphicon-list-alt',
        'glyphicon-lock', 'glyphicon-flag', 'glyphicon-headphones',
        'glyphicon-volume-up', 'glyphicon-tag', 'glyphicon-tags',
        'glyphicon-book', 'glyphicon-bookmark', 'glyphicon-print',
        'glyphicon-camera', 'glyphicon-list', 'glyphicon-facetime-video',
        'glyphicon-picture', 'glyphicon-map-marker', 'glyphicon-adjust',
        'glyphicon-tint', 'glyphicon-edit', 'glyphicon-share',
        'glyphicon-check', 'glyphicon-move', 'glyphicon-play',
        'glyphicon-eject', 'glyphicon-plus-sign', 'glyphicon-minus-sign',
        'glyphicon-remove-sign', 'glyphicon-ok-sign',
        'glyphicon-question-sign', 'glyphicon-info-sign',
        'glyphicon-screenshot', 'glyphicon-remove-circle',
        'glyphicon-ok-circle', 'glyphicon-ban-circle',
        'glyphicon-share-alt', 'glyphicon-exclamation-sign',
        'glyphicon-gift', 'glyphicon-leaf', 'glyphicon-fire',
        'glyphicon-eye-open', 'glyphicon-eye-close',
        'glyphicon-warning-sign', 'glyphicon-plane',
        'glyphicon-calendar', 'glyphicon-random', 'glyphicon-comment',
        'glyphicon-magnet', 'glyphicon-retweet',
        'glyphicon-shopping-cart', 'glyphicon-folder-close',
        'glyphicon-folder-open', 'glyphicon-hdd', 'glyphicon-bullhorn',
        'glyphicon-bell', 'glyphicon-certificate', 'glyphicon-thumbs-up',
        'glyphicon-thumbs-down', 'glyphicon-hand-left',
        'glyphicon-globe', 'glyphicon-wrench', 'glyphicon-tasks',
        'glyphicon-filter', 'glyphicon-briefcase', 'glyphicon-dashboard',
        'glyphicon-paperclip', 'glyphicon-heart-empty', 'glyphicon-link',
        'glyphicon-phone', 'glyphicon-pushpin', 'glyphicon-usd',
        'glyphicon-gbp', 'glyphicon-sort', 'glyphicon-flash',
        'glyphicon-record', 'glyphicon-save', 'glyphicon-open',
        'glyphicon-saved', 'glyphicon-send', 'glyphicon-floppy-disk',
        'glyphicon-credit-card', 'glyphicon-cutlery',
        'glyphicon-earphone', 'glyphicon-phone-alt', 'glyphicon-tower',
        'glyphicon-stats', 'glyphicon-cloud-download',
        'glyphicon-cloud-upload', 'glyphicon-tree-conifer',
        'glyphicon-tree-deciduous' 
    ];
    
    // TODO: add themes and perhaps have colors inside JS instead of different css-classes for each theme
    $scope.availableThemes = [
        'theme-1', 'theme-2', 'theme-3',
        'theme-4', 'theme-5', 'theme-6',
        'theme-7', 'theme-8', 'theme-9',
        'theme-10'
    ];
    
    $scope.changeIcon = function($event) {
      if (!$scope.changeIconState) {
        var state = {};
        $scope.changeIconState = state;
        
        // Create popover
        state.popover = $popover(angular.element($event.currentTarget), {
          template: 'views/popover/select-app-icon.html',
          placement: 'bottom-left',
          show: true,
          scope: $scope
        });
        
        var destroy = function() {
          state.popover.destroy();
          delete $scope.changeIconState;
        };
        
        // When popup is hidden or scope is destroyed, hide popup
        state.popover.$scope.$on('tooltip.hide', destroy);
        $scope.$on('$destroy', destroy);
      }
    };
    
    $scope.selectIcon = function(icon) {
        $rootScope.currentAppDefinition.definition.icon = icon;
    };
    
    $scope.changeTheme = function($event) {
      if(!$scope.changeThemeState) {
        var state = {};
        $scope.changeThemeState = state;
        
        // Create popover
        state.popover = $popover(angular.element($event.currentTarget), {
          template: 'views/popover/select-app-theme.html',
          placement: 'bottom-left',
          show: true,
          scope: $scope
        });
        
        var destroy = function() {
          state.popover.destroy();
          delete $scope.changeThemeState;
        };
        
        // When popup is hidden or scope is destroyed, hide popup
        state.popover.$scope.$on('tooltip.hide', destroy);
        $scope.$on('$destroy', destroy);
      }
    };
    
    $scope.selectTheme = function(theme) {
        $rootScope.currentAppDefinition.definition.theme = theme;
    };
    
    $scope.loadApp();
}]);

angular.module('activitiModeler')
.controller('ModelsIncludedPopupCtrl', ['$rootScope', '$scope', '$translate', '$http', '$timeout', '$location', function ($rootScope, $scope, $translate, $http, $timeout, $location) {

    $scope.popup = {
        loading: false,
        selectedModels: []
    };
    
    if ($rootScope.currentAppDefinition.definition.models) {
        for (var i = 0; i < $rootScope.currentAppDefinition.definition.models.length; i++) {
            $scope.popup.selectedModels.push($rootScope.currentAppDefinition.definition.models[i].id);
        }
    }
    
    $scope.loadModels = function() {
        $scope.popup.loading = true;
        
        $http({method: 'GET', url: ACTIVITI.CONFIG.contextRoot + '/app/rest/models-for-app-definition'}).
          success(function(data, status, headers, config) {
              $scope.popup.models = data;
              $scope.popup.loading = false;
          }).
          error(function(data, status, headers, config) {
             $scope.popup.loading = false;
          });
    };
    
    $scope.selectModel = function(model) {
        var index = $scope.popup.selectedModels.indexOf(model.id);
        if (index >= 0) {
            $scope.popup.selectedModels.splice(index, 1);
        } else {
            $scope.popup.selectedModels.push(model.id);
        }
        
        var modelArray = [];
        for (var i = 0; i < $scope.popup.models.data.length; i++) {
            if ($scope.popup.selectedModels.indexOf($scope.popup.models.data[i].id) >= 0) {
                var selectedModel = $scope.popup.models.data[i];
                var summaryModel = {
                    id: selectedModel.id,
                    name: selectedModel.name,
                    version: selectedModel.version,
                    modelType: selectedModel.modelType,
                    description: selectedModel.description,
                    stencilSetId: selectedModel.stencilSet,
                    createdByFullName: selectedModel.createdByFullName,
                    createdBy: selectedModel.createdBy,
                    lastUpdatedByFullName: selectedModel.lastUpdatedByFullName,
                    lastUpdatedBy: selectedModel.lastUpdatedBy,
                    lastUpdated: selectedModel.lastUpdated
                };
                modelArray.push(summaryModel);
            }
        }
        $rootScope.currentAppDefinition.definition.models = modelArray;
    };
    
    $scope.isModelSelected = function(model) {
        if ($scope.popup.selectedModels.indexOf(model.id) >= 0) {
            return true;
        } else {
            return false;
        }
    };
    
    $scope.close = function() {
        $scope.$hide();
    };
    
    $scope.loadModels();
}]);