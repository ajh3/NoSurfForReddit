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

package com.aaronhalbert.nosurfforreddit.di.presentation;

import com.aaronhalbert.nosurfforreddit.ui.detail.NoSurfWebViewFragment;
import com.aaronhalbert.nosurfforreddit.ui.detail.PostFragment;
import com.aaronhalbert.nosurfforreddit.ui.login.LoginFragment;
import com.aaronhalbert.nosurfforreddit.ui.main.MainActivity;
import com.aaronhalbert.nosurfforreddit.ui.main.ViewPagerFragment;
import com.aaronhalbert.nosurfforreddit.ui.master.ContainerFragment;
import com.aaronhalbert.nosurfforreddit.ui.master.PostsFragment;

import dagger.Subcomponent;

@Subcomponent(modules = {PresentationModule.class, ViewModelModule.class})
public interface PresentationComponent {
    void inject(MainActivity mainActivity);

    void inject(NoSurfWebViewFragment noSurfWebViewFragment);

    void inject(ViewPagerFragment viewPagerFragment);

    void inject(PostFragment postFragment);

    void inject(PostsFragment postsFragment);

    void inject(LoginFragment loginFragment);

    void inject(ContainerFragment containerFragment);
}
