package controllers


import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import models._


case class LoginInfo(username : String , password : String)

object Login extends Controller {
  
 var loginForm = Form(
    mapping(
      "username" -> nonEmptyText(5,15),
      "password" -> nonEmptyText(5,15)
    )(LoginInfo.apply)(LoginInfo.unapply)
  )

  
  def doLogin = Action {implicit request =>
    loginForm.bindFromRequest.fold(
    formWithErrors => {
      BadRequest(views.html.main("Login Error",formWithErrors))
    },
    loginData => {
      val result = UserModel.checkUser(loginData.username, loginData.password)
      if(!result.isEmpty){        
        if(result.head._2==0){
          Logger.info("Loggin successfull, Redirecting to admin Page")
        	Redirect("/adminArea").withSession(
        			request.session + ("user" -> loginData.username)
        			+ ("uid" -> result.head._1.toString() )
        			+ ("usergroup" -> result.head._2.toString() )
        			)
        }
        else if(result.head._2>0){
          Logger.info("Loggin successfull, Redirecting to user Page with group: "+result.head._2)
          Redirect("/userArea").withSession(
              request.session + ("user" -> loginData.username)
              + ("uid" -> result.head._1.toString() )
              + ("usergroup" -> result.head._2.toString() )
              )
        }
        else{
          Logger.error("Loggin Attempt Failed : Invalid User Details")
          Redirect(routes.Application.index()).flashing(
                  "error" -> "Invalid Login Attempt")
        }
      }
      else{
        Ok("Invalid Username or Password")
      }
    })
  }

  def logout = Action { implicit request =>
    Redirect("/").withSession(
        request.session - "uid" - "user"   
    ) 
  }
}