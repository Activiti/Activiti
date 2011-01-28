
The yui files were created using the YUI dependency configurator including the modules needed by the activiti webapps.
Clicking the url will display the configurator with the applied settings:
 
http://developer.yahoo.com/yui/articles/hosting/?animation&autocomplete&base&button&calendar&connection&container&cookie&datasource&datatable&dom&event&fonts&grids&history&json&menu&paginator&reset&resize&selector&tabview&treeview&yuiloader&MIN&norollup

Note: The build directory contains the latest images. To enable offline YUI, do the following:
1) Download Full Developer Kit from: http://developer.yahoo.com/yui/2/
2) Extract all images contained in the build directory, maintaining file hierarchy and place in this directory.
3) Replace all instances of "http://yui.yahooapis.com/2.8.1/" (update number to current version) in the CSS file with an empty string "" (no quotes)
4) Images will now be loaded from the local server.