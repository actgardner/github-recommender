name := "GithubRecommender"

version := "1.0"

mainClass in (Compile, run):= Some("me.agardner.github.recommender.ALSRecommender")

libraryDependencies += "org.apache.spark" %% "spark-mllib" % "0.9.1"

