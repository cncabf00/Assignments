package parking.node

import java.rmi.RemoteException
import java.rmi.registry.LocateRegistry
import java.rmi.server.UnicastRemoteObject
import scala.actors.Actor
import scala.annotation.serializable
import scala.util.control.Breaks._
import parking.monitor.Address
import parking.monitor.Car
import parking.monitor.Monitor
import parking.monitor.ParkingListener
import com.sun.org.apache.xalan.internal.xsltc.compiler.ForEach

class ParkingNodeImpl(val id: Int, var parkingPlace: Int, address: Address) extends ParkingNode with ParkingListener {
  val monitorHost = "localhost"
  val monitorPort = 10000
  val monitorName = "monitor"
  val tag = "[Node " + id + "]: "

  @serializable var clock = new VectorClock(id)
  var otherNodes = Map[Int,ParkingNode]()
  class MarkerInfo {
    var sendingMarker = Map[Int, Boolean]()
    var waitingMarker = Map[Int, Boolean]()
    var parkingPlaceSnapshot = 0
  }
  var markerInfos = Map[Marker, MarkerInfo]()
  var sendLocks = Map[Int, Object]()
  var snapshotRequestors = Map[Marker, ParkingNode]()
  var waitingCars = List[Car]()
  var todos = List[VectorClock]()
  var initialized = false
  var monitor: Monitor = null
  class Response(var clock: VectorClock) {
    var received = Map[Int, Boolean]()
  }

  var responses = List[Response]()
  var permitSent = false
  val todoLock = new Object()
  val waitingCarLock = new Object()
  val otherNodeLock = new Object()
  val responseLock = new Object()
  val parkingLock = new Object()
  val snapshotRequestorLock = new Object()
  val markerInfoLock = new Object()

  def requestClock(externalClock: VectorClock): VectorClock = {
    syncTime(externalClock).copy
  }

  def allowParking(sender: Int, refClock: VectorClock, externalClock: VectorClock) = {
    tick
    syncTime(externalClock)
    responseLock.synchronized {

      (responses.filter(response => response.clock.equals(refClock))).foreach(response => response.received += (sender -> true))

      if (!responses.head.received.values.exists(p => p == false)) {
        parkACar

        responses = responses.tail
      }
    }
  }

  def occupyAPlace(actor: Int, clock: VectorClock, refClock: VectorClock) = {
    parkingLock.synchronized {
      parkingPlace -= 1
      //            println(tag+"parkingPlace-1,now="+parkingPlace)
      todoLock.synchronized {
        if (!todos.isEmpty && todos.head.equals(refClock)) {
          //          println(tag+"pop todos next is node "+(if (todos.tail.isEmpty) "null" else todos.tail.head.id))
          todos = todos.tail
          permitSent = false
        }
      }
      markerInfoLock.synchronized {
        if (actor != id) {
          markerInfos.foreach(p => {
            if (p._2.waitingMarker.contains(actor) && p._2.waitingMarker(actor))
              p._2.parkingPlaceSnapshot -= 1
          })
        }
      }
      tick
      syncTime(clock)
    }

  }

  def checkSnapShot(marker: Marker, info: MarkerInfo) = {
    snapshotRequestorLock.synchronized {
      if (!info.waitingMarker.exists(p => p._2 == true) && !info.sendingMarker.exists(p => p._2 == true)) {
        //      println(tag+"snapshot end")
        if (snapshotRequestors.contains(marker)) {
          tick
          snapshotRequestors(marker).snapshotCallBack(info.parkingPlaceSnapshot, marker, clock)
          snapshotRequestors -= marker
        }
      }
    }
  }

  def receiveMark(marker: Marker, sender: Int, externalClock: VectorClock) = {
    //    println(tag+"receive maker from node "+sender)
    markerInfoLock.synchronized {
      syncTime(externalClock)
      if (!markerInfos.contains(marker)) {
        createMarkerInfo(marker)
        sendMarkerToAll(marker)
        snapshotRequestorLock.synchronized {
          snapshotRequestors += (marker -> marker.requestor)
        }
      }
      val info = markerInfos(marker)
      info.waitingMarker.synchronized {
        info.waitingMarker += (sender -> false)
        //    	println(tag+"waitingMarker="+info.waitingMarker)
      }
      checkSnapShot(marker, info)
    }
  }

  def addNode(node: ParkingNode) = {
    //    println(tag+"add node "+node.getId)
    otherNodeLock.synchronized {
      val id=node.getId
      otherNodes += (id->node)
      clock.addDimension(id)
      sendLocks += (id -> new Object)
    }
  }

  def snapshotCallBack(result: Int, marker: Marker, externalClock: VectorClock) = {
    parkingLock.synchronized {
      if (!initialized) {
        tick
        syncTime(externalClock)
        parkingPlace += result - markerInfos(marker).parkingPlaceSnapshot
        initialized = true
        checkTodos
      }
    }
    //    println(tag+"initialized")
    //    println(tag+"current parking places="+parkingPlace)
  }

  def sendMark(nodeId:Int, node: ParkingNode, marker: Marker) = {
    tick
    val clockCopy = clock.copy
    class MarkerSender extends Actor {
      def act() {
        sendLocks(nodeId).synchronized {
          //          println(tag+"send marker from node "+id+" to node "+node.getId)
          val info = markerInfos(marker)
          info.sendingMarker.synchronized {
            info.sendingMarker += (nodeId -> false)
            //        	  println(tag+"send marker from node "+id+" to node "+node.getId+"\n"+info.sendingMarker)
            checkSnapShot(marker, info)
          }
          node.receiveMark(marker, id, clockCopy)

        }
      }
    }
    (new MarkerSender).start

  }

  def sendMarkerToAll(marker: Marker) = {
    val markerInfo = markerInfos(marker)
    otherNodes.foreach(p => sendMark(p._1, p._2, marker))
  }

  def addToTodo(clockCopy: VectorClock) = {
    todoLock.synchronized {
      tick
      syncTime(clockCopy)
      todos = todos ::: List(clockCopy)
      todos = todos.sortWith((x, y) => x.lessThan(y))
      checkTodos
    }

  }

  def sendParkingInfo(nodeId:Int,node: ParkingNode, clockCopy: VectorClock) = {
    sendLocks(nodeId).synchronized {
      node.addToTodo(clockCopy)
    }
  }

  def takeSnapshot(): Marker = {
    //    println(tag+"start taking snapshot")
    @serializable val marker = new Marker(System.nanoTime(), id, this)
    createMarkerInfo(marker)
    sendMarkerToAll(marker)
    marker
    //    }
  }

  def createMarkerInfo(marker: Marker) = {

    val markerInfo = new MarkerInfo()
    markerInfo.parkingPlaceSnapshot = parkingPlace
    otherNodeLock.synchronized {
      otherNodes.foreach(p => markerInfo.waitingMarker += (p._1 -> true))
      otherNodes.foreach(p => markerInfo.sendingMarker += (p._1 -> true))

      //    println(tag+"create maker info waitingMarker="+markerInfo.waitingMarker+" sendingMarker="+markerInfo.sendingMarker)
      markerInfoLock.synchronized {
        markerInfos += (marker -> markerInfo)
      }
    }
  }

  def parkACar() = {
    //    println(tag+"park a car")
    waitingCarLock.synchronized {
      val car = waitingCars.head
      tick
      val clockCopy = clock.copy
      val refClock = todos.head
      monitor.carParked(car, this)
      this.occupyAPlace(id, clockCopy, refClock)
      waitingCars = waitingCars.tail
      class ParkingInformer extends Actor {
        def act() = {
          otherNodes.foreach(p => {
            p._2.occupyAPlace(id, clockCopy, refClock)
          })
        }
      }
      (new ParkingInformer).start

    }
    //    

  }

  def checkTodos() = {
    //    println(tag+"check todo, current parking place="+parkingPlace+" permitSent="+permitSent+", todo list="+todos.length)
    todoLock.synchronized {
      if (parkingPlace > 0 && !todos.isEmpty && !permitSent) {
        //        println(tag+"first in todos is node "+todos.head.id)
        if (todos.head.id != id) {
//                println(tag + "allow node " + todos.head.id)
                otherNodes(todos.head.id).allowParking(id, todos.head, clock)
                permitSent = true
                //	            println(tag+"permitSent="+permitSent)
        }
      }
    }
  }

  def tick() = {
    clock.synchronized {
      clock.tick
    }
  }

  def syncTime(externalClock: VectorClock): VectorClock = {
    //    println(tag+"sync with "+externalClock.internalClock)
    clock.synchronized {
      clock.sync(externalClock)
      clock
    }
    //    println(tag+"after sync "+clock.internalClock)
  }

  def tryToPark() = {
    responseLock.synchronized {
      tick
      val clockCopy = clock.copy
      addToTodo(clockCopy)
      //    println(tag+"car No."+waitingCars.last.id+" has a vector time "+clockCopy.internalClock)

      var response = new Response(clockCopy)
      otherNodes.foreach(p => {
        response.received += (p._1 -> false)
      })
      responses = responses ::: List(response)

      otherNodes.foreach(p => {
        class Parker extends Actor {
          def act() {
            sendParkingInfo(p._1,p._2, clockCopy)
          }
        }
        (new Parker).start
      })
    }

  }

  def onCarArrival(car: Car) = {
    waitingCarLock.synchronized {
      waitingCars = waitingCars ::: List(car)
      waitingCars.foreach(car => {
        //        println(tag+"cars: "+car.id)
      })
      tryToPark

    }
  }

  def onCarLeft(car: Car) = {
    val clockCopy = clock.copy
    this.informCarLeft(id, clockCopy)
    otherNodes.foreach(p => {
      sendLocks(p._1).synchronized {
        p._2.informCarLeft(id, clockCopy)
      }
    })
  }

  def informCarLeft(actor: Int, externalClock: VectorClock) = {
    parkingLock.synchronized {
      parkingPlace += 1
      //    println(tag+"parkingPlace+1,now="+parkingPlace)
      if (actor != id) {
        markerInfos.foreach(p => {
          if (p._2.waitingMarker.contains(actor) && p._2.waitingMarker(actor))
            p._2.parkingPlaceSnapshot += 1
        })
      }
      syncTime(externalClock)
      checkTodos
    }

  }

  def getCurrentParkingPlace() = {
    println(clock.internalClock)
    parkingPlace
  }

  def getId() = id

  def registerAsServer() = {
    try {

      if (System.getSecurityManager == null) {
        System.setSecurityManager(new SecurityManager)
      }

      val stub = UnicastRemoteObject.exportObject(this, 0)
      val registry = LocateRegistry.createRegistry(address.port)

      // Register this object as the RMI handler
      val url = "//" + address.host + ":" + address.port.toString + "/" + address.name
      registry.rebind(url, stub)
      println("Node " + id + " ready, java.RMI listening on " + url)

    } catch {
      case e: java.rmi.server.ExportException => e.printStackTrace()
      case e: java.io.FileNotFoundException => e.printStackTrace()
    }
  }

  def getMonitor() = {
    try {

      // Setup the security manager so we can get the Student object shipped to us over RMI
      if (System.getSecurityManager == null) {
        System.setSecurityManager(new SecurityManager);
      }

      val registry = LocateRegistry.getRegistry(monitorPort)
      monitor = registry.lookup("//" + monitorHost + ":" + monitorPort.toString + "/" + monitorName).asInstanceOf[Monitor]

    } catch {
      case e: java.security.AccessControlException => e.printStackTrace()
      case e: java.rmi.ConnectException =>
      case e: java.io.FileNotFoundException =>
      case e: java.util.NoSuchElementException => e.printStackTrace()
    }
  }

  def getOtherNodes() = {
    val listOfAddress = monitor.getNodeList
    if (listOfAddress != null) {
      if (listOfAddress.length > 0)
        listOfAddress.foreach(nodeAddress => {
          try {
            val registry = LocateRegistry.getRegistry(nodeAddress.port)
            val node = registry.lookup("//" + nodeAddress.host + ":" + nodeAddress.port.toString + "/" + nodeAddress.name).asInstanceOf[ParkingNode]
            addNode(node)
            node.addNode(this)

          } catch {
            case e: java.security.AccessControlException => e.printStackTrace()
            case e: java.rmi.ConnectException =>
            case e: java.io.FileNotFoundException =>
            case e: java.util.NoSuchElementException => e.printStackTrace()
          }
        })
    }
  }

  def init() = {
    parkingLock.synchronized {
      registerAsServer
      getMonitor
      getOtherNodes
      initialized = false
      if (!otherNodes.isEmpty) {
        takeSnapshot
      }
    }
  }

  def printState() = {
    println(tag + "parking place=" + parkingPlace)

  }

}