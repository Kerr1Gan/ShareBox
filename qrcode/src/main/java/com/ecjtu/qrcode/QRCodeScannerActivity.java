package com.ecjtu.qrcode;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;

/**
 * Created by Ethan_Xiang on 2017/10/19.
 */

public class QRCodeScannerActivity extends AppCompatActivity implements QRCodeReaderView.OnQRCodeReadListener {

    private QRCodeReaderView mQRCodeReader;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        getWindow().getDecorView()
                .setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        setContentView(R.layout.activity_qrcode_scanner);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
//        upArrow.setColorFilter(getResources().getColor(R.color.grey), PorterDuff.Mode.SRC_ATOP);
//        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        toolbar.getNavigationIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        toolbar.setPadding(toolbar.getPaddingLeft(), toolbar.getPaddingTop() + getStatusBarHeight(), toolbar.getPaddingRight(), toolbar.getPaddingBottom());
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mQRCodeReader = (QRCodeReaderView) findViewById(R.id.qr_decoder);

        mQRCodeReader.setAutofocusInterval(2000L);
        mQRCodeReader.setOnQRCodeReadListener(this);
        mQRCodeReader.setBackCamera();
        mQRCodeReader.startCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mQRCodeReader != null) {
            mQRCodeReader.startCamera();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mQRCodeReader != null) {
            mQRCodeReader.stopCamera();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onQRCodeRead(String text, PointF[] points) {

    }

    private int getStatusBarHeight() {
        return getResources().getDimensionPixelOffset(getResources().getIdentifier("status_bar_height", "dimen", "android"));
    }
}
