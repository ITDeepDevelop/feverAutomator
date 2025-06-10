package com.automator.model.services;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Genera getter, setter
@NoArgsConstructor // Costruttore vuoto
@AllArgsConstructor // Costruttore con tutti i campi
public class EventoRow {
    private String nomeEventoELocation;
    private String codiceSpettacoloGenere;
    private String dataEvento;
    private String sessioni;
    private String nomeLocation;
    private String indirizzo;
    private String citta;
    private String cap;
    private String capienza;
    private String email;
    public void stampaDettagli() {
        System.out.println("----- Dettagli Evento -----");
        System.out.println("Nome evento + Location: " + nomeEventoELocation);
        System.out.println("Codice spettacolo / Genere: " + codiceSpettacoloGenere);
        System.out.println("Data evento: " + dataEvento);
        System.out.println("Sessioni: " + sessioni);
        System.out.println("Nome location: " + nomeLocation);
        System.out.println("Indirizzo: " + indirizzo);
        System.out.println("Città: " + citta);
        System.out.println("Cap " + cap);
        System.out.println("Capienza " + capienza);
        System.out.println("Email: " + email);
        System.out.println("----------------------------\n");
    }

    public static String toPDFName(String nomeEventoLocation, String data, String sessione) {
        if (nomeEventoLocation == null) nomeEventoLocation = "";
        if (data == null) data = "";
        if (sessione == null) sessione = "";

        // 1) rimuove spazi, '/', '\', ':', '-'
        String cleanEvento = nomeEventoLocation.replaceAll("[\\s:/\\\\-]", "");
        // 2) rimuove spazi, '/', '-'
        String cleanData   = data.replaceAll("[\\s/\\\\-]", "");
        // 3) tiene solo cifre
        String cleanSessione = sessione.replaceAll("[^0-9]", "");

        return cleanEvento + "_" + cleanData + "_" + cleanSessione + ".pdf";
    }




}
