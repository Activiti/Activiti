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

var activitiModeler = angular.module('activitiModeler', [
  'http-auth-interceptor',
  'ngCookies',
  'ngResource',
  'ngSanitize',
  'ngRoute',
  'ngDragDrop',
  'mgcrea.ngStrap',
  'mgcrea.ngStrap.helpers.dimensions', // Needed for tooltips
  'ui.grid',
  'ui.grid.edit',
  'ui.grid.selection',
  'ui.grid.autoResize',
  'ui.grid.moveColumns',
  'ui.grid.cellNav',
  'ngAnimate',
  'pascalprecht.translate',
  'ngFileUpload',
  'angularSpectrumColorpicker',
  'duScroll',
  'dndLists'
]);

var activitiModule = activitiModeler;
var activitiApp = activitiModeler;
wireServices(activitiModeler);

activitiModeler
  // Initialize routes
  .config(['$provide', '$routeProvider', '$selectProvider', '$translateProvider', function ($provide, $routeProvider, $selectProvider, $translateProvider) {

    var appName = 'editor';
    $provide.value('appName', appName);
    var appResourceRoot = ACTIVITI.CONFIG.webContextRoot + (ACTIVITI.CONFIG.webContextRoot ? '/' + appName + '/' : '');
    $provide.value('appResourceRoot', appResourceRoot);


    // Override caret for bs-select directive
      angular.extend($selectProvider.defaults, {
          caretHtml: '&nbsp;<i class="icon icon-caret-down"></i>'
      });


      var authRouteResolver = ['$rootScope', 'AuthenticationSharedService', function($rootScope, AuthenticationSharedService) {

        if(!$rootScope.authenticated) {
          // Return auth-promise. On success, the promise resolves and user is assumed authenticated from now on. If
          // promise is rejected, route will not be followed (no unneeded HTTP-calls will be done, which case a 401 in the end, anyway)
          return AuthenticationSharedService.authenticate();

        } else {
          // Authentication done on rootscope, no need to call service again. Any unauthenticated access to REST will result in
          // a 401 and will redirect to login anyway. Done to prevent additional call to authenticate every route-change
          $rootScope.authenticated = true;
          return true;
        }
      }];

        $routeProvider
            .when('/login', {
                templateUrl: appResourceRoot + 'views/login.html',
                controller: 'LoginController'
            })
            .when('/processes', {
                templateUrl: appResourceRoot + 'views/processes.html',
                controller: 'ProcessesCtrl',
                resolve: {
                    verify: authRouteResolver
                }
            })
            .when('/processes/:modelId', {
                templateUrl: appResourceRoot + 'views/process.html',
                controller: 'ProcessCtrl',
                resolve: {
                    verify: authRouteResolver
                }
            })
            .when('/processes/:modelId/history/:modelHistoryId', {
                templateUrl: appResourceRoot + 'views/process.html',
                controller: 'ProcessCtrl',
                resolve: {
                    verify: authRouteResolver
                }
            })
            .when('/forms', {
                templateUrl: appResourceRoot + 'views/forms.html',
                controller: 'FormsCtrl',
                resolve: {
                    verify: authRouteResolver
                }
            })
            .when('/forms/:modelId', {
                templateUrl: appResourceRoot + 'views/form.html',
                controller: 'FormCtrl',
                resolve: {
                    verify: authRouteResolver
                }
            })
            .when('/forms/:modelId/history/:modelHistoryId', {
                templateUrl: appResourceRoot + 'views/form.html',
                controller: 'FormCtrl',
                resolve: {
                    verify: authRouteResolver
                }
            })
            .when('/decision-tables', {
                templateUrl: appResourceRoot + 'views/decision-tables.html',
                controller: 'DecisionTablesController',
                resolve: {
                    verify: authRouteResolver
                }
            })
            .when('/decision-tables/:modelId', {
                templateUrl: appResourceRoot + 'views/decision-table.html',
                controller: 'DecisionTableDetailsCtrl',
                resolve: {
                    verify: authRouteResolver
                }
            })
            .when('/decision-tables/:modelId/history/:modelHistoryId', {
                templateUrl: appResourceRoot + 'views/decision-table.html',
                controller: 'DecisionTableDetailsCtrl',
                resolve: {
                    verify: authRouteResolver
                },
            })
            .when('/apps', {
                templateUrl: appResourceRoot + 'views/app-definitions.html',
                controller: 'AppDefinitionsCtrl',
                resolve: {
                    verify: authRouteResolver
                }
            })
            .when('/apps/:modelId', {
                templateUrl: appResourceRoot + 'views/app-definition.html',
                controller: 'AppDefinitionCtrl',
                resolve: {
                    verify: authRouteResolver
                }
            })
            .when('/apps/:modelId/history/:modelHistoryId', {
                templateUrl: 'views/app-definition.html',
                controller: 'AppDefinitionCtrl',
                resolve: {
                    verify: authRouteResolver
                }
            })
            .when('/editor/:modelId', {
                templateUrl: appResourceRoot + 'editor-app/editor.html',
                controller: 'EditorController',
                resolve: {
                    verify: authRouteResolver
                }
            })
	        .when('/form-editor/:modelId', {
	            templateUrl: appResourceRoot + 'views/form-builder.html',
	            controller: 'FormBuilderController',
	            resolve: {
	                verify: authRouteResolver
	            }
	        })
	        .when('/decision-table-editor/:modelId', {
                templateUrl: appResourceRoot + 'views/decision-table-editor.html',
                controller: 'DecisionTableEditorController',
                resolve: {
                    verify: authRouteResolver
                }
            })
	        .when('/app-editor/:modelId', {
                templateUrl: appResourceRoot + 'views/app-definition-builder.html',
                controller: 'AppDefinitionBuilderController',
                resolve: {
                    verify: authRouteResolver
                }
            });
            
        if (ACTIVITI.CONFIG.appDefaultRoute) {
            $routeProvider.when('/', {
                redirectTo: ACTIVITI.CONFIG.appDefaultRoute
            });
        }
        else {
            $routeProvider.when('/', {
                redirectTo: '/processes',
                resolve: {
                    verify: authRouteResolver
                }
            })
        }

        $routeProvider.otherwise({
            templateUrl: appResourceRoot + 'views/login.html',
            controller: 'LoginController'
        });

        // Initialize angular-translate
        $translateProvider.useStaticFilesLoader({
            prefix: appResourceRoot + 'i18n/',
            suffix: '.json'
        });

        $translateProvider.registerAvailableLanguageKeys(['en'], {
            'en_*': 'en',
            'en-*': 'en'
        });
        
  }])
  .run(['$rootScope', '$timeout', '$modal', '$translate', '$location', '$window', 'appResourceRoot',
        function($rootScope, $timeout, $modal, $translate, $location, $window, appResourceRoot) {

            $rootScope.restRootUrl = function() {
                return ACTIVITI.CONFIG.contextRoot;
            };

          $rootScope.appResourceRoot = appResourceRoot;

            $rootScope.window = {};
            var updateWindowSize = function() {
                $rootScope.window.width = $window.innerWidth;
                $rootScope.window.height  = $window.innerHeight;
            };

            // Window resize hook
            angular.element($window).bind('resize', function() {
                $rootScope.safeApply(updateWindowSize());
            });

            $rootScope.$watch('window.forceRefresh', function(newValue) {
                if(newValue) {
                    $timeout(function() {
                        updateWindowSize();
                        $rootScope.window.forceRefresh = false;
                    });
                }
            });

            updateWindowSize();

            // Main navigation
            $rootScope.mainNavigation = [
                {
                    'id': 'processes',
                    'title': 'GENERAL.NAVIGATION.PROCESSES',
                    'path': '/processes'
                },
                {
                    'id': 'forms',
                    'title': 'GENERAL.NAVIGATION.FORMS',
                    'path': '/forms'
                },
                {
                    'id': 'decision-tables',
                    'title': 'GENERAL.NAVIGATION.DECISION-TABLES',
                    'path': '/decision-tables'
                },
                {
                    'id': 'apps',
                    'title': 'GENERAL.NAVIGATION.APPS',
                    'path': '/apps'
                }
            ];

            $rootScope.config = ACTIVITI.CONFIG;

            $rootScope.mainPage = $rootScope.mainNavigation[0];

            /*
             * History of process and form pages accessed by the editor.
             * This is needed because you can navigate to sub processes and forms
             */
            $rootScope.editorHistory = [];

            /*
             * Set the current main page, using the page object. If the page is already active,
             * this is a no-op.
             */
            $rootScope.setMainPage = function(mainPage) {
                $rootScope.mainPage = mainPage;
                $location.path($rootScope.mainPage.path);
            };

            /*
             * Set the current main page, using the page ID. If the page is already active,
             * this is a no-op.
             */
            $rootScope.setMainPageById = function(mainPageId) {
                for (var i=0; i<$rootScope.mainNavigation.length; i++) {
                    if (mainPageId == $rootScope.mainNavigation[i].id) {
                        $rootScope.mainPage = $rootScope.mainNavigation[i];
                        break;
                    }
                }
            };

            /**
             * A 'safer' apply that avoids concurrent updates (which $apply allows).
             */
            $rootScope.safeApply = function(fn) {
                var phase = this.$root.$$phase;
                if(phase == '$apply' || phase == '$digest') {
                    if(fn && (typeof(fn) === 'function')) {
                        fn();
                    }
                } else {
                    this.$apply(fn);
                }
            };

            // Alerts
            $rootScope.alerts = {
                queue: []
            };

            $rootScope.showAlert = function(alert) {
                if(alert.queue.length > 0) {
                    alert.current = alert.queue.shift();
                    // Start timout for message-pruning
                    alert.timeout = $timeout(function() {
                        if (alert.queue.length == 0) {
                            alert.current = undefined;
                            alert.timeout = undefined;
                        } else {
                            $rootScope.showAlert(alert);
                        }
                    }, (alert.current.type == 'error' ? 5000 : 1000));
                } else {
                    $rootScope.alerts.current = undefined;
                }
            };

            $rootScope.addAlert = function(message, type) {
                var newAlert = {message: message, type: type};
                if (!$rootScope.alerts.timeout) {
                    // Timeout for message queue is not running, start one
                    $rootScope.alerts.queue.push(newAlert);
                    $rootScope.showAlert($rootScope.alerts);
                } else {
                    $rootScope.alerts.queue.push(newAlert);
                }
            };

            $rootScope.dismissAlert = function() {
                if (!$rootScope.alerts.timeout) {
                    $rootScope.alerts.current = undefined;
                } else {
                    $timeout.cancel($rootScope.alerts.timeout);
                    $rootScope.alerts.timeout = undefined;
                    $rootScope.showAlert($rootScope.alerts);
                }
            };

            $rootScope.addAlertPromise = function(promise, type) {
                if (promise) {
                    promise.then(function(data) {
                        $rootScope.addAlert(data, type);
                    });
                }
            };


            // Edit profile and change password
            $rootScope.editProfile = function() {
                _internalCreateModal({
                    template: 'views/popup/account-edit.html'
                }, $modal, $rootScope);
            };

            $rootScope.changePassword = function() {
                _internalCreateModal({
                    template: 'views/popup/account-change-password.html'
                }, $modal, $rootScope);
            };
        }
  ])
  .run(['$rootScope', '$location', 'AuthenticationSharedService', 'Account', '$translate', '$window', '$modal',
        function($rootScope, $location, AuthenticationSharedService, Account, $translate, $window , $modal) {
      
            var proposedLanguage = $translate.proposedLanguage();
            if (proposedLanguage !== 'de' && proposedLanguage !== 'en' && proposedLanguage !== 'es' && proposedLanguage !== 'fr'
                && proposedLanguage !== 'it' && proposedLanguage !== 'ja') {
              
                $translate.use('en');
            }

            var fixedUrlPart = '/editor/';

            $rootScope.logout = function() {
                AuthenticationSharedService.logout();
            };

            var redirectToLogin = function(data) {
                var absUrl = $location.absUrl();
                var index = absUrl.indexOf(fixedUrlPart);
                var newUrl;
                if (data !== null && data !== undefined && data.isFromLogout !== undefined && data.isFromLogout === true) {
                    newUrl = absUrl.substring(0, index) + '/#login';
                    if (ACTIVITI.CONFIG.loginUrl) {
                        newUrl = ACTIVITI.CONFIG.loginUrl.replace("{url}", $location.absUrl());
                    }
                } else {
                    newUrl = absUrl.substring(0, index) + '/#login?redirectUrl=' + encodeURIComponent($location.absUrl());
                    if (ACTIVITI.CONFIG.loginUrl) {
                        newUrl = ACTIVITI.CONFIG.loginUrl.replace("{url}", encodeURIComponent($location.absUrl()));
                    }
                }
                $window.location.href = newUrl;
            };


            // Call when the 401 response is returned by the client
            $rootScope.$on('event:auth-loginRequired', function(rejection) {
                $rootScope.authenticated = false;
                $rootScope.authenticationChecked = true;

                redirectToLogin();
            });

            // Call when the user is authenticated
           $rootScope.$on('event:auth-authConfirmed', function() {
        	   $rootScope.authenticated = true;
               Account.get().then(function () {

            	   if ($rootScope.account && $rootScope.account.type && $rootScope.account.type != 'enterprise' &&
                		   ($location.path() == '/stencils' || $location.path().indexOf('/stencils/') >= 0)) {

                	   $location.path('/processes');

           		   } else if ($location.path() == '' || $location.path() == '#') {
                	   $location.path('/processes');
                   }
               });
            });

            // Call when the user logs in
            $rootScope.$on('event:auth-loginConfirmed', function() {
                $rootScope.authenticated = true;
                $rootScope.account = Account.get();
                $location.path('/processes');
            });

            // Call when the user logs out
            $rootScope.$on('event:auth-loginCancelled', function(event, data) {
                $rootScope.authenticated = false;
                redirectToLogin(data);
            });
            // Call when login fails
            $rootScope.$on('event:auth-loginFailed', function() {
               $rootScope.addAlertPromise($translate('LOGIN.MESSAGES.ERROR.AUTHENTICATION'), 'error');
            });

            $rootScope.backToLanding = function() {
                var baseUrl = $location.absUrl();
                var index = baseUrl.indexOf(fixedUrlPart);
                if (index >= 0) {
                    baseUrl = baseUrl.substring(0, index);
                    baseUrl += '/';
                }
                $window.location.href = baseUrl;
            };
    }])

    // Moment-JS date-formatting filter
    .filter('dateformat', function() {
        return function(date, format) {
            if (date) {
                if (format) {
                    return moment(date).format(format);
                } else {
                    return moment(date).calendar();
                }
            }
            return '';
        };
    });