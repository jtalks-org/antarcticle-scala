Antarcticle-scala
=================

Antarcticle is an article engine for [JTalks](http://jtalks.org/) community project.

##Installation
At the moment the only way to get Antarticle executable is to build it from the source code. So, start

##Configuration
###Database

The following databases are supported:

* H2
* MySQL
* PostgreSQL

it will also probably work with SQLite, Microsoft Access and Derby, but we haven't tested them.

It's possible to perform a data migration from an [old Antarticle version](https://github.com/jtalks-org/antarcticle). See [migration script](https://github.com/jtalks-org/antarcticle-scala/blob/master/databaseMigration.sql) for detailed instructions on how to perform database migration.

###Authentication

TBD

##Development

- Continious Integration server : [http://ci.jtalks.org/view/Antarcticle](http://ci.jtalks.org/view/Antarcticle/)  
- Test application instance: [http://qa.jtalks.org/antarcticle](http://qa.jtalks.org/antarcticle/)
- Staging application instance: [http://preprod.jtalks.org/antarcticle](http://preprod.jtalks.org/antarcticle)
