/*
 * MegaMekLab - Copyright (C) 2017 - The MegaMek Team
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 */
package megameklab.com.ui.Dropship.tabs;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import megamek.common.Bay;
import megamek.common.Entity;
import megamek.common.InfantryBay;
import megamek.common.util.EncodeControl;
import megamek.common.verifier.TestAero;
import megameklab.com.ui.EntitySource;
import megameklab.com.util.IView;

/**
 * Tab for adding and modifying aerospace transport bays.
 * 
 * @author Neoancient
 *
 */
public class TransportTab extends IView {
    
    /**
     * 
     */
    private static final long serialVersionUID = 6288658666144030993L;
    
    private final JLabel lblMaxDoors = new JLabel();
    private final InstalledBaysModel modelInstalled = new InstalledBaysModel();
    private final JTable tblInstalled = new JTable(modelInstalled);
    private final AvailableBaysModel modelAvailable = new AvailableBaysModel();
    private final JTable tblAvailable = new JTable(modelAvailable);
    
    public TransportTab(EntitySource eSource) {
        super(eSource);
        initUI();
    }
    
    private void initUI() {
        ResourceBundle resourceMap = ResourceBundle.getBundle("megameklab.resources.Tabs", new EncodeControl()); //$NON-NLS-1$
        
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(new JLabel(resourceMap.getString("TransportTab.lblCurrentBays.text")), gbc); //$NON-NLS-1$
        
        gbc.gridy++;
        gbc.gridwidth = 1;
        add(new JLabel(resourceMap.getString("TransportTab.lblMaxDoors.text")), gbc); //$NON-NLS-1$
        gbc.gridx = 1;
        add(lblMaxDoors, gbc);
        
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        JButton btnRemoveBay = new JButton(resourceMap.getString("TransportTab.btnRemoveBay.text")); //$NON-NLS-1$
        btnRemoveBay.setToolTipText(resourceMap.getString("TransportTab.btnRemoveBay.tooltip")); //$NON-NLS-1$
        add(btnRemoveBay, gbc);
        
        gbc.gridy++;
        gbc.gridwidth = 3;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        add(new JScrollPane(tblInstalled), gbc);
        
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        add(new JLabel(resourceMap.getString("TransportTab.lblAvailableBays.text")), gbc); //$NON-NLS-1$
        
        gbc.gridy = 2;
        JButton btnAddBay = new JButton(resourceMap.getString("TransportTab.btnAddBay.text")); //$NON-NLS-1$
        btnAddBay.setToolTipText(resourceMap.getString("TransportTab.btnAddBay.tooltip")); //$NON-NLS-1$
        add(btnAddBay, gbc);
        
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        add(new JScrollPane(tblAvailable), gbc);
        
        tblInstalled.setRowHeight(24);
        TableColumn col = tblInstalled.getColumnModel().getColumn(InstalledBaysModel.COL_SIZE);
        col.setCellEditor(new SpinnerCellEditor(InstalledBaysModel.COL_SIZE));
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setToolTipText(resourceMap.getString("TransportTab.colSize.tooltip")); //$NON-NLS-1$
        col.setCellRenderer(renderer);
        col = tblInstalled.getColumnModel().getColumn(InstalledBaysModel.COL_DOORS);
        col.setCellEditor(new SpinnerCellEditor(InstalledBaysModel.COL_DOORS));
        renderer = new DefaultTableCellRenderer();
        renderer.setToolTipText(resourceMap.getString("TransportTab.colDoors.tooltip")); //$NON-NLS-1$
        col.setCellRenderer(renderer);
        col = tblInstalled.getColumnModel().getColumn(InstalledBaysModel.COL_TONNAGE);
        renderer = new DefaultTableCellRenderer();
        renderer.setToolTipText(resourceMap.getString("TransportTab.colTonnage.tooltip")); //$NON-NLS-1$
        col.setCellRenderer(renderer);
        col = tblInstalled.getColumnModel().getColumn(InstalledBaysModel.COL_PERSONNEL);
        renderer = new DefaultTableCellRenderer();
        renderer.setToolTipText(resourceMap.getString("TransportTab.colPersonnel.tooltip")); //$NON-NLS-1$
        col.setCellRenderer(renderer);
        tblInstalled.setShowGrid(false);
        tblInstalled.setIntercellSpacing(new Dimension(0, 0));
        tblAvailable.setShowGrid(false);
        tblAvailable.setIntercellSpacing(new Dimension(0, 0));
        
        refresh();
    }
    
    public void refresh() {
        lblMaxDoors.setText(String.valueOf(TestAero.maxBayDoors(getAero())));
        modelInstalled.refreshBays();
        modelAvailable.refreshBays();
    }
    
    private int doorsAvailable() {
        int total = TestAero.maxBayDoors(getAero());
        for (Bay bay : getAero().getTransportBays()) {
            total -= bay.getDoors();
        }
        return Math.max(total, 0);
    }
    
    private class InstalledBaysModel extends AbstractTableModel {
        
        /**
         * 
         */
        private static final long serialVersionUID = -8643492032818089043L;
        
        private static final int COL_NAME      = 0;
        private static final int COL_SIZE      = 1;
        private static final int COL_DOORS     = 2;
        private static final int COL_TONNAGE   = 3;
        private static final int COL_PERSONNEL = 4;
        private static final int NUM_COLS      = 5;
        
        private final List<Bay> bayList = new ArrayList<>();
        private final List<TestAero.TransportBay> bayTypeList = new ArrayList<>();
        
        public void refreshBays() {
            bayList.clear();
            bayTypeList.clear();
            for (Bay bay : getAero().getTransportBays()) {
                TestAero.TransportBay bayType = TestAero.TransportBay.getBayType(bay);
                bayList.add(bay);
                bayTypeList.add(bayType);
            }
            fireTableDataChanged();
        }
        
        @Override
        public String getColumnName(int column) {
            switch (column) {
                case COL_NAME:
                    return "Bay Type";
                case COL_SIZE:
                    return "Size";
                case COL_DOORS:
                    return "Doors";
                case COL_TONNAGE:
                    return "Tonnage";
                case COL_PERSONNEL:
                    return "Personnel";
            }
            return "";
        }

        @Override
        public int getRowCount() {
            return bayList.size();
        }
        
        @Override
        public int getColumnCount() {
            return NUM_COLS;
        }
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case COL_NAME:
                    return bayTypeList.get(rowIndex).getDisplayName();
                case COL_SIZE:
                    if (bayTypeList.get(rowIndex) != TestAero.TransportBay.CARGO) {
                        return (int)bayList.get(rowIndex).getUnusedSlots();
                    }
                    return bayList.get(rowIndex).getUnusedSlots();
                case COL_DOORS:
                    return bayList.get(rowIndex).getDoors();
                case COL_PERSONNEL:
                    if (bayList.get(rowIndex) instanceof InfantryBay) {
                        return "*";
                    }
                    return bayTypeList.get(rowIndex).getPersonnel()
                            * (int)bayList.get(rowIndex).getCapacity();
                case COL_TONNAGE:
                    return bayList.get(rowIndex).getWeight();
                default:
                    return null;
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if (columnIndex == COL_DOORS) {
                return getAero().hasETypeFlag(Entity.ETYPE_DROPSHIP)
                        || getAero().hasETypeFlag(Entity.ETYPE_JUMPSHIP);
            }
            return (columnIndex == COL_SIZE);
        }

    }
    
    private class AvailableBaysModel extends AbstractTableModel {
        
        /**
         * 
         */
        private static final long serialVersionUID = -5456813671712646392L;
        
        private static final int COL_NAME      = 0;
        private static final int COL_SIZE      = 1;
        private static final int COL_PERSONNEL = 2;
        private static final int NUM_COLS      = 3;
        
        private List<TestAero.TransportBay> bayList = new ArrayList<>();
        
        public void refreshBays() {
            bayList.clear();
            for (TestAero.TransportBay bay : TestAero.TransportBay.values()) {
                if (eSource.getTechManager().isLegal(bay.getTechAdvancement())) {
                    bayList.add(bay);
                }
            }
            fireTableDataChanged();
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case COL_NAME:
                    return "Bay Type";
                case COL_SIZE:
                    return "Unit Weight";
                case COL_PERSONNEL:
                    return "Personnel";
            }
            return "";
        }

        @Override
        public int getRowCount() {
            return bayList.size();
        }

        @Override
        public int getColumnCount() {
            return NUM_COLS;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (rowIndex < bayList.size()) {
                switch (columnIndex) {
                    case COL_NAME:
                        return bayList.get(rowIndex).getDisplayName();
                    case COL_SIZE:
                        return bayList.get(rowIndex).getWeight();
                    case COL_PERSONNEL:
                        if (bayList.get(rowIndex).getDisplayName().startsWith("Infantry")) {
                            return "*";
                        }
                        return bayList.get(rowIndex).getPersonnel();
                }
            }            
            return "?";
        }
        
    }
    
    private class SpinnerCellEditor extends AbstractCellEditor implements TableCellEditor, ChangeListener {
        
        /**
         * 
         */
        private static final long serialVersionUID = -5334192308060664513L;
        
        private final JSpinner spinner = new JSpinner();
        private final int column;
        
        SpinnerCellEditor(int column) {
            this.column = column;
            spinner.addChangeListener(this);
        }

        @Override
        public Object getCellEditorValue() {
            return spinner.getValue();
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            int row = tblInstalled.convertRowIndexToModel(tblInstalled.getSelectedRow());
            if ((row < 0) || (row >= modelInstalled.bayList.size())) {
                return;
            }
            final Bay bay = modelInstalled.bayList.get(row);
            if (column == InstalledBaysModel.COL_SIZE) {
                Bay newBay = modelInstalled.bayTypeList.get(row).newBay((Double)getCellEditorValue(),
                        bay.getBayNumber());
                newBay.setDoors(bay.getDoors());
                getAero().removeTransporter(bay);
                getAero().addTransporter(newBay);
                modelInstalled.bayList.set(row, newBay);
            } else if (column == InstalledBaysModel.COL_DOORS) {
                modelInstalled.bayList.get(row).setDoors((Integer)getCellEditorValue());
            }
            modelInstalled.fireTableRowsUpdated(row, row);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
                int column) {
            boolean isCargo = modelInstalled.bayTypeList.get(row) == TestAero.TransportBay.CARGO;
            if (column == InstalledBaysModel.COL_DOORS) {
                int doors = ((Integer)modelInstalled.getValueAt(row,  column)).intValue();
                SpinnerNumberModel model = new SpinnerNumberModel(doors,
                        isCargo? 0 : 1, doorsAvailable() + doors, 1);
                spinner.removeChangeListener(this);
                spinner.setModel(model);
                spinner.addChangeListener(this);
                return spinner;
            } else if (column == InstalledBaysModel.COL_SIZE) {
                double step = isCargo? 0.5 : 1.0;
                SpinnerNumberModel model = new SpinnerNumberModel(modelInstalled.bayList.get(row).getUnusedSlots(),
                            step, null, step);
                spinner.removeChangeListener(this);
                spinner.setModel(model);
                spinner.addChangeListener(this);
                return spinner;
            }
            return null;
        }
        
    }
}
