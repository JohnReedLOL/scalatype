import org.scalacheck.{Gen, Arbitrary}
import org.specs2._
import org.scalacheck.Prop._

class ScalaCheckSpec extends Specification with ScalaCheck {
  def is = s2"""

    A ScalaCheck property inside example ${forAll { (i: Int) => i > 0 || i <= 0 }}

    A `prop` method to create a property from a function
      returning a match result
      ${prop { (i: Int) => i must be_>(0) or be_<=(0) }}
      returning a boolean value
      ${prop { (i: Int) => i > 0 || i <= 0 }}
      using an implication and a boolean value
      ${prop { (i: Int) => (i > 0) ==> (i > 0) }}

    Custom `Arbitrary` instance for a parameter ${prop { (i: Int) => i must be_>(0) }.setArbitrary(positiveInts)}
    Custom minimum number of ok tests ${prop { (i: Int) => (i > 0) ==> (i > 0) }.set(minTestsOk = 50)}
  """

  val positiveInts = Arbitrary(Gen.choose(1, 5))
}