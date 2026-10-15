package it.ness.queryable.model.v3;

import org.apache.maven.plugin.logging.SystemStreamLog;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PreservedFilterGenerationTest {
    @Test
    public void preservesEveryFilterKindAndItsManualParameters() {
        for (FilterDefBase filter : List.of(new QFilterDef(new SystemStreamLog()),
                new QLikeFilterDef(new SystemStreamLog()), new QListFilterDef(new SystemStreamLog()),
                new QLikeListFilterDef(new SystemStreamLog()), new QNilFilterDef(new SystemStreamLog()),
                new QNotNilFilterDef(new SystemStreamLog()), new QLogicalDeleteFilterDef(new SystemStreamLog()))) {
            boolean plural = filter instanceof QListFilterDef || filter instanceof QLikeListFilterDef;
            String filterName = plural ? "Customer.obj.names" : "Customer.obj.name";
            JavaClassSource entity = model("String", filterName);
            filter.entityName = "Customer";
            filter.prefix = "obj";
            filter.name = "name";
            filter.filterName = "Customer.obj.name";
            filter.queryName = "obj.name";
            filter.fieldType = "string";
            filter.type = "string";
            String original = entity.toString();
            filter.addAnnotationToModelClass(entity);
            assertEquals(filter.getClass().getSimpleName(), original, entity.toString());
            assertEquals("", filter.getSearchMethod(Set.of(filterName)));
        }
    }

    @Test
    public void preservesIndividualDateFiltersWithoutSuppressingOtherBounds() {
        for (String type : List.of("LocalDate", "LocalDateTime", "ZonedDateTime", "Instant", "java.util.Date")) {
            JavaClassSource entity = model(type, "Customer.from.name");
            QFilterDef filter = new QFilterDef(new SystemStreamLog());
            filter.entityName = "Customer";
            filter.name = "name";
            filter.filterName = "Customer.obj.name";
            filter.fieldType = type;
            filter.type = type;
            filter.addAnnotationToModelClass(entity);
            String source = entity.toString();
            assertTrue(source.contains("manual = :manual"));
            assertTrue(source.contains("name <= :name"));
            assertTrue(source.contains("name = :name"));
            String search = filter.getSearchMethod(Set.of("Customer.from.name"));
            assertFalse(search.contains("Customer.from.name"));
            assertTrue(search.contains("Customer.to.name"));
            assertTrue(search.contains("Customer.obj.name"));
            assertTrue(filter.getSearchMethod().contains("Customer.from.name"));
            filter.addAnnotationToModelClass(entity);
            assertEquals(source, entity.toString());
        }
    }

    private JavaClassSource model(String type, String name) {
        return Roaster.parse(JavaClassSource.class, """
                import it.ness.queryable.annotations.QPreserveFilters;
                import org.hibernate.annotations.*;
                @QPreserveFilters("%s")
                @FilterDef(name = "%s", parameters = @ParamDef(name = "manual", type = String.class))
                @Filter(name = "%s", condition = "manual = :manual")
                public class Customer { public %s name; }
                """.formatted(name, name, name, type));
    }
}
