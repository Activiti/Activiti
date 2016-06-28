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

activitiApp.controller('AboutActivitiPopupCrtl', ['$rootScope', '$scope', '$http', '$translate', '$interval', '$dateFormatter', function($rootScope, $scope, $http, $translate, $interval, $dateFormatter) {
    $scope.popup = {
        loading: true,
        activitiVersion: {},
        licenseHolder: ''
    };

    $http({method: 'GET', url: ACTIVITI.CONFIG.contextRoot + '/app/rest/about-info'}).
        success(function(response, status, headers, config) {
            $scope.popup.licenseHolder = response.holder;
            $scope.popup.activitiVersion = response.versionInfo.edition + ' v' + response.versionInfo.majorVersion + '.' + response.versionInfo.minorVersion + '.' + response.versionInfo.revisionVersion;
            $scope.popup.activitiVersionType = response.versionInfo.type;
            if (response.goodBeforeDate) {
                $scope.popup.goodBeforeDate = $dateFormatter.formatDate(new Date(response.goodBeforeDate));
            }
            $scope.popup.loading = false;
        }).
        error(function(response, status, headers, config) {
            $scope.popup.loading = false;
        });


    $scope.cancel = function() {
        $scope.close();
    };


    $scope.close = function() {
        $scope.$hide();
    }

}]);