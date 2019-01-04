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

package com.aaronhalbert.nosurfforreddit.utils;

// event wrapper translated from Kotlin, as described here: https://medium.com/androiddevelopers/livedata-with-snackbar-navigation-and-other-events-the-singleliveevent-case-ac2622673150

public class Event<T> {
    private boolean hasBeenHandled;
    private final T content;

    public Event(T content) {
        this.content = content;
    }

    @SuppressWarnings("UnusedReturnValue")
    public T getContentIfNotHandled() {
        if (hasBeenHandled) {
            return null;
        } else {
            hasBeenHandled = true;
            return content;
        }
    }

    @SuppressWarnings("unused")
    public T peekContent() {
        return content;
    }

    @SuppressWarnings("unused")
    public boolean getHasBeenHandled() {
        return hasBeenHandled;
    }
}
