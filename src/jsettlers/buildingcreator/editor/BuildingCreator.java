package jsettlers.buildingcreator.editor;

import go.graphics.swing.AreaContainer;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import jsettlers.buildingcreator.editor.jobeditor.PersonJobEditor;
import jsettlers.buildingcreator.editor.map.BuildingtestMap;
import jsettlers.buildingcreator.editor.map.PseudoTile;
import jsettlers.common.Color;
import jsettlers.common.buildings.EBuildingType;
import jsettlers.common.buildings.RelativeBricklayer;
import jsettlers.common.buildings.RelativeStack;
import jsettlers.common.material.EMaterialType;
import jsettlers.common.movable.EDirection;
import jsettlers.common.position.ISPosition2D;
import jsettlers.common.position.RelativePoint;
import jsettlers.common.position.ShortPoint2D;
import jsettlers.graphics.JOGLPanel;
import jsettlers.graphics.action.Action;
import jsettlers.graphics.action.SelectAction;
import jsettlers.graphics.map.IMapInterfaceListener;
import jsettlers.graphics.map.MapInterfaceConnector;
import jsettlers.graphics.map.draw.ImageProvider;

/**
 * This is the main building creator class.
 * 
 * @author michael
 */
public class BuildingCreator implements IMapInterfaceListener {
	private BuildingDefinition definition;
	private JFrame window;
	private final BuildingtestMap map;

	private ToolType tool = ToolType.SET_BLOCKED;
	private JPanel actionList;
	private JLabel positionDisplayer;

	private BuildingCreator() {
		EBuildingType type = askType();

		definition = new BuildingDefinition(type);

		map = new BuildingtestMap(definition);
		for (int x = 0; x < map.getWidth(); x++) {
			for (int y = 0; y < map.getHeight(); y++) {
				reloadColor(new ShortPoint2D(x, y));
			}
		}

		JOGLPanel mapPanel = new JOGLPanel();
		MapInterfaceConnector connector = mapPanel.showHexMap(map, null);
		connector.addListener(this);

		JPanel menu = new JPanel();
		menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
		menu.setPreferredSize(new Dimension(200, 100));
		actionList = new JPanel();
		actionList.setLayout(new BoxLayout(actionList, BoxLayout.Y_AXIS));
		menu.add(new JScrollPane(actionList));

		menu.add(createToolChangeBar());

		JButton addButton = new JButton("add new action");
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addNewAction();
			}
		});
		menu.add(addButton);

		JButton xmlButton = new JButton("show xml data");
		xmlButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showXML();
			}
		});
		menu.add(xmlButton);
		positionDisplayer = new JLabel();
		menu.add(positionDisplayer);

		JPanel root = new JPanel();
		root.setLayout(new BoxLayout(root, BoxLayout.X_AXIS));
		root.add(new AreaContainer(mapPanel.getArea()));
		root.add(menu);

		window = new JFrame("Edit " + type.toString());
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.add(root);
		window.pack();
		window.setSize(600, 500);
		window.setVisible(true);
	}

	private JButton createToolChangeBar() {
		JButton button = new JButton("Select tool...");
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				tool =
				        (ToolType) JOptionPane.showInputDialog(null,
				                "Select building type", "Building Type",
				                JOptionPane.QUESTION_MESSAGE, null,
				                ToolType.values(), tool);
			}
		});
		return button;
	}

	private void reloadActionList() {
		actionList.removeAll();
		for (String name : definition.getActionNames()) {
			PersonJobEditor panel =
			        new PersonJobEditor(name, definition.getActionByName(name));
			actionList.add(panel);
		}
		actionList.revalidate();
	}

	private void addNewAction() {
		String acitonName =
		        JOptionPane.showInputDialog(window, "Select job name");
		if (acitonName != null) {
			definition.addAction(acitonName);
			reloadActionList();
		}
	}

	private EBuildingType askType() {
		return (EBuildingType) JOptionPane.showInputDialog(null,
		        "Select building type", "Building Type",
		        JOptionPane.QUESTION_MESSAGE, null, EBuildingType.values(),
		        null);
	}

	public static void main(String[] args) {
		ImageProvider provider = ImageProvider.getInstance();
		provider.addLookupPath(new File(
		        "/home/michael/.wine/drive_c/BlueByte/S3AmazonenDemo/GFX"));
		provider.addLookupPath(new File("D:/Games/Siedler3/GFX"));

		new BuildingCreator();
	}

	@Override
	public void action(Action action) {
		if (action instanceof SelectAction) {
			SelectAction sAction = (SelectAction) action;
			ISPosition2D pos = sAction.getPosition();
			RelativePoint relative = absoluteToRelative(pos);

			positionDisplayer.setText("x = "
			        + (pos.getX() - BuildingtestMap.OFFSET) + ", y = "
			        + (pos.getY() - BuildingtestMap.OFFSET));

			if (tool == ToolType.SET_BLOCKED) {
				toogleUsedTile(relative);
			} else if (tool == ToolType.SET_DOOOR) {
				setDoor(relative);
			} else if (tool == ToolType.ADD_STACK) {
				addStack(relative);
			} else if (tool == ToolType.REMOVE_STACK) {
				removeStack(relative);
			} else if (tool == ToolType.SET_FLAG) {
				setFlag(relative);
			} else if (tool == ToolType.SET_BUILDMARK) {
				definition.toggleBuildmarkStatus(relative);
			} else if (tool == ToolType.BRICKLAYER_NE) {
				definition.toggleBrickayer(relative, EDirection.NORTH_EAST);
			} else if (tool == ToolType.BRICKLAYER_NW) {
				definition.toggleBrickayer(relative, EDirection.NORTH_WEST);
			}

			reloadColor(pos);
		}
	}

	private void removeStack(RelativePoint relative) {
		definition.removeStack(relative);
	}

	private void addStack(RelativePoint relative) {
		EMaterialType material =
		        (EMaterialType) JOptionPane.showInputDialog(null,
		                "Select building type", "Building Type",
		                JOptionPane.QUESTION_MESSAGE, null,
		                EMaterialType.values(), tool);
		Integer buildrequired =
		        (Integer) JOptionPane.showInputDialog(null,
		                "Select building type", "Building Type",
		                JOptionPane.QUESTION_MESSAGE, null, new Integer[] {
		                        0, 1, 2, 3, 4, 5, 6, 7, 8
		                }, tool);
		if (material != null && buildrequired != null) {
			definition.setStack(relative, material, buildrequired.intValue());
		}
	}

	private void setDoor(RelativePoint tile) {
		RelativePoint oldDoor = definition.getDoor();
		ISPosition2D oldPos = relativeToAbsolute(oldDoor);
		reloadColor(oldPos);

		definition.setDoor(tile);
	}

	private void setFlag(RelativePoint tile) {
		RelativePoint oldFlag = definition.getFlag();
		ISPosition2D oldPos = relativeToAbsolute(oldFlag);
		reloadColor(oldPos);

		definition.setFlag(tile);
	}

	private ISPosition2D relativeToAbsolute(RelativePoint oldDoor) {
		ISPosition2D oldPos =
		        new ShortPoint2D(oldDoor.getDx() + BuildingtestMap.OFFSET,
		                oldDoor.getDy() + BuildingtestMap.OFFSET);
		return oldPos;
	}

	private RelativePoint absoluteToRelative(ISPosition2D pos) {
		RelativePoint tile =
		        new RelativePoint(pos.getX() - BuildingtestMap.OFFSET,
		                pos.getY() - BuildingtestMap.OFFSET);
		return tile;
	}

	private void toogleUsedTile(RelativePoint relative) {
		if (definition.getBlockedStatus(relative)) {
			definition.setBlockedStatus(relative, false, false);
		} else if (definition.getProtectedStatus(relative)) {
			definition.setBlockedStatus(relative, true, true);
		} else {
			definition.setBlockedStatus(relative, true, false);
		}
	}

	private void reloadColor(ISPosition2D pos) {
		PseudoTile tile = (PseudoTile) map.getTile(pos);
		ArrayList<Color> colors = new ArrayList<Color>();

		RelativePoint relative = absoluteToRelative(pos);
		if (definition.getBlockedStatus(relative)) {
			colors.add(new Color(.5f, 0, 0, 1));
		} else if (definition.getProtectedStatus(relative)) {
			colors.add(new Color(0xff0000));
		}

		if (definition.getBuildmarkStatus(relative)) {
			colors.add(new Color(0xd1b26f));
		}

		if (definition.getDoor().equals(relative)) {
			colors.add(new Color(0x029386));
		}

		if (definition.getFlag().equals(relative)) {
			colors.add(new Color(0x75bbfd));
		}

		if (definition.getStack(relative) != null) {
			colors.add(new Color(0x96f97b));
			tile.setStack(new MapStack(definition.getStack(relative)));
		} else {
			tile.setStack(null);
		}
		
		if (definition.getBricklayerStatus(relative)) {
			colors.add(new Color(0xffff14));
		}

		if (!colors.isEmpty()) {
			Color color = mixColors(colors);
			tile.setDebugColor(color);
		} else {
			tile.setDebugColor(null);
		}
	}

	private Color mixColors(ArrayList<Color> colors) {
		int bluesum = 0;
		int redsum = 0;
		int greensum = 0;
		for (Color color : colors) {
			bluesum += color.getBlue();
			redsum += color.getRed();
			greensum += color.getGreen();
		}
		Color color =
		        new Color(redsum / colors.size(), greensum / colors.size(),
		                bluesum / colors.size(), 255);
		return color;
	}

	private void showXML() {
		StringBuilder builder = new StringBuilder("");
		for (RelativePoint tile : definition.getBlocked()) {
			builder.append("\t<blocked dx=\"");
			builder.append(tile.getDx());
			builder.append("\" dy=\"");
			builder.append(tile.getDy());
			builder.append("\" block=\"true\" />\n");
		}
		for (RelativePoint tile : definition.getJustProtected()) {
			builder.append("\t<blocked dx=\"");
			builder.append(tile.getDx());
			builder.append("\" dy=\"");
			builder.append(tile.getDy());
			builder.append("\" block=\"false\" />\n");
		}

		RelativePoint door = definition.getDoor();
		builder.append("\t<door dx=\"");
		builder.append(door.getDx());
		builder.append("\" dy=\"");
		builder.append(door.getDy());
		builder.append("\" />\n");

		for (RelativeStack stack : definition.getStacks()) {
			builder.append("\t<stack dx=\"");
			builder.append(stack.getDx());
			builder.append("\" dy=\"");
			builder.append(stack.getDy());
			builder.append("\" material=\"");
			builder.append(stack.getType().name());
			builder.append("\" buildrequired=\"");
			builder.append(stack.requiredForBuild());
			builder.append("\" />\n");
		}
		for (RelativeBricklayer bricklayer : definition.getBricklayers()) {
			builder.append("\t<bricklayer dx=\"");
			builder.append(bricklayer.getPosition().getDx());
			builder.append("\" dy=\"");
			builder.append(bricklayer.getPosition().getDy());
			builder.append("\" direction=\"");
			builder.append(bricklayer.getDirection());
			builder.append("\" />\n");
		}

		RelativePoint flag = definition.getFlag();
		builder.append("\t<flag dx=\"");
		builder.append(flag.getDx());
		builder.append("\" dy=\"");
		builder.append(flag.getDy());
		builder.append("\" />\n");

		for (RelativePoint mark : definition.getBuildmarks()) {
			builder.append("\t<buildmark dx=\"");
			builder.append(mark.getDx());
			builder.append("\" dy=\"");
			builder.append(mark.getDy());
			builder.append("\" />\n");
		}

		JDialog dialog = new JDialog(window, "xml");
		dialog.add(new JScrollPane(new JTextArea(builder.toString())));
		dialog.pack();
		dialog.setVisible(true);
	}
}
