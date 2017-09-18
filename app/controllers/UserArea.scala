package controllers

import play.api._
import play.api.mvc._

object UserArea extends Controller {

  def dashboard = Action{ implicit request =>
    if(checkSession(request)){      
      val reportMenu = GeneralFunctions.loadReportMenuItemsForUser(request.session.get("usergroup").get)
      Ok(views.html.userheader("Dash Board",1,reportMenu))
    }
    else{
      Redirect(routes.Application.index()).flashing(
        "error" -> "Your last session expired, Please Login again ")
    }
  }
  
  
  def checkSession (req : Request[AnyContent]) : Boolean = {
    req.session.get("uid").map { user =>
      val group = req.session.get("usergroup").get.toInt
//      println(group)
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

 