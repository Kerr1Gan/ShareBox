//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.ethan.and.ui.sendby.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;

import com.google.android.gms.base.R.styleable;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.internal.SignInButtonCreator;
import com.google.android.gms.common.internal.SignInButtonImpl;
import com.google.android.gms.dynamic.RemoteCreator.RemoteCreatorException;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class MSignInButton extends FrameLayout implements OnClickListener {
    public static final int SIZE_STANDARD = 0;
    public static final int SIZE_WIDE = 1;
    public static final int SIZE_ICON_ONLY = 2;
    public static final int COLOR_DARK = 0;
    public static final int COLOR_LIGHT = 1;
    public static final int COLOR_AUTO = 2;
    private int mSize;
    private int mColor;
    private View zaau;
    private OnClickListener zaav;

    public MSignInButton(Context var1) {
        this(var1, (AttributeSet) null);
    }

    public MSignInButton(Context var1, AttributeSet var2) {
        this(var1, var2, 0);
    }

    public MSignInButton(Context var1, AttributeSet var2, int var3) {
        super(var1, var2, var3);
        this.zaav = null;
        MSignInButton var4 = this;
        TypedArray var7 = var1.getTheme().obtainStyledAttributes(var2, styleable.SignInButton, 0, 0);

        try {
            var4.mSize = var7.getInt(styleable.SignInButton_buttonSize, 0);
            var4.mColor = var7.getInt(styleable.SignInButton_colorScheme, 2);
        } finally {
            var7.recycle();
        }

        this.setStyle(this.mSize, this.mColor);
    }

    public final void setSize(int var1) {
        this.setStyle(var1, this.mColor);
    }

    public final void setColorScheme(int var1) {
        this.setStyle(this.mSize, var1);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public final void setScopes(Scope[] var1) {
        this.setStyle(this.mSize, this.mColor);
    }

    public final void setStyle(int var1, int var2) {
        this.mSize = var1;
        this.mColor = var2;
        Context var4 = this.getContext();
        MSignInButton var3 = this;
        if (this.zaau != null) {
            this.removeView(this.zaau);
        }

        try {
            var3.zaau = SignInButtonCreator.createView(var4, var3.mSize, var3.mColor);
        } catch (RemoteCreatorException var9) {
            Log.w("SignInButton", "Sign in button not found, using placeholder instead");
            int var7 = this.mColor;
            int var6 = this.mSize;
            SignInButtonImpl var8;
            (var8 = new SignInButtonImpl(var4)).configure(var4.getResources(), var6, var7);
            this.zaau = var8;
        }

        this.addView(this.zaau);
        this.zaau.setEnabled(this.isEnabled());
        this.zaau.setOnClickListener(this);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public final void setStyle(int var1, int var2, Scope[] var3) {
        this.setStyle(var1, var2);
    }

    public final void setOnClickListener(OnClickListener var1) {
        this.zaav = var1;
        if (this.zaau != null) {
            this.zaau.setOnClickListener(this);
        }

    }

    public final void setEnabled(boolean var1) {
        super.setEnabled(var1);
        this.zaau.setEnabled(var1);
    }

    public final void onClick(View var1) {
        if (this.zaav != null && var1 == this.zaau) {
            this.zaav.onClick(this);
        }

    }

    public Button getSignInButton() {
        return (Button) zaau;
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ColorScheme {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ButtonSize {
    }
}
