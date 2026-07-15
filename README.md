# PRG2104 Assignment 2 — Amazon Sales Analytics

## Group and dataset

- Group number: `[fill in]`
- Architect: `[Looi Jun Xian]`
- Coder: `[Ong Kang Xu]`
- Documenter: `[Lim Jun Jie]`
- Dataset: **Amazon Sales Dataset (#3)**
- Source: <https://www.kaggle.com/datasets/karkavelrajaj/amazon-sales-dataset>
- Local file: `data/amazon.csv`
- Verified MD5: `ee866c4757bb72b417533a2a742a8fb2`
- Raw records: 1,465; unique product IDs: 1,351

The dataset describes Amazon products, prices, discounts, ratings, and reviews. It does not contain quantities sold, so this project does not label any derived value as revenue.

## Analytical questions

1. **Top-N:** Which five unique products have the largest `rating_count`?
2. **Aggregation:** What is the average `discount_percentage` for each top-level product category?
3. **Filter and relational pipeline:** Which unique products have a rating of at least 4.5 **and** a discount of at least 50%? The program prints the top ten, ordered by rating, rating count, and product name.

Duplicate CSV rows are resolved by `product_id`; the row with the largest valid rating count represents that product. This prevents the same product from being counted more than once.

## Technology

- Scala 3.8.4
- sbt 1.12.12
- `scala-csv` 2.0.0 for header-aware CSV parsing
- MUnit 1.0.2 for automated tests
- Immutable `Vector`, `Seq`, and `Map` collection pipelines
- `Try` for file and CSV failures; `Option` for missing or invalid numbers

## Data cleaning rules

- Remove the rupee symbol and thousands separators before parsing prices.
- Remove `%` before parsing discount percentages.
- Remove thousands separators before parsing rating counts.
- Parse invalid or missing numeric fields as `None` with `toDoubleOption` or `toLongOption`.
- Use the text before the first `|` as the main category.
- Preserve the original `amazon.csv`; cleaning occurs only in memory.

## Run the project

From this directory, run:

```bash
sbt test
sbt run
```

If `sbt` is unavailable but the local launcher is present:

```bash
java -jar tools/sbt-launch-1.12.12.jar test
java -jar tools/sbt-launch-1.12.12.jar run
```

The final verified output is saved in `docs/output.txt`.

## Documentation and role evidence

- Verified sample run: `docs/output.txt`



## Evidence to place in the assignment document

- GitHub Classroom repository URL and commit-history URL
- Code excerpts with final `Main.scala` line numbers
- Successful `sbt clean compile`, `sbt test`, and `sbt run` evidence
- `docs/output.txt` or screenshots of all three results
- An 80–120 word subtype-polymorphism note
- A 300–500 word OOP discussion
- A 200–300 word AI reflection
- Completed AI Interaction Log and three signed declarations
