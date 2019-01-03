package com.aaronhalbert.nosurfforreddit.utils;

// event wrapper translated from Kotlin, as described here: https://medium.com/androiddevelopers/livedata-with-snackbar-navigation-and-other-events-the-singleliveevent-case-ac2622673150

public class Event<T> {
    private boolean hasBeenHandled;
    private final T content;

    public Event(T content) {
        this.content = content;
    }

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
