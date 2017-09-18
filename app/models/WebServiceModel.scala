package models

import play.api.db._
import anorm._
import anorm.SqlParser._
import play.api.Play.current
import org.postgresql.util.PSQLException
import play.Logger

case class ServiceList(apiid : Int, serviceName : String , serviceQry : String)
case class ReportViewList(vid : Int, apiid : Int, serviceName : String, linkTitle : String, filepath : String)
case class ChartList(ctdid :Int, chtid : Int, chartname : String, datasource : Int, dataSourceName : String, width : Int, height : Int, xAggregator : String, xField : String, yAggregator : String, yField : String, groupBy : String, color : String, chartType : Int )
case class ReportPageList(rpgid : Int, pageName : String, linkTitle : String, status : Int)
case class FilterList(ftrid : Int, field : String, condition : String, parameter : String, filterAdder : String, htmlObject : String, chtid : Int )
case class HtmlComponentList(hcdid : Int , componentType : String, defaultValue : String, controlName : String)
case class ReportBurstingScheduleList(rpsid : Int, hour : Int, minute : Int, frequency : String, subject : String, contents : String)

object WebServiceModel {
  
  val serviceList = {  
    get[Int]("apiid")~
    get[String]("servicename")~
    get[String]("serviceqry") map {
      case apiid~servicename~serviceqry => ServiceList(apiid,servicename,serviceqry)
    }
  }  
  
  val reportViewList = {
    get[Int]("vid")~
    get[Int]("apiid")~
    get[String]("servicename")~
    get[String]("linktitle")~
    get[String]("filepath") map {
      case vid~apiid~servicename~linktitle~filepath => ReportViewList(vid,apiid,servicename,linktitle,filepath) 
    }
  }

  val filterList ={
    get[Int]("ftrid")~
    get[String]("field")~
    get[String]("condition")~
    get[String]("parameter")~
    get[String]("filterAdder")~
    get[String]("htmlObject")~
    get[Int]("chtid") map {
      case ftrid~field~condition~parameter~filterAdder~htmlObject~chtid => FilterList(ftrid,field,condition,parameter,filterAdder,htmlObject,chtid)
    }
  }
  
  val htmlComponentList = {
    get[Int]("hcdid")~
    get[String]("componentType")~
    get[String]("defaultValue")~
    get[String]("controlName") map {
      case hcdid~componentType~defaultValue~controlName => HtmlComponentList(hcdid,componentType,defaultValue,controlName)
    }
  }
  
  val chartList = {
    get[Int]("ctdid")~
    get[Int]("chtid")~
    get[String]("chartname")~
    get[Int]("datasource")~
    get[String]("datasourceName")~
    get[Int]("width")~
    get[Int]("height")~
    get[String]("xaggregator")~
    get[String]("xfield")~
    get[String]("yaggregator")~
    get[String]("yfield")~
    get[String]("groupby")~
    get[String]("color")~
    get[Int]("charttype") map {
      case ctdid~chtid~chartname~datasource~datasourceName~width~height~xaggregator~xfield~yaggregator~yfield~groupby~color~charttype => ChartList(ctdid,chtid,chartname,datasource,datasourceName,width,height,xaggregator,xfield,yaggregator,yfield,groupby,color,charttype)
    }
  }
  
  val reportPageList = {
    get[Int]("rpgid")~
    get[String]("pagename")~
    get[String]("linktitle")~
    get[Int]("status") map {
      case rpgid~pagename~linktitle~status => ReportPageList(rpgid,pagename,linktitle,status)
    }
  }  
  
  val reportBurstingScheduleList = {
    get[Int]("rbsid")~
    get[Int]("hour")~
    get[Int]("minute")~
    get[String]("frequency")~
    get[String]("subject")~
    get[String]("contents") map {
      case rbsid~hour~minute~frequency~subject~contents => ReportBurstingScheduleList(rbsid,hour,minute,frequency,subject,contents)
    }
  }
  
  def saveNewAPI(serviceName : String, serviceQry : String ) : Int = DB.withConnection { implicit c =>
  val result = SQL("INSERT INTO serviceapi(servicename,serviceqry) VALUES ({servicename},{serviceqry})").on(
        'servicename  -> serviceName,
        'serviceqry  -> serviceQry
      ).executeUpdate()
    return result
  }
  
  def readAllServiceAPI : List[ServiceList] = DB.withConnection { implicit c =>
    SQL("SELECT * FROM serviceapi").as(serviceList *)
  }
  
  def readAllReportView : List[ReportViewList] = DB.withConnection { implicit c => 
    SQL("""SELECT A.vid,A.apiid,B.servicename,A.linktitle,A.filepath FROM reportview as A
INNER JOIN serviceapi B on A.apiid=B.apiid""").as(reportViewList *)
  }  
  
  
  def readAllReportViewForUser(groupId : String) : List[ReportViewList] = DB.withConnection { implicit c =>
    SQL("""SELECT A.vid,A.apiid,B.servicename,A.linktitle,A.filepath FROM reportview as A
INNER JOIN serviceapi B on A.apiid=B.apiid WHERE md5(CAST((A.vid)AS text)) 
in (SELECT report FROM userprevilage WHERE reporttype=1 AND groupid={groupid})""")
    .on(
    'groupid -> groupId    
    )
    .as(reportViewList *)
  }
  
  def readAllreportBurstingSchedule : List[ReportBurstingScheduleList] = DB.withConnection { implicit c =>
    SQL("SELECT * FROM reportburstingschedule").as(reportBurstingScheduleList *)    
  }
  
  def readAllCharts : List[ChartList] = DB.withConnection { implicit c =>
    SQL("""SELECT 0 as ctdid,A.chtid,A.datasource,A.chartname,A.width,A.height,A.xaggregator,A.xfield,A.yaggregator,A.yfield,A.groupby,B.accounttitle as datasourceName,A.color,A.charttype FROM chartdata A INNER JOIN tbldatasource B on A.datasource=B.dsid""").as(chartList *)    
  }  
  
  def readSingleChart(hashId : String) : List[ChartList] = DB.withConnection { implicit c =>
    SQL("""SELECT 0 as ctdid,A.chtid,A.datasource,A.chartname,A.width,A.height,A.xaggregator,A.xfield,A.yaggregator,A.yfield,A.groupby,B.accounttitle as datasourceName,A.color,A.charttype FROM chartdata A INNER JOIN tbldatasource B on A.datasource=B.dsid
    WHERE md5(CAST((chtid)AS text))={hashId}""").on(
    'hashId  -> hashId    
    ).as(chartList *)    
  }
  
  def readAllReportPage : List[ReportPageList] = DB.withConnection { implicit c =>
    SQL("""SELECT * FROM reportpage""").as(reportPageList *)    
  }  
  
  def readAllPublishedReportPage : List[ReportPageList] = DB.withConnection { implicit c =>
    SQL("""SELECT * FROM reportpage WHERE status=1""").as(reportPageList *)    
  }
  
  def readAllPublishedReportPageForUser(groupId : String) : List[ReportPageList] = DB.withConnection { implicit c =>
    SQL("""SELECT * FROM reportpage WHERE status=1 AND md5(CAST((rpgid)AS text)) 
      in (SELECT report FROM userprevilage WHERE reporttype=2 AND groupid={groupid})""")
    .on(
    'groupid -> groupId    
    )
    .as(reportPageList *)    
  }
  
  def readSingleReportView(hashId : String ) : List[ReportViewList] = DB.withConnection { implicit c => 
    SQL("""SELECT A.vid,A.apiid,B.servicename,A.linktitle,A.filepath FROM reportview as A
INNER JOIN serviceapi B on A.apiid=B.apiid WHERE md5(CAST((vid)AS text))={hashId}""").on(
    'hashId  -> hashId    
    ).as (reportViewList *)
  } 
  
  def readHtmlComponentsForPage(hashId : String ) : List[HtmlComponentList] = DB.withConnection { implicit c => 
    SQL("""SELECT  hcdid,componenttype,defaultvalue,controlname FROM htmlcomponentdata WHERE md5(CAST((rpgid) AS text))={hashId}""").on(
    'hashId  -> hashId    
    ).as (htmlComponentList *)
  }  
  
  def readFiltersForChart(hashId : String ) : List[FilterList] = DB.withConnection { implicit c => 
    SQL("""SELECT  * FROM filterdata WHERE md5(CAST((chtid) AS text))={hashId} ORDER BY(ftrid)""").on(
    'hashId  -> hashId    
    ).as (filterList *)
  }  
  
  def updateReportPageStatus(hashId : String) = DB.withConnection { implicit c =>
    try{      
      SQL("UPDATE reportpage SET status = (CASE status WHEN 1 THEN 0 ELSE 1 END) where md5(CAST((rpgid)AS text))={hashId}")
      .on( 'hashId  -> hashId)
      .executeUpdate()    
    }
    catch{
      case ex : PSQLException => {
        Logger.debug(ex.getMessage)        
      }
    }
  }  
  
  def processAPIQry(hashId : String): List[Map[String,Any]] = DB.withConnection { implicit c =>
    val queryToExecute = SQL("SELECT * FROM serviceapi WHERE md5(CAST((apiid)AS text))={hashId}").on(
    'hashId  -> hashId    
    )
    val apiQry =queryToExecute().map(row => row).head.asList.drop(2).head.toString().stripPrefix("Some(").stripSuffix(")").trim()
    val sql = SQL(apiQry)    
   
    val result = sql().map(row =>      
      row.asMap
    ).toList
    return result; 
//    val sql =SQL("SELECT date,sentimentvalue FROM sentimentresult")
//    val result=sql().map ( row =>
//      row[String]("date") -> row[Int]("sentimentvalue")
//    )
//    println(result)
//    
//  println(sql())
//    val rowCollection =sql().head
//    println(rowCollection.asList(0)[String])
//    for(singleRow <- rowCollection){
//      println(singleRow.asList(0))
//    }
//    val forTestMap =  List(Map(""->10))
//    forTestMap
  }   
  
  def saveNewView(apiid : Int, linkTitle : String, filename : String ) : Int = DB.withConnection { implicit c =>
    try{
      val result = SQL("INSERT INTO reportview(apiid,linktitle,filepath) VALUES ({apiid},{linktitle},{filepath})").on(
          'apiid  -> apiid,
          'linktitle  -> linkTitle,
          'filepath  -> filename
          ).executeUpdate()
          return result
    }
    catch{
      case ex : PSQLException => {
        Logger.debug(ex.getMessage)
        return 0
      }
    }
  }  
  
  def saveNewChart(datasource : Int,chartName : String, width : Int, height : Int, xAggregator : String, xField : String, yAggregator : String, yField : String, groupBy : String, color : String, chartType : Int) : Int = DB.withConnection { implicit c =>
    try{
      val result = SQL("INSERT INTO chartdata(datasource,chartname,width,height,xaggregator,xfield,yaggregator,yfield,groupby,color,charttype) VALUES ({datasource},{chartname},{width},{height},{xaggregator},{xfield},{yaggregator},{yfield},{groupby},{color},{charttype})").on(
          'datasource  -> datasource,
          'chartname   -> chartName,
          'width  -> width,
          'height  -> height,
          'xaggregator  -> xAggregator,
          'xfield  -> xField,
          'yaggregator  -> yAggregator,
          'yfield  -> yField,
          'groupby  -> groupBy,
          'color  ->  color,
          'charttype  -> chartType
          ).executeUpdate()
          return result
    }
    catch{
      case ex : PSQLException => {
        Logger.debug(ex.getMessage)
        return 0
      }
    }
  }     
  
  def saveNewReportPage(pageName : String, linkTitle : String ) : Int = DB.withConnection { implicit c =>
    try{
      val result = SQL("INSERT INTO reportpage(pagename, linktitle,status) VALUES ({pagename},{linktitle},{status})").on(
          'pagename  -> pageName,
          'linktitle  -> linkTitle,
          'status     -> 0
          ).executeUpdate()
          return result
    }
    catch{
      case ex : PSQLException => {
        Logger.debug(ex.getMessage)
        return 0
      }
    }
  }
  
  def saveReportBurstingSchedule(hour:Int, minute : Int, frequency : String, subject : String, contents : String) : Int = DB.withConnection { implicit c =>
    try{      
//      if(SQL("SELECT count(rbsid)"))
      var result : Int = 0 
      val qry=SQL("SELECT count(rbsid) FROM reportburstingschedule")
      qry().map { row =>
        if((row.asMap.get(".count").get).toString().toInt > 0){
          result = SQL("UPDATE reportBurstingSchedule SET hour={hour}, minute={minute}, frequency={frequency}, subject={subject}, contents={contents}").on(
            'hour  -> hour,
            'minute  -> minute,
            'frequency     -> frequency,
            'subject  -> subject,
            'contents  -> contents
            ).executeUpdate()
        }else{
        	result = SQL("INSERT INTO reportBurstingSchedule(hour, minute, frequency, subject, contents) VALUES ({hour},{minute},{frequency},{subject},{contents})").on(
      			'hour  -> hour,
      			'minute  -> minute,
      			'frequency     -> frequency,
      			'subject  -> subject,
      			'contents  -> contents
      			).executeUpdate()
        }
      }
      result
    }
    catch{
      case ex : PSQLException => {
        Logger.debug(ex.getMessage)
        return 0
      }
    }
  }
  
  def saveFilter(field : String, condition : String, parameter : String, filterAdder : String, htmlObject : String, chtid : Int ) : Int = DB.withConnection { implicit c =>
    try{
      val result = SQL("INSERT INTO filterdata(field,condition,parameter,filteradder,htmlobject,chtid) VALUES ({field},{condition},{parameter},{filteradder},{htmlobject},{chtid})").on(
          'field  -> field,
          'condition  -> condition,
          'parameter     -> parameter,
          'filteradder  -> filterAdder,
          'htmlobject  -> htmlObject,
          'chtid  -> chtid
          ).executeUpdate()
          return result
    }
    catch{
      case ex : PSQLException => {
        Logger.debug(ex.getMessage)
        return 0
      }
    }
  }  
   
  def saveHtmlComponent(componentType : String, defaultValue : String,  controlName : String, rpgid : Int) : Int = DB.withConnection { implicit c => 
    try{
      val result = SQL("INSERT INTO htmlcomponentdata(componenttype,defaultvalue,controlname,rpgid) VALUES ({componenttype},{defaultvalue},{controlname},{rpgid})").on(
          'componenttype -> componentType,
          'defaultvalue -> defaultValue,
          'controlname     -> controlName,
          'rpgid  -> rpgid
          ).executeUpdate()
          return result
    }
    catch{
      case ex : PSQLException => {
        Logger.debug(ex.getMessage)
        return 0
      }
    }
  }
  
  def saveChartToPage(page : Int, charts : List[Int] ) : Int = DB.withConnection { implicit c =>
    try{
      val result = charts.map { chart => SQL("INSERT INTO charttopage(page, chart) VALUES ({page},{chart})").on(
          'page  -> page,
          'chart  -> chart
          ).executeUpdate()
      }      
      return result.reverse.head
    }
    catch{
      case ex : PSQLException => {
        Logger.debug(ex.getMessage)
        return 0
      }
    }
  }  
  
  def readLastChartId : Int = DB.withConnection { implicit c =>
    try{
      val result = SQL("SELECT MAX(chtid) as id FROM chartdata").apply().head
      result[Int]("id")
    }
    catch{
      case ex : PSQLException => {
        Logger.debug(ex.getMessage)
        return 0
      }
    }
//    val result = SQL("SELECT date FROM sentimentresult").apply().head
//    result[String]("date")
  }  
  
  def readLastReportPageId : Int = DB.withConnection { implicit c =>
    try{
      val result = SQL("SELECT MAX(rpgid) as id FROM reportpage").apply().head
      result[Int]("id")
    }
    catch{
      case ex : PSQLException => {
        Logger.debug(ex.getMessage)
        return 0
      }
    }
  }
  
  def readChartForPage(hashId : String) : List[ChartList]/*List[Map[String,Any]]*/ = DB.withConnection { implicit c => 
    val result = SQL("""
    select A.ctdid, A.chart as chtid,B.datasource, C.accounttitle as datasourceName, B.chartname,B.width,B.height,B.xaggregator,B.xfield,B.yaggregator,B.yfield,B.groupBy,B.color,B.charttype 
        from charttopage A 
        INNER JOIN chartdata B 
          on B.chtid=A.chart 
        INNER JOIN tbldatasource C 
          on C.dsid=B.datasource  
        where md5(CAST((page)AS text))={hashId}
        ORDER BY chtid ASC
    """
    ).on(
    'hashId  -> hashId    
    ).as(chartList *)
    
    result
    
    /*result().head.asList.head
    result().map( row => 
      row.asMap
    ).toList*/
    
  } 
  
  
  def processQryToList(query : String): List[Map[String,Any]]=DB.withConnection { implicit c =>
    val sql = SQL(query)    
//    println(sql())
    val result = sql().map(row => 
      row.asMap
    ).toList
    return result;
  }
  
  def removeChartsFromPage(hashId : String) : Int = DB.withConnection { implicit c =>
    try{      
      val result = SQL("DELETE FROM charttopage WHERE md5(CAST((ctdid)AS text))={hashId}").on(
          'hashId      -> hashId
          ).executeUpdate()
          return result
    }
    catch{
      case ex : PSQLException => {
        Logger.debug(ex.getMessage)
        return 0
      }
    }
  }
  
  def removeReportBurstingSchedule : Int = DB.withConnection { implicit c =>
    try{
      val result = SQL("DELETE FROM reportburstingschedule").executeUpdate()
      result
    }
    catch{
      case ex : PSQLException => {
        Logger.debug(ex.getMessage)
        return 0
      }
    }
  }
  
  def removeChart(hashId : String) : Int = DB.withConnection { implicit c =>
    try{      
      val query = SQL("""DELETE FROM chartdata WHERE md5(CAST((chtid) AS text))={hashId} AND chtid NOT IN (SELECT chart from charttopage)""").on(
          'hashId      -> hashId
          )
          val result = query.executeUpdate()
          return result
    }
    catch{
      case ex : PSQLException => {
        Logger.debug(ex.getMessage)
        return 0
      }
    }
  }
  
  def removeService(hashId : String) : Int = DB.withConnection { implicit c =>
    try{      
      val query = SQL("""DELETE FROM serviceapi WHERE md5(CAST((apiid) AS text))={hashId}  AND apiid NOT IN (SELECT apiid FROM reportview)""").on(
          'hashId      -> hashId
          )
          val result = query.executeUpdate()
          return result
    }
    catch{
      case ex : PSQLException => {
        Logger.debug(ex.getMessage)
        return 0
      }
    }
  }
  
  
}