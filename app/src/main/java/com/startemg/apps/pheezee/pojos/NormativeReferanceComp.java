package com.startemg.apps.pheezee.pojos;

public class NormativeReferanceComp {
    String phizioemail;
    String patientid;
    String patient_injured;
    String str_body_part;
    String str_exercise_name;
    String str_muscle_name;
    String orientation;
    String injuredside_ref;
//    String dateString;

    public NormativeReferanceComp(String phizioemail,String patientid, String patient_injured,String injuredside_ref,String str_body_part,  String str_exercise_name, String str_muscle_name, String orientation){
        this.phizioemail = phizioemail;
        this.patientid = patientid;
        this.patient_injured = patient_injured;
        this.str_body_part = str_body_part;
        this.str_exercise_name = str_exercise_name;
        this.str_muscle_name = str_muscle_name;
        this.orientation = orientation;
        this.injuredside_ref = injuredside_ref;
//        this.dateString = dateString;


    }

    public String getPhizioemail() {
        return phizioemail;
    }
    public void setPhizioemail(String phizioemail) {
        this.phizioemail = phizioemail;
    }
    public String getPatientid() {
        return patientid;
    }
    public void setPatientid(String patientid) {
        this.patientid = patientid;
    }

    public String getBodypart() {
        return patient_injured;
    }
    public void setBodypart(String patient_injured) {
        this.patient_injured = patient_injured;
    }
    public String getExercisename() {
        return str_body_part;
    }
    public void setExercisename(String str_body_part) {
        this.str_body_part = str_body_part;
    }

    public String getOrientation() {
        return str_exercise_name;
    }
    public void setOrientation(String str_exercise_name) {
        this.str_exercise_name = str_exercise_name;
    }

    public String getInjuredside_ref() {
        return injuredside_ref;
    }
    public void setInjuredside_ref(String injuredside_ref) {
        this.injuredside_ref = injuredside_ref;
    }
//
//    public String getDateString() {
//        return dateString;
//    }
//    public void setDateString(String dateString) {
//        this.dateString = dateString;
//    }

}
