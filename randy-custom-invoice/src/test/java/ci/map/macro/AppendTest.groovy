package ci.map.macro

import static org.junit.Assert.*
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import ci.map.Macro
import ci.map.MacroFactory

class AppendTest {

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
        ta.setValue('name', 'append')
        macro = mf.createObject(ta)
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test_value() {
        ta.setValue('value', 'xyz')
        ta.setValue('source', 'value')
        macro = mf.createObject(ta)
        assert macro.execute([:], 'abc') == 'abcxyz'
        assert macro.execute([:], '') == 'xyz'
        assert macro.execute([:], null) == 'xyz'
    }

    @Test
    public void test_data() {
        ta.setValue('value', 'key')
        ta.setValue('source', 'data')
        macro = mf.createObject(ta)
        assert macro.execute([key:'123'], 'abc') == 'abc123'
        assert macro.execute([key:'123'], '') == '123'
        assert macro.execute([key:'123'], null) == '123'
    }

    @Test
    public void test_null() {
        ta.setValue('value', null)
        ta.setValue('source', 'value')
        macro = mf.createObject(ta)
        assert macro.execute([:], null) == ''
    }

    @Test
    public void test_empty() {
        ta.setValue('value', '')
        ta.setValue('source', 'value')
        macro = mf.createObject(ta)
        assert macro.execute([:], '') == ''
    }

}