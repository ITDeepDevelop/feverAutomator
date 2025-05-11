# ✅ Avvio progetto con JavaFX

```bash
mvn clean compile
mvn javafx:run
```

# ✅ Avvio progetto con JAR
Questa guida spiega come avviare il progetto JavaFX che integra **Playwright** per l'automazione su browser.

---

## 🧱 Prerequisiti

- Java 17 installato
- JavaFX 21.0.7 installato
- Maven installato (`mvn -v`)
- Browser Playwright (**non inclusi** nel JAR) installati a parte

---

## 🚀 Avvio rapido con script

Sono forniti due script: uno per Unix/macOS/Linux, uno per Windows.

---

### 🖥️ macOS / Linux – `run.sh`

1. Crea lo script `run.sh` nella root del progetto:

```bash
#!/bin/bash

mvn clean package

# === CONFIGURAZIONE ===
JAR_FILE="target/automator-1.0.jar"
JAVAFX_SDK_PATH="/Users/david/Library/Frameworks/JavaFX/javafx-sdk-21.0.7"

# === PLAYWRIGHT CACHE CHECK ===
PLAYWRIGHT_CACHE="$HOME/.cache/ms-playwright"

echo "[INFO] Verifico presenza dei browser Playwright..."
if [ ! -d "$PLAYWRIGHT_CACHE" ] || [ -z "$(ls -A "$PLAYWRIGHT_CACHE")" ]; then
  echo "[INFO] Browser non trovati. Li installo..."
  mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install"
else
  echo "[INFO] Browser già installati."
fi

# === AVVIO APPLICAZIONE ===
echo "[INFO] Avvio l'applicazione JavaFX..."

java --module-path "$JAVAFX_SDK_PATH/lib" --add-modules javafx.controls,javafx.fxml -jar "$JAR_FILE"
```

2. Rendi lo script eseguibile e esegui (macOs/Linux)

```bash
chmod +x run.sh
./run.sh
```

2. Rendi lo script eseguibile e esegui (Windows)

```bat
@echo off
set JAR_FILE=target\fever-automator.jar
set CACHE_DIR=%USERPROFILE%\.cache\ms-playwright

echo [INFO] Verifico presenza dei browser Playwright...

if not exist "%CACHE_DIR%" (
    echo [INFO] Browser non trovati. Li installo...
    mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install"
) else (
    echo [INFO] Browser già installati.
)

echo [INFO] Avvio l'applicazione...
java -jar %JAR_FILE%

```
---

## ℹ️ Note
I browser vengono installati solo se non sono già presenti

Se desideri includere tutto in un pacchetto unico, valuta l'uso di Docker

Il file JAR viene generato da Maven con:

```bash
mvn clean package
```

