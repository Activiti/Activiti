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
};