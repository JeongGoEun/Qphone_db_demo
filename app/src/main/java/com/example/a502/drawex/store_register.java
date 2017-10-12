package com.example.a502.drawex;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by 502 on 2017-10-01.
 */

public class store_register extends Fragment {
    ViewGroup rootView;
    TextView storeCode;
    EditText storeName, storeCall, storePosition;
    Button registerBtn;
    loadJsp task;

    String postURL = "http://192.168.10.2:8090/test/directorFile.jsp", code, shop_name, shop_phone, shop_position,shop_code;    //10.02 ip update OK

    private String[] getJsonData = {"", "", "", ""};

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.store_register, container, false);
        storeName = rootView.findViewById(R.id.storeName);
        storeCall = rootView.findViewById(R.id.storeCall);
        storePosition = rootView.findViewById(R.id.storePosition);
        storeCode = rootView.findViewById(R.id.storeCode);
        registerBtn = rootView.findViewById(R.id.store_register_btn);

        registerBtn.setOnClickListener(new View.OnClickListener() {  //회원가입 리스너 등록
            public void onClick(View v) {    //회원가입 버튼
                shop_name = storeName.getText().toString();     //가게이름
                shop_phone = storeCall.getText().toString();    //가게번호
                shop_position = storePosition.getText().toString();     //가게위치

                task = new loadJsp("shop_register"); //회원가입
                task.execute();
            }
        });

        task = new loadJsp("getCode");    //맨처음 shop_code 가져오기기
        task.execute();
        return rootView;
    }

    class loadJsp extends AsyncTask<Void, String, Void> {
        String code;

        loadJsp(String code) {
            this.code = code;
            Log.e("생성자", code + "");
        }

        protected Void doInBackground(Void... params) {

            Log.e("register", "가게등록 버튼" + code + "");
            if (code.equals("shop_register")) {
                try {
                    HttpClient client = new DefaultHttpClient();
                    //ip주소
                    HttpPost post = new HttpPost(postURL);
                    ArrayList<NameValuePair> param = new ArrayList<NameValuePair>();
                    param.add(new BasicNameValuePair("shop_code", shop_code));  //가게이름
                    param.add(new BasicNameValuePair("shop_name", shop_name));  //가게이름
                    param.add(new BasicNameValuePair("shop_position", shop_position));  //장소
                    param.add(new BasicNameValuePair("shop_phone", shop_phone));  //번호
                   // param.add(new BasicNameValuePair("shop_code", "goeun7"));  //이미지
                    param.add(new BasicNameValuePair("code", code)); //코드 : 회원가입

                    UrlEncodedFormEntity ent = new UrlEncodedFormEntity(param, HTTP.UTF_8);
                    post.setEntity(ent);

                    HttpResponse responsePost = client.execute(post);
                    HttpEntity resEntity = responsePost.getEntity();

                    BufferedReader bufReader = new BufferedReader(new InputStreamReader(responsePost.getEntity().getContent(), "utf-8"));
                    String line = null;
                    String result = "";

                    //줄단위로 읽어오기
                    while ((line = bufReader.readLine()) != null) {
                        result += line;
                        Log.e("register data : ", line);
                    }

                    JSONObject json = new JSONObject(result);
                    JSONArray jArr = json.getJSONArray("dataSend");

                    for (int i = 0; i < jArr.length(); i++) {
                        json = jArr.getJSONObject(i);
                        getJsonData[0] = json.getString("code");  //shop_code받아오기
                    }
                    makeToast("등록되었습니다.");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (code.equals("getCode")) {
                try {
                    HttpClient client = new DefaultHttpClient();
                    //ip주소
                    HttpPost post = new HttpPost(postURL);
                    ArrayList<NameValuePair> param = new ArrayList<NameValuePair>();

                    param.add(new BasicNameValuePair("id", "goeun2"));
                    param.add(new BasicNameValuePair("code", code)); //코드 : 회원가입

                    UrlEncodedFormEntity ent = new UrlEncodedFormEntity(param, HTTP.UTF_8);
                    post.setEntity(ent);
                    HttpResponse responsePost = client.execute(post);
                    HttpEntity resEntity = responsePost.getEntity();

                    BufferedReader bufReader = new BufferedReader(new InputStreamReader(responsePost.getEntity().getContent(), "utf-8"));
                    String line = null;
                    String result = "";

                    //줄단위로 읽어오기
                    while ((line = bufReader.readLine()) != null) {
                        result += line;
                        Log.e("getCode data : ", line);
                    }

                    JSONObject json = new JSONObject(result);
                    JSONArray jArr = json.getJSONArray("dataSend");

                    for (int i = 0; i < jArr.length(); i++) {
                        json = jArr.getJSONObject(i);
                        getJsonData[0] = json.getString("code");
                        shop_code=getJsonData[0].toString();    //shop_code설정
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }

    final Handler handler = new Handler() {
        public void handleMessage(Message msg)
        {
            storeCode.setText(getJsonData[0].toString());   //가게코드 세팅
            storeCode.setTextColor(Color.RED);
        }
    };

    public void makeToast(String str) {
        Message status = toaster.obtainMessage();
        Bundle datax = new Bundle();
        datax.putString("msg", str);
        status.setData(datax);
        toaster.sendMessage(status);
    }
    public Handler toaster = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Toast.makeText(getContext(), msg.getData().getString("msg"), Toast.LENGTH_SHORT).show();
        }
    };
}
