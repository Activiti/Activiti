/**
 * Copyright (c) 2009
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

if (!ORYX) 
	ORYX = new Object();
if (!ORYX.Plugins) 
	ORYX.Plugins = new Object();

ORYX.Plugins.EPCLayouter = ORYX.Plugins.AbstractPlugin.extend({
	facade: undefined,
	construct: function(facade){
		this.facade = facade;
		this.facade.offer({
			'name' : "Layout-EPC",
			'description' : "Layout EPC Model",
			'functionality' : this.layout.bind(this),
			'group' : "Layout",
			'icon' : ORYX.PATH + "images/auto_layout.png",
			'index' : 1,
			'minShape' : 0,
			'maxShape' : 0
		});
	},
	layout: function(){
		
		this.facade.raiseEvent({
            type: ORYX.CONFIG.EVENT_LOADING_ENABLE,
			text: ORYX.I18N.Layouting.doing
        });
		
		
		new Ajax.Request(ORYX.CONFIG.EPC_LAYOUTER, {
			method : 'POST',
			asynchronous : false,
			parameters : {
				data: this.facade.getSerializedJSON()
			},
			onFailure: function(request){
				Ext.Msg.alert("Layouting Error", "Error while layouting:!\n" + request.responseText);
            	this.facade.raiseEvent({type:ORYX.CONFIG.EVENT_LOADING_DISABLE});
			},
			onSuccess: function(request){

				/*Ext.Msg.alert("Oryx", "New Layout arrived:!\n" + request.responseText);*/
				
				var setLayoutCommandClass = ORYX.Core.Command.extend({
					construct: function(layoutArray, plugin){
						this.layoutArray = layoutArray;
						this.plugin = plugin;
						this.oldLayoutArray = [];
					},
					execute: function(){
						this.layoutArray.each(function(elem){
							/* get shape */
							var shape = this.plugin.facade.getCanvas().getChildShapeByResourceId(elem.id);
							
							/* save old layout for undo*/
							var oldLayout = {
								id : elem.id,
								bounds : shape.bounds.clone()
							};
							this.oldLayoutArray.push(oldLayout);
							
							/* set new bounds */
							var bound = elem.bounds.split(" ");
							shape.bounds.set(bound[0],bound[1],bound[2],bound[3]);
							
							/* set new dockers */
							if(elem.dockers != null){
								this.plugin.setDockersBad(shape,elem.dockers);
							}
							
							shape.update();
						}.bind(this));
						
						this.plugin.facade.getCanvas().update();
						this.plugin.facade.updateSelection();					
						
					},
					rollback: function(){
						this.oldLayoutArray.each(function(elem){
							var shape = this.plugin.facade.getCanvas().getChildShapeByResourceId(elem.id);
							shape.bounds.set(elem.bounds);
							shape.update();
						}.bind(this));
						
						this.plugin.facade.getCanvas().update();
						this.plugin.facade.updateSelection();	
					}
				});
				
				
				var resp = request.responseText.evalJSON();
				if (resp instanceof Array && resp.size() > 0) {
					/* create command */
					var command = new setLayoutCommandClass(resp, this);
					/* execute command */
					this.facade.executeCommands([command]);
				}
            	this.facade.raiseEvent({type:ORYX.CONFIG.EVENT_LOADING_DISABLE});
			}.bind(this)
		})
	},
	setDockersBad: function(shape, dockers){
		var dockersString = "";
		dockers.each(function(p){
			dockersString += p.x + " " + p.y + " ";
		});
		dockersString += " # ";
		shape.deserialize([{
								prefix: 'oryx',
								name: 'dockers',
								value: dockersString
							}]);
	},
	setDockersGood: function(shape, dockers){
		if(elem.dockers.length == 1){
			/* docked event */
			
		}else{
			
			/* clear all except of the first and last dockers */
			var dockers = shape.getDockers().slice(1,-1);
			dockers.each(function(docker){
				shape.removeDocker(docker);
			});
			
			/* set first and last docker */
			var firstDocker = shape.getDockers()[0];
			if (firstDocker.getDockedShape()) {
				firstDocker.setReferencePoint(elem.dockers[0]);
			}
			else {
				firstDocker.bounds.moveTo(elem.dockers[0].x,elem.dockers[0].y);
			}
			firstDocker.refresh();
			
			var lastDocker = shape.getDockers()[1];
			if (lastDocker.getDockedShape()) {
				lastDocker.setReferencePoint(elem.dockers[elem.dockers.length - 1]);
			}
			else {
				lastDocker.bounds.moveTo(elem.dockers[elem.dockers.length - 1].x, elem.dockers[elem.dockers.length - 1].y);
			}
			lastDocker.refresh();
			
			/* add new dockers except of the first and last */
			var dockersToAdd = elem.dockers.slice(1,-1);
			dockersToAdd.each(function(dockerPoint){
				var newDocker = shape.createDocker();
				newDocker.parent = shape;
				newDocker.bounds.centerMoveTo(dockerPoint.x, dockerPoint.y);
				/*newDocker.setReferencePoint(dockerPoint);*/
				newDocker.update();
			});
		}		
	}
});
