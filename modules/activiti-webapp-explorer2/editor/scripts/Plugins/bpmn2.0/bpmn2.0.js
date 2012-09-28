/**
 * Copyright (c) 2009
 * Sven Wagner-Boysen, Willi Tscheschner
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

if(!ORYX.Plugins)
	ORYX.Plugins = new Object();

new function(){
	
	ORYX.Plugins.BPMN2_0 = {
	
		/**
		 *	Constructor
		 *	@param {Object} Facade: The Facade of the Editor
		 */
		construct: function(facade){
			this.facade = facade;
			
			this.facade.registerOnEvent(ORYX.CONFIG.EVENT_DRAGDOCKER_DOCKED, this.handleDockerDocked.bind(this));
			this.facade.registerOnEvent(ORYX.CONFIG.EVENT_PROPWINDOW_PROP_CHANGED, this.handlePropertyChanged.bind(this));
			this.facade.registerOnEvent('layout.bpmn2_0.subprocess', this.handleSubProcess.bind(this));
			this.facade.registerOnEvent(ORYX.CONFIG.EVENT_SHAPEREMOVED, this.handleShapeRemove.bind(this));
			
			this.facade.registerOnEvent(ORYX.CONFIG.EVENT_LOADED, this.afterLoad.bind(this));
			

			this.namespace = undefined;
		},
		
		hashedSubProcesses: {},
		
		hashChildShapes: function(shape){
			var children = shape.getChildNodes();
			children.each(function(child){
				if (this.hashedSubProcesses[child.id]){
					this.hashedSubProcesses[child.id] = child.absoluteXY();
					this.hashedSubProcesses[child.id].width 	= child.bounds.width();
					this.hashedSubProcesses[child.id].height 	= child.bounds.height();
					this.hashChildShapes(child);
				}
			}.bind(this));
		},
	
		/**
		 * Handle the layouting of a sub process.
		 * Mainly to adjust the child dockers of a sub process. 
		 *
		 */
		handleSubProcess : function(option) {
			
			var sh = option.shape;
			
			if (!this.hashedSubProcesses[sh.id]) {
				this.hashedSubProcesses[sh.id] = sh.absoluteXY();
				this.hashedSubProcesses[sh.id].width 	= sh.bounds.width();
				this.hashedSubProcesses[sh.id].height 	= sh.bounds.height();
				return;
			}
			
			var offset = sh.absoluteXY();
			offset.x -= this.hashedSubProcesses[sh.id].x;
			offset.y -= this.hashedSubProcesses[sh.id].y;
			
			var resized = this.hashedSubProcesses[sh.id].width !== sh.bounds.width() || this.hashedSubProcesses[sh.id].height !== sh.bounds.height();
			
			this.hashedSubProcesses[sh.id] = sh.absoluteXY();
			this.hashedSubProcesses[sh.id].width 	= sh.bounds.width();
			this.hashedSubProcesses[sh.id].height 	= sh.bounds.height();
			this.hashChildShapes(sh);
			
			
			// Move dockers only if currently is not resizing
			if (this.facade.isExecutingCommands()&&!resized) {
				this.moveChildDockers(sh, offset);
			}
		},
		
		moveChildDockers: function(shape, offset){
			
			if (!offset.x && !offset.y) {
				return;
			} 
			
			var children = shape.getChildNodes(true);
			
			// Get all nodes
			var dockers = children
				// Get all incoming and outgoing edges
				.map(function(node){
					return [].concat(node.getIncomingShapes())
							.concat(node.getOutgoingShapes())
				})
				// Flatten all including arrays into one
				.flatten()
				// Get every edge only once
				.uniq()
				// Get all dockers
				.map(function(edge){
					return edge.dockers.length > 2 ? 
							edge.dockers.slice(1, edge.dockers.length-1) : 
							[];
				})
				// Flatten the dockers lists
				.flatten();
	
			var abs = shape.absoluteBounds();
			abs.moveBy(-offset.x, -offset.y)
			var obj = {};
			dockers.each(function(docker){
				
				if (docker.isChanged){
					return;
				}
				
				var off = Object.clone(offset);
				
				if (!abs.isIncluded(docker.bounds.center())){
					var index 	= docker.parent.dockers.indexOf(docker);
					var size	= docker.parent.dockers.length;
					var from 	= docker.parent.getSource();
					var to 		= docker.parent.getTarget();
					
					var bothAreIncluded = children.include(from) && children.include(to);
					
					if (!bothAreIncluded){
						var previousIsOver = index !== 0 ? abs.isIncluded(docker.parent.dockers[index-1].bounds.center()) : false;
						var nextIsOver = index !== size-1 ? abs.isIncluded(docker.parent.dockers[index+1].bounds.center()) : false;
						
						if (!previousIsOver && !nextIsOver){ return; }
						
						var ref = docker.parent.dockers[previousIsOver ? index-1 : index+1];
						if (Math.abs(-Math.abs(ref.bounds.center().x-docker.bounds.center().x)) < 2){
							off.y = 0;
						} else if(Math.abs(-Math.abs(ref.bounds.center().y-docker.bounds.center().y)) < 2){
							off.x = 0;
						} else {
							return;
						}
					}
					
				}
				
				obj[docker.getId()] = {
					docker:docker,
					offset:off
				}
			})
			
			// Set dockers
			this.facade.executeCommands([new ORYX.Core.MoveDockersCommand(obj)]);
				
		},
		
		/**
		 * DragDocker.Docked Handler
		 *
		 */	
		handleDockerDocked: function(options) {
			var namespace = this.getNamespace();
			
			var edge = options.parent;
			var edgeSource = options.target;
			
			if(edge.getStencil().id() === namespace + "SequenceFlow") {
				var isGateway = edgeSource.getStencil().groups().find(function(group) {
						if(group == "Gateways") 
							return group;
					});
				if(!isGateway && (edge.properties["oryx-conditiontype"] == "Expression"))
					// show diamond on edge source
					edge.setProperty("oryx-showdiamondmarker", true);
				else 
					// do not show diamond on edge source
					edge.setProperty("oryx-showdiamondmarker", false);
				
				// update edge rendering
				//edge.update();
				
				this.facade.getCanvas().update();
			}
		},
		
		/**
		 * PropertyWindow.PropertyChanged Handler
		 */
		handlePropertyChanged: function(option) {
			var namespace = this.getNamespace();
			
			var shapes = option.elements;
			var propertyKey = option.key;
			var propertyValue = option.value;
			
			var changed = false;
			shapes.each(function(shape){
				if((shape.getStencil().id() === namespace + "SequenceFlow") &&
					(propertyKey === "oryx-conditiontype")) {
					
					if(propertyValue != "Expression")
						// Do not show the Diamond
						shape.setProperty("oryx-showdiamondmarker", false);
					else {
						var incomingShapes = shape.getIncomingShapes();
						
						if(!incomingShapes) {
							shape.setProperty("oryx-showdiamondmarker", true);
						}
						
						var incomingGateway = incomingShapes.find(function(aShape) {
							var foundGateway = aShape.getStencil().groups().find(function(group) {
								if(group == "Gateways") 
									return group;
							});
							if(foundGateway)
								return foundGateway;
						});
						
						if(!incomingGateway) 
							// show diamond on edge source
							shape.setProperty("oryx-showdiamondmarker", true);
						else
							// do not show diamond
							shape.setProperty("oryx-showdiamondmarker", false);
					}
					
					changed = true;
				}
			}.bind(this));
			
			if(changed) {this.facade.getCanvas().update();}
			
		},
		
		hashedPoolPositions : {},
		hashedLaneDepth : {},
		hashedBounds : {},
		hashedPositions: {},
		
		isResized: function(shape, bounds){
			
			if (!bounds||!shape){
				return false;
			}
			
			var oldB = bounds;
			//var oldXY = oldB.upperLeft();
			//var xy = shape.absoluteXY();
			return Math.round(oldB.width() - shape.bounds.width()) !== 0 || Math.round(oldB.height() - shape.bounds.height()) !== 0
		},
		
		getDepth: function(child, parent){
			
			var i=0;
			while(child && child.parent && child !== parent){
				child = child.parent;
				++i
			}
			return i;
		},
		
		updateDepth: function(lane, fromDepth, toDepth){
			
			var xOffset = (fromDepth - toDepth) * 30;
			
			lane.getChildNodes().each(function(shape){
				shape.bounds.moveBy(xOffset, 0);
				
				[].concat(children[j].getIncomingShapes())
						.concat(children[j].getOutgoingShapes())
						
			})
			
		},
		
		moveBy: function(pos, offset){
			pos.x += offset.x;
			pos.y += offset.y;
			return pos;
		},
		
		getHashedBounds: function(shape){
			return this.currentPool && this.hashedBounds[this.currentPool.id][shape.id] ? this.hashedBounds[this.currentPool.id][shape.id] : shape.absoluteBounds();
		},
				
		getNamespace: function() {
			if(!this.namespace) {
				var stencilsets = this.facade.getStencilSets();
				if(stencilsets.keys()) {
					this.namespace = stencilsets.keys()[0];
				} else {
					return undefined;
				}
			}
			return this.namespace;
		}
	};
		
	ORYX.Plugins.BPMN2_0 = ORYX.Plugins.AbstractPlugin.extend(ORYX.Plugins.BPMN2_0);
	
}()	