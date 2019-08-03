package com.ethan.and.db.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.ethan.and.db.room.dao.IpMessageDao;
import com.ethan.and.db.room.entity.IpMessage;

@Database(entities = {IpMessage.class}, version = 1)
public abstract class ShareDatabase extends RoomDatabase {

    public abstract IpMessageDao ipMessageDao();
}
