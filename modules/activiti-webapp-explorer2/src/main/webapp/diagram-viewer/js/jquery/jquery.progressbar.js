/*
 * @ Dmitry Farafonov
 */

(function($){
$.ProgressBar = function(options) {
	this.element = $(options.boundingBox);
	if (options.on && options.on.complete){
		this.onComplete = options.on.complete;
	}
	if (options.on && options.on.valueChange){
		this.onValueChange = options.on.valueChange;
	}
	
	this._create();
	
	if (options.label)
		this.set("label", options.label);
	if (options.value)
		this.value(options.value);
	if (options.max)
		this.set("max", options.max);
};
$.ProgressBar.prototype = {
	options: {
		value: 0,
		max: 100
	},

	min: 0,

	_create: function() {
		this.element
			.addClass( "ui-progressbar ui-widget ui-widget-content ui-corner-all" )
			.attr({
				role: "progressbar",
				"aria-valuemin": this.min,
				"aria-valuemax": this.options.max,
				"aria-valuenow": this._value()
			});

		this.valueDiv = $( "<div class='ui-progressbar-label'></div>" )
			.appendTo( this.element );
			
		this.valueDiv = $( "<div class='ui-progressbar-value ui-widget-header ui-corner-left'></div>" )
			.appendTo( this.element );

		this.oldValue = this._value();
		this._refreshValue();
	},

	_destroy: function() {
		this.element
			.removeClass( "ui-progressbar ui-widget ui-widget-content ui-corner-all" )
			.removeAttr( "role" )
			.removeAttr( "aria-valuemin" )
			.removeAttr( "aria-valuemax" )
			.removeAttr( "aria-valuenow" );

		this.valueDiv.remove();
	},

	value: function( newValue ) {
		if ( newValue === undefined ) {
			return this._value();
		}

		this._setOption( "value", newValue );
		return this;
	},

	_setOption: function( key, value ) {
		if ( key === "value" ) {
			//var oldVal = this.options.value;
			this.options.value = value;
			this._refreshValue();
			
			if (this.onValueChange)
				this.onValueChange.apply(this, [{oldVal: this.oldValue, newVal: value}]);
			
			if ( this._value() === this.options.max ) {
				//this._trigger( "complete" );
				if (this.onComplete)
					this.onComplete.apply(this);
			}
		} else if (key === "label") {
			$(this.element).find(".ui-progressbar-label").html(value);
		} else if (key === "max") {
			this.options.max = value;
		}

		//this._super( key, value );
	},

	_value: function() {
		var val = this.options.value;
		// normalize invalid value
		if ( typeof val !== "number" ) {
			val = 0;
		}
		return Math.min( this.options.max, Math.max( this.min, val ) );
	},

	_percentage: function() {
		return 100 * this._value() / this.options.max;
	},

	_refreshValue: function() {
		var value = this.value(),
			percentage = this._percentage();

		if ( this.oldValue !== value ) {
			this.oldValue = value;
			//this._trigger( "change" );
		}

		this.valueDiv
			.toggle( value > this.min )
			.toggleClass( "ui-corner-right", value === this.options.max )
			.width( percentage.toFixed(0) + "%" );
		this.element.attr( "aria-valuenow", value );
		
		//$(this.element).find(".ui-progressbar-label").html(value + "%");
	},
	
	set: function(key, value){
		this._setOption(key, value);
	}
};

})( jQuery );