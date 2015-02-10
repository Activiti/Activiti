/**
 * @namespace Oryx name space for different utility methods
 * @name ORYX.Utils
*/

if(!ORYX) var ORYX = {};

ORYX.Utils = {
    /**
     * General helper method for parsing a param out of current location url
     * @example
     * // Current url in Browser => "http://oryx.org?param=value"
     * ORYX.Utils.getParamFromUrl("param") // => "value" 
     * @param {Object} name
     */
    getParamFromUrl: function(name){
        name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
        var regexS = "[\\?&]" + name + "=([^&#]*)";
        var regex = new RegExp(regexS);
        var results = regex.exec(window.location.href);
        if (results == null) {
            return null;
        }
        else {
            return results[1];
        }
    },
	
	adjustLightness: function(){
		return arguments[0];	
	},
	
	adjustGradient: function(gradient, reference){
		
		if (ORYX.CONFIG.DISABLE_GRADIENT && gradient){
		
			var col = reference.getAttributeNS(null, "stop-color") || "#ffffff";
			
			$A(gradient.getElementsByTagName("stop")).each(function(stop){
				if (stop == reference){ return; }
				stop.setAttributeNS(null, "stop-color", col);
			});
		}
	}
}
/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
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
// namespaces k???mmern? z.b. requireNamespace("ORYX.Core.SVG");
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
Event.observe(window, 'load', Kickstart.load);/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

var ERDF = {

	LITERAL: 0x01,
	RESOURCE: 0x02,
	DELIMITERS: ['.', '-'],
	HASH: '#',
	HYPHEN: "-",

	schemas: [],
	callback: undefined,
	log: undefined,

	init: function(callback) {
		
		// init logging.
		//ERDF.log = Log4js.getLogger("oryx");
		//ERDF.log.setLevel(Log4js.Level.ALL);
		//ERDF.log.addAppender(new ConsoleAppender(ERDF.log, false));

		//if(ERDF.log.isTraceEnabled())
		//	ERDF.log.trace("ERDF Parser is initialized.");

		// register callbacks and default schemas.
		ERDF.callback = callback;
		ERDF.registerSchema('schema', XMLNS.SCHEMA);
		ERDF.registerSchema('rdfs', XMLNS.RDFS);
	},

	run: function() {

		//if(ERDF.log.isTraceEnabled())
		//	ERDF.log.trace("ERDF Parser is running.");

		// do the work.
		return ERDF._checkProfile() && ERDF.parse();
	},
	
	parse: function() {
		
		//(ERDF.log.isDebugEnabled())
		//	ERDF.log.debug("Begin parsing document metadata.");
		
		// time measuring
		ERDF.__startTime = new Date();

		var bodies = document.getElementsByTagNameNS(XMLNS.XHTML, 'body');
		var subject = {type: ERDF.RESOURCE, value: ''};

		var result = ERDF._parseDocumentMetadata() &&
			ERDF._parseFromTag(bodies[0], subject);
			
		// time measuring
		ERDF.__stopTime = new Date();

		var duration = (ERDF.__stopTime - ERDF.__startTime)/1000.;
		//alert('ERDF parsing took ' + duration + ' s.');
		
		return result;
	},
	
	_parseDocumentMetadata: function() {

		// get links from head element.
		var heads = document.getElementsByTagNameNS(XMLNS.XHTML, 'head');
		var links = heads[0].getElementsByTagNameNS(XMLNS.XHTML, 'link');
		var metas = heads[0].getElementsByTagNameNS(XMLNS.XHTML, 'meta');

		// process links first, since they could contain schema definitions.
		$A(links).each(function(link) {
			var properties = link.getAttribute('rel');
			var reversedProperties = link.getAttribute('rev');
			var value = link.getAttribute('href');
			
			ERDF._parseTriplesFrom(
				ERDF.RESOURCE, '',
				properties,
				ERDF.RESOURCE, value);
				
			ERDF._parseTriplesFrom(
				ERDF.RESOURCE, value,
				reversedProperties,
				ERDF.RESOURCE, '');
		});

		// continue with metas.
		$A(metas).each(function(meta) {
			var property = meta.getAttribute('name');
			var value = meta.getAttribute('content');
			
			ERDF._parseTriplesFrom(
				ERDF.RESOURCE, '',
				property,
				ERDF.LITERAL, value);
		});

		return true;
	},
	
	_parseFromTag: function(node, subject, depth) {
		
		// avoid parsing non-xhtml content.
		if(!node || !node.namespaceURI || node.namespaceURI != XMLNS.XHTML) { return; }
		
		// housekeeping.
		if(!depth) depth=0;
		var id = node.getAttribute('id');

		// some logging.
		//if(ERDF.log.isTraceEnabled())
		//	ERDF.log.trace(">".times(depth) + " Parsing " + node.nodeName + " ("+node.nodeType+") for data on " +
		//		((subject.type == ERDF.RESOURCE) ? ('&lt;' + subject.value + '&gt;') : '') +
		//		((subject.type == ERDF.LITERAL) ? '"' + subject.value + '"' : ''));
		
		/* triple finding! */
		
		// in a-tags...
		if(node.nodeName.endsWith(':a') || node.nodeName == 'a') {
			var properties = node.getAttribute('rel');
			var reversedProperties = node.getAttribute('rev');
			var value = node.getAttribute('href');
			var title = node.getAttribute('title');
			var content = node.textContent;

			// rel triples
			ERDF._parseTriplesFrom(
				subject.type, subject.value,
				properties,
				ERDF.RESOURCE, value,
				function(triple) {
					var label = title? title : content;
					
					// label triples
					ERDF._parseTriplesFrom(
						triple.object.type, triple.object.value,
						'rdfs.label',
						ERDF.LITERAL, label);
				});

			// rev triples
			ERDF._parseTriplesFrom(
				subject.type, subject.value,
				reversedProperties,
				ERDF.RESOURCE, '');
				
			// type triples
			ERDF._parseTypeTriplesFrom(
				subject.type, subject.value,
				properties);

		// in img-tags...
		} else if(node.nodeName.endsWith(':img') || node.nodeName == 'img') {
			var properties = node.getAttribute('class');
			var value = node.getAttribute('src');
			var alt = node.getAttribute('alt');

			ERDF._parseTriplesFrom(
				subject.type, subject.value,
				properties,
				ERDF.RESOURCE, value,
				function(triple) {
					var label = alt;
					
					// label triples
					ERDF._parseTriplesFrom(
						triple.object.type, triple.object.value,
						'rdfs.label',
						ERDF.LITERAL, label);
				});

		}
		
		// in every tag
		var properties = node.getAttribute('class');
		var title = node.getAttribute('title');
		var content = node.textContent;
		var label = title ? title : content;
		
		// regular triples
		ERDF._parseTriplesFrom(
			subject.type, subject.value,
			properties,
			ERDF.LITERAL, label);

		if(id) subject = {type: ERDF.RESOURCE, value: ERDF.HASH+id};
		
		// type triples
		ERDF._parseTypeTriplesFrom(
			subject.type, subject.value,
			properties);

		// parse all children that are element nodes.
		var children = node.childNodes;
		if(children) $A(children).each(function(_node) {
			if(_node.nodeType == _node.ELEMENT_NODE)
				ERDF._parseFromTag(_node, subject, depth+1); });
	},
	
	_parseTriplesFrom: function(subjectType, subject, properties,
		objectType, object, callback) {
		
		if(!properties) return;
		properties.toLowerCase().split(' ').each( function(property) {
			
			//if(ERDF.log.isTraceEnabled())
			//	ERDF.log.trace("Going for property " + property);

			var schema = ERDF.schemas.find( function(schema) {
				return false || ERDF.DELIMITERS.find( function(delimiter) {
					return property.startsWith(schema.prefix + delimiter);
				});
			});
			
			if(schema && object) {
				property = property.substring(
					schema.prefix.length+1, property.length);
				var triple = ERDF.registerTriple(
					new ERDF.Resource(subject),
					{prefix: schema.prefix, name: property},
					(objectType == ERDF.RESOURCE) ?
						new ERDF.Resource(object) :
						new ERDF.Literal(object));
						
				if(callback) callback(triple);
			}
		});
	},
	
	_parseTypeTriplesFrom: function(subjectType, subject, properties, callback) {
		
		if(!properties) return;
		properties.toLowerCase().split(' ').each( function(property) {
			
			//if(ERDF.log.isTraceEnabled())
			//	ERDF.log.trace("Going for property " + property);
				
			var schema = ERDF.schemas.find( function(schema) {
				return false || ERDF.DELIMITERS.find( function(delimiter) {
					return property.startsWith(ERDF.HYPHEN + schema.prefix + delimiter);
				});
			});
			
			if(schema && subject) {
				property = property.substring(schema.prefix.length+2, property.length);
				var triple = ERDF.registerTriple(
					(subjectType == ERDF.RESOURCE) ?
						new ERDF.Resource(subject) :
						new ERDF.Literal(subject),
					{prefix: 'rdf', name: 'type'},
					new ERDF.Resource(schema.namespace+property));
				if(callback) callback(triple);
			}
		});
	},
	
	/**
	 * Checks for ERDF profile declaration in head of document.
	 */
	_checkProfile: function() {

		// get profiles from head element.
		var heads = document.getElementsByTagNameNS(XMLNS.XHTML, 'head');
		var profiles = heads[0].getAttribute("profile");
		var found = false;

		// if erdf profile is contained.
		if(profiles && profiles.split(" ").member(XMLNS.ERDF)) {

			// pass check.
			//if(ERDF.log.isTraceEnabled())
			//	ERDF.log.trace("Found ERDF profile " + XMLNS.ERDF);
			return true;
			
		} else {
		
			// otherwise fail check.
			//if(ERDF.log.isFatalEnabled())
			//	ERDF.log.fatal("No ERDF profile found.");
			return false;
		}
	},
	
	__stripHashes: function(s) {
		return (s && (typeof s.substring == 'function') && s.substring(0, 1)=='#') ? s.substring(1, s.length) : s;
	},
	
	registerSchema: function(prefix, namespace) {
		
		// TODO check whether already registered, if so, complain.
		ERDF.schemas.push({
			prefix: prefix,
			namespace: namespace
		});
		
		//if(ERDF.log.isDebugEnabled())
		//	ERDF.log.debug("Prefix '"+prefix+"' for '"+namespace+"' registered.");
	},
	
	registerTriple: function(subject, predicate, object) {
		
		// if prefix is schema, this is a schema definition.
		if(predicate.prefix.toLowerCase() == 'schema')
			this.registerSchema(predicate.name, object.value);
			
		var triple = new ERDF.Triple(subject, predicate, object);
		ERDF.callback(triple);
		
		//if(ERDF.log.isInfoEnabled())
		//	ERDF.log.info(triple)
		
		// return the registered triple.
		return triple;
	},
	
	__enhanceObject: function() {
		
		/* Resource state querying methods */
		this.isResource = function() {
			return this.type == ERDF.RESOURCE };
		this.isLocal = function() {
			return this.isResource() && this.value.startsWith('#') };
		this.isCurrentDocument = function() {
			return this.isResource() && (this.value == '') };
		
		/* Resource getter methods.*/
		this.getId = function() {
			return this.isLocal() ? ERDF.__stripHashes(this.value) : false; };

		/* Liiteral state querying methods  */
		this.isLiteral = function() {
			return this.type == ERDF.LIITERAL };
	},
	
	serialize: function(literal) {
		
		if(!literal){
			return "";
		}else if(literal.constructor == String) {
			return literal;
		} else if(literal.constructor == Boolean) {
			return literal? 'true':'false';
		} else {
			return literal.toString();
		}
	}
};


ERDF.Triple = function(subject, predicate, object) {
	
	this.subject = subject;
	this.predicate = predicate;
	this.object = object;
	
	this.toString = function() {
		
		return "[ERDF.Triple] " +
			this.subject.toString() + ' ' +
			this.predicate.prefix + ':' + this.predicate.name + ' ' +
			this.object.toString();
		};
};

ERDF.Resource = function(uri) {
	
	this.type = ERDF.RESOURCE;
	this.value = uri;
	ERDF.__enhanceObject.apply(this);
	
	this.toString = function() {
		return '&lt;' + this.value + '&gt;';
	}
	
};

ERDF.Literal = function(literal) {
	
	this.type = ERDF.LITERAL;
	this.value = ERDF.serialize(literal);
	ERDF.__enhanceObject.apply(this);

	this.toString = function() {
		return '"' + this.value + '"';
	}
};/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

/*
 * Save and triple generation behaviour. Use this area to configure
 * data management to your needs.
 */
var USE_ASYNCHRONOUS_REQUESTS =		true;
var DISCARD_UNUSED_TRIPLES =			true;
var PREFER_SPANS_OVER_DIVS =			true;
var PREFER_TITLE_OVER_TEXTNODE =		false;
var RESOURCE_ID_PREFIX =				'resource';

var SHOW_DEBUG_ALERTS_WHEN_SAVING =	false;
var SHOW_EXTENDED_DEBUG_INFORMATION =	false;

/*
 * Back end specific workarounds.
 */

var USE_ARESS_WORKAROUNDS =		true;

/*
 * Data management constants. Do not change these, as they are used
 * both internally and externally to communicate on events and to identify
 * command object actions in triple production and embedding rules.
 */

// Resource constants
var RESOURCE_CREATED =			0x01;
var RESOURCE_REMOVED =			0x02;
var RESOURCE_SAVED =				0x04;
var RESOURCE_RELOADED =			0x08;
var RESOURCE_SYNCHRONIZED = 		0x10;

// Triple constants
var TRIPLE_REMOVE =	0x01;
var TRIPLE_ADD =		0x02;
var TRIPLE_RELOAD =	0x04;
var TRIPLE_SAVE =		0x08;

var PROCESSDATA_REF = 'processdata';

// HTTP status code constants
//
//// 2xx
//const 200_OK =			'Ok';
//const 201_CREATED =		'Created';
//const 202_ACCEPTED =		'Accepted';
//const 204_NO_CONTENT =	'No Content';
//
//// 3xx
//const 301_MOVED_PERMANENTLY =	'Moved Permanently';
//const 302_MOVED_TEMPORARILY =	'Moved Temporarily';
//const 304_NOT_MODIFIED =		'Not Modified';
//
//// 4xx
//const 400_BAD_REQUEST =	'Bad Request';
//const 401_UNAUTHORIZED =	'Unauthorized';
//const 403_FORBIDDEN =		'Forbidden';
//const 404_NOT_FOUND =		'Not Found';
//const 409_CONFLICT =		'Conflict';
//
//// 5xx
//const 500_INTERNAL_SERVER_ERROR =		'Internal Server Error';
//const 501_NOT_IMPLEMENTED =			'Not Implemented';
//const 502_BAD_GATEWAY =				'Bad Gateway';
//const 503_SERVICE_UNAVAILABLE =		'Service Unavailable';
//
/**
 * The Data Management object. Use this one when interacting with page internal
 * data. Initialize data management by DataManager.init();
 * @class DataManager
 */
var DataManager = {
	
	/**
	 * The init method should be called once in the DataManagers lifetime.
	 * It causes the DataManager to initialize itself, the erdf parser, do all
	 * neccessary registrations and configurations, to run the parser and
	 * from then on deliver all resulting triples.
	 * No parameters needed are needed in a call to this method.
	 */
	init: function() {
		ERDF.init(DataManager._registerTriple);
		DataManager.__synclocal();
	},
	
	/**
	 * This triple array is meant to be the whole knowledge of the DataManager.
	 */
	_triples: [],
	
	/**
	 * This method is meant for callback from erdf parsing. It is not to be
	 * used in another way than to add triples to the triple store.
	 * @param {Object} triple the triple to add to the triple store.
	 */
	_registerTriple: function(triple) {
		DataManager._triples.push(triple)
	},
	
	/**
	 * The __synclocal method is for internal usage only.
	 * It performs synchronization with the local document, that is, the triple
	 * store is adjustet to the content of the document, which could have been
	 * changed by any other applications running on the same page.
	 */
	__synclocal: function() {
		DataManager._triples = [];
		ERDF.run();
	},
	
	/**
	 * Makes the shape passed into this method synchronize itself with the DOM.
	 * This method returns the shapes resource object for further manipulation.
	 * @param {Object} shape
	 */
	__synchronizeShape: function(shape) {

		var r = ResourceManager.getResource(shape.resourceId);
		var serialize = shape.serialize();

		// store all serialize values
		serialize.each( function(ser) {
			
			var resource = (ser.type == 'resource');
			var _triple = new ERDF.Triple(
				new ERDF.Resource(shape.resourceId),
				{prefix: ser.prefix, name: ser.name},
				resource ?
					new ERDF.Resource(ser.value) :
					new ERDF.Literal(ser.value)
			);
			DataManager.setObject(_triple);
		});
		
		return r;
	},

	__storeShape: function(shape) {
		
		// first synchronize the shape,
		var resource = DataManager.__synchronizeShape(shape);
		
		// then save the synchronized dom.
		resource.save();
	},
		
	__forceExistance: function(shape) {
		
		if(!$(shape.resourceId)) {
			
			if(!$$('.' + PROCESSDATA_REF)[0])
				DataManager.graft(XMLNS.XHTML,
					document.getElementsByTagNameNS(XMLNS.XHTML, 'body').item(0), ['div', {'class': PROCESSDATA_REF, 'style':'display:none;'}]);
				
			// object is literal
			DataManager.graft(XMLNS.XHTML,
				$$('.' + PROCESSDATA_REF)[0], [
				
				'div', {
                    'id': shape.resourceId,
                    //This should be done in a more dynamic way!!!!!
                    'class': (shape instanceof ORYX.Core.Canvas) ? "-oryx-canvas" : undefined
                }
			]);
			
		} else {
			var resource = $(shape.resourceId)
			var children = $A(resource.childNodes)
			children.each( function(child) {
				resource.removeChild(child);
			});
		};
	},
	
	__persistShape: function(shape) {

		// a shape serialization.
		var shapeData = shape.serialize();
		
		// initialize a triple array and construct a shape resource
		// to be used in triple generation.
		var triplesArray = [];
		var shapeResource = new ERDF.Resource(shape.resourceId);

		// remove all triples for this particular shape's resource
		DataManager.removeTriples( DataManager.query(
			shapeResource, undefined, undefined));

		// for each data set in the shape's serialization
		shapeData.each( function(data) {

			// construct a triple's value
			var value = (data.type == 'resource') ?
				new ERDF.Resource(data.value) :
				new ERDF.Literal(data.value);

			// construct triple and add it to the DOM.
			DataManager.addTriple( new ERDF.Triple(
				shapeResource,
				{prefix: data.prefix, name: data.name},
				value
			));
		});
	},
	
	__persistDOM: function(facade) {

		// getChildShapes gets all shapes (nodes AND edges), deep flag
		// makes it return a flattened child hierarchy.
		
		var canvas = facade.getCanvas();
		var shapes = canvas.getChildShapes(true);
		var result = '';
		
		// persist all shapes.
		shapes.each( function(shape) {
			DataManager.__forceExistance(shape);
		});
		//DataManager.__synclocal();
		
		DataManager.__renderCanvas(facade);
		result += DataManager.serialize(
				$(ERDF.__stripHashes(facade.getCanvas().resourceId)), true);
				
		shapes.each( function(shape) {
			
			DataManager.__persistShape(shape);
			result += DataManager.serialize(
				$(ERDF.__stripHashes(shape.resourceId)), true);
		});
		
		//result += DataManager.__renderCanvas(facade);
		
		return result;
	},

	__renderCanvas: function(facade) {

		var canvas = facade.getCanvas();
		var stencilSets = facade.getStencilSets();
		var shapes = canvas.getChildShapes(true);
		
		DataManager.__forceExistance(canvas);
		
		DataManager.__persistShape(canvas);
		
		var shapeResource = new ERDF.Resource(canvas.resourceId);

		// remove all triples for this particular shape's resource
		DataManager.removeTriples( DataManager.query(
			shapeResource, undefined, undefined));

		DataManager.addTriple( new ERDF.Triple(
			shapeResource,
			{prefix: "oryx", name: "mode"},
			new ERDF.Literal("writable")
		));

		DataManager.addTriple( new ERDF.Triple(
			shapeResource,
			{prefix: "oryx", name: "mode"},
			new ERDF.Literal("fullscreen")
		));

		stencilSets.values().each(function(stencilset) {
			DataManager.addTriple( new ERDF.Triple(
				shapeResource,
				{prefix: "oryx", name: "stencilset"},
				new ERDF.Resource(stencilset.source().replace(/&/g, "%26"))
			));
			
			DataManager.addTriple( new ERDF.Triple(
				shapeResource,
				{prefix: "oryx", name: "ssnamespace"},
				new ERDF.Resource(stencilset.namespace())
			));
			
			stencilset.extensions().keys().each(function(extension) {
				DataManager.addTriple( new ERDF.Triple(
					shapeResource,
					{prefix: "oryx", name: "ssextension"},
					new ERDF.Literal(extension)
				));
			});
		});
						
		shapes.each(function(shape) {
			DataManager.addTriple( new ERDF.Triple(
				shapeResource,
				{prefix: "oryx", name: "render"},
				new ERDF.Resource("#" + shape.resourceId)
			));
		});
	},

	__counter: 0,
	__provideId: function() {
		
		while($(RESOURCE_ID_PREFIX+DataManager.__counter))
			DataManager.__counter++;
			
		return RESOURCE_ID_PREFIX+DataManager.__counter;
	},
		
	serializeDOM: function(facade) {
		
		return DataManager.__persistDOM(facade);
	},
	
	syncGlobal: function(facade) {
		
		return DataManager.__syncglobal(facade);
	},
	
	/**
	 * This method is used to synchronize local DOM with remote resources.
	 * Local changes are commited to the server, and remote changes are
	 * performed to the local document.
	 * @param {Object} facade The facade of the editor that holds certain
	 * resource representations as shapes.
	 */
	__syncglobal: function(facade) {

		// getChildShapes gets all shapes (nodes AND edges), deep flag
		// makes it return a flattened child hierarchy.
		
		var canvas = facade.getCanvas();
		var shapes = canvas.getChildShapes(true);

		// create dummy resource representations in the dom
		// for all shapes that were newly created.

		shapes.select( function(shape) {

			// select shapes without resource id.

			return !($(shape.resourceId));

		}).each( function(shape) {

			// create new resources for them.
			if(USE_ARESS_WORKAROUNDS) {
				
				/*
				 * This is a workaround due to a bug in aress. Resources are
				 * ignoring changes to raziel:type property once they are
				 * created. As long as this is not fixed, the resource is now
				 * being created using a randomly guessed id, this temporary id
				 * is then used in references and the appropriate div is being
				 * populated with properties.
				 * 
				 * AFTER THIS PHASE THE DATA IS INCONSISTENT AS REFERENCES POINT
				 * TO IDS THAT ARE UNKNOWN TO THE BACK END.
				 * 
				 * After the resource is actually created in aress, it gets an id
				 * that is persistent. All shapes are then being populated with the
				 * correct id references and stored on the server.
				 * 
				 * AFTER THE SAVE PROCESS HAS RETURNED, THE DATA IS CONSISTENT
				 * REGARDING THE ID REFERENCES AGAIN.
				 */
				
				var razielType = shape.properties['raziel-type'];
				
				var div = '<div xmlns="http://www.w3.org/1999/xhtml">' +
					'<span class="raziel-type">'+razielType+'</span></div>';

				var r = ResourceManager.__createResource(div);
				shape.resourceId = r.id();
				
			} else {
		
				var r = ResourceManager.__createResource();
				shape.resourceId = r.id();
			}

		});

		shapes.each( function(shape) {
			
			// store all shapes.
			DataManager.__storeShape(shape);
		});
	},
	
	/**
	 * This method serializes a single div into a string that satisfies the
	 * client/server communication protocol. It ingnores all elements that have
	 * an attribute named class that includes 'transient'.
	 * @param {Object} node the element to serialize.
	 * @param {Object} preserveNamespace whether to preserve the parent's
	 *                 namespace. If you are not sure about namespaces, provide
	 *                 just the element to be serialized.
	 */
	serialize: function(node, preserveNamespace) {

		if (node.nodeType == node.ELEMENT_NODE) {
			// serialize an element node.
			
			var children = $A(node.childNodes);
			var attributes = $A(node.attributes);
			var clazz = new String(node.getAttribute('class'));
			var ignore = clazz.split(' ').member('transient');

			// ignore transients.

			if(ignore)
				return '';

			// start serialization.
			
			var result = '<' + node.nodeName;
			
			// preserve namespace?
			if(!preserveNamespace) 
				result += ' xmlns="' + (node.namespaceURI ? node.namespaceURI : XMLNS.XHTML) + '" xmlns:oryx="http://oryx-editor.org"';
			
			// add all attributes.
			
			attributes.each(function(attribute) {
				result += ' ' + attribute.nodeName + '="' +
					attribute.nodeValue + '"';});
			
			// close if no children.
			
			if(children.length == 0)
				result += '/>';
				
			else {
				
				// serialize all children.
				
				result += '>';
				children.each(function(_node) {
					result += DataManager.serialize(_node, true)});
				result += '</' + node.nodeName + '>'
			}

			return result;
			
		} else if (node.nodeType == node.TEXT_NODE) {
			
			// serialize a text node.
			return  node.nodeValue;
		}
		
		//TODO serialize cdata areas also.
		//TODO work on namespace awareness.
	},

	addTriple: function(triple) {

		// assert the subject is a resource
		
		if(!triple.subject.type == ERDF.LITERAL)
			throw 'Cannot add the triple ' + triple.toString() +
				' because the subject is not a resource.'
		
		// get the element which represents this triple's subject.
		var elementId = ERDF.__stripHashes(triple.subject.value);
		var element = $(elementId);
				
		// assert the subject is inside this document.
		if(!element)
			throw 'Cannot add the triple ' + triple.toString() +
				' because the subject "'+elementId+'" is not in the document.';

		if(triple.object.type == ERDF.LITERAL)

			// object is literal
			DataManager.graft(XMLNS.XHTML, element, [
				'span', {'class': (triple.predicate.prefix + "-" +
					triple.predicate.name)}, triple.object.value.escapeHTML()
			]);
			
		else {

			// object is resource
			DataManager.graft(XMLNS.XHTML, element, [
				'a', {'rel': (triple.predicate.prefix + "-" +
					triple.predicate.name), 'href': triple.object.value}
			]);
			
		}

		return true;
	},
	
	removeTriples: function(triples) {

		// alert('Removing ' +triples.length+' triples.');

		// from all the triples select those ...
		var removed = triples.select(

			function(triple) {
				
				// TODO remove also from triple store.
				// ... that were actually removed.
				return DataManager.__removeTriple(triple);
			});
		
		// sync and return removed triples.
		// DataManager.__synclocal();
		return removed;
	},
	
	removeTriple: function(triple) {
		
		// remember whether the triple was actually removed.
		var result = DataManager.__removeTriple(triple);

		// sync and return removed triples.
		// DataManager.__synclocal();
		return result;
	},

	__removeTriple: function(triple) {
		
		// assert the subject is a resource
		if(!triple.subject.type == ERDF.LITERAL)
		
			throw 'Cannot remove the triple ' + triple.toString() +
				' because the subject is not a resource.';

		// get the element which represents this triple's subject.
		var elementId = ERDF.__stripHashes(triple.subject.value);
		var element = $(elementId);

		// assert the subject is inside this document.
		if(!element)
		
			throw 'Cannot remove the triple ' + triple.toString() +
				' because the subject is not in the document.';
	  
		if(triple.object.type == ERDF.LITERAL) {
	  
  			// continue searching actively for the triple.
			var result = DataManager.__removeTripleRecursively(triple, element);
			return result;
		}
	},

	__removeTripleRecursively: function(triple, continueFrom) {  

		// return when this node is not an element node.
		if(continueFrom.nodeType != continueFrom.ELEMENT_NODE)
			return false;
		
		var classes = new String(continueFrom.getAttribute('class'));
		var children = $A(continueFrom.childNodes);
		
		if(classes.include(triple.predicate.prefix + '-' + triple.predicate.name)) {
		  
			var content = continueFrom.textContent;
			if(	(triple.object.type == ERDF.LITERAL) &&
				(triple.object.value == content))

				continueFrom.parentNode.removeChild(continueFrom);
			
			return true;
		  
		} else {
		 
			children.each(function(_node) {
			DataManager.__removeTripleRecursively(triple, _node)});
			return false;
		}

	},

	/**
	 * graft() function
	 * Originally by Sean M. Burke from interglacial.com, altered for usage with
	 * SVG and namespace (xmlns) support. Be sure you understand xmlns before
	 * using this funtion, as it creates all grafted elements in the xmlns
	 * provided by you and all element's attribures in default xmlns. If you
	 * need to graft elements in a certain xmlns and wish to assign attributes
	 * in both that and another xmlns, you will need to do stepwise grafting,
	 * adding non-default attributes yourself or you'll have to enhance this
	 * function. Latter, I would appreciate: martin???apfelfabrik.de
	 * @param {Object} namespace The namespace in which
	 * 					elements should be grafted.
	 * @param {Object} parent The element that should contain the grafted
	 * 					structure after the function returned.
	 * @param {Object} t the crafting structure.
	 * @param {Object} doc the document in which grafting is performed.
	 */
	graft: function(namespace, parent, t, doc) {
		
	    doc = (doc || (parent && parent.ownerDocument) || document);
	    var e;
	    if(t === undefined) {
	        echo( "Can't graft an undefined value");
	    } else if(t.constructor == String) {
	        e = doc.createTextNode( t );
	    } else {
	        for(var i = 0; i < t.length; i++) {
	            if( i === 0 && t[i].constructor == String ) {
					var snared = t[i].match( /^([a-z][a-z0-9]*)\.([^\s\.]+)$/i );
	                if( snared ) {
	                    e = doc.createElementNS(namespace, snared[1]);
	                    e.setAttributeNS(null, 'class', snared[2] );
	                    continue;
	                }
	                snared = t[i].match( /^([a-z][a-z0-9]*)$/i );
	                if( snared ) {
	                    e = doc.createElementNS(namespace, snared[1]);  // but no class
	                    continue;
	                }
	
	                // Otherwise:
	                e = doc.createElementNS(namespace, "span");
	                e.setAttribute(null, "class", "namelessFromLOL" );
	            }
	
	            if( t[i] === undefined ) {
	                echo("Can't graft an undefined value in a list!");
	            } else if( t[i].constructor == String || t[i].constructor == Array) {
	                this.graft(namespace, e, t[i], doc );
	            } else if(  t[i].constructor == Number ) {
	                this.graft(namespace, e, t[i].toString(), doc );
	            } else if(  t[i].constructor == Object ) {
	                // hash's properties => element's attributes
	                for(var k in t[i]) { e.setAttributeNS(null, k, t[i][k] ); }
	            } else if(  t[i].constructor == Boolean ) {
	                this.graft(namespace, e, t[i] ? 'true' : 'false', doc );
				} else
					throw "Object " + t[i] + " is inscrutable as an graft arglet.";
	        }
	    }
		
		if(parent) parent.appendChild(e);
	
	    return Element.extend(e); // return the topmost created node
	},

	setObject: function(triple) {

		/**
		 * Erwartungen von Arvid an diese Funktion:
		 * - Es existiert genau ein triple mit dem Subjekt und Praedikat,
		 *   das uebergeben wurde, und dieses haelt uebergebenes Objekt.
		 */

		var triples = DataManager.query(
			triple.subject,
			triple.predicate,
			undefined
		);
		
		DataManager.removeTriples(triples);

		DataManager.addTriple(triple);

		return true;
	},
	
	query: function(subject, predicate, object) {

		/*
		 * Typical triple.
		 *	{value: subject, type: subjectType},
		 *	{prefix: schema.prefix, name: property},
		 *	{value: object, type: objectType});
		 */	
		 	
		return DataManager._triples.select(function(triple) {
			
			var select = ((subject) ?
				(triple.subject.type == subject.type) &&
				(triple.subject.value == subject.value) : true);
			if(predicate) {
				select = select && ((predicate.prefix) ?
					(triple.predicate.prefix == predicate.prefix) : true);
				select = select && ((predicate.name) ?
					(triple.predicate.name == predicate.name) : true);
			}
			select = select && ((object) ?
				(triple.object.type == object.type) &&
				(triple.object.value == object.value) : true);
			return select;
		});
	}
}

Kickstart.register(DataManager.init);

function assert(expr, m) { if(!expr) throw m; };

function DMCommand(action, triple) {
	
	// store action and triple.
	this.action = action;
	this.triple = triple;
	
	this.toString = function() {
		return 'Command('+action+', '+triple+')';
	};
}

function DMCommandHandler(nextHandler) {
	
	/**
	 * Private method to set the next handler in the Chain of Responsibility
	 * (see http://en.wikipedia.org/wiki/Chain-of-responsibility_pattern for
	 * details).
	 * @param {DMCommandHandler} handler The handler that is next in the chain.
	 */
	this.__setNext = function(handler) {
		var _next = this.__next;
		this.__next = nextHandler;
		return _next ? _next : true;
	};
	this.__setNext(nextHandler);

	/**
	 * Invokes the next handler. If there is no next handler, this method
	 * returns false, otherwise it forwards the result of the handling.
	 * @param {Object} command The command object to be processed.
	 */
	this.__invokeNext = function(command) {
		return this.__next ? this.__next.handle(command) : false;
	};
	
	/**
	 * Handles a command. The abstract method process() is called with the
	 * command object that has been passed. If the process method catches the
	 * command (returns true on completion), the handle() method returns true.
	 * If the process() method doesn't catch the command, the next handler will
	 * be invoked.
	 * @param {Object} command The command object to be processed.
	 */
	this.handle = function(command) {
		return this.process(command) ? true : this.__invokeNext(command);
	}
	
	/**
	 * Empty process() method returning false. If javascript knew abstract
	 * class members, this would be one.
	 * @param {Object} command The command object to process.
	 */
	this.process = function(command) { return false; };
};

/**
 * This Handler manages the addition and the removal of meta elements in the
 * head of the document.
 * @param {DMCommandHandler} next The handler that is next in the chain.
 */
function MetaTagHandler(next) {
	
	DMCommandHandler.apply(this, [next]);
	this.process = function(command) {
		
		with(command.triple) {
			
			/* assert prerequisites */
			if( !(
				(subject instanceof ERDF.Resource) &&
				(subject.isCurrentDocument()) &&
				(object instanceof ERDF.Literal)
			))	return false;
		}
		
	};
};

var chain =	new MetaTagHandler();
var command = new DMCommand(TRIPLE_ADD, new ERDF.Triple(
	new ERDF.Resource(''),
	'rdf:tool',
	new ERDF.Literal('')
));

/*
if(chain.handle(command))
	alert('Handled!');
*/

ResourceManager = {
	
	__corrupt: false,
	__latelyCreatedResource: undefined,
	__listeners: $H(),
	__token: 1,
	
	addListener: function(listener, mask) {

		if(!(listener instanceof Function))
			throw 'Resource event listener is not a function!';
		if(!(mask))
			throw 'Invalid mask for resource event listener registration.';

		// construct controller and token.
		var controller = {listener: listener, mask: mask};
		var token = ResourceManager.__token++;
		
		// add new listener.
		ResourceManager.__listeners[token] = controller;
		
		// return the token generated.
		return token;
	},
	
	removeListener: function(token) {
		
		// remove the listener with the token and return it.
		return ResourceManager.__listners.remove(token);
	},
	
	__Event: function(action, resourceId) {
		this.action = action;
		this.resourceId = resourceId;
	},
	
	__dispatchEvent: function(event) {
		
		// get all listeners. for each listener, ...
		ResourceManager.__listeners.values().each(function(controller) {
			
			// .. if listener subscribed to this type of event ...
			if(event.action & controller.mask)
				return controller.listener(event);
		});
	},

	getResource: function(id) {

		// get all possible resources for this.
		id = ERDF.__stripHashes(id);
		var resources = DataManager.query(
			new ERDF.Resource('#'+id),
			{prefix: 'raziel', name: 'entry'},
			undefined
		);

		// check for consistency.
		if((resources.length == 1) && (resources[0].object.isResource())) {
			var entryUrl = resources[0].object.value;
			return new ResourceManager.__Resource(id, entryUrl);
		}

		// else throw an error message.
		throw ('Resource with id ' +id+ ' not recognized as such. ' +
			((resources.length > 1) ?
				' There is more than one raziel:entry URL.' :
				' There is no raziel:entry URL.'));

		return false;
	},

	__createResource: function(alternativeDiv) {
		
		var collectionUrls = DataManager.query(
			new ERDF.Resource(''),
			// TODO This will become raziel:collection in near future.
			{prefix: 'raziel', name: 'collection'},
			undefined
		);

		// check for consistency.
		
		if(	(collectionUrls.length == 1) &&
			(collectionUrls[0].object.isResource())) {

			// get the collection url.
			
			var collectionUrl = collectionUrls[0].object.value;
			var resource = undefined;
			
			// if there is an old id, serialize the dummy div from there,
			// otherwise create a dummy div on the fly.
			
			var serialization = alternativeDiv? alternativeDiv : 
					'<div xmlns="http://www.w3.org/1999/xhtml"></div>';
					
			ResourceManager.__request(
				'POST', collectionUrl, serialization,

				// on success
				function() {
					
					// get div and id that have been generated by the server.
					
					var response = (this.responseXML);
					var div = response.childNodes[0];
					var id = div.getAttribute('id');
					
					// store div in DOM
					if(!$$('.' + PROCESSDATA_REF)[0])
						DataManager.graft(XMLNS.XHTML,
							document.getElementsByTagNameNS(XMLNS.XHTML, 'body').item(0), ['div', {'class': PROCESSDATA_REF, 'style':'display:none;'}]);
				
					$$('.' + PROCESSDATA_REF)[0].appendChild(div.cloneNode(true));

					// parse local erdf data once more.
					
					DataManager.__synclocal();
					
					// get new resource object.

					resource = new ResourceManager.getResource(id);

					// set up an action informing of the creation.
					
					ResourceManager.__resourceActionSucceeded(
						this, RESOURCE_CREATED, undefined);
				},

				function() { ResourceManager.__resourceActionFailed(
					this, RESOURCE_CREATED, undefined);},
				false
			);
			
			return resource;
		}
		
		// else
		throw 'Could not create resource! raziel:collection URL is missing!';
		return false;

	},
	
	__Resource: function(id, url) {
		
		this.__id = id;
		this.__url = url;
		
		/*
		 * Process URL is no longer needed to refer to the shape element on the
		 * canvas. AReSS uses the id's to gather information on fireing
		 * behaviour now.
		 */
		
//		// find the process url.		
//		var processUrl = undefined;
//		
//		var urls = DataManager.query(
//			new ERDF.Resource('#'+this.__id),
//			{prefix: 'raziel', name: 'process'},
//			undefined
//		);
//		
//		if(urls.length == 0) { throw 'The resource with the id ' +id+ ' has no process url.'};
//		
//		urls.each( function(triple) {
//			
//			// if there are more urls, use the last one.
//			processUrl = triple.object.value;
//		});
//		
//		this.__processUrl = processUrl;
//
//		// convenience function for getting the process url.
//		this.processUrl = function() {
//			return this.__processUrl;
//		}


		// convenience finction for getting the id.
		this.id = function() {
			return this.__id;
		}

		// convenience finction for getting the entry url.
		this.url = function() {
			return this.__url;
		}
		
		this.reload = function() {
			var _url = this.__url;
			var _id = this.__id;
			ResourceManager.__request(
				'GET', _url, null,
				function() { ResourceManager.__resourceActionSucceeded(
					this, RESOURCE_RELOADED, _id); },
				function() { ResourceManager.__resourceActionFailed(
					this, RESURCE_RELOADED, _id); },
				USE_ASYNCHRONOUS_REQUESTS
			);
		};
		
		this.save = function(synchronize) {
			var _url = this.__url;
			var _id = this.__id;
			data = DataManager.serialize($(_id));
			ResourceManager.__request(
				'PUT', _url, data,
				function() { ResourceManager.__resourceActionSucceeded(
					this, synchronize ? RESOURCE_SAVED | RESOURCE_SYNCHRONIZED : RESOURCE_SAVED, _id); },
				function() { ResourceManager.__resourceActionFailed(
					this, synchronize ? RESOURCE_SAVED | RESOURCE_SYNCHRONIZED : RESOURCE.SAVED, _id); },
				USE_ASYNCHRONOUS_REQUESTS
			);
		};
		
		this.remove = function() {
			var _url = this.__url;
			var _id = this.__id;
			ResourceManager.__request(
				'DELETE', _url, null,
				function() { ResourceManager.__resourceActionSucceeded(
					this, RESOURCE_REMOVED, _id); },
				function() { ResourceManager.__resourceActionFailed(
					this, RESOURCE_REMOVED, _id);},
				USE_ASYNCHRONOUS_REQUESTS
			);
		};
	},

	request: function(url, requestOptions) {

		var options = {
			method:       'get',
			asynchronous: true,
			parameters:   {}
		};

		Object.extend(options, requestOptions || {});
 		
		var params = Hash.toQueryString(options.parameters);
		if (params) 
			url += (url.include('?') ? '&' : '?') + params;
   
		return ResourceManager.__request(
			options.method, 
			url, 
			options.data, 
			(options.onSuccess instanceof Function ? function() { options.onSuccess(this); } : undefined ), 
			(options.onFailure instanceof Function ? function() { options.onFailure(this); } : undefined ), 
			options.asynchronous && USE_ASYNCHRONOUS_REQUESTS,
			options.headers);
	},
	
	__request: function(method, url, data, success, error, async, headers) {
		
		// get a request object
		var httpRequest = Try.these(

			/* do the Mozilla/Safari/Opera stuff */
			function() { return new XMLHttpRequest(); },
			
			/* do the IE stuff */
			function() { return new ActiveXObject("Msxml2.XMLHTTP"); },
			function() { return new ActiveXObject("Microsoft.XMLHTTP") }
		);

		// if there is no request object ...
        if (!httpRequest) {
			if(!this.__corrupt)
				throw 'This browser does not provide any AJAX functionality. You will not be able to use the software provided with the page you are viewing. Please consider installing appropriate extensions.';
			this.__corrupt = true;
			return false;
        }
		
		if(success instanceof Function)
			httpRequest.onload = success;
		if(error instanceof Function) {
			httpRequest.onerror = error;
		}
		
		var h = $H(headers)
		h.keys().each(function(key) {
			
			httpRequest.setRequestHeader(key, h[key]);
		}); 
		
		try {

			if(SHOW_DEBUG_ALERTS_WHEN_SAVING)
			
				alert(method + ' ' + url + '\n' +
					SHOW_EXTENDED_DEBUG_INFORMATION ? data : '');

			// TODO Remove synchronous calls to the server as soon as xenodot
			// handles asynchronous requests without failure.
	        httpRequest.open(method, url, !async?false:true);
	        httpRequest.send(data);
			
		} catch(e) {
			return false;
		}
		return true;
    },

	__resourceActionSucceeded: function(transport, action, id) {
		
		var status = transport.status;
		var response = transport.responseText;
		
		if(SHOW_DEBUG_ALERTS_WHEN_SAVING)

			alert(status + ' ' + url + '\n' +
				SHOW_EXTENDED_DEBUG_INFORMATION ? data : '');

		// if the status code is not in 2xx, throw an error.
		if(status >= 300)
			throw 'The server responded with an error: ' + status + '\n' + (SHOW_EXTENDED_DEBUG_INFORMATION ? + data : 'If you need additional information here, including the data sent by the server, consider setting SHOW_EXTENDED_DEBUG_INFORMATION to true.');

		switch(action) {
			
			case RESOURCE_REMOVED:

				// get div and id
				var response = (transport.responseXML);
				var div = response.childNodes[0];
				var id = div.getAttribute('id');
				
				// remove the resource from DOM
				var localDiv = document.getElementById(id);
				localDiv.parentNode.removeChild(localDiv);
				break;

			case RESOURCE_CREATED:

				// nothing remains to be done.
				break;
	
			case RESOURCE_SAVED | RESOURCE_SYNCHRONIZED:

				DataManager.__synclocal();

			case RESOURCE_SAVED:

				// nothing remains to be done.
				break;

			case RESOURCE_RELOADED:
			
				// get div and id
				var response = (transport.responseXML);
				var div = response.childNodes[0];
				var id = div.getAttribute('id');
				
				// remove the local resource representation from DOM
				var localDiv = document.getElementById(id)
				localDiv.parentNode.removeChild(localDiv);
				
				// store div in DOM
				if(!$$(PROCESSDATA_REF)[0])
					DataManager.graft(XMLNS.XHTML,
						document.getElementsByTagNameNS(XMLNS.XHTML, 'body').item(0), ['div', {'class': PROCESSDATA_REF, 'style':'display:none;'}]);
				
				$$(PROCESSDATA_REF)[0].appendChild(div.cloneNode(true));
				DataManager.__synclocal();
				break;

			default:
				DataManager.__synclocal();

		}
		 
		// dispatch to all listeners ...
		ResourceManager.__dispatchEvent(

			// ... an event describing the change that happened here.
			new ResourceManager.__Event(action, id)
		);
	},

	__resourceActionFailed: function(transport, action, id) {
		throw "Fatal: Resource action failed. There is something horribly " +
			"wrong with either the server, the transport protocol or your " +
			"online status. Sure you're online?";
	}
}/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

/**
 * The super class for all classes in ORYX. Adds some OOP feeling to javascript.
 * See article "Object Oriented Super Class Method Calling with JavaScript" on
 * http://truecode.blogspot.com/2006/08/object-oriented-super-class-method.html
 * for a documentation on this. Fairly good article that points out errors in
 * Douglas Crockford's inheritance and super method calling approach.
 * Worth reading.
 * @class Clazz
 */
var Clazz = function() {};

/**
 * Empty constructor.
 * @methodOf Clazz.prototype
 */
Clazz.prototype.construct = function() {};

/**
 * Can be used to build up inheritances of classes.
 * @example
 * var MyClass = Clazz.extend({
 *   construct: function(myParam){
 *     // Do sth.
 *   }
 * });
 * var MySubClass = MyClass.extend({
 *   construct: function(myParam){
 *     // Use this to call constructor of super class
 *     arguments.callee.$.construct.apply(this, arguments);
 *     // Do sth.
 *   }
 * });
 * @param {Object} def The definition of the new class.
 */
Clazz.extend = function(def) {
    var classDef = function() {
        if (arguments[0] !== Clazz) { this.construct.apply(this, arguments); }
    };
    
    var proto = new this(Clazz);
    var superClass = this.prototype;
    
    for (var n in def) {
        var item = def[n];                        
        if (item instanceof Function) item.$ = superClass;
        proto[n] = item;
    }

    classDef.prototype = proto;
    
    //Give this new class the same static extend method    
    classDef.extend = this.extend;        
    return classDef;
};/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

if(!ORYX) var ORYX = {};

if(!ORYX.CONFIG) ORYX.CONFIG = {};

/**
 * This file contains URI constants that may be used for XMLHTTPRequests.
 */

ORYX.CONFIG.ROOT_PATH =					"editor/"; //TODO: Remove last slash!!
ORYX.CONFIG.EXPLORER_PATH =				"explorer";
ORYX.CONFIG.LIBS_PATH =					"libs";

/**
 * Regular Config
 */	
ORYX.CONFIG.SERVER_HANDLER_ROOT = 			"service";
ORYX.CONFIG.SERVER_EDITOR_HANDLER =			ORYX.CONFIG.SERVER_HANDLER_ROOT + "/editor";
ORYX.CONFIG.SERVER_MODEL_HANDLER =			ORYX.CONFIG.SERVER_HANDLER_ROOT + "/model";
ORYX.CONFIG.STENCILSET_HANDLER = 			ORYX.CONFIG.SERVER_HANDLER_ROOT + "/editor_stencilset?embedsvg=true&url=true&namespace=";    
ORYX.CONFIG.STENCIL_SETS_URL = 				ORYX.CONFIG.SERVER_HANDLER_ROOT + "/editor_stencilset";

ORYX.CONFIG.PLUGINS_CONFIG =				"editor-app/plugins.xml";
ORYX.CONFIG.SYNTAXCHECKER_URL =				ORYX.CONFIG.SERVER_HANDLER_ROOT + "/syntaxchecker";
ORYX.CONFIG.DEPLOY_URL = 					ORYX.CONFIG.SERVER_HANDLER_ROOT + "/model/deploy";
ORYX.CONFIG.MODEL_LIST_URL = 				ORYX.CONFIG.SERVER_HANDLER_ROOT + "/models";
ORYX.CONFIG.FORM_FLOW_LIST_URL = 			ORYX.CONFIG.SERVER_HANDLER_ROOT + "/formflows";
ORYX.CONFIG.FORM_FLOW_IMAGE_URL = 			ORYX.CONFIG.SERVER_HANDLER_ROOT + "/formflow";
ORYX.CONFIG.FORM_LIST_URL = 				ORYX.CONFIG.SERVER_HANDLER_ROOT + "/forms";
ORYX.CONFIG.FORM_IMAGE_URL = 				ORYX.CONFIG.SERVER_HANDLER_ROOT + "/form";
ORYX.CONFIG.SUB_PROCESS_LIST_URL = 			ORYX.CONFIG.SERVER_HANDLER_ROOT + "/subprocesses";
ORYX.CONFIG.SUB_PROCESS_IMAGE_URL = 		ORYX.CONFIG.SERVER_HANDLER_ROOT + "/subprocess";
ORYX.CONFIG.TEST_SERVICE_URL = 				ORYX.CONFIG.SERVER_HANDLER_ROOT + "/service/";

ORYX.CONFIG.SERVICE_LIST_URL = 				ORYX.CONFIG.SERVER_HANDLER_ROOT + "/services";
ORYX.CONFIG.CONDITION_ELEMENT_LIST_URL = 	ORYX.CONFIG.SERVER_HANDLER_ROOT + "/conditionelements";
ORYX.CONFIG.VARIABLEDEF_ELEMENT_LIST_URL = 	ORYX.CONFIG.SERVER_HANDLER_ROOT + "/variabledefinitionelements";
ORYX.CONFIG.VALIDATOR_LIST_URL = 			ORYX.CONFIG.SERVER_HANDLER_ROOT + "/validators";

ORYX.CONFIG.SS_EXTENSIONS_FOLDER =			ORYX.CONFIG.ROOT_PATH + "stencilsets/extensions/";
ORYX.CONFIG.SS_EXTENSIONS_CONFIG =			ORYX.CONFIG.SERVER_HANDLER_ROOT + "/editor_ssextensions";	
ORYX.CONFIG.ORYX_NEW_URL =					"/new";	
ORYX.CONFIG.BPMN_LAYOUTER =					ORYX.CONFIG.ROOT_PATH + "bpmnlayouter";

ORYX.CONFIG.EXPRESSION_METADATA_URL = 			ORYX.CONFIG.SERVER_HANDLER_ROOT + "/expression-metadata";
ORYX.CONFIG.DATASOURCE_METADATA_URL = 			ORYX.CONFIG.SERVER_HANDLER_ROOT + "/datasource-metadata";/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

if(!ORYX) var ORYX = {};

if(!ORYX.CONFIG) ORYX.CONFIG = {};

/**
 * Signavio specific variables
 */
ORYX.CONFIG.BACKEND_SWITCH 		= 		true;
ORYX.CONFIG.PANEL_LEFT_WIDTH 	= 		250;
ORYX.CONFIG.PANEL_RIGHT_COLLAPSED 	= 	true;
ORYX.CONFIG.PANEL_RIGHT_WIDTH	= 		300;
ORYX.CONFIG.APPNAME = 					'KISBPM';
ORYX.CONFIG.WEB_URL = 					".";

ORYX.CONFIG.BLANK_IMAGE = ORYX.CONFIG.LIBS_PATH + '/ext-2.0.2/resources/images/default/s.gif';

/* Specify offset of header */
ORYX.CONFIG.OFFSET_HEADER = 61;

/* Show grid line while dragging */
ORYX.CONFIG.SHOW_GRIDLINE = 			true;

	/* Editor-Mode */
ORYX.CONFIG.MODE_READONLY =				"readonly";
ORYX.CONFIG.MODE_FULLSCREEN =			"fullscreen";
ORYX.CONFIG.WINDOW_HEIGHT = 			800;	
ORYX.CONFIG.PREVENT_LOADINGMASK_AT_READY = false;

	/* Plugins */
ORYX.CONFIG.PLUGINS_ENABLED =			true;
ORYX.CONFIG.PLUGINS_FOLDER =			"Plugins/";

ORYX.CONFIG.BPMN20_SCHEMA_VALIDATION_ON = true;

	/* Namespaces */
ORYX.CONFIG.NAMESPACE_ORYX =			"http://www.b3mn.org/oryx";
ORYX.CONFIG.NAMESPACE_SVG =				"http://www.w3.org/2000/svg";

	/* UI */
ORYX.CONFIG.CANVAS_WIDTH =				1200; 
ORYX.CONFIG.CANVAS_HEIGHT =				1050;
ORYX.CONFIG.CANVAS_RESIZE_INTERVAL =	100;
ORYX.CONFIG.CANVAS_MIN_WIDTH =  800;
ORYX.CONFIG.CANVAS_MIN_HEIGHT =  300;
ORYX.CONFIG.SELECTED_AREA_PADDING =		4;
ORYX.CONFIG.CANVAS_BACKGROUND_COLOR =	"none";
ORYX.CONFIG.GRID_DISTANCE =				30;
ORYX.CONFIG.GRID_ENABLED =				true;
ORYX.CONFIG.ZOOM_OFFSET =				0.1;
ORYX.CONFIG.DEFAULT_SHAPE_MARGIN =		60;
ORYX.CONFIG.SCALERS_SIZE =				7;
ORYX.CONFIG.MINIMUM_SIZE =				20;
ORYX.CONFIG.MAXIMUM_SIZE =				10000;
ORYX.CONFIG.OFFSET_MAGNET =				15;
ORYX.CONFIG.OFFSET_EDGE_LABEL_TOP =		8;
ORYX.CONFIG.OFFSET_EDGE_LABEL_BOTTOM =	8;
ORYX.CONFIG.OFFSET_EDGE_BOUNDS =		5;
ORYX.CONFIG.COPY_MOVE_OFFSET =			30;
	
ORYX.CONFIG.BORDER_OFFSET =				14;

ORYX.CONFIG.MAX_NUM_SHAPES_NO_GROUP	=	20; // Updated so the form editor shows all elements at once

ORYX.CONFIG.SHAPEMENU_CREATE_OFFSET_CORNER = 30;
ORYX.CONFIG.SHAPEMENU_CREATE_OFFSET = 45;

	/* Shape-Menu Align */
ORYX.CONFIG.SHAPEMENU_RIGHT =			"Oryx_Right";
ORYX.CONFIG.SHAPEMENU_BOTTOM =			"Oryx_Bottom";
ORYX.CONFIG.SHAPEMENU_LEFT =			"Oryx_Left";
ORYX.CONFIG.SHAPEMENU_TOP =				"Oryx_Top";


	/* Morph-Menu Item */
ORYX.CONFIG.MORPHITEM_DISABLED =		"Oryx_MorphItem_disabled";

	/* Property type names */
ORYX.CONFIG.TYPE_STRING =				"string";
ORYX.CONFIG.TYPE_BOOLEAN =				"boolean";
ORYX.CONFIG.TYPE_INTEGER =				"integer";
ORYX.CONFIG.TYPE_FLOAT =				"float";
ORYX.CONFIG.TYPE_COLOR =				"color";
ORYX.CONFIG.TYPE_DATE =					"date";
ORYX.CONFIG.TYPE_CHOICE =				"choice";
ORYX.CONFIG.TYPE_URL =					"url";
ORYX.CONFIG.TYPE_DIAGRAM_LINK =			"diagramlink";
ORYX.CONFIG.TYPE_COMPLEX =				"complex";
ORYX.CONFIG.TYPE_MULTIPLECOMPLEX =		"multiplecomplex";
ORYX.CONFIG.TYPE_TEXT =					"text";
ORYX.CONFIG.TYPE_KISBPM_MULTIINSTANCE =	"kisbpm-multiinstance";
ORYX.CONFIG.TYPE_MODEL_LINK =			"modellink";
ORYX.CONFIG.TYPE_FORM_FLOW_LINK =		"formflowlink";
ORYX.CONFIG.TYPE_FORM_LINK =			"formlink";
ORYX.CONFIG.TYPE_SUB_PROCESS_LINK =		"subprocesslink";
ORYX.CONFIG.TYPE_SERVICE_LINK =			"servicelink";
ORYX.CONFIG.TYPE_CONDITIONS =			"conditions";
ORYX.CONFIG.TYPE_VARIABLES = 			"variables";
ORYX.CONFIG.TYPE_LISTENER =				"listener";
ORYX.CONFIG.TYPE_EPC_FREQ = 			"epcfrequency";
ORYX.CONFIG.TYPE_GLOSSARY_LINK =		"glossarylink";
ORYX.CONFIG.TYPE_EXPRESSION = 			"expression";
ORYX.CONFIG.TYPE_DATASOURCE = 			"datasource";
ORYX.CONFIG.TYPE_DATASOURCE_MINIMAL =	"datasource-minimal";
ORYX.CONFIG.TYPE_VALIDATORS =			"validators";

	
	/* Vertical line distance of multiline labels */
ORYX.CONFIG.LABEL_LINE_DISTANCE =		2;
ORYX.CONFIG.LABEL_DEFAULT_LINE_HEIGHT =	12;

	/* Open Morph Menu with Hover */
ORYX.CONFIG.ENABLE_MORPHMENU_BY_HOVER = false;


	/* Editor constants come here */
ORYX.CONFIG.EDITOR_ALIGN_BOTTOM =		0x01;
ORYX.CONFIG.EDITOR_ALIGN_MIDDLE =		0x02;
ORYX.CONFIG.EDITOR_ALIGN_TOP =			0x04;
ORYX.CONFIG.EDITOR_ALIGN_LEFT =			0x08;
ORYX.CONFIG.EDITOR_ALIGN_CENTER =		0x10;
ORYX.CONFIG.EDITOR_ALIGN_RIGHT =		0x20;
ORYX.CONFIG.EDITOR_ALIGN_SIZE =			0x30;

	/* Event types */
ORYX.CONFIG.EVENT_MOUSEDOWN =			"mousedown";
ORYX.CONFIG.EVENT_MOUSEUP =				"mouseup";
ORYX.CONFIG.EVENT_MOUSEOVER =			"mouseover";
ORYX.CONFIG.EVENT_MOUSEOUT =			"mouseout";
ORYX.CONFIG.EVENT_MOUSEMOVE =			"mousemove";
ORYX.CONFIG.EVENT_DBLCLICK =			"dblclick";
ORYX.CONFIG.EVENT_KEYDOWN =				"keydown";
ORYX.CONFIG.EVENT_KEYUP =				"keyup";

ORYX.CONFIG.EVENT_LOADED =				"editorloaded";
ORYX.CONFIG.EVENT_SAVED =				"editorSaved";
	
ORYX.CONFIG.EVENT_EXECUTE_COMMANDS =		"executeCommands";
ORYX.CONFIG.EVENT_STENCIL_SET_LOADED =		"stencilSetLoaded";
ORYX.CONFIG.EVENT_SELECTION_CHANGED =		"selectionchanged";
ORYX.CONFIG.EVENT_SHAPEADDED =				"shapeadded";
ORYX.CONFIG.EVENT_SHAPEREMOVED =			"shaperemoved";
ORYX.CONFIG.EVENT_PROPERTY_CHANGED =		"propertyChanged";
ORYX.CONFIG.EVENT_DRAGDROP_START =			"dragdrop.start";
ORYX.CONFIG.EVENT_SHAPE_MENU_CLOSE =		"shape.menu.close";
ORYX.CONFIG.EVENT_DRAGDROP_END =			"dragdrop.end";
ORYX.CONFIG.EVENT_RESIZE_START =			"resize.start";
ORYX.CONFIG.EVENT_RESIZE_END =				"resize.end";
ORYX.CONFIG.EVENT_DRAGDOCKER_DOCKED =		"dragDocker.docked";
ORYX.CONFIG.EVENT_HIGHLIGHT_SHOW =			"highlight.showHighlight";
ORYX.CONFIG.EVENT_HIGHLIGHT_HIDE =			"highlight.hideHighlight";
ORYX.CONFIG.EVENT_LOADING_ENABLE =			"loading.enable";
ORYX.CONFIG.EVENT_LOADING_DISABLE =			"loading.disable";
ORYX.CONFIG.EVENT_LOADING_STATUS =			"loading.status";
ORYX.CONFIG.EVENT_OVERLAY_SHOW =			"overlay.show";
ORYX.CONFIG.EVENT_OVERLAY_HIDE =			"overlay.hide";
ORYX.CONFIG.EVENT_ARRANGEMENT_TOP =			"arrangement.setToTop";
ORYX.CONFIG.EVENT_ARRANGEMENT_BACK =		"arrangement.setToBack";
ORYX.CONFIG.EVENT_ARRANGEMENT_FORWARD =		"arrangement.setForward";
ORYX.CONFIG.EVENT_ARRANGEMENT_BACKWARD =	"arrangement.setBackward";
ORYX.CONFIG.EVENT_PROPWINDOW_PROP_CHANGED =	"propertyWindow.propertyChanged";
ORYX.CONFIG.EVENT_LAYOUT_ROWS =				"layout.rows";
ORYX.CONFIG.EVENT_LAYOUT_BPEL =				"layout.BPEL";
ORYX.CONFIG.EVENT_LAYOUT_BPEL_VERTICAL =    "layout.BPEL.vertical";
ORYX.CONFIG.EVENT_LAYOUT_BPEL_HORIZONTAL =  "layout.BPEL.horizontal";
ORYX.CONFIG.EVENT_LAYOUT_BPEL_SINGLECHILD = "layout.BPEL.singlechild";
ORYX.CONFIG.EVENT_LAYOUT_BPEL_AUTORESIZE =	"layout.BPEL.autoresize";
ORYX.CONFIG.EVENT_AUTOLAYOUT_LAYOUT =		"autolayout.layout";
ORYX.CONFIG.EVENT_UNDO_EXECUTE =			"undo.execute";
ORYX.CONFIG.EVENT_UNDO_ROLLBACK =			"undo.rollback";
ORYX.CONFIG.EVENT_BUTTON_UPDATE =           "toolbar.button.update";
ORYX.CONFIG.EVENT_LAYOUT = 					"layout.dolayout";
ORYX.CONFIG.EVENT_GLOSSARY_LINK_EDIT = 		"glossary.link.edit";
ORYX.CONFIG.EVENT_GLOSSARY_SHOW =			"glossary.show.info";
ORYX.CONFIG.EVENT_GLOSSARY_NEW =			"glossary.show.new";
ORYX.CONFIG.EVENT_DOCKERDRAG = 				"dragTheDocker";
ORYX.CONFIG.EVENT_CANVAS_SCROLL = 			"canvas.scroll";
	
ORYX.CONFIG.EVENT_SHOW_PROPERTYWINDOW =		"propertywindow.show";
ORYX.CONFIG.EVENT_ABOUT_TO_SAVE = "file.aboutToSave";
	
	/* Selection Shapes Highlights */
ORYX.CONFIG.SELECTION_HIGHLIGHT_SIZE =				5;
ORYX.CONFIG.SELECTION_HIGHLIGHT_COLOR =				"#4444FF";
ORYX.CONFIG.SELECTION_HIGHLIGHT_COLOR2 =			"#9999FF";
	
ORYX.CONFIG.SELECTION_HIGHLIGHT_STYLE_CORNER = 		"corner";
ORYX.CONFIG.SELECTION_HIGHLIGHT_STYLE_RECTANGLE = 	"rectangle";
	
ORYX.CONFIG.SELECTION_VALID_COLOR =					"#00FF00";
ORYX.CONFIG.SELECTION_INVALID_COLOR =				"#FF0000";


ORYX.CONFIG.DOCKER_DOCKED_COLOR =		"#00FF00";
ORYX.CONFIG.DOCKER_UNDOCKED_COLOR =		"#FF0000";
ORYX.CONFIG.DOCKER_SNAP_OFFSET =		10;
		
	/* Copy & Paste */
ORYX.CONFIG.EDIT_OFFSET_PASTE =			10;

	/* Key-Codes */
ORYX.CONFIG.KEY_CODE_X = 				88;
ORYX.CONFIG.KEY_CODE_C = 				67;
ORYX.CONFIG.KEY_CODE_V = 				86;
ORYX.CONFIG.KEY_CODE_DELETE = 			46;
ORYX.CONFIG.KEY_CODE_META =				224;
ORYX.CONFIG.KEY_CODE_BACKSPACE =		8;
ORYX.CONFIG.KEY_CODE_LEFT =				37;
ORYX.CONFIG.KEY_CODE_RIGHT =			39;
ORYX.CONFIG.KEY_CODE_UP =				38;
ORYX.CONFIG.KEY_CODE_DOWN =				40;

	// TODO Determine where the lowercase constants are still used and remove them from here.
ORYX.CONFIG.KEY_Code_enter =			12;
ORYX.CONFIG.KEY_Code_left =				37;
ORYX.CONFIG.KEY_Code_right =			39;
ORYX.CONFIG.KEY_Code_top =				38;
ORYX.CONFIG.KEY_Code_bottom =			40;

/* Supported Meta Keys */
	
ORYX.CONFIG.META_KEY_META_CTRL = 		"metactrl";
ORYX.CONFIG.META_KEY_ALT = 				"alt";
ORYX.CONFIG.META_KEY_SHIFT = 			"shift";

/* Key Actions */

ORYX.CONFIG.KEY_ACTION_DOWN = 			"down";
ORYX.CONFIG.KEY_ACTION_UP = 			"up";


/* Form Rowlayouting */
ORYX.CONFIG.FORM_ROW_WIDTH =            350;
ORYX.CONFIG.FORM_GROUP_MARGIN =            5;
ORYX.CONFIG.FORM_GROUP_EMPTY_HEIGHT =   100;

/* Form element types */
ORYX.CONFIG.FORM_ELEMENT_ID_PREFIX = 				'http://b3mn.org/stencilset/xforms';
ORYX.CONFIG.FORM_ELEMENT_TYPE_ROOT = 				'http://b3mn.org/stencilset/xforms#XForm';
ORYX.CONFIG.FORM_ELEMENT_TYPE_GROUP = 				'http://b3mn.org/stencilset/xforms#Group';
ORYX.CONFIG.FORM_ELEMENT_TYPE_REPEATING_GROUP =		'http://b3mn.org/stencilset/xforms#RepeatingGroup';
ORYX.CONFIG.FORM_ELEMENT_TYPE_LABEL_FIELD = 		'http://b3mn.org/stencilset/xforms#LabelField';
	
	/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

function printf() {
	
	var result = arguments[0];
	for (var i=1; i<arguments.length; i++)
		result = result.replace('%' + (i-1), arguments[i]);
	return result;
}

// oryx constants.
var ORYX_LOGLEVEL_TRACE = 5;
var ORYX_LOGLEVEL_DEBUG = 4;
var ORYX_LOGLEVEL_INFO = 3;
var ORYX_LOGLEVEL_WARN = 2;
var ORYX_LOGLEVEL_ERROR = 1;
var ORYX_LOGLEVEL_FATAL = 0;
var ORYX_LOGLEVEL = 3;
var ORYX_CONFIGURATION_DELAY = 100;
var ORYX_CONFIGURATION_WAIT_ATTEMPTS = 10;

if(!ORYX) var ORYX = {};

ORYX = Object.extend(ORYX, {

	//set the path in the config.js file!!!!
	PATH: ORYX.CONFIG.ROOT_PATH,
	//CONFIGURATION: "config.js",

	URLS: [],

	alreadyLoaded: [],

	configrationRetries: 0,

	Version: '0.1.1',

	availablePlugins: [],

	/**
	 * The ORYX.Log logger.
	 */
	Log: {
	
		__appenders: [
			{ 
				append: function(message) {
					if(typeof(console) !== "undefined" && console.log !== undefined) {
						console.log(message); 
					}
				}
			}
		],
	
		trace: function() {	if(ORYX_LOGLEVEL >= ORYX_LOGLEVEL_TRACE)
			ORYX.Log.__log('TRACE', arguments); },
		debug: function() { if(ORYX_LOGLEVEL >= ORYX_LOGLEVEL_DEBUG)
			ORYX.Log.__log('DEBUG', arguments); },
		info: function() { if(ORYX_LOGLEVEL >= ORYX_LOGLEVEL_INFO)
			ORYX.Log.__log('INFO', arguments); },
		warn: function() { if(ORYX_LOGLEVEL >= ORYX_LOGLEVEL_WARN)
			ORYX.Log.__log('WARN', arguments); },
		error: function() { if(ORYX_LOGLEVEL >= ORYX_LOGLEVEL_ERROR)
			ORYX.Log.__log('ERROR', arguments); },
		fatal: function() { if(ORYX_LOGLEVEL >= ORYX_LOGLEVEL_FATAL)
			ORYX.Log.__log('FATAL', arguments); },
		
		__log: function(prefix, messageParts) {
			
			messageParts[0] = (new Date()).getTime() + " "
				+ prefix + " " + messageParts[0];
			var message = printf.apply(null, messageParts);
			
			ORYX.Log.__appenders.each(function(appender) {
				appender.append(message);
			});
		},
		
		addAppender: function(appender) {
			ORYX.Log.__appenders.push(appender);
		}
	},

	/**
	 * First bootstrapping layer. The Oryx loading procedure begins. In this
	 * step, all preliminaries that are not in the responsibility of Oryx to be
	 * met have to be checked here, such as the existance of the prototpe
	 * library in the current execution environment. After that, the second
	 * bootstrapping layer is being invoked. Failing to ensure that any
	 * preliminary condition is not met has to fail with an error.
	 */
	load: function() {
		
		ORYX.Log.debug("Oryx begins loading procedure.");
		
		// check for prototype
		if( (typeof Prototype=='undefined') ||
			(typeof Element == 'undefined') ||
			(typeof Element.Methods=='undefined') ||
			parseFloat(Prototype.Version.split(".")[0] + "." +
				Prototype.Version.split(".")[1]) < 1.5)

			throw("Application requires the Prototype JavaScript framework >= 1.5.3");
		
		ORYX.Log.debug("Prototype > 1.5 found.");

		// continue loading.
		ORYX._load();
	},

	/**
	 * Second bootstrapping layer. The oryx configuration is checked. When not
	 * yet loaded, config.js is being requested from the server. A repeated
	 * error in retrieving the configuration will result in an error to be
	 * thrown after a certain time of retries. Once the configuration is there,
	 * all urls that are registered with oryx loading are being requested from
	 * the server. Once everything is loaded, the third layer is being invoked.
	 */
	_load: function() {
	/*
		// if configuration not there already,
		if(!(ORYX.CONFIG)) {
			
			// if this is the first attempt...
			if(ORYX.configrationRetries == 0) {
				
				// get the path and filename.
				var configuration = ORYX.PATH + ORYX.CONFIGURATION;
	
				ORYX.Log.debug("Configuration not found, loading from '%0'.",
					configuration);
				
				// require configuration file.
				Kickstart.require(configuration);
				
			// else if attempts exceeded ...
			} else if(ORYX.configrationRetries >= ORYX_CONFIGURATION_WAIT_ATTEMPTS) {
				
				throw "Tried to get configuration" +
					ORYX_CONFIGURATION_WAIT_ATTEMPTS +
					" times from '" + configuration + "'. Giving up."
					
			} else if(ORYX.configrationRetries > 0){
				
				// point out how many attempts are left...
				ORYX.Log.debug("Waiting once more (%0 attempts left)",
					(ORYX_CONFIGURATION_WAIT_ATTEMPTS -
						ORYX.configrationRetries));

			}
			
			// any case: continue in a moment with increased retry count.
			ORYX.configrationRetries++;
			window.setTimeout(ORYX._load, ORYX_CONFIGURATION_DELAY);
			return;
		}
		
		ORYX.Log.info("Configuration loaded.");
		
		// load necessary scripts.
		ORYX.URLS.each(function(url) {
			ORYX.Log.debug("Requireing '%0'", url);
			Kickstart.require(ORYX.PATH + url) });
	*/
		// configurate logging and load plugins.
		ORYX.loadPlugins();
	},

	/**
	 * Third bootstrapping layer. This is where first the plugin coniguration
	 * file is loaded into oryx, analyzed, and where all plugins are being
	 * requested by the server. Afterwards, all editor instances will be
	 * initialized.
	 */
	loadPlugins: function() {
		
		// load plugins if enabled.
		if(ORYX.CONFIG.PLUGINS_ENABLED)
			ORYX._loadPlugins()
		else
			ORYX.Log.warn("Ignoring plugins, loading Core only.");

		// init the editor instances.
		init();
	},
	
	_loadPlugins: function() {

		// load plugin configuration file.
		var source = ORYX.CONFIG.PLUGINS_CONFIG;

		ORYX.Log.debug("Loading plugin configuration from '%0'.", source);
	
		new Ajax.Request(source, {
			asynchronous: false,
			method: 'get',
			onSuccess: function(result) {

				/*
				 * This is the method that is being called when the plugin
				 * configuration was successfully loaded from the server. The
				 * file has to be processed and the contents need to be
				 * considered for further plugin requireation.
				 */
				
				ORYX.Log.info("Plugin configuration file loaded.");
		
				// get plugins.xml content
				var resultXml = result.responseXML;
				
				// TODO: Describe how properties are handled.
				// Get the globale Properties
				var globalProperties = [];
				var preferences = $A(resultXml.getElementsByTagName("properties"));
				preferences.each( function(p) {

					var props = $A(p.childNodes);
					props.each( function(prop) {
						var property = new Hash(); 
						
						// get all attributes from the node and set to global properties
						var attributes = $A(prop.attributes)
						attributes.each(function(attr){property[attr.nodeName] = attr.nodeValue});				
						if(attributes.length > 0) { globalProperties.push(property) };				
					});
				});

				
				// TODO Why are we using XML if we don't respect structure anyway?
				// for each plugin element in the configuration..
				var plugin = resultXml.getElementsByTagName("plugin");
				$A(plugin).each( function(node) {
					
					// get all element's attributes.
					// TODO: What about: var pluginData = $H(node.attributes) !?
					var pluginData = new Hash();
					$A(node.attributes).each( function(attr){
						pluginData[attr.nodeName] = attr.nodeValue});				
					
					// ensure there's a name attribute.
					if(!pluginData['name']) {
						ORYX.Log.error("A plugin is not providing a name. Ingnoring this plugin.");
						return;
					}

					// ensure there's a source attribute.
					if(!pluginData['source']) {
						ORYX.Log.error("Plugin with name '%0' doesn't provide a source attribute.", pluginData['name']);
						return;
					}
					
					// Get all private Properties
					var propertyNodes = node.getElementsByTagName("property");
					var properties = [];
					$A(propertyNodes).each(function(prop) {
						var property = new Hash(); 
						
						// Get all Attributes from the Node			
						var attributes = $A(prop.attributes)
						attributes.each(function(attr){property[attr.nodeName] = attr.nodeValue});				
						if(attributes.length > 0) { properties.push(property) };	
					
					});
					
					// Set all Global-Properties to the Properties
					properties = properties.concat(globalProperties);
					
					// Set Properties to Plugin-Data
					pluginData['properties'] = properties;
					
					// Get the RequieredNodes
					var requireNodes = node.getElementsByTagName("requires");
					var requires;
					$A(requireNodes).each(function(req) {			
						var namespace = $A(req.attributes).find(function(attr){ return attr.name == "namespace"})
						if( namespace && namespace.nodeValue ){
							if( !requires ){
								requires = {namespaces:[]}
							}
						
							requires.namespaces.push(namespace.nodeValue)
						} 
					});					
					
					// Set Requires to the Plugin-Data, if there is one
					if( requires ){
						pluginData['requires'] = requires;
					}


					// Get the RequieredNodes
					var notUsesInNodes = node.getElementsByTagName("notUsesIn");
					var notUsesIn;
					$A(notUsesInNodes).each(function(not) {			
						var namespace = $A(not.attributes).find(function(attr){ return attr.name == "namespace"})
						if( namespace && namespace.nodeValue ){
							if( !notUsesIn ){
								notUsesIn = {namespaces:[]}
							}
						
							notUsesIn.namespaces.push(namespace.nodeValue)
						} 
					});					
					
					// Set Requires to the Plugin-Data, if there is one
					if( notUsesIn ){
						pluginData['notUsesIn'] = notUsesIn;
					}		
					
								
					var url = ORYX.PATH + ORYX.CONFIG.PLUGINS_FOLDER + pluginData['source'];
		
					ORYX.Log.debug("Requireing '%0'", url);
		
					// Add the Script-Tag to the Site
					//Kickstart.require(url);
		
					ORYX.Log.info("Plugin '%0' successfully loaded.", pluginData['name']);
		
					// Add the Plugin-Data to all available Plugins
					ORYX.availablePlugins.push(pluginData);
		
				});
		
			},
			onFailure:this._loadPluginsOnFails
		});

	},

	_loadPluginsOnFails: function(result) {

		ORYX.Log.error("Plugin configuration file not available.");
	}
});


/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

/**
 * Init namespaces
 */
if(!ORYX) {var ORYX = {};}
if(!ORYX.Core) {ORYX.Core = {};}
if(!ORYX.Core.SVG) {ORYX.Core.SVG = {};}


/**
 * EditPathHandler
 * 
 * Edit SVG paths' coordinates according to specified from-to movement and
 * horizontal and vertical scaling factors. 
 * The resulting path's d attribute is stored in instance variable d.
 * 
 * @constructor
 */
ORYX.Core.SVG.EditPathHandler = Clazz.extend({
	
	construct: function() {
		arguments.callee.$.construct.apply(this, arguments);
		
		this.x = 0;
		this.y = 0;
		this.oldX = 0;
		this.oldY = 0;
		this.deltaWidth = 1;
		this.deltaHeight = 1;
		
		this.d = "";
	},
	
	/**
	 * init
	 * 
	 * @param {float} x Target point's x-coordinate
	 * @param {float} y Target point's y-coordinate
	 * @param {float} oldX Reference point's x-coordinate
	 * @param {float} oldY Reference point's y-coordinate
	 * @param {float} deltaWidth Horizontal scaling factor
	 * @param {float} deltaHeight Vertical scaling factor
	 */
	init: function(x, y, oldX, oldY, deltaWidth, deltaHeight) {
		this.x = x;
		this.y = y;
		this.oldX = oldX;
		this.oldY = oldY;
		this.deltaWidth = deltaWidth;
		this.deltaHeight = deltaHeight;
		
		this.d = "";
	},

	/**
	 * editPointsAbs
	 * 
	 * @param {Array} points Array of absolutePoints
	 */
	editPointsAbs: function(points) {
		if(points instanceof Array) {
			var newPoints = [];
			var x, y;
			for(var i = 0; i < points.length; i++) {
				x = (parseFloat(points[i]) - this.oldX)*this.deltaWidth + this.x;
				i++;
				y = (parseFloat(points[i]) - this.oldY)*this.deltaHeight + this.y;
				newPoints.push(x);
				newPoints.push(y);
			}
			
			return newPoints;
		} else {
			//TODO error
		}
	},
	
	/**
	 * editPointsRel
	 * 
	 * @param {Array} points Array of absolutePoints
	 */
	editPointsRel: function(points) {
		if(points instanceof Array) {
			var newPoints = [];
			var x, y;
			for(var i = 0; i < points.length; i++) {
				x = parseFloat(points[i])*this.deltaWidth;
				i++;
				y = parseFloat(points[i])*this.deltaHeight;
				newPoints.push(x);
				newPoints.push(y);
			}
			
			return newPoints;
		} else {
			//TODO error
		}
	},

	/**
	 * arcAbs - A
	 * 
	 * @param {Number} rx
	 * @param {Number} ry
	 * @param {Number} xAxisRotation
	 * @param {Boolean} largeArcFlag
	 * @param {Boolean} sweepFlag
	 * @param {Number} x
	 * @param {Number} y
	 */
	arcAbs: function(rx, ry, xAxisRotation, largeArcFlag, sweepFlag, x, y) {
	    var pointsAbs = this.editPointsAbs([x, y]);
		var pointsRel = this.editPointsRel([rx, ry]);
		
		this.d = this.d.concat(" A" + pointsRel[0] + " " + pointsRel[1] + 
								" " + xAxisRotation + " " + largeArcFlag + 
								" " + sweepFlag + " " + pointsAbs[0] + " " +
								pointsAbs[1] + " ");					
	},

	/**
	 * arcRel - a
	 * 
	 * @param {Number} rx
	 * @param {Number} ry
	 * @param {Number} xAxisRotation
	 * @param {Boolean} largeArcFlag
	 * @param {Boolean} sweepFlag
	 * @param {Number} x
	 * @param {Number} y
	 */
	arcRel: function(rx, ry, xAxisRotation, largeArcFlag, sweepFlag, x, y) {
		var pointsRel = this.editPointsRel([rx, ry, x, y]);
		
		this.d = this.d.concat(" a" + pointsRel[0] + " " + pointsRel[1] + 
								" " + xAxisRotation + " " + largeArcFlag + 
								" " + sweepFlag + " " + pointsRel[2] + " " +
								pointsRel[3] + " ");	
	},

	/**
	 * curvetoCubicAbs - C
	 * 
	 * @param {Number} x1
	 * @param {Number} y1
	 * @param {Number} x2
	 * @param {Number} y2
	 * @param {Number} x
	 * @param {Number} y
	 */
	curvetoCubicAbs: function(x1, y1, x2, y2, x, y) {
	    var pointsAbs = this.editPointsAbs([x1, y1, x2, y2, x, y]);
		
		this.d = this.d.concat(" C" + pointsAbs[0] + " " + pointsAbs[1] + 
								" " + pointsAbs[2] + " " + pointsAbs[3] + 
								" " + pointsAbs[4] + " " + pointsAbs[5] + " ");	
	},

	/**
	 * curvetoCubicRel - c
	 * 
	 * @param {Number} x1
	 * @param {Number} y1
	 * @param {Number} x2
	 * @param {Number} y2
	 * @param {Number} x
	 * @param {Number} y
	 */
	curvetoCubicRel: function(x1, y1, x2, y2, x, y) {
	    var pointsRel = this.editPointsRel([x1, y1, x2, y2, x, y]);
		
		this.d = this.d.concat(" c" + pointsRel[0] + " " + pointsRel[1] + 
								" " + pointsRel[2] + " " + pointsRel[3] + 
								" " + pointsRel[4] + " " + pointsRel[5] + " ");	
	},

	/**
	 * linetoHorizontalAbs - H
	 * 
	 * @param {Number} x
	 */
	linetoHorizontalAbs: function(x) {
	    var pointsAbs = this.editPointsAbs([x, 0]);
		
		this.d = this.d.concat(" H" + pointsAbs[0] + " ");	
	},

	/**
	 * linetoHorizontalRel - h
	 * 
	 * @param {Number} x
	 */
	linetoHorizontalRel: function(x) {
	    var pointsRel = this.editPointsRel([x, 0]);
		
		this.d = this.d.concat(" h" + pointsRel[0] + " ");	
	},

	/**
	 * linetoAbs - L
	 * 
	 * @param {Number} x
	 * @param {Number} y
	 */
	linetoAbs: function(x, y) {
	    var pointsAbs = this.editPointsAbs([x, y]);
		
		this.d = this.d.concat(" L" + pointsAbs[0] + " " + pointsAbs[1] + " ");
	},

	/**
	 * linetoRel - l
	 * 
	 * @param {Number} x
	 * @param {Number} y
	 */
	linetoRel: function(x, y) {
	    var pointsRel = this.editPointsRel([x, y]);
		
		this.d = this.d.concat(" l" + pointsRel[0] + " " + pointsRel[1] + " ");
	},

	/**
	 * movetoAbs - M
	 * 
	 * @param {Number} x
	 * @param {Number} y
	 */
	movetoAbs: function(x, y) {
	    var pointsAbs = this.editPointsAbs([x, y]);
		
		this.d = this.d.concat(" M" + pointsAbs[0] + " " + pointsAbs[1] + " ");
	},

	/**
	 * movetoRel - m
	 * 
	 * @param {Number} x
	 * @param {Number} y
	 */
	movetoRel: function(x, y) {
	    var pointsRel;
		if(this.d === "") {
			pointsRel = this.editPointsAbs([x, y]);
		} else {
			pointsRel = this.editPointsRel([x, y]);
		}
		
		this.d = this.d.concat(" m" + pointsRel[0] + " " + pointsRel[1] + " ");
	},

	/**
	 * curvetoQuadraticAbs - Q
	 * 
	 * @param {Number} x1
	 * @param {Number} y1
	 * @param {Number} x
	 * @param {Number} y
	 */
	curvetoQuadraticAbs: function(x1, y1, x, y) {
	    var pointsAbs = this.editPointsAbs([x1, y1, x, y]);
		
		this.d = this.d.concat(" Q" + pointsAbs[0] + " " + pointsAbs[1] + " " +
								pointsAbs[2] + " " + pointsAbs[3] + " ");
	},

	/**
	 * curvetoQuadraticRel - q
	 * 
	 * @param {Number} x1
	 * @param {Number} y1
	 * @param {Number} x
	 * @param {Number} y
	 */
	curvetoQuadraticRel: function(x1, y1, x, y) {
	    var pointsRel = this.editPointsRel([x1, y1, x, y]);
		
		this.d = this.d.concat(" q" + pointsRel[0] + " " + pointsRel[1] + " " +
								pointsRel[2] + " " + pointsRel[3] + " ");
	},

	/**
	 * curvetoCubicSmoothAbs - S
	 * 
	 * @param {Number} x2
	 * @param {Number} y2
	 * @param {Number} x
	 * @param {Number} y
	 */
	curvetoCubicSmoothAbs: function(x2, y2, x, y) {
	    var pointsAbs = this.editPointsAbs([x2, y2, x, y]);
		
		this.d = this.d.concat(" S" + pointsAbs[0] + " " + pointsAbs[1] + " " +
								pointsAbs[2] + " " + pointsAbs[3] + " ");
	},

	/**
	 * curvetoCubicSmoothRel - s
	 * 
	 * @param {Number} x2
	 * @param {Number} y2
	 * @param {Number} x
	 * @param {Number} y
	 */
	curvetoCubicSmoothRel: function(x2, y2, x, y) {
	    var pointsRel = this.editPointsRel([x2, y2, x, y]);
		
		this.d = this.d.concat(" s" + pointsRel[0] + " " + pointsRel[1] + " " +
								pointsRel[2] + " " + pointsRel[3] + " ");
	},

	/**
	 * curvetoQuadraticSmoothAbs - T
	 * 
	 * @param {Number} x
	 * @param {Number} y
	 */
	curvetoQuadraticSmoothAbs: function(x, y) {
	    var pointsAbs = this.editPointsAbs([x, y]);
		
		this.d = this.d.concat(" T" + pointsAbs[0] + " " + pointsAbs[1] + " ");
	},

	/**
	 * curvetoQuadraticSmoothRel - t
	 * 
	 * @param {Number} x
	 * @param {Number} y
	 */
	curvetoQuadraticSmoothRel: function(x, y) {
	    var pointsRel = this.editPointsRel([x, y]);
		
		this.d = this.d.concat(" t" + pointsRel[0] + " " + pointsRel[1] + " ");
	},

	/**
	 * linetoVerticalAbs - V
	 * 
	 * @param {Number} y
	 */
	linetoVerticalAbs: function(y) {
	    var pointsAbs = this.editPointsAbs([0, y]);
		
		this.d = this.d.concat(" V" + pointsAbs[1] + " ");
	},

	/**
	 * linetoVerticalRel - v
	 * 
	 * @param {Number} y
	 */
	linetoVerticalRel: function(y) {
	    var pointsRel = this.editPointsRel([0, y]);
		
		this.d = this.d.concat(" v" + pointsRel[1] + " ");
	},

	/**
	 * closePath - z or Z
	 */
	closePath: function() {
	    this.d = this.d.concat(" z");
	}

});/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

/**
 * Init namespaces
 */
if(!ORYX) {var ORYX = {};}
if(!ORYX.Core) {ORYX.Core = {};}
if(!ORYX.Core.SVG) {ORYX.Core.SVG = {};}


/**
 * MinMaxPathHandler
 * 
 * Determine the minimum and maximum of a SVG path's absolute coordinates.
 * For relative coordinates the absolute value is computed for consideration.
 * The values are stored in attributes minX, minY, maxX, and maxY.
 * 
 * @constructor
 */
ORYX.Core.SVG.MinMaxPathHandler = Clazz.extend({
	
	construct: function() {
		arguments.callee.$.construct.apply(this, arguments);
		
		this.minX = undefined;
		this.minY = undefined;
		this.maxX = undefined;
		this.maxY = undefined;
		
		this._lastAbsX = undefined;
		this._lastAbsY = undefined;
	},

	/**
	 * Store minimal and maximal coordinates of passed points to attributes minX, maxX, minY, maxY
	 * 
	 * @param {Array} points Array of absolutePoints
	 */
	calculateMinMax: function(points) {
		if(points instanceof Array) {
			var x, y;
			for(var i = 0; i < points.length; i++) {
				x = parseFloat(points[i]);
				i++;
				y = parseFloat(points[i]);
				
				this.minX = (this.minX !== undefined) ? Math.min(this.minX, x) : x;
				this.maxX = (this.maxX !== undefined) ? Math.max(this.maxX, x) : x;
				this.minY = (this.minY !== undefined) ? Math.min(this.minY, y) : y;
				this.maxY = (this.maxY !== undefined) ? Math.max(this.maxY, y) : y;
					
				this._lastAbsX = x;
				this._lastAbsY = y;
			}
		} else {
			//TODO error
		}
	},

	/**
	 * arcAbs - A
	 * 
	 * @param {Number} rx
	 * @param {Number} ry
	 * @param {Number} xAxisRotation
	 * @param {Boolean} largeArcFlag
	 * @param {Boolean} sweepFlag
	 * @param {Number} x
	 * @param {Number} y
	 */
	arcAbs: function(rx, ry, xAxisRotation, largeArcFlag, sweepFlag, x, y) {
	    this.calculateMinMax([x, y]);
	},

	/**
	 * arcRel - a
	 * 
	 * @param {Number} rx
	 * @param {Number} ry
	 * @param {Number} xAxisRotation
	 * @param {Boolean} largeArcFlag
	 * @param {Boolean} sweepFlag
	 * @param {Number} x
	 * @param {Number} y
	 */
	arcRel: function(rx, ry, xAxisRotation, largeArcFlag, sweepFlag, x, y) {
	    this.calculateMinMax([this._lastAbsX + x, this._lastAbsY + y]);
	},

	/**
	 * curvetoCubicAbs - C
	 * 
	 * @param {Number} x1
	 * @param {Number} y1
	 * @param {Number} x2
	 * @param {Number} y2
	 * @param {Number} x
	 * @param {Number} y
	 */
	curvetoCubicAbs: function(x1, y1, x2, y2, x, y) {
	    this.calculateMinMax([x1, y1, x2, y2, x, y]);
	},

	/**
	 * curvetoCubicRel - c
	 * 
	 * @param {Number} x1
	 * @param {Number} y1
	 * @param {Number} x2
	 * @param {Number} y2
	 * @param {Number} x
	 * @param {Number} y
	 */
	curvetoCubicRel: function(x1, y1, x2, y2, x, y) {
	    this.calculateMinMax([this._lastAbsX + x1, this._lastAbsY + y1,
							  this._lastAbsX + x2, this._lastAbsY + y2,
							  this._lastAbsX + x, this._lastAbsY + y]);
	},

	/**
	 * linetoHorizontalAbs - H
	 * 
	 * @param {Number} x
	 */
	linetoHorizontalAbs: function(x) {
	    this.calculateMinMax([x, this._lastAbsY]);
	},

	/**
	 * linetoHorizontalRel - h
	 * 
	 * @param {Number} x
	 */
	linetoHorizontalRel: function(x) {
	    this.calculateMinMax([this._lastAbsX + x, this._lastAbsY]);
	},

	/**
	 * linetoAbs - L
	 * 
	 * @param {Number} x
	 * @param {Number} y
	 */
	linetoAbs: function(x, y) {
	    this.calculateMinMax([x, y]);
	},

	/**
	 * linetoRel - l
	 * 
	 * @param {Number} x
	 * @param {Number} y
	 */
	linetoRel: function(x, y) {
	    this.calculateMinMax([this._lastAbsX + x, this._lastAbsY + y]);
	},

	/**
	 * movetoAbs - M
	 * 
	 * @param {Number} x
	 * @param {Number} y
	 */
	movetoAbs: function(x, y) {
	    this.calculateMinMax([x, y]);
	},

	/**
	 * movetoRel - m
	 * 
	 * @param {Number} x
	 * @param {Number} y
	 */
	movetoRel: function(x, y) {
	    if(this._lastAbsX && this._lastAbsY) {
			this.calculateMinMax([this._lastAbsX + x, this._lastAbsY + y]);
		} else {
			this.calculateMinMax([x, y]);
		}
	},

	/**
	 * curvetoQuadraticAbs - Q
	 * 
	 * @param {Number} x1
	 * @param {Number} y1
	 * @param {Number} x
	 * @param {Number} y
	 */
	curvetoQuadraticAbs: function(x1, y1, x, y) {
	    this.calculateMinMax([x1, y1, x, y]);
	},

	/**
	 * curvetoQuadraticRel - q
	 * 
	 * @param {Number} x1
	 * @param {Number} y1
	 * @param {Number} x
	 * @param {Number} y
	 */
	curvetoQuadraticRel: function(x1, y1, x, y) {
	    this.calculateMinMax([this._lastAbsX + x1, this._lastAbsY + y1, this._lastAbsX + x, this._lastAbsY + y]);
	},

	/**
	 * curvetoCubicSmoothAbs - S
	 * 
	 * @param {Number} x2
	 * @param {Number} y2
	 * @param {Number} x
	 * @param {Number} y
	 */
	curvetoCubicSmoothAbs: function(x2, y2, x, y) {
	    this.calculateMinMax([x2, y2, x, y]);
	},

	/**
	 * curvetoCubicSmoothRel - s
	 * 
	 * @param {Number} x2
	 * @param {Number} y2
	 * @param {Number} x
	 * @param {Number} y
	 */
	curvetoCubicSmoothRel: function(x2, y2, x, y) {
	    this.calculateMinMax([this._lastAbsX + x2, this._lastAbsY + y2, this._lastAbsX + x, this._lastAbsY + y]);
	},

	/**
	 * curvetoQuadraticSmoothAbs - T
	 * 
	 * @param {Number} x
	 * @param {Number} y
	 */
	curvetoQuadraticSmoothAbs: function(x, y) {
	    this.calculateMinMax([x, y]);
	},

	/**
	 * curvetoQuadraticSmoothRel - t
	 * 
	 * @param {Number} x
	 * @param {Number} y
	 */
	curvetoQuadraticSmoothRel: function(x, y) {
	    this.calculateMinMax([this._lastAbsX + x, this._lastAbsY + y]);
	},

	/**
	 * linetoVerticalAbs - V
	 * 
	 * @param {Number} y
	 */
	linetoVerticalAbs: function(y) {
	    this.calculateMinMax([this._lastAbsX, y]);
	},

	/**
	 * linetoVerticalRel - v
	 * 
	 * @param {Number} y
	 */
	linetoVerticalRel: function(y) {
	    this.calculateMinMax([this._lastAbsX, this._lastAbsY + y]);
	},

	/**
	 * closePath - z or Z
	 */
	closePath: function() {
	    return;// do nothing
	}

});/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

/**
 * Init namespaces
 */
if(!ORYX) {var ORYX = {};}
if(!ORYX.Core) {ORYX.Core = {};}
if(!ORYX.Core.SVG) {ORYX.Core.SVG = {};}


/**
 * PathHandler
 * 
 * Determine absolute points of a SVG path. The coordinates are stored 
 * sequentially in the attribute points (x-coordinates at even indices,
 * y-coordinates at odd indices).
 * 
 * @constructor
 */
ORYX.Core.SVG.PointsPathHandler = Clazz.extend({
	
	construct: function() {
		arguments.callee.$.construct.apply(this, arguments);
		
		this.points = [];
		
		this._lastAbsX = undefined;
		this._lastAbsY = undefined;
	},

	/**
	 * addPoints
	 * 
	 * @param {Array} points Array of absolutePoints
	 */
	addPoints: function(points) {
		if(points instanceof Array) {
			var x, y;
			for(var i = 0; i < points.length; i++) {
				x = parseFloat(points[i]);
				i++;
				y = parseFloat(points[i]);
				
				this.points.push(x);
				this.points.push(y);
				//this.points.push({x:x, y:y});
					
				this._lastAbsX = x;
				this._lastAbsY = y;
			}
		} else {
			//TODO error
		}
	},

	/**
	 * arcAbs - A
	 * 
	 * @param {Number} rx
	 * @param {Number} ry
	 * @param {Number} xAxisRotation
	 * @param {Boolean} largeArcFlag
	 * @param {Boolean} sweepFlag
	 * @param {Number} x
	 * @param {Number} y
	 */
	arcAbs: function(rx, ry, xAxisRotation, largeArcFlag, sweepFlag, x, y) {
	    this.addPoints([x, y]);
	},

	/**
	 * arcRel - a
	 * 
	 * @param {Number} rx
	 * @param {Number} ry
	 * @param {Number} xAxisRotation
	 * @param {Boolean} largeArcFlag
	 * @param {Boolean} sweepFlag
	 * @param {Number} x
	 * @param {Number} y
	 */
	arcRel: function(rx, ry, xAxisRotation, largeArcFlag, sweepFlag, x, y) {
	    this.addPoints([this._lastAbsX + x, this._lastAbsY + y]);
	},

	/**
	 * curvetoCubicAbs - C
	 * 
	 * @param {Number} x1
	 * @param {Number} y1
	 * @param {Number} x2
	 * @param {Number} y2
	 * @param {Number} x
	 * @param {Number} y
	 */
	curvetoCubicAbs: function(x1, y1, x2, y2, x, y) {
	    this.addPoints([x, y]);
	},

	/**
	 * curvetoCubicRel - c
	 * 
	 * @param {Number} x1
	 * @param {Number} y1
	 * @param {Number} x2
	 * @param {Number} y2
	 * @param {Number} x
	 * @param {Number} y
	 */
	curvetoCubicRel: function(x1, y1, x2, y2, x, y) {
	    this.addPoints([this._lastAbsX + x, this._lastAbsY + y]);
	},

	/**
	 * linetoHorizontalAbs - H
	 * 
	 * @param {Number} x
	 */
	linetoHorizontalAbs: function(x) {
	    this.addPoints([x, this._lastAbsY]);
	},

	/**
	 * linetoHorizontalRel - h
	 * 
	 * @param {Number} x
	 */
	linetoHorizontalRel: function(x) {
	    this.addPoints([this._lastAbsX + x, this._lastAbsY]);
	},

	/**
	 * linetoAbs - L
	 * 
	 * @param {Number} x
	 * @param {Number} y
	 */
	linetoAbs: function(x, y) {
	    this.addPoints([x, y]);
	},

	/**
	 * linetoRel - l
	 * 
	 * @param {Number} x
	 * @param {Number} y
	 */
	linetoRel: function(x, y) {
	    this.addPoints([this._lastAbsX + x, this._lastAbsY + y]);
	},

	/**
	 * movetoAbs - M
	 * 
	 * @param {Number} x
	 * @param {Number} y
	 */
	movetoAbs: function(x, y) {
	    this.addPoints([x, y]);
	},

	/**
	 * movetoRel - m
	 * 
	 * @param {Number} x
	 * @param {Number} y
	 */
	movetoRel: function(x, y) {
	    if(this._lastAbsX && this._lastAbsY) {
			this.addPoints([this._lastAbsX + x, this._lastAbsY + y]);
		} else {
			this.addPoints([x, y]);
		}
	},

	/**
	 * curvetoQuadraticAbs - Q
	 * 
	 * @param {Number} x1
	 * @param {Number} y1
	 * @param {Number} x
	 * @param {Number} y
	 */
	curvetoQuadraticAbs: function(x1, y1, x, y) {
	    this.addPoints([x, y]);
	},

	/**
	 * curvetoQuadraticRel - q
	 * 
	 * @param {Number} x1
	 * @param {Number} y1
	 * @param {Number} x
	 * @param {Number} y
	 */
	curvetoQuadraticRel: function(x1, y1, x, y) {
	    this.addPoints([this._lastAbsX + x, this._lastAbsY + y]);
	},

	/**
	 * curvetoCubicSmoothAbs - S
	 * 
	 * @param {Number} x2
	 * @param {Number} y2
	 * @param {Number} x
	 * @param {Number} y
	 */
	curvetoCubicSmoothAbs: function(x2, y2, x, y) {
	    this.addPoints([x, y]);
	},

	/**
	 * curvetoCubicSmoothRel - s
	 * 
	 * @param {Number} x2
	 * @param {Number} y2
	 * @param {Number} x
	 * @param {Number} y
	 */
	curvetoCubicSmoothRel: function(x2, y2, x, y) {
	    this.addPoints([this._lastAbsX + x, this._lastAbsY + y]);
	},

	/**
	 * curvetoQuadraticSmoothAbs - T
	 * 
	 * @param {Number} x
	 * @param {Number} y
	 */
	curvetoQuadraticSmoothAbs: function(x, y) {
	    this.addPoints([x, y]);
	},

	/**
	 * curvetoQuadraticSmoothRel - t
	 * 
	 * @param {Number} x
	 * @param {Number} y
	 */
	curvetoQuadraticSmoothRel: function(x, y) {
	    this.addPoints([this._lastAbsX + x, this._lastAbsY + y]);
	},

	/**
	 * linetoVerticalAbs - V
	 * 
	 * @param {Number} y
	 */
	linetoVerticalAbs: function(y) {
	    this.addPoints([this._lastAbsX, y]);
	},

	/**
	 * linetoVerticalRel - v
	 * 
	 * @param {Number} y
	 */
	linetoVerticalRel: function(y) {
	    this.addPoints([this._lastAbsX, this._lastAbsY + y]);
	},

	/**
	 * closePath - z or Z
	 */
	closePath: function() {
	    return;// do nothing
	}

});/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

/**
 *
 * Config variables
 */
NAMESPACE_ORYX = "http://www.b3mn.org/oryx";
NAMESPACE_SVG = "http://www.w3.org/2000/svg/";

/**
 * @classDescription This class wraps the manipulation of a SVG marker.
 * @namespace ORYX.Core.SVG
 * uses Inheritance (Clazz)
 * uses Prototype 1.5.0
 *
 */

/**
 * Init package
 */
if(!ORYX) {var ORYX = {};}
if(!ORYX.Core) {ORYX.Core = {};}
if(!ORYX.Core.SVG) {ORYX.Core.SVG = {};}

ORYX.Core.SVG.SVGMarker = Clazz.extend({

	/**
	 * Constructor
	 * @param markerElement {SVGMarkerElement}
	 */
	construct: function(markerElement) {
		arguments.callee.$.construct.apply(this, arguments);

		this.id = undefined;
		this.element = markerElement;
		this.refX = undefined;
		this.refY = undefined;
		this.markerWidth = undefined;
		this.markerHeight = undefined;
		this.oldRefX = undefined;
		this.oldRefY = undefined;
		this.oldMarkerWidth = undefined;
		this.oldMarkerHeight = undefined;
		this.optional = false;
		this.enabled = true;
		this.minimumLength = undefined;
		this.resize = false;

		this.svgShapes = [];

		this._init(); //initialisation of all the properties declared above.
	},

	/**
	 * Initializes the values that are defined in the constructor.
	 */
	_init: function() {
		//check if this.element is a SVGMarkerElement
		if(!( this.element == "[object SVGMarkerElement]")) {
			throw "SVGMarker: Argument is not an instance of SVGMarkerElement.";
		}

		this.id = this.element.getAttributeNS(null, "id");
		
		//init svg marker attributes
		var refXValue = this.element.getAttributeNS(null, "refX");
		if(refXValue) {
			this.refX = parseFloat(refXValue);
		} else {
			this.refX = 0;
		}
		var refYValue = this.element.getAttributeNS(null, "refY");
		if(refYValue) {
			this.refY = parseFloat(refYValue);
		} else {
			this.refY = 0;
		}
		var markerWidthValue = this.element.getAttributeNS(null, "markerWidth");
		if(markerWidthValue) {
			this.markerWidth = parseFloat(markerWidthValue);
		} else {
			this.markerWidth = 3;
		}
		var markerHeightValue = this.element.getAttributeNS(null, "markerHeight");
		if(markerHeightValue) {
			this.markerHeight = parseFloat(markerHeightValue);
		} else {
			this.markerHeight = 3;
		}

		this.oldRefX = this.refX;
		this.oldRefY = this.refY;
		this.oldMarkerWidth = this.markerWidth;
		this.oldMarkerHeight = this.markerHeight;

		//init oryx attributes
		var optionalAttr = this.element.getAttributeNS(NAMESPACE_ORYX, "optional");
		if(optionalAttr) {
			optionalAttr = optionalAttr.strip();
			this.optional = (optionalAttr.toLowerCase() === "yes");
		} else {
			this.optional = false;
		}

		var enabledAttr = this.element.getAttributeNS(NAMESPACE_ORYX, "enabled");
		if(enabledAttr) {
			enabledAttr = enabledAttr.strip();
			this.enabled = !(enabledAttr.toLowerCase() === "no");
		} else {
			this.enabled = true;
		}

		var minLengthAttr = this.element.getAttributeNS(NAMESPACE_ORYX, "minimumLength");
		if(minLengthAttr) {
			this.minimumLength = parseFloat(minLengthAttr);
		}

		var resizeAttr = this.element.getAttributeNS(NAMESPACE_ORYX, "resize");
		if(resizeAttr) {
			resizeAttr = resizeAttr.strip();
			this.resize = (resizeAttr.toLowerCase() === "yes");
		} else {
			this.resize = false;
		}

		//init SVGShape objects
		//this.svgShapes = this._getSVGShapes(this.element);
	},

	/**
	 *
	 */
	_getSVGShapes: function(svgElement) {
		if(svgElement.hasChildNodes) {
			var svgShapes = [];
			var me = this;
			$A(svgElement.childNodes).each(function(svgChild) {
				try {
					var svgShape = new ORYX.Core.SVG.SVGShape(svgChild);
					svgShapes.push(svgShape);
				} catch (e) {
					svgShapes = svgShapes.concat(me._getSVGShapes(svgChild));
				}
			});
			return svgShapes;
		}
	},

	/**
	 * Writes the changed values into the SVG marker.
	 */
	update: function() {
		//TODO mache marker resizebar!!! aber erst wenn der rest der connectingshape funzt!

//		//update marker attributes
//		if(this.refX != this.oldRefX) {
//			this.element.setAttributeNS(null, "refX", this.refX);
//		}
//		if(this.refY != this.oldRefY) {
//			this.element.setAttributeNS(null, "refY", this.refY);
//		}
//		if(this.markerWidth != this.oldMarkerWidth) {
//			this.element.setAttributeNS(null, "markerWidth", this.markerWidth);
//		}
//		if(this.markerHeight != this.oldMarkerHeight) {
//			this.element.setAttributeNS(null, "markerHeight", this.markerHeight);
//		}
//
//		//update SVGShape objects
//		var widthDelta = this.markerWidth / this.oldMarkerWidth;
//		var heightDelta = this.markerHeight / this.oldMarkerHeight;
//		if(widthDelta != 1 && heightDelta != 1) {
//			this.svgShapes.each(function(svgShape) {
//
//			});
//		}

		//update old values to prepare the next update
		this.oldRefX = this.refX;
		this.oldRefY = this.refY;
		this.oldMarkerWidth = this.markerWidth;
		this.oldMarkerHeight = this.markerHeight;
	},
	
	toString: function() { return (this.element) ? "SVGMarker " + this.element.id : "SVGMarker " + this.element;}
 });/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

/**
 *
 * Config variables
 */
NAMESPACE_ORYX = "http://www.b3mn.org/oryx";
NAMESPACE_SVG = "http://www.w3.org/2000/svg/";

/**
 * @classDescription This class wraps the manipulation of a SVG basic shape or a path.
 * @namespace ORYX.Core.SVG
 * uses Inheritance (Clazz)
 * uses Prototype 1.5.0
 * uses PathParser by Kevin Lindsey (http://kevlindev.com/)
 * uses MinMaxPathHandler
 * uses EditPathHandler
 *
 */

//init package
if(!ORYX) {var ORYX = {};}
if(!ORYX.Core) {ORYX.Core = {};}
if(!ORYX.Core.SVG) {ORYX.Core.SVG = {};}

ORYX.Core.SVG.SVGShape = Clazz.extend({

	/**
	 * Constructor
	 * @param svgElem {SVGElement} An SVGElement that is a basic shape or a path.
	 */
	construct: function(svgElem) {
		arguments.callee.$.construct.apply(this, arguments);

		this.type;
		this.element = svgElem;
		this.x = undefined;
		this.y = undefined;
		this.width = undefined;
		this.height = undefined;
		this.oldX = undefined;
		this.oldY = undefined;
		this.oldWidth = undefined;
		this.oldHeight = undefined;
		this.radiusX = undefined;
		this.radiusY = undefined;
		this.isHorizontallyResizable = false;
		this.isVerticallyResizable = false;
		//this.anchors = [];
		this.anchorLeft = false;
		this.anchorRight = false;
		this.anchorTop = false;
		this.anchorBottom = false;
		
		//attributes of path elements of edge objects
		this.allowDockers = true;
		this.resizeMarkerMid = false;

		this.editPathParser;
		this.editPathHandler;

		this.init(); //initialisation of all the properties declared above.
	},

	/**
	 * Initializes the values that are defined in the constructor.
	 */
	init: function() {

		/**initialize position and size*/
		if(ORYX.Editor.checkClassType(this.element, SVGRectElement) || ORYX.Editor.checkClassType(this.element, SVGImageElement)) {
			this.type = "Rect";
			
			var xAttr = this.element.getAttributeNS(null, "x");
			if(xAttr) {
				this.oldX = parseFloat(xAttr);
			} else {
				throw "Missing attribute in element " + this.element;
			}
			var yAttr = this.element.getAttributeNS(null, "y");
			if(yAttr) {
				this.oldY = parseFloat(yAttr);
			} else {
				throw "Missing attribute in element " + this.element;
			}
			var widthAttr = this.element.getAttributeNS(null, "width");
			if(widthAttr) {
				this.oldWidth = parseFloat(widthAttr);
			} else {
				throw "Missing attribute in element " + this.element;
			}
			var heightAttr = this.element.getAttributeNS(null, "height");
			if(heightAttr) {
				this.oldHeight = parseFloat(heightAttr);
			} else {
				throw "Missing attribute in element " + this.element;
			}

		} else if(ORYX.Editor.checkClassType(this.element, SVGCircleElement)) {
			this.type = "Circle";
			
			var cx = undefined;
			var cy = undefined;
			//var r = undefined;

			var cxAttr = this.element.getAttributeNS(null, "cx");
			if(cxAttr) {
				cx = parseFloat(cxAttr);
			} else {
				throw "Missing attribute in element " + this.element;
			}
			var cyAttr = this.element.getAttributeNS(null, "cy");
			if(cyAttr) {
				cy = parseFloat(cyAttr);
			} else {
				throw "Missing attribute in element " + this.element;
			}
			var rAttr = this.element.getAttributeNS(null, "r");
			if(rAttr) {
				//r = parseFloat(rAttr);
				this.radiusX = parseFloat(rAttr);
			} else {
				throw "Missing attribute in element " + this.element;
			}
			this.oldX = cx - this.radiusX;
			this.oldY = cy - this.radiusX;
			this.oldWidth = 2*this.radiusX;
			this.oldHeight = 2*this.radiusX;

		} else if(ORYX.Editor.checkClassType(this.element, SVGEllipseElement)) {
			this.type = "Ellipse";
			
			var cx = undefined;
			var cy = undefined;
			//var rx = undefined;
			//var ry = undefined;
			var cxAttr = this.element.getAttributeNS(null, "cx");
			if(cxAttr) {
				cx = parseFloat(cxAttr);
			} else {
				throw "Missing attribute in element " + this.element;
			}
			var cyAttr = this.element.getAttributeNS(null, "cy");
			if(cyAttr) {
				cy = parseFloat(cyAttr);
			} else {
				throw "Missing attribute in element " + this.element;
			}
			var rxAttr = this.element.getAttributeNS(null, "rx");
			if(rxAttr) {
				this.radiusX = parseFloat(rxAttr);
			} else {
				throw "Missing attribute in element " + this.element;
			}
			var ryAttr = this.element.getAttributeNS(null, "ry");
			if(ryAttr) {
				this.radiusY = parseFloat(ryAttr);
			} else {
				throw "Missing attribute in element " + this.element;
			}
			this.oldX = cx - this.radiusX;
			this.oldY = cy - this.radiusY;
			this.oldWidth = 2*this.radiusX;
			this.oldHeight = 2*this.radiusY;

		} else if(ORYX.Editor.checkClassType(this.element, SVGLineElement)) {
			this.type = "Line";
			
			var x1 = undefined;
			var y1 = undefined;
			var x2 = undefined;
			var y2 = undefined;
			var x1Attr = this.element.getAttributeNS(null, "x1");
			if(x1Attr) {
				x1 = parseFloat(x1Attr);
			} else {
				throw "Missing attribute in element " + this.element;
			}
			var y1Attr = this.element.getAttributeNS(null, "y1");
			if(y1Attr) {
				y1 = parseFloat(y1Attr);
			} else {
				throw "Missing attribute in element " + this.element;
			}
			var x2Attr = this.element.getAttributeNS(null, "x2");
			if(x2Attr) {
				x2 = parseFloat(x2Attr);
			} else {
				throw "Missing attribute in element " + this.element;
			}
			var y2Attr = this.element.getAttributeNS(null, "y2");
			if(y2Attr) {
				y2 = parseFloat(y2Attr);
			} else {
				throw "Missing attribute in element " + this.element;
			}
			this.oldX = Math.min(x1,x2);
			this.oldY = Math.min(y1,y2);
			this.oldWidth = Math.abs(x1-x2);
			this.oldHeight = Math.abs(y1-y2);

		} else if(ORYX.Editor.checkClassType(this.element, SVGPolylineElement) || ORYX.Editor.checkClassType(this.element, SVGPolygonElement)) {
			this.type = "Polyline";
			
			var pointsArray = [];
			if (this.element.points&&this.element.points.numberOfItems){
			    for(var i=0, size=this.element.points.numberOfItems; i<size; i++){
			        pointsArray.push(this.element.points.getItem(i).x)
			        pointsArray.push(this.element.points.getItem(i).y)
			    }
			} else {
				var points = this.element.getAttributeNS(null, "points");
				if(points) {
					points = points.replace(/,/g , " ");
					pointsArray = points.split(" ");
					pointsArray = pointsArray.without("");
				} else {
					throw "Missing attribute in element " + this.element;
				}
			}
			

			if(pointsArray && pointsArray.length && pointsArray.length > 1) {
				var minX = parseFloat(pointsArray[0]);
				var minY = parseFloat(pointsArray[1]);
				var maxX = parseFloat(pointsArray[0]);
				var maxY = parseFloat(pointsArray[1]);

				for(var i = 0; i < pointsArray.length; i++) {
					minX = Math.min(minX, parseFloat(pointsArray[i]));
					maxX = Math.max(maxX, parseFloat(pointsArray[i]));
					i++;
					minY = Math.min(minY, parseFloat(pointsArray[i]));
					maxY = Math.max(maxY, parseFloat(pointsArray[i]));
				}

				this.oldX = minX;
				this.oldY = minY;
				this.oldWidth = maxX-minX;
				this.oldHeight = maxY-minY;
			} else {
				throw "Missing attribute in element " + this.element;
			}

		} else if(ORYX.Editor.checkClassType(this.element, SVGPathElement)) {
			this.type = "Path";
			
			this.editPathParser = new PathParser();
			this.editPathHandler = new ORYX.Core.SVG.EditPathHandler();
			this.editPathParser.setHandler(this.editPathHandler);
		
			var parser = new PathParser();
			var handler = new ORYX.Core.SVG.MinMaxPathHandler();
			parser.setHandler(handler);
			parser.parsePath(this.element);

			this.oldX = handler.minX;
			this.oldY = handler.minY;
			this.oldWidth = handler.maxX - handler.minX;
			this.oldHeight = handler.maxY - handler.minY;

			delete parser;
			delete handler;
		} else {
			throw "Element is not a shape.";
		}

		/** initialize attributes of oryx namespace */
		//resize
		var resizeAttr = this.element.getAttributeNS(NAMESPACE_ORYX, "resize");
		if(resizeAttr) {
			resizeAttr = resizeAttr.toLowerCase();
			if(resizeAttr.match(/horizontal/)) {
				this.isHorizontallyResizable = true;
			} else {
				this.isHorizontallyResizable = false;
			}
			if(resizeAttr.match(/vertical/)) {
				this.isVerticallyResizable = true;
			} else {
				this.isVerticallyResizable = false;
			}
		} else {
			this.isHorizontallyResizable = false;
			this.isVerticallyResizable = false;
		}

		//anchors
		var anchorAttr = this.element.getAttributeNS(NAMESPACE_ORYX, "anchors");
		if(anchorAttr) {
			anchorAttr = anchorAttr.replace("/,/g", " ");
			var anchors = anchorAttr.split(" ").without("");
			
			for(var i = 0; i < anchors.length; i++) {
				switch(anchors[i].toLowerCase()) {
					case "left":
						this.anchorLeft = true;
						break;
					case "right":
						this.anchorRight = true;
						break;
					case "top":
						this.anchorTop = true;
						break;
					case "bottom":
						this.anchorBottom = true;
						break;
				}
			}
		}
		
		//allowDockers and resizeMarkerMid
		if(ORYX.Editor.checkClassType(this.element, SVGPathElement)) {
			var allowDockersAttr = this.element.getAttributeNS(NAMESPACE_ORYX, "allowDockers"); 
			if(allowDockersAttr) {
				if(allowDockersAttr.toLowerCase() === "no") {
					this.allowDockers = false; 
				} else {
					this.allowDockers = true;
				}
			}
			
			var resizeMarkerMidAttr = this.element.getAttributeNS(NAMESPACE_ORYX, "resizeMarker-mid"); 
			if(resizeMarkerMidAttr) {
				if(resizeMarkerMidAttr.toLowerCase() === "yes") {
					this.resizeMarkerMid = true; 
				} else {
					this.resizeMarkerMid = false;
				}
			}
		}	
			
		this.x = this.oldX;
		this.y = this.oldY;
		this.width = this.oldWidth;
		this.height = this.oldHeight;
	},

	/**
	 * Writes the changed values into the SVG element.
	 */
	update: function() {
		
		if(this.x !== this.oldX || this.y !== this.oldY || this.width !== this.oldWidth || this.height !== this.oldHeight) {
			switch(this.type) {
				case "Rect":
					if(this.x !== this.oldX) this.element.setAttributeNS(null, "x", this.x);
					if(this.y !== this.oldY) this.element.setAttributeNS(null, "y", this.y);
				 	if(this.width !== this.oldWidth) this.element.setAttributeNS(null, "width", this.width);
					if(this.height !== this.oldHeight) this.element.setAttributeNS(null, "height", this.height);
					break;
				case "Circle":
					//calculate the radius
					//var r;
//					if(this.width/this.oldWidth <= this.height/this.oldHeight) {
//						this.radiusX = ((this.width > this.height) ? this.width : this.height)/2.0;
//					} else {
					 	this.radiusX = ((this.width < this.height) ? this.width : this.height)/2.0;
					//}
	
					this.element.setAttributeNS(null, "cx", this.x + this.width/2.0);
					this.element.setAttributeNS(null, "cy", this.y + this.height/2.0);
					this.element.setAttributeNS(null, "r", this.radiusX);
					break;
				case "Ellipse":
					this.radiusX = this.width/2;
					this.radiusY = this.height/2;
	
					this.element.setAttributeNS(null, "cx", this.x + this.radiusX);
					this.element.setAttributeNS(null, "cy", this.y + this.radiusY);
					this.element.setAttributeNS(null, "rx", this.radiusX);
					this.element.setAttributeNS(null, "ry", this.radiusY);
					break;
				case "Line":
					if(this.x !== this.oldX)
						this.element.setAttributeNS(null, "x1", this.x);
						
					if(this.y !== this.oldY)
						this.element.setAttributeNS(null, "y1", this.y);
						
					if(this.x !== this.oldX || this.width !== this.oldWidth)
						this.element.setAttributeNS(null, "x2", this.x + this.width);
					
					if(this.y !== this.oldY || this.height !== this.oldHeight)
						this.element.setAttributeNS(null, "y2", this.y + this.height);
					break;
				case "Polyline":
					var points = this.element.getAttributeNS(null, "points");
					if(points) {
						points = points.replace(/,/g, " ").split(" ").without("");
	
						if(points && points.length && points.length > 1) {
	
							//TODO what if oldWidth == 0?
							var widthDelta = (this.oldWidth === 0) ? 0 : this.width / this.oldWidth;
						    var heightDelta = (this.oldHeight === 0) ? 0 : this.height / this.oldHeight;
	
							var updatedPoints = "";
						    for(var i = 0; i < points.length; i++) {
								var x = (parseFloat(points[i])-this.oldX)*widthDelta + this.x;
								i++;
								var y = (parseFloat(points[i])-this.oldY)*heightDelta + this.y;
		    					updatedPoints += x + " " + y + " ";
						    }
							this.element.setAttributeNS(null, "points", updatedPoints);
						} else {
							//TODO error
						}
					} else {
						//TODO error
					}
					break;
				case "Path":
					//calculate scaling delta
					//TODO what if oldWidth == 0?
					var widthDelta = (this.oldWidth === 0) ? 0 : this.width / this.oldWidth;
					var heightDelta = (this.oldHeight === 0) ? 0 : this.height / this.oldHeight;
	
					//use path parser to edit each point of the path
					this.editPathHandler.init(this.x, this.y, this.oldX, this.oldY, widthDelta, heightDelta);
					this.editPathParser.parsePath(this.element);
	
					//change d attribute of path
					this.element.setAttributeNS(null, "d", this.editPathHandler.d);
					break;
			}

			this.oldX = this.x;
			this.oldY = this.y;
			this.oldWidth = this.width;
			this.oldHeight = this.height;
		}
		
		// Remove cached variables
		delete this.visible;
		delete this.handler;
	},
	
	isPointIncluded: function(pointX, pointY) {

		// Check if there are the right arguments and if the node is visible
		if(!pointX || !pointY || !this.isVisible()) {
			return false;
		}

		switch(this.type) {
			case "Rect":
				return (pointX >= this.x && pointX <= this.x + this.width &&
						pointY >= this.y && pointY <= this.y+this.height);
				break;
			case "Circle":
				//calculate the radius
//				var r;
//				if(this.width/this.oldWidth <= this.height/this.oldHeight) {
//					r = ((this.width > this.height) ? this.width : this.height)/2.0;
//				} else {
//				 	r = ((this.width < this.height) ? this.width : this.height)/2.0;
//				}
				return ORYX.Core.Math.isPointInEllipse(pointX, pointY, this.x + this.width/2.0, this.y + this.height/2.0, this.radiusX, this.radiusX);
				break;
			case "Ellipse":
				return ORYX.Core.Math.isPointInEllipse(pointX, pointY, this.x + this.radiusX, this.y + this.radiusY, this.radiusX, this.radiusY);			
				break;
			case "Line":
				return ORYX.Core.Math.isPointInLine(pointX, pointY, this.x, this.y, this.x + this.width, this.y + this.height);
				break;
			case "Polyline":
				var points = this.element.getAttributeNS(null, "points");
	
				if(points) {
					points = points.replace(/,/g , " ").split(" ").without("");
	
					points = points.collect(function(n) {
						return parseFloat(n);
					});
					
					return ORYX.Core.Math.isPointInPolygone(pointX, pointY, points);
				} else {
					return false;
				}
				break;
			case "Path":
				
				// Cache Path handler
				if (!this.handler) {
					var parser = new PathParser();
					this.handler = new ORYX.Core.SVG.PointsPathHandler();
					parser.setHandler(this.handler);
					parser.parsePath(this.element);
				}
				
				return ORYX.Core.Math.isPointInPolygone(pointX, pointY, this.handler.points);

				break;
			default:
				return false;
		}
	},

	/**
	 * Returns true if the element is visible
	 * @param {SVGElement} elem
	 * @return boolean
	 */
	isVisible: function(elem) {
		
		if (this.visible !== undefined){
			return this.visible;
		}
			
		if (!elem) {
			elem = this.element;
		}

		var hasOwnerSVG = false;
		try { 
			hasOwnerSVG = !!elem.ownerSVGElement;
		} catch(e){}
		
		// Is SVG context
		if ( hasOwnerSVG ) {
			// IF G-Element
			if (ORYX.Editor.checkClassType(elem, SVGGElement)) {
				if (elem.className && elem.className.baseVal == "me") {
					this.visible = true;
					return this.visible;
				}
			}

			// Check if fill or stroke is set
			var fill = elem.getAttributeNS(null, "fill");
			var stroke = elem.getAttributeNS(null, "stroke");
			if (fill && fill == "none" && stroke && stroke == "none") {
				this.visible = false;
			} else {
				// Check if displayed
				var attr = elem.getAttributeNS(null, "display");
				if(!attr)
					this.visible = this.isVisible(elem.parentNode);
				else if (attr == "none") 
					this.visible = false;
				else
					this.visible = true;
			}
		} else {
			this.visible = true;
		}
		
		return this.visible;
	},

	toString: function() { return (this.element) ? "SVGShape " + this.element.id : "SVGShape " + this.element;}
 });/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

/**
 * Init namespaces
 */
if(!ORYX) {var ORYX = {};}
if(!ORYX.Core) {ORYX.Core = {};}
if(!ORYX.Core.SVG) {ORYX.Core.SVG = {};}

/**
 * @classDescription Class for adding text to a shape.
 * 
 */
ORYX.Core.SVG.Label = Clazz.extend({
	
	_characterSets:[
		"%W",
		"@",
		"m",
		"wDGMOQ?????#+=<>~^",
		"ABCHKNRSUVXZ??????????&",
		"bdghnopqux???????????ETY1234567890?????_????${}*????`???????????",
		"aeksvyz?????FLP????????????????",
		"c-",
		"rtJ\"/()[]:;!|\\",
		"fjI., ",
		"'",
		"il"
		],
	_characterSetValues:[15,14,13,11,10,9,8,7,6,5,4,3],

	/**
	 * Constructor
	 * @param options {Object} :
	 * 	textElement
	 * 
	 */
	construct: function(options) {
		arguments.callee.$.construct.apply(this, arguments);
		
		if(!options.textElement) {
			throw "Label: No parameter textElement." 
		} else if (!ORYX.Editor.checkClassType( options.textElement, SVGTextElement ) ) {
			throw "Label: Parameter textElement is not an SVGTextElement."	
		}
		
		this.invisibleRenderPoint = -5000;
		
		this.node = options.textElement;
		
		
		this.node.setAttributeNS(null, 'stroke-width', '0pt');
		this.node.setAttributeNS(null, 'letter-spacing', '-0.01px');
		
		this.shapeId = options.shapeId;
		
		this.id;
		
		this.fitToElemId;
		
		this.edgePosition;
		
		this.x;
		this.y;
		this.oldX;
		this.oldY;
		
		this.isVisible = true;
		
		this._text;
		this._verticalAlign;
		this._horizontalAlign;
		this._rotate;
		this._rotationPoint;
		
		//this.anchors = [];
		this.anchorLeft;
		this.anchorRight;
		this.anchorTop;
		this.anchorBottom;
		
		this._isChanged = true;

		//if the text element already has an id, don't change it.
		var _id = this.node.getAttributeNS(null, 'id');
		if(_id) {
			this.id = _id;
		}
		
		//initialization	
		
		//set referenced element the text is fit to
		this.fitToElemId = this.node.getAttributeNS(ORYX.CONFIG.NAMESPACE_ORYX, 'fittoelem');
		if(this.fitToElemId)
			this.fitToElemId = this.shapeId + this.fitToElemId;
		
		//set alignment	
		var alignValues = this.node.getAttributeNS(ORYX.CONFIG.NAMESPACE_ORYX, 'align');
		if(alignValues) {
			alignValues = alignValues.replace(/,/g, " ");
			alignValues = alignValues.split(" ");
			alignValues = alignValues.without("");
			
			alignValues.each((function(alignValue) {
				switch (alignValue) {
					case 'top':
					case 'middle':
					case 'bottom':
						if(!this._verticalAlign){this._originVerticalAlign = this._verticalAlign = alignValue;}
						break;
					case 'left':
					case 'center':
					case 'right':
						if(!this._horizontalAlign){this._originHorizontalAlign = this._horizontalAlign = alignValue;}
						break;
				}
			}).bind(this));
		}
		
		//set edge position (only in case the label belongs to an edge)
		this.edgePosition = this.node.getAttributeNS(ORYX.CONFIG.NAMESPACE_ORYX, 'edgePosition');
		if(this.edgePosition) {
			this.originEdgePosition = this.edgePosition = this.edgePosition.toLowerCase();
		}
		
		
		//get offset top
		this.offsetTop = this.node.getAttributeNS(ORYX.CONFIG.NAMESPACE_ORYX, 'offsetTop') || ORYX.CONFIG.OFFSET_EDGE_LABEL_TOP;
		if(this.offsetTop) {
			this.offsetTop = parseInt(this.offsetTop);
		}
		
		//get offset top
		this.offsetBottom = this.node.getAttributeNS(ORYX.CONFIG.NAMESPACE_ORYX, 'offsetBottom') || ORYX.CONFIG.OFFSET_EDGE_LABEL_BOTTOM;
		if(this.offsetBottom) {
			this.offsetBottom = parseInt(this.offsetBottom);
		}
		
				
		//set rotation
		var rotateValue = this.node.getAttributeNS(ORYX.CONFIG.NAMESPACE_ORYX, 'rotate');
		if(rotateValue) {
			try {
				this._rotate = parseFloat(rotateValue);
			} catch (e) {
				this._rotate = 0;
			}
		} else {
			this._rotate = 0;
		}
		
		//anchors
		var anchorAttr = this.node.getAttributeNS(ORYX.CONFIG.NAMESPACE_ORYX, "anchors");
		if(anchorAttr) {
			anchorAttr = anchorAttr.replace("/,/g", " ");
			var anchors = anchorAttr.split(" ").without("");
			
			for(var i = 0; i < anchors.length; i++) {
				switch(anchors[i].toLowerCase()) {
					case "left":
						this.originAnchorLeft = this.anchorLeft = true;
						break;
					case "right":
						this.originAnchorRight = this.anchorRight = true;
						break;
					case "top":
						this.originAnchorTop = this.anchorTop = true;
						break;
					case "bottom":
						this.originAnchorBottom = this.anchorBottom = true;
						break;
				}
			}
		}
		
		//if no alignment defined, set default alignment
		if(!this._verticalAlign) { this._verticalAlign = 'bottom'; }
		if(!this._horizontalAlign) { this._horizontalAlign = 'left'; }

		var xValue = this.node.getAttributeNS(null, 'x');
		if(xValue) {
			this.oldX = this.x = parseFloat(xValue);
		} else {
			//TODO error
		}
		
		var yValue = this.node.getAttributeNS(null, 'y');
		if(yValue) {
			this.oldY = this.y = parseFloat(yValue);
		} else {
			//TODO error
		}
		
		//set initial text
		this.text(this.node.textContent);
	},
	
	/**
	 * Reset the anchor position to the original value
	 * which was specified in the stencil set
	 * 
	 */
	resetAnchorPosition: function(){
		this.anchorLeft = this.originAnchorLeft || false;
		this.anchorRight = this.originAnchorRight || false;
		this.anchorTop = this.originAnchorTop || false;
		this.anchorBottom = this.originAnchorBottom || false;
	},
	
	isOriginAnchorLeft: function(){ return this.originAnchorLeft || false; },
	isOriginAnchorRight: function(){ return this.originAnchorRight || false; },
	isOriginAnchorTop: function(){ return this.originAnchorTop || false; },
	isOriginAnchorBottom: function(){ return this.originAnchorBottom || false; },
	
	
	isAnchorLeft: function(){ return this.anchorLeft || false; },
	isAnchorRight: function(){ return this.anchorRight || false; },
	isAnchorTop: function(){ return this.anchorTop || false; },
	isAnchorBottom: function(){ return this.anchorBottom || false; },
	
	/**
	 * Returns the x coordinate
	 * @return {number}
	 */
	getX: function(){
		try {
			var x = this.node.x.baseVal.getItem(0).value;
			switch(this.horizontalAlign()){
				case "left": return x;
				case "center": return x - (this.getWidth()/2);
				case "right": return x - this.getWidth();
			}
			return this.node.getBBox().x;
		} catch(e){
			return this.x;
		}
	},
		
	setX: function(x){
		if (this.position)
			this.position.x = x;
		else 
			this.setOriginX(x);
	},
	
	
	/**
	 * Returns the y coordinate
	 * @return {number}
	 */
	getY: function(){
		try {
			return this.node.getBBox().y;
		} catch(e){
			return this.y;
		}
	},
	
	setY: function(y){
		if (this.position)
			this.position.y = y;
		else 
			this.setOriginY(y);
	},
	
	setOriginX: function(x){
		this.x = x;
	},
	
	setOriginY: function(y){
		this.y = y;
	},

	
	/**
	 * Returns the width of the label
	 * @return {number}
	 */
	getWidth: function(){
		try {
			try {
				var width, cn = this.node.childNodes;
				if (cn.length == 0) {
					width = this.node.getBBox().width;
				} else {
					for (var i = 0, size = cn.length; i < size; ++i) {
						var w = cn[i].getComputedTextLength();
						if ("undefined" == typeof width || width < w) {
							width = w;
						}
					}
				}
				return width+(width%2==0?0:1);
			} catch (ee) {
				return this.node.getBBox().width;
			}
		} catch(e){
			return 0;
		}
	},
	
	getOriginUpperLeft: function(){
		var x = this.x, y = this.y;
		switch (this._horizontalAlign){
			case 'center' :
				x -= this.getWidth()/2;
				break;
			case 'right' :
				x -= this.getWidth();
				break;
		}
		switch (this._verticalAlign){
			case 'middle' :
				y -= this.getHeight()/2;
				break;
			case 'bottom' :
				y -= this.getHeight();
				break;
		}
		return {x:x, y:y};
	},
	
	/**
	 * Returns the height of the label
	 * @return {number}
	 */
	getHeight: function(){
		try {
			return this.node.getBBox().height;
		} catch(e){
			return 0;
		}
	},
	
	/**
	 * Returns the relative center position of the label 
	 * to its parent shape.
	 * @return {Object}
	 */
	getCenter: function(){
		var up = {x: this.getX(), y: this.getY()};
		up.x += this.getWidth()/2;
		up.y += this.getHeight()/2;
		return up;
	},
	
	/**
	 * Sets the position of a label relative to the parent.
	 * @param {Object} position
	 */
	setPosition: function(position){
		if (!position || position.x === undefined || position.y === undefined) {
			delete this.position;
		} else {
			this.position = position;
		}
		
		if (this.position){
			delete this._referencePoint;
			delete this.edgePosition;
		}
		
		this._isChanged = true;
		this.update();
	},
	
	/**
	 * Return the position
	 */
	getPosition: function(){
		return this.position;
	},
	
	setReferencePoint: function(ref){
		if (ref) {
			this._referencePoint = ref;
		} else {
			delete this._referencePoint;
		}
		if (this._referencePoint){
			delete this.position;
		}
	},
	
	getReferencePoint: function(){
		return this._referencePoint || undefined;
	},
	
	changed: function() {
		this._isChanged = true;
	},
	
	/**
	 * Register a callback which will be called if the label
	 * was rendered.
	 * @param {Object} fn
	 */
	registerOnChange: function(fn){
		if (!this.changeCallbacks){
			this.changeCallbacks = [];
		}
		if (fn instanceof Function && !this.changeCallbacks.include(fn)){
			this.changeCallbacks.push(fn);
		}
	},
	
	/**
	 * Unregister the callback for changes.
	 * @param {Object} fn
	 */
	unregisterOnChange: function(fn){
		if (this.changeCallbacks && fn instanceof Function && this.changeCallbacks.include(fn)){
			this.changeCallbacks = this.changeCallbacks.without(fn);
		}
	},
	
	/**
	 * Returns TRUE if the labe is currently in
	 * the update mechanism.
	 * @return {Boolean}
	 */
	isUpdating: function(){
		return !!this._isUpdating;	
	},
	
	
	getOriginEdgePosition: function(){
		return this.originEdgePosition;	
	},
	
	/**
	 * Returns the edgeposition.
	 * 
	 * @return {String} "starttop", "startmiddle", "startbottom", 
	 * "midtop", "midbottom", "endtop", "endbottom" or null
	 */
	getEdgePosition: function(){
		return this.edgePosition || null;	
	},
	
	/**
	 * Set the edge position, must be one of the valid
	 * edge positions (see getEdgePosition).
	 * Removes the reference point and the absolute position as well.
	 * 
	 * @param {Object} position
	 */
	setEdgePosition: function(position){
		if (["starttop", "startmiddle", "startbottom", 
			"midtop", "midbottom", "endtop", "endbottom"].include(position)){
			this.edgePosition = position;
			delete this.position;
			delete this._referencePoint;
		} else {
			delete this.edgePosition;
		}
	},
	
	/**
	 * Update the SVG text element.
	 */
	update: function(force) {
		
		var x = this.x, y = this.y;
		if (this.position){
			x = this.position.x;
			y = this.position.y;
		}
		x = Math.floor(x); y = Math.floor(y);
		
		if(this._isChanged || x !== this.oldX || y !== this.oldY || force === true) {
			if (this.isVisible) {
				this._isChanged = false;
				this._isUpdating = true;
				
				this.node.setAttributeNS(null, 'x', x);
				this.node.setAttributeNS(null, 'y', y);
				this.node.removeAttributeNS(null, "fill-opacity");
				
				//this.node.setAttributeNS(null, 'font-size', this._fontSize);
				//this.node.setAttributeNS(ORYX.CONFIG.NAMESPACE_ORYX, 'align', this._horizontalAlign + " " + this._verticalAlign);
				
				this.oldX = x;
				this.oldY = y;
				
				//set rotation
				if (!this.position && !this.getReferencePoint()) {
					if (this._rotate !== undefined) {
						if (this._rotationPoint) 
							this.node.setAttributeNS(null, 'transform', 'rotate(' + this._rotate + ' ' + Math.floor(this._rotationPoint.x) + ' ' + Math.floor(this._rotationPoint.y) + ')');
						else 
							this.node.setAttributeNS(null, 'transform', 'rotate(' + this._rotate + ' ' + Math.floor(x) + ' ' + Math.floor(y) + ')');
					}
				} else {
					this.node.removeAttributeNS(null, 'transform');
				}
				
				var textLines = this._text.split("\n");
				while (textLines.last() == "") 
					textLines.pop();
				
				
				if (this.node.ownerDocument) {
					// Only reset the tspans if the text 
					// has changed or has to be wrapped
					if (this.fitToElemId || this._textHasChanged){
						this.node.textContent = ""; // Remove content
						textLines.each((function(textLine, index){
							var tspan = this.node.ownerDocument.createElementNS(ORYX.CONFIG.NAMESPACE_SVG, 'tspan');
							tspan.textContent = textLine.trim();
							if (this.fitToElemId) {
								tspan.setAttributeNS(null, 'x', this.invisibleRenderPoint);
								tspan.setAttributeNS(null, 'y', this.invisibleRenderPoint);
							}
							
							/*
							 * Chrome's getBBox() method fails, if a text node contains an empty tspan element.
							 * So, we add a whitespace to such a tspan element.
							 */
							if(tspan.textContent === "") {
								tspan.textContent = " ";
							}
							
							//append tspan to text node
							this.node.appendChild(tspan);
						}).bind(this));
						delete this._textHasChanged;
						delete this.indices;
					}
					
					//Work around for Mozilla bug 293581
					if (this.isVisible && this.fitToElemId) {
						this.node.setAttributeNS(null, 'visibility', 'hidden');
					}
					
					if (this.fitToElemId) {
						window.setTimeout(this._checkFittingToReferencedElem.bind(this), 0);
						//this._checkFittingToReferencedElem();
					} else {
						window.setTimeout(this._positionText.bind(this), 0);
						//this._positionText();
					}
				}
			} else {
				this.node.textContent = "";
				//this.node.setAttributeNS(null, "fill-opacity", "0.2");
			}
		}
	},
	
	_checkFittingToReferencedElem: function() {
		try {
			var tspans = $A(this.node.getElementsByTagNameNS(ORYX.CONFIG.NAMESPACE_SVG, 'tspan'));
			
			//only do this in firefox 3. all other browsers do not support word wrapping!!!!!
			//if (/Firefox[\/\s](\d+\.\d+)/.test(navigator.userAgent) && new Number(RegExp.$1)>=3) {
				var newtspans = [];
				
				var refNode = this.node.ownerDocument.getElementById(this.fitToElemId);
				
				if (refNode) {
					var refbb = refNode.getBBox();
					
					var fontSize = this.getFontSize();
					
					for (var j = 0; j < tspans.length; j++) {
						var tspan = tspans[j];
						
						var textLength = this._getRenderedTextLength(tspan, undefined, undefined, fontSize);
						
						var refBoxLength = (this._rotate != 0 
								&& this._rotate % 180 != 0 
								&& this._rotate % 90 == 0 ? 
										refbb.height : refbb.width);
						
						if (textLength > refBoxLength) {
						
							var startIndex = 0;
							var lastSeperatorIndex = 0;
							
							var numOfChars = this.getTrimmedTextLength(tspan.textContent);
							for (var i = 0; i < numOfChars; i++) {
								var sslength = this._getRenderedTextLength(tspan, startIndex, i-startIndex, fontSize);
								
								if (sslength > refBoxLength - 2) {
									var newtspan = this.node.ownerDocument.createElementNS(ORYX.CONFIG.NAMESPACE_SVG, 'tspan');
									if (lastSeperatorIndex <= startIndex) {
										lastSeperatorIndex = (i == 0) ? i : i-1;
										newtspan.textContent = tspan.textContent.slice(startIndex, lastSeperatorIndex).trim();
										//lastSeperatorIndex = i;
									}
									else {
										newtspan.textContent = tspan.textContent.slice(startIndex, ++lastSeperatorIndex).trim();
									}
									
									newtspan.setAttributeNS(null, 'x', this.invisibleRenderPoint);
									newtspan.setAttributeNS(null, 'y', this.invisibleRenderPoint);
									
									//insert tspan to text node
									//this.node.insertBefore(newtspan, tspan);
									newtspans.push(newtspan);
									
									startIndex = lastSeperatorIndex;
								}
								else {
									var curChar = tspan.textContent.charAt(i);
									if (curChar == ' ' ||
									curChar == '-' ||
									curChar == "." ||
									curChar == "," ||
									curChar == ";" ||
									curChar == ":") {
										lastSeperatorIndex = i;
									}
								}
							}
							
							tspan.textContent = tspan.textContent.slice(startIndex).trim();
						}
						
						newtspans.push(tspan);
					}
					
					while (this.node.hasChildNodes()) 
						this.node.removeChild(this.node.childNodes[0]);
					
					while (newtspans.length > 0) {
						this.node.appendChild(newtspans.shift());
					}
				}
			//}
		} catch (e) {
			ORYX.Log.fatal("Error " + e);
		}
		window.setTimeout(this._positionText.bind(this), 0);
		//this._positionText();
	},
	
	/**
	 * This is a work around method for Mozilla bug 293581.
	 * Before the method getComputedTextLength works, the text has to be rendered.
	 */
	_positionText: function() {
		try {
			
			var tspans = this.node.childNodes;
			
			var fontSize = this.getFontSize(this.node); 
			
			var invalidTSpans = [];
			
			var x = this.x, y = this.y;
			if (this.position){
				x = this.position.x;
				y = this.position.y;
			}
			x = Math.floor(x); y = Math.floor(y);
			
			var i = 0, indic = []; // Cache indices if the _positionText is called again, before update is called 
			var is =(this.indices || $R(0,tspans.length-1).toArray());
			var length = is.length;
			is.each((function(index){
				if ("undefined" == typeof index){
					return;
				}
				
				var tspan = tspans[i++];
				
				if(tspan.textContent.trim() === "") {
					invalidTSpans.push(tspan);
				} else {
					//set vertical position
					var dy = 0;
					switch (this._verticalAlign) {
						case 'bottom':
							dy = -(length - index - 1) * (fontSize);
							break;
						case 'middle':
							dy = -(length / 2.0 - index - 1) * (fontSize);
							dy -= ORYX.CONFIG.LABEL_LINE_DISTANCE / 2;
							break;
						case 'top':
							dy = index * (fontSize);
							dy += fontSize;
							break;
					}
					tspan.setAttributeNS(null, 'dy', Math.floor(dy));
					
					tspan.setAttributeNS(null, 'x', x);
					tspan.setAttributeNS(null, 'y', y);
					indic.push(index);
				}
				
			}).bind(this));
			
			indic.length = tspans.length;
			this.indices = this.indices || indic;
			
			invalidTSpans.each(function(tspan) {
				this.node.removeChild(tspan)
			}.bind(this));
			
			//set horizontal alignment
			switch (this._horizontalAlign) {
				case 'left':
					this.node.setAttributeNS(null, 'text-anchor', 'start');
					break;
				case 'center':
					this.node.setAttributeNS(null, 'text-anchor', 'middle');
					break;
				case 'right':
					this.node.setAttributeNS(null, 'text-anchor', 'end');
					break;
			}
			
		} catch(e) {
			//console.log(e);
			this._isChanged = true;
		}
		
		
		if(this.isVisible) {
			this.node.removeAttributeNS(null, 'visibility');
		}		
		
		
		// Finished
		delete this._isUpdating;
		
		// Raise change event
		(this.changeCallbacks||[]).each(function(fn){
			fn.apply(fn);
		})
				
	},
	
	/**
	 * Returns the text length of the text content of an SVG tspan element.
	 * For all browsers but Firefox 3 the values are estimated.
	 * @param {TSpanSVGElement} tspan
	 * @param {int} startIndex Optional, for sub strings
	 * @param {int} endIndex Optional, for sub strings
	 */
	_getRenderedTextLength: function(tspan, startIndex, endIndex, fontSize) {
		//if (/Firefox[\/\s](\d+\.\d+)/.test(navigator.userAgent) && new Number(RegExp.$1) >= 3) {
			if(startIndex === undefined) {
//test string: abcdefghijklmnopqrstuvwxyz????????????????,.-#+ 1234567890?????ABCDEFGHIJKLMNOPQRSTUVWXYZ;:_'*???????????????!"????$%&/()=?[]{}|<>'~????`\^?????????@?????????????????
//				for(var i = 0; i < tspan.textContent.length; i++) {
//					console.log(tspan.textContent.charAt(i), tspan.getSubStringLength(i,1), this._estimateCharacterWidth(tspan.textContent.charAt(i))*(fontSize/14.0));
//				}
				return tspan.getComputedTextLength();
			} else {
				return tspan.getSubStringLength(startIndex, endIndex);
			}
		/*} else {
			if(startIndex === undefined) {
				return this._estimateTextWidth(tspan.textContent, fontSize);
			} else {
				return this._estimateTextWidth(tspan.textContent.substr(startIndex, endIndex).trim(), fontSize);
			}
		}*/
	},
	
	/**
	 * Estimates the text width for a string.
	 * Used for word wrapping in all browser but FF3.
	 * @param {Object} text
	 */
	_estimateTextWidth: function(text, fontSize) {
		var sum = 0.0;
		for(var i = 0; i < text.length; i++) {
			sum += this._estimateCharacterWidth(text.charAt(i));
		}
		
		return sum*(fontSize/14.0);
	},
	
	/**
	 * Estimates the width of a single character for font size 14.
	 * Used for word wrapping in all browser but FF3.
	 * @param {Object} character
	 */
	_estimateCharacterWidth: function(character) {
		for(var i = 0; i < this._characterSets.length; i++) {
 			if(this._characterSets[i].indexOf(character) >= 0) {
				return this._characterSetValues[i];
			}
 		}	
		return 9;
 	},
	
	getReferencedElementWidth: function() {
		var refNode = this.node.ownerDocument.getElementById(this.fitToElemId);
		
		if(refNode) {
			var refbb = refNode.getBBox();
				
			if(refbb) {
				return (this._rotate != 0 
						&& this._rotate % 180 != 0 
						&& this._rotate % 90 == 0 ? 
								refbb.height : refbb.width);
			}
		}
		
		return undefined;
	},
	
	/**
	 * If no parameter is provided, this method returns the current text.
	 * @param text {String} Optional. Replaces the old text with this one.
	 */
	text: function() {
		switch (arguments.length) {
			case 0:
				return this._text
				break;
			
			case 1:
				var oldText = this._text;
				if(arguments[0]) {
					// Filter out multiple spaces to fix issue in chrome for line-wrapping
					this._text = arguments[0].toString();
					if(this._text != null && this._text != undefined) {
						this._text = this._text.replace(/ {2,}/g,' ');
					}
				} else {
					this._text = "";
				}
				if(oldText !== this._text) {
					this._isChanged = true;
					this._textHasChanged = true;
				}
				break;
				
			default: 
				//TODO error
				break;
		}
	},
	
	getOriginVerticalAlign: function(){
		return this._originVerticalAlign;
	},
	
	verticalAlign: function() {
		switch(arguments.length) {
			case 0:
				return this._verticalAlign;
			case 1:
				if(['top', 'middle', 'bottom'].member(arguments[0])) {
					var oldValue = this._verticalAlign;
					this._verticalAlign = arguments[0];
					if(this._verticalAlign !== oldValue) {
						this._isChanged = true;
					}
				}
				break;
				
			default:
				//TODO error
				break;
		}
	},
	
	getOriginHorizontalAlign: function(){
		return this._originHorizontalAlign;
	},
	
	horizontalAlign: function() {
		switch(arguments.length) {
			case 0:
				return this._horizontalAlign;
			case 1:
				if(['left', 'center', 'right'].member(arguments[0])) {
					var oldValue = this._horizontalAlign;
					this._horizontalAlign = arguments[0];
					if(this._horizontalAlign !== oldValue) {
						this._isChanged = true;
					}	
				}
				break;
				
			default:
				//TODO error
				break;
		}
	},
	
	rotate: function() {
		switch(arguments.length) {
			case 0:
				return this._rotate;
			case 1:
				if (this._rotate != arguments[0]) {
					this._rotate = arguments[0];
					this._rotationPoint = undefined;
					this._isChanged = true;
				}
			case 2:
				if(this._rotate != arguments[0] ||
				   !this._rotationPoint ||
				   this._rotationPoint.x != arguments[1].x ||
				   this._rotationPoint.y != arguments[1].y) {
					this._rotate = arguments[0];
					this._rotationPoint = arguments[1];
					this._isChanged = true;
				}
				
		}
	},
	
	hide: function() {
		if(this.isVisible) {
			this.isVisible = false;
			this._isChanged = true;
		}
	},
	
	show: function() {
		if(!this.isVisible) {
			this.isVisible = true;
			this._isChanged = true;

			// Since text is removed from the tspan when "hidden", mark
			// the text as changed to get it redrawn
			this._textHasChanged = true;
		}
	},
	
	/**
	 * iterates parent nodes till it finds a SVG font-size
	 * attribute.
	 * @param {SVGElement} node
	 */
	getInheritedFontSize: function(node) {
		if(!node || !node.getAttributeNS)
			return;
			
		var attr = node.getAttributeNS(null, "font-size");
		if(attr) {
			return parseFloat(attr);
		} else if(!ORYX.Editor.checkClassType(node, SVGSVGElement)) {
			return this.getInheritedFontSize(node.parentNode);
		}
	},
	
	getFontSize: function(node) {
		var tspans = this.node.getElementsByTagNameNS(ORYX.CONFIG.NAMESPACE_SVG, 'tspan');
			
		//trying to get an inherited font-size attribute
		//NO CSS CONSIDERED!
		var fontSize = this.getInheritedFontSize(this.node); 
		
		if (!fontSize) {
			//because this only works in firefox 3, all other browser use the default line height
			if (tspans[0] && /Firefox[\/\s](\d+\.\d+)/.test(navigator.userAgent) && new Number(RegExp.$1) >= 3) {
				fontSize = tspans[0].getExtentOfChar(0).height;
			}
			else {
				fontSize = ORYX.CONFIG.LABEL_DEFAULT_LINE_HEIGHT;
			}
			
			//handling of unsupported method in webkit
			if (fontSize <= 0) {
				fontSize = ORYX.CONFIG.LABEL_DEFAULT_LINE_HEIGHT;
			}
		}
		
		if(fontSize)
			this.node.setAttribute("oryx:fontSize", fontSize);
		
		return fontSize;
	},
	
	/**
	 * Get trimmed text length for use with
	 * getExtentOfChar and getSubStringLength.
	 * @param {String} text
	 */
	getTrimmedTextLength: function(text) {
		text = text.strip().gsub('  ', ' ');
		
		var oldLength;
		do {
			oldLength = text.length;
			text = text.gsub('  ', ' ');
		} while (oldLength > text.length);

		return text.length;
	},
	
	/**
	 * Returns the offset from
	 * edge to the label which is 
	 * positioned under the edge
	 * @return {int}
	 */
	getOffsetBottom: function(){
		return this.offsetBottom;
	},
	
		
	/**
	 * Returns the offset from
	 * edge to the label which is 
	 * positioned over the edge
	 * @return {int}
	 */
	getOffsetTop: function(){
		return this.offsetTop;
	},
	
	/**
	 * 
	 * @param {Object} obj
	 */
	deserialize: function(obj, shape){
		if (obj && "undefined" != typeof obj.x && "undefined" != typeof obj.y){			
			this.setPosition({x:obj.x, y:obj.y});
			
			if ("undefined" != typeof obj.distance){
				var from = shape.dockers[obj.from];
				var to = shape.dockers[obj.to];
				if (from && to){
					this.setReferencePoint({
						dirty : true,
						distance : obj.distance,
						intersection : {x: obj.x, y: obj.y},
						orientation : obj.orientation,
						segment: {
							from: from,
							fromIndex: obj.from,
							fromPosition: from.bounds.center(),
							to: to,
							toIndex: obj.to,
							toPosition: to.bounds.center()
						}
					})
				}
			}
			
			if (obj.left) this.anchorLeft = true;
			if (obj.right) this.anchorRight = true;
			if (obj.top) this.anchorTop = true;
			if (obj.bottom) this.anchorBottom = true;
			if (obj.valign) this.verticalAlign(obj.valign);
			if (obj.align) this.horizontalAlign(obj.align);
			
		} else if (obj && "undefined" != typeof obj.edge){
			this.setEdgePosition(obj.edge);
		}
	},

	/**
	 * 
	 * @return {Object}
	 */
	serialize: function(){
		
		// On edge position
		if (this.getEdgePosition()){
			if (this.getOriginEdgePosition() !== this.getEdgePosition()){
				return {edge: this.getEdgePosition()};
			} else {
				return null;
			}
		}
		
		// On self defined position
		if (this.position){
			var pos = {x: this.position.x, y: this.position.y};
			if (this.isAnchorLeft() && this.isAnchorLeft() !== this.isOriginAnchorLeft()){
				pos.left = true;
			}
			if (this.isAnchorRight() && this.isAnchorRight() !== this.isOriginAnchorRight()){
				pos.right = true;
			}
			if (this.isAnchorTop() && this.isAnchorTop() !== this.isOriginAnchorTop()){
				pos.top = true;
			}
			if (this.isAnchorBottom() && this.isAnchorBottom() !== this.isOriginAnchorBottom()){
				pos.bottom = true;
			}
			
			if (this.getOriginVerticalAlign() !== this.verticalAlign()){
				pos.valign = this.verticalAlign();
			}
			if (this.getOriginHorizontalAlign() !== this.horizontalAlign()){
				pos.align = this.horizontalAlign();
			}
			
			return pos;
		}
		
		// On reference point which is interesting for edges
		if (this.getReferencePoint()){
			var ref = this.getReferencePoint();
			return {
				distance : ref.distance,
				x : ref.intersection.x,
				y : ref.intersection.y,
				from : ref.segment.fromIndex,
				to : ref.segment.toIndex,
				orientation : ref.orientation,
				valign : this.verticalAlign(),
				align : this.horizontalAlign()
			}
		}
		return null;
	},
	
	toString: function() { return "Label " + this.id }
 });/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

/**
 * Init namespaces
 */
if(!ORYX) {var ORYX = {};}
if(!ORYX.Core) {ORYX.Core = {};}
if(!ORYX.Core.Math) {ORYX.Core.Math = {};}
	
/**
 * Calculate the middle point between two given points
 * @param {x:double, y:double} point1
 * @param {x:double, y:double} point2
 * @return the middle point
 */
ORYX.Core.Math.midPoint = function(point1, point2) {
	return 	{
				x: (point1.x + point2.x) / 2.0,
				y: (point1.y + point2.y) / 2.0
			}
}
			
/**
 * Returns a TRUE if the point is over a line (defined by
 * point1 and point 2). In Addition a threshold can be set,
 * which defines the weight of those line.
 * 
 * @param {int} pointX - Point X
 * @param {int} pointY - Point Y
 * @param {int} lPoint1X - Line first Point X
 * @param {int} lPoint1Y - Line first Point Y
 * @param {int} lPoint2X - Line second Point X
 * @param {int} lPoint2Y - Line second Point y
 * @param {int} offset {optional} - maximal distance to line
 * @class ORYX.Core.Math.prototype
 */
ORYX.Core.Math.isPointInLine = function (pointX, pointY, lPoint1X, lPoint1Y, lPoint2X, lPoint2Y, offset) {

	offset = offset ? Math.abs(offset) : 1;
	
	// Check if the edge is vertical
	if(Math.abs(lPoint1X-lPoint2X)<=offset && Math.abs(pointX-lPoint1X)<=offset && pointY-Math.max(lPoint1Y, lPoint2Y)<=offset && Math.min(lPoint1Y, lPoint2Y)-pointY<=offset) {
		return true
	}

	// Check if the edge is horizontal
	if(Math.abs(lPoint1Y-lPoint2Y)<=offset && Math.abs(pointY-lPoint1Y)<=offset && pointX-Math.max(lPoint1X, lPoint2X)<=offset && Math.min(lPoint1X, lPoint2X)-pointX<=offset) {
		return true
	}

	if(pointX > Math.max(lPoint1X, lPoint2X) || pointX < Math.min(lPoint1X, lPoint2X)) {
		return false
	}

	if(pointY > Math.max(lPoint1Y, lPoint2Y) || pointY < Math.min(lPoint1Y, lPoint2Y)) {
		return false
	}
			
	var s = (lPoint1Y - lPoint2Y) / (lPoint1X - lPoint2X);
	
	return 	Math.abs(pointY - ((s * pointX) + lPoint1Y - s * lPoint1X)) < offset
}

/**
 * Get a boolean if the point is in the polygone
 * 
 */
ORYX.Core.Math.isPointInEllipse = function (pointX, pointY, cx, cy, rx, ry) {

	if(cx === undefined || cy === undefined || rx === undefined || ry === undefined) {
		throw "ORYX.Core.Math.isPointInEllipse needs a ellipse with these properties: x, y, radiusX, radiusY"
	} 
	
    var tx = (pointX - cx) / rx;
    var ty = (pointY - cy) / ry;
	
    return tx * tx + ty * ty < 1.0;
}
	
/**
 * Get a boolean if the point is in the polygone
 * @param {int} pointX
 * @param {int} pointY
 * @param {[int]} Cornerpoints of the Polygone (x,y,x,y,...)
 */
ORYX.Core.Math.isPointInPolygone = function(pointX, pointY, polygone){

	if (arguments.length < 3) {
		throw "ORYX.Core.Math.isPointInPolygone needs two arguments"
	}
	
	var lastIndex = polygone.length-1;
	
	if (polygone[0] !== polygone[lastIndex - 1] || polygone[1] !== polygone[lastIndex]) {
		polygone.push(polygone[0]);
		polygone.push(polygone[1]);
	}
	
	var crossings = 0;

	var x1, y1, x2, y2, d;
	
    for (var i = 0; i < polygone.length - 3; ) {
        x1=polygone[i];
        y1=polygone[++i];
        x2=polygone[++i];
        y2=polygone[i+1];
        d=(pointY - y1) * (x2 - x1) - (pointX - x1) * (y2 - y1);

        if ((y1 >= pointY) != (y2 >= pointY)) {
            crossings += y2 - y1 >= 0 ? d >= 0 : d <= 0;
        }
        if (!d && Math.min(x1,x2) <= pointX && pointX <= Math.max(x1,x2)
            && Math.min(y1,y2) <= pointY && pointY <= Math.max(y1,y2)) {
            return true;
        }
    }
	return (crossings%2)?true:false;
}

/**
 *	Calculates the distance between a point and a line. It is also testable, if 
 *  the distance orthogonal to the line, matches the segment of the line.
 *  
 *  @param {float} lineP1
 *  	The starting point of the line segment
 *  @param {float} lineP2
 *  	The end point of the line segment
 *  @param {Point} point
 *  	The point to calculate the distance to.
 *  @param {boolean} toSegmentOnly
 *  	Flag to signal if only the segment of the line shell be evaluated.
 */
ORYX.Core.Math.distancePointLinie = function(
									lineP1, 
									lineP2, 
									point, 
									toSegmentOnly) {
	
	var intersectionPoint = 
				ORYX.Core.Math.getPointOfIntersectionPointLine(lineP1, 
																lineP2, 
																point, 
																toSegmentOnly);
	
	if(!intersectionPoint) {
		return null;
	}
	
	return ORYX.Core.Math.getDistancePointToPoint(point, intersectionPoint);
};

/**
 * Calculates the distance between two points.
 * 
 * @param {point} point1
 * @param {point} point2
 */
ORYX.Core.Math.getDistancePointToPoint = function(point1, point2) {
	return Math.sqrt(Math.pow(point1.x - point2.x, 2) + Math.pow(point1.y - point2.y, 2));
};

/**
 * Calculates the relative distance of a point which is between two other points.
 * 
 * @param {point} between1
 * @param {point} between2
 * @param {point} point
 */
ORYX.Core.Math.getDistanceBetweenTwoPoints = function(between1, between2, point) {
	return 	ORYX.Core.Math.getDistancePointToPoint(point, between1) /
			ORYX.Core.Math.getDistancePointToPoint(between1, between2);
};


/**
 * Returns true, if the point is of the left hand
 * side of the regarding the line.
 * 
 * @param {point} lineP1 Line first point
 * @param {point} lineP2 Line second point
 * @param {point} point
 */
ORYX.Core.Math.pointIsLeftOfLine = function(lineP1, lineP2, point){
	
	var vec1 = ORYX.Core.Math.getVector(lineP1, lineP2);
	var vec2 = ORYX.Core.Math.getVector(lineP1, point);
	// if the cross produkt is more than 0
	return ((vec1.x*vec2.y) - (vec2.x*vec1.y)) > 0
};

/**
 * Calculates the a point which is relatively between two other points.
 * 
 * @param {point} point1
 * @param {point} point2
 * @param {number} relative Relative which is between 0 and 1
 */
ORYX.Core.Math.getPointBetweenTwoPoints = function(point1, point2, relative) {
	relative = Math.max(Math.min(relative || 0, 1), 0);
	
	if (relative === 0){
		return point1;
	} else if (relative === 1){
		return point2;
	}
	
	return {
		x: point1.x + ((point2.x - point1.x) * relative),
		y: point1.y + ((point2.y - point1.y) * relative)
	}
};


/**
 * Returns the vector of the both points
 *
 * @param {point} point1
 * @param {point} point2
 */
ORYX.Core.Math.getVector = function(point1, point2){
	return {
		x: point2.x - point1.x,
		y: point2.y - point1.y
	}
}

/**
 * Returns the an identity vector of the given vector, 
 * which has the length ot one.
 *
 * @param {point} vector
 * or 
 * @param {point} point1
 * @param {point} point2
 */
ORYX.Core.Math.getIdentityVector = function(vector){
	
	if (arguments.length == 2){
		vector = ORYX.Core.Math.getVector(arguments[0], arguments[1]);
	}
	
	var length = Math.sqrt((vector.x*vector.x)+(vector.y*vector.y))
	return {
		x: vector.x / (length || 1),
		y: vector.y / (length || 1)
	}
}


ORYX.Core.Math.getOrthogonalIdentityVector = function(point1, point2){
	var vec = arguments.length == 1 ? point1 : ORYX.Core.Math.getIdentityVector(point1, point2);
	return {
		x: vec.y,
		y: -vec.x
	}
}


/**
 * Returns the intersection point of a line and a point that defines a line
 * orthogonal to the given line.
 * 
 *  @param {float} lineP1
 *  	The starting point of the line segment
 *  @param {float} lineP2
 *  	The end point of the line segment
 *  @param {Point} point
 *  	The point to calculate the distance to.
 *  @param {boolean} onSegmentOnly
 *  	Flag to signal if only the segment of the line shell be evaluated.
 */
ORYX.Core.Math.getPointOfIntersectionPointLine = function(
													lineP1, 
													lineP2, 
													point, 
													onSegmentOnly) {

	/* 
	 * [P3 - P1 - u(P2 - P1)] dot (P2 - P1) = 0
	 * u =((x3-x1)(x2-x1)+(y3-y1)(y2-y1))/(p2-p1)??
	 */
	var denominator = Math.pow(lineP2.x - lineP1.x, 2) 
						+ Math.pow(lineP2.y - lineP1.y, 2);
	if(denominator == 0) {
		return undefined;
	}
	
	var u = ((point.x - lineP1.x) * (lineP2.x - lineP1.x)  
			+ (point.y - lineP1.y) * (lineP2.y - lineP1.y))
			/ denominator;
			
	if(onSegmentOnly) {
		if (!(0 <= u && u <= 1)) {
			return undefined;
		}
	}
	
	pointOfIntersection = new Object();
	pointOfIntersection.x = lineP1.x + u * (lineP2.x - lineP1.x);
	pointOfIntersection.y = lineP1.y + u * (lineP2.y - lineP1.y);	
	
	return pointOfIntersection;												
};

/**
 * Translated the point with the given matrix.
 * @param {Point} point
 * @param {Matrix} matrix 
 * @return {Object} Includes x, y
 */
ORYX.Core.Math.getTranslatedPoint = function(point, matrix){
	var x = matrix.a*point.x+matrix.c*point.y+matrix.e*1;
	var y = matrix.b*point.x+matrix.d*point.y+matrix.f*1;
    return {x:x, y:y}
}


/**
 * Returns the inverse matrix of the given SVG transformation matrix
 * @param {SVGTransformationMatrix} matrix
 * @return {Matrix}
 */
ORYX.Core.Math.getInverseMatrix = function(matrix){

	var det = ORYX.Core.Math.getDeterminant(matrix), m = matrix;
	// +-     -+
	// | a c e |
	// | b d f |
	// | 0 0 1 |
	// +-     -+
	return {
		a: det * ((m.d*1)-(m.f*0)),
		b: det * ((m.f*0)-(m.b*1)),
		c: det * ((m.e*0)-(m.c*1)),
		d: det * ((m.a*1)-(m.e*0)),
		e: det * ((m.c*m.f)-(m.e*m.d)),
		f: det * ((m.e*m.b)-(m.a*m.f))
	}
}

/**
 * Returns the determinant of the svg transformation matrix
 * @param {SVGTranformationMatrix} matrix
 * @return {Number}
 *
 */
ORYX.Core.Math.getDeterminant = function(m){
	// a11a22a33+a12a23a31+a13a21a32-a13a22a31-a12a21a33-a11a23a32
	return (m.a*m.d*1)+(m.c*m.f*0)+(m.e*m.b*0)-(m.e*m.d*0)-(m.c*m.b*1)-(m.a*m.f*0);
}

/**
 * Returns the bounding box of the given node. Translates the 
 * origin bounding box with the tranlation matrix.
 * @param {SVGElement} node
 * @return {Object} Includes x, y, width, height
 */
ORYX.Core.Math.getTranslatedBoundingBox = function(node){
	var matrix = node.getCTM();
	var bb = node.getBBox();
	var ul = ORYX.Core.Math.getTranslatedPoint({x:bb.x, y:bb.y}, matrix);
	var ll = ORYX.Core.Math.getTranslatedPoint({x:bb.x, y:bb.y+bb.height}, matrix);
	var ur = ORYX.Core.Math.getTranslatedPoint({x:bb.x+bb.width, y:bb.y}, matrix);
	var lr = ORYX.Core.Math.getTranslatedPoint({x:bb.x+bb.width, y:bb.y+bb.height}, matrix);
	
	var minPoint = {
	    x: Math.min(ul.x, ll.x, ur.x, lr.x),
	    y: Math.min(ul.y, ll.y, ur.y, lr.y)
	}
	var maxPoint = {
	    x: Math.max(ul.x, ll.x, ur.x, lr.x),
	    y: Math.max(ul.y, ll.y, ur.y, lr.y)
	}
	return {
	    x: minPoint.x,
	    y: minPoint.y,
	    width: maxPoint.x - minPoint.x,
	    height: maxPoint.y - minPoint.y
	}
};


/**
 * Returns the angle of the given line, which is representated by the two points
 * @param {Point} p1
 * @param {Point} p2
 * @return {Number} 0 <= x <= 359.99999
 */
ORYX.Core.Math.getAngle = function(p1, p2){
	if(p1.x == p2.x && p1.y == p2.y)
		return 0;

	var angle = Math.asin(Math.sqrt(Math.pow(p1.y-p2.y, 2))
				/(Math.sqrt(Math.pow(p2.x-p1.x, 2)+Math.pow(p1.y-p2.y, 2))))
				*180/Math.PI;
	
	if(p2.x >= p1.x && p2.y <= p1.y)
		return angle;
	else if(p2.x < p1.x && p2.y <= p1.y)
		return 180 - angle;
	else if(p2.x < p1.x && p2.y > p1.y)
		return 180 + angle;
	else
		return 360 - angle;
};


/**
 * Implementation of the cohen-sutherland algorithm
 */
new function(){
	
	var RIGHT = 2, TOP = 8,  BOTTOM = 4, LEFT = 1;
	
 	function computeOutCode (x, y, xmin, ymin, xmax, ymax) {
		var code = 0;
		if (y > ymax)
		    code |= TOP;
		else if (y < ymin)
		    code |= BOTTOM;
		if (x > xmax)
		    code |= RIGHT;
		else if (x < xmin)
		    code |= LEFT;
		return code;
	}
	
	/**
	 * Returns TRUE if the rectangle is over the edge and has intersection points or includes it
	 * @param {Object} x1 Point A of the line
	 * @param {Object} y1
	 * @param {Object} x2 Point B of the line
	 * @param {Object} y2
	 * @param {Object} xmin Point A of the rectangle
	 * @param {Object} ymin
	 * @param {Object} xmax Point B of the rectangle
	 * @param {Object} ymax
	 */
	ORYX.Core.Math.isRectOverLine = function(x1, y1, x2, y2, xmin, ymin, xmax, ymax){
		return !!ORYX.Core.Math.clipLineOnRect.apply(ORYX.Core.Math, arguments);
	}
	
	/**
	 * Returns the clipped line on the given rectangle. If there is 
	 * no intersection, it will return NULL.
	 *  
	 * @param {Object} x1 Point A of the line
	 * @param {Object} y1
	 * @param {Object} x2 Point B of the line
	 * @param {Object} y2
	 * @param {Object} xmin Point A of the rectangle
	 * @param {Object} ymin
	 * @param {Object} xmax Point B of the rectangle
	 * @param {Object} ymax
	 */
	ORYX.Core.Math.clipLineOnRect = function(x1, y1, x2, y2, xmin, ymin, xmax, ymax){
        //Outcodes for P0, P1, and whatever point lies outside the clip rectangle
        var outcode0, outcode1, outcodeOut, hhh = 0;
        var accept = false, done = false;
 
        //compute outcodes
        outcode0 = computeOutCode(x1, y1, xmin, ymin, xmax, ymax);
        outcode1 = computeOutCode(x2, y2, xmin, ymin, xmax, ymax);
 
        do {
            if ((outcode0 | outcode1) == 0 ){
                accept = true;
                done = true;
            } else if ( (outcode0 & outcode1) > 0 ) {
                done = true;
            } else {
                //failed both tests, so calculate the line segment to clip
                //from an outside point to an intersection with clip edge
                var x = 0, y = 0;
                //At least one endpoint is outside the clip rectangle; pick it.
                outcodeOut = outcode0 != 0 ? outcode0: outcode1;
                //Now find the intersection point;
                //use formulas y = y0 + slope * (x - x0), x = x0 + (1/slope)* (y - y0)
                if ( (outcodeOut & TOP) > 0 ) {
                    x = x1 + (x2 - x1) * (ymax - y1)/(y2 - y1);
                    y = ymax;
                } else if ((outcodeOut & BOTTOM) > 0 ) {
                    x = x1 + (x2 - x1) * (ymin - y1)/(y2 - y1);
                    y = ymin;
                } else if ((outcodeOut & RIGHT)> 0) {
                    y = y1 + (y2 - y1) * (xmax - x1)/(x2 - x1);
                    x = xmax;
                } else if ((outcodeOut & LEFT) > 0) {
                    y = y1 + (y2 - y1) * (xmin - x1)/(x2 - x1);
                    x = xmin;
                }
				
                //Now we move outside point to intersection point to clip
                //and get ready for next pass.
                if (outcodeOut == outcode0) {
                    x1 = x;
                    y1 = y;
                    outcode0 = computeOutCode (x1, y1, xmin, ymin, xmax, ymax);
                } else {
                    x2 = x;
                    y2 = y;
                    outcode1 = computeOutCode (x2, y2, xmin, ymin, xmax, ymax);
                }
            }
            hhh ++;
        } while (done  != true && hhh < 5000);
 
        if(accept) {
            return {a:{x:x1, y:y1}, b:{x:x2, y:y2}};
        }
		return null;
    }
}();


/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

/**
 * Init namespace
 */
if(!ORYX) {var ORYX = {};}
if(!ORYX.Core) {ORYX.Core = {};}
if(!ORYX.Core.StencilSet) {ORYX.Core.StencilSet = {};}

/**
 * Class Stencil
 * uses Prototpye 1.5.0
 * uses Inheritance
 * 
 * This class represents one stencil of a stencil set.
 */
ORYX.Core.StencilSet.Stencil = {

	/**
	 * Constructor
	 */
	construct: function(jsonStencil, namespace, source, stencilSet, propertyPackages, defaultPosition) {
		arguments.callee.$.construct.apply(this, arguments); // super();
		
		// check arguments and set defaults.
		if(!jsonStencil) throw "Stencilset seems corrupt.";
		if(!namespace) throw "Stencil does not provide namespace.";
		if(!source) throw "Stencil does not provide SVG source.";
		if(!stencilSet) throw "Fatal internal error loading stencilset.";
		//if(!propertyPackages) throw "Fatal internal error loading stencilset.";
		
		this._source = source;
		this._jsonStencil = jsonStencil;
		this._stencilSet = stencilSet;
		this._namespace = namespace;
		this._propertyPackages = propertyPackages;
		
		if(defaultPosition && !this._jsonStencil.position) 
			this._jsonStencil.position = defaultPosition;
		
		this._view;
		this._properties = new Hash();

		// check stencil consistency and set defaults.
		/*with(this._jsonStencil) {
			
			if(!type) throw "Stencil does not provide type.";
			if((type != "edge") && (type != "node"))
				throw "Stencil type must be 'edge' or 'node'.";
			if(!id || id == "") throw "Stencil does not provide valid id.";
			if(!title || title == "")
				throw "Stencil does not provide title";
			if(!description) { description = ""; };
			if(!groups) { groups = []; }
			if(!roles) { roles = []; }

			// add id of stencil to its roles
			roles.push(id);
		}*/
		
		//init all JSON values
		if(!this._jsonStencil.type || !(this._jsonStencil.type === "edge" || this._jsonStencil.type === "node")) {
			throw "ORYX.Core.StencilSet.Stencil(construct): Type is not defined.";
		}
		if(!this._jsonStencil.id || this._jsonStencil.id === "") {
			throw "ORYX.Core.StencilSet.Stencil(construct): Id is not defined.";
		}
		if(!this._jsonStencil.title || this._jsonStencil.title === "") {
			throw "ORYX.Core.StencilSet.Stencil(construct): Title is not defined.";
		}

		if(!this._jsonStencil.description) { this._jsonStencil.description = ""; };
		if(!this._jsonStencil.groups) { this._jsonStencil.groups = []; }
		if(!this._jsonStencil.roles) { this._jsonStencil.roles = []; }
		
		//add id of stencil to its roles
		this._jsonStencil.roles.push(this._jsonStencil.id);

		//prepend namespace to each role
		this._jsonStencil.roles.each((function(role, index) {
			this._jsonStencil.roles[index] = namespace + role;
		}).bind(this));

		//delete duplicate roles
		this._jsonStencil.roles = this._jsonStencil.roles.uniq();

		//make id unique by prepending namespace of stencil set
		this._jsonStencil.id = namespace + this._jsonStencil.id;

		this.postProcessProperties();
		
		// init serialize callback
		if(!this._jsonStencil.serialize) {
			this._jsonStencil.serialize = {};
			//this._jsonStencil.serialize = function(shape, data) { return data;};
		}
		
		// init deserialize callback
		if(!this._jsonStencil.deserialize) {
			this._jsonStencil.deserialize = {};
			//this._jsonStencil.deserialize = function(shape, data) { return data;};
		}
		
		// init layout callback
		if(!this._jsonStencil.layout) {
			this._jsonStencil.layout = []
			//this._jsonStencil.layout = function() {return true;}
		}
		
		//TODO does not work correctly, if the url does not exist
		//How to guarantee that the view is loaded correctly before leaving the constructor???
		var url = source + "view/" + jsonStencil.view;
		// override content type when this is webkit.
		
		if(this._jsonStencil.view.trim().match(/</)) {
			var parser	= new DOMParser();		
			var xml 	= parser.parseFromString( this._jsonStencil.view ,"text/xml");
			
			//check if result is a SVG document
			if( ORYX.Editor.checkClassType( xml.documentElement, SVGSVGElement )) {
	
				this._view = xml.documentElement;
				
			} else {
				throw "ORYX.Core.StencilSet.Stencil(_loadSVGOnSuccess): The response is not a SVG document."
			}
		} else {
			new Ajax.Request(
				url, {
					asynchronous:false, method:'get',
					onSuccess:this._loadSVGOnSuccess.bind(this),
					onFailure:this._loadSVGOnFailure.bind(this)
			});
		}
	},

	postProcessProperties: function() {

		// init property packages
		if(this._jsonStencil.propertyPackages && this._jsonStencil.propertyPackages instanceof Array) {
			
			this._jsonStencil.propertyPackages.each((function(ppId) {
				var pp = this._propertyPackages[ppId];
				
				if(pp) {
					pp.each((function(prop){
						var oProp = new ORYX.Core.StencilSet.Property(prop, this._namespace, this);
						this._properties[oProp.prefix() + "-" + oProp.id()] = oProp;
						
					}).bind(this));
				}
			}).bind(this));
		}
		
		// init properties
		if(this._jsonStencil.properties && this._jsonStencil.properties instanceof Array) {
			this._jsonStencil.properties.each((function(prop) {
				var oProp = new ORYX.Core.StencilSet.Property(prop, this._namespace, this);
				this._properties[oProp.prefix() + "-" + oProp.id()] = oProp;
			}).bind(this));
		}

	},

	/**
	 * @param {ORYX.Core.StencilSet.Stencil} stencil
	 * @return {Boolean} True, if stencil has the same namespace and type.
	 */
	equals: function(stencil) {
		return (this.id() === stencil.id());
	},

	stencilSet: function() {
		return this._stencilSet;
	},

	type: function() {
		return this._jsonStencil.type;
	},

	namespace: function() {
		return this._namespace;
	},

	id: function() {
		return this._jsonStencil.id;
	},
    
    idWithoutNs: function(){
        return this.id().replace(this.namespace(),"");
    },

	title: function() {
		return ORYX.Core.StencilSet.getTranslation(this._jsonStencil, "title");
	},

	description: function() {
		return ORYX.Core.StencilSet.getTranslation(this._jsonStencil, "description");
	},
	
	groups: function() {
		return ORYX.Core.StencilSet.getTranslation(this._jsonStencil, "groups");
	},
	
	position: function() {
		return (isNaN(this._jsonStencil.position) ? 0 : this._jsonStencil.position);
	},

	view: function() {
		return this._view.cloneNode(true) || this._view;
	},

	icon: function() {
		return this._jsonStencil.icon;
	},
	
	fixedAspectRatio: function() {
		return this._jsonStencil.fixedAspectRatio === true;
	},
	
	hasMultipleRepositoryEntries: function() {
		return (this.getRepositoryEntries().length > 0);
	},
	
	getRepositoryEntries: function() {
		return (this._jsonStencil.repositoryEntries) ?
			$A(this._jsonStencil.repositoryEntries) : $A([]);
	},
	
	properties: function() {
		return this._properties.values();
	},

	property: function(id) {
		return this._properties[id];
	},

	roles: function() {
		return this._jsonStencil.roles;
	},
	
	defaultAlign: function() {
		if(!this._jsonStencil.defaultAlign)
			return "east";
		return this._jsonStencil.defaultAlign;
	},

	serialize: function(shape, data) {
		return this._jsonStencil.serialize;
		//return this._jsonStencil.serialize(shape, data);
	},
	
	deserialize: function(shape, data) {
		return this._jsonStencil.deserialize;
		//return this._jsonStencil.deserialize(shape, data);
	},
	
	// in which case is targetShape used?
//	layout: function(shape, targetShape) {
//		return this._jsonStencil.layout(shape, targetShape);
//	},
	// layout property to store events for layouting in plugins
	layout: function(shape) {
		return this._jsonStencil.layout
	},
	
	addProperty: function(property, namespace) {
		if(property && namespace) {
			var oProp = new ORYX.Core.StencilSet.Property(property, namespace, this);
			this._properties[oProp.prefix() + "-" + oProp.id()] = oProp;
		}
	},
	
	removeProperty: function(propertyId) {
		if(propertyId) {
			var oProp = this._properties.values().find(function(prop) {
				return (propertyId == prop.id());
			});
			if(oProp)
				delete this._properties[oProp.prefix() + "-" + oProp.id()];
		}
	},

	_loadSVGOnSuccess: function(result) {
		
		var xml = null;
		
		/*
		 * We want to get a dom object for the requested file. Unfortunately,
		 * safari has some issues here. this is meant as a fallback for all
		 * browsers that don't recognize the svg mimetype as XML but support
		 * data: urls on Ajax calls.
		 */
		
		// responseXML != undefined.
		// if(!(result.responseXML))
		
			// get the dom by data: url.
			// xml = _evenMoreEvilHack(result.responseText, 'text/xml');
		
		// else
		
			// get it the usual way.
			xml = result.responseXML;

		//check if result is a SVG document
		if( ORYX.Editor.checkClassType( xml.documentElement, SVGSVGElement )) {

			this._view = xml.documentElement;
			
		} else {
			throw "ORYX.Core.StencilSet.Stencil(_loadSVGOnSuccess): The response is not a SVG document."
		}
	},

	_loadSVGOnFailure: function(result) {
		throw "ORYX.Core.StencilSet.Stencil(_loadSVGOnFailure): Loading SVG document failed."
	},

	toString: function() { return "Stencil " + this.title() + " (" + this.id() + ")"; }
};

ORYX.Core.StencilSet.Stencil = Clazz.extend(ORYX.Core.StencilSet.Stencil);

/**
 * Transform a string into an xml document, the Safari way, as long as
 * the nightlies are broken. Even more evil version.
 * @param {Object} str
 * @param {Object} contentType
 */
function _evenMoreEvilHack(str, contentType) {
	
	/*
	 * This even more evil hack was taken from
	 * http://web-graphics.com/mtarchive/001606.php#chatty004999
	 */
	
	if (window.ActiveXObject) {
		var d = new ActiveXObject("MSXML.DomDocument");
		d.loadXML(str);
		return d;
	} else if (window.XMLHttpRequest) {
		var req = new XMLHttpRequest;
		req.open("GET", "data:" + (contentType || "application/xml") +
						";charset=utf-8," + encodeURIComponent(str), false);
		if (req.overrideMimeType) {
			req.overrideMimeType(contentType);
		}
		req.send(null);
		return req.responseXML;
	}
}

/**
 * Transform a string into an xml document, the Safari way, as long as
 * the nightlies are broken.
 * @param {Object} result the xml document object.
 */
function _evilSafariHack(serializedXML) {
	
	/*
	 *  The Dave way. Taken from:
	 *  http://web-graphics.com/mtarchive/001606.php
	 *  
	 *  There is another possibility to parse XML in Safari, by implementing
	 *  the DOMParser in javascript. However, in the latest nightlies of
	 *  WebKit, DOMParser is already available, but still buggy. So, this is
	 *  the best compromise for the time being.
	 */		
	
	var xml = serializedXML;
	var url = "data:text/xml;charset=utf-8," + encodeURIComponent(xml);
	var dom = null;
	
	// your standard AJAX stuff
	var req = new XMLHttpRequest();
	req.open("GET", url);
	req.onload = function() { dom = req.responseXML; }
	req.send(null);
	
	return dom;
}
	/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

/**
 * Init namespace
 */
if (!ORYX) {
    var ORYX = {};
}
if (!ORYX.Core) {
    ORYX.Core = {};
}
if (!ORYX.Core.StencilSet) {
    ORYX.Core.StencilSet = {};
}

/**
 * Class Property
 * uses Prototpye 1.5.0
 * uses Inheritance
 */
ORYX.Core.StencilSet.Property = Clazz.extend({

    /**
     * Constructor
     */
    construct: function(jsonProp, namespace, stencil){
        arguments.callee.$.construct.apply(this, arguments);
        
        this._jsonProp = jsonProp || ORYX.Log.error("Parameter jsonProp is not defined.");
        this._namespace = namespace || ORYX.Log.error("Parameter namespace is not defined.");
        this._stencil = stencil || ORYX.Log.error("Parameter stencil is not defined.");
        
        this._items = {};
        this._complexItems = {};
        
	    // Flag to indicate whether or not the property should be hidden 
	    // This can be for example when the stencil set is upgraded, but the model
        // has a value for that specific property filled in which we still want to show.
        // If the value is missing, the property can simply be not shown.
        this._hidden = false;
        
        jsonProp.id = jsonProp.id || ORYX.Log.error("ORYX.Core.StencilSet.Property(construct): Id is not defined.");
		jsonProp.id = jsonProp.id.toLowerCase();
		
        if (!jsonProp.type) {
            ORYX.Log.info("Type is not defined for stencil '%0', id '%1'. Falling back to 'String'.", stencil, jsonProp.id);
            jsonProp.type = "string";
        }
        else {
            jsonProp.type = jsonProp.type.toLowerCase();
        }
        
        jsonProp.prefix = jsonProp.prefix || "oryx";
        jsonProp.title = jsonProp.title || "";
        jsonProp.value = jsonProp.value || "";
        jsonProp.description = jsonProp.description || "";
        jsonProp.readonly = jsonProp.readonly || false;
        jsonProp.optional = jsonProp.optional !== false;
        
        //init refToView
        if (this._jsonProp.refToView) {
            if (!(this._jsonProp.refToView instanceof Array)) {
                this._jsonProp.refToView = [this._jsonProp.refToView];
            }
        }
        else {
            this._jsonProp.refToView = [];
        }
        
		var globalMin = this.getMinForType(jsonProp.type);
        if (jsonProp.min === undefined || jsonProp.min === null) {
            jsonProp.min =globalMin;
        } else if (jsonProp.min < globalMin) {
			jsonProp.min = globalMin;
		}
        
		var globalMax = this.getMaxForType(jsonProp.type);
        if (jsonProp.max === undefined || jsonProp.max === null) {
            jsonProp.max = globalMax;
        } else if (jsonProp.max > globalMax) {
			jsonProp.min = globalMax;
		}
        
        if (!jsonProp.fillOpacity) {
            jsonProp.fillOpacity = false;
        }
		
		if ("number" != typeof jsonProp.lightness) {
			jsonProp.lightness = 1;
		} else {
			jsonProp.lightness = Math.max(0, Math.min(1, jsonProp.lightness));
		}
        
        if (!jsonProp.strokeOpacity) {
            jsonProp.strokeOpacity = false;
        }
        
        if (jsonProp.length === undefined || jsonProp.length === null) {
            jsonProp.length = Number.MAX_VALUE;
        }
        
        if (!jsonProp.wrapLines) {
            jsonProp.wrapLines = false;
        }
        
        if (!jsonProp.dateFormat) {
            jsonProp.dateFormat = ORYX.I18N.PropertyWindow.dateFormat || "m/d/y";
        }
        
        if (!jsonProp.fill) {
            jsonProp.fill = false;
        }
        
        if (!jsonProp.stroke) {
            jsonProp.stroke = false;
        }
        
        if(!jsonProp.inverseBoolean) {
        	jsonProp.inverseBoolean = false;
        }
		
		if(!jsonProp.directlyEditable && jsonProp.directlyEditable != false) {
        	jsonProp.directlyEditable = true;
        }
		
		if(jsonProp.visible !== false) {
			jsonProp.visible = true;
		}
		
		if(jsonProp.isList !== true) {
			jsonProp.isList = false;
			
			if(!jsonProp.list || !(jsonProp.list instanceof Array)) {
				jsonProp.list = [];
			}	
		}
		
		if(!jsonProp.category) {
			if (jsonProp.popular) {
				jsonProp.category = "popular";
			} else {
				jsonProp.category = "others";
			}
		}
		
		if(!jsonProp.alwaysAppearInMultiselect) {
			jsonProp.alwaysAppearInMultiselect = false;
		}
        
        if (jsonProp.type === ORYX.CONFIG.TYPE_CHOICE) {
            if (jsonProp.items && jsonProp.items instanceof Array) {
                jsonProp.items.each((function(jsonItem){
                	// why is the item's value used as the key???
                    this._items[jsonItem.value.toLowerCase()] = new ORYX.Core.StencilSet.PropertyItem(jsonItem, namespace, this);
                }).bind(this));
            }
            else {
                throw "ORYX.Core.StencilSet.Property(construct): No property items defined."
            }
            // extended by Kerstin (start)
        }
        else 
            if (jsonProp.type === ORYX.CONFIG.TYPE_COMPLEX || jsonProp.type == ORYX.CONFIG.TYPE_MULTIPLECOMPLEX) {
                if (jsonProp.complexItems && jsonProp.complexItems instanceof Array) {
                    jsonProp.complexItems.each((function(jsonComplexItem){
                        this._complexItems[jsonComplexItem.id.toLowerCase()] = new ORYX.Core.StencilSet.ComplexPropertyItem(jsonComplexItem, namespace, this);
                    }).bind(this));
                }
            }
        // extended by Kerstin (end)
    },
	
	getMinForType : function(type) {
		if (type.toLowerCase() == ORYX.CONFIG.TYPE_INTEGER) {
			return -Math.pow(2,31)
		} else {
			return -Number.MAX_VALUE+1;
		}
	}, 
	getMaxForType : function(type) {
		if (type.toLowerCase() == ORYX.CONFIG.TYPE_INTEGER) {
			return Math.pow(2,31)-1
		} else {
			return Number.MAX_VALUE;
		}
	},
    
    /**
     * @param {ORYX.Core.StencilSet.Property} property
     * @return {Boolean} True, if property has the same namespace and id.
     */
    equals: function(property){
        return (this._namespace === property.namespace() &&
        this.id() === property.id()) ? true : false;
    },
    
    namespace: function(){
        return this._namespace;
    },
    
    stencil: function(){
        return this._stencil;
    },
    
    id: function(){
        return this._jsonProp.id;
    },
    
    prefix: function(){
        return this._jsonProp.prefix;
    },
    
    type: function(){
        return this._jsonProp.type;
    },
    
    inverseBoolean: function() {
    	return this._jsonProp.inverseBoolean;
    },
	
	category: function() {
		return this._jsonProp.category;
	},
	
	setCategory: function(value) {
		this._jsonProp.category = value;
	},
	
	directlyEditable: function() {
		return this._jsonProp.directlyEditable;
	},
	
	visible: function() {
		return this._jsonProp.visible;
	},
    
    title: function(){
        return ORYX.Core.StencilSet.getTranslation(this._jsonProp, "title");
    },
    
    value: function(){
        return this._jsonProp.value;
    },
    
    readonly: function(){
        return this._jsonProp.readonly;
    },
    
    optional: function(){
        return this._jsonProp.optional;
    },
    
    description: function(){
        return ORYX.Core.StencilSet.getTranslation(this._jsonProp, "description");
    },
	
    /**
     * An optional link to a SVG element so that the property affects the
     * graphical representation of the stencil.
     */
    refToView: function(){
        return this._jsonProp.refToView;
    },
    
    /**
     * If type is integer or float, min is the lower bounds of value.
     */
    min: function(){
        return this._jsonProp.min;
    },
    
    /**
     * If type ist integer or float, max is the upper bounds of value.
     */
    max: function(){
        return this._jsonProp.max;
    },
    
    /**
     * If type is float, this method returns if the fill-opacity property should
     *  be set.
     *  @return {Boolean}
     */
    fillOpacity: function(){
        return this._jsonProp.fillOpacity;
    },
    
    /**
     * If type is float, this method returns if the stroke-opacity property should
     *  be set.
     *  @return {Boolean}
     */
    strokeOpacity: function(){
        return this._jsonProp.strokeOpacity;
    },
    
    /**
     * If type is string or richtext, length is the maximum length of the text.
     * TODO how long can a string be.
     */
    length: function(){
        return this._jsonProp.length ? this._jsonProp.length : Number.MAX_VALUE;
    },
    
    wrapLines: function(){
        return this._jsonProp.wrapLines;
    },
    
    /**
     * If type is date, dateFormat specifies the format of the date. The format
     * specification of the ext library is used:
     *
     * Format  Output      Description
     *	------  ----------  --------------------------------------------------------------
     *	  d      10         Day of the month, 2 digits with leading zeros
     *	  D      Wed        A textual representation of a day, three letters
     *	  j      10         Day of the month without leading zeros
     *	  l      Wednesday  A full textual representation of the day of the week
     *	  S      th         English ordinal day of month suffix, 2 chars (use with j)
     *	  w      3          Numeric representation of the day of the week
     *	  z      9          The julian date, or day of the year (0-365)
     *	  W      01         ISO-8601 2-digit week number of year, weeks starting on Monday (00-52)
     *	  F      January    A full textual representation of the month
     *	  m      01         Numeric representation of a month, with leading zeros
     *	  M      Jan        Month name abbreviation, three letters
     *	  n      1          Numeric representation of a month, without leading zeros
     *	  t      31         Number of days in the given month
     *	  L      0          Whether its a leap year (1 if it is a leap year, else 0)
     *	  Y      2007       A full numeric representation of a year, 4 digits
     *	  y      07         A two digit representation of a year
     *	  a      pm         Lowercase Ante meridiem and Post meridiem
     *	  A      PM         Uppercase Ante meridiem and Post meridiem
     *	  g      3          12-hour format of an hour without leading zeros
     *	  G      15         24-hour format of an hour without leading zeros
     *	  h      03         12-hour format of an hour with leading zeros
     *	  H      15         24-hour format of an hour with leading zeros
     *	  i      05         Minutes with leading zeros
     *	  s      01         Seconds, with leading zeros
     *	  O      -0600      Difference to Greenwich time (GMT) in hours
     *	  T      CST        Timezone setting of the machine running the code
     *	  Z      -21600     Timezone offset in seconds (negative if west of UTC, positive if east)
     *
     * Example:
     *  F j, Y, g:i a  ->  January 10, 2007, 3:05 pm
     */
    dateFormat: function(){
        return this._jsonProp.dateFormat;
    },
    
    /**
     * If type is color, this method returns if the fill property should
     *  be set.
     *  @return {Boolean}
     */
    fill: function(){
        return this._jsonProp.fill;
    },
	
	/**
	 * Lightness defines the satiation of the color
	 * 0 is the pure color
	 * 1 is white
	 * @return {Integer} lightness
	 */
	lightness: function(){
		return this._jsonProp.lightness;
	},
    
    /**
     * If type is color, this method returns if the stroke property should
     *  be set.
     *  @return {Boolean}
     */
    stroke: function(){
        return this._jsonProp.stroke;
    },
    
    /**
     * If type is choice, items is a hash map with all alternative values
     * (PropertyItem objects) with id as keys.
     */
    items: function(){
        return $H(this._items).values();
    },
    
    item: function(value){
        if (value) {
			return this._items[value.toLowerCase()];
		} else {
			return null;
		}
    },
    
    toString: function(){
        return "Property " + this.title() + " (" + this.id() + ")";
    },
    
    complexItems: function(){
        return $H(this._complexItems).values();
    },
    
    complexItem: function(id){
        if(id) {
			return this._complexItems[id.toLowerCase()];
		} else {
			return null;
		}
		
    },
    
    complexAttributeToView: function(){
        return this._jsonProp.complexAttributeToView || "";
    },
    
    isList: function() {
    	return !!this._jsonProp.isList;
    },
    
    getListItems: function() {
    	return this._jsonProp.list;
    },
	
	/**
	 * If type is glossary link, the 
	 * type of category can be defined where
	 * the link only can go to.
	 * @return {String} The glossary category id 
	 */
	linkableType: function(){
		return this._jsonProp.linkableType || "";
	},
	
	alwaysAppearInMultiselect : function() {
		return this._jsonProp.alwaysAppearInMultiselect;
	},
	
	popular: function() {
		return this._jsonProp.popular || false;
	},
	
	setPopular: function() {
		this._jsonProp.popular = true;
	},
	
	hide: function() {
		this._hidden = true;
	},
	
	isHidden: function() {
		return this._hidden;
	}
	
});
/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

/**
 * Init namespace
 */
if(!ORYX) {var ORYX = {};}
if(!ORYX.Core) {ORYX.Core = {};}
if(!ORYX.Core.StencilSet) {ORYX.Core.StencilSet = {};}

/**
 * Class Stencil
 * uses Prototpye 1.5.0
 * uses Inheritance
 */
ORYX.Core.StencilSet.PropertyItem = Clazz.extend({

	/**
	 * Constructor
	 */
	construct: function(jsonItem, namespace, property) {
		arguments.callee.$.construct.apply(this, arguments);

		if(!jsonItem) {
			throw "ORYX.Core.StencilSet.PropertyItem(construct): Parameter jsonItem is not defined.";
		}
		if(!namespace) {
			throw "ORYX.Core.StencilSet.PropertyItem(construct): Parameter namespace is not defined.";
		}
		if(!property) {
			throw "ORYX.Core.StencilSet.PropertyItem(construct): Parameter property is not defined.";
		}
		
		this._jsonItem = jsonItem;
		this._namespace = namespace;
		this._property = property;
		
		//init all values
		if(!jsonItem.value) {
			throw "ORYX.Core.StencilSet.PropertyItem(construct): Value is not defined.";
		}
		
		if(this._jsonItem.refToView) {
			if(!(this._jsonItem.refToView instanceof Array)) {
				this._jsonItem.refToView = [this._jsonItem.refToView];
			}
		} else {
			this._jsonItem.refToView = [];
		}
	},

	/**
	 * @param {ORYX.Core.StencilSet.PropertyItem} item
	 * @return {Boolean} True, if item has the same namespace and id.
	 */
	equals: function(item) {
		return (this.property().equals(item.property()) &&
			this.value() === item.value());
	},

	namespace: function() {
		return this._namespace;
	},

	property: function() {
		return this._property;
	},

	value: function() {
		return this._jsonItem.value;
	},
	
	title: function() {
		return ORYX.Core.StencilSet.getTranslation(this._jsonItem, "title");
	},

	refToView: function() {
		return this._jsonItem.refToView;
	},
	
	icon: function() {
		return (this._jsonItem.icon) ? this.property().stencil()._source + "icons/" + this._jsonItem.icon : "";
	},

	toString: function() { return "PropertyItem " + this.property() + " (" + this.value() + ")"; }
});/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

/**
 * Init namespaces
 */
if(!ORYX) {var ORYX = {};}
if(!ORYX.Core) {ORYX.Core = {};}
if(!ORYX.Core.StencilSet) {ORYX.Core.StencilSet = {};}

/**
 * Class Stencil
 * uses Prototpye 1.5.0
 * uses Inheritance
 */
ORYX.Core.StencilSet.ComplexPropertyItem = Clazz.extend({

	/**
	 * Constructor
	 */
	construct: function(jsonItem, namespace, property) {
		arguments.callee.$.construct.apply(this, arguments);

		if(!jsonItem) {
			throw "ORYX.Core.StencilSet.ComplexPropertyItem(construct): Parameter jsonItem is not defined.";
		}
		if(!namespace) {
			throw "ORYX.Core.StencilSet.ComplexPropertyItem(construct): Parameter namespace is not defined.";
		}
		if(!property) {
			throw "ORYX.Core.StencilSet.ComplexPropertyItem(construct): Parameter property is not defined.";
		}
		
		this._jsonItem = jsonItem;
		this._namespace = namespace;
		this._property = property;
		this._items = new Hash();
		this._complexItems = new Hash();
		
		//init all values
		if(!jsonItem.name) {
			throw "ORYX.Core.StencilSet.ComplexPropertyItem(construct): Name is not defined.";
		}
		
		if(!jsonItem.type) {
			throw "ORYX.Core.StencilSet.ComplexPropertyItem(construct): Type is not defined.";
		} else {
			jsonItem.type = jsonItem.type.toLowerCase();
		}
		
		if(jsonItem.type === ORYX.CONFIG.TYPE_CHOICE) {
			if(jsonItem.items && jsonItem.items instanceof Array) {
				jsonItem.items.each((function(item) {
					this._items[item.value] = new ORYX.Core.StencilSet.PropertyItem(item, namespace, this);
				}).bind(this));
			} else {
				throw "ORYX.Core.StencilSet.Property(construct): No property items defined."
			}
		} else if(jsonItem.type === ORYX.CONFIG.TYPE_COMPLEX) {
			if(jsonItem.complexItems && jsonItem.complexItems instanceof Array) {
				jsonItem.complexItems.each((function(complexItem) {
					this._complexItems[complexItem.id] = new ORYX.Core.StencilSet.ComplexPropertyItem(complexItem, namespace, this);
				}).bind(this));
			} else {
				throw "ORYX.Core.StencilSet.Property(construct): No property items defined."
			}
		}
	},

	/**
	 * @param {ORYX.Core.StencilSet.PropertyItem} item
	 * @return {Boolean} True, if item has the same namespace and id.
	 */
	equals: function(item) {
		return (this.property().equals(item.property()) &&
			this.name() === item.name());
	},

	namespace: function() {
		return this._namespace;
	},

	property: function() {
		return this._property;
	},

	name: function() {
		return ORYX.Core.StencilSet.getTranslation(this._jsonItem, "name");
	},
	
	id: function() {
		return this._jsonItem.id;
	},
	
	type: function() {
		return this._jsonItem.type;
	},
	
	optional: function() {
		return this._jsonItem.optional;
	},
	
	width: function() {
		return this._jsonItem.width;
	},
	
	value: function() {
		return this._jsonItem.value;
	},
	
	items: function() {
		return this._items.values();
	},
	
	complexItems: function() {
		return this._complexItems.values();
	},
	
	disable: function() {
		return this._jsonItem.disable;
	}
});/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

/**
 * Init namespaces
 */
if(!ORYX) {var ORYX = {};}
if(!ORYX.Core) {ORYX.Core = {};}
if(!ORYX.Core.StencilSet) {ORYX.Core.StencilSet = {};}

/**
 * Class Rules uses Prototpye 1.5.0 uses Inheritance
 * 
 * This class implements the API to check the stencil sets' rules.
 */
ORYX.Core.StencilSet.Rules = {

	/**
	 * Constructor
	 */
	construct: function() {
		arguments.callee.$.construct.apply(this, arguments);

		this._stencilSets = [];
		this._stencils = [];
		this._containerStencils = [];
		
		this._cachedConnectSET = new Hash();
		this._cachedConnectSE = new Hash();
		this._cachedConnectTE = new Hash();
		this._cachedCardSE = new Hash();
		this._cachedCardTE = new Hash();
		this._cachedContainPC = new Hash();
		this._cachedMorphRS = new Hash();
		
		this._connectionRules = new Hash();
		this._cardinalityRules = new Hash();
		this._containmentRules = new Hash();
		this._morphingRules = new Hash();
		this._layoutRules = new Hash();
	},
	
	/**
	 * Call this method to initialize the rules for a stencil set and all of its
	 * active extensions.
	 * 
	 * @param {Object}
	 *            stencilSet
	 */
	initializeRules: function(stencilSet) {
		
		var existingSS = this._stencilSets.find(function(ss) {
							return (ss.namespace() == stencilSet.namespace());
						});
		if (existingSS) {
			// reinitialize all rules
			var stencilsets = this._stencilSets.clone();
			stencilsets = stencilsets.without(existingSS);
			stencilsets.push(stencilSet);
			
			this._stencilSets = [];
			this._stencils = [];
			this._containerStencils = [];
			
			this._cachedConnectSET = new Hash();
			this._cachedConnectSE = new Hash();
			this._cachedConnectTE = new Hash();
			this._cachedCardSE = new Hash();
			this._cachedCardTE = new Hash();
			this._cachedContainPC = new Hash();
			this._cachedMorphRS = new Hash();
			
			this._connectionRules = new Hash();
			this._cardinalityRules = new Hash();
			this._containmentRules = new Hash();
			this._morphingRules = new Hash();
			this._layoutRules = new Hash();
			
			stencilsets.each(function(ss){
				this.initializeRules(ss);
			}.bind(this));
			return;
		}
		else {
			this._stencilSets.push(stencilSet);
			
			var jsonRules = new Hash(stencilSet.jsonRules());
			var namespace = stencilSet.namespace();
			var stencils = stencilSet.stencils();
			
			stencilSet.extensions().values().each(function(extension) {
				if(extension.rules) {
					if(extension.rules.connectionRules)
						jsonRules.connectionRules = jsonRules.connectionRules.concat(extension.rules.connectionRules);
					if(extension.rules.cardinalityRules)
						jsonRules.cardinalityRules = jsonRules.cardinalityRules.concat(extension.rules.cardinalityRules);
					if(extension.rules.containmentRules)
						jsonRules.containmentRules = jsonRules.containmentRules.concat(extension.rules.containmentRules);
					if(extension.rules.morphingRules)
						jsonRules.morphingRules = jsonRules.morphingRules.concat(extension.rules.morphingRules);
				}
				if(extension.stencils) 
					stencils = stencils.concat(extension.stencils);
			});
			
			this._stencils = this._stencils.concat(stencilSet.stencils());
			
			// init connection rules
			var cr = this._connectionRules;
			if (jsonRules.connectionRules) {
				jsonRules.connectionRules.each((function(rules){
					if (this._isRoleOfOtherNamespace(rules.role)) {
						if (!cr[rules.role]) {
							cr[rules.role] = new Hash();
						}
					}
					else {
						if (!cr[namespace + rules.role]) 
							cr[namespace + rules.role] = new Hash();
					}
					
					rules.connects.each((function(connect){
						var toRoles = [];
						if (connect.to) {
							if (!(connect.to instanceof Array)) {
								connect.to = [connect.to];
							}
							connect.to.each((function(to){
								if (this._isRoleOfOtherNamespace(to)) {
									toRoles.push(to);
								}
								else {
									toRoles.push(namespace + to);
								}
							}).bind(this));
						}
						
						var role, from;
						if (this._isRoleOfOtherNamespace(rules.role)) 
							role = rules.role;
						else 
							role = namespace + rules.role;
						
						if (this._isRoleOfOtherNamespace(connect.from)) 
							from = connect.from;
						else 
							from = namespace + connect.from;
						
						if (!cr[role][from]) 
							cr[role][from] = toRoles;
						else 
							cr[role][from] = cr[role][from].concat(toRoles);
						
					}).bind(this));
				}).bind(this));
			}
			
			// init cardinality rules
			var cardr = this._cardinalityRules;
			if (jsonRules.cardinalityRules) {
				jsonRules.cardinalityRules.each((function(rules){
					var cardrKey;
					if (this._isRoleOfOtherNamespace(rules.role)) {
						cardrKey = rules.role;
					}
					else {
						cardrKey = namespace + rules.role;
					}
					
					if (!cardr[cardrKey]) {
						cardr[cardrKey] = {};
						for (i in rules) {
							cardr[cardrKey][i] = rules[i];
						}
					}
					
					var oe = new Hash();
					if (rules.outgoingEdges) {
						rules.outgoingEdges.each((function(rule){
							if (this._isRoleOfOtherNamespace(rule.role)) {
								oe[rule.role] = rule;
							}
							else {
								oe[namespace + rule.role] = rule;
							}
						}).bind(this));
					}
					cardr[cardrKey].outgoingEdges = oe;
					var ie = new Hash();
					if (rules.incomingEdges) {
						rules.incomingEdges.each((function(rule){
							if (this._isRoleOfOtherNamespace(rule.role)) {
								ie[rule.role] = rule;
							}
							else {
								ie[namespace + rule.role] = rule;
							}
						}).bind(this));
					}
					cardr[cardrKey].incomingEdges = ie;
				}).bind(this));
			}
			
			// init containment rules
			var conr = this._containmentRules;
			if (jsonRules.containmentRules) {
				jsonRules.containmentRules.each((function(rules){
					var conrKey;
					if (this._isRoleOfOtherNamespace(rules.role)) {
						conrKey = rules.role;
					}
					else {
						this._containerStencils.push(namespace + rules.role);
						conrKey = namespace + rules.role;
					}
					if (!conr[conrKey]) {
						conr[conrKey] = [];
					}
					(rules.contains||[]).each((function(containRole){
						if (this._isRoleOfOtherNamespace(containRole)) {
							conr[conrKey].push(containRole);
						}
						else {
							conr[conrKey].push(namespace + containRole);
						}
					}).bind(this));
				}).bind(this));
			}
			
			// init morphing rules
			var morphr = this._morphingRules;
			if (jsonRules.morphingRules) {
				jsonRules.morphingRules.each((function(rules){
					var morphrKey;
					if (this._isRoleOfOtherNamespace(rules.role)) {
						morphrKey = rules.role;
					}
					else {
						morphrKey = namespace + rules.role;
					}
					if (!morphr[morphrKey]) {
						morphr[morphrKey] = [];
					}
					if(!rules.preserveBounds) {
						rules.preserveBounds = false;
					}
					rules.baseMorphs.each((function(baseMorphStencilId){
						var morphStencil = this._getStencilById(namespace + baseMorphStencilId);
						if(morphStencil) {
							morphr[morphrKey].push(morphStencil);
						}
					}).bind(this));
				}).bind(this));
			}
			
			// init layouting rules
			var layoutRules = this._layoutRules;
			if (jsonRules.layoutRules) {
				
				var getDirections = function(o){
					return {
							"edgeRole":o.edgeRole||undefined,
							"t": o["t"]||1,
							"r": o["r"]||1,
							"b": o["b"]||1,
							"l": o["l"]||1
						}
				}
				
				jsonRules.layoutRules.each(function(rules){
					var layoutKey;
					if (this._isRoleOfOtherNamespace(rules.role)) {
						layoutKey = rules.role;
					}
					else {
						layoutKey = namespace + rules.role;
					}
					if (!layoutRules[layoutKey]) {
						layoutRules[layoutKey] = {};
					}
					if (rules["in"]){
						layoutRules[layoutKey]["in"] = getDirections(rules["in"]);
					}
					if (rules["ins"]){
						layoutRules[layoutKey]["ins"] = (rules["ins"]||[]).map(function(e){ return getDirections(e) })
					}
					if (rules["out"]) {
						layoutRules[layoutKey]["out"] = getDirections(rules["out"]);
					}
					if (rules["outs"]){
						layoutRules[layoutKey]["outs"] = (rules["outs"]||[]).map(function(e){ return getDirections(e) })
					}
				}.bind(this));
			}			
		}
	},
	
	_getStencilById: function(id) {
		return this._stencils.find(function(stencil) {
			return stencil.id()==id;
		});
	},
	
	_cacheConnect: function(args) {
		result = this._canConnect(args);
		
		if (args.sourceStencil && args.targetStencil) {
			var source = this._cachedConnectSET[args.sourceStencil.id()];
			
			if(!source) {
				source = new Hash();
				this._cachedConnectSET[args.sourceStencil.id()] = source;
			}
			
			var edge = source[args.edgeStencil.id()];
			
			if(!edge) {
				edge = new Hash();
				source[args.edgeStencil.id()] = edge;
			}
			
			edge[args.targetStencil.id()] = result;
			
		} else if (args.sourceStencil) {
			var source = this._cachedConnectSE[args.sourceStencil.id()];
			
			if(!source) {
				source = new Hash();
				this._cachedConnectSE[args.sourceStencil.id()] = source;
			}
			
			source[args.edgeStencil.id()] = result;

		} else {
			var target = this._cachedConnectTE[args.targetStencil.id()];
			
			if(!target) {
				target = new Hash();
				this._cachedConnectTE[args.targetStencil.id()] = target;
			}
			
			target[args.edgeStencil.id()] = result;
		}
		
		return result;
	},
	
	_cacheCard: function(args) {
			
		if(args.sourceStencil) {
			var source = this._cachedCardSE[args.sourceStencil.id()]
			
			if(!source) {
				source = new Hash();
				this._cachedCardSE[args.sourceStencil.id()] = source;
			}
			
			var max = this._getMaximumNumberOfOutgoingEdge(args);
			if(max == undefined)
				max = -1;
				
			source[args.edgeStencil.id()] = max;
		}	
		
		if(args.targetStencil) {
			var target = this._cachedCardTE[args.targetStencil.id()]
			
			if(!target) {
				target = new Hash();
				this._cachedCardTE[args.targetStencil.id()] = target;
			}
			
			var max = this._getMaximumNumberOfIncomingEdge(args);
			if(max == undefined)
				max = -1;
				
			target[args.edgeStencil.id()] = max;
		}
	},
	
	_cacheContain: function(args) {
		
		var result = [this._canContain(args), 
					  this._getMaximumOccurrence(args.containingStencil, args.containedStencil)]
		
		if(result[1] == undefined) 
			result[1] = -1;
		
		var children = this._cachedContainPC[args.containingStencil.id()];
		
		if(!children) {
			children = new Hash();
			this._cachedContainPC[args.containingStencil.id()] = children;
		}
		
		children[args.containedStencil.id()] = result;
		
		return result;
	},
	
	/**
	 * Returns all stencils belonging to a morph group. (calculation result is
	 * cached)
	 */
	_cacheMorph: function(role) {
		
		var morphs = this._cachedMorphRS[role];
		
		if(!morphs) {
			morphs = [];
			
			if(this._morphingRules.keys().include(role)) {
				morphs = this._stencils.select(function(stencil) {
					return stencil.roles().include(role);
				});
			}
			
			this._cachedMorphRS[role] = morphs;
		}
		return morphs;
	},
	
	/** Begin connection rules' methods */
	
	/**
	 * 
	 * @param {Object}
	 *            args sourceStencil: ORYX.Core.StencilSet.Stencil | undefined
	 *            sourceShape: ORYX.Core.Shape | undefined
	 * 
	 * At least sourceStencil or sourceShape has to be specified
	 * 
	 * @return {Array} Array of stencils of edges that can be outgoing edges of
	 *         the source.
	 */
	outgoingEdgeStencils: function(args) {
		// check arguments
		if(!args.sourceShape && !args.sourceStencil) {
			return [];
		}
		
		// init arguments
		if(args.sourceShape) {
			args.sourceStencil = args.sourceShape.getStencil();
		}
		
		var _edges = [];
		
		// test each edge, if it can connect to source
		this._stencils.each((function(stencil) {
			if(stencil.type() === "edge") {
				var newArgs = Object.clone(args);
				newArgs.edgeStencil = stencil;
				if(this.canConnect(newArgs)) {
					_edges.push(stencil);
				}
			}
		}).bind(this));

		return _edges;
	},

	/**
	 * 
	 * @param {Object}
	 *            args targetStencil: ORYX.Core.StencilSet.Stencil | undefined
	 *            targetShape: ORYX.Core.Shape | undefined
	 * 
	 * At least targetStencil or targetShape has to be specified
	 * 
	 * @return {Array} Array of stencils of edges that can be incoming edges of
	 *         the target.
	 */
	incomingEdgeStencils: function(args) {
		// check arguments
		if(!args.targetShape && !args.targetStencil) {
			return [];
		}
		
		// init arguments
		if(args.targetShape) {
			args.targetStencil = args.targetShape.getStencil();
		}
		
		var _edges = [];
		
		// test each edge, if it can connect to source
		this._stencils.each((function(stencil) {
			if(stencil.type() === "edge") {
				var newArgs = Object.clone(args);
				newArgs.edgeStencil = stencil;
				if(this.canConnect(newArgs)) {
					_edges.push(stencil);
				}
			}
		}).bind(this));

		return _edges;
	},
	
	/**
	 * 
	 * @param {Object}
	 *            args edgeStencil: ORYX.Core.StencilSet.Stencil | undefined
	 *            edgeShape: ORYX.Core.Edge | undefined targetStencil:
	 *            ORYX.Core.StencilSet.Stencil | undefined targetShape:
	 *            ORYX.Core.Node | undefined
	 * 
	 * At least edgeStencil or edgeShape has to be specified!!!
	 * 
	 * @return {Array} Returns an array of stencils that can be source of the
	 *         specified edge.
	 */
	sourceStencils: function(args) {
		// check arguments
		if(!args || 
		   !args.edgeShape && !args.edgeStencil) {
			return [];
		}
		
		// init arguments
		if(args.targetShape) {
			args.targetStencil = args.targetShape.getStencil();
		}
		
		if(args.edgeShape) {
			args.edgeStencil = args.edgeShape.getStencil();
		}
		
		var _sources = [];
		
		// check each stencil, if it can be a source
		this._stencils.each((function(stencil) {
			var newArgs = Object.clone(args);
			newArgs.sourceStencil = stencil;
			if(this.canConnect(newArgs)) {
				_sources.push(stencil);
			}
		}).bind(this));

		return _sources;
	},
	
	/**
	 * 
	 * @param {Object}
	 *            args edgeStencil: ORYX.Core.StencilSet.Stencil | undefined
	 *            edgeShape: ORYX.Core.Edge | undefined sourceStencil:
	 *            ORYX.Core.StencilSet.Stencil | undefined sourceShape:
	 *            ORYX.Core.Node | undefined
	 * 
	 * At least edgeStencil or edgeShape has to be specified!!!
	 * 
	 * @return {Array} Returns an array of stencils that can be target of the
	 *         specified edge.
	 */
	targetStencils: function(args) {
		// check arguments
		if(!args || 
		   !args.edgeShape && !args.edgeStencil) {
			return [];
		}
		
		// init arguments
		if(args.sourceShape) {
			args.sourceStencil = args.sourceShape.getStencil();
		}
		
		if(args.edgeShape) {
			args.edgeStencil = args.edgeShape.getStencil();
		}
		
		var _targets = [];
		
		// check stencil, if it can be a target
		this._stencils.each((function(stencil) {
			var newArgs = Object.clone(args);
			newArgs.targetStencil = stencil;
			if(this.canConnect(newArgs)) {
				_targets.push(stencil);
			}
		}).bind(this));

		return _targets;
	},

	/**
	 * 
	 * @param {Object}
	 *            args edgeStencil: ORYX.Core.StencilSet.Stencil edgeShape:
	 *            ORYX.Core.Edge |undefined sourceStencil:
	 *            ORYX.Core.StencilSet.Stencil | undefined sourceShape:
	 *            ORYX.Core.Node |undefined targetStencil:
	 *            ORYX.Core.StencilSet.Stencil | undefined targetShape:
	 *            ORYX.Core.Node |undefined
	 * 
	 * At least source or target has to be specified!!!
	 * 
	 * @return {Boolean} Returns, if the edge can connect source and target.
	 */
	canConnect: function(args) {	
		// check arguments
		if(!args ||
		   (!args.sourceShape && !args.sourceStencil &&
		    !args.targetShape && !args.targetStencil) ||
		    !args.edgeShape && !args.edgeStencil) {
		   	return false; 
		}
		
		// init arguments
		if(args.sourceShape) {
			args.sourceStencil = args.sourceShape.getStencil();
		}
		if(args.targetShape) {
			args.targetStencil = args.targetShape.getStencil();
		}
		if(args.edgeShape) {
			args.edgeStencil = args.edgeShape.getStencil();
		}
		
		var result;
		
		if(args.sourceStencil && args.targetStencil) {
			var source = this._cachedConnectSET[args.sourceStencil.id()];
			
			if(!source)
				result = this._cacheConnect(args);
			else {
				var edge = source[args.edgeStencil.id()];

				if(!edge)
					result = this._cacheConnect(args);
				else {	
					var target = edge[args.targetStencil.id()];

					if(target == undefined)
						result = this._cacheConnect(args);
					else
						result = target;
				}
			}
		} else if (args.sourceStencil) {	
			var source = this._cachedConnectSE[args.sourceStencil.id()];
			
			if(!source)
				result = this._cacheConnect(args);
			else {
				var edge = source[args.edgeStencil.id()];
					
				if(edge == undefined)
					result = this._cacheConnect(args);
				else
					result = edge;
			}
		} else { // args.targetStencil
			var target = this._cachedConnectTE[args.targetStencil.id()];
			
			if(!target)
				result = this._cacheConnect(args);
			else {
				var edge = target[args.edgeStencil.id()];
					
				if(edge == undefined)
					result = this._cacheConnect(args);
				else
					result = edge;
			}
		}	
			
		// check cardinality
		if (result) {
			if(args.sourceShape) {
				var source = this._cachedCardSE[args.sourceStencil.id()];
				
				if(!source) {
					this._cacheCard(args);
					source = this._cachedCardSE[args.sourceStencil.id()];
				}
				
				var max = source[args.edgeStencil.id()];
				
				if(max == undefined) {
					this._cacheCard(args);
				}
				
				max = source[args.edgeStencil.id()];
				
				if(max != -1) {
					result = args.sourceShape.getOutgoingShapes().all(function(cs) {
								if((cs.getStencil().id() === args.edgeStencil.id()) && 
								   ((args.edgeShape) ? cs !== args.edgeShape : true)) {
									max--;
									return (max > 0) ? true : false;
								} else {
									return true;
								}
							});
				}
			} 
			
			if (args.targetShape) {
				var target = this._cachedCardTE[args.targetStencil.id()];
				
				if(!target) {
					this._cacheCard(args);
					target = this._cachedCardTE[args.targetStencil.id()];
				}
				
				var max = target[args.edgeStencil.id()];
				
				if(max == undefined) {
					this._cacheCard(args);
				}
				
				max = target[args.edgeStencil.id()];
				
				if(max != -1) {
					result = args.targetShape.getIncomingShapes().all(function(cs){
								if ((cs.getStencil().id() === args.edgeStencil.id()) &&
								((args.edgeShape) ? cs !== args.edgeShape : true)) {
									max--;
									return (max > 0) ? true : false;
								}
								else {
									return true;
								}
							});
				}
			}
		}
		
		return result;
	},
	
	/**
	 * 
	 * @param {Object}
	 *            args edgeStencil: ORYX.Core.StencilSet.Stencil edgeShape:
	 *            ORYX.Core.Edge |undefined sourceStencil:
	 *            ORYX.Core.StencilSet.Stencil | undefined sourceShape:
	 *            ORYX.Core.Node |undefined targetStencil:
	 *            ORYX.Core.StencilSet.Stencil | undefined targetShape:
	 *            ORYX.Core.Node |undefined
	 * 
	 * At least source or target has to be specified!!!
	 * 
	 * @return {Boolean} Returns, if the edge can connect source and target.
	 */
	_canConnect: function(args) {
		// check arguments
		if(!args ||
		   (!args.sourceShape && !args.sourceStencil &&
		    !args.targetShape && !args.targetStencil) ||
		    !args.edgeShape && !args.edgeStencil) {
		   	return false; 
		}
		
		// init arguments
		if(args.sourceShape) {
			args.sourceStencil = args.sourceShape.getStencil();
		}
		if(args.targetShape) {
			args.targetStencil = args.targetShape.getStencil();
		}
		if(args.edgeShape) {
			args.edgeStencil = args.edgeShape.getStencil();
		}

		// 1. check connection rules
		var resultCR;
		
		// get all connection rules for this edge
		var edgeRules = this._getConnectionRulesOfEdgeStencil(args.edgeStencil);

		// check connection rules, if the source can be connected to the target
		// with the specified edge.
		if(edgeRules.keys().length === 0) {
			resultCR = false;
		} else {
			if(args.sourceStencil) {
				resultCR = args.sourceStencil.roles().any(function(sourceRole) {
					var targetRoles = edgeRules[sourceRole];

					if(!targetRoles) {return false;}
		
					if(args.targetStencil) {
						return (targetRoles.any(function(targetRole) {
							return args.targetStencil.roles().member(targetRole);
						}));
					} else {
						return true;
					}
				});
			} else { // !args.sourceStencil -> there is args.targetStencil
				resultCR = edgeRules.values().any(function(targetRoles) {
					return args.targetStencil.roles().any(function(targetRole) {
						return targetRoles.member(targetRole);
					});
				});
			}
		}
		
		return resultCR;
	},

	/** End connection rules' methods */


	/** Begin containment rules' methods */

	isContainer: function(shape) {
		return this._containerStencils.member(shape.getStencil().id());
	},

	/**
	 * 
	 * @param {Object}
	 *            args containingStencil: ORYX.Core.StencilSet.Stencil
	 *            containingShape: ORYX.Core.AbstractShape containedStencil:
	 *            ORYX.Core.StencilSet.Stencil containedShape: ORYX.Core.Shape
	 */
	canContain: function(args) {
		if(!args ||
		   !args.containingStencil && !args.containingShape ||
		   !args.containedStencil && !args.containedShape) {
		   	return false;
		}
		
		// init arguments
		if(args.containedShape) {
			args.containedStencil = args.containedShape.getStencil();
		}
		
		if(args.containingShape) {
			args.containingStencil = args.containingShape.getStencil();
		}
		
		//if(args.containingStencil.type() == 'edge' || args.containedStencil.type() == 'edge')
		//	return false;
		if(args.containedStencil.type() == 'edge') 
			return false;
		
		var childValues;
		
		var parent = this._cachedContainPC[args.containingStencil.id()];
		
		if(!parent)
			childValues = this._cacheContain(args);
		else {
			childValues = parent[args.containedStencil.id()];
			
			if(!childValues)
				childValues = this._cacheContain(args);
		}

		if(!childValues[0])
			return false;
		else if (childValues[1] == -1)
			return true;
		else {
			if(args.containingShape) {
				var max = childValues[1];
				return args.containingShape.getChildShapes(false).all(function(as) {
					if(as.getStencil().id() === args.containedStencil.id()) {
						max--;
						return (max > 0) ? true : false;
					} else {
						return true;
					}
				});
			} else {
				return true;
			}
		}
	},
	
	/**
	 * 
	 * @param {Object}
	 *            args containingStencil: ORYX.Core.StencilSet.Stencil
	 *            containingShape: ORYX.Core.AbstractShape containedStencil:
	 *            ORYX.Core.StencilSet.Stencil containedShape: ORYX.Core.Shape
	 */
	_canContain: function(args) {
		if(!args ||
		   !args.containingStencil && !args.containingShape ||
		   !args.containedStencil && !args.containedShape) {
		   	return false;
		}
		
		// init arguments
		if(args.containedShape) {
			args.containedStencil = args.containedShape.getStencil();
		}
		
		if(args.containingShape) {
			args.containingStencil = args.containingShape.getStencil();
		}
		
//		if(args.containingShape) {
//			if(args.containingShape instanceof ORYX.Core.Edge) {
//				// edges cannot contain other shapes
//				return false;
//			}
//		}

		
		var result;
		
		// check containment rules
		result = args.containingStencil.roles().any((function(role) {
			var roles = this._containmentRules[role];
			if(roles) {
				return roles.any(function(role) {
					return args.containedStencil.roles().member(role);
				});
			} else {
				return false;
			}
		}).bind(this));
		
		return result;
	},
	
	/** End containment rules' methods */
	
	
	/** Begin morphing rules' methods */
	
	/**
	 * 
	 * @param {Object}
	 *           args 
	 *            stencil: ORYX.Core.StencilSet.Stencil | undefined 
	 *            shape: ORYX.Core.Shape | undefined
	 * 
	 * At least stencil or shape has to be specified
	 * 
	 * @return {Array} Array of stencils that the passed stencil/shape can be
	 *         transformed to (including the current stencil itself)
	 */
	morphStencils: function(args) {
		// check arguments
		if(!args.stencil && !args.shape) {
			return [];
		}
		
		// init arguments
		if(args.shape) {
			args.stencil = args.shape.getStencil();
		}
		
		var _morphStencils = [];
		args.stencil.roles().each(function(role) {
			this._cacheMorph(role).each(function(stencil) {
				_morphStencils.push(stencil);
			})
		}.bind(this));


		var baseMorphs = this.baseMorphs();
		// BaseMorphs should be in the front of the array
		_morphStencils = _morphStencils.uniq().sort(function(a,b){ return baseMorphs.include(a)&&!baseMorphs.include(b) ? -1 : (baseMorphs.include(b)&&!baseMorphs.include(a) ? 1 : 0)})
		return _morphStencils;
	},
	
	/**
	 * @return {Array} An array of all base morph stencils
	 */
	baseMorphs: function() {
		var _baseMorphs = [];
		this._morphingRules.each(function(pair) {
			pair.value.each(function(baseMorph) {
				_baseMorphs.push(baseMorph);
			});
		});
		return _baseMorphs;
	},
	
	/**
	 * Returns true if there are morphing rules defines
	 * @return {boolean} 
	 */
	containsMorphingRules: function(){
		return this._stencilSets.any(function(ss){ return !!ss.jsonRules().morphingRules});
	},
	
	/**
	 * 
	 * @param {Object}
	 *            args 
	 *            sourceStencil:
	 *            ORYX.Core.StencilSet.Stencil | undefined 
	 *            sourceShape:
	 *            ORYX.Core.Node |undefined 
	 *            targetStencil:
	 *            ORYX.Core.StencilSet.Stencil | undefined 
	 *            targetShape:
	 *            ORYX.Core.Node |undefined
	 * 
	 * 
	 * @return {Stencil} Returns, the stencil for the connecting edge 
	 * or null if connection is not possible
	 */
	connectMorph: function(args) {	
		// check arguments
		if(!args ||
		   (!args.sourceShape && !args.sourceStencil &&
		    !args.targetShape && !args.targetStencil)) {
		   	return false; 
		}
		
		// init arguments
		if(args.sourceShape) {
			args.sourceStencil = args.sourceShape.getStencil();
		}
		if(args.targetShape) {
			args.targetStencil = args.targetShape.getStencil();
		}
		
		var incoming = this.incomingEdgeStencils(args);
		var outgoing = this.outgoingEdgeStencils(args);
		
		var edgeStencils = incoming.select(function(e) { return outgoing.member(e); }); // intersection of sets
		var baseEdgeStencils = this.baseMorphs().select(function(e) { return edgeStencils.member(e); }); // again: intersection of sets
		
		if(baseEdgeStencils.size()>0)
			return baseEdgeStencils[0]; // return any of the possible base morphs
		else if(edgeStencils.size()>0)
			return edgeStencils[0];	// return any of the possible stencils
		
		return null; //connection not possible
	},
	
	/**
	 * Return true if the stencil should be located in the shape menu
	 * @param {ORYX.Core.StencilSet.Stencil} morph
	 * @return {Boolean} Returns true if the morphs in the morph group of the
	 * specified morph shall be displayed in the shape menu
	 */
	showInShapeMenu: function(stencil) {
		return 	this._stencilSets.any(function(ss){
				    return ss.jsonRules().morphingRules
							.any(function(r){
								return 	stencil.roles().include(ss.namespace() + r.role) 
										&& r.showInShapeMenu !== false;
							})
				});
	},
	
	preserveBounds: function(stencil) {
		return this._stencilSets.any(function(ss) {
			return ss.jsonRules().morphingRules.any(function(r) {
				
				
				return stencil.roles().include(ss.namespace() + r.role) 
					&& r.preserveBounds;
			})
		})
	},
	
	/** End morphing rules' methods */


	/** Begin layouting rules' methods */
	
	/**
	 * Returns a set on "in" and "out" layouting rules for a given shape
	 * @param {Object} shape
	 * @param {Object} edgeShape (Optional)
	 * @return {Object} "in" and "out" with a default value of {"t":1, "r":1, "b":1, "r":1} if not specified in the json
	 */
	getLayoutingRules : function(shape, edgeShape){
		
		if (!shape||!(shape instanceof ORYX.Core.Shape)){ return }
		
		var layout = {"in":{},"out":{}};
		
		var parseValues = function(o, v){
			if (o && o[v]){
				["t","r","b","l"].each(function(d){
					layout[v][d]=Math.max(o[v][d],layout[v][d]||0);
				});
			}
			if (o && o[v+"s"] instanceof Array){
				["t","r","b","l"].each(function(d){
					var defaultRule = o[v+"s"].find(function(e){ return !e.edgeRole });
					var edgeRule;
					if (edgeShape instanceof ORYX.Core.Edge) {
						edgeRule = o[v + "s"].find(function(e){return this._hasRole(edgeShape, e.edgeRole) }.bind(this));
					}
					layout[v][d]=Math.max(edgeRule?edgeRule[d]:defaultRule[d],layout[v][d]||0);
				}.bind(this));
			}
		}.bind(this)
		
		// For each role
		shape.getStencil().roles().each(function(role) {
			// check if there are layout information
			if (this._layoutRules[role]){
				// if so, parse those information to the 'layout' variable
				parseValues(this._layoutRules[role], "in");
				parseValues(this._layoutRules[role], "out");
			}
		}.bind(this));
		
		// Make sure, that every attribute has an value,
		// otherwise set 1
		["in","out"].each(function(v){
			["t","r","b","l"].each(function(d){
					layout[v][d]=layout[v][d]!==undefined?layout[v][d]:1;
				});
		})
		
		return layout;
	},
	
	/** End layouting rules' methods */
	
	/** Helper methods */

	/**
	 * Checks wether a shape contains the given role or the role is equal the stencil id 
	 * @param {ORYX.Core.Shape} shape
	 * @param {String} role
	 */
	_hasRole: function(shape, role){
		if (!(shape instanceof ORYX.Core.Shape)||!role){ return }
		var isRole = shape.getStencil().roles().any(function(r){ return r == role});
		
		return isRole || shape.getStencil().id() == (shape.getStencil().namespace()+role);
	},

	/**
	 * 
	 * @param {String}
	 *            role
	 * 
	 * @return {Array} Returns an array of stencils that can act as role.
	 */
	_stencilsWithRole: function(role) {
		return this._stencils.findAll(function(stencil) {
			return (stencil.roles().member(role)) ? true : false;
		});
	},
	
	/**
	 * 
	 * @param {String}
	 *            role
	 * 
	 * @return {Array} Returns an array of stencils that can act as role and
	 *         have the type 'edge'.
	 */
	_edgesWithRole: function(role) {
		return this._stencils.findAll(function(stencil) {
			return (stencil.roles().member(role) && stencil.type() === "edge") ? true : false;
		});
	},
	
	/**
	 * 
	 * @param {String}
	 *            role
	 * 
	 * @return {Array} Returns an array of stencils that can act as role and
	 *         have the type 'node'.
	 */
	_nodesWithRole: function(role) {
		return this._stencils.findAll(function(stencil) {
			return (stencil.roles().member(role) && stencil.type() === "node") ? true : false;
		});
	},

	/**
	 * 
	 * @param {ORYX.Core.StencilSet.Stencil}
	 *            parent
	 * @param {ORYX.Core.StencilSet.Stencil}
	 *            child
	 * 
	 * @returns {Boolean} Returns the maximum occurrence of shapes of the
	 *          stencil's type inside the parent.
	 */
	_getMaximumOccurrence: function(parent, child) {
		var max;
		child.roles().each((function(role) {
			var cardRule = this._cardinalityRules[role];
			if(cardRule && cardRule.maximumOccurrence) {
				if(max) {
					max = Math.min(max, cardRule.maximumOccurrence);
				} else {
					max = cardRule.maximumOccurrence;
				}
			}
		}).bind(this));

		return max;
	},


	/**
	 * 
	 * @param {Object}
	 *            args sourceStencil: ORYX.Core.Node edgeStencil:
	 *            ORYX.Core.StencilSet.Stencil
	 * 
	 * @return {Boolean} Returns, the maximum number of outgoing edges of the
	 *         type specified by edgeStencil of the sourceShape.
	 */
	_getMaximumNumberOfOutgoingEdge: function(args) {
		if(!args ||
		   !args.sourceStencil ||
		   !args.edgeStencil) {
		   	return false;
		}
		
		var max;
		args.sourceStencil.roles().each((function(role) {
			var cardRule = this._cardinalityRules[role];

			if(cardRule && cardRule.outgoingEdges) {
				args.edgeStencil.roles().each(function(edgeRole) {
					var oe = cardRule.outgoingEdges[edgeRole];

					if(oe && oe.maximum) {
						if(max) {
							max = Math.min(max, oe.maximum);
						} else {
							max = oe.maximum;
						}
					}
				});
			}
		}).bind(this));

		return max;
	},
	
	/**
	 * 
	 * @param {Object}
	 *            args targetStencil: ORYX.Core.StencilSet.Stencil edgeStencil:
	 *            ORYX.Core.StencilSet.Stencil
	 * 
	 * @return {Boolean} Returns the maximum number of incoming edges of the
	 *         type specified by edgeStencil of the targetShape.
	 */
	_getMaximumNumberOfIncomingEdge: function(args) {
		if(!args ||
		   !args.targetStencil ||
		   !args.edgeStencil) {
		   	return false;
		}
		
		var max;
		args.targetStencil.roles().each((function(role) {
			var cardRule = this._cardinalityRules[role];
			if(cardRule && cardRule.incomingEdges) {
				args.edgeStencil.roles().each(function(edgeRole) {
					var ie = cardRule.incomingEdges[edgeRole];
					if(ie && ie.maximum) {
						if(max) {
							max = Math.min(max, ie.maximum);
						} else {
							max = ie.maximum;
						}
					}
				});
			}
		}).bind(this));

		return max;
	},
	
	/**
	 * 
	 * @param {ORYX.Core.StencilSet.Stencil}
	 *            edgeStencil
	 * 
	 * @return {Hash} Returns a hash map of all connection rules for
	 *         edgeStencil.
	 */
	_getConnectionRulesOfEdgeStencil: function(edgeStencil) {
		var edgeRules = new Hash();
		edgeStencil.roles().each((function(role) {
			if(this._connectionRules[role]) {
				this._connectionRules[role].each(function(cr) {
					if(edgeRules[cr.key]) {
						edgeRules[cr.key] = edgeRules[cr.key].concat(cr.value);
					} else {
						edgeRules[cr.key] = cr.value;
					}
				});
			}
		}).bind(this));
		
		return edgeRules;
	},
	
	_isRoleOfOtherNamespace: function(role) {
		return (role.indexOf("#") > 0);
	},

	toString: function() { return "Rules"; }
}
ORYX.Core.StencilSet.Rules = Clazz.extend(ORYX.Core.StencilSet.Rules);
/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

/**
 * Init namespace
 */
if (!ORYX) {
    var ORYX = {};
}
if (!ORYX.Core) {
    ORYX.Core = {};
}
if (!ORYX.Core.StencilSet) {
    ORYX.Core.StencilSet = {};
}

/**
 * This class represents a stencil set. It offers methods for accessing
 *  the attributes of the stencil set description JSON file and the stencil set's
 *  stencils.
 */
ORYX.Core.StencilSet.StencilSet = Clazz.extend({

    /**
     * Constructor
     * @param source {URL} A reference to the stencil set specification.
     *
     */
    construct: function(source, modelMetaData, editorId){
        arguments.callee.$.construct.apply(this, arguments);
        
        if (!source) {
            throw "ORYX.Core.StencilSet.StencilSet(construct): Parameter 'source' is not defined.";
        }
        
        if (source.endsWith("/")) {
            source = source.substr(0, source.length - 1);
        }
		
		this._extensions = new Hash();
        
        this._source = source;
        this._baseUrl = source.substring(0, source.lastIndexOf("/") + 1);
        
        this._jsonObject = {};
        
        this._stencils = new Hash();
		this._availableStencils = new Hash();
        
		if(ORYX.CONFIG.BACKEND_SWITCH) {
			this._baseUrl = "editor/stencilsets/bpmn2.0/";
			this._source = "stencilsets/bpmn2.0/bpmn2.0.json";
			
			new Ajax.Request(ACTIVITI.CONFIG.contextRoot + '/editor/stencilset?version=' + Date.now(), {
	            asynchronous: false,
	            method: 'get',
	            onSuccess: this._init.bind(this),
	            onFailure: this._cancelInit.bind(this)
	        });
			
		} else {
			new Ajax.Request(source, {
	            asynchronous: false,
	            method: 'get',
	            onSuccess: this._init.bind(this),
	            onFailure: this._cancelInit.bind(this)
	        });
		}
        
        if (this.errornous) 
            throw "Loading stencil set " + source + " failed.";
    },
    
    /**
     * Finds a root stencil in this stencil set. There may be many of these. If
     * there are, the first one found will be used. In Firefox, this is the
     * topmost definition in the stencil set description file.
     */
    findRootStencilName: function(){
    
        // find any stencil that may be root.
        var rootStencil = this._stencils.values().find(function(stencil){
            return stencil._jsonStencil.mayBeRoot
        });
        
		// if there is none, just guess the first.
		if (!rootStencil) {
			ORYX.Log.warn("Did not find any stencil that may be root. Taking a guess.");
			rootStencil = this._stencils.values()[0];
		}

        // return its id.
        return rootStencil.id();
    },
    
    /**
     * @param {ORYX.Core.StencilSet.StencilSet} stencilSet
     * @return {Boolean} True, if stencil set has the same namespace.
     */
    equals: function(stencilSet){
        return (this.namespace() === stencilSet.namespace());
    },
    
	/**
	 * 
	 * @param {Oryx.Core.StencilSet.Stencil} rootStencil If rootStencil is defined, it only returns stencils
	 * 			that could be (in)direct child of that stencil.
	 */
    stencils: function(rootStencil, rules, sortByGroup){
		if(rootStencil && rules) {
			var stencils = this._availableStencils.values();
			var containers = [rootStencil];
			var checkedContainers = [];
			
			var result = [];
			
			while (containers.size() > 0) {
				var container = containers.pop();
				checkedContainers.push(container);
				var children = stencils.findAll(function(stencil){
					var args = {
						containingStencil: container,
						containedStencil: stencil
					};
					return rules.canContain(args);
				});
				for(var i = 0; i < children.size(); i++) {
					if (!checkedContainers.member(children[i])) {
						containers.push(children[i]);
					}
				}
				result = result.concat(children).uniq();
			}
			
			// Sort the result to the origin order
			result = result.sortBy(function(stencil) {
				return stencils.indexOf(stencil);
			});
			
			
			if(sortByGroup) {
				result = result.sortBy(function(stencil) {
					return stencil.groups().first();
				});
			}
			
			var edges = stencils.findAll(function(stencil) {
				return stencil.type() == "edge";
			});
			result = result.concat(edges);
			
			return result;
			
		} else {
        	if(sortByGroup) {
				return this._availableStencils.values().sortBy(function(stencil) {
					return stencil.groups().first();
				});
			} else {
				return this._availableStencils.values();
			}
		}
    },
    
    nodes: function(){
        return this._availableStencils.values().findAll(function(stencil){
            return (stencil.type() === 'node')
        });
    },
    
    edges: function(){
        return this._availableStencils.values().findAll(function(stencil){
            return (stencil.type() === 'edge')
        });
    },
    
    stencil: function(id){
        return this._stencils[id];
    },
    
    title: function(){
        return ORYX.Core.StencilSet.getTranslation(this._jsonObject, "title");
    },
    
    description: function(){
        return ORYX.Core.StencilSet.getTranslation(this._jsonObject, "description");
    },
    
    namespace: function(){
        return this._jsonObject ? this._jsonObject.namespace : null;
    },
    
    jsonRules: function(){
        return this._jsonObject ? this._jsonObject.rules : null;
    },
    
    source: function(){
        return this._source;
    },
	
	extensions: function() {
		return this._extensions;
	},
	
	addExtension: function(url) {
		
		new Ajax.Request(url, {
            method: 'GET',
            asynchronous: false,
			onSuccess: (function(transport) {
				this.addExtensionDirectly(transport.responseText);
			}).bind(this),
			onFailure: (function(transport) {
				ORYX.Log.debug("Loading stencil set extension file failed. The request returned an error." + transport);
			}).bind(this),
			onException: (function(transport) {
				ORYX.Log.debug("Loading stencil set extension file failed. The request returned an error." + transport);
			}).bind(this)
		
		});
	},
	
	addExtensionDirectly: function(str){

		try {
			eval("var jsonExtension = " + str);

			if(!(jsonExtension["extends"].endsWith("#")))
					jsonExtension["extends"] += "#";
					
			if(jsonExtension["extends"] == this.namespace()) {
				this._extensions[jsonExtension.namespace] = jsonExtension;
				
				var defaultPosition = this._stencils.keys().size();
				//load new stencils
				if(jsonExtension.stencils) {
					$A(jsonExtension.stencils).each(function(stencil) {
						defaultPosition++;
						var oStencil = new ORYX.Core.StencilSet.Stencil(stencil, this.namespace(), this._baseUrl, this, undefined, defaultPosition);            
						this._stencils[oStencil.id()] = oStencil;
						this._availableStencils[oStencil.id()] = oStencil;
					}.bind(this));
				}
				
				//load additional properties
				if (jsonExtension.properties) {
					var stencils = this._stencils.values();
					
					stencils.each(function(stencil){
						var roles = stencil.roles();
						
						jsonExtension.properties.each(function(prop){
							prop.roles.any(function(role){
								role = jsonExtension["extends"] + role;
								if (roles.member(role)) {
									prop.properties.each(function(property){
										stencil.addProperty(property, jsonExtension.namespace);
									});
									
									return true;
								}
								else 
									return false;
							})
						})
					}.bind(this));
				}
				
				//remove stencil properties
				if(jsonExtension.removeproperties) {
					jsonExtension.removeproperties.each(function(remprop) {
						var stencil = this.stencil(jsonExtension["extends"] + remprop.stencil);
						if(stencil) {
							remprop.properties.each(function(propId) {
								stencil.removeProperty(propId);
							});
						}
					}.bind(this));
				}
				
				//remove stencils
				if(jsonExtension.removestencils) {
					$A(jsonExtension.removestencils).each(function(remstencil) {
						delete this._availableStencils[jsonExtension["extends"] + remstencil];
					}.bind(this));
				}
			}
		} catch (e) {
			ORYX.Log.debug("StencilSet.addExtension: Something went wrong when initialising the stencil set extension. " + e);
		}	
	},
	
	removeExtension: function(namespace) {
		var jsonExtension = this._extensions[namespace];
		if(jsonExtension) {
			
			//unload extension's stencils
			if(jsonExtension.stencils) {
				$A(jsonExtension.stencils).each(function(stencil) {
					var oStencil = new ORYX.Core.StencilSet.Stencil(stencil, this.namespace(), this._baseUrl, this);            
					delete this._stencils[oStencil.id()]; // maybe not ??
					delete this._availableStencils[oStencil.id()];
				}.bind(this));
			}
			
			//unload extension's properties
			if (jsonExtension.properties) {
				var stencils = this._stencils.values();
				
				stencils.each(function(stencil){
					var roles = stencil.roles();
					
					jsonExtension.properties.each(function(prop){
						prop.roles.any(function(role){
							role = jsonExtension["extends"] + role;
							if (roles.member(role)) {
								prop.properties.each(function(property){
									stencil.removeProperty(property.id);
								});
								
								return true;
							}
							else 
								return false;
						})
					})
				}.bind(this));
			}
			
			//restore removed stencil properties
			if(jsonExtension.removeproperties) {
				jsonExtension.removeproperties.each(function(remprop) {
					var stencil = this.stencil(jsonExtension["extends"] + remprop.stencil);
					if(stencil) {
						var stencilJson = $A(this._jsonObject.stencils).find(function(s) { return s.id == stencil.id() });
						remprop.properties.each(function(propId) {
							var propertyJson = $A(stencilJson.properties).find(function(p) { return p.id == propId });
							stencil.addProperty(propertyJson, this.namespace());
						}.bind(this));
					}
				}.bind(this));
			}
			
			//restore removed stencils
			if(jsonExtension.removestencils) {
				$A(jsonExtension.removestencils).each(function(remstencil) {
					var sId = jsonExtension["extends"] + remstencil;
					this._availableStencils[sId] = this._stencils[sId];
				}.bind(this));
			}
		}
		delete this._extensions[namespace];
	},
    
    __handleStencilset: function(response){
    
        try {
            // using eval instead of prototype's parsing,
            // since there are functions in this JSON.
            eval("this._jsonObject =" + response.responseText);
        } 
        catch (e) {
            throw "Stenciset corrupt: " + e;
        }
        
        // assert it was parsed.
        if (!this._jsonObject) {
            throw "Error evaluating stencilset. It may be corrupt.";
        }
        
        with (this._jsonObject) {
        
            // assert there is a namespace.
            if (!namespace || namespace === "") 
                throw "Namespace definition missing in stencilset.";
            
            if (!(stencils instanceof Array)) 
                throw "Stencilset corrupt.";
            
            // assert namespace ends with '#'.
            if (!namespace.endsWith("#")) 
                namespace = namespace + "#";
            
            // assert title and description are strings.
            if (!title) 
                title = "";
            if (!description) 
                description = "";
        }
    },
    
    /**
     * This method is called when the HTTP request to get the requested stencil
     * set succeeds. The response is supposed to be a JSON representation
     * according to the stencil set specification.
     * @param {Object} response The JSON representation according to the
     * 			stencil set specification.
     */
    _init: function(response){
    
        // init and check consistency.
        this.__handleStencilset(response);
		
		var pps = new Hash();
		
		// init property packages
		if(this._jsonObject.propertyPackages) {
			$A(this._jsonObject.propertyPackages).each((function(pp) {
				pps[pp.name] = pp.properties;
			}).bind(this));
		}
		
		var defaultPosition = 0;
		
        // init each stencil
        $A(this._jsonObject.stencils).each((function(stencil){
        	defaultPosition++;
        	
            // instantiate normally.
            var oStencil = new ORYX.Core.StencilSet.Stencil(stencil, this.namespace(), this._baseUrl, this, pps, defaultPosition);      
			this._stencils[oStencil.id()] = oStencil;
			this._availableStencils[oStencil.id()] = oStencil;
            
        }).bind(this));
    },
    
    _cancelInit: function(response){
        this.errornous = true;
    },
    
    toString: function(){
        return "StencilSet " + this.title() + " (" + this.namespace() + ")";
    }
});
/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

/**
 * Init namespace
 */
if(!ORYX) {var ORYX = {};}
if(!ORYX.Core) {ORYX.Core = {};}
if(!ORYX.Core.StencilSet) {ORYX.Core.StencilSet = {};}

/**
 * Class StencilSets
 * uses Prototpye 1.5.0
 * uses Inheritance
 *
 * Singleton
 */
//storage for loaded stencil sets by namespace
ORYX.Core.StencilSet._stencilSetsByNamespace = new Hash();

//storage for stencil sets by url
ORYX.Core.StencilSet._stencilSetsByUrl = new Hash();	

//storage for stencil set namespaces by editor instances
ORYX.Core.StencilSet._StencilSetNSByEditorInstance = new Hash();

//storage for rules by editor instances
ORYX.Core.StencilSet._rulesByEditorInstance = new Hash();

/**
 * 
 * @param {String} editorId
 * 
 * @return {Hash} Returns a hash map with all stencil sets that are loaded by
 * 					the editor with the editorId.
 */
ORYX.Core.StencilSet.stencilSets = function(editorId) {
	var stencilSetNSs = ORYX.Core.StencilSet._StencilSetNSByEditorInstance[editorId];
	var stencilSets = new Hash();
	if(stencilSetNSs) {
		stencilSetNSs.each(function(stencilSetNS) {
			var stencilSet = ORYX.Core.StencilSet.stencilSet(stencilSetNS)
			stencilSets[stencilSet.namespace()] = stencilSet;
		});
	}
	return stencilSets;
};

/**
 * 
 * @param {String} namespace
 * 
 * @return {ORYX.Core.StencilSet.StencilSet} Returns the stencil set with the specified
 * 										namespace.
 * 
 * The method can handle namespace strings like
 *  http://www.example.org/stencilset
 *  http://www.example.org/stencilset#
 *  http://www.example.org/stencilset#ANode
 */
ORYX.Core.StencilSet.stencilSet = function(namespace) {
	ORYX.Log.trace("Getting stencil set %0", namespace);
	var splitted = namespace.split("#", 1);
	if(splitted.length === 1) {
		ORYX.Log.trace("Getting stencil set %0", splitted[0]);
		return ORYX.Core.StencilSet._stencilSetsByNamespace[splitted[0] + "#"];
	} else {
		return undefined;
	}
};

/**
 * 
 * @param {String} id
 * 
 * @return {ORYX.Core.StencilSet.Stencil} Returns the stencil specified by the id.
 * 
 * The id must be unique and contains the namespace of the stencil's stencil set.
 * e.g. http://www.example.org/stencilset#ANode
 */
ORYX.Core.StencilSet.stencil = function(id) {
	ORYX.Log.trace("Getting stencil for %0", id);
	var ss = ORYX.Core.StencilSet.stencilSet(id);
	if(ss) {
		return ss.stencil(id);
	} else {

		ORYX.Log.trace("Cannot fild stencil for %0", id);
		return undefined;
	}
};

/**
 * 
 * @param {String} editorId
 * 
 * @return {ORYX.Core.StencilSet.Rules} Returns the rules object for the editor
 * 									specified by its editor id.
 */
ORYX.Core.StencilSet.rules = function(editorId) {
	if(!ORYX.Core.StencilSet._rulesByEditorInstance[editorId]) {
		ORYX.Core.StencilSet._rulesByEditorInstance[editorId] = new ORYX.Core.StencilSet.Rules();
	}
	return ORYX.Core.StencilSet._rulesByEditorInstance[editorId];
};

/**
 * 
 * @param {String} url
 * @param {String} editorId
 * 
 * Loads a stencil set from url, if it is not already loaded.
 * It also stores which editor instance loads the stencil set and 
 * initializes the Rules object for the editor instance.
 */
ORYX.Core.StencilSet.loadStencilSet = function(url, modelMetaData, editorId) {
	
	// Alfresco: disable cache, because stencil sets are now flexible
	
	//var stencilSet = ORYX.Core.StencilSet._stencilSetsByUrl[url];

	//if(!stencilSet) {
		//load stencil set
		stencilSet = new ORYX.Core.StencilSet.StencilSet(url, modelMetaData, editorId);
		
		//store stencil set
		ORYX.Core.StencilSet._stencilSetsByNamespace[stencilSet.namespace()] = stencilSet;
		
		//store stencil set by url
		ORYX.Core.StencilSet._stencilSetsByUrl[url] = stencilSet;
	//}
	
	var namespace = stencilSet.namespace();
	
	//store which editorInstance loads the stencil set
	if(ORYX.Core.StencilSet._StencilSetNSByEditorInstance[editorId]) {
		ORYX.Core.StencilSet._StencilSetNSByEditorInstance[editorId].push(namespace);
	} else {
		ORYX.Core.StencilSet._StencilSetNSByEditorInstance[editorId] = [namespace];
	}

	//store the rules for the editor instance
	if(ORYX.Core.StencilSet._rulesByEditorInstance[editorId]) {
		ORYX.Core.StencilSet._rulesByEditorInstance[editorId].initializeRules(stencilSet);
	} else {
		var rules = new ORYX.Core.StencilSet.Rules();
		rules.initializeRules(stencilSet);
		ORYX.Core.StencilSet._rulesByEditorInstance[editorId] = rules;
	}
};

/**
 * Returns the translation of an attribute in jsonObject specified by its name
 * according to navigator.language
 */
ORYX.Core.StencilSet.getTranslation = function(jsonObject, name) {
	var lang = ORYX.I18N.Language.toLowerCase();
	
	var result = jsonObject[name + "_" + lang];
	
	if(result)
		return result;
		
	result = jsonObject[name + "_" + lang.substr(0, 2)];
	
	if(result)
		return result;
		
	return jsonObject[name];
};
/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

/**
 * Init namespaces
 */
if(!ORYX) {var ORYX = {};}
if(!ORYX.Core) {ORYX.Core = {};}


/**
 * @classDescription With Bounds you can set and get position and size of UIObjects.
 */
ORYX.Core.Command = Clazz.extend({

	/**
	 * Constructor
	 */
	construct: function() {

	},
	
	execute: function(){
		throw "Command.execute() has to be implemented!"
	},
	
	rollback: function(){
		throw "Command.rollback() has to be implemented!"
	}
	
	
 });/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

/**
 * Init namespaces
 */
if(!ORYX) {var ORYX = {};}
if(!ORYX.Core) {ORYX.Core = {};}


/**
 * @classDescription With Bounds you can set and get position and size of UIObjects.
 */
ORYX.Core.Bounds = {

	/**
	 * Constructor
	 */
	construct: function() {
		this._changedCallbacks = []; //register a callback with changedCallacks.push(this.method.bind(this));
		this.a = {};
		this.b = {};
		this.set.apply(this, arguments);
		this.suspendChange = false;
		this.changedWhileSuspend = false;
	},
	
	/**
	 * Calls all registered callbacks.
	 */
	_changed: function(sizeChanged) {
		if(!this.suspendChange) {
			this._changedCallbacks.each(function(callback) {
				callback(this, sizeChanged);
			}.bind(this));
			this.changedWhileSuspend = false;
		} else
			this.changedWhileSuspend = true;
	},
	
	/**
	 * Registers a callback that is called, if the bounds changes.
	 * @param callback {Function} The callback function.
	 */
	registerCallback: function(callback) {
		if(!this._changedCallbacks.member(callback)) {
			this._changedCallbacks.push(callback);	
		}
	},
	
	/**
	 * Unregisters a callback.
	 * @param callback {Function} The callback function.
	 */
	unregisterCallback: function(callback) {
			this._changedCallbacks = this._changedCallbacks.without(callback);
	},
	
	/**
	 * Sets position and size of the shape dependent of four coordinates
	 * (set(ax, ay, bx, by);), two points (set({x: ax, y: ay}, {x: bx, y: by});)
	 * or one bound (set({a: {x: ax, y: ay}, b: {x: bx, y: by}});).
	 */
	set: function() {
		
		var changed = false;
		
		switch (arguments.length) {
		
			case 1:
				if(this.a.x !== arguments[0].a.x) {
					changed = true;
					this.a.x = arguments[0].a.x;
				}
				if(this.a.y !== arguments[0].a.y) {
					changed = true;
					this.a.y = arguments[0].a.y;
				}
				if(this.b.x !== arguments[0].b.x) {
					changed = true;
					this.b.x = arguments[0].b.x;
				}
				if(this.b.y !== arguments[0].b.y) {
					changed = true;
					this.b.y = arguments[0].b.y;
				}
				break;
			
			case 2:
				var ax = Math.min(arguments[0].x, arguments[1].x);
				var ay = Math.min(arguments[0].y, arguments[1].y);
				var bx = Math.max(arguments[0].x, arguments[1].x);
				var by = Math.max(arguments[0].y, arguments[1].y);
				if(this.a.x !== ax) {
					changed = true;
					this.a.x = ax;
				}
				if(this.a.y !== ay) {
					changed = true;
					this.a.y = ay;
				}
				if(this.b.x !== bx) {
					changed = true;
					this.b.x = bx;
				}
				if(this.b.y !== by) {
					changed = true;
					this.b.y = by;
				}
				break;
			
			case 4:
				var ax = Math.min(arguments[0], arguments[2]);
				var ay = Math.min(arguments[1], arguments[3]);
				var bx = Math.max(arguments[0], arguments[2]);
				var by = Math.max(arguments[1], arguments[3]);
				if(this.a.x !== ax) {
					changed = true;
					this.a.x = ax;
				}
				if(this.a.y !== ay) {
					changed = true;
					this.a.y = ay;
				}
				if(this.b.x !== bx) {
					changed = true;
					this.b.x = bx;
				}
				if(this.b.y !== by) {
					changed = true;
					this.b.y = by;
				}
				break;
		}
		
		if(changed) {
			this._changed(true);
		}
	},
	
	/**
	 * Moves the bounds so that the point p will be the new upper left corner.
	 * @param {Point} p
	 * or
	 * @param {Number} x
	 * @param {Number} y
	 */
	moveTo: function() {
		
		var currentPosition = this.upperLeft();
		switch (arguments.length) {
			case 1:
				this.moveBy({
					x: arguments[0].x - currentPosition.x,
					y: arguments[0].y - currentPosition.y
				});
				break;
			case 2:
				this.moveBy({
					x: arguments[0] - currentPosition.x,
					y: arguments[1] - currentPosition.y
				});
				break;
			default:
				//TODO error
		}
		
	},
	
	/**
	 * Moves the bounds relatively by p.
	 * @param {Point} p
	 * or
	 * @param {Number} x
	 * @param {Number} y
	 * 
	 */
	moveBy: function() {
		var changed = false;
		
		switch (arguments.length) {
			case 1:
				var p = arguments[0];
				if(p.x !== 0 || p.y !== 0) {
					changed = true;
					this.a.x += p.x;
					this.b.x += p.x;
					this.a.y += p.y;
					this.b.y += p.y;
				}
				break;	
			case 2:
				var x = arguments[0];
				var y = arguments[1];
				if(x !== 0 || y !== 0) {
					changed = true;
					this.a.x += x;
					this.b.x += x;
					this.a.y += y;
					this.b.y += y;
				}
				break;	
			default:
				//TODO error
		}
		
		if(changed) {
			this._changed();
		}
	},
	
	/***
	 * Includes the bounds b into the current bounds.
	 * @param {Bounds} b
	 */
	include: function(b) {
		
		if( (this.a.x === undefined) && (this.a.y === undefined) &&
			(this.b.x === undefined) && (this.b.y === undefined)) {
			return b;
		};
		
		var cx = Math.min(this.a.x,b.a.x);
		var cy = Math.min(this.a.y,b.a.y);
		
		var dx = Math.max(this.b.x,b.b.x);
		var dy = Math.max(this.b.y,b.b.y);

		
		this.set(cx, cy, dx, dy);
	},
	
	/**
	 * Relatively extends the bounds by p.
	 * @param {Point} p
	 */
	extend: function(p) {
		
		if(p.x !== 0 || p.y !== 0) {
			// this is over cross for the case that a and b have same coordinates.
			//((this.a.x > this.b.x) ? this.a : this.b).x += p.x;
			//((this.b.y > this.a.y) ? this.b : this.a).y += p.y;
			this.b.x += p.x;
			this.b.y += p.y;
			
			this._changed(true);
		}
	},
	
	/**
	 * Widens the scope of the bounds by x.
	 * @param {Number} x
	 */
	widen: function(x) {
		if (x !== 0) {
			this.suspendChange = true;
			this.moveBy({x: -x, y: -x});
			this.extend({x: 2*x, y: 2*x});
			this.suspendChange = false;
			if(this.changedWhileSuspend) {
				this._changed(true);
			}
		}
	},
	
	/**
	 * Returns the upper left corner's point regardless of the
	 * bound delimiter points.
	 */
	upperLeft: function() {
		var result = {};
		result.x = this.a.x;
		result.y = this.a.y;
		return result;
	},
	
	/**
	 * Returns the lower Right left corner's point regardless of the
	 * bound delimiter points.
	 */
	lowerRight: function() {
		var result = {};
		result.x = this.b.x;
		result.y = this.b.y;
		return result;
	},
	
	/**
	 * @return {Number} Width of bounds.
	 */
	width: function() {
		return this.b.x - this.a.x;
	},
	
	/**
	 * @return {Number} Height of bounds.
	 */
	height: function() {
		return this.b.y - this.a.y;
	},
	
	/**
	 * @return {Point} The center point of this bounds.
	 */
	center: function() {
		var center = {};
		center.x =(this.a.x + this.b.x)/2.0; 
		center.y =(this.a.y + this.b.y)/2.0;
		return center;
	},

	
	/**
	 * @return {Point} The center point of this bounds relative to upperLeft.
	 */
	midPoint: function() {
		
		var midpoint = {};
		midpoint.x = (this.b.x - this.a.x)/2.0; 
		midpoint.y = (this.b.y - this.a.y)/2.0;
		return midpoint;
	},
		
	/**
	 * Moves the center point of this bounds to the new position.
	 * @param p {Point} 
	 * or
	 * @param x {Number}
	 * @param y {Number}
	 */
	centerMoveTo: function() {
		var currentPosition = this.center();
		
		switch (arguments.length) {
			
			case 1:
				this.moveBy(arguments[0].x - currentPosition.x,
							arguments[0].y - currentPosition.y);
				break;
			
			case 2:
				this.moveBy(arguments[0] - currentPosition.x,
							arguments[1] - currentPosition.y);
				break;
		}
	},
	
	isIncluded: function(point, offset) {
		
		var pointX, pointY, offset;

		// Get the the two Points	
		switch(arguments.length) {
			case 1:
				pointX = arguments[0].x;
				pointY = arguments[0].y;
				offset = 0;
				
				break;
			case 2:
				if(arguments[0].x && arguments[0].y) {
					pointX = arguments[0].x;
					pointY = arguments[0].y;
					offset = Math.abs(arguments[1]);
				} else {
					pointX = arguments[0];
					pointY = arguments[1];
					offset = 0;
				}
				break;
			case 3:
				pointX = arguments[0];
				pointY = arguments[1];
				offset = Math.abs(arguments[2]);
				break;
			default:
				throw "isIncluded needs one, two or three arguments";
		}
				
		var ul = this.upperLeft();
		var lr = this.lowerRight();
		
		if(pointX >= ul.x - offset 
			&& pointX <= lr.x + offset && pointY >= ul.y - offset 
			&& pointY <= lr.y + offset)
			return true;
		else 
			return false;
	},
	
	/**
	 * @return {Bounds} A copy of this bounds.
	 */
	clone: function() {
		
		//Returns a new bounds object without the callback
		// references of the original bounds
		return new ORYX.Core.Bounds(this);
	},
	
	toString: function() {
		
		return "( "+this.a.x+" | "+this.a.y+" )/( "+this.b.x+" | "+this.b.y+" )";
	},
	
	serializeForERDF: function() {

		return this.a.x+","+this.a.y+","+this.b.x+","+this.b.y;
	}
 };
 
ORYX.Core.Bounds = Clazz.extend(ORYX.Core.Bounds);/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

/**
 * Init namespaces
 */
if(!ORYX) {var ORYX = {};}
if(!ORYX.Core) {ORYX.Core = {};}


/**
 * @classDescription Abstract base class for all objects that have a graphical representation
 * within the editor.
 * @extends Clazz
 */
ORYX.Core.UIObject = {
	/**
	 * Constructor of the UIObject class.
	 */
	construct: function(options) {	
		
		this.isChanged = true;			//Flag, if UIObject has been changed since last update.
		this.isResized = true;
		this.isVisible = true;			//Flag, if UIObject's display attribute is set to 'inherit' or 'none'
		this.isSelectable = false;		//Flag, if UIObject is selectable.
		this.isResizable = false;		//Flag, if UIObject is resizable.
		this.isMovable = false;			//Flag, if UIObject is movable.
		
		this.id = ORYX.Editor.provideId();	//get unique id
		this.parent = undefined;		//parent is defined, if this object is added to another uiObject.
		this.node = undefined;			//this is a reference to the SVG representation, either locally or in DOM.
		this.children = [];				//array for all add uiObjects
		
		this.bounds = new ORYX.Core.Bounds();		//bounds with undefined values

		this._changedCallback = this._changed.bind(this);	//callback reference for calling _changed
		this.bounds.registerCallback(this._changedCallback);	//set callback in bounds
		
		if(options && options.eventHandlerCallback) {
			this.eventHandlerCallback = options.eventHandlerCallback;
		}
	},
	
	/**
	 * Sets isChanged flag to true. Callback for the bounds object.
	 */
	_changed: function(bounds, isResized) {
		this.isChanged = true;
		if(this.bounds == bounds)
			this.isResized = isResized || this.isResized;
	},
	
	/**
	 * If something changed, this method calls the refresh method that must be implemented by subclasses.
	 */
	update: function() {
		if(this.isChanged) {
			this.refresh();
			this.isChanged = false;
			
			//call update of all children
			this.children.each(function(value) {
				value.update();
			});
		}
	},
	
	/**
	 * Is called in update method, if isChanged is set to true. Sub classes should call the super class method.
	 */
	refresh: function() {
		
	},
	
	/**
	 * @return {Array} Array of all child UIObjects.
	 */
	getChildren: function() {
		return this.children.clone();
	},
	
	/**
	 * @return {Array} Array of all parent UIObjects.
	 */
	getParents: function(){
		var parents = [];
		var parent = this.parent;
		while(parent){
			parents.push(parent);
			parent = parent.parent;
		}
		return parents;
	},
	
	/**
	 * Returns TRUE if the given parent is one of the UIObjects parents or the UIObject themselves, otherwise FALSE.
	 * @param {UIObject} parent
	 * @return {Boolean} 
	 */
	isParent: function(parent){
		var cparent = this;
		while(cparent){
			if (cparent === parent){
				return true;
			}
			cparent = cparent.parent;
		}
		return false;
	},
	
	/**
	 * @return {String} Id of this UIObject
	 */
	getId: function() {
		return this.id;
	},
	
	/**
	 * Method for accessing child uiObjects by id.
	 * @param {String} id
	 * @param {Boolean} deep
	 * 
	 * @return {UIObject} If found, it returns the UIObject with id.
	 */
	getChildById: function(id, deep) {
		return this.children.find(function(uiObj) {
			if(uiObj.getId() === id) {
				return uiObj;
			} else {
				if(deep) {
					var obj = uiObj.getChildById(id, deep);
					if(obj) {
						return obj;
					}
				}
			}
		});
	},
	
	/**
	 * Adds an UIObject to this UIObject and sets the parent of the
	 * added UIObject. It is also added to the SVG representation of this
	 * UIObject.
	 * @param {UIObject} uiObject
	 */
	add: function(uiObject) {
		//add uiObject, if it is not already a child of this object
		if (!(this.children.member(uiObject))) {
			//if uiObject is child of another parent, remove it from that parent.
			if(uiObject.parent) {
				uiObject.remove(uiObject);
			}
			
			//add uiObject to children
			this.children.push(uiObject);
			
			//set parent reference
			uiObject.parent = this;
			
			//add uiObject.node to this.node
			uiObject.node = this.node.appendChild(uiObject.node);
			
			//register callback to get informed, if child is changed
			uiObject.bounds.registerCallback(this._changedCallback);

			//uiObject.update();
		} else {
			ORYX.Log.info("add: ORYX.Core.UIObject is already a child of this object.");
		}
	},
	
	/**
	 * Removes UIObject from this UIObject. The SVG representation will also
	 * be removed from this UIObject's SVG representation.
	 * @param {UIObject} uiObject
	 */
	remove: function(uiObject) {
		//if uiObject is a child of this object, remove it.
		if (this.children.member(uiObject)) {
			//remove uiObject from children
			this.children = this._uiObjects.without(uiObject);
			
			//delete parent reference of uiObject
			uiObject.parent = undefined;
			
			//delete uiObject.node from this.node
			uiObject.node = this.node.removeChild(uiObject.node);
			
			//unregister callback to get informed, if child is changed
			uiObject.bounds.unregisterCallback(this._changedCallback);
		} else {
			ORYX.Log.info("remove: ORYX.Core.UIObject is not a child of this object.");
		}
		
	},
	
	/**
	 * Calculates absolute bounds of this UIObject.
	 */
	absoluteBounds: function() {
		if(this.parent) {
			var absUL = this.absoluteXY();
			return new ORYX.Core.Bounds(absUL.x, absUL.y,
							absUL.x + this.bounds.width(),
							absUL.y + this.bounds.height());
		} else {
			return this.bounds.clone();
		}
	},

	/**
	 * @return {Point} The absolute position of this UIObject.
	 */
	absoluteXY: function() {
		if(this.parent) {
			var pXY = this.parent.absoluteXY();
			var result = {};
			result.x = pXY.x + this.bounds.upperLeft().x;
			result.y = pXY.y + this.bounds.upperLeft().y;
			return result;
		} else {
			var result = {};
			result.x = this.bounds.upperLeft().x;
			result.y = this.bounds.upperLeft().y;
			return result;
		}
	},

	/**
	 * @return {Point} The absolute position from the Center of this UIObject.
	 */
	absoluteCenterXY: function() {
		if(this.parent) {
			var pXY = this.parent.absoluteXY();	
			var result = {};
			result.x = pXY.x + this.bounds.center().x;
			result.y = pXY.y + this.bounds.center().y;
			return result;
			
		} else {
			var result = {};
			result.x = this.bounds.center().x;
			result.y = this.bounds.center().y;
			return result;
		}
	},
	
	/**
	 * Hides this UIObject and all its children.
	 */
	hide: function() {
		this.node.setAttributeNS(null, 'display', 'none');
		this.isVisible = false;
		this.children.each(function(uiObj) {
			uiObj.hide();	
		});
	},
	
	/**
	 * Enables visibility of this UIObject and all its children.
	 */
	show: function() {
		this.node.setAttributeNS(null, 'display', 'inherit');
		this.isVisible = true;
		this.children.each(function(uiObj) {
			uiObj.show();	
		});		
	},
	
	addEventHandlers: function(node) {
		
		node.addEventListener(ORYX.CONFIG.EVENT_MOUSEDOWN, this._delegateEvent.bind(this), false);
		node.addEventListener(ORYX.CONFIG.EVENT_MOUSEMOVE, this._delegateEvent.bind(this), false);	
		node.addEventListener(ORYX.CONFIG.EVENT_MOUSEUP, this._delegateEvent.bind(this), false);
		node.addEventListener(ORYX.CONFIG.EVENT_MOUSEOVER, this._delegateEvent.bind(this), false);
		node.addEventListener(ORYX.CONFIG.EVENT_MOUSEOUT, this._delegateEvent.bind(this), false);
		node.addEventListener('click', this._delegateEvent.bind(this), false);
		node.addEventListener(ORYX.CONFIG.EVENT_DBLCLICK, this._delegateEvent.bind(this), false);
			
	},
		
	_delegateEvent: function(event) {
		if(this.eventHandlerCallback) {
			this.eventHandlerCallback(event, this);
		}
	},
	
	toString: function() { return "UIObject " + this.id }
 };
 ORYX.Core.UIObject = Clazz.extend(ORYX.Core.UIObject);/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

/**
 * Init namespaces
 */
if(!ORYX) {var ORYX = {};}
if(!ORYX.Core) {ORYX.Core = {};}

/**
 * Top Level uiobject.
 * @class ORYX.Core.AbstractShape
 * @extends ORYX.Core.UIObject
 */
ORYX.Core.AbstractShape = ORYX.Core.UIObject.extend(
/** @lends ORYX.Core.AbstractShape.prototype */
{

	/**
	 * Constructor
	 */
	construct: function(options, stencil, facade) {
		
		arguments.callee.$.construct.apply(this, arguments);
		
		this.resourceId = ORYX.Editor.provideId(); //Id of resource in DOM
		
		// stencil reference
		this._stencil = stencil;
		// if the stencil defines a super stencil that should be used for its instances, set it.
		if (this._stencil._jsonStencil.superId){
			stencilId = this._stencil.id()
			superStencilId = stencilId.substring(0, stencilId.indexOf("#") + 1) + stencil._jsonStencil.superId;
			stencilSet =  this._stencil.stencilSet();
			this._stencil = stencilSet.stencil(superStencilId);
		}
		
		//Hash map for all properties. Only stores the values of the properties.
		this.properties = new Hash();
		this.propertiesChanged = new Hash();

		// List of properties which are not included in the stencilset, 
		// but which gets (de)serialized
		this.hiddenProperties = new Hash();
		
		
		//Initialization of property map and initial value.
		this._stencil.properties().each((function(property) {
			var key = property.prefix() + "-" + property.id();
			this.properties[key] = property.value();
			this.propertiesChanged[key] = true;
		}).bind(this));
		
		// if super stencil was defined, also regard stencil's properties:
		if (stencil._jsonStencil.superId) {
			stencil.properties().each((function(property) {
				var key = property.prefix() + "-" + property.id();
				var value = property.value();
				var oldValue = this.properties[key];
				this.properties[key] = value;
				this.propertiesChanged[key] = true;

				// Raise an event, to show that the property has changed
				// required for plugins like processLink.js
				//window.setTimeout( function(){

					this._delegateEvent({
							type	: ORYX.CONFIG.EVENT_PROPERTY_CHANGED, 
							name	: key, 
							value	: value,
							oldValue: oldValue
						});

				//}.bind(this), 10)

			}).bind(this));
		}

	},

	layout: function() {

	},
	
	/**
	 * Returns the stencil object specifiing the type of the shape.
	 */
	getStencil: function() {
		return this._stencil;
	},
	
	/**
	 * 
	 * @param {Object} resourceId
	 */
	getChildShapeByResourceId: function(resourceId) {

		resourceId = ERDF.__stripHashes(resourceId);
		
		return this.getChildShapes(true).find(function(shape) {
					return shape.resourceId == resourceId
				});
	},
	/**
	 * 
	 * @param {Object} deep
	 * @param {Object} iterator
	 */
	getChildShapes: function(deep, iterator) {
		var result = [];

		this.children.each(function(uiObject) {
			if(uiObject instanceof ORYX.Core.Shape && uiObject.isVisible ) {
				if(iterator) {
					iterator(uiObject);
				}
				result.push(uiObject);
				if(deep) {
					result = result.concat(uiObject.getChildShapes(deep, iterator));
				} 
			}
		});

		return result;
	},
    
    /**
     * @param {Object} shape
     * @return {boolean} true if any of shape's childs is given shape
     */
    hasChildShape: function(shape){
        return this.getChildShapes().any(function(child){
            return (child === shape) || child.hasChildShape(shape);
        });
    },
	
	/**
	 * 
	 * @param {Object} deep
	 * @param {Object} iterator
	 */
	getChildNodes: function(deep, iterator) {
		var result = [];

		this.children.each(function(uiObject) {
			if(uiObject instanceof ORYX.Core.Node && uiObject.isVisible) {
				if(iterator) {
					iterator(uiObject);
				}
				result.push(uiObject);
			}
			if(uiObject instanceof ORYX.Core.Shape) {
				if(deep) {
					result = result.concat(uiObject.getChildNodes(deep, iterator));
				}
			}
		});

		return result;
	},
	
	/**
	 * 
	 * @param {Object} deep
	 * @param {Object} iterator
	 */
	getChildEdges: function(deep, iterator) {
		var result = [];

		this.children.each(function(uiObject) {
			if(uiObject instanceof ORYX.Core.Edge && uiObject.isVisible) {
				if(iterator) {
					iterator(uiObject);
				}
				result.push(uiObject);
			}
			if(uiObject instanceof ORYX.Core.Shape) {
				if(deep) {
					result = result.concat(uiObject.getChildEdges(deep, iterator));
				}
			}
		});

		return result;
	},
	
	/**
	 * Returns a sorted array of ORYX.Core.Node objects.
	 * Ordered in z Order, the last object has the highest z Order.
	 */
	//TODO deep iterator
	getAbstractShapesAtPosition: function() {
		var x, y;
		switch (arguments.length) {
			case 1:
				x = arguments[0].x;
				y = arguments[0].y;
				break;
			case 2:	//two or more arguments
				x = arguments[0];
				y = arguments[1];
				break;
			default:
				throw "getAbstractShapesAtPosition needs 1 or 2 arguments!"
		}

		if(this.isPointIncluded(x, y)) {

			var result = [];
			result.push(this);

			//check, if one child is at that position						
			
			
			var childNodes = this.getChildNodes();
			var childEdges = this.getChildEdges();
			
			[childNodes, childEdges].each(function(ne){
				var nodesAtPosition = new Hash();
				
				ne.each(function(node) {
					if(!node.isVisible){ return }
					var candidates = node.getAbstractShapesAtPosition( x , y );
					if(candidates.length > 0) {
						var nodesInZOrder = $A(node.node.parentNode.childNodes);
						var zOrderIndex = nodesInZOrder.indexOf(node.node);
						nodesAtPosition[zOrderIndex] = candidates;
					}
				});
				
				nodesAtPosition.keys().sort().each(function(key) {
					result = result.concat(nodesAtPosition[key]);
				});
 			});
						
			return result;
			
		} else {
			return [];
		}
	},
	
	/**
	 * 
	 * @param key {String} Must be 'prefix-id' of property
	 * @param value {Object} Can be of type String or Number according to property type.
	 */
	setProperty: function(key, value, force) {
		var oldValue = this.properties[key];
		if(oldValue !== value || force === true) {
			this.properties[key] = value;
			this.propertiesChanged[key] = true;
			this._changed();
			
			// Raise an event, to show that the property has changed
			//window.setTimeout( function(){

			if (!this._isInSetProperty) {
				this._isInSetProperty = true;
				
				this._delegateEvent({
						type	: ORYX.CONFIG.EVENT_PROPERTY_CHANGED, 
						elements : [this],
						name	: key, 
						value	: value,
						oldValue: oldValue
					});
				
				delete this._isInSetProperty;
			}
			//}.bind(this), 10)
		}
	},
	
	/**
	 * Returns TRUE if one of the properties is flagged as dirty
	 * @return {boolean}
	 */
	isPropertyChanged: function(){
		return this.propertiesChanged.any(function(property){ return property.value });
	},

	/**
	 * 
	 * @param {String} Must be 'prefix-id' of property
	 * @param {Object} Can be of type String or Number according to property type.
	 */
	setHiddenProperty: function(key, value) {
		// IF undefined, Delete
		if (value === undefined) {
			delete this.hiddenProperties[key];
			return;
		}
		var oldValue = this.hiddenProperties[key];
		if (oldValue !== value) {
			this.hiddenProperties[key] = value;
		}
	},
	/**
	 * Calculate if the point is inside the Shape
	 * @param {Point}
	 */
	isPointIncluded: function(pointX, pointY, absoluteBounds) {
		var absBounds = absoluteBounds ? absoluteBounds : this.absoluteBounds();
		return absBounds.isIncluded(pointX, pointY);
				
	},
	
	/**
	 * Get the serialized object
	 * return Array with hash-entrees (prefix, name, value)
	 * Following values will given:
	 * 		Type
	 * 		Properties
	 */
	serialize: function() {
		var serializedObject = [];
		
		// Add the type
		serializedObject.push({name: 'type', prefix:'oryx', value: this.getStencil().id(), type: 'literal'});	
	
		// Add hidden properties
		this.hiddenProperties.each(function(prop){
			serializedObject.push({name: prop.key.replace("oryx-", ""), prefix: "oryx", value: prop.value, type: 'literal'});
		}.bind(this));
		
		// Add all properties
		this.getStencil().properties().each((function(property){
			
			var prefix = property.prefix();	// Get prefix
			var name = property.id();		// Get name
			
			//if(typeof this.properties[prefix+'-'+name] == 'boolean' || this.properties[prefix+'-'+name] != "")
				serializedObject.push({name: name, prefix: prefix, value: this.properties[prefix+'-'+name], type: 'literal'});

		}).bind(this));
		
		return serializedObject;
	},
		
		
	deserialize: function(serialize){
		// Search in Serialize
		var initializedDocker = 0;
		
		// Sort properties so that the hidden properties are first in the list
		serialize = serialize.sort(function(a,b){ a = Number(this.properties.keys().member(a.prefix+"-"+a.name)); b = Number(this.properties.keys().member(b.prefix+"-"+b.name)); return a > b ? 1 : (a < b ? -1 : 0) }.bind(this));
		
		serialize.each((function(obj){
			
			var name 	= obj.name;
			var prefix 	= obj.prefix;
			var value 	= obj.value;
            
            // Complex properties can be real json objects, encode them to a string
            if (Object.prototype.toString.call(value) === "Object") value = JSON.stringify(value);

			switch(prefix + "-" + name){
				case 'raziel-parent': 
							// Set parent
							if(!this.parent) {break};
							
							// Set outgoing Shape
							var parent = this.getCanvas().getChildShapeByResourceId(value);
							if(parent) {
								parent.add(this);
							}
							
							break;											
				default:
							// If list, eval as an array
							var prop = this.getStencil().property(prefix+"-"+name);
							if (prop && prop.isList() && typeof value === "string"){
								if ((value||"").strip()&&!value.startsWith("[")&&!value.startsWith("]"))
									value = "[\""+value.strip()+"\"]";
								value = ((value||"").strip()||"[]").evalJSON();
							}
							
							// Set property
							if(this.properties.keys().member(prefix+"-"+name)) {
								this.setProperty(prefix+"-"+name, value);
							} else if(!(name === "bounds"||name === "parent"||name === "target"||name === "dockers"||name === "docker"||name === "outgoing"||name === "incoming")) {
								this.setHiddenProperty(prefix+"-"+name, value);
							}
					
			}
		}).bind(this));
	},
	
	toString: function() { return "ORYX.Core.AbstractShape " + this.id },
    
    /**
     * Converts the shape to a JSON representation.
     * @return {Object} A JSON object with included ORYX.Core.AbstractShape.JSONHelper and getShape() method.
     */
    toJSON: function(){
        var json = {
            resourceId: this.resourceId,
            properties: jQuery.extend({}, this.properties, this.hiddenProperties).inject({}, function(props, prop){
              var key = prop[0];
              var value = prop[1];
                
              //If complex property, value should be a json object
              if ( this.getStencil().property(key)
                	&& this.getStencil().property(key).type() === ORYX.CONFIG.TYPE_COMPLEX 
                	&& Object.prototype.toString.call(value) === "String"){
						
                  try {value = JSON.parse(value);} catch(error){}
            	  //try {value = JSON.parse(value);} catch(error){}
              
			  // Parse date
			  } else if (value instanceof Date&&this.getStencil().property(key)){
			  	try {
					value = value.format(this.getStencil().property(key).dateFormat());
				} catch(e){}
			  }
              
              //Takes "my_property" instead of "oryx-my_property" as key
              key = key.replace(/^[\w_]+-/, "");
              props[key] = value;
              
              return props;
            }.bind(this)),
            stencil: {
                id: this.getStencil().idWithoutNs()
            },
            childShapes: this.getChildShapes().map(function(shape){
                return shape.toJSON();
            })
        };
        
        if(this.getOutgoingShapes){
            json.outgoing = this.getOutgoingShapes().map(function(shape){
                return {
                    resourceId: shape.resourceId
                };
            });
        }
        
        if(this.bounds){
            json.bounds = { 
                lowerRight: this.bounds.lowerRight(), 
                upperLeft: this.bounds.upperLeft() 
            };
        }
        
        if(this.dockers){
            json.dockers = this.dockers.map(function(docker){
                var d = docker.getDockedShape() && docker.referencePoint ? docker.referencePoint : docker.bounds.center();
                d.getDocker = function(){return docker;};
                return d;
            });
        }
        
        jQuery.extend(json, ORYX.Core.AbstractShape.JSONHelper);
        
        // do not pollute the json attributes (for serialization), so put the corresponding
        // shape is encapsulated in a method
        json.getShape = function(){
            return this;
        }.bind(this);
        
        return json;
    }
 });
 
/**
 * @namespace Collection of methods which can be used on a shape json object (ORYX.Core.AbstractShape#toJSON()).
 * @example
 * jQuery.extend(shapeAsJson, ORYX.Core.AbstractShape.JSONHelper);
 */
ORYX.Core.AbstractShape.JSONHelper = {
     /**
      * Iterates over each child shape.
      * @param {Object} iterator Iterator function getting a child shape and his parent as arguments.
      * @param {boolean} [deep=false] Iterate recursively (childShapes of childShapes)
      * @param {boolean} [modify=false] If true, the result of the iterator function is taken as new shape, return false to delete it. This enables modifying the object while iterating through the child shapes.
      * @example
      * // Increases the lowerRight x value of each direct child shape by one. 
      * myShapeAsJson.eachChild(function(shape, parentShape){
      *     shape.bounds.lowerRight.x = shape.bounds.lowerRight.x + 1;
      *     return shape;
      * }, false, true);
      */
     eachChild: function(iterator, deep, modify){
         if(!this.childShapes) return;
         
         var newChildShapes = []; //needed if modify = true
         
         this.childShapes.each(function(shape){
		 	 if (!(shape.eachChild instanceof Function)){
		 		jQuery.extend(shape, ORYX.Core.AbstractShape.JSONHelper);
			 }
             var res = iterator(shape, this);
             if(res) newChildShapes.push(res); //if false is returned, and modify = true, current shape is deleted.
             
             if(deep) shape.eachChild(iterator, deep, modify);
         }.bind(this));
         
         if(modify) this.childShapes = newChildShapes;
     },
     
	 getShape: function(){
	 	return null;
	 },
     getChildShapes: function(deep){
         var allShapes = this.childShapes;
         
         if(deep){
             this.eachChild(function(shape){
			 	 if (!(shape.getChildShapes instanceof Function)){
			 		jQuery.extend(shape, ORYX.Core.AbstractShape.JSONHelper);
				 }
                 allShapes = allShapes.concat(shape.getChildShapes(deep));
             }, true);
         }
         
         return allShapes;
     },
     
     /**
      * @return {String} Serialized JSON object
      */
     serialize: function(){
         return JSON.stringify(this);
     }
 }
/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

/**
 * Init namespaces
 */
if(!ORYX) {var ORYX = {};}

/**
   @namespace Namespace for the Oryx core elements.
   @name ORYX.Core
*/
if(!ORYX.Core) {ORYX.Core = {};}

/**
 * @class Oryx canvas.
 * @extends ORYX.Core.AbstractShape
 *
 */
ORYX.Core.Canvas = ORYX.Core.AbstractShape.extend({
    /** @lends ORYX.Core.Canvas.prototype */

	/**
	 * Defines the current zoom level
	 */
	zoomLevel:1,

	/**
	 * Constructor
	 */
	construct: function(options, stencil, facade) {
		arguments.callee.$.construct.apply(this, arguments);

		if(!(options && options.width && options.height)) {
		
			ORYX.Log.fatal("Canvas is missing mandatory parameters options.width and options.height.");
			return;
		}
		this.facade = facade;	
		//TODO: set document resource id
		this.resourceId = options.id;

		this.nodes = [];
		
		this.edges = [];
		
		// Row highlighting states
		this.colHighlightState = 0;
		
		this.colHighlightEnabled = false; 
		
		//init svg document
		this.rootNode = ORYX.Editor.graft("http://www.w3.org/2000/svg", options.parentNode,
			['svg', {id: this.id, width: options.width, height: options.height},
				['defs', {}]
			]);
			
		this.rootNode.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
		this.rootNode.setAttribute("xmlns:svg", "http://www.w3.org/2000/svg");

		this._htmlContainer = ORYX.Editor.graft("http://www.w3.org/1999/xhtml", options.parentNode,
			['div', {id: "oryx_canvas_htmlContainer", style:"position:absolute; top:5px"}]);

		// Additional SVG-node BELOW the stencils to allow underlays (if that is even a word) by plugins
		this.underlayNode = ORYX.Editor.graft("http://www.w3.org/2000/svg", this.rootNode,
				['svg', {id: "underlay-container"}]);
		
		// Create 2 svg-elements in the svg-container
		this.columnHightlight1 = ORYX.Editor.graft("http://www.w3.org/2000/svg", this.underlayNode,
				['rect', {x: 0, width: ORYX.CONFIG.FORM_ROW_WIDTH + 35, height: "100%", style: "fill: #fff6d5", visibility: "hidden"}]);
		
		this.columnHightlight2 = ORYX.Editor.graft("http://www.w3.org/2000/svg", this.underlayNode,
				['rect', {x: ORYX.CONFIG.FORM_ROW_WIDTH + 35, width: ORYX.CONFIG.FORM_ROW_WIDTH + 25, height: "100%", style: "fill: #fff6d5", visibility: "hidden"}]);
		
		this.node = ORYX.Editor.graft("http://www.w3.org/2000/svg", this.rootNode,
			['g', {},
				['g', {"class": "stencils"},
					['g', {"class": "me"}],
					['g', {"class": "children"}],
					['g', {"class": "edge"}]
				],
				['g', {"class":"svgcontainer"}]
			]);
		
		/*
		var off = 2 * ORYX.CONFIG.GRID_DISTANCE;
		var size = 3;
		var d = "";
		for(var i = 0; i <= options.width; i += off)
			for(var j = 0; j <= options.height; j += off)
				d = d + "M" + (i - size) + " " + j + " l" + (2*size) + " 0 m" + (-size) + " " + (-size) + " l0 " + (2*size) + " m0" + (-size) + " ";
							
		ORYX.Editor.graft("http://www.w3.org/2000/svg", this.node.firstChild.firstChild,
			['path', {d:d , stroke:'#000000', 'stroke-width':'0.15px'},]);
		*/
		
		//Global definition of default font for shapes
		//Definitions in the SVG definition of a stencil will overwrite these settings for
		// that stencil.
		/*if(navigator.platform.indexOf("Mac") > -1) {
			this.node.setAttributeNS(null, 'stroke', 'black');
			this.node.setAttributeNS(null, 'stroke-width', '0.5px');
			this.node.setAttributeNS(null, 'font-family', 'Skia');
			//this.node.setAttributeNS(null, 'letter-spacing', '2px');
			this.node.setAttributeNS(null, 'font-size', ORYX.CONFIG.LABEL_DEFAULT_LINE_HEIGHT);
		} else {
			this.node.setAttributeNS(null, 'stroke', 'none');
			this.node.setAttributeNS(null, 'font-family', 'Verdana');
			this.node.setAttributeNS(null, 'font-size', ORYX.CONFIG.LABEL_DEFAULT_LINE_HEIGHT);
		}*/
		
		this.node.setAttributeNS(null, 'stroke', 'none');
		this.node.setAttributeNS(null, 'font-family', 'Verdana, sans-serif');
		this.node.setAttributeNS(null, 'font-size-adjust', 'none');
		this.node.setAttributeNS(null, 'font-style', 'normal');
		this.node.setAttributeNS(null, 'font-variant', 'normal');
		this.node.setAttributeNS(null, 'font-weight', 'normal');
		this.node.setAttributeNS(null, 'line-heigth', 'normal');
		
		this.node.setAttributeNS(null, 'font-size', ORYX.CONFIG.LABEL_DEFAULT_LINE_HEIGHT);
			
		this.bounds.set(0,0,options.width, options.height);
		
		this.addEventHandlers(this.rootNode.parentNode);
		
		//disable context menu
		this.rootNode.oncontextmenu = function() {return false;};
	},
	
	focus: function(){
		
		try {
			// Get a href
			if (!this.focusEl) 
			{
				this.focusEl = jQuery('body').append(jQuery('<a href="#" class="x-grid3-focus x-grid3-focus-canvas"/>'));
				this.focusEl.swallowEvent("click", true);
			}
			
			// Focus it
			this.focusEl.focus.defer(1, this.focusEl);
			this.focusEl.blur.defer(3, this.focusEl);
			
		} catch(e){}
	},
	
	setHightlightState: function(state) {
		if(this.colHighlightEnabled && this.colHighlightState != state) {
			if(state == 0) {
				this.columnHightlight1.setAttribute("visibility", "hidden");
				this.columnHightlight2.setAttribute("visibility", "hidden");
			} else if(state == 1) {
				this.columnHightlight1.setAttribute("visibility", "visible");
				this.columnHightlight2.setAttribute("visibility", "hidden");
			} else if(state == 2) {
				this.columnHightlight1.setAttribute("visibility", "hidden");
				this.columnHightlight2.setAttribute("visibility", "visible");
			} else if(state == 3) {
				this.columnHightlight1.setAttribute("visibility", "visible");
				this.columnHightlight2.setAttribute("visibility", "visible");
			}
			this.colHighlightState = state;
		}
	},
	
	setHightlightStateBasedOnX : function(x) {
		if(x > ORYX.CONFIG.FORM_ROW_WIDTH + 30) {
			this.setHightlightState(2);
		} else {
			this.setHightlightState(1);
		}
	},
	
	update: function() {
		
		this.nodes.each(function(node) {
			this._traverseForUpdate(node);
		}.bind(this));
		
		// call stencil's layout callback
		// (needed for row layouting of xforms)
		//this.getStencil().layout(this);
		
		var layoutEvents = this.getStencil().layout();
		
		if(layoutEvents) {
			layoutEvents.each(function(event) {
		
				// setup additional attributes
				event.shape = this;
				event.forceExecution = true;
				event.target = this.rootNode;
				
				// do layouting
				
				this._delegateEvent(event);
			}.bind(this))
		}
		
		this.nodes.invoke("_update");
		
		this.edges.invoke("_update", true);
		
		/*this.children.each(function(child) {
			child._update();
		});*/
	},
	
	_traverseForUpdate: function(shape) {
		var childRet = shape.isChanged;
		shape.getChildNodes(false, function(child) {
			if(this._traverseForUpdate(child)) {
				childRet = true;
			}
		}.bind(this));
		
		if(childRet) {
			shape.layout();
			return true;
		} else {
			return false;
		}
	},
	
	layout: function() {
		
		
		
	},
	
	/**
	 * 
	 * @param {Object} deep
	 * @param {Object} iterator
	 */
	getChildNodes: function(deep, iterator) {
		if(!deep && !iterator) {
			return this.nodes.clone();
		} else {
			var result = [];
			this.nodes.each(function(uiObject) {
				if(iterator) {
					iterator(uiObject);
				}
				result.push(uiObject);
				
				if(deep && uiObject instanceof ORYX.Core.Shape) {
					result = result.concat(uiObject.getChildNodes(deep, iterator));
				}
			});
	
			return result;
		}
	},
	
	/**
	 * buggy crap! use base class impl instead! 
	 * @param {Object} iterator
	 */
/*	getChildEdges: function(iterator) {
		if(iterator) {
			this.edges.each(function(edge) {
				iterator(edge);
			});
		}
		
		return this.edges.clone();
	},
*/	
	/**
	 * Overrides the UIObject.add method. Adds uiObject to the correct sub node.
	 * @param {UIObject} uiObject
	 */
	add: function(uiObject, index, silent) {
		//if uiObject is child of another UIObject, remove it.
		if(uiObject instanceof ORYX.Core.UIObject) {
			if (!(this.children.member(uiObject))) {
				//if uiObject is child of another parent, remove it from that parent.
				if(uiObject.parent) {
					uiObject.parent.remove(uiObject, true);
				}

				//add uiObject to the Canvas
				//add uiObject to this Shape
				if(index != undefined)
					this.children.splice(index, 0, uiObject);
				else
					this.children.push(uiObject);

				//set parent reference
				uiObject.parent = this;

				//add uiObject.node to this.node depending on the type of uiObject
				if(uiObject instanceof ORYX.Core.Shape) {
					if(uiObject instanceof ORYX.Core.Edge) {
						uiObject.addMarkers(this.rootNode.getElementsByTagNameNS(NAMESPACE_SVG, "defs")[0]);
						uiObject.node = this.node.childNodes[0].childNodes[2].appendChild(uiObject.node);
						this.edges.push(uiObject);
					} else {
						uiObject.node = this.node.childNodes[0].childNodes[1].appendChild(uiObject.node);
						this.nodes.push(uiObject);
					}
				} else {	//UIObject
					uiObject.node = this.node.appendChild(uiObject.node);
				}

				uiObject.bounds.registerCallback(this._changedCallback);
					
				if(this.eventHandlerCallback && silent !== true)
					this.eventHandlerCallback({type:ORYX.CONFIG.EVENT_SHAPEADDED,shape:uiObject})
			} else {
				
				ORYX.Log.warn("add: ORYX.Core.UIObject is already a child of this object.");
			}
		} else {

			ORYX.Log.fatal("add: Parameter is not of type ORYX.Core.UIObject.");
		}
	},

	/**
	 * Overrides the UIObject.remove method. Removes uiObject.
	 * @param {UIObject} uiObject
	 */
	remove: function(uiObject, silent) {
		//if uiObject is a child of this object, remove it.
		if (this.children.member(uiObject)) {
			//remove uiObject from children
			var parent = uiObject.parent;
						
			this.children = this.children.without(uiObject);

			//delete parent reference of uiObject
			uiObject.parent = undefined;

			//delete uiObject.node from this.node
			if(uiObject instanceof ORYX.Core.Shape) {
				if(uiObject instanceof ORYX.Core.Edge) {
					uiObject.removeMarkers();
					uiObject.node = this.node.childNodes[0].childNodes[2].removeChild(uiObject.node);
					this.edges = this.edges.without(uiObject);
				} else {
					uiObject.node = this.node.childNodes[0].childNodes[1].removeChild(uiObject.node);
					this.nodes = this.nodes.without(uiObject);
				}
			} else {	//UIObject
					uiObject.node = this.node.removeChild(uiObject.node);
			}

			if(this.eventHandlerCallback && silent !== true)
				this.eventHandlerCallback({type:ORYX.CONFIG.EVENT_SHAPEREMOVED,shape:uiObject, parent:parent});
				
			uiObject.bounds.unregisterCallback(this._changedCallback);
		} else {

			ORYX.Log.warn("remove: ORYX.Core.UIObject is not a child of this object.");
		}
	},
    
    /**
     * Creates shapes out of the given collection of shape objects and adds them to the canvas.
     * @example 
     * canvas.addShapeObjects({
         bounds:{ lowerRight:{ y:510, x:633 }, upperLeft:{ y:146, x:210 } },
         resourceId:"oryx_F0715955-50F2-403D-9851-C08CFE70F8BD",
         childShapes:[],
         properties:{},
         stencil:{
           id:"Subprocess"
         },
         outgoing:[{resourceId: 'aShape'}],
         target: {resourceId: 'aShape'}
       });
     * @param {Object} shapeObjects 
     * @param {Function} [eventHandler] An event handler passed to each newly created shape (as eventHandlerCallback)
     * @return {Array} A collection of ORYX.Core.Shape
     * @methodOf ORYX.Core.Canvas.prototype
     */
    addShapeObjects: function(shapeObjects, eventHandler){
        if(!shapeObjects) return;
		
		this.initializingShapes = true;
        
        /*FIXME This implementation is very evil! At first, all shapes are created on
          canvas. In a second step, the attributes are applied. There must be a distinction
          between the configuration phase (where the outgoings, for example, are just named),
          and the creation phase (where the outgoings are evaluated). This must be reflected
          in code to provide a nicer API/ implementation!!! */
        
        var addShape = function(shape, parent){
            // Create a new Stencil
            var stencil = ORYX.Core.StencilSet.stencil(this.getStencil().namespace() + shape.stencil.id );

            // Create a new Shape
            var ShapeClass = (stencil.type() == "node") ? ORYX.Core.Node : ORYX.Core.Edge;
            var newShape = new ShapeClass(
              {'eventHandlerCallback': eventHandler},
              stencil, this.facade);
            
            // Set the resource id
            newShape.resourceId = shape.resourceId;
            newShape.node.id = "svg-" + shape.resourceId;
			
            // Set parent to json object to be used later
            // Due to the nested json structure, normally shape.parent is not set/ must not be set. 
            // In special cases, it can be easier to set this directly instead of a nested structure.
            shape.parent = "#" + ((shape.parent && shape.parent.resourceId) || parent.resourceId);
            
            // Add the shape to the canvas
            this.add( newShape );

            return {
              json: shape,
              object: newShape
            };
        }.bind(this);
        
        /** Builds up recursively a flatted array of shapes, including a javascript object and json representation
         * @param {Object} shape Any object that has Object#childShapes
         */
        var addChildShapesRecursively = function(shape){
            var addedShapes = [];
        
            if (shape.childShapes && shape.childShapes.constructor == String)
            {
            	shape.childShapes = JSON.parse(shape.childShapes);
            }
 
            shape.childShapes.each(function(childShape){
              addedShapes.push(addShape(childShape, shape));
              addedShapes = addedShapes.concat(addChildShapesRecursively(childShape));
            });
            
            return addedShapes;
        }.bind(this);

        var shapes = addChildShapesRecursively({
            childShapes: shapeObjects, 
            resourceId: this.resourceId
        });
                    

        // prepare deserialisation parameter
        shapes.each(
            function(shape){
            	var properties = [];
                for(field in shape.json.properties){
                    properties.push({
                      prefix: 'oryx',
                      name: field,
                      value: shape.json.properties[field]
                    });
                  }
                  
                  // Outgoings
                  shape.json.outgoing.each(function(out){
                    properties.push({
                      prefix: 'raziel',
                      name: 'outgoing',
                      value: "#"+out.resourceId
                    });
                  });
                  
                  // Target 
                  // (because of a bug, the first outgoing is taken when there is no target,
                  // can be removed after some time)
                  if(shape.object instanceof ORYX.Core.Edge) {
	                  var target = shape.json.target || shape.json.outgoing[0];
	                  if(target){
	                    properties.push({
	                      prefix: 'raziel',
	                      name: 'target',
	                      value: "#"+target.resourceId
	                    });
	                  }
                  }
                  
                  // Bounds
                  if (shape.json.bounds) {
                      properties.push({
                          prefix: 'oryx',
                          name: 'bounds',
                          value: shape.json.bounds.upperLeft.x + "," + shape.json.bounds.upperLeft.y + "," + shape.json.bounds.lowerRight.x + "," + shape.json.bounds.lowerRight.y
                      });
                  }
                  
                  //Dockers [{x:40, y:50}, {x:30, y:60}] => "40 50 30 60  #"
                  if(shape.json.dockers){
                    properties.push({
                      prefix: 'oryx',
                      name: 'dockers',
                      value: shape.json.dockers.inject("", function(dockersStr, docker){
                        return dockersStr + docker.x + " " + docker.y + " ";
                      }) + " #"
                    });
                  }
                  
                  //Parent
                  properties.push({
                    prefix: 'raziel',
                    name: 'parent',
                    value: shape.json.parent
                  });
            
                  shape.__properties = properties;
	         }.bind(this)
        );
  
        // Deserialize the properties from the shapes
        // This can't be done earlier because Shape#deserialize expects that all referenced nodes are already there
        
        // first, deserialize all nodes
        shapes.each(function(shape) {
        	if(shape.object instanceof ORYX.Core.Node) {
        		shape.object.deserialize(shape.__properties, shape.json);
        	}
        });
        
        // second, deserialize all edges
        shapes.each(function(shape) {
        	if(shape.object instanceof ORYX.Core.Edge) {
        		shape.object.deserialize(shape.__properties, shape.json);
				shape.object._oldBounds = shape.object.bounds.clone();
				shape.object._update();
        	}
        });
       
		delete this.initializingShapes;
        return shapes.pluck("object");
    },
    
    /**
     * Updates the size of the canvas, regarding to the containg shapes.
     */
    updateSize: function(){
        // Check the size for the canvas
        var maxWidth    = 0;
        var maxHeight   = 0;
        var offset      = 100;
        this.getChildShapes(true, function(shape){
            var b = shape.bounds;
            maxWidth    = Math.max( maxWidth, b.lowerRight().x + offset)
            maxHeight   = Math.max( maxHeight, b.lowerRight().y + offset)
        }); 
        
        if( this.bounds.width() < maxWidth || this.bounds.height() < maxHeight ){
            this.setSize({width: Math.max(this.bounds.width(), maxWidth), height: Math.max(this.bounds.height(), maxHeight)})
        }
    },

	getRootNode: function() {
		return this.rootNode;
	},
	
	getUnderlayNode: function() {
		return this.underlayNode;
	},
	
	getSvgContainer: function() {
		return this.node.childNodes[1];
	},
	
	getHTMLContainer: function() {
		return this._htmlContainer;
	},	

	/**
	 * Return all elements of the same highest level
	 * @param {Object} elements
	 */
	getShapesWithSharedParent: function(elements) {

		// If there is no elements, return []
		if(!elements || elements.length < 1) { return []; }
		// If there is one element, return this element
		if(elements.length == 1) { return elements;}

		return elements.findAll(function(value){
			var parentShape = value.parent;
			while(parentShape){
				if(elements.member(parentShape)) return false;
				parentShape = parentShape.parent;
			}
			return true;
		});		

	},

	setSize: function(size, dontSetBounds) {
		if(!size || !size.width || !size.height){return;};
		
		if(this.rootNode.parentNode){
			this.rootNode.parentNode.style.width = size.width + 'px';
			this.rootNode.parentNode.style.height = size.height + 'px';
		}
		
		this.rootNode.setAttributeNS(null, 'width', size.width);
		this.rootNode.setAttributeNS(null, 'height', size.height);

		//this._htmlContainer.style.top = "-" + (size.height + 4) + 'px';		
		if( !dontSetBounds ){
			this.bounds.set({a:{x:0,y:0},b:{x:size.width/this.zoomLevel,y:size.height/this.zoomLevel}});
		}
	},
	
	/**
	 * Returns an SVG document of the current process.
	 * @param {Boolean} escapeText Use true, if you want to parse it with an XmlParser,
	 * 					false, if you want to use the SVG document in browser on client side.
	 */
	getSVGRepresentation: function(escapeText) {
		// Get the serialized svg image source
        var svgClone = this.getRootNode().cloneNode(true);
		
		this._removeInvisibleElements(svgClone);
		
		var x1, y1, x2, y2;
		this.getChildShapes(true).each(function(shape) {
			var absBounds = shape.absoluteBounds();
			var ul = absBounds.upperLeft();
			var lr = absBounds.lowerRight();
			if(x1 == undefined) {
				x1 = ul.x;
				y1 = ul.y;
				x2 = lr.x;
				y2 = lr.y;
			} else {
				x1 = Math.min(x1, ul.x);
				y1 = Math.min(y1, ul.y);
				x2 = Math.max(x2, lr.x);
				y2 = Math.max(y2, lr.y);
			}
		});
		
		var margin = 50;
		var width, height, tx, ty;
		if(x1 == undefined) {
			width = 0;
			height = 0;
			tx = 0;
			ty = 0;
		} else {
			width = x2;
			height = y2;
			tx = -x1+margin/2;
			ty = -y1+margin/2;
		}
		
        // Set the width and height
        svgClone.setAttributeNS(null, 'width', width + margin);
        svgClone.setAttributeNS(null, 'height', height + margin);
		
		//remove scale factor
		svgClone.childNodes[1].removeAttributeNS(null, 'transform');
		
		try{
			var svgCont = svgClone.childNodes[1].childNodes[1];
			svgCont.parentNode.removeChild(svgCont);
		} catch(e) {}

		if(escapeText) {
			$A(svgClone.getElementsByTagNameNS(ORYX.CONFIG.NAMESPACE_SVG, 'tspan')).each(function(elem) {
				elem.textContent = elem.textContent.escapeHTML();
			});
			
			$A(svgClone.getElementsByTagNameNS(ORYX.CONFIG.NAMESPACE_SVG, 'text')).each(function(elem) {
				if(elem.childNodes.length == 0)
					elem.textContent = elem.textContent.escapeHTML();
			});
		}
		
		// generating absolute urls for the pdf-exporter
		$A(svgClone.getElementsByTagNameNS(ORYX.CONFIG.NAMESPACE_SVG, 'image')).each(function(elem) {
			var href = elem.getAttributeNS("http://www.w3.org/1999/xlink","href");
			
			if(!href.match("^(http|https)://")) {
				href = window.location.protocol + "//" + window.location.host + href;
				elem.setAttributeNS("http://www.w3.org/1999/xlink", "href", href);
			}
		});
		
		
		// escape all links
		$A(svgClone.getElementsByTagNameNS(ORYX.CONFIG.NAMESPACE_SVG, 'a')).each(function(elem) {
			elem.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", (elem.getAttributeNS("http://www.w3.org/1999/xlink","href")||"").escapeHTML());
		});
		
        return svgClone;
	},
	
	/**   
	* Removes all nodes (and its children) that has the
	* attribute visibility set to "hidden"
	*/
	_removeInvisibleElements: function(element) {
		var index = 0;
		while(index < element.childNodes.length) {
			var child = element.childNodes[index];
			if(child.getAttributeNS &&
				child.getAttributeNS(null, "visibility") === "hidden") {
				element.removeChild(child);
			} else {
				this._removeInvisibleElements(child);
				index++; 
			}
		}
		
	},
	
	/**
	 * This method checks all shapes on the canvas and removes all shapes that
	 * contain invalid bounds values or dockers values(NaN)
	 */
	/*cleanUp: function(parent) {
		if (!parent) {
			parent = this;
		}
		parent.getChildShapes().each(function(shape){
			var a = shape.bounds.a;
			var b = shape.bounds.b;
			if (isNaN(a.x) || isNaN(a.y) || isNaN(b.x) || isNaN(b.y)) {
				parent.remove(shape);
			}
			else {
				shape.getDockers().any(function(docker) {
					a = docker.bounds.a;
					b = docker.bounds.b;
					if (isNaN(a.x) || isNaN(a.y) || isNaN(b.x) || isNaN(b.y)) {
						parent.remove(shape);
						return true;
					}
					return false;
				});
				shape.getMagnets().any(function(magnet) {
					a = magnet.bounds.a;
					b = magnet.bounds.b;
					if (isNaN(a.x) || isNaN(a.y) || isNaN(b.x) || isNaN(b.y)) {
						parent.remove(shape);
						return true;
					}
					return false;
				});
				this.cleanUp(shape);
			}
		}.bind(this));
	},*/

	_delegateEvent: function(event) {
		if(this.eventHandlerCallback && ( event.target == this.rootNode || event.target == this.rootNode.parentNode )) {
			this.eventHandlerCallback(event, this);
		}
	},
	
	toString: function() { return "Canvas " + this.id },
    
    /**
     * Calls {@link ORYX.Core.AbstractShape#toJSON} and adds some stencil set information.
     */
    toJSON: function() {
        var json = arguments.callee.$.toJSON.apply(this, arguments);
        
//		if(ORYX.CONFIG.STENCILSET_HANDLER.length > 0) {
//			json.stencilset = {
//				url: this.getStencil().stencilSet().namespace()
//	        };
//		} else {
			json.stencilset = {
				url: this.getStencil().stencilSet().source(),
				namespace: this.getStencil().stencilSet().namespace()
	        };	
//		}
        
        
        return json;
    }
 });/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
var idCounter = 0;
var ID_PREFIX = "resource";

/**
 * Main initialization method. To be called when loading
 * of the document, including all scripts, is completed.
 */
function init() {

	ORYX.Log.debug("Querying editor instances");

	// Hack for WebKit to set the SVGElement-Classes
	ORYX.Editor.setMissingClasses();
    
    // If someone wants to create the editor instance himself
    if (window.onOryxResourcesLoaded) {
        window.onOryxResourcesLoaded();
    } 
    // Else fetch the model from server and display editor
    else {
		var modelId = window.location.search.substring(4);
		var modelUrl = "./service/model/" + modelId + "/json";

        ORYX.Editor.createByUrl(modelUrl);
    }
}

/**
   @namespace Global Oryx name space
   @name ORYX
*/
if(!ORYX) {var ORYX = {};}

/**
 * The Editor class.
 * @class ORYX.Editor
 * @extends Clazz
 * @param {Object} config An editor object, passed to {@link ORYX.Editor#loadSerialized}
 * @param {String} config.id Any ID that can be used inside the editor. If fullscreen=false, any HTML node with this id must be present to render the editor to this node.
 * @param {boolean} [config.fullscreen=true] Render editor in fullscreen mode or not.
 * @param {String} config.stencilset.url Stencil set URL.
 * @param {String} [config.stencil.id] Stencil type used for creating the canvas.  
 * @param {Object} config.properties Any properties applied to the canvas.
*/
ORYX.Editor = {
    /** @lends ORYX.Editor.prototype */
	// Defines the global dom event listener 
	DOMEventListeners: new Hash(),

	// Defines the selection
	selection: [],
	
	// Defines the current zoom level
	zoomLevel:1.0,

	construct: function(config) {
		
		// initialization.
		this._eventsQueue 	= [];
		this.loadedPlugins 	= [];
		this.pluginsData 	= [];
		
		
		//meta data about the model for the signavio warehouse
		//directory, new, name, description, revision, model (the model data)
		
		this.modelMetaData = config;
		
		var model = config;
		
		this.id = model.modelId;
		
		if(config.model) {
			model = config.model;
		}
		
        if(!this.id) {
        	this.id = model.id;
        	if(!this.id) {
        		this.id = ORYX.Editor.provideId();
        	}
        }
		
        // Defines if the editor should be fullscreen or not
		this.fullscreen = config.fullscreen !== false;
		
		// Initialize the eventlistener
		this._initEventListener();

		// Load particular stencilset
		if(ORYX.CONFIG.BACKEND_SWITCH) {
			var ssUrl = (model.stencilset.namespace||model.stencilset.url).replace("#", "%23");
        	ORYX.Core.StencilSet.loadStencilSet(ssUrl, this.modelMetaData, this.id);
		} else {
			var ssUrl = model.stencilset.url;
        	ORYX.Core.StencilSet.loadStencilSet(ssUrl, this.modelMetaData, this.id);
		}

		// CREATES the canvas
		this._createCanvas(model.stencil ? model.stencil.id : null, model.properties);

		// GENERATES the whole EXT.VIEWPORT
		this._generateGUI();

		// Initializing of a callback to check loading ends
		var loadPluginFinished 	= false;
		var loadContentFinished = false;
		var initFinished = function(){	
			if( !loadPluginFinished || !loadContentFinished ){ return }
			this._finishedLoading();
		}.bind(this)
		
		// LOAD the plugins
		window.setTimeout(function(){
			this.loadPlugins();
			loadPluginFinished = true;
			initFinished();
		}.bind(this), 100);

		// LOAD the content of the current editor instance
		window.setTimeout(function(){
            this.loadSerialized(model, true); // Request the meta data as well
            this.getCanvas().update();
			loadContentFinished = true;
			initFinished();
		}.bind(this), 200);
	},
	
	_finishedLoading: function() {
		// Raise Loaded Event
		this.handleEvents( {type:ORYX.CONFIG.EVENT_LOADED} )
	},
	
	_initEventListener: function(){

		// Register on Events
		
		document.documentElement.addEventListener(ORYX.CONFIG.EVENT_KEYDOWN, this.catchKeyDownEvents.bind(this), false);
		document.documentElement.addEventListener(ORYX.CONFIG.EVENT_KEYUP, this.catchKeyUpEvents.bind(this), false);

		// Enable Key up and down Event
		this._keydownEnabled = 	true;
		this._keyupEnabled =  	true;

		this.DOMEventListeners[ORYX.CONFIG.EVENT_MOUSEDOWN] = [];
		this.DOMEventListeners[ORYX.CONFIG.EVENT_MOUSEUP] 	= [];
		this.DOMEventListeners[ORYX.CONFIG.EVENT_MOUSEOVER] = [];
		this.DOMEventListeners[ORYX.CONFIG.EVENT_MOUSEOUT] 	= [];
		this.DOMEventListeners[ORYX.CONFIG.EVENT_SELECTION_CHANGED] = [];
		this.DOMEventListeners[ORYX.CONFIG.EVENT_MOUSEMOVE] = [];
				
	},
	
	/**
	 * Generate the whole viewport of the
	 * Editor and initialized the Ext-Framework
	 * 
	 */
	_generateGUI: function() {

		// Defines the layout height if it's NOT fullscreen
		var layoutHeight 	= ORYX.CONFIG.WINDOW_HEIGHT;
		var canvasParent	= this.getCanvas().rootNode.parentNode;
		
		jQuery("#canvasSection").append(canvasParent);
		
		// Set the editor to the center, and refresh the size
	 	canvasParent.parentNode.setAttributeNS(null, 'align', 'center');
	 	canvasParent.setAttributeNS(null, 'align', 'left');
		this.getCanvas().setSize({
			width	: ORYX.CONFIG.CANVAS_WIDTH,
			height	: ORYX.CONFIG.CANVAS_HEIGHT
		});		
						
	},
	
	getAvailablePlugins: function(){
		var curAvailablePlugins=ORYX.availablePlugins.clone();
		curAvailablePlugins.each(function(plugin){
			if(this.loadedPlugins.find(function(loadedPlugin){
				return loadedPlugin.type==this.name;
			}.bind(plugin))){
				plugin.engaged=true;
			}else{
				plugin.engaged=false;
			}
			}.bind(this));
		return curAvailablePlugins;
	},

	loadScript: function (url, callback){
	    var script = document.createElement("script")
	    script.type = "text/javascript";
	    if (script.readyState){  //IE
	        script.onreadystatechange = function(){
	            if (script.readyState == "loaded" || script.readyState == "complete"){
	                script.onreadystatechange = null;
	                callback();
	            }
        	};
    	} else {  //Others
	        script.onload = function(){
	            callback();
	        };
		}
	    script.src = url;
		document.getElementsByTagName("head")[0].appendChild(script);
	},
	/**
	 * activate Plugin
	 * 
	 * @param {String} name
	 * @param {Function} callback
	 * 		callback(sucess, [errorCode])
	 * 			errorCodes: NOTUSEINSTENCILSET, REQUIRESTENCILSET, NOTFOUND, YETACTIVATED
	 */
	activatePluginByName: function(name, callback, loadTry){

		var match=this.getAvailablePlugins().find(function(value){return value.name==name});
		if(match && (!match.engaged || (match.engaged==='false'))){		
				var loadedStencilSetsNamespaces = this.getStencilSets().keys();
				var facade = this._getPluginFacade();
				var newPlugin;
				var me=this;
				ORYX.Log.debug("Initializing plugin '%0'", match.name);
				
					if (!match.requires 	|| !match.requires.namespaces 	|| match.requires.namespaces.any(function(req){ return loadedStencilSetsNamespaces.indexOf(req) >= 0 }) ){
						if(!match.notUsesIn 	|| !match.notUsesIn.namespaces 	|| !match.notUsesIn.namespaces.any(function(req){ return loadedStencilSetsNamespaces.indexOf(req) >= 0 })){
	
					try {
						
						var className 	= eval(match.name);
							var newPlugin = new className(facade, match);
							newPlugin.type = match.name;
							
							// If there is an GUI-Plugin, they get all Plugins-Offer-Meta-Data
							if (newPlugin.registryChanged) 
								newPlugin.registryChanged(me.pluginsData);
							
							// If there have an onSelection-Method it will pushed to the Editor Event-Handler
							if (newPlugin.onSelectionChanged) 
								me.registerOnEvent(ORYX.CONFIG.EVENT_SELECTION_CHANGED, newPlugin.onSelectionChanged.bind(newPlugin));
							this.loadedPlugins.push(newPlugin);
							this.loadedPlugins.each(function(loaded){
								if(loaded.registryChanged)
									loaded.registryChanged(this.pluginsData);
							}.bind(me));
							callback(true);
						
					} catch(e) {
						ORYX.Log.warn("Plugin %0 is not available", match.name);
						if(!!loadTry){
							callback(false,"INITFAILED");
							return;
						}
						this.loadScript("plugins/scripts/"+match.source, this.activatePluginByName.bind(this,match.name,callback,true));
					}
					}else{
						callback(false,"NOTUSEINSTENCILSET");
						ORYX.Log.info("Plugin need a stencilset which is not loaded'", match.name);
					}
								
				} else {
					callback(false,"REQUIRESTENCILSET");
					ORYX.Log.info("Plugin need a stencilset which is not loaded'", match.name);
				}

			
			}else{
				callback(false, match?"NOTFOUND":"YETACTIVATED");
				//TODO error handling
			}
	},

	/**
	 *  Laden der Plugins
	 */
	loadPlugins: function() {
		
		// if there should be plugins but still are none, try again.
		// TODO this should wait for every plugin respectively.
		/*if (!ORYX.Plugins && ORYX.availablePlugins.length > 0) {
			window.setTimeout(this.loadPlugins.bind(this), 100);
			return;
		}*/
		
		var me = this;
		var newPlugins = [];


		var loadedStencilSetsNamespaces = this.getStencilSets().keys();

		// Available Plugins will be initalize
		var facade = this._getPluginFacade();
		
		// If there is an Array where all plugins are described, than only take those
		// (that comes from the usage of oryx with a mashup api)
		if( ORYX.MashupAPI && ORYX.MashupAPI.loadablePlugins && ORYX.MashupAPI.loadablePlugins instanceof Array ){
		
			// Get the plugins from the available plugins (those who are in the plugins.xml)
			ORYX.availablePlugins = $A(ORYX.availablePlugins).findAll(function(value){
										return ORYX.MashupAPI.loadablePlugins.include( value.name )
									})
			
			// Add those plugins to the list, which are only in the loadablePlugins list
			ORYX.MashupAPI.loadablePlugins.each(function( className ){
				if( !(ORYX.availablePlugins.find(function(val){ return val.name == className }))){
					ORYX.availablePlugins.push( {name: className } );
				}
			})
		}
		
		
		ORYX.availablePlugins.each(function(value) {
			ORYX.Log.debug("Initializing plugin '%0'", value.name);
				if( (!value.requires 	|| !value.requires.namespaces 	|| value.requires.namespaces.any(function(req){ return loadedStencilSetsNamespaces.indexOf(req) >= 0 }) ) &&
					(!value.notUsesIn 	|| !value.notUsesIn.namespaces 	|| !value.notUsesIn.namespaces.any(function(req){ return loadedStencilSetsNamespaces.indexOf(req) >= 0 }) )&&
					/*only load activated plugins or undefined */
					(value.engaged || (value.engaged===undefined)) ){

				try {
					var className 	= eval(value.name);
					if( className ){
						var plugin		= new className(facade, value);
						plugin.type		= value.name;
						newPlugins.push( plugin );
						plugin.engaged=true;
					}
				} catch(e) {
					ORYX.Log.warn("Plugin %0 is not available %1", value.name, e);
				}
							
			} else {
				ORYX.Log.info("Plugin need a stencilset which is not loaded'", value.name);
			}
			
		});

		newPlugins.each(function(value) {
			// If there is an GUI-Plugin, they get all Plugins-Offer-Meta-Data
			if(value.registryChanged)
				value.registryChanged(me.pluginsData);

			// If there have an onSelection-Method it will pushed to the Editor Event-Handler
			if(value.onSelectionChanged)
				me.registerOnEvent(ORYX.CONFIG.EVENT_SELECTION_CHANGED, value.onSelectionChanged.bind(value));
		});

		this.loadedPlugins = newPlugins;
		
		this.registerPluginsOnKeyEvents();
		
		this.setSelection();
		
	},

	/**
	 * Creates the Canvas
	 * @param {String} [stencilType] The stencil type used for creating the canvas. If not given, a stencil with myBeRoot = true from current stencil set is taken.
	 * @param {Object} [canvasConfig] Any canvas properties (like language).
	 */
	_createCanvas: function(stencilType, canvasConfig) {
        if (stencilType) {
            // Add namespace to stencilType
            if (stencilType.search(/^http/) === -1) {
                stencilType = this.getStencilSets().values()[0].namespace() + stencilType;
            }
        }
        else {
            // Get any root stencil type
            stencilType = this.getStencilSets().values()[0].findRootStencilName();
        }
        
		// get the stencil associated with the type
		var canvasStencil = ORYX.Core.StencilSet.stencil(stencilType);
			
		if (!canvasStencil) 
			ORYX.Log.fatal("Initialisation failed, because the stencil with the type %0 is not part of one of the loaded stencil sets.", stencilType);
		
		// create all dom
		// TODO fix border, so the visible canvas has a double border and some spacing to the scrollbars
		var div = ORYX.Editor.graft("http://www.w3.org/1999/xhtml", null, ['div']);
		// set class for custom styling
		div.addClassName("ORYX_Editor");
						
		// create the canvas
		this._canvas = new ORYX.Core.Canvas({
			width					: ORYX.CONFIG.CANVAS_WIDTH,
			height					: ORYX.CONFIG.CANVAS_HEIGHT,
			'eventHandlerCallback'	: this.handleEvents.bind(this),
			id						: this.id,
			parentNode				: div
		}, canvasStencil, this._getPluginFacade());
        
        if (canvasConfig) {
          // Migrate canvasConfig to an RDF-like structure
          //FIXME this isn't nice at all because we don't want rdf any longer
          var properties = [];
          for(field in canvasConfig){
            properties.push({
              prefix: 'oryx',
              name: field,
              value: canvasConfig[field]
            });
          }
            
          this._canvas.deserialize(properties);
        }
				
	},

	/**
	 * Returns a per-editor singleton plugin facade.
	 * To be used in plugin initialization.
	 */
	_getPluginFacade: function() {

		// if there is no pluginfacade already created:
		if(!(this._pluginFacade))

			// create it.
			this._pluginFacade = {

				activatePluginByName:		this.activatePluginByName.bind(this),
				//deactivatePluginByName:		this.deactivatePluginByName.bind(this),
				getAvailablePlugins:	this.getAvailablePlugins.bind(this),
				offer:					this.offer.bind(this),
				getStencilSets:			this.getStencilSets.bind(this),
				getStencilSetExtensionDefinition:function(){ return Object.clone(this.ss_extensions_def||{})}.bind(this),
				getRules:				this.getRules.bind(this),
				loadStencilSet:			this.loadStencilSet.bind(this),
				createShape:			this.createShape.bind(this),
				deleteShape:			this.deleteShape.bind(this),
				getSelection:			this.getSelection.bind(this),
				setSelection:			this.setSelection.bind(this),
				updateSelection:		this.updateSelection.bind(this),
				getCanvas:				this.getCanvas.bind(this),
				
				importJSON:				this.importJSON.bind(this),
                getJSON:                this.getJSON.bind(this),
                getSerializedJSON:      this.getSerializedJSON.bind(this),
				
				executeCommands:		this.executeCommands.bind(this),
				isExecutingCommands:	this.isExecutingCommands.bind(this),
				
				registerOnEvent:		this.registerOnEvent.bind(this),
				unregisterOnEvent:		this.unregisterOnEvent.bind(this),
				raiseEvent:				this.handleEvents.bind(this),
				enableEvent:			this.enableEvent.bind(this),
				disableEvent:			this.disableEvent.bind(this),
				
				eventCoordinates:		this.eventCoordinates.bind(this),
				eventCoordinatesXY:		this.eventCoordinatesXY.bind(this),
								
				getModelMetaData:		this.getModelMetaData.bind(this)
			};

		// return it.
		return this._pluginFacade;
	},

	isExecutingCommands: function(){
		return !!this.commandExecuting;
	},

	/**
	 * Implementes the command pattern
	 * (The real usage of the command pattern
	 * is implemented and shown in the Plugins/undo.js)
	 *
	 * @param <Oryx.Core.Command>[] Array of commands
	 */
	executeCommands: function(commands){
		
		if (!this.commandStack){
			this.commandStack = [];
		}
		if (!this.commandStackExecuted){
			this.commandStackExecuted = [];
		}
		
		
		this.commandStack = [].concat(this.commandStack)
							  .concat(commands);
		
		// Check if already executes
		if (this.commandExecuting){ return; }
		
		// Start execution
		this.commandExecuting = true;
		
		// Iterate over all commands
		while(this.commandStack.length > 0){
			var command = this.commandStack.shift();
			// and execute it
			command.execute();
			this.commandStackExecuted.push(command);
		}
		
		// Raise event for executing commands
		this.handleEvents({
			type		: ORYX.CONFIG.EVENT_EXECUTE_COMMANDS,
			commands	: this.commandStackExecuted
		});
		
		// Remove temporary vars
		delete this.commandStack;
		delete this.commandStackExecuted;
		delete this.commandExecuting;
		
		
		this.updateSelection();

	},
	
    /**
     * Returns JSON of underlying canvas (calls ORYX.Canvas#toJSON()).
     * @return {Object} Returns JSON representation as JSON object.
     */
    getJSON: function(){
    	delete Array.prototype.toJSON;
        var canvasJSON = this.getCanvas().toJSON();
        canvasJSON.ssextensions = this.getStencilSets().values()[0].extensions().keys().findAll(function(sse){ return !sse.endsWith('/meta#') });
        return canvasJSON;
    },
    
    /**
     * Serializes a call to toJSON().
     * @return {String} Returns JSON representation as string.
     */
    getSerializedJSON: function(){
        return JSON.stringify(this.getJSON());
    },
    
	/**
	* Imports shapes in JSON as expected by {@link ORYX.Editor#loadSerialized}
	* @param {Object|String} jsonObject The (serialized) json object to be imported
	* @param {boolean } [noSelectionAfterImport=false] Set to true if no shapes should be selected after import
	* @throws {SyntaxError} If the serialized json object contains syntax errors
	*/
	importJSON: function(jsonObject, noSelectionAfterImport) {
		
        try {
            jsonObject = this.renewResourceIds(jsonObject);
        } catch(error){
            throw error;
        }     
		//check, if the imported json model can be loaded in this editor
		// (stencil set has to fit)
		if(jsonObject.stencilset.namespace && jsonObject.stencilset.namespace !== this.getCanvas().getStencil().stencilSet().namespace()) {
			alert(String.format(ORYX.I18N.JSONImport.wrongSS, jsonObject.stencilset.namespace, this.getCanvas().getStencil().stencilSet().namespace()));
			return null;
		} else {
			var commandClass = ORYX.Core.Command.extend({
			construct: function(jsonObject, loadSerializedCB, noSelectionAfterImport, facade){
				this.jsonObject = jsonObject;
				this.noSelection = noSelectionAfterImport;
				this.facade = facade;
				this.shapes;
				this.connections = [];
				this.parents = new Hash();
				this.selection = this.facade.getSelection();
				this.loadSerialized = loadSerializedCB;
			},			
			execute: function(){
				
				if (!this.shapes) {
					// Import the shapes out of the serialization		
					this.shapes	= this.loadSerialized( this.jsonObject );		
					
					//store all connections
					this.shapes.each(function(shape) {
						
						if (shape.getDockers) {
							var dockers = shape.getDockers();
							if (dockers) {
								if (dockers.length > 0) {
									this.connections.push([dockers.first(), dockers.first().getDockedShape(), dockers.first().referencePoint]);
								}
								if (dockers.length > 1) {
									this.connections.push([dockers.last(), dockers.last().getDockedShape(), dockers.last().referencePoint]);
								}
							}
						}
						
						//store parents
						this.parents[shape.id] = shape.parent;
					}.bind(this));
				} else {
					this.shapes.each(function(shape) {
						this.parents[shape.id].add(shape);
					}.bind(this));
					
					this.connections.each(function(con) {
						con[0].setDockedShape(con[1]);
						con[0].setReferencePoint(con[2]);
						con[0].update();
					});
				}
				
				//this.parents.values().uniq().invoke("update");
				this.facade.getCanvas().update();			
					
				if(!this.noSelection)
					this.facade.setSelection(this.shapes);
				else
					this.facade.updateSelection();
					
				// call updateSize again, because during loadSerialized the edges' bounds  
				// are not yet initialized properly
				this.facade.getCanvas().updateSize();	
					
				},
				rollback: function(){
					var selection = this.facade.getSelection();
					
					this.shapes.each(function(shape) {
						selection = selection.without(shape);
						this.facade.deleteShape(shape);
					}.bind(this));
					
					/*this.parents.values().uniq().each(function(parent) {
						if(!this.shapes.member(parent))
							parent.update();
					}.bind(this));*/
					
					this.facade.getCanvas().update();
					
					this.facade.setSelection(selection);
				}
			})
			
			var command = new commandClass(jsonObject, 
											this.loadSerialized.bind(this),
											noSelectionAfterImport,
											this._getPluginFacade());
			
			this.executeCommands([command]);	
			
			return command.shapes.clone();
		}
	},
    
    /**
     * This method renew all resource Ids and according references.
     * Warning: The implementation performs a substitution on the serialized object for
     * easier implementation. This results in a low performance which is acceptable if this
     * is only used when importing models.
     * @param {Object|String} jsonObject
     * @throws {SyntaxError} If the serialized json object contains syntax errors.
     * @return {Object} The jsonObject with renewed ids.
     * @private
     */
    renewResourceIds: function(jsonObject){
        // For renewing resource ids, a serialized and object version is needed
        if(Object.prototype.toString.call(jsonObject) === "String"){
            try {
                var serJsonObject = jsonObject;
                jsonObject = JSON.parse(jsonObject);
            } catch(error){
                throw new SyntaxError(error.message);
            }
        } else {
            var serJsonObject = JSON.stringify(jsonObject);
        }        
        
        // collect all resourceIds recursively
        var collectResourceIds = function(shapes){
            if(!shapes) return [];
            
            return shapes.map(function(shape){
                return collectResourceIds(shape.childShapes).concat(shape.resourceId);
            }).flatten();
        }
        var resourceIds = collectResourceIds(jsonObject.childShapes);
        
        // Replace each resource id by a new one
        resourceIds.each(function(oldResourceId){
            var newResourceId = ORYX.Editor.provideId();
            serJsonObject = serJsonObject.replace(new RegExp(oldResourceId, 'g'), newResourceId);
        });
        
        return JSON.parse(serJsonObject);
    },

    /**
     * Loads serialized model to the oryx.
     * @example
     * editor.loadSerialized({
     *    resourceId: "mymodel1",
     *    childShapes: [
     *       {
     *          stencil:{ id:"Subprocess" },
     *          outgoing:[{resourceId: 'aShape'}],
     *          target: {resourceId: 'aShape'},
     *          bounds:{ lowerRight:{ y:510, x:633 }, upperLeft:{ y:146, x:210 } },
     *          resourceId: "myshape1",
     *          childShapes:[],
     *          properties:{},
     *       }
     *    ],
     *    properties:{
     *       language: "English"
     *    },
     *    stencilset:{
     *       url:"http://localhost:8080/oryx/stencilsets/bpmn1.1/bpmn1.1.json"
     *    },
     *    stencil:{
     *       id:"BPMNDiagram"
     *    }
     * });
     * @param {Object} model Description of the model to load.
     * @param {Array} [model.ssextensions] List of stenctil set extensions.
     * @param {String} model.stencilset.url
     * @param {String} model.stencil.id 
     * @param {Array} model.childShapes
     * @param {Array} [model.properties]
     * @param {String} model.resourceId
     * @return {ORYX.Core.Shape[]} List of created shapes
     * @methodOf ORYX.Editor.prototype
     */
    loadSerialized: function(model, requestMeta){
        var canvas  = this.getCanvas();
      
        // Bugfix (cf. http://code.google.com/p/oryx-editor/issues/detail?id=240)
        // Deserialize the canvas' stencil set extensions properties first!
        this.loadSSExtensions(model.ssextensions);
		
		// Load Meta Data Extension if available
		// #Signavio
		if (requestMeta === true) {
			var metaDataExtension = this.getExtensionForMetaData();
			if (metaDataExtension) {
				this.loadSSExtension(metaDataExtension);
			}
		}
		
        var shapes = this.getCanvas().addShapeObjects(model.childShapes, this.handleEvents.bind(this));
        
        if(model.properties) {
        	for(key in model.properties) {
        		var value = model.properties[key];
				var prop = this.getCanvas().getStencil().property("oryx-"+key);
        		if (!(typeof value === "string") && (!prop || !prop.isList())) {
        			value = JSON.stringify(value);
        		}
            	this.getCanvas().setProperty("oryx-" + key, value);
            }
        }
        
        
        this.getCanvas().updateSize();
		
		// Force to update the selection
		this.selection = [null];
		this.setSelection([]);
		
        return shapes;
    },
	
	/**
	 * Return the namespace of the extension which
	 * provided all the self defined meta data
	 * @return {String} Returns null if no extension is defined, otherwise the namespace
	 *
	 */
	getExtensionForMetaData: function(){
		if (!this.ss_extensions_def||!(this.ss_extensions_def.extensions instanceof Array)){
			return null;
		}
		
		var stencilsets = this.getStencilSets();
		var extension = this.ss_extensions_def.extensions.find(function(ex){
				return !!stencilsets[ex["extends"]] && ex.namespace.endsWith("/meta#");
			});
			
		return extension ? extension.namespace || null : null;		
	},
    
    /**
     * Calls ORYX.Editor.prototype.ss_extension_namespace for each element
     * @param {Array} ss_extension_namespaces An array of stencil set extension namespaces.
     */
    loadSSExtensions: function(ss_extension_namespaces){
        if(!ss_extension_namespaces) return;

        ss_extension_namespaces.each(function(ss_extension_namespace){
            this.loadSSExtension(ss_extension_namespace);
        }.bind(this));
    },
	
	/**
	* Loads a stencil set extension.
	* The stencil set extensions definiton file must already
	* be loaded when the editor is initialized.
	*/
	loadSSExtension: function(ss_extension_namespace) {				
		
		if (this.ss_extensions_def) {
			var extension = this.ss_extensions_def.extensions.find(function(ex){
				return (ex.namespace == ss_extension_namespace);
			});
			
			if (!extension) {
				return;
			}
			
			var stencilset = this.getStencilSets()[extension["extends"]];
			
			if (!stencilset) {
				return;
			}
			
			// Check if absolute or relative url
			if ((extension["definition"]||"").startsWith("/")){
				stencilset.addExtension(extension["definition"])
			} else {
				stencilset.addExtension(ORYX.CONFIG.SS_EXTENSIONS_FOLDER + extension["definition"])
			}
			
			//stencilset.addExtension("/oryx/build/stencilsets/extensions/" + extension["definition"])
			this.getRules().initializeRules(stencilset);
			
			this._getPluginFacade().raiseEvent({
				type: ORYX.CONFIG.EVENT_STENCIL_SET_LOADED
			});
		}
		
	},

	disableEvent: function(eventType){
		if(eventType == ORYX.CONFIG.EVENT_KEYDOWN) {
			this._keydownEnabled = false;
		}
		if(eventType == ORYX.CONFIG.EVENT_KEYUP) {
			this._keyupEnabled = false;
		}
		if(this.DOMEventListeners.keys().member(eventType)) {
			var value = this.DOMEventListeners.remove(eventType);
			this.DOMEventListeners['disable_' + eventType] = value;
		}
	},

	enableEvent: function(eventType){
		if(eventType == ORYX.CONFIG.EVENT_KEYDOWN) {
			this._keydownEnabled = true;
		}
		
		if(eventType == ORYX.CONFIG.EVENT_KEYUP) {
			this._keyupEnabled = true;
		}
		
		if(this.DOMEventListeners.keys().member("disable_" + eventType)) {
			var value = this.DOMEventListeners.remove("disable_" + eventType);
			this.DOMEventListeners[eventType] = value;
		}
	},

	/**
	 *  Methods for the PluginFacade
	 */
	registerOnEvent: function(eventType, callback) {
		if(!(this.DOMEventListeners.keys().member(eventType))) {
			this.DOMEventListeners[eventType] = [];
		}

		this.DOMEventListeners[eventType].push(callback);
	},

	unregisterOnEvent: function(eventType, callback) {
		if(this.DOMEventListeners.keys().member(eventType)) {
			this.DOMEventListeners[eventType] = this.DOMEventListeners[eventType].without(callback);
		} else {
			// Event is not supported
			// TODO: Error Handling
		}
	},

	getSelection: function() {
		return this.selection || [];
	},

	getStencilSets: function() { 
		return ORYX.Core.StencilSet.stencilSets(this.id); 
	},
	
	getRules: function() {
		return ORYX.Core.StencilSet.rules(this.id);
	},
	
	loadStencilSet: function(source) {
		try {
			ORYX.Core.StencilSet.loadStencilSet(source, this.modelMetaData, this.id);
			this.handleEvents({type:ORYX.CONFIG.EVENT_STENCIL_SET_LOADED});
		} catch (e) {
			ORYX.Log.warn("Requesting stencil set file failed. (" + e + ")");
		}
	},

	offer: function(pluginData) {
		if(!this.pluginsData.member(pluginData)){
			this.pluginsData.push(pluginData);
		}
	},
	
	/**
	 * It creates an new event or adds the callback, if already existing,
	 * for the key combination that the plugin passes in keyCodes attribute
	 * of the offer method.
	 * 
	 * The new key down event fits the schema:
	 * 		key.event[.metactrl][.alt][.shift].'thekeyCode'
	 */
	registerPluginsOnKeyEvents: function() {
		this.pluginsData.each(function(pluginData) {
			
			if(pluginData.keyCodes) {
				
				pluginData.keyCodes.each(function(keyComb) {
					var eventName = "key.event";
					
					/* Include key action */
					eventName += '.' + keyComb.keyAction;
					
					if(keyComb.metaKeys) {
						/* Register on ctrl or apple meta key as meta key */
						if(keyComb.metaKeys.
							indexOf(ORYX.CONFIG.META_KEY_META_CTRL) > -1) {
								eventName += "." + ORYX.CONFIG.META_KEY_META_CTRL;
						}
							
						/* Register on alt key as meta key */
						if(keyComb.metaKeys.
							indexOf(ORYX.CONFIG.META_KEY_ALT) > -1) {
								eventName += '.' + ORYX.CONFIG.META_KEY_ALT;
						}
						
						/* Register on shift key as meta key */
						if(keyComb.metaKeys.
							indexOf(ORYX.CONFIG.META_KEY_SHIFT) > -1) {
								eventName += '.' + ORYX.CONFIG.META_KEY_SHIFT;
						}		
					}
					
					/* Register on the actual key */
					if(keyComb.keyCode)	{
						eventName += '.' + keyComb.keyCode;
					}
					
					/* Register the event */
					ORYX.Log.debug("Register Plugin on Key Event: %0", eventName);
					if (pluginData.toggle === true && pluginData.buttonInstance) {
						this.registerOnEvent(eventName, function(){
							pluginData.buttonInstance.toggle(!pluginData.buttonInstance.pressed); // Toggle 
							pluginData.functionality.call(pluginData, pluginData.buttonInstance, pluginData.buttonInstance.pressed); // Call function
						});
					} else {
						this.registerOnEvent(eventName, pluginData.functionality)
					}
				
				}.bind(this));
			}
		}.bind(this));
	},
	
	isEqual: function(a,b){
		return a === b || (a.length === b.length && a.all(function(r){ return b.include(r) }))
	},
	
	isDirty: function(a){
		return a.any(function(shape){ return shape.isPropertyChanged() })
	},

	setSelection: function(elements, subSelectionElement, force) {
		
		if (!elements) { elements = []; }
		if (!(elements instanceof Array)) { elements = [elements]; }
		
		elements = elements.findAll(function(n){ return n && n instanceof ORYX.Core.Shape });
		
		if (elements[0] instanceof ORYX.Core.Canvas) {
			elements = [];
		}
		
		if (!force && this.isEqual(this.selection, elements) && !this.isDirty(elements)){
			return;
		}
		
		this.selection = elements;
		this._subSelection = subSelectionElement;
		
		this.handleEvents({type:ORYX.CONFIG.EVENT_SELECTION_CHANGED, elements:elements, subSelection: subSelectionElement, force: !!force})
	},
	
	updateSelection: function() {
		this.setSelection(this.selection, this._subSelection, true);
		/*var s = this.selection;
		this.setSelection();
		this.setSelection(s);*/
	},

	getCanvas: function() {
		return this._canvas;
	},
	

	/**
	*	option = {
	*		type: string,
	*		position: {x:int, y:int},
	*		connectingType:	uiObj-Class
	*		connectedShape: uiObj
	*		draggin: bool
	*		namespace: url
	*       parent: ORYX.Core.AbstractShape
	*		template: a template shape that the newly created inherits properties from.
	*		}
	*/
	createShape: function(option) {

		if(option && option.serialize && option.serialize instanceof Array){
		
			var type = option.serialize.find(function(obj){return (obj.prefix+"-"+obj.name) == "oryx-type"});
			var stencil = ORYX.Core.StencilSet.stencil(type.value);
			
			if(stencil.type() == 'node'){
				var newShapeObject = new ORYX.Core.Node({'eventHandlerCallback':this.handleEvents.bind(this)}, stencil, this._getPluginFacade());	
			} else {
				var newShapeObject = new ORYX.Core.Edge({'eventHandlerCallback':this.handleEvents.bind(this)}, stencil, this._getPluginFacade());	
			}
		
			this.getCanvas().add(newShapeObject);
			newShapeObject.deserialize(option.serialize);
		
			return newShapeObject;
		}

		// If there is no argument, throw an exception
		if(!option || !option.type || !option.namespace) { throw "To create a new shape you have to give an argument with type and namespace";}
		
		var canvas = this.getCanvas();
		var newShapeObject;

		// Get the shape type
		var shapetype = option.type;

		// Get the stencil set
		var sset = ORYX.Core.StencilSet.stencilSet(option.namespace);
		// Create an New Shape, dependents on an Edge or a Node
		if(sset.stencil(shapetype).type() == "node") {
			newShapeObject = new ORYX.Core.Node({'eventHandlerCallback':this.handleEvents.bind(this)}, sset.stencil(shapetype), this._getPluginFacade())
		} else {
			newShapeObject = new ORYX.Core.Edge({'eventHandlerCallback':this.handleEvents.bind(this)}, sset.stencil(shapetype), this._getPluginFacade())
		}
		
		// when there is a template, inherit the properties.
		if(option.template) {

			newShapeObject._jsonStencil.properties = option.template._jsonStencil.properties;
			newShapeObject.postProcessProperties();
		}

		// Add to the canvas
		if(option.parent && newShapeObject instanceof ORYX.Core.Node) {
			option.parent.add(newShapeObject);
		} else {
			canvas.add(newShapeObject);
		}
		
		
		// Set the position
		var point = option.position ? option.position : {x:100, y:200};
	
		
		var con;
		// If there is create a shape and in the argument there is given an ConnectingType and is instance of an edge
		if(option.connectingType && option.connectedShape && !(newShapeObject instanceof ORYX.Core.Edge)) {

			// there will be create a new Edge
			con = new ORYX.Core.Edge({'eventHandlerCallback':this.handleEvents.bind(this)}, sset.stencil(option.connectingType));
			
			// And both endings dockers will be referenced to the both shapes
			con.dockers.first().setDockedShape(option.connectedShape);
			
			var magnet = option.connectedShape.getDefaultMagnet()
			var cPoint = magnet ? magnet.bounds.center() : option.connectedShape.bounds.midPoint();
			con.dockers.first().setReferencePoint( cPoint );
			con.dockers.last().setDockedShape(newShapeObject);
			con.dockers.last().setReferencePoint(newShapeObject.getDefaultMagnet().bounds.center());		
			
			// The Edge will be added to the canvas and be updated
			canvas.add(con);	
			//con.update();
			
		} 
		
		// Move the new Shape to the position
		if(newShapeObject instanceof ORYX.Core.Edge && option.connectedShape) {

			newShapeObject.dockers.first().setDockedShape(option.connectedShape);
			
			if( option.connectedShape instanceof ORYX.Core.Node ){
				newShapeObject.dockers.first().setReferencePoint(option.connectedShape.getDefaultMagnet().bounds.center());					
				newShapeObject.dockers.last().bounds.centerMoveTo(point);			
			} else {
				newShapeObject.dockers.first().setReferencePoint(option.connectedShape.bounds.midPoint());								
			}
			
			var start = newShapeObject.dockers.first();
        	var end = newShapeObject.dockers.last();
        	
        	if(start.getDockedShape() && end.getDockedShape()) {
        		var startPoint = start.getAbsoluteReferencePoint();
        		var endPoint = end.getAbsoluteReferencePoint();
        		
        		var docker = newShapeObject.createDocker();
        		docker.bounds.centerMoveTo({
        			x: startPoint.x + (endPont.x - startPoint.x) / 2,
        			y: startPoint.y + (endPont.y - startPoint.y) / 2
        		});
        	}

		} else {
			
			var b = newShapeObject.bounds
			if( newShapeObject instanceof ORYX.Core.Node && newShapeObject.dockers.length == 1){
				b = newShapeObject.dockers.first().bounds
			}
			
			b.centerMoveTo(point);
			
			var upL = b.upperLeft();
			b.moveBy( -Math.min(upL.x, 0) , -Math.min(upL.y, 0) )
			
			var lwR = b.lowerRight();
			b.moveBy( -Math.max(lwR.x-canvas.bounds.width(), 0) , -Math.max(lwR.y-canvas.bounds.height(), 0) )
			
		}
		
		// Update the shape
		if (newShapeObject instanceof ORYX.Core.Edge) {
			newShapeObject._update(false);
		}
		
		// And refresh the selection
		if(!(newShapeObject instanceof ORYX.Core.Edge)&&!(option.dontUpdateSelection)) {
			this.setSelection([newShapeObject]);
		}
		
		if(con && con.alignDockers) {
			//con.alignDockers();
		} 
		if(newShapeObject.alignDockers) {
			newShapeObject.alignDockers();
		}

		return newShapeObject;
	},
	
	deleteShape: function(shape) {
		
		if (!shape || !shape.parent){ return }
		
		//remove shape from parent
		// this also removes it from DOM
		shape.parent.remove(shape);
		
		//delete references to outgoing edges
		shape.getOutgoingShapes().each(function(os) {
			var docker = os.getDockers().first();
			if(docker && docker.getDockedShape() == shape) {
				docker.setDockedShape(undefined);
			}
		});
		
		//delete references to incoming edges
		shape.getIncomingShapes().each(function(is) {
			var docker = is.getDockers().last();
			if(docker && docker.getDockedShape() == shape) {
				docker.setDockedShape(undefined);
			}
		});
		
		//delete references of the shape's dockers
		shape.getDockers().each(function(docker) {
			docker.setDockedShape(undefined);
		});
	},
	
	/**
	 * Returns an object with meta data about the model.
	 * Like name, description, ...
	 * 
	 * Empty object with the current backend.
	 * 
	 * @return {Object} Meta data about the model
	 */
	getModelMetaData: function() {
		return this.modelMetaData;
	},

	/* Event-Handler Methods */
	
	/**
	* Helper method to execute an event immediately. The event is not
	* scheduled in the _eventsQueue. Needed to handle Layout-Callbacks.
	*/
	_executeEventImmediately: function(eventObj) {
		if(this.DOMEventListeners.keys().member(eventObj.event.type)) {
			this.DOMEventListeners[eventObj.event.type].each((function(value) {
				value(eventObj.event, eventObj.arg);		
			}).bind(this));
		}
	},

	_executeEvents: function() {
		this._queueRunning = true;
		while(this._eventsQueue.length > 0) {
			var val = this._eventsQueue.shift();
			this._executeEventImmediately(val);
		}
		this._queueRunning = false;
	},
	
	/**
	 * Leitet die Events an die Editor-Spezifischen Event-Methoden weiter
	 * @param {Object} event Event , welches gefeuert wurde
	 * @param {Object} uiObj Target-UiObj
	 */
	handleEvents: function(event, uiObj) {
		
		ORYX.Log.trace("Dispatching event type %0 on %1", event.type, uiObj);

		switch(event.type) {
			case ORYX.CONFIG.EVENT_MOUSEDOWN:
				this._handleMouseDown(event, uiObj);
				break;
			case ORYX.CONFIG.EVENT_MOUSEMOVE:
				this._handleMouseMove(event, uiObj);
				break;
			case ORYX.CONFIG.EVENT_MOUSEUP:
				this._handleMouseUp(event, uiObj);
				break;
			case ORYX.CONFIG.EVENT_MOUSEOVER:
				this._handleMouseHover(event, uiObj);
				break;
			case ORYX.CONFIG.EVENT_MOUSEOUT:
				this._handleMouseOut(event, uiObj);
				break;
		}
		/* Force execution if necessary. Used while handle Layout-Callbacks. */
		if(event.forceExecution) {
			this._executeEventImmediately({event: event, arg: uiObj});
		} else {
			this._eventsQueue.push({event: event, arg: uiObj});
		}
		
		if(!this._queueRunning) {
			this._executeEvents();
		}
		
		// TODO: Make this return whether no listener returned false.
		// So that, when one considers bubbling undesireable, it won't happen.
		return false;
	},

	isValidEvent: function(e){
		try {
			var isInput = ["INPUT", "TEXTAREA"].include(e.target.tagName.toUpperCase());
			var gridHasFocus = e.target.className.include("x-grid3-focus") && !e.target.className.include("x-grid3-focus-canvas");
			return !isInput && !gridHasFocus;
		} catch(e){
			return false;
		}
	},

	catchKeyUpEvents: function(event) {
		if(!this._keyupEnabled) {
			return;
		}
		/* assure we have the current event. */
        if (!event) 
            event = window.event;
        
		// Checks if the event comes from some input field
		if (!this.isValidEvent(event)){
			return;
		}
		
		/* Create key up event type */
		var keyUpEvent = this.createKeyCombEvent(event,	ORYX.CONFIG.KEY_ACTION_UP);
		
		ORYX.Log.debug("Key Event to handle: %0", keyUpEvent);

		/* forward to dispatching. */
		this.handleEvents({type: keyUpEvent, event:event});
	},
	
	/**
	 * Catches all key down events and forward the appropriated event to 
	 * dispatching concerning to the pressed keys.
	 * 
	 * @param {Event} 
	 * 		The key down event to handle
	 */
	catchKeyDownEvents: function(event) {
		if(!this._keydownEnabled) {
			return;
		}
		/* Assure we have the current event. */
        if (!event) 
            event = window.event;
        
		/* Fixed in FF3 */
		// This is a mac-specific fix. The mozilla event object has no knowledge
		// of meta key modifier on osx, however, it is needed for certain
		// shortcuts. This fix adds the metaKey field to the event object, so
		// that all listeners that registered per Oryx plugin facade profit from
		// this. The original bug is filed in
		// https://bugzilla.mozilla.org/show_bug.cgi?id=418334
		//if (this.__currentKey == ORYX.CONFIG.KEY_CODE_META) {
		//	event.appleMetaKey = true;
		//}
		//this.__currentKey = pressedKey;
		
		// Checks if the event comes from some input field
		if (!this.isValidEvent(event)){
			return;
		}
		
		/* Create key up event type */
		var keyDownEvent = this.createKeyCombEvent(event, ORYX.CONFIG.KEY_ACTION_DOWN);
		
		ORYX.Log.debug("Key Event to handle: %0", keyDownEvent);
		
		/* Forward to dispatching. */
		this.handleEvents({type: keyDownEvent,event: event});
	},
	
	/**
	 * Creates the event type name concerning to the pressed keys.
	 * 
	 * @param {Event} keyDownEvent
	 * 		The source keyDownEvent to build up the event name
	 */
	createKeyCombEvent: function(keyEvent, keyAction) {

		/* Get the currently pressed key code. */
        var pressedKey = keyEvent.which || keyEvent.keyCode;
		//this.__currentKey = pressedKey;
		
		/* Event name */
		var eventName = "key.event";
		
		/* Key action */
		if(keyAction) {
			eventName += "." + keyAction;
		}
		
		/* Ctrl or apple meta key is pressed */
		if(keyEvent.ctrlKey || keyEvent.metaKey) {
			eventName += "." + ORYX.CONFIG.META_KEY_META_CTRL;
		}
		
		/* Alt key is pressed */
		if(keyEvent.altKey) {
			eventName += "." + ORYX.CONFIG.META_KEY_ALT;
		}
		
		/* Alt key is pressed */
		if(keyEvent.shiftKey) {
			eventName += "." + ORYX.CONFIG.META_KEY_SHIFT;
		}
		
		/* Return the composed event name */
		return  eventName + "." + pressedKey;
	},

	_handleMouseDown: function(event, uiObj) {
		
		// get canvas.
		var canvas = this.getCanvas();
		// Try to get the focus
		canvas.focus()
	
		// find the shape that is responsible for this element's id.
		var element = event.currentTarget;
		var elementController = uiObj;

		// gather information on selection.
		var currentIsSelectable = (elementController !== null) &&
			(elementController !== undefined) && (elementController.isSelectable);
		var currentIsMovable = (elementController !== null) &&
			(elementController !== undefined) && (elementController.isMovable);
		var modifierKeyPressed = event.shiftKey || event.ctrlKey;
		var noObjectsSelected = this.selection.length === 0;
		var currentIsSelected = this.selection.member(elementController);


		// Rule #1: When there is nothing selected, select the clicked object.
		if(currentIsSelectable && noObjectsSelected) {

			this.setSelection([elementController]);

			ORYX.Log.trace("Rule #1 applied for mouse down on %0", element.id);

		// Rule #3: When at least one element is selected, and there is no
		// control key pressed, and the clicked object is not selected, select
		// the clicked object.
		} else if(currentIsSelectable && !noObjectsSelected &&
			!modifierKeyPressed && !currentIsSelected) {

			this.setSelection([elementController]);

			//var objectType = elementController.readAttributes();
			//alert(objectType[0] + ": " + objectType[1]);

			ORYX.Log.trace("Rule #3 applied for mouse down on %0", element.id);

		// Rule #4: When the control key is pressed, and the current object is
		// not selected, add it to the selection.
		} else if(currentIsSelectable && modifierKeyPressed
			&& !currentIsSelected) {
				
			var newSelection = this.selection.clone();
			newSelection.push(elementController)
			this.setSelection(newSelection)

			ORYX.Log.trace("Rule #4 applied for mouse down on %0", element.id);

		// Rule #6
		} else if(currentIsSelectable && currentIsSelected &&
			modifierKeyPressed) {

			var newSelection = this.selection.clone();
			this.setSelection(newSelection.without(elementController))

			ORYX.Log.trace("Rule #6 applied for mouse down on %0", elementController.id);

		// Rule #5: When there is at least one object selected and no control
		// key pressed, we're dragging.
		/*} else if(currentIsSelectable && !noObjectsSelected
			&& !modifierKeyPressed) {

			if(this.log.isTraceEnabled())
				this.log.trace("Rule #5 applied for mouse down on "+element.id);
*/
		// Rule #2: When clicked on something that is neither
		// selectable nor movable, clear the selection, and return.
		} else if (!currentIsSelectable && !currentIsMovable) {
			
			this.setSelection([]);
			
			ORYX.Log.trace("Rule #2 applied for mouse down on %0", element.id);

			return;

		// Rule #7: When the current object is not selectable but movable,
		// it is probably a control. Leave the selection unchanged but set
		// the movedObject to the current one and enable Drag. Dockers will
		// be processed in the dragDocker plugin.
		} else if(!currentIsSelectable && currentIsMovable && !(elementController instanceof ORYX.Core.Controls.Docker)) {
			
			// TODO: If there is any moveable elements, do this in a plugin
			//ORYX.Core.UIEnableDrag(event, elementController);

			ORYX.Log.trace("Rule #7 applied for mouse down on %0", element.id);
		
		// Rule #8: When the element is selectable and is currently selected and no 
		// modifier key is pressed
		} else if(currentIsSelectable && currentIsSelected &&
			!modifierKeyPressed) {
			
			this._subSelection = this._subSelection != elementController ? elementController : undefined;
						
			this.setSelection(this.selection, this._subSelection);
			
			ORYX.Log.trace("Rule #8 applied for mouse down on %0", element.id);
		}
		
		
		// prevent event from bubbling, return.
		//Event.stop(event);
		return;
	},

	_handleMouseMove: function(event, uiObj) {
		return;
	},

	_handleMouseUp: function(event, uiObj) {
		// get canvas.
		var canvas = this.getCanvas();

		// find the shape that is responsible for this elemement's id.
		var elementController = uiObj;

		//get event position
		var evPos = this.eventCoordinates(event);

		//Event.stop(event);
	},

	_handleMouseHover: function(event, uiObj) {
		return;
	},

	_handleMouseOut: function(event, uiObj) {
		return;
	},

	/**
	 * Calculates the event coordinates to SVG document coordinates.
	 * @param {Event} event
	 * @return {SVGPoint} The event coordinates in the SVG document
	 */
	eventCoordinates: function(event) {

		var canvas = this.getCanvas();

		var svgPoint = canvas.node.ownerSVGElement.createSVGPoint();
		svgPoint.x = event.clientX;
		svgPoint.y = event.clientY;
		
		var additionalIEZoom = 1;
        if (!isNaN(screen.logicalXDPI) && !isNaN(screen.systemXDPI)) {
            var ua = navigator.userAgent;
            if (ua.indexOf('MSIE') >= 0) {
                //IE 10 and below
                var zoom = Math.round((screen.deviceXDPI / screen.logicalXDPI) * 100);
                if (zoom !== 100) {
                    additionalIEZoom = zoom / 100
                }
            }
        }
        
        if (additionalIEZoom !== 1) {
            svgPoint.x = svgPoint.x * additionalIEZoom;
            svgPoint.y = svgPoint.y * additionalIEZoom;
        }
		
		var matrix = canvas.node.getScreenCTM();
		return svgPoint.matrixTransform(matrix.inverse());
	},
	
	eventCoordinatesXY: function(x, y) {

		var canvas = this.getCanvas();

		var svgPoint = canvas.node.ownerSVGElement.createSVGPoint();
		svgPoint.x = x;
		svgPoint.y = y;
		
		var additionalIEZoom = 1;
        if (!isNaN(screen.logicalXDPI) && !isNaN(screen.systemXDPI)) {
            var ua = navigator.userAgent;
            if (ua.indexOf('MSIE') >= 0) {
                //IE 10 and below
                var zoom = Math.round((screen.deviceXDPI / screen.logicalXDPI) * 100);
                if (zoom !== 100) {
                    additionalIEZoom = zoom / 100
                }
            }
        }
        
        if (additionalIEZoom !== 1) {
            svgPoint.x = svgPoint.x * additionalIEZoom;
            svgPoint.y = svgPoint.y * additionalIEZoom;
        }
		
		var matrix = canvas.node.getScreenCTM();
		return svgPoint.matrixTransform(matrix.inverse());
	}
};
ORYX.Editor = Clazz.extend(ORYX.Editor);

/**
 * Creates a new ORYX.Editor instance by fetching a model from given url and passing it to the constructur
 * @param {String} modelUrl The JSON URL of a model.
 * @param {Object} config Editor config passed to the constructur, merged with the response of the request to modelUrl
 */
ORYX.Editor.createByUrl = function(modelUrl){
    new Ajax.Request(modelUrl, {
      method: 'GET',
      onSuccess: function(transport) {
    	var editorConfig = JSON.parse(transport.responseText);
        new ORYX.Editor(editorConfig);
      }.bind(this)
    });
}

// TODO Implement namespace awareness on attribute level.
/**
 * graft() function
 * Originally by Sean M. Burke from interglacial.com, altered for usage with
 * SVG and namespace (xmlns) support. Be sure you understand xmlns before
 * using this funtion, as it creates all grafted elements in the xmlns
 * provided by you and all element's attribures in default xmlns. If you
 * need to graft elements in a certain xmlns and wish to assign attributes
 * in both that and another xmlns, you will need to do stepwise grafting,
 * adding non-default attributes yourself or you'll have to enhance this
 * function. Latter, I would appreciate: martin???apfelfabrik.de
 * @param {Object} namespace The namespace in which
 * 					elements should be grafted.
 * @param {Object} parent The element that should contain the grafted
 * 					structure after the function returned.
 * @param {Object} t the crafting structure.
 * @param {Object} doc the document in which grafting is performed.
 */
ORYX.Editor.graft = function(namespace, parent, t, doc) {

    doc = (doc || (parent && parent.ownerDocument) || document);
    var e;
    if(t === undefined) {
        throw "Can't graft an undefined value";
    } else if(t.constructor == String) {
        e = doc.createTextNode( t );
    } else {
        for(var i = 0; i < t.length; i++) {
            if( i === 0 && t[i].constructor == String ) {
                var snared;
                snared = t[i].match( /^([a-z][a-z0-9]*)\.([^\s\.]+)$/i );
                if( snared ) {
                    e = doc.createElementNS(namespace, snared[1] );
                    e.setAttributeNS(null, 'class', snared[2] );
                    continue;
                }
                snared = t[i].match( /^([a-z][a-z0-9]*)$/i );
                if( snared ) {
                    e = doc.createElementNS(namespace, snared[1] );  // but no class
                    continue;
                }

                // Otherwise:
                e = doc.createElementNS(namespace, "span" );
                e.setAttribute(null, "class", "namelessFromLOL" );
            }

            if( t[i] === undefined ) {
                throw "Can't graft an undefined value in a list!";
            } else if( t[i].constructor == String || t[i].constructor == Array ) {
                this.graft(namespace, e, t[i], doc );
            } else if(  t[i].constructor == Number ) {
                this.graft(namespace, e, t[i].toString(), doc );
            } else if(  t[i].constructor == Object ) {
                // hash's properties => element's attributes
                for(var k in t[i]) { e.setAttributeNS(null, k, t[i][k] ); }
            } else {

			}
        }
    }
	if(parent && parent.appendChild) {
	    parent.appendChild( e );
	} else {

	}
    return e; // return the topmost created node
};

ORYX.Editor.provideId = function() {
	var res = [], hex = '0123456789ABCDEF';

	for (var i = 0; i < 36; i++) res[i] = Math.floor(Math.random()*0x10);

	res[14] = 4;
	res[19] = (res[19] & 0x3) | 0x8;

	for (var i = 0; i < 36; i++) res[i] = hex[res[i]];

	res[8] = res[13] = res[18] = res[23] = '-';

	return "oryx_" + res.join('');
};

/**
 * When working with Ext, conditionally the window needs to be resized. To do
 * so, use this class method. Resize is deferred until 100ms, and all subsequent
 * resizeBugFix calls are ignored until the initially requested resize is
 * performed.
 */
ORYX.Editor.resizeFix = function() {
	if (!ORYX.Editor._resizeFixTimeout) {
		ORYX.Editor._resizeFixTimeout = window.setTimeout(function() {
			window.resizeBy(1,1);
			window.resizeBy(-1,-1);
			ORYX.Editor._resizefixTimeout = null;
		}, 100); 
	}
};

ORYX.Editor.Cookie = {
	
	callbacks:[],
		
	onChange: function( callback, interval ){
	
		this.callbacks.push(callback);
		this.start( interval )
	
	},
	
	start: function( interval ){
		
		if( this.pe ){
			return;
		}
		
		var currentString = document.cookie;
		
		this.pe = new PeriodicalExecuter( function(){
			
			if( currentString != document.cookie ){
				currentString = document.cookie;
				this.callbacks.each(function(callback){ callback(this.getParams()) }.bind(this));
			}
			
		}.bind(this), ( interval || 10000 ) / 1000);	
	},
	
	stop: function(){

		if( this.pe ){
			this.pe.stop();
			this.pe = null;
		}
	},
		
	getParams: function(){
		var res = {};
		
		var p = document.cookie;
		p.split("; ").each(function(param){ res[param.split("=")[0]] = param.split("=")[1];});
		
		return res;
	},	
	
	toString: function(){
		return document.cookie;
	}
};

/**
 * Workaround for SAFARI/Webkit, because
 * when trying to check SVGSVGElement of instanceof there is 
 * raising an error
 * 
 */
ORYX.Editor.SVGClassElementsAreAvailable = true;
ORYX.Editor.setMissingClasses = function() {
	
	try {
		SVGElement;
	} catch(e) {
		ORYX.Editor.SVGClassElementsAreAvailable = false;
		SVGSVGElement 		= document.createElementNS('http://www.w3.org/2000/svg', 'svg').toString();
		SVGGElement 		= document.createElementNS('http://www.w3.org/2000/svg', 'g').toString();
		SVGPathElement 		= document.createElementNS('http://www.w3.org/2000/svg', 'path').toString();
		SVGTextElement 		= document.createElementNS('http://www.w3.org/2000/svg', 'text').toString();
		//SVGMarkerElement 	= document.createElementNS('http://www.w3.org/2000/svg', 'marker').toString();
		SVGRectElement 		= document.createElementNS('http://www.w3.org/2000/svg', 'rect').toString();
		SVGImageElement 	= document.createElementNS('http://www.w3.org/2000/svg', 'image').toString();
		SVGCircleElement 	= document.createElementNS('http://www.w3.org/2000/svg', 'circle').toString();
		SVGEllipseElement 	= document.createElementNS('http://www.w3.org/2000/svg', 'ellipse').toString();
		SVGLineElement	 	= document.createElementNS('http://www.w3.org/2000/svg', 'line').toString();
		SVGPolylineElement 	= document.createElementNS('http://www.w3.org/2000/svg', 'polyline').toString();
		SVGPolygonElement 	= document.createElementNS('http://www.w3.org/2000/svg', 'polygon').toString();
		
	}
	
}
ORYX.Editor.checkClassType = function( classInst, classType ) {
	
	if( ORYX.Editor.SVGClassElementsAreAvailable ){
		return classInst instanceof classType
	} else {
		return classInst == classType
	}
};
/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

/**
 * Init namespaces
 */
if(!ORYX) {var ORYX = {};}
if(!ORYX.Core) {ORYX.Core = {};}


new function(){
	
	ORYX.Core.UIEnableDrag = function(event, uiObj, option) {
	
		this.uiObj = uiObj;
		var upL = uiObj.bounds.upperLeft();
	
		var a = uiObj.node.getScreenCTM();
		this.faktorXY= {x: a.a, y: a.d};
		
		this.scrollNode = uiObj.node.ownerSVGElement.parentNode.parentNode;
		
		this.offSetPosition =  {
			x: Event.pointerX(event) - (upL.x * this.faktorXY.x),
			y: Event.pointerY(event) - (upL.y * this.faktorXY.y)};
	
		this.offsetScroll	= {x:this.scrollNode.scrollLeft,y:this.scrollNode.scrollTop};
			
		this.dragCallback = ORYX.Core.UIDragCallback.bind(this);
		this.disableCallback = ORYX.Core.UIDisableDrag.bind(this);
	
		this.movedCallback = option ? option.movedCallback : undefined;
		this.upCallback = option ? option.upCallback : undefined;
		
		document.documentElement.addEventListener(ORYX.CONFIG.EVENT_MOUSEUP, this.disableCallback, true);
		document.documentElement.addEventListener(ORYX.CONFIG.EVENT_MOUSEMOVE, 	this.dragCallback , false);
	
	};
	
	ORYX.Core.UIDragCallback = function(event) {
	
		var position = {
			x: Event.pointerX(event) - this.offSetPosition.x,
			y: Event.pointerY(event) - this.offSetPosition.y}
	
		position.x 	-= this.offsetScroll.x - this.scrollNode.scrollLeft; 
		position.y 	-= this.offsetScroll.y - this.scrollNode.scrollTop;
	
		position.x /= this.faktorXY.x;
		position.y /= this.faktorXY.y;
	
		this.uiObj.bounds.moveTo(position);
		//this.uiObj.update();
	
		if(this.movedCallback)
			this.movedCallback(event);
		
		//Event.stop(event);
	
	};
	
	ORYX.Core.UIDisableDrag = function(event) {
		document.documentElement.removeEventListener(ORYX.CONFIG.EVENT_MOUSEMOVE, this.dragCallback, false);
		document.documentElement.removeEventListener(ORYX.CONFIG.EVENT_MOUSEUP, this.disableCallback, true);
		
		if(this.upCallback)
			this.upCallback(event);
			
		this.upCallback = undefined;
		this.movedCallback = undefined;		
		
		Event.stop(event);	
	};



	
	/**
	 * Implements a command to move docker by an offset.
	 * 
	 * @class ORYX.Core.MoveDockersCommand
	 * @param {Object} object An object with the docker id as key and docker and offset as object value
	 * 
	 */	
	ORYX.Core.MoveDockersCommand = ORYX.Core.Command.extend({
		construct: function(dockers){
			this.dockers 	= $H(dockers);
			this.edges 		= $H({});
		},
		execute: function(){
			if (this.changes) {
				this.executeAgain();
				return;
			} else {
				this.changes = $H({});
			}
			
			this.dockers.values().each(function(docker){
				var edge = docker.docker.parent;
				if (!edge){ return }
				
				if (!this.changes[edge.getId()]) {
					this.changes[edge.getId()] = {
						edge				: edge,
						oldDockerPositions	: edge.dockers.map(function(r){ return r.bounds.center() })
					}
				}
				docker.docker.bounds.moveBy(docker.offset);
				this.edges[edge.getId()] = edge;
				docker.docker.update();
			}.bind(this));
			this.edges.each(function(edge){
				this.updateEdge(edge.value);
				if (this.changes[edge.value.getId()])
					this.changes[edge.value.getId()].dockerPositions = edge.value.dockers.map(function(r){ return r.bounds.center() })
			}.bind(this));
		},
		updateEdge: function(edge){
			edge._update(true);
			[edge.getOutgoingShapes(), edge.getIncomingShapes()].flatten().invoke("_update", [true])
		},
		executeAgain: function(){
			this.changes.values().each(function(change){
				// Reset the dockers
				this.removeAllDocker(change.edge);
				change.dockerPositions.each(function(pos, i){	
					if (i==0||i==change.dockerPositions.length-1){ return }					
					var docker = change.edge.createDocker(undefined, pos);
					docker.bounds.centerMoveTo(pos);
					docker.update();
				}.bind(this));
				this.updateEdge(change.edge);
			}.bind(this));
		},
		rollback: function(){
			this.changes.values().each(function(change){
				// Reset the dockers
				this.removeAllDocker(change.edge);
				change.oldDockerPositions.each(function(pos, i){	
					if (i==0||i==change.oldDockerPositions.length-1){ return }					
					var docker = change.edge.createDocker(undefined, pos);
					docker.bounds.centerMoveTo(pos);
					docker.update();
				}.bind(this));
				this.updateEdge(change.edge);
			}.bind(this));
		},
		removeAllDocker: function(edge){
			edge.dockers.slice(1, edge.dockers.length-1).each(function(docker){
				edge.removeDocker(docker);
			})
		}
	});
	
}();
/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

/**
 * Init namespaces
 */
if(!ORYX) {var ORYX = {};}
if(!ORYX.Core) {ORYX.Core = {};}

/**
 * @classDescription Base class for Shapes.
 * @extends ORYX.Core.AbstractShape
 */
ORYX.Core.Shape = {

	/**
	 * Constructor
	 */
	construct: function(options, stencil, facade) {
		// call base class constructor
		arguments.callee.$.construct.apply(this, arguments);
		
		this.facade = facade;
		this.dockers = [];
		this.magnets = [];
		
		this._defaultMagnet;
		
		this.incoming = [];
		this.outgoing = [];
		
		this.nodes = [];
		
		this._dockerChangedCallback = this._dockerChanged.bind(this);
		
		//Hash map for all labels. Labels are not treated as children of shapes.
		this._labels = new Hash();
		
		// create SVG node
		this.node = ORYX.Editor.graft("http://www.w3.org/2000/svg",
			null,
			['g', {id:"svg-" + this.resourceId},
				['g', {"class": "stencils"},
					['g', {"class": "me"}],
					['g', {"class": "children", style:"overflow:hidden"}],
					['g', {"class": "edge"}]
				],
				['g', {"class": "controls"},
					['g', {"class": "dockers"}],
					['g', {"class": "magnets"}]				
				]
			]);
	},

	/**
	 * If changed flag is set, refresh method is called.
	 */
	update: function() {
		//if(this.isChanged) {
			//this.layout();
		//}
	},
	
	/**
	 * !!!Not called from any sub class!!!
	 */
	_update: function() {

	},
	
	/**
	 * Calls the super class refresh method
	 *  and updates the svg elements that are referenced by a property.
	 */
	refresh: function() {
		//call base class refresh method
		arguments.callee.$.refresh.apply(this, arguments);
		
		if(this.node.ownerDocument) {
			//adjust SVG to properties' values
			var me = this;
			this.propertiesChanged.each((function(propChanged) {
				if(propChanged.value) {
					var prop = this.properties[propChanged.key];
					var property = this.getStencil().property(propChanged.key);
					if (property != undefined) {
						this.propertiesChanged[propChanged.key] = false;
	
						//handle choice properties
						if(property.type() == ORYX.CONFIG.TYPE_CHOICE) {
							//iterate all references to SVG elements
							property.refToView().each((function(ref) {
								//if property is referencing a label, update the label
								if(ref !== "") {
									var label = this._labels[this.id + ref];
									if (label && property.item(prop)) {
										label.text(property.item(prop).title());
									}
								}
							}).bind(this));
								
							//if the choice's items are referencing SVG elements
							// show the selected and hide all other referenced SVG
							// elements
							var refreshedSvgElements = new Hash();
							property.items().each((function(item) {
								item.refToView().each((function(itemRef) {
									if(itemRef == "") { return; }
									
									var svgElem = this.node.ownerDocument.getElementById(this.id + itemRef);
		
									if(!svgElem) { return; }
									
									
									/* Do not refresh the same svg element multiple times */
									if(!refreshedSvgElements[svgElem.id] || prop == item.value()) {
										svgElem.setAttributeNS(null, 'display', ((prop == item.value()) ? 'inherit' : 'none'));
										refreshedSvgElements[svgElem.id] = svgElem;
									}
									
									// Reload the href if there is an image-tag
									if(ORYX.Editor.checkClassType(svgElem, SVGImageElement)) {
										svgElem.setAttributeNS('http://www.w3.org/1999/xlink', 'href', svgElem.getAttributeNS('http://www.w3.org/1999/xlink', 'href'));
									}
								}).bind(this));
							}).bind(this));
							
						} else { //handle properties that are not of type choice
							//iterate all references to SVG elements
							property.refToView().each((function(ref) {
								//if the property does not reference an SVG element,
								// do nothing

								if(ref === "") { return; }

								var refId = this.id + ref;
								
								if (property.type() === ORYX.CONFIG.TYPE_KISBPM_MULTIINSTANCE)
								{
									if (ref === "multiinstance") {
										
										var svgElemParallel = this.node.ownerDocument.getElementById(this.id + 'parallel');
										if(svgElemParallel) 
										{
											if (prop === 'Parallel')
											{
												svgElemParallel.setAttributeNS(null, 'display', 'inherit');
											}
											else
											{
												svgElemParallel.setAttributeNS(null, 'display', 'none');
											}
										} 
										
										var svgElemSequential = this.node.ownerDocument.getElementById(this.id + 'sequential');
										
										if(svgElemSequential) 
										{
											if (prop === 'Sequential')
											{
												svgElemSequential.setAttributeNS(null, 'display', 'inherit');
											}
											else
											{
												svgElemSequential.setAttributeNS(null, 'display', 'none');
											}
										} 
									}
									return;
									
								} 
								else if (property.type() === "cancelactivity")
								{
									var svgElemFrame = this.node.ownerDocument.getElementById(this.id + 'frame');
									var svgElemFrame2 = this.node.ownerDocument.getElementById(this.id + 'frame2');
									
									if (prop === 'true')
									{
										svgElemFrame.setAttributeNS(null, 'display', 'inherit');
										svgElemFrame2.setAttributeNS(null, 'display', 'inherit');
									}
									else
									{
										svgElemFrame.setAttributeNS(null, 'display', 'none');
										svgElemFrame2.setAttributeNS(null, 'display', 'none');
									}
								}
								
								//get the SVG element
								var svgElem = this.node.ownerDocument.getElementById(refId);
								
								//if the SVG element can not be found
								if(!svgElem || !(svgElem.ownerSVGElement)) { 
									//if the referenced SVG element is a SVGAElement, it cannot
									// be found with getElementById (Firefox bug).
									// this is a work around
									if(property.type() === ORYX.CONFIG.TYPE_URL || property.type() === ORYX.CONFIG.TYPE_DIAGRAM_LINK) {
										var svgElems = this.node.ownerDocument.getElementsByTagNameNS('http://www.w3.org/2000/svg', 'a');
										
										svgElem = $A(svgElems).find(function(elem) {
											return elem.getAttributeNS(null, 'id') === refId;
										});
										
										if(!svgElem) { return; } 
									} else {
										//this.propertiesChanged[propChanged.key] = true;
										return;
									}					
								}
								
								if (property.complexAttributeToView()) {
									var label = this._labels[refId];
									if (label) {
										try {
									    	propJson = prop.evalJSON();
									    	var value = propJson[property.complexAttributeToView()]
									    	label.text(value ? value : prop);
									    } catch (e) {
									    	label.text(prop);
									    }
									}
									
								} else {
									switch (property.type()) {
										case ORYX.CONFIG.TYPE_BOOLEAN:	
										    if (typeof prop == "string")
												prop = prop === "true"
		
											svgElem.setAttributeNS(null, 'display', (!(prop === property.inverseBoolean())) ? 'inherit' : 'none');
											
											break;
										case ORYX.CONFIG.TYPE_COLOR:
											if(property.fill()) {
												if (svgElem.tagName.toLowerCase() === "stop"){
													if (prop){
														
														if (property.lightness() &&  property.lightness() !== 1){
															prop = ORYX.Utils.adjustLightness(prop, property.lightness());
														}
														
														svgElem.setAttributeNS(null, "stop-color", prop);
													
														// Adjust stop color of the others
														if (svgElem.parentNode.tagName.toLowerCase() === "radialgradient"){
															ORYX.Utils.adjustGradient(svgElem.parentNode, svgElem);
														}
													}
													
													// If there is no value, set opaque
													if (svgElem.parentNode.tagName.toLowerCase() === "radialgradient"){
														$A(svgElem.parentNode.getElementsByTagName('stop')).each(function(stop){
															stop.setAttributeNS(null, "stop-opacity", prop ? stop.getAttributeNS(ORYX.CONFIG.NAMESPACE_ORYX, 'default-stop-opacity') || 1 : 0);
														}.bind(this))
													}
												} else {
													svgElem.setAttributeNS(null, 'fill', prop);
												}
											}
											if(property.stroke()) {
												svgElem.setAttributeNS(null, 'stroke', prop);
											}
											break;
										case ORYX.CONFIG.TYPE_STRING:
											var label = this._labels[refId];
											if (label) {
												label.text(prop);
											}
											break;
										case ORYX.CONFIG.TYPE_EXPRESSION:
											var label = this._labels[refId];
											if (label) {
												label.text(prop);
											}
											break;
										case ORYX.CONFIG.TYPE_DATASOURCE:
											var label = this._labels[refId];
											if (label) {
												label.text(prop);
											}
											break;	
										case ORYX.CONFIG.TYPE_INTEGER:
											var label = this._labels[refId];
											if (label) {
												label.text(prop);
											}
											break;
										case ORYX.CONFIG.TYPE_FLOAT:
											if(property.fillOpacity()) {
												svgElem.setAttributeNS(null, 'fill-opacity', prop);
											} 
											if(property.strokeOpacity()) {
												svgElem.setAttributeNS(null, 'stroke-opacity', prop);
											}
											if(!property.fillOpacity() && !property.strokeOpacity()) {
												var label = this._labels[refId];
												if (label) {
													label.text(prop);
												}
											}
											break;
										
										case ORYX.CONFIG.TYPE_FORM_LINK:
	  										if (ref == "pimg") {
	  											var onclickAttr = svgElem.getAttributeNodeNS('', 'onclick');
	  											if(onclickAttr) {
	  												if(prop && ("" + prop).length > 0) {
	  													onclickAttr.textContent = "window.location = '../service/editor?id=" + prop + "_form'";
	  							    	   			} else {
	  							    	   				newFormFacade = this.facade;
	  							    	   				onclickAttr.textContent = "displayNewFormDialog('" + this.resourceId + "');";
	  							    		        }
	  											}
	  										} else if (ref == "linkIndicator") {
	  											if (prop && prop.length > 0) {
	  												svgElem.setAttributeNS(null, 'display', 'inherit');
	  											} else {
	  												svgElem.setAttributeNS(null, 'display', 'none');
	  											}
	  										}
	  										break;
										case ORYX.CONFIG.TYPE_URL:
										case ORYX.CONFIG.TYPE_DIAGRAM_LINK:
											//TODO what is the dafault path?
											var hrefAttr = svgElem.getAttributeNodeNS('http://www.w3.org/1999/xlink', 'xlink:href');
											if(hrefAttr) {
												hrefAttr.textContent = prop;
											} else {
												svgElem.setAttributeNS('http://www.w3.org/1999/xlink', 'xlink:href', prop);
											}	
											break;
										
									}
								}
							}).bind(this));
							
							
						}
					}
					
				}
			}).bind(this));
			
			//update labels
			this._labels.values().each(function(label) {
				label.update();
			});
		}
	},
	
	layout: function() {
		//this.getStencil().layout(this)
		var layoutEvents = this.getStencil().layout()
		if (layoutEvents) {
			layoutEvents.each(function(event) {
				
				// setup additional attributes
				event.shape = this;
				event.forceExecution = true;
				
				// do layouting
				this._delegateEvent(event);
			}.bind(this))
			
		}
	},
	
	/**
	 * Returns an array of Label objects.
	 */
	getLabels: function() {
		return this._labels.values();
	},
	
	/**
	 * Returns the label for a given ref
	 * @return {ORYX.Core.Label} Returns null if there is no label
	 */
	getLabel: function(ref){
		if (!ref){
			return null;
		}
		return (this._labels.find(function(o){
				return o.key.endsWith(ref);
			})||{}).value || null;
	},
	
	/**
	 * Hides all related labels
	 * 
	 */
	hideLabels: function(){
		this.getLabels().invoke("hide");
	},

	/**
	 * Shows all related labels
	 * 
	 */
	showLabels: function(){
		var labels = this.getLabels();
		labels.invoke("show");
		labels.each(function(label) {
				label.update();
		});
	},
	
	setOpacity: function(value, animate){
		
		value = Math.max(Math.min((typeof value == "number" ? value : 1.0), 1.0), 0.0);
				
		if (value !== 1.0){
			value = String(value);
			this.node.setAttributeNS(null, "fill-opacity", value)
			this.node.setAttributeNS(null, "stroke-opacity", value)
		} else {
			this.node.removeAttributeNS(null, "fill-opacity");
			this.node.removeAttributeNS(null, "stroke-opacity");
		}
	},
	
	/**
	 * Returns an array of dockers of this object.
	 */
	getDockers: function() {
		return this.dockers;
	},
	
	getMagnets: function() {
		return this.magnets;
	},
	
	getDefaultMagnet: function() {
		if(this._defaultMagnet) {
			return this._defaultMagnet;
		} else if (this.magnets.length > 0) {
			return this.magnets[0];
		} else {
			return undefined;
		}
	},

	getParentShape: function() {
		return this.parent;
	},
	
	getIncomingShapes: function(iterator) {
		if(iterator) {
			this.incoming.each(iterator);
		}
		return this.incoming;
	},
	
	getIncomingNodes: function(iterator) {
        return this.incoming.select(function(incoming){
            var isNode = (incoming instanceof ORYX.Core.Node);
            if(isNode && iterator) iterator(incoming);
            return isNode;
        });
    },
	
	
	getOutgoingShapes: function(iterator) {
		if(iterator) {
			this.outgoing.each(iterator);
		}
		return this.outgoing;
	},
    
    getOutgoingNodes: function(iterator) {
        return this.outgoing.select(function(out){
            var isNode = (out instanceof ORYX.Core.Node);
            if(isNode && iterator) iterator(out);
            return isNode;
        });
    },
	
	getAllDockedShapes: function(iterator) {
		var result = this.incoming.concat(this.outgoing);
		if(iterator) {
			result.each(iterator);
		}
		return result
	},

	getCanvas: function() {
		if(this.parent instanceof ORYX.Core.Canvas) {
			return this.parent;
		} else if(this.parent instanceof ORYX.Core.Shape) {
			return this.parent.getCanvas();
		} else {
			return undefined;
		}
	},
	
	/**
	 * 
	 * @param {Object} deep
	 * @param {Object} iterator
	 */
	getChildNodes: function(deep, iterator) {
		if(!deep && !iterator) {
			return this.nodes.clone();
		} else {
			var result = [];
			this.nodes.each(function(uiObject) {
				if(!uiObject.isVisible){return}
				if(iterator) {
					iterator(uiObject);
				}
				result.push(uiObject);
				
				if(deep && uiObject instanceof ORYX.Core.Shape) {
					result = result.concat(uiObject.getChildNodes(deep, iterator));
				}
			});
	
			return result;
		}
	},
	
	/**
	 * Overrides the UIObject.add method. Adds uiObject to the correct sub node.
	 * @param {UIObject} uiObject
	 * @param {Number} index
	 */
	add: function(uiObject, index, silent) {
		//parameter has to be an UIObject, but
		// must not be an Edge.
		if(uiObject instanceof ORYX.Core.UIObject 
			&& !(uiObject instanceof ORYX.Core.Edge)) {
			
			if (!(this.children.member(uiObject))) {
				//if uiObject is child of another parent, remove it from that parent.
				if(uiObject.parent) {
					uiObject.parent.remove(uiObject, true);
				}

				//add uiObject to this Shape
				if(index != undefined)
					this.children.splice(index, 0, uiObject);
				else
					this.children.push(uiObject);

				//set parent reference
				uiObject.parent = this;

				//add uiObject.node to this.node depending on the type of uiObject
				var parent;
				if(uiObject instanceof ORYX.Core.Node) {
					parent = this.node.childNodes[0].childNodes[1];
					this.nodes.push(uiObject);
				} else if(uiObject instanceof ORYX.Core.Controls.Control) {
					var ctrls = this.node.childNodes[1];
					if(uiObject instanceof ORYX.Core.Controls.Docker) {
						parent = ctrls.childNodes[0];
						if (this.dockers.length >= 2){
							this.dockers.splice(index!==undefined?Math.min(index, this.dockers.length-1):this.dockers.length-1, 0, uiObject);
						} else {
							this.dockers.push(uiObject);
						}
					} else if(uiObject instanceof ORYX.Core.Controls.Magnet) {
						parent = ctrls.childNodes[1];
						this.magnets.push(uiObject);
					} else {
						parent = ctrls;
					}
				} else {	//UIObject
					parent = this.node;
				}

				if(index != undefined && index < parent.childNodes.length)
					uiObject.node = parent.insertBefore(uiObject.node, parent.childNodes[index]);
				else
					uiObject.node = parent.appendChild(uiObject.node);
					
				this._changed();
				//uiObject.bounds.registerCallback(this._changedCallback);
				
				
				if(this.eventHandlerCallback && silent !== true)
					this.eventHandlerCallback({type:ORYX.CONFIG.EVENT_SHAPEADDED,shape:uiObject})
					
			} else {

				ORYX.Log.warn("add: ORYX.Core.UIObject is already a child of this object.");
			}
		} else {

			ORYX.Log.warn("add: Parameter is not of type ORYX.Core.UIObject.");
		}
	},

	/**
	 * Overrides the UIObject.remove method. Removes uiObject.
	 * @param {UIObject} uiObject
	 */
	remove: function(uiObject, silent) {
		//if uiObject is a child of this object, remove it.
		if (this.children.member(uiObject)) {
			//remove uiObject from children
			var parent = uiObject.parent;

			this.children = this.children.without(uiObject);

			//delete parent reference of uiObject
			uiObject.parent = undefined;

			//delete uiObject.node from this.node
			if(uiObject instanceof ORYX.Core.Shape) {
				if(uiObject instanceof ORYX.Core.Edge) {
					uiObject.removeMarkers();
					uiObject.node = this.node.childNodes[0].childNodes[2].removeChild(uiObject.node);
				} else {
					uiObject.node = this.node.childNodes[0].childNodes[1].removeChild(uiObject.node);
					this.nodes = this.nodes.without(uiObject);
				}
			} else if(uiObject instanceof ORYX.Core.Controls.Control) {
				if (uiObject instanceof ORYX.Core.Controls.Docker) {
					uiObject.node = this.node.childNodes[1].childNodes[0].removeChild(uiObject.node);
					this.dockers = this.dockers.without(uiObject);
				} else if (uiObject instanceof ORYX.Core.Controls.Magnet) {
					uiObject.node = this.node.childNodes[1].childNodes[1].removeChild(uiObject.node);
					this.magnets = this.magnets.without(uiObject);
				} else {
					uiObject.node = this.node.childNodes[1].removeChild(uiObject.node);
				}
			}

			if(this.eventHandlerCallback && silent !== true)
				this.eventHandlerCallback({type: ORYX.CONFIG.EVENT_SHAPEREMOVED, shape: uiObject, parent: parent});
			
			this._changed();
			//uiObject.bounds.unregisterCallback(this._changedCallback);
		} else {

			ORYX.Log.warn("remove: ORYX.Core.UIObject is not a child of this object.");
		}
	},
	
	/**
	 * Calculate the Border Intersection Point between two points
	 * @param {PointA}
	 * @param {PointB}
	 */
	getIntersectionPoint: function() {
			
		var pointAX, pointAY, pointBX, pointBY;
		
		// Get the the two Points	
		switch(arguments.length) {
			case 2:
				pointAX = arguments[0].x;
				pointAY = arguments[0].y;
				pointBX = arguments[1].x;
				pointBY = arguments[1].y;
				break;
			case 4:
				pointAX = arguments[0];
				pointAY = arguments[1];
				pointBX = arguments[2];
				pointBY = arguments[3];
				break;
			default:
				throw "getIntersectionPoints needs two or four arguments";
		}
		
		
		
		// Defined an include and exclude point
		var includePointX, includePointY, excludePointX, excludePointY;

		var bounds = this.absoluteBounds();
		
		if(this.isPointIncluded(pointAX, pointAY, bounds)){
			includePointX = pointAX;
			includePointY = pointAY;
		} else {
			excludePointX = pointAX;
			excludePointY = pointAY;
		}

		if(this.isPointIncluded(pointBX, pointBY, bounds)){
			includePointX = pointBX;
			includePointY = pointBY;
		} else {
			excludePointX = pointBX;
			excludePointY = pointBY;
		}
				
		// If there is no inclue or exclude Shape, than return
		if(!includePointX || !includePointY || !excludePointX || !excludePointY) {
			return undefined;
		}

		var midPointX = 0;
		var midPointY = 0;		
		
		var refPointX, refPointY;
		
		var minDifferent = 1;
		// Get the UpperLeft and LowerRight
		//var ul = bounds.upperLeft();
		//var lr = bounds.lowerRight();
		
		var i = 0;
		
		while(true) {
			// Calculate the midpoint of the current to points	
			var midPointX = Math.min(includePointX, excludePointX) + ((Math.max(includePointX, excludePointX) - Math.min(includePointX, excludePointX)) / 2.0);
			var midPointY = Math.min(includePointY, excludePointY) + ((Math.max(includePointY, excludePointY) - Math.min(includePointY, excludePointY)) / 2.0);
			
			
			// Set the new midpoint by the means of the include of the bounds
			if(this.isPointIncluded(midPointX, midPointY, bounds)){
				includePointX = midPointX;
				includePointY = midPointY;
			} else {
				excludePointX = midPointX;
				excludePointY = midPointY;
			}			
			
			// Calc the length of the line
			var length = Math.sqrt(Math.pow(includePointX - excludePointX, 2) + Math.pow(includePointY - excludePointY, 2))
			// Calc a point one step from the include point
			refPointX = includePointX + ((excludePointX - includePointX) / length),
			refPointY = includePointY + ((excludePointY - includePointY) / length)
					
			
			// If the reference point not in the bounds, break
			if(!this.isPointIncluded(refPointX, refPointY, bounds)) {
				break
			}
							
			
		}

		// Return the last includepoint
		return {x:refPointX , y:refPointY};
	},

   
    
    /**
     * Calculate if the point is inside the Shape
     * @param {PointX}
     * @param {PointY} 
     */
    isPointIncluded: function(){
		return  false
	},

	/**
	 * Returns TRUE if the given node
	 * is a child node of the shapes node
	 * @param {Element} node
	 * @return {Boolean}
	 *
	 */
	containsNode: function(node){
		var me = this.node.firstChild.firstChild;
		while(node){
			if (node == me){
				return true;
			}
			node = node.parentNode;
		}
		return false
	},
    
    /**
     * Calculate if the point is over an special offset area
     * @param {Point}
     */
    isPointOverOffset: function(){
		return  this.isPointIncluded.apply( this , arguments )
	},
		
	_dockerChanged: function() {

	},
		
	/**
	 * Create a Docker for this Edge
	 *
	 */
	createDocker: function(index, position) {
		var docker = new ORYX.Core.Controls.Docker({eventHandlerCallback: this.eventHandlerCallback});
		docker.bounds.registerCallback(this._dockerChangedCallback);
		if (position) {
			docker.bounds.centerMoveTo(position);
		}
		this.add(docker, index);
		
		return docker
	},

	/**
	 * Get the serialized object
	 * return Array with hash-entrees (prefix, name, value)
	 * Following values will given:
	 * 		Bounds
	 * 		Outgoing Shapes
	 * 		Parent
	 */
	serialize: function() {
		var serializedObject = arguments.callee.$.serialize.apply(this);

		// Add the bounds
		serializedObject.push({name: 'bounds', prefix:'oryx', value: this.bounds.serializeForERDF(), type: 'literal'});

		// Add the outgoing shapes
		this.getOutgoingShapes().each((function(followingShape){
			serializedObject.push({name: 'outgoing', prefix:'raziel', value: '#'+ERDF.__stripHashes(followingShape.resourceId), type: 'resource'});			
		}).bind(this));

		// Add the parent shape, if the parent not the canvas
		//if(this.parent instanceof ORYX.Core.Shape){
			serializedObject.push({name: 'parent', prefix:'raziel', value: '#'+ERDF.__stripHashes(this.parent.resourceId), type: 'resource'});	
		//}			
		
		return serializedObject;
	},
		
		
	deserialize: function(serialize, json){
		arguments.callee.$.deserialize.apply(this, arguments);
		
		// Set the Bounds
		var bounds = serialize.find(function(ser){ return 'oryx-bounds' === (ser.prefix+"-"+ser.name) });
		if (bounds) {
			var b = bounds.value.replace(/,/g, " ").split(" ").without("");
			if (this instanceof ORYX.Core.Edge) {
				if (!this.dockers.first().isChanged)
					this.dockers.first().bounds.centerMoveTo(parseFloat(b[0]), parseFloat(b[1]));
				if (!this.dockers.last().isChanged)
					this.dockers.last().bounds.centerMoveTo(parseFloat(b[2]), parseFloat(b[3]));
			} else {
				this.bounds.set(parseFloat(b[0]), parseFloat(b[1]), parseFloat(b[2]), parseFloat(b[3]));
			}
		}
		
		if (json && json.labels instanceof Array){
			json.labels.each(function(slabel){
				var label = this.getLabel(slabel.ref);
				if (label){
					label.deserialize(slabel, this);
				}
			}.bind(this))
		}
	},
	
	toJSON: function(){
		var json = arguments.callee.$.toJSON.apply(this, arguments);
		
		var labels = [], id = this.id;
		this._labels.each(function(obj){
			var slabel = obj.value.serialize();
			if (slabel){
				slabel.ref = obj.key.replace(id, '');
				labels.push(slabel);
			}
		});
		
		if (labels.length > 0){
			json.labels = labels;
		}
		return json;
	},

		
	/**
	 * Private methods.
	 */

	/**
	 * Child classes have to overwrite this method for initializing a loaded
	 * SVG representation.
	 * @param {SVGDocument} svgDocument
	 */
	_init: function(svgDocument) {
		//adjust ids
		this._adjustIds(svgDocument, 0);
	},

	_adjustIds: function(element, idIndex) {
		if(element instanceof Element) {
			var eid = element.getAttributeNS(null, 'id');
			if(eid && eid !== "") {
				element.setAttributeNS(null, 'id', this.id + eid);
			} else {
				element.setAttributeNS(null, 'id', this.id + "_" + this.id + "_" + idIndex);
				idIndex++;
			}
			
			// Replace URL in fill attribute
			var fill = element.getAttributeNS(null, 'fill');
			if (fill&&fill.include("url(#")){
				fill = fill.replace(/url\(#/g, 'url(#'+this.id);
				element.setAttributeNS(null, 'fill', fill);
			}
			
			if(element.hasChildNodes()) {
				for(var i = 0; i < element.childNodes.length; i++) {
					idIndex = this._adjustIds(element.childNodes[i], idIndex);
				}
			}
		}
		return idIndex;
	},

	toString: function() { return "ORYX.Core.Shape " + this.getId() }
};
ORYX.Core.Shape = ORYX.Core.AbstractShape.extend(ORYX.Core.Shape);/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

/**
 * Init namespaces
 */
if(!ORYX) {var ORYX = {};}
if(!ORYX.Core) {ORYX.Core = {};}
if(!ORYX.Core.Controls) {ORYX.Core.Controls = {};}


/**
 * @classDescription Abstract base class for all Controls.
 */
ORYX.Core.Controls.Control = ORYX.Core.UIObject.extend({
	
	toString: function() { return "Control " + this.id; }
 });/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

/**
 * Init namespaces
 */
if(!ORYX) {var ORYX = {};}
if(!ORYX.Core) {ORYX.Core = {};}
if(!ORYX.Core.Controls) {ORYX.Core.Controls = {};}


/**
 * @classDescription Represents a movable docker that can be bound to a shape. Dockers are used
 * for positioning shape objects.
 * @extends {Control}
 * 
 * TODO absoluteXY und absoluteCenterXY von einem Docker liefern falsche Werte!!!
 */
ORYX.Core.Controls.Docker = ORYX.Core.Controls.Control.extend({
	/**
	 * Constructor
	 */
	construct: function() {
		arguments.callee.$.construct.apply(this, arguments);
		
		this.isMovable = true;				// Enables movability
		this.bounds.set(0, 0, 16, 16);		// Set the bounds
		this.referencePoint = undefined;		// Refrenzpoint 
		this._dockedShapeBounds = undefined;		
		this._dockedShape = undefined;
		this._oldRefPoint1 = undefined;
		this._oldRefPoint2 = undefined;
		
		//this.anchors = [];
		this.anchorLeft;
		this.anchorRight;
		this.anchorTop;
		this.anchorBottom;

		this.node = ORYX.Editor.graft("http://www.w3.org/2000/svg",
			null,
			['g']);

		// The DockerNode reprasentation
		this._dockerNode = ORYX.Editor.graft("http://www.w3.org/2000/svg",
			this.node,
			['g', {"pointer-events":"all"},
					['circle', {cx:"8", cy:"8", r:"8", stroke:"none", fill:"none"}],
					['circle', {cx:"8", cy:"8", r:"3", stroke:"black", fill:"red", "stroke-width":"1"}]
				]);
			
		// The ReferenzNode reprasentation	
		this._referencePointNode = ORYX.Editor.graft("http://www.w3.org/2000/svg",
			this.node,	
			['g', {"pointer-events":"none"},
				['circle', {cx: this.bounds.upperLeft().x, cy: this.bounds.upperLeft().y, r: 3, fill:"red", "fill-opacity":0.4}]]);

		// Hide the Docker
		this.hide();
		
		//Add to the EventHandler
		this.addEventHandlers(this._dockerNode);

		// Buffer the Update Callback for un-/register on Event-Handler 
		this._updateCallback = this._changed.bind(this);
	},
	
	update: function() {
		// If there have an DockedShape	
		if(this._dockedShape) {
			if(this._dockedShapeBounds && this._dockedShape instanceof ORYX.Core.Node) {
				// Calc the delta of width and height of the lastBounds and the current Bounds
				var dswidth = this._dockedShapeBounds.width();
				var dsheight = this._dockedShapeBounds.height();
				if(!dswidth)
					dswidth = 1;
				if(!dsheight)
					dsheight = 1;	
				var widthDelta = 	this._dockedShape.bounds.width() 	/ dswidth;
				var heightDelta = 	this._dockedShape.bounds.height() 	/ dsheight;
				
				// If there is an different
				if(widthDelta !== 1.0 || heightDelta !== 1.0) {
					// Set the delta
					this.referencePoint.x *= widthDelta;
					this.referencePoint.y *= heightDelta;
				}
	
				// Clone these bounds
				this._dockedShapeBounds = this._dockedShape.bounds.clone();				
			}
			
			// Get the first and the last Docker of the parent Shape
			var dockerIndex = this.parent.dockers.indexOf(this)
			var dock1 = this;
			var dock2 = this.parent.dockers.length > 1 ? 
							(dockerIndex === 0?							// If there is the first element
							 	this.parent.dockers[dockerIndex + 1]:	// then take the next docker
								this.parent.dockers[dockerIndex - 1]):  // if not, then take the docker before
							undefined;
			
			// Calculate the first absolute Refenzpoint 
			var absoluteReferenzPoint1 = dock1.getDockedShape() ? 
				dock1.getAbsoluteReferencePoint() : 
				dock1.bounds.center();

			// Calculate the last absolute Refenzpoint 
			var absoluteReferenzPoint2 = dock2 && dock2.getDockedShape() ? 
				dock2.getAbsoluteReferencePoint() : 
				dock2 ? 
					dock2.bounds.center() :
					undefined;

			// If there is no last absolute Referenzpoint		
			if(!absoluteReferenzPoint2) {
				// Calculate from the middle of the DockedShape
				var center = this._dockedShape.absoluteCenterXY();
				var minDimension = this._dockedShape.bounds.width() * this._dockedShape.bounds.height(); 
				absoluteReferenzPoint2 = {
					x: absoluteReferenzPoint1.x + (center.x - absoluteReferenzPoint1.x) * -minDimension,
					y: absoluteReferenzPoint1.y + (center.y - absoluteReferenzPoint1.y) * -minDimension
				}
			}
			
			var newPoint = undefined;
			
			/*if (!this._oldRefPoint1 || !this._oldRefPoint2 ||
				absoluteReferenzPoint1.x !== this._oldRefPoint1.x ||
				absoluteReferenzPoint1.y !== this._oldRefPoint1.y ||
				absoluteReferenzPoint2.x !== this._oldRefPoint2.x ||
				absoluteReferenzPoint2.y !== this._oldRefPoint2.y) {*/
				
				// Get the new point for the Docker, calucalted by the intersection point of the Shape and the two points
				newPoint = this._dockedShape.getIntersectionPoint(absoluteReferenzPoint1, absoluteReferenzPoint2);
				
				// If there is new point, take the referencepoint as the new point
				if(!newPoint) {
					newPoint = this.getAbsoluteReferencePoint();
				}
				
				if(this.parent && this.parent.parent) {
					var grandParentPos = this.parent.parent.absoluteXY();
					newPoint.x -= grandParentPos.x;
					newPoint.y -= grandParentPos.y;
				}
				
				// Set the bounds to the new point
				this.bounds.centerMoveTo(newPoint)
			
				this._oldRefPoint1 = absoluteReferenzPoint1;
				this._oldRefPoint2 = absoluteReferenzPoint2;
			} 
			/*else {
				newPoint = this.bounds.center();
			}*/
			
			
	//	}
		
		// Call the super class
		arguments.callee.$.update.apply(this, arguments);
	},

	/**
	 * Calls the super class refresh method and updates the view of the docker.
	 */
	refresh: function() {
		arguments.callee.$.refresh.apply(this, arguments);
		
		// Refresh the dockers node
		var p = this.bounds.upperLeft();
		this._dockerNode.setAttributeNS(null, 'transform','translate(' + p.x + ', ' + p.y + ')');
		
		// Refresh the referencepoints node
		p = Object.clone(this.referencePoint);
		
		if(p && this._dockedShape){
			var upL 
			if(this.parent instanceof ORYX.Core.Edge) {
				upL = this._dockedShape.absoluteXY();
			} else {
				upL = this._dockedShape.bounds.upperLeft();
			}
			p.x += upL.x;
			p.y += upL.y;
		} else {
			p = this.bounds.center();
		}			

		this._referencePointNode.setAttributeNS(null, 'transform','translate(' + p.x + ', ' + p.y + ')');
	},

	/**
	 * Set the reference point
	 * @param {Object} point
	 */	
	setReferencePoint: function(point) {
		// Set the referencepoint
		if(this.referencePoint !== point &&
			(!this.referencePoint || 
			!point ||
			this.referencePoint.x !== point.x || 
			this.referencePoint.y !== point.y)) {
				
			this.referencePoint = point;
			this._changed();			
		}

		
		// Update directly, because the referencepoint has no influence of the bounds
		//this.refresh();
	},
	
	/**
	 * Get the absolute referencepoint
	 */
	getAbsoluteReferencePoint: function() {
		if(!this.referencePoint || !this._dockedShape) {
			return undefined;
		} else {
			var absUL = this._dockedShape.absoluteXY();
			return {	
						x: this.referencePoint.x + absUL.x,
						y: this.referencePoint.y + absUL.y
					}
		}
	},	
	
	/**
	 * Set the docked Shape from the docker
	 * @param {Object} shape
	 */
	setDockedShape: function(shape) {

		// If there is an old docked Shape
		if(this._dockedShape) {
			this._dockedShape.bounds.unregisterCallback(this._updateCallback)
			
			// Delete the Shapes from the incoming and outgoing array
			// If this Docker the incoming of the Shape
			if(this === this.parent.dockers.first()) {
				
				this.parent.incoming = this.parent.incoming.without(this._dockedShape);
				this._dockedShape.outgoing = this._dockedShape.outgoing.without(this.parent);
			
			// If this Docker the outgoing of the Shape	
			} else if (this === this.parent.dockers.last()){
	
				this.parent.outgoing = this.parent.outgoing.without(this._dockedShape);
				this._dockedShape.incoming = this._dockedShape.incoming.without(this.parent);
							
			}
			
		}

		
		// Set the new Shape
		this._dockedShape = shape;
		this._dockedShapeBounds = undefined;
		var referencePoint = undefined;
		
		// If there is an Shape, register the updateCallback if there are changes in the shape bounds
		if(this._dockedShape) {
			
			// Add the Shapes to the incoming and outgoing array
			// If this Docker the incoming of the Shape
			if(this === this.parent.dockers.first()) {
				
				this.parent.incoming.push(shape);
				shape.outgoing.push(this.parent);
			
			// If this Docker the outgoing of the Shape	
			} else if (this === this.parent.dockers.last()){
	
				this.parent.outgoing.push(shape);
				shape.incoming.push(this.parent);
							
			}
			
			// Get the bounds and set the new referencepoint
			var bounds = this.bounds;
			var absUL = shape.absoluteXY();
			
			/*if(shape.parent){
				var b = shape.parent.bounds.upperLeft();
				absUL.x -= b.x;
				absUL.y -= b.y;
			}*/
			
			referencePoint = {
				x: bounds.center().x - absUL.x,
				y: bounds.center().y - absUL.y
			}	
						
			this._dockedShapeBounds = this._dockedShape.bounds.clone();
			
			this._dockedShape.bounds.registerCallback(this._updateCallback);
			
			// Set the color of the docker as docked
			this.setDockerColor(ORYX.CONFIG.DOCKER_DOCKED_COLOR);				
		} else {
			// Set the color of the docker as undocked
			this.setDockerColor(ORYX.CONFIG.DOCKER_UNDOCKED_COLOR);
		}

		// Set the referencepoint
		this.setReferencePoint(referencePoint);
		this._changed();
		//this.update();
	},
	
	/**
	 * Get the docked Shape
	 */
	getDockedShape: function() {
		return this._dockedShape;
	},

	/**
	 * Returns TRUE if the docker has a docked shape
	 */
	isDocked: function() {
		return !!this._dockedShape;
	},
		
	/**
	 * Set the Color of the Docker
	 * @param {Object} color
	 */
	setDockerColor: function(color) {
		this._dockerNode.lastChild.setAttributeNS(null, "fill", color);
	},
	
	preventHiding: function(prevent){
		this._preventHiding = Math.max(0, (this._preventHiding||0) + (prevent ? 1 : -1));
	},
	
	/**
	 * Hides this UIObject and all its children.
	 */
	hide: function() {
		if (this._preventHiding){
			return false;
		}
		
		// Hide docker and reference point
		this.node.setAttributeNS(null, 'visibility', 'hidden');
		this._referencePointNode.setAttributeNS(null, 'visibility', 'hidden');
		
		this.children.each(function(uiObj) {
			uiObj.hide();	
		});				
	},
	
	/**
	 * Enables visibility of this UIObject and all its children.
	 */
	show: function() {
		// Show docker
		this.node.setAttributeNS(null, 'visibility', 'visible');
		
		// Hide reference point if the connected shape is an edge
		if (this.getDockedShape() instanceof ORYX.Core.Edge){
			this._referencePointNode.setAttributeNS(null, 'visibility', 'hidden');
		} else {
			this._referencePointNode.setAttributeNS(null, 'visibility', 'visible');
		}
		
		this.children.each(function(uiObj) {
			uiObj.show();	
		});		
	},
	
	toString: function() { return "Docker " + this.id }
});/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

/**
 * Init namespaces
 */
if(!ORYX) {var ORYX = {};}
if(!ORYX.Core) {ORYX.Core = {};}
if(!ORYX.Core.Controls) {ORYX.Core.Controls = {};}


/**
 * @classDescription Represents a magnet that is part of another shape and can
 * be attached to dockers. Magnets are used for linking edge objects
 * to other Shape objects.
 * @extends {Control}
 */
ORYX.Core.Controls.Magnet = ORYX.Core.Controls.Control.extend({
		
	/**
	 * Constructor
	 */
	construct: function() {
		arguments.callee.$.construct.apply(this, arguments);
		
		//this.anchors = [];
		this.anchorLeft;
		this.anchorRight;
		this.anchorTop;
		this.anchorBottom;
		
		this.bounds.set(0, 0, 16, 16);
		
		//graft magnet's root node into owner's control group.
		this.node = ORYX.Editor.graft("http://www.w3.org/2000/svg",
			null,
			['g', {"pointer-events":"all"},
					['circle', {cx:"8", cy:"8", r:"4", stroke:"none", fill:"red", "fill-opacity":"0.3"}],
				]);
			
		this.hide();
	},
	
	update: function() {
		arguments.callee.$.update.apply(this, arguments);
		
		//this.isChanged = true;
	},
	
	_update: function() {		
		arguments.callee.$.update.apply(this, arguments);
		
		//this.isChanged = true;
	},
	
	refresh: function() {
		arguments.callee.$.refresh.apply(this, arguments);

		var p = this.bounds.upperLeft();
		/*if(this.parent) {
			var parentPos = this.parent.bounds.upperLeft();
			p.x += parentPos.x;
			p.y += parentPos.y;
		}*/
		
		this.node.setAttributeNS(null, 'transform','translate(' + p.x + ', ' + p.y + ')');
	},
	
	show: function() {
		//this.refresh();
		arguments.callee.$.show.apply(this, arguments);
	},
	
	toString: function() {
		return "Magnet " + this.id;
	}
});
/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

/**
 * Init namespaces
 */
if (!ORYX) {
    var ORYX = {};
}
if (!ORYX.Core) {
    ORYX.Core = {};
}

/**
 * @classDescription Abstract base class for all Nodes.
 * @extends ORYX.Core.Shape
 */
ORYX.Core.Node = {

    /**
     * Constructor
     * @param options {Object} A container for arguments.
     * @param stencil {Stencil}
     */
    construct: function(options, stencil, facade){
        arguments.callee.$.construct.apply(this, arguments);
        
        this.isSelectable = true;
        this.isMovable = true;
		this._dockerUpdated = false;
		this.facade = facade;
        
        this._oldBounds = new ORYX.Core.Bounds(); //init bounds with undefined values
        this._svgShapes = []; //array of all SVGShape objects of
        // SVG representation
        
        //TODO vielleicht in shape verschieben?
        this.minimumSize = undefined; // {width:..., height:...}
        this.maximumSize = undefined;
        
        //TODO vielleicht in shape oder uiobject verschieben?
        // vielleicht sogar isResizable ersetzen?
        this.isHorizontallyResizable = false;
        this.isVerticallyResizable = false;
        
        this.dataId = undefined;
        
        this._init(this._stencil.view());
        this.forcedHeight = -1;
    },
        
    /**
     * This method checks whether the shape is resized correctly and calls the
     * super class update method.
     */
    _update: function(){
		
		this.dockers.invoke("update");
		if (this.isChanged) {

			var bounds = this.bounds;
            var oldBounds = this._oldBounds;
						
			if (this.isResized) {
			
				var widthDelta = bounds.width() / oldBounds.width();
				var heightDelta = bounds.height() / oldBounds.height();
				
				//iterate over all relevant svg elements and resize them
				this._svgShapes.each(function(svgShape){
					//adjust width
					if (svgShape.isHorizontallyResizable) {
						svgShape.width = svgShape.oldWidth * widthDelta;
					}
					//adjust height
					if (svgShape.isVerticallyResizable) {
						svgShape.height = svgShape.oldHeight * heightDelta;
					}
					
					//check, if anchors are set
					var anchorOffset;
					var leftIncluded = svgShape.anchorLeft;
					var rightIncluded = svgShape.anchorRight;
					
					if (rightIncluded) {
						anchorOffset = oldBounds.width() - (svgShape.oldX + svgShape.oldWidth);
						if (leftIncluded) {
							svgShape.width = bounds.width() - svgShape.x - anchorOffset;
						}
						else {
							svgShape.x = bounds.width() - (anchorOffset + svgShape.width);
						}
					}
					else 
						if (!leftIncluded) {
							svgShape.x = widthDelta * svgShape.oldX;
							if (!svgShape.isHorizontallyResizable) {
								svgShape.x = svgShape.x + svgShape.width * widthDelta / 2 - svgShape.width / 2;
							}
						}
					
					var topIncluded = svgShape.anchorTop;
					var bottomIncluded = svgShape.anchorBottom;
					
					if (bottomIncluded) {
						anchorOffset = oldBounds.height() - (svgShape.oldY + svgShape.oldHeight);
						if (topIncluded) {
							svgShape.height = bounds.height() - svgShape.y - anchorOffset;
						}
						else {
							// Hack for choreography task layouting
							if (!svgShape._isYLocked) {
								svgShape.y = bounds.height() - (anchorOffset + svgShape.height);
							}
						}
					}
					else 
						if (!topIncluded) {
							svgShape.y = heightDelta * svgShape.oldY;
							if (!svgShape.isVerticallyResizable) {
								svgShape.y = svgShape.y + svgShape.height * heightDelta / 2 - svgShape.height / 2;
							}
						}
				});
				
				//check, if the current bounds is unallowed horizontally or vertically resized
				var p = {
					x: 0,
					y: 0
				};
				if (!this.isHorizontallyResizable && bounds.width() !== oldBounds.width()) {
					p.x = oldBounds.width() - bounds.width();
				}
				if (!this.isVerticallyResizable && bounds.height() !== oldBounds.height()) {
					p.y = oldBounds.height() - bounds.height();
				}
				if (p.x !== 0 || p.y !== 0) {
					bounds.extend(p);
				}
				
				//check, if the current bounds are between maximum and minimum bounds
				p = {
					x: 0,
					y: 0
				};
				var widthDifference, heightDifference;
				if (this.minimumSize) {
				
					ORYX.Log.debug("Shape (%0)'s min size: (%1x%2)", this, this.minimumSize.width, this.minimumSize.height);
					widthDifference = this.minimumSize.width - bounds.width();
					if (widthDifference > 0) {
						p.x += widthDifference;
					}
					heightDifference = this.minimumSize.height - bounds.height();
					if (heightDifference > 0) {
						p.y += heightDifference;
					}
				}
				if (this.maximumSize) {
				
					ORYX.Log.debug("Shape (%0)'s max size: (%1x%2)", this, this.maximumSize.width, this.maximumSize.height);
					widthDifference = bounds.width() - this.maximumSize.width;
					if (widthDifference > 0) {
						p.x -= widthDifference;
					}
					heightDifference = bounds.height() - this.maximumSize.height;
					if (heightDifference > 0) {
						p.y -= heightDifference;
					}
				}
				if (p.x !== 0 || p.y !== 0) {
					bounds.extend(p);
				}
				
				//update magnets
				
				var widthDelta = bounds.width() / oldBounds.width();
				var heightDelta = bounds.height() / oldBounds.height();
				
				var leftIncluded, rightIncluded, topIncluded, bottomIncluded, center, newX, newY;
				
				this.magnets.each(function(magnet){
					leftIncluded = magnet.anchorLeft;
					rightIncluded = magnet.anchorRight;
					topIncluded = magnet.anchorTop;
					bottomIncluded = magnet.anchorBottom;
					
					center = magnet.bounds.center();
					
					if (leftIncluded) {
						newX = center.x;
					}
					else 
						if (rightIncluded) {
							newX = bounds.width() - (oldBounds.width() - center.x)
						}
						else {
							newX = center.x * widthDelta;
						}
					
					if (topIncluded) {
						newY = center.y;
					}
					else 
						if (bottomIncluded) {
							newY = bounds.height() - (oldBounds.height() - center.y);
						}
						else {
							newY = center.y * heightDelta;
						}
					
					if (center.x !== newX || center.y !== newY) {
						magnet.bounds.centerMoveTo(newX, newY);
					}
				});
				
				//set new position of labels
				this.getLabels().each(function(label){
					// Set the position dependings on it anchor
					if (!label.isAnchorLeft()) {
						if (label.isAnchorRight()) {
							label.setX(bounds.width() - (oldBounds.width() - label.oldX))
						} else {
							label.setX((label.position?label.position.x:label.x) * widthDelta);
						}
					}
					if (!label.isAnchorTop()) {
						if (label.isAnchorBottom()) {
							label.setY(bounds.height() - (oldBounds.height() - label.oldY));
						} else {
							label.setY((label.position?label.position.y:label.y) * heightDelta);
						}
					}
					
					// If there is an position,
					// set the origin position as well
					if (label.position){
						if (!label.isOriginAnchorLeft()) {
							if (label.isOriginAnchorRight()) {
								label.setOriginX(bounds.width() - (oldBounds.width() - label.oldX))
							} else {
								label.setOriginX(label.x * widthDelta);
							}
						}
						if (!label.isOriginAnchorTop()) {
							if (label.isOriginAnchorBottom()) {
								label.setOriginY(bounds.height() - (oldBounds.height() - label.oldY));
							} else {
								label.setOriginY(label.y * heightDelta);
							}
						}
					}
				});
				
				//update docker
				var docker = this.dockers[0];
				if (docker) {
					docker.bounds.unregisterCallback(this._dockerChangedCallback);
					if (!this._dockerUpdated) {
						docker.bounds.centerMoveTo(this.bounds.center());
						this._dockerUpdated = false;
					}
					
					docker.update();
					docker.bounds.registerCallback(this._dockerChangedCallback);
				}
				this.isResized = false;
			}
            
            this.refresh();
			
			this.isChanged = false;
			
			this._oldBounds = this.bounds.clone();
        }
		
		this.children.each(function(value) {
			if(!(value instanceof ORYX.Core.Controls.Docker)) {
				value._update();
			}
		});
		
		if (this.dockers.length > 0&&!this.dockers.first().getDockedShape()) {
			this.dockers.each(function(docker){
				docker.bounds.centerMoveTo(this.bounds.center())
			}.bind(this))
		}
		
		/*this.incoming.each((function(edge) {
			if(!(this.dockers[0] && this.dockers[0].getDockedShape() instanceof ORYX.Core.Node))
				edge._update(true);
		}).bind(this));
		
		this.outgoing.each((function(edge) {
			if(!(this.dockers[0] && this.dockers[0].getDockedShape() instanceof ORYX.Core.Node))
				edge._update(true);
		}).bind(this)); */
    },
    
    /**
     * This method repositions and resizes the SVG representation
     * of the shape.
     */
    refresh: function(){
        arguments.callee.$.refresh.apply(this, arguments);
        
        /** Movement */
        var x = this.bounds.upperLeft().x;
        var y = this.bounds.upperLeft().y;
        
		// Move owner element
		this.node.firstChild.setAttributeNS(null, "transform", "translate(" + x + ", " + y + ")");
		// Move magnets
		this.node.childNodes[1].childNodes[1].setAttributeNS(null, "transform", "translate(" + x + ", " + y + ")");
        
        /** Resize */
        
        //iterate over all relevant svg elements and update them
        this._svgShapes.each(function(svgShape){
            svgShape.update();
        });
    },
    
    _dockerChanged: function(){
		var docker = this.dockers[0];
        
        //set the bounds of the the association
        this.bounds.centerMoveTo(docker.bounds.center());
        
		this._dockerUpdated = true;
        //this._update(true);
    },
    
    /**
     * This method traverses a tree of SVGElements and returns
     * all SVGShape objects. For each basic shape or path element
     * a SVGShape object is initialized.
     *
     * @param svgNode {SVGElement}
     * @return {Array} Array of SVGShape objects
     */
    _initSVGShapes: function(svgNode){
        var svgShapes = [];
        try {
            var svgShape = new ORYX.Core.SVG.SVGShape(svgNode);
            svgShapes.push(svgShape);
        } 
        catch (e) {
            //do nothing
        }
        
        if (svgNode.hasChildNodes()) {
            for (var i = 0; i < svgNode.childNodes.length; i++) {
                svgShapes = svgShapes.concat(this._initSVGShapes(svgNode.childNodes[i]));
            }
        }
        
        return svgShapes;
    },
    
    /**
     * Calculate if the point is inside the Shape
     * @param {PointX}
     * @param {PointY} 
     * @param {absoluteBounds} optional: for performance
     */
    isPointIncluded: function(pointX, pointY, absoluteBounds){
        // If there is an arguments with the absoluteBounds
        var absBounds = absoluteBounds && absoluteBounds instanceof ORYX.Core.Bounds ? absoluteBounds : this.absoluteBounds();
        
        if (!absBounds.isIncluded(pointX, pointY)) {
			return false;
		} else {
			
		}
			
        
        //point = Object.clone(point);
        var ul = absBounds.upperLeft();
        var x = pointX - ul.x;
        var y = pointY - ul.y;		
	
		var i=0;
		do {
			var isPointIncluded = this._svgShapes[i++].isPointIncluded( x, y );
		} while( !isPointIncluded && i < this._svgShapes.length)
		
		return isPointIncluded;

        /*return this._svgShapes.any(function(svgShape){
            return svgShape.isPointIncluded(point);
        });*/
    },
 
    
    /**
     * Calculate if the point is over an special offset area
     * @param {Point}
     */
    isPointOverOffset: function( pointX, pointY ){       
		var isOverEl = arguments.callee.$.isPointOverOffset.apply( this , arguments );
		
		if (isOverEl) {
						
	        // If there is an arguments with the absoluteBounds
	        var absBounds = this.absoluteBounds();
	        absBounds.widen( - ORYX.CONFIG.BORDER_OFFSET );
			
	        if ( !absBounds.isIncluded( pointX, pointY )) {
	            return true;
	        }		
		}
		
		return false;
		
	},
	   
    serialize: function(){
        var result = arguments.callee.$.serialize.apply(this);
        
        // Add the docker's bounds
        // nodes only have at most one docker!
        this.dockers.each((function(docker){
			if (docker.getDockedShape()) {
				var center = docker.referencePoint;
				center = center ? center : docker.bounds.center();
				result.push({
					name: 'docker',
					prefix: 'oryx',
					value: $H(center).values().join(','),
					type: 'literal'
				});
			}
        }).bind(this));
        
        // Get the spezific serialized object from the stencil
        try {
            //result = this.getStencil().serialize(this, result);

			var serializeEvent = this.getStencil().serialize();
			
			/*
			 * call serialize callback by reference, result should be found
			 * in serializeEvent.result
			 */
			if(serializeEvent.type) {
				serializeEvent.shape = this;
				serializeEvent.data = result;
				serializeEvent.result = undefined;
				serializeEvent.forceExecution = true;
				
				this._delegateEvent(serializeEvent);
				
				if(serializeEvent.result) {
					result = serializeEvent.result;
				}
			}
        } 
        catch (e) {
        }
        return result;
    },
    
    deserialize: function(data){
    	arguments.callee.$.deserialize.apply(this, arguments);
		
	    try {
            //data = this.getStencil().deserialize(this, data);

			var deserializeEvent = this.getStencil().deserialize();
			
			/*
			 * call serialize callback by reference, result should be found
			 * in serializeEventInfo.result
			 */
			if(deserializeEvent.type) {
				deserializeEvent.shape = this;
				deserializeEvent.data = data;
				deserializeEvent.result = undefined;
				deserializeEvent.forceExecution = true;
				
				this._delegateEvent(deserializeEvent);
				if(deserializeEvent.result) {
					data = deserializeEvent.result;
				}
			}
        } 
        catch (e) {
        }
		
		// Set the outgoing shapes
		var outgoing = data.findAll(function(ser){ return (ser.prefix+"-"+ser.name) == 'raziel-outgoing'});
		outgoing.each((function(obj){
			// TODO: Look at Canvas
			if(!this.parent) {return};
								
			// Set outgoing Shape
			var next = this.getCanvas().getChildShapeByResourceId(obj.value);
																	
			if(next){
				if(next instanceof ORYX.Core.Edge) {
					//Set the first docker of the next shape
					next.dockers.first().setDockedShape(this);
					next.dockers.first().setReferencePoint(next.dockers.first().bounds.center());
				} else if(next.dockers.length > 0) { //next is a node and next has a docker
					next.dockers.first().setDockedShape(this);
					//next.dockers.first().setReferencePoint({x: this.bounds.width() / 2.0, y: this.bounds.height() / 2.0});
				}
			}	
			
		}).bind(this));
        
        if (this.dockers.length === 1) {
            var dockerPos;
            dockerPos = data.find(function(entry){
                return (entry.prefix + "-" + entry.name === "oryx-dockers");
            });
            
            if (dockerPos) {
                var points = dockerPos.value.replace(/,/g, " ").split(" ").without("").without("#");
				if (points.length === 2 && this.dockers[0].getDockedShape()) {
                    this.dockers[0].setReferencePoint({
                        x: parseFloat(points[0]),
                        y: parseFloat(points[1])
                    });
                }
                else {
                    this.dockers[0].bounds.centerMoveTo(parseFloat(points[0]), parseFloat(points[1]));
                }
            }
        }
    },
    
    /**
     * This method excepts the SVGDoucment that is the SVG representation
     * of this shape.
     * The bounds of the shape are calculated, the SVG representation's upper left point
     * is moved to 0,0 and it the method sets if this shape is resizable.
     *
     * @param {SVGDocument} svgDocument
     */
    _init: function(svgDocument){
        arguments.callee.$._init.apply(this, arguments);
		
        var svgNode = svgDocument.getElementsByTagName("g")[0]; //outer most g node
        // set all required attributes
        var attributeTitle = svgDocument.ownerDocument.createAttribute("title");
        attributeTitle.nodeValue = this.getStencil().title();
        svgNode.setAttributeNode(attributeTitle);
        
        var attributeId = svgDocument.ownerDocument.createAttribute("id");
        attributeId.nodeValue = this.id;
        svgNode.setAttributeNode(attributeId);
        
        // 
        var stencilTargetNode = this.node.childNodes[0].childNodes[0]; //<g class=me>"
        svgNode = stencilTargetNode.appendChild(svgNode);
        
        // Add to the EventHandler
        this.addEventHandlers(svgNode.parentNode);
        
        /**set minimum and maximum size*/
        var minSizeAttr = svgNode.getAttributeNS(ORYX.CONFIG.NAMESPACE_ORYX, "minimumSize");
        if (minSizeAttr) {
            minSizeAttr = minSizeAttr.replace("/,/g", " ");
            var minSizeValues = minSizeAttr.split(" ");
            minSizeValues = minSizeValues.without("");
            
            if (minSizeValues.length > 1) {
                this.minimumSize = {
                    width: parseFloat(minSizeValues[0]),
                    height: parseFloat(minSizeValues[1])
                };
            }
            else {
                //set minimumSize to (1,1), so that width and height of the stencil can never be (0,0)
                this.minimumSize = {
                    width: 1,
                    height: 1
                };
            }
        }
        
        var maxSizeAttr = svgNode.getAttributeNS(ORYX.CONFIG.NAMESPACE_ORYX, "maximumSize");
        if (maxSizeAttr) {
            maxSizeAttr = maxSizeAttr.replace("/,/g", " ");
            var maxSizeValues = maxSizeAttr.split(" ");
            maxSizeValues = maxSizeValues.without("");
            
            if (maxSizeValues.length > 1) {
                this.maximumSize = {
                    width: parseFloat(maxSizeValues[0]),
                    height: parseFloat(maxSizeValues[1])
                };
            }
        }
        
        if (this.minimumSize && this.maximumSize &&
        (this.minimumSize.width > this.maximumSize.width ||
        this.minimumSize.height > this.maximumSize.height)) {
        
            //TODO wird verschluckt!!!
            throw this + ": Minimum Size must be greater than maxiumSize.";
        }
        
        /**get current bounds and adjust it to upperLeft == (0,0)*/
        //initialize all SVGShape objects
        this._svgShapes = this._initSVGShapes(svgNode);
        
        //get upperLeft and lowerRight of stencil
        var upperLeft = {
            x: undefined,
            y: undefined
        };
        var lowerRight = {
            x: undefined,
            y: undefined
        };
        var me = this;
        this._svgShapes.each(function(svgShape){
            upperLeft.x = (upperLeft.x !== undefined) ? Math.min(upperLeft.x, svgShape.x) : svgShape.x;
            upperLeft.y = (upperLeft.y !== undefined) ? Math.min(upperLeft.y, svgShape.y) : svgShape.y;
            lowerRight.x = (lowerRight.x !== undefined) ? Math.max(lowerRight.x, svgShape.x + svgShape.width) : svgShape.x + svgShape.width;
            lowerRight.y = (lowerRight.y !== undefined) ? Math.max(lowerRight.y, svgShape.y + svgShape.height) : svgShape.y + svgShape.height;
            
            /** set if resizing is enabled */
            //TODO isResizable durch die beiden anderen booleans ersetzen?
            if (svgShape.isHorizontallyResizable) {
                me.isHorizontallyResizable = true;
                me.isResizable = true;
            }
            if (svgShape.isVerticallyResizable) {
                me.isVerticallyResizable = true;
                me.isResizable = true;
            }
            if (svgShape.anchorTop && svgShape.anchorBottom) {
                me.isVerticallyResizable = true;
                me.isResizable = true;
            }
            if (svgShape.anchorLeft && svgShape.anchorRight) {
                me.isHorizontallyResizable = true;
                me.isResizable = true;
            }
        });
        
        //move all SVGShapes by -upperLeft
        this._svgShapes.each(function(svgShape){
            svgShape.x -= upperLeft.x;
            svgShape.y -= upperLeft.y;
            svgShape.update();
        });
        
        //set bounds of shape
        //the offsets are also needed for positioning the magnets and the docker
        var offsetX = upperLeft.x;
        var offsetY = upperLeft.y;
        
        lowerRight.x -= offsetX;
        lowerRight.y -= offsetY;
        upperLeft.x = 0;
        upperLeft.y = 0;
        
        //prevent that width or height of initial bounds is 0
        if (lowerRight.x === 0) {
            lowerRight.x = 1;
        }
        if (lowerRight.y === 0) {
            lowerRight.y = 1;
        }
        
        this._oldBounds.set(upperLeft, lowerRight);
        this.bounds.set(upperLeft, lowerRight);
        
        /**initialize magnets */
        
        var magnets = svgDocument.getElementsByTagNameNS(ORYX.CONFIG.NAMESPACE_ORYX, "magnets");
        
        if (magnets && magnets.length > 0) {
        
            magnets = $A(magnets[0].getElementsByTagNameNS(ORYX.CONFIG.NAMESPACE_ORYX, "magnet"));
            
            var me = this;
            magnets.each(function(magnetElem){
                var magnet = new ORYX.Core.Controls.Magnet({
                    eventHandlerCallback: me.eventHandlerCallback
                });
                var cx = parseFloat(magnetElem.getAttributeNS(ORYX.CONFIG.NAMESPACE_ORYX, "cx"));
                var cy = parseFloat(magnetElem.getAttributeNS(ORYX.CONFIG.NAMESPACE_ORYX, "cy"));
                magnet.bounds.centerMoveTo({
                    x: cx - offsetX,
                    y: cy - offsetY
                });
                
                //get anchors
                var anchors = magnetElem.getAttributeNS(ORYX.CONFIG.NAMESPACE_ORYX, "anchors");
                if (anchors) {
                    anchors = anchors.replace("/,/g", " ");
                    anchors = anchors.split(" ").without("");
                    for(var i = 0; i < anchors.length; i++) {
						switch(anchors[i].toLowerCase()) {
							case "left":
								magnet.anchorLeft = true;
								break;
							case "right":
								magnet.anchorRight = true;
								break;
							case "top":
								magnet.anchorTop = true;
								break;
							case "bottom":
								magnet.anchorBottom = true;
								break;
						}
					}
                }
                
                me.add(magnet);
                
                //check, if magnet is default magnet
                if (!this._defaultMagnet) {
                    var defaultAttr = magnetElem.getAttributeNS(ORYX.CONFIG.NAMESPACE_ORYX, "default");
                    if (defaultAttr && defaultAttr.toLowerCase() === "yes") {
                        me._defaultMagnet = magnet;
                    }
                }
            });
        }
        else {
            // Add a Magnet in the Center of Shape			
            var magnet = new ORYX.Core.Controls.Magnet();
            magnet.bounds.centerMoveTo(this.bounds.width() / 2, this.bounds.height() / 2);
            this.add(magnet);
        }
        
        /**initialize docker */
        var dockerElem = svgDocument.getElementsByTagNameNS(ORYX.CONFIG.NAMESPACE_ORYX, "docker");
        
        if (dockerElem && dockerElem.length > 0) {
            dockerElem = dockerElem[0];
            var docker = this.createDocker();
            var cx = parseFloat(dockerElem.getAttributeNS(ORYX.CONFIG.NAMESPACE_ORYX, "cx"));
            var cy = parseFloat(dockerElem.getAttributeNS(ORYX.CONFIG.NAMESPACE_ORYX, "cy"));
            docker.bounds.centerMoveTo({
                x: cx - offsetX,
                y: cy - offsetY
            });
            
            //get anchors
            var anchors = dockerElem.getAttributeNS(ORYX.CONFIG.NAMESPACE_ORYX, "anchors");
            if (anchors) {
                anchors = anchors.replace("/,/g", " ");
                anchors = anchors.split(" ").without("");
                
				for(var i = 0; i < anchors.length; i++) {
					switch(anchors[i].toLowerCase()) {
						case "left":
							docker.anchorLeft = true;
							break;
						case "right":
							docker.anchorRight = true;
							break;
						case "top":
							docker.anchorTop = true;
							break;
						case "bottom":
							docker.anchorBottom = true;
							break;
					}
				}
            }
        }
        
        /**initialize labels*/
        var textElems = svgNode.getElementsByTagNameNS(ORYX.CONFIG.NAMESPACE_SVG, 'text');
        $A(textElems).each((function(textElem){
            var label = new ORYX.Core.SVG.Label({
                textElement: textElem,
				shapeId: this.id
            });
            label.x -= offsetX;
            label.y -= offsetY;
            this._labels[label.id] = label;
			
			label.registerOnChange(this.layout.bind(this));
			
			// Only apply fitting on form-components
			if(this._stencil.id().indexOf(ORYX.CONFIG.FORM_ELEMENT_ID_PREFIX) == 0) {
				label.registerOnChange(this.fitToLabels.bind(this));
			}
			
        }).bind(this));
    },
    
    fitToLabels: function() {
    	var y = 0;
    	
    	this.getLabels().each(function(label){
    		var lr = label.getY() + label.getHeight();
    		if(lr > y) {
    			y = lr;
    		}
    	});
    	
    	var bounds = this.bounds;
    	var boundsChanged = false;
    	
    	if(this.minimumSize) {
    		// Check if y-value exceeds the min-value. If not, stick to this value.
    		var minHeight = this.minimumSize.height;
    		if(y < minHeight && bounds.height() > minHeight && minHeight > this.forcedHeight) {
    			bounds.set(bounds.upperLeft().x, bounds.upperLeft().y, bounds.lowerRight().x, bounds.upperLeft().y + minHeight);
    			boundsChanged = true;
    		} else if(y > minHeight && bounds.height() != y && y > this.forcedHeight){
    			bounds.set(bounds.upperLeft().x, bounds.upperLeft().y, bounds.lowerRight().x, bounds.upperLeft().y + y);
    			boundsChanged = true;
    		} else if(bounds.height() > this.forcedHeight && this.forcedHeight > 0) {
    			bounds.set(bounds.upperLeft().x, bounds.upperLeft().y, bounds.lowerRight().x, bounds.upperLeft().y + this.forcedHeight);
    			boundsChanged = true;
    		}
    	}
    	
    	if(boundsChanged) {
    		// Force facade to re-layout since bounds are changed AFTER layout has been performed
    		if(this.facade.getCanvas() != null) {
				this.facade.getCanvas().update();
			}
    		
    		// Re-select if needed to force the select
    		if(this.facade.getSelection().member(this)) {
    			var selectedNow = this.facade.getSelection();
    			this.facade.setSelection([]);
    			this.facade.setSelection(selectedNow);
    		}
    	}
    },
	
	/**
	 * Override the Method, that a docker is not shown
	 *
	 */
	createDocker: function() {
		var docker = new ORYX.Core.Controls.Docker({eventHandlerCallback: this.eventHandlerCallback});
		docker.bounds.registerCallback(this._dockerChangedCallback);
		
		this.dockers.push( docker );
		docker.parent = this;
		docker.bounds.registerCallback(this._changedCallback);		
		
		return docker		
	},	
    
    toString: function(){
        return this._stencil.title() + " " + this.id
    }
};
ORYX.Core.Node = ORYX.Core.Shape.extend(ORYX.Core.Node);
/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

NAMESPACE_SVG = "http://www.w3.org/2000/svg";
NAMESPACE_ORYX = "http://www.b3mn.org/oryx";


/**
 * Init namespaces
 */
if (!ORYX) {
    var ORYX = {};
}
if (!ORYX.Core) {
    ORYX.Core = {};
}


/**
 * @classDescription Abstract base class for all connections.
 * @extends {ORYX.Core.Shape}
 * @param options {Object}
 *
 * TODO da die verschiebung der Edge nicht ueber eine
 *  translation gemacht wird, die sich auch auf alle kind UIObjects auswirkt,
 *  muessen die kinder hier beim verschieben speziell betrachtet werden.
 *  Das sollte ueberarbeitet werden.
 *
 */
ORYX.Core.Edge = {
    /**
     * Constructor
     * @param {Object} options
     * @param {Stencil} stencil
     */
    construct: function(options, stencil, facade){
        arguments.callee.$.construct.apply(this, arguments);
        
        this.isMovable = true;
        this.isSelectable = true;
		
		this._dockerUpdated = false;
        
        this._markers = new Hash(); //a hash map of SVGMarker objects where keys are the marker ids
        this._paths = [];
        this._interactionPaths = [];
        this._dockersByPath = new Hash();
        this._markersByPath = new Hash();
		
		/* Data structures to store positioning information of attached child nodes */ 
		this.attachedNodePositionData = new Hash();
        
        //TODO was muss hier initial erzeugt werden?
        var stencilNode = this.node.childNodes[0].childNodes[0];
        stencilNode = ORYX.Editor.graft("http://www.w3.org/2000/svg", stencilNode, ['g', {
            "pointer-events": "painted"
        }]);
        
        //Add to the EventHandler
        this.addEventHandlers(stencilNode.parentNode);
        
        
        this._oldBounds = this.bounds.clone();
        
        //load stencil
        this._init(this._stencil.view());
        
        if (stencil instanceof Array) {
            this.deserialize(stencil);
        }
        
    },
    
    _update: function(force){
        if(this._dockerUpdated || this.isChanged || force) {
		  
			this.dockers.invoke("update");
			
	        if (false && (this.bounds.width() === 0 || this.bounds.height() === 0)) {
				var width = this.bounds.width();
				var height = this.bounds.height();
	            this.bounds.extend({
	                x: width === 0 ? 2 : 0,
	                y: height === 0 ? 2 : 0
	            });
	            this.bounds.moveBy({
	                x: width === 0 ? -1 : 0,
	                y: height === 0 ? -1 : 0
	            });
            
	        }
	        
	        // TODO: Bounds muss abhaengig des Eltern-Shapes gesetzt werden
	        var upL = this.bounds.upperLeft();
	        var oldUpL = this._oldBounds.upperLeft();
	        var oldWidth = this._oldBounds.width() === 0 ? this.bounds.width() : this._oldBounds.width();
	        var oldHeight = this._oldBounds.height() === 0 ? this.bounds.height() : this._oldBounds.height();
	        var diffX = upL.x - oldUpL.x;
	        var diffY = upL.y - oldUpL.y;
	        var diffWidth = (this.bounds.width() / oldWidth) || 1;
	        var diffHeight = (this.bounds.height() / oldHeight) || 1;
	        
	        this.dockers.each((function(docker){
	            // Unregister on BoundsChangedCallback
	            docker.bounds.unregisterCallback(this._dockerChangedCallback);
	            
	            // If there is any changes at the edge and is there is not an DockersUpdate
	            // set the new bounds to the docker
	            if (!this._dockerUpdated) {
	                docker.bounds.moveBy(diffX, diffY);
	                
	                if (diffWidth !== 1 || diffHeight !== 1) {
	                    var relX = docker.bounds.upperLeft().x - upL.x;
	                    var relY = docker.bounds.upperLeft().y - upL.y;
	                    
	                    docker.bounds.moveTo(upL.x + relX * diffWidth, upL.y + relY * diffHeight);
	                }
	            }
	            // Do Docker update and register on DockersBoundChange
	            docker.update();
	            docker.bounds.registerCallback(this._dockerChangedCallback);
	            
	        }).bind(this));
	        
	        if (this._dockerUpdated) {
	            var a = this.dockers.first().bounds.center();
	            var b = this.dockers.first().bounds.center();
	            
	            this.dockers.each((function(docker){
	                var center = docker.bounds.center();
	                a.x = Math.min(a.x, center.x);
	                a.y = Math.min(a.y, center.y);
	                b.x = Math.max(b.x, center.x);
	                b.y = Math.max(b.y, center.y);
	            }).bind(this));
	            
	            //set the bounds of the the association
	            this.bounds.set(Object.clone(a), Object.clone(b));
	        }
			
			upL = this.bounds.upperLeft(); oldUpL = this._oldBounds.upperLeft();
			diffWidth = (this.bounds.width() / (oldWidth||this.bounds.width())); diffHeight = (this.bounds.height() / (oldHeight||this.bounds.height())); 
	        diffX = upL.x - oldUpL.x; diffY = upL.y - oldUpL.y;
					
			//reposition labels
			this.getLabels().each(function(label) {
				
				if (label.getReferencePoint()){
					var ref = label.getReferencePoint();
					var from = ref.segment.from, to = ref.segment.to;
					if (!from || !from.parent || !to || !to.parent) {
						return;
					}
					
					var fromPosition = from.bounds.center(), toPosition = to.bounds.center();

					if (fromPosition.x === ref.segment.fromPosition.x && fromPosition.y === ref.segment.fromPosition.y &&
						toPosition.x === ref.segment.toPosition.x && toPosition.y === ref.segment.toPosition.y && !ref.dirty){
						return;
					}
					
					if (!this.parent.initializingShapes) {
						var oldDistance = ORYX.Core.Math.getDistanceBetweenTwoPoints(ref.segment.fromPosition, ref.segment.toPosition, ref.intersection);
						var newIntersection = ORYX.Core.Math.getPointBetweenTwoPoints(fromPosition, toPosition, isNaN(oldDistance) ? 0.5 : oldDistance);
						
						/**
						 * Set position
						 */
						// Get the orthogonal identity vector of the current segment
						var oiv = ORYX.Core.Math.getOrthogonalIdentityVector(fromPosition, toPosition);
						var isHor = Math.abs(oiv.y)===1, isVer = Math.abs(oiv.x)===1;
						oiv.x *= ref.distance; oiv.y *= ref.distance; 				// vector * distance
						oiv.x += newIntersection.x; oiv.y += newIntersection.y; 	// vector + the intersection point				
						var mx = isHor && ref.orientation && (ref.iorientation||ref.orientation).endsWith("r") ? -label.getWidth() : 0;		
						var my = isVer && ref.orientation && (ref.iorientation||ref.orientation).startsWith("l") ? -label.getHeight()+2 : 0;
						label.setX(oiv.x+mx); label.setY(oiv.y+my);
						
						// Update the reference point
						this.updateReferencePointOfLabel(label, newIntersection, from, to);
					} else {
						var oiv = ORYX.Core.Math.getOrthogonalIdentityVector(fromPosition, toPosition);
						oiv.x *= ref.distance; oiv.y *= ref.distance; // vector * distance
						oiv.x += ref.intersection.x; oiv.y += ref.intersection.y; // vector + the intersection point		
						label.setX(oiv.x); label.setY(oiv.y);
						ref.segment.fromPosition = fromPosition; ref.segment.toPosition = toPosition;		
					}
					
					return;	
				}
				
				// Update label position if no reference point is set
				if (label.position && !this.parent.initializingShapes){
					var x = label.position.x + (diffX * (diffWidth||1));
					if (x > this.bounds.lowerRight().x){
						x += this.bounds.width()-(this.bounds.width()/(diffWidth||1));
					}
					
					var y = label.position.y + (diffY * (diffHeight||1));
					if (y > this.bounds.lowerRight().y){
						y += this.bounds.height()-(this.bounds.height()/(diffHeight||1));
					}
					label.setX(x);label.setY(y);
					return;
				}
				
				switch (label.getEdgePosition()) {
					case "starttop":
						var angle = this._getAngle(this.dockers[0], this.dockers[1]);
						var pos = this.dockers.first().bounds.center();
						
						if (angle <= 90 || angle > 270) {
							label.horizontalAlign("left");
							label.verticalAlign("bottom");
							label.x = pos.x + label.getOffsetTop();
							label.y = pos.y - label.getOffsetTop();
							label.rotate(360 - angle, pos);
						} else {
							label.horizontalAlign("right");
							label.verticalAlign("bottom");
							label.x = pos.x - label.getOffsetTop();
							label.y = pos.y - label.getOffsetTop();
							label.rotate(180 - angle, pos);
						}
						
						break;

					case "startmiddle":
						var angle = this._getAngle(this.dockers[0], this.dockers[1]);
						var pos = this.dockers.first().bounds.center();
						
						if (angle <= 90 || angle > 270) {
							label.horizontalAlign("left");
							label.verticalAlign("bottom");
							label.x = pos.x + 2;
							label.y = pos.y + 4;
							label.rotate(360 - angle, pos);
						} else {
							label.horizontalAlign("right");
							label.verticalAlign("bottom");
							label.x = pos.x + 1;
							label.y = pos.y + 4;
							label.rotate(180 - angle, pos);
						}
						
						break;
												
					case "startbottom":
						var angle = this._getAngle(this.dockers[0], this.dockers[1]);
						var pos = this.dockers.first().bounds.center();
						
						if (angle <= 90 || angle > 270) {
							label.horizontalAlign("left");
							label.verticalAlign("top");
							label.x = pos.x + label.getOffsetBottom();
							label.y = pos.y + label.getOffsetBottom();
							label.rotate(360 - angle, pos);
						} else {
							label.horizontalAlign("right");
							label.verticalAlign("top");
							label.x = pos.x - label.getOffsetBottom();
							label.y = pos.y + label.getOffsetBottom();
							label.rotate(180 - angle, pos);
						}
						
						break;
					case "midtop":
						var numOfDockers = this.dockers.length;
						if(numOfDockers%2 == 0) {
							var angle = this._getAngle(this.dockers[numOfDockers/2-1], this.dockers[numOfDockers/2])
							var pos1 = this.dockers[numOfDockers/2-1].bounds.center();
							var pos2 = this.dockers[numOfDockers/2].bounds.center();
							var pos = {x:(pos1.x + pos2.x)/2.0, y:(pos1.y+pos2.y)/2.0};
							
							label.horizontalAlign("center");
							label.verticalAlign("bottom");
							label.x = pos.x;
							label.y = pos.y - label.getOffsetTop();
								
							if (angle <= 90 || angle > 270) {
								label.rotate(360 - angle, pos);
							} else {
								label.rotate(180 - angle, pos);
							}
						} else {
							var index = parseInt(numOfDockers/2);
							var angle = this._getAngle(this.dockers[index], this.dockers[index+1])
							var pos = this.dockers[index].bounds.center();
							
							if (angle <= 90 || angle > 270) {
								label.horizontalAlign("left");
								label.verticalAlign("bottom");
								label.x = pos.x + label.getOffsetTop();
								label.y = pos.y - label.getOffsetTop();
								label.rotate(360 - angle, pos);
							} else {
								label.horizontalAlign("right");
								label.verticalAlign("bottom");
								label.x = pos.x - label.getOffsetTop();
								label.y = pos.y - label.getOffsetTop();
								label.rotate(180 - angle, pos);
							}
						}
						
						break;
					case "midbottom":
						var numOfDockers = this.dockers.length;
						if(numOfDockers%2 == 0) {
							var angle = this._getAngle(this.dockers[numOfDockers/2-1], this.dockers[numOfDockers/2])
							var pos1 = this.dockers[numOfDockers/2-1].bounds.center();
							var pos2 = this.dockers[numOfDockers/2].bounds.center();
							var pos = {x:(pos1.x + pos2.x)/2.0, y:(pos1.y+pos2.y)/2.0};
							
							label.horizontalAlign("center");
							label.verticalAlign("top");
							label.x = pos.x;
							label.y = pos.y + label.getOffsetTop();
							
							if (angle <= 90 || angle > 270) {
								label.rotate(360 - angle, pos);
							} else {
								label.rotate(180 - angle, pos);
							}
						} else {
							var index = parseInt(numOfDockers/2);
							var angle = this._getAngle(this.dockers[index], this.dockers[index+1])
							var pos = this.dockers[index].bounds.center();
							
							if (angle <= 90 || angle > 270) {
								label.horizontalAlign("left");
								label.verticalAlign("top");
								label.x = pos.x + label.getOffsetBottom();
								label.y = pos.y + label.getOffsetBottom();
								label.rotate(360 - angle, pos);
							} else {
								label.horizontalAlign("right");
								label.verticalAlign("top");
								label.x = pos.x - label.getOffsetBottom();
								label.y = pos.y + label.getOffsetBottom();
								label.rotate(180 - angle, pos);
							}
						}
						
						break;
					case "endtop":
						var length = this.dockers.length;
						var angle = this._getAngle(this.dockers[length-2], this.dockers[length-1]);
						var pos = this.dockers.last().bounds.center();
						
						if (angle <= 90 || angle > 270) {
							label.horizontalAlign("right");
							label.verticalAlign("bottom");
							label.x = pos.x - label.getOffsetTop();
							label.y = pos.y - label.getOffsetTop();
							label.rotate(360 - angle, pos);
						} else {
							label.horizontalAlign("left");
							label.verticalAlign("bottom");
							label.x = pos.x + label.getOffsetTop();
							label.y = pos.y - label.getOffsetTop();
							label.rotate(180 - angle, pos);
						}
						
						break;
					case "endbottom":
						var length = this.dockers.length;
						var angle = this._getAngle(this.dockers[length-2], this.dockers[length-1]);
						var pos = this.dockers.last().bounds.center();
						
						if (angle <= 90 || angle > 270) {
							label.horizontalAlign("right");
							label.verticalAlign("top");
							label.x = pos.x - label.getOffsetBottom();
							label.y = pos.y + label.getOffsetBottom();
							label.rotate(360 - angle, pos);
						} else {
							label.horizontalAlign("left");
							label.verticalAlign("top");
							label.x = pos.x + label.getOffsetBottom();
							label.y = pos.y + label.getOffsetBottom();
							label.rotate(180 - angle, pos);
						}
						
						break;
				}
			}.bind(this));
			
			this.children.each(function(value) {
				if(value instanceof ORYX.Core.Node) {
					this.calculatePositionOfAttachedChildNode.call(this, value);
				}
			}.bind(this));
			
			this.refreshAttachedNodes();
			this.refresh();
			
			this.isChanged = false;
			this._dockerUpdated = false;
			
			this._oldBounds = this.bounds.clone();
        }
		
		
  	    // IE10 specific fix, start and end-markes get left behind when moving path
		var userAgent = navigator.userAgent;
		if (navigator.appVersion.indexOf("MSIE 10") !== -1 || (userAgent.indexOf('Trident') !== -1 && userAgent.indexOf('rv:11') !== -1)) 
		{
			this.node.parentNode.insertBefore(this.node, this.node);
		}
    },
	
	/**
	 *  Moves a point to the upperLeft of a node's bounds.
	 *  
	 *  @param {point} point
	 *  	The point to move
	 *  @param {ORYX.Core.Bounds} bounds
	 *  	The Bounds of the related noe
	 */
	movePointToUpperLeftOfNode: function(point, bounds) {
		point.x -= bounds.width()/2;
		point.y -= bounds.height()/2;
	},
	
	/**
	 * Refreshes the visual representation of edge's attached nodes.
	 */	
	refreshAttachedNodes: function() {
		this.attachedNodePositionData.values().each(function(nodeData) {
			var startPoint = nodeData.segment.docker1.bounds.center();
			var endPoint = nodeData.segment.docker2.bounds.center();
			this.relativizePoint(startPoint);
			this.relativizePoint(endPoint);
			
			var newNodePosition = new Object();
			
			/* Calculate new x-coordinate */
			newNodePosition.x = startPoint.x 
								+ nodeData.relativDistanceFromDocker1
									* (endPoint.x - startPoint.x);
			
			/* Calculate new y-coordinate */
			newNodePosition.y = startPoint.y 
								+ nodeData.relativDistanceFromDocker1
									* (endPoint.y - startPoint.y);
			
			/* Convert new position to the upper left of the node */
			this.movePointToUpperLeftOfNode(newNodePosition, nodeData.node.bounds);
			
			/* Move node to its new position */
			nodeData.node.bounds.moveTo(newNodePosition);
			nodeData.node._update();					
			
		}.bind(this));
	},
	
	/**
	 * Calculates the position of an edge's child node. The node is placed on 
	 * the path of the edge.
	 * 
	 * @param {node}
	 * 		The node to calculate the new position
	 * @return {Point}
	 * 		The calculated upper left point of the node's shape.
	 */
	calculatePositionOfAttachedChildNode: function(node) {
		/* Initialize position */
		var position = new Object();
		position.x = 0;
		position.y = 0;
		
		/* Case: Node was just added */
		if(!this.attachedNodePositionData[node.getId()]) {
			this.attachedNodePositionData[node.getId()] = new Object();
			this.attachedNodePositionData[node.getId()]
					.relativDistanceFromDocker1 = 0;
			this.attachedNodePositionData[node.getId()].node = node;
			this.attachedNodePositionData[node.getId()].segment = new Object();
			this.findEdgeSegmentForNode(node);
		}else if(node.isChanged) {
			this.findEdgeSegmentForNode(node);
		}
		
		
		
	},
	
	/**
	 * Finds the appropriate edge segement for a node.
	 * The segment is choosen, which has the smallest distance to the node.
	 * 
	 * @param {ORYX.Core.Node} node
	 * 		The concerning node
	 */
	findEdgeSegmentForNode: function(node) {
		var length = this.dockers.length;
		var smallestDistance = undefined;
		
		for(i=1;i<length;i++) {
			var lineP1 = this.dockers[i-1].bounds.center();
			var lineP2 = this.dockers[i].bounds.center();
			this.relativizePoint(lineP1);
			this.relativizePoint(lineP2);
			
			var nodeCenterPoint = node.bounds.center();
			var distance = ORYX.Core.Math.distancePointLinie(
															lineP1,
															lineP2, 
															nodeCenterPoint, 
															true);
			
			if((distance || distance == 0) && ((!smallestDistance && smallestDistance != 0) 
						|| distance < smallestDistance)) {
				
				smallestDistance = distance;
				
				this.attachedNodePositionData[node.getId()].segment.docker1 = 
													this.dockers[i-1];
				this.attachedNodePositionData[node.getId()].segment.docker2 = 
													this.dockers[i];
	
			}
			
			/* Either the distance does not match the segment or the distance
			 * between docker1 and docker2 is 0
			 * 
			 * In this case choose the nearest docker as attaching point.
			 * 
			 */
			if(!distance && !smallestDistance && smallestDistance != 0) {
				(ORYX.Core.Math.getDistancePointToPoint(nodeCenterPoint, lineP1)
					< ORYX.Core.Math.getDistancePointToPoint(nodeCenterPoint, lineP2)) ?
					this.attachedNodePositionData[node.getId()].relativDistanceFromDocker1 = 0 :
					this.attachedNodePositionData[node.getId()].relativDistanceFromDocker1 = 1;
				this.attachedNodePositionData[node.getId()].segment.docker1 = 
													this.dockers[i-1];
				this.attachedNodePositionData[node.getId()].segment.docker2 = 
													this.dockers[i];
			}
		}
		
		/* Calculate position on edge segment for the node */
		if(smallestDistance || smallestDistance == 0) {
			this.attachedNodePositionData[node.getId()].relativDistanceFromDocker1 =
			this.getLineParameterForPosition(
					this.attachedNodePositionData[node.getId()].segment.docker1,
					this.attachedNodePositionData[node.getId()].segment.docker2,
					node);
		}
	},
	
	
	/**
	 *
	 * @param {ORYX.Core.Node|Object} node or position
	 * @return {Object} An object with the following attribute: {ORYX.Core.Docker} fromDocker, {ORYX.Core.Docker} toDocker, {X/Y} position, {int} distance
	 */
 	findSegment: function(node){
		
		var length = this.dockers.length;
		var result;
		
		var nodeCenterPoint = node instanceof ORYX.Core.UIObject ? node.bounds.center() : node;
			
		for (i = 1; i < length; i++) {
			var lineP1 = this.dockers[i - 1].bounds.center();
			var lineP2 = this.dockers[i].bounds.center();
			
			var distance = ORYX.Core.Math.distancePointLinie(lineP1, lineP2, nodeCenterPoint, true);
			
			if (typeof distance == "number" && (result === undefined || distance < result.distance)) {
				result = {
					distance: distance,
					fromDocker: this.dockers[i - 1],
					toDocker: this.dockers[i]
				}
				
			}
		}
		return result;
	},
	
	/**
	 * Returns the value of the scalar to determine the position of the node on 
	 * line defined by docker1 and docker2.
	 * 
	 * @param {point} docker1
	 * 		The docker that defines the start of the line segment
	 * @param {point} docker2
	 * 		The docker that defines the end of the line segment
	 * @param {ORYX.Core.Node} node
	 * 		The concerning node
	 * 
	 * @return {float} positionParameter
	 * 		The scalar value to determine the position on the line
	 */
	getLineParameterForPosition: function(docker1, docker2, node) {
		var dockerPoint1 = docker1.bounds.center();
		var dockerPoint2 = docker2.bounds.center();
		this.relativizePoint(dockerPoint1);
		this.relativizePoint(dockerPoint2);
		
		var intersectionPoint = ORYX.Core.Math.getPointOfIntersectionPointLine(
									dockerPoint1,
									dockerPoint2,
									node.bounds.center(), true);
		if(!intersectionPoint) {
			return 0;
		}
		
		var relativeDistance = 
			ORYX.Core.Math.getDistancePointToPoint(intersectionPoint, dockerPoint1) /
			ORYX.Core.Math.getDistancePointToPoint(dockerPoint1, dockerPoint2);
		
		return relativeDistance;
	},
	/**
	 * Makes point relative to the upper left of the edge's bound.
	 * 
	 * @param {point} point
	 * 		The point to relativize
	 */
	relativizePoint: function(point) {
		point.x -= this.bounds.upperLeft().x;
		point.y -= this.bounds.upperLeft().y;		
	},
	
	/**
	 * Move the first and last docker and calls the refresh method.
	 * Attention: This does not calculates intersection point between the
	 * edge and the bounded nodes. This only works if only the nodes are
	 * moves.
	 *
	 */
	optimizedUpdate: function(){
		
		var updateDocker = function(docker){
			if (!docker._dockedShape || !docker._dockedShapeBounds)
				return;
			var off = {
			    x: docker._dockedShape.bounds.a.x - docker._dockedShapeBounds.a.x,
			    y: docker._dockedShape.bounds.a.y - docker._dockedShapeBounds.a.y
			};
			docker.bounds.moveBy(off);
			docker._dockedShapeBounds.moveBy(off);
		}
		
		updateDocker(this.dockers.first());
		updateDocker(this.dockers.last());
		
		this.refresh();
	},
    
    refresh: function(){
        //call base class refresh method
        arguments.callee.$.refresh.apply(this, arguments);
        
        //TODO consider points for marker mids
        var lastPoint;
        this._paths.each((function(path, index){
            var dockers = this._dockersByPath[path.id];
            var c = undefined;
			var d = undefined;
            if (lastPoint) {
                d = "M" + lastPoint.x + " " + lastPoint.y;
            }
            else {
                c = dockers[0].bounds.center();
                lastPoint = c;
                
                d = "M" + c.x + " " + c.y;
            }
            
            for (var i = 1; i < dockers.length; i++) {
                // for each docker, draw a line to the center
                c = dockers[i].bounds.center();
                d = d + "L" + c.x + " " + c.y + " ";
                lastPoint = c;
            }
            
            path.setAttributeNS(null, "d", d);
            this._interactionPaths[index].setAttributeNS(null, "d", d);
            
        }).bind(this));
		
		
		/* move child shapes of an edge */
		if(this.getChildNodes().length > 0) {
	        var x = this.bounds.upperLeft().x;
	        var y = this.bounds.upperLeft().y;
	        
			this.node.firstChild.childNodes[1].setAttributeNS(null, "transform", "translate(" + x + ", " + y + ")");
		}
		
    },
    
    /**
     * Calculate the Border Intersection Point between two points
     * @param {PointA}
     * @param {PointB}
     */
    getIntersectionPoint: function(){
    
        var length = Math.floor(this.dockers.length / 2)
        
        return ORYX.Core.Math.midPoint(this.dockers[length - 1].bounds.center(), this.dockers[length].bounds.center())
    },
    
	/**
     * Returns TRUE if the bounds is over the edge
     * @param {Bounds}
     *
     */
    isBoundsIncluded: function(bounds){
		var dockers = this.dockers, size = dockers.length;
		return dockers.any(function(docker, i){
			if (i == size-1){ return false; }
			var a = docker.bounds.center();
			var b = dockers[i+1].bounds.center();
			
			return ORYX.Core.Math.isRectOverLine(a.x, a.y, b.x, b.y, bounds.a.x, bounds.a.y, bounds.b.x, bounds.b.y);
		});
	},
    
    /**
     * Calculate if the point is inside the Shape
     * @param {PointX}
     * @param {PointY} 
     */
    isPointIncluded: function(pointX, pointY){
    
        var isbetweenAB = this.absoluteBounds().isIncluded(pointX, pointY, 
												ORYX.CONFIG.OFFSET_EDGE_BOUNDS);
        
		var isPointIncluded = undefined;
		
        if (isbetweenAB && this.dockers.length > 0) {
		
			var i = 0;
			var point1, point2;
			
			
			do {
			
				point1 = this.dockers[i].bounds.center();
				point2 = this.dockers[++i].bounds.center();
				
				isPointIncluded = ORYX.Core.Math.isPointInLine(pointX, pointY, 
											point1.x, point1.y, 
											point2.x, point2.y, 
											ORYX.CONFIG.OFFSET_EDGE_BOUNDS);
				
			} while (!isPointIncluded && i < this.dockers.length - 1)
			
		}
		
		return isPointIncluded;

    },
  
    
    /**
     * Calculate if the point is over an special offset area
     * @param {Point}
     */
    isPointOverOffset: function(){
		return  false
	},
	
	/**
	 * Returns TRUE if the given node
	 * is a child node of the shapes node
	 * @param {Element} node
	 * @return {Boolean}
	 *
	 */
	containsNode: function(node){
		if (this._paths.include(node) || 
       		this._interactionPaths.include(node)){
			return true;		
		}	
		return false;
	},
	
	/**
	* Returns the angle of the line between two dockers
	* (0 - 359.99999999)
	*/
	_getAngle: function(docker1, docker2) {
		var p1 = docker1 instanceof ORYX.Core.Controls.Docker ? docker1.absoluteCenterXY() : docker1;
		var p2 = docker2 instanceof ORYX.Core.Controls.Docker ? docker2.absoluteCenterXY() : docker2;
		
		return ORYX.Core.Math.getAngle(p1, p2);
	},
	    
    alignDockers: function(){
        this._update(true);
        
        var firstPoint = this.dockers.first().bounds.center();
        var lastPoint = this.dockers.last().bounds.center();
        
        var deltaX = lastPoint.x - firstPoint.x;
        var deltaY = lastPoint.y - firstPoint.y;
        
        var numOfDockers = this.dockers.length - 1;
        
        this.dockers.each((function(docker, index){
            var part = index / numOfDockers;
            docker.bounds.unregisterCallback(this._dockerChangedCallback);
            docker.bounds.moveTo(firstPoint.x + part * deltaX, firstPoint.y + part * deltaY);
            docker.bounds.registerCallback(this._dockerChangedCallback);
        }).bind(this));
        
        this._dockerChanged();
    },
    
	add: function(shape){
        arguments.callee.$.add.apply(this, arguments);
		
		// If the new shape is a Docker which is not contained
		if (shape instanceof ORYX.Core.Controls.Docker && this.dockers.include(shape)){
			// Add it to the dockers list ordered by paths		
			var pathArray = this._dockersByPath.values()[0];
			if (pathArray) {
				pathArray.splice(this.dockers.indexOf(shape), 0, shape);
			}
			
			/* Perform nessary adjustments on the edge's child shapes */
			this.handleChildShapesAfterAddDocker(shape);
		}
	},
	
	/**
	 * Performs nessary adjustments on the edge's child shapes.
	 * 
	 * @param {ORYX.Core.Controls.Docker} docker
	 * 		The added docker
	 */
	handleChildShapesAfterAddDocker: function(docker) {
		/* Ensure type of Docker */
		if(!docker instanceof ORYX.Core.Controls.Docker) {return undefined;}
		
		var index = this.dockers.indexOf(docker);
		if(!(0 < index && index < this.dockers.length - 1)) {
		/* Exception: Expect added docker between first and last node of the edge */
			return undefined;
		} 
			
		/* Get child nodes concerning the segment of the new docker */
		var startDocker = this.dockers[index-1];
		var endDocker = this.dockers[index+1];
		
		/* Adjust the position of edge's child nodes */
		var segmentElements = 
			this.getAttachedNodePositionDataForSegment(startDocker, endDocker);
		
		var lengthSegmentPart1 = ORYX.Core.Math.getDistancePointToPoint(
										startDocker.bounds.center(),
										docker.bounds.center());
		var lengthSegmentPart2 = ORYX.Core.Math.getDistancePointToPoint(
										endDocker.bounds.center(),
										docker.bounds.center());
										
		if(!(lengthSegmentPart1 + lengthSegmentPart2)) {return;}
		
		var relativDockerPosition = lengthSegmentPart1 / (lengthSegmentPart1 + lengthSegmentPart2);
			
		segmentElements.each(function(nodePositionData) {
			/* Assign child node to the new segment */
			if(nodePositionData.value.relativDistanceFromDocker1 < relativDockerPosition) {
				/* Case: before added Docker */
				nodePositionData.value.segment.docker2 = docker;
				nodePositionData.value.relativDistanceFromDocker1 = 
					nodePositionData.value.relativDistanceFromDocker1 / relativDockerPosition;
			} else {
				/* Case: after added Docker */
				nodePositionData.value.segment.docker1 = docker;
				var newFullDistance = 1 - relativDockerPosition;
				var relativPartOfSegment = 
							nodePositionData.value.relativDistanceFromDocker1
							- relativDockerPosition;
				
				nodePositionData.value.relativDistanceFromDocker1 = 
										relativPartOfSegment / newFullDistance;
				
			}
		})
		
		
		// Update all labels reference points
		this.getLabels().each(function(label){

			var ref = label.getReferencePoint();
			if (!ref) {
				return;
			}
			var index = this.dockers.indexOf(docker);
			if (index >= ref.segment.fromIndex && index <= ref.segment.toIndex){
				
				var segment = this.findSegment(ref.intersection);
				if (!segment){ 
					// Choose whether the first of the last segment
					segment.fromDocker = ref.segment.fromIndex >= (this.dockers.length/2) ? this.dockers[0] : this.dockers[this.dockers.length-2]; 
					segment.toDocker = this.dockers[this.dockers.indexOf(from)+1]; // The next one if the to docker
				}
				
				var fromPosition = segment.fromDocker.bounds.center(), toPosition = segment.toDocker.bounds.center();
			
				var intersection = ORYX.Core.Math.getPointOfIntersectionPointLine(
										fromPosition, 		// P1 - Center of the first docker
										toPosition, 		// P2 - Center of the second docker
										ref.intersection, 	// P3 - Center of the label
										true);
				//var oldDistance = ORYX.Core.Math.getDistanceBetweenTwoPoints(ref.segment.fromPosition, ref.segment.toPosition, ref.intersection);
				//intersection = ORYX.Core.Math.getPointBetweenTwoPoints(fromPosition, toPosition, isNaN(oldDistance) ? 0.5 : (lengthOld*oldDistance)/lengthNew);
					
				// Update the reference point
				this.updateReferencePointOfLabel(label, intersection, segment.fromDocker, segment.toDocker, true);
			}
		}.bind(this));
		
		/* Update attached nodes visual representation */
		this.refreshAttachedNodes();
	},
	
	/**
	 *	Returns elements from {@link attachedNodePositiondata} that match the
	 *  segement defined by startDocker and endDocker.
	 *  
	 *  @param {ORYX.Core.Controls.Docker} startDocker
	 *  	The docker defining the begin of the segment.
	 *  @param {ORYX.Core.Controls.Docker} endDocker
	 *  	The docker defining the begin of the segment.
	 *  
	 *  @return {Hash} attachedNodePositionData
	 *  	Child elements matching the segment
	 */
	getAttachedNodePositionDataForSegment: function(startDocker, endDocker) {
		/* Ensure that the segment is defined correctly */
		if(!((startDocker instanceof ORYX.Core.Controls.Docker) 
			&& (endDocker instanceof ORYX.Core.Controls.Docker))) {
				return [];
			}
			
		/* Get elements of the segment */
		var elementsOfSegment = 
			this.attachedNodePositionData.findAll(function(nodePositionData) {
				return nodePositionData.value.segment.docker1 === startDocker &&
						nodePositionData.value.segment.docker2 === endDocker;
			});
		
		/* Return a Hash in each case */
		if(!elementsOfSegment) {return [];}
		
		return elementsOfSegment;
	},
	
	/**
	 * Removes an edge's child shape
	 */
	remove: function(shape) {
		arguments.callee.$.remove.apply(this, arguments);
		
		if(this.attachedNodePositionData[shape.getId()]) {
			delete this.attachedNodePositionData[shape.getId()];
		}
		
		/* Adjust child shapes if neccessary */
		if(shape instanceof ORYX.Core.Controls.Docker) {
			this.handleChildShapesAfterRemoveDocker(shape);
		}
	},
	
	updateReferencePointOfLabel: function(label, intersection, from, to, dirty){
		if (!label.getReferencePoint() || !label.isVisible) {
			return;
		}
		
		var ref = label.getReferencePoint();
		
		//
		if (ref.orientation && ref.orientation !== "ce"){
			var angle = this._getAngle(from, to);
			if (ref.distance >= 0){
				if(angle == 0){
					label.horizontalAlign("left");//ref.orientation == "lr" ? "right" : "left");
					label.verticalAlign("bottom");
				} else if (angle > 0 && angle < 90){
					label.horizontalAlign("right");
					label.verticalAlign("bottom");
				} else if (angle == 90){
					label.horizontalAlign("right");
					label.verticalAlign("top");//ref.orientation == "lr" ? "bottom" : "top");
				} else if (angle > 90 && angle < 180){
					label.horizontalAlign("right");
					label.verticalAlign("top");
				} else if (angle == 180){
					label.horizontalAlign("left");//ref.orientation == "ur" ? "right" : "left");
					label.verticalAlign("top");
				} else if (angle > 180 && angle < 270){
					label.horizontalAlign("left");
					label.verticalAlign("top");				
				} else if (angle == 270){
					label.horizontalAlign("left");
					label.verticalAlign("top");//ref.orientation == "ll" ? "bottom" : "top");
				} else if (angle > 270 && angle <= 360){
					label.horizontalAlign("left");
					label.verticalAlign("bottom");
				}
			} else {
				if(angle == 0){
					label.horizontalAlign("left");//ref.orientation == "ur" ? "right" : "left");
					label.verticalAlign("top");
				} else if (angle > 0 && angle < 90){
					label.horizontalAlign("left");
					label.verticalAlign("top");
				} else if (angle == 90){
					label.horizontalAlign("left");
					label.verticalAlign("top");//ref.orientation == "ll" ? "bottom" : "top");
				} else if (angle > 90 && angle < 180){
					label.horizontalAlign("left");
					label.verticalAlign("bottom");
				} else if (angle == 180){
					label.horizontalAlign("left");//ref.orientation == "lr" ? "right" : "left");
					label.verticalAlign("bottom");
				} else if (angle > 180 && angle < 270){
					label.horizontalAlign("right");
					label.verticalAlign("bottom");
				} else if (angle == 270){
					label.horizontalAlign("right");
					label.verticalAlign("top");//ref.orientation == "lr" ? "bottom" : "top");
				} else if (angle > 270 && angle <= 360){
					label.horizontalAlign("right");
					label.verticalAlign("top");
				}			
			}
			ref.iorientation = ref.iorientation || ref.orientation;
			ref.orientation = (label.verticalAlign()=="top"?"u":"l") + (label.horizontalAlign()=="left"?"l":"r");
		}
	
		label.setReferencePoint(jQuery.extend({},{
				intersection: intersection,
				segment: {
					from: from,
					fromIndex: this.dockers.indexOf(from),
					fromPosition: from.bounds.center(),
					to: to,
					toIndex: this.dockers.indexOf(to),
					toPosition: to.bounds.center()
				},
				dirty: dirty || false
			},ref))
	},
	/**
	 * 	Adjusts the child shapes of an edges after a docker was removed.
	 * 	
	 *  @param{ORYX.Core.Controls.Docker} docker
	 *  	The removed docker.
	 */
	handleChildShapesAfterRemoveDocker: function(docker) {
		/* Ensure docker type */
		if(!(docker instanceof ORYX.Core.Controls.Docker)) {return;}
		
		this.attachedNodePositionData.each(function(nodePositionData) {
			if(nodePositionData.value.segment.docker1 === docker) {
				/* The new start of the segment is the predecessor of docker2. */
				var index = this.dockers.indexOf(nodePositionData.value.segment.docker2);
				if(index == -1) {return;}
				nodePositionData.value.segment.docker1 = this.dockers[index - 1];
			} 
			else if(nodePositionData.value.segment.docker2 === docker) {
				/* The new end of the segment is the successor of docker1. */
				var index = this.dockers.indexOf(nodePositionData.value.segment.docker1);
				if(index == -1) {return;}
				nodePositionData.value.segment.docker2 = this.dockers[index + 1];
			}
		}.bind(this));
		
		// Update all labels reference points
		this.getLabels().each(function(label){

			var ref = label.getReferencePoint();
			if (!ref) {
				return;
			}
			var from = ref.segment.from;
			var to = ref.segment.to;
			
			if (from !== docker && to !== docker){ 
				return; 
			}
			
			var segment = this.findSegment(ref.intersection);
			if (!segment){ 
				from = segment.fromDocker;
				to = segment.toDocker;
			} else {
				from = from === docker ? this.dockers[this.dockers.indexOf(to)-1] : from;
				to = this.dockers[this.dockers.indexOf(from)+1];
			}

			var intersection = ORYX.Core.Math.getPointOfIntersectionPointLine(from.bounds.center(), to.bounds.center(), ref.intersection, true);			
			// Update the reference point
			this.updateReferencePointOfLabel(label, intersection, from, to, true);
		}.bind(this));
		
		/* Update attached nodes visual representation */
		this.refreshAttachedNodes();
	},
	
	/**
     *@deprecated Use the .createDocker() Method and set the point via the bounds
     */
    addDocker: function(position, exDocker){
        var lastDocker;
		var result;
        this._dockersByPath.any((function(pair){
            return pair.value.any((function(docker, index){
                if (!lastDocker) {
                    lastDocker = docker;
                    return false;
                }
                else {
                    var point1 = lastDocker.bounds.center();
                    var point2 = docker.bounds.center();
                    
                    var additionalIEZoom = 1;
                    if (!isNaN(screen.logicalXDPI) && !isNaN(screen.systemXDPI)) {
                        var ua = navigator.userAgent;
                        if (ua.indexOf('MSIE') >= 0) {
                            //IE 10 and below
                            var zoom = Math.round((screen.deviceXDPI / screen.logicalXDPI) * 100);
                            if (zoom !== 100) {
                                additionalIEZoom = zoom / 100
                            }
                        }
                    }
                    
                    if (additionalIEZoom !== 1) {
                        position.x = position.x / additionalIEZoom;
                        position.y = position.y / additionalIEZoom;
                    }
                    
                    if (ORYX.Core.Math.isPointInLine(position.x, position.y, point1.x, point1.y, point2.x, point2.y, 10)) {
                        var path = this._paths.find(function(path){
                            return path.id === pair.key;
                        });
                        if (path) {
                            var allowAttr = path.getAttributeNS(NAMESPACE_ORYX, 'allowDockers');
                            if (allowAttr && allowAttr.toLowerCase() === "no") {
                                return true;
                            }
                        }
                        
                        var newDocker = (exDocker) ? exDocker : this.createDocker(this.dockers.indexOf(lastDocker) + 1, position);
						newDocker.bounds.centerMoveTo(position);
                        if(exDocker)
							this.add(newDocker, this.dockers.indexOf(lastDocker) + 1);
						result = newDocker;
                        return true;
                    }
                    else {
                        lastDocker = docker;
                        return false;
                    }
                }
            }).bind(this));
        }).bind(this));
		return result;
    },
    
    removeDocker: function(docker){
        if (this.dockers.length > 2 && !(this.dockers.first() === docker)) {
            this._dockersByPath.any((function(pair){
                if (pair.value.member(docker)) {
                    if (docker === pair.value.last()) {
                        return true;
                    }
                    else {
                        this.remove(docker);
                        this._dockersByPath[pair.key] = pair.value.without(docker);
                        this.isChanged = true;
                        this._dockerChanged();
                        return true;
                    }
                }
                return false;
            }).bind(this));
        }
    },
	
	/**
	 * Removes all dockers from the edge which are on 
	 * the line between two dockers
	 * @return {Object} Removed dockers in an indicied array 
	 * (key is the removed position of the docker, value is docker themselve)
	 */
	removeUnusedDockers:function(){
		var marked = $H({});
		
		this.dockers.each(function(docker, i){
			if (i==0||i==this.dockers.length-1){ return }
			var previous = this.dockers[i-1];
			
			/* Do not consider already removed dockers */
			if(marked.values().indexOf(previous) != -1 && this.dockers[i-2]) {
				previous = this.dockers[i-2];
			}
			var next = this.dockers[i+1];
			
			var cp = previous.getDockedShape() && previous.referencePoint ? previous.getAbsoluteReferencePoint() : previous.bounds.center();
			var cn = next.getDockedShape() && next.referencePoint ? next.getAbsoluteReferencePoint() : next.bounds.center();
			var cd = docker.bounds.center();
			
			if (ORYX.Core.Math.isPointInLine(cd.x, cd.y, cp.x, cp.y, cn.x, cn.y, 1)){
				marked[i] = docker;
			}
		}.bind(this))
		
		marked.each(function(docker){
			this.removeDocker(docker.value);
		}.bind(this))
		
		if (marked.values().length > 0){
			this._update(true);
		}
		
		return marked;
	},
    
    /**
     * Initializes the Edge after loading the SVG representation of the edge.
     * @param {SVGDocument} svgDocument
     */
    _init: function(svgDocument){
        arguments.callee.$._init.apply(this, arguments);
        
        var minPointX, minPointY, maxPointX, maxPointY;
        
        //init markers
        var defs = svgDocument.getElementsByTagNameNS(NAMESPACE_SVG, "defs");
        if (defs.length > 0) {
            defs = defs[0];
            var markerElements = $A(defs.getElementsByTagNameNS(NAMESPACE_SVG, "marker"));
            var marker;
            var me = this;
            markerElements.each(function(markerElement){
                try {
                    marker = new ORYX.Core.SVG.SVGMarker(markerElement.cloneNode(true));
                    me._markers[marker.id] = marker;
                    var textElements = $A(marker.element.getElementsByTagNameNS(NAMESPACE_SVG, "text"));
                    var label;
                    textElements.each(function(textElement){
                        label = new ORYX.Core.SVG.Label({
                            textElement: textElement,
							shapeId: this.id
                        });
                        me._labels[label.id] = label;
                    });
                } 
                catch (e) {
                }
            });
        }
        
        
        var gs = svgDocument.getElementsByTagNameNS(NAMESPACE_SVG, "g");
        if (gs.length <= 0) {
            throw "Edge: No g element found.";
        }
        var g = gs[0];
        
        
        g.setAttributeNS(null, "id", null);
        
        var isFirst = true;
        
        $A(g.childNodes).each((function(path, index){
            if (ORYX.Editor.checkClassType(path, SVGPathElement)) {
                path = path.cloneNode(false);
                
                var pathId = this.id + "_" + index;
                path.setAttributeNS(null, "id", pathId);
                this._paths.push(path);
                
                //check, if markers are set and update the id
                var markersByThisPath = [];
                var markerUrl = path.getAttributeNS(null, "marker-start");
                
                if (markerUrl && markerUrl !== "") {
                    markerUrl = markerUrl.strip();
                    markerUrl = markerUrl.replace(/^url\(#/, '');
                    
                    var markerStartId = this.getValidMarkerId(markerUrl);
                    path.setAttributeNS(null, "marker-start", "url(#" + markerStartId + ")");
                    
                    markersByThisPath.push(this._markers[markerStartId]);
                }
                
                markerUrl = path.getAttributeNS(null, "marker-mid");
                
                if (markerUrl && markerUrl !== "") {
                    markerUrl = markerUrl.strip();
                    markerUrl = markerUrl.replace(/^url\(#/, '');
                    var markerMidId = this.getValidMarkerId(markerUrl);
                    path.setAttributeNS(null, "marker-mid", "url(#" + markerMidId + ")");
                    
                    markersByThisPath.push(this._markers[markerMidId]);
                }
                
                markerUrl = path.getAttributeNS(null, "marker-end");
                
                if (markerUrl && markerUrl !== "") {
                    markerUrl = markerUrl.strip();
                    
                    var markerEndId = this.getValidMarkerId(markerUrl);
                    path.setAttributeNS(null, "marker-end", "url(#" + markerEndId + ")");
                    
                    markersByThisPath.push(this._markers[markerEndId]);
                }
                
                this._markersByPath[pathId] = markersByThisPath;
                
                //init dockers
                var parser = new PathParser();
                var handler = new ORYX.Core.SVG.PointsPathHandler();
                parser.setHandler(handler);
                parser.parsePath(path);
                
                if (handler.points.length < 4) {
                    throw "Edge: Path has to have two or more points specified.";
                }
                
                this._dockersByPath[pathId] = [];
                
				for (var i = 0; i < handler.points.length; i += 2) {
					//handler.points.each((function(point, pIndex){
					var x = handler.points[i];
					var y = handler.points[i+1];
					if (isFirst || i > 0) {
						var docker = new ORYX.Core.Controls.Docker({
							eventHandlerCallback: this.eventHandlerCallback
						});
						docker.bounds.centerMoveTo(x,y);
						docker.bounds.registerCallback(this._dockerChangedCallback);
						this.add(docker, this.dockers.length);
						
						//this._dockersByPath[pathId].push(docker);
						
						//calculate minPoint and maxPoint
						if (minPointX) {
							minPointX = Math.min(x, minPointX);
							minPointY = Math.min(y, minPointY);
						}
						else {
							minPointX = x;
							minPointY = y;
						}
						
						if (maxPointX) {
							maxPointX = Math.max(x, maxPointX);
							maxPointY = Math.max(y, maxPointY);
						}
						else {
							maxPointX = x;
							maxPointY = y;
						}
					}
					//}).bind(this));
				}
                isFirst = false;
            }
        }).bind(this));
        
        this.bounds.set(minPointX, minPointY, maxPointX, maxPointY);
        
        if (false&&(this.bounds.width() === 0 || this.bounds.height() === 0)) {
			var width = this.bounds.width();
			var height = this.bounds.height();
			
            this.bounds.extend({
                x: width === 0 ? 2 : 0,
                y: height === 0 ? 2 : 0
            });
            
            this.bounds.moveBy({
                x: width === 0 ? -1 : 0,
                y: height === 0 ? -1 : 0
            });
            
        }
        
        this._oldBounds = this.bounds.clone();
        
        //add paths to this.node
        this._paths.reverse();
        var paths = [];
        this._paths.each((function(path){
            paths.push(this.node.childNodes[0].childNodes[0].childNodes[0].appendChild(path));
        }).bind(this));
        
        this._paths = paths;
        
        //init interaction path
        this._paths.each((function(path){
            var iPath = path.cloneNode(false);
			iPath.setAttributeNS(null, "id", undefined);
            iPath.setAttributeNS(null, "stroke-width", 10);
            iPath.setAttributeNS(null, "visibility", "hidden");
            iPath.setAttributeNS(null, "stroke-dasharray", null);
            iPath.setAttributeNS(null, "stroke", "black");
            iPath.setAttributeNS(null, "fill", "none");
			iPath.setAttributeNS(null, "title", this.getStencil().title());
            this._interactionPaths.push(this.node.childNodes[0].childNodes[0].childNodes[0].appendChild(iPath));
        }).bind(this));
        
        this._paths.reverse();
        this._interactionPaths.reverse();
		
		/**initialize labels*/
        var textElems = svgDocument.getElementsByTagNameNS(ORYX.CONFIG.NAMESPACE_SVG, 'text');
        
		$A(textElems).each((function(textElem){
            var label = new ORYX.Core.SVG.Label({
                textElement: textElem,
				shapeId: this.id
            });
            this.node.childNodes[0].childNodes[0].appendChild(label.node);
            this._labels[label.id] = label;
			
			label.registerOnChange(this.layout.bind(this));
        }).bind(this)); 
		
        
        this.propertiesChanged.each(function(pair){
            pair.value = true;
        });
        
        
        //if(this.dockers.length == 2) {
        	
        	
        	
      //  }
		
        //this._update(true);
    },
    
    getValidMarkerId: function(markerUrl) {
    	if(markerUrl.indexOf("url(\"#") >= 0) {
            // Fix for IE9, additional quotes are added to the <id
            var rawId = markerUrl.replace(/^url\(\"#/, "").replace(/\"\)$/, '');
            return this.id + rawId;
          } else {
            markerUrl = markerUrl.replace(/^url\(#/, '');
            return this.id.concat(markerUrl.replace(/\)$/, ''));
          }
    },
    
    /**
     * Adds all necessary markers of this Edge to the SVG document.
     * Has to be called, while this.node is part of DOM.
     */
    addMarkers: function(defs){
        this._markers.each(function(marker){
            if (!defs.ownerDocument.getElementById(marker.value.id)) {
                marker.value.element = defs.appendChild(marker.value.element);
            }
        });
    },
    
    /**
     * Removes all necessary markers of this Edge from the SVG document.
     * Has to be called, while this.node is part of DOM.
     */
    removeMarkers: function(){
        var svgElement = this.node.ownerSVGElement;
        if (svgElement) {
            var defs = svgElement.getElementsByTagNameNS(NAMESPACE_SVG, "defs");
            if (defs.length > 0) {
                defs = defs[0];
                this._markers.each(function(marker){
                    var foundMarker = defs.ownerDocument.getElementById(marker.value.id);
                    if (foundMarker) {
                        marker.value.element = defs.removeChild(marker.value.element);
                    }
                });
            }
        }
    },
    
    /**
     * Calls when a docker has changed
     */
    _dockerChanged: function(){
    
        //this._update(true);
		this._dockerUpdated = true;
        
    },
    
    serialize: function(){
        var result = arguments.callee.$.serialize.apply(this);
        
        //add dockers triple
        var value = "";
        this._dockersByPath.each((function(pair){
            pair.value.each(function(docker){
                var position = docker.getDockedShape() && docker.referencePoint ? docker.referencePoint : docker.bounds.center();
                value = value.concat(position.x + " " + position.y + " ");
            });
            
            value += " # ";
        }).bind(this));
        result.push({
            name: 'dockers',
            prefix: 'oryx',
            value: value,
            type: 'literal'
        });
        
        //add parent triple dependant on the dockedShapes
        //TODO change this when canvas becomes a resource
/*        var source = this.dockers.first().getDockedShape();
        var target = this.dockers.last().getDockedShape();
        var sharedParent;
        if (source && target) {
            //get shared parent
            while (source.parent) {
                source = source.parent;
                if (source instanceof ORYX.Core.Canvas) {
                    sharedParent = source;
                    break;
                }
                else {
                    var targetParent = target.parent;
                    var found;
                    while (targetParent) {
                        if (source === targetParent) {
                            sharedParent = source;
                            found = true;
                            break;
                        }
                        else {
                            targetParent = targetParent.parent;
                        }
                    }
                    if (found) {
                        break;
                    }
                }
            }
        }
        else 
            if (source) {
                sharedParent = source.parent;
            }
            else 
                if (target) {
                    sharedParent = target.parent;
                }
*/        
        //if (sharedParent) {
/*            result.push({
                name: 'parent',
                prefix: 'raziel',
                //value: '#' + ERDF.__stripHashes(sharedParent.resourceId),
                value: '#' + ERDF.__stripHashes(this.getCanvas().resourceId),
                type: 'resource'
            });*/
        //}
		
		//serialize target and source
		var lastDocker = this.dockers.last();
		
		var target = lastDocker.getDockedShape();
		
		if(target) {
			result.push({
				name: 'target',
				prefix: 'raziel',
				value: '#' + ERDF.__stripHashes(target.resourceId),
				type: 'resource'
			});
		}
        
        try {
            //result = this.getStencil().serialize(this, result);
			var serializeEvent = this.getStencil().serialize();
			
			/*
			 * call serialize callback by reference, result should be found
			 * in serializeEvent.result
			 */
			if(serializeEvent.type) {
				serializeEvent.shape = this;
				serializeEvent.data = result;
				serializeEvent.result = undefined;
				serializeEvent.forceExecution = true;
				
				this._delegateEvent(serializeEvent);
				
				if(serializeEvent.result) {
					result = serializeEvent.result;
				}
			}
        } 
        catch (e) {
        }
        return result;
    },
    
    deserialize: function(data){
        try {
            //data = this.getStencil().deserialize(this, data);
			
			var deserializeEvent = this.getStencil().deserialize();
			
			/*
			 * call serialize callback by reference, result should be found
			 * in serializeEventInfo.result
			 */
			if(deserializeEvent.type) {
				deserializeEvent.shape = this;
				deserializeEvent.data = data;
				deserializeEvent.result = undefined;
				deserializeEvent.forceExecution = true;
				
				this._delegateEvent(deserializeEvent);
				if(deserializeEvent.result) {
					data = deserializeEvent.result;
				}
			}
        } 
        catch (e) {
        }
        
		// Set the outgoing shapes
		var target = data.find(function(ser) {return (ser.prefix+"-"+ser.name) == 'raziel-target'});
		var targetShape;
		if(target) {
			targetShape = this.getCanvas().getChildShapeByResourceId(target.value);
		}
		
		var outgoing = data.findAll(function(ser){ return (ser.prefix+"-"+ser.name) == 'raziel-outgoing'});
		outgoing.each((function(obj){
			// TODO: Look at Canvas
			if(!this.parent) {return};
								
			// Set outgoing Shape
			var next = this.getCanvas().getChildShapeByResourceId(obj.value);
															
			if(next){
				if(next == targetShape) {
					// If this is an edge, set the last docker to the next shape
					this.dockers.last().setDockedShape(next);
					this.dockers.last().setReferencePoint({x: next.bounds.width() / 2.0, y: next.bounds.height() / 2.0});
				} else if(next instanceof ORYX.Core.Edge) {
					//Set the first docker of the next shape
					next.dockers.first().setDockedShape(this);
					//next.dockers.first().setReferencePoint({x: this.bounds.width() / 2.0, y: this.bounds.height() / 2.0});
				} /*else if(next.dockers.length > 0) { //next is a node and next has a docker
					next.dockers.first().setDockedShape(this);
					next.dockers.first().setReferencePoint({x: this.bounds.width() / 2.0, y: this.bounds.height() / 2.0});
				}*/
			}	
			
		}).bind(this));
		
        
        var oryxDockers = data.find(function(obj){
            return (obj.prefix === "oryx" &&
            obj.name === "dockers");
        });
		
        if (oryxDockers) {
            var dataByPath = oryxDockers.value.split("#").without("").without(" ");
            
            dataByPath.each((function(data, index){
                var values = data.replace(/,/g, " ").split(" ").without("");
                
                //for each docker two values must be defined
                if (values.length % 2 === 0) {
                    var path = this._paths[index];
                    
                    if (path) {
                        if (index === 0) {
                            while (this._dockersByPath[path.id].length > 2) {
                                this.removeDocker(this._dockersByPath[path.id][1]);
                            }
                        }
                        else {
                            while (this._dockersByPath[path.id].length > 1) {
                                this.removeDocker(this._dockersByPath[path.id][0]);
                            }
                        }
                        
                        var dockersByPath = this._dockersByPath[path.id];
                        
                        if (index === 0) {
                            //set position of first docker
                            var x = parseFloat(values.shift());
                            var y = parseFloat(values.shift());
                            
                            if (dockersByPath.first().getDockedShape()) {
                                dockersByPath.first().setReferencePoint({
                                    x: x,
                                    y: y
                                });
                            }
                            else {
                                dockersByPath.first().bounds.centerMoveTo(x, y);
                            }
                        }
                        
                        //set position of last docker
                        y = parseFloat(values.pop());
                        x = parseFloat(values.pop());
                        
                        if (dockersByPath.last().getDockedShape()) {
                            dockersByPath.last().setReferencePoint({
                                x: x,
                                y: y
                            });
                        } else {
                            dockersByPath.last().bounds.centerMoveTo(x, y);
                        }
                        
                        //add additional dockers
                        for (var i = 0; i < values.length; i++) {
                            x = parseFloat(values[i]);
                            y = parseFloat(values[++i]);
                            
                            var newDocker = this.createDocker();
                            newDocker.bounds.centerMoveTo(x, y);
                            
                            //this.dockers = this.dockers.without(newDocker);
                            //this.dockers.splice(this.dockers.indexOf(dockersByPath.last()), 0, newDocker);
                            //dockersByPath.splice(this.dockers.indexOf(dockersByPath.last()), 0, newDocker);
						}
                    }
                }
            }).bind(this));
        } else {
            this.alignDockers();
        }
		
        arguments.callee.$.deserialize.apply(this, arguments);
		
		this._changed();
    },
    
    toString: function(){
        return this.getStencil().title() + " " + this.id;
    },
    
    /**
     * @return {ORYX.Core.Shape} Returns last docked shape or null.
     */
    getTarget: function(){
        return this.dockers.last() ? this.dockers.last().getDockedShape() : null;
    },
	
	/**
	 * @return {ORYX.Core.Shape} Returns the first docked shape or null
	 */
	getSource: function() {
		return this.dockers.first() ? this.dockers.first().getDockedShape() : null;
	},
	
	/**
	 * Checks whether the edge is at least docked to one shape.
	 * 
	 * @return {boolean} True if edge is docked
	 */
	isDocked: function() {
		var isDocked = false;
		this.dockers.each(function(docker) {
			if(docker.isDocked()) {
				isDocked = true;
				throw $break;
			}
		});
		return isDocked;
	},
    
    /**
     * Calls {@link ORYX.Core.AbstractShape#toJSON} and add a some stencil set information.
     */
    toJSON: function() {
        var json = arguments.callee.$.toJSON.apply(this, arguments);
        
        if(this.getTarget()) {
            json.target = {
                resourceId: this.getTarget().resourceId
            };
        }
        
        return json;
    }
};
ORYX.Core.Edge = ORYX.Core.Shape.extend(ORYX.Core.Edge);
/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

if(!ORYX){ var ORYX = {} }
if(!ORYX.Plugins){ ORYX.Plugins = {} }

/**
   This abstract plugin class can be used to build plugins on.
   It provides some more basic functionality like registering events (on*-handlers)...
   @example
    ORYX.Plugins.MyPlugin = ORYX.Plugins.AbstractPlugin.extend({
        construct: function() {
            // Call super class constructor
            arguments.callee.$.construct.apply(this, arguments);
            
            [...]
        },
        [...]
    });
   
   @class ORYX.Plugins.AbstractPlugin
   @constructor Creates a new instance
   @author Willi Tscheschner
*/
ORYX.Plugins.AbstractPlugin = Clazz.extend({
    /** 
     * The facade which offer editor-specific functionality
     * @type Facade
     * @memberOf ORYX.Plugins.AbstractPlugin.prototype
     */
	facade: null,
	
	construct: function( facade ){
		this.facade = facade;
		
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_LOADED, this.onLoaded.bind(this));
	},
        
    /**
       Overwrite to handle load event. TODO: Document params!!!
       @methodOf ORYX.Plugins.AbstractPlugin.prototype
    */
	onLoaded: function(){},
	
    /**
       Overwrite to handle selection changed event. TODO: Document params!!!
       @methodOf ORYX.Plugins.AbstractPlugin.prototype
    */
	onSelectionChanged: function(){},
	
    /**
       Show overlay on given shape.
       @methodOf ORYX.Plugins.AbstractPlugin.prototype
       @example
       showOverlay(
           myShape,
           { stroke: "green" },
           ORYX.Editor.graft("http://www.w3.org/2000/svg", null, ['path', {
               "title": "Click the element to execute it!",
               "stroke-width": 2.0,
               "stroke": "black",
               "d": "M0,-5 L5,0 L0,5 Z",
               "line-captions": "round"
           }])
       )
       @param {Oryx.XXX.Shape[]} shapes One shape or array of shapes the overlay should be put on
       @param {Oryx.XXX.Attributes} attributes some attributes...
       @param {Oryx.svg.node} svgNode The svg node which should be used as overlay
       @param {String} [svgNode="NW"] The svg node position where the overlay should be placed
    */
	showOverlay: function(shapes, attributes, svgNode, svgNodePosition ){
		
		if( !(shapes instanceof Array) ){
			shapes = [shapes]
		}
		
		// Define Shapes
		shapes = shapes.map(function(shape){
			var el = shape;
			if( typeof shape == "string" ){
				el = this.facade.getCanvas().getChildShapeByResourceId( shape );
				el = el || this.facade.getCanvas().getChildById( shape, true );
			}
			return el;
		}.bind(this)).compact();
		
		// Define unified id
		if( !this.overlayID ){
			this.overlayID = this.type + ORYX.Editor.provideId();
		}
		
		this.facade.raiseEvent({
			type		: ORYX.CONFIG.EVENT_OVERLAY_SHOW,
			id			: this.overlayID,
			shapes		: shapes,
			attributes 	: attributes,
			node		: svgNode,
			nodePosition: svgNodePosition || "NW"
		});
		
	},
	
    /**
       Hide current overlay.
       @methodOf ORYX.Plugins.AbstractPlugin.prototype
    */
	hideOverlay: function(){
		this.facade.raiseEvent({
			type	: ORYX.CONFIG.EVENT_OVERLAY_HIDE,
			id		: this.overlayID
		});		
	},
	
    /**
       Does a transformation with the given xslt stylesheet.
       @methodOf ORYX.Plugins.AbstractPlugin.prototype
       @param {String} data The data (e.g. eRDF) which should be transformed
       @param {String} stylesheet URL of a stylesheet which should be used for transforming data.
    */
	doTransform: function( data, stylesheet ) {		
		
		if( !stylesheet || !data ){
			return ""
		}

        var parser 		= new DOMParser();
        var parsedData 	= parser.parseFromString(data, "text/xml");
		source=stylesheet;
		new Ajax.Request(source, {
			asynchronous: false,
			method: 'get',
			onSuccess: function(transport){
				xsl = transport.responseText
			}.bind(this),
			onFailure: (function(transport){
				ORYX.Log.error("XSL load failed" + transport);
			}).bind(this)
		});
        var xsltProcessor = new XSLTProcessor();
		var domParser = new DOMParser();
		var xslObject = domParser.parseFromString(xsl, "text/xml");
        xsltProcessor.importStylesheet(xslObject);
        
        try {
        	
            var newData 		= xsltProcessor.transformToFragment(parsedData, document);
            var serializedData 	= (new XMLSerializer()).serializeToString(newData);
            
           	/* Firefox 2 to 3 problem?! */
            serializedData = !serializedData.startsWith("<?xml") ? "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + serializedData : serializedData;
            
            return serializedData;
            
        }catch (error) {
            return -1;
        }
        
	},
	
	/**
	 * Opens a new window that shows the given XML content.
	 * @methodOf ORYX.Plugins.AbstractPlugin.prototype
	 * @param {Object} content The XML content to be shown.
	 * @example
	 * openDownloadWindow( "my.xml", "<exampleXML />" );
	 */
	openXMLWindow: function(content) {
		var win = window.open(
		   'data:application/xml,' + encodeURIComponent(
		     content
		   ),
		   '_blank', "resizable=yes,width=600,height=600,toolbar=0,scrollbars=yes"
		);
	},
	
    /**
     * Opens a download window for downloading the given content.
     * @methodOf ORYX.Plugins.AbstractPlugin.prototype
     * @param {String} filename The content's file name
     * @param {String} content The content to download
     */
	openDownloadWindow: function(filename, content) {
		var win = window.open("");
		if (win != null) {
			win.document.open();
			win.document.write("<html><body>");
			var submitForm = win.document.createElement("form");
			win.document.body.appendChild(submitForm);
			
			var createHiddenElement = function(name, value) {
				var newElement = document.createElement("input");
				newElement.name=name;
				newElement.type="hidden";
				newElement.value = value;
				return newElement
			}
			
			submitForm.appendChild( createHiddenElement("download", content) );
			submitForm.appendChild( createHiddenElement("file", filename) );
			
			
			submitForm.method = "POST";
			win.document.write("</body></html>");
			win.document.close();
			submitForm.action= ORYX.PATH + "/download";
			submitForm.submit();
		}		
	},
    
    /**
     * Serializes DOM.
     * @methodOf ORYX.Plugins.AbstractPlugin.prototype
     * @type {String} Serialized DOM
     */
    getSerializedDOM: function(){
        // Force to set all resource IDs
        var serializedDOM = DataManager.serializeDOM( this.facade );

        //add namespaces
        serializedDOM = '<?xml version="1.0" encoding="utf-8"?>' +
        '<html xmlns="http://www.w3.org/1999/xhtml" ' +
        'xmlns:b3mn="http://b3mn.org/2007/b3mn" ' +
        'xmlns:ext="http://b3mn.org/2007/ext" ' +
        'xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" ' +
        'xmlns:atom="http://b3mn.org/2007/atom+xhtml">' +
        '<head profile="http://purl.org/NET/erdf/profile">' +
        '<link rel="schema.dc" href="http://purl.org/dc/elements/1.1/" />' +
        '<link rel="schema.dcTerms" href="http://purl.org/dc/terms/ " />' +
        '<link rel="schema.b3mn" href="http://b3mn.org" />' +
        '<link rel="schema.oryx" href="http://oryx-editor.org/" />' +
        '<link rel="schema.raziel" href="http://raziel.org/" />' +
        '<base href="' +
        location.href.split("?")[0] +
        '" />' +
        '</head><body>' +
        serializedDOM +
        '</body></html>';
        
        return serializedDOM;
    },
    
    /**
     * Sets the editor in read only mode: Edges/ dockers cannot be moved anymore,
     * shapes cannot be selected anymore.
     * @methodOf ORYX.Plugins.AbstractPlugin.prototype
     */
    enableReadOnlyMode: function(){
        //Edges cannot be moved anymore
        this.facade.disableEvent(ORYX.CONFIG.EVENT_MOUSEDOWN);
        
        // Stop the user from editing the diagram while the plugin is active
        this._stopSelectionChange = function(){
            if(this.facade.getSelection().length > 0) {
                this.facade.setSelection([]);
            }
        };
        this.facade.registerOnEvent(ORYX.CONFIG.EVENT_SELECTION_CHANGED, this._stopSelectionChange.bind(this));
    },
    /**
     * Disables read only mode, see @see
     * @methodOf ORYX.Plugins.AbstractPlugin.prototype
     * @see ORYX.Plugins.AbstractPlugin.prototype.enableReadOnlyMode
     */
    disableReadOnlyMode: function(){
        // Edges can be moved now again
        this.facade.enableEvent(ORYX.CONFIG.EVENT_MOUSEDOWN);
        
        if (this._stopSelectionChange) {
            this.facade.unregisterOnEvent(ORYX.CONFIG.EVENT_SELECTION_CHANGED, this._stopSelectionChange.bind(this));
            this._stopSelectionChange = undefined;
        }
    },
    
    /**
     * Extracts RDF from DOM.
     * @methodOf ORYX.Plugins.AbstractPlugin.prototype
     * @type {String} Extracted RFD. Null if there are transformation errors.
     */
    getRDFFromDOM: function(){
        //convert to RDF
		try {
			var xsl = "";
			source=ORYX.PATH + "lib/extract-rdf.xsl";
			new Ajax.Request(source, {
				asynchronous: false,
				method: 'get',
				onSuccess: function(transport){
					xsl = transport.responseText
				}.bind(this),
				onFailure: (function(transport){
					ORYX.Log.error("XSL load failed" + transport);
				}).bind(this)
			});
			
			var domParser = new DOMParser();
			var xmlObject = domParser.parseFromString(this.getSerializedDOM(), "text/xml");
			var xslObject = domParser.parseFromString(xsl, "text/xml");
			var xsltProcessor = new XSLTProcessor();
			xsltProcessor.importStylesheet(xslObject);
			var result = xsltProcessor.transformToFragment(xmlObject, document);
			
			var serializer = new XMLSerializer();
			
			return serializer.serializeToString(result);
		} catch(e){
			console.log("error serializing " + e);
			return "";
		}

		
    },
    
    /**
	 * Checks if a certain stencil set is loaded right now.
	 * 
	 */
	isStencilSetExtensionLoaded: function(stencilSetExtensionNamespace) {
		return this.facade.getStencilSets().values().any(
			function(ss){ 
				return ss.extensions().keys().any(
					function(extensionKey) {
						return extensionKey == stencilSetExtensionNamespace;
					}.bind(this)
				);
			}.bind(this)
		);
	},
	
	/**
	 * Raises an event so that registered layouters does
	 * have the posiblility to layout the given shapes 
	 * For further reading, have a look into the AbstractLayouter
	 * class
	 * @param {Object} shapes
	 */
	doLayout: function(shapes){
		// Raises a do layout event
		if (this.facade.raiseEvent)
		{
			this.facade.raiseEvent({
				type		: ORYX.CONFIG.EVENT_LAYOUT,
				shapes		: shapes
			});
		}
		else
		{
			this.facade.handleEvents({
				type		: ORYX.CONFIG.EVENT_LAYOUT,
				shapes		: shapes
			});
		}
	},
	
	
	/**
	 * Does a primitive layouting with the incoming/outgoing 
	 * edges (set the dockers to the right position) and if 
	 * necessary, it will be called the real layouting 
	 * @param {ORYX.Core.Node} node
	 * @param {Array} edges
	 */
	layoutEdges : function(node, allEdges, offset){		

		if (!this.facade.isExecutingCommands()){ return }		

		var Command = ORYX.Core.Command.extend({
			construct: function(edges, node, offset, plugin){
				this.edges = edges;
				this.node = node;
				this.plugin = plugin;
				this.offset = offset;
				
				// Get the new absolute center
				var center = node.absoluteXY();
				this.ulo = {x: center.x - offset.x, y:center.y - offset.y};
				
				
			},
			execute: function(){
				
				if (this.changes){
					this.executeAgain();
					return;
				} else {
					this.changes = [];
					this.edges.each(function(edge){
						this.changes.push({
							edge: edge,
							oldDockerPositions: edge.dockers.map(function(r){ return r.bounds.center() })
						})
					}.bind(this));
				}
				
				// Find all edges, which are related to the node and
				// have more than two dockers
				this.edges
					// Find all edges with more than two dockers
					.findAll(function(r){ return r.dockers.length > 2 }.bind(this))
					// For every edge, check second and one before last docker
					// if there are horizontal/vertical on the same level
					// and if so, align the the bounds 
					.each(function(edge){
						if (edge.dockers.first().getDockedShape() === this.node){
							var second = edge.dockers[1];
							if (this.align(second.bounds, edge.dockers.first())){ second.update(); }
						} else if (edge.dockers.last().getDockedShape() === this.node) {
							var beforeLast = edge.dockers[edge.dockers.length-2];
							if (this.align(beforeLast.bounds, edge.dockers.last())){ beforeLast.update(); }									
						}
						edge._update(true);
						edge.removeUnusedDockers();
						if (this.isBendPointIncluded(edge)){
							this.plugin.doLayout(edge);
							return;
						}
					}.bind(this));
				
				
				// Find all edges, which have only to dockers 
				// and is located horizontal/vertical.
				// Do layout with those edges
				this.edges
					// Find all edges with exactly two dockers
					.each(function(edge){
						if (edge.dockers.length == 2){
							var p1 = edge.dockers.first().getAbsoluteReferencePoint() || edge.dockers.first().bounds.center();
							var p2 = edge.dockers.last().getAbsoluteReferencePoint() || edge.dockers.first().bounds.center();
							// Find all horizontal/vertical edges
							if (Math.abs(-Math.abs(p1.x - p2.x) + Math.abs(this.offset.x)) < 2 || Math.abs(-Math.abs(p1.y - p2.y) + Math.abs(this.offset.y)) < 2){
								this.plugin.doLayout(edge);
							}
						}
					}.bind(this));
		
				this.edges.each(function(edge, i){
					this.changes[i].dockerPositions = edge.dockers.map(function(r){ return r.bounds.center() });
				}.bind(this));
				
			},
			/**
			 * Align the bounds if the center is 
			 * the same than the old center
			 * @params {Object} bounds
			 * @params {Object} bounds2
			 */
			align: function(bounds, refDocker){
				
				var abRef = refDocker.getAbsoluteReferencePoint() || refDocker.bounds.center();
				
				var xdif = bounds.center().x-abRef.x;
				var ydif = bounds.center().y-abRef.y;
				if (Math.abs(-Math.abs(xdif) + Math.abs(this.offset.x)) < 3 && this.offset.xs === undefined){
					bounds.moveBy({x:-xdif, y:0})
				}
				if (Math.abs(-Math.abs(ydif) + Math.abs(this.offset.y)) < 3 && this.offset.ys === undefined){
					bounds.moveBy({y:-ydif, x:0})
				}
				
				if (this.offset.xs !== undefined || this.offset.ys !== undefined){
					var absPXY = refDocker.getDockedShape().absoluteXY();
					xdif = bounds.center().x-(absPXY.x+((abRef.x-absPXY.x)/this.offset.xs));
					ydif = bounds.center().y-(absPXY.y+((abRef.y-absPXY.y)/this.offset.ys));
					
					if (Math.abs(-Math.abs(xdif) + Math.abs(this.offset.x)) < 3){
						bounds.moveBy({x:-(bounds.center().x-abRef.x), y:0})
					}
					
					if (Math.abs(-Math.abs(ydif) + Math.abs(this.offset.y)) < 3){
						bounds.moveBy({y:-(bounds.center().y-abRef.y), x:0})
					}
				}
			},
			
			/**						
			 * Returns a TRUE if there are bend point which overlay the shape
			 */
			isBendPointIncluded: function(edge){
				// Get absolute bounds
				var ab = edge.dockers.first().getDockedShape();
				var bb = edge.dockers.last().getDockedShape();
				
				if (ab) {
					ab = ab.absoluteBounds();
					ab.widen(5);
				}
				
				if (bb) {
					bb = bb.absoluteBounds();
					bb.widen(20); // Wide with 20 because of the arrow from the edge
				}
				
				return edge.dockers
						.any(function(docker, i){ 
							var c = docker.bounds.center();
									// Dont count first and last
							return 	i != 0 && i != edge.dockers.length-1 && 
									// Check if the point is included to the absolute bounds
									((ab && ab.isIncluded(c)) || (bb && bb.isIncluded(c)))
						})
			},
			
			removeAllDocker: function(edge){
				edge.dockers.slice(1, edge.dockers.length-1).each(function(docker){
					edge.removeDocker(docker);
				})
			},
			executeAgain: function(){
				this.changes.each(function(change){
					// Reset the dockers
					this.removeAllDocker(change.edge);
					change.dockerPositions.each(function(pos, i){	
						if (i==0||i==change.dockerPositions.length-1){ return }					
						var docker = change.edge.createDocker(undefined, pos);
						docker.bounds.centerMoveTo(pos);
						docker.update();
					}.bind(this));
					change.edge._update(true);
				}.bind(this));
			},
			rollback: function(){					
				this.changes.each(function(change){
					// Reset the dockers
					this.removeAllDocker(change.edge);
					change.oldDockerPositions.each(function(pos, i){	
						if (i==0||i==change.oldDockerPositions.length-1){ return }					
						var docker = change.edge.createDocker(undefined, pos);
						docker.bounds.centerMoveTo(pos);
						docker.update();
					}.bind(this));
					change.edge._update(true);
				}.bind(this));
			}
		});
		
		this.facade.executeCommands([new Command(allEdges, node, offset, this)]);

	}
});/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

if(!ORYX){ var ORYX = {} }
if(!ORYX.Plugins){ ORYX.Plugins = {} }

/**
   This abstract plugin implements the core behaviour of layout
   
   @class ORYX.Plugins.AbstractLayouter
   @constructor Creates a new instance
   @author Willi Tscheschner
*/
ORYX.Plugins.AbstractLayouter = ORYX.Plugins.AbstractPlugin.extend({
	
	/**
	 * 'layouted' defined all types of shapes which will be layouted. 
	 * It can be one value or an array of values. The value
	 * can be a Stencil ID (as String) or an class type of either 
	 * a ORYX.Core.Node or ORYX.Core.Edge
     * @type Array|String|Object
     * @memberOf ORYX.Plugins.AbstractLayouter.prototype
	 */
	layouted : [],
	
	/**
	 * Constructor
	 * @param {Object} facade
	 * @memberOf ORYX.Plugins.AbstractLayouter.prototype
	 */
	construct: function( facade ){
		arguments.callee.$.construct.apply(this, arguments);
			
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_LAYOUT, this._initLayout.bind(this));
	},
	
	/**
	 * Proofs if this shape should be layouted or not
	 * @param {Object} shape
     * @memberOf ORYX.Plugins.AbstractLayouter.prototype
	 */
	isIncludedInLayout: function(shape){
		if (!(this.layouted instanceof Array)){
			this.layouted = [this.layouted].compact();
		}
		
		// If there are no elements
		if (this.layouted.length <= 0) {
			// Return TRUE
			return true;
		}
		
		// Return TRUE if there is any correlation between 
		// the 'layouted' attribute and the shape themselve.
		return this.layouted.any(function(s){
			if (typeof s == "string") {
				return shape.getStencil().id().include(s);
			} else {
				return shape instanceof s;
			}
		})
	},
	
	/**
	 * Callback to start the layouting
	 * @param {Object} event Layout event
	 * @param {Object} shapes Given shapes
     * @memberOf ORYX.Plugins.AbstractLayouter.prototype
	 */
	_initLayout: function(event){
		
		// Get the shapes
		var shapes = [event.shapes].flatten().compact();
		
		// Find all shapes which should be layouted
		var toLayout = shapes.findAll(function(shape){
			return this.isIncludedInLayout(shape) 
		}.bind(this))
		
		// If there are shapes left 
		if (toLayout.length > 0){
			// Do layout
			this.layout(toLayout);
		}
	},
	
	/**
	 * Implementation of layouting a set on shapes
	 * @param {Object} shapes Given shapes
     * @memberOf ORYX.Plugins.AbstractLayouter.prototype
	 */
	layout: function(shapes){
		throw new Error("Layouter has to implement the layout function.")
	}
});/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

if (!ORYX.Plugins) 
    ORYX.Plugins = new Object();

ORYX.Plugins.Edit = Clazz.extend({
    
    construct: function(facade){
    
        this.facade = facade;
        this.clipboard = new ORYX.Plugins.Edit.ClipBoard();
        
        //this.facade.registerOnEvent(ORYX.CONFIG.EVENT_KEYDOWN, this.keyHandler.bind(this));
        
        this.facade.offer({
         name: ORYX.I18N.Edit.cut,
         description: ORYX.I18N.Edit.cutDesc,
         icon: ORYX.PATH + "images/cut.png",
		 keyCodes: [{
				metaKeys: [ORYX.CONFIG.META_KEY_META_CTRL],
				keyCode: 88,
				keyAction: ORYX.CONFIG.KEY_ACTION_DOWN
			}
		 ],
         functionality: this.callEdit.bind(this, this.editCut),
         group: ORYX.I18N.Edit.group,
         index: 1,
         minShape: 1
         });
         
        this.facade.offer({
         name: ORYX.I18N.Edit.copy,
         description: ORYX.I18N.Edit.copyDesc,
         icon: ORYX.PATH + "images/page_copy.png",
		 keyCodes: [{
				metaKeys: [ORYX.CONFIG.META_KEY_META_CTRL],
				keyCode: 67,
				keyAction: ORYX.CONFIG.KEY_ACTION_DOWN
			}
		 ],
         functionality: this.callEdit.bind(this, this.editCopy, [true, false]),
         group: ORYX.I18N.Edit.group,
         index: 2,
         minShape: 1
         });
         
        this.facade.offer({
         name: ORYX.I18N.Edit.paste,
         description: ORYX.I18N.Edit.pasteDesc,
         icon: ORYX.PATH + "images/page_paste.png",
		 keyCodes: [{
				metaKeys: [ORYX.CONFIG.META_KEY_META_CTRL],
				keyCode: 86,
				keyAction: ORYX.CONFIG.KEY_ACTION_DOWN
			}
		 ],
         functionality: this.callEdit.bind(this, this.editPaste),
         isEnabled: this.clipboard.isOccupied.bind(this.clipboard),
         group: ORYX.I18N.Edit.group,
         index: 3,
         minShape: 0,
         maxShape: 0
         });
         
        this.facade.offer({
            name: ORYX.I18N.Edit.del,
            description: ORYX.I18N.Edit.delDesc,
            icon: ORYX.PATH + "images/cross.png",
			keyCodes: [{
					metaKeys: [ORYX.CONFIG.META_KEY_META_CTRL],
					keyCode: 8,
					keyAction: ORYX.CONFIG.KEY_ACTION_DOWN
				},
				{	
					keyCode: 46,
					keyAction: ORYX.CONFIG.KEY_ACTION_DOWN
				}
			],
            functionality: this.callEdit.bind(this, this.editDelete),
            group: ORYX.I18N.Edit.group,
            index: 4,
            minShape: 1
        });
    },
	
	callEdit: function(fn, args){
		window.setTimeout(function(){
			fn.apply(this, (args instanceof Array ? args : []));
		}.bind(this), 1);
	},
	
	/**
	 * Handles the mouse down event and starts the copy-move-paste action, if
	 * control or meta key is pressed.
	 */
	handleMouseDown: function(event) {
		if(this._controlPressed) {
			this._controlPressed = false;
			this.editCopy();
//			console.log("copiedEle: %0",this.clipboard.shapesAsJson)
//			console.log("mousevent: %o",event)
			this.editPaste();
			event.forceExecution = true;
			this.facade.raiseEvent(event, this.clipboard.shapesAsJson);
			
		}
	},
    
    /**
     * The key handler for this plugin. Every action from the set of cut, copy,
     * paste and delete should be accessible trough simple keyboard shortcuts.
     * This method checks whether any event triggers one of those actions.
     *
     * @param {Object} event The keyboard event that should be analysed for
     *     triggering of this plugin.
     */
//    keyHandler: function(event){
//        //TODO document what event.which is.
//        
//        ORYX.Log.debug("edit.js handles a keyEvent.");
//        
//        // assure we have the current event.
//        if (!event) 
//            event = window.event;
//        
//        
//        // get the currently pressed key and state of control key.
//        var pressedKey = event.which || event.keyCode;
//        var ctrlPressed = event.ctrlKey;
//        
//        // if the object is to be deleted, do so, and return immediately.
//        if ((pressedKey == ORYX.CONFIG.KEY_CODE_DELETE) ||
//        ((pressedKey == ORYX.CONFIG.KEY_CODE_BACKSPACE) &&
//        (event.metaKey || event.appleMetaKey))) {
//        
//            ORYX.Log.debug("edit.js deletes the shape.");
//            this.editDelete();
//            return;
//        }
//        
//         // if control key is not pressed, we're not interested anymore.
//         if (!ctrlPressed)
//         return;
//         
//         // when ctrl is pressed, switch trough the possibilities.
//         switch (pressedKey) {
//         
//	         // cut.
//	         case ORYX.CONFIG.KEY_CODE_X:
//	         this.editCut();
//	         break;
//	         
//	         // copy.
//	         case ORYX.CONFIG.KEY_CODE_C:
//	         this.editCopy();
//	         break;
//	         
//	         // paste.
//	         case ORYX.CONFIG.KEY_CODE_V:
//	         this.editPaste();
//	         break;
//         }
//    },
	
    /**
     * Returns a list of shapes which should be considered while copying.
     * Besides the shapes of given ones, edges and attached nodes are added to the result set.
     * If one of the given shape is a child of another given shape, it is not put into the result. 
     */
    getAllShapesToConsider: function(shapes){
        var shapesToConsider = []; // only top-level shapes
        var childShapesToConsider = []; // all child shapes of top-level shapes
        
        shapes.each(function(shape){
            //Throw away these shapes which have a parent in given shapes
            isChildShapeOfAnother = shapes.any(function(s2){
                return s2.hasChildShape(shape);
            });
            if(isChildShapeOfAnother) return;
            
            // This shape should be considered
            shapesToConsider.push(shape);
            // Consider attached nodes (e.g. intermediate events)
            if (shape instanceof ORYX.Core.Node) {
				var attached = shape.getOutgoingNodes();
				attached = attached.findAll(function(a){ return !shapes.include(a) });
                shapesToConsider = shapesToConsider.concat(attached);
            }
            
            childShapesToConsider = childShapesToConsider.concat(shape.getChildShapes(true));
        }.bind(this));
        
        // All edges between considered child shapes should be considered
        // Look for these edges having incoming and outgoing in childShapesToConsider
        var edgesToConsider = this.facade.getCanvas().getChildEdges().select(function(edge){
            // Ignore if already added
            if(shapesToConsider.include(edge)) return false;
            // Ignore if there are no docked shapes
            if(edge.getAllDockedShapes().size() === 0) return false; 
            // True if all docked shapes are in considered child shapes
            return edge.getAllDockedShapes().all(function(shape){
                // Remember: Edges can have other edges on outgoing, that is why edges must not be included in childShapesToConsider
                return shape instanceof ORYX.Core.Edge || childShapesToConsider.include(shape);
            });
        });
        shapesToConsider = shapesToConsider.concat(edgesToConsider);
        
        return shapesToConsider;
    },
    
    /**
     * Performs the cut operation by first copy-ing and then deleting the
     * current selection.
     */
    editCut: function(){
        //TODO document why this returns false.
        //TODO document what the magic boolean parameters are supposed to do.
        
        this.editCopy(false, true);
        this.editDelete(true);
        return false;
    },
    
    /**
     * Performs the copy operation.
     * @param {Object} will_not_update ??
     */
    editCopy: function( will_update, useNoOffset ){
        var selection = this.facade.getSelection();
        
        //if the selection is empty, do not remove the previously copied elements
        if(selection.length == 0) return;
        
        this.clipboard.refresh(selection, this.getAllShapesToConsider(selection), this.facade.getCanvas().getStencil().stencilSet().namespace(), useNoOffset);

        if( will_update ) this.facade.updateSelection();
    },
    
    /**
     * Performs the paste operation.
     */
    editPaste: function(){
        // Create a new canvas with childShapes 
		//and stencilset namespace to be JSON Import conform
		var canvas = {
            childShapes: this.clipboard.shapesAsJson,
			stencilset:{
				namespace:this.clipboard.SSnamespace
			}
        };
        // Apply json helper to iterate over json object
		jQuery.extend(canvas, ORYX.Core.AbstractShape.JSONHelper);
        
        var childShapeResourceIds = canvas.getChildShapes(true).pluck("resourceId");
        var outgoings = {};
        // Iterate over all shapes
        canvas.eachChild(function(shape, parent){
            // Throw away these references where referenced shape isn't copied
            shape.outgoing = shape.outgoing.select(function(out){
                return childShapeResourceIds.include(out.resourceId);
            });
			shape.outgoing.each(function(out){
				if (!outgoings[out.resourceId]){ outgoings[out.resourceId] = []; }
				outgoings[out.resourceId].push(shape);
			});
			
            return shape;
        }.bind(this), true, true);
        

        // Iterate over all shapes
        canvas.eachChild(function(shape, parent){
            
        	// Check if there has a valid target
            if(shape.target && !(childShapeResourceIds.include(shape.target.resourceId))){
                shape.target = undefined;
                shape.targetRemoved = true;
            }
    		
    		// Check if the first docker is removed
    		if(	shape.dockers && 
    			shape.dockers.length >= 1 && 
    			shape.dockers[0].getDocker &&
    			((shape.dockers[0].getDocker().getDockedShape() &&
    			!childShapeResourceIds.include(shape.dockers[0].getDocker().getDockedShape().resourceId)) || 
    			!shape.getShape().dockers[0].getDockedShape()&&!outgoings[shape.resourceId])) {
    				
    			shape.sourceRemoved = true;
    		}
			
            return shape;
        }.bind(this), true, true);

		
        // Iterate over top-level shapes
        canvas.eachChild(function(shape, parent){
            // All top-level shapes should get an offset in their bounds
            // Move the shape occording to COPY_MOVE_OFFSET
        	if (this.clipboard.useOffset) {
	            shape.bounds = {
	                lowerRight: {
	                    x: shape.bounds.lowerRight.x + ORYX.CONFIG.COPY_MOVE_OFFSET,
	                    y: shape.bounds.lowerRight.y + ORYX.CONFIG.COPY_MOVE_OFFSET
	                },
	                upperLeft: {
	                    x: shape.bounds.upperLeft.x + ORYX.CONFIG.COPY_MOVE_OFFSET,
	                    y: shape.bounds.upperLeft.y + ORYX.CONFIG.COPY_MOVE_OFFSET
	                }
	            };
        	}
            // Only apply offset to shapes with a target
            if (shape.dockers){
                shape.dockers = shape.dockers.map(function(docker, i){
                    // If shape had a target but the copied does not have anyone anymore,
                    // migrate the relative dockers to absolute ones.
                    if( (shape.targetRemoved === true && i == shape.dockers.length - 1&&docker.getDocker) ||
						(shape.sourceRemoved === true && i == 0&&docker.getDocker)){

                        docker = docker.getDocker().bounds.center();
                    }

					// If it is the first docker and it has a docked shape, 
					// just return the coordinates
				   	if ((i == 0 && docker.getDocker instanceof Function && 
				   		shape.sourceRemoved !== true && (docker.getDocker().getDockedShape() || ((outgoings[shape.resourceId]||[]).length > 0 && (!(shape.getShape() instanceof ORYX.Core.Node) || outgoings[shape.resourceId][0].getShape() instanceof ORYX.Core.Node)))) || 
						(i == shape.dockers.length - 1 && docker.getDocker instanceof Function && 
						shape.targetRemoved !== true && (docker.getDocker().getDockedShape() || shape.target))){
							
						return {
                        	x: docker.x, 
                        	y: docker.y,
                        	getDocker: docker.getDocker
						}
					} else if (this.clipboard.useOffset) {
	                    return {
		                        x: docker.x + ORYX.CONFIG.COPY_MOVE_OFFSET, 
		                        y: docker.y + ORYX.CONFIG.COPY_MOVE_OFFSET,
	                        	getDocker: docker.getDocker
		                    };
				   	} else {
				   		return {
                        	x: docker.x, 
                        	y: docker.y,
                        	getDocker: docker.getDocker
						};
				   	}
                }.bind(this));

            } else if (shape.getShape() instanceof ORYX.Core.Node && shape.dockers && shape.dockers.length > 0 && (!shape.dockers.first().getDocker || shape.sourceRemoved === true || !(shape.dockers.first().getDocker().getDockedShape() || outgoings[shape.resourceId]))){
            	
            	shape.dockers = shape.dockers.map(function(docker, i){
            		
                    if((shape.sourceRemoved === true && i == 0&&docker.getDocker)){
                    	docker = docker.getDocker().bounds.center();
                    }
                    
                    if (this.clipboard.useOffset) {
	            		return {
	                        x: docker.x + ORYX.CONFIG.COPY_MOVE_OFFSET, 
	                        y: docker.y + ORYX.CONFIG.COPY_MOVE_OFFSET,
	                    	getDocker: docker.getDocker
	                    };
                    } else {
	            		return {
	                        x: docker.x, 
	                        y: docker.y,
	                    	getDocker: docker.getDocker
	                    };
                    }
            	}.bind(this));
            }
            
            return shape;
        }.bind(this), false, true);

        this.clipboard.useOffset = true;
        this.facade.importJSON(canvas);
    },
    
    /**
     * Performs the delete operation. No more asking.
     */
    editDelete: function(){
        var selection = this.facade.getSelection();
        
        var clipboard = new ORYX.Plugins.Edit.ClipBoard();
        clipboard.refresh(selection, this.getAllShapesToConsider(selection));
        
		var command = new ORYX.Plugins.Edit.DeleteCommand(clipboard , this.facade);
                                       
		this.facade.executeCommands([command]);
    }
}); 

ORYX.Plugins.Edit.ClipBoard = Clazz.extend({
    construct: function(){
        this.shapesAsJson = [];
        this.selection = [];
		this.SSnamespace="";
		this.useOffset=true;
    },
    isOccupied: function(){
        return this.shapesAsJson.length > 0;
    },
    refresh: function(selection, shapes, namespace, useNoOffset){
        this.selection = selection;
        this.SSnamespace=namespace;
        // Store outgoings, targets and parents to restore them later on
        this.outgoings = {};
        this.parents = {};
        this.targets = {};
        this.useOffset = useNoOffset !== true;
        
        this.shapesAsJson = shapes.map(function(shape){
            var s = shape.toJSON();
            s.parent = {resourceId : shape.getParentShape().resourceId};
            s.parentIndex = shape.getParentShape().getChildShapes().indexOf(shape)
            return s;
        });
    }
});

ORYX.Plugins.Edit.DeleteCommand = ORYX.Core.Command.extend({
    construct: function(clipboard, facade){
        this.clipboard          = clipboard;
        this.shapesAsJson       = clipboard.shapesAsJson;
        this.facade             = facade;
        
        // Store dockers of deleted shapes to restore connections
        this.dockers            = this.shapesAsJson.map(function(shapeAsJson) {
            var shape = shapeAsJson.getShape();
            var incomingDockers = shape.getIncomingShapes().map(function(s){return s.getDockers().last();});
            var outgoingDockers = shape.getOutgoingShapes().map(function(s){return s.getDockers().first();});
            var dockers = shape.getDockers().concat(incomingDockers, outgoingDockers).compact().map(function(docker){
                return {
                    object: docker,
                    referencePoint: docker.referencePoint,
                    dockedShape: docker.getDockedShape()
                };
            });
            return dockers;
        }).flatten();
    },          
    execute: function(){
        this.shapesAsJson.each(function(shapeAsJson){
            // Delete shape
            this.facade.deleteShape(shapeAsJson.getShape());
        }.bind(this));
        
        this.facade.setSelection([]);
        this.facade.getCanvas().update();		
		this.facade.updateSelection();
        
    },
    rollback: function(){
        this.shapesAsJson.each(function(shapeAsJson) {
            var shape = shapeAsJson.getShape();
            var parent = this.facade.getCanvas().getChildShapeByResourceId(shapeAsJson.parent.resourceId) || this.facade.getCanvas();
            parent.add(shape, shape.parentIndex);
        }.bind(this));
        
        //reconnect shapes
        this.dockers.each(function(d) {
            d.object.setDockedShape(d.dockedShape);
            d.object.setReferencePoint(d.referencePoint);
        }.bind(this));
        
        this.facade.setSelection(this.selectedShapes);
        this.facade.getCanvas().update();	
		this.facade.updateSelection();
        
    }
});/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

/**
 * @namespace Oryx name space for plugins
 * @name ORYX.Plugins
*/
if(!ORYX.Plugins)
	ORYX.Plugins = new Object();

/**
 * The view plugin offers all of zooming functionality accessible over the 
 * tool bar. This are zoom in, zoom out, zoom to standard, zoom fit to model.
 * 
 * @class ORYX.Plugins.View
 * @extends Clazz
 * @param {Object} facade The editor facade for plugins.
*/
ORYX.Plugins.View = {
	/** @lends ORYX.Plugins.View.prototype */
	facade: undefined,

	construct: function(facade, ownPluginData) {
		this.facade = facade;
		//Standard Values
		this.zoomLevel = 1.0;
		this.maxFitToScreenLevel=1.5;
		this.minZoomLevel = 0.1;
		this.maxZoomLevel = 2.5;
		this.diff=5; //difference between canvas and view port, s.th. like toolbar??
		
		//Read properties
		if (ownPluginData !== undefined && ownPluginData !== null) {
			ownPluginData.properties.each( function(property) {			
				if (property.zoomLevel) {this.zoomLevel = Number(1.0);}		
				if (property.maxFitToScreenLevel) {this.maxFitToScreenLevel=Number(property.maxFitToScreenLevel);}
				if (property.minZoomLevel) {this.minZoomLevel = Number(property.minZoomLevel);}
				if (property.maxZoomLevel) {this.maxZoomLevel = Number(property.maxZoomLevel);}
			}.bind(this));
		}

		
		/* Register zoom in */
		this.facade.offer({
			'name':ORYX.I18N.View.zoomIn,
			'functionality': this.zoom.bind(this, [1.0 + ORYX.CONFIG.ZOOM_OFFSET]),
			'group': ORYX.I18N.View.group,
			'icon': ORYX.PATH + "images/magnifier_zoom_in.png",
			'description': ORYX.I18N.View.zoomInDesc,
			'index': 1,
			'minShape': 0,
			'maxShape': 0,
			'isEnabled': function(){return this.zoomLevel < this.maxZoomLevel }.bind(this)});
		
		/* Register zoom out */
		this.facade.offer({
			'name':ORYX.I18N.View.zoomOut,
			'functionality': this.zoom.bind(this, [1.0 - ORYX.CONFIG.ZOOM_OFFSET]),
			'group': ORYX.I18N.View.group,
			'icon': ORYX.PATH + "images/magnifier_zoom_out.png",
			'description': ORYX.I18N.View.zoomOutDesc,
			'index': 2,
			'minShape': 0,
			'maxShape': 0,
			'isEnabled': function(){ return this._checkSize() }.bind(this)});
		
		/* Register zoom standard */
		this.facade.offer({
			'name':ORYX.I18N.View.zoomStandard,
			'functionality': this.setAFixZoomLevel.bind(this, 1),
			'group': ORYX.I18N.View.group,
			'icon': ORYX.PATH + "images/zoom_standard.png",
			'cls' : 'icon-large',
			'description': ORYX.I18N.View.zoomStandardDesc,
			'index': 3,
			'minShape': 0,
			'maxShape': 0,
			'isEnabled': function(){return this.zoomLevel != 1}.bind(this)
		});
		
		/* Register zoom fit to model */
		this.facade.offer({
			'name':ORYX.I18N.View.zoomFitToModel,
			'functionality': this.zoomFitToModel.bind(this),
			'group': ORYX.I18N.View.group,
			'icon': ORYX.PATH + "images/image.png",
			'description': ORYX.I18N.View.zoomFitToModelDesc,
			'index': 4,
			'minShape': 0,
			'maxShape': 0
		});
	},
	
	/**
	 * It sets the zoom level to a fix value and call the zooming function.
	 * 
	 * @param {Number} zoomLevel
	 * 			the zoom level
	 */
	setAFixZoomLevel : function(zoomLevel) {
		this.zoomLevel = zoomLevel;
		this._checkZoomLevelRange();
		this.zoom(1);
	},
	
	/**
	 * It does the actual zooming. It changes the viewable size of the canvas 
	 * and all to its child elements.
	 * 
	 * @param {Number} factor
	 * 		the factor to adjust the zoom level
	 */
	zoom: function(factor) {
		// TODO: Zoomen auf allen Objekten im SVG-DOM
		
		this.zoomLevel *= factor;
		var scrollNode 	= this.facade.getCanvas().getHTMLContainer().parentNode.parentNode;
		var canvas 		= this.facade.getCanvas();
		var newWidth 	= canvas.bounds.width()  * this.zoomLevel;
		var newHeight 	= canvas.bounds.height() * this.zoomLevel;
		
		/* Set new top offset */
		var offsetTop = (canvas.node.parentNode.parentNode.parentNode.offsetHeight - newHeight) / 2.0;	
		offsetTop = offsetTop > 20 ? offsetTop - 20 : 0;
		canvas.node.parentNode.parentNode.style.marginTop = offsetTop + "px";
		offsetTop += 5;
		canvas.getHTMLContainer().style.top = offsetTop + "px";
		
		/*readjust scrollbar*/
		var newScrollTop=	scrollNode.scrollTop - Math.round((canvas.getHTMLContainer().parentNode.getHeight()-newHeight) / 2)+this.diff;
		var newScrollLeft=	scrollNode.scrollLeft - Math.round((canvas.getHTMLContainer().parentNode.getWidth()-newWidth) / 2)+this.diff;
		
		/* Set new Zoom-Level */
		canvas.setSize({width: newWidth, height: newHeight}, true);
		
		/* Set Scale-Factor */
		canvas.node.setAttributeNS(null, "transform", "scale(" +this.zoomLevel+ ")");	

		/* Refresh the Selection */
		this.facade.updateSelection();
		scrollNode.scrollTop=newScrollTop;
		scrollNode.scrollLeft=newScrollLeft;
		
		/* Update the zoom-level*/
		canvas.zoomLevel = this.zoomLevel;
	},
	
	
	/**
	 * It calculates the zoom level to fit whole model into the visible area
	 * of the canvas. Than the model gets zoomed and the position of the 
	 * scroll bars are adjusted.
	 * 
	 */
	zoomFitToModel: function() {
		
		/* Get the size of the visible area of the canvas */
		var scrollNode 	= this.facade.getCanvas().getHTMLContainer().parentNode.parentNode;
		var visibleHeight = scrollNode.getHeight() - 30;
		var visibleWidth = scrollNode.getWidth() - 30;
		
		var nodes = this.facade.getCanvas().getChildShapes();
		
		if(!nodes || nodes.length < 1) {
			return false;			
		}
			
		/* Calculate size of canvas to fit the model */
		var bounds = nodes[0].absoluteBounds().clone();
		nodes.each(function(node) {
			bounds.include(node.absoluteBounds().clone());
		});
		
		
		/* Set new Zoom Level */
		var scaleFactorWidth =  visibleWidth / bounds.width();
		var scaleFactorHeight = visibleHeight / bounds.height();
		
		/* Choose the smaller zoom level to fit the whole model */
		var zoomFactor = scaleFactorHeight < scaleFactorWidth ? scaleFactorHeight : scaleFactorWidth;
		
		/*Test if maximum zoom is reached*/
		if(zoomFactor>this.maxFitToScreenLevel){zoomFactor=this.maxFitToScreenLevel}
		/* Do zooming */
		this.setAFixZoomLevel(zoomFactor);
		
		/* Set scroll bar position */
		scrollNode.scrollTop = Math.round(bounds.upperLeft().y * this.zoomLevel) - 5;
		scrollNode.scrollLeft = Math.round(bounds.upperLeft().x * this.zoomLevel) - 5;
		
	},
	
	/**
	 * It checks if the zoom level is less or equal to the level, which is required
	 * to schow the whole canvas.
	 * 
	 * @private
	 */
	_checkSize:function(){
		var canvasParent=this.facade.getCanvas().getHTMLContainer().parentNode;
		var minForCanvas= Math.min((canvasParent.parentNode.getWidth()/canvasParent.getWidth()),(canvasParent.parentNode.getHeight()/canvasParent.getHeight()));
		return 1.05 > minForCanvas;
		
	},
	/**
	 * It checks if the zoom level is included in the definined zoom
	 * level range.
	 * 
	 * @private
	 */
	_checkZoomLevelRange: function() {
		/*var canvasParent=this.facade.getCanvas().getHTMLContainer().parentNode;
		var maxForCanvas= Math.max((canvasParent.parentNode.getWidth()/canvasParent.getWidth()),(canvasParent.parentNode.getHeight()/canvasParent.getHeight()));
		if(this.zoomLevel > maxForCanvas) {
			this.zoomLevel = maxForCanvas;			
		}*/
		if(this.zoomLevel < this.minZoomLevel) {
			this.zoomLevel = this.minZoomLevel;			
		}
		
		if(this.zoomLevel > this.maxZoomLevel) {
			this.zoomLevel = this.maxZoomLevel;			
		}
	}
};

ORYX.Plugins.View = Clazz.extend(ORYX.Plugins.View);
/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
if(!Signavio){ var Signavio = {} };
	if (!Signavio.Core) { Signavio.Core = {} };
	Signavio.Core.Version = "1.0";
			/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

if (!Signavio) {
	var Signavio = new Object();
}

if (!Signavio.Plugins) {
	Signavio.Plugins = new Object();
}

if (!Signavio.Plugins.Utils) {
	Signavio.Plugins.Utils = new Object();
}

if (!Signavio.Helper) {
	Signavio.Helper = new Object();
}


new function() {

	/**
	 * Provides an uniq id
	 * @overwrite
	 * @return {String}
	 *
	 */
	ORYX.Editor.provideId = function() {
		var res = [], hex = '0123456789ABCDEF';
	
		for (var i = 0; i < 36; i++) res[i] = Math.floor(Math.random()*0x10);
	
		res[14] = 4;
		res[19] = (res[19] & 0x3) | 0x8;
	
		for (var i = 0; i < 36; i++) res[i] = hex[res[i]];
	
		res[8] = res[13] = res[18] = res[23] = '-';
	
		return "sid-" + res.join('');
	};


}();

/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

if (!ORYX.Plugins) {
    ORYX.Plugins = new Object();
}

/**
 * This plugin is responsible for displaying loading indicators and to prevent
 * the user from accidently unloading the page by, e.g., pressing the backspace
 * button and returning to the previous site in history.
 * @param {Object} facade The editor plugin facade to register enhancements with.
 */
ORYX.Plugins.Loading = {

    construct: function(facade){
    
        this.facade = facade;
        
        // The parent Node
        this.node = ORYX.Editor.graft("http://www.w3.org/1999/xhtml", this.facade.getCanvas().getHTMLContainer().parentNode, ['div', {
            'class': 'LoadingIndicator'
        }, '']);
        
        this.facade.registerOnEvent(ORYX.CONFIG.EVENT_LOADING_ENABLE, this.enableLoading.bind(this));
        this.facade.registerOnEvent(ORYX.CONFIG.EVENT_LOADING_DISABLE, this.disableLoading.bind(this));
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_LOADING_STATUS, this.showStatus.bind(this));
        
        this.disableLoading();
    },
    
    enableLoading: function(options){
		if(options.text) 
			this.node.innerHTML = options.text + "...";
		else
			this.node.innerHTML = ORYX.I18N.Loading.waiting;
		this.node.removeClassName('StatusIndicator');
		this.node.addClassName('LoadingIndicator');
        this.node.style.display = "block";
		
		var pos = this.facade.getCanvas().rootNode.parentNode.parentNode.parentNode.parentNode;

		this.node.style.top 		= pos.offsetTop + 'px';
		this.node.style.left 		= pos.offsetLeft +'px';
					
    },
    
    disableLoading: function(){
        this.node.style.display = "none";
    },
	
	showStatus: function(options) {
		if(options.text) {
			this.node.innerHTML = options.text;
			this.node.addClassName('StatusIndicator');
			this.node.removeClassName('LoadingIndicator');
			this.node.style.display = 'block';

			var pos = this.facade.getCanvas().rootNode.parentNode.parentNode.parentNode.parentNode;

			this.node.style.top 	= pos.offsetTop + 'px';
			this.node.style.left 	= pos.offsetLeft +'px';
												
			var tout = options.timeout ? options.timeout : 2000;
			
			window.setTimeout((function(){
            
                this.disableLoading();
                
            }).bind(this), tout);
		}
		
	}
}

ORYX.Plugins.Loading = Clazz.extend(ORYX.Plugins.Loading);
/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
if (!ORYX.Plugins) {
    ORYX.Plugins = new Object();
}

/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

/**
 * This plugin is responsible for resizing the canvas.
 * @param {Object} facade The editor plugin facade to register enhancements with.
 */
ORYX.Plugins.CanvasResize = Clazz.extend({

    construct: function(facade){
		
        this.facade = facade;
		new ORYX.Plugins.CanvasResizeButton( this.facade.getCanvas(), "N", this.resize.bind(this));
		new ORYX.Plugins.CanvasResizeButton( this.facade.getCanvas(), "W", this.resize.bind(this));
		new ORYX.Plugins.CanvasResizeButton( this.facade.getCanvas(), "E", this.resize.bind(this));
		new ORYX.Plugins.CanvasResizeButton( this.facade.getCanvas(), "S", this.resize.bind(this));
		
		window.setTimeout(function(){jQuery(window).trigger('resize');});

    },
    
    resize: function( position, shrink ){
    	
    	resizeCanvas = function(position, extentionSize, facade) {
        	var canvas 		= facade.getCanvas();
    		var b 			= canvas.bounds;
    		var scrollNode 	= facade.getCanvas().getHTMLContainer().parentNode.parentNode;
    		
    		if( position == "E" || position == "W"){
    			canvas.setSize({width: (b.width() + extentionSize)*canvas.zoomLevel, height: (b.height())*canvas.zoomLevel})

    		} else if( position == "S" || position == "N"){
    			canvas.setSize({width: (b.width())*canvas.zoomLevel, height: (b.height() + extentionSize)*canvas.zoomLevel})
    		}

    		if( position == "N" || position == "W"){
    			
    			var move = position == "N" ? {x: 0, y: extentionSize}: {x: extentionSize, y: 0 };

    			// Move all children
    			canvas.getChildNodes(false, function(shape){ shape.bounds.moveBy(move) })
    			// Move all dockers, when the edge has at least one docked shape
    			var edges = canvas.getChildEdges().findAll(function(edge){ return edge.getAllDockedShapes().length > 0})
    			var dockers = edges.collect(function(edge){ return edge.dockers.findAll(function(docker){ return !docker.getDockedShape() })}).flatten();
    			dockers.each(function(docker){ docker.bounds.moveBy(move)})
    		} else if( position == "S" ){
    			scrollNode.scrollTop += extentionSize;
    		} else if( position == "E" ){
    			scrollNode.scrollLeft += extentionSize;
    		}
    		
    		jQuery(window).trigger('resize');
    		
    		canvas.update();
    		facade.updateSelection();
        }
		
		var commandClass = ORYX.Core.Command.extend({
			construct: function(position, extentionSize, facade){
				this.position = position;
				this.extentionSize = extentionSize;
				this.facade = facade;
			},			
			execute: function(){
				resizeCanvas(this.position, this.extentionSize, this.facade);
			},
			rollback: function(){
				resizeCanvas(this.position, -this.extentionSize, this.facade);
			},
			update:function(){
			}
		});
		
		var extentionSize = ORYX.CONFIG.CANVAS_RESIZE_INTERVAL;
		if(shrink) extentionSize = -extentionSize;
		var command = new commandClass(position, extentionSize, this.facade);
		
		this.facade.executeCommands([command]);
			
    }
    
});


ORYX.Plugins.CanvasResizeButton = Clazz.extend({
	
	construct: function(canvas, position, callback){
		this.canvas = canvas;
		var parentNode = canvas.getHTMLContainer().parentNode;
		
		window.myParent=parentNode;
			
		var actualScrollNode = jQuery('#canvasSection')[0];
		var scrollNode 	= actualScrollNode;
		var canvasNode = jQuery('#canvasSection').find(".ORYX_Editor")[0];
		var svgRootNode = canvasNode.children[0];
		
		var iconClass = 'glyphicon glyphicon-chevron-';
		var iconClassShrink = 'glyphicon glyphicon-chevron-';
		if(position == 'N') {
			iconClass += 'up';
			iconClassShrink += 'down';
		} else if(position == 'S') {
			iconClass += 'down';
			iconClassShrink += 'up';
		} else if(position == 'E') {
			iconClass += 'right';
			iconClassShrink += 'left';
		} else if(position == 'W') {
			iconClass += 'left';
			iconClassShrink += 'right';
		}
		
		// The buttons
		var idGrow = 'canvas-shrink-' + position;
		var idShrink = 'canvas-grow-' + position;
		
		var buttonGrow 	= ORYX.Editor.graft("http://www.w3.org/1999/xhtml", parentNode, ['div', {'class': 'canvas_resize_indicator canvas_resize_indicator_grow' + ' ' + position, 'id': idGrow ,'title':ORYX.I18N.RESIZE.tipGrow+ORYX.I18N.RESIZE[position]},
             ['i', {'class' : iconClass}]
		]);
		var buttonShrink 	= ORYX.Editor.graft("http://www.w3.org/1999/xhtml", parentNode, ['div', {'class': 'canvas_resize_indicator canvas_resize_indicator_shrink' + ' ' + position, 'id': idShrink ,'title':ORYX.I18N.RESIZE.tipGrow+ORYX.I18N.RESIZE[position]},
             ['i', {'class' : iconClassShrink}]
		]);
		// Defines a callback which gives back
		// a boolean if the current mouse event 
		// is over the particular button area
		var offSetWidth = 60;
		var isOverOffset = function(event) {
			
			var isOverButton = event.target.id.indexOf("canvas-shrink") != -1
				|| event.target.id.indexOf("canvas-grow") != -1
				|| event.target.parentNode.id.indexOf("canvas-shrink") != -1
				|| event.target.parentNode.id.indexOf("canvas-grow") != -1;
			if(isOverButton) {
				if(event.target.id == idGrow || event.target.id == idShrink || 
						event.target.parentNode.id == idGrow || event.target.parentNode.id == idShrink ) {
					return true;
				} else {
					return false;
				}
			}
			
			if(event.target!=parentNode && event.target!=scrollNode&& event.target!=scrollNode.firstChild&& event.target!=svgRootNode&& event.target!=scrollNode){ return false; }
			
			//if(inCanvas){offSetWidth=30}else{offSetWidth=30*2}
			//Safari work around
			var X=event.offsetX !== undefined ? event.offsetX : event.layerX;
			var Y=event.offsetY !== undefined ? event.offsetY : event.layerY;
			
			var canvasOffset = 0;
			if(canvasNode.clientWidth < actualScrollNode.clientWidth) {
              	var widthDiff = actualScrollNode.clientWidth -  canvasNode.clientWidth;
              	canvasOffset = widthDiff / 2;
            }
			
				// Adjust to relative location to the actual viewport
				Y = Y - actualScrollNode.scrollTop;
				X = X - actualScrollNode.scrollLeft;
			
			
			if(position == "N"){
				return  Y < offSetWidth;
			} else if(position == "W"){
				return X < offSetWidth + canvasOffset;
			} else if(position == "E"){
				return actualScrollNode.clientWidth - X < offSetWidth + canvasOffset;
			} else if(position == "S"){
				return actualScrollNode.clientHeight - Y < offSetWidth;
			}
			
			return false;
		};
		
		var showButtons = (function() {
			buttonGrow.show(); 
        
			var w = canvas.bounds.width();
			var h = canvas.bounds.height();
        
			if(position=="N" && (h - ORYX.CONFIG.CANVAS_RESIZE_INTERVAL > ORYX.CONFIG.CANVAS_MIN_HEIGHT)) buttonShrink.show();
			else if(position=="E" && (w - ORYX.CONFIG.CANVAS_RESIZE_INTERVAL > ORYX.CONFIG.CANVAS_MIN_WIDTH)) buttonShrink.show();
			else if(position=="S" && (h - ORYX.CONFIG.CANVAS_RESIZE_INTERVAL > ORYX.CONFIG.CANVAS_MIN_HEIGHT)) buttonShrink.show();
			else if(position=="W" && (w - ORYX.CONFIG.CANVAS_RESIZE_INTERVAL > ORYX.CONFIG.CANVAS_MIN_WIDTH)) buttonShrink.show();
			else buttonShrink.hide();
			

		}).bind(this);
        
		var hideButtons = function() {
			buttonGrow.hide(); 
			buttonShrink.hide();
		};
        
		// If the mouse move is over the button area, show the button
		parentNode.parentNode.addEventListener(	ORYX.CONFIG.EVENT_MOUSEMOVE, 	function(event){ if( isOverOffset(event) ){showButtons();} else {hideButtons()}} , false );
		// If the mouse is over the button, show them
		buttonGrow.addEventListener(		ORYX.CONFIG.EVENT_MOUSEOVER, 	function(event){showButtons();}, true );
		buttonShrink.addEventListener(		ORYX.CONFIG.EVENT_MOUSEOVER, 	function(event){showButtons();}, true );
		// If the mouse is out, hide the button
		//scrollNode.addEventListener(		ORYX.CONFIG.EVENT_MOUSEOUT, 	function(event){button.hide()}, true )
		parentNode.parentNode.addEventListener(	ORYX.CONFIG.EVENT_MOUSEOUT, 	function(event){hideButtons()} , true );
		//svgRootNode.addEventListener(	ORYX.CONFIG.EVENT_MOUSEOUT, 	function(event){ inCanvas = false } , true );
        
		// Hide the button initialy
		hideButtons();

		// Add the callbacks
	    buttonGrow.addEventListener('click', function(){callback( position ); showButtons();}, true);
	    buttonShrink.addEventListener('click', function(){callback( position, true ); showButtons();}, true);

	}
	

});

/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

if (!ORYX.Plugins) 
    ORYX.Plugins = new Object();

ORYX.Plugins.RenameShapes = Clazz.extend({

    facade: undefined,
    
    construct: function(facade){
    
        this.facade = facade;
      	
        this.facade.registerOnEvent(ORYX.CONFIG.EVENT_CANVAS_SCROLL, this.hideField.bind(this)); 
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_DBLCLICK, this.actOnDBLClick.bind(this)); 
		this.facade.offer({
		 keyCodes: [{
				keyCode: 113, // F2-Key
				keyAction: ORYX.CONFIG.KEY_ACTION_DOWN 
			}
		 ],
         functionality: this.renamePerF2.bind(this)
         });
		
		
		document.documentElement.addEventListener(ORYX.CONFIG.EVENT_MOUSEDOWN, this.hide.bind(this), true);
    },
	
	/**
	 * This method handles the "F2" key down event. The selected shape are looked
	 * up and the editing of title/name of it gets started.
	 */
	renamePerF2 : function() {
		var selectedShapes = this.facade.getSelection();
		this.actOnDBLClick(undefined, selectedShapes.first());
	},
	
	actOnDBLClick: function(evt, shape){
		
		if( !(shape instanceof ORYX.Core.Shape) ){ return; }
		
		// Destroys the old input, if there is one
		this.destroy();
		
		// Get all properties which where at least one ref to view is set
		var props = shape.getStencil().properties().findAll(function(item){ 
			return (item.refToView() 
					&&  item.refToView().length > 0
					&&	item.directlyEditable()); 
		});
		// from these, get all properties where write access are and the type is String or Expression
		props = props.findAll(function(item){ return !item.readonly() &&  (item.type() == ORYX.CONFIG.TYPE_STRING || item.type() == ORYX.CONFIG.TYPE_EXPRESSION || item.type() == ORYX.CONFIG.TYPE_DATASOURCE); });
		
		// Get all ref ids
		var allRefToViews	= props.collect(function(prop){ return prop.refToView(); }).flatten().compact();
		// Get all labels from the shape with the ref ids
		var labels			= shape.getLabels().findAll(function(label){ return allRefToViews.any(function(toView){ return label.id.endsWith(toView); }); });
		
		// If there are no referenced labels --> return
		if( labels.length == 0 ){ return; } 
		
		// Define the nearest label
		var nearestLabel 	= labels.length <= 1 ? labels[0] : null;	
		if( !nearestLabel ){
			nearestLabel = labels.find(function(label){ return label.node == evt.target || label.node == evt.target.parentNode; });
			if( !nearestLabel ){
				
				var evtCoord 	= this.facade.eventCoordinates(evt);
				
				var additionalIEZoom = 1;
                if (!isNaN(screen.logicalXDPI) && !isNaN(screen.systemXDPI)) {
                    var ua = navigator.userAgent;
                    if (ua.indexOf('MSIE') >= 0) {
                        //IE 10 and below
                        var zoom = Math.round((screen.deviceXDPI / screen.logicalXDPI) * 100);
                        if (zoom !== 100) {
                            additionalIEZoom = zoom / 100
                        }
                    }
                }
                
                if (additionalIEZoom !== 1) {
                     evtCoord.x = evtCoord.x / additionalIEZoom;
                     evtCoord.y = evtCoord.y / additionalIEZoom;
                }

				evtCoord.y += $("editor-header").clientHeight - $("canvasSection").scrollTop - 5;
				if (KISBPM.HEADER_CONFIG.showAppTitle == false)
				{
					evtCoord.y += 61;
				}
				
				evtCoord.x -= $("canvasSection").scrollLeft;
				
				var trans		= this.facade.getCanvas().rootNode.lastChild.getScreenCTM();
				evtCoord.x		*= trans.a;
				evtCoord.y		*= trans.d;

				var diff = labels.collect(function(label){ 
							var center 	= this.getCenterPosition( label.node ); 
							var len 	= Math.sqrt( Math.pow(center.x - evtCoord.x, 2) + Math.pow(center.y - evtCoord.y, 2));
							return {diff: len, label: label}; 
						}.bind(this));
				
				diff.sort(function(a, b){ return a.diff > b.diff; });
				
				nearestLabel = 	diff[0].label;

			}
		}
		// Get the particular property for the label
		var prop 			= props.find(function(item){ return item.refToView().any(function(toView){ return nearestLabel.id == shape.id + toView; });});
		
		// Get the center position from the nearest label
		var width		= Math.min(Math.max(100, shape.bounds.width()), 200);
		var center 		= this.getCenterPosition( nearestLabel.node, shape );
		center.x		-= (width/2);
		var propId		= prop.prefix() + "-" + prop.id();
		var textInput = document.createElement("textarea");
		textInput.id = 'shapeTextInput';
		textInput.style.position = 'absolute';
		textInput.style.width = width + 'px';
		textInput.style.left = (center.x < 10) ? 10 : center.x + 'px';
		textInput.style.top = (center.y - 15) + 'px';
		textInput.className = 'x-form-textarea x-form-field x_form_text_set_absolute';
		textInput.value = shape.properties[propId];
		this.oldValueText = shape.properties[propId];
		document.getElementById('canvasSection').appendChild(textInput);
		this.shownTextField = textInput;
		
		
		// Value change listener needs to be defined now since we reference it in the text field
		this.updateValueFunction = function(newValue, oldValue) {
			var currentEl 	= shape;
			var facade		= this.facade;
			
			if (oldValue != newValue) {
				// Implement the specific command for property change
				var commandClass = ORYX.Core.Command.extend({
					construct: function(){
						this.el = currentEl;
						this.propId = propId;
						this.oldValue = oldValue;
						this.newValue = newValue;
						this.facade = facade;
					},
					execute: function(){
						this.el.setProperty(this.propId, this.newValue);
						//this.el.update();
						this.facade.setSelection([this.el]);
						this.facade.getCanvas().update();
						this.facade.updateSelection();
					},
					rollback: function(){
						this.el.setProperty(this.propId, this.oldValue);
						//this.el.update();
						this.facade.setSelection([this.el]);
						this.facade.getCanvas().update();
						this.facade.updateSelection();
					}
				});
				// Instantiated the class
				var command = new commandClass();
				
				// Execute the command
				this.facade.executeCommands([command]);
			}
		}.bind(this);
			
		jQuery("#shapeTextInput").focus();
		
		jQuery("#shapeTextInput").autogrow();
			
		// Disable the keydown in the editor (that when hitting the delete button, the shapes not get deleted)
		this.facade.disableEvent(ORYX.CONFIG.EVENT_KEYDOWN);
		
	},
	
	getCenterPosition: function(svgNode, shape){
		
		if (!svgNode) { return {x:0, y:0}; }
		
		var scale = this.facade.getCanvas().node.getScreenCTM();
		var absoluteXY = shape.bounds.upperLeft();
		
		var hasParent = true;
		var searchShape = shape;
		while (hasParent)
		{
			if (searchShape.getParentShape().getStencil().idWithoutNs() === 'BPMNDiagram')
			{
				hasParent = false;
			}
			else
			{
				var parentXY = searchShape.getParentShape().bounds.upperLeft();
				absoluteXY.x += parentXY.x;
				absoluteXY.y += parentXY.y;
				searchShape = searchShape.getParentShape();
			}
		}
		
		var center = shape.bounds.midPoint();
		center.x += absoluteXY.x + scale.e;
		center.y += absoluteXY.y + scale.f;
		
		center.x *= scale.a;
		center.y *= scale.d;
		
		var additionalIEZoom = 1;
        if (!isNaN(screen.logicalXDPI) && !isNaN(screen.systemXDPI)) {
            var ua = navigator.userAgent;
            if (ua.indexOf('MSIE') >= 0) {
                //IE 10 and below
                var zoom = Math.round((screen.deviceXDPI / screen.logicalXDPI) * 100);
                if (zoom !== 100) {
                    additionalIEZoom = zoom / 100
                }
            }
        }
        
        if (additionalIEZoom === 1) {
             center.y = center.y - jQuery("#canvasSection").offset().top + 5;
             center.x -= jQuery("#canvasSection").offset().left;
        
        } else {
             var canvasOffsetLeft = jQuery("#canvasSection").offset().left;
             var canvasScrollLeft = jQuery("#canvasSection").scrollLeft();
             var canvasScrollTop = jQuery("#canvasSection").scrollTop();
             
             var offset = scale.e - (canvasOffsetLeft * additionalIEZoom);
             var additionaloffset = 0;
             if (offset > 10) {
                 additionaloffset = (offset / additionalIEZoom) - offset;
             }
             center.y = center.y - (jQuery("#canvasSection").offset().top * additionalIEZoom) + 5 + ((canvasScrollTop * additionalIEZoom) - canvasScrollTop);
             center.x = center.x - (canvasOffsetLeft * additionalIEZoom) + additionaloffset + ((canvasScrollLeft * additionalIEZoom) - canvasScrollLeft);
        }
		
	
		return center;			
	},
	
	hide: function(e){
		if (this.shownTextField && (!e || e.target !== this.shownTextField)) {
			var newValue = this.shownTextField.value;
			if (newValue !== this.oldValueText)
			{
				this.updateValueFunction(newValue, this.oldValueText);
			}
			this.destroy();
		}
	},
	
	hideField: function(e){
		if (this.shownTextField) {
			this.destroy();
		}
	},
	
	destroy: function(e){
		var textInputComp = jQuery("#shapeTextInput");
		if( textInputComp ){
			textInputComp.remove(); 
			delete this.shownTextField; 
			
			this.facade.enableEvent(ORYX.CONFIG.EVENT_KEYDOWN);
		}
	}
});
/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

if(!ORYX.Plugins)
	ORYX.Plugins = new Object();

/**
 * Supports EPCs by offering a syntax check and export and import ability..
 * 
 * 
 */
ORYX.Plugins.ProcessLink = Clazz.extend({

	facade: undefined,

	/**
	 * Offers the plugin functionality:
	 * 
	 */
	construct: function(facade) {

		this.facade = facade;
		
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_PROPERTY_CHANGED, this.propertyChanged.bind(this) );
		
	},


	/**
	 * 
	 * @param {Object} option
	 */
	propertyChanged: function( option, node){

		if( option.name !== "oryx-refuri" || !node instanceof ORYX.Core.Node ){ return }
		
		
		if( option.value && option.value.length > 0 && option.value != "undefined"){
			
			this.show( node, option.value );
					
		} else {

			this.hide( node );

		}				

	},
	
	/**
	 * Shows the Link for a particular shape with a specific url
	 * 
	 * @param {Object} shape
	 * @param {Object} url
	 */
	show: function( shape, url){

		
		// Generate the svg-representation of a link
		var link  = ORYX.Editor.graft("http://www.w3.org/2000/svg", null ,
					[ 'a',
						{'target': '_blank'},
						['path', 
							{ "stroke-width": 1.0, "stroke":"#00DD00", "fill": "#00AA00", "d":  "M3,3 l0,-2.5 l7.5,0 l0,-2.5 l7.5,4.5 l-7.5,3.5 l0,-2.5 l-8,0", "line-captions": "round"}
						]
					]);

		var link  = ORYX.Editor.graft("http://www.w3.org/2000/svg", null ,		
						[ 'a',
							{'target': '_blank'},
							['path', { "style": "fill:#92BFFC;stroke:#000000;stroke-linecap:round;stroke-linejoin:round;stroke-width:0.72", "d": "M0 1.44 L0 15.05 L11.91 15.05 L11.91 5.98 L7.37 1.44 L0 1.44 Z"}],
							['path', { "style": "stroke:#000000;stroke-linecap:round;stroke-linejoin:round;stroke-width:0.72;fill:none;", "transform": "translate(7.5, -8.5)", "d": "M0 10.51 L0 15.05 L4.54 15.05"}],
							['path', { "style": "fill:#f28226;stroke:#000000;stroke-linecap:round;stroke-linejoin:round;stroke-width:0.72", "transform": "translate(-3, -1)", "d": "M0 8.81 L0 13.06 L5.95 13.06 L5.95 15.05 A50.2313 50.2313 -175.57 0 0 10.77 11.08 A49.9128 49.9128 -1.28 0 0 5.95 6.54 L5.95 8.81 L0 8.81 Z"}],
						]);

	/*
	 * 
	 * 					[ 'a',
						{'target': '_blank'},
						['path', { "style": "fill:none;stroke-width:0.5px; stroke:#000000", "d": "M7,4 l0,2"}],
						['path', { "style": "fill:none;stroke-width:0.5px; stroke:#000000", "d": "M4,8 l-2,0 l0,6"}],
						['path', { "style": "fill:none;stroke-width:0.5px; stroke:#000000", "d": "M10,8 l2,0 l0,6"}],
						['rect', { "style": "fill:#96ff96;stroke:#000000;stroke-width:1", "width": 6, "height": 4, "x": 4, "y": 0}],
						['rect', { "style": "fill:#ffafff;stroke:#000000;stroke-width:1", "width": 6, "height": 4, "x": 4, "y": 6}],
						['rect', { "style": "fill:#96ff96;stroke:#000000;stroke-width:1", "width": 6, "height": 4, "x": 0, "y": 12}],
						['rect', { "style": "fill:#96ff96;stroke:#000000;stroke-width:1", "width": 6, "height": 4, "x": 8, "y": 12}],
						['rect', { "style": "fill:none;stroke:none;pointer-events:all", "width": 14, "height": 16, "x": 0, "y": 0}]
					]);
	 */
		
		// Set the link with the special namespace
		link.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", url);
		
		
		// Shows the link in the overlay					
		this.facade.raiseEvent({
					type: 			ORYX.CONFIG.EVENT_OVERLAY_SHOW,
					id: 			"arissupport.urlref_" + shape.id,
					shapes: 		[shape],
					node:			link,
					nodePosition:	"SE"
				});	
							
	},	

	/**
	 * Hides the Link for a particular shape
	 * 
	 * @param {Object} shape
	 */
	hide: function( shape ){

		this.facade.raiseEvent({
					type: 			ORYX.CONFIG.EVENT_OVERLAY_HIDE,
					id: 			"arissupport.urlref_" + shape.id
				});	
							
	}		
});/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

Array.prototype.insertFrom = function(from, to){
	to 			= Math.max(0, to);
	from 		= Math.min( Math.max(0, from), this.length-1 );
		
	var el 		= this[from];
	var old 	= this.without(el);
	var newA 	= old.slice(0, to);
	newA.push(el);
	if(old.length > to ){
		newA 	= newA.concat(old.slice(to))
	};
	return newA;
}

if(!ORYX.Plugins)
	ORYX.Plugins = new Object();

ORYX.Plugins.Arrangement = ORYX.Plugins.AbstractPlugin.extend({

	facade: undefined,

	construct: function(facade) {
		this.facade = facade;

		// Z-Ordering
		/** Hide for SIGNAVIO 
		
		this.facade.offer({
			'name':ORYX.I18N.Arrangement.btf,
			'functionality': this.setZLevel.bind(this, this.setToTop),
			'group': ORYX.I18N.Arrangement.groupZ,
			'icon': ORYX.PATH + "images/shape_move_front.png",
			'description': ORYX.I18N.Arrangement.btfDesc,
			'index': 1,
			'minShape': 1});
			
		this.facade.offer({
			'name':ORYX.I18N.Arrangement.btb,
			'functionality': this.setZLevel.bind(this, this.setToBack),
			'group': ORYX.I18N.Arrangement.groupZ,
			'icon': ORYX.PATH + "images/shape_move_back.png",
			'description': ORYX.I18N.Arrangement.btbDesc,
			'index': 2,
			'minShape': 1});

		this.facade.offer({
			'name':ORYX.I18N.Arrangement.bf,
			'functionality': this.setZLevel.bind(this, this.setForward),
			'group': ORYX.I18N.Arrangement.groupZ,
			'icon': ORYX.PATH + "images/shape_move_forwards.png",
			'description': ORYX.I18N.Arrangement.bfDesc,
			'index': 3,
			'minShape': 1});

		this.facade.offer({
			'name':ORYX.I18N.Arrangement.bb,
			'functionality': this.setZLevel.bind(this, this.setBackward),
			'group': ORYX.I18N.Arrangement.groupZ,
			'icon': ORYX.PATH + "images/shape_move_backwards.png",
			'description': ORYX.I18N.Arrangement.bbDesc,
			'index': 4,
			'minShape': 1});

		// Aligment
		this.facade.offer({
			'name':ORYX.I18N.Arrangement.ab,
			'functionality': this.alignShapes.bind(this, [ORYX.CONFIG.EDITOR_ALIGN_BOTTOM]),
			'group': ORYX.I18N.Arrangement.groupA,
			'icon': ORYX.PATH + "images/shape_align_bottom.png",
			'description': ORYX.I18N.Arrangement.abDesc,
			'index': 1,
			'minShape': 2});



		this.facade.offer({
			'name':ORYX.I18N.Arrangement.at,
			'functionality': this.alignShapes.bind(this, [ORYX.CONFIG.EDITOR_ALIGN_TOP]),
			'group': ORYX.I18N.Arrangement.groupA,
			'icon': ORYX.PATH + "images/shape_align_top.png",
			'description': ORYX.I18N.Arrangement.atDesc,
			'index': 3,
			'minShape': 2});

		this.facade.offer({
			'name':ORYX.I18N.Arrangement.al,
			'functionality': this.alignShapes.bind(this, [ORYX.CONFIG.EDITOR_ALIGN_LEFT]),
			'group': ORYX.I18N.Arrangement.groupA,
			'icon': ORYX.PATH + "images/shape_align_left.png",
			'description': ORYX.I18N.Arrangement.alDesc,
			'index': 4,
			'minShape': 2});

		this.facade.offer({
			'name':ORYX.I18N.Arrangement.ar,
			'functionality': this.alignShapes.bind(this, [ORYX.CONFIG.EDITOR_ALIGN_RIGHT]),
			'group': ORYX.I18N.Arrangement.groupA,
			'icon': ORYX.PATH + "images/shape_align_right.png",
			'description': ORYX.I18N.Arrangement.arDesc,
			'index': 6,
			'minShape': 2});

		**/
		
		this.facade.offer({
			'name':ORYX.I18N.Arrangement.am,
			'functionality': this.alignShapes.bind(this, [ORYX.CONFIG.EDITOR_ALIGN_MIDDLE]),
			'group': ORYX.I18N.Arrangement.groupA,
			'icon': ORYX.PATH + "images/shape_align_middle.png",
			'description': ORYX.I18N.Arrangement.amDesc,
			'index': 1,
			'minShape': 2});
			
		this.facade.offer({
			'name':ORYX.I18N.Arrangement.ac,
			'functionality': this.alignShapes.bind(this, [ORYX.CONFIG.EDITOR_ALIGN_CENTER]),
			'group': ORYX.I18N.Arrangement.groupA,
			'icon': ORYX.PATH + "images/shape_align_center.png",
			'description': ORYX.I18N.Arrangement.acDesc,
			'index': 2,
			'minShape': 2});

			
		this.facade.offer({
			'name':ORYX.I18N.Arrangement.as,
			'functionality': this.alignShapes.bind(this, [ORYX.CONFIG.EDITOR_ALIGN_SIZE]),
			'group': ORYX.I18N.Arrangement.groupA,
			'icon': ORYX.PATH + "images/shape_align_size.png",
			'description': ORYX.I18N.Arrangement.asDesc,
			'index': 3,
			'minShape': 2});
			


		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_ARRANGEMENT_TOP, 	this.setZLevel.bind(this, this.setToTop)	);
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_ARRANGEMENT_BACK, 	this.setZLevel.bind(this, this.setToBack)	);
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_ARRANGEMENT_FORWARD, 	this.setZLevel.bind(this, this.setForward)	);
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_ARRANGEMENT_BACKWARD, 	this.setZLevel.bind(this, this.setBackward)	);						

	
	},
	
	onSelectionChanged: function(elemnt){
		var selection = this.facade.getSelection();
		if (selection.length === 1 && selection[0] instanceof ORYX.Core.Edge) {
			this.setToTop(selection);
		}
	},
	
	setZLevel:function(callback, event){
			
		//Command-Pattern for dragging one docker
		var zLevelCommand = ORYX.Core.Command.extend({
			construct: function(callback, elements, facade){
				this.callback 	= callback;
				this.elements 	= elements;
				// For redo, the previous elements get stored
				this.elAndIndex	= elements.map(function(el){ return {el:el, previous:el.parent.children[el.parent.children.indexOf(el)-1]} })
				this.facade		= facade;
			},			
			execute: function(){
				
				// Call the defined z-order callback with the elements
				this.callback( this.elements )			
				this.facade.setSelection( this.elements )
			},
			rollback: function(){
				
				// Sort all elements on the index of there containment
				var sortedEl =	this.elAndIndex.sortBy( function( el ) {
									var value 	= el.el;
									var t 		= $A(value.node.parentNode.childNodes);
									return t.indexOf(value.node);
								}); 
				
				// Every element get setted back bevor the old previous element
				for(var i=0; i<sortedEl.length; i++){
					var el			= sortedEl[i].el;
					var p 			= el.parent;			
					var oldIndex 	= p.children.indexOf(el);
					var newIndex 	= p.children.indexOf(sortedEl[i].previous);
					newIndex		= newIndex || 0
					p.children 	= p.children.insertFrom(oldIndex, newIndex)			
					el.node.parentNode.insertBefore(el.node, el.node.parentNode.childNodes[newIndex+1]);
				}

				// Reset the selection
				this.facade.setSelection( this.elements )
			}
		});
	
		// Instanziate the dockCommand
		var command = new zLevelCommand(callback, this.facade.getSelection(), this.facade);
		if( event.excludeCommand ){
			command.execute();
		} else {
			this.facade.executeCommands( [command] );	
		}
		
	},

	setToTop: function(elements) {

		// Sortieren des Arrays nach dem Index des SVGKnotens im Bezug auf dem Elternknoten.
		var tmpElem =  elements.sortBy( function(value, index) {
			var t = $A(value.node.parentNode.childNodes);
			return t.indexOf(value.node);
		});
		// Sortiertes Array wird nach oben verschoben.
		tmpElem.each( function(value) {
			var p = value.parent;
			if (p.children.last() === value){
				return;
			}
			p.children = p.children.without( value )
			p.children.push(value);
			value.node.parentNode.appendChild(value.node);			
		});
	},

	setToBack: function(elements) {
		// Sortieren des Arrays nach dem Index des SVGKnotens im Bezug auf dem Elternknoten.
		var tmpElem =  elements.sortBy( function(value, index) {
			var t = $A(value.node.parentNode.childNodes);
			return t.indexOf(value.node);
		});

		tmpElem = tmpElem.reverse();

		// Sortiertes Array wird nach unten verschoben.
		tmpElem.each( function(value) {
			var p = value.parent
			p.children = p.children.without( value )
			p.children.unshift( value );
			value.node.parentNode.insertBefore(value.node, value.node.parentNode.firstChild);
		});
		
		
	},

	setBackward: function(elements) {
		// Sortieren des Arrays nach dem Index des SVGKnotens im Bezug auf dem Elternknoten.
		var tmpElem =  elements.sortBy( function(value, index) {
			var t = $A(value.node.parentNode.childNodes);
			return t.indexOf(value.node);
		});

		// Reverse the elements
		tmpElem = tmpElem.reverse();
		
		// Delete all Nodes who are the next Node in the nodes-Array
		var compactElem = tmpElem.findAll(function(el) {return !tmpElem.some(function(checkedEl){ return checkedEl.node == el.node.previousSibling})});
		
		// Sortiertes Array wird nach eine Ebene nach oben verschoben.
		compactElem.each( function(el) {
			if(el.node.previousSibling === null) { return; }
			var p 		= el.parent;			
			var index 	= p.children.indexOf(el);
			p.children 	= p.children.insertFrom(index, index-1)			
			el.node.parentNode.insertBefore(el.node, el.node.previousSibling);
		});
		
		
	},

	setForward: function(elements) {
		// Sortieren des Arrays nach dem Index des SVGKnotens im Bezug auf dem Elternknoten.
		var tmpElem =  elements.sortBy( function(value, index) {
			var t = $A(value.node.parentNode.childNodes);
			return t.indexOf(value.node);
		});


		// Delete all Nodes who are the next Node in the nodes-Array
		var compactElem = tmpElem.findAll(function(el) {return !tmpElem.some(function(checkedEl){ return checkedEl.node == el.node.nextSibling})});
	
			
		// Sortiertes Array wird eine Ebene nach unten verschoben.
		compactElem.each( function(el) {
			var nextNode = el.node.nextSibling		
			if(nextNode === null) { return; }
			var index 	= el.parent.children.indexOf(el);
			var p 		= el.parent;
			p.children 	= p.children.insertFrom(index, index+1)			
			el.node.parentNode.insertBefore(nextNode, el.node);
		});
	},


	alignShapes: function(way) {

		var elements = this.facade.getSelection();

		// Set the elements to all Top-Level elements
		elements = this.facade.getCanvas().getShapesWithSharedParent(elements);
		// Get only nodes
		elements = elements.findAll(function(value) {
			return (value instanceof ORYX.Core.Node)
		});
		// Delete all attached intermediate events from the array
		elements = elements.findAll(function(value) {
			var d = value.getIncomingShapes()
			return d.length == 0 || !elements.include(d[0])
		});
		if(elements.length < 2) { return; }

		// get bounds of all shapes.
		var bounds = elements[0].absoluteBounds().clone();
		elements.each(function(shape) {
		        bounds.include(shape.absoluteBounds().clone());
		});
		
		// get biggest width and heigth
		var maxWidth = 0;
		var maxHeight = 0;
		elements.each(function(shape){
			maxWidth = Math.max(shape.bounds.width(), maxWidth);
			maxHeight = Math.max(shape.bounds.height(), maxHeight);
		});

		var commandClass = ORYX.Core.Command.extend({
			construct: function(elements, bounds, maxHeight, maxWidth, way, plugin){
				this.elements = elements;
				this.bounds = bounds;
				this.maxHeight = maxHeight;
				this.maxWidth = maxWidth;
				this.way = way;
				this.facade = plugin.facade;
				this.plugin = plugin;
				this.orgPos = [];
			},
			setBounds: function(shape, maxSize) {
				if(!maxSize)
					maxSize = {width: ORYX.CONFIG.MAXIMUM_SIZE, height: ORYX.CONFIG.MAXIMUM_SIZE};

				if(!shape.bounds) { throw "Bounds not definined." }
				
				var newBounds = {
                    a: {x: shape.bounds.upperLeft().x - (this.maxWidth - shape.bounds.width())/2,
                        y: shape.bounds.upperLeft().y - (this.maxHeight - shape.bounds.height())/2},
                    b: {x: shape.bounds.lowerRight().x + (this.maxWidth - shape.bounds.width())/2,
                        y: shape.bounds.lowerRight().y + (this.maxHeight - shape.bounds.height())/2}
	            }
				
				/* If the new width of shape exceeds the maximum width, set width value to maximum. */
				if(this.maxWidth > maxSize.width) {
					newBounds.a.x = shape.bounds.upperLeft().x - 
									(maxSize.width - shape.bounds.width())/2;
					
					newBounds.b.x =	shape.bounds.lowerRight().x + (maxSize.width - shape.bounds.width())/2
				}
				
				/* If the new height of shape exceeds the maximum height, set height value to maximum. */
				if(this.maxHeight > maxSize.height) {
					newBounds.a.y = shape.bounds.upperLeft().y - 
									(maxSize.height - shape.bounds.height())/2;
					
					newBounds.b.y =	shape.bounds.lowerRight().y + (maxSize.height - shape.bounds.height())/2
				}
				
				/* set bounds of shape */
				shape.bounds.set(newBounds);
				
			},			
			execute: function(){
				// align each shape according to the way that was specified.
				this.elements.each(function(shape, index) {
					this.orgPos[index] = shape.bounds.upperLeft();
					
					var relBounds = this.bounds.clone();
					var newCoordinates;
					if (shape.parent && !(shape.parent instanceof ORYX.Core.Canvas) ) {
						var upL = shape.parent.absoluteBounds().upperLeft();
						relBounds.moveBy(-upL.x, -upL.y);
					}
					
					switch (this.way) {
						// align the shapes in the requested way.
						case ORYX.CONFIG.EDITOR_ALIGN_BOTTOM:
			                newCoordinates = {
								x: shape.bounds.upperLeft().x,
								y: relBounds.b.y - shape.bounds.height()
							}; break;
		
				        case ORYX.CONFIG.EDITOR_ALIGN_MIDDLE:
			                newCoordinates = {
								x: shape.bounds.upperLeft().x,
								y: (relBounds.a.y + relBounds.b.y - shape.bounds.height()) / 2
							}; break;
		
				        case ORYX.CONFIG.EDITOR_ALIGN_TOP:
			                newCoordinates = {
								x: shape.bounds.upperLeft().x,
								y: relBounds.a.y
							}; break;
		
				        case ORYX.CONFIG.EDITOR_ALIGN_LEFT:
			                newCoordinates = {
								x: relBounds.a.x,
								y: shape.bounds.upperLeft().y
							}; break;
		
				        case ORYX.CONFIG.EDITOR_ALIGN_CENTER:
			                newCoordinates = {
								x: (relBounds.a.x + relBounds.b.x - shape.bounds.width()) / 2,
								y: shape.bounds.upperLeft().y
							}; break;
		
				        case ORYX.CONFIG.EDITOR_ALIGN_RIGHT:
			                newCoordinates = {
								x: relBounds.b.x - shape.bounds.width(),
								y: shape.bounds.upperLeft().y
							}; break;
							
						case ORYX.CONFIG.EDITOR_ALIGN_SIZE:
							if(shape.isResizable) {
								this.orgPos[index] = {a: shape.bounds.upperLeft(), b: shape.bounds.lowerRight()};
								this.setBounds(shape, shape.maximumSize);
							}
							break;
					}
					
					if (newCoordinates){
						var offset =  {
							x: shape.bounds.upperLeft().x - newCoordinates.x,
							y: shape.bounds.upperLeft().y - newCoordinates.y
						}
						// Set the new position
						shape.bounds.moveTo(newCoordinates);
						this.plugin.layoutEdges(shape, shape.getAllDockedShapes(),offset);
						//shape.update()
					}			
				}.bind(this));
		
				//this.facade.getCanvas().update();
				//this.facade.updateSelection();
			},
			rollback: function(){
				this.elements.each(function(shape, index) {
					if (this.way == ORYX.CONFIG.EDITOR_ALIGN_SIZE) {
						if(shape.isResizable) {shape.bounds.set(this.orgPos[index]);}
					} else {shape.bounds.moveTo(this.orgPos[index]);}
				}.bind(this));
				
				//this.facade.getCanvas().update();
				//this.facade.updateSelection();
			}
		})
		
		var command = new commandClass(elements, bounds, maxHeight, maxWidth, parseInt(way), this);
		
		this.facade.executeCommands([command]);	
	}
});/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

if (!ORYX.Plugins) 
    ORYX.Plugins = new Object();

ORYX.Plugins.Save = Clazz.extend({
	
    facade: undefined,
	
	processURI: undefined,
	
	changeSymbol : "*",
	
    construct: function(facade){
		this.facade = facade;
		
		document.addEventListener("keydown", function(e){
			if (e.ctrlKey&&e.keyCode === 83){
				Event.stop(e);
			}
		}, false);
		
		window.onbeforeunload = this.onUnLoad.bind(this);
		
		this.changeDifference = 0;
		
		// Register on event for executing commands --> store all commands in a stack		 
		// --> Execute
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_UNDO_EXECUTE, function(){ this.changeDifference++; this.updateTitle(); }.bind(this) );
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_EXECUTE_COMMANDS, function(){ this.changeDifference++; this.updateTitle(); }.bind(this) );
		// --> Saved from other places in the editor
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_SAVED, function(){ this.changeDifference = 0; this.updateTitle(); }.bind(this) );
		
		// --> Rollback
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_UNDO_ROLLBACK, function(){ this.changeDifference--; this.updateTitle(); }.bind(this) );
		
		//TODO very critical for load time performance!!!
		//this.serializedDOM = DataManager.__persistDOM(this.facade);
		
		this.hasChanges = this._hasChanges.bind(this);
	},
	
	updateTitle: function(){
		
		var value = window.document.title || document.getElementsByTagName("title")[0].childNodes[0].nodeValue;
		
		if (this.changeDifference === 0 && value.startsWith(this.changeSymbol)){
			window.document.title = value.slice(1);
		} else if (this.changeDifference !== 0 && !value.startsWith(this.changeSymbol)){
			window.document.title = this.changeSymbol + "" + value;
		}
	},
	
	_hasChanges: function() {
	  return this.changeDifference !== 0 || (this.facade.getModelMetaData()['new'] && this.facade.getCanvas().getChildShapes().size() > 0);
	},
	
	onUnLoad: function(){
		if(this._hasChanges()) {
			return ORYX.I18N.Save.unsavedData;
		}	
	}
});
/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

if(!ORYX.Plugins) 
	ORYX.Plugins = new Object();

ORYX.Plugins.DragDropResize = ORYX.Plugins.AbstractPlugin.extend({

	/**
	 *	Constructor
	 *	@param {Object} Facade: The Facade of the Editor
	 */
	construct: function(facade) {
		this.facade = facade;

		// Initialize variables
		this.currentShapes 		= [];			// Current selected Shapes
		//this.pluginsData 		= [];			// Available Plugins
		this.toMoveShapes 		= [];			// Shapes there will be moved
		this.distPoints 		= [];			// Distance Points for Snap on Grid
		this.isResizing 		= false;		// Flag: If there was currently resized
		this.dragEnable 		= false;		// Flag: If Dragging is enabled
		this.dragIntialized 	= false;		// Flag: If the Dragging is initialized
		this.edgesMovable		= true;			// Flag: If an edge is docked it is not movable
		this.offSetPosition 	= {x: 0, y: 0};	// Offset of the Dragging
		this.faktorXY 			= {x: 1, y: 1};	// The Current Zoom-Faktor
		this.containmentParentNode;				// the current future parent node for the dragged shapes
		this.isAddingAllowed 	= false;		// flag, if adding current selected shapes to containmentParentNode is allowed
		this.isAttachingAllowed = false;		// flag, if attaching to the current shape is allowed
		
		this.callbackMouseMove	= this.handleMouseMove.bind(this);
		this.callbackMouseUp	= this.handleMouseUp.bind(this);
		
		// Get the SVG-Containernode 
		var containerNode = this.facade.getCanvas().getSvgContainer();
		
		// Create the Selected Rectangle in the SVG
		this.selectedRect = new ORYX.Plugins.SelectedRect(containerNode);
		
		// Show grid line if enabled
		if (ORYX.CONFIG.SHOW_GRIDLINE) {
			this.vLine = new ORYX.Plugins.GridLine(containerNode, ORYX.Plugins.GridLine.DIR_VERTICAL);
			this.hLine = new ORYX.Plugins.GridLine(containerNode, ORYX.Plugins.GridLine.DIR_HORIZONTAL);
		}
		
		// Get a HTML-ContainerNode
		containerNode = this.facade.getCanvas().getHTMLContainer();
		
		this.scrollNode = this.facade.getCanvas().rootNode.parentNode.parentNode;
		
		// Create the southeastern button for resizing
		this.resizerSE = new ORYX.Plugins.Resizer(containerNode, "southeast", this.facade);
		this.resizerSE.registerOnResize(this.onResize.bind(this)); // register the resize callback
		this.resizerSE.registerOnResizeEnd(this.onResizeEnd.bind(this)); // register the resize end callback
		this.resizerSE.registerOnResizeStart(this.onResizeStart.bind(this)); // register the resize start callback
		
		// Create the northwestern button for resizing
		this.resizerNW = new ORYX.Plugins.Resizer(containerNode, "northwest", this.facade);
		this.resizerNW.registerOnResize(this.onResize.bind(this)); // register the resize callback
		this.resizerNW.registerOnResizeEnd(this.onResizeEnd.bind(this)); // register the resize end callback
		this.resizerNW.registerOnResizeStart(this.onResizeStart.bind(this)); // register the resize start callback
		
		// For the Drag and Drop
		// Register on MouseDown-Event on a Shape
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_MOUSEDOWN, this.handleMouseDown.bind(this));
	},

	/**
	 * On Mouse Down
	 *
	 */
	handleMouseDown: function(event, uiObj) {
		// If the selection Bounds not intialized and the uiObj is not member of current selectio
		// then return
		if(!this.dragBounds || !this.currentShapes.member(uiObj) || !this.toMoveShapes.length) {return;};
		
		// Start Dragging
		this.dragEnable = true;
		this.dragIntialized = true;
		this.edgesMovable = true;

		// Calculate the current zoom factor
		var a = this.facade.getCanvas().node.getScreenCTM();
		this.faktorXY.x = a.a;
		this.faktorXY.y = a.d;
		
		var eventX = Event.pointerX(event);
		var eventY = Event.pointerY(event);

		// Set the offset position of dragging
		var upL = this.dragBounds.upperLeft();
		this.offSetPosition =  {
			x: eventX - (upL.x * this.faktorXY.x),
			y: eventY - (upL.y * this.faktorXY.y)};
		
		this.offsetScroll	= {x:this.scrollNode.scrollLeft,y:this.scrollNode.scrollTop};
			
		// Register on Global Mouse-MOVE Event
		document.documentElement.addEventListener(ORYX.CONFIG.EVENT_MOUSEMOVE, this.callbackMouseMove, false);	
		// Register on Global Mouse-UP Event
		document.documentElement.addEventListener(ORYX.CONFIG.EVENT_MOUSEUP, this.callbackMouseUp, true);			

		return;
	},

	/**
	 * On Key Mouse Up
	 *
	 */
	handleMouseUp: function(event) {
		
		//disable containment highlighting
		this.facade.raiseEvent({
									type:ORYX.CONFIG.EVENT_HIGHLIGHT_HIDE,
									highlightId:"dragdropresize.contain"
								});
								
		this.facade.raiseEvent({
									type:ORYX.CONFIG.EVENT_HIGHLIGHT_HIDE,
									highlightId:"dragdropresize.attached"
								});

		// If Dragging is finished
		if(this.dragEnable) {
		
			// and update the current selection
			if(!this.dragIntialized) {
				
				// Do Method after Dragging
				this.afterDrag();	
				
				// Check if the Shape is allowed to dock to the other Shape						
				if ( 	this.isAttachingAllowed &&
						this.toMoveShapes.length == 1 && this.toMoveShapes[0] instanceof ORYX.Core.Node  &&
						this.toMoveShapes[0].dockers.length > 0) {
					
					// Get the position and the docker					
					var position 	= this.facade.eventCoordinates( event );	
					var docker 		= this.toMoveShapes[0].dockers[0];


			
					//Command-Pattern for dragging several Shapes
					var dockCommand = ORYX.Core.Command.extend({
						construct: function(docker, position, newDockedShape, facade){
							this.docker 		= docker;
							this.newPosition	= position;
							this.newDockedShape = newDockedShape;
							this.newParent 		= newDockedShape.parent || facade.getCanvas();
							this.oldPosition	= docker.parent.bounds.center();
							this.oldDockedShape	= docker.getDockedShape();
							this.oldParent 		= docker.parent.parent || facade.getCanvas();
							this.facade			= facade;
							
							if( this.oldDockedShape ){
								this.oldPosition = docker.parent.absoluteBounds().center();
							}
							
						},			
						execute: function(){
							this.dock( this.newDockedShape, this.newParent,  this.newPosition );
							
							// Raise Event for having the docked shape on top of the other shape
							this.facade.raiseEvent({type:ORYX.CONFIG.EVENT_ARRANGEMENT_TOP, excludeCommand: true})									
						},
						rollback: function(){
							this.dock( this.oldDockedShape, this.oldParent, this.oldPosition );
						},
						dock:function( toDockShape, parent, pos ){
							// Add to the same parent Shape
							parent.add( this.docker.parent )
							
							
							// Set the Docker to the new Shape
							this.docker.setDockedShape( undefined );
							this.docker.bounds.centerMoveTo( pos )				
							this.docker.setDockedShape( toDockShape );	
							//this.docker.update();
							
							this.facade.setSelection( [this.docker.parent] );	
							this.facade.getCanvas().update();
							this.facade.updateSelection();
																												
											
						}
					});
			
					// Instanziate the dockCommand
					var commands = [new dockCommand(docker, position, this.containmentParentNode, this.facade)];
					this.facade.executeCommands(commands);	
						
					
				// Check if adding is allowed to the other Shape	
				} else if( this.isAddingAllowed ) {
					
				
					// Refresh all Shapes --> Set the new Bounds
					this.refreshSelectedShapes();
					
				}
				
				this.facade.updateSelection();
							
				//this.currentShapes.each(function(shape) {shape.update()})
				// Raise Event: Dragging is finished
				this.facade.raiseEvent({type:ORYX.CONFIG.EVENT_DRAGDROP_END});
			}	

			if (this.vLine)
				this.vLine.hide();
			if (this.hLine)
				this.hLine.hide();
		}

		// Disable 
		this.dragEnable = false;	
		

		// UnRegister on Global Mouse-UP/-Move Event
		document.documentElement.removeEventListener(ORYX.CONFIG.EVENT_MOUSEUP, this.callbackMouseUp, true);	
		document.documentElement.removeEventListener(ORYX.CONFIG.EVENT_MOUSEMOVE, this.callbackMouseMove, false);				
			
		return;
	},

	/**
	* On Key Mouse Move
	*
	*/
	handleMouseMove: function(event) {
		// If dragging is not enabled, go return
		if(!this.dragEnable) { return };
		// If Dragging is initialized
		if(this.dragIntialized) {
			// Raise Event: Drag will be started
			this.facade.raiseEvent({type:ORYX.CONFIG.EVENT_DRAGDROP_START});
			this.dragIntialized = false;
			
			// And hide the resizers and the highlighting
			this.resizerSE.hide();
			this.resizerNW.hide();
			
			// if only edges are selected, containmentParentNode must be the canvas
			this._onlyEdges = this.currentShapes.all(function(currentShape) {
				return (currentShape instanceof ORYX.Core.Edge);
			});
			
			// Do method before Drag
			this.beforeDrag();
			
			this._currentUnderlyingNodes = [];
			
		}

			
		// Calculate the new position
		var position = {
			x: Event.pointerX(event) - this.offSetPosition.x,
			y: Event.pointerY(event) - this.offSetPosition.y}

		position.x 	-= this.offsetScroll.x - this.scrollNode.scrollLeft; 
		position.y 	-= this.offsetScroll.y - this.scrollNode.scrollTop;
		
		// If not the Control-Key are pressed
		var modifierKeyPressed = event.shiftKey || event.ctrlKey;
		if(ORYX.CONFIG.GRID_ENABLED && !modifierKeyPressed) {
			// Snap the current position to the nearest Snap-Point
			position = this.snapToGrid(position);
		} else {
			if (this.vLine)
				this.vLine.hide();
			if (this.hLine)
				this.hLine.hide();
		}

		// Adjust the point by the zoom faktor 
		position.x /= this.faktorXY.x;
		position.y /= this.faktorXY.y;

		// Set that the position is not lower than zero
		position.x = Math.max( 0 , position.x)
		position.y = Math.max( 0 , position.y)

		// Set that the position is not bigger than the canvas
		var c = this.facade.getCanvas();
		position.x = Math.min( c.bounds.width() - this.dragBounds.width(), 		position.x)
		position.y = Math.min( c.bounds.height() - this.dragBounds.height(), 	position.y)	
						

		// Drag this bounds
		this.dragBounds.moveTo(position);

		// Update all selected shapes and the selection rectangle
		//this.refreshSelectedShapes();
		this.resizeRectangle(this.dragBounds);

		this.isAttachingAllowed = false;

		//check, if a node can be added to the underlying node
		var eventCoordinates = this.facade.eventCoordinates(event);
		
		var additionalIEZoom = 1;
        if (!isNaN(screen.logicalXDPI) && !isNaN(screen.systemXDPI)) {
            var ua = navigator.userAgent;
            if (ua.indexOf('MSIE') >= 0) {
                //IE 10 and below
                var zoom = Math.round((screen.deviceXDPI / screen.logicalXDPI) * 100);
                if (zoom !== 100) {
                    additionalIEZoom = zoom / 100
                }
            }
        }
        
        if (additionalIEZoom !== 1) {
             eventCoordinates.x = eventCoordinates.x / additionalIEZoom;
             eventCoordinates.y = eventCoordinates.y / additionalIEZoom;
        }
		
		var underlyingNodes = $A(this.facade.getCanvas().getAbstractShapesAtPosition(eventCoordinates));
		
		var checkIfAttachable = this.toMoveShapes.length == 1 && this.toMoveShapes[0] instanceof ORYX.Core.Node && this.toMoveShapes[0].dockers.length > 0
		checkIfAttachable	= checkIfAttachable && underlyingNodes.length != 1
		
			
		if (!checkIfAttachable &&
				underlyingNodes.length === this._currentUnderlyingNodes.length  &&
				underlyingNodes.all(function(node, index){return this._currentUnderlyingNodes[index] === node}.bind(this))) {
					
			return
			
		} else if(this._onlyEdges) {
			
			this.isAddingAllowed = true;
			this.containmentParentNode = this.facade.getCanvas();
			
		} else {
		
			/* Check the containment and connection rules */
			var options = {
				event : event,
				underlyingNodes : underlyingNodes,
				checkIfAttachable : checkIfAttachable
			};
			this.checkRules(options);
							
		}
		
		this._currentUnderlyingNodes = underlyingNodes.reverse();
		
		//visualize the containment result
		if( this.isAttachingAllowed ) {
			
			this.facade.raiseEvent({
									type: 			ORYX.CONFIG.EVENT_HIGHLIGHT_SHOW,
									highlightId: 	"dragdropresize.attached",
									elements: 		[this.containmentParentNode],
									style: 			ORYX.CONFIG.SELECTION_HIGHLIGHT_STYLE_RECTANGLE,
									color: 			ORYX.CONFIG.SELECTION_VALID_COLOR
								});
								
		} else {
			
			this.facade.raiseEvent({
									type:ORYX.CONFIG.EVENT_HIGHLIGHT_HIDE,
									highlightId:"dragdropresize.attached"
								});
		}
		
		if( !this.isAttachingAllowed ){
			if( this.isAddingAllowed ) {

				this.facade.raiseEvent({
										type:ORYX.CONFIG.EVENT_HIGHLIGHT_SHOW,
										highlightId:"dragdropresize.contain",
										elements:[this.containmentParentNode],
										color: ORYX.CONFIG.SELECTION_VALID_COLOR
									});

			} else {

				this.facade.raiseEvent({
										type:ORYX.CONFIG.EVENT_HIGHLIGHT_SHOW,
										highlightId:"dragdropresize.contain",
										elements:[this.containmentParentNode],
										color: ORYX.CONFIG.SELECTION_INVALID_COLOR
									});

			}
		} else {
			this.facade.raiseEvent({
									type:ORYX.CONFIG.EVENT_HIGHLIGHT_HIDE,
									highlightId:"dragdropresize.contain"
								});			
		}	

		// Stop the Event
		//Event.stop(event);
		return;
	},
	
//	/**
//	 * Rollbacks the docked shape of an edge, if the edge is not movable.
//	 */
//	redockEdges: function() {
//		this._undockedEdgesCommand.dockers.each(function(el){
//			el.docker.setDockedShape(el.dockedShape);
//			el.docker.setReferencePoint(el.refPoint);
//		})
//	},
	
	/**
	 *  Checks the containment and connection rules for the selected shapes.
	 */
	checkRules : function(options) {
		var event = options.event;
		var underlyingNodes = options.underlyingNodes;
		var checkIfAttachable = options.checkIfAttachable;
		var noEdges = options.noEdges;
		
		//get underlying node that is not the same than one of the currently selected shapes or
		// a child of one of the selected shapes with the highest z Order.
		// The result is a shape or the canvas
		this.containmentParentNode = underlyingNodes.reverse().find((function(node) {
			return (node instanceof ORYX.Core.Canvas) || 
					(((node instanceof ORYX.Core.Node) || ((node instanceof ORYX.Core.Edge) && !noEdges)) 
					&& (!(this.currentShapes.member(node) || 
							this.currentShapes.any(function(shape) {
								return (shape.children.length > 0 && shape.getChildNodes(true).member(node));
							}))));
		}).bind(this));
								
		if( checkIfAttachable ){
				
			this.isAttachingAllowed	= this.facade.getRules().canConnect({
												sourceShape:	this.containmentParentNode, 
												edgeShape:		this.toMoveShapes[0], 
												targetShape:	this.toMoveShapes[0]
												});						
			
			if ( this.isAttachingAllowed	) {
				var point = this.facade.eventCoordinates(event);
				this.isAttachingAllowed	= this.containmentParentNode.isPointOverOffset( point.x, point.y );
			}						
		}
		
		if( !this.isAttachingAllowed ){
			//check all selected shapes, if they can be added to containmentParentNode
			this.isAddingAllowed = this.toMoveShapes.all((function(currentShape) {
				if(currentShape instanceof ORYX.Core.Edge ||
					currentShape instanceof ORYX.Core.Controls.Docker ||
					this.containmentParentNode === currentShape.parent) {
					return true;
				} else if(this.containmentParentNode !== currentShape) {
					
					if(!(this.containmentParentNode instanceof ORYX.Core.Edge) || !noEdges) {
					
						if(this.facade.getRules().canContain({containingShape:this.containmentParentNode,
															  containedShape:currentShape})) {	  	
							return true;
						}
					}
				}
				return false;
			}).bind(this));				
		}
		
		if(!this.isAttachingAllowed && !this.isAddingAllowed && 
				(this.containmentParentNode instanceof ORYX.Core.Edge)) {
			options.noEdges = true;
			options.underlyingNodes.reverse();
			this.checkRules(options);			
		}
	},
	
	/**
	 * Redraw the selected Shapes.
	 *
	 */
	refreshSelectedShapes: function() {
		// If the selection bounds not initialized, return
		if(!this.dragBounds) {return}

		// Calculate the offset between the bounds and the old bounds
		var upL = this.dragBounds.upperLeft();
		var oldUpL = this.oldDragBounds.upperLeft();
		var offset = {
			x: upL.x - oldUpL.x,
			y: upL.y - oldUpL.y };

		// Instanciate the dragCommand
		var commands = [new ORYX.Core.Command.Move(this.toMoveShapes, offset, this.containmentParentNode, this.currentShapes, this)];
		// If the undocked edges command is setted, add this command
		if( this._undockedEdgesCommand instanceof ORYX.Core.Command ){
			commands.unshift( this._undockedEdgesCommand );
		}
		// Execute the commands			
		this.facade.executeCommands( commands );	

		// copy the bounds to the old bounds
		if( this.dragBounds )
			this.oldDragBounds = this.dragBounds.clone();

	},
	
	/**
	 * Callback for Resize
	 *
	 */
	onResize: function(bounds) {
		// If the selection bounds not initialized, return
		if(!this.dragBounds) {return}
		
		this.dragBounds = bounds;
		this.isResizing = true;

		// Update the rectangle 
		this.resizeRectangle(this.dragBounds);
	},
	
	onResizeStart: function() {
		this.facade.raiseEvent({type:ORYX.CONFIG.EVENT_RESIZE_START});
	},

	onResizeEnd: function() {
		
		if (!(this.currentShapes instanceof Array)||this.currentShapes.length<=0) {
			return;
		}
		
		// If Resizing finished, the Shapes will be resize
		if(this.isResizing) {
			
			var commandClass = ORYX.Core.Command.extend({
				construct: function(shape, newBounds, plugin){
					this.shape = shape;
					this.oldBounds = shape.bounds.clone();
					this.newBounds = newBounds;
					this.plugin = plugin;
				},			
				execute: function(){
					this.shape.bounds.set(this.newBounds.a, this.newBounds.b);
					this.update(this.getOffset(this.oldBounds, this.newBounds));
					
				},
				rollback: function(){
					this.shape.bounds.set(this.oldBounds.a, this.oldBounds.b);
					this.update(this.getOffset(this.newBounds, this.oldBounds))
				},
				
				getOffset:function(b1, b2){
					return {
						x: b2.a.x - b1.a.x,
						y: b2.a.y - b1.a.y,
						xs: b2.width()/b1.width(),
						ys: b2.height()/b1.height()
					}
				},
				update:function(offset){
					this.shape.getLabels().each(function(label) {
						label.changed();
					});
					
					var allEdges = [].concat(this.shape.getIncomingShapes())
						.concat(this.shape.getOutgoingShapes())
						// Remove all edges which are included in the selection from the list
						.findAll(function(r){ return r instanceof ORYX.Core.Edge }.bind(this))
												
					this.plugin.layoutEdges(this.shape, allEdges, offset);

					this.plugin.facade.setSelection([this.shape]);
					this.plugin.facade.getCanvas().update();
					this.plugin.facade.updateSelection();
				}
			});
			
			var bounds = this.dragBounds.clone();
			var shape = this.currentShapes[0];
			
			if(shape.parent) {
				var parentPosition = shape.parent.absoluteXY();
				bounds.moveBy(-parentPosition.x, -parentPosition.y);
			}
				
			var command = new commandClass(shape, bounds, this);
			
			this.facade.executeCommands([command]);
			
			this.isResizing = false;
			
			this.facade.raiseEvent({type:ORYX.CONFIG.EVENT_RESIZE_END});
		}
	},
	

	/**
	 * Prepare the Dragging
	 *
	 */
	beforeDrag: function(){

		var undockEdgeCommand = ORYX.Core.Command.extend({
			construct: function(moveShapes){
				this.dockers = moveShapes.collect(function(shape){ return shape instanceof ORYX.Core.Controls.Docker ? {docker:shape, dockedShape:shape.getDockedShape(), refPoint:shape.referencePoint} : undefined }).compact();
			},			
			execute: function(){
				this.dockers.each(function(el){
					el.docker.setDockedShape(undefined);
				})
			},
			rollback: function(){
				this.dockers.each(function(el){
					el.docker.setDockedShape(el.dockedShape);
					el.docker.setReferencePoint(el.refPoint);
					//el.docker.update();
				})
			}
		});
		
		this._undockedEdgesCommand = new undockEdgeCommand( this.toMoveShapes );
		this._undockedEdgesCommand.execute();	
		
	},

	hideAllLabels: function(shape) {
			
			// Hide all labels from the shape
			shape.getLabels().each(function(label) {
				label.hide();
			});
			// Hide all labels from docked shapes
			shape.getAllDockedShapes().each(function(dockedShape) {
				var labels = dockedShape.getLabels();
				if(labels.length > 0) {
					labels.each(function(label) {
						label.hide();
					});
				}
			});

			// Do this recursive for all child shapes
			// EXP-NICO use getShapes
			shape.getChildren().each((function(value) {
				if(value instanceof ORYX.Core.Shape)
					this.hideAllLabels(value);
			}).bind(this));
	},

	/**
	 * Finished the Dragging
	 *
	 */
	afterDrag: function(){
				
	},

	/**
	 * Show all Labels at these shape
	 * 
	 */
	showAllLabels: function(shape) {

			// Show the label of these shape
			//shape.getLabels().each(function(label) {
			for(var i=0; i<shape.length ;i++){
				var label = shape[i];
				label.show();
			}//);
			// Show all labels at docked shapes
			//shape.getAllDockedShapes().each(function(dockedShape) {
			var allDockedShapes = shape.getAllDockedShapes()
			for(var i=0; i<allDockedShapes.length ;i++){
				var dockedShape = allDockedShapes[i];				
				var labels = dockedShape.getLabels();
				if(labels.length > 0) {
					labels.each(function(label) {
						label.show();
					});
				}
			}//);

			// Do this recursive
			//shape.children.each((function(value) {
			for(var i=0; i<shape.children.length ;i++){
				var value = shape.children[i];	
				if(value instanceof ORYX.Core.Shape)
					this.showAllLabels(value);
			}//).bind(this));
	},

	/**
	 * Intialize Method, if there are new Plugins
	 *
	 */
	/*registryChanged: function(pluginsData) {
		// Save all new Plugin, sorted by group and index
		this.pluginsData = pluginsData.sortBy( function(value) {
			return (value.group + "" + value.index);
		});
	},*/

	/**
	 * On the Selection-Changed
	 *
	 */
	onSelectionChanged: function(event) {
		var elements = event.elements;
		
		// Reset the drag-variables
		this.dragEnable = false;
		this.dragIntialized = false;
		this.resizerSE.hide();
		this.resizerNW.hide();

		// If there is no elements
		if(!elements || elements.length == 0) {
			// Hide all things and reset all variables
			this.selectedRect.hide();
			this.currentShapes = [];
			this.toMoveShapes = [];
			this.dragBounds = undefined;
			this.oldDragBounds = undefined;
		} else {

			// Set the current Shapes
			this.currentShapes = elements;

			// Get all shapes with the highest parent in object hierarchy (canvas is the top most parent)
			var topLevelElements = this.facade.getCanvas().getShapesWithSharedParent(elements);
			this.toMoveShapes = topLevelElements;
			
			this.toMoveShapes = this.toMoveShapes.findAll( function(shape) { return shape instanceof ORYX.Core.Node && 
																			(shape.dockers.length === 0 || !elements.member(shape.dockers.first().getDockedShape()))});		
																			
			elements.each((function(shape){
				if(!(shape instanceof ORYX.Core.Edge)) {return;}
				
				var dks = shape.getDockers();
								
				var hasF = elements.member(dks.first().getDockedShape());
				var hasL = elements.member(dks.last().getDockedShape());	
						
//				if(!hasL) {
//					this.toMoveShapes.push(dks.last());
//				}
//				if(!hasF){
//					this.toMoveShapes.push(dks.first())
//				} 
				/* Enable movement of undocked edges */
				if(!hasF && !hasL) {
					var isUndocked = !dks.first().getDockedShape() && !dks.last().getDockedShape();
					if(isUndocked) {
						this.toMoveShapes = this.toMoveShapes.concat(dks);
					}
				}
				
				if( shape.dockers.length > 2 && hasF && hasL){
					this.toMoveShapes = this.toMoveShapes.concat(dks.findAll(function(el,index){ return index > 0 && index < dks.length-1}));
				}
				
			}).bind(this));
			
			// Calculate the new area-bounds of the selection
			var newBounds = undefined;
			this.toMoveShapes.each(function(value) {
				var shape = value;
				if(value instanceof ORYX.Core.Controls.Docker) {
					/* Get the Shape */
					shape = value.parent;
				}
				
				if(!newBounds){
					newBounds = shape.absoluteBounds();
				}
				else {
					newBounds.include(shape.absoluteBounds());
				}
			}.bind(this));
			
			if(!newBounds){
				elements.each(function(value){
					if(!newBounds) {
						newBounds = value.absoluteBounds();
					} else {
						newBounds.include(value.absoluteBounds());
					}
				});
			}
			
			// Set the new bounds
			this.dragBounds = newBounds;
			this.oldDragBounds = newBounds.clone();

			// Update and show the rectangle
			this.resizeRectangle(newBounds);
			this.selectedRect.show();
			
			// Show the resize button, if there is only one element and this is resizeable
			if(elements.length == 1 && elements[0].isResizable) {
				var aspectRatio = elements[0].getStencil().fixedAspectRatio() ? elements[0].bounds.width() / elements[0].bounds.height() : undefined;
				this.resizerSE.setBounds(this.dragBounds, elements[0].minimumSize, elements[0].maximumSize, aspectRatio);
				this.resizerSE.show();
				this.resizerNW.setBounds(this.dragBounds, elements[0].minimumSize, elements[0].maximumSize, aspectRatio);
				this.resizerNW.show();
			} else {
				this.resizerSE.setBounds(undefined);
				this.resizerNW.setBounds(undefined);
			}

			// If Snap-To-Grid is enabled, the Snap-Point will be calculate
			if(ORYX.CONFIG.GRID_ENABLED) {

				// Reset all points
				this.distPoints = [];

				if (this.distPointTimeout)
					window.clearTimeout(this.distPointTimeout)
				
				this.distPointTimeout = window.setTimeout(function(){
					// Get all the shapes, there will consider at snapping
					// Consider only those elements who shares the same parent element
					var distShapes = this.facade.getCanvas().getChildShapes(true).findAll(function(value){
						var parentShape = value.parent;
						while(parentShape){
							if(elements.member(parentShape)) return false;
							parentShape = parentShape.parent
						}
						return true;
					})
					
					// The current selection will delete from this array
					//elements.each(function(shape) {
					//	distShapes = distShapes.without(shape);
					//});

					// For all these shapes
					distShapes.each((function(value) {
						if(!(value instanceof ORYX.Core.Edge)) {
							var ul = value.absoluteXY();
							var width = value.bounds.width();
							var height = value.bounds.height();

							// Add the upperLeft, center and lowerRight - Point to the distancePoints
							this.distPoints.push({
								ul: {
									x: ul.x,
									y: ul.y
								},
								c: {
									x: ul.x + (width / 2),
									y: ul.y + (height / 2)
								},
								lr: {
									x: ul.x + width,
									y: ul.y + height
								}
							});
						}
					}).bind(this));
					
				}.bind(this), 10)


			}
		}
	},

	/**
	 * Adjust an Point to the Snap Points
	 *
	 */
	snapToGrid: function(position) {

		// Get the current Bounds
		var bounds = this.dragBounds;
		
		var point = {};

		var ulThres = 6;
		var cThres = 10;
		var lrThres = 6;

		var scale = this.vLine ? this.vLine.getScale() : 1;
		
		var ul = { x: (position.x/scale), y: (position.y/scale)};
		var c = { x: (position.x/scale) + (bounds.width()/2), y: (position.y/scale) + (bounds.height()/2)};
		var lr = { x: (position.x/scale) + (bounds.width()), y: (position.y/scale) + (bounds.height())};

		var offsetX, offsetY;
		var gridX, gridY;
		
		// For each distant point
		this.distPoints.each(function(value) {

			var x, y, gx, gy;
			if (Math.abs(value.c.x-c.x) < cThres){
				x = value.c.x-c.x;
				gx = value.c.x;
			}/* else if (Math.abs(value.ul.x-ul.x) < ulThres){
				x = value.ul.x-ul.x;
				gx = value.ul.x;
			} else if (Math.abs(value.lr.x-lr.x) < lrThres){
				x = value.lr.x-lr.x;
				gx = value.lr.x;
			} */
			

			if (Math.abs(value.c.y-c.y) < cThres){
				y = value.c.y-c.y;
				gy = value.c.y;
			}/* else if (Math.abs(value.ul.y-ul.y) < ulThres){
				y = value.ul.y-ul.y;
				gy = value.ul.y;
			} else if (Math.abs(value.lr.y-lr.y) < lrThres){
				y = value.lr.y-lr.y;
				gy = value.lr.y;
			} */

			if (x !== undefined) {
				offsetX = offsetX === undefined ? x : (Math.abs(x) < Math.abs(offsetX) ? x : offsetX);
				if (offsetX === x)
					gridX = gx;
			}

			if (y !== undefined) {
				offsetY = offsetY === undefined ? y : (Math.abs(y) < Math.abs(offsetY) ? y : offsetY);
				if (offsetY === y)
					gridY = gy;
			}
		});
		
		
		if (offsetX !== undefined) {
			ul.x += offsetX;	
			ul.x *= scale;
			if (this.vLine&&gridX)
				this.vLine.update(gridX);
		} else {
			ul.x = (position.x - (position.x % (ORYX.CONFIG.GRID_DISTANCE/2)));
			if (this.vLine)
				this.vLine.hide()
		}
		
		if (offsetY !== undefined) {	
			ul.y += offsetY;
			ul.y *= scale;
			if (this.hLine&&gridY)
				this.hLine.update(gridY);
		} else {
			ul.y = (position.y - (position.y % (ORYX.CONFIG.GRID_DISTANCE/2)));
			if (this.hLine)
				this.hLine.hide();
		}
		
		return ul;
	},
	
	showGridLine: function(){
		
	},


	/**
	 * Redraw of the Rectangle of the SelectedArea
	 * @param {Object} bounds
	 */
	resizeRectangle: function(bounds) {
		// Resize the Rectangle
		this.selectedRect.resize(bounds);
	}

});


ORYX.Plugins.SelectedRect = Clazz.extend({

	construct: function(parentId) {

		this.parentId = parentId;

		this.node = ORYX.Editor.graft("http://www.w3.org/2000/svg", $(parentId),
					['g']);

		this.dashedArea = ORYX.Editor.graft("http://www.w3.org/2000/svg", this.node,
			['rect', {x: 0, y: 0,
				'stroke-width': 1, stroke: '#777777', fill: 'none',
				'stroke-dasharray': '2,2',
				'pointer-events': 'none'}]);

		this.hide();

	},

	hide: function() {
		this.node.setAttributeNS(null, 'display', 'none');
	},

	show: function() {
		this.node.setAttributeNS(null, 'display', '');
	},

	resize: function(bounds) {
		var upL = bounds.upperLeft();

		var padding = ORYX.CONFIG.SELECTED_AREA_PADDING;

		this.dashedArea.setAttributeNS(null, 'width', bounds.width() + 2*padding);
		this.dashedArea.setAttributeNS(null, 'height', bounds.height() + 2*padding);
		this.node.setAttributeNS(null, 'transform', "translate("+ (upL.x - padding) +", "+ (upL.y - padding) +")");
	}


});



ORYX.Plugins.GridLine = Clazz.extend({
	
	construct: function(parentId, direction) {

		if (ORYX.Plugins.GridLine.DIR_HORIZONTAL !== direction && ORYX.Plugins.GridLine.DIR_VERTICAL !== direction) {
			direction = ORYX.Plugins.GridLine.DIR_HORIZONTAL
		}
		
	
		this.parent = $(parentId);
		this.direction = direction;
		this.node = ORYX.Editor.graft("http://www.w3.org/2000/svg", this.parent,
					['g']);

		this.line = ORYX.Editor.graft("http://www.w3.org/2000/svg", this.node,
			['path', {
				'stroke-width': 1, stroke: 'silver', fill: 'none',
				'stroke-dasharray': '5,5',
				'pointer-events': 'none'}]);

		this.hide();

	},

	hide: function() {
		this.node.setAttributeNS(null, 'display', 'none');
	},

	show: function() {
		this.node.setAttributeNS(null, 'display', '');
	},

	getScale: function(){
		try {
			return this.parent.parentNode.transform.baseVal.getItem(0).matrix.a;
		} catch(e) {
			return 1;
		}
	},
	
	update: function(pos) {
		
		if (this.direction === ORYX.Plugins.GridLine.DIR_HORIZONTAL) {
			var y = pos instanceof Object ? pos.y : pos; 
			var cWidth = this.parent.parentNode.parentNode.width.baseVal.value/this.getScale();
			this.line.setAttributeNS(null, 'd', 'M 0 '+y+ ' L '+cWidth+' '+y);
		} else {
			var x = pos instanceof Object ? pos.x : pos; 
			var cHeight = this.parent.parentNode.parentNode.height.baseVal.value/this.getScale();
			this.line.setAttributeNS(null, 'd', 'M'+x+ ' 0 L '+x+' '+cHeight);
		}
		
		this.show();
	}


});

ORYX.Plugins.GridLine.DIR_HORIZONTAL = "hor";
ORYX.Plugins.GridLine.DIR_VERTICAL = "ver";

ORYX.Plugins.Resizer = Clazz.extend({

	construct: function(parentId, orientation, facade) {

		this.parentId 		= parentId;
		this.orientation	= orientation;
		this.facade			= facade;
		
		this.node = ORYX.Editor.graft("http://www.w3.org/1999/xhtml", $('canvasSection'),
			['div', {'class': 'resizer_'+ this.orientation, style:'left:0px; top:0px;position:absolute;'}]);

		this.node.addEventListener(ORYX.CONFIG.EVENT_MOUSEDOWN, this.handleMouseDown.bind(this), true);
		document.documentElement.addEventListener(ORYX.CONFIG.EVENT_MOUSEUP, 	this.handleMouseUp.bind(this), 		true);
		document.documentElement.addEventListener(ORYX.CONFIG.EVENT_MOUSEMOVE, 	this.handleMouseMove.bind(this), 	false);

		this.dragEnable = false;
		this.offSetPosition = {x: 0, y: 0};
		this.bounds = undefined;

		this.canvasNode = this.facade.getCanvas().node;

		this.minSize = undefined;
		this.maxSize = undefined;
		
		this.aspectRatio = undefined;

		this.resizeCallbacks 		= [];
		this.resizeStartCallbacks 	= [];
		this.resizeEndCallbacks 	= [];
		this.hide();
		
		// Calculate the Offset
		this.scrollNode = this.node.parentNode.parentNode.parentNode;

	},

	handleMouseDown: function(event) {
		this.dragEnable = true;

		this.offsetScroll	= {x:this.scrollNode.scrollLeft,y:this.scrollNode.scrollTop};
			
		this.offSetPosition =  {
			x: Event.pointerX(event) - this.position.x,
			y: Event.pointerY(event) - this.position.y};
		
		this.resizeStartCallbacks.each((function(value) {
			value(this.bounds);
		}).bind(this));

	},

	handleMouseUp: function(event) {
		this.dragEnable = false;
		this.containmentParentNode = null;
		this.resizeEndCallbacks.each((function(value) {
			value(this.bounds);
		}).bind(this));
				
	},

	handleMouseMove: function(event) {
		if(!this.dragEnable) { return }
		
		if(event.shiftKey || event.ctrlKey) {
			this.aspectRatio = this.bounds.width() / this.bounds.height();
		} else {
			this.aspectRatio = undefined;
		}

		var position = {
			x: Event.pointerX(event) - this.offSetPosition.x,
			y: Event.pointerY(event) - this.offSetPosition.y};


		position.x 	-= this.offsetScroll.x - this.scrollNode.scrollLeft; 
		position.y 	-= this.offsetScroll.y - this.scrollNode.scrollTop;
		
		position.x  = Math.min( position.x, this.facade.getCanvas().bounds.width());
		position.y  = Math.min( position.y, this.facade.getCanvas().bounds.height());
		
		var offset = {
			x: position.x - this.position.x,
			y: position.y - this.position.y
		};
		
		if(this.aspectRatio) {
			// fixed aspect ratio
			newAspectRatio = (this.bounds.width()+offset.x) / (this.bounds.height()+offset.y);
			if(newAspectRatio>this.aspectRatio) {
				offset.x = this.aspectRatio * (this.bounds.height()+offset.y) - this.bounds.width();
			} else if(newAspectRatio<this.aspectRatio) {
				offset.y = (this.bounds.width()+offset.x) / this.aspectRatio - this.bounds.height();
			}
		}
		
		// respect minimum and maximum sizes of stencil
		if(this.orientation==="northwest") {
			
			if(this.bounds.width()-offset.x > this.maxSize.width) {
				offset.x = -(this.maxSize.width - this.bounds.width());
				if(this.aspectRatio)
					offset.y = this.aspectRatio * offset.x;
			}
			if(this.bounds.width()-offset.x < this.minSize.width) {
				offset.x = -(this.minSize.width - this.bounds.width());
				if(this.aspectRatio)
					offset.y = this.aspectRatio * offset.x;
			}
			if(this.bounds.height()-offset.y > this.maxSize.height) {
				offset.y = -(this.maxSize.height - this.bounds.height());
				if(this.aspectRatio)
					offset.x = offset.y / this.aspectRatio;
			}
			if(this.bounds.height()-offset.y < this.minSize.height) {
				offset.y = -(this.minSize.height - this.bounds.height());
				if(this.aspectRatio)
					offset.x = offset.y / this.aspectRatio;
			}
			
		} else { // defaults to southeast
			if(this.bounds.width()+offset.x > this.maxSize.width) {
				offset.x = this.maxSize.width - this.bounds.width();
				if(this.aspectRatio)
					offset.y = this.aspectRatio * offset.x;
			}
			if(this.bounds.width()+offset.x < this.minSize.width) {
				offset.x = this.minSize.width - this.bounds.width();
				if(this.aspectRatio)
					offset.y = this.aspectRatio * offset.x;
			}
			if(this.bounds.height()+offset.y > this.maxSize.height) {
				offset.y = this.maxSize.height - this.bounds.height();
				if(this.aspectRatio)
					offset.x = offset.y / this.aspectRatio;
			}
			if(this.bounds.height()+offset.y < this.minSize.height) {
				offset.y = this.minSize.height - this.bounds.height();
				if(this.aspectRatio)
					offset.x = offset.y / this.aspectRatio;
			}
		}

		if(this.orientation==="northwest") {
			this.bounds.extend({x:-offset.x, y:-offset.y});
			this.bounds.moveBy(offset);
		} else { // defaults to southeast
			this.bounds.extend(offset);
		}

		this.update();

		this.resizeCallbacks.each((function(value) {
			value(this.bounds);
		}).bind(this));

		Event.stop(event);

	},
	
	registerOnResizeStart: function(callback) {
		if(!this.resizeStartCallbacks.member(callback)) {
			this.resizeStartCallbacks.push(callback);
		}
	},
	
	unregisterOnResizeStart: function(callback) {
		if(this.resizeStartCallbacks.member(callback)) {
			this.resizeStartCallbacks = this.resizeStartCallbacks.without(callback);
		}
	},

	registerOnResizeEnd: function(callback) {
		if(!this.resizeEndCallbacks.member(callback)) {
			this.resizeEndCallbacks.push(callback);
		}
	},
	
	unregisterOnResizeEnd: function(callback) {
		if(this.resizeEndCallbacks.member(callback)) {
			this.resizeEndCallbacks = this.resizeEndCallbacks.without(callback);
		}
	},
		
	registerOnResize: function(callback) {
		if(!this.resizeCallbacks.member(callback)) {
			this.resizeCallbacks.push(callback);
		}
	},

	unregisterOnResize: function(callback) {
		if(this.resizeCallbacks.member(callback)) {
			this.resizeCallbacks = this.resizeCallbacks.without(callback);
		}
	},

	hide: function() {
		this.node.style.display = "none";
	},

	show: function() {
		if(this.bounds)
			this.node.style.display = "";
	},

	setBounds: function(bounds, min, max, aspectRatio) {
		this.bounds = bounds;

		if(!min)
			min = {width: ORYX.CONFIG.MINIMUM_SIZE, height: ORYX.CONFIG.MINIMUM_SIZE};

		if(!max)
			max = {width: ORYX.CONFIG.MAXIMUM_SIZE, height: ORYX.CONFIG.MAXIMUM_SIZE};

		this.minSize = min;
		this.maxSize = max;
		
		this.aspectRatio = aspectRatio;

		this.update();
	},

	update: function() {
		if(!this.bounds) { return; }

		var upL = this.bounds.upperLeft();
		
		if(this.bounds.width() < this.minSize.width)	{ this.bounds.set(upL.x, upL.y, upL.x + this.minSize.width, upL.y + this.bounds.height());};
		if(this.bounds.height() < this.minSize.height)	{ this.bounds.set(upL.x, upL.y, upL.x + this.bounds.width(), upL.y + this.minSize.height);};
		if(this.bounds.width() > this.maxSize.width)	{ this.bounds.set(upL.x, upL.y, upL.x + this.maxSize.width, upL.y + this.bounds.height());};
		if(this.bounds.height() > this.maxSize.height)	{ this.bounds.set(upL.x, upL.y, upL.x + this.bounds.width(), upL.y + this.maxSize.height);};

		var a = this.canvasNode.getScreenCTM();
	    
		upL.x *= a.a;
		upL.y *= a.d;
		
		var additionalIEZoom = 1;
        if (!isNaN(screen.logicalXDPI) && !isNaN(screen.systemXDPI)) {
            var ua = navigator.userAgent;
            if (ua.indexOf('MSIE') >= 0) {
                //IE 10 and below
                var zoom = Math.round((screen.deviceXDPI / screen.logicalXDPI) * 100);
                if (zoom !== 100) {
                    additionalIEZoom = zoom / 100
                }
            }
        }
        
        if (additionalIEZoom === 1) {
             upL.y = upL.y - jQuery("#canvasSection").offset().top + a.f;
             upL.x = upL.x - jQuery("#canvasSection").offset().left + a.e;
        
        } else {
             var canvasOffsetLeft = jQuery("#canvasSection").offset().left;
             var canvasScrollLeft = jQuery("#canvasSection").scrollLeft();
             var canvasScrollTop = jQuery("#canvasSection").scrollTop();
             
             var offset = a.e - (canvasOffsetLeft * additionalIEZoom);
             var additionaloffset = 0;
             if (offset > 10) {
                 additionaloffset = (offset / additionalIEZoom) - offset;
             }
             upL.y = upL.y - (jQuery("#canvasSection").offset().top * additionalIEZoom) + ((canvasScrollTop * additionalIEZoom) - canvasScrollTop) + a.f;
             upL.x = upL.x - (canvasOffsetLeft * additionalIEZoom) + additionaloffset + ((canvasScrollLeft * additionalIEZoom) - canvasScrollLeft) + a.e;
        }
		
		if(this.orientation==="northwest") {
			upL.x -= 13;
			upL.y -= 13;
		} else { // defaults to southeast
			upL.x +=  (a.a * this.bounds.width()) + 3 ;
			upL.y +=  (a.d * this.bounds.height())  + 3;
		}
		
		this.position = upL;

		this.node.style.left = this.position.x + "px";
		this.node.style.top = this.position.y + "px";
	}
});



/**
 * Implements a Command to move shapes
 * 
 */ 
ORYX.Core.Command.Move = ORYX.Core.Command.extend({
	construct: function(moveShapes, offset, parent, selectedShapes, plugin){
		this.moveShapes = moveShapes;
		this.selectedShapes = selectedShapes;
		this.offset 	= offset;
		this.plugin		= plugin;
		// Defines the old/new parents for the particular shape
		this.newParents	= moveShapes.collect(function(t){ return parent || t.parent });
		this.oldParents	= moveShapes.collect(function(shape){ return shape.parent });
		this.dockedNodes= moveShapes.findAll(function(shape){ return shape instanceof ORYX.Core.Node && shape.dockers.length == 1}).collect(function(shape){ return {docker:shape.dockers[0], dockedShape:shape.dockers[0].getDockedShape(), refPoint:shape.dockers[0].referencePoint} });
	},			
	execute: function(){
		this.dockAllShapes()				
		// Moves by the offset
		this.move( this.offset);
		// Addes to the new parents
		this.addShapeToParent( this.newParents ); 
		// Set the selection to the current selection
		this.selectCurrentShapes();
		this.plugin.facade.getCanvas().update();
		this.plugin.facade.updateSelection();
	},
	rollback: function(){
		// Moves by the inverted offset
		var offset = { x:-this.offset.x, y:-this.offset.y };
		this.move( offset );
		// Addes to the old parents
		this.addShapeToParent( this.oldParents ); 
		this.dockAllShapes(true)	
		
		// Set the selection to the current selection
		this.selectCurrentShapes();
		this.plugin.facade.getCanvas().update();
		this.plugin.facade.updateSelection();
		
	},
	move:function(offset, doLayout){
		
		// Move all Shapes by these offset
		for(var i=0; i<this.moveShapes.length ;i++){
			var value = this.moveShapes[i];					
			value.bounds.moveBy(offset);
			
			if (value instanceof ORYX.Core.Node) {
				
				(value.dockers||[]).each(function(d){
					d.bounds.moveBy(offset);
				})
				
				// Update all Dockers of Child shapes
				/*var childShapesNodes = value.getChildShapes(true).findAll(function(shape){ return shape instanceof ORYX.Core.Node });							
				var childDockedShapes = childShapesNodes.collect(function(shape){ return shape.getAllDockedShapes() }).flatten().uniq();							
				var childDockedEdge = childDockedShapes.findAll(function(shape){ return shape instanceof ORYX.Core.Edge });							
				childDockedEdge = childDockedEdge.findAll(function(shape){ return shape.getAllDockedShapes().all(function(dsh){ return childShapesNodes.include(dsh) }) });							
				var childDockedDockers = childDockedEdge.collect(function(shape){ return shape.dockers }).flatten();
				
				for (var j = 0; j < childDockedDockers.length; j++) {
					var docker = childDockedDockers[j];
					if (!docker.getDockedShape() && !this.moveShapes.include(docker)) {
						//docker.bounds.moveBy(offset);
						//docker.update();
					}
				}*/
				
				
				var allEdges = [].concat(value.getIncomingShapes())
					.concat(value.getOutgoingShapes())
					// Remove all edges which are included in the selection from the list
					.findAll(function(r){ return	r instanceof ORYX.Core.Edge && !this.moveShapes.any(function(d){ return d == r || (d instanceof ORYX.Core.Controls.Docker && d.parent == r)}) }.bind(this))
					// Remove all edges which are between the node and a node contained in the selection from the list
					.findAll(function(r){ return 	(r.dockers.first().getDockedShape() == value || !this.moveShapes.include(r.dockers.first().getDockedShape())) &&  
													(r.dockers.last().getDockedShape() == value || !this.moveShapes.include(r.dockers.last().getDockedShape()))}.bind(this))
													
				// Layout all outgoing/incoming edges
				this.plugin.layoutEdges(value, allEdges, offset);
				
				
				var allSameEdges = [].concat(value.getIncomingShapes())
					.concat(value.getOutgoingShapes())
					// Remove all edges which are included in the selection from the list
					.findAll(function(r){ return r instanceof ORYX.Core.Edge && r.dockers.first().isDocked() && r.dockers.last().isDocked() && !this.moveShapes.include(r) && !this.moveShapes.any(function(d){ return d == r || (d instanceof ORYX.Core.Controls.Docker && d.parent == r)}) }.bind(this))
					// Remove all edges which are included in the selection from the list
					.findAll(function(r){ return this.moveShapes.indexOf(r.dockers.first().getDockedShape()) > i ||  this.moveShapes.indexOf(r.dockers.last().getDockedShape()) > i}.bind(this))

				for (var j = 0; j < allSameEdges.length; j++) {
					for (var k = 1; k < allSameEdges[j].dockers.length-1; k++) {
						var docker = allSameEdges[j].dockers[k];
						if (!docker.getDockedShape() && !this.moveShapes.include(docker)) {
							docker.bounds.moveBy(offset);
						}
					}
				}	
				
				/*var i=-1;
				var nodes = value.getChildShapes(true);
				var allEdges = [];
				while(++i<nodes.length){
					var edges = [].concat(nodes[i].getIncomingShapes())
						.concat(nodes[i].getOutgoingShapes())
						// Remove all edges which are included in the selection from the list
						.findAll(function(r){ return r instanceof ORYX.Core.Edge && !allEdges.include(r) && r.dockers.any(function(d){ return !value.bounds.isIncluded(d.bounds.center)})})
					allEdges = allEdges.concat(edges);
					if (edges.length <= 0){ continue }
					//this.plugin.layoutEdges(nodes[i], edges, offset);
				}*/
			}
		}
										
	},
	dockAllShapes: function(shouldDocked){
		// Undock all Nodes
		for (var i = 0; i < this.dockedNodes.length; i++) {
			var docker = this.dockedNodes[i].docker;
			
			docker.setDockedShape( shouldDocked ? this.dockedNodes[i].dockedShape : undefined )
			if (docker.getDockedShape()) {
				docker.setReferencePoint(this.dockedNodes[i].refPoint);
				//docker.update();
			}
		}
	},
	
	addShapeToParent:function( parents ){
		
		// For every Shape, add this and reset the position		
		for(var i=0; i<this.moveShapes.length ;i++){
			var currentShape = this.moveShapes[i];
			if(currentShape instanceof ORYX.Core.Node &&
			   currentShape.parent !== parents[i]) {
				
				// Calc the new position
				var unul = parents[i].absoluteXY();
				var csul = currentShape.absoluteXY();
				var x = csul.x - unul.x;
				var y = csul.y - unul.y;

				// Add the shape to the new contained shape
				parents[i].add(currentShape);
				// Add all attached shapes as well
				currentShape.getOutgoingShapes((function(shape) {
					if(shape instanceof ORYX.Core.Node && !this.moveShapes.member(shape)) {
						parents[i].add(shape);
					}
				}).bind(this));

				// Set the new position
				if(currentShape instanceof ORYX.Core.Node && currentShape.dockers.length == 1){
					var b = currentShape.bounds;
					x += b.width()/2;y += b.height()/2
					currentShape.dockers.first().bounds.centerMoveTo(x, y);
				} else {
					currentShape.bounds.moveTo(x, y);
				}
				
			} 
			
			// Update the shape
			//currentShape.update();
			
		}
	},
	selectCurrentShapes:function(){
		this.plugin.facade.setSelection( this.selectedShapes );
	}
});
/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

if(!ORYX.Plugins)
	ORYX.Plugins = new Object();

ORYX.Plugins.DragDocker = Clazz.extend({

	/**
	 *	Constructor
	 *	@param {Object} Facade: The Facade of the Editor
	 */
	construct: function(facade) {
		this.facade = facade;
		
		// Set the valid and invalid color
		this.VALIDCOLOR 	= ORYX.CONFIG.SELECTION_VALID_COLOR;
		this.INVALIDCOLOR 	= ORYX.CONFIG.SELECTION_INVALID_COLOR;
		
		// Define Variables 
		this.shapeSelection = undefined;
		this.docker 		= undefined;
		this.dockerParent   = undefined;
		this.dockerSource 	= undefined;
		this.dockerTarget 	= undefined;
		this.lastUIObj 		= undefined;
		this.isStartDocker 	= undefined;
		this.isEndDocker 	= undefined;
		this.undockTreshold	= 10;
		this.initialDockerPosition = undefined;
		this.outerDockerNotMoved = undefined;
		this.isValid 		= false;
		
		// For the Drag and Drop
		// Register on MouseDown-Event on a Docker
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_MOUSEDOWN, this.handleMouseDown.bind(this));
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_DOCKERDRAG, this.handleDockerDrag.bind(this));

		
		// Register on over/out to show / hide a docker
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_MOUSEOVER, this.handleMouseOver.bind(this));
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_MOUSEOUT, this.handleMouseOut.bind(this));		
		
		
	},
	
	/**
	 * MouseOut Handler
	 *
	 */
	handleMouseOut: function(event, uiObj) {
		// If there is a Docker, hide this
		if(!this.docker && uiObj instanceof ORYX.Core.Controls.Docker) {
			uiObj.hide();
		} else if(!this.docker && uiObj instanceof ORYX.Core.Edge) {
			uiObj.dockers.each(function(docker){
				docker.hide();
			});
		}
	},

	/**
	 * MouseOver Handler
	 *
	 */
	handleMouseOver: function(event, uiObj) {
		// If there is a Docker, show this		
		if(!this.docker && uiObj instanceof ORYX.Core.Controls.Docker) {
			uiObj.show();
		} else if(!this.docker && uiObj instanceof ORYX.Core.Edge) {
			uiObj.dockers.each(function(docker){
				docker.show();
			});
		}
	},
	/**
	 * DockerDrag Handler
	 * delegates the uiEvent of the drag event to the mouseDown function
	 */
	handleDockerDrag: function(event, uiObj) {
		this.handleMouseDown(event.uiEvent, uiObj);
	},
	
	/**
	 * MouseDown Handler
	 *
	 */	
	handleMouseDown: function(event, uiObj) {
		// If there is a Docker
		if(uiObj instanceof ORYX.Core.Controls.Docker && uiObj.isMovable) {
			
			/* Buffering shape selection and clear selection*/
			this.shapeSelection = this.facade.getSelection();
			this.facade.setSelection();
			
			this.docker = uiObj;
			this.initialDockerPosition = this.docker.bounds.center();
			this.outerDockerNotMoved = false;			
			this.dockerParent = uiObj.parent;
			
			// Define command arguments
			this._commandArg = {docker:uiObj, dockedShape:uiObj.getDockedShape(), refPoint:uiObj.referencePoint || uiObj.bounds.center()};

			// Show the Docker
			this.docker.show();
			
			// If the Dockers Parent is an Edge, 
			//  and the Docker is either the first or last Docker of the Edge
			if(uiObj.parent instanceof ORYX.Core.Edge && 
			   	(uiObj.parent.dockers.first() == uiObj || uiObj.parent.dockers.last() == uiObj)) {
				
				// Get the Edge Source or Target
				if(uiObj.parent.dockers.first() == uiObj && uiObj.parent.dockers.last().getDockedShape()) {
					this.dockerTarget = uiObj.parent.dockers.last().getDockedShape();
				} else if(uiObj.parent.dockers.last() == uiObj && uiObj.parent.dockers.first().getDockedShape()) {
					this.dockerSource = uiObj.parent.dockers.first().getDockedShape();
				}
				
			} else {
				// If there parent is not an Edge, undefined the Source and Target
				this.dockerSource = undefined;
				this.dockerTarget = undefined;				
			}
		
			this.isStartDocker = this.docker.parent.dockers.first() === this.docker;
			this.isEndDocker = this.docker.parent.dockers.last() === this.docker;
					
			// add to canvas while dragging
			this.facade.getCanvas().add(this.docker.parent);
			
			// Hide all Labels from Docker
			this.docker.parent.getLabels().each(function(label) {
				label.hide();
			});
			
			var eventCoordinates = this.facade.eventCoordinates(event);
			var additionalIEZoom = 1;
            if (!isNaN(screen.logicalXDPI) && !isNaN(screen.systemXDPI)) {
                var ua = navigator.userAgent;
                if (ua.indexOf('MSIE') >= 0) {
                    //IE 10 and below
                    var zoom = Math.round((screen.deviceXDPI / screen.logicalXDPI) * 100);
                    if (zoom !== 100) {
                        additionalIEZoom = zoom / 100
                    }
                }
            }
            
            if (additionalIEZoom !== 1) {
                eventCoordinates.x = eventCoordinates.x / additionalIEZoom;
                eventCoordinates.y = eventCoordinates.y / additionalIEZoom;
            }
			
			// Undocked the Docker from current Shape
			if ((!this.isStartDocker && !this.isEndDocker) || !this.docker.isDocked()) {
				
				this.docker.setDockedShape(undefined);
				// Set the Docker to the center of the mouse pointer
				this.docker.bounds.centerMoveTo(eventCoordinates);
				this.dockerParent._update();
			} else {
				this.outerDockerNotMoved = true;
			}
			
			var option = {movedCallback: this.dockerMoved.bind(this), upCallback: this.dockerMovedFinished.bind(this)};
			
			this.startEventPos = eventCoordinates;
			
			// Enable the Docker for Drag'n'Drop, give the mouseMove and mouseUp-Callback with
			ORYX.Core.UIEnableDrag(event, uiObj, option);
		}
	},
	
	/**
	 * Docker MouseMove Handler
	 *
	 */
	dockerMoved: function(event) {
		this.outerDockerNotMoved = false;
		var snapToMagnet = undefined;
		
		if (this.docker.parent) {
			if (this.isStartDocker || this.isEndDocker) {
			
				// Get the EventPosition and all Shapes on these point
				var evPos = this.facade.eventCoordinates(event);
				
				var additionalIEZoom = 1;
	            if (!isNaN(screen.logicalXDPI) && !isNaN(screen.systemXDPI)) {
	                var ua = navigator.userAgent;
	                if (ua.indexOf('MSIE') >= 0) {
	                    //IE 10 and below
	                    var zoom = Math.round((screen.deviceXDPI / screen.logicalXDPI) * 100);
	                    if (zoom !== 100) {
	                        additionalIEZoom = zoom / 100
	                    }
	                }
	            }
	            
	            if (additionalIEZoom !== 1) {
	            	evPos.x = evPos.x / additionalIEZoom;
	            	evPos.y = evPos.y / additionalIEZoom;
	            }
				
				if(this.docker.isDocked()) {
					/* Only consider start/end dockers if they are moved over a treshold */
					var distanceDockerPointer = 
						ORYX.Core.Math.getDistancePointToPoint(evPos, this.initialDockerPosition);
					if(distanceDockerPointer < this.undockTreshold) {
						this.outerDockerNotMoved = true;
						return;
					}
					
					/* Undock the docker */
					this.docker.setDockedShape(undefined);
					// Set the Docker to the center of the mouse pointer
					//this.docker.bounds.centerMoveTo(evPos);
					this.dockerParent._update();
				}
				
				var shapes = this.facade.getCanvas().getAbstractShapesAtPosition(evPos);
				
				// Get the top level Shape on these, but not the same as Dockers parent
				var uiObj = shapes.pop();
				if (this.docker.parent === uiObj) {
					uiObj = shapes.pop();
				}
				
				// If the top level Shape the same as the last Shape, then return
				if (this.lastUIObj == uiObj) {
				//return;
				
				// If the top level uiObj instance of Shape and this isn't the parent of the docker 
				}
				else 
					if (uiObj instanceof ORYX.Core.Shape) {
						
						// Ask by the StencilSet if the source, the edge and the target valid connections.
						if (this.docker.parent instanceof ORYX.Core.Edge) {
							
							var highestParent = this.getHighestParentBeforeCanvas(uiObj);
							/* Ensure that the shape to dock is not a child shape 
							 * of the same edge.
							 */
							if (highestParent instanceof ORYX.Core.Edge && this.docker.parent === highestParent) 
							{
								this.isValid = false;
								this.dockerParent._update();
								return;
							}
							this.isValid = false;
							var curObj = uiObj, orgObj = uiObj;
							while (!this.isValid && curObj && !(curObj instanceof ORYX.Core.Canvas))
							{
								uiObj = curObj;
								this.isValid = this.facade.getRules().canConnect({
											sourceShape: this.dockerSource ? // Is there a docked source 
															this.dockerSource : // than set this
															(this.isStartDocker ? // if not and if the Docker is the start docker
																uiObj : // take the last uiObj
																undefined), // if not set it to undefined;
											edgeShape: this.docker.parent,
											targetShape: this.dockerTarget ? // Is there a docked target 
											this.dockerTarget : // than set this
														(this.isEndDocker ? // if not and if the Docker is not the start docker
															uiObj : // take the last uiObj
															undefined) // if not set it to undefined;
										});
								curObj = curObj.parent;
							}
							
							// Reset uiObj if no 
							// valid parent is found
							if (!this.isValid){
								uiObj = orgObj;
							}

						}
						else {
							this.isValid = this.facade.getRules().canConnect({
								sourceShape: uiObj,
								edgeShape: this.docker.parent,
								targetShape: this.docker.parent
							});
						}
						
						// If there is a lastUIObj, hide the magnets
						if (this.lastUIObj) {
							this.hideMagnets(this.lastUIObj);
						}
						
						// If there is a valid connection, show the magnets
						if (this.isValid) {
							this.showMagnets(uiObj);
						}
						
						// Set the Highlight Rectangle by these value
						this.showHighlight(uiObj, this.isValid ? this.VALIDCOLOR : this.INVALIDCOLOR);
						
						// Buffer the current Shape
						this.lastUIObj = uiObj;
					}
					else {
						// If there is no top level Shape, then hide the highligting of the last Shape
						this.hideHighlight();
						this.lastUIObj ? this.hideMagnets(this.lastUIObj) : null;
						this.lastUIObj = undefined;
						this.isValid = false;
					}
				
				// Snap to the nearest Magnet
				if (this.lastUIObj && this.isValid && !(event.shiftKey || event.ctrlKey)) {
					snapToMagnet = this.lastUIObj.magnets.find(function(magnet){
						return magnet.absoluteBounds().isIncluded(evPos);
					});
					
					if (snapToMagnet) {
						this.docker.bounds.centerMoveTo(snapToMagnet.absoluteCenterXY());
					//this.docker.update()
					}
				}
			}
		}
		// Snap to on the nearest Docker of the same parent
		if(!(event.shiftKey || event.ctrlKey) && !snapToMagnet) {
			var minOffset = ORYX.CONFIG.DOCKER_SNAP_OFFSET;
			var nearestX = minOffset + 1;
			var nearestY = minOffset + 1;
			
			var dockerCenter = this.docker.bounds.center();
			
			if (this.docker.parent) {
				
				this.docker.parent.dockers.each((function(docker){
					if (this.docker == docker) {
						return
					};
					
					var center = docker.referencePoint ? docker.getAbsoluteReferencePoint() : docker.bounds.center();
					
					nearestX = Math.abs(nearestX) > Math.abs(center.x - dockerCenter.x) ? center.x - dockerCenter.x : nearestX;
					nearestY = Math.abs(nearestY) > Math.abs(center.y - dockerCenter.y) ? center.y - dockerCenter.y : nearestY;
					
					
				}).bind(this));
				
				if (Math.abs(nearestX) < minOffset || Math.abs(nearestY) < minOffset) {
					nearestX = Math.abs(nearestX) < minOffset ? nearestX : 0;
					nearestY = Math.abs(nearestY) < minOffset ? nearestY : 0;
					
					this.docker.bounds.centerMoveTo(dockerCenter.x + nearestX, dockerCenter.y + nearestY);
					//this.docker.update()
				} else {
					
					
					
					var previous = this.docker.parent.dockers[Math.max(this.docker.parent.dockers.indexOf(this.docker)-1, 0)];
					var next = this.docker.parent.dockers[Math.min(this.docker.parent.dockers.indexOf(this.docker)+1, this.docker.parent.dockers.length-1)];
					
					if (previous && next && previous !== this.docker && next !== this.docker){
						var cp = previous.bounds.center();
						var cn = next.bounds.center();
						var cd = this.docker.bounds.center();
						
						// Checks if the point is on the line between previous and next
						if (ORYX.Core.Math.isPointInLine(cd.x, cd.y, cp.x, cp.y, cn.x, cn.y, 10)) {
							// Get the rise
							var raise = (Number(cn.y)-Number(cp.y))/(Number(cn.x)-Number(cp.x));
							// Calculate the intersection point
							var intersecX = ((cp.y-(cp.x*raise))-(cd.y-(cd.x*(-Math.pow(raise,-1)))))/((-Math.pow(raise,-1))-raise);
							var intersecY = (cp.y-(cp.x*raise))+(raise*intersecX);
							
							if(isNaN(intersecX) || isNaN(intersecY)) {return;}
							
							this.docker.bounds.centerMoveTo(intersecX, intersecY);
						}
					}
					
				}
			}
		}
		//this.facade.getCanvas().update();
		this.dockerParent._update();
	},

	/**
	 * Docker MouseUp Handler
	 *
	 */
	dockerMovedFinished: function(event) {
		
		/* Reset to buffered shape selection */
		this.facade.setSelection(this.shapeSelection);
		
		// Hide the border
		this.hideHighlight();
		
		// Show all Labels from Docker
		this.dockerParent.getLabels().each(function(label){
			label.show();
			//label.update();
		});
	
		// If there is a last top level Shape
		if(this.lastUIObj && (this.isStartDocker || this.isEndDocker)){				
			// If there is a valid connection, the set as a docked Shape to them
			if(this.isValid) {
				
				this.docker.setDockedShape(this.lastUIObj);	
				
				this.facade.raiseEvent({
					type 	:ORYX.CONFIG.EVENT_DRAGDOCKER_DOCKED, 
					docker	: this.docker,
					parent	: this.docker.parent,
					target	: this.lastUIObj
				});
			}
			
			this.hideMagnets(this.lastUIObj);
		}
		
		// Hide the Docker
		this.docker.hide();
		
		if(this.outerDockerNotMoved) {
			// Get the EventPosition and all Shapes on these point
			var evPos = this.facade.eventCoordinates(event);
			var shapes = this.facade.getCanvas().getAbstractShapesAtPosition(evPos);
			
			/* Remove edges from selection */
			var shapeWithoutEdges = shapes.findAll(function(node) {
				return node instanceof ORYX.Core.Node;
			});
			shapes = shapeWithoutEdges.length ? shapeWithoutEdges : shapes;
			this.facade.setSelection(shapes);
		} else {
			//Command-Pattern for dragging one docker
			var dragDockerCommand = ORYX.Core.Command.extend({
				construct: function(docker, newPos, oldPos, newDockedShape, oldDockedShape, facade){
					this.docker 		= docker;
					this.index			= docker.parent.dockers.indexOf(docker);
					this.newPosition	= newPos;
					this.newDockedShape = newDockedShape;
					this.oldPosition	= oldPos;
					this.oldDockedShape	= oldDockedShape;
					this.facade			= facade;
					this.index			= docker.parent.dockers.indexOf(docker);
					this.shape			= docker.parent;
					
				},			
				execute: function(){
					if (!this.docker.parent){
						this.docker = this.shape.dockers[this.index];
					}
					this.dock( this.newDockedShape, this.newPosition );
					this.removedDockers = this.shape.removeUnusedDockers();
					this.facade.updateSelection();
				},
				rollback: function(){
					this.dock( this.oldDockedShape, this.oldPosition );
					(this.removedDockers||$H({})).each(function(d){
						this.shape.add(d.value, Number(d.key));
						this.shape._update(true);
					}.bind(this));
					this.facade.updateSelection();
				},
				dock:function( toDockShape, pos ){			
					// Set the Docker to the new Shape
					this.docker.setDockedShape( undefined );
					if( toDockShape ){			
						this.docker.setDockedShape( toDockShape );	
						this.docker.setReferencePoint( pos );
						//this.docker.update();	
						//this.docker.parent._update();				
					} else {
						this.docker.bounds.centerMoveTo( pos );
					}
	
					this.facade.getCanvas().update();
				}
			});
			
			
			if (this.docker.parent){
				// Instanziate the dockCommand
				var command = new dragDockerCommand(this.docker, this.docker.getDockedShape() ? this.docker.referencePoint : this.docker.bounds.center(), this._commandArg.refPoint, this.docker.getDockedShape(), this._commandArg.dockedShape, this.facade);
				this.facade.executeCommands( [command] );	
			}
		}
	
		// Update all Shapes
		//this.facade.updateSelection();
			
		// Undefined all variables
		this.docker 		= undefined;
		this.dockerParent   = undefined;
		this.dockerSource 	= undefined;
		this.dockerTarget 	= undefined;	
		this.lastUIObj 		= undefined;		
	},
	
	/**
	 * Hide the highlighting
	 */
	hideHighlight: function() {
		this.facade.raiseEvent({type:ORYX.CONFIG.EVENT_HIGHLIGHT_HIDE, highlightId:'validDockedShape'});
	},

	/**
	 * Show the highlighting
	 *
	 */
	showHighlight: function(uiObj, color) {
		
		this.facade.raiseEvent({
										type:		ORYX.CONFIG.EVENT_HIGHLIGHT_SHOW, 
										highlightId:'validDockedShape',
										elements:	[uiObj],
										color:		color
									});
	},
	
	showMagnets: function(uiObj){
		uiObj.magnets.each(function(magnet) {
			magnet.show();
		});
	},
	
	hideMagnets: function(uiObj){
		uiObj.magnets.each(function(magnet) {
			magnet.hide();
		});
	},
	
	getHighestParentBeforeCanvas: function(shape) {
		if(!(shape instanceof ORYX.Core.Shape)) {return undefined;}
		
		var parent = shape.parent;
		while(parent && !(parent.parent instanceof ORYX.Core.Canvas)) {
			parent = parent.parent;
		}	
		
		return parent;		
	}	

});

/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

if(!ORYX.Plugins)
	ORYX.Plugins = new Object();

ORYX.Plugins.AddDocker = Clazz.extend({

	/**
	 *	Constructor
	 *	@param {Object} Facade: The Facade of the Editor
	 */
	construct: function(facade) {
		this.facade = facade;
		this.enableAdd = false;
		this.enableRemove = false;
		
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_MOUSEDOWN, this.handleMouseDown.bind(this));
	},
	
	setEnableAdd: function(enable){
		this.enableAdd = enable;
		
		if(this.enableAdd) {
    		jQuery("#add-bendpoint-button").addClass('pressed');
    	} else {
    		jQuery("#add-bendpoint-button").removeClass('pressed');
    		jQuery("#add-bendpoint-button").blur();
    	}
	},
	setEnableRemove: function(enable){
		this.enableRemove = enable;
		
		if(this.enableRemove) {
    		jQuery("#remove-bendpoint-button").addClass('pressed');
    	} else {
    		jQuery("#remove-bendpoint-button").removeClass('pressed');
    		jQuery("#remove-bendpoint-button").blur();
    	}
	},
	
    enabledAdd: function(enable){
        return this.enableAdd;
    },
    enabledRemove: function(){
        return this.enableRemove;
    },
	
	/**
	 * MouseDown Handler
	 *
	 */	
	handleMouseDown: function(event, uiObj) {
		if (this.enabledAdd() && uiObj instanceof ORYX.Core.Edge) {
            this.newDockerCommand({
                edge: uiObj,
                position: this.facade.eventCoordinates(event)
            });
            this.setEnableAdd(false);
            
		} else if (this.enabledRemove() &&
				   uiObj instanceof ORYX.Core.Controls.Docker &&
				   uiObj.parent instanceof ORYX.Core.Edge) {
            this.newDockerCommand({
                edge: uiObj.parent,
                docker: uiObj
            });
            this.setEnableRemove(false);
        }
		document.body.style.cursor = 'default';
	},
    
    // Options: edge (required), position (required if add), docker (required if delete)
    newDockerCommand: function(options){
        if(!options.edge)
            return;

        var commandClass = ORYX.Core.Command.extend({
            construct: function(addEnabled, deleteEnabled, edge, docker, pos, facade){
                this.addEnabled = addEnabled;
                this.deleteEnabled = deleteEnabled;
                this.edge = edge;
                this.docker = docker;
                this.pos = pos;
                this.facade = facade;
            },
            execute: function(){
                if (this.addEnabled) {
					if (!this.docker){
                    	this.docker = this.edge.addDocker(this.pos);
						this.index = this.edge.dockers.indexOf(this.docker);
					} else {
                    	this.edge.add(this.docker, this.index);
					}
                }
                else if (this.deleteEnabled) {
					this.index = this.edge.dockers.indexOf(this.docker);
                    this.pos = this.docker.bounds.center();
                    this.edge.removeDocker(this.docker);
                }
                this.edge.getLabels().invoke("show");
                this.facade.getCanvas().update();
                this.facade.updateSelection();
            },
            rollback: function(){
                if (this.addEnabled) {
                    if (this.docker instanceof ORYX.Core.Controls.Docker) {
                        this.edge.removeDocker(this.docker);
                    }
                }
                else if (this.deleteEnabled) {
                    this.edge.add(this.docker, this.index);
                }
                this.edge.getLabels().invoke("show");
                this.facade.getCanvas().update();
                this.facade.updateSelection();
            }
        })
        
        var command = new commandClass(this.enabledAdd(), this.enabledRemove(), options.edge, options.docker, options.position, this.facade);
        
        this.facade.executeCommands([command]);
    }
});

/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

if(!ORYX.Plugins)
	ORYX.Plugins = new Object();

 ORYX.Plugins.SelectionFrame = Clazz.extend({

	construct: function(facade) {
		this.facade = facade;

		// Register on MouseEvents
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_MOUSEDOWN, this.handleMouseDown.bind(this));
		document.documentElement.addEventListener(ORYX.CONFIG.EVENT_MOUSEUP, this.handleMouseUp.bind(this), true);

		// Some initiale variables
		this.position 		= {x:0, y:0};
		this.size 			= {width:0, height:0};
		this.offsetPosition = {x: 0, y: 0};

		// (Un)Register Mouse-Move Event
		this.moveCallback 	= undefined;
		this.offsetScroll	= {x:0,y:0};
		// HTML-Node of Selection-Frame
		this.node = ORYX.Editor.graft("http://www.w3.org/1999/xhtml", $('canvasSection'),
			['div', {'class':'Oryx_SelectionFrame'}]);

		this.hide();
	},

	handleMouseDown: function(event, uiObj) {
		// If there is the Canvas
		if( uiObj instanceof ORYX.Core.Canvas ) {
			// Calculate the Offset
			var scrollNode = uiObj.rootNode.parentNode.parentNode;
						
			var a = this.facade.getCanvas().node.getScreenCTM();
			this.offsetPosition = {
				x: a.e,
				y: a.f
			};

			// Set the new Position
			this.setPos({
			    x: Event.pointerX(event) - jQuery("#canvasSection").offset().left, 
				y: Event.pointerY(event) - jQuery("#canvasSection").offset().top + 5
			});
			
			// Reset the size
			this.resize({width:0, height:0});
			this.moveCallback = this.handleMouseMove.bind(this);
		
			// Register Mouse-Move Event
			document.documentElement.addEventListener(ORYX.CONFIG.EVENT_MOUSEMOVE, this.moveCallback, false);

			this.offsetScroll		= {x:scrollNode.scrollLeft,y:scrollNode.scrollTop};
			
			// Show the Frame
			this.show();
		}

		Event.stop(event);
	},

	handleMouseUp: function(event) {
		// If there was an MouseMoving
		if(this.moveCallback) {
			// Hide the Frame
			this.hide();

			// Unregister Mouse-Move
			document.documentElement.removeEventListener(ORYX.CONFIG.EVENT_MOUSEMOVE, this.moveCallback, false);			
		
			this.moveCallback = undefined;

			var corrSVG = this.facade.getCanvas().node.getScreenCTM();

			// Calculate the positions of the Frame
			var a = {
				x: this.size.width > 0 ? this.position.x : this.position.x + this.size.width,
				y: this.size.height > 0 ? this.position.y : this.position.y + this.size.height
			};

			var b = {
				x: a.x + Math.abs(this.size.width),
				y: a.y + Math.abs(this.size.height)
			};
			
			var additionalIEZoom = 1;
            if (!isNaN(screen.logicalXDPI) && !isNaN(screen.systemXDPI)) {
                var ua = navigator.userAgent;
                if (ua.indexOf('MSIE') >= 0) {
                    //IE 10 and below
                    var zoom = Math.round((screen.deviceXDPI / screen.logicalXDPI) * 100);
                    if (zoom !== 100) {
                        additionalIEZoom = zoom / 100
                    }
                }
            }
            
            if (additionalIEZoom === 1) {
                a.x = a.x - (corrSVG.e - jQuery("#canvasSection").offset().left);
                a.y = a.y - (corrSVG.f - jQuery("#canvasSection").offset().top);
                b.x = b.x - (corrSVG.e - jQuery("#canvasSection").offset().left);
                b.y = b.y - (corrSVG.f - jQuery("#canvasSection").offset().top);
            
            } else {
                 var canvasOffsetLeft = jQuery("#canvasSection").offset().left;
                 var canvasScrollLeft = jQuery("#canvasSection").scrollLeft();
                 var canvasScrollTop = jQuery("#canvasSection").scrollTop();
                 
                 var offset = a.e - (canvasOffsetLeft * additionalIEZoom);
                 var additionaloffset = 0;
                 if (offset > 10) {
                     additionaloffset = (offset / additionalIEZoom) - offset;
                 }
                 
                 a.x = a.x - (corrSVG.e - (canvasOffsetLeft * additionalIEZoom) + additionaloffset + ((canvasScrollLeft * additionalIEZoom) - canvasScrollLeft));
                 a.y = a.y - (corrSVG.f - (jQuery("#canvasSection").offset().top * additionalIEZoom) + ((canvasScrollTop * additionalIEZoom) - canvasScrollTop));
                 b.x = b.x - (corrSVG.e - (canvasOffsetLeft * additionalIEZoom) + additionaloffset + ((canvasScrollLeft * additionalIEZoom) - canvasScrollLeft));
                 b.y = b.y - (corrSVG.f - (jQuery("#canvasSection").offset().top * additionalIEZoom) + ((canvasScrollTop * additionalIEZoom) - canvasScrollTop));
            }
			
			

			// Fit to SVG-Coordinates
			a.x /= corrSVG.a; a.y /= corrSVG.d;
			b.x /= corrSVG.a; b.y /= corrSVG.d;
			
			// Calculate the elements from the childs of the canvas
			var elements = this.facade.getCanvas().getChildShapes(true).findAll(function(value) {
				var absBounds = value.absoluteBounds();
				
				var bA = absBounds.upperLeft();
				var bB = absBounds.lowerRight();
				
				if(bA.x > a.x && bA.y > a.y && bB.x < b.x && bB.y < b.y)
					return true;
				return false;
			});

			// Set the selection
			this.facade.setSelection(elements);
		}
	},

	handleMouseMove: function(event) {
		// Calculate the size
		var size = {
			width	: Event.pointerX(event) - this.position.x - jQuery("#canvasSection").offset().left,
			height	: Event.pointerY(event) - this.position.y - jQuery("#canvasSection").offset().top + 5
		};

		var scrollNode 	= this.facade.getCanvas().rootNode.parentNode.parentNode;
		size.width 		-= this.offsetScroll.x - scrollNode.scrollLeft; 
		size.height 	-= this.offsetScroll.y - scrollNode.scrollTop;
						
		// Set the size
		this.resize(size);

		Event.stop(event);
	},

	hide: function() {
		this.node.style.display = "none";
	},

	show: function() {
		this.node.style.display = "";
	},

	setPos: function(pos) {
		// Set the Position
		this.node.style.top = pos.y + "px";
		this.node.style.left = pos.x + "px";
		this.position = pos;
	},

	resize: function(size) {

		// Calculate the negative offset
		this.setPos(this.position);
		this.size = Object.clone(size);
		
		if(size.width < 0) {
			this.node.style.left = (this.position.x + size.width) + "px";
			size.width = - size.width;
		}
		if(size.height < 0) {
			this.node.style.top = (this.position.y + size.height) + "px";
			size.height = - size.height;
		}

		// Set the size
		this.node.style.width = size.width + "px";
		this.node.style.height = size.height + "px";
	}

});


/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

if(!ORYX.Plugins)
	ORYX.Plugins = new Object(); 

ORYX.Plugins.ShapeHighlighting = Clazz.extend({

	construct: function(facade) {
		
		this.parentNode = facade.getCanvas().getSvgContainer();
		
		// The parent Node
		this.node = ORYX.Editor.graft("http://www.w3.org/2000/svg", this.parentNode,
					['g']);

		this.highlightNodes = {};
		
		facade.registerOnEvent(ORYX.CONFIG.EVENT_HIGHLIGHT_SHOW, this.setHighlight.bind(this));
		facade.registerOnEvent(ORYX.CONFIG.EVENT_HIGHLIGHT_HIDE, this.hideHighlight.bind(this));		

	},

	setHighlight: function(options) {
		if(options && options.highlightId){
			var node = this.highlightNodes[options.highlightId];
			
			if(!node){
				node= ORYX.Editor.graft("http://www.w3.org/2000/svg", this.node,
					['path', {
						"stroke-width": 2.0, "fill":"none"
						}]);	
			
				this.highlightNodes[options.highlightId] = node;
			}

			if(options.elements && options.elements.length > 0) {
				
				this.setAttributesByStyle( node, options );
				this.show(node);
			
			} else {
			
				this.hide(node);			
			
			}
			
		}
	},
	
	hideHighlight: function(options) {
		if(options && options.highlightId && this.highlightNodes[options.highlightId]){
			this.hide(this.highlightNodes[options.highlightId]);
		}		
	},
	
	hide: function(node) {
		node.setAttributeNS(null, 'display', 'none');
	},

	show: function(node) {
		node.setAttributeNS(null, 'display', '');
	},
	
	setAttributesByStyle: function( node, options ){
		
		// If the style say, that it should look like a rectangle
		if( options.style && options.style == ORYX.CONFIG.SELECTION_HIGHLIGHT_STYLE_RECTANGLE ){
			
			// Set like this
			var bo = options.elements[0].absoluteBounds();
			
			var strWidth = options.strokewidth ? options.strokewidth 	: ORYX.CONFIG.BORDER_OFFSET
			
			node.setAttributeNS(null, "d", this.getPathRectangle( bo.a, bo.b , strWidth ) );
			node.setAttributeNS(null, "stroke", 		options.color 		? options.color 		: ORYX.CONFIG.SELECTION_HIGHLIGHT_COLOR);
			node.setAttributeNS(null, "stroke-opacity", options.opacity 	? options.opacity 		: 0.2);
			node.setAttributeNS(null, "stroke-width", 	strWidth);
						
		} else if(options.elements.length == 1 
					&& options.elements[0] instanceof ORYX.Core.Edge &&
					options.highlightId != "selection") {
			
			/* Highlight containment of edge's childs */
			var path = this.getPathEdge(options.elements[0].dockers);
			if (path && path.length > 0)
			{
				node.setAttributeNS(null, "d", path);
			}
			node.setAttributeNS(null, "stroke", options.color ? options.color : ORYX.CONFIG.SELECTION_HIGHLIGHT_COLOR);
			node.setAttributeNS(null, "stroke-opacity", options.opacity ? options.opacity : 0.2);
			node.setAttributeNS(null, "stroke-width", 	ORYX.CONFIG.OFFSET_EDGE_BOUNDS);
			
		}else {
			// If not, set just the corners
			var path = this.getPathByElements(options.elements);
			if (path && path.length > 0)
			{
				node.setAttributeNS(null, "d", path);
			}
			node.setAttributeNS(null, "stroke", options.color ? options.color : ORYX.CONFIG.SELECTION_HIGHLIGHT_COLOR);
			node.setAttributeNS(null, "stroke-opacity", options.opacity ? options.opacity : 1.0);
			node.setAttributeNS(null, "stroke-width", 	options.strokewidth ? options.strokewidth 	: 2.0);
						
		}
	},
	
	getPathByElements: function(elements){
		if(!elements || elements.length <= 0) {return undefined}
		
		// Get the padding and the size
		var padding = ORYX.CONFIG.SELECTED_AREA_PADDING;
		
		var path = ""
		
		// Get thru all Elements
		elements.each((function(element) {
			if(!element) {return}
			// Get the absolute Bounds and the two Points
			var bounds = element.absoluteBounds();
			bounds.widen(padding)
			var a = bounds.upperLeft();
			var b = bounds.lowerRight();
			
			path = path + this.getPath(a ,b);
												
		}).bind(this));

		return path;
		
	},

	getPath: function(a, b){
				
		return this.getPathCorners(a, b);
	
	},
			
	getPathCorners: function(a, b){

		var size = ORYX.CONFIG.SELECTION_HIGHLIGHT_SIZE;
				
		var path = ""

		// Set: Upper left 
		path = path + "M" + a.x + " " + (a.y + size) + " l0 -" + size + " l" + size + " 0 ";
		// Set: Lower left
		path = path + "M" + a.x + " " + (b.y - size) + " l0 " + size + " l" + size + " 0 ";
		// Set: Lower right
		path = path + "M" + b.x + " " + (b.y - size) + " l0 " + size + " l-" + size + " 0 ";
		// Set: Upper right
		path = path + "M" + b.x + " " + (a.y + size) + " l0 -" + size + " l-" + size + " 0 ";
		
		return path;
	},
	
	getPathRectangle: function(a, b, strokeWidth){

		var size = ORYX.CONFIG.SELECTION_HIGHLIGHT_SIZE;

		var path 	= ""
		var offset 	= strokeWidth / 2.0;
		 
		// Set: Upper left 
		path = path + "M" + (a.x + offset) + " " + (a.y);
		path = path + " L" + (a.x + offset) + " " + (b.y - offset);
		path = path + " L" + (b.x - offset) + " " + (b.y - offset);
		path = path + " L" + (b.x - offset) + " " + (a.y + offset);
		path = path + " L" + (a.x + offset) + " " + (a.y + offset);

		return path;
	},
	
	getPathEdge: function(edgeDockers) {
		var length = edgeDockers.length;
		var path = "M" + edgeDockers[0].bounds.center().x + " " 
					+  edgeDockers[0].bounds.center().y;
		
		for(i=1; i<length; i++) {
			var dockerPoint = edgeDockers[i].bounds.center();
			path = path + " L" + dockerPoint.x + " " +  dockerPoint.y;
		}
		
		return path;
	}
	
});

 
ORYX.Plugins.HighlightingSelectedShapes = Clazz.extend({

	construct: function(facade) {
		this.facade = facade;
		this.opacityFull = 0.9;
		this.opacityLow = 0.4;

		// Register on Dragging-Events for show/hide of ShapeMenu
		//this.facade.registerOnEvent(ORYX.CONFIG.EVENT_DRAGDROP_START, this.hide.bind(this));
		//this.facade.registerOnEvent(ORYX.CONFIG.EVENT_DRAGDROP_END,  this.show.bind(this));		
	},

	/**
	 * On the Selection-Changed
	 *
	 */
	onSelectionChanged: function(event) {
		if(event.elements && event.elements.length > 1) {
			this.facade.raiseEvent({
										type:		ORYX.CONFIG.EVENT_HIGHLIGHT_SHOW, 
										highlightId:'selection',
										elements:	event.elements.without(event.subSelection),
										color:		ORYX.CONFIG.SELECTION_HIGHLIGHT_COLOR,
										opacity: 	!event.subSelection ? this.opacityFull : this.opacityLow
									});

			if(event.subSelection){
				this.facade.raiseEvent({
											type:		ORYX.CONFIG.EVENT_HIGHLIGHT_SHOW, 
											highlightId:'subselection',
											elements:	[event.subSelection],
											color:		ORYX.CONFIG.SELECTION_HIGHLIGHT_COLOR,
											opacity: 	this.opacityFull
										});	
			} else {
				this.facade.raiseEvent({type:ORYX.CONFIG.EVENT_HIGHLIGHT_HIDE, highlightId:'subselection'});				
			}						
			
		} else {
			this.facade.raiseEvent({type:ORYX.CONFIG.EVENT_HIGHLIGHT_HIDE, highlightId:'selection'});
			this.facade.raiseEvent({type:ORYX.CONFIG.EVENT_HIGHLIGHT_HIDE, highlightId:'subselection'});
		}		
	}
});/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

if (!ORYX.Plugins) 
    ORYX.Plugins = new Object();

ORYX.Plugins.Overlay = Clazz.extend({

    facade: undefined,
	
	styleNode: undefined,
    
    construct: function(facade){
		
        this.facade = facade;

		this.changes = [];

		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_OVERLAY_SHOW, this.show.bind(this));
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_OVERLAY_HIDE, this.hide.bind(this));	

		this.styleNode = document.createElement('style')
		this.styleNode.setAttributeNS(null, 'type', 'text/css')
		
		document.getElementsByTagName('head')[0].appendChild( this.styleNode )

    },
	
	/**
	 * Show the overlay for specific nodes
	 * @param {Object} options
	 * 
	 * 	String				options.id		- MUST - Define the id of the overlay (is needed for the hiding of this overlay)		
	 *	ORYX.Core.Shape[] 	options.shapes 	- MUST - Define the Shapes for the changes
	 * 	attr-name:value		options.changes	- Defines all the changes which should be shown
	 * 
	 * 
	 */
	show: function( options ){
		
		// Checks if all arguments are available
		if( 	!options || 
				!options.shapes || !options.shapes instanceof Array ||
				!options.id	|| !options.id instanceof String || options.id.length == 0) { 
				
					return
					
		}
		
		//if( this.changes[options.id]){
		//	this.hide( options )
		//}
			

		// Checked if attributes are setted
		if( options.attributes ){
			
			// FOR EACH - Shape
			options.shapes.each(function(el){
				
				// Checks if the node is a Shape
				if( !el instanceof ORYX.Core.Shape){ return }
				
				this.setAttributes( el.node , options.attributes )
				
			}.bind(this))

		}	
		
		var isSVG = true
		try {
			isSVG = options.node && options.node instanceof SVGElement;
		} catch(e){}
		
		// Checks if node is setted and if this is an SVGElement		
		if ( options.node && isSVG) {
			
			options["_temps"] = []
						
			// FOR EACH - Node
			options.shapes.each(function(el, index){
				
				// Checks if the node is a Shape
				if( !el instanceof ORYX.Core.Shape){ return }
				
				var _temp = {}
				_temp.svg = options.dontCloneNode ? options.node : options.node.cloneNode( true );
				
				// Add the svg node to the ORYX-Shape
				el.node.firstChild.appendChild( _temp.svg )		
				
				// If
				if (el instanceof ORYX.Core.Edge && !options.nodePosition) {
					options['nodePosition'] = "START"
				}
						
				// If the node position is setted, it has to be transformed
				if( options.nodePosition ){
					
					var b = el.bounds;
					var p = options.nodePosition.toUpperCase();
					
					// Check the values of START and END
					if( el instanceof ORYX.Core.Node && p == "START"){
						p = "NW";
					} else if(el instanceof ORYX.Core.Node && p == "END"){
						p = "SE";
					} else if(el instanceof ORYX.Core.Edge && p == "START"){
						b = el.getDockers().first().bounds
					} else if(el instanceof ORYX.Core.Edge && p == "END"){
						b = el.getDockers().last().bounds
					}

					// Create a callback for the changing the position 
					// depending on the position string
					_temp.callback = function(){
						
						var x = 0; var y = 0;
						
						if( p == "NW" ){
							// Do Nothing
						} else if( p == "N" ) {
							x = b.width() / 2;
						} else if( p == "NE" ) {
							x = b.width();
						} else if( p == "E" ) {
							x = b.width(); y = b.height() / 2;
						} else if( p == "SE" ) {
							x = b.width(); y = b.height();
						} else if( p == "S" ) {
							x = b.width() / 2; y = b.height();
						} else if( p == "SW" ) {
							y = b.height();
						} else if( p == "W" ) {
							y = b.height() / 2;
						} else if( p == "START" || p == "END") {
							x = b.width() / 2; y = b.height() / 2;
						} else {
							return
						}
						
						if( el instanceof ORYX.Core.Edge){
							x  += b.upperLeft().x ; y  += b.upperLeft().y ;
						}
						
						_temp.svg.setAttributeNS(null, "transform", "translate(" + x + ", " + y + ")")
					
					}.bind(this)
					
					_temp.element = el;
					_temp.callback();
					
					b.registerCallback( _temp.callback );
					
				}
				
				
				options._temps.push( _temp )	
				
			}.bind(this))
			
			
			
		}		
	

		// Store the changes
		if( !this.changes[options.id] ){
			this.changes[options.id] = [];
		}
		
		this.changes[options.id].push( options );
				
	},
	
	/**
	 * Hide the overlay with the spefic id
	 * @param {Object} options
	 */
	hide: function( options ){
		
		// Checks if all arguments are available
		if( 	!options || 
				!options.id	|| !options.id instanceof String || options.id.length == 0 ||
				!this.changes[options.id]) { 
				
					return
					
		}		
		
		
		// Delete all added attributes
		// FOR EACH - Shape
		this.changes[options.id].each(function(option){
			
			option.shapes.each(function(el, index){
				
				// Checks if the node is a Shape
				if( !el instanceof ORYX.Core.Shape){ return }
				
				this.deleteAttributes( el.node )
							
			}.bind(this));

	
			if( option._temps ){
				
				option._temps.each(function(tmp){
					// Delete the added Node, if there is one
					if( tmp.svg && tmp.svg.parentNode ){
						tmp.svg.parentNode.removeChild( tmp.svg )
					}
		
					// If 
					if( tmp.callback && tmp.element){
						// It has to be unregistered from the edge
						tmp.element.bounds.unregisterCallback( tmp.callback )
					}
							
				}.bind(this))
				
			}
		
			
		}.bind(this));

		
		this.changes[options.id] = null;
		
		
	},
	
	
	/**
	 * Set the given css attributes to that node
	 * @param {HTMLElement} node
	 * @param {Object} attributes
	 */
	setAttributes: function( node, attributes ) {
		
		
		// Get all the childs from ME
		var childs = this.getAllChilds( node.firstChild.firstChild )
		
		var ids = []
		
		// Add all Attributes which have relation to another node in this document and concate the pure id out of it
		// This is for example important for the markers of a edge
		childs.each(function(e){ ids.push( $A(e.attributes).findAll(function(attr){ return attr.nodeValue.startsWith('url(#')}) )})
		ids = ids.flatten().compact();
		ids = ids.collect(function(s){return s.nodeValue}).uniq();
		ids = ids.collect(function(s){return s.slice(5, s.length-1)})
		
		// Add the node ID to the id
		ids.unshift( node.id + ' .me')
		
		var attr				= $H(attributes);
        var attrValue			= attr.toJSON().gsub(',', ';').gsub('"', '');
        var attrMarkerValue		= attributes.stroke ? attrValue.slice(0, attrValue.length-1) + "; fill:" + attributes.stroke + ";}" : attrValue;
        var attrTextValue;
        if( attributes.fill ){
            var copyAttr        = Object.clone(attributes);
        	copyAttr.fill		= "black";
        	attrTextValue		= $H(copyAttr).toJSON().gsub(',', ';').gsub('"', '');
        }
                	
        // Create the CSS-Tags Style out of the ids and the attributes
        csstags = ids.collect(function(s, i){return "#" + s + " * " + (!i? attrValue : attrMarkerValue) + "" + (attrTextValue ? " #" + s + " text * " + attrTextValue : "") })
		
		// Join all the tags
		var s = csstags.join(" ") + "\n" 
		
		// And add to the end of the style tag
		this.styleNode.appendChild(document.createTextNode(s));
		
		
	},
	
	/**
	 * Deletes all attributes which are
	 * added in a special style sheet for that node
	 * @param {HTMLElement} node 
	 */
	deleteAttributes: function( node ) {
				
		// Get all children which contains the node id		
		var delEl = $A(this.styleNode.childNodes)
					 .findAll(function(e){ return e.textContent.include( '#' + node.id ) });
		
		// Remove all of them
		delEl.each(function(el){
			el.parentNode.removeChild(el);
		});		
	},
	
	getAllChilds: function( node ){
		
		var childs = $A(node.childNodes)
		
		$A(node.childNodes).each(function( e ){ 
		        childs.push( this.getAllChilds( e ) )
		}.bind(this))

    	return childs.flatten();
	}

    
});
/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

if (!ORYX.Plugins) 
    ORYX.Plugins = new Object();

ORYX.Plugins.KeysMove = ORYX.Plugins.AbstractPlugin.extend({

    facade: undefined,
    
    construct: function(facade){
    
        this.facade = facade;
        this.copyElements = [];
        
        //this.facade.registerOnEvent(ORYX.CONFIG.EVENT_KEYDOWN, this.keyHandler.bind(this));

		// SELECT ALL
		this.facade.offer({
		keyCodes: [{
		 		metaKeys: [ORYX.CONFIG.META_KEY_META_CTRL],
				keyCode: 65,
				keyAction: ORYX.CONFIG.KEY_ACTION_DOWN 
			}
		 ],
         functionality: this.selectAll.bind(this)
         });
		 
		// MOVE LEFT SMALL		
		this.facade.offer({
		keyCodes: [{
		 		metaKeys: [ORYX.CONFIG.META_KEY_META_CTRL],
				keyCode: ORYX.CONFIG.KEY_CODE_LEFT,
				keyAction: ORYX.CONFIG.KEY_ACTION_DOWN 
			}
		 ],
         functionality: this.move.bind(this, ORYX.CONFIG.KEY_CODE_LEFT, false)
         });
		 
		 // MOVE LEFT
		 this.facade.offer({
		 keyCodes: [{
				keyCode: ORYX.CONFIG.KEY_CODE_LEFT,
				keyAction: ORYX.CONFIG.KEY_ACTION_DOWN 
			}
		 ],
         functionality: this.move.bind(this, ORYX.CONFIG.KEY_CODE_LEFT, true)
         });
		 
		// MOVE RIGHT SMALL	
		 this.facade.offer({
		 keyCodes: [{
		 		metaKeys: [ORYX.CONFIG.META_KEY_META_CTRL],
				keyCode: ORYX.CONFIG.KEY_CODE_RIGHT,
				keyAction: ORYX.CONFIG.KEY_ACTION_DOWN 
			}
		 ],
         functionality: this.move.bind(this, ORYX.CONFIG.KEY_CODE_RIGHT, false)
         });
		 
		// MOVE RIGHT	
		 this.facade.offer({
		 keyCodes: [{
				keyCode: ORYX.CONFIG.KEY_CODE_RIGHT,
				keyAction: ORYX.CONFIG.KEY_ACTION_DOWN 
			}
		 ],
         functionality: this.move.bind(this, ORYX.CONFIG.KEY_CODE_RIGHT, true)
         });
		 
		// MOVE UP SMALL	
		 this.facade.offer({
		 keyCodes: [{
		 		metaKeys: [ORYX.CONFIG.META_KEY_META_CTRL],
				keyCode: ORYX.CONFIG.KEY_CODE_UP,
				keyAction: ORYX.CONFIG.KEY_ACTION_DOWN 
			}
		 ],
         functionality: this.move.bind(this, ORYX.CONFIG.KEY_CODE_UP, false)
         });
		 
		// MOVE UP	
		 this.facade.offer({
		 keyCodes: [{
				keyCode: ORYX.CONFIG.KEY_CODE_UP,
				keyAction: ORYX.CONFIG.KEY_ACTION_DOWN 
			}
		 ],
         functionality: this.move.bind(this, ORYX.CONFIG.KEY_CODE_UP, true)
         });
		 
		// MOVE DOWN SMALL	
		 this.facade.offer({
		 keyCodes: [{
		 		metaKeys: [ORYX.CONFIG.META_KEY_META_CTRL],
				keyCode: ORYX.CONFIG.KEY_CODE_DOWN,
				keyAction: ORYX.CONFIG.KEY_ACTION_DOWN 
			}
		 ],
         functionality: this.move.bind(this, ORYX.CONFIG.KEY_CODE_DOWN, false)
         });
		 
		// MOVE DOWN	
		 this.facade.offer({
		 keyCodes: [{
				keyCode: ORYX.CONFIG.KEY_CODE_DOWN,
				keyAction: ORYX.CONFIG.KEY_ACTION_DOWN 
			}
		 ],
         functionality: this.move.bind(this, ORYX.CONFIG.KEY_CODE_DOWN, true)
         });
		 
         
    },
    
	/**
	 * Select all shapes in the editor
	 *
	 */
	selectAll: function(e){
    	Event.stop(e.event);
		this.facade.setSelection(this.facade.getCanvas().getChildShapes(true))
	},
	
	move: function(key, far, e) {
		
    	Event.stop(e.event);

		// calculate the distance to move the objects and get the selection.
		var distance = far? 20 : 5;
		var selection = this.facade.getSelection();
		var currentSelection = this.facade.getSelection();
		var p = {x: 0, y: 0};
		
		// switch on the key pressed and populate the point to move by.
		switch(key) {

			case ORYX.CONFIG.KEY_CODE_LEFT:
				p.x = -1*distance;
				break;
			case ORYX.CONFIG.KEY_CODE_RIGHT:
				p.x = distance;
				break;
			case ORYX.CONFIG.KEY_CODE_UP:
				p.y = -1*distance;
				break;
			case ORYX.CONFIG.KEY_CODE_DOWN:
				p.y = distance;
				break;
		}
		
		// move each shape in the selection by the point calculated and update it.
		selection = selection.findAll(function(shape){ 
			// Check if this shape is docked to an shape in the selection			
			if(shape instanceof ORYX.Core.Node && shape.dockers.length == 1 && selection.include( shape.dockers.first().getDockedShape() )){ 
				return false 
			} 
			
			// Check if any of the parent shape is included in the selection
			var s = shape.parent; 
			do{ 
				if(selection.include(s)){ 
					return false
				}
			}while(s = s.parent); 
			
			// Otherwise, return true
			return true;
			
		});
		
		/* Edges must not be movable, if only edges are selected and at least 
		 * one of them is docked.
		 */
		var edgesMovable = true;
		var onlyEdgesSelected = selection.all(function(shape) {
			if(shape instanceof ORYX.Core.Edge) {
				if(shape.isDocked()) {
					edgesMovable = false;
				}
				return true;	
			}
			return false;
		});
		
		if(onlyEdgesSelected && !edgesMovable) {
			/* Abort moving shapes */
			return;
		}
		
		selection = selection.map(function(shape){ 
			if( shape instanceof ORYX.Core.Node ){
				/*if( shape.dockers.length == 1 ){
					return shape.dockers.first()
				} else {*/
					return shape
				//}
			} else if( shape instanceof ORYX.Core.Edge ) {
				
				var dockers = shape.dockers;
				
				if( selection.include( shape.dockers.first().getDockedShape() ) ){
					dockers = dockers.without( shape.dockers.first() )
				}

				if( selection.include( shape.dockers.last().getDockedShape() ) ){
					dockers = dockers.without( shape.dockers.last() )
				}
				
				return dockers	
							
			} else {
				return null
			}
		
		}).flatten().compact();
		
		if (selection.size() > 0) {
			
			//Stop moving at canvas borders
			var selectionBounds = [ this.facade.getCanvas().bounds.lowerRight().x,
			                        this.facade.getCanvas().bounds.lowerRight().y,
			                        0,
			                        0 ];
			selection.each(function(s) {
				selectionBounds[0] = Math.min(selectionBounds[0], s.bounds.upperLeft().x);
				selectionBounds[1] = Math.min(selectionBounds[1], s.bounds.upperLeft().y);
				selectionBounds[2] = Math.max(selectionBounds[2], s.bounds.lowerRight().x);
				selectionBounds[3] = Math.max(selectionBounds[3], s.bounds.lowerRight().y);
			});
			if(selectionBounds[0]+p.x < 0)
				p.x = -selectionBounds[0];
			if(selectionBounds[1]+p.y < 0)
				p.y = -selectionBounds[1];
			if(selectionBounds[2]+p.x > this.facade.getCanvas().bounds.lowerRight().x)
				p.x = this.facade.getCanvas().bounds.lowerRight().x - selectionBounds[2];
			if(selectionBounds[3]+p.y > this.facade.getCanvas().bounds.lowerRight().y)
				p.y = this.facade.getCanvas().bounds.lowerRight().y - selectionBounds[3];
			
			if(p.x!=0 || p.y!=0) {
				// Instantiate the moveCommand
				var commands = [new ORYX.Core.Command.Move(selection, p, null, currentSelection, this)];
				// Execute the commands			
				this.facade.executeCommands(commands);
			}
			
		}
	},
	
	getUndockedCommant: function(shapes){

		var undockEdgeCommand = ORYX.Core.Command.extend({
			construct: function(moveShapes){
				this.dockers = moveShapes.collect(function(shape){ return shape instanceof ORYX.Core.Controls.Docker ? {docker:shape, dockedShape:shape.getDockedShape(), refPoint:shape.referencePoint} : undefined }).compact();
			},			
			execute: function(){
				this.dockers.each(function(el){
					el.docker.setDockedShape(undefined);
				})
			},
			rollback: function(){
				this.dockers.each(function(el){
					el.docker.setDockedShape(el.dockedShape);
					el.docker.setReferencePoint(el.refPoint);
					//el.docker.update();
				})
			}
		});
		
		command = new undockEdgeCommand( shapes );
		command.execute();	
		return command;
	},
	
//    /**
//     * The key handler for this plugin. Every action from the set of cut, copy,
//     * paste and delete should be accessible trough simple keyboard shortcuts.
//     * This method checks whether any event triggers one of those actions.
//     *
//     * @param {Object} event The keyboard event that should be analysed for
//     *     triggering of this plugin.
//     */
//    keyHandler: function(event){
//        //TODO document what event.which is.
//        
//        ORYX.Log.debug("keysMove.js handles a keyEvent.");
//        
//        // assure we have the current event.
//        if (!event) 
//            event = window.event;
//        
//        // get the currently pressed key and state of control key.
//        var pressedKey = event.which || event.keyCode;
//        var ctrlPressed = event.ctrlKey;
//
//		// if the key is one of the arrow keys, forward to move and return.
//		if ([ORYX.CONFIG.KEY_CODE_LEFT, ORYX.CONFIG.KEY_CODE_RIGHT,
//			ORYX.CONFIG.KEY_CODE_UP, ORYX.CONFIG.KEY_CODE_DOWN].include(pressedKey)) {
//			
//			this.move(pressedKey, !ctrlPressed);
//			return;
//		}
//		
//    }
	
});
/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

if(!ORYX.Plugins) { ORYX.Plugins = {} }
if(!ORYX.Plugins.Layouter) { ORYX.Plugins.Layouter = {} }

new function(){
	
	/**
	 * Edge layouter is an implementation to layout an edge
	 * @class ORYX.Plugins.Layouter.EdgeLayouter
	 * @author Willi Tscheschner
	 */
	ORYX.Plugins.Layouter.EdgeLayouter = ORYX.Plugins.AbstractLayouter.extend({
		
		/**
		 * Layout only Edges
		 */
		layouted : [	"http://b3mn.org/stencilset/bpmn1.1#SequenceFlow", 
						"http://b3mn.org/stencilset/bpmn1.1#MessageFlow",
						"http://b3mn.org/stencilset/timjpdl3#SequenceFlow", 
						"http://b3mn.org/stencilset/jbpm4#SequenceFlow", 
						"http://b3mn.org/stencilset/bpmn2.0#MessageFlow",
						"http://b3mn.org/stencilset/bpmn2.0#SequenceFlow", 
						"http://b3mn.org/stencilset/bpmn2.0choreography#MessageFlow",
						"http://b3mn.org/stencilset/bpmn2.0choreography#SequenceFlow", 
						"http://b3mn.org/stencilset/bpmn2.0conversation#ConversationLink",
						"http://b3mn.org/stencilset/epc#ControlFlow",
						"http://www.signavio.com/stencilsets/processmap#ProcessLink",
						"http://www.signavio.com/stencilsets/organigram#connection"],
		
		/**
		 * Layout a set on edges
		 * @param {Object} edges
		 */
		layout: function(edges){
			edges.each(function(edge){
				this.doLayout(edge)
			}.bind(this))
		},
		
		/**
		 * Layout one edge
		 * @param {Object} edge
		 */
		doLayout: function(edge){
			// Get from and to node
			var from 	= edge.getIncomingNodes()[0]; 
			var to 		= edge.getOutgoingNodes()[0];
			
			// Return if one is null
			if (!from || !to) { return }
			
			var positions = this.getPositions(from, to, edge);
		
			if (positions.length > 0){
				this.setDockers(edge, positions[0].a, positions[0].b);
			}
				
		},
		
		/**
		 * Returns a set on positions which are not containt either 
		 * in the bounds in from or to.
		 * @param {Object} from Shape where the edge is come from
		 * @param {Object} to Shape where the edge is leading to
		 * @param {Object} edge Edge between from and to
		 */
		getPositions : function(from, to, edge){
			
			// Get absolute bounds
			var ab = from.absoluteBounds();
			var bb = to.absoluteBounds();
			
			// Get center from and to
			var a = ab.center();
			var b = bb.center();
			
			var am = ab.midPoint();
			var bm = bb.midPoint();
		
			// Get first and last reference point
			var first = Object.clone(edge.dockers.first().referencePoint);
			var last = Object.clone(edge.dockers.last().referencePoint);
			// Get the absolute one
			var aFirst = edge.dockers.first().getAbsoluteReferencePoint();
			var aLast = edge.dockers.last().getAbsoluteReferencePoint(); 
			
			// IF ------>
			// or  |
			//     V
			// Do nothing
			if (Math.abs(aFirst.x-aLast.x) < 1 || Math.abs(aFirst.y-aLast.y) < 1) {
				return []
			}
			
			// Calc center position, between a and b
			// depending on there weight
			var m = {}
			m.x = a.x < b.x ? 
					(((b.x - bb.width()/2) - (a.x + ab.width()/2))/2) + (a.x + ab.width()/2): 
					(((a.x - ab.width()/2) - (b.x + bb.width()/2))/2) + (b.x + bb.width()/2);

			m.y = a.y < b.y ? 
					(((b.y - bb.height()/2) - (a.y + ab.height()/2))/2) + (a.y + ab.height()/2): 
					(((a.y - ab.height()/2) - (b.y + bb.height()/2))/2) + (b.y + bb.height()/2);
								
								
			// Enlarge both bounds with 10
			ab.widen(5); // Wide the from less than 
			bb.widen(20);// the to because of the arrow from the edge
								
			var positions = [];
			var off = this.getOffset.bind(this);
			
			// Checks ----+
			//            |
			//            V
			if (!ab.isIncluded(b.x, a.y)&&!bb.isIncluded(b.x, a.y)) {
				positions.push({
					a : {x:b.x+off(last,bm,"x"),y:a.y+off(first,am,"y")},
					z : this.getWeight(from, a.x < b.x ? "r" : "l", to, a.y < b.y ? "t" : "b", edge)
				});
			}
						
			// Checks | 
			//        +--->
			if (!ab.isIncluded(a.x, b.y)&&!bb.isIncluded(a.x, b.y)) {
				positions.push({
					a : {x:a.x+off(first,am,"x"),y:b.y+off(last,bm,"y")},
					z : this.getWeight(from, a.y < b.y ? "b" : "t", to, a.x < b.x ? "l" : "r", edge)
				});
			}
						
			// Checks  --+
			//           |
			//           +--->
			if (!ab.isIncluded(m.x, a.y)&&!bb.isIncluded(m.x, b.y)) {
				positions.push({
					a : {x:m.x,y:a.y+off(first,am,"y")},
					b : {x:m.x,y:b.y+off(last,bm,"y")},
					z : this.getWeight(from, "r", to, "l", edge, a.x > b.x)
				});
			}
			
			// Checks | 
			//        +---+
			//            |
			//            V
			if (!ab.isIncluded(a.x, m.y)&&!bb.isIncluded(b.x, m.y)) {
				positions.push({
					a : {x:a.x+off(first,am,"x"),y:m.y},
					b : {x:b.x+off(last,bm,"x"),y:m.y},
					z : this.getWeight(from, "b", to, "t", edge, a.y > b.y)
				});
			}	
			
			// Sort DESC of weights
			return positions.sort(function(a,b){ return a.z < b.z ? 1 : (a.z == b.z ? -1 : -1)});
		},
		
		/**
		 * Returns a offset for the pos to the center of the bounds
		 * 
		 * @param {Object} val
		 * @param {Object} pos2
		 * @param {String} dir Direction x|y
		 */
		getOffset: function(pos, pos2, dir){
			return pos[dir] - pos2[dir];
		},
		
		/**
		 * Returns a value which shows the weight for this configuration
		 * 
		 * @param {Object} from Shape which is coming from
		 * @param {String} d1 Direction where is goes
		 * @param {Object} to Shape which goes to
		 * @param {String} d2 Direction where it comes to
		 * @param {Object} edge Edge between from and to
		 * @param {Boolean} reverse Reverse the direction (e.g. "r" -> "l")
		 */
		getWeight: function(from, d1, to, d2, edge, reverse){
			
			d1 = (d1||"").toLowerCase();
			d2 = (d2||"").toLowerCase();
			
			if (!["t","r","b","l"].include(d1)){ d1 = "r"}
			if (!["t","r","b","l"].include(d2)){ d1 = "l"}
			
			// If reverse is set
			if (reverse) {
				// Reverse d1 and d2
				d1 = d1=="t"?"b":(d1=="r"?"l":(d1=="b"?"t":(d1=="l"?"r":"r")))
				d2 = d2=="t"?"b":(d2=="r"?"l":(d2=="b"?"t":(d2=="l"?"r":"r")))
			}
			
					
			var weight = 0;
			// Get rules for from "out" and to "in"
			var dr1 = this.facade.getRules().getLayoutingRules(from, edge)["out"];
			var dr2 = this.facade.getRules().getLayoutingRules(to, edge)["in"];

			var fromWeight = dr1[d1];
			var toWeight = dr2[d2];


			/**
			 * Return a true if the center 1 is in the same direction than center 2
			 * @param {Object} direction
			 * @param {Object} center1
			 * @param {Object} center2
			 */
			var sameDirection = function(direction, center1, center2){
				switch(direction){
					case "t": return Math.abs(center1.x - center2.x) < 2 && center1.y < center2.y
					case "r": return center1.x > center2.x && Math.abs(center1.y - center2.y) < 2
					case "b": return Math.abs(center1.x - center2.x) < 2 && center1.y > center2.y
					case "l": return center1.x < center2.x && Math.abs(center1.y - center2.y) < 2
					default: return false;
				}
			}

			// Check if there are same incoming edges from 'from'
			var sameIncomingFrom = from
								.getIncomingShapes()
								.findAll(function(a){ return a instanceof ORYX.Core.Edge})
								.any(function(e){ 
									return sameDirection(d1, e.dockers[e.dockers.length-2].bounds.center(), e.dockers.last().bounds.center());
								});

			// Check if there are same outgoing edges from 'to'
			var sameOutgoingTo = to
								.getOutgoingShapes()
								.findAll(function(a){ return a instanceof ORYX.Core.Edge})
								.any(function(e){ 
									return sameDirection(d2, e.dockers[1].bounds.center(), e.dockers.first().bounds.center());
								});
			
			// If there are equivalent edges, set 0
			//fromWeight = sameIncomingFrom ? 0 : fromWeight;
			//toWeight = sameOutgoingTo ? 0 : toWeight;
			
			// Get the sum of "out" and the direction plus "in" and the direction 						
			return (sameIncomingFrom||sameOutgoingTo?0:fromWeight+toWeight);
		},
		
		/**
		 * Removes all current dockers from the node 
		 * (except the start and end) and adds two new
		 * dockers, on the position a and b.
		 * @param {Object} edge
		 * @param {Object} a
		 * @param {Object} b
		 */
		setDockers: function(edge, a, b){
			if (!edge){ return }
			
			// Remove all dockers (implicit,
			// start and end dockers will not removed)
			edge.dockers.each(function(r){
				edge.removeDocker(r);
			});
			
			// For a and b (if exists), create
			// a new docker and set position
			[a, b].compact().each(function(pos){
				var docker = edge.createDocker(undefined, pos);
				docker.bounds.centerMoveTo(pos);
			});
			
			// Update all dockers from the edge
			edge.dockers.each(function(docker){
				docker.update()
			})
			
			// Update edge
			//edge.refresh();
			edge._update(true);
			
		}
	});
	
	
}()
/*
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/*
 * All code Copyright 2013 KIS Consultancy all rights reserved
 */

if(!ORYX.Plugins)
	ORYX.Plugins = new Object();

new function(){
	
	ORYX.Plugins.BPMN2_0 = {
	
		/**
		 *	Constructor
		 *	@param {Object} Facade: The Facade of the Editor
		 */
		construct: function(facade){
			this.facade = facade;
			
			this.facade.registerOnEvent(ORYX.CONFIG.EVENT_DRAGDOCKER_DOCKED, this.handleDockerDocked.bind(this));
			this.facade.registerOnEvent(ORYX.CONFIG.EVENT_PROPWINDOW_PROP_CHANGED, this.handlePropertyChanged.bind(this));
			this.facade.registerOnEvent('layout.bpmn2_0.pool', this.handleLayoutPool.bind(this));
			this.facade.registerOnEvent('layout.bpmn2_0.subprocess', this.handleSubProcess.bind(this));
			this.facade.registerOnEvent(ORYX.CONFIG.EVENT_SHAPEREMOVED, this.handleShapeRemove.bind(this));
			
			this.facade.registerOnEvent(ORYX.CONFIG.EVENT_LOADED, this.afterLoad.bind(this));
			

			this.namespace = undefined;
		},
		
		/**
         * Force to update every pool
         */
        afterLoad: function(){
                this.facade.getCanvas().getChildNodes().each(function(shape){
                        if (shape.getStencil().id().endsWith("Pool")) {
                                this.handleLayoutPool({
                                        shape: shape
                                });
                        }
                }.bind(this))
        },
       
        /**
         * If a pool is selected and contains no lane,
         * a lane is created automagically
         */
        onSelectionChanged: function(event) {
                var selection = event.elements;
               
                if(selection && selection.length === 1) {
                        var namespace = this.getNamespace();
                        var shape = selection[0];
                        if(shape.getStencil().idWithoutNs() === "Pool") {
                                if(shape.getChildNodes().length === 0) {
                                        // create a lane inside the selected pool
                                        var option = {
                                                        type:namespace + "Lane",
                                                        position:{x:0,y:0},
                                                        namespace:shape.getStencil().namespace(),
                                                        parent:shape
                                        };
                                        this.facade.createShape(option);
                                        this.facade.getCanvas().update();
                                        this.facade.setSelection([shape]);
                                }
                        }
                }
               
                // Preventing selection of all lanes but not the pool
                if(selection.any(function(s){ return s instanceof ORYX.Core.Node && s.getStencil().id().endsWith("Lane")})){
                        var lanes = selection.findAll(function(s){
                                return s instanceof ORYX.Core.Node && s.getStencil().id().endsWith("Lane")
                        });
                       
                        var pools = [];
                        var unselectLanes = [];
                        lanes.each(function(lane){
                                pools.push(this.getParentPool(lane))
                        }.bind(this));
                       
                        pools = pools.uniq().findAll(function(pool){
                                var childLanes = this.getLanes(pool, true);
                                if (childLanes.all(function(lane){ return lanes.include(lane)})){
                                        unselectLanes = unselectLanes.concat(childLanes);
                                        return true;
                                } else if (selection.include(pool) && childLanes.any(function(lane){ return lanes.include(lane)})) {
                                        unselectLanes = unselectLanes.concat(childLanes);
                                        return true;
                                } else {
                                        return false;
                                }
                        }.bind(this))
                       
                        if (unselectLanes.length > 0 && pools.length > 0){
                                selection = selection.without.apply(selection, unselectLanes);
                                selection = selection.concat(pools);
                                this.facade.setSelection(selection.uniq());
                        }
                }
        },
       
        handleShapeRemove: function(option) {
               
                var sh                          = option.shape;
                var parent                      = option.parent;
                                       
                if (sh instanceof ORYX.Core.Node && sh.getStencil().idWithoutNs() === "Lane" && this.facade.isExecutingCommands()) {
               
                        var pool = this.getParentPool(parent);
                        if (pool&&pool.parent){        
                       
                                var isLeafFn = function(leaf){
                                        return !leaf.getChildNodes().any(function(r){ return r.getStencil().idWithoutNs() === "Lane"});
                                }
                               
                                var isLeaf = isLeafFn(sh);
                                var parentHasMoreLanes = parent.getChildNodes().any(function(r){ return r.getStencil().idWithoutNs() === "Lane"});
                               
                                if (isLeaf && parentHasMoreLanes){
                                       
                                        var command = new ResizeLanesCommand(sh, parent, pool, this);
                                        this.facade.executeCommands([command]);
                                       
                                } else if(      !isLeaf &&
                                                        !this.facade.getSelection().any(function(select){ // Find one of the selection, which is a lane and child of "sh" and is a leaf lane
                                                                        return  select instanceof ORYX.Core.Node && select.getStencil().idWithoutNs() === "Lane" &&
                                                                                        select.isParent(sh) && isLeafFn(select);})) {
                                                                                               
                                        var Command = ORYX.Core.Command.extend({
                                                construct: function(shape, facade) {
                                                        this.children = shape.getChildNodes(true);
                                                        this.facade = facade;
                                                },
                                                execute: function() {
                                                        this.children.each(function(child){
                                                                child.bounds.moveBy(30,0)
                                                        });
                                                        //this.facade.getCanvas().update();
                                                },
                                                rollback: function() {
                                                        this.children.each(function(child){
                                                                child.bounds.moveBy(-30,0)
                                                        })
                                                        //this.facade.getCanvas().update();
                                                }
                                        });
                                        this.facade.executeCommands([new Command(sh, this.facade)]);
                                       
                                } else if (isLeaf&&!parentHasMoreLanes&&parent == pool){
                                        parent.add(sh);
                                }
                        }
               
                }
               
        },

		
		hashedSubProcesses: {},
		
		hashChildShapes: function(shape){
			var children = shape.getChildNodes();
			children.each(function(child){
				if (this.hashedSubProcesses[child.id]){
					this.hashedSubProcesses[child.id] = child.absoluteXY();
					this.hashedSubProcesses[child.id].width 	= child.bounds.width();
					this.hashedSubProcesses[child.id].height 	= child.bounds.height();
					this.hashChildShapes(child);
				}
			}.bind(this));
		},
	
		/**
		 * Handle the layouting of a sub process.
		 * Mainly to adjust the child dockers of a sub process. 
		 *
		 */
		handleSubProcess : function(option) {
			
			var sh = option.shape;
			
			if (!this.hashedSubProcesses[sh.id]) {
				this.hashedSubProcesses[sh.id] = sh.absoluteXY();
				this.hashedSubProcesses[sh.id].width 	= sh.bounds.width();
				this.hashedSubProcesses[sh.id].height 	= sh.bounds.height();
				return;
			}
			
			var offset = sh.absoluteXY();
			offset.x -= this.hashedSubProcesses[sh.id].x;
			offset.y -= this.hashedSubProcesses[sh.id].y;
			
			var resized = this.hashedSubProcesses[sh.id].width !== sh.bounds.width() || this.hashedSubProcesses[sh.id].height !== sh.bounds.height();
			
			this.hashedSubProcesses[sh.id] = sh.absoluteXY();
			this.hashedSubProcesses[sh.id].width 	= sh.bounds.width();
			this.hashedSubProcesses[sh.id].height 	= sh.bounds.height();
			this.hashChildShapes(sh);
			
			
			// Move dockers only if currently is not resizing
			if (this.facade.isExecutingCommands()&&!resized) {
				this.moveChildDockers(sh, offset);
			}
		},
		
		moveChildDockers: function(shape, offset){
			
			if (!offset.x && !offset.y) {
				return;
			} 
			
			var children = shape.getChildNodes(true);
			
			// Get all nodes
			var dockers = children
				// Get all incoming and outgoing edges
				.map(function(node){
					return [].concat(node.getIncomingShapes())
							.concat(node.getOutgoingShapes())
				})
				// Flatten all including arrays into one
				.flatten()
				// Get every edge only once
				.uniq()
				// Get all dockers
				.map(function(edge){
					return edge.dockers.length > 2 ? 
							edge.dockers.slice(1, edge.dockers.length-1) : 
							[];
				})
				// Flatten the dockers lists
				.flatten();
	
			var abs = shape.absoluteBounds();
			abs.moveBy(-offset.x, -offset.y)
			var obj = {};
			dockers.each(function(docker){
				
				if (docker.isChanged){
					return;
				}
				
				var off = Object.clone(offset);
				
				if (!abs.isIncluded(docker.bounds.center())){
					var index 	= docker.parent.dockers.indexOf(docker);
					var size	= docker.parent.dockers.length;
					var from 	= docker.parent.getSource();
					var to 		= docker.parent.getTarget();
					
					var bothAreIncluded = children.include(from) && children.include(to);
					
					if (!bothAreIncluded){
						var previousIsOver = index !== 0 ? abs.isIncluded(docker.parent.dockers[index-1].bounds.center()) : false;
						var nextIsOver = index !== size-1 ? abs.isIncluded(docker.parent.dockers[index+1].bounds.center()) : false;
						
						if (!previousIsOver && !nextIsOver){ return; }
						
						var ref = docker.parent.dockers[previousIsOver ? index-1 : index+1];
						if (Math.abs(-Math.abs(ref.bounds.center().x-docker.bounds.center().x)) < 2){
							off.y = 0;
						} else if(Math.abs(-Math.abs(ref.bounds.center().y-docker.bounds.center().y)) < 2){
							off.x = 0;
						} else {
							return;
						}
					}
					
				}
				
				obj[docker.getId()] = {
					docker:docker,
					offset:off
				}
			})
			
			// Set dockers
			this.facade.executeCommands([new ORYX.Core.MoveDockersCommand(obj)]);
				
		},
		
		/**
		 * DragDocker.Docked Handler
		 *
		 */	
		handleDockerDocked: function(options) {
			var namespace = this.getNamespace();
			
			var edge = options.parent;
			var edgeSource = options.target;
			
			if(edge.getStencil().id() === namespace + "SequenceFlow") {
				var isGateway = edgeSource.getStencil().groups().find(function(group) {
						if(group == "Gateways") 
							return group;
					});
				if(!isGateway && (edge.properties["oryx-conditiontype"] == "Expression"))
					// show diamond on edge source
					edge.setProperty("oryx-showdiamondmarker", true);
				else 
					// do not show diamond on edge source
					edge.setProperty("oryx-showdiamondmarker", false);
				
				// update edge rendering
				//edge.update();
				
				this.facade.getCanvas().update();
			}
		},
		
		/**
		 * PropertyWindow.PropertyChanged Handler
		 */
		handlePropertyChanged: function(option) {
			var namespace = this.getNamespace();
			
			var shapes = option.elements;
			var propertyKey = option.key;
			var propertyValue = option.value;
			
			var changed = false;
			shapes.each(function(shape){
				if((shape.getStencil().id() === namespace + "SequenceFlow") &&
					(propertyKey === "oryx-conditiontype")) {
					
					if(propertyValue != "Expression")
						// Do not show the Diamond
						shape.setProperty("oryx-showdiamondmarker", false);
					else {
						var incomingShapes = shape.getIncomingShapes();
						
						if(!incomingShapes) {
							shape.setProperty("oryx-showdiamondmarker", true);
						}
						
						var incomingGateway = incomingShapes.find(function(aShape) {
							var foundGateway = aShape.getStencil().groups().find(function(group) {
								if(group == "Gateways") 
									return group;
							});
							if(foundGateway)
								return foundGateway;
						});
						
						if(!incomingGateway) 
							// show diamond on edge source
							shape.setProperty("oryx-showdiamondmarker", true);
						else
							// do not show diamond
							shape.setProperty("oryx-showdiamondmarker", false);
					}
					
					changed = true;
				}
			}.bind(this));
			
			if(changed) {this.facade.getCanvas().update();}
			
		},
		
		hashedPoolPositions : {},
		hashedLaneDepth : {},
		hashedBounds : {},
		hashedPositions: {},
		
		/**
         * Handler for layouting event 'layout.bpmn2_0.pool'
         * @param {Object} event
         */
        handleLayoutPool: function(event){
               
               
                var pool = event.shape;
                var selection = this.facade.getSelection();
                var currentShape = selection.include(pool) ? pool : selection.first();
               
                currentShape = currentShape || pool;
               
                this.currentPool = pool;
               
                // Check if it is a pool or a lane
                if (!(currentShape.getStencil().id().endsWith("Pool") || currentShape.getStencil().id().endsWith("Lane"))) {
                        return;
                }
               
                // Check if the lane is within the pool and is not removed lately
                if (currentShape !== pool && !currentShape.isParent(pool) && !this.hashedBounds[pool.id][currentShape.id]){
                        return;
                }
               
               
                if (!this.hashedBounds[pool.id]) {
                        this.hashedBounds[pool.id] = {};
                }
               
                // Find all child lanes
                var lanes = this.getLanes(pool);
               
                if (lanes.length <= 0) {
                        return
                }
               
                var allLanes = this.getLanes(pool, true), hp;
                var considerForDockers = allLanes.clone();
               
                var hashedPositions = $H({});
                allLanes.each(function(lane){
                        hashedPositions[lane.id] = lane.bounds.upperLeft();
                })
               
               
               
                // Show/hide caption regarding the number of lanes
                if (lanes.length === 1 && this.getLanes(lanes.first()).length <= 0) {
                        // TRUE if there is a caption
                        lanes.first().setProperty("oryx-showcaption", lanes.first().properties["oryx-name"].trim().length > 0);
                        var rect = lanes.first().node.getElementsByTagName("rect");
                        rect[0].setAttributeNS(null, "display", "none");
                } else {
                        allLanes.invoke("setProperty", "oryx-showcaption", true);
                        allLanes.each(function(lane){
                                var rect = lane.node.getElementsByTagName("rect");
                                rect[0].removeAttributeNS(null, "display");
                        })
                }
               
                var deletedLanes = [];
                var addedLanes = [];
               
                // Get all new lanes
                var i=-1;
                while (++i<allLanes.length) {
                        if (!this.hashedBounds[pool.id][allLanes[i].id]){
                                addedLanes.push(allLanes[i])
                        }
                }
               
                if (addedLanes.length > 0){
                        currentShape = addedLanes.first();
                }
               
               
                // Get all deleted lanes
                var resourceIds = $H(this.hashedBounds[pool.id]).keys();
                var i=-1;
                while (++i<resourceIds.length) {
                        if (!allLanes.any(function(lane){ return lane.id == resourceIds[i]})){
                                deletedLanes.push(this.hashedBounds[pool.id][resourceIds[i]]);
                                selection = selection.without(function(r){ return r.id == resourceIds[i] });
                        }
                }              
               
                var height, width, x, y;
               
                if (deletedLanes.length > 0 || addedLanes.length > 0) {
                       
                        if (addedLanes.length === 1 && this.getLanes(addedLanes[0].parent).length === 1){
                                // Set height from the pool
                                height = this.adjustHeight(lanes, addedLanes[0].parent);
                        } else {
                                // Set height from the pool
                                height = this.updateHeight(pool);
                        }
                        // Set width from the pool
                        width = this.adjustWidth(lanes, pool.bounds.width());  
                       
                        pool.update();
                }
               
                /**
                 * Set width/height depending on the pool
                 */
                else if (pool == currentShape) {
                       
                        if (selection.length === 1 && this.isResized(pool, this.hashedPoolPositions[pool.id])) {
                                var oldXY = this.hashedPoolPositions[pool.id].upperLeft();
                                var xy = pool.bounds.upperLeft();
                                var scale = 0;
                                if (this.shouldScale(pool)){
                                        var old = this.hashedPoolPositions[pool.id];
                                        scale = old.height()/pool.bounds.height();
                                }
                       
                                this.adjustLanes(pool, allLanes, oldXY.x - xy.x, oldXY.y - xy.y, scale);
                        }
                       
                        // Set height from the pool
                        height = this.adjustHeight(lanes, undefined, pool.bounds.height());
                        // Set width from the pool
                        width = this.adjustWidth(lanes, pool.bounds.width());          
                }
               
                /**???
                 * Set width/height depending on containing lanes
                 */            
                else {
                       
                        // Reposition the pool if one shape is selected and the upperleft has changed
                        if (selection.length === 1 && this.isResized(currentShape, this.hashedBounds[pool.id][currentShape.id])){
                                var oldXY = this.hashedBounds[pool.id][currentShape.id].upperLeft();
                                var xy = currentShape.absoluteXY();
                                x = oldXY.x - xy.x;
                                y = oldXY.y - xy.y;
                               
                                // Adjust all other lanes beneath this lane
                                if (x||y){
                                        considerForDockers = considerForDockers.without(currentShape);
                                        this.adjustLanes(pool, this.getAllExcludedLanes(pool, currentShape), x, 0);
                                }
                               
                                // Adjust all child lanes
                                var childLanes = this.getLanes(currentShape, true);
                                if (childLanes.length > 0){
                                        if (this.shouldScale(currentShape)){
                                                var old = this.hashedBounds[pool.id][currentShape.id];
                                                var scale = old.height()/currentShape.bounds.height();
                                                this.adjustLanes(pool, childLanes, x, y, scale);
                                        } else {
                                                this.adjustLanes(pool, childLanes, x, y, 0);
                                        }
                                }
                        }
                       
                        // Cache all bounds
                        var changes = allLanes.map(function(lane){ return {
                                shape: lane,
                                bounds: lane.bounds.clone()
                        } });
                       
                        // Get height and adjust child heights
                        height = this.adjustHeight(lanes, currentShape);
                        // Check if something has changed and maybe create a command
                        this.checkForChanges(allLanes, changes);
                       
                        // Set width from the current shape
                        width = this.adjustWidth(lanes, currentShape.bounds.width()+(this.getDepth(currentShape,pool)*30));
                }
               
                this.setDimensions(pool, width, height, x, y);
               
               
                if (this.facade.isExecutingCommands() && (deletedLanes.length === 0 || addedLanes.length !== 0)){
                        // Update all dockers
                        this.updateDockers(considerForDockers, pool);
                       
                        // Check if the order has changed
                        if (this.hashedPositions[pool.id] && this.hashedPositions[pool.id].keys().any(function(key, i){
                                        return (allLanes[i]||{}).id     !== key;
                                })){
                               
                                var LanesHasBeenReordered = ORYX.Core.Command.extend({
                                        construct: function(originPosition, newPosition, lanes, plugin, poolId) {
                                                this.originPosition = Object.clone(originPosition);
                                                this.newPosition = Object.clone(newPosition);
                                                this.lanes = lanes;
                                                this.plugin = plugin;
                                                this.pool = poolId;
                                        },
                                        execute: function(){
                                                if (!this.executed){
                                                        this.executed = true;
                                                        this.lanes.each(function(lane){
                                                                if (this.newPosition[lane.id])
                                                                        lane.bounds.moveTo(this.newPosition[lane.id])
                                                        }.bind(this));
                                                        this.plugin.hashedPositions[this.pool] = Object.clone(this.newPosition);
                                                }
                                        },
                                        rollback: function(){
                                                this.lanes.each(function(lane){
                                                        if (this.originPosition[lane.id])
                                                                lane.bounds.moveTo(this.originPosition[lane.id])
                                                }.bind(this));
                                                this.plugin.hashedPositions[this.pool] = Object.clone(this.originPosition);
                                        }
                                });
                               
                                var hp2 = $H({});
                                allLanes.each(function(lane){
                                        hp2[lane.id] = lane.bounds.upperLeft();
                                })
                       
                                var command = new LanesHasBeenReordered(hashedPositions, hp2, allLanes, this, pool.id);
                                this.facade.executeCommands([command]);
                                       
                        }
                }
               
                this.hashedBounds[pool.id] = {};
                this.hashedPositions[pool.id] = hashedPositions;
               
                var i=-1;
                while (++i < allLanes.length) {
                        // Cache positions
                        this.hashedBounds[pool.id][allLanes[i].id] = allLanes[i].absoluteBounds();
                       
                        // Cache also the bounds of child shapes, mainly for child subprocesses
                        this.hashChildShapes(allLanes[i]);
               
                        this.hashedLaneDepth[allLanes[i].id] = this.getDepth(allLanes[i], pool);
                       
                        this.forceToUpdateLane(allLanes[i]);
                }
               
                this.hashedPoolPositions[pool.id] = pool.bounds.clone();
               
               
                // Update selection
                //this.facade.setSelection(selection);          
        },
       
        shouldScale: function(element){
                var childLanes = element.getChildNodes().findAll(function(shape){ return shape.getStencil().id().endsWith("Lane") })
                return childLanes.length > 1 || childLanes.any(function(lane){ return this.shouldScale(lane) }.bind(this))
        },
       
        /**
         * Lookup if some bounds has changed
         * @param {Object} lanes
         * @param {Object} changes
         */
        checkForChanges: function(lanes, changes){
                // Check if something has changed
                if (this.facade.isExecutingCommands() && changes.any(function(change){
                        return change.shape.bounds.toString() !== change.bounds.toString();
                })){
                       
                        var Command = ORYX.Core.Command.extend({
                                                construct: function(changes) {
                                                        this.oldState = changes;
                                                        this.newState = changes.map(function(s){ return {shape:s.shape, bounds:s.bounds.clone()}});
                                                },
                                                execute: function(){
                                                        if (this.executed){
                                                                this.applyState(this.newState);
                                                        }
                                                        this.executed = true;
                                                },
                                                rollback: function(){
                                                        this.applyState(this.oldState);
                                                },
                                                applyState: function(state){
                                                        state.each(function(s){
                                                                s.shape.bounds.set(s.bounds.upperLeft(), s.bounds.lowerRight());
                                                        })
                                                }
                                        });
                                       
                        this.facade.executeCommands([new Command(changes)]);
                }
        },
       
        isResized: function(shape, bounds){
               
                if (!bounds||!shape){
                        return false;
                }
               
                var oldB = bounds;
                //var oldXY = oldB.upperLeft();
                //var xy = shape.absoluteXY();
                return Math.round(oldB.width() - shape.bounds.width()) !== 0 || Math.round(oldB.height() - shape.bounds.height()) !== 0
        },
       
        adjustLanes: function(pool, lanes, x, y, scale){
               
                scale = scale || 0;

                // For every lane, adjust the child nodes with the offset
                lanes.each(function(l){
                        l.getChildNodes().each(function(child){
                                if (!child.getStencil().id().endsWith("Lane")){
                                        var cy = scale ? child.bounds.center().y - (child.bounds.center().y/scale) : -y;
                                        child.bounds.moveBy((x||0), -cy);
                                       
                                        if (scale&&child.getStencil().id().endsWith("Subprocess")) {
                                                this.moveChildDockers(child, {x:(0), y:-cy});
                                        }
                               
                                }
                        }.bind(this));
                        this.hashedBounds[pool.id][l.id].moveBy(-(x||0), !scale?-y:0);
                        if (scale) {
                                l.isScaled = true;
                        }
                }.bind(this))
               
        },
       
        getAllExcludedLanes: function(parent, lane){
                var lanes = [];
                parent.getChildNodes().each(function(shape){
                        if ((!lane || shape !== lane) && shape.getStencil().id().endsWith("Lane")){
                                lanes.push(shape);
                                lanes = lanes.concat(this.getAllExcludedLanes(shape, lane));
                        }
                }.bind(this));
                return lanes;
        },
       
       
        forceToUpdateLane: function(lane){
               
                if (lane.bounds.height() !== lane._svgShapes[0].height) {      
                        lane.isChanged = true;
                        lane.isResized = true;
                        lane._update();
                }
        },
       
        getDepth: function(child, parent){
               
                var i=0;
                while(child && child.parent && child !== parent){
                        child = child.parent;
                        ++i
                }
                return i;
        },
       
        updateDepth: function(lane, fromDepth, toDepth){
               
                var xOffset = (fromDepth - toDepth) * 30;
               
                lane.getChildNodes().each(function(shape){
                        shape.bounds.moveBy(xOffset, 0);
                       
                        [].concat(children[j].getIncomingShapes())
                                        .concat(children[j].getOutgoingShapes())
                                       
                })
               
        },
       
        setDimensions: function(shape, width, height, x, y){
                var isLane = shape.getStencil().id().endsWith("Lane");
                // Set the bounds
                shape.bounds.set(
                                isLane  ? 30 : (shape.bounds.a.x - (x || 0)),
                                isLane  ? shape.bounds.a.y : (shape.bounds.a.y - (y || 0)),
                                width   ? shape.bounds.a.x + width - (isLane?30:(x||0)) : shape.bounds.b.x,
                                height  ? shape.bounds.a.y + height - (isLane?0:(y||0)) : shape.bounds.b.y
                        );
        },

        setLanePosition: function(shape, y){
               
                shape.bounds.moveTo(30, y);
       
        },
               
        adjustWidth: function(lanes, width) {
               
                // Set width to each lane
                (lanes||[]).each(function(lane){
                        this.setDimensions(lane, width);
                        this.adjustWidth(this.getLanes(lane), width-30);
                }.bind(this));
               
                return width;
        },
       
       
        adjustHeight: function(lanes, changedLane, propagateHeight){
               
                var oldHeight = 0;
                if (!changedLane && propagateHeight){
                        var i=-1;
                        while (++i<lanes.length){      
                                oldHeight += lanes[i].bounds.height();          
                        }
                }
               
                var i=-1;
                var height = 0;
               
                // Iterate trough every lane
                while (++i<lanes.length){
                       
                        if (lanes[i] === changedLane) {
                                // Propagate new height down to the children
                                this.adjustHeight(this.getLanes(lanes[i]), undefined, lanes[i].bounds.height());
                               
                                lanes[i].bounds.set({x:30, y:height}, {x:lanes[i].bounds.width()+30, y:lanes[i].bounds.height()+height})
                                                               
                        } else if (!changedLane && propagateHeight) {
                               
                                var tempHeight = (lanes[i].bounds.height() * propagateHeight) / oldHeight;
                                // Propagate height
                                this.adjustHeight(this.getLanes(lanes[i]), undefined, tempHeight);
                                // Set height propotional to the propagated and old height
                                this.setDimensions(lanes[i], null, tempHeight);
                                this.setLanePosition(lanes[i], height);
                        } else {
                                // Get height from children
                                var tempHeight = this.adjustHeight(this.getLanes(lanes[i]), changedLane, propagateHeight);
                                if (!tempHeight) {
                                        tempHeight = lanes[i].bounds.height();
                                }
                                this.setDimensions(lanes[i], null, tempHeight);
                                this.setLanePosition(lanes[i], height);
                        }
                       
                        height += lanes[i].bounds.height();
                }
               
                return height;
               
        },
       
       
        updateHeight: function(root){
               
                var lanes = this.getLanes(root);
               
                if (lanes.length == 0){
                        return root.bounds.height();
                }
               
                var height = 0;
                var i=-1;
                while (++i < lanes.length) {
                        this.setLanePosition(lanes[i], height);
                        height += this.updateHeight(lanes[i]);
                }
               
                this.setDimensions(root, null, height);
               
                return height;
        },
       
        getOffset: function(lane, includePool, pool){
               
                var offset = {x:0,y:0};
               
               
                /*var parent = lane;
                 while(parent) {
                                               
                       
                        var offParent = this.hashedBounds[pool.id][parent.id] ||(includePool === true ? this.hashedPoolPositions[parent.id] : undefined);
                        if (offParent){
                                var ul = parent.bounds.upperLeft();
                                var ulo = offParent.upperLeft();
                                offset.x += ul.x-ulo.x;
                                offset.y += ul.y-ulo.y;
                        }
                       
                        if (parent.getStencil().id().endsWith("Pool")) {
                                break;
                        }
                       
                        parent = parent.parent;
                }       */
               
                var offset = lane.absoluteXY();
               
                var hashed = this.hashedBounds[pool.id][lane.id] ||(includePool === true ? this.hashedPoolPositions[lane.id] : undefined);
                if (hashed) {
                        offset.x -= hashed.upperLeft().x;      
                        offset.y -= hashed.upperLeft().y;              
                } else {
                        return {x:0,y:0}
                }              
                return offset;
        },
       
        getNextLane: function(shape){
                while(shape && !shape.getStencil().id().endsWith("Lane")){
                        if (shape instanceof ORYX.Core.Canvas) {
                                return null;
                        }
                        shape = shape.parent;
                }
                return shape;
        },
       
        getParentPool: function(shape){
                while(shape && !shape.getStencil().id().endsWith("Pool")){
                        if (shape instanceof ORYX.Core.Canvas) {
                                return null;
                        }
                        shape = shape.parent;
                }
                return shape;
        },
       
        updateDockers: function(lanes, pool){
               
                var absPool = pool.absoluteBounds(), movedShapes = [];
                var oldPool = (this.hashedPoolPositions[pool.id]||absPool).clone();
               
                var i=-1, j=-1, k=-1, l=-1, docker;
                var dockers = {};
               
                while (++i < lanes.length) {
                       
                        if (!this.hashedBounds[pool.id][lanes[i].id]) {
                                continue;
                        }
                       
                        var isScaled = lanes[i].isScaled;
                        delete lanes[i].isScaled;
                        var children = lanes[i].getChildNodes();
                        var absBounds = lanes[i].absoluteBounds();
                        var oldBounds = (this.hashedBounds[pool.id][lanes[i].id]||absBounds);
                        //oldBounds.moveBy((absBounds.upperLeft().x-lanes[i].bounds.upperLeft().x), (absBounds.upperLeft().y-lanes[i].bounds.upperLeft().y));
                        var offset = this.getOffset(lanes[i], true, pool);
                        var xOffsetDepth = 0;

                        var depth = this.getDepth(lanes[i], pool);
                        if ( this.hashedLaneDepth[lanes[i].id] !== undefined &&  this.hashedLaneDepth[lanes[i].id] !== depth) {
                                xOffsetDepth = (this.hashedLaneDepth[lanes[i].id] - depth) * 30;
                                offset.x += xOffsetDepth;
                        }
                       
                        j=-1;
                       
                        while (++j < children.length) {
                               
                                if (xOffsetDepth && !children[j].getStencil().id().endsWith("Lane")) {
                                        movedShapes.push({xOffset:xOffsetDepth, shape: children[j]});
                                        children[j].bounds.moveBy(xOffsetDepth, 0);
                                }
                               
                                if (children[j].getStencil().id().endsWith("Subprocess")) {
                                        this.moveChildDockers(children[j], offset);
                                }
                               
                                var edges = [].concat(children[j].getIncomingShapes())
                                        .concat(children[j].getOutgoingShapes())
                                        // Remove all edges which are included in the selection from the list
                                        .findAll(function(r){ return r instanceof ORYX.Core.Edge })

                                k=-1;
                                while (++k < edges.length) {                    
                                       
                                        if (edges[k].getStencil().id().endsWith("MessageFlow")) {
                                                this.layoutEdges(children[j], [edges[k]], offset);
                                                continue;
                                        }
                                       
                                        l=-1;
                                        while (++l < edges[k].dockers.length) {
                                               
                                                docker = edges[k].dockers[l];
                                               
                                                if (docker.getDockedShape()||docker.isChanged){
                                                        continue;
                                                }
                                       
                                       
                                                pos = docker.bounds.center();
                                               
                                                // Check if the modified center included the new position
                                                var isOverLane = oldBounds.isIncluded(pos);
                                                // Check if the original center is over the pool
                                                var isOutSidePool = !oldPool.isIncluded(pos);
                                                var previousIsOverLane = l == 0 ? isOverLane : oldBounds.isIncluded(edges[k].dockers[l-1].bounds.center());
                                                var nextIsOverLane = l == edges[k].dockers.length-1 ? isOverLane : oldBounds.isIncluded(edges[k].dockers[l+1].bounds.center());
                                                var off = Object.clone(offset);
                                               
                                                // If the
                                                if (isScaled && isOverLane && this.isResized(lanes[i], this.hashedBounds[pool.id][lanes[i].id])){
                                                        var relY = (pos.y - absBounds.upperLeft().y + off.y);
                                                        off.y -= (relY - (relY * (absBounds.height()/oldBounds.height())));
                                                }
                                               
                                                // Check if the previous dockers docked shape is from this lane
                                                // Otherwise, check if the docker is over the lane OR is outside the lane
                                                // but the previous/next was over this lane
                                                if (isOverLane){
                                                        dockers[docker.id] = {docker: docker, offset:off};
                                                }
                                                /*else if (l == 1 && edges[k].dockers.length>2 && edges[k].dockers[l-1].isDocked()){
                                                        var dockedLane = this.getNextLane(edges[k].dockers[l-1].getDockedShape());
                                                        if (dockedLane != lanes[i])
                                                                continue;
                                                        dockers[docker.id] = {docker: docker, offset:offset};
                                                }
                                                // Check if the next dockers docked shape is from this lane
                                                else if (l == edges[k].dockers.length-2 && edges[k].dockers.length>2 && edges[k].dockers[l+1].isDocked()){
                                                        var dockedLane = this.getNextLane(edges[k].dockers[l+1].getDockedShape());
                                                        if (dockedLane != lanes[i])
                                                                continue;
                                                        dockers[docker.id] = {docker: docker, offset:offset};
                                                }
                                                                                               
                                                else if (isOutSidePool) {
                                                        dockers[docker.id] = {docker: docker, offset:this.getOffset(lanes[i], true, pool)};
                                                }*/
                                               
                                       
                                        }
                                }
                                               
                        }
                }
               
                // Move the moved children
                var MoveChildCommand = ORYX.Core.Command.extend({
                        construct: function(state){
                                this.state = state;
                        },
                        execute: function(){
                                if (this.executed){
                                        this.state.each(function(s){
                                                s.shape.bounds.moveBy(s.xOffset, 0);
                                        });
                                }
                                this.executed = true;
                        },
                        rollback: function(){
                                this.state.each(function(s){
                                        s.shape.bounds.moveBy(-s.xOffset, 0);
                                });
                        }
                })
               
               
                // Set dockers
                this.facade.executeCommands([new ORYX.Core.MoveDockersCommand(dockers), new MoveChildCommand(movedShapes)]);

        },
       
        moveBy: function(pos, offset){
                pos.x += offset.x;
                pos.y += offset.y;
                return pos;
        },
       
        getHashedBounds: function(shape){
                return this.currentPool && this.hashedBounds[this.currentPool.id][shape.id] ? this.hashedBounds[this.currentPool.id][shape.id] : shape.absoluteBounds();
        },
       
        /**
         * Returns a set on all child lanes for the given Shape. If recursive is TRUE, also indirect children will be returned (default is FALSE)
         * The set is sorted with first child the lowest y-coordinate and the last one the highest.
         * @param {ORYX.Core.Shape} shape
         * @param {boolean} recursive
         */
        getLanes: function(shape, recursive){
                var namespace = this.getNamespace();
            
                // Get all the child lanes
                var lanes = shape.getChildNodes(recursive||false).findAll(function(node) { return (node.getStencil().id() === namespace + "Lane"); });
               
                // Sort all lanes by there y coordinate
                lanes = lanes.sort(function(a, b){
                       
                                        // Get y coordinates for upper left and lower right
                                        var auy = Math.round(a.bounds.upperLeft().y);
                                        var buy = Math.round(b.bounds.upperLeft().y);
                                        var aly = Math.round(a.bounds.lowerRight().y);
                                        var bly = Math.round(b.bounds.lowerRight().y);
                                       
                                        var ha  = this.getHashedBounds(a);
                                        var hb  = this.getHashedBounds(b);
                                       
                                        // Get the old y coordinates
                                        var oauy = Math.round(ha.upperLeft().y);
                                        var obuy = Math.round(hb.upperLeft().y);
                                        var oaly = Math.round(ha.lowerRight().y);
                                        var obly = Math.round(hb.lowerRight().y);
                                       
                                        // If equal, than use the old one
                                        if (auy == buy && aly == bly) {
                                                auy = oauy; buy = obuy; aly = oaly; bly = obly;
                                        }
                                       
                                        if (Math.round(a.bounds.height()-ha.height()) === 0 && Math.round(b.bounds.height()-hb.height()) === 0){
                                                return auy < buy ? -1 : (auy > buy ? 1: 0);
                                        }
                                       
                                        // Check if upper left and lower right is completely above/below
                                        var above = auy < buy && aly < bly;
                                        var below = auy > buy && aly > bly;
                                        // Check if a is above b including the old values
                                        var slightlyAboveBottom = auy < buy && aly >= bly && oaly < obly;
                                        var slightlyAboveTop = auy >= buy && aly < bly && oauy < obuy;
                                        // Check if a is below b including the old values
                                        var slightlyBelowBottom = auy > buy && aly <= bly && oaly > obly;
                                        var slightlyBelowTop = auy <= buy && aly > bly && oauy > obuy;
                                       
                                        // Return -1 if a is above b, 1 if b is above a, or 0 otherwise
                                        return  (above || slightlyAboveBottom || slightlyAboveTop ? -1 : (below || slightlyBelowBottom || slightlyBelowTop ? 1 : 0))
                                }.bind(this));
                               
                // Return lanes
                return lanes;
        },
                       
        getNamespace: function() {
                if(!this.namespace) {
                        var stencilsets = this.facade.getStencilSets();
                        if(stencilsets.keys()) {
                                this.namespace = stencilsets.keys()[0];
                        } else {
                                return undefined;
                        }
                }
                return this.namespace;
        }
	};
	
	var ResizeLanesCommand = ORYX.Core.Command.extend({

        construct: function(shape, parent, pool, plugin) {
       
                this.facade  = plugin.facade;
                this.plugin  = plugin;
                this.shape       = shape;
                this.changes;
               
                this.pool       = pool;
               
                this.parent     = parent;
               
               
                this.shapeChildren = [];
               
                /*
                 * The Bounds have to be stored
                 * separate because they would
                 * otherwise also be influenced
                 */
                this.shape.getChildShapes().each(function(childShape) {
                        this.shapeChildren.push({
                                shape: childShape,
                                bounds: {
                                        a: {
                                                x: childShape.bounds.a.x,
                                                y: childShape.bounds.a.y
                                        },
                                        b: {
                                                x: childShape.bounds.b.x,
                                                y: childShape.bounds.b.y
                                        }
                                }
                        });
                }.bind(this));

                this.shapeUpperLeft = this.shape.bounds.upperLeft();
               
                // If there is no parent,
                // correct the abs position with the parents abs.
                /*if (!this.shape.parent) {
                        var pAbs = parent.absoluteXY();
                        this.shapeUpperLeft.x += pAbs.x;
                        this.shapeUpperLeft.y += pAbs.y;
                }*/
                this.parentHeight       = this.parent.bounds.height();

        },
       
        getLeafLanes: function(lane){
                var childLanes = this.plugin.getLanes(lane).map(function(child){
                        return this.getLeafLanes(child);
                }.bind(this)).flatten();
                return childLanes.length > 0 ? childLanes : [lane];
        },
       
        findNewLane: function(){
               
                var lanes = this.plugin.getLanes(this.parent);

                var leafLanes = this.getLeafLanes(this.parent);
                /*leafLanes = leafLanes.sort(function(a,b){
                        var aupl = a.absoluteXY().y;
                        var bupl = b.absoluteXY().y;
                        return aupl < bupl ? -1 : (aupl > bupl ? 1 : 0)
                })*/
                this.lane = leafLanes.find(function(l){ return l.bounds.upperLeft().y >= this.shapeUpperLeft.y }.bind(this)) || leafLanes.last();
                this.laneUpperLeft = this.lane.bounds.upperLeft();      
        },
       
        execute: function() {
               
                if(this.changes) {
                        this.executeAgain();
                        return;
                }

                /*
                 * Rescue all ChildShapes of the deleted
                 * Shape into the lane that takes its
                 * place
                 */
               
                if (!this.lane){
                        this.findNewLane();
                }
               
                if(this.lane) {                
                       
                        var laUpL = this.laneUpperLeft;
                        var shUpL = this.shapeUpperLeft;
                       
                        var depthChange = this.plugin.getDepth(this.lane, this.parent)-1;
                                               
                        this.changes = $H({});
                       
                        // Selected lane is BELOW the removed lane
                        if (laUpL.y >= shUpL.y) {                              
                                this.lane.getChildShapes().each(function(childShape) {
                                       
                                        /*
                                         * Cache the changes for rollback
                                         */
                                        if(!this.changes[childShape.getId()]) {
                                                this.changes[childShape.getId()] = this.computeChanges(childShape, this.lane, this.lane, this.shape.bounds.height());
                                        }
                                       
                                        childShape.bounds.moveBy(0, this.shape.bounds.height());
                                }.bind(this));
                               
                                this.plugin.hashChildShapes(this.lane);
                               
                                this.shapeChildren.each(function(shapeChild) {
                                        shapeChild.shape.bounds.set(shapeChild.bounds);
                                        shapeChild.shape.bounds.moveBy((shUpL.x-30)-(depthChange*30), 0);
                                       
                                        /*
                                         * Cache the changes for rollback
                                         */
                                        if(!this.changes[shapeChild.shape.getId()]) {
                                                this.changes[shapeChild.shape.getId()] = this.computeChanges(shapeChild.shape, this.shape, this.lane, 0);
                                        }
                                       
                                        this.lane.add(shapeChild.shape);
                                       
                                }.bind(this));          
                       
                                this.lane.bounds.moveBy(0, shUpL.y-laUpL.y);
                       
                        // Selected lane is ABOVE the removed lane      
                        } else if(shUpL.y > laUpL.y){
                               
                                this.shapeChildren.each(function(shapeChild) {
                                        shapeChild.shape.bounds.set(shapeChild.bounds);        
                                        shapeChild.shape.bounds.moveBy((shUpL.x-30)-(depthChange*30), this.lane.bounds.height());                      
                                       
                                        /*
                                         * Cache the changes for rollback
                                         */
                                        if(!this.changes[shapeChild.shape.getId()]) {
                                                this.changes[shapeChild.shape.getId()] = this.computeChanges(shapeChild.shape, this.shape, this.lane, 0);
                                        }
                                       
                                        this.lane.add(shapeChild.shape);
                                       
                                }.bind(this));
                        }
                       
                       

                       
                }
                               
                /*
                 * Adjust the height of the lanes
                 */
                // Get the height values
                var oldHeight   = this.lane.bounds.height();                            
                var newHeight   = this.lane.length === 1 ? this.parentHeight : this.lane.bounds.height() + this.shape.bounds.height();

                // Set height
                this.setHeight(newHeight, oldHeight, this.parent, this.parentHeight, true);
               
                // Cache all sibling lanes
                //this.changes[this.shape.getId()] = this.computeChanges(this.shape, this.parent, this.parent, 0);
                this.plugin.getLanes(this.parent).each(function(childLane){
                        if(!this.changes[childLane.getId()] && childLane !== this.lane && childLane !== this.shape) {
                                this.changes[childLane.getId()] = this.computeChanges(childLane, this.parent, this.parent, 0);
                        }
                }.bind(this))
                       
                // Update
                this.update();
        },
       
        setHeight: function(newHeight, oldHeight, parent, parentHeight, store){
               
                // Set heigh of the lane
                this.plugin.setDimensions(this.lane, this.lane.bounds.width(), newHeight);
                this.plugin.hashedBounds[this.pool.id][this.lane.id] = this.lane.absoluteBounds();
               
                // Adjust child lanes
                this.plugin.adjustHeight(this.plugin.getLanes(parent), this.lane);
               
                if (store === true){
                        // Store changes
                        this.changes[this.shape.getId()] = this.computeChanges(this.shape, parent, parent, 0, oldHeight, newHeight);    
                }
               
                // Set parents height
                this.plugin.setDimensions(parent, parent.bounds.width(), parentHeight);
               
                if (parent !== this.pool){
                        this.plugin.setDimensions(this.pool, this.pool.bounds.width(), this.pool.bounds.height() + (newHeight-oldHeight));
                }
        },
       
        update: function(){
               
                // Hack to prevent the updating of the dockers
                this.plugin.hashedBounds[this.pool.id]["REMOVED"] = true;
                // Update
                //this.facade.getCanvas().update();
        },
       
        rollback: function() {
               
                var laUpL = this.laneUpperLeft;
                var shUpL = this.shapeUpperLeft;
                       
                this.changes.each(function(pair) {
                       
                        var parent                      = pair.value.oldParent;
                        var shape                       = pair.value.shape;
                        var parentHeight        = pair.value.parentHeight;
                        var oldHeight           = pair.value.oldHeight;
                        var newHeight           = pair.value.newHeight;
                       
                        // Move siblings
                        if (shape.getStencil().id().endsWith("Lane")){
                                shape.bounds.moveTo(pair.value.oldPosition);    
                        }
                       
                        // If lane
                        if(oldHeight) {                                
                                this.setHeight(oldHeight, newHeight, parent, parent.bounds.height() + (oldHeight - newHeight));
                                if (laUpL.y >= shUpL.y) {
                                        this.lane.bounds.moveBy(0, this.shape.bounds.height()-1);
                                }
                        } else {
                                parent.add(shape);
                                shape.bounds.moveTo(pair.value.oldPosition);
                               
                        }

                       
                }.bind(this));
               
                // Update
                //this.update();
               
        },
       
        executeAgain: function() {
               
                this.changes.each(function(pair) {
                        var parent        = pair.value.newParent;
                        var shape         = pair.value.shape;
                        var newHeight = pair.value.newHeight;
                        var oldHeight = pair.value.oldHeight;
                       
                        // If lane
                        if(newHeight) {
                                var laUpL = this.laneUpperLeft.y;
                                var shUpL = this.shapeUpperLeft.y;
                       
                                if (laUpL >= shUpL) {
                                        this.lane.bounds.moveBy(0, shUpL - laUpL);
                                }
                                this.setHeight(newHeight, oldHeight, parent, parent.bounds.height() + (newHeight-oldHeight));
                        } else {
                                parent.add(shape);
                                shape.bounds.moveTo(pair.value.newPosition);
                        }
                       
                }.bind(this));
               
                // Update
                this.update();
        },
       
        computeChanges: function(shape, oldParent, parent, yOffset, oldHeight, newHeight) {
               
                oldParent = this.changes[shape.getId()] ? this.changes[shape.getId()].oldParent : oldParent;
                var oldPosition = this.changes[shape.getId()] ? this.changes[shape.getId()].oldPosition : shape.bounds.upperLeft();
               
                var sUl = shape.bounds.upperLeft();
               
                var pos = {x: sUl.x, y: sUl.y + yOffset};
               
                var changes = {
                        shape           : shape,
                        parentHeight: oldParent.bounds.height(),
                        oldParent       : oldParent,
                        oldPosition     : oldPosition,
                        oldHeight       : oldHeight,
                        newParent       : parent,
                        newPosition : pos,
                        newHeight       : newHeight
                };
                       
                return changes;
        }
       
	});

		
	ORYX.Plugins.BPMN2_0 = ORYX.Plugins.AbstractPlugin.extend(ORYX.Plugins.BPMN2_0);
	
}()	