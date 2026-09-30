package org.example.agendamento.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureTest {

    private static final String BASE_PACKAGE = "org.example.agendamento";

    private static JavaClasses classesDeProducao;

    @BeforeAll
    static void importarClasses() {
        classesDeProducao = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(BASE_PACKAGE);
    }

    @Test
    void dominioNaoDeveDependerDeSpring() {
        ArchRule regra = noClasses()
                .that().resideInAPackage(BASE_PACKAGE + ".domain..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework..");

        regra.check(classesDeProducao);
    }

    @Test
    void dominioNaoDeveDependerDeJpa() {
        ArchRule regra = noClasses()
                .that().resideInAPackage(BASE_PACKAGE + ".domain..")
                .should().dependOnClassesThat().resideInAnyPackage("jakarta.persistence..");

        regra.check(classesDeProducao);
    }

    @Test
    void dominioNaoDeveDependerDeAdapters() {
        ArchRule regra = noClasses()
                .that().resideInAPackage(BASE_PACKAGE + ".domain..")
                .should().dependOnClassesThat().resideInAPackage(BASE_PACKAGE + ".adapter..");

        regra.check(classesDeProducao);
    }

    @Test
    void aplicacaoNaoDeveDependerDeAdapters() {
        ArchRule regra = noClasses()
                .that().resideInAPackage(BASE_PACKAGE + ".application..")
                .should().dependOnClassesThat().resideInAPackage(BASE_PACKAGE + ".adapter..");

        regra.check(classesDeProducao);
    }

    @Test
    void aplicacaoNaoDeveDependerDeJpa() {
        ArchRule regra = noClasses()
                .that().resideInAPackage(BASE_PACKAGE + ".application..")
                .should().dependOnClassesThat().resideInAnyPackage("jakarta.persistence..");

        regra.check(classesDeProducao);
    }

    @Test
    void adaptersDePersistenciaNaoDevemDependerDeAdaptersDePagamento() {
        ArchRule regra = noClasses()
                .that().resideInAPackage(BASE_PACKAGE + ".adapter.out.persistence..")
                .should().dependOnClassesThat().resideInAPackage(BASE_PACKAGE + ".adapter.out.pagamento..");

        regra.check(classesDeProducao);
    }

    @Test
    void naoDeveHaverCiclosEntrePacotesPrincipais() {
        ArchRule regra = SlicesRuleDefinition.slices()
                .matching(BASE_PACKAGE + ".(*)..")
                .should().beFreeOfCycles();

        regra.check(classesDeProducao);
    }
}
