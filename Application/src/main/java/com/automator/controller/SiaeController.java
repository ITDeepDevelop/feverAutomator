package com.automator.controller;

import com.automator.model.SiaeAutomationService;

public class SiaeController {
    private final SiaeAutomationService siaeService;

    public SiaeController() {
        siaeService = new SiaeAutomationService();
    }

    public boolean handleOperation(String operationName) {
        switch (operationName) {
            case "Operazione 1":
                return siaeService.runOperation1();
            case "Operazione 2":
                return siaeService.runOperation2();
            case "Operazione 3":
                return siaeService.runOperation3();
            case "Operazione 4":
                return siaeService.runOperation4();
            default:
                return false;
        }
    }
} 