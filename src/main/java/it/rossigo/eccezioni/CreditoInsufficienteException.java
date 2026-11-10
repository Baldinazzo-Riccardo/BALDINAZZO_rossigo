package it.rossigo.eccezioni;

public class CreditoInsufficienteException extends RossiGoException {
    private final String idUtente;
    private final double creditoAttuale;
    private final double creditoRichiesto;

    public CreditoInsufficienteException(String idUtente, double creditoAttuale, double creditoRichiesto) {
        super(String.format("Credito insufficiente per l'utente %s: disponibili %.2f €, richiesti almeno %.2f €.", idUtente, creditoAttuale, creditoRichiesto));
        this.idUtente = idUtente;
        this.creditoAttuale = creditoAttuale;
        this.creditoRichiesto = creditoRichiesto;
    }

    public CreditoInsufficienteException(String idUtente, double creditoAttuale, double creditoRichiesto, Throwable causa) {
        super(String.format("Credito insufficiente per l'utente %s: disponibili %.2f €, richiesti almeno %.2f €.", idUtente, creditoAttuale, creditoRichiesto), causa);
        this.idUtente = idUtente;
        this.creditoAttuale = creditoAttuale;
        this.creditoRichiesto = creditoRichiesto;
    }

    public String getIdUtente() {
        return idUtente;
    }

    public double getCreditoAttuale() {
        return creditoAttuale;
    }

    public double getCreditoRichiesto() {
        return creditoRichiesto;
    }
}
