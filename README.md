github-recommender
==================

Recommend new repositories to GitHub users based on commit activity

Example
----

Visit http://recommend.batsignal.io to try it out!

Usage
----

- Get the MySQL dump of users, projects and commits from http://ghtorrent.org/downloads.html
- Configure MySQL with a db, user and password (defaults are all 'github')
- Install all the Python dependencies from `backend/requirements.txt`
- Run `mysql/create_tables.sql` to setup table definitions
- Run `scripts/prepare_data.py` to collapse all the fork trees, create the preferences table and save it to disk

At this point you have a CSV file with user_id, project_id, weight preferences to do some ML on.

For the example here, I used Mahout's Hadoop recommender implementation. This works on Cloudera's CDH5 out of the box:

- Create a new directory structure on Hadoop: `hadoop fs -mkdir /github; hadoop fs -mkdir /github/csv`
- Upload the CSV file: `hadoop fs -put recommendations.csv /github/csv`
- Run the first phase: `mahout recommenditembased --input /github/csv --output /github/recommend --minPrefsPerUser 1 --maxPrefsPerUser 100 -s SIMILARITY_PEARSON_CORRELATION --maxSimilarItemsPerItem 10000 --maxPrefsPerUserInItemSimilarity 10000 --maxPrefsPerUser 10000`
- Pull the results directory down: `hadoop fs -get /github/recommend`
- Run `backend/parse_recommendation.py` to produce a CSV from the weird format Mahout produces

Right now the Mahout recommender will give fewer than 10 items per user if they haven't committed enough. There's also an experimental Spark recommender using ALS, which is slower but should give some recommendations for everyone. It's a Scala project in GitHubRecommender.

If you didn't use Mahout, you're on your own for getting a CSV of user, repo, weight recommendations.

- Load the CSV into MySQL: `LOAD DATA INFILE 'recommendations.csv' INTO TABLE recommendations FIELDS TERMINATED BY ',';`

At this point you can deploy the web server by running `python backend/web_ui.py`. 

- Edit `web/index.html` and set BACKEND_SERVER to point to the backend web server. 
- Upload the contents of the web directory to S3 or another static web host.

And you're done. Feel free to substitute your own, better collaborative filtering approach and reuse the web UI. 
