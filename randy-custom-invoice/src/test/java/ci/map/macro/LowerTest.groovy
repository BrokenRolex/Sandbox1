package ci.map.macro

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test

import ci.map.Macro;
import ci.map.MacroFactory;;

class LowerTest {

    MacroFactory mf
    TestAttributes ta
    Macro macro

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        mf = new MacroFactory()
        ta = new TestAttributes()
        ta.setValue('name', 'lower')
        macro = mf.createObject(ta)
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test() {
        assert macro.execute([:], 'abc') == 'abc'
        assert macro.execute([:], 'ABC') == 'abc'
        assert macro.execute([:], 'AbC') == 'abc'
        assert macro.execute([:], null) == ''
        //fail("Not yet implemented");
    }

}
