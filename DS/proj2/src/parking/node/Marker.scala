package parking.node

@serializable class Marker(val id:Long, val initiator:Int, val requestor:ParkingNode) extends Equals {
  def canEqual(other: Any) = {
    other.isInstanceOf[parking.node.Marker]
  }
  
  override def equals(other: Any) = {
    other match {
      case that: parking.node.Marker => that.canEqual(Marker.this) && id == that.id && initiator == that.initiator
      case _ => false
    }
  }
  
  override def hashCode() = {
    val prime = 41
    prime * (prime + id.hashCode) + initiator.hashCode
  }
}