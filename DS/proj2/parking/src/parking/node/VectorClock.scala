package parking.node

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.Intercepter
import scala.util.control.Breaks._
object Greater extends Exception

@serializable class VectorClock(val id:Int) extends Equals {
  var internalClock=Map(id->0)
  
  def lessThan(another:VectorClock):Boolean={
    var lessEqual=false
    var less=false
    try
    {
      another.internalClock.foreach{
        case (id,thatTime) => {
          val time=if (internalClock.contains(id)) internalClock(id) else 0
          if (time<=thatTime){
            lessEqual=true
            if (!less && time<thatTime)
              less=true
            }
            else{
              throw Greater;
            }
          }
        }
      less || (lessEqual && id<another.id)
    }
    catch {
      case Greater=>false
      case ex:java.util.NoSuchElementException=>println("vector clock "+id+" missed a dimension: "+ex.getMessage());false
    }
  }
  
  def sync(another:VectorClock)={
//    if (another.id!=this.id)
    {
      this.synchronized
      {
        another.internalClock.foreach{
          case (thatId,time) =>{
            var max=if (internalClock.contains(thatId)) internalClock(thatId) else 0
            max=math.max(time,max)
            internalClock+=(thatId->max)
          }
        }
      }
      
    }
  }
  
  def addDimension(newDimension:Int)={
    internalClock+=(newDimension->0)
  }
  
  def tick()={
    internalClock+=(id->(internalClock(id)+1))
  }
  
  def copy()={
    var selfCopy=new VectorClock(id)
    selfCopy.sync(this)
    selfCopy
  }
  
  def canEqual(other: Any) = {
    other.isInstanceOf[parking.node.VectorClock]
  }
  
  override def equals(other: Any) = {
    other match {
      case that: parking.node.VectorClock => {
        if (internalClock.size!=that.internalClock.size || id!=that.id){
          false
        }
        else{
        var equal=true
		    try
		    {
		      breakable{
		      internalClock.foreach{
		        case (id,time) => {
		          val thatTime=that.internalClock(id)
		          if (time!=thatTime){
		            equal=false
		            break
		            }
		          }
		        }
		      }
		      equal
		    }
		    catch {
		      case ex:java.util.NoSuchElementException=>false
		    }
        }
      }
      case _ => false
    }
  }
  
  
}