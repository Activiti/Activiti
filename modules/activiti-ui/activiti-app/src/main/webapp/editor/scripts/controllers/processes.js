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
  .controller('ProcessesCtrl', ['$rootScope', '$scope', '$translate', '$http', '$timeout','$location', '$modal', function ($rootScope, $scope, $translate, $http, $timeout, $location, $modal) {

      // Main page (needed for visual indicator of current page)
      $rootScope.setMainPageById('processes');
      $rootScope.formItems = undefined;

      // get latest thumbnails
      $scope.imageVersion = Date.now();

	  $scope.model = {
        filters: [
            {id: 'myProcesses', labelKey: 'MY-PROCESSES'}
		],

		sorts: [
		        {id: 'modifiedDesc', labelKey: 'MODIFIED-DESC'},
		        {id: 'modifiedAsc', labelKey: 'MODIFIED-ASC'},
		        {id: 'nameAsc', labelKey: 'NAME-ASC'},
		        {id: 'nameDesc', labelKey: 'NAME-DESC'}
		]
	  };

	  if ($rootScope.modelFilter) {
		  $scope.model.activeFilter = $rootScope.modelFilter.filter;
		  $scope.model.activeSort = $rootScope.modelFilter.sort;
		  $scope.model.filterText = $rootScope.modelFilter.filterText;

	  } else {
		  // By default, show first filter and use first sort
	      $scope.model.activeFilter = $scope.model.filters[0];
	      $scope.model.activeSort = $scope.model.sorts[0];
	      $rootScope.modelFilter = {
	        filter: $scope.model.activeFilter,
	        sort: $scope.model.activeSort,
	        filterText: ''
	      };
	  }

	  $scope.activateFilter = function(filter) {
		  $scope.model.activeFilter = filter;
		  $rootScope.modelFilter.filter = filter;
		  $scope.loadProcesses();
	  };

	  $scope.activateSort = function(sort) {
		  $scope.model.activeSort = sort;
		  $rootScope.modelFilter.sort = sort;
		  $scope.loadProcesses();
	  };

	  $scope.loadProcesses = function() {
		  $scope.model.loading = true;

		  var params = {
		      filter: $scope.model.activeFilter.id,
		      sort: $scope.model.activeSort.id,
		      modelType: 0
		  };

		  if ($scope.model.filterText && $scope.model.filterText != '') {
		    params.filterText = $scope.model.filterText;
		  }

		  $http({method: 'GET', url: ACTIVITI.CONFIG.contextRoot + '/app/rest/models', params: params}).
		  	success(function(data, status, headers, config) {
	    		$scope.model.processes = data;
	    		$scope.model.loading = false;
	        }).
	        error(function(data, status, headers, config) {
	           console.log('Something went wrong: ' + data);
	           $scope.model.loading = false;
	        });
	  };

	  var timeoutFilter = function() {
	      $scope.model.isFilterDelayed = true;
	      $timeout(function() {
	          $scope.model.isFilterDelayed = false;
	          if ($scope.model.isFilterUpdated) {
	              $scope.model.isFilterUpdated = false;
	              timeoutFilter();
	          } else {
	              $scope.model.filterText = $scope.model.pendingFilterText;
	              $rootScope.modelFilter.filterText = $scope.model.filterText;
	              $scope.loadProcesses();
	          }
	      }, 500);
	  };

	  $scope.filterDelayed = function() {
	      if ($scope.model.isFilterDelayed) {
	          $scope.model.isFilterUpdated = true;
	      } else {
	          timeoutFilter();
	      }
	  };

	  $scope.createProcess = function(mode) {
	    var modalInstance = _internalCreateModal({
	        template: 'views/popup/process-create.html?version=' + Date.now()
	    }, $modal, $scope);
	  };

	  $scope.importProcess = function () {
          _internalCreateModal({
              template: 'views/popup/process-import.html?version=' + Date.now()
          }, $modal, $scope);
	  };

	  $scope.showProcessDetails = function(process) {
	      if (process) {
	          $rootScope.editorHistory = [];
	          $location.path("/processes/" + process.id);
	      }
	  };

	  $scope.editProcessDetails = function(process) {
		  if (process) {
		      $rootScope.editorHistory = [];
              $location.path("/editor/" + process.id);
		  }
	  };

	  // Finally, load initial processes
	  $scope.loadProcesses();
  }]);

angular.module('activitiModeler')
.controller('CreateNewProcessModelCrtl', ['$rootScope', '$scope', '$modal', '$http', '$location',
                                          function ($rootScope, $scope, $modal, $http, $location) {

    $scope.model = {
       loading: false,
       process: {
            name: '',
            key: '',
            description: '',
           	modelType: 0
       }
    };

    if ($scope.initialModelType !== undefined) {
        $scope.model.process.modelType = $scope.initialModelType;
    }

    $scope.ok = function () {

        if (!$scope.model.process.name || $scope.model.process.name.length == 0 ||
        	!$scope.model.process.key || $scope.model.process.key.length == 0) {
        	
            return;
        }

        $scope.model.loading = true;

        $http({method: 'POST', url: ACTIVITI.CONFIG.contextRoot + '/app/rest/models', data: $scope.model.process}).
            success(function(data) {
                $scope.$hide();

                $scope.model.loading = false;
                $rootScope.editorHistory = [];
                $location.path("/editor/" + data.id);
            }).
            error(function(data, status, headers, config) {
                $scope.model.loading = false;
                $scope.model.errorMessage = data.message;
            });
    };

    $scope.cancel = function () {
        if(!$scope.model.loading) {
            $scope.$hide();
        }
    };
}]);

angular.module('activitiModeler')
.controller('DuplicateProcessModelCrtl', ['$rootScope', '$scope', '$modal', '$http', '$location',
                                          function ($rootScope, $scope, $modal, $http, $location) {

    $scope.model = {
       loading: false,
       process: {
            name: '',
            key: '',
            description: '',
            modelType:''
       }
    };

    if ($scope.originalModel) {
        //clone the model
        $scope.model.process.name = $scope.originalModel.process.name;
        $scope.model.process.key = $scope.originalModel.process.key;
        $scope.model.process.description = $scope.originalModel.process.description;
        $scope.model.process.id = $scope.originalModel.process.id;
        $scope.model.process.modelType = $scope.originalModel.process.modelType;
    }
    

    $scope.ok = function () {

        if (!$scope.model.process.name || $scope.model.process.name.length == 0 || 
        	!$scope.model.process.key || $scope.model.process.key.length == 0) {
        	
            return;
        }

        $scope.model.loading = true;

        $http({method: 'POST', url: ACTIVITI.CONFIG.contextRoot + '/app/rest/models/'+$scope.model.process.id+'/clone', data: $scope.model.process}).
            success(function(data) {
                $scope.$hide();

                $scope.model.loading = false;
                $rootScope.editorHistory = [];
                $location.path("/editor/" + data.id);
            }).
            error(function() {
                $scope.model.loading = false;
                $modal.$hide();
            });
    };

    $scope.cancel = function () {
        if(!$scope.model.loading) {
            $scope.$hide();
        }
    };
}]);

angular.module('activitiModeler')
.controller('ImportProcessModelCrtl', ['$rootScope', '$scope', '$http', 'Upload', '$location', function ($rootScope, $scope, $http, Upload, $location) {

  $scope.model = {
       loading: false
  };

  $scope.onFileSelect = function($files, isIE) {

      $scope.model.loading = true;

      for (var i = 0; i < $files.length; i++) {
          var file = $files[i];

          var url;
          if (isIE) {
              url = ACTIVITI.CONFIG.contextRoot + '/app/rest/import-process-model/text';
          } else {
              url = ACTIVITI.CONFIG.contextRoot + '/app/rest/import-process-model';
          }

          Upload.upload({
              url: url,
              method: 'POST',
              file: file
          }).progress(function(evt) {
              $scope.model.uploadProgress = parseInt(100.0 * evt.loaded / evt.total);

          }).success(function(data) {
              $scope.model.loading = false;

              $location.path("/editor/" + data.id);
              $scope.$hide();

          }).error(function(data) {

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
