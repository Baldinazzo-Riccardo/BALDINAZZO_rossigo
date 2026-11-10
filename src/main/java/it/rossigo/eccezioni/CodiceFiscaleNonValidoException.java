package it.rossigo.eccezioni;

public class CodiceFiscaleNonValidoException extends RossiGoException {
    private final String codiceFiscaleErrato;

    public CodiceFiscaleNonValidoException(String codiceFiscaleErrato) {
        super(String.format("Il codice fiscale fornito '%s' non è formalmente valido secondo gli standard nazionali.", codiceFiscaleErrato));
        this.codiceFiscaleErrato = codiceFiscaleErrato;
    }

    public CodiceFiscaleNonValidoException(String codiceFiscaleErrato, Throwable causa) {
        super(String.format("Il codice fiscale fornito '%s' non è formalmente valido secondo gli standard nazionali.", codiceFiscaleErrato), causa);
        this.codiceFiscaleErrato = codiceFiscaleErrato;
    }

    public String getCodiceFiscaleErrato() {
        return codiceFiscaleErrato;
    }
}
