package cn.szxiwang.appone;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

/**
 * App向单片机发送数据
 */
public class SendAsyncTask extends AsyncTask<String, Void, Void> {
      
    //这里是连接ESP8266的IP和端口号，IP是通过指令在单片机开发板查询到，而端口号可以自行设置，也可以使用默认的，333就是默认的  
    private static final String IP = "192.168.4.1";  
    private static final int PORT = 333;  

    private Socket client = null;
    private PrintStream out = null;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(String... params) {  
        String str = params[0];  
        try {  
            client = new Socket(IP, PORT);  
            client.setSoTimeout(5000);  
            // 获取Socket的输出流，用来发送数据到服务端  
            out = new PrintStream(client.getOutputStream());  
            out.print(str);  
            out.flush();  
  
            if (client == null) {  
                return null;  
            } else {  
                out.close();  
                client.close();  
            }  
  
        } catch (IOException e) {
            e.printStackTrace();  
        }
        return null;  
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }

}