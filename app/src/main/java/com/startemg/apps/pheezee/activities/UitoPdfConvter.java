package com.startemg.apps.pheezee.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;

import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import startemg.apps.pheezee.R;

public class UitoPdfConvter extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 123;
    private ScrollView scrollView;
    private LinearLayout scrollableContent;
    private Button generatePdfButton;

    Bitmap resizedBitmap;

    EscPosPrinter printer_th;

    Bitmap decodedByte;

    PdfRenderer renderer = null;

    private TextView patient_name_rt_kt, pt_email_rt_kt, patient_id_rt_kt;

    private LineChart emgChart;
    TextView print_button, print_button_dim;

    String phizio_email_et, patient_name_et, emg_array_data_et, patient_id_et;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uito_new_convert);

        scrollView = findViewById(R.id.scroll_view);
        scrollableContent = findViewById(R.id.scrollable_content);
        generatePdfButton = findViewById(R.id.btn_generate_pdf);
        patient_name_rt_kt = findViewById(R.id.patient_name_rt);
        pt_email_rt_kt = findViewById(R.id.pt_email_rt);
        patient_id_rt_kt = findViewById(R.id.patient_id_rt);
        emgChart = findViewById(R.id.emgChart);
        print_button = findViewById(R.id.button_bluetooth);

        Intent intent = getIntent();
        phizio_email_et = intent.getStringExtra("phizio_email");
        patient_name_et = intent.getStringExtra("patient_name");
        emg_array_data_et = intent.getStringExtra("emg_array_data");
        patient_id_et = intent.getStringExtra("patientid");






        pt_email_rt_kt.setText(phizio_email_et);
        patient_name_rt_kt.setText(patient_name_et);
        patient_id_rt_kt.setText(patient_id_et);


        File pdfFile = new File(getApplicationContext().getExternalFilesDir(null), "layout.pdf");

        if (pdfFile.exists()) {
            if (pdfFile.delete()) {
                // File deleted successfully
                Log.d("File", "File deleted successfully");
            } else {
                // Failed to delete the file
                Log.d("File", "Failed to delete the file");
            }
        } else {
            // File doesn't exist
            Log.d("File", "File doesn't exist");
        }


        emgChart.getDescription().setEnabled(false);
        emgChart.setTouchEnabled(false);
        emgChart.setDragEnabled(false);
        emgChart.setScaleEnabled(false);
        emgChart.setPinchZoom(false);
        emgChart.setRotation(270);


        XAxis xAxis = emgChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        YAxis yAxis = emgChart.getAxisLeft();
        yAxis.setDrawGridLines(false);

        String data = emg_array_data_et;
        data = data.substring(1, data.length() - 1);
        String[] values = data.split(",");
        int[] emgData = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            emgData[i] = Integer.parseInt(values[i]);
        }
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < emgData.length; i++) {
            entries.add(new Entry(i, emgData[i]));
        }

        LineDataSet lineDataSet = new LineDataSet(entries, "EMG");
        lineDataSet.setColor(getResources().getColor(R.color.pitch_black));
        LineData lineData = new LineData(lineDataSet);
        // Set the LineData to the chart
        emgChart.setData(lineData);
        // Refresh the chart
        emgChart.invalidate();
        generatePdfButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

//        TextView print_button, print_button_dim;
        print_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkPermissions()) {
                    convertLayoutToPdf(scrollableContent);
                } else {
                    requestPermissions();
                }
            }
        });
    }


    private boolean checkPermissions() {
        int storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return storagePermission == PackageManager.PERMISSION_GRANTED;
    }
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_CODE);
    }

    private void convertLayoutToPdf(View view) {
        // Create a bitmap of the layout
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        // Create a file to store the PDF
        File pdfFile = new File(getApplicationContext().getExternalFilesDir(null), "layout.pdf");

        try {
            // Create a PDF document
            PdfDocument document = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);

            // Draw the bitmap onto the PDF page
            Canvas pdfCanvas = page.getCanvas();
            pdfCanvas.drawBitmap(bitmap, 0, 0, null);

            // Finish the page and save the document
            document.finishPage(page);
            document.writeTo(new FileOutputStream(pdfFile));
            document.close();

            printing_function();

            // Show a success message
            Toast.makeText(getApplicationContext(), "PDF saved successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printing_function() {

        try {
            renderer = new PdfRenderer(ParcelFileDescriptor.open(new File(getApplicationContext().getExternalFilesDir(null), "layout.pdf"), ParcelFileDescriptor.MODE_READ_ONLY));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Create an array to hold the bitmaps
        Bitmap[] bitmaps = new Bitmap[renderer.getPageCount()];
        int i ;
        // Render each page into a bitmap
        for (i = 0; i < renderer.getPageCount(); i++) {
            PdfRenderer.Page page = renderer.openPage(i);
            Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
//            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT);

            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
            Paint paint = new Paint();
            paint.setColorFilter(filter);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(bitmap, 0, 0, paint);


//            ColorMatrix matrix = new ColorMatrix();
//            matrix.setSaturation(0);
//            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
//            Paint paint = new Paint();
//            paint.setColorFilter(filter);
//            Canvas canvas = new Canvas(bitmap);
//            canvas.drawBitmap(bitmap, 0, 0, paint);
            bitmaps[i] = bitmap;
            decodedByte = bitmap;

            try {
                printer_th = new EscPosPrinter(BluetoothPrintersConnections.selectFirstPaired(), 203, 70f, 48);
                int width = decodedByte.getWidth(), height = decodedByte.getHeight();
                StringBuilder textToPrint = new StringBuilder();
                for (int y = 0; y < height; y += 256) {
                    resizedBitmap = Bitmap.createBitmap(decodedByte, 0, y, width, (y + 256 >= height) ? height - y : 256);
                    textToPrint.append("[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer_th, resizedBitmap) + "</img>\n");
                }
                printer_th.printFormattedTextAndCut(textToPrint.toString());
                decodedByte.recycle();
                page.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Clean up resources
        renderer.close();



    }




    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                convertLayoutToPdf(scrollableContent);
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

}