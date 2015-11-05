package ci.map.macro

import static org.junit.Assert.*
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import ci.map.Macro;
import ci.map.MacroFactory

class PrefixTest {

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
        ta.setValue('name', 'prefix')
        macro = mf.createObject(ta)
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test() {
        ta.setValue('value', 'xyz')
        macro = mf.createObject(ta)
        assert macro.execute([:], 'abc') == 'xyzabc'
        assert macro.execute([:], '') == 'xyz'
        assert macro.execute([:], null) == 'xyz'
        //
        ta.setValue('value', null)
        macro = mf.createObject(ta)
        assert macro.execute([:], 'abc') == 'abc'
        assert macro.execute([:], '') == ''
        assert macro.execute([:], null) == ''
        //
        ta.setValue('value', 'key')
        ta.setValue('source', 'data')
        macro = mf.createObject(ta)
        assert macro.execute([key:'123'], 'abc') == '123abc'
    }

}