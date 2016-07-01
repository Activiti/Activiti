/*
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/**
 *
 * Utility objects and methods for the Activiti Administrator app
 *
 */
ActivitiAdmin.Utils = {};

ActivitiAdmin.Utils.resetTimeToMorning = function(date) {
	date.setUTCHours(0);
	date.setUTCMinutes(0);
	date.setUTCSeconds(0);
	return date;
};

ActivitiAdmin.Utils.resetTimeToEvening = function(date) {
	date.setUTCHours(23);
	date.setUTCMinutes(59);
	date.setUTCSeconds(59);
	return date;
};

ActivitiAdmin.Utils.variableFilterTypes = [
   {name: "String", id: "string"},
   {name: "Integer", id: "integer"},
   {name: "Double", id: "double"},
   {name: "Long", id: "long"},
   {name: "Short", id: "short"},
   {name: "Boolean", id: "boolean"},
   {name: "Date", id: "date"}
];

ActivitiAdmin.Utils.variableFilterOperators = [
    {sign: "=", id: "equals", name: "Equals"},
    {sign: "<>", id: "notEquals", name: "Not equals"},
    {sign: "<", id: "lessThan", name: "Less than" },
    {sign: "<=", id: "lessThanOrEquals", name: "Less than or equals"},
    {sign: ">", id: "greaterThan", name: "Greater than" },
    {sign: ">=", id: "greaterThanOrEquals", name: "Greater than or equals" },
    {sign: "like", id: "like", name: "Like"}
];


/**
 * Activiti.Utils.Filter
 *
 * Generic list filter implementation.
 */
ActivitiAdmin.Utils.Filter = function(config, $http, $timeout, $rootScope) {
	this.init(config, $http, $timeout, $rootScope);
};

ActivitiAdmin.Utils.Filter.prototype = {

	init: function(config, $http, $timeout, $rootScope) {
		this.order = ActivitiAdmin.Config.filter.defaultOrder;
		this.resultSize = ActivitiAdmin.Config.filter.defaultResultSize;
		this.$http = $http;
		this.$timeout = $timeout;
		this.$rootScope = $rootScope;

		this.waiting = false;
		this.delayed = false;

		this.setConfig(config);
		// Initialize supported properties and defaults
		this.properties = {};
		this._initializeSupportedPropertiesFromConfig();

		this.loading = false;
		this.pendingReload = false;
	},

	setConfig: function(config) {
		this.config = config;
		this.options = config.options;

		if(config.sortObjects !== undefined && config.sortObjects.length > 0) {
			this.sortObjects = config.sortObjects;
			if(this.sort == undefined) {
				this.sort = config.sortObjects[0];
			}
		}
	},

	refresh: function() {
		// Clear the pending reload flag, if needed
		this.pendingReload = false;

		if(this.loading) {
			this.pendingReload = true;
		} else {
			this.loading = true;
			this.$http(this._getHttpParameters()).
	    		success(this._success.bind(this)).
	    		error(this._error.bind(this));
		}
	},

	refreshDelayed: function() {
		// If already waiting, another wait-cycle will be done
		// after the current wait is over
		if(this.waiting) {
			this.delayed = true;
		} else {
			this._scheduleDelayedRefresh();
		}
	},

	clear: function() {
		// Reset with initial values passed in commit
		this.properties = {};
		this._initializeSupportedPropertiesFromConfig();

        if (this.processDefinition) {
            delete this.processDefinition;
        }

		this.refresh();
	},

	_initializeSupportedPropertiesFromConfig: function() {
		this.supportedProperties = [];
		if(this.config.supportedProperties) {
			for(var i=0; i< this.config.supportedProperties.length; i++) {
				var prop = this.config.supportedProperties[i];
				if(prop.showByDefault === true) {
					var def = '';
					if(prop.defaultValue) {
						def = prop.defaultValue;
					}
					this.properties[prop.id] = def;
				} else {
					// Not shown by default, keep in supported properties array
					this.supportedProperties.push(prop);
				}
			}
		}
	},


	_scheduleDelayedRefresh: function() {
		this.waiting = true;

		this.$timeout((function() {
			this.waiting = false;
			if(this.delayed) {
				this.delayed = false;
				// Delay agian
				this._scheduleDelayedRefresh();
			} else {
				// Actually do the refresh-call
				this.refresh();
			}
		}).bind(this), ActivitiAdmin.Config.filter.delay);
	},

	_success: function(data, status, headers, config) {
		this.loading = false;
		if(this.config.success !== undefined) {
			this.config.success(data, status, headers, config);
		}

		// In case a refresh-request came in when the current request was still going on, refresh after finished
		if(this.pendingReload) {
			this.refresh();
		}
	},

	_error: function(data, status, headers, config) {
		this.loading = false;

		if (this.config.error !== undefined) {
			this.config.error(data, status, headers, config);
		}

		// In case a refresh-request came in when the current request was still going on, refresh after finished
		if (this.pendingReload) {
			this.refresh();
		}
	},

	setResultSize: function(resultSize) {
    	if(this.resultSize != resultSize) {
    		this.resultSize = resultSize;
    		this.refresh();
    	}
	},

	setSort: function(sortObject) {
		this.sort = sortObject;
		this.refresh();
	},

	toggleOrder: function() {
		if(this.order == 'asc') {
			this.order = 'desc';
		} else {
			this.order = 'asc';
		}
		this.refresh();
	},

	addProperty: function(prop, value) {
		if(prop !== undefined) {
			if(!value) {
				value = '';
			}
			this.properties[prop.id] = value;

			var indexToRemove = -1;
			// Remove the property from the supported-properties list
			for(var i=0; i<this.supportedProperties.length; i++) {
				var supported = this.supportedProperties[i];
				if(supported.id == prop.id) {
					indexToRemove = i;
					break;
				}
			}
			if(indexToRemove >= 0) {
				this.supportedProperties.splice(indexToRemove, 1);
			}

			// Focus the newly added field
			this.$timeout(function() {
				var formField = $('#filter-' + prop.id);
				formField.focus();
			}, 100);
		}
	},
	
	removeProperty: function(prop) {
	    if (prop !== undefined) {
	        delete this.properties[prop.id];
	    }
	},

	/**
	 * Return the label that corresponds to a value selected in a set of filter-options.
	 */
	getLabelForOption: function(optionType) {
		var options = this.options[optionType];
		if(options != undefined && options.length > 0) {
			for(var i=0; i<options.length; i++) {
				var option = options[i];
				if(this.properties[optionType] == option.value) {
					return option.name;
				}
			}
			return options[0].name;
		}
		return "???";
	},

	selectOption: function(optionType, option) {
		var currentValue = this.properties[optionType];
		if(currentValue != option.value) {
			this.properties[optionType] = option.value;
			this.refresh();
		}
	},

	/**
	 * Return an object that can be used in a $http request. It contains all parameters
	 * set in this filter including size, sort and order.
	 */
	_getHttpParameters: function() {
		var httpConfig = {};
		httpConfig.method = this.config.method,
		httpConfig.url = this.config.url;

		// Add size and sorting
		var params = {
			"size": this.resultSize,
			"order": this.order
		};
		if(this.sort !== undefined) {
			params.sort = this.sort.id;
		}

		// Add filter properties
		for (var filter in this.properties) {
			if (this.properties[filter] !== undefined && this.properties[filter] !== '' && this.properties[filter] !== null) {
				// Special case for "like" filters, add wildcard
				if (filter.indexOf("Like") > 0) {
					params[filter] = "%" + this.properties[filter] + "%";
				} else {
					if (this.properties[filter].getMonth) {
						// Special handling for dates. Use correct format and reset time-component
						if (filter.indexOf("Before") > 0) {
							ActivitiAdmin.Utils.resetTimeToEvening(this.properties[filter]);
						} else {
							ActivitiAdmin.Utils.resetTimeToMorning(this.properties[filter]);
						}
						params[filter] = moment(this.properties[filter]).format("YYYY-MM-DDTHH:mm:ssZ");
					} else {
					  // Normal value, pass it through the callback first, if any
					  var value = this.properties[filter];
					  if(this.config.valueFilter) {
					    value = this.config.valueFilter(filter, this.properties[filter]);
					  }
						params[filter] = value;
					}
				}
			}
		}
		
		if(httpConfig.method == 'POST' ) {
			// Populate JSON-body
			httpConfig.data = params;
		} else {
			// Populate query string
			httpConfig.params = params;
		}
		return httpConfig;
	}

};




