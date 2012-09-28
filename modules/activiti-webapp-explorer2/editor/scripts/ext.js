/**
 * Copyright (c) 2009
 * Philipp Giese
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

// enable key-Events on form-fields
Ext.override(Ext.form.Field, {
    fireKey : function(e) {
        if(((Ext.isIE && e.type == 'keydown') || e.type == 'keypress') && e.isSpecialKey()) {
            this.fireEvent('specialkey', this, e);
        }
        else {
            this.fireEvent(e.type, this, e);
        }
    }
  , initEvents : function() {
//		this.el.on(Ext.isIE ? "keydown" : "keypress", this.fireKey,  this);
        this.el.on("focus", this.onFocus,  this);
        this.el.on("blur", this.onBlur,  this);
        this.el.on("keydown", this.fireKey, this);
        this.el.on("keypress", this.fireKey, this);
        this.el.on("keyup", this.fireKey, this);

        // reference to original value for reset
        this.originalValue = this.getValue();
    }
 
});