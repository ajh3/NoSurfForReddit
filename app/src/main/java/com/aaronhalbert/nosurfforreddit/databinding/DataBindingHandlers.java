package com.aaronhalbert.nosurfforreddit.databinding;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.databinding.BindingAdapter;

import android.content.Context;
import android.graphics.Paint;
import android.widget.ImageView;
import android.widget.TextView;

import com.aaronhalbert.nosurfforreddit.R;
import com.aaronhalbert.nosurfforreddit.glide.GlideApp;

import static com.aaronhalbert.nosurfforreddit.repository.redditschema.Listing.DEFAULT;
import static com.aaronhalbert.nosurfforreddit.repository.redditschema.Listing.IMAGE;
import static com.aaronhalbert.nosurfforreddit.repository.redditschema.Listing.NSFW;
import static com.aaronhalbert.nosurfforreddit.repository.redditschema.Listing.SELF;
import static com.aaronhalbert.nosurfforreddit.repository.redditschema.Listing.SPOILER;

@SuppressWarnings("WeakerAccess")
public class DataBindingHandlers {

    /* these methods are called by the binding framework any time it finds a view with one
     * of the synthetic properties specified in the annotation arguments */

    //binding framework calls this any time it finds an ImageView with an imageUrl synthetic property

    @BindingAdapter("imageUrl")
    public static void bindImage(ImageView iv, String url) {
        Context context = iv.getContext();

        // Glide seems to have a problem with switching this url. If/else works
        // Not sure whether it's a bug in Glide or in my code
        if (DEFAULT.equals(url)) {
            GlideApp.with(context)
                    .load(AppCompatResources.getDrawable(
                            context,
                            R.drawable.link_post_thumbnail))
                    .centerCrop()
                    .into(iv);
        } else if (SELF.equals(url)) {
            GlideApp.with(context)
                    .load(AppCompatResources.getDrawable(
                            context,
                            R.drawable.self_post_thumbnail))
                    .centerCrop()
                    .into(iv);
        } else if (NSFW.equals(url)) {
            GlideApp.with(context)
                    .load(AppCompatResources.getDrawable(
                            context,
                            R.drawable.nsfw_thumbnail))
                    .centerCrop()
                    .into(iv);
        } else if (IMAGE.equals(url)) {
            GlideApp.with(context)
                    .load(AppCompatResources.getDrawable(
                            context,
                            R.drawable.link_post_thumbnail))
                    .centerCrop()
                    .into(iv);
        } else if (SPOILER.equals(url)) {
            GlideApp.with(context)
                    .load(AppCompatResources.getDrawable(
                            context,
                            R.drawable.spoiler_thumbnail))
                    .centerCrop()
                    .into(iv);
        } else {
            GlideApp.with(context)
                    .load(url)
                    .centerCrop()
                    .into(iv);
        }
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
