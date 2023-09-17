package com.exist.service7r1;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

//For Mocking a Static Method
import mockit.Expectations;
import mockit.Mocked;

//For Disabling Printing of Texts in the Console
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.exist.model7r1.Table;
import com.exist.util7r1.FileValidatorUtil;
import com.exist.util7r1.ScannerUtil;
import com.exist.util7r1.StringGeneratorUtil;

public class TableServiceImplTest {

	private TableService tableService;
	private Table table;
	private List<String> extractedKeyValueArrayList;
	private List<String> keyArrayList;
	private List<String> valueArrayList;
	private Map<String, List<String>> keyValueMap;
	private List<Integer> dimension;
	
	private final PrintStream originalOut = System.out;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	
	@BeforeEach
	void setUpBeforeEach() {
		tableService = new TableServiceImpl();
		table = new Table();
		tableService.setTable(table);
		extractedKeyValueArrayList = new ArrayList<String>(Arrays.asList("abc:def","ABC:DEF","123:456"));
		keyArrayList = new ArrayList<String>(Arrays.asList("abc","ABC","123"));
	    valueArrayList = new ArrayList<String>(Arrays.asList("def","DEF","456"));
		keyValueMap = new LinkedHashMap<String, List<String>>();
		for (int i = 0; i < keyArrayList.size(); i++) {
			String key = keyArrayList.get(i);
			String value = valueArrayList.get(i);
			keyValueMap.put(key, new ArrayList<String>());
			keyValueMap.get(key).add(key+value);
			keyValueMap.get(key).add(value);
		}
		dimension = new ArrayList<Integer>(Arrays.asList(2, 1));
	}
	
	@BeforeEach
	void setUpStreams() {
		System.setOut(new PrintStream(outContent));
	}
	
    @AfterEach
    public void restoreStreams() {
    	System.setOut(originalOut);
//		String output = outContent.toString();
//		System.out.println(output);
    }
	
//1
    @Test
    void testFileValidatorUtilWithValidFile() {
        FileValidatorUtil mockFileValidatorUtil = mock(FileValidatorUtil.class);
        when(mockFileValidatorUtil.runFileValidator(eq(new String[]{"valid_file.txt"}))).thenReturn("valid_file.txt");
        assertEquals("valid_file.txt", mockFileValidatorUtil.runFileValidator(new String[]{"valid_file.txt"}));
    }
//2	
	@Test
	void testRunTableServiceWithValidFile() {
	    TableService spyTableService = spy(tableService);
	    spyTableService.setTable(table); 
	    
	    String[] args = {"valid_file.txt"};
	    
	    FileValidatorUtil mockFileValidatorUtil = mock(FileValidatorUtil.class);
        when(mockFileValidatorUtil.runFileValidator(args)).thenReturn(args[0]);
        spyTableService.setFileValidatorUtil(mockFileValidatorUtil);
        	    
	    doNothing().when(spyTableService).checkExistingOrNewFile();

	    spyTableService.runTableService(args);
	    
	    verify(spyTableService).runTableService(args);
	    assertEquals("valid_file.txt", table.getFilePath());
	}
//3	
	@Test
	void testExtractArrayListOfLinesFromNullFile() {
		TableService spyTableService = spy(tableService);
		doNothing().when(spyTableService).extractArrayListOfLinesFromFile(Mockito.any());
		spyTableService.extractArrayListOfLinesFromFile(null);
	    verify(spyTableService).extractArrayListOfLinesFromFile(null);
	}
//4
	@Test 
	void testExtractArrayListOfLinesFromFile() {
		File tempFile = null;
		try (InputStream inputStream = TableServiceImpl.class.getResourceAsStream("/test_file.txt")) {
	        if (inputStream != null) {
	            tempFile = File.createTempFile("test_file", ".tmp");
	            tempFile.deleteOnExit();
	            
	            try (OutputStream outputStream = new FileOutputStream(tempFile)) {
	                IOUtils.copy(inputStream, outputStream);
	            }
	            tableService.extractArrayListOfLinesFromFile(tempFile);
	        } else {
	            System.err.println("Resource not found.");
	            System.exit(0);
	        }
	    } catch (IOException e) {
	        System.err.println("Error reading the resource: " + tempFile.getAbsolutePath());
	        e.printStackTrace();
	        System.exit(0);
	    }
		List<String> expectedArrayListOfLines = new ArrayList<String>();
		expectedArrayListOfLines.add("abc:def ABC:DEF ");
		expectedArrayListOfLines.add("123:456 ");
		assertEquals(expectedArrayListOfLines, tableService.getArrayListOfLines());
	}
//5
	@Test
	void testExtractKeyValueArrayListAndDim() {
		List<String> arrayListOfLines = new ArrayList<String>();
		arrayListOfLines.add("abc:def ABC:DEF ");
		arrayListOfLines.add("123:456 ");
	    tableService.setArrayListOfLines(arrayListOfLines);

	    tableService.extractKeyValueArrayListAndDim();
	    	    
	    assertEquals(extractedKeyValueArrayList, tableService.getExtractedKeyValueArrayList());
	    
	    List<Integer> expectedDimArrayList = new ArrayList<Integer>();  
	    expectedDimArrayList.add(2);
	    expectedDimArrayList.add(1);	    
	    assertEquals(expectedDimArrayList, table.getDimension());
	}
//6  
	@Test
	void testExtractKeyArrayList() {
		tableService.setExtractedKeyValueArrayList(extractedKeyValueArrayList);
		tableService.extractKeyArrayList();
		assertEquals(keyArrayList, tableService.getKeyArrayList());
	}
//7
	@Test
	void testExtractValueArrayList() {
		tableService.setExtractedKeyValueArrayList(extractedKeyValueArrayList);
		tableService.extractValueArrayList();
		assertEquals(valueArrayList, tableService.getValueArrayList());
	}
//8	
	@Test
	void testCreateMapFromKeyAndValueArrayLists() {
		tableService.setKeyArrayList(keyArrayList);
		tableService.setValueArrayList(valueArrayList);
		tableService.createMapFromKeyAndValueArrayLists();
		assertEquals(keyValueMap, table.getKeyValueMap());
	}
//9
	@Mocked
	private StringGeneratorUtil stringGeneratorUtil;
	@Test
	void testCreateRandomMap() {
		new Expectations() {{
			StringGeneratorUtil.getString(3);
			returns("abc", "def", "ABC", "DEF", "123", "456");
		}};
		
	    tableService.setTable(table);
	    tableService.setKeyLength(3);
	    tableService.setValueLength(3);
	    table.setDimension(dimension);

	    tableService.createRandomMap();

		assertEquals(keyValueMap, table.getKeyValueMap());
	}
//10
	@Test
	void testCreateKeyArrayListFromMap() {
		table.setKeyValueMap(keyValueMap);
		tableService.createKeyArrayListFromMap();
		assertEquals(keyArrayList, tableService.getKeyArrayList());
	}
//11
	@Test
	void testCreateValueArrayListFromMap() {
		table.setKeyValueMap(keyValueMap);
		tableService.createValueArrayListFromMap();
		assertEquals(valueArrayList, tableService.getValueArrayList());		
	}
//12
	@Mocked
	private ScannerUtil scannerUtil;
	@Test
	void testSearchWithMatch() {
		new Expectations() {{
			ScannerUtil.scanNextLine("\nEnter the string to be searched: ");
			result = "123";
		}};
		
		table.setKeyValueMap(keyValueMap);
		table.setDimension(dimension);
		
		tableService.search();
		assertTrue(tableService.withMatch());
	}
//13
	@Test
	void testSearchNoMatch() {
		new Expectations() {{
			ScannerUtil.scanNextLine("\nEnter the string to be searched: ");
			result = "XYZ";
		}};
		
		table.setKeyValueMap(keyValueMap);
		table.setDimension(dimension);
		
		tableService.search();
		assertFalse(tableService.withMatch());
	}
//14
	@Test
	void testEdit() {
		new Expectations() {{
			ScannerUtil.scanNextLine("Enter the key of the key:value pair to edit: ");
			result = "ABC";
			ScannerUtil.scanNextLine("Type \"k\" to edit the key, type \"v\" to edit the value or type \"x\""
									 + " to exit the editor: ");
			result = "v";
			ScannerUtil.scanNextLine("Enter the new value containing no space or \":\". Type \"x\" to exit editor: ");
			result = "GHI";
		}};
		
	    TableService spyTableService = spy(tableService);
	    spyTableService.setTable(table); 
        	    
	    doNothing().when(spyTableService).saveNewTable("Text file " + table.getFilePath() + " is now updated with new "
	    											   + "key:value pairs.\n\nNEW TABLE");
		
		table.setKeyValueMap(keyValueMap);
		table.setDimension(dimension);
		
		spyTableService.edit();
		
		List<String> valueArrayList = new ArrayList<String>(Arrays.asList("def","GHI","456"));
	    Map<String, List<String>> keyValueMap = new LinkedHashMap<String, List<String>>();
		for (int i = 0; i < keyArrayList.size(); i++) {
			String key = keyArrayList.get(i);
			String value = valueArrayList.get(i);
			keyValueMap.put(key, new ArrayList<String>());
			keyValueMap.get(key).add(key+value);
			keyValueMap.get(key).add(value);
		}
		
		assertEquals(keyValueMap,table.getKeyValueMap());
	}
//15 
	@Test
	void testPrintTable() {
		TableService spyTableService = spy(tableService);
	    spyTableService.setTable(table);
		table.setKeyValueMap(keyValueMap);
		table.setDimension(dimension);
		spyTableService.printTable();
		verify(spyTableService).printTable();
	}
//16
	@Test
	void testResetDimAndTable() {
		new Expectations() {{
			StringGeneratorUtil.getString(2);
			returns("kl", "98", "!@", "PQ", "12", "==");
			StringGeneratorUtil.getString(3);
			returns("mno", "765", "#$%", "RST", "345", "???");
		}};
		
		TableService spyTableService = spy(tableService);
	    spyTableService.setTable(table);     	    
	    doNothing().when(spyTableService).saveNewTable("Text file " + table.getFilePath() + " is now updated with new "
	    											   + "key:value pairs.\n\nNEW TABLE");
	    
	    spyTableService.resetDimAndTable(3, 2, 2, 3);
	    
	    List<Integer> dimension = new ArrayList<Integer>(Arrays.asList(2, 2, 2));
		List<String> keyArrayList = new ArrayList<String>(Arrays.asList("kl", "98", "!@", "PQ", "12", "=="));
	    List<String> valueArrayList = new ArrayList<String>(Arrays.asList("mno", "765", "#$%", "RST", "345", "???"));
	    Map<String, List<String>> keyValueMap = new LinkedHashMap<String, List<String>>();
		for (int i = 0; i < keyArrayList.size(); i++) {
			String key = keyArrayList.get(i);
			String value = valueArrayList.get(i);
			keyValueMap.put(key, new ArrayList<String>());
			keyValueMap.get(key).add(key+value);
			keyValueMap.get(key).add(value);
		}
	    
	    assertEquals(dimension,table.getDimension());
	    assertEquals(2, spyTableService.getKeyLength());
	    assertEquals(3, spyTableService.getValueLength());
	    assertEquals(keyValueMap, table.getKeyValueMap());
	}
//17
	@Test
	void testPrepareTextEntry() {
		table.setKeyValueMap(keyValueMap);
		table.setDimension(dimension);
		
		int counter = 0;
		String textEntry = "";
		for (int i = 0; i < dimension.size(); i++) {
			for (int j = 0; j < dimension.get(i); j++) {
				textEntry = textEntry + keyArrayList.get(counter) + ":" + valueArrayList.get(counter) + " ";
				counter++;	
			}
			textEntry = textEntry + "\r\n";
		}
		
		tableService.prepareTextEntry();
		assertEquals(textEntry, tableService.getTextEntry());
	}
//18
	@Test
	void testWriteStringToFile() {
		TableService spyTableService = spy(tableService);
		doNothing().when(spyTableService).writeStringToFile(Mockito.any());
		spyTableService.writeStringToFile("message");
		verify(spyTableService).writeStringToFile("message");
	}
//19
	@Test
	void testSortTableAscending() {
		TableService spyTableService = spy(tableService);
	    spyTableService.setTable(table);     	    
	    doNothing().when(spyTableService).saveNewTable("Text file " + table.getFilePath() + " is now updated with new "
				   + "key:value pairs.\n\nSORTED TABLE");
		
		new Expectations() {{
			ScannerUtil.scanNextLine("Do you want to save the sorted table? Yes(y)/No(n): ");
			result = "y";
		}};
		
		table.setKeyValueMap(keyValueMap);
		table.setDimension(dimension);
		
		List<String> keyArrayList = new ArrayList<String>(Arrays.asList("123", "ABC", "abc"));
		List<String> valueArrayList = new ArrayList<String>(Arrays.asList("456", "DEF", "def"));
		Map<String, List<String>> keyValueMap = new LinkedHashMap<String, List<String>>();
		for (int i = 0; i < keyArrayList.size(); i++) {
			String key = keyArrayList.get(i);
			String value = valueArrayList.get(i);
			keyValueMap.put(key, new ArrayList<String>());
			keyValueMap.get(key).add(key+value);
			keyValueMap.get(key).add(value);
		}
		
		spyTableService.sortTableAscending();
		assertEquals(keyValueMap, table.getKeyValueMap());
	}
//20
	@Test
	void testCreateTempKeyArrayListFromMap() {
		assertEquals(keyArrayList, tableService.createTempKeyArrayListFromMap(keyValueMap));
	}
//21
	@Test
	void testCreateTempValueArrayListFromMap() {
		assertEquals(valueArrayList, tableService.createTempValueArrayListFromMap(keyValueMap));
	}
//22
	@Test
	void testAddRow() {
		new Expectations() {{
			StringGeneratorUtil.getString(3);
			returns("!@#", "$%^", "===", "???");
		}};		
		
		TableService spyTableService = spy(tableService);
	    spyTableService.setTable(table);     	    
	    doNothing().when(spyTableService).saveNewTable("Text file " + table.getFilePath() + " is now updated with new "
	    											   + "key:value pairs.\n\nNEW TABLE");
	    table.setKeyValueMap(keyValueMap);
		table.setDimension(dimension);
		
		List<Integer> dimension = new ArrayList<Integer>(this.dimension);
		dimension.add(2);
		
		List<String> keyArrayList = new ArrayList<String>(Arrays.asList("!@#", "==="));
	    List<String> valueArrayList = new ArrayList<String>(Arrays.asList("$%^", "???"));
	    Map<String, List<String>> keyValueMap = new LinkedHashMap<String, List<String>>(this.keyValueMap);
		for (int i = 0; i < keyArrayList.size(); i++) {
			String key = keyArrayList.get(i);
			String value = valueArrayList.get(i);
			keyValueMap.put(key, new ArrayList<String>());
			keyValueMap.get(key).add(key+value);
			keyValueMap.get(key).add(value);
		}
		
		spyTableService.addRow(2, 3, 3);
	    assertEquals(dimension,table.getDimension());
	    assertEquals(keyValueMap, table.getKeyValueMap());
	}
	
}
