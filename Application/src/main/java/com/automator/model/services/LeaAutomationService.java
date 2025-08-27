package com.automator.model.services;

import java.nio.file.Path;

import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.nio.file.Files;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.automator.model.services.EventoRow.toPDFName;

public class LeaAutomationService {
    private static final Logger logger = LoggerFactory.getLogger(LeaAutomationService.class);
    private List<EventoRow> eventListFromExcel;
    
    private boolean TEST_MODE = true;

    // COSTANTS
    private static final String HOMEPAGE_URL = "https://licence.soundreef.com/it/";
    private static final String LOGIN_URL = "https://licence.soundreef.com/it/login";
    private static final String LICENSES_URL = "https://licence.soundreef.com/it/licenses";
    private static final String COOKIE_SELECTOR = "#CybotCookiebotDialogBodyButtonAccept";
    private static final String USERNAME_SELECTOR = "#username";
    private static final String PASSWORD_SELECTOR = "#password";
    private static final String LOGIN_BUTTON_SELECTOR = "button[type='submit'].waves-effect.btn.primary.action";
    private static final String EVENTI_DA_CONFERMARE_SELECTOR = "a:has-text('Eventi da confirmre')";
    private static final String EVENT_BOX_SELECTOR = "a.box.rounded.license";
    private static final String EVENT_NAME_SELECTOR = "input#license_form_lm_event_name";
    private static final String EVENT_CITY_SELECTOR = "input#venue_city";
    private static final String POSTCODE_SELECTOR = "input#venue_postcode";
    private static final String CAPACITY_SELECTOR = "input#license_form_lm_venue_postalCodeacity";
    private static final String ALT_CAPACITY_SELECTOR = "input[name='lm_venue_postalCodeacity']";
    private static final String APPROVAL_BUTTON_SELECTOR = "a#cmd-event-approval.btn.primary";
    private static final String CONSENT1_SELECTOR = "label[for='consent_1']";
    private static final String CONSENT7_SELECTOR = "label[for='consent_7']";
    private static final String LICENSE_DATE_SELECTOR = "#license_start_date";
    private static final String DOWNLOAD_LINK_TEXT = "Scarica licenza";
    private static final String NAV_LINK_LOGIN = "Accedi person";
    private static final String NAV_LINK_LICENSES = "Licenze description";
    private static final String DOWNLOAD_FOLDER = "LeaDownloads";
    private static final String DEFAULT_TIME_NOT_FOUND = "Non Trovata";
    private static final String LOGIN_BUTTON_TEXT = "Accedi";

    public void loadEventsFromExcel(List<EventoRow> listaEventi) {
        this.eventListFromExcel = listaEventi;
    }

    private boolean loadEventsFromExcelWithLogging() {
        logger.info("Caricamento dati da Excel...");
        List<EventoRow> lista = loadEventsFromExcel();

        if (lista == null || lista.isEmpty()) {
            logger.error("Nessun event trovato o error nel file Excel!");
            return false;
        }

        this.eventListFromExcel = lista;
        logger.info("Caricati " + lista.size() + " events da Excel");
        stampaEventiDimostrativi();
        return true;
    }

    public boolean confirmLicenses(String email, String password) {
        if (!loadEventsFromExcelWithLogging()) return false;
        return startWebAutomation(email, password);
    }

    private void stampaEventiDimostrativi() {
        logger.info("Primi events caricati:");
        for (int i = 0; i < Math.min(5, eventListFromExcel.size()); i++) {
            EventoRow event = eventListFromExcel.get(i);
            System.out.println("  " + (i+1) + ". " + event.getNomeEventoELocation() +
                    " - " + event.getCitta() +
                    " (CAP: " + event.getCap() +
                    ", Capienza: " + event.getCapienza() + ")");
        }
        if (eventListFromExcel.size() > 5) {
            logger.info("  ... e altri " + (eventListFromExcel.size() - 5) + " events");
        }
    }

    private boolean startWebAutomation(String email, String password) {
        try (Playwright playwright = Playwright.create()) {
            logger.info("Avvio automazione web...");

            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            Page page = browser.newContext().newPage();

            page.navigate(HOMEPAGE_URL);
            acceptCookie(page);

            logger.info("Credenziali: " + email);
            loginToLea(page, email, password);

            manageEventsToConfirm(page);

            page.waitForTimeout(5000);
            page.close();
            browser.close();

            return true;
        } catch (Exception e) {
            logger.error("Errore durante l'automazione web: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void acceptCookie(Page page) {
        try {
            page.waitForSelector(COOKIE_SELECTOR, new Page.WaitForSelectorOptions().setTimeout(10000));
            page.click(COOKIE_SELECTOR);
            page.waitForTimeout(1000);
        } catch (Exception e) {
            logger.info("Errore nell'accettazione dei cookie: " + e.getMessage());
        }
    }

    private void loginToLea(Page page, String email, String password) {
        try {
            page.waitForSelector("a[href='"+LOGIN_URL+"']", new Page.WaitForSelectorOptions().setTimeout(10000));
            page.click("a[href='" + LOGIN_URL + "']");
            page.waitForLoadState(LoadState.NETWORKIDLE);

            page.waitForSelector(USERNAME_SELECTOR, new Page.WaitForSelectorOptions().setTimeout(5000));
            page.waitForSelector(PASSWORD_SELECTOR, new Page.WaitForSelectorOptions().setTimeout(5000));
            page.locator(USERNAME_SELECTOR).fill(email);
            page.locator(PASSWORD_SELECTOR).fill(password);
            page.waitForTimeout(1000);

            page.waitForSelector("button[type='submit'].waves-effect.btn.action", new Page.WaitForSelectorOptions().setTimeout(10000));
            page.click(LOGIN_BUTTON_SELECTOR);
            page.waitForLoadState(LoadState.NETWORKIDLE);
            page.waitForTimeout(1000);

            logger.info("Credenziali inserite con success");
        } catch (Exception e) {
            throw new RuntimeException("Login failed", e);
        }
    }

    private void manageEventsToConfirm(Page page) {
        try {
            page.waitForSelector(EVENTI_DA_CONFERMARE_SELECTOR);
            page.click(EVENTI_DA_CONFERMARE_SELECTOR);
            page.waitForSelector(EVENT_BOX_SELECTOR);

            Set<String> eventsVisitati = new HashSet<>();

            while (true) {
                List<ElementHandle> events = page.querySelectorAll(EVENT_BOX_SELECTOR);
                if (events.isEmpty()) {
                    logger.info("Nessun event disponibile.");
                    break;
                }

                ElementHandle eventDaProcessare = null;
                String href = null;
                for (ElementHandle event : events) {
                    href = event.getAttribute("href");
                    if (href != null && !eventsVisitati.contains(href)) {
                        eventDaProcessare = event;
                        break;
                    }
                }

                if (eventDaProcessare == null) {
                    logger.info("🎉 Tutti gli events sono stati visitati!");
                    break;
                }

                eventsVisitati.add(href);
                page.navigate(href);
                page.waitForLoadState();
                page.waitForTimeout(2000);

                processSingleEvent(page);
            }

            logger.info("Tutti gli events sono stati gestiti!");
        } catch (Exception e) {
            logger.error("Errore in gestisciEventiDaConfermare: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processSingleEvent(Page page) {
        try {
            String eventNameWeb = cleanEventName(page);
            String cityWeb = leggiCittaEvento(page);
            EventoRow eventCorrispondente = findCorrespondingEvent(eventNameWeb, cityWeb);

            boolean postalCodeInserito = false;
            boolean postalCodeienzaInserita = false;
            boolean eventProcessato = false;

            if (eventCorrispondente != null) {
                postalCodeInserito = inserisciCapEvento(page, eventCorrispondente);
                postalCodeienzaInserita = insertEventCapacity(page, eventCorrispondente);

                if (postalCodeInserito && postalCodeienzaInserita) {
                    eventProcessato = confirmEventoEGeneraLicenza(page);
                } else {
                    logger.info("Dati incompleti. CAP inserito: " + postalCodeInserito + ", Capienza: " + postalCodeienzaInserita);
                }
            } else {
                logger.info("Evento non trovato in Excel: " + eventNameWeb + " - " + cityWeb);
            }

            if (eventProcessato) {
                goBackToHomepageToProcessNextEvent(page);
            } else {
                page.goBack();
                page.waitForSelector(EVENT_BOX_SELECTOR);
            }

        } catch (Exception e) {
            logger.error("Errore nel processo event: " + e.getMessage());
        }
    }

    private String cleanEventName(Page page) {
        page.waitForSelector(EVENT_NAME_SELECTOR);
        String firstName = page.inputValue(EVENT_NAME_SELECTOR);
        int cutIndex = Math.min(
                firstName.indexOf('@') != -1 ? firstName.indexOf('@') : Integer.MAX_VALUE,
                firstName.indexOf('-') != -1 ? firstName.indexOf('-') : Integer.MAX_VALUE
        );
        return (cutIndex == Integer.MAX_VALUE ? firstName : firstName.substring(0, cutIndex)).trim();
    }

    private String leggiCittaEvento(Page page) {
        page.waitForSelector(EVENT_CITY_SELECTOR);
        return page.inputValue(EVENT_CITY_SELECTOR);
    }

    private boolean inserisciCapEvento(Page page, EventoRow event) {
        try {
            String postalCode = event.getCap();
            if (postalCode != null && !postalCode.trim().isEmpty()) {
                page.waitForSelector(POSTCODE_SELECTOR, new Page.WaitForSelectorOptions().setTimeout(5000));
                page.fill(POSTCODE_SELECTOR, postalCode.trim());
                logger.info("CAP inserito: " + postalCode);
                return true;
            }
        } catch (Exception e) {
            logger.info("Errore nell'inserimento del CAP: " + e.getMessage());
        }
        return false;
    }

    private boolean insertEventCapacity(Page page, EventoRow event) {
        String postalCodeienza = event.getCapienza();
        if (postalCodeienza != null && !postalCodeienza.trim().isEmpty()) {
            try {
                page.waitForSelector(CAPACITY_SELECTOR, new Page.WaitForSelectorOptions().setTimeout(5000));
                page.fill(CAPACITY_SELECTOR, postalCodeienza.trim());
                logger.info("Capienza inserita: " + postalCodeienza);
                return true;
            } catch (Exception e) {
                try {
                    page.waitForSelector(ALT_CAPACITY_SELECTOR, new Page.WaitForSelectorOptions().setTimeout(2000));
                    page.fill(ALT_CAPACITY_SELECTOR, postalCodeienza.trim());
                    logger.info("Capienza inserita (campo alternativo): " + postalCodeienza);
                    return true;
                } catch (Exception ignored) {
                    logger.info("Nessun campo postalCodeienza trovato.");
                }
            }
        }
        return false;
    }

    private boolean confirmEventoEGeneraLicenza(Page page) {
        try {
            page.waitForSelector(APPROVAL_BUTTON_SELECTOR, new Page.WaitForSelectorOptions().setTimeout(5000));
            page.click(APPROVAL_BUTTON_SELECTOR);

            selezionaCheckboxConsenso(page);

            page.waitForSelector("button[type='submit'].waves-effect.btn.primary", new Page.WaitForSelectorOptions().setTimeout(5000));
            page.click("button[type='submit'].waves-effect.btn.primary");
            page.waitForTimeout(3000);
            logger.info("Licenza generata!");
            return true;
        } catch (Exception e) {
            logger.info("Errore durante la generazione license: " + e.getMessage());
            return false;
        }
    }

    private void selezionaCheckboxConsenso(Page page) {
        page.waitForSelector(CONSENT1_SELECTOR, new Page.WaitForSelectorOptions().setTimeout(5000));
        page.click(CONSENT1_SELECTOR);
        logger.info("Checkbox 1 selezionata.");

        page.waitForSelector(CONSENT7_SELECTOR, new Page.WaitForSelectorOptions().setTimeout(5000));
        page.click(CONSENT7_SELECTOR);
        logger.info("Checkbox 2 selezionata.");
    }

    private void goBackToHomepageToProcessNextEvent(Page page) {
        page.navigate(HOMEPAGE_URL);
        page.waitForTimeout(2000);
        page.waitForSelector(EVENTI_DA_CONFERMARE_SELECTOR);
        page.click(EVENTI_DA_CONFERMARE_SELECTOR);
        page.waitForSelector(EVENT_BOX_SELECTOR);
    }

    private EventoRow findCorrespondingEvent(String eventNameWeb, String cityWeb) {
        if (eventListFromExcel == null || eventListFromExcel.isEmpty()) return null;

        for (EventoRow event : eventListFromExcel) {
            String firstNameExcel = event.getNomeEventoELocation();
            if (firstNameExcel != null) {
                int sep = firstNameExcel.indexOf(" - ");
                if (sep != -1) firstNameExcel = firstNameExcel.substring(0, sep);

                if (eventNameWeb.trim().equalsIgnoreCase(firstNameExcel.trim()) &&
                        cityWeb.trim().equalsIgnoreCase(event.getCitta().trim())) {
                    return event;
                }
            }
        }

        for (EventoRow event : eventListFromExcel) {
            String firstNameExcel = event.getNomeEventoELocation();
            if (firstNameExcel != null && cityWeb != null &&
                    (firstNameExcel.toLowerCase().contains(eventNameWeb.toLowerCase()) ||
                            eventNameWeb.toLowerCase().contains(firstNameExcel.toLowerCase())) &&
                    cityWeb.trim().equalsIgnoreCase(event.getCitta().trim())) {
                logger.info("Trovata corrispondenza parziale per firstName event");
                return event;
            }
        }

        return null;
    }


    public boolean downloadLicenses(String email, String password, String month, String year) {
        int maxAttempts = 2;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try (Playwright playwright = Playwright.create()) {
                Browser browser = startBrowser();
                BrowserContext context = createContextDownload(browser);
                Page page = context.newPage();

                performLoginSoundreef(page, email, password);
                navigateToLicensesSection(page);

                List<EventoRow> eventsDaScaricare = loadEventsFromExcel();
                processEventsToDownload(eventsDaScaricare, page, year, month);

                context.close();
                browser.close();
                return true;

            } catch (Exception e) {
                logger.error("Errore al tentativo " + attempt);
                e.printStackTrace();
                if (attempt == maxAttempts) {
                    logger.error("Tutti i tentativi falliti.");
                    return false;
                }
            }
        }

        return false;
    }
    private Browser startBrowser() {
        return Playwright.create().chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(false)
        );
    }

    private BrowserContext createContextDownload(Browser browser) {
        return browser.newContext(
                new Browser.NewContextOptions().setAcceptDownloads(true)
        );
    }

    private void performLoginSoundreef(Page page, String email, String password) {
        page.navigate(HOMEPAGE_URL);

        page.getByRole(AriaRole.NAVIGATION)
                .getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName(NAV_LINK_LOGIN))
                .click();

        page.locator(USERNAME_SELECTOR).fill(email);
        page.locator(PASSWORD_SELECTOR).fill(password);

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(LOGIN_BUTTON_TEXT)).click();

        // Accetta i cookie se presente
        Locator allowCookies = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Allow all cookies"));
        if (allowCookies.count() > 0) {
            allowCookies.click();
        }
    }

    private void navigateToLicensesSection(Page page) {
        page.getByRole(AriaRole.NAVIGATION)
                .getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName(NAV_LINK_LICENSES))
                .click();

        page.navigate(LICENSES_URL);
    }

    private List<EventoRow> loadEventsFromExcel() {
        try {
            ExcelReader reader = new ExcelReader();
            reader.read(ExcelStorage.getInstance().getFile());
            return reader.getEventoRows();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void processEventsToDownload(List<EventoRow> events, Page page, String year, String month) throws Exception {
        for (EventoRow event : events) {
            logger.info("Analizzo il PDF: " + event);
            page.navigate(LICENSES_URL);
            page.waitForTimeout(1000);

            String eventName = extractCleanName(event.getNomeEventoELocation());
            if (eventName.isEmpty()) break;

            Locator matches = page.getByText(eventName, new Page.GetByTextOptions().setExact(false));
            int count = matches.count();

            if (count == 0) {
                logger.info("Nessun elemento trovato per: " + eventName);
                continue;
            }

            logger.info("Trovati " + count + " elementi per: " + eventName);
            boolean reachedRightTime = false;

            for (int i = 0; i < count; i++) {
                page.navigate(LICENSES_URL);

                Locator match = matches.nth(i);
                String time = extractTime(match.innerText());
                match.click();

                Locator dateLocator = page.locator(LICENSE_DATE_SELECTOR);
                dateLocator.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED));
                String dateValue = dateLocator.getAttribute("value");

                if (matchesYearMonth(dateValue, Integer.parseInt(year), Integer.parseInt(month))) {
                    reachedRightTime = true;
                    downloadLicenses(event, page, time);
                } else if (reachedRightTime) {
                    break;
                }
            }
        }
    }

    private void downloadLicenses(EventoRow event, Page page, String time) throws Exception {
        Download download = page.waitForDownload(() -> {
            page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(DOWNLOAD_LINK_TEXT)).click();
        });

        Path downloadDir = Paths.get(System.getProperty("user.home"), "Desktop", DOWNLOAD_FOLDER);
        Files.createDirectories(downloadDir);

        String firstNameFile = toPDFName(event.getNomeEventoELocation(), event.getDataEvento(), time);
        Path pathFinale = downloadDir.resolve(firstNameFile);

        logger.info("Salvo file con Evento: " + event.getNomeEventoELocation() + " | Data: " + event.getDataEvento() + " | Ora: " + time);
        download.saveAs(pathFinale);
        logger.info("File salvato in: " + pathFinale.toAbsolutePath());
    }

    public boolean matchesYearMonth(String value, int expectedYear, int expectedMonth) {
        if (value == null || value.isBlank()) return false;
        try {
            LocalDate date = LocalDate.parse(value);
            return date.getYear() == expectedYear && date.getMonthValue() == expectedMonth;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public String extractTime(String text) {
        if (text == null || text.length() < 5) return DEFAULT_TIME_NOT_FOUND;
        String lastFive = text.substring(text.length() - 5);
        return lastFive.replace(":", "");
    }

    private String extractCleanName(String eventName) {
        int index = eventName.indexOf(" - ");
        return (index != -1) ? eventName.substring(0, index) : eventName;
    }
}

