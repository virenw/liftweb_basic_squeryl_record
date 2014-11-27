package code
package model

import net.liftweb.http.S
import net.liftweb.record._
import net.liftweb.common._
import net.liftweb.record.field.LongField
import net.liftweb.util.FieldError
import net.liftweb.squerylrecord.RecordTypeMode._
import org.squeryl.KeyedEntity
import scala.xml.{Elem, NodeSeq, Text}

class User extends MegaProtoUser[User] with KeyedEntity[LongField[User]] {

  def meta = User

  /**
   * Make sure that the email field is unique in the database
   */
  override def valUnique(msg: => String)(email: String): List[FieldError] = {
    val user = User.findUserByEmail(email)
    if(user.isDefined && user.get.id == id && user.get.id.value != id.value){
      List(FieldError(this.email, Text(msg)))
    }else{
      Nil
    }
  }

  override lazy val password = new MyPassword(this) {

    protected def appendFieldId(in: Elem): Elem = uniqueFieldId match {
      case Full(i) => {
        import net.liftweb.util.Helpers._
        in % ("id" -> i)
      }
      case _ => in
    }

    // S.SFuncHolder(this.setPassword(_))
    private def elem2: NodeSeq = S.fmapFunc({pwd: List[String] => pwd match {
      case x1 :: x2 :: Nil if x1 == x2 => this(x1)
      case _ => Nil
    }}) { funcName => <span>
      {appendFieldId(<input type="password" name={funcName} value="" tabindex={tabIndex toString}/>)}
      &nbsp;{S.?("repeat")}&nbsp;
      <input type="password" name={funcName} value="" tabindex={tabIndex toString}/>
    </span>
    }

    override def toForm: Box[NodeSeq] = Full(elem2)

  }

  override def saveTheRecord(): Box[User] = Full(DemoSchema.users.insertOrUpdate(this))
}

/**
 * The singleton that has methods for accessing the database
 */
object User extends User with MetaMegaProtoUser[User] {

  override def screenWrap = Full(<lift:surround with="default" at="content">
    <lift:bind /></lift:surround>)

  // define the order fields will appear in forms and output
  override def fieldOrder = List(id, firstName, lastName, email,
    locale, timezone, password)

  // comment this line out to require email validations
  override def skipEmailValidation = true

  protected def userFromStringId(id: String): Box[User] = {
    try {
      Full(from(DemoSchema.users)(u => where(u.id === id.toLong) select (u)).single)
    } catch {
      case _ : Throwable => Empty
    }
  }

  protected def findUserByUniqueId(id: String): Box[User] =
    try {
      from(DemoSchema.users)(u => where (u.id.value === id.toLong) select(u)).headOption
    } catch {
      case _ : Throwable => Empty
    }

  protected def findUserByEmail(email: String): Box[User] = {
    try {
      val user = from(DemoSchema.users)(u => where(u.email === email) select (u)).headOption
      if (user.isDefined) {
        Full(user.get)
      } else {
        Empty
      }
    }catch {
      case _ : Throwable => Empty
    }
  }

  protected def findUserByUserName(email: String): Box[User] = findUserByEmail(email)
}

