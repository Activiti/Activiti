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
