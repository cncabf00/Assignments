import java.rmi.server.*;
import java.rmi.*;
import java.util.Timer;
import java.util.TimerTask;

public class Client
{

  public static void main (String[] argv) {
    if (argv.length<1)
    {
      System.out.println("too few arguments");
    }
    else
    {
      try {
        final CallbackClientInterface cb=new CallbackClientImpl();
        final ClockInterface clock = 
          (ClockInterface) Naming.lookup ("//"+argv[0]+"/Clock");
        Timer timer=new Timer();
        System.out.println(clock.register(cb));
        
      } catch (Exception e) {
        System.out.println ("ClockClient exception: " + e);
        e.printStackTrace();
      }  
    }
  }
}