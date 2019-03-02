package com.ethan.and.db.room.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.ethan.and.db.room.entity.IpMessage;

import java.util.List;

@Dao
public interface IpMessageDao {
    @Query("SELECT * FROM ip_message ORDER BY _id DESC")
    List<IpMessage> getAllMessage();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(IpMessage sms);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(IpMessage sms);
}
