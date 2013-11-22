package com.thevalerios.pdf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.itextpdf.text.io.RandomAccessSourceFactory;
import com.itextpdf.text.pdf.PRTokeniser;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.RandomAccessFileOrArray;

public class PdfToCsv {
	private boolean columnsWritten = false;
	private Map<String, String> repMap = null;
	private Map<String, String> entryMap = null;
	private int entryCounter = 0;

	public static void main(String[] args) {
		PdfToCsv pdfToCsv = new PdfToCsv();
		pdfToCsv.processFolder("C:\\Users\\mvalerio\\Documents\\OLAttachments");

	}

	public PdfToCsv() {
		super();
		this.columnsWritten = false;
		this.entryMap = new HashMap<String, String>();
		this.repMap = new HashMap<String, String>();
		this.repMap.put("MML", "2030");
		this.repMap.put("AAH", "2040");
		this.repMap.put("ATM", "2030");
		this.repMap.put("BAM", "2021");
		this.repMap.put("BRH", "2055");
		this.repMap.put("GSK", "2046");
		this.repMap.put("JLW", "2030");
		this.repMap.put("JMN", "2021");
		this.repMap.put("JRF", "2021");
		this.repMap.put("JRM", "2021");
		this.repMap.put("JRM2", "2040");
		this.repMap.put("KRB", "2055");
		this.repMap.put("KSH", "2021");
		this.repMap.put("PCA1", "2021");
		this.repMap.put("PDR", "2030");
		this.repMap.put("PJD", "2040");
		this.repMap.put("RHH", "2012");
		this.repMap.put("RLC", "2040");
		this.repMap.put("RWS", "2021");
		this.repMap.put("SKC", "2021");
		this.repMap.put("SMD1", "2030");
		this.repMap.put("PDR", "2030");
		this.repMap.put("TDH", "2040");
		this.repMap.put("WAW", "2021");
	}

	public void processFolder(String folder) {
		File dir = new File(folder);
		String csvOutputFile = folder.concat("\\superFile.csv");
		for (File child : dir.listFiles()) {

			if (child.isFile() && child.getName().endsWith(".pdf")) {
				
				System.out.println(child.getAbsoluteFile());
				try {
					this.parsePdf(child.getAbsolutePath(), csvOutputFile);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		try {
			this.writeToCSV(csvOutputFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void parsePdf(String src, String dest) throws IOException {
		boolean inSlsRep = false;
		boolean inOrder = false;
		PdfReader reader = new PdfReader(src);
		// we can inspect the syntax of the imported page
		byte[] streamBytes = reader.getPageContent(1);
		PRTokeniser tokenizer = new PRTokeniser(new RandomAccessFileOrArray(
				new RandomAccessSourceFactory().createSource(streamBytes)));
		// PrintWriter out = new PrintWriter(new FileOutputStream(dest, true));
		// if (!this.columnsWritten){
		// out.println("CostCenter, SalesRep, OrdNum,OrdDate,OrdPercent,OrdAmount,OrdCost,OrdGP,OrdShipDate,OrdCustName,OrdCustPO,SrcFile");
		// this.columnsWritten=true;
		// }

		String curSlsRep = null;
		String curCostCenter = null;
		while (tokenizer.nextToken()) {
			if (tokenizer.getTokenType() == PRTokeniser.TokenType.STRING) {
				String token = tokenizer.getStringValue();
				// System.out.println("token=" + token);
				if (token.startsWith("For Slsm:")) {
					inSlsRep = true;
//					System.out.println("new sales person");
					curSlsRep = token.substring(token.indexOf(":") + 2,
							token.length()).trim();
					curCostCenter = this.repMap.get(curSlsRep.substring(0,
							curSlsRep.indexOf(" ")));
					if (curCostCenter == null) {
						curCostCenter = "";
					}
					// System.out.println("curSlsRep=" + curSlsRep);
				} else if (token.startsWith("Order#")) {
					inOrder = true;
				} else if (token.startsWith("Slsm Totals:")) {
					inSlsRep = false;
				} else if (token.startsWith("Order Taken By:")) {
					// inOrder=false;
				} else if (token.startsWith("PO#")) {

				} else if (token.startsWith("Remarks:")) {
				} else if (token.contains("----")) {

				} else {
					if (inSlsRep && inOrder) {
						String[] items = token.split("\\s+");
						String ordCustName = new String();
						if (items.length > 6) {
//							System.out.println(items.length);
							String orderNum = items[0];
							if (!this.entryMap.containsKey(orderNum)) {
								System.out.println(orderNum + " is being added to the entry Map");
								this.entryCounter++;
								System.out.println("Entry Counter = " + this.entryCounter);
								String ordDate = items[1];
								String ordPercent = items[2];
								String ordAmount = items[3];
								String ordCost = items[4];
								String ordGP = items[5];
								String ordShipDate = null;
								int i = 0;
								if (items.length > 9) {
									ordShipDate = items[6];
									if (!isValidDate(ordShipDate)) {
										ordCustName = ordCustName
												.concat(ordShipDate + " ");
										ordShipDate = "";
									}
									i = 7;
								} else {
									ordShipDate = "";
									i = 6;
								}

								while (i < items.length - 1) {
									ordCustName = ordCustName.concat(items[i]
											+ " ");
									i++;
								}
								String custPO = items[items.length - 1];
								// out.println("OrdNum,       OrdDate,         OrdPercent,     OrdAmount,          OrdCost,        OrdGP,           OrdShipDate,         OrdCustName,        OrdCustPO");
								String entry = new String("\"" + curCostCenter
										+ "\",\"" + curSlsRep + "\",\""
										+ orderNum + "\",\"" + ordDate
										+ "\",\"" + ordPercent + "\",\""
										+ ordAmount + "\",\"" + ordCost
										+ "\",\"" + ordGP + "\",\""
										+ ordShipDate + "\",\"" + ordCustName
										+ "\",\"" + custPO + "\",\"" + src
										+ "\"");
								String key = new String(orderNum);
								this.entryMap.put(key, entry);
								entry = null;
//								System.out.println("curSlsRep = " + curSlsRep);
//								System.out.println("orderNum = " + orderNum);
//								System.out.println("ordDate = " + ordDate);
//								System.out
//										.println("ordPercent = " + ordPercent);
//								System.out.println("ordAmount = " + ordAmount);
//								System.out.println("ordCost = " + ordCost);
//								System.out.println("ordGP = " + ordGP);
//								System.out.println("ordShipDate = "
//										+ ordShipDate);
//								System.out.println("ordCustName = "
//										+ ordCustName);
//								System.out.println("custPO = " + custPO);
							}
						}
					}

				}
//				System.out.println("token=" + token);
				// out.println(token);

			}
		}
		// out.flush();
		// out.close();
		reader.close();
		
	}

	private void writeToCSV(String dest) throws FileNotFoundException {
		PrintWriter out = new PrintWriter(new FileOutputStream(dest, true));
		if (!this.columnsWritten) {
			out.println("CostCenter, SalesRep, OrdNum,OrdDate,OrdPercent,OrdAmount,OrdCost,OrdGP,OrdShipDate,OrdCustName,OrdCustPO,SrcFile");
			this.columnsWritten = true;
		}
		Collection<String> values = this.entryMap.values();
		System.out.print("Number of entries = " + values.size());
		Iterator<String> iterator = values.iterator();
		int counter = 0;
		while(iterator.hasNext()){
//		for (iterator = this.entryMap.values().iterator(); iterator
//				.hasNext();) {
			String entry = (String) iterator.next();
			out.println(entry);
			counter++;
			System.out.println("-------------------------------");
			System.out.println(entry);
			System.out.println(counter);
			System.out.println("-------------------------------");
		}
		out.flush();
		out.close();
	}

	private boolean isValidDate(String dateString) {
		SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		try {
			df.parse(dateString);
			return true;
		} catch (ParseException e) {
			return false;

		}
	}

	private boolean isInteger(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch (NumberFormatException nfe) {
		}
		return false;
	}
}
