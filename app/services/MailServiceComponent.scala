package services

import java.util.{Date, Properties}
import javax.mail._
import javax.mail.internet.{InternetAddress, MimeMessage}

import conf.{Keys, PropertiesProviderComponent}

trait MailServiceComponent {

  val mailService: MailService

  trait MailService {
    def sendEmail(to: String, subject: String, text: String)
  }

}

trait MailServiceComponentImpl extends MailServiceComponent {
  this: PropertiesProviderComponent =>

  val mailService = new MailServiceImpl

  class MailServiceImpl extends MailService {

    val session = {
      val properties = new Properties()
      val host = propertiesProvider.get[String](Keys.MailSmtpHost).getOrElse("smtp.mail.ru")
      val port = propertiesProvider.get[String](Keys.MailSmtpPort).getOrElse("465")
      properties.put("mail.smtp.host", host)
      properties.put("mail.smtp.port", port)
      propertiesProvider.get[String](Keys.MailSmtpPort)
      propertiesProvider.get[Boolean](Keys.MailSmtpAuth).map {
        auth => {
          if (auth) {
            properties.put("mail.smtp.auth", "true")
            properties.put("mail.smtp.socketFactory.port", port)
            properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
            properties.put("mail.smtp.starttls.enable", propertiesProvider.get[String](Keys.MailSmtpSsl).getOrElse("true"))
          }
        }
      }

      Session.getInstance(properties, new Authenticator {
        override def getPasswordAuthentication: PasswordAuthentication = {
          val username = propertiesProvider.get[String](Keys.MailSmtpUser).getOrElse("jtalks@inbox.ru")
          val password = propertiesProvider.get[String](Keys.MailSmtpPassword).getOrElse("javatalks")
          new PasswordAuthentication(username, password)
        }
      })
    }

    override def sendEmail(to: String, subject: String, text: String): Unit = {
      val message = new MimeMessage(session)
      message.addHeader("Content-type", "text/HTML; charset=UTF-8")
      message.addHeader("format", "flowed")
      message.addHeader("Content-Transfer-Encoding", "8bit")
      val from = propertiesProvider.get[String](Keys.MailSmtpFrom).getOrElse("jtalks@inbox.ru")
      message.setFrom(new InternetAddress(from))
      message.addRecipients(Message.RecipientType.TO, to)
      message.setSubject(subject)
      message.setSentDate(new Date)
      message.setContent(text, "text/html")
      Transport.send(message)
    }
  }

}