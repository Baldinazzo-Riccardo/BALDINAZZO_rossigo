package it.rossigo.eccezioni;

import it.rossigo.modello.StatoVeicolo;

public class VeicoloNonDisponibileException extends RossiGoException {
    private final String codiceVeicolo;
    private final StatoVeicolo statoAttuale;

    public VeicoloNonDisponibileException(String codiceVeicolo, StatoVeicolo statoAttuale) {
        super(String.format("Il veicolo %s non è disponibile per il noleggio (stato attuale: %s).", codiceVeicolo, statoAttuale));
        this.codiceVeicolo = codiceVeicolo;
        this.statoAttuale = statoAttuale;
    }

    public VeicoloNonDisponibileException(String codiceVeicolo, StatoVeicolo statoAttuale, Throwable causa) {
        super(String.format("Il veicolo %s non è disponibile per il noleggio (stato attuale: %s).", codiceVeicolo, statoAttuale), causa);
        this.codiceVeicolo = codiceVeicolo;
        this.statoAttuale = statoAttuale;
    }

    public String getCodiceVeicolo() {
        return codiceVeicolo;
    }

    public StatoVeicolo getStatoAttuale() {
        return statoAttuale;
    }
}
