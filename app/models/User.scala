package models

import play.api.db._
import anorm._
import anorm.SqlParser._
import play.api.Play.current

object UserModel{
  
  def checkUser(username : String, password : String) : Map[Int,Int] = DB.withConnection { implicit c =>
    val userRow = SQL("SELECT uid,grpid  FROM tbluser WHERE username= {username} AND password = {password} ").on( 
        'username -> username,
        'password -> password ).apply()
    if(userRow.isEmpty){
      Map(-1 -> -1)
    }    
    else{      
      val returnMap = Map(userRow.head [Int]("uid") -> userRow.head [Int]("grpid"))
//      return userRow.head [Int]("uid")
      return returnMap
    }
  }
   
}