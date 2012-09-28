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
});