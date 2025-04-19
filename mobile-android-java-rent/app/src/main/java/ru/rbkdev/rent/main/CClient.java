package ru.rbkdev.rent.main;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.IOException;
import java.io.DataOutputStream;
import java.io.ByteArrayOutputStream;

import java.net.Socket;
import java.net.InetSocketAddress;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class CClient extends Thread {

    private static Socket m_socket;

    private static final String SERVER_IP = "192.168.43.23";
    // private static final String SERVER_IP = "192.168.1.66";
    private static final int SERVER_PORT = 4000;
    // private static final int SEC_TIMEOUT = 50; // ~sec
    private static final int SEC_TIMEOUT = 5000; // ~sec

    private static ByteArrayOutputStream m_bufferArrayStream;
    private static CRingBuffer m_buffer;

    // private static int m_timeout = SEC_TIMEOUT;

    private static int m_count = 0;
    private static int m_number = 0;
    private static int m_size = 0;
    private static byte[] m_array = new byte[4096];
    private static boolean m_full = false;

    private static class ClientThreadInit implements Runnable {

        @Override
        public void run() {
            try {
                m_bufferArrayStream = new ByteArrayOutputStream();
                m_buffer = new CRingBuffer();

                m_socket = new Socket();
                m_socket.setSoTimeout(SEC_TIMEOUT);
                m_socket.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT), SEC_TIMEOUT);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private static class ClientThreadRead implements Runnable {

        @Override
        public void run() {

            if(m_socket != null) {

                try {

                    int length;
                    byte[] buffer = new byte[1100];

                    // m_socket.setSoTimeout(m_timeout);
                    InputStream stream = m_socket.getInputStream();

                    while((length = stream.read(buffer)) != -1) {

                        m_buffer.Write(buffer, length);

                        m_full = FillingMessage();
                        if(m_full)
                            return;
                    }

                } catch(IOException e) {
                    e.printStackTrace();
                    Log.v("TAG", "IOException");
                }

                /*try {
                    m_timeout = SEC_TIMEOUT;
                    m_socket.setSoTimeout(SEC_TIMEOUT);
                } catch(IOException e) {
                    e.printStackTrace();
                    Log.v("PD_TAG", "setSoTimeout");
                }*/
            }
        }
    }

    private static class ClientThreadSend implements Runnable {

        @Override
        public void run() {

            byte[] message = m_bufferArrayStream.toByteArray();
            int size = message.length;

            if(m_socket != null && size != 0) {
                try {
                    DataOutputStream dos = new DataOutputStream(m_socket.getOutputStream());
                    dos.writeInt(size);
                    dos.write(message,0, size);
                    dos.flush();
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static int StartThread(Thread thread) {
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return 1;
        }
        return 0;
    }

    public CClient() {
        StartThread(new Thread(new ClientThreadInit()));
    }

    public void Close() {
        try {
            m_socket.close();
        } catch (IOException exc) {
            exc.printStackTrace();
        }
    }

    public static void Delay(int millisecond) {
        try {
            Thread.sleep(millisecond);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*public static void SetTimeout(int millisecond) {
        m_timeout = millisecond;
    }*/

    @Nullable
    public static ArrayList<ArrayList<ByteArrayOutputStream>> DataExchange(ArrayList<byte[]> data, byte command, Context context) {
        m_full = false;
        m_count = m_number = m_size = 0;

        ByteArrayOutputStream message = CreateMessage(data, command);
        int size = message.size();

        int pos = 0;
        int sizeSend;

        int number = size / CLISTID.SIZE_TRAFFIC;
        if(size % CLISTID.SIZE_TRAFFIC != 0)
            number++;

        ByteArrayOutputStream part = MakePackage(ByteBuffer.allocate(4).putInt(number).array(), 4);
        m_bufferArrayStream.reset();
        if(part != null) {
            if(Send(part.toByteArray()) != 0)
                return null;
            part.reset();
        } else
            return null;

        do {
            sizeSend = Math.min(size - pos, CLISTID.SIZE_TRAFFIC);

            if(part != null) {
                part.write(message.toByteArray(), pos, sizeSend);
                part = MakePackage(part.toByteArray(), part.size());
            }

            m_bufferArrayStream.reset();
            if(part != null) {
                if (Send(part.toByteArray()) != 0)
                    return null;
                part.reset();
            }

            Delay(0);

        } while(size != (pos += sizeSend));

        Delay(0);

        m_bufferArrayStream.reset();
        byte[] array = Read();
        size = array.length;
        if(size != 0 && m_full)
            return CheckMessage(ParseMessage(array, size, command), context);
        else
            return  null;
    }

    @NotNull
    public static ByteArrayOutputStream CreateMessage(@NotNull ArrayList<byte[]> message, byte command) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        try {

            buffer.write(new byte[]{ (byte) 0xFA, (byte) 0xFB, command });

            for(byte[] p : message) {
                buffer.write(p);
                buffer.write(new byte[]{(byte) 0xFA, (byte) 0xFB, (byte) 0x60});
            }

            buffer.write(new byte[]{(byte) 0xFA, (byte) 0xFB, (byte) 0xFF});

        } catch (IOException e) {
            e.printStackTrace();
        }

        return buffer;
    }

    private static int Send(byte[] buffer) {

        try {
            m_bufferArrayStream.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            return 1;
        }

        return StartThread(new Thread(new ClientThreadSend()));
    }

    @NotNull
    private static byte[] Read() {
        StartThread(new Thread(new ClientThreadRead()));
        return m_bufferArrayStream.toByteArray();
    }

    private static ByteArrayOutputStream MakePackage(byte[] in, int size) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            out.write("_FAB_".getBytes());
            out.write(ConvertOfSize(size));
            out.write(in);
            out.write("_AFE_".getBytes());
            return out;
        } catch(IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static ArrayList<ArrayList<ByteArrayOutputStream>> ParseMessage(byte[] mesasge, int sizeMesasge, byte command) {

        ArrayList<ArrayList<ByteArrayOutputStream>> parse_message = new ArrayList<>();
        ByteArrayOutputStream temp = new ByteArrayOutputStream();

        //  Защита от мусора
        int startPosition = -1;
        for(int i = 2; i < sizeMesasge; i++) {
            if(mesasge[i - 2] == (byte) 0xFA && mesasge[i - 1] == (byte) 0xFB && mesasge[i] == command) {
                startPosition = i + 1;
                break;
            }
        }
        if(startPosition == -1)
            return parse_message;

        for(int position = startPosition; position < sizeMesasge; position++) {

            if(mesasge[position] == (byte) 0xFA && mesasge[position + 1] == (byte) 0xFB) {

                switch(mesasge[position + 2]) {

                    case (byte) 0x22 : {
                        position += 2;
                        if(temp.toByteArray()[0] == '0') {
                            temp.reset();
                            break;
                        } else
                            return parse_message;
                    }

                    case (byte) 0x45 : {
                        position += 2;

                        int size = (temp.toByteArray()[0] << 8) | temp.toByteArray()[1];
                        if(size <= 64000) {
                            temp.reset();
                            break;
                        } else
                            return parse_message;
                    }

                    case (byte) 0x78: {
                        position += 2;
                        parse_message.add(new ArrayList<ByteArrayOutputStream>());
                        temp.reset();
                        break;
                    }

                    case (byte) 0x60: {
                        position += 2;
                        parse_message.get(parse_message.size() - 1).add(temp);
                        temp = new ByteArrayOutputStream();
                        break;
                    }

                    case (byte) 0xFF: {
                        return parse_message;
                    }

                    default:
                }
            } else
                temp.write(mesasge[position]);
        }
        return parse_message;
    }

    private static ArrayList<ArrayList<ByteArrayOutputStream>> CheckMessage(ArrayList<ArrayList<ByteArrayOutputStream>> bundle, Context context) {

        try {

            if(bundle == null)
                throw new CException("error: NULL ANSWER");
            if(bundle.isEmpty())
                throw new CException("error: BUNDLE IS EMPTY");

            ArrayList<ByteArrayOutputStream> headerBundle = bundle.get(0);

            if(headerBundle.size() != CLISTID.COMMAND_HEADER_SIZE)
                throw new CException("error: COMMAND HEADER SIZE");

            {
                byte[] number_bundle = headerBundle.get(0).toByteArray();
                if(number_bundle.length != 1)
                    throw new CException("error: LENGTH NUMBER BUNDLE");

                if(number_bundle[0] != bundle.size())
                    throw new CException("error: NUMBER BUNDLES");
            }

            {
                byte[] result_server = headerBundle.get(1).toByteArray();
                if(result_server.length != 1)
                    throw new CException("error: LENGTH RESULT SERVER");

                if(result_server[0] != CLISTID.SERVER_SUCCESS)
                    throw new CException("error: OPERATION ON SERVER: " + CMainActivity.g_commandError.get((int)result_server[0]));
            }

            {
                byte[] result_command = headerBundle.get(2).toByteArray();
                if (result_command.length != 1)
                    throw new CException("error: LENGTH RESULT COMMAND");

                if(result_command[0] != CLISTID.COMMAND_SUCCESS)
                    throw new CException("error: OPERATION COMMAND: " + CMainActivity.g_commandError.get((int)result_command[0]));
            }

            for (int i = 1; i < bundle.size(); i++) {
                ArrayList<ByteArrayOutputStream> item = bundle.get(i);
                if (!item.isEmpty()) {
                    byte[] arrayItem = item.get(0).toByteArray();
                    if (arrayItem.length != 1)
                        throw new CException("error: LENGTH ARRAY NUMBER ITEM: " + i);

                    if (arrayItem[0] != item.size())
                        throw new CException("error: NUMBER ITEMS: " + i);
                }
            }

        } catch(CException exception) {
            Toast.makeText(context, exception.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }

        return bundle;
    }

    static int ConvertToSize(byte[] in) {
        int sizeOut = 0;
        sizeOut |= (in[0] & 0x00ff) << 24;
        sizeOut |= (in[1] & 0x00ff) << 16;
        sizeOut |= (in[2] & 0x00ff) << 8;
        sizeOut |= (in[3] & 0x00ff);
        return  sizeOut;
    }

    static byte[] ConvertOfSize(int size) {
        byte[] out = new byte[4];
        out[0] = (byte)(size >> 24);
        out[1] = (byte)(size >> 16);
        out[2] = (byte)(size >> 8);
        out[3] = (byte)size;
        return out;
    }


    static int ParsePackage(byte[] in, int pos, int size) {

        for(int i = pos; i + CLISTID.MARK_SIZE < size; i++) {

            if(in[i] == '_' && in[i + 1] == 'F' && in[i + 2] == 'A' && in[i + 3] == 'B' && in[i + 4] == '_') {
                m_size = ConvertToSize(Arrays.copyOfRange(in, i + 5, i + 9));

                if(m_array.length < i + 9 + m_size && size < i + 9 + m_size)
                    return -1;

                m_array = Arrays.copyOfRange(in, i + 9, i + 9 + m_size);

                /*int begin = i + 9;
                int end = i + 9 + m_size;

                for(int k = 0, l = begin; l < end; k++, l++)
                    m_array[k] = in[l];*/

                int j = i + 9 + m_size;
                if(j + CLISTID.MARK_SIZE <= size) {
                    if(in[j] == '_' && in[j + 1] == 'A' && in[j + 2] == 'F' && in[j + 3] == 'E' && in[j + 4] == '_')
                        return j + CLISTID.MARK_SIZE;
                } else
                     return -1;
            }
        }

        return -1;
    }

    static boolean FillingMessage() throws IOException {

        byte[] data = m_buffer.Read();

        int pos_now;
        int pos_prev = 0;

        int size = data.length;

        while(true) {

            pos_now = ParsePackage(data, pos_prev, size);
            if(pos_now != -1) {

                m_buffer.Flush(pos_now - pos_prev);
                // m_buffer.Flush(pos_now);

                pos_prev = pos_now;

                if(m_count != 0) {
                    m_bufferArrayStream.write(m_array);
                } else
                    m_number = ConvertToSize(m_array);

                if(m_count++ == m_number)
                    return true;
            } else
                break;
        }

        return false;
    }
}
