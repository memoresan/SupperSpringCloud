package scala

object ImplicitTest {
  def main(args: Array[String]): Unit = {
      8.myMax2(2)
  }

  implicit class MyRich2(self:Int){
    def myMax2(i:Int): Int ={
      if(self < i) i else  self
    }
  }
}
