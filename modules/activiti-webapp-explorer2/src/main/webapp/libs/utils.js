/**
 *  utils.js 
 *  (c) 2009 Signavio GmbH
 *   
 *  @author Willi Tscheschner
 *   
 *  Utils is a shared javascript file which provides common 
 *  functionalities for all applications in the Signavio Process Editor
 *  
 */

// define namespace
if(!Signavio){ var Signavio = {} };
if(!Signavio.Utils){  Signavio.Utils = {} };


new function(){
	
	/**
	 * Splits the text by " " and \n and adds those
	 * strings to the resulting set of the tokenization
	 * @param {String} text The text which should be tokenized 
	 */
	Signavio.Utils.tokenize = function(text) {
	    var token = text.split(" ");
	    token = token.map(function(t, i){ return i == 0 ? t : [" ", t]}).flatten();
	    token = token.map(function(t){
	        var tn= t.split("\n");
	        return tn.map(function(tt, i){ return i == 0 ? tt : ["\n", tt]}).flatten();
	    }).flatten();
	    return token.findAll(function(t){ return t});
	}

	/**
	 * Strips all none letter and digits
	 * out of the string
	 * 
	 * @param {String} phrase
	 * @return {String}
	 */
	Signavio.Utils.strip = function(phrase){
		return phrase.toLowerCase()
					.replace(/[^a-zäöüß0-9]/g, '')
					.replace(/ü/g, 'u')
					.replace(/ä/g, 'a')
					.replace(/ö/g, 'o')
	}

	/**
	 * Returns true if a is in b included or other way around.
	 * 
	 * @param {String} a
	 * @param {String} b
	 * @return {boolean}
	 */
	Signavio.Utils.isEqualTerms = function(a, b){
		
		// Check if initial values are equal
		if (a && b && (a.include(b) || b.include(a))){
			return true;
		}
		
		// Strip
		a = this.strip(a);
		b = this.strip(b);
		
		// Check if included
		return a && b && (a.include(b) || b.include(a));
	}

	/**
	 * Replace all common stings from string1 with those
	 * which are in the string2
	 * 
	 * @param {String} string1
	 * @param {String} string2
	 * @param {int} cursor (Optional)
	 */
	Signavio.Utils.replaceCommonSubString = function(string1, string2, cursor) {
		
		// If string1 and string2 includes each other after stripingv
		var s1 = this.strip(string1);
		var s2 = this.strip(string2);
		if(s1 === s2 || s2.include(s1)){
			return string2;
		}
		
		// Get cursor pos
		var pos = cursor === undefined ? string1.length : cursor;
		// Tokenize
		var textT = this.tokenize(string1);
		var token = string2.split(/\s+/g); // Replace all special chars and split by " " 

		// Get index of the token where the cursor is
		var selectedIndex = textT.indexOf(textT.find(function(t){ pos -= t == "\n" ? 0 : (t == " " ? 1 : t.length+1) ; return pos <= 0}));
		selectedIndex = Math.max(selectedIndex , 0);
		var rounds = textT.length;
		var found = false;

		// Go from the selected index to 0 and then up from the length of the textT to the selected index, but only one round
		for (var i=selectedIndex; i != selectedIndex+1 && rounds >= 0;  i == 0 ? i = textT.length -1 : --i, --rounds ){
			
			
			// If current text phrase is empty, go furhter
			if (textT[i] == " " || textT[i] == "\n") { continue }
			
			// Find the token from the suggestion
			var tok = token.find(function(t){ return this.isEqualTerms(t, textT[i]) }.bind(this));
			
			// If there is a suggested phrases
			if (tok) {
				found = true;
				// Partition all words to word which occur before and after this word
				var front = token.slice(0, token.indexOf(tok)).reverse();
				var back = token.slice(token.indexOf(tok)+1);
				// Replace current phrase with the word
				textT[i] = tok;

                var lastKnownIndex = i;
                
                // Go through every word which occurs before
				for (var j=i; j>=0; --j){
                    if (textT[j] == " " || textT[j] == "\n") { continue }
                    tok = front.find(function(t){ return this.isEqualTerms(t, textT[j]) }.bind(this));

					if (tok) {
						var index = front.indexOf(tok);
						textT[lastKnownIndex] = front.slice(0,index).reverse().join(" ") +(index >0?" ":"")+ textT[lastKnownIndex];
						
						lastKnownIndex = j;
						textT[j] = tok;
						front= front.slice(index+1);
					}
				}

                if (front.length >0){
				    textT[lastKnownIndex] = front.reverse().join(" ") + " " + textT[lastKnownIndex];
                }

				var lastKnownIndex = i;

                // Go through every word which occurs after
				for (var j=i; j<textT.length; j++){
                    if (textT[j] == " " || textT[j] == "\n") { continue }
					tok = back.find(function(t){ return this.isEqualTerms(t, textT[j]) }.bind(this));
					if (tok) {
						var index = back.indexOf(tok);
						textT[lastKnownIndex] += (index>0?" ":"")+back.slice(0, index).join(" ");
						
						lastKnownIndex = j;
						textT[j] = tok;
						back = back.slice(index+1);
					}
				}
				
                if (back.length >0){
				    textT[lastKnownIndex] += " "+back.join(" ");
                }
				break;
			}
			if (selectedIndex===0&&textT.length===1){break;}
		}

		var nString1 = textT.join("");

        if (nString1 == string1 && !found) {
            nString1 += (nString1.endsWith(" ") ? "" : " ") + string2;
        }
        return nString1;
	}
	
	
	/**
	 * Returns a string which is 
	 * HTML unescaped.
	 * 
	 * @param {String} str
	 * @return {String}
	 */
	Signavio.Utils.unescapeHTML = function(str){
		
		str = str || "";
		
		var d = document.createElement("div");
		try {
		    d.innerHTML = str;
		} catch (e){
		    d.textContent = str;
		}
		var nstr =  d.textContent || d.innerText || "";
		
		// Unescape the unecaped string till no changes are there
		return nstr && str && nstr !== str ? Signavio.Utils.unescapeHTML(nstr) : nstr;
	}

	/**
	 * Returns a string which is 
	 * HTML escaped.
	 * 
	 * @param {String} str
	 * @return {String}
	 */	
	Signavio.Utils.escapeHTML = function(str){
		
		str = str || "";
		var d = document.createElement("div");
		try {
		    d.innerHTML = str;
		} catch (e){
			if (!!str.match(/&[aAoOuU]uml;/g)||!!str.match(/&szlig;/g)){
				$H({
				    "ä" : "&auml;",
				    "Ä" : "&Auml;",
				    "ö" : "&ouml;",
				    "Ö" : "&Ouml;",
				    "ü" : "&uuml;",
				    "Ü" : "&Uuml;",
					"ß" : "&szlig;"
				}).each(function(map){
				    str = str.gsub(map.value, map.key)
				})
			}
			try {
				d.innerHTML = str;
			} catch (ee) {
	 		   d.textContent = str;
			}
		}
		return d.innerHTML;
	}
	
	
	/**
	 * Return the value in the record 
	 * for a given query. 
	 * @param {Object} record Record
	 * @param {Object} query Query to extract the value (e.g. "rep.title")
	 */
	Signavio.Utils.extractValue = function(record, query){
		if( !record || !query ){ return null }
			
		// Split data field
		var o = query.split(".");
		
		// Get value
		var val = record instanceof Ext.data.Record ? record.get(o[0]) : record[o[0]];
		var i	= 0;
		
		// Iterate over value since there is no 
		// value of the end is reached
		while( val && ++i < o.length ){
			val = val[o[i]];
		}
		
		return typeof val == "string" ? val.unescapeHTML() : val;
	}
	

	/*** SPECIFIC BROWSER FIXES ***/
	
	/**
	 * Chrome Fixes
	 */
	if (Ext.isChrome){
		(function(){
			
			// Fixes for Chrome Bug
			// http://code.google.com/p/chromium/issues/detail?id=58493
			
			// Check if there exists the bug in the current chrome version
//			var parseNode = (new DOMParser()).parseFromString("<div ext:qtip='tooltip'></div>", "text/xml");
//			if (parseNode.getElementsByTagName("parsererror").length == 0){
//				return;
//			}
			
			// @overwrite
			var inHtml = Ext.DomHelper.insertHtml;
			Ext.DomHelper.insertHtml = function(foo, bar, html){
				html = html.gsub("ext:qtip=", "title="); 
				html = html.gsub("ext:tree-node-id=", "tree-node-id=");
				return inHtml.call(this, foo, bar, html);
			};
			
			// Remove namespace awareness of node ids
			Ext.tree.TreeEventModel.prototype.getNode = function(e){
		        var t;
		        if(t = e.getTarget('.x-tree-node-el', 10)){
		            var id = Ext.fly(t, '_treeEvents').dom.getAttributeNS(null, 'tree-node-id');
		            if(id){
		                return this.tree.getNodeById(id);
		            }
		        }
		        return null;
		    };
		    
		    // Fix use of ext namespaces
		    Ext.Template.prototype.overwrite = function(el, values, returnElement){
		        el = Ext.getDom(el);
		        el.innerHTML = this.applyTemplate(values).gsub(" ext:qtip=", " title=");
		        return returnElement ? Ext.get(el.firstChild, true) : el.firstChild;
		    };
			
		}());
	}
	
}()
