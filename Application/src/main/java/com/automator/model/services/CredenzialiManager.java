package com.automator.model.services;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class CredenzialiManager {
	
	private static final String FILE_PATH = "credenziali.json";
    private final List<Credenziale> lista = new ArrayList<>();

    public CredenzialiManager() { carica(); }

    public void aggiungi(String email, String password) {
        // evita duplicati
        boolean exists = lista.stream().anyMatch(c ->
                c.getEmail().equals(email) && c.getPassword().equals(password));
        if (!exists) {
            lista.add(new Credenziale(email, password));
            salva();
        }
    }

    public List<Credenziale> getAll() { return lista; }

    private void salva() {
        try (FileWriter fw = new FileWriter(FILE_PATH)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(lista, fw);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void carica() {
        File f = new File(FILE_PATH);
        if (!f.exists()) return;
        try (FileReader fr = new FileReader(f)) {
            Gson gson = new Gson();
            Credenziale[] arr = gson.fromJson(fr, Credenziale[].class);
            if (arr != null) lista.addAll(Arrays.asList(arr));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
