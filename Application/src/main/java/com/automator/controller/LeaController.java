package com.automator.controller;

import com.automator.model.services.LeaAutomationService;

public class LeaController {
    private final LeaAutomationService leaService = new LeaAutomationService();

    public boolean handleOperation(String operationName) {
        switch (operationName) {
            case "Op1": return leaService.op1();
            case "Op2": return leaService.op2();
            case "Op3": return leaService.op3();
            case "Op4": return leaService.op4();
            default: {
                System.out.println("Invalid operation name: " + operationName);
                return false;
            }
        }
    }
} 