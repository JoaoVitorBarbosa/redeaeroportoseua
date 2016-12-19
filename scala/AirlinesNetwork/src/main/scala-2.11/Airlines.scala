import model.Flight
import model.ManipulateGraph
import org.apache.log4j.{Level, Logger}
import org.apache.spark._
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Dataset, SQLContext, SparkSession}

/**
  * Created by joao on 01/12/16.
  */
object Airlines {
  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.ERROR)

    var argsStr = ""
    for (elem <- args) {
      argsStr += elem + " "
    }

    println("Commandos: " + argsStr)

    var command: String = if(args.length > 0) args(0) else ""
    var year = ""
    var month = ""
    var dayOfMonth = ""
    var dayOfWeek = ""
    var state = ""
    var distance = ""

    if (command == "triplets")
      {
        year = args(1)
        month = args(2)
        dayOfMonth = args(3)
        dayOfWeek = args(4)
        state = args(5)
        distance = args(6)
      }

    val spark: SparkSession = SparkSession.builder().master("spark://joao:7077").appName("Arilines").getOrCreate()
    val sc: SparkContext = spark.sparkContext
    val mGraph = new ManipulateGraph(sc, spark,"/home/joao/Desenvolvimento/Airlines/2008_3.csv", "/home/joao/Desenvolvimento/Airlines/airports.csv")
    mGraph.buildGraph()

    //command = "shortestPath"

    command match {
      case "pagerank" => mGraph.pageRank()
      case "numVertices" => println(mGraph.graph.vertices.count())
      case "numEdges" => println(mGraph.graph.edges.count())
      case "inDegree" => mGraph.maxInDegree()
      case "outDegree" => mGraph.maxOutDegree()
      case "degree" => mGraph.maxDegree()
      case "triplets" => mGraph.getTriplets(year, month, dayOfMonth, dayOfWeek, state, distance)
      case "triangle" => mGraph.getTriangle()
      case "shortestPath" => mGraph.shortestPath("TPA")
    }
  }
}
