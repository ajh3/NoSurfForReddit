package com.aaronhalbert.nosurfforreddit;

import android.databinding.BindingAdapter;
import android.util.Log;
import android.widget.ImageView;

public class DataBindingHandlers {
    //binding framework calls this any time it finds an ImageView with an imageUrl synthetic property
    @BindingAdapter("app:imageUrl")
    public static void bindImage(ImageView iv, String url) {

        GlideApp.with(iv.getContext())
                .load(url)
                .centerCrop()
                .into(iv);
    }

    @BindingAdapter("app:thumbnailUrl")
    public static void bindThumbnail(ImageView iv, String url) {

        GlideApp.with(iv.getContext())
                .load(url)
                .centerCrop()
                .into(iv);
    }
}
