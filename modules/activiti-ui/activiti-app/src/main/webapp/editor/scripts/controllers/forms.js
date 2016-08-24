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
  .controller('FormsCtrl', ['$rootScope', '$scope', '$translate', '$http', '$timeout','$location', '$modal', function ($rootScope, $scope, $translate, $http, $timeout, $location, $modal) {

      // Main page (needed for visual indicator of current page)
      $rootScope.setMainPageById('forms');
      $rootScope.formItems = undefined;

      // get latest thumbnails
      $scope.imageVersion = Date.now();

	  $scope.model = {
        filters: [
            {id: 'myReusableForms', labelKey: 'MY-REUSABLE-FORMS'}
		],

		sorts: [
		        {id: 'modifiedDesc', labelKey: 'MODIFIED-DESC'},
		        {id: 'modifiedAsc', labelKey: 'MODIFIED-ASC'},
		        {id: 'nameAsc', labelKey: 'NAME-ASC'},
		        {id: 'nameDesc', labelKey: 'NAME-DESC'}
		]
	  };

	  if ($rootScope.formFilter) {
		  $scope.model.activeFilter = $rootScope.formFilter.filter;
		  $scope.model.activeSort = $rootScope.formFilter.sort;
		  $scope.model.filterText = $rootScope.formFilter.filterText;

	  } else {
		  // By default, show first filter and use first sort
	      $scope.model.activeFilter = $scope.model.filters[0];
	      $scope.model.activeSort = $scope.model.sorts[0];
	      $rootScope.formFilter = {
	        filter: $scope.model.activeFilter,
	        sort: $scope.model.activeSort,
	        filterText: ''
	      };
	  }

	  $scope.activateFilter = function(filter) {
		  $scope.model.activeFilter = filter;
		  $rootScope.formFilter.filter = filter;
		  $scope.loadForms();
	  };

	  $scope.activateSort = function(sort) {
		  $scope.model.activeSort = sort;
		  $rootScope.formFilter.sort = sort;
		  $scope.loadForms();
	  };

	  $scope.loadForms = function() {
		  $scope.model.loading = true;

		  var params = {
		      filter: $scope.model.activeFilter.id,
		      sort: $scope.model.activeSort.id,
		      modelType: 2
		  };

		  if ($scope.model.filterText && $scope.model.filterText != '') {
		    params.filterText = $scope.model.filterText;
		  }

		  $http({method: 'GET', url: ACTIVITI.CONFIG.contextRoot + '/app/rest/models', params: params}).
		  	success(function(data, status, headers, config) {
	    		$scope.model.forms = data;
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
	          $rootScope.formFilter.filterText = $scope.model.filterText;
	          $scope.loadForms();
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

	  $scope.createForm = function() {
	      $rootScope.currentKickstartModel = undefined;
		  $scope.createFormCallback = function(result) {
		      $rootScope.editorHistory = [];
			  $location.path("/form-editor/" + result.id);
		  };
          _internalCreateModal({
			  template: 'views/popup/form-create.html?version=' + Date.now(),
			  scope: $scope
		  }, $modal, $scope);
	  };

	  $scope.showFormDetails = function(form) {
	      if (form) {
	          $rootScope.editorHistory = [];
	          $location.path("/forms/" + form.id);
	      }
	  };

	  $scope.editFormDetails = function(form) {
		  if (form) {
		      $rootScope.editorHistory = [];
			  $location.path("/form-editor/" + form.id);
		  }
	  };

	  // Finally, load initial forms
	  $scope.loadForms();
  }]);


angular.module('activitiModeler')
.controller('CreateNewFormCtrl', ['$rootScope', '$scope', '$http',
    function ($rootScope, $scope, $http) {

    $scope.model = {
       loading: false,
       form: {
            name: '',
            key: '',
            description: '',
            modelType: 2
       }
    };

    $scope.ok = function () {

        if (!$scope.model.form.name || $scope.model.form.name.length == 0 ||
        	!$scope.model.form.key || $scope.model.form.key.length == 0) {
        	
            return;
        }

        $scope.model.loading = true;

        $http({method: 'POST', url: ACTIVITI.CONFIG.contextRoot + '/app/rest/models', data: $scope.model.form}).
            success(function(data, status, headers, config) {
                $scope.$hide();
                $scope.model.loading = false;

                if ($scope.createFormCallback) {
                	$scope.createFormCallback(data);
                	$scope.createFormCallback = undefined;
                }

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
	.controller('DuplicateFormCtrl', ['$rootScope', '$scope', '$http',
		function ($rootScope, $scope, $http) {

			$scope.model = {
				loading: false,
				form: {
					id: '',
					name: '',
					key: '',
					description: '',
					modelType: 2
				}
			};

			if ($scope.originalModel) {
				//clone the model
				$scope.model.form.name = $scope.originalModel.form.name;
				$scope.model.form.key = $scope.originalModel.form.key;
				$scope.model.form.description = $scope.originalModel.form.description;
				$scope.model.form.modelType = $scope.originalModel.form.modelType;
				$scope.model.form.id = $scope.originalModel.form.id;
			}

			$scope.ok = function () {

				if (!$scope.model.form.name || $scope.model.form.name.length == 0 ||
					!$scope.model.form.key || $scope.model.form.key.length == 0) {
					
					return;
				}

				$scope.model.loading = true;

				$http({method: 'POST', url: ACTIVITI.CONFIG.contextRoot + '/app/rest/models/'+$scope.model.form.id+'/clone', data: $scope.model.form}).
					success(function(data, status, headers, config) {
						$scope.$hide();
						$scope.model.loading = false;

						if ($scope.duplicateFormCallback) {
							$scope.duplicateFormCallback(data);
							$scope.duplicateFormCallback = undefined;
						}

					}).
					error(function(data, status, headers, config) {
						$scope.model.loading = false;
						$scope.$hide();
					});
			};

			$scope.cancel = function () {
				if(!$scope.model.loading) {
					$scope.$hide();
				}
			};
		}]);