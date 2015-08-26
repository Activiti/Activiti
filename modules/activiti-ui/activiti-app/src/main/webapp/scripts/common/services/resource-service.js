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

// User service
activitiModule.service('ResourceService', ['$http', '$q', 'appResourceRoot',
    function ($http, $q, appResourceRoot) {

        var loadedResources = {};

        function loadStylesheet(relativeUrl, cache)
        {
            var url = appResourceRoot + relativeUrl;
            if (!cache || !loadedResources[url])
            {
                if (cache) {
                    loadedResources[url] = true;
                }
                if (document.createStyleSheet)
                {
                    try
                    {
                        document.createStyleSheet();
                    } catch (e) { }
                }
                else {
                    var link = document.createElement("link");
                    link.rel = "stylesheet";
                    link.type = "text/css";
                    link.media = "all";
                    link.href = url;
                    document.getElementsByTagName("head")[0].appendChild(link);
                }
            }
        }

        function loadScript(relativeUrl, callback, cache)
        {
            var url = appResourceRoot + relativeUrl;
            if (cache && loadedResources[url] && callback)
            {
                callback();
            }
            else
            {
                if (cache) {
                    loadedResources[url] = true;
                }

                // Insert the node so it gets loaded
                var script = document.createElement("script");
                script.type="text/javascript";
                script.src = url;

                if (callback) {
                    var done = false;

                    // Attach handlers for all browsers
                    script.onload = script.onreadystatechange = function()
                    {
                        if (!done && (!this.readyState || this.readyState == "loaded" || this.readyState == "complete"))
                        {
                            done = true;
                            callback();
                        }
                    };
                }
                var el = document.getElementsByTagName("head")[0];
                el.appendChild(script);
            }
        }

        function loadScripts(relativeUrls, callback, cache)
        {
            function loadNext()
            {
                var relativeUrl = relativeUrls.shift();
                if (relativeUrl)
                {
                    loadScript.call(this, relativeUrl, loadNext.bind(this), cache);
                }
                else
                {
                    if (callback)
                    {
                        callback();
                    }
                }
            }
            loadNext.call(this);
        }

        function loadFromHtml(url, callback, cache)
        {
            $http.get(url).success(function(responseText)
            {
                var xmlDoc;
                if (window.DOMParser)
                {
                    var parser = new DOMParser();
                    xmlDoc = parser.parseFromString(responseText, "text/xml");
                }
                else // Internet Explorer
                {
                    xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
                    xmlDoc.async = false;
                    xmlDoc.loadXML(responseText);
                }
                var resources = xmlDoc.getElementsByTagName("link");
                var resourceUrl;
                var resourceUrls = [];
                for (var i = 0, il = resources.length; i < il; i++)
                {
                    resourceUrl = resources[i].getAttribute("href");
                    if (resourceUrl)
                    {
                        loadStylesheet(resourceUrl, cache);
                    }
                }
                resources = xmlDoc.getElementsByTagName("script");
                for (i = 0, il = resources.length; i < il; i++)
                {
                    resourceUrl = resources[i].getAttribute("src");
                    if (resourceUrl)
                    {
                        resourceUrls.push(resourceUrl);
                    }
                }
                loadScripts(resourceUrls, callback, cache);
            });
        }

        this.loadFromHtml = loadFromHtml;
        this.loadScript = loadScript;
        this.loadStylesheet = loadStylesheet;
    }]);
