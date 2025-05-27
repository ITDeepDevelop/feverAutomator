package com.automator.model.services;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitForSelectorState;

public class SiaeAutomationService {

    public boolean runOperation1() {
        // logica vera dell'operazione 1
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
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

    public boolean runOperation2() {
    	/*
    	 * attualmente, per facilità di test,le credenziali verrano salvate "in chiaro",
    	 * successivamente verrano prese da input nella UI 
    	 * */
    	String email = "";   
    	String password = "";     

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

            page.waitForTimeout(5000);

            page.close();
            browser.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    	
    }

    public boolean runOperation3() {
        System.out.println("Esecuzione Operazione 3");
        return true;
    }

    public boolean runOperation4() {
        System.out.println("Esecuzione Operazione 4");
        return false;
    }
}
