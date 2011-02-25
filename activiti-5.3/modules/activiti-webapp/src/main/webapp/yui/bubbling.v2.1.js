/*
Copyright (c) 2008, Bubbling Library Team. All rights reserved.
Portions Copyright (c) 2008, Yahoo!, Inc. All rights reserved.
Code licensed under the BSD License:
http://www.bubbling-library.com/eng/licence
version: 2.1
*/
YAHOO.namespace("plugin","behavior");
(function() {
  var $Y = YAHOO.util,
	  $E = YAHOO.util.Event,
	  $D = YAHOO.util.Dom,
	  $L = YAHOO.lang,
	  $  = YAHOO.util.Dom.get;
	  
  /**
  * @class Bubbling
  */
  YAHOO.Bubbling = function () {
  	var obj = {},
	    _config = {
			classname: 'js'
		},
		_handle = null;
	// private stuff
	var navRelExternal = function (layer, args) {
		  var el = args[1].anchor;
		  if (!(args[1].flagged || args[1].decrepitate) && el) {
			  var r = el.getAttribute("rel"),
			  	  t = el.getAttribute("target");
		  	  if ((!t || (t === '')) && (r == 'external')) {
				el.setAttribute("target", "blank");
		      }
		  }
	};
    var defaultActionsControl = function (layer, args) {
	  obj.processingAction (layer, args, obj.defaultActions);
    };
	var _target = {newRef: null, oldRef: null, value: null}; // last target (this variable will be used by the onChange/onFocus/onBlur layer)
	
	function _onBlur() {
		if (_target.oldRef) {
			// firing the blur
			obj.fire( 'blur', {
				target: _target.oldRef
			});
		}
	}
	
	function _onFocus() {
		if (_target.newRef) {
			// firing the blur
			obj.fire( 'focus', {
				target: _target.newRef,
				blur: _target.oldRef
			});
		}
	}
	
	function _onChange() {
		if (_target.newRef) {
			_target.newValue = (_target.tagName=="SELECT"?_target.newRef[_target.newRef.selectedIndex].value:_target.newRef.value);
		    if (_target.newValue != _target.oldValue) {
				if (YAHOO.env.ua.webkit && (_target.tagName == "SELECT")) {
					_target.newRef.setAttribute( 'default', _target.newValue );
				}
				// firing the blur
				obj.fire( 'change', {
					target: _target.newRef,
					value: _target.newValue,
					newValue: _target.newValue,
					oldValue: _target.oldValue,
					rel: _target.newRef.getAttribute( 'rel' )
				});
				_target.oldValue = _target.newValue;
			}
		}
	}
	
	function _setTarget (newRef, tn) {
		_target = {
			oldRef: _target.newRef,
			newRef: newRef,
			tagName: (newRef&&newRef.tagName?newRef.tagName.toUpperCase():null),
			oldValue: (newRef?newRef.value:null)
		};
		if (tn=='SELECT') { // select element (hack for safari: default attribute)
			_target.oldValue = (YAHOO.env.ua.webkit?newRef.getAttribute('default'):newRef[newRef.selectedIndex].value);
		}
	}

	function _isFocusable (t) {
		var tagName, tags = '[INPUT][TEXTAREA][SELECT][BUTTON][A][IMG]';
		if (t && (tagName = t.tagName.toUpperCase())) {
			if (t.getAttribute('tabindex') || (tags.indexOf('['+tagName+']') !== -1))  {
				return true;
			}
		}
		return false;
	}

	function _checkFocus (t, conf) {
		/* checking the tagName */
		var tn = (t&&t.tagName?t.tagName.toUpperCase():null);
		if (t && tn) {
			if (tn == 'OPTION') {
				return _checkFocus (t.parentNode, conf);
			} else {
				/* checking if the target is the document */
				if (document.body && (t === document.body)) {
					// the document received the focus, trigger the onBlur and onChange
					_onChange ();
					_setTarget (null);
					_onBlur ();
				} else if ((t !== _target.newRef) && _isFocusable(t)) {
					/* checking if the target element is focusable */
					/* - 1: setting the new target */
					_onChange ();
					_setTarget (t, tn);
					_onBlur ();
					_onFocus ();
				}
			}
		}
	}

	// public vars
	obj.ready = false;
	obj.bubble = {}; // CustomEvent Handles
    
	// mapping external methods...
	obj.getOwnerByClassName = function(node, className) {
		return ($D.hasClass(node, className)?node:$D.getAncestorByClassName (node, className));
    };
    obj.getOwnerByTagName = function(node, tagName) {
		node = $D.get(node);
		if (!node) {
			return null;
		}
		return (node.tagName && node.tagName.toUpperCase() == tagName.toUpperCase()?node:$D.getAncestorByTagName (node, tagName));
	};
	// Deprecated in favor of getOwnerByClassName and getOwnerByTagName
	obj.getAncestorByClassName = obj.getOwnerByClassName;
    obj.getAncestorByTagName = obj.getOwnerByTagName;

	// public methods
	obj.onKeyPressedTrigger = function(args, e, m){
	  var b = 'key';
	  e = e || $E.getEvent();
	  m = m || {};
	  m.action = b;
	  // comparing the targets and the value of the targets
	  m.target = args.target || (e?$E.getTarget(e):null);
	  //_monitorChanges( m.target, m );
	  _checkFocus (m.target, m);
	  m.flagged = false; m.decrepitate = false;
	  m.event = e;
	  m.stop = false;
	  m.type = args.type;
	  m.keyCode = args.keyCode;
	  m.charCode = args.charCode;
	  m.ctrlKey = args.ctrlKey;
	  m.shiftKey = args.shiftKey;
	  m.altKey = args.altKey;
	  this.bubble.key.fire(e, m);
	  if (m.stop) {
	  	$E.stopEvent(e);
	  }
	  return m.stop;
	};
	obj.onEventTrigger = function(b, e, m){
	  e = e || $E.getEvent();
	  var t = 
	  m = m || {};
	  m.action = b;
	  m.target = (e?$E.getTarget(e):null);
	  // comparing the target and the value of the target (if clicks)
	  if ((b == 'navigate') || (b == 'property')) {
	  	//_checkChanges( m.target, m );
		_checkFocus(m.target, m);
	  }
	  m.flagged = false; m.decrepitate = false;
	  m.event = e;
	  m.stop = false;
	  this.bubble[b].fire(e, m);
	  if (m.stop) {
	  	$E.stopEvent(e);
	  }
	  return m.stop;
	};
	obj.onNavigate = function(e){
	  var conf = {
	  	anchor: this.getOwnerByTagName( $E.getTarget(e), 'A' ),
		button: obj.getYUIButton($E.getTarget(e))
	  };
	  if (conf.button) {
          conf.value = conf.button.get('value');
          conf.rel = conf.button._button.getAttribute('rel');
	  } else if (conf.anchor) {
		  conf.rel = conf.anchor.getAttribute('rel');
	  } else {
	  	  conf.input = this.getOwnerByTagName( $E.getTarget(e), 'INPUT' );
	  	  conf.select = this.getOwnerByTagName( $E.getTarget(e), 'SELECT' );
		  if (conf.input) {
				conf.value = conf.input.getAttribute('value');
				conf.rel = conf.input.getAttribute('rel');
		  } else if (conf.select) {
				conf.value = conf.select[conf.select.selectedIndex].value;
				conf.rel = conf.select.getAttribute('rel');
		  }
	  }
	  if (!this.onEventTrigger ('navigate', e, conf)) {
	    this.onEventTrigger ('god', e, conf); // if nobody claim the event, god can handle it...
	  }
	};
	obj.onProperty = function(e){
	  this.onEventTrigger ('property', e, {
	  	anchor: this.getOwnerByTagName( $E.getTarget(e), 'A' ),
		button: obj.getYUIButton($E.getTarget(e))
	  });
	};
	obj._timeoutId = 0;
	obj.onRepaint = function(e){
	  // Downshift Your Code (can't let something happen multiple times in a second)
	  // http://yuiblog.com/blog/2007/07/09/downshift-your-code/
      clearTimeout(obj._timeoutId);
      obj._timeoutId = setTimeout(function(){
            var b = 'repaint',
                e = {target:document.body},
                m = {
            	    action: b,
            	    target: null,
            	    event: e,
	  				flagged: false,
            	    decrepitate: false,
            	    stop: false
            	};
        	obj.bubble[b].fire(e, m);
        	if (m.stop) {
        	  	$E.stopEvent(e);
        	}
        }, 150
      );
	};
	obj.onRollOver = function(e){
	  this.onEventTrigger ('rollover', e, {
	  	anchor: this.getOwnerByTagName( $E.getTarget(e), 'A' )
	  });
	};
	obj.onRollOut = function(e){
	  this.onEventTrigger ('rollout', e, {
	  	anchor: this.getOwnerByTagName( $E.getTarget(e), 'A' )
	  });
	};
	obj.onKeyPressed = function(args){
	  this.onKeyPressedTrigger(args);
	};
	/**
	* * This method will try to match the classname of the DOM element with the list of actions (literal object)
	* @public
	* @param {object} el        element reference
	* @param {object} actions   object with the list of possibles actions
	* @return void
	*/
	obj.getActionName = function (el, depot) {
	  depot = depot || {};
	  var b = null, r = null,
	      f = ($D.inDocument(el)?function(b){return $D.hasClass(el, b)}:function(b){return el.hasClass(b);}); // f: check is certain object has a classname
	  if (el && ($L.isObject(el) || (el = $( el )))) {
	  	try{
			r = el.getAttribute("rel"); // if rel is available...
		} catch (e) {};
		for (b in depot) { // behaviors in the depot...
			if ((depot.hasOwnProperty(b)) && (f(b) || (b === r))) {
				return b;
			}
		}
	  }
	  return null;
	};
	/**
	* * This method will try to match the classnames of the DOM element with the list of actions (literal object)
	* @public
	* @param {object} el        element reference
	* @param {object} actions   object with the list of possibles actions
	* @return void
	*/
	obj.getAllActions = function (el, depot) {
	  depot = depot || {};
	  var b = null, r = null, actions = [],
	      f = ($D.inDocument(el)?function(b){return $D.hasClass(el, b)}:function(b){return el.hasClass(b);}); // f: check is certain object has a classname
	  if (el && ($L.isObject(el) || (el = $( el )))) {
	  	try{
			r = el.getAttribute("rel"); // if rel is available...
		} catch (e) {};
		for (b in depot) { // behaviors in the depot...
			if ((depot.hasOwnProperty(b)) && (f(b) || (b === r))) {
				actions.push(b);
			}
		}
	  }
	  return actions;
	};
	/**
	* * Getting the first child element based on the tagName
	* @public
	* @param {object} el Child element reference
	* @param {object} c  ClassName of the Ancestor
	* @return void
	*/
	obj.getFirstChildByTagName = function (el, t) {
	  if (el && ($L.isObject(el) || (el = $( el ))) && t) {
	  	var l = el.getElementsByTagName(t);
		if (l.length > 0) {
		  return l[0];
		}
	  }
	  return null;
	};
	/**
	* * Analyzing the target and the related target elements to see if the action was within a certain element
	* @public
	* @param {object} e  event reference
	* @param {object} el DOM element reference
	* @return bool
	*/
	obj.virtualTarget = function (e, el) {
	  if (el && ($L.isObject(el) || (el = $( el ))) && $L.isObject(e)) {
	    var t = $E.getRelatedTarget ( e );  // target element
	    if ($L.isObject(t)) {
	      while((t.parentNode) && $L.isObject(t.parentNode) &&  (t.parentNode.tagName !== "BODY")) {
		    if (t.parentNode === el) {
		      return true;
		    }
		    t = t.parentNode;
		  }
		}
	  }
	  return false;
	};
	/**
	* * getting the real YUI Button reference from a dom element, usually the target for a certain event
	* @public
	* @param {object} t      dom element reference
	* @return void
	*/
	obj.getYUIButton = function (t) {
		var el = this.getOwnerByClassName( t, 'yui-button' ), bt = null, id = null;
		if ($L.isObject(el) && YAHOO.widget.Button) {
			bt = YAHOO.widget.Button.getButton(el.id);
		}
		return bt;
	};
	/**
	* * Creating a new behaviors layer...
	* @public
	* @param {string||array} layers  Behaviors layers GUID
	* @param {object} scope  Custom  Event default execution scope
	* @return boolean if not exists...
	*/
    obj.addLayer = function (layers, scope) {
		var result = false;
		layers = ($L.isArray(layers)?layers:[layers]);
        scope = scope || window;
		for (var i = 0; i < layers.length; ++i) {
          if (layers[i] && !this.bubble.hasOwnProperty(layers[i])) {
            this.bubble[layers[i]] = new $Y.CustomEvent(layers[i], scope, true);
            result = true;
          }
        }
		return result;
    };
	/**
	* * Subscribing an behavior to certain behaviors layer...
	* @public
	* @param {string} layer  Behavior layer GUID
	* @param {object} bh     The function that represent the behavior
	* @return boolean if it is the first listener
	*/
    obj.subscribe = function (layer, bh, scope) {
        var first = this.addLayer(layer); // return true if it's the first listener
        if (layer) {
			if ($L.isObject(scope)) {
			  this.bubble[layer].subscribe(bh, scope, true);  // correcting the default scope
			} else {
			  this.bubble[layer].subscribe(bh);  // use the default scope
			}
        }
        return first;
    };
    obj.on = obj.subscribe; // defining an alias...
	/**
	* * Broadcasting the message in the corresponding behavior layer...
	* @public
	* @param {string} layer  Behavior layer GUID
	* @param {object} obj    The function that represent the behavior
	* @return boolean if someone has claim the event
	*/
    obj.fire = function (layer, obj) {
	    obj = obj || {};
	    obj.action = layer;
	    obj.flagged = false; obj.decrepitate = false;
	    obj.stop = false;
	    if (this.bubble.hasOwnProperty(layer)) {
	        this.bubble[layer].fire(null, obj);
	    }
	    return obj.stop;
    };
	/**
	* * Processing an action based on the classname of the target element...
	* @public
	* @param {string} layer     Behavior layer GUID
	* @param {object} args      Event object (extended)
	* @param {object} actions   List of available behaviors...
	* @param {boolean} force    Process the actions without worry about the flagged value...
	* @return void
	*/
	obj.processingAction = function (layer, args, actions, force) {
      var behavior = null, t;
	  if (!(args[1].flagged || args[1].decrepitate) || force) {
	  	  // checking for anchor, input or button
		  t = args[1].anchor || args[1].button || args[1].input || args[1].select;
		  if (t) {
			behavior = this.getActionName ( t, actions );
			args[1].el = t;
		  }
		  if (behavior && (actions[behavior].apply(args[1], [layer, args]))) {
			$E.stopEvent(args[0]);
			args[1].flagged = true;
		    args[1].decrepitate = true;
		    args[1].stop = true;
		  }
	  }
	};
	obj.defaultActions = {};
    obj.addDefaultAction = function (n, f, force) {
		if (n && f && (!this.defaultActions.hasOwnProperty(n) || force)) {
		  	this.defaultActions[n] = f;
		}
    };

	// default Suscriptions
	obj.on('navigate', navRelExternal);
	obj.on('navigate', defaultActionsControl);
	
	// initialization of the font and scroll monitors
	obj.initMonitors = function () {
	    var fMonitors = function () {
            var oMonitors = new YAHOO.widget.Module('yui-cms-font-monitor', {
                monitorresize:true,
                visible:false
            });
            oMonitors.render(document.body);
            // monitoring font-size...
            YAHOO.widget.Module.textResizeEvent.subscribe(obj.onRepaint, obj, true);
            // monitoring scroll actions...
            YAHOO.widget.Overlay.windowScrollEvent.subscribe(obj.onRepaint, obj, true);
	    };
	    if ($L.isFunction(YAHOO.widget.Module)) {
	      $E.onDOMReady (fMonitors, obj, true);
	    }
    };
	// initialization inside the selfconstructor
	obj.init = function () {
	  var el = document.body; /* the top of the DOM */
	  clearInterval(_handle); /* there is not need to keep waiting for the timer to execute this function */
	  if (!this.ready) {
	  	this.ready = true;
		
		// override the default configuration object with an external configuration object
	  	YAHOO._Bubbling = YAHOO._Bubbling || {};
	  	$L.augmentObject(_config, YAHOO._Bubbling, true);
		
		// hack to add the class JS to the body
		$D.addClass (el, _config.classname);
		
		// default behaviors
		$E.addListener(window, "resize", obj.onRepaint, obj, true);

	    $E.addListener(el,
			"click",
			obj.onNavigate,
			obj,
			true
		);
	    /*
	        Listen for the "mousedown" event in Opera b/c it does not
	        support the "contextmenu" event
	    */
	    $E.addListener(
	        el,
	        (YAHOO.env.ua.opera ? "mousedown" : "contextmenu"),
	        obj.onProperty,
	        obj,
	        true
	    );
	    /*
	        Assign a "click" event handler to the trigger element(s) for
	        Opera to prevent default browser behaviors.
	    */
	    if(YAHOO.env.ua.opera) {
	        $E.addListener(
	            el,
	            "click",
	            obj.onProperty,
	            obj,
	            true
	        );
	    }
		$E.addListener(el, "mouseover", obj.onRollOver, obj, true);
	    $E.addListener(el, "mouseout", obj.onRollOut, obj, true);

		// keys...
        $E.addListener(document, "keyup",  obj.onKeyPressed, obj, true);
		$E.addListener(document, "keydown",  obj.onKeyPressed, obj, true);

		obj.fire('ready', {module:'bubbling'});
	  }
	};
	// creating the default layers...
	obj.addLayer (['navigate','god','property','key','repaint','rollover', 'rollout', 'blur', 'focus', 'change', 'ready']); // god layer - hack: the layer after the common navigate layer...

    // all the listeners will be added to the document.body, but we need to wait until the object become available to add the listeners.
	_handle = setInterval(function() {
	    try {
	        // throws an error if document.body is not exist
			if ($L.isObject(document.body)) {
				obj.init ();
			}
	    } catch (e) { 
	    }
	}, $E.POLL_INTERVAL);
	// if something goes wrong, we just wait for the onDOMReady...
	$E.onDOMReady(obj.init, obj, true);
	
	return obj;
  }();
})();
YAHOO.register("bubbling", YAHOO.Bubbling, {version: "2.1", build: "234"});