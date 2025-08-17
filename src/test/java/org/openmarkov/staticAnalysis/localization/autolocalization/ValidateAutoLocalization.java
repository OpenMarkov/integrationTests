package org.openmarkov.staticAnalysis.localization.autolocalization;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.localize.ClassLocalizable;
import org.openmarkov.core.localize.StringBundle;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.core.localize.spi.LocalizeResourcesProvider;
import org.openmarkov.core.stringformat.StringFormat;
import org.openmarkov.plugin.PluginSearch;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * See the method {@link ValidateAutoLocalization#validateAutoLocalization()}, which is the purpose of this test class.
 *
 * @author jrico
 */
public class ValidateAutoLocalization {
    
    /**
     * List of all {@link Bundle}, including their {@link StringBundle} preloaded.
     */
    private static final List<Bundle> BUNDLES = StringDatabase
            .getBundleProviders()
            .flatMap(Bundle::of)
            .toList();
    /**
     * Map where every module has a list of {@link Bundle}s that are defined in said module.
     */
    private static final Map<Module, List<Bundle>> MODULES_AND_BUNDLES = ValidateAutoLocalization.BUNDLES
            .stream()
            .collect(Collectors.groupingBy(bundle -> bundle.provider.getClass().getModule()));
    /**
     * Map where every module has a list of {@link ClassLocalizable}s that are defined in said module.
     */
    private static final Map<Module, List<Class<ClassLocalizable>>> MODULES_AND_AUTOLOCALIZABLES = PluginSearch.init()
                                                                                                               .childrenOf(ClassLocalizable.class)
                                                                                                               .stream()
                                                                                                               .filter(localizableClass -> !localizableClass.isInterface() && !Modifier.isAbstract(localizableClass.getModifiers()))
                                                                                                               .collect(Collectors.groupingBy(Class::getModule));
    /**
     * List where every {@link ClassLocalizable} of every module is associated to the Bundles that can be accessed in
     * said module.
     */
    private static final List<AutolocalizablesAndBundles> LOCALIZABLES_AND_ACCESIBLE_BUNDLES = ValidateAutoLocalization.MODULES_AND_AUTOLOCALIZABLES
            .entrySet()
            .stream()
            .map(moduleAndLocalizable -> {
                var targetModule = moduleAndLocalizable.getKey();
                List<Bundle> resourceProviders = ValidateAutoLocalization.MODULES_AND_BUNDLES
                        .entrySet()
                        .stream()
                        .filter(providersModule -> targetModule.canRead(providersModule.getKey()))
                        .flatMap(providersModule -> providersModule.getValue()
                                                                   .stream())
                        .toList();
                return new AutolocalizablesAndBundles(resourceProviders, moduleAndLocalizable.getValue());
            }).toList();
    
    /**
     * Searches for every {@link ClassLocalizable} class and then gets their localization String in the
     * {@link StringDatabase#getUniqueInstance()} to verify it is well written.
     * <p>
     * A localization String is well written when:
     * <ul>
     *   <li>A localization String is specified for the class.</li>
     *   <li>The localization String is written in a stringBundle that is accessible to the class.</li>
     *   <li>All the values (See {@link StringFormat}) of the localization String correspond to fields of the class.
     *   </li>
     *   <li>All the pseudocodes written on the values can be resolved.
     *   </li>
     * </ul>
     * If a localization String was to be wrong, this test will fail specifying which where wrong, why, and it will also
     * print some advice and example code on how to solve it.
     */
    @Test void validateAutoLocalization() {
        List<Error> errors = ValidateAutoLocalization.findAllAutoLocalizationErrors();
        if (errors.isEmpty()) {
            return;
        }
        ValidateAutoLocalization.failWithLocalizationErrors(errors);
    }
    
    private static void failWithLocalizationErrors(List<Error> errors) {
        var errorClassAndErrors = errors.stream().collect(Collectors.groupingBy(Error::getClass));
        String errorsDescription = errorClassAndErrors
                .entrySet()
                .stream()
                .map(entry -> {
                    var errorClass = entry.getKey();
                    var unspecifiedErrorsForClass = entry.getValue();
                    if (errorClass == Error.FieldOrMethodMissing.class) {
                        var errorsForClass = unspecifiedErrorsForClass
                                .stream().map(v -> (Error.FieldOrMethodMissing) v)
                                .sorted(Comparator.comparing(v -> v.classWithMissingComponent.getName()))
                                .toList();
                        String subErrorsDetails = errorsForClass.stream()
                                                                .map(error ->
                                                                             "\t- No " + error.marker.toString()
                                                                                                     .toLowerCase() + " "
                                                                                     + error.missingComponent + " in "
                                                                                     + error.classWithMissingComponent.getName() + " of module "
                                                                                     + error.classWithMissingComponent.getModule()
                                                                                                                      .getName() + " when localizing " + error.whenLocalizingClass.getName())
                                                                .distinct()
                                                                .collect(Collectors.joining(System.lineSeparator()));
                        return "Some fields and methods are missing:" + System.lineSeparator() + subErrorsDetails;
                    }
                    if (errorClass == Error.LocalizationMissing.class) {
                        var errorsForClass = unspecifiedErrorsForClass
                                .stream().map(v -> (Error.LocalizationMissing) v)
                                .sorted(Comparator.comparing(v -> v.localizableClass.getName()))
                                .toList();
                        String subErrorsDetails = errorsForClass.stream()
                                                                .map(error ->
                                                                             "    <Localization class=\"" + error.localizableClass.getName()
                                                                                     + "\"" + System.lineSeparator() +
                                                                                     "                  value=\"\"/>")
                                                                .distinct()
                                                                .collect(Collectors.joining(System.lineSeparator()));
                        return "Some classes are not localized, you can add them with:" + System.lineSeparator() + subErrorsDetails;
                    }
                    if (errorClass == Error.LocalizationInInaccessibleBundle.class) {
                        var errorsForClass = unspecifiedErrorsForClass
                                .stream().map(v -> (Error.LocalizationInInaccessibleBundle) v)
                                .sorted(Comparator.comparing(v -> v.localizableClass.getName()))
                                .toList();
                        String subErrorsDetails = errorsForClass.stream()
                                                                .map(error ->
                                                                             "\tClass " + error.localizableClass.getName() + " is localized in " + error.wrongBundle.stringBundle + "_en.xml of module " + error.wrongBundle.provider.getClass()
                                                                                                                                                                                                                                     .getModule()
                                                                                                                                                                                                                                     .getName() + ", but it should be in " + error.localizableClass.getModule()
                                                                                                                                                                                                                                                                                                   .getName())
                                                                .distinct()
                                                                .collect(Collectors.joining(System.lineSeparator()));
                        return "Some classes are localized in the wrong module, you can apply this movements:" + System.lineSeparator() + subErrorsDetails;
                    }
                    return "";
                })
                .collect(Collectors.joining(System.lineSeparator() + System.lineSeparator()));
        fail(System.lineSeparator() + System.lineSeparator() + errorsDescription + System.lineSeparator() + System.lineSeparator());
    }
    
    private static @NotNull List<Error> findAllAutoLocalizationErrors() {
        List<Error> errors = new ArrayList<>();
        ValidateAutoLocalization.LOCALIZABLES_AND_ACCESIBLE_BUNDLES
                .stream()
                .flatMap(autolocalizablesAndBundles ->
                                 autolocalizablesAndBundles.localizablesClasses
                                         .stream()
                                         .map(autoLocalizableClass -> new AutolocalizableAndBundles(autolocalizablesAndBundles.bundles, autoLocalizableClass))
                )
                .forEach(autolocalizableAndBundles -> {
                    Class<ClassLocalizable> localizableClasses = autolocalizableAndBundles.localizablesClasses;
                    String keyName = localizableClasses.getName();
                    @Nullable Bundle localizedBundle;
                    Optional<Bundle> accessibleBundle = autolocalizableAndBundles.bundles
                            .stream()
                            .filter(bundles -> bundles.stringBundle.getString(keyName) != null)
                            .findFirst();
                    if (accessibleBundle.isPresent()) {
                        localizedBundle = accessibleBundle.get();
                    } else {
                        Optional<Bundle> inaccessibleBundle = ValidateAutoLocalization.BUNDLES.stream()
                                                                                              .filter(bundles -> bundles.stringBundle.getString(keyName) != null)
                                                                                              .findFirst();
                        if (inaccessibleBundle.isPresent()) {
                            localizedBundle = inaccessibleBundle.get();
                            errors.add(new Error.LocalizationInInaccessibleBundle(localizableClasses, localizedBundle));
                        } else {
                            errors.add(new Error.LocalizationMissing(localizableClasses));
                            return;
                        }
                    }
                    String localizedString = localizedBundle.stringBundle.getString(keyName);
                    var formattings = StringFormat.getAllFormattings(localizedString).toList();
                    for (var formatting : formattings) {
                        Class<?> argumentClass = localizableClasses;
                        if (!"this".equals(formatting.field())) {
                            var fieldClass = new StringFormat.PseudoCode(StringFormat.PseudoCode.Marker.FIELD, formatting.field())
                                    .resolveClassesThatShouldBeOpen(argumentClass);
                            if (!fieldClass.found()) {
                                errors.add(new Error.FieldOrMethodMissing(localizableClasses, localizableClasses, formatting.field(), StringFormat.PseudoCode.Marker.FIELD));
                                continue;
                            }
                            argumentClass = fieldClass.value().get(fieldClass.value().size() - 1);
                        }
                        for (var pseudocode : formatting.pseudocode()) {
                            var classesFound = pseudocode.resolveClassesThatShouldBeOpen(argumentClass);
                            if (classesFound.found()) {
                                argumentClass = classesFound.value().get(classesFound.value().size() - 1);
                            } else {
                                errors.add(new Error.FieldOrMethodMissing(localizableClasses, argumentClass, pseudocode.methodOrAttributeName(), pseudocode.marker()));
                            }
                        }
                    }
                });
        return errors;
    }
    
    /**
     * Represents every kind of error related to an improper localization of a {@link ClassLocalizable} class.
     */
    public static abstract sealed class Error permits Error.FieldOrMethodMissing, Error.LocalizationInInaccessibleBundle, Error.LocalizationMissing {
        
        /**
         * Represents the error of an {@link ClassLocalizable} class that has no localization file.
         */
        static final class LocalizationMissing extends Error {
            Class<ClassLocalizable> localizableClass;
            
            /**
             * Constructs a {@code LocalizationMissing} error with the specified {@link ClassLocalizable} class.
             *
             * @param localizableClass the class that is missing localization.
             */
            LocalizationMissing(Class<ClassLocalizable> localizableClass) {
                this.localizableClass = localizableClass;
            }
        }
        
        /**
         * Represents the error of an {@link ClassLocalizable} class whose localization value is in a stringBundle not
         * accessible from this class.
         */
        static final class LocalizationInInaccessibleBundle extends Error {
            Class<ClassLocalizable> localizableClass;
            Bundle wrongBundle;
            
            /**
             * Constructs an error representing an {@link ClassLocalizable} class whose localization value is in a stringBundle
             * not accessible from this class.
             *
             * @param localizableClass the class whose localization is in an inaccessible stringBundle.
             * @param wrongBundle      the stringBundle containing localization value, but that is not accessible to the class.
             */
            LocalizationInInaccessibleBundle(Class<ClassLocalizable> localizableClass, Bundle wrongBundle) {
                this.localizableClass = localizableClass;
                this.wrongBundle = wrongBundle;
            }
        }
        
        /**
         * Represents the error of a field or method that cannot be resolved reflectively when resolving the
         * localization String of the {@link ClassLocalizable} class.
         */
        static final class FieldOrMethodMissing extends Error {
            Class<ClassLocalizable> whenLocalizingClass;
            Class<?> classWithMissingComponent;
            String missingComponent;
            StringFormat.PseudoCode.Marker marker;
            
            /**
             * Constructs the error to represent a field or method that could not be resolved while resolving the
             * localization String.
             *
             * @param whenLocalizingClass       the {@link ClassLocalizable} class being localized.
             * @param classWithMissingComponent the class containing the missing field or method.
             * @param missingComponent          the name of the missing field or method.
             * @param marker                    the marker associated with the formatting process.
             */
            FieldOrMethodMissing(Class<ClassLocalizable> whenLocalizingClass, Class<?> classWithMissingComponent, String missingComponent, StringFormat.PseudoCode.Marker marker) {
                this.whenLocalizingClass = whenLocalizingClass;
                this.classWithMissingComponent = classWithMissingComponent;
                this.missingComponent = missingComponent;
                this.marker = marker;
            }
        }
    }
    
    /**
     * List of {@link ClassLocalizable} classes associated with the bundles they can access (Meaning these
     * {@link ClassLocalizable}s come all from the same module).
     */
    private record AutolocalizablesAndBundles(List<Bundle> bundles,
                                              List<Class<ClassLocalizable>> localizablesClasses) {
    }
    
    /**
     * A {@link ClassLocalizable} class associated with the stringBundle it can access.
     */
    private record AutolocalizableAndBundles(List<Bundle> bundles,
                                             Class<ClassLocalizable> localizablesClasses) {
    }
    
    /**
     * Aggregate of all the information to represent a Bundle file.
     *
     * @param provider     The {@link LocalizeResourcesProvider} where this stringBundle came from.
     * @param bundleName   The name of the stringBundle.
     * @param stringBundle The resolved {@link StringBundle}.
     */
    public record Bundle(LocalizeResourcesProvider provider, String bundleName, StringBundle stringBundle) {
        
        /**
         * Creates a stream of bundles created from every Bundle in
         * {@link LocalizeResourcesProvider#getBundlesMap(Locale)}.
         *
         * @param provider Used to retrieve all the localization resource bundles.
         * @return a stream of bundles created from every Bundle in
         * {@link LocalizeResourcesProvider#getBundlesMap(Locale)}.
         */
        public static Stream<Bundle> of(LocalizeResourcesProvider provider) {
            return provider.getBundlesMap(Locale.ENGLISH)
                           .entrySet()
                           .stream()
                           .map(entry -> new Bundle(provider, entry.getKey(), entry.getValue()));
        }
    }
    
}
