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
 * การทำงานของ "buildTreeFromXML" และกระบวนการ Duplicate Node
 *
 * - "buildTreeFromXML" ส่งผลให้เกิด field ซ้อนกัน (DuplicateNode) แต่จะไม่ส่งผลกระทบต่อไฟล์ XML หลังจาก Save
 *
 * - การระบุชนิดข้อมูลของ Field ใน ReadyAPI:
 *   1. ReadyAPI รู้ชนิดข้อมูล (Data Type) ของ Field โดยอ้างอิงจากการ Generate Project ผ่าน WSDL
 *   2. ชื่อ Field แต่ละอันจะถูก Map กับ Outline ที่มีการระบุชนิดข้อมูลไว้ล่วงหน้า
 *   3. ไฟล์ XML ไม่สามารถกำหนด Data Type ได้โดยตรง แต่จะอ้างอิงจาก XSD ซึ่ง WSDL ใช้ในการดึงข้อมูลโครงสร้าง (Schema จำนวน 12 ไฟล์)
 *
 * - กระบวนการ Duplicate Node แบบเดิม:
 *   1. ค้นหาคำว่า "Zero or more repetitions:" ในไฟล์ XML ที่ Generate ผ่าน SoapUI
 *   2. Field ใดที่มีการกำหนด "Unbound" จะปรากฏข้อความ "Zero or more repetitions:" เหนือ Field นั้น
 *   3. หากพบ Field เหล่านี้ ระบบจะเพิ่ม Field ซ้ำ (Duplicate) ตามความจำเป็น
 *
 * - กระบวนการ Duplicate Node แบบใหม่:
 *   1. ใช้ฟังก์ชัน "collectFieldsWithZeroOrMoreRepetitions" ใน "listDuplicateAllowedFields"
 *      เพื่อรวบรวม Field ที่เข้าข่าย "Zero or more repetitions:"
 *   2. รายการ Field ที่พบทั้งหมดจะแสดงใน Terminal โดยเป็น Field ที่ตัดข้อมูลซ้ำออกแล้ว
 *   3. จากนั้น Copy รายการ ArrayList ที่ได้ไปใส่ใน "duplicateAllowedFields" (Whitelist) ด้วยตนเอง (Manual)
 *
 * - ข้อดีของกระบวนการแบบใหม่:
 *   1. ไม่ต้องค้นหาคำว่า "Zero or more repetitions:" ใน XML ซ้ำอีก
 *   2. ลดความซับซ้อนในกรณีที่ไฟล์ XML จริงไม่ได้ Generate มาจาก SoapUI แต่ถูกสร้างจาก Excel
 *      (เช่น จาก Excel -> XML โดยตรง แทนที่จะเป็น WSDL -> XML)
 */


import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
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

    private static final Set<String> duplicateAllowedFields = new HashSet<>(Arrays.asList(
            "LetterOfGuaranteeAssetDetail", "NotGenerate", "ExistingGuaranteeCollateralDetails", "needPaymentForAccruedInterestAmount", "isCustWillReqForDrawdownAfterCreditLineDecrease",
            "AuthorizedPersonToSignContract", "Exe1VerifiedCorrect", "LeaseholdAssetDetail", "DocumentProperty", "SecurityDetail",
            "StepRate", "JointVentureOrConsortiumProfiles", "PersonalOrJuristicProfiles", "FeePaymentMethod", "TFCCustomerNum",
            "DGENSupport", "MortgageRank", "LegalActivityDetail", "feeContractReference", "CreditLineAccountInfo",
            "StockAssetDetail", "ShipAssetDetail", "OwnerAuthPersonToPerformLegalActDetail", "CodeNameMappings", "LeaseholdAssetSubDetails",
            "CarAssetDetail", "ExistingGuaranteeContractDetails", "BillExchangeAssetDetail", "ExistingLegalActType", "CustomerNameAndGUID",
            "TemplateRefValue", "InstallmentEveryMonth", "CommoditiesAssetDetail", "Locations", "PackageFinanceDetails",
            "AddExternalError", "Sequence", "TemplateKeyGUID", "TemplateDetail", "GoldAssetDetail",
            "BillingScheduleTypes", "ALSCustomerNum", "CondominiumAssetDetail", "TemplateSubLevel", "TransferTo",
            "BillInfos", "isSME", "needPaymentForAccruedInterest", "Covenants", "RightOnBenefitAssetDetail",
            "CommercialCollateralContractDetail", "authorizedPersonToPerformLegalAct", "AuthorizedPerson", "ISupplyInfo", "PNDuePayments",
            "CollateralDetail", "CustIdentification", "LeasingAssetDetail", "OtherFeeInfo", "PaymentStep",
            "FeeInfo", "BondAssetDetail", "RemarkForAdditionalDocument", "InterestODInfo", "MachineAssetDetail",
            "LinkageNoInCase", "ProjectNameSoftLoans", "RightDebtors", "RepaymentTransactionInfos", "ExistingLegalActivity",
            "specialLoans", "FeeListDetail", "TransactionDetail", "IsNonstandardContract", "LandBuildingAssetDetail",
            "AccountInfo", "SpecificDebtInContract", "InterestRateValueAsOfDates", "SplitOfShareCertificateDetail", "SpecialLoanTypes",
            "PeriodInstallment", "MachineRegistrationNos", "ProcessAgentInfos", "CreditLineFees", "PensionAssetDetail",
            "GuaranteeGroupDetail", "Date", "GuaranteeExistingContract", "DisbursementInfo", "ApplicationDetail",
            "GuaranteeDetail", "KeeperDetails", "IsProcessIncreaseAndExtendCreditLine", "Paragraphs", "ContractLanguage"
    ));

    public static void main(String[] args) {
        SwingUtilities.invokeLater(DynamicXMLTreeEditor::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Dynamic XML Tree Editor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);

        JSplitPane splitPane = new JSplitPane();
        JPanel editorPanel = new JPanel(new BorderLayout());
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JPanel fieldPanel = new JPanel(new GridBagLayout());

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("XML");
        tree = new JTree(root);
        JScrollPane treeScrollPane = new JScrollPane(tree);
//        JScrollPane valueScrollPane = new JScrollPane(valueField);

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

        gbc.gridx = 0; gbc.gridy = 0; fieldPanel.add(fieldNameLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 0; fieldPanel.add(fieldNameField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; fieldPanel.add(valueLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 1; fieldPanel.add(valueField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; fieldPanel.add(saveValueButton, gbc);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; fieldPanel.add(addSubfieldButton, gbc);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; fieldPanel.add(deleteFieldButton, gbc);
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; fieldPanel.add(undoButton, gbc);
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2; fieldPanel.add(redoButton, gbc);
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2; fieldPanel.add(duplicateFieldButton, gbc);
//        fieldPanel.add(valueScrollPane, gbc);

        editorPanel.add(fieldPanel, BorderLayout.NORTH);

        JButton xmlMergerButton = new JButton("XML Merger");
        JButton loadXMLButton = new JButton("Load XML");
        JButton saveFileButton = new JButton("Save File");

        bottomPanel.add(xmlMergerButton);
        bottomPanel.add(loadXMLButton);
        bottomPanel.add(saveFileButton);

        splitPane.setLeftComponent(treeScrollPane);
        splitPane.setRightComponent(editorPanel);
        splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(500);

        frame.getContentPane().add(splitPane, BorderLayout.CENTER);
        frame.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
        frame.setVisible(true);

        tree.addTreeSelectionListener(e -> {
            selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (selectedNode != null && selectedNode.getUserObject() instanceof Element) {
                Element element = (Element) selectedNode.getUserObject();
                fieldNameField.setText(element.getTagName());
                valueField.setText(element.getTextContent());
            }
        });

        saveValueButton.addActionListener(e -> {saveStateToUndoStack();saveFieldValue();});
        addSubfieldButton.addActionListener(e -> {saveStateToUndoStack();addSubfield();});
        deleteFieldButton.addActionListener(e -> {saveStateToUndoStack();deleteField();});
        undoButton.addActionListener(e -> undo());
        redoButton.addActionListener(e -> redo());
        duplicateFieldButton.addActionListener(e -> {saveStateToUndoStack();duplicateNode();});

        xmlMergerButton.addActionListener(e -> {
            try {
                File exeFile;

                // 1. ลองหาไฟล์ใน Resource (กรณี XMLMerger.exe อยู่ใน Resource ของ JAR)
                URL resource = DynamicXMLTreeEditor.class.getResource("/XMLMerger.exe");
                if (resource != null) {
                    exeFile = new File(resource.toURI());
                } else {
                    // 2. ลองหาไฟล์จากโฟลเดอร์เดียวกับที่รันโปรแกรม
                    // exeFile = new File("XMLMerger.exe");
                    exeFile = new File("src/application/XMLMerger.exe");
                }

                // ตรวจสอบว่าไฟล์มีอยู่จริงหรือไม่
                if (!exeFile.exists()) {
                    JOptionPane.showMessageDialog(frame, "Error: XMLMerger.exe not found!",
                            "File Not Found", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Run XMLMerger.exe
                ProcessBuilder pb = new ProcessBuilder(exeFile.getAbsolutePath());
                pb.directory(exeFile.getParentFile()); // ตั้งค่า working directory
                pb.start();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error: Unable to execute XMLMerger.exe",
                        "Execution Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });


        loadXMLButton.addActionListener(e -> {loadXMLFile(frame);listDuplicateAllowedFields();});
        saveFileButton.addActionListener(e -> saveXMLFile(frame));
    }

    /**
     * Save XML structure to undo stack
     */
    private static void saveStateToUndoStack() {
        try {
            String currentState = convertXMLToString();
            if (!undoStack.isEmpty() && !currentState.equals(undoStack.peek())) {
                undoStack.push(currentState);
                redoStack.clear();
            }
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
        // transformer.setOutputProperty(OutputKeys.INDENT, "yes");
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

    /**
     * Reload tree after undo/redo
     */
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
     */
    private static void loadXMLFile(JFrame frame) {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();

                // โหลด XML Document
                xmlDocument = builder.parse(file);
                xmlDocument.getDocumentElement().normalize();

                // สร้าง root node ของ Tree
                DefaultMutableTreeNode root = new DefaultMutableTreeNode(xmlDocument.getDocumentElement());
                buildTreeFromXML(xmlDocument.getDocumentElement(), root);

                // ตั้งค่า Tree
                tree.setModel(new DefaultTreeModel(root));
                addTreeWillExpandListener(tree);

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error loading XML file: " + e.getMessage());
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

        // ตรวจสอบว่ามีลูก (subfield) ที่เป็น Element หรือไม่
        NodeList children = element.getChildNodes();
        boolean hasElementChild = false;
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                hasElementChild = true;
                break;
            }
        }

        // เพิ่ม Placeholder "Loading..." เฉพาะเมื่อมีลูกที่เป็น Element
        if (hasElementChild) {
            DefaultMutableTreeNode loadingNode = new DefaultMutableTreeNode("Loading...");
            node.add(loadingNode);
        }
    }

    /**
     * Adds a listener to dynamically load XML child nodes when a tree node is expanded
     *
     * @param tree
     */
    private static void addTreeWillExpandListener(JTree tree) {
        tree.addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override
            public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
                DefaultMutableTreeNode expandingNode = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();

                // ตรวจสอบว่า UserObject เป็น Element
                if (expandingNode.getUserObject() instanceof Element) {
                    Element expandingElement = (Element) expandingNode.getUserObject();
                    NodeList children = expandingElement.getChildNodes();

                    // ลบ Placeholder ("Loading...") และเพิ่มลูกใหม่
                    if (expandingNode.getChildCount() == 1 && "Loading...".equals(expandingNode.getFirstChild().toString())) {
                        expandingNode.removeAllChildren(); // ลบ Placeholder

                        for (int i = 0; i < children.getLength(); i++) {
                            Node child = children.item(i);
                            if (child.getNodeType() == Node.ELEMENT_NODE) {
                                buildTreeFromXML((Element) child, expandingNode);
                            }
                        }
                        ((DefaultTreeModel) tree.getModel()).reload(expandingNode);
                    }
                } else {
                    throw new ClassCastException("Tree Node does not contain an Element object");
                }
            }
            @Override
            public void treeWillCollapse(TreeExpansionEvent event) {
            }
        });
    }

    /**
     * update node label
     *
     * @param node
     * @param element
     */
    private static void updateNodeLabel(DefaultMutableTreeNode node, Element element) {
        String label = element.getTagName() + " : " + element.getTextContent();
        node.setUserObject(label);
    }

    /**
     * highlight node
     *
     * @param node
     */
    private static void highlightNode(DefaultMutableTreeNode node) {
        tree.setSelectionPath(new TreePath(node.getPath()));
    }


    /**
     * save field value
     */
    private static void saveFieldValue() {
        if (selectedNode != null && selectedNode.getUserObject() instanceof Element) {
            Element element = (Element) selectedNode.getUserObject();
            element.setTextContent(valueField.getText());
            updateNodeLabel(selectedNode, element);
            ((DefaultTreeModel) tree.getModel()).reload(selectedNode);
            highlightNode(selectedNode);
        }
    }

    /**
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
     *
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
                // transformer.setOutputProperty(OutputKeys.INDENT, "yes");
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
     * - "buildTreeFromXML" ส่งผลให้เกิด field ซ้อนกัน (DuplicateNode) แต่จะไม่ส่งผลกระทบต่อไฟล์ XML หลังจาก Save
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
     * โหนดที่ต้องการอัปเดต
     *
     * @param node
     */
    private static void updateTreeView(DefaultMutableTreeNode node) {
        TreePath path = new TreePath(node.getPath());
        tree.expandPath(path);
        tree.scrollPathToVisible(path);
    }

    /**
     * ล้างเฉพาะ Text Content ในโหนดและโหนดย่อย โดยยังคงโครงสร้าง field (ลูก) ไว้
     * โหนดที่ต้องการล้างเนื้อหา
     *
     * @param node
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
