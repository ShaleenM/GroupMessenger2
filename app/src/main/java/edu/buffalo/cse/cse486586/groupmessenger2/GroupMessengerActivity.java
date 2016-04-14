// My version2

package edu.buffalo.cse.cse486586.groupmessenger2;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.widget.TextView;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 *
 * @author stevko
 *
 */
class Node implements Serializable{
    String msg;
    String msgid;
    String senderPort;
    String myport;
    String senatorPort;
    int seq;
    boolean ready;
    String msgType;
    int seq_max = -1;
    public Node(String msg, String msgid, String senderPort,int seq, String myport, boolean ready, String msgType){
        this.msg = msg;
        this.msgid = msgid;
        this.senderPort= senderPort;
        this.seq = seq;
        this.myport = myport;
        this.ready = ready;
        this.msgType = msgType;
        this.senatorPort = myport;
    }
    public Node(){}
}
public class GroupMessengerActivity extends Activity {

    static final String TAG = GroupMessengerActivity.class.getSimpleName();
    static final int SERVER_PORT = 10000;

    static public HashMap<String , Integer> prop_msg_count_map = new HashMap<String, Integer>();
    static public HashMap<String , Node> nodeIndex = new HashMap<String, Node>();
    static public TreeMap<Integer , String> map = new TreeMap<Integer, String>();
    public int seq = -1;

    public int keyvalue =0;
    public int c =0;
    public int d =0;

    static final String REMOTE_PORT0 = "11108";
    static final String REMOTE_PORT1 = "11112";
    static final String REMOTE_PORT2 = "11116";
    static final String REMOTE_PORT3 = "11120";
    static final String REMOTE_PORT4 = "11124";

    TextView tv;

    String myPort = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        Log.d(TAG, "In OnCreate...");
        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        myPort = String.valueOf((Integer.parseInt(portStr) * 2));

        try {

            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            Log.d(TAG, "Server Socket Created :: " + SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            Log.d(TAG, "Can't create a ServerSocket" + e.toString());
        }

        tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());

        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));

        final Button send = (Button) findViewById(R.id.button4);
        final TextView text_view = (TextView) findViewById(R.id.textView1);
        final EditText edit_text = (EditText) findViewById(R.id.editText1);
        Log.v(TAG, "Before onClick Listner  ");

        send.setOnClickListener(new OnSendClickListner(edit_text, text_view, myPort));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }


    private Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }

    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        final TextView text_view = (TextView) findViewById(R.id.textView1);
        String[] ports = {REMOTE_PORT0, REMOTE_PORT1, REMOTE_PORT2, REMOTE_PORT3, REMOTE_PORT4};
//        public int seq= (Integer.parseInt(myPort)*100)-1;

        PriorityQueue<Node> q = new PriorityQueue<Node>(1000, new Comparator<Node>() {
            @Override
            public int compare(Node lhs, Node rhs){
                if (lhs.seq > rhs.seq){
                    return 1;
                }
                else if (lhs.seq < rhs.seq){
                    return -1;
                }
                else {
                    if(Long.valueOf(lhs.senatorPort) < Long.valueOf(rhs.senatorPort)){
                        return 1;
                    }
                    else if(Long.valueOf(lhs.senatorPort) > Long.valueOf(rhs.senatorPort)){
                        return -1;
                    }
                    else{
                        return 0;
                    }
                }
            }
        });

        @Override
        protected Void doInBackground(ServerSocket... sockets) {

            Log.d(TAG, "Socket  :: " + sockets[0]);
            ServerSocket serverSocket = sockets[0];

            ObjectInputStream in;
            ObjectOutputStream out;

            try {
                while(true) {
                    Log.d(TAG, "I am before socket.accept of ::  "+ myPort);
                    Socket clientSocket = serverSocket.accept();
                    Log.d(TAG, "Server accepted socket :: "+ myPort);
//                    serverSocket.setSoTimeout(500);

                    // Reading from the Socket
                    in = new ObjectInputStream(clientSocket.getInputStream());
                    Node node = (Node) in.readObject();
                    Log.d(TAG, "Message Type :: " + node.msgType);

                    if(node.msgType.equals("MESSAGE"))
                    {
                        //Store messages in a hold back queue
                        q.add(node);
                        Log.d(TAG, "Tag2..." + node.msgid + " :::: " + prop_msg_count_map.get(node.msgid));
                        c++;

                        //Send <mesage ID , Sequence> to sender
                        try
                        {
                            seq = seq+1;
                            node.seq = seq;
                            node.myport = myPort;
                            node.msgType = "PROPOSED";

                            //Create Socket
                            Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(node.senderPort));

                            out = new ObjectOutputStream(socket.getOutputStream());
                            out.writeObject(node);
                            Log.d(TAG, myPort + "::  Proposed to :: " + node.senderPort + "Sequence number  ::" + node.seq);
                            out.flush();

                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else if(node.msgType.equals("PROPOSED"))
                    {
                        Log.d(TAG, "Tag1..." + node.msgid + " :::: " + prop_msg_count_map.get(node.msgid));
                        if(prop_msg_count_map.get(node.msgid) < 5)
                        {

                            prop_msg_count_map.put(node.msgid,prop_msg_count_map.get(node.msgid)+1);//Increment number of proposals recieved
                            node.senatorPort=myPort;
                            if(node.seq_max < node.seq) {
                                node.seq_max = node.seq;
                                node.senatorPort = node.myport;
                            }else if(node.seq_max == node.seq){
                                node.senatorPort = (Integer.parseInt(node.senatorPort)>Integer.parseInt(node.myport))?node.myport:node.senatorPort;
                            }else{
                                node.senatorPort = node.myport;
                            }

                            if(prop_msg_count_map.get(node.msgid) == 5)
                            {
                                Node node2 = node;
                                node2.seq = node2.seq_max;
                                node2.ready = true;
                                node2.msgType = "AGREED";
                                Log.d(TAG, "Agreed Seq for "+node2.msg+"  ::::  "+node.seq+"---"+node.seq_max);
                                try
                                {
                                    for(String port  : ports)
                                    {
                                        Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(port));
                                        out = new ObjectOutputStream(socket.getOutputStream());
                                        out.writeObject(node2);
                                        out.flush();
                                    }
                                }catch (IOException e) {
                                    Log.d(TAG, "IOException:: " + e.toString());
                                }
                            }
                        }
                    }
                    else if(node.msgType.equals("AGREED"))
                    {
                        node.seq = node.seq_max;
                        q.remove(nodeIndex.get(node.msgid));
                        Iterator<Node> nodeIterator = q.iterator();
                        while(nodeIterator.hasNext()){
                            Node messagetemp = nodeIterator.next();
                            if(messagetemp.msgid.equals(node.msgid)){
                                q.remove(messagetemp);
                                q.add(node);
                                Log.d(TAG,"modified queue with message ::"+node.msg+"  Final_Seq :: "+node.seq+"  Status :: "+ node.ready);
                            }
                        }
//                        q.add(node);
                        Log.d(TAG, "Tag101! form doInBackground :::" + node.msg+node.ready);
                        if(d >= c-1){
                            publishProgress(node.msgid);
                            d++;
                        }
                        else{
                            d++;
                        }
//                        publishProgress(node.msgid);
                    }
                }
            } catch (IOException e) {
                Log.d(TAG, e.toString());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }
        protected void onProgressUpdate(String... strings) {

            Log.d(TAG, "in onProgressUpdate :: msg passed :: " + strings[0]);

            Iterator<Node> nodeIterator = q.iterator();
            while(nodeIterator.hasNext()){
                Node messagetemp = nodeIterator.next();
//                if(messagetemp.msgid.equals(node.msgid)){
                q.remove(messagetemp);
                q.add(messagetemp);
//                    Log.d(TAG,"modified queue with message ::"+node.msg+"  Final_Seq :: "+node.seq+"  Status :: "+ node.ready);
            }

            Iterator<Node> nodeIterator1 = q.iterator();
            while(nodeIterator1.hasNext()){
                Node messagetemp = nodeIterator1.next();
//                if(messagetemp.msgid.equals(node.msgid)){
                q.remove(messagetemp);
                q.add(messagetemp);
//                    Log.d(TAG,"modified queue with message ::"+node.msg+"  Final_Seq :: "+node.seq+"  Status :: "+ node.ready);
            }

            Iterator<Node> nodeIterator2 = q.iterator();
            while(nodeIterator2.hasNext()){
                Node messagetemp = nodeIterator2.next();
//                if(messagetemp.msgid.equals(node.msgid)){
                q.remove(messagetemp);
                q.add(messagetemp);
//                    Log.d(TAG,"modified queue with message ::"+node.msg+"  Final_Seq :: "+node.seq+"  Status :: "+ node.ready);
            }

            Iterator<Node> nodeIterator3 = q.iterator();
            while(nodeIterator3.hasNext()){
                Node messagetemp = nodeIterator3.next();
//                if(messagetemp.msgid.equals(node.msgid)){
                q.remove(messagetemp);
                q.add(messagetemp);
//                    Log.d(TAG,"modified queue with message ::"+node.msg+"  Final_Seq :: "+node.seq+"  Status :: "+ node.ready);
            }

            Iterator<Node> nodeIterator4 = q.iterator();
            while(nodeIterator4.hasNext()){
                Node messagetemp = nodeIterator4.next();
//                if(messagetemp.msgid.equals(node.msgid)){
                q.remove(messagetemp);
                q.add(messagetemp);
//                    Log.d(TAG,"modified queue with message ::"+node.msg+"  Final_Seq :: "+node.seq+"  Status :: "+ node.ready);
            }

            while(!q.isEmpty() && q.peek().ready){
                Node node = q.poll();
                Log.d(TAG, "Tag101! after Poll :::" + node.msg);
                Log.d(TAG, "Priority Queue Order- Sequence:::" + node.seq);

                final Uri uri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger2.provider");
                Log.d(TAG, "in onProgressUpdate :: URI Built :: " + uri);


//                String key = Integer.toString(node.seq);
                String key = Integer.toString(keyvalue);
                keyvalue++;
                String msg = node.msg;
                Log.d(TAG, "seq / Key :: " + key + " ::: " + msg);
//            Log.d(TAG, "Values before insert :: s " + msg);

                ContentValues values = new ContentValues();

                values.put("key", key);
                values.put("value", msg);
                //Writing to screen
                text_view.append(key+" ::"+msg+"\n");
                try {
                    Log.d(TAG, "Tag101! Before insert :::" + msg);

                    getContentResolver().insert(uri, values);
                } catch (Exception e) {
                    Log.d(TAG, e.toString());
                }
            }
            return;
        }
    }
}