<div class="user-info">
  <span class="user">${user.firstName?html} ${user.lastName?html}</span>
  <span class="separator">&nbsp;</span>
  <a href="${url.context}/dologout">${msg("link.logout")}</a>
</div>
<div class="application-info">
  <img src="${url.context}/res/images/logo.png"/>
</div>
