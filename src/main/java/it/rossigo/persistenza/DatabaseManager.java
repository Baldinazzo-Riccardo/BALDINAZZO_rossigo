package it.rossigo.persistenza;

import it.rossigo.eccezioni.ErroreDatabaseException;
import it.rossigo.modello.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DatabaseManager {
    private static DatabaseManager istanza;
    private final String urlDb;

    private DatabaseManager(String urlDb) {
        this.urlDb = urlDb;
    }

    public static synchronized DatabaseManager getIstanza() {
        if (istanza == null) {
            istanza = new DatabaseManager("jdbc:sqlite:rossigo.db");
        }
        return istanza;
    }

    public static synchronized DatabaseManager getIstanzaCustom(String percorsoDb) {
        istanza = new DatabaseManager("jdbc:sqlite:" + percorsoDb);
        return istanza;
    }

    public Connection getConnessione() throws ErroreDatabaseException {
        try {
            return DriverManager.getConnection(urlDb);
        } catch (SQLException e) {
            throw new ErroreDatabaseException("Impossibile connettersi al database SQLite.", e);
        }
    }

    public void inizializzaSchema() throws ErroreDatabaseException {
        String sqlVeicoli = """
            CREATE TABLE IF NOT EXISTS veicoli (
                codice TEXT PRIMARY KEY,
                tipo TEXT NOT NULL,
                livello_batteria REAL NOT NULL,
                stato TEXT NOT NULL,
                numero_marce INTEGER,
                velocita_massima INTEGER,
                limitato_zona_pedonale INTEGER
            );
        """;

        String sqlUtenti = """
            CREATE TABLE IF NOT EXISTS utenti (
                id TEXT PRIMARY KEY,
                nome TEXT NOT NULL,
                email TEXT NOT NULL,
                codice_fiscale TEXT NOT NULL,
                credito REAL NOT NULL
            );
        """;

        String sqlNoleggi = """
            CREATE TABLE IF NOT EXISTS noleggi (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                id_utente TEXT NOT NULL,
                codice_veicolo TEXT NOT NULL,
                orario_inizio TEXT NOT NULL,
                orario_fine TEXT,
                costo_totale REAL NOT NULL,
                FOREIGN KEY (id_utente) REFERENCES utenti(id),
                FOREIGN KEY (codice_veicolo) REFERENCES veicoli(codice)
            );
        """;

        try (Connection conn = getConnessione(); Statement stmt = conn.createStatement()) {
            stmt.execute(sqlVeicoli);
            stmt.execute(sqlUtenti);
            stmt.execute(sqlNoleggi);
        } catch (SQLException e) {
            throw new ErroreDatabaseException("Errore durante l'inizializzazione delle tabelle.", e);
        }
    }

    public void salvaVeicolo(Veicolo veicolo) throws ErroreDatabaseException {
        String sql = """
            INSERT INTO veicoli (codice, tipo, livello_batteria, stato, numero_marce, velocita_massima, limitato_zona_pedonale)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(codice) DO UPDATE SET
                livello_batteria = excluded.livello_batteria,
                stato = excluded.stato,
                numero_marce = excluded.numero_marce,
                velocita_massima = excluded.velocita_massima,
                limitato_zona_pedonale = excluded.limitato_zona_pedonale;
        """;

        try (Connection conn = getConnessione(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, veicolo.getCodice());
            if (veicolo instanceof Bici b) {
                ps.setString(2, "BICI");
                ps.setDouble(3, b.getLivelloBatteria());
                ps.setString(4, b.getStato().name());
                ps.setInt(5, b.getNumeroMarce());
                ps.setNull(6, Types.INTEGER);
                ps.setNull(7, Types.INTEGER);
            } else if (veicolo instanceof Monopattino m) {
                ps.setString(2, "MONOPATTINO");
                ps.setDouble(3, m.getLivelloBatteria());
                ps.setString(4, m.getStato().name());
                ps.setNull(5, Types.INTEGER);
                ps.setInt(6, m.getVelocitaMassima());
                ps.setInt(7, m.isLimitatoInZonaPedonale() ? 1 : 0);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ErroreDatabaseException("Errore nel salvataggio del veicolo.", e);
        }
    }

    public void aggiornaStatoEBatteriaVeicolo(Veicolo veicolo) throws ErroreDatabaseException {
        String sql = "UPDATE veicoli SET livello_batteria = ?, stato = ? WHERE codice = ?;";

        try (Connection conn = getConnessione(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, veicolo.getLivelloBatteria());
            ps.setString(2, veicolo.getStato().name());
            ps.setString(3, veicolo.getCodice());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ErroreDatabaseException("Errore nell'aggiornamento del veicolo.", e);
        }
    }

    public Optional<Veicolo> trovaVeicoloPerCodice(String codice) throws ErroreDatabaseException {
        String sql = "SELECT * FROM veicoli WHERE codice = ?;";

        try (Connection conn = getConnessione(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codice);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mappaVeicolo(rs));
                }
            }
        } catch (SQLException e) {
            throw new ErroreDatabaseException("Errore nella ricerca del veicolo " + codice, e);
        }
        return Optional.empty();
    }

    public List<Veicolo> trovaTuttiVeicoli() throws ErroreDatabaseException {
        String sql = "SELECT * FROM veicoli ORDER BY codice ASC;";
        List<Veicolo> lista = new ArrayList<>();

        try (Connection conn = getConnessione();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mappaVeicolo(rs));
            }
        } catch (SQLException e) {
            throw new ErroreDatabaseException("Errore nel recupero dei veicoli.", e);
        }
        return lista;
    }

    private Veicolo mappaVeicolo(ResultSet rs) throws SQLException {
        String codice = rs.getString("codice");
        String tipo = rs.getString("tipo");
        double batteria = rs.getDouble("livello_batteria");
        StatoVeicolo stato = StatoVeicolo.valueOf(rs.getString("stato"));

        if ("BICI".equalsIgnoreCase(tipo)) {
            int marce = rs.getInt("numero_marce");
            return new Bici(codice, batteria, stato, marce);
        } else {
            int velocita = rs.getInt("velocita_massima");
            boolean pedonale = rs.getInt("limitato_zona_pedonale") == 1;
            return new Monopattino(codice, batteria, stato, velocita, pedonale);
        }
    }

    public void salvaUtente(Utente utente) throws ErroreDatabaseException {
        String sql = """
            INSERT INTO utenti (id, nome, email, codice_fiscale, credito)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT(id) DO UPDATE SET
                nome = excluded.nome,
                email = excluded.email,
                codice_fiscale = excluded.codice_fiscale,
                credito = excluded.credito;
        """;

        try (Connection conn = getConnessione(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, utente.getId());
            ps.setString(2, utente.getNome());
            ps.setString(3, utente.getEmail());
            ps.setString(4, utente.getCodiceFiscale());
            ps.setDouble(5, utente.getCredito());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ErroreDatabaseException("Errore nel salvataggio dell'utente.", e);
        }
    }

    public void aggiornaCreditoUtente(Utente utente) throws ErroreDatabaseException {
        String sql = "UPDATE utenti SET credito = ? WHERE id = ?;";

        try (Connection conn = getConnessione(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, utente.getCredito());
            ps.setString(2, utente.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ErroreDatabaseException("Errore nell'aggiornamento del credito.", e);
        }
    }

    public Optional<Utente> trovaUtentePerId(String id) throws ErroreDatabaseException {
        String sql = "SELECT * FROM utenti WHERE id = ?;";

        try (Connection conn = getConnessione(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Utente(
                            rs.getString("id"),
                            rs.getString("nome"),
                            rs.getString("email"),
                            rs.getString("codice_fiscale"),
                            rs.getDouble("credito")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new ErroreDatabaseException("Errore nella ricerca dell'utente " + id, e);
        }
        return Optional.empty();
    }

    public List<Utente> trovaTuttiUtenti() throws ErroreDatabaseException {
        String sql = "SELECT * FROM utenti ORDER BY nome ASC;";
        List<Utente> lista = new ArrayList<>();

        try (Connection conn = getConnessione();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Utente(
                        rs.getString("id"),
                        rs.getString("nome"),
                        rs.getString("email"),
                        rs.getString("codice_fiscale"),
                        rs.getDouble("credito")
                ));
            }
        } catch (SQLException e) {
            throw new ErroreDatabaseException("Errore nel recupero degli utenti.", e);
        }
        return lista;
    }

    public void salvaNoleggio(Noleggio noleggio) throws ErroreDatabaseException {
        String sql = """
            INSERT INTO noleggi (id_utente, codice_veicolo, orario_inizio, orario_fine, costo_totale)
            VALUES (?, ?, ?, ?, ?);
        """;

        try (Connection conn = getConnessione();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, noleggio.getUtente().getId());
            ps.setString(2, noleggio.getVeicolo().getCodice());
            ps.setString(3, noleggio.getOrarioInizio().toString());
            ps.setString(4, noleggio.getOrarioFine() != null ? noleggio.getOrarioFine().toString() : null);
            ps.setDouble(5, noleggio.getCostoTotale());
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    noleggio.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new ErroreDatabaseException("Errore nel salvataggio del noleggio.", e);
        }
    }

    public void aggiornaTermineNoleggio(Noleggio noleggio) throws ErroreDatabaseException {
        String sql = "UPDATE noleggi SET orario_fine = ?, costo_totale = ? WHERE id = ?;";

        try (Connection conn = getConnessione(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, noleggio.getOrarioFine() != null ? noleggio.getOrarioFine().toString() : null);
            ps.setDouble(2, noleggio.getCostoTotale());
            ps.setInt(3, noleggio.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ErroreDatabaseException("Errore nell'aggiornamento del noleggio.", e);
        }
    }

    public Optional<Noleggio> trovaNoleggioAttivoPerVeicolo(String codiceVeicolo) throws ErroreDatabaseException {
        String sql = "SELECT * FROM noleggi WHERE codice_veicolo = ? AND orario_fine IS NULL ORDER BY id DESC LIMIT 1;";

        try (Connection conn = getConnessione(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codiceVeicolo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String idUtente = rs.getString("id_utente");
                    LocalDateTime inizio = LocalDateTime.parse(rs.getString("orario_inizio"));
                    double costo = rs.getDouble("costo_totale");

                    Optional<Utente> u = trovaUtentePerId(idUtente);
                    Optional<Veicolo> v = trovaVeicoloPerCodice(codiceVeicolo);

                    if (u.isPresent() && v.isPresent()) {
                        return Optional.of(new Noleggio(id, u.get(), v.get(), inizio, null, costo));
                    }
                }
            }
        } catch (SQLException e) {
            throw new ErroreDatabaseException("Errore nella ricerca del noleggio attivo per " + codiceVeicolo, e);
        }
        return Optional.empty();
    }

    public List<Noleggio> trovaTuttiNoleggi() throws ErroreDatabaseException {
        String sql = "SELECT * FROM noleggi ORDER BY id ASC;";
        List<Noleggio> lista = new ArrayList<>();

        try (Connection conn = getConnessione();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String idUtente = rs.getString("id_utente");
                String codVeicolo = rs.getString("codice_veicolo");
                LocalDateTime inizio = LocalDateTime.parse(rs.getString("orario_inizio"));
                String fineStr = rs.getString("orario_fine");
                LocalDateTime fine = fineStr != null ? LocalDateTime.parse(fineStr) : null;
                double costo = rs.getDouble("costo_totale");

                Utente u = trovaUtentePerId(idUtente).orElse(null);
                Veicolo v = trovaVeicoloPerCodice(codVeicolo).orElse(null);

                lista.add(new Noleggio(id, u, v, inizio, fine, costo));
            }
        } catch (SQLException e) {
            throw new ErroreDatabaseException("Errore nel recupero dei noleggi.", e);
        }
        return lista;
    }
}
