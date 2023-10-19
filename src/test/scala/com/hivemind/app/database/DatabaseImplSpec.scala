package com.hivemind.app.database

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class DatabaseImplSpec extends AnyFlatSpec with should.Matchers {

  "A Stack" should "pop values in last-in-first-out order" in {

    1 should be(1)
  }

  it should "throw NoSuchElementException if an empty stack is popped" in {
    val emptyStack: () => Nothing = () => throw new NoSuchElementException("jaja")
    a[NoSuchElementException] should be thrownBy {
      emptyStack()
    }
  }
}
