/**
 * Copyright (c) 2025 Phumsith Insakul. All rights reserved.
 *
 * This code is part of the XMLMerger application.
 *
 * Author: Phumsith Insakul
 * Email: phumparfait@gmail.com
 *
 * Description:
 * XMLMerger Allows you to merge from folder that contains XML files (Test Data) to XML Structure File
 *
 * License: MIT License
 */

/**
 * วิธีการใช้งาน:
 * 1. ใช้งานผ่านการรันโค้ดโดยตรง (เลือก Path สำหรับ XML Structure, XML to Merge และ Save Path)
 * 2. ใช้งานผ่าน GUI (บรรทัด 130)
 * 3. ใช้งานผ่าน Application (src/application/XMLMerger.exe)
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
import java.util.*;
import java.util.List;
import java.util.stream.IntStream;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

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

    public static void main(String[] args) {
        try {
            // จับเวลาเริ่มต้น
            long startTime = System.nanoTime();

            // Load XML Structure
            File cleanFile = new File("C:\\Installer\\XMLToMerge\\XMLStructure\\clean-xml.xml");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document cleanDoc = builder.parse(cleanFile);

            // Load Folder (XML to Merge)
            File sourceDir = new File("C:\\Installer\\XMLToMerge\\SourceFiles\\testdataXML_SIT");
            File[] sourceFiles = sourceDir.listFiles((dir, name) -> name.endsWith(".xml"));

            // Header ของ log
            System.out.printf("%-30s | %-15s\n", "File Name", "Time (seconds)");
            System.out.println("--------------------------------------------------");

            if (sourceFiles != null) {
                for (File sourceFile : sourceFiles) {
                    long mergeStartTime = System.nanoTime(); // จับเวลาเริ่มต้นแต่ละไฟล์
                    Document sourceDoc = builder.parse(sourceFile);

                    Document resultDoc = (Document) cleanDoc.cloneNode(true);
                    Node resultRoot = resultDoc.getDocumentElement();

                    Node sourceRoot = sourceDoc.getDocumentElement();
                    mergeNodes(resultDoc, resultRoot, sourceRoot);

                    saveMergedXML(resultDoc, sourceFile.getName());
                    long mergeEndTime = System.nanoTime(); // จับเวลาสิ้นสุดแต่ละไฟล์

                    // Log รายละเอียดของแต่ละไฟล์
                    System.out.printf("%-30s | %-15.7f\n", sourceFile.getName(),
                            (mergeEndTime - mergeStartTime) / 1_000_000_000.0);
                }
            }

            long endTime = System.nanoTime(); // จับเวลาสิ้นสุด
            System.out.println("--------------------------------------------------");
            System.out.printf("%-30s | %-15.7f\n", "Total Execution Time",
                    (endTime - startTime) / 1_000_000_000.0);
            System.out.println("Merge completed successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * save merged XML Files only for manual filled paths
     *
     * @param resultDoc
     * @param sourceFileName
     * @throws TransformerException
     */
    private static void saveMergedXML(Document resultDoc, String sourceFileName) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        String outputFileName = "C:\\Installer\\XMLToMerge\\FinishedAllFiles\\Mock_Result_testdataXML_SIT_HashMap\\" /* + "Merged_" */ + sourceFileName;
        StreamResult result = new StreamResult(new File(outputFileName));

        DOMSource domSource = new DOMSource(resultDoc);
        transformer.transform(domSource, result);
    }

    /**
     * กรณีทำเป็น .exe (Application) ต้องเข้ากรณีที่มี UI ให้เลือกไฟล์
     * สามารถใช้งาน "XMLMerger Application" ได้ที่ src/application/XMLMerger.exe
     */
    private static JFrame frame;
    private static JTextField xmlStructureField;
    private static JTextField sourceFolderField;
    private static JTextField saveFolderField;

    /**
     * 3 บรรทัดด้านล่าง คือโค้ดที่ใช้เปิดใช้งาน GUI (อย่าลืมปิด main ด้านบน)
     */
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(XMLMerger::XMLMergerGUI);
//    }

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
                JOptionPane.showMessageDialog(frame, "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                File xmlStructureFile = new File(xmlStructurePath);
                File sourceFolder = new File(sourceFolderPath);
                File[] sourceFiles = sourceFolder.listFiles((dir, name) -> name.endsWith(".xml"));

                if (sourceFiles == null || sourceFiles.length == 0) {
                    JOptionPane.showMessageDialog(frame, "No XML files found in the source folder", "Error", JOptionPane.ERROR_MESSAGE);
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
            // transformer.setOutputProperty("indent", "yes");

            String outputFileName = saveFolderPath + File.separator /* + "Merged_" */ + sourceFileName;
            StreamResult result = new StreamResult(new File(outputFileName));

            DOMSource domSource = new DOMSource(resultDoc);
            transformer.transform(domSource, result);
        }
    }

        /**
         * รวมโหนด (Node) ระหว่างโครงสร้าง XML สองชุด (4 กรณีหลัก)
         *
         * เมธอดนี้ใช้สำหรับการรวมโหนดจาก XML ต้นทาง (source) เข้ากับ XML ปลายทาง (clean)
         * โดยรองรับ 4 กรณีหลัก:
         * 1. การรวมโหนดแบบปกติ: โหนดที่มีโครงสร้างตรงกันจะถูกรวมข้อมูลเข้าด้วยกัน
         * 2. การรวมโหนดแบบ "Unbound Field" ที่มีลูกเพียง 1 ตัว เช่น ALSCustomerNums ที่มี "ALSCustomerNum"
         * 3. การรวมโหนดแบบ "Unbound Field" ที่มีโครงสร้างซับซ้อน เช่น CollateralDetails ที่มีหลาย "CollateralDetail"
         * 4. การรวมโหนดแบบ "Unbound Field" ที่ไม่มีโครงสร้างลูก (ไม่มี Subfields) เช่น ProjectNameSoftLoans ที่ไม่มี "ProjectNameSoftLoan"
         *
         * เมธอดนี้มี 2 รูปแบบ:
         * แบบที่ 1: ทำงานกับโครงสร้างโหนดโดยตรง ไม่ใช้ HashMap เหมาะสำหรับ XML ที่มีขนาดเล็กหรือโครงสร้างไม่ซับซ้อน แต่ประสิทธิภาพอาจช้ากว่าสำหรับเอกสารขนาดใหญ่
         * แบบที่ 2: ใช้ "HashMap" ในการค้นหาและรวมโหนด เหมาะสำหรับเอกสาร XML ที่มีขนาดใหญ่และต้องการความเร็วในการประมวลผล (เร็วขึ้นสูงสุด 2.67 เท่า)
         *
         * โค้ดนี้เป็นรูปแบบที่ 1 โดยประมวลผลโหนดแบบเรียงลำดับโดยตรง
         *
         * @param cleanDoc
         * @param cleanNode
         * @param sourceNode
         */
//        private static void mergeNodes(Document cleanDoc, Node cleanNode, Node sourceNode) {
//            NodeList sourceChildren = sourceNode.getChildNodes();
//
//            for (int i = 0; i < sourceChildren.getLength(); i++) {
//                Node sourceChild = sourceChildren.item(i);
//
//                if (sourceChild.getNodeType() == Node.ELEMENT_NODE) {
//                    String tagName = sourceChild.getNodeName();
//
//                    /**
//                     * 1. ตรวจสอบว่า Field ต้อง Duplicate หรือไม่:
//                     * - ในกรณีที่ XML โครงสร้าง (Structure) มี Field ซ้ำเพียง 1 อัน เช่น ALSCustomerNum แต่ XML ข้อมูล (Test Data) มี Field ซ้ำหลายอัน เช่น CollateralDetail, TransactionDetail, หรือ DisbursementInfo
//                     * - ระบบจะ Duplicate Field เพิ่มขึ้น เพื่อให้โครงสร้างรองรับข้อมูลทั้งหมด แล้วลบต้นฉบับที่ซ้ำด้วย removeDuplicateNodes
//                     */
//                    if (duplicateAllowedFields.contains(tagName)) {
//                        int sourceCount = countChildrenWithData(sourceNode, tagName);
//                        int cleanCount = countChildrenWithData(cleanNode, tagName);
//                        boolean didDuplicate = false;
//                        boolean didSpecialDuplicate = false;
//
//                        /**
//                         * 2. กรณีจำนวน Field ใน Source มากกว่า Clean:
//                         * - หาก sourceCount > cleanCount เช่น CollateralDetail มี 2 Field ใน Source แต่ Clean มีแค่ 1 Field (เพราะเป็นโครงสร้างเปล่า):
//                         * - ระบบจะ Duplicate Field เพิ่มเติมเพื่อให้โครงสร้างรองรับข้อมูลทั้งหมด
//                         */
//                        if (sourceCount > cleanCount) {
//                            duplicateField(cleanDoc, cleanNode, tagName, sourceCount - cleanCount);
//                            didDuplicate = true;
//                        }
//
//                        /**
//                         * 3. กรณี Field พิเศษ ("Special Duplicate Fields"):
//                         * - ตรวจสอบว่า Field ที่อยู่ในกลุ่ม isSpecialDuplicateFields เช่น ProjectNameSoftLoans, ExistingGuaranteeCollateralDetails, ...
//                         * - หากพบว่า Field เหล่านี้มีข้อมูลซ้ำหลายอัน ระบบจะเพิ่ม/รวมข้อมูลจาก Source เข้ามาใน Clean และลบต้นฉบับด้วย removeSpecialDuplicateNodes
//                         * - การแยกกรณีนี้ออกจาก Duplicate ปกติเพราะเป็น Field แบบ Unbound ที่ไม่มี Subfield เช่น ProjectNameSoftLoans ไม่มี "ProjectNameSoftLoan"
//                         */
//                        if (isSpecialDuplicateFields(tagName)) {
//                            Node importedNode = cleanDoc.importNode(sourceChild, true);
//                            cleanNode.appendChild(importedNode);
//                            didSpecialDuplicate = true;
//                        }
//
//                        /**
//                         * 4. การ Merge หลัง Duplicate:
//                         * - เมื่อ Duplicate Field เพิ่มแล้ว ระบบจะ Merge ข้อมูลเฉพาะ Field ที่ถูก Duplicate ไม่กระทบต้นฉบับ
//                         */
//                        mergeDuplicateFields(cleanDoc, cleanNode, sourceNode, tagName);
//
//                        /**
//                         * 5. ลบต้นฉบับหลัง Duplicate:
//                         * - หากมีการ Duplicate Field (didDuplicate เป็นจริง): ลบต้นฉบับซ้ำด้วย removeDuplicateNodes
//                         * - หากมีการ Special Duplicate (didSpecialDuplicate เป็นจริง): ลบต้นฉบับพิเศษด้วย removeSpecialDuplicateNodes
//                         */
//                        if (didDuplicate) {
//                            removeDuplicateNodes(cleanNode, tagName);
//                        } else if (didSpecialDuplicate) {
//                            removeSpecialDuplicateNodes(cleanNode, tagName);
//                        }
//                    } else {
//                        Node correspondingCleanNode = findChildNode(cleanNode, tagName);
//
//                        if (correspondingCleanNode != null) {
//                            mergeNodes(cleanDoc, correspondingCleanNode, sourceChild);
//                        } else {
//                            Node importedNode = cleanDoc.importNode(sourceChild, true);
//                            cleanNode.appendChild(importedNode);
//                        }
//                    }
//                } else if (sourceChild.getNodeType() == Node.TEXT_NODE) {
//                    if (!sourceChild.getTextContent().trim().isEmpty()) {
//                        cleanNode.setTextContent(sourceChild.getTextContent().trim());
//                    }
//                }
//            }
//        }


    /**
     * รวมโหนด (Node) ระหว่างโครงสร้าง XML สองชุด (4 กรณีหลัก)
     *
     * เมธอดนี้ใช้สำหรับการรวมโหนดจาก XML ต้นทาง (source) เข้ากับ XML ปลายทาง (clean)
     * โดยรองรับ 4 กรณีหลัก:
     * 1. การรวมโหนดแบบปกติ: โหนดที่มีโครงสร้างตรงกันจะถูกรวมข้อมูลเข้าด้วยกัน
     * 2. การรวมโหนดแบบ "Unbound Field" ที่มีลูกเพียง 1 ตัว เช่น ALSCustomerNums ที่มี "ALSCustomerNum"
     * 3. การรวมโหนดแบบ "Unbound Field" ที่มีโครงสร้างซับซ้อน เช่น CollateralDetails ที่มีหลาย "CollateralDetail"
     * 4. การรวมโหนดแบบ "Unbound Field" ที่ไม่มีโครงสร้างลูก (ไม่มี Subfields) เช่น ProjectNameSoftLoans ที่ไม่มี "ProjectNameSoftLoan"
     *
     * เมธอดนี้มี 2 รูปแบบ:
     * แบบที่ 1: ทำงานกับโครงสร้างโหนดโดยตรง ไม่ใช้ HashMap เหมาะสำหรับ XML ที่มีขนาดเล็กหรือโครงสร้างไม่ซับซ้อน แต่ประสิทธิภาพอาจช้ากว่าสำหรับเอกสารขนาดใหญ่
     * แบบที่ 2: ใช้ "HashMap" ในการค้นหาและรวมโหนด เหมาะสำหรับเอกสาร XML ที่มีขนาดใหญ่และต้องการความเร็วในการประมวลผล (เร็วขึ้นสูงสุด 2.67 เท่า)
     *
     * โค้ดนี้เป็นแบบที่ 2 โดยใช้ HashMap เพื่อเพิ่มประสิทธิภาพ
     *
     * @param cleanDoc
     * @param cleanNode
     * @param sourceNode
     */
    private static void mergeNodes(Document cleanDoc, Node cleanNode, Node sourceNode) {
            // สร้าง HashMap สำหรับ cleanNode
            Map<String, List<Node>> cleanNodeMap = buildNodeMap(cleanNode);

            NodeList sourceChildren = sourceNode.getChildNodes();

            for (int i = 0; i < sourceChildren.getLength(); i++) {
                Node sourceChild = sourceChildren.item(i);

                if (sourceChild.getNodeType() == Node.ELEMENT_NODE) {
                    String tagName = sourceChild.getNodeName();

                    // ตรวจสอบว่าเป็น Field ที่ต้อง Duplicate
                    if (duplicateAllowedFields.contains(tagName)) {
                        int sourceCount = countChildrenWithData(sourceNode, tagName);
                        int cleanCount = cleanNodeMap.containsKey(tagName) ? cleanNodeMap.get(tagName).size() : 0;

                        boolean didDuplicate = false;
                        boolean didSpecialDuplicate = false;

                        // Duplicate Field เกินไปก่อน
                        if (sourceCount > cleanCount) {
                            duplicateField(cleanDoc, cleanNode, tagName, sourceCount - cleanCount);
                            didDuplicate = true;

                            // อัพเดต HashMap หลัง Duplicate
                            cleanNodeMap = buildNodeMap(cleanNode);
                        }

                        // เช็คกรณีพิเศษ
                        if (isSpecialDuplicateFields(tagName)) {
                            Node importedNode = cleanDoc.importNode(sourceChild, true);
                            cleanNode.appendChild(importedNode);
                            didSpecialDuplicate = true;

                            // อัพเดต HashMap หลังเพิ่ม Special Node
                            cleanNodeMap = buildNodeMap(cleanNode);
                        }

                        // Merge ข้อมูล
                        mergeDuplicateFields(cleanDoc, cleanNode, sourceNode, tagName);

                        // ลบต้นฉบับหลัง Duplicate
                        if (didDuplicate) {
                            removeDuplicateNodes(cleanNode, tagName);
                            cleanNodeMap = buildNodeMap(cleanNode);
                        } else if (didSpecialDuplicate) {
                            removeSpecialDuplicateNodes(cleanNode, tagName);
                            cleanNodeMap = buildNodeMap(cleanNode);
                        }
                    } else {
                        // กรณีไม่ใช่ Field ที่ Duplicate
                        List<Node> correspondingCleanNodes = cleanNodeMap.get(tagName);

                        if (correspondingCleanNodes != null && !correspondingCleanNodes.isEmpty()) {
                            mergeNodes(cleanDoc, correspondingCleanNodes.get(0), sourceChild);
                        } else {
                            Node importedNode = cleanDoc.importNode(sourceChild, true);
                            cleanNode.appendChild(importedNode);

                            // อัพเดต HashMap หลังเพิ่ม Node
                            cleanNodeMap = buildNodeMap(cleanNode);
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
     * สร้างแผนที่ (HashMap) ของโหนดจาก NodeList
     *
     * ฟังก์ชันนี้จะสร้างแผนที่ (Map) ที่จับคู่ tagName ของโหนดลูก (Child Node)
     * กับรายการโหนด (List<Node>) ที่มีชื่อแท็กเดียวกันทั้งหมด
     * โดยโหนดที่ไม่ใช่ ELEMENT_NODE จะถูกกรองออก
     *
     * ตัวอย่าง:
     * หาก parentNode มีโหนดลูกดังนี้:
     * <parent>
     *     <childA>...</childA>
     *     <childB>...</childB>
     *     <childA>...</childA>
     * </parent>
     *
     * จะคืนค่าเป็นแผนที่:
     * {
     *     "childA" => [Node1, Node3],
     *     "childB" => [Node2]
     * }
     *
     * การประมวลผลภายในฟังก์ชันใช้แนวคิด Functional Programming ผ่าน Java Stream API:
     * - ใช้ IntStream เพื่อวนลูปแบบลำดับอินเด็กซ์ (Index-based Iteration)
     * - ใช้ mapToObj เพื่อแปลงอินเด็กซ์เป็น Node
     * - ใช้ filter เพื่อตัดโหนดที่ไม่ใช่ ELEMENT_NODE
     * - ใช้ forEach พร้อม Lambda Expression เพื่อเพิ่มโหนดลงใน Map
     *
     * @param parentNode โหนดต้นทางที่ต้องการสร้างแผนที่โหนดลูก
     * @return แผนที่ (Map) ที่จับคู่ tagName กับ List ของโหนดลูกที่มีชื่อแท็กเดียวกัน
     */

    private static Map<String, List<Node>> buildNodeMap(Node parentNode) {
        Map<String, List<Node>> nodeMap = new HashMap<>();
        NodeList children = parentNode.getChildNodes();

        IntStream.range(0, children.getLength())
                .mapToObj(children::item)
                .filter(child -> child.getNodeType() == Node.ELEMENT_NODE)
                .forEach(child -> {
                    String tagName = child.getNodeName();
                    nodeMap.computeIfAbsent(tagName, k -> new ArrayList<>()).add(child);
                });

        return nodeMap;
    }

    /**
         * check special duplicate fields (Unbound แต่ไม่มี Subfield)
         *
         * @param tagName
         * @return
         */
        private static boolean isSpecialDuplicateFields(String tagName) {
            List<String> specialDuplicateFields = Arrays.asList(
                    "ProjectNameSoftLoans", "ExistingGuaranteeCollateralDetails", "creditLineFees",
                    "feeContractReference", "SpecificDebtInContract", "RemarkForAdditionalDocument"
            );
            return specialDuplicateFields.contains(tagName);
        }

        /**
         * remove special duplicate nodes contains:
         * "ProjectNameSoftLoans", "ExistingGuaranteeCollateralDetails", "creditLineFees",
         * "feeContractReference", "SpecificDebtInContract", "RemarkForAdditionalDocument"
         *
         * @param parentNode
         * @param tagName
         */
        public static void removeSpecialDuplicateNodes(Node parentNode, String tagName) {
            // สร้าง List เพื่อเก็บค่าที่ได้จาก <ProjectNameSoftLoans> และอีก 5 Fields
            Set<String> uniqueValues = new HashSet<>();

            // รับทั้งหมดของลูกใน parentNode
            NodeList nodeList = parentNode.getChildNodes();

            // ใช้ลูปเพื่อเช็คทุกโหนดใน parentNode
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);

                // ตรวจสอบว่าเป็นประเภทโหนด Element และตรงกับ tagName ที่ต้องการ
                if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals(tagName)) {
                    // ดึงค่าของ Text content ของโหนด
                    String nodeValue = node.getTextContent().trim();

                    // ถ้าค่ามีอยู่แล้วใน Set ก็ให้ลบโหนดนี้ออก
                    if (uniqueValues.contains(nodeValue)) {
                        parentNode.removeChild(node);
                        i--; // ลดลูปเพื่อให้ไม่ข้ามโหนดถัดไป
                    } else {
                        // ถ้าเป็นค่าที่ไม่ซ้ำกัน ก็เพิ่มค่าเข้าไปใน Set
                        uniqueValues.add(nodeValue);
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



