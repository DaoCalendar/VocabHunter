/*
 * Open Source Software published under the Apache Licence, Version 2.0.
 */

package io.github.vocabhunter.gui.main;

import io.github.vocabhunter.analysis.core.CoreConstants;
import io.github.vocabhunter.analysis.core.VocabHunterException;
import io.github.vocabhunter.analysis.session.EnrichedSessionState;
import io.github.vocabhunter.analysis.session.SessionSerialiser;
import io.github.vocabhunter.analysis.settings.FileListManager;
import io.github.vocabhunter.analysis.settings.FileListManagerImpl;
import io.github.vocabhunter.gui.common.EnvironmentManager;
import io.github.vocabhunter.gui.common.WebPageTool;
import io.github.vocabhunter.gui.dialogues.FileDialogue;
import io.github.vocabhunter.gui.dialogues.FileDialogueFactory;
import io.github.vocabhunter.gui.dialogues.FileDialogueType;
import io.github.vocabhunter.gui.settings.SettingsManager;
import io.github.vocabhunter.gui.settings.SettingsManagerImpl;
import io.github.vocabhunter.test.utils.TestFileManager;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.picocontainer.MutablePicoContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testfx.api.FxRobot;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.github.vocabhunter.gui.common.GuiConstants.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.api.FxToolkit.registerPrimaryStage;
import static org.testfx.api.FxToolkit.setupApplication;
import static org.testfx.matcher.base.NodeMatchers.*;

@RunWith(MockitoJUnitRunner.class)
public class GuiTest extends FxRobot {
    private static final String BOOK1 = "great-expectations.txt";

    private static final String BOOK2 = "oliver-twist.txt";

    private static final int SCREEN_WIDTH = 1366;

    private static final int SCREEN_HEIGHT = 768;

    private static final Logger LOG = LoggerFactory.getLogger(GuiTest.class);

    private TestFileManager manager;

    @Mock
    EnvironmentManager environmentManager;

    @Mock
    private FileDialogueFactory fileDialogueFactory;

    @Mock
    private FileDialogue newSessionDialogue;

    @Mock
    private FileDialogue saveSessionDialogue;

    @Mock
    private FileDialogue openSessionDialogue;

    @Mock
    private FileDialogue exportDialogue;

    @Mock
    private WebPageTool webPageTool;

    @Captor
    private ArgumentCaptor<String> webPageCaptor;

    private Path exportFile;

    private Path sessionFile;

    private int stepNo;

    @BeforeClass
    public static void setupSpec() throws Exception {
        if (Boolean.getBoolean("headless")) {
            System.setProperty("testfx.robot", "glass");
            System.setProperty("testfx.headless", "true");
            System.setProperty("prism.order", "sw");
            System.setProperty("prism.text", "t2k");
            System.setProperty("java.awt.headless", "true");
        }
        registerPrimaryStage();
    }

    @Before
    public void setUp() throws Exception {
        when(environmentManager.getScreenSize()).thenReturn(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        when(environmentManager.useSystemMenuBar()).thenReturn(false);

        manager = new TestFileManager(getClass());
        exportFile = manager.addFile("export.txt");
        sessionFile = manager.addFile("session.wordy");

        Path document1 = getResource(BOOK1);
        Path document2 = getResource(BOOK2);

        setUpFileDialogue(FileDialogueType.NEW_SESSION, newSessionDialogue, document1, document2);
        setUpFileDialogue(FileDialogueType.SAVE_SESSION, saveSessionDialogue, sessionFile);
        setUpFileDialogue(FileDialogueType.OPEN_SESSION, openSessionDialogue, sessionFile);
        setUpFileDialogue(FileDialogueType.EXPORT_SELECTION, exportDialogue, exportFile);

        Path settingsFile = manager.addFile(SettingsManagerImpl.SETTINGS_JSON);
        SettingsManager settingsManager = new SettingsManagerImpl(settingsFile);
        Path fileListManagerFile = manager.addFile(FileListManagerImpl.SETTINGS_JSON);
        FileListManager fileListManager = new FileListManagerImpl(fileListManagerFile);

        MutablePicoContainer pico = GuiContainerBuilder.createBaseContainer();
        pico.addComponent(settingsManager);
        pico.addComponent(fileListManager);
        pico.addComponent(fileDialogueFactory);
        pico.addComponent(environmentManager);
        pico.addComponent(webPageTool);
        VocabHunterGuiExecutable.setPico(pico);

        setupApplication(VocabHunterGuiExecutable.class);
    }

    private void setUpFileDialogue(final FileDialogueType type, final FileDialogue dialogue, final Path file1, final Path... files) {
        when(fileDialogueFactory.create(eq(type), any(Stage.class))).thenReturn(dialogue);
        when(dialogue.isFileSelected()).thenReturn(true);
        when(dialogue.getSelectedFile()).thenReturn(file1, files);
    }

    @After
    public void tearDown() throws Exception {
        manager.cleanup();
    }

    @Test
    public void testWalkThrough() {
        part1BasicWalkThrough();
        part2StartNewSessionAndFilter();
        part3ReopenFirstSession();
        part4AboutDialogue();
        part5WebLinks();
    }

    private void part1BasicWalkThrough() {
        step("Open application", () -> {
            verifyThat("#mainBorderPane", isVisible());
        });

        step("Start new session", () -> {
            clickOn("#buttonNew");
            verifyThat("#mainWordPane", isVisible());
            verifyThat("#mainWord", hasText("and"));
        });

        step("Mark word as known", () -> {
            clickOn("#buttonKnown");
            verifyThat("#mainWord", hasText("the"));
        });

        step("Mark word as unknown", () -> {
            clickOn("#buttonUnknown");
            verifyThat("#mainWord", hasText("to"));
        });

        step("Show selection", () -> {
            clickOn("#buttonEditOff");
            verifyThat("#buttonKnown", isInvisible());
        });

        step("Return to edit mode", () -> {
            clickOn("#buttonEditOn");
            verifyThat("#buttonKnown", isVisible());
        });

        step("Export the selection", () -> {
            clickOn("#buttonExport");
            String text = null;
            text = readFile(exportFile);
            assertThat("Export file content", text, containsString("the"));
        });

        step("Save the session", () -> {
            clickOn("#buttonSave");
            validateSavedSession(BOOK1);
        });
    }

    private void part2StartNewSessionAndFilter() {
        step("Open a new session for a different book", () -> {
            clickOn("#buttonNew");
            verifyThat("#mainWord", hasText("the"));
        });

        step("Define filter", () -> {
            clickOn("#buttonSetupFilters");
            doubleClickOn("#fieldMinimumLetters").write("6");
            doubleClickOn("#fieldMinimumOccurrences").write("4");
            clickOn("#fieldInitialCapital");
            clickOn("#buttonOk");
            verifyThat("#mainWord", hasText("surgeon"));
        });

        step("Mark filtered word as known", () -> {
            clickOn("#buttonKnown");
            verifyThat("#mainWord", hasText("workhouse"));
        });

        step("Disable filter", () -> {
            clickOn("#buttonEnableFilters");
            verifyThat("#mainWord", hasText("workhouse"));
        });
    }

    private void part3ReopenFirstSession() {
        step("Re-open the old session", () -> {
            clickOn("#buttonOpen");
            clickOn("Discard");
            verifyThat("#mainWord", hasText("a"));
        });
    }

    private void part4AboutDialogue() {
        step("Open About dialogue", () -> {
            clickOn("#menuHelp");
            clickOn("#menuAbout");
            verifyThat("#aboutDialogue", isVisible());
        });

        step("Open website from About dialogue", () -> {
            clickOn("#linkWebsite");
            validateWebPage(WEBSITE);
        });

        step("Close About dialogue", () -> {
            clickOn("#buttonClose");
        });
    }

    private void part5WebLinks() {
        step("Open website", () -> {
            clickOn("#menuHelp");
            clickOn("#menuWebsite");
            validateWebPage(WEBSITE);
        });

        step("Open VocabHunter How To", () -> {
            clickOn("#menuHelp");
            clickOn("#menuHowTo");
            validateWebPage(WEBPAGE_HELP);
        });

        step("Open Issue Reporting", () -> {
            clickOn("#menuHelp");
            clickOn("#menuIssue");
            validateWebPage(WEBPAGE_ISSUE);
        });
    }

    private void validateWebPage(final String webpageHelp) {
        verify(webPageTool, atLeastOnce()).showWebPage(webPageCaptor.capture());
        assertEquals("Website", webpageHelp, webPageCaptor.getValue());
    }

    private String readFile(final Path file) {
        try {
            return new String(Files.readAllBytes(file), CoreConstants.CHARSET);
        } catch (IOException e) {
            throw new VocabHunterException(String.format("Unable to read file %s", file), e);
        }
    }

    private void validateSavedSession(final String name) {
        EnrichedSessionState state = SessionSerialiser.read(sessionFile);

        assertEquals("Session state name", name, state.getState().getName());
    }

    private Path getResource(final String file) throws Exception {
        URL url = getClass().getResource("/" + file);

        if (url == null) {
            throw new VocabHunterException(String.format("Unable to load %s", file));
        } else {
            return Paths.get(url.toURI());
        }
    }

    private void step(final String step, final Runnable runnable) {
        ++stepNo;
        LOG.info("STEP {}: Begin - {}", stepNo, step);
        runnable.run();
        LOG.info("STEP {}:   End - {}", stepNo, step);
    }
}
