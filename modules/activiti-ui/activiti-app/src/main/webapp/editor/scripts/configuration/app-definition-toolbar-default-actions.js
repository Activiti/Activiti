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

var APP_DEFINITION_TOOLBAR = {
    ACTIONS: {
    	
        saveModel: function (services) {

            _internalCreateModal({
                backdrop: true,
                keyboard: true,
                template: 'views/popup/app-definition-save-model.html?version=' + Date.now(),
                scope: services.$scope
            }, services.$modal, services.$scope);
        },

        help: function (services) {
            
        },
        
        feedback: function (services) {
            
        },
        
        closeEditor:  function (services) {
        	services.$location.path('/apps');
        }
    }
};

/** Custom controller for the save dialog */
angular.module('activitiModeler').controller('SaveAppDefinitionCtrl',
    [ '$rootScope', '$scope', '$http', '$route', '$location', '$translate',
    function ($rootScope, $scope, $http, $route, $location, $translate) {
	
    var description = '';
    if ($rootScope.currentAppDefinition.description)
    {
    	description = $rootScope.currentAppDefinition.description;
    }
    
    var saveDialog = { 
        name: $rootScope.currentAppDefinition.name,
        description: description,
        publish: false
    };
    
    $scope.saveDialog = saveDialog;
    
    $scope.status = {
        loading: false
    };

    $scope.cancel = function () {
    	$scope.$hide();
    };

    $scope.saveAndClose = function (force) {
    	$scope.save(function() {
    		$location.path('/apps');
    	}, force);
    };
    
    $scope.save = function (saveCallback, force) {

        if (!$scope.saveDialog.name || $scope.saveDialog.name.length == 0) {
            return;
        }

        // Indicator spinner image
        $scope.status.loading = true;
        
        var data = {
            appDefinition: $rootScope.currentAppDefinition,
            publish: $scope.saveDialog.publish
        };
        
        data.appDefinition.name = $scope.saveDialog.name;
        if ($scope.saveDialog.description && $scope.saveDialog.description.length > 0) {
        	data.appDefinition.description = $scope.saveDialog.description;
        }

        if (force !== undefined && force !== null && force === true) {
            data.force = true;
        }
        
        delete Array.prototype.toJSON;
        delete $scope.conflict;
        $http({method: 'PUT', url: ACTIVITI.CONFIG.contextRoot + '/app/rest/app-definitions/' + $rootScope.currentAppDefinition.id, data: data}).
            success(function(response, status, headers, config) {
                // Regular error
                if (response.error) {
                    $scope.status.loading = false;
                    
                } else {
                    $scope.$hide();
                    $rootScope.addAlert($translate.instant('APP.POPUP.SAVE-APP-SAVE-SUCCESS', 'info'));
                    if (saveCallback) {
                        saveCallback();
                    }
                }

            }).
            error(function(data, status, headers, config) {

                if (status === 409 && data && data.messageKey === 'app.publish.procdef.key.conflict') {
                    $scope.conflict = {
                        type: 'conflictingProcDefKey',
                        data: data.customData
                    };
                } else if(status === 409 && data && data.messageKey === 'app.publish.procdef.duplicate.keys') {
                    $scope.conflict = {
                        type: 'duplicateProcDefKeys',
                        data: data.customData
                    };
                } else if (status === 409 && data && data.messageKey === 'app.publish.process.model.already.used') {
                    $scope.conflict = {
                        type: 'processModelAlreadyUsed',
                        data: data.customData
                    };
                } else {
                    $scope.$hide();
                    $rootScope.addAlertPromise($translate('APP.POPUP.SAVE-APP-SAVE-FAIL'), 'error');
                }

            });
    };

    $scope.isOkButtonDisabled = function() {
        if ($scope.status.loading) {
            return false;
        } else if ($scope.error && $scope.error.hasCustomStencilItem) {
            return false;
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
                    $location.path('/apps');
                });
            }
        }
    };

}]);