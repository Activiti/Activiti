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
 * Service with small utility methods
 */
angular.module('activitiModeler').service('UtilityService', [ '$window', '$document', '$timeout', function ($window, $document, $timeout) {

    this.scrollToElement = function(elementId) {
        $timeout(function() {
            var someElement = angular.element(document.getElementById(elementId))[0];
            if (someElement) {
                if (someElement.getBoundingClientRect().top > $window.innerHeight) {
                    $document.scrollToElement(someElement, 0, 1000);
                }
            }
        });
    };

}]);