package it.rossigo.modello;

public abstract class Veicolo {
    protected String codice;
    protected double livelloBatteria;
    protected StatoVeicolo stato;

    public Veicolo(String codice, double livelloBatteria, StatoVeicolo stato) {
        this.codice = codice;
        this.livelloBatteria = Math.max(0.0, Math.min(100.0, livelloBatteria));
        this.stato = stato != null ? stato : StatoVeicolo.LIBERO;
    }

    public abstract double calcolaCosto(int minuti);

    public abstract double consumoBatteriaPerMinuto();

    public String getCodice() {
        return codice;
    }

    public void setCodice(String codice) {
        this.codice = codice;
    }

    public double getLivelloBatteria() {
        return livelloBatteria;
    }

    public void setLivelloBatteria(double livelloBatteria) {
        this.livelloBatteria = Math.max(0.0, Math.min(100.0, livelloBatteria));
    }

    public StatoVeicolo getStato() {
        return stato;
    }

    public void setStato(StatoVeicolo stato) {
        this.stato = stato;
    }

    public void applicaConsumo(int minuti) {
        double consumo = consumoBatteriaPerMinuto() * minuti;
        this.livelloBatteria = Math.max(0.0, this.livelloBatteria - consumo);
    }

    public void ricaricaCompleta() {
        this.livelloBatteria = 100.0;
        this.stato = StatoVeicolo.LIBERO;
    }

    @Override
    public String toString() {
        return String.format("[%s] Batteria: %.1f%% | Stato: %s", codice, livelloBatteria, stato);
    }
}
