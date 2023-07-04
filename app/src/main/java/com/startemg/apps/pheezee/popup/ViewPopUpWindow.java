package com.startemg.apps.pheezee.popup;

import static com.startemg.apps.pheezee.activities.PatientsView.REQ_CAMERA;
import static com.startemg.apps.pheezee.activities.PatientsView.REQ_GALLERY;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.startemg.apps.pheezee.activities.PatientsView;
import com.startemg.apps.pheezee.pojos.PatientDetailsData;
import com.startemg.apps.pheezee.room.Entity.PhizioPatients;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import me.bastanfar.semicirclearcprogressbar.SemiCircleArcProgressBar;
import startemg.apps.pheezee.R;

public class ViewPopUpWindow{
    Context context;
    static PhizioPatients patient;
    EditPopUpWindow.onClickListner listner;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    Bitmap profile;
    String json_phizioemail, view_data,view_data_last,view_data_report, view_data_goal , gender, age;

    AlertDialog.Builder builder = null;
    boolean use_new_photo=false;

    PopupWindow popup;

    boolean gallery_selected=false;
    boolean camera_selected=false;

    String pt_age ;

    final CharSequence[] items = { "Take Photo", "Choose from Library",
            "Cancel" };

    public ViewPopUpWindow(final Activity context, PhizioPatients patient, String json_phizioemail){
        Log.e("patient", String.valueOf(patient));
        this.context = context;
        this.patient = patient;
        this.json_phizioemail = json_phizioemail;
    }



    public ViewPopUpWindow(Context context,String json_phizioemail, Bitmap photo){
        this.context = context;
        this.json_phizioemail = json_phizioemail;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        editor = sharedPref.edit();
        this.profile = photo;
        use_new_photo = true;
    }

    public ViewPopUpWindow(Context applicationContext, String patients, String json_phizioemails) {
        Log.e("patient", String.valueOf(patients));
        this.context = applicationContext;
        this.patient = patient;
        this.json_phizioemail = json_phizioemails;
    }



    public void openViewPopUpWindow(){
        PopupWindow pw;
        final String[] case_description = {""};
        final String[] case_00 ={""};
        Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
        Point size = new Point();display.getSize(size);int width = size.x;int height = size.y;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        @SuppressLint("InflateParams") final View layout = inflater.inflate(R.layout.view_pop, null);

        pw = new PopupWindow(layout, ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT,true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            pw.setElevation(10);
        }
        pw.setTouchable(true);
        pw.setOutsideTouchable(true);
        pw.setContentView(layout);
        pw.setFocusable(true);
        pw.setAnimationStyle(R.style.Animation);
        pw.showAtLocation(layout, Gravity.CENTER, 0, 0);

        final TextView tv_patientId = layout.findViewById(R.id.tv_patient_id);
        final TextView tv_create_account = layout.findViewById(R.id.tv_create_account);
        final TextView patientName = layout.findViewById(R.id.patientName);
        final TextView tv_Medicalhistory_det = layout.findViewById(R.id.tv_Medicalhistory_det);
        final TextView tv_affected_side = layout.findViewById(R.id.tv_affected_side);
        final TextView tv_condition_det = layout.findViewById(R.id.tv_condition_det);
        final TextView tv_Speciality_det = layout.findViewById(R.id.tv_Speciality_det);
        final TextView patientPhone = layout.findViewById(R.id.patientphone);
        ImageView patient_profilepic = layout.findViewById(R.id.imageView4);
        ImageView patient_profilepic_image = layout.findViewById(R.id.profile_picture);
        final TextView patientEmail = layout.findViewById(R.id.patientemail);
        final ImageView backbutton = layout.findViewById(R.id.iv_back_app_info);
        final TextView text_data = layout.findViewById(R.id.text_data);
        final TextView text_data_01 = layout.findViewById(R.id.text_data_01);
        final TextView text_data_02 = layout.findViewById(R.id.text_data_02);
        final TextView text_data_03 = layout.findViewById(R.id.text_data_03);
        final LinearLayout edit_button_action = layout.findViewById(R.id.edit_button);
        final  TextView join_date = layout.findViewById(R.id.join);
        final  TextView session_number = layout.findViewById(R.id.session_count_number);
        final  TextView last_session_number = layout.findViewById(R.id.last_session);
        final  TextView report_number = layout.findViewById(R.id.report_count);
        final  Button deleteBtn = layout.findViewById(R.id.delete_botton);
        final ImageView injured_side = layout.findViewById(R.id.effected_side);
        final SemiCircleArcProgressBar semi_prog_bar = layout.findViewById(R.id.semi_prog);
        final TextView tv_goal_num_value = layout.findViewById(R.id.tv_goal_num);
        final Button start_session_btr = layout.findViewById(R.id.start_session);
        final Button go_to_home_btr = layout.findViewById(R.id.go_to_home);
        final RelativeLayout testing_lt_btn = layout.findViewById(R.id.testing_lt);


        testing_lt_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(popup != null) {
                    popup.dismiss();
                }
            }
        });

        go_to_home_btr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(popup != null) {
                    popup.dismiss();
                }
                pw.dismiss();
            }
        });




        new CountDownTimer(2000, 1000) {

            public void onTick(long millisUntilFinished) {
                SharedPreferences view_data_s = PreferenceManager.getDefaultSharedPreferences(context);
                view_data = view_data_s.getString("view_data", "");
                SharedPreferences view_data_l = PreferenceManager.getDefaultSharedPreferences(context);
                view_data_last = view_data_l.getString("view_data_last", "");
                SharedPreferences view_data_r = PreferenceManager.getDefaultSharedPreferences(context);
                view_data_report = view_data_r.getString("view_data_report", "");
                SharedPreferences view_data_g = PreferenceManager.getDefaultSharedPreferences(context);
                view_data_goal = view_data_g.getString("view_data_goal", "");
                Log.e("view_data_goal",view_data_goal);

            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            public void onFinish() {
                try{
                    if(patient.getPatientinjured()!= null){
                        if(patient.getPatientinjured().equalsIgnoreCase("Left")){
                            injured_side.setImageResource(R.drawable.left_side_injured);
                        }else if (patient.getPatientinjured().equalsIgnoreCase("Right")){
                            injured_side.setImageResource(R.drawable.right_side_injured);
                        }else if(patient.getPatientinjured().equalsIgnoreCase("Bi-Lateral")){
                            injured_side.setImageResource(R.drawable.billateral_side_injured);
                        }

                    }
                }catch (NumberFormatException e){

                }
                String start_date = patient.getDateofjoin();
                start_date = start_date.replace("/","-");
                String[] date_split = start_date.split("-");
                start_date = date_split[0]+"-"+date_split[1]+"-"+ date_split[2];

                String last_date = view_data_last;

                if(last_date != "-" ) {
                    last_date = last_date.replace("-", "-");
                    String[] date_split2 = last_date.split("-");
                    if (date_split2.length != 0) {
                        last_date = date_split2[2] + "-" + date_split2[1] + "-" + date_split2[0];
                    } else {
                        last_date = "-";
                    }
                }else{
                    last_date = "-";
                }








                join_date.setText(start_date);
                session_number.setText(view_data);
                last_session_number.setText(last_date);
                report_number.setText(view_data);

                gender = patient.getPatientgender();

                if(gender.equalsIgnoreCase("Male")){
                    gender = "M";
                }else if(gender.equalsIgnoreCase("Female")){
                    gender = "F";
                }

                age = patient.getPatientage();


                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                Date date = null;
                try {
                    if(age.length() >= 3) {
                        date = formatter.parse(age);
                        Instant instant = date.toInstant();
                        ZonedDateTime zone = instant.atZone(ZoneId.systemDefault());
                        LocalDate givenDate = zone.toLocalDate();
                        //Calculating the difference between given date to current date.
                        Period period = Period.between(givenDate, LocalDate.now());

                        pt_age = String.valueOf(period.getYears());
                    }else {
                        pt_age = age;
                    }
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                //Converting obtained Date object to LocalDate object



                patientName.setText(patient.getPatientname()+","+" "+gender+"/"+pt_age);
                tv_patientId.setText("Patient ID: "+patient.getPatientid());
                patientPhone.setText(patient.getPatientphone());
                patientEmail.setText(patient.getPatientemail());
                tv_affected_side.setText(patient.getPatientinjured());
                tv_condition_det.setText(patient.getPatientcasedes());
                tv_Speciality_det.setText(patient.getPatientcondition());
                tv_Medicalhistory_det.setText(patient.getPatienthistory());
                if(view_data_goal.equalsIgnoreCase("-")){
                    semi_prog_bar.setPercent(0);
                }else {
                    semi_prog_bar.setPercent(Integer.parseInt(view_data_goal));
                }

//                tv_goal_num_value.setText(view_data_goal.concat("%"));
//                tv_goal_num_value.setText("0".concat("%"));
            }

        }.start();





        edit_button_action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup = new PopupWindow(context);
                View customView = LayoutInflater.from(context).inflate(R.layout.view_patient_three_dot, null);
                popup.setContentView(customView);
                popup.setWidth(380);
                int location[] = new int[2];
                int x = location[0] + customView.getWidth();
                int y = location[1] + 50; // add 50 pixels from the top
                popup.showAtLocation(customView, Gravity.TOP | Gravity.RIGHT, x, 250);
                LinearLayout delete_patient_btn = customView.findViewById(R.id.delete_patient);
                LinearLayout edit_patient_btn = customView.findViewById(R.id.edit_patient);
                popup.setOutsideTouchable(true);
                popup.setFocusable(true);


                delete_patient_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((PatientsView)context).deletePatient(patient);
                        popup.dismiss();
                    }
                });

                edit_patient_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((PatientsView)context).editThePatientDetails(patient);
                        popup.dismiss();
                    }
                });



//                ((PatientsView)context).editThePatientDetails(patient);
            }
        });

        start_session_btr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((PatientsView)context).startSession(patient.getPatientid(), patient.getPatientname(), patient.getDateofjoin(), patient.getPatientinjured());
            }
        });



        ArrayList<Integer> arrayList_history = new ArrayList<>();
        String[] array_history  = {"DM","Hypertension", "Hypothyroidism", "Hyperthyroidism", "Presence of implant", "others"};

        // check Box


        //  back botton
        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(popup != null) {
                    popup.dismiss();
                }
                pw.dismiss();

            }
        });





        Calendar c=Calendar.getInstance();
        Integer month=c.get(Calendar.MONTH);
        Integer day=c.get(Calendar.DAY_OF_MONTH);
        Integer year=c.get(Calendar.YEAR);



        if(use_new_photo==false)
        {
            Glide.with(context)
                    .load("https://s3.ap-south-1.amazonaws.com/pheezee/physiotherapist/" + json_phizioemail.replaceFirst("@", "%40") + "/patients/" + patient.getPatientid() + "/images/profilepic.png")
                    .apply(new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .error(R.drawable.test_patient_add_1))
                    .listener(new RequestListener<Drawable>() {
                                  @Override
                                  public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                      return false;
                                  }

                                  @Override
                                  public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                      patient_profilepic_image.setVisibility(View.VISIBLE);
                                      patient_profilepic.setVisibility(View.GONE);
                                      return false;
                                  }
                              }
                    )
                    .into(patient_profilepic_image);

        }else {
            if(this.profile!=null){
                patient_profilepic_image.setImageBitmap(this.profile);
                patient_profilepic_image.setVisibility(View.VISIBLE);
                patient_profilepic.setVisibility(View.GONE);
            }

        }


        tv_patientId.setVisibility(View.VISIBLE);
        tv_create_account.setText("Patient Profile");


        //Adapter for spinner
        ArrayAdapter<String> array_exercise_names = new ArrayAdapter<String>(context, R.layout.support_simple_spinner_dropdown_item, context.getResources().getStringArray(R.array.case_description));
        array_exercise_names.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        String[] cases_list = context.getResources().getStringArray(R.array.case_description);
        ArrayList<String> arrayList = new ArrayList<>();
        Collections.addAll(arrayList,cases_list);
        if(arrayList.contains(patient.getPatientcasedes())) {
        }else{

        }

        try{
            if(!patient.getPatientcondition().equals("")){
            }
        } catch (Exception e) {
            Log.e("your app", e.toString());
        }



















        Button addBtn = layout.findViewById(R.id.addBtn);

        addBtn.setText("Update");
        final Button cancelBtn = layout.findViewById(R.id.cancelBtn);









        if(patient.getPatientgender().equalsIgnoreCase("Male")) {
        }
        else{
        }






        case_description[0] = patient.getPatientcasedes();
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(case_description[0].equalsIgnoreCase("Other")){

                }else {
                    try {

                    } catch (Exception e) {
                        Log.e("your app", e.toString());
                    }
                }



                String patientname = patientName.getText().toString();

                String patientphone = patientPhone.getText().toString();
                String patientemail = patientEmail.getText().toString();
                String patientcondition = text_data_02.getText().toString();







                if(case_description[0].equals("Specialities")){
                    text_data.setText("False");
                }else{
                    text_data.setText("True");
                }
                Log.e("Kranthi_text_data", text_data.getText().toString());
                if(patientcondition.equals(null)){
                    text_data_01.setText("False");
                }else{
                    text_data_01.setText("True");
                }

            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((PatientsView)context).openReportActivity(patient.getPatientid(),patient.getPatientname(), patient.getDateofjoin());
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((PatientsView)context).deletePatient(patient);
            }
        });


    }





    private void cameraIntent() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            ((Activity) context).startActivityForResult(takePicture, 41);
        }else {
            ActivityCompat.requestPermissions(((Activity) context), new String[] {Manifest.permission.CAMERA}, REQ_CAMERA);
        }
    }
    private void galleryIntent() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        pickPhoto.putExtra("patientid",1);
            ((Activity) context).startActivityForResult(pickPhoto, 42);
        }else {
            ActivityCompat.requestPermissions(((Activity) context), new String[] {Manifest.permission.CAMERA}, REQ_GALLERY);
        }
    }


    public interface onClickListner{
        void onAddClickListner(PhizioPatients patients, PatientDetailsData data, boolean isvalid,Bitmap photo);
    }

    public void setOnClickListner(EditPopUpWindow.onClickListner listner){
        this.listner = listner;
    }


}