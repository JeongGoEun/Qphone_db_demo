package com.example.a502.drawex;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
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
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by 502 on 2017-10-02.
 */

public class store_notice_click extends Fragment {
    EditText titleEdit;
    EditText contentEdit;
    String postURL = "http://192.168.35.212:8090/test/noticeFile.jsp";    //10.02 ip update OK
    String shop_code,notice_title,notice_text,notice_day;
    TextView titleText;
    TextView contentText;
    Button btn,createBtn; //등록버튼, new
    Button alterBtn,deleteBtn,cancelBtn;
    ViewGroup rootView;
    String[] getJsonData = {"", "",""};
    loadJsp task;
    Bundle bundle;
    int listIndex;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.store_notice_click, container, false);

        titleEdit = rootView.findViewById(R.id.titleEdit);
        contentEdit = rootView.findViewById(R.id.contentEdit);
        titleText = rootView.findViewById(R.id.titleText);
        contentText = rootView.findViewById(R.id.contentText);

        bundle = this.getArguments();
        if (bundle != null) {
            Log.e("log1", "notNull");
            String value = bundle.getString("key");
            if (value.equals("newNotice")) {
                Log.e("log1", "newNotice");
                titleEdit.setVisibility(View.VISIBLE);
                contentEdit.setVisibility(View.VISIBLE);

                titleText.setVisibility(View.INVISIBLE);
                contentText.setVisibility(View.INVISIBLE);

                createBtn = rootView.findViewById(R.id.editBtn);
                createBtn.setText("등록");
                createBtn.setOnClickListener(new View.OnClickListener() { //리스너 등록
                    @Override
                    public void onClick(View view) {

                        notice_title = titleText.getText().toString();
                        notice_text = contentEdit.getText().toString();
                        notice_day =getCurDate();

                        Log.e("공지사항 생성",notice_day+"--"+notice_text+"--"+notice_title);

                        task=new loadJsp("create");
                        task.execute();

                    }
                });

                btn = rootView.findViewById(R.id.delBtn);
                btn.setVisibility(View.INVISIBLE);

            } else if (value.equals("showNotice")) {    //수정, 삭제, 취소
                Log.e("log1", "showNotice");
                titleEdit.setVisibility(View.VISIBLE);  //수정 가능하도록
                contentEdit.setVisibility(View.VISIBLE);
                titleEdit.setText(bundle.getString("title"));
                contentEdit.setText(bundle.getString("content"));

                titleText.setVisibility(View.INVISIBLE);
                contentText.setVisibility(View.INVISIBLE);

                listIndex=bundle.getInt("getIndex")+1;    //쿼리 위한 리스트 인덱스 얻어오기
                Log.e("리스트 인덱스",listIndex+"");

                alterBtn=rootView.findViewById(R.id.editBtn);   //버튼들 등록
                deleteBtn=rootView.findViewById(R.id.delBtn);
                cancelBtn=rootView.findViewById(R.id.calBtn);

                alterBtn.setOnClickListener(new View.OnClickListener() {    //수정 버튼
                    @Override
                    public void onClick(View view) {
                        notice_title = titleText.getText().toString();
                        notice_text = contentEdit.getText().toString();
                        notice_day =getCurDate();

                        Log.e("공지사항 수정",notice_day+"--"+notice_text+"--"+notice_title);

                        task=new loadJsp("alter");
                        task.execute();
                    }
                });

                deleteBtn.setOnClickListener(new View.OnClickListener() {   //공지사항 삭제
                    @Override
                    public void onClick(View view) {
                        task=new loadJsp("delete");
                        task.execute();
                    }
                });
            }
        }
        return rootView;
    }

    class loadJsp extends AsyncTask<Void, String, Void> {
        String code;
        Boolean stateCode=true;
        loadJsp(String code) {
            this.code = code;
            Log.e("생성자", code + "");
        }

        protected Void doInBackground(Void... params) {

            Log.e("create", "공지 생성" + code + "");
            if (code.equals("create")) {
                try {
                    HttpClient client = new DefaultHttpClient();
                    //ip주소
                    HttpPost post = new HttpPost(postURL);
                    ArrayList<NameValuePair> param = new ArrayList<NameValuePair>();

                    param.add(new BasicNameValuePair("shop_code", "1"));    //임의의 값

                    param.add(new BasicNameValuePair("notice_title", notice_title));
                    param.add(new BasicNameValuePair("notice_text", notice_text));
                    param.add(new BasicNameValuePair("notice_day", notice_day));
                    makeToast(notice_day+notice_title+notice_text);

                    param.add(new BasicNameValuePair("code", code)); //코드 : 공지생성

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
                        Log.e("create 공지사항 : ", line);
                    }

                    JSONObject json = new JSONObject(result);
                    JSONArray jArr = json.getJSONArray("dataSend");

                    for (int i = 0; i < jArr.length(); i++) {
                        json = jArr.getJSONObject(i);

                        getJsonData[0] = json.getString("code");

                        if ("fail".equals(getJsonData[0])) {
                            stateCode=false;
                            makeToast("공지사항 등록 실패");
                        }
                    }
                    if(stateCode){
                        makeToast("공지사항 등록 완료");
                    }
                    Log.e("서버에서 온 데이터 : ", getJsonData[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else if (code.equals("alter")) {
                try {
                    HttpClient client = new DefaultHttpClient();
                    //ip주소
                    HttpPost post = new HttpPost(postURL);
                    ArrayList<NameValuePair> param = new ArrayList<NameValuePair>();

                    param.add(new BasicNameValuePair("shop_code", "1"));    //임의의 값

                    param.add(new BasicNameValuePair("notice_title", notice_title));
                    param.add(new BasicNameValuePair("notice_text", notice_text));
                    param.add(new BasicNameValuePair("notice_day", getCurDate()));
                    param.add(new BasicNameValuePair("notice_index", listIndex+""));    //인덱스도 추가

                    param.add(new BasicNameValuePair("code", code)); //코드 : 공지생성

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
                        Log.e("alter 공지사항 : ", line);
                    }

                    JSONObject json = new JSONObject(result);
                    JSONArray jArr = json.getJSONArray("dataSend");

                    for (int i = 0; i < jArr.length(); i++) {
                        json = jArr.getJSONObject(i);

                        getJsonData[0] = json.getString("code");

                        if ("fail".equals(getJsonData[0])) {
                            stateCode=false;
                            makeToast("공지사항 수정 실패");
                        }
                    }
                    if(stateCode){
                        makeToast("공지사항 수정 완료");
                    }
                    Log.e("서버에서 온 데이터 : ", getJsonData[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else if (code.equals("delete")) {   //공지사항 삭제
                try {
                    HttpClient client = new DefaultHttpClient();
                    //ip주소
                    HttpPost post = new HttpPost(postURL);
                    ArrayList<NameValuePair> param = new ArrayList<NameValuePair>();

                    param.add(new BasicNameValuePair("shop_code", "1"));    //임의의 값
                    param.add(new BasicNameValuePair("notice_index", listIndex+""));    //인덱스도 추가

                    param.add(new BasicNameValuePair("code", code)); //코드 : 공지생성

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
                        Log.e("delete 공지사항 : ", line);
                    }

                    JSONObject json = new JSONObject(result);
                    JSONArray jArr = json.getJSONArray("dataSend");

                    for (int i = 0; i < jArr.length(); i++) {
                        json = jArr.getJSONObject(i);

                        getJsonData[0] = json.getString("code");

                        if ("fail".equals(getJsonData[0])) {
                            stateCode=false;
                            makeToast("공지사항 삭제 실패");
                        }
                    }
                    if(stateCode){
                        makeToast("공지사항 삭제 완료");
                    }
                    Log.e("서버에서 온 데이터 : ", getJsonData[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
    public String getCurDate(){
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat CurYearFormat = new SimpleDateFormat("yyyy");
        SimpleDateFormat CurMonthFormat = new SimpleDateFormat("MM");
        SimpleDateFormat CurDayFormat = new SimpleDateFormat("dd");

        return CurYearFormat.format(date).toString()+"."+CurMonthFormat.format(date).toString()+"."+CurDayFormat.format(date).toString();
    }

    final Handler handler = new Handler() {
        public void handleMessage(Message msg)
        {

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
