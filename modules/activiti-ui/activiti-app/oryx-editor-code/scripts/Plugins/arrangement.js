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

Array.prototype.insertFrom = function(from, to){
	to 			= Math.max(0, to);
	from 		= Math.min( Math.max(0, from), this.length-1 );
		
	var el 		= this[from];
	var old 	= this.without(el);
	var newA 	= old.slice(0, to);
	newA.push(el);
	if(old.length > to ){
		newA 	= newA.concat(old.slice(to))
	};
	return newA;
}

if(!ORYX.Plugins)
	ORYX.Plugins = new Object();

ORYX.Plugins.Arrangement = ORYX.Plugins.AbstractPlugin.extend({

	facade: undefined,

	construct: function(facade) {
		this.facade = facade;

		// Z-Ordering
		/** Hide for SIGNAVIO 
		
		this.facade.offer({
			'name':ORYX.I18N.Arrangement.btf,
			'functionality': this.setZLevel.bind(this, this.setToTop),
			'group': ORYX.I18N.Arrangement.groupZ,
			'icon': ORYX.PATH + "images/shape_move_front.png",
			'description': ORYX.I18N.Arrangement.btfDesc,
			'index': 1,
			'minShape': 1});
			
		this.facade.offer({
			'name':ORYX.I18N.Arrangement.btb,
			'functionality': this.setZLevel.bind(this, this.setToBack),
			'group': ORYX.I18N.Arrangement.groupZ,
			'icon': ORYX.PATH + "images/shape_move_back.png",
			'description': ORYX.I18N.Arrangement.btbDesc,
			'index': 2,
			'minShape': 1});

		this.facade.offer({
			'name':ORYX.I18N.Arrangement.bf,
			'functionality': this.setZLevel.bind(this, this.setForward),
			'group': ORYX.I18N.Arrangement.groupZ,
			'icon': ORYX.PATH + "images/shape_move_forwards.png",
			'description': ORYX.I18N.Arrangement.bfDesc,
			'index': 3,
			'minShape': 1});

		this.facade.offer({
			'name':ORYX.I18N.Arrangement.bb,
			'functionality': this.setZLevel.bind(this, this.setBackward),
			'group': ORYX.I18N.Arrangement.groupZ,
			'icon': ORYX.PATH + "images/shape_move_backwards.png",
			'description': ORYX.I18N.Arrangement.bbDesc,
			'index': 4,
			'minShape': 1});

		// Aligment
		this.facade.offer({
			'name':ORYX.I18N.Arrangement.ab,
			'functionality': this.alignShapes.bind(this, [ORYX.CONFIG.EDITOR_ALIGN_BOTTOM]),
			'group': ORYX.I18N.Arrangement.groupA,
			'icon': ORYX.PATH + "images/shape_align_bottom.png",
			'description': ORYX.I18N.Arrangement.abDesc,
			'index': 1,
			'minShape': 2});



		this.facade.offer({
			'name':ORYX.I18N.Arrangement.at,
			'functionality': this.alignShapes.bind(this, [ORYX.CONFIG.EDITOR_ALIGN_TOP]),
			'group': ORYX.I18N.Arrangement.groupA,
			'icon': ORYX.PATH + "images/shape_align_top.png",
			'description': ORYX.I18N.Arrangement.atDesc,
			'index': 3,
			'minShape': 2});

		this.facade.offer({
			'name':ORYX.I18N.Arrangement.al,
			'functionality': this.alignShapes.bind(this, [ORYX.CONFIG.EDITOR_ALIGN_LEFT]),
			'group': ORYX.I18N.Arrangement.groupA,
			'icon': ORYX.PATH + "images/shape_align_left.png",
			'description': ORYX.I18N.Arrangement.alDesc,
			'index': 4,
			'minShape': 2});

		this.facade.offer({
			'name':ORYX.I18N.Arrangement.ar,
			'functionality': this.alignShapes.bind(this, [ORYX.CONFIG.EDITOR_ALIGN_RIGHT]),
			'group': ORYX.I18N.Arrangement.groupA,
			'icon': ORYX.PATH + "images/shape_align_right.png",
			'description': ORYX.I18N.Arrangement.arDesc,
			'index': 6,
			'minShape': 2});

		**/
		
		this.facade.offer({
			'name':ORYX.I18N.Arrangement.am,
			'functionality': this.alignShapes.bind(this, [ORYX.CONFIG.EDITOR_ALIGN_MIDDLE]),
			'group': ORYX.I18N.Arrangement.groupA,
			'icon': ORYX.PATH + "images/shape_align_middle.png",
			'description': ORYX.I18N.Arrangement.amDesc,
			'index': 1,
			'minShape': 2});
			
		this.facade.offer({
			'name':ORYX.I18N.Arrangement.ac,
			'functionality': this.alignShapes.bind(this, [ORYX.CONFIG.EDITOR_ALIGN_CENTER]),
			'group': ORYX.I18N.Arrangement.groupA,
			'icon': ORYX.PATH + "images/shape_align_center.png",
			'description': ORYX.I18N.Arrangement.acDesc,
			'index': 2,
			'minShape': 2});

			
		this.facade.offer({
			'name':ORYX.I18N.Arrangement.as,
			'functionality': this.alignShapes.bind(this, [ORYX.CONFIG.EDITOR_ALIGN_SIZE]),
			'group': ORYX.I18N.Arrangement.groupA,
			'icon': ORYX.PATH + "images/shape_align_size.png",
			'description': ORYX.I18N.Arrangement.asDesc,
			'index': 3,
			'minShape': 2});
			


		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_ARRANGEMENT_TOP, 	this.setZLevel.bind(this, this.setToTop)	);
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_ARRANGEMENT_BACK, 	this.setZLevel.bind(this, this.setToBack)	);
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_ARRANGEMENT_FORWARD, 	this.setZLevel.bind(this, this.setForward)	);
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_ARRANGEMENT_BACKWARD, 	this.setZLevel.bind(this, this.setBackward)	);						

	
	},
	
	onSelectionChanged: function(elemnt){
		var selection = this.facade.getSelection();
		if (selection.length === 1 && selection[0] instanceof ORYX.Core.Edge) {
			this.setToTop(selection);
		}
	},
	
	setZLevel:function(callback, event){
			
		//Command-Pattern for dragging one docker
		var zLevelCommand = ORYX.Core.Command.extend({
			construct: function(callback, elements, facade){
				this.callback 	= callback;
				this.elements 	= elements;
				// For redo, the previous elements get stored
				this.elAndIndex	= elements.map(function(el){ return {el:el, previous:el.parent.children[el.parent.children.indexOf(el)-1]} })
				this.facade		= facade;
			},			
			execute: function(){
				
				// Call the defined z-order callback with the elements
				this.callback( this.elements )			
				this.facade.setSelection( this.elements )
			},
			rollback: function(){
				
				// Sort all elements on the index of there containment
				var sortedEl =	this.elAndIndex.sortBy( function( el ) {
									var value 	= el.el;
									var t 		= $A(value.node.parentNode.childNodes);
									return t.indexOf(value.node);
								}); 
				
				// Every element get setted back bevor the old previous element
				for(var i=0; i<sortedEl.length; i++){
					var el			= sortedEl[i].el;
					var p 			= el.parent;			
					var oldIndex 	= p.children.indexOf(el);
					var newIndex 	= p.children.indexOf(sortedEl[i].previous);
					newIndex		= newIndex || 0
					p.children 	= p.children.insertFrom(oldIndex, newIndex)			
					el.node.parentNode.insertBefore(el.node, el.node.parentNode.childNodes[newIndex+1]);
				}

				// Reset the selection
				this.facade.setSelection( this.elements )
			}
		});
	
		// Instanziate the dockCommand
		var command = new zLevelCommand(callback, this.facade.getSelection(), this.facade);
		if( event.excludeCommand ){
			command.execute();
		} else {
			this.facade.executeCommands( [command] );	
		}
		
	},

	setToTop: function(elements) {

		// Sortieren des Arrays nach dem Index des SVGKnotens im Bezug auf dem Elternknoten.
		var tmpElem =  elements.sortBy( function(value, index) {
			var t = $A(value.node.parentNode.childNodes);
			return t.indexOf(value.node);
		});
		// Sortiertes Array wird nach oben verschoben.
		tmpElem.each( function(value) {
			var p = value.parent;
			if (p.children.last() === value){
				return;
			}
			p.children = p.children.without( value )
			p.children.push(value);
			value.node.parentNode.appendChild(value.node);			
		});
	},

	setToBack: function(elements) {
		// Sortieren des Arrays nach dem Index des SVGKnotens im Bezug auf dem Elternknoten.
		var tmpElem =  elements.sortBy( function(value, index) {
			var t = $A(value.node.parentNode.childNodes);
			return t.indexOf(value.node);
		});

		tmpElem = tmpElem.reverse();

		// Sortiertes Array wird nach unten verschoben.
		tmpElem.each( function(value) {
			var p = value.parent
			p.children = p.children.without( value )
			p.children.unshift( value );
			value.node.parentNode.insertBefore(value.node, value.node.parentNode.firstChild);
		});
		
		
	},

	setBackward: function(elements) {
		// Sortieren des Arrays nach dem Index des SVGKnotens im Bezug auf dem Elternknoten.
		var tmpElem =  elements.sortBy( function(value, index) {
			var t = $A(value.node.parentNode.childNodes);
			return t.indexOf(value.node);
		});

		// Reverse the elements
		tmpElem = tmpElem.reverse();
		
		// Delete all Nodes who are the next Node in the nodes-Array
		var compactElem = tmpElem.findAll(function(el) {return !tmpElem.some(function(checkedEl){ return checkedEl.node == el.node.previousSibling})});
		
		// Sortiertes Array wird nach eine Ebene nach oben verschoben.
		compactElem.each( function(el) {
			if(el.node.previousSibling === null) { return; }
			var p 		= el.parent;			
			var index 	= p.children.indexOf(el);
			p.children 	= p.children.insertFrom(index, index-1)			
			el.node.parentNode.insertBefore(el.node, el.node.previousSibling);
		});
		
		
	},

	setForward: function(elements) {
		// Sortieren des Arrays nach dem Index des SVGKnotens im Bezug auf dem Elternknoten.
		var tmpElem =  elements.sortBy( function(value, index) {
			var t = $A(value.node.parentNode.childNodes);
			return t.indexOf(value.node);
		});


		// Delete all Nodes who are the next Node in the nodes-Array
		var compactElem = tmpElem.findAll(function(el) {return !tmpElem.some(function(checkedEl){ return checkedEl.node == el.node.nextSibling})});
	
			
		// Sortiertes Array wird eine Ebene nach unten verschoben.
		compactElem.each( function(el) {
			var nextNode = el.node.nextSibling		
			if(nextNode === null) { return; }
			var index 	= el.parent.children.indexOf(el);
			var p 		= el.parent;
			p.children 	= p.children.insertFrom(index, index+1)			
			el.node.parentNode.insertBefore(nextNode, el.node);
		});
	},


	alignShapes: function(way) {

		var elements = this.facade.getSelection();

		// Set the elements to all Top-Level elements
		elements = this.facade.getCanvas().getShapesWithSharedParent(elements);
		// Get only nodes
		elements = elements.findAll(function(value) {
			return (value instanceof ORYX.Core.Node)
		});
		// Delete all attached intermediate events from the array
		elements = elements.findAll(function(value) {
			var d = value.getIncomingShapes()
			return d.length == 0 || !elements.include(d[0])
		});
		if(elements.length < 2) { return; }

		// get bounds of all shapes.
		var bounds = elements[0].absoluteBounds().clone();
		elements.each(function(shape) {
		        bounds.include(shape.absoluteBounds().clone());
		});
		
		// get biggest width and heigth
		var maxWidth = 0;
		var maxHeight = 0;
		elements.each(function(shape){
			maxWidth = Math.max(shape.bounds.width(), maxWidth);
			maxHeight = Math.max(shape.bounds.height(), maxHeight);
		});

		var commandClass = ORYX.Core.Command.extend({
			construct: function(elements, bounds, maxHeight, maxWidth, way, plugin){
				this.elements = elements;
				this.bounds = bounds;
				this.maxHeight = maxHeight;
				this.maxWidth = maxWidth;
				this.way = way;
				this.facade = plugin.facade;
				this.plugin = plugin;
				this.orgPos = [];
			},
			setBounds: function(shape, maxSize) {
				if(!maxSize)
					maxSize = {width: ORYX.CONFIG.MAXIMUM_SIZE, height: ORYX.CONFIG.MAXIMUM_SIZE};

				if(!shape.bounds) { throw "Bounds not definined." }
				
				var newBounds = {
                    a: {x: shape.bounds.upperLeft().x - (this.maxWidth - shape.bounds.width())/2,
                        y: shape.bounds.upperLeft().y - (this.maxHeight - shape.bounds.height())/2},
                    b: {x: shape.bounds.lowerRight().x + (this.maxWidth - shape.bounds.width())/2,
                        y: shape.bounds.lowerRight().y + (this.maxHeight - shape.bounds.height())/2}
	            }
				
				/* If the new width of shape exceeds the maximum width, set width value to maximum. */
				if(this.maxWidth > maxSize.width) {
					newBounds.a.x = shape.bounds.upperLeft().x - 
									(maxSize.width - shape.bounds.width())/2;
					
					newBounds.b.x =	shape.bounds.lowerRight().x + (maxSize.width - shape.bounds.width())/2
				}
				
				/* If the new height of shape exceeds the maximum height, set height value to maximum. */
				if(this.maxHeight > maxSize.height) {
					newBounds.a.y = shape.bounds.upperLeft().y - 
									(maxSize.height - shape.bounds.height())/2;
					
					newBounds.b.y =	shape.bounds.lowerRight().y + (maxSize.height - shape.bounds.height())/2
				}
				
				/* set bounds of shape */
				shape.bounds.set(newBounds);
				
			},			
			execute: function(){
				// align each shape according to the way that was specified.
				this.elements.each(function(shape, index) {
					this.orgPos[index] = shape.bounds.upperLeft();
					
					var relBounds = this.bounds.clone();
					var newCoordinates;
					if (shape.parent && !(shape.parent instanceof ORYX.Core.Canvas) ) {
						var upL = shape.parent.absoluteBounds().upperLeft();
						relBounds.moveBy(-upL.x, -upL.y);
					}
					
					switch (this.way) {
						// align the shapes in the requested way.
						case ORYX.CONFIG.EDITOR_ALIGN_BOTTOM:
			                newCoordinates = {
								x: shape.bounds.upperLeft().x,
								y: relBounds.b.y - shape.bounds.height()
							}; break;
		
				        case ORYX.CONFIG.EDITOR_ALIGN_MIDDLE:
			                newCoordinates = {
								x: shape.bounds.upperLeft().x,
								y: (relBounds.a.y + relBounds.b.y - shape.bounds.height()) / 2
							}; break;
		
				        case ORYX.CONFIG.EDITOR_ALIGN_TOP:
			                newCoordinates = {
								x: shape.bounds.upperLeft().x,
								y: relBounds.a.y
							}; break;
		
				        case ORYX.CONFIG.EDITOR_ALIGN_LEFT:
			                newCoordinates = {
								x: relBounds.a.x,
								y: shape.bounds.upperLeft().y
							}; break;
		
				        case ORYX.CONFIG.EDITOR_ALIGN_CENTER:
			                newCoordinates = {
								x: (relBounds.a.x + relBounds.b.x - shape.bounds.width()) / 2,
								y: shape.bounds.upperLeft().y
							}; break;
		
				        case ORYX.CONFIG.EDITOR_ALIGN_RIGHT:
			                newCoordinates = {
								x: relBounds.b.x - shape.bounds.width(),
								y: shape.bounds.upperLeft().y
							}; break;
							
						case ORYX.CONFIG.EDITOR_ALIGN_SIZE:
							if(shape.isResizable) {
								this.orgPos[index] = {a: shape.bounds.upperLeft(), b: shape.bounds.lowerRight()};
								this.setBounds(shape, shape.maximumSize);
							}
							break;
					}
					
					if (newCoordinates){
						var offset =  {
							x: shape.bounds.upperLeft().x - newCoordinates.x,
							y: shape.bounds.upperLeft().y - newCoordinates.y
						}
						// Set the new position
						shape.bounds.moveTo(newCoordinates);
						this.plugin.layoutEdges(shape, shape.getAllDockedShapes(),offset);
						//shape.update()
					}			
				}.bind(this));
		
				//this.facade.getCanvas().update();
				//this.facade.updateSelection();
			},
			rollback: function(){
				this.elements.each(function(shape, index) {
					if (this.way == ORYX.CONFIG.EDITOR_ALIGN_SIZE) {
						if(shape.isResizable) {shape.bounds.set(this.orgPos[index]);}
					} else {shape.bounds.moveTo(this.orgPos[index]);}
				}.bind(this));
				
				//this.facade.getCanvas().update();
				//this.facade.updateSelection();
			}
		})
		
		var command = new commandClass(elements, bounds, maxHeight, maxWidth, parseInt(way), this);
		
		this.facade.executeCommands([command]);	
	}
});