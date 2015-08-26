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

angular.module('activitiApp')
    .controller('DocumentPreviewController', ['$rootScope', '$scope', '$modal', 'appResourceRoot', 'RelatedContentService',
        function ($rootScope, $scope, $modal, appResourceRoot, RelatedContentService) {

            RelatedContentService.addUrlToContent($scope.content);

            $scope.deleteContent = function(content, task) {
                var modalInstance = _internalCreateModal({
                    template: appResourceRoot + 'views/modal/delete-content.html',
                    show: true
                }, $modal, $scope);

                modalInstance.$scope.popup = {
                    content: content,
                    loading: false
                };

                modalInstance.$scope.ok = function() {
                    RelatedContentService.deleteContent(content.id, task && task.id).then(function() {
                        $scope.deleted({ content: content });
                        $scope.content = null;
                    });
                };
            };

        }
    ]
);
