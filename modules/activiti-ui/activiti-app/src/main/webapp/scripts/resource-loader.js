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
(function(resources){

    if (resources) {

        // Pause angular bootstrap so we have time to register and override angular services/directives etc
        window.name = 'NG_DEFER_BOOTSTRAP!';

        function load(res, node, callback, scope) {
            var resource;
            if (res.tag === 'script') {
                resource = document.createElement('script');
                resource.type = res.type || 'text/javascript';
                resource.src = res.src;

                if (callback) {
                    var done = false;

                    // Attach handlers for all browsers
                    resource.onload = resource.onreadystatechange = function()
                    {
                        if (!done && (!this.readyState || this.readyState == "loaded" || this.readyState == "complete"))
                        {
                            done = true;
                            callback.call(scope ? scope : this, res);
                        }
                    };
                }
            }
            else if (res.tag === 'link') {
                resource = document.createElement('link');
                resource.rel = res.rel || 'stylesheet';
                resource.href = res.href;
            }

            if (node.nextSibling) {
                node.parentNode.insertBefore(resource, node.nextSibling);
            }
            else {
                node.parentNode.appendChild(resource);
            }

            if (res.tag === 'link' && callback) {
                callback.call(scope ? scope : this, res);
            }
        }

        function getResourceLoaderElement() {
            var scripts = document.getElementsByTagName('script');
            for (var i = 0, il = scripts.length; i < il; i++) {
                if (scripts[i].src.indexOf('scripts/resource-loader.js') != -1) {
                    return scripts[i];
                }
            }
            return null;
        }

        var res = resources['*'];
        var resourceLoaderElement = getResourceLoaderElement();
        var appName = resourceLoaderElement.getAttribute('app');
        if (resources.hasOwnProperty(appName)) {
            res = resources[appName];
        }

        var loadedResources = 0;
        for (var i = 0, il = res.length; i < il; i++) {
            load(res[i], resourceLoaderElement, function(){
                loadedResources++;
                if (loadedResources == res.length) {
                    // Let angular resume bootstrap
                    var interval = window.setInterval(function(){
                        if (angular && typeof angular.resumeBootstrap == 'function') {
                            angular.resumeBootstrap();
                            window.clearInterval(interval);
                        }
                    }, 20);

                }
            });
        }
    }

})(ACTIVITI.CONFIG.resources);