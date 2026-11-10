package it.rossigo.modello;

public class Bici extends Veicolo {
    private static final double TARIFFA_MINUTO = 0.15;
    private static final double CONSUMO_MINUTO = 0.35;
    private int numeroMarce;

    public Bici(String codice, double livelloBatteria, StatoVeicolo stato, int numeroMarce) {
        super(codice, livelloBatteria, stato);
        this.numeroMarce = numeroMarce;
    }

    @Override
    public double calcolaCosto(int minuti) {
        return Math.max(0, minuti) * TARIFFA_MINUTO;
    }

    @Override
    public double consumoBatteriaPerMinuto() {
        return CONSUMO_MINUTO;
    }

    public int getNumeroMarce() {
        return numeroMarce;
    }

    public void setNumeroMarce(int numeroMarce) {
        this.numeroMarce = numeroMarce;
    }

    @Override
    public String toString() {
        return String.format("Bici %s | Marce: %d | Tariffa: %.2f €/min", super.toString(), numeroMarce, TARIFFA_MINUTO);
    }
}
