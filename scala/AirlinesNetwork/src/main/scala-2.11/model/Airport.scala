package model

/**
  * Created by joao on 13/12/16.
  */
case class Airport (iata: String, airport: String, city: String, state: String, country: String, latitude: Double, longitude: Double) {
  def toJson(): String = {
      return String.format("{\"iata\": \"%s\", \"airport\": \"%s\", \"city\": \"%s\", \"state\": \"%s\", \"country\": \"%s\", \"latitude\": \"%s\", \"longitude\": \"%s\"}", iata, airport,
        city, state, country, latitude.toString, longitude.toString)
  }

}