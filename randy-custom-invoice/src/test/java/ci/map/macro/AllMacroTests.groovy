package ci.map.macro

import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.junit.runners.Suite.SuiteClasses

@RunWith(Suite.class)
@SuiteClasses([
     AppendTest.class,
     LeftPadTest.class,
     LowerTest.class,
     PrefixTest.class,
     RightPadTest.class,
     UpperTest.class,
      ])
class AllMacroTests {
}
