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

/**
 * Init namespaces
 */
if(!ORYX) {var ORYX = {};}
if(!ORYX.Core) {ORYX.Core = {};}


new function(){
	
	ORYX.Core.UIEnableDrag = function(event, uiObj, option) {
	
		this.uiObj = uiObj;
		var upL = uiObj.bounds.upperLeft();
	
		var a = uiObj.node.getScreenCTM();
		this.faktorXY= {x: a.a, y: a.d};
		
		this.scrollNode = uiObj.node.ownerSVGElement.parentNode.parentNode;
		
		this.offSetPosition =  {
			x: Event.pointerX(event) - (upL.x * this.faktorXY.x),
			y: Event.pointerY(event) - (upL.y * this.faktorXY.y)};
	
		this.offsetScroll	= {x:this.scrollNode.scrollLeft,y:this.scrollNode.scrollTop};
			
		this.dragCallback = ORYX.Core.UIDragCallback.bind(this);
		this.disableCallback = ORYX.Core.UIDisableDrag.bind(this);
	
		this.movedCallback = option ? option.movedCallback : undefined;
		this.upCallback = option ? option.upCallback : undefined;
		
		document.documentElement.addEventListener(ORYX.CONFIG.EVENT_MOUSEUP, this.disableCallback, true);
		document.documentElement.addEventListener(ORYX.CONFIG.EVENT_MOUSEMOVE, 	this.dragCallback , false);
	
	};
	
	ORYX.Core.UIDragCallback = function(event) {
	
		var position = {
			x: Event.pointerX(event) - this.offSetPosition.x,
			y: Event.pointerY(event) - this.offSetPosition.y}
	
		position.x 	-= this.offsetScroll.x - this.scrollNode.scrollLeft; 
		position.y 	-= this.offsetScroll.y - this.scrollNode.scrollTop;
	
		position.x /= this.faktorXY.x;
		position.y /= this.faktorXY.y;
	
		this.uiObj.bounds.moveTo(position);
		//this.uiObj.update();
	
		if(this.movedCallback)
			this.movedCallback(event);
		
		//Event.stop(event);
	
	};
	
	ORYX.Core.UIDisableDrag = function(event) {
		document.documentElement.removeEventListener(ORYX.CONFIG.EVENT_MOUSEMOVE, this.dragCallback, false);
		document.documentElement.removeEventListener(ORYX.CONFIG.EVENT_MOUSEUP, this.disableCallback, true);
		
		if(this.upCallback)
			this.upCallback(event);
			
		this.upCallback = undefined;
		this.movedCallback = undefined;		
		
		Event.stop(event);	
	};



	
	/**
	 * Implements a command to move docker by an offset.
	 * 
	 * @class ORYX.Core.MoveDockersCommand
	 * @param {Object} object An object with the docker id as key and docker and offset as object value
	 * 
	 */	
	ORYX.Core.MoveDockersCommand = ORYX.Core.Command.extend({
		construct: function(dockers){
			this.dockers 	= $H(dockers);
			this.edges 		= $H({});
		},
		execute: function(){
			if (this.changes) {
				this.executeAgain();
				return;
			} else {
				this.changes = $H({});
			}
			
			this.dockers.values().each(function(docker){
				var edge = docker.docker.parent;
				if (!edge){ return }
				
				if (!this.changes[edge.getId()]) {
					this.changes[edge.getId()] = {
						edge				: edge,
						oldDockerPositions	: edge.dockers.map(function(r){ return r.bounds.center() })
					}
				}
				docker.docker.bounds.moveBy(docker.offset);
				this.edges[edge.getId()] = edge;
				docker.docker.update();
			}.bind(this));
			this.edges.each(function(edge){
				this.updateEdge(edge.value);
				if (this.changes[edge.value.getId()])
					this.changes[edge.value.getId()].dockerPositions = edge.value.dockers.map(function(r){ return r.bounds.center() })
			}.bind(this));
		},
		updateEdge: function(edge){
			edge._update(true);
			[edge.getOutgoingShapes(), edge.getIncomingShapes()].flatten().invoke("_update", [true])
		},
		executeAgain: function(){
			this.changes.values().each(function(change){
				// Reset the dockers
				this.removeAllDocker(change.edge);
				change.dockerPositions.each(function(pos, i){	
					if (i==0||i==change.dockerPositions.length-1){ return }					
					var docker = change.edge.createDocker(undefined, pos);
					docker.bounds.centerMoveTo(pos);
					docker.update();
				}.bind(this));
				this.updateEdge(change.edge);
			}.bind(this));
		},
		rollback: function(){
			this.changes.values().each(function(change){
				// Reset the dockers
				this.removeAllDocker(change.edge);
				change.oldDockerPositions.each(function(pos, i){	
					if (i==0||i==change.oldDockerPositions.length-1){ return }					
					var docker = change.edge.createDocker(undefined, pos);
					docker.bounds.centerMoveTo(pos);
					docker.update();
				}.bind(this));
				this.updateEdge(change.edge);
			}.bind(this));
		},
		removeAllDocker: function(edge){
			edge.dockers.slice(1, edge.dockers.length-1).each(function(docker){
				edge.removeDocker(docker);
			})
		}
	});
	
}();
