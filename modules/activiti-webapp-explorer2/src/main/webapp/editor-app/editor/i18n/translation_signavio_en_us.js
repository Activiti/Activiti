ORYX.I18N.PropertyWindow.dateFormat = "d/m/y";

ORYX.I18N.View.East = "Attributes";
ORYX.I18N.View.West = "Modeling Elements";

ORYX.I18N.Oryx.title	= "Signavio";
ORYX.I18N.Oryx.pleaseWait = "Please wait while the Signavio Process Editor is loading...";
ORYX.I18N.Edit.cutDesc = "Cuts the selection into the clipboard";
ORYX.I18N.Edit.copyDesc = "Copies the selection into the clipboard";
ORYX.I18N.Edit.pasteDesc = "Pastes the clipboard to the canvas";
ORYX.I18N.ERDFSupport.noCanvas = "The xml document has no canvas node included!";
ORYX.I18N.ERDFSupport.noSS = "The Signavio Process Editor canvas node has no stencil set definition included!";
ORYX.I18N.ERDFSupport.deprText = "Exporting to eRDF is not recommended anymore because the support will be stopped in future versions of the Signavio Process Editor. If possible, export the model to JSON. Do you want to export anyway?";
ORYX.I18N.Save.pleaseWait = "Please wait<br/>while saving...";

ORYX.I18N.Save.saveAs = "Save a copy...";
ORYX.I18N.Save.saveAsDesc = "Save a copy...";
ORYX.I18N.Save.saveAsTitle = "Save a copy...";
ORYX.I18N.Save.savedAs = "Copy saved";
ORYX.I18N.Save.savedDescription = "The process diagram is stored under";
ORYX.I18N.Save.notAuthorized = "You are currently not logged in. Please <a href='/p/login' target='_blank'>log in</a> in a new window so that you can save the current diagram."
ORYX.I18N.Save.transAborted = "The saving request took too long. You may use a faster internet connection. If you use wireless LAN, please check the strength of your connection.";
ORYX.I18N.Save.noRights = "You do not have the required rights to store that model. Please check in the <a href='/p/explorer' target='_blank'>Signavio Explorer</a>, if you still have the rights to write in the target directory.";
ORYX.I18N.Save.comFailed = "The communication with the Signavio server failed. Please check your internet connection. If the problem resides, please contact the Signavio Support via the envelope symbol in the toolbar.";
ORYX.I18N.Save.failed = "Something went wrong when trying to save your diagram. Please try again. If the problem resides, please contact the Signavio Support via the envelope symbol in the toolbar.";
ORYX.I18N.Save.exception = "Some exceptions are raised while trying to save your diagram. Please try again. If the problem resides, please contact the Signavio Support via the envelope symbol in the toolbar.";
ORYX.I18N.Save.retrieveData = "Please wait, data is retrieving.";

/** New Language Properties: 10.6.09*/
if(!ORYX.I18N.ShapeMenuPlugin) ORYX.I18N.ShapeMenuPlugin = {};
ORYX.I18N.ShapeMenuPlugin.morphMsg = "Transform shape";
ORYX.I18N.ShapeMenuPlugin.morphWarningTitleMsg = "Transform shape";
ORYX.I18N.ShapeMenuPlugin.morphWarningMsg = "There are child shape which can not be contained in the transformed element.<br/>Do you want to transform anyway?";

if (!Signavio) { var Signavio = {}; }
if (!Signavio.I18N) { Signavio.I18N = {} }
if (!Signavio.I18N.Editor) { Signavio.I18N.Editor = {} }

if (!Signavio.I18N.Editor.Linking) { Signavio.I18N.Editor.Linking = {} }
Signavio.I18N.Editor.Linking.CreateDiagram = "Create a new diagram";
Signavio.I18N.Editor.Linking.UseDiagram = "Use existing diagram";
Signavio.I18N.Editor.Linking.UseLink = "Use web link";
Signavio.I18N.Editor.Linking.Close = "Close";
Signavio.I18N.Editor.Linking.Cancel = "Cancel";
Signavio.I18N.Editor.Linking.UseName = "Adopt diagram name";
Signavio.I18N.Editor.Linking.UseNameHint = "Replaces the current name of the modeling element ({type}) with the name of the linked diagram.";
Signavio.I18N.Editor.Linking.CreateTitle = "Establish link";
Signavio.I18N.Editor.Linking.AlertSelectModel = "You have to select a model.";
Signavio.I18N.Editor.Linking.ButtonLink = "Link diagram";
Signavio.I18N.Editor.Linking.LinkNoAccess = "You have no access to this diagram.";
Signavio.I18N.Editor.Linking.LinkUnavailable = "The diagram is unavailable.";
Signavio.I18N.Editor.Linking.RemoveLink = "Remove link";
Signavio.I18N.Editor.Linking.EditLink = "Edit Link";
Signavio.I18N.Editor.Linking.OpenLink = "Open";
Signavio.I18N.Editor.Linking.BrokenLink = "The link is broken!";
Signavio.I18N.Editor.Linking.PreviewTitle = "Preview";

if(!Signavio.I18N.Glossary_Support) { Signavio.I18N.Glossary_Support = {}; }
Signavio.I18N.Glossary_Support.renameEmpty = "No dictionary entry";
Signavio.I18N.Glossary_Support.renameLoading = "Searching...";

/** New Language Properties: 08.09.2009*/
if(!ORYX.I18N.PropertyWindow) ORYX.I18N.PropertyWindow = {};
ORYX.I18N.PropertyWindow.oftenUsed = "Main properties";
ORYX.I18N.PropertyWindow.moreProps = "More properties";

ORYX.I18N.PropertyWindow.btnOpen = "Open";
ORYX.I18N.PropertyWindow.btnRemove = "Remove";
ORYX.I18N.PropertyWindow.btnEdit = "Edit";
ORYX.I18N.PropertyWindow.btnUp = "Move up";
ORYX.I18N.PropertyWindow.btnDown = "Move down";
ORYX.I18N.PropertyWindow.createNew = "Create new";

if(!ORYX.I18N.PropertyWindow) ORYX.I18N.PropertyWindow = {};
ORYX.I18N.PropertyWindow.oftenUsed = "Main attributes";
ORYX.I18N.PropertyWindow.moreProps = "More attributes";
ORYX.I18N.PropertyWindow.characteristicNr = "Cost &amp; Resource Analysis";
ORYX.I18N.PropertyWindow.meta = "Custom attributes";

if(!ORYX.I18N.PropertyWindow.Category){ORYX.I18N.PropertyWindow.Category = {}}
ORYX.I18N.PropertyWindow.Category.popular = "Main Attributes";
ORYX.I18N.PropertyWindow.Category.characteristicnr = "Cost &amp; Resource Analysis";
ORYX.I18N.PropertyWindow.Category.others = "More Attributes";
ORYX.I18N.PropertyWindow.Category.meta = "Custom Attributes";

if(!ORYX.I18N.PropertyWindow.ListView) ORYX.I18N.PropertyWindow.ListView = {};
ORYX.I18N.PropertyWindow.ListView.title = "Edit: ";
ORYX.I18N.PropertyWindow.ListView.dataViewLabel = "Already existing entries.";
ORYX.I18N.PropertyWindow.ListView.dataViewEmptyText = "No list entries.";
ORYX.I18N.PropertyWindow.ListView.addEntryLabel = "Add a new entry";
ORYX.I18N.PropertyWindow.ListView.buttonAdd = "Add";
ORYX.I18N.PropertyWindow.ListView.save = "Save";
ORYX.I18N.PropertyWindow.ListView.cancel = "Cancel";

if(!Signavio.I18N.Buttons) Signavio.I18N.Buttons = {};
Signavio.I18N.Buttons.save		= "Save";
Signavio.I18N.Buttons.cancel 	= "Cancel";
Signavio.I18N.Buttons.remove	= "Remove";

if(!Signavio.I18N.btn) {Signavio.I18N.btn = {};}
Signavio.I18N.btn.btnEdit = "Edit";
Signavio.I18N.btn.btnRemove = "Remove";
Signavio.I18N.btn.moveUp = "Move up";
Signavio.I18N.btn.moveDown = "Move down";

if(!Signavio.I18N.field) {Signavio.I18N.field = {};}
Signavio.I18N.field.Url = "URL";
Signavio.I18N.field.UrlLabel = "Label";
