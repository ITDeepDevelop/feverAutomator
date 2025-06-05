package com.automator.controller;

import com.automator.model.services.SiaeAutomationService;

public class SiaeController {
    private final SiaeAutomationService siaeService;

    public SiaeController() {
        siaeService = new SiaeAutomationService();
    }

    public boolean handleOperation(String operationName, String email, String password) {
        switch (operationName) {
            case "Richiesta permesso per un evento":
                return siaeService.runOperation1();
            case "Accettazione Permessi":
                return siaeService.licenseCheck(email,password);
            case "Assegna Bordero":
                return siaeService.assignBordero(email,password);
            case "Riconsegna Bordero":
                return siaeService.givebackBordero(email,password);
            default:
                return false;
        }
    }
} 