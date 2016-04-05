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

/**
 * General bootstrap of the application.
 */
angular.module('activitiModeler')
    .controller('EditorController', ['$rootScope', '$scope', '$http', '$q', '$routeParams', '$timeout', '$location', '$translate', '$modal', 'FormBuilderService',
        function ($rootScope, $scope, $http, $q, $routeParams, $timeout, $location, $translate, $modal, FormBuilderService) {

    $rootScope.editorFactory = $q.defer();

    $rootScope.forceSelectionRefresh = false;

    $rootScope.ignoreChanges = false; // by default never ignore changes

    $rootScope.staticIncludeVersion = Date.now();

    /**
     * Initialize the event bus: couple all Oryx events with a dispatch of the
     * event of the event bus. This way, it gets much easier to attach custom logic
     * to any event.
     */


    /* Helper method to fetch model from server (always needed) */
    function fetchModel() {

        var modelUrl;
        if ($routeParams.modelId) {
            modelUrl = KISBPM.URL.getModel($routeParams.modelId);
        } else {
            modelUrl = KISBPM.URL.newModelInfo();
        }

        $http({method: 'GET', url: modelUrl}).
            success(function (data, status, headers, config) {
                $rootScope.editor = new ORYX.Editor(data);
                $rootScope.modelData = angular.fromJson(data);
                $rootScope.editorFactory.resolve();
            }).
            error(function (data, status, headers, config) {
                $location.path("/processes/");
            });
    }


    function initScrollHandling() {
        var canvasSection = jQuery('#canvasSection');
        canvasSection.scroll(function() {

            // Hides the resizer and quick menu items during scrolling

            var selectedElements = $rootScope.editor.selection;
            var subSelectionElements = $rootScope.editor._subSelection;

            $scope.selectedElements = selectedElements;
            $scope.subSelectionElements = subSelectionElements;
            if (selectedElements && selectedElements.length > 0)
            {
            	$rootScope.selectedElementBeforeScrolling = selectedElements[0];
            }

            jQuery('.Oryx_button').each(function(i, obj) {
                  $scope.orginalOryxButtonStyle = obj.style.display;
                  obj.style.display = 'none';
            });
            jQuery('.resizer_southeast').each(function(i, obj) {
                  $scope.orginalResizerSEStyle = obj.style.display;
                  obj.style.display = 'none';
            });
            jQuery('.resizer_northwest').each(function(i, obj) {
                  $scope.orginalResizerNWStyle = obj.style.display;
                  obj.style.display = 'none';
            });
            $rootScope.editor.handleEvents({type:ORYX.CONFIG.EVENT_CANVAS_SCROLL});
        });

        canvasSection.scrollStopped(function(){

            // Puts the quick menu items and resizer back when scroll is stopped.

            $rootScope.editor.setSelection([]); // needed cause it checks for element changes and does nothing if the elements are the same
            $rootScope.editor.setSelection($scope.selectedElements, $scope.subSelectionElements);
            $scope.selectedElements = undefined;
            $scope.subSelectionElements = undefined;

            function handleDisplayProperty(obj) {
                if (jQuery(obj).position().top > 0) {
                    obj.style.display = 'block';
                } else {
                    obj.style.display = 'none';
                }
            }

            jQuery('.Oryx_button').each(function(i, obj) {
                handleDisplayProperty(obj);
            });
            jQuery('.resizer_southeast').each(function(i, obj) {
                handleDisplayProperty(obj);
            });
            jQuery('.resizer_northwest').each(function(i, obj) {
                handleDisplayProperty(obj);
            });

        });
    }

    /**
     * Initialize the Oryx Editor when the content has been loaded
     */
    if (!$rootScope.editorInitialized) {

        ORYX._loadPlugins();

        fetchModel();

        /**
         * A 'safer' apply that avoids concurrent updates (which $apply allows).
         */
        $rootScope.safeApply = function(fn) {
        	if (this.$root) {
	            var phase = this.$root.$$phase;
	            if(phase == '$apply' || phase == '$digest') {
	                if(fn && (typeof(fn) === 'function')) {
	                    fn();
	                }
	            } else {
	                this.$apply(fn);
	            }
	            
        	} else {
                this.$apply(fn);
            }
        };

        /**
         * Initialize the event bus: couple all Oryx events with a dispatch of the
         * event of the event bus. This way, it gets much easier to attach custom logic
         * to any event.
         */

        $rootScope.editorFactory.promise.then(function() {

            $rootScope.formItems = undefined;

            KISBPM.eventBus.editor = $rootScope.editor;

            var eventMappings = [
                { oryxType : ORYX.CONFIG.EVENT_SELECTION_CHANGED, kisBpmType : KISBPM.eventBus.EVENT_TYPE_SELECTION_CHANGE },
                { oryxType : ORYX.CONFIG.EVENT_DBLCLICK, kisBpmType : KISBPM.eventBus.EVENT_TYPE_DOUBLE_CLICK },
                { oryxType : ORYX.CONFIG.EVENT_MOUSEOUT, kisBpmType : KISBPM.eventBus.EVENT_TYPE_MOUSE_OUT },
                { oryxType : ORYX.CONFIG.EVENT_MOUSEOVER, kisBpmType : KISBPM.eventBus.EVENT_TYPE_MOUSE_OVER }

            ];

            eventMappings.forEach(function(eventMapping) {
                $rootScope.editor.registerOnEvent(eventMapping.oryxType, function(event) {
                    KISBPM.eventBus.dispatch(eventMapping.kisBpmType, event);
                });
            });

            // The Oryx canvas is ready (we know since we're in this promise callback) and the
            // event bus is ready. The editor is now ready for use
            KISBPM.eventBus.dispatch(KISBPM.eventBus.EVENT_TYPE_EDITOR_READY, {type : KISBPM.eventBus.EVENT_TYPE_EDITOR_READY});

            // Show getting started if this is the first time (boolean true for use local storage)
            ACTIVITI_EDITOR_TOUR.gettingStarted($scope, $translate, $q, true);

        });

        // Hook in resizing of main panels when window resizes
        // TODO: perhaps move to a separate JS-file?
        jQuery(window).resize(function () {

            // Calculate the offset based on the bottom of the module header
            var offset = jQuery("#editor-header").offset();
            var propSectionHeight = jQuery('#propertySection').height();
            var canvas = jQuery('#canvasSection');
            var mainHeader = jQuery('#main-header');

            if (offset == undefined || offset === null
                || propSectionHeight === undefined || propSectionHeight === null
                || canvas === undefined || canvas === null || mainHeader === null) {
                return;
            }

            if ($rootScope.editor)
        	{
	        	var selectedElements = $rootScope.editor.selection;
	            var subSelectionElements = $rootScope.editor._subSelection;

	            $scope.selectedElements = selectedElements;
	            $scope.subSelectionElements = subSelectionElements;
	            if (selectedElements && selectedElements.length > 0)
	            {
	            	$rootScope.selectedElementBeforeScrolling = selectedElements[0];

	            	$rootScope.editor.setSelection([]); // needed cause it checks for element changes and does nothing if the elements are the same
	                $rootScope.editor.setSelection($scope.selectedElements, $scope.subSelectionElements);
	                $scope.selectedElements = undefined;
	                $scope.subSelectionElements = undefined;
	            }
        	}

            var totalAvailable = jQuery(window).height() - offset.top - mainHeader.height() - 21;
            canvas.height(totalAvailable - propSectionHeight);
            jQuery('#paletteSection').height(totalAvailable);

            // Update positions of the resize-markers, according to the canvas

            var actualCanvas = null;
            if (canvas && canvas[0].children[1]) {
                actualCanvas = canvas[0].children[1];
            }

            var canvasTop = canvas.position().top;
            var canvasLeft = canvas.position().left;
            var canvasHeight = canvas[0].clientHeight;
            var canvasWidth = canvas[0].clientWidth;
            var iconCenterOffset = 8;
            var widthDiff = 0;

            var actualWidth = 0;
            if(actualCanvas) {
                // In some browsers, the SVG-element clientwidth isn't available, so we revert to the parent
                actualWidth = actualCanvas.clientWidth || actualCanvas.parentNode.clientWidth;
            }


            if(actualWidth < canvas[0].clientWidth) {
                widthDiff = actualWidth - canvas[0].clientWidth;
                // In case the canvas is smaller than the actual viewport, the resizers should be moved
                canvasLeft -= widthDiff / 2;
                canvasWidth += widthDiff;
            }

            var iconWidth = 17;
            var iconOffset = 20;

            var north = jQuery('#canvas-grow-N');
            north.css('top', canvasTop + iconOffset + 'px');
            north.css('left', canvasLeft - 10 + (canvasWidth - iconWidth) / 2 + 'px');

            var south = jQuery('#canvas-grow-S');
            south.css('top', (canvasTop + canvasHeight - iconOffset - iconCenterOffset) +  'px');
            south.css('left', canvasLeft - 10 + (canvasWidth - iconWidth) / 2 + 'px');

            var east = jQuery('#canvas-grow-E');
            east.css('top', canvasTop - 10 + (canvasHeight - iconWidth) / 2 + 'px');
            east.css('left', (canvasLeft + canvasWidth - iconOffset - iconCenterOffset) + 'px');

            var west = jQuery('#canvas-grow-W');
            west.css('top', canvasTop -10 + (canvasHeight - iconWidth) / 2 + 'px');
            west.css('left', canvasLeft + iconOffset + 'px');

            north = jQuery('#canvas-shrink-N');
            north.css('top', canvasTop + iconOffset + 'px');
            north.css('left', canvasLeft + 10 + (canvasWidth - iconWidth) / 2 + 'px');

            south = jQuery('#canvas-shrink-S');
            south.css('top', (canvasTop + canvasHeight - iconOffset - iconCenterOffset) +  'px');
            south.css('left', canvasLeft +10 + (canvasWidth - iconWidth) / 2 + 'px');

            east = jQuery('#canvas-shrink-E');
            east.css('top', canvasTop + 10 + (canvasHeight - iconWidth) / 2 +  'px');
            east.css('left', (canvasLeft + canvasWidth - iconOffset - iconCenterOffset) + 'px');

            west = jQuery('#canvas-shrink-W');
            west.css('top', canvasTop + 10 + (canvasHeight - iconWidth) / 2 + 'px');
            west.css('left', canvasLeft + iconOffset + 'px');
        });

        jQuery(window).trigger('resize');

        jQuery.fn.scrollStopped = function(callback) {
            jQuery(this).scroll(function(){
                var self = this, $this = jQuery(self);
                if ($this.data('scrollTimeout')) {
                    clearTimeout($this.data('scrollTimeout'));
                }
                $this.data('scrollTimeout', setTimeout(callback,50,self));
            });
        };

        $rootScope.editorInitialized = true;

    } else {

        // Editor is already defined and in memory, we just need to fetch the model.
        fetchModel();

    }

    $scope.$on('$locationChangeStart', function(event, next, current) {
    	if ($rootScope.editor && !$rootScope.ignoreChanges) {
    		var plugins = $rootScope.editor.loadedPlugins;

    		var savePlugin;
    		for (var i=0; i<plugins.length; i++) {
    			if (plugins[i].type == 'ORYX.Plugins.Save') {
    				savePlugin = plugins[i];
    				break;
    			}
    		}

    		if (savePlugin && savePlugin.hasChanges()) {
    			// Always prevent location from changing. We'll use a popup to determine the action we want to take
    			event.preventDefault();

    			if (!$scope.unsavedChangesModalInstance) {

    				var handleResponseFunction = function (discard) {
    					$scope.unsavedChangesModalInstance = undefined;
    					if (discard) {
    						$rootScope.ignoreChanges = true;
    		                $location.url(next.substring(next.indexOf('/#') + 2));
    					} else {
    		                $rootScope.ignoreChanges = false;
    		                $rootScope.setMainPageById('processes');
    					}
    				};

    				$scope.handleResponseFunction = handleResponseFunction;

                    _internalCreateModal({
    					template: 'editor-app/popups/unsaved-changes.html',
    					scope: $scope
    				},  $modal, $scope);
    			}
    		}
    	}
    });

    // Always needed, cause the DOM element on wich the scroll event listeners are attached are changed for every new model
    initScrollHandling();

}]);

angular.module('activitiModeler')
  .controller('EditorUnsavedChangesPopupCrtl', ['$rootScope', '$scope', '$http', '$location', '$window', function ($rootScope, $scope, $http, $location, $window) {

    $scope.discard = function () {
      if ($scope.handleResponseFunction) {
        $scope.handleResponseFunction(true);
        // Also clear any 'onbeforeunload', added by oryx
        $window.onbeforeunload = undefined;
      }
      $scope.$hide();
    };

    $scope.save = function () {
      if ($scope.handleResponseFunction) {
        $scope.handleResponseFunction(false);
      }
      $scope.$hide();
    };

    $scope.cancel = function () {
      if ($scope.handleResponseFunction) {
        $scope.handleResponseFunction(null);
      }
      $scope.$hide();
    };
  }]);

