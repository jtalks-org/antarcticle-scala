import org.jtalks.antarcticle.persistence._
import org.jtalks.antarcticle.persistence.repositories.SlickArticlesRepositoryComponent
import org.jtalks.antarcticle.persistence.schema.{User, Article}
import org.jtalks.antarcticle.servlet.{UsersServlet, ArticlesServlet}
import org.jtalks.antarcticle.persistence.DatabaseProvider
import org.scalatra._
import javax.servlet.ServletContext
import com.mchange.v2.c3p0.ComboPooledDataSource
import org.slf4j.LoggerFactory
import scala.slick.driver.{H2Driver, ExtendedProfile}
import scala.slick.session.Database
import java.sql.Timestamp

object DAL
  extends SlickArticlesRepositoryComponent
  with ProductionDatabase
  with Schema {

  import profile.simple._
  def createDb = {
    db withSession { implicit session: Session =>
      schema.create
    }
  }
}

trait ProductionDatabase extends DatabaseProvider with Profile {
  private val logger = LoggerFactory.getLogger(getClass)

  private val dataSource = new ComboPooledDataSource
  logger.info("Created c3p0 connection pool")

  val db = Database.forDataSource(dataSource)
  val profile = H2Driver

  override def close = {
    logger.info("Closing c3p0 connection pool")
    dataSource.close
  }
}


class ScalatraBootstrap extends LifeCycle {
  def createData {
    import DAL._
    import DAL.profile.simple._

    DAL.createDb

    val time: Timestamp = new Timestamp(new java.util.Date().getTime())
    db withSession { implicit session: Session =>
      Users.insertAll(
        User(None, "user1"),
        User(None, "user2")
      )

      Articles.insertAll(
        Article(None, "New title", "<b>content</b>", time, time, "description1", 1),
        Article(None, "New title 2", "<i>html text</i>", time, time, "description2", 2)
      )
    }
  }

  override def init(context: ServletContext) {
    context.mount(new ArticlesServlet(DAL), "/*")
    context.mount(new UsersServlet, "/*")
  }

  override def destroy(context: ServletContext) {
    super.destroy(context)
    DAL.close
  }
}
