package com.automator.model.services;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.SelectOption;

public class SiaeAutomationService {

    public boolean runOperation1() {
        // logica vera dell'operazione 1
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            BrowserContext context = browser.newContext();
            Page page = context.newPage();

      
                page.navigate("https://www.siae.it/it/");
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("ACCETTO")).click();
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Accedi").setExact(true)).click();
                page.locator("input[type=\"text\"]").fill("originals_italy@feverup.com");
                page.locator("input[type=\"password\"]").fill("Siae@123");
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Accedi")).click();
               
           
            // 3. Nuovo permesso
            page.click("text=Nuovo permesso");
            page.waitForTimeout(1000);

            // 4. Inserimento dati statici (poi da Excel)
            page.fill("input[name='citta']", "Milano");
            page.fill("input[name='location']", "Teatro Verdi");

            if (page.isVisible("select[name='spazio']")) {
                page.selectOption("select[name='spazio']", new SelectOption().setLabel("Sala A"));
            }
            page.click("text=Procedi");

            // 5. Categoria e genere
            page.selectOption("select[name='categoria']", new SelectOption().setLabel("Concerti, Manifestazioni Musicali"));
            page.selectOption("select[name='genere']", "53"); // Leggera
            page.fill("input[name='percentuale']", "100");
            page.click("text=Procedi");

            // 6. Sessioni (esempio con due fasce)
            page.click("text=Data singola");
            page.fill("input[name='data']", "2025-06-20");
            page.fill("input[name='oraInizio']", "21:00");
            page.fill("input[name='oraFine']", "22:00");
            page.click("text=Aggiungi");

            page.fill("input[name='oraInizio']", "23:00");
            page.fill("input[name='oraFine']", "24:00");
            page.click("text=Aggiungi");
            page.click("text=Procedi");

            /* 7. PDF Licenza
            page.check("input[name='possiedeLicenza']");
            page.setInputFiles("input[type='file']", Paths.get("src/main/resources/licenza.pdf"));
            page.selectOption("select[name='musicaPubblicoDominio']", "NO");
            page.click("text=Procedi");
*/
            // 8. Conferma
            //page.click("text=Invia");


            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean runOperation2() {
        System.out.println("Esecuzione Operazione 2");
        return Math.random() < 0.5;
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
