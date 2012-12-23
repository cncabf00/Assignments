package parking.monitor

import java.rmi.Naming
import java.rmi.RemoteException
import java.rmi.RMISecurityManager
import java.rmi.registry.LocateRegistry
import java.rmi.registry.Registry
import java.rmi.server.UnicastRemoteObject
import java.util.Date

import scala.actors.Actor
import scala.util.Random

import parking.node.ParkingNodeImpl

class MonitorImpl extends Monitor{
  
  val monitorHost="localhost"
  val monitorPort=10000
  val monitorName="monitor"
  var parkingPlace=3
  var nodes=List[ParkingListener]()
  @serializable var nodeAddress=List[Address]()
  var carID=0
  val rand=new Random(System.nanoTime())
  var parkedCars=List[Car]()
  val parkingLock=new Object()
  
  def carParked(car:Car,node:ParkingListener)={
    parkingLock.synchronized
    {
      parkingPlace-=1
      parkedCars=parkedCars:::List(car)
      println("PARKED: car(No."+car.id+","+car.arrivingTime.toString()+") has parked in parking node "+node.getId+", current parkingPlace="+parkingPlace)
    }
  }
  
  def getNodeList():List[Address]={
    nodeAddress
  }
  
  def carLeave()={
    if (!parkedCars.isEmpty)
    {
      parkingPlace+=1
      val car=parkedCars(rand.nextInt(parkedCars.length))
      val node=nodes(rand.nextInt(nodes.length))
      parkingLock.synchronized
      {
        parkedCars-=car
      }
      println("LEFT: car(No."+car.id+") has left from node "+node.getId+" current parkingPlace="+parkingPlace)
      node.onCarLeft(car)
    }
  }
  
  def arrangeCar()={
    val car=new Car(carID,new Date())
    carID+=1
    val pos=rand.nextInt(nodes.length)
    val node=nodes(pos)
    node.onCarArrival(car)
//    println("car(No."+car.id+","+car.arrivingTime.toString()+") has been arranged to parking node "+node.getId)
  }
  
  def printGlobalState()={
    println("----------global state---------------")
    println("real parkingPlace="+parkingPlace)
    nodes.foreach(node=>node.printState)
    println("-------------------------------------")
  }
  
    def registerAsServer()={
    try {
        
      if (System.getSecurityManager == null) {
        System.setSecurityManager(new SecurityManager)
      }
  
      val stub = UnicastRemoteObject.exportObject(this, 0)
      val registry = LocateRegistry.createRegistry(monitorPort)
  
      // Register this object as the RMI handler
      val url = "//" + monitorHost + ":" + monitorPort.toString + "/" + monitorName
      registry.rebind(url, stub)
      println("Monitor ready, java.RMI listening on " + url)
  
    } catch {
      case e: java.rmi.server.ExportException => e.printStackTrace()
      case e: java.io.FileNotFoundException => e.printStackTrace()
    }
  }
    
    def makeSomeNodes()={
      for (i<- 1 to 7)
      {
        @serializable val address=new Address("localhost",8000+i,"parking")
        @serializable val node=new ParkingNodeImpl(i,parkingPlace,address)
        node.init
        nodes=nodes:::List(node)
        nodeAddress=nodeAddress:::List(address)
//        Thread.sleep(1000)
//        printGlobalState
        if (i>=5)
          Thread.sleep(2000)
      }
    }
    
    def start()={
      registerAsServer
      object nodeMaker extends Actor{
        def act()={
          makeSomeNodes
        }
      }
      nodeMaker.start
      Thread.sleep(1000)
      printGlobalState
      class Arranger extends Actor {
         def act() {
          
            for (i<- 1 to 300)
            {
              nodes.synchronized {
              arrangeCar
              }
              Thread.sleep(rand.nextInt(50)+70)
            }
        }
      }
      (new Arranger).start
      class Pusher extends Actor {
         def act() {
           for (i<-1 to 299)
           {
             Thread.sleep(rand.nextInt(50)+75)
            nodes.synchronized {
              carLeave
            }
           }
           Thread.sleep(1000)
           printGlobalState
           System.exit(0)
        }
      }
      (new Pusher).start
    }
}