package com.aaronhalbert.nosurfforreddit.databinding;

import androidx.databinding.BindingAdapter;

import android.graphics.Paint;
import android.widget.ImageView;
import android.widget.TextView;

import com.aaronhalbert.nosurfforreddit.R;
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

    @BindingAdapter("strikethrough")
    public static void strikethru(TextView tv, boolean hasBeenClicked) {
        if (hasBeenClicked) {
            tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            tv.setTextColor(tv.getResources().getColor(R.color.colorRecyclerViewTextClicked));
        } else {
            tv.setPaintFlags(tv.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
            tv.setTextColor(tv.getResources().getColor(R.color.colorRecyclerViewText));
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
