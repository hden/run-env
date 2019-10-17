#!/bin/sh

main() {
  echo "" # see https://github.com/actions/toolkit/issues/168
  sanitize "$INPUT_PROFILE" "profile"

  set +e
  OUTPUT=$(sh -c "$(run-env "$INPUT_PROFILE")" 2>&1)
  SUCCESS=$?
  echo "$OUTPUT"
  set -e

  if [ $SUCCESS -ne 0 ]; then
      exit $SUCCESS
  fi

  OUTPUT=$(stripcolors "$OUTPUT")
  URL=$(echo "$OUTPUT" | grep -P 'http.+' -o)

  if [ "$GITHUB_WORKFLOW" -ne "" ]; then
    echo ::set-output name=url::"$URL"
  fi

  # Create a deployment summery from GitHub Actions.
  if [ "$GITHUB_EVENT_NAME" = 'pull_request' ]; then
      COMMENT="#### Google Cloud Run
\`\`\`
$OUTPUT
\`\`\`
*Workflow: \`$GITHUB_WORKFLOW\`, Action: \`$GITHUB_ACTION\`*"
      PAYLOAD=$(echo '{}' | jq --arg body "$COMMENT" '.body = $body')
      COMMENTS_URL=$(jq -r .pull_request.comments_url < "$GITHUB_EVENT_PATH")
      curl -s -S -H "Authorization: token $GITHUB_TOKEN" --header "Content-Type: application/json" --data "$PAYLOAD" "$COMMENTS_URL" > /dev/null
  fi
}

sanitize() {
  if [ -z "$1" ]; then
    >&2 echo "Unable to find the $2. Did you set with.$2?"
    exit 1
  fi
}

# stripcolors takes some output and removes ANSI color codes.
stripcolors() {
  echo "$1" | sed 's/\x1b\[[0-9;]*m//g'
}

main
