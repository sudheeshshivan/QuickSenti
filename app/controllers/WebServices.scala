package controllers



import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models.AdminModel
import models.WebServiceModel
import play.api.libs.json._
import scala.collection.immutable.Map
import scala.collection.mutable.HashMap
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import scala.io.Source
import java.io.PrintWriter
import play.api.Play
import java.util.Calendar
import org.quartz.impl._
import org.quartz.Job
import org.quartz.JobKey
import org.quartz.JobExecutionContext
import org.quartz.JobBuilder.newJob
import org.quartz.TriggerBuilder.newTrigger
import org.quartz.CronScheduleBuilder._
import java.util.Date


case class WebServiceInfo(serviceName : String , serivceQry : String)
case class UploadViewInfo(service : Int, linkTitle : String)
case class ChartInfo(dataSource : Int, chartName : String, width : Int, height : Int, xAggregation : String, xField : String, yAggregation : String, yField : String, groupBy : String, color : String, chartType : Int, filterField : List[String], filterCondition : List[String], filterParameter : List[String], htmlObject : List[String], filterAdder : List[String])
case class ReportPage(pageName : String, linkTitle : String, componentType : List[String], defaultValue : List[String], controlName : List[String])
case class GraphToPage(page : Int, charts : List[Int])
case class ScheduleReportBursting(hour : Int, minute : Int, frequency : String, subject : String, contents: String)



object WebServices extends Controller {
  
  /*Form Definition Starts Here */
  
  //  Form For WebService API
  val webServiceAPIForm = Form(
    mapping(
      "serviceName" -> nonEmptyText,
      "serviceQry"  -> nonEmptyText
    )(WebServiceInfo.apply)(WebServiceInfo.unapply)
  )
  
  val uploadViewForm = Form(
    mapping(
        "service" -> number,
        "linkTitle" -> nonEmptyText
    )(UploadViewInfo.apply)(UploadViewInfo.unapply)
  )
  
  val chartCreationForm = Form(
    mapping(
        "dataSource" -> number,
        "chartName"  -> nonEmptyText,
        "width"  -> number(min=1,max=1000),
        "height" -> number(min=1,max=1000),
        "xAggregation" -> text,
        "xField" -> nonEmptyText,
        "yAggregation"  -> text,
        "yField" -> nonEmptyText,
        "groupBy" -> text,
        "color"  -> text,
        "chartType" -> number,
        "filterField" -> list(nonEmptyText),
        "filterCondition" -> list(nonEmptyText),
        "filterParameter" -> list(nonEmptyText),
        "htmlObject" -> list(nonEmptyText),
        "filterAdder" -> list(nonEmptyText)
    )(ChartInfo.apply)(ChartInfo.unapply)
  ) 
  
  val reportPageForm = Form(
    mapping(
        "pageName" -> nonEmptyText,
        "linkTitle" -> nonEmptyText,
        "componentType" -> list(text),
        "defaultValue"  -> list(text),
        "controlName"  -> list(text)
    )(ReportPage.apply)(ReportPage.unapply)
  )
  
  val graphToPageForm = Form(
    mapping(
        "page" -> number,
        "charts" -> list(number)         
    )(GraphToPage.apply)(GraphToPage.unapply)
  )
  
  
  val scheduleReportBurstingForm = Form(
    mapping(
      "hour" -> number.verifying ("Invalid hour entry", f => (f >=0 && f <= 24)),
      "minute" -> number.verifying ("Invalid minute entry", f => (f >=0 && f <= 59)),
      "frequency" -> nonEmptyText,
      "subject" -> nonEmptyText,
      "contents" -> nonEmptyText
    )(ScheduleReportBursting.apply)(ScheduleReportBursting.unapply)
    
  )
  
  /*Form Definition Ends Here*/ 
  
  
//  Load Report Menu
  
  def newServiceAPI = Action {implicit request =>
    if(AdminArea.checkSession(request)){  
      val serviceList = WebServiceModel.readAllServiceAPI
      val reportMenu = GeneralFunctions.loadReportMenuItems
      Ok(views.html.newServiceAPI(views.html.adminheader("New Serive API ", 2,reportMenu),webServiceAPIForm,serviceList))
    }
    else{
      Ok(views.html.main("Session Expired. Please Login",Login.loginForm))
    } 
  }
    
  def doCreateServiceAPI = Action {implicit request =>
    if(AdminArea.checkSession(request)){  
      val reportMenu = GeneralFunctions.loadReportMenuItems
      webServiceAPIForm.bindFromRequest.fold(
      formWithErrors => {        
        val serviceList = WebServiceModel.readAllServiceAPI  
        Ok(views.html.newServiceAPI(views.html.adminheader("New Serive API ", 2, reportMenu),formWithErrors,serviceList))
      },
      apiData => {
        val result = WebServiceModel.saveNewAPI(apiData.serviceName, apiData.serivceQry)
        val serviceList = WebServiceModel.readAllServiceAPI
        if(result>0){
          Ok(views.html.newServiceAPI(views.html.adminheader("Service Create Successfully ", 2,reportMenu),webServiceAPIForm,serviceList))
        }
        else{
          Ok(views.html.adminheader("Service Creation Failed ", 2, reportMenu))
        }
      })
    }
    else{
      Ok(views.html.main("Session Expired. Please Login",Login.loginForm))
    } 
  }
  
  
  def chartApiGenerator = Action { implicit request =>
    if(AdminArea.checkSession(request)){
      //This hash id will be the id of chart
      try{
        val hashId = request.body.asFormUrlEncoded.get.get("hashId").get.head
        val currentChart = WebServiceModel.readSingleChart(hashId).head
        var query = GeneralFunctions.generateQueryForChart(currentChart)
        val filterData = WebServiceModel.readFiltersForChart(hashId)
        val groupByClause = query.substring(query.indexOf("GROUP BY"), query.size) //query.split("GROUP BY")
        query = query.substring(0, query.indexOf("GROUP BY"))
        if(!filterData.isEmpty){
          query += "WHERE"
          for(filter <- filterData){
            var filterValue = request.body.asFormUrlEncoded.get.get(filter.htmlObject).get.head
            if(filter.field=="sentiment"||filter.field=="date"){
              filterValue = "'"+filterValue+"'"
            }
            query +=" "+filter.field+filter.condition+filterValue
            if(filter.filterAdder!="NONE"){
              query += query+" "+filter.filterAdder
            }
          }
        }
        query += " "+groupByClause
        val queryResult = WebServiceModel.processQryToList(query)
        val result = queryResult.map { t =>
          var sMap = Map[String,String]()
          t.foreach {
            f =>
            /*sMap += (f._1.substring(f._1.indexOf(".")+1).toString() ->f._2.toString())*/
            sMap += (f._1.substring(f._1.indexOf(".")+1).toString() -> someExtractor(f._2))
          }
          sMap
        }
        val json_obj = Json.toJson(result)
        Ok(json_obj)
      }
      catch{
        case ex : Exception =>{
          Logger.error(ex.getMessage)
          Redirect(routes.AdminArea.dashboard()).flashing(
                  "error" -> "Invalid Report Selection/ Request")
        }
      }
    }
    else{
      Ok(views.html.main("Session Expired. Please Login",Login.loginForm))
    }    
  }
  
  def apiGenerator(hashId : String/*, inputs : List[String]*/) = Action {implicit request =>
//    if(AdminArea.checkSession(request)){  
      val apiResult = WebServiceModel.processAPIQry(hashId)
      
      val result = apiResult.map { t =>
        var sMap = Map[String,String]()
        t.foreach {
            f =>
              /*sMap += (f._1.substring(f._1.indexOf(".")+1).toString() -> f._2.toString())*/
              sMap += (f._1.substring(f._1.indexOf(".")+1).toString() -> someExtractor(f._2))
        }
        sMap
      }
      val json_obj = Json.toJson(result)
//      val jsonString: String = Json.stringify(result)
      Ok(json_obj)
   /* }
    else{
      Ok(views.html.main("Session Expired. Please Login",Login.loginForm))
    } */
  }
  
  def uploadAPIView = Action { implicit request =>
    if(AdminArea.checkSession(request)){  
      val serviceList = GeneralFunctions.loadWebServicesForForm
      val reportMenu = GeneralFunctions.loadReportMenuItems
      val reportViewList = WebServiceModel.readAllReportView
//      println(reportViewList)
      Ok(views.html.uploadview(views.html.adminheader("Upload New View ", 2,reportMenu),uploadViewForm,serviceList,reportViewList))
    }
    else{
      Ok(views.html.main("Session Expired. Please Login",Login.loginForm))
    } 
  }  
  
  def scheduleReportBursting = Action { implicit request =>
    if(AdminArea.checkSession(request)){  
      val reportMenu = GeneralFunctions.loadReportMenuItems
      val reportBurstingScheduleList = WebServiceModel.readAllreportBurstingSchedule
      
      if(reportBurstingScheduleList.size > 0){
         val reportBurstingSchedule = reportBurstingScheduleList.head
     		 val dataMap = Map("hour" -> reportBurstingSchedule.hour.toString(), "minute" -> reportBurstingSchedule.minute.toString(), "frequency" -> reportBurstingSchedule.frequency, "subject" -> reportBurstingSchedule.subject, "contents" -> reportBurstingSchedule.contents)
     		 val formForEdit = scheduleReportBurstingForm.bind(dataMap)
     		 Ok(views.html.schedulereportbursting(views.html.adminheader("Upload New View ", 2,reportMenu),formForEdit))
      }
      else{
        Ok(views.html.schedulereportbursting(views.html.adminheader("Upload New View ", 2,reportMenu),scheduleReportBurstingForm))
      }
//      println(re portViewList)
    }
    else{
      Ok(views.html.main("Session Expired. Please Login",Login.loginForm))
    }
  }
  
  def doScheduleReportBursting = Action { implicit request =>
    if(AdminArea.checkSession(request)){
      val reportMenu = GeneralFunctions.loadReportMenuItems
      scheduleReportBurstingForm.bindFromRequest().fold(
        formWithError =>{
          Ok(views.html.schedulereportbursting(views.html.adminheader("Upload New View ", 2,reportMenu),formWithError))
        }, 
        scheduleData =>{
          WebServiceModel.saveReportBurstingSchedule(scheduleData.hour, scheduleData.minute, scheduleData.frequency, scheduleData.subject, scheduleData.contents)
          
          /*Scheduling Crone job using Quartz*/
          val scheduler = StdSchedulerFactory.getDefaultScheduler();
          Logger.info("Quarz scheduler starting for Report Bursting Schedule...")
      
          scheduler.start();
          
          val job = newJob(classOf[ReportBurster])
          .withIdentity("jobBurster", "reportBurstingGroup")
          .build();
          val jobKey = new JobKey("jobBurster", "reportBurstingGroup")
          
          if(scheduler.checkExists(jobKey)){      
            Logger.info("Quartz scheduler deleting the job already scheduled for new update.")
            scheduler.deleteJob(jobKey)      
          }
          
          val trigger = newTrigger()
          .withIdentity("triggerBurster", "reportBurstingGroup")
          .withSchedule(cronSchedule("0 "+scheduleData.minute+" "+scheduleData.hour+" "+scheduleData.frequency))
//          .withSchedule(croneSchedule("0 15 10 ? * *"))
          .forJob("jobBurster", "reportBurstingGroup")
          .build();
          
//          scheduler.getContext().put("command", "hadoop jar $HADOOP_HOME/share/mavensample-0.0.1-SNAPSHOT-jar-with-dependencies.jar /user/twitterSenti/" + configuringDataSource.tablename + " " + configuringDataSource.tablename)
          scheduler.scheduleJob(job, trigger);
          Logger.info("Job Scheduled")
          
          /*----------------------------------------*/
          Redirect(routes.AdminArea.dashboard()).flashing(
                  "success" -> "Schedule updated")
        }
      )
    }
    else{
      Ok(views.html.main("Session Expired. Please Login",Login.loginForm))
    }
  }
  
  def doDeleteReportBurstingSchedule = Action { implicit request =>
    if(AdminArea.checkSession(request)){
      if(WebServiceModel.removeReportBurstingSchedule==1){
        Logger.info("Report Bursting Schedule removed properly")
      }
      else{
        Logger.error("Something wrong happend. There were more than one schedules. All are removed")
      }
      Redirect(routes.AdminArea.dashboard()).flashing(
                  "success" -> "Schedule Deleted")
    }
    else{
      Ok(views.html.main("Session Expired. Please Login",Login.loginForm))
    }
  }
  
  def someExtractor(obj:Any) : String = {
    val returnValue = obj match {
      case Some(value) => value 
      case value => value
    } 
    returnValue.toString()
  }
  
  def loadReportView(hashId : String )= Action { implicit request =>
    if((!AdminArea.checkSession(request))&&(!UserArea.checkSession(request))){
      Ok(views.html.main("Session Expired. Please Login",Login.loginForm))
    }
    else{
      try{    
        val userData = GeneralFunctions.getUserData(request)
        var header = views.html.main("QSenti | Login", Login.loginForm) //Initializing with some HtmlContent
        
    	  if(userData.get("userGroup").get==0){
          val reportMenu = GeneralFunctions.loadReportMenuItems     
          header = views.html.adminheader("Report Page", 4,reportMenu)
        }
        else{
          val reportMenu  = GeneralFunctions.loadReportMenuItemsForUser(userData.get("userGroup").get.toString())
          header = views.html.userheader("Report Page",1,reportMenu)
        }
    		Ok(views.html.reportLoader(header,hashId,1))
      }
      catch{
        case ex : Exception =>{
          Logger.error(ex.getMessage)
          Redirect(routes.AdminArea.dashboard()).flashing(
                  "error" -> "Invalid Report Selection/ Request")
        }
      }
    } 
  }  
  
  def loadReportPage(hashId : String) = Action { implicit request =>    
//        Here hashId refers to the report page ID
    if((!AdminArea.checkSession(request))&&(!UserArea.checkSession(request))){
    	Ok(views.html.main("Session Expired. Please Login",Login.loginForm))
    }
    else{
    	try{
        val userData = GeneralFunctions.getUserData(request)
        var header = views.html.main("QSenti | Login", Login.loginForm) //Initializing with some HtmlContent
        if(userData.get("userGroup").get==0){
        	val reportMenu = GeneralFunctions.loadReportMenuItems     
          header = views.html.adminheader("Report Page", 4,reportMenu)
        }
        else{
          val reportMenu  = GeneralFunctions.loadReportMenuItemsForUser(userData.get("userGroup").get.toString())
//          println(userData.get("userGroup"))
          header = views.html.userheader("Report Page",1,reportMenu)
        }
 				Ok(views.html.reportLoader(header,hashId,2))
    	}
    	catch{
      	case ex : Exception =>{
      		Logger.error(ex.getMessage)
      		Redirect(routes.AdminArea.dashboard()).flashing(
      				"error" -> "Invalid Report Selection/ Request")
      	}
    	}
//        Ok(json_obj)
//          Ok(views.html.reports.hello)
//          views.html.barchartexample("Admin area | Dash Board",1,reportMenu)
//          Ok(views.html.reports.barchartexample(json_obj))
//      chartToPage.map{ chart =>
//        println((chart/*.get("chartdata.height")*/))        
//      }
//      println(chartToPage.head)        
    }
  }
  
  def loadReportPageContent(hashId : String, export : String ) = Action { implicit request =>
    try{   
      
      val chartToPage = WebServiceModel.readChartForPage(hashId)
      var jsonList : List[(JsValue,Map[String,Any])]  = List()
      val htmlComponentList = WebServiceModel.readHtmlComponentsForPage(hashId)
      for(currentChart <- chartToPage){
        val filterList = WebServiceModel.readFiltersForChart(GeneralFunctions.getMD5Hash(currentChart.chtid.toString())) //readFilterForChart requires the hash of chart id
        var specMap = Map(
            "width" -> currentChart.width,
            "height" -> currentChart.height,
            "color" -> currentChart.color,
            "chartName" -> currentChart.chartname,
            "chartType" -> currentChart.chartType,
            "chartId" -> currentChart.chtid
            )
        for(filter <- filterList){
          specMap += ("htmlObject" -> filter.htmlObject)
        }
        
        /*val currentChartHash=GeneralFunctions.getMD5Hash(currentChart.chtid.toString())
        val filterData = WebServiceModel.readFiltersForChart(currentChartHash)
        if(!filterData.isEmpty){
          for(filter <- filterData){
            sql +="WHERE "+filter.field+filter.condition+filter.            
          }
        }*/
//        println(sql)
        val query = GeneralFunctions.generateQueryForChart(currentChart)
        val queryResult = WebServiceModel.processQryToList(query)
        val result = queryResult.map { t =>
          var sMap = Map[String,String]()
//          println(t)
          t.foreach {
            f =>
            /*sMap += (f._1.substring(f._1.indexOf(".")+1).toString() -> f._2.toString())*/
            sMap += (f._1.substring(f._1.indexOf(".")+1).toString() -> someExtractor(f._2))
          }
          sMap
        }
        val json_obj = Json.toJson(result)
        jsonList = (json_obj,specMap) :: jsonList
      }
      Ok(views.html.reports.reportpage(jsonList,htmlComponentList,export))
    }
    catch{
      case ex : Exception =>{
        Logger.error(ex.getMessage)
        Redirect(routes.AdminArea.dashboard()).flashing(
                "error" -> "Invalid Report Selection/ Request")
      }
    }
//    Ok("Loading the report page Contents only")
    
  }
  
  def loadReportViewContent(hashId : String, export : String ) = Action { implicit request =>
    try{
        val reportView = WebServiceModel.readSingleReportView(hashId)
        val viewName = reportView.head.filepath.substring(0, reportView.head.filepath.indexOf("."))
        val clazz : Class[ _ ] = Play.current.classloader.loadClass("views.html.reports."+viewName)          
        val reportViewList = WebServiceModel.readAllReportView
//        val adminHeader = views.html.adminheader(viewName, 2,reportMenu)
        val render  = clazz.getDeclaredMethod("render"/*, Play.current.classloader.loadClass("play.twirl.api.Html")*/) //Specify the type of parameter while declare render method
        Ok(render.invoke(clazz/*,adminHeader*/).asInstanceOf[ play.twirl.api.Html ]) // Pass the declared type of arguments
      }
      catch{
        case ex: Exception =>{
          Logger.error(ex.getMessage)
          Redirect(routes.AdminArea.dashboard()).flashing(
                  "error" -> "Invalid Report Selection/ Request")
        }
      }
//    Ok("Loading the report View Contents only")
  }
  
  def chartList = Action { implicit request =>
    if(AdminArea.checkSession(request)){
      val reportMenu = GeneralFunctions.loadReportMenuItems
      val chartList = WebServiceModel.readAllCharts
      Ok(views.html.chartlist(views.html.adminheader("Interactive Report Generator ", 2,reportMenu),chartList))
    }
    else{
      Ok(views.html.main("Session Expired. Please Login",Login.loginForm))
    }
  }
  
  def interactiveReportGenerator = Action { implicit request => 
    if(AdminArea.checkSession(request)){
      val reportMenu = GeneralFunctions.loadReportMenuItems
      val dataSourceList = GeneralFunctions.loadDataSourcesForForm
      Ok(views.html.interactiveReportGenerator(views.html.adminheader("Interactive Report Generator ", 2,reportMenu),chartCreationForm,dataSourceList))
    }
    else{
      Ok(views.html.main("Session Expired. Please Login",Login.loginForm))
    }    
  }
  
  def doInteractiveReportGenerator = Action { implicit request =>
    if(AdminArea.checkSession(request)){
      val reportMenu = GeneralFunctions.loadReportMenuItems
      chartCreationForm.bindFromRequest.fold(
        formWithErrors=>{          
          val dataSourceList = GeneralFunctions.loadDataSourcesForForm
          Ok(views.html.interactiveReportGenerator(views.html.adminheader("Interactive Report Generator ", 2, reportMenu),formWithErrors,dataSourceList))   
        },
        chartData =>{
          val result = WebServiceModel.saveNewChart(chartData.dataSource, chartData.chartName ,chartData.width, chartData.height, chartData.xAggregation, chartData.xField, chartData.yAggregation, chartData.yField, chartData.groupBy,chartData.color, chartData.chartType)
    		  val reportMenu = GeneralFunctions.loadReportMenuItems
          if(result>0){
          	for(i <- 0 to chartData.filterField.size-1){
          		val field = chartData.filterField(i)
      				val condition = chartData.filterCondition(i)
      				val parameter = chartData.filterParameter(i)
      				val htmlObject = chartData.htmlObject(i)
      				val filterAdder = chartData.filterAdder(i)
              val chtid = WebServiceModel.readLastChartId
      				WebServiceModel.saveFilter(field, condition, parameter, filterAdder, htmlObject, chtid)
          	}
        	  Ok(views.html.adminheader("Admin area | Dash Board",1,reportMenu))
          }
          else{
            Logger.debug("Query failed to write data int DB [interactiveReportGenerator]")
            Ok(views.html.adminheader("Admin area | Dash Board",1,reportMenu))            
          }
        })
      
      /*request.body.asFormUrlEncoded.get.get("chartName").get.head
      This extract data from the request without binding with a form*/
    }
    else{
    	Ok(views.html.main("Session Expired. Please Login",Login.loginForm))
    }  
  }
  
  def newReportPage = Action { implicit request =>
    if(AdminArea.checkSession(request)){
      val reportMenu = GeneralFunctions.loadReportMenuItems
      val reportPageList = WebServiceModel.readAllReportPage
      Ok(views.html.newreportpage(views.html.adminheader("Interactive Report Generator ", 2, reportMenu), reportPageForm, reportPageList))
    }
    else{
      Ok(views.html.main("Session Expired. Please Login",Login.loginForm))
    }   
  }
  
  def updateReportPageStatus(hashId : String) = Action { implicit request =>
    if(AdminArea.checkSession(request)){
      WebServiceModel.updateReportPageStatus(hashId)
      val reportMenu = GeneralFunctions.loadReportMenuItems
      val reportPageList = WebServiceModel.readAllReportPage
      Ok(views.html.newreportpage(views.html.adminheader("Interactive Report Generator ", 2, reportMenu), reportPageForm, reportPageList))
    }
    else{
      Ok(views.html.main("Session Expired. Please Login",Login.loginForm))
    }     
  }   
  
  def doNewReportPage = Action { implicit request =>
    if(AdminArea.checkSession(request)){
      val reportMenu = GeneralFunctions.loadReportMenuItems
      reportPageForm.bindFromRequest.fold(
        formWithErrors => {
        	val reportPageList = WebServiceModel.readAllReportPage
          Ok(views.html.newreportpage(views.html.adminheader("Some Errors Occured ", 2,reportMenu),formWithErrors,reportPageList))
        }, 
        reportPageData => {
          val result = WebServiceModel.saveNewReportPage(reportPageData.pageName, reportPageData.linkTitle)
    		  val reportPageList = WebServiceModel.readAllReportPage
          if(result>0){            
            for(i <- 0 to reportPageData.componentType.size-1){
              val componentType = reportPageData.componentType(i)
              val defaultValue = reportPageData.defaultValue(i)
              val controlName = reportPageData.controlName(i)
              val rpgid = WebServiceModel.readLastReportPageId
              WebServiceModel.saveHtmlComponent(componentType, defaultValue, controlName, rpgid)
            }
    			  Ok(views.html.newreportpage(views.html.adminheader("Interactive Report Generator ", 2,reportMenu),reportPageForm,reportPageList))   
          }
          else{
            Ok(views.html.newreportpage(views.html.adminheader("Interactive Report Generator Failed ", 2,reportMenu),reportPageForm,reportPageList))
          }
        }
      )
    }
    else{
      Ok(views.html.main("Session Expired. Please Login",Login.loginForm))
    }   
  }
  
  def graphToPage = Action { implicit request =>
    if(AdminArea.checkSession(request)){
      val reportMenu = GeneralFunctions.loadReportMenuItems
      val reportPageSeq = GeneralFunctions.loadReportPagesForForm
      val chartList = WebServiceModel.readAllCharts
      Ok(views.html.addgraphtopage(views.html.adminheader("Add Graph To Page", 2,reportMenu), graphToPageForm, reportPageSeq,chartList))
    }
    else{
      Ok(views.html.main("Session Expired. Please Login",Login.loginForm))
    } 
  }
  
  def doAddGraphToPage = Action { implicit request =>
    if(AdminArea.checkSession(request)){
      val reportMenu = GeneralFunctions.loadReportMenuItems
    	val reportPageSeq = GeneralFunctions.loadReportPagesForForm
 			val chartList = WebServiceModel.readAllCharts      
      graphToPageForm.bindFromRequest.fold(
        formWithErrors => {
          Logger.error(formWithErrors.errors.toString())
      	  Ok(views.html.addgraphtopage(views.html.adminheader("Error in chart adding to form ", 2,reportMenu),formWithErrors,reportPageSeq,chartList))
          
        }, 
        chartToPageData => {
          WebServiceModel.saveChartToPage(chartToPageData.page, chartToPageData.charts)
           Ok(views.html.addgraphtopage(views.html.adminheader("Successfully added chart to form ", 2,reportMenu),graphToPageForm,reportPageSeq,chartList))
        }
      )
    }
    else{
      Ok(views.html.main("Session Expired. Please Login",Login.loginForm))
    } 
  }
  
    
  def doUploadView = Action(parse.multipartFormData) { implicit request =>
    if(AdminArea.checkSessionWithMultipart(request.session)){  
      val reportMenu = GeneralFunctions.loadReportMenuItems
      request.body.file("viewFile").map { viewFile =>
        import java.io.File
        val filename = viewFile.filename
        val contentType = viewFile.contentType
        val serviceList = GeneralFunctions.loadWebServicesForForm
        uploadViewForm.bindFromRequest.fold(
          formWithErrors => {  
            val reportViewList = WebServiceModel.readAllReportView
            Ok(views.html.uploadview(views.html.adminheader("Upload View Error", 2,reportMenu), formWithErrors, serviceList,reportViewList))
          }, 
          uploadViewData => {      
            try{
            	viewFile.ref.moveTo(new File("app/views/reports/"+uploadViewData.linkTitle + ".scala.html"),true)
              var viewContent = """
              @()
                  adminHeader
              """ 
              for(line <- Source.fromFile("app/views/reports/"+uploadViewData.linkTitle + ".scala.html").getLines()){
                viewContent += """
                """+line
              }              
              val writer = new PrintWriter("app/views/reports/"+uploadViewData.linkTitle + ".scala.html")
              writer.write(viewContent)
              writer.close()
              
            	WebServiceModel.saveNewView(uploadViewData.service, uploadViewData.linkTitle, filename)
            	val reportViewList = WebServiceModel.readAllReportView
            	Ok(views.html.uploadview(views.html.adminheader("View Uploaded Successfully ", 2, reportMenu), uploadViewForm, serviceList,reportViewList))
            }
            catch{
              case ex : Exception =>{
                Logger.debug(ex.getMessage);
                Redirect(routes.Application.index).flashing(
                  "error" -> "Missing file")
              }
            }
          }
        )
      }.getOrElse {
        Redirect(routes.Application.index).flashing(
          "error" -> "Missing file")
          Ok("Errror")
      }
    }
    else{
      Ok(views.html.main("Session Expired. Please Login",Login.loginForm))
    } 
  }  
  
  
  def removeGraphFromPage = Action { implicit request =>
    if(AdminArea.checkSession(request)){
      val reportMenu = GeneralFunctions.loadReportMenuItems
      val reportPageList = WebServiceModel.readAllReportPage
      Ok(views.html.removeGraphFromPage(views.html.adminheader("Remove Graph From Page", 2,reportMenu),reportPageList)) 
    }
    else{
      Ok(views.html.main("Session Expired. Please Login",Login.loginForm))
    }
  }
  
  def apiReadGraphAssignedToPage(hashId : String) = Action {implicit request =>
    if(AdminArea.checkSession(request)){
//      Hash ID Refers to the page id
      if(hashId=="0"){
        Ok("Please select a page.")
      }
      else{        
        val assignedGraphs = WebServiceModel.readChartForPage(hashId)
        Ok(views.html.graphsAssignedToPage(assignedGraphs))
      }
    }
    else{
      Ok(views.html.main("Session Expired. Please Login",Login.loginForm))
    }
  }
  
  def apiReadPrivilageAssignedToGroup(hashId : String) = Action {implicit request =>
    if(AdminArea.checkSession(request)){
//      Hash ID Refers to the userGroup ID
      if(hashId=="0"){
        Ok("Please select a group.")
      }
      else{        
        import models.UserManagerModel
        val assignedPrivilage = UserManagerModel.readReportListAssigned(hashId)
        var privilagedList : List[(Any,String)] = List() 
        for(privilage <- assignedPrivilage){
          privilagedList =  (privilage.get("userprevilage.plgid"),someExtractor(privilage.get("reportpage.pagename").get)) :: privilagedList
//          println(someExtractor(privilage.get("reportpage.pagename").get))
        }
//        val assignedGraphs = WebServiceModel.readChartForPage(hashId)
        Ok(views.html.privilageAssignedToGroup(privilagedList))
      }
    }
    else{
      Ok(views.html.main("Session Expired. Please Login",Login.loginForm))
    }
  }
  
  
  def apiReadReportForPrevilage(reportType : Int) = Action { implicit request =>
    if(AdminArea.checkSession(request)){
      
    	var reportDisplayList : List[(String,Int)] = List()
      if(reportType==1){
        val reportList = WebServiceModel.readAllReportView
        for(reportView <- reportList){  
          reportDisplayList = (reportView.linkTitle,reportView.vid) :: reportDisplayList
        }
        Ok(views.html.reportsForPrevilage(reportDisplayList))
      }
      else{
        val reportList = WebServiceModel.readAllReportPage
        for(reportPage <-reportList){
          reportDisplayList = (reportPage.linkTitle,reportPage.rpgid) :: reportDisplayList
        }
        Ok(views.html.reportsForPrevilage(reportDisplayList))
      }
    }
    else{
      Ok(views.html.main("Session Expired. Please Login",Login.loginForm))
    }
  }
  
  def doRemoveGraphFromPage = Action { implicit request =>
    if(AdminArea.checkSession(request)){
      val reportMenu = GeneralFunctions.loadReportMenuItems
      val reportPageList = WebServiceModel.readAllReportPage
      val chartsToRemove = request.body.asFormUrlEncoded.get.get("hashId").get
//      println(chartsToRemove)
      for(chartAssignmentToRemove <- chartsToRemove){
        val result = WebServiceModel.removeChartsFromPage(chartAssignmentToRemove)
      }
      Ok(views.html.removeGraphFromPage(views.html.adminheader("Remove Graph From Page | Item Removed", 2,reportMenu),reportPageList))
    } 
    else{
      Ok(views.html.main("Session Expired. Please Login",Login.loginForm))
    }
  }
  
  def doRemoveGraph(hashId : String) = Action { implicit request =>
      if(AdminArea.checkSession(request)){
        val result = WebServiceModel.removeChart(hashId)
        if(result>0){   
          Redirect(routes.WebServices.chartList()).flashing(
            "success" -> "Chart Deleted Successully")
        }
        else{
          Redirect(routes.WebServices.chartList()).flashing(
                  "error" -> "This Chart cannot remove as it has some references..")
        }
      }
      else{
        Ok(views.html.main("Session Expired. Please Login",Login.loginForm))
      }
  }
  
  def doRemoveService(hashId : String) = Action { implicit request =>
      if(AdminArea.checkSession(request)){
        val result = WebServiceModel.removeService(hashId)
        if(result>0){   
          Redirect(routes.AdminArea.dashboard()).flashing(
            "success" -> "Service Deleted Successully")
        }
        else{
          Redirect(routes.AdminArea.dashboard()).flashing(
                  "error" -> "Some error has occured while deleting this service")
        }
      }
      else{
        Ok(views.html.main("Session Expired. Please Login",Login.loginForm))
      }
  }
}


class ReportBurster extends Job {
  def execute(ctxt: JobExecutionContext) {

    import play.api.libs.mailer._
    import play.api.Play.current    
    import java.io.File
    import io.github.cloudify.scala.spdf._
    import java.net._
    import models.UserManagerModel
    
    
//    USIGN sPDF Libarary
    val pdf = Pdf(new PdfConfig {
      orientation := Landscape
      pageSize := "Letter"
      marginTop := "1in"
      marginBottom := "1in"
      marginLeft := "1in"
      marginRight := "1in"
    })
    val reportMenu = GeneralFunctions.loadReportMenuItems
    for(menu <- reportMenu){
      if(menu._3==1){
        Logger.info("Processing PDF from View")
        pdf.run(new URL("http://localhost:9000/WebService/reports/show/content/"+GeneralFunctions.getMD5Hash(menu._2.toString())+"/export"), new File("public/pdf_list/"+menu._2+menu._1+ ".pdf"))
//        println("http://localhost:9000/WebService/reports/show/content/"+GeneralFunctions.getMD5Hash(menu._2.toString())+"/export")
      }
      else if(menu._3==2){
        Logger.info("Processing PDF from Report Page")
        pdf.run(new URL("http://localhost:9000/WebService/reports/show_page/content/"+GeneralFunctions.getMD5Hash(menu._2.toString())+"/export"), new File("public/pdf_list/"+menu._2+menu._1+".pdf"))
      }
    }
    val reportBurstingSchedule = WebServiceModel.readAllreportBurstingSchedule.head
    val mailContent = reportBurstingSchedule.contents
    val mailSubject = reportBurstingSchedule.subject
    val userList = UserManagerModel.readAllUser
    userList.map { userData => 
        val permissionList = GeneralFunctions.loadReportMenuItemsForUser(userData.userGroupId.toString())
        val attachmentSeq = permissionList.map(
          permission =>
          AttachmentFile(permission._1+".pdf", new File("public/pdf_list/"+permission._2+permission._1+".pdf"))
        )
        
        val email = Email(
          mailSubject,
          "Sabu <sabulbs@gmail.com>",
          Seq(userData.username+" <"+userData.email+">"),
          attachments = attachmentSeq,  
          bodyHtml = Some("""
              <html>
              <body>
              """+
              mailContent
              +"""
              </body>
              </html>
              """)
        )
        MailerPlugin.send(email)
    }
    
    /*READING Data from the argument "command" and then process
     * val schedulerContext = ctxt.getScheduler.getContext   
    var str =  schedulerContext.get("command")
    Runtime.getRuntime.exec(str.toString())*/
    Logger.debug("Report Bursting Triggered at: " + new Date )
  }
}
  
  /*implicit val creatureWrites = new Writes[List[Map[String, Any]]] {
    def writes(c: List[Map[String, Any]]):  JsValue = {
        val re = Map()
        c.keySet.map { x => re += x -> c.get(x)_ }
//      Json.toJson(c.head)
        
    }
  }*/
  
 /* implicit val creatureWrites = new Writes[List[Map[String, Any]]] {
  def writes(c: List[Map[String, Any]]):  JsValue = {
    Json.toJson(c.map {
      f => 
      val rMap = new HashMap[String,String]();
      f.foreach(t => rMap += (t._1 -> t._2.toString()) )
      val v = JsValue(rMap);
      rMap
      
    })*/
      //val r:JsArray = new JsArray()
//    c.map ( t =>
        //println(t)
//      t.
//      ) 
    //}
  //  r
/*    val json = new Jsonv(); 
//        Json.obj(f._1 -> f._2.toString())
  t.foreach(f => {
    println(f._1 + " : "+f._2.toString())
    println(json)
    r.append(json)
    println(r)
*/  /*}
}*/








     /* val result = apiResult.map { t =>
        val map = HashMap[String,String]()
        t.foreach {
            f => 
              map +=  (f._1.substring(f._1.indexOf(".")+1) -> f._2.toString() ) 
            //Map("username" -> t.username, "tweet" -> t.tweet, "date" -> t.date)
        }
        println(map)
        map
      }
      println(result)*/
//      val str = Json.toJson(apiResult.head)
//      val gson = new Gson();
//      gson.toJson(apiResult);
      /*var str = "["
      for(singleMap <- apiResult ){
        str = str + "{"
        for(item <- singleMap){        
        str = str + """
         """"+item._1+"""": """"+item._2+"""",""" 
        }
        str = str.stripPrefix(",").stripSuffix(",").trim()
        str = str + "},"
      }
      str = str.stripPrefix(",").stripSuffix(",").trim()
      str = str + "]"*/
//      val json_obj = Json.parse(str)
//      Ok(gson.toJson(apiResult.))