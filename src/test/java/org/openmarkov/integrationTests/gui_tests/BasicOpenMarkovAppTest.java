package org.openmarkov.integrationTests.gui_tests;

import org.assertj.swing.core.MouseButton;
import org.assertj.swing.fixture.JPanelFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.integrationTests.IntegrationTest;
import org.openmarkov.java.classUtils.ClassUtils;

import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This test verifies basic functionalities of the interface.
 * <p>
 * If they were to fail, OpenMarkov would have no meaning.
 *
 * @author jrico
 */
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "Your machine does not have a Graphic Environment")
public class BasicOpenMarkovAppTest extends BaseOpenMarkovAppTest {
    
    /**
     * Opens a network and then changes the name of a node, verifying the node's name change was successfully changed.
     */
    @Test
    void testChangeNodeName() {
        OpenNetworkResult result = this.openNetwork(ClassUtils.getResourceAsFile(IntegrationTest.class, "/networks/bn/BN-catarnet.pgmx"));
        ProbNet probNet = result.probNet();
        JPanelFixture editorPanelFixture = result.editorPanel();
        var nodeClosestToLeftUpCorner = probNet
                .getNodes().stream().min(
                        Comparator.comparingDouble(node -> Math.sqrt(Math.pow(node.getCoordinateX(), 2) + Math.pow(node.getCoordinateY(), 2))))
                .get();
        // Double-click on the node to open the Node Properties dialog directly (EditorInputHandler
        // opens NodePropertiesDialog on double-click in edition mode).
        editorPanelFixture.robot().click(
                editorPanelFixture.target(),
                new java.awt.Point((int) nodeClosestToLeftUpCorner.getCoordinateX(), (int) nodeClosestToLeftUpCorner.getCoordinateY()),
                MouseButton.LEFT_BUTTON, 2);
        var nodePropertiesDialog = this.window.dialog("NodePropertiesDialog");
        
        String originalNodeName = nodeClosestToLeftUpCorner.getName();
        String newNodeName = originalNodeName + "_Test";
        assertNotNull(probNet.getNode(originalNodeName));
        assertNull(probNet.getNode(newNodeName));
        nodePropertiesDialog.textBox("jTextFieldNodeName").setText(newNodeName);
        nodePropertiesDialog.button("jButtonApply").click();
        assertNull(probNet.getNode(originalNodeName));
        assertNotNull(probNet.getNode(newNodeName));
    }
    
    /**
     * Opens a network and then creates a node by clicking on the "Chance Node Creation" mode and clicking on an empty
     * space, verifying a node "A" was successfully created.
     */
    @Test
    void testAddNode() {
        OpenNetworkResult result = this.openNetwork(ClassUtils.getResourceAsFile(IntegrationTest.class, "/EmptyNetwork.pgmx"));
        ProbNet probNet = result.probNet();
        JPanelFixture editorPanelFixture = result.editorPanel();
        this.window.toggleButton("ChanceCreationMode").check();
        
        assertNull(probNet.getNode("A"));
        // Click on the empty space of the left up corner
        editorPanelFixture.robot()
                          .click(editorPanelFixture.target(), new java.awt.Point(10, 10), MouseButton.LEFT_BUTTON, 1);
        assertNotNull(probNet.getNode("A"));
    }
    
    /**
     * Opens a network and then changes deletes a node, verifying the node was successfully deleted.
     */
    @Test
    void testDeleteNode() {
        OpenNetworkResult result = this.openNetwork(ClassUtils.getResourceAsFile(IntegrationTest.class, "/networks/bn/BN-catarnet.pgmx"));
        ProbNet probNet = result.probNet();
        JPanelFixture editorPanelFixture = result.editorPanel();
        var nodeClosestToLeftUpCorner = probNet
                .getNodes().stream().min(
                        Comparator.comparingDouble(node -> Math.sqrt(Math.pow(node.getCoordinateX(), 2) + Math.pow(node.getCoordinateY(), 2))))
                .get();
        // Click on the node
        editorPanelFixture.robot().click(
                editorPanelFixture.target(),
                new java.awt.Point((int) nodeClosestToLeftUpCorner.getCoordinateX(), (int) nodeClosestToLeftUpCorner.getCoordinateY()),
                MouseButton.LEFT_BUTTON, 1);
        
        String originalNodeName = nodeClosestToLeftUpCorner.getName();
        assertNotNull(probNet.getNode(originalNodeName));
        editorPanelFixture.pressAndReleaseKeys(java.awt.event.KeyEvent.VK_DELETE);
        assertNull(probNet.getNode(originalNodeName));
    }
    
    /**
     * Opens a network and then creates a node by clicking on the "Chance Node Creation" mode and clicking on two empty
     * spaces to create a node "A" and "B", then it clicks on the Link Creation button from the menu and clicks from "A"
     * to "B" in order to create a link, verifying the successful creation.
     * <p>
     * After the link is created, it moves to Selection mode and clicks on the link and pressed "Delete" to verify it
     * has been successfully deleted.
     */
    @Test
    void testAddAndRemoveLink() {
        OpenNetworkResult result = this.openNetwork(ClassUtils.getResourceAsFile(IntegrationTest.class, "/EmptyNetwork.pgmx"));
        ProbNet probNet = result.probNet();
        JPanelFixture editorPanelFixture = result.editorPanel();
        this.window.toggleButton("ChanceCreationMode").check();
        
        editorPanelFixture.robot()
                          .click(editorPanelFixture.target(), new java.awt.Point(50, 30), MouseButton.LEFT_BUTTON, 1);
        assertNotNull(probNet.getNode("A"));
        editorPanelFixture.robot()
                          .click(editorPanelFixture.target(), new java.awt.Point(150, 30), MouseButton.LEFT_BUTTON, 1);
        assertNotNull(probNet.getNode("B"));
        
        
        this.window.toggleButton("LinkCreationMode").check();
        editorPanelFixture.robot()
                          .pressMouse(editorPanelFixture.target(), new java.awt.Point(50, 30), MouseButton.LEFT_BUTTON);
        editorPanelFixture.robot().moveMouse(editorPanelFixture.target(), new java.awt.Point(150, 30));
        editorPanelFixture.robot().releaseMouse(MouseButton.LEFT_BUTTON);
        assertNotNull(probNet.getLink(probNet.getNode("A"), probNet.getNode("B"), true));
        
        this.window.toggleButton("ObjectSelectionMode").check();
        editorPanelFixture.robot()
                          .pressMouse(editorPanelFixture.target(), new java.awt.Point(100, 30), MouseButton.LEFT_BUTTON);
        editorPanelFixture.robot().releaseMouse(MouseButton.LEFT_BUTTON);
        editorPanelFixture.pressAndReleaseKeys(java.awt.event.KeyEvent.VK_DELETE);
        assertNull(probNet.getLink(probNet.getNode("A"), probNet.getNode("B"), true));
    }
    
}











