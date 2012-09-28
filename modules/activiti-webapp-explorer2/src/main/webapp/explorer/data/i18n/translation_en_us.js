/**
 * @author willi.tscheschner
 * 
 * contains all strings for default language (en_us)
 * 
 */



// namespace
if(window.Signavio == undefined) Signavio = {};
if(window.Signavio.I18N == undefined) Signavio.I18N = {};

Signavio.I18N.Language = "en_us"; //Pattern <ISO language code>_<ISO country code> in lower case!

Signavio.I18N.askForRefresh = "The content of the current folder might have changed.<br/>Do you want to refresh the view?";

// DEFINE THE DEFAULT VALUES FOR THE EXT MSG-BOX
Ext.MessageBox.buttonText.yes = "Yes";
Ext.MessageBox.buttonText.no = "No";
Ext.MessageBox.buttonText.cancel = "Cancel";
Ext.MessageBox.buttonText.ok = "OK";


// REPOSITORY
if(!Signavio.I18N.Repository) Signavio.I18N.Repository = {};
Signavio.I18N.Repository.leftPanelTitle = "";
Signavio.I18N.Repository.rightPanelTitle = "";


// DATE
if(!Signavio.I18N.Repository.Date) Signavio.I18N.Repository.Date = {};
Signavio.I18N.Repository.Date.ago = "ago";
// SINGULAR
Signavio.I18N.Repository.Date.year = "year";
Signavio.I18N.Repository.Date.month = "month";
Signavio.I18N.Repository.Date.day = "day";
Signavio.I18N.Repository.Date.hour = "hour";
Signavio.I18N.Repository.Date.minute = "minute";
Signavio.I18N.Repository.Date.second = "second";
// PLURAL
Signavio.I18N.Repository.Date.years = "years";
Signavio.I18N.Repository.Date.months = "months";
Signavio.I18N.Repository.Date.days = "days";
Signavio.I18N.Repository.Date.hours = "hours";
Signavio.I18N.Repository.Date.minutes = "minutes";
Signavio.I18N.Repository.Date.seconds = "seconds";


// LOGGING TOOL
if(!Signavio.I18N.Repository.Log) Signavio.I18N.Repository.Log = {};
Signavio.I18N.Repository.Log.ERROR = "ERROR";
Signavio.I18N.Repository.Log.CRITICAL = "CRITICAL";
Signavio.I18N.Repository.Log.FATAL = "FATAL";
Signavio.I18N.Repository.Log.LOG = "Log";

Signavio.I18N.Repository.loadingFailed = "The application could not be loaded. Please double check your internet connection and reload the page.";

Signavio.I18N.Repository.noFeaturesTitle = "No functions";
Signavio.I18N.Repository.noFeaturesMsg = "There are no functions available at the moment.";


// HEADER TEMPLATE
if(!Signavio.I18N.Repository.Header) Signavio.I18N.Repository.Header = {};
Signavio.I18N.Repository.Header.openIdSample = "your.openid.net";
Signavio.I18N.Repository.Header.sayHello = "Hi";
Signavio.I18N.Repository.Header.login = "login";
Signavio.I18N.Repository.Header.logout = "logout";
Signavio.I18N.Repository.Header.loginTitle = "Login";
Signavio.I18N.Repository.Header.loginError401 = "The user name and/or password is incorrect!";
Signavio.I18N.Repository.Header.loginError404 = "User is not activated.";
Signavio.I18N.Repository.Header.loginError500 = "Something went wrong.";

Signavio.I18N.Repository.Header.windowBtnLogin = "Login";
Signavio.I18N.Repository.Header.windowBtnCancel = "Cancel";
Signavio.I18N.Repository.Header.windowBtnResetPassword = "Reset password";
Signavio.I18N.Repository.Header.windowFieldName = "Mail";
Signavio.I18N.Repository.Header.windowFieldUserName = "User name";
Signavio.I18N.Repository.Header.windowFieldPassword = "Password";
Signavio.I18N.Repository.Header.windowFieldRemember = "Remember me";
Signavio.I18N.Repository.Header.windowResetPasswordDesc = "Please enter your mail address to receive a mail with instructions how to reset your password.";
Signavio.I18N.Repository.Header.windowBtnSend = "Send";
Signavio.I18N.Repository.Header.windowResetPasswordHintOk = "Mail has been send to reset your password.";
Signavio.I18N.Repository.Header.windowResetPasswordHintFail = "Failed to send the mail to reset your password. Please try again or contact the support.";

// CONTEXT PLUGIN
if(!Signavio.I18N.Repository.ContextPlugin) Signavio.I18N.Repository.ContextPlugin = {};
Signavio.I18N.Repository.ContextPlugin.selectedElements = "Selected elements";


// FOLDER PLUGIN
if(!Signavio.I18N.Repository.Folder) Signavio.I18N.Repository.Folder = {};
Signavio.I18N.Repository.Folder.folder = "&raquo; Folders";
Signavio.I18N.Repository.Folder.favorits = "&raquo; Favorites";
Signavio.I18N.Repository.Folder.savedSearch = "&raquo; Saved Search";

Signavio.I18N.Repository.Folder["public"] = "Workspace";

// INFO PLUGIN
if(!Signavio.I18N.Repository.Info) Signavio.I18N.Repository.Info = {};
Signavio.I18N.Repository.Info.noTitle = "No title";
Signavio.I18N.Repository.Info.noDescription = "No summary";
Signavio.I18N.Repository.Info.noSelection = "No elements are selected";
Signavio.I18N.Repository.Info.willBeNotified = "In case of changes, you will get an email notification.";
Signavio.I18N.Repository.Info.wontBeNotified = "";
Signavio.I18N.Repository.Info.notifyMe = "Notify me via Email, if the diagram changes.";
Signavio.I18N.Repository.Info.dontNotifyMe = "Don't notify me.";
Signavio.I18N.Repository.Info.elementSelected = "elements selected";
// DEFINE ATTRIBUTES
Signavio.I18N.Repository.Info.Attributes = {};
Signavio.I18N.Repository.Info.Attributes.name = "Name";
Signavio.I18N.Repository.Info.Attributes.description = "Description";
Signavio.I18N.Repository.Info.Attributes.noname = "No name";
Signavio.I18N.Repository.Info.Attributes.nodescription = "No description";
Signavio.I18N.Repository.Info.Attributes.author = "Author";
Signavio.I18N.Repository.Info.Attributes.created = "Created";
Signavio.I18N.Repository.Info.Attributes.updated = "Update";
Signavio.I18N.Repository.Info.Attributes.info = "edited <b>#{time}</b> ago #{delimiter}by <b>#{user}</b>";
Signavio.I18N.Repository.Info.Attributes.infoMulipleOne = "last edited <b>#{time}</b> ago";
Signavio.I18N.Repository.Info.Attributes.infoMulipleTwo= "last edited between <b>#{time}</b> and <b>#{time2}</b>";

// BREADCRUMP PLUGIN
if(!Signavio.I18N.Repository.BreadCrumb) Signavio.I18N.Repository.BreadCrumb = {};
Signavio.I18N.Repository.BreadCrumb.delimiter = "&raquo; ";
Signavio.I18N.Repository.BreadCrumb.search = "Search: ";
Signavio.I18N.Repository.BreadCrumb.nrOfResults = "found in {nr} objects"; 
Signavio.I18N.Repository.BreadCrumb.none = "No directory selected";
Signavio.I18N.Repository.BreadCrumb.goBack = "Back";

// VIEW TEMPLATE
if(!Signavio.I18N.Repository.View) Signavio.I18N.Repository.View = {};
Signavio.I18N.Repository.View.foundInNames = "Found in titles";
Signavio.I18N.Repository.View.foundInDescriptions = "Found in descriptions";
Signavio.I18N.Repository.View.foundInLabels = "Found in diagram elements";
Signavio.I18N.Repository.View.foundInComments = "Found in comments";
Signavio.I18N.Repository.View.foundInRevComments = "Found in revision comments";
Signavio.I18N.Repository.View.foundInMetaData = "Found in custom attributes";

// ICONVIEW PLUGIN
if(!Signavio.I18N.Repository.IconView) Signavio.I18N.Repository.IconView = {};
Signavio.I18N.Repository.IconView.none = "No elements";
Signavio.I18N.Repository.IconView.description = "Icon view";

if(!Signavio.I18N.Repository.TableView) Signavio.I18N.Repository.TableView = {};
Signavio.I18N.Repository.TableView.foundIn = "Found in";
Signavio.I18N.Repository.TableView.name = "Name";
Signavio.I18N.Repository.TableView.description = "Description";
Signavio.I18N.Repository.TableView.revision = "Revision";
Signavio.I18N.Repository.TableView.lastChanges = "Last change";
Signavio.I18N.Repository.TableView.lastAuthor = "Last author";
Signavio.I18N.Repository.TableView.toolTip = "List view";

// ALL OFFERS
if(!Signavio.I18N.Repository.Offer) Signavio.I18N.Repository.Offer = {};

// DELETE
Signavio.I18N.Repository.Offer.deleteTitle = "Delete";
Signavio.I18N.Repository.Offer.deleteDescription = "Move the selected elements to the trash";
Signavio.I18N.Repository.Offer.removeTitle = "Remove";
Signavio.I18N.Repository.Offer.removeDescription = "Remove the selected elements. This action is irreversible.";
Signavio.I18N.Repository.Offer.restoreTitle = "Restore";
Signavio.I18N.Repository.Offer.restoreDescription = "Restore the elements from the trash";
Signavio.I18N.Repository.Offer.deleteQuestion = "Are you sure you want to move the diagram(s) to the trash?";
Signavio.I18N.Repository.Offer.removeQuestion = "Do you really want to permanently delete the selecte diagram(s) from the trash? This action is irreversible.";
// MOVE
Signavio.I18N.Repository.Offer.copyPrefix = " (Copy)";
Signavio.I18N.Repository.Offer.moveTitle = "Move";
Signavio.I18N.Repository.Offer.moveDescription = "Move the selected elements to the specified folder";
Signavio.I18N.Repository.Offer.moveCreateCopy = "Create a copy";
Signavio.I18N.Repository.Offer.moveCreateCopyNot = "Only available if all elements are diagrams.";
Signavio.I18N.Repository.Offer.moveWindowHeader = "Move '#{title}' to:";
Signavio.I18N.Repository.Offer.moveWindowHeaderMultiple = "Move all #{count} selected elements to:";
Signavio.I18N.Repository.Offer.moveBtnOk = "Move";
Signavio.I18N.Repository.Offer.moveBtnCancel = "Cancel";
Signavio.I18N.Repository.Offer.moveAlertTitle = "Move";
Signavio.I18N.Repository.Offer.moveAlertDesc = "You dont have write access for the selected target folder.";


Signavio.I18N.Repository.Offer.copyTitle = "Copy";
Signavio.I18N.Repository.Offer.copyDescription = "Copies the selected elements to a specified folder";
Signavio.I18N.Repository.Offer.copyWindowHeader = "Copy '#{title}' to:";
Signavio.I18N.Repository.Offer.copyWindowHeaderMultiple = "Copy all #{count} selected diagrams to:";
Signavio.I18N.Repository.Offer.copyBtnOk = "Copy";
Signavio.I18N.Repository.Offer.copyAlertTitle = "Copy";
Signavio.I18N.Repository.Offer.copyAlertDesc = "You dont have write access for the selected target folder.";


// NEW
Signavio.I18N.Repository.Offer.newTitle = "New";
Signavio.I18N.Repository.Offer.newFolderTitle = "Folder";
Signavio.I18N.Repository.Offer.newFolderDescription = "Create a new Folder";
Signavio.I18N.Repository.Offer.newFolderDefaultTitle = "New Folder";
Signavio.I18N.Repository.Offer.newFFOnly = "It is not possible to use the process editor with your web browser. More information about browser compatibility is available <a target='_blank' href='http://www.signavio.com/en/browser-compatibility.html' >here</a>."
Signavio.I18N.Repository.Offer.newWindowFolderTitle = "Create new folder";
Signavio.I18N.Repository.Offer.newWindowFolderDesc = "Please enter a name for the new folder:";

// Edit
Signavio.I18N.Repository.Offer.editGroupTitle = "Edit";
Signavio.I18N.Repository.Offer.edit = "Edit diagram";
Signavio.I18N.Repository.Offer.editDescription = "Edit the current diagram";

// SEARCH
Signavio.I18N.Repository.Offer.search = "Search";
// UPDATE
Signavio.I18N.Repository.Offer.updateTitle = "Refresh";
Signavio.I18N.Repository.Offer.updateDescription = "Updates the whole repository";

// HOVERBUTTON
if(!Signavio.I18N.Repository.HoverButton) Signavio.I18N.Repository.HoverButton = {};
Signavio.I18N.Repository.HoverButton.deleteString = "Delete";

if(!Signavio.I18N.Repository.FormStorePanel) Signavio.I18N.Repository.FormStorePanel = {};
Signavio.I18N.Repository.FormStorePanel.commit = "Save";
Signavio.I18N.Repository.FormStorePanel.reject = "Discard";
Signavio.I18N.Repository.FormStorePanel.language = "Requires a <a href='/p/explorer'>refresh</a> of the page.";

if(!Signavio.I18N.Repository.Hint) Signavio.I18N.Repository.Hint = {};
Signavio.I18N.Repository.Hint.supportedBrowserEditor = "Currently, modeling is not supported for your browser (see <a href='http://www.signavio.com/en/browser-compatibility.html' target='_blank'>browser compatibility</a>).<br/>Please use Firefox 3.5 or our Thin Client for Windows."

if(!Signavio.I18N.Repository.SearchFields) Signavio.I18N.Repository.SearchFields = {};
Signavio.I18N.Repository.SearchFields.labels = "Attributes";
Signavio.I18N.Repository.SearchFields.text1 = "Revision comment";
Signavio.I18N.Repository.SearchFields.text2 = "Attributes";
Signavio.I18N.Repository.SearchFields.comments = "Comments";
Signavio.I18N.Repository.SearchFields.description = "Description";
Signavio.I18N.Repository.SearchFields.name = "Name";
