package ci.map.macro

import static org.junit.Assert.*
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

import ci.map.*
import ci.map.TestAttributes
//import org.xml.sax.Attributes

class RightPadTest {

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
        ta.setValue('name', 'rightpad')
        ta.setValue('value', '20')
        macro = mf.createObject(ta)
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test_small() {
        assert macro.execute([:], 'banana').length() == 20
    }

    @Test
    public void test_null() {
        assert macro.execute([:], null).length() == 20
    }

    @Test
    public void test_empty() {
        assert macro.execute([:], '').length() == 20
    }

    @Test
    public void test_big() {
        assert macro.execute([:], 'x'*100).length() == 20
    }

    @Test
    public void test_zero() {
        ta.setValue('value', '0')
        macro = mf.createObject(ta)
        assert macro.execute([:], 'x'*100).length() == 0
    }

}
