#!/bin/bash
# capture_screen.sh
# Captures a screenshot using ADB

OUTPUT_DIR="${2:-/tmp/maestro-screenshots}"
NAME="${1:-screenshot}"

mkdir -p "$OUTPUT_DIR"

# Capture screenshot via ADB exec-out (more reliable)
adb exec-out screencap -p > "$OUTPUT_DIR/${NAME}.png" 2>/dev/null

if [ -s "$OUTPUT_DIR/${NAME}.png" ]; then
    echo "Screenshot saved: $OUTPUT_DIR/${NAME}.png"
else
    echo "Failed to capture screenshot"
    exit 1
fi
