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

package com.aaronhalbert.nosurfforreddit.di.application;

import com.aaronhalbert.nosurfforreddit.di.presentation.PresentationComponent;
import com.aaronhalbert.nosurfforreddit.di.presentation.PresentationModule;
import com.aaronhalbert.nosurfforreddit.di.presentation.ViewModelModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {ApplicationModule.class, NetworkingModule.class})
public interface ApplicationComponent {
    /* factory method returning subcomponent, to establish parent/child relationship.
     *
     * Since we have a subcomponent, we don't need to expose any dependencies publicly
     * from this component; sub-components automatically have access to the entire object graph. */
    PresentationComponent newPresentationComponent(PresentationModule presentationModule,
                                                   ViewModelModule viewModelModule);
}
