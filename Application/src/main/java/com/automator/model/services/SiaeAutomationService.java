package com.automator.model.services;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;


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
        System.out.println("Esecuzione Operazione 2");
        return Math.random() < 0.5;
    }

    public boolean assignBordero(String email, String password) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            // Vai al sito
            page.navigate("https://www.siae.it/it/");

            // Accetta cookie
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("ACCETTO")).click();

            // Clic su Accedi
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Accedi").setExact(true)).click();

            // Email
            page.locator("input[type=\"text\"]").click();
            page.locator("input[type=\"text\"]").fill("originals_italy@feverup.com");

            // Password
            page.locator("input[type=\"password\"]").click();
            page.locator("input[type=\"password\"]").fill("Siae@123");

            // Login
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Accedi")).click();

            // Accesso con Assegna Bordero
            page.locator("#APMO_STD")
                    .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Accedi"))
                    .click();

            // Click su “Da assegnare”
            page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Da assegnare")).click();

            // Cambia pagina: seleziona 50 righe visibili
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("5")).click();
            page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName("50")).click();

            // Aspetta che la tabella sia visibile
            page.waitForSelector("table tbody tr");

            Locator rows = page.locator("table tbody tr");
            int rowCount = rows.count();

            // Itera sulle prime 10 righe
            for (int i = 0; i < Math.min(rowCount, 10); i++) {
                Locator row = rows.nth(i);
                System.out.println("▶ Riga " + i + ": clic su 'assegna'");

                Locator assegnaButton = row.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("assegna"));
                if (assegnaButton.count() > 0) {
                    assegnaButton.first().click();
                }

                // Tab E-mail
                page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("E-mail")).click();

                // Casella email
                page.getByRole(AriaRole.TEXTBOX).click();
                page.getByRole(AriaRole.TEXTBOX).fill("rodella.et@gmail.com"); // ← da DB in futuro

                // Cerca
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Cerca")).click();

                page.waitForTimeout(1000); // per sicurezza

                Locator cell = page.getByRole(AriaRole.CELL, new Page.GetByRoleOptions().setName("rodella.et@gmail.com"));

                if (cell.count() > 0 && cell.first().isVisible()) {
                    System.out.println("✔ Email trovata nella tabella");
                    page.getByRole(AriaRole.RADIO).check();
                    page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("PROGRAMMI MUSICALI")).click();
                    // TODO: click su "Conferma"
                } else {
                    System.out.println("❌ Email non trovata, inserimento manuale");
                    page.getByText("Non hai trovato il direttore").click();
                    page.getByRole(AriaRole.TEXTBOX).click();
                    page.getByRole(AriaRole.TEXTBOX).fill("asdsad@adas.it");
                    // ⬇️ Torna alla tabella principale
                    page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Annulla")).click();
                    page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("PROGRAMMI MUSICALI")).click();
                    // TODO: click su "Conferma"
                }

                // Attendi un attimo prima della prossima riga
                page.waitForTimeout(500);
            }

            // Fine
            page.waitForTimeout(3000);
            page.close();
            browser.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean runOperation4() {
        System.out.println("Esecuzione Operazione 4");
        return false;
    }
}
