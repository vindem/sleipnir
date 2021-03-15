# Information #
SLEIPNIR is a DAG scheduling simulator focused on mobile cloud/edge/iot infrastructures released under MIT license. If you use it in your papers, please cite my work using the following bibtex entry:

```
@inproceedings{DBLP:conf/ccgrid/MaioB18,
  author    = {Vincenzo De Maio and
               Ivona Brandic},
  editor    = {Esam El{-}Araby and
               Dhabaleswar K. Panda and
               Sandra Gesing and
               Amy W. Apon and
               Volodymyr V. Kindratenko and
               Massimo Cafaro and
               Alfredo Cuzzocrea},
  title     = {First Hop Mobile Offloading of {DAG} Computations},
  booktitle = {18th {IEEE/ACM} International Symposium on Cluster, Cloud and Grid
               Computing, {CCGRID} 2018, Washington, DC, USA, May 1-4, 2018},
  pages     = {83--92},
  publisher = {{IEEE} Computer Society},
  year      = {2018},
  url       = {https://doi.org/10.1109/CCGRID.2018.00023},
  doi       = {10.1109/CCGRID.2018.00023},
  timestamp = {Wed, 16 Oct 2019 14:14:53 +0200},
  biburl    = {https://dblp.org/rec/conf/ccgrid/MaioB18.bib},
  bibsource = {dblp computer science bibliography, https://dblp.org}
}
```

# Prerequisites #
SLEIPNIR is written in Java, therefore the latest version of JDK is required. 
Dependencies are resolved using Apache Maven, which is available at: https://maven.apache.org/download.cgi
Also, SLEIPNIR uses Apache Spark to distribute load on multiple cores.  Apache Spark can be downloaded at: https://spark.apache.org/downloads.html

# Download and install SLEIPNIR #
The simulator is available from github (https://github.com/vindem/sleipnir)
To download the latest version, you need to clone the repository running 
`git clone https://github.com/vindem/sleipnir`
To install the simulator, you need to run
`mvn clean package -Dmaven.test.skip`
Which generates the jar archive needed by Apache Spark. In alternative, you can also use the available build.sh script

`./build.sh`

This command will generate the jar sleipnir.jar in the subfolder target/.

# Running simulation with SLEIPNIR #
## “HelloWorld” example ##
In the HelloWorld example, we simulate offloading of a workflow composed of 5 sequential FACEBOOK DAGs, executed by 1000 mobile devices. Computational infrastructure is composed of 6 cloud nodes. We simulate mobility over the area of HERNALS, divided in hexagonal cells of 2 km radius with an edge node in each cell. For offloading, HEFT list-based algorithm is used. The “HelloWorld” example is described in the source file OffloadingHelloWorld.java.

## Running HelloWorld example ##
To run SLEIPNIR, just run

`spark-submit target/sleipnir.jar`

## Simulation Setup ##
Simulation can be configured either by using command lines arguments or the configuration file simulation.json. The arguments that can be used are the following:


* -h, -? Prints usage information
* -mobile=n Instantiates n mobile devices
* -cloud=n Instantiates n cloud nodes
* -wlRuns=n Each workflows has n applications
* -cloudonly=true/false If true, simulation uses only Cloud nodes
* -area=name Urban area where the offloading is performed (possible choices: HERNALS, LEOPOLDSTADT, SIMMERING)
* -eta=n Sets the eta parameter, which is necessary to set offloading cost (the higher the eta, the lower the cost).
* -outfile=string Saves output in file filename
* -iter=n Executes simulation for n iterations
* -navigatorMapSize=# Lambda parameter for size of navigator MAP (in kb)
* -antivirusFileSize=# Lambda parameter for size of antivirus file (in kb)
* -facerecImageSize=# Lambda parameter for size of image file (in kb) for Facerec app
* -chessMi=# Lambda parameter for computational size of Chess app 
* -navigatorDistr=# Probability of NAVIGATOR app in workflow (must be between 0 and 1).
* -antivirusDistr=# Probability of ANTIVIRUS app in workflow (must be between 0 and 1).
* -facerecDistr=# Probability of FACEREC app in workflow (must be between 0 and 1).
* -chessDistr=# Probability of CHESS app in workflow (must be between 0 and 1).
* -facebookDistr=# Probability of FACEBOOK app in workflow (must be between 0 and 1).
* -mobility=true/false If true, SLEIPNIR simulates mobility of users using sumo trace files. Example files for the areas of HERNALS, LEOPOLDSTADT and SIMMERING are available [here(https://www.dropbox.com/s/flox79qk2h24oqi/sleipnir-mobility-traces.zip?dl=0)


