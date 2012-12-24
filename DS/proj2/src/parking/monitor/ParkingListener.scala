package parking.monitor

import java.rmi.RemoteException
import java.rmi.Remote

trait ParkingListener extends Remote{
  
  @throws(classOf[RemoteException])
  def onCarArrival(car:Car)
  
  @throws(classOf[RemoteException])
  def onCarLeft(car:Car)
  
  @throws(classOf[RemoteException])
  def getCurrentParkingPlace():Int
 
  @throws(classOf[RemoteException])
  def getId():Int
  
  @throws(classOf[RemoteException])
  def printState
}