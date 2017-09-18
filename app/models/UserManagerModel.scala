package models

import play.api.db._
import anorm._
import anorm.SqlParser._
import play.api.Play.current
import org.apache.hadoop.hdfs.server.namenode.INodeReference.WithCount
import play.Logger


case class UserGroup(grpid : Int, groupName : String)
case class User(uid : Int , username : String, password : String, userGroupId : Int, userGroup : String, email : String )

object UserManagerModel {
  
  val userGroup ={
    get[Int]("grpid")~
    get[String]("groupname") map {
      case grpid~groupname => UserGroup(grpid, groupname)
    }
  }
  
  val users = {
    get[Int]("uid")~
    get[String]("username")~
    get[String]("password")~
    get[Int]("grpid")~
    get[String]("groupname")~
    get[String]("email") map {
      case uid~username~password~grpid~usergroup~email => User(uid,username,password,grpid,usergroup,email)
    }
  }
  
  def deleteUserGroup(hashId : String ) : Int = DB.withConnection { implicit c =>
    var result = 0
    var isItDeletable = 0
    
//    Checking whether there is any user exist under this group 
    val qry1=SQL("SELECT count(*) FROM tbluser WHERE md5(CAST((grpid)AS text))={hashId} ").on(
      'hashId      -> hashId
    )
    qry1().map { row =>
      if((row.asMap.get(".count").get).toString().toInt > 0){
        isItDeletable = 1
      }
    }
    
//    Checking whether there is any privilage assignment exist
    val qry2=SQL("SELECT count(*) FROM userprevilage WHERE md5(CAST((groupid)AS text))={hashId} ").on(
      'hashId      -> hashId
    )
    qry2().map { row =>
      if((row.asMap.get(".count").get).toString().toInt > 0){
        isItDeletable = 1
      }
    }
    if(isItDeletable>0){      
    	Logger.error("Cannot delete the selected group as it has some references") 
    }
    else{
    	result = SQL("DELETE FROM tblusergroup WHERE md5(CAST((grpid)AS text))={hashId}").on(
    			'hashId      -> hashId
    			).executeUpdate()        
    }
    result
  }

  def deleteUser(hashId : String ) : Int = DB.withConnection { implicit c => 
    val result = SQL("DELETE FROM tbluser WHERE md5(CAST((uid)AS text))={hashId}").on(
      'hashId      -> hashId
    ).executeUpdate()
    return result
  }

  def removePrivilage(hashId : String ) : Int = DB.withConnection { implicit c =>
    val sql= SQL("DELETE FROM userprevilage WHERE md5(CAST((plgid) AS text))={hashId}").on(
      'hashId      -> hashId
    )
    println(sql)
    val result = sql.executeUpdate()
    return result
  }
  
  
  
  def saveUserGroup(groupName : String) : Int = DB.withConnection { implicit c =>
   
    val result = SQL("INSERT INTO tblusergroup(groupname) VALUES ({groupName})").on(
        'groupName  -> groupName
      ).executeUpdate()
    return result
 
  }
  
  
  def saveNewUser(username : String, password : String, userGroup :Int, email : String) : Int = DB.withConnection { implicit c =>
    val result = SQL("INSERT INTO tbluser(username,password,grpid,email) VALUES ({username},{password},{groupId},{email})").on(
        'username  -> username,
        'password  -> password,
        'groupId  -> userGroup,
        'email  -> email
      ).executeUpdate()
    return result
  }
  
  def saveUserPrevilage(groupId : Int, reportType : Int, reportId : String) : Int = DB.withConnection { implicit c =>
    val result = SQL("INSERT INTO userprevilage(groupid,report,reporttype) VALUES ({groupId},{reportId},{reportType})").on(
        'groupId  -> groupId,
        'reportId  -> reportId,
        'reportType  -> reportType
      ).executeUpdate()
    return result
  }
  
  def readAllUserGroup : List[UserGroup] = DB.withConnection { implicit c =>
    SQL("SELECT * FROM tblusergroup").as(userGroup *)
  }
  
  def readAllUser : List[User] = DB.withConnection { implicit c =>
    SQL("""SELECT uid,username,password,A.grpid,B.groupname,A.email FROM tbluser A 
INNER JOIN tblusergroup B ON B.grpid=A.grpid""").as(users *)
  }
  
  
  def readAllUserForTest: List[Map[String,Any]] = DB.withConnection { implicit c =>
    val sql = SQL("""SELECT * FROM tbluser as u""")
    val result = sql().map(row => 
      row.asMap
    ).toList
    return result;
  }
  
  def readReportListAssigned(hashId : String) : List[Map[String,Any]] = DB.withConnection { implicit c => 
    
    val sql = SQL("""select A.plgid,A.groupid,A.report,B.pagename FROM userprevilage A 
    INNER JOIN reportpage B 
      on md5(CAST((B.rpgid)AS text))=A.report 
      WHERE reporttype=2 AND
      md5(CAST((A.groupid)AS text))={hashId}
    """).on(
       'hashId -> hashId
    )
    
    val result = sql().map( row =>
        row.asMap
          ).toList
          
    result
  }
  
}