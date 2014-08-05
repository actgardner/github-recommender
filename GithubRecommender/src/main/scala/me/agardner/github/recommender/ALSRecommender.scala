package me.agardner.github.recommender

import java.io.{File, PrintWriter}

import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import org.apache.spark.mllib.recommendation.Rating
import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD

import com.esotericsoftware.kryo.Kryo
import org.apache.spark.serializer.KryoRegistrator

import scala.io.Source

class MyRegistrator extends KryoRegistrator {
  override def registerClasses(kryo: Kryo) {
    kryo.register(classOf[me.agardner.github.recommender.ALSRecommendUser] )
  }
}

class ALSRecommendUser(sourcePath:String, modelPath:String, targetPath:String) extends Function1[Int,Unit] with Serializable{

  override def apply(x:Int) {

  }
}

/**
 * Created by gardner on 2014-07-31.
 */
object ALSRecommender {
  def main(args: Array[String]) {
    val sourcePath = args(0)
    val modelPath = args(1)
    val targetPath = args(2)
    val conf = new SparkConf().setAppName("Github Recommender - User Recommender")
    //conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    //conf.set("spark.kryo.registrator", "me.agardner.github.recommender.MyRegistrator")
    conf.set("spark.master", "spark://d1.hadoop.cto.pythian.com:7077")
    conf.set("spark.executor.memory", "30g")
    val sc = new SparkContext(conf)
    val userMatrix: RDD[(Int, Array[Double])] = sc.objectFile(modelPath + "/user")
    val itemMatrix: RDD[(Int, Array[Double])] = sc.objectFile(modelPath + "/product")
    val data = sc.textFile(sourcePath)

    val ratings = data.map(_.split(',') match { case Array(user, item, rate) =>
      Rating(user.toInt, item.toInt, rate.toDouble)
    })

    // Build the recommendation model using ALS
    val rank = 10
    val model = new MatrixFactorizationModel(rank, userMatrix, itemMatrix)

    val products = ratings.map { case Rating(user, product, rate) =>
      product
    }.distinct()

    val usersProducts = ratings.map { case Rating(user, product, rate) =>
      (user, product)
    }
    val userPrefs = new PrintWriter(new File(args(2)))
    for (line <- Source.fromFile(args(3)).getLines())  {
      val x = line.toInt
      val tuples = sc.parallelize(Array(x)).cartesian(products).subtract(usersProducts).repartition(6)
      System.out.println("Tuples: " + tuples.count())
      val recommendations = model.predict(tuples).map { case x:Rating =>
        (x.rating, (x.user,x.product))
      }.takeOrdered(20)
      for (r <- recommendations){
        userPrefs.append(r._2._1+","+r._2._2+","+r._1+"\n")
      }
    }
    userPrefs.close()
  }
}
