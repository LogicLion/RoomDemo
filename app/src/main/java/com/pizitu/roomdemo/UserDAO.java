package com.pizitu.roomdemo;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import io.reactivex.Flowable;

/**
 * @author wzh
 * @date 2018/3/16
 */
@Dao
public interface UserDAO {
    @Query("select * from user")
    Flowable<List<UserBean>> getUserList();

    @Query("select * from user where name=:name")
    Flowable<UserBean> getUserByName(String name);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addUser(UserBean userBean);

    @Delete
    void deleteUser(UserBean userBean);

    @Update
    void updateUser(UserBean userBean);
}
