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
		ORYX.Core.StencilSet._rulesByEditorInstance[editorId] = new ORYX.Core.StencilSet.Rules();;
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
ORYX.Core.StencilSet.loadStencilSet = function(url, editorId) {
	var stencilSet = ORYX.Core.StencilSet._stencilSetsByUrl[url];

	if(!stencilSet) {
		//load stencil set
		stencilSet = new ORYX.Core.StencilSet.StencilSet(url);
		
		//store stencil set
		ORYX.Core.StencilSet._stencilSetsByNamespace[stencilSet.namespace()] = stencilSet;
		
		//store stencil set by url
		ORYX.Core.StencilSet._stencilSetsByUrl[url] = stencilSet;
	}
	
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
