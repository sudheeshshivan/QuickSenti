package models

import play.api.db._
import anorm._
import anorm.SqlParser._
import play.api.Play.current
import org.apache.commons.lang3.RandomStringUtils  // For generating Random String
import controllers.AdminArea


case class ConfigurationInfoList(dsid : Int, accountType : String, accountTitle : String, accessKey : String, accessSecret : String, consumerKey : String, consumerSecret : String, keywords : String, processid : Int, filename : String, tablename : String)

case class SentimentTrend(date : String, sentiment : String , retweetCount : Int , sentimentValue : Int, percentage : Int )

case class AnalysisSchedule(dsid : Int, scheduleHr : Int, scheduleMin : Int, accountTitle : String)



object AdminModel {
  
  
  
  
  val configuration ={
    get[Int]("dsid")~
    get[String]("accountType")~
    get[String]("accountTitle")~
    get[String]("accessKey")~
    get[String]("accessSecret")~
    get[String]("consumerKey")~
    get[String]("consumerSecret")~
    get[String]("keywords")~
    get[Int]("processid")~
    get[String]("filename")~
    get[String]("tablename")map {
      case dsid~accountType~accountTitle~accessKey~accessSecret~consumerKey~consumerSecret~keywords~processid~filename~tablename =>  ConfigurationInfoList(dsid, accountType, accountTitle, accessKey, accessSecret, consumerKey, consumerSecret, keywords, processid, filename,tablename)
    }
  }
  
  val sentimentTrendReport = {
    get[String]("date")~
    get[String]("sentiment")~
    get[Int]("retweetCount")~
    get[Int]("sentimentValue")~
    get[Int]("percentage") map {
      case date~sentiment~retweetCount~sentimentValue~percentage => SentimentTrend(date,sentiment,retweetCount,sentimentValue,percentage)
    }
  }
  
  
  val analysisSchedule  = {
    get[Int]("dsid")~
    get[Int]("scheduleHr")~
    get[Int]("scheduleMin")~
    get[String]("accountTitle") map {
      case dsid~scheduleHr~scheduleMin~accountTitle => AnalysisSchedule (dsid,scheduleHr,scheduleMin, accountTitle) 
    }
  }
  
  
  
  def saveConfiguration(accountType : String, accountTitle : String, accessKey : String, accessSecret : String, consumerKey : String, consumerSecret : String, keywords : String, tablename : String) : Int = DB.withConnection { implicit c =>
    val result = SQL("INSERT INTO tbldatasource(accountType, accountTitle, accessKey, accessSecret, consumerKey, consumerSecret, keywords, processid, filename, tablename ) VALUES   ({accoutType}, {accountTitle}, {accessKey}, {accessSecret}, {consumerKey}, {consumerSecret}, {keywords}, {processid}, {filename},{tablename})").on(
        'accoutType    -> accountType,
        'accountTitle  -> accountTitle,
        'accessKey     -> accessKey,
        'accessSecret  -> accessSecret,
        'consumerKey   -> consumerKey,
        'consumerSecret-> consumerSecret,
        'keywords      -> keywords,
        'processid     -> 0,
        'filename      -> RandomStringUtils.randomAlphanumeric(10),
        'tablename     -> tablename
      ).executeUpdate()
    return result
  }
  
  
  
  def updateConfiguration(accountType : String, accountTitle : String, accessKey : String, accessSecret : String, consumerKey : String, consumerSecret : String, keywords : String, tablename : String, hashId: String) : Int = DB.withConnection { implicit c =>
    val result = SQL("UPDATE tbldatasource SET accountType={accoutType}, accountTitle = {accountTitle}, accessKey = {accessKey}, accessSecret = {accessSecret}, consumerKey = {consumerKey}, consumerSecret = {consumerSecret}, keywords = {keywords}, tablename = {tablename} WHERE md5(CAST((dsid)AS text))={hashId} ").on(
        'accoutType    -> accountType,
        'accountTitle  -> accountTitle,
        'accessKey     -> accessKey,
        'accessSecret  -> accessSecret,
        'consumerKey   -> consumerKey,
        'consumerSecret-> consumerSecret,
        'keywords      -> keywords,
        'tablename     -> tablename,
        'hashId        -> hashId
      ).executeUpdate()
    return result
  }
  
  
  
  def readSingleDataSource(hashId : String ) : List[ConfigurationInfoList] = DB.withConnection { implicit c => 
    SQL("SELECT * FROM tbldatasource WHERE md5(CAST((dsid)AS text))={hashId}").on(
    'hashId  -> hashId    
    ).as (configuration *)
  
  }
  
  
  def readAllDataSource : List[ConfigurationInfoList] = DB.withConnection { implicit c =>
    SQL("select * from tbldatasource").as (configuration *)
  }
  
  def readSingleDataSource(dsid : Int) : List[ConfigurationInfoList] = DB.withConnection { implicit c =>
    SQL("select * from tbldatasource WHERE dsid="+dsid).as (configuration *)
  }
  
  def deleteDataSource(hashId : String ) : Int = DB.withConnection { implicit c => 
    val result = SQL("DELETE FROM tbldatasource WHERE md5(CAST((dsid)AS text))={hashId}").on(
      'hashId      -> hashId
    ).executeUpdate()
    return result
  }
  
  def deleteAnalysisSchedule(hashId : String ) : Int = DB.withConnection { implicit c => 
    val result = SQL("DELETE FROM analysisschedule WHERE md5(CAST((dsid)AS text))={hashId}").on(
      'hashId      -> hashId
    ).executeUpdate()
    return result
  }
  
  
  def updateStreamingProcessId(processId : Int, hashId : String ) : Int = DB.withConnection { implicit c => 
    val result = SQL("UPDATE tbldatasource SET processid={processid}  WHERE md5(CAST((dsid)AS text))={hashId}").on(
      'processid   -> processId,
      'hashId      -> hashId
    ).executeUpdate()
    return result
  }
  
 
  
  def readSentimentTrend(startDate : String, endDate : String) : List[SentimentTrend] = DB.withConnection { implicit c =>  
    val qry = """select A.date,B.sentiment,B.retweetcount,B.sentimentvalue,((B.sentimentvalue*100)/A.total) as percentage
    from  (
    select date,sum(sentimentvalue) as total FROM sentimentresult group by date
    ) A
    full outer join (
      select * FROM sentimentresult
    ) B on A.date = B.date WHERE A.date BETWEEN '"""+ startDate +"""' AND '"""+ endDate +"""'  ORDER By date,percentage DESC"""
    SQL(qry).as (sentimentTrendReport *)
  }
  
  
  
  def readPieData(startDate : String, endDate : String) : List[SentimentTrend] = DB.withConnection { implicit c =>  
    val qry = """select A.date,B.sentiment,B.retweetcount,B.sentimentvalue,((B.sentimentvalue*100)/A.total) as percentage
    from  (
    select date,sum(sentimentvalue) as total FROM sentimentresult group by date
    ) A
    full outer join (
      select * FROM sentimentresult
    ) B on A.date = B.date WHERE A.date BETWEEN '"""+ startDate +"""' AND '"""+ endDate +"""'  ORDER By sentiment,percentage DESC"""
    SQL(qry).as (sentimentTrendReport *)
  }
  
  def savescheduleAnalysis(dsid : Int, scheduleHr : Int, scheduleMin : Int) : Int = DB.withConnection { implicit c =>
    val result = SQL("INSERT INTO analysisschedule(dsid, schedulehr, schedulemin) VALUES   ({dsid}, {scheduleHr}, {scheduleMin})").on(
        'dsid    -> dsid,
        'scheduleHr  -> scheduleHr,
        'scheduleMin     -> scheduleMin
      ).executeUpdate()
    return result
  }
  
  def readAllAnalysisSchedule : List[AnalysisSchedule] = DB.withConnection { implicit c =>
    SQL("select A.dsid,A.schedulehr,A.schedulemin,B.accounttitle FROM analysisschedule A INNER JOIN tbldatasource B ON A.dsid=B.dsid").as (analysisSchedule *)
  }
  
}