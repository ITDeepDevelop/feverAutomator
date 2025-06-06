package com.automator.model.services;

import java.util.Objects;

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

    // Costruttore vuoto
    public EventoRow() {
    }

    // Costruttore con tutti i campi
    public EventoRow(String nomeEventoELocation,
                     String codiceSpettacoloGenere,
                     String dataEvento,
                     String sessioni,
                     String nomeLocation,
                     String indirizzo,
                     String citta,
                     String cap,
                     String capienza,
                     String email) {
        this.nomeEventoELocation = nomeEventoELocation;
        this.codiceSpettacoloGenere = codiceSpettacoloGenere;
        this.dataEvento = dataEvento;
        this.sessioni = sessioni;
        this.nomeLocation = nomeLocation;
        this.indirizzo = indirizzo;
        this.citta = citta;
        this.cap = cap;
        this.capienza = capienza;
        this.email = email;
    }

    // Getter e Setter
    public String getNomeEventoELocation() {
        return nomeEventoELocation;
    }

    public void setNomeEventoELocation(String nomeEventoELocation) {
        this.nomeEventoELocation = nomeEventoELocation;
    }

    public String getCodiceSpettacoloGenere() {
        return codiceSpettacoloGenere;
    }

    public void setCodiceSpettacoloGenere(String codiceSpettacoloGenere) {
        this.codiceSpettacoloGenere = codiceSpettacoloGenere;
    }

    public String getDataEvento() {
        return dataEvento;
    }

    public void setDataEvento(String dataEvento) {
        this.dataEvento = dataEvento;
    }

    public String getSessioni() {
        return sessioni;
    }

    public void setSessioni(String sessioni) {
        this.sessioni = sessioni;
    }

    public String getNomeLocation() {
        return nomeLocation;
    }

    public void setNomeLocation(String nomeLocation) {
        this.nomeLocation = nomeLocation;
    }

    public String getIndirizzo() {
        return indirizzo;
    }

    public void setIndirizzo(String indirizzo) {
        this.indirizzo = indirizzo;
    }

    public String getCitta() {
        return citta;
    }

    public void setCitta(String citta) {
        this.citta = citta;
    }

    public String getCap() {
        return cap;
    }

    public void setCap(String cap) {
        this.cap = cap;
    }

    public String getCapienza() {
        return capienza;
    }

    public void setCapienza(String capienza) {
        this.capienza = capienza;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Metodo stampaDettagli come da implementazione originale
    public void stampaDettagli() {
        System.out.println("----- Dettagli Evento -----");
        System.out.println("Nome evento + Location: " + nomeEventoELocation);
        System.out.println("Codice spettacolo / Genere: " + codiceSpettacoloGenere);
        System.out.println("Data evento: " + dataEvento);
        System.out.println("Sessioni: " + sessioni);
        System.out.println("Nome location: " + nomeLocation);
        System.out.println("Indirizzo: " + indirizzo);
        System.out.println("Città: " + citta);
        System.out.println("Cap: " + cap);
        System.out.println("Capienza: " + capienza);
        System.out.println("Email: " + email);
        System.out.println("----------------------------\n");
    }

    // toString()
    @Override
    public String toString() {
        return "EventoRow{" +
                "nomeEventoELocation='" + nomeEventoELocation + '\'' +
                ", codiceSpettacoloGenere='" + codiceSpettacoloGenere + '\'' +
                ", dataEvento='" + dataEvento + '\'' +
                ", sessioni='" + sessioni + '\'' +
                ", nomeLocation='" + nomeLocation + '\'' +
                ", indirizzo='" + indirizzo + '\'' +
                ", citta='" + citta + '\'' +
                ", cap='" + cap + '\'' +
                ", capienza='" + capienza + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    // equals() basato su tutti i campi
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventoRow)) return false;
        EventoRow that = (EventoRow) o;
        return Objects.equals(nomeEventoELocation, that.nomeEventoELocation) &&
                Objects.equals(codiceSpettacoloGenere, that.codiceSpettacoloGenere) &&
                Objects.equals(dataEvento, that.dataEvento) &&
                Objects.equals(sessioni, that.sessioni) &&
                Objects.equals(nomeLocation, that.nomeLocation) &&
                Objects.equals(indirizzo, that.indirizzo) &&
                Objects.equals(citta, that.citta) &&
                Objects.equals(cap, that.cap) &&
                Objects.equals(capienza, that.capienza) &&
                Objects.equals(email, that.email);
    }

    // hashCode() basato su tutti i campi
    @Override
    public int hashCode() {
        return Objects.hash(
                nomeEventoELocation,
                codiceSpettacoloGenere,
                dataEvento,
                sessioni,
                nomeLocation,
                indirizzo,
                citta,
                cap,
                capienza,
                email
        );
    }
}
