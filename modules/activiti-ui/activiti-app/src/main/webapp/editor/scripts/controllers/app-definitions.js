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

angular.module('activitiModeler')
  .controller('AppDefinitionsCtrl', ['$rootScope', '$scope', '$translate', '$http', '$timeout','$location', '$modal', function ($rootScope, $scope, $translate, $http, $timeout, $location, $modal) {

      // Main page (needed for visual indicator of current page)
      $rootScope.setMainPageById('apps');

	  $scope.model = {
        filters: [
            {id: 'myApps', labelKey: 'MY-APPS'},
            {id: 'sharedWithMe', labelKey: 'SHARED-WITH-ME'},
            {id: 'sharedWithOthers', labelKey: 'SHARED-WITH-OTHERS'}
		],

		sorts: [
	        {id: 'modifiedDesc', labelKey: 'MODIFIED-DESC'},
	        {id: 'modifiedAsc', labelKey: 'MODIFIED-ASC'},
	        {id: 'nameAsc', labelKey: 'NAME-ASC'},
	        {id: 'nameDesc', labelKey: 'NAME-DESC'}
		]
	  };

	  if ($rootScope.appFilter) {
		  $scope.model.activeFilter = $rootScope.appFilter.filter;
		  $scope.model.activeSort = $rootScope.appFilter.sort;
		  $scope.model.filterText = $rootScope.appFilter.filterText;

	  } else {
		  // By default, show first filter and use first sort
	      $scope.model.activeFilter = $scope.model.filters[0];
	      $scope.model.activeSort = $scope.model.sorts[0];
	      $rootScope.appFilter = {
	    		  filter: $scope.model.activeFilter,
	    		  sort: $scope.model.activeSort,
	    		  filterText: ''
	      };
	  }

	  $scope.activateFilter = function(filter) {
		  $scope.model.activeFilter = filter;
		  $rootScope.appFilter.filter = filter;
		  $scope.loadApps();
	  };

	  $scope.activateSort = function(sort) {
		  $scope.model.activeSort = sort;
		  $rootScope.appFilter.sort = sort;
		  $scope.loadApps();
	  };

	  $scope.loadApps = function() {
		  $scope.model.loading = true;

		  var params = {
		      filter: $scope.model.activeFilter.id,
		      sort: $scope.model.activeSort.id,
		      modelType: 3
		  };

		  if ($scope.model.filterText && $scope.model.filterText != '') {
		    params.filterText = $scope.model.filterText;
		  }

		  $http({method: 'GET', url: ACTIVITI.CONFIG.contextRoot + '/app/rest/models', params: params}).
		  	success(function(data, status, headers, config) {
	    		$scope.model.apps = data;
	    		$scope.model.loading = false;
	        }).
	        error(function(data, status, headers, config) {
	           $scope.model.loading = false;
	        });
	  };

	  var timeoutFilter = function() {
	    $scope.model.isFilterDelayed = true;
	    $timeout(function() {
	        $scope.model.isFilterDelayed = false;
	        if($scope.model.isFilterUpdated) {
	          $scope.model.isFilterUpdated = false;
	          timeoutFilter();
	        } else {
	          $scope.model.filterText = $scope.model.pendingFilterText;
	          $rootScope.appFilter.filterText = $scope.model.filterText;
	          $scope.loadApps();
	        }
	    }, 500);
	  };

	  $scope.filterDelayed = function() {
	    if($scope.model.isFilterDelayed) {
	      $scope.model.isFilterUpdated = true;
	    } else {
	      timeoutFilter();
	    }
	  };

	  $scope.createApp = function() {

          _internalCreateModal({
			  template: 'views/popup/app-definition-create.html?version=' + Date.now(),
			  scope: $scope
		  }, $modal, $scope);
	  };

	  $scope.showAppDetails = function(app) {
	    if (app) {
	      $location.path("/apps/" + app.id);
	    }
	  };

	  $scope.editAppDetails = function(app) {
        if (app) {
          $location.path("/app-editor/" + app.id);
        }
      };

      $scope.importAppDefinition = function () {
          _internalCreateModal({
              template: 'views/popup/app-definitions-import.html?version=' + Date.now()
          }, $modal, $scope);
      };

	  // Finally, load initial forms
	  $scope.loadApps();
  }]);


angular.module('activitiModeler')
    .controller('CreateNewAppCrtl', ['$rootScope', '$scope', '$http', '$location', '$translate', function ($rootScope, $scope, $http, $location, $translate) {

        $scope.model = {
            loading: false,
            app: {
                name: '',
                key: '',
                description: '',
                modelType: 3
            }
        };

        $scope.ok = function () {

            if (!$scope.model.app.name || $scope.model.app.name.length == 0 ||
            	!$scope.model.app.key || $scope.model.app.key.length == 0) {
            	
                return;
            }

            $scope.model.loading = true;

            $http({method: 'POST', url: ACTIVITI.CONFIG.contextRoot + '/app/rest/models', data: $scope.model.app}).
                success(function (data, status, headers, config) {
                    $scope.$hide();

                    $scope.model.loading = false;
                    $location.path("/app-editor/" + data.id);

                }).
                error(function (response, status, headers, config) {
                    $scope.model.loading = false;
					
					if (response && response.message && response.message.length > 0) {
						$scope.model.errorMessage = response.message;
						
					} else if (response && response.messageKey) {
                        $translate(response.messageKey, response.customData).then(function (message) {
                            $scope.errorMessage = message;
                        });
                    }
                });
        };

        $scope.cancel = function () {
            if (!$scope.model.loading) {
                $scope.$hide();
            }
        };
    }]);

angular.module('activitiModeler')
    .controller('DuplicateAppCrtl', ['$rootScope', '$scope', '$http', '$location', '$translate', function ($rootScope, $scope, $http, $location, $translate) {

        $scope.model = {
            loading: false,
            app: {
                id: '',
                name: '',
                key: '',
                description: '',
                modelType: 3
            }
        };

        if ($scope.originalModel) {
            //clone the model
            $scope.model.app.name = $scope.originalModel.app.name;
            $scope.model.app.key = $scope.originalModel.app.key;
            $scope.model.app.description = $scope.originalModel.app.description;
            $scope.model.app.modelType = $scope.originalModel.app.modelType;
            $scope.model.app.id = $scope.originalModel.app.id;
        }

        $scope.ok = function () {

            if (!$scope.model.app.name || $scope.model.app.name.length == 0) {
                return;
            }

            $scope.model.loading = true;

            $http({method: 'POST', url: ACTIVITI.CONFIG.contextRoot + '/app/rest/models/'+$scope.model.app.id+'/clone', data: $scope.model.app}).
                success(function (data, status, headers, config) {
                    $scope.$hide();

                    $scope.model.loading = false;
                    $location.path("/app-editor/" + data.id);

                }).
                error(function (response, status, headers, config) {
                    $scope.model.loading = false;

                    if (response && response.messageKey) {
                        $translate(response.messageKey, response.customData).then(function (message) {
                            $scope.errorMessage = message;
                        });
                    }
                });
        };

        $scope.cancel = function () {
            if (!$scope.model.loading) {
                $scope.$hide();
            }
        };
    }]);

angular.module('activitiModeler')
.controller('ImportAppDefinitionCtrl', ['$rootScope', '$scope', '$http', 'Upload', '$location', function ($rootScope, $scope, $http, Upload, $location) {

  $scope.model = {
       loading: false,
       renewIdmIds: false
  };

  $scope.onFileSelect = function($files, isIE) {

      $scope.model.loading = true;

      for (var i = 0; i < $files.length; i++) {
          var file = $files[i];

          var url;
          if (isIE) {
             url = ACTIVITI.CONFIG.contextRoot + '/app/rest/app-definitions/text/import?renewIdmEntries=' + $scope.model.renewIdmIds;
          } else {
              url = ACTIVITI.CONFIG.contextRoot + '/app/rest/app-definitions/import?renewIdmEntries=' + $scope.model.renewIdmIds;
          }
          Upload.upload({
              url: url,
              method: 'POST',
              file: file
          }).progress(function(evt) {
              $scope.model.uploadProgress = parseInt(100.0 * evt.loaded / evt.total);

          }).success(function(data, status, headers, config) {
              $scope.model.loading = false;

              $location.path("/apps/" + data.id);
              $scope.$hide();

          }).error(function(data, status, headers, config) {

              if (data && data.message) {
                  $scope.model.errorMessage = data.message;
              }

              $scope.model.error = true;
              $scope.model.loading = false;
          });
      }
  };

  $scope.cancel = function () {
      if(!$scope.model.loading) {
          $scope.$hide();
      }
  };
}]);
