/**
 * Copyright (c) 2009
 *
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

if (!Signavio) {
	var Signavio = new Object();
}

if (!Signavio.Plugins) {
	Signavio.Plugins = new Object();
}

if (!Signavio.Plugins.Utils) {
	Signavio.Plugins.Utils = new Object();
}

if (!Signavio.Helper) {
	Signavio.Helper = new Object();
}


new function(){
	
	var mask;
	
	
	Signavio.Plugins.Utils.getFFVersion = function(){
		try {
			return Number(window.navigator.userAgent.match("Firefox.([0-9]+[\.][0-9]+)")[1]) || 0 ;
		} catch(e){
			return 0;
		}	
	}
	
	
	/**
	 * Shows an overlay of signavio
	 */
	Signavio.Helper.ShowMask = function(force, parent){
		
		if (!force && ORYX.CONFIG.PREVENT_LOADINGMASK_AT_READY === true){
			return;
		}		
		
		if (mask){
			return;
		}
		
		var s 	= "background:white;bottom:0;height:100%;left:0;position:absolute;right:0;top:0;width:100%;z-index:100000;"
		var ss 	= "left:50%;margin-left:-200px;margin-top:-90px;position:absolute;top:50%;display:none;width:391px;"
		var sversion 	= "color:#ad0f5b;padding-right:10px;font-family:tahoma,arial,san-serif;font-size:12px;";
		var stext 		= "display:block;position:relative;text-align:right;top:0;width:100%;";
		var stitle 		= "color:#ad0f5b;font-weight:bold;padding-right:10px;font-family:tahoma,arial,san-serif;font-size:12px;"
		var sloading 	= "height:16px;width:16px;margin-bottom:-4px;background: transparent url(../libs/ext-2.0.2/resources/images/default/tree/loading.gif) no-repeat center;"
		var simg 		= "padding-bottom:10px;border-bottom:1px solid #ad0f5b;";

		// Define the parent
		parent = (parent ? Ext.get(parent) : null) || Ext.getBody();
		
		if (parent !== Ext.getBody()){
			parent.setStyle("position", "relative")
		}

		mask = Ext.get(document.createElement("div"));
		parent.appendChild(mask);
		mask.dom.setAttribute("style", s);
		mask.dom.innerHTML = 	"<div class='mask-logo' style='"+ss+"'>"+
									"<div>"+
										"<img style='"+simg+"' src='"+ORYX.CONFIG.EXPLORER_PATH+"/src/img/signavio/smoky/logo.png' />"+
									"</div>"+
									"<span class='mask-text' style='"+stext+"'>"+
										"<span class='mask-title' style='"+stitle+"'>Editor</span>"+
										"<span class='mask-version' style='"+sversion+"'>Version "+Signavio.Core.Version+"</span>"+
										"<img style='"+sloading+"' src='"+(ORYX.CONFIG.BLANK_IMAGE||Ext.BLANK_IMAGE_URL)+"'/>"+
									"</span>" +
								"</div>";

		mask.first().show({duration:0.3});

	}
			
	// When body is loaded, show overlay		
	Ext.onReady(Signavio.Helper.ShowMask);
	
	/**
	 * Hides the overlay
	 */
	Signavio.Helper.HideMask = function(){
		window.setTimeout(function(){
			if (mask){			
				mask.first().hide({duration:0.4, remove:true,  block:true});
				mask.hide({duration:0.3, remove:true,  block :true});
				delete mask;
			}

		}.bind(this), 2000)
	}
			
	Signavio.Plugins.Loading = {
	
		facade: undefined,
		construct: function(facade) {
			this.facade = facade;
			
			this.facade.registerOnEvent(ORYX.CONFIG.EVENT_LOADED, Signavio.Helper.HideMask);
			
			/**
			 * Overwrite the toJSON method in the Canvas
			 * to set the correct stencilset namespace.
			 * 
			 */
			/*(var me = this;
			new function(){
				// Copy prototype method
				var toJSON = ORYX.Core.Canvas.prototype.toJSON;
				ORYX.Core.Canvas.prototype.toJSON = function(){
					// Call super
					var json = toJSON.call(this);
					// Check for replace stencil set namespace
					json.stencilset.namespace = me.facade.getModelMetaData().model.stencilset.namespace;
					
					return json;
				}
			}()*/
		}
	}
	
	Signavio.Plugins.Loading = Clazz.extend(Signavio.Plugins.Loading);

	/**
	 * Provides an uniq id
	 * @overwrite
	 * @return {String}
	 *
	 */
	ORYX.Editor.provideId = function() {
		var res = [], hex = '0123456789ABCDEF';
	
		for (var i = 0; i < 36; i++) res[i] = Math.floor(Math.random()*0x10);
	
		res[14] = 4;
		res[19] = (res[19] & 0x3) | 0x8;
	
		for (var i = 0; i < 36; i++) res[i] = hex[res[i]];
	
		res[8] = res[13] = res[18] = res[23] = '-';
	
		return "sid-" + res.join('');
	};


}();


/**
 * Ext specific extension
 * 
 * 
 * 
 */
new function(){
	
	/**
	 * Implementation of an Ext-LinkButton
	 * 
	 * 
	 */
	Ext.LinkButton = Ext.extend(Ext.BoxComponent, {

		// On Click Handler
	    click: null,
		
		// Image url 
	    image: null,
		
		// Image style (only if an image url is setted) 
	    imageStyle: null,

		toggle:false, 
		
		toggleStyle:null,

		selected:false,
		
		href:false,

		el: null, 
		
	    // private
	    onRender : function(ct, position){
					
	        if( this.el == null ){	

	            this.el = document.createElement('a');

	            if (this.tabIndex)
	            	this.el.setAttribute("tabindex", this.tabIndex)
	            
	            this.el.id = this.getId();
	            this.el.className = this.cls||"x-link-button";
				
				if( !this.disabled )
	            	this.el.href = this.href ? this.href : "#" + this.text;

	            if( !this.disabled ){
	                Element.observe( this.el, 'click', this.onClick.bind(this));
	            }
		
				if( this.image ){
					this.el.innerHTML = '<img src="' + this.image + '" title="' + this.text + '"' + ( this.imageStyle ? ' style="' + this.imageStyle + '"/>': '/>')
				} else {
					this.el.innerHTML = this.text ? Ext.util.Format.htmlEncode(this.text) : (this.html || '');	
				}

	            if(this.forId){
	                this.el.setAttribute('htmlFor', this.forId);
	            }

	        }

	        Ext.LinkButton.superclass.onRender.call(this, ct, position);

	    },
		
		onClick: function(e){
			
			if( this.disabled ){ Event.stop(e); return; }
			
			// Toggle the button
			if( this.toggle ){
				this.selected = !this.selected;
				if( this.toggleStyle ){
					this._setStyle( this.el.dom, '')
					this.el.dom.setAttribute('style','')
					if( this.selected ){
						this.el.applyStyles( this.toggleStyle )
					} else {
						this.el.applyStyles( this.initialConfig.style )
					}
				}
			}

			
			if( this.click instanceof Function )
				this.click.apply(this.click, [this, e]); 
			 
			Event.stop(e)
		},
	    
	    setText: function(t, encode){
	        this.text = t;
	        if(this.rendered){
	            this.el.dom.innerHTML = encode !== false ? Ext.util.Format.htmlEncode(t) : t;
	        }
	        return this;
	    },
		
		_setStyle: function(node, style){
			if( Ext.isIE ){
				node.style.setAttribute('cssText', style );	
			} else {
				node.setAttribute('style', style );	
			}
		}
	});

	Ext.reg('linkbutton', Ext.LinkButton);
	
}();


/**
 * Helper Methods
 * 
 */

new function(){
	
	
	Signavio.Helper.RecordReader = function(meta){
	    meta = meta || {};
	    this.rels = meta.rels || this.rels;
	    Signavio.Helper.RecordReader.superclass.constructor.call(this, meta, ['rep','href','rel']);
	};
	Ext.extend(Signavio.Helper.RecordReader, Ext.data.JsonReader, {

		rels: ["gitem"],
		
		read : function(response){
			var json = response.responseText;
			var o = eval("("+json+")");
			if(!o) {
				throw {message: "JsonReader.read: Json object not found"};
			}
			var Record = this.recordType;
			var records = [], total = 0;
			o.each(function(rec){
				if (this.rels.include(rec.rel)) {
					records.push(new Record(rec));
				} 
				if (rec.rel == "info" && rec.rep.size){
					total = rec.rep.size;
				}
			}.bind(this))
			return {
				success : true,
				records : records,
				totalRecords : total || records.length
			}
		}
	})
	
	
	
	/**
	 * Creates a new record, including 'rel', 'href', and 'rep' attributes
	 * @param {String} rel
	 * @param {String} href
	 * @param {Object} rep
	 */
	Signavio.Helper.createRecord = function(rel, href, rep){
					
		var Rec = Ext.data.Record.create(["rel", "href", "rep"]);

		var record = new Rec({
		    rel	: rel,
		    href: href,
		    rep	: rep
		});
		
		return record;
	}	
}()

