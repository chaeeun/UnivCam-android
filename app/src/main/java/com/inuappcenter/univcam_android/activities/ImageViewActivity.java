package com.inuappcenter.univcam_android.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.inuappcenter.univcam_android.R;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;

/**
 * Created by ichaeeun on 2017. 8. 16..
 */

public class ImageViewActivity extends AppCompatActivity{
    ImageView mImageView;
    PinchZoomImageView mPinchZoomImageView;
    RelativeLayout mContainer;

    private Uri mImageUri;
    private Animator mCurrentAnimator;
    private int mLongAnimationDuration;

    String cameraPath;

    boolean b = true;
    private static final int REQUEST_OPEN_RESULT_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        cameraPath = getIntent().getExtras().getString("cameraPath");

        mImageUri=Uri.fromFile(new File(cameraPath));


        mImageView = (ImageView) findViewById(R.id.imageView);
        mPinchZoomImageView = (PinchZoomImageView) findViewById(R.id.pinchZoomImageView);
        mContainer = (RelativeLayout)findViewById(R.id.container);

            Glide.with(getApplicationContext())
                    .load(cameraPath)
                    .into(mImageView);

        mContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (b) {
                    pinchZoomPan();
                    b = false;
                } else {
                    pinchOriginal();
                    b = true;
                }
            }
        });

        mLongAnimationDuration = getResources().getInteger(android.R.integer.config_longAnimTime);


    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        View decorView = getWindow().getDecorView();
        if(hasFocus) {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            );
        }
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
//        if(requestCode == REQUEST_OPEN_RESULT_CODE && resultCode == RESULT_OK) {
//            if(resultData != null) {
//                mImageUri = resultData.getData();
//                /*
//                try {
//                    Bitmap bitmap = getBitmapFromUri(uri);
//                    mImageView.setImageBitmap(bitmap);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                */
//                Glide.with(this)
//                        .load(mImageUri)
//                        .into(mImageView);
//            }
//        }
//    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return bitmap;
    }

    private void zoomImageFromThumb() {
        if(mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        Glide.with(this)
                .load(cameraPath)
                .into(mPinchZoomImageView);

        Rect startBounds = new Rect();
        Rect finalBounds = new Rect();
        Point globalOffset = new Point();
        mImageView.getGlobalVisibleRect(startBounds);
        findViewById(R.id.container)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        float startScale;
        if((float) finalBounds.width() /finalBounds.height() >
                (float) startBounds.width() / startBounds.height()) {
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = (float) startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = (float) startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        mImageView.setAlpha(0f);
        mPinchZoomImageView.setVisibility(View.VISIBLE);

        mPinchZoomImageView.setPivotX(0f);
        mPinchZoomImageView.setPivotY(0f);

        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(mPinchZoomImageView, View.X, startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(mPinchZoomImageView, View.Y, startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(mPinchZoomImageView, View.SCALE_X, startScale, 1f))
                .with(ObjectAnimator.ofFloat(mPinchZoomImageView, View.SCALE_Y, startScale, 1f));
        set.setDuration(mLongAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);

                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;
    }

    private void pinchZoomPan() {
        mPinchZoomImageView.setImageUri(mImageUri);
//        Glide.with(getApplicationContext())
//                .load(cameraPath)
//                .into(mPinchZoomImageView);
        mImageView.setAlpha(0.f);
        mPinchZoomImageView.setVisibility(View.VISIBLE);
    }

    private void pinchOriginal() {
        mPinchZoomImageView.setImageUri(mImageUri);
//        Glide.with(getApplicationContext())
//                .load(cameraPath)
//                .into(mPinchZoomImageView);
        mImageView.setAlpha(1.f);
        mPinchZoomImageView.setVisibility(View.INVISIBLE);
    }
}
