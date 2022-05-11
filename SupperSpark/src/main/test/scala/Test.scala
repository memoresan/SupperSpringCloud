package scala

object Test {
  def main(args: Array[String]): Unit = {
    var a :Option[String] = None
    if(a.getOrElse(null)==null){
      a = Some("1")
    }
    println(a.get)
  }

}



