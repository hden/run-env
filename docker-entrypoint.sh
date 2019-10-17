#!/bin/sh

# stripcolors takes some output and removes ANSI color codes.
stripcolors() {
  echo "$1" | sed 's/\x1b\[[0-9;]*m//g'
}

set -e

# Optionally activate gsutil.
if [[ ! -z "$GCLOUD_AUTH" ]]; then
    echo "$GCLOUD_AUTH" | base64 --decode > "$HOME"/gcloud.json
    sh -c "gcloud auth activate-service-account --key-file=$HOME/gcloud.json $*"
fi

set +e
OUTPUT=$(sh -c "$(run-env $*)" 2>&1)
SUCCESS=$?
echo "$OUTPUT"
set -e

if [ $SUCCESS -ne 0 ]; then
    exit $SUCCESS
fi

OUTPUT=$(stripcolors "$OUTPUT")

# Create a deployment summery from GitHub Actions.
if [[ "$GITHUB_EVENT_NAME" == 'pull_request' ]]; then
    COMMENT="#### Google Cloud Run
\`\`\`
$OUTPUT
\`\`\`
*Workflow: \`$GITHUB_WORKFLOW\`, Action: \`$GITHUB_ACTION\`*"
    PAYLOAD=$(echo '{}' | jq --arg body "$COMMENT" '.body = $body')
    COMMENTS_URL=$(cat $GITHUB_EVENT_PATH | jq -r .pull_request.comments_url)
    curl -s -S -H "Authorization: token $GITHUB_TOKEN" --header "Content-Type: application/json" --data "$PAYLOAD" "$COMMENTS_URL" > /dev/null
fi

# TODO: Add script for Google Cloud Build

# Create a deployment summery from CircleCI.
if [[ ! -z "$CIRCLE_PULL_REQUEST" ]]; then
    COMMENT="#### Google Cloud Run
\`\`\`
$OUTPUT
\`\`\`
*Workflow: \`$CIRCLE_WORKFLOW_ID\`, URL: \`$CIRCLE_BUILD_URL\`*"
    PAYLOAD=$(echo '{}' | jq --arg body "$COMMENT" '.body = $body')
    COMMENTS_URL="$CIRCLE_PULL_REQUEST/comments"
    curl -s -S -H "Authorization: token $GITHUB_TOKEN" --header "Content-Type: application/json" --data "$PAYLOAD" "$COMMENTS_URL" > /dev/null
fi

exit 0
