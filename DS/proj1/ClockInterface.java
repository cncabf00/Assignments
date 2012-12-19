import java.rmi.*;
public interface ClockInterface extends Remote {
  public String register(CallbackClientInterface client) throws RemoteException;
}