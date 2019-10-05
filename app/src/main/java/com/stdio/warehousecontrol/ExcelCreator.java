package com.stdio.warehousecontrol;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.stdio.warehousecontrol.gmailHelper.GMailSender;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;

public class ExcelCreator {

    static Context mContext;
    static Activity mActivity;
    static String recipient = "kwork-stdio@mail.ru";

    public static void createExcelFile(Context context, Activity activity) throws ParseException {

        mContext = context;
        mActivity = activity;

        // создание самого excel файла в памяти
        XSSFWorkbook workbook = new XSSFWorkbook();
        // создание листа с названием "Просто лист"
        XSSFSheet sheet = workbook.createSheet("List");

        // счетчик для строк
        int rowNum = 0;

        // создаем подписи к столбцам (это будет первая строчка в листе Excel файла)
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue("Артикул");
        row.createCell(1).setCellValue("Штрих-код");
        row.createCell(2).setCellValue("Наименование");
        row.createCell(3).setCellValue("Количество");
        row.createCell(4).setCellValue("Размер");

        // заполняем лист данными
        for (DataModel dataModel : MainActivity.list) {
            createSheetHeader(sheet, ++rowNum, dataModel);
        }

        // записываем созданный в памяти Excel документ в файл
        File file = new File(context.getExternalFilesDir(null), "WarehouseControl.xlsx");
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(file);
            workbook.write(os);
            Log.w("FileUtils", "Writing file" + file);
        } catch (IOException e) {
            Log.w("FileUtils", "Error writing " + file, e);
        } catch (Exception e) {
            Log.w("FileUtils", "Failed to save file", e);
        } finally {
            try {
                if (null != os)
                    os.close();
            } catch (Exception ex) {
            }
        }
        sendMessage(file);
    }

    // заполнение строки (rowNum) определенного листа (sheet)
    // данными  из dataModel созданного в памяти Excel файла
    private static void createSheetHeader(XSSFSheet sheet, int rowNum, DataModel dataModel) {
        Row row = sheet.createRow(rowNum);

        row.createCell(0).setCellValue(dataModel.article);
        row.createCell(1).setCellValue(dataModel.barcode);
        row.createCell(2).setCellValue(dataModel.name);
        row.createCell(3).setCellValue(dataModel.count);
        row.createCell(4).setCellValue(dataModel.address);
    }

    private static void sendMessage(final File excelFile) {
        final ProgressDialog dialog = new ProgressDialog(mContext);
        dialog.setTitle("Sending Email");
        dialog.setMessage("Please wait");
        dialog.show();
        Thread sender = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    GMailSender sender = new GMailSender("warehousecontrolapp@gmail.com", "yourpassword");
                    sender.sendMail("Cкладской учет",
                            excelFile,
                            "warehousecontrolapp@gmail.com",
                            recipient);
                    dialog.dismiss();
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext, "Excel файл успешно создан " + excelFile + " \nи отправлен по адресу " + recipient, Toast.LENGTH_LONG).show();
                        }
                    }); {

                    }
                } catch (Exception e) {
                    Log.e("mylog", "Error: " + e.getMessage());
                }
            }
        });
        sender.start();
    }
}