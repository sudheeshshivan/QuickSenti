package controllers


import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import scala.util.control._
import java.io._
import scala.sys.process._
import java.lang._
import models.AdminModel
import java.lang.reflect.Field
import org.apache.hadoop.hbase._
import org.apache.hadoop.hbase.client.{HBaseAdmin,HTable,Get}
import org.apache.hadoop.hbase.util.Bytes
import java.net.Socket
import play.api.libs.json._
import org.apache.hadoop.hbase.client.Scan
import java.math.RoundingMode
import org.postgresql.util.PSQLException
import org.quartz.impl._
import org.quartz.Job
import org.quartz.JobKey
import org.quartz.JobExecutionContext
import org.quartz.JobBuilder.newJob
import org.quartz.TriggerBuilder.newTrigger
import org.quartz.CronScheduleBuilder._
import java.util.Date



case class ConfigurationInfo(accountType : String, accountTitle : String, accessKey : String, accessSecret : String, consumerKey : String, consumerSecret : String, keywords : String,tablename : String)

case class ScheduleAnalysisInfo(dsid : Int, scheduleHr : Int, scheduleMin : Int)

object AdminArea extends Controller {
  

 /* Form Models Declaration */
  
//  Form For defining new datasource
  val configurationForm = Form(
    mapping(
      "accountType"    -> nonEmptyText,
      "accountTitle"   -> nonEmptyText,
      "accessKey"      -> nonEmptyText,
      "accessSecret"   -> nonEmptyText,
      "consumerKey"    -> nonEmptyText,
      "consumerSecret" -> nonEmptyText,
      "keywords"       -> nonEmptyText,
      "tablename"      -> nonEmptyText
    )(ConfigurationInfo.apply)(ConfigurationInfo.unapply)
  )
  
  
  
//  Form for scheduling analysis part
  val scheduleAnalysisForm = Form(
      mapping(
          "dsid"  -> number.verifying("Error ", !_.<(0) ),
          "scheduleHr" -> number(min = 1, max = 24),
          "scheduleMin" -> number(min = 1, max = 60)
      )(ScheduleAnalysisInfo.apply)(ScheduleAnalysisInfo.unapply)
  )
  
  /* Form Model Declaration Ends */
   
  
//  Menu for headers
  
  
  def checkSession (req : Request[AnyContent]) : Boolean = {
    req.session.get("uid").map { user =>
      val group = req.session.get("usergroup").get
//      println(group)
      if(group.toInt==0){        
    	  return true
      }
      else{
        return false
      }
    }.getOrElse {
      return false
    } 
  }
  
  
  
  def checkSessionWithMultipart (req : Session ) : Boolean = {
    req.get("uid").map { user =>
      return true
    }.getOrElse {
      return false
    } 
  }
  
  
  def dashboard = Action {implicit request =>
    if(checkSession(request)){      
      val reportMenu = GeneralFunctions.loadReportMenuItems
//      Ok(play.core.PlayVersion.current.toString())
      // Ok(views.html.dashboard(views.html.adminheader("Admin area | Dash Board",1,reportMenu)))   
      Ok(
        views.html.dashboard2(
          views.html.adminheader("Admin area | Dash Board",1,reportMenu), 
          views.html.adminfooter()
        )
      )  
    }
    else{      
      Redirect(routes.Application.index()).flashing(
                  "error" -> "Your last session expired, Please Login again ")
    }
  }  
  
  
  def dataSource = Action {implicit request =>
    if(checkSession(request)){
      val reportMenu = GeneralFunctions.loadReportMenuItems
      Ok(
          views.html.datasource(
            views.html.adminheader("Data Source Configuration",2,reportMenu), 
            configurationForm,
            "",
            views.html.adminfooter()
          )
        )
    }
    else{
      Redirect(routes.Application.index()).flashing(
                  "error" -> "Your last session expired, Please Login again ")
    }
  }
  
  
  
  def editDataSource(hashId : String) = Action {  implicit request =>
    if(checkSession(request)){
      val reportMenu = GeneralFunctions.loadReportMenuItems
      val result = AdminModel.readSingleDataSource(hashId).head
      
      val dataMap = Map("accountType" -> result.accountType, "accountTitle" -> result.accountTitle, "tablename" -> result.tablename, "accessKey" -> result.accessKey, "accessSecret" -> result.accessSecret, "consumerKey" -> result.consumerKey, "consumerSecret" -> result.consumerSecret, "keywords" -> result.keywords)
      val formForEdit = configurationForm.bind(dataMap)
      Ok(views.html.editdatasource(views.html.adminheader("Edit DataSource Configuration ",2,reportMenu),formForEdit, hashId))
    }
    else{
      Redirect(routes.Application.index()).flashing(
                  "error" -> "Your last session expired, Please Login again ")
    }
  }
  
  
  
  
  def doConfiguration = Action {implicit request =>
    if(checkSession(request)){
      val reportMenu = GeneralFunctions.loadReportMenuItems
      configurationForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(
          views.html.datasource(
            views.html.adminheader("Configuration Error",2,reportMenu),
            formWithErrors,
            "",
            views.html.adminfooter()
          )
        )
      },
      configurationData => {
        val result = AdminModel.saveConfiguration(configurationData.accountType, configurationData.accountTitle, configurationData.accessKey, configurationData.accessSecret, configurationData.consumerKey, configurationData.consumerSecret, configurationData.keywords, configurationData.tablename)
        
        if(result > 0){
          Ok(
            views.html.datasource(
              views.html.adminheader("Data Source Configuration",2,reportMenu),
              configurationForm,"Configuration Updated Successfully",
              views.html.adminfooter()
              )
            )
        }
        else{
          Ok("Updation Error")
        }
      })
    }
    else{      
      Redirect(routes.Application.index()).flashing(
                  "error" -> "Your last session expired, Please Login again ")
    }
  }
  
  
  def deleteDataSource(hashId : String) = Action {implicit request =>    
    if(checkSession(request)){
      val reportMenu = GeneralFunctions.loadReportMenuItems
      val result= AdminModel.deleteDataSource(hashId)
      val dataSource = AdminModel.readAllDataSource
      Ok(views.html.datasourcelist(views.html.adminheader("Data Source List | QSenti",2,reportMenu),dataSource))
    }
    else{
      Redirect(routes.Application.index()).flashing(
                  "error" -> "Your last session expired, Please Login again ")
    }   
  }
  
  
  
  def doUpdateConfiguration(hashId : String) = Action {implicit request =>    
    if(checkSession(request)){
      val reportMenu = GeneralFunctions.loadReportMenuItems
      configurationForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.editdatasource(views.html.adminheader("Configuration Error",2,reportMenu),formWithErrors,hashId))
      },
      configurationData => {
        val result = AdminModel.updateConfiguration(configurationData.accountType, configurationData.accountTitle, configurationData.accessKey, configurationData.accessSecret, configurationData.consumerKey, configurationData.consumerSecret, configurationData.keywords, configurationData.tablename,hashId)
        
        if(result > 0){
          Ok(
            views.html.datasource(
              views.html.adminheader("Data Source Configuration",2,reportMenu),
              configurationForm,
              "Configuration Updated Successfully",
              views.html.adminfooter()
            )
          )
        }
        else{
          Ok("Updation Error")
        }
      })
    }
    else{
      Redirect(routes.Application.index()).flashing(
                  "error" -> "Your last session expired, Please Login again ")
    }
  }
  
  
  def dataSourceList = Action { implicit request =>
    if(checkSession(request)){
      val reportMenu = GeneralFunctions.loadReportMenuItems
      val dataSource = AdminModel.readAllDataSource
      Ok(views.html.datasourcelist(views.html.adminheader("Data Source List | QSenti",2,reportMenu),dataSource))
    }
    else{
      Redirect(routes.Application.index()).flashing(
                  "error" -> "Your last session expired, Please Login again ")
    }    
  }
  
  
    
  def dataSourceDetailedView(hashId : String) = Action { implicit request => 
    if(checkSession(request)){
      val reportMenu = GeneralFunctions.loadReportMenuItems
      val result = AdminModel.readSingleDataSource(hashId) 
      Ok(views.html.dataSourceDetailedView(views.html.adminheader("QSenti | Data Source Detailed View ", 0,reportMenu),result))
    }
    else{
      Redirect(routes.Application.index()).flashing(
                  "error" -> "Your last session expired, Please Login again ")
    }    
  }
  
  
  def updateStreamingStatus(hashId : String ) = Action { implicit request =>
    if(checkSession(request)){
      val reportMenu = GeneralFunctions.loadReportMenuItems
      val result = AdminModel.readSingleDataSource(hashId)
      val configList = result.head
      val fileName = configList.filename
      
      if(configList.processid>0){
        val processResult = scala.sys.process.Process("kill "+configList.processid )! ProcessLogger( _ => {})
        AdminModel.updateStreamingProcessId(0, hashId)
      }
      else{
        
        val confFile = new File("public/flume_config/"+configList.filename+".conf")
        confFile.getParentFile().mkdirs()
        if(!confFile.exists()){
          confFile.createNewFile()
        }
        val writer = new PrintWriter(confFile)
        writer.write("""
    
    
        TwitterAgent.sources = Twitter
        TwitterAgent.channels = MemChannel
        TwitterAgent.sinks = HDFS
        
        TwitterAgent.sources.Twitter.type = com.qburst.twittersource.TwitterSampleStreamSource
        TwitterAgent.sources.Twitter.channels = MemChannel
        TwitterAgent.sources.Twitter.consumer.key = """  + configList.consumerKey + """
        TwitterAgent.sources.Twitter.consumer.secret = """  + configList.consumerSecret + """
        TwitterAgent.sources.Twitter.access.token = """  + configList.accessKey + """
        TwitterAgent.sources.Twitter.access.token.secret = """  + configList.accessSecret + """
        TwitterAgent.sources.Twitter.filter.by = """  + configList.keywords + """
        
        TwitterAgent.sinks.HDFS.channel = MemChannel
        TwitterAgent.sinks.HDFS.type = hdfs
        TwitterAgent.sinks.HDFS.hdfs.path = hdfs://localhost:9100/user/twitterSenti/""" + configList.tablename + """
        TwitterAgent.sinks.HDFS.hdfs.fileType = DataStream
        TwitterAgent.sinks.HDFS.hdfs.writeFormat = Text
        TwitterAgent.sinks.HDFS.hdfs.batchSize = 64000
        TwitterAgent.sinks.HDFS.hdfs.rollSize = 67108864
        TwitterAgent.sinks.HDFS.hdfs.rollInterval = 3600
        TwitterAgent.sinks.HDFS.hdfs.rollCount = 0
        
        TwitterAgent.channels.MemChannel.type = memory
        TwitterAgent.channels.MemChannel.capacity = 1000000
        TwitterAgent.channels.MemChannel.transactionCapacity = 10000
    
    """)
        writer.close()
      
        
        /*** HBase Table Creation Begins ***/
        /*
        val conf = HBaseConfiguration.create();
        val hbase = new HBaseAdmin(conf);
        if(hbase.tableExists(configList.tablename)==false){       
          val desc = new HTableDescriptor(TableName.valueOf(configList.tablename));
          val meta = new HColumnDescriptor("cf".getBytes());
          desc.addFamily(meta);
          hbase.createTable(desc);          
        }*/
        
        //Sample Code to start flume agent with the Process constructor
        //  val processResult = Process("flume-ng agent -n TwitterAgent -c $FLUME_HOME/conf -f public/flume_config/"+configList.filename+".conf")! ProcessLogger( _ => {})
        
        
        //Create a directory within HDFS
        val str = "/home/qburst/hadoop-2.6.0/bin/hdfs dfs -mkdir /user/twitterSenti/" + configList.tablename
        val startDirectory = Runtime.getRuntime.exec(str)
        
        
        val myProcess = Runtime.getRuntime.exec("flume-ng agent -n TwitterAgent -c $FLUME_HOME/conf -f public/flume_config/"+configList.filename+".conf")
        // Starting flume agent with exec      
        
        
        //Reading the process id of the flume agent
        val f = myProcess.getClass().getDeclaredField("pid")
        f.setAccessible(true)
        val pid =f.getInt(myProcess)
        
        
        AdminModel.updateStreamingProcessId(pid, hashId)  
      }      
      Redirect("../dataSource/detailedview/"+hashId)
    }
    else{
      Redirect(routes.Application.index()).flashing(
                  "error" -> "Your last session expired, Please Login again ")
    }
  }
  
  def getTrendData(startDate : String , endDate : String)  = Action{implicit request =>
    if(checkSession(request)){
      val reportMenu = GeneralFunctions.loadReportMenuItems
      val result = AdminModel.readSentimentTrend(startDate,endDate)
      if(!result.isEmpty){       
        var resultMap : Map[String,Int] = Map()
        var trendData ="""{"trendData":[""";
        var dateList : List[String] = List.empty
        
        for(report <- result){
          if(!dateList.contains(report.date)){
            if(report.sentiment == "Negative" || report.sentiment == "Very negative"){
              trendData += """
                {"name":"""" + report.date + """","value":""" + report.percentage * -1 + """},"""
            }
            else if(report.sentiment=="Neutral"){
              trendData += """
                {"name":"""" + report.date + """","value":""" + (report.percentage * 0) + """},"""            
            }
            else{
              trendData += """
                {"name":"""" + report.date + """","value":""" + report.percentage + """},"""            
            }              
            dateList = dateList.+:(report.date) 
          }
        }      
        
        val pieResult = AdminModel.readPieData(startDate, endDate)        
        var pieChartData = "["
        var currentSentiment ="" 
        for(report <- pieResult){          
          if(currentSentiment==""){
            currentSentiment = report.sentiment
            pieChartData += """
            {
              "data" : ["""
          }
          if(currentSentiment!=report.sentiment){
            pieChartData = pieChartData.stripPrefix(",").stripSuffix(",").trim()
            pieChartData += """
            ],
              "sentiment":"""" + currentSentiment + """", "value":23
            },
            {
              "data" : ["""
            currentSentiment = report.sentiment
          }
          else if(currentSentiment==report.sentiment){
            pieChartData += """
                {
                  "date" : "12-05-2014",
                  "value" : 25
                },"""
          }
        }        
        pieChartData = pieChartData.stripPrefix(",").stripSuffix(",").trim()
        pieChartData += """
        ],
          "sentiment":"""" + currentSentiment + """", "value":23
        }
      ]"""
        trendData = trendData.stripPrefix(",").stripSuffix(",").trim()
        trendData += """
        ],
        "pieChartData" : """ + pieChartData + """
        }
        """
        val json_obj = Json.parse(trendData)
        Ok(json_obj) 
      }
      else{
        Ok(" Sorry, No Data Available :-( ")
      }
    }
    else{
      Ok("Invalid Request")      
    }
  }  
  
  def scheduleAnalysis = Action {implicit request =>
    if(checkSession(request)){   
      val reportMenu = GeneralFunctions.loadReportMenuItems
      val sourceSeq = GeneralFunctions.loadDataSourcesForForm
      val scheduledRecords = AdminModel.readAllAnalysisSchedule      
      Ok(views.html.scheduleAnalysis(views.html.adminheader("Schedule Analysis", 2,reportMenu), scheduleAnalysisForm, sourceSeq, scheduledRecords))     
    }
    else{
      Redirect(routes.Application.index()).flashing(
                  "error" -> "Your last session expired, Please Login again ")
    }
  }
  
  def doScheduleAnalysis = Action { implicit request =>
    if(checkSession(request)){
      val reportMenu = GeneralFunctions.loadReportMenuItems
      val sourceSeq = GeneralFunctions.loadDataSourcesForForm
      
      scheduleAnalysisForm.bindFromRequest.fold(
      formWithErrors => {  
        
        val scheduledRecords = AdminModel.readAllAnalysisSchedule
        BadRequest(views.html.scheduleAnalysis(views.html.adminheader("Schedule Analysis Error",2,reportMenu),formWithErrors, sourceSeq, scheduledRecords))
      },
      scheduleData => {
        try{
          val result = AdminModel.savescheduleAnalysis(scheduleData.dsid,scheduleData.scheduleHr,scheduleData.scheduleMin)
          val dataSource = AdminModel.readSingleDataSource(scheduleData.dsid)
          val configuringDataSource = dataSource.head
          /*Scheduling Crone job using Quartz*/
          val scheduler = StdSchedulerFactory .getDefaultScheduler();
          Logger.info("Quarz scheduler starting...")      
          scheduler.start();          
          val job = newJob(classOf[MyWorker])
          .withIdentity("job"+scheduleData.dsid, "analyserGroup")
          .build();
          val jobKey = new JobKey("job"+scheduleData.dsid, "analyserGroup")          
          if(scheduler.checkExists(jobKey)){      
            Logger.info("Quartz scheduler shutdown.")
            scheduler.deleteJob(jobKey)      
          }          
          val trigger = newTrigger()
          .withIdentity("trigger"+scheduleData.dsid, "analyserGroup")
          .withSchedule(cronSchedule("0 "+scheduleData.scheduleMin+" "+scheduleData.scheduleHr+" * * ?"))
          .forJob("job"+scheduleData.dsid, "analyserGroup")
          .build();
          scheduler.getContext().put("command", "hadoop jar $HADOOP_HOME/share/mavensample-0.0.1-SNAPSHOT-jar-with-dependencies.jar /user/twitterSenti/" + configuringDataSource.tablename + " " + configuringDataSource.tablename)
          scheduler.scheduleJob(job, trigger);
          /*----------------------------------------*/
          val scheduledRecords = AdminModel.readAllAnalysisSchedule  
          if(result>0){
             Ok(views.html.scheduleAnalysis(views.html.adminheader("Successfully Saved",2,reportMenu),scheduleAnalysisForm, sourceSeq, scheduledRecords)) 
          }
          else{
            Ok("Schedule Process Failed")
          }
        }
        catch{
          case ex : PSQLException =>{
            Ok(ex.getMessage.toString())
          }
        }    
      })
    }
    else{
      Redirect(routes.Application.index()).flashing(
                  "error" -> "Your last session expired, Please Login again ")
    }
  }
    
  def deleteAnalysisSchedule(hashId : String) = Action {implicit request =>    
    if(checkSession(request)){
      val reportMenu = GeneralFunctions.loadReportMenuItems
      val result= AdminModel.deleteAnalysisSchedule(hashId)
      val sourceSeq = GeneralFunctions.loadDataSourcesForForm
      val scheduledRecords = AdminModel.readAllAnalysisSchedule      
      Ok(views.html.scheduleAnalysis(views.html.adminheader("Schedule Analysis", 2,reportMenu), scheduleAnalysisForm, sourceSeq, scheduledRecords))
    }
    else{
      Redirect(routes.Application.index()).flashing(
                  "error" -> "Your last session expired, Please Login again ")
    }   
  }
}

class MyWorker extends Job {
  def execute(ctxt: JobExecutionContext) {
//    val str = "hadoop jar $HADOOP_HOME/share/mavensample-0.0.1-SNAPSHOT-jar-with-dependencies.jar /user/twitterSenti/"
//    Runtime.getRuntime.exec(str)    
    val schedulerContext = ctxt.getScheduler.getContext   
    var str =  schedulerContext.get("command")
    val analysisTask = Runtime.getRuntime.exec(str.toString())
    Logger.debug("Analysis triggered at: " + new Date +" with the command: "+ str)
    val analysisExitCode = analysisTask.waitFor()
    if(analysisExitCode==0){
      Logger.info("Analysis Complete successfully at:"+new Date+" Now triggering sqoop")
      val sqoopTask = Runtime.getRuntime.exec("sqoop export --connect jdbc:postgresql://localhost/qsenti --username postgres -password qburst --table sentimentresult --export-dir /user/qburst/sentiment.tsv/part-00000 --fields-terminated-by '\t'")
      val sqoopExitCode = sqoopTask.waitFor()
      if(sqoopExitCode>0){
        Logger.error("Data Transfer From HDFS to PostgreSQL Failed at : " + new Date)
      }
      else{
        Logger.info("Data Moved From HDFS to POstgreSQL Successfully")
      }
    }
    else{
      Logger.error("Analysis Task Failed at:"+new Date+"\n\t\t Data Transfer from HDFS to PostgreSQL cannot be initiated.")
    }
  }
}