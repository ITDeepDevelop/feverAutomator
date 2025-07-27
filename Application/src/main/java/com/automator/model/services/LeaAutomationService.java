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

    // AGGIUNTA: Variabile di istanza per la lista degli eventi da Excel
    private List<EventoRow> listaEventiDaExcel;

    // Metodo setter per impostare la lista degli eventi caricati dall'UI
    public void setListaEventiDaExcel(List<EventoRow> listaEventi) {
        this.listaEventiDaExcel = listaEventi;
    }


    public boolean op1(String email, String password) {
        try {
            // Caricamento dati Excel
            logger.info("Caricamento dati da Excel...");
            ExcelReader reader = new ExcelReader();
            if (ExcelStorage.getInstance().getFile() == null) {
                logger.error("Nessun file Excel caricato! Assicurati di aver selezionato un file Excel prima di avviare l'operazione.");
                return false;
            }
            reader.read(ExcelStorage.getInstance().getFile());
            this.listaEventiDaExcel = reader.getEventoRows();

            if (this.listaEventiDaExcel == null || this.listaEventiDaExcel.isEmpty()) {
                logger.error("Nessun evento trovato nel file Excel!");
                return false;
            }

            logger.info("Caricati {} eventi da Excel", this.listaEventiDaExcel.size());
            logger.info("Primi eventi caricati:");
            for (int i = 0; i < Math.min(5, this.listaEventiDaExcel.size()); i++) {
                EventoRow evento = this.listaEventiDaExcel.get(i);
                logger.info("  {}. {} - {} (CAP: {}, Capienza: {})", i+1,
                        evento.getNomeEventoELocation(),
                        evento.getCitta(),
                        evento.getCap(),
                        evento.getCapienza());
            }
            if (this.listaEventiDaExcel.size() > 5) {
                logger.info("  ... e altri {} eventi", this.listaEventiDaExcel.size() - 5);
            }
        } catch (Exception e) {
            logger.error("Errore nel caricamento dei dati Excel: {}", e.getMessage(), e);
            return false;
        }

        // Avvio automazione web
        try (Playwright playwright = Playwright.create()) {
            logger.info("Avvio automazione web...");
            Browser browser = playwright.chromium()
                    .launch(new BrowserType.LaunchOptions().setHeadless(false));
            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            page.navigate("https://licence.soundreef.com/it/");
            allowCookies(page);
            logger.info("Credenziali utilizzate: {}", email);
            loginProcedure(page, email, password);
            runTo(page);

            page.waitForTimeout(5000);
            page.close();
            browser.close();
            return true;
        } catch (Exception e) {
            logger.error("Errore durante l'automazione web: {}", e.getMessage(), e);
            return false;
        }
    }

    public void allowCookies(Page page) {
        try {
            page.waitForSelector("#CybotCookiebotDialogBodyButtonAccept", new Page.WaitForSelectorOptions().setTimeout(10000));
            page.click("#CybotCookiebotDialogBodyButtonAccept");
            page.waitForTimeout(1000);
        } catch (Exception e) {
            logger.error("Errore nell'accettazione dei cookie: {}", e.getMessage());
        }
    }

    public void loginProcedure(Page page, String email, String password) {
        logger.info("Tentativo di login per email: {}", email);
        try {
            page.waitForSelector("a[href='https://licence.soundreef.com/it/login']", new Page.WaitForSelectorOptions().setTimeout(10000));
            page.click("a[href='https://licence.soundreef.com/it/login']");
            page.waitForLoadState(LoadState.NETWORKIDLE);

            page.waitForSelector("#username", new Page.WaitForSelectorOptions().setTimeout(5000));
            page.waitForSelector("#password", new Page.WaitForSelectorOptions().setTimeout(5000));
            page.locator("#username").fill(email);
            page.locator("#password").fill(password);
            page.waitForTimeout(1000);

            page.waitForSelector("button[type='submit'].waves-effect.btn.action", new Page.WaitForSelectorOptions().setTimeout(10000));
            page.click("button[type='submit'].waves-effect.btn.primary.action");
            page.waitForLoadState(LoadState.NETWORKIDLE);
            page.waitForTimeout(1000);

            logger.info("Login effettuato con successo per {}", email);
        } catch (Exception e) {
            logger.error("Login fallito per {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Login fallito", e);
        }
    }
    

    private void runTo(Page page) {
        try {
            page.waitForSelector("a:has-text('Eventi da confermare')");
            page.click("a:has-text('Eventi da confermare')");
            System.out.println("Cliccato su 'Eventi da confermare'");

            page.waitForSelector("a.box.rounded.license");
            Set<String> eventiVisitati = new HashSet<>();

            while (true) {
                List<ElementHandle> eventi = page.querySelectorAll("a.box.rounded.license");
                int numeroEventi = eventi.size();
                logger.debug("Eventi trovati: {}", numeroEventi);

                if (numeroEventi == 0) {
                    logger.info("Nessun evento disponibile.");
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
                    logger.info("Tutti gli eventi sono stati visitati!");
                    break;
                }

                eventiVisitati.add(href);
                System.out.println("Apro evento: " + href);
                page.navigate(href);
                page.waitForLoadState();
                page.waitForTimeout(2000);

                // Lettura dati
                page.waitForSelector("input#license_form_lm_event_name");
                String nomeEventoWeb = page.inputValue("input#license_form_lm_event_name");

                // Pulisci il nome evento: prendi tutto ciò che c'è prima di '@' o '-'
                int indexAt = nomeEventoWeb.indexOf('@');
                int indexDash = nomeEventoWeb.indexOf('-');

                // Trova la prima occorrenza tra '@' e '-'
                int cutIndex = -1;
                if (indexAt != -1 && indexDash != -1) {
                    cutIndex = Math.min(indexAt, indexDash);
                } else if (indexAt != -1) {
                    cutIndex = indexAt;
                } else if (indexDash != -1) {
                    cutIndex = indexDash;
                }

                // Se esiste uno dei simboli, taglia il nome
                if (cutIndex != -1) {
                    nomeEventoWeb = nomeEventoWeb.substring(0, cutIndex).trim();
                }

                logger.debug("Nome evento dal web (pulito): {}", nomeEventoWeb);

                page.waitForSelector("input#venue_city");
                String cittaWeb = page.inputValue("input#venue_city");
                logger.debug("Città dal web: {}", cittaWeb);
                EventoRow eventoCorrispondente = trovaEventoCorrispondente(nomeEventoWeb, cittaWeb);

                boolean capInserito = false;
                boolean capienzaInserita = false;
                boolean eventoProcessato = false;

                if (eventoCorrispondente != null) {
                    logger.info("Evento trovato nei dati Excel: {} - {}", nomeEventoWeb, cittaWeb);
                    String cap = eventoCorrispondente.getCap();
                    if (cap != null && !cap.trim().isEmpty()) {
                        try {
                            page.waitForSelector("input#venue_postcode", new Page.WaitForSelectorOptions().setTimeout(5000));
                            page.fill("input#venue_postcode", cap.trim());
                            System.out.println("CAP inserito: " + cap);
                            capInserito = true;
                        } catch (Exception e) {
                            logger.error("Errore nell'inserimento del CAP: {}", e.getMessage());
                        }
                    }

                    String capienza = eventoCorrispondente.getCapienza();
                    if (capienza != null && !capienza.trim().isEmpty()) {
                        try {
                            page.waitForSelector("input#license_form_lm_venue_capacity", new Page.WaitForSelectorOptions().setTimeout(5000));
                            page.fill("input#license_form_lm_venue_capacity", capienza.trim());
                            logger.info("Capienza inserita: {}", capienza);
                            capienzaInserita = true;
                        } catch (Exception e) {
                            logger.error("Campo capienza non trovato: {}", e.getMessage());
                            try {
                                page.waitForSelector("input[name='lm_venue_capacity']", new Page.WaitForSelectorOptions().setTimeout(2000));
                                page.fill("input[name='lm_venue_capacity']", capienza.trim());
                                logger.info("Capienza inserita (campo alternativo): {}", capienza);
                                capienzaInserita = true;
                            } catch (Exception e2) {
                                logger.error("Nessun campo capienza trovato.");
                            }
                        }
                    }

                    if (capInserito && capienzaInserita) {
                        try {
                            logger.info("Dati inseriti, confermo evento...");
                            page.waitForSelector("a#cmd-event-approval.btn.primary", new Page.WaitForSelectorOptions().setTimeout(5000));
                            page.click("a#cmd-event-approval.btn.primary");
                            logger.info("Conferma cliccata.");
                            page.waitForTimeout(2000);

                        } catch (Exception e) {
                            logger.error("Errore clic Conferma: {}", e.getMessage());
                        }

                            // Checkbox di consenso
                        try {
                            page.waitForSelector("label[for='consent_1']", new Page.WaitForSelectorOptions().setTimeout(5000));
                            page.click("label[for='consent_1']");
                            logger.info("Checkbox 1 selezionata.");
                            page.waitForSelector("label[for='consent_7']", new Page.WaitForSelectorOptions().setTimeout(5000));
                            page.click("label[for='consent_7']");
                            logger.info("Checkbox 2 selezionata.");
                            page.waitForTimeout(1000);
                        } catch (Exception e) {
                            logger.error("Errore selezione checkbox: {}", e.getMessage());
                        }

                            // Clicca "Genera licenza"
                        try {
                            page.waitForSelector("button[type='submit'].waves-effect.btn.primary", new Page.WaitForSelectorOptions().setTimeout(5000));
                            page.click("button[type='submit'].waves-effect.btn.primary");
                            logger.info("Licenza generata!");
                            page.waitForTimeout(3000);
                            eventoProcessato = true;
                        } catch (Exception e) {
                            logger.error("Errore su 'Genera licenza': {}", e.getMessage());
                        }

                    } else {
                        logger.error("Dati incompleti. CAP inserito: {}, Capienza inserita: {}", capInserito, capienzaInserita);
                    }
                } else {
                    logger.error("Evento non trovato in Excel: {} - {}", nomeEventoWeb, cittaWeb);
                }

                // Decidi cosa fare dopo il tentativo
                if (eventoProcessato) {
                    logger.info("Evento processato, torno alla homepage...");
                    page.navigate("https://licence.soundreef.com/it/");
                    page.waitForTimeout(2000);
                    page.waitForSelector("a:has-text('Eventi da confermare')");
                    page.click("a:has-text('Eventi da confermare')");
                    page.waitForSelector("a.box.rounded.license");
                } else {
                    logger.info("Evento non processato, torno alla lista...");
                    page.goBack();
                    page.waitForSelector("a.box.rounded.license");
                }

                logger.debug("Avvio nuovo ciclo di processo eventi...");
            }

            logger.info("Tutti gli eventi sono stati gestiti!");

        } catch (Exception e) {
            logger.error("Errore in runTo: {}", e.getMessage(), e);
            e.printStackTrace();
        }
    }



    private EventoRow trovaEventoCorrispondente(String nomeEventoWeb, String cittaWeb) {
        if (listaEventiDaExcel == null || listaEventiDaExcel.isEmpty()) {
            logger.error("Lista eventi da Excel non disponibile");
            return null;
        }

        for (EventoRow evento : listaEventiDaExcel) {
            String nomeEventoExcel = evento.getNomeEventoELocation();
            if (nomeEventoExcel != null) {
                int separatorIndex = nomeEventoExcel.indexOf(" - ");
                if (separatorIndex != -1) {
                    nomeEventoExcel = nomeEventoExcel.substring(0, separatorIndex);
                }

                boolean nomeMatch = nomeEventoWeb != null && nomeEventoWeb.trim().equalsIgnoreCase(nomeEventoExcel.trim());
                boolean cittaMatch = cittaWeb != null && evento.getCitta() != null && cittaWeb.trim().equalsIgnoreCase(evento.getCitta().trim());
                if (nomeMatch && cittaMatch) return evento;
            }
        }

        // Se non trova una corrispondenza esatta, prova una ricerca più flessibile
        for (EventoRow evento : listaEventiDaExcel) {
            String nomeEventoExcel = evento.getNomeEventoELocation();
            if (nomeEventoExcel != null && cittaWeb != null && evento.getCitta() != null) {
                boolean nomeContains = nomeEventoExcel.toLowerCase().contains(nomeEventoWeb.toLowerCase().trim()) || nomeEventoWeb.toLowerCase().contains(nomeEventoExcel.toLowerCase().trim());
                boolean cittaMatch = cittaWeb.trim().equalsIgnoreCase(evento.getCitta().trim());
                
                if (nomeContains && cittaMatch) {
                    logger.info("Trovata corrispondenza parziale per nome evento");
                    return evento;
                }
            }
        }
        
        return null;
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
        logger.debug("Cliccato su 'Accedi person'");
        page.locator("#username").fill(email);
        page.locator("#password").fill(password);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Accedi")).click();
        logger.info("Login eseguito per {}", email);

        Locator cookies = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Allow all cookies"));
        if (cookies.count() > 0) {
            cookies.click();
            logger.debug("Banner cookies accettato");
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
            logger.debug("Tentativo {}: {}", i + 1, entry);
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
