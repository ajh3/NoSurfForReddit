package com.aaronhalbert.nosurfforreddit.databinding;

import androidx.databinding.BindingAdapter;
import android.widget.ImageView;

import com.aaronhalbert.nosurfforreddit.glide.GlideApp;

public class DataBindingHandlers {
    //binding framework calls this any time it finds an ImageView with an imageUrl synthetic property
    @BindingAdapter("imageUrl")
    public static void bindImage(ImageView iv, String url) {

        GlideApp.with(iv.getContext())
                .load(url)
                .centerCrop()
                .into(iv);
    }
}
