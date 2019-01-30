package cn.szxiwang.appone;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import static java.lang.System.in;

/**
 * App接受单片机的数据
 */
public class MobileServer implements Runnable {
    private ServerSocket server;
    private Handler handler = new Handler();

    public MobileServer() {
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void run() {
        try {
            //5000是手机端开启的服务器的端口号，ESP8266进行TCP连接时使用的端口，而IP也是通过指令查询的联入设备的IP
            server = new ServerSocket(5000);
            while (true) {
                Log.e("TAGF", "server accept");
                Socket client = server.accept();
                DataInputStream in = new DataInputStream(client.getInputStream());
                byte[] receive = new byte[1024];
                in.read(receive);
                in.close();
                parseData(receive);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("TAGF", "IOException1");
        }
        if (server != null){
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("TAGF", "IOException2");
            }
        }
    }

    /**
     * 解析数据
     * @param receive 数据
     */
    private void parseData(byte[] receive) {
        String tmp = new String(receive);
//        String tmp = "0001:00:00,42.50,38.50;";//测试数据
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
            sendMessage(tmp);
        }
    }

    /**
     * 发送MainActivity显示数据内容
     * @param str 数据
     */
    public void sendMessage(String str){
        Message message = new Message();
        message.what = 1;
//      message.obj = new String(str);
        message.obj = str;
        handler.sendMessage(message);
    }
}
