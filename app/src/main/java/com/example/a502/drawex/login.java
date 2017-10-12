package com.example.a502.drawex;


import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
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
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by 502 on 2017-09-26.
 */

public class login extends Fragment {
    ViewGroup rootView;

    EditText idTxt;
    EditText pwTxt;
    TextView registerTxt;
    TextView findIdTxt;
    TextView findpwTxt;
    Button loginBtn;
    NavigationView navigationView;
    CheckBox memberCheck,directorCheck;

    String id,pw;
    boolean memberLogin=false,directorLogin=false;
    loadJsp task;

    private String[] getJsonData = { "" };
    AppCompatActivity activity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = (ViewGroup)inflater.inflate(R.layout.login, container, false);

        idTxt=(EditText)rootView.findViewById(R.id.id);
        pwTxt=(EditText)rootView.findViewById(R.id.passward);
        registerTxt=(TextView)rootView.findViewById(R.id.register);
        findIdTxt=(TextView)rootView.findViewById(R.id.find_id);
        findpwTxt=(TextView)rootView.findViewById(R.id.find_pw);
        loginBtn = (Button) rootView.findViewById(R.id.loginBtn);
        memberCheck=(CheckBox)rootView.findViewById(R.id.memberCheck);  //체크박스
        directorCheck=(CheckBox)rootView.findViewById(R.id.directorCheck);

        activity = (AppCompatActivity) getActivity();


        //비밀번호 입력시 *로 뜨게
        pwTxt.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        pwTxt.setTransformationMethod(PasswordTransformationMethod.getInstance());

        //비밀번호 누르고 로그인버트안눌러두 되게게
        pwTxt.setImeOptions(EditorInfo.IME_ACTION_DONE);
        pwTxt.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if(actionId == EditorInfo.IME_ACTION_DONE)
                {
                    return true;
                }
                return false;
            }
        });

        navigationView=(NavigationView)activity.findViewById(R.id.navigation_view);



        loginBtn.setOnClickListener(new View.OnClickListener() {    //로그인 버튼
            public void onClick(View v) {
                if(memberCheck.isChecked()) {   //회원 로그인
                    id = idTxt.getText().toString();  //아이디 비밀번호 받기
                    pw = pwTxt.getText().toString();
                    task = new loadJsp("login","m");    //login DB연결
                    task.execute();
                }
                else if(directorCheck.isChecked()){ //관리자 로그임
                    id = idTxt.getText().toString();  //아이디 비밀번호 받기
                    pw = pwTxt.getText().toString();
                    task = new loadJsp("login","d");    //login DB연결
                    task.execute();
                }

                    if (memberLogin)   //일반 손님 로그인 성공시
                    {
                        Log.e("log1", "normal login");
                        navigationView.getMenu().setGroupVisible(R.id.noLogin, false);
                        navigationView.getMenu().setGroupVisible(R.id.after_login_store, false);
                        navigationView.getMenu().setGroupVisible(R.id.after_login_normal, true);
                    }
                    if (idTxt.getText().toString().equals("2") && pwTxt.getText().toString().equals("2"))   //r관리자 로그인 성공시
                    {
                        Log.e("log1", "store login");
                        navigationView.getMenu().setGroupVisible(R.id.noLogin, false);
                        navigationView.getMenu().setGroupVisible(R.id.after_login_normal, false);
                        navigationView.getMenu().setGroupVisible(R.id.after_login_store, true);
                    }
            }
        });


        findIdTxt.setOnClickListener(new View.OnClickListener() {   //아이디찾기
            public void onClick(View v) {

            }
        });

        findpwTxt.setOnClickListener(new View.OnClickListener() {   //비밀번호찾기
            public void onClick(View v) {

            }
        });

        registerTxt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {   //회원가입 -> register.class에서 디비 처리
                register r=new register();
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.frame, r).commit();

            }
        });

        return rootView;

    }

    class loadJsp extends AsyncTask<Void,String,Void> {
        String code,check;

        loadJsp(String code,String check) {
            this.code = code;
            this.check=check;
            Log.e("생성자", code + "---"+check+"");
        }

        protected Void doInBackground(Void... params) {
            if(check.equals("m")) { //member login
                Log.e("login", "로그인 버튼" + code + "");
                String postURL = "http://192.168.35.212:8090/test/NewFile.jsp";    //10.02 ip update OK
                try {
                    HttpClient client = new DefaultHttpClient();

                    HttpPost post = new HttpPost(postURL);
                    ArrayList<NameValuePair> param = new ArrayList<NameValuePair>();

                    param.add(new BasicNameValuePair("id", id));
                    param.add(new BasicNameValuePair("pw", pw));
                    param.add(new BasicNameValuePair("code", code)); //코드 : 로그인

                    UrlEncodedFormEntity ent = new UrlEncodedFormEntity(param, HTTP.UTF_8);
                    post.setEntity(ent);

                    HttpResponse responsePost = client.execute(post);
                    HttpEntity resEntity = responsePost.getEntity();

                    BufferedReader bufReader=new BufferedReader(new InputStreamReader(responsePost.getEntity().getContent(),"utf-8"));
                    String line=null;
                    String result="";

                    //줄단위로 읽어오기
                    while((line=bufReader.readLine())!=null){
                        result+=line;
                        Log.e("서버에서 온 데이터 : ",line);
                    }

                    JSONObject json=new JSONObject(result);
                    JSONArray jArr=json.getJSONArray("dataSend");

                    for(int i=0;i<jArr.length();i++){
                        json=jArr.getJSONObject(i);

                        getJsonData[0]=json.getString("code");

                        if("fail".equals(getJsonData[0])){
                            makeToast("아이디/비밀번호 오류");
                            memberLogin=false;
                        }
                        else{
                            memberLogin=true;   //화면 전환을 위해
                           makeToast("로그인성공");
                        }
                    }
                    if (resEntity != null) {
                        Log.e("RESPONSE", EntityUtils.toString(resEntity));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else{   //director login
                try {
                    String postURL = "http://192.168.35.212:8090/test/directorFile.jsp";    //10.09 ip update OK
                    HttpClient client = new DefaultHttpClient();

                    HttpPost post = new HttpPost(postURL);
                    ArrayList<NameValuePair> param = new ArrayList<NameValuePair>();

                    param.add(new BasicNameValuePair("id", id));
                    param.add(new BasicNameValuePair("pw", pw));
                    param.add(new BasicNameValuePair("code", code)); //코드 : 로그인

                    UrlEncodedFormEntity ent = new UrlEncodedFormEntity(param, HTTP.UTF_8);
                    post.setEntity(ent);

                    HttpResponse responsePost = client.execute(post);
                    HttpEntity resEntity = responsePost.getEntity();

                    BufferedReader bufReader=new BufferedReader(new InputStreamReader(responsePost.getEntity().getContent(),"utf-8"));
                    String line=null;
                    String result="";

                    //줄단위로 읽어오기
                    while((line=bufReader.readLine())!=null){
                        result+=line;
                        Log.e("서버에서 온 데이터 : ",line);
                    }

                    JSONObject json=new JSONObject(result);
                    JSONArray jArr=json.getJSONArray("dataSend");

                    for(int i=0;i<jArr.length();i++){
                        json=jArr.getJSONObject(i);

                        getJsonData[0]=json.getString("code");

                        if("fail".equals(getJsonData[0])){
                            makeToast("아이디/비밀번호 오류");
                            directorLogin=false;
                        }
                        else{
                            makeToast("로그인성공");
                            directorLogin=true;
                        }
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
            idTxt.setText(getJsonData[0].toString());
            idTxt.setTextColor(Color.RED);
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

