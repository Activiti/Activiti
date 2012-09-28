/**
 * Copyright (c) 2009
 * Jan-Felix Schwarz, Willi Tscheschner, Nicolas Peters, Martin Czuchra, Daniel Polak
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

if(!ORYX.Plugins) {
	ORYX.Plugins = new Object();
}

ORYX.Plugins.ShapeMenuPlugin = {

	construct: function(facade) {
		this.facade = facade;
		
		this.alignGroups = new Hash();

		var containerNode = this.facade.getCanvas().getHTMLContainer();

		this.shapeMenu = new ORYX.Plugins.ShapeMenu(containerNode);
		this.currentShapes = [];

		// Register on dragging and resizing events for show/hide of ShapeMenu
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_DRAGDROP_START, this.hideShapeMenu.bind(this));
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_DRAGDROP_END,  this.showShapeMenu.bind(this));
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_RESIZE_START,  (function(){
			this.hideShapeMenu();
			this.hideMorphMenu();
		}).bind(this));
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_RESIZE_END,  this.showShapeMenu.bind(this));
		
		// Enable DragZone
		var DragZone = new Ext.dd.DragZone(containerNode.parentNode, {shadow: !Ext.isMac});
		DragZone.afterDragDrop = this.afterDragging.bind(this, DragZone);
		DragZone.beforeDragOver = this.beforeDragOver.bind(this, DragZone);
		
		// Memory of created Buttons
		this.createdButtons = {};
		
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_STENCIL_SET_LOADED, (function(){ this.registryChanged() }).bind(this));

		this.timer = null;
		
		this.resetElements = true;

	},

	hideShapeMenu: function(event) {
		window.clearTimeout(this.timer);
		this.timer = null;
		this.shapeMenu.hide();
	},

	showShapeMenu: function( dontGenerateNew ) {
	
		if( !dontGenerateNew || this.resetElements ){
			
			window.clearTimeout(this.timer);
			this.timer = window.setTimeout(function(){
				
					// Close all Buttons
				this.shapeMenu.closeAllButtons();
		
				// Show the Morph Button
				this.showMorphButton(this.currentShapes);
				
				// Show the Stencil Buttons
				this.showStencilButtons(this.currentShapes);	
				
				// Show the ShapeMenu
				this.shapeMenu.show(this.currentShapes);
				
				this.resetElements = false;
			}.bind(this), 300)
			
		} else {
			
			window.clearTimeout(this.timer);
			this.timer = null;
			
			// Show the ShapeMenu
			this.shapeMenu.show(this.currentShapes);
			
		}
	},

	registryChanged: function(pluginsData) {
		
		if(pluginsData) {
			pluginsData = pluginsData.each(function(value) {value.group = value.group ? value.group : 'unknown'});
			this.pluginsData = pluginsData.sortBy( function(value) {
				return (value.group + "" + value.index);
			});			
		}		
		
		this.shapeMenu.removeAllButtons();
		this.shapeMenu.setNumberOfButtonsPerLevel(ORYX.CONFIG.SHAPEMENU_RIGHT, 2);
		this.createdButtons = {};
		
		this.createMorphMenu();
		
		if( !this.pluginsData ){
			this.pluginsData = [];
		}

		this.baseMorphStencils = this.facade.getRules().baseMorphs();
		
		// Checks if the stencil set has morphing attributes
		var isMorphing = this.facade.getRules().containsMorphingRules();
		
		// Create Buttons for all Stencils of all loaded stencilsets
		var stencilsets = this.facade.getStencilSets();
		stencilsets.values().each((function(stencilSet){
			
			var nodes = stencilSet.nodes();
			nodes.each((function(stencil) {
								
				// Create a button for each node
				var option = {type: stencil.id(), namespace: stencil.namespace(), connectingType: true};
				var button = new ORYX.Plugins.ShapeMenuButton({
					callback: 	this.newShape.bind(this, option),
					icon: 		stencil.icon(),
					align: 		ORYX.CONFIG.SHAPEMENU_RIGHT,
					group:		0,
					//dragcallback: this.hideShapeMenu.bind(this),
					msg:		stencil.title() + " - " + ORYX.I18N.ShapeMenuPlugin.clickDrag
					});
				
				// Add button to shape menu
				this.shapeMenu.addButton(button); 
				
				// Add to the created Button Array
				this.createdButtons[stencil.namespace() + stencil.type() + stencil.id()] = button;
				
				// Drag'n'Drop will enable
				Ext.dd.Registry.register(button.node.lastChild, option);				
			}).bind(this));
		

			var edges = stencilSet.edges();
			edges.each((function(stencil) {

				// Create a button for each edge
				var option = {type: stencil.id(), namespace: stencil.namespace()};
				var button = new ORYX.Plugins.ShapeMenuButton({
					callback: 	this.newShape.bind(this, option),
					// icon: 		isMorphing ? ORYX.PATH + "images/edges.png" : stencil.icon(),
					icon: 		stencil.icon(),
					align: 		ORYX.CONFIG.SHAPEMENU_RIGHT,
					group:		1,
					//dragcallback: this.hideShapeMenu.bind(this),
					msg:		(isMorphing ? ORYX.I18N.Edge : stencil.title()) + " - " + ORYX.I18N.ShapeMenuPlugin.drag
				});
				
				// Add button to shape menu
				this.shapeMenu.addButton(button); 
				
				// Add to the created Button Array
				this.createdButtons[stencil.namespace() + stencil.type() + stencil.id()] = button;
				
				// Drag'n'Drop will enable
				Ext.dd.Registry.register(button.node.lastChild, option);
				
			}).bind(this));
		
		}).bind(this));				
					
	},
	
	createMorphMenu: function() {
		
		this.morphMenu = new Ext.menu.Menu({
			id: 'Oryx_morph_menu',
			items: []
		});
		
		this.morphMenu.on("mouseover", function() {
			this.morphMenuHovered = true;
		}, this);
		this.morphMenu.on("mouseout", function() {
			this.morphMenuHovered = false;
		}, this);
		
		
		// Create the button to show the morph menu
		var button = new ORYX.Plugins.ShapeMenuButton({
			hovercallback: 	(ORYX.CONFIG.ENABLE_MORPHMENU_BY_HOVER ? this.showMorphMenu.bind(this) : undefined), 
			resetcallback: 	(ORYX.CONFIG.ENABLE_MORPHMENU_BY_HOVER ? this.hideMorphMenu.bind(this) : undefined), 
			callback:		(ORYX.CONFIG.ENABLE_MORPHMENU_BY_HOVER ? undefined : this.toggleMorphMenu.bind(this)), 
			icon: 			ORYX.PATH + 'images/wrench_orange.png',
			align: 			ORYX.CONFIG.SHAPEMENU_BOTTOM,
			group:			0,
			msg:			ORYX.I18N.ShapeMenuPlugin.morphMsg
		});				
		
		this.shapeMenu.setNumberOfButtonsPerLevel(ORYX.CONFIG.SHAPEMENU_BOTTOM, 1)
		this.shapeMenu.addButton(button);
		this.morphMenu.getEl().appendTo(button.node);
		this.morphButton = button;
	},
	
	showMorphMenu: function() {
		this.morphMenu.show(this.morphButton.node);
		this._morphMenuShown = true;
	},
	
	hideMorphMenu: function() {
		this.morphMenu.hide();
		this._morphMenuShown = false;
	},
	
	toggleMorphMenu: function() {
		if(this._morphMenuShown)
			this.hideMorphMenu();
		else
			this.showMorphMenu();
	},
	
	onSelectionChanged: function(event) {
		var elements = event.elements;

		this.hideShapeMenu();
		this.hideMorphMenu();
				
		if( this.currentShapes.inspect() !== elements.inspect() ){
			this.currentShapes = elements;
			this.resetElements = true;
			
			this.showShapeMenu();
		} else {
			this.showShapeMenu(true)
		}
		
	},
	
	/**
	 * Show button for morphing the selected shape into another stencil
	 */
	showMorphButton: function(elements) {
		
		if(elements.length != 1) return;
		
		var possibleMorphs = this.facade.getRules().morphStencils({ stencil: elements[0].getStencil() });
		possibleMorphs = possibleMorphs.select(function(morph) {
			if(elements[0].getStencil().type() === "node") {
				//check containment rules
				return this.facade.getRules().canContain({containingShape:elements[0].parent, containedStencil:morph});
			} else { 
				//check connect rules
				return this.facade.getRules().canConnect({
											sourceShape:	elements[0].dockers.first().getDockedShape(), 
											edgeStencil:	morph, 
											targetShape:	elements[0].dockers.last().getDockedShape()
											});	
			}
		}.bind(this));
		if(possibleMorphs.size()<=1) return; // if morphing to other stencils is not possible, don't show button
		
		this.morphMenu.removeAll();
		
		// populate morph menu with the possible morph stencils ordered by their position
		possibleMorphs = possibleMorphs.sortBy(function(stencil) { return stencil.position(); });
		possibleMorphs.each((function(morph) {
			var menuItem = new Ext.menu.Item({ 
				text: morph.title(), 
				icon: morph.icon(), 
				disabled: morph.id()==elements[0].getStencil().id(),
				disabledClass: ORYX.CONFIG.MORPHITEM_DISABLED,
				handler: (function() { this.morphShape(elements[0], morph); }).bind(this) 
			});
			this.morphMenu.add(menuItem);
		}).bind(this));
		
		this.morphButton.prepareToShow();
		
	},

	/**
	 * Show buttons for creating following shapes
	 */
	showStencilButtons: function(elements) {

		if(elements.length != 1) return;

		//TODO temporaere nutzung des stencilsets
		var sset = this.facade.getStencilSets()[elements[0].getStencil().namespace()];

		// Get all available edges
		var edges = this.facade.getRules().outgoingEdgeStencils({canvas:this.facade.getCanvas(), sourceShape:elements[0]});
		
		// And find all targets for each Edge
		var targets = new Array();
		var addedEdges = new Array();
		
		var isMorphing = this.facade.getRules().containsMorphingRules();
		
		edges.each((function(edge) {
			
			if (isMorphing){
				if(this.baseMorphStencils.include(edge)) {
					var shallAppear = true;
				} else {
					
					// if edge is member of a morph groups where none of the base morphs is in the outgoing edges
					// we want to display the button (but only for the first one)
					
					var possibleMorphs = this.facade.getRules().morphStencils({ stencil: edge });
					
					var shallAppear = !possibleMorphs.any((function(morphStencil) {
						if(this.baseMorphStencils.include(morphStencil) && edges.include(morphStencil)) return true;
						return addedEdges.include(morphStencil);
					}).bind(this));
					
				}
			}
			if(shallAppear || !isMorphing) {
				if(this.createdButtons[edge.namespace() + edge.type() + edge.id()]) 
					this.createdButtons[edge.namespace() + edge.type() + edge.id()].prepareToShow();
				addedEdges.push(edge);
			}
			
			// get all targets for this edge
			targets = targets.concat(this.facade.getRules().targetStencils(
					{canvas:this.facade.getCanvas(), sourceShape:elements[0], edgeStencil:edge}));

		}).bind(this));
		
		targets.uniq();
		
		var addedTargets = new Array();
		// Iterate all possible target 
		targets.each((function(target) {
			
			if (isMorphing){
				
				// continue with next target stencil
				if (target.type()==="edge") return; 
				
				// continue when stencil should not shown in the shape menu
				if (!this.facade.getRules().showInShapeMenu(target)) return 
				
				// if target is not a base morph 
				if(!this.baseMorphStencils.include(target)) {
					
					// if target is member of a morph groups where none of the base morphs is in the targets
					// we want to display the button (but only for the first one)
					
					var possibleMorphs = this.facade.getRules().morphStencils({ stencil: target });
					if(possibleMorphs.size()==0) return; // continue with next target
	
					var baseMorphInTargets = possibleMorphs.any((function(morphStencil) {
						if(this.baseMorphStencils.include(morphStencil) && targets.include(morphStencil)) return true;
						return addedTargets.include(morphStencil);
					}).bind(this));
					
					if(baseMorphInTargets) return; // continue with next target
				}
			}
			
			// if this is reached the button shall appear in the shape menu:
			if(this.createdButtons[target.namespace() + target.type() + target.id()]) 
				this.createdButtons[target.namespace() + target.type() + target.id()].prepareToShow();
			addedTargets.push(target);
			
		}).bind(this));
		
	},

	
	beforeDragOver: function(dragZone, target, event){

		if (this.shapeMenu.isVisible){
			this.hideShapeMenu();
		}

		var coord = this.facade.eventCoordinates(event.browserEvent);
		var aShapes = this.facade.getCanvas().getAbstractShapesAtPosition(coord);

		if(aShapes.length <= 0) {return false;}	
		
		var el = aShapes.last();
		
		if(this._lastOverElement == el) {
			
			return false;
			
		} else {
			// check containment rules
			var option = Ext.dd.Registry.getHandle(target.DDM.currentTarget);
			
			// revert to original options if these were modified
			if(option.backupOptions) {
				for(key in option.backupOptions) {
					option[key] = option.backupOptions[key];
				}
				delete option.backupOptions;
			}

			var stencilSet = this.facade.getStencilSets()[option.namespace];

			var stencil = stencilSet.stencil(option.type);

			var candidate = aShapes.last();

			if(stencil.type() === "node") {
				//check containment rules
				var canContain = this.facade.getRules().canContain({containingShape:candidate, containedStencil:stencil});
									
				// if not canContain, try to find a morph which can be contained
				if(!canContain) {
					var possibleMorphs = this.facade.getRules().morphStencils({stencil: stencil});
					for(var i=0; i<possibleMorphs.size(); i++) {
						canContain = this.facade.getRules().canContain({
							containingShape:candidate, 
							containedStencil:possibleMorphs[i]
						});
						if(canContain) {
							option.backupOptions = Object.clone(option);
							option.type = possibleMorphs[i].id();
							option.namespace = possibleMorphs[i].namespace();
							break;
						}
					}
				}
					
				this._currentReference = canContain ? candidate : undefined;
					
	
			} else { //Edge
			
				var curCan = candidate, orgCan = candidate;
				var canConnect = false;
				while(!canConnect && curCan && !(curCan instanceof ORYX.Core.Canvas)){
					candidate = curCan;
					//check connection rules
					canConnect = this.facade.getRules().canConnect({
											sourceShape: this.currentShapes.first(), 
											edgeStencil: stencil, 
											targetShape: curCan
											});	
					curCan = curCan.parent;
				}

			 	// if not canConnect, try to find a morph which can be connected
				if(!canConnect) {
					
					candidate = orgCan;
					var possibleMorphs = this.facade.getRules().morphStencils({stencil: stencil});
					for(var i=0; i<possibleMorphs.size(); i++) {
						var curCan = candidate;
						var canConnect = false;
						while(!canConnect && curCan && !(curCan instanceof ORYX.Core.Canvas)){
							candidate = curCan;
							//check connection rules
							canConnect = this.facade.getRules().canConnect({
														sourceShape:	this.currentShapes.first(), 
														edgeStencil:	possibleMorphs[i], 
														targetShape:	curCan
													});	
							curCan = curCan.parent;
						}
						if(canConnect) {
							option.backupOptions = Object.clone(option);
							option.type = possibleMorphs[i].id();
							option.namespace = possibleMorphs[i].namespace();
							break;
						} else {
							candidate = orgCan;
						}
					}
				}
										
				this._currentReference = canConnect ? candidate : undefined;		
				
			}	

			this.facade.raiseEvent({
											type:		ORYX.CONFIG.EVENT_HIGHLIGHT_SHOW, 
											highlightId:'shapeMenu',
											elements:	[candidate],
											color:		this._currentReference ? ORYX.CONFIG.SELECTION_VALID_COLOR : ORYX.CONFIG.SELECTION_INVALID_COLOR
										});
												
			var pr = dragZone.getProxy();
			pr.setStatus(this._currentReference ? pr.dropAllowed : pr.dropNotAllowed );
			pr.sync();
										
		}
		
		this._lastOverElement = el;
		
		return false;
	},	

	afterDragging: function(dragZone, target, event) {
		
		if (!(this.currentShapes instanceof Array)||this.currentShapes.length<=0) {
			return;
		}
		var sourceShape = this.currentShapes;
		
		this._lastOverElement = undefined;
		
		// Hide the highlighting
		this.facade.raiseEvent({type: ORYX.CONFIG.EVENT_HIGHLIGHT_HIDE, highlightId:'shapeMenu'});
		
		// Check if drop is allowed
		var proxy = dragZone.getProxy()
		if(proxy.dropStatus == proxy.dropNotAllowed) { return this.facade.updateSelection();}
				
		// Check if there is a current Parent
		if(!this._currentReference) { return }
				
		var option = Ext.dd.Registry.getHandle(target.DDM.currentTarget);
		option['parent'] = this._currentReference;

		var xy = event.getXY();
		var pos = {x: xy[0], y: xy[1]};

		var a = this.facade.getCanvas().node.getScreenCTM();
		// Correcting the UpperLeft-Offset
		pos.x -= a.e; pos.y -= a.f;
		// Correcting the Zoom-Faktor
		pos.x /= a.a; pos.y /= a.d;
		// Correcting the ScrollOffset
		pos.x -= document.documentElement.scrollLeft;
		pos.y -= document.documentElement.scrollTop;

		var parentAbs = this._currentReference.absoluteXY();
		pos.x -= parentAbs.x;
		pos.y -= parentAbs.y;
		
		// If the ctrl key is not pressed, 
		// snapp the new shape to the center 
		// if it is near to the center of the other shape
		if (!event.ctrlKey){
			// Get the center of the shape
			var cShape = this.currentShapes[0].bounds.center();
			// Snapp +-20 Pixel horizontal to the center 
			if (20 > Math.abs(cShape.x - pos.x)){
				pos.x = cShape.x;
			}
			// Snapp +-20 Pixel vertical to the center 
			if (20 > Math.abs(cShape.y - pos.y)){
				pos.y = cShape.y;
			}
		}
				
		option['position'] = pos;
		option['connectedShape'] = this.currentShapes[0];
		if(option['connectingType']) {
			var stencilset = this.facade.getStencilSets()[option.namespace];
			var containedStencil = stencilset.stencil(option.type);
			var args = { sourceShape: this.currentShapes[0], targetStencil: containedStencil };
			option['connectingType'] = this.facade.getRules().connectMorph(args).id();
		}
		
		if (ORYX.CONFIG.SHAPEMENU_DISABLE_CONNECTED_EDGE===true) {
			delete option['connectingType'];
		}
			
		var command = new ORYX.Plugins.ShapeMenuPlugin.CreateCommand(Object.clone(option), this._currentReference, pos, this);
		
		this.facade.executeCommands([command]);
		
		// Inform about completed Drag 
		this.facade.raiseEvent({type: ORYX.CONFIG.EVENT_SHAPE_MENU_CLOSE, source:sourceShape, destination:this.currentShapes});
		
		// revert to original options if these were modified
		if(option.backupOptions) {
			for(key in option.backupOptions) {
				option[key] = option.backupOptions[key];
			}
			delete option.backupOptions;
		}	
		
		this._currentReference = undefined;		
	},

	newShape: function(option, event) {
		var stencilset = this.facade.getStencilSets()[option.namespace];
		var containedStencil = stencilset.stencil(option.type);

		if(this.facade.getRules().canContain({
			containingShape:this.currentShapes.first().parent,
			"containedStencil":containedStencil
		})) {

			option['connectedShape'] = this.currentShapes[0];
			option['parent'] = this.currentShapes.first().parent;
			option['containedStencil'] = containedStencil;
		
			var args = { sourceShape: this.currentShapes[0], targetStencil: containedStencil };
			var targetStencil = this.facade.getRules().connectMorph(args);
			if (!targetStencil){ return }// Check if there can be a target shape
			option['connectingType'] = targetStencil.id();

			if (ORYX.CONFIG.SHAPEMENU_DISABLE_CONNECTED_EDGE===true) {
				delete option['connectingType'];
			}
			
			var command = new ORYX.Plugins.ShapeMenuPlugin.CreateCommand(option, undefined, undefined, this);
		
			this.facade.executeCommands([command]);
		}
	},
	
	/**
	 * Morph a shape to a new stencil
	 * {Command implemented}
	 * @param {Shape} shape
	 * @param {Stencil} stencil
	 */
	morphShape: function(shape, stencil) {
		
		var MorphTo = ORYX.Core.Command.extend({
			construct: function(shape, stencil, facade){
				this.shape = shape;
				this.stencil = stencil;
				this.facade = facade;
			},
			execute: function(){
				
				var shape = this.shape;
				var stencil = this.stencil;
				var resourceId = shape.resourceId;
				
				// Serialize all attributes
				var serialized = shape.serialize();
				stencil.properties().each((function(prop) {
					if(prop.readonly()) {
						serialized = serialized.reject(function(serProp) {
							return serProp.name==prop.id();
						});
					}
				}).bind(this));
		
				// Get shape if already created, otherwise create a new shape
				if (this.newShape){
					newShape = this.newShape;
					this.facade.getCanvas().add(newShape);
				} else {
					newShape = this.facade.createShape({
									type: stencil.id(),
									namespace: stencil.namespace(),
									resourceId: resourceId
								});
				}
				
				// calculate new bounds using old shape's upperLeft and new shape's width/height
				var boundsObj = serialized.find(function(serProp){
					return (serProp.prefix === "oryx" && serProp.name === "bounds");
				});
				
				var changedBounds = null;
				
				if(!this.facade.getRules().preserveBounds(shape.getStencil())) {
					
					var bounds = boundsObj.value.split(",");
					if (parseInt(bounds[0], 10) > parseInt(bounds[2], 10)) { // if lowerRight comes first, swap array items
						var tmp = bounds[0];
						bounds[0] = bounds[2];
						bounds[2] = tmp;
						tmp = bounds[1];
						bounds[1] = bounds[3];
						bounds[3] = tmp;
					}
					bounds[2] = parseInt(bounds[0], 10) + newShape.bounds.width();
					bounds[3] = parseInt(bounds[1], 10) + newShape.bounds.height();
					boundsObj.value = bounds.join(",");
					
				}  else {
					
					var height = shape.bounds.height();
					var width  = shape.bounds.width();
					
					// consider the minimum and maximum size of
					// the new shape
					
					if (newShape.minimumSize) {
						if (shape.bounds.height() < newShape.minimumSize.height) {
							height = newShape.minimumSize.height;
						}
						
						
						if (shape.bounds.width() < newShape.minimumSize.width) {
							width = newShape.minimumSize.width;
						}
					}
					
					if(newShape.maximumSize) {
						if(shape.bounds.height() > newShape.maximumSize.height) {
							height = newShape.maximumSize.height;
						}	
						
						if(shape.bounds.width() > newShape.maximumSize.width) {
							width = newShape.maximumSize.width;
						}
					}
					
					changedBounds = {
						a : {
							x: shape.bounds.a.x,
							y: shape.bounds.a.y
						},
						b : {
							x: shape.bounds.a.x + width,
							y: shape.bounds.a.y + height
						}						
					};
					
				}
				
				var oPos = shape.bounds.center();
				if(changedBounds !== null) {
					newShape.bounds.set(changedBounds);
				}
				
				// Set all related dockers
				this.setRelatedDockers(shape, newShape);
				
				// store DOM position of old shape
				var parentNode = shape.node.parentNode;
				var nextSibling = shape.node.nextSibling;
				
				// Delete the old shape
				this.facade.deleteShape(shape);
				
				// Deserialize the new shape - Set all attributes
				newShape.deserialize(serialized);
				/*
				 * Change color to default if unchanged
				 * 23.04.2010
				 */
				if(shape.getStencil().property("oryx-bgcolor") 
						&& shape.properties["oryx-bgcolor"]
						&& shape.getStencil().property("oryx-bgcolor").value().toUpperCase()== shape.properties["oryx-bgcolor"].toUpperCase()){
						if(newShape.getStencil().property("oryx-bgcolor")){
							newShape.setProperty("oryx-bgcolor", newShape.getStencil().property("oryx-bgcolor").value());
						}
				}	
				if(changedBounds !== null) {
					newShape.bounds.set(changedBounds);
				}
				
				if(newShape.getStencil().type()==="edge" || (newShape.dockers.length==0 || !newShape.dockers[0].getDockedShape())) {
					newShape.bounds.centerMoveTo(oPos);
				} 
				
				if(newShape.getStencil().type()==="node" && (newShape.dockers.length==0 || !newShape.dockers[0].getDockedShape())) {
					this.setRelatedDockers(newShape, newShape);
					
				}
				
				// place at the DOM position of the old shape
				if(nextSibling) parentNode.insertBefore(newShape.node, nextSibling);
				else parentNode.appendChild(newShape.node);
				
				// Set selection
				this.facade.setSelection([newShape]);
				this.facade.getCanvas().update();
				this.facade.updateSelection();
				this.newShape = newShape;
				
			},
			rollback: function(){
				
				if (!this.shape || !this.newShape || !this.newShape.parent) {return}
				
				// Append shape to the parent
				this.newShape.parent.add(this.shape);
				// Set dockers
				this.setRelatedDockers(this.newShape, this.shape);
				// Delete new shape
				this.facade.deleteShape(this.newShape);
				// Set selection
				this.facade.setSelection([this.shape]);
				// Update
				this.facade.getCanvas().update();
				this.facade.updateSelection();
			},
			
			/**
			 * Set all incoming and outgoing edges from the shape to the new shape
			 * @param {Shape} shape
			 * @param {Shape} newShape
			 */
			setRelatedDockers: function(shape, newShape){
				
				if(shape.getStencil().type()==="node") {
					
					(shape.incoming||[]).concat(shape.outgoing||[])
						.each(function(i) { 
							i.dockers.each(function(docker) {
								if (docker.getDockedShape() == shape) {
									var rPoint = Object.clone(docker.referencePoint);
									// Move reference point per percent

									var rPointNew = {
										x: rPoint.x*newShape.bounds.width()/shape.bounds.width(),
										y: rPoint.y*newShape.bounds.height()/shape.bounds.height()
									};

									docker.setDockedShape(newShape);
									// Set reference point and center to new position
									docker.setReferencePoint(rPointNew);
									if(i instanceof ORYX.Core.Edge) {
										docker.bounds.centerMoveTo(rPointNew);
									} else {
										var absXY = shape.absoluteXY();
										docker.bounds.centerMoveTo({x:rPointNew.x+absXY.x, y:rPointNew.y+absXY.y});
										//docker.bounds.moveBy({x:rPointNew.x-rPoint.x, y:rPointNew.y-rPoint.y});
									}
								}
							});	
						});
					
					// for attached events
					if(shape.dockers.length>0&&shape.dockers.first().getDockedShape()) {
						newShape.dockers.first().setDockedShape(shape.dockers.first().getDockedShape());
						newShape.dockers.first().setReferencePoint(Object.clone(shape.dockers.first().referencePoint));
					}
				
				} else { // is edge
					newShape.dockers.first().setDockedShape(shape.dockers.first().getDockedShape());
					newShape.dockers.first().setReferencePoint(shape.dockers.first().referencePoint);
					newShape.dockers.last().setDockedShape(shape.dockers.last().getDockedShape());
					newShape.dockers.last().setReferencePoint(shape.dockers.last().referencePoint);
				}
			}
		});
		
		// Create and execute command (for undo/redo)			
		var command = new MorphTo(shape, stencil, this.facade);
		this.facade.executeCommands([command]);
	}
}
ORYX.Plugins.ShapeMenuPlugin = ORYX.Plugins.AbstractPlugin.extend(ORYX.Plugins.ShapeMenuPlugin);

ORYX.Plugins.ShapeMenu = {

	/***
	 * Constructor.
	 */
	construct: function(parentNode) {

		this.bounds = undefined;
		this.shapes = undefined;
		this.buttons = [];
		this.isVisible = false;

		this.node = ORYX.Editor.graft("http://www.w3.org/1999/xhtml", $(parentNode),
			['div', {id: ORYX.Editor.provideId(), 'class':'Oryx_ShapeMenu'}]);
		
		this.alignContainers = new Hash();
		this.numberOfButtonsPerLevel = new Hash();
	},

	addButton: function(button) {
		this.buttons.push(button);
		// lazy grafting of the align containers
		if(!this.alignContainers[button.align]) {
			this.alignContainers[button.align] = ORYX.Editor.graft("http://www.w3.org/1999/xhtml", this.node,
					['div', {'class':button.align}]);
			this.node.appendChild(this.alignContainers[button.align]);
			
			// add event listeners for hover effect
			var onBubble = false;
			this.alignContainers[button.align].addEventListener(ORYX.CONFIG.EVENT_MOUSEOVER, this.hoverAlignContainer.bind(this, button.align), onBubble);
			this.alignContainers[button.align].addEventListener(ORYX.CONFIG.EVENT_MOUSEOUT, this.resetAlignContainer.bind(this, button.align), onBubble);
			this.alignContainers[button.align].addEventListener(ORYX.CONFIG.EVENT_MOUSEUP, this.hoverAlignContainer.bind(this, button.align), onBubble);
		}
		this.alignContainers[button.align].appendChild(button.node);
	},

	deleteButton: function(button) {
		this.buttons = this.buttons.without(button);
		this.node.removeChild(button.node);
	},

	removeAllButtons: function() {
		var me = this;
		this.buttons.each(function(value){
			if (value.node&&value.node.parentNode)
				value.node.parentNode.removeChild(value.node);
		});
		this.buttons = [];
	},

	closeAllButtons: function() {
		this.buttons.each(function(value){ value.prepareToHide() });
		this.isVisible = false;
	},

	
	/**
	 * Show the shape menu
	 */
	show: function(shapes) {

		//shapes = (shapes||[]).findAll(function(r){ return r && r.node && r.node.parent });

		if(shapes.length <= 0 )
			return

		this.shapes = shapes;

		var newBounds = undefined;
		var tmpBounds = undefined;

		this.shapes.each(function(value) {
			var a = value.node.getScreenCTM();
			var upL = value.absoluteXY();
			a.e = a.a*upL.x;
			a.f = a.d*upL.y;
			tmpBounds = new ORYX.Core.Bounds(a.e, a.f, a.e+a.a*value.bounds.width(), a.f+a.d*value.bounds.height());

			/*if(value instanceof ORYX.Core.Edge) {
				tmpBounds.moveBy(value.bounds.upperLeft())
			}*/

			if(!newBounds)
				newBounds = tmpBounds
			else
				newBounds.include(tmpBounds);

		});

		this.bounds = newBounds;
		//this.bounds.moveBy({x:document.documentElement.scrollLeft, y:document.documentElement.scrollTop});

		var bounds = this.bounds;

		var a = this.bounds.upperLeft();

		var left = 0,
			leftButtonGroup = 0;
		var top = 0,
			topButtonGroup = 0;
		var bottom = 0,
			bottomButtonGroup;
		var right = 0
			rightButtonGroup = 0;
		var size = 22;
		
		this.getWillShowButtons().sortBy(function(button) {
			return button.group;
		});
		
		this.getWillShowButtons().each(function(button){
			
			var numOfButtonsPerLevel = this.getNumberOfButtonsPerLevel(button.align);

			if (button.align == ORYX.CONFIG.SHAPEMENU_LEFT) {
				// vertical levels
				if(button.group!=leftButtonGroup) {
					left = 0;
					leftButtonGroup = button.group;
				}
				var x = Math.floor(left / numOfButtonsPerLevel)
				var y = left % numOfButtonsPerLevel;
				
				button.setLevel(x);
				
				button.setPosition(a.x-5 - (x+1)*size, 
						a.y+numOfButtonsPerLevel*button.group*size + button.group*0.3*size + y*size);
				
				//button.setPosition(a.x-22, a.y+left*size);
				left++;
 			} else if (button.align == ORYX.CONFIG.SHAPEMENU_TOP) {
 				// horizontal levels
 				if(button.group!=topButtonGroup) {
					top = 0;
					topButtonGroup = button.group;
				}
 				var x = top % numOfButtonsPerLevel;
 				var y = Math.floor(top / numOfButtonsPerLevel);
 				
 				button.setLevel(y);
 				
 				button.setPosition(a.x+numOfButtonsPerLevel*button.group*size + button.group*0.3*size + x*size,
 						a.y-5 - (y+1)*size);
				top++;
 			} else if (button.align == ORYX.CONFIG.SHAPEMENU_BOTTOM) {
 				// horizontal levels
 				if(button.group!=bottomButtonGroup) {
					bottom = 0;
					bottomButtonGroup = button.group;
				}
 				var x = bottom % numOfButtonsPerLevel;
 				var y = Math.floor(bottom / numOfButtonsPerLevel);
 				
 				button.setLevel(y);
 				
 				button.setPosition(a.x+numOfButtonsPerLevel*button.group*size + button.group*0.3*size + x*size,
 						a.y+bounds.height() + 5 + y*size);
				bottom++;
			} else {
				// vertical levels
				if(button.group!=rightButtonGroup) {
					right = 0;
					rightButtonGroup = button.group;
				}
				var x = Math.floor(right / numOfButtonsPerLevel)
				var y = right % numOfButtonsPerLevel;
				
				button.setLevel(x);
				
				button.setPosition(a.x+bounds.width() + 5 + x*size, 
						a.y+numOfButtonsPerLevel*button.group*size + button.group*0.3*size + y*size - 5);
				right++;
			}

			button.show();
		}.bind(this));
		this.isVisible = true;

	},

	/**
	 * Hide the shape menu
	 */
	hide: function() {

		this.buttons.each(function(button){
			button.hide();
		});

		this.isVisible = false;
		//this.bounds = undefined;
		//this.shape = undefined;
	},

	hoverAlignContainer: function(align, evt) {
		this.buttons.each(function(button){
			if(button.align == align) button.showOpaque();
		});
	},
	
	resetAlignContainer: function(align, evt) {
		this.buttons.each(function(button){
			if(button.align == align) button.showTransparent();
		});
	},
	
	isHover: function() {
		return 	this.buttons.any(function(value){
					return value.isHover();
				});
	},
	
	getWillShowButtons: function() {
		return this.buttons.findAll(function(value){return value.willShow});
	},
	
	/**
	 * Returns a set on buttons for that align value
	 * @params {String} align
	 * @params {String} group
	 */
	getButtons: function(align, group){
		return this.getWillShowButtons().findAll(function(b){ return b.align == align && (group === undefined || b.group == group)})
	},
	
	/**
	 * Set the number of buttons to display on each level of the shape menu in the specified align group.
	 * Example: setNumberOfButtonsPerLevel(ORYX.CONFIG.SHAPEMENU_RIGHT, 2) causes that the buttons of the right align group 
	 * will be rendered in 2 rows.
	 */
	setNumberOfButtonsPerLevel: function(align, number) {
		this.numberOfButtonsPerLevel[align] = number;
	},
	
	/**
	 * Returns the number of buttons to display on each level of the shape menu in the specified align group.
	 * Default value is 1
	 */
	getNumberOfButtonsPerLevel: function(align) {
		if(this.numberOfButtonsPerLevel[align])
			return Math.min(this.getButtons(align,0).length, this.numberOfButtonsPerLevel[align]);
		else
			return 1;
	}

}
ORYX.Plugins.ShapeMenu = Clazz.extend(ORYX.Plugins.ShapeMenu);

ORYX.Plugins.ShapeMenuButton = {
	
	/**
	 * Constructor
	 * @param option A key map specifying the configuration options:
	 * 					id: 	(String) The id of the parent DOM element for the new button
	 * 					icon: 	(String) The url to the icon of the button
	 * 					msg:	(String) A tooltip message
	 * 					caption:(String) The caption of the button (attention: button width > 22, only set for single column button layouts)
	 * 					align:	(String) The direction in which the button is aligned
	 * 					group: 	(Integer) The button group in the specified alignment 
	 * 							(buttons in the same group will be aligned side by side)
	 * 					callback:		(Function) A callback that is executed when the button is clicked
	 * 					dragcallback:	(Function) A callback that is executed when the button is dragged
	 * 					hovercallback:	(Function) A callback that is executed when the button is hovered
	 * 					resetcallback:	(Function) A callback that is executed when the button is reset
	 * 					arguments:		(Array) An argument array to pass to the callback functions
	 */
	construct: function(option) {

		if(option) {
			this.option = option;
			if(!this.option.arguments)
				this.option.arguments = [];
		} else {
			//TODO error
		}

		this.parentId = this.option.id ? this.option.id : null;

		// graft the button.
		var buttonClassName = this.option.caption ? "Oryx_button_with_caption" : "Oryx_button";
		this.node = ORYX.Editor.graft("http://www.w3.org/1999/xhtml", $(this.parentId),
			['div', {'class':buttonClassName}]);

		var imgOptions = {src:this.option.icon};
		if(this.option.msg){
			imgOptions.title = this.option.msg;
		}
		
		// graft and update icon (not in grafting for ns reasons).
		//TODO Enrich graft()-function to do this in one of the above steps.
		if(this.option.icon)
			ORYX.Editor.graft("http://www.w3.org/1999/xhtml", this.node,
				['img', imgOptions]);
		
		if(this.option.caption) {
			var captionNode = ORYX.Editor.graft("http://www.w3.org/1999/xhtml", this.node, ['span']);
			ORYX.Editor.graft("http://www.w3.org/1999/xhtml", captionNode, this.option.caption);
		}

		var onBubble = false;

		this.node.addEventListener(ORYX.CONFIG.EVENT_MOUSEOVER, this.hover.bind(this), onBubble);
		this.node.addEventListener(ORYX.CONFIG.EVENT_MOUSEOUT, this.reset.bind(this), onBubble);
		this.node.addEventListener(ORYX.CONFIG.EVENT_MOUSEDOWN, this.activate.bind(this), onBubble);
		this.node.addEventListener(ORYX.CONFIG.EVENT_MOUSEUP, this.hover.bind(this), onBubble);
		this.node.addEventListener('click', this.trigger.bind(this), onBubble);
		this.node.addEventListener(ORYX.CONFIG.EVENT_MOUSEMOVE, this.move.bind(this), onBubble);

		this.align = this.option.align ? this.option.align : ORYX.CONFIG.SHAPEMENU_RIGHT;
		this.group = this.option.group ? this.option.group : 0;

		this.hide();

		this.dragStart 	= false;
		this.isVisible 	= false;
		this.willShow 	= false;
		this.resetTimer;
	},
	
	hide: function() {
		this.node.style.display = "none";
		this.isVisible = false;
	},

	show: function() {
		this.node.style.display = "";
		this.node.style.opacity = this.opacity;
		this.isVisible = true;
	},
	
	showOpaque: function() {
		this.node.style.opacity = 1.0;
	},
	
	showTransparent: function() {
		this.node.style.opacity = this.opacity;
	},
	
	prepareToShow: function() {
		this.willShow = true;
	},

	prepareToHide: function() {
		this.willShow = false;
		this.hide();
	},

	setPosition: function(x, y) {
		this.node.style.left = x + "px";
		this.node.style.top = y + "px";
	},
	
	setLevel: function(level) {
		if(level==0) this.opacity = 0.5;
		else if(level==1) this.opacity = 0.2;
		//else if(level==2) this.opacity = 0.1;
		else this.opacity = 0.0;
	},
	
	setChildWidth: function(width) {
		this.childNode.style.width = width + "px";
	},

	reset: function(evt) {
		// Delete the timeout for hiding
		window.clearTimeout( this.resetTimer )
		this.resetTimer = window.setTimeout( this.doReset.bind(this), 100)
		
		if(this.option.resetcallback) {
			this.option.arguments.push(evt);
			var state = this.option.resetcallback.apply(this, this.option.arguments);
			this.option.arguments.remove(evt);
		}
	},
	
	doReset: function() {
		
		if(this.node.hasClassName('Oryx_down'))
			this.node.removeClassName('Oryx_down');

		if(this.node.hasClassName('Oryx_hover'))
			this.node.removeClassName('Oryx_hover');

	},

	activate: function(evt) {
		this.node.addClassName('Oryx_down');
		//Event.stop(evt);
		this.dragStart = true;
	},

	isHover: function() {
		return this.node.hasClassName('Oryx_hover') ? true: false;
	},

	hover: function(evt) {
		// Delete the timeout for hiding
		window.clearTimeout( this.resetTimer )
		this.resetTimer = null;
		
		this.node.addClassName('Oryx_hover');
		this.dragStart = false;
		
		if(this.option.hovercallback) {
			this.option.arguments.push(evt);
			var state = this.option.hovercallback.apply(this, this.option.arguments);
			this.option.arguments.remove(evt);
		}
	},

	move: function(evt) {
		if(this.dragStart && this.option.dragcallback) {
			this.option.arguments.push(evt);
			var state = this.option.dragcallback.apply(this, this.option.arguments);
			this.option.arguments.remove(evt);
		}
	},

	trigger: function(evt) {
		if(this.option.callback) {
			//Event.stop(evt);
			this.option.arguments.push(evt);
			var state = this.option.callback.apply(this, this.option.arguments);
			this.option.arguments.remove(evt);
		}
		this.dragStart = false;
	},

	toString: function() {
		return "HTML-Button " + this.id;
	}
}
ORYX.Plugins.ShapeMenuButton = Clazz.extend(ORYX.Plugins.ShapeMenuButton);

//create command for undo/redo
ORYX.Plugins.ShapeMenuPlugin.CreateCommand = ORYX.Core.Command.extend({
	construct: function(option, currentReference, position, plugin){
		this.option = option;
		this.currentReference = currentReference;
		this.position = position;
		this.plugin = plugin;
		this.shape;
		this.edge;
		this.targetRefPos;
		this.sourceRefPos;
		/*
		 * clone options parameters
		 */
        this.connectedShape = option.connectedShape;
        this.connectingType = option.connectingType;
        this.namespace = option.namespace;
        this.type = option.type;
        this.containedStencil = option.containedStencil;
        this.parent = option.parent;
        this.currentReference = currentReference;
        this.shapeOptions = option.shapeOptions;
	},			
	execute: function(){
		
		var resume = false;
		
		if (this.shape) {
			if (this.shape instanceof ORYX.Core.Node) {
				this.parent.add(this.shape);
				if (this.edge) {
					this.plugin.facade.getCanvas().add(this.edge);
					this.edge.dockers.first().setDockedShape(this.connectedShape);
					this.edge.dockers.first().setReferencePoint(this.sourceRefPos);
					this.edge.dockers.last().setDockedShape(this.shape);
					this.edge.dockers.last().setReferencePoint(this.targetRefPos);
				}
				
				this.plugin.facade.setSelection([this.shape]);
				
			} else if (this.shape instanceof ORYX.Core.Edge) {
				this.plugin.facade.getCanvas().add(this.shape);
				this.shape.dockers.first().setDockedShape(this.connectedShape);
				this.shape.dockers.first().setReferencePoint(this.sourceRefPos);
			}
			resume = true;
		}
		else {
			this.shape = this.plugin.facade.createShape(this.option);
			this.edge = (!(this.shape instanceof ORYX.Core.Edge)) ? this.shape.getIncomingShapes().first() : undefined;
		}
		
		if (this.currentReference && this.position) {
			
			if (this.shape instanceof ORYX.Core.Edge) {
			
				if (!(this.currentReference instanceof ORYX.Core.Canvas)) {
					this.shape.dockers.last().setDockedShape(this.currentReference);
					
					// @deprecated It now uses simply the midpoint
					var upL = this.currentReference.absoluteXY();
					var refPos = {
						x: this.position.x - upL.x,
						y: this.position.y - upL.y
					};
					
					this.shape.dockers.last().setReferencePoint(this.currentReference.bounds.midPoint());
				}
				else {
					this.shape.dockers.last().bounds.centerMoveTo(this.position);
					//this.shape.dockers.last().update();
				}
				this.sourceRefPos = this.shape.dockers.first().referencePoint;
				this.targetRefPos = this.shape.dockers.last().referencePoint;
				
			} else if (this.edge){
				this.sourceRefPos = this.edge.dockers.first().referencePoint;
				this.targetRefPos = this.edge.dockers.last().referencePoint;
			}
		} else {
			var containedStencil = this.containedStencil;
			var connectedShape = this.connectedShape;
			var bc = connectedShape.bounds;
			var bs = this.shape.bounds;
			
			var pos = bc.center();
			if(containedStencil.defaultAlign()==="north") {
				pos.y -= (bc.height() / 2) + ORYX.CONFIG.SHAPEMENU_CREATE_OFFSET + (bs.height()/2);
			} else if(containedStencil.defaultAlign()==="northeast") {
				pos.x += (bc.width() / 2) + ORYX.CONFIG.SHAPEMENU_CREATE_OFFSET_CORNER + (bs.width()/2);
				pos.y -= (bc.height() / 2) + ORYX.CONFIG.SHAPEMENU_CREATE_OFFSET_CORNER + (bs.height()/2);
			} else if(containedStencil.defaultAlign()==="southeast") {
				pos.x += (bc.width() / 2) + ORYX.CONFIG.SHAPEMENU_CREATE_OFFSET_CORNER + (bs.width()/2);
				pos.y += (bc.height() / 2) + ORYX.CONFIG.SHAPEMENU_CREATE_OFFSET_CORNER + (bs.height()/2);
			} else if(containedStencil.defaultAlign()==="south") {
				pos.y += (bc.height() / 2) + ORYX.CONFIG.SHAPEMENU_CREATE_OFFSET + (bs.height()/2);
			} else if(containedStencil.defaultAlign()==="southwest") {
				pos.x -= (bc.width() / 2) + ORYX.CONFIG.SHAPEMENU_CREATE_OFFSET_CORNER + (bs.width()/2);
				pos.y += (bc.height() / 2) + ORYX.CONFIG.SHAPEMENU_CREATE_OFFSET_CORNER + (bs.height()/2);
			} else if(containedStencil.defaultAlign()==="west") {
				pos.x -= (bc.width() / 2) + ORYX.CONFIG.SHAPEMENU_CREATE_OFFSET + (bs.width()/2);
			} else if(containedStencil.defaultAlign()==="northwest") {
				pos.x -= (bc.width() / 2) + ORYX.CONFIG.SHAPEMENU_CREATE_OFFSET_CORNER + (bs.width()/2);
				pos.y -= (bc.height() / 2) + ORYX.CONFIG.SHAPEMENU_CREATE_OFFSET_CORNER + (bs.height()/2);
			} else {
				pos.x += (bc.width() / 2) + ORYX.CONFIG.SHAPEMENU_CREATE_OFFSET + (bs.width()/2);
			}
			
			// Move shape to the new position
			this.shape.bounds.centerMoveTo(pos);
			
			// Move all dockers of a node to the position
			if (this.shape instanceof ORYX.Core.Node){
				(this.shape.dockers||[]).each(function(docker){
					docker.bounds.centerMoveTo(pos);
				})
			}
			
			//this.shape.update();
			this.position = pos;
			
			if (this.edge){
				this.sourceRefPos = this.edge.dockers.first().referencePoint;
				this.targetRefPos = this.edge.dockers.last().referencePoint;
			}
		}
		
		this.plugin.facade.getCanvas().update();
		this.plugin.facade.updateSelection();
		
		if (!resume) {
			// If there is a connected shape
			if (this.edge){
				// Try to layout it
				this.plugin.doLayout(this.edge);
			} else if (this.shape instanceof ORYX.Core.Edge){
				// Try to layout it
				this.plugin.doLayout(this.shape);
			}
		}

	},
	rollback: function(){
		this.plugin.facade.deleteShape(this.shape);
		if(this.edge) {
			this.plugin.facade.deleteShape(this.edge);
		}
		//this.currentParent.update();
		this.plugin.facade.setSelection(this.plugin.facade.getSelection().without(this.shape, this.edge));
	}
});