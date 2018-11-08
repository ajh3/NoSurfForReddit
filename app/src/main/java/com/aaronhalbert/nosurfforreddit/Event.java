package com.aaronhalbert.nosurfforreddit;

public class Event<T> {
    private boolean hasBeenHandled;
    private T content;

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

    public T peekContent() {
        return content;
    }

    public boolean getHasBeenHandled() {
        return hasBeenHandled;
    }
}
