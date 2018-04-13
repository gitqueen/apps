package GroupChat;

import javax.bluetooth.*;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import java.io.*;
import javax.microedition.io.*;
import java.util.TimerTask;
import java.util.Timer;

public class Server extends MIDlet implements CommandListener, DiscoveryListener {

    private int MAX_CLIENTS=7;
    private static String eof = "EOF\r\n";
    private Display disp;
    private Form form;
    private TextField text;
    private Command exit;
    private Command host;
    private Command send;
    private String UIDstr="BAE0D0C0B0A000955570605040302010";
    private String iName;
    private int current_location=0;
    private int current_clients=0;
    private InputStream in[];
    private OutputStream out[];
    private Timer timer[];
    private reading_listener hark[];
    private ImageDisplayingCanvas canvas;

    public Server(){
        try {
            iName = LocalDevice.getLocalDevice().getFriendlyName();
        } catch (BluetoothStateException ex) {
            ex.printStackTrace();
        }
        disp=Display.getDisplay(this);
        form=new Form("Server");
        text=new TextField(iName+":","",60,TextField.ANY);
        exit=new Command("Quit",Command.SCREEN,100);
        host=new Command("HOST a chat",Command.SCREEN,50);
        send=new Command("POST!!!",Command.SCREEN,1);
        form.addCommand(exit);
        form.addCommand(host);
        form.setCommandListener(this);
        in=new InputStream[MAX_CLIENTS];
        out=new OutputStream[MAX_CLIENTS];
        timer=new Timer[MAX_CLIENTS];
        hark=new reading_listener[MAX_CLIENTS];
        canvas=new ImageDisplayingCanvas();
    }

    protected void destroyApp(boolean unconditional) {
    }

    protected void pauseApp() {
    }

    protected void startApp() throws MIDletStateChangeException {
        canvas.setDisplayForm(disp, form);
        disp.setCurrent(canvas);
    }

    public void commandAction(Command c, Displayable d) {
        if (c==exit){
            destroyApp(false);
            notifyDestroyed();
        }
        else if (c==host){
            //System.out.println("host command");
            if (current_clients==0) {
                form.addCommand(send);
                form.append(text);
            }
            try {
                StreamConnectionNotifier strConn = (StreamConnectionNotifier) Connector.open("btspp://localhost:"+UIDstr+";name=SS");
                StreamConnection connection = (StreamConnection) strConn.acceptAndOpen();
                //System.out.println("acceptAndOpen() returns");
                out[current_clients]=connection.openOutputStream();
                in[current_clients]=connection.openInputStream();
                //System.out.println("i/o established");
                hark[current_clients]=new reading_listener(current_clients);
                timer[current_clients]=new Timer();
                timer[current_clients].schedule(hark[current_clients], 1000, 100);
                //System.out.println("scheduling done");
                current_clients++;
                //System.out.println("current_clients="+current_clients);
                if (current_clients==MAX_CLIENTS){
                    form.removeCommand(c);
                }
            } catch (IOException ex) {
                Alert alert=new Alert("Could not Initialise","Check Bluetooth Settings.",null,AlertType.ERROR);
                alert.setTimeout(Alert.FOREVER);
                disp.setCurrent(alert,form);
            }
        }
        else if (c==send){
            String temp=text.getString().trim();
            if (!temp.equals("")) {
                for (int i=0;i<current_clients;i++){
                    sendData(iName+".$."+temp,i);
                }
                StringItem temptemp = new StringItem("-----"+iName+"-----",temp);
                text.setString("");
                form.insert(current_location, temptemp);
                current_location++;
            }
        }
    }

    public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
    }

    public void inquiryCompleted(int discType) {
    }

    public void serviceSearchCompleted(int transID, int respCode) {
    }

    public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
    }

    private String ReceiveMessages(int index) {
        StringBuffer buffer = new StringBuffer("");
        try {
            int xx = 0;
            while (xx != -1) {
                xx = in[index].read();
                if (xx != -1) {
                    buffer.append((char) xx);
                }
                if (buffer.toString().endsWith(eof)) {
                    buffer.delete(buffer.length()-eof.length(), buffer.length());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buffer.toString();
    }

    public void sendData(String v, int index){
        try {
            out[index].write(v.getBytes());
            out[index].write(eof.getBytes());
            out[index].flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public class reading_listener extends TimerTask {

        private int index;

        reading_listener(int index_constructor){
            index=index_constructor;
        }

        public void run() {
            try {
                if (in[index].available() != 0) {
                    String temp = ReceiveMessages(index);
                    int special=temp.indexOf(".$.");
                    String name=temp.substring(0, special);
                    String message=temp.substring(special+3, temp.length());
                    StringItem temptemp = new StringItem("-----"+name+"-----", message);
                    form.insert(current_location, temptemp);
                    current_location++;
                    //System.out.println("index="+index);
                    //System.out.println("current_clients="+current_clients);
                    for (int i=0;i<current_clients;i++){
                        //System.out.println("i="+i+" index="+index);
                        if (i!=index){
                            sendData(temp,i);
                        }
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}