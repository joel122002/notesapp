package com.notesapp.notes;

import com.parse.Parse;
import android.app.Application;

public class App extends Application {
    //This class is called at the begining of the application so as to connect to the server
    private static final String APPLICATION_ID = BuildConfig.APPLICATIONID;
    private static final String CLIENT_KEY = BuildConfig.CLIENT_KEY;
    private static final String SERVER = BuildConfig.SERVER;
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(APPLICATION_ID)
                // if defined
                .clientKey(CLIENT_KEY)
                .server(SERVER)
                .build()
        );
    }
}