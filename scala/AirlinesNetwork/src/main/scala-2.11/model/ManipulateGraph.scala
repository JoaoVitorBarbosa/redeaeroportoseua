package model
import java.io._
import org.apache.spark._
import org.apache.spark.graphx._
import org.apache.spark.graphx.lib.ShortestPaths
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Dataset, SparkSession}


class ManipulateGraph(val sc: SparkContext, val spark: SparkSession, val flightsPath: String, val airportsPath: String) {

  var graph: Graph[Airport, Flight] = null
  var rddAirports: RDD[(VertexId, Airport)] = null
  val base_dir: String = "/home/joao/Desenvolvimento/Django/Airlines/network/static/network/output/"

  def ConvertCsvToFlights(): Dataset[Flight] ={
    import spark.implicits._
    import org.apache.spark.sql.Encoders
    // create schema for flight
    val schema = Encoders.product[Flight].schema

    // read csv of flights to flight dataset
    val flights: Dataset[Flight] = spark.read.format("com.databricks.spark.csv").schema(schema).option("header", "true").load(this.flightsPath).as[Flight]
    return flights
  }

  def convertCsvToAirports(): Dataset[Airport]={
    import spark.implicits._
    import org.apache.spark.sql.Encoders
    // create schema for airport
    val schema = Encoders.product[Airport].schema

    // read csv of airports to airport dataset
    val airports: Dataset[Airport] = spark.read.format("com.databricks.spark.csv").schema(schema).option("header", "true").load(this.airportsPath).as[Airport]

    return airports
  }

  def buildGraph(): Unit ={
    import spark.implicits._

    val flights: Dataset[Flight] = ConvertCsvToFlights()

    val flightsArray = flights.collect()

    // create array with (airportName, hasCode)
    // fill airports
    val airports: Dataset[(String, PartitionID)] = flights.flatMap(f => Array((f.Origin, f.Origin.hashCode),(f.Dest, f.Dest.hashCode))).distinct

    // build graph
    // Vertex is tuple (hashcode, airportName)
    val vertices: RDD[(VertexId, String)] = sc.parallelize(airports.collect().map(x => (x._2.toLong, x._1)))

    // edge is (srcId, targetId, flight)
    val relationships: RDD[Edge[Flight]] =
      sc.parallelize(flightsArray.map(flight => Edge(flight.Origin.hashCode.toLong, flight.Dest.hashCode.toLong, flight)))

    //build graph
    val graphAux = Graph(vertices, relationships)

    // collect all information about airports
    val airportsAttr = convertCsvToAirports()
    // set airports to format (vertexId, airport) => used in ourterJoinVertices
    this.rddAirports = sc.parallelize(airportsAttr.map(airport => (airport.iata.hashCode.toLong, airport)).collect())
    val defaultAirport = new Airport("","","","","",0.0,0.0)

    this.graph = graphAux.outerJoinVertices(rddAirports) { case (uid, deg, Some(att)) => att case(uid, deg, None) => defaultAirport }
    this.graph.persist()
  }

  def pageRank(): Unit = {
    val defaultAirport = new Airport("","","","","",0.0,0.0)

    val topVertices = this.graph.pageRank(0.001).vertices
    val g: Graph[(Double, Airport), Flight] = this.graph.outerJoinVertices(topVertices) { case (uid, deg, Some(att)) => (att, deg) case(uid, deg, None) => (0.0, defaultAirport) }
    val pgJson = g.vertices.sortBy(x => x._2._1, false).map(x => List("\"airport\": " + x._2._2.toJson(), "\"value\": " + x._2._1.toString).mkString("{", ",", "}")).collect().mkString("[", ",", "]")
    saveFile(this.base_dir + "pagerank.json", pgJson)
  }

  def maxInDegree(): Unit ={
    //val mDegree = this.graph.inDegrees.reduce((u, v) => if(u._2 > v._2) u else v)
    val topVertices = this.graph.inDegrees
    val g = this.graph.outerJoinVertices(topVertices) { case (uid, deg, Some(att)) => (deg, att) case(uid, deg, None) => (deg, 0)  }
    val degreeJson = g.vertices.sortBy(x => x._2._2, false).map(x => List("\"airport\": " + x._2._1.toJson(), "\"value\": " + x._2._2.toString).mkString("{", ",", "}")).collect().mkString("[", ",", "]")
    saveFile(this.base_dir + "indegree.json", degreeJson)
  }

  def maxOutDegree(): Unit ={
    //val maxDegree: (VertexId, PartitionID) = this.graph.outDegrees.reduce((u, v) => if(u._2 > v._2) u else v)
    val topVertices = this.graph.outDegrees
    val g = this.graph.outerJoinVertices(topVertices) { case (uid, deg, Some(att)) => (deg, att) case(uid, deg, None) => (deg, 0) }
    val degreeJson = g.vertices.sortBy(x => x._2._2, false).map(x => List("\"airport\": " + x._2._1.toJson(), "\"value\": " + x._2._2.toString).mkString("{", ",", "}")).collect().mkString("[", ",", "]")
    saveFile(this.base_dir + "outdegree.json", degreeJson)
  }

  def maxDegree(): Unit ={
    val topVertices = this.graph.degrees
    val g = this.graph.outerJoinVertices(topVertices) { case (uid, deg, Some(att)) => (deg, att) case(uid, deg, None) => (deg, 0) }
    val degreeJson = g.vertices.sortBy(x => x._2._2, false).map(x => List("\"airport\": " + x._2._1.toJson(), "\"value\": " + x._2._2.toString).mkString("{", ",", "}")).collect().mkString("[", ",", "]")
    saveFile(this.base_dir + "degree.json", degreeJson)
  }

  def getTriangle(): Unit = {
    val triangles = this.graph.triangleCount().vertices
    val g = this.graph.outerJoinVertices(triangles) { case (uid, deg, Some(att)) => (deg, att) case(uid, deg, None) => (deg, 0) }
    val trianglesJson = g.vertices.sortBy(x => x._2._2, false).map(x => List("\"airport\": " + x._2._1.toJson(), "\"value\": " + x._2._2.toString).mkString("{", ",", "}")).collect().mkString("[", ",", "]")
    saveFile(this.base_dir + "triangle.json", trianglesJson)
  }

  def getTriplets(yearStr: String, monthStr: String, dayofMonthStr: String, dayOfWeekStr: String, state: String, distance: String): Unit = {

    println("Parameters: " + yearStr + " " + monthStr + " " + dayofMonthStr + " " + dayOfWeekStr)

    val rddTriplets = this.graph.triplets.filter(triplet => (if(yearStr != "") triplet.attr.Year == yearStr.toInt else true) &&
      (if(monthStr != "") triplet.attr.Month== monthStr.toInt else true) &&
      (if(dayofMonthStr != "") triplet.attr.DayofMonth == dayofMonthStr.toInt else true) &&
      (if(dayOfWeekStr != "") triplet.attr.DayOfWeek == dayOfWeekStr.toInt else true) &&
      (if(state == "ALL") true else (triplet.dstAttr.state == state || triplet.srcAttr.state == state)) &&
      (if(distance != "" && triplet.attr.Distance != "") triplet.attr.Distance.toInt > distance.toInt else true)
    )

    val graphJson: String = rddTriplets.map(triplet => List("\"src\": " + triplet.srcAttr.toJson(), "\"dest\": " + triplet.dstAttr.toJson(), "\"edge\" :" + triplet.attr.toJson()).mkString("{", ",", "}")).collect().mkString("[", ",", "]")
    saveFile(this.base_dir + "graph.json", graphJson)
  }

  def saveFile(filePath: String, str: String): Unit = {
    val file = new File(filePath)
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write(str)
    bw.close()
  }

  def shortestPath(iata: String) : Unit = {
    val result = ShortestPaths.run(this.graph, Seq(iata.hashCode.toLong))
    //result.vertices.take(10).foreach(println)
    val shortestPath = result               // result is a graph
      .vertices                             // we get the vertices RDD
      .filter({case(vId, _) => vId == "IND".hashCode.toLong})  // we filter to get only the shortest path from v1
      .first                                // there's only one value
      ._2                                   // the result is a tuple (v1, Map)
      .get(iata.hashCode.toLong)

    println(shortestPath)
  }



  def ConnectedComponents(): Unit ={
    val cc = this.graph.connectedComponents()
    val joined = this.graph.outerJoinVertices(cc.vertices){(vid, vd, cc) => (vd, cc)}
    //val filtered = joined.subgraph(vpred = { (vid, vdcc) => vdcc._2 ==  })

//    val joined = g.outerJoinVertices(cc.vertices) {
//      (vid, vd, cc) => (vd, cc)
//    }
//    // Filter by component ID.
//    val filtered = joined.subgraph(vpred = {
//      (vid, vdcc) => vdcc._2 == Some(component)
//    })
//    // Discard component IDs.
//    filtered.mapVertices {
//      (vid, vdcc) => vdcc._1
//    }
  }

}
