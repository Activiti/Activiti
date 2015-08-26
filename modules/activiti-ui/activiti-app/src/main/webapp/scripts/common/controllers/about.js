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