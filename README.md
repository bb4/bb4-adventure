# bb4-adventure

📊 [Build status for all bb4 projects](https://github.com/bb4/.github)

A configurable story builder. Stories are configured using xml script files that reside in 
com/barryecker4/puzzle/adventure/stories.

Currently there is a Ludlow adventure based on a scenario from an old Dragon magazine, 
and a learnBinary tutorial that demonstrates that the framework can be used for things other than adventure gaming. 

This project started as a framework to create adventure games (hence the name), but then I realized it could have many other applications. It can be used to deliver any interactive story or tutorial. Furthermore, it could be used to edit any arbitrary graph or hierarchy such as the data for [aikido application builder]().

## Running it

**Option 1 — installer (recommended):** when a GitHub release is published, the
[Build Installers](https://github.com/bb4/bb4-adventure/actions/workflows/release.yml) workflow
builds native installers and attaches them to that release. Download the one for your OS from the
[latest release](https://github.com/bb4/bb4-adventure/releases/latest)
(macOS `.dmg`, Windows `.msi`, Linux `.deb` — one installer per story app).

| App | Installer |
|-----|-----------|
| Ludlow Adventure | `bb4-adventure-<version>` |
| Ludlow Text Adventure | `bb4-textadventure-<version>` |
| Binary Tutorial | `bb4-binarytutorial-<version>` |
| Binary Tutorial (text version) | `bb4-textbinarytutorial-<version>` |
| Aikido Editor | `bb4-aikidoeditor-<version>` |

The text editions read choices from a terminal. On Windows the installer opens a console. On Linux the menu item opens a terminal. On macOS, launch the executable inside the `.app` from Terminal.

### macOS installers (Gatekeeper)

Release `.dmg` files are built with [jpackage](https://docs.oracle.com/en/java/javase/21/docs/specs/man/jpackage.html)
and are **not** signed with an Apple Developer ID or notarized (we do not use Apple’s paid developer
program). macOS tags apps you install from the internet with a **quarantine** flag and may refuse to
run them, showing *“could not verify … is free of malware”* with only **Move to Trash** — not because
the build is infected, but because Gatekeeper does not recognize the developer.

That extra step is unfortunate friction for users; the tradeoff is shipping free, unsigned open-source
installers. The app is open source here, or you can build the same `.dmg` yourself with
`./gradlew jpackageAdventure` (or `jpackageBinaryTutorial`, `jpackageAikidoEditor`, …).

**After installing from the `.dmg` (required on most current Macs):**

1. Drag the app into **Applications** as usual. Note the exact name (e.g. `Ludlow Adventure.app`).

2. In **Terminal**, remove the quarantine attribute **once per app** (only if you trust this GitHub
   release):

   ```bash
   xattr -dr com.apple.quarantine "/Applications/Ludlow Adventure.app"
   ```

   Replace `Ludlow Adventure` with the app you installed (`Binary Tutorial`, `Aikido Editor`,
   `Ludlow Text Adventure`, or `Binary Tutorial (text version)`). Then open it normally.

   Without this step, **Control‑click → Open** in Finder and Apple’s **Open Anyway** in System
   Settings often **do not** help on recent macOS; quarantine removal is what usually works.

**If it still will not launch:** open the app once, then **System Settings → Privacy & Security →
Security → Open Anyway** for that app.

**Option 2 — from source:**

### [How to Build](https://github.com/barrybecker4/bb4-common/wiki/Building-bb4-Projects)

If you have not already done so, first install [Git](http://git-scm.com/), and [Intellij](http://www.jetbrains.com/idea/).

Requires **JDK 25+** (bb4-gradle 2.1). Type `./gradlew build run` at the root. This will start the adventure game.
`./gradlew jpackageAllApps` builds every native installer for the current OS (`.deb` on Linux, `.dmg` on macOS, `.msi` on Windows).
If you want to open the source in Intellij, then first run `./gradlew idea`.

When there is a new release, versioned artifacts will be published by Barry Becker to [Sonatype](https://oss.sonatype.org).

### License
All source (unless otherwise specified in individual file) is provided under the [MIT License](http://www.opensource.org/licenses/MIT)






