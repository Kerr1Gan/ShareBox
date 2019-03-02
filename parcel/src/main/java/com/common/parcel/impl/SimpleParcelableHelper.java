package com.common.parcel.impl;

import android.os.Parcel;
import android.os.Parcelable;

import com.common.parcel.base.ParcelableFileCacheHelper;

/**
 * Created by Ethan_Xiang on 2017/8/22.
 */

public class SimpleParcelableHelper extends ParcelableFileCacheHelper {

    public SimpleParcelableHelper(String path) {
        super(path);
    }


    @Override
    public  <T> T readParcel(Parcel parcel) {
        return parcel.readParcelable(getClass().getClassLoader());
    }

    @Override
    public <T> Parcel writeParcel(Parcel parcel, T object) {
        if (object instanceof Parcelable) {
            parcel.writeParcelable((Parcelable) object, 0);
        }
        return parcel;
    }
}
