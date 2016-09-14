/*
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
'use strict';

var DECISION_TABLE_TOOLBAR = {
    ACTIONS: {

        saveModel: function (services) {

            _internalCreateModal({
                backdrop: true,
                keyboard: true,
                template: 'views/popup/decision-table-save-model.html?version=' + Date.now(),
                scope: services.$scope
            }, services.$modal, services.$scope);
        },

        help: function (services) {

        },

        feedback: function (services) {

        },

        closeEditor:  function (services) {

            var callback = function() {
                services.$rootScope.decisiontableChanges = false;
                
                if (services.$rootScope.editorHistory.length > 0) {
                	var navigationObject = services.$rootScope.editorHistory.pop();
        			services.$location.path('/editor/' + navigationObject.id);
        		} else {
        			services.$location.path('/decision-tables');
        		}
            };

            if (services.$rootScope.decisiontableChanges == true) {

                services.$scope.$emit("decisionTableChangesEvent");

                var unbindMustSave = services.$scope.$on("mustSaveEvent", function(){
                    //save the decision table data
                    var description = '';
                    if (services.$rootScope.currentDecisionTable.description) {
                        description = services.$rootScope.currentDecisionTable.description;
                    }

                    var data = {
                        newVersion: false
                    };
                    
                    unbindEvents();
                    services.DecisionTableBuilderService.saveDecisionTable(data, services.$rootScope.currentDecisionTable.name, 
                    	null, description, callback);
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
    .controller('SaveDecisionTableCtrl', [ '$rootScope', '$scope', '$http', '$route', '$location', '$translate', 'DecisionTableService',
        function ($rootScope, $scope, $http, $route, $location, $translate, DecisionTableService) {

            var description = '';
            if ($rootScope.currentDecisionTableModel.description) {
                description = $rootScope.currentDecisionTableModel.description;
            }

            $scope.saveDialog = {
                name: $rootScope.currentDecisionTableModel.name,
                key: $rootScope.currentDecisionTableModel.key,
                description: description,
                newVersion: false,
                comment: ''
            };

            $scope.keyFieldPattern = /^[a-zA-Z_]\w*$/;

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
		            	$location.path('/decision-tables');
		            }
                });
            };

            $scope.save = function (additionalSaveCallback) {

                if (!$scope.saveDialog.name || $scope.saveDialog.name.length == 0 || !$scope.saveDialog.key || $scope.saveDialog.key.length == 0) {
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
                    
                    $rootScope.currentDecisionTableModel.name = $scope.saveDialog.name;
                    $rootScope.currentDecisionTableModel.key = $scope.saveDialog.key;
                    $rootScope.currentDecisionTableModel.description = $scope.saveDialog.description;
                    
                    $rootScope.addAlertPromise($translate('DECISION-TABLE-EDITOR.ALERT.SAVE-CONFIRM', {name: $scope.saveDialog.name}), 'info');
                    
                    if (additionalSaveCallback) {
                        additionalSaveCallback();
                    }
                    
                    $rootScope.decisionTableChanges = false;
                };

                var errorCallback = function(errorMessage) {
                	$scope.status.loading = false;
                    $scope.saveDialog.errorMessage = errorMessage.message;
                };

                DecisionTableService.saveDecisionTable(data, $scope.saveDialog.name, $scope.saveDialog.key, 
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
                            $location.path('/decision-tables');
                        });
                    }
                }
            };

        }]);
