import scala.util.control.Breaks._
import parking.monitor.MonitorImpl
import parking.node.VectorClock
object tester extends App {
val monitor=new MonitorImpl
monitor.start

//  var clock1=new VectorClock(1)
//  var clock2=new VectorClock(2)
//  var clock3=new VectorClock(3)
//  var clock4=new VectorClock(4)
//  var clock5=new VectorClock(5)
//  var clock6=new VectorClock(1)
//  var clock7=new VectorClock(2)
//  var clock8=new VectorClock(3)
//  var clock9=new VectorClock(4)
//  var clock10=new VectorClock(5)
//  var clock11=new VectorClock(1)
//  var clock12=new VectorClock(2)
//  var clock13=new VectorClock(3)
//  var clock14=new VectorClock(4)
//  clock1.internalClock=Map(5 -> 0, 1 -> 1, 2 -> 2, 3 -> 1, 4 -> 2)
//  clock2.internalClock=Map(5 -> 0, 1 -> 4, 2 -> 1, 3 -> 1, 4 -> 2)
//  clock3.internalClock=Map(5 -> 4, 1 -> 2, 2 -> 1, 3 -> 0, 4 -> 0)
//  clock4.internalClock=Map(5 -> 5, 1 -> 2, 2 -> 1, 3 -> 0, 4 -> 0)
//	clock5.internalClock=Map(5 -> 5, 1 -> 3, 2 -> 1, 3 -> 0, 4 -> 0)
//	clock6.internalClock= Map(5 -> 5, 1 -> 4, 2 -> 1, 3 -> 0, 4 -> 0)
//	clock7.internalClock=Map(5 -> 5, 1 -> 4, 2 -> 1, 3 -> 1, 4 -> 0)
//	clock8.internalClock=Map(5 -> 5, 1 -> 4, 2 -> 1, 3 -> 2, 4 -> 0)
//	clock9.internalClock=Map(5 -> 5, 1 -> 4, 2 -> 2, 3 -> 2, 4 -> 0)
//	clock10.internalClock=Map(5 -> 5, 1 -> 4, 2 -> 2, 3 -> 2, 4 -> 1)
//	clock11.internalClock=Map(5 -> 5, 1 -> 4, 2 -> 3, 3 -> 2, 4 -> 1)
//	clock12.internalClock=Map(5 -> 5, 1 -> 4, 2 -> 4, 3 -> 2, 4 -> 1)
//	clock13.internalClock=Map(5 -> 6, 1 -> 4, 2 -> 4, 3 -> 2, 4 -> 1)
//	clock14.internalClock=Map(5 -> 7, 1 -> 4, 2 -> 4, 3 -> 2, 4 -> 1)
//	var list=List(clock1,clock2,clock3,clock4,clock5,clock6,clock7,clock8,clock9,clock10,clock11,clock12,clock13,clock14)
//	list=list.sortWith((x,y)=>x.lessThan(y))
// 	list.foreach(clock=>{
// 	  println(clock.id)
// 	})
}