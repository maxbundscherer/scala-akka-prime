package de.maxbundscherer.akka.scala.prim.utils

trait CSV {

  object CSVWriter {

    def writeToCSV(to: Int, primeSize: Int, startTime: Long, time: Long, maxWorkers: Int, filename: String): Unit = {

      val writer = com.github.tototoshi.csv.CSVWriter.open(filename, append = true)

      writer.writeRow(List(to, primeSize, startTime, time, maxWorkers))

      writer.close()

    }

  }

}
