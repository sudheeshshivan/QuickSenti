package controllers

import play.api._
import play.api.mvc._

import models.AdminModel

object UserArea extends Controller {

  def dashboard = Action{ implicit request =>
    if(checkSession(request)){      
      val dataSource = AdminModel.readAllDataSource
      Ok(views.html.userheader("Dash Board",1,dataSource))
    }
    else{
      Redirect(routes.Application.index()).flashing(
        "error" -> "Your last session expired, Please Login again ")
    }
  }
  
  
  def checkSession (req : Request[AnyContent]) : Boolean = {
    req.session.get("uid").map { user =>
      val group = req.session.get("usergroup").get.toInt
      if(group!=0){        
        return true
      }
      else{
        return false
      }
    }.getOrElse {
      return false
    } 
  }
}

 