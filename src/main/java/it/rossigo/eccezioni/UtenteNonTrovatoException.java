package it.rossigo.eccezioni;

public class UtenteNonTrovatoException extends RossiGoException {
    private final String idUtente;

    public UtenteNonTrovatoException(String idUtente) {
        super(String.format("Nessun utente trovato con ID '%s'.", idUtente));
        this.idUtente = idUtente;
    }

    public UtenteNonTrovatoException(String idUtente, Throwable causa) {
        super(String.format("Nessun utente trovato con ID '%s'.", idUtente), causa);
        this.idUtente = idUtente;
    }

    public String getIdUtente() {
        return idUtente;
    }
}
