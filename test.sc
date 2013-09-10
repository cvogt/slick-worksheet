import scala.slick.driver.H2Driver.simple._
import scala.slick.driver.H2Driver
import slick.jdbc.StaticQuery.interpolation
import config._
object test {
  val computerId = 1                              //> computerId  : Int = 1
  
  // plain SQL, using interpolation for automatic escaping
  val computerSql = sql"select * from COMPUTER c WHERE c.id = $computerId"
                                                  //> computerSql  : scala.slick.jdbc.SQLInterpolationResult[Int] = SQLInterpolati
                                                  //| onResult(WrappedArray(select * from COMPUTER c WHERE c.id = , ),1,<function2
                                                  //| >)

  computerSql.as[(Long,String,Long)]              //> res0: scala.slick.jdbc.StaticQuery0[(Long, String, Long)] = scala.slick.jdbc
                                                  //| .StaticQuery0@7e78fc6

  computerSql.as[Computer].list                   //> res1: List[Computer] = List(Computer(1,MacBook Pro 15.4 inch,1))
  
  sql"select * from COMPUTER c,COMPANY co where c.MANUFACTURER_ID = co.ID".as[(Computer,Company)].list
                                                  //> res2: List[(Computer, Company)] = List((Computer(1,MacBook Pro 15.4 inch,1),
                                                  //| Company(1,Apple Inc.)), (Computer(2,CM-2a,2),Company(2,Thinking Machines)), 
                                                  //| (Computer(3,CM-200,2),Company(2,Thinking Machines)), (Computer(4,CM-5e,2),Co
                                                  //| mpany(2,Thinking Machines)), (Computer(5,CM-5,2),Company(2,Thinking Machines
                                                  //| )), (Computer(6,MacBook Pro,1),Company(1,Apple Inc.)), (Computer(7,Apple IIe
                                                  //| ,1),Company(1,Apple Inc.)), (Computer(8,Apple IIc,1),Company(1,Apple Inc.)),
                                                  //|  (Computer(9,Apple IIGS,1),Company(1,Apple Inc.)), (Computer(10,Apple IIc Pl
                                                  //| us,1),Company(1,Apple Inc.)), (Computer(11,Apple II Plus,1),Company(1,Apple 
                                                  //| Inc.)), (Computer(12,Apple III,1),Company(1,Apple Inc.)), (Computer(13,Apple
                                                  //|  Lisa,1),Company(1,Apple Inc.)), (Computer(14,CM-2,2),Company(2,Thinking Mac
                                                  //| hines)), (Computer(15,Connection Machine,2),Company(2,Thinking Machines)), (
                                                  //| Computer(16,Apple II,1),Company(1,Apple Inc.)), (Computer(17,Apple III Plus,
                                                  //| 1),Company(1,Apple Inc.)
                                                  //| Output exceeds cutoff limit.
  
  // type-safe, collection-like queries
  Companies.take(5).run                           //> res3: Seq[Company] = Vector(Company(1,Apple Inc.), Company(2,Thinking Machin
                                                  //| es), Company(3,RCA), Company(4,Netronics), Company(5,Tandy Corporation))
  
  (for( c <- Computers; co <- Companies; if c.manufacturerId === co.id && c.id === 1L ) yield (c,co)).first
                                                  //> res4: (Computer, Company) = (Computer(1,MacBook Pro 15.4 inch,1),Company(1,A
                                                  //| pple Inc.))
  
  // remote joing
  Computers.flatMap(
    c => Companies.filter( co => c.manufacturerId === co.id )
            .map( co => (c,co) )
  ).run : Seq[(Computer,Company)]                 //> res5: Seq[(Computer, Company)] = Vector((Computer(1,MacBook Pro 15.4 inch,1)
                                                  //| ,Company(1,Apple Inc.)), (Computer(2,CM-2a,2),Company(2,Thinking Machines)),
                                                  //|  (Computer(3,CM-200,2),Company(2,Thinking Machines)), (Computer(4,CM-5e,2),C
                                                  //| ompany(2,Thinking Machines)), (Computer(5,CM-5,2),Company(2,Thinking Machine
                                                  //| s)), (Computer(6,MacBook Pro,1),Company(1,Apple Inc.)), (Computer(7,Apple II
                                                  //| e,1),Company(1,Apple Inc.)), (Computer(8,Apple IIc,1),Company(1,Apple Inc.))
                                                  //| , (Computer(9,Apple IIGS,1),Company(1,Apple Inc.)), (Computer(10,Apple IIc P
                                                  //| lus,1),Company(1,Apple Inc.)), (Computer(11,Apple II Plus,1),Company(1,Apple
                                                  //|  Inc.)), (Computer(12,Apple III,1),Company(1,Apple Inc.)), (Computer(13,Appl
                                                  //| e Lisa,1),Company(1,Apple Inc.)), (Computer(14,CM-2,2),Company(2,Thinking Ma
                                                  //| chines)), (Computer(15,Connection Machine,2),Company(2,Thinking Machines)), 
                                                  //| (Computer(16,Apple II,1),Company(1,Apple Inc.)), (Computer(17,Apple III Plus
                                                  //| ,1),Company(1,Apple Inc.
                                                  //| Output exceeds cutoff limit.
  
  // local join
  val companies = Companies.run                   //> companies  : Seq[Company] = Vector(Company(1,Apple Inc.), Company(2,Thinking
                                                  //|  Machines), Company(3,RCA), Company(4,Netronics), Company(5,Tandy Corporatio
                                                  //| n), Company(6,Commodore International), Company(7,MOS Technology), Company(8
                                                  //| ,Micro Instrumentation and Telemetry Systems), Company(9,IMS Associates,  In
                                                  //| c.), Company(10,Digital Equipment Corporation), Company(11,Lincoln Laborator
                                                  //| y), Company(12,Moore School of Electrical Engineering), Company(13,IBM), Com
                                                  //| pany(14,Amiga Corporation), Company(15,Canon), Company(16,Nokia), Company(17
                                                  //| ,Sony), Company(18,OQO), Company(19,NeXT), Company(20,Atari), Company(22,Aco
                                                  //| rn COMPUTER), Company(23,Timex Sinclair), Company(24,Nintendo), Company(25,S
                                                  //| inclair Research Ltd), Company(26,Xerox), Company(27,Hewlett-Packard), Compa
                                                  //| ny(28,Zemmix), Company(29,ACVS), Company(30,Sanyo), Company(31,Cray), Compan
                                                  //| y(32,Evans & Sutherland), Company(33,E.S.R. Inc.), Company(34,OMRON), Compan
                                                  //| y(35,BBN Technologies), 
                                                  //| Output exceeds cutoff limit.
  val computers = Computers.run                   //> computers  : Seq[Computer] = Vector(Computer(1,MacBook Pro 15.4 inch,1), Com
                                                  //| puter(2,CM-2a,2), Computer(3,CM-200,2), Computer(4,CM-5e,2), Computer(5,CM-5
                                                  //| ,2), Computer(6,MacBook Pro,1), Computer(7,Apple IIe,1), Computer(8,Apple II
                                                  //| c,1), Computer(9,Apple IIGS,1), Computer(10,Apple IIc Plus,1), Computer(11,A
                                                  //| pple II Plus,1), Computer(12,Apple III,1), Computer(13,Apple Lisa,1), Comput
                                                  //| er(14,CM-2,2), Computer(15,Connection Machine,2), Computer(16,Apple II,1), C
                                                  //| omputer(17,Apple III Plus,1), Computer(18,COSMAC ELF,3), Computer(19,COSMAC 
                                                  //| VIP,3), Computer(20,ELF II,4), Computer(21,Macintosh,1), Computer(22,Macinto
                                                  //| sh II,0), Computer(23,Macintosh Plus,1), Computer(24,Macintosh IIfx,0), Comp
                                                  //| uter(25,iMac,1), Computer(26,Mac Mini,1), Computer(27,Mac Pro,1), Computer(2
                                                  //| 8,Power Macintosh,1), Computer(29,PowerBook,1), Computer(30,Xserve,0), Compu
                                                  //| ter(31,Powerbook 100,0), Computer(32,Powerbook 140,0), Computer(33,Powerbook
                                                  //|  170,0), Computer(34,Pow
                                                  //| Output exceeds cutoff limit.
  computers.flatMap(
    c => companies.filter( co => c.manufacturerId == co.id )
            .map( co => (c,co) )
  ) : Seq[(Computer,Company)]                     //> res6: Seq[(Computer, Company)] = Vector((Computer(1,MacBook Pro 15.4 inch,1
                                                  //| ),Company(1,Apple Inc.)), (Computer(2,CM-2a,2),Company(2,Thinking Machines)
                                                  //| ), (Computer(3,CM-200,2),Company(2,Thinking Machines)), (Computer(4,CM-5e,2
                                                  //| ),Company(2,Thinking Machines)), (Computer(5,CM-5,2),Company(2,Thinking Mac
                                                  //| hines)), (Computer(6,MacBook Pro,1),Company(1,Apple Inc.)), (Computer(7,App
                                                  //| le IIe,1),Company(1,Apple Inc.)), (Computer(8,Apple IIc,1),Company(1,Apple 
                                                  //| Inc.)), (Computer(9,Apple IIGS,1),Company(1,Apple Inc.)), (Computer(10,Appl
                                                  //| e IIc Plus,1),Company(1,Apple Inc.)), (Computer(11,Apple II Plus,1),Company
                                                  //| (1,Apple Inc.)), (Computer(12,Apple III,1),Company(1,Apple Inc.)), (Compute
                                                  //| r(13,Apple Lisa,1),Company(1,Apple Inc.)), (Computer(14,CM-2,2),Company(2,T
                                                  //| hinking Machines)), (Computer(15,Connection Machine,2),Company(2,Thinking M
                                                  //| achines)), (Computer(16,Apple II,1),Company(1,Apple Inc.)), (Computer(17,Ap
                                                  //| ple III Plus,1),Company
                                                  //| Output exceeds cutoff limit.

  println("Welcome to the Scala worksheet")       //> Welcome to the Scala worksheet
}