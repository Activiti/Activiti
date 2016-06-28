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