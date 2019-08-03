package com.ethan.and.db.room;

import androidx.room.Room;
import android.content.Context;

public class RoomRepository {

    private ShareDatabase shareDatabase;

    public RoomRepository(Context context) {
        shareDatabase = Room.databaseBuilder(context.getApplicationContext(), ShareDatabase.class, "share.db")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();
    }

    public ShareDatabase getShareDatabase() {
        return shareDatabase;
    }
}
