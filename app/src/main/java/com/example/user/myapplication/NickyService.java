package com.example.user.myapplication;

/**
 * Created by user on 2016/5/20.
 */
import android.app.ProgressDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

//繼承android.app.Service
public class NickyService extends Service {
    private Handler handler = new Handler();
    String[][] Channel_Info=null;
    String User_APIKEY="";
    int channel_total=-1;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onStart(Intent intent, int startId) {

        if(intent!=null){
            Log.d("intent","not null");
            User_APIKEY=intent.getStringExtra("User_APIKEY").toString();
        }else{
            Log.d("intent","null");
        }

        handler.postDelayed(showTime, 1000);

        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(showTime);
        super.onDestroy();
    }

    private Runnable showTime = new Runnable() {
        public void run() {
            Log.d("User_APIKEY",User_APIKEY);
            String[][] temp=null;
            if(Channel_Info!=null){
                temp=Channel_Info;
            }

            if(User_APIKEY!=null){
                Log.d("User_APIKEY","not null");
            }else{
                Log.d("User_APIKEY","null");
            }

            ThingSpeakWork TSW= new ThingSpeakWork();
            TSW.refresh();
            try {
                TSW.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            if(temp!=null&&Channel_Info!=null){
                for(int i=0;i<=channel_total;i++){
                    Log.d("name",Channel_Info[i][2]);
                    Log.d("percent",Channel_Info[i][4]);
                }
            }

            handler.postDelayed(this, 1000);
        }
    };

    public class ThingSpeakWork extends AsyncTask<Integer, Integer, Integer> {

        HttpURLConnection_WORK HUCW = null;
        String JSONstr = null;
        JSONObject result = null;
        HashMap<String, String> map = null;


        String Channel_ID="";
        String Channel_percent="";

        void percentChannel(String ID) {
            Channel_ID = ID;
            this.execute(4);
        }

        void refresh() {
            this.execute(5);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Integer... params) {

            switch (params[0]) {

                case 4://percent
                    map = null;

                    HUCW = new HttpURLConnection_WORK("https://api.thingspeak.com/channels/" + Channel_ID + "/feeds.json", map);
                    JSONstr = HUCW.sendHttpURLConnectionRequest("GET");

                    int num = 0;
                    float max = 0, last = 0,percent=0;
                    try {
                        result = new JSONObject(JSONstr);
                        num = result.getJSONArray("feeds").length();
                        if(num!=0){
                            for (int i = 0; i < num; i++) {
                                if (max < result.getJSONArray("feeds").getJSONObject(i).getInt("field1")) {
                                    max = result.getJSONArray("feeds").getJSONObject(i).getInt("field1");
                                }
                            }
                            last = result.getJSONArray("feeds").getJSONObject(num - 1).getInt("field1");

                            percent = last / max * 100;
                            Channel_percent=String.format("%.1f", percent) ;
                        }else {
                            Channel_percent = "0.0";
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;

                case 5://refresh

                    map = null;

                    HUCW = new HttpURLConnection_WORK("https://api.thingspeak.com/channels.json?api_key="+User_APIKEY, map);
                    JSONstr = HUCW.sendHttpURLConnectionRequest("GET");


                    try {
                        JSONArray temp = new JSONArray(JSONstr);
                        if(!JSONstr.equals("")){
                            channel_total=temp.length()-1;
                            String[][] temp2 =new String[channel_total+1][5];

                            for (int i = 0;i<=channel_total;i++){
                                for(int j = 0;j<5;j++){
                                    switch (j){
                                        case 0:
                                            temp2[i][0]="";
                                            break;
                                        case 1:
                                            temp2[i][1]= String.valueOf(temp.getJSONObject(i).getInt("id"));
                                            break;
                                        case 2:
                                            temp2[i][2]= temp.getJSONObject(i).getString("name");
                                            break;
                                        case 3:
                                            temp2[i][3]= temp.getJSONObject(i).getJSONArray("api_keys").getJSONObject(0).getString("api_key");
                                            break;
                                        case 4:
                                            temp2[i][4]= "0.0";
                                            break;
                                    }
                                }
                            }
                            Channel_Info=temp2;



                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
            }
            return params[0];
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Integer method) {
            super.onPostExecute(method);
            switch (method) {

                case 5: //refresh

                    if(channel_total>-1){
                        for(int i=0;i<=channel_total;i++){
                            ThingSpeakWork mTSW = new ThingSpeakWork();
                            mTSW.percentChannel(Channel_Info[i][1]);
                            try {
                                mTSW.get();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }
                            Channel_Info[i][4]=Channel_percent;
                        }
                    }

                    break;
            }

            HUCW = null;
            JSONstr = null;
            result = null;
            map = null;//設null防止佔用過多記憶體
        }
    }

    public class HttpURLConnection_WORK {
        private URL url;                    //儲存網路php路徑
        private Map<String, String> map;    //儲存要送的值
        private String encode="utf-8";        //儲存編碼
        //建構子(網路路徑,要送的值)
        public HttpURLConnection_WORK(String path,Map<String, String> map){
            try {
                this.url=new URL(path);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            this.map=map;
        }
        //發送HttpURLConnectionRequest(網址,資料內容,編碼方式)
        public String sendHttpURLConnectionRequest(String method) {
            String temp="";
            try {
                //打開服務器
                HttpURLConnection hucn=(HttpURLConnection) url.openConnection();
                hucn.setReadTimeout(5000);            //設置讀取超時為5秒
                hucn.setConnectTimeout(10000);        //設置連接網路超時為10秒

                //設置輸出入流串
                hucn.setDoInput(true);                //可從伺服器取得資料
                if(method=="GET"){
                    //設置請求的方法
                    hucn.setRequestMethod(method);
                }else {
                    hucn.setDoOutput(true);                //可寫入資料至伺服器
                    //設置請求的方法
                    hucn.setRequestMethod(method);
                    //POST方法不能緩存數據,需手動設置使用緩存的值為false
                    hucn.setUseCaches(false);
                    //寫入參數
                    OutputStream os=hucn.getOutputStream();            //設置輸出流串
                    DataOutputStream dos=new DataOutputStream(os);    //封裝寫給伺服器的資料,需存進這裡
                    if (map != null && !map.isEmpty()) {            //判斷map是否非null或有初始化
                        String str=null;                //用來存傳送參數
                        //entrySet()會得到map內的key-value成對的集合,並回傳
                        for (Map.Entry<String, String> entry : map.entrySet()) {
                            if(str==null)    //判斷是否為第一次調用
                                str=entry.getKey()+"="+ URLEncoder.encode(entry.getValue(),encode);
                            else
                                str=str+"&"+entry.getKey()+"="+URLEncoder.encode(entry.getValue(),encode);
                            //Key值不變,Value轉成UTF-8編碼可使用中文
                            //Map.Entry內提供了getKey()、getValue()、setValue(),雖然增加一行卻省略了很多對Map不必要的get調用
                        }
                        dos.writeBytes(str);    //將設置好的請求參數寫進dos
                        //顯示時必須進行解碼,否則看到的中文會變成亂碼
                        Log.i("text","HttpURLConnection_POST.dos傳送資料="+java.net.URLDecoder.decode(str,encode));
                    }
                    //輸出完關閉輸出流
                    dos.flush();
                    dos.close();
                }

                //判斷是否請求成功,為200時表示成功,其他均有問題
                if(hucn.getResponseCode() == 200){
                    //取得回傳的inputStream (輸入流串)
                    InputStream inputStream = hucn.getInputStream();
                    temp= changeInputStream(inputStream);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("text","HttpURLConnection_POST="+e.toString());
            }

            return temp;
        }
        public String changeInputStream(InputStream inputStream) {    //將輸入串流轉成字串回傳
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            //ByteArrayOutputStream型態可不斷寫入來增長緩存區,可使用toByteArray()、toString()獲取數據
            byte[] data = new byte[1024];
            int len;
            String result = "";
            if (inputStream != null) {        //判斷inputStream是否非空字串
                try {
                    while ((len = inputStream.read(data)) != -1) {    //將inputStream寫入data並回傳字數到len
                        outputStream.write(data, 0, len);            //將data寫入到輸出流中,參數二為起始位置,len是讀取長度
                    }
                    result = new String(outputStream.toByteArray(), encode);    //resilt取得outputStream的string並轉成encode邊碼
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("text", "Http_Client.changeInputStream.IOException="+e.toString());
                }
            }
            return result;                //回傳result
        }
    }
}