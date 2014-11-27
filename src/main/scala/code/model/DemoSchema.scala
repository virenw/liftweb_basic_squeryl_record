package code.model

import org.squeryl.Schema

/**
 * Created by viren on 11/19/14.
 */
object DemoSchema extends Schema {

  val users = table[User]("user_table")

  // this is a test schema, we can expose the power tools ! :
  override def drop = super.drop
}
