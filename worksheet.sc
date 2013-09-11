/**
  https://github.com/cvogt/slick-worksheet/
  Branch: SEGL2013
*/
import setup._
import driver.simple._
import slick.jdbc.StaticQuery.interpolation
import scala.slick.jdbc.{GetResult, StaticQuery => Q}
/*
	Example Data Model
	
	+------------------+
	| COMPUTER         |        +---------+
	+------------------+        | COMPANY |
	| NAME             |        +---------+
	| MANUFACTURER_ID--+--------| ID      |
	+------------------+        | NAME    |
	                            +---------+
*/

object worksheet {
  println("Hello World")                          //> Hello World

  //val db = Database.forURL("jdbc:h2:mem:test", driver = "org.h2.Driver")
}