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

var ACTIVITI_EDITOR_TOUR = {

    /*
     * General 'getting started' tutorial for the Editor.
     */
    gettingStarted: function($scope, $translate, $q, useLocalStorage) {
        var userName;
        if ($scope.account.firstName) {
            userName = $scope.account.firstName;
        } else {
            userName = $scope.account.fullname;
        }

        $q.all([
            $translate('TOUR.WELCOME-TITLE', {userName: userName}), $translate('TOUR.WELCOME-CONTENT'),
            $translate('TOUR.PALETTE-TITLE'), $translate('TOUR.PALETTE-CONTENT'),
            $translate('TOUR.CANVAS-TITLE'), $translate('TOUR.CANVAS-CONTENT'),
            $translate('TOUR.DRAGDROP-TITLE'), $translate('TOUR.DRAGDROP-CONTENT'),
            $translate('TOUR.PROPERTIES-TITLE'), $translate('TOUR.PROPERTIES-CONTENT'),
            $translate('TOUR.TOOLBAR-TITLE'), $translate('TOUR.TOOLBAR-CONTENT'),
            $translate('TOUR.END-TITLE'), $translate('TOUR.END-CONTENT')
        ]).then(function(translations) {

            // We're using a hack here due to https://github.com/sorich87/bootstrap-tour/issues/85:
            // when clicking next in the tour, it always sets the 'display' css property to 'none'
            // The hack is simple: before the next step is shown, we reset the 'display' property to 'block'

            var tourStepDomElements = ['body', '#paletteHelpWrapper', '#canvasHelpWrapper', '#propertiesHelpWrapper', '#editor-header'];

            var tour = new Tour({
                name: 'activitiEditorTour',
                storage: (useLocalStorage ? window.localStorage : false),
                container: 'body',
                backdrop: true,
                keyboard: true,
                steps: [
                    {
                        orphan: true,
                        title: translations[0],
                        content: translations[1],
                        template: ACTIVITI_EDITOR_TOUR._buildStepTemplate(false, true, false, 300),
                        onNext: ACTIVITI_EDITOR_TOUR._buildOnNextFunction(tourStepDomElements[0])
                    },
                    {
                        element: tourStepDomElements[1],
                        title: translations[2],
                        content: translations[3],
                        template: ACTIVITI_EDITOR_TOUR._buildStepTemplate(false, true, false, 400, 'images/tour/open-group.gif'),
                        onNext: ACTIVITI_EDITOR_TOUR._buildOnNextFunction(tourStepDomElements[1])
                    },
                    {
                        element: tourStepDomElements[2],
                        title: translations[4],
                        content: translations[5],
                        placement: 'left',
                        template: ACTIVITI_EDITOR_TOUR._buildStepTemplate(false, true, false),
                        onNext: ACTIVITI_EDITOR_TOUR._buildOnNextFunction(tourStepDomElements[2])
                    },
                    {
                        orphan: true,
                        title: translations[6],
                        content: translations[7],
                        template: ACTIVITI_EDITOR_TOUR._buildStepTemplate(false, true, false, 720, 'images/tour/tour-dnd.gif'),
                        onNext: ACTIVITI_EDITOR_TOUR._buildOnNextFunction(tourStepDomElements[0])
                    },
                    {
                        element: tourStepDomElements[3],
                        title: translations[8],
                        content: translations[9],
                        placement: 'top',
                        template: ACTIVITI_EDITOR_TOUR._buildStepTemplate(false, true, false, 400),
                        onNext: ACTIVITI_EDITOR_TOUR._buildOnNextFunction(tourStepDomElements[3])
                    },
                    {
                        element: tourStepDomElements[4],
                        title: translations[10],
                        content: translations[11],
                        placement: 'bottom',
                        template: ACTIVITI_EDITOR_TOUR._buildStepTemplate(false, true, false, 400),
                        onNext: ACTIVITI_EDITOR_TOUR._buildOnNextFunction(tourStepDomElements[4])
                    },
                    {
                        orphan: true,
                        title: translations[12],
                        content: translations[13],
                        template: ACTIVITI_EDITOR_TOUR._buildStepTemplate(false, false, true, 400),
                        onNext: ACTIVITI_EDITOR_TOUR._buildOnNextFunction(tourStepDomElements[0])
                    }
                ],

                onEnd: ACTIVITI_EDITOR_TOUR._buildOnEndFunction(tourStepDomElements)
            });

            tour.init();
            tour.start();
        })
    },

    /*
     * Tutorial showing how to use the bendpoint functionality for sequenceflow
     */
    sequenceFlowBendpoint: function($scope, $translate, $q, useLocalStorage) {

        $q.all([
            $translate('FEATURE-TOUR.BENDPOINT.TITLE'), $translate('FEATURE-TOUR.BENDPOINT.DESCRIPTION')
        ]).then(function(translations) {

            // We're using a hack here due to https://github.com/sorich87/bootstrap-tour/issues/85:
            // when clicking next in the tour, it always sets the 'display' css property to 'none'
            // The hack is simple: before the next step is shown, we reset the 'display' property to 'block'

            var tourStepDomElements = ['body'];

            var tour = new Tour({
                name: 'bendpointTour',
                storage: (useLocalStorage ? window.localStorage : false),
                container: 'body',
                backdrop: true,
                keyboard: true,
                steps: [
                    {
                        orphan: true,
                        title: translations[0],
                        content: translations[1],
                        template: ACTIVITI_EDITOR_TOUR._buildStepTemplate(false, false, true, 500, 'images/tour/sequenceflow-bendpoint.gif'),
                        onNext: ACTIVITI_EDITOR_TOUR._buildOnNextFunction(tourStepDomElements[0])
                    }
                ],

                onEnd: ACTIVITI_EDITOR_TOUR._buildOnEndFunction(tourStepDomElements)
            });

            tour.init();
            tour.start();
        })
    },



    _buildStepTemplate : function (addPrevButton, addNextButton, addEndTourButton, optionalForcedWidth, image) {

        var width = 200;
        if (optionalForcedWidth) {
            width = optionalForcedWidth;
        }

        var template =
            '<div class=\'popover tour\' style=\'max-width:' + width + 'px\'>' +
            '<div class=\'arrow\'></div>' +
            '<h3 class=\'popover-title\'></h3>' +
            '<div class=\'popover-content\'></div>' +
            '<div class=\'popover-navigation\'>';
        if (image) {
            template = template + '<div><img src=\'' + image + '\' style=\'border 1px solid black;margin:5px 0 5px 0;\'></img></div>';
        }
        if (addPrevButton) {
            template = template + '<button class=\'btn btn-sm btn-default \' data-role=\'prev\'>« Prev</button>';
        }
        if (addNextButton) {
            template = template + '<button class=\'btn btn-sm btn-default\' data-role=\'next\' style=\'float:right\'">Next »</button>';
        }
        if (addEndTourButton) {
            template = template + '<button class=\'btn btn-warning btn-sm\' data-role=\'end\' style=\'float:right\'">Got it!</button>';
        }

        template = template + '</div>' + '</nav>' + '</div>';
        return template;
    },

    _buildOnNextFunction: function(selector) {
        return function () {
            jQuery(selector).each(function (i, obj) {
                obj.style.display = 'block';
            })
        };
    },

    _buildOnEndFunction: function(selectors) {
        return function () {
            for (var elementsToResetIndex = 0; elementsToResetIndex < selectors.length; elementsToResetIndex++) {
                jQuery(selectors[elementsToResetIndex]).each(function (i, obj) {
                    obj.style.display = 'block';
                });
            }
        }
    }

};
