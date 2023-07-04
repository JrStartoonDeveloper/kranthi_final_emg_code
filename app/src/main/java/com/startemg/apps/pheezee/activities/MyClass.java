package com.startemg.apps.pheezee.activities;

import android.content.Context;

import com.startemg.apps.pheezee.popup.ViewPopUpWindow;


public class MyClass {
    public void ViewPopUpWindows(Context context, String patient, String email) {
        ViewPopUpWindow popUpWindow = new ViewPopUpWindow(context, patient, email);
        popUpWindow.openViewPopUpWindow();
    }
}
