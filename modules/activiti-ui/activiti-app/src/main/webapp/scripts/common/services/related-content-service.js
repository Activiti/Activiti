/*
 * Activiti app component part of the Activiti project
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
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

activitiModule.service('RelatedContentService', ['$http', '$q', '$rootScope', '$translate', '$location', 'Upload', '$timeout',
    function ($http, $q, $rootScope, $translate, $location, Upload, $timeout) {

        var httpAsPromise = function(options) {
            var deferred = $q.defer();
            $http(options).
                success(function (response, status, headers, config) {
                    deferred.resolve(response);
                })
                .error(function (response, status, headers, config) {
                    deferred.reject(response);
                });
            return deferred.promise;
        };

        this.deleteContent = function(contentId, taskId) {
            var promise = httpAsPromise(
                {
                    method: 'DELETE',
                    url: ACTIVITI.CONFIG.contextRoot + '/app/rest/content/' + contentId
                }
            );

            return promise;
        };

        this.addRelatedContent = function(taskId, processInstanceId, file, isIE) {
            var deferred = $q.defer();
            var uploadPromise;
            var url;

            if(taskId) {
                if (isIE) {
                    url = ACTIVITI.CONFIG.contextRoot + '/app/rest/tasks/' + taskId + '/raw-content/text';
                } else {
                    url = ACTIVITI.CONFIG.contextRoot + '/app/rest/tasks/' + taskId + '/raw-content';
                }
                uploadPromise = Upload.upload({
                    url: url,
                    method: 'POST',
                    file: file
                });
                
            } else {
                if (isIE) {
                    url = ACTIVITI.CONFIG.contextRoot + '/app/rest/content/raw/text';
                } else {
                    url = ACTIVITI.CONFIG.contextRoot + '/app/rest/content/raw';
                }
                // Generic piece of content, initially marked as temporary
                uploadPromise = Upload.upload({
                    url: url,
                    method: 'POST',
                    file: file
                });
            }

            var service = this;
            uploadPromise.success(function (response, status, headers, config) {
                if(response && response.id) {
                    service.addUrlToContent(response);
                }
                deferred.resolve(response);
            }).error(function (data, status, headers, config) {
                deferred.reject(data);
            });

            // Add progress to the promise as well, to keep caller
            // notified of progress but still have control over the success-call
            // in order to add the raw URL before resolving promise
            var promise = deferred.promise;
            promise.progress = function(callback) {
                uploadPromise.progress(callback);
                return promise;
            };

            return promise

        };

        this.addRelatedContentFromSource = function(taskId, processInstanceId, source, sourceId, name, link) {
            var url;
            if(taskId) {
                url = ACTIVITI.CONFIG.contextRoot + '/app/rest/tasks/' + taskId + '/content';
            } else {
                url = ACTIVITI.CONFIG.contextRoot + '/app/rest/content';
            }

            // Force a boolean value to be sent in the response body
            link = (link == true);

            var data = {
                source: source,
                sourceId: sourceId,
                name: name,
                link: link
            };

            var service = this;
            var deferred = $q.defer();
            $http(
                {
                    method: 'POST',
                    url: url,
                    data: data
                }
               ).success(function (response, status, headers, config) {
                    if(response && response.id) {
                        service.addUrlToContent(response);
                    }
                    deferred.resolve(response);
                })
                .error(function (response, status, headers, config) {
                        deferred.reject(response);
                });


            var promise = deferred.promise;

            return promise;
        };

        this.getRelatedContent = function(id) {
            var url = ACTIVITI.CONFIG.contextRoot + '/app/rest/content/' + id;

            var service = this;
            var deferred = $q.defer();
            $http(
                {
                    method: 'GET',
                    url: url
                }
            ).success(function (response, status, headers, config) {
                if(response && response.id) {
                    service.addUrlToContent(response);
                }
                deferred.resolve(response);
            })
            .error(function (response, status, headers, config) {
                deferred.reject(response);
            });

            var promise = deferred.promise;
            return promise;
        };

        this.addUrlToContent = function(content) {
            if(content && content.id) {
                content.rawUrl = ACTIVITI.CONFIG.contextRoot + '/app/rest/content/' + content.id + "/raw";

                var fileExtenstion = content.name.split('.').pop();

                if(!content.link && (content.simpleType == 'word' || content.simpleType == 'excel' || content.simpleType == 'powerpoint')) {
                    content.officeUrl = ACTIVITI.CONFIG.contextRoot + '/aos/' + content.id + "/" + content.name;
                }

                if(content.thumbnailStatus == 'created') {
                    content.thumbnailUrl  =  ACTIVITI.CONFIG.contextRoot + '/app/rest/content/' + content.id + "/rendition/thumbnail?noCache=" + new Date().getTime();
                }

                if(content.previewStatus == 'created') {
                    content.pdfUrl =  ACTIVITI.CONFIG.contextRoot + '/app/rest/content/' + content.id + "/rendition/preview?noCache=" + new Date().getTime();
                } else if (content.simpleType === 'image' || fileExtenstion === 'jpg' || fileExtenstion === 'jpeg' || fileExtenstion === 'png') {
                    content.imageUrl = content.rawUrl;
                    content.thumbnailUrl = content.rawUrl;
                } else if(content.simpleType == 'pdf') {
                    content.pdfUrl = content.rawUrl;
                }

            }
        };
    }
]);
