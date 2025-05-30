package com.automator.model.services;

import com.microsoft.playwright.*;

import com.microsoft.playwright.options.WaitForSelectorState;

import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.io.*;


public class SiaeAutomationService {

    public boolean runOperation1() {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = launchBrowser(playwright);
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
            permessiTable(page);

            page.waitForTimeout(5000);
            page.close();
            browser.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    	
    }
    
    /*
     * 30/05/2025 --> manca solo l'implementazione relativa all click su "accetta permesso" ed alla conferma 
     * 					di accettazione sul modale successivo
     * 					
     * */
    private void permessiTable(Page page) {
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
    }
    

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
                    System.err.println("❌ Errore alla pagina " + i + ", riga " + j);
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

    private void processRowAssign(Page page, Locator row, int index) {
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

        Locator assegnaButton = row.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("assegna"));
        if (assegnaButton.count() > 0) {
            assegnaButton.first().click();
        }

        page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("E-mail")).click();
        page.getByRole(AriaRole.TEXTBOX).fill("rodella.et@gmail.com"); // ← da DB in futuro
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
                    System.err.println("❌ Errore alla pagina " + i + ", riga " + j);
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

        // Clic su "Riconsegna a SIAE"
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Riconsegna a SIAE")).click();

        // Verifica se compare il messaggio "Il Programma che stai"
        Locator warningText = page.getByText("Il Programma che stai");

        if (warningText.isVisible()) {
            // Clic sul textbox
            page.getByRole(AriaRole.TEXTBOX).click();
            // Inserisce testo nel campo
            page.getByRole(AriaRole.TEXTBOX).fill("Riconsegna automatizzata");
            // Sostituisce la conferma con "Annulla"
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Annulla")).click();
            page.waitForTimeout(1000);
            return;
        }

        // Seleziona radio "Programma artista principale"
        page.getByRole(AriaRole.RADIO, new Page.GetByRoleOptions().setName("Programma artista principale")).check();

        //TODO Clic su "Annulla" da sostituire con click su conferma
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Annulla")).click();

        // Clic su "PROGRAMMI MUSICALI"
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("PROGRAMMI MUSICALI")).click();
        page.waitForTimeout(1000);
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

}
