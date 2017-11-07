package com.ecjtu.sharebox.ui.holder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ethan_Xiang on 2017/10/27.
 */

public class FileExpandableProperty implements Cloneable, Serializable {
    private List<String> childList;

    private String group;

    private boolean isActivated = false;

    private List<String> activatedList = new ArrayList<>();

    public FileExpandableProperty(String group, List<String> childList) {
        this.group = group;
        this.childList = childList;
    }

    public boolean isActivated() {
        return isActivated;
    }

    public void setActivated(boolean activated) {
        isActivated = activated;
    }

    public void activate(boolean active) {
        isActivated = active;
        if (isActivated) {
            activatedList.clear();
            activatedList.addAll(childList);
        } else {
            activatedList.clear();
        }
    }

    public void activateItem(boolean active, String file) {
        if (active) {
            if (activatedList.indexOf(file) < 0) {
                activatedList.add(file);
            }
            if (activatedList.size() == childList.size()) {
                isActivated = true;
            }
        } else {
            activatedList.remove(file);
            if (activatedList.size() != childList.size()) {
                isActivated = false;
            }
        }
    }

    public boolean isItemActivated(String file) {
        return activatedList.indexOf(file) >= 0;
    }

    public List<String> getActivatedList() {
        return activatedList;
    }

    public void setActivatedList(List<String> list) {
        activatedList = list;
    }

    public List<String> getChildList() {
        return childList;
    }

    public void setChildList(List<String> childList) {
        this.childList = childList;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
