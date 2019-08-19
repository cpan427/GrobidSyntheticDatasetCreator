import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.form.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.util.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class FrankenArticleGenerator {

	// Here are the main inputs requested from the command line. This creates defaults of these inputs when a user doesn't input the arguments.
	private static int nbRequestedArticles = 5;
	private static String inputTeiFolderPath = "inputs/teiArticles";
	private static String pdfOutputFolderPath = "outputs/pdf";
	private static String teiOutputFolderPath = "outputs/tei";

	/*	The user is going to create random franken-articles using this program.
		For now, let's just generate random articles (which include both the PDFs and TEI files).
	 */
	public static void main (String args[]) throws IOException {
		
		if (args.length < 1) {
			System.out.println("You're using the default parameters!");	
		} else {
	
			Integer nbRequestedArticles = Integer.valueOf(args[0]);
	
			System.out.print("Number of articles requested: ");
			System.out.println(Integer.toString(nbRequestedArticles));
	
			if (args.length > 2) pdfOutputFolderPath = args[1];
			if (args.length > 3) inputTeiFolderPath = args[2];
			if (args.length > 4) pdfOutputFolderPath = args[3];
			if (args.length > 5) teiOutputFolderPath = args[4];
			
		}
		
		ensureDirectoriesExist();
		
		HashMap<String, ArrayList<String>> examples = TeiFileParser.parseFiles(inputTeiFolderPath);
		
		// TODO: Need to figure out how to get the input and output paths into the function.		
	
		for (int i = 1; i <= nbRequestedArticles; i++) {
			generateFrankenArticle(i, pdfOutputFolderPath, examples);
		}
		
		System.out.print("Successfully created ");
		System.out.print(Integer.toString(nbRequestedArticles));
		System.out.print(" franken-articles.");
	}
	
	
	/* Creating the beginning of the TEI file, which consists of:
	    <tei xmlns:xlink="http://www.w3.org/1999/xlink">
			<teiHeader/>
			<fileDesc xml:id="0"/>
			<text>
				...
			</text>
	 	</tei>
	 */
	private static void populateTeiFileBeginning(Document teiDoc) {
		
		// Adding the " <tei xmlns:xlink="http://www.w3.org/1999/xlink"> " portion
		Element teiDec = teiDoc.createElement("tei");
		teiDoc.appendChild(teiDec);
		Attr currAttr = teiDoc.createAttribute("xmlns:xlink");
		currAttr.setValue("http://www.w3.org/1999/xlink");
		teiDec.setAttributeNode(currAttr);
		
		// Adding the teiHeader node
		Element teiHeaderNode = teiDoc.createElement("teiHeader");
		teiDec.appendChild(teiHeaderNode);
		
		// Adding the fileDesc node
		Element fileDescNode = teiDoc.createElement("fileDesc");
		teiDec.appendChild(fileDescNode);
		currAttr = teiDoc.createAttribute("xml:id");
		currAttr.setValue("0");
		fileDescNode.setAttributeNode(currAttr);
		
		// Adding the text node
		Element textNode = teiDoc.createElement("text");
		teiDec.appendChild(textNode);
		
		
		
	}
	
	/*	This code generates a random franken-article.
	
	*/
	private static void generateFrankenArticle(int number, String folderPath, HashMap<String, ArrayList<String>> examples) throws IOException {
		
		
		//Opening documents
		PDDocument pdfDocument = new PDDocument();
		PdfCreator pdfCreator = new PdfCreator(pdfDocument);

		String fileName = generateFileName(number, true);
		String teiPath = teiOutputFolderPath + fileName + ".tei";
		checkIfFileExists(teiPath);
		File teiFile = new File(teiPath);
		if (!teiFile.createNewFile()) {
			System.out.println("Hmm.... something went wrong. Path name is: " + teiPath);
			return;
		}
		
		//Setting up the tei file (OLD VERSION)
		
		// Open TEI File
		DocumentBuilderFactory dbFactory =
		         DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = null;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		Document teiDoc = dBuilder.newDocument();
		populateTeiFileBeginning(teiDoc);
		
				
		
		//Adding the elements into to the files
		while (Math.random() > 0.1) {
			addRandomElements(pdfDocument, teiDoc, examples, pdfCreator);
		}

		
		// write the content into xml file
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
	        Transformer transformer;
			transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(teiDoc);
	        StreamResult result = new StreamResult(teiFile);
	        transformer.transform(source, result);
	        
		} catch ( TransformerException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		
		// Saving and closing the pdf file
		try {
			String path = pdfOutputFolderPath + fileName + ".pdf";
			pdfCreator.closeDocument(path);
		}
		catch (Exception e) {
			System.out.println(e);
		}	
	}
	
	private static final PDFont FONT = PDType1Font.TIMES_ROMAN;
    private static final float FONT_SIZE = 12;
	
	private static void addRandomElements(PDDocument doc, Document teiDoc,
			HashMap<String, ArrayList<String>> examples, 
			PdfCreator pdfCreator) throws IOException {
				
		
		Random r = new Random();
		int chosenNumber = r.nextInt(11);
		
		Element textNode = (Element) teiDoc.getElementsByTagName("text").item(0);

		// Add paragraph:
		if (chosenNumber < 9) {
			addParagraphExample(examples, doc, teiDoc, r, textNode, pdfCreator);
			return;
		}
		
		// Add a list
		else if (chosenNumber == 9) {
			addListExample(examples, doc, teiDoc, r, textNode, pdfCreator);
		} 
		
		// Add a table
		else if (chosenNumber == 10){
			addTableExample(examples, doc, teiDoc, r, textNode, pdfCreator);
		}
	}
	
	private static void addParagraphExample(HashMap<String, ArrayList<String>> examples, 
			PDDocument doc, Document teiDoc, Random r, Element textNode, PdfCreator pdfCreator) throws IOException {
		System.out.println("We're trying this out!");
		int paragraphSelection = examples.get("p").size();
		int arrNum = r.nextInt(paragraphSelection);
		String teiData = examples.get("p").get(arrNum);
		
		// Writing to the PDF file
		pdfCreator.insertParagraphIntoPDF(teiData);

		// Writing to the TEI file
		Element currPara = teiDoc.createElement("p");
		currPara.appendChild(teiDoc.createTextNode(teiData));
		textNode.appendChild(currPara);
	}
	
	private static void addListExample(HashMap<String, ArrayList<String>> examples, 
			PDDocument doc, Document teiDoc, Random r, Element textNode, PdfCreator pdfCreator) throws IOException {
		int arrNum = r.nextInt(examples.get("list").size());
		
		String rawData = examples.get("list").get(arrNum);
		ArrayList<String> pdfList = parseList(rawData);
		
		// Writing to the TEI file
		Element currPara = teiDoc.createElement("list");
		for (int i = 0; i < pdfList.size(); i++) {
			Element listItem = teiDoc.createElement("item");
			listItem.appendChild(teiDoc.createTextNode(pdfList.get(i)));
			currPara.appendChild(listItem);
		}
		textNode.appendChild(currPara);

		// Writing to the PDF file
		pdfCreator.insertListIntoPDF(pdfList);
	}
	
	private static void addTableExample(HashMap<String, ArrayList<String>> examples, 
			PDDocument doc, Document teiDoc, Random r, Element textNode, PdfCreator pdfCreator) throws IOException {
		
		int arrNum = r.nextInt(examples.get("table").size());
		String teiData = examples.get("table").get(arrNum);
		
		// Writing to the TEI file
		Element currTable = teiDoc.createElement("table");
		Attr currAttr = teiDoc.createAttribute("orientation");
		currAttr.setValue("portrait");
		currTable.setAttributeNode(currAttr);
		currAttr = teiDoc.createAttribute("type");
		currAttr.setValue("table");
		currTable.setAttributeNode(currAttr);
		currTable.appendChild(teiDoc.createTextNode(teiData));
		textNode.appendChild(currTable);
		
		// Writing to the PDF
		ArrayList<String> teiTableList = parseList(PdfCreator.convertUnicodeToAscii(teiData));
		pdfCreator.insertTableIntoPDF(teiTableList);
	}
	
	private static ArrayList<String> parseList(String text) {
		ArrayList<String> parsedList = new ArrayList<String>();
		System.out.println("\n\nWe're doing a list here!");
		int nextItemIndex = -1;
		int endOfItemIndex = -1;
		System.out.println(text);
		do {
			nextItemIndex = text.indexOf("[item]", nextItemIndex) + 6;
			endOfItemIndex = text.indexOf("[/item]", nextItemIndex);
			if (nextItemIndex != (-1 + 6)) parsedList.add(text.substring(nextItemIndex, endOfItemIndex));
		} while (nextItemIndex != (-1 + 6));
		System.out.println("List Size:" + parsedList.size());
		return parsedList;
	}
	
	
	private static void ensureDirectoriesExist() {
		ensureDirectoryExists(inputTeiFolderPath);
		ensureDirectoryExists(pdfOutputFolderPath);
		ensureDirectoryExists(teiOutputFolderPath);
	}
	
	private static void ensureDirectoryExists(String path) {
		File f = new File(path);
		
		if (!f.exists()) {
			f.mkdirs();
		}
	}
	
	private static void checkIfFileExists(String path) throws IOException {
		File f = new File(path);
		
		if (f.isFile()) {
			throw new IOException(path + " already exists! Clear out your directory.");
		}
	}

	private static void getInputtedArgs(String args[]) {

		printArgumentResults();
	}

	/* Prints arguments used for the program.
	 */

	private static void printArgumentResults() {
		System.out.println("Starting to Execute FrankenArticleGenerator\n");
		System.out.println("Number of requested articles: " + nbRequestedArticles);
		System.out.println("Input Folder Paths: ");
		System.out.println("\t Input TEI Articles Path: " + inputTeiFolderPath);
		System.out.println("Output Folder Paths: ");
		System.out.println("\t Output Synthetic TEI Articles Path: " + teiOutputFolderPath);
		System.out.println("\t Output Synthetic PDF Articles Path: " + pdfOutputFolderPath);
	}
	
	
	private static String generateFileName(int number, boolean isTeiFile) {
		String res = Integer.toString(number);

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		res = "/" + calendar.get(Calendar.YEAR) + "_" + calendar.get(Calendar.MONTH) + "_" + calendar.get(Calendar.DAY_OF_MONTH) + "__" + 
				calendar.get(Calendar.HOUR_OF_DAY) + "_" + calendar.get(Calendar.MINUTE) + "_" + calendar.get(Calendar.SECOND) + "_file_" + res;
	
		return res;
	}
}
