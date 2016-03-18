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
    .controller('ToolbarController', ['$scope', '$http', '$modal', '$q', '$rootScope', '$translate', '$location', function ($scope, $http, $modal, $q, $rootScope, $translate, $location) {

    	$scope.editorFactory.promise.then(function () {
	        var toolbarItems = KISBPM.TOOLBAR_CONFIG.items;
	        $scope.items = [];
	        
	        for (var i = 0; i < toolbarItems.length; i++)
	        {
	        	if ($rootScope.modelData.model.modelType === 'form')
		        {
		        	if (!toolbarItems[i].disableInForm)
		        	{
		        		$scope.items.push(toolbarItems[i]);
		        	}
		        }
	        	else
	        	{
	        		$scope.items.push(toolbarItems[i]);
	        	}
	        }
    	});
        
        $scope.secondaryItems = KISBPM.TOOLBAR_CONFIG.secondaryItems;

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
            var services = { '$scope' : $scope, '$rootScope' : $rootScope, '$http' : $http, '$modal' : $modal, '$q' : $q, '$translate' : $translate};
            executeFunctionByName(buttonClicked.action, window, services);

            // Other events
            var event = {
                type : KISBPM.eventBus.EVENT_TYPE_TOOLBAR_BUTTON_CLICKED,
                toolbarItem : buttonClicked
            };
            KISBPM.eventBus.dispatch(event.type, event);
        };
        
        // Click handler for secondary toolbar buttons
        $scope.toolbarSecondaryButtonClicked = function(buttonIndex) {
            var buttonClicked = $scope.secondaryItems[buttonIndex];
            var services = { '$scope' : $scope, '$http' : $http, '$modal' : $modal, '$q' : $q, '$translate' : $translate, '$location': $location};
            executeFunctionByName(buttonClicked.action, window, services);
        };
        
        /* Key bindings */
        Mousetrap.bind(['command+z', 'ctrl+z'], function(e) {
        	var services = { '$scope' : $scope, '$rootScope' : $rootScope, '$http' : $http, '$modal' : $modal, '$q' : $q, '$translate' : $translate};
        	KISBPM.TOOLBAR.ACTIONS.undo(services);
            return false;
        });
        
        Mousetrap.bind(['command+y', 'ctrl+y'], function(e) {
        	var services = { '$scope' : $scope, '$rootScope' : $rootScope, '$http' : $http, '$modal' : $modal, '$q' : $q, '$translate' : $translate};
        	KISBPM.TOOLBAR.ACTIONS.redo(services);
            return false;
        });
        
        Mousetrap.bind(['command+c', 'ctrl+c'], function(e) {
        	var services = { '$scope' : $scope, '$rootScope' : $rootScope, '$http' : $http, '$modal' : $modal, '$q' : $q, '$translate' : $translate};
        	KISBPM.TOOLBAR.ACTIONS.copy(services);
            return false;
        });
        
        Mousetrap.bind(['command+v', 'ctrl+v'], function(e) {
        	var services = { '$scope' : $scope, '$rootScope' : $rootScope, '$http' : $http, '$modal' : $modal, '$q' : $q, '$translate' : $translate};
        	KISBPM.TOOLBAR.ACTIONS.paste(services);
            return false;
        });
        
        Mousetrap.bind(['del'], function(e) {
        	var services = { '$scope' : $scope, '$rootScope' : $rootScope, '$http' : $http, '$modal' : $modal, '$q' : $q, '$translate' : $translate};
        	KISBPM.TOOLBAR.ACTIONS.deleteItem(services);
            return false;
        });

        /* Undo logic */

        $scope.undoStack = [];
        $scope.redoStack = [];

        $scope.editorFactory.promise.then(function() {

            // Catch all command that are executed and store them on the respective stacks
            $scope.editor.registerOnEvent(ORYX.CONFIG.EVENT_EXECUTE_COMMANDS, function( evt ){

                // If the event has commands
                if( !evt.commands ){ return; }

                $scope.undoStack.push( evt.commands );
                $scope.redoStack = [];
                
                for(var i = 0; i < $scope.items.length; i++) 
        		{
                    var item = $scope.items[i];
                    if (item.action === 'KISBPM.TOOLBAR.ACTIONS.undo')
                    {
                    	item.enabled = true;
                    }
                    else if (item.action === 'KISBPM.TOOLBAR.ACTIONS.redo')
                    {
                    	item.enabled = false;
                    }
        		}

                // Update
                $scope.editor.getCanvas().update();
                $scope.editor.updateSelection();

            });

        });
        
        // Handle enable/disable toolbar buttons 
        $scope.editorFactory.promise.then(function() {
        	$scope.editor.registerOnEvent(ORYX.CONFIG.EVENT_SELECTION_CHANGED, function( evt ){
        		var elements = evt.elements;
        		
        		for(var i = 0; i < $scope.items.length; i++) 
        		{
                    var item = $scope.items[i];
                    if (item.enabledAction && item.enabledAction === 'element')
                    {
                    	var minLength = 1;
                    	if(item.minSelectionCount) {
                    		minLength = item.minSelectionCount;
                    	}
                    	if (elements.length >= minLength && !item.enabled) {
                    		$scope.safeApply(function () {
                    			item.enabled = true;
                            });
                    	}
                    	else if (elements.length == 0 && item.enabled) {
                    		$scope.safeApply(function () {
                    			item.enabled = false;
                            });
                    	}
                    }
                }
        	});
        	
        });

    }]);