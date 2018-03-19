package com.pizitu.roomdemo;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

/**
 * @author wzh
 * @date 2018/3/17
 */
@Database(entities = {UserBean.class},version = 1,exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    //单例
    private static AppDatabase sInstance;
    public static AppDatabase getDatabase(Context context) {
        if (sInstance == null) {
            sInstance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class,
                    "user.db").build();
        }
        return sInstance;
    }
    public static void onDestroy() {
        sInstance = null;
    }

    //声明提供DAO对象
    public abstract UserDAO userDAO();
}
