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

    var description = '';
    if ($rootScope.currentForm.description)
    {
    	description = $rootScope.currentForm.description;
    }

    var saveDialog = { name: $rootScope.currentForm.name,
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

        if (!$scope.saveDialog.name || $scope.saveDialog.name.length == 0) {
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

        var errorCallback = function() {
            $scope.$hide();
            // TODO: i18n
            $rootScope.addAlert("Form '" + $scope.saveDialog.name, ' could not be saved');
        };

        FormBuilderService.saveForm(data, $scope.saveDialog.name, $scope.saveDialog.description, saveCallback, errorCallback);
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
