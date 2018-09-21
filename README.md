# Table-Extension-System-for-MSc-Dissertation

This table extension system is a Java Play Webservice which provides functions for table extension and repository management. <br>
For further about the system, please look at :
<ul>
  <li><a href="http://web.informatik.uni-mannheim.de/ds4dm/API-definition.html">The Details on the REST-API-calls used for executing the different functions</a>
  <li><a href="http://web.informatik.uni-mannheim.de/ds4dm/">An overview of the algorithms and their evaluations</a>
  <li><a href="http://web.informatik.uni-mannheim.de/ds4dm/Javadoc/index.html">Detailed documentation (JavaDoc) of the individual classes in the code</a>
  <li><a href="https://dws.informatik.uni-mannheim.de/fileadmin/lehrstuehle/ki/pub/KleppmannBizer-DensityAndCorrelationBasedTableExtension-LWDA2018.pdf">A paper describing the unconstrained- and correlation-based- table extension</a>.
</ul>	
<br>
<br>


## Setup Option 1 - Installing on a computer
<ol>
  <li>Download this GitHub repository
  <li> Go to the releases page (https://github.com/BenediktKleppmann/DS4DM-Backend/releases) and download the three jar-files: "CreateCorrespondenceFiles-0.0.1-SNAPSHOT.jar", "CreateLuceneIndex-0.0.1-SNAPSHOT-jar-with-dependencies.jar" and "winter-1.0-jar-with-dependencies.jar". <br>
    Copy them to the following location in the downloaded GitHub repository: DS4DM-Backend\DS4DM-Webservice\DS4DM_webservice\lib
  <li>Make sure that the environment variable JAVA_HOME points to a jdk_8... -folder
  <li>Open the a terminal and execute:<br>
    <table frame="box">
      <tr>
        <th>
		  cd <i>&lt;path_to_downloaded_folder&gt;</i>/DS4DM-Backend/DS4DM-Webservice/DS4DM_webservice<br>
          java -Xms1024m -Xmx1024m -XX:MetaspaceSize=64m -XX:MaxMetaspaceSize=256m -jar activator-launch-1.2.12.jar "run -Dhttp.port=9004"
        </th>
      </tr>
    </table><br>
   <li>In your RapidMiner-process you now set url-Parameter of the Data Search operator to "http://localhost:9004".<br>
     <img class="img-responsive" src="http://web.informatik.uni-mannheim.de/ds4dm/images/Set_URL_in_Data_Search_operator.png" alt="keyword-based search" height="350" width="900" align="middle"  style="display: block; margin-left:auto; margin-right: auto;z-index: 1;">  
</ol>
<br>


## Setup Option 2 - Running a virtual machine
<ol>
  <li>From the <a href="https://drive.google.com/drive/u/2/folders/10FXg3QIXJXtIux78sqlpsGhKbNd6VVkp">Project's Google Drive repository</a> download the virtual machine image 'Ubuntu Server 16.04.4 (32bit).vdi'
  <li>Launch the virtual machine
  <li>Log on to the user: 'osboxes.org', password: 'osboxes.org'
  <li>open a terminal and execute the following commands:<br>
	  <table frame="box">
		  <tr>
			<th>
			  cd /home/osboxes/Desktop/DS4DM-Backend-master/DS4DM-Webservice/DS4DM_webservice<br>
			  java -Xms1024m -Xmx1024m -XX:MetaspaceSize=64m -XX:MaxMetaspaceSize=256m -jar activator-launch-1.2.12.jar "run -Dhttp.port=9004"
			</th>
		  </tr>
		</table><br>
</ol>


## Technical description of the Backend components
### DS4DM-CreateCorrespondences
This backend component contains methods for finding/creating correspondences between tables. These methods are used by DS4DM-Webservice (the main backend component). For this the CreateCorrespondenceFiles-maven-project is compiled into a jar-file. The jar file with dependencies is saved to the folder DS4DM-Backend/DS4DM-Webservice/DS4DM_webservice/lib/ and added to the Build Path of the DS4DM_webservice-maven-project

### DS4DM-CreateLuceneIndex
This backend component contains methods for indexing tables. These methods are also used by the DS4DM-Webservice. As with CreateCorrespondences, the CreateLuceneIndex-maven-project is compiled to a jar file and the jar file with dependencies is saved to DS4DM-Backend/DS4DM-Webservice/DS4DM_webservice/lib/, from where it is included in the Build Path of DS4DM_webservice.

### DS4DM-Webservice
This is the main Backend component. 
The maven-project is structured according to the Java-Play-framework-guidelines. This allows the program activator-launch-1.2.12.jar to provide an API endpoint which calls various methods in this backend component.
The File DS4DM-Backend/DS4DM-Webservice/DS4DM_webservice/conf/routes specifies the API calls that are possible and which methods these call. The majority of the called methods are in the class DS4DM-Backend/DS4DM-Webservice/DS4DM_webservice/app/controllers/ExtendTable.java. (All of the executed code is in the folder DS4DM-Backend/DS4DM-Webservice/DS4DM_webservice/app).
The DS4DM-Webservices uses repositories of tables. These repositories are in the folder DS4DM-Backend/DS4DM-Webservice/DS4DM_webservice/public/repositories. Each repository has one folder containing csv-tables, one folder containing Indexes and another folder containing Correspondences, as well as a file with repository statistics.

### Evaluation tables
This isn't a backend component, but a collection of the csv files that were used for the evaluations. For more information on the evaluations, please refer to http://web.informatik.uni-mannheim.de/ds4dm/#evaluation.



## Other Resources:
<ul>
  <li><a href="http://ds4dm.de/">Official DS4DM website</a>
  <li><a href="http://web.informatik.uni-mannheim.de/ds4dm/">Website for the DSDM Backend</a>
  <li><a href="http://dws.informatik.uni-mannheim.de/en/projects/ds4dm-data-search-for-data-mining/">Website from Mannheim University</a>
</ul>


