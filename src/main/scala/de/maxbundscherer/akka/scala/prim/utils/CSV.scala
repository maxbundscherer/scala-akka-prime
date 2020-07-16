package de.maxbundscherer.akka.scala.prim.utils

trait CSV {

  object CSVWriter {

    def writeToCSV(to: Int, maxWorkers: Int, primeCounter: Int, time: Long, filename: String): Unit = {

      val writer = com.github.tototoshi.csv.CSVWriter.open(filename, append = true)

      writer.writeRow(List(to, maxWorkers, primeCounter, time))

      writer.close()

    }

  }

}
