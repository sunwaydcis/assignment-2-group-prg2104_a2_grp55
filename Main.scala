import com.github.tototoshi.csv.CSVReader

import java.io.File
import scala.util.{Failure, Success, Try}

final case class AmazonProduct(
    productId: String,
    productName: String,
    mainCategory: String,
    discountedPrice: Option[Double],
    actualPrice: Option[Double],
    discountPercentage: Option[Double],
    rating: Option[Double],
    ratingCount: Option[Long]
)

final case class TopProductResult(
    rank: Int,
    productId: String,
    productName: String,
    ratingCount: Long
)

final case class CategoryDiscountResult(
    category: String,
    averageDiscount: Double,
    productCount: Int
)

final case class FilteredProductResult(
    rank: Int,
    productId: String,
    productName: String,
    category: String,
    rating: Double,
    discountPercentage: Double,
    ratingCount: Option[Long]
)

object Main:
  private def nonEmpty(raw: String): Option[String] =
    Option(raw).map(_.trim).filter(_.nonEmpty)

  private def parseCurrency(raw: String): Option[Double] =
    nonEmpty(raw)
      .map(_.replace("₹", "").replace(",", ""))
      .flatMap(_.toDoubleOption)

  private def parsePercentage(raw: String): Option[Double] =
    nonEmpty(raw)
      .map(_.stripSuffix("%").trim)
      .flatMap(_.toDoubleOption)

  private def parseRating(raw: String): Option[Double] =
    nonEmpty(raw).flatMap(_.toDoubleOption)

  private def parseRatingCount(raw: String): Option[Long] =
    nonEmpty(raw)
      .map(_.replace(",", ""))
      .flatMap(_.toLongOption)

  private def extractMainCategory(raw: String): String =
    nonEmpty(raw)
      .flatMap(_.split("\\|").headOption)
      .map(_.trim)
      .filter(_.nonEmpty)
      .getOrElse("Unknown")

  private def productFrom(row: Map[String, String]): AmazonProduct =
    AmazonProduct(
      productId = row.getOrElse("product_id", "Unknown"),
      productName = row.getOrElse("product_name", "Unknown product"),
      mainCategory = extractMainCategory(row.getOrElse("category", "")),
      discountedPrice = parseCurrency(row.getOrElse("discounted_price", "")),
      actualPrice = parseCurrency(row.getOrElse("actual_price", "")),
      discountPercentage = parsePercentage(row.getOrElse("discount_percentage", "")),
      rating = parseRating(row.getOrElse("rating", "")),
      ratingCount = parseRatingCount(row.getOrElse("rating_count", ""))
    )

  def loadProducts(path: String): Try[Vector[AmazonProduct]] =
    Try {
      val reader = CSVReader.open(new File(path))
      try reader.allWithHeaders().iterator.map(productFrom).toVector
      finally reader.close()
    }

  private def uniqueProducts(products: Seq[AmazonProduct]): Vector[AmazonProduct] =
    products
      .groupBy(_.productId)
      .valuesIterator
      .flatMap(_.maxByOption(_.ratingCount.getOrElse(0L)))
      .toVector

  private def toTopProduct(product: AmazonProduct): Option[TopProductResult] =
    product.ratingCount.map(count =>
      TopProductResult(
        rank = 0,
        productId = product.productId,
        productName = product.productName,
        ratingCount = count
      )
    )

  def topFiveProducts(products: Seq[AmazonProduct]): Vector[TopProductResult] =
    val rankedProducts = products.groupBy(_.productId).valuesIterator.flatMap(_.maxByOption(_.ratingCount.getOrElse(0L))).flatMap(toTopProduct).toVector.sortBy(result => (-result.ratingCount, result.productName))

    rankedProducts
      .take(5)
      .zipWithIndex
      .map { case (result, index) => result.copy(rank = index + 1) }

  def averageDiscountByCategory(
      products: Seq[AmazonProduct]
  ): Vector[CategoryDiscountResult] =
    uniqueProducts(products).groupBy(_.mainCategory).map { case (category, categoryProducts) =>
        val validDiscounts: Seq[Double] = categoryProducts.flatMap(product =>
          product.discountPercentage: Option[Double]
        )
        val average =
          if validDiscounts.nonEmpty then validDiscounts.sum / validDiscounts.size
          else 0.0

        CategoryDiscountResult(category, average, validDiscounts.size)
      }
      .toVector
      .filter(_.productCount > 0)
      .sortBy(_.category)

  def highlyRatedDiscountedProducts(
      products: Seq[AmazonProduct]
  ): Vector[FilteredProductResult] =
    uniqueProducts(products)
      .filter(product =>
        product.rating.exists(_ >= 4.5) &&
          product.discountPercentage.exists(_ >= 50.0)
      )
      .sortBy(product =>
        (
          -product.rating.getOrElse(0.0),
          -product.ratingCount.getOrElse(0L),
          product.productName
        )
      )
      .take(10)
      .zipWithIndex
      .map { case (product, index) =>
        FilteredProductResult(
          rank = index + 1,
          productId = product.productId,
          productName = product.productName,
          category = product.mainCategory,
          rating = product.rating.getOrElse(0.0),
          discountPercentage = product.discountPercentage.getOrElse(0.0),
          ratingCount = product.ratingCount
        )
      }

  def printRows[T](title: String, rows: Seq[T])(format: T => String): Unit =
    println()
    println(title)
    println("-" * title.length)
    rows.foreach(row => println(format(row)))

  private def shorten(text: String, maxLength: Int): String =
    if text.length <= maxLength then text
    else text.take(maxLength - 3).trim + "..."

  def main(args: Array[String]): Unit =
    val path = args.headOption.getOrElse("data/amazon.csv")

    loadProducts(path) match
      case Success(products) =>
        val uniqueProductCount = products.map(_.productId).distinct.size
        println(s"Loaded ${products.size} CSV records from $path")
        println(s"Unique product IDs: $uniqueProductCount")

        printRows("Question 1 - Top 5 products by rating count", topFiveProducts(products))(
          result =>
            f"${result.rank}%d. ${result.productName} (${result.productId}) - ${result.ratingCount}%,d ratings"
        )

        printRows(
          "Question 2 - Average discount percentage by main category",
          averageDiscountByCategory(products)
        )(result =>
          f"${result.category}%-30s ${result.averageDiscount}%6.2f%% (${result.productCount}%d products)"
        )

        printRows(
          "Question 3 - Rating >= 4.5 AND discount >= 50% (top 10)",
          highlyRatedDiscountedProducts(products)
        )(result =>
          val ratingCountText = result.ratingCount.fold("N/A")(count => f"$count%,d")
          val shortName = shorten(result.productName, maxLength = 78)
          val metrics =
            f"Rating: ${result.rating}%.1f | Discount: ${result.discountPercentage}%.0f%% | Rating count: $ratingCountText"

          s"""#${result.rank} $shortName
             |   Product ID: ${result.productId}
             |   Category: ${result.category}
             |   $metrics
             |""".stripMargin
        )

      case Failure(error) =>
        System.err.println(s"Could not load '$path': ${error.getMessage}")
