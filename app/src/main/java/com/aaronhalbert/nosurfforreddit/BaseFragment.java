/*
 * Copyright (c) 2018-present, Aaron J. Halbert.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package com.aaronhalbert.nosurfforreddit;

import com.aaronhalbert.nosurfforreddit.di.application.ApplicationComponent;
import com.aaronhalbert.nosurfforreddit.di.presentation.PresentationComponent;
import com.aaronhalbert.nosurfforreddit.di.presentation.PresentationModule;
import com.aaronhalbert.nosurfforreddit.di.presentation.ViewModelModule;

import androidx.annotation.UiThread;
import androidx.fragment.app.Fragment;

abstract public class BaseFragment extends Fragment {
    private static final String INJECTION_ALREADY_PERFORMED = "Injection already performed on this activity";

    private boolean isInjectorUsed;

    @UiThread
    protected PresentationComponent getPresentationComponent() {
        if (isInjectorUsed) {
            throw new RuntimeException(INJECTION_ALREADY_PERFORMED);
        }

        isInjectorUsed = true;

        return getApplicationComponent()
                .newPresentationComponent(new PresentationModule(getActivity()), new ViewModelModule());
    }

    private ApplicationComponent getApplicationComponent() {
        return ((NoSurfApplication) getActivity().getApplication()).getApplicationComponent();
    }
}
