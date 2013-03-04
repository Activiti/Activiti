if (typeof(console) == "undefined") {
  var console = {
    info: function(){},
    warn: function(){},
    error: function(){},
    log: function(){},
    time: function(){},
    timeEnd: function(){}
  };
}

if(!Array.isArray) {
  Array.isArray = function (vArg) {
    return Object.prototype.toString.call(vArg) === "[object Array]";
  };
}