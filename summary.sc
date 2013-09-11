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

object summary {
  // worksheet Hello World
  println("Hello World")                          //> Hello World
  
  // connections
  val db = Database.forURL("jdbc:h2:mem:test", driver = "org.h2.Driver")
                                                  //> db  : setup.driver.backend.DatabaseDef = scala.slick.jdbc.JdbcBackend$Databa
                                                  //| seFactoryDef$$anon$5@5812f9ee
  
  implicit val session = db.createSession // <- needs to be closed manually (see end of file). Better use db.withTransaction instead!
                                                  //> session  : setup.driver.backend.Session = scala.slick.jdbc.JdbcBackend$BaseS
                                                  //| ession@49e808ca

  sqlu"create table COMPANY ( ID INT, NAME VARCHAR(255) )".first
                                                  //> res0: Int = 0
  sqlu"""insert into COMPANY (ID,NAME) values (1,'Typesafe')""".first
                                                  //> res1: Int = 1
  sql"""select * from COMPANY""".as[(Int,String)].list
                                                  //> res2: List[(Int, String)] = List((1,Typesafe))
  sql"""select * from COMPANY""".as[Company].list //> res3: List[Company] = List(Company(1,Typesafe))
  
	  
	// plain SQL, using interpolation for automatic escaping
  val id = 1                                      //> id  : Int = 1
  sql"select * from COMPANY co WHERE co.ID = $id".as[Company].first
                                                  //> res4: Company = Company(1,Typesafe)

  initDb // creates tables with sample data
  
  sql"select * from COMPUTER c,COMPANY co where c.MANUFACTURER_ID = co.ID and c.ID = 1".as[(Computer,Company)].first
                                                  //> res5: (Computer, Company) = (Computer(1,MacBook Pro 15.4 inch,1),Company(1,
                                                  //| Apple Inc.))
  
  // type-safe, collection-like queries
  Companies.take(5).run.foreach( println )        //> Company(1,Apple Inc.)
                                                  //| Company(2,Thinking Machines)
                                                  //| Company(3,RCA)
                                                  //| Company(4,Netronics)
                                                  //| Company(5,Tandy Corporation)
  
  Companies.filter(_.id === 1).first              //> res6: setup.Companies#TableElementType = Company(1,Apple Inc.)
  
  // join
  (for( c <- Computers; co <- Companies; if c.manufacturerId === co.id && c.id === 1 ) yield (c,co)).first
                                                  //> res7: (Computer, Company) = (Computer(1,MacBook Pro 15.4 inch,1),Company(1,
                                                  //| Apple Inc.))

	// join as method calls
  Computers.filter(_.id === 1).flatMap(
    c => Companies.filter( c.manufacturerId === _.id )
            .map( co => (c,co) )
  ).first                                         //> res8: (Computer, Company) = (Computer(1,MacBook Pro 15.4 inch,1),Company(1,
                                                  //| Apple Inc.))

  // re-usable join condition
  def joinComputerCompany( c:Computers, co:Companies ) = c.manufacturerId === co.id
                                                  //> joinComputerCompany: (c: setup.Computers, co: setup.Companies)scala.slick.l
                                                  //| ifted.Column[Boolean]
  Computers.filter(_.id === 1).flatMap(
    c => Companies.filter( joinComputerCompany(c, _) )
            .map( co => (c,co) )
  ).first                                         //> res9: (Computer, Company) = (Computer(1,MacBook Pro 15.4 inch,1),Company(1,
                                                  //| Apple Inc.))
  
  // re-usable join
  def joinComputersCompanies( computers:Query[Computers,Computer] = Computers, companies:Query[Companies,Company] = Companies ) =
	  computers.flatMap(
	    c => companies.filter( joinComputerCompany(c,_) )
	            .map( co => (c,co) )
	  )                                       //> joinComputersCompanies: (computers: setup.driver.simple.Query[setup.Compute
                                                  //| rs,Computer], companies: setup.driver.simple.Query[setup.Companies,Company]
                                                  //| )scala.slick.lifted.Query[(setup.Computers, setup.Companies),(Computer, Com
                                                  //| pany)]

	joinComputersCompanies( Computers.filter(_.id === 1), Companies ).run.foreach( println )
                                                  //> (Computer(1,MacBook Pro 15.4 inch,1),Company(1,Apple Inc.))
  // re-usable join as method extension
	implicit def computersWithCompanies( computers:Query[Computers,Computer] ) = new{
	  def joinCompanies( companies:Query[Companies,Company] = Companies ) = computers.flatMap( c =>
	    companies.filter( joinComputerCompany(c,_) )
	             .map( (c,_) )
	  )
	}                                         //> computersWithCompanies: (computers: setup.driver.simple.Query[setup.Compute
                                                  //| rs,Computer])AnyRef{def joinCompanies(companies: setup.driver.simple.Query[
                                                  //| setup.Companies,Company]): scala.slick.lifted.Query[(setup.Computers, setup
                                                  //| .Companies),(Computer, Company)]; def joinCompanies$default$1: setup.driver
                                                  //| .simple.Query[setup.Companies,Company] @scala.annotation.unchecked.unchecke
                                                  //| dVariance}
    
  // Computers with Mac in their name not built by Apple
  Computers.where(_.name like "%Mac%").joinCompanies( Companies.filter(c => !(c.name like "%Apple%")) ).run.foreach( println )
                                                  //> (Computer(15,Connection Machine,2),Company(2,Thinking Machines))
                                                  //| (Computer(454,Commodore MAX Machine,6),Company(6,Commodore International))
  
  // re-usable pagination function
  def paginate[T,E]( page: Int, size: Int, query:Query[T,E] ) = query.drop( (page-1)*size ).take( size )
                                                  //> paginate: [T, E](page: Int, size: Int, query: setup.driver.simple.Query[T,E
                                                  //| ])scala.slick.lifted.Query[T,E]

  // re-usable pagination as method extension
  implicit def queryPaginationExtension[T,E]( q:Query[T,E] ) = new{
	  def paginate( page:Int, size:Int ) = q.drop( (page-1)*size ).take( size )
	}                                         //> queryPaginationExtension: [T, E](q: setup.driver.simple.Query[T,E])AnyRef{d
                                                  //| ef paginate(page: Int,size: Int): scala.slick.lifted.Query[T,E]}
	
  paginate( 2, 3, Computers.joinCompanies( Companies.filter(_.id === 1 ) ) ).run.foreach( println )
                                                  //> (Computer(8,Apple IIc,1),Company(1,Apple Inc.))
                                                  //| (Computer(9,Apple IIGS,1),Company(1,Apple Inc.))
                                                  //| (Computer(10,Apple IIc Plus,1),Company(1,Apple Inc.))

  Computers.joinCompanies( Companies.filter(_.id === 1 ) ).paginate(2,3).run.foreach( println )
                                                  //> (Computer(8,Apple IIc,1),Company(1,Apple Inc.))
                                                  //| (Computer(9,Apple IIGS,1),Company(1,Apple Inc.))
                                                  //| (Computer(10,Apple IIc Plus,1),Company(1,Apple Inc.))
  
  // local join
  {
    def joinComputerCompany( c:Computer, co:Company ) = c.manufacturerId == co.id
	  val companies = Companies.run
	  val computers = Computers.run
	  computers.flatMap(
	    c => companies.filter( joinComputerCompany(c,_) )
	            .map( co => (c,co) )
	  ).take(5).foreach( println )
	}                                         //> (Computer(1,MacBook Pro 15.4 inch,1),Company(1,Apple Inc.))
                                                  //| (Computer(2,CM-2a,2),Company(2,Thinking Machines))
                                                  //| (Computer(3,CM-200,2),Company(2,Thinking Machines))
                                                  //| (Computer(4,CM-5e,2),Company(2,Thinking Machines))
                                                  //| (Computer(5,CM-5,2),Company(2,Thinking Machines))

  session.close
}