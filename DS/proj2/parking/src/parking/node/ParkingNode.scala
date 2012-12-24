package parking.node

import java.rmi.Remote
import java.rmi.RemoteException

trait ParkingNode extends Remote {

  @throws(classOf[RemoteException])
  def requestClock(clock:VectorClock):VectorClock

  @throws(classOf[RemoteException])
  def occupyAPlace(actor:Int,clock:VectorClock, refClock:VectorClock)
  
  @throws(classOf[RemoteException])
  def receiveMark(marker:Marker,sender:Int,externalClock:VectorClock)
  
  @throws(classOf[RemoteException])
  def addNode(node:ParkingNode)
  
  @throws(classOf[RemoteException])
  def snapshotCallBack(result:Int,marker:Marker,externalClock:VectorClock)
  
  @throws(classOf[RemoteException])
  def informCarLeft(actor:Int,clock:VectorClock)
  
  @throws(classOf[RemoteException])
  def getId():Int
  
  @throws(classOf[RemoteException])
  def allowParking(sender:Int, refClock:VectorClock,externalClock:VectorClock)
  
  @throws(classOf[RemoteException])
  def addToTodo(clockCopy: VectorClock)
  
}
