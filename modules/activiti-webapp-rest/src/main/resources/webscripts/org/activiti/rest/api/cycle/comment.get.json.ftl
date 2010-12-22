[
  <#list comments as comment>{
    "RepositoryNodeComment": {
<#escape x as jsonUtils.encodeJSONString(x)>
      "id": "${comment.id!''}",
	  "connectorId": "${comment.connectorId!''}",
	  "nodeId": "${comment.nodeId!''}",
	  "elementId": "${comment.elementId!''}",
	  "content": "${comment.content!''}",
	  "author": "${comment.author!''}",
	  "answeredCommentId": "${comment.answeredCommentId!''}",
</#escape>
	  "creationDate": "${comment.date?datetime}"
    }
  }<#if comment_has_next>,</#if></#list>
]