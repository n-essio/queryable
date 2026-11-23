package it.ness.queryable.model.v4;

import org.apache.maven.plugin.logging.SystemStreamLog;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class QListFilterTest {

    @Test
    public void generatesBigDecimalSearchMethod() {
        JavaClassSource javaClass = Roaster.parse(
                JavaClassSource.class,
                "package example; import it.ness.queryable.annotations.QList; " +
                        "import java.math.BigDecimal; public class Customer { @QList public BigDecimal amount; }"
        );

        QListFilter filter = new QListFilter(new SystemStreamLog())
                .parseQFilter("Customer", javaClass.getField("amount"), false);

        assertNotNull(filter);
        assertTrue(filter.getSearchMethod().contains("CriteriaBuilder.In<BigDecimal>"));
        assertTrue(filter.getSearchMethod().contains("asBigDecimalList"));
    }
}
