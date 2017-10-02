package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models.AdminModel
import models.UserManagerModel
import play.api.libs.json._
import models.WebServiceModel
import org.apache.commons.mail.EmailAttachment

import scala.sys.process._
/*
import io.github.cloudify.scala.spdf._
import java.io._
import java.net._
import scala.io.Source
import scala.xml.Elem
import javax.mail._
import java.util.Properties
import javax.mail.internet._
import javax.mail.search._
import play.api.libs.mailer.AttachmentData
import play.api.Play.current
import play.Plugin
import play.libs.mailer.Email
import play.libs.mailer.MailerPlugin
import play.api.libs.mailer.MailerPlugin*/


case class UserGroupInfo(groupName : String)
case class UserInfo(grpid : Int, username : String, password : String, email : String)
case class AssignPrevilageInfo(user : Int, reportType : Int, page : List[String])

case class AssignPrevilagesInfo(user : Int, page : List[String])

object UserManager  extends Controller {

    /*Form Defining Starts*/
  
  
  
//  Form For new UserGroup
  val userGroupForm = Form(
    mapping(
      "groupName" -> nonEmptyText    
    )(UserGroupInfo.apply)(UserGroupInfo.unapply)
  )
  
  
//  Form for new User
  val userForm = Form(
    mapping(
      "grpid"      -> number,
      "username"   -> nonEmptyText(5,15),
      "password" -> tuple(
          "main" -> nonEmptyText(5,15),
          "confirm" -> nonEmptyText
      ).verifying(
        // Add an additional constraint: both passwords must match
        "Passwords don't match", password => password._1 == password._2
      ).transform[String]({password => password._1}, {p => p -> p})      
      ,
      "email" -> nonEmptyText.verifying ("Enter valid email id", email => email.contains('@'))
    )(UserInfo.apply)(UserInfo.unapply)  
  )
  
  
  val assignPrevilageForm = Form(
    mapping(
        "user" -> number,
        "reportType" -> number,
        "page" -> list(text)
    )(AssignPrevilageInfo.apply)(AssignPrevilageInfo.unapply)
  )

  val assignPrevilagesForm = Form(
    mapping(
        "user" -> number,
        "page" -> list(text)
    )(AssignPrevilagesInfo.apply)(AssignPrevilagesInfo.unapply)
  )
  
  
  /*Form Definition Ends*/
  
  
  
  //  Menu for headers
  val reportMenu = GeneralFunctions.loadReportMenuItems
  
  
  
  def newUser = Action{implicit request =>
    if(AdminArea.checkSession(request)){  
      val groupSeq = GeneralFunctions.loadUserGroupForForm
      val userList = UserManagerModel.readAllUser
      Ok(views.html.newUser(views.html.adminheader("New User ", 3,reportMenu), userForm, groupSeq,userList,
        views.html.adminfooter()
        )
      )
    }
    else{
      Ok(views.html.main("Session Expired. Please Login",Login.loginForm))
    }      
  }
  
  def createNewUser = Action { implicit request =>  
    if(AdminArea.checkSession(request)){
      val groupSeq = GeneralFunctions.loadUserGroupForForm
      
      userForm.bindFromRequest.fold(
      formWithErrors => {        
        val userList = UserManagerModel.readAllUser
        Ok(views.html.newUser(views.html.adminheader("New User ", 3,reportMenu),formWithErrors,groupSeq,
         userList,
         views.html.adminfooter()
         )
        )
      },
      groupData => {
        val result = UserManagerModel.saveNewUser(groupData.username, groupData.password, groupData.grpid,groupData.email)
        val userList = UserManagerModel.readAllUser
        if(result>0){
          Ok(views.html.newUser(views.html.adminheader("Created Successfully ", 3,reportMenu), userForm, groupSeq, 
            userList,
            views.html.adminfooter()
            )
          )
        }
        else{
          Ok(views.html.newUser(views.html.adminheader("User creation Failed", 3,reportMenu), userForm, groupSeq, 
            userList,
            views.html.adminfooter()
            )
          )
        }
      })
    }
    else{
      Ok(views.html.main("Session Expired. Please Login",Login.loginForm))
    }      
  }
    
  
  def newUserGroup = Action{implicit request =>
    if(AdminArea.checkSession(request)){
      val userGroupList = UserManagerModel.readAllUserGroup
      Ok(views.html.newUserGroup(views.html.adminheader("New User Group", 3,reportMenu),userGroupForm,
        userGroupList,
        views.html.adminfooter()
        )
      )

    }
    else{
      Ok(views.html.main("Session Expired. Please Login",Login.loginForm))
    }
  }
  
  
  def userList = Action {
    
    // import play.api.libs.mailer._
    // import play.api.Play.current    
    // import java.io.File
    // import io.github.cloudify.scala.spdf._
    // import java.net._
    
    // val myTask  = Runtime.getRuntime.exec("mkdir /home/qburst/scala/samplDir")
    // val someData = myTask.waitFor()
    // Logger.info("Some Sample command line execution Started")
    // Logger.info(someData.toString())
    
    
//    USIGN sPDF Libarary
//    val pdf = Pdf(new PdfConfig {
//      orientation := Landscape
//      pageSize := "Letter"
//      marginTop := "1in"
//      marginBottom := "1in"
//      marginLeft := "1in"
//      marginRight := "1in"
//    })
//    pdf.run(new URL("http://localhost:9000/WebService/reports/show/content/9bf31c7ff062936a96d3c8bd1f8f2ff3/export"), new File("public/pdf_list/hwtest.pdf"))
    /*for(menu <- reportMenu){
    	if(menu._3==1){
    		Logger.info("Processing PDF from View")
    		pdf.run(new URL("http://localhost:9000/WebService/reports/show/content/"+GeneralFunctions.getMD5Hash(menu._2.toString())+"/export"), new File("public/pdf_list/"+menu._2+menu._1+ ".pdf"))
    		println("http://localhost:9000/WebService/reports/show/content/"+GeneralFunctions.getMD5Hash(menu._2.toString())+"/export")
    	}
    	else if(menu._3==2){
    		Logger.info("Processing PDF from Report Page")
    		pdf.run(new URL("http://localhost:9000/WebService/reports/show_page/content/"+GeneralFunctions.getMD5Hash(menu._2.toString())+"/export"), new File("public/pdf_list/"+menu._2+menu._1+".pdf"))
    	}
    }
    val userList = UserManagerModel.readAllUser
    userList.map { userData => 
    		val permissionList = GeneralFunctions.loadReportMenuItemsForUser(userData.userGroupId.toString())
    		val attachmentSeq = permissionList.map(
  				permission =>
  				AttachmentFile(permission._1+".pdf", new File("public/pdf_list/"+permission._2+permission._1+".pdf"))
				)
				val email = Email(
  				"Report - Daily Report",
  				"Sabu <sabulbs@gmail.com>",
  				Seq(userData.username+" <"+userData.email+">"),
  				attachments = attachmentSeq,  
  				bodyHtml = Some("""
  						<html>
  						<body>
  						<h3>Hai ,</h3>
  						<p> This is a test message from the new email id from my application. Scala sends first email to you :-) </p>
  						<img src="favicon.png">
  						</body>
  						</html>
  						""")
				)
				
				MailerPlugin.send(email)
    }*/
    Ok("Users list")     
  }
  
  def doNewUserGroup = Action { implicit request =>      
    if(AdminArea.checkSession(request)){
      userGroupForm.bindFromRequest.fold(
      formWithErrors => {
        val userGroupList = UserManagerModel.readAllUserGroup
        Ok(views.html.newUserGroup(views.html.adminheader("Group Creation Failed", 3,reportMenu),
          formWithErrors, 
          userGroupList,
          views.html.adminfooter()
          )
        )

      },
      groupData => {
        val result =  UserManagerModel.saveUserGroup(groupData.groupName)
        val userGroupList = UserManagerModel.readAllUserGroup
        
        if(result > 0){
          Ok(views.html.newUserGroup(views.html.adminheader("Successfully updated", 3,reportMenu), userGroupForm,
            userGroupList,
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
      Ok(views.html.main("Session Expired. Please Login",Login.loginForm))
    }
  }
  
  def deleteUserGroup(hashId : String) = Action {implicit request =>    
    if(AdminArea.checkSession(request)){
      val result= UserManagerModel.deleteUserGroup(hashId)
      val userGroupList = UserManagerModel.readAllUserGroup
      if(result>0){        
    	  Ok(views.html.newUserGroup(views.html.adminheader("User Group Deleted", 3,reportMenu), userGroupForm,
          userGroupList,
          views.html.adminfooter()
          )
        )
      }
      else{
        Redirect(routes.AdminArea.dashboard()).flashing(
        "error" -> "Group Deletion error ")
      }
    }
    else{
      Ok(views.html.main("Session Expired. Please Login",Login.loginForm))
    }   
  }
  
  
  def deleteUser(hashId : String) = Action {implicit request =>    
    if(AdminArea.checkSession(request)){
      val result= UserManagerModel.deleteUser(hashId)
      val userList = UserManagerModel.readAllUser
      val groupSeq = GeneralFunctions.loadUserGroupForForm
      Ok(views.html.newUser(views.html.adminheader("Deleted Successfully ", 3,reportMenu), userForm, groupSeq, 
        userList,
        views.html.adminfooter()
        )
      )
    }
    else{
      Ok(views.html.main("Session Expired. Please Login",Login.loginForm))
    }   
  }
  
  
  def assignUserPrevilage = Action { implicit request =>
    if(AdminArea.checkSession(request)){
      val userGroupSeq = GeneralFunctions.loadUserGroupForForm
      val reports = GeneralFunctions.loadReportMenuItems

      Ok(views.html.assignPrevilage(views.html.adminheader("Assign Previlage to Users",3,reportMenu),
        assignPrevilagesForm,
        userGroupSeq,reports,
        views.html.adminfooter()
        )
      )

      // val dataSources = GeneralFunctions.loadDataSources

      // Ok(
      //   views.html.assignPrevilage(
      //     views.html.adminheader("Assign Previlage to Users",3,reportMenu),
      //     assignPrevilagesForm,
      //     userGroupSeq,
      //     // dataSources,
      //     views.html.adminfooter()
      //   )
      // )
    }
    else{
      Ok(views.html.main("Session Expired. Please Login",Login.loginForm))
    }    
  } 
  
  def removeUserPrevilage = Action { implicit request =>
    if(AdminArea.checkSession(request)){
      val userGroupSeq = GeneralFunctions.loadUserGroupForForm
      val reports = GeneralFunctions.loadReportMenuItems
      
      Ok(views.html.removePrivilage(views.html.adminheader("Remove Previlage to Users",3,reportMenu),
        userGroupSeq,
        views.html.adminfooter()
        )
      )
    }
    else{
      Ok(views.html.main("Session Expired. Please Login",Login.loginForm))
    }    
  }
  
  def doRemovePrivilage = Action { implicit request =>
    if(AdminArea.checkSession(request)){
      val assignmentToRemove = request.body.asFormUrlEncoded.get.get("hashId").get
      println(assignmentToRemove)
      for(singleAssignment <- assignmentToRemove ){
        UserManagerModel.removePrivilage(singleAssignment)
      }
     Ok("Removing Assigned privilage") 
    }
    else{
      Ok(views.html.main("Session Expired. Please Login",Login.loginForm))
    }
  }
  
  def doAssignUserPrevilage = Action { implicit request =>
    if(AdminArea.checkSession(request)){
      assignPrevilageForm.bindFromRequest().fold(
        formWithErrors => 
          Ok(formWithErrors.toString())
        , previlageData => {
      	  var result = 0
    			previlageData.page.map{ pageId =>
    			  result += UserManagerModel.saveUserPrevilage(previlageData.user, previlageData.reportType, pageId)
        	}   
          if(result>0){
            Redirect(routes.UserManager.assignUserPrevilage()).flashing(
                  "success" -> "Previlage assigned  successfully")
          }
          else{
            Redirect(routes.UserManager.assignUserPrevilage()).flashing(
                  "error" -> "Previlage Assignment failed, Some Error occurred")
          }            
        }
      )
    }
    else{
      Ok(views.html.main("Session Expired. Please Login",Login.loginForm))
    }
  }


  def doAssignUserPrevilages = Action { implicit request =>
    if(AdminArea.checkSession(request)){
      assignPrevilagesForm.bindFromRequest().fold(
        formWithErrors => 
          Ok("helloo --> "+formWithErrors.toString())
        , previlageData => {
          var result = 0
          previlageData.page.map{ pageId =>
            result += UserManagerModel.saveUserPrevilages(previlageData.user, pageId)
          }   
          if(result>0){
            Redirect(routes.UserManager.assignUserPrevilage()).flashing(
                  "success" -> "Previlage assigned  successfully")
          }
          else{
            Redirect(routes.UserManager.assignUserPrevilage()).flashing(
                  "error" -> "Previlage Assignment failed, Some Error occurred")
          }            
        }
      )
    }
    else{
      Ok(views.html.main("Session Expired. Please Login",Login.loginForm))
    }
  }



}







//    val page1 = """<html><body>Hai Hello</body></html>"""
//    val page = Source.fromURL("http://google.com")
//    Elem.
//    page
//    val outputStream = new ByteArrayOutputStream
//    pdf.run(page1, new File("samplePDF.pdf"))
//    println(new URL("http://localhost:9000/adminArea/reports/show_page/b6d767d2f8ed5d21a44b0e5886680cb9").getFile.getBytes)
//    pdf.run(new URL("http://localhost:9000/WebService/reports/show_page/content/37693cfc748049e45d87b8c7d8b9aacd/export"), new File("report.pdf"))
    
      /*for(permission <- permissionList){
        println(permission)
      }*/
//      var attachmentSeq : List[AttachmentFile] = List()
      /*for(permission <- permissionList){
        attachmentSeq += Seq(AttachmentFile(permission._3+".pdf", new File("public/pdf_list/"+permission._2+permission._1+".pdf")) ) 
      }*/
//      var email : Email = Email("","")
              
    
    /*val email = Email(
      "Scala mail Server",
      "Sabu <sabulbs@gmail.com>",
      Seq("Sabik Ahammed <sabikla@gmail.com>"),
      // adds attachment
      attachments = Seq(
        AttachmentFile("report.pdf", new File("report.pdf")) 
//        AttachmentFile("favicon.png", new File("public/images/favicon.png")),
        // adds inline attachment from byte array
//        AttachmentData("data.txt", "data".getBytes, "text/plain", Some("Simple data"), Some(EmailAttachment.INLINE))
      ),
      // sends text, HTML or both...
//      bodyText = Some("A text message")
      bodyHtml = Some("""
      <html>
        <body>
        <h3>Hai ,</h3>
        <p> This is a test message from the new email id from my application. Scala sends first email to you :-) </p>
        <img src="favicon.png">
        </body>
      </html>
      """),
      bcc = Seq("sabikla@gmail.com")
    )*/
//    MailerPlugin.send(email)
    
    
    
    
    



/*
     * READING TWEETS FROM HBASE AND PARSING IT
     * 
    var tweetData : String = ""
    var counter : Int = 0 
    try{
       
      val conf = HBaseConfiguration.create()
      val table = new HTable(conf, "delhitable")
      val scan = new Scan;
      val scanner = table.getScanner(scan)
      val si = scanner.iterator()
      var tweet : String = ""
      
      while(si.hasNext()){
        val rowKey = si.next().toString().split("/").head.substring(11)
        
        val theget= new Get(Bytes.toBytes(rowKey))
        val result=table.get(theget)
        val value=result.value()
        tweet = Bytes.toString(value)
        val json: JsValue = Json.parse(tweet)
        val textData = (json \ "text").as[String]
        counter +=1
        tweetData = tweetData + "\n\n" + counter + "  " + textData
      }      
      Ok(tweetData)
    }
    
    catch {
      case ex: Exception =>{
        Ok(tweetData)
      }
    }
    * 
    */