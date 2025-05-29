package com.automator.controller;

import com.automator.model.services.SiaeAutomationService;

public class SiaeController {
    private final SiaeAutomationService siaeService;

    public SiaeController() {
        siaeService = new SiaeAutomationService();
    }

    public boolean handleOperation(String operationName, String email, String password) {
        switch (operationName) {
            case "Operazione 1":
                return siaeService.runOperation1();
            case "Operazione 2":
                return siaeService.runOperation2();
            case "Assegna Bordero":
                return siaeService.assignBordero(email,password);
            case "Riconsegna Bordero":
                return siaeService.givebackBordero(email,password);
            default:
                return false;
        }
    }
} 