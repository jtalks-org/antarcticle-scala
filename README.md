Antarcticle-scala
=================

Antarcticle is an article engine for [JTalks](http://jtalks.org/) community project.
Please report all the bugs and feature requests [here](http://jira.jtalks.org/browse/ANTARCTICLE).

##Installation

 The following two deployment alternatives are supported:
 
### Native Play Framework deployment

1. Clone this repository to get the latest source code
2. Install [Java 1.7](http://www.oracle.com/technetwork/java/javase/downloads/java-se-jre-7-download-432155.html), [Scala 2.10](http://www.scala-lang.org/download/2.10.3.html) and [Play Framework 2.2](http://downloads.typesafe.com/play/2.2.0/play-2.2.0.zip)
3. Navigate to clonned source code root and try ```play``` console command. It should open Play console if evething has been installed in a correct way
4. Perform application configuration via **conf/application.conf** as described in **Configuration** section below
5. Excecute ```play run <port>``` command to launch an application

### Traditional war-file deployment

War file may be deployed to any servlet container or application server on your choice (e.g. Apache Tomcat). You can download an assemnled war from our [repository](http://repo.jtalks.org/content/repositories/deployment-pipeline/deployment-pipeline/antarcticle/) (usually you'd want the latest version) or build one yourself from the source code.

####Building war file from a source code (optional)

1. Clone this repository to get the latest source code
2. Install [SBT 0.13](http://www.scala-sbt.org/0.13.0/docs/Getting-Started/Setup)
3. Navigate to root project folder and execute the following to create an application deployment archive: ```sbt war``` 
4. Find assembled war at **/target/antarcticle-scala-X.X.war**

####Installing the war file

1. First, proceed to **configuration** section to prepare the application environment. For war-based deployment model application configuration should be performed via JNDI. Consult your server documentation on how to set JNDI properties fro an application. You can find Tomcat configuration below in Examples section.
2. When done with configuration deploy WAR file to the servlet container and enjoy application running   

##Configuration

The following sections describe Antarcticle configuration properties. They can be set either in  **conf/application.conf** application configuration file or via JNDI, if you're running Antarcticle in JNDI-aware environment. The later way is preferable for Tomcat or other servlet container. Properties' names are different for JNDI and file configurations.

**application.conf** is a simple key-value property file, **#** symbol used for comments. It may be edited either before build procedure in source or after WAR deployment. All changes in this file require application restart.

###Database

The following databases are supported:

* H2
* MySQL
* PostgreSQL

SQLite, Microsoft Access and Derby will also probably work, but we haven't tested them.

When creating database in MySQL you need to use utf_bin collation like this:
`create database antarcticle character set utf8 collate utf8_bin`

To configure database connection use the following properties:

application.conf:

    db.default.driver=com.mysql.jdbc.Driver
    db.default.url="jdbc:mysql://localhost:3306/antarcticle?characterEncoding=UTF-8"
    db.default.user=root
    db.default.password=root

JNDI properties:

    <Environment name="ANTARCTICLE_DB_DRIVER" value="com.mysql.jdbc.Driver" type="java.lang.String"/>
    <Environment name="ANTARCTICLE_DB_URL" value="jdbc:mysql://localhost:3306/antarcticle?characterEncoding=UTF-8" type="java.lang.String"/>
    <Environment name="ANTARCTICLE_DB_USER" value="root" type="java.lang.String"/>
    <Environment name="ANTARCTICLE_DB_PASSWORD" value="root" type="java.lang.String"/>

Where driver can be selected from the [available drivers](http://slick.typesafe.com/doc/2.0.0/api/#scala.slick.driver.JdbcDriver), URL is represented in default JDBC notation and _antarcticle_ stands for a database name. Don't forget to actualy create this database before launching Antarcticle application. On the first launch application will create all necessary tables and data on it's own.

It's also possible to perform a data migration from an [old Antarticle version](https://github.com/jtalks-org/antarcticle). See [migration script](https://github.com/jtalks-org/antarcticle-scala/blob/master/databaseMigration.sql) for detailed instructions on how to perform database migration.

###Authentication

Authentication is performed via [JTalks Poulpe](https://github.com/jtalks-org/poulpe) instance which is an authentication service or using fake internal authenticator. The latter is mostly intended for testing purposes and should not be be used in production.

To setup antarcticle to use Poulpe ensure the following properties are set:

application.conf:

    security.authentication.useFake=false
    security.authentication.poulpe.url="http://mydomain.com/poulpeContext"
    
JNDI properties:

    <Environment name="ANTARCTICLE_USE_FAKE_AUTHENTICATION" value="false" type="java.lang.Boolean"/>
    <Environment name="ANTARCTICLE_POULPE_URL" value="http://mydomain.com/poulpeContext" type="java.lang.String"/>  

To configure fake authentication manager (contains only admin/admin user) set properties as follows:

application.conf:

    security.authentication.useFake=true

JNDI properties:

    <Environment name="ANTARCTICLE_USE_FAKE_AUTHENTICATION" value="true" type="java.lang.Boolean"/>


###Examples

The following sample illustrates JNDI-based configuration for Apache Tomcat 6-7 in **context.xml** configuration file:

```xml
<?xml version='1.0' encoding='utf-8'?>
<Context>
    <WatchedResource>WEB-INF/web.xml</WatchedResource>
    <Environment name="ANTARCTICLE_DB_DRIVER" 
         value="com.mysql.jdbc.Driver"
         type="java.lang.String"/>
    <Environment name="ANTARCTICLE_DB_URL" 
         value="jdbc:mysql://localhost:3306/antarcticle?characterEncoding=UTF-8"
         type="java.lang.String"/>
    <Environment name="ANTARCTICLE_DB_USER" 
         value="root"
         type="java.lang.String"/>
    <Environment name="ANTARCTICLE_DB_PASSWORD" 
         value="root"
         type="java.lang.String"/>
    <Environment name="ANTARCTICLE_USE_FAKE_AUTHENTICATION" 
         value="false"
         type="java.lang.Boolean"/>
    <Environment name="ANTARCTICLE_POULPE_URL" 
         value="http://mydomain.com/poulpeContext"
         type="java.lang.String"/>    
    <Environment name="ANTARCTICLE_SMTP_HOST" 
         value="smtp.mail.ru"
         type="java.lang.String"/>
    <Environment name="ANTARCTICLE_SMTP_PORT" 
         value="465"
         type="java.lang.String"/>
    <Environment name="ANTARCTICLE_SMTP_USER" 
         value="your_username"
         type="java.lang.String"/>
    <Environment name="ANTARCTICLE_SMTP_PASSWORD" 
         value="your_password"
         type="java.lang.String"/>
    <Environment name="ANTARCTICLE_SMTP_AUTH" 
         value="true"
         type="java.lang.Boolean"/>
    <Environment name="ANTARCTICLE_SMTP_SSL" 
         value="true"
         type="java.lang.String"/>
</Context>
```

##Logging
For logging logback library is used. Sample of logging config logger.xml could be found in project/conf folder.
By default logback writes logs to $USER_HOME/logs/application.log file and STDOUT with DEBUG log level 

In order to use custom logger.xml file on Tomcat you need to logger.file java system property like this:

On Windows:
    
    set JAVA_OPTS=-Dlogger.file=c:/logger.xml
    
On Unix:
    
    export JAVA_OPTS=-Dlogger.file=/opt/prod/logger.xml
    

##Development

It's possible to generate project files for Intellij Idea with ```sbt idea``` command. For Eclipse the same can be archived with a separate [plugin](https://github.com/typesafehub/sbteclipse).

To get unit test coverage report run ```sbt clean scoverage:test```

###Necessary software
- IDE or text editor on your choice
- GIT client
- JDK 1.7
- Scala 2.10
- SBT 0.13
- Play Framework 2.2 

###Useful links
- Bug tracker: [http://jira.jtalks.org/browse/ANTARCTICLE](http://jira.jtalks.org/browse/ANTARCTICLE)
- Continuous Integration server : [http://ci.jtalks.org/view/Antarcticle](http://ci.jtalks.org/view/Antarcticle/)  
- Test application instance: [http://qa.jtalks.org/antarcticle](http://qa.jtalks.org/antarcticle/)
