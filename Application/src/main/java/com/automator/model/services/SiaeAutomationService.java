package com.automator.model.services;

import com.automator.view.components.ExcelTableView;
import com.microsoft.playwright.*;

import com.microsoft.playwright.options.WaitForSelectorState;

import com.microsoft.playwright.options.AriaRole;


import com.microsoft.playwright.options.SelectOption;

import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.io.*;
import javafx.collections.ObservableList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SiaeAutomationService {

    private static final Logger logger = LoggerFactory.getLogger(SiaeAutomationService.class);

    private boolean TEST_MODE = true;
    private static final int DEFAULT_PAGES_ASSIGN_BORDERO = 10;
    private static final int MAX_ATTEMPTS_PER_METHOD = 1;

    // ================================
    // CONSTANTS
    // ================================
    private static final String SIAE_URL = "https://www.siae.it/it/";
    private static final String BUTTON_ACCEPT_COOKIES = "ACCETTO";
    private static final String BUTTON_LOGIN = "Accedi";
    private static final String INPUT_TEXT_SELECTOR = "input[type=\"text\"]";
    private static final String INPUT_PASSWORD_SELECTOR = "input[type=\"password\"]";
    private static final String FORM_PORTALE_ORGANIZZATORI = "#PORTUP_STD";
    private static final String FORM_PORTALE_BORDERO = "#APMO_STD";

    private static final String PLACEHOLDER_CITY = "input[placeholder='Città']";
    private static final String PLACEHOLDER_LOCATION = "input[placeholder='Locale / Indirizzo']";
    private static final String ROLE_LISTBOX = "ul[role='listbox']";

    private static final String BUTTON_PROCEDI = "Procedi";
    private static final String BUTTON_OK = "OK";
    private static final String BUTTON_SUBMIT = "button[type='submit']";

    private static final String TEXT_NUOVO_PERMESSO = "Nuovo Permesso";
    private static final String TEXT_I_TUOI_PERMESSI = "text=I tuoi Permessi";
    private static final String TEXT_DA_ACCETTARE = "button[role='tab']:has-text('Da Accettare')";
    private static final String TEXT_DA_ASSEGNARE = "Da assegnare";
    private static final String TEXT_DA_RICONSEGNARE = "Da riconsegnare";

    private static final String BUTTON_ASSEGNA = "assegna";
    private static final String BUTTON_SEARCH = "Cerca";
    private static final String BUTTON_CONFIRM = "Conferma";
    private static final String BUTTON_PROGRAMMI_MUSICALI = "PROGRAMMI MUSICALI";
    private static final String BUTTON_RICONSEGNA = "Riconsegna a SIAE";

    private static final String CHECKPOINT_PATH = "checkpoints/checkpoint.properties";

    private static final String CHECKPOINT_KEY_PAGE = "page";
    private static final String CHECKPOINT_KEY_ROW = "row";

    private static final String PDF_FOLDER = "Desktop";
    private static final String PDF_SUBFOLDER = "LeaDownloads";

    private static final String DEFAULT_RETURN_REASON = "Riconsegna automatizzata";

    public boolean createPermission(String email, String password) {

        ExcelReader reader = new ExcelReader();
        try {
            reader.read(ExcelStorage.getInstance().getFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<EventoRow> eventiOriginali = reader.getEventoRows();
        List<EventoRow> eventlist = EventoRow.espandiEventiConSessioni(eventiOriginali);

        Playwright playwright = Playwright.create();
        Browser browser = launchBrowser(playwright);
        BrowserContext context = browser.newContext();
        Page page = context.newPage();

        page.navigate(SIAE_URL);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(BUTTON_ACCEPT_COOKIES)).click();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(BUTTON_LOGIN).setExact(true)).click();
        page.locator(INPUT_TEXT_SELECTOR).fill(email);
        page.locator(INPUT_PASSWORD_SELECTOR).fill(password);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(BUTTON_LOGIN)).click();



        // 2 "Portale Organizzatori Professionali > Accedi"
        page.locator(FORM_PORTALE_ORGANIZZATORI)
                .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName(BUTTON_LOGIN))
                .click();
        // 3. Nuovo permesso
        page.waitForSelector("text=Nuovo Permesso");
        page.getByText(TEXT_NUOVO_PERMESSO).click();
        int j = 0;

        for(EventoRow evento: eventlist ) {
            if (evento.getNomeLocation().isEmpty()) {
                break;
            }
            // 4. Inserimento dati statici (poi da Excel)
            // Inserisci città

            page.waitForTimeout(3000);
            page.locator(PLACEHOLDER_CITY).click();
            page.locator(PLACEHOLDER_CITY).fill(evento.getCitta());
            page.waitForSelector("ul[role='listbox'] >> text=" + evento.getCitta());
            page.keyboard().press("ArrowDown");
            page.keyboard().press("Enter");

            // Inserisci locale
            page.locator(PLACEHOLDER_LOCATION).click();
            page.locator(PLACEHOLDER_LOCATION).fill(evento.getNomeLocation());
            page.waitForSelector("ul[role='listbox'] >> text=" + evento.getNomeLocation());
            page.keyboard().press("ArrowDown");
            page.keyboard().press("Enter");

            try {
                Locator dropdownSala = page.locator("div[role='button']:has-text('Seleziona lo spazio o la sala del tuo evento')");
                dropdownSala.click();
                page.waitForSelector(ROLE_LISTBOX);

                Locator salaDesiderata = page.locator("li:has-text('" + evento.getSala() + "')");
                if (salaDesiderata.count() > 0) {
                    salaDesiderata.click();
                    logger.info("Sala selezionata: " + evento.getSala());
                } else {
                    logger.info(evento.getSala() + " Questa sala non è disponibile. Uso quella predefinita.");
                    // Puoi anche non fare nulla qui se "spazio predefinito" va bene
                }
            } catch (Exception e) {
                logger.info("Errore durante la selezione della sala. Uso quella predefinita.");
            }

            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(BUTTON_PROCEDI)).click();


            // 5 Seleziona Categoria Evento
            String codice = evento.getCodiceSpettacoloGenere();
            String categoriaEvento = CategoriaEventoMapper.getCategoria(codice);
            String genereEvento = CategoriaEventoMapper.getGenere(codice);

            // Apri la prima tendina (Categoria)
            page.locator("div[role='button']:not(.Mui-disabled)").nth(0).click();
            page.waitForSelector(ROLE_LISTBOX);

            // Clicca sull'opzione della categoria mappata
            page.locator("li:has-text('" + categoriaEvento + "')").click();

            // Apri la seconda tendina (Genere)
            page.locator("div[role='button']:not(.Mui-disabled)").nth(1).click();
            page.waitForSelector(ROLE_LISTBOX);

            // Clicca sull'opzione del genere mappato
            page.locator("li:has-text('" + genereEvento + "')").click();


            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(BUTTON_PROCEDI)).click();


            // 6 Data evento
            try {
                String dataEvento = evento.getDataEvento(); // Es. "2025-06-25"

                logger.info("Data letta da Excel: " + dataEvento); //  DEBUG

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate data = LocalDate.parse(dataEvento, formatter); // Assicurati che il formato sia corretto

                // Apri il calendario
                page.locator("input[readonly]").click();

                // Aspetta che appaia il calendario
                page.waitForSelector(".DayPicker", new Page.WaitForSelectorOptions().setTimeout(3000));

                String meseItaliano = data.getMonth().getDisplayName(TextStyle.FULL, Locale.ITALIAN);
                int anno = data.getYear();
                int giorno = data.getDayOfMonth();

                navigateToTargetMonth(page, meseItaliano, anno);
                selectDay(page, giorno);

                logger.info("Data selezionata con successo");
            } catch (Exception e) {
                logger.error("Errore nella selezione della data: " + e.getMessage());
                e.printStackTrace();
            }
            // 7. Fasce orarie da Excel
            try	 {
                String orarioInizio = evento.getSessioni().replace("H", "").trim();
                LocalTime inizio = LocalTime.parse(orarioInizio, DateTimeFormatter.ofPattern("HH:mm"));
                LocalTime fine = inizio.plusHours(1);
                String orarioFine = fine.format(DateTimeFormatter.ofPattern("HH:mm"));

                page.locator("input[placeholder='hh:mm']").nth(0).fill(orarioInizio);
                page.locator("input[placeholder='hh:mm']").nth(1).fill(orarioFine);

                page.locator("label:has-text('MODALITÀ DI INGRESSO') + div div[role='button']").nth(0).click();
                page.waitForSelector(ROLE_LISTBOX);
                page.locator("li:has-text('Ingresso a pagamento')").click();

            } catch (Exception e) {
                logger.error("Errore nella gestione delle sessioni orarie: " + e.getMessage());
                e.printStackTrace();
            }



            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(BUTTON_PROCEDI)).click();

            // 7 Descrizione
            page.locator("input[name='furtherInfo']").fill(evento.getNomeEventoELocation());
            Locator checkbox = page.locator("label:has-text('Sono in possesso di licenza OGC') input[type='checkbox']");
            checkbox.click();

            // carica PDF
            try {
                String nomeFilePDF = EventoRow.toPDFName(
                        evento.getNomeEventoELocation(),
                        evento.getDataEvento(),
                        evento.getSessioni()
                );
                // Percorso dinamico Desktop > LeaDownloads
                String userHome = System.getProperty("user.home");
                Path cartellaPDF = Paths.get(userHome, PDF_FOLDER, PDF_SUBFOLDER);
                Path pathPDF = cartellaPDF.resolve(nomeFilePDF);

                if (!Files.exists(pathPDF)) {
                    String errorMsg = "File PDF non trovato: " + pathPDF.toString();
                    logger.error(errorMsg);
                    throw new RuntimeException(errorMsg);
                } else {
                    logger.info("Carico PDF: " + pathPDF.toString());
                    page.setInputFiles("input[type='file']", pathPDF);
                    page.waitForSelector("text=" + nomeFilePDF);
                    logger.info("PDF caricato con successo");
                }
            } catch (Exception e) {
                logger.error("Errore nel caricamento del PDF: " + e.getMessage());
            }

            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(BUTTON_PROCEDI)).click();


            Locator dropdown = page.locator("div#outlined-select");
            dropdown.click(new Locator.ClickOptions().setForce(true));

            // Aspetta che l'opzione "No" sia visibile
            page.waitForSelector("ul[role='listbox'] li[role='option']");

            // Clicca su "No" in modo preciso usando getByRole con setExact
            page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions()
                    .setName("No")
                    .setExact(true)
            ).click();
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(BUTTON_PROCEDI)).click();

            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(BUTTON_PROCEDI)).click();
            try {
                // Primo INVIA (pagina principale)
                page.waitForSelector(BUTTON_SUBMIT, new Page.WaitForSelectorOptions().setTimeout(10000));
                page.locator(BUTTON_SUBMIT).click();

                // Secondo INVIA (nel popup di conferma)
                page.waitForSelector("div[role='dialog'] button:has-text('Invia')", new Page.WaitForSelectorOptions().setTimeout(5000));
                page.locator("div[role='dialog'] button:has-text('Invia')").click();

                // Attendi popup "La richiesta è stata inviata con successo"
                page.waitForSelector("text=La richiesta è stata inviata con successo", new Page.WaitForSelectorOptions().setTimeout(10000));
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(BUTTON_OK)).click();

                // Attendi la schermata successiva con "Inviata"
                page.waitForSelector("text=Inviata", new Page.WaitForSelectorOptions().setTimeout(10000));

                // Torna alla home del portale organizzatori
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Portale Organizzatori Professionali")).click();
                page.waitForTimeout(1000);

                // Clicca nuovamente su "Nuovo Permesso"
                page.waitForSelector("text=Nuovo Permesso", new Page.WaitForSelectorOptions().setTimeout(5000));
                page.getByText(TEXT_NUOVO_PERMESSO).click();

                logger.info("Evento inviato con successo. Passo al successivo...");
            } catch (Exception e) {
                logger.error("Errore dopo invio permesso: " + e.getMessage());

            }
            logger.info("Elaborazione evento " + (j + 1) + " di " + eventlist.size());
        }
        page.close();
        browser.close();
        return true;

    }

    public boolean licenseCheck(String email, String password) {

        try (Playwright playwright = Playwright.create()) {

            //lancio del browser
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            /*navigazione ed attesa verso SIAE*/
            page.navigate(SIAE_URL);
            page.waitForTimeout(2000);

            //gestione dell'accetazione dei cookies, se presenti
            Locator acceptCookiesBtn = page.locator("button.iubenda-cs-accept-btn");
            if (acceptCookiesBtn.isVisible(new Locator.IsVisibleOptions().setTimeout(2000))) {
                acceptCookiesBtn.click();
                page.waitForTimeout(1000); // Breve attesa per sicurezza
            }

            //Clicca sul bottone BUTTON_LOGIN/ LOGIN
            page.locator("button:has(span:text('Accedi'))").nth(0).click();
            page.waitForTimeout(2000); // Attendere il caricamento del form

            // Inserisci l'email e la password
            page.locator("label:text('E-mail')").locator("xpath=..").locator("input").fill(email);
            page.locator("label:text('Password')").locator("xpath=..").locator("input").fill(password);

            // invio form di login
            page.locator("button:has(span:text('Accedi'))").last().click();
            page.waitForTimeout(5000); // Attendere l'autenticazione

            // DOPO il login: inizia il percorso verso la pagina di accettazioen dei permessi
            //clicca il bottone "Accedi" nel form con id PORTUP_STD

            page.waitForSelector("form#PORTUP_STD", new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE));
            page.locator("form#PORTUP_STD button[type='submit']").click();
            //page.waitForTimeout(5000);


            page.waitForTimeout(3000);

            // Cerca il testo "I tuoi Permessi" con un approccio più flessibile
            Locator permessiText = page.locator(TEXT_I_TUOI_PERMESSI).first();
            permessiText.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(10000));

            // Trova l'elemento cliccabile più vicino (button, div cliccabile, ecc.)
            Locator clickableElement = permessiText.locator("xpath=ancestor-or-self::*[self::button or self::div[@role='button'] or contains(@class, 'clickable') or @onclick]").first();

            if (clickableElement.count() > 0) {
                clickableElement.click();
            } else {
                // Se non trova un elemento cliccabile, prova a cliccare direttamente sul testo
                permessiText.click();
            }

            //tabella dei permessi --> NAVIGAZIONE VERSO SEZIONE "DA ACCETTARE"
            Locator daAccettareTab = page.locator(TEXT_DA_ACCETTARE).first();
            daAccettareTab.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(10000));
            daAccettareTab.click();
            logger.info("Click eseguito su 'Da Accettare'");

            page.waitForTimeout(1500);
            //permessiTable(page);
            //processPermissionsOnCurrentPage(page);
            processAllPermissionPages(page);

            page.waitForTimeout(5000);
            page.close();
            browser.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public boolean givebackBordero(String email, String password) {
        int maxAttempts = MAX_ATTEMPTS_PER_METHOD;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try (Playwright playwright = Playwright.create()) {
                logger.info("RICONSEGNA BORDERO:  Tentativo " + attempt + " di " + maxAttempts);
                System.out.println("RICONSEGNA BORDERO:  Tentativo " + attempt + " di " + maxAttempts);
                Browser browser = launchBrowser(playwright);
                BrowserContext context = browser.newContext();
                Page page = context.newPage();

                loginToSiaeBorderoPage(page, email, password);
                navigateToGiveBackSection(page);
                processAllPagesGiveBack(page);

                page.waitForTimeout(3000);
                page.close();
                browser.close();
                logger.info(" Operazione riuscita al tentativo " + attempt);
                System.out.println(" Operazione riuscita al tentativo " + attempt);
                return true;
            } catch (Exception e) {
                logger.error(" Errore al tentativo " + attempt);
                System.out.println(" Errore al tentativo " + attempt);
                e.printStackTrace();
                if (attempt == maxAttempts) {
                    logger.error(" Tutti i tentativi falliti.");
                    return false;
                }
            }
        }
        return false;
    }

    private Browser launchBrowser(Playwright playwright) {
        return playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
    }

    private void loginToSiaeBorderoPage(Page page, String email, String password) {
        page.navigate(SIAE_URL);
        logger.info("Navigating to " + SIAE_URL);
        System.out.println("Navigating to " + SIAE_URL);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(BUTTON_ACCEPT_COOKIES)).click();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(BUTTON_LOGIN).setExact(true)).click();
        page.locator(INPUT_TEXT_SELECTOR).fill(email);
        page.locator(INPUT_PASSWORD_SELECTOR).fill(password);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(BUTTON_LOGIN)).click();
        logger.info("Logging in...");
        System.out.println("Logging in...");
        page.locator(FORM_PORTALE_BORDERO)
                .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName(BUTTON_LOGIN))
                .click();
    }

    private void navigateToAssignSection(Page page) {
        page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName(TEXT_DA_ASSEGNARE)).click();

        logger.info("Navigating to assign bordero page");
        System.out.println("Navigating to assign bordero page");

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Data")).click();
        page.waitForTimeout(3000);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Data")).click();

    }

    private void navigateToGiveBackSection(Page page) {
        page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName(TEXT_DA_RICONSEGNARE)).click();

        Locator button5 = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("5"));

        // Verifica se esiste e visibile entro 3 secondi
        if (button5.count() > 0) {
            try {
                button5.waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(3000)
                );
                button5.click();

                Locator option50 = page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName("50"));
                if (option50.count() > 0) {
                    option50.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
                    option50.click();
                }
            } catch (PlaywrightException e) {
                logger.info(" Pulsante '5' non trovato o non visibile entro il timeout, proseguo senza selezionare righe per pagina.");
            }
        } else {
            logger.info(" Pulsante '5' non presente, proseguo.");
        }
    }

    private void processAllPermissionPages(Page page) {
        try {
            boolean hasMorePages = true;
            int currentPage = 1;

            while (hasMorePages) {
                logger.info("=== Processando pagina " + currentPage + " dei permessi ===");

                // Processa tutti i permessi della pagina corrente
                processPermissionsOnCurrentPage(page);

                // Controlla se ci sono altre pagine
                hasMorePages = navigateToNextPageIfExists(page);

                if (hasMorePages) {
                    currentPage++;
                    // Attendi che la nuova pagina si carichi completamente
                    page.waitForLoadState(LoadState.NETWORKIDLE);
                    page.waitForTimeout(3000);
                }

                // Sicurezza: evita loop infiniti
                if (currentPage > 20) {
                    logger.info("Raggiunto limite massimo pagine (20)");
                    break;
                }
            }

            logger.info("=== Completato processamento di tutte le pagine ===");

        } catch (Exception e) {
            logger.error("Errore durante la navigazione delle pagine: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processPermissionsOnCurrentPage(Page page) {
        try {
            logger.info("Cercando bottoni 'Visualizza' nella pagina corrente...");

            // Attendi che la tabella sia completamente caricata
            page.waitForSelector("table, .MuiTable-root", new Page.WaitForSelectorOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(10000));

            boolean foundMoreButtons = true;
            int totalProcessed = 0;

            while (foundMoreButtons) {
                // Ri-trova tutti i bottoni "Visualizza" ogni volta (riferimenti freschi)
                Locator visualizzaButtons = page.locator(
                        "button:has-text('Visualizza'), " +
                                "button:has-text('VISUALIZZA'), " +
                                "a:has-text('Visualizza'), " +
                                "*[role='button']:has-text('Visualizza')"
                );

                int currentButtonCount = visualizzaButtons.count();
                logger.info("Trovati " + currentButtonCount + " bottoni 'Visualizza' rimanenti (totale processati: " + totalProcessed + ")");

                if (currentButtonCount > 0) {
                    // Prendi sempre il PRIMO bottone disponibile
                    Locator firstButton = visualizzaButtons.first();

                    try {
                        logger.info("Processando bottone " + (totalProcessed + 1) + " (primo disponibile)");

                        // Scroll al bottone e clicca
                        firstButton.scrollIntoViewIfNeeded();
                        firstButton.waitFor(new Locator.WaitForOptions()
                                .setState(WaitForSelectorState.VISIBLE)
                                .setTimeout(5000));

                        logger.info("Cliccando su 'Visualizza'...");
                        firstButton.click();

                        // Attendi che la nuova pagina si carichi
                        page.waitForLoadState(LoadState.NETWORKIDLE);
                        page.waitForTimeout(3000);

                        // Processa la pagina del permesso e accettalo
                        processPermissionDetailsPage(page);

                        // Attendi che la tabella si ricarichi completamente dopo l'accettazione
                        page.waitForLoadState(LoadState.NETWORKIDLE);
                        page.waitForTimeout(3000);

                        // Attendi che la tabella sia di nuovo visibile
                        page.waitForSelector("table, .MuiTable-root", new Page.WaitForSelectorOptions()
                                .setState(WaitForSelectorState.VISIBLE)
                                .setTimeout(10000));

                        totalProcessed++;
                        logger.info("Bottone processato con successo (totale processati: " + totalProcessed + ")");

                    } catch (Exception e) {
                        logger.error("Errore nel processare il bottone " + (totalProcessed + 1) + ": " + e.getMessage());

                        // In caso di errore, prova comunque a tornare alla tabella
                        try {
                            page.goBack();
                            page.waitForLoadState(LoadState.NETWORKIDLE);
                            page.waitForTimeout(2000);

                            // Riclicca su "Da Accettare" per essere sicuri di essere nella sezione corretta
                            Locator daAccettareTab = page.locator(TEXT_DA_ACCETTARE).first();
                            if (daAccettareTab.count() > 0) {
                                daAccettareTab.click();
                                page.waitForTimeout(2000);
                            }

                        } catch (Exception backError) {
                            logger.error("Errore nel tornare indietro: " + backError.getMessage());
                        }

                        totalProcessed++; // Continua con il prossimo anche se questo ha fallito
                    }
                } else {
                    logger.info("Nessun bottone 'Visualizza' rimanente nella pagina corrente");
                    foundMoreButtons = false;
                }

                // Sicurezza: evita loop infiniti
                if (totalProcessed > 50) {
                    logger.info("Raggiunto limite massimo di permessi per pagina (50)");
                    break;
                }
            }

            logger.info("Completato processamento di tutti i bottoni nella pagina corrente. Totale processati: " + totalProcessed);

        } catch (Exception e) {
            logger.error("Errore durante il processamento dei permessi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processPermissionDetailsPage(Page page) {
        try {
            logger.info("Processando pagina dettagli permesso...");

            // Attendi che la pagina dei dettagli sia completamente caricata
            page.waitForLoadState(LoadState.NETWORKIDLE);
            page.waitForTimeout(2000);

            // Cerca il bottone "Accetta Permesso"
            Locator accettaButton = page.locator(
                    "button:has-text('Accetta Permesso'), " +
                            "button:has-text('ACCETTA PERMESSO'), " +
                            ".MuiButton-root:has-text('Accetta Permesso')"
            );

            if (accettaButton.count() > 0) {
                logger.info("Trovato bottone 'Accetta Permesso', cliccando...");

                // Scroll al bottone e clicca
                accettaButton.first().scrollIntoViewIfNeeded();
                accettaButton.first().waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(5000));

                accettaButton.first().click();

                // Attendi che appaia la finestra di conferma
                page.waitForTimeout(1500);

                // Cerca il bottone "Conferma" nella finestra di dialogo
                Locator confermaButton = page.locator(
                        "button:has-text('Conferma'), " +
                                "button:has-text('CONFERMA'), " +
                                ".MuiButton-root:has-text('Conferma'), " +
                                "[role='dialog'] button:has-text('Conferma')"
                );

                if (confermaButton.count() > 0) {
                    logger.info("Trovato bottone 'Conferma' nella finestra di dialogo, cliccando...");

                    confermaButton.first().waitFor(new Locator.WaitForOptions()
                            .setState(WaitForSelectorState.VISIBLE)
                            .setTimeout(5000));

                    confermaButton.first().click();

                    // Attendi che la conferma sia processata
                    page.waitForTimeout(10000);

                    // Cerca il bottone "Ok" finale
                    Locator okButton = page.locator(
                            "button:has-text('Ok'), " +
                                    "button:has-text('OK'), " +
                                    ".MuiButton-root:has-text('Ok'), " +
                                    "[role='dialog'] button:has-text('Ok')"
                    );

                    if (okButton.count() > 0) {
                        logger.info("Trovato bottone 'Ok' finale, cliccando...");

                        okButton.first().waitFor(new Locator.WaitForOptions()
                                .setState(WaitForSelectorState.VISIBLE)
                                .setTimeout(5000));

                        okButton.first().click();

                        // Attendi che il browser torni alla tabella automaticamente
                       /* page.waitForTimeout(2000);
                        page.waitForLoadState(LoadState.NETWORKIDLE);
                        page.waitForTimeout(1000);*/

                        logger.info("Permesso accettato con successo!");

                        Locator daAccettareTab = page.locator("button[role='tab']:has-text('Da Accettare')").first();
                        daAccettareTab.waitFor(new Locator.WaitForOptions()
                                .setState(WaitForSelectorState.VISIBLE)
                                .setTimeout(10000));
                        daAccettareTab.click();
                        logger.info("Click eseguito su 'Da Accettare'post accettazione permesso");
                        // page.goBack();

                    } else {
                        logger.info("Bottone 'Ok' finale non trovato");
                        // Attendi comunque un po' per eventuali redirect automatici
                        page.waitForLoadState(LoadState.NETWORKIDLE);
                        page.waitForTimeout(1000);
                    }

                } else {
                    logger.info("Bottone 'Conferma' non trovato nella finestra di dialogo");
                }

            } else {
                logger.info("Bottone 'Accetta Permesso' non trovato nella pagina");
            }

        } catch (Exception e) {
            logger.error("Errore durante l'accettazione del permesso: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean assignBordero(String email, String password) {
        int maxAttempts = MAX_ATTEMPTS_PER_METHOD;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try (Playwright playwright = Playwright.create()) {
                logger.info("ASSIGN BORDERO: Tentativo " + attempt + " di " + maxAttempts);
                System.out.println("ASSIGN BORDERO: Tentativo " + attempt + " di " + maxAttempts);
                Browser browser = launchBrowser(playwright);
                BrowserContext context = browser.newContext();
                Page page = context.newPage();

                loginToSiaeBorderoPage(page, email, password);
                navigateToAssignSection(page);
                processAllPagesAssign(page);

                page.waitForTimeout(3000);
                page.close();
                browser.close();
                logger.info("Operazione riuscita al tentativo " + attempt);
                System.out.println("Operazione riuscita al tentativo " + attempt);
                return true;
            } catch (Exception e) {
                logger.error("Errore al tentativo " + attempt);
                System.out.println("Errore al tentativo " + attempt);
                e.printStackTrace();
                if (attempt == maxAttempts) {
                    logger.error("Tutti i tentativi falliti.");
                    System.out.println("Tutti i tentativi falliti.");
                    return false;
                }
            }
        }
        return false;
    }


    private void processAllPagesAssign(Page page) {
        int optionCount = DEFAULT_PAGES_ASSIGN_BORDERO;

        for (int i = 0; i < optionCount; i++) {
            logger.info(" Elaboro pagina: " + (i + 1) + " #################");
            logger.info("\n");
            System.out.println(" Elaboro pagina: " + (i + 1) + " #################");
            page.waitForSelector("table tbody tr");

            Locator rows = page.locator("table tbody tr");
            int rowCount = rows.count();

            for (int j = 0; j < rowCount; j++) {
                try {
                    processRowAssign(page, rows.nth(j), j+1);
                    saveCheckpoint(i, j + 1); // Salva dopo ogni riga completata
                } catch (Exception e) {
                    logger.error(" Errore alla pagina " + (i+1) + ", riga " + (j+1));
                    System.out.println(" Errore alla pagina " + (i+1) + ", riga " + (j+1));
                    saveCheckpoint(i, j);
                    throw e;
                }
            }

            if(i!=optionCount-1) {
                logger.info("Vado alla prossima pagina");
                System.out.println("Vado alla prossima pagina");
                page.locator("span[title='Previous Page'] + span button").click();
                page.waitForTimeout(1000);
            }
        }
    }

    private void processRowAssign(Page page, Locator row, int index){
        logger.info("Record numero " + index +" : clic su 'assegna'");
        System.out.println("Record numero " + index +" : clic su 'assegna'");
        // Stampa contenuto effettivo della riga
        Locator cells = row.locator("td");
        int cellCount = cells.count();

        // Recupera Data e Locale/Spazio
        String data = cells.nth(1).innerText().trim();
        String localeSpazio = cells.nth(3).innerText().trim();

        StringBuilder rowContent = new StringBuilder(" Riga " + index + ": ");
        for (int i = 0; i < cellCount; i++) {
            try {
                String text = cells.nth(i).innerText().trim();
                if (!text.isEmpty()) {
                    rowContent.append("[").append(text).append("] ");
                }
            } catch (Exception e) {
                rowContent.append("[Errore lettura cella] ");
            }
        }
        logger.info("Sto valutando la seguente riga di SIAE "+rowContent);
        System.out.println("Sto valutando la seguente riga di SIAE "+rowContent);

        Locator assegnaButton = row.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName(BUTTON_ASSEGNA));
        if (assegnaButton.count() > 0) {
            assegnaButton.first().click();
        }

        page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("E-mail")).click();
        logger.info(data);
        logger.info(localeSpazio);
        String emailToFill = "";
        try {
            logger.info("Cerco una riga excel relativa all'evento in data " + data + " per il locale " + localeSpazio);
            System.out.println("Cerco una riga excel relativa all'evento in data " + data + " per il locale " + localeSpazio);
            ExcelReader reader = new ExcelReader();
            reader.read(ExcelStorage.getInstance().getFile());
            emailToFill = reader.getValueByTwoKeys("Data evento",data, "Nome location",localeSpazio,"E-mail");
            if(emailToFill.isEmpty()){
                logger.info("Questa riga SIAE non corrisponde a alcuna riga nel file excel");
                System.out.println("Questa riga SIAE non corrisponde a alcuna riga nel file excel");
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(BUTTON_PROGRAMMI_MUSICALI)).click();
                return;
            }
            page.getByRole(AriaRole.TEXTBOX).fill(emailToFill);
        } catch (Exception e) {
            e.printStackTrace();
        }
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(BUTTON_SEARCH)).click();
        page.waitForTimeout(1000);

        Locator cell = page.getByRole(AriaRole.CELL, new Page.GetByRoleOptions().setName(emailToFill));

        if (cell.count() > 0 && cell.first().isVisible()) {
            logger.info(" Email trovata nella tabella");
            System.out.println(" Email trovata nella tabella");
            page.getByRole(AriaRole.RADIO).check();
            if(!TEST_MODE) {page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(BUTTON_CONFIRM)).click();}
            else {navigateToAssignSection(page);}
        } else {
            logger.info(" Email non trovata, inserimento manuale");
            System.out.println(" Email non trovata, inserimento manuale");
            page.getByText("Non hai trovato il direttore").click();
            page.getByRole(AriaRole.TEXTBOX).fill(emailToFill);
            if(!TEST_MODE) {page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(BUTTON_CONFIRM)).click();}
            else {navigateToAssignSection(page);}
        }
    }

    private void processAllPagesGiveBack(Page page) {
        int j=0;

        while (true) {
            j++;
            try {
                if (processRowGiveBack(page))
                    saveCheckpoint(0, j + 1);
                else break;
            } catch (Exception e) {
                logger.error(" Errore alla pagina 0, riga " + (j+1));
                saveCheckpoint(0, j); // Salva dove si è fermato
                throw e; // facoltativo: puoi anche continuare
            }
        }
    }

    private boolean processRowGiveBack(Page page) {

        page.waitForTimeout(3000);
        logger.info("Ho aspettato");
        Locator assegnaButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions()
                        .setName("Visualizza")
                        .setExact(false)
        );

        if (assegnaButton.count() > 0) {
            logger.info(" Trovato un visualizza");
            assegnaButton.first().click();
        } else {
            logger.info(" Dati finiti: nessun bottone 'visualizza' trovato nella riga.");
            return false;
        }

        Locator noPageLocator = page.getByText("Si è verificato un errore",
                new Page.GetByTextOptions().setExact(false));

        // Aspetta fino a 2 secondi che l'elemento compaia (se non compare, count() rimane 0)
        try {
            noPageLocator.waitFor(new Locator.WaitForOptions().setTimeout(2000));
        } catch (PlaywrightException e) {
            logger.info("timeout: l'elemento non è stato trovato entro 2 secondi");
        }

        if (noPageLocator.count() > 0) {
            logger.info("Elemento ‘Si è verificato un errore’ trovato");
            page.goBack();
            page.waitForTimeout(1000);
            return true;
        }

        // Clic su "Riconsegna a SIAE"
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(BUTTON_RICONSEGNA)).click();


        // Verifica se compare il messaggio "Il Programma che stai"
        Locator warningText = page.getByText("Il Programma che stai");

        if (warningText.isVisible()) {
            logger.info("Caso Warning Verificato trovato");
            // Clic sul textbox
            page.getByRole(AriaRole.TEXTBOX).click();
            // Inserisce testo nel campo
            page.getByRole(AriaRole.TEXTBOX).fill(DEFAULT_RETURN_REASON);
            // Conferma effettiva
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(BUTTON_CONFIRM)).click();
            page.waitForTimeout(1000);
            return true;
        }

        try {
            // Cerca il gruppo con il testo "Vuoi riconsegnare il PM a"
            Locator giveBackText = page.getByRole(
                    AriaRole.GROUP,
                    new Page.GetByRoleOptions().setName("Vuoi riconsegnare il PM a")
            );

            // Attende che il gruppo sia visibile entro 5 secondi
            giveBackText.waitFor(new Locator.WaitForOptions().setTimeout(5000));

            // Clicca sul primo div dentro il gruppo
            giveBackText.locator("div").first().click();
        } catch (PlaywrightException e) {
            // Se il gruppo non è presente o cliccabile, continua senza errori
            logger.info("Elemento 'Vuoi riconsegnare il PM a' non trovato o non cliccabile, si prosegue.");
        }

        try {
            // Attende che il campo sia visibile entro 5 secondi (5000 ms)
            Locator motivoCancellazione = page.locator("#filled-input-cancellation-reason");
            motivoCancellazione.waitFor(new Locator.WaitForOptions().setTimeout(5000));
            motivoCancellazione.fill(DEFAULT_RETURN_REASON);
        } catch (TimeoutError e) {
            // L'elemento non è stato trovato entro 5 secondi, prosegue senza fare nulla
            logger.info("Campo motivo cancellazione non trovato, si prosegue.");
        }


        // Seleziona radio "Programma artista principale"
        Locator radio = page.getByRole(AriaRole.RADIO, new Page.GetByRoleOptions().setName("Programma artista principale"));
        if (radio.count() > 0) {
            radio.check();
        }

        //Conferma effettiva
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(BUTTON_CONFIRM)).click();
        return true;
    }

    private boolean navigateToNextPageIfExists(Page page) {
        try {
            logger.info("Controllando se esistono altre pagine...");

            // Cerca il bottone "Next Page" (freccia destra)
            Locator nextPageButton = page.locator(
                    "button:not([disabled]):not(.Mui-disabled):has(svg path[d='M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z'])"
            );

            if (nextPageButton.count() > 0) {
                logger.info("Trovato bottone Next, navigando alla pagina successiva...");
                nextPageButton.click();
                return true;
            } else {
                logger.info("Nessuna pagina successiva trovata");
                return false;
            }

        } catch (Exception e) {
            logger.error("Errore nella navigazione alla pagina successiva: " + e.getMessage());
            return false;
        }
    }

    private void navigateToTargetMonth(Page page, String targetMonth, int targetYear) {
        int maxAttempts = 12;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            try {
                // Trova il mese corrente visualizzato
                String currentMonthText = getCurrentMonth(page);
                logger.info("Mese corrente: " + currentMonthText);

                // Controlla se siamo già nel mese giusto
                if (currentMonthText.toLowerCase().contains(targetMonth.toLowerCase()) &&
                        currentMonthText.contains(String.valueOf(targetYear))) {
                    logger.info(" Mese corretto raggiunto: " + currentMonthText);
                    return;
                }

                // Naviga al mese successivo
                clickNextMonth(page);
                page.waitForTimeout(500); // Piccola pausa per l'aggiornamento

            } catch (Exception e) {
                logger.error("Errore durante navigazione mese: " + e.getMessage());
                break;
            }
        }

        throw new RuntimeException(" Non riesco a raggiungere il mese: " + targetMonth + " " + targetYear);
    }

    private String getCurrentMonth(Page page) {
        // Prova diversi selettori per il mese
        String[] selectors = {
                ".DayPicker-Caption",
                ".DayPicker-Month .DayPicker-Caption",
                "div[role='heading']"
        };

        for (String selector : selectors) {
            try {
                Locator monthElements = page.locator(selector);

                // Se ci sono più elementi, trova il primo visibile
                for (int i = 0; i < monthElements.count(); i++) {
                    Locator element = monthElements.nth(i);
                    if (element.isVisible()) {
                        String text = element.textContent().trim();
                        if (!text.isEmpty()) {
                            return text;
                        }
                    }
                }
            } catch (Exception e) {
                // Continua con il prossimo selettore
            }
        }

        throw new RuntimeException(" Impossibile trovare il mese corrente");
    }

    private void clickNextMonth(Page page) {
        String[] selectors = {
                "button[aria-label='Go to next month']",
                ".DayPicker-NavButton--next",
                "button[title*='next']",
                "button[title*='successivo']"
        };

        for (String selector : selectors) {
            try {
                Locator button = page.locator(selector);
                if (button.count() > 0 && button.isVisible()) {
                    button.click();
                    logger.info(" Cliccato su mese successivo");
                    return;
                }
            } catch (Exception e) {
                // Continua con il prossimo selettore
            }
        }

        throw new RuntimeException(" Bottone 'mese successivo' non trovato");
    }

    private void selectDay(Page page, int day) {
        try {
            // Approccio 1: Cerca per testo del giorno escludendo giorni esterni
            Locator dayLocator = page.locator(
                    String.format(".DayPicker-Day:has-text('%d'):not(.DayPicker-Day--outside)", day)
            );

            if (dayLocator.count() > 0) {
                // Se ci sono più giorni con lo stesso numero, trova quello del mese corrente
                for (int i = 0; i < dayLocator.count(); i++) {
                    Locator currentDay = dayLocator.nth(i);
                    if (currentDay.isVisible() &&
                            !currentDay.getAttribute("class").contains("DayPicker-Day--outside")) {
                        currentDay.click();
                        logger.info(" Giorno " + day + " selezionato (metodo 1)");
                        return;
                    }
                }
            }

            // Approccio 2: Cerca tra tutte le celle del calendario
            Locator allDays = page.locator(".DayPicker-Day[role='gridcell']");
            for (int i = 0; i < allDays.count(); i++) {
                Locator currentDay = allDays.nth(i);
                String dayText = currentDay.textContent().trim();

                if (dayText.equals(String.valueOf(day)) &&
                        currentDay.isVisible() &&
                        !currentDay.getAttribute("class").contains("DayPicker-Day--outside")) {
                    currentDay.click();
                    logger.info(" Giorno " + day + " selezionato (metodo 2)");
                    return;
                }
            }

            // Approccio 3: Fallback con aria-label parziale
            Locator dayWithAriaLabel = page.locator(
                    String.format("div[role='gridcell'][aria-label*='%d ']", day)
            );

            if (dayWithAriaLabel.count() > 0) {
                dayWithAriaLabel.first().click();
                logger.info(" Giorno " + day + " selezionato (metodo 3)");
                return;
            }

        } catch (Exception e) {
            logger.error("Errore nella selezione del giorno: " + e.getMessage());
        }

        throw new RuntimeException(" Impossibile selezionare il giorno " + day);
    }

    private void saveCheckpoint(int page, int row) {
        try {
            Path checkpointFile = Paths.get(CHECKPOINT_PATH);
            Files.createDirectories(checkpointFile.getParent()); // crea cartella checkpoints/ se non esiste

            Properties props = new Properties();
            props.setProperty(CHECKPOINT_KEY_PAGE, String.valueOf(page));
            props.setProperty(CHECKPOINT_KEY_ROW, String.valueOf(row));

            try (FileWriter writer = new FileWriter(checkpointFile.toFile())) {
                props.store(writer, "Checkpoint salvato nel progetto corrente");
                logger.info("Checkpoint salvato in: " + checkpointFile.toAbsolutePath());
            }
        } catch (IOException e) {
            logger.error("Errore nel salvataggio del checkpoint:");
            e.printStackTrace();
        }
    }

    public static String getCategoria(String codice) {
        String lower = codice.toLowerCase();

        if (lower.contains("musica leggera") || lower.contains("musica classica") || lower.contains("jazz") || lower.contains("folk")) {
            return "CONCERTI, MANIFESTAZIONI MUSICALI, MUSICA IN EVENTI, SPETTACOLI DI ARTE VARIA";
        }

        if (lower.contains("rivista") || lower.contains("lirica") || lower.contains("operetta") || lower.contains("cabaret")
                || lower.contains("dialettale") || lower.contains("recital") || lower.contains("balletto")
                || lower.contains("danza") || lower.contains("burattini")) {
            return "TEATRO, LIRICA, RECITAL, BALLETTI";
        }

        if (lower.contains("sport") || CategoriaEventoMapper.isGenereSportivo(lower)) {
            return "MUSICA IN EVENTI SPORTIVI E GARE DA BALLO";
        }

        if (lower.contains("ballo") || lower.contains("orchestra")
                || lower.contains("ballo sm") || lower.contains("ballo or")
                || (lower.contains("esecuz") && (lower.contains("sm") || lower.contains("or")))) {
            return "TRATTENIMENTI MUSICALI CON BALLO O SENZA BALLO";
        }

        return "CONCERTI, MANIFESTAZIONI MUSICALI, MUSICA IN EVENTI, SPETTACOLI DI ARTE VARIA"; // fallback
    }

    public static String getGenere(String codice) {
        String lower = codice.toLowerCase();

        if (lower.contains("musica leggera")) return "Concerto leggera";
        if (lower.contains("musica classica")) return "Concerto classica";
        if (lower.contains("jazz")) return "Concerto jazz";
        if (lower.contains("folk")) return "Concerti folkloristici";
        if (lower.contains("banda")) return "Banda";
        if (lower.contains("piazza")) return "Feste in piazza";
        if (lower.contains("mostre")) return "Mostre";
        if (lower.contains("fiere")) return "Fiere";
        if (lower.contains("parchi")) return "Parchi divertimento";
        if (lower.contains("circo")) return "Circo";

        // Generi sportivi
        if (lower.contains("atletica")) return "Atletica leggera";
        if (lower.contains("automobilismo")) return "Automobilismo";
        if (lower.contains("baseball")) return "Baseball";
        if (lower.contains("basket")) return "Basket";
        if (lower.contains("calcio serie c")) return "Calcio serie C e inferiori";
        if (lower.contains("calcio")) return "Calcio";
        if (lower.contains("ciclismo")) return "Ciclismo";
        if (lower.contains("ippici")) return "Concorsi ippici";
        if (lower.contains("cavalli")) return "Corse cavalli";
        if (lower.contains("motociclismo")) return "Motociclismo";
        if (lower.contains("motonautica")) return "Motonautica";
        if (lower.contains("nuoto")) return "Nuoto e pallanuoto";
        if (lower.contains("pallavolo")) return "Pallavolo";
        if (lower.contains("pugilato")) return "Pugilato";
        if (lower.contains("rugby")) return "Rugby";
        if (lower.contains("invernali")) return "Sport invernali";
        if (lower.contains("tennis")) return "Tennis";
        if (lower.contains("sport")) return "Sport";

        // Generi teatrali
        if (lower.contains("rivista") || lower.contains("comm.musicale")) return "Comm.musicale/Rivista";
        if (lower.contains("lirica")) return "Lirica";
        if (lower.contains("operetta")) return "Operetta";
        if (lower.contains("cabaret")) return "Prosa / Cabaret";
        if (lower.contains("dialettale")) return "Prosa dialettale";
        if (lower.contains("recital")) return "Recital";
        if (lower.contains("balletto")) return "Balletto";
        if (lower.contains("danza")) return "Danza";
        if (lower.contains("burattini")) return "Burattini";

        // Ballo / Musica
        if (lower.contains("ballo sm")) return "Ballo SM";
        if (lower.contains("ballo or")) return "Ballo OR";
        if (lower.contains("esecuz") && lower.contains("sm")) return "Esecuz. musicali SM";
        if (lower.contains("esecuz") && lower.contains("or")) return "Esecuz. musicali OR";

        return "Arte varia"; // fallback
    }

    class CategoriaEventoMapper {

        public static boolean isGenereSportivo(String lower) {
            return lower.contains("atletica") || lower.contains("automobilismo") || lower.contains("baseball") ||
                    lower.contains("basket") || lower.contains("calcio") || lower.contains("ciclismo") ||
                    lower.contains("ippici") || lower.contains("cavalli") || lower.contains("motociclismo") ||
                    lower.contains("motonautica") || lower.contains("nuoto") || lower.contains("pallavolo") ||
                    lower.contains("pugilato") || lower.contains("rugby") || lower.contains("invernali") ||
                    lower.contains("tennis") || lower.contains("sport");
        }

        public static String getCategoria(String codice) {
            String lower = codice.toLowerCase();

            if (lower.contains("musica leggera") || lower.contains("musica classica") || lower.contains("jazz") || lower.contains("folk")) {
                return "CONCERTI, MANIFESTAZIONI MUSICALI, MUSICA IN EVENTI, SPETTACOLI DI ARTE VARIA";
            }

            if (lower.contains("rivista") || lower.contains("lirica") || lower.contains("operetta") || lower.contains("cabaret")
                    || lower.contains("dialettale") || lower.contains("recital") || lower.contains("balletto")
                    || lower.contains("danza") || lower.contains("burattini")) {
                return "TEATRO, LIRICA, RECITAL, BALLETTI";
            }

            if (lower.contains("sport") || CategoriaEventoMapper.isGenereSportivo(lower)) {
                return "MUSICA IN EVENTI SPORTIVI E GARE DA BALLO";
            }

            if (lower.contains("ballo") || lower.contains("orchestra")
                    || lower.contains("ballo sm") || lower.contains("ballo or")
                    || (lower.contains("esecuz") && (lower.contains("sm") || lower.contains("or")))) {
                return "TRATTENIMENTI MUSICALI CON BALLO O SENZA BALLO";
            }

            return "CONCERTI, MANIFESTAZIONI MUSICALI, MUSICA IN EVENTI, SPETTACOLI DI ARTE VARIA"; // fallback
        }

        public static String getGenere(String codice) {
            String lower = codice.toLowerCase();

            if (lower.contains("musica leggera")) return "Concerto leggera";
            if (lower.contains("musica classica")) return "Concerto classica";
            if (lower.contains("jazz")) return "Concerto jazz";
            if (lower.contains("folk")) return "Concerti folkloristici";
            if (lower.contains("banda")) return "Banda";
            if (lower.contains("piazza")) return "Feste in piazza";
            if (lower.contains("mostre")) return "Mostre";
            if (lower.contains("fiere")) return "Fiere";
            if (lower.contains("parchi")) return "Parchi divertimento";
            if (lower.contains("circo")) return "Circo";

            // Generi sportivi
            if (lower.contains("atletica")) return "Atletica leggera";
            if (lower.contains("automobilismo")) return "Automobilismo";
            if (lower.contains("baseball")) return "Baseball";
            if (lower.contains("basket")) return "Basket";
            if (lower.contains("calcio serie c")) return "Calcio serie C e inferiori";
            if (lower.contains("calcio")) return "Calcio";
            if (lower.contains("ciclismo")) return "Ciclismo";
            if (lower.contains("ippici")) return "Concorsi ippici";
            if (lower.contains("cavalli")) return "Corse cavalli";
            if (lower.contains("motociclismo")) return "Motociclismo";
            if (lower.contains("motonautica")) return "Motonautica";
            if (lower.contains("nuoto")) return "Nuoto e pallanuoto";
            if (lower.contains("pallavolo")) return "Pallavolo";
            if (lower.contains("pugilato")) return "Pugilato";
            if (lower.contains("rugby")) return "Rugby";
            if (lower.contains("invernali")) return "Sport invernali";
            if (lower.contains("tennis")) return "Tennis";
            if (lower.contains("sport")) return "Sport";

            // Generi teatrali
            if (lower.contains("rivista") || lower.contains("comm.musicale")) return "Comm.musicale/Rivista";
            if (lower.contains("lirica")) return "Lirica";
            if (lower.contains("operetta")) return "Operetta";
            if (lower.contains("cabaret")) return "Prosa / Cabaret";
            if (lower.contains("dialettale")) return "Prosa dialettale";
            if (lower.contains("recital")) return "Recital";
            if (lower.contains("balletto")) return "Balletto";
            if (lower.contains("danza")) return "Danza";
            if (lower.contains("burattini")) return "Burattini";

            // Ballo / Musica
            if (lower.contains("ballo sm")) return "Ballo SM";
            if (lower.contains("ballo or")) return "Ballo OR";
            if (lower.contains("esecuz") && lower.contains("sm")) return "Esecuz. musicali SM";
            if (lower.contains("esecuz") && lower.contains("or")) return "Esecuz. musicali OR";

            return "Arte varia"; // fallback
        }
    }

}
