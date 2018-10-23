package com.aaronhalbert.nosurfforreddit.dependencyinjection.application;

import android.app.Application;

import dagger.Module;

@Module
public class ApplicationModule {
     private final Application application;

     public ApplicationModule(Application application) {
         this.application = application;
     }

    // @Provides methods go here

}
