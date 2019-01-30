package cn.szxiwang.appone;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

//public class MainActivity extends AppCompatActivity implements View.OnClickListener {
//    private String content,content1,content2,content3;
//    private TextView tvShow;
//    private Button btnOn, btnOff,btnUp,btnDown,btnDebug;
//    private Handler handler = new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what){
//                case 1:
//                    String content = "";
//                    if (msg.obj.equals("on")){
//                        content = "开关已打开";
//                    }
//                    if (msg.obj.equals("off")){
//                        content = "开关已关闭";
//                    }
//                    if (msg.obj.equals("up")){
//                        content = "温度升高1℃";
//                    }
//                    if (msg.obj.equals("down")){
//                        content = "温度降低1℃";
//                    }
//                    tvShow.setText("工作状态：" + content+"\n剩余时间："+content1+"\n设定温度："+content2+"\n当前温度："+content3);
////                    Toast.makeText(MainActivity.this, "接收到信息！", Toast.LENGTH_LONG).show();
//                    break;
//            }
//        }
//    };

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText et;
    private boolean workingStatus = false;
    private String time = "45:00";
    private float presetTemperature = 0, currentTemperature = 20;
    private TextView tvShow;
    private Button btnOn, btnOff, btnUp, btnDown, btnDebug;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    parseData((String)msg.obj);
                    break;
            }
        }
    };
    private Handler repeatHandler = new Handler();
    private Runnable repeatRunnable = new Runnable() {
        @Override
        public void run() {
            sendShip("0");
            repeatHandler.postDelayed(repeatRunnable, 500);//0.5s
            Log.e("TAGF","0.5s执行");
        }
    };
    private MobileServer mobileServer;

    /**
     * 单片机发送给APP指令内容格式：
     * 000xx,x1x1,x2x2;
     * 001;
     * 002;
     * 003;
     * 004;
     * 005xx;
     * 006x1x1;
     * 007x2x2;
     * @param str
     */
    private void parseData(String str) {//0001:00:00,42.50,38.50
        String obj = str.substring(0, 3);

        if (obj.equals("000")){//xx,x1x1,x2x2
            String tmp = str.substring(3);
            List<Integer> indexList = new ArrayList<>();
            for (int i=0;i<tmp.length();i++){
                if (",".equals(tmp.charAt(i)+"")){
                   indexList.add(i);
                }
            }
            if (indexList.size() == 2){
                time = tmp.substring(0, indexList.get(0));
                presetTemperature = Float.parseFloat(tmp.substring(indexList.get(0)+1, indexList.get(1)));
                currentTemperature = Float.parseFloat(tmp.substring(indexList.get(1)+1));
            }
        }
        else if (obj.equals("001")) {
            if (repeatHandler != null){
                repeatHandler.removeCallbacks(repeatRunnable);
                repeatHandler.post(repeatRunnable);//开启每0.5s发送一次
            }
            workingStatus = true;
            Toast.makeText(MainActivity.this,"开关已打开!", Toast.LENGTH_SHORT).show();
        }
        else if (obj.equals("002")) {
            if (repeatHandler != null){
                repeatHandler.removeCallbacks(repeatRunnable);//关闭每0.5s发送一次
            }
            workingStatus = false;
            Toast.makeText(MainActivity.this,"开关已关闭!", Toast.LENGTH_SHORT).show();
        }
        else if (obj.equals("003")) {
            Toast.makeText(MainActivity.this,"温度升高1℃", Toast.LENGTH_SHORT).show();
            presetTemperature++;
        }
        else if (obj.equals("004")) {
            Toast.makeText(MainActivity.this,"温度降低1℃", Toast.LENGTH_SHORT).show();
            presetTemperature--;
        }
        else if (obj.equals("005")) {
            time = str.substring(3);
        }
        else if (obj.equals("006")) {
            presetTemperature = Float.parseFloat(str.substring(3));
        }
        else if (obj.equals("007")) {
            currentTemperature = Float.parseFloat(str.substring(3));
        }
        tvShow.setText("工作状态：" + (workingStatus ? "开启" : "关闭") + "\n剩余时间：" + time + "\n设定温度：" + presetTemperature + "℃" + "\n当前温度：" + currentTemperature + "℃");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvShow = findViewById(R.id.tvShow);
        btnOn = findViewById(R.id.btnOn);
        btnOff = findViewById(R.id.btnOff);
        btnUp = findViewById(R.id.btnUp);
        btnDown = findViewById(R.id.btnDown);
        btnDebug = findViewById(R.id.btnDebug);
        et = findViewById(R.id.et);

        btnOn.setOnClickListener(this);
        btnOff.setOnClickListener(this);
        btnUp.setOnClickListener(this);
        btnDown.setOnClickListener(this);
        btnDebug.setOnClickListener(this);

//        btnOn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                tvShow.setText("开启继电器");
//                Toast.makeText(MainActivity.this,"开启继电器", Toast.LENGTH_SHORT).show();
//            }
//        });
//        btnOff.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                tvShow.setText("关闭继电器");
//                Toast.makeText(MainActivity.this,"关闭继电器", Toast.LENGTH_SHORT).show();
//            }
//        });

        //开启服务器
        mobileServer = new MobileServer();
        mobileServer.setHandler(handler);
        new Thread(mobileServer).start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnOn:
                sendShip("1");
//                Toast.makeText(MainActivity.this,"发送数据内容：ON", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnOff:
                sendShip("2");
//                Toast.makeText(MainActivity.this,"发送数据内容：OFF", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnUp:
                sendShip("3");
//                Toast.makeText(MainActivity.this,"发送数据内容：UP", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnDown:
                sendShip("4");
//                Toast.makeText(MainActivity.this,"发送数据内容：DOWN", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnDebug:
                closeKeyboard();
                if (!et.getText().toString().isEmpty()){
                    String tmp = et.getText().toString();
                    int lastIndex = -1;
                    for (int i=0;i<tmp.length();i++){
                        if ((tmp.charAt(i)+"").equals(";")){//寻找分号下标
                            lastIndex = i;
                            break;
                        }
                    }
                    if (lastIndex != -1){//数据存在分号且内容无误
                        tmp = tmp.substring(0, lastIndex);
                        Log.e("TAGF", "单片机发送内容=" + tmp);
                        if (mobileServer != null)
                            mobileServer.sendMessage(tmp);
                    }
                }
                break;
        }
    }

    /**
     * APP发送给单片机指令内容格式：
     * 0 -> 0.5秒
     * 1 -> ON
     * 2 -> OFF
     * 3 -> UP
     * 4 -> DOWN
     * @param str
     */
    private void sendShip(String str) {
        new SendAsyncTask().execute(str);
    }

    /**
     * 关闭键盘
     */
    private void closeKeyboard(){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && imm.isActive())
            imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
    }

}
