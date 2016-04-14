package edu.buffalo.cse.cse486586.groupmessenger2;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import edu.buffalo.cse.cse486586.groupmessenger2.Node;
import edu.buffalo.cse.cse486586.groupmessenger2.GroupMessengerActivity;

/**
 * Created by Shaleen Mathur on 2/26/16.
 */

public class OnSendClickListner implements OnClickListener{

    static final String REMOTE_PORT0 = "11108";
    static final String REMOTE_PORT1 = "11112";
    static final String REMOTE_PORT2 = "11116";
    static final String REMOTE_PORT3 = "11120";
    static final String REMOTE_PORT4 = "11124";
    public int counter = 0;


    private static final String TAG = OnSendClickListner.class.getName();
    private final EditText edit_Text ;
    private final TextView text_view  ;
    private final String myPort;

    public OnSendClickListner(EditText editText1 , TextView textView1, String myPort)
    {
        this.edit_Text = editText1;
        this.text_view = textView1;
        this.myPort = myPort;
    }
    @Override
    public void onClick(View v)
    {
        String msg = edit_Text.getText().toString() + "\n";
        Log.d(TAG, "in OnClick with message :: " + msg);
        edit_Text.setText(""); // This is one way to reset the input box.
//        text_view.append(msg);// This is one way to display a string.

        Log.d(TAG, "Calling Client Task ");
        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);
        return;
    }

    public class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {

            Log.d(TAG, "In client :: This port ::   "+ msgs[1]);
            String[] ports = {REMOTE_PORT0, REMOTE_PORT1, REMOTE_PORT2, REMOTE_PORT3, REMOTE_PORT4};
            String msgToSend = msgs[0].trim();
            Log.d(TAG, "writing this message ::  "+ msgs[0]);
            String msgid =  myPort+"$$"+String.valueOf(counter)+"$$"+msgToSend;//Integer.toString(counter);
            Log.d(TAG, "Message ID ::  "+ msgid);
//            Node node = new Node(msgToSend,msgid,myPort,-1,myPort,false, "MESSAGE");
            Node node = new Node(msgToSend,msgid,myPort,-1,myPort,false, "MESSAGE");
            counter = counter+1;

            GroupMessengerActivity.nodeIndex.put(node.msgid, node);
            GroupMessengerActivity.prop_msg_count_map.put(node.msgid, 0);

            try {
                for(String port  : ports)
                {
                    Log.d(TAG, "Sending to Port ::  "+ port);
                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(port));
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    out.writeObject(node);

                    out.flush();
                    out.close();
                    socket.close();
                }
            } catch (UnknownHostException e) {
                Log.d(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.d(TAG, "ClientTask socket IOException:: " + e.toString());
            }
            return null;
        }
    }
}
