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
angular.module('activitiModeler').controller('KisBpmFormReferenceCrtl',
    [ '$scope', '$modal', '$http', function($scope, $modal, $http) {
	
     // Config for the modal window
     var opts = {
         template:  'editor-app/configuration/properties/form-reference-popup.html?version=' + Date.now(),
         scope: $scope
     };

     // Open the dialog
        _internalCreateModal(opts, $modal, $scope);
}]);

angular.module('activitiModeler').controller('KisBpmFormReferencePopupCrtl',
    [ '$rootScope', '$scope', '$http', '$location', function($rootScope, $scope, $http, $location) {
	 
	$scope.state = {'loadingForms' : true, 'formError' : false};
	
	$scope.popup = {'state' : 'formReference'};
    
    $scope.foldersBreadCrumbs = [];
    
    // Close button handler
    $scope.close = function() {
    	$scope.property.mode = 'read';
        $scope.$hide();
    };
    
    // Selecting/deselecting a subprocess
    $scope.selectForm = function(form, $event) {
   	 	$event.stopPropagation();
   	 	if ($scope.selectedForm && $scope.selectedForm.id && form.id == $scope.selectedForm.id) {
   	 		// un-select the current selection
   	 		$scope.selectedForm = null;
   	 	} else {
   	 		$scope.selectedForm = form;
   	 	}
    };
    
    // Saving the selected value
    $scope.save = function() {
   	 	if ($scope.selectedForm) {
   	 		$scope.property.value = {'id' : $scope.selectedForm.id, 'name' : $scope.selectedForm.name};
   	 	} else {
   	 		$scope.property.value = null; 
   	 	}
   	 	$scope.updatePropertyInModel($scope.property);
   	 	$scope.close();
    };
    
    // Open the selected value
    $scope.open = function() {
        if ($scope.selectedForm) {
            $scope.property.value = {'id' : $scope.selectedForm.id, 'name' : $scope.selectedForm.name};
            $scope.updatePropertyInModel($scope.property);
            
            var modelMetaData = $scope.editor.getModelMetaData();
            var json = $scope.editor.getJSON();
            json = JSON.stringify(json);

            var params = {
                modeltype: modelMetaData.model.modelType,
                json_xml: json,
                name: modelMetaData.name,
                description: modelMetaData.description,
                newversion: false,
                lastUpdated: modelMetaData.lastUpdated
            };

            // Update
            $http({ method: 'POST',
                data: params,
                ignoreErrors: true,
                headers: {'Accept': 'application/json',
                          'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'},
                transformRequest: function (obj) {
                    var str = [];
                    for (var p in obj) {
                        str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
                    }
                    return str.join("&");
                },
                url: KISBPM.URL.putModel(modelMetaData.modelId)})

                .success(function (data, status, headers, config) {
                    $scope.editor.handleEvents({
                        type: ORYX.CONFIG.EVENT_SAVED
                    });

                    var allSteps = EDITOR.UTIL.collectSortedElementsFromPrecedingElements($scope.selectedShape);

                    $rootScope.editorHistory.push({
                        id: modelMetaData.modelId, 
                        name: modelMetaData.name,
                        stepId: $scope.selectedShape.resourceId,
                        allSteps: allSteps,
                        type: 'bpmnmodel'
                    });
                    $location.path('form-editor/' + $scope.selectedForm.id);

                })
                .error(function (data, status, headers, config) {
                    
                });
            
            $scope.close();
        }
    };
    
    $scope.newForm = function() {
        $scope.popup.state = 'newForm';
        
        var modelMetaData = $scope.editor.getModelMetaData();
        
        $scope.model = {
            loading: false,
            form: {
                 name: '',
                 description: '',
                 modelType: 2,
                 referenceId:  modelMetaData.modelId    
            }
        };
    };
    
    $scope.createForm = function() {
        
        if (!$scope.model.form.name || $scope.model.form.name.length == 0) {
            return;
        }

        $scope.model.loading = true;

        $http({method: 'POST', url: ACTIVITI.CONFIG.contextRoot + '/app/rest/models', data: $scope.model.form}).
            success(function(data, status, headers, config) {
                
                var newFormId = data.id;
                $scope.property.value = {'id' : newFormId, 'name' : data.name};
                $scope.updatePropertyInModel($scope.property);
                
                var modelMetaData = $scope.editor.getModelMetaData();
                var json = $scope.editor.getJSON();
                json = JSON.stringify(json);

                var params = {
                    modeltype: modelMetaData.model.modelType,
                    json_xml: json,
                    name: modelMetaData.name,
                    description: modelMetaData.description,
                    newversion: false,
                    lastUpdated: modelMetaData.lastUpdated
                };

                // Update
                $http({ method: 'POST',
                    data: params,
                    ignoreErrors: true,
                    headers: {'Accept': 'application/json',
                              'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'},
                    transformRequest: function (obj) {
                        var str = [];
                        for (var p in obj) {
                            str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
                        }
                        return str.join("&");
                    },
                    url: KISBPM.URL.putModel(modelMetaData.modelId)})

                    .success(function (data, status, headers, config) {
                        $scope.editor.handleEvents({
                            type: ORYX.CONFIG.EVENT_SAVED
                        });
                        
                        $scope.model.loading = false;
                        $scope.$hide();

                        var allSteps = EDITOR.UTIL.collectSortedElementsFromPrecedingElements($scope.selectedShape);

                        $rootScope.editorHistory.push({
                            id: modelMetaData.modelId, 
                            name: modelMetaData.name, 
                            type: 'bpmnmodel',
                            stepId: $scope.selectedShape.resourceId,
                            allSteps: allSteps,
                        });
                        $location.path('form-editor/' + newFormId);

                    })
                    .error(function (data, status, headers, config) {
                        $scope.model.loading = false;
                        $scope.$hide();
                    });
                
            }).
            error(function(data, status, headers, config) {
                $scope.model.loading = false;
                $scope.$hide();
            });
    };
    
    $scope.cancel = function() {
        $scope.close();
    };

    $scope.loadForms = function() {
        var modelMetaData = $scope.editor.getModelMetaData();
        $http.get(ACTIVITI.CONFIG.contextRoot + '/app/rest/form-models?referenceId=' + modelMetaData.modelId)
            .success(
                function(response) {
                    $scope.state.loadingForms = false;
                    $scope.state.formError = false;
                    $scope.forms = response.data;
                })
            .error(
                function(data, status, headers, config) {
                    $scope.state.loadingForms = false;
                    $scope.state.formError = true;
                });
    };

    if ($scope.property && $scope.property.value && $scope.property.value.id) {
   	     $scope.selectedForm = $scope.property.value;
    }

    $scope.loadForms();
}]);