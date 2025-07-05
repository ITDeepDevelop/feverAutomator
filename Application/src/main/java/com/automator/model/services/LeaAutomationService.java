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
	
	// AGGIUNTA: Variabile di istanza per la lista degli eventi da Excel
    private List<EventoRow> listaEventiDaExcel;

    // Metodo setter per impostare la lista degli eventi caricati dall'UI
    public void setListaEventiDaExcel(List<EventoRow> listaEventi) {
        this.listaEventiDaExcel = listaEventi;
    }
	

   /* public boolean op1(String email, String password) {
        // logica vera dell'operazione 1
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium()
                    .launch(new BrowserType.LaunchOptions()
                            .setHeadless(false)
                    );
            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            page.navigate("https://licence.soundreef.com/it/");
            
            allowCookies(page);
            System.out.print(password + email + "op1");
            loginProcedure(page,email,password);
            runTo(page);
            
            
            
            page.waitForTimeout(5000);
             
            page.close();
            browser.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }*/
	
	public boolean op1(String email, String password) {
	    try {
	        // PRIMA COSA: Carica i dati Excel
	        System.out.println("Caricamento dati da Excel...");
	        ExcelReader reader = new ExcelReader();
	        
	        // Verifica che il file Excel sia stato impostato
	        if (ExcelStorage.getInstance().getFile() == null) {
	            System.err.println("❌ Nessun file Excel caricato! Assicurati di aver selezionato un file Excel prima di avviare l'operazione.");
	            return false;
	        }
	        
	        // Leggi il file Excel
	        reader.read(ExcelStorage.getInstance().getFile());
	        
	        // Carica tutti gli eventi dalla classe EventoRow
	        this.listaEventiDaExcel = reader.getEventoRows();
	        
	        if (this.listaEventiDaExcel == null || this.listaEventiDaExcel.isEmpty()) {
	            System.err.println("❌ Nessun evento trovato nel file Excel!");
	            return false;
	        }
	        
	        System.out.println("✅ Caricati " + this.listaEventiDaExcel.size() + " eventi da Excel");
	        
	       
	        
	        // Opzionale: stampa un riassunto degli eventi caricati
	        System.out.println("📋 Primi eventi caricati:");
	        for (int i = 0; i < Math.min(5, this.listaEventiDaExcel.size()); i++) {
	            EventoRow evento = this.listaEventiDaExcel.get(i);
	            System.out.println("  " + (i+1) + ". " + evento.getNomeEventoELocation() + 
	                             " - " + evento.getCitta() + 
	                             " (CAP: " + evento.getCap() + 
	                             ", Capienza: " + evento.getCapienza() + ")");
	        }
	        if (this.listaEventiDaExcel.size() > 5) {
	            System.out.println("  ... e altri " + (this.listaEventiDaExcel.size() - 5) + " eventi");
	        }
	        
	    } catch (Exception e) {
	        System.err.println("❌ Errore nel caricamento dei dati Excel: " + e.getMessage());
	        e.printStackTrace();
	        return false;
	    }
	    
	    // SECONDA PARTE: Avvia l'automazione web
	    try (Playwright playwright = Playwright.create()) {
	        System.out.println("Avvio automazione web...");
	        
	        Browser browser = playwright.chromium()
	                .launch(new BrowserType.LaunchOptions()
	                        .setHeadless(false)
	                );
	        BrowserContext context = browser.newContext();
	        Page page = context.newPage();

	        page.navigate("https://licence.soundreef.com/it/");
	        
	        allowCookies(page);
	        System.out.println("Credenziali: " + email);
	        loginProcedure(page, email, password);
	        runTo(page);
	        
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
    
    public void allowCookies(Page page)
    {
    
    	try {
            // Attende che il bottone dei cookie sia visibile
            page.waitForSelector("#CybotCookiebotDialogBodyButtonAccept", new Page.WaitForSelectorOptions()
                .setTimeout(10000));
            
            // Clicca sul bottone "Accetta tutti i cookie"
            page.click("#CybotCookiebotDialogBodyButtonAccept");
            
            // Attende un momento per assicurarsi che il dialog si chiuda
            page.waitForTimeout(1000);
            
        } catch (Exception e) {
            System.out.println("Errore nell'accettazione dei cookie: " + e.getMessage());
            // Non rilancia l'eccezione per non interrompere il flusso principale
        }

    }
    
    public void loginProcedure(Page page, String email, String password)
    {
    	System.out.print(password + email);
    	try {
            // Step 1: Clicca sul link "Accedi"
            page.waitForSelector("a[href='https://licence.soundreef.com/it/login']", new Page.WaitForSelectorOptions()
                .setTimeout(10000));
            
            page.click("a[href='https://licence.soundreef.com/it/login']");
            
            // Attende che la pagina di login si carichi
            page.waitForLoadState(LoadState.NETWORKIDLE);
            
         // Step 2: Inserimento credenziali
            // Attende che i campi di input siano visibili
            page.waitForSelector("#username", new Page.WaitForSelectorOptions()
                .setTimeout(5000));
            page.waitForSelector("#password", new Page.WaitForSelectorOptions()
                .setTimeout(5000));
            
         // Step 2: Inserimento credenziali
            // Inserisci l'email e la password usando locator come nel tuo esempio SIAE
            page.locator("#username").fill(email);
            page.locator("#password").fill(password);
            
            page.waitForTimeout(1000); // Breve attesa per sicurezza

            
         // Step 3: Clicca sul bottone "Accedi"
            page.waitForSelector("button[type='submit'].waves-effect.btn.action", new Page.WaitForSelectorOptions()
                .setTimeout(10000));
            
            page.click("button[type='submit'].waves-effect.btn.primary.action");
            
            // Attende che il login sia completato (la pagina cambi)
            page.waitForLoadState(LoadState.NETWORKIDLE);
            
            // Breve pausa per assicurarsi che i valori siano inseriti
            page.waitForTimeout(1000);
            
            System.out.println("Credenziali inserite con successo");
            
            System.out.println("Navigazione verso la pagina di login completata");
            
        } catch (Exception e) {
            System.out.println("Errore durante la procedura di login: " + e.getMessage());
            throw new RuntimeException("Login fallito", e);
        }
    	
    }
    
   /* private void runTo(Page page) {
        try {
        	
        	
            // Clicca sul link "Eventi da confermare"
            page.waitForSelector("a:has-text('Eventi da confermare')");
            page.click("a:has-text('Eventi da confermare')");
            System.out.println("Cliccato su 'Eventi da confermare'");
            
            // Attende che la pagina degli eventi sia caricata
            page.waitForSelector("a.box.rounded.license");
            
            // Itera sugli eventi
            while (true) {
                List<ElementHandle> eventi = page.querySelectorAll("a.box.rounded.license");
                int numeroEventi = eventi.size();
                System.out.println("Eventi trovati: " + numeroEventi);
                for (int i = 0; i < numeroEventi; i++) {
                    // Ricarica gli elementi ogni volta
                    eventi = page.querySelectorAll("a.box.rounded.license");
                    
                    // Ottiene l'href dell'evento corrente
                    String href = eventi.get(i).getAttribute("href");
                    System.out.println("Apro evento: " + href);
                    
                    // Naviga all'evento
                    page.navigate(href);
                    
                    // Attende che la pagina si carichi
                    page.waitForLoadState();
                    page.waitForTimeout(2000); // attende 2 secondi 
                    
                 // Crea un nuovo oggetto EventoRow
                    EventoRow evento = new EventoRow();
                    
                    // Attendi il campo nome evento e leggilo
                    page.waitForSelector("input#license_form_lm_event_name");
                    String nomeEvento = page.inputValue("input#license_form_lm_event_name");
                    System.out.println("Nome evento: " + nomeEvento);
                    
                    
                    
                 // Attendi il campo città
                    page.waitForSelector("input#venue_city");
                    
                    // Legge la città
                    String city = page.inputValue("input#venue_city");
                    System.out.println("Città trovata: " + city);
                    
                   
                    
                    
                    
                 // Inserisci il CAP nel campo input dedicato
                   /* page.fill("input#venue_postcode", cap);
                    System.out.println("CAP inserito: " + cap);*/
                    // Attendi 2s e torna indietro
                    /*page.waitForTimeout(2000);
                    // Torna indietro
                    page.goBack();
                    page.waitForSelector("a.box.rounded.license");
                }

                break; // esci dal ciclo dopo una sola iterazione per ora
            }

        } catch (Exception e) {
            System.err.println("Errore in runTo: " + e.getMessage());
            e.printStackTrace();
        }
    }*/
    
    private void runTo(Page page) {
        try {
            // Clicca sul link "Eventi da confermare"
            page.waitForSelector("a:has-text('Eventi da confermare')");
            page.click("a:has-text('Eventi da confermare')");
            System.out.println("Cliccato su 'Eventi da confermare'");

            // Attende che la pagina degli eventi sia caricata
            page.waitForSelector("a.box.rounded.license");

            // Set per evitare di processare lo stesso evento più volte
            Set<String> eventiVisitati = new HashSet<>();

            while (true) {
                List<ElementHandle> eventi = page.querySelectorAll("a.box.rounded.license");
                int numeroEventi = eventi.size();
                System.out.println("Eventi trovati: " + numeroEventi);

                if (numeroEventi == 0) {
                    System.out.println("🎉 Nessun evento disponibile.");
                    break;
                }

                // Trova il primo evento non ancora visitato
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
                System.out.println("Apro evento: " + href);
                page.navigate(href);
                page.waitForLoadState();
                page.waitForTimeout(2000);

                // Leggi i dati dall'evento web
                page.waitForSelector("input#license_form_lm_event_name");
                String nomeEventoWeb = page.inputValue("input#license_form_lm_event_name");
                System.out.println("Nome evento dal web: " + nomeEventoWeb);

                page.waitForSelector("input#venue_city");
                String cittaWeb = page.inputValue("input#venue_city");
                System.out.println("Città dal web: " + cittaWeb);

                EventoRow eventoCorrispondente = trovaEventoCorrispondente(nomeEventoWeb, cittaWeb);
                boolean capInserito = false;
                boolean capienzaInserita = false;
                boolean eventoProcessato = false;

                if (eventoCorrispondente != null) {
                    System.out.println("Evento trovato nei dati Excel!");

                    // Inserisci CAP
                    String cap = eventoCorrispondente.getCap();
                    if (cap != null && !cap.trim().isEmpty()) {
                        try {
                            page.waitForSelector("input#venue_postcode", new Page.WaitForSelectorOptions().setTimeout(5000));
                            page.fill("input#venue_postcode", cap.trim());
                            System.out.println("CAP inserito: " + cap);
                            capInserito = true;
                        } catch (Exception e) {
                            System.out.println("Errore nell'inserimento del CAP: " + e.getMessage());
                        }
                    }

                    // Inserisci capienza
                    String capienza = eventoCorrispondente.getCapienza();
                    if (capienza != null && !capienza.trim().isEmpty()) {
                        try {
                            page.waitForSelector("input#license_form_lm_venue_capacity", new Page.WaitForSelectorOptions().setTimeout(5000));
                            page.fill("input#license_form_lm_venue_capacity", capienza.trim());
                            System.out.println("Capienza inserita: " + capienza);
                            capienzaInserita = true;
                        } catch (Exception e) {
                            System.out.println("Campo capienza non trovato: " + e.getMessage());
                            try {
                                page.waitForSelector("input[name='lm_venue_capacity']", new Page.WaitForSelectorOptions().setTimeout(2000));
                                page.fill("input[name='lm_venue_capacity']", capienza.trim());
                                System.out.println("Capienza inserita (campo alternativo): " + capienza);
                                capienzaInserita = true;
                            } catch (Exception e2) {
                                System.out.println("Nessun campo capienza trovato.");
                            }
                        }
                    }

                    if (capInserito && capienzaInserita) {
                        try {
                            System.out.println("CAP e capienza inseriti. Cliccando su Conferma...");
                            page.waitForSelector("a#cmd-event-approval.btn.primary", new Page.WaitForSelectorOptions().setTimeout(5000));
                            page.click("a#cmd-event-approval.btn.primary");
                            System.out.println("✅ Conferma cliccata.");
                            page.waitForTimeout(2000);

                            // Checkbox di consenso
                            try {
                                page.waitForSelector("label[for='consent_1']", new Page.WaitForSelectorOptions().setTimeout(5000));
                                page.click("label[for='consent_1']");
                                System.out.println("✅ Checkbox 1 selezionata.");

                                page.waitForSelector("label[for='consent_7']", new Page.WaitForSelectorOptions().setTimeout(5000));
                                page.click("label[for='consent_7']");
                                System.out.println("✅ Checkbox 2 selezionata.");

                                page.waitForTimeout(1000);

                                // Clicca "Genera licenza"
                                try {
                                    page.waitForSelector("button[type='submit'].waves-effect.btn.primary", new Page.WaitForSelectorOptions().setTimeout(5000));
                                    page.click("button[type='submit'].waves-effect.btn.primary");
                                    System.out.println("✅ Licenza generata!");
                                    page.waitForTimeout(3000);
                                    eventoProcessato = true;
                                } catch (Exception e) {
                                    System.out.println("Errore su 'Genera licenza': " + e.getMessage());
                                }
                            } catch (Exception e) {
                                System.out.println("Errore selezione checkbox: " + e.getMessage());
                            }
                        } catch (Exception e) {
                            System.out.println("Errore clic Conferma: " + e.getMessage());
                        }
                    } else {
                        System.out.println("⚠️ Dati incompleti. CAP inserito: " + capInserito + ", Capienza inserita: " + capienzaInserita);
                    }
                } else {
                    System.out.println("Evento non trovato in Excel: " + nomeEventoWeb + " - " + cittaWeb);
                }

                // Decidi cosa fare dopo il tentativo
                if (eventoProcessato) {
                    System.out.println("🔄 Tornando alla homepage per il prossimo evento...");
                    page.navigate("https://licence.soundreef.com/it/");
                    page.waitForTimeout(2000);
                    page.waitForSelector("a:has-text('Eventi da confermare')");
                    page.click("a:has-text('Eventi da confermare')");
                    page.waitForSelector("a.box.rounded.license");
                } else {
                    System.out.println("↩️ Evento non processato, tornando alla lista...");
                    page.goBack();
                    page.waitForSelector("a.box.rounded.license");
                }

                System.out.println("➡️ Prossimo ciclo...");
            }

            System.out.println("✅ Tutti gli eventi sono stati gestiti!");

        } catch (Exception e) {
            System.err.println("Errore in runTo: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Trova l'evento corrispondente nei dati Excel basandosi su nome evento e città
     */
    private EventoRow trovaEventoCorrispondente(String nomeEventoWeb, String cittaWeb) {
        if (listaEventiDaExcel == null || listaEventiDaExcel.isEmpty()) {
            System.out.println("Lista eventi da Excel non disponibile");
            return null;
        }
        
        for (EventoRow evento : listaEventiDaExcel) {
            // Confronta il nome dell'evento (gestisce il caso in cui il nome Excel contenga " - Location")
            String nomeEventoExcel = evento.getNomeEventoELocation();
            if (nomeEventoExcel != null) {
                // Estrae solo la parte del nome evento (prima del " - ")
                int separatorIndex = nomeEventoExcel.indexOf(" - ");
                if (separatorIndex != -1) {
                    nomeEventoExcel = nomeEventoExcel.substring(0, separatorIndex);
                }
                
                // Confronta nome evento e città (case-insensitive e trim)
                boolean nomeMatch = nomeEventoWeb != null && 
                                   nomeEventoWeb.trim().equalsIgnoreCase(nomeEventoExcel.trim());
                boolean cittaMatch = cittaWeb != null && evento.getCitta() != null && 
                                    cittaWeb.trim().equalsIgnoreCase(evento.getCitta().trim());
                
                if (nomeMatch && cittaMatch) {
                    return evento;
                }
            }
        }
        
        // Se non trova una corrispondenza esatta, prova una ricerca più flessibile
        for (EventoRow evento : listaEventiDaExcel) {
            String nomeEventoExcel = evento.getNomeEventoELocation();
            if (nomeEventoExcel != null && cittaWeb != null && evento.getCitta() != null) {
                // Controllo se il nome dell'evento web è contenuto nel nome Excel
                boolean nomeContains = nomeEventoExcel.toLowerCase().contains(nomeEventoWeb.toLowerCase().trim()) ||
                                      nomeEventoWeb.toLowerCase().contains(nomeEventoExcel.toLowerCase().trim());
                boolean cittaMatch = cittaWeb.trim().equalsIgnoreCase(evento.getCitta().trim());
                
                if (nomeContains && cittaMatch) {
                    System.out.println("Trovata corrispondenza parziale per nome evento");
                    return evento;
                }
            }
        }
        
        return null;
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
