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


public class SiaeAutomationService {
	
	
	 public boolean nuoviPermessi(String email, String password) {
		
		 ExcelReader reader = new ExcelReader();
		 
		 try {
			reader.read(ExcelStorage.getInstance().getFile());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 System.out.print("TEST");
		 List<EventoRow> eventiOriginali = reader.getEventoRows();
		 List<EventoRow> eventlist = EventoRow.espandiEventiConSessioni(eventiOriginali);
	        Playwright playwright = Playwright.create();
	            Browser browser = launchBrowser(playwright);
	            BrowserContext context = browser.newContext();
	            Page page = context.newPage();

	      
	                page.navigate("https://www.siae.it/it/");
	                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("ACCETTO")).click();
	                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Accedi").setExact(true)).click();
	                page.locator("input[type=\"text\"]").fill(email);
	                page.locator("input[type=\"password\"]").fill(password);
	                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Accedi")).click();
	               
	           
	            
	                // 2 "Portale Organizzatori Professionali > Accedi"
	                page.locator("#PORTUP_STD")
	                        .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Accedi"))
	                        .click();
	             // 3. Nuovo permesso
	                page.waitForSelector("text=Nuovo Permesso");
	                page.getByText("Nuovo Permesso").click();
	                int j = 0;
	                
	              for(EventoRow evento: eventlist ) {
	            	  if (evento.getNomeLocation().isEmpty()) {
	            		  break;
	            	  }
	            // 4. Inserimento dati statici (poi da Excel)
	             // Inserisci città
	            	page.pause();

		       		 System.out.print(evento);
	       		 System.out.print(evento.getCitta());
	       		 System.out.print(evento.getNomeLocation());
	            	page.waitForTimeout(3000);
	            	
	            	String cittaTarget = evento.getCitta().toUpperCase();  // "Como" -> "COMO"

	            	// Focus campo città
	            	page.locator("input[placeholder='Città']").click();
	            	page.locator("input[placeholder='Città']").fill("");  // pulizia
	            	page.locator("input[placeholder='Città']").type(cittaTarget, new Locator.TypeOptions().setDelay(100));

	            	// Attendi caricamento lista
	            	page.waitForSelector("ul[role='listbox'] li");

	            	// Ottieni tutte le opzioni del dropdown
	            	Locator opzioni = page.locator("ul[role='listbox'] li");
	            	int count = opzioni.count();

	            	System.out.println("📋 Lista città disponibili:");
	            	boolean found = false;

	            	for (int i = 0; i < count; i++) {
	            	    Locator li = opzioni.nth(i);
	            	    String testoComplessivo = li.textContent().trim();

	            	    System.out.println(" - Voce [" + i + "]: " + testoComplessivo);

	            	    // Se la voce inizia esattamente con "COMO" (senza considerare spazi) la clicchiamo
	            	    if (testoComplessivo.replaceAll("\\s+", "").startsWith(cittaTarget)) {
	            	        li.click();
	            	        System.out.println("✅ Città trovata e cliccata: " + testoComplessivo);
	            	        found = true;
	            	        break;
	            	    }
	            	}

	            	if (!found) {
	            	    System.err.println("❌ Nessuna voce trovata per la città: " + cittaTarget);
	            	    page.pause();
	            	}

	                // Inserisci locale
	            	// Step 1: Inserisci solo il nome del locale
	            	String nomeLocation = evento.getNomeLocation(); // es: "Sociale"
	            	String via = evento.getIndirizzo().split(",")[0].trim(); // es: "Via Vincenzo Bellini"

	            	page.locator("input[placeholder='Locale / Indirizzo']").click();
	            	page.locator("input[placeholder='Locale / Indirizzo']").fill(nomeLocation);

	            	// Step 2: Aspetta la lista delle opzioni
	            	page.waitForSelector("ul[role='listbox'] li");

	            	// Step 3: Cerca nella lista quella che contiene anche la via
	            	Locator opzioni1 = page.locator("ul[role='listbox'] li");
	            	int count1 = opzioni1.count();
	            	boolean found1 = false;

	            	System.out.println("🔍 Cerco opzione contenente via: " + via);

	            	for (int i = 0; i < count1; i++) {
	            	    String testo = opzioni1.nth(i).textContent().trim();
	            	    System.out.println(" - [" + i + "] " + testo);

	            	    if (testo.toLowerCase().contains(via.toLowerCase())) {
	            	        opzioni1.nth(i).click();
	            	        found1= true;
	            	        System.out.println("✅ Locale selezionato: " + testo);
	            	        break;
	            	    }
	            	}

	            	if (!found1) {
	            	    System.err.println("❌ Nessun locale trovato con via: " + via);
	            	    page.pause(); // debug visivo se necessario
	            	}

	                page.pause();
	             // Clicca sul dropdown "Spazio/Sala"
	                try {
	                    Locator dropdownSala = page.locator("div[role='button']:has-text('Seleziona lo spazio o la sala del tuo evento')");
	                    dropdownSala.click();
	                    page.waitForSelector("ul[role='listbox']");

	                    Locator salaDesiderata = page.locator("li:has-text('" + evento.getSala() + "')");
	                    if (salaDesiderata.count() > 0) {
	                        salaDesiderata.click();
	                        System.out.println("🏛️ Sala selezionata: " + evento.getSala());
	                    } else {
	                        System.out.println("⚠️ Sala '" + evento.getSala() + "' non disponibile. Uso quella predefinita.");
	                        // Puoi anche non fare nulla qui se "spazio predefinito" va bene
	                    }
	                } catch (Exception e) {
	                    System.out.println("⚠️ Errore durante la selezione della sala. Uso quella predefinita.");
	                }
	                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Procedi")).click();
	                
	             // 5 Seleziona Categoria Evento
	                String codice = evento.getCodiceSpettacoloGenere();
	                String categoriaEvento = CategoriaEventoMapper.getCategoria(codice);
	                String genereEvento = CategoriaEventoMapper.getGenere(codice);
	                
	             // Apri la prima tendina (Categoria)
	                page.locator("div[role='button']:not(.Mui-disabled)").nth(0).click();
	                page.waitForSelector("ul[role='listbox']");

	                // Clicca sull'opzione della categoria mappata
	                page.locator("li:has-text('" + categoriaEvento + "')").click();

	                // Apri la seconda tendina (Genere)
	                page.locator("div[role='button']:not(.Mui-disabled)").nth(1).click();
	                page.waitForSelector("ul[role='listbox']");

	                // Clicca sull'opzione del genere mappato
	                page.locator("li:has-text('" + genereEvento + "')").click();

	                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Procedi")).click();
	                
	           
	             // 6 Data evento   
	                try {
	                    String dataEvento = evento.getDataEvento(); // Es. "2025-06-25"
	                    
	                    System.out.println("📅 Data letta da Excel: " + dataEvento); // 🔍 DEBUG

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

	                    System.out.println("✅ Data selezionata con successo");
	                } catch (Exception e) {
	                    System.err.println("❌ Errore nella selezione della data: " + e.getMessage());
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
	                    page.waitForSelector("ul[role='listbox']");
	                    page.locator("li:has-text('Ingresso a pagamento')").click();

	                } catch (Exception e) {
	                    System.err.println("❌ Errore nella gestione delle sessioni orarie: " + e.getMessage());
	                    e.printStackTrace();
	                }

	           
	       
	                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Procedi")).click();
	                
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
	                    Path cartellaPDF = Paths.get(userHome, "Desktop", "LeaDownloads");
	                    Path pathPDF = cartellaPDF.resolve(nomeFilePDF);
	                    
	                    if (!Files.exists(pathPDF)) {
	                        String errorMsg = "❌ File PDF non trovato: " + pathPDF.toString();
	                        System.err.println(errorMsg);
	                        throw new RuntimeException(errorMsg);
	                    } else {
	                        System.out.println("📎 Carico PDF: " + pathPDF.toString());
	                        page.setInputFiles("input[type='file']", pathPDF);
	                        page.waitForSelector("text=" + nomeFilePDF);
	                        System.out.println("✅ PDF caricato con successo");
	                    }
	                } catch (Exception e) {
	                    System.err.println("❌ Errore nel caricamento del PDF: " + e.getMessage());
	                }
	             
	                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Procedi")).click();
	          
	                
	                Locator dropdown = page.locator("div#outlined-select");
	                dropdown.click(new Locator.ClickOptions().setForce(true));

	                // Aspetta che l'opzione "No" sia visibile
	                page.waitForSelector("ul[role='listbox'] li[role='option']");

	                // Clicca su "No" in modo preciso usando getByRole con setExact
	                page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions()
	                    .setName("No")
	                    .setExact(true)
	                ).click();
	             page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Procedi")).click();
	             
	                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Procedi")).click();
	                try {
	                	// Primo INVIA (pagina principale)
	                    page.waitForSelector("button[type='submit']", new Page.WaitForSelectorOptions().setTimeout(10000));
	                    page.locator("button[type='submit']").click();

	                    // Secondo INVIA (nel popup di conferma)
	                    page.waitForSelector("div[role='dialog'] button:has-text('Invia')", new Page.WaitForSelectorOptions().setTimeout(5000));
	                    page.locator("div[role='dialog'] button:has-text('Invia')").click();

	                    // Attendi popup "La richiesta è stata inviata con successo"
	                    page.waitForSelector("text=La richiesta è stata inviata con successo", new Page.WaitForSelectorOptions().setTimeout(10000));
	                    page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("OK")).click();

	                    // Attendi la schermata successiva con "Inviata"
	                    page.waitForSelector("text=Inviata", new Page.WaitForSelectorOptions().setTimeout(10000));

	                    // Torna alla home del portale organizzatori
	                    page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Portale Organizzatori Professionali")).click();
	                    page.waitForTimeout(1000);

	                    // Clicca nuovamente su "Nuovo Permesso"
	                    page.waitForSelector("text=Nuovo Permesso", new Page.WaitForSelectorOptions().setTimeout(5000));
	                    page.getByText("Nuovo Permesso").click();

	                    System.out.println("✅ Evento inviato con successo. Passo al successivo...");
	                } catch (Exception e) {
	                    System.err.println("❌ Errore dopo invio permesso: " + e.getMessage());
	                    
	                }
					System.out.println("▶ Elaborazione evento " + (j + 1) + " di " + eventlist.size());
	              }
	            page.close();
	            browser.close();
	            return true;
	      
	    }

   // }
    /*
     * 03/06/25 --> gestita la presenza di tabella con più pagine + *30/05/25
     * */
    public boolean licenseCheck(String email, String password) {

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
                System.out.println("Trovati " + currentButtonCount + " bottoni 'Visualizza' rimanenti (totale processati: " + totalProcessed + ")");
                
                if (currentButtonCount > 0) {
                    // Prendi sempre il PRIMO bottone disponibile
                    Locator firstButton = visualizzaButtons.first();
                    
                    try {
                        System.out.println("Processando bottone " + (totalProcessed + 1) + " (primo disponibile)");
                        
                        // Scroll al bottone e clicca
                        firstButton.scrollIntoViewIfNeeded();
                        firstButton.waitFor(new Locator.WaitForOptions()
                            .setState(WaitForSelectorState.VISIBLE)
                            .setTimeout(5000));
                        
                        System.out.println("Cliccando su 'Visualizza'...");
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
                        System.out.println("Bottone processato con successo (totale processati: " + totalProcessed + ")");
                        
                    } catch (Exception e) {
                        System.err.println("Errore nel processare il bottone " + (totalProcessed + 1) + ": " + e.getMessage());
                        
                        // In caso di errore, prova comunque a tornare alla tabella
                        try {
                            page.goBack();
                            page.waitForLoadState(LoadState.NETWORKIDLE);
                            page.waitForTimeout(2000);
                            
                            // Riclicca su "Da Accettare" per essere sicuri di essere nella sezione corretta
                            Locator daAccettareTab = page.locator("button[role='tab']:has-text('Da Accettare')").first();
                            if (daAccettareTab.count() > 0) {
                                daAccettareTab.click();
                                page.waitForTimeout(2000);
                            }
                            
                        } catch (Exception backError) {
                            System.err.println("Errore nel tornare indietro: " + backError.getMessage());
                        }
                        
                        totalProcessed++; // Continua con il prossimo anche se questo ha fallito
                    }
                } else {
                    System.out.println("Nessun bottone 'Visualizza' rimanente nella pagina corrente");
                    foundMoreButtons = false;
                }
                
                // Sicurezza: evita loop infiniti
                if (totalProcessed > 50) {
                    System.out.println("Raggiunto limite massimo di permessi per pagina (50)");
                    break;
                }
            }
            
            System.out.println("Completato processamento di tutti i bottoni nella pagina corrente. Totale processati: " + totalProcessed);
            
        } catch (Exception e) {
            System.err.println("Errore durante il processamento dei permessi: " + e.getMessage());
            e.printStackTrace();
        }
    }

 // Metodo per processare la pagina dei dettagli del permesso e accettarlo
    private void processPermissionDetailsPage(Page page) {
        try {
            System.out.println("Processando pagina dettagli permesso...");
            
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
                System.out.println("Trovato bottone 'Accetta Permesso', cliccando...");
                
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
                    System.out.println("Trovato bottone 'Conferma' nella finestra di dialogo, cliccando...");
                    
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
                        System.out.println("Trovato bottone 'Ok' finale, cliccando...");
                        
                        okButton.first().waitFor(new Locator.WaitForOptions()
                            .setState(WaitForSelectorState.VISIBLE)
                            .setTimeout(5000));
                        
                        okButton.first().click();
                        
                        // Attendi che il browser torni alla tabella automaticamente
                       /* page.waitForTimeout(2000);
                        page.waitForLoadState(LoadState.NETWORKIDLE);
                        page.waitForTimeout(1000);*/
                        
                        System.out.println("Permesso accettato con successo!");

                        Locator daAccettareTab = page.locator("button[role='tab']:has-text('Da Accettare')").first();
                        daAccettareTab.waitFor(new Locator.WaitForOptions()
                            .setState(WaitForSelectorState.VISIBLE)
                            .setTimeout(10000));
                        daAccettareTab.click();
                        System.out.println("Click eseguito su 'Da Accettare'post accettazione permesso");
                        // page.goBack();
                        
                    } else {
                        System.out.println("Bottone 'Ok' finale non trovato");
                        // Attendi comunque un po' per eventuali redirect automatici
                        page.waitForLoadState(LoadState.NETWORKIDLE);
                        page.waitForTimeout(1000);
                    }
                    
                } else {
                    System.out.println("Bottone 'Conferma' non trovato nella finestra di dialogo");
                }
                
            } else {
                System.out.println("Bottone 'Accetta Permesso' non trovato nella pagina");
            }
            
        } catch (Exception e) {
            System.err.println("Errore durante l'accettazione del permesso: " + e.getMessage());
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
                System.out.println("⚠️ Pulsante '5' non trovato o non visibile entro il timeout, proseguo senza selezionare righe per pagina.");
            }
        } else {
            System.out.println("⚠️ Pulsante '5' non presente, proseguo.");
        }
    }

    private void navigateToGiveBackSection(Page page) {
        page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Da riconsegnare")).click();

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
                System.out.println("⚠️ Pulsante '5' non trovato o non visibile entro il timeout, proseguo senza selezionare righe per pagina.");
            }
        } else {
            System.out.println("⚠️ Pulsante '5' non presente, proseguo.");
        }
    }


    private void processAllPagesAssign(Page page) {
        Locator menu = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("1"));
        int optionCount = 0;
        if (menu.count() > 0) {
            try {
                menu.waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(3000)
                );
                menu.click();
                page.waitForSelector("ul[role='listbox'] li[role='option']");
                Locator options = page.locator("ul[role='listbox'] li[role='option']");
                optionCount = options.count();
                options.nth(0).click();  // remove focus
            } catch (PlaywrightException e) {
                System.out.println("⚠️ Menu '1' non trovato o non visibile entro 3 secondi. Processerò come fallback 5 righe.");
                optionCount = 5;
            }
        } else {
            System.out.println("⚠️ Menu '1' non presente. Processerò come fallback 5 righe.");
            optionCount = 5;
        }

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
        String emailToFill = "";
        try {
            ExcelReader reader = new ExcelReader();
            reader.read(ExcelStorage.getInstance().getFile());
            emailToFill = reader.getValueByTwoKeys("Data evento",data, "Nome location",localeSpazio,"E-mail");
            if(emailToFill.isEmpty()){
                System.out.println("Email non presente nel file excel");
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("PROGRAMMI MUSICALI")).click();
                return;
            }
            page.getByRole(AriaRole.TEXTBOX).fill(emailToFill);
        } catch (Exception e) {
            e.printStackTrace();
        }
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Cerca")).click();
        page.waitForTimeout(1000);

        Locator cell = page.getByRole(AriaRole.CELL, new Page.GetByRoleOptions().setName(emailToFill));

        if (cell.count() > 0 && cell.first().isVisible()) {
            System.out.println("✔ Email trovata nella tabella");
            page.getByRole(AriaRole.RADIO).check();
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Conferma")).click();
        } else {
            System.out.println("❌ Email non trovata, inserimento manuale");
            page.getByText("Non hai trovato il direttore").click();
            page.getByRole(AriaRole.TEXTBOX).fill(emailToFill);
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Conferma")).click();
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
                    System.err.println("❌ Errore alla pagina 0, riga " + (j+1));
                    saveCheckpoint(0, j); // Salva dove si è fermato
                    throw e; // facoltativo: puoi anche continuare
                }
            }
        }

    private boolean processRowGiveBack(Page page) {

        page.waitForTimeout(3000);
        System.out.println("Ho aspettato");
        Locator assegnaButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions()
                        .setName("Visualizza")
                        .setExact(false)
        );

        if (assegnaButton.count() > 0) {
            System.out.println("✅ Trovato un visualizza");
            assegnaButton.first().click();
        } else {
            System.out.println("✅ Dati finiti: nessun bottone 'visualizza' trovato nella riga.");
            return false;
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
            return true;
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
            // Conferma effettiva
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Conferma")).click();
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
            System.out.println("Elemento 'Vuoi riconsegnare il PM a' non trovato o non cliccabile, si prosegue.");
        }

        try {
            // Attende che il campo sia visibile entro 5 secondi (5000 ms)
            Locator motivoCancellazione = page.locator("#filled-input-cancellation-reason");
            motivoCancellazione.waitFor(new Locator.WaitForOptions().setTimeout(5000));
            motivoCancellazione.fill("Riconsegna automatizzata");
        } catch (TimeoutError e) {
            // L'elemento non è stato trovato entro 5 secondi, prosegue senza fare nulla
            System.out.println("Campo motivo cancellazione non trovato, si prosegue.");
        }


        // Seleziona radio "Programma artista principale"
        Locator radio = page.getByRole(AriaRole.RADIO, new Page.GetByRoleOptions().setName("Programma artista principale"));
        if (radio.count() > 0) {
            radio.check();
        }

        //Conferma effettiva
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Conferma")).click();
        return true;
    }

    private static final String CHECKPOINT_PATH = "../../Checkpoints/checkpoint.properties";

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
    
    public static class CategoriaEventoMapper {
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

    	    if (lower.contains("sport") || isGenereSportivo(lower)) {
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

        private static  boolean isGenereSportivo(String lower) {
            return lower.contains("atletica") || lower.contains("automobilismo") || lower.contains("baseball") ||
                   lower.contains("basket") || lower.contains("calcio") || lower.contains("ciclismo") ||
                   lower.contains("ippici") || lower.contains("cavalli") || lower.contains("motociclismo") ||
                   lower.contains("motonautica") || lower.contains("nuoto") || lower.contains("pallavolo") ||
                   lower.contains("pugilato") || lower.contains("rugby") || lower.contains("invernali") ||
                   lower.contains("tennis") || lower.contains("sport");
        }
    }

}
