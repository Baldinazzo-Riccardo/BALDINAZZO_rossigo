package it.rossigo.eccezioni;

public class RossiGoException extends Exception {

    public RossiGoException(String messaggio) {
        super(messaggio);
    }

    public RossiGoException(String messaggio, Throwable causa) {
        super(messaggio, causa);
    }
}
