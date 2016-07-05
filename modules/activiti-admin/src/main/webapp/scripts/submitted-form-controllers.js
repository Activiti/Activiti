/*
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
'use strict';

/* Controllers */

activitiAdminApp.controller('SubmittedFormController', ['$rootScope', '$scope', '$http', '$timeout', '$location', '$translate', '$q', '$modal', '$routeParams',
    function ($rootScope, $scope, $http, $timeout, $location, $translate, $q, $modal, $routeParams) {
        
        $scope.returnToList = function () {
            $location.path("/forms");
        };

        $scope.showSubmittedForm = function () {
            $modal.open({
                templateUrl: 'views/form-render-popup.html',
                windowClass: 'modal modal-full-width',
                controller: 'ShowFormRenderPopupCrtl',
                resolve: {
                    form: function () {
                        return $scope.submittedForm;
                    }
                }
            });
        };
        
        $scope.executeWhenReady(function () {
            if ($rootScope.submittedForm) {
                $scope.submittedForm = $rootScope.submittedForm;
                $rootScope.submittedForm = undefined;
                return;
            }
            
            // Load submitted form
            $http({method: 'GET', url: '/app/rest/activiti/submitted-forms/' + $routeParams.submittedFormId}).
            success(function (data, status, headers, config) {
                $scope.submittedForm = data;
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

activitiAdminApp.controller('ShowFormRenderPopupCrtl',
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
                    if (!form.form) {
                        // Load form
                        $http({
                            method: 'GET',
                            url: '/app/rest/activiti/submitted-forms/' + form.id
                        }).
                        success(function (data, status, headers, config) {
                            $scope.popup.currentForm = data.form;
                            $scope.popup.formName = form.name || '';
                            resetActiveFormTab(data.form);
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
                    } else {
                        $scope.popup.currentForm = form.form;
                        $scope.popup.formName = form.name || '';
                        resetActiveFormTab(form.form);
                    }
                });

                function resetActiveFormTab(form) {
                    if (form.tabs && form.tabs.length > 0) {
                        $scope.activeFormTab = form.tabs[0];
                    } else {
                        $scope.activeFormTab = undefined;
                    }
                };
                
            }]);