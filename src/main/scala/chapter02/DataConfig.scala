package chapter02

case class Restaurant(id: String, name: String, features: Array[String], city: String)
case class SessionData(datetime: String, ip: String,
  entryPoint: String, navigations: Array[String], endPoint: String)

trait Constants {
  val locations = Array(
    "atlanta.txt",
    "boston.txt",
    "chicago.txt",
    "los_angeles.txt",
    "new_orleans.txt",
    "new_york.txt",
    "san_francisco.txt",
    "washington_dc.txt" //
    )

  val cityCodes = Map(
    "A" -> "Atlanta",
    "B" -> "Boston",
    "C" -> "Chicago",
    "D" -> "Los Angeles",
    "E" -> "New Orleans",
    "F" -> "New York",
    "G" -> "San Francisco",
    "H" -> "Washington DC" //
    )

  val navigationOperationCodes = Map(
    "L" -> "browse (move from one restaurant in a list of recommendationsto another)",
    "M" -> "cheaper (search for a restaurant like this one, but cheaper)",
    "N" -> "nicer   (search for a restaurant like this one, but nicer)",
    "O" -> "closer  (unused in the production version of the system)",
    "P" -> "more traditional (search for a restaurant like this, but serving more traditional cuisine)",
    "Q" -> "more creative (search for a restaurant serving more creative cuisine)",
    "R" -> "more lively (search for a restaurant with a livelier atmosphere)",
    "S" -> "quieter (search for a restaurant with a quieter atmosphere)",
    "T" -> """change cuisine (search for a restaurant like this, but
         serving a different kind of food) Note that with this
         tweak, we would ideally like to know what cuisine the user
         wanted to change to, but this information was not recorded.""")
}

case class DataConfig(val datasetPath: String) extends Constants {
  val dataPath = s"$datasetPath/data"
  val sessionPath = s"$datasetPath/session"
  val featuresFile = s"$dataPath/features.txt"
}