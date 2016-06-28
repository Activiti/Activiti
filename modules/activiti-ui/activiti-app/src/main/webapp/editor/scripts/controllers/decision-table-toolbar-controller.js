/*
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
angular.module('activitiModeler')
    .controller('DecisionTableToolbarController', ['$scope', '$http', '$modal', '$q', '$rootScope', '$translate', '$location', 'DecisionTableService',
        function ($scope, $http, $modal, $q, $rootScope, $translate, $location, DecisionTableService) {

    	var toolbarItems = DECISION_TABLE_TOOLBAR_CONFIG.items;
        $scope.items = [];
        
        for (var i = 0; i < toolbarItems.length; i++)
        {
        	$scope.items.push(toolbarItems[i]);
        }
        
        $scope.secondaryItems = DECISION_TABLE_TOOLBAR_CONFIG.secondaryItems;

        // Call configurable click handler (From http://stackoverflow.com/questions/359788/how-to-execute-a-javascript-function-when-i-have-its-name-as-a-string)
        var executeFunctionByName = function(functionName, context /*, args */) {
            var args = Array.prototype.slice.call(arguments).splice(2);
            var namespaces = functionName.split(".");
            var func = namespaces.pop();
            for(var i = 0; i < namespaces.length; i++) {
                context = context[namespaces[i]];
            }
            return context[func].apply(this, args);
        };

        // Click handler for toolbar buttons
        $scope.toolbarButtonClicked = function(buttonIndex) {

            // Default behaviour
            var buttonClicked = $scope.items[buttonIndex];
            var services = { '$scope' : $scope, '$rootScope' : $rootScope, '$http' : $http, '$modal' : $modal, '$q' : $q, '$translate' : $translate, 'DecisionTableService': DecisionTableService};
            executeFunctionByName(buttonClicked.action, window, services);
        };
        
        // Click handler for secondary toolbar buttons
        $scope.toolbarSecondaryButtonClicked = function(buttonIndex) {
            var buttonClicked = $scope.secondaryItems[buttonIndex];
            var services = { '$scope' : $scope, '$rootScope' : $rootScope, '$http' : $http, '$modal' : $modal, '$q' : $q, '$translate' : $translate, '$location': $location, 'DecisionTableService': DecisionTableService };
            executeFunctionByName(buttonClicked.action, window, services);
        };
}]);