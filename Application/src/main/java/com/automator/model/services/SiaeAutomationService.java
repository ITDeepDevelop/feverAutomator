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
            page.locator("#APMO_STD").getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Accedi")).click();

            // Click su “Da assegnare”
            page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Da assegnare")).click();

            // Click su primo bottone "assegna"
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("assegna")).first().click();

            // Tab E-mail
            page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("E-mail")).click();

            // Textbox email
            page.getByRole(AriaRole.TEXTBOX).click();
            page.getByRole(AriaRole.TEXTBOX).fill("rodella.et@gmail.com");

            // Cerca
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Cerca")).click();

            // Se esiste risultato
            Locator cell = page.getByRole(AriaRole.CELL, new Page.GetByRoleOptions().setName("rodella.et@gmail.com"));
            page.waitForTimeout(2000);

            if (cell.isVisible()) {
                page.getByRole(AriaRole.RADIO).check();
                // TODO: click su conferma
            } else {
                // Altrimenti: direttore non trovato
                page.getByText("Non hai trovato il direttore").click();
                page.getByRole(AriaRole.TEXTBOX).click();
                page.getByRole(AriaRole.TEXTBOX).fill("asdsad@adas.it");
                // TODO: click su conferma
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
