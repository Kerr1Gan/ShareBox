package com.ecjtu.sharebox.db.room;

import android.arch.persistence.room.Room;
import android.content.Context;

public class RoomRepository {

    private ShareDatabase shareDatabase;

    public RoomRepository(Context context) {
        shareDatabase = Room.databaseBuilder(context.getApplicationContext(), ShareDatabase.class, "share.db").build();
    }

    public ShareDatabase getShareDatabase() {
        return shareDatabase;
    }
}
