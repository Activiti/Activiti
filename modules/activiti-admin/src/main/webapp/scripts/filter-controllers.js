/*
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
'use strict';

/* Controllers */

activitiAdminApp.controller('ProcessDefinitionFilterController', ['$scope', '$rootScope', '$http', '$timeout',
    function ($scope, $rootScope, $http, $timeout) {
	
	$scope.setFilterPropertyName = function(filterPropertyName) {
		$scope.filterPropertyName = filterPropertyName;
		
		$scope.filter[filterPropertyName] = "TEST";
	};
	
}]);
