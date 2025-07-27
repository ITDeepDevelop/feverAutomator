package com.automator.model.services;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.automator.model.services.EventoRow.toPDFName;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class LeaAutomationService {
    private static final Logger logger = LogManager.getLogger(LeaAutomationService.class);

    private static final String BASE_URL = "https://licence.soundreef.com/it";
    private static final String LOGIN_URL = BASE_URL + "/login";
    private static final String LICENSES_URL = BASE_URL + "/licenses";

    private static final String SELECTOR_COOKIE_ACCEPT = "#CybotCookiebotDialogBodyButtonAccept";
    private static final String SELECTOR_EVENT_CONFIRM_LINK = "a:has-text('Eventi da confermare')";
    private static final String SELECTOR_EVENT_BOX = "a.box.rounded.license";
    private static final String SELECTOR_EVENT_NAME = "input#license_form_lm_event_name";
    private static final String SELECTOR_CITY_INPUT = "input#venue_city";
    private static final String SELECTOR_POSTCODE = "input#venue_postcode";
    private static final String SELECTOR_CAPACITY_MAIN = "input#license_form_lm_venue_capacity";
    private static final String SELECTOR_CAPACITY_ALT = "input[name='lm_venue_capacity']";
    private static final String SELECTOR_CONFIRM_BUTTON = "a#cmd-event-approval.btn.primary";
    private static final String SELECTOR_CHECKBOX_1 = "label[for='consent_1']";
    private static final String SELECTOR_CHECKBOX_7 = "label[for='consent_7']";
    private static final String SELECTOR_GENERATE_BUTTON = "button[type='submit'].waves-effect.btn.primary";

    private List<EventoRow> eventoRows;

    public void setEventoRows(List<EventoRow> rows) {
        this.eventoRows = rows;
    }

    public boolean executeAutomation(String email, String password) {
        if (!loadExcelData()) return false;

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            Page page = browser.newContext().newPage();

            navigateAndLogin(page, email, password);
            processPendingEvents(page);

            return true;
        } catch (Exception e) {
            logger.error("Web automation failed: {}", e.getMessage(), e);
            return false;
        }
    }

    private boolean loadExcelData() {
        logger.info("Loading events from Excel...");
        if (ExcelStorage.getInstance().getFile() == null) {
            logger.error("No Excel file loaded.");
            return false;
        }
        try {
            ExcelReader reader = new ExcelReader();
            reader.read(ExcelStorage.getInstance().getFile());
            this.eventoRows = reader.getEventoRows();

            if (eventoRows == null || eventoRows.isEmpty()) {
                logger.error("No events found in Excel file.");
                return false;
            }
            logPreview(eventoRows);
            return true;
        } catch (Exception e) {
            logger.error("Error reading Excel: {}", e.getMessage(), e);
            return false;
        }
    }

    private void logPreview(List<EventoRow> list) {
        logger.info("Loaded {} events.", list.size());
        for (int i = 0; i < Math.min(5, list.size()); i++) {
            EventoRow e = list.get(i);
            logger.info("{}. {} - {} (CAP: {}, Capienza: {})", i+1, e.getNomeEventoELocation(), e.getCitta(), e.getCap(), e.getCapienza());
        }
        if (list.size() > 5) {
            logger.info("... and {} more events.", list.size() - 5);
        }
    }

    private void navigateAndLogin(Page page, String email, String password) {
        logger.info("Navigating to {} for login as {}", BASE_URL, email);
        page.navigate(BASE_URL);
        acceptCookies(page);
        login(page, email, password);
    }

    private void acceptCookies(Page page) {
        try {
            page.waitForSelector(SELECTOR_COOKIE_ACCEPT, new Page.WaitForSelectorOptions().setTimeout(10000));
            page.click(SELECTOR_COOKIE_ACCEPT);
            page.waitForTimeout(1000);
        } catch (Exception e) {
            logger.error("Cookie dialog not found or already accepted.");
        }
    }

    private void login(Page page, String email, String password) {
        logger.info("Logging in as {}", email);
        page.navigate(LOGIN_URL);
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.fill("#username", email);
        page.fill("#password", password);
        page.click("button[type='submit'].waves-effect.btn.primary");
        page.waitForLoadState(LoadState.NETWORKIDLE);
        logger.info("Login successful.");
    }

    private void processPendingEvents(Page page) {
        logger.info("Start processing pending events");
        page.click(SELECTOR_EVENT_CONFIRM_LINK);
        Set<String> visited = new HashSet<>();

        while (true) {
            List<ElementHandle> events = page.querySelectorAll(SELECTOR_EVENT_BOX);
            if (events.isEmpty()) {
                logger.info("No more events to process.");
                break;
            }

            Optional<ElementHandle> next = events.stream()
                    .filter(e -> !visited.contains(e.getAttribute("href")))
                    .findFirst();

            if (!next.isPresent()) {
                logger.info("All events processed.");
                break;
            }

            String href = next.get().getAttribute("href");
            logger.info("Found next event to process: {}", href);
            visited.add(href);
            processSingleEvent(page, href);
        }
        logger.info("Finished processing pending events");
    }

    private void processSingleEvent(Page page, String href) {
        logger.info("Processing single event at URL: {}", href);
        page.navigate(href);
        page.waitForLoadState();
        page.waitForTimeout(2000);

        String rawName = page.inputValue(SELECTOR_EVENT_NAME);
        String cleanName = cleanEventName(rawName);
        String city = page.inputValue(SELECTOR_CITY_INPUT);
        logger.info("Processing event: {} at {}", cleanName, city);
        logger.info("City from web: {}", city);

        EventoRow match = findMatchingEvento(cleanName, city);
        if (match == null) {
            logger.error("No matching event for {} - {}", cleanName, city);
            page.goBack();
            page.waitForSelector(SELECTOR_EVENT_BOX);
            return;
        }

        boolean filled = fillEventDetails(page, match);
        if (filled) confirmAndGenerate(page);

        // Return to list
        page.navigate(BASE_URL);
        page.waitForSelector(SELECTOR_EVENT_CONFIRM_LINK);
        page.click(SELECTOR_EVENT_CONFIRM_LINK);
        page.waitForSelector(SELECTOR_EVENT_BOX);
    }

    private String cleanEventName(String name) {
        if (name == null) return "";
        int at = name.indexOf('@');
        int dash = name.indexOf('-');
        int cut = -1;
        if (at >= 0 && dash >= 0) cut = Math.min(at, dash);
        else if (at >= 0) cut = at;
        else if (dash >= 0) cut = dash;
        String cleaned = (cut >= 0 ? name.substring(0, cut) : name).trim();
        logger.info("cleanEventName input: '{}', output: '{}'", name, cleaned);
        return cleaned;
    }

    private EventoRow findMatchingEvento(String name, String city) {
        if (eventoRows == null) {
            logger.error("Event list is null");
            return null;
        }
        EventoRow match = eventoRows.stream()
                .filter(e -> matchesExact(e, name, city))
                .findFirst()
                .orElseGet(() -> eventoRows.stream()
                        .filter(e -> matchesPartial(e, name, city))
                        .findFirst()
                        .orElse(null));
        if (match != null) {
            logger.info("Match found for event: '{}' in city: '{}'", name, city);
        } else {
            logger.warn("No match found for event: '{}' in city: '{}'", name, city);
        }
        return match;
    }

    private boolean matchesExact(EventoRow e, String name, String city) {
        String base = Optional.ofNullable(e.getNomeEventoELocation())
                .map(n -> n.split(" - ")[0].trim()).orElse("");
        return base.equalsIgnoreCase(name) && city.equalsIgnoreCase(e.getCitta());
    }

    private boolean matchesPartial(EventoRow e, String name, String city) {
        String excelName = Optional.ofNullable(e.getNomeEventoELocation()).orElse("");
        return (excelName.toLowerCase().contains(name.toLowerCase()) || name.toLowerCase().contains(excelName.toLowerCase()))
                && city.equalsIgnoreCase(e.getCitta());
    }

    private boolean fillEventDetails(Page page, EventoRow event) {
        boolean capOk = fillInput(page, SELECTOR_POSTCODE, event.getCap(), "POSTCODE");
        boolean capienzaOk = fillInputWithFallback(page, SELECTOR_CAPACITY_MAIN, SELECTOR_CAPACITY_ALT, event.getCapienza(), "CAPACITY");
        if (!capOk || !capienzaOk) {
            logger.error("Incomplete event details: POSTCODE ok={}, CAPACITY ok={}", capOk, capienzaOk);
        }
        return capOk && capienzaOk;
    }

    private boolean fillInput(Page page, String selector, String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            logger.warn("{} value is empty; skipping", fieldName);
            return false;
        }
        try {
            page.waitForSelector(selector, new Page.WaitForSelectorOptions().setTimeout(5000));
            page.fill(selector, value.trim());
            logger.info("{} set to {}", fieldName, value);
            return true;
        } catch (Exception e) {
            logger.error("Failed to set {}: {}", fieldName, e.getMessage());
            return false;
        }
    }

    private boolean fillInputWithFallback(Page page, String primary, String fallback, String value, String fieldName) {
        if (fillInput(page, primary, value, fieldName)) return true;
        return fillInput(page, fallback, value, fieldName);
    }

    private void confirmAndGenerate(Page page) {
        try {
            page.click(SELECTOR_CONFIRM_BUTTON);
            page.waitForTimeout(1000);
            page.click(SELECTOR_CHECKBOX_1);
            page.click(SELECTOR_CHECKBOX_7);
            page.waitForTimeout(500);
            page.click(SELECTOR_GENERATE_BUTTON);
            page.waitForTimeout(2000);
            logger.info("License generated.");
        } catch (Exception e) {
            logger.error("Error during confirmation/generation: {}", e.getMessage());
        }
    }


    public boolean downloadLicense(String email, String password, String month, String year) {
        int maxAttempts = 2;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            logger.info("downloadLicense: tentativo {}/{}", attempt, maxAttempts);
            try (Playwright playwright = Playwright.create()) {
                Browser browser = playwright.chromium()
                        .launch(new BrowserType.LaunchOptions().setHeadless(false));
                BrowserContext context = browser.newContext(
                        new Browser.NewContextOptions().setAcceptDownloads(true));
                Page page = context.newPage();

                // Login e navigazione alla pagina licenze
                performLoginAndNavigate(page, email, password);

                // Carica eventi da Excel
                List<EventoRow> toDownload = loadEventsFromExcel();

                // Processa ogni evento
                for (EventoRow event : toDownload) {
                    processEventDownload(page, event, month, year);
                }

                // Cleanup
                context.close();
                browser.close();
                logger.info("downloadLicense: completato con successo");
                return true;

            } catch (Exception e) {
                logger.error("Errore al tentativo {}: {}", attempt, e.getMessage(), e);
                if (attempt == maxAttempts) {
                    logger.error("⛔ downloadLicense: tutti i tentativi falliti");
                    return false;
                }
                // al prossimo tentativo ricreiamo Playwright/browser/context
            }
        }
        return false;
    }

    private void performLoginAndNavigate(Page page, String email, String password) {
        page.navigate("https://licence.soundreef.com/it/");
        page.getByRole(AriaRole.NAVIGATION)
                .getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName("Accedi person"))
                .click();
        logger.info("Cliccato su 'Accedi person'");
        page.locator("#username").fill(email);
        page.locator("#password").fill(password);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Accedi")).click();
        logger.info("Login eseguito per {}", email);

        Locator cookies = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Allow all cookies"));
        if (cookies.count() > 0) {
            cookies.click();
            logger.info("Banner cookies accettato");
        }

        page.getByRole(AriaRole.NAVIGATION)
                .getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName("Licenze description"))
                .click();
        page.navigate("https://licence.soundreef.com/it/licenses");
        logger.info("Navigato alla pagina Licenze");
    }

    private List<EventoRow> loadEventsFromExcel() {
        try {
            ExcelReader reader = new ExcelReader();
            reader.read(ExcelStorage.getInstance().getFile());
            List<EventoRow> events = reader.getEventoRows();
            logger.info("Caricati {} eventi da Excel", events.size());
            return events;
        } catch (Exception e) {
            logger.error("Errore lettura Excel in downloadLicense: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private void processEventDownload(Page page, EventoRow event, String month, String year) {
        String rawName = event.getNomeEventoELocation();
        String name = rawName.contains(" - ") ? rawName.substring(0, rawName.indexOf(" - ")) : rawName;
        if (name.isBlank()) {
            logger.error("Nome evento vuoto, skip: {}", rawName);
            return;
        }

        logger.info("Processo evento: {}", name);
        page.navigate("https://licence.soundreef.com/it/licenses");
        page.waitForTimeout(500);

        Locator matches = page.getByText(name, new Page.GetByTextOptions().setExact(false));
        int count = matches.count();
        if (count == 0) {
            logger.error("Nessun elemento trovato per: {}", name);
            return;
        }

        boolean foundMatch = false;
        for (int i = 0; i < count; i++) {
            page.navigate("https://licence.soundreef.com/it/licenses");
            Locator single = matches.nth(i);
            String entry = single.innerText();
            logger.info("Tentativo {}: {}", i + 1, entry);
            single.click();

            String dateValue = page.locator("#license_start_date")
                    .getAttribute("value");
            if (matchesYearMonth(dateValue, Integer.parseInt(year), Integer.parseInt(month))) {
                foundMatch = true;
                Download download = page.waitForDownload(() ->
                        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Scarica licenza"))
                                .click()
                );
                saveDownloadToDesktop(download, event, entry);
                break;
            }
        }

        if (!foundMatch) {
            logger.error("Nessuna licenza valida trovata per evento: {}", name);
        }
    }

    private void saveDownloadToDesktop(Download download, EventoRow event, String entryText) {
        try {
            Path desktopDir = Paths.get(System.getProperty("user.home"), "Desktop", "LeaDownloads");
            Files.createDirectories(desktopDir);
            String timestamp = extractTime(entryText);
            String filename = toPDFName(event.getNomeEventoELocation(), event.getDataEvento(), timestamp);
            Path target = desktopDir.resolve(filename);
            download.saveAs(target);
            logger.info("File salvato: {}", target.toAbsolutePath());
        } catch (Exception e) {
            logger.error("Errore salvataggio file: {}", e.getMessage(), e);
        }
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
        if (text == null || text.length() < 5) return "Non Trovata";

        String lastFive = text.substring(text.length() - 5); // ultimi 5 caratteri
        return lastFive.replace(":", ""); // rimuove i due punti
    }
}
