'use strict';

angular.module('activitiApp')
    .controller('MainCtrl', function ($scope, $location) {

        $scope.currentTab = 0;

        $scope.tabClicked = function (index) {
            console.log('Tab clicked ' + index);
            if (index == 0) {
                $location.path('/tasks');
            } else if (index == 1) {
                $location.path('/processes');
            }

            $scope.currentTab = index;
        };

    });


