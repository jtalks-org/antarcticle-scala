# Database migration procedure
#
# This script is intended to migrate all the data from existing Ruby-based article application
# to it's new Play-based version. Database schemas are a bit different, so explicit migration
# code is necessary.
#
# Usage:
#
# This implementation assumes, that both databases are located within one MySQL server.
# 1. Create fresh database for new application, let's name it 'antarcticle', although you
#    can choose any name you want
# 2. Configure and launch Antarcticle application, it has database evolution engine embedded
#    and will create schema on it's own
# 3. Now it's time to switch to our new 'antarcticle' database and execute this script. It creates
#    a stored migration procedure, which can be called anytime, but only on empty database
# 4. Execute procedure with name of the old (Ruby) database as a parameter, e.g.:
#    > call migrateDatabase('old_and_ugly_production_antarcticle_database');
# 5. Refresh application page in browser and enjoy all the data migrated

DELIMITER //

CREATE PROCEDURE migrateDatabase(old_database_name VARCHAR(255))

  BEGIN

# 1st step, migrate existing data

    SET @database = old_database_name;

    SET @users_sql = concat(
        'INSERT INTO users (id, username, admin, first_name, last_name, remember_token)
        SELECT old_users.id, old_users.username, old_users.admin, old_users.first_name, old_users.last_name, old_users.remember_token
        FROM ', @database, '.users as old_users');
    PREPARE users_stmt FROM @users_sql;
    EXECUTE users_stmt;
    DEALLOCATE PREPARE users_stmt;

    SET @articles_sql = concat(
        'INSERT INTO articles (id, title, content, author_id, created_at, updated_at, description)
        SELECT (id, title, content, user_id, created_at, updated_at, content) FROM ', @database, '.articles');
    PREPARE articles_stmt FROM @articles_sql;
    EXECUTE articles_stmt;
    DEALLOCATE PREPARE articles_stmt;

    SET @comments_sql = concat(
        'INSERT INTO comments (id, user_id, article_id, content, created_at, updated_at)
        SELECT * FROM ', @database, '.comments');
    PREPARE comments_stmt FROM @comments_sql;
    EXECUTE comments_stmt;
    DEALLOCATE PREPARE comments_stmt;

    SET @tags_sql = concat(
        'INSERT INTO tags (id, name) SELECT * FROM ', @database, '.tags');
    PREPARE tags_stmt FROM @tags_sql;
    EXECUTE tags_stmt;
    DEALLOCATE PREPARE tags_stmt;

    SET @articles_tags_sql = concat(
        'INSERT INTO articles_tags (article_id, tag_id)
        SELECT old_tags.taggable_id, old_tags.tag_id
        FROM ', @database, '.taggings as old_tags');
    PREPARE articles_tags_stmt FROM @articles_tags_sql;
    EXECUTE articles_tags_stmt;
    DEALLOCATE PREPARE articles_tags_stmt;

# 2nd step, correct errors

    UPDATE articles
    SET content = REPLACE(content,
                          '(https://www.youtube.com/watch?feature=player_embedded&v=aMQJnigGvfY/0.jpg)]', '(https://img.youtube.com/vi/aMQJnigGvfY/0.jpg)]')
    WHERE content LIKE '%(https://www.youtube.com/watch?feature=player_embedded&v=aMQJnigGvfY/0.jpg)]%';

    UPDATE comments
    SET content = REPLACE(content, '(https://www.youtube.com/watch?feature=player_embedded&v=aMQJnigGvfY/0.jpg)]', '(https://img.youtube.com/vi/aMQJnigGvfY/0.jpg)]')
    WHERE content LIKE '%(https://www.youtube.com/watch?feature=player_embedded&v=aMQJnigGvfY/0.jpg)]%';

    UPDATE articles
    SET content = REPLACE(content, '```java', '``` XML ')
    WHERE id=20;

    UPDATE articles
    SET content = REPLACE(content, '```\r\ns', '```no-highlight\r\ns')
    WHERE id=12;

    UPDATE articles
    SET content = REPLACE(content, '\r\n1.', '\r\n\r\n1.')
    WHERE id=6 OR id=3 OR id=2;

    UPDATE articles
    SET content = REPLACE(content, 'Но прозорливый читатель', '\r\nНо прозорливый читатель')
    WHERE id=2;
END
//

DELIMITER ;