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

angular.module('activitiModeler').controller('ActivitiDecisionTableReferenceCtrl',
    [ '$scope', '$modal', '$http', function($scope, $modal, $http) {
	
     // Config for the modal window
     var opts = {
         template:  'editor-app/configuration/properties/decisiontable-reference-popup.html?version=' + Date.now(),
         scope: $scope
     };

     // Open the dialog
     _internalCreateModal(opts, $modal, $scope);
}]);
 
angular.module('activitiModeler').controller('ActivitiDecisionTableReferencePopupCtrl', ['$rootScope', '$scope', '$http', '$location',
    function($rootScope, $scope, $http, $location) {

        $scope.state = {
            'loadingDecisionTables': true,
            'decisionTableError': false
        };

        $scope.popup = {
            'state': 'decisionTableReference'
        };

        $scope.foldersBreadCrumbs = [];

        // Make click outside dialog also call close.
        $scope.$parent.$on('modal.hide.before', function() {
            $scope.close();
            $scope.$parent.$apply();
        });

        // Close button handler
        $scope.close = function() {
            $scope.property.newVariablesMapping = undefined;
            $scope.property.mode = 'read';
            $scope.$hide();
        };

        // Selecting/deselecting a decision table
        $scope.selectDecisionTable = function(decisionTable, $event) {
            $event.stopPropagation();
            if ($scope.selectedDecisionTable && $scope.selectedDecisionTable.id && decisionTable.id == $scope.selectedDecisionTable.id) {
                // un-select the current selection
                $scope.selectedDecisionTable = null;
            } else {
                $scope.selectedDecisionTable = decisionTable;
            }
        };

        $scope.isSelected = function () {
            if ($scope.selectedDecisionTable && $scope.selectedDecisionTable.id) {
                return true;
            }
            return false;
        };

        // Saving the selected value
        $scope.save = function() {
            if ($scope.selectedDecisionTable) {
                $scope.property.value = {
                    'id': $scope.selectedDecisionTable.id,
                    'name': $scope.selectedDecisionTable.name,
                    'key': $scope.selectedDecisionTable.key
                };
                
            } else {
                $scope.property.value = null;
            }
            $scope.updatePropertyInModel($scope.property);
            $scope.close();
        };

        // Open the selected value
        $scope.open = function() {
            if ($scope.selectedDecisionTable) {
                $scope.property.value = {
                    'id': $scope.selectedDecisionTable.id,
                    'name': $scope.selectedDecisionTable.name,
                    'key': $scope.selectedDecisionTable.key
                };
                $scope.updatePropertyInModel($scope.property);

                var modelMetaData = $scope.editor.getModelMetaData();
                var json = $scope.editor.getJSON();
                json = JSON.stringify(json);

                var params = {
                    modeltype: modelMetaData.model.modelType,
                    json_xml: json,
                    name: modelMetaData.name,
                    key: modelMetaData.key,
                    description: modelMetaData.description,
                    newversion: false,
                    lastUpdated: modelMetaData.lastUpdated
                };

                // Update
                $http({
                    method: 'POST',
                    data: params,
                    ignoreErrors: true,
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
                    },
                    transformRequest: function (obj) {
	                    var str = [];
	                    for (var p in obj) {
	                        str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
	                    }
	                    return str.join("&");
	                },
                    url: KISBPM.URL.putModel(modelMetaData.modelId)
                })

                .success(function(data, status, headers, config) {
                        $scope.editor.handleEvents({
                            type: ORYX.CONFIG.EVENT_SAVED
                        });

						$rootScope.editorHistory.push({
	                        id: modelMetaData.modelId, 
	                        name: modelMetaData.name,
	                        key: modelMetaData.key,
	                        stepId: $scope.selectedShape.resourceId,
	                        type: 'bpmnmodel'
	                    });
	                    
	                    $location.path('decision-table-editor/' + $scope.selectedDecisionTable.id);
                    })
                    .error(function(data, status, headers, config) {

                    });
                
                $scope.close();
            }
        };

        $scope.newDecisionTable = function() {
            $scope.property.value.variablesmapping = [];

            $scope.popup.state = 'newDecisionTable';

            var modelMetaData = $scope.editor.getModelMetaData();

            $scope.model = {
                loading: false,
                decisionTable: {
                    name: '',
                    key: '',
                    description: '',
                    modelType: 4
                },
                defaultStencilSet: undefined,
                decisionTableStencilSets: []
            };
        };

        $scope.createDecisionTable = function() {

            if (!$scope.model.decisionTable.name || $scope.model.decisionTable.name.length == 0 ||
            	!$scope.model.decisionTable.key || $scope.model.decisionTable.key.length == 0) {
            	
                return;
            }

            var stencilSetId = $scope.model.decisionTable.stencilSet;
            $scope.model.loading = true;

            $http({
                method: 'POST',
                url: ACTIVITI.CONFIG.contextRoot + '/app/rest/models',
                data: $scope.model.decisionTable
            }).
            success(function(data, status, headers, config) {

                var newDecisionTableId = data.id;
                $scope.property.value = {
                    'id': newDecisionTableId,
                    'name': data.name,
                    'key': data.key
                };
                $scope.updatePropertyInModel($scope.property);

                var modelMetaData = $scope.editor.getModelMetaData();
                var json = $scope.editor.getJSON();
                json = JSON.stringify(json);

                var params = {
                    modeltype: modelMetaData.model.modelType,
                    json_xml: json,
                    name: modelMetaData.name,
                    key: modelMetaData.key,
                    description: modelMetaData.description,
                    newversion: false,
                    lastUpdated: modelMetaData.lastUpdated,
                    stencilSet: stencilSetId
                };

                // Update
                $http({
                    method: 'POST',
                    data: params,
                    ignoreErrors: true,
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
                    },
                    transformRequest: function (obj) {
	                    var str = [];
	                    for (var p in obj) {
	                        str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
	                    }
	                    return str.join("&");
	                },
                    url: KISBPM.URL.putModel(modelMetaData.modelId)
                })

                .success(function(data, status, headers, config) {
                        $scope.editor.handleEvents({
                            type: ORYX.CONFIG.EVENT_SAVED
                        });

                        $scope.model.loading = false;
                        $scope.$hide();
                        
                        $rootScope.editorHistory.push({
	                        id: modelMetaData.modelId, 
	                        name: modelMetaData.name,
	                        key: modelMetaData.key,
	                        stepId: $scope.selectedShape.resourceId,
	                        type: 'bpmnmodel'
	                    });
	                    
	                    $location.path('decision-table-editor/' + newDecisionTableId);
                    })
                    .error(function(data, status, headers, config) {
                        $scope.model.loading = false;
                        $scope.$hide();
                    });

            }).
            error(function(data, status, headers, config) {
                $scope.model.loading = false;
                $scope.model.errorMessage = data.message;
            });
        };

        $scope.cancel = function() {
            $scope.close();
        };

        $scope.resetCurrent = function () {
            for (var i = 0, found = false; i < $scope.decisionTables.length && found === false; i++) {
                var table = $scope.decisionTables[i];
                if (table.id === $scope.property.value.id) {
                    $scope.selectedDecisionTable = table;
                    found = true;
                }
            }
        };

        $scope.loadDecisionTables = function() {
            var modelMetaData = $scope.editor.getModelMetaData();
            $http.get(ACTIVITI.CONFIG.contextRoot + '/app/rest/decision-table-models')
                .success(
                    function(response) {
                        $scope.state.loadingDecisionTables = false;
                        $scope.state.decisionTableError = false;
                        $scope.decisionTables = response.data;
                        $scope.resetCurrent();
                    })
                .error(
                    function(data, status, headers, config) {
                        $scope.state.loadingDecisionTables = false;
                        $scope.state.decisionTableError = true;
                    });
        };

        if ($scope.property && $scope.property.value && $scope.property.value.id) {
            $scope.selectedDecisionTable = $scope.property.value;
            $scope.storedId = $scope.property.value.id;
        }

        $scope.loadDecisionTables();
    }
]);