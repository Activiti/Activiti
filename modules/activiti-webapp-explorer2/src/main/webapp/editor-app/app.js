/*
 * Activiti Modeler component part of the Activiti project
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
'use strict';

var activitiModeler = angular.module('activitiModeler', [
  'ngCookies',
  'ngResource',
  'ngSanitize',
  'ngRoute',
  'ngDragDrop',
  'mgcrea.ngStrap', 
  'ngGrid',
  'ngAnimate',
  'pascalprecht.translate',
  'duScroll'
]);

var activitiModule = activitiModeler;

activitiModeler
  // Initialize routes
  .config(['$selectProvider', '$translateProvider', function ($selectProvider, $translateProvider) {

      // Override caret for bs-select directive
      angular.extend($selectProvider.defaults, {
          caretHtml: '&nbsp;<i class="icon icon-caret-down"></i>'
      });
        
        // Initialize angular-translate
        $translateProvider.useStaticFilesLoader({
            prefix: './editor-app/i18n/',
            suffix: '.json'
        });

        $translateProvider.preferredLanguage('en');

        // remember language
        $translateProvider.useCookieStorage();
        
  }])
  .run(['$rootScope', '$timeout', '$modal', '$translate', '$location', '$window', '$http', '$q',
        function($rootScope, $timeout, $modal, $translate, $location, $window, $http, $q) {
	  
			  $rootScope.config = ACTIVITI.CONFIG;
			  
			  $rootScope.editorInitialized = false;
		      
		      $rootScope.editorFactory = $q.defer();
		
		      $rootScope.forceSelectionRefresh = false;
		
		      $rootScope.ignoreChanges = false; // by default never ignore changes
		      
		      $rootScope.validationErrors = [];
		      
		      $rootScope.staticIncludeVersion = Date.now();

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
	  
	  
            /**
             * Initialize the event bus: couple all Oryx events with a dispatch of the
             * event of the event bus. This way, it gets much easier to attach custom logic
             * to any event.
             */

            /* Helper method to fetch model from server (always needed) */
            function fetchModel(modelId) {

                var modelUrl = KISBPM.URL.getModel(modelId);

                $http({method: 'GET', url: modelUrl}).
                    success(function (data, status, headers, config) {
                        $rootScope.editor = new ORYX.Editor(data);
                        $rootScope.modelData = angular.fromJson(data);
                        $rootScope.editorFactory.resolve();
                    }).
                    error(function (data, status, headers, config) {
                      console.log('Error loading model with id ' + modelId + ' ' + data);
                    });
            }


            function initScrollHandling() {
                var canvasSection = jQuery('#canvasSection');
                canvasSection.scroll(function() {

                    // Hides the resizer and quick menu items during scrolling

                    var selectedElements = $rootScope.editor.selection;
                    var subSelectionElements = $rootScope.editor._subSelection;

                    $rootScope.selectedElements = selectedElements;
                    $rootScope.subSelectionElements = subSelectionElements;
                    if (selectedElements && selectedElements.length > 0) {
                    	$rootScope.selectedElementBeforeScrolling = selectedElements[0];
                    }

                    jQuery('.Oryx_button').each(function(i, obj) {
                    	$rootScope.orginalOryxButtonStyle = obj.style.display;
                    	obj.style.display = 'none';
                    });
                    
                    jQuery('.resizer_southeast').each(function(i, obj) {
                    	$rootScope.orginalResizerSEStyle = obj.style.display;
                        obj.style.display = 'none';
                    });
                    jQuery('.resizer_northwest').each(function(i, obj) {
                    	$rootScope.orginalResizerNWStyle = obj.style.display;
                        obj.style.display = 'none';
                    });
                    $rootScope.editor.handleEvents({type:ORYX.CONFIG.EVENT_CANVAS_SCROLL});
                });

                canvasSection.scrollStopped(function(){

                    // Puts the quick menu items and resizer back when scroll is stopped.

                    $rootScope.editor.setSelection([]); // needed cause it checks for element changes and does nothing if the elements are the same
                    $rootScope.editor.setSelection($rootScope.selectedElements, $rootScope.subSelectionElements);
                    $rootScope.selectedElements = undefined;
                    $rootScope.subSelectionElements = undefined;

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
            $rootScope.$on('$includeContentLoaded', function (event) {
	            if (!$rootScope.editorInitialized) {

	            	ORYX._loadPlugins();
	
	                var modelId = EDITOR.UTIL.getParameterByName('modelId');
	                fetchModel(modelId);
	
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
	        	
	        	            $rootScope.selectedElements = selectedElements;
	        	            $rootScope.subSelectionElements = subSelectionElements;
	        	            if (selectedElements && selectedElements.length > 0)
	        	            {
	        	            	$rootScope.selectedElementBeforeScrolling = selectedElements[0];
	        	            	
	        	            	$rootScope.editor.setSelection([]); // needed cause it checks for element changes and does nothing if the elements are the same
	        	                $rootScope.editor.setSelection($rootScope.selectedElements, $rootScope.subSelectionElements);
	        	                $rootScope.selectedElements = undefined;
	        	                $rootScope.subSelectionElements = undefined;
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
	                
	                // Always needed, cause the DOM element on which the scroll event listeners are attached are changed for every new model
	                initScrollHandling();
	                
	                $rootScope.editorInitialized = true;
	            }
            });

            /**
             * Initialize the event bus: couple all Oryx events with a dispatch of the
             * event of the event bus. This way, it gets much easier to attach custom logic
             * to any event.
             */

            $rootScope.editorFactory.promise.then(function() {

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
                
                $rootScope.editor.registerOnEvent(ORYX.CONFIG.EVENT_SHAPEREMOVED, function (event) {
    	    		var validateButton = document.getElementById(event.shape.resourceId + "-validate-button");
    	    		if (validateButton)
    	    		{
    	    			validateButton.style.display = 'none';
    	    		}
                });

                // The Oryx canvas is ready (we know since we're in this promise callback) and the
                // event bus is ready. The editor is now ready for use
                KISBPM.eventBus.dispatch(KISBPM.eventBus.EVENT_TYPE_EDITOR_READY, {type : KISBPM.eventBus.EVENT_TYPE_EDITOR_READY});

            });
            
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
          
        }
  ])

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
