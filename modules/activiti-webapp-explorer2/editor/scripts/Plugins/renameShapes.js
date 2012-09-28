/**
 * Copyright (c) 2008
 * Willi Tscheschner
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
if (!ORYX.Plugins) 
    ORYX.Plugins = new Object();

ORYX.Plugins.RenameShapes = Clazz.extend({

    facade: undefined,
    
    construct: function(facade){
    
        this.facade = facade;
      	
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_DBLCLICK, this.actOnDBLClick.bind(this)); 
		this.facade.offer({
		 keyCodes: [{
				keyCode: 113, // F2-Key
				keyAction: ORYX.CONFIG.KEY_ACTION_DOWN 
			}
		 ],
         functionality: this.renamePerF2.bind(this)
         });
		
		
		document.documentElement.addEventListener(ORYX.CONFIG.EVENT_MOUSEDOWN, this.hide.bind(this), true ) 
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
		if( !(shape instanceof ORYX.Core.Shape) ){ return }
		
		// Destroys the old input, if there is one
		this.destroy();
		
		// Get all properties which where at least one ref to view is set
		var props = shape.getStencil().properties().findAll(function(item){ 
			return (item.refToView() 
					&&  item.refToView().length > 0
					&&	item.directlyEditable()); 
		});
		// from these, get all properties where write access are and the type is String
		props = props.findAll(function(item){ return !item.readonly() &&  item.type() == ORYX.CONFIG.TYPE_STRING });
		
		// Get all ref ids
		var allRefToViews	= props.collect(function(prop){ return prop.refToView() }).flatten().compact();
		// Get all labels from the shape with the ref ids
		var labels			= shape.getLabels().findAll(function(label){ return allRefToViews.any(function(toView){ return label.id.endsWith(toView) }); })
		
		// If there are no referenced labels --> return
		if( labels.length == 0 ){ return } 
		
		// Define the nearest label
		var nearestLabel 	= labels.length <= 1 ? labels[0] : null;	
		if( !nearestLabel ){
			
			nearestLabel = labels.find(function(label){ return label.node == evt.target || label.node == evt.target.parentNode })
			if( !nearestLabel ){
				
				var evtCoord 	= this.facade.eventCoordinates(evt);

				var trans		= this.facade.getCanvas().rootNode.lastChild.getScreenCTM();
				evtCoord.x		*= trans.a;
				evtCoord.y		*= trans.d;

				var diff = labels.collect(function(label){ 
							var center 	= this.getCenterPosition( label.node ); 
							var len 	= Math.sqrt( Math.pow(center.x - evtCoord.x, 2) + Math.pow(center.y - evtCoord.y, 2));
							return {diff: len, label: label} 
						}.bind(this));
				
				diff.sort(function(a, b){ return a.diff > b.diff })	
				
				nearestLabel = 	diff[0].label;

			}
		}
		// Get the particular property for the label
		var prop 			= props.find(function(item){ return item.refToView().any(function(toView){ return nearestLabel.id == shape.id + toView })});

		// Set all particular config values
		var htmlCont 	= this.facade.getCanvas().getHTMLContainer().id;

		// Get the center position from the nearest label
		var width		= Math.min(Math.max(100, shape.bounds.width()), 200);
		var center 		= this.getCenterPosition( nearestLabel.node );
		center.x		-= (width/2);
		var propId		= prop.prefix() + "-" + prop.id();

		// Set the config values for the TextField/Area
		var config 		= 	{
								renderTo	: htmlCont,
								value		: shape.properties[propId],
								x			: (center.x < 10) ? 10 : center.x,
								y			: center.y,
								width		: width,
								style		: 'position:absolute', 
								allowBlank	: prop.optional(), 
								maxLength	: prop.length(),
								emptyText	: prop.title(),
								cls			: 'x_form_text_set_absolute'
							}
		
		// Depending on the property, generate 
		// ether an TextArea or TextField
		if(prop.wrapLines()) {
			
			config.y 		-= (60/2);
			config['grow']	= true;
			this.shownTextField = new Ext.form.TextArea(config);
		} else {
			
			config.y -= (20/2);
			
			this.shownTextField = new Ext.form.TextField(config);
		}
		
		//focus
		this.shownTextField.focus();
		
		// Define event handler
		//	Blur 	-> Destroy
		//	Change 	-> Set new values					
		this.shownTextField.on( 'blur', 	this.destroy.bind(this) )
		this.shownTextField.on( 'change', 	function(node, value){ 
			var currentEl 	= shape;
			var oldValue	= currentEl.properties[propId]; 
			var newValue	= value;
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
				})
				// Instanciated the class
				var command = new commandClass();
				
				// Execute the command
				this.facade.executeCommands([command]);
			}
		}.bind(this) )

		// Diable the keydown in the editor (that when hitting the delete button, the shapes not get deleted)
		this.facade.disableEvent(ORYX.CONFIG.EVENT_KEYDOWN);
		
	},
	
	getCenterPosition: function(svgNode){
		
		if (!svgNode) { return {x:0, y:0} }
		
		var center 		= {x: 0, y:0 };
		var trans,scale,transLocal,bounds;
		var useParent = false;
		try {
			
			if (('hidden' === svgNode.getAttributeNS(null, 'visibility')&&svgNode.childNodes.length>0)
				||svgNode.childNodes.length === 0) {
				useParent = true;
			}
			
			var el 		= useParent ? svgNode.parentNode : svgNode;
			
			trans 		= el.getTransformToElement(this.facade.getCanvas().rootNode.lastChild);
			scale 		= this.facade.getCanvas().rootNode.lastChild.getScreenCTM();
			transLocal 	= el.getTransformToElement(el.parentNode);
		} catch(e){
			return {x:0, y:0}
		}
		
		center.x 	= trans.e - transLocal.e;
		center.y 	= trans.f - transLocal.f;
		
		
		try {
			bounds = svgNode.getBBox();
			
			if (!useParent&&!(bounds.x<-1000)) {
				bounds.y -= 1;
			} else {
				bounds = {x:Number(svgNode.getAttribute('x')), y:Number(svgNode.getAttribute('y')), width:0, height:0};
			}
		} catch(e){
			bounds = {x:Number(svgNode.getAttribute('x')), y:Number(svgNode.getAttribute('y')), width:0, height:0};
		}
		
		center.x += bounds.x;
		center.y += bounds.y;
			
		center.x += bounds.width/2;
		center.y += bounds.height/2;
						
		center.x *= scale.a;
		center.y *= scale.d;
		
		return center;			
	},
	
	hide: function(e){
		if (this.shownTextField && (!e || !this.shownTextField.el || e.target !== this.shownTextField.el.dom)) {
			this.shownTextField.onBlur();
		}
	},
	
	destroy: function(e){
		if( this.shownTextField ){
			this.shownTextField.destroy(); 
			delete this.shownTextField; 
			
			this.facade.enableEvent(ORYX.CONFIG.EVENT_KEYDOWN);
		}
	}
});
