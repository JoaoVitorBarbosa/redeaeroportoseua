package model

/**
  * Created by joao on 01/12/16.
  */
case class Flight(Year: Int, Month: Int, DayofMonth: Int, DayOfWeek: Int, DepTime: String, CRSDepTime: String, ArrTime: String, CRSArrTime: String,
                  UniqueCarrier: String, FlightNum: String, TailNum: String, ActualElapsedTime: String, CRSElapsedTime: String, AirTime: String, ArrDelay: String,
                  DepDelay: String, Origin: String, Dest: String, Distance: String, TaxiIn: String, TaxiOut: String, Cancelled: String, CancellationCode: String,
                  Diverted: String, CarrierDelay: String, WeatherDelay: String, NASDelay: String, SecurityDelay: String, LateAircraftDelay: String){
  def toJson(): String = {
    return String.format("{ \"year\": \"%s\", \"month\" : \"%s\", \"dayOfMonth\" : \"%s\", \"dayOfWeek\": \"%s\", \"uniqueCarrier\": \"%s\", \"origin\": \"%s\", " +
      "\"dest\": \"%s\", \"distance\": \"%s\" }",
      Year.toString, Month.toString, DayofMonth.toString, DayOfWeek.toString, UniqueCarrier, Origin, Dest, Distance)
  }
}

