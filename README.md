Antarcticle-scala
=================

Antarcticle is an article engine for [JTalks](http://jtalks.org/) community project.
Please report all the bugs and feature requests [here](http://jira.jtalks.org/Antarcticle).

##Installation
At the moment the only way to get Antarticle executable is to build it from the source code:

1. Clone this repository. You will probably need to install GIT for that.
2. Install [SBT 0.13+](http://www.scala-sbt.org/0.13.0/docs/Getting-Started/Setup).
3. Navigate to root project folder and execute the following:
```
     sbt packageBin
``` 
to create an application deployment archive (WAR).
4. Once you have the WAR file assembled proceed to **configuration** section to prepare the application environment.
5. When done with configuration deploy WAR file to the servlet container of your choice (e.g. Apache Tomcat) and enjoy application running.   

##Configuration

The following sections describe Antarcticle configuration properties. They can be set either in  **conf/application.conf** application configuration file or via JNDI, if you're running Antarcticle in JNDI-aware environment. The later way is preferable for Tomcat or other servlet container.

**Application.conf** is a simple key-value property file, **#** symbol used for comments. It may be edited either before build procedure in source or after WAR deployment. All changes in this file require application restart.

###Database

The following databases are supported:

* H2
* MySQL
* PostgreSQL

SQLite, Microsoft Access and Derby will also probably work, but we haven't tested them.
To configure database connection use the following properties:

    db.default.driver=com.mysql.jdbc.Driver
    db.default.url="jdbc:mysql://localhost:3306/antarcticle?characterEncoding=UTF-8"
    db.default.user=root
    db.default.password=root

Where driver can be selected from the [available drivers](http://slick.typesafe.com/doc/2.0.0/api/#scala.slick.driver.JdbcDriver), URL is represented in default JDBC notation and _antarcticle_ stands for a database name. Don't forget to actualy create this database before launching Antarcticle application. On the first launch application will create all necessary tables and data on it's own.

It's also possible to perform a data migration from an [old Antarticle version](https://github.com/jtalks-org/antarcticle). See [migration script](https://github.com/jtalks-org/antarcticle-scala/blob/master/databaseMigration.sql) for detailed instructions on how to perform database migration.

###Authentication

Authentication is performed via Poulpe instance or using fake internal authenticator. The later one is mostly intended for testing purposes and should not be be used in production.

To setup antarcticle for using Poulpe ensure the following properties are set:


    security.authentication.useFake=false
    security.authentication.poulpe.url=http://mydomain.com/poulpeContext

To configure fake authentication manager (contains only admin/admin user) set properties as follows:

    security.authentication.useFake=true

##Development
###Necessary software
- IDE or text editor on your choice
- GIT client
- SBT 0.13+
- Play Framework 2.0+ 

###Useful links
- Bug tracker: [http://jira.jtalks.org/Antarcticle](http://jira.jtalks.org/Antarcticle)
- Continious Integration server : [http://ci.jtalks.org/view/Antarcticle](http://ci.jtalks.org/view/Antarcticle/)  
- Test application instance: [http://qa.jtalks.org/antarcticle](http://qa.jtalks.org/antarcticle/)
- Staging application instance: [http://preprod.jtalks.org/antarcticle](http://preprod.jtalks.org/antarcticle)
