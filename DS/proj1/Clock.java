
import java.rmi.*;
import java.rmi.server.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

public class Clock extends UnicastRemoteObject implements ClockInterface {
  Set<CallbackClientInterface> clients=new HashSet<CallbackClientInterface>();
  public Clock () throws RemoteException {
    Timer timer=new Timer();
    timer.scheduleAtFixedRate(new TimerTask() {
          
      @Override
      public void run() {
        List<CallbackClientInterface> needRemove=null;
        for (CallbackClientInterface client:clients)
        {
            try
          {
              client.notifyMe(getTime());
          }
          catch (Exception e) {
                System.out.println ("one ClockClient has been disconnected");
                if (needRemove==null)
                  needRemove=new ArrayList<CallbackClientInterface>();
                needRemove.add(client);
            } 
        }
        if (needRemove!=null)
        {
          for (CallbackClientInterface client:needRemove)
        {
          clients.remove(client);
        }
        }
        
        
      }
    }, 0, 500);
  }
  String getTime(){
    DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    Date date = new Date();
    return dateFormat.format(date);
  }

  public String register(CallbackClientInterface client) throws RemoteException
  {
    clients.add(client);
    return "register successed";
  }
}