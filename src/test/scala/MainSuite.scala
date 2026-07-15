class MainSuite extends munit.FunSuite:
  private def product(
      id: String,
      name: String,
      category: String,
      discount: Option[Double],
      rating: Option[Double],
      ratingCount: Option[Long]
  ): AmazonProduct =
    AmazonProduct(
      productId = id,
      productName = name,
      mainCategory = category,
      discountedPrice = Some(100.0),
      actualPrice = Some(200.0),
      discountPercentage = discount,
      rating = rating,
      ratingCount = ratingCount
    )

  private val sampleProducts = Vector(
    product("A", "Alpha", "Electronics", Some(60.0), Some(4.8), Some(1000L)),
    product("A", "Alpha duplicate", "Electronics", Some(60.0), Some(4.8), Some(900L)),
    product("B", "Beta", "Electronics", Some(40.0), Some(4.6), Some(500L)),
    product("C", "Gamma", "Home", Some(80.0), Some(4.4), Some(300L)),
    product("D", "Delta", "Home", Some(50.0), Some(4.9), Some(200L)),
    product("E", "Epsilon", "Home", None, None, None),
    product("F", "Zeta", "Office", Some(20.0), Some(4.0), Some(100L))
  )

  test("Q1 ranks unique products by descending rating count") {
    val results = Main.topFiveProducts(sampleProducts)

    assertEquals(results.map(_.productId), Vector("A", "B", "C", "D", "F"))
    assertEquals(results.map(_.rank), Vector(1, 2, 3, 4, 5))
  }

  test("Q2 averages valid discounts for each main category") {
    val results = Main.averageDiscountByCategory(sampleProducts)
    val electronics = results.find(_.category == "Electronics")
    val home = results.find(_.category == "Home")

    assertEquals(electronics.map(_.averageDiscount), Some(50.0))
    assertEquals(electronics.map(_.productCount), Some(2))
    assertEquals(home.map(_.averageDiscount), Some(65.0))
    assertEquals(home.map(_.productCount), Some(2))
  }

  test("Q3 requires both the rating and discount conditions") {
    val results = Main.highlyRatedDiscountedProducts(sampleProducts)

    assertEquals(results.map(_.productId), Vector("D", "A"))
    assertEquals(results.map(_.rank), Vector(1, 2))
    assert(results.forall(result => result.rating >= 4.5 && result.discountPercentage >= 50.0))
  }

  test("loader reports a missing file through Failure") {
    assert(Main.loadProducts("data/does-not-exist.csv").isFailure)
  }
