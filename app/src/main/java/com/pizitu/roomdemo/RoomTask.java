package com.pizitu.roomdemo;

import android.arch.persistence.room.Room;
import android.content.Context;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * @author wzh
 * @date 2018/3/17
 */
public class RoomTask {
    UserDAO mDAO;
    List<UserBean> list;

    public RoomTask(Context context) {
        AppDatabase database = Room.databaseBuilder(context, AppDatabase.class, "user.db")
                .build();
        mDAO = database.userDAO();
    }

    public void add(UserBean user) {
        Observable.create(
                (ObservableOnSubscribe<UserBean>) emitter -> mDAO.addUser(user))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    public void delete(UserBean user) {
        Observable.create(
                (ObservableOnSubscribe<UserBean>) emitter -> mDAO.deleteUser(user))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    public void update(UserBean user) {
         Observable.create(
                (ObservableOnSubscribe<UserBean>) emitter -> mDAO.updateUser(user))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    public Flowable<UserBean> query(String name) {
        return mDAO.getUserByName(name)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Flowable<List<UserBean>> query() {
         return mDAO.getUserList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());


    }
}
