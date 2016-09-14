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

var FORM_TOOLBAR = {
    ACTIONS: {

        saveModel: function (services) {

            _internalCreateModal({
                backdrop: true,
                keyboard: true,
                template: 'views/popup/form-save-model.html?version=' + Date.now(),
                scope: services.$scope
            }, services.$modal, services.$scope);
        },

        help: function (services) {

        },

        closeEditor:  function (services) {
            if (services.$rootScope.editorHistory.length > 0) {
        		var callback = function() {
                    services.$rootScope.formChanges = false;

        		    var navigationObject = services.$rootScope.editorHistory.pop();
        		    services.$location.path('/editor/' + navigationObject.id);
        		};

        		if (services.$rootScope.formChanges == true) {

        		    services.$scope.$emit("formChangesEvent");

        		    var unbindMustSave = services.$scope.$on("mustSaveEvent", function(){
        		        //save the form data
        		        var description = '';
        		        if (services.$rootScope.currentForm.description) {
        		            description = services.$rootScope.currentForm.description;
        		        }

        		        var data = {
        		            newVersion: false
        		        };
        		        unbindEvents();
        		        services.FormBuilderService.saveForm(data, services.$rootScope.currentForm.name, description, callback);
        		    });

        		    var unbindDiscardDataEvent = services.$scope.$on("discardDataEvent", function() {
        		        unbindEvents();
        		        callback();
        		    });

        		    var unbindContinueEditingEvent = services.$scope.$on("continueEditingEvent", function () {
        		        unbindEvents();
        		    });

        		} else {
        		    callback();
        		}

            } else {
            	services.$location.path('/forms');
            }

            var unbindEvents = function () {
                unbindContinueEditingEvent();
                unbindMustSave();
                unbindDiscardDataEvent();
            };

        }
    }
};

/** Custom controller for the save dialog */
angular.module('activitiModeler')
    .controller('SaveFormCtrl', [ '$rootScope', '$scope', '$http', '$route', '$location', '$translate', 'FormBuilderService',
                         function ($rootScope, $scope, $http, $route, $location, $translate, FormBuilderService) {

	var formKey = '';
    if ($rootScope.currentForm.key) {
    	formKey = $rootScope.currentForm.key;
    }
	
    var description = '';
    if ($rootScope.currentForm.description) {
    	description = $rootScope.currentForm.description;
    }

    var saveDialog = { name: $rootScope.currentForm.name,
    		formKey: formKey,
            description: description,
            reusable: false,
            newVersion: false,
            comment: ''};

    $scope.saveDialog = saveDialog;

    $scope.status = {
        loading: false
    };

    $scope.cancel = function () {
    	$scope.$hide();
    };

    $scope.saveAndClose = function () {
    	$scope.save(function() {
    	    if ($rootScope.editorHistory.length > 0) {
    	        var navigationObject = $rootScope.editorHistory.pop();
    	        $location.path('/editor/' + navigationObject.id);
 
            } else {
            	$location.path('/forms');
            }
    	});
    };

    $scope.save = function (additionalSaveCallback) {

        if (!$scope.saveDialog.name || $scope.saveDialog.name.length == 0 ||
        	!$scope.saveDialog.formKey || $scope.saveDialog.formKey.length == 0) {
        	
            return;
        }

        // Indicator spinner image
        $scope.status = {
        	loading: true
        };

        var data = {
        	reusable: $scope.saveDialog.reusable,
        	newVersion: $scope.saveDialog.newVersion,
        	comment: $scope.saveDialog.comment
        };

        var saveCallback = function() {
            $scope.$hide();
            // TODO: i18n
            $rootScope.addAlert("Saved form '" + $scope.saveDialog.name, 'info');
            if (additionalSaveCallback) {
                additionalSaveCallback();
            }

            $rootScope.formChanges = false;
        };

        var errorCallback = function(errorMessage) {
        	$scope.status.loading = false;
            $scope.saveDialog.errorMessage = errorMessage.message;
        };

        FormBuilderService.saveForm(data, $scope.saveDialog.name, $scope.saveDialog.formKey, 
        	$scope.saveDialog.description, saveCallback, errorCallback);
    };

    $scope.isOkButtonDisabled = function() {
        if ($scope.status.loading) {
            return false;
        } else if ($scope.error && $scope.error.conflictResolveAction) {
            if ($scope.error.conflictResolveAction === 'saveAs') {
                return !$scope.error.saveAs || $scope.error.saveAs.length == 0;
            } else {
                return false;
            }
        }
        return true;
    };

    $scope.okClicked = function() {
        if ($scope.error) {
            if ($scope.error.conflictResolveAction === 'discardChanges') {
                $scope.close();
                $route.reload();
            } else if ($scope.error.conflictResolveAction === 'overwrite'
                || $scope.error.conflictResolveAction === 'newVersion') {
                $scope.save();
            } else if($scope.error.conflictResolveAction === 'saveAs') {
                $scope.save(function() {
                    $rootScope.ignoreChanges = true;  // Otherwise will get pop up that changes are not saved.
                    if ($rootScope.editorHistory.length > 0) {
                        var navigationObject = $rootScope.editorHistory.pop();
                        $location.path('/editor/' + navigationObject.id);
                        
                    } else {
                    	$location.path('/forms');
                    }
                });
            }
        }
    };

}]);
