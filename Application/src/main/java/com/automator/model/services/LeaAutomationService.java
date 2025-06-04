package com.automator.model.services;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;

public class LeaAutomationService {

    public boolean op1() {
        // logica vera dell'operazione 1
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium()
                    .launch(new BrowserType.LaunchOptions()
                            .setHeadless(false)
                    );
            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            page.navigate("https://example.com");
            page.waitForTimeout(5000);

            page.close();
            browser.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean downloadLicense() {
        int maxAttempts = 2;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try (Playwright playwright = Playwright.create()) {
                // 1. Avvia il browser in modalità non-headless (per debug)
                Browser browser = playwright.chromium()
                        .launch(new BrowserType.LaunchOptions()
                                .setHeadless(false)
                        );

                // 2. Crea un nuovo contesto abilitando i download (senza specificare downloadsPath)
                BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                        .setAcceptDownloads(true)
                );

                // 3. Apri una nuova pagina
                Page page = context.newPage();

                // 4. Naviga alla home di Soundreef
                page.navigate("https://licence.soundreef.com/it/");

                // 5. Clicca sul link di login (Accedi person) <-- getByRole su Page, uso Page.GetByRoleOptions
                page.getByRole(AriaRole.NAVIGATION)
                        .getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName("Accedi person"))
                        .click();

                // 6. Inserisci username e password
                page.locator("#username").click();
                page.locator("#username").fill("anne.schmitz@feverup.com");

                page.locator("#password").click();
                page.locator("#password").fill("kzemositaly2024!");

                // 7. Clicca sul pulsante "Accedi" <-- getByRole su Page, uso Page.GetByRoleOptions
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Accedi")).click();

                // 7.5. Se compare il banner dei cookie, accettalo <-- qui basta getByRole su Page
                Locator allowCookies = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Allow all cookies"));
                if (allowCookies.count() > 0) {
                    allowCookies.click();
                }

                // 8. Dopo il login, clicca su "Licenze description" nel menu di navigazione
                //    Primo getByRole su Page (per NAVIGATION), quindi su Locator per il link
                page.getByRole(AriaRole.NAVIGATION)
                        .getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName("Licenze description"))
                        .click();

                // 9. Naviga esplicitamente all’URL delle licenze
                page.navigate("https://licence.soundreef.com/it/licenses");

                // 10. Clicca sul titolo specifico di una licenza
                page.getByText("Candlelight: tributo a Pino Daniele ed altri @Chiesa di San Giorgio h 20:")
                        .click();

                // 11. Intercetta il download e clicca su "Scarica licenza"
                Download download = page.waitForDownload(() -> {
                    // getByRole su Page per il link di download
                    page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Scarica licenza"))
                            .click();
                });

                // 12. Salva il file scaricato nella cartella "LeaDownloads" sul Desktop dell’utente
                String userHome = System.getProperty("user.home");
                Path desktopDownloads = Paths.get(userHome, "Desktop", "LeaDownloads");
                // Creazione della cartella se non esiste
                Files.createDirectories(desktopDownloads);
                Path targetPath = desktopDownloads.resolve(download.suggestedFilename());
                download.saveAs(targetPath);
                System.out.println("File salvato in: " + targetPath.toAbsolutePath());


                // 13. Torna alla pagina delle licenze (opzionale)
                page.navigate("https://licence.soundreef.com/it/licenses");

                // 14. Chiudi contesto e browser
                context.close();
                browser.close();

                // Se siamo arrivati fin qui, il download è andato a buon fine
                return true;
            } catch (Exception e) {
                System.err.println("❌ Errore al tentativo " + attempt);
                e.printStackTrace();
                if (attempt == maxAttempts) {
                    System.err.println("⛔ Tutti i tentativi falliti.");
                    return false;
                }
                // al prossimo tentativo ricreiamo Playwright/browser/context
            }
        }
        return false;
    }
}
