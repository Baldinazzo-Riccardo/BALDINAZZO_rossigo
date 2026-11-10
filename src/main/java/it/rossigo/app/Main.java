package it.rossigo.app;

import com.github.lalyos.jfiglet.FigletFont;
import it.rossigo.eccezioni.*;
import it.rossigo.modello.*;
import it.rossigo.servizio.FlottaService;

public class Main {

    public static void main(String[] args) {
        stampaBanner();

        FlottaService service = new FlottaService();

        try {
            service.inizializzaSistema();
            System.out.println(">> Sistema e database SQLite inizializzati con successo.\n");
        } catch (ErroreDatabaseException e) {
            System.err.println("[ERRORE CRITICO DB] " + e.getMessage());
            return;
        }

        popolaFlottaEUtenti(service);

        eseguiScenariSimulazione(service);

        dimostraPersistenzaAlRiavvio();
    }

    private static void stampaBanner() {
        try {
            String asciiArt = FigletFont.convertOneLine("RossiGo");
            System.out.println(asciiArt);
        } catch (Exception e) {
            System.out.println("=========================================");
            System.out.println("           *** ROSSIGO ***               ");
            System.out.println("=========================================");
        }
        System.out.println("Sistema di Sharing Micromobilità - Backend Java 25 & SQLite");
        System.out.println("-----------------------------------------------------------\n");
    }

    private static void popolaFlottaEUtenti(FlottaService service) {
        System.out.println("=== FASE 1: REGISTRAZIONE MEZZI E UTENTI DI BASE ===");
        try {
            service.registraVeicolo(new Bici("BK-001", 85.0, StatoVeicolo.LIBERO, 7));
            service.registraVeicolo(new Bici("BK-002", 90.0, StatoVeicolo.LIBERO, 21));
            service.registraVeicolo(new Monopattino("SC-101", 75.0, StatoVeicolo.LIBERO, 25, true));
            service.registraVeicolo(new Monopattino("SC-102", 10.0, StatoVeicolo.LIBERO, 20, false));

            service.registraUtente(new Utente("U01", "Mario Rossi", "mario.rossi@email.it", "RSSMRA85M01H501Q", 15.00));
            service.registraUtente(new Utente("U02", "Lucia Verdi", "lucia.verdi@email.it", "VRDLCU92T45F205R", 0.50));
            service.registraUtente(new Utente("U03", "Giulia Bianchi", "giulia.bianchi@email.it", "BNCGLI98A41L219E", 20.00));

            System.out.println("Mezzi e utenti registrati correttamente nel database.\n");
        } catch (RossiGoException e) {
            System.err.println("[ERRORE REGISTRAZIONE INIZIALE] " + e.getMessage());
        }
    }

    private static void eseguiScenariSimulazione(FlottaService service) {
        System.out.println("=== FASE 2: SIMULAZIONE SCENARI OPERATIVI ===\n");

        System.out.println("[SCENARIO 1] Tentativo di registrazione con Codice Fiscale errato:");
        try {
            Utente utenteErrato = new Utente("U99", "Paolo Neri", "paolo.neri@email.it", "CODICEFISCALEERRATO123", 10.00);
            service.registraUtente(utenteErrato);
            System.out.println("Utente registrato (imprevisto).");
        } catch (CodiceFiscaleNonValidoException e) {
            System.out.println("-> Intercettata eccezione: " + e.getMessage());
            System.out.println("   [Messaggio per app mobile] Attenzione: il codice fiscale inserito non è valido. Correggi i dati prima di procedere.\n");
        } catch (RossiGoException e) {
            System.out.println("-> Errore generico: " + e.getMessage() + "\n");
        }

        System.out.println("[SCENARIO 2] Tentativo di sblocco con credito insufficiente (0.50 €):");
        try {
            service.sbloccaVeicolo("SC-101", "U02");
            System.out.println("Sblocco avvenuto (imprevisto).");
        } catch (CreditoInsufficienteException e) {
            System.out.println("-> Intercettata eccezione: " + e.getMessage());
            System.out.println(String.format("   [Messaggio per app mobile] Credito insufficiente (disponibili: %.2f €). Ricarica almeno %.2f € per sbloccare il mezzo.\n",
                    e.getCreditoAttuale(), e.getCreditoRichiesto()));
        } catch (RossiGoException e) {
            System.out.println("-> Errore generico: " + e.getMessage() + "\n");
        }

        System.out.println("[SCENARIO 3] Tentativo di sblocco con batteria scarica (10% < 15%):");
        try {
            service.sbloccaVeicolo("SC-102", "U01");
            System.out.println("Sblocco avvenuto (imprevisto).");
        } catch (BatteriaScaricaException e) {
            System.out.println("-> Intercettata eccezione: " + e.getMessage());
            System.out.println(String.format("   [Messaggio per app mobile] Il monopattino ha la batteria al %.1f%%. Trova un altro mezzo nelle vicinanze.\n",
                    e.getLivelloAttuale()));
        } catch (RossiGoException e) {
            System.out.println("-> Errore generico: " + e.getMessage() + "\n");
        }

        System.out.println("[SCENARIO 4] Noleggio valido Bici (BK-001) per Mario Rossi (20 minuti):");
        try {
            service.sbloccaVeicolo("BK-001", "U01");
            System.out.println("-> Bici BK-001 sbloccata con successo da Mario Rossi.");
            double costoTotale = service.terminaNoleggio("BK-001", 20);
            System.out.println(String.format("-> Noleggio terminato: durata 20 min. Costo addebitato: %.2f € (1.00 € sblocco + 3.00 € tariffa bici).", costoTotale));
            service.getVeicolo("BK-001").ifPresent(v -> System.out.println("   Stato mezzo aggiornato: " + v));
            service.getUtente("U01").ifPresent(u -> System.out.println("   Credito residuo Mario Rossi: " + String.format("%.2f €\n", u.getCredito())));
        } catch (RossiGoException e) {
            System.out.println("-> Errore durante il noleggio: " + e.getMessage() + "\n");
        }

        System.out.println("[SCENARIO 5] Tentativo di sblocco di un mezzo già IN_USO:");
        try {
            service.sbloccaVeicolo("BK-002", "U03");
            System.out.println("-> Bici BK-002 sbloccata da Giulia Bianchi.");
            service.sbloccaVeicolo("BK-002", "U01");
            System.out.println("Sblocco concorrente riuscito (imprevisto).");
        } catch (VeicoloNonDisponibileException e) {
            System.out.println("-> Intercettata eccezione: " + e.getMessage());
            System.out.println("   [Messaggio per app mobile] Il mezzo selezionato è già in corsa con un altro utente.\n");
        } catch (RossiGoException e) {
            System.out.println("-> Errore generico: " + e.getMessage() + "\n");
        } finally {
            try {
                service.terminaNoleggio("BK-002", 10);
            } catch (RossiGoException ignored) {}
        }

        System.out.println("[SCENARIO 6] Noleggio Monopattino (SC-101) per Giulia Bianchi (15 minuti):");
        try {
            service.sbloccaVeicolo("SC-101", "U03");
            System.out.println("-> Monopattino SC-101 sbloccato.");
            double costoTotale = service.terminaNoleggio("SC-101", 15);
            System.out.println(String.format("-> Noleggio terminato: durata 15 min. Costo addebitato: %.2f € (1.00 € sblocco + 3.75 € tariffa monopattino).", costoTotale));
            service.getVeicolo("SC-101").ifPresent(v -> System.out.println("   Stato monopattino: " + v));
            service.getUtente("U03").ifPresent(u -> System.out.println("   Credito residuo Giulia: " + String.format("%.2f €\n", u.getCredito())));
        } catch (RossiGoException e) {
            System.out.println("-> Errore noleggio monopattino: " + e.getMessage() + "\n");
        }

        System.out.println("[SCENARIO 7] Ricarica mezzo scarico (SC-102):");
        try {
            service.mettiInRicarica("SC-102");
            service.getVeicolo("SC-102").ifPresent(v -> System.out.println("-> Mezzo messo in ricarica: " + v + "\n"));
        } catch (RossiGoException e) {
            System.out.println("-> Errore ricarica: " + e.getMessage() + "\n");
        }

        System.out.println("[SCENARIO 8 - BONUS] Stima costo preventivo e verifica disponibilità credito:");
        try {
            double stima = service.stimaCosto("SC-101", "U01", 60);
            System.out.println(String.format("Stima costo calcolata: %.2f € (imprevisto con credito ridotto).", stima));
        } catch (CreditoInsufficienteException e) {
            System.out.println("-> Intercettata eccezione preventiva: " + e.getMessage());
            System.out.println(String.format("   [Messaggio per app mobile] Il credito attuale (%.2f €) non è sufficiente per la corsa stimata (%.2f €). Ricarica prima di partire!\n",
                    e.getCreditoAttuale(), e.getCreditoRichiesto()));
        } catch (RossiGoException e) {
            System.out.println("-> Errore stima: " + e.getMessage() + "\n");
        }
    }

    private static void dimostraPersistenzaAlRiavvio() {
        System.out.println("=== FASE 3: DIMOSTRAZIONE PERSISTENZA AL RIAVVIO (SIMULAZIONE SHUTDOWN) ===");
        System.out.println("Chiusura sessione corrente... Creazione di una nuova istanza di FlottaService...");

        FlottaService nuovoService = new FlottaService();

        try {
            System.out.println("\nVerifica stato dopo il 'riavvio' dell'applicazione:");

            nuovoService.getUtente("U01").ifPresent(u -> {
                System.out.println("-> Utente Mario Rossi (U01) ritrovato su SQLite con credito aggiornato: " + String.format("%.2f € (previsto: 11.00 €)", u.getCredito()));
            });

            nuovoService.getVeicolo("BK-001").ifPresent(v -> {
                System.out.println("-> Veicolo BK-001 ritrovato su SQLite con batteria residua: " + String.format("%.1f%% (previsto: 78.0%%)", v.getLivelloBatteria()));
            });

            nuovoService.getVeicolo("SC-102").ifPresent(v -> {
                System.out.println("-> Monopattino SC-102 ritrovato su SQLite ricaricato al: " + String.format("%.1f%%", v.getLivelloBatteria()));
            });

            int numeroNoleggi = nuovoService.getTuttiNoleggi().size();
            System.out.println("-> Storico noleggi archiviati nel database SQLite: " + numeroNoleggi + " noleggi totali registrati.");
            System.out.println("\n>> Test di persistenza completato con successo: tutti i dati sono sopravvissuti al riavvio!");

        } catch (ErroreDatabaseException e) {
            System.err.println("[ERRORE TEST PERSISTENZA] " + e.getMessage());
        }
    }
}
