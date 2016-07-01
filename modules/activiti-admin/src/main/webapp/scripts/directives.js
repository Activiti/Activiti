/*
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
'use strict';

angular.module('activitiAdminApp')
    .directive('autoFocus', ['$timeout', function($timeout) {
        return {
            restrict: 'AC',
            link: function(_scope, _element) {
                $timeout(function(){
                    _element[0].focus();
                }, 100);
            }
        };
    }])
    .directive('loadingIndicator', ['$translate', function($translate) {
        return {
            restrict: 'E',
            template: '<div class=\'loading pull-right\' ng-show=\'status.loading\'><div class=\'l1\'></div><div class=\'l2\'></div><div class=\'l2\'></div></div>'
        };
    }])
    .directive('activeMenu', ['$translate', function($translate) {
        return {
            restrict: 'A',
            link: function(scope, element, attrs, controller) {
                var language = attrs.activeMenu;

                scope.$watch(function() {
                    return $translate.uses();
                }, function(selectedLanguage) {
                    if (language === selectedLanguage) {
                        element.addClass('active');
                    } else {
                        element.removeClass('active');
                    }
                });
            }
        };
    }])
    .directive('activeLink', ['$location', function(location) {
        return {
            restrict: 'A',
            link: function(scope, element, attrs, controller) {
                var clazz = attrs.activeLink;
                var path = attrs.href;
                path = path.substring(1); //hack because path does bot return including hashbang
                scope.location = location;
                scope.$watch('location.path()', function(newPath) {
                    if (path === newPath) {
                        element.addClass(clazz);
                    } else {
                        element.removeClass(clazz);
                    }
                });
            }
        };
    }])
    .directive('hourChart', [function () {
        return {
            restrict: 'E',
            templateUrl: 'views/monitoring-hour-chart.html',
            scope: {
                model : '=model',
            },
            link: function ($scope, element, attributes) {
            }
        };
    }])
    .directive('weekdayChart', [function () {
        return {
            restrict: 'E',
            templateUrl: 'views/monitoring-weekday-chart.html',
            scope: {
                model : '=model',
            },
            link: function ($scope, element, attributes) {
            }
        };
    }])
    /** Typeahead scroll hack
     *  directive used in typeahead custom template that enables scrolling 
     *  Must be used in the outer most element of the template and
     *  passed it's index typeahedScrollItem="$parent.$index" */
    .directive('typeaheadScrollItem', function($timeout) {
        return {link: function(scope, element, attrs) {
        
            scope.$watch(attrs.typeaheadScrollItem, function (index) {
                scope.myIndex = index; // this element's index
            
                if (index === 0) { // if first item scroll to top
                    $timeout(function(){
                        element.parent().parent().scrollTop(0);
                    }, 100);
                }
            });
        
            // listen when parent's active index changes
            scope.$parent.$watch('active', function (active) {
                if (scope.myIndex === active) { // if this is the active item
                    var liElement = element.parent();
                    var ulElement = liElement.parent();
                    var elementTop = liElement.offset().top;
                    var minTop = ulElement.offset().top;
                    var maxTop = minTop + ulElement.height();
                    if(!(elementTop > minTop && elementTop < maxTop)) {
                        liElement[0].scrollIntoView();
                    }
                }
            });
        }};
    })
    // Form field preview
    .directive('fieldPreview', ['RecursionHelper', function (RecursionHelper) {
    return {
        restrict: 'E',
        templateUrl: 'views/field-preview-template.html',
        transclude: false,
        scope: {
            field: '=',
            customFieldTemplates: '='
        },
        compile: function (element) {
            return RecursionHelper.compile(element, this.link);
        },
        link: function ($scope, $element, attributes) {
            
            $scope.getHtmlTemplate = function(field) {
                return $scope.customFieldTemplates[field.type];
            };
            
            // initialize dynamic table if field's type is dynamic-table
            // or references a dynamic table
            
            if ($scope.field.type === 'dynamic-table') {
                if ($scope.field.columnDefinitions && $scope.field.columnDefinitions.length > 0) {
                    initializeDynamicTable($scope.field.columnDefinitions);
                }
            } else if ($scope.field.type === 'readonly') {
                if ($scope.field.params && $scope.field.params.field.columnDefinitions && $scope.field.params.field.columnDefinitions.length > 0) {
                    initializeDynamicTable($scope.field.params.field.columnDefinitions);
                }
            }
            
            function initializeDynamicTable (columnDefinitions) {
                
                var columnDefs = [];
                
                for (var i = 0; i < columnDefinitions.length; i++) {
                    if (columnDefinitions[i].visible) {
                        columnDefs.push({
                            'field': columnDefinitions[i].name,
                            'displayName': columnDefinitions[i].name,
                            'enableColumnMenu': false
                        });
                    }
                }
                
                $scope.gridOptions = {
                        columnDefs: columnDefs,
                        enableRowSelection: false,
                        enableSorting: false,
                        enableRowHeaderSelection: false,
                        multiSelect: false,
                        enableHorizontalScrollbar: 0,
                        modifierKeysToMultiSelect: false
                    };
            }
        }
    }
}])

    // Form element
    .directive('formElement', ['RecursionHelper', function (RecursionHelper) {
    return {
        restrict: 'E',
        templateUrl: 'views/form-element-template.html',
        transclude: false,
        scope: {
            field: '=',
            customFieldTemplates: '='
        },
        compile: function (element) {
            return RecursionHelper.compile(element, this.link);
        },
        link: function ($scope, $element, attributes) {
            
            $scope.getHtmlTemplate = function(field) {
                return $scope.customFieldTemplates[field.type];
            };
            
            // initialize dynamic table if field's type is dynamic-table
            // or references a dynamic table
            
            if ($scope.field.type === 'dynamic-table') {
                if ($scope.field.columnDefinitions && $scope.field.columnDefinitions.length > 0) {
                    initializeDynamicTable($scope.field.columnDefinitions);
                }
            } else if ($scope.field.type === 'readonly') {
                if ($scope.field.params && $scope.field.params && $scope.field.params.field && $scope.field.params.field.type === 'dynamic-table') {
                    if ($scope.field.columnDefinitions && $scope.field.columnDefinitions.length > 0) {
                        initializeDynamicTable($scope.field.columnDefinitions, $scope.field.value);
                    } else if ($scope.field.params.field.columnDefinitions && $scope.field.params.field.columnDefinitions.length > 0) {
                        initializeDynamicTable($scope.field.params.field.columnDefinitions, $scope.field.value);
                    }
                }
            }
            
            function initializeDynamicTable (columnDefinitions, data) {
                var columnDefs = [];
                
                for (var i = 0; i < columnDefinitions.length; i++) {
                    if (columnDefinitions[i].visible) {
                        columnDefs.push({
                            'field': columnDefinitions[i].id,
                            'displayName': columnDefinitions[i].name,
                            'enableColumnMenu': false
                        });
                    }
                }
                
                $scope.gridOptions = {
                        columnDefs: columnDefs,
                        data: data,
                        enableRowSelection: false,
                        enableSorting: false,
                        enableRowHeaderSelection: false,
                        multiSelect: false,
                        enableHorizontalScrollbar: 0,
                        modifierKeysToMultiSelect: false
                    };
            }
        }
    }
}])

    .directive('numberInputCheck', function() {

        return {
            require: 'ngModel',
            link: function(scope, element, attrs, modelCtrl) {

                var opts = {
                        positiveOnly: attrs.numberPositiveOnly ? true : false,
                        moneyFraction: attrs.numberMoneyFraction ? true : false
                    };

                modelCtrl.$parsers.push(function (inputValue) {

                    var transformedInput;
                    if (inputValue && inputValue.indexOf('-') === 0 && !opts.positiveOnly) {
                        transformedInput = inputValue.substr(1).replace(/([^0-9])/g, '');
                        transformedInput = '-' + transformedInput;
                    } else {
                        transformedInput = inputValue.replace(/([^0-9])/g, '');
                    }

                    var parsed = parseInt(transformedInput);
                    if (!isNaN(parsed) && !opts.moneyFraction) {
                        transformedInput = parsed.toString();
                    }

                    if (transformedInput != inputValue) {
                        modelCtrl.$setViewValue(transformedInput);
                        modelCtrl.$render();
                    }

                    return transformedInput;
                });
            }
        };
    });
    
