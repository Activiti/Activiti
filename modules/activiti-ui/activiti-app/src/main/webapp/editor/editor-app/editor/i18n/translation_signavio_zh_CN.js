/*
 * Activiti app component part of the Activiti project
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
ORYX.I18N.PropertyWindow.dateFormat = "d/m/y";

ORYX.I18N.View.East = "属性";
ORYX.I18N.View.West = "建模元素";

ORYX.I18N.Oryx.title	= "Signavio";
ORYX.I18N.Oryx.pleaseWait = "正在加载 Signavio 进程编辑器, 请稍候..。";
ORYX.I18N.Edit.cutDesc = "将所选内容剪切到剪贴板";
ORYX.I18N.Edit.copyDesc = "将所选内容复制到剪贴板中";
ORYX.I18N.Edit.pasteDesc = "将剪贴板粘贴到画布上";
ORYX.I18N.ERDFSupport.noCanvas = "xml 文档中没有包含任何画布节点!";
ORYX.I18N.ERDFSupport.noSS = "Signavio 进程编辑器画布节点没有包含模具集定义!";
ORYX.I18N.ERDFSupport.deprText = "不建议再导出到 eRDF, 因为在未来版本的 Signavio 进程编辑器中将停止支持。如果可能, 请将模型导出到 JSON。是否仍然要导出？";
ORYX.I18N.Save.pleaseWait = "请在保存时等待...";

ORYX.I18N.Save.saveAs = "保存副本...";
ORYX.I18N.Save.saveAsDesc = "保存副本...";
ORYX.I18N.Save.saveAsTitle = "保存副本...";
ORYX.I18N.Save.savedAs = "副本已保存";
ORYX.I18N.Save.savedDescription = "流程图存储在";
ORYX.I18N.Save.notAuthorized = "您当前未登录。请在新窗口中 <a href='/p/login' target='_blank'> 登录 </a>, 以便保存当前关系图。";
ORYX.I18N.Save.transAborted = "保存请求花费的时间太长。您可以使用更快的互联网连接。如果使用无线网络, 请检查连接的强度。";
ORYX.I18N.Save.noRights = "您没有存储该模型所需的权限。如果您仍有在目标目录中写入的权限, 请在 <a href='/p/explorer' target='_blank'> Signavio 资源管理器 </a> 中签入。";
ORYX.I18N.Save.comFailed = "与 Signavio 服务器的通信失败。请检查您的互联网连接。如果问题所在, 请通过工具栏中的信封符号与 Signavio 支持联系。";
ORYX.I18N.Save.failed = "试图保存图表时出错。请重试。如果问题所在, 请通过工具栏中的信封符号与 Signavio 支持联系。";
ORYX.I18N.Save.exception = "在尝试保存图表时会引发一些异常。请重试。如果问题所在, 请通过工具栏中的信封符号与 Signavio 支持联系。";
ORYX.I18N.Save.retrieveData = "请稍候, 数据正在检索。";

/** New Language Properties: 10.6.09*/
if(!ORYX.I18N.ShapeMenuPlugin) ORYX.I18N.ShapeMenuPlugin = {};
ORYX.I18N.ShapeMenuPlugin.morphMsg = "变换形状";
ORYX.I18N.ShapeMenuPlugin.morphWarningTitleMsg = "变换形状";
ORYX.I18N.ShapeMenuPlugin.morphWarningMsg = "已转换元素中不能包含子形状. <br/>是否仍然要进行转换？";

if (!Signavio) { var Signavio = {}; }
if (!Signavio.I18N) { Signavio.I18N = {} }
if (!Signavio.I18N.Editor) { Signavio.I18N.Editor = {} }

if (!Signavio.I18N.Editor.Linking) { Signavio.I18N.Editor.Linking = {} }
Signavio.I18N.Editor.Linking.CreateDiagram = "创建新图表";
Signavio.I18N.Editor.Linking.UseDiagram = "使用现有关系图";
Signavio.I18N.Editor.Linking.UseLink = "使用 web 链接";
Signavio.I18N.Editor.Linking.Close = "关闭";
Signavio.I18N.Editor.Linking.Cancel = "取消";
Signavio.I18N.Editor.Linking.UseName = "采用图示名";
Signavio.I18N.Editor.Linking.UseNameHint = "用链接关系图的名称替换建模元素 ({type}) 的当前名称。";
Signavio.I18N.Editor.Linking.CreateTitle = "建立链接";
Signavio.I18N.Editor.Linking.AlertSelectModel = "你必须选择一个模型。";
Signavio.I18N.Editor.Linking.ButtonLink = "链接图";
Signavio.I18N.Editor.Linking.LinkNoAccess = "您无法访问此关系图。";
Signavio.I18N.Editor.Linking.LinkUnavailable = "关系图不可用。";
Signavio.I18N.Editor.Linking.RemoveLink = "删除链接";
Signavio.I18N.Editor.Linking.EditLink = "编辑链接";
Signavio.I18N.Editor.Linking.OpenLink = "打开";
Signavio.I18N.Editor.Linking.BrokenLink = "链接断了!";
Signavio.I18N.Editor.Linking.PreviewTitle = "预览";

if(!Signavio.I18N.Glossary_Support) { Signavio.I18N.Glossary_Support = {}; }
Signavio.I18N.Glossary_Support.renameEmpty = "没有词典条目";
Signavio.I18N.Glossary_Support.renameLoading = "搜索.";

/** New Language Properties: 08.09.2009*/
if(!ORYX.I18N.PropertyWindow) ORYX.I18N.PropertyWindow = {};
ORYX.I18N.PropertyWindow.oftenUsed = "主要性能";
ORYX.I18N.PropertyWindow.moreProps = "更多属性";

ORYX.I18N.PropertyWindow.btnOpen = "打开";
ORYX.I18N.PropertyWindow.btnRemove = "删除";
ORYX.I18N.PropertyWindow.btnEdit = "编辑";
ORYX.I18N.PropertyWindow.btnUp = "上移";
ORYX.I18N.PropertyWindow.btnDown = "下移";
ORYX.I18N.PropertyWindow.createNew = "创建新";

if(!ORYX.I18N.PropertyWindow) ORYX.I18N.PropertyWindow = {};
ORYX.I18N.PropertyWindow.oftenUsed = "主要属性";
ORYX.I18N.PropertyWindow.moreProps = "更多属性";
ORYX.I18N.PropertyWindow.characteristicNr = "成本 &amp; 资源分析";
ORYX.I18N.PropertyWindow.meta = "自定义属性";

if(!ORYX.I18N.PropertyWindow.Category){ORYX.I18N.PropertyWindow.Category = {}}
ORYX.I18N.PropertyWindow.Category.popular = "主要属性";
ORYX.I18N.PropertyWindow.Category.characteristicnr = "成本 &amp; 资源分析";
ORYX.I18N.PropertyWindow.Category.others = "更多属性";
ORYX.I18N.PropertyWindow.Category.meta = "自定义属性";

if(!ORYX.I18N.PropertyWindow.ListView) ORYX.I18N.PropertyWindow.ListView = {};
ORYX.I18N.PropertyWindow.ListView.title = "编辑:";
ORYX.I18N.PropertyWindow.ListView.dataViewLabel = "已经存在的条目。";
ORYX.I18N.PropertyWindow.ListView.dataViewEmptyText = "没有列表项。";
ORYX.I18N.PropertyWindow.ListView.addEntryLabel = "添加新条目";
ORYX.I18N.PropertyWindow.ListView.buttonAdd = "添加";
ORYX.I18N.PropertyWindow.ListView.save = "保存";
ORYX.I18N.PropertyWindow.ListView.cancel = "取消";

if(!Signavio.I18N.Buttons) Signavio.I18N.Buttons = {};
Signavio.I18N.Buttons.save		= "保存";
Signavio.I18N.Buttons.cancel 	= "取消";
Signavio.I18N.Buttons.remove	= "删除";

if(!Signavio.I18N.btn) {Signavio.I18N.btn = {};}
Signavio.I18N.btn.btnEdit = "编辑";
Signavio.I18N.btn.btnRemove = "删除";
Signavio.I18N.btn.moveUp = "上移";
Signavio.I18N.btn.moveDown = "下移";

if(!Signavio.I18N.field) {Signavio.I18N.field = {};}
Signavio.I18N.field.Url = "URL";
Signavio.I18N.field.UrlLabel = "标签";
