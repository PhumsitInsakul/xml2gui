import org.w3c.dom.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import java.awt.*;
import java.io.File;

public class DynamicXMLTreeEditor {
    private static Document xmlDocument;
    private static JTextField valueField;
    private static JTextField fieldNameField;
    private static JComboBox<String> typeComboBox;
    private static DefaultMutableTreeNode selectedNode;
    private static JTree tree;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(DynamicXMLTreeEditor::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Dynamic XML Tree Editor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);

        // Layout
        JSplitPane splitPane = new JSplitPane();
        JPanel editorPanel = new JPanel(new BorderLayout());
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JPanel fieldPanel = new JPanel(new GridBagLayout());

        // Tree and Scroll Pane
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("XML");
        tree = new JTree(root);
        JScrollPane treeScrollPane = new JScrollPane(tree);

        // Editor Section
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel fieldNameLabel = new JLabel("Field Name:");
        JLabel valueLabel = new JLabel("Value:");
        JLabel typeLabel = new JLabel("Type:");

        fieldNameField = new JTextField(20);
        valueField = new JTextField(20);
        typeComboBox = new JComboBox<>(new String[]{"String", "Integer", "Boolean"});

        JButton saveValueButton = new JButton("Save");
        JButton addSubfieldButton = new JButton("Add Subfield");

        // Adding components to the editor panel
        gbc.gridx = 0; gbc.gridy = 0; fieldPanel.add(fieldNameLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 0; fieldPanel.add(fieldNameField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; fieldPanel.add(valueLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 1; fieldPanel.add(valueField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; fieldPanel.add(typeLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 2; fieldPanel.add(typeComboBox, gbc);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; fieldPanel.add(saveValueButton, gbc);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; fieldPanel.add(addSubfieldButton, gbc);

        editorPanel.add(fieldPanel, BorderLayout.NORTH);

        // Buttons for file operations
        JButton loadXMLButton = new JButton("Load XML");
        JButton saveFileButton = new JButton("Save File");

        bottomPanel.add(loadXMLButton);
        bottomPanel.add(saveFileButton);

        splitPane.setLeftComponent(treeScrollPane);
        splitPane.setRightComponent(editorPanel);

        frame.getContentPane().add(splitPane, BorderLayout.CENTER);
        frame.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
        frame.setVisible(true);

        // Tree Selection Listener
        tree.addTreeSelectionListener(e -> {
            selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (selectedNode != null && selectedNode.getUserObject() instanceof Element) {
                Element element = (Element) selectedNode.getUserObject();
                fieldNameField.setText(element.getTagName());
                valueField.setText(element.getTextContent());
                typeComboBox.setSelectedItem("String"); // Default type
            }
        });

        // Button Actions
        saveValueButton.addActionListener(e -> saveFieldValue());
        addSubfieldButton.addActionListener(e -> addSubfield());
        loadXMLButton.addActionListener(e -> loadXMLFile(frame, root));
        saveFileButton.addActionListener(e -> saveXMLFile(frame));
    }

    private static void loadXMLFile(JFrame frame, DefaultMutableTreeNode root) {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                xmlDocument = builder.parse(file);
                xmlDocument.getDocumentElement().normalize();

                root.removeAllChildren();
                buildTreeFromXML(xmlDocument.getDocumentElement(), root);
                ((DefaultTreeModel) tree.getModel()).reload();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Failed to load XML file: " + ex.getMessage());
            }
        }
    }

    private static void buildTreeFromXML(Element element, DefaultMutableTreeNode parent) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(element);
        parent.add(node);

        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                buildTreeFromXML((Element) child, node);
            }
        }
    }

    private static void saveFieldValue() {
        if (selectedNode != null && selectedNode.getUserObject() instanceof Element) {
            Element element = (Element) selectedNode.getUserObject();
            element.setTextContent(valueField.getText());
            JOptionPane.showMessageDialog(null, "Value saved for: " + element.getTagName());
        }
    }

    private static void addSubfield() {
        if (selectedNode != null && selectedNode.getUserObject() instanceof Element) {
            Element parentElement = (Element) selectedNode.getUserObject();
            Element newElement = xmlDocument.createElement(fieldNameField.getText());
            newElement.setTextContent(valueField.getText());
            parentElement.appendChild(newElement);

            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newElement);
            selectedNode.add(newNode);
            ((DefaultTreeModel) tree.getModel()).reload();
        }
    }

    private static void saveXMLFile(JFrame frame) {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                DOMSource source = new DOMSource(xmlDocument);
                StreamResult streamResult = new StreamResult(file);
                transformer.transform(source, streamResult);
                JOptionPane.showMessageDialog(frame, "File saved successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Failed to save file: " + ex.getMessage());
            }
        }
    }
}
