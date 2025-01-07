/**
 * Copyright (c) 2025 Phumsith Insakul. All rights reserved.
 *
 * This code is part of the Dynamic XML Tree Editor application.
 *
 * Author: Phumsith Insakul
 * Email: phumparfait@gmail.com
 *
 * Description:
 * This program allows users to interact with and edit XML structures dynamically.
 * Features include saving, undoing, and redoing changes, as well as adding, deleting, and duplicating fields.
 *
 * License: MIT License
 */


/**
 * buildTreeFromXML ส่งผลให้มี field ซ้อนกัน แต่ไม่ส่งผลกระทบต่อไฟล์หลัง Save (DuplicateNode)
 * ReadyAPI รู้ได้ยังไงว่า field นี้เป็นข้อมูลชนิดอะไร -> (Generate Project ผ่าน WSDL แล้วนำไป map กับชื่อ field ใน outline -> รู้ type ของ field นั้นๆ)
 * xml ไม่สามารถเก็บ/ระบุ/กำหนด Data type ได้ แต่กำหนดจาก xsd ที่ wsdl ใช้ดึงมา (12 schema)
 *
 *
 * การทำงานของ duplicateNode แต่เดิมจะใช้วิธีหาคำ "Zero or more repetitions:" จากในไฟล์ xml ที่ generate ผ่าน soapUI (ถ้ามี Unbound ที่ field ไหน จะส่งผลให้ไฟล์ xml แจ้ง "Zero or more repetitions:" เหนือ field นั้นๆ)
 * วิธีการใหม่คือสามารถเข้าไปเช็คในไฟล์เดิมนั้นว่ามี field ไหนที่เข้ากรณีนี้บ้าง (1023 กรณี) จากนั้นแสดงผลทั้งหมดนั้นบน Terminal (ตัด field ซ้ำเรียบร้อย) จากนั้นนำข้อมูลนั้นไปใส่ใน whitelist (manual เท่านั้น)
 *
 * "Zero or more repetitions:" เปลี่ยนเป็น Whitelist โดยข้อมูลใน Whitelist กำหนดจากการเรียกใช้ "collectFieldsWithZeroOrMoreRepetitions" ใน "listDuplicateAllowedFields" แล้วแสดงข้อมูลทั้งหมดลง Terminal
 * จากนั้นสามารถ Copy Arraylist ทั้งหมดนั้นมาใส่ใน duplicateAllowedFields ได้ทันที (Action ตรงนี้ส่งผลให้การ duplicateNode ไม่ต้องหาคำ "Zero or more repetitions" ซ้ำซ้อนในกรณีที่ไฟล์ xml ฉบับจริงไม่มีคำเหล่านั้น
 * เพราะ generate มาจาก excel (excel -> xml) ไม่ใช่ soapUI (wsdl -> xml))
 */

/**
 * รอ OA เพื่อ download; Launch4j มาทำ .jar เป็น .exe
 */

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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class DynamicXMLTreeEditor {
    private static Document xmlDocument;
    private static JTextField valueField;
    private static JTextField fieldNameField;
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

        fieldNameField = new JTextField(20);
        valueField = new JTextField(20);
        Font thaiFont = new Font("Tahoma", Font.PLAIN, 12);
        fieldNameField.setFont(thaiFont);
        valueField.setFont(thaiFont);

        JButton saveValueButton = new JButton("Save");
        JButton addSubfieldButton = new JButton("Add Subfield");
        JButton deleteFieldButton = new JButton("Delete Field");
        JButton undoButton = new JButton("Undo");
        JButton redoButton = new JButton("Redo");
        JButton duplicateFieldButton = new JButton("Duplicate Field");

        // Adding components to the editor panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        fieldPanel.add(fieldNameLabel, gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        fieldPanel.add(fieldNameField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        fieldPanel.add(valueLabel, gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        fieldPanel.add(valueField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        fieldPanel.add(saveValueButton, gbc);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        fieldPanel.add(addSubfieldButton, gbc);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        fieldPanel.add(deleteFieldButton, gbc);
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        fieldPanel.add(undoButton, gbc);
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        fieldPanel.add(redoButton, gbc);
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        fieldPanel.add(duplicateFieldButton, gbc);

        editorPanel.add(fieldPanel, BorderLayout.NORTH);

        // Buttons for file operations
        JButton loadXMLButton = new JButton("Load XML");
        JButton saveFileButton = new JButton("Save File");

        //bottomPanel.add(convertWSDLButton);
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
        duplicateFieldButton.addActionListener(e -> {
            saveStateToUndoStack();
            duplicateNode();
        });

        loadXMLButton.addActionListener(e -> {
            loadXMLFile(frame);
            listDuplicateAllowedFields();
        });
        saveFileButton.addActionListener(e -> saveXMLFile(frame));

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

    /**
     * Revert to previous state
     */
    private static void undo() {
        if (!undoStack.isEmpty()) {
            try {
                redoStack.push(convertXMLToString());

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
     * reset state กรณี loadXMLFile รอบสอง
     */
    private static void resetState() {
        // ล้าง Undo/Redo Stack
        undoStack.clear();
        redoStack.clear();

        // ล้าง Tree
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("XML");
        ((DefaultTreeModel) tree.getModel()).setRoot(root);

        // ล้างข้อมูลใน Field
        fieldNameField.setText("");
        valueField.setText("");
    }

    /**
     * load file from xml file (input)
     *
     * @param frame
     */
    private static void loadXMLFile(JFrame frame) {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();

                // Reset ก่อนโหลดใหม่
                resetState();

                xmlDocument = builder.parse(file);
                xmlDocument.getDocumentElement().normalize();

                DefaultMutableTreeNode root = new DefaultMutableTreeNode("XML");
                buildTreeFromXML(xmlDocument.getDocumentElement(), root);
                ((DefaultTreeModel) tree.getModel()).setRoot(root);
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

            // สร้าง Subfield ใหม่ใน XML Document
            Element newElement = xmlDocument.createElement(fieldNameField.getText());
            newElement.setTextContent(valueField.getText());
            parentElement.appendChild(newElement);

            // เพิ่ม Subfield ใหม่ใน Tree View
            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newElement);
            ((DefaultTreeModel) tree.getModel()).insertNodeInto(newNode, selectedNode, selectedNode.getChildCount());

            // อัปเดต Tree View
            updateTreeView(newNode);
        }
    }

    /**
     * delete field
     */
    private static void deleteField() {
        if (selectedNode != null && selectedNode.getParent() != null) {
            // ลบโหนดใน XML Document
            Element selectedElement = (Element) selectedNode.getUserObject();
            Node parentElement = selectedElement.getParentNode();
            if (parentElement != null) {
                parentElement.removeChild(selectedElement);
            }

            // ลบโหนดใน Tree View
            DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) selectedNode.getParent();
            ((DefaultTreeModel) tree.getModel()).removeNodeFromParent(selectedNode);

            // อัปเดต Tree View
            updateTreeView(parentNode);
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

    private static final Set<String> duplicateAllowedFields = new HashSet<>(Arrays.asList(
            "LetterOfGuaranteeAssetDetail",
            "NotGenerate",
            "ExistingGuaranteeCollateralDetails",
            "needPaymentForAccruedInterestAmount",
            "isCustWillReqForDrawdownAfterCreditLineDecrease",
            "AuthorizedPersonToSignContract",
            "Exe1VerifiedCorrect",
            "LeaseholdAssetDetail",
            "DocumentProperty",
            "SecurityDetail",
            "StepRate",
            "JointVentureOrConsortiumProfiles",
            "PersonalOrJuristicProfiles",
            "FeePaymentMethod",
            "TFCCustomerNum",
            "DGENSupport",
            "MortgageRank",
            "LegalActivityDetail",
            "feeContractReference",
            "CreditLineAccountInfo",
            "StockAssetDetail",
            "ShipAssetDetail",
            "OwnerAuthPersonToPerformLegalActDetail",
            "CodeNameMappings",
            "LeaseholdAssetSubDetails",
            "CarAssetDetail",
            "ExistingGuaranteeContractDetails",
            "BillExchangeAssetDetail",
            "ExistingLegalActType",
            "CustomerNameAndGUID",
            "TemplateRefValue",
            "InstallmentEveryMonth",
            "CommoditiesAssetDetail",
            "Locations",
            "PackageFinanceDetails",
            "AddExternalError",
            "Sequence",
            "TemplateKeyGUID",
            "TemplateDetail",
            "GoldAssetDetail",
            "BillingScheduleTypes",
            "ALSCustomerNum",
            "CondominiumAssetDetail",
            "TemplateSubLevel",
            "TransferTo",
            "BillInfos",
            "isSME",
            "needPaymentForAccruedInterest",
            "Covenants",
            "RightOnBenefitAssetDetail",
            "CommercialCollateralContractDetail",
            "authorizedPersonToPerformLegalAct",
            "AuthorizedPerson",
            "ISupplyInfo",
            "PNDuePayments",
            "CollateralDetail",
            "CustIdentification",
            "LeasingAssetDetail",
            "OtherFeeInfo",
            "PaymentStep",
            "FeeInfo",
            "BondAssetDetail",
            "RemarkForAdditionalDocument",
            "InterestODInfo",
            "MachineAssetDetail",
            "LinkageNoInCase",
            "ProjectNameSoftLoans",
            "RightDebtors",
            "RepaymentTransactionInfos",
            "ExistingLegalActivity",
            "specialLoans",
            "FeeListDetail",
            "TransactionDetail",
            "IsNonstandardContract",
            "LandBuildingAssetDetail",
            "AccountInfo",
            "SpecificDebtInContract",
            "InterestRateValueAsOfDates",
            "SplitOfShareCertificateDetail",
            "SpecialLoanTypes",
            "PeriodInstallment",
            "MachineRegistrationNos",
            "ProcessAgentInfos",
            "CreditLineFees",
            "PensionAssetDetail",
            "GuaranteeGroupDetail",
            "Date",
            "GuaranteeExistingContract",
            "DisbursementInfo",
            "ApplicationDetail",
            "GuaranteeDetail",
            "KeeperDetails",
            "IsProcessIncreaseAndExtendCreditLine",
            "Paragraphs",
            "ContractLanguage"
            ));

    /**
     * Duplicate Field (Array)
     * buildTreeFromXML ส่งผลให้มี field ซ้อนกัน แต่ไม่ส่งผลกระทบต่อไฟล์หลัง Save
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

            // ตรวจสอบว่า field อยู่ใน whitelist หรือไม่
            if (!duplicateAllowedFields.contains(selectedElement.getTagName())) {
                JOptionPane.showMessageDialog(null, "This node cannot be duplicated because it is not allowed.");
                return;
            }

            // คัดลอกโหนดใน XML Document
            Element duplicateElement = (Element) selectedElement.cloneNode(true);

            // ล้างค่าภายในโหนดที่ Duplicate
            clearContent(duplicateElement);

            // เพิ่มโหนดใหม่เข้าไปในโหนดแม่ใน XML Document
            ((Element) parentNode).appendChild(duplicateElement);

            // เพิ่มโหนดใหม่ใน Tree View
            DefaultMutableTreeNode parentTreeNode = (DefaultMutableTreeNode) selectedNode.getParent();
            DefaultMutableTreeNode newTreeNode = new DefaultMutableTreeNode(duplicateElement);
            parentTreeNode.add(newTreeNode);

            // สร้างโหนดย่อย (Child Nodes) ใน Tree View
            buildTreeFromXML(duplicateElement, newTreeNode);

            // อัปเดต Tree View
            ((DefaultTreeModel) tree.getModel()).reload();
            updateTreeView(newTreeNode);
        }
    }

    /**
     * ฟังก์ชันสำหรับอัปเดต Tree View เฉพาะจุด
     *
     * @param node โหนดที่ต้องการอัปเดต
     */
    private static void updateTreeView(DefaultMutableTreeNode node) {
        TreePath path = new TreePath(node.getPath());
        tree.expandPath(path);
        tree.scrollPathToVisible(path);
    }

    /**
     * ล้างเฉพาะ Text Content ในโหนดและโหนดย่อย โดยยังคงโครงสร้าง field (ลูก) ไว้
     *
     * @param node โหนดที่ต้องการล้างเนื้อหา
     */
    private static void clearContent(Element node) {
        // ลบเฉพาะข้อความ (Text Content) ของโหนดปัจจุบัน
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.TEXT_NODE) {
                child.setNodeValue(""); // ล้างข้อความใน Text Node
            } else if (child.getNodeType() == Node.ELEMENT_NODE) {
                clearContent((Element) child); // เรียกซ้ำสำหรับโหนดย่อย
            }
        }
    }


    /**
     * list duplicatable fields
     */
    private static void listDuplicateAllowedFields() {
        if (xmlDocument == null) {
            System.out.println("XML Document is not loaded.");
            return;
        }

        // ใช้ Set เพื่อเก็บชื่อ field ที่ไม่ซ้ำ
        Set<String> duplicateAllowedFields = new HashSet<>();

        // เรียกใช้ฟังก์ชันเพื่อค้นหา field
        Element rootElement = xmlDocument.getDocumentElement();
        collectFieldsWithZeroOrMoreRepetitions(rootElement, duplicateAllowedFields);

        // แสดงผลในรูปแบบที่พร้อม copy ลง whitelist
        System.out.println("Copy the following whitelist:");
        System.out.println("private static final Set<String> duplicateAllowedFields = new HashSet<>(Arrays.asList(");
        for (String field : duplicateAllowedFields) {
            System.out.println("    \"" + field + "\",");
        }
        System.out.println("));");
    }

    /**
     * collect fields with "Zero or more repetitions:"
     *
     * @param node
     * @param duplicateAllowedFields
     */
    private static void collectFieldsWithZeroOrMoreRepetitions(Node node, Set<String> duplicateAllowedFields) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if (child.getNodeType() == Node.ELEMENT_NODE) {
                // ตรวจสอบว่ามีคอมเมนต์ "Zero or more repetitions" ก่อนหน้าโหนดนี้หรือไม่
                Node prev = child.getPreviousSibling();
                while (prev != null) {
                    if (prev.getNodeType() == Node.COMMENT_NODE &&
                            prev.getNodeValue().contains("Zero or more repetitions:")) {
                        duplicateAllowedFields.add(child.getNodeName()); // เพิ่ม field ลงใน Set
                        break;
                    }
                    prev = prev.getPreviousSibling();
                }

                // ค้นหาในโหนดลูกต่อไป
                collectFieldsWithZeroOrMoreRepetitions(child, duplicateAllowedFields);
            }
        }
    }
}
