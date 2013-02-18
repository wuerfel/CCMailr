import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "CCMailr"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq( javaCore, javaJdbc, javaEbean
      // Add your project dependencies here,
      
    )

    val main = play.Project(appName, appVersion, appDependencies).settings(
      // Add your own project settings here      
    )

}
