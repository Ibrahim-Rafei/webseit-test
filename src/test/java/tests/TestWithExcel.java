package tests;

import TestComponents.BaseTest;
import TestComponents.Retry;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;


public class TestWithExcel extends BaseTest {

    @Test(dataProvider = "getData", retryAnalyzer = Retry.class)
    public void maintest(String input) throws MalformedURLException {
        landingPage.testUrl(input);
    }

    @DataProvider
    public Object[] getData() throws IOException {
        FileInputStream fis= new FileInputStream("src/test/resources/excelData.xlsx");
        XSSFWorkbook workbook=new XSSFWorkbook(fis);
        XSSFSheet sheet = workbook.getSheetAt(0);
        int rowcount = sheet.getPhysicalNumberOfRows();
        Object data[]= new Object[rowcount];
        for (int i=0; i < rowcount ; i++ ){
            data[i] = sheet.getRow(i).getCell(0).getStringCellValue();
        }
        return data ;
    }

}
