# sbGitBlame - IntelliJ IDEA Plugin

> 🤖 **100% AI Generated**: This entire IntelliJ IDEA plugin — including its architecture, Kotlin source code, asynchronous Git porcelain parser, UI settings dialog, Gradle configuration, and documentation — was **100% created by AI**.

A lightweight, high-performance IntelliJ IDEA plugin that displays inline Git commit and push information directly next to the code line in the editor:
- 👤 **Author** (`{author}`)
- 📅 **Date / Time** (`{date}`) – relative (e.g. *"2 days ago"*) or formatted (e.g. *"2024-05-12"*)
- 💬 **Commit Message / Comment** (`{message}`) – e.g. *Fix null pointer exception in parser*
- 🔢 **Short Commit Hash** (`{hash}`) – e.g. `a1b2c3d` at the end of the line

---

## ✨ Features

- **Active Cursor Line Focus by Default**: Displays git blame metadata cleanly on the currently active cursor line without visual clutter.
- **Blank Lines Ignored**: Empty lines or whitespace-only lines do not display annotations.
- **Asynchronous & Non-Blocking**: Fetches git metadata in background threads using `git blame --line-porcelain` and caches results in-memory with automatic cache invalidation.
- **Fully Customizable**:
  - Format template (Default: `{author}, {date} • {message} • {hash}`).
  - Maximum commit message length with automatic ellipsis (`...`).
  - Toggle between relative and absolute date formatting.
  - Font styling (italic/plain) and prefix spacing.
  - Display mode: *Current cursor line only* (default) or *All lines in editor*.
- **Quick Toggle**: Easily enable or disable annotations via keyboard shortcut (`Ctrl + Alt + G`), editor context menu, or menu bar `View -> Toggle sbGitBlame`.

---

## ⚙️ Configuration

In IntelliJ IDEA, go to:
> **Settings / Preferences** ➔ **Tools** ➔ **sbGitBlame**

Here you can customize formatting templates, date patterns, maximum message length, and view a live preview.

---

## 🛠️ Build & Installation

### Building the Plugin from Source
Prerequisites: Java 17+ (or IntelliJ IDEA's bundled JBR) and Git.

```bash
# On Windows (PowerShell / Command Prompt)
.\gradlew.bat buildPlugin

# On Linux / macOS
./gradlew buildPlugin
```

The compiled plugin distribution archive will be created at:
`build/distributions/sbGitBlame-1.0.10.zip`

### Installing in IntelliJ IDEA
1. Open IntelliJ IDEA.
2. Go to **Settings** (`Ctrl + Alt + S` / `Cmd + ,`) ➔ **Plugins**.
3. Click the gear icon ⚙️ in the top-right corner and select **"Install Plugin from Disk..."**.
4. Select the generated archive `build/distributions/sbGitBlame-1.0.10.zip`.
5. Restart IntelliJ IDEA if prompted.

---

## 📂 Project Structure

```
sbGitBlame/
├── .github/workflows/build.yml # GitHub Actions CI workflow for builds & tests
├── build.gradle.kts            # Gradle build script & IntelliJ Platform configuration
├── settings.gradle.kts         # Project settings
├── gradle.properties           # Plugin & platform version properties
├── src/
│   ├── main/
│   │   ├── kotlin/com/sambabyte/gitblame/
│   │   │   ├── actions/
│   │   │   │   └── ToggleGitBlameAction.kt     # Toggle action & shortcut handler
│   │   │   ├── listener/
│   │   │   │   └── GitBlameCaretListener.kt    # Caret listener for cursor line updates
│   │   │   ├── model/
│   │   │   │   ├── BlameInfo.kt                # Line blame data model & formatting
│   │   │   │   └── BlameFileResult.kt          # File blame cache result
│   │   │   ├── painter/
│   │   │   │   └── GitBlameLinePainter.kt      # EditorLinePainter for inline annotations
│   │   │   ├── service/
│   │   │   │   ├── GitBlameParser.kt           # Parser for git blame --line-porcelain
│   │   │   │   └── GitBlameService.kt          # Asynchronous background service & cache
│   │   │   └── settings/
│   │   │       ├── GitBlameSettingsState.kt    # Persistent settings state component
│   │   │       └── GitBlameConfigurable.kt     # Swing configuration UI & live preview
│   │   └── resources/
│   │       └── META-INF/
│   │           └── plugin.xml                  # Plugin descriptor & extension points
│   └── test/
│       └── kotlin/com/sambabyte/gitblame/
│           └── GitBlameParserTest.kt           # Unit tests for porcelain parsing & formatting
└── README.md
```

---

## 🤖 AI Origin Note

This project was 100% engineered and created by Artificial Intelligence (Google Antigravity / Gemini) based on pair-programming prompts.

---

## 📄 License

This project is licensed under the Apache 2.0 License.
