/**
 * @class Ext.ux.ColorField
 * @extends Ext.form.TriggerField
 * Provides a color input field with a {@link Ext.ColorPalette} dropdown.
* @constructor
* Create a new ColorField
 * <br />Example:
 * <pre><code>
var color_field = new Ext.ux.ColorField({
	fieldLabel: 'Color',
	id: 'color',
	width: 175,
	allowBlank: false
});
</code></pre>
* @param {Object} config
 */

Ext.ux.ColorField = Ext.extend(Ext.form.TriggerField,  {
    /**
     * @cfg {String} invalidText
     * The error to display when the color in the field is invalid (defaults to
     * '{value} is not a valid color - it must be in the format {format}').
     */
    invalidText : "'{0}' is not a valid color - it must be in a the hex format (# followed by 3 or 6 letters/numbers A-F/0-9), in a rgb format (like 'rgb(255, 255, 255)'), or as a normal color name (e.g. 'white')",
    /**
     * @cfg {String} triggerClass
     * An additional CSS class used to style the trigger button.  The trigger will always get the
     * class 'x-form-trigger' and triggerClass will be <b>appended</b> if specified (defaults to 'x-form-color-trigger'
     * which displays a color wheel icon).
     */
    triggerClass : 'x-form-color-trigger',
    /**
     * @cfg {String/Object} autoCreate
     * A DomHelper element spec, or true for a default element spec (defaults to
     * {tag: "input", type: "text", size: "10", autocomplete: "off"})
     */

    // private
    defaultAutoCreate : {tag: "input", type: "text", size: "10", autocomplete: "off"},

	colorMap: {
		aliceblue: 'f0f8ff',
		antiquewhite: 'faebd7',
		aqua: '00ffff',
		aquamarine: '7fffd4',
		azure: 'f0ffff',
		beige: 'f5f5dc',
		bisque: 'ffe4c4',
		black: '000000',
		schwarz: '000000',
		blanchedalmond: 'ffebcd',
		blue: '0000ff',
		blau: '0000ff',
		blueviolet: '8a2be2',
		blauviolet: '8a2be2',
		brown: 'a52a2a',
		braun: 'a52a2a',
		burlywood: 'deb887',
		cadetblue: '5f9ea0',
		chartreuse: '7fff00',
		chocolate: 'd2691e',
		schokolade: 'd2691e',
		coral: 'ff7f50',
		cornflowerblue: '6495ed',
		cornsilk: 'fff8dc',
		crimson: 'dc143c',
		cyan: '00ffff',
		darkblue: '00008b',
		dunkelblau: '00008b',
		darkcyan: '008b8b',
		darkgoldenrod: 'b8860b',
		darkgray: 'a9a9a9',
		dunkelgrau: 'a9a9a9',
		darkgreen: '006400',
		'dunkelgrün': '006400',
		darkkhaki: 'bdb76b',
		darkmagenta: '8b008b',
		darkolivegreen: '556b2f',
		'dunkelolivegrün': '556b2f',
		darkorange: 'ff8c00',
		dunkelorange: 'ff8c00',
		darkorchid: '9932cc',
		darkred: '8b0000',
		dunkelrot: '8b0000',
		darksalmon: 'e9967a',
		darkseagreen: '8fbc8f',
		darkslateblue: '483d8b',
		darkslategray: '2f4f4f',
		darkturquoise: '00ced1',
		darkviolet: '9400d3',
		deeppink: 'ff1493',
		deepskyblue: '00bfff',
		dimgray: '696969',
		dodgerblue: '1e90ff',
		feldspar: 'd19275',
		firebrick: 'b22222',
		floralwhite: 'fffaf0',
		forestgreen: '228b22',
		fuchsia: 'ff00ff',
		gainsboro: 'dcdcdc',
		ghostwhite: 'f8f8ff',
		gold: 'ffd700',
		goldenrod: 'daa520',
		goldrot: 'daa520',
		gray: '808080',
		grau: '808080',
		green: '008000',
		'grün': '008000',
		greenyellow: 'adff2f',
		'grüngelb': 'adff2f',
		honeydew: 'f0fff0',
		hotpink: 'ff69b4',
		indianred: 'cd5c5c',
		indigo: '4b0082',
		ivory: 'fffff0',
		khaki: 'f0e68c',
		lavender: 'e6e6fa',
		lavenderblush: 'fff0f5',
		lawngreen: '7cfc00',
		lemonchiffon: 'fffacd',
		lightblue: 'add8e6',
		leichtblau: 'add8e6',
		lightcoral: 'f08080',
		lightcyan: 'e0ffff',
		lightgoldenrodyellow: 'fafad2',
		lightgrey: 'd3d3d3',
		leichtgrau: 'd3d3d3',
		lightgreen: '90ee90',
		'leichtgrün': '90ee90',
		lightpink: 'ffb6c1',
		lightsalmon: 'ffa07a',
		lightseagreen: '20b2aa',
		lightskyblue: '87cefa',
		lightslateblue: '8470ff',
		lightslategray: '778899',
		lightsteelblue: 'b0c4de',
		lightyellow: 'ffffe0',
		leichtgelb: 'ffffe0',
		lime: '00ff00',
		limone: '00ff00',
		limegreen: '32cd32',
		'limonengrün': '32cd32',
		linen: 'faf0e6',
		magenta: 'ff00ff',
		maroon: '800000',
		mediumaquamarine: '66cdaa',
		mediumblue: '0000cd',
		mittelblau: '0000cd',
		mediumorchid: 'ba55d3',
		mediumpurple: '9370d8',
		mediumseagreen: '3cb371',
		mediumslateblue: '7b68ee',
		mediumspringgreen: '00fa9a',
		mediumturquoise: '48d1cc',
		mediumvioletred: 'c71585',
		midnightblue: '191970',
		mintcream: 'f5fffa',
		mistyrose: 'ffe4e1',
		moccasin: 'ffe4b5',
		navajowhite: 'ffdead',
		navy: '000080',
		oldlace: 'fdf5e6',
		olive: '808000',
		olivedrab: '6b8e23',
		orange: 'ffa500',
		orangered: 'ff4500',
		orangerot: 'ff4500',
		orchid: 'da70d6',
		palegoldenrod: 'eee8aa',
		palegreen: '98fb98',
		paleturquoise: 'afeeee',
		palevioletred: 'd87093',
		papayawhip: 'ffefd5',
		peachpuff: 'ffdab9',
		peru: 'cd853f',
		pink: 'ffc0cb',
		plum: 'dda0dd',
		powderblue: 'b0e0e6',
		purple: '800080',
		red: 'ff0000',
		rot: 'ff0000',
		rosybrown: 'bc8f8f',
		royalblue: '4169e1',
		saddlebrown: '8b4513',
		salmon: 'fa8072',
		sandybrown: 'f4a460',
		seagreen: '2e8b57',
		seashell: 'fff5ee',
		sienna: 'a0522d',
		silver: 'c0c0c0',
		silber: 'c0c0c0',
		skyblue: '87ceeb',
		himmelblau: '87ceeb',
		slateblue: '6a5acd',
		slategray: '708090',
		snow: 'fffafa',
		schnee: 'fffafa',
		springgreen: '00ff7f',
		steelblue: '4682b4',
		tan: 'd2b48c',
		teal: '008080',
		thistle: 'd8bfd8',
		tomato: 'ff6347',
		tomate: 'ff6347',
		turquoise: '40e0d0',
		violet: 'ee82ee',
		violetred: 'd02090',
		wheat: 'f5deb3',
		white: 'ffffff',
		'weiß': 'ffffff',
		whitesmoke: 'f5f5f5',
		yellow: 'ffff00',
		gelb: 'ffff00',
		yellowgreen: '9acd32',
		'gelbgrün': '9acd32',
		'taskgelb': '#FFFFCC',
		signavio:'af356f'
	},

    // Limit input to hex values
	//maskRe: /[#a-f0-9]/i,
	
    // private
    validateValue : function(value){
        if(!Ext.ux.ColorField.superclass.validateValue.call(this, value)){
            return false;
        }
        if(value.length < 1){ // if it's blank and textfield didn't flag it then it's valid
        	 this.setColor('');
        	 return true;
        }

        var parseOK = this.validateColor(value);

        if(!value || (parseOK == false)){
            this.markInvalid(String.format(this.invalidText,value));
            return false;
        }
		this.setColor(this.formatColor(value));
        return true;
    },

	/**
	 * Sets the current color and changes the background.
	 * Does *not* change the value of the field.
	 * @param {String} hex The color value.
	 */
	setColor : function(color) {
		if (color=='' || color==undefined)
		{
			if (this.emptyText!='' && this.validateColor(this.emptyText))
				color=this.emptyText;
			else
				color='';
		}
		if (this.trigger)
			this.trigger.setStyle( {
				'background-color': color
			});
		else
		{
			this.on('render',function(){this.setColor(color)},this);
		}
	},
	
    // private
    // Provides logic to override the default TriggerField.validateBlur which just returns true
    validateBlur : function(){
        return !this.menu || !this.menu.isVisible();
    },

    /**
     * Returns the current value of the color field
     * @return {String} value The color value
     */
    getValue : function(){
        var value = Ext.ux.ColorField.superclass.getValue.call(this) || "";
		value = value.replace(/[\s]/g, "").toLowerCase();
		if (value && this.isColor(value)){
			return this.formatColor(value)	
		}
		return "";
    },

    /**
     * Sets the value of the color field.  You can pass a string that can be parsed into a valid HTML color
     * <br />Usage:
     * <pre><code>
		colorField.setValue('#FFFFFF');
       </code></pre>
     * @param {String} color The color string
     */
    setValue : function(color){
		color = this.formatColor(color);
        Ext.ux.ColorField.superclass.setValue.call(this, color);
		this.setColor(color);
    },
	
	isRGB: function(value){
		return value.replace(/(rgb|[()])/g, "").split(",").findAll(function(r){ var p = parseInt(r); return p >= 0 && p <= 255 && String(p) === r }).length === 3;
	},
	
	isHEX: function(value){
		value = value.startsWith("#") ? value.slice(1) : value;
		return !value.replace(/[a-fA-F0-9]+/g, "") && (value.length === 6 || value.length === 3);
	},
	
	isSimpleColor: function(value){
		return !!this.colorMap[value];
	},
	
	isColor: function(value){
		return this.isRGB(value) || this.isHEX(value) || this.isSimpleColor(value);
	},

    // private
    validateColor : function(value){
		if (value && typeof value == "string"){
			// Replace whitespaces
			value = value.replace(/[\s]/g, "").toLowerCase();
			if (this.isColor(value)){
				return true;
			} 
		}
		return false;
    },

    // private
    formatColor : function(value){		
		
		value = value.replace(/[\s]/g, "").toLowerCase();
		
		if (this.isColor(value)){
			if (this.isSimpleColor(value)){
				value = this.colorMap[value];
			} else if (this.isRGB(value)){
				value = value.split(/[^0-9]+/i)
						.slice(1,4)		// Extract to RGB
						.map(function(r){ 
							var hex = parseInt(r).toString(16);
							return (hex.length == 1 ? "0" : "") + hex;
						})				// RGB to HEX 
						.join("");	 	// Concat
			}
			value = value.startsWith("#") ? value : "#" + value;
			value = value.toUpperCase();
		} 
		return value;
    },

    // private
    menuListeners : {
        select: function(e, c){
            this.setValue(c);
			this.fireEvent("select", this, c)
        },
        show : function(){ // retain focus styling
            this.onFocus();
        },
        hide : function(){
            this.focus.defer(10, this);
            var ml = this.menuListeners;
            this.menu.un("select", ml.select,  this);
            this.menu.un("show", ml.show,  this);
            this.menu.un("hide", ml.hide,  this);
        }
    },

    // private
    // Implements the default empty TriggerField.onTriggerClick function to display the ColorPalette
    onTriggerClick : function(){
        if(this.disabled){
            return;
        }
        if(this.menu == null){
            this.menu = new Ext.menu.ColorMenu();
        }

        this.menu.on(Ext.apply({}, this.menuListeners, {
            scope:this
        }));

        this.menu.show(this.el, "tl-bl?");
    },
	
	onDestroy : function(){
    	if(this.menu){
        	this.menu.destroy();
    	}
    	Ext.ux.ColorField.superclass.onDestroy.call(this);
	}
});

Ext.reg('colorfield',Ext.ux.ColorField);
