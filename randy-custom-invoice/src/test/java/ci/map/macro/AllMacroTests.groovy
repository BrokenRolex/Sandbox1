package ci.map.macro

import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.junit.runners.Suite.SuiteClasses

@RunWith(Suite.class)
@SuiteClasses([
    AppendTest.class,
    DropTest.class,
    LeftPadTest.class,
    LowerTest.class,
    PrefixTest.class,
    RightPadTest.class,
    SubstringTest.class,
    TakeTest.class,
    UpperTest.class,
])
class AllMacroTests {
}
