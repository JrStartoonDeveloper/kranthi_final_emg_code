package com.startemg.apps.pheezee.activities;

import static startemg.apps.pheezee.R.id.iv_back_app_info;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.startemg.apps.pheezee.pojos.BluetoothCommunication;
import com.startemg.apps.pheezee.retrofit.GetDataService;
import com.startemg.apps.pheezee.retrofit.RetrofitClientInstance;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import startemg.apps.pheezee.R;

public class BluetoothScreen extends AppCompatActivity implements OpenBluetoothSocketTask.SocketConnectionListener {

    private static final int PERMISSION_BLUETOOTH = 25;
    private static final int PERMISSION_BLUETOOTH_ADMIN = 26;
    private static final int PERMISSION_BLUETOOTH_CONNECT = 27;
    private static final int PERMISSION_BLUETOOTH_SCAN = 28;

    private static final int  REQUEST_ENABLE_BT = 29;

//    ProgressDialog report_dialog;

    Button print_thermal;
    File pdfFile;

    TextView print_button, print_button_dim;
    PDFView pdfView;
    Bitmap[] bitmaps;

    Bitmap resizedBitmap;

    String path;

    PopupWindow popup;

    Bitmap decodedByte;

    private int progress = 5;



    SwipeRefreshLayout swipeRefreshLayout;

    androidx.constraintlayout.widget.ConstraintLayout layout_status;


    PdfRenderer renderer = null;

    Runnable runnable;
    Handler handler;

    String pt_email, pt_name , pt_number;

    GetDataService getDataService;

    EscPosPrinter printer_th;

    boolean loopCompleted = false;
    boolean bluetooth_status = false;

    boolean connection_status = false;

    WebView webView;

    String phizio_email_et, patient_name_et, emg_array_data_et, patient_id_et;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_screen);
        print_button = findViewById(R.id.button_bluetooth);
        print_button_dim = findViewById(R.id.button_bluetooth_disable);
        pdfView = findViewById(R.id.pdfView);
        ImageView button1 = findViewById(iv_back_app_info);
        print_thermal = findViewById(R.id.print_thermal);
        swipeRefreshLayout = findViewById(R.id.refreshLayout);
        webView = findViewById(R.id.webview);
//        floatingActionButton = findViewById(R.id.menu_delete);
        layout_status = findViewById(R.id.layout_id);
        layout_status.setVisibility(View.GONE);
        getDataService = RetrofitClientInstance.getRetrofitInstance().create(GetDataService.class);
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        new_pheezee_printer();

        Intent intent = getIntent();
        phizio_email_et = intent.getStringExtra("phizio_email");
        patient_name_et = intent.getStringExtra("patient_name");
        emg_array_data_et = intent.getStringExtra("emg_array_data");
        patient_id_et = intent.getStringExtra("patientid");




        button1.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.progress_bar_notification);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(lp);
        ProgressBar mCustomProgressBar  = dialog.findViewById(R.id.CustomProgressBar);
        TextView fact_text = dialog.findViewById(R.id.fact_text);
        ImageView cross_button = dialog.findViewById(R.id.cross_button);
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);


        handler = new Handler();

        final Runnable r = new Runnable() {
            public void run() {
                String[] myStringArray = {"Checking joint ROM helps assess flexibility and find ways to improve.", "EMG can identify muscle weakness and imbalances in patients.", "Joint ROM exercises can improve mobility, flexibility, and prevent problems.", "EMG can show patients how to activate muscles correctly during exercises.", "Joint ROM exercises can help prevent injuries and improve sports performance."};
                Random random = new Random();
                int randomIndex = random.nextInt(myStringArray.length);
                String randomValue = myStringArray[randomIndex];
                fact_text.setText(randomValue.toString());
                handler.postDelayed(this, 3000);
            }
        };

        handler.postDelayed(r, 1000);

        handler = new Handler();

        final Runnable bt_check = new Runnable() {
            public void run() {
                if(connection_status == true) {
//                    pheezee_printer();
//                    BluetoothPrintersConnections.selectFirstPaired();
                }
                handler.postDelayed(this, 1000);
            }
        };

        handler.postDelayed(bt_check, 1000);









//        floatingActionButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                File file = new File(getApplicationContext().getExternalFilesDir(null)+"/Pheezee", patientid+date_value+".pdf");
////                Uri fileUri = FileProvider.getUriForFile(BluetoothScreen.this, getApplicationContext().getPackageName() + ".my.package.name.provider", file);
////                Intent shareIntent = new Intent(Intent.ACTION_SEND);
////                shareIntent.setType("application/pdf");
////                shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
////                startActivity(Intent.createChooser(shareIntent, "Share file"));
//            }
//        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                bluetooth_status = false;
                print_button.setVisibility(View.VISIBLE);
                print_button_dim.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        cross_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                finish();
            }
        });









        new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long l) {
//                BluetoothCommunication data = new BluetoothCommunication(pt_email, pt_name, pt_number);
//
//                Call<BluetoothCommunication> bluetoothCommunicationCall = getDataService.bluetooth_data_fetch(data);
//                bluetoothCommunicationCall.enqueue(new Callback<BluetoothCommunication>() {
//                    @Override
//                    public void onResponse(Call<BluetoothCommunication> call, Response<BluetoothCommunication> response) {
//                        if (response.code() == 200) {
//                            BluetoothCommunication res = response.body();
//                            pt_email = res.getPhizioemail();
//                            pt_name = res.getName();
//                            pt_number =  res.getPhone();
//                        }
//
//                    }
//
//
//                    @Override
//                    public void onFailure(Call<BluetoothCommunication> call, Throwable t) {
//
//
//                    }
//                });



                progress++;
                mCustomProgressBar.setProgress((int) progress * 100 / (10000 / 1000));
                Drawable draw = ContextCompat.getDrawable(getApplicationContext(), R.drawable.custom_progressbar);
                mCustomProgressBar.setProgressDrawable(draw);
                    webView.getSettings().setJavaScriptEnabled(true);
//                WebSettings webSettings = webView.getSettings();
//                webSettings.setJavaScriptEnabled(true);
                // Load the HTML content
                String htmlContent = "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<head>\n" +
                        "    <meta charset=\"UTF-8\">\n" +
                        "    <style>\n" +
                        "        body {\n" +
                        "            font-family: Arial, sans-serif;\n" +
                        "        }\n" +
                        "\n" +
                        "        .container {\n" +
                        "            width: 360px;\n" +
                        "            margin: 20px auto;\n" +
                        "            /* padding: 20px; */\n" +
                        "            background-color: #f1f1f1;\n" +
                        "            border: 1px solid #ccc;\n" +
                        "        }\n" +
                        "\n" +
                        "       \n" +
                        "    </style>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "    <div class=\"container\">\n" +
                        "        <div class=\"header\" style=\"height: 80px; display:flex; justify-content:space-around; align-items:center;  text-align: center; background: hsl(0, 0%, 0%); color: white;\"> \n" +
                        "            <img src=\"H:\\Pheezee Thermal EMG\\icon_new_thermal.png\" style=\"width:50px; height:50px; border-radius:6px;\">\n" +
                        "            <div style=\"font-size: 28px; color: #FFFFFF; font-weight:800px font-style:bold font-family: 'Roboto';\">\n" +
                        "              <b>PHEEZEE REPORT</b>\n" +
                        "            </div>\n" +
                        "            \n" +
                        "        </div>\n" +
                        "\n" +
                        "        <div style=\"display: inline-block; margin-top: 10px; width: 100%; text-align: center;\">\n" +
                        "          <h1 style = \"position:relative;\" >Patient Name: "+patient_name_et+"</h1>\n" +
                        "          <h4 style = \"position:relative;\">Patient Id:"+patient_id_et+"</h4>\n" +
                        "      </div>\n" +
                        "\n" +
                        "<script src=\"https://cdn.jsdelivr.net/npm/chart.js\"></script>\n" +
                        "\n" +
                        "<canvas id=\"emgChart\" style=\"transform: rotate(90deg);margin-top: 180px; margin-bottom: 180px;\"></canvas>\n" +
                        "\n" +
                        "<script>\n" +
                        "  // Fetch your EMG data (example values)\n" +
                        "  const emgData = "+emg_array_data_et+";\n" +
                        "\n" +
                        "  // Create the chart\n" +
                        "  const ctx = document.getElementById('emgChart').getContext('2d');\n" +
                        "  new Chart(ctx, {\n" +
                        "    type: 'line',\n" +
                        "    data: {\n" +
                        "      labels: emgData.map((_, index) => index + 1), // Use indexes as labels\n" +
                        "      datasets: [{\n" +
                        "        label: 'EMG',\n" +
                        "        data: emgData,\n" +
                        "        borderColor: 'blue',\n" +
                        "        fill: false\n" +
                        "      }]\n" +
                        "    },\n" +
                        "    options: {\n" +
                        "      scales: {\n" +
                        "        y: {\n" +
                        "          beginAtZero: true\n" +
                        "        }\n" +
                        "      }\n" +
                        "    }\n" +
                        "  });\n" +
                        "</script>\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "       \n" +
                        "    </div>\n" +
                        "</body>\n" +
                        "</html>";
                webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);
            }

            @SuppressLint("MissingPermission")
            @Override
            public void onFinish() {
                layout_status.setVisibility(View.VISIBLE);
                dialog.dismiss();
//                printWebViewAsPdf(webView);
//                String file_name = patient_id_et+"pheezee.pdf";
//                pdfView.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), file_name))
//                        .load();

            }


        }.start();






//        }
        pdfView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                print_button.setVisibility(View.VISIBLE);
                print_button_dim.setVisibility(View.GONE);
                if(popup != null) {
                    popup.dismiss();
                }
            }
        });

        print_button.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {
                printWebViewAsPdf(webView);
                print_button.setVisibility(View.GONE);
                print_button_dim.setVisibility(View.VISIBLE);
                if (adapter.isEnabled()) {
                    if (popup != null) {
                        popup.dismiss();
                    }


                    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(BluetoothScreen.this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(BluetoothScreen.this, new String[]{Manifest.permission.BLUETOOTH}, BluetoothScreen.PERMISSION_BLUETOOTH);
                    } else if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(BluetoothScreen.this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(BluetoothScreen.this, new String[]{Manifest.permission.BLUETOOTH_ADMIN}, BluetoothScreen.PERMISSION_BLUETOOTH_ADMIN);
                    } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(BluetoothScreen.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(BluetoothScreen.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, BluetoothScreen.PERMISSION_BLUETOOTH_CONNECT);
                    } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(BluetoothScreen.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(BluetoothScreen.this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, BluetoothScreen.PERMISSION_BLUETOOTH_SCAN);
                    }
                    else {
                        if (bluetooth_status == true){

//                            if (BluetoothPrintersConnections.selectFirstPaired() != null) {
                            popup = new PopupWindow(BluetoothScreen.this);
                            View customView = LayoutInflater.from(BluetoothScreen.this).inflate(R.layout.printerlayour, null);
                            popup.setContentView(customView);
                            int location[] = new int[2];
                            int x = location[0] + customView.getWidth();
                            int y = location[1] + 50; // add 50 pixels from the top
                            popup.showAtLocation(customView, Gravity.TOP | Gravity.RIGHT, x, 250);

                            LinearLayout thermal_printer = customView.findViewById(R.id.thermal_printer);

                            LinearLayout printer = customView.findViewById(R.id.other_printer);
                            printer.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    print_button.setVisibility(View.VISIBLE);
                                    print_button_dim.setVisibility(View.GONE);
                                    new CountDownTimer(2000, 1000) {
                                        @Override
                                        public void onTick(long l) {
//                                            pdfView.fromFile(new File(getApplicationContext().getExternalFilesDir(null) + "/Pheezee", patientid + date_value + ".pdf"))
//                                                    .load();
                                        }

                                        @Override
                                        public void onFinish() {
                                            popup.dismiss();
////                                            File file = new File(getApplicationContext().getExternalFilesDir(null) + "/Pheezee", patientid + date_value + ".pdf");
//                                            Uri fileUri = FileProvider.getUriForFile(BluetoothScreen.this, getApplicationContext().getPackageName() + ".my.package.name.provider", file);
//                                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
//                                            shareIntent.setType("application/pdf");
//                                            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
//                                            startActivity(Intent.createChooser(shareIntent, "Share file"));
                                        }
                                    }.start();

                                }
                            });
                            thermal_printer.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    // Open the PDF file
                                    final Dialog dialog_dp = new Dialog(BluetoothScreen.this);
                                    dialog_dp.setContentView(R.layout.dialog_progress);
                                    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                                    lp.copyFrom(dialog_dp.getWindow().getAttributes());
                                    lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                                    lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                                    dialog_dp.getWindow().setAttributes(lp);
                                    dialog_dp.setCanceledOnTouchOutside(false);
                                    dialog_dp.show();
                                    loopCompleted = false;


                                    new CountDownTimer(2000, 1000) {
                                        @Override
                                        public void onTick(long l) {

                                            String file_name = "download.pdf";
                                            pdfView.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), file_name))
                                                    .load();
                                            print_button.setVisibility(View.GONE);
                                            print_button_dim.setVisibility(View.VISIBLE);

                                        }

                                        @Override
                                        public void onFinish() {

                                            popup.dismiss();
                                            try {
                                                renderer = new PdfRenderer(ParcelFileDescriptor.open(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "download" + ".pdf"), ParcelFileDescriptor.MODE_READ_ONLY));
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
                                                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                                                ColorMatrix matrix = new ColorMatrix();
                                                matrix.setSaturation(0);
                                                ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
                                                Paint paint = new Paint();
                                                paint.setColorFilter(filter);
                                                Canvas canvas = new Canvas(bitmap);
                                                canvas.drawBitmap(bitmap, 0, 0, paint);
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
                                            loopCompleted = true;
                                            // Clean up resources
                                            renderer.close();



                                        }
                                    }.start();

                                    handler = new Handler();

                                    final Runnable r = new Runnable() {
                                        public void run() {
                                            if(loopCompleted){
                                                print_button.setVisibility(View.VISIBLE);
                                                print_button_dim.setVisibility(View.GONE);
                                                dialog_dp.dismiss();
                                                printer_th.disconnectPrinter();
                                            }

                                            handler.postDelayed(this, 1000);
                                        }
                                    };

                                    handler.postDelayed(r, 1000);

                                }
                            });

//                            }
                        }
                        else if (bluetooth_status == false) {
                            popup = new PopupWindow(BluetoothScreen.this);
                            View customView = LayoutInflater.from(BluetoothScreen.this).inflate(R.layout.buyprinter, null);
                            popup.setContentView(customView);
                            int location[] = new int[2];
                            int x = location[0] + customView.getWidth();
                            int y = location[1] + 50; // add 50 pixels from the top
                            popup.showAtLocation(customView, Gravity.TOP | Gravity.RIGHT, x, 250);
                            LinearLayout scan_layout_test = customView.findViewById(R.id.scan_layout_test);
                            ImageView progressBar_testing_1 = customView.findViewById(R.id.progressBar_testing_1);
                            ProgressBar progressBar_testing_2 = customView.findViewById(R.id.progressBar_testing_2);
                            TextView scan_th_device = customView.findViewById(R.id.scan_th_device);
                            TextView buy_now_ptr = customView.findViewById(R.id.buy_now_ptr);
                            LinearLayout other_printer = customView.findViewById(R.id.other_printer);

                            scan_layout_test.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    new_pheezee_printer();
                                    progressBar_testing_1.setVisibility(View.GONE);
                                    progressBar_testing_2.setVisibility(View.VISIBLE);
                                    scan_th_device.setText("Scanning");
                                    new CountDownTimer(10000, 1000) {
                                        @Override
                                        public void onTick(long millisUntilFinished) {

                                        }

                                        @Override
                                        public void onFinish() {
                                            if (bluetooth_status == true){
                                                if (popup != null) {
                                                    popup.dismiss();
                                                }
//                                                if (BluetoothPrintersConnections.selectFirstPaired() != null) {
                                                popup = new PopupWindow(BluetoothScreen.this);
                                                View customView = LayoutInflater.from(BluetoothScreen.this).inflate(R.layout.printerlayour, null);
                                                popup.setContentView(customView);
                                                int location[] = new int[2];
                                                int x = location[0] + customView.getWidth();
                                                int y = location[1] + 50; // add 50 pixels from the top
                                                popup.showAtLocation(customView, Gravity.TOP | Gravity.RIGHT, x, 250);

                                                LinearLayout thermal_printer = customView.findViewById(R.id.thermal_printer);

                                                LinearLayout printer = customView.findViewById(R.id.other_printer);
                                                printer.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        print_button.setVisibility(View.VISIBLE);
                                                        print_button_dim.setVisibility(View.GONE);

                                                        new CountDownTimer(2000, 1000) {
                                                            @Override
                                                            public void onTick(long l) {

                                                            }

                                                            @Override
                                                            public void onFinish() {
                                                                popup.dismiss();

                                                            }
                                                        }.start();

                                                    }
                                                });
                                                thermal_printer.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        // Open the PDF file
                                                        final Dialog dialog_dp = new Dialog(BluetoothScreen.this);
                                                        dialog_dp.setContentView(R.layout.dialog_progress);
                                                        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                                                        lp.copyFrom(dialog_dp.getWindow().getAttributes());
                                                        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                                                        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                                                        dialog_dp.getWindow().setAttributes(lp);
                                                        dialog_dp.setCanceledOnTouchOutside(false);
                                                        dialog_dp.show();
                                                        loopCompleted = false;


                                                        new CountDownTimer(2000, 1000) {
                                                            @Override
                                                            public void onTick(long l) {
                                                                String file_name = "download.pdf";
                                                                pdfView.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), file_name))
                                                                        .load();
//
                                                                print_button.setVisibility(View.GONE);
                                                                print_button_dim.setVisibility(View.VISIBLE);

                                                            }

                                                            @Override
                                                            public void onFinish() {

                                                                popup.dismiss();

                                                                try {
                                                                    renderer = new PdfRenderer(ParcelFileDescriptor.open(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "download" + ".pdf"), ParcelFileDescriptor.MODE_READ_ONLY));
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
                                                                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                                                                    ColorMatrix matrix = new ColorMatrix();
                                                                    matrix.setSaturation(0);
                                                                    ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
                                                                    Paint paint = new Paint();
                                                                    paint.setColorFilter(filter);
                                                                    Canvas canvas = new Canvas(bitmap);
                                                                    canvas.drawBitmap(bitmap, 0, 0, paint);
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
                                                                loopCompleted = true;
                                                                // Clean up resources
                                                                renderer.close();



                                                            }
                                                        }.start();

                                                        handler = new Handler();

                                                        final Runnable r = new Runnable() {
                                                            public void run() {
                                                                if(loopCompleted){
                                                                    print_button.setVisibility(View.VISIBLE);
                                                                    print_button_dim.setVisibility(View.GONE);
                                                                    dialog_dp.dismiss();
                                                                    printer_th.disconnectPrinter();
                                                                }

                                                                handler.postDelayed(this, 1000);
                                                            }
                                                        };

                                                        handler.postDelayed(r, 1000);

                                                    }
                                                });

//                                                }
                                            }else{
                                                progressBar_testing_1.setVisibility(View.VISIBLE);
                                                progressBar_testing_2.setVisibility(View.GONE);
                                                scan_th_device.setText("Scan");
                                            }
                                        }
                                    }.start();

                                }
                            });

                            buy_now_ptr.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    print_button.setVisibility(View.VISIBLE);
                                    print_button_dim.setVisibility(View.GONE);
                                    popup.dismiss();
                                    Intent intent = new Intent(getApplicationContext(), BuyPrinterActivity.class);
                                    intent.putExtra("pt_name", pt_name);
                                    intent.putExtra("pt_number", pt_number);
                                    intent.putExtra("pt_email", pt_email);
                                    startActivity(intent);


                                }
                            });
                            other_printer.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    popup.dismiss();


                                }
                            });
                        }
                    }

                } else {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }


            }

        });

    }

    private void printWebViewAsPdf(WebView webView) {
        // Create a new PDF document
        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(1100, 2090, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        // Render the WebView content onto the PDF page
        webView.draw(page.getCanvas());
        pdfDocument.finishPage(page);

        // Save the PDF document to a file
        String file_name = patient_id_et+"pheezee.pdf";
        File pdfFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), file_name);
        try {
            OutputStream outputStream = new FileOutputStream(pdfFile);
            Log.e("11111111112222222222222333333333", String.valueOf(outputStream));
            pdfDocument.writeTo(outputStream);
            Toast.makeText(this, "PDF file saved to download", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving PDF file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        // Close the PDF document
        pdfDocument.close();
    }

    @SuppressLint("MissingPermission")
    private void new_pheezee_printer(){

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device does not support Bluetooth
        } else {

        }
        BluetoothTask bluetoothTask = new BluetoothTask();
        bluetoothTask.execute();

    }







    @SuppressLint("MissingPermission")
    private void pheezee_printer() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Bluetooth is not supported on this device
            // Handle the error or notify the user accordingly
        } else {
            // Bluetooth is supported
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        BluetoothDevice targetPrinter = null;
        for (BluetoothDevice device : pairedDevices) {
            if (device.getName().equals("RSTRADING") || device.getName().equals("MPT-III") || device.getName().equals("Pheezee Printer")) {
                targetPrinter = device;
                break;
            }
        }




        if (targetPrinter != null) {

            String deviceAddress = targetPrinter.getAddress();
            UUID uuid = targetPrinter.getUuids()[0].getUuid();
            connectToBluetoothPrinter(deviceAddress,uuid);
            // Bluetooth printer is available
            // Proceed with the connection using connections.selectFirstPaired() method
        } else {

            // Bluetooth printer is not available
            // Handle the error or notify the user accordingly
        }


    }







    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void connectToBluetoothPrinter(String deviceAddressinfo,UUID uuiddata) {
        String deviceAddress = deviceAddressinfo; // Replace with the actual device address
        UUID uuid = uuiddata; // Replace with the actual UUID

        OpenBluetoothSocketTask task = new OpenBluetoothSocketTask(deviceAddress, uuid, this);
        task.execute();
    }


    @Override
    public void onSocketConnectionResult(boolean isConnected, BluetoothSocket socket) {
        if (isConnected) {
            bluetooth_status = true;
            connection_status = true;

        } else {
            bluetooth_status = false;
        }
    }


    private class BluetoothTask extends AsyncTask<Void, Void, BluetoothConnection> {

        @Override
        protected BluetoothConnection doInBackground(Void... params) {
            BluetoothPrintersConnections printers = new BluetoothPrintersConnections();
            BluetoothConnection[] bluetoothPrinters = printers.getList();

            if (bluetoothPrinters != null && bluetoothPrinters.length > 0) {
                for (BluetoothConnection printer : bluetoothPrinters) {
                    try {
                        return printer.connect();
                    } catch (EscPosConnectionException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }





//            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//            if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
//                @SuppressLint("MissingPermission")
//                Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
//                if (pairedDevices.size() > 0) {
//                    try {
//                        BluetoothConnection printer = BluetoothPrintersConnections.selectFirstPaired();
//                        // Proceed with printing or other operations
//                        if(printer.equals("Unable to connect to bluetooth device.")){
//                            Toast.makeText(BluetoothScreen.this, "222222222222222222222", Toast.LENGTH_LONG).show();
//                        }
//                        Toast.makeText(BluetoothScreen.this, "111111111111111111111111111", Toast.LENGTH_LONG).show();
////                        return 0;
//                    } catch (Exception e) {
//                        // Handle the exception (e.g., display an error message)
//                        return null;
//                    }
//
//                }
//            }
            return null;
        }

        @Override
        protected void onPostExecute(BluetoothConnection device) {
            if (device != null) {
                // Do something with the Bluetooth device
                bluetooth_status = true;

            } else {
                bluetooth_status = false;
                // Handle case when no paired devices are available
            }
        }
    }

}