package it.rossigo.modello;

public class Monopattino extends Veicolo {
    private static final double TARIFFA_MINUTO = 0.25;
    private static final double CONSUMO_MINUTO = 1.20;
    private int velocitaMassima;
    private boolean limitatoInZonaPedonale;

    public Monopattino(String codice, double livelloBatteria, StatoVeicolo stato, int velocitaMassima, boolean limitatoInZonaPedonale) {
        super(codice, livelloBatteria, stato);
        this.velocitaMassima = velocitaMassima;
        this.limitatoInZonaPedonale = limitatoInZonaPedonale;
    }

    @Override
    public double calcolaCosto(int minuti) {
        return Math.max(0, minuti) * TARIFFA_MINUTO;
    }

    @Override
    public double consumoBatteriaPerMinuto() {
        return CONSUMO_MINUTO;
    }

    public int getVelocitaMassima() {
        return velocitaMassima;
    }

    public void setVelocitaMassima(int velocitaMassima) {
        this.velocitaMassima = velocitaMassima;
    }

    public boolean isLimitatoInZonaPedonale() {
        return limitatoInZonaPedonale;
    }

    public void setLimitatoInZonaPedonale(boolean limitatoInZonaPedonale) {
        this.limitatoInZonaPedonale = limitatoInZonaPedonale;
    }

    @Override
    public String toString() {
        return String.format("Monopattino %s | VMax: %d km/h | Pedonale: %s | Tariffa: %.2f €/min",
                super.toString(), velocitaMassima, limitatoInZonaPedonale ? "Sì (6 km/h)" : "No", TARIFFA_MINUTO);
    }
}
