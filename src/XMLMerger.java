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

import org.w3c.dom.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class XMLMerger {

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

//    public static void main(String[] args) {
//        try {
//            // Load XML Structure
//            File cleanFile = new File("C:\\Installer\\XMLToMerge\\XMLStructure\\coop-xml.xml");
//            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//            factory.setNamespaceAware(true);
//            DocumentBuilder builder = factory.newDocumentBuilder();
//            Document cleanDoc = builder.parse(cleanFile);
//
//            // Load Folder (XML to Merge)
//            File sourceDir = new File("C:\\Installer\\XMLToMerge\\SourceFiles");
//            File[] sourceFiles = sourceDir.listFiles((dir, name) -> name.endsWith(".xml"));
//
//            if (sourceFiles != null) {
//                for (File sourceFile : sourceFiles) {
//                    Document sourceDoc = builder.parse(sourceFile);
//
//                    Document resultDoc = (Document) cleanDoc.cloneNode(true);
//                    Node resultRoot = resultDoc.getDocumentElement();
//
//                    Node sourceRoot = sourceDoc.getDocumentElement();
//                    mergeNodes(resultDoc, resultRoot, sourceRoot);
//
//                    saveMergedXML(resultDoc, sourceFile.getName());
//                    System.out.println("Merged: " + sourceFile.getName());
//                }
//            }
//
//            System.out.println("Merge completed successfully!");
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static void saveMergedXML(Document resultDoc, String sourceFileName) throws TransformerException {
//        TransformerFactory transformerFactory = TransformerFactory.newInstance();
//        Transformer transformer = transformerFactory.newTransformer();
//        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
//
//        String outputFileName = "C:\\Installer\\XMLToMerge\\FinishedAllFiles\\Merged_" + sourceFileName;
//        StreamResult result = new StreamResult(new File(outputFileName));
//
//        DOMSource domSource = new DOMSource(resultDoc);
//        transformer.transform(domSource, result);
//    }

    /**
     * กรณีทำเป็น .exe ต้องเข้ากรณีที่มี UI ให้เลือกไฟล์
     */
    private static JFrame frame;
    private static JTextField xmlStructureField;
    private static JTextField sourceFolderField;
    private static JTextField saveFolderField;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(XMLMerger::XMLMergerGUI);
    }

    private static void XMLMergerGUI() {
        frame = new JFrame("XML Merger Tool");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);
        frame.setLayout(new BorderLayout(10, 10));

        // Panel สำหรับส่วนฟอร์ม
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        xmlStructureField = createFileChooserPanel(formPanel, "XML Structure File:", "Choose XML Structure File", true);
        sourceFolderField = createFileChooserPanel(formPanel, "Source Folder:", "Choose Source Folder", false);
        saveFolderField = createFileChooserPanel(formPanel, "Save Folder:", "Choose Save Folder", false);

        // Panel สำหรับปุ่มด้านล่าง
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton mergeButton = new JButton("Merge Files");
        mergeButton.addActionListener(new MergeActionListener());

        buttonPanel.add(mergeButton);

        // เพิ่ม Panel ต่างๆ เข้า Frame
        frame.add(formPanel, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    /**
     * create File Chooser Panel
     *
     * @param parent
     * @param label
     * @param buttonText
     * @param isFileChooser
     * @return
     */
    private static JTextField createFileChooserPanel(JPanel parent, String label, String buttonText, boolean isFileChooser) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JLabel labelComponent = new JLabel(label);
        JTextField textField = new JTextField();
        JButton button = new JButton(buttonText);

        button.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if (!isFileChooser) {
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            } else {
                fileChooser.setFileFilter(new FileNameExtensionFilter("XML Files", "xml"));
            }

            int result = fileChooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                textField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });

        labelComponent.setPreferredSize(new Dimension(150, 30));
        button.setPreferredSize(new Dimension(150, 30));
        textField.setPreferredSize(new Dimension(300, 30));

        panel.add(labelComponent);
        panel.add(Box.createHorizontalStrut(10)); // เพิ่มช่องว่างระหว่าง label กับ text field
        panel.add(textField);
        panel.add(Box.createHorizontalStrut(10)); // เพิ่มช่องว่างระหว่าง text field กับปุ่ม
        panel.add(button);

        parent.add(panel);
        return textField;
    }

    private static class MergeActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String xmlStructurePath = xmlStructureField.getText();
            String sourceFolderPath = sourceFolderField.getText();
            String saveFolderPath = saveFolderField.getText();

            if (xmlStructurePath.isEmpty() || sourceFolderPath.isEmpty() || saveFolderPath.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please fill in all fields!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                File xmlStructureFile = new File(xmlStructurePath);
                File sourceFolder = new File(sourceFolderPath);
                File[] sourceFiles = sourceFolder.listFiles((dir, name) -> name.endsWith(".xml"));

                if (sourceFiles == null || sourceFiles.length == 0) {
                    JOptionPane.showMessageDialog(frame, "No XML files found in the source folder!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document xmlStructureDoc = builder.parse(xmlStructureFile);

                for (File sourceFile : sourceFiles) {
                    Document sourceDoc = builder.parse(sourceFile);

                    Document resultDoc = (Document) xmlStructureDoc.cloneNode(true);
                    Node resultRoot = resultDoc.getDocumentElement();

                    Node sourceRoot = sourceDoc.getDocumentElement();
                    mergeNodes(resultDoc, resultRoot, sourceRoot);

                    saveMergedXML(resultDoc, saveFolderPath, sourceFile.getName());
                }

                JOptionPane.showMessageDialog(frame, "Merge completed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "An error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }


        /**
         * save Merged XML Files to folder
         *
         * @param resultDoc
         * @param saveFolderPath
         * @param sourceFileName
         * @throws Exception
         */
        private void saveMergedXML(Document resultDoc, String saveFolderPath, String sourceFileName) throws Exception {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            //transformer.setOutputProperty("indent", "yes");

            String outputFileName = saveFolderPath + File.separator + "Merged_" + sourceFileName;
            StreamResult result = new StreamResult(new File(outputFileName));

            DOMSource domSource = new DOMSource(resultDoc);
            transformer.transform(domSource, result);
        }
    }

        /**
         * merge every nodes
         *
         * @param cleanDoc
         * @param cleanNode
         * @param sourceNode
         */
        private static void mergeNodes(Document cleanDoc, Node cleanNode, Node sourceNode) {
            NodeList sourceChildren = sourceNode.getChildNodes();

            for (int i = 0; i < sourceChildren.getLength(); i++) {
                Node sourceChild = sourceChildren.item(i);

                if (sourceChild.getNodeType() == Node.ELEMENT_NODE) {
                    String tagName = sourceChild.getNodeName();

                    /**
                     * ตรวจสอบว่าเป็น Field ที่ต้อง Duplicate
                     * เพิ่มกรณี if ขนานกันระหว่างการตรวจสอบว่าเป็น Field ที่ต้อง Duplicate ไหมในกรณีที่ฝั่ง Clean XML มี Duplicate Field 1 Field เช่น ALSCustomerNum
                     * แต่ฝั่ง Soruce XML มี Duplicate Field > 1 เช่น CollateralDetail, ApplicationDetail
                     * ถ้าเข้ากรณีนั้นจะ Duplicate ออกมา "เกิน" 1 Field เพื่อให้คง Structure ทั้งหมดในนั้น จากนั้นลบต้นฉบับผ่าน removeDuplicateNodes
                     */
                    if (duplicateAllowedFields.contains(tagName)) {
                        int sourceCount = countChildrenWithData(sourceNode, tagName);
                        int cleanCount = countChildrenWithData(cleanNode, tagName);

                        boolean didDuplicate = false;

                        // Duplicate Field ที่จำเป็น
                        if (sourceCount > cleanCount) {
                            duplicateField(cleanDoc, cleanNode, tagName, sourceCount - cleanCount);
                            didDuplicate = true;
                        }

                        // เติมข้อมูลใน Field
                        mergeDuplicateFields(cleanDoc, cleanNode, sourceNode, tagName);

                        // ลบเฉพาะต้นฉบับที่ Duplicate
                        // removeDuplicateNodes เมื่อ node นั้นๆ มีการ action duplicateField เท่านั้น
                        if (didDuplicate) {
                            removeDuplicateNodes(cleanNode, tagName);
                        }
                    } else {
                        Node correspondingCleanNode = findChildNode(cleanNode, tagName);

                        if (correspondingCleanNode != null) {
                            // Merge recursively
                            mergeNodes(cleanDoc, correspondingCleanNode, sourceChild);
                        } else {
                            // Append new node if it does not exist in cleanNode
                            Node importedNode = cleanDoc.importNode(sourceChild, true);
                            cleanNode.appendChild(importedNode);
                        }
                    }
                } else if (sourceChild.getNodeType() == Node.TEXT_NODE) {
                    if (!sourceChild.getTextContent().trim().isEmpty()) {
                        cleanNode.setTextContent(sourceChild.getTextContent().trim());
                    }
                }
            }
        }

        /**
         * remove duplicated nodes (เช็ค isNodeEmpty ไหม จากนั้นค่อยลบ Node ต้นฉบับ)
         *
         * @param parent
         * @param tagName
         */
        private static void removeDuplicateNodes(Node parent, String tagName) {
            NodeList children = parent.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);

                if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals(tagName)) {
                    // ตรวจสอบว่าโหนดว่างเปล่าหรือซ้ำซ้อน
                    if (isNodeEmpty(child)) {
                        parent.removeChild(child);
                        i--; // ปรับ index หลังจากลบ
                    }
                }
            }
        }

        /**
         * check if node is empty or not (กรณีนี้เช็คเพื่อที่จะลบ Duplicated Nodes ต้นฉบับ)
         *
         * @param node
         * @return
         */

        private static boolean isNodeEmpty(Node node) {
            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);

                if (child.getNodeType() == Node.ELEMENT_NODE && !child.getTextContent().trim().isEmpty()) {
                    return false; // โหนดมีข้อมูล
                }
            }
            return true; // โหนดว่าง
        }

        /**
         * ฟังก์ชัน Duplicate Field
         *
         * @param cleanDoc
         * @param cleanNode
         * @param tagName
         * @param count
         */
        private static void duplicateField(Document cleanDoc, Node cleanNode, String tagName, int count) {
            Node templateNode = findChildNode(cleanNode, tagName);

            if (templateNode != null) {
                for (int i = 0; i < count; i++) {
                    Node duplicateNode = cleanDoc.importNode(templateNode, true);
                    cleanNode.appendChild(duplicateNode);
                }
            }
        }

        /**
         * ฟังก์ชันเติมข้อมูลใน Field ที่ Duplicate
         *
         * @param cleanDoc
         * @param cleanNode
         * @param sourceNode
         * @param tagName
         */
        private static void mergeDuplicateFields(Document cleanDoc, Node cleanNode, Node sourceNode, String tagName) {
            NodeList cleanChildren = cleanNode.getChildNodes();
            NodeList sourceChildren = sourceNode.getChildNodes();

            int cleanIndex = 0;

            for (int i = 0; i < sourceChildren.getLength(); i++) {
                Node sourceChild = sourceChildren.item(i);

                if (sourceChild.getNodeType() == Node.ELEMENT_NODE && sourceChild.getNodeName().equals(tagName)) {
                    while (cleanIndex < cleanChildren.getLength()) {
                        Node cleanChild = cleanChildren.item(cleanIndex);

                        if (cleanChild.getNodeType() == Node.ELEMENT_NODE && cleanChild.getNodeName().equals(tagName)) {
                            mergeNodes(cleanDoc, cleanChild, sourceChild);
                            cleanIndex++;
                            break;
                        }
                        cleanIndex++;
                    }
                }
            }
        }

        /**
         * ฟังก์ชันตรวจสอบจำนวน Field ที่มีข้อมูล
         *
         * @param parent
         * @param tagName
         * @return
         */
        private static int countChildrenWithData(Node parent, String tagName) {
            NodeList children = parent.getChildNodes();
            int count = 0;

            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);

                if (child.getNodeName().equals(tagName) && hasNonEmptyChild(child)) {
                    count++;
                }
            }
            return count;
        }

        /**
         * ฟังก์ชันตรวจสอบว่ามีข้อมูลใน Node หรือไม่
         *
         * @param node
         * @return
         */
        private static boolean hasNonEmptyChild(Node node) {
            NodeList children = node.getChildNodes();

            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    if (!child.getTextContent().trim().isEmpty() || hasNonEmptyChild(child)) {
                        return true;
                    }
                }
            }
            return false;
        }

        /**
         * ฟังก์ชันค้นหา Node ตามชื่อ
         *
         * @param parent
         * @param tagName
         * @return
         */
        private static Node findChildNode(Node parent, String tagName) {
            NodeList children = parent.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeName().equals(tagName)) {
                    return child;
                }
            }
            return null;
        }
    }



