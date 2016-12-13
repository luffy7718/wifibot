

package com.example.wifibot;

import android.os.Bundle;
import android.preference.PreferenceActivity;


public class option extends PreferenceActivity {

 
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.option);//option de l'application
    }
}
