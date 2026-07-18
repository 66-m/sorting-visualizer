package io.github.compilerstuck.control.ui.settings;

import io.github.compilerstuck.control.AppContext;
import io.github.compilerstuck.control.catalog.AlgorithmCatalog;
import io.github.compilerstuck.control.catalog.AlgorithmDescriptor;
import io.github.compilerstuck.control.config.ShuffleType;
import io.github.compilerstuck.control.ui.ComponentFactory;
import io.github.compilerstuck.control.ui.JCheckBoxList;
import io.github.compilerstuck.control.ui.StyledCard;
import io.github.compilerstuck.control.ui.UiTheme;
import io.github.compilerstuck.sortingalgorithms.SortingAlgorithm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Algorithm selection, run-all, and shuffle controls. */
public final class SortingPanel {
    private final AppContext app;
    private final Component dialogParent;

    private final ArrayList<SortingAlgorithm> algorithmList;
    private final ArrayList<ShuffleType> shuffleTypes;
    private final JComboBox<String> algorithmListComboBox;
    private final JCheckBox runAllCheckBox;
    private final JButton buttonRunAllSettings;
    private final JComboBox<String> shuffleListBox;
    private final StyledCard card;

    public SortingPanel(AppContext app, Component dialogParent) {
        this.app = app;
        this.dialogParent = dialogParent;

        card = ComponentFactory.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        algorithmList = new ArrayList<>();
        for (AlgorithmDescriptor descriptor : AlgorithmCatalog.all()) {
            SortingAlgorithm alg = descriptor.factory().apply(app.getArrayController(), app.getRenderContext());
            alg.setOperationReporter(app.getStateManager()::setCurrentOperation);
            algorithmList.add(alg);
        }

        algorithmListComboBox = ComponentFactory.createComboBox();
        for (SortingAlgorithm algorithm : algorithmList) {
            algorithmListComboBox.addItem(algorithm.getName());
        }
        algorithmListComboBox.setSelectedIndex(0);
        algorithmListComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        algorithmListComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, UiTheme.INPUT_HEIGHT));

        final int[] selectedAlgorithmIndex = {0};
        algorithmListComboBox.addActionListener(e -> {
            app.setAlgorithm(algorithmList.get(algorithmListComboBox.getSelectedIndex()));
            selectedAlgorithmIndex[0] = algorithmListComboBox.getSelectedIndex();
        });

        JPanel runAllRow = new JPanel();
        runAllRow.setLayout(new BoxLayout(runAllRow, BoxLayout.X_AXIS));
        runAllRow.setOpaque(false);
        runAllRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        runAllRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, UiTheme.BUTTON_HEIGHT));

        runAllCheckBox = ComponentFactory.createCheckBox("Run all");
        runAllCheckBox.setSelected(false);

        buttonRunAllSettings = ComponentFactory.createSmallButton("Configure");
        buttonRunAllSettings.setEnabled(false);
        buttonRunAllSettings.addActionListener(e -> showRunAllDialog(selectedAlgorithmIndex[0]));

        runAllCheckBox.addActionListener(e -> {
            algorithmListComboBox.setEnabled(!runAllCheckBox.isSelected());
            buttonRunAllSettings.setEnabled(runAllCheckBox.isSelected());
        });

        runAllRow.add(runAllCheckBox);
        runAllRow.add(Box.createHorizontalGlue());
        runAllRow.add(buttonRunAllSettings);

        shuffleTypes = new ArrayList<>(Arrays.asList(
                ShuffleType.RANDOM, ShuffleType.REVERSE,
                ShuffleType.ALMOST_SORTED, ShuffleType.SORTED
        ));

        shuffleListBox = ComponentFactory.createComboBox();
        for (ShuffleType st : shuffleTypes) {
            shuffleListBox.addItem(st.toString());
        }
        shuffleListBox.setSelectedIndex(0);
        shuffleListBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        shuffleListBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, UiTheme.INPUT_HEIGHT));
        shuffleListBox.addActionListener(e ->
                app.getArrayController().setShuffleType(shuffleTypes.get(shuffleListBox.getSelectedIndex()))
        );

        card.add(algorithmListComboBox);
        card.add(Box.createVerticalStrut(UiTheme.SPACING_SM));
        card.add(runAllRow);
        card.add(Box.createVerticalStrut(UiTheme.SPACING_SM));
        card.add(shuffleListBox);
    }

    public StyledCard getCard() {
        return card;
    }

    public boolean isRunAllSelected() {
        return runAllCheckBox.isSelected();
    }

    public List<SortingAlgorithm> getAlgorithmList() {
        return algorithmList;
    }

    public int getSelectedAlgorithmIndex() {
        return algorithmListComboBox.getSelectedIndex();
    }

    public void setInputsEnabled(boolean enabled) {
        algorithmListComboBox.setEnabled(enabled && !runAllCheckBox.isSelected());
        runAllCheckBox.setEnabled(enabled);
        shuffleListBox.setEnabled(enabled);
        buttonRunAllSettings.setEnabled(enabled && runAllCheckBox.isSelected());
    }

    private void showRunAllDialog(int selectedIndex) {
        DefaultListModel<JCheckBox> runAllSettings = new DefaultListModel<>();
        JCheckBoxList checkBoxList = new JCheckBoxList(runAllSettings);

        for (SortingAlgorithm alg : algorithmList) {
            JCheckBox algCheckBox = new JCheckBox(alg.getName());
            algCheckBox.setSelected(alg.isSelected());
            algCheckBox.addChangeListener(e -> alg.setSelected(algCheckBox.isSelected()));
            runAllSettings.addElement(algCheckBox);
        }

        final int[] dragFromIndex = {-1};
        final boolean[] wasSelected = {false};

        checkBoxList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragFromIndex[0] = checkBoxList.locationToIndex(e.getPoint());
                wasSelected[0] = !runAllSettings.getElementAt(dragFromIndex[0]).isSelected();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragFromIndex[0] = -1;
                wasSelected[0] = false;
            }
        });

        checkBoxList.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int currentDragIndex = checkBoxList.locationToIndex(e.getPoint());

                if (dragFromIndex[0] != -1 && currentDragIndex != -1 && dragFromIndex[0] != currentDragIndex) {
                    JCheckBox draggedItem = runAllSettings.getElementAt(dragFromIndex[0]);
                    runAllSettings.remove(dragFromIndex[0]);
                    runAllSettings.add(currentDragIndex, draggedItem);

                    SortingAlgorithm temp = algorithmList.get(dragFromIndex[0]);
                    algorithmList.remove(dragFromIndex[0]);
                    algorithmList.add(currentDragIndex, temp);

                    dragFromIndex[0] = currentDragIndex;
                    if (wasSelected[0] != draggedItem.isSelected()) {
                        draggedItem.setSelected(wasSelected[0]);
                    }
                }
            }
        });

        JDialog runAllSettingDialog = new JDialog();
        runAllSettingDialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                for (SortingAlgorithm alg : algorithmList) {
                    algorithmListComboBox.addItem(alg.getName());
                    algorithmListComboBox.removeItemAt(0);
                }
                algorithmListComboBox.setSelectedIndex(selectedIndex);
            }
        });

        runAllSettingDialog.setSize(350, 500);
        runAllSettingDialog.setLocationRelativeTo(dialogParent);
        runAllSettingDialog.setTitle("Configure Algorithm Execution Order");
        runAllSettingDialog.add(new JScrollPane(checkBoxList));
        runAllSettingDialog.setResizable(false);
        runAllSettingDialog.setModal(true);
        runAllSettingDialog.setVisible(true);
    }
}
