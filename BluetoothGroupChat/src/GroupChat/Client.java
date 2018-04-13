package GroupChat;

import javax.bluetooth.*;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import java.io.*;
import java.util.Vector;
import javax.microedition.io.*;
import java.util.TimerTask;
import java.util.Timer;

public class Client extends MIDlet implements CommandListener {

    private static String eof = "EOF\r\n";
    private Display disp;
    private Form form;
    private TextField text;
    private ChoiceGroup radio;
    private Command exit;
    private Command find_devices;
    private Command send;
    private Command select_device;
    private Command cancel_discovery;
    private ClientThread ct;
    private String UIDstr="BAE0D0C0B0A000955570605040302010";
    private String iName;
    private int chosen;
    private Vector devices;
    private ServiceRecord servRec[];
    private int current_location=0;
    private Timer timer;
    private Command select_service;
    private ImageDisplayingCanvas canvas;

    public Client(){
        try {
            iName = LocalDevice.getLocalDevice().getFriendlyName();
        } catch (BluetoothStateException ex) {
            ex.printStackTrace();
        }
        disp=Display.getDisplay(this);
        form=new Form("Client");
        text=new TextField(iName+":","",60,TextField.ANY);
        radio=new ChoiceGroup("",Choice.EXCLUSIVE);
        exit=new Command("Quit",Command.SCREEN,100);
        find_devices=new Command("FIND devices",Command.SCREEN,3);
        send=new Command("POST!!!",Command.SCREEN,1);
        cancel_discovery=new Command("Stop Discovering",Command.SCREEN,5);
        select_device=new Command("Choose Device",Command.SCREEN,4);
        select_service=new Command("Choose Service",Command.SCREEN,4);
        form.addCommand(exit);
        form.addCommand(find_devices);
        form.setCommandListener(this);
        devices=new Vector();
        timer=new Timer();
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
        else if (c==find_devices){
            form.removeCommand(c);
            ct=new ClientThread();
            ct.run();
            radio.setLabel("Devices Found:");
            form.append(radio);
            form.addCommand(cancel_discovery);
        }
        else if (c==cancel_discovery){
            ct.cancelSearch();
            form.removeCommand(c);
            form.addCommand(select_device);
        }
        else if (c==select_device){
            form.removeCommand(c);
            chosen=radio.getSelectedIndex();
            radio.deleteAll();
            ct.findServices();
            form.addCommand(select_service);
        }
        else if (c==select_service){
            form.removeCommand(c);
            chosen=0;
            ct.establishConnection();
            form.addCommand(send);
            form.delete(0);
            form.append(text);
            timer.schedule(ct.hark, 1000, 500);
        }
        else if (c==send){
            String temp=text.getString().trim();
            if (!temp.equals("")) {
                ct.sendData(iName+".$."+temp);
                text.setString("");
                StringItem temptemp = new StringItem("-----"+iName+"-----", temp);
                form.insert(current_location, temptemp);
                current_location++;
            }
        }
    }

    private class ClientThread implements Runnable, DiscoveryListener{

        private LocalDevice x;
        private DiscoveryAgent agent;
        private OutputStream out;
        private InputStream in;
        private reading_listener hark;

        public ClientThread (){
            //System.out.println("ClientThread() Constructor called");
            try {
                x = LocalDevice.getLocalDevice();
                agent=x.getDiscoveryAgent();
            } catch (BluetoothStateException ex) {
                Alert alert=new Alert("Unable to Get Local Device",ex.toString(),null,AlertType.ERROR);
                alert.setTimeout(Alert.FOREVER);
                disp.setCurrent(alert,form);
            }
            hark=new reading_listener();
        }

        public void run() {
            try {
                agent.startInquiry(DiscoveryAgent.GIAC, this);
            } catch (BluetoothStateException ex) {
                Alert alert=new Alert("Unable to Start Bluetooth","Ensure BT is on."+ex.toString(),null,AlertType.ERROR);
                alert.setTimeout(Alert.FOREVER);
                disp.setCurrent(alert,form);
            }
        }

        public void cancelSearch(){
            agent.cancelInquiry(this);
        }

        public void findServices(){
            UUID uid[]=new UUID[1];
            uid[0]=new UUID(UIDstr,false);
            try {
                agent.searchServices(null, uid, (RemoteDevice) devices.elementAt(chosen), this);
            } catch (BluetoothStateException ex) {
                ex.printStackTrace();
            }
        }

        public void establishConnection(){
            //System.out.println("inside establish connection()");
            //System.out.println(servRec[chosen].toString());
            String connURL=servRec[chosen].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
            //System.out.println("after getURL");
            StreamConnection strconn;
            try {
                strconn = (StreamConnection) Connector.open(connURL);
                out=strconn.openOutputStream();
                in=strconn.openInputStream();
                //System.out.println("strconn="+strconn.toString());
                //System.out.println("in="+in.toString());
                //System.out.println("out="+out.toString());
                hark.passInputStream(this.in);
                text.setString("heyy...its "+iName+"here :)");
                //System.out.println("hark"+hark.toString());
                //System.out.println("uName="+uName.toString());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
            devices.addElement(btDevice);
            try {
                radio.append(btDevice.getFriendlyName(false), null);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        public void inquiryCompleted(int discType) {
        }

        public void serviceSearchCompleted(int transID, int respCode) {
            if (respCode==DiscoveryListener.SERVICE_SEARCH_COMPLETED){
                int length=servRec.length;
                radio.setLabel("Services Found:");
                for (int i=0;i<length;i++){
                radio.append("BT Chat\n"+/*jhol :P*/servRec[i].toString(), null);
                }
            }
            else{
                Alert alert=new Alert("SERVICE SEARCH","No Services Found",null,AlertType.ERROR);
                alert.setTimeout(Alert.FOREVER);
                disp.setCurrent(alert,form);
            }
        }

        public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
            servRec=servRecord;
        }

        public void sendData(String v){
            try {
                out.write(v.getBytes());
                out.write(eof.getBytes());
                out.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public class reading_listener extends TimerTask {

        private InputStream in;

        public void passInputStream(InputStream inn){
            this.in=inn;
        }

        private String ReceiveMessages() {
        StringBuffer buffer = new StringBuffer("");
        try {
                int x = 0;
                while (x != -1) {
                    x = in.read();
                    if (x != -1) {
                        buffer.append((char) x);
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

        public void run() {
            //while (true){
                try {
                    if (in.available() != 0) {
                        String temp = ReceiveMessages();
                        int special=temp.indexOf(".$.");
                        String name=temp.substring(0, special);
                        String message=temp.substring(special+3, temp.length());
                        StringItem temptemp = new StringItem("-----"+name+"-----", message);
                        form.insert(current_location, temptemp);
                        current_location++;
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            //}
        }
    }
}