import java.io.PrintWriter
import org.jsoup.Jsoup
import org.jsoup.nodes.{Node, Element}
import org.jsoup.select.Elements
import scala.collection.JavaConversions._
import scalaz._
import Scalaz._
object Weblio{
  def definition(article:Element)={
    val elems = article.getElementsByTag("div").get(1)
    elems.getElementsByTag("div").map(_.text()).mkString("\n")
  }
  def articles(vocab:String) = {
    //TODO scala 2.10/2.11にしてTryを使う
    val doc = Jsoup.connect("http://ejje.weblio.jp/content/"+vocab).get
    val body = doc.body()
    val base = body.getElementsByAttributeValueMatching("id","base").first()
    val main = base.getElementsByAttributeValueMatching("id","main").first()
    main.getElementsByClass("kiji")
  }
}
object Anki{
  def convert(words:Seq[(String,String)]):String={
    words.map{
      case(word,definition)=>word + ";\"" + definition.replaceAll(";","::").replaceAll("\"","''")+"\""
    }.mkString("\n")
  }
}
object Main{
  import Weblio._
  import Anki._
  def main(args: Array[String])={
    printWords(args)
  }
  def printWords = convertWords _ andThen print
  def convertWords(words:Seq[String]) = words.par.map{
    word => word->createCard(word)
  }.toBuffer |> convert
  def createCard(word:String) = articles(word).map(definition).mkString("\n")
  def export(out:String,value:String) = new PrintWriter(out) <| (_.print(value)) |> (_.close)
}