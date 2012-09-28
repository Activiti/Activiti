/**
 * @author willi.tscheschner
 * 
 * contains all strings for default language (en_us)
 * 
 */



// namespace
if(window.Signavio == undefined) Signavio = {};
if(window.Signavio.I18N == undefined) Signavio.I18N = {};

Signavio.I18N.Language = "de"; //Pattern <ISO language code>_<ISO country code> in lower case!

Signavio.I18N.askForRefresh = "Der Inhalt des aktuellen Ordners hat sich möglicherweise geändert.<br/>Möchten Sie die Ansicht aktualisieren?";

// DEFINE THE DEFAULT VALUES FOR THE EXT MSG-BOX
Ext.MessageBox.buttonText.yes = "Ja";
Ext.MessageBox.buttonText.no = "Nein";
Ext.MessageBox.buttonText.cancel = "Abbrechen";
Ext.MessageBox.buttonText.ok = "OK";


// REPOSITORY
if(!Signavio.I18N.Repository) Signavio.I18N.Repository = {};
Signavio.I18N.Repository.leftPanelTitle = "";
Signavio.I18N.Repository.rightPanelTitle = "";


// DATE
if(!Signavio.I18N.Repository.Date) Signavio.I18N.Repository.Date = {};
Signavio.I18N.Repository.Date.ago = "vor";
// SINGULAR
Signavio.I18N.Repository.Date.year = "Jahr";
Signavio.I18N.Repository.Date.month = "Monat";
Signavio.I18N.Repository.Date.day = "Tag";
Signavio.I18N.Repository.Date.hour = "Stunde";
Signavio.I18N.Repository.Date.minute = "Minute";
Signavio.I18N.Repository.Date.second = "Sekunde";
// PLURAL
Signavio.I18N.Repository.Date.years = "Jahre";
Signavio.I18N.Repository.Date.months = "Monate";
Signavio.I18N.Repository.Date.days = "Tage";
Signavio.I18N.Repository.Date.hours = "Stunden";
Signavio.I18N.Repository.Date.minutes = "Minuten";
Signavio.I18N.Repository.Date.seconds = "Sekunden";


// LOGGING TOOL
if(!Signavio.I18N.Repository.Log) Signavio.I18N.Repository.Log = {};
Signavio.I18N.Repository.Log.ERROR = "FEHLER";
Signavio.I18N.Repository.Log.CRITICAL = "KRITISCH";
Signavio.I18N.Repository.Log.FATAL = "FATAL";
Signavio.I18N.Repository.Log.LOG = "Log";

Signavio.I18N.Repository.loadingFailed = "Die Anwendung konnte nicht geladen werden. Bitte überprüfen Sie ihre Internetverbindung und laden die Seite erneut.";

Signavio.I18N.Repository.noFeaturesTitle = "Keine Funktionalität";
Signavio.I18N.Repository.noFeaturesMsg = "Im Moment sind keine Funktionen verfügbar.";


// HEADER TEMPLATE
if(!Signavio.I18N.Repository.Header) Signavio.I18N.Repository.Header = {};
Signavio.I18N.Repository.Header.openIdSample = "your.openid.net";
Signavio.I18N.Repository.Header.sayHello = "Hi";
Signavio.I18N.Repository.Header.login = "einloggen";
Signavio.I18N.Repository.Header.logout = "ausloggen";
Signavio.I18N.Repository.Header.loginTitle = "Einloggen";
Signavio.I18N.Repository.Header.loginError401 = "Der Benutzername und/oder das Passwort sind nicht korrekt.";
Signavio.I18N.Repository.Header.loginError404 = "Der Benutzer ist nicht aktiviert.";
Signavio.I18N.Repository.Header.loginError500 = "Während des einloggens ist etwas schief gegangen.";


Signavio.I18N.Repository.Header.windowBtnLogin = "Einloggen";
Signavio.I18N.Repository.Header.windowBtnCancel = "Abbrechen";
Signavio.I18N.Repository.Header.windowBtnResetPassword = "Passwort zurücksetzen";
Signavio.I18N.Repository.Header.windowFieldName = "E-Mail";
Signavio.I18N.Repository.Header.windowFieldUserName = "Benutzername";
Signavio.I18N.Repository.Header.windowFieldPassword = "Passwort";
Signavio.I18N.Repository.Header.windowFieldRemember = "Angemeldet bleiben";
Signavio.I18N.Repository.Header.windowResetPasswordDesc = "Bitte geben Sie die E-Mail Adresse des Benutzers an, für den Sie das Passwort zurücksetzen möchten.";
Signavio.I18N.Repository.Header.windowBtnSend = "Senden";
Signavio.I18N.Repository.Header.windowResetPasswordHintOk = "Eine E-Mail wurde versendet um das Passwort zurück zu setzen";
Signavio.I18N.Repository.Header.windowResetPasswordHintFail = "Das senden der E-Mail um das Passwort zurück zu setzen ist fehlgeschlagen. Bitte versuchen Sie es erneut oder kontaktieren Sie den Support.";


// CONTEXT PLUGIN
if(!Signavio.I18N.Repository.ContextPlugin) Signavio.I18N.Repository.ContextPlugin = {};
Signavio.I18N.Repository.ContextPlugin.selectedElements = "Selektierte Elemente";


// FOLDER PLUGIN
if(!Signavio.I18N.Repository.Folder) Signavio.I18N.Repository.Folder = {};
Signavio.I18N.Repository.Folder.folder = "&raquo; Ordner";
Signavio.I18N.Repository.Folder.favorits = "&raquo; Favoriten";
Signavio.I18N.Repository.Folder.savedSearch = "&raquo; Gespeicherte Suche";

Signavio.I18N.Repository.Folder["public"] = "Arbeitsbereich";

// INFO PLUGIN
if(!Signavio.I18N.Repository.Info) Signavio.I18N.Repository.Info = {};
Signavio.I18N.Repository.Info.noTitle = "Kein Titel";
Signavio.I18N.Repository.Info.noDescription = "Keine Beschreibung";
Signavio.I18N.Repository.Info.noSelection = "Keine Elemente selektiert";
Signavio.I18N.Repository.Info.willBeNotified = "Sie bekommen eine Email-Benachrichtigung, wenn sich das Diagramm ändert.";
Signavio.I18N.Repository.Info.wontBeNotified = "";
Signavio.I18N.Repository.Info.notifyMe = "Ich möchte eine Email-Benachrichtigung erhalten, wenn sich das Diagramm ändert.";
Signavio.I18N.Repository.Info.dontNotifyMe = "Ich möchte keine Email-Benachrichtigungen mehr bekommen.";
Signavio.I18N.Repository.Info.elementSelected = "selektierte Elemente"
// DEFINE ATTRIBUTES
Signavio.I18N.Repository.Info.Attributes = {};
Signavio.I18N.Repository.Info.Attributes.name = "Name";
Signavio.I18N.Repository.Info.Attributes.description = "Beschreibung";
Signavio.I18N.Repository.Info.Attributes.noname = "Kein Name";
Signavio.I18N.Repository.Info.Attributes.nodescription = "Keine Beschreibung";
Signavio.I18N.Repository.Info.Attributes.author = "Autor";
Signavio.I18N.Repository.Info.Attributes.created = "Erstellt am";
Signavio.I18N.Repository.Info.Attributes.updated = "Geändert am";
Signavio.I18N.Repository.Info.Attributes.info = "geändert vor <b>#{time}</b> #{delimiter}von <b>#{user}</b>";
Signavio.I18N.Repository.Info.Attributes.infoMulipleOne = "geändert vor <b>#{time}</b>";
Signavio.I18N.Repository.Info.Attributes.infoMulipleTwo= "geändert zwischen <b>#{time}</b> und <b>#{time2}</b>";

// BREADCRUMP PLUGIN
if(!Signavio.I18N.Repository.BreadCrumb) Signavio.I18N.Repository.BreadCrumb = {};
Signavio.I18N.Repository.BreadCrumb.delimiter = "&raquo; ";
Signavio.I18N.Repository.BreadCrumb.search = "Suche: ";
Signavio.I18N.Repository.BreadCrumb.nrOfResults = "in {nr} Objekten gefunden"; 
Signavio.I18N.Repository.BreadCrumb.none = "Es wurde kein Ordner selektiert";
Signavio.I18N.Repository.BreadCrumb.goBack = "Zurück";

//VIEW TEMPLATE
if(!Signavio.I18N.Repository.View) Signavio.I18N.Repository.View = {};
Signavio.I18N.Repository.View.foundInNames = "Suchbegriff im Diagramm-Titel gefunden";
Signavio.I18N.Repository.View.foundInDescriptions = "Suchbegriff in der Diagramm-Beschreibung gefunden";
Signavio.I18N.Repository.View.foundInLabels = "Suchbegriff in Diagramm-Elementen gefunden";
Signavio.I18N.Repository.View.foundInComments = "Suchbegriff in den Kommentaren zum Diagramm gefunden";
Signavio.I18N.Repository.View.foundInRevComments = "Suchbegriff in Revisionskommentaren gefunden";
Signavio.I18N.Repository.View.foundInMetaData = "Suchbegriff in Zusatz-Attributen gefunden";


// ICONVIEW PLUGIN
if(!Signavio.I18N.Repository.IconView) Signavio.I18N.Repository.IconView = {};
Signavio.I18N.Repository.IconView.none = "Keine Elemente";
Signavio.I18N.Repository.IconView.description = "Symbolansicht";

if(!Signavio.I18N.Repository.TableView) Signavio.I18N.Repository.TableView = {};
Signavio.I18N.Repository.TableView.foundIn = "Gefunden in";
Signavio.I18N.Repository.TableView.name = "Name";
Signavio.I18N.Repository.TableView.description = "Beschreibung";
Signavio.I18N.Repository.TableView.revision = "Revision";
Signavio.I18N.Repository.TableView.lastChanges = "Letzte Änderung";
Signavio.I18N.Repository.TableView.lastAuthor = "Letzter Autor";
Signavio.I18N.Repository.TableView.tooltip = "Listenansicht";

// DELETE
Signavio.I18N.Repository.Offer.deleteTitle = "Löschen";
Signavio.I18N.Repository.Offer.deleteDescription = "Verschieben der ausgewählten Diagramme in den Papierkorb";
Signavio.I18N.Repository.Offer.removeTitle = "Entfernen";
Signavio.I18N.Repository.Offer.removeDescription = "Löschen der ausgewählten Diagramme vom Server";
Signavio.I18N.Repository.Offer.restoreTitle = "Wiederherstellen";
Signavio.I18N.Repository.Offer.restoreDescription = "Wiederherstellen der ausgewählten Diagramme in den Originalordner";
Signavio.I18N.Repository.Offer.deleteQuestion = "Sind Sie sicher, dass Sie die ausgewählten Diagramme in den Papierkorb verschieben möchten?";
Signavio.I18N.Repository.Offer.removeQuestion = "Möchten Sie die ausgewählten Diagramme wirklich unwiderruflich aus dem Papierkorb entfernen?";

// MOVE
Signavio.I18N.Repository.Offer.copyPrefix = " (Kopie)";
Signavio.I18N.Repository.Offer.moveTitle = "Verschieben";
Signavio.I18N.Repository.Offer.moveDescription = "Verschieben der selektierten Elemente in einem anderen Ordner";
Signavio.I18N.Repository.Offer.moveCreateCopy = "Kopie erstellen";
Signavio.I18N.Repository.Offer.moveCreateCopyNot = "Nur verfügbar wenn alle Elemente Diagramme sind.";
Signavio.I18N.Repository.Offer.moveWindowHeader = "Verschieben von '#{title}' nach:";
Signavio.I18N.Repository.Offer.moveWindowHeaderMultiple = "Verschieben von allen #{count} selektierten Elementen nach:";
Signavio.I18N.Repository.Offer.moveBtnOk = "Verschieben";
Signavio.I18N.Repository.Offer.moveBtnCancel = "Abbrechen";
Signavio.I18N.Repository.Offer.moveAlertTitle = "Verschieben";
Signavio.I18N.Repository.Offer.moveAlertDesc = "Sie haben keine Schreibrecht für den Zielordner.";

Signavio.I18N.Repository.Offer.copyTitle = "Kopieren";
Signavio.I18N.Repository.Offer.copyDescription = "Kopieren der selektierten Modelle zu einem neuen Ordner";
Signavio.I18N.Repository.Offer.copyWindowHeader = "Kopieren von '#{title}' nach:";
Signavio.I18N.Repository.Offer.copyWindowHeaderMultiple = "Kopieren von allen #{count} selektierten Diagrammen nach:";
Signavio.I18N.Repository.Offer.copyBtnOk = "Kopieren";
Signavio.I18N.Repository.Offer.copyAlertTitle = "Kopieren";
Signavio.I18N.Repository.Offer.copyAlertDesc = "Sie haben keine Schreibrecht für den Zielordner.";


// NEW
Signavio.I18N.Repository.Offer.newTitle = "Neu";
Signavio.I18N.Repository.Offer.newFolderTitle = "Ordner";
Signavio.I18N.Repository.Offer.newFolderDescription = "Anlegen eines neuen Ordners";
Signavio.I18N.Repository.Offer.newFolderDefaultTitle = "Neuer Ordner";
Signavio.I18N.Repository.Offer.newFFOnly = "Leider können Sie mit Ihrem Webbrowser die Modellierungssicht des Prozesseditors nicht benutzen. Bitte <a target='_blank' href='http://www.signavio.com/de/browserkompatibilitaet.html'>laden Sie sich unseren kostenlosen Signavio Thin Client herunter</a>, der es Ihnen in wenigen Schritten ermöglicht auch modellierend auf das System zuzugreifen. Mehr Informationen über die Browserkompatibilität finden Sie <a target='_blank' href='http://www.signavio.com/de/browserkompatibilitaet.html'>hier</a>."
Signavio.I18N.Repository.Offer.newWindowFolderTitle = "Anlegen eines neuen Ordners";
Signavio.I18N.Repository.Offer.newWindowFolderDesc = "Bitte tragen Sie den Namen des neuen Ordners ein:";

// Edit
Signavio.I18N.Repository.Offer.editGroupTitle = "Bearbeiten";
Signavio.I18N.Repository.Offer.edit = "Diagramm editieren";
Signavio.I18N.Repository.Offer.editDescription = "Bearbeiten des aktuellen Diagramms";

// SEARCH
Signavio.I18N.Repository.Offer.search = "Suche";
// UPDATE
Signavio.I18N.Repository.Offer.updateTitle = "Aktualisieren";
Signavio.I18N.Repository.Offer.updateDescription = "Aktualisieren des kompletten Explorers";

// HOVERBUTTON
if(!Signavio.I18N.Repository.HoverButton) Signavio.I18N.Repository.HoverButton = {};
Signavio.I18N.Repository.HoverButton.deleteString = "Löschen";

if(!Signavio.I18N.Repository.FormStorePanel) Signavio.I18N.Repository.FormStorePanel = {};
Signavio.I18N.Repository.FormStorePanel.commit = "Speichern";
Signavio.I18N.Repository.FormStorePanel.reject = "Verwerfen";
Signavio.I18N.Repository.FormStorePanel.language = "Erfordert ein <a href='/p/explorer'>Neuladen</a> der Seite.";

if(!Signavio.I18N.Repository.Hint) Signavio.I18N.Repository.Hint = {};
Signavio.I18N.Repository.Hint.supportedBrowserEditor = "Modellierung wird für Ihren Browser derzeit nicht unterstützt (siehe <a href='http://www.signavio.com/de/browserkompatibilitaet.html' target='_blank'>Browserkompatibilität</a>).<br/>Benutzen Sie bitte Firefox 3.5 oder unseren Thin Client für Windows."

if(!Signavio.I18N.Repository.SearchFields) Signavio.I18N.Repository.SearchFields = {};
Signavio.I18N.Repository.SearchFields.labels = "Attributen";
Signavio.I18N.Repository.SearchFields.text1 = "Revisionskommentar";
Signavio.I18N.Repository.SearchFields.text2 = "Attributen";
Signavio.I18N.Repository.SearchFields.comments = "Kommentar";
Signavio.I18N.Repository.SearchFields.description = "Beschreibung";
Signavio.I18N.Repository.SearchFields.name = "Name";
