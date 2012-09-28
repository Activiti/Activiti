ImageViewer = Ext.extend(Ext.Window, {
    initComponent: function() {
        this.bodyCfg = {
            tag: 'img',
            src: this.src,
            autoscroll: true,
            fixedcenter	: true
        };
        ImageViewer.superclass.initComponent.apply(this, arguments);
    },

    onRender: function() {
        ImageViewer.superclass.onRender.apply(this, arguments);
        this.body.on('load', this.onImageLoad, this, {single: true});
    },

    onImageLoad: function() {
       // var h = this.getFrameHeight(),
       // w = this.getFrameWidth();
       // this.setSize(this.body.dom.offsetWidth + w, this.body.dom.offsetHeight + h);
    },

    setSrc: function(src) {
        this.body.on('load', this.onImageLoad, this, {single: true});
        //this.body.dom.style.width = this.body.dom.style.width = 'auto';
        this.body.dom.src = src;
    },

    initEvents: function() {
        ImageViewer.superclass.initEvents.apply(this, arguments);
        if (this.resizer) {
            this.resizer.preserveRatio = true;
        }
    }
});