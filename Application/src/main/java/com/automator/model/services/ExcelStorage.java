package com.automator.model.services;

import java.io.File;

public class ExcelStorage {
    private static final ExcelStorage instance = new ExcelStorage();
    private File file;

    private ExcelStorage() {}

    public static ExcelStorage getInstance() {
        return instance;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }
}

