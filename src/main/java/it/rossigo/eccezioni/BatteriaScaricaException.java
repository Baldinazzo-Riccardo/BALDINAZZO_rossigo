package it.rossigo.eccezioni;

public class BatteriaScaricaException extends RossiGoException {
    private final String codiceVeicolo;
    private final double livelloAttuale;

    public BatteriaScaricaException(String codiceVeicolo, double livelloAttuale) {
        super(String.format("Batteria al %.1f%% per il mezzo %s: soglia minima 15%% non raggiunta, scegli un altro mezzo.", livelloAttuale, codiceVeicolo));
        this.codiceVeicolo = codiceVeicolo;
        this.livelloAttuale = livelloAttuale;
    }

    public BatteriaScaricaException(String codiceVeicolo, double livelloAttuale, Throwable causa) {
        super(String.format("Batteria al %.1f%% per il mezzo %s: soglia minima 15%% non raggiunta, scegli un altro mezzo.", livelloAttuale, codiceVeicolo), causa);
        this.codiceVeicolo = codiceVeicolo;
        this.livelloAttuale = livelloAttuale;
    }

    public String getCodiceVeicolo() {
        return codiceVeicolo;
    }

    public double getLivelloAttuale() {
        return livelloAttuale;
    }
}
