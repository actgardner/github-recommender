github-recommender
==================

Recommend new repositories to GitHub users, using collaborative filtering. User preferences are implicit - rather than using stars, we use commits and issues logged. Each project is only considered once - commits to a fork count towards a user's preference for the "parent" repository instead. This reduces the space of repositories to recommend, and hopefully refers users to more active repositories. It also prevents users being recommended projects they've already forked. 

The current collaborative filtering approach uses binary preferences for user,repo pairs. This yields useful, but somewhat predictable results for most users. Using the number of commits and issues provides more interesting recommendations, but many users receive no recommendations. A good compromise may be taking the log(N+1) of user activities, but this was not tested due to lack of time.

Another possibility is to use ALS to factor the matrix, and generate the predicted user-item scores at query time. 

Example
----

Visit http://recommend.batsignal.io to try it out! You need to have lots of public commits for it to come up with recommendations.

Usage
----

- Get the MySQL dump of users, projects and commits from http://ghtorrent.org/downloads.html
- Configure MySQL with a db, user and password (defaults are all 'github')
- Install all the Python dependencies from `backend/requirements.txt`
- Run `mysql/create_tables.sql` to setup table definitions
- Run `scripts/prepare_data.py` to collapse all the fork trees, create the preferences table
- Run `mysql/download_prefs.sql` to save it to disk

At this point you have a CSV file at `/tmp/userprefs.csv` with user_id, project_id, weight preferences to do some ML on.

For the example here, I used Mahout's Hadoop recommender implementation. This works on Cloudera's CDH5 out of the box:

- Create a new directory structure on Hadoop: `hadoop fs -mkdir /github; hadoop fs -mkdir /github/csv`
- Upload the CSV file: `hadoop fs -put /tmp/userprefs.csv /github/csv`
- Run the Mahout recommender: `mahout recommenditembased --input /github/csv --output /github/recommend --minPrefsPerUser 0 --maxPrefsPerUser 1000 -s SIMILARITY_LOGLIKELIHOOD --booleanData true`
- Pull the results directory down: `hadoop fs -get /github/recommend`
- Run `backend/parse_recommendation.py` to produce a CSV from the weird format Mahout produces

As stated above, this Mahout invocation optimizes for the most users receiving recommendations - removing the booleanData and using other recommenders yields more interesting recommendations for the subset of users who are very active.

If you didn't use Mahout, you're on your own for getting a CSV of user, repo, weight recommendations.

- Load the CSV into MySQL: `LOAD DATA INFILE 'recommendations.csv' INTO TABLE recommendations FIELDS TERMINATED BY ',';`

At this point you can deploy the web server by running `python backend/web_ui.py`. 

- Edit `web/index.html` and set BACKEND_SERVER to point to the backend web server. 
- Upload the contents of the web directory to S3 or another static web host.

And you're done. Feel free to substitute your own, better collaborative filtering approach and reuse the web UI. 
