package com.automator.controller;

import com.automator.model.services.LeaAutomationService;

public class LeaController {
    private final LeaAutomationService leaService = new LeaAutomationService();

    public boolean handleOperation(String operationName, String email, String password, String month, String year) {
        switch (operationName) {
            case "Conferma Evento": return leaService.confermaLicenze(email, password);
            case "Download Licenza": return leaService.downloadLicenze(email, password, month, year);

            default: {
                System.out.println("Invalid operation name: " + operationName);
                return false;
            }
        }
    }
} 