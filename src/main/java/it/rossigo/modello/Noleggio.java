package it.rossigo.modello;

import java.time.LocalDateTime;

public class Noleggio {
    private int id;
    private Utente utente;
    private Veicolo veicolo;
    private LocalDateTime orarioInizio;
    private LocalDateTime orarioFine;
    private double costoTotale;

    public Noleggio(int id, Utente utente, Veicolo veicolo, LocalDateTime orarioInizio, LocalDateTime orarioFine, double costoTotale) {
        this.id = id;
        this.utente = utente;
        this.veicolo = veicolo;
        this.orarioInizio = orarioInizio;
        this.orarioFine = orarioFine;
        this.costoTotale = costoTotale;
    }

    public Noleggio(Utente utente, Veicolo veicolo, LocalDateTime orarioInizio) {
        this(0, utente, veicolo, orarioInizio, null, 0.0);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Utente getUtente() {
        return utente;
    }

    public void setUtente(Utente utente) {
        this.utente = utente;
    }

    public Veicolo getVeicolo() {
        return veicolo;
    }

    public void setVeicolo(Veicolo veicolo) {
        this.veicolo = veicolo;
    }

    public LocalDateTime getOrarioInizio() {
        return orarioInizio;
    }

    public void setOrarioInizio(LocalDateTime orarioInizio) {
        this.orarioInizio = orarioInizio;
    }

    public LocalDateTime getOrarioFine() {
        return orarioFine;
    }

    public void setOrarioFine(LocalDateTime orarioFine) {
        this.orarioFine = orarioFine;
    }

    public double getCostoTotale() {
        return costoTotale;
    }

    public void setCostoTotale(double costoTotale) {
        this.costoTotale = costoTotale;
    }

    public boolean isAttivo() {
        return orarioFine == null;
    }

    @Override
    public String toString() {
        return String.format("Noleggio #%d [Veicolo: %s, Utente: %s] Da: %s A: %s (Tot: %.2f €)",
                id,
                veicolo != null ? veicolo.getCodice() : "N/D",
                utente != null ? utente.getId() : "N/D",
                orarioInizio,
                orarioFine != null ? orarioFine.toString() : "IN CORSO",
                costoTotale);
    }
}
