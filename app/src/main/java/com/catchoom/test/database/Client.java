package com.catchoom.test.database;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.catchoom.test.RecognitionActivity;
import com.catchoom.test.SplashScreenActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import static android.support.v7.appcompat.R.id.text;
import static com.catchoom.test.R.layout.camera_overlay;

public class Client extends AsyncTask<String, String, String> {

    String dstAddress;
    String response = "";
    int dstPort;

    Client(String addr, int port) {
        dstAddress = addr;
        dstPort = port;
    }

    @Override
    protected String doInBackground(String...  query) {

        Socket socket = new Socket();

        try {
            socket.connect(new InetSocketAddress(dstAddress, dstPort),3000);
            if(socket!=null) {
                byte[] vals = new byte[1024];
                PrintWriter outToServer = new PrintWriter(
                        new OutputStreamWriter(
                                socket.getOutputStream()));
                outToServer.print(query[0]);
                outToServer.flush();

                InputStream inputStream = socket.getInputStream();
                int nums = 0;
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while ((nums = in.read()) != 32) {
                    response = response + Character.toString((char) nums);
                }
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
            response = "UnknownHostException: " + e.toString();
        } catch (IOException e) {
            e.printStackTrace();
            response = "IOException: " + e.toString();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return response;

    }

    @Override
    protected void onPostExecute(String result) {
        this.cancel(true);
    }

}