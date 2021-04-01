/*
 * Copyright 2017 Rozdoum
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.rozdoum.socialcomponents.utils;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import androidx.annotation.Nullable;
import android.widget.ImageView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.firebase.storage.StorageReference;
import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.enums.UploadImagePrefix;

import java.util.Date;


public class ImageUtil {

    public static final String TAG = ImageUtil.class.getSimpleName();

    public static String generateImageTitle(UploadImagePrefix prefix, String parentId) {
        if (parentId != null) {
            return prefix.toString() + parentId;
        }

        return prefix.toString() + new Date().getTime();
    }

    public static String generatePostImageTitle(String parentId) {
        return generateImageTitle(UploadImagePrefix.POST, parentId) + "_" + new Date().getTime();
    }

    public static void loadImage( RequestManager glideRequests, String url, ImageView imageView) {
        loadImage(glideRequests, url, imageView, DiskCacheStrategy.ALL);
    }

    public static void loadImage(RequestManager glideRequests, String url, ImageView imageView, DiskCacheStrategy diskCacheStrategy) {
        glideRequests.load(url)
                .apply(new RequestOptions().diskCacheStrategy(diskCacheStrategy)
                .error(R.drawable.ic_stub))
                .into(imageView);
    }

    public static void loadImage(RequestManager glideRequests, String url, ImageView imageView,
                                 RequestListener<Drawable> listener) {
        RequestOptions options = new RequestOptions()
                .error(R.drawable.ic_stub)
                .diskCacheStrategy(DiskCacheStrategy.DATA);

        glideRequests.load(url)
                .apply(options)
                .listener(listener)
                .into(imageView);
    }

    public static void loadImageCenterCrop(RequestManager glideRequests, String url, ImageView imageView,
                                           int width, int height) {
        glideRequests.load(url)
                .apply(new RequestOptions().centerCrop().override(width,height).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.ic_stub))
                .into(imageView);
    }

    public static void loadImageCenterCrop(RequestManager glideRequests, StorageReference imageStorageRef, ImageView imageView,
                                           int width, int height) {
        glideRequests.load(imageStorageRef)
                .apply(new RequestOptions().centerCrop().override(width,height).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.ic_stub))
                .into(imageView);
    }

    public static void loadImageCenterCrop(RequestManager glideRequests, StorageReference imageStorageRef, ImageView imageView,
                                           int width, int height, RequestListener<Drawable> listener) {
        glideRequests.load(imageStorageRef)
                .apply(new RequestOptions().centerCrop())
                .apply(new RequestOptions().override(width, height))
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.ic_stub))
                .listener(listener)
                .into(imageView);
    }

    public static void loadMediumImageCenterCrop(RequestManager glideRequests,
                                                 StorageReference imageStorageRefMedium,
                                                 StorageReference imageStorageRefOriginal,
                                                 ImageView imageView,
                                                 int width,
                                                 int height,
                                                 RequestListener<Drawable> listener) {


        glideRequests.load(imageStorageRefMedium)
                .apply(new RequestOptions().centerCrop())
                .apply(new RequestOptions().override(width,height))
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.ic_stub))
                .listener(listener)
                .error(glideRequests.load(imageStorageRefOriginal))
                .into(imageView);
    }

    public static void loadImageCenterCrop(RequestManager glideRequests, String url, ImageView imageView,
                                           int width, int height, RequestListener<Drawable> listener) {
        glideRequests.load(url)
                .apply(new RequestOptions().centerCrop())
                .apply(new RequestOptions().override(width,height))
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.ic_stub))
                .listener(listener)
                .into(imageView);
    }

    public static void loadImageCenterCrop(RequestManager glideRequests, StorageReference imageStorageRef, ImageView imageView,
                                           RequestListener<Drawable> listener) {

        glideRequests.load(imageStorageRef)
                .apply(new RequestOptions().centerCrop())
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.ic_stub))
                .listener(listener)
                .into(imageView);
    }

    public static void loadImageCenterCrop(RequestManager glideRequests, String url, ImageView imageView,
                                           RequestListener<Drawable> listener) {

        glideRequests.load(url)
                .apply(new RequestOptions().centerCrop())
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.ic_stub))
                .listener(listener)
                .into(imageView);
    }


    @Nullable
    public static Bitmap loadBitmap(RequestManager glideRequests, String url, int width, int height) {
        try {
            return glideRequests.asBitmap()
                    .load(url)
                    .apply(new RequestOptions().centerCrop())
                    .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.DATA))
                    .submit(width, height)
                    .get();
        } catch (Exception e) {
            LogUtil.logError(TAG, "getBitmapfromUrl", e);
            return null;
        }
    }

    public static void loadImageWithSimpleTarget(RequestManager glideRequests, String url, SimpleTarget<Bitmap> simpleTarget) {
        glideRequests.asBitmap()
                .load(url)
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                .apply(new RequestOptions().fitCenter())
                .into(simpleTarget);
    }

    public static void loadImageWithSimpleTarget(RequestManager glideRequests, StorageReference imageStorageRef, SimpleTarget<Bitmap> simpleTarget) {
        glideRequests.asBitmap()
                .load(imageStorageRef)
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                .apply(new RequestOptions().fitCenter())
                .into(simpleTarget);
    }

    public static void loadLocalImage(RequestManager glideRequests, Uri uri, ImageView imageView,
                                      RequestListener<Drawable> listener) {
        glideRequests.load(uri)
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
                .apply(new RequestOptions().skipMemoryCache(true))
                .apply(new RequestOptions().fitCenter())
                .listener(listener)
                .into(imageView);
    }
}
