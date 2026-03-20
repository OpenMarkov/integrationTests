package org.openmarkov.integrationTests.gui_tests;

import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JPanelFixture;
import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.full.OpenMarkov;
import org.openmarkov.gui.configuration.LocalPreference;
import org.openmarkov.gui.configuration.LocalPreferences;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;

import javax.swing.*;
import java.awt.*;
import java.io.File;

import static org.assertj.swing.core.BasicRobot.robotWithCurrentAwtHierarchy;

/**
 * Common class to test functionalities in the user version of OpenMarkov.
 *
 * @author jrico
 */
public abstract class BaseOpenMarkovAppTest extends BaseWindowTest<FrameFixture, Frame> {
    
    /**
     * The window is set-up by starting the {@link OpenMarkov} application and getting the {@link MainGUI} shown after
     * the splash screen.
     * <p>
     * The {@link FrameFixture} is the resulting {@link MainGUI}, which must be {@link MainGUI#INSTANCE}.
     * <p>
     * Before setting the application, the {@link org.openmarkov.full.HoverLoggerPlugin} is enabled, and storing
     * preferences are disabled in order to preserve the developer's preferences (otherwise, it would be highly possible
     * a test might override at least one {@link LocalPreference}).
     *
     * @return A {@link FrameFixture} containing the {@link MainGUI#INSTANCE}.
     */
    @Override protected FrameFixture setUpWindow() {
        LocalPreference.IGNORE_STORAGE = true;
        LocalPreferences.HOVER_LOGGER_ENABLED.set(true);
        org.assertj.swing.launcher.ApplicationLauncher.application(OpenMarkov.class).start();
        return WindowFinder.findFrame(new GenericTypeMatcher<>(Frame.class) {
            @Override protected boolean isMatching(Frame component) {
                return component.isShowing() && component instanceof MainGUI;
            }
        }).using(robotWithCurrentAwtHierarchy());
    }
    
    /**
     * Opens a network by using the mouse and keyboard on OpenMarkov's interface.
     *
     * @param netFileToOpen The network file to open.
     * @return Both the {@link NetworkEditorPanel} and the {@link ProbNet}.
     */
    public @NotNull BaseOpenMarkovAppTest.OpenNetworkResult openNetwork(File netFileToOpen) {
        this.window.menuItem("File").click();
        this.window.menuItem("File.Open").click();
        var openDialog = this.window.fileChooser("NetworkOMFileChooser");
        openDialog.selectFile(netFileToOpen);
        openDialog.approveButton().click();
        this.window.comboBox("ZoomComboBox").selectItem("100%");
        var editorPanel = this.window.panel(new GenericTypeMatcher<>(JPanel.class) {
            @Override protected boolean isMatching(JPanel component) {
                return component.isShowing() && component instanceof NetworkEditorPanel;
            }
        });
        ProbNet probNet = ((NetworkEditorPanel) editorPanel.target()).getNetworkPanel().getProbNet();
        return new OpenNetworkResult(editorPanel, probNet);
    }
    
    public record OpenNetworkResult(JPanelFixture editorPanel, ProbNet probNet) {
    }
    
}











