package it.ness.queryable.model.v3;

import org.apache.maven.plugin.logging.SystemStreamLog;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class QListFilterDefTest {

    @Test
    public void usesStringParamDefForStringField() {
        assertParamDefType("String", "string", "String");
    }

    @Test
    public void usesIntegerParamDefForIntegerField() {
        QListFilterDef filterDef = assertParamDefType("Integer", "int", "Integer");

        assertFalse(filterDef.getSearchMethod().isEmpty());
        assertTrue(filterDef.getSearchMethod().contains("asIntegerList"));
    }

    @Test
    public void usesLongParamDefForLongField() {
        assertParamDefType("Long", "long", "Long");
    }

    @Test
    public void usesBigIntegerParamDefForBigIntegerField() {
        assertParamDefType("BigInteger", "big_integer", "java.math.BigInteger");
    }

    private QListFilterDef assertParamDefType(String fieldType, String expectedType, String generatedType) {
        JavaClassSource javaClass = Roaster.parse(
                JavaClassSource.class,
                "package example; import it.ness.queryable.annotations.QList; " +
                        "import java.math.BigInteger; public class Customer { @QList public " + fieldType + " id; }"
        );
        QListFilterDef filterDef = new QListFilterDef(new SystemStreamLog())
                .parseQFilterDef("Customer", javaClass.getField("id"), false);

        assertNotNull(filterDef);
        assertEquals(expectedType, filterDef.getType());

        filterDef.addAnnotationToModelClass(javaClass);
        String generatedSource = javaClass.toString();
        assertTrue(generatedSource, generatedSource.contains("type = @" + generatedType));
        return filterDef;
    }
}
