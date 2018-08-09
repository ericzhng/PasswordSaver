package com.aero.umich.passwordsaver.utils_xls;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.aero.umich.passwordsaver.R;
import com.aero.umich.passwordsaver.database.PasswordContract;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.aero.umich.passwordsaver.utils_xls.DateUtils.toTimestamp;

public
class ExcelFileInstance {

    String TAG = ExcelFileInstance.class.getSimpleName();

    // resources
    int res_id = R.raw.password;

    // xls file info
    int nrowsdata, ncols;
    Context mContext;

    // header info
    public static List<String> columnsInfo = new ArrayList<String>();
    public static List<ContentValues> mPasswordArray = new ArrayList<ContentValues>();


    public
    ExcelFileInstance(Context context) {
        mContext = context;
    }


    // if table contract changes, only needs to change here the order of strValues
    private
    void insertContentValues(String[] strValues) {

        Date date = new Date();

        PasswordData password = new PasswordData(strValues[1], strValues[2],
                strValues[3], strValues[4], strValues[5],
                (int) Math.round(Double.parseDouble(strValues[6])), toTimestamp(date));

        mPasswordArray.add(password.convertContentValues());
    }

    // Helper methods
    public
    void ReadXlsFile() {

        LogDebugInfo("reading xlsx file from resources");

        InputStream mStream = mContext.getResources().openRawResource(res_id);

        try {
            // we create an XSSF Workbook object for our XLSX Excel File
            XSSFWorkbook workbook = new XSSFWorkbook(mStream);

            // we get first sheet
            XSSFSheet sheet = workbook.getSheetAt(0);

            // we iterate on rows
            int rows = sheet.getPhysicalNumberOfRows();
            nrowsdata = rows - 1;

            FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();

            // description row
            Row row = sheet.getRow(0);
            ncols = row.getPhysicalNumberOfCells();

            for (int c = 0; c < ncols; c++) {
                String strValue = getCellAsString(row, c, formulaEvaluator);
                columnsInfo.add(strValue);
            }

            // data rows
            String[] strValues = new String[ncols];

            mPasswordArray.clear();
            for (int r = 1; r < nrowsdata; r++) {
                row = sheet.getRow(r);
                int cellsCount = row.getPhysicalNumberOfCells();

                assert (cellsCount >= ncols);

                for (int k = 0; k < ncols; k++) {
                    strValues[k] = getCellAsString(row, k, formulaEvaluator);
                }

                //----------------------------------------------------//
                // insert in contentvalues
                insertContentValues(strValues);
                //----------------------------------------------------//

            }
        } catch (Exception e) {
            /* proper exception handling to be here */
            LogDebugInfo(e.toString());
            e.printStackTrace();
        }
    }


    protected
    String getCellAsString(Row row, int c, FormulaEvaluator formulaEvaluator) {
        String value = "";
        try {
            Cell cell = row.getCell(c);

            CellValue cellValue = formulaEvaluator.evaluate(cell);

            switch (cellValue.getCellType()) {

                case Cell.CELL_TYPE_BOOLEAN:
                    value = "" + cellValue.getBooleanValue();
                    break;

                case Cell.CELL_TYPE_NUMERIC:
                    double numericValue = cellValue.getNumberValue();

                    if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        double date = cellValue.getNumberValue();
                        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");
                        value = formatter.format(HSSFDateUtil.getJavaDate(date));
                    } else {
                        String str = String.format("%.0f", numericValue);
                        value = "" + str;
                    }
                    break;
                case Cell.CELL_TYPE_STRING:
                    value = "" + cellValue.getStringValue();
                    break;
                default:
                    break;
            }
        } catch (NullPointerException e) {
            LogDebugInfo(e.toString());
        }
        return value;
    }


    private
    void LogDebugInfo(String string) {

        Log.d(TAG, string + "\n");

    }


    // write to xls file

    public
    void attachResultsToRows(XSSFSheet sheet, List<PasswordData> pwDataInput) {

        int nLen = pwDataInput.size();

        // Create header row first
        Row headerRow = sheet.createRow(0);

        if (columnsInfo.size() == 0) {
            columnsInfo.add("ID");
            columnsInfo.add("Account");
            columnsInfo.add("Username");
            columnsInfo.add("Profile email");
            columnsInfo.add("Password");
            columnsInfo.add("Description");
            columnsInfo.add("Level");
        }

        for (int i = 0; i < columnsInfo.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnsInfo.get(i));
        }

        for (int k = 0; k < nLen; k++) {
            Row row = sheet.createRow(k + 1);
            PasswordData data = pwDataInput.get(k);
            row.createCell(0).setCellValue(k + 1);
            row.createCell(1).setCellValue(data.AccountInfo);
            row.createCell(2).setCellValue(data.Username);
            row.createCell(3).setCellValue(data.Email);
            row.createCell(4).setCellValue(data.Password);
            row.createCell(5).setCellValue(data.Description);
            row.createCell(6).setCellValue(data.securityLevel);
        }
    }


    public
    void WriteXlsFile(String OutputFileName, Cursor mCursor) {

        List<PasswordData> pwDataInput = new ArrayList<PasswordData>();

        // Indices for the _id, description, and priority columns
        int idIndex = mCursor.getColumnIndex(PasswordContract.PasswordEntry._ID);
        int IndexAccountInfo = mCursor.getColumnIndex(PasswordContract.PasswordEntry.COLUMN_ACCOUNTINFO);
        int IndexUserName = mCursor.getColumnIndex(PasswordContract.PasswordEntry.COLUMN_USERNAME);
        int IndexEmail = mCursor.getColumnIndex(PasswordContract.PasswordEntry.COLUMN_EMAIL);
        int IndexPassword = mCursor.getColumnIndex(PasswordContract.PasswordEntry.COLUMN_PASSWORD);
        int IndexSecurityLevel = mCursor.getColumnIndex(PasswordContract.PasswordEntry.COLUMN_SECURITY);
        int IndexDescription = mCursor.getColumnIndex(PasswordContract.PasswordEntry.COLUMN_DESCRIPTION);

        // get to the right location in the cursor
        int size = mCursor.getCount();
        pwDataInput.clear();
        for (int k = 0; k < size; k++) {
            mCursor.moveToPosition(k);
            /* Read date from the cursor */
            final int id = mCursor.getInt(idIndex);
            String accountInfo = mCursor.getString(IndexAccountInfo);
            String userName = mCursor.getString(IndexUserName);
            String emailAddress = mCursor.getString(IndexEmail);
            String password = mCursor.getString(IndexPassword);
            int securityLevel = mCursor.getInt(IndexSecurityLevel);
            String description = mCursor.getString(IndexDescription);

            pwDataInput.add(new PasswordData(accountInfo, userName, emailAddress, password,
                    description, securityLevel, toTimestamp(new Date())));
        }

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet(WorkbookUtil.createSafeSheetName("sheetmy"));

        attachResultsToRows(sheet, pwDataInput);

        // 1
        File cacheDir = mContext.getCacheDir(); //may produce NullPointer
        File outFile = new File(cacheDir, OutputFileName);

        // 2
        // File path = new File(mContext.getFilesDir(),"PasswordAppDir");
        // if(!path.exists()){
        //     path.mkdir();
        // }
        // File outFile = new File(path, OutputFileName);

        if (isExternalStorageWritable()) {
            File path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS);
            path.mkdirs();
            outFile = new File(path, OutputFileName);
        }

        //Log.e("getAlbumStorageDir", "Directory not created");


        try {
            OutputStream outputStream = new FileOutputStream(outFile);
            workbook.write(outputStream);
            outputStream.flush();
            outputStream.close();

            LogDebugInfo("sharing file...");

        } catch (Exception e) {
            LogDebugInfo(e.toString());
        }
    }


    /* Checks if external storage is available for read and write */
    public
    boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public
    boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public
    File getPublicAlbumStorageDir(String fileName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), fileName);
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory not created");
        }
        return file;
    }
}
