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

        // To fix cache
        $scope.cacheBuster = function(force) {
            if (!$scope.model.cacheBuster || force) {
                $scope.model.cacheBuster = new Date().getTime();
            } else {
                return $scope.model.cacheBuster;
            }
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
                    $scope.cacheBuster(true);
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







