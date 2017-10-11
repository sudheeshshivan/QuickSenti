package controllers

import java.util.Date
import org.quartz.JobBuilder.newJob
import org.quartz.SimpleScheduleBuilder.simpleSchedule
import org.quartz.CronScheduleBuilder._
import org.quartz.TriggerBuilder.newTrigger
import org.quartz.impl.StdSchedulerFactory
import org.quartz.Job
import org.quartz.JobExecutionContext
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import org.quartz.JobKey

object Application extends Controller {

	def checkSession (req : Request[AnyContent]) : Boolean = {
        req.session.get("uid").map { user =>
            val group = req.session.get("usergroup").get
            if (group.toInt==0) {        
                 return true
            } else {
                return false
            }
        }.getOrElse {
          return false
        } 
    }

    def checkUserSession (req : Request[AnyContent]) : Boolean = {
        req.session.get("uid").map { user =>
            val group = req.session.get("usergroup").get
            if (group.toInt==0) {        
                 return false
            } else {
                return true
            }
        }.getOrElse {
          return false
        } 
    }
  
 	def index = Action {implicit request =>

        if(checkUserSession(request)){
            Redirect("/userArea")
        } else if(checkSession(request)){
    		Redirect("/adminArea")
        } else {
            Ok(views.html.main("QuickSenti | Login", Login.loginForm))
        }

  	}
}
