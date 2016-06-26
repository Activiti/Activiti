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
angular.module('activitiApp').directive('documentPreview', ['$parse', 'appResourceRoot', function ($parse, appResourceRoot) {
    return {
        restrict: 'E',
        templateUrl: appResourceRoot + 'views/templates/document-preview.html',
        scope: {
            content: '=',
            task: '=',
            deleted: '&onDelete',
            editable: '=',
            readOnly: '='
        },
        controller: 'DocumentPreviewController'
    };
}]);

angular.module('activitiApp').directive('activitiForm', ['$rootScope', 'appResourceRoot', function ($rootScope, appResourceRoot) {
    return {
        restrict: 'E',
        templateUrl: appResourceRoot + 'views/templates/form-template.html',
        scope: {
            taskId : '=taskId',
            formDefinition: '=formDefinition',
            processName: '=processName',
            processDefinitionId: '=processDefinitionId',
            outcomesOnly: '=outcomesOnly',
            disableForm: '=disableForm',
            disableFormText: '=disableFormText',
            hideButtons: '=hideButtons'
        },
        link: function ($scope, element, attributes) {
            $rootScope.window.forceRefresh = true;
        }
    };
}]);

angular.module('activitiApp').
    directive('optimalGrid', ['$compile', '$http', function($compile, $http) {
        var directive = {};
        directive.restrict = 'A';
        directive.replace = true;
        directive.scope = {
            toWatch : "=optimalGrid"
        };

        directive.controller = ['$scope', '$element', '$timeout', function($scope, $element, $timeout) {
            // Deep watch of object
            $scope.$watch('toWatch', function(newValue) {
                var previous;
                var seperatorNeeded = false;
                var i=0;
                $element.children().each(function() {
                    var current = angular.element(this);
                    if(current.hasClass('col-xs-6')) {
                        if(!current.hasClass('ng-hide')) {
                            i++;
                        }
                    } else if(current.hasClass('col-xs-12')) {
                        if(current.hasClass('seperator')) {
                            // Seperator
                            if(i == 2) {
                                i = 0;
                                current.removeClass('hidden');
                            } else {
                                current.addClass('hidden');
                            }
                        } else {
                            // Double column
                            if(previous && i > 0) {
                                previous.removeClass('hidden');
                            }
                            i = 0;
                        }
                    }
                    previous = current;
                });


                if(i > 0 && previous) {
                    previous.removeClass('hidden');
                }
            }, true);
        }];
        return directive;
    }]);

angular.module('activitiApp').
    directive('jumpers', ['$compile', '$http', function($compile, $http) {
        var directive = {};
        directive.restrict = 'A';
        directive.replace = true;
        directive.transclude = true;
        directive.template = '<div><ul class="jumpers">' +
            '<li ng-repeat="jumper in jumpers" ng-show="jumper.show || !jumper.static" ng-click="jumperClicked(jumper)" ng-class="{\'pending\': !jumper.show}">{{jumper.title | translate}} <span ng-show="jumper.badge != undefined">{{jumper.badge}}</span></li>' +
            '</ul></div>';

        directive.scope = {
            jumpers : "=jumpers",
            activeJumperReference: "=activeJumper"
        };

        directive.controller = ['$scope', '$element', '$timeout', function($scope, $element, $timeout) {

            $scope.state = {};
            $scope.$watch('activeJumperReference', function(newValue, oldValue) {
                if(!$scope.activeJumper || $scope.activeJumper.id != newValue) {
                    var newJumper = $scope.findJumper(newValue);
                    if(newJumper) {
                        $scope.jumperClicked(newJumper);
                    }
                }
            });

            $scope.findJumper = function(tabId) {
                if($scope.jumpers) {
                    for(var i=0; i< $scope.jumpers.length; i++) {
                        if($scope.jumpers[i].id == tabId) {
                            return $scope.jumpers[i];
                        }
                    }
                }
                return undefined;
            };

            $scope.jumperClicked = function(jumper) {
                if(!jumper.show) {
                    jumper.show = true;
                }

                if(jumper) {
                    $scope.activeJumperReference = jumper.id;
                    $scope.activeJumper = jumper;
                    if(jumper.anchor) {
                        $timeout(function() {
                            var selected = angular.element("#" + jumper.anchor);
                            if(selected.length) {
                                var parentElement = selected.parent();

                                var selectedTop = selected.position().top - parentElement.position().top + parentElement.scrollTop();
                                var selectedBottom = selectedTop + selected.outerHeight();
                                var elementBottom = parentElement.scrollTop() + parentElement.innerHeight();
                                var elementTop = elementBottom - parentElement.innerHeight();

                                if(selectedTop <= elementTop) {
                                    // scroll up
                                    parentElement.scrollTop(selectedTop);
                                } else if(selectedBottom > elementBottom) {
                                    // scroll down
                                    parentElement.scrollTop(selectedTop);
                                }
                            }

                        }, 100)
                    }

                } else {
                    $scope.activeJumperReference = undefined;
                    $scope.activeJumper = undefined;
                }
            };
        }];
        return directive;
    }]);


/**
 * Directive for using the bootstrap-daterangepicker widget.
 *
 * Taken/inspired from https://github.com/luisfarzati/ng-bs-daterangepicker
 */
angular.module('activitiApp')
    .directive('input', ['$compile', '$http', function($compile, $http) {
        return {
            restrict: 'E',
            require: '?ngModel',
            link: function ($scope, $element, $attributes, ngModel) {
                if ($attributes.type !== 'daterange' || ngModel === null ) return;

                var options = {};
                options.format = $attributes.format || 'YYYY-MM-DD';
                options.separator = $attributes.separator || ' - ';
                options.minDate = $attributes.minDate && moment($attributes.minDate);
                options.maxDate = $attributes.maxDate && moment($attributes.maxDate);
                options.dateLimit = $attributes.limit && moment.duration.apply(this, $attributes.limit.split(' ').map(function (elem, index) { return index === 0 && parseInt(elem, 10) || elem; }) );
                options.ranges = $attributes.ranges && $parse($attributes.ranges)($scope);
                options.locale = $attributes.locale && $parse($attributes.locale)($scope);
                options.opens = $attributes.opens && $parse($attributes.opens)($scope);

                function format(date) {
                    return date.format(options.format);
                }

                function formatted(dates) {
                    return [format(dates.startDate), format(dates.endDate)].join(options.separator);
                }

                ngModel.$formatters.unshift(function (modelValue) {
                    if (!modelValue) return '';
                    return modelValue;
                });

                ngModel.$parsers.unshift(function (viewValue) {
                    return viewValue;
                });

                ngModel.$render = function () {
                    if (!ngModel.$viewValue || !ngModel.$viewValue.startDate) return;
                    $element.val(formatted(ngModel.$viewValue));
                };

                $scope.$watch($attributes.ngModel, function (modelValue) {
                    if (!modelValue || (!modelValue.startDate)) {
                        ngModel.$setViewValue({ startDate: moment().startOf('day'), endDate: moment().startOf('day') });
                        return;
                    }
                    $element.data('daterangepicker').startDate = modelValue.startDate;
                    $element.data('daterangepicker').endDate = modelValue.endDate;
                    $element.data('daterangepicker').updateView();
                    $element.data('daterangepicker').updateCalendars();
                    $element.data('daterangepicker').updateInputText();
                });

                $element.daterangepicker(options, function(start, end, rangeName) {
                    $scope.$apply(function () {
                        ngModel.$setViewValue({ startDate: start, endDate: end ,rangeName: rangeName});
                        ngModel.$render();
                    });
                });
            }
        };
    }]);

angular.module('activitiApp').
    directive('userPicture', ['$compile', '$http', function($compile, $http) {
        var directive = {};
        directive.restrict = 'A';
        directive.replace = true;
        directive.transclude = false;
        directive.template =
            '<div class="{{userPic.class}}" ng-style="{\'background-image\': userPic.style}" title="{{userPic.userName}}">' +
                '<span>{{userPic.text}}</span>'+
            '</div>';

        directive.scope = {
            user : "=userPicture"
        };

        directive.controller = ['$scope', '$element', '$timeout', function($scope, $element, $timeout) {
            $scope.$watch('user', function(newValue, oldValue) {
                $scope.refreshUser(newValue);
            });
            $element.bind('error', function() {
                console.log('No picture');
            });


            $scope.refreshUser = function(user) {
                if(user) {
                    var newUserPic = {text: ''};
                    if(user.pictureId) {
                        newUserPic.class = "user-picture";
                        newUserPic.style='url("' + ACTIVITI.CONFIG.contextRoot + '/app/rest/users/' + user.id  +'/picture")';
                    } else {
                        newUserPic.class="user-picture no-picture";
                    }

                    if(user.firstName && user.lastName) {
                        newUserPic.text = user.firstName.substring(0,1).toUpperCase() + user.lastName.substring(0,1).toUpperCase();
                        newUserPic.userName = user.firstName + ' ' + user.lastName;
                    } else if(user.lastName != undefined && user.lastName != null) {
                        if (user.lastName.length > 1) {
                            newUserPic.text = user.lastName.substring(0, 2).toUpperCase();
                        } else if (user.lastName.length == 1) {
                            newUserPic.text = user.lastName.substring(0, 1).toUpperCase();
                        }
                        newUserPic.userName = user.lastName;
                    } else if(user.firstName != undefined && user.firstName != null) {
                        if (user.firstName.length > 1) {
                            newUserPic.text = user.firstName.substring(0, 2).toUpperCase();
                        } else if (user.firstName.length == 1) {
                            newUserPic.text = user.firstName.substring(0, 1).toUpperCase();
                        }
                        newUserPic.userName = user.firstName;
                    } else {
                        newUserPic.text = '??';
                        newUserPic.userName = '';
                    }
                }

                $scope.userPic = newUserPic;
            };



            $scope.refreshUser($scope.user);
        }];
        return directive;
    }]);

//form builder element renderer
angular.module('activitiApp')
    .directive('formElement', ['$rootScope', '$timeout', '$modal', '$http', '$templateCache', '$translate', 'appResourceRoot', 'RecursionHelper', function ($rootScope, $timeout, $modal, $http, $templateCache, $translate, appResourceRoot, RecursionHelper) {
        return {
            restrict: 'E',
            templateUrl: appResourceRoot + 'views/templates/form-element-template.html',
            transclude: false,
            scope: {
                field: '=',
                taskForm: '=',
                controlCallbacks: '=',
                model: '='
            },
            compile: function (element) {
                return RecursionHelper.compile(element, this.link);
            },
            link: function ($scope, $element, attributes) {

                $scope.appResourceRoot = appResourceRoot;
                $scope.activitiFieldIdPrefix = $rootScope.activitiFieldIdPrefix;

                $scope.onFieldValueChange = function(field){
                    return $scope.controlCallbacks.onFieldValueChange(field);
                };

                $scope.isEmpty = function(field){
                    return $scope.controlCallbacks.isEmpty(field);
                };

                $scope.isEmptyDropdown = function(field) {
                    return $scope.controlCallbacks.isEmptyDropdown(field);
                };

                $scope.fieldPersonSelected = function(user, field){
                    return $scope.controlCallbacks.fieldPersonSelected(user, field);
                };

                $scope.fieldPersonEmailSelected = function(email, field){
                    return $scope.controlCallbacks.fieldPersonEmailSelected(email, field);
                };
                
                $scope.fieldPersonRemoved = function(user, field){
                    return $scope.controlCallbacks.fieldPersonRemoved(user, field);
                };

                $scope.fieldGroupSelected = function(group, field){
                    return $scope.controlCallbacks.fieldGroupSelected(group, field);
                };
                
                $scope.fieldGroupRemoved = function(group, field){
                    return $scope.controlCallbacks.fieldGroupRemoved(group, field);
                };

                $scope.removeContent = function(content, field){
                    return $scope.controlCallbacks.removeContent(content, field);
                };

                $scope.contentUploaded = function(content, field){
                    return $scope.controlCallbacks.contentUploaded(content, field);
                };

                $scope.uploadInProgress = function(status){
                    return $scope.controlCallbacks.uploadInProgress(status);
                };

                $scope.handleReadonlyClick = function($event, field){
                    return $scope.controlCallbacks.handleReadonlyClick($event, field);
                };

                $scope.clearDate = function(field, callback){
                    return $scope.controlCallbacks.clearDate(field, callback);
                };

                $scope.selectToday = function(field, callback){
                    return $scope.controlCallbacks.selectToday(field, callback);
                };

                $scope.closeDatePopup = function(field){
                    return $scope.controlCallbacks.closeDatePopup(field);
                };

            }
        }
    }]);
