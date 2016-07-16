/*
 * Open Source Software published under the Apache Licence, Version 2.0.
 */

package io.github.vocabhunter.analysis.session;

import io.github.vocabhunter.analysis.core.VocabHunterException;
import io.github.vocabhunter.analysis.marked.MarkedWord;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static io.github.vocabhunter.analysis.session.TestSessionStateTool.buildSession;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FormatHandlingTest {
    private static final String FORMAT_UNSUPPORTED_VERSION = "format-unsupported-version.wordy";

    private static final String FORMAT_1 = "format1.wordy";

    private static final String FORMAT_2 = "format2.wordy";

    private static final String FORMAT_3 = "format3.wordy";

    private static final String FORMAT_4 = "format4.wordy";

    private static final SessionState EXPECTED_STATE = buildSession();

    @Test(expected = VocabHunterException.class)
    public void testUnsupportedVersion() throws Exception {
        readState(FORMAT_UNSUPPORTED_VERSION);
    }

    @Test
    public void testVersion1() throws Exception {
        validate(FORMAT_1);
    }

    @Test
    public void testVersion2() throws Exception {
        validate(FORMAT_2);
    }

    @Test
    public void testVersion3() throws Exception {
        validate(FORMAT_3);
    }

    @Test
    public void testVersion4() throws Exception {
        validate(FORMAT_4);
    }

    private void validate(final String filename) throws Exception {
        Path file = getResourceFile(filename);
        EnrichedSessionState expected = new EnrichedSessionState(EXPECTED_STATE, file);

        validateMarkedWords(file, expected);
        validateState(file, expected);
    }

    private void validateState(final Path file, final EnrichedSessionState expected) {
        EnrichedSessionState actual = readState(file);

        Optional<Path> expectedFile = expected.getFile();
        Optional<Path> actualFile = actual.getFile();
        assertEquals("Session file reference", expectedFile, actualFile);

        SessionState expectedState = expected.getState();
        SessionState actualState = actual.getState();

        int expectedFormatVersion = expectedState.getFormatVersion();
        int actualFormatVersion = actualState.getFormatVersion();
        assertEquals("Format version", expectedFormatVersion, actualFormatVersion);

        String expectedName = expectedState.getName();
        String actualName = actualState.getName();
        assertEquals("Session name", expectedName, actualName);

        assertTrue("Equivalent", expectedState.isEquivalent(actualState));

        // This catch-all case should already be covered
        assertEquals("Session file", expected, actual);
    }

    private void validateMarkedWords(final Path file, final EnrichedSessionState expected) {
        List<SessionWord> expectedWords = expected.getState().getOrderedUses();
        List<? extends MarkedWord> actualWords = SessionSerialiser.readMarkedWords(file);

        validateMarkedWords(expectedWords, actualWords, MarkedWord::getWordIdentifier);
        validateMarkedWords(expectedWords, actualWords, MarkedWord::getState);
        validateMarkedWords(expectedWords, actualWords, MarkedWord::getUseCount);
    }

    private void validateMarkedWords(final List<? extends MarkedWord> expected, final List<? extends MarkedWord> actual, final Function<MarkedWord, Object> f) {
        List<Object> expectedValues = expected.stream()
            .map(f)
            .collect(toList());
        List<Object> actualValues = actual.stream()
            .map(f)
            .collect(toList());

        assertEquals("Values", expectedValues, actualValues);
    }

    private EnrichedSessionState readState(final String file) throws Exception {
        return readState(getResourceFile(file));
    }

    private EnrichedSessionState readState(final Path file) {
        return SessionSerialiser.read(file);
    }

    private Path getResourceFile(final String file) throws Exception {
        return Paths.get(FormatHandlingTest.class.getResource("/" + file).toURI());
    }
}
