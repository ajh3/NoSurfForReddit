package com.aaronhalbert.nosurfforreddit.databinding;

import androidx.databinding.BindingAdapter;

import android.graphics.Paint;
import android.widget.ImageView;
import android.widget.TextView;

import com.aaronhalbert.nosurfforreddit.glide.GlideApp;

@SuppressWarnings("WeakerAccess")
public class DataBindingHandlers {
    //binding framework calls this any time it finds an ImageView with an imageUrl synthetic property
    @BindingAdapter("imageUrl")
    public static void bindImage(ImageView iv, String url) {

        GlideApp.with(iv.getContext())
                .load(url)
                .centerCrop()
                .into(iv);
    }

    @BindingAdapter("strikethroughClickedPostTitle")
    public static void strikethru(TextView tv, boolean hasBeenClicked) {
        if (hasBeenClicked) {
            tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            tv.setPaintFlags(tv.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }
}
