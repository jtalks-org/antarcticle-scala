package services

import java.util.{Date, Properties}
import javax.mail._
import javax.mail.internet.{InternetAddress, MimeMessage}

import conf.{Keys, PropertiesProviderComponent}

import scala.concurrent.Future

trait MailServiceComponent {

  val mailService: MailService

  trait MailService {
    def sendEmail(to: String, subject: String, text: String): Future[Unit]
  }
}

trait MailServiceComponentImpl extends MailServiceComponent {
  this: PropertiesProviderComponent =>

  val mailService = new MailServiceImpl

  class MailServiceImpl extends MailService {

    lazy val session = {
      val properties = new Properties()
      val host = propertiesProvider.get(Keys.MailSmtpHost).get
      val port = propertiesProvider.get(Keys.MailSmtpPort).get
      properties.put("mail.smtp.host", host)
      properties.put("mail.smtp.port", port)
      propertiesProvider.get(Keys.MailSmtpAuth).map {
        auth => {
          if (auth) {
            properties.put("mail.smtp.auth", "true")
            properties.put("mail.smtp.socketFactory.port", port)
            properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
            propertiesProvider.get(Keys.MailSmtpSsl) map {
              key => properties.put("mail.smtp.starttls.enable", key)
            }
          }
        }
      }

      Session.getInstance(properties, new Authenticator {
        override def getPasswordAuthentication: PasswordAuthentication = {
          val username = propertiesProvider.get(Keys.MailSmtpUser).get
          val password = propertiesProvider.get(Keys.MailSmtpPassword).get
          new PasswordAuthentication(username, password)
        }
      })
    }

    override def sendEmail(to: String, subject: String, text: String): Future[Unit] = {
      import scala.concurrent.ExecutionContext.Implicits.global
      Future {
        val message = new MimeMessage(session)
        message.addHeader("Content-type", "text/HTML; charset=UTF-8")
        message.addHeader("format", "flowed")
        message.addHeader("Content-Transfer-Encoding", "8bit")
        val from = propertiesProvider.get(Keys.MailSmtpFrom).getOrElse(propertiesProvider.get(Keys.MailSmtpUser).get)
        message.setFrom(new InternetAddress(from))
        message.addRecipients(Message.RecipientType.TO, to)
        message.setSubject(subject)
        message.setSentDate(new Date)
        message.setContent(text, "text/html")
        Transport.send(message)
      }
    }
  }
}