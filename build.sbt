name := "flightbookingsystem"
 
version := "1.0" 
      
lazy val `flightbookingsystem` = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
scalaVersion := "2.11.11"

libraryDependencies ++= Seq( javaJdbc , cache , evolutions, javaWs )
libraryDependencies += "org.xerial" % "sqlite-jdbc" % "3.20.0"
unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )
libraryDependencies += guice

fork in run := false
resolvers += "SQLite-JDBC Repository" at "https://oss.sonatype.org/content/repositories/snapshots"
      