/*
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
'use strict';

/**
 * Shared thumbnail service
 */
angular.module('activitiModeler').service('ThumbnailService', [

    '$q',

    function ($q) {

        this.generateThumbnail = function(canvasElementId, modelInfo) {

            var deferred = $q.defer();
            var width = 300;

            html2canvas(jQuery('#' + canvasElementId), {
                onrendered: function (canvas) {

                    var scale = canvas.width / width;

                    var extra_canvas = document.createElement('canvas');
                    extra_canvas.setAttribute('width', width);
                    extra_canvas.setAttribute('height', canvas.height / scale);

                    var ctx = extra_canvas.getContext('2d');
                    ctx.drawImage(canvas, 0, 0, canvas.width, canvas.height, 0, 0, width, canvas.height / scale);

                    var modelImageBase64 = extra_canvas.toDataURL('image/png');
                    if (modelInfo) {
                        modelInfo.modelImageBase64 = modelImageBase64;
                    }

                    deferred.resolve(modelImageBase64);
                }
            });

            return deferred.promise;
        };

    }
]);
