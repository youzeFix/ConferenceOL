package com.usiellau.conferenceol.activity;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.usiellau.conferenceol.JCWrapper.JCManager;
import com.usiellau.conferenceol.R;
import com.usiellau.conferenceol.adapter.ConfRvAdapter;
import com.usiellau.conferenceol.network.ConfSvMethods;
import com.usiellau.conferenceol.network.HttpResult;
import com.usiellau.conferenceol.network.entity.ConfIng;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by UsielLau on 2018/1/22 0022 2:49.
 */

public class ConfManageActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    Toolbar toolbar;
    DrawerLayout drawer;
    NavigationView navigationView;
    RecyclerView rvConfList;
    SwipeRefreshLayout refreshLayout;

    SpotsDialog progressDialog;

    ConfRvAdapter confListAdapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conf_manage);
        ButterKnife.bind(this);
        initViews();
        refreshConfList();
    }

    private void initViews(){
        toolbar=findViewById(R.id.toolbar);
        toolbar.setTitle("会议列表");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        drawer=findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        rvConfList=findViewById(R.id.rv_conf_list);
        rvConfList.setLayoutManager(new LinearLayoutManager(this));
        rvConfList.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        confListAdapter=new ConfRvAdapter(this,new ArrayList<ConfIng>());
        rvConfList.setAdapter(confListAdapter);
        confListAdapter.setOnItemClickListener(new ConfRvAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                showPwdInputDialog(position);
            }
        });


        refreshLayout=findViewById(R.id.refresh_layout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d("ConfManageActivity","onRefresh.......");
                refreshConfList();
            }
        });

    }


    private void showPwdInputDialog(final int position){
        MaterialDialog dialog=new MaterialDialog.Builder(this)
                .input("请输入房间密码", null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        attemptEnterRoom(position,input.toString());
                    }
                })
                .inputType(InputType.TYPE_TEXT_VARIATION_PASSWORD)
                .title("提示")
                .show();
    }

    private void attemptEnterRoom(int position,String pwd){
        ConfIng conf=confListAdapter.getData().get(position);
        if(conf.getPassword().equals(pwd)){
            enterRoom(conf);
        }else{
            Toast.makeText(this, "密码错误", Toast.LENGTH_SHORT).show();
        }
    }

    private void enterRoom(ConfIng confIng){
        ConfSvMethods.getInstance().enterRoom(new Observer<HttpResult>() {
            @Override
            public void onSubscribe(Disposable d) {
                showProgressDialog();
            }

            @Override
            public void onNext(HttpResult httpResult) {

            }

            @Override
            public void onError(Throwable e) {
                closeProgressDialog();
                Toast.makeText(ConfManageActivity.this, "error", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onComplete() {
                closeProgressDialog();
            }
        },confIng.getRoomId(), PreferenceManager.getDefaultSharedPreferences(ConfManageActivity.this).getString(getString(R.string.cloud_setting_last_login_user_id),""));
        if(JCManager.getInstance().mediaChannel.join(confIng.getChannelId(),null)){
            Intent intent=new Intent(this,ConferenceActivity.class);
            intent.putExtra("roomId",confIng.getRoomId());
            startActivity(intent);
        }
        Toast.makeText(this, "进入房间"+confIng.getRoomId(), Toast.LENGTH_SHORT).show();
    }


    private void refreshConfList(){
        Log.d("ConfManageActivity","refreshConfList............");
        ConfSvMethods.getInstance().queryAllConfIng(new Observer<HttpResult<List<ConfIng>>>() {
            @Override
            public void onSubscribe(Disposable d) {
                refreshLayout.setRefreshing(true);
            }

            @Override
            public void onNext(HttpResult<List<ConfIng>> confIngHttpResult) {
                int code=confIngHttpResult.getCode();
                String msg=confIngHttpResult.getMsg();
                List<ConfIng> data=confIngHttpResult.getResult();
                Log.d("ConfManageActivity","会议查询记录数："+data.size());
                if(code==0){
                    confListAdapter.setData(data);
                    confListAdapter.notifyDataSetChanged();
                }else{

                }
            }

            @Override
            public void onError(Throwable e) {
                refreshLayout.setRefreshing(false);
                Toast.makeText(ConfManageActivity.this, "error", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onComplete() {
                refreshLayout.setRefreshing(false);
            }
        });
    }

    private void showProgressDialog(){
        if(progressDialog==null){
            progressDialog=new SpotsDialog(this,R.style.login_progress_dialog);
        }
        progressDialog.show();
    }

    private void closeProgressDialog(){
        progressDialog.cancel();
    }

    /**
     * 点击手机back键时的侧滑菜单逻辑处理
     */
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_conference_manage,menu);
        return true;
    }

    /**
     * toolbar点击事件
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_create_conf:
                Intent intent=new Intent(this,CreateConfActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
        return true;
    }


    /**
     * 侧滑菜单点击事件
     * @param item
     * @return
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_personal:
                Toast.makeText(this, "personal", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_setting:
                Toast.makeText(this, "setting", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_about:
                Toast.makeText(this, "about", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}