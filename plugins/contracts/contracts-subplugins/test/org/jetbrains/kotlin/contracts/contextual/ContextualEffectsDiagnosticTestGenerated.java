/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.contextual;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.JUnit3RunnerWithInners;
import org.jetbrains.kotlin.test.KotlinTestUtils;
import org.jetbrains.kotlin.test.TargetBackend;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link org.jetbrains.kotlin.generators.tests.TestsPackage}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("plugins/contracts/contracts-subplugins/testData")
@TestDataPath("$PROJECT_ROOT")
@RunWith(JUnit3RunnerWithInners.class)
public class ContextualEffectsDiagnosticTestGenerated extends AbstractContextualEffectsDiagnosticTest {
    private void runTest(String testDataFilePath) throws Exception {
        KotlinTestUtils.runTest(this::doTest, TargetBackend.ANY, testDataFilePath);
    }

    public void testAllFilesPresentInTestData() throws Exception {
        KotlinTestUtils.assertAllTestsPresentByMetadata(this.getClass(), new File("plugins/contracts/contracts-subplugins/testData"), Pattern.compile("^(.+)\\.kt$"), TargetBackend.ANY, true);
    }

    @TestMetadata("plugins/contracts/contracts-subplugins/testData/checkedExceptions")
    @TestDataPath("$PROJECT_ROOT")
    @RunWith(JUnit3RunnerWithInners.class)
    public static class CheckedExceptions extends AbstractContextualEffectsDiagnosticTest {
        private void runTest(String testDataFilePath) throws Exception {
            KotlinTestUtils.runTest(this::doTest, TargetBackend.ANY, testDataFilePath);
        }

        public void testAllFilesPresentInCheckedExceptions() throws Exception {
            KotlinTestUtils.assertAllTestsPresentByMetadata(this.getClass(), new File("plugins/contracts/contracts-subplugins/testData/checkedExceptions"), Pattern.compile("^(.+)\\.kt$"), TargetBackend.ANY, true);
        }

        @TestMetadata("extensionFunctions.kt")
        public void testExtensionFunctions() throws Exception {
            runTest("plugins/contracts/contracts-subplugins/testData/checkedExceptions/extensionFunctions.kt");
        }

        @TestMetadata("for.kt")
        public void testFor() throws Exception {
            runTest("plugins/contracts/contracts-subplugins/testData/checkedExceptions/for.kt");
        }

        @TestMetadata("if.kt")
        public void testIf() throws Exception {
            runTest("plugins/contracts/contracts-subplugins/testData/checkedExceptions/if.kt");
        }

        @TestMetadata("innerCatch.kt")
        public void testInnerCatch() throws Exception {
            runTest("plugins/contracts/contracts-subplugins/testData/checkedExceptions/innerCatch.kt");
        }

        @TestMetadata("multipleCatch.kt")
        public void testMultipleCatch() throws Exception {
            runTest("plugins/contracts/contracts-subplugins/testData/checkedExceptions/multipleCatch.kt");
        }

        @TestMetadata("runCatch.kt")
        public void testRunCatch() throws Exception {
            runTest("plugins/contracts/contracts-subplugins/testData/checkedExceptions/runCatch.kt");
        }

        @TestMetadata("simple.kt")
        public void testSimple() throws Exception {
            runTest("plugins/contracts/contracts-subplugins/testData/checkedExceptions/simple.kt");
        }

        @TestMetadata("throwWithContract.kt")
        public void testThrowWithContract() throws Exception {
            runTest("plugins/contracts/contracts-subplugins/testData/checkedExceptions/throwWithContract.kt");
        }

        @TestMetadata("when.kt")
        public void testWhen() throws Exception {
            runTest("plugins/contracts/contracts-subplugins/testData/checkedExceptions/when.kt");
        }
    }

    @TestMetadata("plugins/contracts/contracts-subplugins/testData/dslMarker")
    @TestDataPath("$PROJECT_ROOT")
    @RunWith(JUnit3RunnerWithInners.class)
    public static class DslMarker extends AbstractContextualEffectsDiagnosticTest {
        private void runTest(String testDataFilePath) throws Exception {
            KotlinTestUtils.runTest(this::doTest, TargetBackend.ANY, testDataFilePath);
        }

        public void testAllFilesPresentInDslMarker() throws Exception {
            KotlinTestUtils.assertAllTestsPresentByMetadata(this.getClass(), new File("plugins/contracts/contracts-subplugins/testData/dslMarker"), Pattern.compile("^(.+)\\.kt$"), TargetBackend.ANY, true);
        }

        @TestMetadata("simple.kt")
        public void testSimple() throws Exception {
            runTest("plugins/contracts/contracts-subplugins/testData/dslMarker/simple.kt");
        }
    }

    @TestMetadata("plugins/contracts/contracts-subplugins/testData/safeBuilders")
    @TestDataPath("$PROJECT_ROOT")
    @RunWith(JUnit3RunnerWithInners.class)
    public static class SafeBuilders extends AbstractContextualEffectsDiagnosticTest {
        private void runTest(String testDataFilePath) throws Exception {
            KotlinTestUtils.runTest(this::doTest, TargetBackend.ANY, testDataFilePath);
        }

        public void testAllFilesPresentInSafeBuilders() throws Exception {
            KotlinTestUtils.assertAllTestsPresentByMetadata(this.getClass(), new File("plugins/contracts/contracts-subplugins/testData/safeBuilders"), Pattern.compile("^(.+)\\.kt$"), TargetBackend.ANY, true);
        }

        @TestMetadata("badCases.kt")
        public void testBadCases() throws Exception {
            runTest("plugins/contracts/contracts-subplugins/testData/safeBuilders/badCases.kt");
        }

        @TestMetadata("complicatedBuilders.kt")
        public void testComplicatedBuilders() throws Exception {
            runTest("plugins/contracts/contracts-subplugins/testData/safeBuilders/complicatedBuilders.kt");
        }

        @TestMetadata("functionInvocationKinds.kt")
        public void testFunctionInvocationKinds() throws Exception {
            runTest("plugins/contracts/contracts-subplugins/testData/safeBuilders/functionInvocationKinds.kt");
        }

        @TestMetadata("if.kt")
        public void testIf() throws Exception {
            runTest("plugins/contracts/contracts-subplugins/testData/safeBuilders/if.kt");
        }

        @TestMetadata("loops.kt")
        public void testLoops() throws Exception {
            runTest("plugins/contracts/contracts-subplugins/testData/safeBuilders/loops.kt");
        }

        @TestMetadata("nestedBuild.kt")
        public void testNestedBuild() throws Exception {
            runTest("plugins/contracts/contracts-subplugins/testData/safeBuilders/nestedBuild.kt");
        }

        @TestMetadata("qualifiedThis.kt")
        public void testQualifiedThis() throws Exception {
            runTest("plugins/contracts/contracts-subplugins/testData/safeBuilders/qualifiedThis.kt");
        }

        @TestMetadata("setters.kt")
        public void testSetters() throws Exception {
            runTest("plugins/contracts/contracts-subplugins/testData/safeBuilders/setters.kt");
        }

        @TestMetadata("simple.kt")
        public void testSimple() throws Exception {
            runTest("plugins/contracts/contracts-subplugins/testData/safeBuilders/simple.kt");
        }

        @TestMetadata("when.kt")
        public void testWhen() throws Exception {
            runTest("plugins/contracts/contracts-subplugins/testData/safeBuilders/when.kt");
        }
    }

    @TestMetadata("plugins/contracts/contracts-subplugins/testData/transactions")
    @TestDataPath("$PROJECT_ROOT")
    @RunWith(JUnit3RunnerWithInners.class)
    public static class Transactions extends AbstractContextualEffectsDiagnosticTest {
        private void runTest(String testDataFilePath) throws Exception {
            KotlinTestUtils.runTest(this::doTest, TargetBackend.ANY, testDataFilePath);
        }

        public void testAllFilesPresentInTransactions() throws Exception {
            KotlinTestUtils.assertAllTestsPresentByMetadata(this.getClass(), new File("plugins/contracts/contracts-subplugins/testData/transactions"), Pattern.compile("^(.+)\\.kt$"), TargetBackend.ANY, true);
        }

        @TestMetadata("if.kt")
        public void testIf() throws Exception {
            runTest("plugins/contracts/contracts-subplugins/testData/transactions/if.kt");
        }

        @TestMetadata("loops.kt")
        public void testLoops() throws Exception {
            runTest("plugins/contracts/contracts-subplugins/testData/transactions/loops.kt");
        }

        @TestMetadata("multipleTransactions.kt")
        public void testMultipleTransactions() throws Exception {
            runTest("plugins/contracts/contracts-subplugins/testData/transactions/multipleTransactions.kt");
        }

        @TestMetadata("simple.kt")
        public void testSimple() throws Exception {
            runTest("plugins/contracts/contracts-subplugins/testData/transactions/simple.kt");
        }
    }
}
