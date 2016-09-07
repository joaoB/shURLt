package model

import play.api.Play
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.slick.DatabaseConfigProvider
import scala.concurrent.Future
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._
import scala.concurrent.ExecutionContext.Implicits.global

case class Link(id: Long, short: String, long: String)

case class LinkFormData(long: String)

object LinkForm {

  val form = Form(
    mapping(
      "long" -> nonEmptyText
    )(LinkFormData.apply)(LinkFormData.unapply)
  )
}

class LinkTableDef(tag: Tag) extends Table[Link](tag, "link") {

  def id = column[Long]("id", O.PrimaryKey,O.AutoInc)
  def short = column[String]("short")
  def long = column[String]("long")
  
  override def * =
    (id, short, long) <>(Link.tupled, Link.unapply)
}

object Links {

  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  val links = TableQuery[LinkTableDef]

  def add(link: Link): Future[String] = {
    dbConfig.db.run(links += link).map(res => "Link successfully added").recover {
      case ex: Exception => ex.getCause.getMessage
    }
  }

  def get(id: Long): Future[Option[Link]] = {
    dbConfig.db.run(links.filter(_.id === id).result.headOption)
  }

  def listAll: Future[Seq[Link]] = {
    dbConfig.db.run(links.result)
  }

}
