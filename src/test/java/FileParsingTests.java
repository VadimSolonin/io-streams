import com.codeborne.pdftest.PDF;
import com.codeborne.xlstest.XLS;
import com.opencsv.CSVReader;
import models.Perfume;
import org.junit.jupiter.api.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Year;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class FileParsingTests {
    ClassLoader cl = FileParsingTests.class.getClassLoader();
    ObjectMapper mapper = new ObjectMapper();

    @Test
    public void verifyZipFilesContentTest() throws Exception {
        try (InputStream is = cl.getResourceAsStream("resources.zip");
             ZipInputStream zis = new ZipInputStream(is)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().contains(".pdf")) {
                    PDF pdf = new PDF(zis);
                    assertEquals(1, pdf.numberOfPages);
                    assertEquals("VS", pdf.author);
                    assertTrue(pdf.text.contains("PDF Test File"));
                    System.out.println("Successful check pdf file.");
                } else if (entry.getName().contains(".xlsx")) {
                    XLS xls = new XLS(zis);
                    Assertions.assertEquals("Парфюмерия >> Нишевая парфюмерия >> Montale (100 мл)",
                            xls.excel.getSheetAt(0)
                                    .getRow(22)
                                    .getCell(0)
                                    .getStringCellValue());
                    System.out.println("Successful check xlsx file.");
                } else if (entry.getName().contains(".csv")) {
                    CSVReader csvReader = new CSVReader(new InputStreamReader(zis));
                    List<String[]> content = csvReader.readAll();
                    Assertions.assertEquals(2, content.size());
                    final String[] firstRow = content.get(0);
                    Assertions.assertArrayEquals(new String[]{"Mancera", "Hindu kush"}, firstRow);
                    System.out.println("Successful check csv file.");
                }
            }
        }
    }

    @Test
    void verifyJsonContentTest() throws Exception {
        try (
                InputStream is = cl.getResourceAsStream("Test.json");
                InputStreamReader reader = new InputStreamReader(is)
        ) {
            Perfume perfume = mapper.readValue(reader, Perfume.class);
            assertThat(perfume.getBrand()).isEqualTo("Xerjoff");
            assertThat(perfume.getName()).isNotNull();
            int currentYear = Year.now().getValue();
            assertThat(perfume.getReleasedYear()).isBetween(1370, currentYear);
            assertEquals(perfume.getNotes().get(3), "tobacco");
            System.out.println("Successful check json file.");
        }
    }


}
