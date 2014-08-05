package me.agardner.github.recommender

import org.apache.spark.mllib.recommendation.{ALS, Rating}
import org.apache.spark.{SparkContext, SparkConf}

/**
 * Created by gardner on 2014-08-01.
 */
object ALSFactorizer {
  def main(args: Array[String]) {

    val conf = new SparkConf().setAppName("Github Recommender - ALS Factorizer")
    conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    val sc = new SparkContext(conf)

    // Load and parse the data
    val data = sc.textFile(args(0)).repartition(10)
    val ratings = data.map(_.split(',') match { case Array(user, item, rate) =>
      Rating(user.toInt, item.toInt, rate.toDouble)
    })

    // Build the recommendation model using ALS
    val rank = 10
    val numIterations = 5
    val model = ALS.trainImplicit(ratings, rank, numIterations, 0.01, 1.0)
    model.productFeatures.saveAsObjectFile(args(1)+"/product")
    model.userFeatures.saveAsObjectFile(args(1)+"/user")

    val userList = ratings.map({ case Rating(user, item, rate) =>
      user
    }).distinct()
    userList.saveAsTextFile(args(1)+"/userList")
  }
}