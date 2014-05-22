import java.io.PrintWriter
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import scala.collection.JavaConversions._
import scala.util.Try
import scalaz._
import Scalaz._

object Weblio {
  val definition = (article: Element) => Try {
    val result = article.getElementsByTag("div").get(1).text()
    if (result.isEmpty) throw new RuntimeException("article was empty") else result
  }
  val articles = (word: String) => Try {
    val doc = Jsoup.connect("http://ejje.weblio.jp/content/" + word).get
    val body = doc.body()
    val base = body.getElementsByAttributeValueMatching("id", "base").first()
    val main = base.getElementsByAttributeValueMatching("id", "main").first()
    main.getElementsByClass("kiji")
  }
}
object Anki {
  val convert = (words: Seq[(String, String)]) => {
    words.map {
      case (word, definition) => word + ";\"" + definition.replaceAll(";", "::").replaceAll("\"", "''") + "\""
    }.mkString("\n")
  }
}
object Main {
  import Weblio._
  import Anki._
  def main(args: Array[String]) = {
    val inputs = Iterator.continually(readLine()).takeWhile(_ != null)
    printWords(inputs.toSeq)
  }
  val createCard = articles andThen (_.flatMap(_.map(definition).collect {
    case scala.util.Success(s) => s
  }.mkString("\n") match {
    case "" => Try(throw new RuntimeException("no definition"))
    case s => Try(s)
  }))
  val convertWords = (words: Seq[String]) => words.par.map {
    word => word -> createCard(word)
  }.partition(_._2.isSuccess)
  val printWords = convertWords andThen {
    case (successes, failures) => {
      failures.foreach(System.err.println)
      successes.collect {
        case (word, util.Success(defined)) => word -> defined
      }.toBuffer |> convert |> print
    }
  }
  val export = (out: String, value: String) => new PrintWriter(out) <| (_.print(value)) |> (_.close)
}