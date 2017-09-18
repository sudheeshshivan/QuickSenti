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
  
  def index = Action {implicit request =>

    Ok(views.html.main("QuickSenti | Login", Login.loginForm))
    // Ok("Hello ...")
  }

}
