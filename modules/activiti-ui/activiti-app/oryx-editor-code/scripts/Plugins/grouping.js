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

if(!ORYX.Plugins)
	ORYX.Plugins = new Object();

ORYX.Plugins.Grouping = Clazz.extend({

	facade: undefined,

	construct: function(facade) {
		this.facade = facade;

		this.facade.offer({
			'name':ORYX.I18N.Grouping.group,
			'functionality': this.createGroup.bind(this),
			'group': ORYX.I18N.Grouping.grouping,
			'icon': ORYX.PATH + "images/shape_group.png",
			'description': ORYX.I18N.Grouping.groupDesc,
			'index': 1,
			'minShape': 2,
			'isEnabled': this.isEnabled.bind(this, false)});

		this.facade.offer({
			'name':ORYX.I18N.Grouping.ungroup,
			'functionality': this.deleteGroup.bind(this),
			'group': ORYX.I18N.Grouping.grouping,
			'icon': ORYX.PATH + "images/shape_ungroup.png",
			'description': ORYX.I18N.Grouping.ungroupDesc,
			'index': 2,
			'minShape': 2,
			'isEnabled': this.isEnabled.bind(this, true)});
			
		this.selectedElements = [];
		this.groups = [];
	},

	isEnabled: function(handles) {
		
		var selectedEl = this.selectedElements;

		return	handles === this.groups.any(function(group) {
					return 		group.length === selectedEl.length &&
								group.all(function(grEl) { return selectedEl.member(grEl)})
								});
	},

	onSelectionChanged: function(event) {

		// Get the new selection
		var newSelection = event.elements;
		
		// Find all groups with these selection
		this.selectedElements = this.groups.findAll(function(group) {
				return group.any(function(grEl) { return newSelection.member(grEl)})
		});
		
		// Add the selection to them
		this.selectedElements.push(newSelection)
		
		// Do all in one level and unique
		this.selectedElements = this.selectedElements.flatten().uniq();
		
		// If there are more element, set new selection in the editor
		if(this.selectedElements.length !== newSelection.length) {
			this.facade.setSelection(this.selectedElements);
		}
	},
	
	createGroup: function() {
	
		var selectedElements = this.facade.getSelection();
		
		var commandClass = ORYX.Core.Command.extend({
			construct: function(selectedElements, groups, setGroupsCB, facade){
				this.selectedElements = selectedElements;
				this.groups = groups;
				this.callback = setGroupsCB;
				this.facade = facade;
			},			
			execute: function(){
				var g = this.groups.findAll(function(group) {
					return !group.any(function(grEl) { return selectedElements.member(grEl)})
				});
				
				g.push(selectedElements);

				this.callback(g.clone());
				
				this.facade.setSelection(this.selectedElements);
			},
			rollback: function(){
				this.callback(this.groups.clone());
				
				this.facade.setSelection(this.selectedElements);
			}
		})
		
		var command = new commandClass(selectedElements, this.groups.clone(), this.setGroups.bind(this), this.facade);
		
		this.facade.executeCommands([command]);
	},
	
	deleteGroup: function() {
		
		var selectedElements = this.facade.getSelection();
		
		var commandClass = ORYX.Core.Command.extend({
			construct: function(selectedElements, groups, setGroupsCB, facade){
				this.selectedElements = selectedElements;
				this.groups = groups;
				this.callback = setGroupsCB;
				this.facade = facade;
			},			
			execute: function(){
				// Delete all groups where all these elements are member and where the elements length the same
				var groupPartition = this.groups.partition(function(group) {
						return 		group.length !== selectedElements.length ||
									!group.all(function(grEl) { return selectedElements.member(grEl)})
					});

				this.callback(groupPartition[0]);
				
				this.facade.setSelection(this.selectedElements);
			},
			rollback: function(){
				this.callback(this.groups.clone());
				
				this.facade.setSelection(this.selectedElements);
			}
		})
		
		var command = new commandClass(selectedElements, this.groups.clone(), this.setGroups.bind(this), this.facade);
		
		this.facade.executeCommands([command]);	
	},
	
	setGroups: function(groups) {
		this.groups = groups;
	}

});
