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

public class LeaAutomationService {
	private boolean TEST_MODE = true;
    // COSTANTS
    private static final String HOMEPAGE_URL = "https://licence.soundreef.com/it/";
    private static final String LOGIN_URL = "https://licence.soundreef.com/it/login";
    private static final String LICENSES_URL = "https://licence.soundreef.com/it/licenses";
    private static final String COOKIE_SELECTOR = "#CybotCookiebotDialogBodyButtonAccept";
    private static final String USERNAME_SELECTOR = "#username";
    private static final String PASSWORD_SELECTOR = "#password";
    private static final String LOGIN_BUTTON_SELECTOR = "button[type='submit'].waves-effect.btn.primary.action";
    private static final String EVENTI_DA_CONFERMARE_SELECTOR = "a:has-text('Eventi da confermare')";
    private static final String EVENT_BOX_SELECTOR = "a.box.rounded.license";
    private static final String EVENT_NAME_SELECTOR = "input#license_form_lm_event_name";
    private static final String EVENT_CITY_SELECTOR = "input#venue_city";
    private static final String POSTCODE_SELECTOR = "input#venue_postcode";
    private static final String CAPACITY_SELECTOR = "input#license_form_lm_venue_capacity";
    private static final String ALT_CAPACITY_SELECTOR = "input[name='lm_venue_capacity']";
    private static final String APPROVAL_BUTTON_SELECTOR = "a#cmd-event-approval.btn.primary";
    private static final String CONSENT1_SELECTOR = "label[for='consent_1']";
    private static final String CONSENT7_SELECTOR = "label[for='consent_7']";
    private static final String LICENSE_DATE_SELECTOR = "#license_start_date";
    private static final String DOWNLOAD_LINK_TEXT = "Scarica licenza";
    private static final String NAV_LINK_LOGIN = "Accedi person";
    private static final String NAV_LINK_LICENSES = "Licenze description";
    private static final String DOWNLOAD_FOLDER = "LeaDownloads";
    private static final String DEFAULT_TIME_NOT_FOUND = "Non Trovata";
	
    private List<EventoRow> listaEventiDaExcel;

    public void setListaEventiDaExcel(List<EventoRow> listaEventi) {
        this.listaEventiDaExcel = listaEventi;
    }

    public boolean confermaLicenze(String email, String password) {
        if (!caricaEventiDaExcelConLogging()) return false;
        return avviaAutomazioneWeb(email, password);
    }

    private boolean caricaEventiDaExcelConLogging() {
        System.out.println("Caricamento dati da Excel...");
        List<EventoRow> lista = caricaEventiDaExcel();
        
        if (lista == null || lista.isEmpty()) {
            System.err.println("❌ Nessun evento trovato o errore nel file Excel!");
            return false;
        }

        this.listaEventiDaExcel = lista;
        System.out.println("✅ Caricati " + lista.size() + " eventi da Excel");
        stampaEventiDimostrativi();
        return true;
    }


    private void stampaEventiDimostrativi() {
        System.out.println("📋 Primi eventi caricati:");
        for (int i = 0; i < Math.min(5, listaEventiDaExcel.size()); i++) {
            EventoRow evento = listaEventiDaExcel.get(i);
            System.out.println("  " + (i+1) + ". " + evento.getNomeEventoELocation() + 
                               " - " + evento.getCitta() + 
                               " (CAP: " + evento.getCap() + 
                               ", Capienza: " + evento.getCapienza() + ")");
        }
        if (listaEventiDaExcel.size() > 5) {
            System.out.println("  ... e altri " + (listaEventiDaExcel.size() - 5) + " eventi");
        }
    }

    private boolean avviaAutomazioneWeb(String email, String password) {
        try (Playwright playwright = Playwright.create()) {
            System.out.println("Avvio automazione web...");

            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            Page page = browser.newContext().newPage();

            page.navigate(HOMEPAGE_URL);
            accettaCookie(page);

            System.out.println("Credenziali: " + email);
            effettuaLogin(page, email, password);

            gestisciEventiDaConfermare(page);

            page.waitForTimeout(5000);
            page.close();
            browser.close();

            return true;
        } catch (Exception e) {
            System.err.println("❌ Errore durante l'automazione web: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void accettaCookie(Page page) {
        try {
            page.waitForSelector(COOKIE_SELECTOR, new Page.WaitForSelectorOptions().setTimeout(10000));
            page.click(COOKIE_SELECTOR);
            page.waitForTimeout(1000);
        } catch (Exception e) {
            System.out.println("Errore nell'accettazione dei cookie: " + e.getMessage());
        }
    }

    private void effettuaLogin(Page page, String email, String password) {
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

            System.out.println("Credenziali inserite con successo");
        } catch (Exception e) {
            throw new RuntimeException("Login fallito", e);
        }
    }

    private void gestisciEventiDaConfermare(Page page) {
        try {
            page.waitForSelector(EVENTI_DA_CONFERMARE_SELECTOR);
            page.click(EVENTI_DA_CONFERMARE_SELECTOR);
            page.waitForSelector(EVENT_BOX_SELECTOR);

            Set<String> eventiVisitati = new HashSet<>();

            while (true) {
                List<ElementHandle> eventi = page.querySelectorAll(EVENT_BOX_SELECTOR);
                if (eventi.isEmpty()) {
                    System.out.println("🎉 Nessun evento disponibile.");
                    break;
                }

                ElementHandle eventoDaProcessare = null;
                String href = null;
                for (ElementHandle evento : eventi) {
                    href = evento.getAttribute("href");
                    if (href != null && !eventiVisitati.contains(href)) {
                        eventoDaProcessare = evento;
                        break;
                    }
                }

                if (eventoDaProcessare == null) {
                    System.out.println("🎉 Tutti gli eventi sono stati visitati!");
                    break;
                }

                eventiVisitati.add(href);
                page.navigate(href);
                page.waitForLoadState();
                page.waitForTimeout(2000);

                processaEventoSingolo(page);
            }

            System.out.println("✅ Tutti gli eventi sono stati gestiti!");
        } catch (Exception e) {
            System.err.println("Errore in gestisciEventiDaConfermare: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processaEventoSingolo(Page page) {
        try {
            String nomeEventoWeb = leggiNomeEventoPulito(page);
            String cittaWeb = leggiCittaEvento(page);
            EventoRow eventoCorrispondente = trovaEventoCorrispondente(nomeEventoWeb, cittaWeb);

            boolean capInserito = false;
            boolean capienzaInserita = false;
            boolean eventoProcessato = false;

            if (eventoCorrispondente != null) {
                capInserito = inserisciCapEvento(page, eventoCorrispondente);
                capienzaInserita = inserisciCapienzaEvento(page, eventoCorrispondente);

                if (capInserito && capienzaInserita) {
                    eventoProcessato = confermaEventoEGeneraLicenza(page);
                } else {
                    System.out.println("⚠️ Dati incompleti. CAP inserito: " + capInserito + ", Capienza: " + capienzaInserita);
                }
            } else {
                System.out.println("Evento non trovato in Excel: " + nomeEventoWeb + " - " + cittaWeb);
            }

            if (eventoProcessato) {
                ritornaAllaHomepagePerProssimoEvento(page);
            } else {
                page.goBack();
                page.waitForSelector(EVENT_BOX_SELECTOR);
            }

        } catch (Exception e) {
            System.err.println("Errore nel processo evento: " + e.getMessage());
        }
    }

    private String leggiNomeEventoPulito(Page page) {
        page.waitForSelector(EVENT_NAME_SELECTOR);
        String nome = page.inputValue(EVENT_NAME_SELECTOR);
        int cutIndex = Math.min(
            nome.indexOf('@') != -1 ? nome.indexOf('@') : Integer.MAX_VALUE,
            nome.indexOf('-') != -1 ? nome.indexOf('-') : Integer.MAX_VALUE
        );
        return (cutIndex == Integer.MAX_VALUE ? nome : nome.substring(0, cutIndex)).trim();
    }

    private String leggiCittaEvento(Page page) {
        page.waitForSelector(EVENT_CITY_SELECTOR);
        return page.inputValue(EVENT_CITY_SELECTOR);
    }

    private boolean inserisciCapEvento(Page page, EventoRow evento) {
        try {
            String cap = evento.getCap();
            if (cap != null && !cap.trim().isEmpty()) {
                page.waitForSelector(POSTCODE_SELECTOR, new Page.WaitForSelectorOptions().setTimeout(5000));
                page.fill(POSTCODE_SELECTOR, cap.trim());
                System.out.println("CAP inserito: " + cap);
                return true;
            }
        } catch (Exception e) {
            System.out.println("Errore nell'inserimento del CAP: " + e.getMessage());
        }
        return false;
    }

    private boolean inserisciCapienzaEvento(Page page, EventoRow evento) {
        String capienza = evento.getCapienza();
        if (capienza != null && !capienza.trim().isEmpty()) {
            try {
                page.waitForSelector(CAPACITY_SELECTOR, new Page.WaitForSelectorOptions().setTimeout(5000));
                page.fill(CAPACITY_SELECTOR, capienza.trim());
                System.out.println("Capienza inserita: " + capienza);
                return true;
            } catch (Exception e) {
                try {
                    page.waitForSelector(ALT_CAPACITY_SELECTOR, new Page.WaitForSelectorOptions().setTimeout(2000));
                    page.fill(ALT_CAPACITY_SELECTOR, capienza.trim());
                    System.out.println("Capienza inserita (campo alternativo): " + capienza);
                    return true;
                } catch (Exception ignored) {
                    System.out.println("Nessun campo capienza trovato.");
                }
            }
        }
        return false;
    }

    private boolean confermaEventoEGeneraLicenza(Page page) {
        try {
            page.waitForSelector(APPROVAL_BUTTON_SELECTOR, new Page.WaitForSelectorOptions().setTimeout(5000));
            page.click(APPROVAL_BUTTON_SELECTOR);

            selezionaCheckboxConsenso(page);

            page.waitForSelector("button[type='submit'].waves-effect.btn.primary", new Page.WaitForSelectorOptions().setTimeout(5000));
            page.click("button[type='submit'].waves-effect.btn.primary");
            page.waitForTimeout(3000);
            System.out.println("✅ Licenza generata!");
            return true;
        } catch (Exception e) {
            System.out.println("Errore durante la generazione licenza: " + e.getMessage());
            return false;
        }
    }

    private void selezionaCheckboxConsenso(Page page) {
        page.waitForSelector(CONSENT1_SELECTOR, new Page.WaitForSelectorOptions().setTimeout(5000));
        page.click(CONSENT1_SELECTOR);
        System.out.println("✅ Checkbox 1 selezionata.");

        page.waitForSelector(CONSENT7_SELECTOR, new Page.WaitForSelectorOptions().setTimeout(5000));
        page.click(CONSENT7_SELECTOR);
        System.out.println("✅ Checkbox 2 selezionata.");
    }

    private void ritornaAllaHomepagePerProssimoEvento(Page page) {
        page.navigate(HOMEPAGE_URL);
        page.waitForTimeout(2000);
        page.waitForSelector(EVENTI_DA_CONFERMARE_SELECTOR);
        page.click(EVENTI_DA_CONFERMARE_SELECTOR);
        page.waitForSelector(EVENT_BOX_SELECTOR);
    }

    private EventoRow trovaEventoCorrispondente(String nomeEventoWeb, String cittaWeb) {
        if (listaEventiDaExcel == null || listaEventiDaExcel.isEmpty()) return null;

        for (EventoRow evento : listaEventiDaExcel) {
            String nomeExcel = evento.getNomeEventoELocation();
            if (nomeExcel != null) {
                int sep = nomeExcel.indexOf(" - ");
                if (sep != -1) nomeExcel = nomeExcel.substring(0, sep);

                if (nomeEventoWeb.trim().equalsIgnoreCase(nomeExcel.trim()) &&
                    cittaWeb.trim().equalsIgnoreCase(evento.getCitta().trim())) {
                    return evento;
                }
            }
        }

        for (EventoRow evento : listaEventiDaExcel) {
            String nomeExcel = evento.getNomeEventoELocation();
            if (nomeExcel != null && cittaWeb != null &&
                (nomeExcel.toLowerCase().contains(nomeEventoWeb.toLowerCase()) ||
                 nomeEventoWeb.toLowerCase().contains(nomeExcel.toLowerCase())) &&
                cittaWeb.trim().equalsIgnoreCase(evento.getCitta().trim())) {
                System.out.println("Trovata corrispondenza parziale per nome evento");
                return evento;
            }
        }

        return null;
    }


    public boolean downloadLicenze(String email, String password, String month, String year) {
        int maxAttempts = 2;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try (Playwright playwright = Playwright.create()) {
                Browser browser = avviaBrowser();
                BrowserContext context = creaContestoDownload(browser);
                Page page = context.newPage();

                eseguiLoginSoundreef(page, email, password);
                navigaAllaSezioneLicenze(page);

                List<EventoRow> eventiDaScaricare = caricaEventiDaExcel();
                processaEventiPerDownload(eventiDaScaricare, page, year, month);

                context.close();
                browser.close();
                return true;

            } catch (Exception e) {
                System.err.println("❌ Errore al tentativo " + attempt);
                e.printStackTrace();
                if (attempt == maxAttempts) {
                    System.err.println("⛔ Tutti i tentativi falliti.");
                    return false;
                }
            }
        }

        return false;
    }
    private Browser avviaBrowser() {
        return Playwright.create().chromium().launch(
            new BrowserType.LaunchOptions().setHeadless(false)
        );
    }

    private BrowserContext creaContestoDownload(Browser browser) {
        return browser.newContext(
            new Browser.NewContextOptions().setAcceptDownloads(true)
        );
    }

    private void eseguiLoginSoundreef(Page page, String email, String password) {
        page.navigate(HOMEPAGE_URL);

        page.getByRole(AriaRole.NAVIGATION)
            .getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName(NAV_LINK_LOGIN))
            .click();

        page.locator(USERNAME_SELECTOR).fill(email);
        page.locator(PASSWORD_SELECTOR).fill(password);

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Accedi")).click();

        // Accetta i cookie se presente
        Locator allowCookies = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Allow all cookies"));
        if (allowCookies.count() > 0) {
            allowCookies.click();
        }
    }

    private void navigaAllaSezioneLicenze(Page page) {
        page.getByRole(AriaRole.NAVIGATION)
            .getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName(NAV_LINK_LICENSES))
            .click();

        page.navigate(LICENSES_URL);
    }

    private List<EventoRow> caricaEventiDaExcel() {
        try {
            ExcelReader reader = new ExcelReader();
            reader.read(ExcelStorage.getInstance().getFile());
            return reader.getEventoRows();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    private void processaEventiPerDownload(List<EventoRow> eventi, Page page, String year, String month) throws Exception {
        for (EventoRow event : eventi) {
            System.out.println("Analizzo il PDF: " + event);
            page.navigate(LICENSES_URL);
            page.waitForTimeout(1000);

            String eventName = estraiNomePulito(event.getNomeEventoELocation());
            if (eventName.isEmpty()) break;

            Locator matches = page.getByText(eventName, new Page.GetByTextOptions().setExact(false));
            int count = matches.count();

            if (count == 0) {
                System.out.println("Nessun elemento trovato per: " + eventName);
                continue;
            }

            System.out.println("Trovati " + count + " elementi per: " + eventName);
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
                    scaricaLicenza(event, page, time);
                } else if (reachedRightTime) {
                    break;
                }
            }
        }
    }
    
    private void scaricaLicenza(EventoRow event, Page page, String time) throws Exception {
        Download download = page.waitForDownload(() -> {
            page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(DOWNLOAD_LINK_TEXT)).click();
        });

        Path downloadDir = Paths.get(System.getProperty("user.home"), "Desktop", DOWNLOAD_FOLDER);
        Files.createDirectories(downloadDir);

        String nomeFile = toPDFName(event.getNomeEventoELocation(), event.getDataEvento(), time);
        Path pathFinale = downloadDir.resolve(nomeFile);

        System.out.println("Salvo file con Evento: " + event.getNomeEventoELocation() + " | Data: " + event.getDataEvento() + " | Ora: " + time);
        download.saveAs(pathFinale);
        System.out.println("File salvato in: " + pathFinale.toAbsolutePath());
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

    private String estraiNomePulito(String nomeEvento) {
        int index = nomeEvento.indexOf(" - ");
        return (index != -1) ? nomeEvento.substring(0, index) : nomeEvento;
    }



}
