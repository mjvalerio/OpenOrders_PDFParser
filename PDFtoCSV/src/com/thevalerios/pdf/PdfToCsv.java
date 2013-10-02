package com.thevalerios.pdf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import com.itextpdf.text.io.RandomAccessSourceFactory;
import com.itextpdf.text.pdf.PRTokeniser;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.RandomAccessFileOrArray;

public class PdfToCsv {

	public static void main(String[] args) {
		PdfToCsv pdfToCsv = new PdfToCsv();
		pdfToCsv.processFolder("C:\\Users\\mvalerio\\Documents\\OLAttachments");

	}

	public void processFolder(String folder) {
		File dir = new File(folder);
		for (File child : dir.listFiles()) {

			if (child.isFile() && child.getName().endsWith(".pdf")) {
				String csvOutputFile = folder.concat("\\superFile.csv");
				System.out.println(child.getAbsoluteFile());
				try {
					this.parsePdf(child.getAbsolutePath(), csvOutputFile);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
		}
	}


	public void parsePdf(String src, String dest) throws IOException {
        PdfReader reader = new PdfReader(src);
        // we can inspect the syntax of the imported page
        byte[] streamBytes = reader.getPageContent(1);
        PRTokeniser tokenizer = new PRTokeniser(new RandomAccessFileOrArray(new RandomAccessSourceFactory().createSource(streamBytes)));
        PrintWriter out = new PrintWriter(new FileOutputStream(dest,true));
        while (tokenizer.nextToken()) {
            if (tokenizer.getTokenType() == PRTokeniser.TokenType.STRING) {
                out.println(tokenizer.getStringValue());
            }
        }
        out.flush();
        out.close();
        reader.close();
    }
}
