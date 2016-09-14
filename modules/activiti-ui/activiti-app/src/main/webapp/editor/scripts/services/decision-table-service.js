/*
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
'use strict';

// Decision Table service
angular.module('activitiModeler').service('DecisionTableService', [ '$rootScope', '$http', '$q', '$timeout', '$translate',
    function ($rootScope, $http, $q, $timeout, $translate) {

        var httpAsPromise = function(options) {
            var deferred = $q.defer();
            $http(options).
                success(function (response, status, headers, config) {
                    deferred.resolve(response);
                })
                .error(function (response, status, headers, config) {
                    console.log('Something went wrong during http call:' + response);
                    deferred.reject(response);
                });
            return deferred.promise;
        };

        this.filterDecisionTables = function(filter) {
            return httpAsPromise(
                {
                    method: 'GET',
                    url: ACTIVITI.CONFIG.contextRoot + '/app/rest/decision-table-models',
                    params: {filter: filter}
                }
            );
        };

        /**
         * Fetches the details of a decision table.
         */
        this.fetchDecisionTableDetails = function(modelId, historyModelId) {
            var url = ACTIVITI.CONFIG.contextRoot + '/app/rest/decision-table-models/';
            if (historyModelId) {
                url += 'history/' + encodeURIComponent(historyModelId);
            }
            else {
                url += encodeURIComponent(modelId);
            }
            return httpAsPromise({ method: 'GET', url: url });
        };

        function cleanUpModel (decisionTableDefinition) {
            delete decisionTableDefinition.isEmbeddedTable;
            var expressions = (decisionTableDefinition.inputExpressions || []).concat(decisionTableDefinition.outputExpressions || []);
            if (decisionTableDefinition.rules && decisionTableDefinition.rules.length > 0) {
                decisionTableDefinition.rules.forEach(function (rule) {
                    var headerExpressionIds = [];
                    expressions.forEach(function(def){
                        headerExpressionIds.push(def.id);
                    });

                    // Make sure that the rule has all header ids defined as attribtues
                    headerExpressionIds.forEach(function(id){
                        if (!rule.hasOwnProperty(id)) {
                            rule[id] = "";
                        }
                    });

                    // Make sure that the rule does not have an attribute that is not a header id
                    delete rule.$$hashKey;
                    for (var id in rule) {
                        if (headerExpressionIds.indexOf(id) === -1) {
                            delete rule[id];
                            delete rule.validationErrorMessages;
                        }
                    }

                });
            }
        }

        this.saveDecisionTable = function (data, name, key, description, saveCallback, errorCallback) {

            data.decisionTableRepresentation = {
            	name: name,
            	key: key
            };

            if (description && description.length > 0) {
                data.decisionTableRepresentation.description = description;
            }

            var decisionTableDefinition = angular.copy($rootScope.currentDecisionTable);

            data.decisionTableRepresentation.decisionTableDefinition = decisionTableDefinition;
            decisionTableDefinition.key = key;
            decisionTableDefinition.rules = $rootScope.currentDecisionTableRules;

            cleanUpModel(decisionTableDefinition);

			html2canvas(jQuery('#decisionTableGrid'), {
                onrendered: function (canvas) {

                    var scale = canvas.width / 300.0;

                    var extra_canvas = document.createElement('canvas');
                    extra_canvas.setAttribute('width', 300);
                    extra_canvas.setAttribute('height', canvas.height / scale);

                    var ctx = extra_canvas.getContext('2d');
                    ctx.drawImage(canvas, 0, 0, canvas.width, canvas.height, 0, 0, 300, canvas.height / scale);

                    data.decisionTableImageBase64 = extra_canvas.toDataURL('image/png');
                    
                    delete Array.prototype.toJSON;

	                $http({
	                    method: 'PUT',
	                    url: ACTIVITI.CONFIG.contextRoot + '/app/rest/decision-table-models/' + $rootScope.currentDecisionTable.id,
	                    data: data}).
	                
	                	success(function (response, status, headers, config) {

                            if (saveCallback) {
                                saveCallback();
                            }
                        }).
                        error(function (response, status, headers, config) {
                            if (errorCallback) {
                                errorCallback(response);
                            }
                        });
                }
            });
        };

        this.getDecisionTables = function (decisionTableIds, callback) {

            if (decisionTableIds.length > 0) {

                var decisionTableIdParams = '';
                for (var i = 0; i < decisionTableIds.length; i++) {
                    if (decisionTableIdParams.length > 0) {
                        decisionTableIdParams += '&';
                    }
                    decisionTableIdParams += 'decisionTableId=' + decisionTableIds[i];
                }
                if (decisionTableIdParams.length > 0) {
                    decisionTableIdParams += '&';
                }
                decisionTableIdParams += 'version=' + Date.now();

                $http({method: 'GET', url: ACTIVITI.CONFIG.contextRoot + '/app/rest/decision-table-models/values?' + decisionTableIdParams}).
                    success(function (data) {
                        if (callback) {
                            callback(data);
                        }
                    }).

                    error(function (data) {
                        console.log('Something went wrong when fetching decision table(s):' + JSON.stringify(data));
                    });
                    
            } else {
                if (callback) {
                    callback();
                }
            }
        };

    }]);
