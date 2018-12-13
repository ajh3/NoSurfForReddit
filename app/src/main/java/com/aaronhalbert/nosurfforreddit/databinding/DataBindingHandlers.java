package com.aaronhalbert.nosurfforreddit.databinding;

import android.content.Context;
import android.graphics.Paint;
import android.widget.ImageView;
import android.widget.TextView;

import com.aaronhalbert.nosurfforreddit.R;
import com.aaronhalbert.nosurfforreddit.glide.GlideApp;

import androidx.annotation.DrawableRes;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.databinding.BindingAdapter;

import static com.aaronhalbert.nosurfforreddit.repository.redditschema.Listing.DEFAULT;
import static com.aaronhalbert.nosurfforreddit.repository.redditschema.Listing.IMAGE;
import static com.aaronhalbert.nosurfforreddit.repository.redditschema.Listing.NSFW;
import static com.aaronhalbert.nosurfforreddit.repository.redditschema.Listing.SELF;
import static com.aaronhalbert.nosurfforreddit.repository.redditschema.Listing.SPOILER;

@SuppressWarnings("WeakerAccess")
public class DataBindingHandlers {

    /* these methods are called by the binding framework any time it finds a view with one
     * of the synthetic properties specified in the annotation arguments */

    @BindingAdapter("imageUrl")
    public static void bindImage(ImageView iv, String url) {
        Context context = iv.getContext();

        /* Glide seems to have a problem with switching this url. If/else works
         *
         * Not sure where the bug is coming from */
        if (DEFAULT.equals(url)) {
            loadImage(context, iv, R.drawable.link_post_thumbnail);
        } else if (SELF.equals(url)) {
            loadImage(context, iv, R.drawable.self_post_thumbnail);
        } else if (NSFW.equals(url)) {
            loadImage(context, iv, R.drawable.nsfw_thumbnail);
        } else if (IMAGE.equals(url)) {
            loadImage(context, iv, R.drawable.link_post_thumbnail);
        } else if (SPOILER.equals(url)) {
            loadImage(context, iv, R.drawable.spoiler_thumbnail);
        } else {
            GlideApp.with(context)
                    .asBitmap() // prevents gifs from playing in-app
                    .load(url)
                    .centerCrop()
                    .into(iv);
        }
    }

    private static void loadImage(Context context, ImageView iv, @DrawableRes int resId) {
        GlideApp.with(context)
                .load(AppCompatResources.getDrawable(
                        context,
                        resId))
                .centerCrop()
                .into(iv);
    }

    @BindingAdapter("strikethrough")
    public static void strikethrough(TextView tv, boolean hasBeenClicked) {
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
