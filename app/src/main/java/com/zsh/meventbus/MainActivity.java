package com.zsh.meventbus;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.zsh.meventbus.MEventBus.EventBus;
import com.zsh.meventbus.MEventBus.annotation.Subscribe;
import com.zsh.meventbus.MEventBus.mode.ThreadMode;

public class MainActivity extends AppCompatActivity {
    private TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView=findViewById(R.id.tx_view);
        EventBus.getDefault().register(this);
    }
    public void operate(View v)
    {
        if(v.getId()==R.id.post){
            MessageBean bean=new MessageBean();
            bean.name="哥哥";
            bean.age=15;
            EventBus.getDefault().post(bean);
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getM(MessageBean bean){
        //Log.e("TAG",bean.name);
        textView.setText(bean.name+" "+bean.age);
    }


}
