import org.jtalks.antarcticle.persistence._
import org.jtalks.antarcticle.persistence.repositories.SlickArticlesRepositoryComponent
import org.jtalks.antarcticle.servlet.{UsersServlet, ArticlesServlet}
import org.jtalks.antarcticle.persistence.DatabaseProvider
import org.scalatra._
import javax.servlet.ServletContext
import com.mchange.v2.c3p0.ComboPooledDataSource
import org.slf4j.LoggerFactory
import scala.slick.driver.{H2Driver, ExtendedProfile}
import scala.slick.session.Database
import java.sql.Timestamp

class DAL(override val profile: ExtendedProfile, override val db: Database)
  extends SlickArticlesRepositoryComponent
  with UsersComponent
  with ArticlesComponent
  with DatabaseProvider
  with Profile {

  import profile.simple._
  def createDb = {
    db withSession { implicit session: Session =>
      (Articles.ddl ++ Users.ddl).create
    }
  }
}


class ScalatraBootstrap extends LifeCycle {
  var logger = LoggerFactory.getLogger(getClass)

  val dataSource = new ComboPooledDataSource
  logger.info("Created c3p0 connection pool")

  def createData(dal: DAL) {
    import dal._
    import dal.profile.simple._

    dal.createDb

    db withSession { implicit session: Session =>
      Users.insertAll(
        User(None, "user1"),
        User(None, "user2")
      )

      Articles.insertAll(
        Article(None, "New title", "<b>content</b>", new Timestamp(new java.util.Date().getTime()), 1),
        Article(None, "New title 2", "<i>html text</i>", new Timestamp(new java.util.Date().getTime()), 2)
      )
    }
  }

  override def init(context: ServletContext) {
    val db = Database.forDataSource(dataSource)
    val dal = new DAL(H2Driver, db)

    createData(dal)

    context.mount(new ArticlesServlet(dal), "/*")
    context.mount(new UsersServlet, "/*")
  }

  override def destroy(context: ServletContext) {
    super.destroy(context)
    closeDbConnection()
  }

  private def closeDbConnection() {
     logger.info("Closing c3p0 connection pool")
     dataSource.close
  }
}
