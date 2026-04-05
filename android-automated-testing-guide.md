# Android App Automated Testing Setup & Execution Guide for Rocky Linux 10

## Context
You are tasked with setting up and running automated UI tests for an Android application on a Rocky Linux 10 machine. The preferred tool is Maestro (open-source, YAML-based mobile testing framework). You have root/sudo access and should make everything work end-to-end. You have access to MiniMax MCP for AI-powered image analysis, which will be used to automatically review the look and feel of the app under test.

---

## Phase 1: Environment Setup

### 1.1 System Prerequisites

Install core build and development tools:

```bash
sudo dnf install -y \
  git \
  curl \
  wget \
  unzip \
  xz \
  zip \
  java-17-openjdk \
  java-17-openjdk-devel \
  python3 \
  python3-pip \
  libavahi-compat-libdnssd \
  sqlite \
  which \
  findutils \
  less \
  openssl \
  ca-certificates \
  gnupg2 \
  ccache \
  ninja-build \
  lzop
```

Verify Java version (requires Java 17):
```bash
java -version
# Should output OpenJDK 17.x.x
```

Set JAVA_HOME:
```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
echo "export JAVA_HOME=/usr/lib/jvm/java-17-openjdk" >> ~/.bashrc
```

### 1.2 Install Android SDK Command Line Tools

Create Android SDK directory and download command line tools:

```bash
mkdir -p ~/android-sdk/cmdline-tools
cd ~/android-sdk/cmdline-tools

# Download latest command line tools for Linux
wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O cmdline-tools.zip
unzip -q cmdline-tools.zip
mv cmdline-tools latest
rm cmdline-tools.zip
```

Set environment variables:

```bash
export ANDROID_HOME=~/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools
echo "export ANDROID_HOME=~/android-sdk" >> ~/.bashrc
echo "export PATH=\$PATH:\$ANDROID_HOME/cmdline-tools/latest/bin:\$ANDROID_HOME/platform-tools" >> ~/.bashrc
```

Accept licenses and install required SDK components:

```bash
yes | sdkmanager --licenses > /dev/null 2>&1
sdkmanager --install \
  "platform-tools" \
  "platforms;android-34" \
  "build-tools;34.0.0" \
  "emulator" \
  "system-images;android-34;google_apis;x86_64"
```

### 1.3 Configure KVM Hardware Acceleration (Required for Android Emulator)

Check if your CPU supports virtualization:
```bash
grep -E '(vmx|svm)' /proc/cpuinfo
```

If present, install KVM:
```bash
sudo dnf install -y qemu-kvm libvirt virt-manager virt-install libguestfs-tools
sudo systemctl enable libvirtd
sudo systemctl start libvirtd
sudo usermod -aG kvm,libvirt $USER
```

Log out and back in for group changes to take effect, or run:
```bash
newgrp kvm
```

### 1.4 Install Maestro

Download and install Maestro CLI:

```bash
curl -fsSL "https://get.maestro.mobile.dev" | bash
export PATH="$PATH:$HOME/.maestro/bin"
echo "export PATH=\$PATH:\$HOME/.maestro/bin" >> ~/.bashrc
```

Verify installation:
```bash
maestro --version
```

### 1.5 Install Appium (Optional but Recommended)

Appium provides additional capabilities and WebView inspection:

```bash
npm install -g appium
npm install -g appium-doctor
```

Verify Appium setup:
```bash
appium-doctor --android
```

---

## Phase 2: Project Configuration

### 2.1 Navigate to Your Android App

Identify the APK or AAB you want to test, or the app source code:

```bash
# If testing a built APK:
ls -la /path/to/your/app.apk

# If testing source code, locate build.gradle:
find . -name "build.gradle" -o -name "settings.gradle" | head -5
```

### 2.2 Create the Maestro Test Directory

In your project root, create the `.maestro` directory and test flows:

```bash
mkdir -p .maestro
```

### 2.3 Create a Basic Test Flow

Create `.maestro/Flow.yaml` with tests for your app. Example:

```yaml
appId: com.example.yourapp
---
- launchApp
- waitForAnimationToEnd
- assertVisible:
    id: "login_button"  # Adjust to your app's actual element IDs
- tapOn:
    id: "login_button"
- inputText:
    id: "email_field"
    text: "test@example.com"
- inputText:
    id: "password_field"
    text: "password123"
- tapOn:
    id: "submit_button"
- waitForTimeout: 2000
- assertVisible:
    text: "Welcome"
```

### 2.4 Create Multiple Test Flows

Organize tests into logical flows. Example structure:

```bash
.maestro/
  ├── Flow.yaml              # Main/primary flow
  ├── login/
  │   └── valid_login.yaml
  │   └── invalid_login.yaml
  ├── navigation/
  │   └── main_tabs.yaml
  └── common/
      └── clear_app_data.yaml
```

### 2.5 Create a Config File

Create `.maestro/config.yaml`:

```yaml
appId: com.example.yourapp
platform: android
```

---

## Phase 3: Build the App (If Testing from Source)

### 3.1 Install Gradle Wrapper (If Not Present)

```bash
# In your Android project root:
if [ ! -f ./gradlew ]; then
    gradle wrapper --gradle-version 8.4
fi
chmod +x ./gradlew
```

### 3.2 Build Debug APK

```bash
./gradlew assembleDebug
```

Locate the APK:
```bash
find . -name "*.apk" -type f
# Typically at: app/build/outputs/apk/debug/app-debug.apk
```

### 3.3 Install APK to Connected Device or Emulator

First, start an emulator:
```bash
# Create an AVD if needed
avdmanager create avd -n test_device -k "system-images;android-34;google_apis;x86_64"

# Start the emulator
emulator -avd test_device -no-window -no-audio &
# Wait for emulator to boot
adb wait-for-device
adb shell getprop sys.boot_completed
```

Then install the APK:
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## Phase 4: Running Tests

### 4.1 Run Maestro Tests Locally

```bash
maestro test .maestro/
```

Run a specific flow:
```bash
maestro test .maestro/Flow.yaml
```

Run with debug output:
```bash
maestro test .maestro/ --debug
```

### 4.2 Run with Maestro Cloud (Free Tier - 200 min/month)

First, get your API key from https://cloud.maestro.dev

```bash
maestro login
# Enter your API key when prompted
```

Run tests on Maestro Cloud:
```bash
maestro cloud .maestro/
# or with specific app file:
maestro cloud --app-file=app/build/outputs/apk/debug/app-debug.apk .maestro/
```

### 4.3 Run with Appium

Start Appium server:
```bash
appium --address 0.0.0.0 --port 4723 &
```

Run Appium tests (requires WebDriverAgent or UiAutomator2):
```bash
# Using appium CLI or your test runner
npx appium
```

---

## Phase 5: Visual Look & Feel Review with MiniMax MCP

After each test run, automatically capture screenshots and use MiniMax MCP to analyze the app's visual appearance, UI consistency, layout, and overall user experience.

### 5.1 Create a Screenshot Capture Flow

Create `.maestro/screenshot_flow.yaml`:

```yaml
appId: com.example.yourapp
---
# Flow to capture screenshots at key points for visual review
- launchApp
- waitForAnimationToEnd
- runScript:
    name: captureScreenshot
    args:
      path: /tmp/screenshots/login_screen.png
      element: "root"  # captures full screen
- waitForTimeout: 1000
- tapOn:
    id: "login_button"
- waitForTimeout: 500
- runScript:
    name: captureScreenshot
    args:
      path: /tmp/screenshots/after_login.png
      element: "root"
```

### 5.2 Manual Screenshot Capture Script

Alternatively, capture screenshots directly via ADB during your test run:

```bash
#!/bin/bash
# capture_screenshots.sh
# Run this script in the background or as part of your CI pipeline

OUTPUT_DIR="/tmp/maestro-screenshots"
mkdir -p "$OUTPUT_DIR"

SCREEN_NUMBER=0
while [ $# -gt 0 ]; do
    SCREEN_NAME=$1
    adb exec-out screencap -p > "$OUTPUT_DIR/${SCREEN_NUMBER}_${SCREEN_NAME}.png"
    SCREEN_NUMBER=$((SCREEN_NUMBER + 1))
    shift
done

echo "Captured $SCREEN_NUMBER screenshots to $OUTPUT_DIR"
ls -la "$OUTPUT_DIR"
```

Make it executable:
```bash
chmod +x capture_screenshots.sh
```

### 5.3 Automated Screenshot Capture After Each Test Flow

Modify your Maestro test to capture screenshots at critical points:

```yaml
# Example: .maestro/visual_review_flow.yaml
appId: com.example.yourapp
---
- launchApp
- waitForAnimationToEnd
- name: CaptureLoginScreen
  script:
    file: capture_screen.sh
    args:
      name: "01_login_screen"
      output_dir: "/tmp/maestro-screenshots"

- assertVisible:
    id: "login_button"
- tapOn:
    id: "login_button"
- waitForTimeout: 1000
- name: CaptureHomeScreen
  script:
    file: capture_screen.sh
    args:
      name: "02_home_screen"
      output_dir: "/tmp/maestro-screenshots"

- assertVisible:
    text: "Welcome"
```

### 5.4 Use MiniMax MCP to Analyze Screenshots

Once screenshots are captured, use the MiniMax image understanding MCP tool to perform AI-powered visual review of the app's look and feel. Create a review script:

```bash
#!/bin/bash
# review_screenshots.sh
# Analyzes captured screenshots using MiniMax MCP for look and feel

OUTPUT_DIR="/tmp/maestro-screenshots"
REPORT_FILE="/tmp/maestro-screenshots/visual_review_report.md"

echo "# Visual Look & Feel Review Report" > "$REPORT_FILE"
echo "Generated: $(date)" >> "$REPORT_FILE"
echo "" >> "$REPORT_FILE"

for screenshot in "$OUTPUT_DIR"/*.png; do
    SCREENSHOT_NAME=$(basename "$screenshot")
    echo "## Analyzing: $SCREENSHOT_NAME" >> "$REPORT_FILE"
    echo "" >> "$REPORT_FILE"

    # MiniMax MCP analysis is performed via the Claude Code conversation
    # The MCP tool call format for image analysis is:
    # mcp__MiniMax__understand_image with prompt and image_source
    #
    # Use this prompt template for look and feel review:
    # PROMPT="Analyze this mobile app screenshot for:
    # 1. Visual design quality (colors, contrast, spacing)
    # 2. Layout and alignment issues
    # 3. Typography readability
    # 4. Icon clarity and consistency
    # 5. Overall UI/UX polish
    # 6. Any visual bugs (overlapping elements, cut-off text, etc.)
    # Provide a score from 1-10 and specific improvement suggestions."

    echo "Screenshot: $screenshot" >> "$REPORT_FILE"
    echo "Review: [Attach MiniMax MCP analysis here]" >> "$REPORT_FILE"
    echo "" >> "$REPORT_FILE"
done

echo "Visual review report saved to: $REPORT_FILE"
```

### 5.5 Integrated Test + Visual Review Pipeline

Create a master pipeline script that runs tests AND captures screenshots AND invokes MiniMax analysis:

```bash
#!/bin/bash
# run_tests_with_visual_review.sh

set -e

OUTPUT_DIR="/tmp/maestro-screenshots"
REPORT_FILE="/tmp/maestro-screenshots/visual_review_report.md"
APP_APK="${1:-app/build/outputs/apk/debug/app-debug.apk}"

echo "=== Starting Android Automated Test with Visual Review ==="
echo "APK: $APP_APK"
echo "Output: $OUTPUT_DIR"
echo ""

# Clean and create output directory
rm -rf "$OUTPUT_DIR"
mkdir -p "$OUTPUT_DIR"

# Step 1: Run Maestro tests and capture screenshots
echo ">>> Running Maestro UI tests..."
maestro test .maestro/ \
  --report="/tmp/maestro-report" \
  --on-error=Screenshot

# Step 2: Capture key screenshots via ADB for specific screens
echo ""
echo ">>> Capturing screenshots for visual analysis..."

# Capture home screen
adb shell screencap -p /sdcard/home_screen.png
adb pull /sdcard/home_screen.png "$OUTPUT_DIR/01_home_screen.png" 2>/dev/null || true

# Capture login screen (if applicable)
adb shell screencap -p /sdcard/login_screen.png
adb pull /sdcard/login_screen.png "$OUTPUT_DIR/02_login_screen.png" 2>/dev/null || true

# Capture settings or profile screen
adb shell screencap -p /sdcard/profile_screen.png
adb pull /sdcard/profile_screen.png "$OUTPUT_DIR/03_profile_screen.png" 2>/dev/null || true

# Step 3: Generate manifest of captured screenshots
echo ""
echo ">>> Screenshot manifest:"
ls -la "$OUTPUT_DIR"
echo ""

# Step 4: Analysis via MiniMax MCP
# For each screenshot, invoke MiniMax MCP understand_image tool
# The Claude Code instance will call:
# mcp__MiniMax__understand_image with:
#   prompt: "Analyze this mobile app screenshot for visual design quality, layout issues, typography, icon consistency, UI/UX polish, and any visual bugs. Score 1-10 with specific suggestions."
#   image_source: "/tmp/maestro-screenshots/<filename>.png"

echo ">>> Screenshots ready for MiniMax visual analysis"
echo ""
echo "=== Screenshots captured ==="
for f in "$OUTPUT_DIR"/*.png; do
    echo "  - $(basename $f): $(file $f | cut -d: -f2)"
done

echo ""
echo "=== Manual Step: Invoke MiniMax MCP ==="
echo "For each screenshot, use the MiniMax understand_image MCP tool with:"
echo "  prompt: 'Analyze this mobile app screenshot for: 1) Visual design quality (colors, contrast, spacing) 2) Layout and alignment 3) Typography readability 4) Icon clarity 5) UI/UX polish 6) Visual bugs. Score 1-10 with specific improvement suggestions.'"
echo "  image_source: <path to screenshot>"
echo ""
echo "Results will be output as Claude Code MCP responses."
```

Make it executable:
```bash
chmod +x run_tests_with_visual_review.sh
```

### 5.6 Example MiniMax MCP Image Analysis Prompts

When invoking `mcp__MiniMax__understand_image`, use these prompts for comprehensive look and feel review:

**Visual Design Analysis:**
```
Analyze this mobile app screenshot and provide:
1. **Color & Contrast**: Are colors harmonious? Is there sufficient contrast for readability?
2. **Spacing & Alignment**: Is padding consistent? Are elements properly aligned?
3. **Typography**: Is text readable? Are font sizes appropriate?
4. **Visual Hierarchy**: Can you identify primary, secondary, and tertiary content?
5. **Overall Polish**: Rate the visual appeal from 1-10.
6. **Issues Found**: List any visual bugs, overlapping elements, or cut-off content.
```

**UX/UI Consistency Review:**
```
Review this mobile app screenshot for:
1. **Design Consistency**: Does the UI follow consistent design patterns?
2. **Touch Target Sizes**: Are interactive elements large enough (minimum 48dp)?
3. **Navigation Clarity**: Is it clear how to navigate?
4. **Feedback**: Are there clear visual indicators for interactive states?
5. **Accessibility Concerns**: Note any potential accessibility issues.
6. **Recommendations**: Top 3 improvements to enhance the user experience.
```

---

## Phase 6: CI/CD Automation (GitHub Actions)

### 6.1 Create GitHub Actions Workflow

Create `.github/workflows/android-tests.yml`:

```yaml
name: Android UI Tests with Visual Review

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main, develop]

jobs:
  test:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'

      - name: Setup Android SDK
        uses: android-actions/setup-android@v3

      - name: Cache Gradle
        uses: actions/cache@v4
        with:
          path: ~/.gradle/caches
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*') }}
          restore-keys: ${{ runner.os }}-gradle-

      - name: Build debug APK
        run: ./gradlew assembleDebug

      - name: Run Maestro tests
        uses: mobile-dev-inc/maestro@latest
        with:
          maestro_version: latest
          app_file: app/build/outputs/apk/debug/app-debug.apk
          flow: .maestro/

      - name: Capture screenshots for visual review
        run: |
          mkdir -p screenshots
          adb exec-out screencap -p > screenshots/test_screen.png || true

      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: maestro-results
          path: |
            .maestro/
            screenshots/
            build/reports/

      - name: Visual Review (Manual MiniMax Step)
        run: |
          echo "Screenshots captured. Uploaded as artifacts."
          echo "To perform visual review, download artifacts and use MiniMax MCP."
```

### 6.2 For Self-Hosted Runner on Rocky Linux (Unlimited Free Execution)

On your Rocky Linux machine, register a self-hosted runner:

1. Go to GitHub → Repository → Settings → Actions → Runners
2. Click "New self-hosted runner"
3. Choose Linux x64
4. Download and extract the runner:
```bash
mkdir actions-runner && cd actions-runner
curl -o actions-runner.tar.gz https://github.com/actions/runner/releases/download/v2.317.0/actions-runner-linux-x64-2.317.0.tar.gz
tar xzf actions-runner.tar.gz
```
5. Configure:
```bash
./config.sh --url https://github.com/YOUR_ORG/YOUR_REPO --token YOUR_TOKEN
./run.sh
```

Or run as a service:
```bash
sudo ./svc.sh install
sudo ./svc.sh start
```

---

## Phase 7: Troubleshooting

### Android SDK Issues

**Problem:** `sdkmanager: command not found`
```bash
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin
source ~/.bashrc
```

**Problem:** License not accepted
```bash
yes | sdkmanager --licenses
```

### Emulator Issues

**Problem:** KVM permission denied
```bash
sudo usermod -aG kvm $USER
# Log out and back in
```

**Problem:** Emulator hangs at boot
```bash
# Try with cold boot
emulator -avd test_device -no-window -no-audio -no-snapshot-load -no-snapshot-save &
```

### Maestro Issues

**Problem:** `maestro: command not found`
```bash
export PATH="$PATH:$HOME/.maestro/bin"
source ~/.bashrc
```

**Problem:** App not installed
```bash
# Check app ID matches your APK
adb shell pm list packages | grep your-app-id
```

**Problem:** Element not found
```bash
# Use Maestro Inspector to find correct selectors
maestro inspect
```

### Maestro CLI Version Compatibility (v2.4.0)

**Maestro version 2.4.0 syntax differences:**
- Use full path: `~/.maestro/bin/maestro`
- Do NOT use `--report=` flag (not supported)
- Use `--format=HTML` for HTML reports
- Use `--output=<filename>` for output file
- Use `--test-output-dir=<dir>` for screenshots and test artifacts
- Use `--udid=<device-id>` to specify device

**Correct Maestro 2.4.0 command syntax:**
```bash
~/.maestro/bin/maestro test \
    -p=android \
    --udid="39061FDJG0032C" \
    --format=HTML \
    --output=/tmp/maestro-report.html \
    --test-output-dir=/tmp/maestro-test-output \
    .maestro/tests/
```

**Test YAML syntax notes:**
- Use `scroll` instead of `scrollDown` or `scrollUp`
- Use `point: x,y` instead of `x: value, y: value` for coordinates
- Use `text: "exact text"` for assertions (no partial matching like `textContains`)
- Use `id: "element_id"` for element ID selectors when available

### USB Device Connection Issues

**Problem:** Device shows `no permissions (missing udev rules?)` or `unauthorized`
```bash
# Check if device is detected
adb devices -l

# Device ID format: xxxxxxxx where x is alphanumeric string
# For Pixel 8 Pro: 39061FDJG0032C
```

**Solution:** Create udev rules for Android USB devices

```bash
# Create udev rules file
sudo cat > /etc/udev/rules.d/51-android.rules << 'EOF'
# Google Pixel and Android devices
SUBSYSTEM=="usb", ATTR{idVendor}=="18d1", MODE="0666", GROUP="plugdev", ENV{android_iface}="*"
SUBSYSTEM=="usb", ATTR{idVendor}=="18d1", ATTR{idProduct}=="4ee7", MODE="0666", GROUP="plugdev"
SUBSYSTEM=="usb", ATTR{idVendor}=="18d1", ATTR{idProduct}=="4ee6", MODE="0666", GROUP="plugdev"

# Generic Android device (backup)
SUBSYSTEM=="usb", ENV{DEVTYPE}=="usb_device", MODE="0666", GROUP="plugdev"
EOF

# Reload udev rules
sudo udevadm control --reload-rules
sudo udevadm trigger

# Restart ADB server with sudo
sudo adb kill-server
sudo adb start-server
```

**Verify device is properly connected:**
```bash
# Should show device with "device" status (not "unauthorized" or "no permissions")
adb devices -l
# Example output:
# 39061FDJG0032C         device usb:2-6 product:husky model:Pixel_8_Pro device:husky
```

**If device shows "unauthorized":**
1. Check your phone screen - you may need to authorize the computer
2. Look for an RSA authorization dialog on the device
3. Check the "Always allow from this computer" checkbox
4. Tap "Allow"

### Screenshot Capture Issues

**Problem:** ADB screencap returns empty file
```bash
# Ensure adb is running and device is connected
adb devices
adb shell ls -la /sdcard/
adb exec-out screencap -p > /tmp/test.png
# Verify file size is non-zero
ls -la /tmp/test.png
```

**Problem:** Screenshots directory not writable
```bash
mkdir -p /tmp/maestro-screenshots
chmod 755 /tmp/maestro-screenshots
adb shell mkdir -p /sdcard/
adb shell chmod 777 /sdcard/
```

---

## Phase 8: Verify Everything Works

Run this verification sequence:

```bash
# 1. Verify Java
java -version

# 2. Verify Android SDK
echo $ANDROID_HOME
sdkmanager --list_installed

# 3. Verify Maestro
maestro --version

# 4. Verify APK exists
ls -la app/build/outputs/apk/debug/app-debug.apk

# 5. Run tests
maestro test .maestro/

# 6. Capture a screenshot for visual review
adb exec-out screencap -p > /tmp/test_screenshot.png
ls -la /tmp/test_screenshot.png

# 7. Check exit code
echo $?
# 0 = success, non-zero = failure
```

---

## Quick Reference - One-liner Setup

For a fresh Rocky Linux 10 machine, run all setup in one sequence:

```bash
# Install system deps
sudo dnf install -y git curl wget unzip java-17-openjdk java-17-openjdk-devel python3 python3-pip

# Setup Android SDK
mkdir -p ~/android-sdk/cmdline-tools
cd ~/android-sdk/cmdline-tools
wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O cmdline-tools.zip
unzip -q cmdline-tools.zip && mv cmdline-tools latest && rm cmdline-tools.zip
export ANDROID_HOME=~/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools
yes | sdkmanager --licenses > /dev/null 2>&1
sdkmanager --install "platform-tools" "platforms;android-34" "build-tools;34.0.0"

# Install Maestro
curl -fsSL "https://get.maestro.mobile.dev" | bash
export PATH="$PATH:$HOME/.maestro/bin"

# Navigate to project and run tests
cd /path/to/your/android/project
maestro test .maestro/

# Capture screenshots for visual review
mkdir -p screenshots
adb exec-out screencap -p > screenshots/ui_screen.png
```

---

## Quick Reference - MiniMax Visual Review Commands

After screenshots are captured, use these MiniMax MCP calls to analyze look and feel:

```bash
# Example: Analyze a screenshot with MiniMax MCP
# In Claude Code, invoke:
# mcp__MiniMax__understand_image with:
#   prompt: "Analyze this mobile app screenshot for visual design quality, layout issues, typography readability, icon consistency, UI/UX polish, and any visual bugs. Score 1-10 and provide specific improvement suggestions."
#   image_source: "/path/to/screenshot.png"
```

---

## Output Interpretation

- **Exit code 0** + all assertions passed = Tests succeeded
- **Non-zero exit code** = Tests failed (check Maestro output for failed assertions)
- **Timeout** = Test took >15 min on Maestro Cloud (local runner has no limit)
- **Screenshot analysis scores** = MiniMax MCP output (1-10 scale, higher is better)

Save your test reports:
```bash
maestro test .maestro/ --report=/tmp/maestro-report
```

Save visual review reports:
```bash
# Generate markdown report from screenshots
echo "# Visual Review" > /tmp/visual_report.md
echo "Date: $(date)" >> /tmp/visual_report.md
echo "" >> /tmp/visual_report.md
for f in /tmp/maestro-screenshots/*.png; do
    echo "## $(basename $f)" >> /tmp/visual_report.md
    echo "Use MiniMax MCP to analyze: $f" >> /tmp/visual_report.md
    echo "" >> /tmp/visual_report.md
done
```
