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
/**
 * @author nicolas.peters
 * 
 * Contains all strings for the default language (en-us).
 * Version 1 - 08/29/08
 */
if(!ORYX) var ORYX = {};

if(!ORYX.I18N) ORYX.I18N = {};

ORYX.I18N.Language = "en_us";

if(!ORYX.I18N.Oryx) ORYX.I18N.Oryx = {};

ORYX.I18N.Oryx.title		= "Oryx";
ORYX.I18N.Oryx.noBackendDefined	= "注意!\n 后端没有定义. 无法加载请求的模型.尝试使用保存插件加载配置.";
ORYX.I18N.Oryx.pleaseWait 	= "正在加载, 请稍候...";
ORYX.I18N.Oryx.notLoggedOn = "尚未登录";
ORYX.I18N.Oryx.editorOpenTimeout = "编辑似乎还没有开始.请检查您是否启用了弹出窗口阻止程序并禁用或允许此站点弹出窗口.我们不会在这个网站上显示任何广告.";

if(!ORYX.I18N.AddDocker) ORYX.I18N.AddDocker = {};

ORYX.I18N.AddDocker.group = "Docker";
ORYX.I18N.AddDocker.add = "添加Docker容器";
ORYX.I18N.AddDocker.addDesc = "通过单击它, 将Docker容器添加到edge";
ORYX.I18N.AddDocker.del = "删除Docker容器";
ORYX.I18N.AddDocker.delDesc = "删除Docker容器";

if(!ORYX.I18N.Arrangement) ORYX.I18N.Arrangement = {};

ORYX.I18N.Arrangement.groupZ = "Z-Order";
ORYX.I18N.Arrangement.btf = "移至顶层";
ORYX.I18N.Arrangement.btfDesc = "移至顶层";
ORYX.I18N.Arrangement.btb = "移至底层";
ORYX.I18N.Arrangement.btbDesc = "移至底层";
ORYX.I18N.Arrangement.bf = "向前移动";
ORYX.I18N.Arrangement.bfDesc = "向前移动";
ORYX.I18N.Arrangement.bb = "向后移动";
ORYX.I18N.Arrangement.bbDesc = "向后移动";
ORYX.I18N.Arrangement.groupA = "对齐";
ORYX.I18N.Arrangement.ab = "底部对齐";
ORYX.I18N.Arrangement.abDesc = "底部";
ORYX.I18N.Arrangement.am = "中间对齐";
ORYX.I18N.Arrangement.amDesc = "中间";
ORYX.I18N.Arrangement.at = "顶部对齐";
ORYX.I18N.Arrangement.atDesc = "返回页首";
ORYX.I18N.Arrangement.al = "左对齐";
ORYX.I18N.Arrangement.alDesc = "左";
ORYX.I18N.Arrangement.ac = "中心对准";
ORYX.I18N.Arrangement.acDesc = "中心";
ORYX.I18N.Arrangement.ar = "右对齐";
ORYX.I18N.Arrangement.arDesc = "右";
ORYX.I18N.Arrangement.as = "同类对齐";
ORYX.I18N.Arrangement.asDesc = "相同大小";

if(!ORYX.I18N.Edit) ORYX.I18N.Edit = {};

ORYX.I18N.Edit.group = "编辑";
ORYX.I18N.Edit.cut = "削减";
ORYX.I18N.Edit.cutDesc = "将所选内容剪切到Oryx剪贴板";
ORYX.I18N.Edit.copy = "复制";
ORYX.I18N.Edit.copyDesc = "将所选内容复制到Oryx剪贴板";
ORYX.I18N.Edit.paste = "粘贴";
ORYX.I18N.Edit.pasteDesc = "将Oryx剪贴板粘贴到画布上";
ORYX.I18N.Edit.del = "删除";
ORYX.I18N.Edit.delDesc = "删除所有选定的形状";

if(!ORYX.I18N.EPCSupport) ORYX.I18N.EPCSupport = {};

ORYX.I18N.EPCSupport.group = "EPC";
ORYX.I18N.EPCSupport.exp = "导出 EPC";
ORYX.I18N.EPCSupport.expDesc = "导出图表到 EPML";
ORYX.I18N.EPCSupport.imp = "导入 EPC";
ORYX.I18N.EPCSupport.impDesc = "导入 EPML 文件";
ORYX.I18N.EPCSupport.progressExp = "导出模型";
ORYX.I18N.EPCSupport.selectFile = "选择要导入的 EPML (. empl) 文件.";
ORYX.I18N.EPCSupport.file = "文件";
ORYX.I18N.EPCSupport.impPanel = "导入 EPML 文件";
ORYX.I18N.EPCSupport.impBtn = "导入";
ORYX.I18N.EPCSupport.close = "关闭";
ORYX.I18N.EPCSupport.error = "错误";
ORYX.I18N.EPCSupport.progressImp = "导入...";

if(!ORYX.I18N.ERDFSupport) ORYX.I18N.ERDFSupport = {};

ORYX.I18N.ERDFSupport.exp = "导出到 ERDF";
ORYX.I18N.ERDFSupport.expDesc = "导出到 ERDF";
ORYX.I18N.ERDFSupport.imp = "从 ERDF 导入";
ORYX.I18N.ERDFSupport.impDesc = "从 ERDF 导入";
ORYX.I18N.ERDFSupport.impFailed = "导入 ERDF 的请求失败.";
ORYX.I18N.ERDFSupport.impFailed2 = "导入时出错!<br/> 请检查错误消息: <br/> <br/>";
ORYX.I18N.ERDFSupport.error = "错误";
ORYX.I18N.ERDFSupport.noCanvas = "xml 文档中没有包括 \"Oryx\" 画布节点!";
ORYX.I18N.ERDFSupport.noSS = ""Oryx画布" 节点没有包含任何模具集定义!";
ORYX.I18N.ERDFSupport.wrongSS = "给定的模具集不适合当前编辑器!";
ORYX.I18N.ERDFSupport.selectFile = "在 ERDF 中选择一个 ERDF (. xml) 文件或类型以导入它!";
ORYX.I18N.ERDFSupport.file = "文件";
ORYX.I18N.ERDFSupport.impERDF = "导入 ERDF";
ORYX.I18N.ERDFSupport.impBtn = "导入";
ORYX.I18N.ERDFSupport.impProgress = "导入...";
ORYX.I18N.ERDFSupport.close = "关闭";
ORYX.I18N.ERDFSupport.deprTitle = "真的导出到 ERDF？";
ORYX.I18N.ERDFSupport.deprText = "不建议再导出到 ERDF, 因为在未来版本的 \"Oryx编辑器\" 中将停止支持.如果可能, 请将模型导出到 JSON.是否仍然要导出？";

if(!ORYX.I18N.jPDLSupport) ORYX.I18N.jPDLSupport = {};

ORYX.I18N.jPDLSupport.group = "ExecBPMN";
ORYX.I18N.jPDLSupport.exp = "导出到 JPDL";
ORYX.I18N.jPDLSupport.expDesc = "导出到 JPDL";
ORYX.I18N.jPDLSupport.imp = "从 JPDL 导入";
ORYX.I18N.jPDLSupport.impDesc = "导入 JPDL 文件";
ORYX.I18N.jPDLSupport.impFailedReq = "导入 JPDL 的请求失败.";
ORYX.I18N.jPDLSupport.impFailedJson = "JPDL 的转变失败了.";
ORYX.I18N.jPDLSupport.impFailedJsonAbort = "导入已中止.";
ORYX.I18N.jPDLSupport.loadSseQuestionTitle = "需要加载 JBPM 模具集扩展";
ORYX.I18N.jPDLSupport.loadSseQuestionBody = "为了导入 JPDL, 必须加载模具集扩展.要继续吗？";
ORYX.I18N.jPDLSupport.expFailedReq = "模型导出请求失败.";
ORYX.I18N.jPDLSupport.expFailedXml = "导出到 JPDL 失败.导出报告:";
ORYX.I18N.jPDLSupport.error = "错误";
ORYX.I18N.jPDLSupport.selectFile = "在 JPDL 中选择一个 JPDL (. xml) 文件或类型以导入它!";
ORYX.I18N.jPDLSupport.file = "文件";
ORYX.I18N.jPDLSupport.impJPDL = "导入 JPDL";
ORYX.I18N.jPDLSupport.impBtn = "导入";
ORYX.I18N.jPDLSupport.impProgress = "导入...";
ORYX.I18N.jPDLSupport.close = "关闭";

if(!ORYX.I18N.Save) ORYX.I18N.Save = {};

ORYX.I18N.Save.group = "文件";
ORYX.I18N.Save.save = "保存";
ORYX.I18N.Save.saveDesc = "保存";
ORYX.I18N.Save.saveAs = "另存为...";
ORYX.I18N.Save.saveAsDesc = "另存为...";
ORYX.I18N.Save.unsavedData = "有未保存的数据, 请在退出前保存, 否则您的更改会丢失!";
ORYX.I18N.Save.newProcess = "新建过程";
ORYX.I18N.Save.saveAsTitle = "另存为...";
ORYX.I18N.Save.saveBtn = "保存";
ORYX.I18N.Save.close = "关闭";
ORYX.I18N.Save.savedAs = "保存为";
ORYX.I18N.Save.saved = "保存!";
ORYX.I18N.Save.failed = "保存失败.";
ORYX.I18N.Save.noRights = "您没有保存更改的权限.";
ORYX.I18N.Save.saving = "正在保存";
ORYX.I18N.Save.saveAsHint = "流程图存储在以下位置:";

if(!ORYX.I18N.File) ORYX.I18N.File = {};

ORYX.I18N.File.group = "文件";
ORYX.I18N.File.print = "打印";
ORYX.I18N.File.printDesc = "打印当前型号";
ORYX.I18N.File.pdf = "导出为 PDF";
ORYX.I18N.File.pdfDesc = "导出为 PDF";
ORYX.I18N.File.info = "信息";
ORYX.I18N.File.infoDesc = "信息";
ORYX.I18N.File.genPDF = "正在生成 PDF";
ORYX.I18N.File.genPDFFailed = "生成 PDF 失败.";
ORYX.I18N.File.printTitle = "打印";
ORYX.I18N.File.printMsg = "我们目前正在经历的印刷功能的问题.我们建议使用 PDF 导出来打印图表.是否确实要继续打印？";

if(!ORYX.I18N.Grouping) ORYX.I18N.Grouping = {};

ORYX.I18N.Grouping.grouping = "分组";
ORYX.I18N.Grouping.group = "组";
ORYX.I18N.Grouping.groupDesc = "对所有选定的形状进行分组";
ORYX.I18N.Grouping.ungroup = "取消分组";
ORYX.I18N.Grouping.ungroupDesc = "删除所有选定形状的组";

if(!ORYX.I18N.Loading) ORYX.I18N.Loading = {};

ORYX.I18N.Loading.waiting ="Please wait...";

if(!ORYX.I18N.PropertyWindow) ORYX.I18N.PropertyWindow = {};

ORYX.I18N.PropertyWindow.name = "名称";
ORYX.I18N.PropertyWindow.value = "值";
ORYX.I18N.PropertyWindow.selected = "选定";
ORYX.I18N.PropertyWindow.clickIcon = "单击图标";
ORYX.I18N.PropertyWindow.add = "添加";
ORYX.I18N.PropertyWindow.rem = "删除";
ORYX.I18N.PropertyWindow.complex = "复杂类型的编辑器";
ORYX.I18N.PropertyWindow.text = "文本类型的编辑器";
ORYX.I18N.PropertyWindow.ok = "确定";
ORYX.I18N.PropertyWindow.cancel = "取消";
ORYX.I18N.PropertyWindow.dateFormat = "m/d/y";

if(!ORYX.I18N.ShapeMenuPlugin) ORYX.I18N.ShapeMenuPlugin = {};

ORYX.I18N.ShapeMenuPlugin.drag = "拖动";
ORYX.I18N.ShapeMenuPlugin.clickDrag = "单击或拖动";
ORYX.I18N.ShapeMenuPlugin.morphMsg = "变形形状";

if(!ORYX.I18N.SyntaxChecker) ORYX.I18N.SyntaxChecker = {};

ORYX.I18N.SyntaxChecker.group = "核查";
ORYX.I18N.SyntaxChecker.name = "语法检查器";
ORYX.I18N.SyntaxChecker.desc = "检查语法";
ORYX.I18N.SyntaxChecker.noErrors = "没有语法错误.";
ORYX.I18N.SyntaxChecker.invalid = "服务器的应答无效.";
ORYX.I18N.SyntaxChecker.checkingMessage = "正在检查...";

if(!ORYX.I18N.FormHandler) ORYX.I18N.FormHandler = {};

ORYX.I18N.FormHandler.group = "FormHandling";
ORYX.I18N.FormHandler.name = "FormHandler";
ORYX.I18N.FormHandler.desc = "从处理中测试";

if(!ORYX.I18N.Deployer) ORYX.I18N.Deployer = {};

ORYX.I18N.Deployer.group = "部署";
ORYX.I18N.Deployer.name = "部署者";
ORYX.I18N.Deployer.desc = "部署到引擎";

if(!ORYX.I18N.Tester) ORYX.I18N.Tester = {};

ORYX.I18N.Tester.group = "正在测试";
ORYX.I18N.Tester.name = "测试过程";
ORYX.I18N.Tester.desc = "打开测试组件以测试此过程定义";

if(!ORYX.I18N.Undo) ORYX.I18N.Undo = {};

ORYX.I18N.Undo.group = "撤消";
ORYX.I18N.Undo.undo = "撤消";
ORYX.I18N.Undo.undoDesc = "撤消上一个操作";
ORYX.I18N.Undo.redo = "重做";
ORYX.I18N.Undo.redoDesc = "恢复上次撤消的操作";

if(!ORYX.I18N.View) ORYX.I18N.View = {};

ORYX.I18N.View.group = "缩放";
ORYX.I18N.View.zoomIn = "放大";
ORYX.I18N.View.zoomInDesc = "放大到模型";
ORYX.I18N.View.zoomOut = "缩小";
ORYX.I18N.View.zoomOutDesc = "缩小模型";
ORYX.I18N.View.zoomStandard = "变焦标准";
ORYX.I18N.View.zoomStandardDesc = "缩放到标准级别";
ORYX.I18N.View.zoomFitToModel = "缩放适应模型";
ORYX.I18N.View.zoomFitToModelDesc = "缩放以适合模型大小";

if(!ORYX.I18N.XFormsSerialization) ORYX.I18N.XFormsSerialization = {};

ORYX.I18N.XFormsSerialization.group = "XForms 序列化";
ORYX.I18N.XFormsSerialization.exportXForms = "XForms 导出";
ORYX.I18N.XFormsSerialization.exportXFormsDesc = "导出 XForms + XHTML 标记";
ORYX.I18N.XFormsSerialization.importXForms = "XForms 导入";
ORYX.I18N.XFormsSerialization.importXFormsDesc = "导入 XForms + XHTML 标记";
ORYX.I18N.XFormsSerialization.noClientXFormsSupport = "没有 XForms 支持";
ORYX.I18N.XFormsSerialization.noClientXFormsSupportDesc = "<h2>您的浏览器不支持 XForms.请为火狐安装<a href=\"https://addons.mozilla.org/firefox/addon/824\" target=\"_blank\">Mozilla XForms 插件</a></h2>";
ORYX.I18N.XFormsSerialization.ok = "确定";
ORYX.I18N.XFormsSerialization.selectFile = "在 XForms + xhtml 标记中选择一个 xhtml (. xhtml) 文件或类型以导入它!";
ORYX.I18N.XFormsSerialization.selectCss = "请插入 css 文件的 url";
ORYX.I18N.XFormsSerialization.file = "文件";
ORYX.I18N.XFormsSerialization.impFailed = "请求导入文档失败.";
ORYX.I18N.XFormsSerialization.impTitle = "导入 XForms + XHTML 文档";
ORYX.I18N.XFormsSerialization.expTitle = "导出 XForms + XHTML 文档";
ORYX.I18N.XFormsSerialization.impButton = "导入";
ORYX.I18N.XFormsSerialization.impProgress = "导入.";
ORYX.I18N.XFormsSerialization.close = "关闭";

/** New Language Properties: 08.12.2008 */

ORYX.I18N.PropertyWindow.title = "属性";

if(!ORYX.I18N.ShapeRepository) ORYX.I18N.ShapeRepository = {};
ORYX.I18N.ShapeRepository.title = "形状存储库";

ORYX.I18N.Save.dialogDesciption = "请输入名称、说明和注释.";
ORYX.I18N.Save.dialogLabelTitle = "标题";
ORYX.I18N.Save.dialogLabelDesc = "描述";
ORYX.I18N.Save.dialogLabelType = "类型";
ORYX.I18N.Save.dialogLabelComment = "修订注释";

if(!ORYX.I18N.Perspective) ORYX.I18N.Perspective = {};
ORYX.I18N.Perspective.no = "无透视";
ORYX.I18N.Perspective.noTip = "卸载当前透视图";

/** New Language Properties: 21.04.2009 */
ORYX.I18N.JSONSupport = {
    imp: {
        name: "Import from JSON",
        desc: "Imports a model from JSON",
        group: "Export",
        selectFile: "Select an JSON (.json) file or type in JSON to import it!",
        file: "File",
        btnImp: "Import",
        btnClose: "Close",
        progress: "导入....",
        syntaxError: "语法错误"
    },
    exp: {
        name: "导出到JSON",
        desc: "将当前模型导出到到JSON",
        group: "导出"
    }
};

/** New Language Properties: 09.05.2009 */
if(!ORYX.I18N.JSONImport) ORYX.I18N.JSONImport = {};

ORYX.I18N.JSONImport.title = "JSON 导入";
ORYX.I18N.JSONImport.wrongSS = "导入的文件 ({0}) 的模具集与已加载的模具集 ({1}) 不匹配.";

/** New Language Properties: 14.05.2009 */
if(!ORYX.I18N.RDFExport) ORYX.I18N.RDFExport = {};
ORYX.I18N.RDFExport.group = "导出";
ORYX.I18N.RDFExport.rdfExport = "导出到 RDF";
ORYX.I18N.RDFExport.rdfExportDescription = "将当前模型导出到为资源描述框架 (RDF) 定义的 XML 序列化";

/** New Language Properties: 15.05.2009*/
if(!ORYX.I18N.SyntaxChecker.BPMN) ORYX.I18N.SyntaxChecker.BPMN={};
ORYX.I18N.SyntaxChecker.BPMN_NO_SOURCE = "edge必须有源.";
ORYX.I18N.SyntaxChecker.BPMN_NO_TARGET = "edge必须有目标.";
ORYX.I18N.SyntaxChecker.BPMN_DIFFERENT_PROCESS = "源和目标节点必须包含在同一进程中.";
ORYX.I18N.SyntaxChecker.BPMN_SAME_PROCESS = "源和目标节点必须包含在不同的池中.";
ORYX.I18N.SyntaxChecker.BPMN_FLOWOBJECT_NOT_CONTAINED_IN_PROCESS = "流程对象必须包含在进程中.";
ORYX.I18N.SyntaxChecker.BPMN_ENDEVENT_WITHOUT_INCOMING_CONTROL_FLOW = "结束事件必须具有传入序列流.";
ORYX.I18N.SyntaxChecker.BPMN_STARTEVENT_WITHOUT_OUTGOING_CONTROL_FLOW = "开始事件必须具有传出序列流.";
ORYX.I18N.SyntaxChecker.BPMN_STARTEVENT_WITH_INCOMING_CONTROL_FLOW = "启动事件不能有传入序列流.";
ORYX.I18N.SyntaxChecker.BPMN_ATTACHEDINTERMEDIATEEVENT_WITH_INCOMING_CONTROL_FLOW = "附加的中间事件不能有传入序列流.";
ORYX.I18N.SyntaxChecker.BPMN_ATTACHEDINTERMEDIATEEVENT_WITHOUT_OUTGOING_CONTROL_FLOW = "附加的中间事件必须恰好有一个传出序列流.";
ORYX.I18N.SyntaxChecker.BPMN_ENDEVENT_WITH_OUTGOING_CONTROL_FLOW = "结束事件不能有传出序列流.";
ORYX.I18N.SyntaxChecker.BPMN_EVENTBASEDGATEWAY_BADCONTINUATION = "基于事件的网关不能后跟网关或子流程.";
ORYX.I18N.SyntaxChecker.BPMN_NODE_NOT_ALLOWED = "不允许该节点类型.";

if(!ORYX.I18N.SyntaxChecker.IBPMN) ORYX.I18N.SyntaxChecker.IBPMN={};
ORYX.I18N.SyntaxChecker.IBPMN_NO_ROLE_SET = "交互必须具有发送方和接收方角色集";
ORYX.I18N.SyntaxChecker.IBPMN_NO_INCOMING_SEQFLOW = "此节点必须具有传入序列流.";
ORYX.I18N.SyntaxChecker.IBPMN_NO_OUTGOING_SEQFLOW = "此节点必须具有传出序列流.";

if(!ORYX.I18N.SyntaxChecker.InteractionNet) ORYX.I18N.SyntaxChecker.InteractionNet={};
ORYX.I18N.SyntaxChecker.InteractionNet_SENDER_NOT_SET = "未设置发件人";
ORYX.I18N.SyntaxChecker.InteractionNet_RECEIVER_NOT_SET = "未设置接收器";
ORYX.I18N.SyntaxChecker.InteractionNet_MESSAGETYPE_NOT_SET = "未设置消息类型";
ORYX.I18N.SyntaxChecker.InteractionNet_ROLE_NOT_SET = "未设置角色";

if(!ORYX.I18N.SyntaxChecker.EPC) ORYX.I18N.SyntaxChecker.EPC={};
ORYX.I18N.SyntaxChecker.EPC_NO_SOURCE = "每个edge都必须有一个源.";
ORYX.I18N.SyntaxChecker.EPC_NO_TARGET = "每个edge必须有一个目标.";
ORYX.I18N.SyntaxChecker.EPC_NOT_CONNECTED = "节点必须与edge连接.";
ORYX.I18N.SyntaxChecker.EPC_NOT_CONNECTED_2 = "节点必须与更多的edge连接.";
ORYX.I18N.SyntaxChecker.EPC_TOO_MANY_EDGES = "节点连接的edge太多.";
ORYX.I18N.SyntaxChecker.EPC_NO_CORRECT_CONNECTOR = "节点没有正确的连接器.";
ORYX.I18N.SyntaxChecker.EPC_MANY_STARTS = "必须只有一个启动事件.";
ORYX.I18N.SyntaxChecker.EPC_FUNCTION_AFTER_OR = "在分裂或 XOR 之后必须没有功能.";
ORYX.I18N.SyntaxChecker.EPC_PI_AFTER_OR = "在拆分或/XOR 之后必须没有进程接口.";
ORYX.I18N.SyntaxChecker.EPC_FUNCTION_AFTER_FUNCTION =  "There must be no function after a function.";
ORYX.I18N.SyntaxChecker.EPC_EVENT_AFTER_EVENT =  "There must be no event after an event.";
ORYX.I18N.SyntaxChecker.EPC_PI_AFTER_FUNCTION =  "There must be no process interface after a function.";
ORYX.I18N.SyntaxChecker.EPC_FUNCTION_AFTER_PI =  "There must be no function after a process interface.";
ORYX.I18N.SyntaxChecker.EPC_SOURCE_EQUALS_TARGET = "edge必须连接两个不同的节点.";

if(!ORYX.I18N.SyntaxChecker.PetriNet) ORYX.I18N.SyntaxChecker.PetriNet={};
ORYX.I18N.SyntaxChecker.PetriNet_NOT_BIPARTITE = "图不是二分";
ORYX.I18N.SyntaxChecker.PetriNet_NO_LABEL = "标记的过渡未设置标签";
ORYX.I18N.SyntaxChecker.PetriNet_NO_ID = "有一个没有 id 的节点";
ORYX.I18N.SyntaxChecker.PetriNet_SAME_SOURCE_AND_TARGET = "两个流关系具有相同的源和目标";
ORYX.I18N.SyntaxChecker.PetriNet_NODE_NOT_SET = "未为 flowrelationship 设置节点";

/** New Language Properties: 02.06.2009*/
ORYX.I18N.Edge = "edge";
ORYX.I18N.Node = "节点";

/** New Language Properties: 03.06.2009*/
ORYX.I18N.SyntaxChecker.notice = "将鼠标移到红色十字图标上以查看错误信息.";

/** New Language Properties: 05.06.2009*/
if(!ORYX.I18N.RESIZE) ORYX.I18N.RESIZE = {};
ORYX.I18N.RESIZE.tipGrow = "增加画布大小:";
ORYX.I18N.RESIZE.tipShrink = "减小画布大小:";
ORYX.I18N.RESIZE.N = "顶部";
ORYX.I18N.RESIZE.W = "左";
ORYX.I18N.RESIZE.S ="底部";
ORYX.I18N.RESIZE.E ="右";

/** New Language Properties: 15.07.2009*/
if(!ORYX.I18N.Layouting) ORYX.I18N.Layouting ={};
ORYX.I18N.Layouting.doing = "Layouting...";

/** New Language Properties: 18.08.2009*/
ORYX.I18N.SyntaxChecker.MULT_ERRORS = "多个错误";

/** New Language Properties: 08.09.2009*/
if(!ORYX.I18N.PropertyWindow) ORYX.I18N.PropertyWindow = {};
ORYX.I18N.PropertyWindow.oftenUsed = "常用";
ORYX.I18N.PropertyWindow.moreProps = "更多属性";

/** New Language Properties 01.10.2009 */
if(!ORYX.I18N.SyntaxChecker.BPMN2) ORYX.I18N.SyntaxChecker.BPMN2 = {};

ORYX.I18N.SyntaxChecker.BPMN2_DATA_INPUT_WITH_INCOMING_DATA_ASSOCIATION = "数据输入不能有任何传入的数据关联.";
ORYX.I18N.SyntaxChecker.BPMN2_DATA_OUTPUT_WITH_OUTGOING_DATA_ASSOCIATION = "数据输出不能有任何传出数据关联.";
ORYX.I18N.SyntaxChecker.BPMN2_EVENT_BASED_TARGET_WITH_TOO_MANY_INCOMING_SEQUENCE_FLOWS = "基于事件的网关的目标可能只有一个传入序列流.";

/** New Language Properties 02.10.2009 */
ORYX.I18N.SyntaxChecker.BPMN2_EVENT_BASED_WITH_TOO_LESS_OUTGOING_SEQUENCE_FLOWS = "基于事件的网关必须具有两个或更多传出序列流.";
ORYX.I18N.SyntaxChecker.BPMN2_EVENT_BASED_EVENT_TARGET_CONTRADICTION = "如果在配置中使用了消息中间事件, 则不能使用接收任务, 反之亦然.";
ORYX.I18N.SyntaxChecker.BPMN2_EVENT_BASED_WRONG_TRIGGER = "只有下列中间事件触发器有效: 消息、信号、计时器、条件和倍数.";
ORYX.I18N.SyntaxChecker.BPMN2_EVENT_BASED_WRONG_CONDITION_EXPRESSION = "事件网关的传出序列流不能有条件表达式.";
ORYX.I18N.SyntaxChecker.BPMN2_EVENT_BASED_NOT_INSTANTIATING = "网关不符合实例化进程的条件.请使用网关的启动事件或实例化属性.";

/** New Language Properties 05.10.2009 */
ORYX.I18N.SyntaxChecker.BPMN2_GATEWAYDIRECTION_MIXED_FAILURE = "网关必须同时具有多个传入和传出序列流.";
ORYX.I18N.SyntaxChecker.BPMN2_GATEWAYDIRECTION_CONVERGING_FAILURE = "网关必须有多个传入的, 但大多数不具有多个传出序列流.";
ORYX.I18N.SyntaxChecker.BPMN2_GATEWAYDIRECTION_DIVERGING_FAILURE = "网关不能有多个传入的, 但必须有多个传出序列流.";
ORYX.I18N.SyntaxChecker.BPMN2_GATEWAY_WITH_NO_OUTGOING_SEQUENCE_FLOW = "网关必须至少有一个传出序列流.";
ORYX.I18N.SyntaxChecker.BPMN2_RECEIVE_TASK_WITH_ATTACHED_EVENT = "在事件网关配置中使用的接收任务不得具有任何附加的中间事件.";
ORYX.I18N.SyntaxChecker.BPMN2_EVENT_SUBPROCESS_BAD_CONNECTION = "事件子进程不能有任何传入或传出序列流.";

/** New Language Properties 13.10.2009 */
ORYX.I18N.SyntaxChecker.BPMN_MESSAGE_FLOW_NOT_CONNECTED = "必须至少连接消息流的一端.";

/** New Language Properties 24.11.2009 */
ORYX.I18N.SyntaxChecker.BPMN2_TOO_MANY_INITIATING_MESSAGES = "Choreography活动可能只有一个起始信息.";
ORYX.I18N.SyntaxChecker.BPMN_MESSAGE_FLOW_NOT_ALLOWED = "此处不允许使用消息流.";

/** New Language Properties 27.11.2009 */
ORYX.I18N.SyntaxChecker.BPMN2_EVENT_BASED_WITH_TOO_LESS_INCOMING_SEQUENCE_FLOWS = "不实例化的基于事件的网关必须至少有一个传入序列流.";
ORYX.I18N.SyntaxChecker.BPMN2_TOO_FEW_INITIATING_PARTICIPANTS = "Choreography活动必须有一个启动参与者 (白色).";
ORYX.I18N.SyntaxChecker.BPMN2_TOO_MANY_INITIATING_PARTICIPANTS = "Choreography活性不能有一个以上的启动参与者 (白色).";

ORYX.I18N.SyntaxChecker.COMMUNICATION_AT_LEAST_TWO_PARTICIPANTS = "通信必须至少连接到两个参与者.";
ORYX.I18N.SyntaxChecker.MESSAGEFLOW_START_MUST_BE_PARTICIPANT = "消息流的源必须是参与者.";
ORYX.I18N.SyntaxChecker.MESSAGEFLOW_END_MUST_BE_PARTICIPANT = "消息流的目标必须是参与者.";
ORYX.I18N.SyntaxChecker.CONV_LINK_CANNOT_CONNECT_CONV_NODES = "会话链接必须将通信或子对话节点与参与者连接起来.";
