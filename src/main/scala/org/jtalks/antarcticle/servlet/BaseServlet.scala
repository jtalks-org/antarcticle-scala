package org.jtalks.antarcticle.servlet

import org.scalatra._
import scalate.ScalateSupport
import org.fusesource.scalate.{ TemplateEngine, Binding }
import org.fusesource.scalate.layout.DefaultLayoutStrategy

trait BaseServlet extends ScalatraServlet with ScalateSupport {
  before() {
    contentType = "text/html"
  }

  override protected def defaultTemplatePath: List[String] = List("/WEB-INF/templates/views")

  override protected def createTemplateEngine(config: ConfigT) = {
    val engine = super.createTemplateEngine(config)
    engine.layoutStrategy = new DefaultLayoutStrategy(engine,
      TemplateEngine.templateTypes.map("/WEB-INF/templates/layouts/default." + _): _*)
    engine.packagePrefix = "templates"
    engine.importStatements ++= Seq("import org.jtalks.antarcticle.persistence._")
    engine
  }
}
