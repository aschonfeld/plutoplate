package plutoplate.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.UIManager;

import plutoplate.controller.PlutoplateController;
import plutoplate.model.PlutoplateModel;
import plutoplate.model.PlutoplatePreset;

import com.phidget22.PhidgetException;

public class PlutoplateView {
	class ImagePanel extends JPanel {
		private static final long serialVersionUID = -4393165658106731611L;
		private Image image;

		public ImagePanel(Image image) {
			this.image = image;
		}

		public void paintComponent(Graphics g) {
			setPreferredSize(new Dimension(this.image.getWidth(null), this.image.getHeight(null)));
			g.drawImage(this.image, 0, 0, this);
			setOpaque(false);
			setLayout(new BorderLayout());
			super.paintComponent(g);
		}
	}

	private static String FRAME_LABEL = "Pluto Plate Reverb\u2122";
	private static String HEADER = "<html><font color=\"white\"><B>Pluto Plate Reverb</B>&#8482;</font></html>";
	private PlutoplateModel model;
	private PlutoplateController controller;
	private JFrame viewframe;
	private JLabel noPhidgets = new JLabel(
			"<html><font color=\"white\">No Motors Connected. Please connect to start.</font></html>", 0);
	private JMenu presetMenu;
	private JMenu helpMenu;
	private JMenuItem noPresets = new JMenuItem("No Presets");
	private JSlider motorPosition;
	private JLabel motorStatus;
	private JButton resetPosition;
	private JButton saveAsPreset;
	private JTextField targetPosition;
	private JTextField actualPosition;
	static final int FPS_MIN = 0;
	static final int FPS_MAX = 100;
	static final int FPS_INIT = 0;

	public PlutoplateView(PlutoplateModel model, PlutoplateController controller) {
		this.model = model;
		this.controller = controller;
	}
	
	public String formatDialogMessage(String msg) {
		return "<html><body><p style='width: 200px;'>" + msg + "</p></body></html>";
	}

	public void createView() {
		this.viewframe = new JFrame(FRAME_LABEL);
		this.viewframe.setResizable(false);
		this.viewframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.viewframe.addWindowListener(new WindowListener() {
			public void windowActivated(WindowEvent e) {
			}

			public void windowClosed(WindowEvent e) {
			}

			public void windowClosing(WindowEvent e) {
				try {
					PlutoplateView.this.controller.close();
					Window win = e.getWindow();
					win.setVisible(false);
					win.dispose();
					System.exit(0);
				} catch (PhidgetException ex) {
					JOptionPane.showMessageDialog(PlutoplateView.this.viewframe,
							formatDialogMessage(ex.getDescription()),
							"Error Closing Phidget: " + ex.getErrorCode(), 0);
				}
			}

			public void windowDeactivated(WindowEvent e) {
			}

			public void windowDeiconified(WindowEvent e) {
			}

			public void windowIconified(WindowEvent e) {
			}

			public void windowOpened(WindowEvent e) {
				try {
					PlutoplateView.this.controller.init();
				} catch (PhidgetException ex) {
					JOptionPane.showMessageDialog(
						PlutoplateView.this.viewframe,
						formatDialogMessage(ex.getDescription()),
						"Error Initializing Phidget: " + ex.getErrorCode(),
						0
					);
				}
			}
		});
		Image backgroundImage = this.model.getImageDB().getImage("background");
		ImagePanel panelBgImg = new ImagePanel(backgroundImage);
		JMenuBar menubar = new JMenuBar();
		this.presetMenu = new JMenu("Presets");
		this.helpMenu = new JMenu("Help");
		@SuppressWarnings("unused")
		MenuScroller scrollablePresets = new MenuScroller(this.presetMenu);
		menubar.add(this.presetMenu);
		menubar.add(this.helpMenu);
		constructMenu(false);
		constructHelp();

		Dimension imgSize = new Dimension(backgroundImage.getWidth(null), backgroundImage.getHeight(null) + 40);

		this.viewframe.setPreferredSize(imgSize);
		this.viewframe.setLayout(new BorderLayout());
		this.viewframe.add(menubar, "North");
		this.viewframe.add(panelBgImg, "Center");

		BorderLayout borderLayout1 = new BorderLayout();
		borderLayout1.setHgap(1);
		borderLayout1.setVgap(1);
		JPanel panel = new JPanel(borderLayout1);
		panel.setOpaque(false);
		panel.setPreferredSize(
				new Dimension(backgroundImage.getWidth(null) - 10, backgroundImage.getHeight(null) - 10));

		JLabel header = new JLabel(HEADER, 0);
		header.setFont(new Font("Marlett", 0, 20));
		BorderLayout borderLayout2 = new BorderLayout();
		borderLayout2.setHgap(1);
		borderLayout2.setVgap(3);
		JPanel subPanel = new JPanel(borderLayout2);
		subPanel.setOpaque(false);
		subPanel.add(header, "North");

		this.motorPosition = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
		this.motorPosition.setOpaque(false);
		this.motorPosition.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent arg0) {
			}

			public void mouseEntered(MouseEvent arg0) {
			}

			public void mouseExited(MouseEvent arg0) {
			}

			public void mousePressed(MouseEvent arg0) {
			}

			public void mouseReleased(MouseEvent arg0) {
				if (PlutoplateView.this.motorPosition.isEnabled()) {
					try {
						PlutoplateView.this.controller
								.updatePosition(Integer.valueOf(PlutoplateView.this.motorPosition.getValue()));
						PlutoplateView.this.motorPosition.setEnabled(false);
					} catch (PhidgetException ex) {
						JOptionPane.showMessageDialog(
								PlutoplateView.this.viewframe, formatDialogMessage(ex.getDescription()),
								"Error Altering Motor Position: " + ex.getErrorCode(), 0);
					}
				}
			}
		});
		this.motorPosition.setUI(new CustomSliderUI(this.motorPosition));
		JPanel sliderPanel = new JPanel(new GridLayout(4, 1));
		sliderPanel.setOpaque(false);
		this.motorStatus = new JLabel();
		this.motorStatus.setIcon(this.model.getImageDB().getIcon("motorStopped"));
		this.resetPosition = new JButton("Initialize");
		this.resetPosition.addActionListener(e ->  {
			try {
				PlutoplateView.this.controller.resetPosition();
			} catch (PhidgetException ex) {
				JOptionPane.showMessageDialog(
						PlutoplateView.this.viewframe,
						formatDialogMessage(ex.getDescription()),
						"Error Resetting Position: " + ex.getErrorCode(), 0);
			}
		});
		JPanel resetPanel = new JPanel();
		resetPanel.setOpaque(false);
		resetPanel.add(this.motorStatus);
		resetPanel.add(this.resetPosition);

		JPanel sliderHeader = new JPanel(new BorderLayout());
		sliderHeader.setOpaque(false);
		sliderHeader.add(new JLabel("<html><font color=\"white\">Position</font></html>"), "West");
		sliderHeader.add(resetPanel, "East");

		this.targetPosition = new JTextField(6);
		this.targetPosition.setEditable(false);
		this.actualPosition = new JTextField(6);
		this.actualPosition.setEditable(false);

		this.saveAsPreset = new JButton("Save As Preset");
		this.saveAsPreset.addActionListener(e -> {
			try {
				Boolean closePane = false;
				String name = JOptionPane.showInputDialog(PlutoplateView.this.viewframe, "Preset Name:");
				while (!closePane.booleanValue()) {
					if (name != null) {
						if (name.length() > 0) {
							if (PlutoplateView.this.model.getPresetDB().getPreset(name) == null) {
								Integer position = 0;
								if ((PlutoplateView.this.actualPosition.getText() != null)
										&& (PlutoplateView.this.actualPosition.getText().length() != 0)) {
									position = Integer.parseInt(PlutoplateView.this.actualPosition.getText());
								}
								PlutoplateView.this.controller.savePreset(new PlutoplatePreset(name, position));
								PlutoplateView.this.constructMenu(true);
								closePane = true;
							} else {
								name = JOptionPane.showInputDialog(PlutoplateView.this.viewframe,
										"Preset Name '" + name + "' Is Already Taken.\nPlease Enter Another:");
							}
						} else {
							name = JOptionPane.showInputDialog(PlutoplateView.this.viewframe,
									"Preset Name(Required):");
						}
					} else {
						closePane = true;
					}
				}
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(
						PlutoplateView.this.viewframe, formatDialogMessage(ex.toString()),
						"Error Saving Preset: " + ex.toString(), 0);
			}
		});
		JPanel sliderFooter = new JPanel();
		sliderFooter.setOpaque(false);
		GroupLayout layout = new GroupLayout(sliderFooter);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(new JLabel("<html><font color=\"white\">Target:</font></html>"))
						.addComponent(this.targetPosition))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(new JLabel("<html><font color=\"white\">Current:</font></html>"))
						.addComponent(this.actualPosition))
				.addComponent(this.saveAsPreset));
		sliderPanel.add(sliderHeader);
		sliderPanel.add(this.motorPosition);
		sliderPanel.add(sliderFooter);
		this.noPhidgets.setVisible(true);
		sliderPanel.add(this.noPhidgets);
		subPanel.add(sliderPanel, "South");
		panel.add(subPanel, "Center");
		panelBgImg.add(panel, "North");

		this.viewframe.pack();
		this.viewframe.setVisible(true);
		disableControls();
	}

	public JSlider getMotorPosition() {
		return this.motorPosition;
	}

	public JTextField getTargetPosition() {
		return this.targetPosition;
	}

	public JTextField getActualPosition() {
		return this.actualPosition;
	}

	public JLabel getNoPhidgets() {
		return this.noPhidgets;
	}

	public void motorStopped() {
		this.motorStatus.setIcon(this.model.getImageDB().getIcon("motorStopped"));
	}

	public void motorStarted() {
		this.motorStatus.setIcon(this.model.getImageDB().getIcon("motorMoving"));
	}

	public void enableControls() {
		enableStepperControls();
		constructMenu(true);
	}

	private void enableStepperControls() {
		this.noPhidgets.setVisible(false);
		enableMotorControls();
	}

	private void enableMotorControls() {
		this.motorPosition.setEnabled(true);
		this.resetPosition.setEnabled(true);
		this.saveAsPreset.setEnabled(true);
		for (int i = 0; i < this.presetMenu.getItemCount(); i++) {
			JMenuItem menuItem = this.presetMenu.getItem(i);
			if (!this.noPresets.getText().equals(menuItem.getText())) {
				JMenu submenu = (JMenu) menuItem.getComponent();
				submenu.getItem(0).setEnabled(true);
			}
		}
	}

	public void disableControls() {
		disableMotorControls();
		constructMenu(false);
	}

	private void disableMotorControls() {
		this.motorPosition.setEnabled(false);
		this.resetPosition.setEnabled(false);
		this.saveAsPreset.setEnabled(false);
		for (int i = 0; i < this.presetMenu.getItemCount(); i++) {
			JMenuItem menuItem = this.presetMenu.getItem(i);
			if (!this.noPresets.getText().equals(menuItem.getText())) {
				JMenu submenu = (JMenu) menuItem.getComponent();
				submenu.getItem(0).setEnabled(false);
			}
		}
	}

	private void constructHelp() {
		JMenuItem helpSubMenu = new JMenuItem("Help Information");
		helpSubMenu.addActionListener(e -> JOptionPane.showMessageDialog(
				PlutoplateView.this.viewframe, "Coming Soon", "Help Information", 1
		));
		JMenuItem infoSubMenu = new JMenuItem("About " + FRAME_LABEL);
		infoSubMenu.addActionListener(e -> JOptionPane.showMessageDialog(
				PlutoplateView.this.viewframe, "Version 1.1.0 - 2014", "About" + PlutoplateView.FRAME_LABEL, 1
		));
		this.helpMenu.add(helpSubMenu);
		this.helpMenu.add(infoSubMenu);
	}

	private void constructMenu(Boolean phidgetConnected) {
		this.presetMenu.removeAll();
		Boolean noPresetExists = this.model.getPresetDB().getPresets().isEmpty();
		if (noPresetExists.booleanValue()) {
			this.presetMenu.add(this.noPresets);
		} else {
			for (PlutoplatePreset preset : this.model.getPresetDB().getPresets()) {
				JMenu submenu = new JMenu(preset.getName());
				JMenuItem menuItem = new JMenuItem("Open");
				menuItem.setIcon(UIManager.getIcon("FileView.fileIcon"));
				menuItem.addActionListener(e -> {
					String presetName = "";
					try {
						JMenuItem source = (JMenuItem) e.getSource();
						JPopupMenu parentPopup = (JPopupMenu) source.getParent();
						JMenu parent = (JMenu) parentPopup.getInvoker();
						if (parent.isEnabled()) {
							presetName = parent.getText();
							PlutoplateView.this.controller.openPreset(presetName);
						}
					} catch (PhidgetException ex) {
						JOptionPane.showMessageDialog(PlutoplateView.this.viewframe, ex.getDescription(),
								"Error Opening '" + presetName + "' Preset: " + ex.getErrorCode(), 0);
					}
				});
				menuItem.setEnabled(phidgetConnected.booleanValue());
				submenu.add(menuItem);
				menuItem = new JMenuItem("Delete");
				menuItem.setIcon(UIManager.getIcon("FileView.directoryIcon"));
				menuItem.addActionListener(e -> {
					String presetName = "";
					JMenuItem source = (JMenuItem) e.getSource();
					JPopupMenu parentPopup = (JPopupMenu) source.getParent();
					JMenu parent = (JMenu) parentPopup.getInvoker();
					presetName = parent.getText();
					int response = JOptionPane.showConfirmDialog(PlutoplateView.this.viewframe,
							"Are you sure you would like to delete '" + presetName + "'?", "Confirm", 0, 3);
					if (response == 0) {
						PlutoplateView.this.controller.deletePreset(presetName);
						PlutoplateView.this.constructMenu(true);
					}
				});
				submenu.add(menuItem);
				this.presetMenu.add(submenu);
			}
		}
	}
}