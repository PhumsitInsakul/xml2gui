import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.swing.*;
import javax.swing.tree.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import java.awt.*;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Stack;

public class DynamicXMLTreeEditor {
    private static Document xmlDocument;
    private static JTextField valueField;
    private static JTextField fieldNameField;
    private static JComboBox<String> typeComboBox;
    private static DefaultMutableTreeNode selectedNode;
    private static JTree tree;
    private static Stack<String> undoStack = new Stack<>();
    private static Stack<String> redoStack = new Stack<>();

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

        //JButton convertWSDLButton = new JButton("Convert WSDL to XML");

        JButton saveValueButton = new JButton("Save");
        JButton addSubfieldButton = new JButton("Add Subfield");
        JButton deleteFieldButton = new JButton("Delete Field");
        JButton undoButton = new JButton("Undo");
        JButton redoButton = new JButton("Redo");


        // Adding components to the editor panel
        gbc.gridx = 0; gbc.gridy = 0; fieldPanel.add(fieldNameLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 0; fieldPanel.add(fieldNameField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; fieldPanel.add(valueLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 1; fieldPanel.add(valueField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; fieldPanel.add(typeLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 2; fieldPanel.add(typeComboBox, gbc);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; fieldPanel.add(saveValueButton, gbc);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; fieldPanel.add(addSubfieldButton, gbc);
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; fieldPanel.add(deleteFieldButton, gbc);
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2; fieldPanel.add(undoButton, gbc);
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2; fieldPanel.add(redoButton, gbc);

        editorPanel.add(fieldPanel, BorderLayout.NORTH);

        // Buttons for file operations
        JButton loadXMLButton = new JButton("Load XML");
        JButton saveFileButton = new JButton("Save File");
        JButton duplicateFieldButton = new JButton("Duplicate Field");
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2; fieldPanel.add(duplicateFieldButton, gbc);

        //bottomPanel.add(convertWSDLButton);
        bottomPanel.add(loadXMLButton);
        bottomPanel.add(saveFileButton);

        splitPane.setLeftComponent(treeScrollPane);
        splitPane.setRightComponent(editorPanel);

        frame.getContentPane().add(splitPane, BorderLayout.CENTER);
        frame.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
        frame.setVisible(true);

        // Add action listener for the button
//        convertWSDLButton.addActionListener(e -> {
//            JFileChooser fileChooser = new JFileChooser();
//            int result = fileChooser.showOpenDialog(frame);
//            if (result == JFileChooser.APPROVE_OPTION) {
//                File file = fileChooser.getSelectedFile();
//                String wsdlPath = file.getAbsolutePath();
//                String xmlOutput = convertWSDLToXML(wsdlPath);
//
//                if (xmlOutput != null) {
//                    JOptionPane.showMessageDialog(frame, "WSDL converted to XML successfully!");
//                    try {
//                        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
//                        xmlDocument = builder.parse(new InputSource(new StringReader(xmlOutput)));
//
//                        //DefaultMutableTreeNode root = new DefaultMutableTreeNode("XML");
//                        buildTreeFromXML(xmlDocument.getDocumentElement(), root);
//                        ((DefaultTreeModel) tree.getModel()).setRoot(root);
//                        ((DefaultTreeModel) tree.getModel()).reload();
//                    } catch (Exception ex) {
//                        JOptionPane.showMessageDialog(frame, "Error parsing generated XML: " + ex.getMessage());
//                    }
//                } else {
//                    JOptionPane.showMessageDialog(frame, "Failed to convert WSDL to XML.");
//                }
//            }
//        });

        // Tree Selection Listener
        tree.addTreeSelectionListener(e -> {
            selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (selectedNode != null && selectedNode.getUserObject() instanceof Element) {
                Element element = (Element) selectedNode.getUserObject();
                fieldNameField.setText(element.getTagName());
                valueField.setText(element.getTextContent());
                //typeComboBox.setSelectedItem("String"); // Default type
            }
        });

        // Button Actions
        saveValueButton.addActionListener(e -> {
            saveStateToUndoStack();
            saveFieldValue();
        });
        addSubfieldButton.addActionListener(e -> {
            saveStateToUndoStack();
            addSubfield();
        });
        deleteFieldButton.addActionListener(e -> {
            saveStateToUndoStack();
            deleteField();
        });
        undoButton.addActionListener(e -> undo());
        redoButton.addActionListener(e -> redo());
        loadXMLButton.addActionListener(e -> loadXMLFile(frame, root));
        saveFileButton.addActionListener(e -> saveXMLFile(frame));
        duplicateFieldButton.addActionListener(e -> {
            saveStateToUndoStack();
            duplicateNode();
        });

    }

    // Save XML structure to undo stack
    private static void saveStateToUndoStack() {
        try {
            undoStack.push(convertXMLToString());
            //undoStack.push(convertXMLToString(xmlDocument));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Convert XML document to String
     *
     * @return
     * @throws Exception
     */
    private static String convertXMLToString() throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StreamResult result = new StreamResult(new StringWriter());
        transformer.transform(new DOMSource(xmlDocument), result);
        return result.getWriter().toString();
    }
//    private static String convertXMLToString(Document doc) throws Exception {
//        Transformer transformer = TransformerFactory.newInstance().newTransformer();
//        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
//        StreamResult result = new StreamResult(new StringWriter());
//        transformer.transform(new DOMSource(doc), result);
//        return result.getWriter().toString();
//    }


    /**
     * Revert to previous state
     */
    private static void undo() {
        if (!undoStack.isEmpty()) {
            try {
                redoStack.push(convertXMLToString());
                //redoStack.push(convertXMLToString(xmlDocument));

                String previousState = undoStack.pop();
                xmlDocument = convertStringToXML(previousState);
                reloadTree();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Redo the reverted action
     */
    private static void redo() {
        if (!redoStack.isEmpty()) {
            try {
                undoStack.push(convertXMLToString());
                //undoStack.push(convertXMLToString(xmlDocument));

                String nextState = redoStack.pop();
                xmlDocument = convertStringToXML(nextState);
                reloadTree();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // Reload tree after undo/redo
    private static void reloadTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("XML");
        buildTreeFromXML(xmlDocument.getDocumentElement(), root);
        ((DefaultTreeModel) tree.getModel()).setRoot(root);
        ((DefaultTreeModel) tree.getModel()).reload();
    }

    /**
     * Convert String to XML document
     *
     * @param xmlString
     * @return
     * @throws Exception
     */
    private static Document convertStringToXML(String xmlString) throws Exception {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xmlString)));
    }

    /**
     * load file from xml file (input)
     *
     * @param frame
     * @param root
     */
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

    /**
     * build tree structure from xml file
     *
     * @param element
     * @param parent
     */
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

    /**
     * save field value
     */
    private static void saveFieldValue() {
        if (selectedNode != null && selectedNode.getUserObject() instanceof Element) {
            Element element = (Element) selectedNode.getUserObject();
            element.setTextContent(valueField.getText());
            JOptionPane.showMessageDialog(null, "Value saved for: " + element.getTagName());
        }
    }

//    private static void addSubfield() {
//        if (selectedNode != null && selectedNode.getUserObject() instanceof Element) {
//            Element parentElement = (Element) selectedNode.getUserObject();
//            Element newElement = xmlDocument.createElement(fieldNameField.getText());
//            newElement.setTextContent(valueField.getText());
//            parentElement.appendChild(newElement);
//
//            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newElement);
//            selectedNode.add(newNode);
//            ((DefaultTreeModel) tree.getModel()).reload();
//        }
//    }

    /**
     *
     *add sub field
     */
    private static void addSubfield() {
        if (selectedNode != null && selectedNode.getUserObject() instanceof Element) {
            Element parentElement = (Element) selectedNode.getUserObject();
            if (fieldNameField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Field name cannot be empty!");
                return;
            }
            Element newElement = xmlDocument.createElement(fieldNameField.getText());
            newElement.setTextContent(valueField.getText());
            parentElement.appendChild(newElement);

            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newElement);
            selectedNode.add(newNode);
            ((DefaultTreeModel) tree.getModel()).reload();
        }
    }

    /**
     * delete field
     */
    private static void deleteField() {
        if (selectedNode != null && selectedNode.getParent() != null) {
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) selectedNode.getParent();
            parent.remove(selectedNode);
            ((DefaultTreeModel) tree.getModel()).reload();
        }
    }

    /**
     * save xml file (output)
     * @param frame
     */
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

    /**
     * Duplicate Field (Array)
     */
    private static void duplicateNode() {
        if (selectedNode != null && selectedNode.getUserObject() instanceof Element) {
            Element selectedElement = (Element) selectedNode.getUserObject();

            // ตรวจสอบว่าโหนดที่เลือกมีโหนดแม่ (Parent Node)
            Node parentNode = selectedElement.getParentNode();
            if (parentNode == null || !(parentNode instanceof Element)) {
                JOptionPane.showMessageDialog(null, "The selected node cannot be duplicated.");
                return;
            }

            // ตรวจสอบว่ามีคอมเมนต์ <!--Zero or more repetitions:--> หรือไม่
            Node previousSibling = selectedElement.getPreviousSibling();
            boolean hasZeroOrMoreRepetitions = false;
            while (previousSibling != null) {
                if (previousSibling.getNodeType() == Node.COMMENT_NODE &&
                        previousSibling.getNodeValue().contains("Zero or more repetitions:")) {
                    hasZeroOrMoreRepetitions = true;
                    break;
                }
                previousSibling = previousSibling.getPreviousSibling();
            }

            if (!hasZeroOrMoreRepetitions) {
                JOptionPane.showMessageDialog(null, "This node cannot be duplicated because it is not unbounded.");
                return;
            }

            // คัดลอกโหนด
            Element duplicateElement = (Element) selectedElement.cloneNode(true);

            // เพิ่มโหนดใหม่เข้าไปในโหนดแม่
            parentNode.appendChild(duplicateElement);

            // อัปเดต tree view
            DefaultMutableTreeNode parentTreeNode = (DefaultMutableTreeNode) selectedNode.getParent();
            DefaultMutableTreeNode newTreeNode = new DefaultMutableTreeNode(duplicateElement);
            parentTreeNode.add(newTreeNode);
            buildTreeFromXML(duplicateElement, newTreeNode);

            // รีเฟรช Tree View
            ((DefaultTreeModel) tree.getModel()).reload();
        }
    }





    /**
     * Convert WSDL to XML
     *
     * @param wsdlURL The URL or path of the WSDL file
     * @return XML document as a string
     */
//    private static String convertWSDLToXML(String wsdlURL) {
//        try {
//            // Create a DocumentBuilder to parse WSDL
//            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//            DocumentBuilder builder = factory.newDocumentBuilder();
//
//            // Parse the WSDL file
//            Document wsdlDocument = builder.parse(wsdlURL);
//
//            // Extract the relevant elements (e.g., operations, messages, etc.)
//            Element root = wsdlDocument.getDocumentElement();
//
//            // Create a new XML Document for the output
//            Document outputDocument = builder.newDocument();
//            Element outputRoot = outputDocument.createElement("WSDLContent");
//            outputDocument.appendChild(outputRoot);
//
//            // Recursively copy WSDL content into the new XML document
//            copyWSDLToXML(root, outputRoot, outputDocument);
//
//            // Convert the resulting XML document to a String
//            return convertXMLToString(outputDocument);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

    /**
     * Recursively copy WSDL elements into an XML structure
     *
     * @param wsdlNode  The WSDL node
     * @param xmlParent The parent node in the XML document
     * @param xmlDoc    The XML document
     */
//    private static void copyWSDLToXML(Node wsdlNode, Element xmlParent, Document xmlDoc) {
//        NodeList children = wsdlNode.getChildNodes();
//        for (int i = 0; i < children.getLength(); i++) {
//            Node child = children.item(i);
//            if (child.getNodeType() == Node.ELEMENT_NODE) {
//                Element childElement = xmlDoc.createElement(child.getNodeName());
//                if (child.getTextContent() != null && !child.getTextContent().trim().isEmpty()) {
//                    childElement.setTextContent(child.getTextContent().trim());
//                }
//                xmlParent.appendChild(childElement);
//                copyWSDLToXML(child, childElement, xmlDoc); // Recursively process child nodes
//            }
//        }
//    }
}
