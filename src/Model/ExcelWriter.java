package Model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import jxl.CellView;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.UnderlineStyle;
import jxl.write.Formula;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;


public class ExcelWriter {

	private WritableCellFormat timesBoldUnderline;
	private WritableCellFormat times;
	private String inputFile;
	private ArrayList<ArrayList<ArrayList<Result<Integer, Double, Integer, Integer>>>> results; //run -> gen -> pop#,fit,turb#

	public void setResults(ArrayList<ArrayList<ArrayList<Result<Integer, Double, Integer, Integer>>>> results) {
		this.results = results;
	}

	public void setOutputFile(String inputFile) {
		this.inputFile = inputFile;
	}

	public void write() throws IOException, WriteException {
		File file = new File(inputFile);
		WorkbookSettings wbSettings = new WorkbookSettings();
		wbSettings.setLocale(new Locale("en", "EN"));

		WritableWorkbook workbook = Workbook.createWorkbook(file, wbSettings);

		int runNo = 0;
		for (ArrayList<ArrayList<Result<Integer, Double, Integer, Integer>>> run : results){ //30 runs
			workbook.createSheet("Run "+runNo, runNo);
			WritableSheet excelSheet = workbook.getSheet(runNo);
			createHeaders(excelSheet);

			createResults(excelSheet, run);
			runNo++;
		}

		workbook.write();
		workbook.close();
	}

	private void createHeaders(WritableSheet sheet)
			throws WriteException {
		
        // Lets create a times font
        WritableFont times10pt = new WritableFont(WritableFont.TIMES, 10);
        // Define the cell format
        times = new WritableCellFormat(times10pt);
        // Lets automatically wrap the cells
        times.setWrap(true);

        // create create a bold font with unterlines
        WritableFont times10ptBoldUnderline = new WritableFont(
                        WritableFont.TIMES, 10, WritableFont.BOLD, false,
                        UnderlineStyle.SINGLE);
        timesBoldUnderline = new WritableCellFormat(times10ptBoldUnderline);
        // Lets automatically wrap the cells
        timesBoldUnderline.setWrap(true);

        CellView cv = new CellView();
        cv.setFormat(times);
        cv.setFormat(timesBoldUnderline);
        cv.setAutosize(true);
        
        
		addCaption(sheet, 0, 0, "Generation");
		addCaption(sheet, 1, 0, "Population");
		addCaption(sheet, 2, 0, "Fitness");
		addCaption(sheet, 3, 0, "Turbines");
		addCaption(sheet, 4, 0, "Children survived");


	}

	private void createResults(WritableSheet sheet, ArrayList<ArrayList<Result<Integer, Double, Integer, Integer>>> gens) throws WriteException,
	RowsExceededException {

		int row = 1;
		int genNo = 1;
		for (ArrayList<Result<Integer, Double, Integer, Integer>> gen : gens){ //50+ gen
			for (Result<Integer, Double, Integer, Integer> pop : gen){
				//gen,pop#,fit,turb#
				writeCell(sheet, 0, row, genNo);
				writeCell(sheet, 1, row, pop.x+1);
				writeCellD(sheet, 2, row, pop.y);
				writeCell(sheet, 3, row, pop.z);
				writeCell(sheet, 4, row, pop.a);
				row++;
			}
			genNo++;

		}


//		// Lets calculate the sum of it
//		StringBuffer buf = new StringBuffer();
//		buf.append("SUM(A2:A10)");
//		Formula f = new Formula(0, 10, buf.toString());
//		sheet.addCell(f);
//		buf = new StringBuffer();
//		buf.append("SUM(B2:B10)");
//		f = new Formula(1, 10, buf.toString());
//		sheet.addCell(f);
	}

	private void addCaption(WritableSheet sheet, int column, int row, String s)
			throws RowsExceededException, WriteException {
		Label label;
		label = new Label(column, row, s, timesBoldUnderline);
		sheet.addCell(label);
	}

	private void writeCell(WritableSheet sheet, int column, int row, Integer integer) throws WriteException, RowsExceededException {
		Number number;
		number = new Number(column, row, integer, times);
		sheet.addCell(number);
	}
	
	private void writeCellD(WritableSheet sheet, int column, int row, Double dub) throws WriteException, RowsExceededException {
		Number number;
		number = new Number(column, row, dub, times);
		sheet.addCell(number);
	}

	private void addLabel(WritableSheet sheet, int column, int row, String s) throws WriteException, RowsExceededException {
		Label label;
		label = new Label(column, row, s, times);
		sheet.addCell(label);
	}

}