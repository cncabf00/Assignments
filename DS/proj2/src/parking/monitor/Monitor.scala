package parking.monitor

import java.rmi.Remote
import java.rmi.RemoteException

import parking.node.ParkingNode

trait Monitor extends Remote{
    
  @throws(classOf[RemoteException])
  def carParked(car:Car,node:ParkingListener)
  
  @throws(classOf[RemoteException])
  def getNodeList():List[Address]
}