# sbGitBlame - IntelliJ IDEA Plugin

Ein schnelles, ressourcenschonendes IntelliJ IDEA Plugin, das neben der Code-Zeile im Editor die Git-Push-/Commit-Informationen anzeigt:
- 👤 **Autor** (`{author}`)
- 📅 **Datum / Zeit** (`{date}`) – wahlweise relativ (*„vor 2 Tagen“*) oder formatiert (*„2024-05-12“*)
- 💬 **Commit-Nachricht / Kommentar** (`{message}`) – z. B. `Fix null pointer exception in parser`
- 🔢 **Kurzer Commit-Hash** (`{hash}`) – z. B. `a1b2c3d` am Ende der Zeile

---

## ✨ Features

- **Cursor-Fokus als Standard**: Blendet die Git-Informationen dezent auf der aktuell aktiven Cursor-Zeile ein (mit Umschaltmöglichkeit auf *Alle Zeilen*).
- **Leere Zeilen werden ignoriert**: Zeilen ohne Code zeigen keine überflüssigen Annotationen an.
- **Asynchron & Non-Blocking**: Blame-Daten werden im Hintergrund asynchron über `git blame --line-porcelain` abgefragt und gecacht.
- **Vollständig anpassbar**:
  - Eigenes Format-Template definieren (Standard: `{author}, {date} • {message} • {hash}`).
  - Maximale Länge der Commit-Nachricht konfigurierbar.
  - Umschalten zwischen relativer und absoluter Datumsanzeige.
  - Kursiver oder normaler Schriftstil.
- **Schnell-Umschaltung**: Über Tastenkürzel (`Strg + Alt + G` bzw. `Ctrl + Alt + G`), Editor-Kontextmenü oder Menüleiste `View -> Toggle sbGitBlame`.

---

## ⚙️ Einstellungen

In IntelliJ IDEA unter:
> **Settings / Preferences** ➔ **Tools** ➔ **sbGitBlame**

---

## 🛠️ Build & Installation

### Plugin aus Quellcode bauen
```bash
# Auf Windows (PowerShell / CMD)
.\gradlew.bat buildPlugin

# Auf Linux / macOS
./gradlew buildPlugin
```

Das fertige Plugin-Archiv wird unter folgendem Pfad erzeugt:
`build/distributions/sbGitBlame-1.0.7.zip`

### Plugin in IntelliJ IDEA installieren
1. Öffne IntelliJ IDEA.
2. Gehe zu **Settings** (`Ctrl+Alt+S` / `Cmd+,`) ➔ **Plugins**.
3. Klicke oben rechts auf das Zahnrad-Symbol ⚙️ und wähle **„Install Plugin from Disk...“**.
4. Wähle die Datei `build/distributions/sbGitBlame-1.0.7.zip` aus.
5. Starte IntelliJ IDEA neu.
