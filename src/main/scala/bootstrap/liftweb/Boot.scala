package bootstrap.liftweb

import javax.naming.InitialContext
import javax.sql.DataSource

import net.liftweb._
import net.liftweb.squerylrecord.SquerylRecord
import org.squeryl.adapters.H2Adapter
import util._
import Helpers._

import common._
import http._
import js.jquery.JQueryArtifacts
import sitemap._
import Loc._
import mapper._

import code.model._
import net.liftmodules.JQueryModule
import org.squeryl.Session
import net.liftweb.common.Full
import net.liftweb.squerylrecord.RecordTypeMode._



/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  def boot {

    DefaultConnectionIdentifier.jndiName = Props.get("jndi.name") openOr "jdbc/demodb"
    val ds = new InitialContext().lookup("java:comp/env/jdbc/demodb").asInstanceOf[DataSource]
    SquerylRecord.initWithSquerylSession(Session.create(ds.getConnection(), new H2Adapter))

    //in dev mode print schema
    inTransaction {
      //uncomment following 2 lines to drop schema and create a new one.
      //if you run the application for the first you want to create the schema by uncommenting these lines,
      //after that comment them so the schema is drop and created again.
      DemoSchema.drop
      DemoSchema.create

      //this statement prints the schema
      DemoSchema printDdl
    }

    // where to search snippet
    LiftRules.addToPackages("code")

    // Build SiteMap
    def sitemap = SiteMap(
      Menu.i("Home") / "index" >> User.AddUserMenusAfter, // the simple way to declare a menu

      // more complex because this menu allows anything in the
      // /static path to be visible
      Menu(Loc("Static", Link(List("static"), true, "/static/index"), 
	       "Static Content")))

    def sitemapMutators = User.sitemapMutator

    // set the sitemap.  Note if you don't want access control for
    // each page, just comment this line out.
    LiftRules.setSiteMapFunc(() => sitemapMutators(sitemap))

    //Init the jQuery module, see http://liftweb.net/jquery for more information.
    LiftRules.jsArtifacts = JQueryArtifacts
    JQueryModule.InitParam.JQuery=JQueryModule.JQuery191
    JQueryModule.init()

    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)
    
    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // What is the function to test if a user is logged in?
    LiftRules.loggedInTest = Full(() => User.loggedIn_?)

    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) =>
      new Html5Properties(r.userAgent))

    //All Squeryl queries need to run in the context of a transaction
    //Configure a transaction around all HTTP requests
    S.addAround(new LoanWrapper {
      override def apply[T](f: => T): T = {
        val result = inTransaction {
          try {
            Right(f)
          } catch {
            case e: LiftFlowOfControlException => Left(e)
          }
        }

        result match {
          case Right(r) => r
          case Left(exception) => throw exception
        }
      }
    })
  }
}
