package com.aaronhalbert.nosurfforreddit.databinding;

import androidx.core.content.ContextCompat;
import androidx.databinding.BindingAdapter;

import android.graphics.Paint;
import android.widget.ImageView;
import android.widget.TextView;

import com.aaronhalbert.nosurfforreddit.R;
import com.aaronhalbert.nosurfforreddit.glide.GlideApp;

@SuppressWarnings("WeakerAccess")
public class DataBindingHandlers {

    /* these methods are called by the binding framework any time it finds a view with one
     * of the synthetic properties specified in the annotation arguments */

    //binding framework calls this any time it finds an ImageView with an imageUrl synthetic property

    @BindingAdapter("imageUrl")
    public static void bindImage(ImageView iv, String url) {
        GlideApp.with(iv.getContext())
                .load(url)
                .centerCrop()
                .into(iv);
    }

    @BindingAdapter("strikethrough")
    public static void strikethru(TextView tv, boolean hasBeenClicked) {
        if (hasBeenClicked) {
            tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            tv.setTextColor(ContextCompat
                    .getColor(tv.getContext(), R.color.colorRecyclerViewTextClicked));
        } else {
            tv.setPaintFlags(tv.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
            tv.setTextColor(ContextCompat
                    .getColor(tv.getContext(), R.color.colorRecyclerViewText));
        }
    }

    @BindingAdapter("transparency")
    public static void transparency(ImageView iv, boolean hasBeenClicked) {
        if (hasBeenClicked) {
            iv.setAlpha(0.2f);
        } else {
            iv.setAlpha(1f);
        }
    }
}
