package com.automator.model.services;
import com.microsoft.playwright.*;

public class LeaAutomationService {

    public boolean op1() {
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

    public boolean op2() {
        System.out.println("Esecuzione Operazione 2");
        return Math.random() < 0.5;
    }

    public boolean op3() {
        System.out.println("Esecuzione Operazione 3");
        return true;
    }

    public boolean op4() {
        System.out.println("Esecuzione Operazione 4");
        return false;
    }
}
