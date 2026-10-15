package it.ness.queryable.util;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class PreservedFiltersTest {
    @Test
    public void readsSingleArrayAndQualifiedAnnotationsAndDeduplicates() {
        assertEquals(Set.of("Customer.obj.name"), PreservedFilters.names(entity(
                "@it.ness.queryable.annotations.QPreserveFilters(\"Customer.obj.name\")")));
        assertEquals(Set.of("Customer.obj.name", "Customer.obj.code"), PreservedFilters.names(entity(
                "@QPreserveFilters({\"Customer.obj.name\", \"Customer.obj.code\", \"Customer.obj.name\"})")));
        assertEquals(Set.of(), PreservedFilters.names(entity("")));
    }

    @Test
    public void requiresExplicitNamesAndBothHibernateAnnotations() {
        for (String annotation : List.of("@QPreserveFilters({})", "@QPreserveFilters(\"\")",
                "@QPreserveFilters(\" Customer.obj.name\")", "@QPreserveFilters(FILTER_NAME)")) {
            assertThrows(IllegalArgumentException.class, () -> PreservedFilters.names(entity(annotation)));
        }
        assertThrows(IllegalArgumentException.class,
                () -> PreservedFilters.validateDefinitions(entity(""), Set.of("missing")));
        JavaClassSource entity = entity("""
                @FilterDefs({@FilterDef(name = "custom")})
                @Filters({@Filter(name = "custom", condition = "active = true")})
                """);
        PreservedFilters.validateDefinitions(entity, Set.of("custom"));
    }

    @Test
    public void validatesHooksAndKeepsDeclarationOrder() {
        JavaClassSource service = service("""
                @QPreservedFilter("second")
                private void applySecond(PanacheQuery<Customer> search) throws Exception {}
                @QPreservedFilter("first")
                protected void applyFirst(PanacheQuery<Customer> search) {}
                """);
        assertEquals(List.of("applySecond", "applyFirst"),
                PreservedFilters.hooks(service, Set.of("first", "second"), "example.Customer"));
        for (String method : List.of(
                "@QPreservedFilter(\"unknown\") void apply(PanacheQuery<Customer> q) {}",
                "@QPreservedFilter(\"first\") int apply(PanacheQuery<Customer> q) {return 0;}",
                "@QPreservedFilter(\"first\") static void apply(PanacheQuery<Customer> q) {}",
                "@QPreservedFilter(\"first\") void apply() {}",
                "@QPreservedFilter(\"first\") void apply(String q) {}",
                "@QPreservedFilter(\"first\") void apply(PanacheQuery<String> q) {}",
                "@QPreservedFilter(\"first\") void apply(PanacheQuery q) {}",
                "@QPreservedFilter(\"first\") void apply(PanacheQuery<Customer>[] q) {}",
                "@QPreservedFilter(FILTER_NAME) void apply(PanacheQuery<Customer> q) {}",
                "@QPreservedFilter(\"first\") void a(PanacheQuery<Customer> q) {}"
                        + "@QPreservedFilter(\"first\") void b(PanacheQuery<Customer> q) {}")) {
            assertThrows(IllegalArgumentException.class,
                    () -> PreservedFilters.hooks(service(method), Set.of("first"), "example.Customer"));
        }
    }

    private JavaClassSource entity(String annotations) {
        return Roaster.parse(JavaClassSource.class, annotations + " public class Customer {}");
    }

    private JavaClassSource service(String methods) {
        return Roaster.parse(JavaClassSource.class, """
                package example;
                import io.quarkus.hibernate.orm.panache.PanacheQuery;
                import it.ness.queryable.annotations.QPreservedFilter;
                public class CustomerServiceRs {
                """ + methods + "}");
    }
}
