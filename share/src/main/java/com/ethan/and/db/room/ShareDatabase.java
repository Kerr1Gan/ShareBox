package com.ethan.and.db.room;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.ethan.and.db.room.dao.IpMessageDao;
import com.ethan.and.db.room.entity.IpMessage;

@Database(entities = {IpMessage.class}, version = 1)
public abstract class ShareDatabase extends RoomDatabase {

    public abstract IpMessageDao ipMessageDao();
}
