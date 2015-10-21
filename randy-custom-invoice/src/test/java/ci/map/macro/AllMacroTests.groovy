package ci.map.macro

import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.junit.runners.Suite.SuiteClasses

@RunWith(Suite.class)
@SuiteClasses([ LeftPadTest.class, RightPadTest.class ])
class AllMacroTests {
}
