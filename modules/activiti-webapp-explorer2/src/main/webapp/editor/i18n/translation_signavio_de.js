Ext.PagingToolbar.prototype.firstText = "Erste Seite";
Ext.PagingToolbar.prototype.prevText = "Vorherige Seite";
Ext.PagingToolbar.prototype.nextText = "Nächste Seite";
Ext.PagingToolbar.prototype.lastText  = "Letzte Seite";
	

ORYX.I18N.PropertyWindow.dateFormat = "d.m.y";


ORYX.I18N.View.East = "Attribute";
ORYX.I18N.View.West = "Modellierungselemente";

ORYX.I18N.Oryx.pleaseWait = "Der Signavio Process Editor wird geladen. Bitte warten...";
ORYX.I18N.AddDocker.add = "Docker hinzufügen";
ORYX.I18N.AddDocker.del = "Docker löschen";
ORYX.I18N.ERDFSupport.noCanvas = "Das XML Dokument enthält keinen Canvas Knoten.";
ORYX.I18N.ERDFSupport.deprText = "Der Export nach eRDF wird nicht empfohlen, da dieses Format in zukünftigen Versionen des Signavio Process Editors nicht mehr unterstützt wird. Verwenden Sie stattdessen den Export nach JSON, falls möglich. Wollen Sie dennoch das Model nach eRDF exportieren?";
ORYX.I18N.Save.unsavedData = "Das Diagramm enthält nicht gespeicherte Daten. Sind Sie sicher, dass Sie den Editor schließen möchten?";
ORYX.I18N.Save.pleaseWait = "Bitte warten Sie, während<br/>das Diagramm gespeichert wird.";
ORYX.I18N.File.info = "Info";
ORYX.I18N.File.infoDesc = "Info";
ORYX.I18N.PropertyWindow.name = "Attribut";
ORYX.I18N.View.zoomStandard = "Zoom: Originalgröße";
ORYX.I18N.View.zoomStandardDesc = "Zoom: Originalgröße";
ORYX.I18N.View.zoomFitToModel = "Zoom: Modellgröße";
ORYX.I18N.View.zoomFitToModelDesc = "Zoom: Modellgröße";
ORYX.I18N.ShapeRepository.title = "Modellierungselemente";
ORYX.I18N.Save.dialogLabelComment = "Änderungs-\nkommentar";

ORYX.I18N.Save.saveAs = "Kopie speichern...";
ORYX.I18N.Save.saveAsDesc = "Kopie speichern...";
ORYX.I18N.Save.saveAsTitle = "Kopie speichern...";
ORYX.I18N.Save.savedAs = "Kopie gespeichert";
ORYX.I18N.Save.savedDescription = "Das kopierte Diagramm ist unter folgendem Link gespeichert";
ORYX.I18N.Save.notAuthorized = "Sie sind derzeit nicht angemeldet. Bitte melden Sie sich in einem <a href='/p/login' target='_blank'>neuen Fenstern</a> an, damit Sie diese Model speichern können."
ORYX.I18N.Save.transAborted = "Die Anfrage zum Speichern Ihres Diagramms hat zu lange gedauert. Bitte benutzen Sie eine schnellere Internetverbindung. Wenn Sie eine kabellose Internetverbindung benutzen, dann überprüfen Sie bitte die Signalstärke.";
ORYX.I18N.Save.noRights = "Sie haben nicht die benötigten Rechte, um das Diagramm abzuspeichern. Bitte überprüfen Sie im <a href='/p/explorer' target='_blank'>Signavio Explorer</a>, ob Sie noch die benötigten Rechte im Zielordner besitzen.";
ORYX.I18N.Save.comFailed = "Die Kommunikation mit dem Signavio Server ist fehlgeschlagen. Bitte überprüfen Sie Ihre Internetverbindung. Wenn das Problem weiterhin besteht, wenden Sie sich bitte an den Signavio Support über das Briefumschlagssymbol in der Toolbar.";
ORYX.I18N.Save.failed = "Beim Speichern Ihres Diagramms ist ein Problem aufgetreten. Bitte versuchen Sie es erneut. Wenn das Problem weiterhin besteht, wenden Sie sich bitte an den Signavio Support über das Briefumschlagssymbol in der Toolbar.";
ORYX.I18N.Save.exception = "Beim Speichern Ihres Diagramms sind einige Probleme aufgetreten. Bitte versuchen Sie es erneut. Wenn das Problem weiterhin besteht, wenden Sie sich bitte an den Signavio Support über das Briefumschlagssymbol in der Toolbar.";
ORYX.I18N.Save.retrieveData = "Bitte warten, Daten werden geladen";

/** New Language Properties: 10.6.09*/
if(!ORYX.I18N.ShapeMenuPlugin) ORYX.I18N.ShapeMenuPlugin = {};
ORYX.I18N.ShapeMenuPlugin.morphMsg = "Umwandeln";
ORYX.I18N.ShapeMenuPlugin.morphWarningTitleMsg = "Umwandeln";
ORYX.I18N.ShapeMenuPlugin.morphWarningMsg = "Einige Kindelemente können nicht im neuen Element enthalten sein.<br/>Möchten Sie dennoch das Element umwandeln?";

if (!Signavio) { var Signavio = {}; }
if (!Signavio.I18N) { Signavio.I18N = {} }
if (!Signavio.I18N.Editor) { Signavio.I18N.Editor = {} }

if (!Signavio.I18N.Editor.Linking) { Signavio.I18N.Editor.Linking = {} }
Signavio.I18N.Editor.Linking.CreateDiagram = "Neues Diagramm erstellen:";
Signavio.I18N.Editor.Linking.UseDiagram = "Vorhandenes Diagramm verwenden";
Signavio.I18N.Editor.Linking.UseLink = "Web-Link verwenden";
Signavio.I18N.Editor.Linking.CreateTitle = "Verlinkung setzen";
Signavio.I18N.Editor.Linking.Close = "Schließen";
Signavio.I18N.Editor.Linking.Cancel = "Abbrechen";
Signavio.I18N.Editor.Linking.UseName = "Diagrammnamen übernehmen";
Signavio.I18N.Editor.Linking.UseNameHint = "Ersetzt den Bezeichner des aktuellen Elements ({type}) durch den Namen des zu verlinkenden Diagramms.";
Signavio.I18N.Editor.Linking.AlertSelectModel = "Bitte selektieren Sie ein Diagramm.";
Signavio.I18N.Editor.Linking.ButtonLink = "Verlinkung setzen";
Signavio.I18N.Editor.Linking.LinkNoAccess = "Sie haben keine Berechtigung für das Diagramm.";
Signavio.I18N.Editor.Linking.LinkUnavailable = "Das Diagramm ist nicht verfügbar.";
Signavio.I18N.Editor.Linking.RemoveLink = "Link löschen";
Signavio.I18N.Editor.Linking.EditLink = "Link ändern";
Signavio.I18N.Editor.Linking.OpenLink = "Öffnen";
Signavio.I18N.Editor.Linking.BrokenLink = "Der Link ist nicht verfügbar!";
Signavio.I18N.Editor.Linking.PreviewTitle = "Vorschau";

if(!ORYX.I18N.PropertyWindow) ORYX.I18N.PropertyWindow = {};
ORYX.I18N.PropertyWindow.oftenUsed = "Hauptattribute";
ORYX.I18N.PropertyWindow.moreProps = "Weitere Attribute";
ORYX.I18N.PropertyWindow.characteristicNr = "Kennzahlen";
ORYX.I18N.PropertyWindow.meta = "Eigene Attribute";

if(!ORYX.I18N.PropertyWindow.Category){ORYX.I18N.PropertyWindow.Category = {}}
ORYX.I18N.PropertyWindow.Category.popular = "Hauptattribute";
ORYX.I18N.PropertyWindow.Category.characteristicnr = "Kennzahlen";
ORYX.I18N.PropertyWindow.Category.others = "Weitere Attribute";
ORYX.I18N.PropertyWindow.Category.meta = "Eigene Attribute";

if(!ORYX.I18N.PropertyWindow.ListView) ORYX.I18N.PropertyWindow.ListView = {};
ORYX.I18N.PropertyWindow.ListView.title = "Attribut: ";
ORYX.I18N.PropertyWindow.ListView.dataViewLabel = "Bereits vorhandene Einträge";
ORYX.I18N.PropertyWindow.ListView.dataViewEmptyText = "Es sind noch keine Einträge vorhanden.";
ORYX.I18N.PropertyWindow.ListView.addEntryLabel = "Neuen Eintrag hinzufügen";
ORYX.I18N.PropertyWindow.ListView.buttonAdd = "Hinzufügen";
ORYX.I18N.PropertyWindow.ListView.save = "Speichern";
ORYX.I18N.PropertyWindow.ListView.cancel = "Abbrachen";

if(!Signavio.I18N.Buttons) Signavio.I18N.Buttons = {};
Signavio.I18N.Buttons.save		= "Speichern";
Signavio.I18N.Buttons.cancel 	= "Abbrechen";
Signavio.I18N.Buttons.remove	= "Entfernen";

if(!Signavio.I18N.btn) {Signavio.I18N.btn = {};}
Signavio.I18N.btn.btnEdit = "Editieren";
Signavio.I18N.btn.btnRemove = "Löschen";
Signavio.I18N.btn.moveUp = "Nach oben";
Signavio.I18N.btn.moveDown = "Nach unten";

if(!Signavio.I18N.field) {Signavio.I18N.field = {};}
Signavio.I18N.field.Url = "URL";
Signavio.I18N.field.UrlLabel = "Label";