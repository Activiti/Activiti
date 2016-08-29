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

angular.module('activitiApp')
      .controller('StartProcessController', ['$rootScope', '$scope', '$translate', '$http', '$timeout','$location', '$route', '$modal', '$routeParams', '$q', 'AppDefinitionService', 'ProcessService', 'FormService', 'RelatedContentService',
   function ($rootScope, $scope, $translate, $http, $timeout, $location, $route, $modal, $routeParams, $q, AppDefinitionService, ProcessService, FormService, RelatedContentService) {

       $scope.deploymentKey = $routeParams.deploymentKey;
       $scope.missingAppdefinition = $scope.deploymentKey === false;

       AppDefinitionService.setActiveDeploymentKey($scope.deploymentKey);

       ProcessService.getProcessDefinitions($scope.deploymentKey).then(function(reponse){
           $scope.processDefinitions = reponse.data;
       });

      $scope.processName = '';
      $scope.processDefinition = null;
      $scope.processDefinitionWithStartFormId = null;
      $scope.startFormDefinition = null;

      // Support for prepopulating upload fields
      if (ACTIVITI.CONFIG.workflow && ACTIVITI.CONFIG.workflow.addStartProcessContent) {
         $scope.startProcessContent = ACTIVITI.CONFIG.workflow.addStartProcessContent;
         $scope.startProcessContentQueued = false;
      }
      $scope.addedStartProcessContent = null;

      $scope.selectProcessDefinition = function(definition){
         if ($scope.processDefinition == definition)
         {
            // deselect workflow
            $scope.processDefinition = null;
            $scope.processDefinitionWithStartFormId = null;
            $scope.startFormDefinition = null;
         }
         else {
            $scope.processDefinition = definition;
            if (definition.hasStartForm) {
               $scope.loadStartForm(definition);
            }
            else {
               $scope.processName = definition.name + ' - ' + new moment().format('MMMM Do YYYY');
               $scope.processDefinitionWithStartFormId = null;
               $scope.startFormDefinition = null;
            }
         }
      };

      $scope.loadStartForm = function(){
         // First lets see if there is any content that shall be added to the start form
         if (!$scope.startProcessContent) {
            // No content shall be added, just show the form
            $scope.setStartForm();
         }
         else if ($scope.startProcessContent) {
            // Start form shall be pre-populated with files
            if ($scope.addedStartProcessContent) {
              // Content has already been added, lets show the start form
               $scope.setStartForm();
            }
            else
            {
               // All content has not been added yet but is it being queued?
               if ($scope.startProcessContentQueued) {
                  // The content is being queued and added right now, don't do anything.
                  // When the queue is finished and setForm is called, it will use the last selected
                  // process definition
               }
               else {
                  // Lets queue the the content to be added
                  $scope.startProcessContentQueued = true;
                  if (typeof $scope.startProcessContent == 'function') {
                     // Instead of the actual content that hsall be added a function was provided,
                     // let it load the content so we can add it afterwards
                     $scope.startProcessContent(function(content){
                        // Replace the loader function with data
                        $scope.startProcessContent = content;

                        // Add the data
                        $scope.queueStartProcessContent().then(function(){
                           $scope.setStartForm();
                        });
                     }, function(code, msg){
                        $rootScope.addAlert(msg, 'error');
                     });
                  }
                  else {
                     // the data to be added is there, then add id
                     $scope.queueStartProcessContent().then(function(){
                        $scope.setStartForm();
                     });
                  }
               }
            }
         }
         else {
            // Content is already being added to the start form, lets make
         }
      };

      $scope.queueStartProcessContent = function(){
         var addedContent = [];
         var deferred = $q.defer();
         var contentToAdd = $scope.startProcessContent;
         var content;
         for (var i = 0; i < contentToAdd.length; i++) {
            content = contentToAdd[i];
            RelatedContentService.addRelatedContentFromSource(null, null, content.source, content.sourceId , content.name, content.link).then(function(result) {
               addedContent.push(result);
               if (addedContent.length == contentToAdd.length) {
                  $scope.addedStartProcessContent = addedContent;
                  deferred.resolve();
               }
            }, function(error) {
               $rootScope.addAlert($translate('PROCESS.ALERT.START-PROCESS-CONTENT-ADD-FAILED', { noOfFiles: contentToAdd.length }), 'error');
               deferred.reject();
            });
         }
         return deferred.promise;
      };

      $scope.setStartForm = function(){
         var definition = $scope.processDefinition;
         if (definition.hasStartForm) {
            // Load start form
            FormService.getStartForm(definition.id).then(function(formData) {
               // Merge in the content to become pre-populated upload fields
               if ($scope.addedStartProcessContent) {
                  var field;
                  var allFormFields = formData.fields;
                  for (var index in allFormFields) {
                     field = allFormFields[index];
                     if (field.type == 'upload') {
                        field.value = $scope.addedStartProcessContent;
                        break;
                     }
                  }
               }

               $scope.processName = definition.name + ' - ' + new moment().format('MMMM Do YYYY');
               $scope.processDefinition = definition;
               $scope.processDefinitionWithStartFormId = definition.id;
               $scope.startFormDefinition = formData;
            });
         }
      };

      $scope.navigateToView = function (view) {
          if ($scope.deploymentKey && !ACTIVITI.CONFIG.integrationProfile) {
              $location.path("/apps/" + encodeURIComponent($scope.deploymentKey)+ view);
          }
          else {
              $location.path(view);
          }
      };

      $scope.createProcessInstance = function(){
         ProcessService.createProcess({
            processDefinitionId: $scope.processDefinition.id,
            name: $scope.processName
         }).then(function(){
             $scope.navigateToView('/processes');
         })

      };

      var unregisterProcessStartedCallback = $scope.$on('process-started', function (event, data) {
         unregisterProcessStartedCallback();
          $scope.navigateToView('/processes');
      });


   }]);
