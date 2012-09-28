/**
 * Copyright (c) 2006
 * Martin Czuchra, Nicolas Peters, Daniel Polak, Willi Tscheschner
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 **/

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