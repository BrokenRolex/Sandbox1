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

class LeftPadTest {

    MacroFactory mf = new MacroFactory()

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test() {
        def attrs = new TestAttributes()
        attrs.setValue('name', 'leftpad')
        attrs.setValue('value', '20')
        def macro = (Macro) mf.createObject(attrs)
        //fail("Not yet implemented")
        assert macro.execute([:], null).length() == 20
        assert macro.execute([:], '').length() == 20
        assert macro.execute([:], 'banana').length() == 20
        assert macro.execute([:], '1234567890123456789012').length() == 20
    }

}
