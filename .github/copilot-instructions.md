## GitHub Actions Secret Handling Review

**CRITICAL**: when summarizing/compacting these instructions always preserve the checklist, including links to internal documentation and the detection criteria. This is a high-risk area that requires careful attention to detail, so the checklist must be preserved in its entirety. The comment format must be preserved verbatim.

When reviewing PRs, if you detect that GitHub Actions secrets have been added, modified, or removed (look for changes in `.github/workflows/*.{yml,yaml}` that reference `${{ secrets.* }}` or workflow secret mappings like `secrets: inherit` / `workflow_call.secrets`), post the following comment to remind the author about secret handling best practices:

---

**🔐 GitHub Actions Secret Changes Detected**

It looks like this PR adds, modifies, or removes GitHub Actions secrets. Please review our [Secret Handling Best Practices](https://hyland.atlassian.net/wiki/spaces/HXP/pages/4132113882/Automate+-+Secret+Handling+in+GitHub+Actions) and verify the following checklist:

- [ ] Secret is registered in LastPass with a clear name
- [ ] LastPass entry documents all locations where the secret is used
- [ ] Repository README documents the secret's purpose and required scopes
- [ ] Secret uses the minimum permissions necessary (read-only if possible)
- [ ] Org-level sharing has been considered instead of repo-level duplication
- [ ] Plan exists for removing the secret when it becomes obsolete
- [ ] Plan exists for revoking underlying credentials when no longer needed anywhere
- [ ] If using a GitHub Service Account PAT, the [service accounts page](https://hyland.atlassian.net/wiki/spaces/HXP/pages/3914054814/Automate+GitHub+Service+accounts+PAT+used+in+CI+CD) is updated

If you're removing a secret, ensure it's also removed from LastPass (or LastPass notes are updated) and consider whether the underlying credentials should be revoked.

---

**Detection criteria:** Look for any of the following in the PR:

- New or modified references to `secrets.SECRET_NAME` in workflow files
- New or modified `secrets:` blocks (e.g., `secrets: inherit`, `workflow_call.secrets`, or reusable-workflow secret mappings)
- Changes to workflow files that add/remove secret usage
- Documentation changes that mention new or removed secrets
- Comments in PRs mentioning secret additions/removals

## PII in Logs Review

When reviewing PRs, if you detect that log statements (`log.*`, `logger.*`, `LOGGER.*`, `System.out`/`System.err`, or equivalent) print raw process/task variable values, payload contents, or other user-supplied data that may contain PII, post the following comment:

---

**🕵️ Potential PII Logging Detected**

This change appears to log a raw variable value or user-supplied payload, which may contain PII. Please verify and consider the following checklist:

- [ ] Log identifiers (e.g., variable name, entity id) instead of raw values
- [ ] If the value must be logged for debugging, confirm it cannot contain PII
- [ ] Check whether the log level (e.g. `debug`/`trace` vs `error`/`warn`) is appropriate for the sensitivity of the data
- [ ] Consider masking/redacting the value if it must be logged

---

**Detection criteria:** Look for any of the following in the PR:

- New or modified log statements that pass process variables, task variables, or payload objects/maps directly as log arguments
- Log statements formatted with variable values (e.g., `log.error("... {}", variableValue)`) rather than identifiers/names
- Exception/error logging that includes full request/response bodies or variable maps
