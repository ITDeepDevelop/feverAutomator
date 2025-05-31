package com.automator.model.services;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;

import com.microsoft.playwright.options.SelectOption;

import com.microsoft.playwright.options.WaitForSelectorState;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.io.*;

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
                    
                    navigateToTargetMonth(page, "Maggio", 2025);
                    
                    selectDay(page, 31);
                    
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
                page.pause();

            page.close();
            browser.close();
            return true;
        /*} catch (Exception e) {
            e.printStackTrace();
            return false;
        }*/
    }

    public boolean runOperation2() {
        System.out.println("Esecuzione Operazione 2");
        return Math.random() < 0.5;
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
