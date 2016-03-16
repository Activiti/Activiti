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

if (!ORYX.Plugins) 
    ORYX.Plugins = new Object();

ORYX.Plugins.RenameShapes = Clazz.extend({

    facade: undefined,
    
    construct: function(facade){
    
        this.facade = facade;
      	
        this.facade.registerOnEvent(ORYX.CONFIG.EVENT_CANVAS_SCROLL, this.hideField.bind(this)); 
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_DBLCLICK, this.actOnDBLClick.bind(this)); 
		this.facade.offer({
		 keyCodes: [{
				keyCode: 113, // F2-Key
				keyAction: ORYX.CONFIG.KEY_ACTION_DOWN 
			}
		 ],
         functionality: this.renamePerF2.bind(this)
         });
		
		
		document.documentElement.addEventListener(ORYX.CONFIG.EVENT_MOUSEDOWN, this.hide.bind(this), true);
    },
	
	/**
	 * This method handles the "F2" key down event. The selected shape are looked
	 * up and the editing of title/name of it gets started.
	 */
	renamePerF2 : function() {
		var selectedShapes = this.facade.getSelection();
		this.actOnDBLClick(undefined, selectedShapes.first());
	},
	
	actOnDBLClick: function(evt, shape){
		
		if( !(shape instanceof ORYX.Core.Shape) ){ return; }
		
		// Destroys the old input, if there is one
		this.destroy();
		
		// Get all properties which where at least one ref to view is set
		var props = shape.getStencil().properties().findAll(function(item){ 
			return (item.refToView() 
					&&  item.refToView().length > 0
					&&	item.directlyEditable()); 
		});
		// from these, get all properties where write access are and the type is String or Expression
		props = props.findAll(function(item){ return !item.readonly() &&  (item.type() == ORYX.CONFIG.TYPE_STRING || item.type() == ORYX.CONFIG.TYPE_EXPRESSION || item.type() == ORYX.CONFIG.TYPE_DATASOURCE); });
		
		// Get all ref ids
		var allRefToViews	= props.collect(function(prop){ return prop.refToView(); }).flatten().compact();
		// Get all labels from the shape with the ref ids
		var labels			= shape.getLabels().findAll(function(label){ return allRefToViews.any(function(toView){ return label.id.endsWith(toView); }); });
		
		// If there are no referenced labels --> return
		if( labels.length == 0 ){ return; } 
		
		// Define the nearest label
		var nearestLabel 	= labels.length <= 1 ? labels[0] : null;	
		if( !nearestLabel ){
			nearestLabel = labels.find(function(label){ return label.node == evt.target || label.node == evt.target.parentNode; });
			if( !nearestLabel ){
				
				var evtCoord 	= this.facade.eventCoordinates(evt);
				
				var additionalIEZoom = 1;
                if (!isNaN(screen.logicalXDPI) && !isNaN(screen.systemXDPI)) {
                    var ua = navigator.userAgent;
                    if (ua.indexOf('MSIE') >= 0) {
                        //IE 10 and below
                        var zoom = Math.round((screen.deviceXDPI / screen.logicalXDPI) * 100);
                        if (zoom !== 100) {
                            additionalIEZoom = zoom / 100
                        }
                    }
                }
                
                if (additionalIEZoom !== 1) {
                     evtCoord.x = evtCoord.x / additionalIEZoom;
                     evtCoord.y = evtCoord.y / additionalIEZoom;
                }

				evtCoord.y += $("editor-header").clientHeight - $("canvasSection").scrollTop - 5;
				if (KISBPM.HEADER_CONFIG.showAppTitle == false)
				{
					evtCoord.y += 61;
				}
				
				evtCoord.x -= $("canvasSection").scrollLeft;
				
				var trans		= this.facade.getCanvas().rootNode.lastChild.getScreenCTM();
				evtCoord.x		*= trans.a;
				evtCoord.y		*= trans.d;

				var diff = labels.collect(function(label){ 
							var center 	= this.getCenterPosition( label.node ); 
							var len 	= Math.sqrt( Math.pow(center.x - evtCoord.x, 2) + Math.pow(center.y - evtCoord.y, 2));
							return {diff: len, label: label}; 
						}.bind(this));
				
				diff.sort(function(a, b){ return a.diff > b.diff; });
				
				nearestLabel = 	diff[0].label;

			}
		}
		// Get the particular property for the label
		var prop 			= props.find(function(item){ return item.refToView().any(function(toView){ return nearestLabel.id == shape.id + toView; });});
		
		// Get the center position from the nearest label
		var width		= Math.min(Math.max(100, shape.bounds.width()), 200);
		var center 		= this.getCenterPosition( nearestLabel.node, shape );
		center.x		-= (width/2);
		var propId		= prop.prefix() + "-" + prop.id();
		var textInput = document.createElement("textarea");
		textInput.id = 'shapeTextInput';
		textInput.style.position = 'absolute';
		textInput.style.width = width + 'px';
		textInput.style.left = (center.x < 10) ? 10 : center.x + 'px';
		textInput.style.top = (center.y - 15) + 'px';
		textInput.className = 'x-form-textarea x-form-field x_form_text_set_absolute';
		textInput.value = shape.properties[propId];
		this.oldValueText = shape.properties[propId];
		document.getElementById('canvasSection').appendChild(textInput);
		this.shownTextField = textInput;
		
		
		// Value change listener needs to be defined now since we reference it in the text field
		this.updateValueFunction = function(newValue, oldValue) {
			var currentEl 	= shape;
			var facade		= this.facade;
			
			if (oldValue != newValue) {
				// Implement the specific command for property change
				var commandClass = ORYX.Core.Command.extend({
					construct: function(){
						this.el = currentEl;
						this.propId = propId;
						this.oldValue = oldValue;
						this.newValue = newValue;
						this.facade = facade;
					},
					execute: function(){
						this.el.setProperty(this.propId, this.newValue);
						//this.el.update();
						this.facade.setSelection([this.el]);
						this.facade.getCanvas().update();
						this.facade.updateSelection();
					},
					rollback: function(){
						this.el.setProperty(this.propId, this.oldValue);
						//this.el.update();
						this.facade.setSelection([this.el]);
						this.facade.getCanvas().update();
						this.facade.updateSelection();
					}
				});
				// Instantiated the class
				var command = new commandClass();
				
				// Execute the command
				this.facade.executeCommands([command]);
			}
		}.bind(this);
			
		jQuery("#shapeTextInput").focus();
		
		jQuery("#shapeTextInput").autogrow();
			
		// Disable the keydown in the editor (that when hitting the delete button, the shapes not get deleted)
		this.facade.disableEvent(ORYX.CONFIG.EVENT_KEYDOWN);
		
	},
	
	getCenterPosition: function(svgNode, shape){
		
		if (!svgNode) { return {x:0, y:0}; }
		
		var scale = this.facade.getCanvas().node.getScreenCTM();
		var absoluteXY = shape.bounds.upperLeft();
		
		var hasParent = true;
		var searchShape = shape;
		while (hasParent)
		{
			if (searchShape.getParentShape().getStencil().idWithoutNs() === 'BPMNDiagram')
			{
				hasParent = false;
			}
			else
			{
				var parentXY = searchShape.getParentShape().bounds.upperLeft();
				absoluteXY.x += parentXY.x;
				absoluteXY.y += parentXY.y;
				searchShape = searchShape.getParentShape();
			}
		}
		
		var center = shape.bounds.midPoint();
		center.x += absoluteXY.x + scale.e;
		center.y += absoluteXY.y + scale.f;
		
		center.x *= scale.a;
		center.y *= scale.d;
		
		var additionalIEZoom = 1;
        if (!isNaN(screen.logicalXDPI) && !isNaN(screen.systemXDPI)) {
            var ua = navigator.userAgent;
            if (ua.indexOf('MSIE') >= 0) {
                //IE 10 and below
                var zoom = Math.round((screen.deviceXDPI / screen.logicalXDPI) * 100);
                if (zoom !== 100) {
                    additionalIEZoom = zoom / 100
                }
            }
        }
        
        if (additionalIEZoom === 1) {
             center.y = center.y - jQuery("#canvasSection").offset().top + 5;
             center.x -= jQuery("#canvasSection").offset().left;
        
        } else {
             var canvasOffsetLeft = jQuery("#canvasSection").offset().left;
             var canvasScrollLeft = jQuery("#canvasSection").scrollLeft();
             var canvasScrollTop = jQuery("#canvasSection").scrollTop();
             
             var offset = scale.e - (canvasOffsetLeft * additionalIEZoom);
             var additionaloffset = 0;
             if (offset > 10) {
                 additionaloffset = (offset / additionalIEZoom) - offset;
             }
             center.y = center.y - (jQuery("#canvasSection").offset().top * additionalIEZoom) + 5 + ((canvasScrollTop * additionalIEZoom) - canvasScrollTop);
             center.x = center.x - (canvasOffsetLeft * additionalIEZoom) + additionaloffset + ((canvasScrollLeft * additionalIEZoom) - canvasScrollLeft);
        }
		
	
		return center;			
	},
	
	hide: function(e){
		if (this.shownTextField && (!e || e.target !== this.shownTextField)) {
			var newValue = this.shownTextField.value;
			if (newValue !== this.oldValueText)
			{
				this.updateValueFunction(newValue, this.oldValueText);
			}
			this.destroy();
		}
	},
	
	hideField: function(e){
		if (this.shownTextField) {
			this.destroy();
		}
	},
	
	destroy: function(e){
		var textInputComp = jQuery("#shapeTextInput");
		if( textInputComp ){
			textInputComp.remove(); 
			delete this.shownTextField; 
			
			this.facade.enableEvent(ORYX.CONFIG.EVENT_KEYDOWN);
		}
	}
});
