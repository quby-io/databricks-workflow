package com.quby.demo.databricks.ml

import com.quby.demo.databricks.schema.UserRatingSchema
import com.quby.demo.databricks.util.SparkActiveSession
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.recommendation.{ALS, ALSModel}
import org.apache.spark.sql.Dataset

object RecommendationSystem extends SparkActiveSession {

  val ranks     = List(5)
  val regParams = List(0.01, 0.5, 5, 10)
  val numIters  = List(1,3)
  var bestModel: Option[ALSModel] = None
  var optimalRMSE  = Double.MaxValue
  var bestRank     = 0
  var bestRegParam = -1.0
  var bestNumIter  = -1

  def train(trainingData: Dataset[UserRatingSchema]):ALSModel = {
    import spark.implicits._

    val Array(training, test) = trainingData
      .select('userId, 'itemId, 'rating)
      .randomSplit(Array(0.8, 0.2))
    val als = new ALS()
      .setMaxIter(5)
      .setRank(ranks.indexOf(0))
      .setUserCol("userId")
      .setItemCol("itemId")
      .setRatingCol("rating")
      .setImplicitPrefs(true)

    val model = als.fit(training)
    // Evaluate the model by computing the RMSE on the test data
    val predictions = model.transform(test)
    val evaluator = new RegressionEvaluator()
      .setMetricName("rmse")
      .setLabelCol("rating")
      .setPredictionCol("prediction")

    //save it on Mlfolow
    val currentRMSE = evaluator.evaluate(predictions)

    model
  }

}
