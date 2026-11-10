package it.rossigo.modello;

public class Utente {
    private String id;
    private String nome;
    private String email;
    private String codiceFiscale;
    private double credito;

    public Utente(String id, String nome, String email, String codiceFiscale, double credito) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.codiceFiscale = codiceFiscale != null ? codiceFiscale.toUpperCase().trim() : "";
        this.credito = credito;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCodiceFiscale() {
        return codiceFiscale;
    }

    public void setCodiceFiscale(String codiceFiscale) {
        this.codiceFiscale = codiceFiscale != null ? codiceFiscale.toUpperCase().trim() : "";
    }

    public double getCredito() {
        return credito;
    }

    public void setCredito(double credito) {
        this.credito = credito;
    }

    public void addebita(double importo) {
        this.credito -= importo;
    }

    public void ricaricaCredito(double importo) {
        if (importo > 0) {
            this.credito += importo;
        }
    }

    @Override
    public String toString() {
        return String.format("Utente[%s] %s (CF: %s) - Credito: %.2f €", id, nome, codiceFiscale, credito);
    }
}
