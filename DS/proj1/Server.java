import java.rmi.*;

public class Server
{

  public static void main (String[] argv) {
    try {
      Naming.rebind ("Clock", new Clock ());
      System.out.println ("Clock Server is ready.");
    } catch (Exception e) {
      System.out.println ("Clock Server failed: " + e);
    }
  }
}