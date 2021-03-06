package org.template.similarproduct

import io.prediction.controller.LServing

import breeze.stats.mean
import breeze.stats.meanAndVariance
import breeze.stats.MeanAndVariance

class Serving
  extends LServing[Query, PredictedResult] {

  override def serve(query: Query,
                     predictedResults: Seq[PredictedResult]): PredictedResult = {

    // MODFIED
    val standard: Seq[Array[ItemScore]] = if (query.num == 1) {
      // if query 1 item, don't standardize
      predictedResults.map(_.itemScores)
    } else {
      // Standardize the score before combine
      val mvList: Seq[MeanAndVariance] = predictedResults.map { pr =>
        meanAndVariance(pr.itemScores.map(_.score))
      }

      predictedResults.zipWithIndex
        .map {
          case (pr, i) =>
            pr.itemScores.map { is =>
              // standardize score (z-score)
              // if standard deviation is 0 (when all items have the same score,
              // meaning all items are ranked equally), return 0.
              val score = if (mvList(i).stdDev == 0) {
                0
              } else {
                (is.score - mvList(i).mean) / mvList(i).stdDev
              }

              is.copy(score = score)
            }
        }
    }

    // sum the standardized score if same item
    val combined = standard.flatten // Array of ItemScore
      .groupBy(_.item) // groupBy item id
      .mapValues(itemScores => (itemScores.map(_.score).reduce(_ + _), itemScores(0))) //Add tuple (score, ItemScore) instead of just score value
      .toArray // array of (item id, score)
      .sortBy(_._2._1)(Ordering.Double.reverse) //Order by score value
      .take(query.num)
      .map { case (k, (d, is)) => is.copy(score = d) } //Create resulting ItemScore with proper score value

    new PredictedResult(combined)
  }
}
