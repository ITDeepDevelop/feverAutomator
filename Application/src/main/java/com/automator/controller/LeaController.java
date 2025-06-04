package com.automator.controller;

import com.automator.model.services.LeaAutomationService;

public class LeaController {
    private final LeaAutomationService leaService = new LeaAutomationService();

    public boolean handleOperation(String operationName, String email, String password) {
        switch (operationName) {
            case "Op1": return leaService.op1();
            case "Download Licenza": return leaService.downloadLicense( email, password);
            default: {
                System.out.println("Invalid operation name: " + operationName);
                return false;
            }
        }
    }
} 