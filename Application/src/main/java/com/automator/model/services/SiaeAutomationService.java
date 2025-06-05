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
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.io.*;
import javafx.collections.ObservableList;


public class SiaeAutomationService {
	
	
	 public boolean runOperation1() {
	        Playwright playwright = Playwright.create();
	            Browser browser = launchBrowser(playwright);
	            BrowserContext context = browser.newContext();
	            Page page = context.newPage();

	      
	                page.navigate("https://www.siae.it/it/");
	                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("ACCETTO")).click();
	                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Accedi").setExact(true)).click();
	                page.locator("input[type=\"text\"]").fill("originals_italy@feverup.com");
	                page.locator("input[type=\"password\"]").fill("Siae@123");
	                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Accedi")).click();
	               
	           
	            
	                // 2 "Portale Organizzatori Professionali > Accedi"
	                page.locator("#PORTUP_STD")
	                        .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Accedi"))
	                        .click();
	             // 3. Nuovo permesso
	                page.waitForSelector("text=Nuovo Permesso");
	                page.getByText("Nuovo Permesso").click();
	                

	            // 4. Inserimento dati statici (poi da Excel)
	             // Inserisci città
	                page.locator("input[placeholder='Città']").click();
	                page.locator("input[placeholder='Città']").fill("BERGAMO");
	                page.waitForSelector("ul[role='listbox'] >> text=BERGAMO");
	                page.keyboard().press("ArrowDown");
	                page.keyboard().press("Enter");

	                // Inserisci locale
	                page.locator("input[placeholder='Locale / Indirizzo']").click();
	                page.locator("input[placeholder='Locale / Indirizzo']").fill("CENTRO CONGRESSI GIOVANNI");
	                page.waitForSelector("ul[role='listbox'] >> text=CENTRO CONGRESSI");
	                page.keyboard().press("ArrowDown");
	                page.keyboard().press("Enter");
	                
	                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Procedi")).click();
	                
	                
	             // 5 Seleziona Categoria Evento
	     
	                
	                page.locator("div[role='button']:not(.Mui-disabled)").nth(0).click();
	                page.waitForSelector("ul[role='listbox']");
	                page.locator("li:has-text('CONCERTI, MANIFESTAZIONI MUSICALI')").click();

	                
	                page.locator("div[role='button']:not(.Mui-disabled)").nth(1).click(); // seconda tendina
	                page.waitForSelector("ul[role='listbox']");
	                page.locator("li:has-text('CONCERTO LEGGERA')").click();

	                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Procedi")).click();
	                
	             // 6 Data evento   
	                
	                try {
	                    // Apri il calendario
	                    page.locator("input[readonly]").click();
	                    
	                    // Aspetta che appaia il calendario
	                    page.waitForSelector(".DayPicker", new Page.WaitForSelectorOptions().setTimeout(3000));
	                    
	                    navigateToTargetMonth(page, "Giugno", 2025);
	                    
	                    selectDay(page, 25);
	                    
	                    System.out.println("✅ Data selezionata con successo");
	                    
	                } catch (Exception e) {
	                    System.err.println("❌ Errore nella selezione della data: " + e.getMessage());
	                    e.printStackTrace();
	                }
	                
	             // Inserisci l'orario di inizio
	                page.locator("input[placeholder='hh:mm']").nth(0).click();
	                page.locator("input[placeholder='hh:mm']").nth(0).fill("21:00");

	                // Inserisci l'orario di fine
	                page.locator("input[placeholder='hh:mm']").nth(1).click();
	                page.locator("input[placeholder='hh:mm']").nth(1).fill("23:00");

	                // Seleziona modalità di ingresso (dropdown)
	                page.locator("label:has-text('MODALITÀ DI INGRESSO') + div div[role='button']").click();
	                page.waitForSelector("ul[role='listbox']");
	                page.locator("li:has-text('Ingresso a pagamento')").click();
	 
	                
	                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Procedi")).click();
	                
	             // 7 Descrizione 
	                page.locator("input[name='furtherInfo']").fill("Candlelight Spring: Coldplay vs Imagine Dragons - Giovanni XXIII");
	               
	                Locator dropdown = page.locator("div#outlined-select");
	                dropdown.click(new Locator.ClickOptions().setForce(true));

	                // 2. Aspetta che la lista sia visibile
	             page.waitForSelector("ul[role='listbox']");
	             
	             	// 3. Clicca sulla voce corretta del menu
	             page.locator("li:has-text('Tutti i brani non tutelati o di pubblico dominio')").click();

	                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Procedi")).click();
	                page.pause();

	            page.close();
	            browser.close();
	            return true;
	      
	    }

/*
    public List<EventoSiaeRow> estraiEventiDaExcel(ExcelTableView excelView) {
        List<EventoSiaeRow> eventi = new ArrayList<>();
        ObservableList<ObservableList<String>> righe = excelView.getTable().getItems();


        for (ObservableList<String> riga : righe) {
            // Stampa per debug
            System.out.println("🔍 Riga Excel: " + riga);

            // Verifica che la riga abbia abbastanza colonne
            if (riga.size() < 10) {
                System.err.println("⚠ Riga ignorata (colonne insufficienti): " + riga.size());
                continue;
            }

            EventoSiaeRow evento = new EventoSiaeRow(
            	    riga.get(0), // city
            	    riga.get(1), // address
            	    riga.get(2), // category
            	    riga.get(3), // subCategory
            	    riga.get(4), // date
            	    riga.get(5), // timeFrom
            	    riga.get(6), // timeTo
            	    riga.get(7), // ticketType
            	    riga.get(8), // description
            	    riga.get(9)  // musicType
            	);

            eventi.add(evento);
        }

        return eventi;
    }*/

   // }
    /*
     * 03/06/25 --> gestita la presenza di tabella con più pagine + *30/05/25
     * */
    public boolean licenseCheck(String email, String password) {
    	/*
    	 * attualmente, per facilità di test,le credenziali verrano salvate "in chiaro",
    	 * successivamente verrano prese da input nella UI 
    	 * */
    	  


    	try (Playwright playwright = Playwright.create()) {
    		
            //lancio del browser
    		Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            BrowserContext context = browser.newContext();
            Page page = context.newPage();
           
            /*navigazione ed attesa verso SIAE*/
            page.navigate("https://www.siae.it/it/");
            page.waitForTimeout(2000);
            
            //gestione dell'accetazione dei cookies, se presenti
            Locator acceptCookiesBtn = page.locator("button.iubenda-cs-accept-btn");
            if (acceptCookiesBtn.isVisible(new Locator.IsVisibleOptions().setTimeout(2000))) {
                acceptCookiesBtn.click();
                page.waitForTimeout(1000); // Breve attesa per sicurezza
            }

            //Clicca sul bottone "Accedi"/ LOGIN
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
            Locator permessiText = page.locator("text=I tuoi Permessi").first();
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
            Locator daAccettareTab = page.locator("button[role='tab']:has-text('Da Accettare')").first();
            daAccettareTab.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(10000));
            daAccettareTab.click();
            System.out.println("Click eseguito su 'Da Accettare'");

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
    
 // Metodo principale per processare tutte le pagine dei permessi
    private void processAllPermissionPages(Page page) {
        try {
            boolean hasMorePages = true;
            int currentPage = 1;
            
            while (hasMorePages) {
                System.out.println("=== Processando pagina " + currentPage + " dei permessi ===");
                
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
                    System.out.println("Raggiunto limite massimo pagine (20)");
                    break;
                }
            }
            
            System.out.println("=== Completato processamento di tutte le pagine ===");
            
        } catch (Exception e) {
            System.err.println("Errore durante la navigazione delle pagine: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Metodo per processare tutti i permessi nella pagina corrente
    private void processPermissionsOnCurrentPage(Page page) {
        try {
            System.out.println("Cercando bottoni 'Visualizza' nella pagina corrente...");
            
            // Attendi che la tabella sia completamente caricata
            page.waitForSelector("table, .MuiTable-root", new Page.WaitForSelectorOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(10000));
            
            boolean foundMoreButtons = true;
            int processedCount = 0;
            
            while (foundMoreButtons) {
                // Ri-trova tutti i bottoni "Visualizza" ogni volta (riferimenti freschi)
                Locator visualizzaButtons = page.locator(
                    "button:has-text('Visualizza'), " +
                    "button:has-text('VISUALIZZA'), " +
                    "a:has-text('Visualizza'), " +
                    "*[role='button']:has-text('Visualizza')"
                );
                
                int totalButtons = visualizzaButtons.count();
                System.out.println("Trovati " + totalButtons + " bottoni 'Visualizza' totali, processati: " + processedCount);
                
                if (processedCount >= totalButtons) {
                    System.out.println("Tutti i bottoni della pagina corrente sono stati processati");
                    foundMoreButtons = false;
                    break;
                }
                
                // Prendi il prossimo bottone da processare
                Locator currentButton = visualizzaButtons.nth(processedCount);
                
                if (currentButton.count() > 0) {
                    try {
                        System.out.println("Processando bottone " + (processedCount + 1) + "/" + totalButtons);
                        
                        // Scroll al bottone e clicca
                        currentButton.scrollIntoViewIfNeeded();
                        currentButton.waitFor(new Locator.WaitForOptions()
                            .setState(WaitForSelectorState.VISIBLE)
                            .setTimeout(5000));
                        
                        System.out.println("Cliccando su 'Visualizza'...");
                        currentButton.click();
                        
                        // Attendi che la nuova pagina si carichi
                        page.waitForLoadState(LoadState.NETWORKIDLE);
                        page.waitForTimeout(3000);
                        
                        // Processa la pagina del permesso (qui puoi aggiungere la tua logica)
                       // processPermissionDetailsPage(page);
                        
                        // TORNA INDIETRO alla tabella
                        System.out.println("Tornando indietro alla tabella...");
                        page.goBack();
                        
                        // Attendi che la tabella si ricarichi completamente
                        page.waitForLoadState(LoadState.NETWORKIDLE);
                        page.waitForTimeout(3000);
                        
                        // Attendi che la tabella sia di nuovo visibile
                        page.waitForSelector("table, .MuiTable-root", new Page.WaitForSelectorOptions()
                            .setState(WaitForSelectorState.VISIBLE)
                            .setTimeout(10000));
                        
                        processedCount++;
                        System.out.println("Bottone " + processedCount + " processato con successo");
                        
                    } catch (Exception e) {
                        System.err.println("Errore nel processare il bottone " + (processedCount + 1) + ": " + e.getMessage());
                        
                        // In caso di errore, prova comunque a tornare alla tabella
                        try {
                            page.goBack();
                            page.waitForLoadState(LoadState.NETWORKIDLE);
                            page.waitForTimeout(2000);
                        } catch (Exception backError) {
                            System.err.println("Errore nel tornare indietro: " + backError.getMessage());
                        }
                        
                        processedCount++; // Continua con il prossimo anche se questo ha fallito
                    }
                } else {
                    foundMoreButtons = false;
                }
            }
            
            System.out.println("Completato processamento di tutti i bottoni nella pagina corrente");
            
        } catch (Exception e) {
            System.err.println("Errore durante il processamento dei permessi: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
 // Metodo per navigare alla pagina successiva se esiste
    private boolean navigateToNextPageIfExists(Page page) {
        try {
            System.out.println("Controllando se esistono altre pagine...");
            
            // Cerca il bottone "Next Page" (freccia destra)
            Locator nextPageButton = page.locator(
                "button:not([disabled]):not(.Mui-disabled):has(svg path[d='M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z'])"
            );
            
            if (nextPageButton.count() > 0) {
                System.out.println("Trovato bottone Next, navigando alla pagina successiva...");
                nextPageButton.click();
                return true;
            } else {
                System.out.println("Nessuna pagina successiva trovata");
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("Errore nella navigazione alla pagina successiva: " + e.getMessage());
            return false;
        }
    }
    
    
    /*
     * 30/05/2025 --> manca solo l'implementazione relativa all click su "accetta permesso" ed alla conferma 
     * 					di accettazione sul modale successivo
     * 					
     * */
    /*private void permessiTable(Page page) {
    	// Loop principale per tutte le pagine
    	boolean hasMorePages = true;
    	int totalProcessed = 0;

    	while (hasMorePages) {
    	    // Attendi che la tabella sia visibile
    	    page.waitForSelector("table.MuiTable-root tbody tr", new Page.WaitForSelectorOptions().setTimeout(10000));
    	    
    	    // Conta le righe
    	    int rowCount = page.locator("table.MuiTable-root tbody tr").count();
    	    System.out.println("Righe trovate: " + rowCount);
    	    //qualora non siano presenti righe si chiuderà in automatico la pagina
    	    if(rowCount == 0) {
    	    	page.close();
    	    	System.out.println("Nessuna riga trovata nella tabella I TUOI PERMESSI >>> DA ACCETTARE");
    	    }
    	    
    	    // Processa ogni riga
    	    for (int i = 0; i < rowCount; i++) {
    	        System.out.println("Processando riga " + (i + 1));
    	        
    	        // Clicca sul pulsante Visualizza della riga corrente
    	        Locator visualizzaBtn = page.locator("table.MuiTable-root tbody tr").nth(i).locator("button:has-text('Visualizza')");
    	        visualizzaBtn.click();
    	        page.waitForTimeout(3000);
    	        
    	        // QUI AGGIUNGI LA LOGICA PER LA PAGINA DI DETTAGLIO
    	        
    	        // Torna indietro
    	        page.goBack();
    	        page.waitForTimeout(2000);
    	        
    	        totalProcessed++;
    	    }
    	    
    	    // Controlla se esiste una pagina successiva
    	    try {
    	        Locator nextBtn = page.locator("button[aria-label*='next'], button:has-text('Successiva')").first();
    	        if (nextBtn.count() > 0 && nextBtn.isEnabled()) {
    	            nextBtn.click();
    	            page.waitForTimeout(3000);
    	        } else {
    	            hasMorePages = false;
    	        }
    	    } catch (Exception e) {
    	        hasMorePages = false;
    	    }
    	}

    	System.out.println("Totale permessi processati: " + totalProcessed);
    }*/
    

    public boolean givebackBordero(String email, String password) {
        int maxAttempts = 2;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try (Playwright playwright = Playwright.create()) {
                System.out.println("▶ Tentativo " + attempt + " di " + maxAttempts);
                Browser browser = launchBrowser(playwright);
                BrowserContext context = browser.newContext();
                Page page = context.newPage();

                loginToSiaeBorderoPage(page, email, password);
                navigateToGiveBackSection(page);
                processAllPagesGiveBack(page);

                page.waitForTimeout(3000);
                page.close();
                browser.close();
                System.out.println("✅ Operazione riuscita al tentativo " + attempt);
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

    public boolean assignBordero(String email, String password) {
        int maxAttempts = 2;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try (Playwright playwright = Playwright.create()) {
                System.out.println("▶ Tentativo " + attempt + " di " + maxAttempts);
                Browser browser = launchBrowser(playwright);
                BrowserContext context = browser.newContext();
                Page page = context.newPage();

                loginToSiaeBorderoPage(page, email, password);
                navigateToAssignSection(page);
                processAllPagesAssign(page);

                page.waitForTimeout(3000);
                page.close();
                browser.close();
                System.out.println("✅ Operazione riuscita al tentativo " + attempt);
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


    private Browser launchBrowser(Playwright playwright) {
        return playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
    }

    private void loginToSiaeBorderoPage(Page page, String email, String password) {
        page.navigate("https://www.siae.it/it/");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("ACCETTO")).click();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Accedi").setExact(true)).click();
        page.locator("input[type=\"text\"]").fill(email);
        page.locator("input[type=\"password\"]").fill(password);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Accedi")).click();
        page.locator("#APMO_STD")
                .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Accedi"))
                .click();
    }

    private void navigateToAssignSection(Page page) {
        page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Da assegnare")).click();
        Locator button5 = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("5"));
        button5.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(10000));
        button5.click();
        Locator option50 = page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName("50"));
        option50.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        option50.click();
    }

    private void navigateToGiveBackSection(Page page) {
        page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Da riconsegnare")).click();
        Locator button5 = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("5"));
        button5.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(10000));
        button5.click();
        Locator option50 = page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName("50"));
        option50.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        option50.click();
    }

    private void processAllPagesAssign(Page page) {
        Locator menu = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("1"));
        menu.click();
        page.waitForSelector("ul[role='listbox'] li[role='option']");
        Locator options = page.locator("ul[role='listbox'] li[role='option']");
        int optionCount = options.count();
        options.nth(0).click();  // remove focus

        for (int i = 0; i < optionCount; i++) {
            System.out.println("▶ Elaboro pagina: " + (i + 1) + "#################");
            System.out.println("\n");
            page.waitForSelector("table tbody tr");

            Locator rows = page.locator("table tbody tr");
            int rowCount = rows.count();

            for (int j = 0; j < rowCount; j++) {
                try {
                    processRowAssign(page, rows.nth(j), j+1);
                    saveCheckpoint(i, j + 1); // Salva dopo ogni riga completata
                } catch (Exception e) {
                    System.err.println("❌ Errore alla pagina " + (i+1) + ", riga " + (j+1));
                    saveCheckpoint(i, j); // Salva dove si è fermato
                    throw e; // facoltativo: puoi anche continuare
                }
            }

            if(i!=optionCount-1) {
                page.locator("span[title='Previous Page'] + span button").click();
                page.waitForTimeout(1000);
            }
        }
    }

    private void processRowAssign(Page page, Locator row, int index){
        System.out.println("Record numero " + index +": clic su 'assegna'");
        // Stampa contenuto effettivo della riga
        Locator cells = row.locator("td");
        int cellCount = cells.count();

        // Recupera Data e Locale/Spazio
        String data = cells.nth(1).innerText().trim();
        String localeSpazio = cells.nth(3).innerText().trim();

        StringBuilder rowContent = new StringBuilder("📄 Riga " + index + ": ");
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
        System.out.println(rowContent);

        Locator assegnaButton = row.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("assegna"));
        if (assegnaButton.count() > 0) {
            assegnaButton.first().click();
        }

        page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("E-mail")).click();
        System.out.println(data);
        System.out.println(localeSpazio);
        try {
            ExcelReader reader = new ExcelReader();
            reader.read(ExcelStorage.getInstance().getFile());
            String emailToFill = reader.getValueByTwoKeys("Data evento",data, "Nome location",localeSpazio,"E-mail");
            page.getByRole(AriaRole.TEXTBOX).fill(emailToFill);
            //page.waitForTimeout(10000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Cerca")).click();
        page.waitForTimeout(1000);

        Locator cell = page.getByRole(AriaRole.CELL, new Page.GetByRoleOptions().setName("rodella.et@gmail.com"));

        if (cell.count() > 0 && cell.first().isVisible()) {
            System.out.println("✔ Email trovata nella tabella");
            page.getByRole(AriaRole.RADIO).check();
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("PROGRAMMI MUSICALI")).click();
            // TODO: click su "Conferma"
        } else {
            System.out.println("❌ Email non trovata, inserimento manuale");
            page.getByText("Non hai trovato il direttore").click();
            page.getByRole(AriaRole.TEXTBOX).fill("asdsad@adas.it");
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Annulla")).click();
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("PROGRAMMI MUSICALI")).click();
            // TODO: click su "Conferma"
        }
    }

    private void processAllPagesGiveBack(Page page) {
        Locator menu = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("1"));
        menu.click();
        page.waitForSelector("ul[role='listbox'] li[role='option']");
        Locator options = page.locator("ul[role='listbox'] li[role='option']");
        int optionCount = options.count();
        options.nth(0).click();  // remove focus

        for (int i = 0; i < optionCount; i++) {
            System.out.println("▶ Elaboro pagina: " + (i + 1) + "#################");
            System.out.println("\n");
            page.waitForSelector("table tbody tr");

            Locator rows = page.locator("table tbody tr");
            int rowCount = rows.count();

            for (int j = 0; j < rowCount; j++) {
                try {
                    processRowGiveBack(page, rows.nth(j), j+1);
                    saveCheckpoint(i, j + 1); // Salva dopo ogni riga completata
                } catch (Exception e) {
                    System.err.println("❌ Errore alla pagina " + (i+1) + ", riga " + (j+1));
                    saveCheckpoint(i, j); // Salva dove si è fermato
                    throw e; // facoltativo: puoi anche continuare
                }
            }

            if(i!=optionCount-1) {
                page.locator("span[title='Previous Page'] + span button").click();
                page.waitForTimeout(1000);
            }
        }
    }

    private void processRowGiveBack(Page page, Locator row, int index) {
        System.out.println("Record numero " + index +": clic su 'assegna'");
        // Stampa contenuto effettivo della riga
        Locator cells = row.locator("td");
        int cellCount = cells.count();

        StringBuilder rowContent = new StringBuilder("📄 Riga " + index + ": ");
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
        System.out.println(rowContent);

        Locator assegnaButton = row.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("visualizza"));
        if (assegnaButton.count() > 0) {
            assegnaButton.first().click();
        }

        Locator noPageLocator = page.getByText("Si è verificato un errore",
                new Page.GetByTextOptions().setExact(false));

        // Aspetta fino a 2 secondi che l'elemento compaia (se non compare, count() rimane 0)
        try {
            noPageLocator.waitFor(new Locator.WaitForOptions().setTimeout(2000));
        } catch (PlaywrightException e) {
            System.out.println("timeout: l'elemento non è stato trovato entro 2 secondi");
        }

        if (noPageLocator.count() > 0) {
            System.out.println("Elemento ‘Si è verificato un errore’ trovato");
            page.goBack();
            page.waitForTimeout(1000);
            return;
        }

        // Clic su "Riconsegna a SIAE"
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Riconsegna a SIAE")).click();


        // Verifica se compare il messaggio "Il Programma che stai"
        Locator warningText = page.getByText("Il Programma che stai");

        if (warningText.isVisible()) {
            System.out.println("Caso Warning Verificato trovato");
            // Clic sul textbox
            page.getByRole(AriaRole.TEXTBOX).click();
            // Inserisce testo nel campo
            page.getByRole(AriaRole.TEXTBOX).fill("Riconsegna automatizzata");
            // Sostituisce la conferma con "Annulla"
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Annulla")).click();
            page.waitForTimeout(1000);
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("PROGRAMMI MUSICALI")).click();
            return;
        }

        // Seleziona radio "Programma artista principale"
        Locator radio = page.getByRole(AriaRole.RADIO, new Page.GetByRoleOptions().setName("Programma artista principale"));
        if (radio.count() > 0) {
            radio.check();
        }

        //TODO Clic su "Annulla" da sostituire con click su conferma
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Annulla")).click();

        // Clic su "PROGRAMMI MUSICALI"
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("PROGRAMMI MUSICALI")).click();
    }

    private static final String CHECKPOINT_PATH = "../Checkpoints/checkpoint.properties";

    private void saveCheckpoint(int page, int row) {
        try {
            Path checkpointFile = Paths.get(CHECKPOINT_PATH);
            Files.createDirectories(checkpointFile.getParent()); // crea cartella checkpoints/ se non esiste

            Properties props = new Properties();
            props.setProperty("page", String.valueOf(page));
            props.setProperty("row", String.valueOf(row));

            try (FileWriter writer = new FileWriter(checkpointFile.toFile())) {
                props.store(writer, "Checkpoint salvato nel progetto corrente");
                System.out.println("Checkpoint salvato in: " + checkpointFile.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Errore nel salvataggio del checkpoint:");
            e.printStackTrace();
        }
    }
    
    /**
     * Naviga al mese target nel calendario
     */
    private void navigateToTargetMonth(Page page, String targetMonth, int targetYear) {
        int maxAttempts = 12;
        
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            try {
                // Trova il mese corrente visualizzato
                String currentMonthText = getCurrentMonth(page);
                System.out.println("Mese corrente: " + currentMonthText);
                
                // Controlla se siamo già nel mese giusto
                if (currentMonthText.toLowerCase().contains(targetMonth.toLowerCase()) && 
                    currentMonthText.contains(String.valueOf(targetYear))) {
                    System.out.println("✅ Mese corretto raggiunto: " + currentMonthText);
                    return;
                }
                
                // Naviga al mese successivo
                clickNextMonth(page);
                page.waitForTimeout(500); // Piccola pausa per l'aggiornamento
                
            } catch (Exception e) {
                System.err.println("Errore durante navigazione mese: " + e.getMessage());
                break;
            }
        }
        
        throw new RuntimeException("❌ Non riesco a raggiungere il mese: " + targetMonth + " " + targetYear);
    }

    /**
     * Ottiene il mese attualmente visualizzato
     */
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
        
        throw new RuntimeException("❌ Impossibile trovare il mese corrente");
    }

    /**
     * Clicca sul bottone mese successivo
     */
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
                    System.out.println("➡️ Cliccato su mese successivo");
                    return;
                }
            } catch (Exception e) {
                // Continua con il prossimo selettore
            }
        }
        
        throw new RuntimeException("❌ Bottone 'mese successivo' non trovato");
    }

    /**
     * Seleziona il giorno specifico nel calendario
     */
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
                        System.out.println("✅ Giorno " + day + " selezionato (metodo 1)");
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
                    System.out.println("✅ Giorno " + day + " selezionato (metodo 2)");
                    return;
                }
            }
            
            // Approccio 3: Fallback con aria-label parziale
            Locator dayWithAriaLabel = page.locator(
                String.format("div[role='gridcell'][aria-label*='%d ']", day)
            );
            
            if (dayWithAriaLabel.count() > 0) {
                dayWithAriaLabel.first().click();
                System.out.println("✅ Giorno " + day + " selezionato (metodo 3)");
                return;
            }
            
        } catch (Exception e) {
            System.err.println("Errore nella selezione del giorno: " + e.getMessage());
        }
        
        throw new RuntimeException("❌ Impossibile selezionare il giorno " + day);
    }

}
