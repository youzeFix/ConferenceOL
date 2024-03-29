package com.usiellau.conferenceol.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;


import com.usiellau.conferenceol.R;
import com.usiellau.conferenceol.network.ConfSvMethods;
import com.usiellau.conferenceol.network.HttpResult;
import com.usiellau.conferenceol.network.entity.User;
import com.usiellau.conferenceol.tools.BitmapCacher;
import com.usiellau.conferenceol.tools.ImageLoader;
import com.usiellau.conferenceol.util.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by UsielLau on 2018/1/19 0019 4:03.
 */

public class LoginActivity extends AppCompatActivity{

    @BindView(R.id.et_username)
    EditText etUsername;
    @BindView(R.id.et_password)
    EditText etPassword;
    @BindView(R.id.btn_register)
    Button btnRegister;
    @BindView(R.id.btn_login)
    Button btnLogin;
    @BindView(R.id.iv_background)
    ImageView ivBackground;

    ProgressDialog progressDialog;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //定义全屏参数
        int flag= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        //设置当前窗体为全屏显示
        getWindow().setFlags(flag, flag);
        ButterKnife.bind(this);
        autoCompletedIfLastLogined();
    }



    @OnClick(R.id.btn_login)
    public void clickBtnLogin(){
        ConfSvMethods confSvMethods=ConfSvMethods.getInstance();
        final String username=etUsername.getText().toString();
        final String password=etPassword.getText().toString();
        if(!Utils.isMobiPhoneNum(username)){
            Toast.makeText(this, "非法用户名", Toast.LENGTH_SHORT).show();
            return;
        }


        confSvMethods.login(new Observer<HttpResult<User>>() {
            @Override
            public void onSubscribe(Disposable d) {
                showProgressDialog();
            }

            @Override
            public void onNext(HttpResult<User> userHttpResult) {
                int code=userHttpResult.getCode();
                String msg=userHttpResult.getMsg();
                User user=userHttpResult.getResult();
                if(code==0){
                    Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                    Utils.saveLastLogined(getBaseContext(),user);
                    Intent intent=new Intent(LoginActivity.this,ConfManageActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                }
                Log.d("LoginActivity",userHttpResult.toString());
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                Toast.makeText(LoginActivity.this, "error", Toast.LENGTH_SHORT).show();
                closeProgressDialog();
            }

            @Override
            public void onComplete() {
                closeProgressDialog();
            }
        },username,password);
    }

    private void autoCompletedIfLastLogined(){
        String username = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("username", null);
        if (TextUtils.isEmpty(username)) {
            return;
        }
        String password = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("password", null);
        etUsername.setText(username);
        etPassword.setText(password);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        ivBackground.setImageBitmap(ImageLoader.decodeSampledBitmapFromResource(getResources(),
                R.drawable.pic_guide_girl,ivBackground.getWidth(),ivBackground.getHeight()));
    }

    @OnClick(R.id.btn_register)
    public void clickBtnRegister(){
        Intent intent=new Intent(this,RegisterActivity.class);
        startActivity(intent);
    }

    private void showProgressDialog(){
        if(progressDialog==null){
            progressDialog=new ProgressDialog(this);
            progressDialog.setMessage("登陆中...");
        }
        progressDialog.show();
    }

    private void closeProgressDialog(){
        progressDialog.cancel();
    }


}
