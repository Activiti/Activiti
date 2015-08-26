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
/**
 * Controller for profile mgmt
 */
activitiApp.controller('IdmProfileMgmtController', ['$rootScope', '$scope', '$modal', 'IdmService', '$translate',
    function ($rootScope, $scope, $modal, IdmService, $translate) {

        $rootScope.setMainPageById('profile');

        $scope.model = {
            loading: true
        };

        $scope.showUploadPictureModal = function() {
            _internalCreateModal({
                scope: $scope,
                template: 'views/popup/idm-profile-picture-upload.html',
                show: true
            }, $modal, $scope);
        };

        $scope.emailChanged = function() {
            $scope.model.profile.emailErrorMessage = undefined;
            if ($scope.model.profile.email !== null
                && $scope.model.profile.email !== undefined
                && $scope.model.profile.email !== '') {
                $scope.model.loading = true;

                IdmService.updateProfileDetails($scope.model.profile, function (response) {
                    $scope.model.editingEmail = false;
                    $scope.loadProfile(); // reload values from server
                }, function (data, status) {
                    $scope.model.loading = false;
                    if (status === 409) {
                        $scope.model.profile.emailErrorMessage = $translate.instant(data.message);
                    }
                });
            } else {
                // Reset if invalid value
                $scope.model.profile.email = $scope.model.originalEmail;
            }
        };

        $scope.firstNameChanged = function() {
            $scope.model.loading = true;
            IdmService.updateProfileDetails($scope.model.profile, function (response) {
                $scope.model.editingFirstName = false;
                $scope.model.loading = false;
            });
        };

        $scope.lastNameChanged = function() {
            $scope.model.loading = true;
            IdmService.updateProfileDetails($scope.model.profile, function () {
                $scope.model.editingLastName = false;
                $scope.model.loading = false;
            });
        };

        $scope.companyChanged = function() {
            $scope.model.loading = true;
            IdmService.updateProfileDetails($scope.model.profile, function () {
                $scope.model.editingCompany = false;
                $scope.model.loading = false;
            });
        };

        $scope.showChangePasswordModal = function() {
            $scope.model.changePassword = {};
            _internalCreateModal({
                scope: $scope,
                template: 'views/popup/idm-change-password.html',
                show: true
            }, $modal, $scope);
        };

        // Fetch profile when page is shown
        $scope.loadProfile = function() {
            IdmService.getProfile().then(function (profileData) {
                $scope.model.originalEmail = profileData.email; // Storing it extra, so we're able to reset
                $scope.model.profile = profileData;
                $scope.model.loading = false;
            });
        };
        $scope.loadProfile();

        IdmService.getIdmInfo().then(function(data) {
            $scope.model.showChangePasswordButton = (data !== null && data !== undefined && data === 'default');
        });

    }]);

activitiApp.
    controller('UploadUserPictureController', ['$rootScope', '$scope', 'Upload', function ($rootScope, $scope, Upload) {

        $scope.popup = {
            loading: false
        };

        $scope.onFileSelect = function($files) {

            $scope.popup.loading = true;

            for (var i = 0; i < $files.length; i++) {
                var file = $files[i];
                Upload.upload({
                    url: ACTIVITI.CONFIG.contextRoot + '/app/rest/admin/profile-picture',
                    method: 'POST',
                    file: file
                }).progress(function(evt) {
                    $scope.popup.uploadProgress = parseInt(100.0 * evt.loaded / evt.total);

                }).success(function(data, status, headers, config) {
                    $scope.popup.loading = false;
                    $scope.$hide();
                    $scope.loadProfile();
                }).error(function(data, status, headers, config) {

                    if (data && data.message) {
                        $scope.popup.errorMessage = data.message;
                    }

                    $scope.popup.error = true;
                    $scope.popup.loading = false;
                });
            }
        };

        $scope.cancel = function () {
            if(!$scope.popup.loading) {
                $scope.$hide();
            }
        };

    }]);

activitiApp.
    controller('IdmChangePasswordController', ['$rootScope', '$scope', 'IdmService', function ($rootScope, $scope, IdmService) {

        $scope.isConfirmButtonDisabled = function() {
            return !$scope.model.changePassword.originalPassword
                || $scope.model.changePassword.originalPassword.length == 0
                || !$scope.model.changePassword.newPassword
                || $scope.model.changePassword.newPassword.length === 0
                || !$scope.model.changePassword.newPassword2
                || $scope.model.changePassword.newPassword2.length === 0
                || $scope.model.changePassword.newPassword !== $scope.model.changePassword.newPassword2;
        };

        $scope.showPasswordsDontMatch = function() {
            return $scope.model.changePassword.originalPassword
                && $scope.model.changePassword.originalPassword.length > 0
                && $scope.model.changePassword.newPassword
                && $scope.model.changePassword.newPassword.length > 0
                && $scope.model.changePassword.newPassword2
                && $scope.model.changePassword.newPassword2.length > 0
                && $scope.model.changePassword.newPassword !== $scope.model.changePassword.newPassword2;

        };

        $scope.changePassword = function() {
            $scope.model.changePassword.error = false;
            IdmService.changePassword($scope.model.changePassword.originalPassword,  $scope.model.changePassword.newPassword)
                .then(function() {
                    $scope.$hide();
                }, function() {
                    $scope.model.changePassword.error = true
                });
        };

    }]);







