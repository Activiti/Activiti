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
activitiModule
  .directive('restrictInput', ["$parse", function ($parse) {
    return {
      restrict: 'A',
      require: 'ngModel',
      priority: 1002,

      link: function postLink(scope, elm, attrs, ctrl) {

        var acceptedFormat = attrs["restrictInput"];
        if (acceptedFormat == undefined || acceptedFormat == null || acceptedFormat == "") {
           acceptedFormat = attrs["dateFormat"];
        }

        scope.field.acceptedFormat=acceptedFormat;

        function calculateAcceptedFormats(format) {
          var format1 = format.toUpperCase(); //d-m-yyyy
          var format2 = format1.replace(/-D-/,"-DD-").replace(/^D-/,"DD-").replace(/-D$/,"-DD"); //dd-m-yyyy
          var format3 = format1.replace(/-M-/,"-MM-").replace(/^M-/,"MM-").replace(/-M$/,"-MM");  //d-mm-yyyy
          var format4 = format2.replace(/-M-/,"-MM-").replace(/^M-/,"MM-").replace(/-M$/,"-MM");  //dd-mm-yyyy
          return [format1,format2,format3,format4];
        }

        var acceptedFormats = calculateAcceptedFormats(acceptedFormat);
        var skipValidation = false;

        if (acceptedFormat == undefined || acceptedFormat == null || acceptedFormat == "") {
          skipValidation = true;
        }
        var oldRenderer = ctrl.$render;

        ctrl.$render = function () {
          elm.val(ctrl.$viewValue);
          if (ctrl.$dateValue && !isNaN(ctrl.$dateValue.getTime())) {
            if (oldRenderer) {
              oldRenderer();
            }
          }
        };

        function isValidText(viewValue, format) {
          if (viewValue === undefined || viewValue == null || viewValue ==='') return true;
          if (viewValue.length > format.length) return false;

           for (var i = 0; i < Math.min(format.length, viewValue.length); i++) {
             var charType = format[i];
             if (charType.toUpperCase().match(/D|M|Y/)) {
               if (viewValue[i].match(/\d/) == null) return false;
             }else {
               if (viewValue[i] != charType) return false;
             }
           }
           return true;
         }

        ctrl.$parsers.unshift(function (viewValue) {

          if (skipValidation) return viewValue; //just skip this parser

          var isValid = false;
          for(var i =0 ; i < acceptedFormats.length && !isValid; i++){
            isValid |= isValidText(viewValue,acceptedFormats[i]);
          }

          if (!isValid) {
            //should restore to the latest known well formated date.
            ctrl.$dateValue = null;
          } else {
            ctrl.$lastValidText = viewValue;

              //by default the date picker in angular strap does not reset the dateValue if the viewValue is null or empty.
              if (viewValue === undefined || viewValue == null || viewValue === '') {
                  ctrl.$dateValue = null;
              }

            return viewValue;
          }

          ctrl.$setViewValue(ctrl.$lastValidText);
          ctrl.$render();
          return ctrl.$lastValidText;
        });

      }
    }
  }
  ]);


activitiModule
    .directive('autoHeight', ['$rootScope', '$timeout', function($rootScope, $timeout) {
        return {
            restrict: 'AC',
            scope: {
              'toWatch': '=autoHeight'
            },
            compile: function (element, attr) {
                return function ($scope, $element, $attrs) {
                    var offset = 0;
                    if($attrs['offset']) {
                        offset = parseInt($attrs['offset']);
                        if(offset == NaN || offset == undefined) {
                            offset = 0;
                        }
                    }

                    var update = function($element) {
                        // Get hold of parent and iterate all the children to get available height

                        $timeout(function() {
                            var total = $element.parent().outerHeight() - offset;
                            var found = false;
                            $element.parent().children().each(function() {
                                if(!found) {
                                    if($element[0] == this) {
                                        found = true;
                                    } else {
                                        // Substract preceding child's height
                                        total -= angular.element(this).outerHeight();
                                    }
                                }
                            });

                            if(found) {
                                $element.height(total);
                            }
                        }, 0);
                    };

                    if($scope.unregisterWatcher) {
                        $scope.unregisterWatcher();
                    }
                    $scope.unregisterWatcher = $rootScope.$watch('window.height', function(windowHeight) {
                        update($element);
                    });

                    if($scope.unregisterForceWatcher) {
                        $scope.unregisterForceWatcher();
                    }
                    $scope.unregisterForceWatcher = $rootScope.$watch('window.forceRefresh', function(forceValue) {
                        update($element);
                    });


                    $scope.$on('$destroy', function() {
                        // Cleanup watcher for window-height
                        if($scope.unregisterWatcher) {
                            $scope.unregisterWatcher();
                        }
                        if($scope.unregisterForceWatcher) {
                            $scope.unregisterForceWatcher();
                        }
                    });
                }
            }
        };
    }]);

/**
 * Directive that ensures the child-element with class .active is visible and scrolls if needed. Watches the value
 * of the directive and will re-apply if this value is changes.
 */
activitiModule
    .directive('scrollToActive', ['$timeout', function($timeout) {
        return {
            restrict: 'AC',
            scope: {
                toWatch: "=scrollToActiveModel"
            },
            compile: function (element, attr) {
                return function ($scope, $element, $attrs) {
                    $scope.$watch('toWatch', function() {
                        $timeout(function() {
                            var useParent = $attrs['useParent'];
                            var offsetTop = $attrs['offsetTop'];
                            if(offsetTop) {
                                offsetTop = parseInt(offsetTop);
                                if(offsetTop == NaN) {
                                    offsetTop = 0;
                                }
                            }
                            if (!offsetTop) {
                                offsetTop = 0;
                            }

                            var selectedArr = $element.children('.active');
                            if(selectedArr && selectedArr.length > 0) {
                                var selected = angular.element(selectedArr[0]);

                                if(useParent) {
                                    $element = angular.element($element.parent());
                                }
                                var selectedTop = selected.position().top - $element.position().top + $element.scrollTop();
                                var selectedBottom = selectedTop + selected.outerHeight();
                                var elementBottom = $element.scrollTop() + $element.innerHeight();
                                var elementTop = elementBottom - $element.innerHeight();

                                if(selectedTop <= elementTop) {
                                    // scroll up
                                    $element.scrollTop(selectedTop - selected.outerHeight() - offsetTop);
                                } else if(selectedBottom > elementBottom) {
                                    // scroll down
                                    $element.scrollTop(elementTop + selected.outerHeight() - offsetTop);
                                }
                            }
                        }, 0);
                    });
                }
            }
        };
    }]);

/**
 * Directive that ensures the popup is scrolled into view, using the first parent as scroll-pane that has
 * a class 'scroll-container' set on it. Is applied when the popup is shown.
 */
activitiModule
    .directive('autoScroll', ['$timeout', function($timeout) {
        return {
            restrict: 'AC',
            compile: function (element, attr) {
                return function ($scope, $element, $attrs) {
                    $scope.$on('tooltip.show', function() {
                        $timeout(function() {
                            // Find appropriate parent
                            var parent = $element[0];
                            while(parent) {
                                if(parent.className && parent.className.indexOf('scroll-container') >= 0) {
                                    break;
                                }
                                parent = parent.parentNode;
                            }

                            if(parent) {
                                parent = angular.element(parent);
                                var selectedTop = $element.offset().top  - parent.offset().top + $element.scrollTop();
                                var selectedBottom = selectedTop + $element.outerHeight();

                                if(selectedBottom + 30 >= parent.outerHeight()) {
                                    parent.scrollTop(selectedTop);
                                }
                            }
                        }, 50);
                    });
                }
            }
        };
    }]);

activitiModule
    .directive('userName', function() {
        var directive = {};
        directive.template = '{{user.firstName && user.firstName || ""}} {{user.lastName && user.lastName || ""}} {{ (user.email && !user.firstName && !user.lastName) && user.email || ""}}';
        directive.scope = {
            user: "=userName"
        };
        return directive;
    });


/**
 * Executes the method that is set on the directive attribute value when ANY OTHER element is clicked, which is not the element the
 * directive is on, or any of it's children.
 *
 */
activitiModule
    .directive('clickAnywhere', ["$document", "$parse", function ($document, $parse) {

        var linkFunction = function ($scope, $element, $attributes) {

            var scopeExpression = $attributes.clickAnywhere;
            var invoker = $parse(scopeExpression);

            var ignoreId = $attributes.ignore;
            var ignoreClass = $attributes.ignoreClass;
            var ignorePopupEvents = $attributes.ignorePopupEvents == 'true';

            var handler = function (event) {
                // Check source of event
                var parent = event.target;
                while(parent) {
                    if(parent == $element[0] ||
                        (ignoreId && parent.id == ignoreId) ||
                        (ignoreClass && parent.className && parent.className.indexOf(ignoreClass) >= 0)) {

                        event.stopPropagation();
                        event.preventDefault();
                        return;
                    }
                    parent = parent.parentNode;
                }

                $scope.$apply(
                    function () {
                        invoker($scope, {$event: event});
                    }
                );
            };

            $document.on("click", handler);

            $scope.$on('$destroy', function () {
                $document.off("click", handler);
            });

            // Special handling for tooltips which don't destroy the scope
            var hideReg = $scope.$on('tooltip.hide', function () {
                if(!ignorePopupEvents) {
                    $document.off("click", handler);
                    hideReg();
                }
            });

        };

        // Return the linking function.
        return( linkFunction );
    }
    ]);




activitiModule
    .directive('autoFocus', ['$timeout', '$parse', function($timeout, $parse) {
        return {
            restrict: 'AC',
            compile: function($element, attr) {
                var selectText;

                if(attr["selectText"]) {
                    selectText = $parse(attr["selectText"]);
                }

                return function(_scope, _element, _attrs) {
                    var firstChild = (_attrs.focusFirstChild !== undefined);
                    $timeout(function () {
                        if (firstChild) {
                            // look for first input-element in child-tree and focus that
                            var inputs = _element.find('input');
                            if (inputs && inputs.length > 0) {
                                inputs[0].focus();

                                if(selectText && selectText(_scope.$parent)) {
                                    input[0].setSelectionRange(0,input[0].value.length);
                                }
                            }
                        } else {
                            // Focus element where the directive is put on
                            _element[0].focus();
                            if(selectText && selectText(_scope.$parent)) {
                                _element[0].setSelectionRange(0,_element[0].value.length);
                            }
                        }
                    }, 100);
                }
            }
        };
    }]);

activitiModule
    .directive('focusWhen', ['$timeout', function ($timeout) {
        return {
            link: function (scope, element, attrs) {
                scope.$watch(attrs.ngFocus, function (val) {
                    if (angular.isDefined(val) && val) {
                        $timeout(function () {
                            element[0].focus();
                        });
                    }
                }, true);

                element.bind('blur', function () {
                    if (angular.isDefined(attrs.ngFocusLost)) {
                        scope.safeApply(attrs.ngFocusLost);
                    }
                });
            }
        };
    }]);


activitiModule
    .directive('loading', [function() {
        var directive = {};
        directive.restrict = 'A';
        directive.template = '<div class="loading" ng-show="loading"><div class="l1"></div><div class="l2"></div><div class="l3"></div></div>';
        directive.scope = {
            loading : "=loading",
            loadingText: "=loadingText"
        };
        return directive;
    }]);

// Workaround for https://github.com/twbs/bootstrap/issues/8379 :
// prototype.js interferes with regular dropdown behavior
activitiModule
    .directive('activitiFixDropdownBug', function() {
        return {
            restrict: 'AEC',
            link: function(scope, element, attrs) {
                element.on('hidden.bs.dropdown	', function () {
                    element.show(); // evil prototype.js has added display:none to it ...
                })
            }
        };
    });

/**
 * Directive for rendering user link.
 */
activitiModule
  .directive('userLink', function() {
    var directive = {};
    directive.template = '{{user.firstName && user.firstName || ""}} {{user.lastName && user.lastName || ""}} {{ (user.email && !user.firstName && !user.lastName) && user.email || ""}}';
    directive.scope = {
        user: "=userLink"
    };

    directive.compile = function(element, attributes) {
        element.addClass('people-link');
    };

    return directive;
});

/**
 * Directive for rendering a form field.
 */
activitiModule
    .directive('formField', function () {
        var directive = {};

        directive.template = ' {{field.name || ""}} - {{field.id}}';
        directive.scope = {
            field: "=formField"
        };

        directive.compile = function (element, attributes) {
            element.addClass('form-field');
        };
        return directive;
    });
/**
 * Directive to capture mouse up, down, enter and escape on input fields (eg. list navigation)
 */
activitiModule
    .directive('customKeys', ["$parse", function ($parse) {
        var directive = {};
        directive.compile = function($element, attr) {
            var up, down, enter, escape;

            if(attr["upPressed"]) {
                up = $parse(attr["upPressed"]);
            }
            if(attr["downPressed"]) {
                down = $parse(attr["downPressed"]);
            }
            if(attr["enterPressed"]) {
                enter = $parse(attr["enterPressed"]);
            }

            if(attr["escapePressed"]) {
                escape = $parse(attr["escapePressed"]);
            }

            return function(scope, element, attr) {
                element.on('keyup', function(e) {
                    if(e.keyCode === 38) {
                        scope.$apply(function() {
                            if(up) {
                                up(scope, {$event:e});
                            }
                        });
                    } else if(e.keyCode === 40) {
                        scope.$apply(function() {
                            if(down) {
                                down(scope, {$event:e});
                            }
                        });
                    } else if(e.keyCode === 13) {
                        scope.$apply(function() {
                            if(enter) {
                                enter(scope, {$event:e});
                            }
                        });
                    } else if(e.keyCode === 27) {
                    scope.$apply(function() {
                        if(escape) {
                            escape(scope, {$event:e});
                        }
                    });
                }
                });

                element.on('keydown', element, function (e) {
                    if (e.keyCode === 38 || e.keyCode === 40 || e.keyCode === 13 || e.keyCode === 27)
                        e.preventDefault();
                });
            };
    };
    return directive;
}]);

// Delayed setting of model value in scope, based on input value unchanged after a number of millis
// See below: ngDebounce is preferred (as it hooks into ngModel, meaning that ng-change will keep working - but not with delayedModel)
activitiModule
    .directive('delayedModel', ['$timeout', function($timeout) {
    return {
        scope: {
            targetModel: '=delayedModel'
        },
        link: function(scope, element, attrs) {

            element.val(scope.targetModel);

            // Also watch model for any changes not triggered by timer
            scope.$watch('targetModel', function(newVal, oldVal) {
                if(scheduled) {
                    $timeout.cancel(scheduled);
                }
                if (newVal !== oldVal) {
                    element.val(scope.targetModel);
                }
            });

            var scheduled;
            element.on('keyup paste search', function() {
                if(element.val() !== scope.targetModel) {
                    if(scheduled) {
                        $timeout.cancel(scheduled);
                    }
                    scheduled = $timeout(function() {
                        scope.targetModel = element[0].value;
                        element.val(scope.targetModel);
                        scope.$apply();
                    }, attrs.delay || 200);
                }
            });
        }
    };
}]);


// From https://gist.github.com/benbrandt22/bb44184a2eddcd4b0b8a
activitiModule.directive('ngDebounce', ['$timeout', function($timeout) {
    return {
        restrict: 'A',
        require: 'ngModel',
        priority: 99,
        link: function(scope, elm, attr, ngModelCtrl) {
            if (attr.type === 'radio' || attr.type === 'checkbox') return;

            elm.unbind('input');

            var debounce;
            elm.bind('input', function() {
                $timeout.cancel(debounce);
                debounce = $timeout( function() {
                    scope.$apply(function() {
                        ngModelCtrl.$setViewValue(elm.val());
                    });
                }, attr.ngDebounce || 1000);
            });
            elm.bind('blur', function() {
                scope.$apply(function() {
                    ngModelCtrl.$setViewValue(elm.val());
                });
            });
        }

    }
}]);

activitiModule.directive('externalContent', ['$parse', '$timeout', 'appResourceRoot', function ($parse, $timeout, appResourceRoot) {
    var directive = {};
    directive.restrict = 'A';
    directive.templateUrl = appResourceRoot + '../views/common/templates/external-content-template.html';

    directive.scope = {
        taskId : '=taskId',
        processInstanceId: '=formDefinition',
        folderSelect: '=folderSelect',
        linkOnly: '=linkOnly',
        preSelectedAlfrescoAccount: '=account',
        uploadInProgress: '=uploadInProgress'
    };
    directive.link = function ($scope, $element, $attributes) {

        if($attributes["onContentUpload"]) {
            $scope.uploadedCallback = $parse($attributes['onContentUpload']);
        }

        if($attributes["onFolderSelect"]) {
            $scope.folderSelectCallback = $parse($attributes['onFolderSelect']);
        }

        if($attributes["onUploadInProgress"]) {
            $scope.uploadInProgressCallback = $parse($attributes['onUploadInProgress']);
        }

        // Schedule hooking in of click-listener. we cannot use angular ng-click since this
        // will force a new $apply/$digest to happen when the click() is called on the button
        $timeout(function() {
            $element.find('div.select-file').click(function() {
                $element.find('input[type="file"]').click();
            });
        }, 200);
    };
    directive.controller = ['$scope', '$element', 'RelatedContentService', '$modal', '$window', '$translate', '$rootScope',  function($scope, $element, RelatedContentService, $modal, $window, $translate, $rootScope) {
        $scope.uploadModel = {uploading: false};

        $scope.clearPopupError = function() {
        };

        $scope.openFileSelect = function(element) {
            $scope.errorMessage = undefined;
            var parent = angular.element(element).parent();
            parent.children('input').click();
        };

        $scope.onFileSelect = function ($files, isIE) {
            $scope.errorMessage = undefined;
            if(!$scope.linkOnly) {
                $scope.errorMessage = undefined;
                if(!$scope.folderSelect && !$scope.uploadModel.uploading && $files.length > 0) {
                    if ($scope.uploadInProgressCallback) {
                        $scope.uploadInProgressCallback($scope.$parent, {status: true});
                    }

                    $scope.uploadModel.uploading = true;
                    var file = $files[0];

                    $scope.clearPopupError();
                    RelatedContentService.addRelatedContent($scope.taskId, $scope.processInstanceId, file, isIE).progress(function (evt) {
                        $scope.uploadModel.uploadProgress = parseInt(100.0 * evt.loaded / evt.total);
                    }).then(function (data) {
                        if ($scope.uploadInProgressCallback) {
                            $scope.uploadInProgressCallback($scope.$parent, {status: false});

                        }

                        $scope.uploadModel.uploading = false;
                        if($scope.uploadedCallback) {
                            $scope.uploadedCallback($scope.$parent, {'content': data});
                        }
                    }, function (error) {
                        // Error callback
                        if ($scope.uploadInProgressCallback) {
                            $scope.uploadInProgressCallback($scope.$parent, {status: false});
                        }

                        if(error && error.messageKey) {
                            var formattedData = {};
                            if(error.customData) {
                                formattedData.quota =  $scope.formatFileSize(error.customData.quota)
                            };

                            $translate(error.messageKey, formattedData).then(function(message) {
                                $scope.errorMessage = message;
                            });
                        }
                        $scope.uploadModel.uploading = false;
                    });
                }
            }
        };

    }];

    return directive;
}]);


activitiModule.
    directive('selectPeoplePopover', ['$rootScope', '$http', '$popover', 'appResourceRoot', 'UserService', '$parse', function($rootScope, $http, $popover, appResourceRoot, UserService, $parse) {
        var directive = {};
        directive.restrict = 'A';

        directive.scope = {
            excludeTaskId: '=excludeTaskId',
            excludeProcessId: '=excludeProcessId',
            excludeUserId: '=excludeUserId',
            excludeUserIds: '=excludeUserIds',
            tenantId: '=tenantId',
            type: '=type',
            restrictWithGroup: '=restrictWithGroup',
            selectPeopleFormFields: '=selectPeopleFormFields',
            ignoreContainer: '=ignoreContainer'
        };

        directive.link = function($scope, $element, attrs) {
            // Set defaults
            var placement = "bottom";

            $element.addClass("toggle-people-select");

            if(attrs.placement) {
                placement = attrs.placement;
            }

            var closeOnSelect = true;
            if(attrs.closeOnSelect !== undefined) {
                closeOnSelect = attrs.closeOnSelect;
            }

            if ($scope.ignoreContainer) {
                $scope.popover = $popover($element, {template: appResourceRoot + '../views/common/popover/select-people-popover.html?'  +
                    Date.now(), placement: placement});

            } else {
                $scope.popover = $popover($element, {template: appResourceRoot + '../views/common/popover/select-people-popover.html?'  +
                    Date.now(), placement: placement, container: 'body'});
            }

            // Parse callbacks
            var selectedCallback, cancelledCallback, emailSelectedCallback;
            if (attrs['onPeopleSelected']) {
                selectedCallback = $parse(attrs['onPeopleSelected']);
            }
            if (attrs['onCancel']) {
                cancelledCallback = $parse(attrs['onCancel']);
            }
            if (attrs['onEmailSelected']) {
                emailSelectedCallback = $parse(attrs['onEmailSelected']);
            }

            // Parse type
            // Can be 'workflow' or 'idm'. In 'workflow', the users are retrieved for filling task assignments etc. This is the default if this param is omitted. 'idm' is more strict.
            var backendType = 'workflow';
            if ($scope.type !== null && $scope.type !== undefined) {
                backendType = $scope.type;
            }

            var popoverScope = $scope.popover.$scope;
            popoverScope.title = attrs['popoverTitle'];

            popoverScope.popupModel = {
                emailMode: false,
                showRecentResults: false, // Disabled recent for the moment. Put this on true to set it back
                userResults: [],
                userField: {},
                userFieldFilter: ['people']
            };
            
            if ($scope.selectPeopleFormFields) {
                popoverScope.popupModel.formFields = $scope.selectPeopleFormFields;
            }

            if (attrs['emailModeDisabled']) {
                var emailModeDisabledValue = attrs['emailModeDisabled'];
                if (emailModeDisabledValue === 'true') {
                    popoverScope.popupModel.emailDisabled = true;
                }
            }

            popoverScope.popupModel.emailMode = false;


            popoverScope.setSearchType = function() {
                popoverScope.popupModel.userSourceType = 'search';
            };
            
            popoverScope.setFormFieldType = function() {
                popoverScope.popupModel.userSourceType = 'field';
            };
            
            popoverScope.$watch('popupModel.userField', function() {
                if (popoverScope.popupModel.userField && popoverScope.popupModel.userField.id) {
                    if (selectedCallback) {
                        // Run callback in parent scope of directive
                        var simpleUserField = {
                                id: popoverScope.popupModel.userField.id, 
                                name: popoverScope.popupModel.userField.name,
                                type: popoverScope.popupModel.userField.type
                        }
       
                        selectedCallback($scope.$parent, {'userField': simpleUserField});
                        popoverScope.popupModel.userField = {};
                    }
                    
                    if (closeOnSelect || closeOnSelect === 'true') {
                        popoverScope.$hide();
                    }
                }           
            });

            popoverScope.$watch('popupModel.filter', function() {
                if (popoverScope.popupModel.filter && popoverScope.popupModel.filter.length > 0) {

                    var userGetPromise;
                    if (backendType === 'idm') {
                        userGetPromise = UserService.getFilteredUsersStrict(popoverScope.popupModel.filter, $scope.tenantId, $scope.restrictWithGroup);
                    } else {
                        // Default: go to workflow users backend
                        userGetPromise = UserService.getFilteredUsers(popoverScope.popupModel.filter, $scope.excludeTaskId,
                                $scope.excludeProcessId, $scope.tenantId, $scope.restrictWithGroup);
                    }

                    userGetPromise.then(function(result) {
                        popoverScope.popupModel.showRecentResults =  false;

                        var users = [];
                        var excludeUserIdSet = $scope.excludeUserId !== null && $scope.excludeUserId !== undefined;
                        var excludeUserIdsSet = $scope.excludeUserIds !== null && $scope.excludeUserIds !== undefined;
                        if (excludeUserIdSet === true || excludeUserIdsSet === true) {
                            for (var userIndex=0; userIndex < result.data.length; userIndex++) {

                                var userExcluded = false;
                                if (excludeUserIdSet === true && result.data[userIndex].id === $scope.excludeUserId) {
                                    userExcluded = true;
                                }
                                if (excludeUserIdsSet === true && $scope.excludeUserIds.indexOf(result.data[userIndex].id) >= 0) {
                                    userExcluded = true;
                                }

                                if (!userExcluded) {
                                    users.push(result.data[userIndex]);
                                }

                            }
                        } else {
                            users = result.data;
                        }
                        popoverScope.popupModel.userResults = users;
                        popoverScope.resetSelection();
                    });
                } else {
                    popoverScope.resetSelection();
                    popoverScope.popupModel.userResults = [];
                }
            });

            popoverScope.resetSelection = function() {
                popoverScope.popupModel.selectedUser = undefined;
                popoverScope.popupModel.selectedIndex = -1;
            };

            popoverScope.nextUser = function() {
                var users = popoverScope.popupModel.userResults;
                if(users && users.length > 0 && popoverScope.popupModel.selectedIndex < users.length -1) {
                    popoverScope.popupModel.selectedIndex+=1;
                    popoverScope.popupModel.selectedUser = users[popoverScope.popupModel.selectedIndex];
                }
            };

            popoverScope.previousUser = function() {
                var users = popoverScope.popupModel.userResults;
                if(users && users.length > 0 && popoverScope.popupModel.selectedIndex > 0) {
                    popoverScope.popupModel.selectedIndex-=1;
                    popoverScope.popupModel.selectedUser = users[popoverScope.popupModel.selectedIndex];
                }
            };

            popoverScope.confirmUser = function(user) {
                if (!user) {
                    // Selection is done with keyboard, use selection index
                    var users = popoverScope.popupModel.userResults;
                    if (popoverScope.popupModel.selectedIndex >= 0 && popoverScope.popupModel.selectedIndex <users.length) {
                        user = users[popoverScope.popupModel.selectedIndex];
                    }
                }

                if (user) {
                    if (selectedCallback) {
                        // Run callback in parent scope of directive
                        selectedCallback($scope.$parent, {'user': user});
                    }

                    if (closeOnSelect === 'true') {
                        popoverScope.$hide();
                    } else {
                        var users = popoverScope.popupModel.userResults;
                        users.splice(jQuery.inArray(user, users),1);
                        popoverScope.popupModel.selectedIndex=0;
                        popoverScope.popupModel.selectedUser = users[popoverScope.popupModel.selectedIndex];
                    }
                }
            };

            popoverScope.selectPersonByEmail = function(validEmail) { // Not so nice we have to pass the valid email agrument, but couldnt make it work properly
                if (validEmail) {
                    if (emailSelectedCallback) {
                        emailSelectedCallback($scope.$parent, {email: popoverScope.popupModel.email});
                        popoverScope.$hide();
                    }
                }
            };

            popoverScope.$on('tooltip.hide', function() {
                // Invalidate recent results
                if(popoverScope.popupModel.showRecentResults && popoverScope.popupModel.added) {
                    popoverScope.popupModel.recentUsers = [];
                }
                popoverScope.popupModel.userResults = [];
                popoverScope.popupModel.filter = '';
                popoverScope.popupModel.emailMode = false;

                if(popoverScope.popupModel.added) {
                    popoverScope.popupModel.added = false;
                } else {
                    if(cancelledCallback) {
                        // Run callback in parent scope of directive
                        cancelledCallback($scope.$parent);
                    }
                }
            });

        };
        return directive;
    }]);

activitiModule.
directive('selectFunctionalGroupPopover', ['$rootScope', '$http', '$popover','appResourceRoot', 'FunctionalGroupService', '$parse',
    function($rootScope, $http, $popover, appResourceRoot, FunctionalGroupService, $parse) {

    var directive = {};
    directive.restrict = 'A';

    directive.scope = {
        type: '=type',
        ignoreContainer: '=ignoreContainer',
        restrictWithGroup: '=restrictWithGroup',
        excludeGroupIds: '=excludeGroupIds'
    };

    directive.link = function($scope, $element, attrs) {
        // Set defaults
        var placement = "bottom";

        $element.addClass("toggle-functional-group-select");

        if (attrs.placement) {
            placement = attrs.placement;
        }

        var closeOnSelect = true;
        if (attrs.closeOnSelect !== undefined) {
            closeOnSelect = attrs.closeOnSelect;
        }

        if ($scope.ignoreContainer) {
            $scope.popover = $popover($element, {template: appResourceRoot + '../views/common/popover/select-functional-group-popover.html?' +
                Date.now(), placement: placement});

        } else {
            $scope.popover = $popover($element, {template: appResourceRoot + '../views/common/popover/select-functional-group-popover.html?' +
                Date.now(), placement: placement, container: 'body'});
        }

        // Parse callbacks
        var selectedCallback, cancelledCallback;
        if (attrs['onGroupSelected']) {
            selectedCallback = $parse(attrs['onGroupSelected']);
        }
        if (attrs['onCancel']) {
            cancelledCallback = $parse(attrs['onCancel']);
        }

        var popoverScope = $scope.popover.$scope;
        popoverScope.title = attrs['popoverTitle'];

        popoverScope.popupModel = {
            groupResults: []
        };

        popoverScope.$watch('popupModel.filter', function() {
            if (popoverScope.popupModel.filter && popoverScope.popupModel.filter.length > 0) {

                var tenantId;
                if ($rootScope.common !== null && $rootScope.common !== undefined && $rootScope.common.selectedTenantId !== null && $rootScope.common.selectedTenantId !== undefined) {
                    tenantId = $rootScope.common.selectedTenantId > 0 ? $rootScope.common.selectedTenantId : undefined;
                }

               FunctionalGroupService.getFilteredGroups(popoverScope.popupModel.filter, $scope.restrictWithGroup, tenantId).then(function(result) {
                    var groups = [];
                    if ($scope.excludeGroupId != null && $scope.excludeGroupId) {
                        for (var groupIndex=0; groupIndex < result.data.length; groupIndex++) {
                            if (result.data[groupIndex].id !== $scope.excludeGroupId) {
                                groups.push(result.data[groupIndex]);
                            }
                        }
                    } else if ($scope.excludeGroupIds != null && $scope.excludeGroupIds !== undefined) {
                        for (var groupIndex=0; groupIndex < result.data.length; groupIndex++) {
                            if ($scope.excludeGroupIds.indexOf(result.data[groupIndex].id) < 0) {
                                groups.push(result.data[groupIndex]);
                            }
                        }
                    } else {
                        groups = result.data;
                    }
                    popoverScope.popupModel.groupResults = groups;
                    popoverScope.resetSelection();
                });
            } else {
                popoverScope.resetSelection();
                popoverScope.popupModel.groupResults = [];
            }
        });

        popoverScope.resetSelection = function() {
            popoverScope.popupModel.selectedGroup = undefined;
            popoverScope.popupModel.selectedIndex = -1;
        };

        popoverScope.nextGroup = function() {
            var groups = popoverScope.popupModel.groupResults;
            if (groups && groups.length > 0 && popoverScope.popupModel.selectedIndex < groups.length -1) {
                popoverScope.popupModel.selectedIndex+=1;
                popoverScope.popupModel.groupUser = groups[popoverScope.popupModel.selectedIndex];
            }
        };

        popoverScope.previousGroup = function() {
            var groups = popoverScope.popupModel.groupResults;
            if (groups && groups.length > 0 && popoverScope.popupModel.selectedIndex > 0) {
                popoverScope.popupModel.selectedIndex-=1;
                popoverScope.popupModel.selectedGroup = groups[popoverScope.popupModel.selectedIndex];
            }
        };

        popoverScope.confirmGroup = function(group) {
            if (!group) {
                // Selection is done with keyboard, use selection index
                var groups = popoverScope.popupModel.groupResults;
                if (popoverScope.popupModel.selectedIndex >= 0 && popoverScope.popupModel.selectedIndex < groups.length) {
                    group = groups[popoverScope.popupModel.selectedIndex];
                }
            }

            if (group) {
                if(selectedCallback) {
                    // Run callback in parent scope of directive
                    selectedCallback($scope.$parent, {'group': group});
                }

                if (closeOnSelect === 'true') {
                    popoverScope.$hide();
                } else {
                    var groups = popoverScope.popupModel.groupResults;
                    groups.splice(jQuery.inArray(group, groups), 1);
                    popoverScope.popupModel.selectedIndex = 0;
                    popoverScope.popupModel.selectedGroup = groups[popoverScope.popupModel.selectedIndex];
                }
            }
        };

        popoverScope.$on('tooltip.hide', function() {
            popoverScope.popupModel.groupResults = [];
            popoverScope.popupModel.filter = '';

            if (popoverScope.popupModel.added) {
                popoverScope.popupModel.added = false;
            } else {
                if (cancelledCallback) {
                    // Run callback in parent scope of directive
                    cancelledCallback($scope.$parent);
                }
            }
        });

    };
    return directive;
}]);

activitiModule.directive('tabControl', ['$compile', '$http', '$templateCache', function($compile, $http, $templateCache) {

        var updateTemplate = function($scope, element, attributes) {
            if(!$scope.activeTemplate || $scope.activeTemplate != $scope.activeTab.id) {
                // Check if current loaded template is still the right one
                var contentDiv = $(element.children()[1]);

                var childScope = angular.element(element.children()[1]).scope();
                if($scope.activeTemplate && childScope != $scope) {
                    // Child-scope created by the included element, should be destroyed
                    childScope.$destroy();
                }

                if($scope.activeTab && $scope.activeTab.templateUrl) {
                    // Load the HTML-fragment or get from cache
                    var loader = $http.get($scope.activeTab.templateUrl, {cache: $templateCache});
                    var promise = loader.success(function(html) {
                        contentDiv.html(html);
                    }).then(function (response) {
                        $scope.activeTemplate = $scope.activeTab.id;
                        contentDiv.replaceWith($compile(contentDiv.html())($scope));
                    });
                } else {
                    // No templates are being used, no need to use the contentDiv for this tab, clear it
                    contentDiv.empty();
                }
            }
        };

        var directive = {};
        directive.restrict = 'A';
        directive.replace = true;
        directive.transclude = true;
        directive.template = '<div><div class="clearfix"><ul class="tabs clearfix">' +
            '<li ng-repeat="tab in tabs" ng-class="{\'active\': tab.id == activeTab.id}"><a ng-click="tabClicked(tab)">{{tab.title && (tab.title | translate) || (tab.name | translate)}}</a></li>' +
            '</ul></div>' +
            '<div></div>' +
            '</div>';

        directive.scope = {
            possibleTabs : "=tabControl",
            model: "=model",
            activeTabReference: "=activeTab"
        };


        directive.controller = ['$scope', '$element', function($scope, $element) {

            $scope.refreshTabs = function() {
                var tabs = [];
                for(var i=0; i < $scope.possibleTabs.length; i++) {
                    var tab = $scope.possibleTabs[i];
                    if(!tab.hide) {
                        tabs.push(tab);
                    }
                }
                $scope.tabs = tabs;
            };

            $scope.$watch('possibleTabs', function() {
                $scope.refreshTabs();
            }, true);

            $scope.$watch('activeTabReference', function(newValue, oldValue) {
                if(!$scope.activeTab || $scope.activeTab.id != newValue) {
                    // Active tab ID changed from outside of the directive controller, need to switch to the
                    // right tab within this scope
                    var newTab = $scope.findTab(newValue);
                    if(newTab) {
                        $scope.tabClicked(newTab);
                    }
                }
            });

            $scope.findTab = function(tabId) {
                if($scope.possibleTabs) {
                    for(var i=0; i< $scope.possibleTabs.length; i++) {
                        if($scope.possibleTabs[i].id == tabId) {
                            return $scope.possibleTabs[i];
                        }
                    }
                }
                return undefined;
            };

            $scope.tabClicked = function(tab) {
                if (tab.hide) {
                    tab.hide = false;
                    $scope.refreshTabs();
                }
                $scope.activeTab = tab;
                if (tab) {
                    $scope.activeTabReference = tab.id;
                } else {
                    $scope.activeTabReference = undefined;
                }
                updateTemplate($scope, $element);
            };

            $scope.refreshTabs();

            if($scope.tabs && $scope.tabs.length > 0) {
                if($scope.activeTabReference) {
                    $scope.activeTab = $scope.findTab($scope.activeTabReference);
                }

                if(!$scope.activeTab) {
                    // Revert to the first tab, if no tab is forced to be shown first
                    $scope.activeTab = $scope.tabs[0];
                }
                $scope.tabClicked($scope.activeTab);
            }
        }];

        directive.link = updateTemplate;

        return directive;
    }]);

/**
 * Directive that calls the function present in the toggle-dragover attribute with a single parameter (over) when
 * dragging over the element has started (over = true) or ended (over = false)
 */
activitiModule
    .directive('toggleDragover', ["$document", "$parse", function ($document, $parse) {
        var linkFunction = function ($scope, $element, $attributes) {


            var toggleFunction = $attributes.toggleDragover;
            var callback = $parse(toggleFunction);

            var el = $element[0];

            el.addEventListener('dragenter',function(e) {
                    $scope.$apply(function() {
                        callback($scope, {'over': true});
                    });
                    return false;
                },
                false
            );

            el.addEventListener('dragleave', function(e) {
                    $scope.$apply(function() {
                        callback($scope, {'over': false});
                    });
                    return false;
                },
                false
            );
        };

        return( linkFunction );
    }]);

activitiModule.directive('editInPlace', function () {
    return {
        restrict: 'E',
        scope: {
            value: '='
        },
        template: '<span ng-click="edit()" ng-bind="value"></span><span class="glyphicon glyphicon-pencil edit-in-place-icon"></span><input ng-model="value" class="inline-edit-value form-control" ng-blur="stopEdit()" custom-keys enter-pressed="stopEdit()">',
        link: function ($scope, element, attrs) {

            var iconElement = angular.element(element.children()[1]);
            var inputElement = angular.element(element.children()[2]);

            // This directive should have a set class so we can style it.
            element.addClass('edit-in-place');

            // Initially, we're not editing.
            $scope.editing = false;

            // ng-click handler to activate edit-in-place
            $scope.edit = function () {
                $scope.editing = true;

                // We control display through a class on the directive itself. See the CSS.
                element.addClass('active');

                // And we must focus the element.
                // `angular.element()` provides a chainable array, like jQuery so to access a native DOM function,
                // we have to reference the first element in the array.
                inputElement[0].focus();
            };

            $scope.stopEdit = function() {
                $scope.editing = false;
                element.removeClass('active');
            };

//            // When we leave the input, we're done editing.
//            inputElement.prop('onblur', function () {
//                console.log('ONBLUR');
//                $scope.editing = false;
//                element.removeClass('active');
//            });
        }
    };
});


/* UTILITY METHODS */

/**
 * This creates a modal window that auto closes on route change.
 * By default, this is NOT the case, and leads to some funny behaviour.
 *
 * Use this method vs the default $modal({myJson}) approach
 */
var _internalCreateModal = function(modalConfig, $modal, $scope) {

    if ($scope !== null && $scope !== undefined) {
        $scope.modal = $modal(modalConfig);

        $scope.$on('$routeChangeStart', function () {
            if ($scope.modal) {
                $scope.modal.hide();
            }
        });

        return $scope.modal;
    } else {
        return $modal(modalConfig);
    }

};

activitiModule.
    directive('numberInputCheck', function() {

        return {
            require: 'ngModel',
            link: function(scope, element, attrs, modelCtrl) {

                modelCtrl.$parsers.push(function (inputValue) {

                    var transformedInput;
                    if (inputValue && inputValue.indexOf('-') == 0) {
                        transformedInput = inputValue.substr(1).replace(/([^0-9])/g, '');
                        transformedInput = '-' + transformedInput;
                    } else {
                        transformedInput = inputValue.replace(/([^0-9])/g, '');
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
