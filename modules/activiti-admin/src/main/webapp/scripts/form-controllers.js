/*
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
'use strict';

activitiAdminApp.controller('FormController', ['$scope', '$rootScope', '$http', '$timeout', '$location', '$routeParams', '$modal', '$translate', '$q', 'gridConstants',
    function ($scope, $rootScope, $http, $timeout, $location, $routeParams, $modal, $translate, $q, gridConstants) {
        $rootScope.navigation = {selection: 'forms'};
        
        $scope.returnToList = function () {
            $location.path("/forms");
        };

        $scope.showForm = function () {
            $modal.open({
                templateUrl: 'views/form-popup.html',
                windowClass: 'modal modal-full-width',
                controller: 'ShowFormPopupCrtl',
                resolve: {
                    form: function () {
                        return $scope.form;
                    }
                }
            });
        };

        $scope.showSubmittedForm = function (submittedForm) {
            if (submittedForm && submittedForm.getProperty('id')) {
                $location.path("/submitted-form/"+submittedForm.getProperty('id'));
            }
        };
        
        $scope.openApp = function(appId) {
            if (appId) {
                $location.path("/app/" + appId);
            }
        };
        
        $scope.openDeployment = function(deploymentId) {
            if (deploymentId) {
                $location.path("/deployment/" + deploymentId);
            }
        };

        $q.all([$translate('SUBMITTED-FORM.HEADER.ID'),
                $translate('SUBMITTED-FORM.HEADER.TASK-ID'),
                $translate('SUBMITTED-FORM.HEADER.PROCESS-ID'),
                $translate('SUBMITTED-FORM.HEADER.SUBMITTED'),
                $translate('SUBMITTED-FORM.HEADER.SUBMITTED-BY')])
            .then(function (headers) {

                $scope.gridSubmittedForms = {
                    data: 'submittedForms.data',
                    enableRowReordering: false,
                    multiSelect: false,
                    keepLastSelected: false,
                    enableSorting: false,
                    rowHeight: 36,
                    afterSelectionChange: $scope.showSubmittedForm,
                    columnDefs: [
                        {field: 'id', displayName: headers[0]},
                        {field: 'taskId', displayName: headers[1]},
                        {field: 'processId', displayName: headers[2]},
                        {field: 'submitted', displayName: headers[3], cellTemplate: gridConstants.dateTemplate},
                        {field: 'submittedBy', displayName: headers[4], cellTemplate: gridConstants.userObjectTemplate}
                    ]
                };
            });

        $scope.executeWhenReady(function () {
            // Load form
            $http({method: 'GET', url: '/app/rest/activiti/forms/' + $routeParams.formId}).
            success(function (data, status, headers, config) {
                $scope.form = data;

                // Load form submitted forms
                $http({
                    method: 'GET',
                    url: '/app/rest/activiti/form-submitted-forms/' + $routeParams.formId
                }).
                success(function (submittedFormsData, status, headers, config) {
                    $scope.submittedForms = submittedFormsData;
                });
            }).
            error(function (data, status, headers, config) {
                if (data && data.message) {
                    // Extract error-message
                    $rootScope.addAlert(data.message, 'error');
                } else {
                    // Use default error-message
                    $rootScope.addAlert($translate.instant('ALERT.GENERAL.HTTP-ERROR'), 'error');
                }
            });
        });

    }]);

activitiAdminApp.controller('ShowFormPopupCrtl',
    ['$rootScope', '$scope', '$modalInstance', '$http', 'form', '$timeout', '$translate', 'uiGridConstants',
        function ($rootScope, $scope, $modalInstance, $http, form, $timeout, $translate, uiGridConstants) {

            $scope.status = {loading: false};

            $scope.cancel = function () {
                if (!$scope.status.loading) {
                    $modalInstance.dismiss('cancel');
                }
            };

            $scope.popup = {};
            
            $scope.formTabClicked = function(tab) {
                $scope.activeFormTab = tab;
            };

            $scope.executeWhenReady(function () {
                // Load form definition
                $http({
                    method: 'GET',
                    url: '/app/rest/activiti/forms/' + form.id + '/editorJson'
                }).
                success(function (data, status, headers, config) {
                    $scope.popup.currentForm = data;
                    $scope.popup.formName = form.name || '';
                    resetActiveFormTab(data);
                }).
                error(function (data, status, headers, config) {
                    if (data && data.message) {
                        // Extract error-message
                        $rootScope.addAlert(data.message, 'error');
                    } else {
                        // Use default error-message
                        $rootScope.addAlert($translate.instant('ALERT.GENERAL.HTTP-ERROR'), 'error');
                    }
                });
            });

            function resetActiveFormTab(form) {
                if (form.tabs && form.tabs.length > 0) {
                    $scope.activeFormTab = form.tabs[0];
                } else {
                    $scope.activeFormTab = undefined;
                }
            };
            
        }]);
