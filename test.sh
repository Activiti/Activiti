
SHA=f46f54dd9b207c90f15809ebd08a0b8c1a5e4fe7
GITHUB_REPOSITORY=alfresco/hxp-frontend-apps
GITHUB_EVENT_NAME=pull_request
echo "Event name: $GITHUB_EVENT_NAME"

LABELS=$(gh api /repos/$GITHUB_REPOSITORY/commits/$SHA/pulls | jq -r '.[].labels.[].name')

if [[ "$GITHUB_EVENT_NAME" == "push" ]]; then
    PR_CREATOR=$(gh api /repos/$GITHUB_REPOSITORY/commits/$SHA/pulls | jq -r '.[0].user.login')
    echo "Creator: $PR_CREATOR"
fi
echo "Labels found: $LABELS"
if [[ "$GITHUB_ACTOR" == "dependabot[bot]" && "$LABELS" == *"$FOUND_LABEL"* ]]
then
    echo "PR opened by dependabot and with gh_action. The result is true"
    echo "SKIP=true" >> $GITHUB_OUTPUT
fi