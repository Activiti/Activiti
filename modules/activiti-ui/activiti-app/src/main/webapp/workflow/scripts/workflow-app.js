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

var activitiApp = angular.module('activitiApp', [
    'http-auth-interceptor',
    'ngCookies',
    'ngResource',
    'ngSanitize',
    'ngRoute',
    'ngDragDrop',
    'mgcrea.ngStrap',
    'ngFileUpload',
    'ngAnimate',
    'pascalprecht.translate',
    'ui.grid',
    'ui.grid.edit',
    'ui.grid.selection',
    'ui.grid.autoResize',
    'angular-loading-bar',
    'cfp.hotkeys'
]);

var activitiModule = activitiApp;

activitiApp

  // Initialize routes
   .config(['$provide', '$routeProvider', '$selectProvider', '$datepickerProvider', '$translateProvider', 'cfpLoadingBarProvider',
   function ($provide, $routeProvider, $selectProvider, $datepickerProvider, $translateProvider, cfpLoadingBarProvider) {

   var appName = 'workflow';
   $provide.value('appName', appName);
   var appResourceRoot = ACTIVITI.CONFIG.webContextRoot + (ACTIVITI.CONFIG.webContextRoot ? '/' + appName + '/' : '');
   $provide.value('appResourceRoot', appResourceRoot);

    // Override caret for bs-select directive
    angular.extend($selectProvider.defaults, {
        caretHtml: '&nbsp;<i class="icon icon-caret-down"></i>'
    });

    // Override carets for bs-datepicker directive
    angular.extend($datepickerProvider.defaults, {
        iconLeft: 'icon icon-caret-left',
        iconRight: 'icon icon-caret-right'
    });

    /*
     * Route resolver for all authenticated routes
     */
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

    /*
     * Route resolver for all unauthenticated routes
     */
    var unauthRouteResolver = ['$rootScope', function($rootScope) {
      $rootScope.authenticationChecked = true;
    }];

    $routeProvider
        .when('/start-process', {
            templateUrl: appResourceRoot + 'views/start-process.html',
            controller: 'StartProcessController',
            resolve: {
                verify: authRouteResolver
            }
        })
        .when('/apps/:deploymentKey/start-process', {
            templateUrl: appResourceRoot + 'views/start-process.html',
            controller: 'StartProcessController',
            resolve: {
                verify: authRouteResolver
            }
        })
        .when('/tasks', {
            templateUrl: appResourceRoot + 'views/tasks.html',
            controller: 'TasksController',
            resolve: {
                verify: authRouteResolver
            }
        })
        .when('/apps/:deploymentKey/tasks', {
            templateUrl: appResourceRoot + 'views/tasks.html',
            controller: 'TasksController',
            resolve: {
                verify: authRouteResolver
            }
        })
        .when('/task/:taskId', {
            templateUrl: appResourceRoot + 'views/task.html',
            controller: 'TaskController',
            resolve: {
                verify: authRouteResolver
            }
        })
        .when('/apps/:deploymentKey/task/:taskId', {
            templateUrl: appResourceRoot + 'views/task.html',
            controller: 'TaskController',
            resolve: {
                verify: authRouteResolver
            }
        })
        .when('/processes', {
            templateUrl: appResourceRoot + 'views/processes.html',
            controller: 'ProcessesController',
            resolve: {
                verify: authRouteResolver
            }
        })
        .when('/apps/:deploymentKey/processes', {
            templateUrl: appResourceRoot + 'views/processes.html',
            controller: 'ProcessesController',
            resolve: {
                verify: authRouteResolver
            }
        })
        .when('/process/:processId', {
            templateUrl: appResourceRoot + 'views/process.html',
            controller: 'ProcessController',
            resolve: {
                verify: authRouteResolver
            }
        })
        .when('/apps/:deploymentKey/process/:processId', {
            templateUrl: appResourceRoot + 'views/process.html',
            controller: 'ProcessController',
            resolve: {
                verify: authRouteResolver
            }
        })
        .otherwise({
            redirectTo: ACTIVITI.CONFIG.appDefaultRoute || '/tasks'
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

       // turn loading bar spinner off (angular-loading-bar lib)
       cfpLoadingBarProvider.includeSpinner = false;
    }])
    .run(['$rootScope', '$routeParams', '$timeout', '$translate', '$location', '$http', '$window', 'appResourceRoot', 'AppDefinitionService',
        function($rootScope, $routeParams, $timeout, $translate, $location, $http, $window, appResourceRoot, AppDefinitionService) {

            $rootScope.restRootUrl = function() {
                return ACTIVITI.CONFIG.contextRoot;
            };

        $rootScope.config = ACTIVITI.CONFIG;
        $rootScope.appResourceRoot = appResourceRoot;
        $rootScope.activitiFieldIdPrefix = 'activiti-';

        var proposedLanguage = $translate.proposedLanguage();
        if (proposedLanguage !== 'de' && proposedLanguage !== 'en' && proposedLanguage !== 'es' && proposedLanguage !== 'fr'
            && proposedLanguage !== 'it' && proposedLanguage !== 'ja') {
            
            $translate.use('en');
        }
        
        $rootScope.window = {};
        var updateWindowSize = function() {
            $rootScope.window.width = $window.innerWidth;
            $rootScope.window.height  = $window.innerHeight;
        };

        // Window resize hook
        angular.element($window).bind('resize', function() {
            $rootScope.$apply(updateWindowSize());
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
                'id': 'login',
                'title': 'GENERAL.NAVIGATION.LOGIN',
                'unauthenticated': true
            },
            {
                'id': 'tasks',
                'title': 'GENERAL.NAVIGATION.TASKS',
                'path': '/tasks'
            },
            {
                'id': 'processes',
                'title': 'GENERAL.NAVIGATION.PROCESSES',
                'path': '/processes'
            }
        ];

        $rootScope.mainPage = $rootScope.mainNavigation[0];

        // Empty object to hold cached app-definitions
        $rootScope.appDefinitions = {

        };

        /*
         * Set the current main page, using the page object. If the page is already active,
         * this is a no-op.
         */
        $rootScope.setMainPage = function(mainPage) {
            $rootScope.mainPage = mainPage;

            var path;
            if($rootScope.activeAppDefinition) {
                path = "/apps/" + $rootScope.activeAppDefinition.id + mainPage.path;
            } else {
                path = $rootScope.mainPage.path;
            }
            $location.path(path);
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



        // Alerts
        $rootScope.alerts = {
            queue: []
        };

        $rootScope.showAlert = function(alert) {
            if(alert.queue.length > 0) {
                alert.current = alert.queue.shift();
                // Start timout for message-pruning
                alert.timeout = $timeout(function() {
                    if(alert.queue.length == 0) {
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
            if(!$rootScope.alerts.timeout) {
                // Timeout for message queue is not running, start one
                $rootScope.alerts.queue.push(newAlert);
                $rootScope.showAlert($rootScope.alerts);
            } else {
                $rootScope.alerts.queue.push(newAlert);
            }
        };

        $rootScope.dismissAlert = function() {
            if(!$rootScope.alerts.timeout) {
                $rootScope.alerts.current = undefined;
            } else {
                $timeout.cancel($rootScope.alerts.timeout);
                $rootScope.alerts.timeout = undefined;
                $rootScope.showAlert($rootScope.alerts);
            }
        };

        $rootScope.addAlertPromise = function(promise, type) {
            if(promise) {
                promise.then(function(data) {
                    $rootScope.addAlert(data, type);
                });
            }
        };

        $rootScope.model = {};
        // TODO: remove proc-def from rootscope or make smarter
        $rootScope.root = {};

        $rootScope.loadProcessDefinitions = function(deploymentKey) {
            var url = ACTIVITI.CONFIG.contextRoot + '/app/rest/process-definitions?latest=true';
            if (deploymentKey) {
                url += '&deploymentKey=' + deploymentKey;
            }
            $http({method: 'GET', url: url}).
                success(function(response, status, headers, config) {
                    $rootScope.root.processDefinitions = response.data;
                }).
                error(function(response, status, headers, config) {
                    console.log('Something went wrong: ' + response);
                });
        };

        $rootScope.$on("$locationChangeStart",
            function (event, newUrl, oldUrl) {
                if(newUrl.indexOf("headless") >= 0) {
                    $rootScope.root.headless = true;
                } else {
                    $rootScope.root.headless = false;
                }
            }
        );

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
    }
  ])
  .run(['$rootScope', '$location', '$window', 'AuthenticationSharedService', '$translate', 'appName', '$modal',
        function($rootScope, $location, $window, AuthenticationSharedService, $translate, appName , $modal) {

          var fixedUrlPart = '/' + appName + '/';

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
              $rootScope.authenticationChecked = true;

              if($location.path() == '' || $location.path()=='#' || $location.path() == '/login') {
                  $location.path('/');
              }
          });

          // Call when the user logs in
          $rootScope.$on('event:auth-loginConfirmed', function() {
              AuthenticationSharedService.authenticate();
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
                baseUrl = baseUrl.substring(0, index) + '/';
            }
            $window.location.href = baseUrl;
        }
    }])

    // Moment-JS date-formatting filter
    .filter('dateformat', function() {
        return function(date, format) {
            if (date) {
                if(format == 'fromNow') {
                    return moment(date).fromNow();
                } else if(format == 'fromNowFull') {
                    return moment(date).fromNow() + ' (' + moment(date).format('MMMM Do YYYY') + ')';
                } else if (format) {
                    return moment(date).format(format);
                } else {
                    return moment(date).calendar();
                }
            }
            return '';
        };
    })
    .filter('duration', ['$translate', function($translate) {
        return function(millis) {
            if (millis) {
                var duration = moment.duration(millis);
                var result = '';
                var hours = duration.hours();
                if (hours > 0) {
                    result = result + hours + ' ' + $translate.instant('GENERAL.TIME.HOURS') + ' ';
                }
                var mins = duration.minutes();
                if (mins > 0) {
                    result = result + mins + ' ' + $translate.instant('GENERAL.TIME.MINUTES');
                }

                if (hours == 0 && mins == 0) {
                    result = duration.seconds() + ' ' + $translate.instant('GENERAL.TIME.SECONDS');

                }

                return result;
            }
            return millis;
        };
    }])
    .filter('username', function() {
        return function(user) {
            if (user) {
               if(user.firstName) {
                   return user.firstName + " " + user.lastName;
               } else {
                   return user.lastName;
               }
            }
            return '';
        };
    });
