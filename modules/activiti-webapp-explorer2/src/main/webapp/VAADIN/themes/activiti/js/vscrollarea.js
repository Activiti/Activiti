var VSA_scrollAreas = new Array();

var VSA_default_imagesPath = "images";
var VSA_default_btnUpImage = "button-up.gif";
var VSA_default_btnDownImage = "button-down.gif";
var VSA_default_scrollStep = 5;
var VSA_default_wheelSensitivity = 10;
var VSA_default_scrollbarPosition = 'right';//'left','right','inline';
var VSA_default_scrollButtonHeight = 11;
var VSA_default_scrollbarWidth = 20;

var VSA_resizeTimer = 2000;

if (window.addEventListener)
	window.addEventListener("load", VSA_initScrollbars, false);
else if (window.attachEvent)
	window.attachEvent("onload", VSA_initScrollbars);

function VSA_initScrollbars()
{
	var scrollElements = VSA_getElements("scrollable", "DIV", document, "class");
	for (var i=0; i<scrollElements.length; i++)
	{
		VSA_scrollAreas[i] = new ScrollArea(i, scrollElements[i]);
	}
}

function ScrollArea(index, elem) //constructor
{
	this.index = index;
	this.element = elem;

	var attr = this.element.getAttribute("imagesPath");
	this.imagesPath = attr ? attr : VSA_default_imagesPath;

	attr = this.element.getAttribute("btnUpImage");
	this.btnUpImage = attr ? attr : VSA_default_btnUpImage;

	attr = this.element.getAttribute("btnDownImage");
	this.btnDownImage = attr ? attr : VSA_default_btnDownImage;

	attr = Number(this.element.getAttribute("scrollStep"));
	this.scrollStep = attr ? attr : VSA_default_scrollStep;

	attr = Number(this.element.getAttribute("wheelSensitivity"));
	this.wheelSensitivity = attr ? attr : VSA_default_wheelSensitivity;

	attr = this.element.getAttribute("scrollbarPosition");
	this.scrollbarPosition = attr ? attr : VSA_default_scrollbarPosition;
	
	attr = this.element.getAttribute("scrollButtonHeight");
	this.scrollButtonHeight = attr ? attr : VSA_default_scrollButtonHeight;

	attr = this.element.getAttribute("scrollbarWidth");
	this.scrollbarWidth = attr ? attr : VSA_default_scrollbarWidth;

	this.scrolling = false;

	this.iOffsetY = 0;
	this.scrollHeight = 0;
	this.scrollContent = null;
	this.scrollbar = null;
	this.scrollup = null;
	this.scrolldown = null;
	this.scrollslider = null;
	this.scroll = null;
	this.enableScrollbar = false;
	this.scrollFactor = 1;
	this.scrollingLimit = 0;
	this.topPosition = 0;

	//functions declaration
	this.init = VSA_init;
	this.scrollUp = VSA_scrollUp;
	this.scrollDown = VSA_scrollDown;
	this.createScrollBar = VSA_createScrollBar;
	this.scrollIt = VSA_scrollIt;

	this.init();
}


function VSA_init() {
	this.scrollContent = document.createElement("DIV");
	this.scrollContent.style.position = "absolute";
	this.scrollContent.style.overflow = "hidden";
	this.scrollContent.style.width = this.element.offsetWidth + "px";
	this.scrollContent.style.height = this.element.offsetHeight + "px";

	while(this.element.children.length) this.scrollContent.appendChild(this.element.children[0]);

	this.element.style.overflow = "hidden";
	this.element.style.display = "block";
	this.element.style.visibility = "visible";
	this.element.style.position = "relative";
	this.element.appendChild(this.scrollContent);

	this.scrollContent.className = 'scroll-content';

	this.element.index = this.index;
	this.element.over = false;
	
	var _this = this;
	this.element.onmouseover = function(){
		_this.element.over = true;
	};
	this.element.onmouseout = function(){
		_this.element.over = false;
	}

	if (document.all)
	{
		this.element.onscroll = VSA_handleOnScroll;
		this.element.onresize = VSA_handleResize;
	}
	else
	{
		window.onresize = VSA_handleResize;
	}
	
	this.createScrollBar();
	
	if (window.addEventListener)
        /** DOMMouseScroll is for mozilla. */
        this.element.addEventListener('DOMMouseScroll', VSA_handleMouseWheel, false);
	/** IE/Opera. */
	this.element.onmousewheel = document.onmousewheel = VSA_handleMouseWheel;

}

function VSA_createScrollBar()
{

	if (this.scrollbar != null)
	{
		this.element.removeChild(this.scrollbar);
		this.scrollbar = null;
	}

	if (this.scrollContent.scrollHeight <= this.scrollContent.offsetHeight)
		this.enableScrollbar = false;
	else if (this.element.offsetHeight > 2*this.scrollButtonHeight)
		this.enableScrollbar = true;
	else
		this.enableScrollbar = false;

	if (this.scrollContent.scrollHeight - Math.abs(this.scrollContent.scrollTop) < this.element.offsetHeight)
		this.scrollContent.style.top = 0;

	if (this.enableScrollbar)
	{

		this.scrollbar = document.createElement("DIV");
		this.element.appendChild(this.scrollbar);		
		this.scrollbar.style.position = "absolute";
		this.scrollbar.style.top = "0px";
		this.scrollbar.style.height = this.element.offsetHeight+"px";
		this.scrollbar.style.width = this.scrollbarWidth + "px";

		this.scrollbar.className = 'vscroll-bar';

		if(this.scrollbarWidth != this.scrollbar.offsetWidth)
		{
			this.scrollbarWidth = this.scrollbar.offsetHeight;
		}
		
		this.scrollbarWidth = this.scrollbar.offsetWidth;

		if(this.scrollbarPosition == 'left')
		{
			this.scrollContent.style.left = this.scrollbarWidth + 5 + "px";			
			this.scrollContent.style.width = this.element.offsetWidth - this.scrollbarWidth - 5 + "px";			
		}
		else if(this.scrollbarPosition == 'right')
		{
			this.scrollbar.style.left = this.element.offsetWidth - this.scrollbarWidth  + "px";
			this.scrollContent.style.width = this.element.offsetWidth - this.scrollbarWidth - 5 + "px";			
		}

		//create scroll up button
		this.scrollup = document.createElement("DIV");
		this.scrollup.index = this.index;
		this.scrollup.onmousedown = VSA_handleBtnUpMouseDown;
		this.scrollup.onmouseup = VSA_handleBtnUpMouseUp;
		this.scrollup.onmouseout = VSA_handleBtnUpMouseOut;
		this.scrollup.style.position = "absolute";
		this.scrollup.style.top = "0px";
		this.scrollup.style.left = "0px";		
		this.scrollup.style.height = this.scrollButtonHeight + "px";		
		this.scrollup.style.width = this.scrollbarWidth + "px";
		
		this.scrollup.innerHTML = '<img src="' + this.imagesPath + '/' + this.btnUpImage + '" border="0"/>';
		this.scrollbar.appendChild(this.scrollup);

		this.scrollup.className = 'vscroll-up';

		if(this.scrollButtonHeight != this.scrollup.offsetHeight)
		{
			this.scrollButtonHeight = this.scrollup.offsetHeight;
		}
				
		//create scroll down button
		this.scrolldown = document.createElement("DIV");
		this.scrolldown.index = this.index;
		this.scrolldown.onmousedown = VSA_handleBtnDownMouseDown;
		this.scrolldown.onmouseup = VSA_handleBtnDownMouseUp;
		this.scrolldown.onmouseout = VSA_handleBtnDownMouseOut;
		this.scrolldown.style.position = "absolute";
		this.scrolldown.style.left = "0px";		
		this.scrolldown.style.top =  this.scrollbar.offsetHeight - this.scrollButtonHeight + "px";
		this.scrolldown.style.width = this.scrollbarWidth + "px";
		this.scrolldown.innerHTML = '<img src="' + this.imagesPath + '/' + this.btnDownImage + '" border="0"/>';
		this.scrollbar.appendChild(this.scrolldown);

		this.scrolldown.className = 'vscroll-down';

		//create scroll
		this.scroll = document.createElement("DIV");
		this.scroll.index = this.index;
		this.scroll.style.position = "absolute";
		this.scroll.style.zIndex = 0;
		this.scroll.style.textAlign = "center";
		this.scroll.style.top = this.scrollButtonHeight + "px";
		this.scroll.style.left = "0px";		
		this.scroll.style.width = this.scrollbarWidth + "px";
		
		var h = this.scrollbar.offsetHeight - 2*this.scrollButtonHeight;
		this.scroll.style.height = ((h > 0) ? h : 0) + "px";
		
		this.scroll.innerHTML = '';
		this.scroll.onclick = VSA_handleScrollbarClick;
		this.scrollbar.appendChild(this.scroll);
		this.scroll.style.overflow = "hidden";

		this.scroll.className = "vscroll-line";

		//create slider
		this.scrollslider = document.createElement("DIV");
		this.scrollslider.index = this.index;
		this.scrollslider.style.position = "absolute";
		this.scrollslider.style.zIndex = 1000;
		this.scrollslider.style.textAlign = "center";
		this.scrollslider.innerHTML = '<div id="scrollslider' + this.index + '" style="padding:0;margin:0;"><div class="scroll-bar-top"></div><div class="scroll-bar-bottom"></div></div>';
		this.scrollbar.appendChild(this.scrollslider);
		
		this.subscrollslider = document.getElementById("scrollslider"+this.index);		
		this.subscrollslider.style.height = Math.round((this.scrollContent.offsetHeight/this.scrollContent.scrollHeight)*(this.scrollbar.offsetHeight - 2*this.scrollButtonHeight)) + "px";
		
		this.scrollslider.className = "vscroll-slider";
		
		this.scrollHeight = this.scrollbar.offsetHeight - 2*this.scrollButtonHeight - this.scrollslider.offsetHeight;
		this.scrollFactor = (this.scrollContent.scrollHeight - this.scrollContent.offsetHeight)/this.scrollHeight;
		this.topPosition = getRealTop(this.scrollbar) + this.scrollButtonHeight;
		/* this.scrollbarHeight = this.scrollbar.offsetHeight - 2*this.scrollButtonHeight - this.scrollslider.offsetHeight; */

		this.scrollslider.style.top = /* 1 / this.scrollFactor * Math.abs(this.scrollContent.offsetTop) +*/ this.scrollButtonHeight + "px";
		this.scrollslider.style.left = "0px";
		this.scrollslider.style.width = "100%";
		this.scrollslider.onmousedown = VSA_handleSliderMouseDown;
		if (document.all)
			this.scrollslider.onmouseup = VSA_handleSliderMouseUp;
	}
	else
		this.scrollContent.style.width = this.element.offsetWidth + "px";
}

function VSA_handleBtnUpMouseDown()
{
	var sa = VSA_scrollAreas[this.index];
	sa.scrolling = true;
	sa.scrollUp();
}

function VSA_handleBtnUpMouseUp()
{
	VSA_scrollAreas[this.index].scrolling = false;
}

function VSA_handleBtnUpMouseOut()
{
	VSA_scrollAreas[this.index].scrolling = false;
}

function VSA_handleBtnDownMouseDown()
{
	var sa = VSA_scrollAreas[this.index];
	sa.scrolling = true;
	sa.scrollDown();
}

function VSA_handleBtnDownMouseUp()
{
	VSA_scrollAreas[this.index].scrolling = false;
}

function VSA_handleBtnDownMouseOut()
{
	VSA_scrollAreas[this.index].scrolling = false;
}

function VSA_scrollIt()
{
	this.scrollContent.scrollTop = this.scrollFactor * ((this.scrollslider.offsetTop + this.scrollslider.offsetHeight/2) - this.scrollButtonHeight - this.scrollslider.offsetHeight/2);
}

function VSA_scrollUp()
{
	if (this.scrollingLimit > 0)
	{
		this.scrollingLimit--;
		if (this.scrollingLimit == 0) this.scrolling = false;
	}
	if (!this.scrolling) return;
	if ( this.scrollContent.scrollTop - this.scrollStep > 0)
	{
		this.scrollContent.scrollTop -= this.scrollStep;
		this.scrollslider.style.top = 1 / this.scrollFactor * Math.abs(this.scrollContent.scrollTop) + this.scrollButtonHeight + "px";
	}
	else
	{
		this.scrollContent.scrollTop = "0";
		this.scrollslider.style.top = this.scrollButtonHeight + "px";
		return;
	}
	setTimeout("VSA_Ext_scrollUp(" + this.index + ")", 30);
}

function VSA_Ext_scrollUp(index)
{
	VSA_scrollAreas[index].scrollUp();
}

function VSA_scrollDown()
{
	if (this.scrollingLimit > 0)
	{
		this.scrollingLimit--;
		if (this.scrollingLimit == 0) this.scrolling = false;
	}
	if (!this.scrolling) return;


	this.scrollContent.scrollTop += this.scrollStep;
	this.scrollslider.style.top =  1 / this.scrollFactor * Math.abs(this.scrollContent.scrollTop) + this.scrollButtonHeight + "px";

	if (this.scrollContent.scrollTop >= (this.scrollContent.scrollHeight - this.scrollContent.offsetHeight))
	{
		this.scrollContent.scrollTop = (this.scrollContent.scrollHeight - this.scrollContent.offsetHeight);
		this.scrollslider.style.top = this.scrollbar.offsetHeight - this.scrollButtonHeight - this.scrollslider.offsetHeight + "px";
		return;
	}

	setTimeout("VSA_Ext_scrollDown(" + this.index + ")", 30);
}

function VSA_Ext_scrollDown(index)
{
	VSA_scrollAreas[index].scrollDown();
}

function VSA_handleMouseMove(evt)
{
	var sa = VSA_scrollAreas[((document.all && !window.opera) ? this.index : document.documentElement.scrollAreaIndex)];
	var posy = 0;
	if (!evt) var evt = window.event;
	
	if (evt.pageY)
		posy = evt.pageY;
	else if (evt.clientY)
		posy = evt.clientY;
			
		if (document.all && !window.opera)
		{
			posy += document.documentElement.scrollTop;
		}

	var iNewY = posy - sa.iOffsetY - getRealTop(sa.scrollbar) - sa.scrollButtonHeight;
		iNewY += sa.scrollButtonHeight;
		
	if (iNewY < sa.scrollButtonHeight)
		iNewY = sa.scrollButtonHeight;
	if (iNewY > (sa.scrollbar.offsetHeight - sa.scrollButtonHeight) - sa.scrollslider.offsetHeight)
		iNewY = (sa.scrollbar.offsetHeight - sa.scrollButtonHeight) - sa.scrollslider.offsetHeight;

	sa.scrollslider.style.top = iNewY + "px";

	sa.scrollIt();
}

function VSA_handleSliderMouseDown(evt)
{
	if (!(document.uniqueID && document.compatMode && !window.XMLHttpRequest))
	{
		document.onselectstart = function() { return false; }
		document.onmousedown = function() { return false; }
	}

	var sa = VSA_scrollAreas[this.index];
	if (document.all && !window.opera)
	{
		sa.scrollslider.setCapture()
		sa.iOffsetY = event.offsetY;
		sa.scrollslider.onmousemove = VSA_handleMouseMove;
	}
	else
	{
		if(window.opera)
		{
			sa.iOffsetY = event.offsetY;
		}
		else
		{
			sa.iOffsetY = evt.layerY;
		}
		document.documentElement.scrollAreaIndex = sa.index;
		document.documentElement.addEventListener("mousemove", VSA_handleMouseMove, true);
		document.documentElement.addEventListener("mouseup", VSA_handleSliderMouseUp, true);
	}
	return false;
}

function VSA_handleSliderMouseUp()
{
	if (!(document.uniqueID && document.compatMode && !window.XMLHttpRequest))
	{
		document.onmousedown = null;
		document.onselectstart = null;
	}

	if (document.all && !window.opera)
	{
		var sa = VSA_scrollAreas[this.index];
		sa.scrollslider.onmousemove = null;
		sa.scrollslider.releaseCapture();
		sa.scrollIt();
	}
	else
	{
		var sa = VSA_scrollAreas[document.documentElement.scrollAreaIndex];
		document.documentElement.removeEventListener("mousemove", VSA_handleMouseMove, true);
		document.documentElement.removeEventListener("mouseup", VSA_handleSliderMouseUp, true);
		sa.scrollIt();
	}
	return false;
}

function VSA_handleResize()
{
	if (VSA_resizeTimer)
	{
		clearTimeout(VSA_resizeTimer);
		VSA_resizeTimer = 0;
	}
	VSA_resizeTimer = setTimeout("VSA_performResizeEvent()", 100);
}

function VSA_performResizeEvent()
{
	for (var i=0; i<VSA_scrollAreas.length; i++)
		VSA_scrollAreas[i].createScrollBar();
}
function VSA_handleMouseWheel(event){
	if (this.index != null) {
		var sa = VSA_scrollAreas[this.index];
		
		if (sa.scrollbar == null) return;
		sa.scrolling = true;
		sa.scrollingLimit = sa.wheelSensitivity;
		
        var delta = 0;
        if (!event) /* For IE. */
                event = window.event;
        if (event.wheelDelta) { /* IE/Opera. */
                delta = event.wheelDelta/120;
                 
                /*if (window.opera)
                        delta = -delta;*/
        } else if (event.detail) { /** Mozilla case. */
                delta = -event.detail/3;
        }
        if (delta && sa.element.over) {
                if (delta > 0)
			        sa.scrollUp();
				else
			        sa.scrollDown();
				
                if (event.preventDefault)
                        event.preventDefault();
                event.returnValue = false;
        }
	}
}

function VSA_handleSelectStart()
{
	event.returnValue = false;
}

function VSA_handleScrollbarClick(evt)
{
	var sa = VSA_scrollAreas[this.index];
	var offsetY = (document.all ? event.offsetY : evt.layerY);

	if (offsetY < (sa.scrollButtonHeight + sa.scrollslider.offsetHeight/2))
		sa.scrollslider.style.top = sa.scrollButtonHeight + "px";
	else if (offsetY > (sa.scrollbar.offsetHeight - sa.scrollButtonHeight - sa.scrollslider.offsetHeight))
		sa.scrollslider.style.top = sa.scrollbar.offsetHeight - sa.scrollButtonHeight - sa.scrollslider.offsetHeight + "px";
	else
	{
		sa.scrollslider.style.top = offsetY + sa.scrollButtonHeight - sa.scrollslider.offsetHeight/2 + "px";
	}
	sa.scrollIt();
}

function VSA_handleOnScroll()
{
	//event.srcElement.doScroll("pageUp");
}

//--- common functions ----

function VSA_getElements(attrValue, tagName, ownerNode, attrName) //get Elements By Attribute Name
{
	if (!tagName) tagName = "*";
	if (!ownerNode) ownerNode = document;
	if (!attrName) attrName = "name";
	var result = [];
	var nl = ownerNode.getElementsByTagName(tagName);
	for (var i=0; i<nl.length; i++)
	{
	//	if (nl.item(i).getAttribute(attrName) == attrValue)
//		result.push(nl.item(i));
		if (nl.item(i).className.indexOf(attrValue) != -1)
		result.push(nl.item(i));
	}
	return result;
}



function getRealTop(obj) {
	var posTop = 0;
	while (obj.offsetParent) {
		posTop += obj.offsetTop;
		obj = obj.offsetParent;
	}
	return posTop;
}