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