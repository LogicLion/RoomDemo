package com.pizitu.roomdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tv_info)
    TextView mTvInfo;
    private RoomTask mTask;
    private UserBean user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        mTvInfo.setMovementMethod(ScrollingMovementMethod.getInstance());

        mTask = new RoomTask(this);

        //Flow每次增删改，都会执行
        mTask.query()
                .subscribe(userBeans -> mTvInfo.setText(userBeans.toString()));
    }

    @OnClick({R.id.btn_add,R.id.btn_delete,R.id.btn_update,R.id.btn_clear})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add:
                user = new UserBean(1,"小明", "12");
                mTask.add(user);
                break;
            case R.id.btn_delete:
                user = new UserBean(1,"小明", "12");
                mTask.delete(user);
                break;
            case R.id.btn_update:
                user.setAge("15");
                mTask.update(user);
                break;

            case R.id.btn_clear:
                mTvInfo.setText("");
                break;
            default:
                break;
        }
    }
}
