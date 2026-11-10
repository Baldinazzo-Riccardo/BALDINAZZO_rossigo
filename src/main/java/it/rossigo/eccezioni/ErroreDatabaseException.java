package it.rossigo.eccezioni;

public class ErroreDatabaseException extends RossiGoException {

    public ErroreDatabaseException(String messaggio) {
        super(messaggio);
    }

    public ErroreDatabaseException(String messaggio, Throwable causa) {
        super(messaggio, causa);
    }
}
