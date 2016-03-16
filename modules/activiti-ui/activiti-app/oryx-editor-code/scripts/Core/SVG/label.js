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
		"wDGMOQ√ñ#+=<>~^",
		"ABCHKNRSUVXZ√ú√Ñ&",
		"bdghnopqux√∂√ºETY1234567890√ü_¬ß${}*¬¥`¬µ‚Ç¨",
		"aeksvyz√§FLP?¬∞¬≤¬≥",
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
//test string: abcdefghijklmnopqrstuvwxyz√∂√§√º,.-#+ 1234567890√üABCDEFGHIJKLMNOPQRSTUVWXYZ;:_'*√ú√Ñ√ñ!"¬ß$%&/()=?[]{}|<>'~¬¥`\^¬∞¬µ@‚Ç¨¬≤¬≥
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
 });