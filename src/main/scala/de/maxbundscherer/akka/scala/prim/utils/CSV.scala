package de.maxbundscherer.akka.scala.prim.utils

trait CSV {

  object CSVWriter {

    def writeToCSV(to: Int, primeSize: Int, startTime: Long, maxWorkers: Int, time: Long, filename: String): Unit = {

      val writer = com.github.tototoshi.csv.CSVWriter.open(filename, append = true)

      writer.writeRow(List(to, primeSize, startTime, maxWorkers, time))

      writer.close()

    }

  }

}
