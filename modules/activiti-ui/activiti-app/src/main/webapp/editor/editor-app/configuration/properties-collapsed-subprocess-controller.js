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

angular.module('activitiModeler').controller('KisBpmCollapsedSubprocessReferenceCrtl',
    [ '$scope', '$modal', '$http', function($scope, $modal, $http) {
	
     // Config for the modal window
     var opts = {
         template:  'editor-app/configuration/properties/subprocess-reference-popup.html?version=' + Date.now(),
         scope: $scope
     };

     // Open the dialog
        _internalCreateModal(opts, $modal, $scope);
}]);

angular.module('activitiModeler').controller('KisBpmCollapsedSubprocessReferencePopupCrtl', [ '$scope', '$http', function($scope, $http) {
	
    $scope.state = {'loadingSubprocesses' : true, 'error' : false};
    
    // Close button handler
    $scope.close = function() {
    	$scope.property.mode = 'read';
        $scope.$hide();
    };
    
    // Selecting/deselecting a subprocess
    $scope.selectSubProcess = function(sub, $event) {
   	 	$event.stopPropagation();
   	 	if ($scope.selectedSubProcess && $scope.selectedSubProcess.id && sub.id == $scope.selectedSubProcess.id) {
   	 		// un-select the current selection
   	 		$scope.selectedSubProcess = null;
   	 	} else {
   	 		$scope.selectedSubProcess = sub;
   	 	}
    };
    
    // Saving the selected value
    $scope.save = function() {
   	 	if ($scope.selectedSubProcess) {
   	 		$scope.property.value = {'id' : $scope.selectedSubProcess.id, 'name' : $scope.selectedSubProcess.name};
   	 	} else {
   	 		$scope.property.value = null; 
   	 	}
   	 	$scope.updatePropertyInModel($scope.property);
   	 	$scope.close();
    };
    
    $scope.loadProcesses = function() {
   	 
    	$http.get(ACTIVITI.CONFIG.contextRoot + '/app/rest/models?filter=myprocesses')
    		.success(
    			function(response) {
    				$scope.state.loadingSubprocesses = false;
    				$scope.state.subprocessError = false;
    				$scope.subProcesses = response.data;
    			})
    		.error(
    			function(data, status, headers, config) {
    				$scope.state.loadingSubprocesses = false;
    				$scope.state.subprocessError = true;
    			});
    };
    
    if ($scope.property && $scope.property.value && $scope.property.value.id) {
   	 	$scope.selectedSubProcess = $scope.property.value;
    }
    
    $scope.loadProcesses();  
}]);