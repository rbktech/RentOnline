package com.fonekey.mainpage;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class CClient {
    private Socket m_socket;

    class ClientThread implements Runnable {

        @Override
        public void run() {
            try {
                InetAddress serverAddr = InetAddress.getByName("45.134.60.232");
                m_socket = new Socket(serverAddr,3500);

            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    /*private void OpenClient() {
        try {
            InetAddress serverAddr = InetAddress.getByName("45.134.60.232");
            m_socket = new Socket(serverAddr,3500);

        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }*/

    public CClient() {
        //OpenClient();
        new Thread(new ClientThread()).start();
    }

    public void SendData(String message) {
        try {
            //DataOutputStream outputStream = new DataOutputStream(m_socket.getOutputStream());
            //outputStream.writeBytes(message);
            //outputStream.flush();
            // m_socket.close();
            //outputStream.writeUTF("12345");
            //outputStream.write("message2".getBytes());
            //outputStream.writeByte(12);

            //m_socket.getOutputStream().write(message.getBytes());
            //m_socket.getOutputStream().flush();

            //PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(m_socket.getOutputStream())), true);
            //out.println(message);
            //out.flush();

            PrintWriter out = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(m_socket.getOutputStream())),
                    true);
            out.println(message);
            out.flush();
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        try {
            m_socket.shutdownInput();
            m_socket.shutdownOutput();
            m_socket.close();
        } catch (IOException e) {
            // Log.e(LOG_TAG, "Ошибка закрытия сокета :" + e.getMessage());
        } finally {
            m_socket = null;
        }
    }
}
