#!/bin/sh

main() {
  set -e
  echo "" # see https://github.com/actions/toolkit/issues/168
  sanitize "$INPUT_PROFILE" "profile"
  COMMAND=${INPUT_COMMAND:-deploy}

  set +e
  OUTPUT=$(sh -c "$(run-env "$COMMAND" "$INPUT_PROFILE")" 2>&1)
  SUCCESS=$?
  echo "$OUTPUT"
  set -e

  if [ $SUCCESS -ne 0 ]; then
    exit $SUCCESS
  fi

  if [ "$COMMAND" = "delete" ]; then
    exit 0
  fi

  OUTPUT=$(stripcolors "$OUTPUT")
  URL=$(echo "$OUTPUT" | grep -P 'http.+' -o)
  echo ::set-output name=url::"$URL"
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
