package it.rossigo.servizio;

import it.kamaladafrica.codicefiscale.CodiceFiscale;
import it.rossigo.eccezioni.*;
import it.rossigo.modello.*;
import it.rossigo.persistenza.DatabaseManager;
import org.apache.commons.validator.routines.EmailValidator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class FlottaService {
    private static final double COSTO_SBLOCCO = 1.00;
    private static final double SOGLIA_MINIMA_BATTERIA = 15.0;

    private final DatabaseManager db;

    public FlottaService() {
        this.db = DatabaseManager.getIstanza();
    }

    public void inizializzaSistema() throws ErroreDatabaseException {
        db.inizializzaSchema();
    }

    public void registraVeicolo(Veicolo veicolo) throws RossiGoException {
        if (veicolo == null || veicolo.getCodice() == null || veicolo.getCodice().isBlank()) {
            throw new RossiGoException("Dati del veicolo non validi.");
        }
        db.salvaVeicolo(veicolo);
    }

    public void registraUtente(Utente utente) throws RossiGoException {
        if (utente == null || utente.getId() == null || utente.getId().isBlank()) {
            throw new RossiGoException("Dati dell'utente non validi.");
        }

        if (utente.getEmail() == null || !EmailValidator.getInstance().isValid(utente.getEmail())) {
            throw new RossiGoException("Indirizzo email non valido: " + utente.getEmail());
        }

        String cf = utente.getCodiceFiscale();
        if (cf == null || !CodiceFiscale.isFormatValid(cf)) {
            throw new CodiceFiscaleNonValidoException(cf);
        }

        db.salvaUtente(utente);
    }

    public Noleggio sbloccaVeicolo(String codiceVeicolo, String idUtente) throws RossiGoException {
        Utente utente = db.trovaUtentePerId(idUtente)
                .orElseThrow(() -> new UtenteNonTrovatoException(idUtente));

        Veicolo veicolo = db.trovaVeicoloPerCodice(codiceVeicolo)
                .orElseThrow(() -> new RossiGoException("Veicolo non trovato: " + codiceVeicolo));

        if (veicolo.getStato() != StatoVeicolo.LIBERO) {
            throw new VeicoloNonDisponibileException(codiceVeicolo, veicolo.getStato());
        }

        if (veicolo.getLivelloBatteria() < SOGLIA_MINIMA_BATTERIA) {
            throw new BatteriaScaricaException(codiceVeicolo, veicolo.getLivelloBatteria());
        }

        if (utente.getCredito() < COSTO_SBLOCCO) {
            throw new CreditoInsufficienteException(idUtente, utente.getCredito(), COSTO_SBLOCCO);
        }

        veicolo.setStato(StatoVeicolo.IN_USO);
        db.aggiornaStatoEBatteriaVeicolo(veicolo);

        Noleggio noleggio = new Noleggio(utente, veicolo, LocalDateTime.now());
        db.salvaNoleggio(noleggio);

        return noleggio;
    }

    public double terminaNoleggio(String codiceVeicolo, int minutiUsati) throws RossiGoException {
        Veicolo veicolo = db.trovaVeicoloPerCodice(codiceVeicolo)
                .orElseThrow(() -> new RossiGoException("Veicolo non trovato: " + codiceVeicolo));

        Noleggio noleggio = db.trovaNoleggioAttivoPerVeicolo(codiceVeicolo)
                .orElseThrow(() -> new RossiGoException("Nessun noleggio attivo trovato per il mezzo " + codiceVeicolo));

        Utente utente = db.trovaUtentePerId(noleggio.getUtente().getId())
                .orElseThrow(() -> new UtenteNonTrovatoException(noleggio.getUtente().getId()));

        double costoUso = veicolo.calcolaCosto(minutiUsati);
        double costoTotale = COSTO_SBLOCCO + costoUso;

        veicolo.applicaConsumo(minutiUsati);
        veicolo.setStato(StatoVeicolo.LIBERO);
        db.aggiornaStatoEBatteriaVeicolo(veicolo);

        utente.addebita(costoTotale);
        db.aggiornaCreditoUtente(utente);

        noleggio.setOrarioFine(LocalDateTime.now());
        noleggio.setCostoTotale(costoTotale);
        db.aggiornaTermineNoleggio(noleggio);

        return costoTotale;
    }

    public void mettiInRicarica(String codiceVeicolo) throws RossiGoException {
        Veicolo veicolo = db.trovaVeicoloPerCodice(codiceVeicolo)
                .orElseThrow(() -> new RossiGoException("Veicolo non trovato: " + codiceVeicolo));

        if (veicolo.getStato() == StatoVeicolo.IN_USO) {
            throw new VeicoloNonDisponibileException(codiceVeicolo, veicolo.getStato());
        }

        veicolo.setStato(StatoVeicolo.IN_RICARICA);
        veicolo.setLivelloBatteria(100.0);
        db.aggiornaStatoEBatteriaVeicolo(veicolo);
    }

    public double stimaCosto(String codiceVeicolo, String idUtente, int minutiPrevisti) throws RossiGoException {
        Utente utente = db.trovaUtentePerId(idUtente)
                .orElseThrow(() -> new UtenteNonTrovatoException(idUtente));

        Veicolo veicolo = db.trovaVeicoloPerCodice(codiceVeicolo)
                .orElseThrow(() -> new RossiGoException("Veicolo non trovato: " + codiceVeicolo));

        double costoStimato = COSTO_SBLOCCO + veicolo.calcolaCosto(minutiPrevisti);

        if (utente.getCredito() < costoStimato) {
            throw new CreditoInsufficienteException(idUtente, utente.getCredito(), costoStimato);
        }

        return costoStimato;
    }

    //ottenimento dati da db
    public Optional<Veicolo> getVeicolo(String codice) throws ErroreDatabaseException {
        return db.trovaVeicoloPerCodice(codice);
    }

    public Optional<Utente> getUtente(String id) throws ErroreDatabaseException {
        return db.trovaUtentePerId(id);
    }

    public List<Veicolo> getTuttiVeicoli() throws ErroreDatabaseException {
        return db.trovaTuttiVeicoli();
    }

    public List<Utente> getTuttiUtenti() throws ErroreDatabaseException {
        return db.trovaTuttiUtenti();
    }

    public List<Noleggio> getTuttiNoleggi() throws ErroreDatabaseException {
        return db.trovaTuttiNoleggi();
    }
}
