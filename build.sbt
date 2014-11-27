name := "Lift 2.6 starter template"

version := "0.0.3"

organization := "net.liftweb"

scalaVersion := "2.10.0"

resolvers ++= Seq("snapshots"     at "http://oss.sonatype.org/content/repositories/snapshots",
                  "staging"       at "http://oss.sonatype.org/content/repositories/staging",
                  "releases"      at "http://oss.sonatype.org/content/repositories/releases"
                 )

Seq(com.github.siasia.WebPlugin.webSettings :_*)

unmanagedResourceDirectories in Test <+= (baseDirectory) { _ / "src/main/webapp" }

scalacOptions ++= Seq("-deprecation", "-unchecked")

env in Compile := Some(file("./src/main/webapp/WEB-INF/jetty-env.xml") asFile)

libraryDependencies ++= {
  val liftVersion = "2.6-M2"
  Seq(
    "net.liftweb"       %% "lift-webkit"            % liftVersion        % "compile",
    "net.liftweb"       %% "lift-mapper"            % liftVersion        % "compile",
    "net.liftmodules"   %% "lift-jquery-module_2.6" % "2.5",
    "net.liftweb"       %% "lift-record"            % liftVersion,
    "net.liftweb"       %% "lift-squeryl-record"    % liftVersion,
    "org.eclipse.jetty" % "jetty-webapp"            % "8.1.7.v20120910"  % "container,test",
    "org.eclipse.jetty.orbit" % "javax.servlet"     % "3.0.0.v201112011016" % "container,test" artifacts Artifact("javax.servlet", "jar", "jar"),
    "org.eclipse.jetty" % "jetty-plus" % "8.1.7.v20120910" % "container,test",
    "ch.qos.logback"    % "logback-classic"         % "1.0.6",
    "org.specs2"        %% "specs2"                 % "1.14"             % "test",
    "com.h2database"    % "h2"                      % "1.3.167"
  )
}