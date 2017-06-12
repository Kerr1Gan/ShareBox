package com.newindia.sharebox.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

public class CircleProgressView extends View {

    private static final String TAG = "CircleProgressBar";

    private float mMaxProgress = 100;

    private float mProgress = 30;

    private final int mCircleLineStrokeWidth = 8;

    private final int mTxtStrokeWidth = 2;

    // 画圆所在的距形区域
    private final RectF mRectF;

    private final Paint mPaint;

    private final Context mContext;

    private String mTxtHint1;

    private String mTxtHint2;

    private int mBackgroundColor;

    private int mProgressColor;

    private boolean mSolid=true;

    private int mProgressDis=10;

    private boolean mAnimate=false;

    private int mAnimTime=0;

    private float mCurProgress;

    private float mIncrement;

    private int mTextColor;

    private String mStartText="";

    private boolean mShowStartText=true;

    public CircleProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        mRectF = new RectF();
        mPaint = new Paint();

        mBackgroundColor=getBackgroundColor();
        mProgress=getBackgroundColor();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = this.getWidth();
        int height = this.getHeight();

        if (width != height) {
            int min = Math.min(width, height);
            width = min;
            height = min;
        }

        // 设置画笔相关属性
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.rgb(0xe9, 0xe9, 0xe9));
        canvas.drawColor(Color.TRANSPARENT);
        mPaint.setStrokeWidth(mCircleLineStrokeWidth);
        mPaint.setStyle(Paint.Style.STROKE);
        // 位置
        mRectF.left = mCircleLineStrokeWidth / 2 + mProgressDis/2 -1; // 左上角x
        mRectF.top = mCircleLineStrokeWidth / 2 + mProgressDis/2 -1; // 左上角y
        mRectF.right = width - mCircleLineStrokeWidth / 2 - mProgressDis/2 +1; // 右下角x
        mRectF.bottom = height - mCircleLineStrokeWidth / 2 - mProgressDis/2 +1; // 右下角y


        // 绘制圆圈，进度条背景
        if(mSolid){
            mPaint.setStyle(Paint.Style.FILL);
        }
        mPaint.setColor(mBackgroundColor);
        canvas.drawArc(mRectF, -90, 360, false, mPaint);

        // 位置
        mRectF.left = mCircleLineStrokeWidth / 2; // 左上角x
        mRectF.top = mCircleLineStrokeWidth / 2; // 左上角y
        mRectF.right = width - mCircleLineStrokeWidth / 2; // 右下角x
        mRectF.bottom = height - mCircleLineStrokeWidth / 2; // 右下角y

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(mProgressColor);
        canvas.drawArc(mRectF, -90, ((float) mCurProgress / mMaxProgress) * 360, false, mPaint);

        // 绘制进度文案显示
        mPaint.setColor(mTextColor);
        mPaint.setStrokeWidth(mTxtStrokeWidth);
        String text;
        if(mShowStartText){
            text=mStartText;
        }else
            text = mCurProgress + "%";

        int textHeight = height / 4;
        mPaint.setTextSize(textHeight);
        int textWidth = (int) mPaint.measureText(text, 0, text.length());
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawText(text, width / 2 - textWidth / 2, height / 2 + textHeight / 2, mPaint);

        if (!TextUtils.isEmpty(mTxtHint1)) {
            mPaint.setStrokeWidth(mTxtStrokeWidth);
            text = mTxtHint1;
            textHeight = height / 8;
            mPaint.setTextSize(textHeight);
            mPaint.setColor(Color.rgb(0x99, 0x99, 0x99));
            textWidth = (int) mPaint.measureText(text, 0, text.length());
            mPaint.setStyle(Paint.Style.FILL);
            canvas.drawText(text, width / 2 - textWidth / 2, height / 4 + textHeight / 2, mPaint);
        }

        if (!TextUtils.isEmpty(mTxtHint2)) {
            mPaint.setStrokeWidth(mTxtStrokeWidth);
            text = mTxtHint2;
            textHeight = height / 8;
            mPaint.setTextSize(textHeight);
            textWidth = (int) mPaint.measureText(text, 0, text.length());
            mPaint.setStyle(Paint.Style.FILL);
            canvas.drawText(text, width / 2 - textWidth / 2, 3 * height / 4 + textHeight / 2, mPaint);
        }

        if(mAnimate)
            startAnimate();
    }

    public float getMaxProgress() {
        return mMaxProgress;
    }

    public void setMaxProgress(int maxProgress) {
        this.mMaxProgress = maxProgress;
    }

    public void setProgress(int progress) {
        setProgress(progress,false,0);
    }

    public boolean setProgress(int progress,boolean animate,int time){
        if(mAnimate) return false;
        this.mProgress = progress;
        if(!animate) {
            mCurProgress=progress;
            this.invalidate();
            return false;
        }
        mShowStartText=false;
        mAnimate=animate;
        mAnimTime=time;
        mIncrement=0;
        startAnimate();
        return true;
    }

    public void setProgressNotInUiThread(int progress) {
        this.mProgress = progress;
        this.postInvalidate();
    }

    public String getTxtHint1() {
        return mTxtHint1;
    }

    public void setTxtHint1(String txtHint1) {
        this.mTxtHint1 = txtHint1;
    }

    public String getTxtHint2() {
        return mTxtHint2;
    }

    public void setTxtHint2(String txtHint2) {
        this.mTxtHint2 = txtHint2;
    }

    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.mBackgroundColor = backgroundColor;
    }

    public int getProgressColor() {
        return mProgressColor;
    }

    public void setProgressColor(int progressColor) {
        this.mProgressColor = progressColor;
    }

    public void setSolid(boolean solid){
        mSolid=solid;
    }

    public boolean isSolid(){
        return mSolid;
    }

    private void startAnimate(){
        if(mAnimate){
            if(mIncrement==0)
                mIncrement= (float) ((mProgress*1.0-mCurProgress*1.0)/(mAnimTime*1.0/100*1.0));

            mCurProgress+=mIncrement;
            if(mCurProgress>mProgress){
                mCurProgress=mProgress;
                mAnimate=false;
            }
            postInvalidateDelayed(100);
        }
    }

    public void setTextColor(int color){
        mTextColor=color;
    }

    public int getTextColor(){
        return mTextColor;
    }

    public void setStartText(String text){
        mCurProgress=0;
        mProgress=0;
        mAnimate=false;
        mStartText=text;
        mShowStartText=true;
        invalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mAnimate=false;
    }
}