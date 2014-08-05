import cherrypy
import MySQLdb
import json
import requests
import settings

db = MySQLdb.connect(**settings.db)

class GitHubRecommends(object):
    @cherrypy.expose
    def recommend(self, user):
        cherrypy.response.headers['Access-Control-Allow-Origin']='*'
        c = db.cursor()
        query = """
        SELECT url 
           FROM recommendations r
            JOIN projects p ON (r.project_id = p.id) 
            JOIN users u ON (r.user_id = u.id) WHERE u.login = %s
        ORDER BY weight DESC limit 9"""
        c.execute(query, [user])  
        recs = []
        for f in xrange(0, c.rowcount):
            row = c.fetchone()
            metadata = requests.get(row[0], auth=settings.github_creds).json()
            if 'name' in metadata:
                recs.append(
                      {
                       'name':metadata['name'], 
                       'description':metadata['description'],
                       'language':metadata['language'],
                       'owner':metadata['owner']['login'],
                       'owner_avatar':metadata['owner']['avatar_url'],
                       'owner_url':metadata['owner']['html_url'],
                       'url':metadata['html_url'],
                       'forks':metadata['forks_count'],
                       'issues':metadata['open_issues_count'],
                       'watchers':metadata['watchers_count']
                      })
        return json.dumps(recs)

cherrypy.quickstart(GitHubRecommends())
