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


XMLNS = {
	ATOM:	"http://www.w3.org/2005/Atom",
	XHTML:	"http://www.w3.org/1999/xhtml",
	ERDF:	"http://purl.org/NET/erdf/profile",
	RDFS:	"http://www.w3.org/2000/01/rdf-schema#",
	RDF:	"http://www.w3.org/1999/02/22-rdf-syntax-ns#",
	RAZIEL: "http://b3mn.org/Raziel",

	SCHEMA: ""
};

//TODO kann kickstart sich vielleicht auch um die erzeugung von paketen/
// namespaces k�mmern? z.b. requireNamespace("ORYX.Core.SVG");
var Kickstart = {
 	started: false,
	callbacks: [],
	alreadyLoaded: [],
	PATH: '',

	load: function() { Kickstart.kick(); },

	kick: function() {
		//console.profile("loading");
		if(!Kickstart.started) {
			Kickstart.started = true;
			Kickstart.callbacks.each(function(callback){
				// call the registered callback asynchronously.
				window.setTimeout(callback, 1);
			});
		}
	},

	register: function(callback) {
		//TODO Add some mutual exclusion between kick and register calls.
		with(Kickstart) {
			if(started) window.setTimeout(callback, 1);
			else Kickstart.callbacks.push(callback)
		}
	},

	/**
	 * Loads a js, assuring that it has only been downloaded once.
	 * @param {String} url the script to load.
	 */
	require: function(url) {
		// if not already loaded, include it.
		if(Kickstart.alreadyLoaded.member(url))
			return false;
		return Kickstart.include(url);
	},

	/**
	 * Loads a js, regardless of whether it has only been already downloaded.
	 * @param {String} url the script to load.
	 */
	include: function(url) {

		// prepare a script tag and place it in html head.
		var head = document.getElementsByTagNameNS(XMLNS.XHTML, 'head')[0];
		var s = document.createElementNS(XMLNS.XHTML, "script");
		s.setAttributeNS(XMLNS.XHTML, 'type', 'text/javascript');
	   	s.src = Kickstart.PATH + url;

		//TODO macht es sinn, dass neue skript als letztes kind in den head
		// einzubinden (stichwort reihenfolge der skript tags)?
	   	head.appendChild(s);

		// remember this url.
		Kickstart.alreadyLoaded.push(url);

		return true;
	}
}

// register kickstart as the new onload event listener on current window.
// previous listener(s) are triggered to launch with kickstart.
Event.observe(window, 'load', Kickstart.load);