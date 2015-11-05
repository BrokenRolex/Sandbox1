package ci.map.macro

import static org.junit.Assert.*
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import ci.map.Macro
import ci.map.MacroFactory

class SubstringTest {

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
        ta.setValue('name', 'substring')
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test() {
        ta.setValue('value', '5')
        macro = mf.createObject(ta)
        assert macro.execute([:], 'helloworld') == 'world'
        ta.setValue('value', '5,2')
        macro = mf.createObject(ta)
        assert macro.execute([:], 'helloworld') == 'wo'
        ta.setValue('value', '0,2')
        macro = mf.createObject(ta)
        assert macro.execute([:], 'helloworld') == 'he'
        ta.setValue('value', '0,9999')
        macro = mf.createObject(ta)
        assert macro.execute([:], 'helloworld') == 'helloworld'
        ta.setValue('value', '0')
        macro = mf.createObject(ta)
        assert macro.execute([:], 'helloworld') == 'helloworld'
        ta.setValue('value', '-1')
        macro = mf.createObject(ta)
        assert macro.execute([:], 'helloworld') == 'helloworld'
    }
    
    @Test(expected = NumberFormatException.class)
    public void test_empty_value () {
        ta.setValue('value', '')
        macro = mf.createObject(ta)
        macro.execute([:], 'helloworld')
    }

    @Test(expected = NumberFormatException.class)
    public void test_null_value () {
        ta.setValue('value', null)
        macro = mf.createObject(ta)
        macro.execute([:], 'helloworld')
    }
    
    @Test(expected = NumberFormatException.class)
    public void test_non_numeric_offset_and_length () {
        ta.setValue('value', 'x,y')
        macro = mf.createObject(ta)
        macro.execute([:], 'helloworld')
    }

    @Test(expected = NumberFormatException.class)
    public void test_non_numeric_length () {
        ta.setValue('value', '1,y')
        macro = mf.createObject(ta)
        macro.execute([:], 'helloworld')
    }

}
