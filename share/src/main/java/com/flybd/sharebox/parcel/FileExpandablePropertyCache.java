package com.flybd.sharebox.parcel;


import android.os.Parcel;

import com.common.parcel.base.ParcelableFileCacheHelper;
import com.ethan.and.ui.holder.FileExpandableInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ethan_Xiang on 2017/10/11.
 */

public class FileExpandablePropertyCache extends ParcelableFileCacheHelper {

    public FileExpandablePropertyCache(String path) {
        super(path);
    }

    @Override
    public <T> T readParcel(Parcel parcel) {
        int size = parcel.readInt();
        List<FileExpandableInfo> ret = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            List<String> childList = new ArrayList<>();
            parcel.readStringList(childList);
            String group = parcel.readString();
            boolean isActivated = (boolean) parcel.readValue(null);
            List<String> activatedList = new ArrayList<>();
            parcel.readStringList(activatedList);

            FileExpandableInfo child = new FileExpandableInfo(group, childList);
            child.setActivated(isActivated);
            child.setActivatedList(activatedList);

            ret.add(child);
        }
        return (T) ret;
    }

    @Override
    public  <T> Parcel writeParcel(Parcel parcel, T object) {
        if (!(object instanceof List)) {
            return null;
        }
        List list = (List) object;
        parcel.writeInt(list.size());
        for (int i = 0; i < list.size(); i++) {
            if (!(list.get(i) instanceof FileExpandableInfo)) {
                break;
            }
            FileExpandableInfo vh = (FileExpandableInfo) list.get(i);
            List<String> childList = vh.getChildList();
            parcel.writeStringList(childList);
            parcel.writeString(vh.getGroup());
            parcel.writeValue(vh.isActivated());
            parcel.writeStringList(vh.getActivatedList());
        }
        return parcel;
    }
}
