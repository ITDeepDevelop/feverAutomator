
package com.automator.test;

import org.junit.jupiter.api.Test;

import com.automator.model.services.ExcelReader;
import com.automator.model.services.ExcelStorage;

import java.io.File;
import java.net.URL;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExcelReaderTest {

    @Test
    void testFindEventoAndGetCitta() throws Exception {
        URL resourceUrl = getClass().getResource("/excel/File1.xlsx");
        assertNotNull(resourceUrl, "File di test non trovato nelle risorse");

//    //metodo per richiamare ovunque nel progetto l'excel salvato da UI e poterlo usare
//      ExcelReader reader = new ExcelReader();
//      reader.read(ExcelStorage.getInstance().getFile());
        File file = new File(resourceUrl.toURI());

        ExcelReader reader = new ExcelReader();
        reader.read(file);

        // Cambia questo valore con uno effettivamente presente nel file
        String valoreEventoDaCercare = "Candlelight: Tributo ai Beatles - Sala Bianca"; 

        Map<String, String> row = reader.getRowByColumnValue("Nome evento + Location", valoreEventoDaCercare);

        assertNotNull(row, "Evento non trovato");
        System.out.println("Riga trovata: " + row);
        
        // qua puoi mettere qualsiasi colonna del excel e lui ti restituisce il valore della riga che gli abbiamo passato
        String citta = reader.getCellValueFromRow(row, "Città");
        System.out.println("Città evento: " + citta);
        
        System.out.println("Tutte le righe" + reader.getAllRows());
        String email = reader.getValueByTwoKeys(
        	    "Data evento",
        	    "16/05/2025",
        	    "Nome location",
        	    "Centro Congressi Giovanni XXIII",
        	    "Email"
        	);
    	System.out.println("Email trovata: " + email);
        
        assertNotNull(citta);
        assertFalse(citta.isBlank(), "La città non dovrebbe essere vuota");
    }
}
