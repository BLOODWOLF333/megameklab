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
package megameklab.com.ui.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import megamek.common.Aero;
import megamek.common.EquipmentType;
import megamek.common.Mech;
import megamek.common.Mounted;
import megamek.common.util.EncodeControl;
import megameklab.com.ui.util.CustomComboBox;
import megameklab.com.util.UnitUtil;

/**
 * Controls for selecting type and number of heat sinks for mechs and asfs.
 * 
 * @author Neoancient
 *
 */
public class HeatSinkView extends MainUIView implements ActionListener, ChangeListener {
    
    /**
     * 
     */
    private static final long serialVersionUID = -3310994380270514088L;

    public interface HeatSinkListener {
        void heatSinksChanged(int index, int count);
        void heatSinksChanged(EquipmentType hsType, int count);
        void heatSinkBaseCountChanged(int count);
    }
    private final List<HeatSinkListener> listeners = new CopyOnWriteArrayList<>();
    public void addListener(HeatSinkListener l) {
        listeners.add(l);
    }
    public void removeListener(HeatSinkListener l) {
        listeners.remove(l);
    }
    
    public final static int TYPE_SINGLE      = 0;
    public final static int TYPE_DOUBLE_IS   = 1;
    public final static int TYPE_DOUBLE_AERO = 1; // ASFs simply use an index and don't distinguish between IS and Clan
    public final static int TYPE_DOUBLE_CLAN = 2;
    public final static int TYPE_COMPACT     = 3;
    public final static int TYPE_LASER       = 4;
    public final static int TYPE_PROTOTYPE   = 5;
    public final static int TYPE_FREEZER     = 6;
    
    private final static String[] LOOKUP_NAMES = {
            "Heat Sink", "ISDoubleHeatSink", "CLDoubleHeatSink", "IS1 Compact Heat Sink",
            "CLLaser Heat Sink", "ISDoubleHeatSinkPrototype", "ISDoubleHeatSinkFreezer"
    };
    private final List<EquipmentType> heatSinks;
    private String[] mechDisplayNames;
    private String[] aeroDisplayNames;

    private final CustomComboBox<Integer> cbHSType = new CustomComboBox<>(i -> getDisplayName(i));
    private final JSpinner spnCount = new JSpinner();
    private final JSpinner spnBaseCount = new JSpinner();
    private final JLabel lblFreeText = new JLabel();
    private final JLabel lblFreeCount = new JLabel();
    
    private SpinnerNumberModel countModel = new SpinnerNumberModel(0, 0, 100, 1);
    private SpinnerNumberModel baseCountModel = new SpinnerNumberModel(0, 0, 100, 1);
    
    private final ITechManager techManager;
    private boolean isAero;
    private boolean isPrimitive;
    
    public HeatSinkView(ITechManager techManager) {
        this.techManager = techManager;
        heatSinks = new ArrayList<>();
        for (String key : LOOKUP_NAMES) {
            heatSinks.add(EquipmentType.get(key));
        }
        initUI();
    }
    
    private void initUI() {
        ResourceBundle resourceMap = ResourceBundle.getBundle("megameklab.resources.Views", new EncodeControl()); //$NON-NLS-1$
        mechDisplayNames = resourceMap.getString("HeatSinkView.mechNames.values").split(","); //$NON-NLS-1$
        aeroDisplayNames = resourceMap.getString("HeatSinkView.aeroNames.values").split(","); //$NON-NLS-1$
        
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(createLabel(resourceMap.getString("HeatSinkView.cbHSType.text"), labelSize), gbc); //$NON-NLS-1$
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        setFieldSize(cbHSType, controlSize);
        cbHSType.setToolTipText(resourceMap.getString("HeatSinkView.cbHSType.tooltip")); //$NON-NLS-1$
        add(cbHSType, gbc);
        cbHSType.addActionListener(this);
        
        spnCount.setModel(countModel);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        add(createLabel(resourceMap.getString("HeatSinkView.spnCount.text"), labelSize), gbc); //$NON-NLS-1$
        gbc.gridx = 1;
        gbc.gridy = 1;
        setFieldSize(spnCount.getEditor(), spinnerEditorSize);
        spnCount.setToolTipText(resourceMap.getString("HeatSinkView.spnCount.tooltip")); //$NON-NLS-1$
        add(spnCount, gbc);
        spnCount.addChangeListener(this);

        gbc.gridx = 3;
        gbc.gridy = 1;
        lblFreeText.setText(resourceMap.getString("HeatSinkView.lblFree.text"));
        add(lblFreeText, gbc);
        gbc.gridx = 4;
        gbc.gridy = 1;
        lblFreeCount.setToolTipText(resourceMap.getString("HeatSinkView.lblFree.tooltip")); //$NON-NLS-1$
        add(lblFreeCount, gbc);

        spnBaseCount.setModel(baseCountModel);
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(createLabel(resourceMap.getString("HeatSinkView.spnBaseCount.text"), labelSize), gbc); //$NON-NLS-1$
        gbc.gridx = 1;
        gbc.gridy = 2;
        setFieldSize(spnBaseCount.getEditor(), spinnerEditorSize);
        spnBaseCount.setToolTipText(resourceMap.getString("HeatSinkView.spnBaseCount.tooltip")); //$NON-NLS-1$
        add(spnBaseCount, gbc);
        spnBaseCount.addChangeListener(this);
    }
    
    private String getDisplayName(int index) {
        return isAero? aeroDisplayNames[index] : mechDisplayNames[index];
    }
    
    public void setFromMech(Mech mech) {
        isAero = false;
        isPrimitive = mech.isPrimitive();
        refresh();
        Optional<EquipmentType> hs = mech.getMisc().stream().map(Mounted::getType)
                .filter(et -> UnitUtil.isHeatSink(et)).findAny();
        if (hs.isPresent()) {
            cbHSType.removeActionListener(this);
            setHeatSinkType(hs.get());
            cbHSType.addActionListener(this);
        }
        int totalSinks = mech.heatSinks(false);
        spnCount.removeChangeListener(this);
        countModel.setValue(totalSinks);
        countModel.setMinimum(mech.getEngine().getWeightFreeEngineHeatSinks());
        spnCount.addChangeListener(this);
        boolean isCompact = cbHSType.getSelectedItem() != null
                && ((Integer)cbHSType.getSelectedItem()) == TYPE_COMPACT;
        int capacity = mech.getEngine().integralHeatSinkCapacity(isCompact);
        spnBaseCount.setEnabled(mech.isOmni());
        spnBaseCount.removeChangeListener(this);
        baseCountModel.setMaximum(capacity);
        baseCountModel.setValue(Math.max(0, mech.getEngine().getBaseChassisHeatSinks(isCompact)));
        spnBaseCount.addChangeListener(this);
        lblFreeCount.setText(String.valueOf(UnitUtil.getCriticalFreeHeatSinks(mech, isCompact)));
    }
    
    public void setFromAero(Aero aero) {
        isAero = true;
        isPrimitive = aero.isPrimitive();
        refresh();
        cbHSType.removeActionListener(this);
        setHeatSinkIndex(aero.getHeatType());
        cbHSType.addActionListener(this);
        spnCount.removeChangeListener(this);
        countModel.setValue(aero.getHeatSinks());
        countModel.setMinimum(aero.getEngine().getWeightFreeEngineHeatSinks());
        spnCount.addChangeListener(this);
        spnBaseCount.removeChangeListener(this);
        baseCountModel.setMaximum(aero.getHeatSinks());
        baseCountModel.setValue(Math.max(0, aero.getHeatSinks() - aero.getPodHeatSinks()));
        spnBaseCount.addChangeListener(this);
        spnBaseCount.setEnabled(aero.isOmni());
        lblFreeText.setVisible(false);
        lblFreeCount.setVisible(false);
    }
    
    public void refresh() {
        Integer prev = (Integer)cbHSType.getSelectedItem();
        cbHSType.removeActionListener(this);
        cbHSType.removeAllItems();
        if (isAero) {
            cbHSType.addItem(TYPE_SINGLE);
            if (techManager.isLegal(heatSinks.get(TYPE_DOUBLE_IS))
                    || techManager.isLegal(heatSinks.get(TYPE_DOUBLE_CLAN))) {
                cbHSType.addItem(TYPE_DOUBLE_AERO);
            }
        } else if (isPrimitive) {
            cbHSType.addItem(TYPE_SINGLE);
        } else {
            for (int i = 0; i < heatSinks.size(); i++) {
                if (techManager.isLegal(heatSinks.get(i))) {
                    cbHSType.addItem(i);
                }
            }
        }
        cbHSType.setSelectedItem(prev);
        cbHSType.addActionListener(this);
        if (cbHSType.getSelectedIndex() < 0) {
            cbHSType.setSelectedIndex(0);
        }
    }
    
    public int getHeatSinkIndex() {
        return (Integer)cbHSType.getSelectedItem();
    }
    
    public void setHeatSinkIndex(int index) {
        cbHSType.setSelectedItem(index);
    }
    
    public EquipmentType getHeatSinkType() {
        return heatSinks.get(getHeatSinkIndex());
    }
    
    public void setHeatSinkType(EquipmentType hs) {
        int index = heatSinks.indexOf(hs);
        cbHSType.setSelectedItem(index);
    }
    
    public int getCount() {
        return countModel.getNumber().intValue();
    }
    
    public int getBaseCount() {
        return baseCountModel.getNumber().intValue();
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == cbHSType) {
            reportChange();
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == spnCount) {
            reportChange();
        } else if (e.getSource() == spnBaseCount) {
            listeners.forEach(l -> l.heatSinkBaseCountChanged(getBaseCount()));
            lblFreeCount.setText(String.valueOf(getBaseCount()));
        }
    }
    
    private void reportChange() {
        if (isAero) {
            listeners.forEach(l -> l.heatSinksChanged(getHeatSinkIndex(), getCount()));
        } else {
            listeners.forEach(l -> l.heatSinksChanged(getHeatSinkType(), getCount()));
        }
    }
    
}
