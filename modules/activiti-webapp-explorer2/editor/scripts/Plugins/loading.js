/**
 * Copyright (c) 2006
 * Martin Czuchra, Nicolas Peters, Daniel Polak, Willi Tscheschner
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
