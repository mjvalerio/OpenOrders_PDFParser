package com.thevalerios.pdf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

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
		boolean inSlsRep = false;
		boolean inOrder = false;
        PdfReader reader = new PdfReader(src);
        // we can inspect the syntax of the imported page
        byte[] streamBytes = reader.getPageContent(1);
        PRTokeniser tokenizer = new PRTokeniser(new RandomAccessFileOrArray(new RandomAccessSourceFactory().createSource(streamBytes)));
        PrintWriter out = new PrintWriter(new FileOutputStream(dest,true));
        while (tokenizer.nextToken()) {
            if (tokenizer.getTokenType() == PRTokeniser.TokenType.STRING) {
            	String token = tokenizer.getStringValue();
            	//System.out.println("token=" + token);
            	if (token.startsWith("For Slsm:")) {
            		inSlsRep = true;
					System.out.println("new sales person");
					String curSlsRep = token.substring(token.indexOf(":")+2,token.length());
					System.out.println("curSlsRep="+ curSlsRep);
				}
            	else if (token.startsWith("Order#")){
            		inOrder = true;
            	}
            	else if (token.startsWith("Slsm Totals:")){
            		inSlsRep = false;
            	}
            	else if (token.startsWith("Order Taken By:")) {
					inOrder=false;
				}
            	else if (token.startsWith("PO#")){
            		
            	}
            	else{
            		if (inSlsRep && inOrder) {	
            			String[] items = token.split("\\s+");
            			System.out.println(items.length);
                		System.out.println("token=" + token);
            			String ordDate = items[0];
            			String ordPercent = items[1];
            			String ordAmount = items[2];
            			String ordCost = items[3];
            			String ordGP = items[4];
            			String ordShipDate = items[5];
            			String ordCustName = new String();
            			int i = 6;
            			while (i<items.length) {
							ordCustName.concat(items[i] + " ");
							i++;
						} 
            			String custPO = items[items.length-1];
            			System.out.println("ordDate = " + ordDate);
            			System.out.println("ordPercent = " + ordPercent);
            			System.out.println("ordAmount = " + ordAmount);
            			System.out.println("ordCost = " + ordCost);
            			System.out.println("ordGP = " + ordGP);
            			System.out.println("ordShipDate = " + ordShipDate);
            			System.out.println("ordCustName = " + ordCustName);
            			System.out.println("custPO = " + custPO);
                		
					}
            		
            	}
            	out.println(token);
                
            }
        }
        out.flush();
        out.close();
        reader.close();
    }
}
