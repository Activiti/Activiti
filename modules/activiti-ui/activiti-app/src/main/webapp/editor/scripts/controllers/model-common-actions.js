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

angular.module('activitiModeler')
.controller('EditModelPopupCrtl', ['$rootScope', '$scope', '$http', '$translate', '$location',
    function ($rootScope, $scope, $http, $translate, $location) {

        var model;
        var popupType;
        if ($scope.model.process) {
            model = $scope.model.process;
            popupType = 'PROCESS';
        } else if ($scope.model.form) {
            model = $scope.model.form;
            popupType = 'FORM';
        } else if ($scope.model.decisionTable) {
            model = $scope.model.decisionTable;
            popupType = 'DECISION-TABLE';
        } else {
            model = $scope.model.app;
            popupType = 'APP';
        }

    	$scope.popup = {
    		loading: false,
    		popupType: popupType,
        	modelName: model.name,
        	modelKey: model.key,
        	modelDescription: model.description,
    		id: model.id
    	};

    	$scope.ok = function () {

    		if (!$scope.popup.modelName || $scope.popup.modelName.length == 0 ||
    			!$scope.popup.modelKey || $scope.popup.modelKey.length == 0) {
    			
    			return;
    		}

        	$scope.model.name = $scope.popup.modelName;
        	$scope.model.key = $scope.popup.modelKey;
        	$scope.model.description = $scope.popup.modelDescription;

    		$scope.popup.loading = true;
    		var updateData = {
    			name: $scope.model.name, 
    			key: $scope.model.key, description: 
    			$scope.model.description
    		};

    		$http({method: 'PUT', url: ACTIVITI.CONFIG.contextRoot + '/app/rest/models/' + $scope.popup.id, data: updateData}).
    			success(function(data, status, headers, config) {
    				if ($scope.model.process) {
    					$scope.model.process = data;
    				} else if ($scope.model.form) {
    					$scope.model.form = data;
    				} else if ($scope.model.decisionTable) {
    					$scope.model.decisionTable = data;
    				} else {
    					$scope.model.app = data;
    				}

    				$scope.addAlertPromise($translate('PROCESS.ALERT.EDIT-CONFIRM'), 'info');
    				$scope.$hide();
    				$scope.popup.loading = false;

    				if (popupType === 'FORM') {
                        $location.path("/forms/" +  $scope.popup.id);
                    } else if (popupType === 'APP') {
                        $location.path("/apps/" +  $scope.popup.id);
                    } else if (popupType === 'DECISION-TABLE') {
                        $location.path("/decision-tables/" +  $scope.popup.id);
                    } else {
                        $location.path("/processes/" +  $scope.popup.id);
                    }

    			}).
    			error(function(data, status, headers, config) {
    				$scope.popup.loading = false;
    				$scope.popup.errorMessage = data.message;
    			});
    	};

    	$scope.cancel = function () {
    		if (!$scope.popup.loading) {
    			$scope.$hide();
    		}
    	};
}]);

angular.module('activitiModeler')
    .controller('DeleteModelPopupCrtl', ['$rootScope', '$scope', '$http', '$translate', function ($rootScope, $scope, $http, $translate) {

        var model;
        var popupType;
        if ($scope.model.process) {
            model = $scope.model.process;
            popupType = 'PROCESS';
        } else if ($scope.model.form) {
            model = $scope.model.form;
            popupType = 'FORM';
        } else if ($scope.model.decisionTable) {
            model = $scope.model.decisionTable;
            popupType = 'DECISION-TABLE';
        } else {
            model = $scope.model.app;
            popupType = 'APP';
        }

        $scope.popup = {
            loading: true,
            loadingRelations: true,
            cascade: 'false',
            popupType: popupType,
            model: model
        };

        // Loading relations when opening
        $http({method: 'GET', url: ACTIVITI.CONFIG.contextRoot + '/app/rest/models/' + $scope.popup.model.id + '/parent-relations'}).
            success(function (data, status, headers, config) {
                $scope.popup.loading = false;
                $scope.popup.loadingRelations = false;
                $scope.popup.relations = data;
            }).
            error(function (data, status, headers, config) {
                $scope.$hide();
                $scope.popup.loading = false;
            });

        $scope.ok = function () {
            $scope.popup.loading = true;
            var params = {
                // Explicit string-check because radio-values cannot be js-booleans
                cascade: $scope.popup.cascade === 'true'
            };

            $http({method: 'DELETE', url: ACTIVITI.CONFIG.contextRoot + '/app/rest/models/' + $scope.popup.model.id, params: params}).
                success(function (data, status, headers, config) {
                    $scope.$hide();
                    $scope.popup.loading = false;
                    $scope.addAlertPromise($translate(popupType + '.ALERT.DELETE-CONFIRM'), 'info');
                    $scope.returnToList();
                }).
                error(function (data, status, headers, config) {
                    $scope.$hide();
                    $scope.popup.loading = false;
                });
        };

        $scope.cancel = function () {
            if (!$scope.popup.loading) {
                $scope.$hide();
            }
        };
    }]);

angular.module('activitiModeler')
.controller('UseAsNewVersionPopupCrtl', ['$rootScope', '$scope', '$http', '$translate', '$location', function ($rootScope, $scope, $http, $translate, $location) {

	var model;
	var popupType;
	if ($scope.model.process) {
		model = $scope.model.process;
		popupType = 'PROCESS';
	} else if ($scope.model.form) {
        model = $scope.model.form;
        popupType = 'FORM';
    } else if ($scope.model.decisionTable) {
        model = $scope.model.decisionTable;
        popupType = 'DECISION-TABLE';
    } else {
        model = $scope.model.app;
        popupType = 'APP';
    }

	$scope.popup = {
		loading: false,
		model: model,
		popupType: popupType,
		latestModelId: $scope.model.latestModelId,
		comment: ''
	};

	$scope.ok = function () {
		$scope.popup.loading = true;

		var actionData = {
			action: 'useAsNewVersion',
			comment: $scope.popup.comment
		};

		$http({method: 'POST', url: ACTIVITI.CONFIG.contextRoot + '/app/rest/models/' + $scope.popup.latestModelId + '/history/' + $scope.popup.model.id, data: actionData}).
			success(function(data, status, headers, config) {

                var backToOverview = function() {
                    if (popupType === 'FORM') {
                        $location.path("/forms/" +  $scope.popup.latestModelId);
                    } else if (popupType === 'APP') {
                        $location.path("/apps/" +  $scope.popup.latestModelId);
                    } else if (popupType === 'DECISION-TABLE') {
                        $location.path("/decision-tables/" +  $scope.popup.latestModelId);
                    } else {
                        $location.path("/processes/" +  $scope.popup.latestModelId);
                    }
                };


                if (data && data.unresolvedModels && data.unresolvedModels.length > 0) {

                    // There were unresolved models

                    $scope.popup.loading = false;
                    $scope.popup.foundUnresolvedModels = true;
                    $scope.popup.unresolvedModels = data.unresolvedModels;

                    $scope.close = function() {
                        $scope.$hide();
                        backToOverview();
                    };


                } else {

                    // All models working resolved perfectly

                    $scope.popup.loading = false;
                    $scope.$hide();

                    $scope.addAlertPromise($translate(popupType + '.ALERT.NEW-VERSION-CONFIRM'), 'info');
                    backToOverview();

                }

			}).
			error(function(data, status, headers, config) {
				$scope.$hide();
				$scope.popup.loading = false;
			});
	};

	$scope.cancel = function () {
		if (!$scope.popup.loading) {
			$scope.$hide();
		}
	};
}]);

/**
 * The controller for driving the share model popup.
 */
angular.module('activitiModeler')
	.controller('ShareModelPopupCrtl', ['$rootScope', '$scope', '$http', '$timeout', '$translate', 'UserService',
        function ($rootScope, $scope, $http, $timeout, $translate, UserService) {

            var model = $scope.model.app;
            var popupType = 'APP';

            $scope.popup = {
                loading: false,
                popupType: popupType,
                model: model,
                comment: '',
                updated: {},
                removed: {},
                added: {},
                groupsAdded: {},
                recentUsers: []
            };

            // Fetch the share info from the server
            var shareInfoUrl = ACTIVITI.CONFIG.contextRoot + '/app/rest/models/' + $scope.popup.model.id + '/share-info';

            $http({method: 'GET', url: shareInfoUrl}).
                success(function (data, status, headers, config) {
                    $scope.popup.shareInfo = data;

                    // Get user ids. Used in the people picker to filter out users
                    $scope.currentlySharedUserIds = [];

                    if (data && data.data && data.data.length > 0) {
                        for (var infoIndex = 0; infoIndex < data.data.length; infoIndex++) {

                            if (data.data[infoIndex].person !== null && data.data[infoIndex].person !== undefined) {
                                $scope.currentlySharedUserIds.push(data.data[infoIndex].person.id);
                            } 
                        }
                    }

                }).
                error(function (data, status, headers, config) {
                    $scope.$hide();
                    $scope.popup.loading = false;
                });

            /**
             * Change permission of a user
             */
            $scope.setPermission = function (info, permission) {
                if (!permission) { // no permission => User or group should be removed

                    var list = $scope.popup.shareInfo.data;

                    // Only send removal to server in case it was an existing share
                    if (info.id) { // Entries from the server have an id property

                        $scope.popup.removed[info.id] = true;

                        if ($scope.popup.updated[info.id]) {
                            delete $scope.popup.updated[info.id];
                        }

                    } else {
                        // Remove info from the added list as well
                        if (info.person && info.person.id) {
                            delete $scope.popup.added[info.person.id];

                        } else if (info.person.email) {
                            delete $scope.popup.added[info.person.email];
                        } 
                    }

                    if (info.person) {
                        var personIndex = $scope.currentlySharedUserIds.indexOf(info.person.id);
                        if (personIndex >= 0) {
                            $scope.currentlySharedUserIds.splice(personIndex, 1);
                        }
                    }

                    for (var i = 0; i < list.length; i++) {
                        if (list[i] == info) {
                            list.splice(i, 1);
                            break;
                        }
                    }

                } else {
                    info.permission = permission;
                    if (info.id) {
                        $scope.popup.updated[info.id] = permission;
                    }
                }
            };

            /**
             * Adds a user using en email address
             */
            $scope.addEmailUser = function (email) {
                var added = {person: {email: email}, permission: 'read'};
                if ($scope.popup.shareInfo.data) {

                    for (var i = 0; i < $scope.popup.shareInfo.data.length; i++) {
                        if ($scope.popup.shareInfo.data[i].email == email) {
                            // Skip duplicate emails
                            return;
                        }
                    }

                    $scope.popup.shareInfo.data.push(added);
                    $scope.popup.added[email] = added;

                } else {
                    $scope.popup.shareInfo.data = [added];
                    $scope.popup.added[email] = added;
                }

                $scope.popup.newPerson = undefined;
            };

            /**
             * Add a 'real user' (ie one from the same tenant)
             */
            $scope.addRealUser = function (user) {
                var added = {person: user, permission: 'read'};
                if ($scope.popup.shareInfo.data) {

                    // Skip duplicate person
                    for (var i = 0; i < $scope.popup.shareInfo.data.length; i++) {
                        if ($scope.popup.shareInfo.data[i].person && $scope.popup.shareInfo.data[i].person.id == user.id) {
                            return;
                        }
                    }

                    $scope.popup.shareInfo.data.splice(0, 0, added);
                    $scope.popup.added[user.id] = added;

                } else {
                    $scope.popup.shareInfo.data = [added];
                    $scope.popup.added[user.id] = added;

                }

                // Add to list that is filtered when fetching users
                $scope.currentlySharedUserIds.push(user.id);

                $scope.popup.newPerson = undefined;
            };

            /**
             * Saves the changes to the server
             */
            $scope.ok = function () {
                $scope.popup.loading = true;
                var shareData = {added: [], updated: [], removed: []};

                // Add additions
                for (var prop in $scope.popup.added) {
                    if ($scope.popup.added[prop].person) {
                        var person = $scope.popup.added[prop].person;
                        shareData.added.push({
                            userId: person.id,
                            email: person.email,
                            permission: $scope.popup.added[prop].permission
                        });
                    }
                }

                // Add removals
                for (var prop in $scope.popup.removed) {
                    shareData.removed.push(prop);
                }

                // Add updates
                for (var prop in $scope.popup.updated) {
                    shareData.updated.push({
                        id: prop,
                        permission: $scope.popup.updated[prop]
                    });
                }

                delete Array.prototype.toJSON;

                var putUrl = ACTIVITI.CONFIG.contextRoot + '/app/rest/models/' + $scope.popup.model.id + '/share-info';

                $http({method: 'PUT', url: putUrl, data: shareData}).
                    success(function (data, status, headers, config) {
                        $scope.$hide();

                        $scope.popup.loading = false;
                        $scope.addAlertPromise($translate(popupType + '.ALERT.SHARE-CONFIRM'), 'info');
                    }).
                    error(function (data, status, headers, config) {
                        $scope.$hide();
                        $scope.popup.loading = false;
                    });

            };

            /**
             * Close the popup
             */
            $scope.cancel = function () {
                if (!$scope.popup.loading) {
                    $scope.$hide();
                }
            };
}]);
