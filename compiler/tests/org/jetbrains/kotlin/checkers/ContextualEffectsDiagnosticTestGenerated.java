/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.checkers;

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
@TestMetadata("compiler/testData/diagnostics/contextualEffects")
@TestDataPath("$PROJECT_ROOT")
@RunWith(JUnit3RunnerWithInners.class)
public class ContextualEffectsDiagnosticTestGenerated extends AbstractContextualEffectsDiagnosticTest {
    private void runTest(String testDataFilePath) throws Exception {
        KotlinTestUtils.runTest(this::doTest, TargetBackend.ANY, testDataFilePath);
    }

    public void testAllFilesPresentInContextualEffects() throws Exception {
        KotlinTestUtils.assertAllTestsPresentByMetadata(this.getClass(), new File("compiler/testData/diagnostics/contextualEffects"), Pattern.compile("^(.+)\\.kt$"), TargetBackend.ANY, true);
    }

    @TestMetadata("compiler/testData/diagnostics/contextualEffects/checkedExceptions")
    @TestDataPath("$PROJECT_ROOT")
    @RunWith(JUnit3RunnerWithInners.class)
    public static class CheckedExceptions extends AbstractContextualEffectsDiagnosticTest {
        private void runTest(String testDataFilePath) throws Exception {
            KotlinTestUtils.runTest(this::doTest, TargetBackend.ANY, testDataFilePath);
        }

        public void testAllFilesPresentInCheckedExceptions() throws Exception {
            KotlinTestUtils.assertAllTestsPresentByMetadata(this.getClass(), new File("compiler/testData/diagnostics/contextualEffects/checkedExceptions"), Pattern.compile("^(.+)\\.kt$"), TargetBackend.ANY, true);
        }

        @TestMetadata("extensionFunctions.kt")
        public void testExtensionFunctions() throws Exception {
            runTest("compiler/testData/diagnostics/contextualEffects/checkedExceptions/extensionFunctions.kt");
        }

        @TestMetadata("for.kt")
        public void testFor() throws Exception {
            runTest("compiler/testData/diagnostics/contextualEffects/checkedExceptions/for.kt");
        }

        @TestMetadata("if.kt")
        public void testIf() throws Exception {
            runTest("compiler/testData/diagnostics/contextualEffects/checkedExceptions/if.kt");
        }

        @TestMetadata("innerCatch.kt")
        public void testInnerCatch() throws Exception {
            runTest("compiler/testData/diagnostics/contextualEffects/checkedExceptions/innerCatch.kt");
        }

        @TestMetadata("multipleCatch.kt")
        public void testMultipleCatch() throws Exception {
            runTest("compiler/testData/diagnostics/contextualEffects/checkedExceptions/multipleCatch.kt");
        }

        @TestMetadata("runCatch.kt")
        public void testRunCatch() throws Exception {
            runTest("compiler/testData/diagnostics/contextualEffects/checkedExceptions/runCatch.kt");
        }

        @TestMetadata("simple.kt")
        public void testSimple() throws Exception {
            runTest("compiler/testData/diagnostics/contextualEffects/checkedExceptions/simple.kt");
        }

        @TestMetadata("throwWithContract.kt")
        public void testThrowWithContract() throws Exception {
            runTest("compiler/testData/diagnostics/contextualEffects/checkedExceptions/throwWithContract.kt");
        }

        @TestMetadata("when.kt")
        public void testWhen() throws Exception {
            runTest("compiler/testData/diagnostics/contextualEffects/checkedExceptions/when.kt");
        }
    }
}
