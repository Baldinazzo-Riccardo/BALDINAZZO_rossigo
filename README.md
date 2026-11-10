# IL MIO ESERCIZIO DI ROSSI-GO
###### Baldinazzo Riccardo

---

## Descrizione del Progetto

Questo progetto è un'applicazione Java sviluppata con Maven per gestire la simulazione del servizio RossiGo.
L'applicazione permette di gestire i dati dei clienti, la validazione delle informazioni personali, l'esecuzione di vari scenari di test e il salvataggio dei dati all'interno di un database locale.

---

## Librerie:

- `org.xerial` : Usato per la gestione del database SQLite all'interno del progetto.
- `it.kamaladafrica` : Usato per validazione del codice fiscale italiano dei clienti.
- `com.github.lalyos` : Usato per print del titolo iniziale in ASCII art all'avvio dell'applicazione.
- `commons-validator` : Usato per la validazione dell'indirizzo email e di altri formati di testo.

### Motivazione delle scelte:
Queste librerie esterne sono state scelte per non dover "reinventare la ruota" su operazioni comuni e ripetitive:
- `xerial` fornisce un driver leggero per database senza dover installare server esterni.
- `kamaladafrica` e `commons-validator` evitano errori manuali nel controllo del formato dei dati inseriti (codice fiscale ed email).
- `jfiglet` (lalyos) rende l'interfaccia a riga di comando più chiara e visivamente gradevole.

---

## Scenari Simulati

Nel programma sono stati simulati i seguenti scenari principali:

1. **Inserimento di un nuovo cliente:** Creazione di un profilo con verifica della validità di codice fiscale ed email.
2. **Gestione degli errori di input:** Tentativo di inserimento di dati errati (es. codice fiscale corto o email senza `@`) per testare le eccezioni.
3. **Salvataggio su DB:** Scrittura dei dati validati nel file di database locale `rossigo.db`.
4. **Lettura e stampa dati:** Recupero delle informazioni dal database per mostrare lo stato aggiornato a schermo.

---
