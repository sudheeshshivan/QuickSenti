package controllers

import java.security.MessageDigest
import models.AdminModel
import models.UserManagerModel
import models.WebServiceModel
import scala.collection.Seq
import models.ChartList
import play.api.mvc._

object GeneralFunctions {

  def getMD5Hash(input : String ) : String = {
    return MessageDigest.getInstance("MD5").digest(input.getBytes).map("%02x".format(_)).mkString
  }
  
  def loadDataSourcesForForm : Seq[(String,String)] = {    
    val dataSource = AdminModel.readAllDataSource
//    var sourceSeq : Seq[(String,String)] = null
    var sourceSeq = Seq(""->"Select a Datasource")
    for(singleData <- dataSource){
      sourceSeq  = sourceSeq ++ Seq(singleData.dsid.toString() -> singleData.accountTitle)
    }
    return sourceSeq
  } 
  
  
  def loadUserGroupForForm : Seq[(String,String)] = {    
    val groupList = UserManagerModel.readAllUserGroup
    var groupSeq = Seq(""->"Select a Group")
    for(singleData <- groupList){
      groupSeq  = groupSeq ++ Seq(singleData.grpid.toString -> singleData.groupName)
    }
    return groupSeq
  } 
  
  
  def loadUsersForForm : Seq[(String,String)] ={
    val userList = UserManagerModel.readAllUser
    var userSeq = Seq(""->"Select a User")
    for(singleUser <- userList){
      userSeq = userSeq ++ Seq(getMD5Hash(singleUser.uid.toString()) -> singleUser.username)
    }
    userSeq
  }
  
  
  def loadWebServicesForForm : Seq[(String,String)] = {    
    val serviceList = WebServiceModel.readAllServiceAPI
    var serviceSeq = Seq(""->"Select a Group")
    for(singleData <- serviceList){
      serviceSeq  = serviceSeq ++ Seq(singleData.apiid.toString() -> singleData.serviceName)
    }
    return serviceSeq
  } 
  
  def loadReportPagesForForm : Seq[(String,String)] = {    
    val reportPageList = WebServiceModel.readAllReportPage
    var reportPageSeq = Seq(""->"Select a Group")
    for(singleData <- reportPageList){
      reportPageSeq  = reportPageSeq ++ Seq(singleData.rpgid.toString() -> singleData.pageName)
    }
    return reportPageSeq
  } 
  
  def loadReportMenuItems : List[(String,Int,Int)] = {
    val reportViewList  = WebServiceModel.readAllReportView
    val reportPageList = WebServiceModel.readAllPublishedReportPage
    var reportMenuList : List[(String,Int,Int)] = List()
    for(reportView <- reportViewList){  
      reportMenuList = (reportView.linkTitle,reportView.vid,1) :: reportMenuList
    }
    for(reportPage <- reportPageList){
      reportMenuList = (reportPage.linkTitle,reportPage.rpgid,2) :: reportMenuList
    }
    return reportMenuList
  }
  
  
  def loadReportMenuItemsForUser(userGroup : String) : List[(String,Int,Int)] = { 
    
    val reportViewList  = WebServiceModel.readAllReportViewForUser(userGroup)
    val reportPageList = WebServiceModel.readAllPublishedReportPageForUser(userGroup)
//  println(reportPageList)
    var reportMenuList : List[(String,Int,Int)] = List()
    for(reportView <- reportViewList){  
      reportMenuList = (reportView.linkTitle,reportView.vid,1) :: reportMenuList
    }
    for(reportPage <- reportPageList){
      reportMenuList = (reportPage.linkTitle,reportPage.rpgid,2) :: reportMenuList
    }
    return reportMenuList
  }
  
  def generateQueryForChart(currentChart  : ChartList) : String ={
    val xAggregation = currentChart.xAggregator
    val xField = currentChart.xField
    val yAggregation = currentChart.yAggregator
    val yField = currentChart.yField
    val groupBy = currentChart.groupBy
    
    var sql ="SELECT "
    if(xAggregation!="none"){
      sql += xAggregation+"("+xField+") as x, "
    }
    else{
      sql += xField+" as x, "
    }
    if(yAggregation!="none"){
      sql += yAggregation+"("+yField+") as y "
    }
    else{
      sql += yField+" as y "
    }
    sql +="FROM sentimentresult "
    
    if(groupBy!="none"){
      sql += "GROUP BY "+groupBy
    }
    
    sql
  }
  
  def getUserData(request : Request[AnyContent]) : Map[String,Any] = {
      var userData  = Map[String,Any]()
      userData += ("userGroup" -> request.session.get("usergroup").get.toInt)
      userData += ("userId" -> request.session.get("uid").get.toInt)
      userData
  }
  
}