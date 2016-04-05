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

if (!ORYX.Plugins) {
    ORYX.Plugins = new Object();
}

/**
 * This plugin is responsible for displaying loading indicators and to prevent
 * the user from accidently unloading the page by, e.g., pressing the backspace
 * button and returning to the previous site in history.
 * @param {Object} facade The editor plugin facade to register enhancements with.
 */
ORYX.Plugins.Loading = {

    construct: function(facade){
    
        this.facade = facade;
        
        // The parent Node
        this.node = ORYX.Editor.graft("http://www.w3.org/1999/xhtml", this.facade.getCanvas().getHTMLContainer().parentNode, ['div', {
            'class': 'LoadingIndicator'
        }, '']);
        
        this.facade.registerOnEvent(ORYX.CONFIG.EVENT_LOADING_ENABLE, this.enableLoading.bind(this));
        this.facade.registerOnEvent(ORYX.CONFIG.EVENT_LOADING_DISABLE, this.disableLoading.bind(this));
		this.facade.registerOnEvent(ORYX.CONFIG.EVENT_LOADING_STATUS, this.showStatus.bind(this));
        
        this.disableLoading();
    },
    
    enableLoading: function(options){
		if(options.text) 
			this.node.innerHTML = options.text + "...";
		else
			this.node.innerHTML = ORYX.I18N.Loading.waiting;
		this.node.removeClassName('StatusIndicator');
		this.node.addClassName('LoadingIndicator');
        this.node.style.display = "block";
		
		var pos = this.facade.getCanvas().rootNode.parentNode.parentNode.parentNode.parentNode;

		this.node.style.top 		= pos.offsetTop + 'px';
		this.node.style.left 		= pos.offsetLeft +'px';
					
    },
    
    disableLoading: function(){
        this.node.style.display = "none";
    },
	
	showStatus: function(options) {
		if(options.text) {
			this.node.innerHTML = options.text;
			this.node.addClassName('StatusIndicator');
			this.node.removeClassName('LoadingIndicator');
			this.node.style.display = 'block';

			var pos = this.facade.getCanvas().rootNode.parentNode.parentNode.parentNode.parentNode;

			this.node.style.top 	= pos.offsetTop + 'px';
			this.node.style.left 	= pos.offsetLeft +'px';
												
			var tout = options.timeout ? options.timeout : 2000;
			
			window.setTimeout((function(){
            
                this.disableLoading();
                
            }).bind(this), tout);
		}
		
	}
}

ORYX.Plugins.Loading = Clazz.extend(ORYX.Plugins.Loading);
