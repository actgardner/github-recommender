# Recursively find forks and make a look-up table with the original (root) repo

import MySQLdb
import string
import settings

db = MySQLdb.connect(**settings.db)
c = db.cursor()

c.execute('truncate fork_source')
c.execute('truncate commit_prefs')

for l in xrange(1,20):
    sql = 'INSERT INTO fork_source SELECT a.id, {0}.id FROM projects a'.format(string.ascii_lowercase[l-1])
    for m in xrange(1,l):
        sql += ' JOIN projects {0} ON ({0}.id={1}.forked_from)'.format(string.ascii_lowercase[m], string.ascii_lowercase[m-1])
    sql += ' WHERE {0}.forked_from IS NULL'.format(string.ascii_lowercase[l-1])
    print sql
    c.execute(sql)
db.commit()

c.execute('INSERT INTO commit_prefs SELECT author_id, parent_id, COUNT(*) FROM commits c JOIN fork_source f ON (c.project_id = f.project_id) GROUP BY author_id, parent_id;')
