package com.automator.model.services;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.automator.model.services.EventoRow.toPDFName;

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

    public boolean downloadLicense( String email, String password, String month, String year) {
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
                page.locator("#username").fill(email);

                page.locator("#password").click();
                page.locator("#password").fill(password);

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

                List<EventoRow> toDownload = new ArrayList<>();
                try {
                    com.automator.model.services.ExcelReader reader = new ExcelReader();
                    reader.read(ExcelStorage.getInstance().getFile());
                    toDownload = reader.getEventoRows();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                for (EventoRow event : toDownload) {
                    System.out.println("Analizzo il PDF: " + event);
                    page.navigate("https://licence.soundreef.com/it/licenses");
                    page.waitForTimeout(1000);

                    String eventName = event.getNomeEventoELocation();
                    int index = eventName.indexOf(" - ");
                    if (index != -1) {
                        eventName = eventName.substring(0, index);
                    }

                    if(eventName.isEmpty()) break;

                    // 10. Trova titolo parziale di una licenza
                    Locator matches = page.getByText(
                            eventName,
                            new Page.GetByTextOptions().setExact(false)
                    );

                    int count = matches.count();
                    if (count == 0) {
                        System.out.println("Nessun elemento trovato per: " + eventName);
                        continue;
                    }

                    System.out.println("Trovati " + count + " elementi per: " + eventName);
                    boolean reachedRightTime = false;

                    for (int i = 0; i < count; i++) {
                        page.navigate("https://licence.soundreef.com/it/licenses");
                        Locator single = matches.nth(i);
                        // stampa il testo esatto che stiamo per cliccare (opzionale, per debug)
                        System.out.println("  → Clicco su: " + single.innerText());
                        String time = extractTime(single.innerText());

                        // clicca l’i‐esimo elemento
                        single.click();

                        Locator dateLocator = page.locator("#license_start_date");
                        dateLocator.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED));
                        // Leggi l’attributo “value”
                        String dateValue = dateLocator.getAttribute("value");

                        int yearToUse = Integer.parseInt(year);
                        int monthToUse = Integer.parseInt(month);

                        boolean rightTime = matchesYearMonth(dateValue, yearToUse, monthToUse);
                        if (rightTime) {
                            reachedRightTime = true;
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
                            String fileName = toPDFName(event.getNomeEventoELocation(), event.getDataEvento(), time);
                            Path targetPath = desktopDownloads.resolve(fileName);
                            download.saveAs(targetPath);
                            System.out.println("File salvato in: " + targetPath.toAbsolutePath());

                        }
                        else if(reachedRightTime) break; //sono in ordine cronologico, la prima riga con data non valida sarà seguita da sole righe con data non valida
                    }
                }

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

    public boolean matchesYearMonth(String value, int expectedYear, int expectedMonth) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            LocalDate date = LocalDate.parse(value); // il parser si aspetta "YYYY-MM-DD"
            return date.getYear() == expectedYear && date.getMonthValue() == expectedMonth;
        } catch (DateTimeParseException e) {
            // Se il formato non è "yyyy-MM-dd", restituiamo false
            return false;
        }
    }

    public String extractTime(String text) {
        if (text == null) return "";

        // 1) Trova "H" seguito da orario tipo 22:00 (case-insensitive sulla H)
        Pattern pattern = Pattern.compile("(?i)H\\s*(\\d{1,2}:\\d{2})");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            // 2) group(1) è ad esempio "22:00"
            String timeWithColon = matcher.group(1);
            // 3) Rimuove tutto ciò che non è cifra, ottenendo "2200"
            return timeWithColon.replaceAll("\\D", "");
        }

        return "";
    }
}
