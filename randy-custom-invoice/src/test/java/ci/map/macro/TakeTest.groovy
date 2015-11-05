package ci.map.macro

import static org.junit.Assert.*
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import ci.map.Macro
import ci.map.MacroFactory

class TakeTest {

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
        ta.setValue('name', 'take')
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test_good() {
        ta.setValue('value', '5')
        macro = mf.createObject(ta)
        assert macro.execute([:], 'helloxxx') == 'hello'
        assert macro.execute([:], 'xxx') == 'xxx'
    }

    @Test
    public void test_negative() {
        ta.setValue('value', '-1')
        macro = mf.createObject(ta)
        assert macro.execute([:], 'abc') == ''
    }

    @Test
    public void test_zero() {
        ta.setValue('value', '0')
        macro = mf.createObject(ta)
        assert macro.execute([:], 'abc') == ''
    }

    @Test
    public void test_null() {
        ta.setValue('value', null)
        macro = mf.createObject(ta)
        assert macro.execute([:], 'abc') == ''
        assert macro.execute([:], null) == ''
    }

    @Test
    public void test_empty() {
        ta.setValue('value', '')
        macro = mf.createObject(ta)
        assert macro.execute([:], 'abc') == ''
        assert macro.execute([:], '') == ''
    }

}
