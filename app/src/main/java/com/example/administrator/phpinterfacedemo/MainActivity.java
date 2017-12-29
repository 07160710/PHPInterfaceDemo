package com.example.administrator.phpinterfacedemo;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    String webUrl="http://10.0.2.2/phptest/area_api.php";//PHP接口地址。测试的时候，模拟器访问本地机器，地址应该填写http://10.0.2.2后面接你实际php部署的地址
    Spinner sp_province;
    TextView tv_show;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sp_province = (Spinner)findViewById(R.id.sp_province);
        tv_show = (TextView) findViewById(R.id.tv_show);

        GetPro getPro = new GetPro();//读取php的数据接口，获取省份，并填入下拉控件spinner中
        getPro.start();
        sp_province.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String pro = parent.getItemAtPosition(position).toString();//获取选择项的文本
                GetCityAndArea getCityAndArea = new GetCityAndArea(pro);
                getCityAndArea.start();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private final Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1234:
                    sp_province.setAdapter((SpinnerAdapter) msg.obj);
                    break;
                case 5678:
                    tv_show.setText((String)msg.obj);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    class GetPro extends Thread{
        @Override
        public void run() {//获取省份列表
            try {
                URL url = new URL(webUrl);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setConnectTimeout(5000);
                int code = httpURLConnection.getResponseCode();
                if(code==200){
                    InputStream in = httpURLConnection.getInputStream();
                    InputStreamReader din = new InputStreamReader(in);
                    BufferedReader bdin = new BufferedReader(din);
                    StringBuffer sbf = new StringBuffer();
                    String line = null;

                    while((line=bdin.readLine())!=null){
                        sbf.append(line);
                    }

                    String jsonData =new String(sbf.toString().getBytes(),"UTF-8") ; //此句非常重要！把字符串转为utf8编码，因为String 默认是unicode编码的。
                    JSONArray jsonArray = new JSONArray(jsonData);
                    List<String> list = new ArrayList<String>();
                    for(int i=0;i<jsonArray.length();i++){
                        String pro = jsonArray.opt(i).toString();
                        list.add(pro);
                    }
                    ArrayAdapter<String>adapter = new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_spinner_item,list);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                   // sp_province.setAdapter(adapter);//线程不能访问主线程activity的控件
                    Message msg = new Message();
                    msg.what = 1234;
                    msg.obj = adapter;
                    handler.sendMessage(msg);


                }else{
                    Looper.prepare();
                    Toast.makeText(MainActivity.this,"网址不可访问",Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }catch (Exception ee){
                Looper.prepare();
                Toast.makeText(MainActivity.this,"网络异常",Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }
    }
    class GetCityAndArea extends Thread{//输入省，获取下属市和区的信息
        private String pro="";
        public GetCityAndArea(String pro){
            try {
                this.pro = URLEncoder.encode(pro,"UTF-8");
            }catch (Exception ee){

            }
        }
        @Override
        public void run() {
            try {
                URL url = new URL(webUrl+"?province="+pro);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setConnectTimeout(5000);
                int code = httpURLConnection.getResponseCode();
                if(code==200){
                    InputStream in = httpURLConnection.getInputStream();
                    InputStreamReader din = new InputStreamReader(in);
                    BufferedReader bdin = new BufferedReader(din);
                    StringBuffer sbf = new StringBuffer();
                    String line = null;

                    while((line=bdin.readLine())!=null){
                        sbf.append(line);
                    }

                    String jsonData =new String(sbf.toString().getBytes(),"UTF-8") ; //此句非常重要！把字符串转为utf8编码，因为String 默认是unicode编码的。
                    JSONArray jsonArray = new JSONArray(jsonData);
                    StringBuffer cinf=new StringBuffer();
                    for(int i=0;i<jsonArray.length();i++){
                        String inf = "";
                        JSONObject jsonObject = new JSONObject(jsonArray.opt(i).toString());
                        inf ="市："+ jsonObject.getString("city")+"  "+jsonObject.getString("area")+"\n";
                        cinf.append(inf);
                    }

                    Message msg = new Message();
                    msg.what = 5678;
                    msg.obj = cinf.toString();
                    handler.sendMessage(msg);


                }else{
                    Looper.prepare();
                    Toast.makeText(MainActivity.this,"网址不可访问",Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }catch (Exception ee){
                Looper.prepare();
                Toast.makeText(MainActivity.this,"网络异常",Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }
    }

}
