package com.automator.model.services;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

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

    public boolean runOperation2() {
        System.out.println("Esecuzione Operazione 2");
        return Math.random() < 0.5;
    }

    public boolean runOperation4() {
        System.out.println("Esecuzione Operazione 4");
        return false;
    }

    public boolean assignBordero(String email, String password) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = launchBrowser(playwright);
            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            loginToSiae(page, email, password);
            navigateToAssignSection(page);
            processAllPages(page);

            page.waitForTimeout(3000);
            page.close();
            browser.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private Browser launchBrowser(Playwright playwright) {
        return playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
    }

    private void loginToSiae(Page page, String email, String password) {
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
        page.waitForTimeout(500);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("5")).click();
        Locator option50 = page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName("50"));
        option50.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        option50.click();
    }

    private void processAllPages(Page page) {
        Locator menu = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("1"));
        menu.click();
        page.waitForSelector("ul[role='listbox'] li[role='option']");
        Locator options = page.locator("ul[role='listbox'] li[role='option']");
        int optionCount = options.count();
        options.nth(0).click();  // remove focus

        for (int i = 0; i < optionCount; i++) {
            System.out.println("▶ Elaboro pagina/opzione: " + (i + 1));
            page.waitForSelector("table tbody tr");

            Locator rows = page.locator("table tbody tr");
            int rowCount = rows.count();

            for (int j = 0; j < rowCount; j++) {
                processRow(page, rows.nth(j));
            }

            menu = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Integer.toString(i)));
            menu.click();
            page.waitForSelector("ul[role='listbox'] li[role='option']");
            page.locator("ul[role='listbox'] li[role='option']").nth(i).click();
            page.waitForTimeout(500);
        }
    }

    private void processRow(Page page, Locator row) {
        System.out.println("Riga: clic su 'assegna'");
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
}
