name := "scb"

version := "1.0"

organization := "com.scb"

libraryDependencies ++= {
  val akkaVersion = "2.4.12"
  val slickVersion = "3.1.1"

  Seq(
    "com.typesafe.akka"       %% "akka-actor"               % akkaVersion,
    "com.typesafe.akka"       %% "akka-cluster"             % akkaVersion,
    "com.typesafe.akka"       %% "akka-cluster-metrics"     % akkaVersion, 
    "com.typesafe.akka"       %% "akka-cluster-tools"       % akkaVersion,
    "com.typesafe.akka"       %% "akka-remote"              % akkaVersion,
    "com.typesafe.akka"       %% "akka-slf4j"               % akkaVersion,
    "com.typesafe.slick"      %% "slick"                    % slickVersion,
    "com.typesafe.slick"      %% "slick-hikaricp"           % slickVersion, 
    "com.h2database"          %  "h2"                       % "1.4.192",
    "ch.qos.logback"          %  "logback-classic"          % "1.1.7",
    "io.kamon"                % "sigar-loader"              % "1.6.6-rev002"
  )
}

// Assembly settings
mainClass in Global := Some("scb.tpcdi.Main")

jarName in assembly := "scb.jar"
