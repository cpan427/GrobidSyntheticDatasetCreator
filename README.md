<h1> Introduction </h1>

The GrobidSyntheticDatasetCreator program creates a set of synthetically-created TEI XML articles with equivalent PDF files. This set of files are meant to provide additional data to improve grobid's performance. Grobid is a "machine learning library for extracting, parsing and re-structuring raw documents such as PDF into structured XML/TEI encoded documents with a particular focus on technical and scientific publications." To learn more about grobid, you can read its <a href="https://grobid.readthedocs.io/en/latest/">documentation</a>.

The program takes TEI XML files as inputs and then creates new TEI XML files with corresponding PDFs. This code serves as a proof-of-concept that one can synthetically create datasets for grobid with existing sets of TEI files. Thus, it only focuses on creating the paragraph, list, and table elements. 

<h1> Setup & Dependencies</h1>

This program can be run from the commandline with the FrankenArticleGenerator class. In terms of major dependencies, it uses the <a href="https://pdfbox.apache.org/">PDFBox</a> and Boxable libraries. (Boxable is an extention of the PDFBox library.) 

One way to integrate <a href="https://pdfbox.apache.org/">PDFBox</a> is download the relevant JAR files (i.e., pdfbox-app, preflight-app, pdfbox, fontbox, preflight, xmpbox, and pdfbox-tools) and include them when building the FrankenArticleGenerator class. A <a href="https://jar-download.com/artifact-search/boxable">JAR file</a> of <a href="https://github.com/dhorions/boxable">Boxable</a> can also be downloaded. (One can also include these libraries through Maven, although this would require converting this program into a Maven project.) 

In addition, one has to supply the TEI XML datasets that will be used to generate synthetic datasets. All default file paths appear within the beginning of the FrankenArticleGenerator class.

<h1> Known Issues & Areas for Further Improvement </h1>

- Existing TEI XML corpus tables TEI XML doesn't quite match other standards, making it impossible to figure out what information should take which cells (especially when the cells become irregularly sized). Thus, the synthetic table generator creates a table with the parsed information, although not necessarily in the order that it originally appears.
- Tables sometime overlap with other elements and/or span multiple pages.
